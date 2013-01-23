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

import junit.framework.TestCase;
import org.apache.commons.httpclient.Cookie;
import org.gatein.wsrp.WSRPConstants;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.handler.CookieUtil;

import java.util.List;

import static org.gatein.wsrp.test.support.CookieSupport.createCookie;
import static org.gatein.wsrp.test.support.CookieSupport.createCookies;

/**
 * @author <a href="mailto:chris.laprun@jboss.com?subject=org.gatein.wsrp.consumer.handlers.ProducerSessionInformationTestCase">Chris
 *         Laprun</a>
 * @version $Revision: 10388 $
 * @since 2.4
 */
public class ProducerSessionInformationTestCase extends TestCase
{
   ProducerSessionInformation info;
   private static final int SLEEP_TIME = 1500;

   protected void setUp() throws Exception
   {
      info = new ProducerSessionInformation();
   }

   public void testUserCookie() throws Exception
   {
      assertTrue(info.getUserCookies().isEmpty());

      List<Cookie> cookies = createCookies(createCookie("name", "value", 1));
      info.setUserCookies(cookies);

      assertEquals("name=value", CookieUtil.coalesceAndExternalizeCookies(info.getUserCookies()));

      // wait for cookie expiration
      Thread.sleep(SLEEP_TIME);
      assertTrue(info.getUserCookies().isEmpty()); // we shouldn't have a cookie now

      cookies = createCookies(createCookie("name1", "value1", 1), createCookie("name2", "value2", 3));
      info.setUserCookies(cookies);
      assertEquals("name1=value1,name2=value2", CookieUtil.coalesceAndExternalizeCookies(info.getUserCookies()));

      Thread.sleep(SLEEP_TIME);
      assertEquals("name2=value2", CookieUtil.coalesceAndExternalizeCookies(info.getUserCookies()));

      try
      {
         info.setUserCookies(null);
         fail("Should have thrown an IllegalArgumentException");
      }
      catch (IllegalArgumentException e)
      {
         //expected
      }
   }

   public void testGroupCookies() throws Exception
   {
      String groupId = "groupId";

      try
      {
         info.setGroupCookiesFor(groupId, createCookies(createCookie("name1", "value1", 1), createCookie("name2", "value2", -1)));
         fail("Cannot add group cookie if not perGroup");
      }
      catch (IllegalStateException e)
      {
         //expected
      }

      info.setPerGroupCookies(true);
      info.setGroupCookiesFor(groupId, createCookies(createCookie("name1", "value1", 1), createCookie("name2", "value2", WSRPConstants.SESSION_NEVER_EXPIRES)));

      assertEquals("name1=value1,name2=value2", CookieUtil.coalesceAndExternalizeCookies(info.getGroupCookiesFor(groupId)));

      Thread.sleep(SLEEP_TIME);
      assertEquals("name2=value2", CookieUtil.coalesceAndExternalizeCookies(info.getGroupCookiesFor(groupId)));

      info.clearGroupCookies();
      assertTrue(info.getGroupCookiesFor(groupId).isEmpty());
   }

   public void testSessionForPortlet() throws Exception
   {
      String handle = "handle";
      String handle2 = "handle2";
      String sid = "id";
      String sid2 = "id2";

      assertNull(info.getSessionIdForPortlet(handle));
      assertEquals(0, info.getNumberOfSessions());

      addSession(handle, sid, 1);
      addSession(handle2, sid2, 3);

      assertNull(info.getSessionIdForPortlet("unknown"));

      assertEquals(sid, info.getSessionIdForPortlet(handle));
      assertEquals(2, info.getNumberOfSessions());

      Thread.sleep(SLEEP_TIME);
      assertNull(info.getSessionIdForPortlet(handle));
      assertEquals(sid2, info.getSessionIdForPortlet(handle2));
      assertEquals(1, info.getNumberOfSessions());

      info.removeSessionForPortlet(handle2);
      assertEquals(0, info.getNumberOfSessions());
   }

   public void testReplaceUserCookies() throws Exception
   {
      info.setUserCookies(createCookies(createCookie("name", "value", 1)));

      info.replaceUserCookiesWith(null);
      assertEquals("name=value", CookieUtil.coalesceAndExternalizeCookies(info.getUserCookies()));

      ProducerSessionInformation other = new ProducerSessionInformation();

      info.replaceUserCookiesWith(other);
      assertEquals("name=value", CookieUtil.coalesceAndExternalizeCookies(info.getUserCookies()));

      other.setUserCookies(createCookies(createCookie("name2", "value2", 1)));
      info.replaceUserCookiesWith(other);
      assertEquals("name2=value2", CookieUtil.coalesceAndExternalizeCookies(info.getUserCookies()));

      Thread.sleep(SLEEP_TIME);
      info.replaceUserCookiesWith(other);
      assertTrue(info.getUserCookies().isEmpty());
   }

   public void testReleaseSessions()
   {
      addSession("handle", "id", 1);
      addSession("handle2", "id2", 1);
      addSession("handle3", "id3", 1);

      assertEquals(3, info.getNumberOfSessions());

      info.removeSessions();

      assertEquals(0, info.getNumberOfSessions());

      addSession("handle", "id", 1);
      addSession("handle2", "id2", 2);

      info.removeSession("id2");

      assertEquals(1, info.getNumberOfSessions());
      assertNull(info.getSessionIdForPortlet("handle2"));
      assertEquals("id", info.getSessionIdForPortlet("handle"));

      info.removeSessionForPortlet("handle");

      assertEquals(0, info.getNumberOfSessions());
      assertNull(info.getSessionIdForPortlet("handle"));

      try
      {
         info.removeSessionForPortlet("handle");
         fail("Session for portlet 'handle' should have already been released!");
      }
      catch (IllegalArgumentException expected)
      {
         // expected
      }
   }

   public void testSetParentSessionId()
   {
      assertNull(info.getParentSessionId());

      String id = "session";
      info.setParentSessionId(id);
      assertEquals(id, info.getParentSessionId());

      // trying to set the same id should work
      info.setParentSessionId(id);

      try
      {
         info.setParentSessionId("other");
         fail("Cannot modify session id once it has been set");
      }
      catch (IllegalStateException expected)
      {
         // expected
      }
   }

   private void addSession(String handle, String sid, int expires)
   {
      info.addSessionForPortlet(handle, WSRPTypeFactory.createSessionContext(sid, expires));
   }
}
