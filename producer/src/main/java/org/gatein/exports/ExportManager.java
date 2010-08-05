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
package org.gatein.exports;

import java.io.UnsupportedEncodingException;

import org.gatein.exports.data.ExportContext;
import org.gatein.exports.data.ExportPortletData;
import org.oasis.wsrp.v2.Lifetime;
import org.oasis.wsrp.v2.OperationFailed;
import org.oasis.wsrp.v2.OperationNotSupported;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public interface ExportManager
{
   void setPersistanceManager (ExportPersistenceManager exportPersistenceManager);
   
   ExportPersistenceManager getPersistenceManager();
   
   boolean supportExportByValue();
   
   ExportContext createExportContext(boolean exportByValueRequired, Lifetime lifetime) throws UnsupportedEncodingException;
   
   ExportContext createExportContext(byte[] bytes);
   
   ExportPortletData createExportPortletData(ExportContext exportContextData, String portletHandle, byte[] portletState) throws UnsupportedEncodingException;
   
   ExportPortletData createExportPortletData(ExportContext exportContext, Lifetime lifetime, byte[] bytes);
   
   byte[] encodeExportPortletData(ExportContext exportContextData, ExportPortletData exportPortletData) throws UnsupportedEncodingException;
   
   byte[] encodeExportContextData (ExportContext exportContextData) throws UnsupportedEncodingException;

   Lifetime setExportLifetime(ExportContext exportContext, Lifetime lifetime) throws OperationFailed, OperationNotSupported;

   void releaseExport(ExportContext exportContext);
}

