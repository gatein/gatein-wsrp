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
import org.gatein.pc.api.NoSuchPortletException;
import org.gatein.pc.api.Portlet;
import org.gatein.pc.api.PortletContext;
import org.gatein.pc.api.PortletInvokerException;
import org.gatein.wsrp.WSRPConstants;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.WSRPUtils;
import org.gatein.wsrp.consumer.portlet.WSRPPortlet;
import org.gatein.wsrp.consumer.portlet.info.WSRPPortletInfo;
import org.gatein.wsrp.consumer.registry.ConsumerRegistry;
import org.gatein.wsrp.servlet.UserAccess;
import org.oasis.wsrp.v1.CookieProtocol;
import org.oasis.wsrp.v1.Extension;
import org.oasis.wsrp.v1.InvalidHandle;
import org.oasis.wsrp.v1.InvalidRegistration;
import org.oasis.wsrp.v1.ItemDescription;
import org.oasis.wsrp.v1.ModelDescription;
import org.oasis.wsrp.v1.OperationFailed;
import org.oasis.wsrp.v1.PortletDescription;
import org.oasis.wsrp.v1.PortletPropertyDescriptionResponse;
import org.oasis.wsrp.v1.RegistrationContext;
import org.oasis.wsrp.v1.RegistrationData;
import org.oasis.wsrp.v1.ResourceList;
import org.oasis.wsrp.v1.ServiceDescription;
import org.oasis.wsrp.v1.WSRPV1PortletManagementPortType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.ws.Holder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 12692 $
 * @since 2.6
 */
public class ProducerInfo
{
   private final static Logger log = LoggerFactory.getLogger(ProducerInfo.class);

   // Persistent information

   /** DB primary key */
   private Long key;

   /** Configuration of the remote WS endpoints */
   private EndpointConfigurationInfo persistentEndpointInfo;

   /** Registration information */
   private RegistrationInfo persistentRegistrationInfo;

   /** The Producer's identifier */
   private String persistentId;

   /** The cache expiration duration (in seconds) for cached values */
   private Integer persistentExpirationCacheSeconds;

   /** The activated status of the associated Consumer */
   private boolean persistentActive;

   // Transient information

   /** The Cookie handling policy required by the Producer */
   private CookieProtocol requiresInitCookie;

   /** The Producer-Offered Portlets (handle -> WSRPPortlet) */
   private Map<String, Portlet> popsMap;

   /** A cache for Consumer-Configured Portlets (handle -> WSRPPortlet) */
   private Map<String, Portlet> ccpsMap;

   /** Portlet groups. */
   private Map<String, Set<Portlet>> portletGroups;

   /** Time at which the cache expires */
   private long expirationTimeMillis;

   private boolean isModifyRegistrationRequired;

   private ConsumerRegistry registry;
   private static final String ERASED_LOCAL_REGISTRATION_INFORMATION = "Erased local registration information!";

   private Map<String, ItemDescription> customModes;
   private Map<String, ItemDescription> customWindowStates;

   /*protected org.oasis.wsrp.v1.ItemDescription[] userCategoryDescriptions;
   protected org.oasis.wsrp.v1.ItemDescription[] customUserProfileItemDescriptions;   

   protected java.lang.String[] locales;
   protected org.oasis.wsrp.v1.ResourceList resourceList;*/


   public ProducerInfo()
   {
      persistentEndpointInfo = new EndpointConfigurationInfo(this);
      persistentRegistrationInfo = RegistrationInfo.createUndeterminedRegistration(this);
   }

   public ConsumerRegistry getRegistry()
   {
      return registry;
   }

   public void setRegistry(ConsumerRegistry registry)
   {
      this.registry = registry;
   }

   public Long getKey()
   {
      return key;
   }

   public void setKey(Long key)
   {
      this.key = key;
   }

   public Set<String> getSupportedCustomModes()
   {
      if (customModes == null)
      {
         return Collections.emptySet();
      }
      return Collections.unmodifiableSet(customModes.keySet());
   }

