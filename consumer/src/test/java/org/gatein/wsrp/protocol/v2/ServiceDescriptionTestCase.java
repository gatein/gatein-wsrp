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

package org.gatein.wsrp.protocol.v2;

import org.gatein.pc.api.Portlet;
import org.gatein.pc.api.PortletContext;
import org.gatein.pc.api.PortletInvokerException;
import org.gatein.wsrp.consumer.ProducerInfo;
import org.gatein.wsrp.test.ExtendedAssert;
import org.gatein.wsrp.test.protocol.v1.behaviors.BasicMarkupBehavior;
import org.gatein.wsrp.test.protocol.v1.behaviors.SessionMarkupBehavior;
import org.gatein.wsrp.test.protocol.v2.BehaviorRegistry;
import org.gatein.wsrp.test.protocol.v2.ServiceDescriptionBehavior;
import org.gatein.wsrp.test.protocol.v2.behaviors.GroupedPortletsServiceDescriptionBehavior;
import org.oasis.wsrp.v2.CookieProtocol;
import org.oasis.wsrp.v2.EventDescription;
import org.oasis.wsrp.v2.ExportDescription;
import org.oasis.wsrp.v2.Extension;
import org.oasis.wsrp.v2.InvalidRegistration;
import org.oasis.wsrp.v2.ItemDescription;
import org.oasis.wsrp.v2.ModelDescription;
import org.oasis.wsrp.v2.ModelTypes;
import org.oasis.wsrp.v2.ModifyRegistrationRequired;
import org.oasis.wsrp.v2.OperationFailed;
import org.oasis.wsrp.v2.PortletDescription;
import org.oasis.wsrp.v2.ResourceList;
import org.oasis.wsrp.v2.ResourceSuspended;

import javax.xml.ws.Holder;
import java.util.List;

/**
 * @author <a href="mailto:boleslaw.dawidowicz@jboss.org">Boleslaw Dawidowicz</a>
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 11320 $
 * @since 2.4
 */
public class ServiceDescriptionTestCase extends InteropServiceDescriptionTestCase
{

   public ServiceDescriptionTestCase() throws Exception
   {
      super();
   }


   @Override
   public void setUp() throws Exception
   {
      super.setUp();

      // use strict mode
      setStrict(true);
   }

   public void testUsesRelaxedMode()
   {
      ExtendedAssert.assertTrue(isStrict());
   }

   public void testGetPortlet() throws Exception
   {
      //obtain one portlet
      Portlet portlet = consumer.getPortlet(PortletContext.createPortletContext(BasicMarkupBehavior.PORTLET_HANDLE));
      checkPortlet(portlet, "", BasicMarkupBehavior.PORTLET_HANDLE);

      portlet = consumer.getPortlet(PortletContext.createPortletContext(SessionMarkupBehavior.PORTLET_HANDLE));
      checkPortlet(portlet, "2", SessionMarkupBehavior.PORTLET_HANDLE);
   }

   public void testRequiresInitCookieIsProperlySetOnConsumerInitiatedRefresh() throws OperationFailed, ResourceSuspended, ModifyRegistrationRequired, InvalidRegistration, PortletInvokerException
   {
      BehaviorRegistry registry = producer.getBehaviorRegistry();

      Holder<List<PortletDescription>> offeredPortlets = new Holder<List<PortletDescription>>();
      registry.getServiceDescriptionBehavior().getServiceDescription(null, null, null, null, new Holder<Boolean>(),
         offeredPortlets, new Holder<List<ItemDescription>>(),
         null, new Holder<List<ItemDescription>>(), new Holder<List<ItemDescription>>(), new Holder<CookieProtocol>(), new Holder<ModelDescription>(), new Holder<List<String>>(),
         new Holder<ResourceList>(), new Holder<List<EventDescription>>(), new Holder<ModelTypes>(), new Holder<List<String>>(), new Holder<ExportDescription>(), new Holder<Boolean>(), new Holder<List<Extension>>());
      setServiceDescriptionBehavior(new GroupedPortletsServiceDescriptionBehavior(offeredPortlets.value));
      ServiceDescriptionBehavior sdb = new GroupedPortletsServiceDescriptionBehavior(offeredPortlets.value);
      sdb.setRequiresInitCookie(CookieProtocol.PER_GROUP);
      setServiceDescriptionBehavior(sdb);

      consumer.refreshProducerInfo();

      ProducerInfo producerInfo = consumer.getProducerInfo();
      assertEquals(CookieProtocol.PER_GROUP, producerInfo.getRequiresInitCookie());
      assertTrue(consumer.getSessionHandler().requiresInitCookie());
   }

   public void testRequiresInitCookieIsProperlySetOnProducerInfoInitiatedRefresh() throws OperationFailed, ResourceSuspended, ModifyRegistrationRequired, InvalidRegistration, PortletInvokerException
   {
      BehaviorRegistry registry = producer.getBehaviorRegistry();

      Holder<List<PortletDescription>> offeredPortlets = new Holder<List<PortletDescription>>();
      registry.getServiceDescriptionBehavior().getServiceDescription(null, null, null, null, new Holder<Boolean>(),
         offeredPortlets, new Holder<List<ItemDescription>>(),
         null, new Holder<List<ItemDescription>>(), new Holder<List<ItemDescription>>(), new Holder<CookieProtocol>(), new Holder<ModelDescription>(), new Holder<List<String>>(),
         new Holder<ResourceList>(), new Holder<List<EventDescription>>(), new Holder<ModelTypes>(), new Holder<List<String>>(), new Holder<ExportDescription>(), new Holder<Boolean>(), new Holder<List<Extension>>());
      setServiceDescriptionBehavior(new GroupedPortletsServiceDescriptionBehavior(offeredPortlets.value));
      ServiceDescriptionBehavior sdb = new GroupedPortletsServiceDescriptionBehavior(offeredPortlets.value);
      sdb.setRequiresInitCookie(CookieProtocol.PER_GROUP);
      setServiceDescriptionBehavior(sdb);

      consumer.getProducerInfo().refresh(true);

      ProducerInfo producerInfo = consumer.getProducerInfo();
      assertEquals(CookieProtocol.PER_GROUP, producerInfo.getRequiresInitCookie());

      // SessionHandler needs to be updated if ProducerInfo is refreshed
      assertTrue(consumer.getSessionHandler().requiresInitCookie());
   }
}
