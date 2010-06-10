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

import org.gatein.common.NotYetImplemented;
import org.gatein.wsrp.WSRPUtils;
import org.gatein.wsrp.services.MarkupService;
import org.gatein.wsrp.spec.v1.V1ToV2Converter;
import org.gatein.wsrp.spec.v1.V2ToV1Converter;
import org.oasis.wsrp.v1.V1AccessDenied;
import org.oasis.wsrp.v1.V1Extension;
import org.oasis.wsrp.v1.V1InconsistentParameters;
import org.oasis.wsrp.v1.V1InvalidCookie;
import org.oasis.wsrp.v1.V1InvalidHandle;
import org.oasis.wsrp.v1.V1InvalidRegistration;
import org.oasis.wsrp.v1.V1InvalidSession;
import org.oasis.wsrp.v1.V1InvalidUserCategory;
import org.oasis.wsrp.v1.V1MarkupContext;
import org.oasis.wsrp.v1.V1MissingParameters;
import org.oasis.wsrp.v1.V1OperationFailed;
import org.oasis.wsrp.v1.V1PortletStateChangeRequired;
import org.oasis.wsrp.v1.V1SessionContext;
import org.oasis.wsrp.v1.V1UnsupportedLocale;
import org.oasis.wsrp.v1.V1UnsupportedMimeType;
import org.oasis.wsrp.v1.V1UnsupportedMode;
import org.oasis.wsrp.v1.V1UnsupportedWindowState;
import org.oasis.wsrp.v1.V1UpdateResponse;
import org.oasis.wsrp.v1.WSRPV1MarkupPortType;
import org.oasis.wsrp.v2.AccessDenied;
import org.oasis.wsrp.v2.EventParams;
import org.oasis.wsrp.v2.Extension;
import org.oasis.wsrp.v2.HandleEventsFailed;
import org.oasis.wsrp.v2.InconsistentParameters;
import org.oasis.wsrp.v2.InteractionParams;
import org.oasis.wsrp.v2.InvalidCookie;
import org.oasis.wsrp.v2.InvalidHandle;
import org.oasis.wsrp.v2.InvalidRegistration;
import org.oasis.wsrp.v2.InvalidSession;
import org.oasis.wsrp.v2.InvalidUserCategory;
import org.oasis.wsrp.v2.MarkupContext;
import org.oasis.wsrp.v2.MarkupParams;
import org.oasis.wsrp.v2.MissingParameters;
import org.oasis.wsrp.v2.ModifyRegistrationRequired;
import org.oasis.wsrp.v2.OperationFailed;
import org.oasis.wsrp.v2.OperationNotSupported;
import org.oasis.wsrp.v2.PortletContext;
import org.oasis.wsrp.v2.PortletStateChangeRequired;
import org.oasis.wsrp.v2.RegistrationContext;
import org.oasis.wsrp.v2.ResourceContext;
import org.oasis.wsrp.v2.ResourceParams;
import org.oasis.wsrp.v2.ResourceSuspended;
import org.oasis.wsrp.v2.RuntimeContext;
import org.oasis.wsrp.v2.SessionContext;
import org.oasis.wsrp.v2.UnsupportedLocale;
import org.oasis.wsrp.v2.UnsupportedMimeType;
import org.oasis.wsrp.v2.UnsupportedMode;
import org.oasis.wsrp.v2.UnsupportedWindowState;
import org.oasis.wsrp.v2.UpdateResponse;
import org.oasis.wsrp.v2.UserContext;

