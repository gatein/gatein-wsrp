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

import java.io.IOException;
import java.io.PrintWriter;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletSecurityException;
import javax.portlet.PortletURL;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class RenderParamPortlet extends GenericPortlet
{
   private static final String NAME = "name";
   private int count = 0;
   private static final String OP = "op";
   private static final String INC = "++";
   private static final String DEC = "--";

   public void processAction(ActionRequest request, ActionResponse response) throws PortletException, PortletSecurityException, IOException
   {
      String op = request.getParameter(OP);
      if (INC.equals(op))
      {
         count++;
      }
      else if (DEC.equals(op))
      {
         count--;
      }
      else
      {
         throw new PortletException("Unrecognized operation!");
      }
   }

   public void render(RenderRequest request, RenderResponse response) throws PortletException, PortletSecurityException, IOException
   {
      response.setContentType("text/html");
      PrintWriter writer = response.getWriter();

      String name = request.getParameter(NAME);
      if (name == null)
      {
         name = "Anonymous";
      }

      writer.println("Hello, " + name + "!");
      writer.println("Counter: " + count);
      PortletURL url = response.createRenderURL();
      url.setParameter(NAME, "Julien");
      writer.println("<a href='" + url + "'>My name is Julien</a>");
      url.setParameter(NAME, "Roy");
      writer.println("<a href='" + url + "'>My name is Roy</a>");
      url = response.createActionURL();
      url.setParameter(OP, INC);
      writer.println("<a href='" + url + "'>counter++</a>");
      url.setParameter(OP, DEC);
      writer.println("<a href='" + url + "'>counter--</a>");
   }
}

