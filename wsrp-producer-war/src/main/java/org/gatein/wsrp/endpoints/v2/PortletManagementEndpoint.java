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

package org.gatein.wsrp.endpoints.v2;

import org.apache.cxf.feature.Features;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.endpoints.WSRPBaseEndpoint;
import org.oasis.wsrp.v2.AccessDenied;
import org.oasis.wsrp.v2.ClonePortlet;
import org.oasis.wsrp.v2.CopiedPortlet;
import org.oasis.wsrp.v2.DestroyPortlets;
import org.oasis.wsrp.v2.DestroyPortletsResponse;
import org.oasis.wsrp.v2.ExportByValueNotSupported;
import org.oasis.wsrp.v2.ExportNoLongerValid;
import org.oasis.wsrp.v2.ExportPortlets;
import org.oasis.wsrp.v2.ExportPortletsResponse;
import org.oasis.wsrp.v2.ExportedPortlet;
import org.oasis.wsrp.v2.Extension;
import org.oasis.wsrp.v2.FailedPortlets;
import org.oasis.wsrp.v2.GetPortletDescription;
import org.oasis.wsrp.v2.GetPortletProperties;
import org.oasis.wsrp.v2.GetPortletPropertyDescription;
import org.oasis.wsrp.v2.ImportPortlet;
import org.oasis.wsrp.v2.ImportPortlets;
import org.oasis.wsrp.v2.ImportPortletsFailed;
import org.oasis.wsrp.v2.ImportPortletsResponse;
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
import org.oasis.wsrp.v2.PortletDescriptionResponse;
import org.oasis.wsrp.v2.PortletLifetime;
import org.oasis.wsrp.v2.PortletPropertyDescriptionResponse;
import org.oasis.wsrp.v2.Property;
import org.oasis.wsrp.v2.PropertyList;
import org.oasis.wsrp.v2.RegistrationContext;
import org.oasis.wsrp.v2.ReleaseExport;
import org.oasis.wsrp.v2.ResetProperty;
import org.oasis.wsrp.v2.ResourceList;
import org.oasis.wsrp.v2.ResourceSuspended;
import org.oasis.wsrp.v2.SetExportLifetime;
import org.oasis.wsrp.v2.SetPortletProperties;
import org.oasis.wsrp.v2.UserContext;
import org.oasis.wsrp.v2.WSRPV2PortletManagementPortType;

import javax.jws.HandlerChain;
import javax.jws.WebParam;
import javax.xml.ws.Holder;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 8784 $
 * @since 2.4
 */
