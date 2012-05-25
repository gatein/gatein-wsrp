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

import org.oasis.wsrp.v2.Extension;

import java.util.List;

/** @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a> */
public class DefaultProducerExtensionAccessor extends AbstractExtensionAccessor implements ProducerExtensionAccessor
{
   private DefaultProducerExtensionAccessor()
   {
   }

   @Override
   public void addRequestExtension(Class fromClass, UnmarshalledExtension extension)
   {
      addUnmarshalledExtension(fromClass, extension);
   }

   @Override
   public List<UnmarshalledExtension> getRequestExtensionsFor(Class targetClass)
   {
      return getUnmarshalledExtensions(targetClass);
   }

   @Override
   public List<Extension> getResponseExtensionsFor(Class wsrpResponseClass)
   {
      return getExtensions(wsrpResponseClass);
   }

   @Override
   public void addResponseExtension(Class wsrpResponseClass, String name, String value)
   {
      addExtension(wsrpResponseClass, name, value);
   }

   // On-demand class holder Singleton pattern (multi-thread safe)
   private static final class InstanceHolder
   {
      public static final ProducerExtensionAccessor instance = new DefaultProducerExtensionAccessor();
   }

   public synchronized static void registerWithAPI()
   {
      // register instance with API
      if (ExtensionAccess.getProducerExtensionAccessor() == null)
      {
         ExtensionAccess.registerProducerAccessorInstance(InstanceHolder.instance);
      }
   }
}
