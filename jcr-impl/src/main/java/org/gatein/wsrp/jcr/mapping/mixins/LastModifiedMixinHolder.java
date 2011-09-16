/*
* JBoss, a division of Red Hat
* Copyright 2008, Red Hat Middleware, LLC, and individual contributors as indicated
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

package org.gatein.wsrp.jcr.mapping.mixins;

import org.chromattic.api.RelationshipType;
import org.chromattic.api.annotations.Create;
import org.chromattic.api.annotations.OneToOne;
import org.chromattic.api.annotations.Owner;

/** @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a> */
public abstract class LastModifiedMixinHolder extends MixinHolder<LastModified>
{
   @OneToOne(type = RelationshipType.EMBEDDED)
   @Owner
   public abstract LastModified getLastModifiedMixin();

   protected abstract void setLastModifiedMixin(LastModified lastModifiedMixin);

   @Create
   protected abstract LastModified createLastModifiedMixin();

   public void setLastModified(long lastModified)
   {
      getCreatedMixin().setLastModified(lastModified);
   }

   public long getLastModified()
   {
      return getCreatedMixin().getLastModified();
   }

   @Override
   public LastModified getMixin()
   {
      return getLastModifiedMixin();
   }

   @Override
   protected void setMixin(LastModified mixin)
   {
      setLastModifiedMixin(mixin);
   }

   @Override
   protected LastModified createMixin()
   {
      return createLastModifiedMixin();
   }
}
