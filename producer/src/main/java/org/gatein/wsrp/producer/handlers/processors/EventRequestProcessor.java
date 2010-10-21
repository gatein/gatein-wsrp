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

package org.gatein.wsrp.producer.handlers.processors;

import org.gatein.common.NotYetImplemented;
import org.gatein.common.util.ParameterValidation;
import org.gatein.pc.api.invocation.EventInvocation;
import org.gatein.pc.api.invocation.PortletInvocation;
import org.gatein.pc.api.invocation.response.PortletInvocationResponse;
import org.gatein.pc.api.invocation.response.UpdateNavigationalStateResponse;
import org.gatein.pc.api.state.AccessMode;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.WSRPUtils;
import org.gatein.wsrp.payload.PayloadUtils;
import org.gatein.wsrp.producer.handlers.MarkupHandler;
import org.gatein.wsrp.spec.v2.WSRP2ExceptionFactory;
import org.oasis.wsrp.v2.Event;
import org.oasis.wsrp.v2.EventParams;
import org.oasis.wsrp.v2.HandleEvents;
import org.oasis.wsrp.v2.HandleEventsResponse;
import org.oasis.wsrp.v2.InvalidHandle;
import org.oasis.wsrp.v2.InvalidRegistration;
import org.oasis.wsrp.v2.MimeRequest;
import org.oasis.wsrp.v2.MissingParameters;
import org.oasis.wsrp.v2.OperationFailed;
import org.oasis.wsrp.v2.OperationNotSupported;
import org.oasis.wsrp.v2.PortletContext;
import org.oasis.wsrp.v2.RegistrationContext;
import org.oasis.wsrp.v2.RuntimeContext;
import org.oasis.wsrp.v2.StateChange;
import org.oasis.wsrp.v2.UnsupportedMimeType;
import org.oasis.wsrp.v2.UnsupportedMode;
import org.oasis.wsrp.v2.UnsupportedWindowState;
import org.oasis.wsrp.v2.UpdateResponse;
import org.oasis.wsrp.v2.UserContext;

import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
class EventRequestProcessor extends UpdateNavigationalStateResponseProcessor<HandleEventsResponse>
{
   private HandleEvents handleEvents;

   public EventRequestProcessor(ProducerHelper producer, HandleEvents handleEvents) throws OperationFailed, UnsupportedMode, InvalidHandle, MissingParameters, UnsupportedMimeType, UnsupportedWindowState, InvalidRegistration, OperationNotSupported
   {
      super(producer);
      this.handleEvents = handleEvents;

      // validate request parameters
      EventParams eventParams = handleEvents.getEventParams();
      WSRP2ExceptionFactory.throwMissingParametersIfValueIsMissing(eventParams, "event params", getContextName());
      WSRP2ExceptionFactory.throwMissingParametersIfValueIsMissing(eventParams.getPortletStateChange(), "portletStateChange", "EventParams");
      List<Event> events = eventParams.getEvents();
      if (!ParameterValidation.existsAndIsNotEmpty(events))
      {
         throw WSRP2ExceptionFactory.createWSException(MissingParameters.class,
            "EventParams must provide at least one event to process", null);
      }

      if (events.size() > 1)
      {
         throw WSRP2ExceptionFactory.createWSException(OperationNotSupported.class,
            "GateIn currently doesn't support sending multiple events to process at once.", null);
      }
      prepareInvocation();
   }

   @Override
   RegistrationContext getRegistrationContext()
   {
      return handleEvents.getRegistrationContext();
   }

   @Override
   RuntimeContext getRuntimeContext()
   {
      return handleEvents.getRuntimeContext();
   }

   @Override
   MimeRequest getParams()
   {
      return handleEvents.getMarkupParams();
   }

   @Override
   public PortletContext getPortletContext()
   {
      return handleEvents.getPortletContext();
   }

   @Override
   UserContext getUserContext()
   {
      return handleEvents.getUserContext();
   }

   @Override
   String getContextName()
   {
      return MarkupHandler.HANDLE_EVENTS;
   }

   @Override
   AccessMode getAccessMode() throws MissingParameters
   {
      StateChange stateChange = handleEvents.getEventParams().getPortletStateChange();

      return WSRPUtils.getAccessModeFromStateChange(stateChange);
   }

   @Override
   PortletInvocation initInvocation(WSRPPortletInvocationContext context)
   {
      EventInvocation eventInvocation = new EventInvocation(context);

      List<Event> events = handleEvents.getEventParams().getEvents();

      if (events.size() > 1)
      {
         throw new NotYetImplemented("Need to support multiple events at once...");
      }

      // since we currently don't support sending multiple events to process at once, assume there's only one
      Event event = events.get(0);

      eventInvocation.setName(event.getName());
      eventInvocation.setPayload(PayloadUtils.getPayloadAsSerializable(event));

      return eventInvocation;
   }

   @Override
   public HandleEventsResponse processResponse(PortletInvocationResponse response)
   {
      if (response instanceof UpdateNavigationalStateResponse)
      {
         UpdateNavigationalStateResponse unsResponse = (UpdateNavigationalStateResponse)response;
         HandleEventsResponse eventsResponse = WSRPTypeFactory.createHandleEventsReponse();

         UpdateResponse updateResponse = createUpdateResponse(unsResponse);
         eventsResponse.setUpdateResponse(updateResponse);

         return eventsResponse;
      }
      else
      {
         throw new IllegalArgumentException("Cannot process response: " + response);
      }
   }
}
