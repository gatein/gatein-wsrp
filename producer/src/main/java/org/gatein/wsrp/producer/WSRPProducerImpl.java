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

package org.gatein.wsrp.producer;

import org.gatein.pc.api.NoSuchPortletException;
import org.gatein.pc.api.Portlet;
import org.gatein.pc.api.PortletInvoker;
import org.gatein.pc.api.PortletInvokerException;
import org.gatein.pc.api.info.RuntimeOptionInfo;
import org.gatein.registration.Registration;
import org.gatein.registration.RegistrationLocal;
import org.gatein.registration.RegistrationManager;
import org.gatein.wsrp.WSRPConstants;
import org.gatein.wsrp.WSRPExceptionFactory;
import org.gatein.wsrp.producer.config.ProducerConfiguration;
import org.gatein.wsrp.producer.config.ProducerConfigurationService;
import org.gatein.wsrp.producer.config.ProducerRegistrationRequirements;
import org.oasis.wsrp.v1.AccessDenied;
import org.oasis.wsrp.v1.BlockingInteractionResponse;
import org.oasis.wsrp.v1.ClonePortlet;
import org.oasis.wsrp.v1.CookieProtocol;
import org.oasis.wsrp.v1.DestroyPortlets;
import org.oasis.wsrp.v1.DestroyPortletsResponse;
import org.oasis.wsrp.v1.GetMarkup;
import org.oasis.wsrp.v1.GetPortletDescription;
import org.oasis.wsrp.v1.GetPortletProperties;
import org.oasis.wsrp.v1.GetPortletPropertyDescription;
import org.oasis.wsrp.v1.GetServiceDescription;
import org.oasis.wsrp.v1.InconsistentParameters;
import org.oasis.wsrp.v1.InitCookie;
import org.oasis.wsrp.v1.InvalidCookie;
import org.oasis.wsrp.v1.InvalidHandle;
import org.oasis.wsrp.v1.InvalidHandleFault;
import org.oasis.wsrp.v1.InvalidRegistration;
import org.oasis.wsrp.v1.InvalidSession;
import org.oasis.wsrp.v1.InvalidUserCategory;
import org.oasis.wsrp.v1.MarkupResponse;
import org.oasis.wsrp.v1.MissingParameters;
import org.oasis.wsrp.v1.ModifyRegistration;
import org.oasis.wsrp.v1.OperationFailed;
import org.oasis.wsrp.v1.PerformBlockingInteraction;
import org.oasis.wsrp.v1.PortletContext;
import org.oasis.wsrp.v1.PortletDescription;
import org.oasis.wsrp.v1.PortletDescriptionResponse;
import org.oasis.wsrp.v1.PortletPropertyDescriptionResponse;
import org.oasis.wsrp.v1.PortletStateChangeRequired;
import org.oasis.wsrp.v1.PropertyList;
import org.oasis.wsrp.v1.RegistrationContext;
import org.oasis.wsrp.v1.RegistrationData;
import org.oasis.wsrp.v1.RegistrationState;
import org.oasis.wsrp.v1.ReleaseSessions;
import org.oasis.wsrp.v1.ReturnAny;
import org.oasis.wsrp.v1.ServiceDescription;
import org.oasis.wsrp.v1.SetPortletProperties;
import org.oasis.wsrp.v1.UnsupportedLocale;
import org.oasis.wsrp.v1.UnsupportedMimeType;
import org.oasis.wsrp.v1.UnsupportedMode;
import org.oasis.wsrp.v1.UnsupportedWindowState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 12286 $
 * @since 2.4
 */
public class WSRPProducerImpl implements WSRPProducer
{
   /** logger used for logging ;) */
   private static final Logger log = LoggerFactory.getLogger(WSRPProducerImpl.class);

   /** The invoker used to retrieve portlet information and invoke methods. */
   private PortletInvoker invoker;

   /** Handles Markup interface calls. */
   private final MarkupHandler markupHandler;

   /** Handles ServiceDescription interface calls. */
   private final ServiceDescriptionHandler serviceDescriptionHandler;

   /** Handles Registration interface calls. */
   private final RegistrationHandler registrationHandler;

   /** Handles Portlet Management interface calls. */
   private final PortletManagementHandler portletManagementHandler;

   /** Registration Manager */
   private RegistrationManager registrationManager; //todo: make sure it's multi-thread safe

   /** configuration service */
   private ProducerConfigurationService configurationService; //todo: make sure it's multi-thread safe

