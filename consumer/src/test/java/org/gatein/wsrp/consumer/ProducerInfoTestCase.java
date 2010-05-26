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

package org.gatein.wsrp.consumer;

import junit.framework.TestCase;
import org.gatein.pc.api.Portlet;
import org.gatein.pc.api.PortletContext;
import org.gatein.pc.api.PortletInvokerException;
import org.gatein.wsrp.spec.v1.WSRP1ExceptionFactory;
import org.gatein.wsrp.test.protocol.v1.PortletManagementBehavior;
import org.gatein.wsrp.test.protocol.v1.RegistrationBehavior;
import org.gatein.wsrp.test.protocol.v1.ServiceDescriptionBehavior;
import org.gatein.wsrp.test.support.BehaviorBackedServiceFactory;
import org.gatein.wsrp.test.support.MockConsumerRegistry;
import org.oasis.wsrp.v1.V1AccessDenied;
import org.oasis.wsrp.v1.V1Extension;
import org.oasis.wsrp.v1.V1InconsistentParameters;
import org.oasis.wsrp.v1.V1InvalidHandle;
import org.oasis.wsrp.v1.V1InvalidRegistration;
import org.oasis.wsrp.v1.V1InvalidRegistrationFault;
import org.oasis.wsrp.v1.V1InvalidUserCategory;
import org.oasis.wsrp.v1.V1MissingParameters;
import org.oasis.wsrp.v1.V1OperationFailed;
import org.oasis.wsrp.v1.V1OperationFailedFault;
import org.oasis.wsrp.v1.V1PortletContext;
import org.oasis.wsrp.v1.V1PortletDescription;
import org.oasis.wsrp.v1.V1Property;
import org.oasis.wsrp.v1.V1RegistrationContext;
import org.oasis.wsrp.v1.V1RegistrationData;
import org.oasis.wsrp.v1.V1ResourceList;
import org.oasis.wsrp.v1.V1UserContext;
import org.oasis.wsrp.v2.RegistrationContext;

import javax.jws.WebParam;
import javax.xml.ws.Holder;
import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 12686 $
 * @since 2.6
 */
public class ProducerInfoTestCase extends TestCase
{
   private ProducerInfo info;
   private BehaviorBackedServiceFactory serviceFactory;

   protected void setUp() throws Exception
   {
      info = new ProducerInfo();
      info.setId("test");
      info.setKey("key");

      serviceFactory = new BehaviorBackedServiceFactory();
      EndpointConfigurationInfo eci = new EndpointConfigurationInfo(serviceFactory);
      info.setEndpointConfigurationInfo(eci);

      info.setRegistry(new MockConsumerRegistry());
   }

   public void testSetRegistrationInfo()
   {
      RegistrationInfo regInfo = new RegistrationInfo(info);
      assertEquals(regInfo, info.getRegistrationInfo());
      assertEquals(info, regInfo.getParent());

      RegistrationInfo regInfo2 = new RegistrationInfo();
      assertEquals(regInfo, info.getRegistrationInfo());
      assertNull(regInfo2.getParent());

      info.setRegistrationInfo(regInfo2);
      assertEquals(regInfo2, info.getRegistrationInfo());
      assertEquals(info, regInfo2.getParent());
   }

   public void testSetNullRegistrationInfo()
   {
      try
      {
         info.setRegistrationInfo(null);
         fail("Shouldn't be possible to set a null RegistrationInfo");
      }
      catch (IllegalArgumentException expected)
      {
      }
   }

   public void testSetEndpointConfigurationInfo()
   {
      EndpointConfigurationInfo endInfo = new EndpointConfigurationInfo();
      info.setEndpointConfigurationInfo(endInfo);
      assertEquals(endInfo, info.getEndpointConfigurationInfo());
   }

   public void testSetNullEndpointConfigurationInfo()
   {
      try
      {
         info.setEndpointConfigurationInfo(null);
         fail("Shouldn't be possible to set a null EndpointConfigurationInfo");
      }
      catch (IllegalArgumentException expected)
      {
      }
   }

