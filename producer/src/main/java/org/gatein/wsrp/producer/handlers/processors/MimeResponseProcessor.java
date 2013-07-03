/*
 * JBoss, a division of Red Hat
 * Copyright 2011, Red Hat Middleware, LLC, and individual
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

package org.gatein.wsrp.producer.handlers.processors;

import org.gatein.common.net.URLTools;
import org.gatein.common.util.MultiValuedPropertyMap;
import org.gatein.common.util.ParameterValidation;
import org.gatein.pc.api.invocation.PortletInvocation;
import org.gatein.pc.api.invocation.response.ContentResponse;
import org.gatein.pc.api.invocation.response.PortletInvocationResponse;
import org.gatein.pc.api.invocation.response.ResponseProperties;
import org.gatein.wsrp.MIMEUtils;
import org.gatein.wsrp.WSRPConstants;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.WSRPUtils;
import org.gatein.wsrp.api.servlet.ServletAccess;
import org.gatein.wsrp.payload.PayloadUtils;
import org.oasis.wsrp.v2.InvalidHandle;
import org.oasis.wsrp.v2.InvalidRegistration;
import org.oasis.wsrp.v2.MimeResponse;
import org.oasis.wsrp.v2.MissingParameters;
import org.oasis.wsrp.v2.ModifyRegistrationRequired;
import org.oasis.wsrp.v2.OperationFailed;
import org.oasis.wsrp.v2.OperationNotSupported;
import org.oasis.wsrp.v2.UnsupportedLocale;
import org.oasis.wsrp.v2.UnsupportedMimeType;
import org.oasis.wsrp.v2.UnsupportedMode;
import org.oasis.wsrp.v2.UnsupportedWindowState;
import org.w3c.dom.Element;

import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
abstract class MimeResponseProcessor<Request, LocalMimeResponse extends MimeResponse, Response> extends RequestProcessor<Request, Response>
{
   private static final String EMPTY = "";

   protected MimeResponseProcessor(ProducerHelper producer, Request request) throws InvalidRegistration, InvalidHandle, UnsupportedLocale, UnsupportedMimeType, UnsupportedWindowState, OperationFailed, MissingParameters, UnsupportedMode, ModifyRegistrationRequired, OperationNotSupported
   {
      super(producer, request);
   }

   @Override
   PortletInvocation initInvocation(WSRPPortletInvocationContext context)
   {
      return internalInitInvocation(context);
   }

   /**
    * Process String returned from RenderResult to add rewriting token if necessary, replacing namespaces by the WSRP
    * rewrite token. fix-me: need to check for producer rewriting
    *
    * @param renderString the String to be processed for rewriting marking
    * @return a String processed to add rewriting tokens as necessary
    */
   protected String processFragmentString(String renderString)
   {
      if (!ParameterValidation.isNullOrEmpty(renderString))
      {
         if (WSRPUtils.getPropertyAccessor().isURLRewritingActive())
         {
            return URLTools.replaceURLsBy(renderString, new WSRPUtils.AbsoluteURLReplacementGenerator(ServletAccess.getRequest()));
         }
      }
      return renderString;
   }

   protected Response internalProcessResponse(PortletInvocationResponse response)
   {
      ContentResponse content = (ContentResponse)response;
      String itemString = null;
      byte[] itemBinary = null;
      String contentType = content.getContentType();
      Boolean requiresRewriting = Boolean.FALSE;
      switch (content.getType())
      {
         case ContentResponse.TYPE_CHARS:
            itemString = processFragmentString(content.getChars());
            requiresRewriting = Boolean.TRUE; // assume that if we got chars, we'll need to rewrite content
            break;
         case ContentResponse.TYPE_BYTES:
            itemBinary = content.getBytes(); // fix-me: might need to convert to Base64?
            // set rewriting to true if needed
            if (MIMEUtils.needsRewriting(contentType))
            {
               requiresRewriting = Boolean.TRUE;
            }
            break;
         case ContentResponse.TYPE_EMPTY:
            itemString = EMPTY;
            contentType = markupRequest.getMediaType(); // assume we got what we asked for :)
            break;
      }

      LocalMimeResponse mimeResponse = WSRPTypeFactory.createMimeResponse(contentType, itemString, itemBinary, getReifiedClass());

      mimeResponse.setLocale(markupRequest.getLocale());

      //TODO: figure out useCachedItem
      Boolean useCachedItem = false;
      mimeResponse.setRequiresRewriting(requiresRewriting);
      mimeResponse.setUseCachedItem(useCachedItem);

      //TODO: check if anything actually uses the ccpp profile warning
      String ccppProfileWarning = null;
      mimeResponse.setCcppProfileWarning(ccppProfileWarning);

      // cache information
      int expires = content.getCacheControl().getExpirationSecs();
      // only create a CacheControl if expiration time is not 0
      if (expires != 0)
      {
         // if expires is negative, replace by -1 to make sure
         if (expires < 0)
         {
            expires = -1;
         }

         mimeResponse.setCacheControl(WSRPTypeFactory.createCacheControl(expires, WSRPConstants.CACHE_PER_USER));
      }

      // GTNWSRP-336
      final ResponseProperties properties = content.getProperties();
      if (properties != null)
      {
         populateClientAttributesWith(mimeResponse, properties.getTransportHeaders());
         populateClientAttributesWith(mimeResponse, properties.getMarkupHeaders());
      }

      additionallyProcessIfNeeded(mimeResponse, response);

      return createResponse(mimeResponse);
   }

   private <T> void populateClientAttributesWith(LocalMimeResponse mimeResponse, MultiValuedPropertyMap<T> transportHeaders)
   {
      for (String key : transportHeaders.keySet())
      {
         final List<T> values = transportHeaders.getValues(key);
         for (T value : values)
         {
            String valueAsString;
            if (value instanceof Element)
            {
               Element element = (Element)value;
               valueAsString = PayloadUtils.outputToXML(element);
            }
            else
            {
               valueAsString = value.toString();
            }
            mimeResponse.getClientAttributes().add(WSRPTypeFactory.createNamedString(key, valueAsString));
         }
      }
   }

   protected abstract Response createResponse(LocalMimeResponse mimeResponse);

   protected abstract Class<LocalMimeResponse> getReifiedClass();

   protected void additionallyProcessIfNeeded(LocalMimeResponse mimeResponse, PortletInvocationResponse response)
   {
      // default implementation does nothing
   }

   protected abstract PortletInvocation internalInitInvocation(WSRPPortletInvocationContext context);
}
