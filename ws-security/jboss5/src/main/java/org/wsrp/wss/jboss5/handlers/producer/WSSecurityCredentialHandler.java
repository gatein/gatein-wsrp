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

import org.jboss.web.tomcat.security.login.WebAuthentication;
import org.jboss.wsf.spi.SPIProvider;
import org.jboss.wsf.spi.SPIProviderResolver;
import org.jboss.wsf.spi.invocation.SecurityAdaptor;
import org.jboss.wsf.spi.invocation.SecurityAdaptorFactory;
import org.wsrp.wss.jboss5.handlers.AbstractWSSecurityCredentialHandler;

import javax.xml.ws.handler.soap.SOAPMessageContext;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class WSSecurityCredentialHandler extends AbstractWSSecurityCredentialHandler
{
   @Override
   protected boolean handleResponse(SOAPMessageContext soapMessageContext)
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

}

