/*
 * JBoss, a division of Red Hat
 * Copyright 2012, Red Hat Middleware, LLC, and individual
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
import org.gatein.pc.api.PortletInvokerException;
import org.gatein.wsrp.handler.RequestHeaderClientHandler;
import org.gatein.wsrp.spec.v2.WSRP2Constants;
import org.gatein.wsrp.spec.v2.WSRP2ExceptionFactory;
import org.gatein.wsrp.test.protocol.v2.BehaviorBackedServiceFactory;
import org.gatein.wsrp.test.protocol.v2.PortletManagementBehavior;
import org.gatein.wsrp.test.protocol.v2.RegistrationBehavior;
import org.gatein.wsrp.test.protocol.v2.ServiceDescriptionBehavior;
import org.gatein.wsrp.test.protocol.v2.behaviors.GroupedPortletsServiceDescriptionBehavior;
import org.gatein.wsrp.test.protocol.v2.behaviors.SupportedOptionsServiceDescriptionBehavior;
import org.gatein.wsrp.test.support.MockConsumerRegistry;
import org.oasis.wsrp.v2.AccessDenied;
import org.oasis.wsrp.v2.CookieProtocol;
import org.oasis.wsrp.v2.Extension;
import org.oasis.wsrp.v2.InconsistentParameters;
import org.oasis.wsrp.v2.InvalidHandle;
import org.oasis.wsrp.v2.InvalidRegistration;
import org.oasis.wsrp.v2.InvalidUserCategory;
import org.oasis.wsrp.v2.Lifetime;
import org.oasis.wsrp.v2.MissingParameters;
import org.oasis.wsrp.v2.ModifyRegistrationRequired;
import org.oasis.wsrp.v2.OperationFailed;
import org.oasis.wsrp.v2.OperationNotSupported;
import org.oasis.wsrp.v2.PortletContext;
import org.oasis.wsrp.v2.PortletDescription;
import org.oasis.wsrp.v2.Property;
import org.oasis.wsrp.v2.RegistrationContext;
import org.oasis.wsrp.v2.RegistrationData;
import org.oasis.wsrp.v2.ResourceList;
import org.oasis.wsrp.v2.ResourceSuspended;
import org.oasis.wsrp.v2.UserContext;

import javax.jws.WebParam;
import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import java.util.ArrayList;
import java.util.Collection;
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
      info = new ProducerInfo(new MockConsumerRegistry());
      info.setId("test");
      info.setKey("key");

      serviceFactory = new BehaviorBackedServiceFactory();
      EndpointConfigurationInfo eci = new EndpointConfigurationInfo(serviceFactory);
      info.setEndpointConfigurationInfo(eci);

      // make sure we reset any held session information
      RequestHeaderClientHandler.resetCurrentInfo();
   }

   public void testSettersWithoutModificationShouldNotChangeLastModified()
   {
      final long initial = info.getLastModified();

      info.setActive(info.isActive());
      checkLastModifiedHasNotChangedSince(initial);

      info.setActiveAndSave(info.isActive());
      checkLastModifiedHasNotChangedSince(initial);

      info.setExpirationCacheSeconds(info.getExpirationCacheSeconds());
      checkLastModifiedHasNotChangedSince(initial);

      info.setId(info.getId());
      checkLastModifiedHasNotChangedSince(initial);

      info.setModifyRegistrationRequired(info.isModifyRegistrationRequired());
      checkLastModifiedHasNotChangedSince(initial);
   }

   private void checkLastModifiedHasNotChangedSince(long initial)
   {
      assertEquals(initial, info.getLastModified());
   }

   public void testSettersWithModificationShouldChangeLastModified() throws InterruptedException
   {
      long initial = info.getLastModified();
      Thread.sleep(10); // to allow for System.currentTimeMillis() to catch up
      info.setActive(!info.isActive());
      checkLastModifiedIsLaterThan(initial);

      initial = info.getLastModified();
      Thread.sleep(10); // to allow for System.currentTimeMillis() to catch up
      info.setActiveAndSave(!info.isActive());
      checkLastModifiedIsLaterThan(initial);

      initial = info.getLastModified();
      Thread.sleep(10); // to allow for System.currentTimeMillis() to catch up
      info.setExpirationCacheSeconds(info.getExpirationCacheSeconds() + 1);
      checkLastModifiedIsLaterThan(initial);

      initial = info.getLastModified();
      Thread.sleep(10); // to allow for System.currentTimeMillis() to catch up
      info.setId(info.getId() + "other");
      checkLastModifiedIsLaterThan(initial);

      initial = info.getLastModified();
      Thread.sleep(10); // to allow for System.currentTimeMillis() to catch up
      info.setModifyRegistrationRequired(!info.isModifyRegistrationRequired());
      checkLastModifiedIsLaterThan(initial);
   }

   private void checkLastModifiedIsLaterThan(long initial)
   {
      assertTrue(initial < info.getLastModified());
   }

   public void testSetKeyDoesNotChangeLastModified()
   {
      long initial = info.getLastModified();
      info.setKey(info.getKey());
      checkLastModifiedHasNotChangedSince(initial);

      initial = info.getLastModified();
      info.setKey(info.getKey() + "other");
      checkLastModifiedHasNotChangedSince(initial);
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

   public void testRefreshDidNotHappenIfFailure() throws PortletInvokerException
   {
      serviceFactory.setFailed(true);
      final boolean refresh = info.refresh(false);
      assertFalse(refresh);
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

   public void testSetNullCache() throws PortletInvokerException
   {
      ServiceDescriptionBehavior behavior = new ServiceDescriptionBehavior();
      serviceFactory.getRegistry().setServiceDescriptionBehavior(behavior);

      // we now have a default value for cache
      assertEquals(ProducerInfo.DEFAULT_CACHE_VALUE, info.getExpirationCacheSeconds());

      // check behavior when no cache has been set
      info.setExpirationCacheSeconds(null);
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
   }

   public void testSetNegativeCache() throws PortletInvokerException
   {
      ServiceDescriptionBehavior behavior = new ServiceDescriptionBehavior();
      serviceFactory.getRegistry().setServiceDescriptionBehavior(behavior);

      // we now have a default value for cache
      assertEquals(ProducerInfo.DEFAULT_CACHE_VALUE, info.getExpirationCacheSeconds());

      // check behavior when cache has been set to a negative value
      info.setExpirationCacheSeconds(-100);
      assertEquals(new Integer(-100), info.getExpirationCacheSeconds());
      assertTrue(info.isRefreshNeeded(false));
      assertFalse(info.isRegistrationChecked());
      assertTrue(info.refresh(false));
      assertFalse(info.isRefreshNeeded(false));
      assertTrue(info.isRegistrationChecked());
      assertTrue(info.refresh(false));
      assertFalse(info.isRefreshNeeded(false));
      assertTrue(info.isRegistrationChecked());
      assertEquals(2, behavior.getCallCount());
   }

   public void testSetZeroCache() throws PortletInvokerException
   {
      ServiceDescriptionBehavior behavior = new ServiceDescriptionBehavior();
      serviceFactory.getRegistry().setServiceDescriptionBehavior(behavior);

      // we now have a default value for cache
      assertEquals(ProducerInfo.DEFAULT_CACHE_VALUE, info.getExpirationCacheSeconds());

      // check behavior when cache has been set to zero
      info.setExpirationCacheSeconds(0);
      assertEquals(new Integer(0), info.getExpirationCacheSeconds());
      assertTrue(info.isRefreshNeeded(false));
      assertFalse(info.isRegistrationChecked());
      assertTrue(info.refresh(false));
      assertFalse(info.isRefreshNeeded(false));
      assertTrue(info.isRegistrationChecked());
      assertTrue(info.refresh(false));
      assertFalse(info.isRefreshNeeded(false));
      assertTrue(info.isRegistrationChecked());
      assertEquals(2, behavior.getCallCount());
   }

   public void testCacheTransitions() throws Exception
   {
      ServiceDescriptionBehavior behavior = new ServiceDescriptionBehavior();
      serviceFactory.getRegistry().setServiceDescriptionBehavior(behavior);

      // we now have a default value for cache
      assertEquals(ProducerInfo.DEFAULT_CACHE_VALUE, info.getExpirationCacheSeconds());

      // check behavior when no cache has been set
      info.setExpirationCacheSeconds(null);
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

      // wait a little so that computations that are only precise to the ms can actually mean something
      Thread.sleep(10);

      // set cache and check that we don't refresh as much
      info.setExpirationCacheSeconds(1);
      assertEquals(new Integer(1), info.getExpirationCacheSeconds());
      assertFalse("we refreshed less than a second ago so we don't need to refresh again", info.refresh(false));
      assertFalse(info.isRefreshNeeded(false));
      assertTrue(info.isRegistrationChecked());
      assertEquals(2, behavior.getCallCount());

      // wait for cache expiration
      Thread.sleep(1100);
      assertFalse("refresh is not needed if cache is not considered", info.isRefreshNeeded(false));
      assertTrue("refresh is needed if cache is considered since it has expired", info.isRefreshNeeded(true));
      assertTrue(info.refresh(false));
      assertFalse("Was just refreshed so refresh is not needed even considering cache", info.isRefreshNeeded(true));
      assertTrue(info.isRegistrationChecked());
      assertFalse(info.refresh(false));
      assertTrue(info.refresh(true));
      assertFalse(info.isRefreshNeeded(false));
      assertTrue(info.isRegistrationChecked());
      assertEquals(4, behavior.getCallCount());

      // wait a little so that computations that are only precise to the ms can actually mean something
      Thread.sleep(10);

      // now ask to not use the cache anymore and check that we do refresh
      info.setExpirationCacheSeconds(0);
      assertEquals(new Integer(0), info.getExpirationCacheSeconds());
      assertTrue(info.refresh(false));
      assertTrue(info.refresh(false));
      assertFalse("since we've been refreshed at least once before, refreshing the endpoint and registration has been done so refresh is not needed", info.isRefreshNeeded(false));
      assertTrue(info.isRefreshNeeded(true));
      assertTrue(info.isRegistrationChecked());
      assertEquals(6, behavior.getCallCount());
   }

   public void testGetPortlet() throws Exception
   {
      ServiceDescriptionBehavior behavior = new ServiceDescriptionBehavior();
      behavior.addPortletDescription(behavior.createPortletDescription("test", null));
      serviceFactory.getRegistry().setServiceDescriptionBehavior(behavior);

      // activate caching for this test so that we can simulate portlet deployment on the producer with a cached SD
      info.setExpirationCacheSeconds(1000);

      org.gatein.pc.api.PortletContext portletContext = org.gatein.pc.api.PortletContext.createPortletContext("test", false);
      Portlet portlet = info.getPortlet(portletContext);
      assertNotNull(portlet);
      assertEquals(portletContext, portlet.getContext());
      assertEquals(1, behavior.getCallCount());

      // test2 is not in the service description, so it should be looked up via Portlet Management...
      portletContext = org.gatein.pc.api.PortletContext.createPortletContext(TestPortletManagementBehavior.HANDLE_FOR_GET_DESCRIPTION, false);
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
      final String handle = "test3";
      behavior.addPortletDescription(behavior.createPortletDescription(handle, null));
      portletContext = org.gatein.pc.api.PortletContext.createPortletContext(handle, false);
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

   public void testGetInfoForEvent()
   {
      assertNull(info.getInfoForEvent(null));
   }

   public void testRequiresInitCookieIsProperlySet() throws PortletInvokerException
   {
      ServiceDescriptionBehavior sdb = new GroupedPortletsServiceDescriptionBehavior(new ArrayList<PortletDescription>(3));
      sdb.setRequiresInitCookie(CookieProtocol.PER_GROUP);
      serviceFactory.getRegistry().setServiceDescriptionBehavior(sdb);

      info.refresh(false);

      assertEquals(CookieProtocol.PER_GROUP, info.getRequiresInitCookie());
   }

   public void testSupportedOptions() throws PortletInvokerException
   {
      ServiceDescriptionBehavior sdb = new SupportedOptionsServiceDescriptionBehavior();
      serviceFactory.getRegistry().setServiceDescriptionBehavior(sdb);

      info.refresh(false);

      final Collection<String> supportedOptions = info.getSupportedOptions();
      assertEquals(2, supportedOptions.size());
      assertTrue(supportedOptions.contains(WSRP2Constants.OPTIONS_EXPORT));
      assertTrue(supportedOptions.contains(WSRP2Constants.OPTIONS_IMPORT));
   }

   private static class TestPortletManagementBehavior extends PortletManagementBehavior
   {

      public static final String HANDLE_FOR_GET_DESCRIPTION = "test2";

      @Override
      public void getPortletDescription(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") PortletContext portletContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext, @WebParam(name = "desiredLocales", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") List<String> desiredLocales, @WebParam(name = "portletDescription", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<PortletDescription> portletDescription, @WebParam(name = "resourceList", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<ResourceList> resourceList, @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions) throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
      {
         super.getPortletDescription(registrationContext, portletContext, userContext, desiredLocales, portletDescription, resourceList, extensions);
         portletDescription.value = createPortletDescription(HANDLE_FOR_GET_DESCRIPTION, null);
      }
   }

   private static class TestRegistrationBehavior extends RegistrationBehavior
   {
      private static final QName PROP_NAME = QName.valueOf("prop0");
      private static final String MODIFIED_VALUE = "value2";
      private static final String ORIGINAL_VALUE = "value";

      @Override
      public void register(@WebParam(name = "registrationData", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationData registrationData, @WebParam(name = "lifetime", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") Lifetime lifetime, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext, @WebParam(name = "registrationState", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<byte[]> registrationState, @WebParam(name = "scheduledDestruction", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<Lifetime> scheduledDestruction, @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions, @WebParam(name = "registrationHandle", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<String> registrationHandle) throws MissingParameters, OperationFailed, OperationNotSupported
      {
         Property prop = checkRegistrationData(registrationData.getRegistrationProperties());

         String value = prop.getStringValue();
         if (ORIGINAL_VALUE.equals(value) && PROP_NAME.equals(prop.getName()))
         {
            super.register(registrationData, lifetime, userContext, registrationState, scheduledDestruction, extensions, registrationHandle);
            return;
         }

         throw WSRP2ExceptionFactory.throwWSException(OperationFailed.class, value + " is not a valid value for " + PROP_NAME, null);
      }

      @Override
      public void modifyRegistration(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext, @WebParam(name = "registrationData", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationData registrationData, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext, @WebParam(name = "registrationState", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<byte[]> registrationState, @WebParam(name = "scheduledDestruction", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<Lifetime> scheduledDestruction, @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions) throws InvalidRegistration, MissingParameters, OperationFailed, OperationNotSupported, ResourceSuspended
      {
         incrementCallCount();

         WSRP2ExceptionFactory.throwMissingParametersIfValueIsMissing(registrationContext, "RegistrationContext", null);

         if (!RegistrationBehavior.REGISTRATION_HANDLE.equals(registrationContext.getRegistrationHandle()))
         {
            WSRP2ExceptionFactory.throwWSException(InvalidRegistration.class, "Invalid registration", null);
         }

         WSRP2ExceptionFactory.throwMissingParametersIfValueIsMissing(registrationData, "RegistrationData", null);

         Property prop = checkRegistrationData(registrationData.getRegistrationProperties());

         String value = prop.getStringValue();
         if (MODIFIED_VALUE.equals(value) && PROP_NAME.equals(prop.getName()))
         {
            return;
         }

         throw WSRP2ExceptionFactory.throwWSException(OperationFailed.class, value
            + " is not a valid value for " + PROP_NAME, null);

      }

      private Property checkRegistrationData(List<Property> registrationProperties) throws OperationFailed
      {
         WSRP2ExceptionFactory.throwOperationFailedIfValueIsMissing(registrationProperties, "RegistrationData");

         Property prop = registrationProperties.get(0);
         WSRP2ExceptionFactory.throwOperationFailedIfValueIsMissing(prop, "Registration property");
         return prop;
      }
   }
}