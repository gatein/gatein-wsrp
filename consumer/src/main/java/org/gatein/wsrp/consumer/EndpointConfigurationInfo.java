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

package org.gatein.wsrp.consumer;

import org.gatein.common.util.ParameterValidation;
import org.gatein.pc.api.InvokerUnavailableException;
import org.gatein.wsrp.services.SOAPServiceFactory;
import org.gatein.wsrp.services.ServiceFactory;
import org.oasis.wsrp.v1.WSRPV1MarkupPortType;
import org.oasis.wsrp.v1.WSRPV1PortletManagementPortType;
import org.oasis.wsrp.v1.WSRPV1RegistrationPortType;
import org.oasis.wsrp.v1.WSRPV1ServiceDescriptionPortType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.BitSet;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 13122 $
 * @since 2.6
 */
public class EndpointConfigurationInfo
{
   private final static Logger log = LoggerFactory.getLogger(EndpointConfigurationInfo.class);

   /** DB primary key */
   private Long key;

   private String persistentServiceDescriptionURL = UNSET;
   private String persistentMarkupURL = UNSET;
   private String persistentRegistrationURL;
   private String persistentPortletManagementURL;
   private String persistentWsdlDefinitionURL = UNSET;

   // transient variables
   /** Access to the WS */
   private transient ServiceFactory serviceFactory;
   private transient String remoteHostAddress;

   // Used to ensure that even invalid values can be persisted to DB so that it can be accessed from the GUI
   public final static String UNSET = "MUST BE SET";

   // maintain the dirty status of each URL
   private BitSet clean = new BitSet();
   private final static int SD = 0;
   private final static int M = 1;
   private final static int PM = 2;
   private final static int R = 3;

   /** Whether we're using information from a WSDL or not. */
   private boolean usingWSDL = true;
   private boolean isModifiedWSDL;

