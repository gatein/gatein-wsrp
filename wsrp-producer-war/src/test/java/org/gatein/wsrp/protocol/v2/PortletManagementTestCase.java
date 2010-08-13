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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gatein.exports.ExportManager;
import org.gatein.exports.data.ExportContext;
import org.gatein.exports.data.ExportData;
import org.gatein.exports.data.ExportPortletData;
import org.gatein.exports.impl.ExportManagerImpl;
import org.gatein.pc.api.Portlet;
import org.gatein.pc.api.PortletStateType;
import org.gatein.pc.api.state.PropertyMap;
import org.gatein.pc.portlet.state.SimplePropertyMap;
import org.gatein.pc.portlet.state.producer.PortletState;
import org.gatein.pc.portlet.state.producer.ProducerPortletInvoker;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.WSRPUtils;
import org.gatein.wsrp.producer.WSRPProducerBaseTest;
import org.gatein.wsrp.servlet.ServletAccess;
import org.gatein.wsrp.spec.v2.WSRP2RewritingConstants;
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
import org.oasis.wsrp.v2.AccessDenied;
import org.oasis.wsrp.v2.BlockingInteractionResponse;
import org.oasis.wsrp.v2.ExportPortlets;
import org.oasis.wsrp.v2.ExportPortletsResponse;
import org.oasis.wsrp.v2.ExportedPortlet;
import org.oasis.wsrp.v2.FailedPortlets;
import org.oasis.wsrp.v2.GetMarkup;
import org.oasis.wsrp.v2.ImportPortlet;
import org.oasis.wsrp.v2.ImportPortlets;
import org.oasis.wsrp.v2.ImportPortletsResponse;
import org.oasis.wsrp.v2.ImportedPortlet;
import org.oasis.wsrp.v2.InconsistentParameters;
import org.oasis.wsrp.v2.InvalidCookie;
import org.oasis.wsrp.v2.InvalidHandle;
import org.oasis.wsrp.v2.InvalidRegistration;
import org.oasis.wsrp.v2.InvalidSession;
import org.oasis.wsrp.v2.InvalidUserCategory;
import org.oasis.wsrp.v2.Lifetime;
import org.oasis.wsrp.v2.MarkupContext;
import org.oasis.wsrp.v2.MarkupParams;
import org.oasis.wsrp.v2.MarkupResponse;
import org.oasis.wsrp.v2.MissingParameters;
import org.oasis.wsrp.v2.ModifyRegistrationRequired;
import org.oasis.wsrp.v2.NamedString;
import org.oasis.wsrp.v2.OperationFailed;
import org.oasis.wsrp.v2.PerformBlockingInteraction;
import org.oasis.wsrp.v2.PortletContext;
import org.oasis.wsrp.v2.RegistrationContext;
import org.oasis.wsrp.v2.RegistrationData;
import org.oasis.wsrp.v2.ResourceSuspended;
import org.oasis.wsrp.v2.StateChange;
import org.oasis.wsrp.v2.UnsupportedLocale;
import org.oasis.wsrp.v2.UnsupportedMimeType;
import org.oasis.wsrp.v2.UnsupportedMode;
import org.oasis.wsrp.v2.UnsupportedWindowState;
import org.oasis.wsrp.v2.UserContext;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
@RunWith(Arquillian.class)
public class PortletManagementTestCase extends NeedPortletHandleTest
{
   private static final String TEST_BASIC_PORTLET_WAR = "test-markup-portlet.war";

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
   
   /*TODO:
    * - tests usercontexts (not sure exactly what needs to be tested for this)
    */
   
   @Test
   public void testExport() throws Exception
   {   
      String handle = getDefaultHandle();
      List<PortletContext> portletContexts = createPortletContextList(handle);
      
      ExportPortlets exportPortlets = createSimpleExportPortlets(portletContexts);
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
   }
  
