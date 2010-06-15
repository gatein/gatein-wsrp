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
import java.io.Writer;

import javax.portlet.ActionRequest;
import javax.portlet.ActionResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletMode;
import javax.portlet.PortletModeException;
import javax.portlet.PortletSecurityException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class MarkupPortlet extends GenericPortlet
{
   private final String SYMBOL = "symbol";
   private final String VALUE = "value";
   private final String RHAT = "RHAT";
   private final String AAPL = "AAPL";
   private final String HELP = "HELP";
   private final String RHAT_VALUE = "50.50";
   private final String AAPL_VALUE = "123.45";

   public void processAction(ActionRequest req, ActionResponse resp) throws PortletModeException, IOException
   {
      String symbol = req.getParameter(SYMBOL);
      if (HELP.equalsIgnoreCase(symbol)) // fix-me: should use help mode but will do for now
      {
         resp.sendRedirect("/WEB-INF/jsp/help.jsp");
         return;
      }

      resp.setRenderParameter(SYMBOL, symbol.toUpperCase());
      if (RHAT.equalsIgnoreCase(symbol))
      {
         resp.setRenderParameter(VALUE, RHAT_VALUE);
      }
      else if (AAPL.equalsIgnoreCase(symbol))
      {
         resp.setRenderParameter(VALUE, AAPL_VALUE);
      }
      else
      {
         resp.setRenderParameter(VALUE, "5.55");
      }
      resp.setPortletMode(PortletMode.VIEW);
   }

   protected void doView(RenderRequest request, RenderResponse response) throws IOException
   {
      response.setContentType("text/html");
      Writer writer = response.getWriter();
      writer.write("<p>");
      writer.write(getSymbol(request));
      writer.write(" stock value: ");
      writer.write(getValue(request));
      writer.write("</p>");
   }

   private String getValue(RenderRequest request)
   {
      String value = request.getParameter(VALUE);
      return value == null ? "value unset" : value;
   }

   private String getSymbol(RenderRequest request)
   {
      String symbol = request.getParameter(SYMBOL);
      return symbol == null ? "symbol unset" : symbol;
   }

   protected void doEdit(RenderRequest request, RenderResponse response) throws PortletException, PortletSecurityException, IOException
   {
      response.setContentType("text/html");
      Writer writer = response.getWriter();
      StringBuffer sb = new StringBuffer(256);
      sb.append("<form method='post' action='")
         .append(response.createActionURL())
         .append("' id='").append(response.getNamespace())
         .append("portfolioManager'><table><tr><td>Stock symbol</td><td><input name='").append(SYMBOL)
         .append("'/></td></tr><tr><td><input type='submit' value='Submit'></td></tr></table></form>");

      writer.write(sb.toString());
   }
}
