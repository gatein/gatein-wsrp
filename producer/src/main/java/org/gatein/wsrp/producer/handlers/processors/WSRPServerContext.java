/*
* JBoss, a division of Red Hat
* Copyright 2012, Red Hat Middleware, LLC, and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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

package org.gatein.wsrp.producer.handlers.processors;

import org.gatein.pc.portlet.impl.spi.AbstractServerContext;
import org.gatein.wci.RequestDispatchCallback;
import org.gatein.wci.ServletContainer;
import org.gatein.wci.ServletContainerFactory;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a> */
public class WSRPServerContext extends AbstractServerContext
{

   public WSRPServerContext(HttpServletRequest clientRequest, HttpServletResponse clientResponse)
   {
      super(clientRequest, clientResponse);
   }

   @Override
   public void dispatch(
      ServletContext target,
      HttpServletRequest request,
      HttpServletResponse response,
      final Callable callable) throws Exception
   {
      ServletContainer container = ServletContainerFactory.getServletContainer();
      container.include(target, request, response, new RequestDispatchCallback()
      {
         @Override
         public Object doCallback(
            ServletContext dispatchedServletContext,
            HttpServletRequest dispatchedRequest,
            HttpServletResponse dispatchedResponse,
            Object handback) throws ServletException, IOException
         {
            callable.call(dispatchedServletContext, dispatchedRequest, dispatchedResponse);

            // We don't use return value anymore
            return null;
         }
      }, null);
   }
}