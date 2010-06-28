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

package org.gatein.wsrp;

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
import org.gatein.wsrp.spec.v2.ErrorCodes;
import org.oasis.wsrp.v2.BlockingInteractionResponse;
import org.oasis.wsrp.v2.CacheControl;
import org.oasis.wsrp.v2.ClientData;
import org.oasis.wsrp.v2.ClonePortlet;
import org.oasis.wsrp.v2.DestroyPortlets;
import org.oasis.wsrp.v2.DestroyPortletsResponse;
import org.oasis.wsrp.v2.Event;
import org.oasis.wsrp.v2.EventDescription;
import org.oasis.wsrp.v2.EventParams;
import org.oasis.wsrp.v2.EventPayload;
import org.oasis.wsrp.v2.FailedPortlets;
import org.oasis.wsrp.v2.GetMarkup;
import org.oasis.wsrp.v2.GetPortletDescription;
import org.oasis.wsrp.v2.GetPortletProperties;
import org.oasis.wsrp.v2.GetPortletPropertyDescription;
import org.oasis.wsrp.v2.GetServiceDescription;
import org.oasis.wsrp.v2.HandleEvents;
import org.oasis.wsrp.v2.HandleEventsResponse;
import org.oasis.wsrp.v2.InitCookie;
import org.oasis.wsrp.v2.InteractionParams;
import org.oasis.wsrp.v2.LocalizedString;
import org.oasis.wsrp.v2.MarkupContext;
import org.oasis.wsrp.v2.MarkupParams;
import org.oasis.wsrp.v2.MarkupResponse;
import org.oasis.wsrp.v2.MarkupType;
import org.oasis.wsrp.v2.ModelDescription;
import org.oasis.wsrp.v2.ModifyRegistration;
import org.oasis.wsrp.v2.NamedString;
import org.oasis.wsrp.v2.NamedStringArray;
import org.oasis.wsrp.v2.NavigationalContext;
import org.oasis.wsrp.v2.ParameterDescription;
import org.oasis.wsrp.v2.PerformBlockingInteraction;
import org.oasis.wsrp.v2.PortletContext;
import org.oasis.wsrp.v2.PortletDescription;
import org.oasis.wsrp.v2.PortletDescriptionResponse;
import org.oasis.wsrp.v2.PortletPropertyDescriptionResponse;
import org.oasis.wsrp.v2.Property;
import org.oasis.wsrp.v2.PropertyDescription;
import org.oasis.wsrp.v2.PropertyList;
import org.oasis.wsrp.v2.RegistrationContext;
import org.oasis.wsrp.v2.RegistrationData;
import org.oasis.wsrp.v2.ReleaseSessions;
import org.oasis.wsrp.v2.ResetProperty;
import org.oasis.wsrp.v2.RuntimeContext;
import org.oasis.wsrp.v2.ServiceDescription;
import org.oasis.wsrp.v2.SessionContext;
import org.oasis.wsrp.v2.SetPortletProperties;
import org.oasis.wsrp.v2.StateChange;
import org.oasis.wsrp.v2.Templates;
import org.oasis.wsrp.v2.UpdateResponse;
import org.oasis.wsrp.v2.UploadContext;
import org.oasis.wsrp.v2.UserContext;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.gatein.wsrp.WSRPRewritingConstants.*;

/**
 * TODO: NEEDS TO BE UPDATED TO CONFORM TO WSRP 2 XSD, see GTNWSRP-42
 * <p/>
 * Creates minimally valid instances of WSRP types, populated with default values where possible, as per
 * wsrp_v2_types.xsd.
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 11317 $
 * @since 2.4
 */
public class WSRPTypeFactory
{
   private static final String REQUIRE_REWRITE_URL_PARAM = "&" + WSRPRewritingConstants.RESOURCE_REQUIRES_REWRITE + "=" + WSRPRewritingConstants.WSRP_REQUIRES_REWRITE;

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

