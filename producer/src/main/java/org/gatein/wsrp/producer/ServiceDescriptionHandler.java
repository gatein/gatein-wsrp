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

import org.gatein.common.net.media.MediaType;
import org.gatein.common.util.ParameterValidation;
import org.gatein.pc.api.Portlet;
import org.gatein.pc.api.PortletInvokerException;
import org.gatein.pc.api.info.CapabilitiesInfo;
import org.gatein.pc.api.info.MetaInfo;
import org.gatein.pc.api.info.ModeInfo;
import org.gatein.pc.api.info.PortletInfo;
import org.gatein.pc.api.info.WindowStateInfo;
import org.gatein.registration.Registration;
import org.gatein.wsrp.WSRPExceptionFactory;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.WSRPUtils;
import org.gatein.wsrp.producer.config.ProducerRegistrationRequirements;
import org.gatein.wsrp.registration.RegistrationPropertyDescription;
import org.oasis.wsrp.v1.CookieProtocol;
import org.oasis.wsrp.v1.GetServiceDescription;
import org.oasis.wsrp.v1.InvalidHandle;
import org.oasis.wsrp.v1.InvalidRegistration;
import org.oasis.wsrp.v1.MarkupType;
import org.oasis.wsrp.v1.ModelDescription;
import org.oasis.wsrp.v1.OperationFailed;
import org.oasis.wsrp.v1.OperationFailedFault;
import org.oasis.wsrp.v1.PortletContext;
import org.oasis.wsrp.v1.PortletDescription;
import org.oasis.wsrp.v1.RegistrationContext;
import org.oasis.wsrp.v1.ServiceDescription;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 12017 $
 * @since 2.4
 */
