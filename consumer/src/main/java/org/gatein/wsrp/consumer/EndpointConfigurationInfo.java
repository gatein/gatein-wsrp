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
import org.gatein.common.util.Version;
import org.gatein.pc.api.InvokerUnavailableException;
import org.gatein.wsrp.services.MarkupService;
import org.gatein.wsrp.services.PortletManagementService;
import org.gatein.wsrp.services.RegistrationService;
import org.gatein.wsrp.services.SOAPServiceFactory;
import org.gatein.wsrp.services.ServiceDescriptionService;
import org.gatein.wsrp.services.ServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages the information pertaining to the web service connection to the remote producer via its {@link ServiceFactory} and provides access to the services classes for WSRP
 * invocations.
 *
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

   public EndpointConfigurationInfo(ServiceFactory serviceFactory)
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

   ServiceDescriptionService getServiceDescriptionService() throws InvokerUnavailableException
   {
      try
      {
         return serviceFactory.getServiceDescriptionService();
      }
      catch (Exception e)
      {
         throw new InvokerUnavailableException("Couldn't access ServiceDescription service. Cause: "
            + e.getLocalizedMessage(), e);
      }
   }

   MarkupService getMarkupService() throws InvokerUnavailableException
   {
      try
      {
         return serviceFactory.getMarkupService();
      }
      catch (Exception e)
      {
         throw new InvokerUnavailableException("Couldn't access Markup service. Cause: "
            + e.getLocalizedMessage(), e);
      }
   }

   PortletManagementService getPortletManagementService() throws InvokerUnavailableException
   {
      try
      {
         return serviceFactory.getPortletManagementService();
      }
      catch (Exception e)
      {
         throw new InvokerUnavailableException("Couldn't access PortletManagement service. Cause: "
            + e.getLocalizedMessage(), e);
      }
   }

   RegistrationService getRegistrationService() throws InvokerUnavailableException
   {
      try
      {
         return serviceFactory.getRegistrationService();
      }
      catch (Exception e)
      {
         throw new InvokerUnavailableException("Couldn't access Registration service. Cause: "
            + e.getLocalizedMessage(), e);
      }
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
      try
      {
         return serviceFactory.refresh(true);
      }
      catch (Exception e)
      {
         throw new InvokerUnavailableException(e);
      }
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

   Version getWSRPVersion()
   {
      return serviceFactory.getWSRPVersion();
   }

   public boolean getWSSEnabled()
   {
      return serviceFactory.isWSSEnabled();
   }

   public void setWSSEnabled(boolean enable)
   {
      serviceFactory.enableWSS(enable);
   }

   public boolean isWSSAvailable()
   {
      return serviceFactory.isWSSAvailable();
   }
}
