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

import org.gatein.pc.federation.FederatingPortletInvoker;
import org.gatein.wsrp.api.session.SessionEventBroadcaster;
import org.gatein.wsrp.consumer.handlers.session.SessionRegistry;
import org.gatein.wsrp.consumer.migration.MigrationService;
import org.gatein.wsrp.consumer.registry.ConsumerRegistry;

/** @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a> */
public interface ConsumerRegistrySPI extends ConsumerRegistry
{
   void setSessionEventBroadcaster(SessionEventBroadcaster sessionEventBroadcaster);

   void setFederatingPortletInvoker(FederatingPortletInvoker federatingPortletInvoker);

   MigrationService getMigrationService();

   void setMigrationService(MigrationService migrationService);

   SessionRegistry getSessionRegistry();

   void setSessionRegistry(SessionRegistry sessionRegistry);

   void start() throws Exception;

   void stop() throws Exception;
}
