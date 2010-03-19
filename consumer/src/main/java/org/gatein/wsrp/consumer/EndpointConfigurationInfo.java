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

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 13122 $
 * @since 2.6
 */
public class EndpointConfigurationInfo
{
   private final static Logger log = LoggerFactory.getLogger(EndpointConfigurationInfo.class);

   // transient variables
   /** Access to the WS */
   private transient ServiceFactory serviceFactory;
   private transient String remoteHostAddress;
   private transient boolean started;

   public EndpointConfigurationInfo()
   {
      serviceFactory = new SOAPServiceFactory();
   }

   protected EndpointConfigurationInfo(ServiceFactory serviceFactory)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(serviceFactory, "ServiceFactory");
      this.serviceFactory = serviceFactory;
   }

   public String getWsdlDefinitionURL()
   {
      return serviceFactory.getWsdlDefinitionURL();
   }

   public void setWsdlDefinitionURL(String wsdlDefinitionURL)
   {
      serviceFactory.setWsdlDefinitionURL(wsdlDefinitionURL);
   }

   public void start() throws Exception
   {
      if (!started)
      {
         serviceFactory.start();
         started = true;
      }
   }

   public void stop() throws Exception
   {
      if (started)
      {
         serviceFactory.stop();
         started = false;
      }
   }

   ServiceFactory getServiceFactory()
   {
      try
      {
         start();
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
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
         return serviceFactory.getService(clazz);
      }
      catch (Exception e)
      {
         throw new InvokerUnavailableException("Couldn't access " + clazz.getSimpleName() + " service. Cause: "
            + e.getLocalizedMessage(), e);
      }
   }

   public boolean isAvailable()
   {
      return serviceFactory.isAvailable();
   }

   public boolean isRefreshNeeded()
   {
      boolean result = !isAvailable();
      if (result && log.isDebugEnabled())
      {
         log.debug("Refresh needed");
      }
      return result;
   }

   public boolean refresh() throws InvokerUnavailableException
   {
      return isRefreshNeeded() && forceRefresh();
   }

   boolean forceRefresh() throws InvokerUnavailableException
   {
      getService(WSRPV1ServiceDescriptionPortType.class, serviceFactory);
      getService(WSRPV1MarkupPortType.class, serviceFactory);
      getService(WSRPV1PortletManagementPortType.class, serviceFactory);
      getService(WSRPV1RegistrationPortType.class, serviceFactory);

      return true;
   }

   public String getRemoteHostAddress()
   {
      if (remoteHostAddress == null)
      {
         // extract host URL
         String wsdl = getWsdlDefinitionURL();
         int hostBegin = wsdl.indexOf("://") + 3;
         remoteHostAddress = wsdl.substring(0, wsdl.indexOf('/', hostBegin));
      }

      return remoteHostAddress;
   }

   /**
    * Number of milliseconds before a WS operation is considered as having timed out.
    *
    * @param msBeforeTimeOut number of milliseconds to wait for a WS operation to return before timing out. Will be set
    *                        to {@link ServiceFactory#DEFAULT_TIMEOUT_MS} if negative.
    */
   public void setWSOperationTimeOut(int msBeforeTimeOut)
   {
      serviceFactory.setWSOperationTimeOut(msBeforeTimeOut);
   }

   public int getWSOperationTimeOut()
   {
      return serviceFactory.getWSOperationTimeOut();
   }
}
