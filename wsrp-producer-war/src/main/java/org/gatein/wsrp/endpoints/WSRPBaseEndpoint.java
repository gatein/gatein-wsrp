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

package org.gatein.wsrp.endpoints;

import org.gatein.wsrp.api.servlet.ServletAccess;
import org.gatein.wsrp.producer.ProducerHolder;
import org.gatein.wsrp.producer.v2.WSRP2Producer;

import javax.servlet.http.HttpSession;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 13313 $
 * @since 2.4
 */
public class WSRPBaseEndpoint
{
   public WSRPBaseEndpoint()
   {
      producer = ProducerHolder.getProducer(true);
   }

   protected void forceSessionAccess()
   {
      // Check if there is a session associated with this thread
      ServletAccess.getRequest().getSession(false);

      // TODO: check if this is still needed
      /*if (portalSession != null)
      {
         // force session access from context so that cookies will be properly tranmitted to consumers. See JBWS-1515.
         context.getHttpSession();
      }*/
   }

   protected WSRP2Producer producer;
}
