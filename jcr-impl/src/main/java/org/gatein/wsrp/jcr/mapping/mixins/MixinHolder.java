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

/**
 * Provides default behavior and functionality for classes wishing to use a mixin.
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 */
public abstract class MixinHolder<M extends BaseMixin>
{
   /**
    * Retrieves a properly initialized mixin that has been created and persisted with the default initial value if none existed.
    *
    * @return the properly initialized mixin associated with this MixinHolder
    */
   protected M getCreatedMixin()
   {
      M mixin = getMixin();
      if (mixin == null)
      {
         mixin = createMixin();
         setMixin(mixin);
         mixin.initializeValue();
      }
      return mixin;
   }

   /**
    * Provides generic access to the mixin. This allows for proper generic encapsulation of the Chromattic-annotated method provided by implementations.
    *
    * @return the related mixin
    */
   public abstract M getMixin();

   /**
    * Provides a generic way to set the mixin on the holding mapping. This allows for proper generic encapsulation of the Chromattic-annotated method provided by implementations.
    *
    * @param mixin the mixin to use on this MixinHolder
    */
   protected abstract void setMixin(M mixin);

   /**
    * Provides a generic way to create a new mixin instance. This allows for proper generic encapsulation of the Chromattic-annotated method provided by implementations.
    *
    * @return the newly created mixin instance.
    */
   protected abstract M createMixin();
}
