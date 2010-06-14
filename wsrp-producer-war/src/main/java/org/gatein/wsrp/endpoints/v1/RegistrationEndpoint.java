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

import org.gatein.common.util.ParameterValidation;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.WSRPUtils;
import org.gatein.wsrp.endpoints.WSRPBaseEndpoint;
import org.gatein.wsrp.spec.v1.V1ToV2Converter;
import org.gatein.wsrp.spec.v1.V2ToV1Converter;
import org.oasis.wsrp.v1.V1Extension;
import org.oasis.wsrp.v1.V1InvalidRegistration;
import org.oasis.wsrp.v1.V1MissingParameters;
import org.oasis.wsrp.v1.V1OperationFailed;
import org.oasis.wsrp.v1.V1Property;
import org.oasis.wsrp.v1.V1RegistrationContext;
import org.oasis.wsrp.v1.V1RegistrationData;
import org.oasis.wsrp.v1.WSRPV1RegistrationPortType;
import org.oasis.wsrp.v2.InvalidRegistration;
import org.oasis.wsrp.v2.MissingParameters;
import org.oasis.wsrp.v2.ModifyRegistration;
import org.oasis.wsrp.v2.OperationFailed;
import org.oasis.wsrp.v2.RegistrationContext;
import org.oasis.wsrp.v2.RegistrationData;
import org.oasis.wsrp.v2.RegistrationState;

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
   name = "WSRPV1RegistrationPortType",
   serviceName = "WSRPService",
   portName = "WSRPRegistrationService",
   targetNamespace = "urn:oasis:names:tc:wsrp:v1:wsdl",
   wsdlLocation = "/WEB-INF/wsdl/wsrp_services.wsdl",
   endpointInterface = "org.oasis.wsrp.v1.WSRPV1RegistrationPortType"
)
@HandlerChain(file = "wshandlers.xml")
public class RegistrationEndpoint extends WSRPBaseEndpoint implements WSRPV1RegistrationPortType
{

   public void register(
      @WebParam(name = "consumerName", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") String consumerName,
      @WebParam(name = "consumerAgent", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") String consumerAgent,
      @WebParam(name = "methodGetSupported", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") boolean methodGetSupported,
      @WebParam(name = "consumerModes", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> consumerModes,
      @WebParam(name = "consumerWindowStates", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> consumerWindowStates,
      @WebParam(name = "consumerUserScopes", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> consumerUserScopes,
      @WebParam(name = "customUserProfileData", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> customUserProfileData,
      @WebParam(name = "registrationProperties", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<V1Property> registrationProperties,
      @WebParam(mode = WebParam.Mode.INOUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<V1Extension>> extensions,
      @WebParam(mode = WebParam.Mode.OUT, name = "registrationHandle", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<String> registrationHandle,
      @WebParam(mode = WebParam.Mode.OUT, name = "registrationState", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<byte[]> registrationState)
      throws V1MissingParameters, V1OperationFailed
   {
      try
      {
         RegistrationData registrationData = WSRPTypeFactory.createRegistrationData(consumerName, methodGetSupported);
         registrationData.setConsumerAgent(consumerAgent);
         if (ParameterValidation.existsAndIsNotEmpty(consumerModes))
         {
            registrationData.getConsumerModes().addAll(consumerModes);
         }
         if (ParameterValidation.existsAndIsNotEmpty(consumerWindowStates))
         {
            registrationData.getConsumerWindowStates().addAll(consumerWindowStates);
         }
         if (ParameterValidation.existsAndIsNotEmpty(consumerUserScopes))
         {
            registrationData.getConsumerUserScopes().addAll(consumerUserScopes);
         }
//      registrationData.getCustomUserProfileData().addAll(customUserProfileData);
         if (ParameterValidation.existsAndIsNotEmpty(registrationProperties))
         {
            registrationData.getRegistrationProperties().addAll(WSRPUtils.transform(registrationProperties, V1ToV2Converter.PROPERTY));
         }
         if (ParameterValidation.existsAndIsNotEmpty(extensions.value))
         {
            registrationData.getExtensions().addAll(WSRPUtils.transform(extensions.value, V1ToV2Converter.EXTENSION));
         }

         RegistrationContext registrationContext = producer.register(registrationData);
         registrationHandle.value = registrationContext.getRegistrationHandle();
         registrationState.value = registrationContext.getRegistrationState();
         extensions.value = WSRPUtils.transform(registrationContext.getExtensions(), V2ToV1Converter.EXTENSION);
      }
      catch (MissingParameters missingParameters)
      {
         throw V2ToV1Converter.toV1Exception(V1MissingParameters.class, missingParameters);
      }
      catch (OperationFailed operationFailed)
      {
         throw V2ToV1Converter.toV1Exception(V1OperationFailed.class, operationFailed);
      }
   }

   public void deregister(
      @WebParam(name = "registrationHandle", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") String registrationHandle,
      @WebParam(name = "registrationState", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") byte[] registrationState,
      @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types", mode = WebParam.Mode.INOUT) Holder<List<V1Extension>> extensions
   ) throws V1InvalidRegistration, V1OperationFailed
   {
      try
      {
         RegistrationContext rc = WSRPTypeFactory.createRegistrationContext(registrationHandle);
         rc.setRegistrationState(registrationState);
         producer.deregister(rc);
      }
      catch (InvalidRegistration invalidRegistration)
      {
         throw V2ToV1Converter.toV1Exception(V1InvalidRegistration.class, invalidRegistration);
      }
      catch (OperationFailed operationFailed)
      {
         throw V2ToV1Converter.toV1Exception(V1OperationFailed.class, operationFailed);
      }
   }

   public void modifyRegistration(
      @WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1RegistrationContext registrationContext,
      @WebParam(name = "registrationData", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1RegistrationData registrationData,
      @WebParam(mode = WebParam.Mode.OUT, name = "registrationState", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<byte[]> registrationState,
      @WebParam(mode = WebParam.Mode.OUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<V1Extension>> extensions)
      throws V1MissingParameters, V1InvalidRegistration, V1OperationFailed
   {

      try
      {
         ModifyRegistration modifyRegistration = WSRPTypeFactory.createModifyRegistration(
            V1ToV2Converter.toV2RegistrationContext(registrationContext),
            V1ToV2Converter.toV2RegistrationData(registrationData));

         RegistrationState result = producer.modifyRegistration(modifyRegistration);

         // it is possible (if not likely) that result of modifyRegistration be null
         if (result != null)
         {
            registrationState.value = result.getRegistrationState();
            extensions.value = WSRPUtils.transform(result.getExtensions(), V2ToV1Converter.EXTENSION);
         }
      }
      catch (MissingParameters missingParameters)
      {
         throw V2ToV1Converter.toV1Exception(V1MissingParameters.class, missingParameters);
      }
      catch (InvalidRegistration invalidRegistration)
      {
         throw V2ToV1Converter.toV1Exception(V1InvalidRegistration.class, invalidRegistration);
      }
      catch (OperationFailed operationFailed)
      {
         throw V2ToV1Converter.toV1Exception(V1OperationFailed.class, operationFailed);
      }
   }
}
