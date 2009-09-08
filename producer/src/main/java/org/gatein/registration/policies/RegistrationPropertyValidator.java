/*
 * JBoss, a division of Red Hat
 * Copyright 2009, Red Hat Middleware, LLC, and individual
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
 * An interface providing an entry point for WSRP deployers to plug their registration property validation mechanism in
 * {@link org.jboss.portal.registration.policies.DefaultRegistrationPolicy}. Implementations of this interface
 * <strong>MUST</strong> provide a no-argument constructor for instantiation from the class name.
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 8784 $
 * @since 2.6
 */
public interface RegistrationPropertyValidator
{
   /**
    * Validates the given value for the registration property identified by the specified name. If the value is
    * acceptable, the method simply returns. An invalid value will raise an exception.
    *
    * @param propertyName the qualified name for the property name
    * @param value        the value that needs to be validated
    * @throws IllegalArgumentException if the specified value is not acceptable for the specified property.
    */
   void validateValueFor(QName propertyName, Object value) throws IllegalArgumentException;
}
