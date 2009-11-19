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
import org.gatein.wsrp.test.ExtendedAssert;
import org.oasis.wsrp.v1.AccessDenied;
import org.oasis.wsrp.v1.ClonePortlet;
import org.oasis.wsrp.v1.DestroyFailed;
import org.oasis.wsrp.v1.DestroyPortlets;
import org.oasis.wsrp.v1.DestroyPortletsResponse;
import org.oasis.wsrp.v1.GetPortletDescription;
import org.oasis.wsrp.v1.GetPortletProperties;
import org.oasis.wsrp.v1.GetPortletPropertyDescription;
import org.oasis.wsrp.v1.GetServiceDescription;
import org.oasis.wsrp.v1.InconsistentParameters;
import org.oasis.wsrp.v1.InvalidHandle;
import org.oasis.wsrp.v1.InvalidRegistration;
import org.oasis.wsrp.v1.InvalidUserCategory;
import org.oasis.wsrp.v1.MissingParameters;
import org.oasis.wsrp.v1.ModelDescription;
import org.oasis.wsrp.v1.OperationFailed;
import org.oasis.wsrp.v1.PortletContext;
import org.oasis.wsrp.v1.PortletDescriptionResponse;
import org.oasis.wsrp.v1.PortletPropertyDescriptionResponse;
import org.oasis.wsrp.v1.Property;
import org.oasis.wsrp.v1.PropertyDescription;
import org.oasis.wsrp.v1.PropertyList;
import org.oasis.wsrp.v1.SetPortletProperties;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 11547 $
 * @since 2.4
 */
public class PortletManagementTestCase extends NeedPortletHandleTest
{
   private static final String TEST_BASIC_PORTLET_WAR = "test-basic-portlet.war";

   public PortletManagementTestCase() throws Exception
   {
      super("PortletManagementTestCase", TEST_BASIC_PORTLET_WAR);
   }

   public void testClonePortlet() throws Exception
   {
      String handle = getDefaultHandle();
      PortletContext initialContext = WSRPTypeFactory.createPortletContext(handle);

      // first check that we get a new PortletContext
      PortletContext cloneContext = clonePortlet(handle);
      ExtendedAssert.assertNotNull(cloneContext);
      ExtendedAssert.assertFalse(initialContext.equals(cloneContext));

      // then check that the initial state is identical
      GetPortletProperties getPortletProperties = WSRPTypeFactory.createGetPortletProperties(null, cloneContext);
      List<Property> result = portletManagementService.getPortletProperties(getPortletProperties).getProperties();
      getPortletProperties = WSRPTypeFactory.createGetPortletProperties(null, initialContext);
      checkGetPropertiesResponse(portletManagementService.getPortletProperties(getPortletProperties), result);

      // check that new clone is not listed in service description
      GetServiceDescription gs = getNoRegistrationServiceDescriptionRequest();
      checkServiceDescriptionWithOnlyBasicPortlet(gs);
   }

   public void testClonePortletNoRegistrationWhenRequired()
   {
      producer.getConfigurationService().getConfiguration().getRegistrationRequirements().setRegistrationRequired(true);

      String handle = getDefaultHandle();
      ClonePortlet clonePortlet = WSRPTypeFactory.createSimpleClonePortlet(handle);

      try
      {
         portletManagementService.clonePortlet(clonePortlet);
         ExtendedAssert.fail("Should have thrown InvalidRegistrationFault");
      }
      catch (InvalidRegistration invalidRegistrationFault)
      {
         // expected
      }
      catch (Exception e)
      {
         ExtendedAssert.fail(e.getMessage());
      }
   }

   public void testDestroyPortlets() throws Exception
   {
      // first try to destroy POP, should fail
      String handle = getDefaultHandle();
      DestroyPortlets destroyPortlets = WSRPTypeFactory.createDestroyPortlets(null, Collections.<String>singletonList(handle));
      DestroyPortletsResponse response = portletManagementService.destroyPortlets(destroyPortlets);
      ExtendedAssert.assertNotNull(response);
      List<DestroyFailed> failures = response.getDestroyFailed();
      ExtendedAssert.assertNotNull(failures);
      ExtendedAssert.assertEquals(1, failures.size());
      DestroyFailed failure = failures.get(0);
      ExtendedAssert.assertNotNull(failure);
      ExtendedAssert.assertEquals(handle, failure.getPortletHandle());
      ExtendedAssert.assertNotNull(failure.getReason());

      // clone portlet and try to destroy it
      PortletContext portletContext = clonePortlet(handle);
      destroyPortlets = WSRPTypeFactory.createDestroyPortlets(null, Collections.<String>singletonList(portletContext.getPortletHandle()));
      response = portletManagementService.destroyPortlets(destroyPortlets);
      ExtendedAssert.assertNotNull(response);
      failures = response.getDestroyFailed();
      ExtendedAssert.assertNull(failures);
   }

