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

package org.gatein.wsrp.consumer.registry;

import org.chromattic.api.ChromatticSession;
import org.gatein.wsrp.WSRPConsumer;
import org.gatein.wsrp.consumer.ConsumerException;
import org.gatein.wsrp.consumer.ProducerInfo;
import org.gatein.wsrp.consumer.registry.mapping.EndpointInfoMapping;
import org.gatein.wsrp.consumer.registry.mapping.ProducerInfoMapping;
import org.gatein.wsrp.consumer.registry.mapping.ProducerInfosMapping;
import org.gatein.wsrp.consumer.registry.mapping.RegistrationInfoMapping;
import org.gatein.wsrp.consumer.registry.mapping.RegistrationPropertyMapping;
import org.gatein.wsrp.consumer.registry.xml.XMLConsumerRegistry;
import org.gatein.wsrp.jcr.ChromatticPersister;
import org.gatein.wsrp.jcr.StoresByPathManager;
import org.gatein.wsrp.registration.mapping.RegistrationPropertyDescriptionMapping;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class JCRConsumerRegistry extends AbstractConsumerRegistry implements StoresByPathManager<ProducerInfo>
{
   private ChromatticPersister persister;
   private boolean loadFromXMLIfNeeded;
   private static final String PRODUCER_INFOS_PATH = ProducerInfosMapping.NODE_NAME;

   public static final List<Class> mappingClasses = new ArrayList<Class>(6);
   private InputStream configurationIS;

   static
   {
      Collections.addAll(mappingClasses, ProducerInfosMapping.class, ProducerInfoMapping.class,
         EndpointInfoMapping.class, RegistrationInfoMapping.class, RegistrationPropertyMapping.class,
         RegistrationPropertyDescriptionMapping.class);
   }

   public JCRConsumerRegistry(ChromatticPersister persister) throws Exception
   {
      this(persister, true);
   }

   protected JCRConsumerRegistry(ChromatticPersister persister, boolean loadFromXMLIfNeeded)
   {
      this.persister = persister;
      this.loadFromXMLIfNeeded = loadFromXMLIfNeeded;
   }

   /** @param is  */
   public void setConfigurationIS(InputStream is)
   {
      this.configurationIS = is;
   }

   @Override
   protected void save(ProducerInfo info, String messageOnError)
   {

      try
      {
         ChromatticSession session = persister.getSession();

         ProducerInfosMapping pims = getProducerInfosMapping(session);
         ProducerInfoMapping pim = pims.createProducerInfo(info.getId());
         String key = session.persist(pims, pim, info.getId());
         info.setKey(key);
         pim.initFrom(info);

         persister.closeSession(true);
      }
      catch (Exception e)
      {
         persister.closeSession(false);
         throw new ConsumerException(messageOnError, e);
      }
   }

   @Override
   protected void delete(ProducerInfo info)
   {
      if (!persister.delete(info, this))
      {
         throw new ConsumerException("Couldn't delete ProducerInfo " + info);
      }
   }

   @Override
   protected String update(ProducerInfo producerInfo)
   {
      String key = producerInfo.getKey();
      if (key == null)
      {
         throw new IllegalArgumentException("ProducerInfo '" + producerInfo.getId()
            + "' hasn't been persisted and thus cannot be updated.");
      }

      String oldId;
      String newId;
      boolean idUnchanged;

      ChromatticSession session = persister.getSession();

      synchronized (this)
      {
         ProducerInfoMapping pim = session.findById(ProducerInfoMapping.class, key);
         if (pim == null)
         {
            throw new IllegalArgumentException("Couldn't find ProducerInfoMapping associated with key " + key);
         }
         oldId = pim.getId();
         newId = producerInfo.getId();
         pim.initFrom(producerInfo);

         idUnchanged = oldId.equals(newId);

         if (!idUnchanged)
         {
            ProducerInfosMapping pims = getProducerInfosMapping(session);
            Map<String, ProducerInfoMapping> nameToProducerInfoMap = pims.getNameToProducerInfoMap();
            nameToProducerInfoMap.put(pim.getId(), pim);
         }

         persister.closeSession(true);
      }

      // if the consumer's id has changed, return the old one so that state can be updated
      return idUnchanged ? null : oldId;
   }

   @Override
   protected Iterator<ProducerInfo> getProducerInfosFromStorage()
   {
      ChromatticSession session = persister.getSession();
      ProducerInfosMapping producerInfosMapping = getProducerInfosMapping(session);

      List<ProducerInfoMapping> mappings = producerInfosMapping.getProducerInfos();

      persister.closeSession(true);

      return new MappingToProducerInfoIterator(mappings.iterator());
   }

   @Override
   protected ProducerInfo loadProducerInfo(String id)
   {
      ChromatticSession session = persister.getSession();
      ProducerInfoMapping pim = session.findByPath(ProducerInfoMapping.class, getPathFor(id));

      if (pim != null)
      {
         return pim.toModel(null);
      }
      else
      {
         return null;
      }
   }

   private ProducerInfosMapping getProducerInfosMapping(ChromatticSession session)
   {
      ProducerInfosMapping producerInfosMapping = session.findByPath(ProducerInfosMapping.class, PRODUCER_INFOS_PATH);

      // if we don't have info from JCR, load from XML and populate JCR
      if (producerInfosMapping == null)
      {
         producerInfosMapping = session.insert(ProducerInfosMapping.class, ProducerInfosMapping.NODE_NAME);

         if (loadFromXMLIfNeeded)
         {
            // Load from XML
            XMLConsumerRegistry fromXML = new XMLConsumerRegistry(configurationIS);
            fromXML.reloadConsumers();

            // Save to JCR
            List<ProducerInfoMapping> infos = producerInfosMapping.getProducerInfos();
            List<WSRPConsumer> consumers = fromXML.getConfiguredConsumers();
            for (WSRPConsumer consumer : consumers)
            {
               ProducerInfo info = consumer.getProducerInfo();

               ProducerInfoMapping pim = producerInfosMapping.createProducerInfo(info.getId());

               // need to add to parent first to attach newly created ProducerInfoMapping
               infos.add(pim);

               // init it from ProducerInfo
               pim.initFrom(info);
            }
         }
      }

      return producerInfosMapping;
   }

   public String getChildPath(ProducerInfo needsComputedPath)
   {
      return getPathFor(needsComputedPath);
   }

   private static String getPathFor(ProducerInfo info)
   {
      return getPathFor(info.getId());
   }

   private static String getPathFor(final String producerInfoId)
   {
      return PRODUCER_INFOS_PATH + "/" + producerInfoId;
   }

   private static ProducerInfoMapping toProducerInfoMapping(ProducerInfo producerInfo, ChromatticSession session)
   {
      ProducerInfoMapping pim = session.findById(ProducerInfoMapping.class, producerInfo.getKey());
      if (pim == null)
      {
         pim = session.insert(ProducerInfoMapping.class, getPathFor(producerInfo));
      }

      pim.initFrom(producerInfo);

      return pim;
   }

   private static class MappingToProducerInfoIterator implements Iterator<ProducerInfo>
   {
      private Iterator<ProducerInfoMapping> mappings;

      public MappingToProducerInfoIterator(Iterator<ProducerInfoMapping> infoMappingIterator)
      {
         this.mappings = infoMappingIterator;
      }

      public boolean hasNext()
      {
         return mappings.hasNext();
      }

      public ProducerInfo next()
      {
         return mappings.next().toModel(null);
      }

      public void remove()
      {
         throw new UnsupportedOperationException("Remove not supported!");
      }
   }
}
