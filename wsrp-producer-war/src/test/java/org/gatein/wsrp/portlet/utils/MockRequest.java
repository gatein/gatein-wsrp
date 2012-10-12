/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2012, Red Hat Middleware, LLC, and individual                    *
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
package org.gatein.wsrp.portlet.utils;

import javax.servlet.http.HttpSession;

import org.apache.catalina.connector.Request;
import org.gatein.wsrp.test.support.MockHttpSession;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class MockRequest extends Request
{

   HttpSession session;
   
   public MockRequest()
   {
      super();
      this.session = MockHttpSession.createMockSession();
   }
   
   @Override
   public String getMethod()
   {
      return "GET";
   }
   
   @Override
   public HttpSession getSession()
   {
      return this.session;
   }
   
   @Override
   public HttpSession getSession(boolean create)
   {
      if (session == null && create)
      {
         session = MockHttpSession.createMockSession();
      }
      
      return session;
   }
   
   @Override
   public String getScheme()
   {
      return "http";
   }
   
   @Override
   public String getServerName()
   {
      return "localhost";
   }
   
   @Override
   public int getServerPort()
   {
      return 8080;
   }
}

