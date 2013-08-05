/*
 * JBoss, a division of Red Hat
 * Copyright 2011, Red Hat Middleware, LLC, and individual
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
import org.gatein.common.util.Version;
import org.gatein.wsrp.WSRPConstants;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.services.ServiceFactory;
import org.gatein.wsrp.test.protocol.v2.ServiceDescriptionBehavior;
import org.oasis.wsrp.v2.Property;
import org.oasis.wsrp.v2.RegistrationContext;
import org.oasis.wsrp.v2.RegistrationData;
import org.oasis.wsrp.v2.ServiceDescription;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 11967 $
 * @since 2.6
 */
public class RegistrationInfoTestCase extends TestCase
{
   private RegistrationInfo info;
   private static final String producerId = "test";
   private static final RegistrationContext FAKE_REGISTRATION_CONTEXT = WSRPTypeFactory.createRegistrationContext("handle");

   protected void setUp() throws Exception
   {
      ProducerInfo pi = new ProducerInfo(null);
      info = pi.getRegistrationInfo();
      // to avoid having the endpoint try to refresh when the registration info wants the WSRP version :(
      pi.setEndpointConfigurationInfo(new EndpointConfigurationInfo()
      {
         @Override
         Version getWSRPVersion()
         {
            return ServiceFactory.WSRP2;
         }
      });
   }

   public void testInitialState()
   {
      assertTrue(info.getRegistrationProperties().isEmpty());
      assertTrue(info.getRegistrationPropertyNames().isEmpty());
      assertNull(info.getRegistrationContext());
      assertNotNull(info.getRegistrationData());
      assertNull(info.getRegistrationHandle());
      assertNull(info.getRegistrationState());

      // before refresh, refresh is needed...
      assertTrue(info.isRefreshNeeded());
      // we don't know if the the local info is consistent with the producer expectations...
      assertNull(info.isConsistentWithProducerExpectations());
      // we don't know if the registration is required...
      assertNull(info.isRegistrationRequired());
      // and we don't know if the registration is valid
      assertNull(info.isRegistrationValid());
      // and we are not registered
      assertFalse(info.isRegistered());

      assertFalse(info.isModifiedSinceLastRefresh());
      assertFalse(info.isModifyRegistrationNeeded());

      try
      {
         info.isRegistrationDeterminedNotRequired();
         fail("refresh hasn't been called, isRegistrationDeterminedNotRequired should fail");
      }
      catch (IllegalStateException expected)
      {
      }

      try
      {
         info.isRegistrationDeterminedRequired();
         fail("refresh hasn't been called, isRegistrationDeterminedRequired should fail");
      }
      catch (IllegalStateException expected)
      {
      }
   }

   public void testRegistration()
   {
      register();

      assertEquals(FAKE_REGISTRATION_CONTEXT.getRegistrationHandle(), info.getRegistrationHandle());
      assertEquals(FAKE_REGISTRATION_CONTEXT.getRegistrationState(), info.getRegistrationState());

      assertTrue(info.isRegistered());
      assertFalse(info.isModifiedSinceLastRefresh());
      assertFalse(info.isModifyRegistrationNeeded());
      assertTrue(info.isConsistentWithProducerExpectations());

      assertFalse(info.isRefreshNeeded());

      assertTrue(info.isRegistrationRequired());
      assertTrue(info.isRegistrationValid());
      assertFalse(info.isRegistrationDeterminedNotRequired());
      assertTrue(info.isRegistrationDeterminedRequired());
      assertFalse(info.isUndetermined());
   }

   public void testIsModifyRegistrationNeeded()
   {
      String key = "foo";

      // if we're not registered, then modifying registration is never needed
      assertFalse(info.isRegistered());
      info.setRegistrationPropertyValue(key, "bar");
      assertFalse(info.isModifyRegistrationNeeded());

      register();

      // if we try to set the property to the same value, nothing should change
      info.setRegistrationPropertyValue(key, "bar");
      assertFalse(info.isModifiedSinceLastRefresh());
      assertFalse(info.isModifyRegistrationNeeded());

      // but if we set the property to a different value, then we need to modify the registration
      info.setRegistrationPropertyValue(key, "new");
      assertTrue(info.isModifiedSinceLastRefresh());
      assertTrue(info.isModifyRegistrationNeeded());
   }

