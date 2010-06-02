/*
* JBoss, a division of Red Hat
* Copyright 2008, Red Hat Middleware, LLC, and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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

import org.gatein.common.NotYetImplemented;
import org.gatein.wsrp.services.ServiceDescriptionService;
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
      throw new NotYetImplemented();
      /*service.getServiceDescription(registrationContext, desiredLocales, portletHandles, userContext,
         requiresRegistration, offeredPortlets, userCategoryDescriptions, extensionDescriptions,
         customWindowStateDescriptions, customModeDescriptions, requiresInitCookie, registrationPropertyDescription,
         locales, resourceList, eventDescriptions, schemaType, supportedOptions, exportDescription,
         mayReturnRegistrationState, extensions);*/
   }
}
