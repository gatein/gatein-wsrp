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
import org.gatein.registration.ConsumerCapabilities;
import org.gatein.registration.ConsumerGroup;
import org.gatein.registration.Registration;
import org.gatein.registration.RegistrationException;
import org.gatein.registration.RegistrationStatus;
import org.gatein.registration.RegistrationUtils;
import org.gatein.registration.spi.ConsumerSPI;
import org.gatein.registration.spi.RegistrationSPI;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 8784 $
 * @since 2.6
 */
public class ConsumerImpl implements ConsumerSPI
{

   private String name;
   private String identity;
   private String consumerAgent;
   private Set<Registration> registrations;
   private RegistrationStatus status;
   private ConsumerGroup group;
   private ConsumerCapabilities capabilities;
   private String key;


   private ConsumerImpl()
   {
      init();
   }

   ConsumerImpl(String identity, String name)
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(name, "name", "Consumer");
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(name, "identity", "Consumer");

      this.name = name;
      this.identity = identity;
      init();
   }

   private void init()
   {
      registrations = new HashSet<Registration>(7);
      status = RegistrationStatus.PENDING;
      capabilities = new ConsumerCapabilitiesImpl();
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

      ConsumerImpl consumer = (ConsumerImpl)o;

      return identity.equals(consumer.identity);
   }

   public int hashCode()
   {
      return identity.hashCode();
   }

   public String getName()
   {
      return name;
   }

   public String getId()
   {
      return identity;
   }


   public String getConsumerAgent()
   {
      return consumerAgent;
   }

   public void setConsumerAgent(String consumerAgent) throws IllegalArgumentException, IllegalStateException
   {
      if (consumerAgent != null && !consumerAgent.equals(this.consumerAgent))
      {
         RegistrationUtils.validateConsumerAgent(consumerAgent);
         this.consumerAgent = consumerAgent;
      }
   }

   public String getPersistentKey()
   {
      return key;
   }

   public ConsumerCapabilities getCapabilities()
   {
      return capabilities;
   }

   public void setCapabilities(ConsumerCapabilities capabilities)
   {
      this.capabilities = capabilities;
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

   public Collection<Registration> getRegistrations() throws RegistrationException
   {
      return Collections.unmodifiableSet(registrations);
   }

   public ConsumerGroup getGroup()
   {
      return group;
   }

   public void addRegistration(RegistrationSPI registration)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(registration, "Registration");

      registrations.add(registration);
   }

   public void setPersistentKey(String key)
   {
      this.key = key;
   }

   public void removeRegistration(RegistrationSPI registration) throws RegistrationException
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(registration, "Registration");

      registrations.remove(registration);

      // status should become pending if there are no registrations
      if (registrations.isEmpty())
      {
         setStatus(RegistrationStatus.PENDING);
      }
   }

   public void setGroup(ConsumerGroup group) throws RegistrationException
   {
      if (this.group != null)
      {
         // if we're trying to set the same group, just return
         if (this.group.equals(group))
         {
            return;
         }

         if (this.group.contains(this))
         {
            this.group.removeConsumer(this);
         }
      }

      this.group = group;

      if (group != null && !this.group.contains(this))
      {
         group.addConsumer(this);
      }
   }
}
