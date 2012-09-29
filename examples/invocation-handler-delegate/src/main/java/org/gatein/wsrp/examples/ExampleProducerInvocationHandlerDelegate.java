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

import org.gatein.pc.api.invocation.PortletInvocation;
import org.gatein.pc.api.invocation.RenderInvocation;
import org.gatein.pc.api.invocation.response.ContentResponse;
import org.gatein.pc.api.invocation.response.PortletInvocationResponse;
import org.gatein.wsrp.api.extensions.ExtensionAccess;
import org.gatein.wsrp.api.extensions.InvocationHandlerDelegate;
import org.gatein.wsrp.api.extensions.UnmarshalledExtension;
import org.oasis.wsrp.v2.MarkupParams;
import org.oasis.wsrp.v2.MarkupResponse;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.List;

/** @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a> */
public class ExampleProducerInvocationHandlerDelegate extends InvocationHandlerDelegate
{
   @Override
   public void processInvocation(PortletInvocation invocation)
   {
      // only process render requests
      if (invocation instanceof RenderInvocation)
      {
         // retrieve the extensions that were passed to the producer
         final List<UnmarshalledExtension> requestExtensions = ExtensionAccess.getProducerExtensionAccessor().getRequestExtensionsFor(MarkupParams.class);

         if (!requestExtensions.isEmpty())
         {
            // ideally, you should check here that you really got the extension you expected but we're assuming here that we got exactly what we're expecting
            final UnmarshalledExtension unmarshalledExtension = requestExtensions.get(0);
            if (unmarshalledExtension.isElement())
            {
               // get the text content which should be the session id that the consumer passed
               final Element element = (Element)unmarshalledExtension.getValue();
               final String textContent = element.getTextContent();

               // and put the value in the session so that the portlet the invocation targets can use it
               invocation.getDispatchedRequest().getSession().setAttribute("consumerSaid", textContent);
            }
         }
      }
   }

   @Override
   public void processInvocationResponse(PortletInvocationResponse response, PortletInvocation invocation)
   {
      if (response instanceof ContentResponse)
      {
         // retrieve the value of the "consumerSaid" attribute
         final Object consumerSaid = invocation.getDispatchedRequest().getSession().getAttribute("consumerSaid");

         // create a DOM element to set as a response extension
         final String namespaceURI = "urn:gatein:wsrp:ext:examples"; // please choose a more appropriate namespace for your own extensions
         final Element markupResponseExtension = DOMUtils.createElement(namespaceURI, "MarkupResponseExtension");
         final Node originalSessionId = DOMUtils.addChild(markupResponseExtension, namespaceURI, "ModifiedSessionId");
         originalSessionId.setTextContent("producer" + consumerSaid);

         // add the newly created extension to the MarkupResponse element that will be created by the WSRP stack in response to this particular portlet invocation
         ExtensionAccess.getProducerExtensionAccessor().addResponseExtension(MarkupResponse.class, markupResponseExtension);
      }
   }
}
