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

import org.gatein.common.util.ParameterValidation;
import org.gatein.wsrp.api.PortalStructureProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class MigrationService
{
   private SortedMap<Long, ExportInfo> exportInfos;
   // todo: fix me
   private PortalStructureProvider structureProvider = new PortalStructureProvider()
   {
      private Map<String, List<String>> pagesToWindows = new HashMap<String, List<String>>(7);
      {
         List<String> windows = new ArrayList<String>(3);
         windows.add("p1w1");
         windows.add("p1w2");
         windows.add("p1w3");

         pagesToWindows.put("p1", windows);

         windows = new ArrayList<String>(2);
         windows.add("p2w1");
         windows.add("p2w2");

         pagesToWindows.put("p2", windows);
      }

      public List<String> getPageIdentifiers()
      {
         return new ArrayList<String>(pagesToWindows.keySet());
      }

      public List<String> getWindowIndentifiersFor(String pageId)
      {
         return pagesToWindows.get(pageId);
      }

      public void assignPortletToWindow(String portletId, String windowId, String pageId)
      {
         System.out.println("Assigned portlet " + portletId + " to window " + windowId + " on page " + pageId);
      }
   };

   public PortalStructureProvider getStructureProvider()
   {
      return structureProvider;
   }

   public void setStructureProvider(PortalStructureProvider structureProvider)
   {
      this.structureProvider = structureProvider;
   }

   public List<ExportInfo> getAvailableExportInfos()
   {
      return new ArrayList<ExportInfo>(getExportInfos().values());
   }

   public ExportInfo getExportInfo(long exportTime)
   {
      return exportInfos.get(exportTime);
   }

   public void add(ExportInfo info)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(info, "ExportInfo");

      getExportInfos().put(info.getExportTime(), info);
   }

   public ExportInfo remove(ExportInfo info)
   {
      return info == null ? null : getExportInfos().remove(info.getExportTime());
   }

   private SortedMap<Long, ExportInfo> getExportInfos()
   {
      if (exportInfos == null)
      {
         exportInfos = new TreeMap<Long, ExportInfo>();
      }
      return exportInfos;
   }

   public boolean isAvailableExportInfosEmpty()
   {
      return exportInfos == null || exportInfos.isEmpty();
   }
}
