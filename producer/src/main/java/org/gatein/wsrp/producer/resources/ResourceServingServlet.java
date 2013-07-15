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

package org.gatein.wsrp.producer.resources;

import org.gatein.common.util.ParameterValidation;
import org.gatein.wsrp.ResourceServingUtil;
import org.gatein.wsrp.producer.ProducerHolder;
import org.gatein.wsrp.producer.v2.WSRP2Producer;
import org.oasis.wsrp.v2.GetResource;
import org.oasis.wsrp.v2.ResourceContext;
import org.oasis.wsrp.v2.ResourceResponse;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * A simple resource serving servlet delegating direct resource calls to the producer.
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class ResourceServingServlet extends HttpServlet
{
   @Override
   protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
   {
      WSRP2Producer producer = ProducerHolder.getProducer();

      GetResource getResource = ResourceServingUtil.decode(req);

      try
      {
         ResourceResponse resource = producer.getResource(getResource);
         ResourceContext resourceContext = resource.getResourceContext();
         byte[] itemBinary = resourceContext.getItemBinary();
         String itemString = resourceContext.getItemString();

         final String mimeType = resourceContext.getMimeType();
         if (!ParameterValidation.isNullOrEmpty(mimeType))
         {
            resp.setContentType(mimeType);
         }

         if (itemBinary != null && itemBinary.length > 0)
         {
            resp.getOutputStream().write(itemBinary);
         }

         if (!ParameterValidation.isNullOrEmpty(itemString))
         {
            resp.getWriter().write(itemString);
         }
      }
      catch (Exception exception)
      {
         throw new ServletException("Couldn't get resource " + getResource.getResourceParams().getResourceID()
            + " for portlet " + getResource.getPortletContext(), exception);
      }
   }
}
