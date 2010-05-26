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

package org.gatein.wsrp.spec.v1;

import org.oasis.wsrp.v1.V1AccessDenied;
import org.oasis.wsrp.v1.V1AccessDeniedFault;
import org.oasis.wsrp.v1.V1Fault;
import org.oasis.wsrp.v1.V1InconsistentParameters;
import org.oasis.wsrp.v1.V1InconsistentParametersFault;
import org.oasis.wsrp.v1.V1InvalidCookie;
import org.oasis.wsrp.v1.V1InvalidCookieFault;
import org.oasis.wsrp.v1.V1InvalidHandle;
import org.oasis.wsrp.v1.V1InvalidHandleFault;
import org.oasis.wsrp.v1.V1InvalidRegistration;
import org.oasis.wsrp.v1.V1InvalidRegistrationFault;
import org.oasis.wsrp.v1.V1InvalidSession;
import org.oasis.wsrp.v1.V1InvalidSessionFault;
import org.oasis.wsrp.v1.V1InvalidUserCategory;
import org.oasis.wsrp.v1.V1InvalidUserCategoryFault;
import org.oasis.wsrp.v1.V1MissingParameters;
import org.oasis.wsrp.v1.V1MissingParametersFault;
import org.oasis.wsrp.v1.V1OperationFailed;
import org.oasis.wsrp.v1.V1OperationFailedFault;
import org.oasis.wsrp.v1.V1PortletStateChangeRequired;
import org.oasis.wsrp.v1.V1PortletStateChangeRequiredFault;
import org.oasis.wsrp.v1.V1UnsupportedLocale;
import org.oasis.wsrp.v1.V1UnsupportedLocaleFault;
import org.oasis.wsrp.v1.V1UnsupportedMimeType;
import org.oasis.wsrp.v1.V1UnsupportedMimeTypeFault;
import org.oasis.wsrp.v1.V1UnsupportedMode;
import org.oasis.wsrp.v1.V1UnsupportedModeFault;
import org.oasis.wsrp.v1.V1UnsupportedWindowState;
import org.oasis.wsrp.v1.V1UnsupportedWindowStateFault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class WSRP1ExceptionFactory
{
   private static final Logger log = LoggerFactory.getLogger(WSRP1ExceptionFactory.class);

   public static final String ACCESS_DENIED = "AccessDenied";
   public static final String INCONSISTENT_PARAMETERS = "InconsistentParameters";
   public static final String INVALID_REGISTRATION = "InvalidRegistration";
   public static final String INVALID_COOKIE = "InvalidCookie";
   public static final String INVALID_HANDLE = "InvalidHandle";
   public static final String INVALID_SESSION = "InvalidSession";
   public static final String INVALID_USER_CATEGORY = "InvalidUserCategory";
   public static final String MISSING_PARAMETERS = "MissingParameters";
   public static final String OPERATION_FAILED = "OperationFailed";
   public static final String PORTLET_STATE_CHANGE_REQUIRED = "PortletStateChangeRequired";
   public static final String UNSUPPORTED_LOCALE = "UnsupportedLocale";
   public static final String UNSUPPORTED_MIME_TYPE = "UnsupportedMimeType";
   public static final String UNSUPPORTED_MODE = "UnsupportedMode";
   public static final String UNSUPPORTED_WINDOW_STATE = "UnsupportedWindowState";

   private static final Map<String, ExceptionFactory<? extends Exception, ? extends V1Fault>> errorCodeToExceptions =
      new HashMap<String, ExceptionFactory<? extends Exception, ? extends V1Fault>>(17);

   static
   {
      try
      {
         errorCodeToExceptions.put(ACCESS_DENIED, new ExceptionFactory<V1AccessDenied, V1AccessDeniedFault>()
         {
         });
         errorCodeToExceptions.put(INCONSISTENT_PARAMETERS, new ExceptionFactory<V1InconsistentParameters, V1InconsistentParametersFault>()
         {
         });
         errorCodeToExceptions.put(INVALID_COOKIE, new ExceptionFactory<V1InvalidCookie, V1InvalidCookieFault>()
         {
         });
         errorCodeToExceptions.put(INVALID_HANDLE, new ExceptionFactory<V1InvalidHandle, V1InvalidHandleFault>()
         {
         });
         errorCodeToExceptions.put(INVALID_REGISTRATION, new ExceptionFactory<V1InvalidRegistration, V1InvalidRegistrationFault>()
         {
         });
         errorCodeToExceptions.put(INVALID_SESSION, new ExceptionFactory<V1InvalidSession, V1InvalidSessionFault>()
         {
         });
         errorCodeToExceptions.put(INVALID_USER_CATEGORY, new ExceptionFactory<V1InvalidUserCategory, V1InvalidUserCategoryFault>()
         {
         });
         errorCodeToExceptions.put(MISSING_PARAMETERS, new ExceptionFactory<V1MissingParameters, V1MissingParametersFault>()
         {
         });
         errorCodeToExceptions.put(OPERATION_FAILED, new ExceptionFactory<V1OperationFailed, V1OperationFailedFault>()
         {
         });
         errorCodeToExceptions.put(PORTLET_STATE_CHANGE_REQUIRED, new ExceptionFactory<V1PortletStateChangeRequired, V1PortletStateChangeRequiredFault>()
         {
         });
         errorCodeToExceptions.put(UNSUPPORTED_LOCALE, new ExceptionFactory<V1UnsupportedLocale, V1UnsupportedLocaleFault>()
         {
         });
         errorCodeToExceptions.put(UNSUPPORTED_MIME_TYPE, new ExceptionFactory<V1UnsupportedMimeType, V1UnsupportedMimeTypeFault>()
         {
         });
         errorCodeToExceptions.put(UNSUPPORTED_MODE, new ExceptionFactory<V1UnsupportedMode, V1UnsupportedModeFault>()
         {
         });
         errorCodeToExceptions.put(UNSUPPORTED_WINDOW_STATE, new ExceptionFactory<V1UnsupportedWindowState, V1UnsupportedWindowStateFault>()
         {
         });
      }
      catch (Exception e)
      {
         throw new RuntimeException("Error initializing WSRP1ExceptionFactory", e);
      }
   }

   private WSRP1ExceptionFactory()
   {
   }

   public static void throwMissingParametersIfValueIsMissing(Object valueToCheck, String valueName, String context)
      throws V1MissingParameters
   {
      if (valueToCheck == null)
      {
         throw new V1MissingParameters("Missing required " + valueName + (context != null ? " in " + context : ""), new V1MissingParametersFault());
      }
   }

   public static void throwOperationFailedIfValueIsMissing(Object valueToCheck, String valueName) throws V1OperationFailed
   {
      if (valueToCheck == null)
      {
         throw new V1OperationFailed("Missing required " + valueName, new V1OperationFailedFault());
      }
   }

   public static <E extends Exception, F extends V1Fault> E throwWSException(String errorCode, String message, Throwable cause) throws E
   {
      ExceptionFactory<E, F> exceptionFactory = (ExceptionFactory<E, F>)errorCodeToExceptions.get(errorCode);
      if (exceptionFactory == null)
      {
         throw new IllegalArgumentException("Unknown error code: " + errorCode);
      }

      throw exceptionFactory.newInstance(message, cause);
   }

   private abstract static class ExceptionFactory<E extends Exception, F extends V1Fault>
   {
      private final Constructor<E> exceptionConstructor;
      private final V1Fault fault;

      public ExceptionFactory() throws NoSuchMethodException, IllegalAccessException, InstantiationException
      {
         ParameterizedType pt = (ParameterizedType)getClass().getGenericSuperclass();
         Class<E> exceptionClass = (Class<E>)pt.getActualTypeArguments()[0];
         Class<F> faultClass = (Class<F>)pt.getActualTypeArguments()[1];
         exceptionConstructor = exceptionClass.getConstructor(String.class, faultClass, Throwable.class);
         fault = faultClass.newInstance();
      }

      public E newInstance(String message, Throwable cause)
      {
         try
         {
            return exceptionConstructor.newInstance(message, fault, cause);
         }
         catch (Exception e)
         {
            log.debug("Couldn't instantiate Exception associated with " + fault.getClass().getSimpleName()
               + ", message: " + message + ", cause: " + cause);
            return null;
         }
      }
   }
}
