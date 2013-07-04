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

package org.gatein.registration;

import org.gatein.pc.api.PortletContext;

import javax.xml.namespace.QName;
import java.util.Map;
import java.util.Set;

/**
 * A class representing an association between a consumer and a producer.
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8784 $
 * @since 2.6
 */
public interface Registration
{
   /**
    * Retrieves the internal identifier for this Registration. This will correspond to the database key for this
    * Registration. This identifier can be used by {@link RegistrationPolicy} to create a handle for this Registration.
    *
    * @return this Registration's internal identifier.
    */
   String getPersistentKey();

   /**
    * Sets the handle for this Registration.
    *
    * @param handle
    */
   void setRegistrationHandle(String handle);

   /**
    * Retrieves the handle associated with this Registration
    *
    * @return the registration handle
    */
   String getRegistrationHandle();

   /**
    * Return the consumer owning this registration.
    *
    * @return the owning consumer
    */
   Consumer getConsumer();

   /**
    * Retrieve the properties associated with this Registration.
    *
    * @return a Map containing the associated properties
    */
   Map<QName, Object> getProperties();

   void setPropertyValueFor(QName propertyName, Object value) throws IllegalArgumentException;

   void setPropertyValueFor(String propertyName, Object value);

   boolean hasEqualProperties(Registration registration);

   boolean hasEqualProperties(Map properties);

   /**
    * Return the status of this specific registration.
    *
    * @return the status
    */
   RegistrationStatus getStatus();

   /**
    * Update the registration status
    *
    * @param status the new status
    */
   void setStatus(RegistrationStatus status);

   /**
    * Updates properties to use the specified ones.
    *
    * @param registrationProperties the new registration properties
    */
   void updateProperties(Map registrationProperties);

   /**
    * Removes the property identified by the specified QName.
    *
    * @param propertyName the QName identifying the property to remove
    */
   void removeProperty(QName propertyName);

   /**
    * Removes the property identified by the specified name, which should resolve to a proper QName.
    *
    * @param propertyName the name identifying the property to remove
    */
   void removeProperty(String propertyName);

   /**
    * Retrieves the value of the property identified by the specified QName.
    *
    * @param propertyName the name of the property which value we want to retrieve
    * @return the value of the property associated with the specified QName
    */
   Object getPropertyValueFor(QName propertyName);

   /**
    * Retrieves the value of the property idenfified by the specified name.
    *
    * @param propertyName the name (which must resolve to a proper QName) of the property which value we want to retrieve
    * @return the value of the property associated with the specified name
    */
   Object getPropertyValueFor(String propertyName);

   /**
    * Whether this Registration knows of the specified PortletContext.
    *
    * @param portletContext a PortletContext we want to determine whether it's part of the set managed by this Registration
    * @return <code>true</code> if this Registration knows the specified PortletContext, <code>false</code> otherwise
    */
   boolean knows(PortletContext portletContext);

   /**
    * Retrieves the set of known PortletContexts.
    *
    * @return the set of known PortletContexts.
    */
   Set<PortletContext> getKnownPortletContexts();

   /**
    * Whether this Registration knows of the specified String version of a PortletContext.
    *
    * @param portletContextId a String representation of a PortletContext
    * @return <code>true</code> if this Registration knows the specified PortletContext, <code>false</code> otherwise
    */
   boolean knows(String portletContextId);
}
