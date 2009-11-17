/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2009, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/

package org.gatein.wsrp.consumer.registry;

import junit.framework.TestCase;
import org.gatein.wsrp.WSRPConsumer;
import org.gatein.wsrp.consumer.ConsumerException;
import org.gatein.wsrp.consumer.EndpointConfigurationInfo;
import org.gatein.wsrp.consumer.ProducerInfo;
import org.gatein.wsrp.consumer.RegistrationInfo;
import org.gatein.wsrp.consumer.registry.xml.XMLConsumerRegistry;
import org.jboss.unit.api.pojo.annotations.Test;

import java.util.Collection;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 12686 $
 * @since 2.6
 */
@Test
public class ConsumerRegistryTestCase extends TestCase
{
   private ConsumerRegistry registry = new XMLConsumerRegistry();

   public ConsumerRegistry getRegistry()
   {
      return registry;
   }

   public void setRegistry(ConsumerRegistry registry)
   {
      this.registry = registry;
   }

   public void testCRUD()
   {
//      TransactionAssert.beginTransaction();
      String id = "test";
      WSRPConsumer consumer = registry.createConsumer(id, null);
      assertNotNull(consumer);
      assertEquals(id, consumer.getProducerId());
      ProducerInfo info = consumer.getProducerInfo();
      assertNotNull(info);
      assertEquals(consumer.getProducerId(), info.getId());
      EndpointConfigurationInfo endpoint = info.getEndpointConfigurationInfo();
      assertNotNull(endpoint);
      RegistrationInfo regInfo = info.getRegistrationInfo();
      assertTrue(regInfo.isUndetermined());
//      TransactionAssert.commitTransaction();

//      TransactionAssert.beginTransaction();
      try
      {
         registry.createConsumer(id, null);
         fail("Shouldn't be possible to create a consumer with an existing id");
      }
      catch (ConsumerException expected)
      {
         // transaction should have been rolled back
//         TransactionAssert.rollbackTransaction();
      }

//      TransactionAssert.beginTransaction();
      consumer = registry.getConsumer(id);
      assertNotNull(consumer);
      assertEquals(id, consumer.getProducerId());
      info = consumer.getProducerInfo();
      assertNotNull(info);
      assertEquals(consumer.getProducerId(), info.getId());
      endpoint = info.getEndpointConfigurationInfo();
      assertNotNull(endpoint);
      assertTrue(info.getRegistrationInfo().isUndetermined());

      assertNull(registry.getConsumer("inexistent"));
      Collection consumers = registry.getConfiguredConsumers();
      assertNotNull(consumers);
      assertEquals(1, consumers.size());
      assertTrue(consumers.contains(consumer));
//      TransactionAssert.commitTransaction();
   }

   public void testUpdateProducerInfo()
   {
      // create a foo consumer
//      TransactionAssert.beginTransaction();
      String id = "foo";
      WSRPConsumer consumer = registry.createConsumer(id, null);
      ProducerInfo info = consumer.getProducerInfo();
//      TransactionAssert.commitTransaction();

//      TransactionAssert.beginTransaction();
      // change the id on the consumer's producer info and save it
      info.setId("bar");
      registry.updateProducerInfo(info);

      assertNull(registry.getConsumer(id));
      assertEquals(consumer, registry.getConsumer("bar"));
//      TransactionAssert.commitTransaction();
   }
}