   private boolean started = false;

   // On-demand class holder Singleton pattern (multi-thread safe)

   private static final class InstanceHolder
   {
      public static final WSRPProducerImpl producer = new WSRPProducerImpl();
   }

   static WSRPProducerImpl getInstance()
   {
      return InstanceHolder.producer;
   }

   static boolean isProducerStarted()
   {
      return InstanceHolder.producer.started;
   }

   private WSRPProducerImpl()
   {
      markupHandler = new MarkupHandler(this);
      serviceDescriptionHandler = new ServiceDescriptionHandler(this);
      registrationHandler = new RegistrationHandler(this);
      portletManagementHandler = new PortletManagementHandler(this);
   }

   ProducerRegistrationRequirements getProducerRegistrationRequirements()
   {
      return getProducerConfiguration().getRegistrationRequirements();
   }

   // ServiceDescription implementation ********************************************************************************

   public ServiceDescription getServiceDescription(GetServiceDescription gs)
      throws InvalidRegistration, OperationFailed
   {
      log.debug("getServiceDescription invoked");
      ServiceDescription sd = serviceDescriptionHandler.getServiceDescription(gs);
      log.debug("end getServiceDescription");
      return sd;
   }

   // MarkupService implementation *************************************************************************************


   public MarkupResponse getMarkup(GetMarkup getMarkup) throws UnsupportedWindowState, InvalidCookie, InvalidSession, AccessDenied, InconsistentParameters, InvalidHandle, UnsupportedLocale, UnsupportedMode, OperationFailed, MissingParameters, InvalidUserCategory, InvalidRegistration, UnsupportedMimeType
   {
      log.debug("getMarkup invoked");
      MarkupResponse response = markupHandler.getMarkup(getMarkup);
      log.debug("end getMarkup");
      return response;
   }

   public BlockingInteractionResponse performBlockingInteraction(PerformBlockingInteraction performBlockingInteraction) throws InvalidSession, UnsupportedMode, UnsupportedMimeType, OperationFailed, UnsupportedWindowState, UnsupportedLocale, AccessDenied, PortletStateChangeRequired, InvalidRegistration, MissingParameters, InvalidUserCategory, InconsistentParameters, InvalidHandle, InvalidCookie
   {
      log.debug("performBlockingInteraction invoked");
      BlockingInteractionResponse interactionResponse = markupHandler.performBlockingInteraction(performBlockingInteraction);
      log.debug("end performBlockingInteraction");
      return interactionResponse;
   }

   public ReturnAny releaseSessions(ReleaseSessions releaseSessions) throws InvalidRegistration, OperationFailed, MissingParameters, AccessDenied
   {
      log.debug("releaseSessions invoked");
      ReturnAny returnAny = markupHandler.releaseSessions(releaseSessions);
      log.debug("end releaseSessions");
      return returnAny;
   }

   public ReturnAny initCookie(InitCookie initCookie) throws AccessDenied, OperationFailed, InvalidRegistration
   {
      log.debug("initCookie invoked");
      ReturnAny returnAny = markupHandler.initCookie(initCookie);
      log.debug("end initCookie");
      return returnAny;
   }

   // Registration implementation **************************************************************************************

   public RegistrationContext register(RegistrationData register) throws MissingParameters, OperationFailed
   {
      log.debug("register invoked");
      RegistrationContext registrationContext = registrationHandler.register(register);
      log.debug("end register");
      return registrationContext;
   }

   public ReturnAny deregister(RegistrationContext deregister) throws OperationFailed, InvalidRegistration
   {
      log.debug("deregister invoked");
      ReturnAny returnAny = registrationHandler.deregister(deregister);
      log.debug("end deregister");
      return returnAny;
   }

   public RegistrationState modifyRegistration(ModifyRegistration modifyRegistration) throws MissingParameters,
      OperationFailed, InvalidRegistration
   {
      log.debug("modifyRegistration invoked");
      RegistrationState registrationState = registrationHandler.modifyRegistration(modifyRegistration);
      log.debug("end modifyRegistration");
      return registrationState;
   }

// PortletManagement implementation *********************************************************************************

   public PortletDescriptionResponse getPortletDescription(GetPortletDescription getPortletDescription)
      throws AccessDenied, InvalidHandle, InvalidUserCategory, InconsistentParameters, MissingParameters,
      InvalidRegistration, OperationFailed
   {
      log.debug("getPortletDescription invoked");
      PortletDescriptionResponse description = portletManagementHandler.getPortletDescription(getPortletDescription);
      log.debug("end getPortletDescription");
      return description;
   }

