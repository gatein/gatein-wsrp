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

package org.gatein.wsrp.spec.v1;

import org.gatein.common.text.TextTools;
import org.gatein.common.util.ParameterValidation;
import org.gatein.pc.api.ActionURL;
import org.gatein.pc.api.ContainerURL;
import org.gatein.pc.api.Mode;
import org.gatein.pc.api.OpaqueStateString;
import org.gatein.pc.api.PortletStateType;
import org.gatein.pc.api.RenderURL;
import org.gatein.pc.api.ResourceURL;
import org.gatein.pc.api.StateString;
import org.gatein.pc.api.StatefulPortletContext;
import org.gatein.pc.api.URLFormat;
import org.gatein.pc.api.WindowState;
import org.gatein.pc.api.cache.CacheLevel;
import org.gatein.pc.api.spi.PortletInvocationContext;
import org.gatein.wsrp.WSRPConstants;
import org.gatein.wsrp.WSRPResourceURL;
import org.gatein.wsrp.WSRPRewritingConstants;
import org.gatein.wsrp.WSRPUtils;
import org.oasis.wsrp.v1.V1BlockingInteractionResponse;
import org.oasis.wsrp.v1.V1CacheControl;
import org.oasis.wsrp.v1.V1ClientData;
import org.oasis.wsrp.v1.V1ClonePortlet;
import org.oasis.wsrp.v1.V1DestroyFailed;
import org.oasis.wsrp.v1.V1DestroyPortlets;
import org.oasis.wsrp.v1.V1DestroyPortletsResponse;
import org.oasis.wsrp.v1.V1GetMarkup;
import org.oasis.wsrp.v1.V1GetPortletDescription;
import org.oasis.wsrp.v1.V1GetPortletProperties;
import org.oasis.wsrp.v1.V1GetPortletPropertyDescription;
import org.oasis.wsrp.v1.V1GetServiceDescription;
import org.oasis.wsrp.v1.V1InitCookie;
import org.oasis.wsrp.v1.V1InteractionParams;
import org.oasis.wsrp.v1.V1LocalizedString;
import org.oasis.wsrp.v1.V1MarkupContext;
import org.oasis.wsrp.v1.V1MarkupParams;
import org.oasis.wsrp.v1.V1MarkupResponse;
import org.oasis.wsrp.v1.V1MarkupType;
import org.oasis.wsrp.v1.V1ModelDescription;
import org.oasis.wsrp.v1.V1ModifyRegistration;
import org.oasis.wsrp.v1.V1PerformBlockingInteraction;
import org.oasis.wsrp.v1.V1PortletContext;
import org.oasis.wsrp.v1.V1PortletDescription;
import org.oasis.wsrp.v1.V1PortletDescriptionResponse;
import org.oasis.wsrp.v1.V1PortletPropertyDescriptionResponse;
import org.oasis.wsrp.v1.V1Property;
import org.oasis.wsrp.v1.V1PropertyDescription;
import org.oasis.wsrp.v1.V1PropertyList;
import org.oasis.wsrp.v1.V1RegistrationContext;
import org.oasis.wsrp.v1.V1RegistrationData;
import org.oasis.wsrp.v1.V1ReleaseSessions;
import org.oasis.wsrp.v1.V1ResetProperty;
import org.oasis.wsrp.v1.V1RuntimeContext;
import org.oasis.wsrp.v1.V1ServiceDescription;
import org.oasis.wsrp.v1.V1SessionContext;
import org.oasis.wsrp.v1.V1SetPortletProperties;
import org.oasis.wsrp.v1.V1StateChange;
import org.oasis.wsrp.v1.V1Templates;
import org.oasis.wsrp.v1.V1UpdateResponse;
import org.oasis.wsrp.v1.V1UploadContext;
import org.oasis.wsrp.v1.V1UserContext;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.gatein.wsrp.WSRPRewritingConstants.*;

/**
 * Creates minimally valid instances of WSRP types, populated with default values where possible, as per
 * wsrp_v1_types.xsd. See <a href="http://jira.jboss.com/jira/browse/JBPORTAL-808">JBPORTAL-808</a> for more
 * information.
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 11317 $
 * @since 2.4
 */
public class WSRP1TypeFactory
{
   private static final String REQUIRE_REWRITE_URL_PARAM = "&" + WSRPRewritingConstants.RESOURCE_REQUIRES_REWRITE + "=" + WSRPRewritingConstants.WSRP_REQUIRES_REWRITE;

   private WSRP1TypeFactory()
   {
   }

   /** ====== WSRP request objects ====== **/
   /**
    * registrationContext(RegistrationContext)?, desiredLocales(xsd:string)*
    *
    * @return
    */
   public static V1GetServiceDescription createGetServiceDescription()
   {
      return new V1GetServiceDescription();
   }

   /**
    * registrationContext(RegistrationContext)?, portletContext(PortletContext), runtimeContext(RuntimeContext),
    * userContext(UserContext)?, markupParams(MarkupParams)
    *
    * @param portletContext
    * @param runtimeContext
    * @param markupParams
    * @return
    * @throws IllegalArgumentException if one of the required parameters is <code>null</code>
    */
   public static V1GetMarkup createMarkupRequest(V1PortletContext portletContext, V1RuntimeContext runtimeContext, V1MarkupParams markupParams)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(runtimeContext, "RuntimeContext");
      ParameterValidation.throwIllegalArgExceptionIfNull(portletContext, "PortletContext");
      ParameterValidation.throwIllegalArgExceptionIfNull(markupParams, "MarkupParams");

