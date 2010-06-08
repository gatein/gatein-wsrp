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

import org.gatein.wsrp.spec.v1.WSRP1ExceptionFactory;
import org.gatein.wsrp.spec.v2.WSRP2ExceptionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 9360 $
 * @since 2.6
 */
public abstract class WSRPExceptionFactory
{
   protected static final Logger log = LoggerFactory.getLogger(WSRPExceptionFactory.class);

   protected static final Map<Class<? extends Exception>, ExceptionFactory<? extends Exception>> exceptionClassToFactory =
      new HashMap<Class<? extends Exception>, ExceptionFactory<? extends Exception>>(33);

   static
   {
      // initialize exception factories
      WSRP1ExceptionFactory.getInstance().loadExceptionFactories();
      WSRP2ExceptionFactory.getInstance().loadExceptionFactories();
   }

   protected abstract void loadExceptionFactories();

   protected WSRPExceptionFactory()
   {
   }

   public static <E extends Exception> E throwWSException(Class<E> exceptionClass, String message, Throwable cause) throws E
   {
      throw createWSException(exceptionClass, message, cause);
   }

   public static <E extends Exception> E createWSException(Class<E> exceptionClass, String message, Throwable cause)
   {
      ExceptionFactory<E> exceptionFactory = (ExceptionFactory<E>)exceptionClassToFactory.get(exceptionClass);

      if (exceptionFactory == null)
      {
         throw new IllegalArgumentException("Unknown exception class: " + exceptionClass);
      }

      return exceptionFactory.newInstance(message, cause);
   }

   protected abstract static class ExceptionFactory<E extends Exception>
   {
      private final Constructor<E> exceptionConstructor;
      protected Object fault;
      private static final String FAULT = "Fault";

      public ExceptionFactory(Class<E> exceptionClass) throws NoSuchMethodException, IllegalAccessException, InstantiationException, ClassNotFoundException
      {
         String faultClassName = exceptionClass.getName() + FAULT;

         Class<?> clazz = Thread.currentThread().getContextClassLoader().loadClass(faultClassName);
         exceptionConstructor = exceptionClass.getConstructor(String.class, initFaultAndGetClass(clazz), Throwable.class);
      }

      protected abstract Class initFaultAndGetClass(Class clazz) throws IllegalAccessException, InstantiationException;

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