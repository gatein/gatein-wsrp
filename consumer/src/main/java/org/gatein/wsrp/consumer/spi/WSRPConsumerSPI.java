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
   RegistrationContext getRegistrationContext() throws PortletInvokerException;

   UserContext getUserContextFrom(WSRPPortletInfo info, PortletInvocation invocation, RuntimeContext runtimeContext) throws PortletInvokerException;

   SessionHandler getSessionHandler();

   void setTemplatesIfNeeded(WSRPPortletInfo info, PortletInvocation invocation, RuntimeContext runtimeContext) throws PortletInvokerException;

   void handleInvalidRegistrationFault() throws PortletInvokerException;

   MarkupService getMarkupService() throws PortletInvokerException;

   /**
    * Determines whether the specified user scope (for markup caching) is supported.
    *
    * @param userScope the user scope which support is to be determined
    * @return <code>true</code> if the given user scope is supported, <code>false</code> otherwise
    */
   boolean supportsUserScope(String userScope);

   WSRPPortletInfo getPortletInfo(PortletInvocation invocation) throws PortletInvokerException;

   SessionRegistry getSessionRegistry();

   void handleModifyRegistrationRequiredFault();
}
