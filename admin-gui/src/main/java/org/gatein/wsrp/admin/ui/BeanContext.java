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

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 13374 $
 * @since 2.6
 */
public abstract class BeanContext implements Serializable
{
   protected final static Logger log = LoggerFactory.getLogger(BeanContext.class);

   public static final String STATUS = "status";
   static final String DEFAULT_RESOURCE_NAME = "locale.portlet.Resource";
   private static final String UNEXPECTED_ERROR = "bean_support_unexpected_error";
   private static final String CAUSE = "bean_support_cause";
   private static final String CURRENT_PLACEHOLDER = "###";

   private String resourceName = DEFAULT_RESOURCE_NAME;

   public void setResourceName(String resourceName)
   {
      this.resourceName = resourceName;
   }

   public String getResourceName()
   {
      return resourceName;
   }

   /**
    * Retrieves the value of the parameter which name is given from the JSF request.
    *
    * @param key name of the parameter which value we want to retrieve
    * @return
    */
   public abstract String getParameter(String key);

   /**
    * @param target
    * @param message
    * @param severity
    * @param additionalParams
    */
   protected abstract void createMessage(String target, String message, Object severity, Object... additionalParams);

   protected abstract Object getErrorSeverity();

   protected abstract Object getInfoSeverity();

   protected abstract Locale getLocale();

   public abstract String getServerAddress();

   public void createErrorMessage(String localizedMessageId, Object... params)
   {
      createLocalizedMessage(STATUS, localizedMessageId, getErrorSeverity(), params);
   }

   public void createTargetedErrorMessage(String target, String localizedMessageId, Object... params)
   {
      createLocalizedMessage(target, localizedMessageId, getErrorSeverity(), params);
   }

   /**
    * Adds a localized message using the appropriate severity to the identified target in the context. This method
    * accepts an arbitrary number of arguments to be passed as parameters of localized strings.
    *
    * @param target             the target in this context that will receive the new message
    * @param localizedMessageId a resource bundle identifier identifying which the localized string to use as a message
    * @param severity           an object representing the severity of the message (typically FacesMessage.Severity)
    * @param params             additional parameters to be passed to replace tokens in localized strings
    */
   protected void createLocalizedMessage(String target, String localizedMessageId, Object severity, Object... params)
   {
      createMessage(target, getMessageFromBundle(localizedMessageId, params), severity);
   }

   public String getMessageFromBundle(String localizedMessageId, Object... params)
   {
      return getLocalizedMessage(localizedMessageId, getLocale(), resourceName, params);
   }

   public static String getLocalizedMessage(String localizationKey, Locale locale, Object... params)
   {
      return getLocalizedMessage(localizationKey, locale, DEFAULT_RESOURCE_NAME, params);
   }

   public static String getLocalizedMessage(String localizationKey, Locale locale, String resourceName, Object... params)
   {
      ResourceBundle rb = ResourceBundle.getBundle(resourceName, locale);

      String message;
      try
      {
         message = rb.getString(localizationKey);
      }
      catch (MissingResourceException e)
      {
         // if the key doesn't exist, return it instead of failing
         log.info("Couldn't find localization message for key '" + localizationKey + "' in bundle " + resourceName
            + " for locale " + locale.getDisplayName());
         return localizationKey;
      }

      return MessageFormat.format(message, params);
   }

   public void createErrorMessageFrom(Exception e)
   {
      createErrorMessageFrom(STATUS, e);
   }

   /**
    * Creates a localized error message targeting the specified object in the context and using the specified error
    * information. This method looks for two specific resource bundle entries to localize the message, {@link
    * #UNEXPECTED_ERROR} and {@link #CAUSE}, using the following format for the message: <code>result of {@link
    * #getLocalizedMessageOrExceptionName(Throwable)} for the exception\n[localized value associated with {@link
    * #CAUSE}result of {@link #getLocalizedMessageOrExceptionName(Throwable)} for the exception's cause if the cause
    * exists]
    *
    * @param target the contextual object target by the message to be created
    * @param e      the exception that we want to display as an error message
    */
   public void createErrorMessageFrom(String target, Exception e)
   {
//      Throwable cause = e.getCause();
      String localizedMessage = getLocalizedMessageOrExceptionName(e);
//      String message = localizedMessage + (cause != null ? "\n" + getMessageFromBundle(CAUSE) + getLocalizedMessageOrExceptionName(cause) : "");
      createMessage(target, localizedMessage, getErrorSeverity(), e.getCause());
   }

