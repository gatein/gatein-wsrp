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

package org.gatein.wsrp.test;

import org.gatein.wsrp.test.protocol.v1.MarkupBehavior;
import org.gatein.wsrp.test.protocol.v1.PortletManagementBehavior;
import org.gatein.wsrp.test.protocol.v1.ServiceDescriptionBehavior;
import org.oasis.wsrp.v1.AccessDenied;
import org.oasis.wsrp.v1.CookieProtocol;
import org.oasis.wsrp.v1.DestroyFailed;
import org.oasis.wsrp.v1.Extension;
import org.oasis.wsrp.v1.InconsistentParameters;
import org.oasis.wsrp.v1.InteractionParams;
import org.oasis.wsrp.v1.InvalidCookie;
import org.oasis.wsrp.v1.InvalidHandle;
import org.oasis.wsrp.v1.InvalidRegistration;
import org.oasis.wsrp.v1.InvalidSession;
import org.oasis.wsrp.v1.InvalidUserCategory;
import org.oasis.wsrp.v1.ItemDescription;
import org.oasis.wsrp.v1.MarkupContext;
import org.oasis.wsrp.v1.MarkupParams;
import org.oasis.wsrp.v1.MissingParameters;
import org.oasis.wsrp.v1.ModelDescription;
import org.oasis.wsrp.v1.OperationFailed;
import org.oasis.wsrp.v1.PortletContext;
import org.oasis.wsrp.v1.PortletDescription;
import org.oasis.wsrp.v1.PortletStateChangeRequired;
import org.oasis.wsrp.v1.Property;
import org.oasis.wsrp.v1.PropertyList;
import org.oasis.wsrp.v1.RegistrationContext;
import org.oasis.wsrp.v1.RegistrationData;
import org.oasis.wsrp.v1.ResetProperty;
import org.oasis.wsrp.v1.ResourceList;
import org.oasis.wsrp.v1.RuntimeContext;
import org.oasis.wsrp.v1.SessionContext;
import org.oasis.wsrp.v1.UnsupportedLocale;
import org.oasis.wsrp.v1.UnsupportedMimeType;
import org.oasis.wsrp.v1.UnsupportedMode;
import org.oasis.wsrp.v1.UnsupportedWindowState;
import org.oasis.wsrp.v1.UpdateResponse;
import org.oasis.wsrp.v1.UserContext;
import org.oasis.wsrp.v1.WSRPV1MarkupPortType;
import org.oasis.wsrp.v1.WSRPV1PortletManagementPortType;
import org.oasis.wsrp.v1.WSRPV1RegistrationPortType;
import org.oasis.wsrp.v1.WSRPV1ServiceDescriptionPortType;

import javax.jws.WebParam;
import javax.xml.ws.Holder;
import java.util.List;

/**
 * This is dummy clone of org.jboss.portal.wsrp.producer.WSRPProducerImpl customizable from client side. Just for
 * consumer implementation testing purposes.
 *
 * @author <a href="mailto:Boleslaw.Dawidowicz@jboss.com">Boleslaw Dawidowicz</a>
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 12020 $
 * @since 2.4
 */
public class TestWSRPProducerImpl implements TestWSRPProducer, WSRPV1MarkupPortType, WSRPV1PortletManagementPortType, WSRPV1RegistrationPortType, WSRPV1ServiceDescriptionPortType
{
   private int sessionExpirationTime = 600;

   private CookieProtocol requiresInitCookie = CookieProtocol.NONE;

   private BehaviorRegistry behaviorRegistry = new BehaviorRegistry();

   public static final String USER_COOKIE = "cookie";

   private String currentMarkupBehaviorHandle;

   private boolean strict = true;

   public TestWSRPProducerImpl()
   {
      reset();
   }

   public BehaviorRegistry getBehaviorRegistry()
   {
      return behaviorRegistry;
   }

   public void setCurrentMarkupBehaviorHandle(String handle)
   {
      currentMarkupBehaviorHandle = handle;
   }

