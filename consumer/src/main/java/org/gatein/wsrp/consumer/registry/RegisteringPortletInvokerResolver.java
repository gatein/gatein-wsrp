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

package org.gatein.wsrp.consumer.registry;

import org.gatein.pc.api.NoSuchPortletException;
import org.gatein.pc.federation.FederatedPortletInvoker;
import org.gatein.pc.federation.FederatingPortletInvoker;
import org.gatein.pc.federation.PortletInvokerResolver;
import org.gatein.pc.federation.impl.FederatedPortletInvokerService;
import org.gatein.wsrp.WSRPConsumer;
import org.gatein.wsrp.consumer.ConsumerException;
import org.gatein.wsrp.consumer.spi.ConsumerRegistrySPI;

import java.util.Collection;

/**
 * Attempts to activate a WSRP consumer named like the missing invoker that triggered the invocation of this PortletInvokerResolver. This is in particularly helpful to activate
 * configured consumers that haven't been started yet when a portlet referencing them is accessed.
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class RegisteringPortletInvokerResolver implements PortletInvokerResolver
{
   private transient ConsumerRegistrySPI consumerRegistry;

   public FederatedPortletInvoker resolvePortletInvokerFor(String invokerId, FederatingPortletInvoker callingInvoker, String compoundPortletId) throws NoSuchPortletException
   {
      FederatingPortletInvoker registryInvoker = consumerRegistry.getFederatingPortletInvoker();
      if (!registryInvoker.equals(callingInvoker))
      {
         throw new IllegalArgumentException("Trying to use a ConsumerRegistry already linked to a different FederatingPortletInvoker ("
            + registryInvoker + ") than the specified one (" + callingInvoker + ")");
      }

      WSRPConsumer consumer = consumerRegistry.getConsumer(invokerId);

      if (consumer == null)
      {
         // if there's no consumer with that invoker id, then there's nothing much we can do
         if (compoundPortletId != null)
         {
            throw new NoSuchPortletException(compoundPortletId);
         }
         else
         {
            return null;
         }
      }
      else
      {
         // register it with the FederatingPortletInvoker
         synchronized (this)
         {
            try
            {
               consumerRegistry.registerWithFederatingPortletInvoker(consumer);
               return new FederatedPortletInvokerService(callingInvoker, invokerId, consumer);
            }
            catch (ConsumerException e)
            {
               return null;
            }
         }
      }
   }

   public boolean knows(String invoker)
   {
      return consumerRegistry.containsConsumer(invoker);
   }

   public Collection<String> getKnownInvokerIds()
   {
      return consumerRegistry.getConfiguredConsumersIds();
   }

   public void setConsumerRegistry(ConsumerRegistrySPI consumerRegistry)
   {
      this.consumerRegistry = consumerRegistry;
   }
}
