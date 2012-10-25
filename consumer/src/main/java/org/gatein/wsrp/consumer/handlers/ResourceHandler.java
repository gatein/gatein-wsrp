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
import org.gatein.pc.api.StateString;
import org.gatein.pc.api.invocation.ResourceInvocation;
import org.gatein.pc.api.spi.InstanceContext;
import org.gatein.pc.api.state.AccessMode;
import org.gatein.wsrp.WSRPRewritingConstants;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.WSRPUtils;
import org.gatein.wsrp.api.extensions.ExtensionAccess;
import org.gatein.wsrp.consumer.spi.WSRPConsumerSPI;
import org.gatein.wsrp.spec.v2.WSRP2RewritingConstants;
import org.oasis.wsrp.v2.Extension;
import org.oasis.wsrp.v2.GetResource;
import org.oasis.wsrp.v2.MarkupParams;
import org.oasis.wsrp.v2.PortletContext;
import org.oasis.wsrp.v2.ResourceContext;
import org.oasis.wsrp.v2.ResourceParams;
import org.oasis.wsrp.v2.ResourceResponse;
import org.oasis.wsrp.v2.RuntimeContext;
import org.oasis.wsrp.v2.SessionContext;

import javax.xml.ws.Holder;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class ResourceHandler extends MimeResponseHandler<ResourceInvocation, GetResource, ResourceResponse, ResourceContext>
{

   public ResourceHandler(WSRPConsumerSPI consumer)
   {
      super(consumer);
   }

   @Override
   protected SessionContext getSessionContextFrom(ResourceResponse resourceResponse)
   {
      return resourceResponse.getSessionContext();
   }

   @Override
   protected ResourceContext getMimeResponseFrom(ResourceResponse resourceResponse)
   {
      return resourceResponse.getResourceContext();
   }

   /*@Override
   protected void updateUserContext(GetResource request, UserContext userContext)
   {
      request.setUserContext(userContext);
   }

   @Override
   protected void updateRegistrationContext(GetResource request) throws PortletInvokerException
   {
      request.setRegistrationContext(consumer.getRegistrationContext());
   }*/

   @Override
   protected RuntimeContext getRuntimeContextFrom(GetResource request)
   {
      return request.getRuntimeContext();
   }

   @Override
   protected GetResource prepareRequest(RequestPrecursor<ResourceInvocation> requestPrecursor, ResourceInvocation invocation)
   {
      /*String resourceInvocationId = invocation.getResourceId();
      Map<String, String> resourceMap = WSRPResourceURL.decodeResource(resourceInvocationId);
      String resourceId = resourceMap.get(WSRP2RewritingConstants.RESOURCE_ID);*/

      // get the resource id from the invocation attributes so that we don't need to redecode the original encoded resource id
      String resourceId = (String)invocation.getAttribute(WSRP2RewritingConstants.RESOURCE_ID);

      // if we didn't get a resource id, that means we need to use a resource URL, so get it and use it as the resource id
      // note that the InvocationDispatcher should properly handle which handler will be used so this should be safe
      // if we had a resource id anyway but are using WSRP 1, we need to reset it to use the URL instead
      if (resourceId == null || !consumer.isUsingWSRP2())
      {
         resourceId = (String)invocation.getAttribute(WSRPRewritingConstants.RESOURCE_URL);
      }

      PortletContext portletContext = requestPrecursor.getPortletContext();

      // since we actually extracted the data into MarkupParams in the RequestPrecursor, use that! :)
      MarkupParams params = requestPrecursor.getMarkupParams();

      // access mode
      InstanceContext instanceContext = invocation.getInstanceContext();
      ParameterValidation.throwIllegalArgExceptionIfNull(instanceContext, "instance context");
      AccessMode accessMode = instanceContext.getAccessMode();
      ParameterValidation.throwIllegalArgExceptionIfNull(accessMode, "access mode");
      if (debug)
      {
         log.debug("Portlet is requesting " + accessMode + " access mode");
      }

      // if the portlet didn't request a resource id, use the fake one:
      if (ParameterValidation.isNullOrEmpty(resourceId))
      {
         throw new IllegalArgumentException("GetResource needs a valid resource id.");
      }

      // Create ResourceParams
      ResourceParams resourceParams = WSRPTypeFactory.createResourceParams(params.isSecureClientCommunication(),
         params.getLocales(), params.getMimeTypes(), params.getMode(), params.getWindowState(), resourceId,
         WSRPUtils.getStateChangeFromAccessMode(accessMode));

      resourceParams.setNavigationalContext(params.getNavigationalContext());
      resourceParams.setClientData(params.getClientData());
      resourceParams.setResourceCacheability(WSRPUtils.getResourceCacheabilityFromCacheLevel(invocation.getCacheLevel()));
      resourceParams.getExtensions().addAll(ExtensionAccess.getConsumerExtensionAccessor().getRequestExtensionsFor(ResourceParams.class));

      for (Map.Entry<String, String[]> entry : invocation.getForm().entrySet())
      {
         String name = entry.getKey();
         for (String value : entry.getValue())
         {
            resourceParams.getFormParameters().add(WSRPTypeFactory.createNamedString(name, value));
         }
      }

      StateString resourceState = invocation.getResourceState();
      if (resourceState != null)
      {
         String state = resourceState.getStringValue();
         if (!StateString.JBPNS_PREFIX.equals(state))  // fix-me: see JBPORTAL-900
         {
            resourceParams.setResourceState(state);
         }
      }

      return WSRPTypeFactory.createGetResource(requestPrecursor.getRegistrationContext(), portletContext,
         requestPrecursor.getRuntimeContext(), requestPrecursor.getUserContext(), resourceParams);
   }

   @Override
   protected List<Extension> getExtensionsFrom(ResourceResponse resourceResponse)
   {
      return resourceResponse.getExtensions();
   }

   @Override
   protected ResourceResponse performRequest(GetResource request) throws Exception
   {
      Holder<SessionContext> sessionContextHolder = new Holder<SessionContext>();
      Holder<ResourceContext> resourceContextHolder = new Holder<ResourceContext>();
      Holder<PortletContext> portletContextHolder = new Holder<PortletContext>(request.getPortletContext());

      final Holder<List<Extension>> extensions = new Holder<List<Extension>>();
      consumer.getMarkupService().getResource(request.getRegistrationContext(), portletContextHolder, request.getRuntimeContext(),
         request.getUserContext(), request.getResourceParams(), resourceContextHolder, sessionContextHolder, extensions);

      ResourceResponse resourceResponse = WSRPTypeFactory.createResourceResponse(resourceContextHolder.value);
      resourceResponse.setPortletContext(portletContextHolder.value);
      resourceResponse.setSessionContext(sessionContextHolder.value);
      if (ParameterValidation.existsAndIsNotEmpty(extensions.value) && WSRPUtils.isSingletonListWithNullOrEmptyElement(extensions.value))
      {
         resourceResponse.getExtensions().addAll(extensions.value);
      }
      return resourceResponse;
   }

}
