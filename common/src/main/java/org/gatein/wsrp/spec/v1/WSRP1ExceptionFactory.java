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

import org.gatein.wsrp.WSRPExceptionFactory;
import org.oasis.wsrp.v1.V1AccessDenied;
import org.oasis.wsrp.v1.V1Fault;
import org.oasis.wsrp.v1.V1InconsistentParameters;
import org.oasis.wsrp.v1.V1InvalidCookie;
import org.oasis.wsrp.v1.V1InvalidHandle;
import org.oasis.wsrp.v1.V1InvalidRegistration;
import org.oasis.wsrp.v1.V1InvalidSession;
import org.oasis.wsrp.v1.V1InvalidUserCategory;
import org.oasis.wsrp.v1.V1MissingParameters;
import org.oasis.wsrp.v1.V1MissingParametersFault;
import org.oasis.wsrp.v1.V1OperationFailed;
import org.oasis.wsrp.v1.V1PortletStateChangeRequired;
import org.oasis.wsrp.v1.V1UnsupportedLocale;
import org.oasis.wsrp.v1.V1UnsupportedMimeType;
import org.oasis.wsrp.v1.V1UnsupportedMode;
import org.oasis.wsrp.v1.V1UnsupportedWindowState;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class WSRP1ExceptionFactory extends WSRPExceptionFactory
{
   protected void loadExceptionFactories()
   {
      try
      {
         exceptionClassToFactory.put(V1AccessDenied.class, new V1ExceptionFactory<V1AccessDenied>(V1AccessDenied.class));
         exceptionClassToFactory.put(V1InconsistentParameters.class, new V1ExceptionFactory<V1InconsistentParameters>(V1InconsistentParameters.class));
         exceptionClassToFactory.put(V1InvalidCookie.class, new V1ExceptionFactory<V1InvalidCookie>(V1InvalidCookie.class));
         exceptionClassToFactory.put(V1InvalidHandle.class, new V1ExceptionFactory<V1InvalidHandle>(V1InvalidHandle.class));
         exceptionClassToFactory.put(V1InvalidRegistration.class, new V1ExceptionFactory<V1InvalidRegistration>(V1InvalidRegistration.class));
         exceptionClassToFactory.put(V1InvalidSession.class, new V1ExceptionFactory<V1InvalidSession>(V1InvalidSession.class));
         exceptionClassToFactory.put(V1InvalidUserCategory.class, new V1ExceptionFactory<V1InvalidUserCategory>(V1InvalidUserCategory.class));
         exceptionClassToFactory.put(V1MissingParameters.class, new V1ExceptionFactory<V1MissingParameters>(V1MissingParameters.class));
         exceptionClassToFactory.put(V1OperationFailed.class, new V1ExceptionFactory<V1OperationFailed>(V1OperationFailed.class));
         exceptionClassToFactory.put(V1PortletStateChangeRequired.class, new V1ExceptionFactory<V1PortletStateChangeRequired>(V1PortletStateChangeRequired.class));
         exceptionClassToFactory.put(V1UnsupportedLocale.class, new V1ExceptionFactory<V1UnsupportedLocale>(V1UnsupportedLocale.class));
         exceptionClassToFactory.put(V1UnsupportedMimeType.class, new V1ExceptionFactory<V1UnsupportedMimeType>(V1UnsupportedMimeType.class));
         exceptionClassToFactory.put(V1UnsupportedMode.class, new V1ExceptionFactory<V1UnsupportedMode>(V1UnsupportedMode.class));
         exceptionClassToFactory.put(V1UnsupportedWindowState.class, new V1ExceptionFactory<V1UnsupportedWindowState>(V1UnsupportedWindowState.class));
      }
      catch (Exception e)
      {
         throw new RuntimeException("Couldn't initialize WSRPV1ExceptionFactory", e);
      }
   }

   private static final class InstanceHolder
   {
      public static final WSRP1ExceptionFactory factory = new WSRP1ExceptionFactory();
   }

   public static WSRPExceptionFactory getInstance()
   {
      return InstanceHolder.factory;
   }

   private WSRP1ExceptionFactory()
   {
   }

   public static void throwMissingParametersIfValueIsMissing(Object valueToCheck, String valueName, String context) throws V1MissingParameters
   {
      if (valueToCheck == null)
      {
         throw new V1MissingParameters("Missing required " + valueName + (context != null ? " in " + context : ""), new V1MissingParametersFault());
      }
   }

   protected static class V1ExceptionFactory<E extends Exception> extends ExceptionFactory
   {
      public V1ExceptionFactory(Class<E> exceptionClass) throws NoSuchMethodException, IllegalAccessException, InstantiationException, ClassNotFoundException
      {
         super(exceptionClass);
      }

      protected Class initFaultAndGetClass(Class clazz) throws IllegalAccessException, InstantiationException
      {
         if (V1Fault.class.isAssignableFrom(clazz))
         {
            Class<? extends V1Fault> faultClass = (Class<V1Fault>)clazz;
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
