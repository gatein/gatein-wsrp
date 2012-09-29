/*
* JBoss, a division of Red Hat
* Copyright 2012, Red Hat Middleware, LLC, and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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

package org.gatein.wsrp.examples;

import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.pc.api.invocation.PortletInvocation;
import org.gatein.pc.api.invocation.RenderInvocation;
import org.gatein.pc.api.invocation.response.ContentResponse;
import org.gatein.pc.api.invocation.response.PortletInvocationResponse;
import org.gatein.wsrp.api.extensions.ConsumerExtensionAccessor;
import org.gatein.wsrp.api.extensions.ExtensionAccess;
import org.gatein.wsrp.api.extensions.InvocationHandlerDelegate;
import org.gatein.wsrp.api.extensions.UnmarshalledExtension;
import org.oasis.wsrp.v2.MarkupParams;
import org.oasis.wsrp.v2.MarkupResponse;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.List;

/**
 * An example of how an InvocationHandlerDelegate can add information extracted from the consumer to pass along to the producer, working in conjunction with an associated
 * producer-side InvocationHandlerDelegate to establish a round-trip communication channel outside of the standard WSRP protocol.
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 */
public class ExampleConsumerInvocationHandlerDelegate extends InvocationHandlerDelegate
{
   private final static Logger log = LoggerFactory.getLogger(InvocationHandlerDelegate.class);

   @Override
   public void processInvocation(PortletInvocation invocation)
   {
      // only attach an extension if we have a render invocation
      if (invocation instanceof RenderInvocation)
      {
         // retrieve the session id from the portlet invocation
         final String id = invocation.getDispatchedRequest().getSession().getId();
         log.info("Session id: " + id);

         // Create an "OriginalSessionId" element containing the session id within a "MarkupParamsExtension" element extension
         final String namespaceURI = "urn:gatein:wsrp:ext:examples"; // please choose a more appropriate namespace for your own extensions
         final Element markupParamsExtension = DOMUtils.createElement(namespaceURI, "MarkupParamsExtension");
         final Node originalSessionId = DOMUtils.addChild(markupParamsExtension, namespaceURI, "OriginalSessionId");
         originalSessionId.setTextContent(id);

         // retrieve the ConsumerExtensionAccessor to be able to interact with extensions on the consumer-side
         final ConsumerExtensionAccessor consumerExtensionAccessor = ExtensionAccess.getConsumerExtensionAccessor();

         // attach the newly created extension to the MarkupParams element for this particular invocation
         consumerExtensionAccessor.addRequestExtension(MarkupParams.class, markupParamsExtension);
      }
   }

   @Override
   public void processInvocationResponse(PortletInvocationResponse response, PortletInvocation invocation)
   {
      if (response instanceof ContentResponse)
      {
         // Get the extension attached to MarkupResponse
         final List<UnmarshalledExtension> extensions = ExtensionAccess.getConsumerExtensionAccessor().getResponseExtensionsFrom(MarkupResponse.class);
         final UnmarshalledExtension unmarshalledExtension = extensions.get(0);

         // check that the retrieved extension does indeed contain a DOM element with the producer's response... Ideally, you would check here that you're really getting what you're expecting
         if (unmarshalledExtension.isElement())
         {
            final Element element = (Element)unmarshalledExtension.getValue();
            final String textContent = element.getTextContent();

            log.info("Got response: " + textContent);

            // put the response value in the session so that portlets use it
            invocation.getDispatchedRequest().getSession().setAttribute("producerAnswered", textContent);
         }
      }
   }


}
