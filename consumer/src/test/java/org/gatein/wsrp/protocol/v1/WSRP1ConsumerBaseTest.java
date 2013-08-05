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

package org.gatein.wsrp.protocol.v1;

import junit.framework.TestCase;
import org.gatein.wsrp.consumer.EndpointConfigurationInfo;
import org.gatein.wsrp.consumer.ProducerInfo;
import org.gatein.wsrp.consumer.WSRPConsumerImpl;
import org.gatein.wsrp.handler.RequestHeaderClientHandler;
import org.gatein.wsrp.test.ExtendedAssert;
import org.gatein.wsrp.test.protocol.v1.BehaviorBackedServiceFactory;
import org.gatein.wsrp.test.protocol.v1.BehaviorRegistry;
import org.gatein.wsrp.test.protocol.v1.PortletManagementBehavior;
import org.gatein.wsrp.test.protocol.v1.RegistrationBehavior;
import org.gatein.wsrp.test.protocol.v1.ServiceDescriptionBehavior;
import org.gatein.wsrp.test.protocol.v1.TestProducerBehavior;
import org.gatein.wsrp.test.protocol.v1.TestWSRPProducer;
import org.gatein.wsrp.test.protocol.v1.TestWSRPProducerImpl;
import org.gatein.wsrp.test.protocol.v1.behaviors.BasicMarkupBehavior;
import org.gatein.wsrp.test.protocol.v1.behaviors.BasicPortletManagementBehavior;
import org.gatein.wsrp.test.protocol.v1.behaviors.BasicServiceDescriptionBehavior;
import org.gatein.wsrp.test.protocol.v1.behaviors.EmptyMarkupBehavior;
import org.gatein.wsrp.test.protocol.v1.behaviors.InitCookieNotRequiredMarkupBehavior;
import org.gatein.wsrp.test.protocol.v1.behaviors.NullMarkupBehavior;
import org.gatein.wsrp.test.protocol.v1.behaviors.PerGroupInitCookieMarkupBehavior;
import org.gatein.wsrp.test.protocol.v1.behaviors.PerUserInitCookieMarkupBehavior;
import org.gatein.wsrp.test.protocol.v1.behaviors.ResourceMarkupBehavior;
import org.gatein.wsrp.test.protocol.v1.behaviors.SessionMarkupBehavior;
import org.gatein.wsrp.test.support.MockConsumerRegistry;
import org.gatein.wsrp.test.support.RequestedMarkupBehavior;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

/**
 * @author <a href="mailto:boleslaw.dawidowicz@jboss.org">Boleslaw Dawidowicz</a>
 * @version $Revision: 11344 $
 */
public abstract class WSRP1ConsumerBaseTest extends TestCase
{
   private static Logger log = LoggerFactory.getLogger(WSRP1ConsumerBaseTest.class);

   /** . */
   private static final String TEST_PRODUCER_ID = "test_producer";

   /** . */
   protected TestWSRPProducer producer = new TestWSRPProducerImpl();

   /** . */
   protected WSRPConsumerImpl consumer = new WSRPConsumerImpl(new ProducerInfo(new MockConsumerRegistry()));

   private boolean strict = true;

   public void setUp() throws Exception
   {
      // reset producer state
      producer.reset();

      // set the test producer identifier
      ProducerInfo producerInfo = consumer.getProducerInfo();
      producerInfo.setId(TEST_PRODUCER_ID);

      // reset the behaviors
      BehaviorRegistry registry = producer.getBehaviorRegistry();
      setServiceDescriptionBehavior(null);
      setPortletManagementBehavior(null);
      setRegistrationBehavior(null);
      registerAdditionalMarkupBehaviors(registry);

      // use a fresh endpoint using a behavior-backed service factory
      producerInfo.setEndpointConfigurationInfo(new EndpointConfigurationInfo(new BehaviorBackedServiceFactory(registry)));

      // todo: remove, this is a quick fix for GTNWSRP-156
      producerInfo.setExpirationCacheSeconds(null);

      // make sure we use clean producer info for each test
      consumer.refreshProducerInfo();

      // use cache to avoid un-necessary calls
      producerInfo.setExpirationCacheSeconds(120);

      // make sure we reset any held session information
      RequestHeaderClientHandler.resetCurrentInfo();
   }

