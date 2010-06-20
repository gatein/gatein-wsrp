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

import org.gatein.common.NotYetImplemented;
import org.gatein.common.net.media.MediaType;
import org.gatein.pc.api.Mode;
import org.gatein.pc.api.WindowState;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.WSRPUtils;
import org.oasis.wsrp.v2.AccessDenied;
import org.oasis.wsrp.v2.EventParams;
import org.oasis.wsrp.v2.Extension;
import org.oasis.wsrp.v2.GetMarkup;
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
import org.oasis.wsrp.v2.MarkupResponse;
import org.oasis.wsrp.v2.MissingParameters;
import org.oasis.wsrp.v2.ModifyRegistrationRequired;
import org.oasis.wsrp.v2.NavigationalContext;
import org.oasis.wsrp.v2.OperationFailed;
import org.oasis.wsrp.v2.OperationNotSupported;
import org.oasis.wsrp.v2.PortletContext;
import org.oasis.wsrp.v2.PortletDescription;
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
import org.oasis.wsrp.v2.WSRPV2MarkupPortType;

import javax.jws.WebParam;
import javax.xml.ws.Holder;
import java.util.ArrayList;
import java.util.List;

/**
 * Behavior delivering Markup services.
 *
 * @author <a href="mailto:chris.laprun@jboss.com?subject=org.gatein.wsrp.test.AbstractMarkupBehavior">Chris Laprun</a>
 * @version $Revision: 10337 $
 * @since 2.6
 */
public abstract class MarkupBehavior extends TestProducerBehavior implements WSRPV2MarkupPortType
{
   private List<String> handles = new ArrayList<String>(3);
   private BehaviorRegistry registry;


   protected MarkupBehavior(BehaviorRegistry registry)
   {
      this.registry = registry;
   }

   /**
    * Returns a markup String based on the passed information.
    *
    * @param mode              the requested mode
    * @param windowState       the requested window state
    * @param navigationalState the current navigational state
    * @param getMarkup         the original GetMarkup request (in case more information is required by this behavior)
    * @return a possibly <code>null</code> markup String
    */
   protected abstract String getMarkupString(Mode mode, WindowState windowState, String navigationalState, GetMarkup getMarkup)
      throws UnsupportedWindowState, InvalidCookie, InvalidSession, AccessDenied, InconsistentParameters, InvalidHandle,
      UnsupportedLocale, UnsupportedMode, OperationFailed, MissingParameters, InvalidUserCategory, InvalidRegistration,
      UnsupportedMimeType;

   /**
    * Allows this behavior to modify the response after the markup has been generated. The default implementation does
    * nothing.
    *
    * @param markupResponse the response that will be passed on to the consumer
    */
   public void modifyResponseIfNeeded(MarkupResponse markupResponse)
   {
      // default implementation does not nothing
   }

   public List<String> getSupportedHandles()
   {
      return handles;
   }

   public PortletDescription getPortletDescriptionFor(String handle)
   {
      if (handles.contains(handle))
      {
         return createPortletDescription(handle, getSuffixFor(handle));
      }
      throw new IllegalArgumentException("MarkupBehavior " + getClass().getName() + " is not associated with handle '"
         + handle + "'");
   }

   protected String getSuffixFor(String handle)
   {
      return "";
   }

   protected void registerHandle(String handle)
   {
      handles.add(handle);
      registry.getServiceDescriptionBehavior().addPortletDescription(createPortletDescription(handle, getSuffixFor(handle)));
   }

   public void getResource(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.INOUT) Holder<PortletContext> portletContext, @WebParam(name = "runtimeContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RuntimeContext runtimeContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext, @WebParam(name = "resourceParams", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") ResourceParams resourceParams, @WebParam(name = "resourceContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<ResourceContext> resourceContext, @WebParam(name = "sessionContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<SessionContext> sessionContext, @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions) throws AccessDenied, InconsistentParameters, InvalidCookie, InvalidHandle, InvalidRegistration, InvalidSession, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended, UnsupportedLocale, UnsupportedMimeType, UnsupportedMode, UnsupportedWindowState
   {
      throw new NotYetImplemented();
   }

   public void performBlockingInteraction(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") PortletContext portletContext, @WebParam(name = "runtimeContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RuntimeContext runtimeContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext, @WebParam(name = "markupParams", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") MarkupParams markupParams, @WebParam(name = "interactionParams", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") InteractionParams interactionParams, @WebParam(name = "updateResponse", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<UpdateResponse> updateResponse, @WebParam(name = "redirectURL", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<String> redirectURL, @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions) throws AccessDenied, InconsistentParameters, InvalidCookie, InvalidHandle, InvalidRegistration, InvalidSession, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, PortletStateChangeRequired, ResourceSuspended, UnsupportedLocale, UnsupportedMimeType, UnsupportedMode, UnsupportedWindowState
   {
      // do nothing
   }

   public void handleEvents(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") PortletContext portletContext, @WebParam(name = "runtimeContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RuntimeContext runtimeContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext, @WebParam(name = "markupParams", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") MarkupParams markupParams, @WebParam(name = "eventParams", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") EventParams eventParams, @WebParam(name = "updateResponse", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<UpdateResponse> updateResponse, @WebParam(name = "failedEvents", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<HandleEventsFailed>> failedEvents, @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions) throws AccessDenied, InconsistentParameters, InvalidCookie, InvalidHandle, InvalidRegistration, InvalidSession, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, PortletStateChangeRequired, ResourceSuspended, UnsupportedLocale, UnsupportedMimeType, UnsupportedMode, UnsupportedWindowState
   {
      throw new NotYetImplemented();
   }

   public List<Extension> releaseSessions(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext, @WebParam(name = "sessionIDs", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") List<String> sessionIDs, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext) throws AccessDenied, InvalidRegistration, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      throw new NotYetImplemented();
   }

   public void getMarkup(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") PortletContext portletContext, @WebParam(name = "runtimeContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RuntimeContext runtimeContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext, @WebParam(name = "markupParams", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") MarkupParams markupParams, @WebParam(name = "markupContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<MarkupContext> markupContext, @WebParam(name = "sessionContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<SessionContext> sessionContext, @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions) throws AccessDenied, InconsistentParameters, InvalidCookie, InvalidHandle, InvalidRegistration, InvalidSession, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, ResourceSuspended, UnsupportedLocale, UnsupportedMimeType, UnsupportedMode, UnsupportedWindowState
   {
      GetMarkup gm = new GetMarkup();
      gm.setMarkupParams(markupParams);
      gm.setPortletContext(portletContext);
      gm.setRegistrationContext(registrationContext);
      gm.setRuntimeContext(runtimeContext);
      gm.setUserContext(userContext);

      NavigationalContext navigationalContext = markupParams.getNavigationalContext();
      String markupString = getMarkupString(WSRPUtils.getJSR168PortletModeFromWSRPName(markupParams.getMode()),
         WSRPUtils.getJSR168WindowStateFromWSRPName(markupParams.getWindowState()),
         navigationalContext != null ? navigationalContext.getOpaqueValue() : null,
         gm);

      markupContext.value = WSRPTypeFactory.createMarkupContext(MediaType.TEXT_HTML.getValue(), markupString);
      markupContext.value.setRequiresRewriting(Boolean.TRUE);

      MarkupResponse markupResponse = WSRPTypeFactory.createMarkupResponse(markupContext.value);

      modifyResponseIfNeeded(markupResponse);
   }

   public List<Extension> initCookie(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext) throws AccessDenied, InvalidRegistration, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      return null;
   }
}
