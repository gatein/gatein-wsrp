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

package org.gatein.wsrp.services;

import org.gatein.common.util.ParameterValidation;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/** @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a> */
public class PortCustomizerRegistry
{
   private static PortCustomizerRegistry instance = new PortCustomizerRegistry();
   private Set<PortCustomizer> customizers;

   private PortCustomizerRegistry()
   {
   }

   public static PortCustomizerRegistry getInstance()
   {
      return instance;
   }

   public Iterable<PortCustomizer> getPortCustomizers()
   {
      return customizers != null ? Collections.unmodifiableSet(customizers) : Collections.<PortCustomizer>emptySet();
   }

   public boolean hasWSSFocusedCustomizers()
   {
      for (PortCustomizer customizer : customizers)
      {
         if (customizer.isWSSFocused())
         {
            return true;
         }
      }

      return false;
   }

   public void register(PortCustomizer customizer)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(customizer, "PortCustomizer");
      if (customizers == null)
      {
         customizers = new HashSet<PortCustomizer>(7);
      }
      customizers.add(customizer);
   }

   public void unregister(PortCustomizer customizer)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(customizer, "PortCustomizer");
      if (customizers == null)
      {
         return;
      }
      customizers.remove(customizer);
   }
}
