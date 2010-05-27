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

import javax.xml.namespace.QName;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 11941 $
 * @since 2.6
 */
public class RegistrationPropertyTestCase extends TestCase
{
   private RegistrationProperty prop;

   private Listener listener = new Listener();

   private static final String VALUE = "value";
   private static final String NEW_VALUE = "newValue";

   private class Listener implements RegistrationProperty.PropertyChangeListener
   {
      boolean called;

      public void propertyValueChanged(RegistrationProperty property, RegistrationProperty.Status previousStatus, Object oldValue, Object newValue)
      {
         called = (prop == property) && VALUE.equals(oldValue) && NEW_VALUE.equals(newValue);
      }
   }

   protected void setUp() throws Exception
   {
      prop = new RegistrationProperty("name", VALUE, "en", listener);
   }

   public void testConstructor()
   {
      try
      {
         new RegistrationProperty((String)null, VALUE, "en", listener);
         fail("Should have failed on null name");
      }
      catch (Exception e)
      {
      }

      try
      {
         new RegistrationProperty("name", null, "en", listener);
      }
      catch (Exception e)
      {
         fail("Shouldn't have failed on null value");
      }

      try
      {
         new RegistrationProperty("name", VALUE, null, listener);
         fail("Should have failed on null lang");
      }
      catch (Exception e)
      {
      }

      try
      {
         new RegistrationProperty("name", VALUE, "en", null);
         fail("Should have failed on null listener");
      }
      catch (Exception e)
      {
      }
   }

   public void testSetNullValue()
   {
      forceValid();

      prop.setValue(null);
      assertNull(prop.getValue());
      assertTrue(prop.isInvalid());
      assertTrue(prop.isDeterminedInvalid());
      assertEquals(RegistrationProperty.Status.MISSING_VALUE, prop.getStatus());
   }

   public void testGetters()
   {
      assertEquals(QName.valueOf("name"), prop.getName());
      assertEquals("value", prop.getValue());
      assertEquals("en", prop.getLang());
      assertNull(prop.isInvalid());
      assertFalse(prop.isDeterminedInvalid());
      assertNull(prop.getDescription());
      assertEquals(RegistrationProperty.Status.UNCHECKED_VALUE, prop.getStatus());
   }

   public void testSetValue()
   {
      forceValid();

      // we haven't changed the value, so the status shouldn't have changed
      prop.setValue(VALUE);
      assertEquals(Boolean.FALSE, prop.isInvalid());
      assertEquals(RegistrationProperty.Status.VALID, prop.getStatus());

      // we changed the value, status is now unknown
      prop.setValue(NEW_VALUE);
      assertEquals(NEW_VALUE, prop.getValue());
      assertNull(prop.isInvalid());
      assertEquals(RegistrationProperty.Status.UNCHECKED_VALUE, prop.getStatus());
   }

   /** Force valid status so that we can check that status properly change based on state. */
   private void forceValid()
   {
      prop.setStatus(RegistrationProperty.Status.VALID);
   }

   public void testPropertyChangedEvent()
   {
      prop.setValue(VALUE);
      assertFalse(listener.called);

      prop.setValue(NEW_VALUE);
      assertTrue(listener.called);
   }
}
