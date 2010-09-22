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

import org.gatein.common.NotYetImplemented;
import org.gatein.exports.ExportPersistenceManager;
import org.gatein.exports.data.ExportContext;
import org.gatein.exports.data.ExportPortletData;
import org.gatein.exports.data.PersistedExportData;
import org.oasis.wsrp.v2.Lifetime;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
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

   public static final String PEC_TYPE = "P_EC";
   public static final double PEC_VERSION = 1.0;
   
   public static final String PED_TYPE = "P_ED";
   public static final double PED_VERSION = 1.0;
   
   Map<String, ExportContext> exportContexts = new HashMap<String, ExportContext>();
   Map<String, ExportPortletData> exportPortletDatas = new HashMap<String, ExportPortletData>();

   public ExportContext getExportContext(String refId)
   {
      return exportContexts.get(refId);
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
   
   public String getExportReferenceId(String type, double version, byte[] bytes) throws UnsupportedEncodingException
   {
    if (supports(type, version))
    {
       PersistedExportData persistedExportData = PersistedExportData.create(bytes);
       return persistedExportData.getRefId();
    }
    else
    {
       return null;
    }
   }

   public boolean removeExportContext(String refId)
   {
      if (exportContexts.containsKey(refId))
      {
         List<String> portlets = exportContexts.get(refId).getPortlets();
         for (String portlet: portlets)
         {
            exportPortletDatas.remove(portlet);
         }
         exportContexts.remove(refId);
         return true;
      }
      else
      {
         return false;
      }
   }

   public boolean removeExportPortletData(String exportContextId, String exportDataId)
   {
      if (exportContexts.containsKey(exportDataId))
      {
         List<String> portlets = exportContexts.get(exportDataId).getPortlets();
         if (portlets.contains(exportDataId))
         {
            portlets.remove(exportContextId);
            exportPortletDatas.remove(exportDataId);
            return true;
         }
      }
      return false;
   }

   public boolean supports(String type, double version)
   {
      return (type.equals(PEC_TYPE) && (version == PEC_VERSION)) || ((type.equals(PED_TYPE) && (version == PED_VERSION)));
   }

   public ExportContext updateExportContext(String refId, ExportContext updatedExportContext)
   {
      if (updatedExportContext != null && refId != null && exportContexts.containsKey(refId))
      {
         exportContexts.put(refId, updatedExportContext);
         return updatedExportContext;
      }
      else
      {
         //throw some error here
         return null;
      }
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

   public byte[] encodeExportContext(String refId) throws IOException
   {
      PersistedExportData persistedExportData = new PersistedExportData(PEC_TYPE, refId);
      return persistedExportData.encodeAsBytes();
   }

   public byte[] encodeExportPortletData(String exportDataRefId) throws IOException
   {
      PersistedExportData persistedExportData = new PersistedExportData(PED_TYPE, exportDataRefId);
      return persistedExportData.encodeAsBytes();
   }

   public String storeExportContext(ExportContext exportContext)
   {
      if (exportContext != null)
      {
         String refId = UUID.randomUUID().toString();
         exportContexts.put(refId, exportContext);
         return refId;
      }
      else
      {
         return null;
      }
   }

   public String storeExportPortletData(ExportContext exportContext, ExportPortletData exportPortletData)
   {
      if (exportPortletData != null && exportContext != null)
      {
         String refId = UUID.randomUUID().toString();
         exportContext.addPortlet(refId);
         
         exportPortletDatas.put(refId, exportPortletData);
         
         return refId;
      }
      else
      {
         return null;
      }
   }
}

