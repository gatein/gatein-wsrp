/*
* JBoss, a division of Red Hat
* Copyright 2012, Red Hat Middleware, LLC, and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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

package org.gatein.wsrp;

import org.gatein.common.util.ParameterValidation;

/**
 * Encapsulates support for last modification checking, the returned values being not useful by themselves but rather as a means to compare them to another such value. Useful for
 * quick dirty checking of objects.
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 */
public class SupportsLastModified
{
   /** GTNWSRP-239: last modification epoch: currently persistent via mixin */
   private long lastModified;

   /**
    * Sets the last modified time to now (as returned by {@link #now()}) if the specified values, presumably fields of the calling object, are different. This method is useful to
    * marks this object as modified when setting a field to the specified new value would result in a modification of the object based on the specified current/old value of the
    * field. Values are compared using {@link ParameterValidation#isOldAndNewDifferent(Object, Object)}.
    *
    * @param oldValue the current / old value that might be replaced by the specified new value
    * @param newValue the new value that might replace the current / old one
    * @return <code>true</code> if both given values were different and therefore would lead to the calling object being set as modified now, <code>false</code> otherwise.
    */
   protected boolean modifyNowIfNeeded(Object oldValue, Object newValue)
   {
      if (ParameterValidation.isOldAndNewDifferent(oldValue, newValue))
      {
         modifyNow();
         return true;
      }
      else
      {
         return false;
      }
   }

   /** Specifies that this object is modified as of now, as specified by {@link #now()}. */
   public void modifyNow()
   {
      setLastModified(now());
   }

   /**
    * Retrieves a long value recording when (with unspecified origin) this object was last modified. Since the time origin is unspecified, the returned value shouldn't be
    * understood as useful in itself. It only takes meaning when comparing it to another such value to compute an interval. Therefore, only differences between returned values
    * matter, not individual values which do not necessarily mark a useful (in itself) point in time.
    *
    * @return a long value recording when (with unspecified origin) this object was last modified.
    */
   public long getLastModified()
   {
      return lastModified;
   }

   /**
    * Sets the last modified marker to the specified value. Note that this method probably shouldn't be used directly by client code which should use {@link #modifyNow()} instead.
    * This method is available for persistence layer implementations to restore value from storage.
    *
    * @param lastModified the new last modified time marker
    */
   public void setLastModified(long lastModified)
   {
      this.lastModified = lastModified;
   }

   /**
    * Retrieves a value representing the current instant / now, without specified origin so shouldn't be depended on to mark a meaningful instant in time by itself. The value is
    * however guaranteed to be meaningful with respect to comparison with previous or future invocations of this method.
    *
    * @return a value representing the current instant / now, without specified origin.
    */
   public static long now()
   {
      // todo: this might be a cause of errors if different nodes run on different VMs since nanoTime is not guaranteed to work similarly on all VMs.
      return System.nanoTime();
   }
}
