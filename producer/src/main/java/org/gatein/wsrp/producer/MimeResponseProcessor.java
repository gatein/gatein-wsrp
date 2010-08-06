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

package org.gatein.wsrp.producer;

import org.gatein.common.net.URLTools;
import org.gatein.pc.api.invocation.response.ContentResponse;
import org.gatein.pc.api.invocation.response.PortletInvocationResponse;
import org.gatein.wsrp.WSRPConstants;
import org.gatein.wsrp.WSRPRewritingConstants;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.WSRPUtils;
import org.gatein.wsrp.servlet.ServletAccess;
import org.oasis.wsrp.v2.MimeResponse;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public abstract class MimeResponseProcessor<LocalMimeResponse extends MimeResponse> extends RequestProcessor
{
   protected String namespace;
   private static final String EMPTY = "";

   protected MimeResponseProcessor(WSRPProducerImpl producer)
   {
      super(producer);
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
      String result = renderString.replaceAll(namespace, WSRPRewritingConstants.WSRP_REWRITE_TOKEN);

      result = URLTools.replaceURLsBy(result, new WSRPUtils.AbsoluteURLReplacementGenerator(ServletAccess.getRequest()));
      return result;
   }

   Object processResponse(PortletInvocationResponse response)
   {
      ContentResponse content = (ContentResponse)response;
      String itemString = null;
      byte[] itemBinary = null;
      String contentType = content.getContentType();
      switch (content.getType())
      {
         case ContentResponse.TYPE_CHARS:
            itemString = processFragmentString(content.getChars());
            break;
         case ContentResponse.TYPE_BYTES:
            itemBinary = content.getBytes(); // fix-me: might need to convert to Base64?
            break;
         case ContentResponse.TYPE_EMPTY:
            itemString = EMPTY;
            contentType = markupRequest.getMediaType(); // assume we got what we asked for :)
            break;
      }

      LocalMimeResponse mimeResponse = WSRPTypeFactory.createMimeResponse(contentType, itemString, itemBinary, getReifiedClass());

      mimeResponse.setLocale(markupRequest.getLocale());

      //TODO: figure out requiresRewriting and useCachedItem
      Boolean requiresRewriting = true;
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

      additionallyProcessIfNeeded(mimeResponse, response);

      return createResponse(mimeResponse);
   }

   protected abstract Object createResponse(LocalMimeResponse mimeResponse);

   protected abstract Class<LocalMimeResponse> getReifiedClass();

   protected void additionallyProcessIfNeeded(LocalMimeResponse mimeResponse, PortletInvocationResponse response)
   {
      // default implementation does nothing
   }
}
