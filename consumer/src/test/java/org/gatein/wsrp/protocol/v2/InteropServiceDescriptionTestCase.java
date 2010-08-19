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

package org.gatein.wsrp.protocol.v2;

import org.gatein.pc.api.Portlet;
import org.gatein.pc.api.PortletInvokerException;
import org.gatein.wsrp.test.ExtendedAssert;
import org.gatein.wsrp.test.protocol.v2.BehaviorRegistry;

import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class InteropServiceDescriptionTestCase extends V2ConsumerBaseTest
{
   public InteropServiceDescriptionTestCase() throws Exception
   {
      super();
   }

   @Override
   public void setUp() throws Exception
   {
      setStrict(false);
      super.setUp();
   }

   public void testUsesRelaxedMode()
   {
      ExtendedAssert.assertFalse(isStrict());
   }

   public void testGetPortlets() throws PortletInvokerException
   {
      //invoke consumer
      Set returnedPortlets = consumer.getPortlets();

      int portletNumber = returnedPortlets.size();
      ExtendedAssert.assertEquals(getPortletNumber(), portletNumber);
      Set<String> handles = getPortletHandles();
      Set<String> consumerHandles = new HashSet<String>(portletNumber);
      for (Object o : returnedPortlets)
      {
         Portlet portlet = (Portlet)o;
         consumerHandles.add(portlet.getContext().getId());
      }

      ExtendedAssert.assertTrue(handles.containsAll(consumerHandles));
   }

   @Override
   protected void registerAdditionalMarkupBehaviors(BehaviorRegistry registry)
   {
      // do nothing so that we don't pollute the service description with markup behaviors
   }
}
