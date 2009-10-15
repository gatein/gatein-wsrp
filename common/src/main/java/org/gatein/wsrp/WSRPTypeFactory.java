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

package org.gatein.wsrp;

import org.gatein.common.util.ParameterValidation;
import org.gatein.common.util.Tools;
import org.gatein.pc.api.ActionURL;
import org.gatein.pc.api.Mode;
import org.gatein.pc.api.OpaqueStateString;
import org.gatein.pc.api.PortletStateType;
import org.gatein.pc.api.PortletURL;
import org.gatein.pc.api.RenderURL;
import org.gatein.pc.api.StateString;
import org.gatein.pc.api.StatefulPortletContext;
import org.gatein.pc.api.URLFormat;
import org.gatein.pc.api.WindowState;
import org.gatein.pc.api.spi.PortletInvocationContext;
import org.oasis.wsrp.v1.BlockingInteractionResponse;
import org.oasis.wsrp.v1.CacheControl;
import org.oasis.wsrp.v1.ClientData;
import org.oasis.wsrp.v1.ClonePortlet;
import org.oasis.wsrp.v1.DestroyFailed;
import org.oasis.wsrp.v1.DestroyPortlets;
import org.oasis.wsrp.v1.DestroyPortletsResponse;
import org.oasis.wsrp.v1.GetMarkup;
import org.oasis.wsrp.v1.GetPortletDescription;
import org.oasis.wsrp.v1.GetPortletProperties;
import org.oasis.wsrp.v1.GetPortletPropertyDescription;
import org.oasis.wsrp.v1.GetServiceDescription;
import org.oasis.wsrp.v1.InitCookie;
import org.oasis.wsrp.v1.InteractionParams;
import org.oasis.wsrp.v1.LocalizedString;
import org.oasis.wsrp.v1.MarkupContext;
import org.oasis.wsrp.v1.MarkupParams;
import org.oasis.wsrp.v1.MarkupResponse;
import org.oasis.wsrp.v1.MarkupType;
import org.oasis.wsrp.v1.ModelDescription;
import org.oasis.wsrp.v1.ModifyRegistration;
import org.oasis.wsrp.v1.PerformBlockingInteraction;
import org.oasis.wsrp.v1.PortletContext;
import org.oasis.wsrp.v1.PortletDescription;
import org.oasis.wsrp.v1.PortletDescriptionResponse;
import org.oasis.wsrp.v1.PortletPropertyDescriptionResponse;
import org.oasis.wsrp.v1.Property;
import org.oasis.wsrp.v1.PropertyDescription;
import org.oasis.wsrp.v1.PropertyList;
import org.oasis.wsrp.v1.RegistrationContext;
import org.oasis.wsrp.v1.RegistrationData;
import org.oasis.wsrp.v1.ReleaseSessions;
import org.oasis.wsrp.v1.ResetProperty;
import org.oasis.wsrp.v1.RuntimeContext;
import org.oasis.wsrp.v1.ServiceDescription;
import org.oasis.wsrp.v1.SessionContext;
import org.oasis.wsrp.v1.SetPortletProperties;
import org.oasis.wsrp.v1.StateChange;
import org.oasis.wsrp.v1.Templates;
import org.oasis.wsrp.v1.UpdateResponse;
import org.oasis.wsrp.v1.UploadContext;
import org.oasis.wsrp.v1.UserContext;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.Map;

/**
 * Creates minimally valid instances of WSRP types, populated with default values where possible, as per
 * wsrp_v1_types.xsd. See <a href="http://jira.jboss.com/jira/browse/JBPORTAL-808">JBPORTAL-808</a> for more
 * information.
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 11317 $
 * @since 2.4
 */
public class WSRPTypeFactory
{
   private WSRPTypeFactory()
   {
   }

   /** ====== WSRP request objects ====== **/
   /**
    * registrationContext(RegistrationContext)?, desiredLocales(xsd:string)*
    *
    * @return
    */
   public static GetServiceDescription createGetServiceDescription()
   {
      return new GetServiceDescription();
   }

