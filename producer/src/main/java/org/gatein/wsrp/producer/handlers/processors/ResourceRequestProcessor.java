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

import org.gatein.pc.api.cache.CacheLevel;
import org.gatein.pc.api.invocation.PortletInvocation;
import org.gatein.pc.api.invocation.ResourceInvocation;
import org.gatein.pc.api.state.AccessMode;
import org.gatein.wsrp.WSRPResourceURL;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.WSRPUtils;
import org.gatein.wsrp.producer.handlers.MarkupHandler;
import org.oasis.wsrp.v2.Extension;
import org.oasis.wsrp.v2.GetResource;
import org.oasis.wsrp.v2.InvalidHandle;
import org.oasis.wsrp.v2.InvalidRegistration;
import org.oasis.wsrp.v2.MimeRequest;
import org.oasis.wsrp.v2.MissingParameters;
import org.oasis.wsrp.v2.ModifyRegistrationRequired;
import org.oasis.wsrp.v2.OperationFailed;
import org.oasis.wsrp.v2.PortletContext;
import org.oasis.wsrp.v2.RegistrationContext;
import org.oasis.wsrp.v2.ResourceContext;
import org.oasis.wsrp.v2.ResourceParams;
import org.oasis.wsrp.v2.ResourceResponse;
import org.oasis.wsrp.v2.RuntimeContext;
import org.oasis.wsrp.v2.UnsupportedLocale;
import org.oasis.wsrp.v2.UnsupportedMimeType;
import org.oasis.wsrp.v2.UnsupportedMode;
import org.oasis.wsrp.v2.UnsupportedWindowState;
import org.oasis.wsrp.v2.UserContext;

import java.util.List;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
class ResourceRequestProcessor extends MimeResponseProcessor<ResourceContext, ResourceResponse>
{
   private final GetResource getResource;

   public ResourceRequestProcessor(ProducerHelper producer, GetResource getResource) throws InvalidRegistration, OperationFailed, MissingParameters, InvalidHandle, UnsupportedMimeType, UnsupportedWindowState, UnsupportedMode, ModifyRegistrationRequired, UnsupportedLocale
   {
      super(producer);
      this.getResource = getResource;
      prepareInvocation();
   }

   public PortletContext getPortletContext()
   {
      return getResource.getPortletContext();
   }

   @Override
   AccessMode getAccessMode() throws MissingParameters
   {
      return AccessMode.READ_ONLY;
   }

   @Override
   List<Extension> getResponseExtensionsFor(ResourceResponse resourceResponse)
   {
      return resourceResponse.getExtensions();
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

   protected PortletInvocation internalInitInvocation(WSRPPortletInvocationContext context)
   {
      ResourceInvocation resourceInvocation = new ResourceInvocation(context);

      ResourceParams resourceParams = this.getResource.getResourceParams();

      // only set the resource id if it's different from the place holder we use if the portlet doesn't set one
      String id = this.getResource.getResourceParams().getResourceID();
      if (!WSRPResourceURL.DEFAULT_RESOURCE_ID.equals(id))
      {
         resourceInvocation.setResourceId(id);
      }

      WSRPRequestContext requestContext = WSRPRequestContext.createRequestContext(markupRequest, resourceParams);
      resourceInvocation.setRequestContext(requestContext);
      resourceInvocation.setForm(requestContext.getForm());

      //TODO: property set validation token for caching (ie ETAG)
      String validationToken = null;
      resourceInvocation.setValidationToken(validationToken);

      resourceInvocation.setResourceState(createNavigationalState(resourceParams.getResourceState()));

      String resourceCacheability = resourceParams.getResourceCacheability();
      if (resourceCacheability != null)
      {
         CacheLevel cacheLevel = WSRPUtils.getCacheLevelFromResourceCacheability(resourceParams.getResourceCacheability());
         resourceInvocation.setCacheLevel(cacheLevel);
      }
      else
      {
         // according to JSR 286, cache level must default to ResourceURL.PAGE
         resourceInvocation.setCacheLevel(CacheLevel.PAGE);
      }

      return resourceInvocation;
   }

   @Override
   protected ResourceResponse createResponse(ResourceContext resourceContext)
   {
      return WSRPTypeFactory.createResourceResponse(resourceContext);
   }

   @Override
   protected Class<ResourceContext> getReifiedClass()
   {
      return ResourceContext.class;
   }
}

