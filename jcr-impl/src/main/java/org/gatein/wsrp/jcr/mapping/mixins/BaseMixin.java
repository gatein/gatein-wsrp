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

package org.gatein.wsrp.jcr.mapping.mixins;

/**
 * Provides base behavior for mixins, which capture a specific view of the persisted data, potentially cross-cutting functionality. Mixin implementations are meant to focus on a
 * single aspect of the persisted data with very narrow functional scope. They are useful to add dynamically new data into already persisted information without having to change
 * the persistence "schema", thus allowing rather seamless evolution of the persisted data.
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 */
public interface BaseMixin
{
   /**
    * Provides a means to initialize the value held by this mixin. Useful when the {@link org.chromattic.api.annotations.DefaultValue} annotation cannot be used because the initial
    * value is not known at compile time.
    */
   void initializeValue();
}