   public void testRefreshAndCache() throws Exception
   {
      ServiceDescriptionBehavior behavior = new ServiceDescriptionBehavior();
      serviceFactory.getRegistry().setServiceDescriptionBehavior(behavior);

      assertNull(info.getExpirationCacheSeconds());

      assertTrue(info.isRefreshNeeded(false));
      assertFalse(info.isRegistrationChecked());
      assertTrue(info.refresh(false));
      assertFalse(info.isRefreshNeeded(false));
      assertTrue(info.isRegistrationChecked());
      assertTrue(info.refresh(false));
      assertFalse(info.isRefreshNeeded(false));
      assertTrue(info.isRegistrationChecked());
      assertEquals(2, behavior.getCallCount());

      info.setExpirationCacheSeconds(1);
      assertEquals(new Integer(1), info.getExpirationCacheSeconds());
      assertTrue(info.refresh(false));
      assertFalse(info.refresh(false));
      assertFalse(info.isRefreshNeeded(false));
      assertTrue(info.isRegistrationChecked());
      assertEquals(3, behavior.getCallCount());

      // wait for cache expiration
      Thread.sleep(1500);
      assertFalse("refresh is not needed if cache is not considered", info.isRefreshNeeded(false));
      assertTrue("refresh is needed if cache is considered since it has expired", info.isRefreshNeeded(true));
      assertTrue(info.refresh(false));
      assertFalse("Was just refreshed so refresh is not needed even considering cache", info.isRefreshNeeded(true));
      assertTrue(info.isRegistrationChecked());
      assertFalse(info.refresh(false));
      assertTrue(info.refresh(true));
      assertFalse(info.isRefreshNeeded(false));
      assertTrue(info.isRegistrationChecked());
      assertEquals(5, behavior.getCallCount());
   }

   public void testGetPortlet() throws Exception
   {
      ServiceDescriptionBehavior behavior = new ServiceDescriptionBehavior();
      behavior.addPortletDescription(behavior.createPortletDescription("test", null));
      serviceFactory.getRegistry().setServiceDescriptionBehavior(behavior);

      // activate caching for this test so that we can simulate portlet deployment on the producer with a cached SD
      info.setExpirationCacheSeconds(1000);

      PortletContext portletContext = PortletContext.createPortletContext("test");
      Portlet portlet = info.getPortlet(portletContext);
      assertNotNull(portlet);
      assertEquals(portletContext, portlet.getContext());
      assertEquals(1, behavior.getCallCount());

      // test2 is not in the service description, so it should be looked up via Portlet Management...
      portletContext = PortletContext.createPortletContext("test2");
      // add portlet management behavior
      TestPortletManagementBehavior pmBehavior = new TestPortletManagementBehavior();
      serviceFactory.getRegistry().setPortletManagementBehavior(pmBehavior);
      portlet = info.getPortlet(portletContext);
      assertEquals(1, pmBehavior.getCallCount());
      assertNotNull(portlet);
      assertEquals(portletContext, portlet.getContext());

      // try again, this time without a portlet management interface, the service description should be refreshed
      serviceFactory.getRegistry().setPortletManagementBehavior(null);
      // simulate a new portlet deployment since last time the SD was refreshed
      behavior.addPortletDescription(behavior.createPortletDescription("test3", null));
      portletContext = PortletContext.createPortletContext("test3");
      portlet = info.getPortlet(portletContext);
      assertEquals(2, behavior.getCallCount());
      assertNotNull(portlet);
      assertEquals(portletContext, portlet.getContext());
   }

   public void testRefreshAndRegistration() throws Exception
   {
      assertFalse(info.isRegistered());
      RegistrationInfo regInfo = info.getRegistrationInfo();
      assertTrue(regInfo.isUndetermined());

      ServiceDescriptionBehavior sd = new ServiceDescriptionBehavior();
      sd.setRequiresRegistration(true);
      serviceFactory.getRegistry().setServiceDescriptionBehavior(sd);
      RegistrationBehavior regBehavior = new RegistrationBehavior();
      serviceFactory.getRegistry().setRegistrationBehavior(regBehavior);

      assertTrue(info.refresh(false));
      assertEquals(1, regBehavior.getCallCount());
      assertTrue(info.isRegistered());
      assertNotNull(info.getRegistrationInfo());
      assertEquals(RegistrationBehavior.REGISTRATION_HANDLE, info.getRegistrationContext().getRegistrationHandle());

      assertTrue(info.refresh(true));
      assertEquals(1, regBehavior.getCallCount());
   }

   public void testGetRegistrationContext() throws Exception
   {
      assertFalse(info.isRegistered());
      RegistrationInfo regInfo = info.getRegistrationInfo();
      assertTrue(regInfo.isUndetermined());

      ServiceDescriptionBehavior sd = new ServiceDescriptionBehavior();
      sd.setRequiresRegistration(true);
      serviceFactory.getRegistry().setServiceDescriptionBehavior(sd);
      RegistrationBehavior regBehavior = new RegistrationBehavior();
      serviceFactory.getRegistry().setRegistrationBehavior(regBehavior);

      RegistrationContext registrationContext = info.getRegistrationContext();
      assertNotNull(registrationContext);
      assertEquals(RegistrationBehavior.REGISTRATION_HANDLE, registrationContext.getRegistrationHandle());
      assertEquals("Registration should have occured", 1, regBehavior.getCallCount());
      assertEquals("Service description should have been called once unregistered and once registered", 2, sd.getCallCount());
   }