   @Test
   public void testExportNullHandle() throws Exception
   {
      String nullHandle = null;
      List<PortletContext> portletContexts = createPortletContextList(nullHandle);
      
      ExportPortlets exportPortlets = createSimpleExportPortlets(portletContexts);
      
      ExportPortletsResponse response = producer.exportPortlets(exportPortlets);
      
      assertNotNull(response.getExportContext());
      assertNull(response.getLifetime());
      assertTrue(response.getExportedPortlet().isEmpty());
      
      assertEquals(1, response.getFailedPortlets().size());
      
      FailedPortlets failedPortlet = response.getFailedPortlets().get(0);
      assertTrue(failedPortlet.getPortletHandles().contains(nullHandle));
      assertEquals("InvalidHandle",failedPortlet.getErrorCode().getLocalPart());
   }
   
   @Test
   public void testExportNullExportContext() throws Exception
   {
         ExportPortlets exportPortlets = new ExportPortlets();
         try
         {
            ExportPortletsResponse response = producer.exportPortlets(exportPortlets);
            ExtendedAssert.fail("Should have thrown a MissingParameters fault if no portlets passed for export.");
         }
         catch (MissingParameters e)
         {
            //expected
         }
   }
   
   @Test
   public void testExportNullExportPortlets() throws Exception
   {
      try
      {
         ExportPortletsResponse response = producer.exportPortlets(null);
         ExtendedAssert.fail("Should have failed if sending a null exportPortlet object");
      }
      catch (MissingParameters e)
      {
         //expected
      }
   }
   
   @Test
   public void testExportNoRegistrationWhenRequired() throws Exception
   {
      producer.getConfigurationService().getConfiguration().getRegistrationRequirements().setRegistrationRequired(true);
      
      String handle = getDefaultHandle();
      List<PortletContext> portletContexts = createPortletContextList(handle);
      
      ExportPortlets exportPortlets = createSimpleExportPortlets(portletContexts);
      
      try
      {
         ExportPortletsResponse response = producer.exportPortlets(exportPortlets);
         ExtendedAssert.fail("ExportPortlets should fail if registration is required and none is provided");
      }
      catch (InvalidRegistration e)
      {
         //expected
      }
   }
   
   @Test
   public void testExportBadRegistrationHandle() throws Exception
   {
      producer.getConfigurationService().getConfiguration().getRegistrationRequirements().setRegistrationRequired(true);
      
      RegistrationContext registrationContext = WSRPTypeFactory.createRegistrationContext("foo123");
      
      List<PortletContext> portletContexts = createPortletContextList(getDefaultHandle());
      
      boolean exportByValueRequired = true;
      Lifetime lifetime = null;
      UserContext userContext = null;
      
      ExportPortlets exportPortlets =  WSRPTypeFactory.createExportPortlets(registrationContext, portletContexts, userContext, lifetime, exportByValueRequired);
      
      try
      {
         ExportPortletsResponse response = producer.exportPortlets(exportPortlets);
         ExtendedAssert.fail("ExportPortlets should fail if registration is required and an invalid registration handle is provided");
      }
      catch (InvalidRegistration e)
      {
         //expected
      }
   }
   
   @Test
   public void testExportRegistrationRequired() throws Exception
   {
      producer.getConfigurationService().getConfiguration().getRegistrationRequirements().setRegistrationRequired(true);
      
      RegistrationData registrationData = WSRPTypeFactory.createRegistrationData("CONSUMER", true);
      RegistrationContext registrationContext = producer.register(registrationData);
      
      
      List<PortletContext> portletContexts = createPortletContextList(getDefaultHandle());
      
      boolean exportByValueRequired = true;
      Lifetime lifetime = null;
      UserContext userContext = null;
      
      ExportPortlets exportPortlets =  WSRPTypeFactory.createExportPortlets(registrationContext, portletContexts, userContext, lifetime, exportByValueRequired);
      
      ExportPortletsResponse response = producer.exportPortlets(exportPortlets);
      
      assertNotNull(response.getExportContext());
      assertNull(response.getLifetime());
      assertTrue(response.getFailedPortlets().isEmpty());
      
      assertEquals(1, response.getExportedPortlet().size());
      
      ExportedPortlet exportPortlet = response.getExportedPortlet().get(0);
      
      assertEquals(getDefaultHandle(), exportPortlet.getPortletHandle());
   }
   
