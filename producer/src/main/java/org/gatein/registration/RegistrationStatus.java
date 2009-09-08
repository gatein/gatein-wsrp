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

import java.io.Serializable;

/**
 * Type safe enumeration that describes the status of a registration.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision:5641 $
 */
public class RegistrationStatus implements Serializable
{
   private String humanReadable;

   /** The registration is valid. */
   public static final RegistrationStatus VALID = new RegistrationStatus("valid");

   /** The registration is waiting for validation. */
   public static final RegistrationStatus PENDING = new RegistrationStatus("pending");

   /** The registration is not valid. */
   public static final RegistrationStatus INVALID = new RegistrationStatus("invalid");

   private RegistrationStatus(String humanReadable)
   {
      this.humanReadable = humanReadable;
   }

   public String toString()
   {
      return "RegistrationStatus: " + humanReadable;
   }
}
