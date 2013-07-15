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

/**
 * A listener that can be notified before a Registration is destroy to get a chance to veto the destruction.
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public interface RegistrationDestructionListener
{
   /**
    * Called before the specified registration is destroyed. Listener has the opportunity to prevent destruction by
    * returning a negative Vote.
    *
    * @param registration the Registration about to be destroyed
    * @return {@link #SUCCESS} if this listener agrees to move forward with the destruction, or a negative Vote built
    *         using {@link Vote#negativeVote(String)} specifying the reason of the negative vote.
    */
   Vote destructionScheduledFor(Registration registration);

   class Vote
   {
      public final boolean result;
      public final String reason;

      public static Vote negativeVote(String reason)
      {
         return new Vote(reason);
      }

      Vote(String reason)
      {
         this.result = false;
         this.reason = reason;
      }

      Vote()
      {
         result = true;
         reason = null;
      }
   }

   Vote SUCCESS = new Vote();
}
