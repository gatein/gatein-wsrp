/*
 * JBoss, a division of Red Hat
 * Copyright 2009, Red Hat Middleware, LLC, and individual
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

package org.gatein.registration.impl;

import org.gatein.pc.api.Mode;
import org.gatein.pc.api.WindowState;
import org.gatein.registration.ConsumerCapabilities;

import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 8784 $
 * @since 2.6
 */
public class ConsumerCapabilitiesImpl implements ConsumerCapabilities
{
   private boolean supportsGetMethod;
   private List<Mode> supportedModes;
   private List<WindowState> supportedWindowStates;
   private List<String> supportedUserScopes;
   private List<String> supportedUserProfileData;

   public boolean supportsGetMethod()
   {
      return supportsGetMethod;
   }

   public List<Mode> getSupportedModes()
   {
      return supportedModes;
   }

   public List<WindowState> getSupportedWindowStates()
   {
      return supportedWindowStates;
   }

   public List<String> getSupportedUserScopes()
   {
      return supportedUserScopes;
   }

   public List<String> getSupportedUserProfileData()
   {
      return supportedUserProfileData;
   }

   public void setSupportsGetMethod(boolean supportsGetMethod)
   {
      this.supportsGetMethod = supportsGetMethod;
   }

   public void setSupportedModes(List<Mode> supportedModes)
   {
      this.supportedModes = supportedModes;
   }

   public void setSupportedWindowStates(List<WindowState> supportedWindowStates)
   {
      this.supportedWindowStates = supportedWindowStates;
   }

   public void setSupportedUserScopes(List<String> supportedUserScopes)
   {
      this.supportedUserScopes = supportedUserScopes;
   }

   public void setSupportedUserProfileData(List<String> supportedUserProfileData)
   {
      this.supportedUserProfileData = supportedUserProfileData;
   }
}
