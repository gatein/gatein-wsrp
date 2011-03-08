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

import org.gatein.wsrp.WSRPConstants;
import org.gatein.wsrp.spec.v1.WSRP1TypeFactory;
import org.oasis.wsrp.v1.V1CookieProtocol;
import org.oasis.wsrp.v1.V1Extension;
import org.oasis.wsrp.v1.V1InvalidRegistration;
import org.oasis.wsrp.v1.V1ItemDescription;
import org.oasis.wsrp.v1.V1ModelDescription;
import org.oasis.wsrp.v1.V1OperationFailed;
import org.oasis.wsrp.v1.V1PortletDescription;
import org.oasis.wsrp.v1.V1PropertyDescription;
import org.oasis.wsrp.v1.V1RegistrationContext;
import org.oasis.wsrp.v1.V1ResourceList;
import org.oasis.wsrp.v1.V1ServiceDescription;
import org.oasis.wsrp.v1.WSRPV1ServiceDescriptionPortType;

import javax.jws.WebParam;
import javax.xml.ws.Holder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:chris.laprun@jboss.com?subject=org.gatein.wsrp.test.ServiceDescriptionBehavior">Chris
 *         Laprun</a>
 * @version $Revision: 11320 $
 * @since 2.6
 */
public class ServiceDescriptionBehavior extends TestProducerBehavior implements WSRPV1ServiceDescriptionPortType
{
   protected V1ServiceDescription serviceDescription;
   private static final V1ServiceDescription DEFAULT_SERVICE_DESCRIPTION = WSRP1TypeFactory.createServiceDescription(false);
   public static final ServiceDescriptionBehavior DEFAULT = new ServiceDescriptionBehavior();

   protected List<V1PortletDescription> offeredPortlets;
   private boolean requiresRegistration;
   private V1CookieProtocol cookieProtocol;
   private V1ModelDescription registrationProperties;

   public ServiceDescriptionBehavior()
   {
      offeredPortlets = new LinkedList<V1PortletDescription>();
   }

   public void setRequiresRegistration(boolean requiresRegistration)
   {
      this.requiresRegistration = requiresRegistration;
   }

   public void setRequiresInitCookie(V1CookieProtocol requiresInitCookie)
   {
      this.cookieProtocol = requiresInitCookie;
   }

   public void setServiceDescription(boolean requiresRegistration, int numberOfProps)
   {
      V1ServiceDescription sd = createServiceDescription(requiresRegistration, numberOfProps);
      offeredPortlets = sd.getOfferedPortlets();
      this.requiresRegistration = sd.isRequiresRegistration();
      registrationProperties = sd.getRegistrationPropertyDescription();
   }

   public static V1ServiceDescription getDefaultServiceDescription()
   {
      return DEFAULT_SERVICE_DESCRIPTION;
   }

   public void addPortletDescription(V1PortletDescription portletDescription)
   {
      offeredPortlets.add(portletDescription);
   }

   public Set<String> getPortletHandles()
   {
      Set<String> handles = new HashSet<String>(offeredPortlets.size());

      for (V1PortletDescription description : offeredPortlets)
      {
         handles.add(description.getPortletHandle());
      }

      return handles;
   }

   public int getPortletNumber()
   {
      return offeredPortlets.size();
   }

   public static V1ServiceDescription createServiceDescription(boolean requiresRegistration, int numberOfProperties)
   {
      V1ServiceDescription sd = WSRP1TypeFactory.createServiceDescription(requiresRegistration);

      if (requiresRegistration)
      {
         List<V1PropertyDescription> descriptions = new ArrayList<V1PropertyDescription>(numberOfProperties);
         for (int i = 0; i < numberOfProperties; i++)
         {
            descriptions.add(WSRP1TypeFactory.createPropertyDescription("prop" + i, WSRPConstants.XSD_STRING));
         }
         sd.setRegistrationPropertyDescription(WSRP1TypeFactory.createModelDescription(descriptions));
      }

      return sd;
   }

   public void getServiceDescription(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1RegistrationContext registrationContext,
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
                                     @WebParam(mode = WebParam.Mode.OUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<V1Extension>> extensions)
      throws V1InvalidRegistration, V1OperationFailed
   {
      incrementCallCount();
      offeredPortlets.value = this.offeredPortlets;
      requiresRegistration.value = this.requiresRegistration;
      requiresInitCookie.value = this.cookieProtocol;
      registrationPropertyDescription.value = registrationProperties;
   }
}
