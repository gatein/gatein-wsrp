/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2010, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/
package org.gatein.wsrp.producer;

import org.gatein.common.NotYetImplemented;
import org.gatein.common.net.URLTools;
import org.gatein.pc.api.Portlet;
import org.gatein.pc.api.cache.CacheLevel;
import org.gatein.pc.api.invocation.PortletInvocation;
import org.gatein.pc.api.invocation.ResourceInvocation;
import org.gatein.pc.api.invocation.response.ContentResponse;
import org.gatein.pc.api.invocation.response.FragmentResponse;
import org.gatein.pc.api.invocation.response.PortletInvocationResponse;
import org.gatein.pc.api.state.AccessMode;
import org.gatein.pc.portlet.impl.jsr168.PortletUtils;
import org.gatein.wsrp.WSRPConstants;
import org.gatein.wsrp.WSRPRewritingConstants;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.WSRPUtils;
import org.gatein.wsrp.servlet.ServletAccess;
import org.oasis.wsrp.v2.CacheControl;
import org.oasis.wsrp.v2.GetResource;
import org.oasis.wsrp.v2.InvalidHandle;
import org.oasis.wsrp.v2.InvalidRegistration;
import org.oasis.wsrp.v2.MarkupParams;
import org.oasis.wsrp.v2.MimeRequest;
import org.oasis.wsrp.v2.MissingParameters;
import org.oasis.wsrp.v2.OperationFailed;
import org.oasis.wsrp.v2.PortletContext;
import org.oasis.wsrp.v2.PortletDescription;
import org.oasis.wsrp.v2.RegistrationContext;
import org.oasis.wsrp.v2.ResourceContext;
import org.oasis.wsrp.v2.ResourceParams;
import org.oasis.wsrp.v2.ResourceResponse;
import org.oasis.wsrp.v2.RuntimeContext;
import org.oasis.wsrp.v2.UnsupportedMimeType;
import org.oasis.wsrp.v2.UnsupportedMode;
import org.oasis.wsrp.v2.UnsupportedWindowState;
import org.oasis.wsrp.v2.UserContext;

/**
 * 
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class ResourceRequestProcessor extends RequestProcessor
{

   private static final String EMPTY = "";
   
   private final GetResource getResource;
   private String namespace;
   
   public ResourceRequestProcessor(WSRPProducerImpl producer, GetResource getResource) throws InvalidRegistration, OperationFailed, MissingParameters, InvalidHandle, UnsupportedMimeType, UnsupportedWindowState, UnsupportedMode
   {
      super(producer);
      this.getResource = getResource;
      prepareInvocation();
   }

   public PortletInvocation getInvocation()
   {
      return invocation;
   }

   public PortletContext getPortletContext()
   {
      return getResource.getPortletContext();
   }

   public ResourceResponse processResponse(PortletInvocationResponse response)
   {
      ContentResponse content = (ContentResponse)response;
      String resourceString = null;
      byte[] resourceBinary = null;
      switch (content.getType())
      {
         case ContentResponse.TYPE_CHARS:
            resourceString = processFragmentString(content.getChars());
            break;
         case ContentResponse.TYPE_BYTES:
            resourceBinary = content.getBytes(); // fix-me: might need to convert to Base64?
            break;
         case ContentResponse.TYPE_EMPTY:
            resourceString = EMPTY;
            break;
      }

      ResourceContext resourceContext;
      if (resourceString != null)
      {
         resourceContext = WSRPTypeFactory.createResourceContext(content.getContentType(), resourceString);
      }
      else
      {
         resourceContext = WSRPTypeFactory.createResourceContext(content.getContentType(), resourceBinary);
      }

      resourceContext.setLocale(markupRequest.getLocale());

      //TODO: figure out requiresRewriting and useCachedItem
      Boolean requiresRewriting = false;
      Boolean useCachedItem = false;
      resourceContext.setRequiresRewriting(requiresRewriting);
      resourceContext.setUseCachedItem(useCachedItem);
      
      //TODO: check if anything actually uses the ccpp profile warning
      String ccppProfileWarning = null;
      resourceContext.setCcppProfileWarning(ccppProfileWarning);
      
      // cache information
      int expires = content.getCacheControl().getExpirationSecs();
      // only create a CacheControl if expiration time is not 0
      if (expires != 0)
      {
         // if expires is negative, replace by -1 to make sure
         if (expires < 0)
         {
            expires = -1;
         }

         resourceContext.setCacheControl(WSRPTypeFactory.createCacheControl(expires, WSRPConstants.CACHE_PER_USER));
      }
      
      return WSRPTypeFactory.createResourceResponse(resourceContext);
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

   @Override
   AccessMode getAccessMode() throws MissingParameters
   {
      return AccessMode.READ_ONLY;
   }

   @Override
   String getContextName()
   {
      return MarkupHandler.GET_RESOURCE;
   }

   @Override
   MimeRequest getParams()
   {
      return getResource.getResourceParams();
   }
   
   @Override
   RegistrationContext getRegistrationContext()
   {
      return getResource.getRegistrationContext();
   }

   @Override
   RuntimeContext getRuntimeContext()
   {
      return getResource.getRuntimeContext();
   }

   @Override
   UserContext getUserContext()
   {
      return getResource.getUserContext();
   }

   @Override
   PortletInvocation initInvocation(WSRPPortletInvocationContext context)
   {
      // MUST match namespace generation used in PortletResponseImpl.getNamespace in portlet module...
      namespace = PortletUtils.generateNamespaceFrom(context.getWindowContext().getId());
      ResourceInvocation resourceInvocation = new ResourceInvocation(context);
  
      ResourceParams resourceParams = this.getResource.getResourceParams();
            
      resourceInvocation.setResourceId(this.getResource.getResourceParams().getResourceID());
      
      WSRPRequestContext requestContext = WSRPRequestContext.createRequestContext(markupRequest, resourceParams);
      resourceInvocation.setRequestContext(requestContext);
      resourceInvocation.setForm(requestContext.getForm());
      
      //TODO: property set validation token for caching (ie ETAG)
      String validationToken = null;
      resourceInvocation.setValidationToken(validationToken);
      
      resourceInvocation.setResourceState(createNavigationalState(resourceParams.getResourceState()));      
      
      if (resourceParams.getResourceCacheability() != null)
      {
         CacheLevel cacheLevel = CacheLevel.valueOf(resourceParams.getResourceCacheability());
         resourceInvocation.setCacheLevel(cacheLevel);
      }
      
      return resourceInvocation;
   }
}

