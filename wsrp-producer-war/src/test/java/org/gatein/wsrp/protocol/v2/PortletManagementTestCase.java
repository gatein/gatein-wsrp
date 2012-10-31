/*
 * JBoss, a division of Red Hat
 * Copyright 2011, Red Hat Middleware, LLC, and individual
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

package org.gatein.wsrp.protocol.v2;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.RequestFacade;
import org.gatein.exports.ExportManager;
import org.gatein.exports.data.ExportContext;
import org.gatein.exports.data.ExportPortletData;
import org.gatein.exports.data.PersistedExportData;
import org.gatein.exports.impl.ExportManagerImpl;
import org.gatein.pc.api.PortletStateType;
import org.gatein.pc.api.state.PropertyMap;
import org.gatein.pc.portlet.impl.state.StateConverterV0;
import org.gatein.pc.portlet.state.SimplePropertyMap;
import org.gatein.pc.portlet.state.StateConverter;
import org.gatein.pc.portlet.state.producer.PortletState;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.api.servlet.ServletAccess;
import org.gatein.wsrp.portlet.utils.MockRequest;
import org.gatein.wsrp.support.TestMockExportPersistenceManager;
import org.gatein.wsrp.test.support.MockHttpServletResponse;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OverProtocol;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oasis.wsrp.v2.BlockingInteractionResponse;
import org.oasis.wsrp.v2.ClonePortlet;
import org.oasis.wsrp.v2.CopiedPortlet;
import org.oasis.wsrp.v2.CopyPortlets;
import org.oasis.wsrp.v2.CopyPortletsResponse;
import org.oasis.wsrp.v2.ExportPortlets;
import org.oasis.wsrp.v2.ExportPortletsResponse;
import org.oasis.wsrp.v2.ExportedPortlet;
import org.oasis.wsrp.v2.FailedPortlets;
import org.oasis.wsrp.v2.GetMarkup;
import org.oasis.wsrp.v2.ImportPortlet;
import org.oasis.wsrp.v2.ImportPortlets;
import org.oasis.wsrp.v2.ImportPortletsResponse;
import org.oasis.wsrp.v2.ImportedPortlet;
import org.oasis.wsrp.v2.InvalidHandle;
import org.oasis.wsrp.v2.InvalidRegistration;
import org.oasis.wsrp.v2.Lifetime;
import org.oasis.wsrp.v2.MarkupContext;
import org.oasis.wsrp.v2.MarkupResponse;
import org.oasis.wsrp.v2.MissingParameters;
import org.oasis.wsrp.v2.NamedString;
import org.oasis.wsrp.v2.OperationFailed;
import org.oasis.wsrp.v2.PerformBlockingInteraction;
import org.oasis.wsrp.v2.PortletContext;
import org.oasis.wsrp.v2.RegistrationContext;
import org.oasis.wsrp.v2.RegistrationData;
import org.oasis.wsrp.v2.ReleaseExport;
import org.oasis.wsrp.v2.SetExportLifetime;
import org.oasis.wsrp.v2.StateChange;
import org.oasis.wsrp.v2.UserContext;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
   @OverProtocol("Servlet 2.5")
   public static Archive createDeployment()
   {
      return V2ProducerBaseTest.createDeployment();
   }

   @Before
   public void setUp() throws Exception
   {
      super.setUp();
      //hack to get around having to have a httpservletrequest when accessing the producer services
      //I don't know why its really needed, seems to be a dependency where wsrp connects with the pc module

      //NOTE: ideally we could just use the MockHttpServlerRequest and Response, but JBossWeb is looking for particular implementations,
      //      we we havce to use the Catalina specific classes. Interestingly, its only appears that JBossWeb requires these classes and not upstream Tomcat
      //      ServletAccess.setRequestAndResponse(MockHttpServletRequest.createMockRequest(null), MockHttpServletResponse
      //            .createMockResponse());

      Request request = new MockRequest();
      request.setCoyoteRequest(new org.apache.coyote.Request());

      RequestFacade requestFacade = new RequestFacade(request);
      ServletAccess.setRequestAndResponse(requestFacade, MockHttpServletResponse.createMockResponse());
   }

   @After
   public void tearDown() throws Exception
   {
      super.tearDown();
   }

   /*TODO:
   * - tests usercontexts (not sure exactly what needs to be tested for this)
   */

   @Test
   public void testExport() throws Exception
   {
      try
      {
         String handle = getDefaultHandle();
         List<PortletContext> portletContexts = createPortletContextList(handle);

         ExportPortlets exportPortlets = createSimpleExportPortlets(portletContexts);
         ExportPortletsResponse response = producer.exportPortlets(exportPortlets);

         checkValidHandle(response, handle);
      }
      catch (Exception e)
      {
         System.out.println("An exception occurred when running testExport");
         e.printStackTrace();
         throw new Exception(e);
      }
   }

   @Test
   public void testExportNonExistentHandle() throws Exception
   {
      String nonExistentHandle = "123FakeHandle";
      List<PortletContext> portletContexts = createPortletContextList(nonExistentHandle);

      ExportPortlets exportPortlets = createSimpleExportPortlets(portletContexts);
      ExportPortletsResponse response = producer.exportPortlets(exportPortlets);

      checkInvalidHandle(response, nonExistentHandle);
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
         fail("ExportPortlets should fail if registration is required and none is provided");
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
      ExportPortlets exportPortlets = WSRPTypeFactory.createExportPortlets(registrationContext, portletContexts, userContext, lifetime, exportByValueRequired);

      try
      {
         ExportPortletsResponse response = producer.exportPortlets(exportPortlets);
         fail("ExportPortlets should fail if registration is required and an invalid registration handle is provided");
      }
      catch (InvalidRegistration e)
      {
         //expected
      }
   }

   @Test
   public void testExportRegistrationRequired() throws Exception
   {
      try
      {
         producer.getConfigurationService().getConfiguration().getRegistrationRequirements().setRegistrationRequired(true);

         RegistrationData registrationData = WSRPTypeFactory.createRegistrationData("CONSUMER", "CONSUMERAGENT.0.0", true);
         RegistrationContext registrationContext = producer.register(WSRPTypeFactory.createRegister(registrationData, null, null));

         List<PortletContext> portletContexts = createPortletContextList(getDefaultHandle());

         boolean exportByValueRequired = true;
         Lifetime lifetime = null;
         UserContext userContext = null;
         ExportPortlets exportPortlets = WSRPTypeFactory.createExportPortlets(registrationContext, portletContexts, userContext, lifetime, exportByValueRequired);

         ExportPortletsResponse response = producer.exportPortlets(exportPortlets);

         checkValidHandle(response, getDefaultHandle());
      }
      catch (Exception e)
      {
         //arquillian can't handle non serializable exceptions, print error message to the server logs
         System.out.println("An exception occured calling " + this.getClass() + " testExportRegistrationRequired");
         e.printStackTrace();
         throw new Exception(e);
      }
   }

   @Test
   public void testExportNullExportContext() throws Exception
   {
      ExportPortlets exportPortlets = new ExportPortlets();
      try
      {
         ExportPortletsResponse response = producer.exportPortlets(exportPortlets);
         fail("Should have thrown a MissingParameters fault if no portlets passed for export.");
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
         fail("Should have failed if sending a null exportPortlet object");
      }
      catch (MissingParameters e)
      {
         //expected
      }
   }

   @Test
   public void testExports() throws Exception
   {
      String nonExistentHandle = "123FakeHandle";
      String handle = getDefaultHandle();
      List<PortletContext> portletContexts = createPortletContextList(nonExistentHandle, handle);

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
      assertEquals("InvalidHandle", failedPortlets.getErrorCode().getLocalPart());
      assertEquals(1, failedPortlets.getPortletHandles().size());
      assertTrue(failedPortlets.getPortletHandles().contains(nonExistentHandle));
   }

   protected void checkInvalidHandle(ExportPortletsResponse response, String handle) throws Exception
   {
      assertNotNull(response.getExportContext());
      assertNull(response.getLifetime());
      assertTrue(response.getExportedPortlet().isEmpty());
      assertEquals(1, response.getFailedPortlets().size());
      FailedPortlets failedPortlet = response.getFailedPortlets().get(0);
      assertTrue(failedPortlet.getPortletHandles().contains(handle));
      assertEquals("InvalidHandle", failedPortlet.getErrorCode().getLocalPart());
   }

   protected void checkValidHandle(ExportPortletsResponse response, String handle) throws Exception
   {
      assertNotNull(response.getExportContext());
      assertNull(response.getLifetime());
      assertTrue(response.getFailedPortlets().isEmpty());
      assertEquals(1, response.getExportedPortlet().size());
      ExportedPortlet exportPortlet = response.getExportedPortlet().get(0);
      assertEquals(handle, exportPortlet.getPortletHandle());
   }

   protected List<PortletContext> createPortletContextList(String... portletHandles)
   {
      List<PortletContext> portletContexts = new ArrayList<PortletContext>();

      for (String portletHandle : portletHandles)
      {
         PortletContext portletContext = WSRPTypeFactory.createPortletContext(portletHandle);
         portletContexts.add(portletContext);
      }
      return portletContexts;
   }

   protected ExportPortlets createSimpleExportPortlets(List<PortletContext> portletContexts)
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

      assertEquals(1, response.getImportedPortlets().size());
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
         fail("ImportPortlets should fail if registration is required and non is provided");
      }
      catch (InvalidRegistration e)
      {
         //expected
      }
   }

   @Test
   public void testImportRegistrationRequired() throws Exception
   {
      try
      {
         producer.getConfigurationService().getConfiguration().getRegistrationRequirements().setRegistrationRequired(true);
         RegistrationData registrationData = WSRPTypeFactory.createRegistrationData("CONSUMER", "CONSUMERAGENT.0.0", true);
         RegistrationContext registrationContext = producer.register(WSRPTypeFactory.createRegister(registrationData, null, null));

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

         assertEquals(1, response.getImportedPortlets().size());
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
      catch (Exception e)
      {
         //arquillian can't handle non serializable exceptions, print error message to the server logs
         System.out.println("An exception occured calling " + this.getClass() + " testImportRegistrationRequired");
         e.printStackTrace();
         throw new Exception(e);
      }
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
         fail("Should have failed when registration is required and an invalid registration handle is used.");
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
         producer.importPortlets(importPortlets);
         fail("Should have thrown an OperationFailedFault");
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

      byte[] importContext = new byte[]{1, 2, 3, 'f', 'a', 'k', 'e'};
      List<ImportPortlet> importPortletsList = createImportPortletList(importPortlet);

      ImportPortlets importPortlets = createSimpleImportPortlets(importContext, importPortletsList);

      try
      {
         producer.importPortlets(importPortlets);
         fail("Should have thrown an OperationFailedFault");
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

      assertEquals(1, response.getImportedPortlets().size());

      ImportedPortlet portlet = response.getImportedPortlets().get(0);
      assertEquals(importID, portlet.getImportID());

      PortletContext portletContext = portlet.getNewPortletContext();

      //check that the new portlet handle is valid and we can access the portlet
      GetMarkup markup = createMarkupRequest(portletContext.getPortletHandle());
      MarkupResponse markupResponse = producer.getMarkup(markup);
      assertNotNull(markupResponse.getMarkupContext());
      assertEquals("<p>symbol unset stock value: value unset</p>", markupResponse.getMarkupContext().getItemString());
   }

   @Test
   public void testExportWithState() throws Exception
   {
      try
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
            assertFalse(originalHandle.equals(portletContext.getPortletHandle()));

            checkStatePortlet(portletContext.getPortletHandle(), "new value");

            List<PortletContext> portletContexts = createPortletContextList(portletContext.getPortletHandle());
            ExportPortlets exportPortlets = createSimpleExportPortlets(portletContexts);
            ExportPortletsResponse response = producer.exportPortlets(exportPortlets);

            assertFalse(response.getExportedPortlet().isEmpty());

            List<PortletContext> portletContextsFromExport = getPortletContext(response);

            assertNotNull(portletContextsFromExport.isEmpty());
            assertEquals(1, portletContexts.size());

            PortletContext portletContextFromExport = portletContextsFromExport.get(0);
            //we should be getting the handle of the stateless portlet
            assertEquals(originalHandle, portletContextFromExport.getPortletHandle());
            //assert that we have a portlet state returned
            assertNotNull(portletContextFromExport.getPortletState());

            //quick check that the imported portlet has the right state
            ImportPortletsResponse importResponse = createImportPortletsResponse("foo", portletContextFromExport);
            assertEquals(1, importResponse.getImportedPortlets().size());
            checkStatePortlet(importResponse.getImportedPortlets().get(0).getNewPortletContext().getPortletHandle(), "new value");
         }
         finally
         {
            undeploy(sessionPortletArchive);
         }
      }
      catch (Exception e)
      {
         System.out.println("ERROR: an error occured " + this.getClass() + " testExportWithState");
         e.printStackTrace();
         throw e;
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
         ImportPortlet importPortlet = WSRPTypeFactory.createImportPortlet(importID, exportData);

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
      ImportPortlet importPortlet = WSRPTypeFactory.createImportPortlet(importID, exportData);
      List<ImportPortlet> importPortletsList = createImportPortletList(importPortlet);
      ImportPortlets importPortlets = createSimpleImportPortlets(importContext, importPortletsList);
      return producer.importPortlets(importPortlets);
   }

   protected byte[] createSessionByteValue(String portletHandle, String value) throws Exception
   {
      Map<String, List<String>> properties = new HashMap<String, List<String>>();
      List<String> values = new ArrayList<String>();
      values.add(value);
      properties.put("name", values);

      PropertyMap property = new SimplePropertyMap(properties);
      PortletState sstate = new PortletState(portletHandle, property);

      StateConverter stateConverter = new StateConverterV0();
      return stateConverter.marshall(PortletStateType.OPAQUE, sstate);
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

         assertFalse(response.getExportedPortlet().isEmpty());

         List<PortletContext> portletContextsFromExport = getPortletContext(response);

         assertNotNull(portletContextsFromExport.isEmpty());
         assertEquals(1, portletContexts.size());

         PortletContext portletContextFromExport = portletContextsFromExport.get(0);
         assertEquals(originalHandle, portletContextFromExport.getPortletHandle());
      }
      finally
      {
         undeploy(sessionPortletArchive);
      }
   }

   @Test
   public void testReleaseExportsThrowsNoErrors() throws Exception
   {
      //NOTE: this test should never cause any errors
      //TODO: once the export by reference is done, we need to write tests that this works

      //null releaseExport
      producer.releaseExport(null);

      //empty releaseExport
      ReleaseExport releaseExport = new ReleaseExport();
      producer.releaseExport(releaseExport);

      //empty releaseExport with a bad export context
      releaseExport = new ReleaseExport();
      releaseExport.setExportContext(new byte[]{-12, 12, 25, 21, 53});
      producer.releaseExport(releaseExport);

      //bad registration handle
      releaseExport = new ReleaseExport();
      RegistrationContext registrationContext = new RegistrationContext();
      registrationContext.setRegistrationHandle("badRegistrationHandle");
      releaseExport.setRegistrationContext(registrationContext);
      producer.releaseExport(releaseExport);

      //bad user context
      releaseExport = new ReleaseExport();
      UserContext userContext = new UserContext();
      userContext.setUserContextKey("baduckey");
      releaseExport.setUserContext(userContext);
      producer.releaseExport(releaseExport);
   }

   @Test
   public void testReleaseExportsNoErrorsRequiresRegistraion() throws Exception
   {
      producer.getConfigurationService().getConfiguration().getRegistrationRequirements().setRegistrationRequired(true);
      this.testReleaseExportsThrowsNoErrors();
   }

   @Test
   public void testExportPortletPM() throws Exception
   {
      exportPortletToPM();
   }

   @Test
   public void testReleaseExportPM() throws Exception
   {
      ExportPortletsResponse response = exportPortletToPM();

      ReleaseExport releaseExport = WSRPTypeFactory.createReleaseExport(null, response.getExportContext(), null);
      producer.releaseExport(releaseExport);

      //Test that doing a release export actually removed the stored RefId
      TestMockExportPersistenceManager persistenceManager = (TestMockExportPersistenceManager)producer.getExportManager().getPersistenceManager();
      assertEquals(0, persistenceManager.getExportContextKeys().size());
      assertEquals(0, persistenceManager.getExportPortletsKeys().size());
   }

   @Test
   public void testImportPortletPM() throws Exception
   {
      ExportPortletsResponse response = exportPortletToPM();

      String importID = "foo";
      ImportPortlet importPortlet = WSRPTypeFactory.createImportPortlet(importID, response.getExportedPortlet().get(0).getExportData());

      List<ImportPortlet> importPortletsList = createImportPortletList(importPortlet);

      ImportPortlets importPortlets = createSimpleImportPortlets(response.getExportContext(), importPortletsList);
      ImportPortletsResponse importResponse = producer.importPortlets(importPortlets);

      assertEquals(1, importResponse.getImportedPortlets().size());
      assertEquals(importID, importResponse.getImportedPortlets().get(0).getImportID());
      assertNotNull(importResponse.getImportedPortlets().get(0).getNewPortletContext().getPortletHandle());
   }

   protected ExportPortletsResponse exportPortletToPM() throws Exception
   {
      TestMockExportPersistenceManager persistenceManager = new TestMockExportPersistenceManager();
      producer.getExportManager().setPersistenceManager(persistenceManager);
      ((ExportManagerImpl)producer.getExportManager()).setPreferExportByValue(false);

      String handle = getDefaultHandle();
      List<PortletContext> portletContexts = createPortletContextList(handle);

      ExportPortlets exportPortlets = createSimpleExportPortlets(portletContexts);
      exportPortlets.setExportByValueRequired(false);

      //Test that we don't have anything in the PM before doing an export
      assertEquals(0, persistenceManager.getExportContextKeys().size());
      assertEquals(0, persistenceManager.getExportPortletsKeys().size());

      ExportPortletsResponse response = producer.exportPortlets(exportPortlets);

      //Test that we have an entry in the PM after doing an export
      assertEquals(1, persistenceManager.getExportContextKeys().size());
      assertEquals(1, persistenceManager.getExportPortletsKeys().size());

      return response;
   }

   @Test
   public void testSetExportLifetimeNull() throws Exception
   {
      try
      {
         producer.setExportLifetime(null);
         fail();
      }
      catch (OperationFailed e)
      {
         //expected
      }
   }

   @Test
   public void testSetExportLifetimeInvalidExportContext() throws Exception
   {
      try
      {
         SetExportLifetime setExportLifetime = WSRPTypeFactory.createSetExportLifetime(null, new byte[]{-10, 24, 24, 54, 'a', 'f', 'g'}, null, null);
         producer.setExportLifetime(setExportLifetime);
         fail();
      }
      catch (OperationFailed e)
      {
         //expected
      }
   }

   @Test
   public void testSetExport() throws Exception
   {
      String handle = getDefaultHandle();
      List<PortletContext> portletContexts = createPortletContextList(handle);

      ExportPortlets exportPortlets = createSimpleExportPortlets(portletContexts);
      ExportPortletsResponse response = producer.exportPortlets(exportPortlets);

      try
      {
         PersistedExportData exportData = new PersistedExportData("foo", "bar");

         SetExportLifetime setExportLifetime = WSRPTypeFactory.createSetExportLifetime(null, exportData.encodeAsBytes(), null, null);
         producer.setExportLifetime(setExportLifetime);
         fail();
      }
      catch (OperationFailed e)
      {
         //expected
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
         ExportPortletData exportPortletData;
         Lifetime lifetime = exportPortletsResponse.getLifetime();
         if (lifetime != null)
         {
            long currentTime = lifetime.getCurrentTime().toGregorianCalendar().getTime().getTime();
            long terminationTime = lifetime.getTerminationTime().toGregorianCalendar().getTime().getTime();
            long refreshDuration = lifetime.getRefreshDuration().getTimeInMillis(lifetime.getCurrentTime().toGregorianCalendar());
            exportPortletData = exportManager.createExportPortletData(exportContext, currentTime, terminationTime, refreshDuration, exportPortlet.getExportData());
         }
         else
         {
            exportPortletData = exportManager.createExportPortletData(exportContext, -1, -1, -1, exportPortlet.getExportData());
         }
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
      assertEquals(expectedValue, responseOriginalStateless.getMarkupContext().getItemString());
   }

   protected PortletContext performBlockingInteractionOnSessionPortlet(String handle, String value, StateChange stateChange) throws Exception
   {
      //perform a blocking interaction to set a state on the portlet;
      PerformBlockingInteraction pbi = createDefaultPerformBlockingInteraction(handle);
      pbi.getInteractionParams().setPortletStateChange(stateChange);
      NamedString namedString = WSRPTypeFactory.createNamedString("value", value);
      pbi.getInteractionParams().getFormParameters().add(namedString);
      BlockingInteractionResponse response = producer.performBlockingInteraction(pbi);
      PortletContext portletContext = response.getUpdateResponse().getPortletContext();

      return portletContext;
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


   /** Simpliest check to make sure copy portlets is working */
   @Test
   public void testSimpleCopyPortletNullRegistrations() throws Exception
   {
      String handle = getDefaultHandle();
      List<PortletContext> portletContexts = createPortletContextList(handle);

      CopyPortlets copyPortlets = createSimpleCopyPortlets(portletContexts);
      CopyPortletsResponse response = producer.copyPortlets(copyPortlets);

      checkSimpleCopyPortletsResponse(response, createStringList(handle), createStringList());

      //Check that the proper registration context can access it
      checkDefaultMarkup(response.getCopiedPortlets().get(0).getNewPortletContext().getPortletHandle(), null);
   }

   /**
    * Check copyPortlet from a null registration context to a non null registration context Note: if the
    * toRegistrationContext is null, then it should use the fromRegistrationContext, which is this case is also null.
    * See also testSimpleCopyPortletToRegistrationNull()
    */
   @Test
   public void testSimpleCopyPortletFromRegistrationNull() throws Exception
   {
      try
      {
         RegistrationData toRegistrationData = WSRPTypeFactory.createRegistrationData("CONSUMERB", "CONSUMERAGENTB.0.0", true);
         RegistrationContext toRegistrationContext = producer.register(WSRPTypeFactory.createRegister(toRegistrationData, null, null));

         String handle = getDefaultHandle();
         List<PortletContext> portletContexts = createPortletContextList(handle);

         CopyPortlets copyPortlets = createSimpleCopyPortlets(portletContexts);
         copyPortlets.setToRegistrationContext(toRegistrationContext);

         CopyPortletsResponse response = producer.copyPortlets(copyPortlets);

         checkSimpleCopyPortletsResponse(response, createStringList(handle), createStringList());

         //Check that the proper registration context can access it
         checkDefaultMarkup(response.getCopiedPortlets().get(0).getNewPortletContext().getPortletHandle(), toRegistrationContext);

         //Check that the null registration cannot access it
         try
         {
            checkDefaultMarkup(response.getCopiedPortlets().get(0).getNewPortletContext().getPortletHandle(), null);
            fail("The null registration context should not be able to access this portlet");
         }
         catch (InvalidHandle e)
         {
            //expected
         }
      }
      catch (Exception e)
      {
         System.out.println("ERROR: An exception occurred when running testSimpleCopyPortletFromRegistrationNull");
         e.printStackTrace();
         throw new Exception(e);
      }
   }

   /**
    * Check copyPortlets from a non-null registration context to a null registration context. Note: this does _NOT_
    * mean
    * the copy should be available from a non registered consumer, the spec states if the toRegistration is null, it
    * should be registered using the fromRegistrationContext
    */
   @Test
   public void testSimpleCopyPortletToRegistrationNull() throws Exception
   {
      //producer.getConfigurationService().getConfiguration().getRegistrationRequirements().setRegistrationRequired(true);

      RegistrationData fromRegistrationData = WSRPTypeFactory.createRegistrationData("CONSUMERA", "CONSUMERAGENAT.0.0", true);
      RegistrationContext fromRegistrationContext = producer.register(WSRPTypeFactory.createRegister(fromRegistrationData, null, null));

      String handle = getDefaultHandle();
      List<PortletContext> portletContexts = createPortletContextList(handle);

      CopyPortlets copyPortlets = createSimpleCopyPortlets(portletContexts);
      //note: createSimpleCopyPortlets sets the toRegistrationContext to null
      copyPortlets.setFromRegistrationContext(fromRegistrationContext);

      CopyPortletsResponse response = producer.copyPortlets(copyPortlets);

      checkSimpleCopyPortletsResponse(response, createStringList(handle), createStringList());

      //Check that the proper registration context can access it
      checkDefaultMarkup(response.getCopiedPortlets().get(0).getNewPortletContext().getPortletHandle(), fromRegistrationContext);

      //Check that the null registration cannot access it
      try
      {
         checkDefaultMarkup(response.getCopiedPortlets().get(0).getNewPortletContext().getPortletHandle(), null);
         fail("The null registration context should not be able to access this portlet");
      }
      catch (InvalidHandle e)
      {
         //expected
      }
   }

   /** Check that we can copy one portlet from one registration context to another */
   @Test
   public void testSimpleCopyPortletWithRegistrations() throws Exception
   {
      //producer.getConfigurationService().getConfiguration().getRegistrationRequirements().setRegistrationRequired(true);

      RegistrationData fromRegistrationData = WSRPTypeFactory.createRegistrationData("CONSUMERA", "CONSUMERAGENTA.0.0", true);
      RegistrationContext fromRegistrationContext = producer.register(WSRPTypeFactory.createRegister(fromRegistrationData, null, null));
      RegistrationData toRegistrationData = WSRPTypeFactory.createRegistrationData("CONSUMERB", "CONSUMERAGENTB.0.0", true);
      RegistrationContext toRegistrationContext = producer.register(WSRPTypeFactory.createRegister(toRegistrationData, null, null));

      String handle = getDefaultHandle();
      List<PortletContext> portletContexts = createPortletContextList(handle);

      CopyPortlets copyPortlets = createSimpleCopyPortlets(portletContexts);
      copyPortlets.setFromRegistrationContext(fromRegistrationContext);
      copyPortlets.setToRegistrationContext(toRegistrationContext);

      CopyPortletsResponse response = producer.copyPortlets(copyPortlets);

      checkSimpleCopyPortletsResponse(response, createStringList(handle), createStringList());

      //Check that the proper registration context can access it
      String portletHandle = response.getCopiedPortlets().get(0).getNewPortletContext().getPortletHandle();
      checkDefaultMarkup(portletHandle, toRegistrationContext);

      //Check that the original registration cannot access it
      try
      {
         checkDefaultMarkup(portletHandle, fromRegistrationContext);
         fail("The original registration context should not be able to access this portlet");
      }
      catch (InvalidHandle e)
      {
         //expected
      }
      //Check that the null registration cannot access it
      try
      {
         checkDefaultMarkup(portletHandle, null);
         fail("The null registration context should not be able to access this portlet");
      }
      catch (InvalidHandle e)
      {
         //expected
      }
   }

   @Test
   public void testCopyPortletNullRegistrationWithRR() throws Exception
   {
      producer.getConfigurationService().getConfiguration().getRegistrationRequirements().setRegistrationRequired(true);

      List<PortletContext> portletContexts = createPortletContextList(getDefaultHandle());
      //creates a copyportlet with a null registration context
      CopyPortlets copyPortlets = createSimpleCopyPortlets(portletContexts);

      try
      {
         producer.copyPortlets(copyPortlets);
         fail("Should not be allowed to call copy portlets without a registration if a registration is requried");
      }
      catch (InvalidRegistration e)
      {
         //expected
      }
   }

   @Test
   public void testCopyPortletNonRegisteredToRegistration() throws Exception
   {
      RegistrationContext invalidRegistrationContext = WSRPTypeFactory.createRegistrationContext("non_registered_handle");

      List<PortletContext> portletContexts = createPortletContextList(getDefaultHandle());
      CopyPortlets copyPortlets = createSimpleCopyPortlets(portletContexts);
      copyPortlets.setToRegistrationContext(invalidRegistrationContext);

      try
      {
         producer.copyPortlets(copyPortlets);
         fail();
      }
      catch (InvalidRegistration e)
      {
         //expected
      }
   }

   @Test
   public void testCopyPortletNonRegisteredFromRegistration() throws Exception
   {
      RegistrationContext invalidRegistrationContext = WSRPTypeFactory.createRegistrationContext("non_registered_handle");

      List<PortletContext> portletContexts = createPortletContextList(getDefaultHandle());
      CopyPortlets copyPortlets = createSimpleCopyPortlets(portletContexts);
      copyPortlets.setFromRegistrationContext(invalidRegistrationContext);

      try
      {
         producer.copyPortlets(copyPortlets);
         fail();
      }
      catch (InvalidRegistration e)
      {
         //expected
      }
   }

   @Test
   public void testCopyPortletNullPortletContexts() throws Exception
   {
      List<PortletContext> portletContexts = null;
      CopyPortlets copyPortlets = new CopyPortlets();

      try
      {
         producer.copyPortlets(copyPortlets);
         fail();
      }
      catch (MissingParameters e)
      {
         //expected
      }
   }

   @Test
   public void testCopyPortletsEmptyPortletContexts() throws Exception
   {
      List<PortletContext> portletContexts = new ArrayList<PortletContext>();
      CopyPortlets copyPortlets = new CopyPortlets();
      copyPortlets.getFromPortletContexts().addAll(portletContexts);

      try
      {
         producer.copyPortlets(copyPortlets);
         fail();
      }
      catch (MissingParameters e)
      {
         //expected
      }
   }

   @Test
   public void testCopyPortletsInvalidPortletContexts() throws Exception
   {
      String fakePortletContext1 = "fakePortletContext1";
      String fakePortletContext2 = "fakePortletContext2";
      List<PortletContext> portletContexts = createPortletContextList(fakePortletContext1, fakePortletContext2);
      CopyPortlets copyPortlets = createSimpleCopyPortlets(portletContexts);

      CopyPortletsResponse response = producer.copyPortlets(copyPortlets);

      assertEquals(0, response.getCopiedPortlets().size());
      assertEquals(1, response.getFailedPortlets().size());
      final FailedPortlets failedPortlets = response.getFailedPortlets().get(0);
      assertTrue(failedPortlets.getPortletHandles().contains(fakePortletContext1));
      assertTrue(failedPortlets.getPortletHandles().contains(fakePortletContext2));
      assertTrue(failedPortlets.getErrorCode().getLocalPart().contains("InvalidHandle"));
   }

   @Test
   public void testCopyPortletsMixedPortletContexts() throws Exception
   {
      String fakePortletContext1 = "fakePortletContext1";
      String fakePortletContext2 = "fakePortletContext2";
      List<PortletContext> portletContexts = createPortletContextList(fakePortletContext1, getDefaultHandle(), fakePortletContext2);
      CopyPortlets copyPortlets = createSimpleCopyPortlets(portletContexts);

      CopyPortletsResponse response = producer.copyPortlets(copyPortlets);

      assertEquals(1, response.getFailedPortlets().size());
      FailedPortlets failedPortlets = response.getFailedPortlets().get(0);
      assertTrue(failedPortlets.getPortletHandles().contains(fakePortletContext1));
      assertTrue(failedPortlets.getPortletHandles().contains(fakePortletContext2));
      assertTrue(failedPortlets.getErrorCode().getLocalPart().contains("InvalidHandle"));

      assertEquals(1, response.getCopiedPortlets().size());
      assertEquals(getDefaultHandle(), response.getCopiedPortlets().get(0).getFromPortletHandle());
      assertFalse(response.getCopiedPortlets().get(0).getNewPortletContext().getPortletHandle().equals(getDefaultHandle()));
   }

   protected CopyPortlets createSimpleCopyPortlets(List<PortletContext> portletContexts)
   {
      RegistrationContext toRegistrationContext = null;
      UserContext toUserContext = null;
      RegistrationContext fromRegistrationContext = null;
      UserContext fromUserContext = null;
      return WSRPTypeFactory.createCopyPortlets(toRegistrationContext, toUserContext, fromRegistrationContext, fromUserContext, portletContexts);
   }

   protected void checkSimpleCopyPortletsResponse(CopyPortletsResponse response, List<String> success, List<String> failure)
   {
      //check that we are getting the expected number of copied portlets
      assertEquals(success.size(), response.getCopiedPortlets().size());
      for (CopiedPortlet copiedPortlet : response.getCopiedPortlets())
      {
         assertTrue(success.contains(copiedPortlet.getFromPortletHandle()));
      }

      //check that we are getting the expected number of failed portlets
      assertEquals(failure.size(), response.getFailedPortlets().size());
      for (FailedPortlets failedPortlet : response.getFailedPortlets())
      {
         assertTrue(failure.containsAll(failedPortlet.getPortletHandles()));
      }

      //Check that if we do get copiedPortlets back, that the new and old PortletHandle are not the same
      for (CopiedPortlet copiedPortlet : response.getCopiedPortlets())
      {
         assertFalse(copiedPortlet.getFromPortletHandle().equals(copiedPortlet.getNewPortletContext().getPortletHandle()));
      }

   }

   protected void checkDefaultMarkup(String portletHandle, RegistrationContext registrationContext) throws Exception
   {
      GetMarkup getMarkup = createDefaultGetMarkup(portletHandle);
      getMarkup.setRegistrationContext(registrationContext);
      MarkupResponse response = producer.getMarkup(getMarkup);

      String defaultMarkup = "<p>symbol unset stock value: value unset</p>";

      MarkupContext markupContext = response.getMarkupContext();
      assertNotNull(markupContext);
      assertEquals("text/html", markupContext.getMimeType());
      assertTrue(markupContext.getItemString().contains(defaultMarkup));
   }

   protected List<String> createStringList(String... names)
   {
      List<String> list = new ArrayList<String>(names.length);
      for (int i = 0; i < names.length; i++)
      {
         list.add(names[i]);
      }
      return list;
   }

   @Test
   public void testClonePortletAvailabilityRR() throws Exception
   {
      producer.getConfigurationService().getConfiguration().getRegistrationRequirements().setRegistrationRequired(true);
      checkClonePortletAvailability();
   }

   @Test
   public void testClonePortletAvailabilityNRR() throws Exception
   {
      producer.getConfigurationService().getConfiguration().getRegistrationRequirements().setRegistrationRequired(false);
      checkClonePortletAvailability();
   }

   public void checkClonePortletAvailability() throws Exception
   {
      RegistrationData registrationDataA = WSRPTypeFactory.createRegistrationData("CONSUMERA", "CONSUMERAGENTA.0.0", true);
      RegistrationContext registrationContextA = producer.register(WSRPTypeFactory.createRegister(registrationDataA, null, null));
      RegistrationData registrationDataB = WSRPTypeFactory.createRegistrationData("CONSUMERB", "CONSUMERAGENTB.0.0", true);
      RegistrationContext registrationContextB = producer.register(WSRPTypeFactory.createRegister(registrationDataB, null, null));

      checkClonePortletAvailability(registrationContextA, registrationContextB);
   }

   public void checkClonePortletAvailability(RegistrationContext registrationContextA, RegistrationContext registrationContextB) throws Exception
   {
      PortletContext portletContext = WSRPTypeFactory.createPortletContext(getDefaultHandle());
      ClonePortlet clonePortlet = WSRPTypeFactory.createClonePortlet(registrationContextA, portletContext, null);
      PortletContext clonedPortletContext = producer.clonePortlet(clonePortlet);

      assertFalse(portletContext.getPortletHandle().equals(clonedPortletContext.getPortletHandle()));

      GetMarkup getMarkupA = createDefaultGetMarkup(clonedPortletContext.getPortletHandle());
      getMarkupA.setRegistrationContext(registrationContextA);
      producer.getMarkup(getMarkupA);

      try
      {
         GetMarkup getMarkupB = createDefaultGetMarkup(clonedPortletContext.getPortletHandle());
         getMarkupB.setRegistrationContext(registrationContextB);
         producer.getMarkup(getMarkupB);
         fail("Should not be able to render a cloned portlet from a registration context which didn't create the clone.");
      }
      catch (InvalidHandle e)
      {
         //expected
      }
   }

   @Test
   public void testClonePortletAvailabilityNonRegisteredA() throws Exception
   {
      producer.getConfigurationService().getConfiguration().getRegistrationRequirements().setRegistrationRequired(false);

      RegistrationData registrationDataA = WSRPTypeFactory.createRegistrationData("CONSUMERA", "CONSUMERAGENTA.0.0", true);
      RegistrationContext registrationContextA = producer.register(WSRPTypeFactory.createRegister(registrationDataA, null, null));

      checkClonePortletAvailability(registrationContextA, null);
   }

   @Test
   public void testClonePortletAvailabilityNonRegisteredB() throws Exception
   {
      producer.getConfigurationService().getConfiguration().getRegistrationRequirements().setRegistrationRequired(false);

      RegistrationData registrationDataB = WSRPTypeFactory.createRegistrationData("CONSUMERB", "CONSUMERAGENTB.0.0", true);
      RegistrationContext registrationContextB = producer.register(WSRPTypeFactory.createRegister(registrationDataB, null, null));

      checkClonePortletAvailability(null, registrationContextB);
   }

   @Test
   public void testClonePortletAvailabilityNullRegistered() throws Exception
   {
      RegistrationContext registrationContextA = null;
      RegistrationContext registrationContextB = null;
      String handle = getDefaultHandle();
      PortletContext portletContext = WSRPTypeFactory.createPortletContext(getDefaultHandle());
      ClonePortlet clonePortlet = WSRPTypeFactory.createClonePortlet(registrationContextA, portletContext, null);
      PortletContext clonedPortletContext = producer.clonePortlet(clonePortlet);

      assertFalse(portletContext.getPortletHandle().equals(clonedPortletContext.getPortletHandle()));

      GetMarkup getMarkupA = createDefaultGetMarkup(clonedPortletContext.getPortletHandle());
      getMarkupA.setRegistrationContext(registrationContextA);
      producer.getMarkup(getMarkupA);

      //Since both A and B are null, this should work
      GetMarkup getMarkupB = createDefaultGetMarkup(clonedPortletContext.getPortletHandle());
      getMarkupB.setRegistrationContext(registrationContextB);
      producer.getMarkup(getMarkupB);
   }

   protected String getMostUsedPortletWARFileName()
   {
      return TEST_BASIC_PORTLET_WAR;
   }

}

