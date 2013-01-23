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

import junit.framework.TestCase;
import org.gatein.wsrp.consumer.handlers.ProducerSessionInformation;
import org.gatein.wsrp.test.handler.MockSOAPMessage;
import org.gatein.wsrp.test.handler.MockSOAPMessageContext;

import javax.xml.soap.MimeHeaders;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.gatein.wsrp.test.support.CookieSupport.*;

/**
 * @author <a href="mailto:chris.laprun@jboss.com?subject=org.gatein.wsrp.handler.RequestHeaderClientHandlerTestCase">Chris
 *         Laprun</a>
 * @version $Revision: 10388 $
 * @since 2.4
 */
public class RequestHeaderClientHandlerTestCase extends TestCase
{
   RequestHeaderClientHandler handler;


   protected void setUp() throws Exception
   {
      handler = new RequestHeaderClientHandler();
      RequestHeaderClientHandler.resetCurrentInfo();
   }

   public void testCreateCookieEmptySessionInformation()
   {
      ProducerSessionInformation info = new ProducerSessionInformation();

      String cookie = RequestHeaderClientHandler.createCoalescedCookieFromCurrentInfo();
      assertTrue(cookie.isEmpty());
   }

   public void testCreateCookieMultipleUserAndGroup()
   {
      ProducerSessionInformation info = new ProducerSessionInformation();
      info.setPerGroupCookies(true);
      final String groupId = "group";
      info.setGroupCookiesFor(groupId, createCookies(createCookie("groupname", "groupvalue", 1), createCookie("groupname2", "groupvalue2", 1)));
      info.setUserCookies(createCookies(createCookie("username", "uservalue", 1), createCookie("username2", "uservalue2", 1)));
      RequestHeaderClientHandler.setCurrentInfo(groupId, info);

      String cookie = RequestHeaderClientHandler.createCoalescedCookieFromCurrentInfo();
      assertEquals("groupname=groupvalue,groupname2=groupvalue2,username=uservalue,username2=uservalue2", cookie);
   }

   public void testSimpleCookieHandleRequest()
   {
      MockSOAPMessage message = new MockSOAPMessage();
      SOAPMessageContext msgContext = MockSOAPMessageContext.createMessageContext(message, getClass().getClassLoader());

      handler.handleRequest(msgContext);
      checkCookies(msgContext, 0, (String[])null);

      ProducerSessionInformation sessionInformation = new ProducerSessionInformation();
      sessionInformation.setUserCookies(createCookies(createCookie("name", "value", 1)));
      RequestHeaderClientHandler.setCurrentInfo(null, sessionInformation);
      handler.handleRequest(msgContext);

      checkCookies(msgContext, 1, "name=value");
   }

   public void testGroupCookieHandleRequest()
   {
      MockSOAPMessage message = new MockSOAPMessage();
      SOAPMessageContext msgContext = MockSOAPMessageContext.createMessageContext(message, getClass().getClassLoader());

      ProducerSessionInformation info = new ProducerSessionInformation();
      info.setPerGroupCookies(true);
      String groupId = "group";
      info.setGroupCookiesFor(groupId, createCookies(createCookie("name", "value", 1)));
      RequestHeaderClientHandler.setCurrentInfo(null, info);

      try
      {
         handler.handleRequest(msgContext);
         fail("group id hasn't been set so shouldn't be able to complete request");
      }
      catch (IllegalStateException e)
      {
         // expected
      }

      RequestHeaderClientHandler.setCurrentGroupId(groupId);

      handler.handleRequest(msgContext);

      checkCookies(msgContext, 1, "name=value");
   }