   @Test
   public void testExports() throws Exception
   {
      String nullHandle = null;
      String nonExistantHandle = "123FakeHandle";
      String handle = getDefaultHandle();
      List<PortletContext> portletContexts = createPortletContextList(nullHandle, nonExistantHandle, handle);
      
      ExportPortlets exportPortlets = createSimpleExportPortlets(portletContexts);
      
      ExportPortletsResponse response = producer.exportPortlets(exportPortlets);
      
      assertNotNull(response.getExportContext());
      assertNull(response.getLifetime());
      assertFalse(response.getExportedPortlet().isEmpty());
      assertFalse(response.getFailedPortlets().isEmpty());
      
      assertEquals(1, response.getExportedPortlet().size());
      
      //Should provide the same error code and so should only produce on set of FailedPortlets
      assertEquals(1, response.getFailedPortlets().size());
      
      ExportedPortlet exportPortlet = response.getExportedPortlet().get(0);
      assertEquals(handle, exportPortlet.getPortletHandle());
      
      FailedPortlets failedPortlets = response.getFailedPortlets().get(0);
      assertEquals("InvalidHandle",failedPortlets.getErrorCode().getLocalPart());
      assertEquals(2, failedPortlets.getPortletHandles().size());
      assertTrue(failedPortlets.getPortletHandles().contains(nullHandle));
      assertTrue(failedPortlets.getPortletHandles().contains(nonExistantHandle));
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

      List<String> portletList = new ArrayList<String>();
      portletList.add(getDefaultHandle());
      byte[] importContext = new ExportContext().encodeAsBytes();
      
      ImportPortlet importPortlet = createSimpleImportPortlet(importID, getDefaultHandle());
      
      List<ImportPortlet> importPortletsList = createImportPortletList(importPortlet);
      
      ImportPortlets importPortlets = createSimpleImportPortlets(importContext, importPortletsList);
      ImportPortletsResponse response = producer.importPortlets(importPortlets); 
   
      assertEquals(1,response.getImportedPortlets().size());
      ImportedPortlet portlet = response.getImportedPortlets().get(0);
      
      assertEquals(importID, portlet.getImportID());
      
      PortletContext portletContext = portlet.getNewPortletContext();

      //check that the new portlet handle is valid and we can access the portlet
      GetMarkup markup = createMarkupRequest(portletContext.getPortletHandle());
      MarkupResponse markupResponse = producer.getMarkup(markup);
      assertNotNull(markupResponse.getMarkupContext());
      assertEquals("<p>symbol unset stock value: value unset</p>", new String(markupResponse.getMarkupContext().getItemString()));
   }
   
   @Test
   public void testImportNoRegistrationWhenRequired() throws Exception
   {
      producer.getConfigurationService().getConfiguration().getRegistrationRequirements().setRegistrationRequired(true);
      
      String importID = "foo";

      List<String> portletList = new ArrayList<String>();
      portletList.add(getDefaultHandle());
      byte[] importContext = new ExportContext().encodeAsBytes();
      
      ImportPortlet importPortlet = createSimpleImportPortlet(importID, getDefaultHandle());
      List<ImportPortlet> importPortletsList = createImportPortletList(importPortlet);
      ImportPortlets importPortlets = createSimpleImportPortlets(importContext, importPortletsList);
      
      try
      {
         ImportPortletsResponse response = producer.importPortlets(importPortlets);
         ExtendedAssert.fail("ImportPortlets should fail if registration is required and non is provided");
      }
      catch (InvalidRegistration e)
      {
         //expected
      }
   }
   
