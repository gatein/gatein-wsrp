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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class ExtensionAccessor
{
   private static final ThreadLocal<Map<Class, List>> EXTENSIONS = new ThreadLocal<Map<Class, List>>();

   /**
    * Retrieves previously set extensions targeted at the specified WSRP 2 target class so that the consumer can add
    * them to the requests before sending them to the producer. For examples, to retrieve extensions currently targeted
    * to be added on MarkupParams, you would pass MarkupParams.class.
    *
    * @param targetClass the WSRP 2 class for which extensions are supposed to be retrieved, if any
    * @return a List containing the Extensions needed to be added to the target class or an empty List if no such
    *         extension exists. Note that we're using a detyped API to avoid pulling undue dependencies in this module.
    */
   public static List getConsumerExtensionsTargetedAt(Class targetClass)
   {
      List extensions = null;
      if (targetClass != null)
      {
         final Map<Class, List> extensionsMap = EXTENSIONS.get();
         if (extensionsMap != null)
         {
            extensions = extensionsMap.get(targetClass);
         }
      }

      return extensions != null ? extensions : Collections.emptyList();
   }

   public static List<UnmarshalledExtension> getProducerResponseExtensionsFrom(Class responseClass)
   {
      if (responseClass != null)
      {

      }

      return Collections.emptyList();
   }

   public static void addConsumerExtensionTargetedAt(Class targetClass, String name, String value)
   {

   }

   public static void addProducerResponseExtensionFrom(Class responseClass, UnmarshalledExtension extension)
   {
      Map<Class, List> extensionsMap = EXTENSIONS.get();
      if (extensionsMap == null)
      {
         extensionsMap = new ConcurrentHashMap<Class, List>(7);
         EXTENSIONS.set(extensionsMap);
      }

      List extensions = extensionsMap.get(responseClass);
      if (extension != null)
      {
         if (extensions == null)
         {
            extensions = new ArrayList(3);
            extensionsMap.put(responseClass, extensions);
         }

         extensions.add(extension);
      }
   }
}
