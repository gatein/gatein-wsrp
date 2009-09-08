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

package org.gatein.wsrp.producer;

import org.gatein.pc.api.Mode;
import org.gatein.pc.api.WindowState;
import org.gatein.pc.api.Portlet;
import org.gatein.wsrp.WSRPUtils;
import org.oasis.wsrp.v1.MarkupType;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Wrapper around information needed to perform a Markup invocation.
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @since 2.6
 */
class MarkupRequest
{
   private String mode;
   private String windowState;
   private MarkupType markupType;
   private String characterSet;
   private Portlet portlet;
   private static final String CHARSET_SEPARATOR = "; charset=";

   public MarkupRequest(MarkupType markupType, String mode, String windowState, String characterSet, Portlet portlet)
   {
      this.characterSet = characterSet;
      this.markupType = markupType;
      this.mode = mode;
      this.windowState = windowState;
      this.portlet = portlet;
   }

   public String getMediaTypeWithCharset()
   {
      return getMediaType() + CHARSET_SEPARATOR + getCharacterSet();
   }

   public String getMediaType()
   {
      return markupType.getMimeType();
   }

   public String getLocale()
   {
      List<String> locales = markupType.getLocales();
      if (locales != null && !locales.isEmpty())
      {
         return locales.get(0);
      }
      else
      {
         return WSRPUtils.toString(Locale.ENGLISH); // no locale was provided, use English...
      }
   }

   public String getMode()
   {
      return mode;
   }

   public String getWindowState()
   {
      return windowState;
   }

   public MarkupType getMarkupType()
   {
      return markupType;
   }

   public String getCharacterSet()
   {
      return characterSet;
   }

   public Portlet getPortlet()
   {
      return portlet;
   }

   public Set<Mode> getSupportedModes()
   {
      List<String> modes = markupType.getModes();
      Set<Mode> result = new HashSet<Mode>(modes.size());
      for (String mode : modes)
      {
         result.add(WSRPUtils.getJSR168PortletModeFromWSRPName(mode));
      }
      return result;
   }

   public Set<WindowState> getSupportedWindowStates()
   {
      List<String> states = markupType.getWindowStates();
      Set<WindowState> result = new HashSet<WindowState>(states.size());
      for (String state : states)
      {
         result.add(WSRPUtils.getJSR168WindowStateFromWSRPName(state));
      }
      return result;
   }
}