   public void testDestroyPortletsNoRegistrationWhenRequired()
   {
      producer.getConfigurationService().getConfiguration().getRegistrationRequirements().setRegistrationRequired(true);

      String handle = getDefaultHandle();
      DestroyPortlets dp = WSRPTypeFactory.createDestroyPortlets(null, Collections.<String>singletonList(handle));

      try
      {
         portletManagementService.destroyPortlets(dp);
         ExtendedAssert.fail("Should have thrown InvalidRegistrationFault");
      }
      catch (InvalidRegistration invalidRegistrationFault)
      {
         // expected
      }
      catch (Exception e)
      {
         ExtendedAssert.fail(e.getMessage());
      }
   }

   public void testGetPortletDescription() throws Exception
   {
      String handle = getDefaultHandle();
      GetPortletDescription gpd = WSRPTypeFactory.createGetPortletDescription(null, handle);

      PortletDescriptionResponse response = portletManagementService.getPortletDescription(gpd);
      ExtendedAssert.assertNotNull(response);

      checkBasicPortletDescription(response.getPortletDescription(), handle);
   }

   public void testGetPortletDescriptionNoRegistrationWhenRequired()
   {
      producer.getConfigurationService().getConfiguration().getRegistrationRequirements().setRegistrationRequired(true);

      String handle = getDefaultHandle();
      GetPortletDescription gpd = WSRPTypeFactory.createGetPortletDescription(null, handle);

      try
      {
         portletManagementService.getPortletDescription(gpd);
         ExtendedAssert.fail("Should have thrown InvalidRegistrationFault");
      }
      catch (InvalidRegistration invalidRegistrationFault)
      {
         // expected
      }
      catch (Exception e)
      {
         ExtendedAssert.fail(e.getMessage());
      }
   }

   public void testGetPortletPropertiesNoRegistration() throws Exception
   {
      String handle = getDefaultHandle();
      PortletContext initialContext = WSRPTypeFactory.createPortletContext(handle);
      GetPortletProperties getPortletProperties = WSRPTypeFactory.createGetPortletProperties(null, initialContext);

      List<String> names = getPortletProperties.getNames();
      Collections.addAll(names, "prefName1", "prefName2");

      PropertyList response = portletManagementService.getPortletProperties(getPortletProperties);
      List<Property> expected = new ArrayList<Property>(2);
      Collections.addAll(expected, WSRPTypeFactory.createProperty("prefName1", "en", "prefValue1"),
         WSRPTypeFactory.createProperty("prefName2", "en", "prefValue2"));
      checkGetPropertiesResponse(response, expected);

      names.clear();
      response = portletManagementService.getPortletProperties(getPortletProperties);
      checkGetPropertiesResponse(response, expected);

      names.add("prefName2");
      response = portletManagementService.getPortletProperties(getPortletProperties);
      checkGetPropertiesResponse(response, Collections.<Property>singletonList(WSRPTypeFactory.createProperty("prefName2", "en", "prefValue2")));
   }

   public void testGetPortletPropertyDescription() throws Exception
   {
      String handle = getDefaultHandle();
      GetPortletPropertyDescription getPortletPropertyDescription = WSRPTypeFactory.createSimpleGetPortletPropertyDescription(handle);

      PortletPropertyDescriptionResponse response = portletManagementService.getPortletPropertyDescription(getPortletPropertyDescription);

      ModelDescription desc = response.getModelDescription();
      ExtendedAssert.assertNotNull(desc);
      List<PropertyDescription> propertyDescriptions = desc.getPropertyDescriptions();
      ExtendedAssert.assertNotNull(propertyDescriptions);

      List<PropertyDescription> expected = new ArrayList<PropertyDescription>(2);
      PropertyDescription description = WSRPTypeFactory.createPropertyDescription("prefName1", WSRPConstants.XSD_STRING);
      description.setHint(WSRPTypeFactory.createLocalizedString("prefName1"));
      description.setLabel(WSRPTypeFactory.createLocalizedString("prefName1"));
      expected.add(description);
      description = WSRPTypeFactory.createPropertyDescription("prefName2", WSRPConstants.XSD_STRING);
      description.setHint(WSRPTypeFactory.createLocalizedString("prefName2"));
      description.setLabel(WSRPTypeFactory.createLocalizedString("prefName2"));
      expected.add(description);

      checkPropertyDescriptions(expected, propertyDescriptions);
   }

