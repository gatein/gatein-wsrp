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

package org.gatein.wsrp.producer.handlers.processors;

import org.gatein.pc.api.StateString;
import org.gatein.pc.api.invocation.ActionInvocation;
import org.gatein.pc.api.invocation.PortletInvocation;
import org.gatein.pc.api.invocation.response.HTTPRedirectionResponse;
import org.gatein.pc.api.invocation.response.PortletInvocationResponse;
import org.gatein.pc.api.invocation.response.UpdateNavigationalStateResponse;
import org.gatein.pc.api.state.AccessMode;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.WSRPUtils;
import org.gatein.wsrp.producer.handlers.MarkupHandler;
import org.gatein.wsrp.spec.v2.WSRP2ExceptionFactory;
import org.oasis.wsrp.v2.BlockingInteractionResponse;
import org.oasis.wsrp.v2.Extension;
import org.oasis.wsrp.v2.InteractionParams;
import org.oasis.wsrp.v2.InvalidHandle;
import org.oasis.wsrp.v2.InvalidRegistration;
import org.oasis.wsrp.v2.MimeRequest;
import org.oasis.wsrp.v2.MissingParameters;
import org.oasis.wsrp.v2.ModifyRegistrationRequired;
import org.oasis.wsrp.v2.OperationFailed;
import org.oasis.wsrp.v2.OperationNotSupported;
import org.oasis.wsrp.v2.PerformBlockingInteraction;
import org.oasis.wsrp.v2.PortletContext;
import org.oasis.wsrp.v2.RegistrationContext;
import org.oasis.wsrp.v2.RuntimeContext;
import org.oasis.wsrp.v2.StateChange;
import org.oasis.wsrp.v2.UnsupportedLocale;
import org.oasis.wsrp.v2.UnsupportedMimeType;
import org.oasis.wsrp.v2.UnsupportedMode;
import org.oasis.wsrp.v2.UnsupportedWindowState;
import org.oasis.wsrp.v2.UpdateResponse;

import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 13121 $
 * @since 2.6
 */
class ActionRequestProcessor extends UpdateNavigationalStateResponseProcessor<PerformBlockingInteraction, BlockingInteractionResponse>
{
   ActionRequestProcessor(ProducerHelper producer, PerformBlockingInteraction performBlockingInteraction) throws MissingParameters, InvalidRegistration, UnsupportedLocale,
      UnsupportedMimeType, UnsupportedWindowState, OperationFailed, InvalidHandle, UnsupportedMode, ModifyRegistrationRequired, OperationNotSupported
   {
      super(producer, performBlockingInteraction);
   }


   @Override
   protected void checkRequest(PerformBlockingInteraction performBlockingInteraction) throws MissingParameters
   {
      final InteractionParams interactionParams = performBlockingInteraction.getInteractionParams();
      WSRP2ExceptionFactory.throwMissingParametersIfValueIsMissing(interactionParams, "InteractionParams", performBlockingInteraction.getClass().getSimpleName());
   }

   public RegistrationContext getRegistrationContext()
   {
      return request.getRegistrationContext();
   }

   RuntimeContext getRuntimeContext()
   {
      return request.getRuntimeContext();
   }

   MimeRequest getParams()
   {
      return request.getMarkupParams();
   }

   public PortletContext getPortletContext()
   {
      return request.getPortletContext();
   }

   org.oasis.wsrp.v2.UserContext getUserContext()
   {
      return request.getUserContext();
   }

   String getContextName()
   {
      return MarkupHandler.PBI;
   }

   AccessMode getAccessMode() throws MissingParameters
   {
      StateChange stateChange = request.getInteractionParams().getPortletStateChange();
      WSRP2ExceptionFactory.throwMissingParametersIfValueIsMissing(stateChange, "portletStateChange", "InteractionParams");
      return WSRPUtils.getAccessModeFromStateChange(stateChange);
   }

   PortletInvocation initInvocation(WSRPPortletInvocationContext context)
   {
      ActionInvocation invocation = new ActionInvocation(context);
      InteractionParams interactionParams = request.getInteractionParams();

      // Request context
      WSRPRequestContext requestContext = WSRPRequestContext.createRequestContext(markupRequest, interactionParams);
      invocation.setRequestContext(requestContext);

      // Interaction state, navigational state is already taken care of in RequestProcessor.prepareInvocation
      StateString interactionState = createNavigationalState(interactionParams.getInteractionState());
      invocation.setInteractionState(interactionState);

      // Form parameters
      invocation.setForm(requestContext.getForm());

      // Extensions
      processExtensionsFrom(interactionParams.getClass(), interactionParams.getExtensions());

      return invocation;
   }

   @Override
   List<Extension> getResponseExtensionsFor(BlockingInteractionResponse blockingInteractionResponse)
   {
      return blockingInteractionResponse.getExtensions();
   }

   protected BlockingInteractionResponse internalProcessResponse(PortletInvocationResponse response)
   {
      if (response instanceof UpdateNavigationalStateResponse)
      {
         UpdateNavigationalStateResponse stateResponse = (UpdateNavigationalStateResponse)response;
         UpdateResponse updateResponse = createUpdateResponse(stateResponse);

         return WSRPTypeFactory.createBlockingInteractionResponse(updateResponse);
      }
      else
      {
         // result should be HTTPRedirectionResult
         HTTPRedirectionResponse redirectionResult = (HTTPRedirectionResponse)response;
         return WSRPTypeFactory.createBlockingInteractionResponse(redirectionResult.getLocation());
      }
   }
}
