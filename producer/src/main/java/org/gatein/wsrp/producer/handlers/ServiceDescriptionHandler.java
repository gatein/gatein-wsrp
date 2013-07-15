/*
 * JBoss, a division of Red Hat
 * Copyright 2011, Red Hat Middleware, LLC, and individual
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

package org.gatein.wsrp.producer.handlers;

import org.gatein.common.net.media.MediaType;
import org.gatein.common.util.ParameterValidation;
import org.gatein.pc.api.Portlet;
import org.gatein.pc.api.PortletInvokerException;
import org.gatein.pc.api.TransportGuarantee;
import org.gatein.pc.api.info.CapabilitiesInfo;
import org.gatein.pc.api.info.EventInfo;
import org.gatein.pc.api.info.EventingInfo;
import org.gatein.pc.api.info.MetaInfo;
import org.gatein.pc.api.info.ModeInfo;
import org.gatein.pc.api.info.NavigationInfo;
import org.gatein.pc.api.info.ParameterInfo;
import org.gatein.pc.api.info.PortletInfo;
import org.gatein.pc.api.info.RuntimeOptionInfo;
import org.gatein.pc.api.info.SecurityInfo;
import org.gatein.pc.api.info.WindowStateInfo;
import org.gatein.pc.portlet.container.managed.LifeCycleStatus;
import org.gatein.pc.portlet.container.managed.ManagedObject;
import org.gatein.pc.portlet.container.managed.ManagedObjectEvent;
import org.gatein.pc.portlet.container.managed.ManagedObjectLifeCycleEvent;
import org.gatein.pc.portlet.container.managed.ManagedObjectRegistryEvent;
import org.gatein.pc.portlet.container.managed.ManagedObjectRegistryEventListener;
import org.gatein.pc.portlet.container.managed.ManagedPortletContainer;
import org.gatein.registration.Registration;
import org.gatein.registration.RegistrationLocal;
import org.gatein.wsrp.SupportsLastModified;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.WSRPUtils;
import org.gatein.wsrp.producer.ServiceDescriptionInterface;
import org.gatein.wsrp.producer.Utils;
import org.gatein.wsrp.producer.WSRPProducerImpl;
import org.gatein.wsrp.producer.config.ProducerRegistrationRequirements;
import org.gatein.wsrp.registration.RegistrationPropertyDescription;
import org.gatein.wsrp.spec.v2.WSRP2Constants;
import org.gatein.wsrp.spec.v2.WSRP2ExceptionFactory;
import org.oasis.wsrp.v2.CookieProtocol;
import org.oasis.wsrp.v2.EventDescription;
import org.oasis.wsrp.v2.GetServiceDescription;
import org.oasis.wsrp.v2.InvalidHandle;
import org.oasis.wsrp.v2.InvalidRegistration;
import org.oasis.wsrp.v2.MarkupType;
import org.oasis.wsrp.v2.ModelDescription;
import org.oasis.wsrp.v2.ModifyRegistrationRequired;
import org.oasis.wsrp.v2.OperationFailed;
import org.oasis.wsrp.v2.ParameterDescription;
import org.oasis.wsrp.v2.PortletContext;
import org.oasis.wsrp.v2.PortletDescription;
import org.oasis.wsrp.v2.RegistrationContext;
import org.oasis.wsrp.v2.ResourceSuspended;
import org.oasis.wsrp.v2.ServiceDescription;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Handles service description calls on behalf of the producer. Implements {@link ManagedObjectRegistryEventListener} to listen to portlet deployment operations to be able to
 * update the service description accordingly.
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 12017 $
 * @since 2.4
 */
public class ServiceDescriptionHandler extends ServiceHandler implements ServiceDescriptionInterface, ManagedObjectRegistryEventListener
{
   // JBPORTAL-1220: force call to initCookie... Required so that BEA version < 9.2 will behave properly as a Consumer
   private static final CookieProtocol BEA_8_CONSUMER_FIX = CookieProtocol.PER_USER;
   /** Stores service description so that we don't constantly need to regenerate it on each call. */
   private ServiceDescriptionInfo serviceDescription;

