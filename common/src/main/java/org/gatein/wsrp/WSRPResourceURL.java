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
import org.gatein.pc.api.Mode;
import org.gatein.pc.api.OpaqueStateString;
import org.gatein.pc.api.ResourceURL;
import org.gatein.pc.api.StateString;
import org.gatein.pc.api.WindowState;
import org.gatein.pc.api.cache.CacheLevel;
import org.gatein.wsrp.spec.v2.WSRP2RewritingConstants;

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
   public final static String DEFAULT_RESOURCE_ID = "_gtn_resid_";

   private String resourceId;
   private StateString resourceState;
   private CacheLevel cacheability;

   private boolean requiresRewrite = false;
   private URL resourceURL;
   private boolean preferOperation = false;


   public WSRPResourceURL()
   {
   }

   public WSRPResourceURL(Mode mode, WindowState windowState, boolean secure, StateString navigationalState, StateString resourceState, String resourceId, CacheLevel cacheability)
   {
      super(mode, windowState, secure, navigationalState);

      if (resourceId == null)
      {
         // if the container didn't provide us with a resource id, fake one so that we can still build a correct WSRP URL.
         resourceId = DEFAULT_RESOURCE_ID;
      }
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

      if (resourceState != null)
      {
         createURLParameter(sb, WSRP2RewritingConstants.RESOURCE_STATE, resourceState.getStringValue());
      }
   }

   @Override
   protected void dealWithSpecificParams(Map<String, String> params, String originalURL)
   {
      super.dealWithSpecificParams(params, originalURL);

      String requireRewrite = getRawParameterValueFor(params, WSRPRewritingConstants.RESOURCE_REQUIRES_REWRITE);
      if (requireRewrite != null)
      {
         requiresRewrite = Boolean.valueOf(requireRewrite);
         params.remove(WSRPRewritingConstants.RESOURCE_REQUIRES_REWRITE);
      }

      // navigational state
      String resourceState = getRawParameterValueFor(params, WSRP2RewritingConstants.RESOURCE_STATE);
      if (resourceState != null)
      {
         this.resourceState = new OpaqueStateString(resourceState);
         params.remove(WSRP2RewritingConstants.RESOURCE_STATE);
      }

      String url = getRawParameterValueFor(params, WSRPRewritingConstants.RESOURCE_URL);
      if (url != null)
      {
         try
         {
            url = URLTools.decodeXWWWFormURL(url);
            this.resourceURL = new URL(url);
            params.remove(WSRPRewritingConstants.RESOURCE_URL);

         }
         catch (MalformedURLException e)
         {
            throw new IllegalArgumentException("Malformed URL: " + url, e);
         }
      }

      String resourceIDParam = getRawParameterValueFor(params, WSRP2RewritingConstants.RESOURCE_ID);
      if (resourceIDParam != null)
      {
         resourceId = resourceIDParam;
      }

      // GTNWSRP-103: if we don't have a resource id and wsrp-requiresRewrite has not been specified, set it to false for better compatibility
      if (resourceIDParam == null && requireRewrite == null)
      {
         requiresRewrite = false;
      }

      // we either need a resource Id or (requiredRewrite and url)
      if (resourceIDParam == null && url == null)
      {
         throw new IllegalArgumentException("The parsed parameters are not valid for a resource url. A resource URL must contain either a "
            + WSRP2RewritingConstants.RESOURCE_ID + " or " + WSRPRewritingConstants.RESOURCE_URL + " and " + WSRPRewritingConstants.RESOURCE_REQUIRES_REWRITE + " parameter in " + originalURL);
      }

      String preferOperationParam = getRawParameterValueFor(params, WSRP2RewritingConstants.RESOURCE_PREFER_OPERATION);
      if (preferOperationParam != null)
      {
         preferOperation = Boolean.valueOf(preferOperationParam);
      }

      String cacheabilityParam = getRawParameterValueFor(params, WSRP2RewritingConstants.RESOURCE_CACHEABILITY);
      if (cacheabilityParam != null)
      {
         cacheability = WSRPUtils.getCacheLevelFromResourceCacheability(cacheabilityParam);
      }

      if (resourceIDParam == null && url == null)
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

   public String getResourceId()
   {
      // we need to return a representation of the wsrp resource identification, this is not necessarily just
      // the wsrp-resourceID, we need to also consider the wsrp-url and other wsrp resource values.
      // This value returned by this method is used by the PC ResourceInvocation
      return encodeResource(resourceId, resourceURL, preferOperation);
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
    * @param resourceId        The original resource ID
    * @param resourceURL       The originial resource url
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
    * Decodes the resource information specified by the encodeResource back into proper resource values todo: improve
    *
    * @param resourceInfo
    */
   public static Map<String, String> decodeResource(String resourceInfo)
   {
      Map<String, String> resource = new HashMap<String, String>();
      
      if (resourceInfo != null && resourceInfo.startsWith(StateString.JBPNS_PREFIX))
      {
         Map<String, String[]> resourceParameters = StateString.decodeOpaqueValue(resourceInfo);

         for (Map.Entry<String, String[]> entry : resourceParameters.entrySet())
         {
            if (entry.getValue() != null && entry.getValue().length > 0)
            {
               resource.put(entry.getKey(), entry.getValue()[0]);
            }
         }
      }
      else //we are not dealing with an encoded resource ID but an actual resource ID
      {
         resource.put(WSRP2RewritingConstants.RESOURCE_ID, resourceInfo);
      }

      return resource;
   }
}
