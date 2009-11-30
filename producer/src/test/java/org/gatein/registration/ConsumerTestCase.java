/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2006, Red Hat Middleware, LLC, and individual                    *
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

package org.gatein.registration;

import junit.framework.TestCase;
import org.gatein.registration.impl.RegistrationManagerImpl;
import org.gatein.registration.impl.RegistrationPersistenceManagerImpl;
import org.gatein.registration.policies.DefaultRegistrationPolicy;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 8784 $
 * @since 2.6
 */
public class ConsumerTestCase extends TestCase
{
   private Consumer consumer;
   private RegistrationManager manager;

   protected void setUp() throws Exception
   {
      manager = new RegistrationManagerImpl();
      RegistrationPolicy policy = new DefaultRegistrationPolicy();
      manager.setPolicy(policy);
      manager.setPersistenceManager(new RegistrationPersistenceManagerImpl());
      policy.setManager(manager);
      consumer = manager.createConsumer("name");
   }

   public void testGetName()
   {
      assertEquals("name", consumer.getName());
   }

   public void testStatus()
   {
      assertEquals(RegistrationStatus.PENDING, consumer.getStatus());

      consumer.setStatus(RegistrationStatus.VALID);
      assertEquals(RegistrationStatus.VALID, consumer.getStatus());
   }

   public void testIllegalStatus()
   {
      try
      {
         consumer.setStatus(null);
         fail("Was expecting an IllegalArgumentException to be thrown on setStatus(null)");
      }
      catch (IllegalArgumentException expected)
      {
      }
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