   public void testRegister() throws Exception
   {
      ServiceDescriptionBehavior sd = new ServiceDescriptionBehavior();
      sd.setServiceDescription(true, 1);

      serviceFactory.getRegistry().setServiceDescriptionBehavior(sd);
      RegistrationBehavior regBehavior = new TestRegistrationBehavior();
      serviceFactory.getRegistry().setRegistrationBehavior(regBehavior);

      try
      {
         info.register();
         fail("Shouldn't register with invalid information");
      }
      catch (PortletInvokerException expected)
      {
      }

      RegistrationInfo regInfo = info.getRegistrationInfo();
      regInfo.setRegistrationPropertyValue(TestRegistrationBehavior.PROP_NAME, TestRegistrationBehavior.ORIGINAL_VALUE);

      info.register();
      RegistrationContext registrationContext = info.getRegistrationContext();
      assertNotNull(registrationContext);
      assertEquals(RegistrationBehavior.REGISTRATION_HANDLE, registrationContext.getRegistrationHandle());
      assertTrue(info.isRegistered());
      assertTrue(info.isRegistrationChecked());
      assertTrue(info.isRegistrationRequired());
   }

   public void testDeregister() throws Exception
   {
      try
      {
         info.deregister();
         fail("Shouldn't be able to deregister without being registered");
      }
      catch (IllegalStateException expected)
      {
      }

      // setup registration
      register();

      info.deregister();
      assertNull(info.getRegistrationContext());
      assertFalse(info.isRegistered());
      assertTrue(info.isRegistrationRequired());
      assertTrue(info.isRegistrationChecked());
   }

   private void register() throws PortletInvokerException
   {
      ServiceDescriptionBehavior sd = new ServiceDescriptionBehavior();
      sd.setServiceDescription(true, 1);
      serviceFactory.getRegistry().setServiceDescriptionBehavior(sd);
      RegistrationBehavior regBehavior = new TestRegistrationBehavior();
      serviceFactory.getRegistry().setRegistrationBehavior(regBehavior);
      info.refresh(false);
      RegistrationInfo regInfo = info.getRegistrationInfo();
      regInfo.setRegistrationPropertyValue(TestRegistrationBehavior.PROP_NAME, TestRegistrationBehavior.ORIGINAL_VALUE);

      info.register();
   }

   public void testModifyRegistration() throws Exception
   {
      try
      {
         info.modifyRegistration();
         fail("Shouldn't be able to modify registration without being registered");
      }
      catch (IllegalStateException expected)
      {
      }

      register();

      RegistrationInfo regInfo = info.getRegistrationInfo();
      regInfo.setRegistrationPropertyValue(TestRegistrationBehavior.PROP_NAME, "invalid");
      assertTrue(info.isRefreshNeeded(false));
      assertTrue(info.isRegistered());
      assertTrue(info.isModifyRegistrationRequired());
      RegistrationProperty prop = regInfo.getRegistrationProperty(TestRegistrationBehavior.PROP_NAME);
      assertNull(prop.isInvalid());

      try
      {
         info.modifyRegistration();
         fail("invalid value for property should fail to modify registration");
      }
      catch (PortletInvokerException expected)
      {
      }

      regInfo.setRegistrationPropertyValue(TestRegistrationBehavior.PROP_NAME, TestRegistrationBehavior.MODIFIED_VALUE);
      assertTrue(info.isRefreshNeeded(false));
      assertNull(prop.isInvalid());
      assertTrue(info.isModifyRegistrationRequired());

      info.modifyRegistration();

      assertTrue(info.isRefreshNeeded(true)); // cache should have been invalidated
      assertFalse(info.isRefreshNeeded(false)); // but the rest of the information is valid so no refresh needed there
      assertTrue(info.refresh(false)); // however, if we refresh the producer info, it should have refreshed
      assertFalse(info.isModifyRegistrationRequired());

      Boolean invalid = prop.isInvalid();
      assertNotNull(invalid);
      assertFalse(invalid);
   }

