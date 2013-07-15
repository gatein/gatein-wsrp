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

package org.gatein.wsrp.jcr.mapping;

/**
 * Provides the default behavior for mapping classes allowing roundtrip conversion between JCR-stored data and object model.
 *
 * @param <T> the type of model objects this mapping can convert to and from
 * @param <R> the type of a registry-like object capable of either registering or creating new model objects for mappings that need to know about such an object, if no registry
 *            needs to be known, using Object for this type is fine
 */
public interface BaseMapping<T, R>
{
   /** Must match constant used by implementations to identify the JCR node name/type that this mapping represents. */
   String JCR_TYPE_NAME_CONSTANT_NAME = "NODE_NAME";

   /**
    * Initializes this BaseMapping from the data of the specified model object
    *
    * @param model the model object to initialize from
    */
   void initFrom(T model);

   /**
    * Converts this BaseMapping into a model object, creating a new one if needed or resetting the specified initial value to the persisted state.
    *
    * @param initial  a potentially <code>null</code> initial value that will be reset to the persisted state
    * @param registry the registry associated with instances of the specified model type
    * @return a new model instance or the specified initial value reset to the persisted state
    */
   T toModel(T initial, R registry);

   /**
    * Retrieves the Java class of the model object this BaseMapping deals with.
    *
    * @return the Java class of the model object this BaseMapping deals with.
    */
   Class<T> getModelClass();
}