   private void register()
   {
      // setting a registration context should simulate a successful registration
      info.setRegistrationContext(FAKE_REGISTRATION_CONTEXT);
      info.setConsumerName(WSRPConstants.DEFAULT_CONSUMER_NAME);
   }

   public void testSimpleSetGetRegistrationProperty()
   {
      QName key = QName.valueOf("{urn:foo:ns}foo");
      info.setRegistrationPropertyValue(key, "bar");

      // check status
      assertNull(info.isConsistentWithProducerExpectations());
      assertTrue(info.isModifiedSinceLastRefresh());
      assertFalse(info.isModifyRegistrationNeeded()); // since we were not registered, modification of registration shouldn't be needed

      Map properties = info.getRegistrationProperties();
      assertFalse(properties.isEmpty());
      Set names = info.getRegistrationPropertyNames();
      assertFalse(names.isEmpty());
      assertTrue(properties.containsKey(key));
      assertTrue(names.contains(key));
      Object prop = properties.get(key);
      assertNotNull(prop);
      RegistrationProperty registrationProperty = info.getRegistrationProperty(key);
      assertEquals(prop, registrationProperty);
      assertEquals("bar", registrationProperty.getValue());
   }

   public void testRegistrationPropertiesAndRefresh()
   {
      info.setRegistrationPropertyValue("prop0", "value0");
      RefreshResult result = info.refresh(createServiceDescription(true, 1), producerId, true, false, false);
      RegistrationProperty prop = info.getRegistrationProperty("prop0");
      assertNull(prop.isInvalid());
      assertFalse(result.hasIssues());
      assertFalse(info.isModifiedSinceLastRefresh());
      assertFalse(info.isModifyRegistrationNeeded());
      assertTrue(info.isConsistentWithProducerExpectations());

      // specifiy that the prop is valid to simulate a successful registration (integration test, should have something
      // testing that in ProducerInfoTestCase)
      prop.setStatus(RegistrationProperty.Status.VALID);

      info.setRegistrationPropertyValue("prop0", "value1");
      assertTrue(info.isRefreshNeeded());
      assertNull("Property value has changed since last refresh, status should be unknown", prop.isInvalid());
      assertEquals("Property value has changed since last refresh, status should be unknown",
         RegistrationProperty.Status.UNCHECKED_VALUE, prop.getStatus());
      assertTrue(info.isModifiedSinceLastRefresh());
      assertTrue(info.isModifyRegistrationNeeded());
   }

   public void testRefreshNoRegistration()
   {
      // no registration expected
      ServiceDescription sd = createServiceDescription(false, 0);
      RefreshResult result = info.refresh(sd, producerId, true, false, false);
      assertNotNull(result);
      assertFalse(result.hasIssues());
      assertTrue(info.isConsistentWithProducerExpectations());
      assertFalse(info.isRegistrationRequired());
      assertTrue(info.isRegistrationDeterminedNotRequired());
      assertFalse(info.isRegistrationDeterminedRequired());
      assertTrue(info.isRegistrationValid());
      assertFalse(info.isModifyRegistrationNeeded());

      result = info.refresh(sd, producerId, false, false, false);
      assertNotNull(result);
      assertFalse(result.hasIssues());
      assertTrue(info.isConsistentWithProducerExpectations());
      assertFalse(info.isRegistrationRequired());
      assertTrue(info.isRegistrationDeterminedNotRequired());
      assertFalse(info.isRegistrationDeterminedRequired());
      assertTrue(info.isRegistrationValid());
      assertFalse(info.isModifyRegistrationNeeded());
   }

