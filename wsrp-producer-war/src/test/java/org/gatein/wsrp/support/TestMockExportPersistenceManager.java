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
import java.util.Map;
import java.util.UUID;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class TestMockExportPersistenceManager implements ExportPersistenceManager
{

   public static final String PEC_TYPE = "P_EC";
   public static final double PEC_VERSION = 1.0;

   Map<String, ExportContext> exportContexts = new HashMap<String, ExportContext>();

   //For testing purposes only
   public Map<String, ExportContext> getExportContexts()
   {
      return exportContexts;
   }

   public ExportContext getExportContext(String type, double version, byte[] bytes) throws UnsupportedEncodingException
   {
      if (supports(type, version))
      {
         PersistedExportData persistedExportData = PersistedExportData.create(bytes);
         String refId = persistedExportData.getRefId();
         return exportContexts.get(refId);
      }
      else
      {
         return null;
      }
   }

   public void releaseExport(byte[] bytes)
   {
      try
      {
         PersistedExportData persistedExportData = PersistedExportData.create(bytes);
         String refId = persistedExportData.getRefId();
         if (exportContexts.containsKey(refId))
         {
            exportContexts.remove(refId);
         }
      }
      catch (Exception e)
      {
         System.out.println("ERROR When trying to release exports");
         e.printStackTrace();
      }
   }

   public ExportContext retrieveExportContextData(String refid)
   {
      if (exportContexts.containsKey(refid))
      {
         return exportContexts.get(refid);
      }
      else
      {
         return null;
      }
   }

   public ExportPortletData retrieveExportPortletData(String refid)
   {
      throw new NotYetImplemented();
   }

   public String storeExportContextData(ExportContext exportContextData)
   {
      if (exportContextData != null)
      {
         String refId = UUID.randomUUID().toString();
         exportContexts.put(refId, exportContextData);
         return refId;
      }
      else
      {
         return null;
      }
   }

   public String storeExportPortletData(ExportPortletData exportPortletData)
   {
      throw new NotYetImplemented();
   }

   public boolean supports(String type, double version)
   {
      return type.equals(PEC_TYPE) && (version == PEC_VERSION);
   }

   public Lifetime updateExportLifetime(ExportContext exportContext, Lifetime lifetime)
   {
      throw new NotYetImplemented();
   }

   public byte[] encodeExportContextData(ExportContext exportContext) throws IOException
   {
      String refId = storeExportContextData(exportContext);
      PersistedExportData persistedExportData = new PersistedExportData(PEC_TYPE, refId);
      return persistedExportData.encodeAsBytes();
   }

   public byte[] encodeExportPortletData(ExportContext exportContext, ExportPortletData exportPortlet)
   {
      // FIXME encodeExportPortletData
      return null;
   }
}

