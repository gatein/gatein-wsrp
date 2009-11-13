/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2006, Red Hat Middleware, LLC, and individual                    *
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
package org.gatein.wsrp.consumer.registry.xml;

import org.gatein.common.xml.NullEntityResolver;
import org.gatein.wsrp.WSRPConsumer;
import org.gatein.wsrp.consumer.ProducerInfo;
import org.gatein.wsrp.consumer.registry.AbstractConsumerRegistry;
import org.jboss.xb.binding.JBossXBException;
import org.jboss.xb.binding.ObjectModelFactory;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.EntityResolver;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 9360 $
 */
public class XMLConsumerRegistry extends AbstractConsumerRegistry
{
   private final static Logger log = LoggerFactory.getLogger(XMLConsumerRegistry.class);

   /** . */
   private static final String defaultWSRPLocation = "conf/wsrp-consumers-config.xml";

   private EntityResolver entityResolver;

   public XMLConsumerRegistry()
   {
      consumers = new TreeMap<String, WSRPConsumer>();
   }

   public EntityResolver getEntityResolver()
   {
      return entityResolver;
   }

   public void setEntityResolver(EntityResolver entityResolver)
   {
      this.entityResolver = entityResolver;
   }

   public void reloadConsumers()
   {
      URL defaultWSRPURL = Thread.currentThread().getContextClassLoader().getResource(defaultWSRPLocation);
      if (defaultWSRPURL != null)
      {
         InputStream inputStream;
         try
         {
            inputStream = defaultWSRPURL.openStream();
         }
         catch (IOException e)
         {
            throw new RuntimeException("Couldn't open default XML WSRP Consumer configuration file", e);
         }

         Unmarshaller unmarshaller = UnmarshallerFactory.newInstance().newUnmarshaller();
         ObjectModelFactory factory = new XMLWSRPConsumerFactory(this);
         if (entityResolver == null)
         {
            log.debug("Could not obtain entity resolver for XMLConsumerRegistry");
            entityResolver = new NullEntityResolver();
         }
         try
         {
            unmarshaller.setEntityResolver(entityResolver);
            consumers = (SortedMap<String, WSRPConsumer>)unmarshaller.unmarshal(inputStream, factory, null);
         }
         catch (JBossXBException e)
         {
            throw new RuntimeException("Couldn't set unmarshall WSRP Consumers configuration", e);
         }

         for (WSRPConsumer consumer : consumers.values())
         {

            ProducerInfo producerInfo = consumer.getProducerInfo();
            try
            {
               // if the producer is marked as active, activate it fo' real! :)
               if (producerInfo.isActive())
               {
                  activateConsumer(consumer);
               }
            }
            catch (Exception e)
            {
               producerInfo.setActive(false);
               updateProducerInfo(producerInfo);
            }
         }
      }
   }

   @Override
   public void stop() throws Exception
   {
      for (WSRPConsumer consumer : consumers.values())
      {
         consumer.stop();
      }
   }

   @Override
   protected void save(ProducerInfo info, String messageOnError)
   {
      // do nothing
   }

   @Override
   protected void delete(ProducerInfo info)
   {
      // do nothing
   }

   @Override
   protected String update(ProducerInfo producerInfo)
   {
      return null;
   }

   @Override
   protected Iterator<ProducerInfo> getProducerInfosFromStorage()
   {
      return new ProducerInfoIterator(consumers.values().iterator());
   }

   SortedMap<String, WSRPConsumer> getConsumers()
   {
      return consumers;
   }

   class ProducerInfoIterator implements Iterator<ProducerInfo>
   {
      private Iterator<WSRPConsumer> consumers;

      ProducerInfoIterator(Iterator<WSRPConsumer> consumers)
      {
         this.consumers = consumers;
      }

      public boolean hasNext()
      {
         return consumers.hasNext();
      }

      public ProducerInfo next()
      {
         return consumers.next().getProducerInfo();
      }

      public void remove()
      {
         throw new UnsupportedOperationException("remove not supported on this iterator implementation");
      }
   }
}
