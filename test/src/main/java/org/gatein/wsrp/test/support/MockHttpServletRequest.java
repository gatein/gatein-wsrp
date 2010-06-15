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

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com?subject=org.gatein.wsrp.test.support.MockHttpServletRequest">Chris
 *         Laprun</a>
 * @version $Revision: 11416 $
 * @since 2.4
 */
public class MockHttpServletRequest implements InvocationHandler, Serializable
{
   private HttpSession session;

   private Map attrs;
   public static String scheme = "http";
   public static String serverName = "test";
   public static Integer serverPort = 1234;

   private MockHttpServletRequest(HttpSession session)
   {
      this.session = session;
      this.attrs = new HashMap();
   }

   /**
    * @param session if <code>null</code>, a new MockHttpSession will be created and used instead
    * @return
    */
   public static HttpServletRequest createMockRequest(HttpSession session)
   {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();

      if (session == null)
      {
         session = MockHttpSession.createMockSession();
      }

      return (HttpServletRequest)Proxy.newProxyInstance(loader, new Class[]{HttpServletRequest.class},
         new MockHttpServletRequest(session));
   }

   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      String methodName = method.getName();
      if ("getSession".equals(methodName))
      {
         return session;
      }
      if ("getHeader".equals(methodName))
      {
         if ("User-Agent".equals(args[0]))
         {
            return "Mock Client User Agent";
         }
         return null;
      }
      if ("toString".equals(methodName))
      {
         return "MockHttpServletResponse";
      }
      if ("getAttribute".equals(methodName))
      {
         return attrs.get(args[0]);
      }
      if ("setAttribute".equals(methodName))
      {
         String name = (String)args[0];
         Object value = args[1];
         if (value != null)
         {
            attrs.put(name, value);
         }
         else
         {
            attrs.remove(value);
         }
         return null;
      }
      if ("removeAttribute".equals(methodName))
      {
         String name = (String)args[0];
         attrs.remove(name);
         return null;
      }
      if ("getScheme".equals(methodName))
      {
         return scheme;
      }
      if ("getServerName".equals(methodName))
      {
         return serverName;
      }
      if ("getServerPort".equals(methodName))
      {
         return serverPort;
      }
      if ("getHeaderNames".equals(methodName))
      {
         return new Enumeration<String>()
         {

            public boolean hasMoreElements()
            {
				return false;
			}
			
            public String nextElement()
            {
				return null;
			}
		};
      }
      if ("getCookies".equals(methodName))
      {
    	  return new Cookie[0];
      }
      if ("getMethod".equals(methodName))
      {
    	  return "GET";
      }
      if ("getContextPath".equals(methodName))
      {
    	  return "/";
      }
      if ("getPathInfo".equals(methodName))
      {
    	  return "/";
      }      
      if ("getQueryString".equals(methodName))
      {
    	  return "";
      }
      if ("getQueryURI".equals(methodName))
      {
    	  return "";
      }
      if ("getRequestURI".equals(methodName))
      {
    	  return "";
      }
      if ("getServletPath".equals(methodName))
      {
    	  return "/";
      }
      
      throw new UnsupportedOperationException("MockHttpServletRequest does not support: " + method);
   }
}
