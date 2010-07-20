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
import org.gatein.common.util.ParameterValidation;
import org.gatein.pc.api.Mode;
import org.gatein.pc.api.PortletContext;
import org.gatein.pc.api.ResourceURL;
import org.gatein.pc.api.StateString;
import org.gatein.pc.api.WindowState;
import org.gatein.pc.api.cache.CacheLevel;
import org.gatein.wsrp.spec.v2.WSRP2RewritingConstants;
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
   private boolean preferOperation = false;

   private static final Map<String, MediaType> SUPPORTED_RESOURCE_TYPES = new HashMap<String, MediaType>(11);

   static
   {
      SUPPORTED_RESOURCE_TYPES.put("html", MediaType.TEXT_HTML);
      SUPPORTED_RESOURCE_TYPES.put("htm", MediaType.TEXT_HTML);
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
      
      if (resourceId != null)
      {
         createURLParameter(sb, WSRP2RewritingConstants.RESOURCE_ID, resourceId);
      }
      
      // false is the default value, so we don't actually need to add it to the string
      if (preferOperation != false)
      {
          createURLParameter(sb, WSRP2RewritingConstants.RESOURCE_PREFER_OPERATION, Boolean.toString(preferOperation));
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
            this.resourceURL = new URL(paramValue);
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
      
      String resourceIDParam = getRawParameterValueFor(params, WSRP2RewritingConstants.RESOURCE_ID);
      if (resourceIDParam != null)
      {
         resourceId = resourceIDParam;
      }
      
      String preferOperationParam = getRawParameterValueFor(params, WSRP2RewritingConstants.RESOURCE_PREFER_OPERATION);
      {
         if (preferOperationParam != null)
         {
            preferOperation = Boolean.valueOf(preferOperationParam);
         }
      }
      
      if (resourceIDParam == null && paramValue == null)
      {
         throw new IllegalArgumentException("The parsed parameters don't contain a value for  "
               + WSRPRewritingConstants.RESOURCE_URL + " or for " + WSRP2RewritingConstants.RESOURCE_ID + " parameter in " + originalURL);
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
      // we need to return a representation of the wsrp resource identification, this is not necessarily just
      // the wsrp-resourceID, we need to also consider the wsrp-url and other wsrp resource values.
      // This value returned by this method is used by the PC ResourceInvocation
      return encodeResource(resourceId, resourceURL, preferOperation);
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

   public static String createAbsoluteURLFrom(String initial, String serverAddress, String portletApplicationName)
   {
      String url = serverAddress;

      if (portletApplicationName != null)
      {
         url += URLTools.SLASH + portletApplicationName;
      }

      if (!ParameterValidation.isNullOrEmpty(initial))
      {
         if (initial.startsWith(URLTools.SLASH))
         {
            url += initial;
         }
         else
         {
            url += URLTools.SLASH + initial;
         }
      }

      return url;

   }

   /**
    * @return
    * @deprecated
    */
   public boolean requiresRewrite()
   {
      return requiresRewrite;
   }
   
   //TODO: figure out a more clean way to encode and decode the pc resource id (note: different from the wsrp resource id)
   //we should either use a Map<String, String> directly or pass an object back
   
   /**
    * Encodes the wsrp resource information into a single string.
    * 
    * @param resourceId The original resource ID
    * @param resourceURL The originial resource url
    * @param preferedOperation The preferedOperation value
    * @return
    */
   public static String encodeResource(String resourceId, URL resourceURL, boolean preferedOperation)
   {
      Map<String, String[]> parameters = new HashMap<String, String[]>();
      
      if (resourceId != null)
      {
         parameters.put(WSRP2RewritingConstants.RESOURCE_ID, new String[]{resourceId});
      }
      
      if (resourceURL != null)
      {
         parameters.put(WSRPRewritingConstants.RESOURCE_URL, new String[]{resourceURL.toString()});
      }
      
      parameters.put(WSRP2RewritingConstants.RESOURCE_PREFER_OPERATION, new String[]{Boolean.toString(preferedOperation)});
      
      return StateString.encodeAsOpaqueValue(parameters);
   }
   
   /**
    * Decodes the resource information specified by the encodeResource back into proper resource values
    * 
    * @param resourceInfo
    */
   public static Map<String, String> decodeResource(String resourceInfo)
   {
      Map<String, String[]> resourceParameters =  StateString.decodeOpaqueValue(resourceInfo);
      
      Map<String, String> resource = new HashMap<String, String>();
      
      for (Map.Entry<String, String[]> entry : resourceParameters.entrySet())
      {
         if (entry.getValue() != null && entry.getValue().length > 0)
         resource.put(entry.getKey(), entry.getValue()[0]);
      }
      
      return resource;
   }
}
