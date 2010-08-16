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
import org.apache.commons.httpclient.Cookie;
import org.gatein.wsrp.consumer.handlers.ProducerSessionInformation;
import org.gatein.wsrp.test.handler.MockSOAPMessage;
import org.gatein.wsrp.test.handler.MockSOAPMessageContext;

import javax.xml.soap.MimeHeaders;
import javax.xml.ws.handler.soap.SOAPMessageContext;

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

   public void testSimpleCookieHandleRequest()
   {
      MockSOAPMessage message = new MockSOAPMessage();
      SOAPMessageContext msgContext = MockSOAPMessageContext.createMessageContext(message, getClass().getClassLoader());

      handler.handleRequest(msgContext);
      MimeHeaders headers = message.getMimeHeaders();
      assertNull(headers.getHeader("Cookie"));

      ProducerSessionInformation sessionInformation = new ProducerSessionInformation();
      sessionInformation.setUserCookie(new Cookie[]{createCookie("name", "value", 1)});
      RequestHeaderClientHandler.setCurrentInfo(null, sessionInformation);
      handler.handleRequest(msgContext);

      headers = message.getMimeHeaders();
      String[] cookie = headers.getHeader("Cookie");
      assertEquals(1, cookie.length);
      assertEquals("name=value", cookie[0]);
   }

   public void testGroupCookieHandleRequest()
   {
      MockSOAPMessage message = new MockSOAPMessage();
      SOAPMessageContext msgContext = MockSOAPMessageContext.createMessageContext(message, getClass().getClassLoader());

      ProducerSessionInformation info = new ProducerSessionInformation();
      info.setPerGroupCookies(true);
      String groupId = "group";
      info.setGroupCookieFor(groupId, new Cookie[]{createCookie("name", "value", 1)});
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

      MimeHeaders headers = message.getMimeHeaders();
      String[] cookie = headers.getHeader("Cookie");
      assertEquals(1, cookie.length);
      assertEquals("name=value", cookie[0]);
   }

   public void testBothCookiesHandleRequest()
   {
      MockSOAPMessage message = new MockSOAPMessage();
      SOAPMessageContext msgContext = MockSOAPMessageContext.createMessageContext(message, getClass().getClassLoader());

      ProducerSessionInformation info = new ProducerSessionInformation();
      info.setPerGroupCookies(true);
      String groupId = "group";
      info.setGroupCookieFor(groupId, new Cookie[]{createCookie("name", "value", 1)});
      info.setUserCookie(new Cookie[]{createCookie("usercookie", "uservalue", 1)});
      RequestHeaderClientHandler.setCurrentInfo(groupId, info);


      handler.handleRequest(msgContext);
      MimeHeaders headers = message.getMimeHeaders();
      String[] cookie = headers.getHeader("Cookie");
      assertEquals(1, cookie.length);
      assertEquals("name=value,usercookie=uservalue", cookie[0]);
   }

   public void testCookieWithoutInitHandleResponse()
   {
      MockSOAPMessage message = new MockSOAPMessage();
      SOAPMessageContext msgContext = MockSOAPMessageContext.createMessageContext(message, getClass().getClassLoader());
      MimeHeaders headers = new MimeHeaders();
      headers.setHeader("Set-Cookie", "name=value");
      message.setMimeHeaders(headers);

      handler.handleResponse(msgContext);
      ProducerSessionInformation info = RequestHeaderClientHandler.getCurrentProducerSessionInformation();
      assertEquals("name=value", info.getUserCookie());
      assertFalse(info.isInitCookieDone());
      assertFalse(info.isPerGroupCookies());
   }

   public void testMultipleCookiesInResponse()
   {
      MockSOAPMessage message = new MockSOAPMessage();
      SOAPMessageContext msgContext = MockSOAPMessageContext.createMessageContext(message, getClass().getClassLoader());
      MimeHeaders headers = new MimeHeaders();
      headers.addHeader("Set-Cookie", "name1=value1");
      headers.addHeader("Set-Cookie", "name2=value2");
      headers.addHeader("Set-Cookie", "name3=value3");
      message.setMimeHeaders(headers);

      handler.handleResponse(msgContext);
      ProducerSessionInformation info = RequestHeaderClientHandler.getCurrentProducerSessionInformation();
      assertEquals("name1=value1,name2=value2,name3=value3", info.getUserCookie());
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

   private Cookie createCookie(String name, String value, int secondsBeforeExpiration)
   {
      return new Cookie("domain", name, value, "path", secondsBeforeExpiration, false);
   }
}