   @Test
   public void testImportRegistrationRequired() throws Exception
   {
      producer.getConfigurationService().getConfiguration().getRegistrationRequirements().setRegistrationRequired(true);
      RegistrationData registrationData = WSRPTypeFactory.createRegistrationData("CONSUMER", true);
      RegistrationContext registrationContext = producer.register(registrationData);
      
      String importID = "foo";
      
      Lifetime lifetime = null;
      UserContext userContext = null;
      
      List<String> portletList = new ArrayList<String>();
      portletList.add(getDefaultHandle());
      ExportContext exportContextData = new ExportContext();
      byte[] importContext = exportContextData.encodeAsBytes();
      
      ImportPortlet importPortlet = createSimpleImportPortlet(importID, getDefaultHandle());
      List<ImportPortlet> importPortletsList = createImportPortletList(importPortlet);
      
      ImportPortlets importPortlets = WSRPTypeFactory.createImportPortlets(registrationContext, importContext, importPortletsList, userContext, lifetime);
      ImportPortletsResponse response = producer.importPortlets(importPortlets); 
   
      assertEquals(1,response.getImportedPortlets().size());
      ImportedPortlet portlet = response.getImportedPortlets().get(0);
      
      assertEquals(importID, portlet.getImportID());
      
      PortletContext portletContext = portlet.getNewPortletContext();
     
      //check that the new portlet handle is valid and we can access the portlet
      GetMarkup markup = createMarkupRequest(portletContext.getPortletHandle());
      markup.setRegistrationContext(registrationContext);
      
      MarkupResponse markupResponse = producer.getMarkup(markup);
      assertNotNull(markupResponse.getMarkupContext());
      assertEquals("<p>symbol unset stock value: value unset</p>", new String(markupResponse.getMarkupContext().getItemString()));
   }
   
   @Test
   public void testImportBadRegistration() throws Exception
   {
      producer.getConfigurationService().getConfiguration().getRegistrationRequirements().setRegistrationRequired(true);

      RegistrationContext registrationContext = WSRPTypeFactory.createRegistrationContext("FAkeREgistrationHAndle");
      
      String importID = "foo";
      
      Lifetime lifetime = null;
      UserContext userContext = null;
      
      List<String> portletList = new ArrayList<String>();
      portletList.add(getDefaultHandle());
      ExportContext exportContextData = new ExportContext();
      byte[] importContext = exportContextData.encodeAsBytes();
      
      ImportPortlet importPortlet = createSimpleImportPortlet(importID, getDefaultHandle());
      List<ImportPortlet> importPortletsList = createImportPortletList(importPortlet);
      
      ImportPortlets importPortlets = WSRPTypeFactory.createImportPortlets(registrationContext, importContext, importPortletsList, userContext, lifetime);
      try
      {
         ImportPortletsResponse response = producer.importPortlets(importPortlets);
         ExtendedAssert.fail("Should have failed when registration is required and an invalid registration handle is used.");
      }
      catch (InvalidRegistration e)
      {
         //expected
      }
   }
   
   @Test
   public void testImportNullImportContext() throws Exception
   {
      String importId = "importInvalidPortletContext";
      
      ImportPortlet importPortlet = createSimpleImportPortlet(importId, getDefaultHandle());
      
      byte[] importContext = null;
      List<ImportPortlet> importPortletsList = createImportPortletList(importPortlet);
      
      ImportPortlets importPortlets = createSimpleImportPortlets(importContext, importPortletsList);
      
      try
      {
         ImportPortletsResponse response = producer.importPortlets(importPortlets);
         ExtendedAssert.fail("Should have thrown an OperationFailedFault");
      }
      catch (OperationFailed e)
      {
         //expected
      }
   }
   
