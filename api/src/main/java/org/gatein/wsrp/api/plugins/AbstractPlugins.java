/*
* JBoss, a division of Red Hat
* Copyright 2012, Red Hat Middleware, LLC, and individual contributors as indicated
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

package org.gatein.wsrp.api.plugins;

import java.util.Collections;
import java.util.List;

/** @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a> */
public abstract class AbstractPlugins implements Plugins
{

   @Override
   public List<String> getPluginImplementationNames(Class pluginClass, String defaultImplementationClassName)
   {
      try
      {
         // find all available implementations
         final List<String> implementations = getImplementationNamesFor(pluginClass);
         // add the default one
         implementations.add(defaultImplementationClassName);
         // sort alphabetically
         Collections.sort(implementations);

         return implementations;
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   protected abstract List<String> getImplementationNamesFor(Class pluginClass);

   @Override
   public <T> T createPluginInstance(String className, Class<T> pluginClass)
   {
      try
      {
         final Class<? extends T> clazz = getImplementationNamed(className, pluginClass);
         if (!pluginClass.isAssignableFrom(clazz))
         {
            throw new IllegalArgumentException("Class does not implement" + pluginClass.getCanonicalName());
         }
         else
         {
            return pluginClass.cast(clazz.newInstance());
         }
      }
      catch (ClassNotFoundException e)
      {
         throw new IllegalArgumentException("Couldn't find class " + className, e);
      }
      catch (Exception e)
      {
         throw new IllegalArgumentException("Couldn't instantiate class " + className, e);
      }
   }

   protected abstract <T> Class<? extends T> getImplementationNamed(String className, Class<T> pluginClass) throws ClassNotFoundException;
}
