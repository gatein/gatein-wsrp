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
import org.gatein.registration.Registration;
import org.gatein.registration.RegistrationException;
import org.gatein.registration.impl.RegistrationPersistenceManagerImpl;
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
            mappings = session.insert(ConsumersAndGroupsMapping.class, ConsumersAndGroupsMapping.NODE_NAME);
         }
         persister.save(); // needed right now as the session must still be open to iterate over nodes

         for (ConsumerGroupMapping cgm : mappings.getConsumerGroups())
         {
            internalAddConsumerGroup(cgm.toModel(newConsumerGroupSPI(cgm.getName()), this));
         }

         for (ConsumerMapping cm : mappings.getConsumers())
         {
            ConsumerSPI consumer = cm.toModel(newConsumerSPI(cm.getId(), cm.getName()), this);
            internalAddConsumer(consumer);

            // get the registrations and add them to local map.
            for (Registration registration : consumer.getRegistrations())
            {
               internalAddRegistration((RegistrationSPI)registration);
            }
         }
      }
      finally
      {
         persister.closeSession(false);
      }
   }

   protected ChromatticPersister getPersister()
   {
      return persister;
   }

   @Override
   protected RegistrationSPI internalRemoveRegistration(String registrationId) throws RegistrationException
   {
      remove(registrationId, RegistrationMapping.class, RegistrationSPI.class);

      return super.internalRemoveRegistration(registrationId);
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
   protected ConsumerSPI internalRemoveConsumer(String consumerId) throws RegistrationException
   {
      remove(consumerId, ConsumerMapping.class, ConsumerSPI.class);
      return super.internalRemoveConsumer(consumerId);
   }

   private <T extends BaseMapping, U> U remove(String name, Class<T> mappingClass, Class<U> modelClass)
   {
      ChromatticSession session = persister.getSession();
      try
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
               throw new IllegalArgumentException("There should be only one " + modelClass.getSimpleName() + " named " + name);
            }

            id = rows.nextRow().getValue("jcr:uuid").getString();

         }

         T toRemove = session.findById(mappingClass, id);
         Class aClass = toRemove.getModelClass();
         if (!modelClass.isAssignableFrom(aClass))
         {
            throw new IllegalArgumentException("Cannot convert a " + mappingClass.getSimpleName() + " to a " + modelClass.getSimpleName());
         }

         final U result = modelClass.cast(toRemove.toModel(null, this));

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
      ConsumerSPI consumerSPI = super.internalSaveChangesTo(consumer);

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
      RegistrationSPI registrationSPI = super.internalSaveChangesTo(registration);

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
   protected ConsumerGroupSPI internalRemoveConsumerGroup(String name) throws RegistrationException
   {
      remove(name, ConsumerGroupMapping.class, ConsumerGroupSPI.class);
      return super.internalRemoveConsumerGroup(name);
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

   @Override
   public boolean isConsumerGroupExisting(String consumerGroupId) throws RegistrationException
   {
      return exists(consumerGroupId);
   }
}
