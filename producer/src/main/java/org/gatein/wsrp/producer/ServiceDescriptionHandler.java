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
import org.gatein.pc.api.info.SecurityInfo;
import org.gatein.pc.api.info.WindowStateInfo;
import org.gatein.registration.Registration;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.WSRPUtils;
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
import org.oasis.wsrp.v2.OperationFailed;
import org.oasis.wsrp.v2.ParameterDescription;
import org.oasis.wsrp.v2.PortletContext;
import org.oasis.wsrp.v2.PortletDescription;
import org.oasis.wsrp.v2.RegistrationContext;
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
class ServiceDescriptionHandler extends ServiceHandler implements ServiceDescriptionInterface
{
   // JBPORTAL-1220: force call to initCookie... Required so that BEA version < 9.2 will behave properly as a Consumer
   private static final CookieProtocol BEA_8_CONSUMER_FIX = CookieProtocol.PER_USER;
   private ServiceDescriptionInfo serviceDescription;

   ServiceDescriptionHandler(WSRPProducerImpl producer)
   {
      super(producer);
      serviceDescription = new ServiceDescriptionInfo(producer);
   }

   public ServiceDescription getServiceDescription(GetServiceDescription gs) throws InvalidRegistration, OperationFailed
   {
      WSRP2ExceptionFactory.throwOperationFailedIfValueIsMissing(gs, "GetServiceDescription");

      RegistrationContext registrationContext = gs.getRegistrationContext();

      // if a RegistrationContext is provided, we need to validate the registration information
      Registration registration = null;
      if (registrationContext != null)
      {
         registration = producer.getRegistrationOrFailIfInvalid(registrationContext);
      }

      ProducerRegistrationRequirements requirements = producer.getProducerRegistrationRequirements();

      // if we don't have registration information but a registration is required, send registration props information
      boolean needsRegistrationProperties = registration == null && requirements.isRegistrationRequired();
      // TODO: verify if this is the correct behaviour. We should always make this change, as if the reqistrationRequired goes from true to false, the serviceDescriptions will never occur.
      if (needsRegistrationProperties)
      {
         serviceDescription.updateRegistrationProperties(requirements);
      }

      boolean needsPortletDescriptions = !(registration == null && requirements.isRegistrationRequired()
         && requirements.isRegistrationRequiredForFullDescription());
      if (needsPortletDescriptions)
      {
         Set<Portlet> portlets;
         try
         {
            portlets = producer.getRemotablePortlets();
         }
         catch (PortletInvokerException e)
         {
            log.warn("Could not retrieve portlets. Reason:\n\t" + e.getLocalizedMessage());
            portlets = Collections.emptySet();
         }
         serviceDescription.updatePortletDescriptions(portlets, gs.getDesiredLocales(), registration);
      }

      return serviceDescription.getServiceDescription(needsRegistrationProperties, needsPortletDescriptions);
   }

