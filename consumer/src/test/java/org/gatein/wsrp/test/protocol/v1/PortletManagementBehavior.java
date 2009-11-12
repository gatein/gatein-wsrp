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

import org.oasis.wsrp.v1.AccessDenied;
import org.oasis.wsrp.v1.DestroyFailed;
import org.oasis.wsrp.v1.Extension;
import org.oasis.wsrp.v1.InconsistentParameters;
import org.oasis.wsrp.v1.InvalidHandle;
import org.oasis.wsrp.v1.InvalidRegistration;
import org.oasis.wsrp.v1.InvalidUserCategory;
import org.oasis.wsrp.v1.MissingParameters;
import org.oasis.wsrp.v1.ModelDescription;
import org.oasis.wsrp.v1.OperationFailed;
import org.oasis.wsrp.v1.PortletContext;
import org.oasis.wsrp.v1.PortletDescription;
import org.oasis.wsrp.v1.Property;
import org.oasis.wsrp.v1.PropertyList;
import org.oasis.wsrp.v1.RegistrationContext;
import org.oasis.wsrp.v1.ResetProperty;
import org.oasis.wsrp.v1.ResourceList;
import org.oasis.wsrp.v1.UserContext;
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
   public void getPortletPropertyDescription(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") PortletContext portletContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") UserContext userContext, @WebParam(name = "desiredLocales", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> desiredLocales, @WebParam(mode = WebParam.Mode.OUT, name = "modelDescription", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<ModelDescription> modelDescription, @WebParam(mode = WebParam.Mode.OUT, name = "resourceList", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<ResourceList> resourceList, @WebParam(mode = WebParam.Mode.OUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<Extension>> extensions) throws MissingParameters, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory, AccessDenied, OperationFailed
   {
      incrementCallCount();
   }

   public void setPortletProperties(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") PortletContext portletContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") UserContext userContext, @WebParam(name = "propertyList", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") PropertyList propertyList, @WebParam(mode = WebParam.Mode.OUT, name = "portletHandle", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<String> portletHandle, @WebParam(mode = WebParam.Mode.OUT, name = "portletState", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<byte[]> portletState, @WebParam(mode = WebParam.Mode.OUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<Extension>> extensions) throws MissingParameters, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory, AccessDenied, OperationFailed
   {
      incrementCallCount();
   }

   public void clonePortlet(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") PortletContext portletContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") UserContext userContext, @WebParam(mode = WebParam.Mode.OUT, name = "portletHandle", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<String> portletHandle, @WebParam(mode = WebParam.Mode.OUT, name = "portletState", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<byte[]> portletState, @WebParam(mode = WebParam.Mode.OUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<Extension>> extensions) throws MissingParameters, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory, AccessDenied, OperationFailed
   {
      incrementCallCount();
   }

   public void getPortletDescription(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") PortletContext portletContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") UserContext userContext, @WebParam(name = "desiredLocales", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> desiredLocales, @WebParam(mode = WebParam.Mode.OUT, name = "portletDescription", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<PortletDescription> portletDescription, @WebParam(mode = WebParam.Mode.OUT, name = "resourceList", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<ResourceList> resourceList, @WebParam(mode = WebParam.Mode.OUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<Extension>> extensions) throws MissingParameters, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory, AccessDenied, OperationFailed
   {
      incrementCallCount();
   }

   public void getPortletProperties(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") PortletContext portletContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") UserContext userContext, @WebParam(name = "names", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> names, @WebParam(mode = WebParam.Mode.OUT, name = "properties", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<Property>> properties, @WebParam(mode = WebParam.Mode.OUT, name = "resetProperties", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<ResetProperty>> resetProperties, @WebParam(mode = WebParam.Mode.OUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<Extension>> extensions) throws MissingParameters, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory, AccessDenied, OperationFailed
   {
      incrementCallCount();
   }

   public void destroyPortlets(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") RegistrationContext registrationContext, @WebParam(name = "portletHandles", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> portletHandles, @WebParam(mode = WebParam.Mode.OUT, name = "destroyFailed", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<DestroyFailed>> destroyFailed, @WebParam(mode = WebParam.Mode.OUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<Extension>> extensions) throws MissingParameters, InconsistentParameters, InvalidRegistration, OperationFailed
   {
      incrementCallCount();
   }
}
