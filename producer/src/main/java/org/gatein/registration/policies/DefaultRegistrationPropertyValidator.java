/*
 * JBoss, a division of Red Hat
 * Copyright 2011, Red Hat Middleware, LLC, and individual
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

package org.gatein.registration.policies;

import javax.xml.namespace.QName;

/**
 * Provides default implementation of RegistrationPropertyValidator that only checks that values are non-empty Strings.
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 8784 $
 * @since 2.6
 */
public class DefaultRegistrationPropertyValidator implements RegistrationPropertyValidator
{
   public void validateValueFor(QName propertyName, Object value) throws IllegalArgumentException
   {
      // accepts only non-null, non-empty String values
      if (!(value instanceof String) || ((String)value).length() <= 0)
      {
         throw new IllegalArgumentException(value + " is not a valid value for property '" + propertyName +
            "'. DefaultRegistrationPropertyValidator only accepts non-null, non-empty Strings.");
      }
   }

   public static final RegistrationPropertyValidator DEFAULT = new DefaultRegistrationPropertyValidator();
}
