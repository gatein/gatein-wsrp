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

package org.gatein.wsrp.producer;

import org.oasis.wsrp.v2.AccessDenied;
import org.oasis.wsrp.v2.ClonePortlet;
import org.oasis.wsrp.v2.CopyPortlets;
import org.oasis.wsrp.v2.CopyPortletsResponse;
import org.oasis.wsrp.v2.DestroyPortlets;
import org.oasis.wsrp.v2.DestroyPortletsResponse;
import org.oasis.wsrp.v2.ExportByValueNotSupported;
import org.oasis.wsrp.v2.ExportNoLongerValid;
import org.oasis.wsrp.v2.ExportPortlets;
import org.oasis.wsrp.v2.ExportPortletsResponse;
import org.oasis.wsrp.v2.Extension;
import org.oasis.wsrp.v2.GetPortletDescription;
import org.oasis.wsrp.v2.GetPortletProperties;
import org.oasis.wsrp.v2.GetPortletPropertyDescription;
import org.oasis.wsrp.v2.GetPortletsLifetime;
import org.oasis.wsrp.v2.GetPortletsLifetimeResponse;
import org.oasis.wsrp.v2.ImportPortlets;
import org.oasis.wsrp.v2.ImportPortletsResponse;
import org.oasis.wsrp.v2.InconsistentParameters;
import org.oasis.wsrp.v2.InvalidHandle;
import org.oasis.wsrp.v2.InvalidRegistration;
import org.oasis.wsrp.v2.InvalidUserCategory;
import org.oasis.wsrp.v2.Lifetime;
import org.oasis.wsrp.v2.MissingParameters;
import org.oasis.wsrp.v2.ModifyRegistrationRequired;
import org.oasis.wsrp.v2.OperationFailed;
import org.oasis.wsrp.v2.OperationNotSupported;
import org.oasis.wsrp.v2.PortletContext;
import org.oasis.wsrp.v2.PortletDescriptionResponse;
import org.oasis.wsrp.v2.PortletPropertyDescriptionResponse;
import org.oasis.wsrp.v2.PropertyList;
import org.oasis.wsrp.v2.ReleaseExport;
import org.oasis.wsrp.v2.ResourceSuspended;
import org.oasis.wsrp.v2.SetExportLifetime;
import org.oasis.wsrp.v2.SetPortletProperties;
import org.oasis.wsrp.v2.SetPortletsLifetime;
import org.oasis.wsrp.v2.SetPortletsLifetimeResponse;

import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public interface PortletManagementInterface
{
   PortletDescriptionResponse getPortletDescription(GetPortletDescription getPortletDescription)
      throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory,
      MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended;

   PortletContext clonePortlet(ClonePortlet clonePortlet)
      throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory,
      MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended;

   DestroyPortletsResponse destroyPortlets(DestroyPortlets destroyPortlets)
      throws InconsistentParameters, InvalidRegistration, MissingParameters, ModifyRegistrationRequired,
      OperationFailed, OperationNotSupported, ResourceSuspended;

   GetPortletsLifetimeResponse getPortletsLifetime(GetPortletsLifetime getPortletsLifetime)
      throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, ModifyRegistrationRequired,
      OperationFailed, OperationNotSupported, ResourceSuspended;

   SetPortletsLifetimeResponse setPortletsLifetime(SetPortletsLifetime setPortletsLifetime)
      throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, ModifyRegistrationRequired,
      OperationFailed, OperationNotSupported, ResourceSuspended;

   CopyPortletsResponse copyPortlets(CopyPortlets copyPortlets)
      throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory,
      MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended;

   ExportPortletsResponse exportPortlets(ExportPortlets exportPortlets)
      throws AccessDenied, ExportByValueNotSupported, InconsistentParameters, InvalidHandle, InvalidRegistration,
      InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported,
      ResourceSuspended;

   ImportPortletsResponse importPortlets(ImportPortlets importPortlets)
      throws AccessDenied, ExportNoLongerValid, InconsistentParameters, InvalidRegistration, InvalidUserCategory,
      MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended;

   List<Extension> releaseExport(ReleaseExport releaseExport);

   Lifetime setExportLifetime(SetExportLifetime setExportLifetime)
      throws AccessDenied, InvalidHandle, InvalidRegistration, ModifyRegistrationRequired, OperationFailed,
      OperationNotSupported, ResourceSuspended;

   PortletContext setPortletProperties(SetPortletProperties setPortletProperties)
      throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory,
      MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended;

   PropertyList getPortletProperties(GetPortletProperties getPortletProperties)
      throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory,
      MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended;

   PortletPropertyDescriptionResponse getPortletPropertyDescription(GetPortletPropertyDescription getPortletPropertyDescription)
      throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory,
      MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended;
}