   private static class TestPortletManagementBehavior extends PortletManagementBehavior
   {
      @Override
      public void getPortletDescription(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1PortletContext portletContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1UserContext userContext, @WebParam(name = "desiredLocales", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> desiredLocales, @WebParam(name = "portletDescription", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types", mode = WebParam.Mode.OUT) Holder<V1PortletDescription> portletDescription, @WebParam(name = "resourceList", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types", mode = WebParam.Mode.OUT) Holder<V1ResourceList> resourceList, @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types", mode = WebParam.Mode.OUT) Holder<List<V1Extension>> extensions) throws V1AccessDenied, V1InconsistentParameters, V1InvalidHandle, V1InvalidRegistration, V1InvalidUserCategory, V1MissingParameters, V1OperationFailed
      {
         super.getPortletDescription(registrationContext, portletContext, userContext, desiredLocales, portletDescription, resourceList, extensions);
         portletDescription.value = createPortletDescription("test2", null);
      }
   }

   private static class TestRegistrationBehavior extends RegistrationBehavior
   {
      private static final String PROP_NAME = "prop0";
      private static final String MODIFIED_VALUE = "value2";
      private static final String ORIGINAL_VALUE = "value";

      @Override
      public void register(@WebParam(name = "consumerName", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") String consumerName, @WebParam(name = "consumerAgent", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") String consumerAgent, @WebParam(name = "methodGetSupported", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") boolean methodGetSupported, @WebParam(name = "consumerModes", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> consumerModes, @WebParam(name = "consumerWindowStates", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> consumerWindowStates, @WebParam(name = "consumerUserScopes", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> consumerUserScopes, @WebParam(name = "customUserProfileData", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> customUserProfileData, @WebParam(name = "registrationProperties", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<V1Property> registrationProperties, @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types", mode = WebParam.Mode.INOUT) Holder<List<V1Extension>> extensions, @WebParam(name = "registrationHandle", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types", mode = WebParam.Mode.OUT) Holder<String> registrationHandle, @WebParam(name = "registrationState", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types", mode = WebParam.Mode.OUT) Holder<byte[]> registrationState) throws V1MissingParameters, V1OperationFailed
      {
         V1Property prop = checkRegistrationData(registrationProperties);

         String value = prop.getStringValue();
         if (ORIGINAL_VALUE.equals(value) && PROP_NAME.equals(prop.getName()))
         {
            super.register(consumerName, consumerAgent, methodGetSupported, consumerModes, consumerWindowStates, consumerUserScopes, customUserProfileData, registrationProperties, extensions, registrationHandle, registrationState);
            return;
         }

         throw WSRP1ExceptionFactory.<V1OperationFailed, V1OperationFailedFault>throwWSException(WSRP1ExceptionFactory.OPERATION_FAILED,
            value + " is not a valid value for " + PROP_NAME, null);
      }

      @Override
      public void modifyRegistration(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1RegistrationContext registrationContext, @WebParam(name = "registrationData", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1RegistrationData registrationData, @WebParam(name = "registrationState", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types", mode = WebParam.Mode.OUT) Holder<byte[]> registrationState, @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types", mode = WebParam.Mode.OUT) Holder<List<V1Extension>> extensions) throws V1InvalidRegistration, V1MissingParameters, V1OperationFailed
      {
         incrementCallCount();

         WSRP1ExceptionFactory.throwMissingParametersIfValueIsMissing(registrationContext, "RegistrationContext", null);

         if (!RegistrationBehavior.REGISTRATION_HANDLE.equals(registrationContext.getRegistrationHandle()))
         {
            WSRP1ExceptionFactory.<V1InvalidRegistration, V1InvalidRegistrationFault>throwWSException(WSRP1ExceptionFactory.INVALID_REGISTRATION, "Invalid registration", null);
         }

         WSRP1ExceptionFactory.throwMissingParametersIfValueIsMissing(registrationData, "RegistrationData", null);

         V1Property prop = checkRegistrationData(registrationData.getRegistrationProperties());

         String value = prop.getStringValue();
         if (MODIFIED_VALUE.equals(value) && PROP_NAME.equals(prop.getName()))
         {
            return;
         }

         throw WSRP1ExceptionFactory.<V1OperationFailed, V1OperationFailedFault>throwWSException(WSRP1ExceptionFactory.OPERATION_FAILED, value
            + " is not a valid value for " + PROP_NAME, null);
      }

      private V1Property checkRegistrationData(List<V1Property> registrationProperties) throws V1OperationFailed
      {
         WSRP1ExceptionFactory.throwOperationFailedIfValueIsMissing(registrationProperties, "RegistrationData");

         V1Property prop = registrationProperties.get(0);
         WSRP1ExceptionFactory.throwOperationFailedIfValueIsMissing(prop, "Registration property");
         return prop;
      }
   }
}