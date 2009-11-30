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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 8784 $
 * @since 2.6
 */
public class RegistrationPersistenceManagerImpl implements RegistrationPersistenceManager
{
   private long lastRegistrationId;
   private Map<String, Consumer> consumers = new HashMap<String, Consumer>();
   private Map<String, ConsumerGroup> groups = new HashMap<String, ConsumerGroup>();
   private Map<String, Registration> registrations = new HashMap<String, Registration>();

   public Consumer createConsumer(String consumerId, String consumerName) throws RegistrationException
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(consumerId, "Consumer identity", null);
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(consumerName, "Consumer name", null);

      ConsumerImpl consumer = new ConsumerImpl(consumerId, consumerName);
      consumer.setStatus(RegistrationStatus.PENDING);
      internalAddConsumer(consumer);

      return consumer;
   }

   public ConsumerGroup getConsumerGroup(String name) throws RegistrationException
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(name, "ConsumerGroup name", null);

      return groups.get(name);
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
         group = new ConsumerGroupImpl(name);
         internalAddConsumerGroup(group);
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

      Registration registration = internalRemoveRegistration(registrationId);
      if (registration == null)
      {
         throw new NoSuchRegistrationException("There is no Registration with id '" + registrationId + "'");
      }

      ConsumerImpl consumer = (ConsumerImpl)registration.getConsumer();
      consumer.removeRegistration(registration);
   }

   public Consumer getConsumerById(String consumerId) throws RegistrationException
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(consumerId, "Consumer identity", null);

      return consumers.get(consumerId);
   }

   public Registration addRegistrationFor(String consumerId, Map registrationProperties) throws RegistrationException
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(consumerId, "Consumer identity", null);
      ParameterValidation.throwIllegalArgExceptionIfNull(registrationProperties, "Registration properties");

      ConsumerImpl consumer = (ConsumerImpl)getConsumerById(consumerId);
      if (consumer == null)
      {
         throw new NoSuchRegistrationException("There is no Consumer with identity '" + consumerId
            + "' to add a Registration to...");
      }

      RegistrationImpl registration = new RegistrationImpl("" + lastRegistrationId++, consumer,
         RegistrationStatus.PENDING, registrationProperties);
      consumer.addRegistration(registration);

      internalAddRegistration(registration);

      return registration;
   }

   public Consumer addConsumerToGroupNamed(String consumerId, String groupName) throws RegistrationException
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(consumerId, "Consumer identity", null);
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(groupName, "ConsumerGroup name", null);

      ConsumerGroupImpl group = (ConsumerGroupImpl)getConsumerGroup(groupName);
      if (group == null)
      {
         throw new NoSuchRegistrationException("There is no ConsumerGroup named '" + groupName
            + "' to add a Consumer to...");
      }

      ConsumerImpl consumer = (ConsumerImpl)getConsumerById(consumerId);
      if (consumer == null)
      {
         throw new NoSuchRegistrationException("There is no Consumer with identity '" + consumerId
            + "' to add to ConsumerGroup named '" + groupName + "'. Did you create it?");
      }

      group.addConsumer(consumer);

      return consumer;
   }

   public Collection<? extends Consumer> getConsumers()
   {
      return Collections.unmodifiableCollection(consumers.values());
   }

   public Collection<? extends Registration> getRegistrations()
   {
      return Collections.unmodifiableCollection(registrations.values());
   }

   public Collection<? extends ConsumerGroup> getConsumerGroups()
   {
      return Collections.unmodifiableCollection(groups.values());
   }

   public Registration getRegistration(String registrationId)
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(registrationId, "Registration id", null);

      return registrations.get(registrationId);
   }

   // internal methods: extension points for subclasses

   protected void internalAddRegistration(RegistrationImpl registration)
   {
      registrations.put(registration.getId(), registration);
   }

   protected Registration internalRemoveRegistration(String registrationId)
   {
      return registrations.remove(registrationId);
   }

   protected void internalAddConsumer(Consumer consumer)
   {
      consumers.put(consumer.getId(), consumer);
   }

   protected Consumer internalRemoveConsumer(String consumerId)
   {
      return consumers.remove(consumerId);
   }

   protected void internalAddConsumerGroup(ConsumerGroup group)
   {
      groups.put(group.getName(), group);
   }

   protected ConsumerGroup internalRemoveConsumerGroup(String name)
   {
      return groups.remove(name);
   }
}
