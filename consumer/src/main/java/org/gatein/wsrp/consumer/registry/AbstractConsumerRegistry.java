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
import org.gatein.pc.api.PortletInvokerException;
import org.gatein.pc.federation.FederatingPortletInvoker;
import org.gatein.wsrp.WSRPConsumer;
import org.gatein.wsrp.api.session.SessionEventBroadcaster;
import org.gatein.wsrp.consumer.ConsumerException;
import org.gatein.wsrp.consumer.ProducerInfo;
import org.gatein.wsrp.consumer.WSRPConsumerImpl;
import org.gatein.wsrp.consumer.handlers.session.InMemorySessionRegistry;
import org.gatein.wsrp.consumer.handlers.session.SessionRegistry;
import org.gatein.wsrp.consumer.migration.InMemoryMigrationService;
import org.gatein.wsrp.consumer.migration.MigrationService;
import org.gatein.wsrp.consumer.spi.ConsumerRegistrySPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 12693 $
 * @since 2.6
 */
public abstract class AbstractConsumerRegistry implements ConsumerRegistrySPI
{
   /** Gives access to the Portal's portlet invokers */
   private FederatingPortletInvoker federatingPortletInvoker;

   private SessionEventBroadcaster sessionEventBroadcaster = SessionEventBroadcaster.NO_OP_BROADCASTER;
   private MigrationService migrationService = new InMemoryMigrationService();
   private SessionRegistry sessionRegistry = new InMemorySessionRegistry();

   private static final String CONSUMER_WITH_ID = "Consumer with id '";
   private static final String RELEASE_SESSIONS_LISTENER = "release_sessions_listener_";

   private static final Logger log = LoggerFactory.getLogger(AbstractConsumerRegistry.class);

   protected ConsumerCache consumerCache;

   protected AbstractConsumerRegistry()
   {
      initConsumerCache();
   }

   protected abstract void initConsumerCache();

   public synchronized void setConsumerCache(ConsumerCache consumers)
   {
      if (consumers == null)
      {
         consumers = new InMemoryConsumerCache(this);
      }
      this.consumerCache = consumers;
   }

   public synchronized void setSessionRegistry(SessionRegistry sessionRegistry)
   {
      if (sessionRegistry == null)
      {
         sessionRegistry = new InMemorySessionRegistry();
      }
      this.sessionRegistry = sessionRegistry;
   }

   public SessionRegistry getSessionRegistry()
   {
      return sessionRegistry;
   }

   public FederatingPortletInvoker getFederatingPortletInvoker()
   {
      return federatingPortletInvoker;
   }

   public synchronized void setSessionEventBroadcaster(SessionEventBroadcaster sessionEventBroadcaster)
   {
      if (sessionEventBroadcaster == null)
      {
         sessionEventBroadcaster = SessionEventBroadcaster.NO_OP_BROADCASTER;
      }
      this.sessionEventBroadcaster = sessionEventBroadcaster;
   }

   public MigrationService getMigrationService()
   {
      return migrationService;
   }

   public synchronized void setMigrationService(MigrationService migrationService)
   {
      if (migrationService == null)
      {
         migrationService = new InMemoryMigrationService();
      }
      this.migrationService = migrationService;
   }

   public WSRPConsumer createConsumer(String id, Integer expirationCacheSeconds, String wsdlURL)
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(id, "Consumer identifier", "Creating a Consumer");

      if (getConsumer(id) != null)
      {
         throw new ConsumerException(CONSUMER_WITH_ID + id + "' already exists!");
      }


      ProducerInfo info = new ProducerInfo(this);
      info.setId(id);
      info.setExpirationCacheSeconds(expirationCacheSeconds);
      info.getEndpointConfigurationInfo().setWsdlDefinitionURL(wsdlURL);

      save(info, "Couldn't create Consumer '" + id + "'");

      log.debug(CONSUMER_WITH_ID + id + "' created");
      return createConsumerFrom(info, true);
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
         consumerCache.removeConsumer(id);
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

