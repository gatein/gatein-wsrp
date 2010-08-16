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

package org.gatein.wsrp.producer.v2;

import org.gatein.exports.ExportManager;
import org.gatein.wsrp.producer.MarkupInterface;
import org.gatein.wsrp.producer.PortletManagementInterface;
import org.gatein.wsrp.producer.RegistrationInterface;
import org.gatein.wsrp.producer.ServiceDescriptionInterface;
import org.gatein.wsrp.producer.WSRPProducer;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public interface WSRP2Producer extends WSRPProducer, MarkupInterface, PortletManagementInterface,
   RegistrationInterface, ServiceDescriptionInterface
{
   /**
    * Retrieves the ExportManager used by this WSRPProducer.
    * 
    * @return The ExportManager used by this WSRPProducer to manage exports
    */
   ExportManager getExportManager();
   
   /**
    * Sets the ExportManager used by this WSRPProducer
    * 
    * @param The ExportManager to be used by this WSRPProducer
    */
   void setExportManager(ExportManager exportManager);
}
