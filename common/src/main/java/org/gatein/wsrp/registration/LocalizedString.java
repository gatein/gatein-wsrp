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

package org.gatein.wsrp.registration;

import org.gatein.common.util.ParameterValidation;

import java.util.Locale;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision:5865 $
 * @since 2.6
 */
public class LocalizedString
{
   private String value;
   private Locale locale;
   private String resourceName;


   public LocalizedString(String value, Locale locale)
   {
      setValue(value);
      this.locale = locale;
   }


   public LocalizedString(String value)
   {
      this(value, Locale.getDefault());
   }

   public LocalizedString()
   {
   }

   public LocalizedString(LocalizedString other)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(other, "LocalizedString");
      this.value = other.value;
      this.locale = other.locale;
      this.resourceName = other.resourceName;
   }


   public String toString()
   {
      return "LocalizedString{" +
         "value='" + value + '\'' +
         ", locale=" + locale +
         ", resourceName='" + resourceName + '\'' +
         '}';
   }

   public boolean equals(Object o)
   {
      if (this == o)
      {
         return true;
      }
      if (o == null || getClass() != o.getClass())
      {
         return false;
      }

      LocalizedString that = (LocalizedString)o;

      if (!locale.equals(that.locale))
      {
         return false;
      }
      if (resourceName != null ? !resourceName.equals(that.resourceName) : that.resourceName != null)
      {
         return false;
      }
      if (value != null ? !value.equals(that.value) : that.value != null)
      {
         return false;
      }

      return true;
   }

   public int hashCode()
   {
      int result;
      result = (value != null ? value.hashCode() : 0);
      result = 31 * result + locale.hashCode();
      result = 31 * result + (resourceName != null ? resourceName.hashCode() : 0);
      return result;
   }

   public String getValue()
   {
      return value;
   }

   public void setValue(String value)
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(value, "value", "LocalizedString");
      this.value = value;
   }

   public Locale getLocale()
   {
      return locale;
   }

   public void setLocale(Locale locale)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(locale, "locale");
      this.locale = locale;
   }

   public String getResourceName()
   {
      return resourceName;
   }

   public void setResourceName(String resourceName)
   {
      this.resourceName = resourceName;
   }
}