import javax.xml.ws.Holder;
import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class V1MarkupService extends MarkupService<WSRPV1MarkupPortType>
{
   public V1MarkupService(WSRPV1MarkupPortType port)
   {
      super(port);
   }

   @Override
   public void getMarkup(RegistrationContext registrationContext, PortletContext portletContext, RuntimeContext runtimeContext, UserContext userContext, MarkupParams markupParams, Holder<MarkupContext> markupContext, Holder<SessionContext> sessionContext, Holder<List<Extension>> extensions) throws AccessDenied, InconsistentParameters, InvalidCookie, InvalidHandle, InvalidRegistration, InvalidSession, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, ResourceSuspended, UnsupportedLocale, UnsupportedMimeType, UnsupportedMode, UnsupportedWindowState
   {
      try
      {
         service.getMarkup(
            V2ToV1Converter.toV1RegistrationContext(registrationContext),
            V2ToV1Converter.toV1PortletContext(portletContext),
            V2ToV1Converter.toV1RuntimeContext(runtimeContext),
            V2ToV1Converter.toV1UserContext(userContext),
            V2ToV1Converter.toV1MarkupParams(markupParams),
            new Holder<V1MarkupContext>(V2ToV1Converter.toV1MarkupContext(markupContext.value)),
            new Holder<V1SessionContext>(V2ToV1Converter.toV1SessionContext(sessionContext.value)),
            new Holder<List<V1Extension>>(WSRPUtils.transform(extensions.value, V2ToV1Converter.EXTENSION)));
      }
      catch (V1AccessDenied accessDenied)
      {
         throw V2ToV1Converter.toV1Exception(AccessDenied.class, accessDenied);
      }
      catch (V1InconsistentParameters inconsistentParameters)
      {
         throw V2ToV1Converter.toV1Exception(InconsistentParameters.class, inconsistentParameters);
      }
      catch (V1InvalidCookie invalidCookie)
      {
         throw V2ToV1Converter.toV1Exception(InvalidCookie.class, invalidCookie);
      }
      catch (V1InvalidHandle invalidHandle)
      {
         throw V2ToV1Converter.toV1Exception(InvalidHandle.class, invalidHandle);
      }
      catch (V1InvalidRegistration invalidRegistration)
      {
         throw V2ToV1Converter.toV1Exception(InvalidRegistration.class, invalidRegistration);
      }
      catch (V1InvalidSession invalidSession)
      {
         throw V2ToV1Converter.toV1Exception(InvalidSession.class, invalidSession);
      }
      catch (V1InvalidUserCategory invalidUserCategory)
      {
         throw V2ToV1Converter.toV1Exception(InvalidUserCategory.class, invalidUserCategory);
      }
      catch (V1MissingParameters missingParameter)
      {
         throw V2ToV1Converter.toV1Exception(MissingParameters.class, missingParameter);
      }
      catch (V1OperationFailed operationFailed)
      {
         throw V2ToV1Converter.toV1Exception(OperationFailed.class, operationFailed);
      }
      catch (V1UnsupportedLocale unsupportedLocale)
      {
         throw V2ToV1Converter.toV1Exception(UnsupportedLocale.class, unsupportedLocale);
      }
      catch (V1UnsupportedMimeType unsupportedMimeType)
      {
         throw V2ToV1Converter.toV1Exception(UnsupportedMimeType.class, unsupportedMimeType);
      }
      catch (V1UnsupportedMode unsupportedMode)
      {
         throw V2ToV1Converter.toV1Exception(UnsupportedMode.class, unsupportedMode);
      }
      catch (V1UnsupportedWindowState unsupportedWindowState)
      {
         throw V2ToV1Converter.toV1Exception(UnsupportedWindowState.class, unsupportedWindowState);
      }

   }

   @Override
   public void getResource(RegistrationContext registrationContext, Holder<PortletContext> portletContext, RuntimeContext runtimeContext, UserContext userContext, ResourceParams resourceParams, Holder<ResourceContext> resourceContext, Holder<SessionContext> sessionContext, Holder<List<Extension>> extensions) throws AccessDenied, InconsistentParameters, InvalidCookie, InvalidHandle, InvalidRegistration, InvalidSession, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended, UnsupportedLocale, UnsupportedMimeType, UnsupportedMode, UnsupportedWindowState
   {

      throw new NotYetImplemented();
   }

   @Override
   public void performBlockingInteraction(RegistrationContext registrationContext, PortletContext portletContext, RuntimeContext runtimeContext, UserContext userContext, MarkupParams markupParams, InteractionParams interactionParams, Holder<UpdateResponse> updateResponse, Holder<String> redirectURL, Holder<List<Extension>> extensions) throws AccessDenied, InconsistentParameters, InvalidCookie, InvalidHandle, InvalidRegistration, InvalidSession, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, PortletStateChangeRequired, ResourceSuspended, UnsupportedLocale, UnsupportedMimeType, UnsupportedMode, UnsupportedWindowState
   {
      try
      {
         service.performBlockingInteraction(
            V2ToV1Converter.toV1RegistrationContext(registrationContext),
            V2ToV1Converter.toV1PortletContext(portletContext),
            V2ToV1Converter.toV1RuntimeContext(runtimeContext),
            V2ToV1Converter.toV1UserContext(userContext),
            V2ToV1Converter.toV1MarkupParams(markupParams),
            V2ToV1Converter.toV1InteractionParams(interactionParams),
            new Holder<V1UpdateResponse>(V2ToV1Converter.toV1UpdateResponse(updateResponse.value)),
            redirectURL,
            new Holder<List<V1Extension>>(WSRPUtils.transform(extensions.value, V2ToV1Converter.EXTENSION)));
      }
      catch (V1AccessDenied accessDenied)
      {
         throw V2ToV1Converter.toV1Exception(AccessDenied.class, accessDenied);
      }
      catch (V1InconsistentParameters inconsistentParameters)
      {
         throw V2ToV1Converter.toV1Exception(InconsistentParameters.class, inconsistentParameters);
      }
      catch (V1InvalidCookie invalidCookie)
      {
         throw V2ToV1Converter.toV1Exception(InvalidCookie.class, invalidCookie);
      }
      catch (V1InvalidHandle invalidHandle)
      {
         throw V2ToV1Converter.toV1Exception(InvalidHandle.class, invalidHandle);
      }
      catch (V1InvalidRegistration invalidRegistration)
      {
         throw V2ToV1Converter.toV1Exception(InvalidRegistration.class, invalidRegistration);
      }
      catch (V1InvalidSession invalidSession)
      {
         throw V2ToV1Converter.toV1Exception(InvalidSession.class, invalidSession);
      }
      catch (V1InvalidUserCategory invalidUserCategory)
      {
         throw V2ToV1Converter.toV1Exception(InvalidUserCategory.class, invalidUserCategory);
      }
      catch (V1MissingParameters missingParameters)
      {
         throw V2ToV1Converter.toV1Exception(MissingParameters.class, missingParameters);
      }
      catch (V1OperationFailed operationFailed)
      {
         throw V2ToV1Converter.toV1Exception(OperationFailed.class, operationFailed);
      }
      catch (V1PortletStateChangeRequired portletStateChangeRequired)
      {
         throw V2ToV1Converter.toV1Exception(PortletStateChangeRequired.class, portletStateChangeRequired);
      }
      catch (V1UnsupportedLocale unsupportedLocale)
      {
         throw V2ToV1Converter.toV1Exception(UnsupportedLocale.class, unsupportedLocale);
      }
      catch (V1UnsupportedMimeType unsupportedMimeType)
      {
         throw V2ToV1Converter.toV1Exception(UnsupportedMimeType.class, unsupportedMimeType);
      }
      catch (V1UnsupportedMode unsupportedMode)
      {
         throw V2ToV1Converter.toV1Exception(UnsupportedMode.class, unsupportedMode);
      }
      catch (V1UnsupportedWindowState unsupportedWindowState)
      {
         throw V2ToV1Converter.toV1Exception(UnsupportedWindowState.class, unsupportedWindowState);
      }
   }

   @Override
   public void handleEvents(RegistrationContext registrationContext, PortletContext portletContext, RuntimeContext runtimeContext, UserContext userContext, MarkupParams markupParams, EventParams eventParams, Holder<UpdateResponse> updateResponse, Holder<List<HandleEventsFailed>> failedEvents, Holder<List<Extension>> extensions) throws AccessDenied, InconsistentParameters, InvalidCookie, InvalidHandle, InvalidRegistration, InvalidSession, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, PortletStateChangeRequired, ResourceSuspended, UnsupportedLocale, UnsupportedMimeType, UnsupportedMode, UnsupportedWindowState
   {
      throw new NotYetImplemented();
   }

   @Override
   public List<Extension> releaseSessions(RegistrationContext registrationContext, List<String> sessionIDs, UserContext userContext) throws AccessDenied, InvalidRegistration, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      try
      {
         return WSRPUtils.transform(service.releaseSessions(V2ToV1Converter.toV1RegistrationContext(registrationContext), sessionIDs), V1ToV2Converter.EXTENSION);
      }
      catch (V1AccessDenied accessDenied)
      {
         throw V2ToV1Converter.toV1Exception(AccessDenied.class, accessDenied);
      }
      catch (V1InvalidRegistration invalidRegistration)
      {
         throw V2ToV1Converter.toV1Exception(InvalidRegistration.class, invalidRegistration);
      }
      catch (V1MissingParameters missingParameters)
      {
         throw V2ToV1Converter.toV1Exception(MissingParameters.class, missingParameters);
      }
      catch (V1OperationFailed operationFailed)
      {
         throw V2ToV1Converter.toV1Exception(OperationFailed.class, operationFailed);
      }
   }

   @Override
   public List<Extension> initCookie(RegistrationContext registrationContext, UserContext userContext) throws AccessDenied, InvalidRegistration, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      try
      {
         return WSRPUtils.transform(service.initCookie(V2ToV1Converter.toV1RegistrationContext(registrationContext)), V1ToV2Converter.EXTENSION);
      }
      catch (V1AccessDenied accessDenied)
      {
         throw V2ToV1Converter.toV1Exception(AccessDenied.class, accessDenied);
      }
      catch (V1InvalidRegistration invalidRegistration)
      {
         throw V2ToV1Converter.toV1Exception(InvalidRegistration.class, invalidRegistration);
      }
      catch (V1OperationFailed operationFailed)
      {
         throw V2ToV1Converter.toV1Exception(OperationFailed.class, operationFailed);
      }
   }
}
