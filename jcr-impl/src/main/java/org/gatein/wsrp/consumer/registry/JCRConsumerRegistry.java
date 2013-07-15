/*
 * JBoss, a division of Red Hat
 * Copyright 2012, Red Hat Middleware, LLC, and individual
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
import org.gatein.wsrp.SupportsLastModified;
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
import org.gatein.wsrp.jcr.mapping.mixins.LastModified;
import org.gatein.wsrp.jcr.mapping.mixins.ModifyRegistrationRequired;
import org.gatein.wsrp.jcr.mapping.mixins.WSSEndpointEnabled;
import org.gatein.wsrp.registration.mapping.RegistrationPropertyDescriptionMapping;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.Value;
import javax.jcr.query.Query;
import javax.jcr.query.QueryResult;
import javax.jcr.query.Row;
import javax.jcr.query.RowIterator;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Provides JCR-backed (using Chromattic) implementation of ConsumerRegistry.
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class JCRConsumerRegistry extends AbstractConsumerRegistry implements StoresByPathManager<ProducerInfo>
{
   private ChromatticPersister persister;
   private boolean loadFromXMLIfNeeded;
   private final String rootNodePath;
   static final String PRODUCER_INFOS_PATH = ProducerInfosMapping.NODE_NAME;

   /** Classes that this class knows how to map from JCR data and back */
   public static final List<Class> mappingClasses = new ArrayList<Class>(6);
   /** An InputStream issued from a configuration file or URL, to initialize this registry from */
   private InputStream configurationIS;

   static
   {
      Collections.addAll(mappingClasses, ProducerInfosMapping.class, ProducerInfoMapping.class,
         EndpointInfoMapping.class, RegistrationInfoMapping.class, RegistrationPropertyMapping.class,
         RegistrationPropertyDescriptionMapping.class, LastModified.class, ModifyRegistrationRequired.class,
         WSSEndpointEnabled.class);
   }

   public JCRConsumerRegistry(ChromatticPersister persister) throws Exception
   {
      this(persister, true);
   }

   /**
    * for tests
    *
    * @param persister
    * @param loadFromXMLIfNeeded
    */
   protected JCRConsumerRegistry(ChromatticPersister persister, boolean loadFromXMLIfNeeded)
   {
      this(persister, loadFromXMLIfNeeded, "/");
   }

   /**
    * Creates a new JCRConsumerRegistry.
    * for tests
    *
    * @param persister           the ChromatticPersister that will deal with JCR via Chromattic
    * @param loadFromXMLIfNeeded whether or not we should load initial data from an XML configuration file
    * @param rootNodePath        the root node path, only useful to be specified for tests where the root node path of the JCR hierarchy might not be standard
    */
   protected JCRConsumerRegistry(ChromatticPersister persister, boolean loadFromXMLIfNeeded, String rootNodePath)
   {
      this.persister = persister;
      this.loadFromXMLIfNeeded = loadFromXMLIfNeeded;
      this.rootNodePath = rootNodePath.endsWith("/") ? rootNodePath : rootNodePath + "/";

      // load consumers from persistence
      initConsumerCache();
   }

   /**
    * for tests
    *
    * @param loadFromXMLIfNeeded
    */
   protected void setLoadFromXMLIfNeeded(boolean loadFromXMLIfNeeded)
   {
      this.loadFromXMLIfNeeded = loadFromXMLIfNeeded;
   }

   @Override
   protected void initConsumerCache()
   {
      setConsumerCache(new InMemoryConsumerCache(this));
   }

   /**
    * Specifies an InputStream pointing to a configuration this registry needs to load.
    *
    * @param is an InputStream pointing to a configuration this registry needs to load
    */
   public void setConfigurationIS(InputStream is)
   {
      this.configurationIS = is;
   }

   public void save(ProducerInfo info, String messageOnError)
   {

      try
      {
         ChromatticSession session = persister.getSession();

         final long now = SupportsLastModified.now();

         // since we're creating a new ProducerInfo, we need to modify the parent as well
         ProducerInfosMapping pims = getProducerInfosMapping(session);
         pims.setLastModified(now);

         // use ProducerInfosMapping to create a child ProducerInfo node and initialize it
         ProducerInfoMapping pim = pims.createProducerInfo(info.getId());
         // we first need to persist the ProducerInfoMapping as a child of the ProducerInfosMapping element, using its id as path
         String key = session.persist(pims, pim, info.getId());
         info.setKey(key);
         info.setLastModified(now);
         pim.initFrom(info);

         persister.closeSession(true);
      }
      catch (Exception e)
      {
         persister.closeSession(false);
         throw new ConsumerException(messageOnError, e);
      }
   }

   public void delete(ProducerInfo info)
   {
      if (!persister.delete(info, this))
      {
         throw new ConsumerException("Couldn't delete ProducerInfo " + info);
      }
   }

   public String update(ProducerInfo producerInfo)
   {
      // retrieve the ProducerInfo by its persistence key because it's producer identifier might change (if it's been renamed for example)
      String key = producerInfo.getKey();
      if (key == null)
      {
         throw new IllegalArgumentException("ProducerInfo '" + producerInfo.getId()
            + "' hasn't been persisted and thus cannot be updated.");
      }

      String oldId;
      String newId;
      boolean idUnchanged;

      try
      {
         ChromatticSession session = persister.getSession();

         // retrieve the mapping associated with the persistence key and if it exists, reset it to the data of the specified ProducerInfo
         ProducerInfoMapping pim = session.findById(ProducerInfoMapping.class, key);
         if (pim == null)
         {
            throw new IllegalArgumentException("Couldn't find ProducerInfoMapping associated with key " + key);
         }
         oldId = pim.getId();
         newId = producerInfo.getId();
         pim.initFrom(producerInfo);

         idUnchanged = oldId.equals(newId);

         // if the ProducerInfo's last modified date is posterior to the set it's contained in, modify that one too
         ProducerInfosMapping pims = getProducerInfosMapping(session);
         final long pimsLastModified = pims.getLastModified();
         final long lastModified = producerInfo.getLastModified();
         if (lastModified > pimsLastModified)
         {
            pims.setLastModified(lastModified);
         }

         if (!idUnchanged)
         {
            // the consumer was renamed, we need to update its parent
            Map<String, ProducerInfoMapping> nameToProducerInfoMap = pims.getNameToProducerInfoMap();
            nameToProducerInfoMap.put(pim.getId(), pim);
         }

         persister.save();

         // if the consumer's id has changed, return the old one so that state can be updated
         return idUnchanged ? null : oldId;
      }
      finally
      {
         persister.closeSession(false);
      }
   }

   public Iterator<ProducerInfo> getProducerInfosFromStorage()
   {
      try
      {
         ChromatticSession session = persister.getSession();

         // get the ProducerInfoMappings from JCR
         List<ProducerInfoMapping> pims = getProducerInfosMapping(session).getProducerInfos();
         List<ProducerInfo> infos = new ArrayList<ProducerInfo>(pims.size());
         for (ProducerInfoMapping pim : pims)
         {
            infos.add(pim.toModel(null, this));
         }

         return infos.iterator();
      }
      finally
      {
         persister.closeSession(false);
      }
   }

   public ProducerInfo loadProducerInfo(String id)
   {
      try
      {
         ChromatticSession session = persister.getSession();

         ProducerInfoMapping pim = getProducerInfoMapping(id, session);
         if (pim != null)
         {
            return pim.toModel(null, this);
         }
         else
         {
            return null;
         }
      }
      finally
      {
         persister.closeSession(false);
      }
   }

   @Override
   public long getPersistedLastModifiedForProducerInfoWith(String id)
   {
      try
      {
         ChromatticSession session = persister.getSession();

         ProducerInfoMapping pim = getProducerInfoMapping(id, session);
         if (pim != null)
         {
            return pim.getLastModified();
         }
         else
         {
            log.debug("There is no ProducerInfo with id '" + id + "'. Return Long.MIN_VALUE for last modified time.");
            return Long.MIN_VALUE;
         }
      }
      finally
      {
         persister.closeSession(false);
      }
   }

   private ProducerInfoMapping getProducerInfoMapping(String id, ChromatticSession session)
   {
      return session.findByPath(ProducerInfoMapping.class, getPathFor(id));
   }

   @Override
   public boolean containsConsumer(String id)
   {
      try
      {
         ChromatticSession session = persister.getSession();

         // this operation is not part of Chromattic, so use the JCR functionality directly
         return session.getJCRSession().itemExists(rootNodePath + getPathFor(id));
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

   public Collection<String> getConfiguredConsumersIds()
   {
      try
      {
         ChromatticSession session = persister.getSession();

         // use JCR directly to only retrieve the ProducerInfo identifiers, this is a little bit convoluted, unfortunately, and we should probably check that we indeed perform better than via Chromattic
         final RowIterator rows = getProducerInfoIds(session);

         final long size = rows.getSize();
         if (size == 0)
         {
            return Collections.emptyList();
         }
         else
         {
            List<String> ids = new ArrayList<String>(size != -1 ? (int)size : 7);

            while (rows.hasNext())
            {
               final Row row = rows.nextRow();
               final Value rowValue = row.getValue("producerid");
               ids.add(rowValue.getString());
            }

            return ids;
         }
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

   /**
    * Uses JCR directly to only retrieve the identifiers of persisted ProducerInfos to avoid having to create full ProducerInfoMappings when we only want the identifiers. This
    * *should* be faster but I guess that depends on the JCR implementation and would need to be evaluated.
    *
    * @param session the ChromatticSession used to access the JCR store
    * @return a RowIterator JCR view over the ProducerInfo identifiers
    * @throws RepositoryException
    */
   private RowIterator getProducerInfoIds(ChromatticSession session) throws RepositoryException
   {
      final Session jcrSession = session.getJCRSession();

      final Query query = jcrSession.getWorkspace().getQueryManager().createQuery("select producerid from wsrp:producerinfo", Query.SQL);
      final QueryResult queryResult = query.execute();
      return queryResult.getRows();
   }

   @Override
   public int getConfiguredConsumerNumber()
   {
      try
      {
         ChromatticSession session = persister.getSession();

         final RowIterator ids = getProducerInfoIds(session);

         return (int)ids.getSize();
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

   /**
    * Load the root element containing all ProducerInfos from JCR, priming the JCR store with data from XML configuration files if no data has been persisted already, if requested.
    * Loading initial data from XML is controlled by the value of {@link #loadFromXMLIfNeeded}.
    *
    * @param session the ChromatticSession used to access the JCR store
    * @return the ProducerInfosMapping element representing the collection of ProducerInfos
    */
   private ProducerInfosMapping getProducerInfosMapping(ChromatticSession session)
   {
      ProducerInfosMapping producerInfosMapping = session.findByPath(ProducerInfosMapping.class, PRODUCER_INFOS_PATH);

      if (producerInfosMapping == null)
      {
         // we don't currently have any persisted data in JCR so first create the JCR node
         producerInfosMapping = session.insert(ProducerInfosMapping.class, ProducerInfosMapping.NODE_NAME);

         if (loadFromXMLIfNeeded)
         {
            // loading initial data is requested so do it
            XMLConsumerRegistry fromXML = new XMLConsumerRegistry(configurationIS);
            fromXML.reloadConsumers();

            // Save to JCR
            List<ProducerInfoMapping> infos = producerInfosMapping.getProducerInfos();
            List<WSRPConsumer> xmlConsumers = fromXML.getConfiguredConsumers();
            for (WSRPConsumer consumer : xmlConsumers)
            {
               ProducerInfo info = consumer.getProducerInfo();

               // create the ProducerInfoMapping children node
               ProducerInfoMapping pim = producerInfosMapping.createProducerInfo(info.getId());

               // need to add to parent first to attach newly created ProducerInfoMapping
               infos.add(pim);

               // init it from ProducerInfo
               pim.initFrom(info);

               // update ProducerInfo with the persistence key
               info.setKey(pim.getKey());

               // populate the cache with the newly created consumer
               consumerCache.putConsumer(info.getId(), consumer);
            }

            producerInfosMapping.setLastModified(SupportsLastModified.now());
            session.save();
         }
      }

      return producerInfosMapping;
   }

   public String getChildPath(ProducerInfo needsComputedPath)
   {
      return getPathFor(needsComputedPath);
   }

   public LastModified lastModifiedToUpdateOnDelete(ChromatticSession session)
   {
      final ProducerInfosMapping pims = session.findByPath(ProducerInfosMapping.class, PRODUCER_INFOS_PATH);
      if (pims != null)
      {
         // GTNWSRP-239
         return pims.getLastModifiedMixin();
      }
      else
      {
         return null;
      }
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

   /**
    * For tests
    *
    * @return
    */
   ChromatticPersister getPersister()
   {
      return persister;
   }
}
