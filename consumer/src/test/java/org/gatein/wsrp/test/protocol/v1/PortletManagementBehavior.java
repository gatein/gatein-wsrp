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

package org.gatein.wsrp.test.protocol.v1;

import org.oasis.wsrp.v1.V1AccessDenied;
import org.oasis.wsrp.v1.V1DestroyFailed;
import org.oasis.wsrp.v1.V1Extension;
import org.oasis.wsrp.v1.V1InconsistentParameters;
import org.oasis.wsrp.v1.V1InvalidHandle;
import org.oasis.wsrp.v1.V1InvalidRegistration;
import org.oasis.wsrp.v1.V1InvalidUserCategory;
import org.oasis.wsrp.v1.V1MissingParameters;
import org.oasis.wsrp.v1.V1ModelDescription;
import org.oasis.wsrp.v1.V1OperationFailed;
import org.oasis.wsrp.v1.V1PortletContext;
import org.oasis.wsrp.v1.V1PortletDescription;
import org.oasis.wsrp.v1.V1Property;
import org.oasis.wsrp.v1.V1PropertyList;
import org.oasis.wsrp.v1.V1RegistrationContext;
import org.oasis.wsrp.v1.V1ResetProperty;
import org.oasis.wsrp.v1.V1ResourceList;
import org.oasis.wsrp.v1.V1UserContext;
import org.oasis.wsrp.v1.WSRPV1PortletManagementPortType;

import javax.jws.WebParam;
import javax.xml.ws.Holder;
import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com?subject=org.gatein.wsrp.test.PortletManagementBehavior">Chris
 *         Laprun</a>
 * @version $Revision: 8784 $
 * @since 2.6
 */
public class PortletManagementBehavior extends TestProducerBehavior implements WSRPV1PortletManagementPortType
{
   public void getPortletDescription(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1PortletContext portletContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1UserContext userContext, @WebParam(name = "desiredLocales", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> desiredLocales, @WebParam(name = "portletDescription", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types", mode = WebParam.Mode.OUT) Holder<V1PortletDescription> portletDescription, @WebParam(name = "resourceList", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types", mode = WebParam.Mode.OUT) Holder<V1ResourceList> resourceList, @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types", mode = WebParam.Mode.OUT) Holder<List<V1Extension>> extensions) throws V1AccessDenied, V1InconsistentParameters, V1InvalidHandle, V1InvalidRegistration, V1InvalidUserCategory, V1MissingParameters, V1OperationFailed
   {
      incrementCallCount();
   }

   public void clonePortlet(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1PortletContext portletContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1UserContext userContext, @WebParam(name = "portletHandle", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types", mode = WebParam.Mode.OUT) Holder<String> portletHandle, @WebParam(name = "portletState", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types", mode = WebParam.Mode.OUT) Holder<byte[]> portletState, @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types", mode = WebParam.Mode.OUT) Holder<List<V1Extension>> extensions) throws V1AccessDenied, V1InconsistentParameters, V1InvalidHandle, V1InvalidRegistration, V1InvalidUserCategory, V1MissingParameters, V1OperationFailed
   {
      incrementCallCount();
   }

   public void destroyPortlets(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1RegistrationContext registrationContext, @WebParam(name = "portletHandles", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> portletHandles, @WebParam(name = "destroyFailed", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types", mode = WebParam.Mode.OUT) Holder<List<V1DestroyFailed>> destroyFailed, @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types", mode = WebParam.Mode.OUT) Holder<List<V1Extension>> extensions) throws V1InconsistentParameters, V1InvalidRegistration, V1MissingParameters, V1OperationFailed
   {
      incrementCallCount();
   }

   public void setPortletProperties(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1PortletContext portletContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1UserContext userContext, @WebParam(name = "propertyList", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1PropertyList propertyList, @WebParam(name = "portletHandle", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types", mode = WebParam.Mode.OUT) Holder<String> portletHandle, @WebParam(name = "portletState", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types", mode = WebParam.Mode.OUT) Holder<byte[]> portletState, @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types", mode = WebParam.Mode.OUT) Holder<List<V1Extension>> extensions) throws V1AccessDenied, V1InconsistentParameters, V1InvalidHandle, V1InvalidRegistration, V1InvalidUserCategory, V1MissingParameters, V1OperationFailed
   {
      incrementCallCount();
   }

   public void getPortletProperties(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1PortletContext portletContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1UserContext userContext, @WebParam(name = "names", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> names, @WebParam(name = "properties", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types", mode = WebParam.Mode.OUT) Holder<List<V1Property>> properties, @WebParam(name = "resetProperties", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types", mode = WebParam.Mode.OUT) Holder<List<V1ResetProperty>> resetProperties, @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types", mode = WebParam.Mode.OUT) Holder<List<V1Extension>> extensions) throws V1AccessDenied, V1InconsistentParameters, V1InvalidHandle, V1InvalidRegistration, V1InvalidUserCategory, V1MissingParameters, V1OperationFailed
   {
      incrementCallCount();
   }

   public void getPortletPropertyDescription(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1PortletContext portletContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1UserContext userContext, @WebParam(name = "desiredLocales", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> desiredLocales, @WebParam(name = "modelDescription", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types", mode = WebParam.Mode.OUT) Holder<V1ModelDescription> modelDescription, @WebParam(name = "resourceList", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types", mode = WebParam.Mode.OUT) Holder<V1ResourceList> resourceList, @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types", mode = WebParam.Mode.OUT) Holder<List<V1Extension>> extensions) throws V1AccessDenied, V1InconsistentParameters, V1InvalidHandle, V1InvalidRegistration, V1InvalidUserCategory, V1MissingParameters, V1OperationFailed
   {
      incrementCallCount();
   }
}
