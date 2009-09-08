/*
 * JBoss, a division of Red Hat
 * Copyright 2009, Red Hat Middleware, LLC, and individual
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

import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.cookie.MalformedCookieException;
import org.apache.commons.httpclient.cookie.RFC2109Spec;
import org.gatein.wsrp.consumer.ProducerSessionInformation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

/**
 * A request handler that uses a thread local to setup cookies on the wire.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @author <a href="mailto:chris.laprun@jboss.com?subject=org.jboss.portal.wsrp.handler.RequestHeaderClientHandler">Chris
 *         Laprun</a>
 */
public class RequestHeaderClientHandler implements SOAPHandler<SOAPMessageContext>
{
   private static final ThreadLocal local = new ThreadLocal();
   private static final RFC2109Spec cookieParser = new RFC2109Spec();
   private static final Logger log = LoggerFactory.getLogger(RequestHeaderClientHandler.class);

   public Set<QName> getHeaders()
   {
      return null;
   }

   public boolean handleMessage(SOAPMessageContext soapMessageContext)
   {
      // outbound message means request
      if (Boolean.TRUE.equals(soapMessageContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)))
      {
         return handleRequest(soapMessageContext);
      }
      else
      {
         return handleResponse(soapMessageContext);
      }
   }

   public boolean handleFault(SOAPMessageContext soapMessageContext)
   {
      return true;
   }

   public void close(MessageContext messageContext)
   {
      // nothing to do
   }

   public boolean handleRequest(SOAPMessageContext msgContext)
   {
      CurrentInfo info = getCurrentInfo(false);
      if (info == null)
      {
         return true;
      }

      ProducerSessionInformation sessionInfo = info.sessionInfo;
      if (sessionInfo == null)
      {
         return true;
      }

      SOAPMessage message = msgContext.getMessage();
      MimeHeaders mimeHeaders = message.getMimeHeaders();
      StringBuffer cookie = new StringBuffer(64);
      if (sessionInfo.isPerGroupCookies())
      {
         if (info.groupId == null)
         {
            throw new IllegalStateException("Was expecting a current group Id...");
         }

         String groupCookie = sessionInfo.getGroupCookieFor(info.groupId);
         if (groupCookie != null)
         {
            cookie.append(groupCookie);
         }
      }

      String userCookie = sessionInfo.getUserCookie();
      if (userCookie != null)
      {
         if (cookie.length() != 0)
         {
            cookie.append(','); // multiple cookies are separated by commas: http://www.ietf.org/rfc/rfc2109.txt, 4.2.2
         }
         cookie.append(userCookie);
      }

      if (cookie.length() != 0)
      {
         mimeHeaders.setHeader("Cookie", cookie.toString());
      }

      return true;
   }

   public boolean handleResponse(MessageContext msgContext)
   {
      SOAPMessageContext smc = (SOAPMessageContext)msgContext;
      SOAPMessage message = smc.getMessage();
      MimeHeaders mimeHeaders = message.getMimeHeaders();
      String[] cookieValues = mimeHeaders.getHeader("Set-Cookie");

      if (cookieValues != null)
      {
         String cookieValue = coalesceCookies(cookieValues);

         Cookie[] cookies = extractCookies((String)msgContext.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY), cookieValue);

         CurrentInfo info = getCurrentInfo(true);
         ProducerSessionInformation sessionInfo = info.sessionInfo;

         if (sessionInfo.isPerGroupCookies())
         {
            if (info.groupId == null)
            {
               throw new IllegalStateException("Was expecting a current group Id...");
            }

            sessionInfo.setGroupCookieFor(info.groupId, cookies);
         }
         else
         {
            sessionInfo.setUserCookie(cookies);
         }
      }

      return true;
   }

   /**
    * Coalesce several Set-Cookie headers into one and returning the resulting concatenated String.
    *
    * @param cookieValues the array containing the values of the different Set-Cookie headers to be coalesced
    * @return the concatenated value that could be used as one Set-Cookie header
    */
   private String coalesceCookies(String[] cookieValues)
   {
      assert cookieValues != null;

      StringBuffer logBuffer = null;
      if (log.isDebugEnabled())
      {
         logBuffer = new StringBuffer(128);
         logBuffer.append("Cookie headers:\n");
      }

      int cookieNumber = cookieValues.length;
      StringBuffer cookieBuffer = new StringBuffer(cookieNumber * 128);
      String cookieValue;
      for (int i = 0; i < cookieNumber; i++)
      {
         cookieValue = cookieValues[i];
         cookieBuffer.append(cookieValue);

         // multiple cookies are separated by commas: http://www.ietf.org/rfc/rfc2109.txt, 4.2.2
         if (i < cookieNumber - 1)
         {
            cookieBuffer.append(',');
         }

         if (log.isDebugEnabled())
         {
            logBuffer.append("\t").append(i).append(":\t").append(cookieValue).append("\n");
         }
      }

      if (log.isDebugEnabled())
      {
         log.debug(logBuffer.toString());
      }

      return cookieBuffer.toString();
   }

   private Cookie[] extractCookies(String endpointAddress, String cookieValue)
   {
      if (endpointAddress == null)
      {
         throw new NullPointerException("Was expecting an endpoint address but none was provided in the MessageContext");
      }

      URL hostURL;
      try
      {
         hostURL = new URL(endpointAddress);
      }
      catch (MalformedURLException e)
      {
         // should not happen
         throw new IllegalArgumentException(endpointAddress + " is not a valid URL for the endpoint address.");
      }

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

   public static void setCurrentInfo(String groupId, ProducerSessionInformation sessionInformation)
   {
      local.set(new CurrentInfo(groupId, sessionInformation));
   }

   public static void resetCurrentInfo()
   {
      local.set(null);
   }

   public static ProducerSessionInformation getCurrentProducerSessionInformation()
   {
      CurrentInfo info = getCurrentInfo(false);

      if (info != null)
      {
         return info.sessionInfo;
      }

      return null;
   }

   public static String getCurrentGroupId()
   {
      CurrentInfo info = getCurrentInfo(false);
      if (info != null)
      {
         return info.groupId;
      }
      return null;
   }

   public static void setCurrentGroupId(String groupId)
   {
      CurrentInfo currentInfo = (CurrentInfo)local.get();
      if (currentInfo == null)
      {
         throw new IllegalStateException("Cannot set current group id when the current info hasn't been initialized.");
      }
      currentInfo.groupId = groupId;
   }

   private static CurrentInfo getCurrentInfo(boolean createIfNeeded)
   {
      CurrentInfo info = (CurrentInfo)local.get();
      if (info == null && createIfNeeded)
      {
         info = new CurrentInfo(null, new ProducerSessionInformation());
         local.set(info);
      }
      return info;
   }

   static class CurrentInfo
   {
      public CurrentInfo(String groupId, ProducerSessionInformation sessionInfo)
      {
         this.groupId = groupId;
         this.sessionInfo = sessionInfo;
      }

      String groupId;
      ProducerSessionInformation sessionInfo;
   }
}
