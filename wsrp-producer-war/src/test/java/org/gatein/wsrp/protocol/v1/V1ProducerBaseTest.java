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

import org.gatein.registration.RegistrationException;
import org.gatein.registration.RegistrationManager;
import org.gatein.registration.policies.DefaultRegistrationPolicy;
import org.gatein.registration.policies.DefaultRegistrationPropertyValidator;
import org.gatein.wsrp.WSRPConstants;
import org.gatein.wsrp.producer.ProducerHolder;
import org.gatein.wsrp.producer.WSRPProducer;
import org.gatein.wsrp.producer.WSRPProducerBaseTest;
import org.gatein.wsrp.producer.config.ProducerRegistrationRequirements;
import org.gatein.wsrp.producer.v1.WSRP1Producer;
import org.gatein.wsrp.registration.RegistrationPropertyDescription;
import org.gatein.wsrp.spec.v1.WSRP1TypeFactory;
import org.gatein.wsrp.test.ExtendedAssert;
import org.oasis.wsrp.v1.V1GetMarkup;
import org.oasis.wsrp.v1.V1GetServiceDescription;
import org.oasis.wsrp.v1.V1LocalizedString;
import org.oasis.wsrp.v1.V1MarkupType;
import org.oasis.wsrp.v1.V1MissingParameters;
import org.oasis.wsrp.v1.V1OperationFailed;
import org.oasis.wsrp.v1.V1PerformBlockingInteraction;
import org.oasis.wsrp.v1.V1PortletContext;
import org.oasis.wsrp.v1.V1PortletDescription;
import org.oasis.wsrp.v1.V1PropertyDescription;
import org.oasis.wsrp.v1.V1RegistrationContext;
import org.oasis.wsrp.v1.V1RegistrationData;
import org.oasis.wsrp.v1.V1RuntimeContext;
import org.oasis.wsrp.v1.V1ServiceDescription;

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
   protected WSRP1Producer producer = ProducerHolder.getV1Producer();


   public V1ProducerBaseTest() throws Exception
   {
      this("V1ProducerBaseTest");
   }

   protected V1ProducerBaseTest(String name) throws Exception
   {
      super(name);
   }

   @Override
   protected WSRPProducer getProducer()
   {
      return producer;
   }

   /**
    * Checks that the specified portlet description corresponds to the expected description of test-basic-portlet. If
    * the handle parameter is not null, checks that it corresponds to the specified portlet decription.
    *
    * @param desc   the tested PortletDescription
    * @param handle the PortletHandle to be checked
    */
   public void checkBasicPortletDescription(V1PortletDescription desc, String handle)
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
      List<V1MarkupType> markupTypes = desc.getMarkupTypes();
      ExtendedAssert.assertEquals(1, markupTypes.size());
      V1MarkupType markupType = markupTypes.get(0);

      List<String> states = new ArrayList<String>(3);
      states.add(WSRPConstants.NORMAL_WINDOW_STATE);
      states.add(WSRPConstants.MAXIMIZED_WINDOW_STATE);
      states.add(WSRPConstants.MINIMIZED_WINDOW_STATE);
      V1MarkupType expected = WSRP1TypeFactory.createMarkupType("text/html",
         Collections.<String>singletonList(WSRPConstants.VIEW_MODE), states, Collections.<String>singletonList("en"));
      assertEquals(expected, markupType);
   }

   protected V1ServiceDescription checkServiceDescriptionWithOnlyBasicPortlet(V1GetServiceDescription gs)
      throws Exception
   {
      deploy("test-basic-portlet.war");
      //Invoke the Web Service
      V1ServiceDescription sd = producer.getServiceDescription(gs);

      ExtendedAssert.assertNotNull("sd != null", sd);

      // Check offered portlets
      List<V1PortletDescription> offeredPortlets = sd.getOfferedPortlets();
      ExtendedAssert.assertNotNull(offeredPortlets);
      ExtendedAssert.assertEquals(1, offeredPortlets.size());

      // Check portlet description
      V1PortletDescription desc = offeredPortlets.get(0);

      checkBasicPortletDescription(desc, null);

      undeploy("test-basic-portlet.war");
      return sd; // for further testing...
   }

   protected V1RegistrationContext registerConsumer() throws V1OperationFailed, V1MissingParameters
   {
      V1RegistrationData registrationData = createBaseRegData();
      return producer.register(registrationData);
   }

   protected V1RegistrationData createBaseRegData()
   {
      V1RegistrationData regData = WSRP1TypeFactory.createDefaultRegistrationData();
      regData.setConsumerName(CONSUMER);
      regData.getRegistrationProperties().add(WSRP1TypeFactory.createProperty("regProp", "en", "regValue"));
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

   protected V1GetServiceDescription getNoRegistrationServiceDescriptionRequest()
   {
      V1GetServiceDescription gs = WSRP1TypeFactory.createGetServiceDescription();
      gs.getDesiredLocales().add("en-US");
      gs.getDesiredLocales().add("en");
      return gs;
   }

   /** === asserts === * */

   protected static void assertEquals(V1MarkupType expected, V1MarkupType tested)
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

         Collections.sort(expected.getWindowStates());
         Collections.sort(tested.getWindowStates());
         assertEquals(message + "Window states", expected.getWindowStates(), tested.getWindowStates());
      }
   }

   protected static void assertEquals(V1PropertyDescription expected, V1PropertyDescription tested)
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

   protected static void assertEquals(String message, V1LocalizedString expected, V1LocalizedString tested)
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
   
   
   protected V1GetMarkup createDefaultMarkupRequest(String handle)
   {
      V1PortletContext portletContext = WSRP1TypeFactory.createPortletContext(handle);
      return WSRP1TypeFactory.createMarkupRequest(portletContext, createDefaultRuntimeContext(), WSRP1TypeFactory.createDefaultMarkupParams());
   }
   
   protected V1PerformBlockingInteraction createDefaultPerformBlockingInteraction(String handle)
   {
      V1PortletContext portletContext = WSRP1TypeFactory.createPortletContext(handle);
      return WSRP1TypeFactory.createPerformBlockingInteraction(portletContext, createDefaultRuntimeContext(), WSRP1TypeFactory.createDefaultMarkupParams(),
            WSRP1TypeFactory.createDefaultInteractionParams());
   }
   
   protected V1RuntimeContext createDefaultRuntimeContext()
   {
      return WSRP1TypeFactory.createRuntimeContext(WSRPConstants.NONE_USER_AUTHENTICATION, "foo", "bar");
   }
}
