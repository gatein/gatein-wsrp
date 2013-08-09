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

      String cookie = createCookie(info, sessionInfo);

      if (cookie.length() != 0)
      {
         mimeHeaders.setHeader(CookieUtil.COOKIE, cookie);
      }

      return true;
   }

   public static String createCookie(ProducerSessionInformation sessionInformation)
   {
      CurrentInfo currentInfo = getCurrentInfo(false);
      if (currentInfo != null)
      {
         return createCookie(currentInfo, sessionInformation);
      }
      else
      {
         return EMPTY;
      }
   }

   private static String createCookie(CurrentInfo info, ProducerSessionInformation sessionInfo)
   {
      StringBuilder cookie = new StringBuilder(128);
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
      return cookie.toString();
   }

   public boolean handleResponse(MessageContext msgContext)
   {
      SOAPMessageContext smc = (SOAPMessageContext)msgContext;
      SOAPMessage message = smc.getMessage();
      MimeHeaders mimeHeaders = message.getMimeHeaders();
      String[] cookieValues = mimeHeaders.getHeader(CookieUtil.SET_COOKIE);

      String endpointAddress = (String)msgContext.get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
      if (cookieValues != null)
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
         Cookie[] cookies = CookieUtil.extractCookiesFrom(hostURL, cookieValues);

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
      return getProducerSessionInformation(false);
   }

   public static ProducerSessionInformation getProducerSessionInformation(boolean createIfNeeded)
   {
      CurrentInfo info = getCurrentInfo(createIfNeeded);

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
