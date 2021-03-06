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

package org.gatein.wsrp.test.protocol.v2.behaviors;

import org.gatein.pc.api.Mode;
import org.gatein.pc.api.WindowState;
import org.gatein.wsrp.test.protocol.v2.BehaviorRegistry;
import org.gatein.wsrp.test.protocol.v2.MarkupBehavior;
import org.oasis.wsrp.v2.GetMarkup;

/**
 * @author <a href="mailto:chris.laprun@jboss.com?subject=org.gatein.wsrp.v1.consumer.producer.EmptyMarkupBehavior">Chris
 *         Laprun</a>
 * @version $Revision: 8784 $
 * @since 2.6
 */
public class EmptyMarkupBehavior extends MarkupBehavior
{
   public static final String PORTLET_HANDLE = "EmptyMarkup";


   public EmptyMarkupBehavior(BehaviorRegistry registry)
   {
      super(registry);
      registerHandle(PORTLET_HANDLE);
   }

   public String getMarkupString(Mode mode, WindowState windowState, String navigationalState, GetMarkup getMarkup)
   {
      return "";
   }
}
