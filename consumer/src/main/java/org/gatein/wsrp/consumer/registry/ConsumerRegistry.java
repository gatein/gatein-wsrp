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

package org.gatein.wsrp.consumer.registry;

import org.gatein.pc.federation.FederatingPortletInvoker;
import org.gatein.wsrp.WSRPConsumer;
import org.gatein.wsrp.api.session.SessionEventBroadcaster;
import org.gatein.wsrp.consumer.ConsumerException;
import org.gatein.wsrp.consumer.ProducerInfo;
import org.gatein.wsrp.consumer.migration.MigrationService;

import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 12693 $
 * @since 2.6
 */
public interface ConsumerRegistry
{
   List<WSRPConsumer> getConfiguredConsumers();

   WSRPConsumer getConsumer(String id);

   FederatingPortletInvoker getFederatingPortletInvoker();

   WSRPConsumer createConsumer(String id, Integer expirationCacheSeconds, String wsdlURL) throws ConsumerException;

   void persistConsumer(WSRPConsumer consumer) throws ConsumerException;

   /**
    * Activates the consumer associated with the specified identifier if and only if access to the remote producer is
    * properly setup (i.e. the associated service factory MUST be available).
    *
    * @param id the identifier of the consumer to be activated
    * @throws ConsumerException
    */
   void activateConsumerWith(String id) throws ConsumerException;

   void updateProducerInfo(ProducerInfo producerInfo) throws ConsumerException;

   void deactivateConsumerWith(String id) throws ConsumerException;

   void registerOrDeregisterConsumerWith(String id, boolean register) throws ConsumerException;

   void destroyConsumer(String id) throws ConsumerException;

   void reloadConsumers();

   void start() throws Exception;

   void stop() throws Exception;

   void setSessionEventBroadcaster(SessionEventBroadcaster sessionEventBroadcaster);

   void setFederatingPortletInvoker(FederatingPortletInvoker federatingPortletInvoker);

   ProducerInfo getProducerInfoByKey(String key);

   MigrationService getMigrationService();

   void setMigrationService(MigrationService migrationService);
}