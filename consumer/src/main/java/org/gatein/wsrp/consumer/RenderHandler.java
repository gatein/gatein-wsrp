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

import org.gatein.common.net.URLTools;
import org.gatein.common.text.TextTools;
import org.gatein.pc.api.PortletInvokerException;
import org.gatein.pc.api.URLFormat;
import org.gatein.pc.api.cache.CacheScope;
import org.gatein.pc.api.invocation.PortletInvocation;
import org.gatein.pc.api.invocation.RenderInvocation;
import org.gatein.pc.api.invocation.response.ErrorResponse;
import org.gatein.pc.api.invocation.response.FragmentResponse;
import org.gatein.pc.api.invocation.response.PortletInvocationResponse;
import org.gatein.pc.api.spi.PortletInvocationContext;
import org.gatein.wsrp.WSRPConstants;
import org.gatein.wsrp.WSRPConsumer;
import org.gatein.wsrp.WSRPPortletURL;
import org.gatein.wsrp.WSRPResourceURL;
import org.gatein.wsrp.WSRPRewritingConstants;
import org.gatein.wsrp.WSRPTypeFactory;
import org.oasis.wsrp.v1.CacheControl;
import org.oasis.wsrp.v1.Extension;
import org.oasis.wsrp.v1.GetMarkup;
import org.oasis.wsrp.v1.MarkupContext;
import org.oasis.wsrp.v1.MarkupResponse;
import org.oasis.wsrp.v1.PortletContext;
import org.oasis.wsrp.v1.RuntimeContext;
import org.oasis.wsrp.v1.SessionContext;
import org.oasis.wsrp.v1.UserContext;

import javax.xml.ws.Holder;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 12082 $
 * @since 2.4 (May 31, 2006)
 */
public class RenderHandler extends InvocationHandler
{

   private static final org.gatein.pc.api.cache.CacheControl DEFAULT_CACHE_CONTROL = new org.gatein.pc.api.cache.CacheControl(0, CacheScope.PRIVATE, null);

   protected RenderHandler(WSRPConsumerImpl consumer)
   {
      super(consumer);
   }

   protected Object prepareRequest(RequestPrecursor requestPrecursor, PortletInvocation invocation)
   {
      if (!(invocation instanceof RenderInvocation))
      {
         throw new IllegalArgumentException("RenderHandler can only handle RenderInvocations!");
      }

      // Create the markup request
      PortletContext portletContext = requestPrecursor.getPortletContext();
      log.debug("Consumer about to attempt rendering portlet '" + portletContext.getPortletHandle() + "'");
      return WSRPTypeFactory.createMarkupRequest(portletContext, requestPrecursor.runtimeContext, requestPrecursor.markupParams);
   }

   protected PortletInvocationResponse processResponse(Object response, PortletInvocation invocation, RequestPrecursor requestPrecursor)
   {
      MarkupResponse markupResponse = (MarkupResponse)response;
      log.debug("Starting processing response");

      // process the response
      consumer.getSessionHandler().updateSessionIfNeeded(markupResponse.getSessionContext(), invocation,
         requestPrecursor.getPortletHandle());

      MarkupContext markupContext = markupResponse.getMarkupContext();
      String markup = markupContext.getMarkupString();
      byte[] binary = markupContext.getMarkupBinary();
      if (markup != null && binary != null)
      {
         return new ErrorResponse(new IllegalArgumentException("Markup response cannot contain both string and binary " +
            "markup. Per Section 6.1.10 of the WSRP specification, this is a Producer error."));
      }

      if (markup == null && binary == null)
      {
         if (markupContext.isUseCachedMarkup())
         {
            //todo: deal with cache
         }
         else
         {
            return new ErrorResponse(new IllegalArgumentException("Markup response must contain at least string or binary" +
               " markup. Per Section 6.1.10 of the WSRP specification, this is a Producer error."));
         }
      }

      if (markup != null && markup.length() > 0)
      {
         if (Boolean.TRUE.equals(markupContext.isRequiresUrlRewriting()))
         {
            markup = processMarkup(
               markup,
               getNamespaceFrom(invocation.getWindowContext()),
               invocation.getContext(),
               invocation.getTarget(),
               new URLFormat(invocation.getSecurityContext().isSecure(), invocation.getSecurityContext().isAuthenticated(), true, true),
               consumer
            );
         }
      }
      else
      {
         // todo: need to deal with binary
      }

      String mimeType = markupContext.getMimeType();
      if (mimeType == null || mimeType.length() == 0)
      {
         return new ErrorResponse(new IllegalArgumentException("No MIME type was provided for portlet content."));
      }

      // generate appropriate CacheControl
      org.gatein.pc.api.cache.CacheControl cacheControl = createCacheControl(markupContext);

      FragmentResponse result = new FragmentResponse(null, null, mimeType, null, markup,
         markupContext.getPreferredTitle(), cacheControl, invocation.getPortalContext().getModes());

      log.debug("Response processed");
      return result;
   }

