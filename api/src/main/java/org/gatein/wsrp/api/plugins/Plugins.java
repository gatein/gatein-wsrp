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

import java.util.List;

/**
 * An entry point for the WSRP plugins system with the underlying server. The portal server in which WSRP is running must provide an implementation of this interface so that WSRP
 * can properly access user-developed plugins.
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 */
public interface Plugins
{
   /** The extension that identifies WSRP plugins. Note that this is just a marker as for backward compatibility reasons, plain jar extensions are also supported. */
   String WSRP_PLUGIN_EXTENSION_SUFFIX = ".wsrp.jar";

   /**
    * Retrieves the list of currently known extension points as plugins in our WSRP implementations. These are interfaces that the WSRP plugin system can detect and work with.
    *
    * @return the list of currently known plugin interface fully qualified names
    */
   List<String> getKnownPluginInterfaceNames();

   /**
    * Retrieves a list of fully qualified names of detected implementations of the specified WSRP extension interface, providing a default implementation class name.
    *
    * @param pluginClass                    the Class object representing the WSRP plugin interface we want to retrieve the implementations of
    * @param defaultImplementationClassName the fully qualified name of the default implementation for the specified plugin
    * @return a list of fully qualified names of detected implementations of the specified WSRP plugin interface
    */
   List<String> getPluginImplementationNames(Class pluginClass, String defaultImplementationClassName);

   /**
    * Instantiates a plugin implementation using the default constructor of the plugin identified by the specified fully qualified class name, specifying that the implementation
    * must implement the specified plugin interface.
    *
    * @param className   the fully qualified name of the implementation class to instantiate
    * @param pluginClass the interface of the plugin of which we want to create an instance
    * @param <T>         the type of the plugin
    * @return an instance of the specified implementation class provided it properly implements the specified plugin interface
    */
   <T> T createPluginInstance(String className, Class<T> pluginClass);
}