   public Set<String> getSupportedCustomWindowStates()
   {
      if (customWindowStates == null)
      {
         return Collections.emptySet();
      }
      return Collections.unmodifiableSet(customWindowStates.keySet());
   }

   public EndpointConfigurationInfo getEndpointConfigurationInfo()
   {
      return persistentEndpointInfo;
   }

   void setEndpointConfigurationInfo(EndpointConfigurationInfo endpointConfigurationInfo)
   {
      this.persistentEndpointInfo = endpointConfigurationInfo;
   }

   public RegistrationInfo getRegistrationInfo()
   {
      // update parent since it might not be set when unfrozen from Hibernate
      persistentRegistrationInfo.setParent(this);
      return persistentRegistrationInfo;
   }

   public void setRegistrationInfo(RegistrationInfo registrationInfo)
   {
      this.persistentRegistrationInfo = registrationInfo;
   }

   public boolean isRegistered()
   {
      Boolean valid = persistentRegistrationInfo.isRegistrationValid();
      if (valid == null)
      {
         return persistentRegistrationInfo.getRegistrationHandle() != null;
      }
      else
      {
         return valid;
      }
   }

   public boolean isRegistrationRequired()
   {
      return persistentRegistrationInfo.isRegistrationDeterminedRequired();
   }

   public boolean isRegistrationChecked()
   {
      return persistentRegistrationInfo.isRegistrationRequired() != null;
   }

   public boolean hasLocalRegistrationInfo()
   {
      return persistentRegistrationInfo.hasLocalInfo();
   }

   /**
    * Determines whether the associated consumer is active.
    *
    * @return
    */
   public boolean isActive()
   {
      return persistentActive && persistentEndpointInfo.isAvailable();
   }

   /**
    * Activates or de-activate this Consumer. Note that this shouldn't be called directly as ConsumersRegistry will
    * handle activation.
    *
    * @param active
    */
   public void setActive(boolean active)
   {
      this.persistentActive = active;
   }

   public void setActiveAndSave(boolean active)
   {
      setActive(active);
      registry.updateProducerInfo(this);
   }

   public boolean isModifyRegistrationRequired()
   {
      return isModifyRegistrationRequired || persistentRegistrationInfo.isModifyRegistrationNeeded();
   }

   // FIX-ME: remove when a better dirty management is in place at property level
   public void setModifyRegistrationRequired(boolean modifyRegistrationRequired)
   {
      this.isModifyRegistrationRequired = modifyRegistrationRequired;
   }

   public CookieProtocol getRequiresInitCookie()
   {
      return requiresInitCookie;
   }

   /**
    * Refreshes the producer's information from the service description if required.
    *
    * @param forceRefresh whether or not to force a refresh regardless of whether one would have been required based on
    *                     cache expiration
    * @return <code>true</code> if the producer's information was just refreshed, <code>false</code> otherwise
    * @throws PortletInvokerException if registration was required but couldn't be achieved properly
    */
   public boolean refresh(boolean forceRefresh) throws PortletInvokerException
   {
      return detailedRefresh(forceRefresh).didRefreshHappen();
   }

   public RefreshResult detailedRefresh(boolean forceRefresh) throws PortletInvokerException
   {
      RefreshResult result = internalRefresh(forceRefresh);

      // update DB
      if (result.didRefreshHappen())
      {
         // mark as inactive if the refresh had issues...
         if (result.hasIssues())
         {
            setActive(false);
         }
         else
         {
            // mark as active if it wasn't already
            if (!isActive())
            {
               setActive(true);
            }
         }

         registry.updateProducerInfo(this);
      }

      return result;
   }

