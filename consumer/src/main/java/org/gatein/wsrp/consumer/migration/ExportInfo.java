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

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Provides metadata information about the result of an export operation on a remote producer.
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class ExportInfo extends BaseMigrationInfo
{
   private final byte[] exportContext;
   private long expirationTime;
   private final SortedMap<String, byte[]> handleToExportedState;
   private static final SortedMap<String, byte[]> EMPTY_EXPORTED = new TreeMap<String, byte[]>();

   public ExportInfo(long exportTime, SortedMap<QName, List<String>> errorCodeToHandles, SortedMap<String, byte[]> handleToState, byte[] exportContext)
   {
      super(exportTime, errorCodeToHandles);

      if (ParameterValidation.existsAndIsNotEmpty(handleToState))
      {
         this.handleToExportedState = handleToState;
      }
      else
      {
         handleToExportedState = EMPTY_EXPORTED;
      }

      this.exportContext = exportContext;
   }

   public long getExpirationTime()
   {
      return expirationTime;
   }

   public String getHumanReadableExpirationTime(Locale locale)
   {
      return getHumanReadableTime(locale, expirationTime);
   }

   public List<String> getExportedPortletHandles()
   {
      return new ArrayList<String>(handleToExportedState.keySet());
   }

   public byte[] getPortletStateFor(String portletHandle)
   {
      return handleToExportedState.get(portletHandle);
   }

   @Override
   public boolean equals(Object o)
   {
      if (this == o)
      {
         return true;
      }
      if (o == null || getClass() != o.getClass())
      {
         return false;
      }

      ExportInfo that = (ExportInfo)o;

      if (exportTime != that.exportTime)
      {
         return false;
      }

      return true;
   }

   @Override
   public int hashCode()
   {
      return (int)(exportTime ^ (exportTime >>> 32));
   }

   public byte[] getExportContext()
   {
      return exportContext;
   }
}
