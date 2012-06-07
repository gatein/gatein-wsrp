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
package org.wsrp.wss.jboss5.handlers.producer;

import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.jboss.web.tomcat.security.login.WebAuthentication;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.invocation.SecurityAdaptor;
import org.jboss.wsf.spi.invocation.SecurityAdaptorFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class WSSecurityCredentialHandler implements SOAPHandler<SOAPMessageContext>
   {
      private static Logger log = LoggerFactory.getLogger(WSSecurityCredentialHandler.class);

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

      private boolean handleResponse(SOAPMessageContext soapMessageContext)
      {
         try
         {
            log.debug("Attempting to add the security Credentials to the current Request");

            SPIProvider spiProvider = SPIProviderResolver.getInstance().getProvider();
            SecurityAdaptor securityAdaptor = spiProvider.getSPI(SecurityAdaptorFactory.class).newSecurityAdapter();

            if (securityAdaptor != null && securityAdaptor.getPrincipal() != null && securityAdaptor.getPrincipal().getName() != null && securityAdaptor.getCredential() != null)
            {
               WebAuthentication wa = new WebAuthentication();
               wa.login(securityAdaptor.getPrincipal().getName(), securityAdaptor.getCredential());
            }
            else
            {
               log.debug("No securityAdaptor available. Cannot add credentials from the WS Security");
            }

         }
         catch (Exception e)
         {
            log.warn("Error occured when trying to programatically login using the ws-security credentials.", e);
         }

         return true;
      }

      private boolean handleRequest(SOAPMessageContext soapMessageContext)
      {
         return true;
      }

      public Set<QName> getHeaders()
      {
         return null;
      }
   }