   @Test 
   public void testImportInvalidImportContext() throws Exception
   {
      String importId = "importInvalidPortletContext";
      
      ImportPortlet importPortlet = createSimpleImportPortlet(importId, getDefaultHandle());
      
      byte[] importContext = new byte[]{1,2,3,'f','a','k','e'};
      List<ImportPortlet> importPortletsList = createImportPortletList(importPortlet);
      
      ImportPortlets importPortlets = createSimpleImportPortlets(importContext, importPortletsList);
      
      try
      {
         ImportPortletsResponse response = producer.importPortlets(importPortlets);
         ExtendedAssert.fail("Should have thrown an OperationFailedFault");
      }
      catch (OperationFailed e)
      {
         //expected
      }
   }
   
   @Test
   public void testImportNullExportData() throws Exception
   {
      String importId = "nullExportData";
      
      ImportPortlet importPortlet = new ImportPortlet();
      importPortlet.setExportData(null);
      importPortlet.setImportID(importId);
      
      List<ImportPortlet> importPortletsList = createImportPortletList(importPortlet);
      
      byte[] importContext = new ExportContext().encodeAsBytes();
      
      ImportPortlets importPortlets = createSimpleImportPortlets(importContext, importPortletsList);
      
      ImportPortletsResponse response = producer.importPortlets(importPortlets);
      
      assertNotNull(response.getImportFailed());
      assertEquals(1, response.getImportFailed().size());
      
      assertEquals(importId, response.getImportFailed().get(0).getImportID().get(0));
      assertEquals("OperationFailed", response.getImportFailed().get(0).getErrorCode().getLocalPart());
   }
   
   @Test
   public void testImportInvalidExportData() throws Exception
   {
      String importId = "invalidExportData";
      
      ImportPortlet importPortlet = new ImportPortlet();
      importPortlet.setExportData("fake_export_data_123".getBytes());
      importPortlet.setImportID(importId);
      
      List<ImportPortlet> importPortletsList = createImportPortletList(importPortlet);
      
      ExportContext exportContextData = new ExportContext();
      byte[] importContext = exportContextData.encodeAsBytes();
      
      ImportPortlets importPortlets = createSimpleImportPortlets(importContext, importPortletsList);
      
      ImportPortletsResponse response = producer.importPortlets(importPortlets);
      
      assertNotNull(response.getImportFailed());
      assertEquals(1, response.getImportFailed().size());
      
      assertEquals(importId, response.getImportFailed().get(0).getImportID().get(0));
      assertEquals("OperationFailed", response.getImportFailed().get(0).getErrorCode().getLocalPart());
   }
   
   @Test
   public void testImportNonExistantPortletData() throws Exception
   {
      String importId = "invalidExportData";
      
      ExportManager exportManager = new ExportManagerImpl();
      ExportPortletData exportPortletData = exportManager.createExportPortletData(null, "non_existant_portlet_handle", null);
      byte[] exportData = exportPortletData.encodeAsBytes();
      
      ImportPortlet importPortlet = WSRPTypeFactory.createImportPortlet(importId, exportData);
      
      List<ImportPortlet> importPortletsList = createImportPortletList(importPortlet);
      
      ExportContext exportContextData = new ExportContext();
      byte[] importContext = exportContextData.encodeAsBytes();
      
      ImportPortlets importPortlets = createSimpleImportPortlets(importContext, importPortletsList);
      
      ImportPortletsResponse response = producer.importPortlets(importPortlets);
      
      assertNotNull(response.getImportFailed());
      assertEquals(1, response.getImportFailed().size());
      
      assertEquals(importId, response.getImportFailed().get(0).getImportID().get(0));
      assertEquals("InvalidHandle", response.getImportFailed().get(0).getErrorCode().getLocalPart());
   }
   