   /**
    * Same as createMarkupRequest(handle, createDefaultRuntimeContext(), createDefaultMarkupParams())
    *
    * @param handle
    * @return
    */
   public static GetMarkup createDefaultMarkupRequest(String handle)
   {
      return createMarkupRequest(createPortletContext(handle), createDefaultRuntimeContext(), createDefaultMarkupParams());
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
   public static GetMarkup createMarkupRequest(PortletContext portletContext, RuntimeContext runtimeContext, MarkupParams markupParams)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(runtimeContext, "RuntimeContext");
      ParameterValidation.throwIllegalArgExceptionIfNull(portletContext, "PortletContext");
      ParameterValidation.throwIllegalArgExceptionIfNull(markupParams, "MarkupParams");

      GetMarkup getMarkup = new GetMarkup();
      getMarkup.setPortletContext(portletContext);
      getMarkup.setRuntimeContext(runtimeContext);
      getMarkup.setMarkupParams(markupParams);
      return getMarkup;
   }

   /**
    * Same as createPerformBlockingInteraction(portletHandle, {@link #createDefaultRuntimeContext}(), {@link
    * #createDefaultMarkupParams}(), {@link #createDefaultInteractionParams}());
    *
    * @param portletHandle
    * @return
    */
   public static PerformBlockingInteraction createDefaultPerformBlockingInteraction(String portletHandle)
   {
      return createPerformBlockingInteraction(createPortletContext(portletHandle), createDefaultRuntimeContext(), createDefaultMarkupParams(),
         createDefaultInteractionParams());
   }

   /**
    * {@link RegistrationContext}?, {@link PortletContext}, {@link RuntimeContext}, {@link UserContext}?, {@link
    * MarkupParams}, {@link InteractionParams}
    *
    * @param portletContext
    * @param runtimeContext
    * @param markupParams
    * @param interactionParams
    * @return
    */
   public static PerformBlockingInteraction createPerformBlockingInteraction(
      PortletContext portletContext, RuntimeContext runtimeContext,
      MarkupParams markupParams,
      InteractionParams interactionParams)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(portletContext, "PortletContext");
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(portletContext.getPortletHandle(), "portlet handle", "PortletContext");
      ParameterValidation.throwIllegalArgExceptionIfNull(runtimeContext, "RuntimeContext");
      ParameterValidation.throwIllegalArgExceptionIfNull(markupParams, "MarkupParams");
      ParameterValidation.throwIllegalArgExceptionIfNull(interactionParams, "InteractionParams");

