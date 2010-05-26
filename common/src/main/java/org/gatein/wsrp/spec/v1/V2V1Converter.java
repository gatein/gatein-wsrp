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

package org.gatein.wsrp.spec.v1;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.gatein.common.NotYetImplemented;
import org.gatein.pc.api.OpaqueStateString;
import org.gatein.wsrp.WSRPTypeFactory;
import org.oasis.wsrp.v1.V1ClientData;
import org.oasis.wsrp.v1.V1Extension;
import org.oasis.wsrp.v1.V1MarkupContext;
import org.oasis.wsrp.v1.V1MarkupParams;
import org.oasis.wsrp.v1.V1PortletContext;
import org.oasis.wsrp.v1.V1PortletDescription;
import org.oasis.wsrp.v1.V1RegistrationContext;
import org.oasis.wsrp.v1.V1RuntimeContext;
import org.oasis.wsrp.v1.V1ServiceDescription;
import org.oasis.wsrp.v1.V1UserContext;
import org.oasis.wsrp.v2.ClientData;
import org.oasis.wsrp.v2.Extension;
import org.oasis.wsrp.v2.MarkupContext;
import org.oasis.wsrp.v2.MarkupParams;
import org.oasis.wsrp.v2.PortletContext;
import org.oasis.wsrp.v2.PortletDescription;
import org.oasis.wsrp.v2.RegistrationContext;
import org.oasis.wsrp.v2.RuntimeContext;
import org.oasis.wsrp.v2.ServiceDescription;
import org.oasis.wsrp.v2.UserContext;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class V2V1Converter
{
   private static final V1ToV2ExtensionFunction V1_TO_V2_EXTENSION_FUNCTION = new V1ToV2ExtensionFunction();

   public static V1PortletDescription toV1PortletDescription(PortletDescription portletDescription)
   {
      throw new NotYetImplemented();
   }

   public static V1PortletContext toV1PortletContext(PortletContext portletContext)
   {
      throw new NotYetImplemented();
   }

   public static MarkupParams toV2MarkupParams(V1MarkupParams v1MarkupParams)
   {
      MarkupParams markupParams = WSRPTypeFactory.createMarkupParams(v1MarkupParams.isSecureClientCommunication(),
         v1MarkupParams.getLocales(), v1MarkupParams.getMimeTypes(), v1MarkupParams.getMode(),
         v1MarkupParams.getWindowState());
      markupParams.setClientData(toV2ClientData(v1MarkupParams.getClientData()));
      markupParams.setNavigationalContext(WSRPTypeFactory.createNavigationalContextOrNull(
         new OpaqueStateString(v1MarkupParams.getNavigationalState()), null));
      markupParams.setValidateTag(v1MarkupParams.getValidateTag());

      markupParams.getMarkupCharacterSets().addAll(v1MarkupParams.getMarkupCharacterSets());
      markupParams.getValidNewModes().addAll(v1MarkupParams.getValidNewModes());
      markupParams.getValidNewWindowStates().addAll(v1MarkupParams.getValidNewWindowStates());

      markupParams.getExtensions().addAll(Lists.transform(v1MarkupParams.getExtensions(), V1_TO_V2_EXTENSION_FUNCTION));
      return markupParams;
   }

   private static ClientData toV2ClientData(V1ClientData clientData)
   {
      throw new NotYetImplemented();
   }

   public static PortletContext toV2PortletContext(V1PortletContext portletContext)
   {
      throw new NotYetImplemented();
   }

   public static RegistrationContext toV2RegistrationContext(V1RegistrationContext registrationContext)
   {
      throw new NotYetImplemented();
   }

   public static RuntimeContext toV2RuntimeContext(V1RuntimeContext runtimeContext)
   {
      throw new NotYetImplemented();
   }

   public static UserContext toV2UserContext(V1UserContext userContext)
   {
      throw new NotYetImplemented();
   }

   public static V1MarkupContext toV1MarkupContext(MarkupContext markupContext)
   {
      throw new NotYetImplemented();
   }

   public static ServiceDescription toV2ServiceDescription(V1ServiceDescription v1ServiceDescription)
   {
      throw new NotYetImplemented();
   }

   private static class V1ToV2ExtensionFunction implements Function<V1Extension, Extension>
   {
      public Extension apply(V1Extension from)
      {
         if (from == null)
         {
            return null;
         }
         else
         {
            Extension extension = new Extension();
            extension.setAny(from.getAny());
            return extension;
         }
      }
   }
}
