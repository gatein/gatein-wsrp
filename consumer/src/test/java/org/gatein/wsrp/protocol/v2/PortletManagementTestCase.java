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
import org.gatein.pc.api.PortletStateType;
import org.gatein.pc.api.info.MetaInfo;
import org.gatein.pc.api.state.DestroyCloneFailure;
import org.gatein.pc.api.state.PropertyChange;
import org.gatein.pc.api.state.PropertyMap;
import org.gatein.wsrp.consumer.migration.ExportInfo;
import org.gatein.wsrp.test.ExtendedAssert;
import org.gatein.wsrp.test.protocol.v2.BehaviorRegistry;
import org.gatein.wsrp.test.protocol.v2.behaviors.BasicMarkupBehavior;
import org.gatein.wsrp.test.protocol.v2.behaviors.BasicPortletManagementBehavior;
import org.gatein.wsrp.test.protocol.v2.behaviors.DestroyClonesPortletManagementBehavior;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 8784 $
 * @since 2.6
 */
public class PortletManagementTestCase extends V2ConsumerBaseTest
{
   public PortletManagementTestCase() throws Exception
   {
   }

   public void testClone() throws Exception
   {
      PortletContext original = PortletContext.createPortletContext(BasicMarkupBehavior.PORTLET_HANDLE);
      PortletContext clone = consumer.createClone(PortletStateType.OPAQUE, original);
      ExtendedAssert.assertNotNull(clone);
      ExtendedAssert.assertFalse(original.equals(clone));
      ExtendedAssert.assertEquals(BasicPortletManagementBehavior.CLONED_HANDLE, clone.getId());

      Portlet originalPortlet = consumer.getPortlet(original);
      Portlet clonePortlet = consumer.getPortlet(clone);
      ExtendedAssert.assertNotNull(clonePortlet);
      ExtendedAssert.assertFalse(originalPortlet.getContext().equals(clonePortlet.getContext()));

      // information about the portlet should be the same
      MetaInfo originalInfo = originalPortlet.getInfo().getMeta();
      MetaInfo cloneInfo = clonePortlet.getInfo().getMeta();
      ExtendedAssert.assertEquals(originalInfo.getMetaValue(MetaInfo.TITLE), cloneInfo.getMetaValue(MetaInfo.TITLE));
      ExtendedAssert.assertEquals(originalInfo.getMetaValue(MetaInfo.DESCRIPTION), cloneInfo.getMetaValue(MetaInfo.DESCRIPTION));
   }

   public void testGetSetProperties() throws Exception
   {
      PortletContext original = PortletContext.createPortletContext(BasicMarkupBehavior.PORTLET_HANDLE);
      PropertyMap props = consumer.getProperties(original);
      checkProperties(props, BasicPortletManagementBehavior.PROPERTY_VALUE);

      PortletContext clone = consumer.createClone(PortletStateType.OPAQUE, original);
      props = consumer.getProperties(clone);
      checkProperties(props, BasicPortletManagementBehavior.PROPERTY_VALUE);

      consumer.setProperties(clone, new PropertyChange[]
         {
            PropertyChange.newUpdate(BasicPortletManagementBehavior.PROPERTY_NAME,
               BasicPortletManagementBehavior.PROPERTY_NEW_VALUE)
         });
      checkProperties(consumer.getProperties(clone), BasicPortletManagementBehavior.PROPERTY_NEW_VALUE);

      consumer.setProperties(clone, new PropertyChange[]
         {
            PropertyChange.newReset(BasicPortletManagementBehavior.PROPERTY_NAME)
         });
      checkProperties(consumer.getProperties(clone), BasicPortletManagementBehavior.PROPERTY_VALUE);
   }

   public void testSetResetSameProperty() throws PortletInvokerException
   {
      PortletContext original = PortletContext.createPortletContext(BasicMarkupBehavior.PORTLET_HANDLE);
      PortletContext clone = consumer.createClone(PortletStateType.OPAQUE, original);

      try
      {
         consumer.setProperties(clone, new PropertyChange[]
            {
               PropertyChange.newUpdate(BasicPortletManagementBehavior.PROPERTY_NAME,
                  BasicPortletManagementBehavior.PROPERTY_NEW_VALUE),
               PropertyChange.newReset(BasicPortletManagementBehavior.PROPERTY_NAME)
            });
         fail("Shouldn't be possible to set and reset the same property in the same call");
      }
      catch (Exception e)
      {
         assertTrue(e.getCause().getLocalizedMessage().contains(BasicPortletManagementBehavior.CANNOT_BOTH_SET_AND_RESET_A_PROPERTY_AT_THE_SAME_TIME));
      }
   }