   private RefreshResult internalRefresh(boolean forceRefresh) throws PortletInvokerException
   {
      ServiceDescription serviceDescription;

      if (isModifyRegistrationRequired)
      {
         return new RefreshResult(RefreshResult.Status.MODIFY_REGISTRATION_REQUIRED);
      }

      // might neeed a different cache value: right now, we cache the whole producer info but we might want to cache
      // POPs and rest of producer info separetely...
      if (forceRefresh || isRefreshNeeded(true))
      {
         log.debug("Refreshing info for producer '" + getId() + "'");


         RefreshResult result = new RefreshResult(); // success by default!

         try
         {
            persistentEndpointInfo.refresh();
         }
         catch (InvokerUnavailableException e)
         {
            log.debug("Couldn't refresh endpoint information, attempting a second time: " + e);

            // try again as refresh on a failed service factory will fail without attempting the refresh
            persistentEndpointInfo.forceRefresh();
            // todo: should we fail fast here?
            // throw new PortletInvokerException("Couldn't refresh endpoint information: " + e.getLocalizedMessage());
         }
         finally
         {
            // save changes to endpoint
            registry.updateProducerInfo(this);
         }

         // get the service description from the producer
         try
         {
            // if we don't yet have registration information, get an unregistered service description
            serviceDescription = getUnmanagedServiceDescription(persistentRegistrationInfo.isUndetermined());
            result.setServiceDescription(serviceDescription);
         }
         catch (OperationFailed operationFailedFault)
         {
            // if we have local registration info, the OperationFailedFault might indicate a need to call modifyRegistration
            if (hasLocalRegistrationInfo())
            {
               log.debug("OperationFailedFault occurred, might indicate a need to modify registration");

               // attempt to get unregistered service description
               serviceDescription = getServiceDescription(true);
               result.setServiceDescription(serviceDescription);

               // re-validate the registration information
               RefreshResult registrationResult = internalRefreshRegistration(serviceDescription, false, true, true);
               if (registrationResult.hasIssues())
               {
                  // if the registration validation has issues, we need to modify our local information
                  isModifyRegistrationRequired = true;
                  setActiveAndSave(false);
               }
               else
               {
                  // we might be in a situation where the producer changed the registration back to the initial state
                  // which is, granted, pretty rare... attempt modifyRegistration
                  log.debug("modifyRegistration was called after OperationFailedFault when a check of registration data didn't reveal any issue...");
                  modifyRegistration();
               }

               result.setRegistrationResult(registrationResult);
               return result;
            }
            else
            {
               serviceDescription = rethrowAsInvokerUnvailable(operationFailedFault);
            }
         }
         catch (InvalidRegistration invalidRegistrationFault)
         {
            log.debug("InvalidRegistrationFault occurred");

            // attempt to get unregistered service description
            serviceDescription = getServiceDescription(true);
            result.setServiceDescription(serviceDescription);

            // check our registration information against what is sent in the service description
            RefreshResult registrationResult = internalRefreshRegistration(serviceDescription, false, true, true);
            if (registrationResult.hasIssues())
            {
               setActiveAndSave(false);
               rethrowAsInvokerUnvailable(invalidRegistrationFault);
            }

            return refreshInfo(false, serviceDescription, result);
         }

         return refreshInfo(forceRefresh, serviceDescription, result);
      }

      return new RefreshResult(RefreshResult.Status.BYPASSED);
   }

   private RefreshResult refreshInfo(boolean forceRefresh, ServiceDescription serviceDescription, RefreshResult result)
      throws PortletInvokerException
   {
      // do we need to call initCookie or not?
      requiresInitCookie = serviceDescription.getRequiresInitCookie();

      // custom mode descriptions
      customModes = toMap(serviceDescription.getCustomModeDescriptions());

      // custom window state descriptions
      customWindowStates = toMap(serviceDescription.getCustomWindowStateDescriptions());

      // do we need to register?
      if (serviceDescription.isRequiresRegistration())
      {
         // refresh and force check for extra props if the registered SD failed
         // todo: deal with forcing check of extra registration properties properly (if needed)
         RefreshResult registrationResult = internalRefreshRegistration(serviceDescription, true, forceRefresh, false);
         registry.updateProducerInfo(this);

         // attempt to register and determine if the current service description can be used to extract POPs
         if (!registrationResult.hasIssues())
         {
            registrationResult = register(serviceDescription, false);
            if (!registrationResult.hasIssues())
            {
               // registration occurred, so we should ask for a new service description
               serviceDescription = getServiceDescription(false);
            }

            // extract the POPs
            extractOfferedPortlets(serviceDescription);
         }

         result.setRegistrationResult(registrationResult);

         return result;
      }
      else
      {
         log.debug("Registration not required");
         persistentRegistrationInfo = new RegistrationInfo(this, false);
         extractOfferedPortlets(serviceDescription);
         return result;
      }
   }

