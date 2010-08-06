/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2010, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/
package org.gatein.exports.impl;

import java.io.UnsupportedEncodingException;

import org.gatein.common.NotYetImplemented;
import org.gatein.exports.ExportManager;
import org.gatein.exports.ExportPersistenceManager;
import org.gatein.exports.data.ExportContext;
import org.gatein.exports.data.ExportData;
import org.gatein.exports.data.ExportPortletData;
import org.gatein.wsrp.WSRPExceptionFactory;
import org.oasis.wsrp.v2.Lifetime;
import org.oasis.wsrp.v2.OperationFailed;
import org.oasis.wsrp.v2.OperationNotSupported;


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

   public void setPersistanceManager(ExportPersistenceManager exportPersistenceManager)
   {
      this.exportPersistenceManager = exportPersistenceManager;
   }
   
   public ExportContext createExportContext(boolean exportByValueRequired, Lifetime lifetime)
         throws UnsupportedEncodingException
   {
      boolean useExportByValue = false;
      if (exportByValueRequired || (exportPersistenceManager == null &&  preferExportByValue))
      {
         useExportByValue = true;
      }
            
      return new ExportContext(useExportByValue, lifetime);
   }
   
   public boolean supportExportByValue()
   {
      return supportExportByValue;
   }
   
   public ExportContext createExportContext(byte[] bytes) throws OperationFailed
   {
      try
      {
      String type = ExportData.getType(bytes);
      double version = ExportData.getVersion(bytes);
      if (ExportContext.TYPE.equals(type) && ExportContext.VERSION==version)
      {
         byte[] internalBytes = ExportData.getInternalBytes(bytes);
         return ExportContext.create(internalBytes);
      }
      else
      {
         throw WSRPExceptionFactory.createWSException(OperationFailed.class, "Byte array format not compatible.", null);
      }
      }
      catch (UnsupportedEncodingException e)
      {
         throw WSRPExceptionFactory.createWSException(OperationFailed.class, "Could not decode the byte array.", e);
      }
   }

   public ExportPortletData createExportPortletData(ExportContext exportContextData, String portletHandle,
         byte[] portletState) throws UnsupportedEncodingException
   {
      return new ExportPortletData(portletHandle, portletState);
   }

   public ExportPortletData createExportPortletData(ExportContext exportContextData, Lifetime lifetime, byte[] bytes) throws OperationFailed
   {
      try
      {
         String type = ExportData.getType(bytes);
         double version = ExportData.getVersion(bytes);
         if (ExportPortletData.TYPE.equals(type) && ExportPortletData.VERSION==version)
         {
            byte[] internalBytes = ExportData.getInternalBytes(bytes);
            return ExportPortletData.create(internalBytes);
         }
         else
         {
            throw WSRPExceptionFactory.createWSException(OperationFailed.class, "Bytes array format not compatible", null);
         }
      }
      catch (UnsupportedEncodingException e)
      {
         throw WSRPExceptionFactory.createWSException(OperationFailed.class, "Could not decode the byte array.", e);
      }
   }

   public byte[] encodeExportPortletData(ExportContext exportContextData, ExportPortletData exportPortletData) throws UnsupportedEncodingException
   {
      if (exportContextData.isExportByValue())
      {
         return exportPortletData.encodeAsBytes();
      }
      else
      {
         throw new NotYetImplemented();
      }
   }

   public byte[] encodeExportContextData(ExportContext exportContextData) throws UnsupportedEncodingException
   {
      if (exportContextData.isExportByValue())
      {
         return exportContextData.encodeAsBytes();
      }
      else
      {
         throw new NotYetImplemented();
      }
   }

   public Lifetime setExportLifetime(ExportContext exportContext, Lifetime lifetime) throws OperationFailed, OperationNotSupported
   {
      if (exportContext.isExportByValue())
      {
         WSRPExceptionFactory.throwWSException(OperationFailed.class, "Cannot set the lifetime for an export that was exported by value.", null);
      }
      if (getPersistenceManager() == null)
      {
         WSRPExceptionFactory.throwWSException(OperationNotSupported.class, "The producer only supports export by value. Cannot call setExportLifetime on this producer", null);
      }
      
      return getPersistenceManager().updateExportLifetime(exportContext, lifetime);
   }

   public void releaseExport(ExportContext exportContext)
   {
      //TODO: since we can't return any errors, we should at least log messages if this method is called and it can't be completed for some reason.
      if (exportContext != null && !exportContext.isExportByValue() && exportPersistenceManager!= null)
      {
         exportPersistenceManager.releaseExport(exportContext);
      }
   }
   
   
}

