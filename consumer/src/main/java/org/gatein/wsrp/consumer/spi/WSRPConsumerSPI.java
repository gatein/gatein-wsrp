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

package org.gatein.wsrp.consumer.spi;

import org.gatein.pc.api.PortletInvokerException;
import org.gatein.pc.api.invocation.PortletInvocation;
import org.gatein.wsrp.WSRPConsumer;
import org.gatein.wsrp.consumer.handlers.SessionHandler;
import org.gatein.wsrp.consumer.handlers.session.SessionRegistry;
import org.gatein.wsrp.consumer.portlet.info.WSRPPortletInfo;
import org.gatein.wsrp.services.MarkupService;
import org.oasis.wsrp.v2.RegistrationContext;
import org.oasis.wsrp.v2.RuntimeContext;
import org.oasis.wsrp.v2.UserContext;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public interface WSRPConsumerSPI extends WSRPConsumer
{
   /**
    * Retrieves the current RegistrationContext if any
    *
    * @return the current RegistrationContext if any
    * @throws PortletInvokerException
    */
   RegistrationContext getRegistrationContext() throws PortletInvokerException;

   /**
    * Retrieves the user context if needed by the portlet from the invocation.
    *
    * @param info           the portlet information for the portlet being interacted with, specifically used to see if the portlet stores the user context in the session
    * @param invocation     the portlet invocation
    * @param runtimeContext the WSRP runtime context to check whether a session exists which would indicate that the user context is already in session
    * @return the user context associated with the specified invocation or <code>null</code> if it's asserted that it's stored in the session so that it's not sent again
    * @throws PortletInvokerException
    */
   UserContext getUserContextFrom(WSRPPortletInfo info, PortletInvocation invocation, RuntimeContext runtimeContext) throws PortletInvokerException;

   /**
    * Retrieves the SessionHandler associated with this consumer.
    *
    * @return the SessionHandler associated with this consumer
    */
   SessionHandler getSessionHandler();

   /**
    * Adds templates (see http://docs.oasis-open.org/wsrp/v2/wsrp-2.0-spec-os-01.html#_Producer_URL_Writing) if required.
    *
    * @param info           the portlet information for the portlet being interacted with, to check if it requires templates
    * @param invocation     the portlet invocation
    * @param runtimeContext the WSRP runtime context to which the templates need to be added
    * @throws PortletInvokerException
    */
   void setTemplatesIfNeeded(WSRPPortletInfo info, PortletInvocation invocation, RuntimeContext runtimeContext) throws PortletInvokerException;

   /**
    * Performs required maintenance when the consumer receives an InvalidaRegistrationFault from the producer. Notably, resets the registration status and check with the producer.
    *
    * @throws PortletInvokerException
    */
   void handleInvalidRegistrationFault() throws PortletInvokerException;

   MarkupService getMarkupService() throws PortletInvokerException;

   /**
    * Determines whether the specified user scope (for markup caching) is supported.
    *
    * @param userScope the user scope which support is to be determined
    * @return <code>true</code> if the given user scope is supported, <code>false</code> otherwise
    */
   boolean supportsUserScope(String userScope);

   /**
    * Retrieves the WSRP-specific {@link org.gatein.pc.api.info.PortletInfo} implementation for the portlet targeted by the specified invocation
    *
    * @param invocation the invocation targeting the portlet we want info about
    * @return the WSRP-specific {@link org.gatein.pc.api.info.PortletInfo} implementation for the portlet targeted by the specified invocation
    * @throws PortletInvokerException
    */
   WSRPPortletInfo getPortletInfo(PortletInvocation invocation) throws PortletInvokerException;

   /**
    * Retrieves the SessionRegistry associated with this consumer.
    * @return the SessionRegistry associated with this consumer.
    */
   SessionRegistry getSessionRegistry();

   /**
    * Performs required operations when the consumer receives a ModifyRegistrationRequiredFault from the producer to get the consumer ready to call modifyRegistration.
    */
   void handleModifyRegistrationRequiredFault();
}
