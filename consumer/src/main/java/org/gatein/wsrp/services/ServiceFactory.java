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
package org.gatein.wsrp.services;

import org.gatein.common.util.Version;

/**
 * A factory that gives access to WSRP services published by a remote producer based on the metadata provided by its WSDL file. This WSDL file is parsed and analyzed to extract
 * the supported WSRP version as well as individual Services information.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 11484 $
 */
public interface ServiceFactory extends Cloneable
{
   int DEFAULT_TIMEOUT_MS = 10000;
   Version WSRP2 = new Version("WSRP", 2, 0, 0, new Version.Qualifier(Version.Qualifier.Prefix.GA), "WSRP2");
   Version WSRP1 = new Version("WSRP", 1, 0, 0, new Version.Qualifier(Version.Qualifier.Prefix.GA), "WSRP1");

   /**
    * Generic, typesafe version of the get*Service methods.
    *
    * @param clazz the expected class of the service we're trying to retrieve
    * @param <T>   the generic type of the service we're trying to retrieve
    * @return the service instance associated with the specified type if any
    * @throws Exception if the service couldn't be initialized or retrieved
    */
   <T> T getService(Class<T> clazz) throws Exception;

   /**
    * Determines whether or not this ServiceFactory is able to provide services. A non-available ServiceFactory might be
    * in a temporary state of non-availability (e.g. if the remote host is not currently reachable) or permanently
    * (because, e.g. its configuration is invalid). Permanent failure is indicated by {@link #isFailed()} status.
    *
    * @return <code>true</code> if this ServiceFactory is ready to provide services, <code>false</code> otherwise.
    */
   boolean isAvailable();

   /**
    * Determines whether or not this ServiceFactory is in a permanent state of failure which cannot be recovered from
    * without user intervention. This notably happens if the configuration is incorrect (i.e. remote host URLs are
    * invalid).
    *
    * @return <code>true</code> if this ServiceFactory is not configured properly, <code>false</code> otherwise.
    */
   boolean isFailed();

   /**
    * Performs initialization of this ServiceFactory based on available connection metadata.
    *
    * @throws Exception if initialization and connection to the remote service couldn't be performed.
    */
   void start() throws Exception;

   /** Performs any clean-up operation needed when this factory is being shutdown. */
   void stop();

   /**
    * Specifies the URL of the WSDL that needs to be analyzed for WSRP service publication.
    *
    * @param wsdlDefinitionURL a String representation of the URL of the remote producer's WSDL
    */
   void setWsdlDefinitionURL(String wsdlDefinitionURL);

   /**
    * Retrieves the current String representation of the remote producer's WSDL URL.
    *
    * @return the current String representation of the remote producer's WSDL URL.
    */
   String getWsdlDefinitionURL();

   /**
    * Specifies how many milliseconds this factory waits before deciding a WS operation is considered as having timed out.
    *
    * @param msBeforeTimeOut number of milliseconds to wait for a WS operation to return before timing out. Will be set
    *                        to {@link #DEFAULT_TIMEOUT_MS} if negative.
    */
   void setWSOperationTimeOut(int msBeforeTimeOut);

   /**
    * Retrieves the current number of milliseconds this factory waits before deciding that a WS operation has timed out.
    *
    * @return the current number of milliseconds this factory waits before deciding that a WS operation has timed out.
    */
   int getWSOperationTimeOut();


   ServiceDescriptionService getServiceDescriptionService() throws Exception;

   MarkupService getMarkupService() throws Exception;

   PortletManagementService getPortletManagementService() throws Exception;

   RegistrationService getRegistrationService() throws Exception;

   /**
    * Returns the WSRP version of the remote service that this ServiceFactory connects to or <code>null</code> if the
    * ServiceFactory is not available.
    */
   Version getWSRPVersion();

   /**
    * Refreshes (if needed) the information held by this factory from the producer's WSDL.
    *
    * @param force whether or not to force the refresh (i.e. retrieval and parsing of the WSDL information), regardless of potential cache
    * @return <code>true</code> if a refresh occurred as a result of this operation, <code>false</code> otherwise
    * @throws Exception
    */
   boolean refresh(boolean force) throws Exception;

   /**
    * Specifies whether WS-Security (WSS) is activated when accessing the services held by this ServiceFactory.
    *
    * @param enable <code>true</code> to activate WS-Security, <code>false</code> to disable it
    */
   void enableWSS(boolean enable);

   /**
    * Retrieves whether WSS is enabled or not on this ServiceFactory
    *
    * @return <code>true</code> if WSS is enable for this ServiceFactory, <code>false</code> otherwise
    */
   boolean isWSSEnabled();

   /**
    * Determines whether WS-Security is configured and available to be enabled on this ServiceFactory. For WSS to be enabled, this method must first return <code>true</code>.
    *
    * @return <code>true</code> if WS-Security is configured and available, <code>false</code> otherwise
    */
   boolean isWSSAvailable();

   ServiceFactory clone();
}
