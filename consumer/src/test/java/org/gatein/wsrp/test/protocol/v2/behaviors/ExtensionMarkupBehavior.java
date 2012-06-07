/*
* JBoss, a division of Red Hat
* Copyright 2008, Red Hat Middleware, LLC, and individual contributors as indicated
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

package org.gatein.wsrp.test.protocol.v2.behaviors;

import org.gatein.pc.api.Mode;
import org.gatein.pc.api.WindowState;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.api.extensions.UnmarshalledExtension;
import org.gatein.wsrp.payload.PayloadUtils;
import org.gatein.wsrp.test.protocol.v2.BehaviorRegistry;
import org.gatein.wsrp.test.protocol.v2.MarkupBehavior;
import org.oasis.wsrp.v2.AccessDenied;
import org.oasis.wsrp.v2.Extension;
import org.oasis.wsrp.v2.GetMarkup;
import org.oasis.wsrp.v2.InconsistentParameters;
import org.oasis.wsrp.v2.InvalidCookie;
import org.oasis.wsrp.v2.InvalidHandle;
import org.oasis.wsrp.v2.InvalidRegistration;
import org.oasis.wsrp.v2.InvalidSession;
import org.oasis.wsrp.v2.InvalidUserCategory;
import org.oasis.wsrp.v2.MarkupResponse;
import org.oasis.wsrp.v2.MissingParameters;
import org.oasis.wsrp.v2.OperationFailed;
import org.oasis.wsrp.v2.UnsupportedLocale;
import org.oasis.wsrp.v2.UnsupportedMimeType;
import org.oasis.wsrp.v2.UnsupportedMode;
import org.oasis.wsrp.v2.UnsupportedWindowState;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.List;

/** @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a> */
public class ExtensionMarkupBehavior extends MarkupBehavior
{
   public static final String PORTLET_HANDLE = "ExtensionMarkupBehavior";
   public static final String SIMPLE_SUCCESS = "Simple Success";
   public static final String ELEMENT_SUCCESS = "Element Success";
   public static final String FAILURE = "Failure!";
   public static final String EXPECTED_REQUEST_EXTENSION_VALUE = "foo";
   public static final String EXPECTED_RESPONSE_EXTENSION_VALUE = "bar";

   private static Element ELEMENT_RESPONSE;

   static
   {
      /*
      <ext1:MarkupResponseState xmlns:ext1='urn:bea:wsrp:ext:v1:types'>
                        <ext1:state>
                                     bar
                        </ext1:state>
               </ext1:MarkupResponseState>
       */
      final String namespaceURI = "urn:bea:wsrp:ext:v1:types";
      ELEMENT_RESPONSE = PayloadUtils.createElement(namespaceURI, "MarkupResponseState");
      Node state = ELEMENT_RESPONSE.getOwnerDocument().createElementNS(namespaceURI, "state");
      state = ELEMENT_RESPONSE.appendChild(state);
      state.setTextContent(ExtensionMarkupBehavior.EXPECTED_RESPONSE_EXTENSION_VALUE);
   }

   private static enum Status
   {
      simple(SIMPLE_SUCCESS, EXPECTED_RESPONSE_EXTENSION_VALUE), element(ELEMENT_SUCCESS, ELEMENT_RESPONSE), failure(FAILURE, null);

      private Status(String markup, Object response)
      {
         this.markup = markup;
         this.response = response;
      }

      private final String markup;
      private final Object response;

   }

   private Status status;

   public ExtensionMarkupBehavior(BehaviorRegistry registry)
   {
      super(registry);
      registerHandle(PORTLET_HANDLE);
   }

   @Override
   protected String getMarkupString(Mode mode, WindowState windowState, String navigationalState, GetMarkup getMarkup) throws UnsupportedWindowState, InvalidCookie, InvalidSession, AccessDenied, InconsistentParameters, InvalidHandle, UnsupportedLocale, UnsupportedMode, OperationFailed, MissingParameters, InvalidUserCategory, InvalidRegistration, UnsupportedMimeType
   {
      status = Status.failure;

      try
      {
         final List<Extension> extensions = getMarkup.getMarkupParams().getExtensions();
         if (!extensions.isEmpty())
         {
            final Extension extension = extensions.get(0);
            final UnmarshalledExtension unmarshalledExtension = PayloadUtils.unmarshallExtension(extension.getAny());
            if (unmarshalledExtension.isElement())
            {
               final Element element = (Element)unmarshalledExtension.getValue();
               if (EXPECTED_REQUEST_EXTENSION_VALUE.equals(element.getTextContent()))
               {
                  status = Status.element;
               }
            }
            else
            {
               final String foo = (String)unmarshalledExtension.getValue();
               if (EXPECTED_REQUEST_EXTENSION_VALUE.equals(foo))
               {
                  status = Status.simple;
               }
            }
         }
      }
      catch (Exception e)
      {
         status = Status.failure;
         throw new RuntimeException(e);
      }

      return status.markup;
   }

   @Override
   public void modifyResponseIfNeeded(MarkupResponse markupResponse)
   {
      if (!Status.failure.equals(status))
      {
         markupResponse.getExtensions().add(WSRPTypeFactory.createExtension(PayloadUtils.marshallExtension(status.response)));
      }
   }
}
