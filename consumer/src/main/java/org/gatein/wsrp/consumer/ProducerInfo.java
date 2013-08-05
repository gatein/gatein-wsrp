/*
 * JBoss, a division of Red Hat
 * Copyright 2012, Red Hat Middleware, LLC, and individual
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
import org.gatein.wsrp.SupportsLastModified;
import org.gatein.wsrp.WSRPConstants;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.WSRPUtils;
import org.gatein.wsrp.consumer.portlet.WSRPPortlet;
import org.gatein.wsrp.consumer.portlet.info.WSRPEventInfo;
import org.gatein.wsrp.consumer.portlet.info.WSRPPortletInfo;
import org.gatein.wsrp.consumer.spi.ConsumerRegistrySPI;
import org.gatein.wsrp.servlet.UserAccess;
import org.gatein.wsrp.spec.v2.WSRP2Constants;
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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * ProducerInfo handles the consumer's state with respect to its remote producer. It encapsulates all the information needed to access and properly interact with a remote
 * producer. In this respect, ProducerInfo encapsulates all the persisted state of the consumer.
 * <p/>
 * It records, in particular:
 * <ul>
 * <li>the name assigned to this producer that is used to identify which consumer ({@link org.gatein.pc.api.PortletInvoker}) by the {@link
 * org.gatein.pc.federation.FederatingPortletInvoker} when it chooses which invoker to dispatch to</li>
 * <li>the connection to the remote producer via its {@link EndpointConfigurationInfo} member</li>
 * <li>the registration status and data that the remote producer requires using its {@link RegistrationInfo} member</li>
 * <li>as well as all the information gathered from examining the {@link ServiceDescription} returned by the producer</li>
 * </ul>
 * <p/>
 * The relation between a consumer and a producer is quite complex so it is usually cached to avoid having to retrieve and parse the producer's {@link ServiceDescription} for each
 * WSRP operation. However, since this relation can evolve over time, it is also necessary to be able to refresh it depending on the information returned by the producer as the
 * result of an interaction with it. In particular, the registration status might have changed or the cached information is staled. This updating of the producer metadata is
 * performed by the {@link #detailedRefresh(boolean)} method, which is the most important method of this class.
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 12692 $
 * @since 2.6
 */
public class ProducerInfo extends SupportsLastModified
{
   static final String RECOVERY_ATTEMPT_MESSAGE = "Attempting recovery by switching producer URL if possible";
   private static final Logger log = LoggerFactory.getLogger(ProducerInfo.class);
   private static final boolean debug = log.isDebugEnabled();
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
   private transient CookieProtocol requiresInitCookie;

   /** The Producer-Offered Portlets (handle -> WSRPPortlet) */
   private transient Map<String, Portlet> popsMap;

   /** A cache for Consumer-Configured Portlets (handle -> WSRPPortlet) */
   private transient Map<String, Portlet> ccpsMap;

   /** Portlet groups. */
   private transient Map<String, Set<Portlet>> portletGroups;

   /** Time at which the cache expires */
   private transient long expirationTimeMillis;

   /** The ConsumerRegistry used to persist Consumers and ProducerInfos, accessed using the internal SPI */
   private final transient ConsumerRegistrySPI registry;
   private static final String ERASED_LOCAL_REGISTRATION_INFORMATION = "Erased local registration information!";

   /**
    * The registration information that the remote producer expects extracted from the service description it sent, as opposed to the currently held registration information,
    * which
    * might be out of sync
    */
   private transient RegistrationInfo expectedRegistrationInfo;

   /** Custom modes supported by the remote producer */
   private transient Map<String, ItemDescription> customModes;

   /** Custom window states supported by the remote producer */
   private transient Map<String, ItemDescription> customWindowStates;

   /** Events */
   private transient Map<QName, EventInfo> eventDescriptions;

   /** Supported options */
   private transient Set<String> supportedOptions = Collections.emptySet();

   /*protected org.oasis.wsrp.v1.ItemDescription[] userCategoryDescriptions;
   protected org.oasis.wsrp.v1.ItemDescription[] customUserProfileItemDescriptions;   

   protected java.lang.String[] locales;
   protected org.oasis.wsrp.v1.ResourceList resourceList;*/


