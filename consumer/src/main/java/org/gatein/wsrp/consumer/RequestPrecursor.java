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

package org.gatein.wsrp.consumer;

import org.gatein.common.util.ContentInfo;
import org.gatein.common.util.ParameterValidation;
import org.gatein.pc.api.PortletInvokerException;
import org.gatein.pc.api.StateString;
import org.gatein.pc.api.invocation.PortletInvocation;
import org.gatein.pc.api.spi.PortletInvocationContext;
import org.gatein.pc.api.spi.SecurityContext;
import org.gatein.pc.api.spi.UserContext;
import org.gatein.wsrp.WSRPConstants;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.WSRPUtils;
import org.oasis.wsrp.v2.MarkupParams;
import org.oasis.wsrp.v2.NavigationalContext;
import org.oasis.wsrp.v2.PortletContext;
import org.oasis.wsrp.v2.RuntimeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;

/**
 * Extracts basic required elements for all invocation requests.
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 13121 $
 * @since 2.4
 */
class RequestPrecursor
{
   private final static Logger log = LoggerFactory.getLogger(RequestPrecursor.class);

   private PortletContext portletContext;
   RuntimeContext runtimeContext;
   MarkupParams markupParams;
   private static final String PORTLET_HANDLE = "portlet handle";
   private static final String SECURITY_CONTEXT = "security context";
   private static final String USER_CONTEXT = "user context";
   private static final String INVOCATION_CONTEXT = "invocation context";
   private static final String STREAM_INFO = "stream info in invocation context";
   private static final String USER_AGENT = "User-Agent";

   public RequestPrecursor(WSRPConsumerImpl wsrpConsumer, PortletInvocation invocation) throws PortletInvokerException
   {
      // retrieve handle
      portletContext = WSRPUtils.convertToWSRPPortletContext(WSRPConsumerImpl.getPortletContext(invocation));
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(getPortletHandle(), PORTLET_HANDLE, null);
      if (log.isDebugEnabled())
      {
         log.debug("About to invoke on portlet: " + getPortletHandle());
      }

      // create runtime context
      SecurityContext securityContext = invocation.getSecurityContext();
      ParameterValidation.throwIllegalArgExceptionIfNull(securityContext, SECURITY_CONTEXT);
      String authType = WSRPUtils.convertRequestAuthTypeToWSRPAuthType(securityContext.getAuthType());
      runtimeContext = WSRPTypeFactory.createRuntimeContext(authType);

      // set the session id if needed
      wsrpConsumer.getSessionHandler().setSessionIdIfNeeded(invocation, runtimeContext, getPortletHandle());

      wsrpConsumer.setTemplatesIfNeeded(invocation, runtimeContext);

      // create markup params
      UserContext userContext = invocation.getUserContext();
      ParameterValidation.throwIllegalArgExceptionIfNull(userContext, USER_CONTEXT);
      PortletInvocationContext context = invocation.getContext();
      ParameterValidation.throwIllegalArgExceptionIfNull(context, INVOCATION_CONTEXT);
      ContentInfo streamInfo = context.getMarkupInfo();
      ParameterValidation.throwIllegalArgExceptionIfNull(streamInfo, STREAM_INFO);

      String mode;
      try
      {
         mode = WSRPUtils.getWSRPNameFromJSR168PortletMode(invocation.getMode());
      }
      catch (Exception e)
      {
         log.debug("Mode was null in context.");
         mode = WSRPConstants.VIEW_MODE;
      }

      String windowState;
      try
      {
         windowState = WSRPUtils.getWSRPNameFromJSR168WindowState(invocation.getWindowState());
      }
      catch (Exception e)
      {
         log.debug("WindowState was null in context.");
         windowState = WSRPConstants.NORMAL_WINDOW_STATE;
      }

      markupParams = WSRPTypeFactory.createMarkupParams(securityContext.isSecure(),
         WSRPUtils.convertLocalesToRFC3066LanguageTags(userContext.getLocales()),
         Collections.singletonList(streamInfo.getMediaType().getValue()), mode, windowState);
      String userAgent = WSRPConsumerImpl.getHttpRequest(invocation).getHeader(USER_AGENT);
      markupParams.setClientData(WSRPTypeFactory.createClientData(userAgent));

      // navigational state
      StateString navigationalState = invocation.getNavigationalState();
      Map<String,String[]> publicNavigationalState = invocation.getPublicNavigationalState();
      NavigationalContext navigationalContext = WSRPTypeFactory.createNavigationalContextOrNull(navigationalState, publicNavigationalState);
      markupParams.setNavigationalContext(navigationalContext);

      if (log.isDebugEnabled())
      {
         log.debug(WSRPUtils.toString(markupParams));
      }
   }

   public String getPortletHandle()
   {
      return portletContext.getPortletHandle();
   }


   public PortletContext getPortletContext()
   {
      return portletContext;
   }
}
