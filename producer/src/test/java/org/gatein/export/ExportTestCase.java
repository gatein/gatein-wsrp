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
package org.gatein.export;

import java.io.UnsupportedEncodingException;

import org.gatein.exports.data.ExportData;
import org.gatein.exports.data.ExportPortletData;
import org.gatein.wsrp.test.ExtendedAssert;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class ExportTestCase extends TestCase
{
   public void testTransformationByValueStateless() throws UnsupportedEncodingException
   {
      String portletId = "TestPortletID_123";
      double version = 1.0;
      
      ExportPortletData exportPortletData = new ExportPortletData(portletId, null);
      assertEquals(version, exportPortletData.getVersion());
      assertEquals(portletId, exportPortletData.getPortletHandle());
      assertNull(exportPortletData.getPortletState());

      byte[] bytes = exportPortletData.encodeAsBytes();
      
      String dataAsString = new String(bytes, "UTF-8");
      
      byte[] internalBytes = ExportData.getInternalBytes(bytes);
      ExportPortletData portletDataFromBytes = ExportPortletData.create(internalBytes);
      assertEquals(version, portletDataFromBytes.getVersion());
      
      assertEquals(portletId, portletDataFromBytes.getPortletHandle());
      assertEquals(version, portletDataFromBytes.getVersion());
      assertNull(portletDataFromBytes.getPortletState());
   }
   
   public void testTransformationByValueStatefull() throws UnsupportedEncodingException
   {
      String portletId = "TestPortletID_123";
      double version = 1.0;
      byte[] state = new byte[]{0, 1, 2, 3, 'a', 'b', 'c'};
      
      ExportPortletData exportPortletData = new ExportPortletData(portletId, state);
      assertEquals(version, exportPortletData.getVersion());
      assertEquals(portletId, exportPortletData.getPortletHandle());
      assertNotNull(exportPortletData.getPortletState());
      ExtendedAssert.assertEquals(state, exportPortletData.getPortletState());

      byte[] bytes = exportPortletData.encodeAsBytes();
      
      String dataAsString = new String(bytes, "UTF-8");
      
      byte[] internalBytes = ExportData.getInternalBytes(bytes);
      ExportPortletData portletDataFromBytes = ExportPortletData.create(internalBytes);
      assertEquals(version, portletDataFromBytes.getVersion());
      
      assertEquals(portletId, portletDataFromBytes.getPortletHandle());
      assertEquals(version, portletDataFromBytes.getVersion());
      ExtendedAssert.assertEquals(state, portletDataFromBytes.getPortletState());
   }
   
   
   /**
    * ADD MORE TESTS
    * - decode from a stored byte array
    * - test that encode returns an expected array
    * - test that you get back the right version (the implementation doesn't do this correctly yet...)
    */
}

