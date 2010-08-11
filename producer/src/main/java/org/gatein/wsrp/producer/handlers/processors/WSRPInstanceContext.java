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
import org.gatein.pc.api.PortletContext;
import org.gatein.pc.api.PortletStateType;
import org.gatein.pc.api.StateEvent;
import org.gatein.pc.api.StatefulPortletContext;
import org.gatein.pc.api.spi.InstanceContext;
import org.gatein.pc.api.state.AccessMode;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 8784 $
 * @since 2.6
 */
class WSRPInstanceContext implements InstanceContext
{
   private PortletContext context;
   private String instanceId;
   private final AccessMode accessMode;
   private boolean wasModified = false;

   public WSRPInstanceContext(PortletContext portletContext, AccessMode accessMode, String instanceId)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(portletContext, "portlet context");
      ParameterValidation.throwIllegalArgExceptionIfNull(accessMode, "AccessMode");

      this.context = portletContext;
      this.accessMode = accessMode;

      if (instanceId != null && instanceId.length() > 0)
      {
         this.instanceId = instanceId;
      }
      else
      {
         this.instanceId = portletContext.getId();
      }
   }

   public String getId()
   {
      return instanceId;
   }

   public AccessMode getAccessMode()
   {
      return accessMode;
   }

   public void onStateEvent(StateEvent event)
   {
      PortletContext portletContext = event.getPortletContext();
      ParameterValidation.throwIllegalArgExceptionIfNull(portletContext, "PortletContext");
      wasModified = true;
      context = portletContext;
   }

   public boolean wasModified()
   {
      return wasModified;
   }

   PortletContext getPortletContext()
   {
      return context;
   }

   public PortletStateType<?> getStateType()
   {
      if (context instanceof StatefulPortletContext)
      {
         StatefulPortletContext spc = (StatefulPortletContext)context;
         return spc.getType();
      }
      else
      {
         return PortletStateType.OPAQUE;
      }
   }
}
