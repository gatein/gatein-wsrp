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
import org.gatein.pc.api.spi.InstanceContext;
import org.gatein.pc.api.spi.PortletInvocationContext;
import org.gatein.pc.api.spi.WindowContext;
import org.gatein.wsrp.payload.PayloadUtils;
import org.gatein.wsrp.spec.v2.ErrorCodes;
import org.gatein.wsrp.spec.v2.WSRP2RewritingConstants;
import org.oasis.wsrp.v2.BlockingInteractionResponse;
import org.oasis.wsrp.v2.CacheControl;
import org.oasis.wsrp.v2.ClientData;
import org.oasis.wsrp.v2.ClonePortlet;
import org.oasis.wsrp.v2.Contact;
import org.oasis.wsrp.v2.CopiedPortlet;
import org.oasis.wsrp.v2.CopyPortlets;
import org.oasis.wsrp.v2.CopyPortletsResponse;
import org.oasis.wsrp.v2.Deregister;
import org.oasis.wsrp.v2.DestroyPortlets;
import org.oasis.wsrp.v2.DestroyPortletsResponse;
import org.oasis.wsrp.v2.EmployerInfo;
import org.oasis.wsrp.v2.Event;
import org.oasis.wsrp.v2.EventDescription;
import org.oasis.wsrp.v2.EventParams;
import org.oasis.wsrp.v2.EventPayload;
import org.oasis.wsrp.v2.ExportPortlets;
import org.oasis.wsrp.v2.ExportPortletsResponse;
import org.oasis.wsrp.v2.ExportedPortlet;
import org.oasis.wsrp.v2.Extension;
import org.oasis.wsrp.v2.FailedPortlets;
import org.oasis.wsrp.v2.GetMarkup;
import org.oasis.wsrp.v2.GetPortletDescription;
import org.oasis.wsrp.v2.GetPortletProperties;
import org.oasis.wsrp.v2.GetPortletPropertyDescription;
import org.oasis.wsrp.v2.GetPortletsLifetime;
import org.oasis.wsrp.v2.GetResource;
import org.oasis.wsrp.v2.GetServiceDescription;
import org.oasis.wsrp.v2.HandleEvents;
import org.oasis.wsrp.v2.HandleEventsResponse;
import org.oasis.wsrp.v2.ImportPortlet;
import org.oasis.wsrp.v2.ImportPortlets;
import org.oasis.wsrp.v2.ImportPortletsFailed;
import org.oasis.wsrp.v2.ImportPortletsResponse;
import org.oasis.wsrp.v2.ImportedPortlet;
import org.oasis.wsrp.v2.InitCookie;
import org.oasis.wsrp.v2.InteractionParams;
import org.oasis.wsrp.v2.ItemDescription;
import org.oasis.wsrp.v2.Lifetime;
import org.oasis.wsrp.v2.LocalizedString;
import org.oasis.wsrp.v2.MarkupContext;
import org.oasis.wsrp.v2.MarkupParams;
import org.oasis.wsrp.v2.MarkupResponse;
import org.oasis.wsrp.v2.MarkupType;
import org.oasis.wsrp.v2.MimeResponse;
import org.oasis.wsrp.v2.MissingParametersFault;
import org.oasis.wsrp.v2.ModelDescription;
import org.oasis.wsrp.v2.ModifyRegistration;
import org.oasis.wsrp.v2.NamedString;
import org.oasis.wsrp.v2.NamedStringArray;
import org.oasis.wsrp.v2.NavigationalContext;
import org.oasis.wsrp.v2.Online;
import org.oasis.wsrp.v2.OperationFailedFault;
import org.oasis.wsrp.v2.ParameterDescription;
import org.oasis.wsrp.v2.PerformBlockingInteraction;
import org.oasis.wsrp.v2.PersonName;
import org.oasis.wsrp.v2.PortletContext;
import org.oasis.wsrp.v2.PortletDescription;
import org.oasis.wsrp.v2.PortletDescriptionResponse;
import org.oasis.wsrp.v2.PortletPropertyDescriptionResponse;
import org.oasis.wsrp.v2.Postal;
import org.oasis.wsrp.v2.Property;
import org.oasis.wsrp.v2.PropertyDescription;
import org.oasis.wsrp.v2.PropertyList;
import org.oasis.wsrp.v2.Register;
import org.oasis.wsrp.v2.RegistrationContext;
import org.oasis.wsrp.v2.RegistrationData;
import org.oasis.wsrp.v2.ReleaseExport;
import org.oasis.wsrp.v2.ReleaseSessions;
import org.oasis.wsrp.v2.ResetProperty;
import org.oasis.wsrp.v2.Resource;
import org.oasis.wsrp.v2.ResourceContext;
import org.oasis.wsrp.v2.ResourceList;
import org.oasis.wsrp.v2.ResourceParams;
import org.oasis.wsrp.v2.ResourceResponse;
import org.oasis.wsrp.v2.ResourceValue;
import org.oasis.wsrp.v2.ReturnAny;
import org.oasis.wsrp.v2.RuntimeContext;
import org.oasis.wsrp.v2.ServiceDescription;
import org.oasis.wsrp.v2.SessionContext;
import org.oasis.wsrp.v2.SessionParams;
import org.oasis.wsrp.v2.SetExportLifetime;
import org.oasis.wsrp.v2.SetPortletProperties;
import org.oasis.wsrp.v2.SetPortletsLifetime;
import org.oasis.wsrp.v2.StateChange;
import org.oasis.wsrp.v2.Telecom;
import org.oasis.wsrp.v2.TelephoneNum;
import org.oasis.wsrp.v2.Templates;
import org.oasis.wsrp.v2.UpdateResponse;
import org.oasis.wsrp.v2.UploadContext;
import org.oasis.wsrp.v2.UserContext;
import org.oasis.wsrp.v2.UserProfile;