   private Map<String, ItemDescription> toMap(List<ItemDescription> itemDescriptions)
   {
      if (itemDescriptions == null)
      {
         return null;
      }
      else
      {
         Map<String, ItemDescription> result = new HashMap<String, ItemDescription>(itemDescriptions.size());
         for (ItemDescription itemDescription : itemDescriptions)
         {
            result.put(itemDescription.getItemName(), itemDescription);
         }
         return result;
      }
   }

   public String getId()
   {
      return persistentId;
   }

   public void setId(String id)
   {
      this.persistentId = id;
   }

   /**
    * Extracts a map of offered Portlet objects from ServiceDescription
    *
    * @param sd
    * @return a Map (portlet handle -> Portlet) of the offered portlets.
    */
   private Map extractOfferedPortlets(ServiceDescription sd)
   {
      if (sd == null)
      {
         throw new IllegalArgumentException("Provided ServiceDescription can't be null");
      }

      List<PortletDescription> portletDescriptions = sd.getOfferedPortlets();

      if (portletDescriptions != null)
      {
         int length = portletDescriptions.size();
         log.debug("Extracting " + length + " portlets.");
         popsMap = new LinkedHashMap<String, Portlet>(length);
         portletGroups = new HashMap<String, Set<Portlet>>();

         for (PortletDescription portletDescription : portletDescriptions)
         {
            WSRPPortlet wsrpPortlet = createWSRPPortletFromPortletDescription(portletDescription);

            if (wsrpPortlet != null)
            {
               popsMap.put(wsrpPortlet.getContext().getId(), wsrpPortlet);
            }
         }
      }
      else
      {
         popsMap = Collections.emptyMap();
         portletGroups = Collections.emptyMap();
      }

      //todo: could extract more information here... and rename method more appropriately
      resetCacheTimerIfNeeded();

      return popsMap;
   }

