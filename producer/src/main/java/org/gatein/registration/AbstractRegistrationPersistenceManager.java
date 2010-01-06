/*
* JBoss, a division of Red Hat
* Copyright 2008, Red Hat Middleware, LLC, and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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

import org.gatein.common.util.ParameterValidation;
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
   public Consumer createConsumer(String consumerId, String consumerName) throws RegistrationException
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(consumerId, "Consumer identity", null);
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(consumerName, "Consumer name", null);

      ConsumerSPI consumer = internalCreateConsumer(consumerId, consumerName);
      internalAddConsumer(consumer);

      return consumer;
   }

   public void saveChangesTo(Consumer consumer)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(consumer, "Consumer");

      if (consumer.getPersistentKey() == null)
      {
         throw new IllegalArgumentException("Consumer " + consumer + " hasn't yet been persisted and thus cannot be updated.");
      }

      internalSaveChangesTo(consumer);
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

   public Registration addRegistrationFor(String consumerId, Map<QName, Object> registrationProperties) throws RegistrationException
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

   // internal methods: extension points for subclasses

   protected abstract void internalAddRegistration(RegistrationSPI registration);

   protected abstract RegistrationSPI internalRemoveRegistration(String registrationId);

   protected abstract RegistrationSPI internalCreateRegistration(ConsumerSPI consumer, Map<QName, Object> registrationProperties);

   protected abstract void internalAddConsumer(ConsumerSPI consumer);

   protected abstract ConsumerSPI internalRemoveConsumer(String consumerId);

   protected abstract ConsumerSPI internalCreateConsumer(String consumerId, String consumerName);

   protected abstract ConsumerSPI internalSaveChangesTo(Consumer consumer);

   protected abstract void internalAddConsumerGroup(ConsumerGroupSPI group);

   protected abstract ConsumerGroupSPI internalRemoveConsumerGroup(String name);

   protected abstract ConsumerGroupSPI internalCreateConsumerGroup(String name);

   protected abstract ConsumerSPI getConsumerSPIById(String consumerId) throws RegistrationException;
}
