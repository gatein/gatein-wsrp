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
import javax.portlet.PortletPreferences;
import javax.portlet.PortletSecurityException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class StatePortlet extends GenericPortlet
{
   private static final String COUNT = "count";

   public void processAction(ActionRequest request, ActionResponse response) throws PortletException, PortletSecurityException, IOException
   {
      String value = request.getParameter("value");
      if (value == null)
      {
         value = "default";
      }

      PortletPreferences pp = request.getPreferences();
      pp.setValue("name", value);
      pp.store();
   }
   
   protected void doView(RenderRequest req, RenderResponse resp) throws PortletException, PortletSecurityException, IOException
   {
      resp.setContentType("text/html");
      PortletPreferences pp = req.getPreferences();
      String value = pp.getValue("name", "default");
      resp.getWriter().write(value);
   }
}

