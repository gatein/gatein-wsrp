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

import com.google.common.collect.Lists;
import org.apache.cxf.feature.Features;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.endpoints.WSRPBaseEndpoint;
import org.gatein.wsrp.spec.v1.V1ToV2Converter;
import org.gatein.wsrp.spec.v1.V2ToV1Converter;
import org.gatein.wsrp.spec.v1.WSRP1ExceptionFactory;
import org.oasis.wsrp.v1.V1AccessDenied;
import org.oasis.wsrp.v1.V1Extension;
import org.oasis.wsrp.v1.V1InconsistentParameters;
import org.oasis.wsrp.v1.V1InteractionParams;
import org.oasis.wsrp.v1.V1InvalidCookie;
import org.oasis.wsrp.v1.V1InvalidHandle;
import org.oasis.wsrp.v1.V1InvalidRegistration;
import org.oasis.wsrp.v1.V1InvalidSession;
import org.oasis.wsrp.v1.V1InvalidUserCategory;
import org.oasis.wsrp.v1.V1MarkupContext;
import org.oasis.wsrp.v1.V1MarkupParams;
import org.oasis.wsrp.v1.V1MissingParameters;
import org.oasis.wsrp.v1.V1OperationFailed;
import org.oasis.wsrp.v1.V1PortletContext;
import org.oasis.wsrp.v1.V1PortletStateChangeRequired;
import org.oasis.wsrp.v1.V1RegistrationContext;
import org.oasis.wsrp.v1.V1RuntimeContext;
import org.oasis.wsrp.v1.V1SessionContext;
import org.oasis.wsrp.v1.V1UnsupportedLocale;
import org.oasis.wsrp.v1.V1UnsupportedMimeType;
import org.oasis.wsrp.v1.V1UnsupportedMode;
import org.oasis.wsrp.v1.V1UnsupportedWindowState;
import org.oasis.wsrp.v1.V1UpdateResponse;
import org.oasis.wsrp.v1.V1UserContext;
import org.oasis.wsrp.v1.WSRPV1MarkupPortType;
import org.oasis.wsrp.v2.AccessDenied;
import org.oasis.wsrp.v2.BlockingInteractionResponse;
import org.oasis.wsrp.v2.Extension;
import org.oasis.wsrp.v2.GetMarkup;
import org.oasis.wsrp.v2.InconsistentParameters;
import org.oasis.wsrp.v2.InitCookie;
import org.oasis.wsrp.v2.InvalidCookie;
import org.oasis.wsrp.v2.InvalidHandle;
import org.oasis.wsrp.v2.InvalidRegistration;
import org.oasis.wsrp.v2.InvalidSession;
import org.oasis.wsrp.v2.InvalidUserCategory;
import org.oasis.wsrp.v2.MarkupResponse;
import org.oasis.wsrp.v2.MissingParameters;
import org.oasis.wsrp.v2.ModifyRegistrationRequired;
import org.oasis.wsrp.v2.OperationFailed;
import org.oasis.wsrp.v2.OperationNotSupported;
import org.oasis.wsrp.v2.PerformBlockingInteraction;
import org.oasis.wsrp.v2.PortletStateChangeRequired;
import org.oasis.wsrp.v2.ReleaseSessions;
import org.oasis.wsrp.v2.ResourceSuspended;
import org.oasis.wsrp.v2.UnsupportedLocale;
import org.oasis.wsrp.v2.UnsupportedMimeType;
import org.oasis.wsrp.v2.UnsupportedMode;
import org.oasis.wsrp.v2.UnsupportedWindowState;

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
   name = "WSRPV1MarkupPortType",
   serviceName = "WSRPService",
   portName = "WSRPMarkupService",
   targetNamespace = "urn:oasis:names:tc:wsrp:v1:wsdl",
   wsdlLocation = "/WEB-INF/wsdl/wsrp_services.wsdl",
   endpointInterface = "org.oasis.wsrp.v1.WSRPV1MarkupPortType"
)
@Features(features = "org.gatein.wsrp.cxf.WSRPEndpointFeature")
public class MarkupEndpoint extends WSRPBaseEndpoint implements WSRPV1MarkupPortType
{
   public void performBlockingInteraction(
      @WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1RegistrationContext registrationContext,
      @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1PortletContext portletContext,
      @WebParam(name = "runtimeContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1RuntimeContext runtimeContext,
      @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1UserContext userContext,
      @WebParam(name = "markupParams", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1MarkupParams markupParams,
      @WebParam(name = "interactionParams", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1InteractionParams interactionParams,
      @WebParam(mode = WebParam.Mode.OUT, name = "updateResponse", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<V1UpdateResponse> updateResponse,
      @WebParam(mode = WebParam.Mode.OUT, name = "redirectURL", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<String> redirectURL,
      @WebParam(mode = WebParam.Mode.OUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<V1Extension>> extensions
   ) throws V1UnsupportedMimeType, V1UnsupportedMode, V1UnsupportedWindowState, V1InvalidCookie, V1InvalidSession, V1MissingParameters,
      V1UnsupportedLocale, V1InconsistentParameters, V1PortletStateChangeRequired, V1InvalidHandle, V1InvalidRegistration,
      V1InvalidUserCategory, V1AccessDenied, V1OperationFailed
   {

      forceSessionAccess();

      PerformBlockingInteraction performBlockingInteraction = WSRPTypeFactory.createPerformBlockingInteraction(
         V1ToV2Converter.toV2RegistrationContext(registrationContext), V1ToV2Converter.toV2PortletContext(portletContext),
         V1ToV2Converter.toV2RuntimeContext(runtimeContext, portletContext.getPortletHandle()),
         V1ToV2Converter.toV2UserContext(userContext), V1ToV2Converter.toV2MarkupParams(markupParams),
         V1ToV2Converter.toV2InteractionParams(interactionParams)
      );

      BlockingInteractionResponse interactionResponse;
      try
      {
         interactionResponse = producer.performBlockingInteraction(performBlockingInteraction);

      }
      catch (InvalidCookie invalidCookie)
      {
         throw V2ToV1Converter.toV1Exception(V1InvalidCookie.class, invalidCookie);
      }
      catch (InvalidHandle invalidHandle)
      {
         throw V2ToV1Converter.toV1Exception(V1InvalidHandle.class, invalidHandle);
      }
      catch (InvalidSession invalidSession)
      {
         throw V2ToV1Converter.toV1Exception(V1InvalidSession.class, invalidSession);
      }
      catch (UnsupportedMode unsupportedMode)
      {
         throw V2ToV1Converter.toV1Exception(V1UnsupportedMode.class, unsupportedMode);
      }
      catch (UnsupportedMimeType unsupportedMimeType)
      {
         throw V2ToV1Converter.toV1Exception(V1UnsupportedMimeType.class, unsupportedMimeType);
      }
      catch (OperationFailed operationFailed)
      {
         throw V2ToV1Converter.toV1Exception(V1OperationFailed.class, operationFailed);
      }
      catch (UnsupportedWindowState unsupportedWindowState)
      {
         throw V2ToV1Converter.toV1Exception(V1UnsupportedWindowState.class, unsupportedWindowState);
      }
      catch (UnsupportedLocale unsupportedLocale)
      {
         throw V2ToV1Converter.toV1Exception(V1UnsupportedLocale.class, unsupportedLocale);
      }
      catch (AccessDenied accessDenied)
      {
         throw V2ToV1Converter.toV1Exception(V1AccessDenied.class, accessDenied);
      }
      catch (PortletStateChangeRequired portletStateChangeRequired)
      {
         throw V2ToV1Converter.toV1Exception(V1PortletStateChangeRequired.class, portletStateChangeRequired);
      }
      catch (InvalidRegistration invalidRegistration)
      {
         throw V2ToV1Converter.toV1Exception(V1InvalidRegistration.class, invalidRegistration);
      }
      catch (MissingParameters missingParameters)
      {
         throw V2ToV1Converter.toV1Exception(V1MissingParameters.class, missingParameters);
      }
      catch (InvalidUserCategory invalidUserCategory)
      {
         throw V2ToV1Converter.toV1Exception(V1InvalidUserCategory.class, invalidUserCategory);
      }
      catch (InconsistentParameters inconsistentParameters)
      {
         throw V2ToV1Converter.toV1Exception(V1InconsistentParameters.class, inconsistentParameters);
      }
      catch (ModifyRegistrationRequired modifyRegistrationRequired)
      {
         throw WSRP1ExceptionFactory.createWSException(V1OperationFailed.class, "Need to call modifyRegistration", modifyRegistrationRequired);
      }
      catch (ResourceSuspended resourceSuspended)
      {
         throw WSRP1ExceptionFactory.createWSException(V1OperationFailed.class, "Resource suspended", resourceSuspended);
      }

      updateResponse.value = V2ToV1Converter.toV1UpdateResponse(interactionResponse.getUpdateResponse());
      redirectURL.value = interactionResponse.getRedirectURL();
      extensions.value = Lists.transform(interactionResponse.getExtensions(), V2ToV1Converter.EXTENSION);
   }

   public List<V1Extension> releaseSessions(
      @WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1RegistrationContext registrationContext,
      @WebParam(name = "sessionIDs", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> sessionIDs
   ) throws V1MissingParameters, V1InvalidRegistration, V1AccessDenied, V1OperationFailed
   {
      forceSessionAccess();

      ReleaseSessions releaseSessions = WSRPTypeFactory.createReleaseSessions(
         V1ToV2Converter.toV2RegistrationContext(registrationContext),
         sessionIDs
      );

      List<Extension> extensions;
      try
      {
         extensions = producer.releaseSessions(releaseSessions);
      }
      catch (InvalidRegistration invalidRegistration)
      {
         throw V2ToV1Converter.toV1Exception(V1InvalidRegistration.class, invalidRegistration);
      }
      catch (OperationFailed operationFailed)
      {
         throw V2ToV1Converter.toV1Exception(V1OperationFailed.class, operationFailed);
      }
      catch (MissingParameters missingParameters)
      {
         throw V2ToV1Converter.toV1Exception(V1MissingParameters.class, missingParameters);
      }
      catch (AccessDenied accessDenied)
      {
         throw V2ToV1Converter.toV1Exception(V1AccessDenied.class, accessDenied);
      }
      catch (ModifyRegistrationRequired modifyRegistrationRequired)
      {
         throw WSRP1ExceptionFactory.createWSException(V1OperationFailed.class, "Need to call modifyRegistration", modifyRegistrationRequired);
      }
      catch (ResourceSuspended resourceSuspended)
      {
         throw WSRP1ExceptionFactory.createWSException(V1OperationFailed.class, "Resource suspended", resourceSuspended);
      }
      catch (OperationNotSupported operationNotSupported)
      {
         throw WSRP1ExceptionFactory.createWSException(V1OperationFailed.class, "Not supported", operationNotSupported);
      }

      return Lists.transform(extensions, V2ToV1Converter.EXTENSION);
   }

   public void getMarkup(
      @WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1RegistrationContext registrationContext,
      @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1PortletContext portletContext,
      @WebParam(name = "runtimeContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1RuntimeContext runtimeContext,
      @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1UserContext userContext,
      @WebParam(name = "markupParams", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1MarkupParams markupParams,
      @WebParam(mode = WebParam.Mode.OUT, name = "markupContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<V1MarkupContext> markupContext,
      @WebParam(mode = WebParam.Mode.OUT, name = "sessionContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<V1SessionContext> sessionContext,
      @WebParam(mode = WebParam.Mode.OUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<V1Extension>> extensions
   ) throws V1UnsupportedMimeType, V1UnsupportedMode, V1UnsupportedWindowState, V1InvalidCookie, V1InvalidSession, V1MissingParameters,
      V1UnsupportedLocale, V1InconsistentParameters, V1InvalidHandle, V1InvalidRegistration, V1InvalidUserCategory, V1AccessDenied,
      V1OperationFailed
   {
      forceSessionAccess();

      GetMarkup getMarkup = WSRPTypeFactory.createGetMarkup(
         V1ToV2Converter.toV2RegistrationContext(registrationContext),
         V1ToV2Converter.toV2PortletContext(portletContext),
         V1ToV2Converter.toV2RuntimeContext(runtimeContext, portletContext.getPortletHandle()),
         V1ToV2Converter.toV2UserContext(userContext),
         V1ToV2Converter.toV2MarkupParams(markupParams)
      );

      MarkupResponse response;
      try
      {
         response = producer.getMarkup(getMarkup);
      }
      catch (UnsupportedWindowState unsupportedWindowState)
      {
         throw V2ToV1Converter.toV1Exception(V1UnsupportedWindowState.class, unsupportedWindowState);
      }
      catch (InvalidCookie invalidCookie)
      {
         throw V2ToV1Converter.toV1Exception(V1InvalidCookie.class, invalidCookie);
      }
      catch (InvalidSession invalidSession)
      {
         throw V2ToV1Converter.toV1Exception(V1InvalidSession.class, invalidSession);
      }
      catch (AccessDenied accessDenied)
      {
         throw V2ToV1Converter.toV1Exception(V1AccessDenied.class, accessDenied);
      }
      catch (InconsistentParameters inconsistentParameters)
      {
         throw V2ToV1Converter.toV1Exception(V1InconsistentParameters.class, inconsistentParameters);
      }
      catch (InvalidHandle invalidHandle)
      {
         throw V2ToV1Converter.toV1Exception(V1InvalidHandle.class, invalidHandle);
      }
      catch (UnsupportedLocale unsupportedLocale)
      {
         throw V2ToV1Converter.toV1Exception(V1UnsupportedLocale.class, unsupportedLocale);
      }
      catch (UnsupportedMode unsupportedMode)
      {
         throw V2ToV1Converter.toV1Exception(V1UnsupportedMode.class, unsupportedMode);
      }
      catch (OperationFailed operationFailed)
      {
         throw V2ToV1Converter.toV1Exception(V1OperationFailed.class, operationFailed);
      }
      catch (MissingParameters missingParameters)
      {
         throw V2ToV1Converter.toV1Exception(V1MissingParameters.class, missingParameters);
      }
      catch (InvalidUserCategory invalidUserCategory)
      {
         throw V2ToV1Converter.toV1Exception(V1InvalidUserCategory.class, invalidUserCategory);
      }
      catch (InvalidRegistration invalidRegistration)
      {
         throw V2ToV1Converter.toV1Exception(V1InvalidRegistration.class, invalidRegistration);
      }
      catch (UnsupportedMimeType unsupportedMimeType)
      {
         throw V2ToV1Converter.toV1Exception(V1UnsupportedMimeType.class, unsupportedMimeType);
      }
      catch (ModifyRegistrationRequired modifyRegistrationRequired)
      {
         throw WSRP1ExceptionFactory.createWSException(V1OperationFailed.class, "Need to call modifyRegistration", modifyRegistrationRequired);
      }
      catch (ResourceSuspended resourceSuspended)
      {
         throw WSRP1ExceptionFactory.createWSException(V1OperationFailed.class, "Resource suspended", resourceSuspended);
      }

      markupContext.value = V2ToV1Converter.toV1MarkupContext(response.getMarkupContext());
      sessionContext.value = V2ToV1Converter.toV1SessionContext(response.getSessionContext());
      extensions.value = Lists.transform(response.getExtensions(), V2ToV1Converter.EXTENSION);
   }

   public List<V1Extension> initCookie(
      @WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1RegistrationContext registrationContext
   ) throws V1InvalidRegistration, V1AccessDenied, V1OperationFailed
   {
      forceSessionAccess();

      InitCookie initCookie = WSRPTypeFactory.createInitCookie(V1ToV2Converter.toV2RegistrationContext(registrationContext));

      List<Extension> extensions;
      try
      {
         extensions = producer.initCookie(initCookie);
      }
      catch (AccessDenied accessDenied)
      {
         throw V2ToV1Converter.toV1Exception(V1AccessDenied.class, accessDenied);
      }
      catch (OperationFailed operationFailed)
      {
         throw V2ToV1Converter.toV1Exception(V1OperationFailed.class, operationFailed);
      }
      catch (InvalidRegistration invalidRegistration)
      {
         throw V2ToV1Converter.toV1Exception(V1InvalidRegistration.class, invalidRegistration);
      }
      catch (ModifyRegistrationRequired modifyRegistrationRequired)
      {
         throw WSRP1ExceptionFactory.createWSException(V1OperationFailed.class, "Need to call modifyRegistration", modifyRegistrationRequired);
      }
      catch (ResourceSuspended resourceSuspended)
      {
         throw WSRP1ExceptionFactory.createWSException(V1OperationFailed.class, "Resource suspended", resourceSuspended);
      }
      catch (OperationNotSupported operationNotSupported)
      {
         throw WSRP1ExceptionFactory.createWSException(V1OperationFailed.class, "Not supported", operationNotSupported);
      }

      return Lists.transform(extensions, V2ToV1Converter.EXTENSION);
   }
}
