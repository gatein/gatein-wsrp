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

import java.util.Collection;

/**
 * An entity that groups several registrations under the same scope, for exemple a Consumer entity could be related to
 * several registrations for the same consumer with different capabilities.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @author @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision:5641 $
 * @since 2.6
 */
public interface Consumer
{

   /**
    * Return the consumer name.
    *
    * @return the consumer name
    */
   String getName();

   /**
    * Return the registration status of the consumer entity.
    *
    * @return the registration stats.
    */
   RegistrationStatus getStatus();

   /**
    * Set the registration status of the consumer entity.
    *
    * @param status the registration status
    */
   void setStatus(RegistrationStatus status);

   /**
    * Return all the registrations for the specified consumer.
    *
    * @return the consumer registrations
    * @throws RegistrationException
    */
   Collection<? extends Registration> getRegistrations() throws RegistrationException;

   /**
    * Returns the group that this consumer belongs to.
    *
    * @return the consumer group
    */
   ConsumerGroup getGroup();

   /**
    * Retrieves this Consumer's identity, which uniquely identifies the Consumer since the name cannot be relied on. It
    * is up to the {@link RegistrationPolicy} to determine what the Consumer's identity is. Note also that this is
    * different from the Consumer's database identifier.
    *
    * @return this Consumer's identity.
    */
   String getId();

   ConsumerCapabilities getCapabilities();

   void setCapabilities(ConsumerCapabilities capabilities);

   void setGroup(ConsumerGroup group) throws RegistrationException, DuplicateRegistrationException;

   String getConsumerAgent();

   void setConsumerAgent(String consumerAgent) throws IllegalArgumentException, IllegalStateException;

   /**
    * Retrieves this Consumer's internal persistent key which would correspond to a primary key in a database.
    *
    * @return
    */
   String getPersistentKey();
}
