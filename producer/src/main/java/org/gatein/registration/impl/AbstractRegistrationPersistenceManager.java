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

package org.gatein.registration.impl;

import org.gatein.common.util.ParameterValidation;
import org.gatein.registration.Consumer;
import org.gatein.registration.ConsumerGroup;
import org.gatein.registration.DuplicateRegistrationException;
import org.gatein.registration.NoSuchRegistrationException;
import org.gatein.registration.Registration;
import org.gatein.registration.RegistrationException;
import org.gatein.registration.RegistrationPersistenceManager;
import org.gatein.registration.RegistrationStatus;
import org.gatein.registration.spi.ConsumerGroupSPI;
import org.gatein.registration.spi.ConsumerSPI;
import org.gatein.registration.spi.RegistrationSPI;

import javax.xml.namespace.QName;
import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public abstract class AbstractRegistrationPersistenceManager implements RegistrationPersistenceManager
{
   private long lastRegistrationId;

   public Consumer createConsumer(String consumerId, String consumerName) throws RegistrationException
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(consumerId, "Consumer identity", null);
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(consumerName, "Consumer name", null);

      ConsumerSPI consumer = internalCreateConsumer(consumerId, consumerName);
      internalAddConsumer(consumer);

      return consumer;
   }

   public void saveChangesTo(Consumer consumer) throws RegistrationException
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(consumer, "Consumer");

      if (consumer.getPersistentKey() == null)
      {
         throw new IllegalArgumentException("Consumer " + consumer + " hasn't yet been persisted and thus cannot be updated.");
      }

      internalSaveChangesTo(consumer);
   }

   public void saveChangesTo(Registration registration) throws RegistrationException
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(registration, "Registration");

      if (registration.getPersistentKey() == null)
      {
         throw new IllegalArgumentException("Registration " + registration + " hasn't yet been persisted and thus cannot be updated");
      }
      internalSaveChangesTo(registration);
   }

   public ConsumerGroup createConsumerGroup(String name) throws RegistrationException
   {
      ConsumerGroup group = getConsumerGroup(name);
      if (group != null)
      {
         throw new DuplicateRegistrationException("A ConsumerGroup named '" + name + "' has already been registered.");
      }
      else
      {
         group = internalCreateConsumerGroup(name);
         internalAddConsumerGroup((ConsumerGroupSPI)group);
         return group;
      }
   }

   public void removeConsumerGroup(String name) throws RegistrationException
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(name, "ConsumerGroup name", null);
      if (internalRemoveConsumerGroup(name) == null)
      {
         throw new NoSuchRegistrationException("There is no ConsumerGroup named '" + name + "'.");
      }
   }

   public void removeConsumer(String consumerId) throws RegistrationException
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(consumerId, "Consumer identity", null);
      if (internalRemoveConsumer(consumerId) == null)
      {
         throw new RegistrationException("There is no Consumer with identity '" + consumerId + "'.");
      }
   }

   public void removeRegistration(String registrationId) throws RegistrationException
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(registrationId, "Registration identity", null);

      RegistrationSPI registration = internalRemoveRegistration(registrationId);
      if (registration == null)
      {
         throw new NoSuchRegistrationException("There is no Registration with id '" + registrationId + "'");
      }

      ConsumerSPI consumer = registration.getConsumer();
      consumer.removeRegistration(registration);
   }

   public RegistrationSPI addRegistrationFor(String consumerId, Map<QName, Object> registrationProperties) throws RegistrationException
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(consumerId, "Consumer identity", null);
      ParameterValidation.throwIllegalArgExceptionIfNull(registrationProperties, "Registration properties");

      ConsumerSPI consumer = getConsumerSPIById(consumerId);
      if (consumer == null)
      {
         throw new NoSuchRegistrationException("There is no Consumer with identity '" + consumerId
            + "' to add a Registration to...");
      }

      RegistrationSPI registration = internalCreateRegistration(consumer, registrationProperties);
      consumer.addRegistration(registration);

      internalAddRegistration(registration);

      return registration;
   }

   public Consumer addConsumerToGroupNamed(String consumerId, String groupName) throws RegistrationException
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(consumerId, "Consumer identity", null);
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(groupName, "ConsumerGroup name", null);

      ConsumerGroup group = getConsumerGroup(groupName);
      if (group == null)
      {
         throw new NoSuchRegistrationException("There is no ConsumerGroup named '" + groupName
            + "' to add a Consumer to...");
      }

      ConsumerSPI consumer = getConsumerSPIById(consumerId);
      if (consumer == null)
      {
         throw new NoSuchRegistrationException("There is no Consumer with identity '" + consumerId
            + "' to add to ConsumerGroup named '" + groupName + "'. Did you create it?");
      }

      group.addConsumer(consumer);

      return consumer;
   }

   public boolean isConsumerExisting(String consumerId) throws RegistrationException
   {
      return getConsumerById(consumerId) != null;
   }

   // internal methods: extension points for subclasses

   protected abstract void internalAddRegistration(RegistrationSPI registration) throws RegistrationException;

   protected abstract RegistrationSPI internalRemoveRegistration(String registrationId) throws RegistrationException;

   protected RegistrationSPI internalCreateRegistration(ConsumerSPI consumer, Map<QName, Object> registrationProperties) throws RegistrationException
   {
      RegistrationSPI registrationSPI = newRegistrationSPI(consumer, registrationProperties);
      registrationSPI.setPersistentKey("" + lastRegistrationId++);
      return registrationSPI;
   }

   protected abstract void internalAddConsumer(ConsumerSPI consumer) throws RegistrationException;

   protected abstract ConsumerSPI internalRemoveConsumer(String consumerId) throws RegistrationException;

   protected ConsumerSPI internalCreateConsumer(String consumerId, String consumerName) throws RegistrationException
   {
      ConsumerSPI consumerSPI = newConsumerSPI(consumerId, consumerName);
      consumerSPI.setPersistentKey(consumerId);
      return consumerSPI;
   }

   protected abstract ConsumerSPI internalSaveChangesTo(Consumer consumer) throws RegistrationException;

   protected abstract RegistrationSPI internalSaveChangesTo(Registration registration) throws RegistrationException;

   protected abstract void internalAddConsumerGroup(ConsumerGroupSPI group) throws RegistrationException;

   protected abstract ConsumerGroupSPI internalRemoveConsumerGroup(String name) throws RegistrationException;

   protected ConsumerGroupSPI internalCreateConsumerGroup(String name) throws RegistrationException
   {
      ConsumerGroupSPI groupSPI = newConsumerGroupSPI(name);
      groupSPI.setPersistentKey(name);
      return groupSPI;
   }

   protected abstract ConsumerSPI getConsumerSPIById(String consumerId) throws RegistrationException;

   public RegistrationSPI newRegistrationSPI(ConsumerSPI consumer, Map<QName, Object> registrationProperties)
   {
      return new RegistrationImpl(consumer, RegistrationStatus.PENDING, registrationProperties, this);
   }

   public ConsumerSPI newConsumerSPI(String consumerId, String consumerName)
   {
      return new ConsumerImpl(consumerId, consumerName);
   }

   public ConsumerGroupSPI newConsumerGroupSPI(String name)
   {
      return new ConsumerGroupImpl(name);
   }
}
