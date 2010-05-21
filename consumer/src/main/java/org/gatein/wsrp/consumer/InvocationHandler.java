/*
 * JBoss, a division of Red Hat
 * Copyright 2010, Red Hat Middleware, LLC, and individual
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

package org.gatein.wsrp.consumer;

import org.gatein.pc.api.PortletInvokerException;
import org.gatein.pc.api.invocation.PortletInvocation;
import org.gatein.pc.api.invocation.response.ErrorResponse;
import org.gatein.pc.api.invocation.response.PortletInvocationResponse;
import org.gatein.pc.api.spi.InstanceContext;
import org.gatein.pc.api.spi.WindowContext;
import org.gatein.pc.portlet.impl.jsr168.PortletUtils;
import org.oasis.wsrp.v2.InvalidCookie;
import org.oasis.wsrp.v2.InvalidRegistration;
import org.oasis.wsrp.v2.InvalidSession;
import org.oasis.wsrp.v2.OperationFailed;
import org.oasis.wsrp.v2.RuntimeContext;
import org.oasis.wsrp.v2.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 13121 $
 * @since 2.4 (May 31, 2006)
 */
public abstract class InvocationHandler
{
   protected WSRPConsumerImpl consumer;

   protected static Logger log = LoggerFactory.getLogger(InvocationHandler.class);

   /**
    * Value indicating that we should not try further (unrecoverable error) for getMarkup and
    * processBlockingInteraction
    */
   private static final int DO_NOT_RETRY = -1;

   /** Maximum number of tries before giving up. */
   private static final int MAXIMUM_RETRY_NUMBER = 3;


   protected InvocationHandler(WSRPConsumerImpl consumer)
   {
      this.consumer = consumer;
   }

   PortletInvocationResponse handle(PortletInvocation invocation) throws PortletInvokerException
   {
      // Extracts basic required information from invocation
      RequestPrecursor requestPrecursor = new RequestPrecursor(consumer, invocation);

      // create the specific request
      Object request = prepareRequest(requestPrecursor, invocation);

      // Perform the request
      Object response = performRequest(request, invocation);
      if (response instanceof ErrorResponse)
      {
         return unwrapWSRPError((ErrorResponse)response);
      }

      return processResponse(response, invocation, requestPrecursor);
   }

   protected Object performRequest(Object request, PortletInvocation invocation) throws PortletInvokerException
   {
      int retryCount = 0;
      Object response = null;

      // as long as we don't get a non-null response and we're allowed to try again, try to perform the request
      while (response == null && retryCount++ <= MAXIMUM_RETRY_NUMBER)
      {
         if (log.isDebugEnabled())
         {
            log.debug("performRequest: " + retryCount + " attempt(s) out of " + MAXIMUM_RETRY_NUMBER + " possible");
         }
         SessionHandler sessionHandler = consumer.getSessionHandler();

         // prepare everything for the request
         updateRegistrationContext(request);
         RuntimeContext runtimeContext = getRuntimeContextFrom(request);

         if (runtimeContext != null)
         {
            WindowContext windowContext = invocation.getWindowContext();
            runtimeContext.setNamespacePrefix(getNamespaceFrom(windowContext));

            InstanceContext instanceContext = invocation.getInstanceContext();
            runtimeContext.setPortletInstanceKey(instanceContext == null ? null : instanceContext.getId());

            updateUserContext(request, consumer.getUserContextFrom(invocation, runtimeContext));
            consumer.setTemplatesIfNeeded(invocation, runtimeContext);
         }

         try
         {
            sessionHandler.initCookieIfNeeded(invocation);

            // if we need cookies, set the current group id
            sessionHandler.initProducerSessionInformation(invocation);

            response = performRequest(request);

            sessionHandler.updateCookiesIfNeeded(invocation);
         }
         catch (Exception e)
         {
            ErrorResponse errorResponse = dealWithError(e, invocation, runtimeContext);
            if (errorResponse != null)
            {
               return errorResponse;
            }
         }
         finally
         {
            // we're done: reset currently held information
            sessionHandler.resetCurrentlyHeldInformation();
         }
      }

      if (retryCount >= MAXIMUM_RETRY_NUMBER)
      {
         return new ErrorResponse(new RuntimeException("Tried to perform request " + MAXIMUM_RETRY_NUMBER
            + " times before giving up. This usually happens if an error in the WS stack prevented the messages to be " +
            "properly transmitted. Look at server.log for clues as to what happened..."));
      }

      if (log.isDebugEnabled())
      {
         log.debug("performRequest finished. Response is " + (response != null ? response.getClass().getName() : null));
      }
      return response;
   }

   static String getNamespaceFrom(WindowContext windowContext)
   {
      if (windowContext != null)
      {
         // MUST match namespace generation used in PortletResponseImpl.getNamespace in portlet module...
         return PortletUtils.generateNamespaceFrom(windowContext.getId());
      }

      return null;
   }

   /**
    * Deals with common error conditions.
    *
    * @param error          the error that is to be dealt with
    * @param invocation     the invocation that caused the error to occur
    * @param runtimeContext the current WSRP RuntimeContext
    * @return an ErrorResponse if the error couldn't be dealt with or <code>null</code> if the error was correctly
    *         handled
    */
   private ErrorResponse dealWithError(Exception error, PortletInvocation invocation, RuntimeContext runtimeContext)
      throws PortletInvokerException
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
         }
         catch (Exception e)
         {
            log.debug("Couldn't init cookie: " + e.getLocalizedMessage());
            return new ErrorResponse(e);
         }
      }
      else if (error instanceof InvalidSession)
      {
         log.debug("Session invalidated after InvalidSessionFault, will re-send session-stored information.");
         sessionHandler.handleInvalidSessionFault(invocation, runtimeContext);
      }
      else if (error instanceof InvalidRegistration)
      {
         log.debug("Invalid registration");
         consumer.handleInvalidRegistrationFault();
      }
      else
      {
         // other errors cannot be dealt with: we have an error condition
         return new ErrorResponse(error);
      }
      return null;
   }

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

   protected abstract void updateUserContext(Object request, UserContext userContext);

   protected abstract void updateRegistrationContext(Object request) throws PortletInvokerException;

   protected abstract RuntimeContext getRuntimeContextFrom(Object request);

   protected abstract Object performRequest(Object request) throws Exception;

   protected abstract Object prepareRequest(RequestPrecursor requestPrecursor, PortletInvocation invocation);

   protected abstract PortletInvocationResponse processResponse(Object response, PortletInvocation invocation, RequestPrecursor requestPrecursor) throws PortletInvokerException;

}
