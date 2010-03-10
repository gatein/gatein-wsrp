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

import org.gatein.common.text.FastURLDecoder;
import org.gatein.common.text.TextTools;
import org.gatein.pc.api.ActionURL;
import org.gatein.pc.api.ContainerURL;
import org.gatein.pc.api.Mode;
import org.gatein.pc.api.OpaqueStateString;
import org.gatein.pc.api.ParametersStateString;
import org.gatein.pc.api.RenderURL;
import org.gatein.pc.api.ResourceURL;
import org.gatein.pc.api.StateString;
import org.gatein.pc.api.WindowState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 13470 $
 * @since 2.4 (Apr 28, 2006)
 */
public abstract class WSRPPortletURL implements ContainerURL
{
   private static final Logger log = LoggerFactory.getLogger(WSRPPortletURL.class);

   private static final String EQUALS = "=";

   private static final String ENCODED_AMPERSAND = "&amp;";
   private static final String AMPERSAND = "&";
   private static final String AMP_AMP = "&amp;amp;";

   private static final String PARAM_SEPARATOR = "|";
   private static final int URL_TYPE_END = WSRPRewritingConstants.URL_TYPE_NAME.length() + EQUALS.length();
   private boolean secure;

   private Mode mode;

   private WindowState windowState;

   /** Are we using strict rewriting parameters validation mode? */
   protected static boolean strict = true;
   /** Holds extra parameters if we are in relaxed validation mode */
   private Map<String, String> extraParams;
   /** Holds extra data after URL in relaxed mode */
   protected String extra;
   /** Remember position of extra parameters wrt end token */
   private boolean extraParamsAfterEndToken = false;
   protected StateString navigationalState;

   public static void setStrict(boolean strict)
   {
      WSRPPortletURL.strict = strict;
      log.debug("Using " + (strict ? "strict" : "lenient") + " rewriting parameters validation mode.");
   }

   public static WSRPPortletURL create(ContainerURL containerURL, boolean secure)
   {
      if (containerURL == null)
      {
         throw new IllegalArgumentException("Cannot construct a WSRPPortletURL from a null PortletURL!");
      }

      Mode mode = containerURL.getMode();
      WindowState windowState = containerURL.getWindowState();
      StateString navigationalState = containerURL.getNavigationalState();

      WSRPPortletURL url;
      if (containerURL instanceof ActionURL)
      {
         StateString interactionState = ((ActionURL)containerURL).getInteractionState();
         url = new WSRPActionURL(mode, windowState, secure, navigationalState, interactionState);
      }
      else if (containerURL instanceof RenderURL)
      {
         url = new WSRPRenderURL(mode, windowState, secure, navigationalState, ((RenderURL)containerURL).getPublicNavigationalStateChanges());
      }
      else if (containerURL instanceof ResourceURL)
      {
         ResourceURL resource = (ResourceURL)containerURL;
         url = new WSRPResourceURL(mode, windowState, secure, navigationalState, resource.getResourceState(),
            resource.getResourceId(), resource.getCacheability());
      }
      else
      {
         throw new IllegalArgumentException("Unknown PortletURL type: " + containerURL.getClass().getName());
      }

      // if we're in relaxed mode, we need to deal with extra params as well
      if (strict && containerURL instanceof WSRPPortletURL)
      {
         WSRPPortletURL other = (WSRPPortletURL)containerURL;
         url.setParams(other.extraParams, other.toString());
         url.setExtra(other.extra);
      }

      return url;
   }

