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
 * Manages consumer registrations with a producer.
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 8966 $
 * @since 2.6
 */
public interface RegistrationManager extends RegistrationPropertyChangeListener, RegistrationPolicyChangeListener
{
   RegistrationPolicy getPolicy();

   void setPolicy(RegistrationPolicy policy);

   RegistrationPersistenceManager getPersistenceManager();

   void setPersistenceManager(RegistrationPersistenceManager persistenceManager);

   Registration addRegistrationTo(String consumerName, Map<QName, Object> registrationProperties, final Map<QName, ? extends PropertyDescription> expectations, boolean createConsumerIfNeeded)
      throws RegistrationException;

   Consumer createConsumer(String name) throws RegistrationException, InvalidConsumerDataException;

   Consumer addConsumerToGroupNamed(String consumerName, String groupName, boolean createGroupIfNeeded,
                                    boolean createConsumerIfNeeded) throws RegistrationException;

   ConsumerGroup createConsumerGroup(String groupName) throws RegistrationException;

   void removeConsumer(String identity) throws RegistrationException, NoSuchRegistrationException;

   void removeConsumer(Consumer consumer) throws RegistrationException, NoSuchRegistrationException;

   Consumer getConsumerByIdentity(String identity) throws RegistrationException;

   boolean isConsumerExisting(String consumerId) throws RegistrationException;

   Consumer getConsumerFor(String registrationHandle) throws RegistrationException;

   Registration getRegistration(String registrationHandle) throws RegistrationException;

   Registration getNonRegisteredRegistration() throws RegistrationException;

   void removeRegistration(String registrationHandle) throws RegistrationException, NoSuchRegistrationException;

   void removeRegistration(Registration registration) throws RegistrationException, NoSuchRegistrationException;

   ConsumerGroup getConsumerGroup(String groupName) throws RegistrationException;

   Collection<? extends ConsumerGroup> getConsumerGroups() throws RegistrationException;

   void removeConsumerGroup(ConsumerGroup group) throws RegistrationException;

   void removeConsumerGroup(String name) throws RegistrationException;

   Collection<? extends Consumer> getConsumers() throws RegistrationException;

   void clear() throws RegistrationException;

   void addRegistrationDestructionListener(RegistrationDestructionListener listener);

   void removeRegistrationDestructionListener(RegistrationDestructionListener listener);
}
