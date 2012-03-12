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

import org.gatein.common.util.ParameterValidation;
import org.gatein.pc.federation.impl.FederatingPortletInvokerService;
import org.gatein.wsrp.WSRPConsumer;
import org.gatein.wsrp.consumer.ProducerInfo;
import org.gatein.wsrp.consumer.migration.InMemoryMigrationService;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.UUID;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class InMemoryConsumerRegistry extends AbstractConsumerRegistry
{
   private SortedMap<String, WSRPConsumer> consumers;
   private Map<String, String> keysToIds;

   public InMemoryConsumerRegistry()
   {
      initConsumers(null);
      setFederatingPortletInvoker(new FederatingPortletInvokerService());
      setMigrationService(new InMemoryMigrationService());
   }

   @Override
   protected void initConsumerCache()
   {
      setConsumerCache(new InMemoryConsumerCache(this));
   }

   @Override
   public WSRPConsumer createConsumerFrom(ProducerInfo producerInfo, boolean putInCache)
   {
      WSRPConsumer consumer = super.createConsumerFrom(producerInfo, putInCache);

      String id = consumer.getProducerId();
      consumers.put(id, consumer);
      ProducerInfo info = consumer.getProducerInfo();
      keysToIds.put(info.getKey(), id);

      return consumer;
   }

   @Override
   public String updateProducerInfo(ProducerInfo producerInfo)
   {
      String oldId = super.updateProducerInfo(producerInfo);
      if (oldId != null)
      {
         keysToIds.put(producerInfo.getKey(), producerInfo.getId());
         consumers.remove(oldId);
      }
      return oldId;
   }

   public void save(ProducerInfo info, String messageOnError)
   {
      // generate a UUID for ProducerInfo
      info.setKey(UUID.randomUUID().toString());
      keysToIds.put(info.getKey(), info.getId());
   }

   public void delete(ProducerInfo info)
   {
      String key = info.getKey();
      String removed = keysToIds.remove(key);
      if (removed != null)
      {
         consumers.remove(removed);
      }
   }

   public String update(ProducerInfo producerInfo)
   {
      String key = producerInfo.getKey();
      String oldId = keysToIds.get(key);
      String newId = producerInfo.getId();
      if (oldId.equals(newId))
      {
         return null;
      }
      else
      {
         keysToIds.put(key, newId);
         WSRPConsumer consumer = consumers.get(oldId);
         consumers.put(newId, consumer);
         return oldId;
      }
   }

   @Override
   public void reloadConsumers()
   {
      // do nothing
   }

   public Iterator<ProducerInfo> getProducerInfosFromStorage()
   {
      return new ProducerInfoIterator(consumers.values().iterator());
   }

   public ProducerInfo loadProducerInfo(String id)
   {
      if (keysToIds.containsValue(id))
      {
         final WSRPConsumer consumer = consumers.get(id);
         return consumer != null ? consumer.getProducerInfo() : null;
      }
      else
      {
         return null;
      }
   }

   @Override
   public void stop() throws Exception
   {
      super.stop();
      consumers.clear();
      keysToIds.clear();
      consumers = null;
      keysToIds = null;
   }

   public Collection<String> getConfiguredConsumersIds()
   {
      return consumers.keySet();
   }

   protected void initConsumers(SortedMap<String, WSRPConsumer> consumers)
   {
      if (!ParameterValidation.existsAndIsNotEmpty(consumers))
      {
         consumers = new TreeMap<String, WSRPConsumer>();
      }
      this.consumers = consumers;
      int size = consumers.size();
      keysToIds = size == 0 ? new HashMap<String, String>() : new HashMap<String, String>(size);
      for (WSRPConsumer consumer : consumers.values())
      {
         keysToIds.put(consumer.getProducerInfo().getKey(), consumer.getProducerId());
      }
   }
}
