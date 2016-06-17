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

import org.gatein.pc.api.ActionURL;
import org.gatein.pc.api.Mode;
import org.gatein.pc.api.OpaqueStateString;
import org.gatein.pc.api.StateString;
import org.gatein.pc.api.WindowState;

import java.util.Map;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 12803 $
 */
public class WSRPActionURL extends WSRPPortletURL implements ActionURL
{
   private StateString interactionState;

   protected WSRPActionURL(Mode mode, WindowState windowState, boolean secure, StateString navigationalState, StateString interactionState, URLContext context)
   {
      super(mode, windowState, secure, navigationalState, context);
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
         // We had some problems with double-encoding "somewhere" along the way. It's *never* OK to have a
         // interaction state of JBPNS with a slash at the end, so, we'll just remove it if we find this case.
         // ideally, we would fix it wherever this broke, but this might be things out of our control, like JavaScript
         // UI stuff, or other consumers sending us back something we sent previously, or ...
         // It is possible that we only receive "JBPNS" here, so, the first check would always be true, but I'm not
         // 100% sure that's the case. So, better safe than sorry.
         if (paramValue.startsWith("JBPNS") && paramValue.endsWith("\\"))
         {
            paramValue = paramValue.substring(0, paramValue.length()-1);
         }
         interactionState = new OpaqueStateString(paramValue);
         params.remove(WSRPRewritingConstants.INTERACTION_STATE);
      }
   }

   protected String getURLType()
   {
      return WSRPRewritingConstants.URL_TYPE_BLOCKING_ACTION;
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
   }
}
