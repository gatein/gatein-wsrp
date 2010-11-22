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
import org.gatein.pc.api.PortletInvokerException;
import org.gatein.pc.api.invocation.ActionInvocation;
import org.gatein.pc.api.invocation.EventInvocation;
import org.gatein.pc.api.invocation.InvocationException;
import org.gatein.pc.api.invocation.PortletInvocation;
import org.gatein.pc.api.invocation.RenderInvocation;
import org.gatein.pc.api.invocation.ResourceInvocation;
import org.gatein.pc.api.invocation.response.ErrorResponse;
import org.gatein.pc.api.invocation.response.PortletInvocationResponse;
import org.gatein.wsrp.WSRPResourceURL;
import org.gatein.wsrp.WSRPRewritingConstants;
import org.gatein.wsrp.consumer.WSRPConsumerImpl;
import org.gatein.wsrp.spec.v2.WSRP2RewritingConstants;

import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class InvocationDispatcher
{
   private final ActionHandler actionHandler;
   private final RenderHandler renderHandler;
   private final ResourceHandler resourceHandler;
   private final EventHandler eventHandler;
   private final WSRPConsumerImpl consumer;
   private final DirectResourceServingHandler directResourceHandler;

   public InvocationDispatcher(WSRPConsumerImpl consumer)
   {
      this.consumer = consumer;
      actionHandler = new ActionHandler(consumer);
      renderHandler = new RenderHandler(consumer);
      resourceHandler = new ResourceHandler(consumer);
      eventHandler = new EventHandler(consumer);
      directResourceHandler = new DirectResourceServingHandler(consumer);
   }

   public PortletInvocationResponse dispatchAndHandle(PortletInvocation invocation) throws PortletInvokerException
   {
      InvocationHandler handler;

      if (invocation instanceof RenderInvocation)
      {
         handler = renderHandler;
      }
      else if (invocation instanceof ActionInvocation)
      {
         handler = actionHandler;
      }
      else if (invocation instanceof ResourceInvocation)
      {
         ResourceInvocation resourceInvocation = (ResourceInvocation)invocation;
         String resourceInvocationId = resourceInvocation.getResourceId();
         String resourceId;
         String resourceURL;
         String preferOperationAsString;

         if (!ParameterValidation.isNullOrEmpty(resourceInvocationId))
         {
            Map<String, String> resourceMap = WSRPResourceURL.decodeResource(resourceInvocationId);
            resourceId = resourceMap.get(WSRP2RewritingConstants.RESOURCE_ID);
            resourceURL = resourceMap.get(WSRPRewritingConstants.RESOURCE_URL);
            preferOperationAsString = resourceMap.get(WSRP2RewritingConstants.RESOURCE_PREFER_OPERATION);

            // put the values in the invocation so that we don't need to redecode the resource id
            resourceInvocation.setAttribute(WSRPRewritingConstants.RESOURCE_URL, resourceURL);
            resourceInvocation.setAttribute(WSRP2RewritingConstants.RESOURCE_ID, resourceId);
            resourceInvocation.setAttribute(WSRP2RewritingConstants.RESOURCE_PREFER_OPERATION, preferOperationAsString);
         }
         else
         {
            // GateIn-specific: WSRP-specific URL parameters might also be put as attributes by UIPortlet when the invocation is created
            resourceId = null;
            resourceURL = (String)resourceInvocation.getAttribute(WSRPRewritingConstants.RESOURCE_URL);
            preferOperationAsString = (String)resourceInvocation.getAttribute(WSRP2RewritingConstants.RESOURCE_PREFER_OPERATION);
         }

         boolean preferOperation = (preferOperationAsString != null && Boolean.parseBoolean(preferOperationAsString));

         if (consumer.isUsingWSRP2() && (preferOperation || resourceURL == null || (resourceId != null && resourceId.length() > 0)))
         {
            handler = resourceHandler;
         }
         else if (resourceURL != null)
         {
            handler = directResourceHandler;
         }
         else
         {
            if (consumer.isUsingWSRP2())
            {
               return new ErrorResponse("Did not get a resource URL or a resource ID, cannot fetch resource.");
            }
            else //using WSRP1
            {
               return new ErrorResponse("Did not get a resource URL, cannot fetch resource.");
            }
         }
      }
      else if (invocation instanceof EventInvocation)
      {
         handler = eventHandler;
      }
      else
      {
         throw new InvocationException("Unknown invocation type: " + invocation);
      }

      return handler.handle(invocation);
   }
}
