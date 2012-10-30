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

package org.gatein.wsrp.consumer.registry;

import junit.framework.TestCase;
import org.gatein.wsrp.WSRPConsumer;
import org.gatein.wsrp.consumer.ConsumerException;
import org.gatein.wsrp.consumer.EndpointConfigurationInfo;
import org.gatein.wsrp.consumer.ProducerInfo;
import org.gatein.wsrp.consumer.RegistrationInfo;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 12686 $
 * @since 2.6
 */
public abstract class ConsumerRegistryTestCase extends TestCase
{
   protected AbstractConsumerRegistry registry;

   @Override
   protected void setUp() throws Exception
   {
      registry = new InMemoryConsumerRegistry();
   }

   public void testCreateAndGet()
   {
      String id = "test";
      WSRPConsumer consumer = registry.createConsumer(id, null, null);
      assertNotNull(consumer);
      assertEquals(id, consumer.getProducerId());
      ProducerInfo info = consumer.getProducerInfo();
      assertNotNull(info);
      assertNotNull(info.getKey());
      assertEquals(consumer.getProducerId(), info.getId());
      EndpointConfigurationInfo endpoint = info.getEndpointConfigurationInfo();
      assertNotNull(endpoint);
      RegistrationInfo regInfo = info.getRegistrationInfo();
      assertTrue(regInfo.isUndetermined());
      assertEquals(registry, info.getRegistry());
      assertTrue(registry.containsConsumer(id));

      WSRPConsumer fromRegistry = registry.getConsumer(id);
      assertNotNull(fromRegistry);
      assertEquals(consumer.getProducerId(), fromRegistry.getProducerId());
      ProducerInfo fromRegistryInfo = fromRegistry.getProducerInfo();
      assertNotNull(fromRegistryInfo);
      assertEquals(fromRegistry.getProducerId(), fromRegistryInfo.getId());
      assertNotNull(fromRegistryInfo.getEndpointConfigurationInfo());
      assertTrue(fromRegistryInfo.getRegistrationInfo().isUndetermined());
      assertEquals(registry, fromRegistryInfo.getRegistry());

      assertEquals(info.getId(), fromRegistryInfo.getId());
      assertEquals(info.getEndpointConfigurationInfo(), fromRegistryInfo.getEndpointConfigurationInfo());
      assertEquals(info.getRegistrationInfo(), fromRegistryInfo.getRegistrationInfo());

      Collection consumers = registry.getConfiguredConsumers();
      assertNotNull(consumers);
      assertEquals(1, consumers.size());
      assertTrue(consumers.contains(consumer));

      final Collection<String> ids = registry.getConfiguredConsumersIds();
      assertNotNull(ids);
      assertEquals(1, ids.size());
      assertTrue(ids.contains(id));

      assertEquals(1, registry.getConfiguredConsumerNumber());
   }

   public void testGetConsumer()
   {
      assertNull(registry.getConsumer("inexistent"));
   }

   public void testDoubleRegistrationOfConsumerWithSameId()
   {
      String id = "foo";

      registry.createConsumer(id, null, null);

      try
      {
         registry.createConsumer(id, null, null);
         fail("Shouldn't be possible to create a consumer with an existing id");
      }
      catch (ConsumerException expected)
      {
      }
   }

   public void testDelete()
   {
      String id = "id";

      WSRPConsumer consumer = registry.createConsumer(id, null, null);
      assertEquals(consumer, registry.getConsumer(id));
      assertTrue(registry.containsConsumer(id));
      assertEquals(1, registry.getConfiguredConsumerNumber());

      registry.destroyConsumer(id);

      assertFalse(registry.containsConsumer(id));
      assertNull(registry.getConsumer(id));
      assertEquals(0, registry.getConfiguredConsumerNumber());
   }

   public void testUpdateProducerInfo()
   {
      // create a foo consumer
      String id = "foo";
      WSRPConsumer consumer = registry.createConsumer(id, null, null);
      ProducerInfo info = consumer.getProducerInfo();

      // update unchanged ProducerInfo should return null
      String previousId = registry.updateProducerInfo(info);
      assertNull(previousId);

      // change the id on the consumer's producer info and save it
      info.setId("bar");
      previousId = registry.updateProducerInfo(info);
      assertEquals("foo", previousId);

      assertNull(registry.getConsumer(id));
      assertFalse(registry.containsConsumer(id));

      assertEquals(info, consumer.getProducerInfo());
      assertEquals(consumer, registry.getConsumer("bar"));
      assertTrue(registry.containsConsumer("bar"));
      assertEquals(1, registry.getConfiguredConsumerNumber());
   }

