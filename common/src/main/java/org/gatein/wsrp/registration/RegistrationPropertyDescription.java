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

package org.gatein.wsrp.registration;

import org.gatein.common.util.ParameterValidation;

import javax.xml.namespace.QName;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Locale;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision:5865 $
 * @since 2.6
 */
public class RegistrationPropertyDescription implements PropertyDescription
{
   public static final ParameterValidation.ValidationErrorHandler HANDLER = new ParameterValidation.ValidationErrorHandler(null)
   {
      @Override
      protected String internalValidationErrorHandling(String failedValue)
      {
         throw new IllegalArgumentException(failedValue + " is not a valid RegistrationPropertyDescription name");
      }
   };
   private String key;
   private QName name;
   private QName type;
   private String schemaLocation;
   private LocalizedString description;
   private LocalizedString hint;
   private LocalizedString label;
   private String[] usages;
   private QName[] aliases;

   private transient ValueChangeListener valueChangeListener;

   public RegistrationPropertyDescription(QName name, QName type)
   {
      this.name = name;
      this.type = type;
   }

   public RegistrationPropertyDescription(String name, QName type)
   {
      this(new QName(name), type);
   }

   public RegistrationPropertyDescription()
   {
   }

   public RegistrationPropertyDescription(RegistrationPropertyDescription other)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(other, "RegistrationPropertyDescription");

      setName(QName.valueOf(other.name.toString()));
      setType(QName.valueOf(other.type.toString()));

      if (other.aliases != null)
      {
         aliases = new QName[other.aliases.length];
         System.arraycopy(other.aliases, 0, aliases, 0, other.aliases.length);
      }

      if (other.description != null)
      {
         setDescription(new LocalizedString(other.description));
      }
      if (other.hint != null)
      {
         setHint(new LocalizedString(other.hint));
      }
      if (other.label != null)
      {
         setLabel(new LocalizedString(other.label));
      }
      if (other.schemaLocation != null)
      {
         setSchemaLocation(other.schemaLocation);
      }

