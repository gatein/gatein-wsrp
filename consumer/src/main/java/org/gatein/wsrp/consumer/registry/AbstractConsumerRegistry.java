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
import org.gatein.pc.api.PortletInvoker;
import org.gatein.pc.api.PortletInvokerException;
import org.gatein.pc.federation.FederatedPortletInvoker;
import org.gatein.pc.federation.FederatingPortletInvoker;
import org.gatein.wsrp.WSRPConsumer;
import org.gatein.wsrp.api.session.SessionEventBroadcaster;
import org.gatein.wsrp.consumer.ConsumerException;
import org.gatein.wsrp.consumer.ProducerInfo;
import org.gatein.wsrp.consumer.WSRPConsumerImpl;
import org.gatein.wsrp.consumer.migration.MigrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 12693 $
 * @since 2.6
 */
public abstract class AbstractConsumerRegistry implements ConsumerRegistry
{
   /** Gives access to the Portal's portlet invokers */
   private FederatingPortletInvoker federatingPortletInvoker;

   private SessionEventBroadcaster sessionEventBroadcaster = SessionEventBroadcaster.NO_OP_BROADCASTER;
   private MigrationService migrationService;

   private static final String CONSUMER_WITH_ID = "Consumer with id '";
   private static final String RELEASE_SESSIONS_LISTENER = "release_sessions_listener_";

   private static final Logger log = LoggerFactory.getLogger(AbstractConsumerRegistry.class);

   private ConsumerCache consumers = new InMemoryConsumerCache();

   public void setConsumerCache(ConsumerCache consumers)
   {
      this.consumers = consumers;
   }

   public FederatingPortletInvoker getFederatingPortletInvoker()
   {
      return federatingPortletInvoker;
   }

   public void setSessionEventBroadcaster(SessionEventBroadcaster sessionEventBroadcaster)
   {
      this.sessionEventBroadcaster = sessionEventBroadcaster;
   }

   public MigrationService getMigrationService()
   {
      return migrationService;
   }

   public void setMigrationService(MigrationService migrationService)
   {
      this.migrationService = migrationService;
   }

   public WSRPConsumer createConsumer(String id, Integer expirationCacheSeconds, String wsdlURL)
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(id, "Consumer identifier", "Creating a Consumer");

      if (getConsumer(id) != null)
      {
         throw new ConsumerException(CONSUMER_WITH_ID + id + "' already exists!");
      }


      ProducerInfo info = new ProducerInfo();
      info.setId(id);
      info.setExpirationCacheSeconds(expirationCacheSeconds);
      info.getEndpointConfigurationInfo().setWsdlDefinitionURL(wsdlURL);

      save(info, "Couldn't create Consumer '" + id + "'");