   public void testStoppingShouldNotStartConsumers() throws Exception
   {
      // fake marking consumer as active in persistence
      ProducerInfo info = Mockito.mock(ProducerInfo.class);
      Mockito.stub(info.isActive()).toReturn(true);
      Mockito.stub(info.getId()).toReturn("foo");
      Mockito.stub(info.getKey()).toReturn("fooKey");
      EndpointConfigurationInfo endpoint = Mockito.mock(EndpointConfigurationInfo.class);
      Mockito.stub(info.getEndpointConfigurationInfo()).toReturn(endpoint);
      registry.save(info, "Couldn't save ProducerInfo");

      WSRPConsumer original = registry.createConsumerFrom(info, true);

      // since consumer is supposed to be active, the registry will attempt to start it:
      assertEquals(original, registry.getFederatingPortletInvoker().getFederatedInvoker("foo").getPortletInvoker());

      // create a consumer to spy from
      WSRPConsumer consumer = Mockito.spy(original);

      // force re-init of registry from "persistence" to ensure that the spy registry actually uses our spy consumer
      AbstractConsumerRegistry registrySpy = Mockito.spy(registry);
      Mockito.doReturn(consumer).when(registrySpy).getConsumer("foo");
      Mockito.doReturn(Collections.singletonList(consumer)).when(registrySpy).getConsumers(false);

      WSRPConsumer foo = registrySpy.getConsumer("foo");
      assertTrue(foo.getProducerInfo().isActive());
      assertEquals(consumer, foo);

      // stop the consumer and then the registry and check that consumer.start hasn't been called
      consumer.stop();
      registrySpy.stop();
      Mockito.verify(consumer, Mockito.times(0)).start();

      // check that consumer is not known by the FederatingPortletInvoker anymore
      assertEquals(null, registrySpy.getFederatingPortletInvoker().getFederatedInvoker("foo"));
   }

   public void testReloadConsumers()
   {
      WSRPConsumer consumer1 = registry.createConsumer("consumer1", null, null);
      WSRPConsumer consumer2 = registry.createConsumer("consumer2", null, null);

      assertEquals(2, registry.getConfiguredConsumersIds().size());
      assertEquals(consumer1, registry.getConsumer(consumer1.getProducerId()));
      assertEquals(consumer2, registry.getConsumer(consumer2.getProducerId()));

      registry.reloadConsumers();

      assertEquals(2, registry.getConfiguredConsumersIds().size());
      assertEquals(consumer1, registry.getConsumer(consumer1.getProducerId()));
      assertEquals(consumer2, registry.getConsumer(consumer2.getProducerId()));
   }

   public void testCacheSimple()
   {
      WSRPConsumer consumer1 = registry.createConsumer("consumer1", null, null);

      assertTrue(registry.containsConsumer(consumer1.getProducerId()));

      assertEquals(consumer1, registry.consumerCache.getConsumer(consumer1.getProducerId()));

      final ProducerInfo info = consumer1.getProducerInfo();
      info.setId("foo");

      registry.updateProducerInfo(info);

      assertEquals(consumer1, registry.getConsumer("foo"));

      registry.destroyConsumer("foo");
      assertFalse(registry.containsConsumer("foo"));
   }

   public void testCacheModifyingProducerInfo()
   {
      WSRPConsumer consumer1 = registry.createConsumer("consumer1", null, null);

      assertTrue(registry.containsConsumer(consumer1.getProducerId()));

      final ProducerInfo info = consumer1.getProducerInfo();
      info.setId("foo");

      // use lower-level method to simulate change from persistence layer
      registry.update(info);

      // retrieving the consumer with its new id should work
      assertEquals(consumer1, registry.getConsumer("foo"));
   }

   public void testDefaultConfiguration() throws Exception
   {
      final String selfv1 = "selfv1";
      final String selfv2 = "selfv2";

      loadConsumersConfiguration("default.xml");

      assertEquals(2, registry.getConfiguredConsumerNumber());
      assertTrue(registry.containsConsumer(selfv1));
      assertTrue(registry.containsConsumer(selfv2));

      checkConsumer(selfv1, 500, 50000, false, "http://localhost:8080/wsrp-producer/v1/MarkupService?wsdl");
      checkConsumer(selfv2, 500, 50000, false, "http://localhost:8080/wsrp-producer/v2/MarkupService?wsdl");
   }

   private void checkConsumer(String id, int expirationCache, int wsTimeOut, boolean wssEnabled, String wsdlURL)
   {
      final WSRPConsumer consumer = registry.getConsumer(id);
      assertNotNull(consumer);
      final ProducerInfo producerInfo = consumer.getProducerInfo();
      assertNotNull(producerInfo);
      assertEquals(id, producerInfo.getId());
      assertEquals(expirationCache, (int)producerInfo.getExpirationCacheSeconds());
      final EndpointConfigurationInfo endpoint = producerInfo.getEndpointConfigurationInfo();
      assertEquals(wsTimeOut, endpoint.getWSOperationTimeOut());
      assertEquals(wssEnabled, endpoint.getWSSEnabled());
      assertEquals(wsdlURL, endpoint.getWsdlDefinitionURL());
   }

   protected void loadConsumersConfiguration(String fileName) throws Exception
   {
      URL location = Thread.currentThread().getContextClassLoader().getResource(fileName);
      assertNotNull(location);
      System.out.println("Loading consumers configuration = " + location);

      loadConsumersConfiguration(location);
   }

   protected abstract void loadConsumersConfiguration(URL location) throws IOException;
}
