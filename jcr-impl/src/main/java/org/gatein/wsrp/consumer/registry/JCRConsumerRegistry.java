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
import org.gatein.wsrp.jcr.mapping.mixins.BaseMixin;
import org.gatein.wsrp.jcr.mapping.mixins.LastModified;
import org.gatein.wsrp.jcr.mapping.mixins.ModifyRegistrationRequired;
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
   private long lastModified;
//   private Map<String, ProducerInfo> infoCache;

   static
   {
      Collections.addAll(mappingClasses, ProducerInfosMapping.class, ProducerInfoMapping.class,
         EndpointInfoMapping.class, RegistrationInfoMapping.class, RegistrationPropertyMapping.class,
         RegistrationPropertyDescriptionMapping.class, LastModified.class, ModifyRegistrationRequired.class);
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

         ProducerInfosMapping pims = getProducerInfosMapping(session);
         ProducerInfoMapping pim = pims.createProducerInfo(info.getId());
         String key = session.persist(pims, pim, info.getId());
         info.setKey(key);
         pim.initFrom(info);

         // update cache
         final long now = System.currentTimeMillis();
         lastModified = now;
//         getRefreshedInfoCache(session).put(info.getId(), info);

         // GTNWSRP-239
         getMixin(pims, session, LastModified.class).setLastModified(now);
         getMixin(pim, session, LastModified.class).setLastModified(now);
         getMixin(pim, session, ModifyRegistrationRequired.class).setModifyRegistrationRequired(info.isModifyRegistrationRequired());
         info.setLastModified(now);

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

         final long now = System.currentTimeMillis();
         if (!idUnchanged)
         {
            ProducerInfosMapping pims = getProducerInfosMapping(session);
            Map<String, ProducerInfoMapping> nameToProducerInfoMap = pims.getNameToProducerInfoMap();
            nameToProducerInfoMap.put(pim.getId(), pim);

            // update cache
            /*getRefreshedInfoCache(session).remove(oldId);
            getRefreshedInfoCache(session).put(newId, producerInfo);*/

            // GTNWSRP-239
            getMixin(pims, session, LastModified.class).setLastModified(now);
            lastModified = now;
         }

         // GTNWSRP-239
         getMixin(pim, session, ModifyRegistrationRequired.class).setModifyRegistrationRequired(producerInfo.isModifyRegistrationRequired());
         getMixin(pim, session, LastModified.class).setLastModified(now);
         producerInfo.setLastModified(now);

         persister.closeSession(true);
      }

      // if the consumer's id has changed, return the old one so that state can be updated
      return idUnchanged ? null : oldId;
   }

   public Iterator<ProducerInfo> getProducerInfosFromStorage()
   {
      ChromatticSession session = persister.getSession();
      final Iterator<ProducerInfo> iterator = new ProducerInfoIterator(getRefreshedInfoCache(session).getConsumers().iterator());
      persister.closeSession(false);
      return iterator;
   }

   /*private Map<String, ProducerInfo> getRefreshedInfoCache(ChromatticSession session)
   {
      ProducerInfosMapping producerInfosMapping = getProducerInfosMapping(session);

      // check if we need to refresh the local cache
      if (lastModified < getMixin(producerInfosMapping, session, LastModified.class).getLastModified())
      {
         List<ProducerInfoMapping> mappings = producerInfosMapping.getProducerInfos();


         for (ProducerInfoMapping mapping : mappings)
         {
            infoCache.put(mapping.getId(), mapping.toModel(null, this));
         }

         lastModified = System.currentTimeMillis();
      }

      return infoCache;
   }*/

   private ConsumerCache getRefreshedInfoCache(ChromatticSession session)
   {
      ProducerInfosMapping producerInfosMapping = getProducerInfosMapping(session);

      // check if we need to refresh the local cache
      if (lastModified < getMixin(producerInfosMapping, session, LastModified.class).getLastModified())
      {
         List<ProducerInfoMapping> mappings = producerInfosMapping.getProducerInfos();

         for (ProducerInfoMapping pim : mappings)
         {
            if (lastModified < getMixin(pim, session, LastModified.class).getLastModified())
            {
               consumers.putConsumer(pim.getId(), createConsumerFrom(pim.toModel(null, this)));
            }
         }

         lastModified = System.currentTimeMillis();
      }

      return consumers;
   }

   public ProducerInfo loadProducerInfo(String id)
   {
      ChromatticSession session = persister.getSession();
      try
      {
         ProducerInfoMapping pim = session.findByPath(ProducerInfoMapping.class, getPathFor(id));

         if (pim != null)
         {
            WSRPConsumer consumer = getRefreshedInfoCache(session).getConsumer(id);

            if (consumer == null)
            {
               return null;
            }
            else
            {
               return consumer.getProducerInfo();
               /*ProducerInfo producerInfo = consumer.getProducerInfo();
               if(producerInfo == null || producerInfo.getLastModified() < getMixin(pim, session, LastModified.class).getLastModified())
               {
                  producerInfo = pim.toModel(producerInfo, this);
                  getRefreshedInfoCache(session).put(id, producerInfo);
                  return producerInfo;
               }
               else
               {
                  return  producerInfo;
               }*/
            }

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

   private <M extends BaseMixin> M getMixin(Object objectToCheck, ChromatticSession session, Class<M> type)
   {
      M mixin = session.getEmbedded(objectToCheck, type);
      if (mixin == null)
      {
         mixin = session.create(type);
         session.setEmbedded(objectToCheck, type, mixin);
         mixin.initializeValue();
      }
      return mixin;
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

   @Override
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

               consumers.putConsumer(info.getId(), consumer);
            }

            lastModified = System.currentTimeMillis();
            getMixin(producerInfosMapping, session, LastModified.class).setLastModified(lastModified);
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
         return getMixin(pims, session, LastModified.class);
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

   private static class MappingToProducerInfoIterator implements Iterator<ProducerInfo>
   {
      private Iterator<ProducerInfoMapping> mappings;
      private final JCRConsumerRegistry registry;

      public MappingToProducerInfoIterator(Iterator<ProducerInfoMapping> infoMappingIterator, JCRConsumerRegistry jcrConsumerRegistry)
      {
         this.mappings = infoMappingIterator;
         this.registry = jcrConsumerRegistry;
      }

      public boolean hasNext()
      {
         return mappings.hasNext();
      }

      public ProducerInfo next()
      {
         return mappings.next().toModel(null, registry);
      }

      public void remove()
      {
         throw new UnsupportedOperationException("Remove not supported!");
      }
   }
}
