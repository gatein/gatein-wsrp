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

package org.gatein.wsrp.consumer.migration;

import org.gatein.wsrp.api.context.ConsumerStructureProvider;

import java.util.List;

/**
 * Provides support for import/export operations.
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public interface MigrationService
{
   /**
    * Accesses the ConsumerStructureProvider this MigrationService uses.
    *
    * @return the ConsumerStructureProvider this MigrationService uses.
    */
   ConsumerStructureProvider getStructureProvider();

   /**
    * Provides a way to wire in a ConsumerStructureProvider.
    *
    * @param structureProvider the ConsumerStructureProvider this MigrationService should be using
    */
   void setStructureProvider(ConsumerStructureProvider structureProvider);

   /**
    * Retrieves a list of available ExportInfo, i.e. information about the result of previously run export operations.
    *
    * @return a list of available ExportInfo
    */
   List<ExportInfo> getAvailableExportInfos();

   /**
    * Retrieves the ExportInfo associated with the specified export time.
    *
    * @param exportTime the export time of the ExportInfo we want to retrieve
    * @return the ExportInfo associated with the specified export time or <code>null</code> if no such ExportInfo exists
    */
   ExportInfo getExportInfo(long exportTime);

   /**
    * Adds the specified ExportInfo to the set of ExportInfos managed by this MigrationService.
    *
    * @param info the ExportInfo to add
    */
   void add(ExportInfo info);

   /**
    * Removes the specified ExportInfo from the ones managed by this MigrationService.
    *
    * @param info the ExportInfo to remove
    * @return the ExportInfo that was removed or <code>null</code> if the given ExportInfo wasn't managed by this MigrationService
    */
   ExportInfo remove(ExportInfo info);

   /**
    * Whether there are any available ExportInfos.
    *
    * @return <code>true</code> if this MigrationService manages some ExportInfo, <code>false</code> otherwise
    */
   boolean isAvailableExportInfosEmpty();
}
