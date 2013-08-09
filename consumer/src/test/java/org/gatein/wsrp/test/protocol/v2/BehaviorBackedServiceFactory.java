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

package org.gatein.wsrp.test.protocol.v2;

import org.gatein.common.NotYetImplemented;
import org.gatein.common.util.Version;
import org.gatein.pc.api.Mode;
import org.gatein.pc.api.WindowState;
import org.gatein.wsrp.services.MarkupService;
import org.gatein.wsrp.services.PortletManagementService;
import org.gatein.wsrp.services.RegistrationService;
import org.gatein.wsrp.services.ServiceDescriptionService;
import org.gatein.wsrp.services.ServiceFactory;
import org.gatein.wsrp.services.v2.V2MarkupService;
import org.gatein.wsrp.services.v2.V2PortletManagementService;
import org.gatein.wsrp.services.v2.V2RegistrationService;
import org.gatein.wsrp.services.v2.V2ServiceDescriptionService;
import org.gatein.wsrp.test.support.RequestedMarkupBehavior;
import org.oasis.wsrp.v2.AccessDenied;
import org.oasis.wsrp.v2.GetMarkup;
import org.oasis.wsrp.v2.InconsistentParameters;
import org.oasis.wsrp.v2.InvalidCookie;
import org.oasis.wsrp.v2.InvalidHandle;
import org.oasis.wsrp.v2.InvalidRegistration;
import org.oasis.wsrp.v2.InvalidSession;
import org.oasis.wsrp.v2.InvalidUserCategory;
import org.oasis.wsrp.v2.MissingParameters;
import org.oasis.wsrp.v2.OperationFailed;
import org.oasis.wsrp.v2.UnsupportedLocale;
import org.oasis.wsrp.v2.UnsupportedMimeType;
import org.oasis.wsrp.v2.UnsupportedMode;
import org.oasis.wsrp.v2.UnsupportedWindowState;
import org.oasis.wsrp.v2.WSRPV2MarkupPortType;
import org.oasis.wsrp.v2.WSRPV2PortletManagementPortType;
import org.oasis.wsrp.v2.WSRPV2RegistrationPortType;
import org.oasis.wsrp.v2.WSRPV2ServiceDescriptionPortType;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 11461 $
 * @since 2.6
 */
public class BehaviorBackedServiceFactory implements ServiceFactory
{
   private BehaviorRegistry registry;
   private final static String MARKUP = "markup";
   private boolean initialized = false;
   private String wsdl = DEFAULT_WSDL_URL;
   public static final String DEFAULT_WSDL_URL = "http://example.com/producer?wsdl";
   private int timeout = DEFAULT_TIMEOUT_MS;
   private boolean failed;


   public BehaviorBackedServiceFactory()
   {
      this(new BehaviorRegistry());
   }

   public BehaviorBackedServiceFactory(BehaviorRegistry registry)
   {
      this.registry = registry;
      registry.registerMarkupBehavior(new SimpleMarkupBehavior());
   }

   public <T> T getService(Class<T> serviceClass) throws Exception
   {
      if (!isAvailable() && !isFailed())
      {
         start();
      }

      if (WSRPV2ServiceDescriptionPortType.class.isAssignableFrom(serviceClass))
      {
         return (T)registry.getServiceDescriptionBehavior();
      }
      if (WSRPV2MarkupPortType.class.isAssignableFrom(serviceClass))
      {
         String requestedMarkupBehavior = RequestedMarkupBehavior.getRequestedMarkupBehavior();
         if (requestedMarkupBehavior == null)
         {
            requestedMarkupBehavior = MARKUP;
         }
         return (T)registry.getMarkupBehaviorFor(requestedMarkupBehavior);
      }
      if (WSRPV2PortletManagementPortType.class.isAssignableFrom(serviceClass))
      {
         return (T)registry.getPortletManagementBehavior();
      }
      if (WSRPV2RegistrationPortType.class.isAssignableFrom(serviceClass))
      {
         return (T)registry.getRegistrationBehavior();
      }
      return null;
   }

   public ServiceDescriptionService getServiceDescriptionService() throws Exception
   {
      return new V2ServiceDescriptionService(getService(WSRPV2ServiceDescriptionPortType.class));
   }

   public MarkupService getMarkupService() throws Exception
   {
      return new V2MarkupService(getService(WSRPV2MarkupPortType.class));
   }

   public PortletManagementService getPortletManagementService() throws Exception
   {
      return new V2PortletManagementService(getService(WSRPV2PortletManagementPortType.class));
   }

   public RegistrationService getRegistrationService() throws Exception
   {
      return new V2RegistrationService(getService(WSRPV2RegistrationPortType.class));
   }

   public Version getWSRPVersion()
   {
      return ServiceFactory.WSRP2;
   }

   public boolean refresh(boolean force) throws Exception
   {
      if (force || (!isAvailable() && !isFailed()))
      {
         start();

         return true;
      }

      return false;
   }

   public BehaviorRegistry getRegistry()
   {
      return registry;
   }

   public void setRegistry(BehaviorRegistry registry)
   {
      this.registry = registry;
   }

   public boolean isAvailable()
   {
      return initialized;
   }

   public boolean isFailed()
   {
      return failed;
   }

   public void setFailed(boolean failed)
   {
      this.failed = failed;
   }

   public void setWSOperationTimeOut(int msBeforeTimeOut)
   {
      if (msBeforeTimeOut < 0)
      {
         msBeforeTimeOut = DEFAULT_TIMEOUT_MS;
      }

      timeout = msBeforeTimeOut;
   }

   public int getWSOperationTimeOut()
   {
      return timeout;
   }

   public void create() throws Exception
   {
      throw new NotYetImplemented();
   }

   public void start() throws Exception
   {
      if (failed)
      {
         throw new RuntimeException("ServiceFactory has failed!");
      }
      initialized = true;
   }

   public void stop()
   {
      throw new NotYetImplemented();
   }

   public void setWsdlDefinitionURL(String wsdlDefinitionURL)
   {
      wsdl = wsdlDefinitionURL;
   }

   public String getWsdlDefinitionURL()
   {
      return wsdl;
   }

   public void destroy()
   {
      throw new NotYetImplemented();
   }

   private class SimpleMarkupBehavior extends MarkupBehavior
   {
      public SimpleMarkupBehavior()
      {
         super(BehaviorBackedServiceFactory.this.registry);
         registerHandle(MARKUP);
      }

      @Override
      protected String getMarkupString(Mode mode, WindowState windowState, String navigationalState, GetMarkup getMarkup) throws UnsupportedWindowState, InvalidCookie, InvalidSession, AccessDenied, InconsistentParameters, InvalidHandle, UnsupportedLocale, UnsupportedMode, OperationFailed, MissingParameters, InvalidUserCategory, InvalidRegistration, UnsupportedMimeType
      {
         return MARKUP;
      }
   }

   @Override
   public ServiceFactory clone()
   {
      final BehaviorBackedServiceFactory factory = new BehaviorBackedServiceFactory();
      factory.registry = this.registry;
      factory.failed = this.failed;
      factory.initialized = this.initialized;
      factory.timeout = this.timeout;
      factory.wsdl = this.wsdl;
      return factory;
   }
}