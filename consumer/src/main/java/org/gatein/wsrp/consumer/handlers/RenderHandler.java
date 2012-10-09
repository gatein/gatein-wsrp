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

import org.gatein.common.util.ParameterValidation;
import org.gatein.pc.api.invocation.RenderInvocation;
import org.gatein.pc.api.invocation.response.FragmentResponse;
import org.gatein.pc.api.invocation.response.PortletInvocationResponse;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.consumer.spi.WSRPConsumerSPI;
import org.oasis.wsrp.v2.Extension;
import org.oasis.wsrp.v2.GetMarkup;
import org.oasis.wsrp.v2.MarkupContext;
import org.oasis.wsrp.v2.MarkupResponse;
import org.oasis.wsrp.v2.PortletContext;
import org.oasis.wsrp.v2.RuntimeContext;
import org.oasis.wsrp.v2.SessionContext;

import javax.xml.ws.Holder;
import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 12082 $
 * @since 2.4 (May 31, 2006)
 */
public class RenderHandler extends MimeResponseHandler<RenderInvocation, GetMarkup, MarkupResponse, MarkupContext>
{

   public RenderHandler(WSRPConsumerSPI consumer)
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
   protected PortletInvocationResponse createContentResponse(MarkupContext markupContext, RenderInvocation invocation,
                                                             String mimeType, byte[] bytes, String markup,
                                                             org.gatein.pc.api.cache.CacheControl cacheControl)
   {

      // encoding should be ignored here TODO: check that this is true
      if (markup != null)
      {
         return new FragmentResponse(null, null, mimeType, null, markup, markupContext.getPreferredTitle(), cacheControl, invocation.getPortalContext().getModes());
      }
      else
      {
         return new FragmentResponse(null, null, mimeType, null, bytes, markupContext.getPreferredTitle(), cacheControl, invocation.getPortalContext().getModes());
      }
   }

   protected GetMarkup prepareRequest(RequestPrecursor<RenderInvocation> requestPrecursor, RenderInvocation invocation)
   {
      // Create the markup request
      PortletContext portletContext = requestPrecursor.getPortletContext();
      if (debug)
      {
         log.debug("Consumer about to attempt rendering portlet '" + portletContext.getPortletHandle() + "'");
      }
      return WSRPTypeFactory.createGetMarkup(requestPrecursor.getRegistrationContext(), portletContext,
         requestPrecursor.getRuntimeContext(), requestPrecursor.getUserContext(), requestPrecursor.getMarkupParams());
   }

   @Override
   protected List<Extension> getExtensionsFrom(MarkupResponse markupResponse)
   {
      return markupResponse.getExtensions();
   }

   /*protected void updateUserContext(GetMarkup request, UserContext userContext)
   {
      request.setUserContext(userContext);
   }

   protected void updateRegistrationContext(GetMarkup request) throws PortletInvokerException
   {
      request.setRegistrationContext(consumer.getRegistrationContext());
   }*/

   protected RuntimeContext getRuntimeContextFrom(GetMarkup request)
   {
      return request.getRuntimeContext();
   }

   protected MarkupResponse performRequest(GetMarkup request) throws Exception
   {
      if (debug)
      {
         log.debug("getMarkup on '" + request.getPortletContext().getPortletHandle() + "'");
      }

      // invocation
      Holder<SessionContext> sessionContextHolder = new Holder<SessionContext>();
      Holder<MarkupContext> markupContextHolder = new Holder<MarkupContext>();
      final Holder<List<Extension>> extensions = new Holder<List<Extension>>();
      consumer.getMarkupService().getMarkup(request.getRegistrationContext(), request.getPortletContext(),
         request.getRuntimeContext(), request.getUserContext(), request.getMarkupParams(),
         markupContextHolder, sessionContextHolder, extensions);

      MarkupResponse markupResponse = WSRPTypeFactory.createMarkupResponse(markupContextHolder.value);
      markupResponse.setSessionContext(sessionContextHolder.value);
      if (ParameterValidation.existsAndIsNotEmpty(extensions.value))
      {
         markupResponse.getExtensions().addAll(extensions.value);
      }
      return markupResponse;
   }
}