   public PortletDescription getPortletDescription(PortletContext portletContext, List<String> desiredLocales, Registration registration) throws InvalidHandle, OperationFailed
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(portletContext, "portlet context");
      Portlet portlet;
      try
      {
         portlet = producer.getPortletWith(WSRPUtils.convertToPortalPortletContext(portletContext), registration);
         return getPortletDescription(portlet, desiredLocales);
      }
      catch (PortletInvokerException e)
      {
         throw WSRP2ExceptionFactory.throwWSException(OperationFailed.class, "Could not retrieve portlet '" + portletContext + "'", e);
      }
   }

   /**
    * Creates a PortletDescription based on the user desired locales (ordered according to user preferences) for the
    * specified component.
    *
    * @param portlet
    * @param desiredLocales the user desired locales (ordered according to user preferences) to use for the description
    * @return a PortletDescription describing the specified portlet
    */
   static PortletDescription getPortletDescription(Portlet portlet, List<String> desiredLocales)
   {
      return getPortletDescription(portlet, desiredLocales, null);
   }

   /**
    * Creates a PortletDescription based on the user desired locales (ordered according to user preferences) for the
    * specified component.
    *
    * @param portlet
    * @param desiredLocales the user desired locales (ordered according to user preferences) to use for the description
    * @return a PortletDescription describing the specified portlet
    */
   static PortletDescription getPortletDescription(Portlet portlet, List<String> desiredLocales, ServiceDescriptionInfo sdi)
   {
      org.gatein.pc.api.PortletContext context = portlet.getContext();
      PortletInfo info = portlet.getInfo();
      String handle = context.getId();
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

      PortletDescription desc = WSRPTypeFactory.createPortletDescription(handle, markupTypes);

      // group ID
      desc.setGroupID(info.getApplicationName());

      MetaInfo metaInfo = info.getMeta();

      // description
      desc.setDescription(Utils.convertToWSRPLocalizedString(metaInfo.getMetaValue(MetaInfo.DESCRIPTION), desiredLocales));

      // short title
      desc.setShortTitle(Utils.convertToWSRPLocalizedString(metaInfo.getMetaValue(MetaInfo.SHORT_TITLE), desiredLocales));

      // title
      desc.setTitle(Utils.convertToWSRPLocalizedString(metaInfo.getMetaValue(MetaInfo.TITLE), desiredLocales));

      // display name
      desc.setDisplayName(Utils.convertToWSRPLocalizedString(metaInfo.getMetaValue(MetaInfo.DISPLAY_NAME), desiredLocales));

      // keywords
      // metaInfo contains comma-separated keywords: we need to extract them into a list
      org.oasis.wsrp.v2.LocalizedString concatenatedKeywords =
         Utils.convertToWSRPLocalizedString(metaInfo.getMetaValue(MetaInfo.KEYWORDS), desiredLocales);
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

               // record event info in ServiceDescriptionInfo
               if (sdi != null)
               {
                  sdi.addEventInfo(entry.getValue(), desiredLocales);
               }
            }
         }
         Map<QName, ? extends EventInfo> consumedEvents = eventsInfo.getConsumedEvents();
         if (ParameterValidation.existsAndIsNotEmpty(consumedEvents))
         {
            List<QName> handledEvents = desc.getHandledEvents();
            for (Map.Entry<QName, ? extends EventInfo> entry : consumedEvents.entrySet())
            {
               handledEvents.add(entry.getKey());

               // record event info in ServiceDescriptionInfo
               if (sdi != null)
               {
                  sdi.addEventInfo(entry.getValue(), desiredLocales);
               }
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
               paramDesc.setDescription(Utils.convertToWSRPLocalizedString(parameterInfo.getDescription(), desiredLocales));
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

      /* todo:
      * [O] ID	portletID
      * [O] string	userCategories[]
      * [O] string	userProfileItems[]
      * [O] string	portletManagedModes[]
      * [O] boolean	usesMethodGet
      * [O] boolean	defaultMarkupSecure
      * [O] boolean	userContextStoredInSession
      * [O] boolean	templatesStoredInSession
      * [O] boolean	hasUserSpecificState
      * [O] boolean	doesUrlTemplateProcessing
      * [O] boolean	mayReturnPortletState
      * [O] Extension	extensions[]
      */
      return desc;
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

   private static class ServiceDescriptionInfo
   {
      /** Empty service description: no registration properties, no offered portlets */
      private ServiceDescription noRegistrationNoPortletsServiceDescription;
      /** No registration properties, offered portles */
      private ServiceDescription noRegistrationPortletsServiceDescription;
      /** Registration properties, no offered portlets */
      private ServiceDescription registrationNoPortletsServiceDescription;
      /** Registration properties, offered portlets */
      private ServiceDescription registrationPortletsServiceDescription;

      private long lastGenerated;
      private Map<QName, EventDescription> eventDescriptions;

      private static final List<String> OPTIONS = new ArrayList<String>(5);

      static
      {
         OPTIONS.add(WSRP2Constants.OPTIONS_EVENTS);
         OPTIONS.add(WSRP2Constants.OPTIONS_IMPORT);
         OPTIONS.add(WSRP2Constants.OPTIONS_EXPORT);
      }

      private ServiceDescriptionInfo(WSRPProducerImpl producer)
      {
         noRegistrationNoPortletsServiceDescription = WSRPTypeFactory.createServiceDescription(false);
         noRegistrationNoPortletsServiceDescription.setRequiresInitCookie(BEA_8_CONSUMER_FIX);
         noRegistrationNoPortletsServiceDescription.getLocales().addAll(producer.getSupportedLocales());
         noRegistrationNoPortletsServiceDescription.getSupportedOptions().addAll(OPTIONS);

         noRegistrationPortletsServiceDescription = WSRPTypeFactory.createServiceDescription(false);
         noRegistrationPortletsServiceDescription.setRequiresInitCookie(BEA_8_CONSUMER_FIX);
         noRegistrationPortletsServiceDescription.getLocales().addAll(producer.getSupportedLocales());
         noRegistrationPortletsServiceDescription.getSupportedOptions().addAll(OPTIONS);

         registrationNoPortletsServiceDescription = WSRPTypeFactory.createServiceDescription(false);
         registrationNoPortletsServiceDescription.setRequiresInitCookie(BEA_8_CONSUMER_FIX);
         registrationNoPortletsServiceDescription.getLocales().addAll(producer.getSupportedLocales());
         registrationNoPortletsServiceDescription.getSupportedOptions().addAll(OPTIONS);

         registrationPortletsServiceDescription = WSRPTypeFactory.createServiceDescription(false);
         registrationPortletsServiceDescription.setRequiresInitCookie(BEA_8_CONSUMER_FIX);
         registrationPortletsServiceDescription.getLocales().addAll(producer.getSupportedLocales());
         registrationPortletsServiceDescription.getSupportedOptions().addAll(OPTIONS);
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
            ModelDescription registrationProperties = null;
            if (ParameterValidation.existsAndIsNotEmpty(info))
            {
               registrationProperties = Utils.convertRegistrationPropertiesToModelDescription(info);
            }
            registrationNoPortletsServiceDescription.setRegistrationPropertyDescription(registrationProperties);
            registrationPortletsServiceDescription.setRegistrationPropertyDescription(registrationProperties);

            // update need to register
            noRegistrationNoPortletsServiceDescription.setRequiresRegistration(requirements.isRegistrationRequired());
            noRegistrationPortletsServiceDescription.setRequiresRegistration(requirements.isRegistrationRequired());
            registrationNoPortletsServiceDescription.setRequiresRegistration(requirements.isRegistrationRequired());
            registrationPortletsServiceDescription.setRequiresRegistration(requirements.isRegistrationRequired());

            lastGenerated = System.currentTimeMillis();
         }
      }

      private void updatePortletDescriptions(Set<Portlet> portlets, List<String> desiredLocales, Registration registration)
      {
         if (ParameterValidation.existsAndIsNotEmpty(portlets))
         {
            Collection<PortletDescription> offeredPortletDescriptions = new ArrayList<PortletDescription>(portlets.size());

            // reset event descriptions as they will be repopulated when we build the portlet descriptions
            eventDescriptions = new HashMap<QName, EventDescription>(portlets.size());

            for (Portlet portlet : portlets)
            {
               PortletDescription desc = getPortletDescription(portlet, desiredLocales, this);
               offeredPortletDescriptions.add(desc);
            }

            // events
            Collection<EventDescription> events = eventDescriptions.values();
            List<EventDescription> eventDescriptions = registrationPortletsServiceDescription.getEventDescriptions();
            eventDescriptions.clear();
            eventDescriptions.addAll(events);

            eventDescriptions = registrationNoPortletsServiceDescription.getEventDescriptions();
            eventDescriptions.clear();
            eventDescriptions.addAll(events);

            eventDescriptions = noRegistrationPortletsServiceDescription.getEventDescriptions();
            eventDescriptions.clear();
            eventDescriptions.addAll(events);

            eventDescriptions = noRegistrationNoPortletsServiceDescription.getEventDescriptions();
            eventDescriptions.clear();
            eventDescriptions.addAll(events);

            // portlets
            List<PortletDescription> offeredPortlets = registrationPortletsServiceDescription.getOfferedPortlets();
            offeredPortlets.clear();
            offeredPortlets.addAll(offeredPortletDescriptions);

            offeredPortlets = noRegistrationPortletsServiceDescription.getOfferedPortlets();
            offeredPortlets.clear();
            offeredPortlets.addAll(offeredPortletDescriptions);
         }
      }

      private ServiceDescription getServiceDescription(boolean needsRegistrationProperties, boolean needsPortletDescriptions)
      {
         if (needsRegistrationProperties)
         {
            return needsPortletDescriptions ? registrationPortletsServiceDescription : registrationNoPortletsServiceDescription;
         }
         else
         {
            return needsPortletDescriptions ? noRegistrationPortletsServiceDescription : noRegistrationNoPortletsServiceDescription;
         }
      }

      public void addEventInfo(EventInfo info, List<String> desiredLocales)
      {
         QName name = info.getName();
         if (!eventDescriptions.containsKey(name))
         {
            EventDescription desc = WSRPTypeFactory.createEventDescription(name);
            desc.setDescription(Utils.convertToWSRPLocalizedString(info.getDescription(), desiredLocales));
            desc.setLabel(Utils.convertToWSRPLocalizedString(info.getDisplayName(), desiredLocales));
            Collection<QName> aliases = info.getAliases();
            if (ParameterValidation.existsAndIsNotEmpty(aliases))
            {
               desc.getAliases().addAll(aliases);
            }
            // todo: deal with type info...
            eventDescriptions.put(name, desc);
         }
      }
   }
}
