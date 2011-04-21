/*
 * JBoss, a division of Red Hat
 * Copyright 2010, Red Hat Middleware, LLC, and individual
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

package org.gatein.registration.impl;

import org.gatein.common.util.ParameterValidation;
import org.gatein.registration.AbstractRegistrationPersistenceManager;
import org.gatein.registration.Consumer;
import org.gatein.registration.ConsumerGroup;
import org.gatein.registration.Registration;
import org.gatein.registration.RegistrationException;
import org.gatein.registration.RegistrationStatus;
import org.gatein.registration.spi.ConsumerGroupSPI;
import org.gatein.registration.spi.ConsumerSPI;
import org.gatein.registration.spi.RegistrationSPI;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 8784 $
 * @since 2.6
 */
public class RegistrationPersistenceManagerImpl extends AbstractRegistrationPersistenceManager
{
   private long lastRegistrationId;
   private Map<String, ConsumerSPI> consumers = new HashMap<String, ConsumerSPI>();
   private Map<String, ConsumerGroupSPI> groups = new HashMap<String, ConsumerGroupSPI>();
   private Map<String, RegistrationSPI> registrations = new HashMap<String, RegistrationSPI>();

   public Collection<ConsumerSPI> getConsumers() throws RegistrationException
   {
      return Collections.unmodifiableCollection(consumers.values());
   }

   public Collection<RegistrationSPI> getRegistrations() throws RegistrationException
   {
      return Collections.unmodifiableCollection(registrations.values());
   }

   public Collection<ConsumerGroupSPI> getConsumerGroups() throws RegistrationException
   {
      return Collections.unmodifiableCollection(groups.values());
   }

   public Registration getRegistration(String registrationId) throws RegistrationException
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(registrationId, "Registration id", null);

      return registrations.get(registrationId);
   }

   public ConsumerGroup getConsumerGroup(String name) throws RegistrationException
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(name, "ConsumerGroup name", null);

      return groups.get(name);
   }

   public Consumer getConsumerById(String consumerId) throws RegistrationException
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(consumerId, "Consumer identity", null);

      return consumers.get(consumerId);
   }

   @Override
   protected void internalAddRegistration(RegistrationSPI registration) throws RegistrationException
   {
      registrations.put(registration.getPersistentKey(), registration);
   }

   @Override
   protected RegistrationSPI internalRemoveRegistration(String registrationId) throws RegistrationException
   {
      return registrations.remove(registrationId);
   }

   @Override
   protected RegistrationSPI internalCreateRegistration(ConsumerSPI consumer, Map<QName, Object> registrationProperties) throws RegistrationException
   {
      return newRegistrationSPI(consumer, registrationProperties, "" + lastRegistrationId++);
   }

   public RegistrationSPI newRegistrationSPI(ConsumerSPI consumer, Map<QName, Object> registrationProperties, String registrationKey)
   {
      return new RegistrationImpl(registrationKey, consumer, RegistrationStatus.PENDING, registrationProperties, this);
   }

   @Override
   protected void internalAddConsumer(ConsumerSPI consumer) throws RegistrationException
   {
      consumers.put(consumer.getId(), consumer);
   }

   @Override
   protected ConsumerSPI internalRemoveConsumer(String consumerId) throws RegistrationException
   {
      return consumers.remove(consumerId);
   }

   @Override
   protected ConsumerSPI internalCreateConsumer(String consumerId, String consumerName) throws RegistrationException
   {
      ConsumerSPI consumerSPI = newConsumerSPI(consumerId, consumerName);
      consumerSPI.setPersistentKey(consumerId);
      return consumerSPI;
   }

   public ConsumerSPI newConsumerSPI(String consumerId, String consumerName)
   {
      return new ConsumerImpl(consumerId, consumerName);
   }

   @Override
   protected void internalAddConsumerGroup(ConsumerGroupSPI group) throws RegistrationException
   {
      groups.put(group.getName(), group);
   }

   @Override
   protected ConsumerGroupSPI internalRemoveConsumerGroup(String name) throws RegistrationException
   {
      return groups.remove(name);
   }

   @Override
   protected ConsumerGroupSPI internalCreateConsumerGroup(String name) throws RegistrationException
   {
      ConsumerGroupSPI groupSPI = newConsumerGroupSPI(name);
      groupSPI.setPersistentKey(name);
      return groupSPI;
   }

   public ConsumerGroupSPI newConsumerGroupSPI(String name)
   {
      return new ConsumerGroupImpl(name);
   }

   @Override
   protected ConsumerSPI getConsumerSPIById(String consumerId) throws RegistrationException
   {
      return (ConsumerSPI)getConsumerById(consumerId);
   }

   @Override
   protected ConsumerSPI internalSaveChangesTo(Consumer consumer) throws RegistrationException
   {
      return (ConsumerSPI)consumer; // nothing to do here, left up to subclasses to implement update in persistent store
   }

   @Override
   protected RegistrationSPI internalSaveChangesTo(Registration registration) throws RegistrationException
   {
      return (RegistrationSPI)registration; // nothing to do here, left up to subclasses to implement update in persistent store
   }
}