      PerformBlockingInteraction performBlockingInteraction = new PerformBlockingInteraction();
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
   public static GetPortletDescription createGetPortletDescription(RegistrationContext registrationContext, String portletHandle)
   {
      GetPortletDescription description = new GetPortletDescription();
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
   public static GetPortletDescription createGetPortletDescription(RegistrationContext registrationContext,
                                                                   org.gatein.pc.api.PortletContext portletContext)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(portletContext, "portlet context");

      PortletContext wsrpPC = createPortletContext(portletContext.getId());
      if (portletContext instanceof StatefulPortletContext)
      {
         StatefulPortletContext context = (StatefulPortletContext)portletContext;
         if (PortletStateType.OPAQUE.equals(context.getType()))
         {
            wsrpPC.setPortletState(((StatefulPortletContext<byte[]>)context).getState());
         }
      }

      GetPortletDescription getPortletDescription = new GetPortletDescription();
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
   public static GetPortletProperties createGetPortletProperties(RegistrationContext registrationContext, PortletContext portletContext)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(portletContext, "PortletContext");
      GetPortletProperties properties = new GetPortletProperties();
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
   public static BlockingInteractionResponse createBlockingInteractionResponse(UpdateResponse updateResponse)
   {
      if (updateResponse == null)
      {
         throw new IllegalArgumentException("BlockingInteractionResponse requires either an UpdateResponse or a redirect URL.");
      }
      BlockingInteractionResponse interactionResponse = new BlockingInteractionResponse();
      interactionResponse.setUpdateResponse(updateResponse);
      return interactionResponse;
   }

   /**
    * ( updateResponse(UpdateResponse) | redirectURL(xsd:string) ), extensions(Extension)*
    *
    * @return
    */
   public static BlockingInteractionResponse createBlockingInteractionResponse(String redirectURL)
   {
      if (redirectURL == null || redirectURL.length() == 0)
      {
         throw new IllegalArgumentException("BlockingInteractionResponse requires either an UpdateResponse or a redirect URL.");
      }
      BlockingInteractionResponse interactionResponse = new BlockingInteractionResponse();
      interactionResponse.setRedirectURL(redirectURL);
      return interactionResponse;
   }

   /**
    * sessionContext(SessionContext)?, portletContext(PortletContext)?, markupContext(MarkupContext)?,
    * navigationalState(xsd:string)? newWindowState(xsd:string)?, newMode(xsd:string)?
    *
    * @return
    */
   public static UpdateResponse createUpdateResponse()
   {
      return new UpdateResponse();
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
   public static PortletDescription createPortletDescription(org.gatein.pc.api.PortletContext portletContext, List<MarkupType> markupTypes)
   {
      PortletContext context = WSRPUtils.convertToWSRPPortletContext(portletContext);

      ParameterValidation.throwIllegalArgExceptionIfNull(markupTypes, "MarkupType");
      if (markupTypes.isEmpty())
      {
         throw new IllegalArgumentException("Cannot create a PortletDescription with an empty list of MarkupTypes!");
      }

      PortletDescription portletDescription = new PortletDescription();
      portletDescription.setPortletHandle(context.getPortletHandle());
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
   public static MarkupParams createDefaultMarkupParams()
   {
      return createMarkupParams(false, WSRPConstants.getDefaultLocales(), WSRPConstants.getDefaultMimeTypes(),
         WSRPConstants.VIEW_MODE, WSRPConstants.NORMAL_WINDOW_STATE);
   }

   /**
    * secureClientCommunication(xsd:boolean), locales(xsd:string)+, mimeTypes(xsd:string)+, mode(xsd:string),
    * windowState(xsd:string), clientData({@link ClientData})?, navigationalState(xsd:string)?,
    * markupCharacterSets(xsd:string)*, validateTag(xsd:string)?, validNewModes(xsd:string)*,
    * validNewWindowStates(xsd:string)*, extensions({@link Extension})*
    *
    * @return
    */
   public static MarkupParams createMarkupParams(boolean secureClientCommunication, List<String> locales,
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

      MarkupParams markupParams = new MarkupParams();
      markupParams.setSecureClientCommunication(secureClientCommunication);
      markupParams.setMode(mode);
      markupParams.setWindowState(windowState);
      if (WSRPUtils.existsAndIsNotEmpty(locales))
      {
         markupParams.getLocales().addAll(locales);
      }
      if (WSRPUtils.existsAndIsNotEmpty(mimeTypes))
      {
         markupParams.getMimeTypes().addAll(mimeTypes);
      }
      return markupParams;
   }

   /**
    * Same as createRuntimeContext({@link WSRPConstants#NONE_USER_AUTHENTICATION})
    *
    * @return
    */
   public static RuntimeContext createDefaultRuntimeContext()
   {
      return createRuntimeContext(WSRPConstants.NONE_USER_AUTHENTICATION);
   }

   /**
    * userAuthentication(xsd:string), portletInstanceKey(xsd:string)?, namespacePrefix(xsd:string)?,
    * templates(Templates)?, sessionID(xsd:string)?, extensions(Extension)*
    *
    * @return
    */
   public static RuntimeContext createRuntimeContext(String userAuthentication)
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(userAuthentication, "user authentication", "RuntimeContext");

      RuntimeContext runtimeContext = new RuntimeContext();
      runtimeContext.setUserAuthentication(userAuthentication);
      return runtimeContext;
   }

   /**
    * portletHandle(xsd:string), portletState(xsd:base64Binary)?, extensions({@link Extension})*
    *
    * @param portletHandle
    * @return
    */
   public static PortletContext createPortletContext(String portletHandle)
   {
      checkPortletHandle(portletHandle);

      PortletContext portletContext = new PortletContext();
      portletContext.setPortletHandle(portletHandle);
      return portletContext;
   }


   /**
    * @param portletHandle
    * @param portletState
    * @return
    * @since 2.6
    */
   public static PortletContext createPortletContext(String portletHandle, byte[] portletState)
   {
      PortletContext pc = createPortletContext(portletHandle);
      pc.setPortletState(portletState);
      return pc;
   }

   /**
    * Same as createInteractionParams(StateChange.readOnly)
    *
    * @return
    */
   public static InteractionParams createDefaultInteractionParams()
   {
      return createInteractionParams(StateChange.READ_ONLY);
   }

   /**
    * portletStateChange({@link StateChange}), interactionState(xsd:string)?, formParameters(NamedString)*,
    * uploadContexts(UploadContext)*, extensions(Extension)*
    *
    * @return
    */
   public static InteractionParams createInteractionParams(StateChange portletStateChange)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(portletStateChange, "portletStateChange");

      InteractionParams interactionParams = new InteractionParams();
      interactionParams.setPortletStateChange(portletStateChange);
      return interactionParams;
   }

   /**
    * registrationContext(RegistrationContext)?
    *
    * @param registrationContext
    * @return
    */
   public static InitCookie createInitCookie(RegistrationContext registrationContext)
   {
      InitCookie initCookie = new InitCookie();
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
   public static ServiceDescription createServiceDescription(boolean requiresRegistration)
   {
      ServiceDescription serviceDescription = new ServiceDescription();
      serviceDescription.setRequiresRegistration(requiresRegistration);
      return serviceDescription;
   }

   /**
    * markupContext(MarkupContext), sessionContext(SessionContext)?, extensions(Extension)*
    *
    * @return
    */
   public static MarkupResponse createMarkupResponse(MarkupContext markupContext)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(markupContext, "MarkupContext");

      MarkupResponse markupResponse = new MarkupResponse();
      markupResponse.setMarkupContext(markupContext);
      return markupResponse;
   }

   /**
    * mimeType: The mime type of the returned markup. The mimeType field MUST be specified whenever markup is returned,
    * and if the markupBinary field is used to return the markup, the mime type MUST include the character set for
    * textual mime types using the syntax specified in RFC1522[14] (e.g. "text/html; charset=UTF-8"). In this particular
    * case this character set MAY be different than the response message.
    * <p/>
    * useCachedMarkup(xsd:boolean[false])?, mimeType(xsd:string)?, (markupString(xsd:string) |
    * markupBinary(xsd:base64Binary)), locale(xsd:string)?, requiresUrlRewriting(xsd:boolean[false])?,
    * cacheControl(CacheControl)?, preferredTitle(xsd:string)?, extensions(Extension)*
    *
    * @return
    */
   public static MarkupContext createMarkupContext(String mediaType, String markupString)
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(mediaType, "Media type", "MarkupContext");
      if (markupString == null)
      {
         throw new IllegalArgumentException("MarkupContext requires either a non-null markup string or binary markup.");
      }
      MarkupContext markupContext = new MarkupContext();
      markupContext.setMimeType(mediaType);
      markupContext.setMarkupString(markupString);
      return markupContext;
   }

