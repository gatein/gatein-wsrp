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

package org.gatein.wsrp.producer.handlers;

import org.gatein.pc.api.PortletInvokerException;
import org.gatein.pc.api.invocation.PortletInvocation;
import org.gatein.pc.api.invocation.response.ContentResponse;
import org.gatein.pc.api.invocation.response.ErrorResponse;
import org.gatein.pc.api.invocation.response.FragmentResponse;
import org.gatein.pc.api.invocation.response.HTTPRedirectionResponse;
import org.gatein.pc.api.invocation.response.PortletInvocationResponse;
import org.gatein.pc.api.invocation.response.UpdateNavigationalStateResponse;
import org.gatein.pc.portlet.state.producer.PortletStateChangeRequiredException;
import org.gatein.registration.Registration;
import org.gatein.registration.RegistrationLocal;
import org.gatein.wsrp.api.extensions.InvocationHandlerDelegate;
import org.gatein.wsrp.api.servlet.ServletAccess;
import org.gatein.wsrp.producer.MarkupInterface;
import org.gatein.wsrp.producer.Utils;
import org.gatein.wsrp.producer.WSRPProducerImpl;
import org.gatein.wsrp.producer.handlers.processors.ProcessorFactory;
import org.gatein.wsrp.producer.handlers.processors.RequestProcessor;
import org.gatein.wsrp.spec.v2.WSRP2ExceptionFactory;
import org.oasis.wsrp.v2.AccessDenied;
import org.oasis.wsrp.v2.BlockingInteractionResponse;
import org.oasis.wsrp.v2.Extension;
import org.oasis.wsrp.v2.GetMarkup;
import org.oasis.wsrp.v2.GetResource;
import org.oasis.wsrp.v2.HandleEvents;
import org.oasis.wsrp.v2.HandleEventsResponse;
import org.oasis.wsrp.v2.InconsistentParameters;
import org.oasis.wsrp.v2.InitCookie;
import org.oasis.wsrp.v2.InvalidCookie;
import org.oasis.wsrp.v2.InvalidHandle;
import org.oasis.wsrp.v2.InvalidRegistration;
import org.oasis.wsrp.v2.InvalidSession;
import org.oasis.wsrp.v2.InvalidUserCategory;
import org.oasis.wsrp.v2.MarkupResponse;
import org.oasis.wsrp.v2.MissingParameters;
import org.oasis.wsrp.v2.ModifyRegistrationRequired;
import org.oasis.wsrp.v2.OperationFailed;
import org.oasis.wsrp.v2.OperationNotSupported;
import org.oasis.wsrp.v2.PerformBlockingInteraction;
import org.oasis.wsrp.v2.PortletStateChangeRequired;
import org.oasis.wsrp.v2.ReleaseSessions;
import org.oasis.wsrp.v2.ResourceResponse;
import org.oasis.wsrp.v2.ResourceSuspended;
import org.oasis.wsrp.v2.UnsupportedLocale;
import org.oasis.wsrp.v2.UnsupportedMimeType;
import org.oasis.wsrp.v2.UnsupportedMode;
import org.oasis.wsrp.v2.UnsupportedWindowState;

import javax.portlet.PortletModeException;
import javax.portlet.WindowStateException;
import java.util.Collections;
import java.util.List;

/**
 * Handles WSRP Markup requests on behalf of the producer.
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 10090 $
 * @since 2.4
 */
public class MarkupHandler extends ServiceHandler implements MarkupInterface
{
   public MarkupHandler(WSRPProducerImpl producer)
   {
      super(producer);
   }

   // Markup implementation ********************************************************************************************


   public MarkupResponse getMarkup(GetMarkup getMarkup)
      throws AccessDenied, InconsistentParameters, InvalidCookie, InvalidHandle, InvalidRegistration, InvalidSession,
      InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, ResourceSuspended,
      UnsupportedLocale, UnsupportedMimeType, UnsupportedMode, UnsupportedWindowState
   {
      try
      {
         return invoke(getMarkup);
      }
      catch (PortletStateChangeRequired portletStateChangeRequired)
      {
         throw WSRP2ExceptionFactory.throwWSException(OperationFailed.class, "Shouldn't happen", portletStateChangeRequired);
      }
      catch (OperationNotSupported operationNotSupported)
      {
         throw WSRP2ExceptionFactory.throwWSException(OperationFailed.class, "Shouldn't happen", operationNotSupported);
      }
   }

   public ResourceResponse getResource(GetResource getResource)
      throws AccessDenied, InconsistentParameters, InvalidCookie, InvalidHandle, InvalidRegistration, InvalidSession,
      InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported,
      ResourceSuspended, UnsupportedLocale, UnsupportedMimeType, UnsupportedMode, UnsupportedWindowState
   {
      try
      {
         return invoke(getResource);
      }
      catch (PortletStateChangeRequired portletStateChangeRequired)
      {
         throw WSRP2ExceptionFactory.throwWSException(OperationFailed.class, "Shouldn't happen", portletStateChangeRequired);
      }
   }

