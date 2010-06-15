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
import org.gatein.registration.policies.DefaultRegistrationPropertyValidator;
import org.gatein.wsrp.WSRPConstants;
import org.gatein.wsrp.registration.RegistrationPropertyDescription;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 8784 $
 * @since 2.6
 */
public class ConsumerTestCase extends TestCase
{
   private Consumer consumer;
   private RegistrationManager manager;
   private static final Map<QName, RegistrationPropertyDescription> EMPTY_EXPECTATIONS = Collections.emptyMap();

   protected void setUp() throws Exception
   {
      manager = new RegistrationManagerImpl();
      DefaultRegistrationPolicy policy = new DefaultRegistrationPolicy();
      policy.setValidator(new DefaultRegistrationPropertyValidator());
      manager.setPolicy(policy);
      manager.setPersistenceManager(new RegistrationPersistenceManagerImpl());
      consumer = manager.createConsumer("name");
   }

   public void testGetName()
   {
      assertEquals("name", consumer.getName());
   }

   public void testStatus() throws RegistrationException
   {
      assertEquals(RegistrationStatus.PENDING, consumer.getStatus());

      String name = consumer.getName();

      // adding a registration that isn't validated doesn't change the status
      Registration registration = manager.addRegistrationTo(name, Collections.<QName, Object>emptyMap(), EMPTY_EXPECTATIONS, false);
      assertEquals(RegistrationStatus.PENDING, consumer.getStatus());

      // but the consumer's status should become valid if the registration becomes so as well
      registration.setStatus(RegistrationStatus.VALID);
      assertEquals(RegistrationStatus.VALID, consumer.getStatus());

      // adding a new registration makes the consumer's status pending
      // need to change the registration props to register this consumer again
      Map<QName, Object> props = new HashMap<QName, Object>(1);
      QName propName = new QName("prop");
      props.put(propName, "value");
      // need to change the expectations to allow the new registration property
      Map<QName, RegistrationPropertyDescription> expectations = new HashMap<QName, RegistrationPropertyDescription>();
      expectations.put(propName, new RegistrationPropertyDescription(propName, WSRPConstants.XSD_STRING));
      registration = manager.addRegistrationTo(name, props, expectations, false);
      assertEquals(2, consumer.getRegistrations().size());
      assertEquals(RegistrationStatus.PENDING, consumer.getStatus());

      // and if the new registration is marked as invalid, so does the consumer's status
      registration.setStatus(RegistrationStatus.INVALID);
      assertEquals(RegistrationStatus.INVALID, consumer.getStatus());

      // if the registration is returned to pending, then so is the consumer
      registration.setStatus(RegistrationStatus.PENDING);
      assertEquals(RegistrationStatus.PENDING, consumer.getStatus());

      // if all the registrations are valid, then so is the consumer
      registration.setStatus(RegistrationStatus.VALID);
      assertEquals(RegistrationStatus.VALID, consumer.getStatus());
   }

   public void testSetGroup() throws Exception
   {
      ConsumerGroup group = manager.createConsumerGroup("group");
      assertTrue(!group.getConsumers().contains(consumer));

      consumer.setGroup(group);
      assertEquals(group, consumer.getGroup());
      assertTrue(group.getConsumers().contains(consumer));

      consumer.setGroup(null);
      assertNull(consumer.getGroup());
      assertTrue(!group.getConsumers().contains(consumer));
   }

   public void testGetIdentity() throws Exception
   {
      assertNotNull(consumer.getId());
   }
}
