/*
 * JBoss, a division of Red Hat
 * Copyright 2010, Red Hat Middleware, LLC, and individual
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

package org.gatein.wsrp.producer.v1;

import org.oasis.wsrp.v1.V1AccessDenied;
import org.oasis.wsrp.v1.V1ClonePortlet;
import org.oasis.wsrp.v1.V1DestroyPortlets;
import org.oasis.wsrp.v1.V1DestroyPortletsResponse;
import org.oasis.wsrp.v1.V1GetPortletDescription;
import org.oasis.wsrp.v1.V1GetPortletProperties;
import org.oasis.wsrp.v1.V1GetPortletPropertyDescription;
import org.oasis.wsrp.v1.V1InconsistentParameters;
import org.oasis.wsrp.v1.V1InvalidHandle;
import org.oasis.wsrp.v1.V1InvalidRegistration;
import org.oasis.wsrp.v1.V1InvalidUserCategory;
import org.oasis.wsrp.v1.V1MissingParameters;
import org.oasis.wsrp.v1.V1OperationFailed;
import org.oasis.wsrp.v1.V1PortletContext;
import org.oasis.wsrp.v1.V1PortletDescriptionResponse;
import org.oasis.wsrp.v1.V1PortletPropertyDescriptionResponse;
import org.oasis.wsrp.v1.V1PropertyList;
import org.oasis.wsrp.v1.V1SetPortletProperties;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public interface V1PortletManagementInterface
{
   V1PortletDescriptionResponse getPortletDescription(V1GetPortletDescription getPortletDescription)
      throws V1AccessDenied, V1InvalidHandle, V1InvalidUserCategory, V1InconsistentParameters, V1MissingParameters,
      V1InvalidRegistration, V1OperationFailed;

   V1PortletContext clonePortlet(V1ClonePortlet clonePortlet) throws V1InvalidUserCategory, V1AccessDenied,
      V1OperationFailed, V1InvalidHandle, V1InvalidRegistration, V1InconsistentParameters, V1MissingParameters;

   V1DestroyPortletsResponse destroyPortlets(V1DestroyPortlets destroyPortlets) throws V1InconsistentParameters,
      V1MissingParameters, V1InvalidRegistration, V1OperationFailed;

   V1PortletContext setPortletProperties(V1SetPortletProperties setPortletProperties) throws V1OperationFailed,
      V1InvalidHandle, V1MissingParameters, V1InconsistentParameters, V1InvalidUserCategory, V1AccessDenied, V1InvalidRegistration;

   V1PropertyList getPortletProperties(V1GetPortletProperties getPortletProperties) throws V1InvalidHandle,
      V1MissingParameters, V1InvalidRegistration, V1AccessDenied, V1OperationFailed, V1InconsistentParameters, V1InvalidUserCategory;

   V1PortletPropertyDescriptionResponse getPortletPropertyDescription(V1GetPortletPropertyDescription getPortletPropertyDescription)
      throws V1MissingParameters, V1InconsistentParameters, V1InvalidUserCategory, V1InvalidRegistration, V1AccessDenied,
      V1InvalidHandle, V1OperationFailed;
}
