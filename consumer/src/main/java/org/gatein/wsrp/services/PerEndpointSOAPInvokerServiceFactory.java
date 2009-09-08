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

import org.gatein.common.util.ParameterValidation;
import org.gatein.wsrp.consumer.EndpointConfigurationInfo;
import org.oasis.wsrp.v1.WSRPV1MarkupPortType;
import org.oasis.wsrp.v1.WSRPV1PortletManagementPortType;
import org.oasis.wsrp.v1.WSRPV1RegistrationPortType;
import org.oasis.wsrp.v1.WSRPV1ServiceDescriptionPortType;

import javax.xml.ws.BindingProvider;
import javax.xml.ws.Service;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 11484 $
 * @since 2.4
 */
public class PerEndpointSOAPInvokerServiceFactory extends AbstractSOAPServiceFactory
{

   /** . */
   protected String serviceDescriptionURL = EndpointConfigurationInfo.UNSET;

   /** . */
   protected String markupURL = EndpointConfigurationInfo.UNSET;

   /** . */
   protected String registrationURL;

   /** . */
   protected String portletManagementURL;

   public String getServiceDescriptionURL()
   {
      return serviceDescriptionURL;
   }

   public void setServiceDescriptionURL(String serviceDescriptionURL)
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(serviceDescriptionURL, "Mandatory Service Description interface", null);
      this.serviceDescriptionURL = serviceDescriptionURL;
      setFailed(false); // reset failed status to false since we can't assert it anymore
   }

   public String getMarkupURL()
   {
      return markupURL;
   }

   public void setMarkupURL(String markupURL)
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(markupURL, "Mandatory Markup interface", null);
      this.markupURL = markupURL;
      setFailed(false); // reset failed status to false since we can't assert it anymore
   }

   public String getRegistrationURL()
   {
      return registrationURL;
   }

   public void setRegistrationURL(String registrationURL)
   {
      this.registrationURL = registrationURL;
      setFailed(false); // reset failed status to false since we can't assert it anymore
   }

   public String getPortletManagementURL()
   {
      return portletManagementURL;
   }

   public void setPortletManagementURL(String portletManagementURL)
   {
      this.portletManagementURL = portletManagementURL;
      setFailed(false); // reset failed status to false since we can't assert it anymore
   }

   /** If retrieved object is of javax.xml.rpc.Service class, we're using the WS stack and we need to get the port. */
   protected <T> T getStubFromService(Class<T> serviceClass, Service service) throws Exception
   {
      log.debug("Unwrapping service " + service + " for class " + serviceClass);
      T stub = service.getPort(serviceClass);

      //
      String portAddress = null;
      boolean isMandatoryInterface = false;
      if (WSRPV1ServiceDescriptionPortType.class.isAssignableFrom(serviceClass))
      {
         portAddress = serviceDescriptionURL;
         isMandatoryInterface = true;
      }
      else if (WSRPV1MarkupPortType.class.isAssignableFrom(serviceClass))
      {
         portAddress = markupURL;
         isMandatoryInterface = true;
      }
      else if (WSRPV1RegistrationPortType.class.isAssignableFrom(serviceClass))
      {
         portAddress = registrationURL;
      }
      else if (WSRPV1PortletManagementPortType.class.isAssignableFrom(serviceClass))
      {
         portAddress = portletManagementURL;
      }

      //
      if (portAddress != null)
      {
         log.debug("Setting the end point to: " + portAddress);
         ((BindingProvider)stub).getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, portAddress);
      }
      else
      {
         if (isMandatoryInterface)
         {
            setFailed(true);
            throw new IllegalStateException("Mandatory interface URLs were not properly initialized: no proper service URL for "
               + serviceClass.getName());
         }
         else
         {
            throw new IllegalStateException("No URL was provided for optional interface "
               + serviceClass.getName());
         }
      }

      //
      return stub;
   }


   public boolean isAvailable()
   {
      return super.isAvailable() && !EndpointConfigurationInfo.UNSET.equals(serviceDescriptionURL)
         && !EndpointConfigurationInfo.UNSET.equals(markupURL);
   }
}