   private static final List<String> OPTIONS = new ArrayList<String>(5);

   static
   {
      OPTIONS.add(WSRP2Constants.OPTIONS_EVENTS);
      OPTIONS.add(WSRP2Constants.OPTIONS_IMPORT);
      OPTIONS.add(WSRP2Constants.OPTIONS_EXPORT);
      OPTIONS.add(WSRP2Constants.OPTIONS_COPYPORTLETS);
   }

   public ServiceDescriptionHandler(WSRPProducerImpl producer)
   {
      super(producer);
      reset();
   }

   public ServiceDescription getServiceDescription(GetServiceDescription gs)
      throws InvalidRegistration, ModifyRegistrationRequired, OperationFailed, ResourceSuspended
   {
      WSRP2ExceptionFactory.throwOperationFailedIfValueIsMissing(gs, "GetServiceDescription");

      try
      {
         // if a RegistrationContext is provided, we need to validate the registration information
         RegistrationContext registrationContext = gs.getRegistrationContext();
         Registration registration = null;
         if (registrationContext != null)
         {
            registration = producer.getRegistrationOrFailIfInvalid(registrationContext);
            RegistrationLocal.setRegistration(registration);
         }

         ProducerRegistrationRequirements requirements = producer.getProducerRegistrationRequirements();

         //update the registration properties with the registration requirements
         serviceDescription.updateRegistrationProperties(requirements);

         // if we don't have registration information but a registration is required, send registration props information
         boolean needsRegistrationProperties = registration == null && requirements.isRegistrationRequired();

         // if we allow sending portlet descriptions even when not registered
         boolean needsPortletDescriptions = !(registration == null && requirements.isRegistrationRequired()
            && requirements.isRegistrationRequiredForFullDescription());

         final List<String> portletHandles = WSRPUtils.replaceByEmptyListIfNeeded(gs.getPortletHandles());
         final List<String> desiredLocales = WSRPUtils.replaceByEmptyListIfNeeded(gs.getDesiredLocales());
         return serviceDescription.getServiceDescription(needsRegistrationProperties, needsPortletDescriptions, portletHandles, desiredLocales);
      }
      finally
      {
         RegistrationLocal.setRegistration(null);
      }
   }

   /**
    * Creates a PortletDescription based on the user desired locales (ordered according to user preferences) for the
    * specified component.
    *
    * @param portletContext the PortletContext of the portlet for which a PortletDescription is needed
    * @param desiredLocales the user desired locales (ordered according to user preferences) to use for the description
    * @return a PortletDescription describing the specified portlet
    */
   public PortletDescription getPortletDescription(PortletContext portletContext, List<String> desiredLocales, Registration registration) throws InvalidHandle, OperationFailed
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(portletContext, "portlet context");

