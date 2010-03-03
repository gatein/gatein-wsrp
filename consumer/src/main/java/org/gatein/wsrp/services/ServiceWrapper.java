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

package org.gatein.wsrp.services;

import org.gatein.wsrp.handler.RequestHeaderClientHandler;

import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import javax.xml.ws.soap.SOAPFaultException;
import java.lang.reflect.ParameterizedType;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
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

   /**
    * HTTP request timeout property. JAX-WS doesn't standardize that value, so needs to be adapted per used
    * implementation
    */
   private static final String JBOSS_WS_TIMEOUT = "org.jboss.ws.timeout";
   private static final String SUN_WS_TIMEOUT = "com.sun.xml.ws.request.timeout";
   private static final String IBM_WS_TIMEOUT = "com.ibm.SOAP.requestTimeout";

   private static final RequestHeaderClientHandler REQUEST_HEADER_CLIENT_HANDLER = new RequestHeaderClientHandler();
   private static final String JBOSS_WS_STUBEXT_PROPERTY_CHUNKED_ENCODING_SIZE = "http://org.jboss.ws/http#chunksize";

   protected ServiceWrapper(Object service, ManageableServiceFactory parentFactory)
   {
      if (service == null)
      {
         throw new IllegalArgumentException("Cannot create a ServiceWrapper without a valid service!");
      }

      Class serviceClass = service.getClass();

      // set timeout properties for different WS stacks
      BindingProvider bindingProvider = (BindingProvider)service;
      setTimeout(bindingProvider.getRequestContext(), parentFactory);


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

   private static void setTimeout(Map<String, Object> requestContext, ManageableServiceFactory parentFactory)
   {
      int timeout = parentFactory.getWSOperationTimeOut();
      requestContext.put(JBOSS_WS_TIMEOUT, timeout);
      requestContext.put(SUN_WS_TIMEOUT, timeout);
      requestContext.put(IBM_WS_TIMEOUT, timeout);
   }

   public static <T> T getServiceWrapper(Class<T> expectedServiceInterface, Object service, String portAddress, ManageableServiceFactory parentFactory)
   {
      BindingProvider bindingProvider = (BindingProvider)service;
      Map<String, Object> requestContext = bindingProvider.getRequestContext();

      // set timeout
      setTimeout(requestContext, parentFactory);

      // set port address
      requestContext.put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, portAddress);

      // Set org.jboss.ws.core.StubExt.PROPERTY_CHUNKED_ENCODING_SIZE to 0 to deactive chunked encoding for
      // better interoperability as Oracle's producer doesn't support it, for example.
      // See https://jira.jboss.org/jira/browse/JBWS-2884 and
      // http://community.jboss.org/wiki/JBossWS-NativeUserGuide#Chunked_encoding_setup
      requestContext.put(JBOSS_WS_STUBEXT_PROPERTY_CHUNKED_ENCODING_SIZE, 0);

      // Add client side handler via JAX-WS API
      Binding binding = bindingProvider.getBinding();
      List<Handler> handlerChain = binding.getHandlerChain();
      if (handlerChain != null)
      {
         // if we already have a handler chain, just add the request hearder handler if it's not already in there
         if (!handlerChain.contains(REQUEST_HEADER_CLIENT_HANDLER))
         {
            handlerChain.add(REQUEST_HEADER_CLIENT_HANDLER);
         }
      }
      else
      {
         // otherwise, create a handler chain and add our handler to it
         handlerChain = new ArrayList<Handler>(1);
         handlerChain.add(REQUEST_HEADER_CLIENT_HANDLER);
      }
      binding.setHandlerChain(handlerChain);

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
