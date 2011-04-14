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

import junit.framework.TestCase;
import org.gatein.common.net.media.MediaType;
import org.gatein.pc.api.Portlet;
import org.gatein.pc.api.PortletContext;
import org.gatein.pc.api.PortletInvokerException;
import org.gatein.pc.api.info.PortletInfo;
import org.gatein.registration.Registration;
import org.gatein.wsrp.WSRPConstants;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.servlet.ServletAccess;
import org.gatein.wsrp.test.support.MockHttpServletRequest;
import org.gatein.wsrp.test.support.MockHttpServletResponse;
import org.gatein.wsrp.test.support.MockHttpSession;
import org.oasis.wsrp.v2.InvalidHandle;
import org.oasis.wsrp.v2.InvalidRegistration;
import org.oasis.wsrp.v2.MarkupType;
import org.oasis.wsrp.v2.MissingParameters;
import org.oasis.wsrp.v2.ModifyRegistrationRequired;
import org.oasis.wsrp.v2.OperationFailed;
import org.oasis.wsrp.v2.PortletDescription;
import org.oasis.wsrp.v2.RegistrationContext;
import org.oasis.wsrp.v2.UnsupportedLocale;
import org.oasis.wsrp.v2.UnsupportedMimeType;
import org.oasis.wsrp.v2.UnsupportedMode;
import org.oasis.wsrp.v2.UnsupportedWindowState;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class MimeResponseProcessorTestCase extends TestCase
{
   private static final String PORTLET_HANDLE = "portletHandle";

   public void testShouldUseProvidedNamespace() throws OperationFailed, UnsupportedMode, InvalidHandle, MissingParameters, UnsupportedMimeType, UnsupportedWindowState, InvalidRegistration, ModifyRegistrationRequired, UnsupportedLocale
   {
      String namespace = "namespace";
      ServletAccess.setRequestAndResponse(MockHttpServletRequest.createMockRequest(MockHttpSession.createMockSession()), MockHttpServletResponse.createMockResponse());

      MimeResponseProcessor processor = new RenderRequestProcessor(new TestProducerHelper(), WSRPTypeFactory.createGetMarkup(null,
         WSRPTypeFactory.createPortletContext(PORTLET_HANDLE),
         WSRPTypeFactory.createRuntimeContext(WSRPConstants.NONE_USER_AUTHENTICATION, "foo", namespace), null,
         WSRPTypeFactory.createMarkupParams(false, WSRPConstants.getDefaultLocales(), WSRPConstants.getDefaultMimeTypes(), WSRPConstants.VIEW_MODE, WSRPConstants.NORMAL_WINDOW_STATE)));

      assertEquals("namespace", processor.invocation.getWindowContext().getNamespace());
   }

   public void testShouldProperlyHandleWildCardsInRequestedMimeTypes() throws OperationFailed, UnsupportedMode, InvalidHandle, MissingParameters, UnsupportedMimeType, ModifyRegistrationRequired, UnsupportedWindowState, InvalidRegistration, UnsupportedLocale
   {
      List<String> mimeTypes = new ArrayList<String>(1);
      mimeTypes.add("*/*");
      ServletAccess.setRequestAndResponse(MockHttpServletRequest.createMockRequest(MockHttpSession.createMockSession()), MockHttpServletResponse.createMockResponse());

      MimeResponseProcessor processor = new RenderRequestProcessor(new TestProducerHelper(), WSRPTypeFactory.createGetMarkup(null,
         WSRPTypeFactory.createPortletContext(PORTLET_HANDLE),
         WSRPTypeFactory.createRuntimeContext(WSRPConstants.NONE_USER_AUTHENTICATION, "foo", "ns"), null,
         WSRPTypeFactory.createMarkupParams(false, WSRPConstants.getDefaultLocales(), mimeTypes, WSRPConstants.VIEW_MODE, WSRPConstants.NORMAL_WINDOW_STATE)));

      assertEquals(TestProducerHelper.PORTLET_MIME_TYPE, processor.markupRequest.getMediaType());

      mimeTypes = new ArrayList<String>(1);
      mimeTypes.add("*");

      processor = new RenderRequestProcessor(new TestProducerHelper(), WSRPTypeFactory.createGetMarkup(null,
         WSRPTypeFactory.createPortletContext(PORTLET_HANDLE),
         WSRPTypeFactory.createRuntimeContext(WSRPConstants.NONE_USER_AUTHENTICATION, "foo", "ns"), null,
         WSRPTypeFactory.createMarkupParams(false, WSRPConstants.getDefaultLocales(), mimeTypes, WSRPConstants.VIEW_MODE, WSRPConstants.NORMAL_WINDOW_STATE)));

      assertEquals(TestProducerHelper.PORTLET_MIME_TYPE, processor.markupRequest.getMediaType());

      mimeTypes = new ArrayList<String>(1);
      mimeTypes.add("text/*");

      processor = new RenderRequestProcessor(new TestProducerHelper(), WSRPTypeFactory.createGetMarkup(null,
         WSRPTypeFactory.createPortletContext(PORTLET_HANDLE),
         WSRPTypeFactory.createRuntimeContext(WSRPConstants.NONE_USER_AUTHENTICATION, "foo", "ns"), null,
         WSRPTypeFactory.createMarkupParams(false, WSRPConstants.getDefaultLocales(), mimeTypes, WSRPConstants.VIEW_MODE, WSRPConstants.NORMAL_WINDOW_STATE)));

      assertEquals(TestProducerHelper.PORTLET_MIME_TYPE, processor.markupRequest.getMediaType());

      mimeTypes = new ArrayList<String>(1);
      mimeTypes.add("image/*");

      try
      {
         new RenderRequestProcessor(new TestProducerHelper(), WSRPTypeFactory.createGetMarkup(null,
            WSRPTypeFactory.createPortletContext(PORTLET_HANDLE),
            WSRPTypeFactory.createRuntimeContext(WSRPConstants.NONE_USER_AUTHENTICATION, "foo", "ns"), null,
            WSRPTypeFactory.createMarkupParams(false, WSRPConstants.getDefaultLocales(), mimeTypes, WSRPConstants.VIEW_MODE, WSRPConstants.NORMAL_WINDOW_STATE)));
         fail("Should have failed on unsupported MIME type");
      }
      catch (UnsupportedMimeType unsupportedMimeType)
      {
         // expected
      }
   }

   public void testShouldReturnFirstMimeTypeMatching() throws OperationFailed, UnsupportedMode, InvalidHandle, MissingParameters, UnsupportedMimeType, ModifyRegistrationRequired, UnsupportedWindowState, InvalidRegistration, UnsupportedLocale
   {
      List<String> mimeTypes = new ArrayList<String>(2);
      mimeTypes.add("text/xml");
      mimeTypes.add("text/*");

      ServletAccess.setRequestAndResponse(MockHttpServletRequest.createMockRequest(MockHttpSession.createMockSession()), MockHttpServletResponse.createMockResponse());

      MimeResponseProcessor processor = new RenderRequestProcessor(new TestProducerHelper(), WSRPTypeFactory.createGetMarkup(null,
         WSRPTypeFactory.createPortletContext(PORTLET_HANDLE),
         WSRPTypeFactory.createRuntimeContext(WSRPConstants.NONE_USER_AUTHENTICATION, "foo", "ns"), null,
         WSRPTypeFactory.createMarkupParams(false, WSRPConstants.getDefaultLocales(), mimeTypes, WSRPConstants.VIEW_MODE, WSRPConstants.NORMAL_WINDOW_STATE)));

      assertEquals("text/xml", processor.markupRequest.getMediaType());

      mimeTypes = new ArrayList<String>(2);
      mimeTypes.add("image/*");
      mimeTypes.add("text/*");

      processor = new RenderRequestProcessor(new TestProducerHelper(), WSRPTypeFactory.createGetMarkup(null,
         WSRPTypeFactory.createPortletContext(PORTLET_HANDLE),
         WSRPTypeFactory.createRuntimeContext(WSRPConstants.NONE_USER_AUTHENTICATION, "foo", "ns"), null,
         WSRPTypeFactory.createMarkupParams(false, WSRPConstants.getDefaultLocales(), mimeTypes, WSRPConstants.VIEW_MODE, WSRPConstants.NORMAL_WINDOW_STATE)));

      assertEquals(TestProducerHelper.PORTLET_MIME_TYPE, processor.markupRequest.getMediaType());
   }

   private static class TestProducerHelper implements ProducerHelper
   {
      static final String PORTLET_MIME_TYPE = MediaType.TEXT_HTML.getValue();

      public Portlet getPortletWith(PortletContext portletContext, Registration registration) throws InvalidHandle, PortletInvokerException
      {
         return new Portlet()
         {
            public PortletContext getContext()
            {
               return PortletContext.createPortletContext(PORTLET_HANDLE);
            }

            public PortletInfo getInfo()
            {
               return null;  //To change body of implemented methods use File | Settings | File Templates.
            }

            public boolean isRemote()
            {
               return false;
            }
         };
      }

      public PortletDescription getPortletDescription(org.oasis.wsrp.v2.PortletContext portletContext, List<String> locales, Registration registration) throws InvalidHandle, OperationFailed
      {
         List<String> modeNames = new ArrayList<String>(1);
         modeNames.add(WSRPConstants.VIEW_MODE);

         List<String> windowStateNames = new ArrayList<String>(1);
         windowStateNames.add(WSRPConstants.NORMAL_WINDOW_STATE);

         List<MarkupType> markupTypes = new ArrayList<MarkupType>(1);
         markupTypes.add(WSRPTypeFactory.createMarkupType(PORTLET_MIME_TYPE, modeNames, windowStateNames, locales));
         markupTypes.add(WSRPTypeFactory.createMarkupType("text/xml", modeNames, windowStateNames, locales));

         return WSRPTypeFactory.createPortletDescription(PORTLET_HANDLE, markupTypes);
      }

      public PortletDescription getPortletDescription(Portlet portlet, List<String> locales)
      {
         throw new UnsupportedOperationException();
      }

      public Registration getRegistrationOrFailIfInvalid(RegistrationContext registrationContext) throws InvalidRegistration, OperationFailed
      {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
      }
   }
}
