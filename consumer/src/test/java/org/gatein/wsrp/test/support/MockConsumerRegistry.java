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

package org.gatein.wsrp.test.support;

import org.gatein.common.util.ParameterValidation;
import org.gatein.pc.federation.FederatingPortletInvoker;
import org.gatein.wsrp.WSRPConsumer;
import org.gatein.wsrp.api.session.SessionEventBroadcaster;
import org.gatein.wsrp.consumer.ConsumerException;
import org.gatein.wsrp.consumer.EndpointConfigurationInfo;
import org.gatein.wsrp.consumer.ProducerInfo;
import org.gatein.wsrp.consumer.migration.MigrationService;
import org.gatein.wsrp.consumer.registry.ConsumerRegistry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 12693 $
 * @since 2.6
 */
public class MockConsumerRegistry implements ConsumerRegistry
{
   private Map<String, WSRPConsumer> consumers = new HashMap(3);
   public static final String MOCK_SERVICE_DESCRIPTION = "mock-service-description";
   public static final String MOCK_MARKUP = "mock-markup";
   public static final String CONSUMER1 = "inDB";
   public static final String CONSUMER2 = "inDB2";

   /**
    * Creates a ConsumerRegistry containing 2 consumers with id '{@link #CONSUMER1}' and '{@link #CONSUMER2}'
    * respectively. CONSUMER2 is active and has a service description URL set to {@link #MOCK_SERVICE_DESCRIPTION} and
    * a
    * markup URL set to {@link #MOCK_MARKUP}
    */
   public MockConsumerRegistry()
   {
      reloadConsumers();
   }

   public List<WSRPConsumer> getConfiguredConsumers()
   {
      return new ArrayList<WSRPConsumer>(consumers.values());
   }

   public WSRPConsumer getConsumer(String id)
   {
      return consumers.get(id);
   }

   public FederatingPortletInvoker getFederatingPortletInvoker()
   {
      return null;
   }

   public WSRPConsumer createConsumer(String id, Integer expirationCacheSeconds, String wsdlURL)
   {
      MockWSRPConsumer consumer = new MockWSRPConsumer(id);
      ProducerInfo producerInfo = consumer.getProducerInfo();
      producerInfo.setExpirationCacheSeconds(expirationCacheSeconds);
      producerInfo.getEndpointConfigurationInfo().setWsdlDefinitionURL(wsdlURL);
      consumers.put(id, consumer);
      return consumer;
   }

   public void persistConsumer(WSRPConsumer consumer)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(consumer, "WSRPConsumer");
      consumers.put(consumer.getProducerId(), consumer);
   }

   public void activateConsumerWith(String id) throws ConsumerException
   {
      // do nothing
   }

   public String updateProducerInfo(ProducerInfo producerInfo)
   {
      return null;
   }

   public void deactivateConsumerWith(String id) throws ConsumerException
   {
      // do nothing
   }

   public void registerOrDeregisterConsumerWith(String id, boolean register)
   {
      // do nothing
   }

   public void destroyConsumer(String id)
   {
      // do nothing
   }

   public void reloadConsumers()
   {
      consumers.clear();
      consumers.put(CONSUMER1, new MockWSRPConsumer(CONSUMER1));
      MockWSRPConsumer consumer = new MockWSRPConsumer(CONSUMER2);
      consumer.getProducerInfo().setActive(true);
      EndpointConfigurationInfo info = consumer.getProducerInfo().getEndpointConfigurationInfo();
      consumers.put(CONSUMER2, consumer);
   }

   public void start() throws Exception
   {
      reloadConsumers();
   }

   public void stop() throws Exception
   {
      //To change body of implemented methods use File | Settings | File Templates.
   }

   public void setFederatingPortletInvoker(FederatingPortletInvoker federatingPortletInvoker)
   {
      //To change body of implemented methods use File | Settings | File Templates.
   }

   public void setSessionEventBroadcaster(SessionEventBroadcaster sessionEventBroadcaster)
   {
      //To change body of implemented methods use File | Settings | File Templates.
   }

   public ProducerInfo getProducerInfoByKey(String key)
   {
      throw new UnsupportedOperationException();
   }

   public MigrationService getMigrationService()
   {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
   }

   public void setMigrationService(MigrationService migrationService)
   {
      //To change body of implemented methods use File | Settings | File Templates.
   }

   public boolean containsConsumer(String id)
   {
      return consumers.containsKey(id);
   }

   public Collection<String> getConfiguredConsumersIds()
   {
      return consumers.keySet();
   }
}
