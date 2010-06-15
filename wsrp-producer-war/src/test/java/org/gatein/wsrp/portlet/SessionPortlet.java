package org.gatein.wsrp.portlet;
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
import java.io.IOException;
import java.io.PrintWriter;

import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletSecurityException;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

/**
 * A simple portlet to test session handling in WSRP.
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class SessionPortlet extends GenericPortlet
{
   private static final String COUNT = "count";

   protected void doView(RenderRequest req, RenderResponse resp) throws PortletException, PortletSecurityException, IOException
   {
      resp.setContentType("text/html");
      PrintWriter writer = resp.getWriter();

      //
      PortletSession session = req.getPortletSession();
      int count = 0;
      if (session.getAttribute(COUNT) != null)
      {
         count = ((Integer)session.getAttribute(COUNT)).intValue() + 1;
      }
      session.setAttribute(COUNT, new Integer(count));
      writer.write("<p>Session id: " + session.getId() + "</p>");
      writer.write("<p>count = " + count + "</p>");
      writer.write("<a href='" + resp.createRenderURL() + "'>render</a>");

      //
      writer.close();
   }
}
