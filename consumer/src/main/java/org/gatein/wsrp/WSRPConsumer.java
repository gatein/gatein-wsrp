/*
 * JBoss, a division of Red Hat
 * Copyright 2011, Red Hat Middleware, LLC, and individual
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
import org.gatein.wsrp.api.session.SessionEventListener;
import org.gatein.wsrp.consumer.ProducerInfo;
import org.gatein.wsrp.consumer.RefreshResult;
import org.gatein.wsrp.consumer.handlers.ProducerSessionInformation;
import org.gatein.wsrp.consumer.migration.ExportInfo;
import org.gatein.wsrp.consumer.migration.ImportInfo;
import org.gatein.wsrp.consumer.migration.MigrationService;

import javax.servlet.http.HttpSession;
import java.util.List;

/**
 * A PortletInvoker implementation proxying a remote producer on behalf of the local portal, translating portal calls to portlets into WSRP operations on the remote producer and
 * translating the remote producer's responses back to portlet container structure so that the local portal thinks it's interacting with local portlets.
 *
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
    * Retrieves the ProducerInfo associated with this consumer.
    *
    * @return the ProducerInfo associated with this consumer.
    * @since 2.6
    */
   ProducerInfo getProducerInfo();

   /**
    * Asks this consumer's ProducerInfo to refresh itself by querying the associated remote producer for its service description and parsing it, potentially triggering other WSRP
    * operations such as registration if required.
    *
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
    * Determines whether this consumer's information needs to be refreshed with respect to its associated remote producer.
    *
    * @return <code>true</code> if this consumer needs to be refreshed, <code>false</code> otherwise
    * @since 2.6
    */
   boolean isRefreshNeeded();

   /**
    * Refreshes this consumer, potentially bypassing any cache, from the associated remote producer.
    *
    * @param forceRefresh whether we want to force the refresh (i.e. bypass potential caches).
    * @return the result of the operation
    * @since 2.6
    */
   RefreshResult refresh(boolean forceRefresh) throws PortletInvokerException;

   /**
    * Gets this consumer ready to receive and send WSRP messages to the remote producer.
    *
    * @throws Exception
    */
   void start() throws Exception;

   /**
    * Stops this consumer, making it unable to send or receive WSRP messages.
    *
    * @throws Exception
    */
   void stop() throws Exception;

   /**
    * Asks the remote producer to export the specified portlets.
    *
    * @param portletHandles a list of handles identifying portlets to be exported
    * @return the metadata resulting of the export operation
    * @throws PortletInvokerException
    */
   ExportInfo exportPortlets(List<String> portletHandles) throws PortletInvokerException;

   /**
    * Tells the remote producer that we don't need the data associated with the specified ExportInfo.
    *
    * @param exportInfo the metadata identifying the exported portlets we don't need anymore
    * @throws PortletInvokerException
    */
   void releaseExport(ExportInfo exportInfo) throws PortletInvokerException;

   /**
    * Retrieves the MigrationService associated with this consumer.
    *
    * @return the MigrationService associated with this consumer.
    */
   MigrationService getMigrationService();

   /**
    * Do we support the WSRP 2 import/export functionality?
    *
    * @return <code>true</code> if import/export is supported by this consumer, <code>false</code> otherwise
    */
   boolean isImportExportSupported();

   /**
    * Imports the specified portlets as identified by their handles associated with a previous export operation as identified by the specified ExportInfo.
    *
    * @param exportInfo the export metadata identifying which exported data we want to import from the remote producer
    * @param portlets   a list of handles identifying which portlets we want to import from the available exported portlets associated with the specified ExportInfo
    * @return the metadata associated with the result of the import operation
    * @throws PortletInvokerException
    */
   ImportInfo importPortlets(ExportInfo exportInfo, List<String> portlets) throws PortletInvokerException;

   /**
    * Is this consumer compatible with WSRP 2?
    *
    * @return <code>true</code> if this consumer uses WSRP 2, <code>false</code> otherwise
    */
   boolean isUsingWSRP2();
}
