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

import org.gatein.common.text.TextTools;
import org.gatein.pc.api.PortletInvokerException;
import org.gatein.pc.api.URLFormat;
import org.gatein.pc.api.cache.CacheScope;
import org.gatein.pc.api.invocation.PortletInvocation;
import org.gatein.pc.api.invocation.RenderInvocation;
import org.gatein.pc.api.invocation.response.ErrorResponse;
import org.gatein.pc.api.invocation.response.FragmentResponse;
import org.gatein.pc.api.invocation.response.PortletInvocationResponse;
import org.gatein.pc.api.spi.PortletInvocationContext;
import org.gatein.wsrp.WSRPConstants;
import org.gatein.wsrp.WSRPConsumer;
import org.gatein.wsrp.WSRPRewritingConstants;
import org.gatein.wsrp.WSRPTypeFactory;
import org.oasis.wsrp.v2.CacheControl;
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

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 12082 $
 * @since 2.4 (May 31, 2006)
 */
public class RenderHandler extends InvocationHandler
{

   private static final org.gatein.pc.api.cache.CacheControl DEFAULT_CACHE_CONTROL = new org.gatein.pc.api.cache.CacheControl(0, CacheScope.PRIVATE, null);

   protected RenderHandler(WSRPConsumerImpl consumer)
   {
      super(consumer);
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
      return WSRPTypeFactory.createMarkupRequest(portletContext, requestPrecursor.runtimeContext, requestPrecursor.markupParams);
   }

   protected PortletInvocationResponse processResponse(Object response, PortletInvocation invocation, RequestPrecursor requestPrecursor)
   {
      MarkupResponse markupResponse = (MarkupResponse)response;

      // process the response
      consumer.getSessionHandler().updateSessionIfNeeded(markupResponse.getSessionContext(), invocation,
         requestPrecursor.getPortletHandle());

      MarkupContext markupContext = markupResponse.getMarkupContext();
      String markup = markupContext.getItemString();
      byte[] binary = markupContext.getItemBinary();
      if (markup != null && binary != null)
      {
         return new ErrorResponse(new IllegalArgumentException("Markup response cannot contain both string and binary " +
            "markup. Per Section 6.1.10 of the WSRP specification, this is a Producer error."));
      }

      if (markup == null && binary == null)
      {
         if (markupContext.isUseCachedItem())
         {
            //todo: deal with cache GTNWSRP-40
         }
         else
         {
            return new ErrorResponse(new IllegalArgumentException("Markup response must contain at least string or binary" +
               " markup. Per Section 6.1.10 of the WSRP specification, this is a Producer error."));
         }
      }

      if (markup != null && markup.length() > 0)
      {
         if (Boolean.TRUE.equals(markupContext.isRequiresRewriting()))
         {
            markup = processMarkup(
               markup,
               getNamespaceFrom(invocation.getWindowContext()),
               invocation.getContext(),
               invocation.getTarget(),
               new URLFormat(invocation.getSecurityContext().isSecure(), invocation.getSecurityContext().isAuthenticated(), true, true),
               consumer
            );
         }
      }
      else
      {
         // todo: need to deal with binary
      }

      String mimeType = markupContext.getMimeType();
      if (mimeType == null || mimeType.length() == 0)
      {
         return new ErrorResponse(new IllegalArgumentException("No MIME type was provided for portlet content."));
      }

      // generate appropriate CacheControl
      org.gatein.pc.api.cache.CacheControl cacheControl = createCacheControl(markupContext);

      return new FragmentResponse(null, null, mimeType, null, markup,
         markupContext.getPreferredTitle(), cacheControl, invocation.getPortalContext().getModes());
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

   static String processMarkup(String markup, String namespace, PortletInvocationContext context, org.gatein.pc.api.PortletContext target, URLFormat format, WSRPConsumer consumer)
   {
      // fix-me: how to deal with fragment header? => interceptor?

      // todo: remove, this is a work-around for GTNWSRP-12
      markup = markup.replaceFirst("%3ftimeout%3d.*%2f", "%2f");

      markup = TextTools.replaceBoundedString(
         markup,
         WSRPRewritingConstants.WSRP_REWRITE,
         WSRPRewritingConstants.END_WSRP_REWRITE,
         new MarkupProcessor(namespace, context, target, format, consumer.getProducerInfo()),
         true,
         false,
         true
      );

      return markup;
   }

   private org.gatein.pc.api.cache.CacheControl createCacheControl(MarkupContext markupContext)
   {
      CacheControl cacheControl = markupContext.getCacheControl();
      org.gatein.pc.api.cache.CacheControl result = DEFAULT_CACHE_CONTROL;

      int expires;
      if (cacheControl != null)
      {
         expires = cacheControl.getExpires();
         String userScope = cacheControl.getUserScope();

         // check that we support the user scope...
         if (consumer.supportsUserScope(userScope))
         {
            if (debug)
            {
               log.debug("RenderHandler.processRenderRequest: trying to cache markup " + userScope + " for " + expires + " seconds.");
            }
            CacheScope scope;
            if (WSRPConstants.CACHE_FOR_ALL.equals(userScope))
            {
               scope = CacheScope.PUBLIC;
            }
            else if (WSRPConstants.CACHE_PER_USER.equals(userScope))
            {
               scope = CacheScope.PRIVATE;
            }
            else
            {
               throw new IllegalArgumentException("Unknown CacheControl user scope: " + userScope); // should not happen
            }

            result = new org.gatein.pc.api.cache.CacheControl(expires, scope, cacheControl.getValidateTag());
         }
      }

      return result;
   }
}
