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

import org.gatein.common.net.URLTools;
import org.gatein.common.util.MarkupInfo;
import org.gatein.pc.api.ContainerURL;
import org.gatein.pc.api.URLFormat;
import org.gatein.pc.api.invocation.PortletInvocation;
import org.gatein.pc.api.spi.PortalContext;
import org.gatein.pc.api.spi.PortletInvocationContext;
import org.gatein.pc.api.spi.SecurityContext;
import org.gatein.pc.api.spi.UserContext;
import org.gatein.pc.api.spi.WindowContext;
import org.gatein.pc.portlet.impl.spi.AbstractClientContext;
import org.gatein.pc.portlet.impl.spi.AbstractPortletInvocationContext;
import org.gatein.pc.portlet.impl.spi.AbstractServerContext;
import org.gatein.registration.Registration;
import org.gatein.registration.RegistrationLocal;
import org.gatein.wsrp.WSRPPortletURL;
import org.gatein.wsrp.WSRPRewritingConstants;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.WSRPUtils;
import org.gatein.wsrp.servlet.ServletAccess;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 13121 $
 */
class WSRPPortletInvocationContext extends AbstractPortletInvocationContext implements PortletInvocationContext
{
   private SecurityContext securityContext;
   private PortalContext portalContext;
   private UserContext userContext;
   private WSRPInstanceContext instanceContext;
   private WindowContext windowContext;

   private static final String EQ = "=";
   private static final String AMP = "&amp;";
   private static final String EQ_TRUE = "=true";
   private HttpServletRequest request;
   private HttpServletResponse response;

   public WSRPPortletInvocationContext(MarkupInfo markupInfo, SecurityContext securityContext, PortalContext portalContext, UserContext userContext,
                                       WSRPInstanceContext instanceContext, WindowContext windowContext)
   {
      super(markupInfo);

      this.securityContext = securityContext;
      this.portalContext = portalContext;
      this.userContext = userContext;
      this.instanceContext = instanceContext;
      this.windowContext = windowContext;

      request = ServletAccess.getRequest();
      response = ServletAccess.getResponse();
   }

   public HttpServletRequest getClientRequest()
   {
      return request;
   }

   public HttpServletResponse getClientResponse()
   {
      return response;
   }

   /** Override the default behavior in order to avoid to encode when it is producer written URL. */
   public String encodeResourceURL(String url)
   {
      if (url != null && !url.startsWith(WSRPRewritingConstants.BEGIN_WSRP_REWRITE))
      {
         // make root relative URLs absolute. Optimization: we don't recheck the presence of the WSRP token.
         url = WSRPUtils.getAbsoluteURLFor(url, false, URLTools.getServerAddressFrom(getClientRequest()));

         // properly encode the URL
         url = URLTools.encodeXWWWFormURL(url);

         // build the WSRP resource URL with rewrite tokens
         StringBuffer sb = new StringBuffer(url.length() * 2);
         sb.append(WSRPRewritingConstants.BEGIN_WSRP_REWRITE).append(WSRPRewritingConstants.URL_TYPE_NAME)
            .append(EQ).append(WSRPRewritingConstants.URL_TYPE_RESOURCE).append(AMP)
            .append(WSRPRewritingConstants.RESOURCE_URL).append(EQ).append(url)
            .append(AMP).append(WSRPRewritingConstants.RESOURCE_REQUIRES_REWRITE)
            .append(EQ_TRUE).append(WSRPRewritingConstants.END_WSRP_REWRITE);
         return sb.toString();
      }

      return url;
   }

   /**
    * <p>URL to be re-written are of the form: <code>wsrp_rewrite?wsrp-urlType=value&amp;amp;name1=value1&amp;amp;name2=value2
    * .../wsrp_rewrite</code> </p> <ul>Examples: <li>Load a resource http://test.com/images/test.gif: <br/>
    * <code>wsrp_rewrite?wsrp-urlType=resource&amp;amp;wsrp-url=http%3A%2F%2Ftest.com%2Fimages%2Ftest.gif&amp;amp;wsrp-requiresRewrite=true/wsrp_rewrite</code></li>
    * <li>Declare a secure interaction back to the Portlet:<br/> <code>wsrp_rewrite?wsrp-urlType=blockingAction&amp;amp;wsrp-secureURL=true&amp;amp;wsrp-navigationalState=a8h4K5JD9&amp;amp;wsrp-interactionState=fg4h923mdk/wsrp_rewrite</code></li>
    * <li>Request the Consumer render the Portlet in a different mode and window state:
    * <code>wsrp_rewrite?wsrp-urlType=render&amp;amp;wsrp-mode=help&amp;amp;wsrp-windowState=maximized/wsrp_rewrite</code></li>
    * </ul>
    *
    * @param containerURL
    * @param urlFormat
    * @return
    */
   public String renderURL(ContainerURL containerURL, URLFormat urlFormat)
   {
      if (containerURL != null)
      {
         Boolean wantSecureBool = urlFormat.getWantSecure();
         boolean wantSecure = (wantSecureBool != null ? wantSecureBool : false);
         WSRPPortletURL.URLContext context = new WSRPPortletURL.URLContext(WSRPPortletURL.URLContext.SERVER_ADDRESS,
            URLTools.getServerAddressFrom(request), WSRPPortletURL.URLContext.PORTLET_CONTEXT, instanceContext.getPortletContext());

         Registration registration = RegistrationLocal.getRegistration();
         if (registration != null)
         {
            context.setValueFor(WSRPPortletURL.URLContext.REGISTRATION_HANDLE, registration.getRegistrationHandle());
         }
         context.setValueFor(WSRPPortletURL.URLContext.INSTANCE_KEY, WSRPTypeFactory.getPortletInstanceKey(instanceContext));
         context.setValueFor(WSRPPortletURL.URLContext.NAMESPACE, WSRPTypeFactory.getNamespacePrefix(windowContext, instanceContext.getPortletContext().getId()));

         WSRPPortletURL url = WSRPPortletURL.create(containerURL, wantSecure, context);
         return url.toString();
      }
      return null;
   }

   public void contextualize(PortletInvocation invocation)
   {
      invocation.setClientContext(new AbstractClientContext(request));
      invocation.setServerContext(new AbstractServerContext(request, response));

      invocation.setSecurityContext(securityContext);
      invocation.setInstanceContext(instanceContext);
      invocation.setWindowContext(windowContext);
      invocation.setPortalContext(portalContext);
      invocation.setUserContext(userContext);
   }

   WindowContext getWindowContext()
   {
      return windowContext;
   }
}