      createConsumerFrom(info, true);
   }

   public synchronized void setFederatingPortletInvoker(FederatingPortletInvoker federatingPortletInvoker)
   {
      this.federatingPortletInvoker = federatingPortletInvoker;
   }

   public WSRPConsumer createConsumerFrom(ProducerInfo producerInfo, boolean putInCache)
   {
      // make sure we set the registry after loading from DB since registry is not persisted.
//      producerInfo.setRegistry(this);

      final WSRPConsumerImpl consumer = createAndActivateIfNeeded(producerInfo);

      // cache consumer
      if (putInCache)
      {
         consumerCache.putConsumer(producerInfo.getId(), consumer);
      }

      return consumer;
   }

   private WSRPConsumerImpl createAndActivateIfNeeded(ProducerInfo producerInfo)
   {
      final WSRPConsumerImpl consumer = new WSRPConsumerImpl(producerInfo);

      // try to activate consumer if it's marked as active and isn't yet
      if (producerInfo.isActive() && !consumer.isActive())
      {
         activateConsumer(consumer);
      }
      return consumer;
   }

   public void activateConsumerWith(String id) throws ConsumerException
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(id, "Consumer identifier", "Activating a Consumer");
      activateConsumer(getConsumer(id));
   }

   protected void activateConsumer(WSRPConsumer consumer)
   {
      startOrStopConsumer(consumer, true, false);
   }

   public void deactivateConsumerWith(String id) throws ConsumerException
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(id, "Consumer identifier", "Deactivating a Consumer");
      deactivateConsumer(getConsumer(id));
   }

   protected void deactivateConsumer(WSRPConsumer consumer)
   {
      startOrStopConsumer(consumer, false, false);
   }

   public void registerWithFederatingPortletInvoker(WSRPConsumer consumer)
   {
      startOrStopConsumer(consumer, true, true);
   }

   public void deregisterWithFederatingPortletInvoker(WSRPConsumer consumer)
   {
      startOrStopConsumer(consumer, false, true);
   }

   public long getPersistedLastModifiedForProducerInfoWith(String id)
   {
      ProducerInfo producerInfo = loadProducerInfo(id);
      if (producerInfo == null)
      {
         return Long.MAX_VALUE;
      }
      else
      {
         return producerInfo.getLastModified();
      }
   }

   public synchronized String updateProducerInfo(ProducerInfo producerInfo)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(producerInfo, "ProducerInfo");

      String oldId = update(producerInfo);

      // if we updated and oldId is not null, we need to update the local information
      if (oldId != null)
      {
         WSRPConsumer consumer = createConsumerFrom(producerInfo, true);

         // update the federating portlet invoker if needed
         if (federatingPortletInvoker.isResolved(oldId))
         {
            federatingPortletInvoker.unregisterInvoker(oldId);
         }

         // update cache
         consumerCache.removeConsumer(oldId);
         consumerCache.putConsumer(producerInfo.getId(), consumer);
      }

      return oldId;
   }

   public void start() throws Exception
   {
      reloadConsumers();
   }

   public void reloadConsumers()
   {
      consumerCache.initFromStorage();
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

      return consumerCache.getConsumer(id);
   }

   public boolean containsConsumer(String id)
   {
      return getConsumer(id) != null;
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

   private void startOrStopConsumer(WSRPConsumer consumer, boolean start, boolean registerOrDeregisterOnly)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(consumer, "WSRPConsumer");
      try
      {
         String id = consumer.getProducerId();
         if (start)
         {
            if (!registerOrDeregisterOnly)
            {
               consumer.refresh(false);
            }

            if (!federatingPortletInvoker.isResolved(id))
            {
               federatingPortletInvoker.registerInvoker(id, consumer);
            }

            sessionEventBroadcaster.registerListener(getListenerIdFrom(id), consumer);
         }
         else
         {
            if (!registerOrDeregisterOnly)
            {
               consumer.deactivate();
            }

            if (federatingPortletInvoker.isResolved(id))
            {
               federatingPortletInvoker.unregisterInvoker(id);
            }

            sessionEventBroadcaster.unregisterListener(getListenerIdFrom(id));
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

   protected List<WSRPConsumer> getConsumers(boolean startConsumers)
   {
      final Collection<WSRPConsumer> consumerz = consumerCache.getConsumers();

      if (startConsumers)
      {
         for (WSRPConsumer consumer : consumerz)
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

   protected static class InMemoryConsumerCache implements ConsumerCache
   {

      private Map<String, WSRPConsumer> consumers = new ConcurrentHashMap<String, WSRPConsumer>(11);
      private boolean invalidated;
      private ConsumerRegistrySPI registry;

      public InMemoryConsumerCache(ConsumerRegistrySPI registry)
      {
         this.registry = registry;
      }

      public void initFromStorage()
      {
         clear();
         Iterator<ProducerInfo> infosFromStorage = registry.getProducerInfosFromStorage();
         while (infosFromStorage.hasNext())
         {
            ProducerInfo info = infosFromStorage.next();
            consumers.put(info.getId(), createConsumer(info));
         }
         setInvalidated(false);
      }

      private WSRPConsumer createConsumer(ProducerInfo info)
      {
         return registry.createConsumerFrom(info, false);
      }

      public Collection<WSRPConsumer> getConsumers()
      {
         refreshIfNeeded();
         return consumers.values();
      }

      public WSRPConsumer getConsumer(String id)
      {
         // try cache first
         WSRPConsumer consumer = consumers.get(id);

         return getUpdatedConsumer(id, consumer);
      }

      private WSRPConsumer getUpdatedConsumer(String id, WSRPConsumer consumer)
      {
         if (consumer == null || consumer.getProducerInfo().getLastModified() < registry.getPersistedLastModifiedForProducerInfoWith(id))
         {
            // if consumer is not in cache or was modified in persistence, (re-)load it from persistence
            ProducerInfo info = registry.loadProducerInfo(id);
            if (info != null)
            {
               consumer = createConsumer(info);
               consumers.put(id, consumer);
               return consumer;
            }
            else
            {
               return null;
            }
         }
         else
         {
            return consumer;
         }
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
         invalidated = true;
      }

      public boolean isInvalidated()
      {
         return invalidated;
      }

      public void setInvalidated(boolean invalidated)
      {
         this.invalidated = invalidated;
      }

      protected void refreshIfNeeded()
      {
         if (isInvalidated())
         {
            consumers.clear();
         }

         // first remove all obsolete Consumers in cache
         Collection<String> consumersIds = registry.getConfiguredConsumersIds();
         Set<String> obsoleteConsumers = new HashSet<String>(consumers.keySet());
         obsoleteConsumers.removeAll(consumersIds);
         for (String obsolete : obsoleteConsumers)
         {
            consumers.remove(obsolete);
         }

         // then check, for each consumer, if it has been modified since we last checked
         for (String id : consumersIds)
         {
            WSRPConsumer consumerInfo = consumers.get(id);
            if (consumerInfo != null)
            {
               getUpdatedConsumer(id, consumerInfo);
            }
            else
            {
               ProducerInfo producerInfo = registry.loadProducerInfo(id);
               consumers.put(id, createConsumer(producerInfo));
            }
         }

         setInvalidated(false);
      }
   }
}
