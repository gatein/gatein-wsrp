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

import org.gatein.common.net.URLTools;
import org.gatein.common.util.ParameterValidation;
import org.gatein.pc.api.Mode;
import org.gatein.pc.api.RenderURL;
import org.gatein.pc.api.StateString;
import org.gatein.pc.api.WindowState;
import org.gatein.wsrp.spec.v2.WSRP2RewritingConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 11404 $
 */
public class WSRPRenderURL extends WSRPPortletURL implements RenderURL
{
   private Map<String, String[]> publicNSChanges;

   protected WSRPRenderURL(Mode mode, WindowState windowState, boolean secure, StateString navigationalState, Map<String, String[]> publicNavigationalStateChanges)
   {
      super(mode, windowState, secure, navigationalState);

      this.publicNSChanges = publicNavigationalStateChanges;
   }

   protected WSRPRenderURL()
   {
   }

   @Override
   protected void dealWithSpecificParams(Map<String, String> params, String originalURL)
   {
      super.dealWithSpecificParams(params, originalURL);

      String paramValue = getRawParameterValueFor(params, WSRP2RewritingConstants.NAVIGATIONAL_VALUES);
      if (paramValue != null)
      {
         publicNSChanges = decodePublicNS(paramValue);
         params.remove(WSRP2RewritingConstants.NAVIGATIONAL_VALUES);
      }
   }

   protected String getURLType()
   {
      return WSRPRewritingConstants.URL_TYPE_RENDER;
   }

   public Map<String, String[]> getPublicNavigationalStateChanges()
   {
      return publicNSChanges;
   }

   protected void appendEnd(StringBuffer sb)
   {
      if (publicNSChanges != null)
      {
         createURLParameter(sb, WSRP2RewritingConstants.NAVIGATIONAL_VALUES, encodePublicNS(publicNSChanges));
      }
   }

   /**
    * Encodes the public NS according to the rules found at <a href='http://docs.oasis-open.org/wsrp/v2/wsrp-2.0-spec-os-01.html#_wsrp-navigationalValues'>
    * http://docs.oasis-open.org/wsrp/v2/wsrp-2.0-spec-os-01.html#_wsrp-navigationalValues</a>
    *
    * @param publicNSChanges
    * @return
    */
   protected static String encodePublicNS(Map<String, String[]> publicNSChanges)
   {
      if (ParameterValidation.existsAndIsNotEmpty(publicNSChanges))
      {
         StringBuilder sb = new StringBuilder(128);

         Set<Map.Entry<String, String[]>> entries = publicNSChanges.entrySet();
         int entryNb = entries.size();
         int currentEntry = 0;
         for (Map.Entry<String, String[]> entry : entries)
         {
            String name = entry.getKey();
            String[] values = entry.getValue();

            if (ParameterValidation.existsAndIsNotEmpty(values))
            {
               int valueNb = values.length;
               int currentValueIndex = 0;
               for (String value : values)
               {
                  sb.append(name).append("=").append(value);
                  if (currentValueIndex++ != valueNb - 1)
                  {
                     sb.append("&");
                  }
               }
            }
            else
            {
               sb.append(name);
            }

            if (currentEntry++ != entryNb - 1)
            {
               sb.append("&");
            }
         }

         return URLTools.encodeXWWWFormURL(sb.toString());
      }
      else
      {
         return null;
      }
   }

   protected static Map<String, String[]> decodePublicNS(String paramValue)
   {
      if (!ParameterValidation.isNullOrEmpty(paramValue))
      {
         String encodedURL = URLTools.decodeXWWWFormURL(paramValue);
         Map<String, String[]> publicNS = new HashMap<String, String[]>(7);

         boolean finished = false;
         while (encodedURL.length() > 0 && !finished)
         {
            int endParamIndex = encodedURL.indexOf(AMPERSAND);
            String param;
            if (endParamIndex < 0)
            {
               // no param left: try the remainder of the String
               param = encodedURL;
               finished = true;
            }
            else
            {
               param = encodedURL.substring(0, endParamIndex);
            }

            int equalsIndex = param.indexOf(EQUALS);
            if (equalsIndex < 0)
            {
               publicNS.put(param, null);
            }
            else
            {
               // extract param name
               String name = param.substring(0, equalsIndex);
               // extract param value
               String value = param.substring(equalsIndex + EQUALS.length(), param.length());

               WSRPUtils.addMultiValuedValueTo(publicNS, name, value);
            }
            encodedURL = encodedURL.substring(endParamIndex + AMPERSAND.length());
         }

         return publicNS;
      }
      else
      {
         return null;
      }
   }
}
