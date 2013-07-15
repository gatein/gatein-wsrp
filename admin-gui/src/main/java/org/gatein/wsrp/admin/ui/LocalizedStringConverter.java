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

package org.gatein.wsrp.admin.ui;

import org.gatein.wsrp.registration.LocalizedString;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

/**
 * Converts a {@link LocalizedString} into a JSF-displayable object and back.
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 9153 $
 * @since 2.6.3
 */
public class LocalizedStringConverter implements Converter
{
   public Object getAsObject(FacesContext facesContext, UIComponent uiComponent, String s)
   {
      return (s == null || s.length() == 0) ? null : new LocalizedString(s);
   }

   public String getAsString(FacesContext facesContext, UIComponent uiComponent, Object o)
   {
      return getAsString(o);
   }

   static String getAsString(Object localizedString)
   {
      return localizedString == null ? null : ((LocalizedString)localizedString).getValue();
   }
}