   /**
    * Retrieves a localized message associated with the specified Throwable.
    *
    * @param e the Throwable for which a localized message is to be retrieved
    * @return the localized message associated with the specified Throwable if it exists or the localized value
    *         associated with the {@link #UNEXPECTED_ERROR} resource bundle entry to which is appended the Throwable
    *         class name.
    */
   private String getLocalizedMessageOrExceptionName(Throwable e)
   {
      String localizedMessage = e.getLocalizedMessage();
      if (localizedMessage == null)
      {
         localizedMessage = getMessageFromBundle(UNEXPECTED_ERROR) + e.getClass().getName();
      }
      return localizedMessage;
   }

   protected void createInfoMessage(String target, String localizedMessageId)
   {
      createLocalizedMessage(target, localizedMessageId, getInfoSeverity());
   }

   public void createInfoMessage(String localizedMessageId)
   {
      createInfoMessage(STATUS, localizedMessageId);
   }

   /**
    * Removes the object identified by the specified name(s) from the session. For a JSF backed implementation, this
    * will allow for the object/bean (defined as session-scoped in <code>faces-config.xml</code>) to be recreated by
    * JSF
    * when needed.
    *
    * @param name       name of the object to be removed
    * @param otherNames additional names of objects to be removed
    */
   public void removeFromSession(String name, String... otherNames)
   {
      Map<String, Object> sessionMap = getSessionMap();
      sessionMap.remove(name);
      if (otherNames != null)
      {
         for (String other : otherNames)
         {
            sessionMap.remove(other);
         }
      }
   }

   /**
    * Retrieves the session map where "session" is a concept left up to implementations (for JSF, the session
    * corresponds quite logically to the HTTP session)
    *
    * @return the session map
    */
   public abstract Map<String, Object> getSessionMap();

   /**
    * Replaces the session object identified by the given name by the specified new one. Passing <code>null</code> for
    * the new value will remove the object reference from the session. If an object was previously assigned to this
    * name, then only an object of the same type (as defined by {@link Class#isAssignableFrom(Class)}) can be assigned
    * to this name.
    *
    * @param name     the name identifying the object to be replaced
    * @param newValue the new value for the object to be replaced or <code>null</code> if the object is to be removed
    * @param <T>      the type of the object to be replaced
    * @return the new value for the object or <code>null</code> if the remove semantics is used
    * @throws IllegalArgumentException if the new value for the identified object is not compatible with the currently
    *                                  stored value
    */
   public <T> T replaceInSession(String name, T newValue)
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(name, "name", "replaceInSession");
      Map<String, Object> sessionMap = getSessionMap();

      // if we passed null, use the remove semantics
      if (newValue == null)
      {
         sessionMap.remove(name);
         return null;
      }

      getFromSession(name, newValue.getClass(), sessionMap, "Provided object: " + newValue
         + " is not compatible with previously assigned '" + name + "' object: " + CURRENT_PLACEHOLDER);
      sessionMap.put(name, newValue);
      return newValue;
   }

   /**
    * Retrieves the session object associated with the specified name and the expected type.
    *
    * @param name          name of the session object to be retrieved
    * @param expectedClass expected class of the object
    * @param <T>           type of the object to be retrieved
    * @return the session object associated with the specified name
    * @throws IllegalArgumentException if the value associated with the specified name is not <code>null</code> and
    *                                  does not match the specified expected class
    */
   public <T> T getFromSession(String name, Class<T> expectedClass)
   {
      return getFromSession(name, expectedClass, getSessionMap(), "Current object:" + CURRENT_PLACEHOLDER
         + " is not compatible with expected class " + expectedClass + " for '" + name + "'");
   }

   /**
    * @param name          name of the session attribute to retrieve
    * @param expectedClass expected class of the attribute
    * @param sessionMap    the session map to retrieve the attribute from
    * @param errorMessage  the error message that will be used if the attribute value is not of the expected class, in
    *                      which {@link #CURRENT_PLACEHOLDER} will be substituted by the current value of the attribute
    *                      at runtime
    * @param <T>           the type of the object to be retrieved
    * @return the value associated with the specified name
    * @throws IllegalArgumentException if the value associated with the specified name is not <code>null</code> and
    *                                  does
    *                                  not match the specified expected class
    */
   private <T> T getFromSession(String name, Class<T> expectedClass, Map<String, Object> sessionMap, String errorMessage)
   {
      Object result = sessionMap.get(name);
      return checkObject(result, expectedClass, errorMessage);
   }

   protected <T> T checkObject(Object result, Class<T> expectedClass, String errorMessage)
   {
      if (result != null && !expectedClass.isAssignableFrom(result.getClass()))
      {
         throw new IllegalArgumentException(errorMessage.replace(CURRENT_PLACEHOLDER, result.toString()));
      }

      return expectedClass.cast(result);
   }

   public abstract <T> T findBean(String name, Class<T> type);
}
