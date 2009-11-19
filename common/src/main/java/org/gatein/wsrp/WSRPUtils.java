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

import org.gatein.common.i18n.LocaleFormat;
import org.gatein.common.util.ConversionException;
import org.gatein.common.util.ParameterValidation;
import org.gatein.pc.api.ActionURL;
import org.gatein.pc.api.Mode;
import org.gatein.pc.api.PortletContext;
import org.gatein.pc.api.PortletStateType;
import org.gatein.pc.api.PortletURL;
import org.gatein.pc.api.RenderURL;
import org.gatein.pc.api.StatefulPortletContext;
import org.gatein.pc.api.WindowState;
import org.gatein.pc.api.state.AccessMode;
import org.gatein.wsrp.registration.LocalizedString;
import org.gatein.wsrp.registration.RegistrationPropertyDescription;
import org.oasis.wsrp.v1.InteractionParams;
import org.oasis.wsrp.v1.MarkupParams;
import org.oasis.wsrp.v1.NamedString;
import org.oasis.wsrp.v1.PropertyDescription;
import org.oasis.wsrp.v1.StateChange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 11351 $
 * @since 2.4
 */
public class WSRPUtils
{
   private final static Map<String, String> JSR168_WSRP_WINDOW_STATES = new HashMap<String, String>(7);
   private final static Map<String, WindowState> WSRP_JSR168_WINDOW_STATES = new HashMap<String, WindowState>(7);
   private final static Map<String, String> JSR168_WSRP_MODES = new HashMap<String, String>(7);
   private final static Map<String, Mode> WSRP_JSR168_MODES = new HashMap<String, Mode>(7);
   private static final String SET_OF_LOCALES = "set of Locales";
   private static final String MODE = "Mode";
   private static final String WSRP_MODE_NAME = "wsrp portlet name";
   private static final String WSRP_WINDOW_STATE_NAME = "wsrp window state name";
   private static final String WINDOW_STATE = "WindowState";

   public static final Set<Mode> DEFAULT_JSR168_MODES;
   public static final Set<WindowState> DEFAULT_JSR168_WINDOWSTATES;

   private static boolean strict = true;
   private static Logger log = LoggerFactory.getLogger(WSRPUtils.class);

   public static void setStrict(boolean strict)
   {
      WSRPUtils.strict = strict;
      log.debug("Using " + (strict ? "strict" : "lenient") + " language code validation mode.");
   }

   static
   {
      JSR168_WSRP_WINDOW_STATES.put(WindowState.MAXIMIZED.toString(), WSRPConstants.MAXIMIZED_WINDOW_STATE);
      JSR168_WSRP_WINDOW_STATES.put(WindowState.MINIMIZED.toString(), WSRPConstants.MINIMIZED_WINDOW_STATE);
      JSR168_WSRP_WINDOW_STATES.put(WindowState.NORMAL.toString(), WSRPConstants.NORMAL_WINDOW_STATE);

      JSR168_WSRP_MODES.put(Mode.EDIT.toString(), WSRPConstants.EDIT_MODE);
      JSR168_WSRP_MODES.put(Mode.HELP.toString(), WSRPConstants.HELP_MODE);
      JSR168_WSRP_MODES.put(Mode.VIEW.toString(), WSRPConstants.VIEW_MODE);

      WSRP_JSR168_WINDOW_STATES.put(WSRPConstants.MAXIMIZED_WINDOW_STATE, WindowState.MAXIMIZED);
      WSRP_JSR168_WINDOW_STATES.put(WSRPConstants.MINIMIZED_WINDOW_STATE, WindowState.MINIMIZED);
      WSRP_JSR168_WINDOW_STATES.put(WSRPConstants.NORMAL_WINDOW_STATE, WindowState.NORMAL);

      WSRP_JSR168_MODES.put(WSRPConstants.EDIT_MODE, Mode.EDIT);
      WSRP_JSR168_MODES.put(WSRPConstants.HELP_MODE, Mode.HELP);
      WSRP_JSR168_MODES.put(WSRPConstants.VIEW_MODE, Mode.VIEW);

      DEFAULT_JSR168_MODES = new HashSet<Mode>(WSRP_JSR168_MODES.values());
      DEFAULT_JSR168_WINDOWSTATES = new HashSet<WindowState>(WSRP_JSR168_WINDOW_STATES.values());
   }

