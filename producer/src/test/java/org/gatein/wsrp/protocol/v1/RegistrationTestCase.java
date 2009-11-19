/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2006, Red Hat Middleware, LLC, and individual                    *
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

package org.gatein.wsrp.protocol.v1;

import org.gatein.wsrp.WSRPConstants;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.WSRPUtils;
import org.gatein.wsrp.registration.RegistrationPropertyDescription;
import org.gatein.wsrp.test.ExtendedAssert;
import org.oasis.wsrp.v1.GetMarkup;
import org.oasis.wsrp.v1.GetServiceDescription;
import org.oasis.wsrp.v1.InvalidRegistration;
import org.oasis.wsrp.v1.ModifyRegistration;
import org.oasis.wsrp.v1.OperationFailed;
import org.oasis.wsrp.v1.PropertyDescription;
import org.oasis.wsrp.v1.RegistrationContext;
import org.oasis.wsrp.v1.RegistrationData;

import javax.xml.namespace.QName;
import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 12309 $
 * @since 2.4
 */
public class RegistrationTestCase extends V1ProducerBaseTest
{
   public RegistrationTestCase() throws Exception
   {
      super("RegistrationTestCase");
   }

   /**
    * R355: The portal MUST pass a name for itself that uniquely identifies it.
    *
    * @throws Exception
    */
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
   public void testConsumerAgent() throws Exception
   {
      configureRegistrationSettings(true, false);
      RegistrationData regData = createBaseRegData();
      regData.setConsumerAgent("invalid consumer agent");

      try
      {
         registrationService.register(regData);
         ExtendedAssert.fail("Trying to register with an invalid consumer agent String should fail.");
      }
      catch (OperationFailed operationFailedFault)
      {
         // expected
      }


      regData.setConsumerAgent(WSRPConstants.CONSUMER_AGENT);
      registrationService.register(regData);
   }

   public void testDeregister() throws Exception
   {
      // initiate registration
      configureRegistrationSettings(true, false);
      RegistrationContext rc = registerConsumer();

      // deregister
      registrationService.deregister(rc);

      // try to get markup, portlet handle doesn't matter since it should fail before trying to retrieve the portlet
      GetMarkup getMarkup = WSRPTypeFactory.createDefaultMarkupRequest("foo");
      getMarkup.getMarkupParams().getMarkupCharacterSets().add(WSRPConstants.DEFAULT_CHARACTER_SET);

      try
      {
         markupService.getMarkup(getMarkup);
         ExtendedAssert.fail("Consumer tried to access info with a de-registered context. Operations should fail.");
      }
      catch (InvalidRegistration invalidRegistrationFault)
      {
         // expected
      }

      // Get description with old registration context should fail
      GetServiceDescription gs = getNoRegistrationServiceDescriptionRequest();
      gs.setRegistrationContext(rc);

      try
      {
         serviceDescriptionService.getServiceDescription(gs);
         ExtendedAssert.fail("Required registration info has been modified: operations should fail until registration is modified.");
      }
      catch (InvalidRegistration invalidRegistrationFault)
      {
         // expected
      }

      // Get description should still work without registration context
      gs = getNoRegistrationServiceDescriptionRequest();

      ExtendedAssert.assertNotNull(serviceDescriptionService.getServiceDescription(gs));
   }

   public void testModifyRegistration() throws Exception
   {
      // initiate registration
      configureRegistrationSettings(true, false);
      RegistrationContext rc = registerConsumer();

      // now modify Producer's set of required registration info
      String newPropName = "New Prop";
      RegistrationPropertyDescription regProp = new RegistrationPropertyDescription(newPropName,
         new QName("urn:oasis:names:tc:wsrp:v1:types", "LocalizedString", "ns1"));
      regProp.setDefaultLabel("New Registration Property");
      producer.getConfigurationService().getConfiguration().getRegistrationRequirements().addRegistrationProperty(regProp);

      // try to get markup, portlet handle doesn't matter since it should fail before trying to retrieve the portlet
      GetMarkup getMarkup = WSRPTypeFactory.createDefaultMarkupRequest("foo");
      getMarkup.getMarkupParams().getMarkupCharacterSets().add(WSRPConstants.DEFAULT_CHARACTER_SET);
      getMarkup.setRegistrationContext(rc);

      try
      {
         markupService.getMarkup(getMarkup);
         ExtendedAssert.fail("Required registration info has been modified: operations should fail until registration is modified.");
      }
      catch (OperationFailed operationFailedFault)
      {
         // expected
         // WSRP primer recommends returning OperationFailedFault and NOT InvalidRegistrationFault
         // kinda weird... will be replaced by ModifyRegistrationRequiredFault in WSRP 2.0
      }

      // Get description should return information just as if consumer wasn't registered
      GetServiceDescription gs = getNoRegistrationServiceDescriptionRequest();
      gs.setRegistrationContext(rc);

      try
      {
         serviceDescriptionService.getServiceDescription(gs);
         ExtendedAssert.fail("Required registration info has been modified: operations should fail until registration is modified.");
      }
      catch (OperationFailed operationFailedFault)
      {
         // expected
         // WSRP primer recommends returning OperationFailedFault and NOT InvalidRegistrationFault
         // kinda weird... will be replaced by ModifyRegistrationRequiredFault in WSRP 2.0
      }

      // remove registration context, try again and check that we get new registration info
      gs.setRegistrationContext(null);
      List<PropertyDescription> pds = serviceDescriptionService.getServiceDescription(gs)
         .getRegistrationPropertyDescription().getPropertyDescriptions();
      ExtendedAssert.assertEquals(2, pds.size());

      // Check that one of the returned property description is equal to the one we just added
      PropertyDescription description = pds.get(1);
      if (description.getName().startsWith("New"))
      {
         assertEquals(WSRPUtils.convertToPropertyDescription(regProp), description);
      }
      else
      {
         assertEquals(WSRPUtils.convertToPropertyDescription(regProp), pds.get(0));
      }

      // Update registration data
      RegistrationData regData = createBaseRegData();
      regData.getRegistrationProperties().add(WSRPTypeFactory.createProperty(newPropName, "en", "blah"));

      // Modify registration and get service description
      ModifyRegistration registration = new ModifyRegistration();
      registration.setRegistrationContext(rc);
      registration.setRegistrationData(regData);
      registrationService.modifyRegistration(registration);
      gs.setRegistrationContext(rc);
      checkServiceDescriptionWithOnlyBasicPortlet(gs);
   }

