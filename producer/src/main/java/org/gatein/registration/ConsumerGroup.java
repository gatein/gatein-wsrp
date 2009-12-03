/*
 * JBoss, a division of Red Hat
 * Copyright 2009, Red Hat Middleware, LLC, and individual
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

package org.gatein.registration;

import java.util.Collection;

/**
 * A business entity that is related to several consumers.
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 8784 $
 * @since 2.6
 */
public interface ConsumerGroup
{

   /**
    * Return this ConsumerGroup's name (i.e. the name of the the business entity aggregating the associated consumers)
    *
    * @return the ConsumerGroup's name
    */
   String getName();

   /**
    * Return this ConsumerGroup's persistent identifier.
    *
    * @return this ConsumerGroup's persistent identifier
    */
   String getPersistentKey();

   /**
    * Return a collection of associated consumers.
    *
    * @return the consumer collection
    * @throws RegistrationException
    */
   Collection getConsumers() throws RegistrationException;

   /**
    * Return the member Consumer associated with the given identifier
    *
    * @param consumerId
    * @return the Consumer associated with the identity or <code>null</code> if no such Consumer is part of this
    *         ConsumerGroup
    * @throws IllegalArgumentException if the consumer identity is null
    * @throws RegistrationException
    */
   Consumer getConsumer(String consumerId) throws IllegalArgumentException, RegistrationException;

   void addConsumer(Consumer consumer) throws RegistrationException;

   void removeConsumer(Consumer consumer) throws RegistrationException;

   boolean contains(Consumer consumer);

   boolean isEmpty();

   RegistrationStatus getStatus();

   void setStatus(RegistrationStatus status);
}
