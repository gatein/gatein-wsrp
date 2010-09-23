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

package org.gatein.registration;

import org.gatein.wsrp.registration.PropertyDescription;

import javax.xml.namespace.QName;

import java.util.List;
import java.util.Map;

/**
 * An interface allowing users of the Registration service to customize different aspects of how Consumers are handled.
 * Methods of this interface are used by RegistrationManager to make appropriate decisions. Implementations of this
 * interface <strong>MUST</strong> provide a no-argument constructor for instantiation from the class name.
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 11406 $
 * @since 2.6
 */
public interface RegistrationPolicy
{
   /**
    * Examines and determines whether the given registration properties are adequate for the Consumer associated with
    * the given identity. This method is called before a Registration is created and thus allows users to decide whether
    * or not to reject a given registration if not satisfied with the given registration properties.
    *
    * @param registrationProperties a Map containing the registration properties in the form of property name (QName) -
    *                               property value (Object) mappings
    * @param consumerIdentity       the Consumer identity (as returned by {@link #getConsumerIdFrom(String,
    *                               java.util.Map)}) for which the registration properties must be ascertained
    * @param expectations           a Map containing the registration expectations of the producer
    * @param manager                the RegistrationManager in which context this RegistrationPolicy is invoked
    * @throws IllegalArgumentException if any of the registration properties is invalid for the specified Consumer
    * @throws RegistrationException    if an exception occured in the registration service
    */
   void validateRegistrationDataFor(Map<QName, Object> registrationProperties, String consumerIdentity,
                                    final Map<QName, ? extends PropertyDescription> expectations, final RegistrationManager manager)
      throws IllegalArgumentException, RegistrationException;

   /**
    * Generates a registration handle based on the database identity of the Registration. This allows users to customize
    * the registration handle format if they want to prevent exposure of database-related data.
    *
    * @param registrationId the database identity of the Registration for which a handle is required.
    * @return a registration handle for the Registration associated with the specified identifier.
    * @throws IllegalArgumentException if the specified registration identity if <code>null</code> or empty
    */
   String createRegistrationHandleFor(String registrationId) throws IllegalArgumentException;

   /**
    * Determines the ConsumerGroup name to which the Consumer associated with the specified name should be assigned with
    * or <code>null</code> if the Consumer should not be automatically assigned to a ConsumerGroup. This method is
    * called during the Consumer creation process to see if the Consumer should be automatically added to a
    * ConsumerGroup.
    *
    * @param consumerName the name of the Consumer being created
    * @return the name of the ConsumerGroup the Consumer must be automatically added to or <code>null</code> if the
    *         Consumer will not be automatically to a ConsumerGroup at creation
    * @throws IllegalArgumentException if the specified Consumer name if <code>null</code> or empty
    */
   String getAutomaticGroupNameFor(String consumerName) throws IllegalArgumentException;

   /**
    * Obtains a consumer identity which uniquely identifies a Consumer in function of the consumer name and registration
    * properties. This is potentially necessary because Consumer names are not guaranteed to be unique (even though the
    * specification states that they should).
    *
    * @param consumerName           the consumer name
    * @param registrationProperties a Map containing the registration properties in the form of property name (QName) -
    *                               property value (Object) mappings. Producer implementations might use the
    *                               registration properties to provide secure Consumer identity.
    * @return the consumer identity
    * @throws InvalidConsumerDataException if the Policy examines the specified registration properties to determine the
    *                                      Consumer identity and decides that they are not in a proper state
    * @throws IllegalArgumentException     if the specified Consumer name if <code>null</code> or empty
    */
   String getConsumerIdFrom(String consumerName, Map<QName, Object> registrationProperties)
      throws IllegalArgumentException, InvalidConsumerDataException;

   /**
    * Determines if the specified Consumer name is acceptable. This method is called before a Consumer is created and
    * before a unique Consumer identity is created. This is in particular used if the Policy mandates that Consumer
    * names must be unique.
    *
    * @param consumerName the name of the Consumer as passed during the registration process
    * @param manager      the RegistrationManager in which context this RegistrationPolicy is invoked
    * @throws IllegalArgumentException if the specified Consumer name if <code>null</code> or empty
    * @throws RegistrationException    if an exception occurred in the Registration service
    */
   void validateConsumerName(String consumerName, final RegistrationManager manager)
      throws IllegalArgumentException, RegistrationException;

   /**
    * Determines if the specified ConsumerGroup name is acceptable. This method is called before a ConsumerGroup is
    * created.
    *
    * @param groupName the name of the ConsumerGroup to be created
    * @param manager   the RegistrationManager in which context this RegistrationPolicy is invoked
    * @throws IllegalArgumentException if the specified ConsumerGroup name if <code>null</code> or empty
    * @throws RegistrationException    if an exception occurred in the Registration service
    */
   void validateConsumerGroupName(String groupName, RegistrationManager manager) throws IllegalArgumentException, RegistrationException;
   
   //TODO: add javadocs here
   boolean checkPortletHandle(Registration registration, String portletHandle);
   void addPortletHandle(Registration registration, String portletHandle);
   void removePortletHandle(Registration registration, String portletHandle);
   void updatePortletHandles(List<String> portlets);
   
   void addPortletContextChangeListener(RegistrationPortletContextChangeListener listener);
}
