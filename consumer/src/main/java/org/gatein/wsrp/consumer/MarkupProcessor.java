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

package org.gatein.wsrp.consumer;

import org.gatein.common.text.TextTools;
import org.gatein.pc.api.URLFormat;
import org.gatein.pc.api.spi.PortletInvocationContext;
import org.gatein.wsrp.WSRPPortletURL;
import org.gatein.wsrp.WSRPResourceURL;
import org.gatein.wsrp.WSRPRewritingConstants;

import java.util.Set;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class MarkupProcessor implements TextTools.StringReplacementGenerator
{
   private final PortletInvocationContext context;
   private final URLFormat format;
   private final Set<String> supportedCustomModes;
   private final Set<String> supportedCustomWindowStates;
   private final String namespace;

   protected MarkupProcessor(String namespace, PortletInvocationContext context, org.gatein.pc.api.PortletContext target, URLFormat format, ProducerInfo info)
   {
      this.namespace = namespace;
      this.context = context;
      this.format = format;
      supportedCustomModes = info.getSupportedCustomModes();
      supportedCustomWindowStates = info.getSupportedCustomWindowStates();
   }

   public String getReplacementFor(String match, String prefix, String suffix)
   {
      if (prefix.equals(match))
      {
         return namespace;
      }
      else if (match.startsWith(WSRPRewritingConstants.BEGIN_WSRP_REWRITE_END))
      {
         // remove end of rewrite token
         match = match.substring(WSRPRewritingConstants.BEGIN_WSRP_REWRITE_END.length());

         WSRPPortletURL portletURL = WSRPPortletURL.create(match, supportedCustomModes, supportedCustomWindowStates, true);
         return context.renderURL(portletURL, format);
      }
      else
      {
         // match is not something we know how to process
         return match;
      }
   }


   static String getResourceURL(String urlAsString, WSRPResourceURL resource)
   {
      String resourceURL = resource.getResourceURL().toExternalForm();
      if (InvocationHandler.log.isDebugEnabled())
      {
         InvocationHandler.log.debug("URL '" + urlAsString + "' refers to a resource which are not currently well supported. " +
            "Attempting to craft a URL that we might be able to work with: '" + resourceURL + "'");
      }

      // right now the resourceURL should be output as is, because it will be used directly but it really should be encoded
      return resourceURL;
   }
}
