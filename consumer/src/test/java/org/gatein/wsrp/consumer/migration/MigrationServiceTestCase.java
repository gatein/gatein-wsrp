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

import junit.framework.TestCase;

import java.util.List;
import java.util.TreeMap;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class MigrationServiceTestCase extends TestCase
{
   private MigrationService service;

   @Override
   protected void setUp() throws Exception
   {
      service = new MigrationService();
   }

   public void testIsAvailableExportInfosEmpty()
   {
      assertTrue(service.isAvailableExportInfosEmpty());
   }

   public void testAddExport()
   {
      ExportInfo info = new ExportInfo(System.currentTimeMillis(), new TreeMap<String, byte[]>(), null);
      service.add(info);

      List<ExportInfo> exports = service.getAvailableExportInfos();
      assertNotNull(exports);
      assertEquals(1, exports.size());
      assertEquals(info, exports.get(0));
      assertEquals(info, service.getExportInfo(info.getExportTime()));
      assertFalse(service.isAvailableExportInfosEmpty());
   }
}
