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

package org.gatein.wsrp.producer;

import org.gatein.exports.ExportManager;
import org.gatein.exports.impl.ExportManagerImpl;
import org.gatein.pc.api.NoSuchPortletException;
import org.gatein.pc.api.Portlet;
import org.gatein.pc.api.PortletInvoker;
import org.gatein.pc.api.PortletInvokerException;
import org.gatein.pc.api.info.RuntimeOptionInfo;
import org.gatein.pc.portlet.container.managed.ManagedObjectRegistryEvent;
import org.gatein.registration.Registration;
import org.gatein.registration.RegistrationLocal;
import org.gatein.registration.RegistrationManager;
import org.gatein.wsrp.WSRPConstants;
import org.gatein.wsrp.WSRPUtils;
import org.gatein.wsrp.api.context.ProducerContext;
import org.gatein.wsrp.producer.config.ProducerConfiguration;
import org.gatein.wsrp.producer.config.ProducerConfigurationService;
import org.gatein.wsrp.producer.config.ProducerRegistrationRequirements;
import org.gatein.wsrp.producer.handlers.MarkupHandler;
import org.gatein.wsrp.producer.handlers.PortletManagementHandler;
import org.gatein.wsrp.producer.handlers.RegistrationHandler;
import org.gatein.wsrp.producer.handlers.ServiceDescriptionHandler;
import org.gatein.wsrp.producer.handlers.processors.ProducerHelper;
import org.gatein.wsrp.producer.v2.WSRP2Producer;
import org.gatein.wsrp.spec.v2.WSRP2ExceptionFactory;
import org.oasis.wsrp.v2.AccessDenied;
import org.oasis.wsrp.v2.BlockingInteractionResponse;
import org.oasis.wsrp.v2.ClonePortlet;
import org.oasis.wsrp.v2.CookieProtocol;
import org.oasis.wsrp.v2.CopyPortlets;
import org.oasis.wsrp.v2.CopyPortletsResponse;
import org.oasis.wsrp.v2.DestroyPortlets;
import org.oasis.wsrp.v2.DestroyPortletsResponse;
import org.oasis.wsrp.v2.ExportByValueNotSupported;
import org.oasis.wsrp.v2.ExportNoLongerValid;
import org.oasis.wsrp.v2.ExportPortlets;
import org.oasis.wsrp.v2.ExportPortletsResponse;
import org.oasis.wsrp.v2.Extension;
import org.oasis.wsrp.v2.GetMarkup;
import org.oasis.wsrp.v2.GetPortletDescription;
import org.oasis.wsrp.v2.GetPortletProperties;
import org.oasis.wsrp.v2.GetPortletPropertyDescription;
import org.oasis.wsrp.v2.GetPortletsLifetime;
import org.oasis.wsrp.v2.GetPortletsLifetimeResponse;
import org.oasis.wsrp.v2.GetRegistrationLifetime;
import org.oasis.wsrp.v2.GetResource;
import org.oasis.wsrp.v2.GetServiceDescription;
import org.oasis.wsrp.v2.HandleEvents;
import org.oasis.wsrp.v2.HandleEventsResponse;
import org.oasis.wsrp.v2.ImportPortlets;
import org.oasis.wsrp.v2.ImportPortletsResponse;
import org.oasis.wsrp.v2.InconsistentParameters;
import org.oasis.wsrp.v2.InitCookie;
import org.oasis.wsrp.v2.InvalidCookie;
import org.oasis.wsrp.v2.InvalidHandle;
import org.oasis.wsrp.v2.InvalidRegistration;
import org.oasis.wsrp.v2.InvalidSession;
import org.oasis.wsrp.v2.InvalidUserCategory;
import org.oasis.wsrp.v2.Lifetime;
import org.oasis.wsrp.v2.MarkupResponse;
import org.oasis.wsrp.v2.MissingParameters;
import org.oasis.wsrp.v2.ModifyRegistration;
import org.oasis.wsrp.v2.ModifyRegistrationRequired;
import org.oasis.wsrp.v2.OperationFailed;
import org.oasis.wsrp.v2.OperationNotSupported;
import org.oasis.wsrp.v2.PerformBlockingInteraction;
import org.oasis.wsrp.v2.PortletContext;
import org.oasis.wsrp.v2.PortletDescription;
import org.oasis.wsrp.v2.PortletDescriptionResponse;
import org.oasis.wsrp.v2.PortletPropertyDescriptionResponse;
import org.oasis.wsrp.v2.PortletStateChangeRequired;
import org.oasis.wsrp.v2.PropertyList;
import org.oasis.wsrp.v2.RegistrationContext;
import org.oasis.wsrp.v2.RegistrationData;
import org.oasis.wsrp.v2.RegistrationState;
import org.oasis.wsrp.v2.ReleaseExport;
import org.oasis.wsrp.v2.ReleaseSessions;
import org.oasis.wsrp.v2.ResourceResponse;
import org.oasis.wsrp.v2.ResourceSuspended;
import org.oasis.wsrp.v2.ServiceDescription;
import org.oasis.wsrp.v2.SetExportLifetime;
import org.oasis.wsrp.v2.SetPortletProperties;
import org.oasis.wsrp.v2.SetPortletsLifetime;
import org.oasis.wsrp.v2.SetPortletsLifetimeResponse;
import org.oasis.wsrp.v2.SetRegistrationLifetime;
import org.oasis.wsrp.v2.UnsupportedLocale;
import org.oasis.wsrp.v2.UnsupportedMimeType;
import org.oasis.wsrp.v2.UnsupportedMode;
import org.oasis.wsrp.v2.UnsupportedWindowState;
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
public class WSRPProducerImpl implements WSRP2Producer, ProducerHelper
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

   /** export manager */
   private ExportManager exportManager;

   /** producer context */
   private ProducerContext producerContext;

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

   public ProducerRegistrationRequirements getProducerRegistrationRequirements()
   {
      return getProducerConfiguration().getRegistrationRequirements();
   }

   // ServiceDescription implementation ********************************************************************************

   public ServiceDescription getServiceDescription(GetServiceDescription gs)
      throws InvalidRegistration, OperationFailed, ResourceSuspended, ModifyRegistrationRequired
   {
      return serviceDescriptionHandler.getServiceDescription(gs);
   }

   // MarkupService implementation *************************************************************************************


   public MarkupResponse getMarkup(GetMarkup getMarkup) throws UnsupportedWindowState, InvalidCookie, InvalidSession, AccessDenied, InconsistentParameters, InvalidHandle, UnsupportedLocale, UnsupportedMode, OperationFailed, MissingParameters, InvalidUserCategory, InvalidRegistration, UnsupportedMimeType, ResourceSuspended, ModifyRegistrationRequired
   {
      return markupHandler.getMarkup(getMarkup);
   }

   public BlockingInteractionResponse performBlockingInteraction(PerformBlockingInteraction performBlockingInteraction) throws InvalidSession, UnsupportedMode, UnsupportedMimeType, OperationFailed, UnsupportedWindowState, UnsupportedLocale, AccessDenied, PortletStateChangeRequired, InvalidRegistration, MissingParameters, InvalidUserCategory, InconsistentParameters, InvalidHandle, InvalidCookie, ResourceSuspended, ModifyRegistrationRequired
   {
      return markupHandler.performBlockingInteraction(performBlockingInteraction);
   }

   public List<Extension> releaseSessions(ReleaseSessions releaseSessions) throws InvalidRegistration, OperationFailed, MissingParameters, AccessDenied, ResourceSuspended, OperationNotSupported, ModifyRegistrationRequired
   {
      return markupHandler.releaseSessions(releaseSessions);
   }

   public List<Extension> initCookie(InitCookie initCookie) throws AccessDenied, OperationFailed, InvalidRegistration, ResourceSuspended, OperationNotSupported, ModifyRegistrationRequired
   {
      return markupHandler.initCookie(initCookie);
   }

   public HandleEventsResponse handleEvents(HandleEvents handleEvents) throws AccessDenied, InconsistentParameters, InvalidCookie, InvalidHandle, InvalidRegistration, InvalidSession, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, PortletStateChangeRequired, ResourceSuspended, UnsupportedLocale, UnsupportedMimeType, UnsupportedMode, UnsupportedWindowState
   {
      return markupHandler.handleEvents(handleEvents);
   }

   public ResourceResponse getResource(GetResource getResource) throws AccessDenied, InconsistentParameters, InvalidCookie, InvalidHandle, InvalidRegistration, InvalidSession, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, ResourceSuspended, UnsupportedLocale, UnsupportedMimeType, UnsupportedMode, UnsupportedWindowState, OperationNotSupported
   {
      return markupHandler.getResource(getResource);
   }

   // Registration implementation **************************************************************************************

   public RegistrationContext register(RegistrationData register) throws MissingParameters, OperationFailed, OperationNotSupported
   {
      return registrationHandler.register(register);
   }

   public List<Extension> deregister(RegistrationContext deregister) throws OperationFailed, InvalidRegistration, ResourceSuspended, OperationNotSupported
   {
      return registrationHandler.deregister(deregister);
   }

   public RegistrationState modifyRegistration(ModifyRegistration modifyRegistration) throws MissingParameters,
      OperationFailed, InvalidRegistration, ResourceSuspended, OperationNotSupported
   {
      return registrationHandler.modifyRegistration(modifyRegistration);
   }

   public Lifetime getRegistrationLifetime(GetRegistrationLifetime getRegistrationLifetime) throws AccessDenied, InvalidHandle, InvalidRegistration, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      return registrationHandler.getRegistrationLifetime(getRegistrationLifetime);
   }

   public Lifetime setRegistrationLifetime(SetRegistrationLifetime setRegistrationLifetime) throws AccessDenied, InvalidHandle, InvalidRegistration, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      return registrationHandler.setRegistrationLifetime(setRegistrationLifetime);
   }

   // PortletManagement implementation *********************************************************************************

   public PortletDescriptionResponse getPortletDescription(GetPortletDescription getPortletDescription)
      throws AccessDenied, InvalidHandle, InvalidUserCategory, InconsistentParameters, MissingParameters,
      InvalidRegistration, OperationFailed, ResourceSuspended, OperationNotSupported, ModifyRegistrationRequired
   {
      return portletManagementHandler.getPortletDescription(getPortletDescription);
   }

   public PortletContext clonePortlet(ClonePortlet clonePortlet) throws InvalidUserCategory, AccessDenied,
      OperationFailed, InvalidHandle, InvalidRegistration, InconsistentParameters, MissingParameters,
      ResourceSuspended, OperationNotSupported, ModifyRegistrationRequired
   {
      return portletManagementHandler.clonePortlet(clonePortlet);
   }

   public DestroyPortletsResponse destroyPortlets(DestroyPortlets destroyPortlets) throws InconsistentParameters,
      MissingParameters, InvalidRegistration, OperationFailed, ResourceSuspended, OperationNotSupported,
      ModifyRegistrationRequired
   {
      return portletManagementHandler.destroyPortlets(destroyPortlets);
   }

   public PortletContext setPortletProperties(SetPortletProperties setPortletProperties) throws OperationFailed,
      InvalidHandle, MissingParameters, InconsistentParameters, InvalidUserCategory, AccessDenied, InvalidRegistration,
      ResourceSuspended, OperationNotSupported, ModifyRegistrationRequired
   {
      return portletManagementHandler.setPortletProperties(setPortletProperties);
   }

   public PropertyList getPortletProperties(GetPortletProperties getPortletProperties) throws InvalidHandle,
      MissingParameters, InvalidRegistration, AccessDenied, OperationFailed, InconsistentParameters, InvalidUserCategory,
      ResourceSuspended, OperationNotSupported, ModifyRegistrationRequired
   {
      return portletManagementHandler.getPortletProperties(getPortletProperties);
   }

   public PortletPropertyDescriptionResponse getPortletPropertyDescription(GetPortletPropertyDescription getPortletPropertyDescription)
      throws MissingParameters, InconsistentParameters, InvalidUserCategory, InvalidRegistration, AccessDenied,
      InvalidHandle, OperationFailed, ResourceSuspended, OperationNotSupported, ModifyRegistrationRequired
   {
      return portletManagementHandler.getPortletPropertyDescription(getPortletPropertyDescription);
   }

   public GetPortletsLifetimeResponse getPortletsLifetime(GetPortletsLifetime getPortletsLifetime) throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      return portletManagementHandler.getPortletsLifetime(getPortletsLifetime);
   }

   public SetPortletsLifetimeResponse setPortletsLifetime(SetPortletsLifetime setPortletsLifetime) throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      return portletManagementHandler.setPortletsLifetime(setPortletsLifetime);
   }

   public CopyPortletsResponse copyPortlets(CopyPortlets copyPortlets) throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      return portletManagementHandler.copyPortlets(copyPortlets);
   }

   public ExportPortletsResponse exportPortlets(ExportPortlets exportPortlets) throws AccessDenied, ExportByValueNotSupported, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      return portletManagementHandler.exportPortlets(exportPortlets);
   }

   public ImportPortletsResponse importPortlets(ImportPortlets importPortlets) throws AccessDenied, ExportNoLongerValid, InconsistentParameters, InvalidRegistration, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      return portletManagementHandler.importPortlets(importPortlets);
   }

   public List<Extension> releaseExport(ReleaseExport releaseExport)
   {
      return portletManagementHandler.releaseExport(releaseExport);
   }

   public Lifetime setExportLifetime(SetExportLifetime setExportLifetime) throws AccessDenied, InvalidHandle, InvalidRegistration, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      return portletManagementHandler.setExportLifetime(setExportLifetime);
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

   public void setExportManager(ExportManager exportManger)
   {
      this.exportManager = exportManger;
   }

   public ProducerContext getProducerContext()
   {
      return producerContext;
   }

   public void setProducerContext(ProducerContext producerContext)
   {
      this.producerContext = producerContext;
   }

   public ExportManager getExportManager()
   {
      if (exportManager == null)
      {
         exportManager = new ExportManagerImpl();
      }
      return exportManager;
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

         registrationManager.setPolicy(registrationRequirements.getPolicy());

         // GTNWSRP-72
//         registrationManager.getPolicy().addPortletContextChangeListener(registrationManager);

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

   public Portlet getPortletWith(org.gatein.pc.api.PortletContext portletContext, Registration registration) throws InvalidHandle, PortletInvokerException
   {
      Portlet portlet;
      try
      {
         RegistrationLocal.setRegistration(registration);
         portlet = invoker.getPortlet(portletContext);
      }
      catch (NoSuchPortletException e)
      {
         throw WSRP2ExceptionFactory.throwWSException(InvalidHandle.class, "Couldn't find portlet with handle '" + portletContext.getId() + "'", e);
      }
      finally
      {
         RegistrationLocal.setRegistration(null);
      }

      if (!isRemotable(portlet))
      {
         throw WSRP2ExceptionFactory.throwWSException(InvalidHandle.class, "Portlet '" + portletContext.getId() + "' is not remotely available.", null);
      }

      return portlet;
   }

   public Set<Portlet> getRemotablePortlets() throws PortletInvokerException
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

   public Registration getRegistrationOrFailIfInvalid(RegistrationContext registrationContext) throws InvalidRegistration, OperationFailed, ModifyRegistrationRequired
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
      RuntimeOptionInfo runtimeOptionInfo = runtimeOptions.get(RuntimeOptionInfo.REMOTABLE_RUNTIME_OPTION);

      return runtimeOptionInfo != null && "true".equals(runtimeOptionInfo.getValues().get(0));
   }

   public List<String> getSupportedLocales()
   {
      if (producerContext != null)
      {
         return WSRPUtils.convertLocalesToRFC3066LanguageTags(producerContext.getSupportedLocales());
      }
      else
      {
         return WSRPConstants.getDefaultLocales();
      }
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

   public void onEvent(ManagedObjectRegistryEvent event)
   {
      serviceDescriptionHandler.onEvent(event);
   }
}
