/*
 * JBoss, a division of Red Hat
 * Copyright 2011, Red Hat Middleware, LLC, and individual
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

package org.gatein.wsrp.admin.ui;

import junit.framework.TestCase;
import org.gatein.common.NotYetImplemented;
import org.gatein.wsrp.WSRPConsumer;
import org.gatein.wsrp.consumer.registry.ConsumerRegistry;
import org.gatein.wsrp.consumer.registry.InMemoryConsumerRegistry;
import org.gatein.wsrp.services.SOAPServiceFactory;
import org.gatein.wsrp.test.protocol.v2.BehaviorBackedServiceFactory;
import org.gatein.wsrp.test.support.MockEndpointConfigurationInfo;

import javax.faces.model.DataModel;
import java.util.Locale;
import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 12612 $
 * @since 2.6
 */
public class ConsumerBeanTestCase extends TestCase
{
   private static final String CONSUMER_ID = "foo";

   /** Since our consumers use the MockEndpointConfigurationInfo, this is the WSDL they are configured with */
   private static final String WSDL = BehaviorBackedServiceFactory.DEFAULT_WSDL_URL;
   private ConsumerBean bean;

   protected void setUp() throws Exception
   {
      bean = new ConsumerBean();

      ConsumerRegistry registry = new TestInMemoryConsumerRegistry();
      registry.createConsumer(CONSUMER_ID, null, WSDL);
      ConsumerManagerBean managerBean = new ConsumerManagerBean();
      managerBean.setRegistry(registry);
      bean.setManager(managerBean);

      bean.setBeanContext(new TestBeanContext());

      // consumer associated with bean is null at this point so it should be loaded from the registry
      bean.setId(CONSUMER_ID);
   }

   public void testInitialState()
   {
      assertEquals(CONSUMER_ID, bean.getId());
      assertEquals(bean.getProducerInfo().getId(), bean.getId());

      assertEquals(WSDL, bean.getWsdl());
      assertEquals(SOAPServiceFactory.DEFAULT_TIMEOUT_MS, bean.getTimeout().intValue());

      assertFalse(bean.isModified());
      assertTrue(bean.isRefreshNeeded());

      assertFalse(bean.isActive());

      assertFalse(bean.isRegistrationChecked());
      assertTrue(bean.isRegistrationCheckNeeded());
      assertFalse(bean.isRegistered());
      assertFalse(bean.isRegistrationLocallyModified());
      assertFalse(bean.isRegistrationPropertiesExisting());

      assertNull(bean.getCurrentExport());

      DataModel existingExports = bean.getExistingExports();
      assertNotNull(existingExports);
      assertEquals(0, existingExports.getRowCount());

      try
      {
         assertFalse(bean.isRegistrationRequired());
         fail("Can't know if registration is required without a refresh");
      }
      catch (IllegalStateException e)
      {
         // expected
      }
      try
      {
         assertFalse(bean.isRegistrationValid());
         fail("Can't know if registration is valid without a refresh");
      }
      catch (Exception e)
      {
         // expected
      }
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

      protected void createMessage(String target, String message, Object severity, Object... addtionalParams)
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

      public String getServerAddress()
      {
         throw new NotYetImplemented();
      }

      public Map<String, Object> getSessionMap()
      {
         throw new NotYetImplemented();
      }

      @Override
      public <T> T findBean(String name, Class<T> type)
      {
         throw new NotYetImplemented();
      }
   }

   private static class TestInMemoryConsumerRegistry extends InMemoryConsumerRegistry
   {
      @Override
      public WSRPConsumer createConsumer(String id, Integer expirationCacheSeconds, String wsdlURL)
      {
         // Use a "real" consumer but with a fake endpoint configuration so we can fake WS access
         WSRPConsumer consumer = super.createConsumer(id, expirationCacheSeconds, wsdlURL);
         consumer.getProducerInfo().setEndpointConfigurationInfo(new MockEndpointConfigurationInfo());
         return consumer;
      }
   }
}
