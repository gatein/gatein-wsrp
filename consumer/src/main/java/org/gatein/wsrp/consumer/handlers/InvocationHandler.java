/*
 * JBoss, a division of Red Hat
 * Copyright 2011, Red Hat Middleware, LLC, and individual
 * contributors as indicated by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of
 * individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.gatein.wsrp.consumer.handlers;

import org.gatein.common.net.media.MediaType;
import org.gatein.common.util.ParameterValidation;
import org.gatein.pc.api.PortletInvokerException;
import org.gatein.pc.api.StateString;
import org.gatein.pc.api.invocation.PortletInvocation;
import org.gatein.pc.api.invocation.response.ErrorResponse;
import org.gatein.pc.api.invocation.response.PortletInvocationResponse;
import org.gatein.pc.api.spi.PortletInvocationContext;
import org.gatein.pc.api.spi.SecurityContext;
import org.gatein.pc.api.spi.WindowContext;
import org.gatein.wsrp.WSRPConstants;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.WSRPUtils;
import org.gatein.wsrp.api.extensions.ExtensionAccess;
import org.gatein.wsrp.api.extensions.UnmarshalledExtension;
import org.gatein.wsrp.consumer.WSRPConsumerImpl;
import org.gatein.wsrp.consumer.portlet.info.WSRPPortletInfo;
import org.gatein.wsrp.consumer.spi.WSRPConsumerSPI;
import org.gatein.wsrp.payload.PayloadUtils;
import org.gatein.wsrp.spec.v2.WSRP2RewritingConstants;
import org.oasis.wsrp.v2.Extension;
import org.oasis.wsrp.v2.InvalidCookie;
import org.oasis.wsrp.v2.InvalidRegistration;
import org.oasis.wsrp.v2.InvalidSession;
import org.oasis.wsrp.v2.MarkupParams;
import org.oasis.wsrp.v2.ModifyRegistrationRequired;
import org.oasis.wsrp.v2.NavigationalContext;
import org.oasis.wsrp.v2.OperationFailed;
import org.oasis.wsrp.v2.PortletContext;
import org.oasis.wsrp.v2.RegistrationContext;
import org.oasis.wsrp.v2.RuntimeContext;
import org.oasis.wsrp.v2.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Handles a specific type of PortletInvocation, translating it back and forth into WSRP-understable structures.
 *
 * @param <Invocation> the type of PortletInvocation this InvocationHandler handles
 * @param <Request>    the type of WSRP request this InvocationHandler can translate to from a portlet container request
 * @param <Response>   the type of WSRP response this InvocationHandler can translate back to portlet container responses
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 13121 $
 * @since 2.4 (May 31, 2006)
 */
public abstract class InvocationHandler<Invocation extends PortletInvocation, Request, Response>
{
   /** The consumer owning this handler */
   protected final WSRPConsumerSPI consumer;

   protected static Logger log = LoggerFactory.getLogger(InvocationHandler.class);
   protected static boolean debug = log.isDebugEnabled();
   protected static boolean trace = log.isTraceEnabled();

   /**
    * Value indicating that we should not try further (unrecoverable error) for getMarkup and
    * processBlockingInteraction
    */
   private static final int DO_NOT_RETRY = -1;

   /** Maximum number of tries before giving up. */
   private static final int MAXIMUM_RETRY_NUMBER = 3;

   protected InvocationHandler(WSRPConsumerSPI consumer)
   {
      this.consumer = consumer;
   }

   /**
    * Translates a portlet container request into a WSRP request, calls the appropriate WSRP operation and translates the received response back into something the portlet
    * container can deal with, taking care of any exception, dealing with the ones we can or transforming them into portlet container exceptions when we can't deal with them
    * ourselves. Follows the Template Method design pattern.
    *
    * @param invocation the initiating portlet container request that will be transformed into a WSRP request
    * @return an appropriate PortletInvocationResponse translated from the WSRP response sent by the producer
    * @throws PortletInvokerException
    */
   public PortletInvocationResponse handle(Invocation invocation) throws PortletInvokerException
   {
      // Extracts basic, common required information from invocation
      RequestPrecursor<Invocation> requestPrecursor = new RequestPrecursor<Invocation>(consumer, invocation);

      // create the specific request, customizing it with specific parameters if needed
      Request request = prepareRequest(requestPrecursor, invocation);

      try
      {
         // Perform the request and get the response
         Response response = performRequest(request, invocation);

         // process the response
         return processResponse(response, invocation, requestPrecursor);
      }
      catch (Exception e)
      {
         // if we didn't get a straight PortletInvokerException (which means we already asserted that the WSRP can't deal with it), try to transform it into something we can deal with
         if (!(e instanceof PortletInvokerException))
         {
            final PortletInvocationResponse response = dealWithError(e, invocation, getRuntimeContextFrom(request));
            if (response instanceof ErrorResponse)
            {
               return unwrapWSRPError((ErrorResponse)response);
            }

            return response;
         }
         else
         {
            throw (PortletInvokerException)e;
         }
      }
   }

