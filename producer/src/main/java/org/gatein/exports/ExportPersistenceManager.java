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
import org.gatein.exports.data.ExportData;
import org.gatein.exports.data.ExportPortletData;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public interface ExportPersistenceManager
{     
   /**
    * Stores the ExportContext and returns the updated ExportContext, which should now have a proper id.
    * 
    *
    * @param exportContext The ExportContext to store
    * @return the updated ExportContext with a proper id to reference it
    */
   ExportContext storeExportContext(ExportContext exportContext);
   
   /**
    * Retrieves an ExportContext which corresponds to a particular reference ID
    * 
    * @param exportContextId The reference ID
    * @return The ExportContext which corresponds to the reference ID
    */
   ExportContext getExportContext(String exportContextId);
   
   /**
    * Updates an already stored ExportContext with an updated one. 
    * 
    * @param updatedExportContext The updated ExportContext
    * @return The new ExportContext
    */
   ExportContext updateExportContext(ExportContext updatedExportContext);
   
   /**
    * Remove a stored ExportContext which corresponds to a reference ID.
    * 
    * @param refId The reference ID of the ExportContext to remove
    * @return True if the exportContext was removed, false otherwise
    */
   boolean removeExportContext(String refId);

   /**
    * Stores a ExportPortletData to a corresponding ExportContext
    * 
    *
    * @param exportContext The ExportContext
    * @param exportPortletData The ExportPortletData
    * @return the reference ID of the stored ExportPortletData
    */
   ExportPortletData storeExportPortletData(ExportContext exportContext, ExportPortletData exportPortletData);
   
   /**
    * Retrieves an ExportPortletData
    * 
    * @param portletDataId The id of the ExportPortletData
    * @return The ExportPortletData object
    */
   ExportPortletData getExportPortletData (String portletDataId);

   /**
    * Updates a particular ExportPortletData
    * 
    * @param updatedPortletData The new ExportPortletData to use
    * @return The updated ExportPortletData
    */
   ExportPortletData updateExportPortletData(ExportPortletData updatedPortletData);
   
   /**
    * Removes a ExportPortletData from a ExportContext
    * 
    * @param portletDataId The reference Id of the ExportPortletData to remove
    * @return True if the ExportPortletData was removed
    */
   boolean removeExportPortletData(String portletDataId);

   <T extends ExportData> T loadExportData(String id, Class<T> expected);
}

