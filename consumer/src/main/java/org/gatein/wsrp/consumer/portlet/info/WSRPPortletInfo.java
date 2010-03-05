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

package org.gatein.wsrp.consumer.portlet.info;

import org.gatein.common.net.media.MediaType;
import org.gatein.common.util.ParameterValidation;
import org.gatein.pc.api.Mode;
import org.gatein.pc.api.TransportGuarantee;
import org.gatein.pc.api.WindowState;
import org.gatein.pc.api.info.CacheInfo;
import org.gatein.pc.api.info.CapabilitiesInfo;
import org.gatein.pc.api.info.EventInfo;
import org.gatein.pc.api.info.EventingInfo;
import org.gatein.pc.api.info.MetaInfo;
import org.gatein.pc.api.info.ModeInfo;
import org.gatein.pc.api.info.NavigationInfo;
import org.gatein.pc.api.info.ParameterInfo;
import org.gatein.pc.api.info.PreferenceInfo;
import org.gatein.pc.api.info.PreferencesInfo;
import org.gatein.pc.api.info.RuntimeOptionInfo;
import org.gatein.pc.api.info.SecurityInfo;
import org.gatein.pc.api.info.WindowStateInfo;
import org.gatein.wsrp.WSRPUtils;
import org.gatein.wsrp.consumer.ProducerInfo;
import org.oasis.wsrp.v1.LocalizedString;
import org.oasis.wsrp.v1.MarkupType;
import org.oasis.wsrp.v1.ModelDescription;
import org.oasis.wsrp.v1.PortletDescription;
import org.oasis.wsrp.v1.PortletPropertyDescriptionResponse;
import org.oasis.wsrp.v1.PropertyDescription;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 12025 $
 * @since 2.4 (Apr 30, 2006)
 */
public class WSRPPortletInfo implements org.gatein.pc.api.info.PortletInfo
{

   public static final String PRODUCER_NAME_META_INFO_KEY = "producer-name";

   private WSRPCapabilitiesInfo capabilities;
   private MetaInfo metaInfo;
   private boolean usesMethodGet;
   private boolean defaultMarkupSecure;
   private boolean onlySecure;
   private boolean hasUserSpecificState;
   private boolean userContextStoredInSession;
   private boolean templatesStoredInSession;
   private boolean doesUrlTemplateProcessing;
   private String applicationName;
   private String groupId;
   private PreferencesInfo prefInfo;
   private ProducerInfo originatingProducer;
   private String portletHandle;

   public WSRPPortletInfo(final PortletDescription portletDescription, ProducerInfo originatingProducerInfo)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(portletDescription, "PortletDescription");
      ParameterValidation.throwIllegalArgExceptionIfNull(originatingProducerInfo, "ProducerInfo");

      createCapabilitiesInfo(portletDescription);

      createMetaInfo(portletDescription, originatingProducerInfo.getId());

      createWSRPInfo(portletDescription, originatingProducerInfo.getId());