   public PortletContext clonePortlet(ClonePortlet clonePortlet) throws InvalidUserCategory, AccessDenied,
      OperationFailed, InvalidHandle, InvalidRegistration, InconsistentParameters, MissingParameters
   {
      log.debug("clonePortlet invoked");
      PortletContext portletContext = portletManagementHandler.clonePortlet(clonePortlet);
      log.debug("end clonePortlet");
      return portletContext;
   }

   public DestroyPortletsResponse destroyPortlets(DestroyPortlets destroyPortlets) throws InconsistentParameters,
      MissingParameters, InvalidRegistration, OperationFailed
   {
      log.debug("destroyPortlets invoked");
      DestroyPortletsResponse destroyPortletsResponse = portletManagementHandler.destroyPortlets(destroyPortlets);
      log.debug("end destroyPortlets");
      return destroyPortletsResponse;
   }

   public PortletContext setPortletProperties(SetPortletProperties setPortletProperties) throws OperationFailed,
      InvalidHandle, MissingParameters, InconsistentParameters, InvalidUserCategory, AccessDenied, InvalidRegistration
   {
      log.debug("setPortletProperties invoked");
      PortletContext portletContext = portletManagementHandler.setPortletProperties(setPortletProperties);
      log.debug("end setPortletProperties");
      return portletContext;
   }

   public PropertyList getPortletProperties(GetPortletProperties getPortletProperties) throws InvalidHandle,
      MissingParameters, InvalidRegistration, AccessDenied, OperationFailed, InconsistentParameters, InvalidUserCategory
   {
      log.debug("getPortletProperties invoked");
      PropertyList list = portletManagementHandler.getPortletProperties(getPortletProperties);
      log.debug("end getPortletProperties");
      return list;
   }

   public PortletPropertyDescriptionResponse getPortletPropertyDescription(GetPortletPropertyDescription getPortletPropertyDescription)
      throws MissingParameters, InconsistentParameters, InvalidUserCategory, InvalidRegistration, AccessDenied,
      InvalidHandle, OperationFailed
   {
      log.debug("getPortletPropertyDescription invoked");
      PortletPropertyDescriptionResponse descriptionResponse = portletManagementHandler.getPortletPropertyDescription(getPortletPropertyDescription);
      log.debug("end getPortletPropertyDescription");
      return descriptionResponse;
   }

   private ProducerConfiguration getProducerConfiguration()
   {
      return configurationService.getConfiguration();
   }

   public RegistrationManager getRegistrationManager()
   {
      return registrationManager;
   }

   public void setRegistrationManager(RegistrationManager registrationManager)
   {
      this.registrationManager = registrationManager;
   }

   public void setConfigurationService(ProducerConfigurationService configurationService)
   {
      this.configurationService = configurationService;
   }

   public ProducerConfigurationService getConfigurationService()
   {
      return configurationService;
   }

   public synchronized void start()
   {
      if (!started)
      {
         ProducerConfiguration configuration = configurationService.getConfiguration();

         // register to listen to changes in configuration and get initial state
         configuration.addChangeListener(this);
         usingStrictModeChangedTo(configuration.isUsingStrictMode());

         ProducerRegistrationRequirements registrationRequirements = getProducerRegistrationRequirements();
         registrationRequirements.addRegistrationPolicyChangeListener(registrationManager);
         registrationRequirements.addRegistrationPropertyChangeListener(registrationManager);

         if (registrationRequirements.isRegistrationRequired())
         {
            registrationManager.setPolicy(registrationRequirements.getPolicy());
         }

         started = true;
      }
   }

   public synchronized void stop()
   {
      if (started)
      {
         ProducerRegistrationRequirements registrationRequirements = getProducerRegistrationRequirements();
         registrationRequirements.removeRegistrationPropertyChangeListener(registrationManager);
         registrationRequirements.removeRegistrationPolicyChangeListener(registrationManager);

         getProducerConfiguration().removeChangeListener(this);

         started = false;
      }
   }

   int getExpirationTime()
   {
      return getProducerConfiguration().getSessionExpirationTime();
   }

   CookieProtocol getRequiresInitCookie()
   {
      return getProducerConfiguration().getRequiresInitCookie();
   }

   public PortletInvoker getPortletInvoker()
   {
      return invoker;
   }

