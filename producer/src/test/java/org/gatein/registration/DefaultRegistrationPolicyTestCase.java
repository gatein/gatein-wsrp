/*
 * Copyright (c) 2007, Your Corporation. All Rights Reserved.
 */

package org.gatein.registration;

import junit.framework.TestCase;
import org.gatein.registration.impl.RegistrationManagerImpl;
import org.gatein.registration.impl.RegistrationPersistenceManagerImpl;
import org.gatein.registration.policies.DefaultRegistrationPolicy;
import org.gatein.registration.policies.DefaultRegistrationPropertyValidator;
import org.gatein.wsrp.registration.PropertyDescription;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 9180 $
 * @since 2.6.3
 */
public class DefaultRegistrationPolicyTestCase extends TestCase
{
   DefaultRegistrationPolicy policy;
   Map<QName, Object> registrationProperties;
   Map<QName, PropertyDescription> expectations;
   private static final String CONSUMER = "consumer";
   private static final QName PROP1 = new QName("prop1");
   private static final QName PROP2 = new QName("prop2");
   private static final QName PROP3 = new QName("prop3");

   @Override
   protected void setUp() throws Exception
   {
      policy = new DefaultRegistrationPolicy();

      policy.setValidator(new DefaultRegistrationPropertyValidator());

      RegistrationManager manager = new RegistrationManagerImpl();
      manager.setPolicy(policy);
      manager.setPersistenceManager(new RegistrationPersistenceManagerImpl());
      manager.createConsumer(CONSUMER);

      policy.setManager(manager);

      registrationProperties = new HashMap<QName, Object>();
      registrationProperties.put(PROP1, "value1");
      registrationProperties.put(PROP2, "value2");

      expectations = new HashMap<QName, PropertyDescription>();
      policy.setExpectations(expectations);
   }

   public void testValidateRegistrationDataForNull() throws RegistrationException
   {
      try
      {
         policy.validateRegistrationDataFor(null, "foo");
         fail("null data cannot be validated");
      }
      catch (IllegalArgumentException e)
      {
         // expected
      }

      try
      {
         policy.validateRegistrationDataFor(Collections.<QName, Object>emptyMap(), null);
         fail("null data cannot be validated");
      }
      catch (IllegalArgumentException e)
      {
         // expected
      }
   }

   public void testValidateRegistrationDataForInexistentConsumer()
   {
      try
      {
         policy.validateRegistrationDataFor(Collections.<QName, Object>emptyMap(), "foo");
      }
      catch (RegistrationException e)
      {
         fail("Should be possible to validate information for inexistent consumer (otherwise, how would we register!)");
      }
   }

   public void testValidateRegistrationDataMissingProps()
   {
      expectations.put(PROP1, new TestPropertyDescription(PROP1));
      expectations.put(PROP2, new TestPropertyDescription(PROP2));
      expectations.put(PROP3, new TestPropertyDescription(PROP3));

      try
      {
         policy.validateRegistrationDataFor(registrationProperties, CONSUMER);
         fail("Missing prop3 should have been detected");
      }
      catch (RegistrationException e)
      {
         assertTrue(e.getLocalizedMessage().contains("prop3"));
      }
   }

   public void testValidateRegistrationDataExtraProps()
   {
      expectations.put(PROP1, new TestPropertyDescription(PROP1));

      try
      {
         policy.validateRegistrationDataFor(registrationProperties, CONSUMER);
         fail("Extra prop2 should have been detected");
      }
      catch (RegistrationException e)
      {
         assertTrue(e.getLocalizedMessage().contains("prop2"));
      }
   }

   public void testValidateRegistrationDataInvalidValue()
   {
      expectations.put(PROP1, new TestPropertyDescription(PROP1));

      registrationProperties.remove(PROP2);
      registrationProperties.put(PROP1, null);

      try
      {
         policy.validateRegistrationDataFor(registrationProperties, CONSUMER);
         fail("Should have detected null value for prop1");
      }
      catch (RegistrationException e)
      {
         assertTrue(e.getLocalizedMessage().contains("prop1"));
      }
   }

   static class TestPropertyDescription implements PropertyDescription
   {
      private QName name;
      private static final QName TYPE = new QName("type");

      TestPropertyDescription(QName name)
      {
         this.name = name;
      }

      public QName getName()
      {
         return name;
      }

      public QName getType()
      {
         return TYPE;
      }
   }
}
