/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2006, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/

package org.gatein.wsrp.test.handler;

import org.gatein.wsrp.test.support.MockHttpServletRequest;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com?subject=org.gatein.wsrp.wsrp.handler.MockSOAPMessageContext">Chris
 *         Laprun</a>
 * @version $Revision: 8784 $
 * @since 2.4
 */
public class MockSOAPMessageContext implements InvocationHandler
{
   MockSOAPMessage message;
   Map<String, List<String>> httpHeaders = new HashMap<String, List<String>>();


   public MockSOAPMessageContext(MockSOAPMessage message)
   {
      this.message = message;
   }

   public MockSOAPMessage getMessage()
   {
      return message;
   }

   public void setMessage(MockSOAPMessage message)
   {
      this.message = message;
   }

   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      String methodName = method.getName();

      if ("getMessage".equals(methodName))
      {
         return getMessage();
      }
      else if ("get".equals(methodName))
      {
         // should only be called to get the endpoint address
         if (BindingProvider.ENDPOINT_ADDRESS_PROPERTY.equals(args[0]))
         {
            return MockHttpServletRequest.hostURL;
         }
         if (MessageContext.HTTP_REQUEST_HEADERS.equals(args[0]) || MessageContext.HTTP_RESPONSE_HEADERS.equals(args[0]))
         {
            return httpHeaders;
         }
         throw new IllegalArgumentException("MockSOAPMessageContext.get method should only be called to retrieve "
            + BindingProvider.ENDPOINT_ADDRESS_PROPERTY + " or " + MessageContext.HTTP_REQUEST_HEADERS +
            " values. Requested: " + args[0]);
      }
      else if ("put".equals(methodName))
      {
         if (MessageContext.HTTP_REQUEST_HEADERS.equals(args[0]))
         {
            httpHeaders = (Map<String, List<String>>)args[1];
            return null;
         }

         throw new IllegalArgumentException("MockSOAPMessageContext.put method should only be called to add Cookies. Tried to add " + args[0] + " with value " + args[1]);
      }
      else if ("toString".equals(methodName))
      {
         return this.toString();
      }

      throw new UnsupportedOperationException("MockSOAPMessageContext does not support " + methodName + " method");
   }

   public static SOAPMessageContext createMessageContext(MockSOAPMessage message, ClassLoader classLoader)
   {
      return (SOAPMessageContext)Proxy.newProxyInstance(classLoader, new Class[]{SOAPMessageContext.class},
         new MockSOAPMessageContext(message));
   }
}
