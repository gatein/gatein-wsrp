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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Provides base behavior for server-specific Plugins implementations.
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 */
public abstract class AbstractPlugins implements Plugins
{
   private static final List<String> KNOWN_PLUGIN_INTERFACES = new ArrayList<String>(3);

   static
   {
      KNOWN_PLUGIN_INTERFACES.add("org.gatein.registration.RegistrationPolicy");
      KNOWN_PLUGIN_INTERFACES.add("org.gatein.registration.policies.RegistrationPropertyValidator");
      KNOWN_PLUGIN_INTERFACES.add("org.gatein.wsrp.api.extensions.InvocationHandlerDelegate");
      KNOWN_PLUGIN_INTERFACES.add("javax.security.auth.callback.CallbackHandler");
   }

   @Override
   public List<String> getKnownPluginInterfaceNames()
   {
      return Collections.unmodifiableList(KNOWN_PLUGIN_INTERFACES);
   }

   @Override
   public List<String> getPluginImplementationNames(Class pluginClass, String defaultImplementationClassName)
   {
      try
      {
         // find all available implementations, including at least the default one
         final List<String> implementations = getImplementationNamesFor(pluginClass.getCanonicalName(), defaultImplementationClassName);
         // sort alphabetically
         Collections.sort(implementations);

         return implementations;
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   /**
    * Retrieves a list of fully qualified names of detected implementations of the WSRP extension interface specified by its provided fully qualified name, providing a default
    * implementation class name.
    *
    * @param pluginClassName                the fully qualified name of the plugin interface of which we want to retrieve the available implementations
    * @param defaultImplementationClassName the fully qualified name of the default implementation
    * @return a list of fully qualified names of detected implementations of the specified WSRP plugin
    */
   protected abstract List<String> getImplementationNamesFor(String pluginClassName, String defaultImplementationClassName);

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

   /**
    * Retrieves the Class object corresponding to the implementation named as specified by the provided name of the specified plugin interface.
    *
    * @param className   the fully qualified name of the implementation which Class we want to retrieve
    * @param pluginClass the interface of the plugin which the specified implementation is supposed to conform to
    * @param <T>         the type of the plugin
    * @return the Class object for the specified implementation of the provided plugin interface
    * @throws ClassNotFoundException if no such implementation exists
    */
   protected abstract <T> Class<? extends T> getImplementationNamed(String className, Class<T> pluginClass) throws ClassNotFoundException;
}