   @Test
   public void testImports() throws Exception
   {
      String importID = "foo";
      String nullImportID = "null";
      String invalidImportID = "invalid";
      
      ExportManager exportManager = new ExportManagerImpl();
      ExportPortletData exportPortletData = exportManager.createExportPortletData(null, getDefaultHandle(), null);
      byte[] exportData = exportPortletData.encodeAsBytes();
      
      byte[] nullExportData = null;
      
      ExportPortletData invalidExportPortletData = exportManager.createExportPortletData(null, "InvalidHandle", null);
      byte[] invalidExportData = invalidExportPortletData.encodeAsBytes();
      
      Lifetime lifetime = null;
      UserContext userContext = null;
      RegistrationContext registrationContext = null;
      
      List<String> portletList = new ArrayList<String>();
      portletList.add(getDefaultHandle());
      ExportContext exportContextData = new ExportContext();
      byte[] importContext = exportContextData.encodeAsBytes();
      
      ImportPortlet nullPortlet = new ImportPortlet();
      nullPortlet.setImportID(nullImportID);
      nullPortlet.setExportData(nullExportData);
      ImportPortlet importPortlet = WSRPTypeFactory.createImportPortlet(importID, exportData);
      ImportPortlet invalidPortlet = WSRPTypeFactory.createImportPortlet(invalidImportID, invalidExportData);
      
      List<ImportPortlet> importPortletsList = new ArrayList<ImportPortlet>();
      importPortletsList.add(invalidPortlet);
      importPortletsList.add(importPortlet);
      importPortletsList.add(nullPortlet);
     
      ImportPortlets importPortlets = WSRPTypeFactory.createImportPortlets(registrationContext, importContext, importPortletsList, userContext, lifetime);
      ImportPortletsResponse response = producer.importPortlets(importPortlets); 
   
      assertEquals(2, response.getImportFailed().size());
      
      assertEquals(1,response.getImportedPortlets().size());
      
      ImportedPortlet portlet = response.getImportedPortlets().get(0);
      assertEquals(importID, portlet.getImportID());
      
      PortletContext portletContext = portlet.getNewPortletContext();
     
      //check that the new portlet handle is valid and we can access the portlet
      GetMarkup markup = createMarkupRequest(portletContext.getPortletHandle());
      MarkupResponse markupResponse = producer.getMarkup(markup);
      assertNotNull(markupResponse.getMarkupContext());
      assertEquals("<p>symbol unset stock value: value unset</p>", new String(markupResponse.getMarkupContext().getItemString()));
   }
      
   @Test
   public void testExportWithState() throws Exception
   {
      undeploy(TEST_BASIC_PORTLET_WAR);
      String sessionPortletArchive = "test-portletstate-portlet.war";
      deploy(sessionPortletArchive);
      
      try
      {
         String originalHandle = getHandleForCurrentlyDeployedArchive();
         
         //check the session portlet to make sure its at the inital state
         checkStatePortlet(originalHandle, "initial");
         
         PortletContext portletContext = performBlockingInteractionOnSessionPortlet(originalHandle, "new value", StateChange.CLONE_BEFORE_WRITE);
         //check that we have a new portlet context
         ExtendedAssert.assertFalse(originalHandle.equals(portletContext.getPortletHandle()));
         
         checkStatePortlet(portletContext.getPortletHandle(), "new value");
         
         List<PortletContext> portletContexts = createPortletContextList(portletContext.getPortletHandle());
         ExportPortlets exportPortlets = createSimpleExportPortlets(portletContexts);
         ExportPortletsResponse response = producer.exportPortlets(exportPortlets);
         
         ExtendedAssert.assertFalse(response.getExportedPortlet().isEmpty());
         
         List<PortletContext> portletContextsFromExport = getPortletContext(response);
         
         ExtendedAssert.assertNotNull(portletContextsFromExport.isEmpty());
         ExtendedAssert.assertEquals(1,portletContexts.size());
         
         PortletContext portletContextFromExport = portletContextsFromExport.get(0);
         //we should be getting the handle of the stateless portlet
         assertEquals(originalHandle, portletContextFromExport.getPortletHandle());
         //assert that we have a portlet state returned
         assertNotNull(portletContextFromExport.getPortletState());
         
         //quick check that the imported portlet has the right state
         ImportPortletsResponse importResponse = createImportPortletsResponse("foo", portletContextFromExport);
         checkStatePortlet(importResponse.getImportedPortlets().get(0).getNewPortletContext().getPortletHandle(), "new value");
         
      }
      finally
      {
         undeploy(sessionPortletArchive);
      }
   }
   