   /**
    * Attempts to perform the specified request, taking care of setting and updating cookies if required, at most {@link #MAXIMUM_RETRY_NUMBER} times to give the consumer the
    * opportunity to react to specific errors (such as need to invoke initCookie or modifyRegistration) that can sometimes be recovered from.
    *
    * @param request    the request to perform
    * @param invocation the PortletInvocation that initiated the current WSRP request
    * @return the producer's reponse
    * @throws Exception
    */
   protected Response performRequest(Request request, PortletInvocation invocation) throws Exception
   {
      int retryCount = 0;
      Response response = null;

      // as long as we don't get a non-null response and we're allowed to try again, try to perform the request
      while (response == null && retryCount++ <= MAXIMUM_RETRY_NUMBER)
      {
         if (debug)
         {
            log.debug("performRequest: " + retryCount + " attempt(s) out of " + MAXIMUM_RETRY_NUMBER + " possible");
         }
         SessionHandler sessionHandler = consumer.getSessionHandler();

         // prepare everything for the request
         RuntimeContext runtimeContext = getRuntimeContextFrom(request);

         if (runtimeContext != null)
         {
            WindowContext windowContext = invocation.getWindowContext();
            runtimeContext.setNamespacePrefix(WSRPTypeFactory.getNamespaceFrom(windowContext));

            // GTNWSRP-369: InstanceContext doesn't actually provide any useful information, use WindowContext's id instead
            /*InstanceContext instanceContext = invocation.getInstanceContext();
            runtimeContext.setPortletInstanceKey(WSRPTypeFactory.getPortletInstanceKey(instanceContext));*/
            runtimeContext.setPortletInstanceKey(windowContext.getId());
         }

         try
         {
            sessionHandler.initCookieIfNeeded(invocation);

            response = performRequest(request);

            sessionHandler.updateCookiesIfNeeded(invocation);
         }
         finally
         {
            // we're done: reset currently held information
            sessionHandler.resetCurrentlyHeldInformation();
         }
      }

      if (retryCount >= MAXIMUM_RETRY_NUMBER)
      {
         throw new RuntimeException("Tried to perform request " + MAXIMUM_RETRY_NUMBER
            + " times before giving up. This usually happens if an error in the WS stack prevented the messages to be " +
            "properly transmitted. Look at server.log for clues as to what happened...");
      }

      if (debug)
      {
         log.debug("performRequest finished. Response is " + (response != null ? response.getClass().getName() : null));
      }
      return response;
   }

   /**
    * Deals with common error conditions.
    *
    * @param error          the error that is to be dealt with
    * @param invocation     the invocation that caused the error to occur
    * @param runtimeContext the current WSRP RuntimeContext
    * @return an ErrorResponse if the error couldn't be dealt with or <code>null</code> if the error was correctly
    * handled
    */
   private PortletInvocationResponse dealWithError(Exception error, Invocation invocation, RuntimeContext runtimeContext) throws PortletInvokerException
   {
      log.error("The portlet threw an exception", error);

      SessionHandler sessionHandler = consumer.getSessionHandler();

      // recoverable errors
      if (error instanceof InvalidCookie)
      {
         // we need to re-init the cookies
         log.debug("Re-initializing cookies after InvalidCookieFault.");
         // force a producer info refresh because the invalid cookie might be due to a change of cookie policy on the producer
         consumer.refreshProducerInfo();
         try
         {
            sessionHandler.initCookieIfNeeded(invocation);

            // re-attempt invocation since we can recover from this error
            return handle(invocation);
         }
         catch (Exception e)
         {
            log.debug("Couldn't init cookie: " + e.getLocalizedMessage());
            return new ErrorResponse(e);
         }
      }
      else if (error instanceof InvalidSession)
      {
         // invalidate the currently held session information
         log.debug("Session invalidated after InvalidSessionFault, will re-send session-stored information.");
         sessionHandler.handleInvalidSessionFault(invocation, runtimeContext);

         // and re-attempt invocation as we can recover from this
         return handle(invocation);
      }
      else if (error instanceof InvalidRegistration)
      {
         // invalidate the registration information, we can't recover from this, the user will have to check the admin UI to see what's wrong
         consumer.handleInvalidRegistrationFault();

         return new ErrorResponse(error);
      }
      else if (error instanceof ModifyRegistrationRequired)
      {
         // we can't recover from this, the user will need to check the admin UI to see how to modify the current registration to make it comply with the new producer's requirements
         consumer.handleModifyRegistrationRequiredFault();

         return new ErrorResponse(error);
      }
      else if (consumer.getProducerInfo().canAttemptRecoveryFrom(error))
      {
         return new WSErrorResponse(error);
      }
      else
      {
         // other errors cannot be dealt with: we have an error condition
         return new ErrorResponse(error);
      }
   }

