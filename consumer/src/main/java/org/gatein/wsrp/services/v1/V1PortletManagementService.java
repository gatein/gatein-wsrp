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

package org.gatein.wsrp.services.v1;

import org.gatein.common.NotYetImplemented;
import org.gatein.wsrp.services.PortletManagementService;
import org.oasis.wsrp.v1.WSRPV1PortletManagementPortType;
import org.oasis.wsrp.v2.AccessDenied;
import org.oasis.wsrp.v2.CopiedPortlet;
import org.oasis.wsrp.v2.ExportByValueNotSupported;
import org.oasis.wsrp.v2.ExportNoLongerValid;
import org.oasis.wsrp.v2.ExportedPortlet;
import org.oasis.wsrp.v2.Extension;
import org.oasis.wsrp.v2.FailedPortlets;
import org.oasis.wsrp.v2.ImportPortlet;
import org.oasis.wsrp.v2.ImportPortletsFailed;
import org.oasis.wsrp.v2.ImportedPortlet;
import org.oasis.wsrp.v2.InconsistentParameters;
import org.oasis.wsrp.v2.InvalidHandle;
import org.oasis.wsrp.v2.InvalidRegistration;
import org.oasis.wsrp.v2.InvalidUserCategory;
import org.oasis.wsrp.v2.Lifetime;
import org.oasis.wsrp.v2.MissingParameters;
import org.oasis.wsrp.v2.ModelDescription;
import org.oasis.wsrp.v2.ModifyRegistrationRequired;
import org.oasis.wsrp.v2.OperationFailed;
import org.oasis.wsrp.v2.OperationNotSupported;
import org.oasis.wsrp.v2.PortletContext;
import org.oasis.wsrp.v2.PortletDescription;
import org.oasis.wsrp.v2.PortletLifetime;
import org.oasis.wsrp.v2.Property;
import org.oasis.wsrp.v2.PropertyList;
import org.oasis.wsrp.v2.RegistrationContext;
import org.oasis.wsrp.v2.ResetProperty;
import org.oasis.wsrp.v2.ResourceList;
import org.oasis.wsrp.v2.ResourceSuspended;
import org.oasis.wsrp.v2.SetExportLifetime;
import org.oasis.wsrp.v2.UserContext;

import javax.xml.ws.Holder;
import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class V1PortletManagementService extends PortletManagementService<WSRPV1PortletManagementPortType>
{
   public V1PortletManagementService(WSRPV1PortletManagementPortType port)
   {
      super(port);
   }

   @Override
   public void getPortletDescription(RegistrationContext registrationContext, PortletContext portletContext, UserContext userContext, List<String> desiredLocales, Holder<PortletDescription> portletDescription, Holder<ResourceList> resourceList, Holder<List<Extension>> extensions) throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      throw new NotYetImplemented();
   }

   @Override
   public void clonePortlet(RegistrationContext registrationContext, PortletContext portletContext, UserContext userContext, Lifetime lifetime, Holder<String> portletHandle, Holder<byte[]> portletState, Holder<Lifetime> scheduledDestruction, Holder<List<Extension>> extensions) throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      throw new NotYetImplemented();
   }

   @Override
   public void destroyPortlets(RegistrationContext registrationContext, List<String> portletHandles, UserContext userContext, Holder<List<FailedPortlets>> failedPortlets, Holder<List<Extension>> extensions) throws InconsistentParameters, InvalidRegistration, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      throw new NotYetImplemented();
   }

   @Override
   public void getPortletsLifetime(RegistrationContext registrationContext, List<PortletContext> portletContext, UserContext userContext, Holder<List<PortletLifetime>> portletLifetime, Holder<List<FailedPortlets>> failedPortlets, Holder<ResourceList> resourceList, Holder<List<Extension>> extensions) throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      throw new NotYetImplemented();
   }

   @Override
   public void setPortletsLifetime(RegistrationContext registrationContext, List<PortletContext> portletContext, UserContext userContext, Lifetime lifetime, Holder<List<PortletLifetime>> updatedPortlet, Holder<List<FailedPortlets>> failedPortlets, Holder<ResourceList> resourceList, Holder<List<Extension>> extensions) throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      throw new NotYetImplemented();
   }

   @Override
   public void copyPortlets(RegistrationContext toRegistrationContext, UserContext toUserContext, RegistrationContext fromRegistrationContext, UserContext fromUserContext, List<PortletContext> fromPortletContexts, Lifetime lifetime, Holder<List<CopiedPortlet>> copiedPortlets, Holder<List<FailedPortlets>> failedPortlets, Holder<ResourceList> resourceList, Holder<List<Extension>> extensions) throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      throw new NotYetImplemented();
   }

   @Override
   public void exportPortlets(RegistrationContext registrationContext, List<PortletContext> portletContext, UserContext userContext, Holder<Lifetime> lifetime, Boolean exportByValueRequired, Holder<byte[]> exportContext, Holder<List<ExportedPortlet>> exportedPortlet, Holder<List<FailedPortlets>> failedPortlets, Holder<ResourceList> resourceList, Holder<List<Extension>> extensions) throws AccessDenied, ExportByValueNotSupported, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      throw new NotYetImplemented();
   }

   @Override
   public void importPortlets(RegistrationContext registrationContext, byte[] importContext, List<ImportPortlet> importPortlet, UserContext userContext, Lifetime lifetime, Holder<List<ImportedPortlet>> importedPortlets, Holder<List<ImportPortletsFailed>> importFailed, Holder<ResourceList> resourceList, Holder<List<Extension>> extensions) throws AccessDenied, ExportNoLongerValid, InconsistentParameters, InvalidRegistration, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      throw new NotYetImplemented();
   }

   @Override
   public List<Extension> releaseExport(RegistrationContext registrationContext, byte[] exportContext, UserContext userContext)
   {
      throw new NotYetImplemented();
   }

   @Override
   public Lifetime setExportLifetime(SetExportLifetime setExportLifetime) throws AccessDenied, InvalidHandle, InvalidRegistration, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      throw new NotYetImplemented();
   }

   @Override
   public void setPortletProperties(RegistrationContext registrationContext, PortletContext portletContext, UserContext userContext, PropertyList propertyList, Holder<String> portletHandle, Holder<byte[]> portletState, Holder<Lifetime> scheduledDestruction, Holder<List<Extension>> extensions) throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      throw new NotYetImplemented();
   }

   @Override
   public void getPortletProperties(RegistrationContext registrationContext, PortletContext portletContext, UserContext userContext, List<String> names, Holder<List<Property>> properties, Holder<List<ResetProperty>> resetProperties, Holder<List<Extension>> extensions) throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      throw new NotYetImplemented();
   }

   @Override
   public void getPortletPropertyDescription(RegistrationContext registrationContext, PortletContext portletContext, UserContext userContext, List<String> desiredLocales, Holder<ModelDescription> modelDescription, Holder<ResourceList> resourceList, Holder<List<Extension>> extensions) throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      throw new NotYetImplemented();
   }
}