   public static GetPortletDescription createGetPortletDescription(RegistrationContext registrationContext, PortletContext portletContext, UserContext userContext)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(portletContext, "portlet context");
      GetPortletDescription description = new GetPortletDescription();
      description.setPortletContext(portletContext);
      description.setRegistrationContext(registrationContext);
      description.setUserContext(userContext);
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

   public static GetPortletProperties createGetPortletProperties(RegistrationContext registrationContext, PortletContext portletContext, UserContext userContext, List<String> names)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(portletContext, "PortletContext");
      GetPortletProperties properties = new GetPortletProperties();
      properties.setRegistrationContext(registrationContext);
      properties.setPortletContext(portletContext);
      properties.setUserContext(userContext);
      properties.getNames().addAll(names);
      return properties;
   }

   /** ====== WSRP Response objects ====== * */

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

   public static UpdateResponse createUpdateResponse()
   {
      return new UpdateResponse();
   }

   public static PortletDescription createPortletDescription(String portletHandle, List<MarkupType> markupTypes)
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(portletHandle, "portlet handle", null);
      checkPortletHandle(portletHandle);
      ParameterValidation.throwIllegalArgExceptionIfNull(markupTypes, "MarkupType");
      if (markupTypes.isEmpty())
      {
         throw new IllegalArgumentException("Cannot create a PortletDescription with an empty list of MarkupTypes!");
      }

      PortletDescription portletDescription = new PortletDescription();
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
   public static MarkupParams createDefaultMarkupParams()
   {
      return createMarkupParams(false, WSRPConstants.getDefaultLocales(), WSRPConstants.getDefaultMimeTypes(),
         WSRPConstants.VIEW_MODE, WSRPConstants.NORMAL_WINDOW_STATE);
   }

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
    * Same as createRuntimeContext({@link WSRPConstants#NONE_USER_AUTHENTICATION})
    *
    * @return
    */
   public static RuntimeContext createDefaultRuntimeContext()
   {
      return createRuntimeContext(WSRPConstants.NONE_USER_AUTHENTICATION);
   }

   public static RuntimeContext createRuntimeContext(String userAuthentication)
   {
      //TODO: portletInstanceKey and NameSpacepPrefix are also required;
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(userAuthentication, "user authentication", "RuntimeContext");

      RuntimeContext runtimeContext = new RuntimeContext();
      runtimeContext.setUserAuthentication(userAuthentication);
      return runtimeContext;
   }

   public static PortletContext createPortletContext(String portletHandle)
   {
      checkPortletHandle(portletHandle);

      PortletContext portletContext = new PortletContext();
      portletContext.setPortletHandle(portletHandle);
      return portletContext;
   }


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

   public static InteractionParams createInteractionParams(StateChange portletStateChange)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(portletStateChange, "portletStateChange");

      InteractionParams interactionParams = new InteractionParams();
      interactionParams.setPortletStateChange(portletStateChange);
      return interactionParams;
   }

   public static InitCookie createInitCookie(RegistrationContext registrationContext)
   {
      InitCookie initCookie = new InitCookie();
      initCookie.setRegistrationContext(registrationContext);
      return initCookie;
   }

   public static ServiceDescription createServiceDescription(boolean requiresRegistration)
   {
      ServiceDescription serviceDescription = new ServiceDescription();
      serviceDescription.setRequiresRegistration(requiresRegistration);
      return serviceDescription;
   }

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
      markupContext.setItemString(markupString);
      return markupContext;
   }

   /**
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
      markupContext.setItemBinary(markupBinary);
      return markupContext;
   }

   /**
    * @param sessionID An opaque string the Portlet defines for referencing state that is stored locally on the
    *                  Producer. The maximum length of a sessionID is 4096 characters,
    * @param expires   Maximum number of seconds between invocations referencing the sessionID before the Producer will
    *                  schedule releasing the related resources. A value of -1 indicates that the sessionID will never
    *                  expire.
    * @return a new SessionContext
    */
   public static SessionContext createSessionContext(String sessionID, int expires)
   {
      //TODO: a sessionID is minOccurs 0, it shouldn't be required, expires also is minOccurs 0
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

   /** For UserProfile and related classes, everything is optional so no need to have factory methods. */
   public static UserContext createUserContext(String userContextKey)
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(userContextKey, "user context key", "UserContext");
      UserContext userContext = new UserContext();
      userContext.setUserContextKey(userContextKey);
      return userContext;
   }

   /**
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
      //TODO: consumer agent requirement
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

   public static Property createProperty(String name, String lang, String stringValue)
   {
      // QName.valueOf validates name
      QName qName = QName.valueOf(name);
      return createProperty(qName, lang, stringValue);
   }

   public static Property createProperty(QName name, String lang, String stringValue)
   {
      //TODO: stringValue is not required
      ParameterValidation.throwIllegalArgExceptionIfNull(name, "name");
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
   public static Templates createTemplates(PortletInvocationContext context)
   {
      Templates templates = new Templates();

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
   public static ClientData createClientData(String userAgent)
   {
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
   public static PropertyDescription createPropertyDescription(String name, QName type)
   {
      // QName.valueOf(name) validates name
      QName qName = QName.valueOf(name);

      ParameterValidation.throwIllegalArgExceptionIfNull(type, "PropertyDescription type");
      PropertyDescription description = new PropertyDescription();
      description.setName(qName);
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
   public static GetPortletPropertyDescription createSimpleGetPortletPropertyDescription(String portletHandle)
   {
      return createGetPortletPropertyDescription(null, createPortletContext(portletHandle), null, null);
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

   public static DestroyPortlets createDestroyPortlets(RegistrationContext registrationContext, List<String> portletHandles)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(portletHandles, "Portlet handles");
      if (!ParameterValidation.existsAndIsNotEmpty(portletHandles))
      {
         throw new IllegalArgumentException("Cannot create a DestroyPortlets with an empty list of portlet handles!");
      }

      DestroyPortlets destroyPortlets = new DestroyPortlets();
      destroyPortlets.setRegistrationContext(registrationContext);
      destroyPortlets.getPortletHandles().addAll(portletHandles);

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
      // QName.valueOf(name) validates name
      QName qName = QName.valueOf(name);

      ResetProperty resetProperty = new ResetProperty();
      resetProperty.setName(qName);
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
      //TODO: modes and windowstates might need a check for null 
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(mimeType, "MIME Type", "MarkupContext");
      MarkupType markupType = new MarkupType();
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

   /**
    * reason(LocalizedString)?, resourceList(ResourceList)?, extensions(Extension)* errorCode(ErrorCodes)
    * portletHandles(xsd:string)+
    *
    * @param portletHandles
    * @param reason
    * @return
    */
   public static FailedPortlets createFailedPortlets(Collection<String> portletHandles, String reason)
   {
      if (ParameterValidation.existsAndIsNotEmpty(portletHandles))
      {
         //TODO: reason should be able to be null
         ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(reason, "Reason for failure", "createFailedPortlets");
         FailedPortlets failedPortlets = new FailedPortlets();
         failedPortlets.getPortletHandles().addAll(portletHandles);
         if (reason != null)
         {
            failedPortlets.setReason(createLocalizedString(reason));
         }
         failedPortlets.setErrorCode(ErrorCodes.OperationFailed);

         return failedPortlets;
      }

      throw new IllegalArgumentException("Must provide non-null, non-empty portlet handle list.");
   }

   /**
    * failedPortlets(FailedPortlets)*, extensions(Extension)*
    *
    * @param failedPortlets
    * @return
    */
   public static DestroyPortletsResponse createDestroyPortletsResponse(List<FailedPortlets> failedPortlets)
   {
      DestroyPortletsResponse dpr = new DestroyPortletsResponse();
      if (ParameterValidation.existsAndIsNotEmpty(failedPortlets))
      {
         dpr.getFailedPortlets().addAll(failedPortlets);
      }
      return dpr;
   }

   public static NavigationalContext createNavigationalContextOrNull(StateString navigationalState, Map<String, String[]> publicNavigationalState)
   {
      if (navigationalState != null || publicNavigationalState != null)
      {
         NavigationalContext context = new NavigationalContext();
         if (navigationalState != null)
         {
            String state = navigationalState.getStringValue();
            if (!StateString.JBPNS_PREFIX.equals(state))  // fix-me: see JBPORTAL-900
            {
               context.setOpaqueValue(state);
            }
         }

         if (ParameterValidation.existsAndIsNotEmpty(publicNavigationalState))
         {
            // todo: public NS GTNWSRP-38
            for (Map.Entry<String, String[]> entry : publicNavigationalState.entrySet())
            {
               String name = entry.getKey();
               for (String value : entry.getValue())
               {
                  context.getPublicValues().add(WSRPTypeFactory.createNamedString(name, value));
               }
            }
         }

         return context;
      }

      return null;
   }

   public static ParameterDescription createParameterDescription(String identifier)
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(identifier, "Parameter identifier", null);
      ParameterDescription desc = new ParameterDescription();
      desc.setIdentifier(identifier);
      return desc;
   }

   public static EventDescription createEventDescription(QName name)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(name, "name");
      EventDescription desc = new EventDescription();
      desc.setName(name);
      return desc;
   }

   public static HandleEventsResponse createHandleEventsReponse()
   {
      return new HandleEventsResponse();
   }

   public static HandleEvents createHandleEvents(PortletContext portletContext, RuntimeContext runtimeContext,
                                                 MarkupParams markupParams, EventParams eventParams)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(portletContext, "PortletContext");
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(portletContext.getPortletHandle(), "portlet handle", "PortletContext");
      ParameterValidation.throwIllegalArgExceptionIfNull(runtimeContext, "RuntimeContext");
      ParameterValidation.throwIllegalArgExceptionIfNull(markupParams, "MarkupParams");
      ParameterValidation.throwIllegalArgExceptionIfNull(eventParams, "EventParams");

      HandleEvents handleEvents = new HandleEvents();
      handleEvents.setPortletContext(portletContext);
      handleEvents.setEventParams(eventParams);
      handleEvents.setMarkupParams(markupParams);
      handleEvents.setRuntimeContext(runtimeContext);
      return handleEvents;
   }

   public static EventParams createEventParams(List<Event> events, StateChange portletStateChange)
   {
      if (!ParameterValidation.existsAndIsNotEmpty(events))
      {
         throw new IllegalArgumentException("Must provide at least one Event to EventParams.");
      }
      ParameterValidation.throwIllegalArgExceptionIfNull(portletStateChange, "StateChange");

      EventParams eventParams = new EventParams();
      eventParams.setPortletStateChange(portletStateChange);
      eventParams.getEvents().addAll(events);
      return eventParams;
   }

   public static Event createEvent(QName name, Serializable payload)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(name, "Event name");
      Event event = new Event();
      event.setName(name);
      if (payload != null)
      {
         Class<? extends Object> type = payload.getClass();
         XmlRootElement annotation = type.getAnnotation(XmlRootElement.class);
         if (annotation != null)
         {
            event.setType(new QName(annotation.namespace(), annotation.name()));
            event.setPayload(WSRPTypeFactory.createEventPayloadAsAny(payload));
         }
         else
         {
            event.setPayload(WSRPTypeFactory.createEventPayloadAsNamedString(payload));
         }
      }
      return event;
   }

   public static EventPayload createEventPayloadAsAny(Object value)
   {
      EventPayload payload = new EventPayload();
      payload.setAny(value);
      return payload;
   }

   public static NamedString createNamedString(String name, String value)
   {
      NamedString namedString = new NamedString();
      namedString.setName(name);
      namedString.setValue(value);
      return namedString;
   }

   public static EventPayload createEventPayloadAsNamedString(Object payload)
   {
      // todo: fix me GTNWSRP-49
      EventPayload result = new EventPayload();
      NamedStringArray value = new NamedStringArray();
      value.getNamedString().add(createNamedString("event", payload.toString()));
      result.setNamedStringArray(value);
      return result;
   }
}
