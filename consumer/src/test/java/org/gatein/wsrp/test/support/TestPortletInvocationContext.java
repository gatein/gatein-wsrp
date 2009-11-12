/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2006, Red Hat Middleware, LLC, and individual                    *
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

package org.gatein.wsrp.test.support;

import org.gatein.common.net.media.MediaType;
import org.gatein.common.util.MarkupInfo;
import org.gatein.common.util.ParameterMap;
import org.gatein.pc.api.ContainerURL;
import org.gatein.pc.api.StateString;
import org.gatein.pc.api.URLFormat;
import org.gatein.pc.api.spi.PortletInvocationContext;
import org.gatein.pc.portlet.impl.spi.AbstractPortletInvocationContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 11068 $
 * @since 2.4
 */
public class TestPortletInvocationContext extends AbstractPortletInvocationContext implements PortletInvocationContext
{

   private HttpServletRequest mockRequest;
   private HttpServletResponse mockResponse;

   public TestPortletInvocationContext()
   {
      super(new MarkupInfo(MediaType.TEXT_HTML, "UTF-8"));  // character set same as WSRPConstants.DEFAULT_CHARACTER_SET

      mockRequest = MockHttpServletRequest.createMockRequest(null);
      mockResponse = MockHttpServletResponse.createMockResponse();
   }

   public void setMockRequest(HttpServletRequest mockRequest)
   {
      this.mockRequest = mockRequest;
   }

   public void setMockResponse(HttpServletResponse mockResponse)
   {
      this.mockResponse = mockResponse;
   }

   public HttpServletRequest getClientRequest()
   {
      if (mockRequest == null)
      {
         throw new IllegalStateException();
      }
      return mockRequest;
   }

   public HttpServletResponse getClientResponse()
   {
      if (mockResponse == null)
      {
         throw new IllegalStateException();
      }
      return mockResponse;
   }

   public String encodeResourceURL(String url)
   {
      return null;
   }

   public String renderURL(ContainerURL containerURL, URLFormat urlFormat)
   {
      return null;
   }

   public String getCharacterEncoding()
   {
      return null;
   }

   public int getContentLength()
   {
      return 0;
   }

   public BufferedReader getReader() throws IOException
   {
      return null;
   }

   public InputStream getInputStream() throws IOException
   {
      return null;
   }

   public String getContentType()
   {
      return null;
   }

   public StateString getInteractionState()
   {
      return null;
   }

   public ParameterMap getForm()
   {
      return null;
   }
}
