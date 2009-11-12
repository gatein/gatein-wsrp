/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2006, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/

package org.gatein.wsrp.test.protocol.v1;

import org.oasis.wsrp.v1.Extension;
import org.oasis.wsrp.v1.InvalidRegistration;
import org.oasis.wsrp.v1.MissingParameters;
import org.oasis.wsrp.v1.OperationFailed;
import org.oasis.wsrp.v1.Property;
import org.oasis.wsrp.v1.RegistrationContext;
import org.oasis.wsrp.v1.RegistrationData;
import org.oasis.wsrp.v1.WSRPV1RegistrationPortType;

import javax.jws.WebParam;
import javax.xml.ws.Holder;
import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com?subject=org.gatein.wsrp.test.RegistrationBehavior">Chris Laprun</a>
 * @version $Revision: 8784 $
 * @since 2.6
 */
public class RegistrationBehavior extends TestProducerBehavior implements WSRPV1RegistrationPortType
{
   public static final String REGISTRATION_HANDLE = "registration";

   public void register(@WebParam(name = "consumerName", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") String consumerName, @WebParam(name = "consumerAgent", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") String consumerAgent, @WebParam(name = "methodGetSupported", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") boolean methodGetSupported, @WebParam(name = "consumerModes", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> consumerModes, @WebParam(name = "consumerWindowStates", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> consumerWindowStates, @WebParam(name = "consumerUserScopes", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> consumerUserScopes, @WebParam(name = "customUserProfileData", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> customUserProfileData, @WebParam(name = "registrationProperties", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<Property> registrationProperties, @WebParam(mode = WebParam.Mode.INOUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<Extension>> extensions, @WebParam(mode = WebParam.Mode.OUT, name = "registrationHandle", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<String> registrationHandle, @WebParam(mode = WebParam.Mode.OUT, name = "registrationState", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<byte[]> registrationState) throws MissingParameters, OperationFailed
   {
      incrementCallCount();
      registrationHandle.value = REGISTRATION_HANDLE;
   }

   public void modifyRegistration(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") RegistrationContext registrationContext, @WebParam(name = "registrationData", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") RegistrationData registrationData, @WebParam(mode = WebParam.Mode.OUT, name = "registrationState", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<byte[]> registrationState, @WebParam(mode = WebParam.Mode.OUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<Extension>> extensions) throws MissingParameters, InvalidRegistration, OperationFailed
   {
      incrementCallCount();
   }

   public List<Extension> deregister(@WebParam(name = "registrationHandle", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") String registrationHandle, @WebParam(name = "registrationState", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") byte[] registrationState, @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<Extension> extensions) throws InvalidRegistration, OperationFailed
   {
      incrementCallCount();
      return null;
   }
}