class
   ServiceDescriptionHandler extends ServiceHandler implements ServiceDescriptionInterface
{
   // JBPORTAL-1220: force call to initCookie... Required so that BEA version < 9.2 will behave properly as a Consumer
   private final CookieProtocol BEA_8_CONSUMER_FIX = CookieProtocol.PER_USER;

   ServiceDescriptionHandler(WSRPProducerImpl producer)
   {
      super(producer);
   }

   public ServiceDescription getServiceDescription(GetServiceDescription gs)
      throws InvalidRegistration, OperationFailed
   {
      WSRPExceptionFactory.throwOperationFailedIfValueIsMissing(gs, "GetServiceDescription");

      RegistrationContext registrationContext = gs.getRegistrationContext();

      ProducerRegistrationRequirements requirements = producer.getProducerRegistrationRequirements();
      ServiceDescription serviceDescription = WSRPTypeFactory.createServiceDescription(requirements.isRegistrationRequired());
      serviceDescription.setRequiresInitCookie(BEA_8_CONSUMER_FIX);
      serviceDescription.getLocales().addAll(producer.getSupportedLocales());

      // if a RegistrationContext is provided, we need to validate the registration information
      Registration registration = null;
      if (registrationContext != null)
      {
         registration = producer.getRegistrationOrFailIfInvalid(registrationContext);
      }

      // get the portlet descriptions based on registration information
      List<PortletDescription> offeredPortlets = getPortletDescriptions(gs.getDesiredLocales(), registration);
      if (offeredPortlets != null)
      {
         serviceDescription.getOfferedPortlets().addAll(offeredPortlets);
      }

      // if we don't have registration information but a registration is required, send registration props information
      if (registration == null && requirements.isRegistrationRequired())
      {
         log.debug("Unregistered consumer while registration is required. Sending registration information.");

         // do not create a ModelDescription if there is no registration properties
         Map<QName, RegistrationPropertyDescription> info = requirements.getRegistrationProperties();
         ModelDescription description = null;
         if (info != null && !info.isEmpty())
         {
            description = Utils.convertRegistrationPropertiesToModelDescription(info);
         }

         serviceDescription.setRegistrationPropertyDescription(description);
      }

      return serviceDescription;
   }


   private Set<PortletDescription> getOfferedPortletDescriptions(List<String> desiredLocales)
   {
      Set<Portlet> portlets;
      try
      {
         portlets = producer.getRemotablePortlets();
      }
      catch (PortletInvokerException e)
      {
         log.warn("Could not retrieve portlets. Reason:\n\t" + e.getLocalizedMessage());
         return Collections.emptySet();
      }

      Set<PortletDescription> offeredPortletDescriptions = new HashSet<PortletDescription>(portlets.size());

      for (Portlet portlet : portlets)
      {
         PortletDescription desc = getPortletDescription(portlet, desiredLocales);
         offeredPortletDescriptions.add(desc);
      }

      return offeredPortletDescriptions;
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
         throw WSRPExceptionFactory.<OperationFailed, OperationFailedFault>throwWSException(WSRPExceptionFactory.OPERATION_FAILED, "Could not retrieve portlet '" + portletContext + "'", e);
      }
   }

   /**
    * @param desiredLocales the locales in which the portlet descriptions must be provided (best effort)
    * @param registration   used to filter offered portlet lists
    * @return an array of portlet descriptions offered by this producer (which could be filtered based on the
    *         registration information) or <code>null</code> if no registration is provided and the producer requires
    *         registration to access the full service description.
    */
   private List<PortletDescription> getPortletDescriptions(List<String> desiredLocales, Registration registration)
   {
      ProducerRegistrationRequirements registrationReq = producer.getProducerRegistrationRequirements();
      if (registration == null && registrationReq.isRegistrationRequired() && registrationReq.isRegistrationRequiredForFullDescription())
      {
         return null;
      }

      Set<PortletDescription> descriptions = getOfferedPortletDescriptions(desiredLocales);
      return new ArrayList<PortletDescription>(descriptions);
   }

   /**
    * Creates a PortletDescription based on the user desired locales (ordered according to user preferences) for the
    * specified component.
    *
    * @param portlet
    * @param desiredLocales the user desired locales (ordered according to user preferences) to use for the description
    * @return a PortletDescription describing the specified portlet
    */
   PortletDescription getPortletDescription(Portlet portlet, List<String> desiredLocales)
   {
      String id = portlet.getContext().getId();
      PortletInfo info = portlet.getInfo();
      log.debug("Constructing portlet description for: " + id);

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

      //todo generate a valid and better portlet handle
      PortletDescription desc = WSRPTypeFactory.createPortletDescription(id, markupTypes);

      // todo: group ID

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
      org.oasis.wsrp.v1.LocalizedString concatenatedKeywords =
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

      /* todo:
      * [O] string	userCategories[]
      * [O] string	userProfileItems[]
      * [O] boolean	usesMethodGet
      * [O] boolean	defaultMarkupSecure
      * [O] boolean	onlySecure
      * [O] boolean	userContextStoredInSession
      * [O] boolean	templatesStoredInSession
      * [O] boolean	hasUserSpecificState
      * [O] boolean	doesUrlTemplateProcessing
      * [O] Extension	extensions
      */
      return desc;
   }

   private List<String> getLocaleNamesFrom(Collection<Locale> locales)
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

   private List<String> getWindowStateNamesFrom(Collection<WindowStateInfo> windowStates)
   {
      List<String> result = new ArrayList<String>(windowStates.size());
      for (WindowStateInfo windowStateInfo : windowStates)
      {
         result.add(WSRPUtils.convertJSR168WindowStateNameToWSRPName(windowStateInfo.getWindowStateName()));
      }
      return result;
   }

   private List<String> getModeNamesFrom(Collection<ModeInfo> modes)
   {
      List<String> result = new ArrayList<String>(modes.size());
      for (ModeInfo modeInfo : modes)
      {
         result.add(WSRPUtils.convertJSR168PortletModeNameToWSRPName(modeInfo.getModeName()));
      }
      return result;
   }
}
