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

package org.gatein.wsrp.producer.config;

import org.gatein.wsrp.ResourceFinder;
import org.gatein.wsrp.api.plugins.AbstractPlugins;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a> */
public class TestPlugins extends AbstractPlugins
{
   private final ResourceFinder resourceFinder;

   public TestPlugins()
   {
      resourceFinder = new ResourceFinder("META-INF/services");
   }

   @Override
   protected List<String> getImplementationNamesFor(String pluginClassName, String defaultImplementationClassName)
   {
      try
      {
         final List<String> allStrings = resourceFinder.findAllStrings(pluginClassName);
         List<String> implementations = new ArrayList<String>(allStrings.size() + 1);
         implementations.add(defaultImplementationClassName);
         return implementations;
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
   }

   @Override
   protected <T> Class<? extends T> getImplementationNamed(String className, Class<T> pluginClass) throws ClassNotFoundException
   {
      try
      {
         return resourceFinder.findClass(className, pluginClass);
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
   }
}