   public ProducerInfo(ConsumerRegistrySPI consumerRegistry)
   {
      persistentEndpointInfo = new EndpointConfigurationInfo();
      persistentRegistrationInfo = RegistrationInfo.createUndeterminedRegistration(this);
      this.registry = consumerRegistry;
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
      if (!getId().equals(that.getId()))
      {
         return false;
      }

      return true;
   }

   @Override
   public int hashCode()
   {
      int result = key != null ? key.hashCode() : 0;
      result = 31 * result + getId().hashCode();
      return result;
   }

   @Override
   public String toString()
   {
      return "ProducerInfo {key='" + key + "', id='" + getId() + "'}";
   }

   public ConsumerRegistrySPI getRegistry()
   {
      return registry;
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

   /** Determines whether the associated consumer is active. */
   public boolean isActive()
   {
      return persistentActive;
   }

   /**
    * Activates or de-activate this Consumer. Note that this shouldn't be called directly as ConsumersRegistry will
    * handle activation.
    */
   public void setActive(boolean active)
   {
      setInternalActive(active);
   }

   private boolean setInternalActive(boolean active)
   {
      final boolean modified = modifyNowIfNeeded(persistentActive, active);
      this.persistentActive = active;
      return modified;
   }

   public String getId()
   {
      return persistentId;
   }

   public void setId(String id)
   {
      modifyNowIfNeeded(persistentId, id);
      this.persistentId = id;
   }

   public void setActiveAndSave(boolean active)
   {
      if (setInternalActive(active))
      {
         registry.updateProducerInfo(this);
      }
   }

   public boolean isModifyRegistrationRequired()
   {
      return persistentRegistrationInfo.isModifyRegistrationNeeded();
   }

   // FIX-ME: remove when a better dirty management is in place at property level

   public void setModifyRegistrationRequired(boolean modifyRegistrationRequired)
   {
      modifyNowIfNeeded(persistentRegistrationInfo.isModifyRegistrationNeeded(), modifyRegistrationRequired);
      persistentRegistrationInfo.setModifyRegistrationNeeded(modifyRegistrationRequired);
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

   /**
    * Refreshes the producer's information from the service description if required.
    *
    * @param forceRefresh whether or not to force a refresh regardless of whether one would have been required based on
    *                     cache expiration
    * @return detailed information about the result of the refresh in the form of a {@link RefreshResult} object
    * @throws PortletInvokerException if registration was required but couldn't be achieved properly
    */
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

         // persist any changes made to this ProducerInfo
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

      // might need a different cache value: right now, we cache the whole producer info but we might want to cache
      // POPs and rest of producer info separately...
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
      ServiceDescription serviceDescription;
      serviceDescription = getServiceDescription(true); // attempt to get unregistered service description
      result.setServiceDescription(serviceDescription);

      // re-validate the registration information
      RefreshResult registrationResult = internalRefreshRegistration(serviceDescription, false, true, true);
      if (registrationResult.hasIssues())
      {
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

   /**
    * Parse the information contained in the specified {@link ServiceDescription} and update the internal state
    *
    * @param forceRefresh       whether we should force the parsing of the information (currently only used to force updating the registration metadata)
    * @param serviceDescription the ServiceDescription to parse the metadata from
    * @param result             the RefreshResult that was created by the refresh call that trigger the parsing of the ServiceDescription metadata
    * @return the RefreshResult that was specified as a parameter, modified as needed
    * @throws PortletInvokerException
    */
   private RefreshResult refreshInfo(boolean forceRefresh, ServiceDescription serviceDescription, RefreshResult result)
      throws PortletInvokerException
   {
      // do we need to call initCookie or not?
      requiresInitCookie = serviceDescription.getRequiresInitCookie();
      log.debug("Requires initCookie: " + requiresInitCookie);

      // supported options
      final List<String> supportedOptions = serviceDescription.getSupportedOptions();
      if (ParameterValidation.existsAndIsNotEmpty(supportedOptions))
      {
         this.supportedOptions = new HashSet<String>(supportedOptions);
      }

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
      }
      else
      {
         log.debug("Registration not required");
         persistentRegistrationInfo = new RegistrationInfo(this, false);
         extractOfferedPortlets(serviceDescription);
      }

      modifyNow();
      return result;
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

   /**
    * Extracts a map of offered Portlet objects from ServiceDescription
    *
    * @param sd the service description to extract portlets from
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
    * Create a custom {@link Portlet} implementation that also contains WSRP-specific information from the specified PortletDescription.
    *
    * @param portletDescription the PortletDescription to extract information from
    * @return a custom {@link Portlet} implementation that also contains WSRP-specific information
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
            "the list of offered portlets for producer " + getId());
      }
      else
      {
         if (info.isHasUserSpecificState())
         {
            log.debug("Portlet '" + portletHandle + "' will store persistent state for each user.");
         }

         wsrpPortlet = new WSRPPortlet(PortletContext.createPortletContext(portletHandle, false), info);

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

   /**
    * Retrieve (possibly from cache) the {@link Portlet} associated with the specified {@link PortletContext}.
    *
    * @param portletContext the PortletContext identifying the portlet to be retrieved
    * @return the {@link Portlet} associated with the specified {@link PortletContext}
    * @throws PortletInvokerException if no portlet with the specified PortletContext could be found or if an error happened while attempting to retrieve the portlet information
    *                                 from the remote producer if the portlet couldn't be resolved from the local cache
    */
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

   public Map<String, Portlet> getProducerOffereedPortletMap() throws PortletInvokerException
   {
      refresh(false);
      return popsMap;
   }

   public Map<String, Portlet> getAllPortletsMap() throws PortletInvokerException
   {
      // calling getNumberOfPortlets refreshes the information if needed so no need to redo it here
      Map<String, Portlet> all = new LinkedHashMap<String, Portlet>(getNumberOfPortlets());

      if (popsMap != null)
      {
         all.putAll(popsMap);
      }

      if (ccpsMap != null)
      {
         all.putAll(ccpsMap);
      }

      return all;
   }

   public int getNumberOfPortlets() throws PortletInvokerException
   {
      refresh(false);

      int portletNb = popsMap != null ? popsMap.size() : 0;
      portletNb = portletNb + (ccpsMap != null ? ccpsMap.size() : 0);

      return portletNb;
   }

   // Cache support ****************************************************************************************************

   private boolean useCache()
   {
      return persistentExpirationCacheSeconds != null && persistentExpirationCacheSeconds > 0;
   }

   private void resetCacheTimerIfNeeded()
   {
      expirationTimeMillis = nowForCache() + (getSafeExpirationCacheSeconds() * 1000);
   }

   /**
    * Determines whether the metadata cache is expired and therefore, if a refresh from the producer might be required to obtain fresher information.
    *
    * @return <code>true</code> if the cache is expired, <code>false</code> otherwise
    * @since 2.6
    */
   private boolean isCacheExpired()
   {
      boolean result = !useCache() || nowForCache() > expirationTimeMillis || popsMap == null
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
      if (modifyNowIfNeeded(persistentExpirationCacheSeconds, expirationCacheSeconds))
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
            expirationTimeMillis = nowForCache();
         }
      }
   }

   /**
    * Returns the cache expiration duration in seconds as a positive value or zero so that it's safe to use in cache
    * expiration time computations.
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
         final Holder<List<ExtensionDescription>> extensionDescriptions = new Holder<List<ExtensionDescription>>();
         final Holder<List<Extension>> extensions = new Holder<List<Extension>>();

         // invocation
         persistentEndpointInfo.getServiceDescriptionService().getServiceDescription(
            asUnregistered ? null : getRegistrationContext(),
            WSRPConstants.getDefaultLocales(), // todo: deal with locales better
            null, // todo: provide a way to only request info on some portlets?
            UserAccess.getUserContext(),
            requiresRegistration,
            offeredPortlets,
            userCategoryDescriptions,
            extensionDescriptions,
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
            extensions);

         // TODO: fix-me
         serviceDescription = WSRPTypeFactory.createServiceDescription(requiresRegistration.value);
         serviceDescription.setRegistrationPropertyDescription(registrationPropertyDescription.value);
         serviceDescription.setRequiresInitCookie(requiresInitCookie.value);
         serviceDescription.setResourceList(resourceList.value);
         serviceDescription.setSchemaType(schemaTypes.value);
         serviceDescription.setExportDescription(exportDescription.value);
         serviceDescription.setMayReturnRegistrationState(mayReturnRegistrationState.value);

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
         if (ParameterValidation.existsAndIsNotEmpty(eventDescriptions.value))
         {
            serviceDescription.getEventDescriptions().addAll(eventDescriptions.value);
         }
         if (ParameterValidation.existsAndIsNotEmpty(extensionDescriptions.value))
         {
            serviceDescription.getExtensionDescriptions().addAll(extensionDescriptions.value);
         }
         if (ParameterValidation.existsAndIsNotEmpty(extensions.value))
         {
            serviceDescription.getExtensions().addAll(extensions.value);
         }
         if (ParameterValidation.existsAndIsNotEmpty(supportedOptions.value))
         {
            serviceDescription.getSupportedOptions().addAll(supportedOptions.value);
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
         + getId() + ", please see the logs for more information. ", cause == null ? e : cause);
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
      modifyNow();
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
    * @param serviceDescription an optional service description containing the information required by the producer to properly register with it
    * @param forceRefresh       whether to force a refresh (regardless of cache status)
    * @return a RefreshResult containing the status of the operation
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
            RefreshResult result = persistentRegistrationInfo.refresh(serviceDescription, getId(), true, forceRefresh, false);
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
                     String msg = "Consumer with id '" + getId() + "' successfully registered with handle: '"
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
            log.info("Consumer with id '" + getId() + "' deregistered.");
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
         throw new IllegalStateException("Cannot deregister producer '" + getId() + "' as it's not registered");
      }

   }

   public void modifyRegistration() throws PortletInvokerException
   {
      modifyRegistration(false);
   }

   public void modifyRegistration(boolean force) throws PortletInvokerException
   {
      try
      {
         internalModifyRegistration(force);
      }
      finally
      {
         registry.updateProducerInfo(this);
      }
   }

   private void internalModifyRegistration(boolean force) throws PortletInvokerException
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

               // update state
               persistentRegistrationInfo.setRegistrationState(registrationState.value);

               log.info("Consumer with id '" + getId() + "' sucessfully modified its registration.");

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
         throw new IllegalStateException("Cannot modify registration for producer '" + getId()
            + "' as it's not registered");
      }
   }

   private void invalidateCache()
   {
      if (useCache())
      {
         expirationTimeMillis = nowForCache();
      }
   }

   private static long nowForCache()
   {
      return System.currentTimeMillis();
   }

   private RefreshResult internalRefreshRegistration(ServiceDescription serviceDescription, boolean mergeWithLocalInfo, boolean forceRefresh, boolean forceCheckOfExtraProps) throws PortletInvokerException
   {
      RefreshResult result =
         persistentRegistrationInfo.refresh(serviceDescription, getId(), mergeWithLocalInfo, forceRefresh, forceCheckOfExtraProps);

      log.debug("Refreshed registration information for consumer with id '" + getId() + "'");

      return result;
   }

   public boolean isRefreshNeeded(boolean considerCache)
   {
      boolean result = (considerCache && isCacheExpired())
         || persistentRegistrationInfo.isRefreshNeeded()
         || persistentEndpointInfo.isRefreshNeeded();
      if (result)
      {
         log.debug("Refresh needed for producer '" + getId() + "'");
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

   public Collection<String> getSupportedOptions()
   {
      return Collections.unmodifiableSet(supportedOptions);
   }

   /**
    * Public for tests
    *
    * @param option a valid WSRP 2 option String, see {@link WSRP2Constants}' OPTIONS_* fields for valid values.
    */
   public void setSupportedOption(String option)
   {
      if (WSRP2Constants.OPTIONS_COPYPORTLETS.equals(option) || WSRP2Constants.OPTIONS_EVENTS.equals(option)
         || WSRP2Constants.OPTIONS_EXPORT.equals(option) || WSRP2Constants.OPTIONS_IMPORT.equals(option)
         || WSRP2Constants.OPTIONS_LEASING.equals(option))
      {
         if (supportedOptions.isEmpty())
         {
            supportedOptions = new HashSet<String>(5);
         }
         supportedOptions.add(option);
      }
      else
      {
         throw new IllegalArgumentException("Invalid option: " + option);
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
