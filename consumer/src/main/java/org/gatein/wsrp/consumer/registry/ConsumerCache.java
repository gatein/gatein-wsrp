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

import org.gatein.wsrp.WSRPConsumer;

import java.util.Collection;

/**
 * A cache for WSRPConsumers to avoid having to retrieve them (and restore their complete state) from persistence. This is crucial because it is not currently possible to
 * completely restore a consumer's state from persistence without having to go through its rather complex lifecycle and negotiation with the remote producer. This cache is however
 * guaranteed to be always provide a correct view of the persisted state, meaning that public methods will always first check if the cached data first needs to be updated from
 * persistence and will do so before returning thus ensuring that the returned values are not staled.
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 */
public interface ConsumerCache
{
   /**
    * Retrieves the up-to-date collection of consumers known by this cache. In particular, this means that the consumers returned by this method should match exactly the ones
    * (and only the ones) that are currently persisted, the cached data being completely refreshed as needed.
    *
    * @return the up-to-date consumers known by this cache
    */
   Collection<WSRPConsumer> getConsumers();

   /**
    * Retrieves the consumer identified by the specified identifier, updating its state from persistence if required.
    *
    * @param id the identifier of the consumer to retrieve
    * @return the up-to-date consumer identified by the specified identifier or <code>null</code> if no such consumer exists in persistence
    */
   WSRPConsumer getConsumer(String id);

   /**
    * Removes the consumer identified by the specified identifier from this cache.
    *
    * @param id the identifier of the consumer to remove
    * @return the consumer that was stored in this cache, or <code>null</code> if no such consumer previously was stored in this cache
    */
   WSRPConsumer removeConsumer(String id);

   /**
    * Puts the specified consumer in cache under the specified identifier.
    *
    * @param id       the consumer's identifier
    * @param consumer the consumer to put in this cache
    */
   void putConsumer(String id, WSRPConsumer consumer);

   /** Clears all consumers from this cache. */
   void clear();

   /**
    * Are we invalidated?
    *
    * @return <code>true</code> if this cache has been invalidated, <code>false</code> otherwise
    */
   boolean isInvalidated();

   /**
    * Specifies whether this cache should be invalidated.
    *
    * @param invalidated <code>true</code> to mark this cache as invalidated, <code>false</code> otherwise
    */
   void setInvalidated(boolean invalidated);

   /** Initializes this cache from persistent store. */
   void initFromStorage();
}
