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
import org.gatein.common.util.Version;
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

import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
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
   private static final String WSRP_V1_BINDING = "urn:oasis:names:tc:wsrp:v1:bind";
   private static final String WSRP_V2_URN = "urn:oasis:names:tc:wsrp:v2:wsdl";
   private static final String WSRP_V2_BINDING = "urn:oasis:names:tc:wsrp:v2:bind";

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
         URL wsdlURL = new URI(wsdlDefinitionURL).toURL();

         WSDLInfo wsdlInfo = new WSDLInfo(wsdlDefinitionURL);

         // try to get v2 of service if possible, first
         QName wsrp2 = wsdlInfo.getWSRP2ServiceQName();
         QName wsrp1 = wsdlInfo.getWSRP1ServiceQName();
         Service service;
         if (wsrp2 != null)
         {
            service = Service.create(wsdlURL, wsrp2);

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
         else if (wsrp1 != null)
         {
            service = Service.create(wsdlURL, wsrp1);

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

   public Version getWSRPVersion()
   {
      if (isAvailable())
      {
         if (isV2)
         {
            return WSRP2;
         }
         else
         {
            return WSRP1;
         }
      }
      else
      {
         return null;
      }
   }

   protected class WSDLInfo
   {
      private final QName wsrp2ServiceQName;
      private final QName wsrp1ServiceQName;

      public WSDLInfo(String wsdlURL) throws WSDLException
      {
         WSDLFactory wsdlFactory = WSDLFactory.newInstance();
         WSDLReader wsdlReader = wsdlFactory.newWSDLReader();

         wsdlReader.setFeature("javax.wsdl.verbose", false);
         wsdlReader.setFeature("javax.wsdl.importDocuments", false);

         Definition definition = wsdlReader.readWSDL(wsdlURL);
         Map<QName, javax.wsdl.Service> services = definition.getServices();
         int serviceNb = services.size();
         if (serviceNb > 2)
         {
            throw new WSDLException(WSDLException.OTHER_ERROR,
               "The specified WSDL contains more than 2 services definitions when we expected at most 2: one for WSRP 1 and one for WSRP 2.");
         }

         QName wsrp1 = null, wsrp2 = null;
         for (QName name : services.keySet())
         {
            String ns = name.getNamespaceURI();
            javax.wsdl.Service service = services.get(name);

            // if the namespace is using one of the WSRP-defined ones, we have a potential candidate
            if (WSRP_V1_URN.equals(ns) || WSRP_V2_URN.equals(ns))
            {
               // but we need to check that the port namespaces to really know which version of the service we've found
               // this is needed for http://www.netunitysoftware.com/wsrp2interop/WsrpProducer.asmx?Operation=WSDL&WsrpVersion=All
               // where the WSRP1 service name has the WSRP2 global target namespace so we need more processing :(
               Map<String, Port> ports = service.getPorts();
               String bindingNSURI = null;
               for (Port port : ports.values())
               {
                  QName bindingName = port.getBinding().getQName();
                  String newBindingNS = bindingName.getNamespaceURI();
                  if (WSRP_V1_BINDING.equals(newBindingNS) || WSRP_V2_BINDING.equals(newBindingNS))
                  {
                     if (bindingNSURI != null && !bindingNSURI.equals(newBindingNS))
                     {
                        throw new WSDLException(WSDLException.OTHER_ERROR, "Inconsistent NS in port bindings. Aborting.");
                     }
                     bindingNSURI = newBindingNS;
                  }
                  else
                  {
                     log.debug("Unknown binding namespace: " + newBindingNS + ". Ignoring binding: " + bindingName);
                  }
               }
               if (WSRP_V1_BINDING.equals(bindingNSURI))
               {
                  wsrp1 = checkPotentialServiceName(wsrp1, name, ns);
               }
               else if (WSRP_V2_BINDING.equals(bindingNSURI))
               {
                  wsrp2 = checkPotentialServiceName(wsrp2, name, ns);
               }
            }
            else
            {
               log.debug("Unknown service namespace: " + ns);
            }
         }

         wsrp2ServiceQName = wsrp2;
         wsrp1ServiceQName = wsrp1;

         if (wsrp1 == null && wsrp2 == null)
         {
            throw new WSDLException(WSDLException.INVALID_WSDL,
               "Found no service definition with WSRP specification namespaces.");
         }
      }

      public QName getWSRP2ServiceQName()
      {
         return wsrp2ServiceQName;
      }

      public QName getWSRP1ServiceQName()
      {
         return wsrp1ServiceQName;
      }

      private QName checkPotentialServiceName(QName potentiallyExisting, QName candidate, String namespace) throws WSDLException
      {
         if (potentiallyExisting != null)
         {
            throw new WSDLException(WSDLException.OTHER_ERROR, "Found 2 different services using the "
               + namespace + " namespace. Cannot decide which one to use for service so aborting.");
         }
         return candidate;
      }
   }
}
