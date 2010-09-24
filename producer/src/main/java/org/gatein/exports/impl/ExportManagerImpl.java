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
import org.gatein.exports.OperationFailedException;
import org.gatein.exports.OperationNotSupportedException;
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

   //set to true if we prefer to export by value instead of by reference
   protected boolean preferExportByValue = false;

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
      boolean useExportByValue = false;
      if (exportByValueRequired || (exportPersistenceManager == null && preferExportByValue))
      {
         useExportByValue = true;
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

   public ExportContext createExportContext(byte[] bytes) throws OperationFailedException
   {
      try
      {
         String type = ExportData.getType(bytes);
         double version = ExportData.getVersion(bytes);
         if (ExportContext.TYPE.equals(type) && ExportContext.VERSION == version)
         {
            byte[] internalBytes = ExportData.getInternalBytes(bytes);
            return ExportContext.create(internalBytes);
         }
         else if (exportPersistenceManager != null && exportPersistenceManager.supports(type, version))
         {
            String refId = exportPersistenceManager.getExportReferenceId(type, version, ExportData.getInternalBytes(bytes));
            return exportPersistenceManager.getExportContext(refId);
         }
         else
         {
            throw new OperationFailedException("Byte array format not compatible");
         }
      }
      catch (UnsupportedEncodingException e)
      {
         throw new OperationFailedException("Could not decode the byte array.");
      }
      catch (IOException e)
      {
         throw new OperationFailedException("Could not decode the byte array.");
      }
   }

   public ExportPortletData createExportPortletData(ExportContext exportContextData, String portletHandle,
                                                    byte[] portletState) throws UnsupportedEncodingException
   {
      return new ExportPortletData(portletHandle, portletState);
   }

   public ExportPortletData createExportPortletData(ExportContext exportContextData, long currentTime, long terminationTime, long refreshDuration, byte[] bytes) throws OperationFailedException
   {
      try
      {
         String type = ExportData.getType(bytes);
         double version = ExportData.getVersion(bytes);
         if (ExportPortletData.TYPE.equals(type) && ExportPortletData.VERSION == version)
         {
            byte[] internalBytes = ExportData.getInternalBytes(bytes);
            return ExportPortletData.create(internalBytes);
         }
         else
         {
            throw new OperationFailedException("Bytes array format not compatible");
         }
      }
      catch (UnsupportedEncodingException e)
      {
         throw new OperationFailedException("Could not decode the byte array.");
      }
      catch (IOException e)
      {
         throw new OperationFailedException("Could not decode the byte array.");
      }
   }

   public byte[] encodeExportPortletData(ExportContext exportContextData, ExportPortletData exportPortletData) throws IOException
   {
      if (exportContextData.isExportByValue())
      {
         return exportPortletData.encodeAsBytes();
      }
      else
      {
         String refId = exportPersistenceManager.storeExportPortletData(exportContextData, exportPortletData);
         return exportPersistenceManager.encodeExportPortletData(refId);
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
         String refId = exportPersistenceManager.storeExportContext(exportContext);
         return exportPersistenceManager.encodeExportContext(refId);
      }
   }

   public ExportContext setExportLifetime(byte[] exportContextBytes, long currentTime, long terminationTime, long refreshDuration) throws OperationNotSupportedException, OperationFailedException
   {  
      if (getPersistenceManager() == null)
      {
         throw new OperationNotSupportedException("The producer only supports export by value. Cannot call setExportLifetime on this producer");
      }
      
      try
      {
         String type = ExportData.getType(exportContextBytes);
         double version = ExportData.getVersion(exportContextBytes);

         if (getPersistenceManager().supports(type, version))
         {
            String refId = getPersistenceManager().getExportReferenceId(type, version, ExportData.getInternalBytes(exportContextBytes));
            ExportContext exportContext = getPersistenceManager().getExportContext(refId);

            if (exportContext.isExportByValue())
            {
               throw new OperationFailedException("Cannot set the lifetime for an export that was exported by value.");
            }

            exportContext.setCurrentTime(currentTime);
            exportContext.setTerminationTime(terminationTime);
            exportContext.setRefreshDuration(refreshDuration);

            ExportContext updatedExportContext = getPersistenceManager().updateExportContext(refId, exportContext);
            return updatedExportContext;
         }
         else
         {
            throw new OperationFailedException("Byte array format not recognized.");
         }
      }
      catch (IOException e)
      {
         throw new OperationFailedException("Could not decode the byte array.");
      }
   }

   public void releaseExport(byte[] bytes) throws IOException
   {
      //TODO: since we can't return any errors, we should at least log messages if this method is called and it can't be completed for some reason.
      if (bytes != null && bytes.length > 0 && exportPersistenceManager != null)
      {
         String type = ExportData.getType(bytes);
         double version = ExportData.getVersion(bytes);
         if (exportPersistenceManager.supports(type, version))
         {
            String refId = exportPersistenceManager.getExportReferenceId(type, version, ExportData.getInternalBytes(bytes));
            exportPersistenceManager.removeExportContext(refId);
         }
      }
   }


}

