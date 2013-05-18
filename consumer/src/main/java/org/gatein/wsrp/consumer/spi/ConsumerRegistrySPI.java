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

package org.gatein.wsrp.consumer.spi;

import org.gatein.pc.federation.FederatingPortletInvoker;
import org.gatein.wsrp.WSRPConsumer;
import org.gatein.wsrp.api.session.SessionEventBroadcaster;
import org.gatein.wsrp.consumer.ConsumerException;
import org.gatein.wsrp.consumer.ProducerInfo;
import org.gatein.wsrp.consumer.handlers.session.SessionRegistry;
import org.gatein.wsrp.consumer.migration.MigrationService;
import org.gatein.wsrp.consumer.registry.ConsumerRegistry;

import java.util.Iterator;

/**
 * Defines the behavior that ConsumerRegistry implementors need to provide in addition to the client-facing API provided by ConsumerRegistry itself.
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 */
public interface ConsumerRegistrySPI extends ConsumerRegistry
{
   void setSessionEventBroadcaster(SessionEventBroadcaster sessionEventBroadcaster);

   void setFederatingPortletInvoker(FederatingPortletInvoker federatingPortletInvoker);

   MigrationService getMigrationService();

   void setMigrationService(MigrationService migrationService);

   SessionRegistry getSessionRegistry();

   void setSessionRegistry(SessionRegistry sessionRegistry);

   /**
    * Gets this ConsumerRegistry ready to work, in particular, loads the consumers from persistence and starts them, registering the active ones with the FederatingPortletInvoker.
    *
    * @throws Exception
    */
   void start() throws Exception;

   /**
    * Stops all consumers, deregistering them with the FederatingPortletInvoker.
    *
    * @throws Exception
    */
   void stop() throws Exception;

   /**
    * Saves for the first time the specified ProducerInfo to persistence, providing a message in case an error happens.
    *
    * @param info           the ProducerInfo to persist
    * @param messageOnError the message to pass along in case an error happens
    * @throws ConsumerException
    */
   void save(ProducerInfo info, String messageOnError) throws ConsumerException;

   /**
    * Deletes the specified ProducerInfo from persistence.
    *
    * @param info the ProducerInfo to delete
    * @throws ConsumerException
    */
   void delete(ProducerInfo info) throws ConsumerException;

   /**
    * Persists the changes made to an already persisted ProducerInfo. Attempting to update a ProducerInfo that has never been persisted before will yield an exception.
    *
    * @param producerInfo the ProducerInfo which modifications we want to persist
    * @return the previous value of the ProducerInfo's id if it has changed, <code>null</code> otherwise
    */
   String update(ProducerInfo producerInfo);

   /**
    * Retrieves all ProducerInfos currently persisted from the persistent store.
    *
    * @return all ProducerInfos currently persisted from the persistent store.
    */
   Iterator<ProducerInfo> getProducerInfosFromStorage();

   /**
    * Loads the ProducerInfo associated with the specified identifier from the persistent state.
    *
    * @param id the identifier of the ProducerInfo to load
    * @return the ProducerInfo identified by the provided identifier or <code>null</code> if no such ProducerInfo is currently persisted
    */
   ProducerInfo loadProducerInfo(String id);

   /**
    * Creates a consumer from the specified ProducerInfo, caching it if so specified.
    *
    * @param producerInfo the ProducerInfo to create a consumer from
    * @param putInCache   whether to cache the newly created consumer
    * @return a WSRPConsumer implementation based on the producer metadata specified by the provided ProducerInfo
    */
   WSRPConsumer createConsumerFrom(ProducerInfo producerInfo, boolean putInCache);

   /**
    * Registers the specified WSRPConsumer with the FederatingPortletInvoker.
    *
    * @param consumer the WSRPConsumer to register
    */
   void registerWithFederatingPortletInvoker(WSRPConsumer consumer);

   /**
    * Deregisters the specified WSRPConsumer with the FederatingPortletInvoker.
    *
    * @param consumer the WSRPConsumer to deregister
    */
   void deregisterWithFederatingPortletInvoker(WSRPConsumer consumer);

   /**
    * Checks the last modification time that was persisted for the ProducerInfo identified by the specified identifier. This is useful to check whether a given, local ProducerInfo
    * has been modified since it was last persisted or, on the flip side, whether some other process (like a different node cluster) has persisted changes since we last loaded the
    * ProducerInfo from the persistent store. This method is meant to be a lightweight dirty check of the ProducerInfo with respect to the persistence layer.
    *
    * @param id the identifier of the ProducerInfo we want to check
    * @return the last mo
    */
   long getPersistedLastModifiedForProducerInfoWith(String id);
}