   public void testModifyRegistrationIncorrectData() throws Exception
   {
      // initiate registration
      configureRegistrationSettings(true, false);
      RegistrationContext rc = registerConsumer();

      // now modify Producer's set of required registration info
      String newPropName = "New Prop";
      RegistrationPropertyDescription regProp = new RegistrationPropertyDescription(newPropName,
         new QName("urn:oasis:names:tc:wsrp:v1:types", "LocalizedString", "ns1"));
      regProp.setDefaultLabel("New Registration Property");
      producer.getConfigurationService().getConfiguration().getRegistrationRequirements().addRegistrationProperty(regProp);

      try
      {
         ModifyRegistration registration = new ModifyRegistration();
         registration.setRegistrationContext(rc);
         registration.setRegistrationData(createBaseRegData());
         registrationService.modifyRegistration(registration);
         ExtendedAssert.fail("Passing incorrect data should fail");
      }
      catch (OperationFailed operationFailed)
      {
         // expected
      }
   }

   public void testRegister() throws Exception
   {
      configureRegistrationSettings(true, false);

      // service description request without registration info
      GetServiceDescription gs = getNoRegistrationServiceDescriptionRequest();

      RegistrationContext registrationContext = registerConsumer();
      ExtendedAssert.assertNotNull(registrationContext);
      ExtendedAssert.assertNotNull(registrationContext.getRegistrationHandle());

      gs.setRegistrationContext(registrationContext);

      checkServiceDescriptionWithOnlyBasicPortlet(gs);
   }

   public void testRegisterWhenRegistrationNotRequired() throws Exception
   {
      configureRegistrationSettings(false, false);

      try
      {
         registerConsumer();
         ExtendedAssert.fail("Shouldn't be possible to register if no registration is required.");
      }
      catch (OperationFailed operationFailedFault)
      {
         // expected
      }
   }

   public void testDeregisterWhenRegistrationNotRequired() throws Exception
   {
      configureRegistrationSettings(false, false);

      try
      {
         registrationService.deregister(null);
         ExtendedAssert.fail("Shouldn't be possible to deregister if no registration is required.");
      }
      catch (OperationFailed operationFailedFault)
      {
         // expected
      }
   }

   public void testModifyRegistrationWhenRegistrationNotRequired() throws Exception
   {
      configureRegistrationSettings(false, false);

      try
      {
         registrationService.modifyRegistration(null);
         ExtendedAssert.fail("Shouldn't be possible to modify registration if no registration is required.");
      }
      catch (OperationFailed operationFailedFault)
      {
         // expected
      }
   }

   public void testModifyRegistrationNoRegistrationWhenRegistrationRequired() throws Exception
   {
      configureRegistrationSettings(true, false);

      try
      {
         registrationService.modifyRegistration(null);
         ExtendedAssert.fail("Shouldn't be possible to modify registration if no registration is required.");
      }
      catch (OperationFailed operationFailedFault)
      {
         // expected
      }
      catch (Exception e)
      {
         ExtendedAssert.fail(e.getMessage());
      }
   }

   public void testDeregisterNoRegistrationWhenRegistrationRequired() throws Exception
   {
      configureRegistrationSettings(true, false);

      try
      {
         registrationService.deregister(null);
         ExtendedAssert.fail("Shouldn't be possible to modify registration if no registration is required.");
      }
      catch (OperationFailed operationFailedFault)
      {
         // expected
      }
      catch (Exception e)
      {
         ExtendedAssert.fail(e.getMessage());
      }
   }
}
