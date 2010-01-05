/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2007, Red Hat Middleware, LLC, and individual                    *
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

package org.gatein.wsrp.test.support;

import org.gatein.common.NotYetImplemented;
import org.gatein.pc.api.Mode;
import org.gatein.pc.api.WindowState;
import org.gatein.wsrp.services.ServiceFactory;
import org.gatein.wsrp.test.BehaviorRegistry;
import org.gatein.wsrp.test.protocol.v1.MarkupBehavior;
import org.oasis.wsrp.v1.AccessDenied;
import org.oasis.wsrp.v1.GetMarkup;
import org.oasis.wsrp.v1.InconsistentParameters;
import org.oasis.wsrp.v1.InvalidCookie;
import org.oasis.wsrp.v1.InvalidHandle;
import org.oasis.wsrp.v1.InvalidRegistration;
import org.oasis.wsrp.v1.InvalidSession;
import org.oasis.wsrp.v1.InvalidUserCategory;
import org.oasis.wsrp.v1.MissingParameters;
import org.oasis.wsrp.v1.OperationFailed;
import org.oasis.wsrp.v1.UnsupportedLocale;
import org.oasis.wsrp.v1.UnsupportedMimeType;
import org.oasis.wsrp.v1.UnsupportedMode;
import org.oasis.wsrp.v1.UnsupportedWindowState;
import org.oasis.wsrp.v1.WSRPV1MarkupPortType;
import org.oasis.wsrp.v1.WSRPV1PortletManagementPortType;
import org.oasis.wsrp.v1.WSRPV1RegistrationPortType;
import org.oasis.wsrp.v1.WSRPV1ServiceDescriptionPortType;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 11461 $
 * @since 2.6
 */
public class BehaviorBackedServiceFactory implements ServiceFactory
{
   private BehaviorRegistry registry;
   private final static String MARKUP = "markup";
   private final static String SD_URL = "sd";
   private final static String M_URL = "m";
   private final static String PM_URL = "pm";
   private final static String R_URL = "r";
   private boolean initialized = false;
   private String wsdl = DEFAULT_WSDL_URL;
   public static final String DEFAULT_WSDL_URL = "http://example.com?wsdl";
   private int timeout;


   public BehaviorBackedServiceFactory()
   {
      registry = new BehaviorRegistry();
      registry.registerMarkupBehavior(new SimpleMarkupBehavior());
   }

   public <T> T getService(Class<T> serviceClass) throws Exception
   {
      if (!isAvailable() && !isFailed())
      {
         start();
      }

      if (WSRPV1ServiceDescriptionPortType.class.isAssignableFrom(serviceClass))
      {
         return (T)registry.getServiceDescriptionBehavior();
      }
      if (WSRPV1MarkupPortType.class.isAssignableFrom(serviceClass))
      {
         return (T)registry.getMarkupBehaviorFor(MARKUP);
      }
      if (WSRPV1PortletManagementPortType.class.isAssignableFrom(serviceClass))
      {
         return (T)registry.getPortletManagementBehavior();
      }
      if (WSRPV1RegistrationPortType.class.isAssignableFrom(serviceClass))
      {
         return (T)registry.getRegistrationBehavior();
      }
      return null;
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
      return false;
   }

   public void setFailed(boolean failed)
   {
      // do nothing
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

   public String getServiceDescriptionURL()
   {
      return SD_URL;
   }

   public String getMarkupURL()
   {
      return M_URL;
   }

   public String getRegistrationURL()
   {
      return R_URL;
   }

   public String getPortletManagementURL()
   {
      return PM_URL;
   }

   public void setServiceDescriptionURL(String serviceDescriptionURL)
   {
      // do nothing
   }

   public void setMarkupURL(String markupURL)
   {
      // do nothing
   }

   public void setRegistrationURL(String registrationURL)
   {
      // do nothing
   }

   public void setPortletManagementURL(String portletManagementURL)
   {
      // do nothing
   }

   public void create() throws Exception
   {
      throw new NotYetImplemented();
   }

   public void start() throws Exception
   {
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
      protected String getMarkupString(Mode mode, WindowState windowState, String navigationalState, GetMarkup getMarkup)
         throws UnsupportedWindowState, InvalidCookie, InvalidSession, AccessDenied, InconsistentParameters,
         InvalidHandle, UnsupportedLocale, UnsupportedMode, OperationFailed, MissingParameters, InvalidUserCategory,
         InvalidRegistration, UnsupportedMimeType
      {
         return MARKUP;
      }
   }
}
