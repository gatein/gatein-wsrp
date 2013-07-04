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

package org.gatein.registration;

import org.gatein.wsrp.registration.PropertyDescription;

import javax.xml.namespace.QName;
import java.util.Map;

/**
 * Allows to react to changes in a set of registration properties in the producer's configuration.
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 9177 $
 * @since 2.6
 */
public interface RegistrationPropertyChangeListener
{
   /**
    * Called whenever registration properties have been changed in the producer's configuration to the specified new propertiy descriptions.
    *
    * @param newRegistrationProperties a Map of the new registration property descriptions, associating the property QName to its description
    */
   void propertiesHaveChanged(Map<QName, ? extends PropertyDescription> newRegistrationProperties);
}