      this.originatingProducer = originatingProducerInfo;
      this.portletHandle = portletDescription.getPortletHandle();
   }


   public WSRPPortletInfo(WSRPPortletInfo other, String newHandle)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(other, "WSRPPortletInfo");
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(newHandle, "new portlet handle", "WSRPPortletInfo");

      usesMethodGet = other.usesMethodGet;
      defaultMarkupSecure = other.defaultMarkupSecure;
      onlySecure = other.onlySecure;
      hasUserSpecificState = other.hasUserSpecificState;
      userContextStoredInSession = other.userContextStoredInSession;
      templatesStoredInSession = other.templatesStoredInSession;
      doesUrlTemplateProcessing = other.doesUrlTemplateProcessing;
      groupId = other.groupId;
      applicationName = other.applicationName;

      WSRPCapabilitiesInfo otherCapabilities = (WSRPCapabilitiesInfo)other.getCapabilities();
      capabilities = new WSRPCapabilitiesInfo(new HashMap<MediaType, MediaTypeInfo>(otherCapabilities.mediaTypes),
         new HashSet<ModeInfo>(otherCapabilities.modes), new HashSet<WindowStateInfo>(otherCapabilities.windowStates),
         new HashSet<Locale>(otherCapabilities.locales));

      WSRPMetaInfo otherMeta = (WSRPMetaInfo)other.getMeta();
      metaInfo = new WSRPMetaInfo(new HashMap<String, org.gatein.common.i18n.LocalizedString>(otherMeta.metaInfos));
      WSRPPreferencesInfo otherPref = (WSRPPreferencesInfo)other.getPreferences();
      prefInfo = new WSRPPreferencesInfo(new HashMap<String, PreferenceInfo>(otherPref.preferences));

      originatingProducer = other.originatingProducer;
      portletHandle = newHandle;
   }

   public String getName()
   {
      return portletHandle;
   }

   public String getApplicationName()
   {
      return applicationName;
   }

   public CapabilitiesInfo getCapabilities()
   {
      return capabilities;
   }

   public PreferencesInfo getPreferences()
   {
      // lazy initialization of preference information since it requires an access to PortletManagement which would be
      // too bandwidth intensive if it was done when the service description is parsed...
      if (prefInfo == null)
      {
         PortletPropertyDescriptionResponse propertyDescs = originatingProducer.getPropertyDescriptionsFor(portletHandle);
         Map<String, PreferenceInfo> prefInfos = null;

         if (propertyDescs != null)
         {
            ModelDescription modelDesc = propertyDescs.getModelDescription();
            if (modelDesc != null)
            {
               List<PropertyDescription> descs = modelDesc.getPropertyDescriptions();
               if (descs != null)
               {
                  prefInfos = new HashMap<String, PreferenceInfo>(descs.size());
                  for (PropertyDescription desc : descs)
                  {
                     String key = desc.getName();
                     prefInfos.put(key, new WSRPPreferenceInfo(key, getPortalLocalizedStringOrNullFrom(desc.getLabel()),
                        getPortalLocalizedStringOrNullFrom(desc.getHint())));
                  }
               }
               else
               {
                  prefInfos = Collections.emptyMap();
               }
            }
         }

         if (prefInfos == null)
         {
            prefInfos = Collections.emptyMap();
         }

         prefInfo = new WSRPPreferencesInfo(prefInfos);
      }

      return prefInfo;
   }

   public MetaInfo getMeta()
   {
      return metaInfo;
   }

   public SecurityInfo getSecurity()
   {
      // todo: get more details on the SecurityInfo contract...
      return new SecurityInfo()
      {
         public boolean containsTransportGuarantee(TransportGuarantee transportGuarantee)
         {
            return TransportGuarantee.NONE.equals(transportGuarantee);
         }

         public Set<TransportGuarantee> getTransportGuarantees()
         {
            return Collections.singleton(TransportGuarantee.NONE);
         }
      };
   }

   public CacheInfo getCache()
   {
      return new CacheInfo()
      {
         public int getExpirationSecs()
         {
            Integer expirationCacheSeconds = originatingProducer.getExpirationCacheSeconds();
            return expirationCacheSeconds != null ? expirationCacheSeconds : 0;
         }
      };
   }

   public Boolean isRemotable()
   {
      return Boolean.FALSE;
   }

   public EventingInfo getEventing()
   {
      //todo: revisit when implementing WSRP 2
      return new EventingInfo()
      {
         public Map<QName, ? extends EventInfo> getProducedEvents()
         {
            return Collections.emptyMap();
         }

         public Map<QName, ? extends EventInfo> getConsumedEvents()
         {
            return Collections.emptyMap();
         }
      };
   }

   public NavigationInfo getNavigation()
   {
      //todo: revisit when implementing WSRP 2
      return new NavigationInfo()
      {
         public ParameterInfo getPublicParameter(String s)
         {
            return null;
         }

         public ParameterInfo getPublicParameter(QName qName)
         {
            return null;
         }

         public Collection<? extends ParameterInfo> getPublicParameters()
         {
            return Collections.emptyList();
         }
      };
   }

   public <T> T getAttachment(Class<T> tClass) throws IllegalArgumentException
   {
      return null;
   }

   public Map<String, RuntimeOptionInfo> getRuntimeOptionsInfo()
   {
      return Collections.emptyMap();
   }

   public boolean isUsesMethodGet()
   {
      return usesMethodGet;
   }

   public boolean isDefaultMarkupSecure()
   {
      return defaultMarkupSecure;
   }

   public boolean isOnlySecure()
   {
      return onlySecure;
   }

   public boolean isHasUserSpecificState()
   {
      return hasUserSpecificState;
   }

   public boolean isUserContextStoredInSession()
   {
      return userContextStoredInSession;
   }

   public boolean isTemplatesStoredInSession()
   {
      return templatesStoredInSession;
   }

   public boolean isDoesUrlTemplateProcessing()
   {
      return doesUrlTemplateProcessing;
   }

   public String getGroupId()
   {
      return groupId;
   }

   private void createWSRPInfo(PortletDescription portletDescription, String consumerId)
   {
//      String[] userCategories = portletDescription.getUserCategories();
//      String[] userProfileItems = portletDescription.getUserProfileItems();

      usesMethodGet = Boolean.TRUE.equals(portletDescription.isUsesMethodGet());

      defaultMarkupSecure = Boolean.TRUE.equals(portletDescription.isDefaultMarkupSecure());
      onlySecure = Boolean.TRUE.equals(portletDescription.isOnlySecure());
      userContextStoredInSession = Boolean.TRUE.equals(portletDescription.isUserContextStoredInSession());
      templatesStoredInSession = Boolean.TRUE.equals(portletDescription.isTemplatesStoredInSession());
      hasUserSpecificState = Boolean.TRUE.equals(portletDescription.isHasUserSpecificState());
      doesUrlTemplateProcessing = Boolean.TRUE.equals(portletDescription.isDoesUrlTemplateProcessing());

      groupId = portletDescription.getGroupID();

      // if we don't have a group id, use the consumer id as the application name
      if (ParameterValidation.isNullOrEmpty(groupId))
      {
         applicationName = consumerId;
      }
      else
      {
         applicationName = groupId;
      }

   }

   private void createCapabilitiesInfo(PortletDescription portletDescription)
   {
      final List<MarkupType> markupTypes = portletDescription.getMarkupTypes();
      final Map<MediaType, MediaTypeInfo> mediaTypes = new HashMap<MediaType, MediaTypeInfo>(markupTypes.size());
      capabilities = new WSRPCapabilitiesInfo();

      for (MarkupType markupType : markupTypes)
      {
         MediaType mediaType = MediaType.create(markupType.getMimeType());
         MediaTypeInfo mediaTypeInfo = new MediaTypeInfo(markupType);
         mediaTypes.put(mediaType, mediaTypeInfo);
         capabilities.addModes(mediaTypeInfo.modes);
         capabilities.addWindowStates(mediaTypeInfo.windowStates);
         capabilities.addLocales(mediaTypeInfo.locales);
      }

      capabilities.setMediaTypes(mediaTypes);
   }

   private void createMetaInfo(PortletDescription portletDescription, String producerId)
   {
      final Map<String, org.gatein.common.i18n.LocalizedString> metaInfos = new HashMap<String, org.gatein.common.i18n.LocalizedString>();
      metaInfos.put(MetaInfo.DESCRIPTION, getPortalLocalizedStringOrNullFrom(portletDescription.getDescription()));
      metaInfos.put(MetaInfo.DISPLAY_NAME, getPortalLocalizedStringOrNullFrom(portletDescription.getDisplayName()));
      metaInfos.put(MetaInfo.SHORT_TITLE, getPortalLocalizedStringOrNullFrom(portletDescription.getShortTitle()));
      metaInfos.put(MetaInfo.TITLE, getPortalLocalizedStringOrNullFrom(portletDescription.getTitle()));

      // keywords need to be concatenated
      List<LocalizedString> keywords = portletDescription.getKeywords();
      String keywordsString = "";
      Locale locale = Locale.ENGLISH;
      if (keywords != null)
      {
         int keywordsNb = keywords.size();
         if (keywordsNb > 0)
         {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < keywordsNb; i++)
            {
               LocalizedString keyword = keywords.get(i);
               sb.append(keyword.getValue());
               if (i != keywordsNb - 1)
               {
                  sb.append(","); // not the last one, so concatenate a comma to separate
               }
            }
            keywordsString = sb.toString();
            // fix-me: for now assume that they all have the same language... this could get messy!
            locale = WSRPUtils.getLocale(keywords.get(0).getLang());
         }
      }

      metaInfos.put(MetaInfo.KEYWORDS, new org.gatein.common.i18n.LocalizedString(keywordsString, locale));

      metaInfos.put(PRODUCER_NAME_META_INFO_KEY, new org.gatein.common.i18n.LocalizedString(producerId, locale));

      metaInfo = new WSRPMetaInfo(metaInfos);
   }

   private org.gatein.common.i18n.LocalizedString getPortalLocalizedStringOrNullFrom(LocalizedString wsrpLocalizedString)
   {
      if (wsrpLocalizedString != null)
      {
         return new org.gatein.common.i18n.LocalizedString(wsrpLocalizedString.getValue(),
            WSRPUtils.getLocale(wsrpLocalizedString.getLang()));
      }

      return null;
   }

   class MediaTypeInfo
   {
      public MediaTypeInfo(MarkupType markupType)
      {
         mimeType = MediaType.create(markupType.getMimeType());

         List<String> modeNames = markupType.getModes();
         modes = new HashSet<ModeInfo>(modeNames.size());
         for (String modeName : modeNames)
         {
            modes.add(new BasicModeInfo(WSRPUtils.getJSR168PortletModeFromWSRPName(modeName)));
         }

         List<String> windStateNames = markupType.getWindowStates();
         windowStates = new HashSet<WindowStateInfo>(windStateNames.size());
         for (String windStateName : windStateNames)
         {
            windowStates.add(new BasicWindowStateInfo(WSRPUtils.getJSR168WindowStateFromWSRPName(windStateName)));
         }

         List<String> localeNames = markupType.getLocales();
         if (localeNames != null)
         {
            locales = new HashSet<Locale>(localeNames.size());
            for (String localeName : localeNames)
            {
               locales.add(WSRPUtils.getLocale(localeName));
            }
         }
         else
         {
            locales = Collections.emptySet();
         }
      }

      MediaType mimeType;
      Set<ModeInfo> modes;
      Set<WindowStateInfo> windowStates;
      Set<Locale> locales;
   }

   class BasicWindowStateInfo implements WindowStateInfo
   {
      WindowState state;

      public BasicWindowStateInfo(WindowState state)
      {
         this.state = state;
      }

      public org.gatein.common.i18n.LocalizedString getDescription()
      {
         return new org.gatein.common.i18n.LocalizedString(getWindowStateName() + " window state", Locale.ENGLISH);
      }

      public WindowState getWindowState()
      {
         return state;
      }

      public String getWindowStateName()
      {
         return state.toString();
      }
   }

   class BasicModeInfo implements ModeInfo
   {
      Mode mode;

      public BasicModeInfo(Mode mode)
      {
         this.mode = mode;
      }

      public org.gatein.common.i18n.LocalizedString getDescription()
      {
         return new org.gatein.common.i18n.LocalizedString(getModeName() + " mode", Locale.ENGLISH);
      }

      public Mode getMode()
      {
         return mode;
      }

      public String getModeName()
      {
         return mode.toString();
      }
   }

   private class WSRPCapabilitiesInfo implements CapabilitiesInfo
   {
      private Map<MediaType, MediaTypeInfo> mediaTypes;
      private Set<ModeInfo> modes;
      private Set<WindowStateInfo> windowStates;
      private Set<Locale> locales;

      private WSRPCapabilitiesInfo()
      {
      }

      private void setMediaTypes(Map<MediaType, MediaTypeInfo> mediaTypes)
      {
         this.mediaTypes = mediaTypes;
      }

      public WSRPCapabilitiesInfo(Map<MediaType, MediaTypeInfo> mediaTypes, Set<ModeInfo> modes, Set<WindowStateInfo> windowStates, Set<Locale> locales)
      {
         this.mediaTypes = mediaTypes;
         this.modes = modes;
         this.windowStates = windowStates;
         this.locales = locales;
      }

      public Set<MediaType> getMediaTypes()
      {
         return mediaTypes.keySet();
      }

      public Set<ModeInfo> getAllModes()
      {
         return modes;
      }

      public Set<ModeInfo> getModes(MediaType mediaType)
      {
         MediaTypeInfo mimeTypeInfo = mediaTypes.get(mediaType);
         if (mimeTypeInfo == null)
         {
            return Collections.emptySet();
         }

         return Collections.unmodifiableSet(mimeTypeInfo.modes);
      }

      public ModeInfo getMode(Mode mode)
      {
         for (ModeInfo info : modes)
         {
            if (info.getMode().equals(mode))
            {
               return info;
            }
         }
         return null;
      }

      public Set<WindowStateInfo> getAllWindowStates()
      {
         return windowStates;
      }

      public Set<WindowStateInfo> getWindowStates(MediaType mediaType)
      {
         MediaTypeInfo mimeTypeInfo = mediaTypes.get(mediaType);
         if (mimeTypeInfo == null)
         {
            return Collections.emptySet();
         }

         return Collections.unmodifiableSet(mimeTypeInfo.windowStates);
      }

      public WindowStateInfo getWindowState(WindowState windowState)
      {
         for (WindowStateInfo info : windowStates)
         {
            if (info.getWindowState().equals(windowState))
            {
               return info;
            }
         }
         return null;
      }

      public Set<Locale> getAllLocales()
      {
         return locales;
      }

      public Set<Locale> getLocales(MediaType mediaType)
      {
         MediaTypeInfo mimeTypeInfo = mediaTypes.get(mediaType);
         if (mimeTypeInfo == null)
         {
            return Collections.emptySet();
         }

         return Collections.unmodifiableSet(mimeTypeInfo.locales);
      }

      private void addModes(Set<ModeInfo> modes)
      {
         if (modes != null)
         {
            int size = modes.size();
            if (this.modes == null)
            {
               this.modes = new HashSet<ModeInfo>(size);
            }

            this.modes.addAll(modes);
         }
      }

      private void addWindowStates(Set<WindowStateInfo> windowStates)
      {
         if (windowStates != null)
         {
            int size = windowStates.size();
            if (this.windowStates == null)
            {
               this.windowStates = new HashSet<WindowStateInfo>(size);
            }

            this.windowStates.addAll(windowStates);
         }
      }

      private void addLocales(Set<Locale> locales)
      {
         if (locales != null)
         {
            int size = locales.size();
            if (this.locales == null)
            {
               this.locales = new HashSet<Locale>(size);
            }

            this.locales.addAll(locales);
         }
      }
   }

   private static class WSRPMetaInfo implements MetaInfo
   {
      private final Map<String, org.gatein.common.i18n.LocalizedString> metaInfos;

      public WSRPMetaInfo(Map<String, org.gatein.common.i18n.LocalizedString> metaInfos)
      {
         this.metaInfos = metaInfos;
      }

      public org.gatein.common.i18n.LocalizedString getMetaValue(String key)
      {
         return metaInfos.get(key);
      }
   }

   static class WSRPPreferencesInfo implements PreferencesInfo
   {
      private Map<String, PreferenceInfo> preferences;

      public WSRPPreferencesInfo(Map<String, PreferenceInfo> preferences)
      {
         this.preferences = preferences;
      }

      public Set<String> getKeys()
      {
         return Collections.unmodifiableSet(preferences.keySet());
      }

      public PreferenceInfo getPreference(String key) throws IllegalArgumentException
      {
         return preferences.get(key);
      }
   }

   static class WSRPPreferenceInfo implements PreferenceInfo
   {
      private String key;
      private org.gatein.common.i18n.LocalizedString displayName;
      private org.gatein.common.i18n.LocalizedString description;

      public WSRPPreferenceInfo(String key, org.gatein.common.i18n.LocalizedString displayName, org.gatein.common.i18n.LocalizedString description)
      {
         this.key = key;
         this.displayName = displayName;
         this.description = description;
      }

      public String getKey()
      {
         return key;
      }

      public org.gatein.common.i18n.LocalizedString getDisplayName()
      {
         return displayName;
      }

      public org.gatein.common.i18n.LocalizedString getDescription()
      {
         return description;
      }

      public Boolean isReadOnly()
      {
         // cannot be determined, so returning null per javadoc
         // note : it will be possible to implement it using WSRP 2.0 which defines this notion
         // in the protocol
         return null;
      }

      public List<String> getDefaultValue()
      {
         return null;
      }
   }
}
