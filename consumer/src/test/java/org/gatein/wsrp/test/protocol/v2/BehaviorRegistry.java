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

package org.gatein.wsrp.test.protocol.v2;

import org.gatein.wsrp.WSRPExceptionFactory;
import org.oasis.wsrp.v2.InvalidHandle;
import org.oasis.wsrp.v2.InvalidHandleFault;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 9360 $
 * @since 2.6
 */
public class BehaviorRegistry
{
   private final Map<String, MarkupBehavior> behaviors = new HashMap<String, MarkupBehavior>();
   private PortletManagementBehavior portletManagementBehavior;
   private ServiceDescriptionBehavior serviceDescriptionBehavior;
   private RegistrationBehavior registrationBehavior;

   public ServiceDescriptionBehavior getServiceDescriptionBehavior()
   {
      // this is required since the consumer will try to access the producer as soon as it's started and the test
      // producer will not be properly setup at that time since it's set up in the test's setUp method...
      if (serviceDescriptionBehavior == null)
      {
         return ServiceDescriptionBehavior.DEFAULT;
      }

      return serviceDescriptionBehavior;
   }

   public void clear()
   {
      behaviors.clear();
   }

   public MarkupBehavior getMarkupBehaviorFor(String handle) throws InvalidHandle
   {
      if (behaviors.containsKey(handle))
      {
         return behaviors.get(handle);
      }
      throw WSRPExceptionFactory.<InvalidHandle, InvalidHandleFault>throwWSException(WSRPExceptionFactory.INVALID_HANDLE,
         "There is no registered MarkupBehavior for handle '" + handle + "'", null);
   }

   public void registerMarkupBehavior(MarkupBehavior behavior)
   {
      for (String handle : behavior.getSupportedHandles())
      {
         MarkupBehavior existing = behaviors.get(handle);
         if (existing != null)
         {
            throw new IllegalArgumentException("Cannot register behavior " + behavior.getClass().getName()
               + " because it uses a handle '" + handle + "' that's already associated with behavior "
               + existing.getClass().getName());
         }
         behaviors.put(handle, behavior);
      }
   }

   public PortletManagementBehavior getPortletManagementBehavior()
   {
      return portletManagementBehavior;
   }

   public void setPortletManagementBehavior(PortletManagementBehavior portletManagementBehavior)
   {
      this.portletManagementBehavior = portletManagementBehavior;
   }

   public void setServiceDescriptionBehavior(ServiceDescriptionBehavior serviceDescriptionBehavior)
   {
      this.serviceDescriptionBehavior = serviceDescriptionBehavior;
   }

   public RegistrationBehavior getRegistrationBehavior()
   {
      return registrationBehavior;
   }

   public void setRegistrationBehavior(RegistrationBehavior registrationBehavior)
   {
      this.registrationBehavior = registrationBehavior;
   }
}
