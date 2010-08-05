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
package org.gatein.wsrp.protocol.v2;

import java.util.ArrayList;
import java.util.List;

import org.gatein.exports.data.ExportContext;
import org.gatein.exports.data.ExportPortletData;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.producer.WSRPProducerBaseTest;
import org.gatein.wsrp.servlet.ServletAccess;
import org.gatein.wsrp.test.ExtendedAssert;
import org.gatein.wsrp.test.support.MockHttpServletRequest;
import org.gatein.wsrp.test.support.MockHttpServletResponse;
import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oasis.wsrp.v2.ExportPortlets;
import org.oasis.wsrp.v2.ExportPortletsResponse;
import org.oasis.wsrp.v2.ExportedPortlet;
import org.oasis.wsrp.v2.FailedPortlets;
import org.oasis.wsrp.v2.ImportPortlet;
import org.oasis.wsrp.v2.ImportPortlets;
import org.oasis.wsrp.v2.ImportPortletsResponse;
import org.oasis.wsrp.v2.Lifetime;
import org.oasis.wsrp.v2.PortletContext;
import org.oasis.wsrp.v2.RegistrationContext;
import org.oasis.wsrp.v2.UserContext;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
@RunWith(Arquillian.class)
public class PortletManagementTestCase extends NeedPortletHandleTest
{
   private static final String TEST_BASIC_PORTLET_WAR = "test-basic-portlet.war";
   

   public PortletManagementTestCase() throws Exception
   {
      super("PortletManagementTestCase", TEST_BASIC_PORTLET_WAR);
   }

   @Deployment
   public static JavaArchive createDeployment()
   {
      JavaArchive jar = ShrinkWrap.create("test.jar", JavaArchive.class);
      jar.addClass(NeedPortletHandleTest.class);
      jar.addClass(V2ProducerBaseTest.class);
      jar.addClass(WSRPProducerBaseTest.class);
      return jar;
   }
   
   @Before
   public void setUp() throws Exception
   {
      if (System.getProperty("test.deployables.dir") != null)
      {
       super.setUp();
       //hack to get around having to have a httpservletrequest when accessing the producer services
       //I don't know why its really needed, seems to be a dependency where wsrp connects with the pc module
       ServletAccess.setRequestAndResponse(MockHttpServletRequest.createMockRequest(null), MockHttpServletResponse.createMockResponse());
      }
   }
   
   @After
   public void tearDown() throws Exception
   {
       if (System.getProperty("test.deployables.dir") != null)
       {
          super.tearDown();
       }
   }
   
   /*TO TEST
    * - export
    * - import
    * - releaseExport
    *  - test a valid setup that the producer is no longer holding any data
    *  - test an invalid setup that we get back the proper errors
    *   - test with invalid portlet handles
    * - setExportLifeTime
    *  - test a valid setup that the export lifetime has been updated
    *  - test an invalid setup that we get an error back
    *    - use an invalid export context
    *    - use an export context that is set to export by value
    *    - invalid registration, usercontext, etc..
    */
   
