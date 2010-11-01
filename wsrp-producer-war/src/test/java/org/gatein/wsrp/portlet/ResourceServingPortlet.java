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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletRequestDispatcher;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.ResourceRequest;
import javax.portlet.ResourceResponse;

/**
 * @author <a href="mailto:mvanco@redhat.com">Michal Vanco</a>
 * @version $Revision$
 */
public class ResourceServingPortlet extends GenericPortlet
{

   @Override
   public void doView(RenderRequest request, RenderResponse response) throws PortletException, IOException
   {
      response.setContentType("text/html");
      String imageHash = "142b674dd615a0513061cf44c7ae6adb";
      request.setAttribute("hash", imageHash);
      String imageWidth = "451";
      String imageHeight = "257";
      request.setAttribute("width", imageWidth);
      request.setAttribute("height", imageHeight);
      PortletRequestDispatcher dispatcher = getPortletContext().getRequestDispatcher("/view.jsp");
      dispatcher.include(request, response);
   }

   @Override
   public void serveResource(ResourceRequest request, ResourceResponse response) throws PortletException, IOException
   {
      response.setContentType(" image/png");
      OutputStream out = response.getPortletOutputStream();
      File image = new File(getPortletContext().getRealPath("/image/jboss_logo.png"));
      BufferedInputStream bis = new BufferedInputStream(new FileInputStream(image));
      long length = image.length();
      byte[] bytes = new byte[(int) length];
      bis.read(bytes, 0, (int) length);
      out.write(bytes);
   }

}
