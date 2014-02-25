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

import org.gatein.common.net.URLTools;
import org.gatein.common.util.ParameterValidation;
import org.gatein.pc.api.Mode;
import org.gatein.pc.api.StateString;
import org.gatein.pc.api.WindowState;
import org.gatein.pc.api.cache.CacheLevel;
import org.oasis.wsrp.v2.GetResource;
import org.oasis.wsrp.v2.NamedString;
import org.oasis.wsrp.v2.PortletContext;
import org.oasis.wsrp.v2.RegistrationContext;
import org.oasis.wsrp.v2.ResourceParams;
import org.oasis.wsrp.v2.RuntimeContext;
import org.oasis.wsrp.v2.StateChange;

import javax.servlet.http.HttpServletRequest;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class ResourceServingUtil
{
   private static final String REG_HANDLE = "regHandle";
   private static final String INSTANCE_KEY = "instanceKey";
   private static final String NS = "ns";
   private static final String MODE = "mode";
   private static final String WINDOW_STATE = "windowState";
   private static final String RESOURCE_STATE = "resourceState";
   private static final String NAV_STATE = "navState";
   private static final String SLASH_REPLACEMENT = "__";
   private static final String QMARK = "?";

   public static GetResource decode(HttpServletRequest req)
   {
      String path = req.getPathInfo();

      int portletHandleEnd = path.indexOf(URLTools.SLASH, 1);
      String portletHandle = path.substring(1, portletHandleEnd);
      PortletContext portletContext = decode(portletHandle);

      String resourceId = path.substring(portletHandleEnd);

      String registrationHandle = req.getParameter(REG_HANDLE);
      RegistrationContext registrationContext = null;
      if (!ParameterValidation.isNullOrEmpty(registrationHandle))
      {
         registrationContext = WSRPTypeFactory.createRegistrationContext(registrationHandle);
      }

      String instanceKey = URLTools.decodeXWWWFormURL(req.getParameter(INSTANCE_KEY));
      String ns = req.getParameter(NS);
      RuntimeContext runtimeContext = WSRPTypeFactory.createRuntimeContext(WSRPConstants.NONE_USER_AUTHENTICATION, instanceKey, ns);

      Enumeration reqLocales = req.getLocales();
      List<String> locales = WSRPUtils.convertLocalesToRFC3066LanguageTags(Collections.list(reqLocales));
      List<String> mimeTypes = WSRPConstants.getDefaultMimeTypes();

      Map<String, String[]> parameters = req.getParameterMap();

      ResourceParams resourceParams = WSRPTypeFactory.createResourceParams(req.isSecure(), locales, mimeTypes,
         WSRPConstants.VIEW_MODE, WSRPConstants.NORMAL_WINDOW_STATE, resourceId, StateChange.READ_ONLY, parameters);
      resourceParams.setResourceState(req.getParameter(RESOURCE_STATE));

      String navState = req.getParameter(NAV_STATE);
      if (!ParameterValidation.isNullOrEmpty(navState))
      {
         resourceParams.setNavigationalContext(WSRPTypeFactory.createNavigationalContext(navState, Collections.<NamedString>emptyList()));
      }

      return WSRPTypeFactory.createGetResource(registrationContext, portletContext, runtimeContext, null, resourceParams);
   }

   public static URL encode(Mode mode, WindowState windowState, boolean secure, StateString navigationalState, StateString resourceState, String resourceId, CacheLevel cacheability, WSRPPortletURL.URLContext context)
   {
      // Generate a resource URL based on the resource Id
      String serverAddress = (String)context.getValueFor(WSRPPortletURL.URLContext.SERVER_ADDRESS);
      org.gatein.pc.api.PortletContext portletContext = (org.gatein.pc.api.PortletContext)context.getValueFor(WSRPPortletURL.URLContext.PORTLET_CONTEXT);
      try
      {
         StringBuilder sb = new StringBuilder(createAbsoluteURLFrom(resourceId, serverAddress, portletContext));
         appendParameter(sb, MODE, mode);
         appendParameter(sb, WINDOW_STATE, windowState);

         // instance key can contain a space if it's based on the portlet context so we need to also encode it
         String instanceKey = (String)context.getValueFor(WSRPPortletURL.URLContext.INSTANCE_KEY);
         instanceKey = URLTools.encodeXWWWFormURL(instanceKey);
         appendParameter(sb, INSTANCE_KEY, instanceKey);

         appendParameter(sb, NS, context.getValueFor(WSRPPortletURL.URLContext.NAMESPACE));
         appendParameter(sb, REG_HANDLE, context.getValueFor(WSRPPortletURL.URLContext.REGISTRATION_HANDLE));
         if (resourceState != null)
         {
            appendParameter(sb, RESOURCE_STATE, resourceState.getStringValue());
         }
         if (navigationalState != null)
         {
            appendParameter(sb, NAV_STATE, navigationalState.getStringValue());
         }

         return new URI(sb.toString()).toURL();
      }
      catch (Exception e)
      {
         throw new RuntimeException("Couldn't create an absolute URL from resourceId: " + resourceId + ", server address: " + serverAddress
            + ", portlet: " + portletContext, e);
      }
   }

   private static String createAbsoluteURLFrom(String resourceId, String serverAddress, org.gatein.pc.api.PortletContext portletContext)
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(resourceId, "resource ID", null);
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(serverAddress, "server address", null);
      ParameterValidation.throwIllegalArgExceptionIfNull(portletContext, "portlet context");

      String url = serverAddress + "/wsrp-producer/resource/";

      url += encode(portletContext);

      if (resourceId.startsWith(URLTools.SLASH))
      {
         url += resourceId + QMARK;
      }
      else
      {
         url += URLTools.SLASH + resourceId + QMARK;
      }

      return url;
   }

   private static String encode(org.gatein.pc.api.PortletContext portletContext)
   {
      String id = portletContext.getId();
      if (id.startsWith(URLTools.SLASH))
      {
         id = id.replace(URLTools.SLASH, SLASH_REPLACEMENT);
      }
      return URLTools.encodeXWWWFormURL(id);
   }

   private static PortletContext decode(String encodedPortletContext)
   {
      if (encodedPortletContext.startsWith(SLASH_REPLACEMENT))
      {
         encodedPortletContext = encodedPortletContext.replace(SLASH_REPLACEMENT, URLTools.SLASH);
      }

      return WSRPTypeFactory.createPortletContext(URLTools.decodeXWWWFormURL(encodedPortletContext));
   }

   private static void appendParameter(StringBuilder builder, String name, Object value)
   {
      if (value != null)
      {
         builder.append("&").append(name).append("=").append(value);
      }
   }
}
