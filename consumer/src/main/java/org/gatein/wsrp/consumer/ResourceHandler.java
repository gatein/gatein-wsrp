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

import org.apache.commons.httpclient.Cookie;
import org.gatein.common.io.IOTools;
import org.gatein.common.net.media.MediaType;
import org.gatein.common.net.media.TypeDef;
import org.gatein.common.util.MultiValuedPropertyMap;
import org.gatein.common.util.ParameterValidation;
import org.gatein.common.util.Tools;
import org.gatein.pc.api.PortletInvokerException;
import org.gatein.pc.api.StateString;
import org.gatein.pc.api.invocation.PortletInvocation;
import org.gatein.pc.api.invocation.ResourceInvocation;
import org.gatein.pc.api.invocation.response.ContentResponse;
import org.gatein.pc.api.invocation.response.PortletInvocationResponse;
import org.gatein.pc.api.invocation.response.ResponseProperties;
import org.gatein.pc.api.spi.InstanceContext;
import org.gatein.pc.api.state.AccessMode;
import org.gatein.wsrp.WSRPResourceURL;
import org.gatein.wsrp.WSRPRewritingConstants;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.WSRPUtils;
import org.gatein.wsrp.handler.CookieUtil;
import org.gatein.wsrp.spec.v2.WSRP2RewritingConstants;
import org.oasis.wsrp.v2.Extension;
import org.oasis.wsrp.v2.GetResource;
import org.oasis.wsrp.v2.MarkupParams;
import org.oasis.wsrp.v2.PortletContext;
import org.oasis.wsrp.v2.ResourceContext;
import org.oasis.wsrp.v2.ResourceParams;
import org.oasis.wsrp.v2.ResourceResponse;
import org.oasis.wsrp.v2.RuntimeContext;
import org.oasis.wsrp.v2.SessionContext;
import org.oasis.wsrp.v2.UserContext;

