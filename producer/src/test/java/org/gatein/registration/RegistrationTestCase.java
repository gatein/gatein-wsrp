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

package org.gatein.registration;

import junit.framework.TestCase;
import org.gatein.registration.impl.RegistrationManagerImpl;
import org.gatein.registration.impl.RegistrationPersistenceManagerImpl;
import org.gatein.registration.policies.DefaultRegistrationPolicy;
import org.gatein.wsrp.WSRPConstants;
import org.gatein.wsrp.registration.PropertyDescription;
import org.gatein.wsrp.registration.RegistrationPropertyDescription;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 11406 $
 * @since 2.6
 */
public class RegistrationTestCase extends TestCase
{
   private Registration registration;
   private Map<QName, Object> registrationProperties;

   protected void setUp() throws Exception
   {
      RegistrationManager manager = new RegistrationManagerImpl();
      RegistrationPolicy policy = new DefaultRegistrationPolicy()
      {
         public void validateRegistrationDataFor(Map<QName, Object> registrationProperties, String consumerIdentity, final Map<QName, ? extends PropertyDescription> expectations, final RegistrationManager manager) throws IllegalArgumentException, RegistrationException, DuplicateRegistrationException
         {
            // accept any registration data here
         }
      };
      manager.setPolicy(policy);
      manager.setPersistenceManager(new RegistrationPersistenceManagerImpl());

      registrationProperties = new HashMap<QName, Object>();
      QName prop1Name = new QName("prop1");
      registrationProperties.put(prop1Name, "value1");
      QName prop2Name = new QName("prop2");
      registrationProperties.put(prop2Name, "value2");

      Map<QName, RegistrationPropertyDescription> expectations = new HashMap<QName, RegistrationPropertyDescription>();
      expectations.put(prop1Name, new RegistrationPropertyDescription(prop1Name, WSRPConstants.XSD_STRING));
      expectations.put(prop2Name, new RegistrationPropertyDescription(prop2Name, WSRPConstants.XSD_STRING));

      registration = manager.addRegistrationTo("name", registrationProperties, expectations, true);
   }

   public void testGetPropertiesIsUnmodifiable()
   {
      Map properties = registration.getProperties();

      try
      {
         properties.remove("foo");
         fail("Properties shouldn't be directly modifiable");
      }
      catch (Exception expected)
      {
      }
   }

   public void testPropertiesAreClonedNotLive()
   {
      QName prop = new QName("prop3");
      registrationProperties.put(prop, "value3");

      assertNull(registration.getProperties().get(prop));
   }

   public void testSetNullPropertyValueThrowsIAE()
   {
      try
      {
         registration.setPropertyValueFor("foo", null);
         fail("Shouldn't accept null values for properties");
      }
      catch (Exception expected)
      {
      }
   }

   public void testSetNullPropertyNameThrowsIAE()
   {
      try
      {
         registration.setPropertyValueFor((QName)null, null);
         fail("Shouldn't accept null names for properties");
      }
      catch (Exception expected)
      {
      }
   }

   public void testProperties()
   {
      QName name = new QName("prop1");
      assertEquals("value1", registration.getProperties().get(name));
      assertEquals("value2", registration.getProperties().get(new QName("prop2")));

      String newValue = "new value";
      registration.setPropertyValueFor("prop1", newValue);
      assertEquals(newValue, registration.getProperties().get(name));

      registration.removeProperty(name);
      assertNull(registration.getPropertyValueFor(name));
   }

   public void testUpdateProperties()
   {
      registrationProperties.remove(new QName("prop1"));

      registration.updateProperties(registrationProperties);
      assertNull(registration.getPropertyValueFor("prop1"));

      QName name = new QName("prop3");
      String value = "value3";
      registrationProperties.put(name, value);
      registration.updateProperties(registrationProperties);
      assertEquals(value, registration.getPropertyValueFor(name));
   }

   public void testHasEqualProperties()
   {
      assertTrue(registration.hasEqualProperties(registration));

      assertTrue(registration.hasEqualProperties(registrationProperties));

      registrationProperties.put(new QName("prop3"), "value3");
      assertTrue(!registration.hasEqualProperties(registrationProperties));
   }

   public void testClearAssociatedState()
   {
      //todo: implement
   }

}
