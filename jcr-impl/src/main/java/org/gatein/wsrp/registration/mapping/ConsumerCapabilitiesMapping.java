/*
 * JBoss, a division of Red Hat
 * Copyright 2011, Red Hat Middleware, LLC, and individual
 * contributors as indicated by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of
 * individual contributors.
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

package org.gatein.wsrp.registration.mapping;

import org.chromattic.api.annotations.PrimaryType;
import org.chromattic.api.annotations.Property;
import org.gatein.common.util.ParameterValidation;
import org.gatein.pc.api.Mode;
import org.gatein.pc.api.WindowState;
import org.gatein.registration.ConsumerCapabilities;
import org.gatein.registration.impl.ConsumerCapabilitiesImpl;
import org.gatein.wsrp.jcr.mapping.BaseMapping;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
@PrimaryType(name = ConsumerCapabilitiesMapping.NODE_NAME)
public abstract class ConsumerCapabilitiesMapping implements BaseMapping<ConsumerCapabilities, Object>
{
   public static final String NODE_NAME = "wsrp:consumercapabilities";

   @Property(name = "supportsgetmethod")
   public abstract boolean getSupportsGetMethod();

   public abstract void setSupportsGetMethod(boolean supportsGetMethod);

   @Property(name = "modes")
   public abstract List<String> getSupportedModes();

   public abstract void setSupportedModes(List<String> modes);

   @Property(name = "windowstates")
   public abstract List<String> getSupportedWindowStates();

   public abstract void setSupportedWindowStates(List<String> windowStates);

   @Property(name = "userscopes")
   public abstract List<String> getSupportedUserScopes();

   public abstract void setSupportedUserScopes(List<String> userScopes);

   @Property(name = "userprofiledata")
   public abstract List<String> getSupportedUserProfileData();

   public abstract void setSupportedUserProfileData(List<String> userProfileData);


   public void initFrom(ConsumerCapabilities capabilities)
   {
      setSupportsGetMethod(capabilities.supportsGetMethod());

      List<String> modes = convertToStringList(capabilities.getSupportedModes());
      if (modes != null)
      {
         setSupportedModes(modes);
      }
      List<String> states = convertToStringList(capabilities.getSupportedWindowStates());
      if (states != null)
      {
         setSupportedWindowStates(states);
      }
      List<String> scopes = capabilities.getSupportedUserScopes();
      if (ParameterValidation.existsAndIsNotEmpty(scopes))
      {
         setSupportedUserScopes(scopes);
      }
      List<String> userProfileData = capabilities.getSupportedUserProfileData();
      if (ParameterValidation.existsAndIsNotEmpty(userProfileData))
      {
         setSupportedUserProfileData(userProfileData);
      }
   }

   private List<String> convertToStringList(List list)
   {
      if (ParameterValidation.existsAndIsNotEmpty(list))
      {
         List<String> result = new ArrayList<String>(list.size());
         for (Object object : list)
         {
            result.add(object.toString());
         }
         return result;
      }

      return null;
   }

   public ConsumerCapabilities toConsumerCapabilities()
   {
      return toModel(null, null);
   }

   @Override
   public ConsumerCapabilities toModel(ConsumerCapabilities initial, Object registry)
   {
      if (initial == null)
      {
         initial = new ConsumerCapabilitiesImpl();
      }


      initial.setSupportsGetMethod(getSupportsGetMethod());

      List<String> modeStrings = getSupportedModes();
      if (ParameterValidation.existsAndIsNotEmpty(modeStrings))
      {
         List<Mode> modes = new ArrayList<Mode>(modeStrings.size());
         for (String modeString : modeStrings)
         {
            modes.add(Mode.create(modeString));
         }
         initial.setSupportedModes(modes);
      }

      List<String> windowStateStrings = getSupportedWindowStates();
      if (ParameterValidation.existsAndIsNotEmpty(windowStateStrings))
      {
         List<WindowState> windowStates = new ArrayList<WindowState>(windowStateStrings.size());
         for (String windowStateString : windowStateStrings)
         {
            windowStates.add(WindowState.create(windowStateString));
         }
         initial.setSupportedWindowStates(windowStates);
      }

      List<String> userProfileData = getSupportedUserProfileData();
      if (ParameterValidation.existsAndIsNotEmpty(userProfileData))
      {
         initial.setSupportedUserProfileData(userProfileData);
      }

      List<String> userScopes = getSupportedUserScopes();
      if (ParameterValidation.existsAndIsNotEmpty(userScopes))
      {
         initial.setSupportedUserScopes(userScopes);
      }

      return initial;
   }

   @Override
   public Class<ConsumerCapabilities> getModelClass()
   {
      return ConsumerCapabilities.class;
   }
}
