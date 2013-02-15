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
package org.gatein.exports.impl;

import org.gatein.exports.ExportManager;
import org.gatein.exports.ExportPersistenceManager;
import org.gatein.exports.data.ExportContext;
import org.gatein.exports.data.ExportData;
import org.gatein.exports.data.ExportPortletData;
import org.gatein.wsrp.WSRPExceptionFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;


/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class ExportManagerImpl implements ExportManager
{

   protected ExportPersistenceManager exportPersistenceManager;

   // GTNWSRP-350: export by reference is not currently supported so prefer export by value
   protected boolean preferExportByValue = true;

   protected boolean supportExportByValue = true;

   public ExportPersistenceManager getPersistenceManager()
   {
      return exportPersistenceManager;
   }

   public void setPersistenceManager(ExportPersistenceManager exportPersistenceManager)
   {
      this.exportPersistenceManager = exportPersistenceManager;
   }

   public ExportContext createExportContext(boolean exportByValueRequired, long currentTime, long terminationTime, long refreshDuration)
      throws UnsupportedEncodingException
   {
      // only use export by reference if we have an ExportPersistenceManager and we don't prefer export by value
      boolean useExportByValue = true;
      if (exportPersistenceManager != null && !preferExportByValue)
      {
         useExportByValue = false;
      }

      return new ExportContext(useExportByValue, currentTime, terminationTime, refreshDuration);
   }

   public boolean supportsExportByValue()
   {
      return supportExportByValue;
   }

   public void setPreferExportByValue(boolean preferExportByValue)
   {
      this.preferExportByValue = preferExportByValue;
   }

   public ExportContext createExportContext(byte[] bytes)
   {
      return createExportData(ExportContext.class, bytes);
   }

   private <T extends ExportData> T createExportData(Class<T> expected, byte[] bytes)
   {
      return ExportData.initExportData(expected, bytes, exportPersistenceManager);
   }

   public ExportPortletData createExportPortletData(ExportContext exportContextData, String portletHandle,
                                                    byte[] portletState) throws UnsupportedEncodingException
   {
      return new ExportPortletData(portletHandle, portletState);
   }

   public ExportPortletData createExportPortletData(ExportContext exportContextData, long currentTime, long terminationTime, long refreshDuration, byte[] bytes)
   {
      return createExportData(ExportPortletData.class, bytes);
   }

   public byte[] encodeExportPortletData(ExportContext exportContextData, ExportPortletData exportPortletData) throws IOException
   {
      if (exportContextData.isExportByValue())
      {
         return exportPortletData.encodeAsBytes();
      }
      else
      {
         final ExportPortletData portletData = exportPersistenceManager.storeExportPortletData(exportContextData, exportPortletData);
         return portletData.encodeAsBytes();
      }
   }

   public byte[] encodeExportContextData(ExportContext exportContext) throws IOException
   {
      if (exportContext.isExportByValue())
      {
         return exportContext.encodeAsBytes();
      }
      else
      {
         final ExportContext stored = exportPersistenceManager.storeExportContext(exportContext);
         return stored.encodeAsBytes();
      }
   }

   public ExportContext setExportLifetime(byte[] exportContextBytes, long currentTime, long terminationTime, long refreshDuration)
   {
      if (getPersistenceManager() == null)
      {
         throw new UnsupportedOperationException("The producer only supports export by value. Cannot call setExportLifetime on this producer");
      }

      ExportContext exportContext = createExportContext(exportContextBytes);
      if (exportContext.isExportByValue())
      {
         throw new IllegalStateException("Cannot set the lifetime for an export that was exported by value");
      }

      exportContext.setCurrentTime(currentTime);
      exportContext.setTerminationTime(terminationTime);
      exportContext.setRefreshDuration(refreshDuration);

      ExportContext updatedExportContext = getPersistenceManager().updateExportContext(exportContext);
      return updatedExportContext;

   }

   public void releaseExport(byte[] bytes) throws IOException
   {
      //TODO: since we can't return any errors, we should at least log messages if this method is called and it can't be completed for some reason.
      if (bytes != null && bytes.length > 0 && exportPersistenceManager != null)
      {
         ExportContext exportContext = createExportContext(bytes);
         exportPersistenceManager.removeExportContext(exportContext.getId());
      }
   }


}