   private WSRPUtils()
   {
   }

   public static WindowState getJSR168WindowStateFromWSRPName(String wsrpWindowStateName)
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(wsrpWindowStateName, WSRP_WINDOW_STATE_NAME, null);
      WindowState windowState = WSRP_JSR168_WINDOW_STATES.get(wsrpWindowStateName);
      return (windowState == null) ? WindowState.create(wsrpWindowStateName) : windowState;
   }

   public static boolean isDefaultWSRPWindowState(String wsrpWindowStateName)
   {
      return WSRP_JSR168_WINDOW_STATES.containsKey(wsrpWindowStateName) || WSRPConstants.SOLO_WINDOW_STATE.equals(wsrpWindowStateName);
   }

   public static String convertJSR168WindowStateNameToWSRPName(String jsr168WindowStateName)
   {
      if (jsr168WindowStateName == null)
      {
         return WSRPConstants.NORMAL_WINDOW_STATE;
      }

      // todo: how should we deal with solo?
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(jsr168WindowStateName, WSRP_WINDOW_STATE_NAME, null);
      String wsrpName = JSR168_WSRP_WINDOW_STATES.get(jsr168WindowStateName);
      return (wsrpName == null) ? jsr168WindowStateName : wsrpName;
   }

   public static String getWSRPNameFromJSR168WindowState(WindowState windowState)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(windowState, WINDOW_STATE);
      return convertJSR168WindowStateNameToWSRPName(windowState.toString());
   }

   public static Mode getJSR168PortletModeFromWSRPName(String wsrpPortletModeName)
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(wsrpPortletModeName, WSRP_MODE_NAME, null);
      Mode mode = WSRP_JSR168_MODES.get(wsrpPortletModeName);
      return (mode == null) ? Mode.create(wsrpPortletModeName) : mode;
   }

   public static boolean isDefaultWSRPMode(String wsrpPortletModeName)
   {
      return WSRP_JSR168_MODES.containsKey(wsrpPortletModeName) || WSRPConstants.PREVIEW_MODE.equals(wsrpPortletModeName);
   }

   public static String convertJSR168PortletModeNameToWSRPName(String jsr168PortletModeName)
   {
      if (jsr168PortletModeName == null)
      {
         return WSRPConstants.VIEW_MODE;
      }

      // todo: how should we deal with preview?
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(jsr168PortletModeName, WSRP_MODE_NAME, null);
      String wsrpName = JSR168_WSRP_MODES.get(jsr168PortletModeName);
      return (wsrpName == null) ? jsr168PortletModeName : wsrpName;
   }

   public static String getWSRPNameFromJSR168PortletMode(Mode portletMode)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(portletMode, MODE);
      return convertJSR168PortletModeNameToWSRPName(portletMode.toString());
   }

   public static AccessMode getAccessModeFromStateChange(StateChange stateChange)
   {
      if (StateChange.READ_ONLY.equals(stateChange))
      {
         return AccessMode.READ_ONLY;
      }
      if (StateChange.CLONE_BEFORE_WRITE.equals(stateChange))
      {
         return AccessMode.CLONE_BEFORE_WRITE;
      }
      if (StateChange.READ_WRITE.equals(stateChange))
      {
         return AccessMode.READ_WRITE;
      }
      throw new IllegalArgumentException("Unsupported StateChange: " + stateChange);
   }

   public static StateChange getStateChangeFromAccessMode(AccessMode accessMode)
   {
      if (AccessMode.READ_ONLY.equals(accessMode))
      {
         return StateChange.READ_ONLY;
      }
      if (AccessMode.READ_WRITE.equals(accessMode))
      {
         return StateChange.READ_WRITE;
      }
      if (AccessMode.CLONE_BEFORE_WRITE.equals(accessMode))
      {
         return StateChange.CLONE_BEFORE_WRITE;
      }
      throw new IllegalArgumentException("Unsupported AccessMode: " + accessMode);
   }


   public static String getWSRPURLTypeFrom(PortletURL url)
   {
      if (url instanceof WSRPPortletURL)
      {
         return ((WSRPPortletURL)url).getURLType();
      }

      if (url instanceof RenderURL)
      {
         return WSRPRewritingConstants.URL_TYPE_RENDER;
      }

      if (url instanceof ActionURL)
      {
         return WSRPRewritingConstants.URL_TYPE_BLOCKING_ACTION;
      }

      throw new IllegalArgumentException("Unrecognized URL type.");
   }

   public static String convertRequestAuthTypeToWSRPAuthType(String authType)
   {
      if (authType == null)
      {
         return WSRPConstants.NONE_USER_AUTHENTICATION;
      }
      if (HttpServletRequest.CLIENT_CERT_AUTH.equals(authType))
      {
         return WSRPConstants.CERTIFICATE_USER_AUTHENTICATION;
      }
      if (HttpServletRequest.BASIC_AUTH.equalsIgnoreCase(authType) || HttpServletRequest.FORM_AUTH.equals(authType))
      {
         return WSRPConstants.PASSWORD_USER_AUTHENTICATION; // is this correct?
      }
      return authType;
   }

   public static List<String> convertLocalesToRFC3066LanguageTags(List<Locale> localesOrderedByPreference)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(localesOrderedByPreference, SET_OF_LOCALES);

      List<String> desiredLocales = new ArrayList<String>(localesOrderedByPreference.size());
      for (Locale locale : localesOrderedByPreference)
      {
         desiredLocales.add(toString(locale));
      }
      return desiredLocales;
   }

   public static PortletContext convertToPortalPortletContext(org.oasis.wsrp.v1.PortletContext portletContext)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(portletContext, "PortletContext");
      String handle = portletContext.getPortletHandle();
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(handle, "portlet handle", "PortletContext");

      PortletContext context;
      byte[] state = portletContext.getPortletState();
      context = PortletContext.createPortletContext(handle, state);

      return context;
   }

   public static PortletContext convertToPortalPortletContext(String portletHandle, byte[] state)
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(portletHandle, "portlet handle", "PortletContext");

      PortletContext context;
      context = PortletContext.createPortletContext(portletHandle, state);

      return context;
   }

   /**
    * @param portletContext
    * @return Since 2.6
    */
   public static org.oasis.wsrp.v1.PortletContext convertToWSRPPortletContext(PortletContext portletContext)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(portletContext, "PortletContext");
      String id = portletContext.getId();
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(id, "portlet id", "PortletContext");

      org.oasis.wsrp.v1.PortletContext result = WSRPTypeFactory.createPortletContext(id);
      if (portletContext instanceof StatefulPortletContext)
      {
         StatefulPortletContext context = (StatefulPortletContext)portletContext;
         if (PortletStateType.OPAQUE.equals(context.getType()))
         {
            result.setPortletState(((StatefulPortletContext<byte[]>)context).getState());
         }
      }
      return result;
   }


   public static Locale getLocale(String lang) throws IllegalArgumentException
   {
      if (lang != null)
      {
         String possiblyRelaxed = lang;
         if (!WSRPUtils.strict)
         {
            // treat en_US as valid by en_US => en-US
            // todo: maybe this should be handled by an interceptor...
            possiblyRelaxed = lang.replace('_', '-');
         }

         try
         {
            return LocaleFormat.RFC3066_LANGUAGE_TAG.getLocale(possiblyRelaxed);
         }
         catch (ConversionException e)
         {
            if (WSRPUtils.strict)
            {
               throw new IllegalArgumentException(e);
            }
            else
            {
               log.debug("Was given an invalid language: '" + possiblyRelaxed
                  + "'. Since we're using relaxed validation, we will assume " + Locale.ENGLISH + " to avoid crashing!", e);
               return Locale.ENGLISH;
            }
         }
      }
      else
      {
         return Locale.getDefault();
      }
   }

   public static String toString(Locale locale) throws IllegalArgumentException
   {
      try
      {
         return LocaleFormat.RFC3066_LANGUAGE_TAG.toString(locale);
      }
      catch (ConversionException e)
      {
         // Previous behavior on using ConversionException was like that
         throw new IllegalArgumentException(e);
      }
   }

   /**
    * Debugging method.
    *
    * @param params
    * @return
    * @since 2.6
    */
   public static String toString(MarkupParams params)
   {
      if (params != null)
      {
         StringBuffer sb = new StringBuffer("MarkupParams");
         if (params.isSecureClientCommunication())
         {
            sb.append("(secure)");
         }
         sb.append("[M=").append(params.getMode()).append("][WS=").append(params.getWindowState()).append("]")
            .append("[NS=").append(params.getNavigationalState()).append("]");
         return sb.toString();
      }
      return null;
   }

   /**
    * Debugging method
    *
    * @param interactionParams
    * @return
    * @since 2.6
    */
   public static String toString(InteractionParams interactionParams)
   {
      if (interactionParams != null)
      {
         StringBuffer sb = new StringBuffer("InteractionParams");
         sb.append("[IS=").append(interactionParams.getInteractionState()).append("]")
            .append("[StateChange=").append(interactionParams.getPortletStateChange().value()).append("]");
         List<NamedString> formParams = interactionParams.getFormParameters();
         if (formParams != null)
         {
            sb.append("\n\tForm params:\n");
            for (NamedString formParam : formParams)
            {
               sb.append("\t\t").append(formParam.getName()).append("='").append(formParam.getValue()).append("'\n");
            }
         }
         return sb.toString();
      }
      return null;
   }

   /**
    * @param propertyDescription
    * @return
    * @since 2.6
    */
   public static RegistrationPropertyDescription convertToRegistrationPropertyDescription(PropertyDescription propertyDescription)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(propertyDescription, "PropertyDescription");
      RegistrationPropertyDescription desc = new RegistrationPropertyDescription(propertyDescription.getName(),
         propertyDescription.getType());
      desc.setLabel(getLocalizedStringOrNull(propertyDescription.getLabel()));
      desc.setHint(getLocalizedStringOrNull(propertyDescription.getHint()));

      return desc;
   }

   public static PropertyDescription convertToPropertyDescription(RegistrationPropertyDescription propertyDescription)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(propertyDescription, "RegistrationPropertyDescription");
      PropertyDescription propDesc = WSRPTypeFactory.createPropertyDescription(propertyDescription.getName().toString(),
         propertyDescription.getType());

      // todo: deal with languages properly!!
      LocalizedString hint = propertyDescription.getHint();
      if (hint != null)
      {
         propDesc.setHint(convertToWSRPLocalizedString(hint));
      }
      LocalizedString label = propertyDescription.getLabel();
      if (label != null)
      {
         propDesc.setLabel(convertToWSRPLocalizedString(label));
      }
      return propDesc;
   }

   public static org.oasis.wsrp.v1.LocalizedString convertToWSRPLocalizedString(LocalizedString regLocalizedString)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(regLocalizedString, "LocalizedString");
      return WSRPTypeFactory.createLocalizedString(toString(regLocalizedString.getLocale()),
         regLocalizedString.getResourceName(), regLocalizedString.getValue());
   }

   private static LocalizedString getLocalizedStringOrNull(org.oasis.wsrp.v1.LocalizedString wsrpLocalizedString)
   {
      if (wsrpLocalizedString == null)
      {
         return null;
      }
      else
      {
         return convertToLocalizedString(wsrpLocalizedString);
      }
   }

   /**
    * @param wsrpLocalizedString
    * @return
    * @since 2.6
    */
   public static LocalizedString convertToLocalizedString(org.oasis.wsrp.v1.LocalizedString wsrpLocalizedString)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(wsrpLocalizedString, "WSRP LocalizedString");
      String lang = wsrpLocalizedString.getLang();
      Locale locale;
      if (lang == null)
      {
         locale = Locale.getDefault();
      }
      else
      {
         locale = getLocale(lang);
      }

      LocalizedString localizedString = new LocalizedString(wsrpLocalizedString.getValue(), locale);
      localizedString.setResourceName(wsrpLocalizedString.getResourceName());
      return localizedString;
   }
}
