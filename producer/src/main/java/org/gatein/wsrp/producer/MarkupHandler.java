/*
 * JBoss, a division of Red Hat
 * Copyright 2009, Red Hat Middleware, LLC, and individual
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

package org.gatein.wsrp.producer;

import org.gatein.pc.api.PortletInvokerException;
import org.gatein.pc.api.invocation.response.ErrorResponse;
import org.gatein.pc.api.invocation.response.FragmentResponse;
import org.gatein.pc.api.invocation.response.HTTPRedirectionResponse;
import org.gatein.pc.api.invocation.response.PortletInvocationResponse;
import org.gatein.pc.api.invocation.response.UpdateNavigationalStateResponse;
import org.gatein.pc.portlet.state.producer.PortletStateChangeRequiredException;
import org.gatein.wsrp.WSRPExceptionFactory;
import org.gatein.wsrp.servlet.ServletAccess;
import org.oasis.wsrp.v1.AccessDenied;
import org.oasis.wsrp.v1.BlockingInteractionResponse;
import org.oasis.wsrp.v1.GetMarkup;
import org.oasis.wsrp.v1.InconsistentParameters;
import org.oasis.wsrp.v1.InitCookie;
import org.oasis.wsrp.v1.InteractionParams;
import org.oasis.wsrp.v1.InvalidCookie;
import org.oasis.wsrp.v1.InvalidHandle;
import org.oasis.wsrp.v1.InvalidRegistration;
import org.oasis.wsrp.v1.InvalidSession;
import org.oasis.wsrp.v1.InvalidUserCategory;
import org.oasis.wsrp.v1.MarkupResponse;
import org.oasis.wsrp.v1.MissingParameters;
import org.oasis.wsrp.v1.OperationFailed;
import org.oasis.wsrp.v1.OperationFailedFault;
import org.oasis.wsrp.v1.PerformBlockingInteraction;
import org.oasis.wsrp.v1.PortletStateChangeRequired;
import org.oasis.wsrp.v1.PortletStateChangeRequiredFault;
import org.oasis.wsrp.v1.ReleaseSessions;
import org.oasis.wsrp.v1.ReturnAny;
import org.oasis.wsrp.v1.UnsupportedLocale;
import org.oasis.wsrp.v1.UnsupportedMimeType;
import org.oasis.wsrp.v1.UnsupportedMode;
import org.oasis.wsrp.v1.UnsupportedModeFault;
import org.oasis.wsrp.v1.UnsupportedWindowState;
import org.oasis.wsrp.v1.UnsupportedWindowStateFault;

import javax.portlet.PortletModeException;
import javax.portlet.WindowStateException;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 10090 $
 * @since 2.4
 */
class MarkupHandler extends ServiceHandler implements MarkupInterface
{
   static final String PBI = "PerformBlockingInteraction";
   static final String GET_MARKUP = "GetMarkup";

   MarkupHandler(WSRPProducerImpl producer)
   {
      super(producer);
   }

   // Markup implementation ********************************************************************************************


   public MarkupResponse getMarkup(GetMarkup getMarkup) throws UnsupportedWindowState, InvalidCookie, InvalidSession, AccessDenied, InconsistentParameters, InvalidHandle, UnsupportedLocale, UnsupportedMode, OperationFailed, MissingParameters, InvalidUserCategory, InvalidRegistration, UnsupportedMimeType
   {
      WSRPExceptionFactory.throwOperationFailedIfValueIsMissing(getMarkup, GET_MARKUP);

      RequestProcessor requestProcessor = new RenderRequestProcessor(producer, getMarkup);

      String handle = requestProcessor.getPortletContext().getPortletHandle();
      PortletInvocationResponse response;
      try
      {
         log.debug("RenderInvocation on portlet '" + handle + "'");
         response = producer.getPortletInvoker().invoke(requestProcessor.getInvocation());
         log.debug("RenderInvocation done");
      }
      catch (PortletInvokerException e)
      {
         throw WSRPExceptionFactory.<OperationFailed, OperationFailedFault>throwWSException(WSRPExceptionFactory.OPERATION_FAILED,
            "Could not render portlet '" + handle + "'", e);
      }

      checkForError(response);

      return (MarkupResponse)requestProcessor.processResponse(response);
   }

