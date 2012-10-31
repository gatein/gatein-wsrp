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

package org.gatein.wsrp.protocol.v1;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.RequestFacade;
import org.gatein.registration.Registration;
import org.gatein.registration.RegistrationManager;
import org.gatein.wsrp.WSRPConstants;
import org.gatein.wsrp.WSRPUtils;
import org.gatein.wsrp.api.servlet.ServletAccess;
import org.gatein.wsrp.portlet.utils.MockRequest;
import org.gatein.wsrp.registration.RegistrationPropertyDescription;
import org.gatein.wsrp.spec.v1.V2ToV1Converter;
import org.gatein.wsrp.spec.v1.WSRP1TypeFactory;
import org.gatein.wsrp.test.ExtendedAssert;
import org.gatein.wsrp.test.support.MockHttpServletResponse;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OverProtocol;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oasis.wsrp.v1.V1GetMarkup;
import org.oasis.wsrp.v1.V1GetServiceDescription;
import org.oasis.wsrp.v1.V1InvalidRegistration;
import org.oasis.wsrp.v1.V1ModifyRegistration;
import org.oasis.wsrp.v1.V1OperationFailed;
import org.oasis.wsrp.v1.V1PropertyDescription;
import org.oasis.wsrp.v1.V1RegistrationContext;
import org.oasis.wsrp.v1.V1RegistrationData;
import org.oasis.wsrp.v2.PropertyDescription;

import javax.xml.namespace.QName;
import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 12309 $
 * @since 2.4
 */
@RunWith(Arquillian.class)
public class RegistrationTestCase extends V1ProducerBaseTest
{
   public RegistrationTestCase() throws Exception
   {
      super("RegistrationTestCase");
   }

   @Deployment
   @OverProtocol("Servlet 2.5")
   public static Archive createDeployment()
   {
      Archive archive = V1ProducerBaseTest.createDeployment();
      return archive;
   }