   public static WSRPPortletURL create(String encodedURL, Set<String> customModes, Set<String> customWindowStates)
   {
      if (log.isDebugEnabled())
      {
         log.debug("Trying to build a WSRPPortletURL from <" + encodedURL + ">");
      }

      if (encodedURL == null || encodedURL.length() == 0)
      {
         throw new IllegalArgumentException("Cannot construct a WSRPPortletURL from a null or empty URL!");
      }

      String originalURL = encodedURL;
      boolean extraAfterEnd = false;
      String extra = null;

      // URL needs to start wsrp_rewrite? and end with /wsrp_rewrite in strict validation mode
      if (!encodedURL.startsWith(WSRPRewritingConstants.BEGIN_WSRP_REWRITE))
      {
         throw new IllegalArgumentException(encodedURL + " does not start with " + WSRPRewritingConstants.BEGIN_WSRP_REWRITE);
      }
      if (!encodedURL.endsWith(WSRPRewritingConstants.END_WSRP_REWRITE))
      {
         // first remove prefix only (as suffix is not at the end of the string)
         encodedURL = encodedURL.substring(WSRPRewritingConstants.WSRP_REWRITE_PREFIX_LENGTH);

         // end token should be marked by the first / in the URL and extract it
         int endTokenIndex = encodedURL.indexOf('/');
         if (endTokenIndex < 0)
         {
            throw new IllegalArgumentException(originalURL + " does not contain " + WSRPRewritingConstants.END_WSRP_REWRITE);
         }

         encodedURL = encodedURL.substring(0, endTokenIndex)
            + encodedURL.substring(endTokenIndex + WSRPRewritingConstants.WSRP_REWRITE_SUFFIX_LENGTH);

         /*
         we need to deal with the case when a WSRP URL is concatenated to a context path using something similar to:
         renderResponse.encodeURL(renderRequest.getContextPath()) in which case, there should be a slash still present.
         How to process further depends on whether we're in strict mode or not...
         */
         int concatenationIndex = encodedURL.indexOf('/');

         if (strict && concatenationIndex != endTokenIndex)
         {
            // in strict mode, the only character available after the end token is the concatenating slash
            throw new IllegalArgumentException(encodedURL + " does not end with "
               + WSRPRewritingConstants.END_WSRP_REWRITE + " or does not appear to be a valid concatenation of URLs.");
         }
         else
         {
            // deal with extra characters: this should only happen when the URL is concatenated to form a longer one
            // hence, it should be possible to have param-value pairs followed by a slash '/' then characters.
            // Anything after the slash will be kept as is, uninterpreted.
            if (concatenationIndex != -1)
            {
               String tmp = encodedURL;
               encodedURL = encodedURL.substring(0, concatenationIndex);
               extra = tmp.substring(concatenationIndex);
            }

            // remember that we should position the extra params after the end token
            extraAfterEnd = true;
         }
      }
      else
      {
         // remove prefix and suffix
         encodedURL = encodedURL.substring(WSRPRewritingConstants.WSRP_REWRITE_PREFIX_LENGTH,
            encodedURL.length() - WSRPRewritingConstants.WSRP_REWRITE_SUFFIX_LENGTH);
      }

      // next param should be the url type
      if (!encodedURL.startsWith(WSRPRewritingConstants.URL_TYPE_NAME + EQUALS))
      {
         throw new IllegalArgumentException(originalURL + " does not specify a URL type.");
      }

      // standardize parameter separators
      if (encodedURL.contains(AMP_AMP))
      {
         throw new IllegalArgumentException(encodedURL + " contains a doubly encoded &!");
      }
      encodedURL = TextTools.replace(encodedURL, ENCODED_AMPERSAND, PARAM_SEPARATOR);
      encodedURL = TextTools.replace(encodedURL, AMPERSAND, PARAM_SEPARATOR);

      // remove url type param name and extract value
      encodedURL = encodedURL.substring(URL_TYPE_END);
      String urlType;
      WSRPPortletURL url;
      if (encodedURL.startsWith(WSRPRewritingConstants.URL_TYPE_RENDER))
      {
         urlType = WSRPRewritingConstants.URL_TYPE_RENDER;
         url = new WSRPRenderURL();
      }
      else if (encodedURL.startsWith(WSRPRewritingConstants.URL_TYPE_BLOCKING_ACTION))
      {
         urlType = WSRPRewritingConstants.URL_TYPE_BLOCKING_ACTION;
         url = new WSRPActionURL();
      }
      else if (encodedURL.startsWith(WSRPRewritingConstants.URL_TYPE_RESOURCE))
      {
         if (log.isDebugEnabled())
         {
            log.debug("Using experimental resource URL support...");
         }
         urlType = WSRPRewritingConstants.URL_TYPE_RESOURCE;
         url = new WSRPResourceURL();
      }
      else
      {
         throw new IllegalArgumentException("Unrecognized URL type: " + encodedURL.substring(0, encodedURL.indexOf(PARAM_SEPARATOR))
            + "in " + originalURL);
      }

      // other parameters
      Map<String, String> params = null;
      int urlTypeLength = urlType.length();
      if (encodedURL.length() > urlTypeLength)
      {
         // truncate again once the value is extracted
         encodedURL = encodedURL.substring(urlTypeLength + PARAM_SEPARATOR.length());

         // extract the other parameters
         params = extractParams(encodedURL, originalURL, customModes, customWindowStates);
      }
      else if (WSRPRewritingConstants.URL_TYPE_RESOURCE.equals(urlType))
      {
         throw new IllegalArgumentException("Both the 'wsrp-url' and 'wsrp-requiresRewrite' parameters MUST also be specified for resource URL '"
            + originalURL + "'");
      }

      url.setParams(params, originalURL);
      url.setExtraParamsAfterEndToken(extraAfterEnd);
      url.setExtra(extra);
      return url;
   }

