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
import org.gatein.wsrp.payload.PayloadUtils;
import org.oasis.wsrp.v2.Extension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a> */
public class AbstractExtensionAccessor
{
   private final ThreadLocal<Map<Class, List<Extension>>> extensions = new ThreadLocal<Map<Class, List<Extension>>>();
   private final ThreadLocal<Map<Class, List<UnmarshalledExtension>>> unmarshalledExtensions = new ThreadLocal<Map<Class, List<UnmarshalledExtension>>>();

   public List<Extension> getExtensions(Class targetClass)
   {
      return get(extensions, targetClass);
   }

   public List<UnmarshalledExtension> getUnmarshalledExtensions(Class targetClass)
   {
      return get(unmarshalledExtensions, targetClass);
   }

   private <T> List<T> get(ThreadLocal<Map<Class, List<T>>> mapThreadLocal, Class targetClass)
   {
      List<T> extensions = null;
      if (targetClass != null)
      {
         final Map<Class, List<T>> extensionsMap = mapThreadLocal.get();
         if (extensionsMap != null)
         {
            extensions = extensionsMap.get(targetClass);
         }
      }

      return extensions != null ? extensions : Collections.<T>emptyList();
   }

   public void addExtension(Class targetClass, Object extension)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(extension, "Extension");

      add(extensions, targetClass, WSRPTypeFactory.createExtension(PayloadUtils.marshallExtension(extension)));
   }

   public void addUnmarshalledExtension(Class targetClass, UnmarshalledExtension extension)
   {
      add(unmarshalledExtensions, targetClass, extension);
   }

   private <T> void add(ThreadLocal<Map<Class, List<T>>> mapThreadLocal, Class targetClass, T toAdd)
   {
      Map<Class, List<T>> extensionsMap = mapThreadLocal.get();
      if (extensionsMap == null)
      {
         extensionsMap = new ConcurrentHashMap<Class, List<T>>(7);
         mapThreadLocal.set(extensionsMap);
      }

      List<T> extensions = extensionsMap.get(targetClass);
      if (toAdd != null)
      {
         if (extensions == null)
         {
            extensions = new ArrayList<T>(3);
            extensionsMap.put(targetClass, extensions);
         }

         extensions.add(toAdd);
      }
   }

   public void clear()
   {
      extensions.set(null);
      unmarshalledExtensions.set(null);
   }
}
