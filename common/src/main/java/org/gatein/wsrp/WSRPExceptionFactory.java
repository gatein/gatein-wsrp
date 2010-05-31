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

package org.gatein.wsrp;

import org.oasis.wsrp.v2.AccessDenied;
import org.oasis.wsrp.v2.AccessDeniedFault;
import org.oasis.wsrp.v2.Fault;
import org.oasis.wsrp.v2.InconsistentParameters;
import org.oasis.wsrp.v2.InconsistentParametersFault;
import org.oasis.wsrp.v2.InvalidCookie;
import org.oasis.wsrp.v2.InvalidCookieFault;
import org.oasis.wsrp.v2.InvalidHandle;
import org.oasis.wsrp.v2.InvalidHandleFault;
import org.oasis.wsrp.v2.InvalidRegistration;
import org.oasis.wsrp.v2.InvalidRegistrationFault;
import org.oasis.wsrp.v2.InvalidSession;
import org.oasis.wsrp.v2.InvalidSessionFault;
import org.oasis.wsrp.v2.InvalidUserCategory;
import org.oasis.wsrp.v2.InvalidUserCategoryFault;
import org.oasis.wsrp.v2.MissingParameters;
import org.oasis.wsrp.v2.MissingParametersFault;
import org.oasis.wsrp.v2.OperationFailed;
import org.oasis.wsrp.v2.OperationFailedFault;
import org.oasis.wsrp.v2.PortletStateChangeRequired;
import org.oasis.wsrp.v2.PortletStateChangeRequiredFault;
import org.oasis.wsrp.v2.UnsupportedLocale;
import org.oasis.wsrp.v2.UnsupportedLocaleFault;
import org.oasis.wsrp.v2.UnsupportedMimeType;
import org.oasis.wsrp.v2.UnsupportedMimeTypeFault;
import org.oasis.wsrp.v2.UnsupportedMode;
import org.oasis.wsrp.v2.UnsupportedModeFault;
import org.oasis.wsrp.v2.UnsupportedWindowState;
import org.oasis.wsrp.v2.UnsupportedWindowStateFault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 9360 $
 * @since 2.6
 */
public class WSRPExceptionFactory
{
   private static final Logger log = LoggerFactory.getLogger(WSRPExceptionFactory.class);

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

   private static final Map<String, ExceptionFactory<? extends Exception, ? extends Fault>> errorCodeToExceptions =
      new HashMap<String, ExceptionFactory<? extends Exception, ? extends Fault>>(17);

   static
   {
      try
      {
         errorCodeToExceptions.put(ACCESS_DENIED, new ExceptionFactory<AccessDenied, AccessDeniedFault>()
         {
         });
         errorCodeToExceptions.put(INCONSISTENT_PARAMETERS, new ExceptionFactory<InconsistentParameters, InconsistentParametersFault>()
         {
         });
         errorCodeToExceptions.put(INVALID_COOKIE, new ExceptionFactory<InvalidCookie, InvalidCookieFault>()
         {
         });
         errorCodeToExceptions.put(INVALID_HANDLE, new ExceptionFactory<InvalidHandle, InvalidHandleFault>()
         {
         });
         errorCodeToExceptions.put(INVALID_REGISTRATION, new ExceptionFactory<InvalidRegistration, InvalidRegistrationFault>()
         {
         });
         errorCodeToExceptions.put(INVALID_SESSION, new ExceptionFactory<InvalidSession, InvalidSessionFault>()
         {
         });
         errorCodeToExceptions.put(INVALID_USER_CATEGORY, new ExceptionFactory<InvalidUserCategory, InvalidUserCategoryFault>()
         {
         });
         errorCodeToExceptions.put(MISSING_PARAMETERS, new ExceptionFactory<MissingParameters, MissingParametersFault>()
         {
         });
         errorCodeToExceptions.put(OPERATION_FAILED, new ExceptionFactory<OperationFailed, OperationFailedFault>()
         {
         });
         errorCodeToExceptions.put(PORTLET_STATE_CHANGE_REQUIRED, new ExceptionFactory<PortletStateChangeRequired, PortletStateChangeRequiredFault>()
         {
         });
         errorCodeToExceptions.put(UNSUPPORTED_LOCALE, new ExceptionFactory<UnsupportedLocale, UnsupportedLocaleFault>()
         {
         });
         errorCodeToExceptions.put(UNSUPPORTED_MIME_TYPE, new ExceptionFactory<UnsupportedMimeType, UnsupportedMimeTypeFault>()
         {
         });
         errorCodeToExceptions.put(UNSUPPORTED_MODE, new ExceptionFactory<UnsupportedMode, UnsupportedModeFault>()
         {
         });
         errorCodeToExceptions.put(UNSUPPORTED_WINDOW_STATE, new ExceptionFactory<UnsupportedWindowState, UnsupportedWindowStateFault>()
         {
         });
      }
      catch (Exception e)
      {
         throw new RuntimeException("Error initializing WSRPExceptionFactory", e);
      }
   }

   private WSRPExceptionFactory()
   {
   }

   public static void throwMissingParametersIfValueIsMissing(Object valueToCheck, String valueName, String context)
      throws MissingParameters
   {
      if (valueToCheck == null)
      {
         throw new MissingParameters("Missing required " + valueName + (context != null ? " in " + context : ""), new MissingParametersFault());
      }
   }

   public static void throwOperationFailedIfValueIsMissing(Object valueToCheck, String valueName) throws OperationFailed
   {
      if (valueToCheck == null)
      {
         throw new OperationFailed("Missing required " + valueName, new OperationFailedFault());
      }
   }

   public static <E extends Exception, F extends Fault> E throwWSException(String errorCode, String message, Throwable cause) throws E
   {
      ExceptionFactory<E, F> exceptionFactory = (ExceptionFactory<E, F>)errorCodeToExceptions.get(errorCode);
      if (exceptionFactory == null)
      {
         throw new IllegalArgumentException("Unknown error code: " + errorCode);
      }

      throw exceptionFactory.newInstance(message, cause);
   }

   private abstract static class ExceptionFactory<E extends Exception, F extends Fault>
   {
      private final Constructor<E> exceptionConstructor;
      private final Fault fault;

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