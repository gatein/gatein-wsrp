/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2006, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/

package org.gatein.wsrp.test.protocol.v1;

import org.gatein.common.net.media.MediaType;
import org.gatein.pc.api.Mode;
import org.gatein.pc.api.WindowState;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.WSRPUtils;
import org.gatein.wsrp.test.BehaviorRegistry;
import org.oasis.wsrp.v1.AccessDenied;
import org.oasis.wsrp.v1.Extension;
import org.oasis.wsrp.v1.GetMarkup;
import org.oasis.wsrp.v1.InconsistentParameters;
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
import org.oasis.wsrp.v1.PortletContext;
import org.oasis.wsrp.v1.PortletDescription;
import org.oasis.wsrp.v1.PortletStateChangeRequired;
import org.oasis.wsrp.v1.RegistrationContext;
import org.oasis.wsrp.v1.RuntimeContext;
import org.oasis.wsrp.v1.SessionContext;
import org.oasis.wsrp.v1.UnsupportedLocale;
import org.oasis.wsrp.v1.UnsupportedMimeType;
import org.oasis.wsrp.v1.UnsupportedMode;
import org.oasis.wsrp.v1.UnsupportedWindowState;
import org.oasis.wsrp.v1.UpdateResponse;
import org.oasis.wsrp.v1.UserContext;
import org.oasis.wsrp.v1.WSRPV1MarkupPortType;

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
public abstract class MarkupBehavior extends TestProducerBehavior implements WSRPV1MarkupPortType
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

   /**
    * Default implementation doesn't do anything.
    *
    * @param registrationContext
    * @param portletContext
    * @param runtimeContext
    * @param userContext
    * @param markupParams
    * @param interactionParams
    * @param updateResponse
    * @param redirectURL
    * @param extensions
    * @throws UnsupportedMimeType
    * @throws UnsupportedMode
    * @throws UnsupportedWindowState
    * @throws InvalidCookie
    * @throws InvalidSession
    * @throws MissingParameters
    * @throws UnsupportedLocale
    * @throws InconsistentParameters
    * @throws PortletStateChangeRequired
    * @throws InvalidHandle
    * @throws InvalidRegistration
    * @throws InvalidUserCategory
    * @throws AccessDenied
    * @throws OperationFailed
    */
   public void performBlockingInteraction(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") PortletContext portletContext, @WebParam(name = "runtimeContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") RuntimeContext runtimeContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") UserContext userContext, @WebParam(name = "markupParams", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") MarkupParams markupParams, @WebParam(name = "interactionParams", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") InteractionParams interactionParams, @WebParam(mode = WebParam.Mode.OUT, name = "updateResponse", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<UpdateResponse> updateResponse, @WebParam(mode = WebParam.Mode.OUT, name = "redirectURL", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<String> redirectURL, @WebParam(mode = WebParam.Mode.OUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<Extension>> extensions) throws UnsupportedMimeType, UnsupportedMode, UnsupportedWindowState, InvalidCookie, InvalidSession, MissingParameters, UnsupportedLocale, InconsistentParameters, PortletStateChangeRequired, InvalidHandle, InvalidRegistration, InvalidUserCategory, AccessDenied, OperationFailed
   {
      // do nothing
   }

   public List<Extension> releaseSessions(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") RegistrationContext registrationContext, @WebParam(name = "sessionIDs", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> sessionIDs) throws MissingParameters, InvalidRegistration, AccessDenied, OperationFailed
   {
      return null;
   }

   public void getMarkup(
      @WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") RegistrationContext registrationContext,
      @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") PortletContext portletContext,
      @WebParam(name = "runtimeContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") RuntimeContext runtimeContext,
      @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") UserContext userContext,
      @WebParam(name = "markupParams", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") MarkupParams markupParams,
      @WebParam(mode = WebParam.Mode.OUT, name = "markupContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<MarkupContext> markupContext,
      @WebParam(mode = WebParam.Mode.OUT, name = "sessionContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<SessionContext> sessionContext,
      @WebParam(mode = WebParam.Mode.OUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<Extension>> extensions)
      throws UnsupportedMimeType, UnsupportedMode, UnsupportedWindowState, InvalidCookie, InvalidSession, MissingParameters, UnsupportedLocale, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory, AccessDenied, OperationFailed
   {
      GetMarkup gm = new GetMarkup();
      gm.setMarkupParams(markupParams);
      gm.setPortletContext(portletContext);
      gm.setRegistrationContext(registrationContext);
      gm.setRuntimeContext(runtimeContext);
      gm.setUserContext(userContext);

      String markupString = getMarkupString(WSRPUtils.getJSR168PortletModeFromWSRPName(markupParams.getMode()),
         WSRPUtils.getJSR168WindowStateFromWSRPName(markupParams.getWindowState()), markupParams.getNavigationalState(), gm);

      markupContext.value = WSRPTypeFactory.createMarkupContext(MediaType.TEXT_HTML.getValue(), markupString);
      markupContext.value.setRequiresUrlRewriting(Boolean.TRUE);

      MarkupResponse markupResponse = WSRPTypeFactory.createMarkupResponse(markupContext.value);

      modifyResponseIfNeeded(markupResponse);
   }

   public List<Extension> initCookie(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") RegistrationContext registrationContext) throws InvalidRegistration, AccessDenied, OperationFailed
   {
      return null;
   }
}
