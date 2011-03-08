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

package org.gatein.wsrp.services.v1;

import org.gatein.wsrp.WSRPUtils;
import org.gatein.wsrp.services.ServiceDescriptionService;
import org.gatein.wsrp.spec.v1.V1ToV2Converter;
import org.gatein.wsrp.spec.v1.V2ToV1Converter;
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
import org.oasis.wsrp.v2.RegistrationContext;
import org.oasis.wsrp.v2.ResourceList;
import org.oasis.wsrp.v2.ResourceSuspended;
import org.oasis.wsrp.v2.UserContext;

import javax.xml.ws.Holder;
import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class V1ServiceDescriptionService extends ServiceDescriptionService<WSRPV1ServiceDescriptionPortType>
{
   public V1ServiceDescriptionService(WSRPV1ServiceDescriptionPortType port)
   {
      super(port);
   }

   @Override
   public void getServiceDescription(
      RegistrationContext registrationContext, List<String> desiredLocales, List<String> portletHandles,
      UserContext userContext, Holder<Boolean> requiresRegistration, Holder<List<PortletDescription>> offeredPortlets,
      Holder<List<ItemDescription>> userCategoryDescriptions, Holder<List<ExtensionDescription>> extensionDescriptions,
      Holder<List<ItemDescription>> customWindowStateDescriptions, Holder<List<ItemDescription>> customModeDescriptions,
      Holder<CookieProtocol> requiresInitCookie, Holder<ModelDescription> registrationPropertyDescription,
      Holder<List<String>> locales, Holder<ResourceList> resourceList, Holder<List<EventDescription>> eventDescriptions,
      Holder<ModelTypes> schemaType, Holder<List<String>> supportedOptions, Holder<ExportDescription> exportDescription,
      Holder<Boolean> mayReturnRegistrationState, Holder<List<Extension>> extensions)
      throws InvalidRegistration, ModifyRegistrationRequired, OperationFailed, ResourceSuspended
   {
      try
      {
         V1RegistrationContext v1RegistrationContext = V2ToV1Converter.toV1RegistrationContext(registrationContext);
         Holder<List<V1PortletDescription>> v1OfferedPortlets = new Holder<List<V1PortletDescription>>();
         Holder<List<V1ItemDescription>> v1UserCategories = new Holder<List<V1ItemDescription>>();
         Holder<List<V1ItemDescription>> v1ProfileITems = new Holder<List<V1ItemDescription>>();
         Holder<List<V1ItemDescription>> v1WindowStates = new Holder<List<V1ItemDescription>>();
         Holder<List<V1ItemDescription>> v1Modes = new Holder<List<V1ItemDescription>>();
         Holder<V1CookieProtocol> v1Cookie = new Holder<V1CookieProtocol>();
         Holder<V1ModelDescription> v1RegistrationProperties = new Holder<V1ModelDescription>();
         Holder<V1ResourceList> v1Resources = new Holder<V1ResourceList>();
         Holder<List<V1Extension>> v1Extensions = new Holder<List<V1Extension>>();

         service.getServiceDescription(v1RegistrationContext, desiredLocales, requiresRegistration, v1OfferedPortlets,
            v1UserCategories,
            v1ProfileITems,
            v1WindowStates,
            v1Modes,
            v1Cookie, v1RegistrationProperties, locales, v1Resources, v1Extensions);

         offeredPortlets.value = WSRPUtils.transform(v1OfferedPortlets.value, V1ToV2Converter.PORTLETDESCRIPTION);
         userCategoryDescriptions.value = WSRPUtils.transform(v1UserCategories.value, V1ToV2Converter.ITEMDESCRIPTION);
//      customUserProfileItemDescriptions.value = description.getCustomUserProfileItemDescriptions();
         customWindowStateDescriptions.value = WSRPUtils.transform(v1WindowStates.value, V1ToV2Converter.ITEMDESCRIPTION);
         customModeDescriptions.value = WSRPUtils.transform(v1Modes.value, V1ToV2Converter.ITEMDESCRIPTION);
         requiresInitCookie.value = V1ToV2Converter.toV2CookieProtocol(v1Cookie.value);
         registrationPropertyDescription.value = V1ToV2Converter.toV2ModelDescription(v1RegistrationProperties.value);
         resourceList.value = V1ToV2Converter.toV2ResourceList(v1Resources.value);
         extensions.value = WSRPUtils.transform(v1Extensions.value, V1ToV2Converter.EXTENSION);
      }
      catch (V1InvalidRegistration v1InvalidRegistration)
      {
         throw V1ToV2Converter.toV2Exception(InvalidRegistration.class, v1InvalidRegistration);
      }
      catch (V1OperationFailed v1OperationFailed)
      {
         throw V1ToV2Converter.toV2Exception(OperationFailed.class, v1OperationFailed);
      }
   }
}
