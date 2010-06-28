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

package org.gatein.wsrp.endpoints.v2;

import org.gatein.common.NotYetImplemented;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.endpoints.WSRPBaseEndpoint;
import org.oasis.wsrp.v2.AccessDenied;
import org.oasis.wsrp.v2.BlockingInteractionResponse;
import org.oasis.wsrp.v2.EventParams;
import org.oasis.wsrp.v2.Extension;
import org.oasis.wsrp.v2.GetMarkup;
import org.oasis.wsrp.v2.HandleEvents;
import org.oasis.wsrp.v2.HandleEventsFailed;
import org.oasis.wsrp.v2.HandleEventsResponse;
import org.oasis.wsrp.v2.InconsistentParameters;
import org.oasis.wsrp.v2.InitCookie;
import org.oasis.wsrp.v2.InteractionParams;
import org.oasis.wsrp.v2.InvalidCookie;
import org.oasis.wsrp.v2.InvalidHandle;
import org.oasis.wsrp.v2.InvalidRegistration;
import org.oasis.wsrp.v2.InvalidSession;
import org.oasis.wsrp.v2.InvalidUserCategory;
import org.oasis.wsrp.v2.MarkupContext;
import org.oasis.wsrp.v2.MarkupParams;
import org.oasis.wsrp.v2.MarkupResponse;
import org.oasis.wsrp.v2.MissingParameters;
import org.oasis.wsrp.v2.ModifyRegistrationRequired;
import org.oasis.wsrp.v2.OperationFailed;
import org.oasis.wsrp.v2.OperationNotSupported;
import org.oasis.wsrp.v2.PerformBlockingInteraction;
import org.oasis.wsrp.v2.PortletContext;
import org.oasis.wsrp.v2.PortletStateChangeRequired;
import org.oasis.wsrp.v2.RegistrationContext;
import org.oasis.wsrp.v2.ReleaseSessions;
import org.oasis.wsrp.v2.ResourceContext;
import org.oasis.wsrp.v2.ResourceParams;
import org.oasis.wsrp.v2.ResourceSuspended;
import org.oasis.wsrp.v2.ReturnAny;
import org.oasis.wsrp.v2.RuntimeContext;
import org.oasis.wsrp.v2.SessionContext;
import org.oasis.wsrp.v2.UnsupportedLocale;
import org.oasis.wsrp.v2.UnsupportedMimeType;
import org.oasis.wsrp.v2.UnsupportedMode;
import org.oasis.wsrp.v2.UnsupportedWindowState;
import org.oasis.wsrp.v2.UpdateResponse;
import org.oasis.wsrp.v2.UserContext;
import org.oasis.wsrp.v2.WSRPV2MarkupPortType;

import javax.jws.HandlerChain;
import javax.jws.WebParam;
import javax.xml.ws.Holder;
import java.util.List;

/**
 * GTNWSRP-41
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 8784 $
 */