      V1GetMarkup getMarkup = new V1GetMarkup();
      getMarkup.setPortletContext(portletContext);
      getMarkup.setRuntimeContext(runtimeContext);
      getMarkup.setMarkupParams(markupParams);
      return getMarkup;
   }

   /**
    * {@link V1RegistrationContext}?, {@link V1PortletContext}, {@link V1RuntimeContext}, {@link V1UserContext}?,
    * {@link
    * V1MarkupParams}, {@link V1InteractionParams}
    *
    * @param portletContext
    * @param runtimeContext
    * @param markupParams
    * @param interactionParams
    * @return
    */
   public static V1PerformBlockingInteraction createPerformBlockingInteraction(
      V1PortletContext portletContext, V1RuntimeContext runtimeContext,
      V1MarkupParams markupParams,
      V1InteractionParams interactionParams)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(portletContext, "PortletContext");
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(portletContext.getPortletHandle(), "portlet handle", "PortletContext");
      ParameterValidation.throwIllegalArgExceptionIfNull(runtimeContext, "RuntimeContext");
      ParameterValidation.throwIllegalArgExceptionIfNull(markupParams, "MarkupParams");
      ParameterValidation.throwIllegalArgExceptionIfNull(interactionParams, "InteractionParams");

      V1PerformBlockingInteraction performBlockingInteraction = new V1PerformBlockingInteraction();
      performBlockingInteraction.setPortletContext(portletContext);
      performBlockingInteraction.setRuntimeContext(runtimeContext);
      performBlockingInteraction.setMarkupParams(markupParams);
      performBlockingInteraction.setInteractionParams(interactionParams);
      return performBlockingInteraction;
   }

   /**
    * registrationContext(RegistrationContext)?, portletContext(PortletContext), userContext(UserContext)?,
    * desiredLocales(xsd:string)*
    *
    * @param registrationContext
    * @param portletHandle       handle for the PortletContext
    * @return
    * @since 2.4.1
    */
   public static V1GetPortletDescription createGetPortletDescription(V1RegistrationContext registrationContext, String portletHandle)
   {
      V1GetPortletDescription description = new V1GetPortletDescription();
      description.setPortletContext(createPortletContext(portletHandle));
      description.setRegistrationContext(registrationContext);
      return description;
   }

   /**
    * @param registrationContext
    * @param portletContext
    * @return
    * @since 2.6
    */
   public static V1GetPortletDescription createGetPortletDescription(V1RegistrationContext registrationContext,
                                                                     org.gatein.pc.api.PortletContext portletContext)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(portletContext, "portlet context");

      V1PortletContext wsrpPC = createPortletContext(portletContext.getId());
      if (portletContext instanceof StatefulPortletContext)
      {
         StatefulPortletContext context = (StatefulPortletContext)portletContext;
         if (PortletStateType.OPAQUE.equals(context.getType()))
         {
            wsrpPC.setPortletState(((StatefulPortletContext<byte[]>)context).getState());
         }
      }

      V1GetPortletDescription getPortletDescription = new V1GetPortletDescription();
      getPortletDescription.setRegistrationContext(registrationContext);
      getPortletDescription.setPortletContext(wsrpPC);
      return getPortletDescription;
   }

   /**
    * registrationContext(RegistrationContext)?, portletContext(PortletContext), userContext(UserContext)?,
    * desiredLocales(xsd:string)*
    *
    * @param registrationContext
    * @param portletContext
    * @return
    * @since 2.4.1
    */
   public static V1GetPortletProperties createGetPortletProperties(V1RegistrationContext registrationContext, V1PortletContext portletContext)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(portletContext, "PortletContext");
      V1GetPortletProperties properties = new V1GetPortletProperties();
      properties.setRegistrationContext(registrationContext);
      properties.setPortletContext(portletContext);
      return properties;
   }

   /** ====== WSRP Response objects ====== **/

   /**
    * ( updateResponse(UpdateResponse) | redirectURL(xsd:string) ), extensions(Extension)*
    *
    * @return
    */
   public static V1BlockingInteractionResponse createBlockingInteractionResponse(V1UpdateResponse updateResponse)
   {
      if (updateResponse == null)
      {
         throw new IllegalArgumentException("BlockingInteractionResponse requires either an UpdateResponse or a redirect URL.");
      }
      V1BlockingInteractionResponse interactionResponse = new V1BlockingInteractionResponse();
      interactionResponse.setUpdateResponse(updateResponse);
      return interactionResponse;
   }

   /**
    * ( updateResponse(UpdateResponse) | redirectURL(xsd:string) ), extensions(Extension)*
    *
    * @return
    */
   public static V1BlockingInteractionResponse createBlockingInteractionResponse(String redirectURL)
   {
      if (redirectURL == null || redirectURL.length() == 0)
      {
         throw new IllegalArgumentException("BlockingInteractionResponse requires either an UpdateResponse or a redirect URL.");
      }
      V1BlockingInteractionResponse interactionResponse = new V1BlockingInteractionResponse();
      interactionResponse.setRedirectURL(redirectURL);
      return interactionResponse;
   }

   /**
    * sessionContext(SessionContext)?, portletContext(PortletContext)?, markupContext(MarkupContext)?,
    * navigationalState(xsd:string)? newWindowState(xsd:string)?, newMode(xsd:string)?
    *
    * @return
    */
   public static V1UpdateResponse createUpdateResponse()
   {
      return new V1UpdateResponse();
   }

   /**
    * portletHandle(xsd:string), markupTypes(MarkupType)+, groupID(xsd:string)?, description(LocalizedString)?,
    * shortTitle(LocalizedString)?, title(LocalizedString)?, displayName(LocalizedString)?, keywords(LocalizedString)*,
    * userCategories(xsd:string)*, userProfileItems(xsd:string)*, usesMethodGet(xsd:boolean[false])?,
    * defaultMarkupSecure(xsd:boolean[false])?, onlySecure(xsd:boolean[false])?, userContextStoredInSession(xsd:boolean[false])?,
    * templatesStoredInSession(xsd:boolean[false])?, hasUserSpecificState(xsd:boolean[false])?,
    * doesUrlTemplateProcessing(xsd:boolean[false])?, extensions(Extension)*
    *
    * @return
    */
   public static V1PortletDescription createPortletDescription(org.gatein.pc.api.PortletContext portletContext, List<V1MarkupType> markupTypes)
   {
      V1PortletContext context = V2ToV1Converter.toV1PortletContext(WSRPUtils.convertToWSRPPortletContext(portletContext));

      ParameterValidation.throwIllegalArgExceptionIfNull(markupTypes, "MarkupType");
      if (markupTypes.isEmpty())
      {
         throw new IllegalArgumentException("Cannot create a PortletDescription with an empty list of MarkupTypes!");
      }

      V1PortletDescription portletDescription = new V1PortletDescription();
      portletDescription.setPortletHandle(context.getPortletHandle());
      portletDescription.getMarkupTypes().addAll(markupTypes);
      return portletDescription;
   }

   public static V1PortletDescription createPortletDescription(String portletHandle, List<V1MarkupType> markupTypes)
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(portletHandle, "portlet handle", null);
      checkPortletHandle(portletHandle);
      ParameterValidation.throwIllegalArgExceptionIfNull(markupTypes, "MarkupType");
      if (markupTypes.isEmpty())
      {
         throw new IllegalArgumentException("Cannot create a PortletDescription with an empty list of MarkupTypes!");
      }

      V1PortletDescription portletDescription = new V1PortletDescription();
      portletDescription.setPortletHandle(portletHandle);
      portletDescription.getMarkupTypes().addAll(markupTypes);
      return portletDescription;
   }

   private static void checkPortletHandle(String portletHandle)
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(portletHandle, "portlet handle", "PortletDescription");
      if (portletHandle.length() > 255)
      {
         throw new IllegalArgumentException("Portlet handles must be less than 255 characters long. Was "
            + portletHandle.length() + " long.");
      }
   }

   /**
    * Same as createMarkupParams(false, {@link WSRPConstants#getDefaultLocales()}, {@link
    * WSRPConstants#getDefaultMimeTypes()}, {@link WSRPConstants#VIEW_MODE}, {@link WSRPConstants#NORMAL_WINDOW_STATE})
    *
    * @return
    */
   public static V1MarkupParams createDefaultMarkupParams()
   {
      return createMarkupParams(false, WSRPConstants.getDefaultLocales(), WSRPConstants.getDefaultMimeTypes(),
         WSRPConstants.VIEW_MODE, WSRPConstants.NORMAL_WINDOW_STATE);
   }

   /**
    * secureClientCommunication(xsd:boolean), locales(xsd:string)+, mimeTypes(xsd:string)+, mode(xsd:string),
    * windowState(xsd:string), clientData({@link V1ClientData})?, navigationalState(xsd:string)?,
    * markupCharacterSets(xsd:string)*, validateTag(xsd:string)?, validNewModes(xsd:string)*,
    * validNewWindowStates(xsd:string)*, extensions({@link V1Extension})*
    *
    * @return
    */
   public static V1MarkupParams createMarkupParams(boolean secureClientCommunication, List<String> locales,
                                                   List<String> mimeTypes, String mode, String windowState)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(locales, "locales");
      if (locales.isEmpty())
      {
         throw new IllegalArgumentException("Cannot create a MarkupParams with an empty list of locales!");
      }

      ParameterValidation.throwIllegalArgExceptionIfNull(mimeTypes, "MIME types");
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(mode, "mode", "MarkupParams");
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(windowState, "window state", "MarkupParams");

      V1MarkupParams markupParams = new V1MarkupParams();
      markupParams.setSecureClientCommunication(secureClientCommunication);
      markupParams.setMode(mode);
      markupParams.setWindowState(windowState);
      if (ParameterValidation.existsAndIsNotEmpty(locales))
      {
         markupParams.getLocales().addAll(locales);
      }
      if (ParameterValidation.existsAndIsNotEmpty(mimeTypes))
      {
         markupParams.getMimeTypes().addAll(mimeTypes);
      }
      return markupParams;
   }

   /**
    * userAuthentication(xsd:string), portletInstanceKey(xsd:string)?, namespacePrefix(xsd:string)?,
    * templates(Templates)?, sessionID(xsd:string)?, extensions(Extension)*
    *
    * @return
    */
   public static V1RuntimeContext createRuntimeContext(String userAuthentication, String portletInstanceKey, String namespacePrefix)
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(userAuthentication, "user authentication", "RuntimeContext");

      V1RuntimeContext runtimeContext = new V1RuntimeContext();
      runtimeContext.setUserAuthentication(userAuthentication);
      runtimeContext.setPortletInstanceKey(portletInstanceKey);
      runtimeContext.setNamespacePrefix(namespacePrefix);
      return runtimeContext;
   }

   /**
    * portletHandle(xsd:string), portletState(xsd:base64Binary)?, extensions({@link V1Extension})*
    *
    * @param portletHandle
    * @return
    */
   public static V1PortletContext createPortletContext(String portletHandle)
   {
      checkPortletHandle(portletHandle);

      V1PortletContext portletContext = new V1PortletContext();
      portletContext.setPortletHandle(portletHandle);
      return portletContext;
   }


   /**
    * @param portletHandle
    * @param portletState
    * @return
    * @since 2.6
    */
   public static V1PortletContext createPortletContext(String portletHandle, byte[] portletState)
   {
      V1PortletContext pc = createPortletContext(portletHandle);
      pc.setPortletState(portletState);
      return pc;
   }

   /**
    * Same as createInteractionParams(StateChange.readOnly)
    *
    * @return
    */
   public static V1InteractionParams createDefaultInteractionParams()
   {
      return createInteractionParams(V1StateChange.READ_ONLY);
   }

   /**
    * portletStateChange({@link V1StateChange}), interactionState(xsd:string)?, formParameters(NamedString)*,
    * uploadContexts(UploadContext)*, extensions(Extension)*
    *
    * @return
    */
   public static V1InteractionParams createInteractionParams(V1StateChange portletStateChange)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(portletStateChange, "portletStateChange");

      V1InteractionParams interactionParams = new V1InteractionParams();
      interactionParams.setPortletStateChange(portletStateChange);
      return interactionParams;
   }

   /**
    * registrationContext(RegistrationContext)?
    *
    * @param registrationContext
    * @return
    */
   public static V1InitCookie createInitCookie(V1RegistrationContext registrationContext)
   {
      V1InitCookie initCookie = new V1InitCookie();
      initCookie.setRegistrationContext(registrationContext);
      return initCookie;
   }

   /**
    * requiresRegistration(xsd:boolean), offeredPortlets(PortletDescription)*, userCategoryDescriptions(ItemDescription)*,
    * customUserProfileItemDescriptions(ItemDescription)*, customWindowStateDescriptions(ItemDescription)*,
    * customModeDescriptions(ItemDescription)*, requiresInitCookie(CookieProtocol[none])?,
    * registrationPropertyDescription(ModelDescription)?, locales(xsd:string)*, resourceList(ResourceList)?,
    * extensions(Extension)*
    *
    * @return
    */
   public static V1ServiceDescription createServiceDescription(boolean requiresRegistration)
   {
      V1ServiceDescription serviceDescription = new V1ServiceDescription();
      serviceDescription.setRequiresRegistration(requiresRegistration);
      return serviceDescription;
   }

   /**
    * markupContext(MarkupContext), sessionContext(SessionContext)?, extensions(Extension)*
    *
    * @return
    */
   public static V1MarkupResponse createMarkupResponse(V1MarkupContext markupContext)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(markupContext, "MarkupContext");

      V1MarkupResponse markupResponse = new V1MarkupResponse();
      markupResponse.setMarkupContext(markupContext);
      return markupResponse;
   }

   /**
    * mimeType: The mime type of the returned markup. The mimeType field MUST be specified whenever markup is returned,
    * and if the markupBinary field is used to return the markup, the mime type MUST include the character set for
    * textual mime types using the syntax specified in RFC1522[14] (e.g. "text/html; charset=UTF-8"). In this
    * particular
    * case this character set MAY be different than the response message.
    * <p/>
    * useCachedMarkup(xsd:boolean[false])?, mimeType(xsd:string)?, (markupString(xsd:string) |
    * markupBinary(xsd:base64Binary)), locale(xsd:string)?, requiresUrlRewriting(xsd:boolean[false])?,
    * cacheControl(CacheControl)?, preferredTitle(xsd:string)?, extensions(Extension)*
    *
    * @return
    */
   public static V1MarkupContext createMarkupContext(String mediaType, String markupString, byte[] markupBinary, Boolean useCacheItem)
   {
      boolean isUseCacheItem = (useCacheItem == null) ? false : useCacheItem.booleanValue();

      V1MarkupContext markupContext = new V1MarkupContext();
      markupContext.setMimeType(mediaType);

      if (isUseCacheItem)
      {
         markupContext.setUseCachedMarkup(useCacheItem);
      }
      else
      {
         ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(mediaType, "MIME type", "MarkupContext");
         if (markupBinary != null)
         {
            markupContext.setMarkupBinary(markupBinary);
         }
         else if (markupString != null)
         {
            markupContext.setMarkupString(markupString);
         }
         else
         {
            throw new IllegalArgumentException("MarkupContext required either a true useCacheItem or a non-null markup string or binary markup");
         }
      }

      return markupContext;
   }

   /**
    * sessionID(xsd:string), expires(xsd:int), extensions(Extension)*
    *
    * @param sessionID An opaque string the Portlet defines for referencing state that is stored locally on the
    *                  Producer. The maximum length of a sessionID is 4096 characters,
    * @param expires   Maximum number of seconds between invocations referencing the sessionID before the Producer will
    *                  schedule releasing the related resources. A value of -1 indicates that the sessionID will never
    *                  expire.
    * @return a new SessionContext
    */
   public static V1SessionContext createSessionContext(String sessionID, int expires)
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(sessionID, "session Id", "SessionContext");
      if (expires < 0)
      {
         throw new IllegalArgumentException("SessionContext requires a positive expiration time.");
      }
      V1SessionContext sessionContext = new V1SessionContext();
      sessionContext.setSessionID(sessionID);
      sessionContext.setExpires(expires);
      return sessionContext;
   }

   /**
    * For UserProfile and related classes, everything is optional so no need to have factory methods.
    * <p/>
    * userContextKey(xsd:string), userCategories(xsd:string)*, profile(UserProfile)?, extensions(Extension)*
    *
    * @return
    */
   public static V1UserContext createUserContext(String userContextKey)
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(userContextKey, "user context key", "UserContext");
      V1UserContext userContext = new V1UserContext();
      userContext.setUserContextKey(userContextKey);
      return userContext;
   }

   /**
    * consumerName(xsd:string), consumerAgent(xsd:string), methodGetSupported(xsd:boolean), consumerModes(xsd:string)*,
    * consumerWindowStates(xsd:string)*, consumerUserScopes(xsd:string)*, customUserProfileData(xsd:string)*,
    * registrationProperties(Property)*, extensions(Extension)*
    *
    * @param consumerName       A name (preferably unique) that identifies the Consumer [R355] An example of such a
    *                           name
    *                           would be the Consumer's URL.
    * @param methodGetSupported A flag that tells the Producer whether the Consumer has implemented portlet URLs
    *                           (regardless of whether they are written through Consumer URL rewriting or Producer URL
    *                           writing, see [Section 10.2]) in a manner that supports HTML markup containing forms
    *                           with
    *                           method="get".
    * @return
    */
   public static V1RegistrationData createRegistrationData(String consumerName, boolean methodGetSupported)
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(consumerName, "consumer name", "RegistrationData");
      V1RegistrationData regData = createDefaultRegistrationData();
      regData.setConsumerName(consumerName);
      regData.setMethodGetSupported(methodGetSupported);
      return regData;
   }

   /**
    * Same as createRegistrationData({@link WSRPConstants#DEFAULT_CONSUMER_NAME}, false) using {@link
    * WSRPConstants#CONSUMER_AGENT} for the consumer agent.
    *
    * @return
    * @since 2.4.1
    */
   public static V1RegistrationData createDefaultRegistrationData()
   {
      V1RegistrationData registrationData = new V1RegistrationData();
      registrationData.setConsumerName(WSRPConstants.DEFAULT_CONSUMER_NAME);
      registrationData.setConsumerAgent(WSRPConstants.CONSUMER_AGENT);
      registrationData.setMethodGetSupported(false);
      return registrationData;
   }

   /**
    * ( stringValue(xsd:string) | any* ), @name(xsd:string), @xml:lang
    *
    * @return
    */
   public static V1Property createProperty(String name, String lang, String stringValue)
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(name, "name", "Property");
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(stringValue, "String value", "Property");

      V1Property property = new V1Property();
      property.setName(name);
      if (!ParameterValidation.isNullOrEmpty(lang))
      {
         property.setLang(lang);
      }
      property.setStringValue(stringValue);
      return property;
   }

   private static final ActionURL ACTION_URL = new ActionURL()
   {
      public StateString getInteractionState()
      {
         return new OpaqueStateString(REWRITE_PARAMETER_OPEN + INTERACTION_STATE + REWRITE_PARAMETER_CLOSE);
      }

      public StateString getNavigationalState()
      {
         return getTemplateNS();
      }

      public Mode getMode()
      {
         return getTemplateMode();
      }

      public WindowState getWindowState()
      {
         return getTemplateWindowState();
      }

      public Map<String, String> getProperties()
      {
         return Collections.emptyMap();
      }
   };

   private static final RenderURL RENDER_URL = new RenderURL()
   {
      public StateString getNavigationalState()
      {
         return getTemplateNS();
      }

      public Map<String, String[]> getPublicNavigationalStateChanges()
      {
         // todo: implement properly
         return null;
      }

      public Mode getMode()
      {
         return getTemplateMode();
      }

      public WindowState getWindowState()
      {
         return getTemplateWindowState();
      }

      public Map<String, String> getProperties()
      {
         return Collections.emptyMap();
      }
   };

   private static ResourceURL RESOURCE_URL = new WSRPResourceURL()
   {
      public String getResourceId()
      {
         return REWRITE_PARAMETER_OPEN + WSRPRewritingConstants.RESOURCE_URL + REWRITE_PARAMETER_CLOSE;
      }

      public StateString getResourceState()
      {
         // todo: fix-me
         return null;
      }

      public CacheLevel getCacheability()
      {
         // todo: fix-me
         return null;
      }

      public Mode getMode()
      {
         return getTemplateMode();
      }

      public WindowState getWindowState()
      {
         return getTemplateWindowState();
      }

      public StateString getNavigationalState()
      {
         return getTemplateNS();
      }
   };

   private static StateString getTemplateNS()
   {
      return new OpaqueStateString(REWRITE_PARAMETER_OPEN + NAVIGATIONAL_STATE + REWRITE_PARAMETER_CLOSE);
   }

   private static WindowState getTemplateWindowState()
   {
      return WindowState.create(REWRITE_PARAMETER_OPEN + WINDOW_STATE + REWRITE_PARAMETER_CLOSE, true);
   }

   private static Mode getTemplateMode()
   {
      return Mode.create(REWRITE_PARAMETER_OPEN + MODE + REWRITE_PARAMETER_CLOSE, true);
   }


   /**
    * defaultTemplate(xsd:string)?, blockingActionTemplate(xsd:string)?, renderTemplate(xsd:string)?,
    * resourceTemplate(xsd:string)?, secureDefaultTemplate(xsd:string)?, secureBlockingActionTemplate(xsd:string)?,
    * secureRenderTemplate(xsd:string)?, secureResourceTemplate(xsd:string)?, extensions(Extension)*
    *
    * @param context
    * @return
    */
   public static V1Templates createTemplates(PortletInvocationContext context)
   {
      V1Templates templates = new V1Templates();

      templates.setBlockingActionTemplate(createTemplate(context, ACTION_URL, Boolean.FALSE));
      templates.setRenderTemplate(createTemplate(context, RENDER_URL, Boolean.FALSE));
      templates.setSecureBlockingActionTemplate(createTemplate(context, ACTION_URL, Boolean.TRUE));
      templates.setSecureRenderTemplate(createTemplate(context, RENDER_URL, Boolean.TRUE));

      //fix-me: deal with resources properly, create fake ones for now
      templates.setResourceTemplate(createTemplate(context, RESOURCE_URL, false));
      templates.setSecureResourceTemplate(createTemplate(context, RESOURCE_URL, true));

      return templates;
   }

   private static String createTemplate(PortletInvocationContext context, ContainerURL url, Boolean secure)
   {
      String template = context.renderURL(url, new URLFormat(secure, null, null, true));
      template = TextTools.replace(template, WSRPRewritingConstants.ENC_OPEN, WSRPRewritingConstants.REWRITE_PARAMETER_OPEN);
      template = TextTools.replace(template, WSRPRewritingConstants.ENC_CLOSE, WSRPRewritingConstants.REWRITE_PARAMETER_CLOSE);

      // fix for GTNWSRP-22
      if (RESOURCE_URL == url)
      {
         template += REQUIRE_REWRITE_URL_PARAM;
      }

      return template;
   }

   /**
    * userAgent(xsd:string)?, extensions(Extension)*
    *
    * @param userAgent
    * @return
    */
   public static V1ClientData createClientData(String userAgent)
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(userAgent, "user agent", "ClientData");
      V1ClientData clientData = new V1ClientData();
      clientData.setUserAgent(userAgent);
      return clientData;
   }

   /**
    * expires(xsd:int), userScope(xsd:string), validateTag(xsd:string)?, extensions(Extension)*
    *
    * @param expires   Number of seconds the markup fragment referenced by this cache control entry remains valid. A
    *                  value of -1 indicates that the markup fragment will never expire.
    * @param userScope
    * @return
    */
   public static V1CacheControl createCacheControl(int expires, String userScope)
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(userScope, "user scope", "CacheControl");
      if (expires < -1)
      {
         throw new IllegalArgumentException("Cache expiration time must be greater than -1, " +
            "-1 indicating that the cache will never expire.");
      }

      V1CacheControl cacheControl = new V1CacheControl();
      cacheControl.setExpires(expires);
      cacheControl.setUserScope(userScope);
      return cacheControl;
   }

   /**
    * registrationHandle(xsd:string), registrationState(xsd:base64Binary)?, extensions(Extension)*
    *
    * @param registrationHandle
    * @return
    * @since 2.4.1
    */
   public static V1RegistrationContext createRegistrationContext(String registrationHandle)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(registrationHandle, "Registration handle");
      V1RegistrationContext registrationContext = new V1RegistrationContext();
      registrationContext.setRegistrationHandle(registrationHandle);
      return registrationContext;
   }

   /**
    * propertyDescriptions(PropertyDescription)*, modelTypes(ModelTypes)?, extensions(Extension)*
    *
    * @return
    * @since 2.6
    */
   public static V1ModelDescription createModelDescription(List<V1PropertyDescription> propertyDescriptions)
   {
      V1ModelDescription description = new V1ModelDescription();
      if (ParameterValidation.existsAndIsNotEmpty(propertyDescriptions))
      {
         description.getPropertyDescriptions().addAll(propertyDescriptions);
      }
      return description;
   }

   /**
    * label(LocalizedString)?, hint(LocalizedString)?, extensions(Extension)*, @name(xsd:string), @type(xsd:QName)
    *
    * @return
    * @since 2.6
    */
   public static V1PropertyDescription createPropertyDescription(String name, QName type)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(name, "PropertyDescription name");
      ParameterValidation.throwIllegalArgExceptionIfNull(type, "PropertyDescription type");
      V1PropertyDescription description = new V1PropertyDescription();
      description.setName(name);
      description.setType(type);
      return description;
   }

   /**
    * value(xsd:string), @xml:lang, @resourceName(xsd:string)?
    *
    * @return
    * @since 2.6
    */
   public static V1LocalizedString createLocalizedString(String lang, String resourceName, String value)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(lang, "LocalizedString language");
      ParameterValidation.throwIllegalArgExceptionIfNull(value, "LocalizedString value");
      V1LocalizedString localizedString = new V1LocalizedString();
      localizedString.setLang(lang);
      localizedString.setResourceName(resourceName);
      localizedString.setValue(value);
      return localizedString;
   }

   /**
    * Same as createLocalizedString("en", null, value)
    *
    * @param value
    * @return
    * @since 2.6
    */
   public static V1LocalizedString createLocalizedString(String value)
   {
      return createLocalizedString("en", null, value);
   }

   /**
    * portletDescription(PortletDescription), resourceList(ResourceList)?, extensions(Extension)*
    *
    * @param portletDescription
    * @return
    * @since 2.6
    */
   public static V1PortletDescriptionResponse createPortletDescriptionResponse(V1PortletDescription portletDescription)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(portletDescription, "PortletDescription");
      V1PortletDescriptionResponse response = new V1PortletDescriptionResponse();
      response.setPortletDescription(portletDescription);
      return response;
   }

   /**
    * modelDescription(ModelDescription)?, resourceList(ResourceList)?, extensions(Extension)*
    *
    * @param propertyDescriptions
    * @return
    * @since 2.6
    */
   public static V1PortletPropertyDescriptionResponse createPortletPropertyDescriptionResponse(List<V1PropertyDescription> propertyDescriptions)
   {
      V1ModelDescription modelDescription = propertyDescriptions == null ? null : createModelDescription(propertyDescriptions);
      V1PortletPropertyDescriptionResponse portletPropertyDescriptionResponse = new V1PortletPropertyDescriptionResponse();
      portletPropertyDescriptionResponse.setModelDescription(modelDescription);
      return portletPropertyDescriptionResponse;
   }

   /**
    * registrationContext(RegistrationContext)?, portletContext(PortletContext), userContext(UserContext)?,
    * desiredLocales(xsd:string)*
    *
    * @return
    * @since 2.6
    */
   public static V1GetPortletPropertyDescription createGetPortletPropertyDescription(V1RegistrationContext registrationContext,
                                                                                     V1PortletContext portletContext,
                                                                                     V1UserContext userContext, List<String> desiredLocales)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(portletContext, "PortletContext");
      V1GetPortletPropertyDescription description = new V1GetPortletPropertyDescription();
      description.setRegistrationContext(registrationContext);
      description.setPortletContext(portletContext);
      description.setUserContext(userContext);
      if (ParameterValidation.existsAndIsNotEmpty(desiredLocales))
      {
         description.getDesiredLocales().addAll(desiredLocales);
      }
      return description;
   }

   /**
    * Same as createGetPortletPropertyDescription(null, createPortletContext(portletHandle), null, null)
    *
    * @param portletHandle
    * @return
    * @since 2.6
    */
   public static V1GetPortletPropertyDescription createSimpleGetPortletPropertyDescription(String portletHandle)
   {
      return createGetPortletPropertyDescription(null, createPortletContext(portletHandle), null, null);
   }


   /**
    * portletHandle(xsd:string), reason(xsd:string)
    *
    * @param portletHandle
    * @param reason
    * @return
    * @since 2.6
    */
   public static V1DestroyFailed createDestroyFailed(String portletHandle, String reason)
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(portletHandle, "Portlet handle", "DestroyFailed");
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(reason, "Reason for failure", "DestroyFailed");
      // todo: check reason should be a fault code from Section 13 of spec but this is not clear...
      V1DestroyFailed destroyFailed = new V1DestroyFailed();
      destroyFailed.setPortletHandle(portletHandle);
      destroyFailed.setReason(reason);
      return destroyFailed;
   }

   /**
    * destroyFailed(DestroyFailed)*, extensions(Extension)*
    *
    * @param destroyFailed
    * @return
    * @since 2.6
    */
   public static V1DestroyPortletsResponse createDestroyPortletsResponse(List<V1DestroyFailed> destroyFailed)
   {
      V1DestroyPortletsResponse destroyPortletsResponse = new V1DestroyPortletsResponse();
      if (ParameterValidation.existsAndIsNotEmpty(destroyFailed))
      {
         destroyPortletsResponse.getDestroyFailed().addAll(destroyFailed);
      }
      return destroyPortletsResponse;
   }

   /**
    * registrationContext(RegistrationContext)?, portletContext(PortletContext), userContext(UserContext)?,
    * propertyList(PropertyList)
    *
    * @param portletContext
    * @param propertyList
    * @return
    * @since 2.6
    */
   public static V1SetPortletProperties createSetPortletProperties(
      V1RegistrationContext registrationContext,
      V1PortletContext portletContext,
      V1PropertyList propertyList)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(portletContext, "PortletContext");
      ParameterValidation.throwIllegalArgExceptionIfNull(propertyList, "PropertyList");

      V1SetPortletProperties properties = new V1SetPortletProperties();
      properties.setRegistrationContext(registrationContext);
      properties.setPortletContext(portletContext);
      properties.setPropertyList(propertyList);
      return properties;
   }

   /**
    * same as createClonePortlet(null, createPortletContext(portletHandle), null)
    *
    * @param portletHandle
    * @return
    * @since 2.6
    */
   public static V1ClonePortlet createSimpleClonePortlet(String portletHandle)
   {
      return createClonePortlet(null, createPortletContext(portletHandle), null);
   }

   /**
    * registrationContext(RegistrationContext)?, portletContext(PortletContext), userContext(UserContext)?
    *
    * @return
    * @since 2.6
    */
   public static V1ClonePortlet createClonePortlet(V1RegistrationContext registrationContext, V1PortletContext portletContext, V1UserContext userContext)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(portletContext, "PortletContext");
      V1ClonePortlet clonePortlet = new V1ClonePortlet();
      clonePortlet.setPortletContext(portletContext);
      clonePortlet.setRegistrationContext(registrationContext);
      clonePortlet.setUserContext(userContext);
      return clonePortlet;
   }

   /**
    * registrationContext(RegistrationContext)?, portletHandles(xsd:string)+
    *
    * @return
    * @since 2.6
    */
   public static V1DestroyPortlets createDestroyPortlets(V1RegistrationContext registrationContext, List<String> portletHandles)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(portletHandles, "Portlet handles");
      if (portletHandles.isEmpty())
      {
         throw new IllegalArgumentException("Cannot create a DestroyPortlets with an empty list of portlet handles!");
      }

      V1DestroyPortlets destroyPortlets = new V1DestroyPortlets();
      destroyPortlets.setRegistrationContext(registrationContext);
      if (ParameterValidation.existsAndIsNotEmpty(portletHandles))
      {
         destroyPortlets.getPortletHandles().addAll(portletHandles);
      }
      return destroyPortlets;
   }

   /**
    * properties(Property)*, resetProperties(ResetProperty)*, extensions(Extension)*
    *
    * @return
    * @since 2.6
    */
   public static V1PropertyList createPropertyList()
   {
      return new V1PropertyList();
   }

   /**
    * EMPTY, @name(xsd:string)
    *
    * @param name
    * @return
    * @since 2.6
    */
   public static V1ResetProperty createResetProperty(String name)
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(name, "Property name", "ResetProperty");

      V1ResetProperty resetProperty = new V1ResetProperty();
      resetProperty.setName(name);
      return resetProperty;
   }

   /**
    * registrationContext(RegistrationContext)?, sessionIDs(xsd:string)*
    *
    * @return
    * @since 2.6
    */
   public static V1ReleaseSessions createReleaseSessions(V1RegistrationContext registrationContext, List<String> sessionIDs)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(sessionIDs, "Session IDs");
      if (sessionIDs.isEmpty())
      {
         throw new IllegalArgumentException("Cannot create a ReleaseSessions with an empty list of session IDs!");
      }

      V1ReleaseSessions sessions = new V1ReleaseSessions();
      sessions.setRegistrationContext(registrationContext);
      if (ParameterValidation.existsAndIsNotEmpty(sessionIDs))
      {
         sessions.getSessionIDs().addAll(sessionIDs);
      }
      return sessions;
   }

   /**
    * registrationContext(RegistrationContext)?, registrationData(RegistrationData)
    *
    * @return
    * @since 2.6
    */
   public static V1ModifyRegistration createModifyRegistration(V1RegistrationContext registrationContext,
                                                               V1RegistrationData registrationData)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(registrationData, "RegistrationData");

      V1ModifyRegistration registration = new V1ModifyRegistration();
      registration.setRegistrationContext(registrationContext);
      registration.setRegistrationData(registrationData);
      return registration;
   }

   /**
    * mimeType(xsd:string), uploadData(xsd:base64Binary), mimeAttributes(NamedString)*, extensions(Extension)*
    *
    * @param mimeType
    * @param uploadData
    * @return
    * @since 2.6.2
    */
   public static V1UploadContext createUploadContext(String mimeType, byte[] uploadData)
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(mimeType, "MIME Type", "UploadContext");
      if (uploadData == null || uploadData.length == 0)
      {
         throw new IllegalArgumentException("Must pass non-null, non-empty upload data");
      }

      V1UploadContext uploadContext = new V1UploadContext();
      uploadContext.setMimeType(mimeType);
      uploadContext.setUploadData(uploadData);
      return uploadContext;
   }

   public static V1MarkupType createMarkupType(String mimeType, List<String> modeNames, List<String> windowStateNames, List<String> localeNames)
   {
      V1MarkupType markupType = new V1MarkupType();
      markupType.setMimeType(mimeType);

      if (ParameterValidation.existsAndIsNotEmpty(modeNames))
      {
         markupType.getModes().addAll(modeNames);
      }
      if (ParameterValidation.existsAndIsNotEmpty(windowStateNames))
      {
         markupType.getWindowStates().addAll(windowStateNames);
      }
      if (ParameterValidation.existsAndIsNotEmpty(localeNames))
      {
         markupType.getLocales().addAll(localeNames);
      }
      return markupType;
   }
}
