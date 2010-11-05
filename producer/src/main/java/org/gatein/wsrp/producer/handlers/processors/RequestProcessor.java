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

package org.gatein.wsrp.producer.handlers.processors;

import org.gatein.common.net.media.MediaType;
import org.gatein.common.util.MarkupInfo;
import org.gatein.common.util.ParameterValidation;
import org.gatein.pc.api.Mode;
import org.gatein.pc.api.Portlet;
import org.gatein.pc.api.PortletInvokerException;
import org.gatein.pc.api.StateString;
import org.gatein.pc.api.WindowState;
import org.gatein.pc.api.invocation.PortletInvocation;
import org.gatein.pc.api.invocation.response.PortletInvocationResponse;
import org.gatein.pc.api.spi.PortalContext;
import org.gatein.pc.api.spi.SecurityContext;
import org.gatein.pc.api.spi.UserContext;
import org.gatein.pc.api.spi.WindowContext;
import org.gatein.pc.api.state.AccessMode;
import org.gatein.pc.portlet.impl.jsr168.PortletUtils;
import org.gatein.registration.Registration;
import org.gatein.wsrp.UserContextConverter;
import org.gatein.wsrp.WSRPConstants;
import org.gatein.wsrp.WSRPUtils;
import org.gatein.wsrp.producer.Utils;
import org.gatein.wsrp.spec.v2.WSRP2ExceptionFactory;
import org.oasis.wsrp.v2.InvalidHandle;
import org.oasis.wsrp.v2.InvalidRegistration;
import org.oasis.wsrp.v2.MarkupType;
import org.oasis.wsrp.v2.MimeRequest;
import org.oasis.wsrp.v2.MissingParameters;
import org.oasis.wsrp.v2.ModifyRegistrationRequired;
import org.oasis.wsrp.v2.NamedString;
import org.oasis.wsrp.v2.NavigationalContext;
import org.oasis.wsrp.v2.OperationFailed;
import org.oasis.wsrp.v2.PortletContext;
import org.oasis.wsrp.v2.PortletDescription;
import org.oasis.wsrp.v2.RegistrationContext;
import org.oasis.wsrp.v2.RuntimeContext;
import org.oasis.wsrp.v2.SessionParams;
import org.oasis.wsrp.v2.UnsupportedMimeType;
import org.oasis.wsrp.v2.UnsupportedMode;
import org.oasis.wsrp.v2.UnsupportedWindowState;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 13121 $
 * @since 2.6
 */
public abstract class RequestProcessor<Response>
{
   private static final String WINDOW_STATE = "window state";
   private static final String PORTLET_MODE = "portlet mode";

   protected PortletInvocation invocation;
   protected MarkupRequest markupRequest;
   protected PortletDescription portletDescription;
   protected Portlet portlet;
   protected WSRPInstanceContext instanceContext;
   protected ProducerHelper producer;


   protected RequestProcessor(ProducerHelper producer)
   {
      this.producer = producer;
   }

