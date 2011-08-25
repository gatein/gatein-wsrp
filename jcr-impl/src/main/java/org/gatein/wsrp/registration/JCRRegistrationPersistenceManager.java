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

package org.gatein.wsrp.registration;

import org.chromattic.api.ChromatticSession;
import org.gatein.common.util.ParameterValidation;
import org.gatein.registration.Consumer;
import org.gatein.registration.ConsumerGroup;
import org.gatein.registration.Registration;
import org.gatein.registration.RegistrationException;
import org.gatein.registration.impl.RegistrationPersistenceManagerImpl;
import org.gatein.registration.spi.ConsumerGroupSPI;
import org.gatein.registration.spi.ConsumerSPI;
import org.gatein.registration.spi.RegistrationSPI;
import org.gatein.wsrp.jcr.ChromatticPersister;
import org.gatein.wsrp.registration.mapping.ConsumerCapabilitiesMapping;
import org.gatein.wsrp.registration.mapping.ConsumerGroupMapping;
import org.gatein.wsrp.registration.mapping.ConsumerMapping;
import org.gatein.wsrp.registration.mapping.ConsumersAndGroupsMapping;
import org.gatein.wsrp.registration.mapping.RegistrationMapping;
import org.gatein.wsrp.registration.mapping.RegistrationPropertiesMapping;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class JCRRegistrationPersistenceManager extends RegistrationPersistenceManagerImpl
{
   private ChromatticPersister persister;

   public static final List<Class> mappingClasses = new ArrayList<Class>(6);

   static
   {
      Collections.addAll(mappingClasses, ConsumersAndGroupsMapping.class, ConsumerMapping.class, ConsumerGroupMapping.class,
         RegistrationMapping.class, ConsumerCapabilitiesMapping.class, RegistrationPropertiesMapping.class);
   }


   public JCRRegistrationPersistenceManager(ChromatticPersister persister) throws Exception
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(persister, "ChromatticPersister");
      this.persister = persister;

      ChromatticSession session = persister.getSession();
      ConsumersAndGroupsMapping mappings = session.findByPath(ConsumersAndGroupsMapping.class, ConsumersAndGroupsMapping.NODE_NAME);
      if (mappings == null)
      {
         mappings = session.insert(ConsumersAndGroupsMapping.class, ConsumersAndGroupsMapping.NODE_NAME);
      }
      persister.save(); // needed right now as the session must still be open to iterate over nodes

      initLocalCaches(mappings);

      persister.closeSession(false);
   }

   public Collection<ConsumerSPI> getConsumers() throws RegistrationException
   {
      initLocalConsumerCache();

      return super.getConsumers();
   }

   public Collection<RegistrationSPI> getRegistrations() throws RegistrationException
   {
      initLocalConsumerCache();

      return super.getRegistrations();
   }

   public Collection<ConsumerGroupSPI> getConsumerGroups() throws RegistrationException
   {
      initLocalConsumerGroupCache();

      return super.getConsumerGroups();
   }

   public Registration getRegistration(String registrationId) throws RegistrationException
   {
      Registration registration = super.getRegistration(registrationId);
      // if we didn't find it in local cache, reload from JCR and check again;
      if (registration == null)
      {
         initLocalConsumerCache();
         return super.getRegistration(registrationId);
      }
      else
      {
         return registration;
      }
   }

   public ConsumerGroup getConsumerGroup(String name) throws RegistrationException
   {
      ConsumerGroup consumerGroup = super.getConsumerGroup(name);
      // if we didn't find it in local cache, reload from JCR and check again;
      if (consumerGroup == null)
      {
         initLocalConsumerGroupCache();
         return super.getConsumerGroup(name);
      }
      else
      {
         return consumerGroup;
      }
   }

   public Consumer getConsumerById(String consumerId) throws RegistrationException
   {
      Consumer consumer = super.getConsumerById(consumerId);
      // if we didn't find it in local cache, reload from JCR and check again;
      if (consumer == null)
      {
         initLocalConsumerCache();
         return super.getConsumerById(consumerId);
      }
      else
      {
         return consumer;
      }
   }

   private void initLocalCaches(ConsumersAndGroupsMapping mappings) throws RegistrationException
   {
      initLocalCache(mappings, false);

      initLocalCache(mappings, true);
   }

   private void initLocalCache(ConsumersAndGroupsMapping mappings, final boolean loadConsumers) throws RegistrationException
   {
      // if we already have mappings, no need to get them from JCR
      ChromatticSession session = null;
      if (mappings == null)
      {
         session = persister.getSession();
         mappings = session.findByPath(ConsumersAndGroupsMapping.class, ConsumersAndGroupsMapping.NODE_NAME);
      }

      if (loadConsumers)
      {
         // load consumers and registrations
         for (ConsumerMapping cm : mappings.getConsumers())
         {
            ConsumerSPI consumer = cm.toConsumer(this);
            internalAddConsumer(consumer);

            // get the registrations and add them to local map.
            for (Registration registration : consumer.getRegistrations())
            {
               internalAddRegistration((RegistrationSPI)registration);
            }
         }
      }
      else
      {
         // load consumer groups
         for (ConsumerGroupMapping cgm : mappings.getConsumerGroups())
         {
            internalAddConsumerGroup(cgm.toConsumerGroup(this));
         }
      }

      // if session is not null, we need to close it
      if (session != null)
      {
         persister.closeSession(false);
      }
   }

   private void initLocalConsumerCache() throws RegistrationException
   {
      initLocalCache(null, true);
   }

   private void initLocalConsumerGroupCache() throws RegistrationException
   {
      initLocalCache(null, false);
   }

   @Override
   protected RegistrationSPI internalRemoveRegistration(String registrationId) throws RegistrationException
   {
      Registration registration = getRegistration(registrationId);
      remove(registration.getPersistentKey(), RegistrationMapping.class);

      return super.internalRemoveRegistration(registrationId);
   }

   @Override
   protected RegistrationSPI internalCreateRegistration(ConsumerSPI consumer, Map registrationProperties) throws RegistrationException
   {
      ChromatticSession session = persister.getSession();
      RegistrationSPI registration;
      try
      {
         ConsumerMapping cm = session.findById(ConsumerMapping.class, consumer.getPersistentKey());
         RegistrationMapping rm = cm.createAndAddRegistrationMappingFrom(null);
         registration = newRegistrationSPI(consumer, registrationProperties, rm.getPersistentKey());
         rm.initFrom(registration);
         persister.closeSession(true);
      }
      catch (Exception e)
      {
         persister.closeSession(false);
         throw new RegistrationException(e);
      }

      return registration;
   }

   @Override
   protected ConsumerSPI internalRemoveConsumer(String consumerId) throws RegistrationException
   {
      remove(consumerId, ConsumerMapping.class);

      return super.internalRemoveConsumer(consumerId);
   }

   private void remove(String id, Class clazz)
   {
      ChromatticSession session = persister.getSession();
      session.remove(session.findById(clazz, id));
      persister.closeSession(true);
   }

   @Override
   protected ConsumerSPI internalCreateConsumer(String consumerId, String consumerName) throws RegistrationException
   {
      ConsumerSPI consumer = super.internalCreateConsumer(consumerId, consumerName);

      ChromatticSession session = persister.getSession();
      ConsumersAndGroupsMapping mappings = session.findByPath(ConsumersAndGroupsMapping.class, ConsumersAndGroupsMapping.NODE_NAME);
      try
      {
         ConsumerMapping cm = mappings.createConsumer(consumerId);
         mappings.getConsumers().add(cm);
         cm.initFrom(consumer);
         consumer.setPersistentKey(cm.getPersistentKey());
         persister.closeSession(true);
      }
      catch (Exception e)
      {
         persister.closeSession(false);
         throw new RegistrationException(e);
      }

      return consumer;
   }

   @Override
   protected ConsumerSPI internalSaveChangesTo(Consumer consumer) throws RegistrationException
   {
      ConsumerSPI consumerSPI = super.internalSaveChangesTo(consumer);

      ChromatticSession session = persister.getSession();
      try
      {
         ConsumerMapping cm = session.findById(ConsumerMapping.class, consumer.getPersistentKey());
         cm.initFrom(consumer);
         persister.closeSession(true);
      }
      catch (Exception e)
      {
         persister.closeSession(false);
         throw new RegistrationException(e);
      }

      return consumerSPI;
   }

   protected RegistrationSPI internalSaveChangesTo(Registration registration) throws RegistrationException
   {
      RegistrationSPI registrationSPI = super.internalSaveChangesTo(registration);

      ChromatticSession session = persister.getSession();
      try
      {
         RegistrationMapping cm = session.findById(RegistrationMapping.class, registration.getPersistentKey());
         cm.initFrom(registration);
         persister.closeSession(true);
      }
      catch (Exception e)
      {
         persister.closeSession(false);
         throw new RegistrationException(e);
      }

      return registrationSPI;
   }

   @Override
   protected ConsumerGroupSPI internalRemoveConsumerGroup(String name) throws RegistrationException
   {
      try
      {
         ConsumerGroup group = getConsumerGroup(name);
         if (group == null)
         {
            return super.internalRemoveConsumerGroup(name);
         }
         else
         {
            remove(group.getPersistentKey(), ConsumerGroupMapping.class);
         }
      }
      catch (RegistrationException e)
      {
         throw new IllegalArgumentException("Couldn't remove ConsumerGroup '" + name + "'", e);
      }

      return super.internalRemoveConsumerGroup(name);
   }

   @Override
   protected ConsumerGroupSPI internalCreateConsumerGroup(String name) throws RegistrationException
   {
      ConsumerGroupSPI group = super.internalCreateConsumerGroup(name);

      ChromatticSession session = persister.getSession();
      ConsumersAndGroupsMapping mappings = session.findByPath(ConsumersAndGroupsMapping.class, ConsumersAndGroupsMapping.NODE_NAME);
      try
      {
         ConsumerGroupMapping cgm = mappings.createConsumerGroup(name);
         mappings.getConsumerGroups().add(cgm);
         group.setPersistentKey(cgm.getPersistentKey());
         cgm.initFrom(group);
         persister.closeSession(true);
      }
      catch (Exception e)
      {
         persister.closeSession(false);
         throw new RegistrationException(e);
      }

      return group;
   }
}