   public void testGetPortletPropertiesNoRegistrationWhenRequired()
   {
      producer.getConfigurationService().getConfiguration().getRegistrationRequirements().setRegistrationRequired(true);

      String handle = getDefaultHandle();
      GetPortletPropertyDescription getPortletPropertyDescription = WSRPTypeFactory.createSimpleGetPortletPropertyDescription(handle);

      try
      {
         portletManagementService.getPortletPropertyDescription(getPortletPropertyDescription);
         ExtendedAssert.fail("Should have thrown InvalidRegistrationFault");
      }
      catch (InvalidRegistration invalidRegistrationFault)
      {
         // expected
      }
      catch (Exception e)
      {
         ExtendedAssert.fail(e.getMessage());
      }
   }

   public void testSetPortletProperties() throws Exception
   {
      String handle = getDefaultHandle();

      PortletContext portletContext = clonePortlet(handle);
      PropertyList propertyList = WSRPTypeFactory.createPropertyList();
      List<Property> properties = propertyList.getProperties();
      Collections.addAll(properties, WSRPTypeFactory.createProperty("prefName1", "en", "newPrefValue1"),
         WSRPTypeFactory.createProperty("prefName2", "en", "newPrefValue2"));
      SetPortletProperties setPortletProperties = WSRPTypeFactory.createSetPortletProperties(null, portletContext, propertyList);

      PortletContext response = portletManagementService.setPortletProperties(setPortletProperties);
      GetPortletProperties getPortletProperties = WSRPTypeFactory.createGetPortletProperties(null, response);
      checkGetPropertiesResponse(portletManagementService.getPortletProperties(getPortletProperties), properties);

      portletContext = WSRPTypeFactory.createPortletContext(handle);
      setPortletProperties.setPortletContext(portletContext);
      try
      {
         response = portletManagementService.setPortletProperties(setPortletProperties);
         ExtendedAssert.fail("Setting properties on Producer-Offered Portlet should fail...");
      }
      catch (InconsistentParameters expected)
      {
         // expected
      }
   }

   public void testSetPortletPropertiesNoRegistrationWhenRequired()
   {
      producer.getConfigurationService().getConfiguration().getRegistrationRequirements().setRegistrationRequired(true);

      PropertyList propertyList = WSRPTypeFactory.createPropertyList();
      List<Property> properties = propertyList.getProperties();
      Collections.addAll(properties, WSRPTypeFactory.createProperty("prefName1", "en", "newPrefValue1"),
         WSRPTypeFactory.createProperty("prefName2", "en", "newPrefValue2"));
      SetPortletProperties setPortletProperties = WSRPTypeFactory.createSetPortletProperties(null,
         WSRPTypeFactory.createPortletContext(getDefaultHandle()), propertyList);

      try
      {
         portletManagementService.setPortletProperties(setPortletProperties);
         ExtendedAssert.fail("Should have thrown InvalidRegistrationFault");
      }
      catch (InvalidRegistration invalidRegistration)
      {
         // expected
      }
      catch (Exception e)
      {
         ExtendedAssert.fail(e.getMessage());
      }
   }

   private PortletContext clonePortlet(String handle) throws InvalidUserCategory, InconsistentParameters,
      InvalidRegistration, MissingParameters, OperationFailed, AccessDenied, InvalidHandle
   {
      ClonePortlet clonePortlet = WSRPTypeFactory.createSimpleClonePortlet(handle);
      return portletManagementService.clonePortlet(clonePortlet);
   }

   private List<Property> checkGetPropertiesResponse(PropertyList response, List<Property> expected)
   {
      ExtendedAssert.assertNotNull(response);
      List<Property> properties = response.getProperties();
      ExtendedAssert.assertEquals(expected.toArray(), properties.toArray(), false, "Didn't receive expected properties!", new PropertyDecorator());
      return properties;
   }

   private void checkPropertyDescriptions(List<PropertyDescription> expected, List<PropertyDescription> propertyDescriptions)
   {
      ExtendedAssert.assertEquals(expected.size(), propertyDescriptions.size());
      PropertyDescription propDesc = propertyDescriptions.get(0);
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
      private Property prop;

      public void decorate(Object decorated)
      {
         prop = (Property)decorated;
      }

      public boolean equals(Object o)
      {
         if (o instanceof ExtendedAssert.DecoratedObject)
         {
            ExtendedAssert.DecoratedObject decoratedObject = (ExtendedAssert.DecoratedObject)o;
            Property that = (Property)decoratedObject.getDecorated();

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

            List<Element> any = prop.getAny();
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