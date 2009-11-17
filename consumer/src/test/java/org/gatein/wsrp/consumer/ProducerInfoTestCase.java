/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2009, Red Hat Middleware, LLC, and individual                    *
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

package org.gatein.wsrp.consumer;

import junit.framework.TestCase;
import org.gatein.pc.api.Portlet;
import org.gatein.pc.api.PortletContext;
import org.gatein.pc.api.PortletInvokerException;
import org.gatein.wsrp.WSRPExceptionFactory;
import org.gatein.wsrp.test.protocol.v1.PortletManagementBehavior;
import org.gatein.wsrp.test.protocol.v1.RegistrationBehavior;
import org.gatein.wsrp.test.protocol.v1.ServiceDescriptionBehavior;
import org.gatein.wsrp.test.support.BehaviorBackedServiceFactory;
import org.gatein.wsrp.test.support.MockConsumerRegistry;
import org.oasis.wsrp.v1.AccessDenied;
import org.oasis.wsrp.v1.Extension;
import org.oasis.wsrp.v1.InconsistentParameters;
import org.oasis.wsrp.v1.InvalidHandle;
import org.oasis.wsrp.v1.InvalidRegistration;
import org.oasis.wsrp.v1.InvalidRegistrationFault;
import org.oasis.wsrp.v1.InvalidUserCategory;
import org.oasis.wsrp.v1.MissingParameters;
import org.oasis.wsrp.v1.OperationFailed;
import org.oasis.wsrp.v1.OperationFailedFault;
import org.oasis.wsrp.v1.PortletDescription;
import org.oasis.wsrp.v1.Property;
import org.oasis.wsrp.v1.RegistrationContext;
import org.oasis.wsrp.v1.RegistrationData;
import org.oasis.wsrp.v1.ResourceList;
import org.oasis.wsrp.v1.UserContext;

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

      serviceFactory = new BehaviorBackedServiceFactory();
      EndpointConfigurationInfo eci = new EndpointConfigurationInfo(serviceFactory);
      info.setEndpointConfigurationInfo(eci);

      info.setRegistry(new MockConsumerRegistry());
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
      public void getPortletDescription(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") org.oasis.wsrp.v1.PortletContext portletContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") UserContext userContext, @WebParam(name = "desiredLocales", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> desiredLocales, @WebParam(mode = WebParam.Mode.OUT, name = "portletDescription", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<PortletDescription> portletDescription, @WebParam(mode = WebParam.Mode.OUT, name = "resourceList", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<ResourceList> resourceList, @WebParam(mode = WebParam.Mode.OUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<Extension>> extensions) throws MissingParameters, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory, AccessDenied, OperationFailed
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
      public void register(@WebParam(name = "consumerName", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") String consumerName, @WebParam(name = "consumerAgent", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") String consumerAgent, @WebParam(name = "methodGetSupported", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") boolean methodGetSupported, @WebParam(name = "consumerModes", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> consumerModes, @WebParam(name = "consumerWindowStates", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> consumerWindowStates, @WebParam(name = "consumerUserScopes", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> consumerUserScopes, @WebParam(name = "customUserProfileData", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> customUserProfileData, @WebParam(name = "registrationProperties", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<Property> registrationProperties, @WebParam(mode = WebParam.Mode.INOUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<Extension>> extensions, @WebParam(mode = WebParam.Mode.OUT, name = "registrationHandle", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<String> registrationHandle, @WebParam(mode = WebParam.Mode.OUT, name = "registrationState", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<byte[]> registrationState) throws MissingParameters, OperationFailed
      {
         Property prop = checkRegistrationData(registrationProperties);

         String value = prop.getStringValue();
         if (ORIGINAL_VALUE.equals(value) && PROP_NAME.equals(prop.getName()))
         {
            super.register(consumerName, consumerAgent, methodGetSupported, consumerModes, consumerWindowStates, consumerUserScopes, customUserProfileData, registrationProperties, extensions, registrationHandle, registrationState);
            return;
         }

         throw WSRPExceptionFactory.<OperationFailed, OperationFailedFault>throwWSException(WSRPExceptionFactory.OPERATION_FAILED,
            value + " is not a valid value for " + PROP_NAME, null);
      }

      @Override
      public void modifyRegistration(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") RegistrationContext registrationContext, @WebParam(name = "registrationData", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") RegistrationData registrationData, @WebParam(mode = WebParam.Mode.OUT, name = "registrationState", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<byte[]> registrationState, @WebParam(mode = WebParam.Mode.OUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<Extension>> extensions) throws MissingParameters, InvalidRegistration, OperationFailed
      {
         incrementCallCount();

         WSRPExceptionFactory.throwMissingParametersIfValueIsMissing(registrationContext, "RegistrationContext", null);

         if (!RegistrationBehavior.REGISTRATION_HANDLE.equals(registrationContext.getRegistrationHandle()))
         {
            WSRPExceptionFactory.<InvalidRegistration, InvalidRegistrationFault>throwWSException(WSRPExceptionFactory.INVALID_REGISTRATION, "Invalid registration", null);
         }

         WSRPExceptionFactory.throwMissingParametersIfValueIsMissing(registrationData, "RegistrationData", null);

         Property prop = checkRegistrationData(registrationData.getRegistrationProperties());

         String value = prop.getStringValue();
         if (MODIFIED_VALUE.equals(value) && PROP_NAME.equals(prop.getName()))
         {
            return;
         }

         throw WSRPExceptionFactory.<OperationFailed, OperationFailedFault>throwWSException(WSRPExceptionFactory.OPERATION_FAILED, value
            + " is not a valid value for " + PROP_NAME, null);
      }

      private Property checkRegistrationData(List<Property> registrationProperties) throws OperationFailed
      {
         WSRPExceptionFactory.throwOperationFailedIfValueIsMissing(registrationProperties, "RegistrationData");

         Property prop = registrationProperties.get(0);
         WSRPExceptionFactory.throwOperationFailedIfValueIsMissing(prop, "Registration property");
         return prop;
      }
   }
}