   /**
    * Parses a WSRP rewritten URL and extracts each component. <p/> TODO: some values need to be in pairs or are
    * mutually exclusive, check for this <p/> <p>URL are of the form: <code>wsrp_rewrite?wsrp-urlType=value&amp;amp;name1=value1&amp;amp;name2=value2
    * .../wsrp_rewrite</code> </p> <ul>Examples: <li>Load a resource http://test.com/images/test.gif: <br/>
    * <code>wsrp_rewrite?wsrp-urlType=resource&amp;amp;wsrp-url=http%3A%2F%2Ftest.com%2Fimages%2Ftest.gif&amp;amp;wsrp-requiresRewrite=true/wsrp_rewrite</code></li>
    * <li>Declare a secure interaction back to the Portlet:<br/> <code>wsrp_rewrite?wsrp-urlType=blockingAction&amp;amp;wsrp-secureURL=true&amp;amp;wsrp-navigationalState=a8h4K5JD9&amp;amp;wsrp-interactionState=fg4h923mdk/wsrp_rewrite</code></li>
    * <li>Request the Consumer render the Portlet in a different mode and window state:
    * <code>wsrp_rewrite?wsrp-urlType=render&amp;amp;wsrp-mode=help&amp;amp;wsrp-windowState=maximized/wsrp_rewrite</code></li>
    * </ul>
    *
    * @param encodedURL a String representation of the URL to create
    * @return an appropriate WSRPPortletURL as built from parsing the specified String
    */
   public static WSRPPortletURL create(String encodedURL)
   {
      return create(encodedURL, Collections.<String>emptySet(), Collections.<String>emptySet());
   }

   protected WSRPPortletURL(Mode mode, WindowState windowState, boolean secure, StateString navigationalState)
   {
      this.mode = mode;
      this.windowState = windowState;
      this.secure = secure;
      this.navigationalState = navigationalState;
   }

   protected WSRPPortletURL()
   {
   }

   protected final void setParams(Map<String, String> params, String originalURL)
   {
      // First extract specific parameters and remove them from the param map...
      dealWithSpecificParams(params, originalURL);

      // ... then deal with extra params if in relaxed mode
      if (!strict)
      {
         extraParams = new HashMap<String, String>();
         extraParams.putAll(params);
      }
   }

   /**
    * Deal with specific parameters first so that we can remove them before dealing with extra params. Sub-classes
    * override to provide support for their specific parameters.
    *
    * @param params      name-value map of the URL parameters
    * @param originalURL a String reprensenting the URL we are working with
    */
   protected void dealWithSpecificParams(Map<String, String> params, String originalURL)
   {
      // mode
      String paramValue = getRawParameterValueFor(params, WSRPRewritingConstants.MODE);
      if (paramValue != null)
      {
         mode = WSRPUtils.getJSR168PortletModeFromWSRPName(paramValue);
         params.remove(WSRPRewritingConstants.MODE);
      }

      // window state
      paramValue = getRawParameterValueFor(params, WSRPRewritingConstants.WINDOW_STATE);
      if (paramValue != null)
      {
         windowState = WSRPUtils.getJSR168WindowStateFromWSRPName(paramValue);
         params.remove(WSRPRewritingConstants.WINDOW_STATE);
      }

      // secure
      paramValue = getRawParameterValueFor(params, WSRPRewritingConstants.SECURE_URL);
      if (paramValue != null)
      {
         secure = Boolean.valueOf(paramValue);
         params.remove(WSRPRewritingConstants.SECURE_URL);
      }

      // navigational state
      paramValue = getRawParameterValueFor(params, WSRPRewritingConstants.NAVIGATIONAL_STATE);
      if (paramValue != null)
      {
         navigationalState = new OpaqueStateString(paramValue);
         params.remove(WSRPRewritingConstants.NAVIGATIONAL_STATE);
      }
   }

   protected String getRawParameterValueFor(Map params, String parameterName)
   {
      if (params != null)
      {
         return (String)params.get(parameterName);
      }
      else
      {
         return null;
      }
   }

   public Mode getMode()
   {
      return mode;
   }

   public WindowState getWindowState()
   {
      return windowState;
   }

   public boolean isSecure()
   {
      return secure;
   }

   protected abstract String getURLType();

