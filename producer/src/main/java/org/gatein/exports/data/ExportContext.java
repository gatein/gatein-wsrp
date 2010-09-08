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

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class ExportContext extends ExportData
{

   protected static final String ENCODING = "UTF-8";
   public static final String TYPE = "WSRP_EC";
   public static final double VERSION = 1.0;
   
   protected long currentTime;
   protected long terminationTime;
   protected long refreshDuration;
   
   protected final boolean exportByValue;

   protected List<String> portlets;

   //for now, we don't store anything in the exported by value ExportContext
   public ExportContext()
   {
      this.exportByValue = true;
   }

   public ExportContext(boolean exportByValue, long currentTime, long terminationTime, long refreshDuration)
   {
      //ignore the lifetime if we are exporting by value 
      if (exportByValue)
      {
         this.currentTime = currentTime;
         this.terminationTime = terminationTime;
         this.refreshDuration = refreshDuration;
      }
      else
      {
         //this.lifeTime = lifetime;
      }
      this.exportByValue = exportByValue;
   }
   
   public ExportContext(String refId, long currentTime, long terminationTime, long refreshDuration)
   {
      this.currentTime = currentTime;
      this.terminationTime = terminationTime;
      this.refreshDuration = refreshDuration;

      this.exportByValue = false;
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

   public void addPortlet(String portletName)
   {
      if (portlets == null)
      {
         this.portlets = new ArrayList<String>();
      }
      this.portlets.add(portletName);
   }
   
   public List<String> getPortlets()
   {
      return portlets;
   }

   public static ExportContext create(byte[] bytes)
   {
      //for now, we don't store anything in the stored by value ExportContext
      ExportContext exportContext = new ExportContext();
      return exportContext;
   }

   public String getType()
   {
      return TYPE;
   }

   public double getVersion()
   {
      return VERSION;
   }

   protected byte[] internalEncodeAsBytes() throws UnsupportedEncodingException
   {
      return "EMPTY".getBytes(ENCODING); // todo: Matt, is this needed?
   }

}

