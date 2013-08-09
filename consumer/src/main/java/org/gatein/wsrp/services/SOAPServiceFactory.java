/*
 * JBoss, a division of Red Hat
 * Copyright 2011, Red Hat Middleware, LLC, and individual
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
import org.gatein.wsrp.handler.RequestHeaderClientHandler;
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
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;
import javax.xml.ws.handler.Handler;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class SOAPServiceFactory implements ManageableServiceFactory
{
   /**
    * HTTP request timeout property. JAX-WS doesn't standardize that value, so needs to be adapted per used
    * implementation
    */
   private static final String JBOSS_WS_TIMEOUT = "org.jboss.ws.timeout";
   private static final String SUN_WS_TIMEOUT = "com.sun.xml.ws.request.timeout";
   private static final String IBM_WS_TIMEOUT = "com.ibm.SOAP.requestTimeout";

   private static final RequestHeaderClientHandler REQUEST_HEADER_CLIENT_HANDLER = new RequestHeaderClientHandler();
   private static final String JBOSS_WS_STUBEXT_PROPERTY_CHUNKED_ENCODING_SIZE = "http://org.jboss.ws/http#chunksize";

   private static final Logger log = LoggerFactory.getLogger(SOAPServiceFactory.class);
   private String wsdlDefinitionURL;

   private boolean isV2 = false;
   private Service wsService;

   private static final String WSRP_V1_BINDING = "urn:oasis:names:tc:wsrp:v1:bind";
   private static final String WSRP_V2_BINDING = "urn:oasis:names:tc:wsrp:v2:bind";

   private boolean failed;
   private boolean available;
   private int msBeforeTimeOut = DEFAULT_TIMEOUT_MS;

   private final ConcurrentHashMap<Class, Object> ports = new ConcurrentHashMap<Class, Object>(7);

   @Override
   public String toString()
   {
      return "SOAPServiceFactory (@" + Integer.toHexString(hashCode()) + ") URL=" + wsdlDefinitionURL + ", WSRP version " + (isV2 ? "2" : "1");
   }

   private void setTimeout(Map<String, Object> requestContext)
   {
      int timeout = getWSOperationTimeOut();
      requestContext.put(JBOSS_WS_TIMEOUT, timeout);
      requestContext.put(SUN_WS_TIMEOUT, timeout);
      requestContext.put(IBM_WS_TIMEOUT, timeout);
   }

   private <T> T customizePort(Class<T> expectedServiceInterface, Object service)
   {
      BindingProvider bindingProvider = (BindingProvider)service;
      Map<String, Object> requestContext = bindingProvider.getRequestContext();

      // set timeout
      setTimeout(requestContext);

      // Set org.jboss.ws.core.StubExt.PROPERTY_CHUNKED_ENCODING_SIZE to 0 to deactive chunked encoding for
      // better interoperability as Oracle's producer doesn't support it, for example.
      // See https://jira.jboss.org/jira/browse/JBWS-2884 and
      // http://community.jboss.org/wiki/JBossWS-NativeUserGuide#Chunked_encoding_setup
      requestContext.put(JBOSS_WS_STUBEXT_PROPERTY_CHUNKED_ENCODING_SIZE, "0");

      // Add client side handler via JAX-WS API
      Binding binding = bindingProvider.getBinding();
      List<Handler> handlerChain = binding.getHandlerChain();
      if (handlerChain != null)
      {
         // if we already have a handler chain, just add the request hearder handler if it's not already in there
         if (!handlerChain.contains(REQUEST_HEADER_CLIENT_HANDLER))
         {
            handlerChain.add(REQUEST_HEADER_CLIENT_HANDLER);
         }
      }
      else
      {
         // otherwise, create a handler chain and add our handler to it
         handlerChain = new ArrayList<Handler>(1);
         handlerChain.add(REQUEST_HEADER_CLIENT_HANDLER);
      }
      binding.setHandlerChain(handlerChain);

      return expectedServiceInterface.cast(service);
   }

   public <T> T getService(Class<T> clazz) throws Exception
   {
      if (log.isDebugEnabled())
      {
         log.debug("Getting service for class " + clazz);
      }

      refresh(false);

      Object service = ports.get(clazz);
      if (service == null)
      {
         return initPortFor(clazz);
      }
      else
      {
         return clazz.cast(service);
      }
   }

   private <T> T initPortFor(Class<T> clazz)
   {
      try
      {
         // need to be careful about: http://cxf.apache.org/faq.html#FAQ-AreJAXWSclientproxiesthreadsafe

         T result = customizePort(clazz, wsService.getPort(clazz));

         // if we managed to retrieve a service, we're probably available
         setFailed(false);
         setAvailable(true);

         ports.put(clazz, result);

         return result;
      }
      catch (Exception e)
      {
         log.debug("No port available for " + clazz, e);
         if (WSRPV2ServiceDescriptionPortType.class.isAssignableFrom(clazz) || WSRPV1ServiceDescriptionPortType.class.isAssignableFrom(clazz)
            || WSRPV2MarkupPortType.class.isAssignableFrom(clazz) || WSRPV1MarkupPortType.class.isAssignableFrom(clazz))
         {
            setFailed(true);
            throw new IllegalArgumentException("Mandatory interface URLs were not properly initialized: no proper service URL for " + clazz.getName(), e);
         }
         else
         {
            log.debug("No URL was provided for optional interface " + clazz.getName(), e);
         }
      }

      return null;
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
      if (!isAvailable())
      {
         try
         {
            ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(wsdlDefinitionURL, "WSDL URL", "SOAPServiceFactory");
            URL wsdlURL = new URI(wsdlDefinitionURL).toURL();

            WSDLInfo wsdlInfo = new WSDLInfo(wsdlDefinitionURL);

            // try to get v2 of service if possible, first
            QName wsrp2 = wsdlInfo.getWSRP2ServiceQName();
            QName wsrp1 = wsdlInfo.getWSRP1ServiceQName();
            if (wsrp2 != null)
            {
               wsService = Service.create(wsdlURL, wsrp2);

               // init the service ports
               initPortFor(WSRPV2MarkupPortType.class);
               initPortFor(WSRPV2ServiceDescriptionPortType.class);
               initPortFor(WSRPV2PortletManagementPortType.class);
               initPortFor(WSRPV2RegistrationPortType.class);

               setFailed(false);
               setAvailable(true);
               isV2 = true;
            }
            else if (wsrp1 != null)
            {
               wsService = Service.create(wsdlURL, wsrp1);

               // init the service ports
               initPortFor(WSRPV1MarkupPortType.class);
               initPortFor(WSRPV1ServiceDescriptionPortType.class);
               initPortFor(WSRPV1PortletManagementPortType.class);
               initPortFor(WSRPV1RegistrationPortType.class);

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
            log.info("Couldn't access WSDL information at " + wsdlDefinitionURL + ". Service won't be available", e);
            setAvailable(false);
            setFailed(true);
            throw e;
         }
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

   public boolean refresh(boolean force) throws Exception
   {
      // if we need a refresh, reload information from WSDL
      if (force || (!isAvailable() && !isFailed()))
      {
         start();

         return true;
      }

      return false;
   }

   @Override
   public ServiceFactory clone()
   {
      final SOAPServiceFactory factory = new SOAPServiceFactory();
      factory.msBeforeTimeOut = this.msBeforeTimeOut;
      factory.wsdlDefinitionURL = this.wsdlDefinitionURL;
      return factory;
   }

   protected static class WSDLInfo
   {
      private final QName wsrp2ServiceQName;
      private final QName wsrp1ServiceQName;
      private static final WSDLFactory wsdlFactory;

      static
      {
         try
         {
            wsdlFactory = WSDLFactory.newInstance();
         }
         catch (WSDLException e)
         {
            throw new RuntimeException(e);
         }
      }

      public WSDLInfo(String wsdlURL) throws WSDLException
      {
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
         String ns = definition.getTargetNamespace();
         for (QName name : services.keySet())
         {
            javax.wsdl.Service service = services.get(name);

            // if the namespace is using one of the WSRP-defined ones, we have a potential candidate
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
