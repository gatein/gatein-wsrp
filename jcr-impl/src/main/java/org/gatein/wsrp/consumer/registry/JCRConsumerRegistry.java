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
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class JCRConsumerRegistry extends AbstractConsumerRegistry implements StoresByPathManager<ProducerInfo>
{
   private ChromatticPersister persister;
   private boolean loadFromXMLIfNeeded;
   private final String rootNodePath;
   static final String PRODUCER_INFOS_PATH = ProducerInfosMapping.NODE_NAME;

   public static final List<Class> mappingClasses = new ArrayList<Class>(6);
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
    * for tests
    *
    * @param persister
    * @param loadFromXMLIfNeeded
    * @param rootNodePath
    */
   protected JCRConsumerRegistry(ChromatticPersister persister, boolean loadFromXMLIfNeeded, String rootNodePath)
   {
      this.persister = persister;
      this.loadFromXMLIfNeeded = loadFromXMLIfNeeded;
      this.rootNodePath = rootNodePath.endsWith("/") ? rootNodePath : rootNodePath + "/";
      initConsumerCache();
   }

   @Override
   protected void initConsumerCache()
   {
      setConsumerCache(new InMemoryConsumerCache(this));
   }

   /** @param is  */
   public void setConfigurationIS(InputStream is)
   {
      this.configurationIS = is;
   }

   public void save(ProducerInfo info, String messageOnError)
   {

      try
      {
         ChromatticSession session = persister.getSession();

         final long now = System.currentTimeMillis();

         ProducerInfosMapping pims = getProducerInfosMapping(session);
         pims.setLastModified(now);

         ProducerInfoMapping pim = pims.createProducerInfo(info.getId());
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

      final long now = System.currentTimeMillis();

      ProducerInfoMapping pim = session.findById(ProducerInfoMapping.class, key);
      if (pim == null)
      {
         throw new IllegalArgumentException("Couldn't find ProducerInfoMapping associated with key " + key);
      }
      oldId = pim.getId();
      newId = producerInfo.getId();
      producerInfo.setLastModified(now);
      pim.initFrom(producerInfo);

      idUnchanged = oldId.equals(newId);

      ProducerInfosMapping pims = getProducerInfosMapping(session);
      pims.setLastModified(now);

      if (!idUnchanged)
      {
         Map<String, ProducerInfoMapping> nameToProducerInfoMap = pims.getNameToProducerInfoMap();
         nameToProducerInfoMap.put(pim.getId(), pim);
      }

      persister.closeSession(true);

      // if the consumer's id has changed, return the old one so that state can be updated
      return idUnchanged ? null : oldId;
   }

   public Iterator<ProducerInfo> getProducerInfosFromStorage()
   {
      ChromatticSession session = persister.getSession();

      try
      {
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
      ChromatticSession session = persister.getSession();
      try
      {
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
      ChromatticSession session = persister.getSession();
      try
      {
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
      ChromatticSession session = persister.getSession();
      try
      {
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
            List<WSRPConsumer> xmlConsumers = fromXML.getConfiguredConsumers();
            for (WSRPConsumer consumer : xmlConsumers)
            {
               ProducerInfo info = consumer.getProducerInfo();

               ProducerInfoMapping pim = producerInfosMapping.createProducerInfo(info.getId());

               // need to add to parent first to attach newly created ProducerInfoMapping
               infos.add(pim);

               // init it from ProducerInfo
               pim.initFrom(info);

               // update ProducerInfo with the persistence key
               info.setKey(pim.getKey());

               consumerCache.putConsumer(info.getId(), consumer);
            }

            producerInfosMapping.setLastModified(System.currentTimeMillis());
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
