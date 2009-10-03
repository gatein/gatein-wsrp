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

package org.gatein.wsrp.other;

import junit.framework.TestCase;
import org.gatein.wsrp.admin.ui.ConsumerBean;

/**
 * TODO: re-activate tests once test-support module is updated.
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 12612 $
 * @since 2.6
 */
public class ConsumerBeanTestCase extends TestCase
{
   private ConsumerBean bean;

   protected void setUp() throws Exception
   {
      bean = new ConsumerBean();
//      bean.setRegistry(new MockConsumerRegistry());

      // consumer associated with bean is null at this point so it should be loaded from the registry
//      bean.setId(MockConsumerRegistry.CONSUMER2);
   }

   // todo: remove when tests are re-activated
   public void testNothing()
   {
      assertTrue(true);
   }

   /*public void testInitialState()
   {
//      assertEquals(MockConsumerRegistry.CONSUMER2, bean.getId());
//      assertEquals(MockConsumerRegistry.MOCK_MARKUP, bean.getMarkup());
//      assertEquals(MockConsumerRegistry.MOCK_SERVICE_DESCRIPTION, bean.getServiceDescription());
      assertFalse(bean.isModified());
   }

  public void testSetId()
   {
      String newId = "newId";
      bean.setId(newId);
      assertEquals(newId, bean.getId());
      assertTrue(bean.isModified());
   }

   public void testSetCache()
   {
      bean.setCache(300);
      assertEquals(300, bean.getCache().intValue());
      assertTrue(bean.isModified());
   }

   private static class TestBeanContext extends BeanContext
   {
      public String getParameter(String key)
      {
         throw new NotYetImplemented();
      }

      protected void createMessage(String target, String message, Object severity)
      {
         // ignore for tests
      }

      protected Object getErrorSeverity()
      {
         return null;
      }

      protected Object getInfoSeverity()
      {
         return null;
      }

      protected Locale getLocale()
      {
         return Locale.getDefault();
      }

      public Map<String, Object> getSessionMap()
      {
         throw new NotYetImplemented();
      }
   }*/
}
