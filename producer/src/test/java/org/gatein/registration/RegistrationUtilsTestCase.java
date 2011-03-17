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

package org.gatein.registration;

import junit.framework.TestCase;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class RegistrationUtilsTestCase extends TestCase
{
   public void testValidateConsumerAgentStrict()
   {
      RegistrationUtils.setStrict(true);

      RegistrationUtils.validateConsumerAgent("foo.bar.baz");
      RegistrationUtils.validateConsumerAgent("foo.bar.baz.");
      RegistrationUtils.validateConsumerAgent("foo.bar.baz. ");
      RegistrationUtils.validateConsumerAgent("foo.bar.baz. dasfsafads");

      checkValidateProperlyRejects("goo");
      checkValidateProperlyRejects("goo.");
      checkValidateProperlyRejects("goo.boo");
      checkValidateProperlyRejects("goo.boo.");
      checkValidateProperlyRejects("goo.boo. ");
   }

   private void checkValidateProperlyRejects(String consumerAgent)
   {
      try
      {
         RegistrationUtils.validateConsumerAgent(consumerAgent);
         fail("Should have rejected '" + consumerAgent + "' as an invalid Consumer Agent string");
      }
      catch (IllegalArgumentException e)
      {
         // expected
      }
   }

   public void testValidateConsumerAgentLenient()
   {
      RegistrationUtils.setStrict(false);

      RegistrationUtils.validateConsumerAgent("foo.bar.baz");

      RegistrationUtils.validateConsumerAgent("goo");
      RegistrationUtils.validateConsumerAgent("goo.");
      RegistrationUtils.validateConsumerAgent("goo.boo");
      RegistrationUtils.validateConsumerAgent("goo.boo.");
      RegistrationUtils.validateConsumerAgent("goo.boo. ");
   }
}
