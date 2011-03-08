/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2011, Red Hat Middleware, LLC, and individual                    *
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
package org.gatein.wsrp.wss;

import java.util.ArrayList;
import java.util.List;

import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class WebServiceSecurityFactory
{

   public static final WebServiceSecurityFactory instance = new WebServiceSecurityFactory();
   
   public static WebServiceSecurityFactory getInstance()
   {
      return instance;
   }
   
   private List<SOAPHandler<SOAPMessageContext>> handlers;
   
   public void registerWebServiceSecurityHandler(SOAPHandler<SOAPMessageContext> handler)
   {
      if (handlers == null)
      {
         handlers = new ArrayList<SOAPHandler<SOAPMessageContext>>();
      }
      handlers.add(handler);
   }
   
   public List<SOAPHandler<SOAPMessageContext>> getHandlers()
   {
      return handlers;
   }
   
   public void unregisterWebServiceSecurityHandler(SOAPHandler<SOAPMessageContext> handler)
   {
      if (handlers != null)
      {
         handlers.remove(handler);
      }
   }
   
}

