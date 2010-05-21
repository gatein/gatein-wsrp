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

import org.gatein.wsrp.endpoints.WSRPBaseEndpoint;
import org.oasis.wsrp.v2.AccessDenied;
import org.oasis.wsrp.v2.Extension;
import org.oasis.wsrp.v2.GetRegistrationLifetime;
import org.oasis.wsrp.v2.InvalidHandle;
import org.oasis.wsrp.v2.InvalidRegistration;
import org.oasis.wsrp.v2.Lifetime;
import org.oasis.wsrp.v2.MissingParameters;
import org.oasis.wsrp.v2.ModifyRegistration;
import org.oasis.wsrp.v2.ModifyRegistrationRequired;
import org.oasis.wsrp.v2.OperationFailed;
import org.oasis.wsrp.v2.OperationNotSupported;
import org.oasis.wsrp.v2.Property;
import org.oasis.wsrp.v2.RegistrationContext;
import org.oasis.wsrp.v2.RegistrationData;
import org.oasis.wsrp.v2.RegistrationState;
import org.oasis.wsrp.v2.ResourceSuspended;
import org.oasis.wsrp.v2.SetRegistrationLifetime;
import org.oasis.wsrp.v2.UserContext;
import org.oasis.wsrp.v2.WSRPV2RegistrationPortType;

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
   name = "WSRPV2RegistrationPortType",
   serviceName = "WSRPService",
   portName = "WSRPRegistrationService",
   targetNamespace = "urn:oasis:names:tc:wsrp:v2:wsdl",
   wsdlLocation = "/WEB-INF/wsdl/wsrp-2.0-services.wsdl",
   endpointInterface = "org.oasis.wsrp.v2.WSRPV2RegistrationPortType"
)
@HandlerChain(file = "wshandlers.xml")
public class RegistrationEndpoint extends WSRPBaseEndpoint implements WSRPV2RegistrationPortType
{

   public void register(
      @WebParam(name = "consumerName", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") String consumerName,
      @WebParam(name = "consumerAgent", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") String consumerAgent,
      @WebParam(name = "methodGetSupported", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") boolean methodGetSupported,
      @WebParam(name = "consumerModes", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> consumerModes,
      @WebParam(name = "consumerWindowStates", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> consumerWindowStates,
      @WebParam(name = "consumerUserScopes", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> consumerUserScopes,
      @WebParam(name = "customUserProfileData", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> customUserProfileData,
      @WebParam(name = "registrationProperties", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<Property> registrationProperties,
      @WebParam(mode = WebParam.Mode.INOUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<Extension>> extensions,
      @WebParam(mode = WebParam.Mode.OUT, name = "registrationHandle", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<String> registrationHandle,
      @WebParam(mode = WebParam.Mode.OUT, name = "registrationState", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<byte[]> registrationState)
      throws MissingParameters, OperationFailed
   {
      RegistrationData registrationData = new RegistrationData();
      registrationData.setConsumerName(consumerName);
      registrationData.setConsumerAgent(consumerAgent);
      registrationData.getConsumerModes().addAll(consumerModes);
      registrationData.getConsumerWindowStates().addAll(consumerWindowStates);
      registrationData.getConsumerUserScopes().addAll(consumerUserScopes);
//      registrationData.getCustomUserProfileData().addAll(customUserProfileData);
      registrationData.getRegistrationProperties().addAll(registrationProperties);
      registrationData.getExtensions().addAll(extensions.value);

      RegistrationContext registrationContext = producer.register(registrationData);

      registrationHandle.value = registrationContext.getRegistrationHandle();
      registrationState.value = registrationContext.getRegistrationState();
      extensions.value = registrationContext.getExtensions();
   }

   public void modifyRegistration(
      @WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") RegistrationContext registrationContext,
      @WebParam(name = "registrationData", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") RegistrationData registrationData,
      @WebParam(mode = WebParam.Mode.OUT, name = "registrationState", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<byte[]> registrationState,
      @WebParam(mode = WebParam.Mode.OUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<Extension>> extensions)
      throws MissingParameters, InvalidRegistration, OperationFailed
   {
      ModifyRegistration modifyRegistration = new ModifyRegistration();
      modifyRegistration.setRegistrationContext(registrationContext);
      modifyRegistration.setRegistrationData(registrationData);

      RegistrationState result = producer.modifyRegistration(modifyRegistration);

      // it is possible (if not likely) that result of modifyRegistration be null
      if (result != null)
      {
         registrationState.value = result.getRegistrationState();
         extensions.value = result.getExtensions();
      }
   }

   public List<Extension> deregister(
      @WebParam(name = "registrationHandle", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") String registrationHandle,
      @WebParam(name = "registrationState", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") byte[] registrationState,
      @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<Extension> extensions)
      throws InvalidRegistration, OperationFailed
   {
      RegistrationContext rc = new RegistrationContext();
      rc.setRegistrationHandle(registrationHandle);
      rc.setRegistrationState(registrationState);

      producer.deregister(rc);
      return null;
   }

   public void register(
      @WebParam(name = "registrationData", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationData registrationData,
      @WebParam(name = "lifetime", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") Lifetime lifetime,
      @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext,
      @WebParam(name = "registrationState", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<byte[]> registrationState,
      @WebParam(name = "scheduledDestruction", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<Lifetime> scheduledDestruction,
      @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions,
      @WebParam(name = "registrationHandle", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<String> registrationHandle)
      throws MissingParameters, OperationFailed, OperationNotSupported
   {
      //To change body of implemented methods use File | Settings | File Templates.
   }

   public List<Extension> deregister(
      @WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext,
      @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext)
      throws InvalidRegistration, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
   }

   public void modifyRegistration(
      @WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext,
      @WebParam(name = "registrationData", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationData registrationData,
      @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext,
      @WebParam(name = "registrationState", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<byte[]> registrationState,
      @WebParam(name = "scheduledDestruction", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<Lifetime> scheduledDestruction,
      @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions)
      throws InvalidRegistration, MissingParameters, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      //To change body of implemented methods use File | Settings | File Templates.
   }

   public Lifetime getRegistrationLifetime(
      @WebParam(name = "getRegistrationLifetime", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", partName = "getRegistrationLifetime") GetRegistrationLifetime getRegistrationLifetime)
      throws AccessDenied, InvalidHandle, InvalidRegistration, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
   }

   public Lifetime setRegistrationLifetime(
      @WebParam(name = "setRegistrationLifetime", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", partName = "setRegistrationLifetime") SetRegistrationLifetime setRegistrationLifetime)
      throws AccessDenied, InvalidHandle, InvalidRegistration, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
   }
}
