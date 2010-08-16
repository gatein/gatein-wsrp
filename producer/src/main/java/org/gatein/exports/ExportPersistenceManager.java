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

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import org.gatein.exports.data.ExportContext;
import org.gatein.exports.data.ExportPortletData;
import org.oasis.wsrp.v2.Lifetime;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public interface ExportPersistenceManager
{  
   String storeExportContextData(ExportContext exportContextData);

   String storeExportPortletData(ExportPortletData exportPortletData);

   ExportContext retrieveExportContextData(String refid);
   
   ExportPortletData retrieveExportPortletData(String refid);

   Lifetime updateExportLifetime(ExportContext exportContext, Lifetime lifetime);

   void releaseExport(byte[] bytes);

   /**
    * Returns true if the PersistenceManager knows how to decode a byte array with
    * the specified type and version.
    * 
    * @param type The type of export
    * @param version The version of the export
    * @return True if the persistence manager can support the specified type and version.
    */
   boolean supports(String type, double version);

   /**
    * Based on the specified type and version, the bytes will be 
    * decoded into an ExportContext.
    * 
    * @param type The type
    * @param version The version
    * @param bytes The bytes to decode
    * @return
    * @throws UnsupportedEncodingException 
    */
   ExportContext getExportContext(String type, double version, byte[] bytes) throws UnsupportedEncodingException;

   byte[] encodeExportPortletData(ExportContext exportContext, ExportPortletData exportPortlet);

   byte[] encodeExportContextData(ExportContext exportContext) throws IOException;
}