      final PortletDescription description = serviceDescription.getPortletDescription(portletContext, desiredLocales, registration);
      if (description == null)
      {
         throw WSRP2ExceptionFactory.throwWSException(InvalidHandle.class, "Couldn't find portlet '" + portletContext.getPortletHandle() + "'. Check that it's properly deployed.", null);
      }
      return description;
   }

   /**
    * Listens to portlet deployment events and updates the service description accordingly.
    *
    * @param event
    */
   public void onEvent(ManagedObjectRegistryEvent event)
   {
      if (event instanceof ManagedObjectEvent)
      {
         ManagedObjectEvent managedObjectEvent = (ManagedObjectEvent)event;
         ManagedObject managedObject = managedObjectEvent.getManagedObject();

         // we're only interested in portlet events
         if (managedObject instanceof ManagedPortletContainer)
         {
            ManagedPortletContainer portletContainer = (ManagedPortletContainer)managedObject;
            String applicationId = portletContainer.getManagedPortletApplication().getId();
            String containerId = portletContainer.getId();

            org.gatein.pc.api.PortletContext pc = org.gatein.pc.api.PortletContext.createPortletContext(applicationId, containerId);

            // and more specifically, their lifecycle eventds
            if (managedObjectEvent instanceof ManagedObjectLifeCycleEvent)
            {
               ManagedObjectLifeCycleEvent lifeCycleEvent = (ManagedObjectLifeCycleEvent)managedObjectEvent;
               LifeCycleStatus status = lifeCycleEvent.getStatus();
               if (LifeCycleStatus.STARTED.equals(status))
               {
                  // if the portlet started, add it to the service description
                  final PortletInfo info = portletContainer.getInfo();
                  // but only if it's remotable
                  if (isRemotable(info.getRuntimeOptionsInfo()))
                  {
                     serviceDescription.addPortletDescription(pc, info);
                  }
               }
               else
               {
                  // otherwise, remove the description of the portlet
                  serviceDescription.removePortletDescription(pc);
               }
            }

         }
      }
   }

   /**
    * Retrieves the WSRP-friendly names for the specified Locales.
    *
    * @param locales the Locales we want to get the WSRP-friendly versions
    * @return
    */
   private static List<String> getLocaleNamesFrom(Collection<Locale> locales)
   {
      if (locales == null || locales.isEmpty())
      {
         return null;
      }

      List<String> localeNames = new ArrayList<String>(locales.size());
      for (Locale locale : locales)
      {
         localeNames.add(WSRPUtils.toString(locale));
      }
      return localeNames;
   }

   private static List<String> getWindowStateNamesFrom(Collection<WindowStateInfo> windowStates)
   {
      List<String> result = new ArrayList<String>(windowStates.size());
      for (WindowStateInfo windowStateInfo : windowStates)
      {
         result.add(WSRPUtils.convertJSR168WindowStateNameToWSRPName(windowStateInfo.getWindowStateName()));
      }
      return result;
   }

   private static List<String> getModeNamesFrom(Collection<ModeInfo> modes)
   {
      List<String> result = new ArrayList<String>(modes.size());
      for (ModeInfo modeInfo : modes)
      {
         result.add(WSRPUtils.convertJSR168PortletModeNameToWSRPName(modeInfo.getModeName()));
      }
      return result;
   }

   /** Resets the service description to its original, pristine state */
   public void reset()
   {
      serviceDescription = new ServiceDescriptionInfo();
   }

   /**
    * Whether the specified runtime options contains the remotable option.
    *
    * @param runtimeOptions
    * @return
    */
   public boolean isRemotable(Map<String, RuntimeOptionInfo> runtimeOptions)
   {
      RuntimeOptionInfo runtimeOptionInfo = runtimeOptions.get(RuntimeOptionInfo.REMOTABLE_RUNTIME_OPTION);

      return runtimeOptionInfo != null && "true".equals(runtimeOptionInfo.getValues().get(0));
   }

   /** Stores all service description related metadata so that we don't constantly need to re-generate it on each call. */
   private class ServiceDescriptionInfo
   {
      /** When were we last generated? */
      private long lastGenerated;
      /** Event descriptions */
      private Map<QName, EventDescription> eventDescriptions;
      /**
       * Since events can be declared by several portlets, we need to have a reference counting mechanism on event descriptions so that we can ensure that, when a portlet is
       * removed, we only remove the related event description if and only if no other portlets reference it.
       */
      private Map<QName, Integer> eventReferenceCount;
      /** Portlet descriptions */
      private Map<String, PortletDescriptionInfo> portletDescriptions;
      /** Registration properties */
      private ModelDescription registrationProperties;
      /** Whether we've already been initialized or not */
      private boolean initialized = false;
      /** Does the associated producer require registration? */
      private boolean requireRegistrations;

      private ServiceDescriptionInfo()
      {
         reset();
      }

      /** Resets all metadata. */
      void reset()
      {
         lastGenerated = 0;
         eventDescriptions = new HashMap<QName, EventDescription>(37);
         eventReferenceCount = new HashMap<QName, Integer>(37);
         portletDescriptions = new HashMap<String, PortletDescriptionInfo>(37);
         registrationProperties = null;
         initialized = false;
         requireRegistrations = false;
      }

      /**
       * Updates the held registration properties based on the new specified producer registration requirements.
       *
       * @param requirements the newly specified registration requirements from which we want to update
       */
      private void updateRegistrationProperties(ProducerRegistrationRequirements requirements)
      {
         // only update our information if the new requirements are posterior to our last modification, since, presumably, we updated them at that time
         long lastModified = requirements.getLastModified();
         if (lastModified > lastGenerated)
         {
            if (log.isDebugEnabled())
            {
               log.debug("Re-generating registration properties information for service description.");
            }

            // do not create a ModelDescription if there is no registration properties
            Map<QName, RegistrationPropertyDescription> info = requirements.getRegistrationProperties();
            if (ParameterValidation.existsAndIsNotEmpty(info))
            {
               registrationProperties = Utils.convertRegistrationPropertiesToModelDescription(info);
            }
            else
            {
               registrationProperties = null;
            }

            // update need to register
            requireRegistrations = requirements.isRegistrationRequired();

            // we just got re-generated
            lastGenerated = SupportsLastModified.now();
         }
      }

      /** Updates portlet descriptions from the set of remotable portlets known by the associated producer. */
      private void updatePortletDescriptions()
      {
         try
         {
            Set<Portlet> portlets = producer.getRemotablePortlets();
            if (ParameterValidation.existsAndIsNotEmpty(portlets))
            {
               for (Portlet portlet : portlets)
               {
                  addPortletDescription(portlet.getContext(), portlet.getInfo());
               }
            }

            initialized = true;
         }
         catch (PortletInvokerException e)
         {
            log.warn("Couldn't get remotable portlets", e);
         }
      }

      /**
       * Retrieves a ServiceDescription instance ready to be sent to the consumer with the specified information.
       *
       * @param needsRegistrationProperties do we want to include registration properties?
       * @param needsPortletDescriptions    do we want to include portlet descriptions?
       * @param portletHandles              list of portlet handles that we only want to include in the service description
       * @param desiredLocales              desired locales for which the service description should be adapted along a best effort policy
       * @return a ServiceDescription instance ready to be sent to the consumer with the specified information.
       */
      private ServiceDescription getServiceDescription(boolean needsRegistrationProperties, boolean needsPortletDescriptions, List<String> portletHandles, List<String> desiredLocales)
      {
         // initialize if needed
         initIfNeeded();

         // only add registration properties if we asked for them
         ModelDescription registrationProperties = needsRegistrationProperties ? this.registrationProperties : null;

         // set the service description details
         ServiceDescription serviceDescription = WSRPTypeFactory.createServiceDescription(false);
         serviceDescription.setRequiresInitCookie(BEA_8_CONSUMER_FIX);
         serviceDescription.getSupportedOptions().addAll(OPTIONS);
         serviceDescription.setRegistrationPropertyDescription(registrationProperties);
         serviceDescription.setRequiresRegistration(requireRegistrations);

         // init supported locales. Note that this doesn't mean that all portlets support all these languages but rather that at least one portlet supports at least one of these languages.
         final Set<String> knownPortletHandles = portletDescriptions.keySet();
         Set<String> supportedLocales = new HashSet<String>(knownPortletHandles.size() * 2);

         // if we asked for portlet decriptions, add them to the service description we will return
         Collection<PortletDescription> portlets;
         if (needsPortletDescriptions)
         {
            // if we don't have a list of portlet handles, select all of them
            if (!ParameterValidation.existsAndIsNotEmpty(portletHandles))
            {
               portletHandles = new ArrayList<String>(knownPortletHandles);
            }

            // for each selected portlet
            portlets = new ArrayList<PortletDescription>(portletHandles.size());
            for (String handle : portletHandles)
            {
               // retrieve the associated description
               PortletDescriptionInfo descriptionInfo = portletDescriptions.get(handle);
               if (descriptionInfo != null)
               {
                  // add the languages that the portlet supports to the set of supported languages
                  supportedLocales.addAll(descriptionInfo.getSupportedLanguages());
                  // and add the best-effort localized description for this portlet based on the locales the consumer asked for
                  portlets.add(descriptionInfo.getBestDescriptionFor(desiredLocales));
               }
            }
            serviceDescription.getOfferedPortlets().addAll(portlets);
         }
         serviceDescription.getLocales().addAll(supportedLocales);

         // events
         Collection<EventDescription> events = eventDescriptions.values();
         serviceDescription.getEventDescriptions().addAll(events);

         return serviceDescription;
      }

      /** Initializes this service description if we were not already */
      private void initIfNeeded()
      {
         if (!initialized)
         {
            updatePortletDescriptions();
         }
      }

      /**
       * Adds the specified event metadata in the specified locale
       *
       * @param info
       * @param locale
       */
      private void addEventInfo(EventInfo info, Locale locale)
      {
         QName name = info.getName();
         if (!eventDescriptions.containsKey(name))
         {
            EventDescription desc = WSRPTypeFactory.createEventDescription(name);
            desc.setDescription(Utils.convertToWSRPLocalizedString(info.getDescription(), locale));
            desc.setLabel(Utils.convertToWSRPLocalizedString(info.getDisplayName(), locale));
            Collection<QName> aliases = info.getAliases();
            if (ParameterValidation.existsAndIsNotEmpty(aliases))
            {
               desc.getAliases().addAll(aliases);
            }
            // todo: deal with type info...
            eventDescriptions.put(name, desc);
            eventReferenceCount.put(name, 1);
         }
         else
         {
            Integer current = eventReferenceCount.get(name);
            eventReferenceCount.put(name, current + 1); // increase reference count
         }
      }

      /**
       * Removes the even information associated with the specified QName
       *
       * @param name the name of the event which information we want to remove
       */
      private void removeEvent(QName name)
      {
         // retrieve the reference count for this event
         Integer current = eventReferenceCount.get(name);
         if (current != null)
         {
            if (current == 1)
            {
               // only remove the even description if we only have one reference to it left
               eventDescriptions.remove(name);
               eventReferenceCount.remove(name);
            }
            else
            {
               // otherwise, simply decrease the reference count for that event
               eventReferenceCount.put(name, current - 1);
            }
         }
      }

      private void addPortletDescription(org.gatein.pc.api.PortletContext context, PortletInfo info)
      {
         String handle = context.getId();
         PortletDescriptionInfo desc = createPortletDescription(info, handle);

         portletDescriptions.put(handle, desc);
      }

      /**
       * Creates the portlet description metadata associated with the specified portlet.
       *
       * @param info   the portlet metadata from the portlet container
       * @param handle the portlet handle of the portlet for which we want to create the metadata
       * @return the metadata for the portlet
       */
      private PortletDescriptionInfo createPortletDescription(PortletInfo info, String handle)
      {
         if (log.isDebugEnabled())
         {
            log.debug("Constructing portlet description for: " + handle);
         }

         // supported MIME types, modes, window states and locales
         CapabilitiesInfo capInfo = info.getCapabilities();
         Collection<MediaType> allMediaTypes = capInfo.getMediaTypes();
         List<MarkupType> markupTypes = new ArrayList<MarkupType>(allMediaTypes.size());
         for (MediaType mediaType : allMediaTypes)
         {
            MarkupType markupType = WSRPTypeFactory.createMarkupType(mediaType.getValue(),
               getModeNamesFrom(capInfo.getModes(mediaType)), getWindowStateNamesFrom(capInfo.getWindowStates(mediaType)),
               getLocaleNamesFrom(capInfo.getLocales(mediaType)));
            markupTypes.add(markupType);
         }

         // prepare languages for which we will generated a portlet description
         Set<Locale> supportedLocales = info.getCapabilities().getAllLocales();
         List<String> supportedLanguages;
         if (supportedLocales.size() == 0)
         {
            // if the portlet doesn't specify supported languages, use English as failback per PLT.25.8.1
            supportedLocales = Collections.singleton(Locale.ENGLISH);
            supportedLanguages = Collections.singletonList("en");
         }
         else
         {
            supportedLanguages = WSRPUtils.convertLocalesToRFC3066LanguageTags(new ArrayList<Locale>(supportedLocales));
         }

         PortletDescriptionInfo descriptionInfo = portletDescriptions.get(handle);
         if (descriptionInfo == null)
         {
            descriptionInfo = new PortletDescriptionInfo(supportedLanguages);
            portletDescriptions.put(handle, descriptionInfo);
         }

         // iterate over locales and create a portlet description for each
         MetaInfo metaInfo = info.getMeta();
         for (Locale localeMatch : supportedLocales)
         {
            PortletDescription desc = WSRPTypeFactory.createPortletDescription(handle, markupTypes);

            // group ID
            desc.setGroupID(info.getApplicationName());

            // description
            desc.setDescription(Utils.convertToWSRPLocalizedString(metaInfo.getMetaValue(MetaInfo.DESCRIPTION), localeMatch));

            // short title
            desc.setShortTitle(Utils.convertToWSRPLocalizedString(metaInfo.getMetaValue(MetaInfo.SHORT_TITLE), localeMatch));

            // title
            desc.setTitle(Utils.convertToWSRPLocalizedString(metaInfo.getMetaValue(MetaInfo.TITLE), localeMatch));

            // display name
            desc.setDisplayName(Utils.convertToWSRPLocalizedString(metaInfo.getMetaValue(MetaInfo.DISPLAY_NAME), localeMatch));

            // keywords
            // metaInfo contains comma-separated keywords: we need to extract them into a list
            org.oasis.wsrp.v2.LocalizedString concatenatedKeywords = Utils.convertToWSRPLocalizedString(metaInfo.getMetaValue(MetaInfo.KEYWORDS), localeMatch);
            if (concatenatedKeywords != null)
            {
               String commaSeparatedKeywords = concatenatedKeywords.getValue();
               if (commaSeparatedKeywords != null && commaSeparatedKeywords.length() > 0)
               {
                  String lang = concatenatedKeywords.getLang();
                  String[] keywordArray = commaSeparatedKeywords.split(",");
                  for (String keyword : keywordArray)
                  {
                     // todo: fix resource name
                     desc.getKeywords().add(WSRPTypeFactory.createLocalizedString(lang, concatenatedKeywords.getResourceName(), keyword.trim()));
                  }
               }
            }

            // events
            EventingInfo eventsInfo = info.getEventing();
            if (eventsInfo != null)
            {
               // produced events are mapped to published events in wsrp
               Map<QName, ? extends EventInfo> producedEvents = eventsInfo.getProducedEvents();
               if (ParameterValidation.existsAndIsNotEmpty(producedEvents))
               {
                  List<QName> publishedEvents = desc.getPublishedEvents();
                  for (Map.Entry<QName, ? extends EventInfo> entry : producedEvents.entrySet())
                  {
                     publishedEvents.add(entry.getKey());
                     addEventInfo(entry.getValue(), localeMatch);
                  }
               }

               // consumed events -> handled events in wsrp
               Map<QName, ? extends EventInfo> consumedEvents = eventsInfo.getConsumedEvents();
               if (ParameterValidation.existsAndIsNotEmpty(consumedEvents))
               {
                  List<QName> handledEvents = desc.getHandledEvents();
                  for (Map.Entry<QName, ? extends EventInfo> entry : consumedEvents.entrySet())
                  {
                     handledEvents.add(entry.getKey());
                     addEventInfo(entry.getValue(), localeMatch);
                  }
               }
            }

            // public parameters
            NavigationInfo navigationInfo = info.getNavigation();
            if (navigationInfo != null)
            {
               Collection<? extends ParameterInfo> parameterInfos = navigationInfo.getPublicParameters();
               if (ParameterValidation.existsAndIsNotEmpty(parameterInfos))
               {
                  List<ParameterDescription> publicValueDescriptions = desc.getNavigationalPublicValueDescriptions();
                  for (ParameterInfo parameterInfo : parameterInfos)
                  {
                     String id = parameterInfo.getId();
                     ParameterDescription paramDesc = WSRPTypeFactory.createParameterDescription(id);
                     paramDesc.setDescription(Utils.convertToWSRPLocalizedString(parameterInfo.getDescription(), localeMatch));
                     paramDesc.setLabel(WSRPTypeFactory.createLocalizedString(id));
                     List<QName> names = paramDesc.getNames();
                     names.add(parameterInfo.getName());
                     Collection<QName> aliases = parameterInfo.getAliases();
                     if (ParameterValidation.existsAndIsNotEmpty(aliases))
                     {
                        names.addAll(aliases);
                     }

                     publicValueDescriptions.add(paramDesc);
                  }
               }
            }

            // security
            SecurityInfo secInfo = info.getSecurity();
            if (secInfo.containsTransportGuarantee(TransportGuarantee.INTEGRAL)
               || secInfo.containsTransportGuarantee(TransportGuarantee.CONFIDENTIAL))
            {
               desc.setOnlySecure(true);
            }

            // add the portlet description to the appropriate PortletDescriptionInfo
            descriptionInfo.addDescriptionFor(WSRPUtils.toString(localeMatch), desc);
         }

         /* todo:
         *[O]ID portletID
         *[O]string userCategories[]
         *[O]string userProfileItems[]
         *[O]string portletManagedModes[]
         *[O]boolean usesMethodGet
         *[O]boolean defaultMarkupSecure
         *[O]boolean userContextStoredInSession
         *[O]boolean templatesStoredInSession
         *[O]boolean hasUserSpecificState
         *[O]boolean doesUrlTemplateProcessing
         *[O]boolean mayReturnPortletState
         *[O]Extension extensions[]
         */

         return descriptionInfo;
      }

      /**
       * Retrieves the PortletDescription associated to the specified PortletContext with metadata using the desired locales, scoped to the specified Registration.
       *
       * @param context        the PortletContext identifying the portlet we want to retrieve the description of
       * @param desiredLocales the locales from which we are ready to accept a description
       * @param registration   the Registration associated with the calling consumer
       * @return the description associated to the specified portlet or <code>null</code> if no such portlet exists or if the {@link org.gatein.registration.RegistrationPolicy}
       * associated with the producer doesn't allow access to the portlet description for the specified registration
       */
      public PortletDescription getPortletDescription(PortletContext context, List<String> desiredLocales, Registration registration)
      {
         initIfNeeded();

         org.gatein.pc.api.PortletContext pcContext = WSRPUtils.convertToPortalPortletContext(context);

         // does the specified registration allow access to the specified portlet?
         if (producer.getRegistrationManager().getPolicy().allowAccessTo(pcContext, registration, "getPortletDescription"))
         {
            PortletDescription description = getPortletDescription(context.getPortletHandle(), desiredLocales);

            // the producer doesn't know of the portlet so it's not a producer-offered portlet
            if (description == null)
            {
               // however, it might be a clone so check if the registration knows of a clone with that portlet context
               if (registration.knows(pcContext))
               {
                  try
                  {
                     // clones have the same metadata as original portlets so retrieve initial context from portlet info and get description from it
                     Portlet portlet = producer.getPortletWith(pcContext, registration);
                     PortletInfo info = portlet.getInfo();
                     org.gatein.pc.api.PortletContext original = org.gatein.pc.api.PortletContext.createPortletContext(info.getApplicationName(), info.getName());
                     return getPortletDescription(original.getId(), desiredLocales);
                  }
                  catch (Exception e)
                  {
                     log.debug("Couldn't retrieve portlet " + pcContext, e);
                     return null;
                  }
               }
            }
            return description;
         }
         else
         {
            return null;
         }
      }

      private PortletDescription getPortletDescription(final String portletHandle, List<String> desiredLocales)
      {
         PortletDescriptionInfo descriptionInfo = portletDescriptions.get(portletHandle);
         if (descriptionInfo != null)
         {
            return descriptionInfo.getBestDescriptionFor(desiredLocales);
         }
         else
         {
            return null;
         }
      }

      /**
       * Removes the description for the portlet identified by the specified context
       *
       * @param pc
       */
      public void removePortletDescription(org.gatein.pc.api.PortletContext pc)
      {
         String handle = WSRPUtils.convertToWSRPPortletContext(pc).getPortletHandle();

         PortletDescription description = getPortletDescription(handle, null);
         if (description != null)
         {
            // remove associated events
            for (QName event : description.getHandledEvents())
            {
               removeEvent(event);
            }
            for (QName event : description.getPublishedEvents())
            {
               removeEvent(event);
            }

            portletDescriptions.remove(handle);
         }
      }

      /**
       * Records the description for each supported languages for a given portlet. These are generated only once when the associated portlets are added so that we only need to
       * filter the proper information when we return a service description to the consumer.
       */
      private class PortletDescriptionInfo
      {
         /** Associates a language to a portlet description */
         private Map<String, PortletDescription> languageToDescription;

         private PortletDescriptionInfo(List<String> supportedLanguages)
         {
            languageToDescription = new HashMap<String, PortletDescription>(supportedLanguages.size());
            for (String supportedLanguage : supportedLanguages)
            {
               languageToDescription.put(supportedLanguage, null);
            }
         }

         /**
          * Retrieves a Set of all supported languages by the portlet.
          *
          * @return a Set of languages by the associated portlet
          */
         public Set<String> getSupportedLanguages()
         {
            return languageToDescription.keySet();
         }

         /**
          * Retrieves the best description for the specified languages based on what the portlet supports.
          *
          * @param desiredLanguages a List of ordered (most desired first) languages for which we're willing to accept a description for the associated portlet
          * @return
          */
         public PortletDescription getBestDescriptionFor(List<String> desiredLanguages)
         {
            String language = null;

            Set<String> supportedLanguages = getSupportedLanguages();
            if (desiredLanguages != null && !desiredLanguages.isEmpty())
            {
               // check first if we have an exact match
               for (String languageTag : desiredLanguages)
               {
                  if (supportedLanguages.contains(languageTag))
                  {
                     language = languageTag;
                     break; // exit loop as soon as we've found a match
                  }
               }

               // if we haven't found an exact match, check if we can find a partial match based on country
               if (language == null)
               {
                  for (String desiredLanguage : desiredLanguages)
                  {
                     for (String supportedLanguage : supportedLanguages)
                     {
                        if (supportedLanguage.startsWith(desiredLanguage))
                        {
                           language = supportedLanguage;
                           break; // exit loop as soon as we've found a match
                        }
                     }
                  }
               }
            }

            if (language == null)
            {
               // if we still haven't determined a language, use the first available one
               for (String supportedLanguage : supportedLanguages)
               {
                  language = supportedLanguage;
                  break;
               }
            }

            return languageToDescription.get(language);
         }

         /**
          * Adds the specified portlet description for the specified language.
          *
          * @param language the language for which we're adding a description
          * @param desc     the portlet description to associate to the specified language
          */
         public void addDescriptionFor(String language, PortletDescription desc)
         {
            languageToDescription.put(language, desc);
         }
      }
   }
}