   public void reset()
   {
      requiresInitCookie = null;
      currentMarkupBehaviorHandle = null;
      behaviorRegistry.clear();
   }

   private MarkupBehavior getMarkupBehaviorFor(String portletHandle) throws InvalidHandle
   {
      return behaviorRegistry.getMarkupBehaviorFor(portletHandle);
   }

   private ServiceDescriptionBehavior getServiceDescriptionBehavior()
   {
      return behaviorRegistry.getServiceDescriptionBehavior();
   }

   public PortletManagementBehavior getPortletManagementBehavior()
   {
      return behaviorRegistry.getPortletManagementBehavior();
   }

   // ServiceDescription implementation ********************************************************************************


   public void getServiceDescription(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") RegistrationContext registrationContext, @WebParam(name = "desiredLocales", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> desiredLocales, @WebParam(mode = WebParam.Mode.OUT, name = "requiresRegistration", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<Boolean> requiresRegistration, @WebParam(mode = WebParam.Mode.OUT, name = "offeredPortlets", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<PortletDescription>> offeredPortlets, @WebParam(mode = WebParam.Mode.OUT, name = "userCategoryDescriptions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<ItemDescription>> userCategoryDescriptions, @WebParam(mode = WebParam.Mode.OUT, name = "customUserProfileItemDescriptions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<ItemDescription>> customUserProfileItemDescriptions, @WebParam(mode = WebParam.Mode.OUT, name = "customWindowStateDescriptions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<ItemDescription>> customWindowStateDescriptions, @WebParam(mode = WebParam.Mode.OUT, name = "customModeDescriptions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<ItemDescription>> customModeDescriptions, @WebParam(mode = WebParam.Mode.OUT, name = "requiresInitCookie", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<CookieProtocol> requiresInitCookie, @WebParam(mode = WebParam.Mode.OUT, name = "registrationPropertyDescription", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<ModelDescription> registrationPropertyDescription, @WebParam(mode = WebParam.Mode.OUT, name = "locales", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<String>> locales, @WebParam(mode = WebParam.Mode.OUT, name = "resourceList", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<ResourceList> resourceList, @WebParam(mode = WebParam.Mode.OUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<Extension>> extensions) throws InvalidRegistration, OperationFailed
   {
      getServiceDescriptionBehavior().getServiceDescription(registrationContext, desiredLocales, requiresRegistration,
         offeredPortlets, userCategoryDescriptions, customUserProfileItemDescriptions, customWindowStateDescriptions,
         customModeDescriptions, requiresInitCookie, registrationPropertyDescription, locales, resourceList, extensions);
   }

   // MarkupService implementation *************************************************************************************


   public void performBlockingInteraction(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") PortletContext portletContext, @WebParam(name = "runtimeContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") RuntimeContext runtimeContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") UserContext userContext, @WebParam(name = "markupParams", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") MarkupParams markupParams, @WebParam(name = "interactionParams", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") InteractionParams interactionParams, @WebParam(mode = WebParam.Mode.OUT, name = "updateResponse", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<UpdateResponse> updateResponse, @WebParam(mode = WebParam.Mode.OUT, name = "redirectURL", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<String> redirectURL, @WebParam(mode = WebParam.Mode.OUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<Extension>> extensions) throws UnsupportedMimeType, UnsupportedMode, UnsupportedWindowState, InvalidCookie, InvalidSession, MissingParameters, UnsupportedLocale, InconsistentParameters, PortletStateChangeRequired, InvalidHandle, InvalidRegistration, InvalidUserCategory, AccessDenied, OperationFailed
   {
      getMarkupBehaviorFor(portletContext.getPortletHandle()).performBlockingInteraction(registrationContext, portletContext, runtimeContext, userContext, markupParams, interactionParams, updateResponse, redirectURL, extensions);
   }

   public List<Extension> releaseSessions(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") RegistrationContext registrationContext, @WebParam(name = "sessionIDs", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> sessionIDs) throws MissingParameters, InvalidRegistration, AccessDenied, OperationFailed
   {
      return null;
   }

   public void getMarkup(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") PortletContext portletContext, @WebParam(name = "runtimeContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") RuntimeContext runtimeContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") UserContext userContext, @WebParam(name = "markupParams", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") MarkupParams markupParams, @WebParam(mode = WebParam.Mode.OUT, name = "markupContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<MarkupContext> markupContext, @WebParam(mode = WebParam.Mode.OUT, name = "sessionContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<SessionContext> sessionContext, @WebParam(mode = WebParam.Mode.OUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<Extension>> extensions) throws UnsupportedMimeType, UnsupportedMode, UnsupportedWindowState, InvalidCookie, InvalidSession, MissingParameters, UnsupportedLocale, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory, AccessDenied, OperationFailed
   {
      getMarkupBehaviorFor(portletContext.getPortletHandle()).getMarkup(registrationContext, portletContext, runtimeContext, userContext, markupParams, markupContext, sessionContext, extensions);
   }

   public List<Extension> initCookie(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") RegistrationContext registrationContext) throws InvalidRegistration, AccessDenied, OperationFailed
   {
      // should only be called if we required cookies to be initialized
      if (requiresInitCookie == null || CookieProtocol.NONE.equals(requiresInitCookie))
      {
         throw new OperationFailed();
      }

      try
      {
         return getMarkupBehaviorFor(currentMarkupBehaviorHandle).initCookie(registrationContext);
      }
      catch (InvalidHandle invalidHandle)
      {
         throw new OperationFailed("Invalid handle", invalidHandle);
      }
   }

   // Registration implementation **************************************************************************************


   public void register(@WebParam(name = "consumerName", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") String consumerName, @WebParam(name = "consumerAgent", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") String consumerAgent, @WebParam(name = "methodGetSupported", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") boolean methodGetSupported, @WebParam(name = "consumerModes", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> consumerModes, @WebParam(name = "consumerWindowStates", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> consumerWindowStates, @WebParam(name = "consumerUserScopes", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> consumerUserScopes, @WebParam(name = "customUserProfileData", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> customUserProfileData, @WebParam(name = "registrationProperties", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<Property> registrationProperties, @WebParam(mode = WebParam.Mode.INOUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<Extension>> extensions, @WebParam(mode = WebParam.Mode.OUT, name = "registrationHandle", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<String> registrationHandle, @WebParam(mode = WebParam.Mode.OUT, name = "registrationState", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<byte[]> registrationState) throws MissingParameters, OperationFailed
   {
      registrationHandle.value = "registration";
   }

   public void modifyRegistration(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") RegistrationContext registrationContext, @WebParam(name = "registrationData", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") RegistrationData registrationData, @WebParam(mode = WebParam.Mode.OUT, name = "registrationState", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<byte[]> registrationState, @WebParam(mode = WebParam.Mode.OUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<Extension>> extensions) throws MissingParameters, InvalidRegistration, OperationFailed
   {
      // do nothing
   }

   public List<Extension> deregister(@WebParam(name = "registrationHandle", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") String registrationHandle, @WebParam(name = "registrationState", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") byte[] registrationState, @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<Extension> extensions) throws InvalidRegistration, OperationFailed
   {
      return null;  // do nothing
   }

   // PortletManagement implementation *********************************************************************************

   public void getPortletPropertyDescription(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") PortletContext portletContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") UserContext userContext, @WebParam(name = "desiredLocales", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> desiredLocales, @WebParam(mode = WebParam.Mode.OUT, name = "modelDescription", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<ModelDescription> modelDescription, @WebParam(mode = WebParam.Mode.OUT, name = "resourceList", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<ResourceList> resourceList, @WebParam(mode = WebParam.Mode.OUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<Extension>> extensions) throws MissingParameters, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory, AccessDenied, OperationFailed
   {
      getPortletManagementBehavior().getPortletPropertyDescription(registrationContext, portletContext, userContext, desiredLocales, modelDescription, resourceList, extensions);
   }

   public void setPortletProperties(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") PortletContext portletContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") UserContext userContext, @WebParam(name = "propertyList", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") PropertyList propertyList, @WebParam(mode = WebParam.Mode.OUT, name = "portletHandle", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<String> portletHandle, @WebParam(mode = WebParam.Mode.OUT, name = "portletState", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<byte[]> portletState, @WebParam(mode = WebParam.Mode.OUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<Extension>> extensions) throws MissingParameters, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory, AccessDenied, OperationFailed
   {
      getPortletManagementBehavior().setPortletProperties(registrationContext, portletContext, userContext, propertyList, portletHandle, portletState, extensions);
   }

   public void clonePortlet(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") PortletContext portletContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") UserContext userContext, @WebParam(mode = WebParam.Mode.OUT, name = "portletHandle", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<String> portletHandle, @WebParam(mode = WebParam.Mode.OUT, name = "portletState", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<byte[]> portletState, @WebParam(mode = WebParam.Mode.OUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<Extension>> extensions) throws MissingParameters, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory, AccessDenied, OperationFailed
   {
      getPortletManagementBehavior().clonePortlet(registrationContext, portletContext, userContext, portletHandle, portletState, extensions);
   }

   public void getPortletDescription(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") PortletContext portletContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") UserContext userContext, @WebParam(name = "desiredLocales", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> desiredLocales, @WebParam(mode = WebParam.Mode.OUT, name = "portletDescription", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<PortletDescription> portletDescription, @WebParam(mode = WebParam.Mode.OUT, name = "resourceList", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<ResourceList> resourceList, @WebParam(mode = WebParam.Mode.OUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<Extension>> extensions) throws MissingParameters, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory, AccessDenied, OperationFailed
   {
      getPortletManagementBehavior().getPortletDescription(registrationContext, portletContext, userContext, desiredLocales, portletDescription, resourceList, extensions);
   }

   public void getPortletProperties(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") PortletContext portletContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") UserContext userContext, @WebParam(name = "names", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> names, @WebParam(mode = WebParam.Mode.OUT, name = "properties", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<Property>> properties, @WebParam(mode = WebParam.Mode.OUT, name = "resetProperties", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<ResetProperty>> resetProperties, @WebParam(mode = WebParam.Mode.OUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<Extension>> extensions) throws MissingParameters, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory, AccessDenied, OperationFailed
   {
      getPortletManagementBehavior().getPortletProperties(registrationContext, portletContext, userContext, names, properties, resetProperties, extensions);
   }

   public void destroyPortlets(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") RegistrationContext registrationContext, @WebParam(name = "portletHandles", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> portletHandles, @WebParam(mode = WebParam.Mode.OUT, name = "destroyFailed", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<DestroyFailed>> destroyFailed, @WebParam(mode = WebParam.Mode.OUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<Extension>> extensions) throws MissingParameters, InconsistentParameters, InvalidRegistration, OperationFailed
   {
      getPortletManagementBehavior().destroyPortlets(registrationContext, portletHandles, destroyFailed, extensions);
   }

   // Producer implementation ******************************************************************************************

   public CookieProtocol getRequiresInitCookie()
   {
      return requiresInitCookie;
   }

   public void setRequiresInitCookie(CookieProtocol requiresInitCookie)
   {
      this.requiresInitCookie = requiresInitCookie;
      getServiceDescriptionBehavior().setRequiresInitCookie(requiresInitCookie);
   }

   public int getSessionExpirationTime()
   {
      return sessionExpirationTime;
   }

   public void setSessionExpirationTime(int sessionExpirationTime)
   {
      this.sessionExpirationTime = sessionExpirationTime;
   }

   public void usingStrictModeChangedTo(boolean strictMode)
   {
//      WSRPValidator.setStrict(strictMode);
      strict = strictMode;
   }
}
