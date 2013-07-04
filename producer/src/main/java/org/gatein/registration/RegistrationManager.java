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

package org.gatein.registration;

import org.gatein.wsrp.registration.PropertyDescription;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.Map;

/**
 * Manages consumers' registrations with a producer.
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 8966 $
 * @since 2.6
 */
public interface RegistrationManager extends RegistrationPropertyChangeListener, RegistrationPolicyChangeListener
{
   /**
    * Retrieves the RegistrationPolicy to which this RegistrationManager delegates decisions.
    *
    * @return the RegistrationPolicy to which this RegistrationManager delegates decisions.
    */
   RegistrationPolicy getPolicy();

   /**
    * Specifies which RegistrationPolicy to use with this RegistrationManager.
    *
    * @param policy the RegistrationPolicy to use with this RegistrationManager.
    */
   void setPolicy(RegistrationPolicy policy);

   /**
    * Retrieves the RegistrationPersistenceManager associated with this RegistrationManager.
    *
    * @return the RegistrationPersistenceManager associated with this RegistrationManager.
    */
   RegistrationPersistenceManager getPersistenceManager();

   /**
    * Sets the RegistrationPersistenceManager associated with this RegistrationManager.
    *
    * @param persistenceManager the RegistrationPersistenceManager to use with this RegistrationManager.
    */
   void setPersistenceManager(RegistrationPersistenceManager persistenceManager);

   /**
    * Adds a registration with the specified registration properties to the consumer identified by the specified name if the properties match the specified expectations, creating
    * the consumer if no such consumer already existed. The associated RegistrationPolicy will be asked for the consumer's identity via its {@link
    * RegistrationPolicy#getConsumerIdFrom(String, java.util.Map)} method and will be asked to validate the registration properties using {@link
    * RegistrationPolicy#validateRegistrationDataFor(java.util.Map, String, java.util.Map, RegistrationManager)}.
    *
    * @param consumerName           the name of the consumer to which we want to add a registration
    * @param registrationProperties a Map of registration properties, associating the QName of the property to its value
    * @param expectations           a Map of expected registration property descriptions, associating the QName of the property to its description so that the specified
    *                               registration properties can be validated
    * @param createConsumerIfNeeded whether or not we should create the associated consumer if no such named consumer already exists
    * @return the newly created Registration
    * @throws RegistrationException
    */
   Registration addRegistrationTo(String consumerName, Map<QName, Object> registrationProperties, final Map<QName, ? extends PropertyDescription> expectations, boolean createConsumerIfNeeded)
      throws RegistrationException;

   /**
    * Creates a consumer named with the specified name if no such consumer named this way already existed. The associated RegistrationPolicy will be asked to create an identifier
    * for the new Consumer using {@link RegistrationPolicy#getConsumerIdFrom(String, java.util.Map)} and might automatically add the consumer to a {@link ConsumerGroup} depending
    * on what {@link RegistrationPolicy#getAutomaticGroupNameFor(String)} returns.
    *
    * @param name the name of the new consumer
    * @return the newly created Consumer
    * @throws RegistrationException
    * @throws InvalidConsumerDataException
    */
   Consumer createConsumer(String name) throws RegistrationException, InvalidConsumerDataException;

   /**
    * Adds the consumer identified by the specified name to the ConsumerGroup identified by the specified consumer group's name, creating the group and/or the consumer if needed.
    * The associated RegistrationPolicy is asked to validate the group's name using {@link RegistrationPolicy#validateConsumerGroupName(String, RegistrationManager)} and the
    * consumer's name using {@link RegistrationPolicy#validateConsumerName(String, RegistrationManager)}, creating the consumer's identifier with {@link
    * RegistrationPolicy#getConsumerIdFrom(String, java.util.Map)}.
    *
    * @param consumerName           the consumer's name
    * @param groupName              the group's name to which the consumer is to be added
    * @param createGroupIfNeeded    whether or not we should create the group with the specified name if none already exists
    * @param createConsumerIfNeeded whether or not we should create a consumer with the specified name if none already exists
    * @return the Consumer (particularly useful if no consumer with the specified name previously existed)
    * @throws RegistrationException
    */
   Consumer addConsumerToGroupNamed(String consumerName, String groupName, boolean createGroupIfNeeded,
                                    boolean createConsumerIfNeeded) throws RegistrationException;

   /**
    * Creates a ConsumerGroup with the specified name if none previously existed. The RegistrationPolicy validates the name using {@link
    * RegistrationPolicy#validateConsumerGroupName(String, RegistrationManager)}
    *
    * @param groupName the name of the group we want to create
    * @return the newly created ConsumerGroup
    * @throws RegistrationException
    */
   ConsumerGroup createConsumerGroup(String groupName) throws RegistrationException;

