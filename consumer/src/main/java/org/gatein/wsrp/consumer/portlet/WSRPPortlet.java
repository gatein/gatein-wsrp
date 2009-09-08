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

package org.gatein.wsrp.consumer.portlet;

import org.gatein.common.util.ParameterValidation;
import org.gatein.pc.api.Portlet;
import org.gatein.pc.api.PortletContext;
import org.gatein.pc.api.info.PortletInfo;
import org.gatein.wsrp.consumer.portlet.info.WSRPPortletInfo;

/**
 * Simple implementation of org.gatein.pc.Portlet interface
 *
 * @author <a href="mailto:boleslaw.dawidowicz@jboss.org">Boleslaw Dawidowicz</a>
 * @version $Revision: 8784 $
 */
public class WSRPPortlet implements Portlet
{

   private PortletContext portletContext;
   private PortletInfo info;

   public WSRPPortlet()
   {
   }

   /**
    *
    */
   public WSRPPortlet(PortletContext context, PortletInfo info)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(context, "PortletContext");
      ParameterValidation.throwIllegalArgExceptionIfNull(info, "PortletInfo");
      this.portletContext = context;
      this.info = info;
   }

   public static WSRPPortlet createClone(PortletContext newContext, WSRPPortletInfo originalInfo)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(newContext, "PortletContext");
      return new WSRPPortlet(newContext, new WSRPPortletInfo(originalInfo, newContext.getId()));
   }

   /** Portlet interface implemented methods */

   public PortletContext getContext()
   {
      return portletContext;
   }

   public PortletInfo getInfo()
   {
      if (info == null)
      {
         throw new IllegalStateException("No PortletInfo was set for WSRPPortlet '" + portletContext.getId() + "'");
      }
      return info;
   }

   public boolean isRemote()
   {
      return true;
   }

   public void setInfo(PortletInfo info)
   {
      this.info = info;
   }


   public WSRPPortletInfo getWSRPInfo()
   {
      return (WSRPPortletInfo)getInfo();
   }

   public void setPortletContext(PortletContext context)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(context, "PortletContext");
      this.portletContext = context;
   }


   public String toString()
   {
      return "WSRPPortlet[context=" + portletContext + "]";
   }
}
