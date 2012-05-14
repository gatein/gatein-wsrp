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

import org.gatein.common.util.ParameterValidation;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.api.extensions.consumer.ConsumerExtensionAccessor;
import org.gatein.wsrp.payload.PayloadUtils;
import org.oasis.wsrp.v2.Extension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a> */
public class DefaultConsumerExtensionAccessor extends ConsumerExtensionAccessor
{
   // On-demand class holder Singleton pattern (multi-thread safe)
   private static final class InstanceHolder
   {
      public static final ConsumerExtensionAccessor instance = new DefaultConsumerExtensionAccessor();
   }

   // register instance with API
   static
   {
      registerInstance(InstanceHolder.instance);
   }


   private static final ThreadLocal<Map<Class, List<Extension>>> EXTENSIONS = new ThreadLocal<Map<Class, List<Extension>>>();
   private static final ThreadLocal<Map<Class, List<UnmarshalledExtension>>> UNMARSHALLED_EXTENSIONS = new ThreadLocal<Map<Class, List<UnmarshalledExtension>>>();

   public List<Extension> getRequestExtensionsFor(Class targetClass)
   {
      List<Extension> extensions = null;
      if (targetClass != null)
      {
         final Map<Class, List<Extension>> extensionsMap = EXTENSIONS.get();
         if (extensionsMap != null)
         {
            extensions = extensionsMap.get(targetClass);
         }
      }

      return extensions != null ? extensions : Collections.<Extension>emptyList();
   }

   public List<UnmarshalledExtension> getResponseExtensionsFrom(Class responseClass)
   {
      if (responseClass != null)
      {

      }

      return Collections.emptyList();
   }

   public void addRequestExtension(Class targetClass, String name, String value)
   {
      Map<Class, List<Extension>> extensionsMap = EXTENSIONS.get();
      if (extensionsMap == null)
      {
         extensionsMap = new ConcurrentHashMap<Class, List<Extension>>(7);
         EXTENSIONS.set(extensionsMap);
      }

      List<Extension> extensions = extensionsMap.get(targetClass);
      if (!ParameterValidation.isNullOrEmpty(name))
      {
         if (extensions == null)
         {
            extensions = new ArrayList<Extension>(3);
            extensionsMap.put(targetClass, extensions);
         }

         extensions.add(WSRPTypeFactory.createExtension(PayloadUtils.marshallExtension(name, value)));
      }
   }

   public void addReponseExtension(Class responseClass, UnmarshalledExtension extension)
   {
      Map<Class, List<UnmarshalledExtension>> extensionsMap = UNMARSHALLED_EXTENSIONS.get();
      if (extensionsMap == null)
      {
         extensionsMap = new ConcurrentHashMap<Class, List<UnmarshalledExtension>>(7);
         UNMARSHALLED_EXTENSIONS.set(extensionsMap);
      }

      List<UnmarshalledExtension> extensions = extensionsMap.get(responseClass);
      if (extension != null)
      {
         if (extensions == null)
         {
            extensions = new ArrayList<UnmarshalledExtension>(3);
            extensionsMap.put(responseClass, extensions);
         }

         extensions.add(extension);
      }
   }
}
