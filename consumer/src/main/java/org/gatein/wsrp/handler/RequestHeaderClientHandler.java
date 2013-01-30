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

import org.apache.commons.httpclient.Cookie;
import org.gatein.wsrp.consumer.handlers.ProducerSessionInformation;

import javax.xml.namespace.QName;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
   private static final ThreadLocal<CurrentInfo> local = new ThreadLocal<CurrentInfo>();
   private static final String EMPTY = "";

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
      final List<String> cookies = CookieUtil.asExternalFormList(getCookiesFromCurrentInfo());
      if(cookies.isEmpty())
      {
         return true;
      }
      else
      {
         SOAPMessage message = msgContext.getMessage();

         // Legacy JBossWS native approach
         final MimeHeaders mimeHeaders = message.getMimeHeaders();
         // proper approach through MessageContext.HTTP_REQUEST_HEADERS
         List<String> cookieHeaders = getCookieHeaders(msgContext);

         for (String cookie : cookies)
         {
            mimeHeaders.addHeader(CookieUtil.COOKIE, cookie);
            cookieHeaders.add(cookie);
         }

         return true;
      }
   }

   private List<String> getCookieHeaders(SOAPMessageContext msgContext)
   {
      Map<String, List<String>> httpHeaders = (Map<String, List<String>>)msgContext.get(MessageContext.HTTP_REQUEST_HEADERS);
      if (httpHeaders == null)
      {
         httpHeaders = new HashMap<String, List<String>>();
         msgContext.put(MessageContext.HTTP_REQUEST_HEADERS, httpHeaders);
      }
      List<String> cookieHeaders = httpHeaders.get(CookieUtil.COOKIE);
      if (cookieHeaders == null)
      {
         cookieHeaders = new LinkedList<String>();
         httpHeaders.put(CookieUtil.COOKIE, cookieHeaders);
      }
      return cookieHeaders;
   }


   public static String createCoalescedCookieFromCurrentInfo()
   {
      return CookieUtil.coalesceAndExternalizeCookies(getCookiesFromCurrentInfo());
   }

   private static List<Cookie> getCookiesFromCurrentInfo()
   {
      CurrentInfo info = getCurrentInfo(false);
      if (info != null)
      {

         final ProducerSessionInformation sessionInfo = info.sessionInfo;
         if (sessionInfo == null)
         {
            return Collections.emptyList();
         }

         final List<Cookie> cookies = new ArrayList<Cookie>(7);
         if (sessionInfo.isPerGroupCookies())
         {
            if (info.groupId == null)
            {
               throw new IllegalStateException("Was expecting a current group Id...");
            }

            cookies.addAll(sessionInfo.getGroupCookiesFor(info.groupId));
         }

         cookies.addAll(sessionInfo.getUserCookies());

         return cookies;
      }

      return Collections.emptyList();
   }

   public boolean handleResponse(MessageContext msgContext)
   {
      SOAPMessageContext smc = (SOAPMessageContext)msgContext;
      SOAPMessage message = smc.getMessage();

      // proper approach through MessageContext.HTTP_RESPONSE_HEADERS
      @SuppressWarnings("unchecked")
      Map<String, List<String>> httpHeaders = (Map<String, List<String>>)smc.get(MessageContext.HTTP_RESPONSE_HEADERS);
      List<String> cookieValues = httpHeaders.get(CookieUtil.SET_COOKIE);
      if (cookieValues == null)
      {
         // try the legacy JBossWS native approach
         MimeHeaders mimeHeaders = message.getMimeHeaders();
         final String[] cookieHeaders = mimeHeaders.getHeader(CookieUtil.SET_COOKIE);
         if (cookieHeaders != null)
         {
            cookieValues = Arrays.asList(cookieHeaders);
         }
      }

      if (cookieValues != null)
      {
         String endpointAddress = (String)msgContext.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
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
         final List<Cookie> cookies = CookieUtil.extractCookiesFrom(hostURL, cookieValues);

         CurrentInfo info = getCurrentInfo(true);
         ProducerSessionInformation sessionInfo = info.sessionInfo;

         if (sessionInfo.isPerGroupCookies())
         {
            if (info.groupId == null)
            {
               throw new IllegalStateException("Was expecting a current group Id...");
            }

            sessionInfo.setGroupCookiesFor(info.groupId, cookies);
         }
         else
         {
            sessionInfo.setUserCookies(cookies);
         }
      }

      return true;
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
      CurrentInfo currentInfo = local.get();
      if (currentInfo == null)
      {
         throw new IllegalStateException("Cannot set current group id when the current info hasn't been initialized.");
      }
      currentInfo.groupId = groupId;
   }

   private static CurrentInfo getCurrentInfo(boolean createIfNeeded)
   {
      CurrentInfo info = local.get();
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
