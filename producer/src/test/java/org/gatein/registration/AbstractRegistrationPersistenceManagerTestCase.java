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
import org.gatein.common.util.MapBuilder;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 9218 $
 * @since 2.6
 */
public abstract class AbstractRegistrationPersistenceManagerTestCase extends TestCase
{

   /** . */
   private Map<QName, Object> registrationProperties;

   public abstract RegistrationPersistenceManager getManager();

   public void startInteraction()
   {
   }

   public void stopInteraction()
   {
   }

   public void setUp() throws Exception
   {
      registrationProperties = new HashMap<QName, Object>();
      registrationProperties.put(new QName("prop1"), "value1");
      registrationProperties.put(new QName("prop2"), "value2");
   }

   protected void tearDown() throws Exception
   {
      registrationProperties = null;
   }

   public void testGetGroupThrowsIAE() throws Exception
   {
      startInteraction();
      try
      {
         getManager().getConsumerGroup(null);
         fail();
      }
      catch (IllegalArgumentException expected)
      {
      }
      stopInteraction();
   }

   public void testCreateConsumer() throws RegistrationException
   {
      startInteraction();
      Consumer consumer = getManager().createConsumer("Bar", "Bar");
      assertNotNull(consumer);
      assertEquals("Bar", consumer.getName());
      assertTrue(consumer.getRegistrations().isEmpty());
      assertNull(consumer.getGroup());
      assertNotNull(consumer.getPersistentKey());
      assertNull(consumer.getConsumerAgent());
      assertNotNull(consumer.getCapabilities());
      assertEquals(RegistrationStatus.PENDING, consumer.getStatus());
      stopInteraction();
   }

   public void testCreateGroup() throws RegistrationException
   {
      startInteraction();
      ConsumerGroup group = getManager().createConsumerGroup("Foo");
      assertNotNull(group);
      assertNotNull(group.getPersistentKey());
      assertEquals("Foo", group.getName());
      assertTrue(group.getConsumers().isEmpty());
      assertEquals(RegistrationStatus.PENDING, group.getStatus());
      stopInteraction();
   }

   public void testAddGroup() throws Exception
   {
      startInteraction();
      ConsumerGroup group = getManager().createConsumerGroup("Foo");
      assertNotNull(group);
      stopInteraction();

      // Test by retrieving the same consumer
      startInteraction();
      group = getManager().getConsumerGroup("Foo");
      assertNotNull(group);
      assertEquals("Foo", group.getName());
      assertEquals(Collections.EMPTY_LIST, new ArrayList(group.getConsumers()));
      stopInteraction();

      // Test by retrieving the consumer list
      startInteraction();
      Collection groups = getManager().getConsumerGroups();
      assertNotNull(groups);
      assertEquals(1, groups.size());
      group = (ConsumerGroup)groups.iterator().next();
      assertNotNull(group);
      assertEquals("Foo", group.getName());
      assertEquals(Collections.EMPTY_LIST, new ArrayList(group.getConsumers()));
      stopInteraction();
   }

   public void testAddDuplicateGroup() throws Exception
   {
      startInteraction();
      getManager().createConsumerGroup("Foo");
      try
      {
         getManager().createConsumerGroup("Foo");
         fail();
      }
      catch (DuplicateRegistrationException expected)
      {
      }
      stopInteraction();
   }

   public void testAddGroupThrowsIAE() throws Exception
   {
      startInteraction();
      try
      {
         getManager().createConsumerGroup(null);
      }
      catch (IllegalArgumentException expected)
      {
         assertEquals(Collections.EMPTY_SET, new HashSet(getManager().getConsumerGroups()));
      }
      stopInteraction();
   }

   public void testRemoveGroup() throws Exception
   {
      startInteraction();
      getManager().createConsumerGroup("Foo");
      stopInteraction();

      startInteraction();
      getManager().removeConsumerGroup("Foo");
      assertNull(getManager().getConsumerGroup("Foo"));
      assertEquals(Collections.EMPTY_SET, new HashSet(getManager().getConsumerGroups()));
      stopInteraction();
   }

   public void testRemoveGroupThrowsIAE() throws Exception
   {
      startInteraction();
      try
      {
         getManager().removeConsumerGroup(null);
      }
      catch (IllegalArgumentException expected)
      {
      }
      stopInteraction();
   }

