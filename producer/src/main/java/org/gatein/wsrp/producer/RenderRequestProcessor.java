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

package org.gatein.wsrp.producer;

import org.gatein.common.net.URLTools;
import org.gatein.pc.api.invocation.PortletInvocation;
import org.gatein.pc.api.invocation.RenderInvocation;
import org.gatein.pc.api.invocation.response.FragmentResponse;
import org.gatein.pc.api.invocation.response.PortletInvocationResponse;
import org.gatein.pc.api.state.AccessMode;
import org.gatein.pc.portlet.impl.jsr168.PortletUtils;
import org.gatein.wsrp.WSRPConstants;
import org.gatein.wsrp.WSRPRewritingConstants;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.WSRPUtils;
import org.gatein.wsrp.servlet.ServletAccess;
import org.oasis.wsrp.v2.GetMarkup;
import org.oasis.wsrp.v2.InvalidHandle;
import org.oasis.wsrp.v2.InvalidRegistration;
import org.oasis.wsrp.v2.MarkupContext;
import org.oasis.wsrp.v2.MarkupParams;
import org.oasis.wsrp.v2.MissingParameters;
import org.oasis.wsrp.v2.OperationFailed;
import org.oasis.wsrp.v2.PortletContext;
import org.oasis.wsrp.v2.RegistrationContext;
import org.oasis.wsrp.v2.RuntimeContext;
import org.oasis.wsrp.v2.UnsupportedMimeType;
import org.oasis.wsrp.v2.UnsupportedMode;
import org.oasis.wsrp.v2.UnsupportedWindowState;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 13121 $
 * @since 2.6
 */
public class RenderRequestProcessor extends RequestProcessor
{
   private static final String EMPTY = "";
   private final GetMarkup getMarkup;
   private String namespace;

   public RenderRequestProcessor(WSRPProducerImpl producer, GetMarkup getMarkup) throws UnsupportedMimeType,
      UnsupportedWindowState, InvalidHandle, UnsupportedMode, MissingParameters, InvalidRegistration, OperationFailed
   {
      super(producer);
      this.getMarkup = getMarkup;
      prepareInvocation();
   }

   RegistrationContext getRegistrationContext()
   {
      return getMarkup.getRegistrationContext();
   }

   RuntimeContext getRuntimeContext()
   {
      return getMarkup.getRuntimeContext();
   }

   MarkupParams getMarkupParams()
   {
      return getMarkup.getMarkupParams();
   }

   PortletContext getPortletContext()
   {
      return getMarkup.getPortletContext();
   }

   org.oasis.wsrp.v2.UserContext getUserContext()
   {
      return getMarkup.getUserContext();
   }

   String getContextName()
   {
      return MarkupHandler.GET_MARKUP;
   }

   AccessMode getAccessMode()
   {
      return AccessMode.READ_ONLY;
   }

   PortletInvocation initInvocation(WSRPPortletInvocationContext context)
   {
      // MUST match namespace generation used in PortletResponseImpl.getNamespace in portlet module...
      namespace = PortletUtils.generateNamespaceFrom(context.getWindowContext().getId());

      return new RenderInvocation(context);
   }

   Object processResponse(PortletInvocationResponse response)
   {
      FragmentResponse fragment = (FragmentResponse)response;
      String markupString = null;
      byte[] markupBinary = null;
      switch (fragment.getType())
      {
         case FragmentResponse.TYPE_CHARS:
            markupString = processFragmentString(fragment.getChars());
            break;
         case FragmentResponse.TYPE_BYTES:
            markupBinary = fragment.getBytes(); // fix-me: might need to convert to Base64?
            break;
         case FragmentResponse.TYPE_EMPTY:
            markupString = EMPTY;
            break;
      }


      MarkupContext markupContext;
      if (markupString != null)
      {
         markupContext = WSRPTypeFactory.createMarkupContext(markupRequest.getMediaType(), markupString);
      }
      else
      {
         markupContext = WSRPTypeFactory.createMarkupContext(markupRequest.getMediaTypeWithCharset(), markupBinary);
      }
      markupContext.setLocale(markupRequest.getLocale());
      markupContext.setRequiresRewriting(Boolean.TRUE);
      markupContext.setPreferredTitle(portletDescription.getTitle().getValue());

      // cache information
      int expires = fragment.getCacheControl().getExpirationSecs();
      // only create a CacheControl if expiration time is not 0
      if (expires != 0)
      {
         // if expires is negative, replace by -1 to make sure
         if (expires < 0)
         {
            expires = -1;
         }

         markupContext.setCacheControl(WSRPTypeFactory.createCacheControl(expires, WSRPConstants.CACHE_PER_USER));
      }

      return WSRPTypeFactory.createMarkupResponse(markupContext);
   }

   /**
    * Process String returned from RenderResult to add rewriting token if necessary, replacing namespaces by the WSRP
    * rewrite token. fix-me: need to check for producer rewriting
    *
    * @param renderString the String to be processed for rewriting marking
    * @return a String processed to add rewriting tokens as necessary
    */
   private String processFragmentString(String renderString)
   {
      String result = renderString.replaceAll(namespace, WSRPRewritingConstants.WSRP_REWRITE_TOKEN);

      result = URLTools.replaceURLsBy(result, new WSRPUtils.AbsoluteURLReplacementGenerator(ServletAccess.getRequest()));
      return result;
   }
}
