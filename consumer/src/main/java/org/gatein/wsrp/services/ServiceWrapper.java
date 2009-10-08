/*
 * JBoss, a division of Red Hat
 * Copyright 2009, Red Hat Middleware, LLC, and individual
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

package org.gatein.wsrp.services;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPFaultException;
import java.lang.reflect.ParameterizedType;
import java.rmi.RemoteException;
import java.util.Map;

/**
 * Wraps endpoints to be able to intercept RemoteExceptions on WSRP calls and fail the associated service factory if the
 * error is not a business exception.
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class ServiceWrapper<T>
{
   protected T service;
   protected ManageableServiceFactory parentFactory;
   private static final int TIMEOUT_MS = 3 * 1000; //todo: expose timeout so that it can be changed from the GUI

   /**
    * HTTP request timeout property. JAX-WS doesn't standardize that value, so needs to be adapted per used
    * implementation
    */
   private static final String JBOSS_WS_TIMEOUT = "org.jboss.ws.timeout";
   private static final String SUN_WS_TIMEOUT = "com.sun.xml.ws.request.timeout";
   private static final String IBM_WS_TIMEOUT = "com.ibm.SOAP.requestTimeout";

   protected ServiceWrapper(Object service, ManageableServiceFactory parentFactory)
   {
      if (service == null)
      {
         throw new IllegalArgumentException("Cannot create a ServiceWrapper without a valid service!");
      }

      Class serviceClass = service.getClass();

      // set timeout properties for different WS stacks
      BindingProvider bindingProvider = (BindingProvider)service;
      setTimeout(bindingProvider);


      Class tClass = (Class)((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
      if (tClass.isAssignableFrom(serviceClass))
      {
         this.service = (T)service;
      }
      else
      {
         throw new IllegalArgumentException(service + " is not an instance of " + tClass.getSimpleName());
      }
      this.parentFactory = parentFactory;
   }

   private static void setTimeout(BindingProvider bindingProvider)
   {
      Map<String, Object> requestContext = bindingProvider.getRequestContext();
      requestContext.put(JBOSS_WS_TIMEOUT, TIMEOUT_MS);
      requestContext.put(SUN_WS_TIMEOUT, TIMEOUT_MS);
      requestContext.put(IBM_WS_TIMEOUT, TIMEOUT_MS);
   }

   public static <T> T getServiceWrapper(Class<T> expectedServiceInterface, Object service, ManageableServiceFactory parentFactory)
   {
      // for now, only set timeouts
      setTimeout((BindingProvider)service);
      return expectedServiceInterface.cast(service);
   }

   protected void handleRemoteException(RemoteException e) throws RemoteException
   {
      // if the remote exception happens to be a SOAPFaultException, this is a business exception, do NOT fail the factory in this case
      // todo: not sure if this is still needed, will need testing
      if (!(e.getCause() instanceof SOAPFaultException))
      {
         parentFactory.setAvailable(false);
      }

      throw e;
   }
}
