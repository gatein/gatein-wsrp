/*
* JBoss, a division of Red Hat
* Copyright 2008, Red Hat Middleware, LLC, and individual contributors as indicated
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

package org.wsrp.wss.jboss5.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Set;

/** @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a> */
public abstract class AbstractWSSecurityCredentialHandler implements SOAPHandler<SOAPMessageContext>
{
   protected static final Logger log = LoggerFactory.getLogger(AbstractWSSecurityCredentialHandler.class);

   public void close(MessageContext arg0)
   {
      //Nothing to do for now
   }

   public boolean handleFault(SOAPMessageContext soapMessageContext)
   {
      return true;
   }

   public boolean handleMessage(SOAPMessageContext soapMessageContext)
   {
      if (Boolean.TRUE.equals(soapMessageContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)))
      {
         return handleRequest(soapMessageContext);
      }
      else
      {
         return handleResponse(soapMessageContext);
      }
   }

   protected boolean handleRequest(SOAPMessageContext soapMessageContext)
   {
      return true;
   }

   protected boolean handleResponse(SOAPMessageContext soapMessageContext)
   {
      return true;
   }

   public Set<QName> getHeaders()
   {
      return null;
   }

}