   @Test
   public void testExport() throws Exception
   {
      boolean exportByValue = true;
      Lifetime lifetime = null;
      UserContext userContext = null;
      RegistrationContext registrationContext = null;
      
      String handle = getDefaultHandle();
      PortletContext defaultContext = WSRPTypeFactory.createPortletContext(handle);
      
      List<PortletContext> portletContexts = new ArrayList<PortletContext>();
      portletContexts.add(defaultContext);
      
      ExportPortlets exportPortlets = WSRPTypeFactory.createExportPortlets(registrationContext, portletContexts, userContext, lifetime, exportByValue);
      
      ExportPortletsResponse response = producer.exportPortlets(exportPortlets);
      
      assertNotNull(response.getExportContext());
      assertNull(response.getLifetime());
      assertTrue(response.getFailedPortlets().isEmpty());
      
      assertEquals(1, response.getExportedPortlet().size());
      
      ExportedPortlet exportPortlet = response.getExportedPortlet().get(0);
      
      assertEquals(handle, exportPortlet.getPortletHandle());
   }
   
   
   @Test
   public void testExportNonExistantHandle() throws Exception
   {
      String nonExistantHandle = "123FakeHandle";
      List<PortletContext> portletContexts = createPortletContextList(nonExistantHandle);
      
      ExportPortlets exportPortlets = createSimpleExportPortlets(portletContexts);
      
      ExportPortletsResponse response = producer.exportPortlets(exportPortlets);
      
      assertNotNull(response.getExportContext());
      assertNull(response.getLifetime());
      assertTrue(response.getExportedPortlet().isEmpty());
      
      assertEquals(1, response.getFailedPortlets().size());
      
      FailedPortlets failedPortlet = response.getFailedPortlets().get(0);
      
      assertTrue(failedPortlet.getPortletHandles().contains(nonExistantHandle));
      assertEquals("InvalidHandle",failedPortlet.getErrorCode().getLocalPart());
      assertTrue(failedPortlet.getPortletHandles().contains(nonExistantHandle));
   }
  
   @Test
   public void testExportNullHandle() throws Exception
   {
      String nonExistantHandle = null;
      List<PortletContext> portletContexts = createPortletContextList(nonExistantHandle);
      
      ExportPortlets exportPortlets = createSimpleExportPortlets(portletContexts);
      
      ExportPortletsResponse response = producer.exportPortlets(exportPortlets);
      
      assertNotNull(response.getExportContext());
      assertNull(response.getLifetime());
      assertTrue(response.getExportedPortlet().isEmpty());
      
      assertEquals(1, response.getFailedPortlets().size());
      
      FailedPortlets failedPortlet = response.getFailedPortlets().get(0);
      assertTrue(failedPortlet.getPortletHandles().contains(nonExistantHandle));
      assertEquals("InvalidHandle",failedPortlet.getErrorCode().getLocalPart());
      assertTrue(failedPortlet.getPortletHandles().contains(nonExistantHandle));
   }
   
   protected List<PortletContext> createPortletContextList(String... portletHandles)
   {
      List<PortletContext> portletContexts = new ArrayList<PortletContext>();
      
      for (String portletHandle : portletHandles)
      {
         PortletContext portletContext = new PortletContext();
         portletContext.setPortletHandle(portletHandle);
         portletContexts.add(portletContext);
      }
      return portletContexts;
   }
   
   protected ExportPortlets createSimpleExportPortlets (List<PortletContext> portletContexts)
   {
      boolean exportByValueRequired = true;
      Lifetime lifetime = null;
      UserContext userContext = null;
      RegistrationContext registrationContext = null;
      
      return WSRPTypeFactory.createExportPortlets(registrationContext, portletContexts, userContext, lifetime, exportByValueRequired);
   }
   
   
   @Test
   public void testImport() throws Exception
   {
      String importID = "foo";
      
      ExportPortletData exportPortletData = new ExportPortletData(getDefaultHandle(), null);
      byte[] exportData = exportPortletData.encodeAsBytes();
      
      Lifetime lifetime = null;
      UserContext userContext = null;
      RegistrationContext registrationContext = null;
      
      List<String> portletList = new ArrayList<String>();
      portletList.add(getDefaultHandle());
      ExportContext exportContextData = new ExportContext();
      byte[] importContext = exportContextData.encodeAsBytes();
      
      ImportPortlet importPortlet = WSRPTypeFactory.createImportPorlet(importID, exportData);
      
      List<ImportPortlet> importPortletsList = new ArrayList<ImportPortlet>();
      importPortletsList.add(importPortlet);
     
      ImportPortlets importPortlets = WSRPTypeFactory.createImportPortlets(registrationContext, importContext, importPortletsList, userContext, lifetime);
      ImportPortletsResponse response = producer.importPortlets(importPortlets); 
   }
   
   
   
   
   @Override
   protected String getMostUsedPortletWARFileName()
   {
      return TEST_BASIC_PORTLET_WAR;
   }

}

