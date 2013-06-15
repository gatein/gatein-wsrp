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

package org.gatein.wsrp.consumer.handlers;

import org.gatein.common.text.TextTools;
import org.gatein.common.util.ParameterValidation;
import org.gatein.pc.api.PortletInvokerException;
import org.gatein.pc.api.URLFormat;
import org.gatein.pc.api.cache.CacheScope;
import org.gatein.pc.api.invocation.PortletInvocation;
import org.gatein.pc.api.invocation.response.ContentResponse;
import org.gatein.pc.api.invocation.response.ErrorResponse;
import org.gatein.pc.api.invocation.response.PortletInvocationResponse;
import org.gatein.pc.api.invocation.response.ResponseProperties;
import org.gatein.pc.api.spi.PortletInvocationContext;
import org.gatein.pc.api.spi.SecurityContext;
import org.gatein.wsrp.MIMEUtils;
import org.gatein.wsrp.WSRPConstants;
import org.gatein.wsrp.WSRPConsumer;
import org.gatein.wsrp.WSRPPortletURL;
import org.gatein.wsrp.WSRPRewritingConstants;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.consumer.ProducerInfo;
import org.gatein.wsrp.consumer.spi.WSRPConsumerSPI;
import org.gatein.wsrp.payload.PayloadUtils;
import org.oasis.wsrp.v2.CacheControl;
import org.oasis.wsrp.v2.MimeResponse;
import org.oasis.wsrp.v2.NamedString;
import org.oasis.wsrp.v2.SessionContext;
import org.w3c.dom.Element;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public abstract class MimeResponseHandler<Invocation extends PortletInvocation, Request, Response, LocalMimeResponse extends MimeResponse> extends InvocationHandler<Invocation, Request, Response>
{
   private static final org.gatein.pc.api.cache.CacheControl DEFAULT_CACHE_CONTROL = new org.gatein.pc.api.cache.CacheControl(0, CacheScope.PRIVATE, null);

   protected MimeResponseHandler(WSRPConsumerSPI consumer)
   {
      super(consumer);
   }

   protected abstract SessionContext getSessionContextFrom(Response response);

   protected abstract LocalMimeResponse getMimeResponseFrom(Response response);

   @Override
   protected PortletInvocationResponse processResponse(Response response, Invocation invocation, RequestPrecursor<Invocation> requestPrecursor) throws PortletInvokerException
   {
      consumer.getSessionHandler().updateSessionIfNeeded(getSessionContextFrom(response), invocation,
         requestPrecursor.getPortletHandle());

      LocalMimeResponse mimeResponse = getMimeResponseFrom(response);
      processExtensions(response);

      return rewriteResponseIfNeeded(mimeResponse, invocation);
   }

   PortletInvocationResponse rewriteResponseIfNeeded(final LocalMimeResponse mimeResponse, final Invocation invocation) throws PortletInvokerException
   {
      String markup = mimeResponse.getItemString();
      byte[] binary = mimeResponse.getItemBinary();
      if (markup != null && binary != null)
      {
         return new ErrorResponse(new IllegalArgumentException("Markup response cannot contain both string and binary " +
            "markup. Per Section 6.1.10 of the WSRP 1.0 specification, this is a Producer error."));
      }

      if (markup == null && binary == null)
      {
         if (mimeResponse.isUseCachedItem() != null && mimeResponse.isUseCachedItem())
         {
            //todo: deal with cache GTNWSRP-40
            log.debug("Consumer " + consumer.getProducerId() + " requested cached data. Not implemented yet!");
         }
         else
         {
            return new ErrorResponse(new IllegalArgumentException("Markup response must contain at least string or binary" +
               " markup. Per Section 6.1.10 of the WSRP 1.0 specification, this is a Producer error."));
         }
      }

      final String mimeType = mimeResponse.getMimeType();

      if (Boolean.TRUE.equals(mimeResponse.isRequiresRewriting()))
      {
         if (!ParameterValidation.isNullOrEmpty(markup))
         {
            markup = processMarkup(markup, invocation);
         }

         // GTNWSRP-189: if we have binary and we require rewriting, convert binary to a string and process
         if (binary != null && binary.length > 0 && MIMEUtils.isInterpretableAsText(mimeType))
         {
            try
            {
               final String charset = MIMEUtils.getCharsetFrom(mimeType);
               String binaryAsString = new String(binary, charset);
               binaryAsString = processMarkup(binaryAsString, invocation);

               // reconvert to binary
               binary = binaryAsString.getBytes(charset);
            }
            catch (UnsupportedEncodingException e)
            {
               throw new PortletInvokerException("Couldn't convert binary as String.", e);
            }
         }
      }

      // GTNWSRP-336
      final ResponseProperties properties = getResponsePropertiesFrom(mimeResponse, consumer.getProducerInfo().getEndpointConfigurationInfo().getWsdlDefinitionURL());
      return createContentResponse(mimeResponse, invocation, properties, mimeType, binary, markup, createCacheControl(mimeResponse));
   }

   private ResponseProperties getResponsePropertiesFrom(MimeResponse mimeResponse, String producerURLAsString)
   {
      final List<NamedString> clientAttributes = mimeResponse.getClientAttributes();
      final ResponseProperties properties;
      if (ParameterValidation.existsAndIsNotEmpty(clientAttributes))
      {
         URL producerURL;
         try
         {
            producerURL = new URL(producerURLAsString);
         }
         catch (MalformedURLException e)
         {
            // shouldn't happen
            throw new RuntimeException(e);
         }
         properties = new ResponseProperties();
         for (NamedString attribute : clientAttributes)
         {
            final String name = attribute.getName();
            final String value = attribute.getValue();
            if (javax.portlet.MimeResponse.MARKUP_HEAD_ELEMENT.equals(name))
            {
               final Element element = PayloadUtils.parseFromXMLString(value);
               properties.getMarkupHeaders().addValue(name, element);
            }
            else
            {
               properties.getTransportHeaders().addValue(name, value);
            }
         }
      }
      else
      {
         properties = null;
      }
      return properties;
   }

   private String processMarkup(String markup, Invocation invocation)
   {
      SecurityContext securityContext = invocation.getSecurityContext();
      markup = processMarkup(
         markup,
         WSRPTypeFactory.getNamespaceFrom(invocation.getWindowContext()),
         invocation.getContext(),
         invocation.getTarget(),
         new URLFormat(securityContext.isSecure(), securityContext.isAuthenticated(), true, true),
         consumer
      );
      return markup;
   }

   protected PortletInvocationResponse createContentResponse(LocalMimeResponse mimeResponse, Invocation invocation,
                                                             ResponseProperties properties, String mimeType, byte[] bytes, String markup,
                                                             org.gatein.pc.api.cache.CacheControl cacheControl)
   {
      if (markup != null)
      {
         return new ContentResponse(properties, null, mimeType, null, markup, cacheControl);
      }
      else
      {
         return new ContentResponse(properties, null, mimeType, MIMEUtils.UTF_8, bytes, cacheControl);
      }
   }

   static String processMarkup(String markup, String namespace, PortletInvocationContext context, org.gatein.pc.api.PortletContext target, URLFormat format, WSRPConsumer consumer)
   {
      markup = TextTools.replaceBoundedString(
         markup,
         WSRPRewritingConstants.BEGIN_WSRP_REWRITE,
         WSRPRewritingConstants.END_WSRP_REWRITE,
         new MarkupProcessor(namespace, context, target, format, consumer.getProducerInfo()),
         true,
         false
      );

      markup = markup.replaceAll(WSRPRewritingConstants.WSRP_REWRITE_TOKEN, namespace);

      return markup;
   }

   protected org.gatein.pc.api.cache.CacheControl createCacheControl(LocalMimeResponse mimeResponse)
   {
      CacheControl cacheControl = mimeResponse.getCacheControl();
      org.gatein.pc.api.cache.CacheControl result = DEFAULT_CACHE_CONTROL;

      int expires;
      if (cacheControl != null)
      {
         expires = cacheControl.getExpires();
         String userScope = cacheControl.getUserScope();

         // check that we support the user scope...
         if (consumer.supportsUserScope(userScope))
         {
            if (debug)
            {
               log.debug("Trying to cache markup " + userScope + " for " + expires + " seconds.");
            }
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

   /**
    * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
    * @version $Revision$
    */
   private static class MarkupProcessor implements TextTools.StringReplacementGenerator
   {
      private final PortletInvocationContext context;
      private final URLFormat format;
      private final Set<String> supportedCustomModes;
      private final Set<String> supportedCustomWindowStates;

      protected MarkupProcessor(String namespace, PortletInvocationContext context, org.gatein.pc.api.PortletContext target, URLFormat format, ProducerInfo info)
      {
         this.context = context;
         this.format = format;
         supportedCustomModes = info.getSupportedCustomModes();
         supportedCustomWindowStates = info.getSupportedCustomWindowStates();
      }

      public String getReplacementFor(String match, String prefix, String suffix, boolean matchedPrefixOnly)
      {
         /*
         We run into some issues with URL encoding. We should not be making assumptions about which URL encoding we should be using. For example, we may be dealing with HTML
         encoding (ampersand as &) or XHTML/XML encoding (ampersand as &amp;) or Javascript encoding (ampersand as \x26). When we recreate the WSRP URL as a portlet URL, we must
         use whatever encoding was used in the original WSRP URL, assuming that this is the correct encoding for the situation.

         NOTE: there may be other encoding situations we are not currently dealing with :(

         Since the URL to be written has to be of format wsrp-urlType=[render|resource|blockingAction]&key1=value1&key2=..., we should be able to extract what the '-' is being
         encoded as and use that as a reference to determine what the '&' is encoded as.

         NOTE: the WSRP specification only covers the situation of & and &amp;. It should be acceptable for us to throw an error or ignore rewriting but we still perform our best
         effort to properly encode the URL.
         */


         // PBR-421: sometimes the /wsrp-rewrite will be escaped as \/wsrp-rewrite so there might be a trailing '\' in the match that we need to remove
         final String trailing = "\\";
         if (match.endsWith(trailing))
         {
            match = match.substring(0, match.length() - trailing.length());
         }

         boolean useJavaScriptEscaping = false;
         boolean useISO_8859_1Encoding = false;
         // work around for GTNWSRP-93 && PBR-421
         if (match.contains("\\x2D") || match.contains("\\x26"))
         {
            useJavaScriptEscaping = true;
            match = match.replaceAll("\\\\x2D", "-").replaceAll("\\\\x26", "&amp;");
         }
         else if (match.contains("\\u002D") || match.contains("\\u0026"))
         {
            useISO_8859_1Encoding = true;
            match = match.replaceAll("\\\\u002D", "-").replaceAll("\\\\u0026", "&amp;");
         }

         WSRPPortletURL portletURL = WSRPPortletURL.create(match, supportedCustomModes, supportedCustomWindowStates, true);

         // escaping format needs to be unique for each processed URL so create a new URLFormat based on what was originally asked but with tailored encoding
         URLFormat urlFormat;
         // If the current url is using &amp; then specify we want to use xml escaped ampersands
         if (match.contains("&amp;"))
         {
            urlFormat = new URLFormat(format.getWantSecure(), format.getWantAuthenticated(), format.getWantRelative(), true);
         }
         else
         {
            urlFormat = new URLFormat(format.getWantSecure(), format.getWantAuthenticated(), format.getWantRelative(), false);
         }

         String value = context.renderURL(portletURL, urlFormat);

         // If Javascript encoding was used, we need to re-escape the URL for Javascript
         // NOTE: we should fix this by specifying the escaping to be used in URLFormat when it supported (see GTNPC-41)
         if (useJavaScriptEscaping)
         {
            value = value.replaceAll("-", "\\\\x2D").replaceAll("&amp;", "\\\\x26");
         }
         else if (useISO_8859_1Encoding)
         {
            value = value.replaceAll("-", "\\\\u002D").replaceAll("&amp;", "\\\\u0026");
         }

         return value;
      }
   }
}
