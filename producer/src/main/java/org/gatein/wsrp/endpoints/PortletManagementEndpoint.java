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

package org.gatein.wsrp.endpoints;

import org.oasis.wsrp.v1.AccessDenied;
import org.oasis.wsrp.v1.ClonePortlet;
import org.oasis.wsrp.v1.DestroyFailed;
import org.oasis.wsrp.v1.DestroyPortlets;
import org.oasis.wsrp.v1.DestroyPortletsResponse;
import org.oasis.wsrp.v1.Extension;
import org.oasis.wsrp.v1.GetPortletDescription;
import org.oasis.wsrp.v1.GetPortletProperties;
import org.oasis.wsrp.v1.GetPortletPropertyDescription;
import org.oasis.wsrp.v1.InconsistentParameters;
import org.oasis.wsrp.v1.InvalidHandle;
import org.oasis.wsrp.v1.InvalidRegistration;
import org.oasis.wsrp.v1.InvalidUserCategory;
import org.oasis.wsrp.v1.MissingParameters;
import org.oasis.wsrp.v1.ModelDescription;
import org.oasis.wsrp.v1.OperationFailed;
import org.oasis.wsrp.v1.PortletContext;
import org.oasis.wsrp.v1.PortletDescription;
import org.oasis.wsrp.v1.PortletDescriptionResponse;
import org.oasis.wsrp.v1.PortletPropertyDescriptionResponse;
import org.oasis.wsrp.v1.Property;
import org.oasis.wsrp.v1.PropertyList;
import org.oasis.wsrp.v1.RegistrationContext;
import org.oasis.wsrp.v1.ResetProperty;
import org.oasis.wsrp.v1.ResourceList;
import org.oasis.wsrp.v1.SetPortletProperties;
import org.oasis.wsrp.v1.UserContext;
import org.oasis.wsrp.v1.WSRPV1PortletManagementPortType;

import javax.jws.HandlerChain;
import javax.jws.WebParam;
import javax.xml.ws.Holder;
import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 8784 $
 * @since 2.4
 */
