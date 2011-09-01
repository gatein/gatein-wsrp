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
import org.gatein.pc.federation.NullInvokerHandler;
import org.gatein.pc.federation.impl.FederatedPortletInvokerService;
import org.gatein.wsrp.WSRPConsumer;

import java.util.Collection;

/**
 * Attempts to activate a WSRP consumer named like the missing invoker that trigger the invocation of this
 * NullInvokerHandler. This is in particularly helpful to activate configured consumers that haven't been started yet
 * when a portlet referencing them is accessed.
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class ActivatingNullInvokerHandler implements NullInvokerHandler
{
   private transient ConsumerRegistry consumerRegistry;

   public FederatedPortletInvoker resolvePortletInvokerFor(String invokerId, FederatingPortletInvoker callingInvoker, String compoundPortletId) throws NoSuchPortletException
   {
      FederatingPortletInvoker registryInvoker = consumerRegistry.getFederatingPortletInvoker();
      if (registryInvoker != callingInvoker)
      {
         throw new IllegalArgumentException("Trying to use a ConsumerRegistry already linked to a different FederatingPortletInvoker ("
            + registryInvoker + ") than the specified one (" + callingInvoker + ")");
      }

      WSRPConsumer consumer = consumerRegistry.getConsumer(invokerId);

      // if there's no consumer with that invoker id, then there's nothing much we can do
      if (consumer == null)
      {
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
         // activate the consumer which should register it with this FederatingPortletInvoker
         synchronized (this)
         {
            consumerRegistry.activateConsumerWith(invokerId);

            return new FederatedPortletInvokerService(callingInvoker, invokerId, consumer);
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

   public void setConsumerRegistry(ConsumerRegistry consumerRegistry)
   {
      this.consumerRegistry = consumerRegistry;
   }
}