   void prepareInvocation() throws InvalidRegistration, OperationFailed, InvalidHandle,
      UnsupportedMimeType, UnsupportedWindowState, UnsupportedMode, MissingParameters, ModifyRegistrationRequired
   {
      Registration registration = producer.getRegistrationOrFailIfInvalid(getRegistrationContext());

      // get session information and deal with it
      final RuntimeContext runtimeContext = getRuntimeContext();
      WSRP2ExceptionFactory.throwMissingParametersIfValueIsMissing(runtimeContext, "RuntimeContext", getContextName());

      checkForSessionIDs(runtimeContext);

      // get parameters
      final MimeRequest params = getParams();
      WSRP2ExceptionFactory.throwMissingParametersIfValueIsMissing(params, "MarkupParams", getContextName());

      // get portlet handle
      PortletContext wsrpPC = getPortletContext();
      WSRP2ExceptionFactory.throwMissingParametersIfValueIsMissing(wsrpPC, "PortletContext", getContextName());
      org.gatein.pc.api.PortletContext portletContext = WSRPUtils.convertToPortalPortletContext(wsrpPC);

      // retrieve the portlet
      try
      {
         // calls RegistrationLocal.setRegistration so no need to here
         portlet = producer.getPortletWith(portletContext, registration);
      }
      catch (PortletInvokerException e)
      {
         throw WSRP2ExceptionFactory.throwWSException(OperationFailed.class, "Could not retrieve portlet '" + portletContext + "'", e);
      }

      // get portlet description for the desired portlet...
      final List<String> desiredLocales = params.getLocales();
      portletDescription = producer.getPortletDescription(portlet, desiredLocales);
      if (Boolean.TRUE.equals(portletDescription.isUsesMethodGet()))
      {
         throw WSRP2ExceptionFactory.throwWSException(OperationFailed.class, "Portlets using GET method in forms are not currently supported.", null);
      }

      List<MarkupType> markupTypes = portletDescription.getMarkupTypes();

      // based on the markup parameters and portlet description generate the most appropriate markup request
      markupRequest = createMarkupRequestFrom(markupTypes, params, portlet);

      // prepare information for invocation
      final org.oasis.wsrp.v2.UserContext wsrpUserContext = getUserContext();
      checkUserContext(wsrpUserContext);

      SecurityContext securityContext = createSecurityContext(params, runtimeContext, wsrpUserContext);
      MarkupInfo streamInfo = createStreamInfo(markupRequest);
      PortalContext portalContext = createPortalContext(params, markupRequest);
      UserContext userContext = createUserContext(wsrpUserContext, markupRequest.getLocale(), desiredLocales);
      String portletInstanceKey = runtimeContext.getPortletInstanceKey();
      instanceContext = createInstanceContext(portletContext, getAccessMode(), portletInstanceKey);
      WindowContext windowContext = createWindowContext(portletContext.getId(), runtimeContext);

      // prepare the invocation
      WSRPPortletInvocationContext context = new WSRPPortletInvocationContext(streamInfo, securityContext, portalContext, userContext, instanceContext, windowContext);
      PortletInvocation invocation = initInvocation(context);

      invocation.setTarget(portlet.getContext());
      invocation.setWindowState(WSRPUtils.getJSR168WindowStateFromWSRPName(markupRequest.getWindowState()));
      invocation.setMode(WSRPUtils.getJSR168PortletModeFromWSRPName(markupRequest.getMode()));

      NavigationalContext navigationalContext = params.getNavigationalContext();
      if (navigationalContext != null)
      {
         StateString navigationalState = createNavigationalState(navigationalContext.getOpaqueValue());
         invocation.setNavigationalState(navigationalState);

         List<NamedString> publicParams = navigationalContext.getPublicValues();
         if (ParameterValidation.existsAndIsNotEmpty(publicParams))
         {
            Map<String, String[]> publicNS = WSRPUtils.createPublicNSFrom(publicParams);
            invocation.setPublicNavigationalState(publicNS);
         }
      }

      context.contextualize(invocation);
      setInvocation(invocation);
   }

   abstract RegistrationContext getRegistrationContext();

   abstract RuntimeContext getRuntimeContext();

   abstract MimeRequest getParams();

   public abstract PortletContext getPortletContext();

   abstract org.oasis.wsrp.v2.UserContext getUserContext();

   abstract String getContextName();

   abstract AccessMode getAccessMode() throws MissingParameters;

   abstract PortletInvocation initInvocation(WSRPPortletInvocationContext context);

   public abstract Response processResponse(PortletInvocationResponse response);


