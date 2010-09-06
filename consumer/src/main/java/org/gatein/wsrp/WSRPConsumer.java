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

package org.gatein.wsrp;

import org.gatein.pc.api.PortletInvoker;
import org.gatein.pc.api.PortletInvokerException;
import org.gatein.pc.api.invocation.PortletInvocation;
import org.gatein.wsrp.api.SessionEventListener;
import org.gatein.wsrp.consumer.ProducerInfo;
import org.gatein.wsrp.consumer.RefreshResult;
import org.gatein.wsrp.consumer.handlers.ProducerSessionInformation;
import org.gatein.wsrp.consumer.migration.ExportInfo;
import org.gatein.wsrp.consumer.migration.MigrationService;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * @author <a href="mailto:boleslaw.dawidowicz@jboss.org">Boleslaw Dawidowicz</a>
 * @author <a href="mailto:chris.laprun@jboss.com?subject=org.gatein.wsrp.WSRPConsumer">Chris Laprun</a>
 * @version $Revision: 8782 $
 * @since 2.4
 */
public interface WSRPConsumer extends PortletInvoker, SessionEventListener
{
   /**
    * Retrieves the identifier for the producer this consumer is associated with.
    *
    * @return the identifier of the associated producer
    */
   String getProducerId();

   /**
    * Retrieves the session information for the producer associated with this consumer.
    *
    * @param invocation a portlet invocation from which the session information should be extracted.
    * @return the session information for the producer associated with this consumer.
    * @see org.gatein.wsrp.consumer.handlers.ProducerSessionInformation
    */
   ProducerSessionInformation getProducerSessionInformationFrom(PortletInvocation invocation);

   /**
    * Retrieves the session information for the producer associated with this consumer.
    *
    * @param session the session from the information should be extracted.
    * @return the session information for the producer associated with this consumer.
    * @see org.gatein.wsrp.consumer.handlers.ProducerSessionInformation
    */
   ProducerSessionInformation getProducerSessionInformationFrom(HttpSession session);

   /**
    * @return
    * @since 2.6
    */
   ProducerInfo getProducerInfo();

   /**
    * @throws PortletInvokerException
    * @since 2.6
    */
   void refreshProducerInfo() throws PortletInvokerException;

   /**
    * Releases all the sessions held by this Consumer
    *
    * @since 2.6
    */
   void releaseSessions() throws PortletInvokerException;

   /**
    * Prepares this Consumer to be used: service is started, endpoints are ready.
    *
    * @throws Exception
    * @since 2.6
    */
   void activate() throws Exception;

   /**
    * Removes this Consumer from service. It cannot be used before being activated again.
    *
    * @throws Exception
    * @since 2.6
    */
   void deactivate() throws Exception;

   /**
    * @return
    * @since 2.6
    */
   boolean isActive();

   /**
    * @return
    * @since 2.6
    */
   boolean isRefreshNeeded();

   /**
    * @param forceRefresh
    * @return
    * @since 2.6
    */
   RefreshResult refresh(boolean forceRefresh) throws PortletInvokerException;

   void start() throws Exception;

   void stop() throws Exception;

   ExportInfo exportPortlets(List<String> portletHandles) throws PortletInvokerException;

   MigrationService getMigrationService();
}
