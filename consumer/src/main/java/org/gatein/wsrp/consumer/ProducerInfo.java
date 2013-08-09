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

import org.gatein.common.NotYetImplemented;
import org.gatein.common.util.ParameterValidation;
import org.gatein.pc.api.InvokerUnavailableException;
import org.gatein.pc.api.NoSuchPortletException;
import org.gatein.pc.api.Portlet;
import org.gatein.pc.api.PortletContext;
import org.gatein.pc.api.PortletInvokerException;
import org.gatein.pc.api.info.EventInfo;
import org.gatein.pc.api.info.TypeInfo;
import org.gatein.wsrp.WSRPConstants;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.WSRPUtils;
import org.gatein.wsrp.consumer.portlet.WSRPPortlet;
import org.gatein.wsrp.consumer.portlet.info.WSRPEventInfo;
import org.gatein.wsrp.consumer.portlet.info.WSRPPortletInfo;
import org.gatein.wsrp.consumer.registry.ConsumerRegistry;
import org.gatein.wsrp.servlet.UserAccess;
import org.oasis.wsrp.v2.CookieProtocol;
import org.oasis.wsrp.v2.EventDescription;
import org.oasis.wsrp.v2.ExportDescription;
import org.oasis.wsrp.v2.Extension;
import org.oasis.wsrp.v2.ExtensionDescription;
import org.oasis.wsrp.v2.InvalidHandle;
import org.oasis.wsrp.v2.InvalidRegistration;
import org.oasis.wsrp.v2.ItemDescription;
import org.oasis.wsrp.v2.Lifetime;
import org.oasis.wsrp.v2.ModelDescription;
import org.oasis.wsrp.v2.ModelTypes;
import org.oasis.wsrp.v2.ModifyRegistrationRequired;
import org.oasis.wsrp.v2.OperationFailed;
import org.oasis.wsrp.v2.PortletDescription;
import org.oasis.wsrp.v2.PortletPropertyDescriptionResponse;
import org.oasis.wsrp.v2.RegistrationContext;
import org.oasis.wsrp.v2.RegistrationData;
import org.oasis.wsrp.v2.ResourceList;
import org.oasis.wsrp.v2.ServiceDescription;
import org.oasis.wsrp.v2.WSRPV2PortletManagementPortType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.wsdl.WSDLException;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceException;
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
   static final String RECOVERY_ATTEMPT_MESSAGE = "Attempting recovery by switching producer URL if possible";
   private final static Logger log = LoggerFactory.getLogger(ProducerInfo.class);
   private final static boolean debug = log.isDebugEnabled();
   public static final Integer DEFAULT_CACHE_VALUE = 300;

   // Persistent information

   /** persistence key */
   private String key;

   /** Configuration of the remote WS endpoints */
   private EndpointConfigurationInfo persistentEndpointInfo;

   /** Registration information */
   private RegistrationInfo persistentRegistrationInfo;

   /** The Producer's identifier */
   private String persistentId;

   /** The cache expiration duration (in seconds) for cached values */
   private Integer persistentExpirationCacheSeconds = DEFAULT_CACHE_VALUE;

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

   private transient RegistrationInfo expectedRegistrationInfo;

   private Map<String, ItemDescription> customModes;
   private Map<String, ItemDescription> customWindowStates;

   /** Events */
   private Map<QName, EventInfo> eventDescriptions;

   /*protected org.oasis.wsrp.v1.ItemDescription[] userCategoryDescriptions;
   protected org.oasis.wsrp.v1.ItemDescription[] customUserProfileItemDescriptions;   

   protected java.lang.String[] locales;
   protected org.oasis.wsrp.v1.ResourceList resourceList;*/


   public ProducerInfo()
   {
      persistentEndpointInfo = new EndpointConfigurationInfo();
      persistentRegistrationInfo = RegistrationInfo.createUndeterminedRegistration(this);
   }

   @Override
   public boolean equals(Object o)
   {
      if (this == o)
      {
         return true;
      }
      if (o == null || getClass() != o.getClass())
      {
         return false;
      }

      ProducerInfo that = (ProducerInfo)o;

      if (key != null ? !key.equals(that.key) : that.key != null)
      {
         return false;
      }
      if (!persistentId.equals(that.persistentId))
      {
         return false;
      }

      return true;
   }

   @Override
   public int hashCode()
   {
      int result = key != null ? key.hashCode() : 0;
      result = 31 * result + persistentId.hashCode();
      return result;
   }

   @Override
   public String toString()
   {
      final StringBuilder sb = new StringBuilder();
      sb.append("ProducerInfo");
      sb.append("{key='").append(key).append('\'');
      sb.append(", id='").append(persistentId).append('\'');
      sb.append('}');
      return sb.toString();
   }

   public ConsumerRegistry getRegistry()
   {
      return registry;
   }

   public void setRegistry(ConsumerRegistry registry)
   {
      this.registry = registry;
   }

   public String getKey()
   {
      return key;
   }

   public void setKey(String key)
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

   public void setEndpointConfigurationInfo(EndpointConfigurationInfo endpointConfigurationInfo)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(endpointConfigurationInfo, "EndpointConfigurationInfo");
      this.persistentEndpointInfo = endpointConfigurationInfo;
   }

   public RegistrationInfo getRegistrationInfo()
   {
      // update parent since it might not be set when unfrozen from persistence
      persistentRegistrationInfo.setParent(this);
      return persistentRegistrationInfo;
   }

   public void setRegistrationInfo(RegistrationInfo registrationInfo)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(registrationInfo, "RegistrationInfo");
      this.persistentRegistrationInfo = registrationInfo;
   }

   public boolean isRegistered()
   {
      return persistentRegistrationInfo.isRegistered();
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
      return persistentActive/* && persistentEndpointInfo.isAvailable()*/;
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

   public RegistrationInfo getExpectedRegistrationInfo()
   {
      return expectedRegistrationInfo;
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

      // if the refresh failed, return immediately
      if (RefreshResult.Status.FAILURE.equals(result.getStatus()))
      {
         setActiveAndSave(false);
         return result;
      }

      // update DB
      if (result.didRefreshHappen())
      {
         // mark as inactive if the refresh had issues...
         if (result.hasIssues())
         {
            setActive(false);

            // record what the Producer's expectations are if we managed to get a service description
            expectedRegistrationInfo = new RegistrationInfo(this.persistentRegistrationInfo);
            expectedRegistrationInfo.refresh(result.getServiceDescription(), getId(), true, true, true);
         }
         else
         {
            // mark as active if it wasn't already
            if (!isActive())
            {
               setActive(true);
            }

            // if we didn't have any issues, then the expected registration info is the one we have
            expectedRegistrationInfo = persistentRegistrationInfo;
         }

         registry.updateProducerInfo(this);
      }

      return result;
   }

   private RefreshResult internalRefresh(boolean forceRefresh) throws PortletInvokerException
   {
      ServiceDescription serviceDescription;

      if (isModifyRegistrationRequired())
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
            log.debug("Couldn't refresh endpoint information, attempting a second time: " + e, e);

            // try again as refresh on a failed service factory will fail without attempting the refresh
            try
            {
               persistentEndpointInfo.forceRefresh();
            }
            catch (InvokerUnavailableException e1)
            {
               result.setStatus(RefreshResult.Status.FAILURE);
               return result;
            }
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
               log.debug("OperationFailedFault occurred, might indicate a need to modify registration", operationFailedFault);

               return handleModifyRegistrationNeeded(result);
            }
            else
            {
               serviceDescription = rethrowAsInvokerUnvailable(operationFailedFault);
            }
         }
         catch (InvalidRegistration invalidRegistrationFault)
         {
            log.debug("InvalidRegistrationFault occurred", invalidRegistrationFault);

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
         catch (ModifyRegistrationRequired modifyRegistrationRequired)
         {
            return handleModifyRegistrationNeeded(result);
         }

         return refreshInfo(forceRefresh, serviceDescription, result);
      }

      return new RefreshResult(RefreshResult.Status.BYPASSED);
   }

   private RefreshResult handleModifyRegistrationNeeded(RefreshResult result) throws PortletInvokerException
   {
      ServiceDescription serviceDescription;// attempt to get unregistered service description
      serviceDescription = getServiceDescription(true);
      result.setServiceDescription(serviceDescription);

      // re-validate the registration information
      RefreshResult registrationResult = internalRefreshRegistration(serviceDescription, false, true, true);
      if (registrationResult.hasIssues())
      {
         // if the registration validation has issues, we need to modify our local information
         setModifyRegistrationRequired(true);
         setActive(false);
      }
      else
      {
         // we might be in a situation where the producer changed the registration back to the initial state
         // which is, granted, pretty rare... attempt modifyRegistration
         log.debug("modifyRegistration was called after OperationFailedFault when a check of registration data didn't reveal any issue...");
         modifyRegistration(true);
      }

      result.setRegistrationResult(registrationResult);
      return result;
   }

   private RefreshResult refreshInfo(boolean forceRefresh, ServiceDescription serviceDescription, RefreshResult result)
      throws PortletInvokerException
   {
      // do we need to call initCookie or not?
      requiresInitCookie = serviceDescription.getRequiresInitCookie();
      log.debug("Requires initCookie: " + requiresInitCookie);

      // custom mode descriptions
      customModes = toMap(serviceDescription.getCustomModeDescriptions());

      // custom window state descriptions
      customWindowStates = toMap(serviceDescription.getCustomWindowStateDescriptions());

      // event descriptions
      List<EventDescription> eventDescriptions = serviceDescription.getEventDescriptions();
      if (!eventDescriptions.isEmpty())
      {
         this.eventDescriptions = new HashMap<QName, EventInfo>(eventDescriptions.size());

         for (final EventDescription event : eventDescriptions)
         {
            QName name = event.getName();
            EventInfo eventInfo = new WSRPEventInfo(
               name,
               WSRPUtils.convertToCommonLocalizedStringOrNull(event.getLabel()),
               WSRPUtils.convertToCommonLocalizedStringOrNull(event.getDescription()),
               new TypeInfo()
               {
                  public String getName()
                  {
                     return event.getType().toString();
                  }

                  public XmlRootElement getXMLBinding()
                  {
                     throw new NotYetImplemented(); // todo
                  }
               },
               event.getAliases());

            this.eventDescriptions.put(name, eventInfo);
         }
      }

      // do we need to register?
      if (serviceDescription.isRequiresRegistration())
      {
         // refresh and force check for extra props if the registered SD failed
         // todo: deal with forcing check of extra registration properties properly (if needed)
         RefreshResult registrationResult = internalRefreshRegistration(serviceDescription, true, forceRefresh, false);

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
            log.debug("Portlet '" + portletHandle + "' will store persistent state for each user.");
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
            if (canAttemptRecoveryFrom(e))
            {
               return getPortlet(portletContext);
            }
            else
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
      expirationTimeMillis = System.currentTimeMillis() + (getSafeExpirationCacheSeconds() * 1000);
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
      // record the previous cache expiration duration
      Integer previousMS = getSafeExpirationCacheSeconds() * 1000;

      // assign the new value
      this.persistentExpirationCacheSeconds = expirationCacheSeconds;

      // recompute the expiration time based on previous value and new one
      long lastExpirationTimeChange = expirationTimeMillis - previousMS;
      int newMS = getSafeExpirationCacheSeconds() * 1000;
      if (lastExpirationTimeChange > 0)
      {
         expirationTimeMillis = lastExpirationTimeChange + newMS;
      }
      else
      {
         expirationTimeMillis = System.currentTimeMillis();
      }

   }

   /**
    * Returns the cache expiration duration in seconds as a positive value or zero so that it's safe to use in cache
    * expiration time computations.
    *
    * @return
    */
   private int getSafeExpirationCacheSeconds()
   {
      return useCache() ? persistentExpirationCacheSeconds : 0;
   }

   private ServiceDescription getUnmanagedServiceDescription(boolean asUnregistered) throws PortletInvokerException, OperationFailed, InvalidRegistration, ModifyRegistrationRequired
   {
      //todo: might need to implement customization of default service description
      ServiceDescription serviceDescription;
      try
      {
         Holder<Boolean> requiresRegistration = new Holder<Boolean>();
         Holder<List<PortletDescription>> offeredPortlets = new Holder<List<PortletDescription>>();
         Holder<List<ItemDescription>> userCategoryDescriptions = new Holder<List<ItemDescription>>();
         Holder<List<ItemDescription>> windowStateDescriptions = new Holder<List<ItemDescription>>();
         Holder<List<ItemDescription>> modeDescriptions = new Holder<List<ItemDescription>>();
         Holder<CookieProtocol> requiresInitCookie = new Holder<CookieProtocol>();
         Holder<ModelDescription> registrationPropertyDescription = new Holder<ModelDescription>();
         Holder<List<String>> locales = new Holder<List<String>>();
         Holder<ResourceList> resourceList = new Holder<ResourceList>();
         Holder<List<EventDescription>> eventDescriptions = new Holder<List<EventDescription>>();
         Holder<ModelTypes> schemaTypes = new Holder<ModelTypes>();
         Holder<List<String>> supportedOptions = new Holder<List<String>>();
         Holder<ExportDescription> exportDescription = new Holder<ExportDescription>();
         Holder<Boolean> mayReturnRegistrationState = new Holder<Boolean>();

         // invocation
         persistentEndpointInfo.getServiceDescriptionService().getServiceDescription(
            asUnregistered ? null : getRegistrationContext(),
            WSRPConstants.getDefaultLocales(), // todo: deal with locales better
            null, // todo: provide a way to only request info on some portlets?
            UserAccess.getUserContext(),
            requiresRegistration,
            offeredPortlets,
            userCategoryDescriptions,
            new Holder<List<ExtensionDescription>>(),
            windowStateDescriptions,
            modeDescriptions,
            requiresInitCookie,
            registrationPropertyDescription,
            locales,
            resourceList,
            eventDescriptions,
            schemaTypes,
            supportedOptions,
            exportDescription,
            mayReturnRegistrationState,
            new Holder<List<Extension>>());

         // TODO: fix-me
         serviceDescription = WSRPTypeFactory.createServiceDescription(requiresRegistration.value);
         serviceDescription.setRegistrationPropertyDescription(registrationPropertyDescription.value);
         serviceDescription.setRequiresInitCookie(requiresInitCookie.value);
         serviceDescription.setResourceList(resourceList.value);
         if (ParameterValidation.existsAndIsNotEmpty(modeDescriptions.value))
         {
            serviceDescription.getCustomModeDescriptions().addAll(modeDescriptions.value);
         }
         if (ParameterValidation.existsAndIsNotEmpty(windowStateDescriptions.value))
         {
            serviceDescription.getCustomWindowStateDescriptions().addAll(windowStateDescriptions.value);
         }
         if (ParameterValidation.existsAndIsNotEmpty(locales.value))
         {
            serviceDescription.getLocales().addAll(locales.value);
         }
         if (ParameterValidation.existsAndIsNotEmpty(offeredPortlets.value))
         {
            serviceDescription.getOfferedPortlets().addAll(offeredPortlets.value);
         }
         if (ParameterValidation.existsAndIsNotEmpty(userCategoryDescriptions.value))
         {
            serviceDescription.getUserCategoryDescriptions().addAll(userCategoryDescriptions.value);
         }

         return serviceDescription;
      }
      catch (Exception e)
      {
         if (canAttemptRecoveryFrom(e))
         {
            return getUnmanagedServiceDescription(asUnregistered);
         }
         else
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
            else if (e instanceof ModifyRegistrationRequired)
            {
               throw (ModifyRegistrationRequired)e;
            }

            return rethrowAsInvokerUnvailable(e);
         }
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
      catch (ModifyRegistrationRequired modifyRegistrationRequired)
      {
         return rethrowAsInvokerUnvailable(modifyRegistrationRequired);
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
         WSRPV2PortletManagementPortType service = getEndpointConfigurationInfo().getPortletManagementService();

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
         if (canAttemptRecoveryFrom(e))
         {
            return getPropertyDescriptionsFor(portletHandle);
         }
         else
         {
            // if we receive an exception that we cannot handle, since the support for PortletManagement is optional,
            // just return null as if the portlet had no properties
            log.debug("Couldn't get property descriptions for portlet '" + portletHandle + "'", e);
            return null;
         }
      }
   }

   public void register() throws PortletInvokerException
   {
      try
      {
         register(null, false);
      }
      catch (PortletInvokerException e)
      {
         registry.updateProducerInfo(this);
         throw e;
      }
   }

   /**
    * Attempts to register with the producer.
    *
    * @param serviceDescription
    * @param forceRefresh
    * @return <code>true</code> if the client code should ask for a new service description, <code>false</code> if the
    * specified description is good to be further processed
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
                     registrationData,
                     null, // todo: support leasing?
                     UserAccess.getUserContext(),
                     registrationState,
                     new Holder<Lifetime>(),
                     new Holder<List<Extension>>(),
                     registrationHandle
                  );

                  RegistrationContext registrationContext = WSRPTypeFactory.createRegistrationContext(registrationHandle.value);
                  registrationContext.setRegistrationState(registrationState.value);

                  persistentRegistrationInfo.setRegistrationContext(registrationContext);

                  if (debug)
                  {
                     String msg = "Consumer with id '" + persistentId + "' successfully registered with handle: '"
                        + registrationContext.getRegistrationHandle() + "'";
                     log.debug(msg);
                  }

                  RefreshResult res = new RefreshResult();

                  res.setRegistrationResult(result);
                  return res;
               }
               catch (Exception e)
               {
                  if (canAttemptRecoveryFrom(e))
                  {
                     return register(serviceDescription, forceRefresh);
                  }
                  else
                  {
                     persistentRegistrationInfo.resetRegistration();
                     setActive(false);
                     throw new PortletInvokerException("Couldn't register with producer '" + persistentId + "'", e);
                  }
               }
            }
            else
            {
               log.debug(result.getStatus().toString());
               setActive(false);
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
            persistentEndpointInfo.getRegistrationService().deregister(registrationContext, UserAccess.getUserContext());
            log.info("Consumer with id '" + persistentId + "' deregistered.");
         }
         catch (Exception e)
         {
            if (canAttemptRecoveryFrom(e))
            {
               deregister();
            }
            else
            {
               throw new PortletInvokerException("Couldn't deregister with producer '" + persistentId + "'", e);
            }
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
      try
      {
         modifyRegistration(false);
      }
      finally
      {
         registry.updateProducerInfo(this);
      }
   }

   private void modifyRegistration(boolean force) throws PortletInvokerException
   {
      if (persistentRegistrationInfo.getRegistrationHandle() != null)
      {
         persistentEndpointInfo.refresh();

         if (force || isModifyRegistrationRequired())
         {
            try
            {
               RegistrationContext registrationContext = getRegistrationContext();
               Holder<byte[]> registrationState = new Holder<byte[]>();

               // invocation
               persistentEndpointInfo.getRegistrationService().modifyRegistration(
                  registrationContext,
                  persistentRegistrationInfo.getRegistrationData(),
                  UserAccess.getUserContext(),
                  registrationState,
                  new Holder<Lifetime>(),
                  new Holder<List<Extension>>());

               // force refresh of internal RegistrationInfo state
               persistentRegistrationInfo.setRegistrationValidInternalState();

               // registration is not modified anymore :)
               setModifyRegistrationRequired(false);

               // update state
               persistentRegistrationInfo.setRegistrationState(registrationState.value);

               log.info("Consumer with id '" + persistentId + "' sucessfully modified its registration.");

               // reset cache to be able to see new offered portlets on the next refresh
               invalidateCache();
            }
            catch (Exception e)
            {
               if (canAttemptRecoveryFrom(e))
               {
                  modifyRegistration(force);
               }
               else
               {
                  throw new PortletInvokerException("Couldn't modify registration with producer '" + persistentId + "'", e);
               }
            }
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

   public EventInfo getInfoForEvent(QName name)
   {
      if (eventDescriptions == null)
      {
         return null;
      }
      else
      {
         return eventDescriptions.get(name);
      }
   }

   public boolean canAttemptRecoveryFrom(Throwable cause)
   {
      if ((cause instanceof WebServiceException || cause instanceof WSDLException) && persistentEndpointInfo.switchProducerIfPossible())
      {
         log.debug(RECOVERY_ATTEMPT_MESSAGE);
         return true;
      }
      else
      {
         return false;
      }
   }
}
