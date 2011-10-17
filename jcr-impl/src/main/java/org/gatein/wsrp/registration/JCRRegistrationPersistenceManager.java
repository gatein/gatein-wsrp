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
import org.gatein.registration.impl.AbstractRegistrationPersistenceManager;
import org.gatein.registration.spi.ConsumerGroupSPI;
import org.gatein.registration.spi.ConsumerSPI;
import org.gatein.registration.spi.RegistrationSPI;
import org.gatein.wsrp.jcr.ChromatticPersister;
import org.gatein.wsrp.jcr.mapping.BaseMapping;
import org.gatein.wsrp.registration.mapping.ConsumerCapabilitiesMapping;
import org.gatein.wsrp.registration.mapping.ConsumerGroupMapping;
import org.gatein.wsrp.registration.mapping.ConsumerMapping;
import org.gatein.wsrp.registration.mapping.ConsumersAndGroupsMapping;
import org.gatein.wsrp.registration.mapping.RegistrationMapping;
import org.gatein.wsrp.registration.mapping.RegistrationPropertiesMapping;

import javax.jcr.RepositoryException;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.RowIterator;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class JCRRegistrationPersistenceManager extends AbstractRegistrationPersistenceManager
{
   private ChromatticPersister persister;
   private final String rootNodePath;

   public static final List<Class> mappingClasses = new ArrayList<Class>(6);

   static
   {
      Collections.addAll(mappingClasses, ConsumersAndGroupsMapping.class, ConsumerMapping.class, ConsumerGroupMapping.class,
         RegistrationMapping.class, ConsumerCapabilitiesMapping.class, RegistrationPropertiesMapping.class);
   }

   public JCRRegistrationPersistenceManager(ChromatticPersister persister) throws Exception
   {
      this(persister, "/");
   }

   protected JCRRegistrationPersistenceManager(ChromatticPersister persister, String rootNodePath) throws Exception
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(persister, "ChromatticPersister");

      this.rootNodePath = rootNodePath.endsWith("/") ? rootNodePath : rootNodePath + "/";
      this.persister = persister;

      ChromatticSession session = persister.getSession();
      try
      {
         ConsumersAndGroupsMapping mappings = session.findByPath(ConsumersAndGroupsMapping.class, ConsumersAndGroupsMapping.NODE_NAME);
         if (mappings == null)
         {
            session.insert(ConsumersAndGroupsMapping.class, ConsumersAndGroupsMapping.NODE_NAME);
         }
      }
      finally
      {
         persister.closeSession(true);
      }
   }

   protected ChromatticPersister getPersister()
   {
      return persister;
   }

   @Override
   public void removeRegistration(String registrationId) throws RegistrationException
   {
      internalRemoveRegistration(registrationId);
   }

   protected RegistrationSPI internalRemoveRegistration(String registrationId) throws RegistrationException
   {
      // can't use remove method as we get id directly instead of name
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(registrationId, "identifier", null);

      final Registration reg = getRegistration(registrationId);
      if (reg != null)
      {
         try
         {
            final ChromatticSession session = persister.getSession();
            final RegistrationMapping mapping = session.findById(RegistrationMapping.class, registrationId);
            session.remove(mapping);
            persister.save();
         }
         finally
         {
            persister.closeSession(false);
         }
         return (RegistrationSPI)reg;
      }
      else
      {
         return null;
      }
   }

   @Override
   protected RegistrationSPI internalCreateRegistration(ConsumerSPI consumer, Map<QName, Object> registrationProperties) throws RegistrationException
   {
      RegistrationSPI registration = super.internalCreateRegistration(consumer, registrationProperties);
      ChromatticSession session = persister.getSession();
      try
      {
         ConsumerMapping cm = session.findById(ConsumerMapping.class, consumer.getPersistentKey());
         RegistrationMapping rm = cm.createAndAddRegistrationMappingFrom(null);
         rm.initFrom(registration);
         registration.setPersistentKey(rm.getPersistentKey());
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
   protected void internalAddConsumer(ConsumerSPI consumer) throws RegistrationException
   {
      // nothing to do
   }

   @Override
   protected ConsumerSPI internalRemoveConsumer(String consumerId) throws RegistrationException
   {
      return remove(consumerId, ConsumerMapping.class, ConsumerSPI.class);
   }

   private <T extends BaseMapping, U> U remove(String name, Class<T> mappingClass, Class<U> modelClass)
   {
      ChromatticSession session = persister.getSession();
      try
      {
         T toRemove = getMapping(session, mappingClass, name);
         if (toRemove == null)
         {
            return null;
         }

         final U result = getModelFrom(toRemove, mappingClass, modelClass);

         session.remove(toRemove);
         persister.closeSession(true);

         return result;
      }
      catch (Exception e)
      {
         persister.closeSession(false);
         throw new RuntimeException(e);
      }
   }

   private <T extends BaseMapping, U> U getModelFrom(T mapping, Class<T> mappingClass, Class<U> modelClass)
   {
      Class aClass = mapping.getModelClass();
      if (!modelClass.isAssignableFrom(aClass))
      {
         throw new IllegalArgumentException("Cannot convert a " + mappingClass.getSimpleName() + " to a " + modelClass.getSimpleName());
      }

      return modelClass.cast(mapping.toModel(null, this));
   }

   private <T extends BaseMapping> T getMapping(ChromatticSession session, Class<T> mappingClass, String name) throws RepositoryException, NoSuchFieldException, IllegalAccessException
   {
      String jcrType = (String)mappingClass.getField(BaseMapping.JCR_TYPE_NAME_CONSTANT_NAME).get(null);
      String id;
      final Query query = session.getJCRSession().getWorkspace().getQueryManager().createQuery("select jcr:uuid from " + jcrType + " where jcr:path = '/%/" + name + "'", Query.SQL);
      final QueryResult queryResult = query.execute();
      final RowIterator rows = queryResult.getRows();
      final long size = rows.getSize();
      if (size == 0)
      {
         return null;
      }
      else
      {
         if (size != 1)
         {
            throw new IllegalArgumentException("There should be only one " + mappingClass.getSimpleName() + " named " + name);
         }

         id = rows.nextRow().getValue("jcr:uuid").getString();
      }

      return session.findById(mappingClass, id);
   }

   @Override
   protected ConsumerSPI internalCreateConsumer(String consumerId, String consumerName) throws RegistrationException
   {
      ConsumerSPI consumer = super.internalCreateConsumer(consumerId, consumerName);

      ChromatticSession session = persister.getSession();
      try
      {
         ConsumersAndGroupsMapping mappings = session.findByPath(ConsumersAndGroupsMapping.class, ConsumersAndGroupsMapping.NODE_NAME);
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
      ConsumerSPI consumerSPI = (ConsumerSPI)consumer;

      ChromatticSession session = persister.getSession();
      try
      {
         ConsumerMapping cm = session.findById(ConsumerMapping.class, consumer.getPersistentKey());
         cm.initFrom(consumerSPI);
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
      RegistrationSPI registrationSPI = (RegistrationSPI)registration;

      ChromatticSession session = persister.getSession();
      try
      {
         RegistrationMapping cm = session.findById(RegistrationMapping.class, registration.getPersistentKey());
         cm.initFrom(registrationSPI);
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
   protected void internalAddConsumerGroup(ConsumerGroupSPI group) throws RegistrationException
   {
      // nothing to do
   }

   @Override
   protected ConsumerGroupSPI internalRemoveConsumerGroup(String name) throws RegistrationException
   {
      return remove(name, ConsumerGroupMapping.class, ConsumerGroupSPI.class);
   }

   @Override
   protected ConsumerGroupSPI internalCreateConsumerGroup(String name) throws RegistrationException
   {
      ConsumerGroupSPI group = super.internalCreateConsumerGroup(name);

      ChromatticSession session = persister.getSession();
      try
      {
         ConsumersAndGroupsMapping mappings = session.findByPath(ConsumersAndGroupsMapping.class, ConsumersAndGroupsMapping.NODE_NAME);
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

   @Override
   protected ConsumerSPI getConsumerSPIById(String consumerId) throws RegistrationException
   {
      return getModel(consumerId, ConsumerSPI.class, ConsumerMapping.class);
   }

   private <T, B extends BaseMapping> T getModel(String id, Class<T> modelClass, Class<B> mappingClass) throws RegistrationException
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(id, "identifier", null);

      final ChromatticSession session = persister.getSession();

      try
      {
         return getModel(id, modelClass, mappingClass, session);
      }
      catch (Exception e)
      {
         throw new RegistrationException(e);
      }
      finally
      {
         persister.closeSession(false);
      }
   }

   private <T, B extends BaseMapping> T getModel(String id, Class<T> modelClass, Class<B> mappingClass, ChromatticSession session) throws RegistrationException
   {
      try
      {
         final B mapping = getMapping(session, mappingClass, id);
         if (mapping == null)
         {
            return null;
         }
         else
         {
            return getModelFrom(mapping, mappingClass, modelClass);
         }
      }
      catch (Exception e)
      {
         throw new RegistrationException(e);
      }
   }

   public ConsumerGroup getConsumerGroup(String name) throws RegistrationException
   {
      return getModel(name, ConsumerGroup.class, ConsumerGroupMapping.class);
   }

   public Consumer getConsumerById(String consumerId) throws IllegalArgumentException, RegistrationException
   {
      return getConsumerSPIById(consumerId);
   }

   public Collection<? extends ConsumerGroup> getConsumerGroups() throws RegistrationException
   {
      final ChromatticSession session = persister.getSession();

      try
      {
         ConsumersAndGroupsMapping mappings = session.findByPath(ConsumersAndGroupsMapping.class, ConsumersAndGroupsMapping.NODE_NAME);
         final List<ConsumerGroupMapping> groupMappings = mappings.getConsumerGroups();
         List<ConsumerGroup> groups = new ArrayList<ConsumerGroup>(groupMappings.size());
         for (ConsumerGroupMapping cgm : groupMappings)
         {
            groups.add(cgm.toModel(newConsumerGroupSPI(cgm.getName()), this));
         }
         return groups;
      }
      finally
      {
         persister.closeSession(false);
      }
   }

   public Registration getRegistration(String registrationId) throws RegistrationException
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(registrationId, "identifier", null);

      final ChromatticSession session = persister.getSession();

      try
      {
         final RegistrationMapping mapping = session.findById(RegistrationMapping.class, registrationId);
         if (mapping == null)
         {
            return null;
         }
         else
         {
            final ConsumerMapping parent = mapping.getParent();
            return parent.toModel(null, this).getRegistration(registrationId);
         }
      }
      catch (Exception e)
      {
         throw new RegistrationException(e);
      }
      finally
      {
         persister.closeSession(false);
      }
   }

   public Collection<? extends Consumer> getConsumers() throws RegistrationException
   {
      final ChromatticSession session = persister.getSession();

      try
      {
         ConsumersAndGroupsMapping mappings = session.findByPath(ConsumersAndGroupsMapping.class, ConsumersAndGroupsMapping.NODE_NAME);
         final List<ConsumerMapping> consumerMappings = mappings.getConsumers();
         List<Consumer> consumers = new ArrayList<Consumer>(consumerMappings.size());
         for (ConsumerMapping consumerMapping : consumerMappings)
         {
            consumers.add(consumerMapping.toModel(newConsumerSPI(consumerMapping.getId(), consumerMapping.getName()), this));
         }
         return consumers;
      }
      finally
      {
         persister.closeSession(false);
      }
   }

   public Collection<? extends Registration> getRegistrations() throws RegistrationException
   {
      final Collection<? extends Consumer> consumers = getConsumers();
      List<Registration> registrations = new ArrayList<Registration>(consumers.size() * 2);
      for (Consumer consumer : consumers)
      {
         registrations.addAll(consumer.getRegistrations());
      }
      return registrations;
   }

   public boolean isConsumerExisting(String consumerId) throws RegistrationException
   {
      return exists(consumerId);
   }

   private boolean exists(String name)
   {
      ChromatticSession session = persister.getSession();
      try
      {
         return session.getJCRSession().itemExists(rootNodePath + ConsumersAndGroupsMapping.NODE_NAME + "/" + name);
      }
      catch (RepositoryException e)
      {
         throw new RuntimeException(e);
      }
      finally
      {
         persister.closeSession(false);
      }
   }

   public boolean isConsumerGroupExisting(String consumerGroupId) throws RegistrationException
   {
      return exists(consumerGroupId);
   }

   @Override
   protected void internalAddRegistration(RegistrationSPI registration) throws RegistrationException
   {
      // nothing to do
   }
}