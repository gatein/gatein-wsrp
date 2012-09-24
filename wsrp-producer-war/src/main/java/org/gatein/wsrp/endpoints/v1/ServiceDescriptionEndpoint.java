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

package org.gatein.wsrp.endpoints.v1;

import org.apache.cxf.feature.Features;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.WSRPUtils;
import org.gatein.wsrp.endpoints.WSRPBaseEndpoint;
import org.gatein.wsrp.spec.v1.V1ToV2Converter;
import org.gatein.wsrp.spec.v1.V2ToV1Converter;
import org.gatein.wsrp.spec.v1.WSRP1ExceptionFactory;
import org.oasis.wsrp.v1.V1CookieProtocol;
import org.oasis.wsrp.v1.V1Extension;
import org.oasis.wsrp.v1.V1InvalidRegistration;
import org.oasis.wsrp.v1.V1ItemDescription;
import org.oasis.wsrp.v1.V1ModelDescription;
import org.oasis.wsrp.v1.V1OperationFailed;
import org.oasis.wsrp.v1.V1PortletDescription;
import org.oasis.wsrp.v1.V1RegistrationContext;
import org.oasis.wsrp.v1.V1ResourceList;
import org.oasis.wsrp.v1.WSRPV1ServiceDescriptionPortType;
import org.oasis.wsrp.v2.GetServiceDescription;
import org.oasis.wsrp.v2.InvalidRegistration;
import org.oasis.wsrp.v2.ModifyRegistrationRequired;
import org.oasis.wsrp.v2.OperationFailed;
import org.oasis.wsrp.v2.ResourceSuspended;
import org.oasis.wsrp.v2.ServiceDescription;

import javax.jws.HandlerChain;
import javax.jws.WebParam;
import javax.xml.ws.Holder;
import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 8784 $
 */
@javax.jws.WebService(
   name = "WSRPV1ServiceDescriptionPortType",
   serviceName = "WSRPService",
   portName = "WSRPServiceDescriptionService",
   targetNamespace = "urn:oasis:names:tc:wsrp:v1:wsdl",
   wsdlLocation = "/WEB-INF/wsdl/wsrp_services.wsdl",
   endpointInterface = "org.oasis.wsrp.v1.WSRPV1ServiceDescriptionPortType"
)
@HandlerChain(file="../producer-handler-chains.xml")
@Features(features = "org.gatein.wsrp.cxf.WSRPEndpointFeature")
public class ServiceDescriptionEndpoint extends WSRPBaseEndpoint implements WSRPV1ServiceDescriptionPortType
{
   public void getServiceDescription(
      @WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1RegistrationContext registrationContext,
      @WebParam(name = "desiredLocales", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> desiredLocales,
      @WebParam(mode = WebParam.Mode.OUT, name = "requiresRegistration", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<Boolean> requiresRegistration,
      @WebParam(mode = WebParam.Mode.OUT, name = "offeredPortlets", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<V1PortletDescription>> offeredPortlets,
      @WebParam(mode = WebParam.Mode.OUT, name = "userCategoryDescriptions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<V1ItemDescription>> userCategoryDescriptions,
      @WebParam(mode = WebParam.Mode.OUT, name = "customUserProfileItemDescriptions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<V1ItemDescription>> customUserProfileItemDescriptions,
      @WebParam(mode = WebParam.Mode.OUT, name = "customWindowStateDescriptions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<V1ItemDescription>> customWindowStateDescriptions,
      @WebParam(mode = WebParam.Mode.OUT, name = "customModeDescriptions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<V1ItemDescription>> customModeDescriptions,
      @WebParam(mode = WebParam.Mode.OUT, name = "requiresInitCookie", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<V1CookieProtocol> requiresInitCookie,
      @WebParam(mode = WebParam.Mode.OUT, name = "registrationPropertyDescription", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<V1ModelDescription> registrationPropertyDescription,
      @WebParam(mode = WebParam.Mode.OUT, name = "locales", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<String>> locales,
      @WebParam(mode = WebParam.Mode.OUT, name = "resourceList", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<V1ResourceList> resourceList,
      @WebParam(mode = WebParam.Mode.OUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<V1Extension>> extensions
   ) throws V1InvalidRegistration, V1OperationFailed
   {
      GetServiceDescription getServiceDescription = WSRPTypeFactory.createGetServiceDescription(
         V1ToV2Converter.toV2RegistrationContext(registrationContext), null);
      getServiceDescription.getDesiredLocales().addAll(desiredLocales);

      ServiceDescription description;
      try
      {
         description = producer.getServiceDescription(getServiceDescription);
      }
      catch (InvalidRegistration invalidRegistration)
      {
         throw V2ToV1Converter.toV1Exception(V1InvalidRegistration.class, invalidRegistration);
      }
      catch (OperationFailed operationFailed)
      {
         throw V2ToV1Converter.toV1Exception(V1OperationFailed.class, operationFailed);
      }
      catch (ModifyRegistrationRequired modifyRegistrationRequired)
      {
         throw WSRP1ExceptionFactory.createWSException(V1OperationFailed.class, "Need to call modifyRegistration", modifyRegistrationRequired);
      }
      catch (ResourceSuspended resourceSuspended)
      {
         throw WSRP1ExceptionFactory.createWSException(V1OperationFailed.class, "Resource suspended", resourceSuspended);
      }

      requiresRegistration.value = description.isRequiresRegistration();
      offeredPortlets.value = WSRPUtils.transform(description.getOfferedPortlets(), V2ToV1Converter.PORTLETDESCRIPTION);
      userCategoryDescriptions.value = WSRPUtils.transform(description.getUserCategoryDescriptions(), V2ToV1Converter.ITEMDESCRIPTION);
//      customUserProfileItemDescriptions.value = description.getCustomUserProfileItemDescriptions();
      customWindowStateDescriptions.value = WSRPUtils.transform(description.getCustomWindowStateDescriptions(), V2ToV1Converter.ITEMDESCRIPTION);
      customModeDescriptions.value = WSRPUtils.transform(description.getCustomModeDescriptions(), V2ToV1Converter.ITEMDESCRIPTION);
      requiresInitCookie.value = V2ToV1Converter.toV1CookieProtocol(description.getRequiresInitCookie());
      registrationPropertyDescription.value = V2ToV1Converter.toV1ModelDescription(description.getRegistrationPropertyDescription());
      locales.value = description.getLocales();
      resourceList.value = V2ToV1Converter.toV1ResourceList(description.getResourceList());
      extensions.value = WSRPUtils.transform(description.getExtensions(), V2ToV1Converter.EXTENSION);
   }
}
