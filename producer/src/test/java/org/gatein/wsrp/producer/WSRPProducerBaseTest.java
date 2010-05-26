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

package org.gatein.wsrp.producer;

import junit.framework.TestCase;
import org.gatein.common.NotYetImplemented;

/**
 * @author <a href="mailto:boleslaw.dawidowicz@jboss.org">Boleslaw Dawidowicz</a>
 * @version $Revision: 8808 $
 */
public abstract class WSRPProducerBaseTest extends TestCase
{
   protected WSRPProducerImpl producer = WSRPProducerImpl.getInstance();

   protected WSRPProducerBaseTest(String name) throws Exception
   {
      super(name);
   }

   public void deploy(String warFileName) throws Exception
   {
      throw new NotYetImplemented("deploy");
   }

   public void undeploy(String warFileName) throws Exception
   {
      throw new NotYetImplemented("undeploy");
   }
}
