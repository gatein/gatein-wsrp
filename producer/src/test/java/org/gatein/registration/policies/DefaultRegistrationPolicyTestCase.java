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

package org.gatein.registration.policies;

import junit.framework.TestCase;
import org.gatein.registration.RegistrationException;
import org.gatein.registration.RegistrationManager;
import org.gatein.registration.impl.RegistrationManagerImpl;
import org.gatein.registration.impl.RegistrationPersistenceManagerImpl;
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
   private RegistrationManager manager;

   @Override
   protected void setUp() throws Exception
   {
      policy = new DefaultRegistrationPolicy();

      policy.setValidator(new DefaultRegistrationPropertyValidator());

      manager = new RegistrationManagerImpl();
      manager.setPolicy(policy);
      manager.setPersistenceManager(new RegistrationPersistenceManagerImpl());
      manager.createConsumer(CONSUMER);

      registrationProperties = new HashMap<QName, Object>();
      registrationProperties.put(PROP1, "value1");
      registrationProperties.put(PROP2, "value2");

      expectations = new HashMap<QName, PropertyDescription>();
   }

   public void testInitialState()
   {
      DefaultRegistrationPolicy registrationPolicy = new DefaultRegistrationPolicy();
      assertEquals(DefaultRegistrationPropertyValidator.DEFAULT, registrationPolicy.getValidator());
   }

   public void testValidateRegistrationDataForNull() throws RegistrationException
   {
      try
      {
         policy.validateRegistrationDataFor(null, "foo", expectations, manager);
         fail("null data cannot be validated");
      }
      catch (IllegalArgumentException e)
      {
         // expected
      }

      try
      {
         policy.validateRegistrationDataFor(Collections.<QName, Object>emptyMap(), null, expectations, manager);
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
         policy.validateRegistrationDataFor(Collections.<QName, Object>emptyMap(), "foo", expectations, manager);
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
         policy.validateRegistrationDataFor(registrationProperties, CONSUMER, expectations, manager);
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
         policy.validateRegistrationDataFor(registrationProperties, CONSUMER, expectations, manager);
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
         policy.validateRegistrationDataFor(registrationProperties, CONSUMER, expectations, manager);
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

      public int compareTo(PropertyDescription o)
      {
         return name.toString().compareTo(o.getName().toString());
      }
   }
}
