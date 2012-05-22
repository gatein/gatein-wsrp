/*
* JBoss, a division of Red Hat
* Copyright 2008, Red Hat Middleware, LLC, and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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

package org.gatein.wsrp.api.extensions;

import org.gatein.wsrp.api.extensions.consumer.ConsumerExtensionAccessor;
import org.gatein.wsrp.api.extensions.producer.ProducerExtensionAccessor;

/**
 * Provides an entry point to the extension support in GateIn WSRP. ConsumerExtensionAccessor provides access to
 * extensions on the consumer-side while ProducerExtensionAccessor provides access to extensions on the producer-side.
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 */
public class ExtensionAccess
{
   private static ConsumerExtensionAccessor consumer;
   private static ProducerExtensionAccessor producer;

   public static ConsumerExtensionAccessor getConsumerExtensionAccessor()
   {
      return consumer;
   }

   public static ProducerExtensionAccessor getProducerExtensionAccessor()
   {
      return producer;
   }

   protected synchronized static void registerConsumerAccessorInstance(ConsumerExtensionAccessor accessor)
   {
      if (consumer != null)
      {
         throw new IllegalStateException("A ConsumerExtensionAccessor has already been registered!");
      }
      else
      {
         consumer = accessor;
      }
   }

   protected synchronized static void registerProducerAccessorInstance(ProducerExtensionAccessor accessor)
   {
      if (producer != null)
      {
         throw new IllegalStateException("A ProducerExtensionAccessor has already been registered!");
      }
      else
      {
         producer = accessor;
      }
   }
}