   public void setPortletInvoker(PortletInvoker invoker)
   {
      this.invoker = invoker;
   }

   Portlet getPortletWith(org.gatein.pc.api.PortletContext portletContext, Registration registration) throws InvalidHandle, PortletInvokerException
   {
      Portlet portlet;
      try
      {
         RegistrationLocal.setRegistration(registration);
         portlet = invoker.getPortlet(portletContext);
      }
      catch (NoSuchPortletException e)
      {
         throw WSRPExceptionFactory.<InvalidHandle, InvalidHandleFault>throwWSException(WSRPExceptionFactory.INVALID_HANDLE,
            "Couldn't find portlet with handle '" + portletContext.getId() + "'", null);
      }
      finally
      {
         RegistrationLocal.setRegistration(null);
      }

      if (!isRemotable(portlet))
      {
         throw WSRPExceptionFactory.<InvalidHandle, InvalidHandleFault>throwWSException(WSRPExceptionFactory.INVALID_HANDLE,
            "Portlet '" + portletContext.getId() + "' is not remotely available.", null);
      }

      return portlet;
   }

   Set<Portlet> getRemotablePortlets() throws PortletInvokerException
   {
      log.debug("Retrieving remotable portlets");
      Set<Portlet> allPortlets = invoker.getPortlets();
      int portletNumber = allPortlets.size();
      Set<Portlet> remotablePortlets = Collections.emptySet();
      if (portletNumber > 0)
      {
         remotablePortlets = new HashSet<Portlet>(portletNumber);
         for (Portlet portlet : allPortlets)
         {
            log.debug("Found portlet: " + portlet.getContext());
            if (isRemotable(portlet))
            {
               remotablePortlets.add(portlet);
               log.debug("Adding remotable portlet to set: " + portlet.getContext());
            }
         }
      }
      return remotablePortlets;
   }

   public PortletDescription getPortletDescription(PortletContext portletContext, List<String> locales, Registration registration) throws InvalidHandle, OperationFailed
   {
      return serviceDescriptionHandler.getPortletDescription(portletContext, locales, registration);
   }

   public PortletDescription getPortletDescription(Portlet portlet, List<String> locales)
   {
      return serviceDescriptionHandler.getPortletDescription(portlet, locales);
   }

   Registration getRegistrationOrFailIfInvalid(RegistrationContext registrationContext) throws InvalidRegistration, OperationFailed
   {
      Registration registration = registrationHandler.getRegistrationFrom(registrationContext);
      registrationHandler.isRegistrationValid(registration, true);

      return registration;
   }

   private Boolean remotableByDefault;

   public Boolean isRemotableByDefault()
   {
      return remotableByDefault;
   }

   public void setRemotableByDefault(Boolean remotableByDefault)
   {
      this.remotableByDefault = remotableByDefault;
   }

   private boolean isRemotable(Portlet portlet)
   {
      Map<String, RuntimeOptionInfo> runtimeOptions = portlet.getInfo().getRuntimeOptionsInfo();
      RuntimeOptionInfo runtimeOptionInfo = runtimeOptions.get(RuntimeOptionInfo.ORG_JBOSS_PORTLETCONTAINER_REMOTABLE);

      return runtimeOptionInfo != null && "true".equals(runtimeOptionInfo.getValues().get(0));

      /*WSRPInfo wsrpInfo = portletInfo.getAttachment(WSRPInfo.class);
      if (wsrpInfo != null)
      {
         Boolean remotable = wsrpInfo.isRemotable();
         log.debug("Portlet " + portlet.getContext() + " remotable: " + remotable);
         if (remotable != null)
         {
            return remotable.booleanValue();
         }
      }
      if (isRemotableByDefault() != null)
      {
         return isRemotableByDefault().booleanValue();
      }
      return false;*/
   }

   public List<String> getSupportedLocales()
   {
      return WSRPConstants.getDefaultLocales(); // todo: avoid hardcoding this at some point...
   }

   public void usingStrictModeChangedTo(boolean strictMode)
   {
      WSRPValidator.setStrict(strictMode);
   }

   // access to handlers for tests

   MarkupInterface getMarkupInterface()
   {
      return markupHandler;
   }

   ServiceDescriptionInterface getServiceDescriptionInterface()
   {
      return serviceDescriptionHandler;
   }

   RegistrationInterface getRegistrationInterface()
   {
      return registrationHandler;
   }

   PortletManagementInterface getPortletManagementInterface()
   {
      return portletManagementHandler;
   }
}
