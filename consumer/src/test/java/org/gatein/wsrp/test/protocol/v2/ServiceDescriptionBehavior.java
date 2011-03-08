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

package org.gatein.wsrp.test.protocol.v2;

import org.gatein.wsrp.WSRPConstants;
import org.gatein.wsrp.WSRPTypeFactory;
import org.oasis.wsrp.v2.CookieProtocol;
import org.oasis.wsrp.v2.EventDescription;
import org.oasis.wsrp.v2.ExportDescription;
import org.oasis.wsrp.v2.Extension;
import org.oasis.wsrp.v2.ExtensionDescription;
import org.oasis.wsrp.v2.InvalidRegistration;
import org.oasis.wsrp.v2.ItemDescription;
import org.oasis.wsrp.v2.ModelDescription;
import org.oasis.wsrp.v2.ModelTypes;
import org.oasis.wsrp.v2.ModifyRegistrationRequired;
import org.oasis.wsrp.v2.OperationFailed;
import org.oasis.wsrp.v2.PortletDescription;
import org.oasis.wsrp.v2.PropertyDescription;
import org.oasis.wsrp.v2.RegistrationContext;
import org.oasis.wsrp.v2.ResourceList;
import org.oasis.wsrp.v2.ResourceSuspended;
import org.oasis.wsrp.v2.ServiceDescription;
import org.oasis.wsrp.v2.UserContext;
import org.oasis.wsrp.v2.WSRPV2ServiceDescriptionPortType;

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
public class ServiceDescriptionBehavior extends TestProducerBehavior implements WSRPV2ServiceDescriptionPortType
{
   protected ServiceDescription serviceDescription;
   private static final ServiceDescription DEFAULT_SERVICE_DESCRIPTION = WSRPTypeFactory.createServiceDescription(false);
   public static final ServiceDescriptionBehavior DEFAULT = new ServiceDescriptionBehavior();

   protected List<PortletDescription> offeredPortlets;
   private boolean requiresRegistration;
   private CookieProtocol cookieProtocol;
   private ModelDescription registrationProperties;

   public ServiceDescriptionBehavior()
   {
      offeredPortlets = new LinkedList<PortletDescription>();
   }

   public void setRequiresRegistration(boolean requiresRegistration)
   {
      this.requiresRegistration = requiresRegistration;
   }

   public void setRequiresInitCookie(CookieProtocol requiresInitCookie)
   {
      this.cookieProtocol = requiresInitCookie;
   }

   public void setServiceDescription(boolean requiresRegistration, int numberOfProps)
   {
      ServiceDescription sd = createServiceDescription(requiresRegistration, numberOfProps);
      offeredPortlets = sd.getOfferedPortlets();
      this.requiresRegistration = sd.isRequiresRegistration();
      registrationProperties = sd.getRegistrationPropertyDescription();
   }

   public static ServiceDescription getDefaultServiceDescription()
   {
      return DEFAULT_SERVICE_DESCRIPTION;
   }

   public void addPortletDescription(PortletDescription portletDescription)
   {
      offeredPortlets.add(portletDescription);
   }

   public Set<String> getPortletHandles()
   {
      Set<String> handles = new HashSet<String>(offeredPortlets.size());

      for (PortletDescription description : offeredPortlets)
      {
         handles.add(description.getPortletHandle());
      }

      return handles;
   }

   public int getPortletNumber()
   {
      return offeredPortlets.size();
   }

   public static ServiceDescription createServiceDescription(boolean requiresRegistration, int numberOfProperties)
   {
      ServiceDescription sd = WSRPTypeFactory.createServiceDescription(requiresRegistration);

      if (requiresRegistration)
      {
         List<PropertyDescription> descriptions = new ArrayList<PropertyDescription>(numberOfProperties);
         for (int i = 0; i < numberOfProperties; i++)
         {
            descriptions.add(WSRPTypeFactory.createPropertyDescription("prop" + i, WSRPConstants.XSD_STRING));
         }
         sd.setRegistrationPropertyDescription(WSRPTypeFactory.createModelDescription(descriptions));
      }

      return sd;
   }

   public void getServiceDescription(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext, @WebParam(name = "desiredLocales", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") List<String> desiredLocales, @WebParam(name = "portletHandles", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") List<String> portletHandles, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext, @WebParam(name = "requiresRegistration", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<Boolean> requiresRegistration, @WebParam(name = "offeredPortlets", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<PortletDescription>> offeredPortlets, @WebParam(name = "userCategoryDescriptions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<ItemDescription>> userCategoryDescriptions, @WebParam(name = "extensionDescriptions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<ExtensionDescription>> extensionDescriptions, @WebParam(name = "customWindowStateDescriptions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<ItemDescription>> customWindowStateDescriptions, @WebParam(name = "customModeDescriptions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<ItemDescription>> customModeDescriptions, @WebParam(name = "requiresInitCookie", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<CookieProtocol> requiresInitCookie, @WebParam(name = "registrationPropertyDescription", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<ModelDescription> registrationPropertyDescription, @WebParam(name = "locales", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<String>> locales, @WebParam(name = "resourceList", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<ResourceList> resourceList, @WebParam(name = "eventDescriptions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<EventDescription>> eventDescriptions, @WebParam(name = "schemaType", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<ModelTypes> schemaType, @WebParam(name = "supportedOptions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<String>> supportedOptions, @WebParam(name = "exportDescription", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<ExportDescription> exportDescription, @WebParam(name = "mayReturnRegistrationState", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<Boolean> mayReturnRegistrationState, @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions) throws InvalidRegistration, ModifyRegistrationRequired, OperationFailed, ResourceSuspended
   {
      incrementCallCount();
      offeredPortlets.value = this.offeredPortlets;
      requiresRegistration.value = this.requiresRegistration;
      requiresInitCookie.value = this.cookieProtocol;
      registrationPropertyDescription.value = registrationProperties;
   }
}