   /**
    * Returns the most appropriate information to base markup generation on based on a Portlet's specified markup types
    * and a markup request parameters.
    *
    * @param markupTypes the Portlet's specified markup types
    * @param params      the markup request parameters
    * @param portlet
    * @return a MarkupRequest containing the most appropriate information to base markup generation for this request
    */
   private MarkupRequest createMarkupRequestFrom(List<MarkupType> markupTypes, MimeRequest params, Portlet portlet)
      throws UnsupportedMimeType, UnsupportedMode, UnsupportedWindowState
   {
      List<String> desiredMIMETypes = params.getMimeTypes();
      MarkupType markupType = null;

      // Get the MIME type to use
      for (String desiredMIMEType : desiredMIMETypes)
      {
         for (MarkupType type : markupTypes)
         {
            if (desiredMIMEType.equals(type.getMimeType()))
            {
               markupType = type;
               break;
            }
         }
      }

      // no MIME type was found: error!
      if (markupType == null)
      {
         throw WSRP2ExceptionFactory.throwWSException(UnsupportedMimeType.class, "None of the specified MIME types are supported by portlet '" + portlet.getContext().getId() + "'", null);
      }

      // use user-desired locales
      List<String> locales = params.getLocales();
      List<String> supportedLocales = new ArrayList<String>(markupType.getLocales());
      if (supportedLocales != null)
      {
         // reset markup type locales
         markupType.getLocales().clear();
         boolean found = false;

         // find the best match
         for (String locale : locales)
         {
            for (String supportedLocale : supportedLocales)
            {
               if (locale.equals(supportedLocale))
               {
                  markupType.getLocales().add(locale);
                  found = true;
                  break;
               }
            }

            if (found)
            {
               break;
            }
         }

         // if no best match was found, use whatever the user gave us
         if (!found)
         {
            markupType.getLocales().addAll(locales);
         }
      }
      else
      {
         markupType.getLocales().addAll(locales);
      }

      // get the mode
      String mode;
      try
      {
         mode = getMatchingOrFailFrom(markupType.getModes(), params.getMode(), PORTLET_MODE);
      }
      catch (IllegalArgumentException e)
      {
         throw WSRP2ExceptionFactory.throwWSException(UnsupportedMode.class, "Unsupported mode '" + params.getMode() + "'", e);
      }

      // get the window state
      String windowState;
      try
      {
         windowState = getMatchingOrFailFrom(markupType.getWindowStates(), params.getWindowState(), WINDOW_STATE);
      }
      catch (IllegalArgumentException e)
      {
         throw WSRP2ExceptionFactory.throwWSException(UnsupportedWindowState.class, "Unsupported window state '" + params.getWindowState() + "'", e);
      }

      // get the character set
      String characterSet = getMatchingOrDefaultFrom(Collections.<String>emptyList(), params.getMarkupCharacterSets(), WSRPConstants.DEFAULT_CHARACTER_SET);

      return new MarkupRequest(markupType, mode, windowState, characterSet, portlet);
   }

   /**
    * Retrieves the desired value from the set of possible values if such value exists or throw an
    * <code>IllegalArgumentException</code>.
    *
    * @param possibleValues the set of supported values
    * @param desired        the desired value
    * @param valueType      a name identifying the type of the desired value (for error reporting purpose)
    * @return the desired value
    * @throws IllegalArgumentException if the desired value is not found in the set of possible values
    */
   private String getMatchingOrFailFrom(List<String> possibleValues, String desired, String valueType) throws IllegalArgumentException
   {
      if (possibleValues.contains(desired))
      {
         return desired;
      }
      throw new IllegalArgumentException(desired + " is not a supported " + valueType);
   }

   /**
    * Retrieves the best matching value from a set of possible values based on an ordered set of preferred values or the
    * given default value if no matching value is found.
    *
    * @param possibleValues  the set of possible values
    * @param preferredValues the ordered (according to user preferences) set of preferred values
    * @param defaultValue    the default value to be used if no match can be found
    * @return the first match in the set of possible values from the ordered set of preferred values or the default
    *         value if no such value can be found
    */
   private String getMatchingOrDefaultFrom(List<String> possibleValues, List<String> preferredValues, String defaultValue)
   {
      if (preferredValues != null && possibleValues != null)
      {
         for (String preferredValue : preferredValues)
         {
            if (possibleValues.contains(preferredValue))
            {
               return preferredValue;
            }
         }
      }

      return defaultValue;
   }

   private void checkUserContext(org.oasis.wsrp.v2.UserContext wsrpUserContext) throws MissingParameters
   {
      if (wsrpUserContext != null)
      {
         WSRP2ExceptionFactory.throwMissingParametersIfValueIsMissing(wsrpUserContext.getUserContextKey(), "User Context Key", "UserContext");
      }
   }


   private void checkForSessionIDs(RuntimeContext runtimeContext) throws OperationFailed
   {
      SessionParams sessionParams = runtimeContext.getSessionParams();
      if (sessionParams != null && sessionParams.getSessionID() != null)
      {
         Utils.throwOperationFaultOnSessionOperation();
      }
   }

   protected StateString createNavigationalState(String navigationalState)
   {
      if (navigationalState == null)
      {
         return null;
      }
      else
      {
         return StateString.create(navigationalState);
      }
   }