      if (other.usages != null)
      {
         usages = new String[other.usages.length];
         System.arraycopy(other.usages, 0, usages, 0, other.usages.length);
      }

//      valueChangeListener = other.valueChangeListener;
   }


   @Override
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

      RegistrationPropertyDescription that = (RegistrationPropertyDescription)o;

      return name.equals(that.name) && type.equals(that.type);
   }

   @Override
   public int hashCode()
   {
      int result = name.hashCode();
      result = 31 * result + type.hashCode();
      return result;
   }

   public String toString()
   {
      return "Registration Property Description named '" + name +
         "', type=" + type +
         ", hint=" + hint +
         ", label=" + label;
   }

   public int compareTo(PropertyDescription o)
   {
      return getName().toString().compareTo(o.getName().toString());
   }

   public String getPersistentKey()
   {
      return key;
   }

   public void setPersistentKey(String key)
   {
      this.key = key;
   }

   public QName getName()
   {
      return name;
   }

   public void setName(QName name)
   {
      if (ParameterValidation.isOldAndNewDifferent(this.name, name))
      {
         QName oldName = this.name;
         this.name = name;
         if (valueChangeListener != null)
         {
            valueChangeListener.valueHasChanged(this, oldName, name, true);
         }
      }
   }

   public void setNameAsString(String name)
   {
      final String sanitizedName = ParameterValidation.sanitizeFromPatternWithHandler(name, ParameterValidation.XSS_CHECK, HANDLER);
      setName(new QName(sanitizedName));
   }

   public String getNameAsString()
   {
      return getName().getLocalPart();
   }

   public QName getType()
   {
      return type;
   }

   public void setType(QName type)
   {
      this.type = (QName)modifyIfNeeded(this.type, type);
   }

   public String getSchemaLocation()
   {
      return schemaLocation;
   }

   public URI getSchemaLocationAsURI()
   {
      try
      {
         return new URI(schemaLocation);
      }
      catch (URISyntaxException e)
      {
         // shouldn't happen
         throw new IllegalArgumentException("Invalid URI: " + schemaLocation + ". Cause: " + e.getLocalizedMessage());
      }
   }

   public void setSchemaLocation(String schemaLocation)
   {
      // 
      if (schemaLocation != null)
      {
         // first check if schemaLocation is a valid URI
         try
         {
            new URI(schemaLocation);
         }
         catch (URISyntaxException e)
         {
            throw new IllegalArgumentException("Invalid URI: " + schemaLocation + ". Cause: " + e.getLocalizedMessage());
         }

         this.schemaLocation = (String)modifyIfNeeded(this.schemaLocation, schemaLocation);
      }
   }

   public LocalizedString getDescription()
   {
      return description;
   }

   public void setDescription(LocalizedString description)
   {
      this.description = (LocalizedString)modifyIfNeeded(this.description, description);
   }

   public void setDefaultDescription(String value)
   {
      setDescription(value == null ? null : new LocalizedString(value));
   }

   public LocalizedString getHint()
   {
      return hint;
   }

   public void setHint(LocalizedString hint)
   {
      this.hint = (LocalizedString)modifyIfNeeded(this.hint, hint);
   }

   public void setDefaultHint(String value)
   {
      setHint(value == null ? null : new LocalizedString(value));
   }

   public LocalizedString getLabel()
   {
      return label;
   }

   public void setLabel(LocalizedString label)
   {
      this.label = (LocalizedString)modifyIfNeeded(this.label, label);
   }

   public void setDefaultLabel(String value)
   {
      setLabel(value == null ? null : new LocalizedString(value));
   }

   public String[] getUsages()
   {
      return usages;
   }

   public void setUsages(String[] usages)
   {
      if (!Arrays.equals(this.usages, usages))
      {
         notifyParentOfChangeIfNeeded(this.usages, usages);
         if (usages != null)
         {
            this.usages = new String[usages.length];
            System.arraycopy(usages, 0, this.usages, 0, usages.length);
         }
         else
         {
            this.usages = null;
         }
      }
   }

   public QName[] getAliases()
   {
      return aliases;
   }

   public void setAliases(QName[] aliases)
   {
      if (!Arrays.equals(this.aliases, aliases))
      {
         notifyParentOfChangeIfNeeded(this.aliases, aliases);
         if (aliases != null)
         {
            this.aliases = new QName[aliases.length];
            System.arraycopy(aliases, 0, this.aliases, 0, aliases.length);
         }
         else
         {
            this.aliases = null;
         }
      }
   }

   public void setValueChangeListener(ValueChangeListener listener)
   {
      this.valueChangeListener = listener;
   }

   public ValueChangeListener getValueChangeListener()
   {
      return valueChangeListener;
   }

   private void notifyParentOfChangeIfNeeded(Object oldValue, Object newValue)
   {
      if (valueChangeListener != null)
      {
         valueChangeListener.valueHasChanged(this, oldValue, newValue, false);
      }
   }

   public Object modifyIfNeeded(Object oldValue, Object newValue)
   {
      if (ParameterValidation.isOldAndNewDifferent(oldValue, newValue))
      {
         notifyParentOfChangeIfNeeded(oldValue, newValue);
         oldValue = newValue;
      }

      return oldValue;
   }

   /**
    * Tries to heuristically determine the language for this RegistrationPropertyDescription
    *
    * @return
    */
   public Locale getLang()
   {
      Locale defaultLocale = Locale.getDefault();
      Locale locale;

      locale = label != null ? label.getLocale() : defaultLocale;
      if (!defaultLocale.equals(locale))
      {
         return locale;
      }

      locale = hint != null ? hint.getLocale() : defaultLocale;
      if (!defaultLocale.equals(locale))
      {
         return locale;
      }

      locale = description != null ? description.getLocale() : defaultLocale;
      if (!defaultLocale.equals(locale))
      {
         return locale;
      }

      return defaultLocale;
   }
}
