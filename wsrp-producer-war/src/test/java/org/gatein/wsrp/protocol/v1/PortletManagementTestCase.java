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

import org.gatein.wsrp.WSRPConstants;
import org.gatein.wsrp.api.servlet.ServletAccess;
import org.gatein.wsrp.producer.WSRPProducerBaseTest;
import org.gatein.wsrp.spec.v1.WSRP1TypeFactory;
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
import org.oasis.wsrp.v1.V1AccessDenied;
import org.oasis.wsrp.v1.V1ClonePortlet;
import org.oasis.wsrp.v1.V1DestroyFailed;
import org.oasis.wsrp.v1.V1DestroyPortlets;
import org.oasis.wsrp.v1.V1DestroyPortletsResponse;
import org.oasis.wsrp.v1.V1GetPortletDescription;
import org.oasis.wsrp.v1.V1GetPortletProperties;
import org.oasis.wsrp.v1.V1GetPortletPropertyDescription;
import org.oasis.wsrp.v1.V1GetServiceDescription;
import org.oasis.wsrp.v1.V1InconsistentParameters;
import org.oasis.wsrp.v1.V1InvalidHandle;
import org.oasis.wsrp.v1.V1InvalidRegistration;
import org.oasis.wsrp.v1.V1InvalidUserCategory;
import org.oasis.wsrp.v1.V1MissingParameters;
import org.oasis.wsrp.v1.V1ModelDescription;
import org.oasis.wsrp.v1.V1OperationFailed;
import org.oasis.wsrp.v1.V1PortletContext;
import org.oasis.wsrp.v1.V1PortletDescriptionResponse;
import org.oasis.wsrp.v1.V1PortletPropertyDescriptionResponse;
import org.oasis.wsrp.v1.V1Property;
import org.oasis.wsrp.v1.V1PropertyDescription;
import org.oasis.wsrp.v1.V1PropertyList;
import org.oasis.wsrp.v1.V1SetPortletProperties;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 11547 $
 * @since 2.4
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
      jar.addClass(V1ProducerBaseTest.class);
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

   @Test
   public void testClonePortlet() throws Exception
   {
      String handle = getDefaultHandle();
      V1PortletContext initialContext = WSRP1TypeFactory.createPortletContext(handle);

      // first check that we get a new PortletContext
      V1PortletContext cloneContext = clonePortlet(handle);
      ExtendedAssert.assertNotNull(cloneContext);
      ExtendedAssert.assertFalse(initialContext.equals(cloneContext));

      // then check that the initial state is identical
      V1GetPortletProperties getPortletProperties = WSRP1TypeFactory.createGetPortletProperties(null, cloneContext);
      List<V1Property> result = producer.getPortletProperties(getPortletProperties).getProperties();
      getPortletProperties = WSRP1TypeFactory.createGetPortletProperties(null, initialContext);
      checkGetPropertiesResponse(producer.getPortletProperties(getPortletProperties), result);

      // check that new clone is not listed in service description
      V1GetServiceDescription gs = getNoRegistrationServiceDescriptionRequest();
      checkServiceDescriptionWithOnlyBasicPortlet(gs);
   }

   @Test
   public void testClonePortletNoRegistrationWhenRequired()
   {
      producer.getConfigurationService().getConfiguration().getRegistrationRequirements().setRegistrationRequired(true);

      String handle = getDefaultHandle();
      V1ClonePortlet clonePortlet = WSRP1TypeFactory.createSimpleClonePortlet(handle);

      try
      {
         producer.clonePortlet(clonePortlet);
         ExtendedAssert.fail("Should have thrown InvalidRegistrationFault");
      }
      catch (V1InvalidRegistration invalidRegistrationFault)
      {
         // expected
      }
      catch (Exception e)
      {
         ExtendedAssert.fail(e.getMessage());
      }
   }

   @Test
   public void testDestroyPortlets() throws Exception
   {
      // first try to destroy POP, should fail
      String handle = getDefaultHandle();
      V1DestroyPortlets destroyPortlets = WSRP1TypeFactory.createDestroyPortlets(null, Collections.<String>singletonList(handle));
      V1DestroyPortletsResponse response = producer.destroyPortlets(destroyPortlets);
      ExtendedAssert.assertNotNull(response);
      List<V1DestroyFailed> failures = response.getDestroyFailed();
      ExtendedAssert.assertNotNull(failures);
      ExtendedAssert.assertEquals(1, failures.size());
      V1DestroyFailed failure = failures.get(0);
      ExtendedAssert.assertNotNull(failure);
      ExtendedAssert.assertEquals(handle, failure.getPortletHandle());
      ExtendedAssert.assertNotNull(failure.getReason());

      // clone portlet and try to destroy it
      V1PortletContext portletContext = clonePortlet(handle);
      destroyPortlets = WSRP1TypeFactory.createDestroyPortlets(null, Collections.<String>singletonList(portletContext.getPortletHandle()));
      response = producer.destroyPortlets(destroyPortlets);
      ExtendedAssert.assertNotNull(response);
      failures = response.getDestroyFailed();

      ExtendedAssert.assertTrue("Got back failures when none expected :" + failures, (failures == null || failures.isEmpty()));
   }

   @Test
   public void testDestroyPortletsNoRegistrationWhenRequired()
   {
      producer.getConfigurationService().getConfiguration().getRegistrationRequirements().setRegistrationRequired(true);

      String handle = getDefaultHandle();
      V1DestroyPortlets dp = WSRP1TypeFactory.createDestroyPortlets(null, Collections.<String>singletonList(handle));

      try
      {
         producer.destroyPortlets(dp);
         ExtendedAssert.fail("Should have thrown InvalidRegistrationFault");
      }
      catch (V1InvalidRegistration invalidRegistrationFault)
      {
         // expected
      }
      catch (Exception e)
      {
         ExtendedAssert.fail(e.getMessage());
      }
   }

   @Test
   public void testGetPortletDescription() throws Exception
   {
      String handle = getDefaultHandle();
      V1GetPortletDescription gpd = WSRP1TypeFactory.createGetPortletDescription(null, handle);

      V1PortletDescriptionResponse response = producer.getPortletDescription(gpd);
      ExtendedAssert.assertNotNull(response);

      checkBasicPortletDescription(response.getPortletDescription(), handle);
   }

   @Test
   public void testGetPortletDescriptionNoRegistrationWhenRequired()
   {
      producer.getConfigurationService().getConfiguration().getRegistrationRequirements().setRegistrationRequired(true);

      String handle = getDefaultHandle();
      V1GetPortletDescription gpd = WSRP1TypeFactory.createGetPortletDescription(null, handle);

      try
      {
         producer.getPortletDescription(gpd);
         ExtendedAssert.fail("Should have thrown InvalidRegistrationFault");
      }
      catch (V1InvalidRegistration invalidRegistrationFault)
      {
         // expected
      }
      catch (Exception e)
      {
         ExtendedAssert.fail(e.getMessage());
      }
   }

   @Test
   public void testGetPortletPropertiesNoKeys() throws Exception
   {
      String handle = getDefaultHandle();
      V1PortletContext initialContext = WSRP1TypeFactory.createPortletContext(handle);
      V1GetPortletProperties getPortletProperties = WSRP1TypeFactory.createGetPortletProperties(null, initialContext);

      V1PropertyList response = producer.getPortletProperties(getPortletProperties);
      List<V1Property> expected = new ArrayList<V1Property>(2);
      Collections.addAll(expected, WSRP1TypeFactory.createProperty("prefName1", "en", "prefValue1"),
         WSRP1TypeFactory.createProperty("prefName2", "en", "prefValue2"));
      checkGetPropertiesResponse(response, expected);
   }

   @Test
   public void testGetPortletPropertiesNoRegistration() throws Exception
   {
      String handle = getDefaultHandle();
      V1PortletContext initialContext = WSRP1TypeFactory.createPortletContext(handle);
      V1GetPortletProperties getPortletProperties = WSRP1TypeFactory.createGetPortletProperties(null, initialContext);

      List<String> names = getPortletProperties.getNames();
      Collections.addAll(names, "prefName1", "prefName2");

      V1PropertyList response = producer.getPortletProperties(getPortletProperties);
      List<V1Property> expected = new ArrayList<V1Property>(2);
      Collections.addAll(expected, WSRP1TypeFactory.createProperty("prefName1", "en", "prefValue1"),
         WSRP1TypeFactory.createProperty("prefName2", "en", "prefValue2"));
      checkGetPropertiesResponse(response, expected);

      names.clear();
      response = producer.getPortletProperties(getPortletProperties);

      names.add("prefName2");
      response = producer.getPortletProperties(getPortletProperties);
      checkGetPropertiesResponse(response, Collections.<V1Property>singletonList(WSRP1TypeFactory.createProperty("prefName2", "en", "prefValue2")));
   }

   public void testGetPortletPropertyDescription() throws Exception
   {
      String handle = getDefaultHandle();
      V1GetPortletPropertyDescription getPortletPropertyDescription = WSRP1TypeFactory.createSimpleGetPortletPropertyDescription(handle);

      V1PortletPropertyDescriptionResponse response = producer.getPortletPropertyDescription(getPortletPropertyDescription);

      V1ModelDescription desc = response.getModelDescription();
      ExtendedAssert.assertNotNull(desc);
      List<V1PropertyDescription> propertyDescriptions = desc.getPropertyDescriptions();
      ExtendedAssert.assertNotNull(propertyDescriptions);

      List<V1PropertyDescription> expected = new ArrayList<V1PropertyDescription>(2);
      V1PropertyDescription description = WSRP1TypeFactory.createPropertyDescription("prefName1", WSRPConstants.XSD_STRING);
      description.setHint(WSRP1TypeFactory.createLocalizedString("prefName1"));
      description.setLabel(WSRP1TypeFactory.createLocalizedString("prefName1"));
      expected.add(description);
      description = WSRP1TypeFactory.createPropertyDescription("prefName2", WSRPConstants.XSD_STRING);
      description.setHint(WSRP1TypeFactory.createLocalizedString("prefName2"));
      description.setLabel(WSRP1TypeFactory.createLocalizedString("prefName2"));
      expected.add(description);

      checkPropertyDescriptions(expected, propertyDescriptions);
   }

   @Test
   public void testGetPortletPropertiesNoRegistrationWhenRequired()
   {
      producer.getConfigurationService().getConfiguration().getRegistrationRequirements().setRegistrationRequired(true);

      String handle = getDefaultHandle();
      V1GetPortletPropertyDescription getPortletPropertyDescription = WSRP1TypeFactory.createSimpleGetPortletPropertyDescription(handle);

      try
      {
         producer.getPortletPropertyDescription(getPortletPropertyDescription);
         ExtendedAssert.fail("Should have thrown InvalidRegistrationFault");
      }
      catch (V1InvalidRegistration invalidRegistrationFault)
      {
         // expected
      }
      catch (Exception e)
      {
         ExtendedAssert.fail(e.getMessage());
      }
   }

   @Test
   public void testSetPortletProperties() throws Exception
   {
      String handle = getDefaultHandle();

      V1PortletContext portletContext = clonePortlet(handle);
      V1PropertyList propertyList = WSRP1TypeFactory.createPropertyList();
      List<V1Property> properties = propertyList.getProperties();
      Collections.addAll(properties, WSRP1TypeFactory.createProperty("prefName1", "en", "newPrefValue1"),
         WSRP1TypeFactory.createProperty("prefName2", "en", "newPrefValue2"));
      V1SetPortletProperties setPortletProperties = WSRP1TypeFactory.createSetPortletProperties(null, portletContext, propertyList);

      V1PortletContext response = producer.setPortletProperties(setPortletProperties);
      V1GetPortletProperties getPortletProperties = WSRP1TypeFactory.createGetPortletProperties(null, response);
      Collections.addAll(getPortletProperties.getNames(), "prefName1", "prefName2");

      checkGetPropertiesResponse(producer.getPortletProperties(getPortletProperties), properties);

      portletContext = WSRP1TypeFactory.createPortletContext(handle);
      setPortletProperties.setPortletContext(portletContext);
      try
      {
         response = producer.setPortletProperties(setPortletProperties);
         ExtendedAssert.fail("Setting properties on Producer-Offered Portlet should fail...");
      }
      catch (V1InconsistentParameters expected)
      {
         // expected
      }
   }

   @Test
   public void testSetPortletPropertiesNoLanguage() throws Exception
   {
      String handle = getDefaultHandle();

      V1PortletContext portletContext = clonePortlet(handle);
      V1PropertyList propertyList = WSRP1TypeFactory.createPropertyList();
      List<V1Property> properties = propertyList.getProperties();
      Collections.addAll(properties, WSRP1TypeFactory.createProperty("prefName1", null, "newPrefValue1"),
         WSRP1TypeFactory.createProperty("prefName2", null, "newPrefValue2"));
      V1SetPortletProperties setPortletProperties = WSRP1TypeFactory.createSetPortletProperties(null, portletContext, propertyList);

      V1PortletContext response = producer.setPortletProperties(setPortletProperties);
      V1GetPortletProperties getPortletProperties = WSRP1TypeFactory.createGetPortletProperties(null, response);
      Collections.addAll(getPortletProperties.getNames(), "prefName1", "prefName2");

      // need to reset properties to use a language since getPortletProperties will return the associated language
      propertyList = WSRP1TypeFactory.createPropertyList();
      properties = propertyList.getProperties();
      Collections.addAll(properties, WSRP1TypeFactory.createProperty("prefName1", "en", "newPrefValue1"),
         WSRP1TypeFactory.createProperty("prefName2", "en", "newPrefValue2"));

      checkGetPropertiesResponse(producer.getPortletProperties(getPortletProperties), properties);
   }

   @Test
   public void testSetResetSamePortletProperty() throws Exception
   {
      String handle = getDefaultHandle();

      V1PortletContext portletContext = clonePortlet(handle);
      V1PropertyList propertyList = WSRP1TypeFactory.createPropertyList();
      propertyList.getProperties().add(WSRP1TypeFactory.createProperty("prefName1", "en", "newPrefValue1"));
      propertyList.getResetProperties().add(WSRP1TypeFactory.createResetProperty("prefName1"));
      V1SetPortletProperties setPortletProperties = WSRP1TypeFactory.createSetPortletProperties(null, portletContext, propertyList);

      try
      {
         producer.setPortletProperties(setPortletProperties);
         fail("Shouldn't be possible to set and reset a property in the same call");
      }
      catch (V1InconsistentParameters v1InconsistentParameters)
      {
         // expected
      }
   }

   @Test
   public void testSetPortletPropertiesNoRegistrationWhenRequired()
   {
      producer.getConfigurationService().getConfiguration().getRegistrationRequirements().setRegistrationRequired(true);

      V1PropertyList propertyList = WSRP1TypeFactory.createPropertyList();
      List<V1Property> properties = propertyList.getProperties();
      Collections.addAll(properties, WSRP1TypeFactory.createProperty("prefName1", "en", "newPrefValue1"),
         WSRP1TypeFactory.createProperty("prefName2", "en", "newPrefValue2"));
      V1SetPortletProperties setPortletProperties = WSRP1TypeFactory.createSetPortletProperties(null,
         WSRP1TypeFactory.createPortletContext(getDefaultHandle()), propertyList);

      try
      {
         producer.setPortletProperties(setPortletProperties);
         ExtendedAssert.fail("Should have thrown InvalidRegistrationFault");
      }
      catch (V1InvalidRegistration invalidRegistration)
      {
         // expected
      }
      catch (Exception e)
      {
         ExtendedAssert.fail(e.getMessage());
      }
   }

   private V1PortletContext clonePortlet(String handle) throws V1InvalidUserCategory, V1InconsistentParameters,
      V1InvalidRegistration, V1MissingParameters, V1OperationFailed, V1AccessDenied, V1InvalidHandle
   {
      V1ClonePortlet clonePortlet = WSRP1TypeFactory.createSimpleClonePortlet(handle);
      return producer.clonePortlet(clonePortlet);
   }

   private List<V1Property> checkGetPropertiesResponse(V1PropertyList response, List<V1Property> expected)
   {
      ExtendedAssert.assertNotNull(response);
      List<V1Property> properties = response.getProperties();
      ExtendedAssert.assertEquals(expected.toArray(), properties.toArray(), false, "Didn't receive expected properties!", new PropertyDecorator());
      return properties;
   }

   private void checkPropertyDescriptions(List<V1PropertyDescription> expected, List<V1PropertyDescription> propertyDescriptions)
   {
      ExtendedAssert.assertEquals(expected.size(), propertyDescriptions.size());
      V1PropertyDescription propDesc = propertyDescriptions.get(0);
      ExtendedAssert.assertNotNull(propDesc);
      String name = propDesc.getName();
      if ("prefName1".equals(name))
      {
         assertEquals(expected.get(0), propDesc);
         assertEquals(expected.get(1), propertyDescriptions.get(1));
      }
      else if ("prefName2".equals(name))
      {
         assertEquals(expected.get(1), propDesc);
         assertEquals(expected.get(0), propertyDescriptions.get(1));
      }
      else
      {
         ExtendedAssert.fail("Unexpected PropertyDescription named '" + name + "'");
      }
   }

   protected String getMostUsedPortletWARFileName()
   {
      return TEST_BASIC_PORTLET_WAR;
   }

   private static class PropertyDecorator implements ExtendedAssert.Decorator
   {
      private V1Property prop;

      public void decorate(Object decorated)
      {
         prop = (V1Property)decorated;
      }

      public boolean equals(Object o)
      {
         if (o instanceof ExtendedAssert.DecoratedObject)
         {
            ExtendedAssert.DecoratedObject decoratedObject = (ExtendedAssert.DecoratedObject)o;
            V1Property that = (V1Property)decoratedObject.getDecorated();

            String name = prop.getName();
            if (name != null ? !name.equals(that.getName()) : that.getName() != null)
            {
               return false;
            }

            String value = prop.getStringValue();
            if (value != null ? !value.equals(that.getStringValue()) : that.getStringValue() != null)
            {
               return false;
            }

            String lang = prop.getLang();
            if (lang != null ? !lang.equals(that.getLang()) : that.getLang() != null)
            {
               return false;
            }

            List<Object> any = prop.getAny();
            return !(any != null ? !any.equals(that.getAny()) : that.getAny() != null);

         }
         else
         {
            return false;
         }
      }


      public String toString()
      {
         return new StringBuffer().append("Property: ").append(prop.getName()).append("=")
            .append(prop.getStringValue()).append(" (").append(prop.getLang()).append(")").toString();
      }
   }
}