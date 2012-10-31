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
import org.oasis.wsrp.v2.Deregister;
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
import org.oasis.wsrp.v2.Register;
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
import java.util.Collections;
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
@HandlerChain(file = "../producer-handler-chains.xml")
@Features(features = "org.gatein.wsrp.cxf.WSRPEndpointFeature")
public class RegistrationEndpoint extends WSRPBaseEndpoint implements WSRPV2RegistrationPortType
{
   public void register(
      @WebParam(name = "registrationData", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationData registrationData,
      @WebParam(name = "lifetime", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") Lifetime lifetime,
      @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext,
      @WebParam(name = "registrationState", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<byte[]> registrationState,
      @WebParam(name = "scheduledDestruction", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<Lifetime> scheduledDestruction,
      @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions,
      @WebParam(name = "registrationHandle", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<String> registrationHandle
   ) throws MissingParameters, OperationFailed, OperationNotSupported
   {
      Register register = WSRPTypeFactory.createRegister(registrationData, lifetime, userContext);

      RegistrationContext registrationContext = producer.register(register);

      registrationHandle.value = registrationContext.getRegistrationHandle();
      registrationState.value = registrationContext.getRegistrationState();
      scheduledDestruction.value = registrationContext.getScheduledDestruction();
      extensions.value = registrationContext.getExtensions();
   }

   public List<Extension> deregister(
      @WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext,
      @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext
   ) throws InvalidRegistration, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      Deregister deregister = WSRPTypeFactory.createDeregister(registrationContext, userContext);

      producer.deregister(deregister);

      return Collections.emptyList();
   }

   public void modifyRegistration(
      @WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext,
      @WebParam(name = "registrationData", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationData registrationData,
      @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext,
      @WebParam(name = "registrationState", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<byte[]> registrationState,
      @WebParam(name = "scheduledDestruction", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<Lifetime> scheduledDestruction,
      @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions
   ) throws InvalidRegistration, MissingParameters, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      ModifyRegistration modifyRegistration = WSRPTypeFactory.createModifyRegistration(registrationContext, registrationData);
      modifyRegistration.setUserContext(userContext);

      RegistrationState result = producer.modifyRegistration(modifyRegistration);

      // it is possible (if not likely) that result of modifyRegistration be null
      if (result != null)
      {
         registrationState.value = result.getRegistrationState();
         scheduledDestruction.value = result.getScheduledDestruction();
         extensions.value = result.getExtensions();
      }
   }

   public Lifetime getRegistrationLifetime(
      @WebParam(name = "getRegistrationLifetime", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", partName = "getRegistrationLifetime") GetRegistrationLifetime getRegistrationLifetime
   ) throws AccessDenied, InvalidHandle, InvalidRegistration, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      return producer.getRegistrationLifetime(getRegistrationLifetime);
   }

   public Lifetime setRegistrationLifetime(
      @WebParam(name = "setRegistrationLifetime", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", partName = "setRegistrationLifetime") SetRegistrationLifetime setRegistrationLifetime
   ) throws AccessDenied, InvalidHandle, InvalidRegistration, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      return producer.setRegistrationLifetime(setRegistrationLifetime);
   }
}
