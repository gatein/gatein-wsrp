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

package org.gatein.wsrp.consumer.handlers;

import org.gatein.common.util.ParameterValidation;
import org.gatein.pc.api.OpaqueStateString;
import org.gatein.pc.api.StateEvent;
import org.gatein.pc.api.invocation.PortletInvocation;
import org.gatein.pc.api.invocation.response.UpdateNavigationalStateResponse;
import org.gatein.pc.api.spi.InstanceContext;
import org.gatein.wsrp.WSRPUtils;
import org.gatein.wsrp.consumer.WSRPConsumerImpl;
import org.gatein.wsrp.payload.PayloadUtils;
import org.oasis.wsrp.v2.Event;
import org.oasis.wsrp.v2.EventPayload;
import org.oasis.wsrp.v2.NamedString;
import org.oasis.wsrp.v2.NavigationalContext;
import org.oasis.wsrp.v2.PortletContext;
import org.oasis.wsrp.v2.UpdateResponse;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public abstract class NavigationalStateUpdatingHandler<Invocation extends PortletInvocation, Request, Response> extends InvocationHandler<Invocation, Request, Response>
{
   public NavigationalStateUpdatingHandler(WSRPConsumerImpl consumer)
   {
      super(consumer);
   }

   protected UpdateNavigationalStateResponse processUpdateResponse(Invocation invocation, RequestPrecursor<Invocation> requestPrecursor, UpdateResponse updateResponse)
   {
      UpdateNavigationalStateResponse result = new UpdateNavigationalStateResponse();

      // new mode
      String newMode = updateResponse.getNewMode();
      if (newMode != null)
      {
         result.setMode(WSRPUtils.getJSR168PortletModeFromWSRPName(newMode));
      }

      // new window state
      String newWindowState = updateResponse.getNewWindowState();
      if (newWindowState != null)
      {
         result.setWindowState(WSRPUtils.getJSR168WindowStateFromWSRPName(newWindowState));
      }

      // navigational state
      NavigationalContext navigationalContext = updateResponse.getNavigationalContext();
      if (navigationalContext != null)
      {
         String navigationalState = navigationalContext.getOpaqueValue();
         if (navigationalState != null) // todo: check meaning of empty private NS
         {
            result.setNavigationalState(new OpaqueStateString(navigationalState));
         }

         List<NamedString> publicParams = navigationalContext.getPublicValues();
         if (ParameterValidation.existsAndIsNotEmpty(publicParams))
         {
            Map<String, String[]> publicNS = WSRPUtils.createPublicNSFrom(publicParams);
            result.setPublicNavigationalStateUpdates(publicNS);
         }
      }

      // events
      List<Event> events = updateResponse.getEvents();
      if (ParameterValidation.existsAndIsNotEmpty(events))
      {
         for (Event event : events)
         {
            EventPayload payload = event.getPayload();
            result.queueEvent(new UpdateNavigationalStateResponse.Event(event.getName(), PayloadUtils.getPayloadAsSerializable(event.getType(), payload)));
         }
      }

      // check if the portlet was cloned
      PortletContext portletContext = updateResponse.getPortletContext();
      SessionHandler sessionHandler = consumer.getSessionHandler();
      if (portletContext != null)
      {
         PortletContext originalContext = requestPrecursor.getPortletContext();
         InstanceContext context = invocation.getInstanceContext();

         String handle = portletContext.getPortletHandle();
         if (!originalContext.getPortletHandle().equals(handle))
         {
            // todo: GTNWSRP-36 If the Producer returns a new portletHandle without returning a new sessionID, the Consumer MUST
            // associate the current sessionID with the new portletHandle rather than the previous portletHandle.
            if (debug)
            {
               log.debug("Portlet '" + requestPrecursor.getPortletHandle() + "' was implicitely cloned. New handle is '"
                  + handle + "'");
            }

            StateEvent event = new StateEvent(WSRPUtils.convertToPortalPortletContext(portletContext), StateEvent.Type.PORTLET_CLONED_EVENT);
            context.onStateEvent(event);
         }
         else
         {
            // check if the state was modified
            byte[] originalState = originalContext.getPortletState();
            byte[] newState = portletContext.getPortletState();
            if (!Arrays.equals(originalState, newState))
            {
               StateEvent event = new StateEvent(WSRPUtils.convertToPortalPortletContext(portletContext), StateEvent.Type.PORTLET_MODIFIED_EVENT);
               context.onStateEvent(event);
            }
         }

         // update the session information associated with the portlet handle
         sessionHandler.updateSessionInfoFor(originalContext.getPortletHandle(), handle, invocation);
      }
      else
      {
         portletContext = requestPrecursor.getPortletContext();
      }

      // update the session info, using either the original or cloned portlet context, as appropriate
      sessionHandler.updateSessionIfNeeded(updateResponse.getSessionContext(), invocation, portletContext.getPortletHandle());
      return result;
   }
}