   public void testRemoveNonExistingGroup() throws Exception
   {
      startInteraction();
      try
      {
         getManager().removeConsumerGroup("Foo");
      }
      catch (NoSuchRegistrationException expected)
      {
      }
      stopInteraction();
   }

   public void testGetConsumerThrowsIAE() throws Exception
   {
      startInteraction();
      try
      {
         ConsumerGroup group = getManager().createConsumerGroup("Foo");
         group.getConsumer(null);
         fail();
      }
      catch (IllegalArgumentException expected)
      {
      }
      stopInteraction();
   }

   public void testAddConsumer() throws Exception
   {
      startInteraction();
      ConsumerGroup group = getManager().createConsumerGroup("Foo");
      stopInteraction();

      startInteraction();
      Consumer consumer = getManager().createConsumer("Bar", "Bar");
      group.addConsumer(consumer);
      assertEquals("Foo", consumer.getGroup().getName());
      stopInteraction();

      // Test by retrieving the same consumer
      startInteraction();
      consumer = group.getConsumer("Bar");
      assertNotNull(consumer);
      assertEquals("Bar", consumer.getName());
      assertEquals(Collections.EMPTY_LIST, new ArrayList(consumer.getRegistrations()));
      assertEquals("Foo", consumer.getGroup().getName());
      stopInteraction();

      // Test by retrieving the consumer list
      startInteraction();
      Collection consumers = group.getConsumers();
      assertNotNull(consumers);
      assertEquals(1, consumers.size());
      consumer = (Consumer)consumers.iterator().next();
      assertNotNull(consumer);
      assertEquals("Bar", consumer.getName());
      assertEquals(Collections.EMPTY_LIST, new ArrayList(consumer.getRegistrations()));
      assertEquals("Foo", consumer.getGroup().getName());
      stopInteraction();
   }

   public void testAddDuplicateConsumer() throws Exception
   {
      startInteraction();
      ConsumerGroup group = getManager().createConsumerGroup("Foo");
      Consumer consumer = getManager().createConsumer("Bar", "Bar");
      group.addConsumer(consumer);
      stopInteraction();

      startInteraction();
      try
      {
         group.addConsumer(consumer);
         fail();
      }
      catch (IllegalArgumentException expected)
      {
      }
      stopInteraction();
   }

   public void testAddConsumerThrowsIAE() throws Exception
   {
      startInteraction();
      ConsumerGroup group = getManager().createConsumerGroup("Foo");
      try
      {
         group.addConsumer(null);
      }
      catch (IllegalArgumentException expected)
      {
         assertEquals(Collections.EMPTY_SET, new HashSet(group.getConsumers()));
      }
      stopInteraction();
   }

   public void testRemoveConsumer() throws Exception
   {
      startInteraction();
      ConsumerGroup group = getManager().createConsumerGroup("Foo");
      Consumer consumer = getManager().createConsumer("Bar", "Bar");
      group.addConsumer(consumer);
      group.removeConsumer(consumer);
      assertNull(group.getConsumer("Bar"));
      assertEquals(Collections.EMPTY_SET, new HashSet(group.getConsumers()));
      stopInteraction();
   }

   public void testRemoveConsumerThrowsIAE() throws Exception
   {
      startInteraction();
      ConsumerGroup group = getManager().createConsumerGroup("Foo");
      try
      {
         group.removeConsumer(null);
      }
      catch (IllegalArgumentException expected)
      {
      }
      stopInteraction();
   }