   public void testRefreshRegistrationDefaultRegistrationNoLocalInfo()
   {
      // before refresh registration status is undetermined
      assertNull(info.isRegistrationRequired());
      assertNull(info.isRegistrationValid());

      RegistrationInfo.RegistrationRefreshResult result = info.refresh(
         createServiceDescription(true, 0), producerId, true, false, false);
      assertNotNull(result);
      assertFalse(result.hasIssues());
      assertTrue(info.isRegistrationRequired());
      assertTrue(info.isRegistrationDeterminedRequired());
      assertFalse(info.isRegistrationDeterminedNotRequired());
      assertFalse(info.isRegistrationValid());
   }

   public void testRefreshRegistrationDefaultRegistrationExtraLocalInfo()
   {
      // set a registration property
      info.setRegistrationPropertyValue("foo", "bar");

      // we were not registered so this is a failure and not a need to call modifyRegistration
      RegistrationInfo.RegistrationRefreshResult result = info.refresh(
         createServiceDescription(true, 0), producerId, false, false, true);
      assertNotNull(result);
      assertTrue(result.hasIssues());
      assertEquals(RefreshResult.Status.FAILURE, result.getStatus());
      assertEquals(1, info.getRegistrationProperties().size());
      assertEquals(1, result.getRegistrationProperties().size());
      assertFalse(info.isModifyRegistrationNeeded());

      checkProperties(result);
   }

   private void checkProperties(RegistrationInfo.RegistrationRefreshResult result)
   {
      Map<QName, RegistrationProperty> regProps = result.getRegistrationProperties();
      assertNotNull(regProps);

      RegistrationProperty prop = regProps.get(QName.valueOf("foo"));
      assertNotNull(prop);
      assertEquals("bar", prop.getValue());
      assertTrue(prop.isInvalid());
      assertEquals(RegistrationProperty.Status.INEXISTENT, prop.getStatus());
   }

   public void testRefreshRegistrationDefaultRegistrationExtraLocalInfoWhileRegistered()
   {
      register();

      // set a registration property
      info.setRegistrationPropertyValue("foo", "bar");

      // we were registered so we need to call modifyRegistration, force check of extra props
      RegistrationInfo.RegistrationRefreshResult result = info.refresh(
         createServiceDescription(true, 0), producerId, false, false, true);
      assertNotNull(result);
      assertEquals(RefreshResult.Status.MODIFY_REGISTRATION_REQUIRED, result.getStatus());
      assertTrue(result.hasIssues());
      assertEquals(1, info.getRegistrationProperties().size());
      assertEquals(1, result.getRegistrationProperties().size());
      assertTrue(info.isModifyRegistrationNeeded());

      checkProperties(result);
   }

   public void testRefreshRegistrationRegistrationNoLocalInfo()
   {
      // producer requests 2 registration properties
      ServiceDescription sd = createServiceDescription(true, 2);

      RegistrationInfo.RegistrationRefreshResult result = info.refresh(sd, producerId, false, false, false);
      assertNotNull(result);
      assertTrue(result.hasIssues());
      assertEquals(RefreshResult.Status.FAILURE, result.getStatus());
      assertEquals(0, info.getRegistrationProperties().size());
      assertEquals(2, result.getRegistrationProperties().size());
      assertFalse(info.isModifyRegistrationNeeded()); // we weren't registered

      Map<QName, RegistrationProperty> regProps = result.getRegistrationProperties();
      assertNotNull(regProps);

      RegistrationProperty prop = regProps.get(QName.valueOf("prop0"));
      assertNotNull(prop);
      assertTrue(prop.isInvalid());
      assertEquals(RegistrationProperty.Status.MISSING, prop.getStatus());
      prop = regProps.get(QName.valueOf("prop1"));
      assertNotNull(prop);
      assertTrue(prop.isInvalid());
      assertEquals(RegistrationProperty.Status.MISSING, prop.getStatus());
   }

