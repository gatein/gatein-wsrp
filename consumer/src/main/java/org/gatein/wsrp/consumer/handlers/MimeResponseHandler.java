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

import org.gatein.common.net.media.MediaType;
import org.gatein.common.net.media.TypeDef;
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
import org.gatein.wsrp.WSRPConstants;
import org.gatein.wsrp.WSRPConsumer;
import org.gatein.wsrp.WSRPPortletURL;
import org.gatein.wsrp.WSRPRewritingConstants;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.api.extensions.ExtensionAccessor;
import org.gatein.wsrp.api.extensions.UnmarshalledExtension;
import org.gatein.wsrp.consumer.ProducerInfo;
import org.gatein.wsrp.consumer.spi.WSRPConsumerSPI;
import org.gatein.wsrp.payload.PayloadUtils;
import org.oasis.wsrp.v2.CacheControl;
import org.oasis.wsrp.v2.Extension;
import org.oasis.wsrp.v2.MimeResponse;
import org.oasis.wsrp.v2.SessionContext;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
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

   /**
    * TODO: handle this better, we should probably have a class in the common module to determine if the MediaType
    * should be treated as a text file or as binary content. We also need to implement the algorithm to determine the
    * character encoding. See GTNCOMMON-14
    *
    * @param type
    * @return
    */
   public static boolean isInterpretableAsText(MediaType type)
   {
      return TypeDef.TEXT.equals(type.getType()) || (TypeDef.APPLICATION.equals(type.getType()) && (type.getSubtype().getName().contains("javascript")));
   }

   protected abstract SessionContext getSessionContextFrom(Response response);

   protected abstract LocalMimeResponse getMimeResponseFrom(Response response);

   @Override
   protected PortletInvocationResponse processResponse(Response response, Invocation invocation, RequestPrecursor<Invocation> requestPrecursor) throws PortletInvokerException
   {
      consumer.getSessionHandler().updateSessionIfNeeded(getSessionContextFrom(response), invocation,
         requestPrecursor.getPortletHandle());

      LocalMimeResponse mimeResponse = getMimeResponseFrom(response);
      final List<Extension> extensions = mimeResponse.getExtensions();
      for (Extension extension : extensions)
      {
         final UnmarshalledExtension unmarshalledExtension = PayloadUtils.unmarshallExtension(extension.getAny());
         ExtensionAccessor.addProducerResponseExtensionFrom(mimeResponse.getClass(), unmarshalledExtension);
      }

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

      String mimeType = mimeResponse.getMimeType();
      if (ParameterValidation.isNullOrEmpty(mimeType))
      {
         return new ErrorResponse(new IllegalArgumentException("No MIME type was provided for portlet content."));
      }

      if (Boolean.TRUE.equals(mimeResponse.isRequiresRewriting()))
      {
         if (!ParameterValidation.isNullOrEmpty(markup))
         {
            markup = processMarkup(markup, invocation);
         }

         // GTNWSRP-189:
         // if we have binary and we require rewriting, convert binary to a string assuming UTF-8 encoding and process
         // this is seen with NetUnity producer's resource download portlet which sends CSS and JS as binary for example
         if (binary != null && binary.length > 0 && isInterpretableAsText(MediaType.create(mimeType)))
         {
            try
            {
               String binaryAsString = new String(binary, "UTF-8");
               binaryAsString = processMarkup(binaryAsString, invocation);

               // reconvert to binary
               binary = binaryAsString.getBytes("UTF-8");
            }
            catch (UnsupportedEncodingException e)
            {
               // shouldn't happen since UTF-8 is always supported...
               throw new PortletInvokerException("Couldn't convert binary as String.", e);
            }
         }
      }

      return createContentResponse(mimeResponse, invocation, null, null, mimeType, binary, markup, createCacheControl(mimeResponse));
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
                                                             ResponseProperties properties, Map<String, Object> attributes,
                                                             String mimeType, byte[] bytes, String markup,
                                                             org.gatein.pc.api.cache.CacheControl cacheControl)
   {
      return new ContentResponse(properties, attributes, mimeType, bytes, markup, cacheControl);
   }

   static String processMarkup(String markup, String namespace, PortletInvocationContext context, org.gatein.pc.api.PortletContext target, URLFormat format, WSRPConsumer consumer)
   {
      // fix-me: how to deal with fragment header? => interceptor?

      // todo: remove, this is a work-around for GTNWSRP-12
      if (!WSRPConstants.RUNS_IN_EPP)
      {
         markup = markup.replaceFirst("%3ftimeout%3d.*%2f", "%2f");
      }

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
      private final String namespace;

      //TODO: the URLFormat here doesn't make any sense, the escaping needs to be unique for each url processed.
      protected MarkupProcessor(String namespace, PortletInvocationContext context, org.gatein.pc.api.PortletContext target, URLFormat format, ProducerInfo info)
      {
         this.namespace = namespace;
         this.context = context;
         this.format = format;
         supportedCustomModes = info.getSupportedCustomModes();
         supportedCustomWindowStates = info.getSupportedCustomWindowStates();
      }

      public String getReplacementFor(String match, String prefix, String suffix, boolean matchedPrefixOnly)
      {
         // We run into some issues with url encoding. We should not be making assumptions about
         // what url encoding we should be using. For example, we may be dealing with html encoding (ampersand as &)
         // or xhtml/xml encoding (ampersand as &amp;) or javascript encoding (ampersand as \x26).
         // When we recreate the WSRP url as a portlet url, we have to use whatever encoding was used in the original wsrp url,
         // we need to assume that is the correct encoding for the situation.

         // NOTE: there may be other encoding situations we are not currently dealing with :(

         boolean useJavaScriptEscaping = false;
         // work around for GTNWSRP-93:
         if (match.contains("\\x2D") || match.contains("\\x26"))
         {
            useJavaScriptEscaping = true;
            match = match.replaceAll("\\\\x2D", "-").replaceAll("\\\\x26", "&amp;");
         }

         WSRPPortletURL portletURL = WSRPPortletURL.create(match, supportedCustomModes, supportedCustomWindowStates, true);

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

         // we now need to add back the javascript url encoding if it was originally used
         // NOTE: we should fix this by specifying the escaping to be used in URLFormat when it supported (see GTNPC-41)
         if (useJavaScriptEscaping)
         {
            value = value.replaceAll("-", "\\\\x2D").replaceAll("&amp;", "\\\\x26");
         }

         return value;
      }
   }
}