   //Tests the situation in which we have a stateful export from one server and importing into another
   @Test
   public void testImportWithState() throws Exception
   {
      undeploy(TEST_BASIC_PORTLET_WAR);
      String sessionPortletArchive = "test-portletstate-portlet.war";
      deploy(sessionPortletArchive);

      try
      {
         String importStringValue = "import value";
         byte[] portletState = createSessionByteValue(getHandleForCurrentlyDeployedArchive(), importStringValue);

         String importID = "foo";

         List<String> portletList = new ArrayList<String>();
         portletList.add(getDefaultHandle());
         byte[] importContext = new ExportContext().encodeAsBytes();

         ExportPortletData exportPortletData = new ExportPortletData(getHandleForCurrentlyDeployedArchive(), portletState);
         byte[] exportData = exportPortletData.encodeAsBytes();
         ImportPortlet importPortlet =  WSRPTypeFactory.createImportPortlet(importID, exportData);

         List<ImportPortlet> importPortletsList = createImportPortletList(importPortlet);

         ImportPortlets importPortlets = createSimpleImportPortlets(importContext, importPortletsList);
         ImportPortletsResponse response = producer.importPortlets(importPortlets); 

         ImportedPortlet importedPortlet = response.getImportedPortlets().get(0);
         
         //since its a stateful, the portlet handles shouldn't be the same
         assertNotSame(getHandleForCurrentlyDeployedArchive(), importedPortlet.getNewPortletContext().getPortletHandle());
         //the pc should be storing the state, so it shouldn't appear in the imported portlet context
         assertNull(importedPortlet.getNewPortletContext().getPortletState());
         
         checkStatePortlet(importedPortlet.getNewPortletContext().getPortletHandle(), importStringValue);

      }
      finally
      {
         undeploy(sessionPortletArchive);
      }
   }
   
   protected ImportPortletsResponse createImportPortletsResponse(String importID, PortletContext portletContext) throws Exception
   {
      byte[] importContext = new ExportContext().encodeAsBytes();
      ExportPortletData exportPortletData = new ExportPortletData(portletContext.getPortletHandle(), portletContext.getPortletState());
      byte[] exportData = exportPortletData.encodeAsBytes();
      ImportPortlet importPortlet =  WSRPTypeFactory.createImportPortlet(importID, exportData);
      List<ImportPortlet> importPortletsList = createImportPortletList(importPortlet);
      ImportPortlets importPortlets = createSimpleImportPortlets(importContext, importPortletsList);
      return producer.importPortlets(importPortlets);
   }
   
   protected byte[] createSessionByteValue(String portletHandle, String value) throws Exception
   {
      ProducerPortletInvoker ppinvoker = (ProducerPortletInvoker) producer.getPortletInvoker();
      Map<String, List<String>> properties = new HashMap<String, List<String>>();
      List<String> values = new ArrayList<String>();
      values.add(value);
      properties.put("name", values);
      
      PropertyMap property = new SimplePropertyMap(properties);
      PortletState sstate = new PortletState(portletHandle, property);
      return ppinvoker.getStateConverter().marshall(PortletStateType.OPAQUE, sstate);
   }
   
   @Test
   public void testExportWithoutSession() throws Exception
   {
      undeploy(TEST_BASIC_PORTLET_WAR);
      String sessionPortletArchive = "test-portletstate-portlet.war";
      deploy(sessionPortletArchive);
      
      try
      {
         String originalHandle = getHandleForCurrentlyDeployedArchive();
         
         //check the session portlet to make sure its at the inital state
         checkStatePortlet(originalHandle, "initial");
                  
         //export the cloned portlet context we get from the performBlockingInteraction
         List<PortletContext> portletContexts = createPortletContextList(originalHandle);
         ExportPortlets exportPortlets = createSimpleExportPortlets(portletContexts);
         ExportPortletsResponse response = producer.exportPortlets(exportPortlets);
         
         ExtendedAssert.assertFalse(response.getExportedPortlet().isEmpty());
         
         List<PortletContext> portletContextsFromExport = getPortletContext(response);
         
         ExtendedAssert.assertNotNull(portletContextsFromExport.isEmpty());
         ExtendedAssert.assertEquals(1,portletContexts.size());
         
         PortletContext portletContextFromExport = portletContextsFromExport.get(0);
         assertEquals(originalHandle, portletContextFromExport.getPortletHandle());
      }
      finally
      {
         undeploy(sessionPortletArchive);
      }
   }
   