   public EndpointConfigurationInfo(ProducerInfo producerInfo)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(producerInfo, "ProducerInfo");
      producerInfo.setEndpointConfigurationInfo(this);
      serviceFactory = new SOAPServiceFactory();
   }

   EndpointConfigurationInfo(ProducerInfo producerInfo, ServiceFactory serviceFactory)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(producerInfo, "ProducerInfo");
      producerInfo.setEndpointConfigurationInfo(this);
      this.serviceFactory = serviceFactory;
   }

   public Long getKey()
   {
      return key;
   }

   public void setKey(Long key)
   {
      this.key = key;
   }

   public boolean usesWSDL()
   {
      return (isWSDLNotNullAndSet() && !isModifiedWSDL) || usingWSDL;
   }

   public void setUsesWSDL(boolean useWSDL)
   {
      usingWSDL = useWSDL;
   }

   public String getWsdlDefinitionURL()
   {
      if (serviceFactory != null)
      {
         persistentWsdlDefinitionURL = (serviceFactory).getWsdlDefinitionURL();
      }

      return persistentWsdlDefinitionURL;
   }

   public String getServiceDescriptionURL()
   {
      if (serviceFactory != null)
      {
         persistentServiceDescriptionURL = serviceFactory.getServiceDescriptionURL();
      }
      return persistentServiceDescriptionURL;
   }

   public String getMarkupURL()
   {
      if (serviceFactory != null)
      {
         persistentMarkupURL = serviceFactory.getMarkupURL();
      }
      return persistentMarkupURL;
   }

   public String getPortletManagementURL()
   {
      if (serviceFactory != null)
      {
         persistentPortletManagementURL = serviceFactory.getPortletManagementURL();
      }
      return persistentPortletManagementURL;
   }

   public String getRegistrationURL()
   {
      if (serviceFactory != null)
      {
         persistentRegistrationURL = serviceFactory.getRegistrationURL();
      }
      return persistentRegistrationURL;
   }

   public void setServiceDescriptionURL(String serviceDescriptionURL)
   {
      if (serviceFactory != null)
      {
         serviceFactory.setServiceDescriptionURL(serviceDescriptionURL);
      }
      this.persistentServiceDescriptionURL = modifyIfNeeded(this.persistentServiceDescriptionURL, serviceDescriptionURL, SD);

   }

   public void setMarkupURL(String markupURL)
   {
      if (serviceFactory != null)
      {
         serviceFactory.setMarkupURL(markupURL);
      }
      this.persistentMarkupURL = modifyIfNeeded(this.persistentMarkupURL, markupURL, M);
   }

   public void setRegistrationURL(String registrationURL)
   {
      if (serviceFactory != null)
      {
         serviceFactory.setRegistrationURL(registrationURL);
      }
      this.persistentRegistrationURL = modifyIfNeeded(this.persistentRegistrationURL, registrationURL, R);
   }

   public void setPortletManagementURL(String portletManagementURL)
   {
      if (serviceFactory != null)
      {
         serviceFactory.setPortletManagementURL(portletManagementURL);
      }
      this.persistentPortletManagementURL = modifyIfNeeded(this.persistentPortletManagementURL, portletManagementURL, PM);
   }

   public void setWsdlDefinitionURL(String wsdlDefinitionURL) throws RuntimeException
   {
      this.persistentWsdlDefinitionURL = wsdlDefinitionURL;

      // WSDL url is optional so can be null (and in particular, it is when loaded from Hibernate most of the time)
      // do not attempt to set the URL if the service factory hasn't been created yet to avoid issues when
      // ConsumerRegistry starts (in particular, raising an exception if the WSDL is not available)
      if (isWSDLNotNullAndSet())
      {
         usingWSDL = true;

         internalSetWsdlURL();
      }
      else
      {
         usingWSDL = false;
      }
   }

   private boolean isWSDLNotNullAndSet()
   {
      return persistentWsdlDefinitionURL != null && !UNSET.equals(persistentWsdlDefinitionURL);
   }

   private String modifyIfNeeded(String oldValue, String newValue, int whichURL)
   {
      if ((oldValue != null && !oldValue.equals(newValue)) || (oldValue == null && newValue != null))
      {
         if (usesWSDL())
         {
            isModifiedWSDL = true;
            usingWSDL = false;
         }

         oldValue = newValue;
         clean.clear(whichURL);
      }

      return oldValue;
   }

   private ServiceFactory initServiceFactoryIfNeeded() throws RuntimeException
   {
      if (serviceFactory == null)
      {
         serviceFactory = new SOAPServiceFactory();
         if (usesWSDL())
         {
//            serviceFactory = new RemoteSOAPInvokerServiceFactory();
            internalSetWsdlURL();
         }
         else
         {
            if (!UNSET.equals(persistentServiceDescriptionURL) && !UNSET.equals(persistentMarkupURL))
            {
//               serviceFactory = new PerEndpointSOAPInvokerServiceFactory();
               serviceFactory.setServiceDescriptionURL(persistentServiceDescriptionURL);
               serviceFactory.setMarkupURL(persistentMarkupURL);
               serviceFactory.setPortletManagementURL(persistentPortletManagementURL);
               serviceFactory.setRegistrationURL(persistentRegistrationURL);
            }
            else
            {
               throw new IllegalStateException("Cannot initialize ServiceFactory: missing either service description or markup URLs!");
            }
         }

         startServiceFactoryIfNeeded();
      }

      return serviceFactory;
   }

   private void startServiceFactoryIfNeeded()
   {
      if (!serviceFactory.isAvailable())
      {
         if (!serviceFactory.isFailed())
         {
            try
            {
               serviceFactory.start();
               refreshServices(serviceFactory);
            }
            catch (Exception e)
            {
               throw new ConsumerException("Couldn't start ServiceFactory", e);
            }
         }
         else
         {
            throw new ConsumerException("ServiceFactory has an error condition that couldn't be recovered from.");
         }
      }
   }

   private void internalSetWsdlURL()
   {
      try
      {
         serviceFactory.setWsdlDefinitionURL(persistentWsdlDefinitionURL);

         // update the URLs based on WSDL information
         persistentMarkupURL = serviceFactory.getMarkupURL();
         persistentPortletManagementURL = serviceFactory.getPortletManagementURL();
         persistentRegistrationURL = serviceFactory.getRegistrationURL();
         persistentServiceDescriptionURL = serviceFactory.getServiceDescriptionURL();

         clean.set(0, 4); // if setting the WSDL URL worked, consider everything clean
         isModifiedWSDL = false;
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   public void start() throws Exception
   {
      initServiceFactoryIfNeeded();
   }

   public void stop() throws Exception
   {
      if (serviceFactory != null)
      {
         serviceFactory.stop();
      }
   }

   ServiceFactory getServiceFactory()
   {
      initServiceFactoryIfNeeded();
      startServiceFactoryIfNeeded();
      return serviceFactory;
   }

   WSRPV1ServiceDescriptionPortType getServiceDescriptionService() throws InvokerUnavailableException
   {
      return getService(WSRPV1ServiceDescriptionPortType.class);
   }

   WSRPV1MarkupPortType getMarkupService() throws InvokerUnavailableException
   {
      return getService(WSRPV1MarkupPortType.class);
   }

   WSRPV1PortletManagementPortType getPortletManagementService() throws InvokerUnavailableException
   {
      return getService(WSRPV1PortletManagementPortType.class);
   }

   WSRPV1RegistrationPortType getRegistrationService() throws InvokerUnavailableException
   {
      return getService(WSRPV1RegistrationPortType.class);
   }

   private <T> T getService(Class<T> clazz) throws InvokerUnavailableException
   {
      return getService(clazz, getServiceFactory());
   }

   private <T> T getService(Class<T> clazz, ServiceFactory serviceFactory) throws InvokerUnavailableException
   {
      try
      {
         T service = serviceFactory.getService(clazz);
         clean.set(getIndexFor(clazz));
         return service;
      }
      catch (Exception e)
      {
         throw new InvokerUnavailableException("Couldn't access " + clazz.getSimpleName() + " service. Cause: "
            + e.getLocalizedMessage(), e);
      }
   }

   private int getIndexFor(Class clazz)
   {
      if (clazz == WSRPV1ServiceDescriptionPortType.class)
      {
         return SD;
      }
      if (clazz == WSRPV1MarkupPortType.class)
      {
         return M;
      }
      if (clazz == WSRPV1PortletManagementPortType.class)
      {
         return PM;
      }
      return R;
   }

   public boolean isAvailable()
   {
      return serviceFactory.isAvailable();
   }

   public boolean isRefreshNeeded()
   {
      boolean result = !isAvailable() || areURLsDirty();
      if (result)
      {
         log.debug("Refresh needed");
      }
      return result;
   }

   private boolean areURLsDirty()
   {
      return !clean.get(SD) || !clean.get(M) || (persistentPortletManagementURL != null && !clean.get(PM))
         || (persistentRegistrationURL != null && !clean.get(R));
   }

   public void refresh() throws InvokerUnavailableException
   {
      if (isRefreshNeeded())
      {
         forceRefresh();
      }
   }

   void forceRefresh() throws InvokerUnavailableException
   {
      ServiceFactory serviceFactory = initServiceFactoryIfNeeded();
      refreshServices(serviceFactory);
   }

   private void refreshServices(ServiceFactory serviceFactory) throws InvokerUnavailableException
   {
      if (areURLsDirty())
      {
         getService(WSRPV1ServiceDescriptionPortType.class, serviceFactory);
         getService(WSRPV1MarkupPortType.class, serviceFactory);
         if (persistentPortletManagementURL != null)
         {
            getService(WSRPV1PortletManagementPortType.class, serviceFactory);
         }
         if (persistentRegistrationURL != null)
         {
            getService(WSRPV1RegistrationPortType.class, serviceFactory);
         }
      }
   }

   public String getRemoteHostAddress()
   {
      if (remoteHostAddress == null || areURLsDirty())
      {
         // extract host URL
         int hostBegin = persistentMarkupURL.indexOf("://") + 3;
         remoteHostAddress = persistentMarkupURL.substring(0, persistentMarkupURL.indexOf('/', hostBegin));
      }

      return remoteHostAddress;
   }
}
