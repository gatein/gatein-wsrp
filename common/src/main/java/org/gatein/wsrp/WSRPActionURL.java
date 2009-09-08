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

package org.gatein.wsrp;

import org.gatein.pc.api.Mode;
import org.gatein.pc.api.WindowState;
import org.gatein.pc.api.ActionURL;
import org.gatein.pc.api.OpaqueStateString;
import org.gatein.pc.api.StateString;

import java.util.Map;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 12803 $
 */
public class WSRPActionURL extends WSRPPortletURL implements ActionURL
{

   private StateString navigationalState;
   private StateString interactionState;

   protected WSRPActionURL(Mode mode, WindowState windowState, boolean secure, StateString navigationalState, StateString interactionState)
   {
      super(mode, windowState, secure);
      this.navigationalState = navigationalState;
      this.interactionState = interactionState;
   }

   protected WSRPActionURL()
   {
   }

   @Override
   protected void dealWithSpecificParams(Map<String, String> params, String originalURL)
   {
      super.dealWithSpecificParams(params, originalURL);

      String paramValue = getRawParameterValueFor(params, WSRPRewritingConstants.INTERACTION_STATE);
      if (paramValue != null)
      {
         interactionState = new OpaqueStateString(paramValue);
         params.remove(WSRPRewritingConstants.INTERACTION_STATE);
      }

      paramValue = getRawParameterValueFor(params, WSRPRewritingConstants.NAVIGATIONAL_STATE);
      if (paramValue != null)
      {
         navigationalState = new OpaqueStateString(paramValue);
         params.remove(WSRPRewritingConstants.NAVIGATIONAL_STATE);
      }
   }

   protected String getURLType()
   {
      return WSRPRewritingConstants.URL_TYPE_BLOCKING_ACTION;
   }

   public StateString getNavigationalState()
   {
      return navigationalState;
   }

   public StateString getInteractionState()
   {
      return interactionState;
   }

   protected void appendEnd(StringBuffer sb)
   {
      if (interactionState != null)
      {
         createURLParameter(sb, WSRPRewritingConstants.INTERACTION_STATE, interactionState.getStringValue());
      }

      if (navigationalState != null)
      {
         createURLParameter(sb, WSRPRewritingConstants.NAVIGATIONAL_STATE, navigationalState.getStringValue());
      }
   }
}
