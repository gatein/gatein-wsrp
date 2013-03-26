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

import org.gatein.common.io.IOTools;
import org.gatein.common.util.MultiValuedPropertyMap;
import org.gatein.common.util.Tools;
import org.gatein.pc.api.invocation.response.ResponseProperties;
import org.gatein.wsrp.MIMEUtils;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.consumer.spi.WSRPConsumerSPI;
import org.gatein.wsrp.handler.CookieUtil;
import org.gatein.wsrp.handler.RequestHeaderClientHandler;
import org.oasis.wsrp.v2.GetResource;
import org.oasis.wsrp.v2.ResourceContext;
import org.oasis.wsrp.v2.ResourceResponse;

import java.net.HttpCookie;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class DirectResourceServingHandler extends ResourceHandler
{
   protected DirectResourceServingHandler(WSRPConsumerSPI consumer)
   {
      super(consumer);
   }

   @Override
   protected ResourceResponse performRequest(GetResource getResource) throws Exception
   {
      // if we perform the request with this handler, that means that the invocation was dispatched this way
      // and the initiliazation of the request done in ResourceHandler.prepareRequest put the URL in the resource ID
      String resourceURL = getResource.getResourceParams().getResourceID();

      URL url = new URL(resourceURL);
      URLConnection urlConnection = url.openConnection();

      final String cookieValue = RequestHeaderClientHandler.createCoalescedCookieFromCurrentInfo();
      if (cookieValue.length() != 0)
      {
         urlConnection.addRequestProperty(CookieUtil.COOKIE, cookieValue);
      }

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
                  final List<HttpCookie> cookies = CookieUtil.extractCookiesFrom(url, values);
                  List<javax.servlet.http.Cookie> propCookies = props.getCookies();
                  for (HttpCookie cookie : cookies)
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

      ResourceContext resourceContext;
      // GTNCOMMON-14
      if (MIMEUtils.isInterpretableAsText(contentType))
      {
         // determine the charset of the content, defaulting to UTF-8 if there isn't one
         String charset = MIMEUtils.getCharsetFrom(contentType);
         String markup = new String(bytes, charset);
         resourceContext = WSRPTypeFactory.createResourceContext(contentType, markup, null);

         // process markup if needed
         if (MIMEUtils.needsRewriting(contentType))
         {
            resourceContext.setRequiresRewriting(true);
         }
      }
      else
      {
         resourceContext = WSRPTypeFactory.createResourceContext(contentType, null, bytes);
         resourceContext.setRequiresRewriting(false);
      }


      return WSRPTypeFactory.createResourceResponse(resourceContext);
   }
}
