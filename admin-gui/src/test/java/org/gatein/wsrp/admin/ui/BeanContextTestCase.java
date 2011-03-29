/*
* JBoss, a division of Red Hat
* Copyright 2008, Red Hat Middleware, LLC, and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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

package org.gatein.wsrp.admin.ui;

import junit.framework.TestCase;

import java.util.Locale;
import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class BeanContextTestCase extends TestCase
{
   public void testMessageFormatting()
   {
      assertEquals("foo value", BeanContext.getLocalizedMessage("foo", Locale.getDefault()));
      assertEquals("foo value", BeanContext.getLocalizedMessage("foo", Locale.getDefault(), "Resource", "blah"));
      assertEquals("param value: foo", BeanContext.getLocalizedMessage("1param", Locale.getDefault(), "Resource", "foo"));
      assertEquals("param1 value: foo param2 value: bar", BeanContext.getLocalizedMessage("2params", Locale.getDefault(), "Resource", "foo", "bar"));
   }

   public void testErrorMessage()
   {
      TestBeanContext context = new TestBeanContext();
      context.createErrorMessage("1param", "error");
      assertEquals("param value: error", context.getMessage());
      assertEquals(TestBeanContext.ERROR, context.getSeverity());
   }

   private static class TestBeanContext extends BeanContext
   {
      private static final String ERROR = "ERROR";
      private static final String INFO = "INFO";
      private String message;
      private Object severity;
      private Object[] params;

      public String getMessage()
      {
         return message;
      }

      public Object getSeverity()
      {
         return severity;
      }

      public Object[] getParams()
      {
         return params;
      }

      @Override
      public String getParameter(String key)
      {
         throw new UnsupportedOperationException();
      }

      @Override
      protected void createMessage(String target, String message, Object severity, Object... additionalParams)
      {
         this.message = message;
         this.severity = severity;
         this.params = additionalParams;
      }

      @Override
      protected Object getErrorSeverity()
      {
         return ERROR;
      }

      @Override
      protected Object getInfoSeverity()
      {
         return INFO;
      }

      @Override
      protected Locale getLocale()
      {
         return Locale.getDefault();
      }

      @Override
      public String getServerAddress()
      {
         throw new UnsupportedOperationException();
      }

      @Override
      public Map<String, Object> getSessionMap()
      {
         throw new UnsupportedOperationException();
      }
   }
}
