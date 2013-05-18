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

import org.gatein.pc.federation.FederatingPortletInvoker;
import org.gatein.wsrp.WSRPConsumer;
import org.gatein.wsrp.consumer.ConsumerException;
import org.gatein.wsrp.consumer.ProducerInfo;

import java.util.Collection;
import java.util.List;

/**
 * Handles consumers' persistence and lifecycles.
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 12693 $
 * @since 2.6
 */
public interface ConsumerRegistry
{
   /**
    * Retrieves the list of configured consumers.
    *
    * @return the list of configured consumers
    */
   List<WSRPConsumer> getConfiguredConsumers();

   /**
    * Retrieves the consumer associated with the specified producer identifier.
    *
    * @param id the producer identifier of the consumer we want to retrieve
    * @return the consumer associated with the specified producer identifier
    */
   WSRPConsumer getConsumer(String id);

   /**
    * Retrieves the {@link FederatingPortletInvoker} used by this ConsumerRegistry to register / unregister consumers as PortletInvokers. This is how the portal in which WSRP is
    * running can interact with remote portlets.
    *
    * @return the {@link FederatingPortletInvoker} used by this ConsumerRegistry
    */
   FederatingPortletInvoker getFederatingPortletInvoker();

   /**
    * Creates a consumer with the specified associated producer identifier, caching producer metadata for the specified time in seconds and accessing the related producer via the
    * specified WSDL location.
    *
    * @param id                     the producer identifier associated with this consumer
    * @param expirationCacheSeconds the number of seconds before cached producer metadata is considered obsolete and needs to be retrieved again from the remote producer
    * @param wsdlURL                the String representation of the URL where the producer WSDL is located
    * @return a new WSRPConsumer minimally configured to accessed the remote producer publishing the specified WSDL
    * @throws ConsumerException if something went wrong during the creation, in particular, if attempting to create a consumer with an already existing identifier
    */
   WSRPConsumer createConsumer(String id, Integer expirationCacheSeconds, String wsdlURL) throws ConsumerException;

   /**
    * Activates the consumer associated with the specified identifier if and only if access to the remote producer is properly setup (i.e. the associated service factory MUST be
    * available). Activating a consumer means that the consumer is ready to be interacted with, meaning, in particular, that it has been registered with the
    * FederatingPortletInvoker and that the portal in which WSRP is running can therefore interact with the remote portlets the consumer proxies.
    *
    * @param id the identifier of the consumer to be activated
    * @throws ConsumerException
    */
   void activateConsumerWith(String id) throws ConsumerException;

   /**
    * Persists the changes made to ProducerInfo.
    *
    * @param producerInfo the ProducerInfo to persist
    * @return the previous value of the ProducerInfo's id if it has changed, <code>null</code> otherwise
    */
   String updateProducerInfo(ProducerInfo producerInfo) throws ConsumerException;

   /**
    * Desactivates the consumer, unregistering it from the FederatingPortletInvoker, meaning that it cannot be interacted with anymore by the portal in which WSRP is running.
    *
    * @param id the identifier associated with the consumer to deactivate
    * @throws ConsumerException
    */
   void deactivateConsumerWith(String id) throws ConsumerException;

   /**
    * Attempts to register (if the specified boolean is <code>true</code>) or deregister (otherwise) the consumer associated with the specified identifier with the associated
    * remote producer.
    *
    * @param id       the identifier of the consumer to de-/register
    * @param register <code>true</code> if a registration should be attempted, <code>false</code> if we want to deregister the specified consumer
    * @throws ConsumerException if something went wrong in particular during the WSRP interaction
    */
   void registerOrDeregisterConsumerWith(String id, boolean register) throws ConsumerException;

   /**
    * Destroys the specified consumer taking care of cleaning (if needed) things up at the same time, meaning: unregistering if registered, deactivating if activated and removing
    * the
    * consumer from persistent storage.
    *
    * @param id the identifier associated with the consumer to destroy
    * @throws ConsumerException if something went wrong, in particular during potential WSRP interactions
    */
   void destroyConsumer(String id) throws ConsumerException;

   /** Reloads consumers from persistence, re-initializing any cache from the persisted state. */
   void reloadConsumers();

   /**
    * Whether or not this registry knows about a consumer with the specified identifier.
    *
    * @param id the identifier of a consumer whose existence we want to check with this registry
    * @return
    */
   boolean containsConsumer(String id);

   /**
    * Retrieves the identifiers for all known consumers.
    *
    * @return the identifiers for all known consumers.
    */
   Collection<String> getConfiguredConsumersIds();

   /**
    * Retrieves the number of configured consumers.
    *
    * @return the number of configured consumers.
    */
   int getConfiguredConsumerNumber();
}
