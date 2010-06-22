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

package org.gatein.wsrp.test.protocol.v1;

import org.gatein.common.net.media.MediaType;
import org.gatein.pc.api.Mode;
import org.gatein.pc.api.WindowState;
import org.gatein.wsrp.WSRPUtils;
import org.gatein.wsrp.spec.v1.WSRP1TypeFactory;
import org.oasis.wsrp.v1.V1AccessDenied;
import org.oasis.wsrp.v1.V1Extension;
import org.oasis.wsrp.v1.V1GetMarkup;
import org.oasis.wsrp.v1.V1InconsistentParameters;
import org.oasis.wsrp.v1.V1InteractionParams;
import org.oasis.wsrp.v1.V1InvalidCookie;
import org.oasis.wsrp.v1.V1InvalidHandle;
import org.oasis.wsrp.v1.V1InvalidRegistration;
import org.oasis.wsrp.v1.V1InvalidSession;
import org.oasis.wsrp.v1.V1InvalidUserCategory;
import org.oasis.wsrp.v1.V1MarkupContext;
import org.oasis.wsrp.v1.V1MarkupParams;
import org.oasis.wsrp.v1.V1MarkupResponse;
import org.oasis.wsrp.v1.V1MissingParameters;
import org.oasis.wsrp.v1.V1OperationFailed;
import org.oasis.wsrp.v1.V1PortletContext;
import org.oasis.wsrp.v1.V1PortletDescription;
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
   protected abstract String getMarkupString(Mode mode, WindowState windowState, String navigationalState, V1GetMarkup getMarkup)
      throws V1UnsupportedWindowState, V1InvalidCookie, V1InvalidSession, V1AccessDenied, V1InconsistentParameters, V1InvalidHandle,
      V1UnsupportedLocale, V1UnsupportedMode, V1OperationFailed, V1MissingParameters, V1InvalidUserCategory, V1InvalidRegistration,
      V1UnsupportedMimeType;

   /**
    * Allows this behavior to modify the response after the markup has been generated. The default implementation does
    * nothing.
    *
    * @param markupResponse the response that will be passed on to the consumer
    */
   public void modifyResponseIfNeeded(V1MarkupResponse markupResponse)
   {
      // default implementation does not nothing
   }

   public List<String> getSupportedHandles()
   {
      return handles;
   }

   public V1PortletDescription getPortletDescriptionFor(String handle)
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
      V1PortletDescription portletDescription = createPortletDescription(handle, getSuffixFor(handle));
      registry.getServiceDescriptionBehavior().addPortletDescription(portletDescription);
   }

   /** Default implementation doesn't do anything. */
   public void performBlockingInteraction(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1PortletContext portletContext, @WebParam(name = "runtimeContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1RuntimeContext runtimeContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1UserContext userContext, @WebParam(name = "markupParams", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1MarkupParams markupParams, @WebParam(name = "interactionParams", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1InteractionParams interactionParams, @WebParam(mode = WebParam.Mode.OUT, name = "updateResponse", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<V1UpdateResponse> updateResponse, @WebParam(mode = WebParam.Mode.OUT, name = "redirectURL", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<String> redirectURL, @WebParam(mode = WebParam.Mode.OUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<V1Extension>> extensions)
      throws V1UnsupportedMimeType, V1UnsupportedMode, V1UnsupportedWindowState, V1InvalidCookie, V1InvalidSession, V1MissingParameters, V1UnsupportedLocale, V1InconsistentParameters, V1PortletStateChangeRequired, V1InvalidHandle, V1InvalidRegistration, V1InvalidUserCategory, V1AccessDenied, V1OperationFailed
   {
      // do nothing
   }

   public List<V1Extension> releaseSessions(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1RegistrationContext registrationContext, @WebParam(name = "sessionIDs", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> sessionIDs) throws V1MissingParameters, V1InvalidRegistration, V1AccessDenied, V1OperationFailed
   {
      return null;
   }

   public void getMarkup(
      @WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1RegistrationContext registrationContext,
      @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1PortletContext portletContext,
      @WebParam(name = "runtimeContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1RuntimeContext runtimeContext,
      @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1UserContext userContext,
      @WebParam(name = "markupParams", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1MarkupParams markupParams,
      @WebParam(mode = WebParam.Mode.OUT, name = "markupContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<V1MarkupContext> markupContext,
      @WebParam(mode = WebParam.Mode.OUT, name = "sessionContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<V1SessionContext> sessionContext,
      @WebParam(mode = WebParam.Mode.OUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<V1Extension>> extensions)
      throws V1UnsupportedMimeType, V1UnsupportedMode, V1UnsupportedWindowState, V1InvalidCookie, V1InvalidSession, V1MissingParameters, V1UnsupportedLocale, V1InconsistentParameters, V1InvalidHandle, V1InvalidRegistration, V1InvalidUserCategory, V1AccessDenied, V1OperationFailed
   {
      V1GetMarkup gm = new V1GetMarkup();
      gm.setMarkupParams(markupParams);
      gm.setPortletContext(portletContext);
      gm.setRegistrationContext(registrationContext);
      gm.setRuntimeContext(runtimeContext);
      gm.setUserContext(userContext);

      String markupString = getMarkupString(WSRPUtils.getJSR168PortletModeFromWSRPName(markupParams.getMode()),
         WSRPUtils.getJSR168WindowStateFromWSRPName(markupParams.getWindowState()), markupParams.getNavigationalState(), gm);

      markupContext.value = WSRP1TypeFactory.createMarkupContext(MediaType.TEXT_HTML.getValue(), markupString);
      markupContext.value.setRequiresUrlRewriting(Boolean.TRUE);

      V1MarkupResponse markupResponse = WSRP1TypeFactory.createMarkupResponse(markupContext.value);

      modifyResponseIfNeeded(markupResponse);
      
      // some markupbehaviours will have the session context set on the markupResponse object and we need to retrieve from there
      sessionContext.value = markupResponse.getSessionContext();
   }

   public List<V1Extension> initCookie(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1RegistrationContext registrationContext) throws V1InvalidRegistration, V1AccessDenied, V1OperationFailed
   {
      return null;
   }
}
