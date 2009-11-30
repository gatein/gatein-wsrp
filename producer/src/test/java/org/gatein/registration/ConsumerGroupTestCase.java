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
public class ConsumerGroupTestCase extends TestCase
{
   private RegistrationManager manager;
   private ConsumerGroup group;
   private static final String NAME = "name";


   protected void setUp() throws Exception
   {
      manager = new RegistrationManagerImpl();
      RegistrationPolicy policy = new DefaultRegistrationPolicy();
      manager.setPolicy(policy);
      manager.setPersistenceManager(new RegistrationPersistenceManagerImpl());
      policy.setManager(manager);
      group = manager.createConsumerGroup(NAME);
   }

   public void testGetName()
   {
      assertEquals(NAME, group.getName());
   }

   public void testConsumersManagement() throws RegistrationException
   {
      assertTrue(group.isEmpty());
      assertEquals(0, group.getConsumers().size());

      Consumer c1 = manager.createConsumer("c1");
      group.addConsumer(c1);
      assertTrue(!group.isEmpty());
      assertEquals(1, group.getConsumers().size());
      assertTrue(group.contains(c1));
      assertEquals(group, c1.getGroup());
      assertEquals(c1, group.getConsumer(c1.getId()));

      Consumer c2 = manager.createConsumer("c2");
      group.addConsumer(c2);
      assertEquals(2, group.getConsumers().size());
      assertTrue(group.contains(c2));
      assertEquals(group, c2.getGroup());

      group.removeConsumer(c1);
      assertEquals(1, group.getConsumers().size());
      assertTrue(!group.contains(c1));
      assertTrue(group.contains(c2));
      assertEquals(null, c1.getGroup());
   }

   public void testAddNullConsumer() throws RegistrationException
   {
      try
      {
         group.addConsumer(null);
         fail("Shouldn't be possible to add null consumer");
      }
      catch (IllegalArgumentException expected)
      {
      }
   }

   public void testStatus()
   {
      assertEquals(RegistrationStatus.PENDING, group.getStatus());
      group.setStatus(RegistrationStatus.VALID);
      assertEquals(RegistrationStatus.VALID, group.getStatus());
   }

   public void testIllegalStatus()
   {
      try
      {
         group.setStatus(null);
         fail("Shouldn't be possible to set the status to null");
      }
      catch (IllegalArgumentException expected)
      {
      }
   }
}