   public BlockingInteractionResponse performBlockingInteraction(PerformBlockingInteraction performBlockingInteraction) throws InvalidSession, UnsupportedMode, UnsupportedMimeType, OperationFailed, UnsupportedWindowState, UnsupportedLocale, AccessDenied, PortletStateChangeRequired, InvalidRegistration, MissingParameters, InvalidUserCategory, InconsistentParameters, InvalidHandle, InvalidCookie
   {
      WSRPExceptionFactory.throwOperationFailedIfValueIsMissing(performBlockingInteraction, PBI);
      final InteractionParams interactionParams = performBlockingInteraction.getInteractionParams();
      WSRPExceptionFactory.throwMissingParametersIfValueIsMissing(interactionParams, "InteractionParams", PBI);

      RequestProcessor requestProcessor = new ActionRequestProcessor(producer, performBlockingInteraction, interactionParams);

      PortletInvocationResponse response;
      String handle = requestProcessor.getPortletContext().getPortletHandle();
      try
      {
         log.debug("ActionInvocation on portlet '" + handle + "'");
         response = producer.getPortletInvoker().invoke(requestProcessor.getInvocation());
         log.debug("ActionInvocation done");
      }
      catch (PortletStateChangeRequiredException e)
      {
         throw WSRPExceptionFactory.<PortletStateChangeRequired, PortletStateChangeRequiredFault>throwWSException(WSRPExceptionFactory.PORTLET_STATE_CHANGE_REQUIRED,
            e.getLocalizedMessage(), e);
      }
      catch (PortletInvokerException e)
      {
         throw WSRPExceptionFactory.<OperationFailed, OperationFailedFault>throwWSException(WSRPExceptionFactory.OPERATION_FAILED,
            "Could not perform action on portlet '" + handle + "'", e);
      }

      checkForError(response);

      return (BlockingInteractionResponse)requestProcessor.processResponse(response);
   }

   public ReturnAny releaseSessions(ReleaseSessions releaseSessions) throws InvalidRegistration, OperationFailed, MissingParameters, AccessDenied
   {
      // our producer never sends session ids so a Consumer trying to release sessions is an error condition
      throwOperationFaultOnSessionOperation();
      return null;
   }

   public ReturnAny initCookie(InitCookie initCookie) throws AccessDenied, OperationFailed, InvalidRegistration
   {
      WSRPExceptionFactory.throwOperationFailedIfValueIsMissing(initCookie, "InitCookie");
      producer.getRegistrationOrFailIfInvalid(initCookie.getRegistrationContext());

      // Force HTTP session creation... this is required for BEA Weblogic version < 9.2.
      // See http://jira.jboss.com/jira/browse/JBPORTAL-1220
      String sessionId = ServletAccess.getRequest().getSession().getId();
      log.debug("Got init cookie operation, created a session with id " + sessionId);

      return new ReturnAny();
   }

   static void throwOperationFaultOnSessionOperation() throws OperationFailed
   {
      throw WSRPExceptionFactory.<OperationFailed, OperationFailedFault>throwWSException(WSRPExceptionFactory.OPERATION_FAILED, "JBoss Portal's Producer" +
         " manages sessions completely on the server side, passing or trying to release sessionIDs is therefore an error.",
         null);
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
            throw WSRPExceptionFactory.<UnsupportedMode, UnsupportedModeFault>throwWSException(WSRPExceptionFactory.UNSUPPORTED_MODE,
               "Unsupported mode: " + ((PortletModeException)cause).getMode(), null);
         }
         if (cause instanceof WindowStateException)
         {
            throw WSRPExceptionFactory.<UnsupportedWindowState, UnsupportedWindowStateFault>throwWSException(WSRPExceptionFactory.UNSUPPORTED_WINDOW_STATE,
               "Unsupported window state: " + ((WindowStateException)cause).getState(), null);
         }
         // todo: deal with other exceptions

         // we're not sure what happened so throw an OperationFailedFault
         throw WSRPExceptionFactory.<OperationFailed, OperationFailedFault>throwWSException(WSRPExceptionFactory.OPERATION_FAILED,
            errorResult.getMessage(), cause);

      }
      else if (!(response instanceof HTTPRedirectionResponse || response instanceof FragmentResponse || response instanceof UpdateNavigationalStateResponse))
      {
         throw WSRPExceptionFactory.<OperationFailed, OperationFailedFault>throwWSException(WSRPExceptionFactory.OPERATION_FAILED,
            "Unsupported result type: " + response.getClass().getName(), null);
      }
   }
}