@javax.jws.WebService(
   name = "WSRPV2PortletManagementPortType",
   serviceName = "WSRPService",
   portName = "WSRPPortletManagementService",
   targetNamespace = "urn:oasis:names:tc:wsrp:v2:wsdl",
   wsdlLocation = "/WEB-INF/wsdl/wsrp-2.0-services.wsdl",
   endpointInterface = "org.oasis.wsrp.v2.WSRPV2PortletManagementPortType"
)
@HandlerChain(file="../producer-handler-chains.xml")
@Features(features = "org.gatein.wsrp.cxf.WSRPEndpointFeature")
public class PortletManagementEndpoint extends WSRPBaseEndpoint implements WSRPV2PortletManagementPortType
{
   public void getPortletPropertyDescription(
      @WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext,
      @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") PortletContext portletContext,
      @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext,
      @WebParam(name = "desiredLocales", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") List<String> desiredLocales,
      @WebParam(name = "modelDescription", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<ModelDescription> modelDescription,
      @WebParam(name = "resourceList", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<ResourceList> resourceList,
      @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions)
      throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory,
      MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      GetPortletPropertyDescription getPortletPropertyDescription = WSRPTypeFactory.createGetPortletPropertyDescription(registrationContext, portletContext, userContext, desiredLocales);

      PortletPropertyDescriptionResponse descriptionResponse = producer.getPortletPropertyDescription(getPortletPropertyDescription);

      modelDescription.value = descriptionResponse.getModelDescription();
      resourceList.value = descriptionResponse.getResourceList();
      extensions.value = descriptionResponse.getExtensions();
   }

   public void getPortletDescription(
      @WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext,
      @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") PortletContext portletContext,
      @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext,
      @WebParam(name = "desiredLocales", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") List<String> desiredLocales,
      @WebParam(name = "portletDescription", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<PortletDescription> portletDescription,
      @WebParam(name = "resourceList", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<ResourceList> resourceList,
      @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions)
      throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory,
      MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      GetPortletDescription getPortletDescription = WSRPTypeFactory.createGetPortletDescription(registrationContext, portletContext, userContext);
      getPortletDescription.getDesiredLocales().addAll(desiredLocales);

      PortletDescriptionResponse description = producer.getPortletDescription(getPortletDescription);

      portletDescription.value = description.getPortletDescription();
      resourceList.value = description.getResourceList();
      extensions.value = description.getExtensions();
   }

   public void clonePortlet(
      @WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext,
      @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") PortletContext portletContext,
      @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext,
      @WebParam(name = "lifetime", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") Lifetime lifetime,
      @WebParam(name = "portletHandle", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<String> portletHandle,
      @WebParam(name = "portletState", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<byte[]> portletState,
      @WebParam(name = "scheduledDestruction", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<Lifetime> scheduledDestruction,
      @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions)
      throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory,
      MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      ClonePortlet clonePortlet = WSRPTypeFactory.createClonePortlet(registrationContext, portletContext, userContext);

      PortletContext response = producer.clonePortlet(clonePortlet);

      portletHandle.value = response.getPortletHandle();
      portletState.value = response.getPortletState();
      extensions.value = response.getExtensions();
   }

   public void destroyPortlets(
      @WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext,
      @WebParam(name = "portletHandles", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") List<String> portletHandles,
      @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext,
      @WebParam(name = "failedPortlets", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<FailedPortlets>> failedPortlets,
      @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions)
      throws InconsistentParameters, InvalidRegistration, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      DestroyPortlets destroyPortlets = WSRPTypeFactory.createDestroyPortlets(registrationContext, portletHandles);

      DestroyPortletsResponse destroyPortletsResponse = producer.destroyPortlets(destroyPortlets);

      failedPortlets.value = destroyPortletsResponse.getFailedPortlets();
      extensions.value = destroyPortletsResponse.getExtensions();
   }

   public void getPortletsLifetime(
      @WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext,
      @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") List<PortletContext> portletContext,
      @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext,
      @WebParam(name = "portletLifetime", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<PortletLifetime>> portletLifetime,
      @WebParam(name = "failedPortlets", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<FailedPortlets>> failedPortlets,
      @WebParam(name = "resourceList", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<ResourceList> resourceList,
      @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions)
      throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      //To change body of implemented methods use File | Settings | File Templates.
   }

   public void setPortletsLifetime(
      @WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext,
      @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") List<PortletContext> portletContext,
      @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext,
      @WebParam(name = "lifetime", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") Lifetime lifetime,
      @WebParam(name = "updatedPortlet", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<PortletLifetime>> updatedPortlet,
      @WebParam(name = "failedPortlets", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<FailedPortlets>> failedPortlets,
      @WebParam(name = "resourceList", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<ResourceList> resourceList,
      @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions)
      throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      //To change body of implemented methods use File | Settings | File Templates.
   }

   public void copyPortlets(
      @WebParam(name = "toRegistrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext toRegistrationContext,
      @WebParam(name = "toUserContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext toUserContext,
      @WebParam(name = "fromRegistrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext fromRegistrationContext,
      @WebParam(name = "fromUserContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext fromUserContext,
      @WebParam(name = "fromPortletContexts", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") List<PortletContext> fromPortletContexts,
      @WebParam(name = "lifetime", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") Lifetime lifetime,
      @WebParam(name = "copiedPortlets", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<CopiedPortlet>> copiedPortlets,
      @WebParam(name = "failedPortlets", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<FailedPortlets>> failedPortlets,
      @WebParam(name = "resourceList", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<ResourceList> resourceList,
      @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions)
      throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      //To change body of implemented methods use File | Settings | File Templates.
   }

   public void exportPortlets(
      @WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext,
      @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") List<PortletContext> portletContext,
      @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext,
      @WebParam(name = "lifetime", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.INOUT) Holder<Lifetime> lifetime,
      @WebParam(name = "exportByValueRequired", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") Boolean exportByValueRequired,
      @WebParam(name = "exportContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<byte[]> exportContext,
      @WebParam(name = "exportedPortlet", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<ExportedPortlet>> exportedPortlet,
      @WebParam(name = "failedPortlets", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<FailedPortlets>> failedPortlets,
      @WebParam(name = "resourceList", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<ResourceList> resourceList,
      @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions)
      throws AccessDenied, ExportByValueNotSupported, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      ExportPortlets exportPortlets = WSRPTypeFactory.createExportPortlets(registrationContext, portletContext, userContext, lifetime.value, exportByValueRequired);

      ExportPortletsResponse exportPortletsResponse = producer.exportPortlets(exportPortlets);

      lifetime.value = exportPortletsResponse.getLifetime();
      exportContext.value = exportPortletsResponse.getExportContext();
      exportedPortlet.value = exportPortletsResponse.getExportedPortlet();
      failedPortlets.value = exportPortletsResponse.getFailedPortlets();
      resourceList.value = exportPortletsResponse.getResourceList();
      extensions.value = exportPortletsResponse.getExtensions();
   }

   public void importPortlets(
      @WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext,
      @WebParam(name = "importContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") byte[] importContext,
      @WebParam(name = "importPortlet", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") List<ImportPortlet> importPortlet,
      @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext,
      @WebParam(name = "lifetime", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") Lifetime lifetime,
      @WebParam(name = "importedPortlets", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<ImportedPortlet>> importedPortlets,
      @WebParam(name = "importFailed", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<ImportPortletsFailed>> importFailed,
      @WebParam(name = "resourceList", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<ResourceList> resourceList,
      @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions)
      throws AccessDenied, ExportNoLongerValid, InconsistentParameters, InvalidRegistration, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      ImportPortlets importPortlets = WSRPTypeFactory.createImportPortlets(registrationContext, importContext, importPortlet, userContext, lifetime);

      ImportPortletsResponse importPortletsResponse = producer.importPortlets(importPortlets);

      importedPortlets.value = importPortletsResponse.getImportedPortlets();
      importFailed.value = importPortletsResponse.getImportFailed();
      resourceList.value = importPortletsResponse.getResourceList();
      extensions.value = importPortletsResponse.getExtensions();
   }

   public List<Extension> releaseExport(
      @WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext,
      @WebParam(name = "exportContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") byte[] exportContext,
      @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext)
   {
      ReleaseExport releaseExport = WSRPTypeFactory.createReleaseExport(registrationContext, exportContext, userContext);

      producer.releaseExport(releaseExport);

      //release export isn't suppose to return anything
      return new ArrayList<Extension>();
   }

   public Lifetime setExportLifetime(
      @WebParam(name = "setExportLifetime", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", partName = "setExportLifetime") SetExportLifetime setExportLifetime)
      throws AccessDenied, InvalidHandle, InvalidRegistration, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      //Note: we are getting a SetExportLifetime instead of the components for some reason.
      return producer.setExportLifetime(setExportLifetime);
   }

   public void setPortletProperties(
      @WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext,
      @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") PortletContext portletContext,
      @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext,
      @WebParam(name = "propertyList", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") PropertyList propertyList,
      @WebParam(name = "portletHandle", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<String> portletHandle,
      @WebParam(name = "portletState", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<byte[]> portletState,
      @WebParam(name = "scheduledDestruction", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<Lifetime> scheduledDestruction,
      @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions)
      throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      SetPortletProperties setPortletProperties = WSRPTypeFactory.createSetPortletProperties(registrationContext, portletContext, propertyList);
      setPortletProperties.setUserContext(userContext);

      PortletContext response = producer.setPortletProperties(setPortletProperties);

      portletHandle.value = response.getPortletHandle();
      portletState.value = response.getPortletState();
      extensions.value = response.getExtensions();
   }

   public void getPortletProperties(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") PortletContext portletContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext, @WebParam(name = "names", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") List<String> names, @WebParam(name = "properties", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Property>> properties, @WebParam(name = "resetProperties", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<ResetProperty>> resetProperties, @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions) throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      GetPortletProperties getPortletProperties = WSRPTypeFactory.createGetPortletProperties(registrationContext, portletContext, userContext, names);

      PropertyList result = producer.getPortletProperties(getPortletProperties);

      properties.value = result.getProperties();
      resetProperties.value = result.getResetProperties();
      extensions.value = result.getExtensions();
   }
}