   protected void updateUserContext(Object request, UserContext userContext)
   {
      getRenderRequest(request).setUserContext(userContext);
   }

   protected void updateRegistrationContext(Object request) throws PortletInvokerException
   {
      getRenderRequest(request).setRegistrationContext(consumer.getRegistrationContext());
   }

   protected RuntimeContext getRuntimeContextFrom(Object request)
   {
      return getRenderRequest(request).getRuntimeContext();
   }

   protected Object performRequest(Object request) throws Exception
   {
      GetMarkup renderRequest = getRenderRequest(request);
      log.debug("getMarkup on '" + renderRequest.getPortletContext().getPortletHandle() + "'");

      // invocation
      Holder<SessionContext> sessionContextHolder = new Holder<SessionContext>();
      Holder<MarkupContext> markupContextHolder = new Holder<MarkupContext>();
      consumer.getMarkupService().getMarkup(renderRequest.getRegistrationContext(), renderRequest.getPortletContext(),
         renderRequest.getRuntimeContext(), renderRequest.getUserContext(), renderRequest.getMarkupParams(),
         markupContextHolder, sessionContextHolder, new Holder<List<Extension>>());
      MarkupResponse markupResponse = new MarkupResponse();
      markupResponse.setMarkupContext(markupContextHolder.value);
      markupResponse.setSessionContext(sessionContextHolder.value);
      return markupResponse;
   }

   private GetMarkup getRenderRequest(Object request)
   {
      if (request instanceof GetMarkup)
      {
         return (GetMarkup)request;
      }

      throw new IllegalArgumentException("RenderHandler: Request is not a GetMarkup request!");
   }

   static String processMarkup(String markup, String namespace, PortletInvocationContext context, org.gatein.pc.api.PortletContext target, URLFormat format, WSRPConsumer consumer)
   {
      // fix-me: how to deal with fragment header? => interceptor?

      markup = TextTools.replaceBoundedString(
         markup,
         WSRPRewritingConstants.WSRP_REWRITE,
         WSRPRewritingConstants.END_WSRP_REWRITE,
         new ResourceURLStringReplacementGenerator(namespace, context, target, format, consumer.getProducerInfo()),
         true,
         false,
         true
      );

      return markup;
   }

   private org.gatein.pc.api.cache.CacheControl createCacheControl(MarkupContext markupContext)
   {
      CacheControl cacheControl = markupContext.getCacheControl();
      org.gatein.pc.api.cache.CacheControl result = DEFAULT_CACHE_CONTROL;

      int expires;
      if (cacheControl != null)
      {
         expires = cacheControl.getExpires();
         String userScope = cacheControl.getUserScope();

         // check that we support the user scope...
         if (consumer.supportsUserScope(userScope))
         {
            log.debug("RenderHandler.processRenderRequest: trying to cache markup " + userScope + " for " + expires + " seconds.");
            CacheScope scope;
            if (WSRPConstants.CACHE_FOR_ALL.equals(userScope))
            {
               scope = CacheScope.PUBLIC;
            }
            else if (WSRPConstants.CACHE_PER_USER.equals(userScope))
            {
               scope = CacheScope.PRIVATE;
            }
            else
            {
               throw new IllegalArgumentException("Unknown CacheControl user scope: " + userScope); // should not happen
            }

            result = new org.gatein.pc.api.cache.CacheControl(expires, scope, cacheControl.getValidateTag());
         }
      }

      return result;
   }

