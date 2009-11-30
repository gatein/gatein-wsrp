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
import org.gatein.registration.NoSuchRegistrationException;
import org.gatein.registration.RegistrationException;
import org.gatein.registration.RegistrationStatus;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision:5672 $
 */
public class ConsumerGroupImpl implements ConsumerGroup
{

   private String name;
   private Map<String, Consumer> consumers;
   private RegistrationStatus status;


   private ConsumerGroupImpl()
   {
      init();
   }

   ConsumerGroupImpl(String name)
   {
      this.name = name;
      init();
   }

   private void init()
   {
      this.consumers = new HashMap<String, Consumer>();
      status = RegistrationStatus.PENDING;
   }

   public String getName()
   {
      return name;
   }


   public boolean equals(Object o)
   {
      if (this == o)
      {
         return true;
      }
      if (o == null || getClass() != o.getClass())
      {
         return false;
      }

      ConsumerGroupImpl that = (ConsumerGroupImpl)o;

      return name.equals(that.name);
   }

   public int hashCode()
   {
      return name.hashCode();
   }

   public RegistrationStatus getStatus()
   {
      return status;
   }

   public void setStatus(RegistrationStatus status)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(status, "RegistrationStatus");
      this.status = status;
   }

   public Collection getConsumers() throws RegistrationException
   {
      return Collections.unmodifiableCollection(consumers.values());
   }

   public Consumer getConsumer(String consumerId) throws IllegalArgumentException, RegistrationException
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(consumerId, "Consumer name", null);
      return consumers.get(consumerId);
   }

   public boolean isEmpty()
   {
      return consumers.isEmpty();
   }

   public void addConsumer(Consumer consumer) throws RegistrationException
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(consumer, "Consumer");
      String identity = consumer.getId();
      if (consumers.containsKey(identity))
      {
         throw new IllegalArgumentException("ConsumerGroup named '" + name
            + "' already contains a Consumer named '" + consumer.getName() + "' (identity: '" + identity + "')");
      }

      consumers.put(identity, consumer);
      consumer.setGroup(this);
   }

   public void removeConsumer(Consumer consumer) throws RegistrationException
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(consumer, "Consumer");

      if (consumers.remove(consumer.getId()) == null)
      {
         throw new NoSuchRegistrationException("ConsumerGroup named '" + name
            + "' does not contain a Consumer named '" + consumer.getName() + "' (identity: '" + consumer.getId()
            + "')");
      }

      consumer.setGroup(null);
   }

   public boolean contains(Consumer consumer)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(consumer, "Consumer");

      return consumers.containsKey(consumer.getId());
   }
}