   @Override
   protected boolean removeCurrent(String archiveName)
   {
      return true;
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

   /**
    * R355: The portal MUST pass a name for itself that uniquely identifies it.
    *
    * @throws Exception
    */
   @Test
   public void testUniqueNameRegistration() throws Exception
   {
      // not sure how to test this...
   }

   /**
    * R356: The portal MAY pass information describing the portal [vendor] type and version.
    * <p/>
    * However, WSRP v1 7.1.1 states: The consumerAgent value MUST start with "productName.majorVersion.minorVersion"
    * where "productName" identifies the product the Consumer installed for its deployment, and majorVersion and
    * minorVersion are vendor-defined indications of the version of its product. This string can then contain any
    * additional characters/words the product or Consumer wish to supply.
    *
    * @throws Exception
    */
   @Test
   public void testConsumerAgent() throws Exception
   {
      configureRegistrationSettings(true, false);
      V1RegistrationData regData = createBaseRegData();
      regData.setConsumerAgent("invalid consumer agent");

      try
      {
         producer.register(regData);
         ExtendedAssert.fail("Trying to register with an invalid consumer agent String should fail.");
      }
      catch (V1OperationFailed operationFailedFault)
      {
         // expected
      }


      regData.setConsumerAgent(WSRPConstants.CONSUMER_AGENT);
      producer.register(regData);
   }

   //@Test
   public void testRegistrationHandle() throws Exception
   {
      try
      {
         // check that a registration handle was created
         V1RegistrationContext rc = registerConsumer();
         String registrationHandle = rc.getRegistrationHandle();
         assertNotNull(registrationHandle);

         // check that a registration was created with that handle
         RegistrationManager registrationManager = producer.getRegistrationManager();
         Registration registration = registrationManager.getRegistration(registrationHandle);
         assertNotNull(registration);

         // check that the registration was persisted...
         String key = registration.getPersistentKey();
         assertNotNull(key);

         // ... and that the handle was created by the policy based on the registration key
         String expectedHandle = registrationManager.getPolicy().createRegistrationHandleFor(key);
         assertEquals(expectedHandle, registrationHandle);
      }
      catch (Exception e)
      {
         //print error to the server logs since these errors can't be serialised back through arquillian.
         e.printStackTrace();
         throw new Exception("An error occured, please see the server logs for details (arquillian can't serialize this exception for output).");
      }
   }

   @Test
   public void testDeregister() throws Exception
   {
      // initiate registration
      configureRegistrationSettings(true, false);
      V1RegistrationContext rc = registerConsumer();

      // deregister
      producer.deregister(rc);

      // try to get markup, portlet handle doesn't matter since it should fail before trying to retrieve the portlet
      V1GetMarkup getMarkup = createDefaultMarkupRequest("foo");
      getMarkup.getMarkupParams().getMarkupCharacterSets().add(WSRPConstants.DEFAULT_CHARACTER_SET);

      try
      {
         producer.getMarkup(getMarkup);
         ExtendedAssert.fail("Consumer tried to access info with a de-registered context. Operations should fail.");
      }
      catch (V1InvalidRegistration invalidRegistrationFault)
      {
         // expected
      }

      // Get description with old registration context should fail
      V1GetServiceDescription gs = getNoRegistrationServiceDescriptionRequest();
      gs.setRegistrationContext(rc);

      try
      {
         producer.getServiceDescription(gs);
         ExtendedAssert.fail("Required registration info has been modified: operations should fail until registration is modified.");
      }
      catch (V1InvalidRegistration invalidRegistrationFault)
      {
         // expected
      }

      // Get description should still work without registration context
      gs = getNoRegistrationServiceDescriptionRequest();

      ExtendedAssert.assertNotNull(producer.getServiceDescription(gs));
   }

   @Test
   public void testModifyRegistration() throws Exception
   {
      // initiate registration
      configureRegistrationSettings(true, false);
      V1RegistrationContext rc = registerConsumer();

      // now modify Producer's set of required registration info
      String newPropName = "New Prop";
      RegistrationPropertyDescription regProp = new RegistrationPropertyDescription(newPropName,
         new QName("urn:oasis:names:tc:wsrp:v1:types", "LocalizedString", "ns1"));
      regProp.setDefaultLabel("New Registration Property");
      producer.getConfigurationService().getConfiguration().getRegistrationRequirements().addRegistrationProperty(regProp);

      // try to get markup, portlet handle doesn't matter since it should fail before trying to retrieve the portlet
      V1GetMarkup getMarkup = createDefaultMarkupRequest("foo");
      getMarkup.getMarkupParams().getMarkupCharacterSets().add(WSRPConstants.DEFAULT_CHARACTER_SET);
      getMarkup.setRegistrationContext(rc);

      try
      {
         producer.getMarkup(getMarkup);
         ExtendedAssert.fail("Required registration info has been modified: operations should fail until registration is modified.");
      }
      catch (V1OperationFailed operationFailedFault)
      {
         // expected
         // WSRP primer recommends returning OperationFailedFault and NOT InvalidRegistrationFault
         // kinda weird... will be replaced by ModifyRegistrationRequiredFault in WSRP 2.0
      }

      // Get description should return information just as if consumer wasn't registered
      V1GetServiceDescription gs = getNoRegistrationServiceDescriptionRequest();
      gs.setRegistrationContext(rc);

      try
      {
         producer.getServiceDescription(gs);
         ExtendedAssert.fail("Required registration info has been modified: operations should fail until registration is modified.");
      }
      catch (V1OperationFailed operationFailedFault)
      {
         // expected
         // WSRP primer recommends returning OperationFailedFault and NOT InvalidRegistrationFault
         // kinda weird... will be replaced by ModifyRegistrationRequiredFault in WSRP 2.0
      }

      // remove registration context, try again and check that we get new registration info
      gs.setRegistrationContext(null);
      List<V1PropertyDescription> pds = producer.getServiceDescription(gs)
         .getRegistrationPropertyDescription().getPropertyDescriptions();
      ExtendedAssert.assertEquals(2, pds.size());

      // Check that one of the returned property description is equal to the one we just added
      V1PropertyDescription description = pds.get(1);
      if (description.getName().startsWith("New"))
      {
         assertEquals(WSRPUtils.convertToPropertyDescription(regProp), description);
      }
      else
      {
         PropertyDescription propertyDescription = WSRPUtils.convertToPropertyDescription(regProp);
         V1PropertyDescription v1PropertyDescription = V2ToV1Converter.toV1PropertyDescription(propertyDescription);
         assertEquals(v1PropertyDescription, pds.get(0));
      }

      // Update registration data
      V1RegistrationData regData = createBaseRegData();
      regData.getRegistrationProperties().add(WSRP1TypeFactory.createProperty(newPropName, "en", "blah"));

      // Modify registration and get service description
      V1ModifyRegistration registration = new V1ModifyRegistration();
      registration.setRegistrationContext(rc);
      registration.setRegistrationData(regData);
      producer.modifyRegistration(registration);
      gs.setRegistrationContext(rc);
      checkServiceDescriptionWithOnlyBasicPortlet(gs);
   }

   @Test
   public void testModifyRegistrationIncorrectData() throws Exception
   {
      // initiate registration
      configureRegistrationSettings(true, false);
      V1RegistrationContext rc = registerConsumer();

      // now modify Producer's set of required registration info
      String newPropName = "New Prop";
      RegistrationPropertyDescription regProp = new RegistrationPropertyDescription(newPropName,
         new QName("urn:oasis:names:tc:wsrp:v1:types", "LocalizedString", "ns1"));
      regProp.setDefaultLabel("New Registration Property");
      producer.getConfigurationService().getConfiguration().getRegistrationRequirements().addRegistrationProperty(regProp);

      try
      {
         V1ModifyRegistration registration = new V1ModifyRegistration();
         registration.setRegistrationContext(rc);
         registration.setRegistrationData(createBaseRegData());
         producer.modifyRegistration(registration);
         ExtendedAssert.fail("Passing incorrect data should fail");
      }
      catch (V1OperationFailed operationFailed)
      {
         // expected
      }
   }

   @Test
   public void testRegister() throws Exception
   {
      configureRegistrationSettings(true, false);

      // service description request without registration info
      V1GetServiceDescription gs = getNoRegistrationServiceDescriptionRequest();

      V1RegistrationContext registrationContext = registerConsumer();
      ExtendedAssert.assertNotNull(registrationContext);
      ExtendedAssert.assertNotNull(registrationContext.getRegistrationHandle());

      gs.setRegistrationContext(registrationContext);

      checkServiceDescriptionWithOnlyBasicPortlet(gs);
   }

   @Test
   public void testRegisterWhenRegistrationNotRequired() throws Exception
   {
      configureRegistrationSettings(false, false);

      try
      {
         registerConsumer();
         ExtendedAssert.fail("Shouldn't be possible to register if no registration is required.");
      }
      catch (V1OperationFailed operationFailedFault)
      {
         // expected
      }
   }

   @Test
   public void testDeregisterWhenRegistrationNotRequired() throws Exception
   {
      configureRegistrationSettings(false, false);

      try
      {
         producer.deregister(null);
         ExtendedAssert.fail("Shouldn't be possible to deregister if no registration is required.");
      }
      catch (V1OperationFailed operationFailedFault)
      {
         // expected
      }
   }

   @Test
   public void testModifyRegistrationWhenRegistrationNotRequired() throws Exception
   {
      configureRegistrationSettings(false, false);

      try
      {
         producer.modifyRegistration(null);
         ExtendedAssert.fail("Shouldn't be possible to modify registration if no registration is required.");
      }
      catch (V1OperationFailed operationFailedFault)
      {
         // expected
      }
   }

   @Test
   public void testModifyRegistrationNoRegistrationWhenRegistrationRequired() throws Exception
   {
      configureRegistrationSettings(true, false);

      try
      {
         producer.modifyRegistration(null);
         ExtendedAssert.fail("Shouldn't be possible to modify registration if no registration is required.");
      }
      catch (V1OperationFailed operationFailedFault)
      {
         // expected
      }
      catch (Exception e)
      {
         ExtendedAssert.fail(e.getMessage());
      }
   }

   @Test
   public void testDeregisterNoRegistrationWhenRegistrationRequired() throws Exception
   {
      configureRegistrationSettings(true, false);

      try
      {
         producer.deregister(null);
         ExtendedAssert.fail("Shouldn't be possible to deregister if no registration is required.");
      }
      catch (V1OperationFailed operationFailedFault)
      {
         // expected
      }
      catch (Exception e)
      {
         ExtendedAssert.fail(e.getMessage());
      }
   }
}