   /**
    * Attempts to unwrap nested errors to make them more palatable to users.
    *
    * @param errorResponse the error response we're trying to make simpler
    * @return hopefully, a simplified error response, one that more clearly identifies the root issue
    */
   protected ErrorResponse unwrapWSRPError(ErrorResponse errorResponse)
   {
      Throwable cause = errorResponse.getCause();
      if (cause != null)
      {
         // unwrap original exception...
         if (cause instanceof OperationFailed && cause.getCause() != null)
         {
            cause = cause.getCause();
         }
         else if (cause instanceof RemoteException)
         {
            cause = ((RemoteException)cause).detail;
         }
         log.debug("Invocation of action failed: " + cause.getMessage(), cause); // fix-me?
         return new ErrorResponse(cause);
      }
      else
      {
         log.debug("Invocation of action failed: " + errorResponse.getMessage());
         return errorResponse;
      }
   }

   // template method hook points

   /**
    * Extracts the RuntimeContext from the specific WSRP request.
    *
    * @param request the request to extract a RuntimeContext from
    * @return the RuntimeContext instance associated with the specified request
    */
   protected abstract RuntimeContext getRuntimeContextFrom(Request request);

   /**
    * Performs the actual specific WSRP call for the specified request.
    *
    * @param request the WSRP request to perform
    * @return the producer's response
    * @throws Exception
    */
   protected abstract Response performRequest(Request request) throws Exception;

   /**
    * Created and further prepares the specific requests based on common extracted information from the specified RequestPrecursor and the originating portlet invocation.
    *
    * @param requestPrecursor the common extracted information for this request
    * @param invocation       the portlet invocation from which we're trying to perform a WSRP call
    * @return the fully prepared request
    */
   protected abstract Request prepareRequest(RequestPrecursor<Invocation> requestPrecursor, Invocation invocation);

   /**
    * Converts the WSRP response into a portlet container {@link PortletInvocationResponse} based on its type and on whether other WSRP components need to be informed of potential
    * changes from the producer.
    *
    * @param response         the original WSRP response
    * @param invocation       the PortletInvocation that triggered the WSRP call
    * @param requestPrecursor the request precursor information we extracted before performing the request
    * @return the appropriate PortletInvocationResponse based on the WSRP producer's response
    * @throws PortletInvokerException
    */
   protected abstract PortletInvocationResponse processResponse(Response response, Invocation invocation, RequestPrecursor<Invocation> requestPrecursor) throws PortletInvokerException;

   public static class WSErrorResponse extends ErrorResponse
   {

      public WSErrorResponse(Throwable cause)
      {
         super(cause);
      }
   }

   /**
    * Extracts extensions from the response.
    *
    * @param response the WSRP response to extract extensions from
    * @return a potentially empty list of extensions for the specified response
    */
   protected abstract List<Extension> getExtensionsFrom(Response response);

   /**
    * Processes extensions, making them available if needed to the {@link org.gatein.wsrp.api.extensions.ConsumerExtensionAccessor}. Used by subclasses.
    */
   protected void processExtensions(Response response)
   {
      final List<Extension> extensions = WSRPUtils.replaceByEmptyListIfNeeded(getExtensionsFrom(response));
      for (Extension extension : extensions)
      {
         try
         {
            final UnmarshalledExtension unmarshalledExtension = PayloadUtils.unmarshallExtension(extension.getAny());
            ExtensionAccess.getConsumerExtensionAccessor().addResponseExtension(response.getClass(), unmarshalledExtension);
         }
         catch (Exception e)
         {
            log.debug("Couldn't unmarshall extension from producer, ignoring it.", e);
         }
      }
   }

   /**
    * Extracts basic required elements for all invocation requests.
    *
    * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
    * @version $Revision: 13121 $
    * @since 2.4
    */
   protected static class RequestPrecursor<Invocation extends PortletInvocation>
   {
      private static final Logger log = LoggerFactory.getLogger(RequestPrecursor.class);

      private final PortletContext portletContext;
      private final RuntimeContext runtimeContext;
      private final MarkupParams markupParams;
      private final RegistrationContext registrationContext;
      private final UserContext userContext;
      private static final String PORTLET_HANDLE = "portlet handle";
      private static final String SECURITY_CONTEXT = "security context";
      private static final String USER_CONTEXT = "user context";
      private static final String INVOCATION_CONTEXT = "invocation context";
      private static final String CONTENT_TYPE = "response content type in invocation context";
      private static final String USER_AGENT = "User-Agent";

