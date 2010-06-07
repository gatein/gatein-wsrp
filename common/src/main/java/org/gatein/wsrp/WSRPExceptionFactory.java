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
import org.oasis.wsrp.v2.ExportByValueNotSupported;
import org.oasis.wsrp.v2.ExportNoLongerValid;
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
import org.oasis.wsrp.v2.ModifyRegistrationRequired;
import org.oasis.wsrp.v2.OperationFailed;
import org.oasis.wsrp.v2.OperationFailedFault;
import org.oasis.wsrp.v2.OperationNotSupported;
import org.oasis.wsrp.v2.PortletStateChangeRequired;
import org.oasis.wsrp.v2.PortletStateChangeRequiredFault;
import org.oasis.wsrp.v2.ResourceSuspended;
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
   private static final Map<Class<? extends Exception>, ExceptionFactory2<? extends Exception>> exceptionClassToFactory =
      new HashMap<Class<? extends Exception>, ExceptionFactory2<? extends Exception>>(19);

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

   static
   {
      try
      {
         exceptionClassToFactory.put(AccessDenied.class, new ExceptionFactory2<AccessDenied>(AccessDenied.class));
         exceptionClassToFactory.put(ExportByValueNotSupported.class, new ExceptionFactory2<ExportByValueNotSupported>(ExportByValueNotSupported.class));
         exceptionClassToFactory.put(ExportNoLongerValid.class, new ExceptionFactory2<ExportNoLongerValid>(ExportNoLongerValid.class));
         exceptionClassToFactory.put(InconsistentParameters.class, new ExceptionFactory2<InconsistentParameters>(InconsistentParameters.class));
         exceptionClassToFactory.put(InvalidCookie.class, new ExceptionFactory2<InvalidCookie>(InvalidCookie.class));
         exceptionClassToFactory.put(InvalidHandle.class, new ExceptionFactory2<InvalidHandle>(InvalidHandle.class));
         exceptionClassToFactory.put(InvalidRegistration.class, new ExceptionFactory2<InvalidRegistration>(InvalidRegistration.class));
         exceptionClassToFactory.put(InvalidSession.class, new ExceptionFactory2<InvalidSession>(InvalidSession.class));
         exceptionClassToFactory.put(InvalidUserCategory.class, new ExceptionFactory2<InvalidUserCategory>(InvalidUserCategory.class));
         exceptionClassToFactory.put(MissingParameters.class, new ExceptionFactory2<MissingParameters>(MissingParameters.class));
         exceptionClassToFactory.put(ModifyRegistrationRequired.class, new ExceptionFactory2<ModifyRegistrationRequired>(ModifyRegistrationRequired.class));
         exceptionClassToFactory.put(OperationFailed.class, new ExceptionFactory2<OperationFailed>(OperationFailed.class));
         exceptionClassToFactory.put(OperationNotSupported.class, new ExceptionFactory2<OperationNotSupported>(OperationNotSupported.class));
         exceptionClassToFactory.put(PortletStateChangeRequired.class, new ExceptionFactory2<PortletStateChangeRequired>(PortletStateChangeRequired.class));
         exceptionClassToFactory.put(ResourceSuspended.class, new ExceptionFactory2<ResourceSuspended>(ResourceSuspended.class));
         exceptionClassToFactory.put(UnsupportedLocale.class, new ExceptionFactory2<UnsupportedLocale>(UnsupportedLocale.class));
         exceptionClassToFactory.put(UnsupportedMimeType.class, new ExceptionFactory2<UnsupportedMimeType>(UnsupportedMimeType.class));
         exceptionClassToFactory.put(UnsupportedMode.class, new ExceptionFactory2<UnsupportedMode>(UnsupportedMode.class));
         exceptionClassToFactory.put(UnsupportedWindowState.class, new ExceptionFactory2<UnsupportedWindowState>(UnsupportedWindowState.class));
      }
      catch (Exception e)
      {
         throw new RuntimeException("Couldn't initialize WSRPExceptionFactory", e);
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

   public static <E extends Exception> E throwWSException(Class<E> exceptionClass, String message, Throwable cause) throws E
   {
      throw createWSException(exceptionClass, message, cause);
   }

   public static <E extends Exception> E createWSException(Class<E> exceptionClass, String message, Throwable cause)
   {
      ExceptionFactory2<E> exceptionFactory = (ExceptionFactory2<E>)exceptionClassToFactory.get(exceptionClass);

      if (exceptionFactory == null)
      {
         throw new IllegalArgumentException("Unknown exception class: " + exceptionClass);
      }

      return exceptionFactory.newInstance(message, cause);
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

   private static class ExceptionFactory2<E extends Exception>
   {
      private final Constructor<E> exceptionConstructor;
      private final Fault fault;
      private static final String FAULT = "Fault";

      public ExceptionFactory2(Class<E> exceptionClass) throws NoSuchMethodException, IllegalAccessException, InstantiationException, ClassNotFoundException
      {
         String faultClassName = exceptionClass.getName() + FAULT;

         Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(faultClassName);
         if (Fault.class.isAssignableFrom(clazz))
         {
            Class<? extends Fault> faultClass = (Class<Fault>)clazz;
            exceptionConstructor = exceptionClass.getConstructor(String.class, faultClass, Throwable.class);
            fault = faultClass.newInstance();
         }
         else
         {
            throw new IllegalArgumentException("Couldn't create a Fault class based on specified exception class: "
               + exceptionClass);
         }
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