   public String toString()
   {
      StringBuffer sb = new StringBuffer(255);

      //
      sb.append(WSRPRewritingConstants.BEGIN_WSRP_REWRITE).append(WSRPRewritingConstants.URL_TYPE_NAME)
         .append(EQUALS).append(getURLType());

      //
      if (secure)
      {
         createURLParameter(sb, WSRPRewritingConstants.SECURE_URL, "true");
      }

      //
      if (mode != null)
      {
         createURLParameter(sb, WSRPRewritingConstants.MODE, WSRPUtils.getWSRPNameFromJSR168PortletMode(mode));
      }

      //
      if (windowState != null)
      {
         createURLParameter(sb, WSRPRewritingConstants.WINDOW_STATE, WSRPUtils.getWSRPNameFromJSR168WindowState(windowState));
      }

      if (navigationalState != null)
      {
         createURLParameter(sb, WSRPRewritingConstants.NAVIGATIONAL_STATE, navigationalState.getStringValue());
      }

      // todo: not sure how to deal with authenticated

      //
      appendEnd(sb);

      // Finish the URL
      if (strict)
      {
         sb.append(WSRPRewritingConstants.END_WSRP_REWRITE);
      }
      else
      {
         // we're in relaxed mode so we need to deal with extra params if they exist
         if (extraParams != null && !extraParams.isEmpty())
         {
            StringBuffer extras = new StringBuffer();
            appendExtraParams(extras);

            // if we had extra params, we need to figure out where thwy should be positioned wrt end token
            if (extraParamsAfterEndToken)
            {
               sb.append(WSRPRewritingConstants.END_WSRP_REWRITE);
               sb.append(extras);
               if (extra != null)
               {
                  sb.append(extra);
               }
            }
            else
            {
               sb.append(extras);
               sb.append(WSRPRewritingConstants.END_WSRP_REWRITE);
            }
         }
         else
         {
            sb.append(WSRPRewritingConstants.END_WSRP_REWRITE);
         }
      }
      return sb.toString();
   }

   protected void appendExtraParams(StringBuffer buffer)
   {
      if (extraParams != null)
      {
         for (Map.Entry<String, String> entry : extraParams.entrySet())
         {
            createURLParameter(buffer, entry.getKey(), entry.getValue());
         }
      }
   }

   protected abstract void appendEnd(StringBuffer sb);

   protected final void createURLParameter(StringBuffer sb, String name, String value)
   {
      if (value != null)
      {
         sb.append(AMPERSAND).append(name).append(EQUALS).append(value);
      }
   }

   private static Map<String, String> extractParams(String encodedURL, String originalURL, Set<String> customModes, Set<String> customWindowStates)
   {
      Map<String, String> params = new HashMap<String, String>();
      boolean finished = false;
      while (encodedURL.length() > 0 && !finished)
      {
         int endParamIndex = encodedURL.indexOf(PARAM_SEPARATOR);
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

         int equalsIndex = param.indexOf(EQUALS);
         if (equalsIndex < 0)
         {
            throw new IllegalArgumentException(param + " is not a valid parameter for " + originalURL);
         }

         // extract param name
         String name = param.substring(0, equalsIndex);
         if (!name.startsWith("wsrp-"))
         {
            if (strict)
            {
               throw new IllegalArgumentException("Invalid parameter name in strict validation mode (see documentation): '"
                  + name + "' in " + originalURL);
            }
            else
            {
               log.debug("Relaxed validation allowed invalid parameter name: " + name + " in " + originalURL);
            }
         }

         // extract param value
         String value = param.substring(equalsIndex + EQUALS.length(), param.length());

         // check that the given mode is valid if the param is supposed to be one
         if (WSRPRewritingConstants.MODE.equals(name))
         {
            value = checkModeOrWindowState(value, true, customModes);
         }

         // check that the given window state is valid if the param is supposed to be one
         if (WSRPRewritingConstants.WINDOW_STATE.equals(name))
         {
            value = checkModeOrWindowState(value, false, customWindowStates);
         }

         params.put(name, value);

         // unserialize opaque state for debugging purpose
         if (log.isTraceEnabled())
         {
            if (WSRPRewritingConstants.INTERACTION_STATE.equals(name) || WSRPRewritingConstants.NAVIGATIONAL_STATE.equals(name))
            {
               StateString clear = ParametersStateString.create(value);
               log.trace(name + " value:" + clear);
            }
         }

         encodedURL = encodedURL.substring(endParamIndex + PARAM_SEPARATOR.length());
      }
      return params;
   }

   private static String checkModeOrWindowState(String value, boolean mode, Set<String> supportedValues)
   {
      // decode potentially encoded value
      value = FastURLDecoder.getUTF8Instance().encode(value);

      // Check if value is a standard one
      boolean standard;
      if (mode)
      {
         standard = WSRPUtils.isDefaultWSRPMode(value);
      }
      else
      {
         standard = WSRPUtils.isDefaultWSRPWindowState(value);
      }

      // the value is not a standard one
      if (!standard)
      {
         // check if this is a supported custom value
         if (supportedValues.contains(value))
         {
            return value;
         }
         else
         {
            throw new IllegalArgumentException("Unsupported " + (mode ? "mode: " : "window state: ") + value);
         }
      }

      return value;
   }

   private void setExtraParamsAfterEndToken(boolean extraParamsAfterEndToken)
   {
      this.extraParamsAfterEndToken = extraParamsAfterEndToken;
   }

   public void setExtra(String extra)
   {
      this.extra = extra;
   }

   public StateString getNavigationalState()
   {
      return navigationalState;
   }
}