@javax.jws.WebService(
   name = "WSRPV2MarkupPortType",
   serviceName = "WSRPService",
   portName = "WSRPMarkupService",
   targetNamespace = "urn:oasis:names:tc:wsrp:v2:wsdl",
   wsdlLocation = "/WEB-INF/wsdl/wsrp-2.0-services.wsdl",
   endpointInterface = "org.oasis.wsrp.v2.WSRPV2MarkupPortType"
)
@HandlerChain(file = "wshandlers.xml")
public class MarkupEndpoint extends WSRPBaseEndpoint implements WSRPV2MarkupPortType
{
   public void handleEvents(
      @WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext,
      @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") PortletContext portletContext,
      @WebParam(name = "runtimeContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RuntimeContext runtimeContext,
      @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext,
      @WebParam(name = "markupParams", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") MarkupParams markupParams,
      @WebParam(name = "eventParams", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") EventParams eventParams,
      @WebParam(name = "updateResponse", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<UpdateResponse> updateResponse,
      @WebParam(name = "failedEvents", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<HandleEventsFailed>> failedEvents,
      @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions
   ) throws AccessDenied, InconsistentParameters, InvalidCookie, InvalidHandle, InvalidRegistration, InvalidSession,
      InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported,
      PortletStateChangeRequired, ResourceSuspended, UnsupportedLocale, UnsupportedMimeType, UnsupportedMode,
      UnsupportedWindowState
   {
      forceSessionAccess();

      HandleEvents handleEvents = WSRPTypeFactory.createHandleEvents(portletContext, runtimeContext, markupParams, eventParams);
      handleEvents.setRegistrationContext(registrationContext);
      handleEvents.setUserContext(userContext);

      HandleEventsResponse response = producer.handleEvents(handleEvents);

      updateResponse.value = response.getUpdateResponse();
      failedEvents.value = response.getFailedEvents();
      extensions.value = response.getExtensions();
   }

   public List<Extension> releaseSessions(
      @WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext,
      @WebParam(name = "sessionIDs", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") List<String> sessionIDs,
      @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext
   ) throws AccessDenied, InvalidRegistration, MissingParameters, ModifyRegistrationRequired, OperationFailed,
      OperationNotSupported, ResourceSuspended
   {
      forceSessionAccess();

      ReleaseSessions releaseSessions = new ReleaseSessions();
      releaseSessions.setRegistrationContext(registrationContext);
      releaseSessions.getSessionIDs().addAll(sessionIDs);

      ReturnAny returnAny = producer.releaseSessions(releaseSessions);

      return returnAny.getExtensions();
   }

   public List<Extension> initCookie(
      @WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext,
      @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext
   ) throws AccessDenied, InvalidRegistration, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      forceSessionAccess();

      InitCookie initCookie = new InitCookie();
      initCookie.setRegistrationContext(registrationContext);

      ReturnAny returnAny = producer.initCookie(initCookie);

      return returnAny.getExtensions();
   }

   public void getMarkup(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") PortletContext portletContext, @WebParam(name = "runtimeContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RuntimeContext runtimeContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext, @WebParam(name = "markupParams", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") MarkupParams markupParams, @WebParam(name = "markupContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<MarkupContext> markupContext, @WebParam(name = "sessionContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<SessionContext> sessionContext, @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions) throws AccessDenied, InconsistentParameters, InvalidCookie, InvalidHandle, InvalidRegistration, InvalidSession, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, ResourceSuspended, UnsupportedLocale, UnsupportedMimeType, UnsupportedMode, UnsupportedWindowState
   {
      forceSessionAccess();

      GetMarkup getMarkup = new GetMarkup();
      getMarkup.setRegistrationContext(registrationContext);
      getMarkup.setPortletContext(portletContext);
      getMarkup.setRuntimeContext(runtimeContext);
      getMarkup.setUserContext(userContext);
      getMarkup.setMarkupParams(markupParams);

      MarkupResponse response = producer.getMarkup(getMarkup);

      markupContext.value = response.getMarkupContext();
      sessionContext.value = response.getSessionContext();
      extensions.value = response.getExtensions();
   }

   public void getResource(
      @WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext,
      @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.INOUT) Holder<PortletContext> portletContext,
      @WebParam(name = "runtimeContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RuntimeContext runtimeContext,
      @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext,
      @WebParam(name = "resourceParams", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") ResourceParams resourceParams,
      @WebParam(name = "resourceContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<ResourceContext> resourceContext,
      @WebParam(name = "sessionContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<SessionContext> sessionContext,
      @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions
   ) throws AccessDenied, InconsistentParameters, InvalidCookie, InvalidHandle, InvalidRegistration, InvalidSession,
      InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported,
      ResourceSuspended, UnsupportedLocale, UnsupportedMimeType, UnsupportedMode, UnsupportedWindowState
   {
      throw new NotYetImplemented();
   }

   public void performBlockingInteraction(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") PortletContext portletContext, @WebParam(name = "runtimeContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RuntimeContext runtimeContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext, @WebParam(name = "markupParams", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") MarkupParams markupParams, @WebParam(name = "interactionParams", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") InteractionParams interactionParams, @WebParam(name = "updateResponse", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<UpdateResponse> updateResponse, @WebParam(name = "redirectURL", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<String> redirectURL, @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions) throws AccessDenied, InconsistentParameters, InvalidCookie, InvalidHandle, InvalidRegistration, InvalidSession, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, PortletStateChangeRequired, ResourceSuspended, UnsupportedLocale, UnsupportedMimeType, UnsupportedMode, UnsupportedWindowState
   {
      forceSessionAccess();

      PerformBlockingInteraction performBlockingInteraction = new PerformBlockingInteraction();
      performBlockingInteraction.setPortletContext(portletContext);
      performBlockingInteraction.setRuntimeContext(runtimeContext);
      performBlockingInteraction.setMarkupParams(markupParams);
      performBlockingInteraction.setInteractionParams(interactionParams);
      performBlockingInteraction.setRegistrationContext(registrationContext);
      performBlockingInteraction.setUserContext(userContext);

      BlockingInteractionResponse interactionResponse = producer.performBlockingInteraction(performBlockingInteraction);

      updateResponse.value = interactionResponse.getUpdateResponse();
      redirectURL.value = interactionResponse.getRedirectURL();
      extensions.value = interactionResponse.getExtensions();
   }
}
