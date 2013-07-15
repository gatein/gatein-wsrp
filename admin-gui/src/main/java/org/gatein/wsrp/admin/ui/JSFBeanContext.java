/*
 * JBoss, a division of Red Hat
 * Copyright 2011, Red Hat Middleware, LLC, and individual
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

package org.gatein.wsrp.admin.ui;

import org.gatein.common.net.URLTools;
import org.gatein.common.util.ParameterValidation;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.Locale;
import java.util.Map;

/**
 * Provides a JSF-backed BeanContext implementation. This is the main implementation since the UI is currently based on JSF.
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 13413 $
 * @since 2.6
 */
public class JSFBeanContext extends BeanContext implements Serializable
{
   public String getParameter(String key)
   {
      return getParameter(key, FacesContext.getCurrentInstance());
   }

   public static String getParameter(String key, FacesContext facesContext)
   {
      Map pmap = facesContext.getExternalContext().getRequestParameterMap();
      return (String)pmap.get(key);
   }

   public Map<String, Object> getSessionMap()
   {
      return JSFBeanContext.getSessionMap(FacesContext.getCurrentInstance());
   }

   @Override
   public <T> T findBean(String name, Class<T> type)
   {
      final FacesContext facesContext = FacesContext.getCurrentInstance();

      // try to get the bean from the application map first
      final Map<String, Object> applicationMap = facesContext.getExternalContext().getApplicationMap();
      Object candidate = applicationMap.get(name);

      if (candidate == null)
      {
         // try the session map
         candidate = getFromSession(name, type);

         if (candidate == null)
         {
            // try to get the bean from an EL expression
            candidate = facesContext.getApplication().evaluateExpressionGet(facesContext, "#{" + name + "}", type);
         }
      }

      if (candidate != null)
      {
         return checkObject(candidate, type, "Bean named '" + name + "' is not of type '" + type.getSimpleName() + "'");
      }
      else
      {
         return null;
      }
   }

   @Override
   public void putInFlash(String name, Object value)
   {
      final Flash flash = FacesContext.getCurrentInstance().getExternalContext().getFlash();
      if (value != null)
      {
         flash.put(name, value);
         flash.putNow(name, value);
         flash.keep(name);
      }
      else
      {
         flash.remove(name);
      }
   }

   @Override
   public <T> T getFromFlash(String name, Class<T> type)
   {
      final Flash flash = FacesContext.getCurrentInstance().getExternalContext().getFlash();
      final Object result = flash.get(name);
      if (result != null)
      {
         return checkObject(result, type, "Flash-scoped object named '" + name + "' is not of type '" + type.getSimpleName() + "'");
      }
      else
      {
         return null;
      }
   }

   public static Map<String, Object> getSessionMap(FacesContext facesContext)
   {
      return facesContext.getExternalContext().getSessionMap();
   }

   protected void createMessage(String target, String message, Object severity, Object... additionalParams)
   {
      outputMessage(target, message, severity, additionalParams);
   }

   public static void outputMessage(String target, String message, Object severity, Object... additionalParams)
   {
      if (ParameterValidation.isNullOrEmpty(target))
      {
         target = STATUS;
      }

      FacesMessage.Severity jsfSeverity;
      if (severity instanceof FacesMessage.Severity)
      {
         jsfSeverity = (FacesMessage.Severity)severity;
      }
      else
      {
         jsfSeverity = FacesMessage.SEVERITY_ERROR;
      }

      // Get the component id from the target
      FacesContext facesContext = FacesContext.getCurrentInstance();
      UIViewRoot viewRoot = facesContext.getViewRoot();
      UIComponent component = viewRoot.findComponent(target);
      if (component != null)
      {
         target = component.getClientId(facesContext);
      }
      else
      {
         // todo: do something better here
         log.info("Couldn't resolve component target: " + target);
      }

      String details = ""; // details is empty to avoid repetition of message...

      // todo: this should be more generic
      if (additionalParams != null && additionalParams.length > 0)
      {
         Exception exception = (Exception)additionalParams[0];
         if (exception != null)
         {
            details = exception.getLocalizedMessage();
         }
      }

      FacesMessage msg = new FacesMessage(jsfSeverity, message, details);
      facesContext.addMessage(target, msg);
   }

   public static void outputLocalizedMessage(String target, String localizationKey, Object severity, String resourceName, Object... params)
   {
      if (severity == null)
      {
         severity = FacesMessage.SEVERITY_ERROR;
      }

      outputMessage(target, getLocalizedMessage(localizationKey, getRequestLocale(), resourceName, params), severity);
   }

   protected Object getErrorSeverity()
   {
      return FacesMessage.SEVERITY_ERROR;
   }

   protected Object getInfoSeverity()
   {
      return FacesMessage.SEVERITY_INFO;
   }

   protected Locale getLocale()
   {
      return getRequestLocale();
   }

   public String getServerAddress()
   {
      Object request = FacesContext.getCurrentInstance().getExternalContext().getRequest();
      String serverAddress;
      if (request instanceof PortletRequest)
      {
         // basically copy URLTools.getServerAddressFrom implementation
         PortletRequest portletRequest = (PortletRequest)request;
         String scheme = portletRequest.getScheme();
         String host = portletRequest.getServerName();
         int port = portletRequest.getServerPort();

         return scheme + URLTools.SCH_END + host + URLTools.PORT_END + port;
      }
      else
      {
         serverAddress = URLTools.getServerAddressFrom((HttpServletRequest)request);
      }

      return serverAddress;
   }

   public static Locale getRequestLocale()
   {
      return FacesContext.getCurrentInstance().getExternalContext().getRequestLocale();
   }
}
