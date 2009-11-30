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
import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 8784 $
 * @since 2.6
 */
public interface RegistrationPersistenceManager
{
   /**
    * Creates a Consumer with the specified identity and name (which might be the same, depending on the policy)
    *
    * @param consumerId
    * @param consumerName
    */
   Consumer createConsumer(String consumerId, String consumerName) throws RegistrationException;

   /**
    * Retrieves the ConsumerGroup identified by the specified name.
    *
    * @param name the name of the group to be retrieved
    * @return the ConsumerGroup identified by the specified name
    * @throws RegistrationException
    */
   ConsumerGroup getConsumerGroup(String name) throws RegistrationException;

   /**
    * Creates a new ConsumerGroup with the associated name.
    *
    * @param name the name of the ConsumerGroup to be created
    * @return a new ConsumerGroup with the associated name
    * @throws RegistrationException
    */
   ConsumerGroup createConsumerGroup(String name) throws RegistrationException;

   void removeConsumerGroup(String name) throws RegistrationException;

   void removeConsumer(String consumerId) throws RegistrationException;

   void removeRegistration(String registrationId) throws RegistrationException;

   /**
    * Return an existing consumer from its id.
    *
    * @param consumerId the consumer id
    * @return the consumer or null if it does not exist
    * @throws IllegalArgumentException if the consumer id argument is null
    * @throws RegistrationException
    */
   Consumer getConsumerById(String consumerId) throws IllegalArgumentException, RegistrationException;

   Registration addRegistrationFor(String consumerId, Map registrationProperties) throws RegistrationException;

   Collection<? extends ConsumerGroup> getConsumerGroups();

   Registration getRegistration(String registrationId);

   Consumer addConsumerToGroupNamed(String consumerId, String groupName) throws RegistrationException;

   Collection<? extends Consumer> getConsumers();

   Collection<? extends Registration> getRegistrations();
}
