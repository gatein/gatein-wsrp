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
import javax.portlet.GenericPortlet;
import javax.portlet.PortletMode;
import javax.portlet.PortletModeException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.io.IOException;
import java.io.Writer;

/**
 * @author <a href="mailto:chris.laprun@jboss.com?subject=org.jboss.portal.test.wsrp.portlet.MultiValuedPortlet">Chris
 *         Laprun</a>
 * @version $Revision$
 */
public class MultiValuedPortlet extends GenericPortlet
{
   private static final String MULTI = "multi";

   public void processAction(ActionRequest req, ActionResponse resp) throws PortletModeException, IOException
   {
      String[] multi = req.getParameterValues(MULTI);

      if (multi != null)
      {
         resp.setRenderParameter(MULTI, multi);
      }
      resp.setPortletMode(PortletMode.VIEW);
   }

   protected void doView(RenderRequest request, RenderResponse response) throws IOException
   {
      response.setContentType("text/html");
      Writer writer = response.getWriter();
      writer.write("multi: ");
      String[] values = request.getParameterValues(MULTI);
      if (values != null)
      {
         StringBuffer sb = new StringBuffer(32);
         for (int i = 0; i < values.length; i++)
         {
            sb.append(values[i]);
            if (i != values.length - 1)
            {
               sb.append(", ");
            }
         }
         writer.write(sb.toString());
      }
   }
}

