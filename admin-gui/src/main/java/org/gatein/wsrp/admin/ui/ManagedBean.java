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

import org.gatein.common.util.ParameterValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.model.SelectItem;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 13413 $
 * @since 2.6
 */
public abstract class ManagedBean implements Serializable
{
   protected transient Logger log = LoggerFactory.getLogger(getClass());

   protected BeanContext beanContext;

   private String cancelOutcome;

   public static final String INVALID_NAME = "INVALID_NAME_ERROR";
   public static final String INVALID_PATH = "INVALID_PATH_ERROR";
   public static final String DUPLICATE = "DUPLICATE_ERROR";

   public static interface PropertyValidator extends Serializable
   {
      boolean checkForDuplicates();

      String getObjectTypeName();

      boolean isAlreadyExisting(String propertyName);

      String doSimpleChecks(String name);

      ParameterValidation.ValidationErrorHandler getValidationErrorHandler(String name, String targetForErrorMessage);

      Pattern getValidationPattern();
   }

   private PropertyValidator validator = new DefaultPropertyValidator();

   protected void setValidator(PropertyValidator validator)
   {
      this.validator = validator;
   }

   public void setBeanContext(BeanContext beanContext)
   {
      this.beanContext = beanContext;
   }

   public void setCancelOutcome(String cancelOutcome)
   {
      this.cancelOutcome = cancelOutcome;
   }

   public String checkNameValidity(String name, String targetForErrorMessage)
   {
      return checkNameValidity(name, targetForErrorMessage, validator);
   }

   public String checkNameValidity(String name, String targetForErrorMessage, PropertyValidator validator)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(validator, "PropertyValidator");

      String objectTypeName = validator.getObjectTypeName();
      if (ParameterValidation.isNullOrEmpty(name))
      {
         beanContext.createTargetedErrorMessage(targetForErrorMessage, INVALID_NAME, name, getLocalizedType(objectTypeName));
         return null;
      }
      else
      {
         String original = name;
         name = validator.doSimpleChecks(name);

         // we got an invalid name after simple checks, fail!
         if (name == null)
         {
            beanContext.createTargetedErrorMessage(targetForErrorMessage, INVALID_NAME, original, getLocalizedType(objectTypeName));
            return null;
         }

         // Trim name
         name = name.trim();

         // "sanitize" name: if it's invalid, return null and output message
         name = ParameterValidation.sanitizeFromPatternWithHandler(name, validator.getValidationPattern(),
            validator.getValidationErrorHandler(name, targetForErrorMessage));

         // we got an invalid name, fail!
         if (name == null)
         {
            return null;
         }

         // Check for duplicate
         if (validator.checkForDuplicates() && validator.isAlreadyExisting(name))
         {
            getDuplicateErrorMessage(name, targetForErrorMessage, objectTypeName);
            return null;
         }

         return name;
      }
   }

   protected void getDuplicateErrorMessage(String name, String targetForErrorMessage, String objectTypeName)
   {
      beanContext.createTargetedErrorMessage(targetForErrorMessage, DUPLICATE, name, getLocalizedType(objectTypeName));
   }

   private String getLocalizedType(String objectTypeName)
   {
      return beanContext.getMessageFromBundle(objectTypeName);
   }

   protected abstract String getObjectTypeName();

   public abstract boolean isAlreadyExisting(String objectName);

   /**
    * @param oldValue
    * @param newValue
    * @return
    * @todo public for test cases
    */
   public boolean isOldAndNewDifferent(Object oldValue, Object newValue)
   {
      oldValue = normalizeStringIfNeeded(oldValue);
      newValue = normalizeStringIfNeeded(newValue);

      return (oldValue != null && !oldValue.equals(newValue)) || (oldValue == null && newValue != null);
   }

   /**
    * Normalizes String by considering empty String as null as JSF would give either and trim non-null Strings.
    *
    * @param value
    * @return
    * @todo public for test cases
    */
   public Object normalizeStringIfNeeded(Object value)
   {
      if (value == null)
      {
         return null;
      }
      else
      {
         if (value instanceof String)
         {
            String stringValue = (String)value;
            return stringValue.length() == 0 ? null : stringValue.trim();
         }
         else
         {
            return value;
         }
      }
   }

   protected class MessageValidationHandler extends ParameterValidation.ValidationErrorHandler
   {
      private String targetForErrorMessage;
      private String validatedName;
      private String objectTypeName;
      private String errorMessageKey;

      public MessageValidationHandler(String defaultValue, String targetForErrorMessage, String validatedName, String objectTypeName)
      {
         this(defaultValue, targetForErrorMessage, validatedName, objectTypeName, INVALID_NAME);
      }

      public MessageValidationHandler(String defaultValue, String targetForErrorMessage, String validatedName, String objectTypeName, String errorMessageKey)
      {
         super(defaultValue);
         this.targetForErrorMessage = targetForErrorMessage;
         this.validatedName = validatedName;
         this.objectTypeName = objectTypeName;
         this.errorMessageKey = errorMessageKey;
      }

      protected String internalValidationErrorHandling(String s)
      {
         beanContext.createTargetedErrorMessage(targetForErrorMessage, errorMessageKey, validatedName, getLocalizedType(objectTypeName));
         return null;
      }
   }

   protected class DefaultPropertyValidator implements PropertyValidator
   {
      public boolean checkForDuplicates()
      {
         return true;
      }

      public String getObjectTypeName()
      {
         return ManagedBean.this.getObjectTypeName();
      }

      public boolean isAlreadyExisting(String propertyName)
      {
         return ManagedBean.this.isAlreadyExisting(propertyName);
      }

      public String doSimpleChecks(String name)
      {
         // if name contains . or /, it's invalid for a Portal object
         return (name.indexOf('.') != -1 || name.indexOf('/') != -1) ? null : name;
      }

      public ParameterValidation.ValidationErrorHandler getValidationErrorHandler(String name, String targetForErrorMessage)
      {
         return new MessageValidationHandler(null, targetForErrorMessage, name, getObjectTypeName());
      }

      public Pattern getValidationPattern()
      {
         return ParameterValidation.XSS_CHECK;
      }
   }

   /**
    * Default action: returns to outcome specified by {@link #setCancelOutcome(String)}
    *
    * @return
    */
   public String cancel()
   {
      return cancelOutcome;
   }

   protected static List<SelectItem> getSelectItemsFrom(List<String> identifiers)
   {
      List<SelectItem> result = new ArrayList<SelectItem>(identifiers.size());
      for (String pageIdentifier : identifiers)
      {
         result.add(new SelectItem(pageIdentifier));
      }
      return result;
   }
}