import javax.xml.ws.Holder;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class ResourceHandler extends MimeResponseHandler<ResourceResponse, ResourceContext>
{

   protected ResourceHandler(WSRPConsumerImpl consumer)
   {
      super(consumer);
   }

   @Override
   protected SessionContext getSessionContextFrom(ResourceResponse resourceResponse)
   {
      return resourceResponse.getSessionContext();
   }

   @Override
   protected ResourceContext getMimeResponseFrom(ResourceResponse resourceResponse)
   {
      return resourceResponse.getResourceContext();
   }

   @Override
   protected void updateUserContext(Object request, UserContext userContext)
   {
      if (request instanceof GetResource)
      {
         getResourceRequest(request).setUserContext(userContext);
      }
   }

   @Override
   protected void updateRegistrationContext(Object request) throws PortletInvokerException
   {
      if (request instanceof GetResource)
      {
         getResourceRequest(request).setRegistrationContext(consumer.getRegistrationContext());
      }
   }

   @Override
   protected RuntimeContext getRuntimeContextFrom(Object request)
   {
      if (request instanceof GetResource)
      {
         return getResourceRequest(request).getRuntimeContext();
      }
      else
      {
         return null;
      }
   }

   @Override
   protected Object prepareRequest(RequestPrecursor requestPrecursor, PortletInvocation invocation)
   {
      if (!(invocation instanceof ResourceInvocation))
      {
         throw new IllegalArgumentException("ResourceHandler can only handle ResourceInvocations!");
      }

      ResourceInvocation resourceInvocation = (ResourceInvocation)invocation;

      String resourceInvocationId = resourceInvocation.getResourceId();

      Map<String, String> resourceMap = WSRPResourceURL.decodeResource(resourceInvocationId);

      String resourceId = resourceMap.get(WSRP2RewritingConstants.RESOURCE_ID);
      String resourceURL = resourceMap.get(WSRPRewritingConstants.RESOURCE_URL);
      String preferOperationAsString = resourceMap.get(WSRP2RewritingConstants.RESOURCE_PREFER_OPERATION);
      boolean preferOperation = (preferOperationAsString != null && Boolean.parseBoolean(preferOperationAsString));

      int version = 1;
      try
      {
         version = consumer.getMarkupService().getVersion();
      }
      catch (PortletInvokerException portletInvokerException)
      {
         log.warn("Encountered an exception when trying to get the consumer's markup service's version, assuming WSRP 1.0 compliant.", portletInvokerException);
      }

      if (version == 2 && (preferOperation || resourceURL == null || (resourceId != null && resourceId.length() > 0)))
      {
         return prepareGetResourceRequest(requestPrecursor, resourceInvocation, resourceId);
      }
      else
      {
         return resourceURL;
      }

   }

   private GetResource prepareGetResourceRequest(RequestPrecursor requestPrecursor, ResourceInvocation invocation, String resourceId)
   {
      PortletContext portletContext = requestPrecursor.getPortletContext();

      // since we actually extracted the data into MarkupParams in the RequestPrecursor, use that! :)
      MarkupParams params = requestPrecursor.markupParams;

      // access mode
      InstanceContext instanceContext = invocation.getInstanceContext();
      ParameterValidation.throwIllegalArgExceptionIfNull(instanceContext, "instance context");
      AccessMode accessMode = instanceContext.getAccessMode();
      ParameterValidation.throwIllegalArgExceptionIfNull(accessMode, "access mode");
      if (debug)
      {
         log.debug("Portlet is requesting " + accessMode + " access mode");
      }

      // if the portlet didn't request a resource id, use the fake one:
      if (resourceId == null || resourceId.length() == 0)
      {
         resourceId = WSRPResourceURL.DEFAULT_RESOURCE_ID;
      }

      // Create ResourceParams
      ResourceParams resourceParams = WSRPTypeFactory.createResourceParams(params.isSecureClientCommunication(),
         params.getLocales(), params.getMimeTypes(), params.getMode(), params.getWindowState(), resourceId,
         WSRPUtils.getStateChangeFromAccessMode(accessMode));

      resourceParams.setNavigationalContext(params.getNavigationalContext());
      resourceParams.setClientData(params.getClientData());
      resourceParams.setResourceCacheability(WSRPUtils.getResourceCacheabilityFromCacheLevel(invocation.getCacheLevel()));

      StateString resourceState = invocation.getResourceState();
      if (resourceState != null)
      {
         String state = resourceState.getStringValue();
         if (!StateString.JBPNS_PREFIX.equals(state))  // fix-me: see JBPORTAL-900
         {
            resourceParams.setResourceState(state);
         }
      }

      return WSRPTypeFactory.createResourceRequest(portletContext, requestPrecursor.runtimeContext, resourceParams);
   }

   @Override
   protected Object performRequest(Object request) throws Exception
   {
      if (request instanceof GetResource)
      {
         return performGetResourceRequest((GetResource)request);
      }
      else if (request instanceof String)
      {
         return performURLRequest((String)request);
      }
      else
      {
         throw new IllegalArgumentException("ResourceHandler performRequest can only be called with a GetResource or String object. Received : " + request);
      }

   }

   private ResourceResponse performGetResourceRequest(GetResource getResource) throws Exception
   {
      Holder<SessionContext> sessionContextHolder = new Holder<SessionContext>();
      Holder<ResourceContext> resourceContextHolder = new Holder<ResourceContext>();
      Holder<PortletContext> portletContextHolder = new Holder<PortletContext>(getResource.getPortletContext());

      consumer.getMarkupService().getResource(getResource.getRegistrationContext(), portletContextHolder, getResource.getRuntimeContext(),
         getResource.getUserContext(), getResource.getResourceParams(), resourceContextHolder, sessionContextHolder, new Holder<List<Extension>>());

      ResourceResponse resourceResponse = WSRPTypeFactory.createResourceResponse(resourceContextHolder.value);
      resourceResponse.setPortletContext(portletContextHolder.value);
      resourceResponse.setSessionContext(sessionContextHolder.value);
      return resourceResponse;
   }

   private ContentResponse performURLRequest(String resourceURL) throws Exception
   {
      URL url = new URL(resourceURL);
      URLConnection urlConnection = url.openConnection();
      String contentType = urlConnection.getContentType();

      // init ResponseProperties for ContentResponse result
      Map<String, List<String>> headers = urlConnection.getHeaderFields();
      ResponseProperties props = new ResponseProperties();
      MultiValuedPropertyMap<String> transportHeaders = props.getTransportHeaders();
      for (Map.Entry<String, List<String>> entry : headers.entrySet())
      {
         String key = entry.getKey();
         if (key != null)
         {
            List<String> values = entry.getValue();
            if (values != null)
            {
               if (CookieUtil.SET_COOKIE.equals(key))
               {
                  Cookie[] cookies = CookieUtil.extractCookiesFrom(url, values.toArray(new String[values.size()]));
                  List<javax.servlet.http.Cookie> propCookies = props.getCookies();
                  for (Cookie cookie : cookies)
                  {
                     propCookies.add(CookieUtil.convertFrom(cookie));
                  }
               }
               else
               {
                  for (String value : values)
                  {
                     transportHeaders.addValue(key, value);
                  }
               }
            }
         }
      }

      int length = urlConnection.getContentLength();
      // if length is not known, use a default value
      length = (length > 0 ? length : Tools.DEFAULT_BUFFER_SIZE * 8);
      byte[] bytes = IOTools.getBytes(urlConnection.getInputStream(), length);

      ContentResponse result;
      MediaType type = MediaType.create(contentType);
      if (TypeDef.TEXT.equals(type.getType()))
      {
         // determine the charset of the content, if any
         String charset = "UTF-8";
         if (contentType != null)
         {
            for (String part : contentType.split(";"))
            {
               if (part.startsWith("charset="))
               {
                  charset = part.substring("charset=".length());
               }
            }
         }

         // build a String-based content response
         result = new ContentResponse(props, Collections.<String, Object>emptyMap(), contentType, null, new String(bytes, charset), null);
      }
      else
      {
         // build a byte-based content response
         result = new ContentResponse(props, Collections.<String, Object>emptyMap(), contentType, bytes, null, null);
      }

      return result;
   }

   @Override
   protected PortletInvocationResponse processResponse(Object response, PortletInvocation invocation, RequestPrecursor requestPrecursor) throws PortletInvokerException
   {
      if (response instanceof ResourceResponse)
      {
         return super.processResponse(response, invocation, requestPrecursor);
      }
      else if (response instanceof ContentResponse)
      {
         return (ContentResponse)response;
      }
      else
      {
         throw new PortletInvokerException("Invalid response object: " + response + ". Expected either a " + ContentResponse.class + " or a " + ResourceResponse.class);
      }
   }

   private GetResource getResourceRequest(Object request)
   {
      if (request instanceof GetResource)
      {
         return (GetResource)request;
      }

      throw new IllegalArgumentException("ResourceHandler: Request is not a GetResource request!");
   }
}
