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

import org.gatein.registration.RegistrationException;
import org.gatein.registration.RegistrationManager;
import org.gatein.registration.policies.DefaultRegistrationPolicy;
import org.gatein.registration.policies.DefaultRegistrationPropertyValidator;
import org.gatein.wsrp.WSRPConstants;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.producer.WSRPProducerBaseTest;
import org.gatein.wsrp.producer.config.ProducerRegistrationRequirements;
import org.gatein.wsrp.registration.RegistrationPropertyDescription;
import org.gatein.wsrp.test.ExtendedAssert;
import org.oasis.wsrp.v1.GetServiceDescription;
import org.oasis.wsrp.v1.LocalizedString;
import org.oasis.wsrp.v1.MarkupType;
import org.oasis.wsrp.v1.MissingParameters;
import org.oasis.wsrp.v1.OperationFailed;
import org.oasis.wsrp.v1.PortletDescription;
import org.oasis.wsrp.v1.PropertyDescription;
import org.oasis.wsrp.v1.RegistrationContext;
import org.oasis.wsrp.v1.RegistrationData;
import org.oasis.wsrp.v1.ServiceDescription;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 12302 $
 * @since 2.4
 */
public abstract class V1ProducerBaseTest extends WSRPProducerBaseTest
{
   private static final String CONSUMER = "test-consumer";

   public V1ProducerBaseTest() throws Exception
   {
      this("V1ProducerBaseTest");
   }

   protected V1ProducerBaseTest(String name) throws Exception
   {
      super(name);
   }


   public void setUp() throws Exception
   {
      super.setUp();

      resetRegistrationInfo();
   }

   public void tearDown() throws Exception
   {
      resetRegistrationInfo();
      super.tearDown();
   }

   /**
    * Checks that the specified portlet description corresponds to the expected description of test-basic-portlet. If
    * the handle parameter is not null, checks that it corresponds to the specified portlet decription.
    *
    * @param desc   the tested PortletDescription
    * @param handle the PortletHandle to be checked
    */
   public void checkBasicPortletDescription(PortletDescription desc, String handle)
   {
      ExtendedAssert.assertNotNull(desc);
      /**
       * @wsrp.specification
       *    Portlet handle must be less than 255 characters (WSRP 1.0 5.1.2)
       */
      ExtendedAssert.assertTrue(desc.getPortletHandle().length() <= 255);
      if (handle != null)
      {
         ExtendedAssert.assertEquals(handle, desc.getPortletHandle());
      }
      ExtendedAssert.assertEquals("title", desc.getTitle().getValue());
      List<MarkupType> markupTypes = desc.getMarkupTypes();
      ExtendedAssert.assertEquals(1, markupTypes.size());
      MarkupType markupType = markupTypes.get(0);

      List<String> states = new ArrayList<String>(3);
      states.add(WSRPConstants.NORMAL_WINDOW_STATE);
      states.add(WSRPConstants.MAXIMIZED_WINDOW_STATE);
      states.add(WSRPConstants.MINIMIZED_WINDOW_STATE);
      MarkupType expected = WSRPTypeFactory.createMarkupType("text/html",
         Collections.<String>singletonList(WSRPConstants.VIEW_MODE), states, Collections.<String>singletonList("en"));
      assertEquals(expected, markupType);
   }

   protected ServiceDescription checkServiceDescriptionWithOnlyBasicPortlet(GetServiceDescription gs)
      throws Exception
   {
      deploy("test-basic-portlet.war");
      //Invoke the Web Service
      ServiceDescription sd = producer.getServiceDescription(gs);

      ExtendedAssert.assertNotNull("sd != null", sd);

      // Check offered portlets
      List<PortletDescription> offeredPortlets = sd.getOfferedPortlets();
      ExtendedAssert.assertNotNull(offeredPortlets);
      for (PortletDescription offeredPortlet : offeredPortlets)
      {
         System.out.println("handle " + offeredPortlet.getPortletHandle());
      }
      ExtendedAssert.assertEquals(1, offeredPortlets.size());

      // Check portlet description
      PortletDescription desc = offeredPortlets.get(0);

      checkBasicPortletDescription(desc, null);

      undeploy("test-basic-portlet.war");
      return sd; // for further testing...
   }

   protected RegistrationContext registerConsumer() throws OperationFailed, MissingParameters
   {
      RegistrationData registrationData = createBaseRegData();
      return producer.register(registrationData);
   }

   protected RegistrationData createBaseRegData()
   {
      RegistrationData regData = WSRPTypeFactory.createDefaultRegistrationData();
      regData.setConsumerName(CONSUMER);
      regData.getRegistrationProperties().add(WSRPTypeFactory.createProperty("regProp", "en", "regValue"));
      return regData;
   }

