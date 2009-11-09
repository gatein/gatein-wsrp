/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2009, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/

package org.gatein.wsrp;

import org.gatein.common.net.URLTools;
import org.gatein.common.net.media.MediaType;
import org.gatein.pc.api.Mode;
import org.gatein.pc.api.WindowState;
import org.jboss.logging.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 13470 $
 * @since 2.4
 */
public class WSRPResourceURL extends WSRPPortletURL
{
   private final static Logger log = Logger.getLogger(WSRPResourceURL.class);

   private URL resourceURL;
   private boolean requiresRewrite = false;
   private static final Map<String, MediaType> SUPPORTED_RESOURCE_TYPES = new HashMap<String, MediaType>(4);

   static
   {
      SUPPORTED_RESOURCE_TYPES.put("css", MediaType.TEXT_CSS);
      SUPPORTED_RESOURCE_TYPES.put("js", MediaType.TEXT_JAVASCRIPT);
      SUPPORTED_RESOURCE_TYPES.put("png", MediaType.create("image/png"));
      MediaType jpeg = MediaType.create("image/jpeg");
      SUPPORTED_RESOURCE_TYPES.put("jpg", jpeg);
      SUPPORTED_RESOURCE_TYPES.put("jpeg", jpeg);
      SUPPORTED_RESOURCE_TYPES.put("gif", MediaType.create("image/gif"));
      SUPPORTED_RESOURCE_TYPES.put("pdf", MediaType.create("application/pdf"));
      SUPPORTED_RESOURCE_TYPES.put("txt", MediaType.create("text/plain"));
   }


   public WSRPResourceURL(Mode mode, WindowState windowState, boolean secure, URL resourceURL, boolean requiresRewrite)
   {
      super(mode, windowState, secure);
      this.resourceURL = resourceURL;
      this.requiresRewrite = requiresRewrite;
   }

   public WSRPResourceURL()
   {
      super();
   }

   protected String getURLType()
   {
      return WSRPRewritingConstants.URL_TYPE_RESOURCE;
   }

   protected void appendEnd(StringBuffer sb)
   {
      if (resourceURL != null)
      {
         createURLParameter(sb, WSRPRewritingConstants.RESOURCE_URL, resourceURL.toExternalForm());
      }

      if (requiresRewrite)
      {
         createURLParameter(sb, WSRPRewritingConstants.RESOURCE_REQUIRES_REWRITE, "true");
      }
   }

   @Override
   protected void dealWithSpecificParams(Map<String, String> params, String originalURL)
   {
      super.dealWithSpecificParams(params, originalURL);

      String paramValue = getRawParameterValueFor(params, WSRPRewritingConstants.RESOURCE_REQUIRES_REWRITE);
      if (paramValue != null)
      {
         requiresRewrite = Boolean.valueOf(paramValue);
         params.remove(WSRPRewritingConstants.RESOURCE_REQUIRES_REWRITE);
      }
      else
      {
         throw new IllegalArgumentException("The parsed parameters don't contain a value for the required "
            + WSRPRewritingConstants.RESOURCE_REQUIRES_REWRITE + " parameter in " + originalURL);
      }

      paramValue = getRawParameterValueFor(params, WSRPRewritingConstants.RESOURCE_URL);
      if (paramValue != null)
      {
         try
         {
            paramValue = URLTools.decodeXWWWFormURL(paramValue);

            if (requiresRewrite)
            {
               // todo: do something...
               log.debug("Required re-writing but this is not yet implemented...");
            }

            resourceURL = new URL(paramValue);
            String file = resourceURL.getFile();

            MediaType mediaType = SUPPORTED_RESOURCE_TYPES.get(file);
            if (mediaType == null)
            {
               log.debug("Couldn't determine (based on extension) MIME type of file: " + file
                  + "\nRetrieving the associated resource will probably fail.");
            }

            params.remove(WSRPRewritingConstants.RESOURCE_URL);
         }
         catch (MalformedURLException e)
         {
            throw new IllegalArgumentException("Malformed URL: " + paramValue, e);
         }
      }
      else
      {
         throw new IllegalArgumentException("The parsed parameters don't contain a value for the required "
            + WSRPRewritingConstants.RESOURCE_URL + " parameter in " + originalURL);
      }
   }

   public String toString()
   {
      StringBuffer result = new StringBuffer(resourceURL.toExternalForm());

      appendExtraParams(result);

      // append extra characters if we have some
      if (extra != null)
      {
         result.append(extra);
      }

      return result.toString();
   }
}
