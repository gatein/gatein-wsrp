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
import org.gatein.common.net.media.MediaType;
import org.gatein.pc.api.Mode;
import org.gatein.pc.api.PortletContext;
import org.gatein.pc.api.ResourceURL;
import org.gatein.pc.api.StateString;
import org.gatein.pc.api.WindowState;
import org.gatein.pc.api.cache.CacheLevel;
import org.jboss.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 13470 $
 * @since 2.4
 */
public class WSRPResourceURL extends WSRPPortletURL implements ResourceURL
{
   private final static Logger log = Logger.getLogger(WSRPResourceURL.class);

   private String resourceId;
   private StateString resourceState;
   private CacheLevel cacheability;

   private boolean requiresRewrite = false;
   private URL resourceURL;

   private static final Map<String, MediaType> SUPPORTED_RESOURCE_TYPES = new HashMap<String, MediaType>(8);

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

   public WSRPResourceURL()
   {
   }

   public WSRPResourceURL(Mode mode, WindowState windowState, boolean secure, StateString navigationalState, StateString resourceState, String resourceId, CacheLevel cacheability)
   {
      super(mode, windowState, secure, navigationalState);

      this.resourceId = resourceId;
      this.resourceState = resourceState;
      this.cacheability = cacheability;
   }

   protected String getURLType()
   {
      return WSRPRewritingConstants.URL_TYPE_RESOURCE;
   }

   protected void appendEnd(StringBuffer sb)
   {
      if (resourceURL != null)
      {
         createURLParameter(sb, WSRPRewritingConstants.RESOURCE_URL, URLTools.encodeXWWWFormURL(resourceURL.toExternalForm()));
      }

      createURLParameter(sb, WSRPRewritingConstants.RESOURCE_REQUIRES_REWRITE, requiresRewrite ? "true" : "false");
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
            // todo: deal with resourceId properly, right now just use resourceURL if any
            resourceId = paramValue; // keep the encoded value as it will be used in URLs

            paramValue = URLTools.decodeXWWWFormURL(paramValue);

            resourceURL = new URL(paramValue);

            String extension = URLTools.getFileExtensionOrNullFrom(resourceURL);

            MediaType mediaType = SUPPORTED_RESOURCE_TYPES.get(extension);
            if (mediaType == null)
            {
               log.debug("Couldn't determine (based on extension) MIME type of file: " + resourceURL.getPath()
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

   /**
    * @return
    * @deprecated
    */
   public URL getResourceURL()
   {
      return resourceURL;
   }

   /**
    * @param resourceURL
    * @deprecated
    */
   public void setResourceURL(URL resourceURL)
   {
      this.resourceURL = resourceURL;
   }

   public String getResourceId()
   {
      return resourceId;
   }

   /**
    * @param resourceId
    * @deprecated
    */
   public void setResourceId(String resourceId)
   {
      this.resourceId = resourceId;
   }

   public StateString getResourceState()
   {
      return resourceState;
   }

   public CacheLevel getCacheability()
   {
      return cacheability;
   }

   /**
    * This method is a hack to provide a minimal resource support before WSRP 2 so that bridged portlets work. We
    * basically build a resource URL based on the server address and the targeted portlet context (which hopefully can
    * be mapped to the context path of the war file it's deployed in). JBoss Portlet Bridge 2.0 uses a resource ID that
    * is the absolute path to resource inside web application context for static resources.
    *
    * @param request
    * @param portletContext
    */
   public void buildURLWith(HttpServletRequest request, PortletContext portletContext)
   {
      String url = URLTools.getServerAddressFrom(request) + URLTools.SLASH + portletContext.getApplicationName();

      if (resourceId != null)
      {
         url += resourceId;
      }

      try
      {
         resourceURL = new URL(url);
         String extension = URLTools.getFileExtensionOrNullFrom(resourceURL);
         MediaType type = SUPPORTED_RESOURCE_TYPES.get(extension);
         if (MediaType.TEXT_CSS.equals(type) || MediaType.TEXT_JAVASCRIPT.equals(type))
         {
            requiresRewrite = true;
         }
      }
      catch (MalformedURLException e)
      {
         throw new IllegalArgumentException("Malformed URL: " + url, e);
      }

      log.info("Attempted to build resource URL that could be accessed directly from consumer: " + resourceURL);
   }

   /**
    * @return
    * @deprecated
    */
   public boolean requiresRewrite()
   {
      return requiresRewrite;
   }
}
