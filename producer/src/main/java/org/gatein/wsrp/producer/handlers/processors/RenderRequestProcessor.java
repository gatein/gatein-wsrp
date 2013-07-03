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

import org.gatein.pc.api.invocation.PortletInvocation;
import org.gatein.pc.api.invocation.RenderInvocation;
import org.gatein.pc.api.invocation.response.PortletInvocationResponse;
import org.gatein.pc.api.state.AccessMode;
import org.gatein.wsrp.WSRPTypeFactory;
import org.oasis.wsrp.v2.Extension;
import org.oasis.wsrp.v2.GetMarkup;
import org.oasis.wsrp.v2.InvalidHandle;
import org.oasis.wsrp.v2.InvalidRegistration;
import org.oasis.wsrp.v2.MarkupContext;
import org.oasis.wsrp.v2.MarkupResponse;
import org.oasis.wsrp.v2.MimeRequest;
import org.oasis.wsrp.v2.MissingParameters;
import org.oasis.wsrp.v2.ModifyRegistrationRequired;
import org.oasis.wsrp.v2.OperationFailed;
import org.oasis.wsrp.v2.OperationNotSupported;
import org.oasis.wsrp.v2.PortletContext;
import org.oasis.wsrp.v2.RegistrationContext;
import org.oasis.wsrp.v2.RuntimeContext;
import org.oasis.wsrp.v2.UnsupportedLocale;
import org.oasis.wsrp.v2.UnsupportedMimeType;
import org.oasis.wsrp.v2.UnsupportedMode;
import org.oasis.wsrp.v2.UnsupportedWindowState;

import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 13121 $
 * @since 2.6
 */
class RenderRequestProcessor extends MimeResponseProcessor<GetMarkup, MarkupContext, MarkupResponse>
{
   public RenderRequestProcessor(ProducerHelper producer, GetMarkup getMarkup) throws UnsupportedMimeType, UnsupportedWindowState, InvalidHandle, UnsupportedMode,
      MissingParameters, InvalidRegistration, OperationFailed, ModifyRegistrationRequired, UnsupportedLocale, OperationNotSupported
   {
      super(producer, getMarkup);
   }

   public RegistrationContext getRegistrationContext()
   {
      return request.getRegistrationContext();
   }

   RuntimeContext getRuntimeContext()
   {
      return request.getRuntimeContext();
   }

   MimeRequest getParams()
   {
      return request.getMarkupParams();
   }

   public PortletContext getPortletContext()
   {
      return request.getPortletContext();
   }

   org.oasis.wsrp.v2.UserContext getUserContext()
   {
      return request.getUserContext();
   }

   AccessMode getAccessMode()
   {
      return AccessMode.READ_ONLY;
   }

   @Override
   List<Extension> getResponseExtensionsFor(MarkupResponse markupResponse)
   {
      return markupResponse.getExtensions();
   }

   protected PortletInvocation internalInitInvocation(WSRPPortletInvocationContext context)
   {
      final RenderInvocation renderInvocation = new RenderInvocation(context);
      renderInvocation.setEncoding(markupRequest.getCharacterSet());
      return renderInvocation;
   }

   @Override
   protected MarkupResponse createResponse(MarkupContext mimeResponse)
   {
      return WSRPTypeFactory.createMarkupResponse(mimeResponse);
   }

   @Override
   protected Class<MarkupContext> getReifiedClass()
   {
      return MarkupContext.class;
   }

   @Override
   protected void additionallyProcessIfNeeded(MarkupContext markupContext, PortletInvocationResponse response)
   {
      markupContext.setPreferredTitle(portletDescription.getTitle().getValue());
   }
}
