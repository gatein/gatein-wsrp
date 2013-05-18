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

package org.gatein.wsrp.jcr;

import org.chromattic.api.ChromatticSession;
import org.gatein.wsrp.jcr.mapping.mixins.LastModified;

/**
 * Provides behavior for JCR stores that can handle their children by path. This allows for more generic code at the ChromatticPersister level.
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public interface StoresByPathManager<C>
{
   /**
    * Computes the JCR path of the specified child from the root of the workspace associated with the data.
    *
    * @param needsComputedPath the child we want to retrieve the path of
    * @return the JCR path of the specified child.
    */
   String getChildPath(C needsComputedPath);

   /**
    * Retrieves a LastModified view of the object that needs to be set as modified when this manager is asked to delete a child.
    * GTNWSRP-239
    *
    * @param session the ChromatticSession managing the deletion
    * @return a LastModified view of the object that needs to be set as modified when this manager is asked to delete a child or <code>null</code> if no such object exists
    */
   LastModified lastModifiedToUpdateOnDelete(ChromatticSession session);
}
