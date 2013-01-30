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

package org.gatein.wsrp.handler;

import com.google.common.base.Function;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.cookie.MalformedCookieException;
import org.apache.commons.httpclient.cookie.RFC2109Spec;
import org.gatein.common.util.ParameterValidation;
import org.gatein.wsrp.WSRPUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class CookieUtil
{
   private static final RFC2109Spec cookieParser = new RFC2109Spec();
   private static final Logger log = LoggerFactory.getLogger(CookieUtil.class);
   public static final String SET_COOKIE = "Set-Cookie";
   public static final String COOKIE = "Cookie";
   private static final String EMPTY = "";
   public static final Function<Cookie, String> COOKIE_STRING_FUNCTION = new Function<Cookie, String>()
   {
      @Override
      public String apply(@Nullable Cookie input)
      {
         return input != null ? input.toExternalForm() : EMPTY;
      }
   };

   public static List<Cookie> extractCookiesFrom(URL remoteAddress, List<String> cookieValues)
   {
      List<Cookie> cookies = new ArrayList<Cookie>(cookieValues.size());
      for (String cookieValue : cookieValues)
      {
         cookies.addAll(Arrays.asList(extractCookies(remoteAddress, cookieValue)));
      }
      return cookies;
   }

   /**
    * Create a String representation of the specified array of Cookies.
    *
    * @param cookies the cookies to be output to external form
    * @return a String representation of the cookies, ready to be sent over the wire.
    */
   public static String coalesceAndExternalizeCookies(List<Cookie> cookies)
   {
      if (ParameterValidation.existsAndIsNotEmpty(cookies))
      {
         return coalesceCookies(asExternalFormList(cookies));
      }
      return EMPTY;
   }

   public static List<String> asExternalFormList(List<Cookie> cookies)
   {
      if(ParameterValidation.existsAndIsNotEmpty(cookies))
      {
         return WSRPUtils.transform(cookies, COOKIE_STRING_FUNCTION);
      }

      return Collections.emptyList();
   }

   /**
    * Coalesce several externalized Cookies into one and returning the resulting concatenated String.
    *
    * @param cookies the array containing the values of the different externalized Cookies to be coalesced
    * @return the concatenated value that could be used as one externalized Cookie or an empty String
    */
   public static String coalesceCookies(List<String> cookies)
   {
      if (ParameterValidation.existsAndIsNotEmpty(cookies))
      {
         int cookieNumber = cookies.size(), i = 0;

         StringBuffer logBuffer = null;
         if (log.isDebugEnabled())
         {
            logBuffer = new StringBuffer(128);
            logBuffer.append("Cookie headers:\n");
         }

         StringBuffer cookieBuffer = new StringBuffer(128 * cookieNumber);
         for (String cookie : cookies)
         {
            cookieBuffer.append(cookie);
            if (i++ != cookieNumber - 1)
            {
               cookieBuffer.append(","); // multiple cookies are separated by commas: http://www.ietf.org/rfc/rfc2109.txt, 4.2.2
            }

            if (log.isDebugEnabled())
            {
               logBuffer.append("\t").append(i).append(":\t").append(cookie).append("\n");
            }
         }

         if (log.isDebugEnabled())
         {
            log.debug(logBuffer.toString());
         }
         return cookieBuffer.toString();
      }

      return EMPTY;
   }

   private static Cookie[] extractCookies(URL hostURL, String cookieValue)
   {
      Cookie[] cookies;
      try
      {
         String host = hostURL.getHost();
         int port = hostURL.getPort();
         if (port == -1)
         {
            port = 80; // if the port is not set in the endpoint address, assume it's 80.
         }
         String path = hostURL.getPath();
         boolean secure = hostURL.getProtocol().endsWith("s"); // todo: is that correct?

         cookies = cookieParser.parse(host, port, path, secure, cookieValue);

         for (Cookie cookie : cookies)
         {
            cookieParser.validate(host, port, path, secure, cookie);
         }
      }
      catch (MalformedCookieException e)
      {
         throw new IllegalArgumentException("Malformed cookie: " + cookieValue);
      }
      return cookies;
   }

   public static javax.servlet.http.Cookie convertFrom(Cookie cookie)
   {
      if (cookie == null)
      {
         return null;
      }


      javax.servlet.http.Cookie result = new javax.servlet.http.Cookie(cookie.getName(), cookie.getValue());
      result.setComment(cookie.getComment());
      result.setDomain(cookie.getDomain());

      Date expiryDate = cookie.getExpiryDate();
      int maxAge;
      if (expiryDate != null)
      {
         long maxAgeLong = expiryDate.getTime() - new Date().getTime();
         if (maxAgeLong >= Integer.MAX_VALUE)
         {
            maxAge = Integer.MAX_VALUE;
         }
         else
         {
            maxAge = (int)maxAgeLong;
         }
      }
      else
      {
         maxAge = -1; // to specify that cookie should not be persisted but removed with the session
      }
      result.setMaxAge(maxAge);

      result.setPath(cookie.getPath());
      result.setSecure(cookie.getSecure());
      result.setVersion(cookie.getVersion());

      return result;
   }

   /**
    * Purges the expired cookies in the specified array.
    *
    * @param cookies the cookies to be purged
    * @return an array of Cookies containing only still valid cookies
    */
   public static List<Cookie> purgeExpiredCookies(List<Cookie> cookies)
   {
      if (!ParameterValidation.existsAndIsNotEmpty(cookies))
      {
         return Collections.emptyList();
      }

      List<Cookie> cleanCookies = new ArrayList<Cookie>(cookies);

      for (Cookie cookie : cookies)
      {
         if (cookie.isExpired())
         {
            cleanCookies.remove(cookie);
         }
      }
      return cleanCookies;
   }
}
