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

package org.gatein.wsrp.spec.v1;

import junit.framework.TestCase;

import org.gatein.wsrp.WSRPTypeFactory;
import org.oasis.wsrp.v1.V1InvalidSession;
import org.oasis.wsrp.v1.V1OperationFailed;
import org.oasis.wsrp.v2.OperationFailed;
import org.oasis.wsrp.v2.OperationFailedFault;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class V2ToV1ConverterTestCase extends TestCase
{
   public void testException() throws Exception
   {
      Throwable throwable = new Throwable();
      OperationFailed operationFailed = new OperationFailed("foo", WSRPTypeFactory.createOperationFailedFault(), throwable);
      V1OperationFailed v1OperationFailed = V2ToV1Converter.toV1Exception(V1OperationFailed.class, operationFailed);
      assertNotNull(v1OperationFailed);
      assertEquals("foo", v1OperationFailed.getMessage());
      assertEquals(throwable, v1OperationFailed.getCause());
   }

   public void testExceptionMismatch()
   {
      Throwable throwable = new Throwable();
      OperationFailed operationFailed = new OperationFailed("foo", WSRPTypeFactory.createOperationFailedFault(), throwable);

      try
      {
         V2ToV1Converter.toV1Exception(V1InvalidSession.class, operationFailed);
         fail("Should have failed as requested v1 exception doesn't match specified v2");
      }
      catch (IllegalArgumentException e)
      {
         // expected
      }
   }

   public void testExceptionWrongRequestedException()
   {
      Throwable throwable = new Throwable();
      OperationFailed operationFailed = new OperationFailed("foo", WSRPTypeFactory.createOperationFailedFault(), throwable);

      try
      {
         V2ToV1Converter.toV1Exception(IllegalArgumentException.class, operationFailed);
         fail("Should have failed as requested exception is not a WSRP 1 exception class");
      }
      catch (IllegalArgumentException e)
      {
         // expected
      }
   }

   public void testExceptionWrongException()
   {
      try
      {
         V2ToV1Converter.toV1Exception(V1OperationFailed.class, new IllegalArgumentException());
         fail("Should have failed as specified exception is not a WSRP 1 exception");
      }
      catch (IllegalArgumentException e)
      {
         // expected
      }
   }

}