   protected List<PortletContext> getPortletContext(ExportPortletsResponse exportPortletsResponse) throws Exception
   {
      List<PortletContext> portletContexts = new ArrayList<PortletContext>();
      
      ExportManager exportManager = new ExportManagerImpl();
      ExportContext exportContext = exportManager.createExportContext(exportPortletsResponse.getExportContext());
      
      List<ExportedPortlet> exportedPortlets = exportPortletsResponse.getExportedPortlet();
      
      for (ExportedPortlet exportPortlet : exportedPortlets)
      {
         ExportPortletData exportPortletData = exportManager.createExportPortletData(exportContext, exportPortletsResponse.getLifetime(), exportPortlet.getExportData());
         String portletHandle = exportPortletData.getPortletHandle();
         byte[] portletState = exportPortletData.getPortletState();
         portletContexts.add(WSRPTypeFactory.createPortletContext(portletHandle, portletState));
      }
      
      return portletContexts;
   }
   
   protected void checkStatePortlet(String handle, String expectedValue) throws Exception
   {
      GetMarkup getMarkupOriginalStateless = createMarkupRequest(handle);
      MarkupResponse responseOriginalStateless = producer.getMarkup(getMarkupOriginalStateless);
      ExtendedAssert.assertEquals(expectedValue, responseOriginalStateless.getMarkupContext().getItemString());
   }
   
   protected PortletContext performBlockingInteractionOnSessionPortlet(String handle, String value, StateChange stateChange) throws Exception
   {
      //perform a blocking interaction to set a state on the portlet;
      PerformBlockingInteraction pbi = WSRPTypeFactory.createDefaultPerformBlockingInteraction(handle);
      pbi.getInteractionParams().setPortletStateChange(stateChange);
      pbi.getInteractionParams().getFormParameters().add(createNamedString("value", value));
      BlockingInteractionResponse response = producer.performBlockingInteraction(pbi);
      PortletContext portletContext = response.getUpdateResponse().getPortletContext();
      
      return portletContext;
   }

   private NamedString createNamedString(String name, String value)
   {
      NamedString namedString = new NamedString();
      namedString.setName(name);
      namedString.setValue(value);
      return namedString;
   }
   
   protected ImportPortlet createSimpleImportPortlet(String importId, String handle) throws IOException
   {
      ExportPortletData exportPortletData = new ExportPortletData(handle, null);
      byte[] exportData = exportPortletData.encodeAsBytes();
      return WSRPTypeFactory.createImportPortlet(importId, exportData);
   }
   
   protected List<ImportPortlet> createImportPortletList(ImportPortlet... importPortlets)
   {
      List<ImportPortlet> importPortletList = new ArrayList<ImportPortlet>();
      
      for (ImportPortlet importPortlet : importPortlets)
      {
         importPortletList.add(importPortlet);
      }
      return importPortletList;
   }
   
   protected ImportPortlets createSimpleImportPortlets(byte[] importContext, List<ImportPortlet> importPortletsList)
   {
      Lifetime lifetime = null;
      UserContext userContext = null;
      RegistrationContext registrationContext = null;
      
      return WSRPTypeFactory.createImportPortlets(registrationContext, importContext, importPortletsList, userContext, lifetime);
   }
   
   protected String getMostUsedPortletWARFileName()
   {
      return TEST_BASIC_PORTLET_WAR;
   }

}

