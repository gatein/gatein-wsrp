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

package org.gatein.wsrp.consumer.handlers;

import org.gatein.pc.api.PortletInvokerException;
import org.gatein.pc.api.invocation.PortletInvocation;
import org.gatein.pc.api.invocation.RenderInvocation;
import org.gatein.pc.api.invocation.response.FragmentResponse;
import org.gatein.pc.api.invocation.response.PortletInvocationResponse;
import org.gatein.pc.api.invocation.response.ResponseProperties;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.consumer.WSRPConsumerImpl;
import org.oasis.wsrp.v2.Extension;
import org.oasis.wsrp.v2.GetMarkup;
import org.oasis.wsrp.v2.MarkupContext;
import org.oasis.wsrp.v2.MarkupResponse;
import org.oasis.wsrp.v2.PortletContext;
import org.oasis.wsrp.v2.RuntimeContext;
import org.oasis.wsrp.v2.SessionContext;
import org.oasis.wsrp.v2.UserContext;

import javax.xml.ws.Holder;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 12082 $
 * @since 2.4 (May 31, 2006)
 */
public class RenderHandler extends MimeResponseHandler<MarkupResponse, MarkupContext>
{

   public RenderHandler(WSRPConsumerImpl consumer)
   {
      super(consumer);
   }

   @Override
   protected SessionContext getSessionContextFrom(MarkupResponse response)
   {
      return response.getSessionContext();
   }

   @Override
   protected MarkupContext getMimeResponseFrom(MarkupResponse markupResponse)
   {
      return markupResponse.getMarkupContext();
   }

   @Override
   protected PortletInvocationResponse createContentResponse(MarkupContext markupContext, PortletInvocation invocation,
                                                             ResponseProperties properties, Map<String, Object> attributes,
                                                             String mimeType, byte[] bytes, String markup,
                                                             org.gatein.pc.api.cache.CacheControl cacheControl)
   {
      return new FragmentResponse(properties, attributes, mimeType, bytes, markup, markupContext.getPreferredTitle(),
         cacheControl, invocation.getPortalContext().getModes());
   }

   protected Object prepareRequest(RequestPrecursor requestPrecursor, PortletInvocation invocation)
   {
      if (!(invocation instanceof RenderInvocation))
      {
         throw new IllegalArgumentException("RenderHandler can only handle RenderInvocations!");
      }

      // Create the markup request
      PortletContext portletContext = requestPrecursor.getPortletContext();
      if (debug)
      {
         log.debug("Consumer about to attempt rendering portlet '" + portletContext.getPortletHandle() + "'");
      }
      return WSRPTypeFactory.createMarkupRequest(portletContext, requestPrecursor.getRuntimeContext(), requestPrecursor.getMarkupParams());
   }

   protected void updateUserContext(Object request, UserContext userContext)
   {
      getRenderRequest(request).setUserContext(userContext);
   }

   protected void updateRegistrationContext(Object request) throws PortletInvokerException
   {
      getRenderRequest(request).setRegistrationContext(consumer.getRegistrationContext());
   }

   protected RuntimeContext getRuntimeContextFrom(Object request)
   {
      return getRenderRequest(request).getRuntimeContext();
   }

   protected Object performRequest(Object request) throws Exception
   {
      GetMarkup renderRequest = getRenderRequest(request);
      if (debug)
      {
         log.debug("getMarkup on '" + renderRequest.getPortletContext().getPortletHandle() + "'");
      }

      // invocation
      Holder<SessionContext> sessionContextHolder = new Holder<SessionContext>();
      Holder<MarkupContext> markupContextHolder = new Holder<MarkupContext>();
      consumer.getMarkupService().getMarkup(renderRequest.getRegistrationContext(), renderRequest.getPortletContext(),
         renderRequest.getRuntimeContext(), renderRequest.getUserContext(), renderRequest.getMarkupParams(),
         markupContextHolder, sessionContextHolder, new Holder<List<Extension>>());
      MarkupResponse markupResponse = new MarkupResponse();
      markupResponse.setMarkupContext(markupContextHolder.value);
      markupResponse.setSessionContext(sessionContextHolder.value);
      return markupResponse;
   }

   private GetMarkup getRenderRequest(Object request)
   {
      if (request instanceof GetMarkup)
      {
         return (GetMarkup)request;
      }

      throw new IllegalArgumentException("RenderHandler: Request is not a GetMarkup request!");
   }

}
