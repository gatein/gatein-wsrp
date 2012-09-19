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
package org.gatein.wsrp.portlet;

import org.gatein.wsrp.WSRPConstants;

import javax.portlet.EventRequest;
import javax.portlet.EventResponse;
import javax.portlet.GenericPortlet;
import javax.portlet.PortletException;
import javax.portlet.PortletSecurityException;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import java.io.IOException;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class BasicPortlet extends GenericPortlet
{

   protected void doView(RenderRequest request, RenderResponse response) throws PortletException, PortletSecurityException, IOException
   {
      if (!Boolean.TRUE.equals(request.getAttribute(WSRPConstants.FROM_WSRP_ATTRIBUTE_NAME)))
      {
         throw new IllegalStateException("Invocation should be marked as coming from WSRP using the WSRPConstants.FROM_WSRP_ATTRIBUTE_NAME attribute!");
      }
      System.out.println("do view");
   }

   @Override
   public void processEvent(EventRequest request, EventResponse response) throws PortletException, IOException
   {
      System.out.println("zipcode: " + request.getEvent().getValue());
   }
}
