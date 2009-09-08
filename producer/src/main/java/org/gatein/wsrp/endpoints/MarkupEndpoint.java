/*
 * JBoss, a division of Red Hat
 * Copyright 2009, Red Hat Middleware, LLC, and individual
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

package org.gatein.wsrp.endpoints;

import org.oasis.wsrp.v1.AccessDenied;
import org.oasis.wsrp.v1.BlockingInteractionResponse;
import org.oasis.wsrp.v1.Extension;
import org.oasis.wsrp.v1.GetMarkup;
import org.oasis.wsrp.v1.InconsistentParameters;
import org.oasis.wsrp.v1.InitCookie;
import org.oasis.wsrp.v1.InteractionParams;
import org.oasis.wsrp.v1.InvalidCookie;
import org.oasis.wsrp.v1.InvalidHandle;
import org.oasis.wsrp.v1.InvalidRegistration;
import org.oasis.wsrp.v1.InvalidSession;
import org.oasis.wsrp.v1.InvalidUserCategory;
import org.oasis.wsrp.v1.MarkupContext;
import org.oasis.wsrp.v1.MarkupParams;
import org.oasis.wsrp.v1.MarkupResponse;
import org.oasis.wsrp.v1.MissingParameters;
import org.oasis.wsrp.v1.OperationFailed;
import org.oasis.wsrp.v1.PerformBlockingInteraction;
import org.oasis.wsrp.v1.PortletContext;
import org.oasis.wsrp.v1.PortletStateChangeRequired;
import org.oasis.wsrp.v1.RegistrationContext;
import org.oasis.wsrp.v1.ReleaseSessions;
import org.oasis.wsrp.v1.ReturnAny;
import org.oasis.wsrp.v1.RuntimeContext;
import org.oasis.wsrp.v1.SessionContext;
import org.oasis.wsrp.v1.UnsupportedLocale;
import org.oasis.wsrp.v1.UnsupportedMimeType;
import org.oasis.wsrp.v1.UnsupportedMode;
import org.oasis.wsrp.v1.UnsupportedWindowState;
import org.oasis.wsrp.v1.UpdateResponse;
import org.oasis.wsrp.v1.UserContext;
import org.oasis.wsrp.v1.WSRPV1MarkupPortType;

import javax.jws.HandlerChain;
import javax.jws.WebParam;
import javax.xml.ws.Holder;
import java.util.List;

/**
 * @author <a href="mailto:palber@novell.com">Polina Alber</a>
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 8784 $
 * @since 2.4
 */
@javax.jws.WebService(
   name = "WSRPV1MarkupPortType",
   serviceName = "WSRPV1Service",
   portName = "WSRPMarkupService",
   targetNamespace = "urn:oasis:names:tc:wsrp:v1:wsdl",
   wsdlLocation = "/WEB-INF/wsdl/wsrp_services.wsdl",
   endpointInterface = "org.oasis.wsrp.v1.WSRPV1MarkupPortType"
)
@HandlerChain(file = "wshandlers.xml")
public class MarkupEndpoint extends WSRPBaseEndpoint implements WSRPV1MarkupPortType
{
   public void performBlockingInteraction(
      @WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") RegistrationContext registrationContext,
      @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") PortletContext portletContext,
      @WebParam(name = "runtimeContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") RuntimeContext runtimeContext,
      @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") UserContext userContext,
      @WebParam(name = "markupParams", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") MarkupParams markupParams,
      @WebParam(name = "interactionParams", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") InteractionParams interactionParams,
      @WebParam(mode = WebParam.Mode.OUT, name = "updateResponse", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<UpdateResponse> updateResponse,
      @WebParam(mode = WebParam.Mode.OUT, name = "redirectURL", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<String> redirectURL,
      @WebParam(mode = WebParam.Mode.OUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<Extension>> extensions
   ) throws UnsupportedMimeType, UnsupportedMode, UnsupportedWindowState, InvalidCookie, InvalidSession, MissingParameters,
      UnsupportedLocale, InconsistentParameters, PortletStateChangeRequired, InvalidHandle, InvalidRegistration,
      InvalidUserCategory, AccessDenied, OperationFailed
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

   public List<Extension> releaseSessions(
      @WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") RegistrationContext registrationContext,
      @WebParam(name = "sessionIDs", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> sessionIDs
   ) throws MissingParameters, InvalidRegistration, AccessDenied, OperationFailed
   {
      forceSessionAccess();

      ReleaseSessions releaseSessions = new ReleaseSessions();
      releaseSessions.setRegistrationContext(registrationContext);
      releaseSessions.getSessionIDs().addAll(sessionIDs);

      ReturnAny returnAny = producer.releaseSessions(releaseSessions);

      return returnAny.getExtensions();
   }

   public void getMarkup(
      @WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") RegistrationContext registrationContext,
      @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") PortletContext portletContext,
      @WebParam(name = "runtimeContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") RuntimeContext runtimeContext,
      @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") UserContext userContext,
      @WebParam(name = "markupParams", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") MarkupParams markupParams,
      @WebParam(mode = WebParam.Mode.OUT, name = "markupContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<MarkupContext> markupContext,
      @WebParam(mode = WebParam.Mode.OUT, name = "sessionContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<SessionContext> sessionContext,
      @WebParam(mode = WebParam.Mode.OUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<Extension>> extensions
   ) throws UnsupportedMimeType, UnsupportedMode, UnsupportedWindowState, InvalidCookie, InvalidSession, MissingParameters,
      UnsupportedLocale, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory, AccessDenied,
      OperationFailed
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

   public List<Extension> initCookie(
      @WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") RegistrationContext registrationContext
   ) throws InvalidRegistration, AccessDenied, OperationFailed
   {
      forceSessionAccess();

      InitCookie initCookie = new InitCookie();
      initCookie.setRegistrationContext(registrationContext);

      ReturnAny returnAny = producer.initCookie(initCookie);

      return returnAny.getExtensions();
   }
}
