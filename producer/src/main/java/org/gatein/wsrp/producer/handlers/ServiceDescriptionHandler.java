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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 12017 $
 * @since 2.4
 */
public class ServiceDescriptionHandler extends ServiceHandler implements ServiceDescriptionInterface, ManagedObjectRegistryEventListener
{
   // JBPORTAL-1220: force call to initCookie... Required so that BEA version < 9.2 will behave properly as a Consumer
   private static final CookieProtocol BEA_8_CONSUMER_FIX = CookieProtocol.PER_USER;
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

   public void onEvent(ManagedObjectRegistryEvent event)
   {
      if (event instanceof ManagedObjectEvent)
      {
         ManagedObjectEvent managedObjectEvent = (ManagedObjectEvent)event;
         ManagedObject managedObject = managedObjectEvent.getManagedObject();

         if (managedObject instanceof ManagedPortletContainer)
         {
            ManagedPortletContainer portletContainer = (ManagedPortletContainer)managedObject;
            String applicationId = portletContainer.getManagedPortletApplication().getId();
            String containerId = portletContainer.getId();

            org.gatein.pc.api.PortletContext pc = org.gatein.pc.api.PortletContext.createPortletContext(applicationId, containerId);

            if (managedObjectEvent instanceof ManagedObjectLifeCycleEvent)
            {
               ManagedObjectLifeCycleEvent lifeCycleEvent = (ManagedObjectLifeCycleEvent)managedObjectEvent;
               LifeCycleStatus status = lifeCycleEvent.getStatus();
               if (LifeCycleStatus.STARTED.equals(status))
               {
                  final PortletInfo info = portletContainer.getInfo();
                  // only add the portlet if it's remotable
                  if (isRemotable(info.getRuntimeOptionsInfo()))
                  {
                     serviceDescription.addPortletDescription(pc, info);
                  }
               }
               else
               {
                  serviceDescription.removePortletDescription(pc);
               }
            }

         }
      }
   }

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

   public void reset()
   {
      serviceDescription = new ServiceDescriptionInfo();
   }

   public boolean isRemotable(Map<String, RuntimeOptionInfo> runtimeOptions)
   {
      RuntimeOptionInfo runtimeOptionInfo = runtimeOptions.get(RuntimeOptionInfo.REMOTABLE_RUNTIME_OPTION);

      return runtimeOptionInfo != null && "true".equals(runtimeOptionInfo.getValues().get(0));
   }

   private class ServiceDescriptionInfo
   {
      private long lastGenerated;
      private Map<QName, EventDescription> eventDescriptions;
      private Map<QName, Integer> eventReferenceCount;
      private Map<String, PortletDescriptionInfo> portletDescriptions;
      private ModelDescription registrationProperties;
      private boolean initialized = false;
      private boolean requireRegistrations;

      private ServiceDescriptionInfo()
      {
         reset();
      }

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

      private void updateRegistrationProperties(ProducerRegistrationRequirements requirements)
      {
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

            lastGenerated = SupportsLastModified.now();
         }
      }

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

      private ServiceDescription getServiceDescription(boolean needsRegistrationProperties, boolean needsPortletDescriptions, List<String> portletHandles, List<String> desiredLocales)
      {
         initIfNeeded();

         ModelDescription registrationProperties = needsRegistrationProperties ? this.registrationProperties : null;

         ServiceDescription serviceDescription = WSRPTypeFactory.createServiceDescription(false);
         serviceDescription.setRequiresInitCookie(BEA_8_CONSUMER_FIX);
         List<String> supportedLocales = producer.getSupportedLocales();
         serviceDescription.getLocales().addAll(supportedLocales);
         serviceDescription.getSupportedOptions().addAll(OPTIONS);
         serviceDescription.setRegistrationPropertyDescription(registrationProperties);
         serviceDescription.setRequiresRegistration(requireRegistrations);

         Collection<PortletDescription> portlets;
         if (needsPortletDescriptions)
         {
            // if we have a list of portlet handles, filter the list of offered portlets
            if (!ParameterValidation.existsAndIsNotEmpty(portletHandles))
            {
               portletHandles = new ArrayList<String>(portletDescriptions.keySet());
            }

            portlets = new ArrayList<PortletDescription>(portletHandles.size());
            for (String handle : portletHandles)
            {
               PortletDescription description = getPortletDescription(handle, desiredLocales);
               if (description != null)
               {
                  portlets.add(description);
               }
            }
            serviceDescription.getOfferedPortlets().addAll(portlets);
         }

         // events
         Collection<EventDescription> events = eventDescriptions.values();
         serviceDescription.getEventDescriptions().addAll(events);

         return serviceDescription;
      }

      private void initIfNeeded()
      {
         if (!initialized)
         {
            updatePortletDescriptions();
         }
      }

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

      private void removeEvent(QName name)
      {
         Integer current = eventReferenceCount.get(name);
         if (current != null)
         {
            if (current == 1)
            {
               eventDescriptions.remove(name);
               eventReferenceCount.remove(name);
            }
            else
            {
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

      private PortletDescriptionInfo createPortletDescription(PortletInfo info, String handle)
      {
         if (log.isDebugEnabled())
         {
            log.debug("Constructing portlet description for: " + handle);
         }

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

         MetaInfo metaInfo = info.getMeta();


         // iterate over locales and create a portlet description for each
         Set<Locale> supportedLocales = info.getCapabilities().getAllLocales();
         List<String> supportedLanguages;
         if (supportedLocales.size() == 0)
         {
            // use English as failback per PLT.25.8.1
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
       * @param context
       * @param desiredLocales
       * @param registration
       * @return
       */
      public PortletDescription getPortletDescription(PortletContext context, List<String> desiredLocales, Registration registration)
      {
         initIfNeeded();

         org.gatein.pc.api.PortletContext pcContext = WSRPUtils.convertToPortalPortletContext(context);
         if (producer.getRegistrationManager().getPolicy().allowAccessTo(pcContext, registration, "getPortletDescription"))
         {
            PortletDescription description = getPortletDescription(context.getPortletHandle(), desiredLocales);

            if (description == null)
            {
               // check if we asked for the description of a clone
               if (registration.knows(pcContext))
               {
                  try
                  {
                     // retrieve initial context from portlet info and get description from it
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

      public void removePortletDescription(org.gatein.pc.api.PortletContext pc)
      {
         String handle = WSRPUtils.convertToWSRPPortletContext(pc).getPortletHandle();

         PortletDescription description = getPortletDescription(handle, null);
         if (description != null)
         {
            // deal with events
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

      private class PortletDescriptionInfo
      {
         private Map<String, PortletDescription> languageToDescription;

         private PortletDescriptionInfo(List<String> supportedLanguages)
         {
            languageToDescription = new HashMap<String, PortletDescription>(supportedLanguages.size());
            for (String supportedLanguage : supportedLanguages)
            {
               languageToDescription.put(supportedLanguage, null);
            }
         }

         public Set<String> getSupportedLanguages()
         {
            return languageToDescription.keySet();
         }

         public PortletDescription getBestDescriptionFor(List<String> desiredLanguages)
         {
            String language = null;

            Set<String> supportedLanguages = getSupportedLanguages();
            // check first if we have an exact match
            if (desiredLanguages != null && !desiredLanguages.isEmpty())
            {
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

         public void addDescriptionFor(String language, PortletDescription desc)
         {
            languageToDescription.put(language, desc);
         }
      }
   }
}
