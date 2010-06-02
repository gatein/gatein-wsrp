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

package org.gatein.wsrp.services;

import org.gatein.common.util.ParameterValidation;
import org.gatein.wsrp.services.v1.V1MarkupService;
import org.gatein.wsrp.services.v1.V1PortletManagementService;
import org.gatein.wsrp.services.v1.V1RegistrationService;
import org.gatein.wsrp.services.v1.V1ServiceDescriptionService;
import org.gatein.wsrp.services.v2.V2MarkupService;
import org.gatein.wsrp.services.v2.V2PortletManagementService;
import org.gatein.wsrp.services.v2.V2RegistrationService;
import org.gatein.wsrp.services.v2.V2ServiceDescriptionService;
import org.oasis.wsrp.v1.WSRPV1MarkupPortType;
import org.oasis.wsrp.v1.WSRPV1PortletManagementPortType;
import org.oasis.wsrp.v1.WSRPV1RegistrationPortType;
import org.oasis.wsrp.v1.WSRPV1ServiceDescriptionPortType;
import org.oasis.wsrp.v2.WSRPV2MarkupPortType;
import org.oasis.wsrp.v2.WSRPV2PortletManagementPortType;
import org.oasis.wsrp.v2.WSRPV2RegistrationPortType;
import org.oasis.wsrp.v2.WSRPV2ServiceDescriptionPortType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class SOAPServiceFactory implements ManageableServiceFactory
{
   private final Logger log = LoggerFactory.getLogger(getClass());

   private String wsdlDefinitionURL;

   private boolean isV2 = false;

   private static final String WSRP_V1_URN = "urn:oasis:names:tc:wsrp:v1:wsdl";
   private final static QName V1_SERVICE = new QName(WSRP_V1_URN, "WSRPService");
   private static final String WSRP_V2_URN = "urn:oasis:names:tc:wsrp:v2:wsdl";
   private final static QName V2_SERVICE = new QName(WSRP_V2_URN, "WSRPService");

   private Map<Class, Object> services = new ConcurrentHashMap<Class, Object>();
   private String markupURL;
   private String serviceDescriptionURL;
   private String portletManagementURL;
   private String registrationURL;
   private boolean failed;
   private boolean available;
   private int msBeforeTimeOut = DEFAULT_TIMEOUT_MS;

   public <T> T getService(Class<T> clazz) throws Exception
   {
      // todo: clean up!

      if (log.isDebugEnabled())
      {
         log.debug("Getting service for class " + clazz);
      }

      // if we need a refresh, reload information from WSDL
      if (!isAvailable() && !isFailed())
      {
         start();
      }

      Object service = services.get(clazz);

      //
      String portAddress = null;
      boolean isMandatoryInterface = false;
      if (WSRPV2ServiceDescriptionPortType.class.isAssignableFrom(clazz)
         || WSRPV1ServiceDescriptionPortType.class.isAssignableFrom(clazz))
      {
         portAddress = serviceDescriptionURL;
         isMandatoryInterface = true;
      }
      else if (WSRPV2MarkupPortType.class.isAssignableFrom(clazz)
         || WSRPV1MarkupPortType.class.isAssignableFrom(clazz))
      {
         portAddress = markupURL;
         isMandatoryInterface = true;
      }
      else if (WSRPV2RegistrationPortType.class.isAssignableFrom(clazz)
         || WSRPV1RegistrationPortType.class.isAssignableFrom(clazz))
      {
         portAddress = registrationURL;
      }
      else if (WSRPV2PortletManagementPortType.class.isAssignableFrom(clazz)
         || WSRPV1PortletManagementPortType.class.isAssignableFrom(clazz))
      {
         portAddress = portletManagementURL;
      }

      // Get the stub from the service, remember that the stub itself is not threadsafe
      // and must be customized for every request to this method.
      if (service != null)
      {
         if (portAddress != null)
         {
            if (log.isDebugEnabled())
            {
               log.debug("Setting the end point to: " + portAddress);
            }

            T result = ServiceWrapper.getServiceWrapper(clazz, service, portAddress, this);

            // if we managed to retrieve a service, we're probably available
            setFailed(false);
            setAvailable(true);

            return result;
         }
         else
         {
            if (isMandatoryInterface)
            {
               setFailed(true);
               throw new IllegalStateException("Mandatory interface URLs were not properly initialized: no proper service URL for "
                  + clazz.getName());
            }
            else
            {
               throw new IllegalStateException("No URL was provided for optional interface " + clazz.getName());
            }
         }
      }
      else
      {
         return null;
      }
   }

   public boolean isAvailable()
   {
      return available;
   }

   public boolean isFailed()
   {
      return failed;
   }


   public void stop()
   {
      // todo: implement as needed
   }

   public void setFailed(boolean failed)
   {
      this.failed = failed;
   }

   public void setAvailable(boolean available)
   {
      this.available = available;
   }

   public void setWSOperationTimeOut(int msBeforeTimeOut)
   {
      if (msBeforeTimeOut < 0)
      {
         msBeforeTimeOut = DEFAULT_TIMEOUT_MS;
      }

      this.msBeforeTimeOut = msBeforeTimeOut;
   }

   public int getWSOperationTimeOut()
   {
      return msBeforeTimeOut;
   }

   public String getWsdlDefinitionURL()
   {
      return wsdlDefinitionURL;
   }

   public void setWsdlDefinitionURL(String wsdlDefinitionURL)
   {
      this.wsdlDefinitionURL = wsdlDefinitionURL;

      // we need a refresh so mark as not available but not failed
      setAvailable(false);
      setFailed(false);
   }

   public void start() throws Exception
   {
      try
      {
         ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(wsdlDefinitionURL, "WSDL URL", "SOAPServiceFactory");
         URI wsdlURL = new URI(wsdlDefinitionURL);

         // try to get v2 of service if possible, first
         Service service;
         try
         {
            service = Service.create(wsdlURL.toURL(), V2_SERVICE);

            WSRPV2MarkupPortType markupPortType = service.getPort(WSRPV2MarkupPortType.class);
            services.put(WSRPV2MarkupPortType.class, markupPortType);
            markupURL = (String)((BindingProvider)markupPortType).getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);

            WSRPV2ServiceDescriptionPortType sdPort = service.getPort(WSRPV2ServiceDescriptionPortType.class);
            services.put(WSRPV2ServiceDescriptionPortType.class, sdPort);
            serviceDescriptionURL = (String)((BindingProvider)sdPort).getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);

            WSRPV2PortletManagementPortType managementPortType = service.getPort(WSRPV2PortletManagementPortType.class);
            services.put(WSRPV2PortletManagementPortType.class, managementPortType);
            portletManagementURL = (String)((BindingProvider)managementPortType).getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);

            WSRPV2RegistrationPortType registrationPortType = service.getPort(WSRPV2RegistrationPortType.class);
            services.put(WSRPV2RegistrationPortType.class, registrationPortType);
            registrationURL = (String)((BindingProvider)registrationPortType).getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);

            setFailed(false);
            setAvailable(true);
            isV2 = true;
         }
         catch (IllegalArgumentException e)
         {
            // if exception message contains both URNs, then it should mean that we only have V1 service, so get that
            // todo: we could allow user to choose what happens here instead of proceeding automatically...
            String message = e.getMessage();
            if (message.contains(WSRP_V1_URN) && message.contains(WSRP_V2_URN))
            {
               service = Service.create(wsdlURL.toURL(), V1_SERVICE);

               WSRPV1MarkupPortType markupPortType = service.getPort(WSRPV1MarkupPortType.class);
               services.put(WSRPV1MarkupPortType.class, markupPortType);
               markupURL = (String)((BindingProvider)markupPortType).getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);

               WSRPV1ServiceDescriptionPortType sdPort = service.getPort(WSRPV1ServiceDescriptionPortType.class);
               services.put(WSRPV1ServiceDescriptionPortType.class, sdPort);
               serviceDescriptionURL = (String)((BindingProvider)sdPort).getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);

               WSRPV1PortletManagementPortType managementPortType = service.getPort(WSRPV1PortletManagementPortType.class);
               services.put(WSRPV1PortletManagementPortType.class, managementPortType);
               portletManagementURL = (String)((BindingProvider)managementPortType).getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);

               WSRPV1RegistrationPortType registrationPortType = service.getPort(WSRPV1RegistrationPortType.class);
               services.put(WSRPV1RegistrationPortType.class, registrationPortType);
               registrationURL = (String)((BindingProvider)registrationPortType).getRequestContext().get(BindingProvider.ENDPOINT_ADDRESS_PROPERTY);

               setFailed(false);
               setAvailable(true);
               isV2 = false;
            }
            else
            {
               throw new IllegalArgumentException("Couldn't find any WSRP service in specified WSDL: " + wsdlDefinitionURL);
            }
         }
      }
      catch (MalformedURLException e)
      {
         setFailed(true);
         throw new IllegalArgumentException(wsdlDefinitionURL + " is not a well-formed URL specifying where to find the WSRP services definition.", e);
      }
      catch (Exception e)
      {
         log.info("Couldn't access WSDL information. Service won't be available", e);
         setAvailable(false);
         setFailed(true);
         throw e;
      }
   }

   public ServiceDescriptionService getServiceDescriptionService() throws Exception
   {
      if (isV2)
      {
         WSRPV2ServiceDescriptionPortType port = getService(WSRPV2ServiceDescriptionPortType.class);
         return new V2ServiceDescriptionService(port);
      }
      else
      {
         WSRPV1ServiceDescriptionPortType port = getService(WSRPV1ServiceDescriptionPortType.class);
         return new V1ServiceDescriptionService(port);
      }
   }

   public MarkupService getMarkupService() throws Exception
   {
      if (isV2)
      {
         WSRPV2MarkupPortType port = getService(WSRPV2MarkupPortType.class);
         return new V2MarkupService(port);
      }
      else
      {
         WSRPV1MarkupPortType port = getService(WSRPV1MarkupPortType.class);
         return new V1MarkupService(port);
      }
   }

   public PortletManagementService getPortletManagementService() throws Exception
   {
      if (isV2)
      {
         WSRPV2PortletManagementPortType port = getService(WSRPV2PortletManagementPortType.class);
         return new V2PortletManagementService(port);
      }
      else
      {
         WSRPV1PortletManagementPortType port = getService(WSRPV1PortletManagementPortType.class);
         return new V1PortletManagementService(port);
      }
   }

   public RegistrationService getRegistrationService() throws Exception
   {
      if (isV2)
      {
         WSRPV2RegistrationPortType port = getService(WSRPV2RegistrationPortType.class);
         return new V2RegistrationService(port);
      }
      else
      {
         WSRPV1RegistrationPortType port = getService(WSRPV1RegistrationPortType.class);
         return new V1RegistrationService(port);
      }
   }
}
