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

import org.gatein.common.util.Tools;

import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com?subject=org.gatein.wsrp.test.support.MockHttpSession">Chris
 *         Laprun</a>
 * @version $Revision: 8784 $
 * @since 2.4
 */
public class MockHttpSession implements InvocationHandler, Serializable
{
   private final Map map = new HashMap();

   private MockHttpSession()
   {
   }

   public static HttpSession createMockSession()
   {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      return (HttpSession)Proxy.newProxyInstance(loader, new Class[]{HttpSession.class}, new MockHttpSession());
   }

   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      String methodName = method.getName();
      if ("setAttribute".equals(methodName))
      {
         map.put(args[0], args[1]);
         return null;
      }
      else if ("removeAttribute".equals(methodName))
      {
         map.remove(args[0]);
         return null;
      }
      else if ("getAttribute".equals(methodName))
      {
         return map.get(args[0]);
      }
      else if ("getAttributeNames".equals(methodName))
      {
         return Tools.toEnumeration(map.keySet().iterator());
      }
      else if ("toString".equals(methodName))
      {
         return "MockHttpSession";
      }
      else if ("getId".equals(methodName))
      {
         return "SESSION_ID";
      }
      else
      {
         throw new UnsupportedOperationException("MockHttpSession does not support: " + method);
      }
   }
}