   public void testBothCookiesHandleRequest()
   {
      MockSOAPMessage message = new MockSOAPMessage();
      SOAPMessageContext msgContext = MockSOAPMessageContext.createMessageContext(message, getClass().getClassLoader());

      ProducerSessionInformation info = new ProducerSessionInformation();
      info.setPerGroupCookies(true);
      String groupId = "group";
      info.setGroupCookiesFor(groupId, createCookies(createCookie("name", "value", 1)));
      info.setUserCookies(createCookies(createCookie("usercookie", "uservalue", 1)));
      RequestHeaderClientHandler.setCurrentInfo(groupId, info);


      handler.handleRequest(msgContext);

      checkCookies(msgContext, 2, "name=value", "usercookie=uservalue");
   }

   public void testCookieWithoutInitHandleResponse()
   {
      MockSOAPMessage message = new MockSOAPMessage();
      SOAPMessageContext msgContext = MockSOAPMessageContext.createMessageContext(message, getClass().getClassLoader());
      setCookies(msgContext, "name=value");

      handler.handleResponse(msgContext);
      ProducerSessionInformation info = RequestHeaderClientHandler.getCurrentProducerSessionInformation();
      assertEquals("name=value", CookieUtil.coalesceAndExternalizeCookies(info.getUserCookies()));
      assertFalse(info.isInitCookieDone());
      assertFalse(info.isPerGroupCookies());
   }

   public void testMultipleCookiesInResponse()
   {
      MockSOAPMessage message = new MockSOAPMessage();
      SOAPMessageContext msgContext = MockSOAPMessageContext.createMessageContext(message, getClass().getClassLoader());
      setCookies(msgContext, "name1=value1", "name2=value2", "name3=value3");

      handler.handleResponse(msgContext);
      ProducerSessionInformation info = RequestHeaderClientHandler.getCurrentProducerSessionInformation();
      assertEquals("name1=value1,name2=value2,name3=value3", CookieUtil.coalesceAndExternalizeCookies(info.getUserCookies()));
   }

   public void testCurrentInfo()
   {
      assertNull(RequestHeaderClientHandler.getCurrentProducerSessionInformation());
      assertNull(RequestHeaderClientHandler.getCurrentGroupId());

      try
      {
         RequestHeaderClientHandler.setCurrentGroupId("foo");
         fail("Current info was not set, shouldn't have thrown an IllegalStateException");
      }
      catch (IllegalStateException e)
      {
         // expected
      }

      ProducerSessionInformation info = new ProducerSessionInformation();
      String groupId = "group";
      RequestHeaderClientHandler.setCurrentInfo(groupId, info);

      assertSame(info, RequestHeaderClientHandler.getCurrentProducerSessionInformation());
      assertEquals(groupId, RequestHeaderClientHandler.getCurrentGroupId());
   }

   private void setCookies(SOAPMessageContext context, String... values)
   {
      Map<String, List<String>> httpHeaders = new HashMap<String, List<String>>();
      final List<String> cookies = new ArrayList<String>(values.length);

      for (String value : values)
      {
         context.getMessage().getMimeHeaders().addHeader(CookieUtil.SET_COOKIE, value);
         cookies.add(value);
      }

      httpHeaders.put(CookieUtil.COOKIE, cookies);
      context.put(MessageContext.HTTP_REQUEST_HEADERS, httpHeaders);
   }

   private void checkCookies(SOAPMessageContext messageContext, int number, String... values)
   {
      final MimeHeaders mimeHeaders = messageContext.getMessage().getMimeHeaders();
      final String[] cookies = mimeHeaders.getHeader(CookieUtil.COOKIE);

      @SuppressWarnings("unchecked")
      Map<String, List<String>> httpHeaders = (Map<String, List<String>>)messageContext.get(MessageContext.HTTP_REQUEST_HEADERS);
      List<String> cookiesList = httpHeaders.get(CookieUtil.COOKIE);

      if (number == 0)
      {
         assertNull(cookies);
         assertNull(cookiesList);
      }
      else
      {
         assertEquals(number, cookies.length);
         assertEquals(number, cookiesList.size());
         int i = 0;
         for (String value : values)
         {
            assertEquals(value, cookies[i]);
            assertEquals(value, cookiesList.get(i));
            i++;
         }
      }
   }
}
