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

package org.gatein.wsrp.registration.mapping;

import org.chromattic.api.RelationshipType;
import org.chromattic.api.annotations.FindById;
import org.chromattic.api.annotations.Id;
import org.chromattic.api.annotations.MappedBy;
import org.chromattic.api.annotations.OneToMany;
import org.chromattic.api.annotations.PrimaryType;
import org.chromattic.api.annotations.Property;
import org.gatein.common.util.ParameterValidation;
import org.gatein.registration.Consumer;
import org.gatein.registration.RegistrationException;
import org.gatein.registration.RegistrationStatus;
import org.gatein.registration.spi.ConsumerGroupSPI;
import org.gatein.registration.spi.ConsumerSPI;
import org.gatein.wsrp.jcr.mapping.BaseMapping;
import org.gatein.wsrp.registration.JCRRegistrationPersistenceManager;

import java.util.Collection;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
@PrimaryType(name = ConsumerGroupMapping.NODE_NAME)
public abstract class ConsumerGroupMapping implements BaseMapping<ConsumerGroupSPI, JCRRegistrationPersistenceManager>
{
   public static final String NODE_NAME = "wsrp:consumergroup";

   @OneToMany(type = RelationshipType.PATH)
   @MappedBy("group")
   public abstract Collection<ConsumerMapping> getConsumers();

   @Property(name = "name")
   public abstract String getName();

   public abstract void setName(String name);

   @Property(name = "status")
   public abstract RegistrationStatus getStatus();

   public abstract void setStatus(RegistrationStatus status);

   @Id
   public abstract String getPersistentKey();

   @FindById
   public abstract ConsumerMapping findConsumerById(String id);

   public void initFrom(ConsumerGroupSPI group)
   {
      setName(group.getName());
      setStatus(group.getStatus());

      try
      {
         for (Consumer consumer : group.getConsumers())
         {
            String id = consumer.getPersistentKey();
            ConsumerMapping cm = findConsumerById(id);
            ParameterValidation.throwIllegalArgExceptionIfNull(cm, "ConsumerMapping (no such mapping with id: " + id + ")");
            getConsumers().add(cm);
            cm.initFrom((ConsumerSPI)consumer);
         }
      }
      catch (RegistrationException e)
      {
         throw new RuntimeException(e);
      }
   }

   public ConsumerGroupSPI toModel(ConsumerGroupSPI initial, JCRRegistrationPersistenceManager persistenceManager)
   {
      ConsumerGroupSPI group = (initial != null ? initial : persistenceManager.newConsumerGroupSPI(getName()));
      group.setPersistentKey(getPersistentKey());
      RegistrationStatus status = getStatus();
      if (status == null)
      {
         status = RegistrationStatus.PENDING;
      }
      group.setStatus(status);

      try
      {
         for (ConsumerMapping cm : getConsumers())
         {
            Consumer consumer = persistenceManager.getConsumerById(cm.getPersistentKey());
            if (consumer == null)
            {
               consumer = cm.toModel((ConsumerSPI)consumer, persistenceManager);
            }

            group.addConsumer(consumer);
         }

         return group;
      }
      catch (RegistrationException e)
      {
         throw new RuntimeException(e);
      }
   }

   public Class<ConsumerGroupSPI> getModelClass()
   {
      return ConsumerGroupSPI.class;
   }

}
