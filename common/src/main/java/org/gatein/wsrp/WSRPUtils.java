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

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.gatein.common.i18n.LocaleFormat;
import org.gatein.common.net.URLTools;
import org.gatein.common.util.ConversionException;
import org.gatein.common.util.ParameterValidation;
import org.gatein.pc.api.Mode;
import org.gatein.pc.api.PortletContext;
import org.gatein.pc.api.PortletStateType;
import org.gatein.pc.api.StatefulPortletContext;
import org.gatein.pc.api.WindowState;
import org.gatein.pc.api.cache.CacheLevel;
import org.gatein.pc.api.state.AccessMode;
import org.gatein.wsrp.registration.LocalizedString;
import org.gatein.wsrp.registration.RegistrationPropertyDescription;
import org.gatein.wsrp.spec.v2.WSRP2Constants;
import org.oasis.wsrp.v2.InteractionParams;
import org.oasis.wsrp.v2.MarkupParams;
import org.oasis.wsrp.v2.NamedString;
import org.oasis.wsrp.v2.NavigationalContext;
import org.oasis.wsrp.v2.PropertyDescription;
import org.oasis.wsrp.v2.StateChange;
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
   private final static Map<CacheLevel, String> JSR286_WSRP_CACHE = new HashMap<CacheLevel, String>(7);
   private static final String SET_OF_LOCALES = "set of Locales";
   private static final String MODE = "Mode";
   private static final String WSRP_MODE_NAME = "wsrp portlet name";
   private static final String WSRP_WINDOW_STATE_NAME = "wsrp window state name";
   private static final String WINDOW_STATE = "WindowState";

   public static final Set<Mode> DEFAULT_JSR168_MODES;
   public static final Set<WindowState> DEFAULT_JSR168_WINDOWSTATES;

   private static boolean strict = true;
   private static Logger log = LoggerFactory.getLogger(WSRPUtils.class);

   /** Switch for 00618063 support case */
   private static PropertyAccessor propertyAccessor = new DefaultPropertyAccessor();
   public static final String DEACTIVATE_URL_REWRITING = "org.gatein.wsrp.producer.deactivateURLRewriting";

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

      JSR286_WSRP_CACHE.put(CacheLevel.FULL, WSRP2Constants.RESOURCE_CACHEABILITY_FULL);
      JSR286_WSRP_CACHE.put(CacheLevel.PAGE, WSRP2Constants.RESOURCE_CACHEABILITY_PAGE);
      JSR286_WSRP_CACHE.put(CacheLevel.PORTLET, WSRP2Constants.RESOURCE_CACHEABILITY_PORTLET);
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

   public static PortletContext convertToPortalPortletContext(org.oasis.wsrp.v2.PortletContext portletContext)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(portletContext, "PortletContext");
      String handle = portletContext.getPortletHandle();
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(handle, "portlet handle", "PortletContext");

      PortletContext context;
      byte[] state = portletContext.getPortletState();
      context = PortletContext.createPortletContext(handle, state, false);

      return context;
   }

   public static PortletContext convertToPortalPortletContext(String portletHandle, byte[] state)
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(portletHandle, "portlet handle", "PortletContext");

      PortletContext context;
      context = PortletContext.createPortletContext(portletHandle, state, false);

      return context;
   }

   /**
    * @param portletContext
    * @return Since 2.6
    */
   public static org.oasis.wsrp.v2.PortletContext convertToWSRPPortletContext(PortletContext portletContext)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(portletContext, "PortletContext");
      String id = portletContext.getId();
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(id, "portlet id", "PortletContext");

      org.oasis.wsrp.v2.PortletContext result = WSRPTypeFactory.createPortletContext(id);
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

   public static String getResourceCacheabilityFromCacheLevel(CacheLevel cacheLevel)
   {
      return cacheLevel == null ? null : cacheLevel.name().toLowerCase(Locale.ENGLISH);
   }

   public static CacheLevel getCacheLevelFromResourceCacheability(String resourceCacheability)
   {
      // if we don't pass a resource cacheability, assume Page for maximum compatibility
      if (resourceCacheability == null)
      {
         return CacheLevel.PAGE;
      }
      return CacheLevel.create(resourceCacheability.toUpperCase(Locale.ENGLISH));
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
         NavigationalContext navigationalContext = params.getNavigationalContext();
         sb.append("[M=").append(params.getMode()).append("][WS=").append(params.getWindowState()).append("]");
         if (navigationalContext != null)
         {
            sb.append("[private NS=").append(navigationalContext.getOpaqueValue()).append("]")
               .append("[public NS=").append(navigationalContext.getPublicValues()).append("]");
         }
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

   public static org.oasis.wsrp.v2.LocalizedString convertToWSRPLocalizedString(LocalizedString regLocalizedString)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(regLocalizedString, "LocalizedString");
      return WSRPTypeFactory.createLocalizedString(toString(regLocalizedString.getLocale()),
         regLocalizedString.getResourceName(), regLocalizedString.getValue());
   }

   private static LocalizedString getLocalizedStringOrNull(org.oasis.wsrp.v2.LocalizedString wsrpLocalizedString)
   {
      if (wsrpLocalizedString == null)
      {
         return null;
      }
      else
      {
         return convertToRegistrationLocalizedString(wsrpLocalizedString);
      }
   }

   /**
    * @param wsrpLocalizedString
    * @return
    * @since 2.6
    */
   public static LocalizedString convertToRegistrationLocalizedString(org.oasis.wsrp.v2.LocalizedString wsrpLocalizedString)
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

   public static String getAbsoluteURLFor(String url, boolean checkWSRPToken, String serverAddress)
   {
      // We don't encode URL through this API when it is a wsrp URL
      if (checkWSRPToken && url.startsWith(WSRPRewritingConstants.BEGIN_WSRP_REWRITE))
      {
         return url;
      }

      if (!URLTools.isNetworkURL(url) && url.startsWith(URLTools.SLASH))
      {
         return serverAddress + url;
      }
      else
      {
         return url;
      }
   }

   /**
    * Todo: Should be moved to common module?
    *
    * @param fromList
    * @param function
    * @param <F>
    * @param <T>
    * @return
    */
   public static <F, T> List<T> transform(List<F> fromList, Function<? super F, ? extends T> function)
   {
      if (fromList == null)
      {
         return null;
      }
      else
      {
         return Lists.transform(fromList, function);
      }
   }

   public static org.gatein.common.i18n.LocalizedString convertToCommonLocalizedStringOrNull(org.oasis.wsrp.v2.LocalizedString wsrpLocalizedString)
   {
      if (wsrpLocalizedString != null)
      {
         return new org.gatein.common.i18n.LocalizedString(wsrpLocalizedString.getValue(),
            getLocale(wsrpLocalizedString.getLang()));
      }

      return null;
   }

   public static Map<String, String[]> createPublicNSFrom(List<NamedString> publicParams)
   {
      // GTNWSRP-38: public NS
      Map<String, String[]> publicNS = new HashMap<String, String[]>(publicParams.size());
      for (NamedString publicParam : publicParams)
      {
         String paramName = publicParam.getName();
         addMultiValuedValueTo(publicNS, paramName, publicParam.getValue());
      }
      return publicNS;
   }

   public static void addMultiValuedValueTo(Map<String, String[]> paramMap, String paramName, String paramValue)
   {
      String[] values = paramMap.get(paramName);
      if (ParameterValidation.existsAndIsNotEmpty(values))
      {
         int valuesNb = values.length;
         String[] newValues = new String[valuesNb + 1];
         System.arraycopy(values, 0, newValues, 0, valuesNb);
         newValues[valuesNb] = paramValue;
         paramMap.put(paramName, newValues);
      }
      else
      {
         values = new String[]{paramValue};
         paramMap.put(paramName, values);
      }
   }

   /**
    * Encodes the public NS according to the rules found at <a href='http://docs.oasis-open.org/wsrp/v2/wsrp-2.0-spec-os-01.html#_wsrp-navigationalValues'>
    * http://docs.oasis-open.org/wsrp/v2/wsrp-2.0-spec-os-01.html#_wsrp-navigationalValues</a>
    *
    * @param publicNSChanges
    * @return
    */
   public static String encodePublicNS(Map<String, String[]> publicNSChanges)
   {
      if (ParameterValidation.existsAndIsNotEmpty(publicNSChanges))
      {
         StringBuilder sb = new StringBuilder(128);

         Set<Map.Entry<String, String[]>> entries = publicNSChanges.entrySet();
         int entryNb = entries.size();
         int currentEntry = 0;
         for (Map.Entry<String, String[]> entry : entries)
         {
            String name = entry.getKey();
            String[] values = entry.getValue();

            if (ParameterValidation.existsAndIsNotEmpty(values))
            {
               int valueNb = values.length;
               int currentValueIndex = 0;
               for (String value : values)
               {
                  sb.append(name).append("=").append(value);
                  if (currentValueIndex++ != valueNb - 1)
                  {
                     sb.append("&");
                  }
               }
            }
            else
            {
               sb.append(name);
            }

            if (currentEntry++ != entryNb - 1)
            {
               sb.append("&");
            }
         }

         return URLTools.encodeXWWWFormURL(sb.toString());
      }
      else
      {
         return null;
      }
   }

   public static Map<String, String[]> decodePublicNS(String paramValue)
   {
      if (!ParameterValidation.isNullOrEmpty(paramValue))
      {
         String encodedURL = URLTools.decodeXWWWFormURL(paramValue);
         Map<String, String[]> publicNS = new HashMap<String, String[]>(7);

         boolean finished = false;
         while (encodedURL.length() > 0 && !finished)
         {
            int endParamIndex = encodedURL.indexOf(WSRPPortletURL.AMPERSAND);
            String param;
            if (endParamIndex < 0)
            {
               // no param left: try the remainder of the String
               param = encodedURL;
               finished = true;
            }
            else
            {
               param = encodedURL.substring(0, endParamIndex);
            }

            int equalsIndex = param.indexOf(WSRPPortletURL.EQUALS);
            if (equalsIndex < 0)
            {
               publicNS.put(param, null);
            }
            else
            {
               // extract param name
               String name = param.substring(0, equalsIndex);
               // extract param value
               String value = param.substring(equalsIndex + WSRPPortletURL.EQUALS.length(), param.length());

               addMultiValuedValueTo(publicNS, name, value);
            }
            encodedURL = encodedURL.substring(endParamIndex + WSRPPortletURL.AMPERSAND.length());
         }

         return publicNS;
      }
      else
      {
         return null;
      }
   }

   public static PropertyAccessor getPropertyAccessor()
   {
      return propertyAccessor;
   }

   static PropertyAccessor getPropertyAccessor(boolean reload)
   {
      propertyAccessor = new DefaultPropertyAccessor();
      return propertyAccessor;
   }

   /**
    * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
    * @version $Revision$
    */
   public static class AbsoluteURLReplacementGenerator extends URLTools.URLReplacementGenerator
   {
      private String serverAddress;

      public AbsoluteURLReplacementGenerator(HttpServletRequest request)
      {
         serverAddress = URLTools.getServerAddressFrom(request);
      }

      public String getReplacementFor(int i, URLTools.URLMatch urlMatch)
      {
         return getAbsoluteURLFor(urlMatch.getURLAsString());
      }

      String getAbsoluteURLFor(String url)
      {
         return WSRPUtils.getAbsoluteURLFor(url, true, serverAddress);
      }
   }

   private static class DefaultPropertyAccessor implements PropertyAccessor
   {
      private boolean urlRewritingActive = !Boolean.parseBoolean(System.getProperty(DEACTIVATE_URL_REWRITING));

      @Override
      public boolean isURLRewritingActive()
      {
         return urlRewritingActive;
      }
   }
}