import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.gatein.wsrp.WSRPRewritingConstants.*;

/**
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
   private static final String AMP = "&";
   private static final String EQ = "=";
   private static final String ADDITIONAL_RESOURCE_URL_PARAMS = AMP + RESOURCE_REQUIRES_REWRITE + EQ
      + WSRP_REQUIRES_REWRITE + AMP + WSRPRewritingConstants.RESOURCE_URL + EQ + REWRITE_PARAMETER_OPEN
      + WSRPRewritingConstants.RESOURCE_URL + REWRITE_PARAMETER_CLOSE + AMP
      + WSRP2RewritingConstants.RESOURCE_PREFER_OPERATION + EQ + REWRITE_PARAMETER_OPEN
      + WSRP2RewritingConstants.RESOURCE_PREFER_OPERATION + REWRITE_PARAMETER_CLOSE;
   private static final OpaqueStateString WSRP_NAVIGATIONAL_STATE_TOKEN = new OpaqueStateString(REWRITE_PARAMETER_OPEN + NAVIGATIONAL_STATE + REWRITE_PARAMETER_CLOSE);
   private static final WindowState WSRP_WINDOW_STATE_TOKEN = WindowState.create(REWRITE_PARAMETER_OPEN + WINDOW_STATE + REWRITE_PARAMETER_CLOSE, true);
   private static final Mode WSRP_MODE_TOKEN = Mode.create(REWRITE_PARAMETER_OPEN + MODE + REWRITE_PARAMETER_CLOSE, true);

   private WSRPTypeFactory()
   {
   }

   /** ====== WSRP request objects ====== **/
   /**
    * registrationContext(RegistrationContext, nillable), desiredLocales(xsd:string)*, portletHandles(xsd:string)*,
    * userContext(UserContext, nillable)
    *
    * @param registrationContext can be null
    * @param userContext         can be null
    * @return
    */
   public static GetServiceDescription createGetServiceDescription(RegistrationContext registrationContext, UserContext userContext)
   {
      GetServiceDescription getServiceDescription = new GetServiceDescription();
      getServiceDescription.setRegistrationContext(registrationContext);
      getServiceDescription.setUserContext(userContext);
      return getServiceDescription;
   }

   /**
    * {@link RegistrationContext} (nillable), {@link PortletContext}, {@link RuntimeContext}, {@link UserContext}
    * (nillable), {@link MarkupParams}
    *
    * @param registrationContext can be null
    * @param portletContext
    * @param runtimeContext
    * @param userContext         can be null
    * @param markupParams
    * @return
    * @throws IllegalArgumentException if one of the required, non-nillable parameters is <code>null</code>
    */
   public static GetMarkup createGetMarkup(RegistrationContext registrationContext, PortletContext portletContext, RuntimeContext runtimeContext, UserContext userContext, MarkupParams markupParams)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(portletContext, "PortletContext");
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(portletContext.getPortletHandle(), "portlet handle", "PortletContext");
      ParameterValidation.throwIllegalArgExceptionIfNull(runtimeContext, "RuntimeContext");
      ParameterValidation.throwIllegalArgExceptionIfNull(markupParams, "MarkupParams");

      GetMarkup getMarkup = new GetMarkup();
      getMarkup.setRegistrationContext(registrationContext);
      getMarkup.setPortletContext(portletContext);
      getMarkup.setRuntimeContext(runtimeContext);
      getMarkup.setUserContext(userContext);
      getMarkup.setMarkupParams(markupParams);
      return getMarkup;
   }

   /**
    * {@link RegistrationContext} (nillable), {@link PortletContext}, {@link RuntimeContext}, {@link UserContext}
    * (nillable), {@link ResourceParams}
    *
    * @param registrationContext can be null
    * @param portletContext
    * @param runtimeContext
    * @param userContext         can be null
    * @param resourceParams
    * @return
    * @throws IllegalArgumentException if one of the required, non-nillable parameters is <code>null</code>
    */
   public static GetResource createGetResource(RegistrationContext registrationContext, PortletContext portletContext, RuntimeContext runtimeContext, UserContext userContext, ResourceParams resourceParams)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(portletContext, "PortletContext");
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(portletContext.getPortletHandle(), "portlet handle", "PortletContext");
      ParameterValidation.throwIllegalArgExceptionIfNull(runtimeContext, "RuntimeContext");
      ParameterValidation.throwIllegalArgExceptionIfNull(resourceParams, "ResourceParams");

      GetResource getResource = new GetResource();
      getResource.setRegistrationContext(registrationContext);
      getResource.setPortletContext(portletContext);
      getResource.setRuntimeContext(runtimeContext);
      getResource.setUserContext(userContext);
      getResource.setResourceParams(resourceParams);
      return getResource;
   }

   /**
    * {@link RegistrationContext} (nillable), {@link PortletContext}, {@link RuntimeContext}, {@link UserContext}
    * (nillable), {@link MarkupParams}, {@link InteractionParams}
    *
    * @param registrationContext can be null
    * @param portletContext
    * @param runtimeContext
    * @param userContext         can be null
    * @param markupParams
    * @param interactionParams
    * @return
    * @throws IllegalArgumentException if one of the required, non-nillable parameters is <code>null</code>
    */
   public static PerformBlockingInteraction createPerformBlockingInteraction(
      RegistrationContext registrationContext, PortletContext portletContext, RuntimeContext runtimeContext,
      UserContext userContext, MarkupParams markupParams,
      InteractionParams interactionParams)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(portletContext, "PortletContext");
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(portletContext.getPortletHandle(), "portlet handle", "PortletContext");
      ParameterValidation.throwIllegalArgExceptionIfNull(runtimeContext, "RuntimeContext");
      ParameterValidation.throwIllegalArgExceptionIfNull(markupParams, "MarkupParams");
      ParameterValidation.throwIllegalArgExceptionIfNull(interactionParams, "InteractionParams");

      PerformBlockingInteraction performBlockingInteraction = new PerformBlockingInteraction();
      performBlockingInteraction.setRegistrationContext(registrationContext);
      performBlockingInteraction.setPortletContext(portletContext);
      performBlockingInteraction.setRuntimeContext(runtimeContext);
      performBlockingInteraction.setUserContext(userContext);
      performBlockingInteraction.setMarkupParams(markupParams);
      performBlockingInteraction.setInteractionParams(interactionParams);
      return performBlockingInteraction;
   }

   /**
    * {@link RegistrationContext} (nillable), {@link PortletContext}, {@link RuntimeContext}, {@link UserContext}
    * (nillable), {@link MarkupParams}, {@link EventParams}
    *
    * @param registrationContext can be null
    * @param portletContext
    * @param runtimeContext
    * @param userContext         can be null
    * @param markupParams
    * @param eventParams
    * @return
    */
   public static HandleEvents createHandleEvents(RegistrationContext registrationContext, PortletContext portletContext, RuntimeContext runtimeContext,
                                                 UserContext userContext, MarkupParams markupParams, EventParams eventParams)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(portletContext, "PortletContext");
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(portletContext.getPortletHandle(), "portlet handle", "PortletContext");
      ParameterValidation.throwIllegalArgExceptionIfNull(runtimeContext, "RuntimeContext");
      ParameterValidation.throwIllegalArgExceptionIfNull(markupParams, "MarkupParams");
      ParameterValidation.throwIllegalArgExceptionIfNull(eventParams, "EventParams");

      HandleEvents handleEvents = new HandleEvents();
      handleEvents.setRegistrationContext(registrationContext);
      handleEvents.setPortletContext(portletContext);
      handleEvents.setRuntimeContext(runtimeContext);
      handleEvents.setUserContext(userContext);
      handleEvents.setMarkupParams(markupParams);
      handleEvents.setEventParams(eventParams);
      return handleEvents;
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
      PortletContext portletContext = createPortletContext(portletHandle);
      ParameterValidation.throwIllegalArgExceptionIfNull(portletContext, "portlet context");
      GetPortletDescription description = new GetPortletDescription();
      description.setPortletContext(portletContext);
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
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(portletHandle, "portlet handle", null);
      if (portletHandle.length() > 255)
      {
         throw new IllegalArgumentException("Portlet handles must be less than 255 characters long. Was "
            + portletHandle.length() + " long.");
      }
   }

   public static MarkupParams createMarkupParams(boolean secureClientCommunication, List<String> locales,
                                                 List<String> mimeTypes, String mode, String windowState)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(locales, "locales");
      if (locales.isEmpty())
      {
         throw new IllegalArgumentException("Cannot create a MarkupParams with an empty list of locales!");
      }

      if (mimeTypes.isEmpty())
      {
         throw new IllegalArgumentException("Cannot create a MarkupParams with an empty list of mimeTypes!");
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

   public static ResourceParams createDefaultResourceParams(String resourceID)
   {
      return createResourceParams(false, WSRPConstants.getDefaultLocales(), WSRPConstants.getDefaultMimeTypes(),
         WSRPConstants.VIEW_MODE, WSRPConstants.NORMAL_WINDOW_STATE, resourceID, StateChange.READ_ONLY);
   }

   public static ResourceParams createResourceParams(boolean secureClientCommunication, List<String> locales, List<String> mimeTypes, String mode, String windowState, String resourceID, StateChange stateChange)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(locales, "locales");
      if (locales.isEmpty())
      {
         throw new IllegalArgumentException("Cannot create a ResourceParams with an empty list of locales!");
      }

      ParameterValidation.throwIllegalArgExceptionIfNull(mimeTypes, "locales");
      if (mimeTypes.isEmpty())
      {
         throw new IllegalArgumentException("Cannot create a MarkupParams with an empty list of mimeTypes!");
      }

      ParameterValidation.throwIllegalArgExceptionIfNull(mimeTypes, "MIME types");
      ParameterValidation.throwIllegalArgExceptionIfNull(stateChange, "State Change");
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(mode, "mode", "ResourceParams");
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(windowState, "window state", "ResourceParams");
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(resourceID, "Resource ID", "ResourceParams");

      ResourceParams resourceParams = new ResourceParams();
      resourceParams.setSecureClientCommunication(secureClientCommunication);
      resourceParams.setMode(mode);
      resourceParams.setWindowState(windowState);
      if (ParameterValidation.existsAndIsNotEmpty(locales))
      {
         resourceParams.getLocales().addAll(locales);
      }

      if (ParameterValidation.existsAndIsNotEmpty(mimeTypes))
      {
         resourceParams.getMimeTypes().addAll(mimeTypes);
      }

      resourceParams.setResourceID(resourceID);
      resourceParams.setPortletStateChange(stateChange);

      return resourceParams;
   }

   public static RuntimeContext createRuntimeContext(String userAuthentication, String portletInstanceKey, String namespacePrefix)
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(userAuthentication, "user authentication", "RuntimeContext");
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(portletInstanceKey, "Portlet Instance Key", "RuntimeContext");
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(namespacePrefix, "Namespace Prefix", "RuntimeContext");

      RuntimeContext runtimeContext = new RuntimeContext();
      runtimeContext.setUserAuthentication(userAuthentication);
      runtimeContext.setPortletInstanceKey(portletInstanceKey);
      runtimeContext.setNamespacePrefix(namespacePrefix);
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

   public static ResourceResponse createResourceResponse(ResourceContext resourceContext)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(resourceContext, "ResourceContext");
      ResourceResponse resourceResponse = new ResourceResponse();
      resourceResponse.setResourceContext(resourceContext);
      return resourceResponse;
   }

   /**
    * mimeType: The mime type of the returned markup. The mimeType field MUST be specified whenever markup is returned,
    * and if the markupBinary field is used to return the markup, the mime type MUST include the character set for
    * textual mime types using the syntax specified in RFC1522[14] (e.g. "text/html; charset=UTF-8"). In this
    * particular case this character set MAY be different than the response message.
    */
   public static MarkupContext createMarkupContext(String mediaType, String markupString, byte[] markupBinary, Boolean useCachedItem)
   {
      boolean isUseCachedItem = (useCachedItem != null) && useCachedItem.booleanValue();

      MarkupContext markupContext = new MarkupContext();
      markupContext.setMimeType(mediaType);

      if (isUseCachedItem)
      {
         markupContext.setUseCachedItem(useCachedItem);
      }
      else
      {
         ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(mediaType, "Media type", "MarkupContext");

         if (markupBinary != null)
         {
            markupContext.setItemBinary(markupBinary);
         }
         else if (markupString != null)
         {
            markupContext.setItemString(markupString);
         }
         else
         {
            throw new IllegalArgumentException("MarkupContext required either a true useCacheItem or a non-null markup string or binary markup");
         }
      }

      return markupContext;
   }

   /**
    * @param mediaType The mime type of the returned resource. The mimeType field MUST be specified whenever resource
    *                  is returned, and if the resourceBinary field is used to return the resource, the mime type MUST
    *                  include the character set for textual mime types using the syntax specified in RFC1522[14] (e.g.
    *                  "text/html; charset=UTF-8"). In this particular case this character set MAY be different than
    *                  the response message.
    * @return a new ResourceContext
    */
   public static ResourceContext createResourceContext(String mediaType, String resourceString, byte[] resourceBinary)
   {
      return createMimeResponse(mediaType, resourceString, resourceBinary, ResourceContext.class);
   }

   public static <T extends MimeResponse> T createMimeResponse(String mimeType, String itemString, byte[] itemBinary, Class<T> clazz)
   {
      if ((itemString == null) && (itemBinary == null || itemBinary.length == 0))
      {
         throw new IllegalArgumentException("MimeResponse requires either a non-null markup string or binary markup.");
      }

      T mimeResponse;
      try
      {
         mimeResponse = clazz.newInstance();
      }
      catch (Exception e)
      {
         throw new RuntimeException("Couldn't instantiate " + clazz.getSimpleName(), e);
      }

      mimeResponse.setMimeType(mimeType);

      if (itemString != null)
      {
         mimeResponse.setItemString(itemString);
      }
      else
      {
         mimeResponse.setItemBinary(itemBinary);
      }

      return mimeResponse;
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
    * @param consumerName       A name (preferably unique) that identifies the Consumer [R355] An example of such a
    *                           name would be the Consumer's URL.
    * @param methodGetSupported A flag that tells the Producer whether the Consumer has implemented portlet URLs
    *                           (regardless of whether they are written through Consumer URL rewriting or Producer URL
    *                           writing, see [Section 10.2]) in a manner that supports HTML markup containing forms
    *                           with method="get".
    * @return
    */
   public static RegistrationData createRegistrationData(String consumerName, String consumerAgent, boolean methodGetSupported)
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(consumerName, "consumer name", "RegistrationData");
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(consumerAgent, "consumer agent", "RegistrationData");
      RegistrationData regData = createDefaultRegistrationData();
      regData.setConsumerName(consumerName);
      regData.setConsumerAgent(consumerAgent);
      regData.setMethodGetSupported(methodGetSupported);
      return regData;
   }

   /**
    * Same as createRegistrationData({@link WSRPConstants#DEFAULT_CONSUMER_NAME}, false) using
    * {@link WSRPConstants#CONSUMER_AGENT} for the consumer agent.
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
      ParameterValidation.throwIllegalArgExceptionIfNull(name, "name");

      Property property = new Property();
      property.setName(name);
      if (!ParameterValidation.isNullOrEmpty(lang))
      {
         property.setLang(lang);
      }
      property.setStringValue(stringValue);
      return property;
   }

   private static final OpaqueStateString WSRP_INTERACTION_STATE_TOKEN = new OpaqueStateString(REWRITE_PARAMETER_OPEN + INTERACTION_STATE + REWRITE_PARAMETER_CLOSE);
   private static final ActionURL ACTION_URL = new ActionURL()
   {
      public StateString getInteractionState()
      {
         return WSRP_INTERACTION_STATE_TOKEN;
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

   private static final HashMap<String, String[]> WSRP_PNS_MAP_TOKEN = new HashMap<String, String[]>();

   static
   {
      WSRP_PNS_MAP_TOKEN.put(WSRP2RewritingConstants.NAVIGATIONAL_VALUES,
         new String[]{REWRITE_PARAMETER_OPEN + WSRP2RewritingConstants.NAVIGATIONAL_VALUES + REWRITE_PARAMETER_CLOSE});
   }

   private static final RenderURL RENDER_URL = new RenderURL()
   {
      public StateString getNavigationalState()
      {
         return getTemplateNS();
      }

      public Map<String, String[]> getPublicNavigationalStateChanges()
      {
         return WSRP_PNS_MAP_TOKEN;
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

   private static final OpaqueStateString WSRP_RESOURCE_STATE_TOKEN = new OpaqueStateString(REWRITE_PARAMETER_OPEN + WSRP2RewritingConstants.RESOURCE_STATE + REWRITE_PARAMETER_CLOSE);
   private static ResourceURL RESOURCE_URL = new ResourceURL()
   {
      public String getResourceId()
      {
         return REWRITE_PARAMETER_OPEN + WSRP2RewritingConstants.RESOURCE_ID + REWRITE_PARAMETER_CLOSE;
      }

      public StateString getResourceState()
      {
         return WSRP_RESOURCE_STATE_TOKEN;
      }

      public CacheLevel getCacheability()
      {
         return CacheLevel.create(REWRITE_PARAMETER_OPEN + WSRP2RewritingConstants.RESOURCE_CACHEABILITY + REWRITE_PARAMETER_CLOSE);
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

      public Map<String, String> getProperties()
      {
         return Collections.emptyMap();
      }
   };

   private static StateString getTemplateNS()
   {
      return WSRP_NAVIGATIONAL_STATE_TOKEN;
   }

   private static WindowState getTemplateWindowState()
   {
      return WSRP_WINDOW_STATE_TOKEN;
   }

   private static Mode getTemplateMode()
   {
      return WSRP_MODE_TOKEN;
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
      templates.setResourceTemplate(createTemplate(context, RESOURCE_URL, false));
      templates.setSecureResourceTemplate(createTemplate(context, RESOURCE_URL, true));

      return templates;
   }

   public static Templates createTemplates(String defaultTemplate, String blockingActionTemplate, String renderTemplate, String resourceTemplate, String secureDefaultTemplate, String secureBlockingActionTemplate, String secureRenderTemplate, String secureResourceTemplate)
   {
      Templates templates = new Templates();
      templates.setDefaultTemplate(defaultTemplate);
      templates.setBlockingActionTemplate(blockingActionTemplate);
      templates.setRenderTemplate(renderTemplate);
      templates.setResourceTemplate(resourceTemplate);
      templates.setSecureDefaultTemplate(secureDefaultTemplate);
      templates.setSecureBlockingActionTemplate(secureBlockingActionTemplate);
      templates.setSecureRenderTemplate(secureRenderTemplate);
      templates.setSecureResourceTemplate(secureResourceTemplate);

      return templates;
   }

   private static String createTemplate(PortletInvocationContext context, ContainerURL url, Boolean secure)
   {
      String template = context.renderURL(url, new URLFormat(secure, null, null, true));
      template = TextTools.replace(template, WSRPRewritingConstants.ENC_OPEN, WSRPRewritingConstants.REWRITE_PARAMETER_OPEN);
      template = TextTools.replace(template, WSRPRewritingConstants.ENC_CLOSE, WSRPRewritingConstants.REWRITE_PARAMETER_CLOSE);

      // fix for GTNWSRP-22
      if (RESOURCE_URL.equals(url))
      {
         template += ADDITIONAL_RESOURCE_URL_PARAMS;
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
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(mimeType, "MIME Type", "MarkupContext");

      ParameterValidation.throwIllegalArgExceptionIfNull(modeNames, "modeNames");
      if (modeNames.isEmpty())
      {
         throw new IllegalArgumentException("Cannot create a MarkupType with an empty list of modes!");
      }

      ParameterValidation.throwIllegalArgExceptionIfNull(windowStateNames, "windowStatesNames");
      if (windowStateNames.isEmpty())
      {
         throw new IllegalArgumentException("Cannot create a MarkupType with an empty list of windowStates!");
      }

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
   public static FailedPortlets createFailedPortlets(Collection<String> portletHandles, ErrorCodes.Codes errorCode, String reason)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(errorCode, "ErrorCode");
      if (ParameterValidation.existsAndIsNotEmpty(portletHandles))
      {
         FailedPortlets failedPortlets = new FailedPortlets();
         failedPortlets.getPortletHandles().addAll(portletHandles);
         if (reason != null)
         {
            failedPortlets.setReason(createLocalizedString(reason));
         }
         failedPortlets.setErrorCode(ErrorCodes.getQname(errorCode));

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

   public static NavigationalContext createNavigationalContext(String opaqueValue, List<NamedString> publicValues)
   {
      NavigationalContext navigationalContext = new NavigationalContext();
      navigationalContext.setOpaqueValue(opaqueValue);

      if (publicValues != null && !publicValues.isEmpty())
      {
         navigationalContext.getPublicValues().addAll(publicValues);
      }

      return navigationalContext;
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
         event.setPayload(PayloadUtils.getPayloadAsEventPayload(event, payload));
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
      ParameterValidation.throwIllegalArgExceptionIfNull(name, "name");
      NamedString namedString = new NamedString();
      namedString.setName(name);
      namedString.setValue(value);
      return namedString;
   }

   public static EventPayload createEventPayloadAsNamedString(NamedStringArray payload)
   {
      EventPayload result = new EventPayload();
      result.setNamedStringArray(payload);
      return result;
   }

   public static ExportPortlets createExportPortlets(RegistrationContext registrationContext, List<PortletContext> portletContexts, UserContext userContext, Lifetime lifetime, Boolean exportByValue)
   {
      if (!ParameterValidation.existsAndIsNotEmpty(portletContexts))
      {
         throw new IllegalArgumentException("Must provide at least one PortletContext to ExportPortlets.");
      }

      ExportPortlets exportPortlets = new ExportPortlets();
      exportPortlets.setRegistrationContext(registrationContext);
      exportPortlets.getPortletContext().addAll(portletContexts);
      exportPortlets.setUserContext(userContext);
      exportPortlets.setLifetime(lifetime);
      exportPortlets.setExportByValueRequired(exportByValue);

      return exportPortlets;
   }

   public static ExportPortletsResponse createExportPortletsResponse(byte[] exportContext, List<ExportedPortlet> exportedPortlets, List<FailedPortlets> failedPortlets, Lifetime lifetime, ResourceList resourceList)
   {
      // everything can be empty or nillable, there is no need to check for null values
      ExportPortletsResponse response = new ExportPortletsResponse();
      response.setExportContext(exportContext);
      response.getExportedPortlet().addAll(exportedPortlets);
      response.getFailedPortlets().addAll(failedPortlets);
      response.setLifetime(lifetime);
      response.setResourceList(resourceList);

      return response;
   }

   public static ExportedPortlet createExportedPortlet(String portletHandle, byte[] exportData)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(portletHandle, "PortletHandle");
      ParameterValidation.throwIllegalArgExceptionIfNull(exportData, "ExportData");

      ExportedPortlet exportedPortlet = new ExportedPortlet();
      exportedPortlet.setPortletHandle(portletHandle);
      exportedPortlet.setExportData(exportData);

      return exportedPortlet;
   }

   public static ImportPortlets createImportPortlets(RegistrationContext registrationContext, byte[] importContext, List<ImportPortlet> importPortlet, UserContext userContext, Lifetime lifetime)
   {
      if (!ParameterValidation.existsAndIsNotEmpty(importPortlet))
      {
         throw new IllegalArgumentException("Must provide at least one ImportPortlet to ImportPortlets.");
      }

      ImportPortlets importPortlets = new ImportPortlets();
      importPortlets.setRegistrationContext(registrationContext);
      importPortlets.setImportContext(importContext);
      importPortlets.getImportPortlet().addAll(importPortlet);
      importPortlets.setUserContext(userContext);
      importPortlets.setLifetime(lifetime);

      return importPortlets;
   }

   public static ImportPortlet createImportPortlet(String importID, byte[] exportData)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(importID, "ImportID");
      ParameterValidation.throwIllegalArgExceptionIfNull(exportData, "ExportData");

      ImportPortlet importPortlet = new ImportPortlet();
      importPortlet.setImportID(importID);
      importPortlet.setExportData(exportData);
      return importPortlet;
   }

   public static ImportPortletsResponse createImportPortletsResponse(List<ImportedPortlet> importedPortlets, List<ImportPortletsFailed> importPortletsFailed, ResourceList resourceList)
   {
      // everything can be empty or nillable, no need to check for null values
      ImportPortletsResponse response = new ImportPortletsResponse();
      response.getImportedPortlets().addAll(importedPortlets);
      response.getImportFailed().addAll(importPortletsFailed);
      response.setResourceList(resourceList);

      return response;
   }

   public static ImportPortletsFailed createImportPortletsFailed(List<String> importIds, ErrorCodes.Codes errorCode, String reason)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(errorCode, "ErrorCode");
      if (ParameterValidation.existsAndIsNotEmpty(importIds))
      {
         ImportPortletsFailed failedPortlets = new ImportPortletsFailed();
         failedPortlets.getImportID().addAll(importIds);
         if (reason != null)
         {
            failedPortlets.setReason(createLocalizedString(reason));
         }
         failedPortlets.setErrorCode(ErrorCodes.getQname(errorCode));

         return failedPortlets;
      }

      throw new IllegalArgumentException("Must provide non-null, non-empty portlet id list.");
   }

   public static ImportedPortlet createImportedPortlet(String portletID, PortletContext portletContext)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(portletID, "PortletID");
      ParameterValidation.throwIllegalArgExceptionIfNull(portletContext, "PortletContext");

      ImportedPortlet importedPortlet = new ImportedPortlet();
      importedPortlet.setImportID(portletID);
      importedPortlet.setNewPortletContext(portletContext);

      return importedPortlet;
   }

   public static ReleaseExport createReleaseExport(RegistrationContext registrationContext, byte[] exportContext, UserContext userContext)
   {
      //Can the exportContext be empty?
      if (exportContext == null || exportContext.length == 0)
      {
         throw new IllegalArgumentException("Must provide a non null or empty exportContext to ReleaseExport.");
      }

      ReleaseExport releaseExport = new ReleaseExport();
      releaseExport.setRegistrationContext(registrationContext);
      releaseExport.setExportContext(exportContext);
      releaseExport.setUserContext(userContext);

      return releaseExport;
   }

   public static SetExportLifetime createSetExportLifetime(RegistrationContext registrationContext, byte[] exportContext, UserContext userContext, Lifetime lifetime)
   {
      //Can the exportContext be empty?
      if (exportContext == null || exportContext.length == 0)
      {
         throw new IllegalArgumentException("Must provide a non null or empty exportContext to SetExportLifetime.");
      }

      SetExportLifetime setExportLifetime = new SetExportLifetime();
      setExportLifetime.setRegistrationContext(registrationContext);
      setExportLifetime.setExportContext(exportContext);
      setExportLifetime.setUserContext(userContext);
      setExportLifetime.setLifetime(lifetime);

      return setExportLifetime;
   }

   public static Contact createContact(Postal postal, Telecom telecom, Online online)
   {
      Contact contact = new Contact();
      contact.setPostal(postal);
      contact.setTelecom(telecom);
      contact.setOnline(online);

      return contact;
   }

   public static Postal createPostal(String name, String street, String city, String stateprov, String postalCode, String country, String organization)
   {
      Postal postal = new Postal();
      postal.setName(name);
      postal.setStreet(street);
      postal.setCity(city);
      postal.setStateprov(stateprov);
      postal.setPostalcode(postalCode);
      postal.setCountry(country);
      postal.setOrganization(organization);

      return postal;
   }

   public static Telecom createTelecom(TelephoneNum telephone, TelephoneNum fax, TelephoneNum mobile, TelephoneNum pager)
   {
      Telecom telecom = new Telecom();
      telecom.setTelephone(telephone);
      telecom.setFax(fax);
      telecom.setMobile(mobile);
      telecom.setPager(pager);

      return telecom;
   }

   public static TelephoneNum createTelephoneNum(String intCode, String loccode, String number, String ext, String comment)
   {
      TelephoneNum telephoneNum = new TelephoneNum();
      telephoneNum.setIntcode(intCode);
      telephoneNum.setLoccode(loccode);
      telephoneNum.setNumber(number);
      telephoneNum.setExt(ext);
      telephoneNum.setComment(comment);

      return telephoneNum;
   }

   public static Online createOnline(String email, String uri)
   {
      Online online = new Online();
      online.setEmail(email);
      online.setUri(uri);
      return online;
   }

   public static EmployerInfo createEmployerInfo(String employer, String department, String jobTitle)
   {
      EmployerInfo employerInfo = new EmployerInfo();
      employerInfo.setEmployer(employer);
      employerInfo.setDepartment(department);
      employerInfo.setJobtitle(jobTitle);

      return employerInfo;
   }

   public static PersonName createPersonName(String prefix, String given, String family, String middle, String suffix, String nickname)
   {
      PersonName personName = new PersonName();
      personName.setPrefix(prefix);
      personName.setGiven(given);
      personName.setFamily(family);
      personName.setMiddle(middle);
      personName.setSuffix(suffix);
      personName.setNickname(nickname);

      return personName;
   }

   public static Extension createExtension(Object any)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(any, "Any");
      Extension extension = new Extension();
      extension.setAny(any);

      return extension;
   }

   public static MissingParametersFault createMissingParametersFault()
   {
      MissingParametersFault missingParametersFault = new MissingParametersFault();
      return missingParametersFault;
   }

   public static OperationFailedFault createOperationFailedFault()
   {
      OperationFailedFault operationFailedFault = new OperationFailedFault();
      return operationFailedFault;
   }

   public static ItemDescription createItemDescription(LocalizedString description, LocalizedString displayName, String itemName)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(itemName, "ItemName");

      ItemDescription itemDescription = new ItemDescription();
      itemDescription.setDescription(description);
      itemDescription.setDisplayName(displayName);
      itemDescription.setItemName(itemName);

      return itemDescription;
   }

   public static Resource createResource(String resourceName, List<ResourceValue> resourceValue)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(resourceName, "ResourceName");
      Resource resource = new Resource();
      resource.setResourceName(resourceName);

      if (resourceValue != null && !resourceValue.isEmpty())
      {
         resource.getValues().addAll(resourceValue);
      }

      return resource;
   }

   public static ResourceList createResourceList(List<Resource> resources)
   {
      if (ParameterValidation.existsAndIsNotEmpty(resources))
      {
         ResourceList resourceList = new ResourceList();
         resourceList.getResources().addAll(resources);

         return resourceList;
      }
      else
      {
         throw new IllegalArgumentException("Must provide non-null, non-empty resource list.");
      }
   }

   public static ResourceValue createResourceValue(String lang, String value)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(lang, "Lang");
      ResourceValue resourceValue = new ResourceValue();
      resourceValue.setLang(value);
      resourceValue.setValue(value);

      return resourceValue;
   }

   public static ReturnAny createReturnAny()
   {
      return new ReturnAny();
   }

   public static SessionParams createSessionParams(String sessionID)
   {
      SessionParams sessionParams = new SessionParams();
      sessionParams.setSessionID(sessionID);

      return sessionParams;
   }

   public static UserProfile createUserProfile(PersonName name, XMLGregorianCalendar bdate, String gender, EmployerInfo employerInfo, Contact homeInfo, Contact businessInfo)
   {
      UserProfile userProfile = new UserProfile();
      userProfile.setName(name);
      userProfile.setBdate(bdate);
      userProfile.setGender(gender);
      userProfile.setEmployerInfo(employerInfo);
      userProfile.setHomeInfo(homeInfo);
      userProfile.setBusinessInfo(businessInfo);

      return userProfile;
   }

   public static CopyPortlets createCopyPortlets(RegistrationContext toRegistrationContext, UserContext toUserContext, RegistrationContext fromRegistrationContext, UserContext fromUserContext, List<PortletContext> fromPortletContexts)
   {
      if (!ParameterValidation.existsAndIsNotEmpty(fromPortletContexts))
      {
         throw new IllegalArgumentException("Must provide at least one PortletContext to CopyPortlets.");
      }

      CopyPortlets copyPortlets = new CopyPortlets();
      copyPortlets.setToRegistrationContext(toRegistrationContext);
      copyPortlets.setToUserContext(toUserContext);
      copyPortlets.setFromRegistrationContext(fromRegistrationContext);
      copyPortlets.setFromUserContext(fromUserContext);
      copyPortlets.getFromPortletContexts().addAll(fromPortletContexts);

      return copyPortlets;
   }

   public static CopyPortletsResponse createCopyPortletsResponse(List<CopiedPortlet> copiedPortlets, List<FailedPortlets> failedPortlets, ResourceList resourceList)
   {
      CopyPortletsResponse response = new CopyPortletsResponse();
      response.getCopiedPortlets().addAll(copiedPortlets);
      response.getFailedPortlets().addAll(failedPortlets);
      response.setResourceList(resourceList);

      return response;
   }

   public static CopiedPortlet createCopiedPortlet(PortletContext newPortletContext, String fromPortletHandle)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(newPortletContext, "newPortletContext");
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(fromPortletHandle, "fromPortletHandle", "createCopiedPortlet");

      CopiedPortlet copiedPortlet = new CopiedPortlet();

      copiedPortlet.setNewPortletContext(newPortletContext);
      copiedPortlet.setFromPortletHandle(fromPortletHandle);

      return copiedPortlet;
   }

   public static NamedStringArray createNamedStringArray()
   {
      return new NamedStringArray();
   }

   public static String getPortletInstanceKey(InstanceContext instanceContext)
   {
      return instanceContext.getId();
   }

   public static String getNamespacePrefix(WindowContext windowContext, String portletHandle)
   {
      String namespacePrefix = getNamespaceFrom(windowContext);
      if (namespacePrefix == null)
      {
         return portletHandle;
      }

      return namespacePrefix;
   }

   public static String getNamespaceFrom(WindowContext windowContext)
   {
      if (windowContext != null)
      {
         return windowContext.getNamespace();
      }

      return null;
   }

   public static GetPortletsLifetime createGetPortletsLifetime(RegistrationContext registrationContext, List<PortletContext> portletContext, UserContext userContext)
   {
      if (ParameterValidation.existsAndIsNotEmpty(portletContext))
      {
         final GetPortletsLifetime getPortletsLifetime = new GetPortletsLifetime();
         getPortletsLifetime.setRegistrationContext(registrationContext);
         getPortletsLifetime.setUserContext(userContext);
         return getPortletsLifetime;
      }

      throw new IllegalArgumentException("List of portlet contexts must not be null or empty");
   }

   public static SetPortletsLifetime createSetPortletsLifetime(RegistrationContext registrationContext, List<PortletContext> portletContext, UserContext userContext, Lifetime lifetime)
   {
      if (ParameterValidation.existsAndIsNotEmpty(portletContext))
      {
         final SetPortletsLifetime setPortletsLifetime = new SetPortletsLifetime();
         setPortletsLifetime.setRegistrationContext(registrationContext);
         setPortletsLifetime.setUserContext(userContext);
         setPortletsLifetime.setLifetime(lifetime);
         return setPortletsLifetime;
      }

      throw new IllegalArgumentException("List of portlet contexts must not be null or empty");
   }

   public static Register createRegister(RegistrationData registrationData, Lifetime lifetime, UserContext userContext)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(registrationData, "RegistrationData");

      Register register = new Register();
      register.setRegistrationData(registrationData);
      register.setLifetime(lifetime);
      register.setUserContext(userContext);

      return register;
   }

   public static Deregister createDeregister(RegistrationContext registrationContext, UserContext userContext)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(registrationContext, "RegistrationContext");

      Deregister deregister = new Deregister();
      deregister.setRegistrationContext(registrationContext);
      deregister.setUserContext(userContext);

      return deregister;
   }
}