   /**
    * @param portletDescription
    * @return
    * @since 2.6
    */
   WSRPPortlet createWSRPPortletFromPortletDescription(PortletDescription portletDescription)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(portletDescription, "PortletDescription");
      String portletHandle = portletDescription.getPortletHandle();
      log.debug("Extracting info for '" + portletHandle + "' portlet");
      WSRPPortletInfo info = new WSRPPortletInfo(portletDescription, this);
      WSRPPortlet wsrpPortlet = null;
      if (info.isUsesMethodGet())
      {
         log.warn("Portlet '" + portletHandle
            + "' uses the GET method in forms. Since we don't handle this, this portlet will be excluded from " +
            "the list of offered portlets for producer " + persistentId);
      }
      else
      {
         if (info.isHasUserSpecificState())
         {
            log.debug("Portlet '" + portletHandle + "' will store persistent state for each user. NOT WELL TESTED!");
         }

         wsrpPortlet = new WSRPPortlet(PortletContext.createPortletContext(portletHandle), info);

         // add the portlet to the appropriate group if needed
         String portletGroupId = portletDescription.getGroupID();
         if (portletGroupId != null)
         {
            Set<Portlet> groupedPortlets = portletGroups.get(portletGroupId);
            if (groupedPortlets == null)
            {
               groupedPortlets = new HashSet<Portlet>();
               portletGroups.put(portletGroupId, groupedPortlets);
            }
            groupedPortlets.add(wsrpPortlet);
         }
      }
      return wsrpPortlet;
   }

   public Portlet getPortlet(PortletContext portletContext) throws PortletInvokerException
   {
      String portletHandle = portletContext.getId();
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(portletHandle, "Portlet handle", "getPortlet");
      log.debug("Retrieving portlet '" + portletHandle + "'");

      // check if we need to refresh
      boolean justRefreshed = refresh(false);

      // First try caches if caches are still valid or we just refreshed
      Portlet portlet = getPortletFromCaches(portletHandle, justRefreshed);

      if (portlet != null) // we had a match in cache, return it
      {
         log.debug("Portlet was cached");
         return portlet;
      }
      else // otherwise, retrieve just the information for the appropriate portlet
      {
         log.debug("Trying to retrieve portlet via getPortletDescription");

         try
         {
            Holder<PortletDescription> descriptionHolder = new Holder<PortletDescription>();
            persistentEndpointInfo.getPortletManagementService().getPortletDescription(
               getRegistrationContext(),
               WSRPUtils.convertToWSRPPortletContext(portletContext),
               UserAccess.getUserContext(),
               WSRPConstants.getDefaultLocales(), // todo: deal with locales better
               descriptionHolder,
               new Holder<ResourceList>(),
               new Holder<List<Extension>>());
            portlet = createWSRPPortletFromPortletDescription(descriptionHolder.value);

            // add the portlet to the CCP cache
            if (ccpsMap == null)
            {
               ccpsMap = new HashMap<String, Portlet>();
            }
            ccpsMap.put(portletHandle, portlet);

            return portlet;
         }
         catch (InvalidHandle invalidHandleFault)
         {
            throw new NoSuchPortletException(invalidHandleFault, portletHandle);
         }
         catch (Exception e)
         {
            log.debug("Couldn't get portlet via getPortletDescription for producer '" + persistentId
               + "'. Attempting to retrieve it from the service description as this producer might not support the PortletManagement interface.", e);

            justRefreshed = refresh(true);
            portlet = getPortletFromCaches(portletHandle, justRefreshed);

            if (portlet == null)
            {
               throw new NoSuchPortletException(portletHandle);
            }
            else
            {
               return portlet;
            }
         }
      }
   }

   private Portlet getPortletFromCaches(String portletHandle, boolean justRefreshed)
   {
      Portlet portlet = null;

      if (justRefreshed || (useCache() && !isCacheExpired()))
      {
         log.debug("Trying cached POPs");

         portlet = popsMap.get(portletHandle);

         if (portlet == null && ccpsMap != null)
         {
            log.debug("Trying cached CCPs");
            portlet = ccpsMap.get(portletHandle);
         }
      }
      return portlet;
   }

   Map<String, Set<Portlet>> getPortletGroupMap() throws PortletInvokerException
   {
      return portletGroups;
   }

   public Map<String, Portlet> getPortletMap() throws PortletInvokerException
   {
      refresh(false);
      return popsMap;
   }

   // Cache support ****************************************************************************************************

   private boolean useCache()
   {
      return persistentExpirationCacheSeconds != null && persistentExpirationCacheSeconds > 0;
   }

   private void resetCacheTimerIfNeeded()
   {
      if (useCache())
      {
         // reset expiration time
         expirationTimeMillis = System.currentTimeMillis() + (persistentExpirationCacheSeconds * 1000);
      }
   }

   /**
    * @return
    * @since 2.6
    */
   private boolean isCacheExpired()
   {
      boolean result = !useCache() || System.currentTimeMillis() > expirationTimeMillis || popsMap == null
         || portletGroups == null;
      if (result)
      {
         log.debug("Cache expired or not used");
      }
      return result;
   }

   public Integer getExpirationCacheSeconds()
   {
      return persistentExpirationCacheSeconds;
   }

   public void setExpirationCacheSeconds(Integer expirationCacheSeconds)
   {
      this.persistentExpirationCacheSeconds = expirationCacheSeconds;
   }

   private ServiceDescription getUnmanagedServiceDescription(boolean asUnregistered) throws PortletInvokerException, OperationFailed, InvalidRegistration
   {
      //todo: might need to implement customization of default service description
      ServiceDescription serviceDescription;
      try
      {
         Holder<Boolean> requiresRegistration = new Holder<Boolean>();
         Holder<List<PortletDescription>> offeredPortlets = new Holder<List<PortletDescription>>();
         Holder<List<ItemDescription>> userCategoryDescriptions = new Holder<List<ItemDescription>>();
         Holder<List<ItemDescription>> userProfileItemDescriptions = new Holder<List<ItemDescription>>();
         Holder<List<ItemDescription>> windowStateDescriptions = new Holder<List<ItemDescription>>();
         Holder<List<ItemDescription>> modeDescriptions = new Holder<List<ItemDescription>>();
         Holder<CookieProtocol> initCookie = new Holder<CookieProtocol>();
         Holder<ModelDescription> registrationPropertyDescription = new Holder<ModelDescription>();
         Holder<List<String>> locales = new Holder<List<String>>();
         Holder<ResourceList> resourceList = new Holder<ResourceList>();

         // invocation
         persistentEndpointInfo.getServiceDescriptionService().getServiceDescription(
            asUnregistered ? null : getRegistrationContext(),
            WSRPConstants.getDefaultLocales(), // todo: deal with locales better
            requiresRegistration,
            offeredPortlets,
            userCategoryDescriptions,
            userProfileItemDescriptions,
            windowStateDescriptions,
            modeDescriptions,
            initCookie,
            registrationPropertyDescription,
            locales,
            resourceList,
            new Holder<List<Extension>>());

         serviceDescription = WSRPTypeFactory.createServiceDescription(requiresRegistration.value);
         serviceDescription.setRegistrationPropertyDescription(registrationPropertyDescription.value);
         serviceDescription.setRequiresInitCookie(initCookie.value);
         serviceDescription.setResourceList(resourceList.value);
         serviceDescription.getCustomModeDescriptions().addAll(modeDescriptions.value);
         serviceDescription.getCustomUserProfileItemDescriptions().addAll(userProfileItemDescriptions.value);
         serviceDescription.getCustomWindowStateDescriptions().addAll(windowStateDescriptions.value);
         serviceDescription.getLocales().addAll(locales.value);
         serviceDescription.getOfferedPortlets().addAll(offeredPortlets.value);
         serviceDescription.getUserCategoryDescriptions().addAll(userCategoryDescriptions.value);

         return serviceDescription;
      }
      catch (Exception e)
      {
         log.debug("Caught Exception in getServiceDescription:\n", e);

         // de-activate
         setActiveAndSave(false);

         if (e instanceof InvalidRegistration)
         {
            resetRegistration();

            throw (InvalidRegistration)e;
         }
         else if (e instanceof OperationFailed)
         {
            throw (OperationFailed)e; // rethrow to deal at higher level as meaning can vary depending on context
         }

         return rethrowAsInvokerUnvailable(e);
      }
   }

   ServiceDescription getServiceDescription(boolean asUnregistered) throws PortletInvokerException
   {
      try
      {
         return getUnmanagedServiceDescription(asUnregistered);
      }
      catch (OperationFailed operationFailedFault)
      {
         return rethrowAsInvokerUnvailable(operationFailedFault);
      }
      catch (InvalidRegistration invalidRegistrationFault)
      {
         return rethrowAsInvokerUnvailable(invalidRegistrationFault);
      }
   }

   private ServiceDescription rethrowAsInvokerUnvailable(Exception e) throws InvokerUnavailableException
   {
      Throwable cause = e.getCause();
      throw new InvokerUnavailableException("Problem getting service description for producer "
         + persistentId + ", please see the logs for more information. ", cause == null ? e : cause);
   }

   public RegistrationContext getRegistrationContext() throws PortletInvokerException
   {
      if (persistentRegistrationInfo.isUndetermined())
      {
         refresh(false);
      }

      return persistentRegistrationInfo.getRegistrationContext();
   }

   public void resetRegistration() throws PortletInvokerException
   {
      persistentRegistrationInfo.resetRegistration();

      invalidateCache();
      registry.updateProducerInfo(this);
   }

   // make package only after package reorg
   public PortletPropertyDescriptionResponse getPropertyDescriptionsFor(String portletHandle)
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(portletHandle, "portlet handle", null);
      try
      {
         WSRPV1PortletManagementPortType service = getEndpointConfigurationInfo().getPortletManagementService();

         Holder<ModelDescription> modelDescription = new Holder<ModelDescription>();
         Holder<ResourceList> resourceList = new Holder<ResourceList>();
         service.getPortletPropertyDescription(
            getRegistrationContext(),
            WSRPTypeFactory.createPortletContext(portletHandle),
            UserAccess.getUserContext(),
            WSRPConstants.getDefaultLocales(),
            modelDescription,
            resourceList,
            new Holder<List<Extension>>());

         PortletPropertyDescriptionResponse response = WSRPTypeFactory.createPortletPropertyDescriptionResponse(null);
         response.setModelDescription(modelDescription.value);
         response.setResourceList(resourceList.value);

         return response;
      }
      catch (InvalidHandle invalidHandleFault)
      {
         throw new IllegalArgumentException("Unknown portlet '" + portletHandle + "'");
      }
      catch (InvalidRegistration invalidRegistrationFault)
      {
         try
         {
            resetRegistration();
         }
         catch (PortletInvokerException e)
         {
            throw new RuntimeException("Couldn't reset registration", e);
         }
         throw new IllegalArgumentException("Couldn't get property descriptions for portlet '" + portletHandle
            + "' because the provided registration is invalid!");
      }
      catch (Exception e)
      {
         // if we receive an exception that we cannot handle, since the support for PortletManagement is optional,
         // just return null as if the portlet had no properties
         log.debug("Couldn't get property descriptions for portlet '" + portletHandle + "'", e);
         return null;
      }
   }

   public void register() throws PortletInvokerException
   {
      register(null, false);
   }

   /**
    * Attempts to register with the producer.
    *
    * @param serviceDescription
    * @param forceRefresh
    * @return <code>true</code> if the client code should ask for a new service description, <code>false</code> if the
    *         specified description is good to be further processed
    * @throws PortletInvokerException
    * @since 2.6
    */
   private RefreshResult register(ServiceDescription serviceDescription, boolean forceRefresh) throws PortletInvokerException
   {
      if (!isRegistered())
      {
         persistentEndpointInfo.refresh();

         if (serviceDescription == null)
         {
            serviceDescription = getServiceDescription(false);
         }

         if (serviceDescription.isRequiresRegistration())
         {
            // check if the configured registration information is correct and if we can get the service description
            RefreshResult result = persistentRegistrationInfo.refresh(serviceDescription, persistentId, true, forceRefresh, false);
            if (!result.hasIssues())
            {
               try
               {
                  log.debug("Attempting registration");
                  RegistrationData registrationData = persistentRegistrationInfo.getRegistrationData();
                  Holder<String> registrationHandle = new Holder<String>();
                  Holder<byte[]> registrationState = new Holder<byte[]>();

                  // invocation
                  persistentEndpointInfo.getRegistrationService().register(
                     registrationData.getConsumerName(),
                     registrationData.getConsumerAgent(),
                     registrationData.isMethodGetSupported(),
                     registrationData.getConsumerModes(),
                     registrationData.getConsumerWindowStates(),
                     registrationData.getConsumerUserScopes(),
                     registrationData.getCustomUserProfileData(),
                     registrationData.getRegistrationProperties(),
                     new Holder<List<Extension>>(),
                     registrationHandle,
                     registrationState
                  );

                  RegistrationContext registrationContext = WSRPTypeFactory.createRegistrationContext(registrationHandle.value);
                  registrationContext.setRegistrationState(registrationState.value);

                  persistentRegistrationInfo.setRegistrationContext(registrationContext);
                  String msg = "Consumer with id '" + persistentId + "' successfully registered with handle: '"
                     + registrationContext.getRegistrationHandle() + "'";
                  log.debug(msg);
                  RefreshResult res = new RefreshResult();
                  res.setRegistrationResult(result);
                  return res;
               }
               catch (Exception e)
               {
                  persistentRegistrationInfo.resetRegistration();
                  setActive(false);
                  throw new PortletInvokerException("Couldn't register with producer '" + persistentId + "'", e);
               }
               finally
               {
                  registry.updateProducerInfo(this);
               }
            }
            else
            {
               log.debug(result.getStatus().toString());
               setActiveAndSave(false);
               throw new PortletInvokerException("Consumer is not ready to be registered with producer because of missing or invalid registration information.");
            }
         }
      }

      return new RefreshResult(RefreshResult.Status.BYPASSED);
   }

   public void deregister() throws PortletInvokerException
   {
      if (isRegistered())
      {
         persistentEndpointInfo.refresh();

         try
         {
            RegistrationContext registrationContext = getRegistrationContext();
            persistentEndpointInfo.getRegistrationService().deregister(
               registrationContext.getRegistrationHandle(),
               registrationContext.getRegistrationState(),
               new ArrayList<Extension>());
            log.info("Consumer with id '" + persistentId + "' deregistered.");
         }
         catch (Exception e)
         {
            throw new PortletInvokerException("Couldn't deregister with producer '" + persistentId + "'", e);
         }
         finally
         {
            resetRegistration();
         }
      }
      else
      {
         throw new IllegalStateException("Cannot deregister producer '" + persistentId + "' as it's not registered");
      }

   }

   public void modifyRegistration() throws PortletInvokerException
   {
      if (persistentRegistrationInfo.getRegistrationHandle() != null)
      {
         persistentEndpointInfo.refresh();

         try
         {
            RegistrationContext registrationContext = getRegistrationContext();
            Holder<byte[]> registrationState = new Holder<byte[]>();

            // invocation
            persistentEndpointInfo.getRegistrationService().modifyRegistration(
               registrationContext,
               persistentRegistrationInfo.getRegistrationData(),
               registrationState,
               new Holder<List<Extension>>());

            // force refresh of internal RegistrationInfo state
            persistentRegistrationInfo.setRegistrationValidInternalState();

            // registration is not modified anymore :)
            isModifyRegistrationRequired = false;

            // update state
            persistentRegistrationInfo.setRegistrationState(registrationState.value);

            log.info("Consumer with id '" + persistentId + "' sucessfully modified its registration.");

            // reset cache to be able to see new offered portlets on the next refresh
            invalidateCache();
         }
         catch (Exception e)
         {
            throw new PortletInvokerException("Couldn't modify registration with producer '" + persistentId + "'", e);
         }
         finally
         {
            registry.updateProducerInfo(this);
         }
      }
      else
      {
         throw new IllegalStateException("Cannot modify registration for producer '" + persistentId
            + "' as it's not registered");
      }
   }

   private void invalidateCache()
   {
      if (useCache())
      {
         expirationTimeMillis = System.currentTimeMillis();
      }
   }

   private RefreshResult internalRefreshRegistration(ServiceDescription serviceDescription, boolean mergeWithLocalInfo, boolean forceRefresh, boolean forceCheckOfExtraProps) throws PortletInvokerException
   {
      RefreshResult result =
         persistentRegistrationInfo.refresh(serviceDescription, persistentId, mergeWithLocalInfo, forceRefresh, forceCheckOfExtraProps);

      log.debug("Refreshed registration information for consumer with id '" + persistentId + "'");

      return result;
   }

   public boolean isRefreshNeeded(boolean considerCache)
   {
      boolean result = (considerCache && isCacheExpired())
         || persistentRegistrationInfo.isRefreshNeeded()
         || persistentEndpointInfo.isRefreshNeeded();
      if (result)
      {
         log.debug("Refresh needed for producer '" + persistentId + "'");
      }
      return result;
   }

   void removeHandleFromCaches(String portletHandle)
   {
      log.debug("Removing '" + portletHandle + "' from caches.");
      ccpsMap.remove(portletHandle);
      popsMap.remove(portletHandle);
   }

   public void eraseRegistrationInfo()
   {
      persistentRegistrationInfo = RegistrationInfo.createUndeterminedRegistration(this);

      registry.updateProducerInfo(this);

      log.warn(ERASED_LOCAL_REGISTRATION_INFORMATION);
   }
}