   public void testAddRegistration() throws Exception
   {
      startInteraction();
      ConsumerGroup group = getManager().createConsumerGroup("Foo");
      Consumer consumer = getManager().createConsumer("Bar", "Bar");
      group.addConsumer(consumer);
      stopInteraction();

      startInteraction();
      consumer = getManager().getConsumerById("Bar");
      Registration reg1 = getManager().addRegistrationFor("Bar", registrationProperties);
      assertNotNull(reg1);
      String regId = reg1.getPersistentKey();
      assertNotNull(regId);
      assertEquals(consumer, reg1.getConsumer());
      Map expectedProps = new HashMap();
      expectedProps.put(new QName("prop1"), "value1");
      expectedProps.put(new QName("prop2"), "value2");
      assertEquals(expectedProps, reg1.getProperties());
      stopInteraction();

      // Retrieve it from the list of consumer registrations
      startInteraction();
      consumer = getManager().getConsumerById("Bar");
      Collection registrations = consumer.getRegistrations();
      assertNotNull(registrations);
      assertEquals(1, registrations.size());
      Registration reg3 = (Registration)registrations.iterator().next();
      assertEquals(regId, reg3.getPersistentKey());
      assertEquals(consumer, reg3.getConsumer());
      assertEquals(expectedProps, reg3.getProperties());
      stopInteraction();

      // Retrieve the same registration from the registry
      startInteraction();
      Registration reg2 = getManager().getRegistration(regId);
      consumer = getManager().getConsumerById("Bar");
      assertNotNull(reg2);
      assertEquals(regId, reg2.getPersistentKey());
      assertEquals(consumer, reg2.getConsumer());
      assertEquals(expectedProps, reg2.getProperties());
      stopInteraction();
   }

   public void testAddRegistrationThrowsIAE() throws Exception
   {
      startInteraction();
      ConsumerGroup group = getManager().createConsumerGroup("Foo");
      Consumer consumer = getManager().createConsumer("Bar", "Bar");
      group.addConsumer(consumer);

      try
      {
         getManager().addRegistrationFor(consumer.getId(), null);
         fail();
      }
      catch (IllegalArgumentException expected)
      {
      }
      stopInteraction();
   }

   public void testRemoveRegistrationThrowsIAE() throws Exception
   {
      startInteraction();
      try
      {
         getManager().removeRegistration(null);
         fail();
      }
      catch (IllegalArgumentException expected)
      {
      }
      stopInteraction();
   }

   public void testRemoveRegistration() throws Exception
   {
      startInteraction();
      ConsumerGroup group = getManager().createConsumerGroup("Foo");
      Consumer consumer = getManager().createConsumer("Bar", "Bar");
      group.addConsumer(consumer);
      Registration reg = getManager().addRegistrationFor("Bar", registrationProperties);
      String regId = reg.getPersistentKey();
      getManager().removeRegistration(regId);
      stopInteraction();

      // remove registration is the only method on RegistrationPersistenceManager that needs to "cascade"
      // this is needed because there is no remove method on Consumer, hence the manager needs to remove the
      // registration from its consumer since it's the only class that has access to the specific consumer impl
      startInteraction();
      consumer = getManager().getConsumerById("Bar");
      Collection registrations = consumer.getRegistrations();
      assertNotNull(registrations);
      assertEquals(0, registrations.size());
      stopInteraction();

      //
      startInteraction();
      assertEquals(null, getManager().getRegistration(regId));
      stopInteraction();
   }

   public void testBulkUpdateRegistrationProperties() throws Exception
   {
      startInteraction();
      ConsumerGroup group = getManager().createConsumerGroup("Foo");
      Consumer consumer = getManager().createConsumer("Bar", "Bar");
      group.addConsumer(consumer);
      getManager().addRegistrationFor("Bar", registrationProperties);
      stopInteraction();

      //
      startInteraction();
      consumer = getManager().getConsumerById("Bar");
      Registration reg = (Registration)consumer.getRegistrations().iterator().next();
      registrationProperties.remove(new QName("prop1"));
      reg.updateProperties(registrationProperties);
      assertEquals(Collections.singletonMap(new QName("prop2"), "value2"), reg.getProperties());
      stopInteraction();

      //
      startInteraction();
      consumer = getManager().getConsumerById("Bar");
      reg = (Registration)consumer.getRegistrations().iterator().next();
      assertEquals(Collections.singletonMap(new QName("prop2"), "value2"), reg.getProperties());
      registrationProperties.put(new QName("prop3"), "value3");
      reg.updateProperties(registrationProperties);
      assertEquals(MapBuilder.hashMap().put(new QName("prop2"), "value2").put(new QName("prop3"), "value3").get(), reg.getProperties());
      stopInteraction();

      //
      startInteraction();
      consumer = getManager().getConsumerById("Bar");
      reg = (Registration)consumer.getRegistrations().iterator().next();
      assertEquals(MapBuilder.hashMap().put(new QName("prop2"), "value2").put(new QName("prop3"), "value3").get(), reg.getProperties());
      stopInteraction();
   }
}
