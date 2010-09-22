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
    * Returns the reference ID of a particular export (if exported by reference).
    * The type and version are used to determine how to deal with the byte array
    * to retrieve this ID.
    * 
    * @param type The type of export
    * @param version The version of the export
    * @param bytes The contents of the export
    * @return The reference ID
    * @throws UnsupportedEncodingException
    */
   String getExportReferenceId (String type, double version, byte[] bytes) throws UnsupportedEncodingException;
   
   /**
    * Stores the ExportContent and returns the reference ID used to store the content.
    * 
    * @param exportContext The ExportContext to store
    * @return The reference ID used to store the content
    */
   String storeExportContext(ExportContext exportContext);
   
   /**
    * Retrieves an ExportContext which corresponds to a particular reference ID
    * 
    * @param refId The reference ID
    * @return The ExportContext which corresponds to the reference ID
    */
   ExportContext getExportContext(String refId);
   
   /**
    * Updates an already stored ExportContext with an updated one. 
    * 
    * @param refId The reference ID of the stored ExportContext
    * @param updatedExportContext The updated ExportContext
    * @return The new ExportContext
    */
   ExportContext updateExportContext(String refId, ExportContext updatedExportContext);
   
   /**
    * Remove a stored ExportContext which corresponds to a reference ID.
    * 
    * @param refId The reference ID of the ExportContext to remove
    * @return True if the exportContext was removed, false otherwise
    */
   boolean removeExportContext(String refId);
   
   /**
    * Retrieves an ExportContext with the specified reference ID and returns
    * the bytes which correspond to this reference
    * 
    * @param refId The reference Id of the ExportContext
    * @return The byte representation of the ExportContext
    * @throws IOException If an error occurs when trying to encode the bytes
    */
   byte[] encodeExportContext(String refId) throws IOException;
   
   /**
    * Stores a ExportPortletData to a corresponding ExportContext
    * 
    * @param exportContext The ExportContext
    * @param exportPortletData The ExportPortletData
    * @return the reference ID of the stored ExportPortletData
    */
   String storeExportPortletData (ExportContext exportContext, ExportPortletData exportPortletData);
   
   /**
    * Retrieves an ExportPortletData from an ExportContext
    * 
    * @param exportContextId The id of the ExportContext
    * @param portletDataId The id of the ExportPortletData
    * @return The ExportPortletData object
    */
   ExportPortletData getExportPortletData (String exportContextId, String portletDataId);

   /**
    * Updates a particular ExportPortletData corresponding to a ExportContext
    * 
    * @param exportContextId The refId of the ExportContext
    * @param exportPortletId The refId of the ExportPortletData
    * @param updatedPortletData The new ExportPortletData to use
    * @return The updated ExportPortletData
    */
   ExportPortletData updateExportPortletData(String exportContextId, String exportPortletId, ExportPortletData updatedPortletData);
   
   /**
    * Removes a ExportPortletData from a ExportContext
    * 
    * @param exportContextId The reference Id of the ExportContext
    * @param portletDataId The reference Id of the ExportPortletData to remove
    * @return True if the ExportPortletData was removed
    */
   boolean removeExportPortletData(String exportContextId, String portletDataId);
   
   /**
    * Retrieves the ExportPortletData with the specified reference ID and returns
    * the byte representation of this export.
    * 
    * @param exportDataRefId The reference ID of the ExportPortletData
    * @return The byte representation of the ExportPortletData
    * @throws IOException
    */
   byte[] encodeExportPortletData(String exportDataRefId) throws IOException;
}