   /**
    * useCachedMarkup(xsd:boolean[false])?, mimeType(xsd:string)?, (markupString(xsd:string) |
    * markupBinary(xsd:base64Binary)), locale(xsd:string)?, requiresUrlRewriting(xsd:boolean[false])?,
    * cacheControl(CacheControl)?, preferredTitle(xsd:string)?, extensions(Extension)*
    *
    * @param mediaType The mime type of the returned markup. The mimeType field MUST be specified whenever markup is
    *                  returned, and if the markupBinary field is used to return the markup, the mime type MUST include
    *                  the character set for textual mime types using the syntax specified in RFC1522[14] (e.g.
    *                  "text/html; charset=UTF-8"). In this particular case this character set MAY be different than the
    *                  response message.
    * @return a new MarkupContext
    */
   public static MarkupContext createMarkupContext(String mediaType, byte[] markupBinary)
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(mediaType, "MIME type", "MarkupContext");
      if (markupBinary == null || markupBinary.length == 0)
      {
         throw new IllegalArgumentException("MarkupContext requires either a non-null markup string or binary markup.");
      }
      MarkupContext markupContext = new MarkupContext();
      markupContext.setMimeType(mediaType);
      markupContext.setMarkupBinary(markupBinary);
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
   public static SessionContext createSessionContext(String sessionID, int expires)
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(sessionID, "session Id", "SessionContext");
      if (expires < 0)
      {
         throw new IllegalArgumentException("SessionContext requires a positive expiration time.");
      }
      SessionContext sessionContext = new SessionContext();
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
   public static UserContext createUserContext(String userContextKey)
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(userContextKey, "user context key", "UserContext");
      UserContext userContext = new UserContext();
      userContext.setUserContextKey(userContextKey);
      return userContext;
   }

   /**
    * consumerName(xsd:string), consumerAgent(xsd:string), methodGetSupported(xsd:boolean), consumerModes(xsd:string)*,
    * consumerWindowStates(xsd:string)*, consumerUserScopes(xsd:string)*, customUserProfileData(xsd:string)*,
    * registrationProperties(Property)*, extensions(Extension)*
    *
    * @param consumerName       A name (preferably unique) that identifies the Consumer [R355] An example of such a name
    *                           would be the Consumer's URL.
    * @param methodGetSupported A flag that tells the Producer whether the Consumer has implemented portlet URLs
    *                           (regardless of whether they are written through Consumer URL rewriting or Producer URL
    *                           writing, see [Section 10.2]) in a manner that supports HTML markup containing forms with
    *                           method="get".
    * @return
    */
   public static RegistrationData createRegistrationData(String consumerName, boolean methodGetSupported)
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(consumerName, "consumer name", "RegistrationData");
      RegistrationData regData = createDefaultRegistrationData();
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
   public static RegistrationData createDefaultRegistrationData()
   {
      RegistrationData registrationData = new RegistrationData();
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
   public static Property createProperty(String name, String lang, String stringValue)
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(name, "name", "Property");
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(lang, "language", "Property");
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(stringValue, "String value", "Property");

      Property property = new Property();
      property.setName(name);
      property.setLang(lang);
      property.setStringValue(stringValue);
      return property;
   }

   private static final ActionURL ACTION_URL = new ActionURL()
   {
      public StateString getInteractionState()
      {
         return new OpaqueStateString(WSRPRewritingConstants.REWRITE_PARAMETER_OPEN + WSRPRewritingConstants.INTERACTION_STATE + WSRPRewritingConstants.REWRITE_PARAMETER_CLOSE);
      }

      public StateString getNavigationalState()
      {
         return new OpaqueStateString(WSRPRewritingConstants.REWRITE_PARAMETER_OPEN + WSRPRewritingConstants.NAVIGATIONAL_STATE + WSRPRewritingConstants.REWRITE_PARAMETER_CLOSE);
      }

      public Mode getMode()
      {
         return Mode.create(WSRPRewritingConstants.REWRITE_PARAMETER_OPEN + WSRPRewritingConstants.MODE + WSRPRewritingConstants.REWRITE_PARAMETER_CLOSE, true);
      }

      public WindowState getWindowState()
      {
         return WindowState.create(WSRPRewritingConstants.REWRITE_PARAMETER_OPEN + WSRPRewritingConstants.WINDOW_STATE + WSRPRewritingConstants.REWRITE_PARAMETER_CLOSE, true);
      }
   };

   private static final RenderURL RENDER_URL = new RenderURL()
   {
      public StateString getNavigationalState()
      {
         return new OpaqueStateString(WSRPRewritingConstants.REWRITE_PARAMETER_OPEN + WSRPRewritingConstants.NAVIGATIONAL_STATE + WSRPRewritingConstants.REWRITE_PARAMETER_CLOSE);
      }

      public Map<String, String[]> getPublicNavigationalStateChanges()
      {
         // todo: implement properly
         return null;
      }

      public Mode getMode()
      {
         return Mode.create(WSRPRewritingConstants.REWRITE_PARAMETER_OPEN + WSRPRewritingConstants.MODE + WSRPRewritingConstants.REWRITE_PARAMETER_CLOSE, true);
      }

      public WindowState getWindowState()
      {
         return WindowState.create(WSRPRewritingConstants.REWRITE_PARAMETER_OPEN + WSRPRewritingConstants.WINDOW_STATE + WSRPRewritingConstants.REWRITE_PARAMETER_CLOSE, true);
      }
   };

   /**
    * defaultTemplate(xsd:string)?, blockingActionTemplate(xsd:string)?, renderTemplate(xsd:string)?,
    * resourceTemplate(xsd:string)?, secureDefaultTemplate(xsd:string)?, secureBlockingActionTemplate(xsd:string)?,
    * secureRenderTemplate(xsd:string)?, secureResourceTemplate(xsd:string)?, extensions(Extension)*
    *
    * @param context
    * @return
    */
   public static Templates createTemplates(PortletInvocationContext context)
   {
      Templates templates = new Templates();

      templates.setBlockingActionTemplate(createTemplate(context, ACTION_URL, Boolean.FALSE));
      templates.setRenderTemplate(createTemplate(context, RENDER_URL, Boolean.FALSE));
      templates.setSecureBlockingActionTemplate(createTemplate(context, ACTION_URL, Boolean.TRUE));
      templates.setSecureRenderTemplate(createTemplate(context, RENDER_URL, Boolean.TRUE));

      //fix-me: deal with resources properly, create fake ones for now
      templates.setResourceTemplate(WSRPRewritingConstants.FAKE_RESOURCE_URL);
      templates.setSecureResourceTemplate(WSRPRewritingConstants.FAKE_RESOURCE_URL);

      return templates;
   }

   private static String createTemplate(PortletInvocationContext context, PortletURL url, Boolean secure)
   {
      String template = context.renderURL(url, new URLFormat(secure, null, null, true));
      template = Tools.replace(template, WSRPRewritingConstants.ENC_OPEN, WSRPRewritingConstants.REWRITE_PARAMETER_OPEN);
      template = Tools.replace(template, WSRPRewritingConstants.ENC_CLOSE, WSRPRewritingConstants.REWRITE_PARAMETER_CLOSE);
      return template;
   }

   /**
    * userAgent(xsd:string)?, extensions(Extension)*
    *
    * @param userAgent
    * @return
    */
   public static ClientData createClientData(String userAgent)
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(userAgent, "user agent", "ClientData");
      ClientData clientData = new ClientData();
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
   public static CacheControl createCacheControl(int expires, String userScope)
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(userScope, "user scope", "CacheControl");
      if (expires < -1)
      {
         throw new IllegalArgumentException("Cache expiration time must be greater than -1, " +
            "-1 indicating that the cache will never expire.");
      }

      CacheControl cacheControl = new CacheControl();
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
   public static RegistrationContext createRegistrationContext(String registrationHandle)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(registrationHandle, "Registration handle");
      RegistrationContext registrationContext = new RegistrationContext();
      registrationContext.setRegistrationHandle(registrationHandle);
      return registrationContext;
   }

   /**
    * propertyDescriptions(PropertyDescription)*, modelTypes(ModelTypes)?, extensions(Extension)*
    *
    * @return
    * @since 2.6
    */
   public static ModelDescription createModelDescription(List<PropertyDescription> propertyDescriptions)
   {
      ModelDescription description = new ModelDescription();
      if (WSRPUtils.existsAndIsNotEmpty(propertyDescriptions))
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
   public static PropertyDescription createPropertyDescription(String name, QName type)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(name, "PropertyDescription name");
      ParameterValidation.throwIllegalArgExceptionIfNull(type, "PropertyDescription type");
      PropertyDescription description = new PropertyDescription();
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
   public static LocalizedString createLocalizedString(String lang, String resourceName, String value)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(lang, "LocalizedString language");
      ParameterValidation.throwIllegalArgExceptionIfNull(value, "LocalizedString value");
      LocalizedString localizedString = new LocalizedString();
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
   public static LocalizedString createLocalizedString(String value)
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
   public static PortletDescriptionResponse createPortletDescriptionResponse(PortletDescription portletDescription)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(portletDescription, "PortletDescription");
      PortletDescriptionResponse response = new PortletDescriptionResponse();
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
   public static PortletPropertyDescriptionResponse createPortletPropertyDescriptionResponse(List<PropertyDescription> propertyDescriptions)
   {
      ModelDescription modelDescription = propertyDescriptions == null ? null : createModelDescription(propertyDescriptions);
      PortletPropertyDescriptionResponse portletPropertyDescriptionResponse = new PortletPropertyDescriptionResponse();
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
   public static GetPortletPropertyDescription createGetPortletPropertyDescription(RegistrationContext registrationContext,
                                                                                   PortletContext portletContext,
                                                                                   UserContext userContext, List<String> desiredLocales)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(portletContext, "PortletContext");
      GetPortletPropertyDescription description = new GetPortletPropertyDescription();
      description.setRegistrationContext(registrationContext);
      description.setPortletContext(portletContext);
      description.setUserContext(userContext);
      if (WSRPUtils.existsAndIsNotEmpty(desiredLocales))
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
   public static GetPortletPropertyDescription createSimpleGetPortletPropertyDescription(String portletHandle)
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
   public static DestroyFailed createDestroyFailed(String portletHandle, String reason)
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(portletHandle, "Portlet handle", "DestroyFailed");
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(reason, "Reason for failure", "DestroyFailed");
      // todo: check reason should be a fault code from Section 13 of spec but this is not clear...
      DestroyFailed destroyFailed = new DestroyFailed();
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
   public static DestroyPortletsResponse createDestroyPortletsResponse(List<DestroyFailed> destroyFailed)
   {
      DestroyPortletsResponse destroyPortletsResponse = new DestroyPortletsResponse();
      if (WSRPUtils.existsAndIsNotEmpty(destroyFailed))
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
   public static SetPortletProperties createSetPortletProperties(
      RegistrationContext registrationContext,
      PortletContext portletContext,
      PropertyList propertyList)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(portletContext, "PortletContext");
      ParameterValidation.throwIllegalArgExceptionIfNull(propertyList, "PropertyList");

      SetPortletProperties properties = new SetPortletProperties();
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
   public static ClonePortlet createSimpleClonePortlet(String portletHandle)
   {
      return createClonePortlet(null, createPortletContext(portletHandle), null);
   }

   /**
    * registrationContext(RegistrationContext)?, portletContext(PortletContext), userContext(UserContext)?
    *
    * @return
    * @since 2.6
    */
   public static ClonePortlet createClonePortlet(RegistrationContext registrationContext, PortletContext portletContext, UserContext userContext)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(portletContext, "PortletContext");
      ClonePortlet clonePortlet = new ClonePortlet();
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
   public static DestroyPortlets createDestroyPortlets(RegistrationContext registrationContext, List<String> portletHandles)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(portletHandles, "Portlet handles");
      if (portletHandles.isEmpty())
      {
         throw new IllegalArgumentException("Cannot create a DestroyPortlets with an empty list of portlet handles!");
      }

      DestroyPortlets destroyPortlets = new DestroyPortlets();
      destroyPortlets.setRegistrationContext(registrationContext);
      if (WSRPUtils.existsAndIsNotEmpty(portletHandles))
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
   public static PropertyList createPropertyList()
   {
      return new PropertyList();
   }

   /**
    * EMPTY, @name(xsd:string)
    *
    * @param name
    * @return
    * @since 2.6
    */
   public static ResetProperty createResetProperty(String name)
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(name, "Property name", "ResetProperty");

      ResetProperty resetProperty = new ResetProperty();
      resetProperty.setName(name);
      return resetProperty;
   }

   /**
    * registrationContext(RegistrationContext)?, sessionIDs(xsd:string)*
    *
    * @return
    * @since 2.6
    */
   public static ReleaseSessions createReleaseSessions(RegistrationContext registrationContext, List<String> sessionIDs)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(sessionIDs, "Session IDs");
      if (sessionIDs.isEmpty())
      {
         throw new IllegalArgumentException("Cannot create a ReleaseSessions with an empty list of session IDs!");
      }

      ReleaseSessions sessions = new ReleaseSessions();
      sessions.setRegistrationContext(registrationContext);
      if (WSRPUtils.existsAndIsNotEmpty(sessionIDs))
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
   public static ModifyRegistration createModifyRegistration(RegistrationContext registrationContext,
                                                             RegistrationData registrationData)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(registrationData, "RegistrationData");

      ModifyRegistration registration = new ModifyRegistration();
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
   public static UploadContext createUploadContext(String mimeType, byte[] uploadData)
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(mimeType, "MIME Type", "UploadContext");
      if (uploadData == null || uploadData.length == 0)
      {
         throw new IllegalArgumentException("Must pass non-null, non-empty upload data");
      }

      UploadContext uploadContext = new UploadContext();
      uploadContext.setMimeType(mimeType);
      uploadContext.setUploadData(uploadData);
      return uploadContext;
   }

   public static MarkupType createMarkupType(String mimeType, List<String> modeNames, List<String> windowStateNames, List<String> localeNames)
   {
      MarkupType markupType = new MarkupType();
      markupType.setMimeType(mimeType);

      if (WSRPUtils.existsAndIsNotEmpty(modeNames))
      {
         markupType.getModes().addAll(modeNames);
      }
      if (WSRPUtils.existsAndIsNotEmpty(windowStateNames))
      {
         markupType.getWindowStates().addAll(windowStateNames);
      }
      if (WSRPUtils.existsAndIsNotEmpty(localeNames))
      {
         markupType.getLocales().addAll(localeNames);
      }
      return markupType;
   }
}
