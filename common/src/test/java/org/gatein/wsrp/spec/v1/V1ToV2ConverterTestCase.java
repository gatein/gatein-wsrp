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

import org.gatein.wsrp.test.ExtendedAssert;
import org.oasis.wsrp.v1.V1MarkupContext;
import org.oasis.wsrp.v1.V1OperationFailed;
import org.oasis.wsrp.v1.V1OperationFailedFault;
import org.oasis.wsrp.v2.InvalidSession;
import org.oasis.wsrp.v2.MarkupContext;
import org.oasis.wsrp.v2.OperationFailed;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class V1ToV2ConverterTestCase extends TestCase
{
   public void testToV2Exception() throws Exception
   {
      Throwable throwable = new Throwable();
      V1OperationFailed v1OF = new V1OperationFailed("foo", new V1OperationFailedFault(), throwable);
      OperationFailed operationFailed = V1ToV2Converter.toV2Exception(OperationFailed.class, v1OF);
      assertNotNull(operationFailed);
      assertEquals("foo", operationFailed.getMessage());
      assertEquals(throwable, operationFailed.getCause());
   }

   public void testToV2ExceptionMismatch()
   {
      Throwable throwable = new Throwable();
      V1OperationFailed v1OF = new V1OperationFailed("foo", new V1OperationFailedFault(), throwable);

      try
      {
         V1ToV2Converter.toV2Exception(InvalidSession.class, v1OF);
         fail("Should have failed as requested v2 exception doesn't match specified v1");
      }
      catch (IllegalArgumentException e)
      {
         // expected
      }
   }

   public void testToV2ExceptionWrongRequestedException()
   {
      Throwable throwable = new Throwable();
      V1OperationFailed v1OF = new V1OperationFailed("foo", new V1OperationFailedFault(), throwable);

      try
      {
         V1ToV2Converter.toV2Exception(IllegalArgumentException.class, v1OF);
         fail("Should have failed as requested exception is not a WSRP 2 exception class");
      }
      catch (IllegalArgumentException e)
      {
         // expected
      }
   }

   public void testToV2ExceptionWrongV1Exception()
   {
      try
      {
         V1ToV2Converter.toV2Exception(OperationFailed.class, new IllegalArgumentException());
         fail("Should have failed as specified exception is not a WSRP 1 exception");
      }
      catch (IllegalArgumentException e)
      {
         // expected
      }
   }
   
   public void testToV2MarkupContext()
   {
      //test with mimetype and markupstring
      V1MarkupContext v1MarkupContext = new V1MarkupContext();
      v1MarkupContext.setMarkupString("test_1");
      v1MarkupContext.setMimeType("mimeType_1");
      MarkupContext markupContext = V1ToV2Converter.toV2MarkupContext(v1MarkupContext);
      assertEquals("test_1", markupContext.getItemString());
      assertEquals("mimeType_1", markupContext.getMimeType());
      assertNull(markupContext.getItemBinary());
      
      //test without the mimetype
      v1MarkupContext.setMimeType(null);
      try
      {
         V1ToV2Converter.toV2MarkupContext(v1MarkupContext);
         ExtendedAssert.fail();
      }
      catch (IllegalArgumentException e)
      {
         //expected
      }
      
      //test with mimetype and markupbinary
      v1MarkupContext = new V1MarkupContext();
      v1MarkupContext.setMarkupBinary(new byte[]{1,2,3});
      v1MarkupContext.setMimeType("mimeType_2");
      markupContext = V1ToV2Converter.toV2MarkupContext(v1MarkupContext);
      ExtendedAssert.assertEquals(new byte[]{1,2,3}, markupContext.getItemBinary());
      assertEquals("mimeType_2", markupContext.getMimeType());
      assertNull(markupContext.getItemString());
      
      //test without the mimetype
      v1MarkupContext.setMimeType(null);
      try
      {
         V1ToV2Converter.toV2MarkupContext(v1MarkupContext);
         fail();
      }
      catch (IllegalArgumentException e)
      {
         //expected
      }
      
      //test with useCachedMarkup
      v1MarkupContext = new V1MarkupContext();
      v1MarkupContext.setUseCachedMarkup(Boolean.TRUE);
      markupContext = V1ToV2Converter.toV2MarkupContext(v1MarkupContext);
      assertNull(markupContext.getItemString());
      assertNull(markupContext.getItemBinary());
      assertTrue(markupContext.isUseCachedItem().booleanValue());
      
      v1MarkupContext = new V1MarkupContext();
      v1MarkupContext.setUseCachedMarkup(Boolean.FALSE);
      try
      {
         V1ToV2Converter.toV2MarkupContext(v1MarkupContext);
         fail();
      }
      catch (IllegalArgumentException e)
      {
         //expected
      }
   }
}
