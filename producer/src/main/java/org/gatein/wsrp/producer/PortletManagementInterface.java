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

package org.gatein.wsrp.producer;

import org.oasis.wsrp.v1.AccessDenied;
import org.oasis.wsrp.v1.ClonePortlet;
import org.oasis.wsrp.v1.DestroyPortlets;
import org.oasis.wsrp.v1.DestroyPortletsResponse;
import org.oasis.wsrp.v1.GetPortletDescription;
import org.oasis.wsrp.v1.GetPortletProperties;
import org.oasis.wsrp.v1.GetPortletPropertyDescription;
import org.oasis.wsrp.v1.InconsistentParameters;
import org.oasis.wsrp.v1.InvalidHandle;
import org.oasis.wsrp.v1.InvalidRegistration;
import org.oasis.wsrp.v1.InvalidUserCategory;
import org.oasis.wsrp.v1.MissingParameters;
import org.oasis.wsrp.v1.OperationFailed;
import org.oasis.wsrp.v1.PortletContext;
import org.oasis.wsrp.v1.PortletDescriptionResponse;
import org.oasis.wsrp.v1.PortletPropertyDescriptionResponse;
import org.oasis.wsrp.v1.PropertyList;
import org.oasis.wsrp.v1.SetPortletProperties;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public interface PortletManagementInterface
{
   PortletDescriptionResponse getPortletDescription(GetPortletDescription getPortletDescription)
      throws AccessDenied, InvalidHandle, InvalidUserCategory, InconsistentParameters, MissingParameters,
      InvalidRegistration, OperationFailed;

   PortletContext clonePortlet(ClonePortlet clonePortlet) throws InvalidUserCategory, AccessDenied,
      OperationFailed, InvalidHandle, InvalidRegistration, InconsistentParameters, MissingParameters;

   DestroyPortletsResponse destroyPortlets(DestroyPortlets destroyPortlets) throws InconsistentParameters,
      MissingParameters, InvalidRegistration, OperationFailed;

   PortletContext setPortletProperties(SetPortletProperties setPortletProperties) throws OperationFailed,
      InvalidHandle, MissingParameters, InconsistentParameters, InvalidUserCategory, AccessDenied, InvalidRegistration;

   PropertyList getPortletProperties(GetPortletProperties getPortletProperties) throws InvalidHandle,
      MissingParameters, InvalidRegistration, AccessDenied, OperationFailed, InconsistentParameters, InvalidUserCategory;

   PortletPropertyDescriptionResponse getPortletPropertyDescription(GetPortletPropertyDescription getPortletPropertyDescription)
      throws MissingParameters, InconsistentParameters, InvalidUserCategory, InvalidRegistration, AccessDenied,
      InvalidHandle, OperationFailed;
}
