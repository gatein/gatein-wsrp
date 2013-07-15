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

import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


/**
 * Provides base behavior for WSRP admin UI beans.
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 13413 $
 * @since 2.6
 */
public abstract class WSRPManagedBean implements Serializable
{
   protected transient Logger log = LoggerFactory.getLogger(getClass());

   /** The BeanContext associated with this bean */
   protected BeanContext beanContext;

   /** The name of the action associated with a cancel operation for this bean, i.e. which view should we redirect to if the user cancels the current view. */
   private String cancelOutcome;

   public static final String INVALID_NAME = "INVALID_NAME_ERROR";
   public static final String INVALID_PATH = "INVALID_PATH_ERROR";
   public static final String DUPLICATE = "DUPLICATE_ERROR";

   static void bypassAndRedisplay()
   {
      // bypass the rest of the life cycle and re-display page
      FacesContext.getCurrentInstance().renderResponse();
   }

   /** Provides an API to validate input properties. */
   public interface PropertyValidator extends Serializable
   {
      /**
       * Determines whether this PropertyValidator checks for duplicated values.
       *
       * @return <code>true</code> if this PropertyValidator checks for duplicated values, <code>false</code> otherwise
       */
      boolean checkForDuplicates();

      /**
       * Retrieves the name of the type of objects this PropertyValidator can validate.
       *
       * @return
       */
      String getTypeOfValidatedValues();

      /**
       * Determines whether the specified property value already exists.
       *
       * @param value
       * @return
       */
      boolean isAlreadyExisting(String value);

      /**
       * Performs a quick check that the specified value doesn't contain any invalid characters to be able to fail fast before performing a full regex validation.
       *
       * @param value the value we want to check
       * @return the validated value or <code>null</code> if it doesn't conform to the expected format
       */
      String checkForInvalidCharacters(String value);

      /**
       * Retrieves the {@link org.gatein.common.util.ParameterValidation.ValidationErrorHandler} associated with this PropertyValidator. Allows implementations to vary how error
       * messages are presented to client code, depending on the context they run in.
       *
       * @param name                  the name of the property being validated
       * @param targetForErrorMessage the UI component target for the potential error message
       * @return
       */
      ParameterValidation.ValidationErrorHandler getValidationErrorHandler(String name, String targetForErrorMessage);

      /**
       * Retrieves the regular expression this PropertyValidator uses to validate values.
       *
       * @return
       */
      Pattern getValidationPattern();

      /**
       * Retrieves the key associated to the error message in the localization resource bundle.
       *
       * @return
       */
      String getErrorKey();
   }

   private PropertyValidator validator = new DefaultPropertyValidator();

   protected void setValidator(PropertyValidator validator)
   {
      this.validator = validator;
   }

   protected PropertyValidator getValidator()
   {
      return validator;
   }

   public void setBeanContext(BeanContext beanContext)
   {
      this.beanContext = beanContext;
   }

   public void setCancelOutcome(String cancelOutcome)
   {
      this.cancelOutcome = cancelOutcome;
   }

   public String checkAndReturnValueIfValid(String value, String targetForErrorMessage)
   {
      return checkAndReturnValueIfValid(value, targetForErrorMessage, validator);
   }

   /**
    * Asks the specified PropertyValidator to check the validity of the specified value, using the specified target for error messages
    *
    * @param value                 the value to be validated
    * @param targetForErrorMessage the name of the UI component being targeted when an error message needs to be displayed
    * @param validator             the PropertyValidator that will validate the values
    * @return the validated value or <code>null</code> otherwise
    */
   public String checkAndReturnValueIfValid(String value, String targetForErrorMessage, PropertyValidator validator)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(validator, "PropertyValidator");

      final String objectTypeName = validator.getTypeOfValidatedValues();
      if (ParameterValidation.isNullOrEmpty(value))
      {
         beanContext.createTargetedErrorMessage(targetForErrorMessage, validator.getErrorKey(), value, getLocalizedType(objectTypeName));
         return null;
      }
      else
      {
         String original = value;
         value = validator.checkForInvalidCharacters(value);

         // we got an invalid value after quickly checking for invalid characters, fail!
         if (value == null)
         {
            beanContext.createTargetedErrorMessage(targetForErrorMessage, validator.getErrorKey(), original, getLocalizedType(objectTypeName));
            return null;
         }

         // Trim value
         value = value.trim();

         // "sanitize" value: if it's invalid, return null and output message
         value = ParameterValidation.sanitizeFromPatternWithHandler(value, validator.getValidationPattern(), validator.getValidationErrorHandler(value, targetForErrorMessage));

         // we got an invalid value, fail!
         if (value == null)
         {
            return null;
         }

         // Check for duplicate
         if (validator.checkForDuplicates() && validator.isAlreadyExisting(value))
         {
            getDuplicateErrorMessage(value, targetForErrorMessage, objectTypeName);
            return null;
         }

         return value;
      }
   }

   /**
    * Retrieves the localized error message to display when the specified value is a duplicate of an already existing one.
    *
    * @param value
    * @param targetForErrorMessage
    * @param objectTypeName
    */
   protected void getDuplicateErrorMessage(String value, String targetForErrorMessage, String objectTypeName)
   {
      beanContext.createTargetedErrorMessage(targetForErrorMessage, DUPLICATE, value, getLocalizedType(objectTypeName));
   }

   /**
    * Retrieves the localized description for the specified object type name (type of validated values).
    *
    * @param objectTypeName
    * @return
    */
   private String getLocalizedType(String objectTypeName)
   {
      return beanContext.getMessageFromBundle(objectTypeName);
   }

   /**
    * Retrieves the name of the type of objects this bean deals with.
    *
    * @return
    */
   protected abstract String getObjectTypeName();

   /**
    * Checks whether the specified value (understood as an identifier for entities this bean manages) is already known to this bean and therefore would consist of a duplicated
    * value.
    *
    * @param objectName
    * @return
    */
   public abstract boolean isAlreadyExisting(String objectName);

   /**
    * Determines whether the specified old and new values are different after being normalized.
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

      public String getTypeOfValidatedValues()
      {
         // delegate to the enclosing bean
         return WSRPManagedBean.this.getObjectTypeName();
      }

      public boolean isAlreadyExisting(String value)
      {
         // delegate to the enclosing bean
         return WSRPManagedBean.this.isAlreadyExisting(value);
      }

      /**
       * Checks whether the specified value contains '.' or '/'.
       *
       * @param value the value which format we want to check
       * @return
       */
      public String checkForInvalidCharacters(String value)
      {
         // if name contains . or /, it's invalid for a Portal object
         return (value.indexOf('.') != -1 || value.indexOf('/') != -1) ? null : value;
      }

      public ParameterValidation.ValidationErrorHandler getValidationErrorHandler(String name, String targetForErrorMessage)
      {
         return new MessageValidationHandler(null, targetForErrorMessage, name, getTypeOfValidatedValues());
      }

      public Pattern getValidationPattern()
      {
         return ParameterValidation.XSS_CHECK;
      }

      @Override
      public String getErrorKey()
      {
         return INVALID_NAME;
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
