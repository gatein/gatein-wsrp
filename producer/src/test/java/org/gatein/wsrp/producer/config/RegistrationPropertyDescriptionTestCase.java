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

package org.gatein.wsrp.producer.config;

import junit.framework.TestCase;
import org.gatein.wsrp.WSRPConstants;
import org.gatein.wsrp.registration.RegistrationPropertyDescription;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class RegistrationPropertyDescriptionTestCase extends TestCase
{
   public void testEquals()
   {
      RegistrationPropertyDescription foo1 = new RegistrationPropertyDescription("foo", WSRPConstants.XSD_STRING);
      RegistrationPropertyDescription foo2 = new RegistrationPropertyDescription("foo", WSRPConstants.XSD_STRING);

      assertEquals(foo1, foo2);

      assertEquals(foo1, new RegistrationPropertyDescription(foo1));
   }
}