   private void checkProperties(PropertyMap props, String expectedValue)
   {
      ExtendedAssert.assertNotNull(props);
      ExtendedAssert.assertEquals(1, props.size());
      ExtendedAssert.assertEquals(expectedValue, props.getProperty(BasicPortletManagementBehavior.PROPERTY_NAME).get(0));
   }

   public void testDestroyClones() throws Exception
   {
      // switch the behavior for portlet management
      BehaviorRegistry behaviorRegistry = producer.getBehaviorRegistry();
      behaviorRegistry.setPortletManagementBehavior(new DestroyClonesPortletManagementBehavior(behaviorRegistry));

      PortletContext original = PortletContext.createPortletContext(BasicMarkupBehavior.PORTLET_HANDLE);
      PortletContext clone = consumer.createClone(PortletStateType.OPAQUE, original);
      ExtendedAssert.assertNotNull(clone);
      Portlet portlet = consumer.getPortlet(clone);
      ExtendedAssert.assertNotNull(portlet);
      ExtendedAssert.assertEquals(BasicPortletManagementBehavior.CLONED_HANDLE, portlet.getContext().getId());

      List clones = new ArrayList(1);
      clones.add(clone);
      List result = consumer.destroyClones(clones);
      ExtendedAssert.assertTrue(result.isEmpty());
      try
      {
         consumer.getPortlet(clone);
         ExtendedAssert.fail("Should have failed: clone should not exist anymore!");
      }
      catch (PortletInvokerException expected)
      {
      }

      // re-create clone and try again with an added invalid portlet context
      clone = consumer.createClone(PortletStateType.OPAQUE, original);
      PortletContext invalidContext = PortletContext.createPortletContext("invalid");
      clones.add(invalidContext);
      result = consumer.destroyClones(clones);
      ExtendedAssert.assertEquals(1, result.size());
      DestroyCloneFailure failure = (DestroyCloneFailure)result.get(0);
      ExtendedAssert.assertEquals("invalid", failure.getPortletId());
      try
      {
         consumer.getPortlet(clone);
         ExtendedAssert.fail("Should have failed: clone should not exist anymore!");
      }
      catch (PortletInvokerException expected)
      {
      }
   }

   public void testInvalidSetProperties() throws Exception
   {
      PortletContext original = PortletContext.createPortletContext(BasicMarkupBehavior.PORTLET_HANDLE);
      try
      {
         consumer.setProperties(original, null);
         ExtendedAssert.fail("Shouldn't be possible to set properties with null changes");
      }
      catch (IllegalArgumentException expected)
      {
         //expected
      }
   }

   public void testNullExportPortlets() throws PortletInvokerException
   {
      try
      {
         consumer.exportPortlets(null);
         fail("Cannot export without a list of portlet handles!");
      }
      catch (IllegalArgumentException e)
      {
         // expected
      }
   }

   public void testEmptyExportPortlets() throws PortletInvokerException
   {
      try
      {
         consumer.exportPortlets(Collections.<String>emptyList());
         fail("Cannot export without a list of portlet handles!");
      }
      catch (IllegalArgumentException e)
      {
         // expected
      }
   }

   public void testExportPortlets() throws PortletInvokerException
   {
      long now = System.currentTimeMillis();
      ArrayList<String> portletContexts = new ArrayList<String>();
      portletContexts.add(BasicMarkupBehavior.PORTLET_HANDLE);
      ExportInfo ei = consumer.exportPortlets(portletContexts);
      assertNotNull(ei);

      long tenSeconds = 10 * 1000;
      assertTrue((ei.getExportTime() - now) < tenSeconds);

      assertEquals(0, ei.getExpirationTime());

      List<String> exportedPortlets = ei.getExportedPortletHandles();
      assertNotNull(exportedPortlets);
      assertEquals(1, exportedPortlets.size());
      assertTrue(exportedPortlets.contains(BasicMarkupBehavior.PORTLET_HANDLE));
      assertNotNull(ei.getPortletStateFor(BasicMarkupBehavior.PORTLET_HANDLE));

      assertTrue(ei.getErrorCodesToFailedPortletHandlesMapping().isEmpty());

      List<ExportInfo> availableExportInfos = consumer.getMigrationService().getAvailableExportInfos();
      assertEquals(1, availableExportInfos.size());
      assertEquals(ei, availableExportInfos.get(0));
   }
}
