/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2009, Red Hat Middleware, LLC, and individual                    *
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
package org.gatein.wsrp.test;

import junit.framework.Assert;

import java.util.Arrays;
import java.util.List;

/**
 * Add more assert methods.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 7374 $
 */
public class ExtendedAssert extends Assert
{

   /** @see #assertEquals(Object[],Object[]) */
   public static void assertEquals(Object[] expected, Object[] actual)
   {
      assertEquals(null, expected, actual);
   }

   /** Test equality as defined by java.util.Array#equals(Object[], Object[]). */
   public static void assertEquals(String message, Object[] expected, Object[] actual)
   {
      if (Arrays.equals(expected, actual))
      {
         return;
      }
      fail(format(message, expected, actual));
   }

   /** @see #assertEquals(char[],char[]) */
   public static void assertEquals(char[] expected, char[] actual)
   {
      assertEquals(null, expected, actual);
   }

   /** Test equality as defined by java.util.Array#equals(char[], char[]). */
   public static void assertEquals(String message, char[] expected, char[] actual)
   {
      if (Arrays.equals(expected, actual))
      {
         return;
      }
      fail(format(message, expected, actual));
   }

   /** @see #assertEquals(byte[],byte[]) */
   public static void assertEquals(byte[] expected, byte[] actual)
   {
      assertEquals(null, expected, actual);
   }

   /** Test equality as defined by java.util.Array#equals(char[], char[]). */
   public static void assertEquals(String message, byte[] expected, byte[] actual)
   {
      if (Arrays.equals(expected, actual))
      {
         return;
      }

      //
      fail(format(message, toString(expected), toString(actual)));
   }

   private static String toString(byte[] expected)
   {
      StringBuffer expectedBuffer = new StringBuffer("[");

      //
      for (byte expectedByte : expected)
      {
         expectedBuffer.append(expectedByte).append(',');
      }

      //
      if (expectedBuffer.length() == 1)
      {
         expectedBuffer.append(']');
      }
      else
      {
         expectedBuffer.setCharAt(expectedBuffer.length(), ']');
      }

      //
      return expectedBuffer.toString();
   }

   private static String format(String message, Object expected, Object actual)
   {
      String formatted = "";
      if (message != null)
      {
         formatted = message + " ";
      }
      return formatted + "expected:<" + format(expected) + "> but was:<" + format(actual) + ">";
   }

   private static String format(Object o)
   {
      if (o instanceof Object[])
      {
         Object[] array = (Object[])o;
         StringBuffer buffer = new StringBuffer("[");
         for (int i = 0; i < array.length; i++)
         {
            buffer.append(i == 0 ? "" : ",").append(String.valueOf(array[i]));
         }
         buffer.append("]");
         return buffer.toString();
      }
      else
      {
         return String.valueOf(o);
      }
   }

   public static void assertEquals(Object[] expected, Object[] tested, boolean isOrderRelevant, String failMessage)
   {
      if (isOrderRelevant)
      {
         if (!Arrays.equals(expected, tested))
         {
            fail(failMessage);
         }
      }
      else
      {
         boolean equals = (expected == tested);

         if (!equals)
         {
            if (expected == null || tested == null)
            {
               fail(failMessage + " Not both null.");
            }

            if (expected.getClass().getComponentType() != tested.getClass().getComponentType())
            {
               fail(failMessage + " Different classes.");
            }

            if (expected.length != tested.length)
            {
               fail(failMessage + " Different sizes (tested: " + tested.length + ", expected: " + expected.length + ").");
            }

            List expectedList = Arrays.asList(expected);
            List testedList = Arrays.asList(tested);
            if (!expectedList.containsAll(testedList))
            {
               fail(failMessage);
            }
         }
      }
   }

   public static void assertEquals(Object[] expected, Object[] tested, boolean isOrderRelevant, String failMessage, Decorator decorator)
   {
      Object[] decoratedExpected = null, decoratedTested = null;
      if (decorator != null)
      {
         decoratedExpected = decorate(expected, decorator);
         decoratedTested = decorate(tested, decorator);
      }

      assertEquals(decoratedExpected, decoratedTested, isOrderRelevant, failMessage);
   }

   public static Object[] decorate(Object[] toBeDecorated, Decorator decorator)
   {
      if (toBeDecorated != null)
      {
         DecoratedObject[] decorated = new DecoratedObject[toBeDecorated.length];
         for (int i = 0; i < decorated.length; i++)
         {
            decorated[i] = new DecoratedObject(toBeDecorated[i], decorator);
         }
         return decorated;
      }
      return null;

   }

   public static void assertString1ContainsString2(String string1, String string2)
   {
      assertTrue("<" + string1 + "> does not contain <" + string2 + ">", string1.indexOf(string2) >= 0);
   }

   public static interface Decorator
   {
      void decorate(Object decorated);
   }

   public static class DecoratedObject
   {
      private Decorator decorator;
      private Object decorated;

      public Object getDecorated()
      {
         return decorated;
      }

      public DecoratedObject(Object decorated, Decorator decorator)
      {
         this.decorator = decorator;
         this.decorated = decorated;
      }

      public boolean equals(Object obj)
      {
         decorator.decorate(decorated);
         return decorator.equals(obj);
      }

      public String toString()
      {
         decorator.decorate(decorated);
         return decorator.toString();
      }
   }
}