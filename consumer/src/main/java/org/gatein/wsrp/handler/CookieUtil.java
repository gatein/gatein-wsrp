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
import org.gatein.common.util.ParameterValidation;
import org.gatein.wsrp.WSRPUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.net.HttpCookie;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class CookieUtil
{
   private static final Logger log = LoggerFactory.getLogger(CookieUtil.class);
   public static final String SET_COOKIE = "Set-Cookie";
   public static final String COOKIE = "Cookie";
   private static final String EMPTY = "";
   private static final Function<Cookie, String> COOKIE_STRING_FUNCTION = new Function<Cookie, String>()
   {
      @Override
      public String apply(@Nullable Cookie input)
      {
         return input != null ? input.toString() : EMPTY;
      }
   };

   /**
    * Extract cookies from the specified list of String representation of cookies, validating them to check if they are valid for the domain associated with the specified URL.
    *
    * @param remoteAddress the address from which the cookies are supposed to be issued
    * @param cookieValues a list of String representation of cookies
    * @return a list of {@link Cookie} objects providing the cookie information extracted from the String representations
    */
   public static List<Cookie> extractCookiesFrom(URL remoteAddress, List<String> cookieValues)
   {
      List<Cookie> cookies = new ArrayList<Cookie>(cookieValues.size());
      for (String cookieValue : cookieValues)
      {
         cookies.addAll(extractCookies(remoteAddress, cookieValue));
      }
      return cookies;
   }

   /**
    * Coalesce the list of specified cookies into a single String representation suitable to be used as part of an HTTP response
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

   /**
    * Converts the specified internal Cookie representations into a list of String representations, ready to be sent over the wire.
    *
    * @param cookies the internal Cookies to be output to external form
    * @return a list of String representations of the input Cookies, one per Cookie
    */
   public static List<String> asExternalFormList(List<Cookie> cookies)
   {
      if (ParameterValidation.existsAndIsNotEmpty(cookies))
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

   private static List<Cookie> extractCookies(URL hostURL, String cookieValue)
   {
      List<HttpCookie> cookies;
      String host = hostURL.getHost();

      final long creationTime = System.currentTimeMillis();
      cookies = HttpCookie.parse(cookieValue);

      List<Cookie> result = new ArrayList<Cookie>(cookies.size());

      for (HttpCookie cookie : cookies)
      {
         final String domain = cookie.getDomain();
         if (domain != null && !HttpCookie.domainMatches(domain, host))
         {
            throw new IllegalArgumentException("Cookie '" + cookie + "' doesn't match host '" + host + "'");
         }

         result.add(new Cookie(cookie, creationTime));
      }

      return result;
   }

   /**
    * Converts the specified internal Cookie into a javax.servlet.http.Cookie object.
    *
    * @param cookie the internal Cookie to be converted
    * @return a javax.servlet.http.Cookie corresponding to the internal representation
    */
   public static javax.servlet.http.Cookie convertFrom(Cookie cookie)
   {
      if (cookie == null)
      {
         return null;
      }


      javax.servlet.http.Cookie result = new javax.servlet.http.Cookie(cookie.getName(), cookie.getValue());
      result.setComment(cookie.getComment());
      result.setDomain(cookie.getDomain());

      int maxAge;
      long maxAgeLong = cookie.getMaxAge();
      if (maxAgeLong >= Integer.MAX_VALUE)
      {
         maxAge = Integer.MAX_VALUE;
      }
      else
      {
         maxAge = (int)maxAgeLong;
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
         if (cookie.hasExpired())
         {
            cleanCookies.remove(cookie);
         }
      }
      return cleanCookies;
   }

   /**
    * An internal representation of Cookie metadata, able to be serialized across cluster nodes (javax.servlet.http.Cookie is not Serializable).
    */
   public static class Cookie implements Serializable
   {
      private final String externalForm;
      private final long maxAge;
      private final long creationTime;
      private final String name;
      private final String value;
      private final String comment;
      private final String domain;
      private final String path;
      private final boolean secure;
      private final int version;

      public Cookie(HttpCookie cookie, long creationTime)
      {
         this(cookie, cookie.getMaxAge(), creationTime);
      }

      public Cookie(String name, String value, int secondsBeforeExpiration)
      {
         this(new HttpCookie(name, value), secondsBeforeExpiration, System.currentTimeMillis());
      }

      private Cookie(HttpCookie cookie, long secondsBeforeExpiration, long creationTime)
      {
         externalForm = cookie.toString();
         maxAge = secondsBeforeExpiration;
         this.creationTime = creationTime;
         name = cookie.getName();
         value = cookie.getValue();
         comment = cookie.getComment();
         domain = cookie.getDomain();
         path = cookie.getPath();
         secure = cookie.getSecure();
         version = cookie.getVersion();
      }

      public boolean hasExpired()
      {
         if (maxAge == 0)
         {
            return true;
         }

         // as per HttpCookie.setMaxAge, negative value implies delete on exit but not expired
         if (maxAge < 0)
         {
            return false;
         }

         long timeSinceCreated = (System.currentTimeMillis() - creationTime) / 1000;
         return timeSinceCreated > maxAge;
      }

      @Override
      public String toString()
      {
         return externalForm;
      }

      public String getName()
      {
         return name;
      }

      public String getValue()
      {
         return value;
      }

      public String getComment()
      {
         return comment;
      }

      public String getDomain()
      {
         return domain;
      }

      public long getMaxAge()
      {
         return maxAge;
      }

      public String getPath()
      {
         return path;
      }

      public boolean getSecure()
      {
         return secure;
      }

      public int getVersion()
      {
         return version;
      }
   }
}
