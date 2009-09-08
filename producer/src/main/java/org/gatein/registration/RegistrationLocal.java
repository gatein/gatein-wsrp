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


/**
 * Hold registration provided by the consumer for the duration of the invocation.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8784 $
 * @since 2.6
 */
public class RegistrationLocal
{

   /** The thread local to keep track of the registration associated with the current thread of execution. */
   private static final ThreadLocal registrationLocal = new ThreadLocal();

   /**
    * Sets the current registration for the Consumer for the current invocation.
    *
    * @param registration the Registration associated with the Consumer. Set to <code>null</code> if no Registration
    *                     exists for the current Consumer.
    */
   public static void setRegistration(Registration registration)
   {
      registrationLocal.set(registration);
   }

   public static Registration getRegistration()
   {
      return (Registration)registrationLocal.get();
   }
}