   public BlockingInteractionResponse performBlockingInteraction(PerformBlockingInteraction performBlockingInteraction)
      throws AccessDenied, InconsistentParameters, InvalidCookie, InvalidHandle, InvalidRegistration, InvalidSession,
      InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, PortletStateChangeRequired,
      ResourceSuspended, UnsupportedLocale, UnsupportedMimeType, UnsupportedMode, UnsupportedWindowState
   {
      try
      {
         return invoke(performBlockingInteraction);
      }
      catch (OperationNotSupported operationNotSupported)
      {
         throw WSRP2ExceptionFactory.throwWSException(OperationFailed.class, "Shouldn't happen", operationNotSupported);
      }
   }

   public List<Extension> releaseSessions(ReleaseSessions releaseSessions)
      throws AccessDenied, InvalidRegistration, MissingParameters, ModifyRegistrationRequired, OperationFailed,
      OperationNotSupported, ResourceSuspended
   {
      // our producer never sends session ids so a Consumer trying to release sessions is an error condition
      Utils.throwOperationFaultOnSessionOperation();
      return null;
   }

   public List<Extension> initCookie(InitCookie initCookie)
      throws AccessDenied, InvalidRegistration, ModifyRegistrationRequired, OperationFailed, OperationNotSupported,
      ResourceSuspended
   {
      WSRP2ExceptionFactory.throwOperationFailedIfValueIsMissing(initCookie, "InitCookie");
      producer.getRegistrationOrFailIfInvalid(initCookie.getRegistrationContext());

      // Force HTTP session creation... this is required for BEA Weblogic version < 9.2.
      // See http://jira.jboss.com/jira/browse/JBPORTAL-1220
      String sessionId = ServletAccess.getRequest().getSession().getId();
      log.debug("Got init cookie operation, created a session with id " + sessionId);

      return Collections.emptyList();
   }

   public HandleEventsResponse handleEvents(HandleEvents handleEvents)
      throws AccessDenied, InconsistentParameters, InvalidCookie, InvalidHandle, InvalidRegistration, InvalidSession,
      InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported,
      PortletStateChangeRequired, ResourceSuspended, UnsupportedLocale, UnsupportedMimeType, UnsupportedMode,
      UnsupportedWindowState
   {
      return invoke(handleEvents);
   }

   private <Request, Response> Response invoke(Request request) throws OperationFailed, ModifyRegistrationRequired, InvalidRegistration, UnsupportedMimeType, MissingParameters, UnsupportedLocale, InvalidHandle, UnsupportedWindowState, UnsupportedMode, PortletStateChangeRequired, OperationNotSupported
   {
      RequestProcessor<Request, Response> requestProcessor = ProcessorFactory.getProcessorFor(producer, request);

      final PortletInvocationResponse response;
      String handle = requestProcessor.getPortletContext().getPortletHandle();
      String invocationType = request.getClass().getSimpleName();

      try
      {
         log.debug(invocationType + " on portlet '" + handle + "'");

         Registration registration = producer.getRegistrationOrFailIfInvalid(requestProcessor.getRegistrationContext());
         RegistrationLocal.setRegistration(registration);
         final PortletInvocation invocation = requestProcessor.getInvocation();

         final InvocationHandlerDelegate delegate = InvocationHandlerDelegate.producerDelegate();
         if (delegate != null)
         {
            delegate.processInvocation(invocation);
         }

         response = producer.getPortletInvoker().invoke(invocation);

         if (delegate != null)
         {
            delegate.processInvocationResponse(response, invocation);
         }

         log.debug(invocationType + " done");

      }
      catch (PortletStateChangeRequiredException e)
      {
         throw WSRP2ExceptionFactory.throwWSException(PortletStateChangeRequired.class, e.getLocalizedMessage(), e);
      }
      catch (PortletInvokerException e)
      {
         throw WSRP2ExceptionFactory.throwWSException(OperationFailed.class, "Could not perform " + invocationType + " on portlet '" + handle + "'", e);
      }

      checkForError(response);

      return requestProcessor.processResponse(response);
   }

   private void checkForError(PortletInvocationResponse response)
      throws UnsupportedMode, OperationFailed, UnsupportedWindowState
   {
      if (response instanceof ErrorResponse)
      {
         ErrorResponse errorResult = (ErrorResponse)response;
         Throwable cause = errorResult.getCause();
         if (cause instanceof PortletModeException)
         {
            throw WSRP2ExceptionFactory.throwWSException(UnsupportedMode.class, "Unsupported mode: " + ((PortletModeException)cause).getMode(), null);
         }
         if (cause instanceof WindowStateException)
         {
            throw WSRP2ExceptionFactory.throwWSException(UnsupportedWindowState.class, "Unsupported window state: " + ((WindowStateException)cause).getState(), null);
         }
         // todo: deal with other exceptions

         // we're not sure what happened so throw an OperationFailedFault
         throw WSRP2ExceptionFactory.throwWSException(OperationFailed.class, errorResult.getMessage(), cause);

      }
      else if (!(response instanceof HTTPRedirectionResponse || response instanceof FragmentResponse || response instanceof UpdateNavigationalStateResponse || response instanceof ContentResponse))
      {
         throw WSRP2ExceptionFactory.throwWSException(OperationFailed.class, "Unsupported result type: " + response.getClass().getName(), null);
      }
   }
}
