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

/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.gatein.wsrp.protocol.v1;

import org.gatein.wsrp.WSRPUtils;
import org.gatein.wsrp.protocol.v1.V1ProducerBaseTest;
import org.gatein.wsrp.registration.RegistrationPropertyDescription;
import org.gatein.wsrp.servlet.ServletAccess;
import org.gatein.wsrp.spec.v1.V2ToV1Converter;
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
import org.oasis.wsrp.v1.V1GetServiceDescription;
import org.oasis.wsrp.v1.V1ModelDescription;
import org.oasis.wsrp.v1.V1PropertyDescription;
import org.oasis.wsrp.v1.V1ServiceDescription;

import java.util.List;

/**
 * Tests WSRP Service Description
 *
 * @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 11547 $
 * @since 2.4 (Feb 20, 2006)
 */
@RunWith(Arquillian.class)
public class ServiceDescriptionTestCase extends V1ProducerBaseTest
{
   public ServiceDescriptionTestCase() throws Exception
   {
      super("ServiceDescriptionTestCase");
   }

   @Deployment
   public static JavaArchive createDeployment()
   {
       return ShrinkWrap.create("test.jar", JavaArchive.class);
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

   @Test
   public void testNotRequiringRegistration() throws Throwable
   {
      producer.getConfigurationService().getConfiguration().getRegistrationRequirements().setRegistrationRequired(false);

      V1GetServiceDescription gs = getNoRegistrationServiceDescriptionRequest();

      V1ServiceDescription sd = checkServiceDescriptionWithOnlyBasicPortlet(gs);

      // registration is not required
      ExtendedAssert.assertFalse(sd.isRequiresRegistration());

      // No registration properties
      ExtendedAssert.assertNull(sd.getRegistrationPropertyDescription());
   }

   @Test
   public void testRequiringRegistrationNotProvidingPortlets() throws Throwable
   {
      RegistrationPropertyDescription regProp = configureRegistrationSettings(true, false);

      // service description request without registration info
      V1GetServiceDescription gs = getNoRegistrationServiceDescriptionRequest();

      //Invoke the Web Service
      V1ServiceDescription sd = producer.getServiceDescription(gs);
      ExtendedAssert.assertNotNull(sd);
      ExtendedAssert.assertTrue(sd.isRequiresRegistration());

      // Check registration properties
      checkRequiredRegistrationProperties(sd, regProp);

      // No offered portlets without registration!
      ExtendedAssert.assertTrue("Should not get any offered portlets back. Retrieved " + sd.getOfferedPortlets(), (sd.getOfferedPortlets() == null || sd.getOfferedPortlets().isEmpty()));
   }

   @Test
   public void testRequiringRegistrationProvidingPortlets() throws Throwable
   {
      RegistrationPropertyDescription regProp = configureRegistrationSettings(true, true);

      // service description request without registration info
      V1GetServiceDescription gs = getNoRegistrationServiceDescriptionRequest();

      //Invoke the Web Service, we should have the complete description
      V1ServiceDescription sd = checkServiceDescriptionWithOnlyBasicPortlet(gs);
      ExtendedAssert.assertNotNull(sd);
      ExtendedAssert.assertTrue(sd.isRequiresRegistration());

      // Check registration properties
      checkRequiredRegistrationProperties(sd, regProp);
   }

   @Test
   public void testLiveDeployment() throws Throwable
   {
      try
      {
         V1GetServiceDescription gsd = getNoRegistrationServiceDescriptionRequest();

         deploy("test-basic-portlet.war");
         V1ServiceDescription sd = producer.getServiceDescription(gsd);
         ExtendedAssert.assertEquals(1, sd.getOfferedPortlets().size());

         deploy("test-markup-portlet.war");
         sd = producer.getServiceDescription(gsd);
         // should now have 2 offered portlets
         ExtendedAssert.assertEquals(2, sd.getOfferedPortlets().size());

         deploy("test-session-portlet.war");
         sd = producer.getServiceDescription(gsd);
         // should now have 3 offered portlets
         ExtendedAssert.assertEquals(3, sd.getOfferedPortlets().size());

         undeploy("test-markup-portlet.war");
         sd = producer.getServiceDescription(gsd);
         // should now have only 2 offered portlets again
         ExtendedAssert.assertEquals(2, sd.getOfferedPortlets().size());

         undeploy("test-session-portlet.war");
         // only basic portlet should still be offered
         checkServiceDescriptionWithOnlyBasicPortlet(gsd);
      }
      finally
      {
         undeploy("test-basic-portlet.war");
         undeploy("test-markup-portlet.war");
         undeploy("test-session-portlet.war");
      }
   }

   private void checkRequiredRegistrationProperties(V1ServiceDescription sd, RegistrationPropertyDescription regProp)
   {
      V1ModelDescription registrationPropertyDescription = sd.getRegistrationPropertyDescription();
      ExtendedAssert.assertNotNull(registrationPropertyDescription);
      List<V1PropertyDescription> propertyDescriptions = registrationPropertyDescription.getPropertyDescriptions();
      ExtendedAssert.assertNotNull(propertyDescriptions);
      ExtendedAssert.assertEquals(1, propertyDescriptions.size());
      assertEquals(V2ToV1Converter.toV1PropertyDescription(WSRPUtils.convertToPropertyDescription(regProp)), propertyDescriptions.get(0));
   }
}