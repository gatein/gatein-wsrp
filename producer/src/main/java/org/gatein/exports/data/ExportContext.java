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
package org.gatein.exports.data;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class ExportContext extends ExportData
{

   protected static final String TYPE = "WSRP_EC";
   private static final double VERSION = 1.0;
   
   private long currentTime;
   private long terminationTime;
   private long refreshDuration;
   private final boolean exportByValue;
   private Map<String, ExportPortletData> portlets;

   //for now, we don't store anything in the exported by value ExportContext
   public ExportContext()
   {
      this(true, -1, -1, -1);
   }

   public ExportContext(boolean exportByValue, long currentTime, long terminationTime, long refreshDuration)
   {
      //only consider lifetime if we are exporting by value
      if (exportByValue)
      {
         this.currentTime = currentTime;
         this.terminationTime = terminationTime;
         this.refreshDuration = refreshDuration;
      }

      this.exportByValue = exportByValue;
   }


   public boolean isExportByValue()
   {
      return this.exportByValue;
   }
   
   public long getCurrentTime ()
   {
      return currentTime;
   }
   
   public void setCurrentTime(long currentTime)
   {
      this.currentTime = currentTime;
   }
   
   public long getTermintationTime()
   {
      return terminationTime;
   }
   
   public void setTerminationTime(long terminationTime)
   {
      this.terminationTime = terminationTime;
   }
   
   public long getRefreshDuration()
   {
      return refreshDuration;
   }
   
   public void setRefreshDuration(long refreshDuration)
   {
      this.refreshDuration = refreshDuration;
   }

   public void addPortlet(ExportPortletData portlet)
   {
      if (portlets == null)
      {
         this.portlets = new HashMap<String, ExportPortletData>(7);
      }
      portlet.setExportContext(this);
      portlets.put(portlet.getPortletHandle(), portlet);
   }

   public ExportPortletData removePortlet(ExportPortletData portlet)
   {
      if(portlets != null)
      {
         final ExportPortletData remove = portlets.remove(portlet.getId());
         portlet.setExportContext(null);
         return remove;
      }

      return null;
   }
   
   public Collection<ExportPortletData> getPortlets()
   {
      return portlets != null ? portlets.values() : Collections.<ExportPortletData>emptyList();
   }

   @Override
   protected void decodeExtraData(ObjectInputStream ois) throws IOException
   {
      // we currently don't pass any information by value
   }

   @Override
   protected void encodeExtraData(ObjectOutputStream oos) throws IOException
   {
      // we currently don't pass any information by value
   }

   @Override
   protected double getVersion()
   {
      return VERSION;
   }

   @Override
   public String getType()
   {
      return TYPE;
   }
}

