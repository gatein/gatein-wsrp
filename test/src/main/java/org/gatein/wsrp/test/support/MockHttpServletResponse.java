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

package org.gatein.wsrp.test.support;

import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author <a href="mailto:chris.laprun@jboss.com?subject=org.gatein.wsrp.test.support.MockHttpServletResponse">Chris
 *         Laprun</a>
 * @version $Revision: 8784 $
 * @since 2.4
 */
public class MockHttpServletResponse implements InvocationHandler, Serializable
{
   Object cookie;

   private MockHttpServletResponse()
   {
   }

   public static HttpServletResponse createMockResponse()
   {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      return (HttpServletResponse)Proxy.newProxyInstance(loader, new Class[]{HttpServletResponse.class}, new MockHttpServletResponse());
   }

   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      String methodName = method.getName();
      if ("addCookie".equals(methodName))
      {
         cookie = args[0];
         return null;
      }
      else if ("reset".equals(methodName))
      {
         cookie = null;
         return null;
      }
      else if ("toString".equals(methodName))
      {
         return "MockHttpServletResponse";
      }
      throw new UnsupportedOperationException("MockHttpServletResponse does not support: " + method);
   }
}