      public RequestPrecursor(WSRPConsumerSPI wsrpConsumer, Invocation invocation) throws PortletInvokerException
      {
         // retrieve handle
         portletContext = WSRPUtils.convertToWSRPPortletContext(WSRPConsumerImpl.getPortletContext(invocation));
         ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(getPortletHandle(), PORTLET_HANDLE, null);
         if (log.isDebugEnabled())
         {
            log.debug("About to invoke on portlet: " + getPortletHandle());
         }

         // registration context
         registrationContext = wsrpConsumer.getRegistrationContext();


         // create runtime context
         SecurityContext securityContext = invocation.getSecurityContext();
         ParameterValidation.throwIllegalArgExceptionIfNull(securityContext, SECURITY_CONTEXT);
         String authType = WSRPUtils.convertRequestAuthTypeToWSRPAuthType(securityContext.getAuthType());

         String portletInstanceKey = WSRPTypeFactory.getPortletInstanceKey(invocation.getInstanceContext());

         String namespacePrefix = WSRPTypeFactory.getNamespacePrefix(invocation.getWindowContext(), getPortletHandle());

         runtimeContext = WSRPTypeFactory.createRuntimeContext(authType, portletInstanceKey, namespacePrefix);

         WSRPPortletInfo info = wsrpConsumer.getPortletInfo(invocation);

         // user context
         userContext = wsrpConsumer.getUserContextFrom(info, invocation, runtimeContext);

         // templates
         wsrpConsumer.setTemplatesIfNeeded(info, invocation, getRuntimeContext());

         // set the session id if needed
         wsrpConsumer.getSessionHandler().setSessionIdIfNeeded(invocation, getRuntimeContext(), getPortletHandle());

         // create markup params
         org.gatein.pc.api.spi.UserContext userContext = invocation.getUserContext();
         ParameterValidation.throwIllegalArgExceptionIfNull(userContext, USER_CONTEXT);
         PortletInvocationContext context = invocation.getContext();
         ParameterValidation.throwIllegalArgExceptionIfNull(context, INVOCATION_CONTEXT);
         final MediaType contentType = context.getResponseContentType();
         ParameterValidation.throwIllegalArgExceptionIfNull(contentType, CONTENT_TYPE);

         String mode;
         try
         {
            mode = WSRPUtils.getWSRPNameFromJSR168PortletMode(invocation.getMode());
         }
         catch (Exception e)
         {
            log.debug("Mode was null in context.");
            mode = WSRPConstants.VIEW_MODE;
         }

         String windowState;
         try
         {
            windowState = WSRPUtils.getWSRPNameFromJSR168WindowState(invocation.getWindowState());
         }
         catch (Exception e)
         {
            log.debug("WindowState was null in context.");
            windowState = WSRPConstants.NORMAL_WINDOW_STATE;
         }

         this.markupParams = WSRPTypeFactory.createMarkupParams(securityContext.isSecure(),
            WSRPUtils.convertLocalesToRFC3066LanguageTags(userContext.getLocales()),
            Collections.singletonList(contentType.getValue()), mode, windowState);
         String userAgent = WSRPConsumerImpl.getHttpRequest(invocation).getHeader(USER_AGENT);
         getMarkupParams().setClientData(WSRPTypeFactory.createClientData(userAgent));
         getMarkupParams().getExtensions().addAll(ExtensionAccess.getConsumerExtensionAccessor().getRequestExtensionsFor(MarkupParams.class));

         // navigational state
         StateString navigationalState = invocation.getNavigationalState();
         Map<String, String[]> publicNavigationalState = invocation.getPublicNavigationalState();

         // it is possible to get additional public navigational state from the invocation attributes if the producer used templates:
         String publicNS = (String)invocation.getAttribute(WSRP2RewritingConstants.NAVIGATIONAL_VALUES);
         if (!ParameterValidation.isNullOrEmpty(publicNS))
         {
            publicNavigationalState.putAll(WSRPUtils.decodePublicNS(publicNS));
         }

         NavigationalContext navigationalContext = WSRPTypeFactory.createNavigationalContextOrNull(navigationalState, publicNavigationalState);
         getMarkupParams().setNavigationalContext(navigationalContext);

         if (log.isDebugEnabled())
         {
            log.debug(WSRPUtils.toString(getMarkupParams()));
         }
      }

      public String getPortletHandle()
      {
         return portletContext.getPortletHandle();
      }


      public PortletContext getPortletContext()
      {
         return portletContext;
      }

      public RegistrationContext getRegistrationContext()
      {
         return registrationContext;
      }

      public UserContext getUserContext()
      {
         return userContext;
      }

      public RuntimeContext getRuntimeContext()
      {
         return runtimeContext;
      }

      public MarkupParams getMarkupParams()
      {
         return markupParams;
      }
   }
}
