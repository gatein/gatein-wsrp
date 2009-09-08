/*
 * JBoss, a division of Red Hat
 * Copyright 2009, Red Hat Middleware, LLC, and individual
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

package org.gatein.wsrp.services;

import org.oasis.wsrp.v1.WSRPV1MarkupPortType;
import org.oasis.wsrp.v1.WSRPV1PortletManagementPortType;
import org.oasis.wsrp.v1.WSRPV1RegistrationPortType;
import org.oasis.wsrp.v1.WSRPV1ServiceDescriptionPortType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 12056 $
 * @since 2.4 (May 3, 2006)
 */
public class RemoteSOAPInvokerServiceFactory extends PerEndpointSOAPInvokerServiceFactory
{
   private final Logger log = LoggerFactory.getLogger(getClass());

   private String wsdlDefinitionURL;

   private final static QName SERVICE = new QName("urn:oasis:names:tc:wsrp:v1:wsdl", "WSRPService");
   private final static QName WSRPServiceDescriptionService = new QName("urn:oasis:names:tc:wsrp:v1:wsdl", "WSRPServiceDescriptionService");
   private final static QName WSRPBaseService = new QName("urn:oasis:names:tc:wsrp:v1:wsdl", "WSRPBaseService");
   private final static QName WSRPPortletManagementService = new QName("urn:oasis:names:tc:wsrp:v1:wsdl", "WSRPPortletManagementService");
   private final static QName WSRPRegistrationService = new QName("urn:oasis:names:tc:wsrp:v1:wsdl", "WSRPRegistrationService");

   public String getWsdlDefinitionURL()
   {
      return wsdlDefinitionURL;
   }

   public void setWsdlDefinitionURL(String wsdlDefinitionURL) throws Exception
   {
      if (wsdlDefinitionURL == null || wsdlDefinitionURL.length() == 0)
      {
         throw new IllegalArgumentException("Require a non-empty, non-null URL specifying where to find the WSRP " +
            "services definition");
      }

      if (!wsdlDefinitionURL.equals(this.wsdlDefinitionURL))
      {
         this.wsdlDefinitionURL = wsdlDefinitionURL;

         try
         {
            initServices();
            setFailed(false);
            setAvailable(true);
         }
         catch (MalformedURLException e)
         {
            setFailed(true);
            throw new IllegalArgumentException("Require a well-formed URL specifying where to find the WSRP services definition", e);
         }
         catch (Exception e)
         {
            log.info("Couldn't access WSDL information. Service won't be available", e);
            setAvailable(false);
            setFailed(true);
            throw e;
         }
      }
   }

   private void initServices() throws MalformedURLException
   {
      URL wsdlURL = new URL(wsdlDefinitionURL);
      Service service = Service.create(wsdlURL, SERVICE);

      WSRPV1MarkupPortType markupPortType = service.getPort(WSRPBaseService, WSRPV1MarkupPortType.class);
      markupURL = (String)((BindingProvider)markupPortType).getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);

      WSRPV1ServiceDescriptionPortType sdPort = service.getPort(WSRPServiceDescriptionService, WSRPV1ServiceDescriptionPortType.class);
      serviceDescriptionURL = (String)((BindingProvider)sdPort).getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);

      WSRPV1PortletManagementPortType managementPortType = service.getPort(WSRPPortletManagementService, WSRPV1PortletManagementPortType.class);
      portletManagementURL = (String)((BindingProvider)managementPortType).getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);

      WSRPV1RegistrationPortType registrationPortType = service.getPort(WSRPRegistrationService, WSRPV1RegistrationPortType.class);
      registrationURL = (String)((BindingProvider)registrationPortType).getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);
   }
}