   public void testRefreshRegistrationRegistrationNoLocalInfoWhileRegistered()
   {
      register();

      // producer requests 2 registration properties
      ServiceDescription sd = createServiceDescription(true, 2);

      RegistrationInfo.RegistrationRefreshResult result = info.refresh(sd, producerId, false, true, false);
      assertNotNull(result);
      assertTrue(result.hasIssues());
      assertEquals(RefreshResult.Status.MODIFY_REGISTRATION_REQUIRED, result.getStatus());
      assertEquals(0, info.getRegistrationProperties().size());
      assertEquals(2, result.getRegistrationProperties().size());
      assertTrue(info.isModifyRegistrationNeeded());

      Map<QName, RegistrationProperty> regProps = result.getRegistrationProperties();
      assertNotNull(regProps);

      RegistrationProperty prop = regProps.get(QName.valueOf("prop0"));
      assertNotNull(prop);
      assertTrue(prop.isInvalid());
      assertEquals(RegistrationProperty.Status.MISSING, prop.getStatus());
      prop = regProps.get(QName.valueOf("prop1"));
      assertNotNull(prop);
      assertTrue(prop.isInvalid());
      assertEquals(RegistrationProperty.Status.MISSING, prop.getStatus());
   }

   public void testRefreshRegistrationRegistrationMergeWithLocalInfo()
   {
      info.setRegistrationPropertyValue("foo", "bar");

      RegistrationInfo.RegistrationRefreshResult result = info.refresh(createServiceDescription(true, 2),
         producerId, true, false, false);
      assertNotNull(result);
      assertTrue(result.hasIssues());

      RegistrationProperty prop = info.getRegistrationProperty("prop0");
      assertNotNull(prop);
      assertNull(prop.getValue());
      assertTrue(prop.isInvalid());
      assertEquals(RegistrationProperty.Status.MISSING_VALUE, prop.getStatus());

      prop = info.getRegistrationProperty("prop1");
      assertNotNull(prop);
      assertNull(prop.getValue());
      assertTrue(prop.isInvalid());
      assertEquals(RegistrationProperty.Status.MISSING_VALUE, prop.getStatus());
      assertEquals(2, info.getRegistrationProperties().size());
      assertEquals(2, result.getRegistrationProperties().size());

      assertNull(info.getRegistrationProperty("foo"));
   }

   public void testForceRefreshRegistration()
   {
      //
      RefreshResult result = info.refresh(createServiceDescription(true, 0), producerId, false, false, false);
      assertNotNull(result);
      assertFalse(result.hasIssues());
      assertFalse(info.isRegistrationValid());

      // Modifying a property renders the info dirty and hence should be refreshed
      info.setRegistrationPropertyValue("foo", "bar");
      result = info.refresh(createServiceDescription(true, 0), producerId, false, false, false);
      assertTrue(result.hasIssues());
      assertFalse(info.isRegistrationValid());

      info.removeRegistrationProperty("foo");
      result = info.refresh(createServiceDescription(true, 0), producerId, false, false, false);
      assertFalse(result.hasIssues());
      assertFalse(info.isRegistrationValid());

      // producer has changed but we're not forcing refresh so registration should still be invalid
      result = info.refresh(createServiceDescription(false, 0), producerId, false, false, false);
      assertFalse(result.hasIssues());
      assertFalse(info.isRegistrationValid());

      // force refresh, registration should now be valid
      result = info.refresh(createServiceDescription(false, 0), producerId, false, true, false);
      assertFalse(result.hasIssues());
      assertTrue(info.isRegistrationValid());
   }

   public void testSetRegistrationContext()
   {
      assertNull(info.isConsistentWithProducerExpectations());
      assertNull(info.getRegistrationHandle());

      String registrationHandle = "registrationHandle";
      info.setRegistrationContext(WSRPTypeFactory.createRegistrationContext(registrationHandle));
      RegistrationContext registrationContext = info.getRegistrationContext();
      assertNotNull(registrationContext);
      assertEquals(registrationHandle, registrationContext.getRegistrationHandle());
      assertNull(registrationContext.getRegistrationState());
      assertTrue(info.isConsistentWithProducerExpectations());
      assertTrue(info.isRegistrationValid());
      assertFalse(info.isRefreshNeeded());
      assertTrue(info.isRegistrationRequired());
      assertTrue(info.isRegistrationDeterminedRequired());
      assertFalse(info.isRegistrationDeterminedNotRequired());
      assertFalse(info.isModifyRegistrationNeeded());
   }

