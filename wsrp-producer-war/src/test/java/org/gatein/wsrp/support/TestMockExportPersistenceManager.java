/*
 * JBoss, a division of Red Hat
 * Copyright 2010, Red Hat Middleware, LLC, and individual
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
package org.gatein.wsrp.support;

import org.gatein.exports.ExportPersistenceManager;
import org.gatein.exports.data.ExportContext;
import org.gatein.exports.data.ExportData;
import org.gatein.exports.data.ExportPortletData;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class TestMockExportPersistenceManager implements ExportPersistenceManager
{

   Map<String, ExportContext> exportContexts = new HashMap<String, ExportContext>();
   Map<String, ExportPortletData> exportPortletDatas = new HashMap<String, ExportPortletData>();

   public ExportContext getExportContext(String exportContextId)
   {
      return exportContexts.get(exportContextId);
   }

   public ExportPortletData getExportPortletData(String exportContextId, String portletDataID)
   {
      ExportContext exportContext = exportContexts.get(exportContextId);
      if (exportContext.getPortlets().contains(portletDataID))
      {
         return exportPortletDatas.get(portletDataID);
      }
      else
      {
         return null;
      }
   }

   //For testing purposes only
   public Set<String> getExportContextKeys()
   {
      return exportContexts.keySet();
   }
   
   //For testing purposes only
   public Set<String> getExportPortletsKeys()
   {
      return exportPortletDatas.keySet();
   }

   public boolean removeExportContext(String refId)
   {
      final ExportContext exportContext = exportContexts.get(refId);
      if(exportContext != null)
      {
         final Collection<ExportPortletData> portlets = exportContext.getPortlets();
         for (ExportPortletData portlet : portlets)
         {
            exportPortletDatas.remove(portlet.getId());
         }
         exportContexts.remove(refId);
         return true;
      }
      else
      {
         return false;
      }
   }

   @Override
   public ExportPortletData getExportPortletData(String portletDataId)
   {
      return exportPortletDatas.get(portletDataId);
   }

   @Override
   public ExportPortletData updateExportPortletData(ExportPortletData updatedPortletData)
   {
      return null;
   }

   @Override
   public boolean removeExportPortletData(String portletDataId)
   {
      final ExportPortletData portletData = getExportPortletData(portletDataId);
      if (portletData != null)
      {
         final ExportContext exportContext = portletData.getExportContext();
         exportContext.removePortlet(portletData);
         exportContexts.remove(portletDataId);
         return true;
      }
      else
      {
         return false;
      }
   }

   @Override
   public <T extends ExportData> T loadExportData(String id, Class<T> expected)
   {
      Object result = null;
      if(ExportContext.class.equals(expected))
      {
         result = exportContexts.get(id);
      }
      else if (ExportPortletData.class.equals(expected))
      {
         result = exportPortletDatas.get(id);
      }
      return expected.cast(result);
   }

   public ExportContext updateExportContext(ExportContext updatedExportContext)
   {
      if (updatedExportContext != null)
      {
         final String id = updatedExportContext.getId();
         if(exportContexts.containsKey(id))
         {
            exportContexts.put(id, updatedExportContext);
            return updatedExportContext;
         }
      }
      return null;
   }

   public ExportPortletData updateExportPortletData(String exportContextId, String exportPortletId, ExportPortletData updatedPortletData)
   {
      if (updatedPortletData != null && exportPortletId != null && exportContextId != null && exportContexts.containsKey(exportContextId))
      {
         exportPortletDatas.put(exportPortletId, updatedPortletData);
         return updatedPortletData;
      }
      else
      {
         return null;
      }
   }

   public ExportContext storeExportContext(ExportContext exportContext)
   {
      if (exportContext != null)
      {
         String refId = UUID.randomUUID().toString();
         exportContext.setId(refId);
         exportContexts.put(refId, exportContext);
         return exportContext;
      }
      else
      {
         return null;
      }
   }

   public ExportPortletData storeExportPortletData(ExportContext exportContext, ExportPortletData exportPortletData)
   {
      if (exportPortletData != null && exportContext != null)
      {
         String refId = UUID.randomUUID().toString();
         exportPortletData.setId(refId);
         exportContext.addPortlet(exportPortletData);
         exportPortletDatas.put(refId, exportPortletData);
         return exportPortletData;
      }
      else
      {
         return null;
      }
   }
}

