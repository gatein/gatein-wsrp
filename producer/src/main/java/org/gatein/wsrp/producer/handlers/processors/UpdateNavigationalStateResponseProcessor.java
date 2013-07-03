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

import org.gatein.common.util.ParameterValidation;
import org.gatein.pc.api.invocation.response.UpdateNavigationalStateResponse;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.WSRPUtils;
import org.oasis.wsrp.v2.InvalidHandle;
import org.oasis.wsrp.v2.InvalidRegistration;
import org.oasis.wsrp.v2.MissingParameters;
import org.oasis.wsrp.v2.ModifyRegistrationRequired;
import org.oasis.wsrp.v2.NavigationalContext;
import org.oasis.wsrp.v2.OperationFailed;
import org.oasis.wsrp.v2.OperationNotSupported;
import org.oasis.wsrp.v2.PortletContext;
import org.oasis.wsrp.v2.UnsupportedLocale;
import org.oasis.wsrp.v2.UnsupportedMimeType;
import org.oasis.wsrp.v2.UnsupportedMode;
import org.oasis.wsrp.v2.UnsupportedWindowState;
import org.oasis.wsrp.v2.UpdateResponse;

import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
abstract class UpdateNavigationalStateResponseProcessor<Request, Response> extends RequestProcessor<Request, Response>
{
   public UpdateNavigationalStateResponseProcessor(ProducerHelper producer, Request request) throws InvalidRegistration, InvalidHandle, UnsupportedLocale, UnsupportedMimeType, UnsupportedWindowState, OperationFailed, MissingParameters, UnsupportedMode, ModifyRegistrationRequired, OperationNotSupported
   {
      super(producer, request);
   }

   protected String getNewStateOrNull(UpdateNavigationalStateResponse renderResult, boolean forMode)
   {
      Object state = forMode ? renderResult.getMode() : renderResult.getWindowState();
      return state != null ? state.toString() : null;
   }

   protected UpdateResponse createUpdateResponse(UpdateNavigationalStateResponse stateResponse)
   {
      UpdateResponse updateResponse = WSRPTypeFactory.createUpdateResponse();
      updateResponse.setNewMode(WSRPUtils.convertJSR168PortletModeNameToWSRPName(getNewStateOrNull(stateResponse, true)));
      updateResponse.setNewWindowState(WSRPUtils.convertJSR168WindowStateNameToWSRPName(getNewStateOrNull(stateResponse, false)));
      NavigationalContext navigationalContext = WSRPTypeFactory.createNavigationalContextOrNull(
         stateResponse.getNavigationalState(),
         stateResponse.getPublicNavigationalStateUpdates()
      );
      updateResponse.setNavigationalContext(navigationalContext);

      // events
      List<UpdateNavigationalStateResponse.Event> events = stateResponse.getEvents();
      if (ParameterValidation.existsAndIsNotEmpty(events))
      {
         for (UpdateNavigationalStateResponse.Event event : events)
         {
            updateResponse.getEvents().add(WSRPTypeFactory.createEvent(event.getName(), event.getPayload()));
         }
      }

      // deal with implicit cloning and state modification
      if (instanceContext.wasModified())
      {
         PortletContext updatedPortletContext = WSRPUtils.convertToWSRPPortletContext(instanceContext.getPortletContext());
         updateResponse.setPortletContext(updatedPortletContext);
      }
      return updateResponse;
   }
}