   public void testGetRegistrationData()
   {
      assertNotNull(info.getRegistrationData());
      assertFalse(info.isModifiedSinceLastRefresh());
      assertFalse(info.isModifyRegistrationNeeded());

      info.setRegistrationPropertyValue("prop0", "value0");
      assertTrue(info.isModifiedSinceLastRefresh());
      assertFalse(info.isModifyRegistrationNeeded()); // not registered, so modifying registration is not needed

      RegistrationData registrationData = info.getRegistrationData();
      checkRegistrationData(registrationData, "value0");

      // check that setRegistrationValidInternalState properly updates RegistrationData if required
      info.setRegistrationPropertyValue("prop0", "value1");
      assertTrue(info.isModifiedSinceLastRefresh());
      assertFalse(info.isModifyRegistrationNeeded());

      register();

      assertFalse(info.isModifiedSinceLastRefresh());
      assertFalse(info.isModifyRegistrationNeeded());
      List<Property> properties = info.getRegistrationData().getRegistrationProperties();
      assertEquals("value1", properties.get(0).getStringValue());
   }

   public void testGetRegistrationDataWithInitialWrongValue()
   {
      info.setRegistrationPropertyValue("prop0", "incorrect");
      info.refresh(createServiceDescription(true, 1), producerId, true, true, false);
      checkRegistrationData(info.getRegistrationData(), "incorrect");

      info.setRegistrationPropertyValue("prop0", "value0");
      RefreshResult res = info.refresh(createServiceDescription(true, 1), producerId, true, true, false);
      assertFalse(res.hasIssues());
      checkRegistrationData(info.getRegistrationData(), "value0");
   }

   public void testRefreshWhileRegisteredAndProducerNotSendingPropertyDescriptions()
   {
      info.setRegistrationPropertyValue("prop0", "value0");
      info.refresh(createServiceDescription(true, 1), producerId, true, true, false);

      // simulate successful registration
      info.setRegistrationContext(FAKE_REGISTRATION_CONTEXT);

      assertTrue(info.isRegistrationRequired());
      assertTrue(info.isRegistrationValid());

      ServiceDescription description = createServiceDescription(true, 0);
      info.refresh(description, producerId, true, true, false);
      assertTrue(info.isRegistrationValid());
      RegistrationProperty prop = info.getRegistrationProperty("prop0");
      assertNotNull(prop);
      assertFalse(prop.isInvalid());

      // check that forcing check of extra properties work
      info.refresh(description, producerId, true, true, true);
      assertFalse(info.isRegistrationValid());
      assertNull(info.getRegistrationProperty("prop0"));
   }

   public void testRefreshRegisteredWithoutPropsAndProducerNowSendsProps()
   {
      // simulate successful registration
      info.setRegistrationContext(FAKE_REGISTRATION_CONTEXT);
      assertTrue(info.isRegistrationRequired());
      assertTrue(info.isRegistrationValid());

      // producer now requires a registration property
      info.refresh(createServiceDescription(true, 1), producerId, true, true, true);
      assertTrue(info.isRegistrationRequired());
      assertTrue(info.isRegistrationDeterminedRequired());
      assertFalse(info.isRegistrationValid());
      assertFalse(info.isConsistentWithProducerExpectations());
      assertFalse(info.isModifiedSinceLastRefresh());
   }

   private void checkRegistrationData(RegistrationData registrationData, String prop0Value)
   {
      assertNotNull(registrationData);
      List<Property> properties = registrationData.getRegistrationProperties();
      assertNotNull(properties);
      assertEquals(1, properties.size());
      Property property = properties.get(0);
      assertEquals("prop0", property.getName().toString());
      assertEquals(prop0Value, property.getStringValue());
   }

   private ServiceDescription createServiceDescription(boolean requiresRegistration, int numberOfProperties)
   {
      return ServiceDescriptionBehavior.createServiceDescription(requiresRegistration, numberOfProperties);
   }
}