   protected void setRegistrationBehavior(RegistrationBehavior behavior)
   {
      producer.getBehaviorRegistry().setRegistrationBehavior(behavior);
   }

   protected void setServiceDescriptionBehavior(ServiceDescriptionBehavior behavior)
   {
      if (behavior == null)
      {
         log.debug("Given service description behavior was null, using the default one instead!");
         behavior = new BasicServiceDescriptionBehavior();
      }

      producer.getBehaviorRegistry().setServiceDescriptionBehavior(behavior);
   }

   protected void setPortletManagementBehavior(PortletManagementBehavior behavior)
   {
      BehaviorRegistry registry = producer.getBehaviorRegistry();

      if (behavior == null)
      {
         log.debug("Given portlet management behavior was null, using the default one instead!");
         behavior = new BasicPortletManagementBehavior(registry);
      }

      registry.setPortletManagementBehavior(behavior);
   }

   public void testProducerId()
   {
      ExtendedAssert.assertEquals(TEST_PRODUCER_ID, consumer.getProducerId());
   }

   public void setStrict(boolean strict)
   {
      this.strict = strict;
      producer.usingStrictModeChangedTo(strict);
   }

   public boolean isStrict()
   {
      return strict;
   }

   private <T extends TestProducerBehavior> T createBehavior(String behaviorClassName, Class<T> expectedBehaviorClass)
   {
      if (behaviorClassName != null)
      {
         try
         {
            Class behaviorClass = getClass().getClassLoader().loadClass(behaviorClassName);
            if (expectedBehaviorClass.isAssignableFrom(behaviorClass))
            {
               return expectedBehaviorClass.cast(behaviorClass.newInstance());
            }
            else
            {
               throw new IllegalArgumentException(behaviorClassName + " is not a " + expectedBehaviorClass.getSimpleName());
            }
         }
         catch (ClassNotFoundException e)
         {
            throw new IllegalArgumentException("Could not find behavior: " + behaviorClassName, e);
         }
         catch (IllegalAccessException e)
         {
            throw new IllegalArgumentException("Could not access behavior: " + behaviorClassName, e);
         }
         catch (InstantiationException e)
         {
            throw new IllegalArgumentException("Could not instantiate behavior: " + behaviorClassName, e);
         }
      }

      return null;
   }

   protected void registerAdditionalMarkupBehaviors(BehaviorRegistry registry)
   {
      registry.registerMarkupBehavior(new BasicMarkupBehavior(registry));
      registry.registerMarkupBehavior(new EmptyMarkupBehavior(registry));
      registry.registerMarkupBehavior(new InitCookieNotRequiredMarkupBehavior(registry));
      registry.registerMarkupBehavior(new PerGroupInitCookieMarkupBehavior(registry));
      registry.registerMarkupBehavior(new PerUserInitCookieMarkupBehavior(registry));
      registry.registerMarkupBehavior(new NullMarkupBehavior(registry));
      registry.registerMarkupBehavior(new SessionMarkupBehavior(registry));
      registry.registerMarkupBehavior(new ResourceMarkupBehavior(registry));
   }

   protected Set<String> getPortletHandles()
   {
      return producer.getBehaviorRegistry().getServiceDescriptionBehavior().getPortletHandles();
   }

   protected int getPortletNumber()
   {
      return producer.getBehaviorRegistry().getServiceDescriptionBehavior().getPortletNumber();
   }

   @Override
   protected void tearDown() throws Exception
   {
      // reset the requested markup behavior
      RequestedMarkupBehavior.setRequestedMarkupBehavior(null);
   }
}