   protected RegistrationPropertyDescription configureRegistrationSettings(boolean requiresRegistration, boolean provideUnregisteredFullDescription)
   {
      // define expected registration infos
      ProducerRegistrationRequirements registrationRequirements = producer.getConfigurationService().getConfiguration().getRegistrationRequirements();
      registrationRequirements.setRegistrationRequired(requiresRegistration);
      registrationRequirements.setRegistrationRequiredForFullDescription(!provideUnregisteredFullDescription);

      if (requiresRegistration)
      {
         // fix-me: http://jira.jboss.com/jira/browse/JBPORTAL-821
         RegistrationPropertyDescription regProp = new RegistrationPropertyDescription("regProp",
            new QName("urn:oasis:names:tc:wsrp:v1:types", "LocalizedString", "ns1"));
         regProp.setDefaultLabel("Registration Property");
         registrationRequirements.addRegistrationProperty(regProp);

         // Use default registration policy: this wiring is normally handled at the producer start, should maybe use a
         // registration policy that is automatically configured when none is provided to avoid having a null one?
         DefaultRegistrationPolicy defaultRegistrationPolicy = new DefaultRegistrationPolicy();
         defaultRegistrationPolicy.setValidator(new DefaultRegistrationPropertyValidator());
         registrationRequirements.setPolicy(defaultRegistrationPolicy);

         RegistrationManager registrationManager = producer.getRegistrationManager();
         registrationManager.setPolicy(defaultRegistrationPolicy);
         registrationRequirements.addRegistrationPropertyChangeListener(registrationManager);

         // create consumer for policy to be able to make decisions properly
         try
         {
            registrationManager.createConsumer(CONSUMER);
         }
         catch (RegistrationException e)
         {
            ExtendedAssert.fail("Couldn't create consumer. Cause: " + e.getLocalizedMessage());
         }

         return regProp;
      }
      else
      {
         return null;
      }
   }

   protected void resetRegistrationInfo() throws RegistrationException
   {
      ProducerRegistrationRequirements registrationRequirements = producer.getConfigurationService().getConfiguration().getRegistrationRequirements();
      registrationRequirements.setRegistrationRequired(false);
      registrationRequirements.clearRegistrationProperties();
      registrationRequirements.clearRegistrationPropertyChangeListeners();
      producer.getRegistrationManager().clear();
      registrationRequirements.removeRegistrationPropertyChangeListener(producer.getRegistrationManager());
   }

   protected GetServiceDescription getNoRegistrationServiceDescriptionRequest()
   {
      GetServiceDescription gs = WSRPTypeFactory.createGetServiceDescription();
      gs.getDesiredLocales().add("en-US");
      gs.getDesiredLocales().add("en");
      return gs;
   }

   /** === asserts === * */

   protected static void assertEquals(MarkupType expected, MarkupType tested)
   {
      String message = "Expected: <" + expected + ">, got: <" + tested + ">. Failed on: ";

      if (expected != tested)
      {
         if (expected == null || tested == null)
         {
            ExtendedAssert.fail(message + "Different classes or not both null.");
         }


         assertEquals(message + "Extensions", expected.getExtensions(), tested.getExtensions());
         assertEquals(message + "Locales", expected.getLocales(), tested.getLocales());
         assertEquals(message + "Modes", expected.getModes(), tested.getModes());
         assertEquals(message + "Window states", expected.getWindowStates(), tested.getWindowStates());
      }
   }

   protected static void assertEquals(PropertyDescription expected, PropertyDescription tested)
   {
      String message = "Expected: <" + expected + ">, got: <" + tested + ">. Failed on ";

      if (expected != tested)
      {
         if (expected == null || tested == null)
         {
            ExtendedAssert.fail(message + "Different classes or not both null.");
         }

         assertEquals(message + "extensions.", expected.getExtensions(), tested.getExtensions());
         assertEquals(message + "hint.", expected.getHint(), tested.getHint());
         assertEquals(message + "label.", expected.getLabel(), tested.getLabel());
         ExtendedAssert.assertEquals(message + "name.", expected.getName(), tested.getName());
         ExtendedAssert.assertEquals(message + "type.", expected.getType(), tested.getType());
      }
   }

   protected static void assertEquals(String message, LocalizedString expected, LocalizedString tested)
   {
      String precise = "Expected: <" + expected + ">, got: <" + tested + ">. Failed on ";

      if (expected != tested)
      {
         if (expected == null || tested == null)
         {
            ExtendedAssert.fail(message + ": Different classes or not both null.");
         }

         ExtendedAssert.assertEquals(precise + "lang.", expected.getLang(), tested.getLang());
         ExtendedAssert.assertEquals(precise + "resource name.", expected.getResourceName(), tested.getResourceName());
         ExtendedAssert.assertEquals(precise + "value.", expected.getValue(), tested.getValue());
      }
   }

   protected void checkException(Exception e, String errorCode)
   {
      Throwable cause = e.getCause();
//      ExtendedAssert.assertTrue(cause instanceof SOAPFaultException);
//      ExtendedAssert.assertEquals(errorCode, ((SOAPFaultException)cause).getFault().getLocalPart());
   }
}
