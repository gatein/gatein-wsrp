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
package org.gatein.wsrp.portlet;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletSecurityException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.portlet.PortletFileUpload;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.junit.Assert;
import org.oasis.wsrp.v2.PerformBlockingInteraction;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Writer;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class ResourcePortlet extends GenericPortlet
{
   protected void doView(RenderRequest request, RenderResponse response) throws PortletException, PortletSecurityException, IOException
   {
      response.setContentType("text/html");
      Writer writer = response.getWriter();
      writer.write("<img src='" + response.encodeURL(request.getContextPath() + "/gif/logo.gif") + "'/>");
   }

   @Override
   public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException
   {  
      String resourceID = request.getResourceID();
      
      if (resourceID.equals("resourceFromWriter"))
      {
         response.setContentType("image/png");
         Writer writer = response.getWriter();
         writer.write("foo");
         response.setContentType("text/html");
         writer.close();
      }
      else if (resourceID.equals("resourceFromStream"))
      {
         response.setContentType("image/png");
         OutputStream stream = response.getPortletOutputStream();
         byte[] byteArray = {0, 1, 2, 3, 4};
         stream.write(byteArray);
         response.setContentType("text/html");
         stream.close();
      }
      else if (resourceID.equals("uploaded"))
      {
         Assert.assertTrue(request.getContentType().startsWith("multipart"));
         InputStream inputStream = request.getPortletInputStream();
         BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
         String line = reader.readLine();
         Assert.assertTrue(line.startsWith("--"));
         line = reader.readLine();
         Assert.assertEquals("Content-type: image/png", line);
         line = reader.readLine();
         Assert.assertTrue(line.isEmpty());
         String image = reader.readLine();
         line = reader.readLine();
         Assert.assertTrue(line.startsWith("--"));
         Assert.assertTrue(line.endsWith("--"));

         response.setContentType("image/png");
         OutputStream outputStream = response.getPortletOutputStream();
         outputStream.write(image.getBytes());
         outputStream.close();
      }   
      else
      {
         throw new PortletException("No known resource with id: " + resourceID);
      }
   }
   
   @Override
   public void processAction(ActionRequest request, ActionResponse response) throws PortletException, IOException
   {
      String actionName = request.getParameter(ActionRequest.ACTION_NAME);
      if (actionName.equals("uploadImage"))
      {
         Assert.assertTrue(request.getContentType().startsWith("multipart"));
         InputStream inputStream = request.getPortletInputStream();
         BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
         String line = reader.readLine();
         Assert.assertTrue(line.startsWith("--"));
         line = reader.readLine();
         Assert.assertEquals("Content-type: image/png", line);
         line = reader.readLine();
         Assert.assertTrue(line.isEmpty());
         String image = reader.readLine();
         line = reader.readLine();
         Assert.assertTrue(line.startsWith("--"));
         
         byte[] expectedBytes = new byte[]{0, 1, 2, 3, 4};
         byte[] receivedBytes = image.getBytes();
         Assert.assertEquals(expectedBytes.length, receivedBytes.length);
         for (int i = 0; i < expectedBytes.length; i++){
            Assert.assertEquals(expectedBytes[i], receivedBytes[i]);
         }
      }
      else
      {
         throw new PortletException("Unkown Action:" + actionName);
      }
   }
   
}
