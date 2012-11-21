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

package org.gatein.wsrp.consumer.migration;

import org.chromattic.api.ChromatticSession;
import org.gatein.common.util.ParameterValidation;
import org.gatein.wsrp.api.context.ConsumerStructureProvider;
import org.gatein.wsrp.consumer.migration.mapping.ExportErrorMapping;
import org.gatein.wsrp.consumer.migration.mapping.ExportInfoMapping;
import org.gatein.wsrp.consumer.migration.mapping.ExportInfosMapping;
import org.gatein.wsrp.consumer.migration.mapping.ExportedStateMapping;
import org.gatein.wsrp.jcr.ChromatticPersister;
import org.gatein.wsrp.jcr.StoresByPathManager;
import org.gatein.wsrp.jcr.mapping.mixins.LastModified;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class JCRMigrationService implements MigrationService, StoresByPathManager<ExportInfo>
{
   private ConsumerStructureProvider structureProvider;
   private ChromatticPersister persister;
   private static final String EXPORT_INFOS_PATH = ExportInfosMapping.NODE_NAME;
   private int exportInfosCount = -1;

   public static final List<Class> mappingClasses = new ArrayList<Class>(4);

   static
   {
      Collections.addAll(mappingClasses, ExportInfosMapping.class, ExportInfoMapping.class, ExportedStateMapping.class,
         ExportErrorMapping.class);
   }


   public JCRMigrationService(ChromatticPersister persister) throws Exception
   {
      this.persister = persister;
   }

   public ConsumerStructureProvider getStructureProvider()
   {
      return structureProvider;
   }

   public void setStructureProvider(ConsumerStructureProvider structureProvider)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(structureProvider, "PortalStructureProvider");
      this.structureProvider = structureProvider;
   }

   public List<ExportInfo> getAvailableExportInfos()
   {
      try
      {
         ChromatticSession session = persister.getSession();

         ExportInfosMapping exportInfosMapping = getExportInfosMapping(session);

         List<ExportInfoMapping> exportInfoMappings = exportInfosMapping.getExportInfos();
         List<ExportInfo> exportInfos = new ArrayList<ExportInfo>(exportInfoMappings.size());
         for (ExportInfoMapping eim : exportInfoMappings)
         {
            exportInfos.add(eim.toModel(null, null));
         }

         exportInfosCount = exportInfos.size();

         return exportInfos;
      }
      finally
      {
         persister.closeSession(false);

      }
   }

   private ExportInfosMapping getExportInfosMapping(ChromatticSession session)
   {
      ExportInfosMapping exportInfosMapping = session.findByPath(ExportInfosMapping.class, ExportInfosMapping.NODE_NAME);
      if (exportInfosMapping == null)
      {
         exportInfosMapping = session.insert(ExportInfosMapping.class, ExportInfosMapping.NODE_NAME);
         exportInfosCount = 0;
      }

      return exportInfosMapping;
   }

   public ExportInfo getExportInfo(long exportTime)
   {
      try
      {
         ChromatticSession session = persister.getSession();

         ExportInfoMapping eim = session.findByPath(ExportInfoMapping.class, getPathFor(exportTime));
         if (eim != null)
         {
            return eim.toModel(null, null);
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

   public void add(ExportInfo info)
   {
      try
      {
         ChromatticSession session = persister.getSession();

         ExportInfoMapping eim = session.findByPath(ExportInfoMapping.class, getChildPath(info));
         long exportTime = info.getExportTime();
         if (eim != null)
         {
            throw new IllegalArgumentException("An ExportInfo with export time "
               + exportTime + " already exists!");
         }
         else
         {
            ExportInfosMapping exportInfosMapping = getExportInfosMapping(session);
            String exportTimeAsString = "" + exportTime;
            ExportInfoMapping exportInfo = exportInfosMapping.createExportInfo(exportTimeAsString);
            session.persist(exportInfosMapping, exportInfo, exportTimeAsString);
            exportInfo.initFrom(info);

            persister.save();
            exportInfosCount++;
         }
      }
      finally
      {
         persister.closeSession(false);
      }
   }

   public ExportInfo remove(ExportInfo info)
   {
      if (persister.delete(info, this))
      {
         exportInfosCount--;
         return info;
      }
      else
      {
         return null;
      }
   }

   public boolean isAvailableExportInfosEmpty()
   {
      if (exportInfosCount == -1)
      {
         try
         {
            ChromatticSession session = persister.getSession();
            ExportInfosMapping mappings = getExportInfosMapping(session);
            exportInfosCount = mappings.getExportInfos().size();
         }
         finally
         {
            persister.closeSession(false);
         }
      }

      return exportInfosCount == 0;
   }

   public String getParentPath()
   {
      return EXPORT_INFOS_PATH;
   }

   public String getChildPath(ExportInfo exportInfo)
   {
      return getPathFor(exportInfo.getExportTime());
   }

   public LastModified lastModifiedToUpdateOnDelete(ChromatticSession session)
   {
      return null;
   }

   private String getPathFor(final long exportTime)
   {
      return getParentPath() + "/" + exportTime;
   }
}
