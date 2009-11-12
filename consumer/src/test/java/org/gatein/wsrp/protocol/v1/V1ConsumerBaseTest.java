/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2006, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/
package org.gatein.wsrp.protocol.v1;

import org.gatein.pc.api.Portlet;
import org.gatein.pc.api.info.MetaInfo;
import org.gatein.wsrp.consumer.WSRPConsumerBaseTest;
import org.gatein.wsrp.test.ExtendedAssert;
import org.gatein.wsrp.test.protocol.v1.TestProducerBehavior;

/**
 * @author <a href="mailto:boleslaw.dawidowicz@jboss.org">Boleslaw Dawidowicz</a>
 * @version $Revision: 11320 $
 */
public class V1ConsumerBaseTest extends WSRPConsumerBaseTest
{
   public V1ConsumerBaseTest() throws Exception
   {
   }

   protected void checkPortlet(Portlet portlet, String suffix, String handle)
   {
      MetaInfo meta = portlet.getInfo().getMeta();
      ExtendedAssert.assertEquals(handle, portlet.getContext().getId());
      ExtendedAssert.assertEquals(TestProducerBehavior.SAMPLE_DESCRIPTION + suffix, TestProducerBehavior.extractString(meta.getMetaValue(MetaInfo.DESCRIPTION)));
      ExtendedAssert.assertEquals(TestProducerBehavior.SAMPLE_TITLE + suffix, TestProducerBehavior.extractString(meta.getMetaValue(MetaInfo.TITLE)));
      ExtendedAssert.assertEquals(TestProducerBehavior.SAMPLE_SHORTTITLE + suffix, TestProducerBehavior.extractString(meta.getMetaValue(MetaInfo.SHORT_TITLE)));
      ExtendedAssert.assertEquals(TestProducerBehavior.SAMPLE_DISPLAYNAME + suffix, TestProducerBehavior.extractString(meta.getMetaValue(MetaInfo.DISPLAY_NAME)));
      ExtendedAssert.assertEquals(TestProducerBehavior.SAMPLE_KEYWORD + suffix, TestProducerBehavior.extractString(meta.getMetaValue(MetaInfo.KEYWORDS)));
   }


   public void setUp() throws Exception
   {
      super.setUp();
   }
}
