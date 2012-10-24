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

package org.gatein.wsrp.test.support;

import org.gatein.common.net.media.MediaType;
import org.gatein.common.util.MarkupInfo;
import org.gatein.pc.api.ActionURL;
import org.gatein.pc.api.ContainerURL;
import org.gatein.pc.api.RenderURL;
import org.gatein.pc.api.ResourceURL;
import org.gatein.pc.api.StateString;
import org.gatein.pc.api.URLFormat;
import org.gatein.pc.api.spi.PortletInvocationContext;
import org.gatein.pc.portlet.impl.spi.AbstractPortletInvocationContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
      super(MediaType.TEXT_HTML);

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
      String result;
      if (containerURL instanceof ActionURL)
      {
         ActionURL actionURL = (ActionURL)containerURL;
         result = "Action is=" + actionURL.getInteractionState().getStringValue();
      }
      else if (containerURL instanceof RenderURL)
      {
         result = "Render";
      }
      else //dealing with a resource
      {
         //fake setup which approximates what the actual PortletInvocationContext should be doing.
         String url = "http://test/mock:type=resource?mock:ComponentID=foo-bar";
         ResourceURL resourceURL = ((ResourceURL)containerURL);
         
         if (urlFormat.getWantEscapeXML())
         {
            url += "&amp;";
         }
         else
         {
            url += "&";  
         }
         
         url += "mock:resourceID=" + resourceURL.getResourceId();
         
         return url;
      }

      StateString ns = containerURL.getNavigationalState();
      result += " ns=" + (ns != null ? ns.getStringValue() : null) + " ws=" + containerURL.getWindowState() + " m="
         + containerURL.getMode();

      return result;
   }
}
