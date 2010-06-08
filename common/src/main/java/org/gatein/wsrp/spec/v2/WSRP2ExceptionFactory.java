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

package org.gatein.wsrp.spec.v2;

import org.gatein.wsrp.WSRPExceptionFactory;
import org.oasis.wsrp.v2.AccessDenied;
import org.oasis.wsrp.v2.ExportByValueNotSupported;
import org.oasis.wsrp.v2.ExportNoLongerValid;
import org.oasis.wsrp.v2.Fault;
import org.oasis.wsrp.v2.InconsistentParameters;
import org.oasis.wsrp.v2.InvalidCookie;
import org.oasis.wsrp.v2.InvalidHandle;
import org.oasis.wsrp.v2.InvalidRegistration;
import org.oasis.wsrp.v2.InvalidSession;
import org.oasis.wsrp.v2.InvalidUserCategory;
import org.oasis.wsrp.v2.MissingParameters;
import org.oasis.wsrp.v2.MissingParametersFault;
import org.oasis.wsrp.v2.ModifyRegistrationRequired;
import org.oasis.wsrp.v2.OperationFailed;
import org.oasis.wsrp.v2.OperationFailedFault;
import org.oasis.wsrp.v2.OperationNotSupported;
import org.oasis.wsrp.v2.PortletStateChangeRequired;
import org.oasis.wsrp.v2.ResourceSuspended;
import org.oasis.wsrp.v2.UnsupportedLocale;
import org.oasis.wsrp.v2.UnsupportedMimeType;
import org.oasis.wsrp.v2.UnsupportedMode;
import org.oasis.wsrp.v2.UnsupportedWindowState;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class WSRP2ExceptionFactory extends WSRPExceptionFactory
{
   protected void loadExceptionFactories()
   {
      // load exception factories
      try
      {
         exceptionClassToFactory.put(AccessDenied.class, new V2ExceptionFactory<AccessDenied>(AccessDenied.class));
         exceptionClassToFactory.put(ExportByValueNotSupported.class, new V2ExceptionFactory<ExportByValueNotSupported>(ExportByValueNotSupported.class));
         exceptionClassToFactory.put(ExportNoLongerValid.class, new V2ExceptionFactory<ExportNoLongerValid>(ExportNoLongerValid.class));
         exceptionClassToFactory.put(InconsistentParameters.class, new V2ExceptionFactory<InconsistentParameters>(InconsistentParameters.class));
         exceptionClassToFactory.put(InvalidCookie.class, new V2ExceptionFactory<InvalidCookie>(InvalidCookie.class));
         exceptionClassToFactory.put(InvalidHandle.class, new V2ExceptionFactory<InvalidHandle>(InvalidHandle.class));
         exceptionClassToFactory.put(InvalidRegistration.class, new V2ExceptionFactory<InvalidRegistration>(InvalidRegistration.class));
         exceptionClassToFactory.put(InvalidSession.class, new V2ExceptionFactory<InvalidSession>(InvalidSession.class));
         exceptionClassToFactory.put(InvalidUserCategory.class, new V2ExceptionFactory<InvalidUserCategory>(InvalidUserCategory.class));
         exceptionClassToFactory.put(MissingParameters.class, new V2ExceptionFactory<MissingParameters>(MissingParameters.class));
         exceptionClassToFactory.put(ModifyRegistrationRequired.class, new V2ExceptionFactory<ModifyRegistrationRequired>(ModifyRegistrationRequired.class));
         exceptionClassToFactory.put(OperationFailed.class, new V2ExceptionFactory<OperationFailed>(OperationFailed.class));
         exceptionClassToFactory.put(OperationNotSupported.class, new V2ExceptionFactory<OperationNotSupported>(OperationNotSupported.class));
         exceptionClassToFactory.put(PortletStateChangeRequired.class, new V2ExceptionFactory<PortletStateChangeRequired>(PortletStateChangeRequired.class));
         exceptionClassToFactory.put(ResourceSuspended.class, new V2ExceptionFactory<ResourceSuspended>(ResourceSuspended.class));
         exceptionClassToFactory.put(UnsupportedLocale.class, new V2ExceptionFactory<UnsupportedLocale>(UnsupportedLocale.class));
         exceptionClassToFactory.put(UnsupportedMimeType.class, new V2ExceptionFactory<UnsupportedMimeType>(UnsupportedMimeType.class));
         exceptionClassToFactory.put(UnsupportedMode.class, new V2ExceptionFactory<UnsupportedMode>(UnsupportedMode.class));
         exceptionClassToFactory.put(UnsupportedWindowState.class, new V2ExceptionFactory<UnsupportedWindowState>(UnsupportedWindowState.class));
      }
      catch (Exception e)
      {
         throw new RuntimeException("Couldn't initialize WSRPExceptionFactory", e);
      }
   }

   private static final class InstanceHolder
   {
      public static final WSRP2ExceptionFactory factory = new WSRP2ExceptionFactory();
   }

   public static WSRPExceptionFactory getInstance()
   {
      return InstanceHolder.factory;
   }

   private WSRP2ExceptionFactory()
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

   protected static class V2ExceptionFactory<E extends Exception> extends ExceptionFactory
   {
      public V2ExceptionFactory(Class<E> exceptionClass) throws NoSuchMethodException, IllegalAccessException, InstantiationException, ClassNotFoundException
      {
         super(exceptionClass);
      }

      protected Class initFaultAndGetClass(Class clazz) throws IllegalAccessException, InstantiationException
      {
         if (Fault.class.isAssignableFrom(clazz))
         {
            Class<? extends Fault> faultClass = (Class<Fault>)clazz;
            fault = faultClass.newInstance();
            return faultClass;
         }
         else
         {
            throw new IllegalArgumentException("Couldn't create fault class based on specified exception class from "
               + clazz);
         }
      }
   }
}
