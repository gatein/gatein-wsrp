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

package org.gatein.wsrp;

import org.gatein.pc.api.Mode;
import org.gatein.pc.api.OpaqueStateString;
import org.gatein.pc.api.RenderURL;
import org.gatein.pc.api.StateString;
import org.gatein.pc.api.WindowState;

import java.util.Map;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 11404 $
 */
public class WSRPRenderURL extends WSRPPortletURL implements RenderURL
{

   private StateString navigationalState;

   protected WSRPRenderURL(Mode mode, WindowState windowState, boolean secure, StateString navigationalState)
   {
      super(mode, windowState, secure);
      this.navigationalState = navigationalState;
   }

   protected WSRPRenderURL()
   {
   }

   @Override
   protected void dealWithSpecificParams(Map<String, String> params, String originalURL)
   {
      super.dealWithSpecificParams(params, originalURL);

      String paramValue = getRawParameterValueFor(params, WSRPRewritingConstants.NAVIGATIONAL_STATE);
      if (paramValue != null)
      {
         navigationalState = new OpaqueStateString(paramValue);
      }
   }

   protected String getURLType()
   {
      return WSRPRewritingConstants.URL_TYPE_RENDER;
   }

   public StateString getNavigationalState()
   {
      return navigationalState;
   }

   public Map<String, String[]> getPublicNavigationalStateChanges()
   {
      return null; // todo: fix me
   }

   protected void appendEnd(StringBuffer sb)
   {
      if (navigationalState != null)
      {
         createURLParameter(sb, WSRPRewritingConstants.NAVIGATIONAL_STATE, navigationalState.getStringValue());
      }
   }
}