   private WSRPInstanceContext createInstanceContext(org.gatein.pc.api.PortletContext portletContext, final AccessMode accessMode, String instanceId)
   {
      return new WSRPInstanceContext(portletContext, accessMode, instanceId);
   }

   private WindowContext createWindowContext(final String portletHandle, final RuntimeContext runtimeContext)
   {
      String id = runtimeContext.getPortletInstanceKey();
      if (ParameterValidation.isNullOrEmpty(id))
      {
         id = portletHandle;
      }

      String namespacePrefix = runtimeContext.getNamespacePrefix();
      if (ParameterValidation.isNullOrEmpty(namespacePrefix))
      {
         namespacePrefix = PortletUtils.generateNamespaceFrom(portletHandle);
      }

      return new WSRPWindowContext(id, namespacePrefix);
   }

   private UserContext createUserContext(final org.oasis.wsrp.v2.UserContext userContext,
                                         String preferredLocale, final List<String> supportedLocales)
   {
      // todo: investigate ways to cache this information?
      // fix-me: should getInformations be put in the request attribute PortletRequest.USER_INFO?
      return UserContextConverter.createPortalUserContextFrom(userContext, supportedLocales, preferredLocale);
   }

   private PortalContext createPortalContext(final MimeRequest params, final MarkupRequest markupRequest)
   {
      return new PortalContext()
      {

         public String getInfo()
         {
            return PortalContext.VERSION.toString();
         }

         public Set<WindowState> getWindowStates()
         {
            List<String> validNewWindowStates = params.getValidNewWindowStates();
            if (ParameterValidation.existsAndIsNotEmpty(validNewWindowStates))
            {
               Set<WindowState> states = new HashSet<WindowState>(validNewWindowStates.size());
               for (String state : validNewWindowStates)
               {
                  states.add(WSRPUtils.getJSR168WindowStateFromWSRPName(state));
               }
               return states;
            }
            return markupRequest.getSupportedWindowStates();
         }

         public Set<Mode> getModes()
         {
            List<String> validNewModes = params.getValidNewModes();
            if (ParameterValidation.existsAndIsNotEmpty(validNewModes))
            {
               Set<Mode> modes = new HashSet<Mode>(validNewModes.size());
               for (String mode : validNewModes)
               {
                  modes.add(WSRPUtils.getJSR168PortletModeFromWSRPName(mode));
               }
               return modes;
            }
            return markupRequest.getSupportedModes();
         }

         public Map<String, String> getProperties()
         {
            return Collections.emptyMap();
         }
      };
   }

   private MarkupInfo createStreamInfo(MarkupRequest markupRequest) throws UnsupportedMimeType
   {
      MarkupInfo markupInfo;
      try
      {
         markupInfo = new MarkupInfo(MediaType.create(markupRequest.getMediaType()), markupRequest.getCharacterSet());
      }
      catch (IllegalArgumentException e)
      {
         throw WSRP2ExceptionFactory.throwWSException(UnsupportedMimeType.class, e.getLocalizedMessage(), e);
      }
      return markupInfo;
   }

   // fix-me: check that the correct semantics is used.

   private SecurityContext createSecurityContext(final MimeRequest params, final RuntimeContext runtimeContext,
                                                 final org.oasis.wsrp.v2.UserContext wsrpUserContext)
   {
      return new SecurityContext()
      {
         public boolean isSecure()
         {
            return params.isSecureClientCommunication();
         }

         public String getAuthType()
         {
            return runtimeContext.getUserAuthentication();
         }

         public String getRemoteUser()
         {
            if (wsrpUserContext != null)
            {
               return wsrpUserContext.getUserContextKey();
            }
            return null;
         }

         public Principal getUserPrincipal()
         {
            return null;
         }

         public boolean isUserInRole(String roleName)
         {
            return wsrpUserContext != null && wsrpUserContext.getUserCategories().contains(roleName);
         }

         public boolean isAuthenticated()
         {
            return wsrpUserContext != null;
         }
      };
   }

   public PortletInvocation getInvocation()
   {
      return invocation;
   }

   public void setInvocation(PortletInvocation invocation)
   {
      this.invocation = invocation;
   }
}