   /**
    * Removes the consumer associated with the specified identity (which might differ from its name, depending on what {@link RegistrationPolicy#getConsumerIdFrom(String,
    * java.util.Map)} returns.
    *
    * @param identity the identifier of the consumer to be removed
    * @throws RegistrationException
    * @throws NoSuchRegistrationException
    */
   void removeConsumer(String identity) throws RegistrationException, NoSuchRegistrationException;

   /**
    * Removes the specified consumer from this RegistrationManager.
    *
    * @param consumer the consumer to be removed
    * @throws RegistrationException
    * @throws NoSuchRegistrationException
    */
   void removeConsumer(Consumer consumer) throws RegistrationException, NoSuchRegistrationException;

   /**
    * Retrieves the consumer associated with the specified identity (which might differ from its name, depending on what {@link RegistrationPolicy#getConsumerIdFrom(String,
    * java.util.Map)} returns.
    *
    * @param identity the identifier of the consumer to be retrieved
    * @return the consumer associated with the specified identity
    * @throws RegistrationException
    */
   Consumer getConsumerByIdentity(String identity) throws RegistrationException;

   /**
    * Determines whether a consumer with the specified identity (which might differ from its name, depending on what {@link RegistrationPolicy#getConsumerIdFrom(String,
    * java.util.Map)} returns) exists.
    *
    * @param consumerId the identity of the consumer which existence we want to determine
    * @return <code>true</code> if a consumer with that identity exists, <code>false</code> otherwise
    * @throws RegistrationException
    */
   boolean isConsumerExisting(String consumerId) throws RegistrationException;

   /**
    * Retrieves the consumer associated with the specified registration handle.
    *
    * @param registrationHandle the registration handle (as returned from a WSRP call) for which we want to retrieve the associated consumer
    * @return the consumer associated with the specified registration handle if it exists, <code>null</code> otherwise
    * @throws RegistrationException
    */
   Consumer getConsumerFor(String registrationHandle) throws RegistrationException;

   /**
    * Retrieves the registration associated with the specified registation handle.
    *
    * @param registrationHandle the registration handle of the presumed registration we want to retrieve
    * @return the registration associated with the specified registration handle, <code>null</code> otherwise
    * @throws RegistrationException
    */
   Registration getRegistration(String registrationHandle) throws RegistrationException;

   /**
    * Retrieves the special Registration associated with consumers that are not registered. We need such a registration since several operations are scoped to a registration. All
    * non-registered consumers share that same, special registration.
    *
    * @return the special registration associated to non-registered consumers
    * @throws RegistrationException
    */
   Registration getNonRegisteredRegistration() throws RegistrationException;

   /**
    * Removes the registration associated with the specified registration handle.
    *
    * @param registrationHandle the registration handle associated to the registration we want to remove
    * @throws RegistrationException
    * @throws NoSuchRegistrationException
    */
   void removeRegistration(String registrationHandle) throws RegistrationException, NoSuchRegistrationException;

   /**
    * Removes the specified registration.
    *
    * @param registration the registration to remove
    * @throws RegistrationException
    * @throws NoSuchRegistrationException
    */
   void removeRegistration(Registration registration) throws RegistrationException, NoSuchRegistrationException;

   /**
    * Retrieves the ConsumerGroup associated with the specified group name.
    *
    * @param groupName the name of the ConsumerGroup to retrieve
    * @return the ConsumerGroup associated with the specified name or <code>null</code> if no such group exists
    * @throws RegistrationException
    */
   ConsumerGroup getConsumerGroup(String groupName) throws RegistrationException;

   /**
    * Retrieves all known ConsumerGroups.
    *
    * @return all known ConsumerGroups
    * @throws RegistrationException
    */
   Collection<? extends ConsumerGroup> getConsumerGroups() throws RegistrationException;

   /**
    * Removes the specified ConsumerGroup.
    *
    * @param group the ConsumerGroup to be removed
    * @throws RegistrationException
    */
   void removeConsumerGroup(ConsumerGroup group) throws RegistrationException;

   /**
    * Removes the ConsumerGroup identified by the specified name.
    *
    * @param name the name of the ConsumerGroup to remove
    * @throws RegistrationException
    */
   void removeConsumerGroup(String name) throws RegistrationException;

   /**
    * Retrieves all known Consumers.
    *
    * @return all known Consumers.
    * @throws RegistrationException
    */
   Collection<? extends Consumer> getConsumers() throws RegistrationException;

   /**
    * Removes all data (Consumers and ConsumerGroups) associated with this RegistrationManager.
    *
    * @throws RegistrationException
    */
   void clear() throws RegistrationException;

   /**
    * Adds the specified {@link RegistrationDestructionListener}.
    *
    * @param listener the RegistrationDestructionListener to add
    */
   void addRegistrationDestructionListener(RegistrationDestructionListener listener);

   /**
    * Removes the specified {@link RegistrationDestructionListener}.
    *
    * @param listener the RegistrationDestructionListener to remove
    */
   void removeRegistrationDestructionListener(RegistrationDestructionListener listener);
}
