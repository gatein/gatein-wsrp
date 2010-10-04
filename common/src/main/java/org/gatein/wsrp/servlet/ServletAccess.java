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

package org.gatein.wsrp.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8784 $
 */
public class ServletAccess implements InvocationHandler
{

   public static final ThreadLocal<Invocation> local = new ThreadLocal<Invocation>();

   protected Object next;

   public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
   {
      String mname = method.getName();
      if ("backgroundProcess".equals(mname))
      {
         // Noop
         return null;
      }
      else if ("setNext".equals(mname))
      {
         next = args[0];
         return null;
      }
      else if ("getNext".equals(mname))
      {
         return next;
      }
      else if ("invoke".equals(mname))
      {
         try
         {
            Object req = args[0];
            Object resp = args[1];
            Invocation invocation = new Invocation((HttpServletRequest)req, (HttpServletResponse)resp);
            local.set(invocation);
            return method.invoke(next, args);
         }
         finally
         {
            local.set(null);
         }
      }
      else
      {
         // getInfo()
         return "A valve that setup a thread local assocation with request and response";
      }
   }

   public static void setRequestAndResponse(HttpServletRequest request, HttpServletResponse response)
   {
      local.set(new Invocation(request, response));
   }

   public static HttpServletRequest getRequest()
   {
      Invocation invocation = local.get();
      return invocation != null ? invocation.req : null;
   }

   public static HttpServletResponse getResponse()
   {
      return local.get().resp;
   }

   private static class Invocation
   {
      private final HttpServletRequest req;
      private final HttpServletResponse resp;

      public Invocation(HttpServletRequest req, HttpServletResponse resp)
      {
         this.req = req;
         this.resp = resp;
      }
   }

}