   static class ResourceURLStringReplacementGenerator implements TextTools.StringReplacementGenerator
   {
      private final PortletInvocationContext context;
      private final URLFormat format;
      private final Set<String> supportedCustomModes;
      private final Set<String> supportedCustomWindowStates;
      private final String serverAddress;
      private final String portletApplicationName;
      private final String namespace;
      public static final int URL_DELIMITER_LENGTH = WSRPRewritingConstants.RESOURCE_URL_DELIMITER.length();

      private ResourceURLStringReplacementGenerator(String namespace, PortletInvocationContext context, org.gatein.pc.api.PortletContext target, URLFormat format, ProducerInfo info)
      {
         this.namespace = namespace;
         this.context = context;
         this.format = format;
         supportedCustomModes = info.getSupportedCustomModes();
         supportedCustomWindowStates = info.getSupportedCustomWindowStates();
         serverAddress = info.getEndpointConfigurationInfo().getRemoteHostAddress();
         portletApplicationName = target.getApplicationName();
      }

      public String getReplacementFor(String match, String prefix, String suffix)
      {
         if (match.startsWith(WSRPRewritingConstants.RESOURCE_URL_DELIMITER))
         {
            // we have a resource URL coming from a template so extract URL
            int index = match.lastIndexOf(WSRPRewritingConstants.RESOURCE_URL_DELIMITER);

/*
            // todo: right now, no need to extract value of require rewrite..
            String requireRewriteStr = match.substring(index + URL_DELIMITER_LENGTH);
            boolean requireRewrite = Boolean.valueOf(requireRewriteStr);
            if (requireRewrite)
            {
               // FIX-ME: do something
               log.debug("Required re-writing but this is not yet implemented...");
            }*/

            match = match.substring(URL_DELIMITER_LENGTH, index);
            return URLTools.decodeXWWWFormURL(match);
         }
         else if (prefix.equals(match))
         {
            return namespace;
         }
         else if (match.startsWith(WSRPRewritingConstants.BEGIN_WSRP_REWRITE_END))
         {
            // remove end of rewrite token
            match = match.substring(WSRPRewritingConstants.BEGIN_WSRP_REWRITE_END.length());

            WSRPPortletURL portletURL = WSRPPortletURL.create(match, supportedCustomModes, supportedCustomWindowStates, true);
            if (portletURL instanceof WSRPResourceURL)
            {
               WSRPResourceURL resource = (WSRPResourceURL)portletURL;
               String replacement = getResourceURL(match, resource);

               // if the URL starts with /, prepend the remote host address and the portlet application name so that we
               // can attempt to create a remotely available URL
               if (replacement.startsWith(URLTools.SLASH))
               {
                  replacement = WSRPResourceURL.createAbsoluteURLFrom(replacement, serverAddress, portletApplicationName);
               }

               return replacement;

/*
               todo: use this code to reactivate primitive use of resources
               // get the parsed URL and add marker to it so that the consumer can know it needs to be intercepted
               URL url = resource.getResourceURL();
               String query = url.getQuery();
               if (ParameterValidation.isNullOrEmpty(query))
               {
                  query = WSRPRewritingConstants.GTNRESOURCE;
               }
               else
               {
                  query = "+" + WSRPRewritingConstants.GTNRESOURCE;
               }

               try
               {
                  URI uri = new URI(url.getProtocol(), url.getUserInfo(), url.getHost(), url.getPort(),
                     url.getPath(), query, url.getRef());

                  // set the resulting URI as the new resource ID, must be encoded as it will be used in URLs
                  String s = URLTools.encodeXWWWFormURL(uri.toString());
                  resource.setResourceId(s);
               }
               catch (Exception e)
               {
                  throw new IllegalArgumentException("Cannot parse specified Resource as a URI: " + url);
               }*/

            }

            return context.renderURL(portletURL, format);
         }
         else
         {
            // match is not something we know how to process
            return match;
         }
      }
   }

   private static String getResourceURL(String urlAsString, WSRPResourceURL resource)
   {
      String resourceURL = resource.getResourceURL().toExternalForm();
      if (log.isDebugEnabled())
      {
         log.debug("URL '" + urlAsString + "' refers to a resource which are not currently well supported. " +
            "Attempting to craft a URL that we might be able to work with: '" + resourceURL + "'");
      }

      // right now the resourceURL should be output as is, because it will be used directly but it really should be encoded 
      return resourceURL;
   }
}
