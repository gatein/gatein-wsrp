/*
* JBoss, a division of Red Hat
* Copyright 2008, Red Hat Middleware, LLC, and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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
import java.text.DateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Provides basic behavior for migration (i.e. WSRP import/export) functionality.
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class BaseMigrationInfo
{
   protected final static Map<QName, List<String>> EMPTY_FAILED = new TreeMap<QName, List<String>>();
   protected final Map<QName, List<String>> errorCodeToHandles;
   protected final long exportTime;

   public BaseMigrationInfo(long exportTime, Map<QName, List<String>> errorCodeToHandles)
   {
      if (ParameterValidation.existsAndIsNotEmpty(errorCodeToHandles))
      {
         this.errorCodeToHandles = errorCodeToHandles;
      }
      else
      {
         this.errorCodeToHandles = EMPTY_FAILED;
      }
      this.exportTime = exportTime;
   }

   public Map<QName, List<String>> getErrorCodesToFailedPortletHandlesMapping()
   {
      return Collections.unmodifiableMap(errorCodeToHandles);
   }

   public long getExportTime()
   {
      return exportTime;
   }

   public String getHumanReadableExportTime(Locale locale)
   {
      return getHumanReadableTime(locale, exportTime);
   }

   protected String getHumanReadableTime(Locale locale, final long time)
   {
      if (locale == null)
      {
         locale = Locale.getDefault();
      }
      return DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, locale).format(new Date(time));
   }
}
