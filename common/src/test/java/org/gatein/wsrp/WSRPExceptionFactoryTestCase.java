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

package org.gatein.wsrp;

import junit.framework.TestCase;
import org.gatein.wsrp.spec.v1.WSRP1ExceptionFactory;
import org.gatein.wsrp.spec.v2.WSRP2ExceptionFactory;
import org.oasis.wsrp.v1.V1OperationFailed;
import org.oasis.wsrp.v1.V1OperationFailedFault;
import org.oasis.wsrp.v2.OperationFailed;
import org.oasis.wsrp.v2.OperationFailedFault;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class WSRPExceptionFactoryTestCase extends TestCase
{
   public void testCreateWSExceptionV1() throws Exception
   {
      Throwable throwable = new Throwable();
      V1OperationFailed operationFailed = WSRP1ExceptionFactory.createWSException(V1OperationFailed.class, "foo", throwable);
      assertNotNull(operationFailed);
      assertEquals("foo", operationFailed.getMessage());
      assertNotNull(operationFailed.getFaultInfo());
      assertEquals(V1OperationFailedFault.class, operationFailed.getFaultInfo().getClass());
      assertEquals(throwable, operationFailed.getCause());

      try
      {
         WSRP1ExceptionFactory.createWSException(Exception.class, "foo", null);
         fail("Should have failed because specified exception is not a WSRP one");
      }
      catch (IllegalArgumentException e)
      {
         // expected
      }

/*
// todo: ideally, we should be able to prevent WSRP1ExceptionFactory to create v2 exceptions... not sure how to do it right now
      try
      {
         WSRP1ExceptionFactory.createWSException(OperationFailed.class, "foo", null);
         fail("Should have failed because specified exception is not a WSRP 1 exception");
      }
      catch (IllegalArgumentException e)
      {
         // expected
      }
*/
   }

   public void testCreateWSExceptionV2() throws Exception
   {
      Throwable throwable = new Throwable();
      OperationFailed operationFailed = WSRP2ExceptionFactory.createWSException(OperationFailed.class, "foo", throwable);
      assertNotNull(operationFailed);
      assertEquals("foo", operationFailed.getMessage());
      assertNotNull(operationFailed.getFaultInfo());
      assertEquals(OperationFailedFault.class, operationFailed.getFaultInfo().getClass());
      assertEquals(throwable, operationFailed.getCause());

      try
      {
         WSRP2ExceptionFactory.createWSException(Exception.class, "foo", null);
         fail("Should have failed because specified exception is not a WSRP one");
      }
      catch (IllegalArgumentException e)
      {
         // expected
      }
   }
}
