/*
 * JBoss, a division of Red Hat
 * Copyright 2010, Red Hat Middleware, LLC, and individual
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

package org.gatein.wsrp.consumer;

import org.gatein.common.util.ParameterValidation;
import org.gatein.wsrp.registration.RegistrationPropertyDescription;

import static org.gatein.wsrp.consumer.RegistrationProperty.Status.*;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 12019 $
 * @since 2.6
 */
public class RegistrationProperty implements Comparable<RegistrationProperty>
{
   private String persistentId;
   private RegistrationPropertyDescription persistentDescription;
   private String persistentLang;
   private String persistentName;
   private String persistentValue;
   private Status status;

   private transient PropertyChangeListener listener;

   public int compareTo(RegistrationProperty o)
   {
      return persistentName.compareTo(o.persistentName);
   }

   public enum Status
   {
      INEXISTENT("registration_property_status_inexistent"),
      MISSING("registration_property_status_missing"),
      MISSING_VALUE("registration_property_status_missing_value"),
      UNCHECKED_VALUE("registration_property_status_unchecked_value"),
      INVALID_VALUE("registration_property_status_invalid_value"),
      VALID("registration_property_status_valid");

      Status(String localizationKey)
      {
         this.localizationKey = localizationKey;
      }

      public String getLocalizationKey()
      {
         return localizationKey;
      }

      private String localizationKey;
   }

   public RegistrationProperty()
   {
   }

   public RegistrationProperty(String name, String stringValue, String lang, PropertyChangeListener listener)
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(name, "Name", "RegistrationProperty");
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(lang, "Lang", "RegistrationProperty");
      ParameterValidation.throwIllegalArgExceptionIfNull(listener, "listener");
      this.persistentName = name;
      this.persistentLang = lang;
      this.listener = listener;
      setValue(stringValue);
   }

   @Override
   public boolean equals(Object o)
   {
      if (this == o)
      {
         return true;
      }
      if (!(o instanceof RegistrationProperty))
      {
         return false;
      }

      RegistrationProperty that = (RegistrationProperty)o;

      if (persistentId != null ? !persistentId.equals(that.persistentId) : that.persistentId != null)
      {
         return false;
      }
      if (!persistentName.equals(that.persistentName))
      {
         return false;
      }
      return !(persistentValue != null ? !persistentValue.equals(that.persistentValue) : that.persistentValue != null);

   }

   @Override
   public int hashCode()
   {
      int result;
      result = (persistentId != null ? persistentId.hashCode() : 0);
      result = 31 * result + persistentName.hashCode();
      result = 31 * result + (persistentValue != null ? persistentValue.hashCode() : 0);
      return result;
   }

   public String getPersistentKey()
   {
      return persistentId;
   }

   public void setPersistentKey(String key)
   {
      this.persistentId = key;
   }

   public String getName()
   {
      return persistentName;
   }

   public void setName(String name)
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(name, "Name", "RegistrationProperty");
      this.persistentName = name;
   }

   public String getValue()
   {
      return persistentValue;
   }

   public RegistrationPropertyDescription getDescription()
   {
      return persistentDescription;
   }

   public void setDescription(RegistrationPropertyDescription description)
   {
      this.persistentDescription = description;
   }

   public Boolean isInvalid()
   {
      if (UNCHECKED_VALUE.equals(status))
      {
         return null;
      }
      else
      {
         return !VALID.equals(status);
      }
   }

   public boolean isDeterminedInvalid()
   {
      return !VALID.equals(status) && !UNCHECKED_VALUE.equals(status);
   }

   public void setValue(String stringValue)
   {
      // only change the value if it's not the same as the old one
      if ((persistentValue != null && !persistentValue.equals(stringValue)) || (persistentValue == null && stringValue != null))
      {
         String oldValue = persistentValue;
         Status oldStatus = status;
         persistentValue = stringValue;
         if (persistentValue == null)
         {
            status = MISSING_VALUE;
         }
         else
         {
            status = UNCHECKED_VALUE;
         }

         // notify listeners
         notifyListener(oldValue, persistentValue, oldStatus);
      }
   }

   public String getLang()
   {
      return persistentLang;
   }

   public void setLang(String lang)
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(lang, "Lang", "RegistrationProperty");
      this.persistentLang = lang;
   }

   public Status getStatus()
   {
      return status;
   }

   public void setStatus(Status status)
   {
      this.status = status;
   }

   private void notifyListener(String oldValue, String newValue, Status oldStatus)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(listener, "PropertyChangeListener");
      listener.propertyValueChanged(this, oldStatus, oldValue, newValue);
   }

   public void setListener(PropertyChangeListener listener)
   {
      this.listener = listener;
   }

   static interface PropertyChangeListener
   {
      /**
       * Only called if an actual change occurred, i.e. oldvalue is guaranteed to be different from newValue
       *
       * @param property
       * @param previousStatus
       * @param oldValue
       * @param newValue
       */
      void propertyValueChanged(RegistrationProperty property, Status previousStatus, Object oldValue, Object newValue);
   }
}