      log.debug(CONSUMER_WITH_ID + id + "' created");
      return createConsumerFrom(info);
   }

   public void destroyConsumer(String id)
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(id, "Consumer identifier", "Destroying a Consumer");

      WSRPConsumer consumer = getConsumer(id);
      if (consumer != null)
      {
         ProducerInfo info = consumer.getProducerInfo();

         try
         {
            consumer.releaseSessions();
         }
         catch (PortletInvokerException e)
         {
            log.debug("releaseSessions failed when attempting to destroy " + CONSUMER_WITH_ID + id + "'");
         }

         // if the consumer is registered, deregister it
         if (info.isRegistered())
         {
            registerOrDeregisterConsumerWith(id, false);
         }

         deactivateConsumer(consumer);

         delete(info);

         // remove from cache
         consumers.removeConsumer(id);
      }
      else
      {
         throw new ConsumerException(CONSUMER_WITH_ID + id + "' doesn't exist!");
      }

      log.debug(CONSUMER_WITH_ID + id + "' destroyed");
   }

   public void persistConsumer(WSRPConsumer consumer)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(consumer, "Consumer");

      ProducerInfo info = consumer.getProducerInfo();

      save(info, CONSUMER_WITH_ID + info.getId() + "' couldn't be persisted!");

      createConsumerFrom(info);
   }

   public void setFederatingPortletInvoker(FederatingPortletInvoker federatingPortletInvoker)
   {
      this.federatingPortletInvoker = federatingPortletInvoker;
   }

   protected WSRPConsumer createConsumerFrom(ProducerInfo producerInfo)
   {
      // make sure we set the registry after loading from DB since registry is not persisted.
      producerInfo.setRegistry(this);

      final WSRPConsumerImpl consumer = new WSRPConsumerImpl(producerInfo, migrationService);

      // cache consumer
      consumers.putConsumer(producerInfo.getId(), consumer);

      return consumer;
   }

   public void activateConsumerWith(String id) throws ConsumerException
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(id, "Consumer identifier", "Activating a Consumer");
      activateConsumer(getConsumer(id));
   }

   protected void activateConsumer(WSRPConsumer consumer)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(consumer, "WSRPConsumer");
      String id = consumer.getProducerId();

      if (!federatingPortletInvoker.isResolved(id))
      {
         startOrStopConsumer(consumer, true);
      }
   }

   public void deactivateConsumerWith(String id) throws ConsumerException
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(id, "Consumer identifier", "Deactivating a Consumer");
      deactivateConsumer(getConsumer(id));
   }

   protected void deactivateConsumer(WSRPConsumer consumer)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(consumer, "Consumer");
      String id = consumer.getProducerId();

      // only process if there is a registered Consumer with the specified id
      if (federatingPortletInvoker.isResolved(id))
      {
         startOrStopConsumer(consumer, false);
      }
   }

   public String updateProducerInfo(ProducerInfo producerInfo)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(producerInfo, "ProducerInfo");

      String oldId = update(producerInfo);

      // if we updated and oldId is not null, we need to update the local information
      if (oldId != null)
      {
         WSRPConsumer consumer = createConsumerFrom(producerInfo);

         // update the federating portlet invoker if needed
         if (federatingPortletInvoker.isResolved(oldId))
         {
            federatingPortletInvoker.unregisterInvoker(oldId);
            federatingPortletInvoker.registerInvoker(producerInfo.getId(), consumer);
         }

         // update cache
         consumers.removeConsumer(oldId);
         consumers.putConsumer(producerInfo.getId(), consumer);
      }

      return oldId;
   }

   public void start() throws Exception
   {
      reloadConsumers();
   }

   public void reloadConsumers()
   {
      consumers.clear();

      Iterator<ProducerInfo> producerInfos = getProducerInfosFromStorage();

      // load the configured producers
      ProducerInfo producerInfo;
      while (producerInfos.hasNext())
      {
         producerInfo = producerInfos.next();

         createConsumerFrom(producerInfo);
      }
   }

   public void stop() throws Exception
   {
      for (WSRPConsumer consumer : getConsumers(false))
      {
         // if producer is not active, it shouldn't be registered with the federating portlet invoker, hence do not
         // unregister it. We have changed how consumers are registered (active consumers are not automatically
         // registered anymore), we also need to check if the consumer is known by the federating portlet invoker... 
         String producerId = consumer.getProducerId();
         if (consumer.getProducerInfo().isActive() && federatingPortletInvoker.isResolved(producerId))
         {
            federatingPortletInvoker.unregisterInvoker(producerId);
         }

         try
         {
            consumer.stop();
         }
         catch (Exception e)
         {
            // ignore and continue
         }
      }
   }

   public List<WSRPConsumer> getConfiguredConsumers()
   {
      return getConsumers(true);
   }

   public WSRPConsumer getConsumer(String id)
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(id, "consumer id", null);

      // try cache first
      WSRPConsumer consumer = consumers.getConsumer(id);
      if (consumer != null)
      {
         return consumer;
      }
      else
      {
         ProducerInfo info = loadProducerInfo(id);
         if (info != null)
         {
            return createConsumerFrom(info);
         }
         else
         {
            return null;
         }
      }

   }

   public boolean containsConsumer(String id)
   {
      return getConsumer(id) != null;
   }

   public Collection<String> getConfiguredConsumersIds()
   {
      return new AbstractCollection<String>()
      {
         final private List<WSRPConsumer> consumers = getConsumers(false);

         @Override
         public Iterator<String> iterator()
         {
            return new Iterator<String>()
            {
               private Iterator<WSRPConsumer> consumerIterator = consumers.iterator();

               public boolean hasNext()
               {
                  return consumerIterator.hasNext();
               }

               public String next()
               {
                  return consumerIterator.next().getProducerId();
               }

               public void remove()
               {
                  throw new UnsupportedOperationException();
               }
            };
         }

         @Override
         public int size()
         {
            return consumers.size();
         }
      };
   }

   public int getConfiguredConsumerNumber()
   {
      return getConfiguredConsumersIds().size();
   }

   public void registerOrDeregisterConsumerWith(String id, boolean register)
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(id, "Consumer identifier", "Registering or deregistering a Consumer");

      WSRPConsumer consumer = getConsumer(id);

      if (consumer == null)
      {
         throw new ConsumerException(CONSUMER_WITH_ID + id + "' doesn't exist!");
      }

      try
      {
         if (register)
         {
            consumer.getProducerInfo().register();
         }
         else
         {
            consumer.getProducerInfo().deregister();
         }
      }
      catch (Exception e)
      {
         // unexpected exception: deactivate the consumer
         deactivateConsumer(consumer);
         Throwable cause = e.getCause();
         throw new ConsumerException("Couldn't " + (register ? "register" : "deregister") + CONSUMER_WITH_ID + id + "'",
            cause != null ? cause : e);
      }
   }

   private void startOrStopConsumer(WSRPConsumer consumer, boolean start)
   {
      try
      {
         String id = consumer.getProducerId();
         if (start)
         {
            consumer.activate();
            federatingPortletInvoker.registerInvoker(id, consumer);
            sessionEventBroadcaster.registerListener(getListenerIdFrom(id), consumer);
         }
         else
         {
            FederatedPortletInvoker fedInvoker = federatingPortletInvoker.getFederatedInvoker(id);
            if (fedInvoker != null)
            {
               PortletInvoker invoker = fedInvoker.getPortletInvoker();
               if (invoker instanceof WSRPConsumer)
               {
                  consumer = (WSRPConsumer)invoker;
                  consumer.deactivate();
                  federatingPortletInvoker.unregisterInvoker(id);
                  sessionEventBroadcaster.unregisterListener(getListenerIdFrom(id));
               }
               else
               {
                  throw new IllegalArgumentException("PortletInvoker with id '" + id + "' is not a WSRPConsumer!");
               }
            }
            else
            {
               throw new IllegalArgumentException("There is no registered PortletInvoker with id '" + id + "'");
            }
         }
      }
      catch (Exception e)
      {
         throw new ConsumerException("Couldn't " + (start ? "start" : "stop") + " Consumer service '" + consumer.getProducerId() + "'", e);
      }

      // update ProducerInfo
      updateProducerInfo(consumer.getProducerInfo());
   }

   private String getListenerIdFrom(String id)
   {
      return RELEASE_SESSIONS_LISTENER + id;
   }

   protected abstract void save(ProducerInfo info, String messageOnError) throws ConsumerException;

   protected abstract void delete(ProducerInfo info) throws ConsumerException;

   /**
    * Persists the changes made to ProducerInfo.
    *
    * @param producerInfo
    * @return the previous value of the ProducerInfo's id if it has changed, <code>null</code> otherwise
    */
   protected abstract String update(ProducerInfo producerInfo);

   protected abstract Iterator<ProducerInfo> getProducerInfosFromStorage();

   protected abstract ProducerInfo loadProducerInfo(String id);

   protected List<WSRPConsumer> getConsumers(boolean startConsumers)
   {
      final Collection<WSRPConsumer> consumerz = consumers.getConsumers();
      for (WSRPConsumer consumer : consumerz)
      {
         if (startConsumers)
         {
            final ProducerInfo info = consumer.getProducerInfo();
            if (info.isActive() && !consumer.isActive())
            {
               try
               {
                  consumer.refresh(false);
               }
               catch (Exception e)
               {
                  log.info("Couldn't activate consumer " + consumer.getProducerId());
                  info.setActiveAndSave(false);
               }
            }
         }
      }

      return new ArrayList<WSRPConsumer>(consumerz);
   }

   protected class ProducerInfoIterator implements Iterator<ProducerInfo>
   {
      private Iterator<WSRPConsumer> consumers;

      public ProducerInfoIterator(Iterator<WSRPConsumer> consumers)
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

   protected class InMemoryConsumerCache implements ConsumerCache
   {
      private Map<String, WSRPConsumer> consumers = new HashMap<String, WSRPConsumer>(11);

      public Collection<WSRPConsumer> getConsumers()
      {
         return consumers.values();
      }

      public WSRPConsumer getConsumer(String id)
      {
         return consumers.get(id);
      }

      public WSRPConsumer removeConsumer(String id)
      {
         return consumers.remove(id);
      }

      public void putConsumer(String id, WSRPConsumer consumer)
      {
         consumers.put(id, consumer);
      }

      public void clear()
      {
         consumers.clear();
      }
   }
}