@javax.jws.WebService(
   name = "WSRPV1PortletManagementPortType",
   serviceName = "WSRPV1Service",
   portName = "WSRPPortletManagementService",
   targetNamespace = "urn:oasis:names:tc:wsrp:v1:wsdl",
   wsdlLocation = "/WEB-INF/wsdl/wsrp_services.wsdl",
   endpointInterface = "org.oasis.wsrp.v1.WSRPV1PortletManagementPortType"
)
@HandlerChain(file = "wshandlers.xml")
public class PortletManagementEndpoint extends WSRPBaseEndpoint implements WSRPV1PortletManagementPortType
{
   public void getPortletPropertyDescription(
      @WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") RegistrationContext registrationContext,
      @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") PortletContext portletContext,
      @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") UserContext userContext,
      @WebParam(name = "desiredLocales", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> desiredLocales,
      @WebParam(mode = WebParam.Mode.OUT, name = "modelDescription", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<ModelDescription> modelDescription,
      @WebParam(mode = WebParam.Mode.OUT, name = "resourceList", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<ResourceList> resourceList,
      @WebParam(mode = WebParam.Mode.OUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<Extension>> extensions
   ) throws MissingParameters, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory, AccessDenied, OperationFailed
   {
      GetPortletPropertyDescription getPortletPropertyDescription = new GetPortletPropertyDescription();
      getPortletPropertyDescription.setRegistrationContext(registrationContext);
      getPortletPropertyDescription.setPortletContext(portletContext);
      getPortletPropertyDescription.setUserContext(userContext);
      getPortletPropertyDescription.getDesiredLocales().addAll(desiredLocales);

      PortletPropertyDescriptionResponse descriptionResponse = producer.getPortletPropertyDescription(getPortletPropertyDescription);

      modelDescription.value = descriptionResponse.getModelDescription();
      resourceList.value = descriptionResponse.getResourceList();
      extensions.value = descriptionResponse.getExtensions();
   }

   public void setPortletProperties(
      @WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") RegistrationContext registrationContext,
      @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") PortletContext portletContext,
      @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") UserContext userContext,
      @WebParam(name = "propertyList", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") PropertyList propertyList,
      @WebParam(mode = WebParam.Mode.OUT, name = "portletHandle", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<String> portletHandle,
      @WebParam(mode = WebParam.Mode.OUT, name = "portletState", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<byte[]> portletState,
      @WebParam(mode = WebParam.Mode.OUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<Extension>> extensions
   ) throws MissingParameters, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory, AccessDenied, OperationFailed
   {
      SetPortletProperties setPortletProperties = new SetPortletProperties();
      setPortletProperties.setRegistrationContext(registrationContext);
      setPortletProperties.setPortletContext(portletContext);
      setPortletProperties.setUserContext(userContext);
      setPortletProperties.setPropertyList(propertyList);

      PortletContext response = producer.setPortletProperties(setPortletProperties);

      portletHandle.value = response.getPortletHandle();
      portletState.value = response.getPortletState();
      extensions.value = response.getExtensions();
   }

   public void clonePortlet(
      @WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") RegistrationContext registrationContext,
      @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") PortletContext portletContext,
      @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") UserContext userContext,
      @WebParam(mode = WebParam.Mode.OUT, name = "portletHandle", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<String> portletHandle,
      @WebParam(mode = WebParam.Mode.OUT, name = "portletState", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<byte[]> portletState,
      @WebParam(mode = WebParam.Mode.OUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<Extension>> extensions
   ) throws MissingParameters, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory, AccessDenied, OperationFailed
   {
      ClonePortlet clonePortlet = new ClonePortlet();
      clonePortlet.setRegistrationContext(registrationContext);
      clonePortlet.setPortletContext(portletContext);
      clonePortlet.setUserContext(userContext);

      PortletContext response = producer.clonePortlet(clonePortlet);

      portletHandle.value = response.getPortletHandle();
      portletState.value = response.getPortletState();
      extensions.value = response.getExtensions();
   }

   public void getPortletDescription(
      @WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") RegistrationContext registrationContext,
      @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") PortletContext portletContext,
      @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") UserContext userContext,
      @WebParam(name = "desiredLocales", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> desiredLocales,
      @WebParam(mode = WebParam.Mode.OUT, name = "portletDescription", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<PortletDescription> portletDescription,
      @WebParam(mode = WebParam.Mode.OUT, name = "resourceList", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<ResourceList> resourceList,
      @WebParam(mode = WebParam.Mode.OUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<Extension>> extensions
   ) throws MissingParameters, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory, AccessDenied, OperationFailed
   {
      GetPortletDescription getPortletDescription = new GetPortletDescription();
      getPortletDescription.setRegistrationContext(registrationContext);
      getPortletDescription.setPortletContext(portletContext);
      getPortletDescription.setUserContext(userContext);
      getPortletDescription.getDesiredLocales().addAll(desiredLocales);

      PortletDescriptionResponse description = producer.getPortletDescription(getPortletDescription);

      portletDescription.value = description.getPortletDescription();
      resourceList.value = description.getResourceList();
      extensions.value = description.getExtensions();
   }

   public void getPortletProperties(
      @WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") RegistrationContext registrationContext,
      @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") PortletContext portletContext,
      @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") UserContext userContext,
      @WebParam(name = "names", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> names,
      @WebParam(mode = WebParam.Mode.OUT, name = "properties", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<Property>> properties,
      @WebParam(mode = WebParam.Mode.OUT, name = "resetProperties", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<ResetProperty>> resetProperties,
      @WebParam(mode = WebParam.Mode.OUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<Extension>> extensions
   ) throws MissingParameters, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory, AccessDenied, OperationFailed
   {
      GetPortletProperties getPortletProperties = new GetPortletProperties();
      getPortletProperties.setRegistrationContext(registrationContext);
      getPortletProperties.setPortletContext(portletContext);
      getPortletProperties.setUserContext(userContext);
      getPortletProperties.getNames().addAll(names);

      PropertyList result = producer.getPortletProperties(getPortletProperties);

      properties.value = result.getProperties();
      resetProperties.value = result.getResetProperties();
      extensions.value = result.getExtensions();
   }

   public void destroyPortlets(
      @WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") RegistrationContext registrationContext,
      @WebParam(name = "portletHandles", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> portletHandles,
      @WebParam(mode = WebParam.Mode.OUT, name = "destroyFailed", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<DestroyFailed>> destroyFailed,
      @WebParam(mode = WebParam.Mode.OUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<Extension>> extensions
   ) throws MissingParameters, InconsistentParameters, InvalidRegistration, OperationFailed
   {
      DestroyPortlets destroyPortlets = new DestroyPortlets();
      destroyPortlets.setRegistrationContext(registrationContext);
      destroyPortlets.getPortletHandles().addAll(portletHandles);

      DestroyPortletsResponse destroyPortletsResponse = producer.destroyPortlets(destroyPortlets);

      destroyFailed.value = destroyPortletsResponse.getDestroyFailed();
      extensions.value = destroyPortletsResponse.getExtensions();
   }
}
