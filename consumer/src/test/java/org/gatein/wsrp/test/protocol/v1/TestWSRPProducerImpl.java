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

import org.gatein.wsrp.spec.v1.WSRP1ExceptionFactory;
import org.oasis.wsrp.v1.V1AccessDenied;
import org.oasis.wsrp.v1.V1CookieProtocol;
import org.oasis.wsrp.v1.V1DestroyFailed;
import org.oasis.wsrp.v1.V1Extension;
import org.oasis.wsrp.v1.V1InconsistentParameters;
import org.oasis.wsrp.v1.V1InteractionParams;
import org.oasis.wsrp.v1.V1InvalidCookie;
import org.oasis.wsrp.v1.V1InvalidHandle;
import org.oasis.wsrp.v1.V1InvalidRegistration;
import org.oasis.wsrp.v1.V1InvalidSession;
import org.oasis.wsrp.v1.V1InvalidUserCategory;
import org.oasis.wsrp.v1.V1ItemDescription;
import org.oasis.wsrp.v1.V1MarkupContext;
import org.oasis.wsrp.v1.V1MarkupParams;
import org.oasis.wsrp.v1.V1MissingParameters;
import org.oasis.wsrp.v1.V1ModelDescription;
import org.oasis.wsrp.v1.V1OperationFailed;
import org.oasis.wsrp.v1.V1PortletContext;
import org.oasis.wsrp.v1.V1PortletDescription;
import org.oasis.wsrp.v1.V1PortletStateChangeRequired;
import org.oasis.wsrp.v1.V1Property;
import org.oasis.wsrp.v1.V1PropertyList;
import org.oasis.wsrp.v1.V1RegistrationContext;
import org.oasis.wsrp.v1.V1RegistrationData;
import org.oasis.wsrp.v1.V1ResetProperty;
import org.oasis.wsrp.v1.V1ResourceList;
import org.oasis.wsrp.v1.V1RuntimeContext;
import org.oasis.wsrp.v1.V1SessionContext;
import org.oasis.wsrp.v1.V1UnsupportedLocale;
import org.oasis.wsrp.v1.V1UnsupportedMimeType;
import org.oasis.wsrp.v1.V1UnsupportedMode;
import org.oasis.wsrp.v1.V1UnsupportedWindowState;
import org.oasis.wsrp.v1.V1UpdateResponse;
import org.oasis.wsrp.v1.V1UserContext;
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

   private V1CookieProtocol requiresInitCookie = V1CookieProtocol.NONE;

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

   private MarkupBehavior getMarkupBehaviorFor(String portletHandle) throws V1InvalidHandle
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


   public void getServiceDescription(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1RegistrationContext registrationContext, @WebParam(name = "desiredLocales", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> desiredLocales, @WebParam(mode = WebParam.Mode.OUT, name = "requiresRegistration", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<Boolean> requiresRegistration, @WebParam(mode = WebParam.Mode.OUT, name = "offeredPortlets", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<V1PortletDescription>> offeredPortlets, @WebParam(mode = WebParam.Mode.OUT, name = "userCategoryDescriptions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<V1ItemDescription>> userCategoryDescriptions, @WebParam(mode = WebParam.Mode.OUT, name = "customUserProfileItemDescriptions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<V1ItemDescription>> customUserProfileItemDescriptions, @WebParam(mode = WebParam.Mode.OUT, name = "customWindowStateDescriptions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<V1ItemDescription>> customWindowStateDescriptions, @WebParam(mode = WebParam.Mode.OUT, name = "customModeDescriptions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<V1ItemDescription>> customModeDescriptions, @WebParam(mode = WebParam.Mode.OUT, name = "requiresInitCookie", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<V1CookieProtocol> requiresInitCookie, @WebParam(mode = WebParam.Mode.OUT, name = "registrationPropertyDescription", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<V1ModelDescription> registrationPropertyDescription, @WebParam(mode = WebParam.Mode.OUT, name = "locales", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<String>> locales, @WebParam(mode = WebParam.Mode.OUT, name = "resourceList", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<V1ResourceList> resourceList, @WebParam(mode = WebParam.Mode.OUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<V1Extension>> extensions) throws V1InvalidRegistration, V1OperationFailed
   {
      getServiceDescriptionBehavior().getServiceDescription(registrationContext, desiredLocales, requiresRegistration,
         offeredPortlets, userCategoryDescriptions, customUserProfileItemDescriptions, customWindowStateDescriptions,
         customModeDescriptions, requiresInitCookie, registrationPropertyDescription, locales, resourceList, extensions);
   }

   // MarkupService implementation *************************************************************************************


   public void performBlockingInteraction(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1PortletContext portletContext, @WebParam(name = "runtimeContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1RuntimeContext runtimeContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1UserContext userContext, @WebParam(name = "markupParams", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1MarkupParams markupParams, @WebParam(name = "interactionParams", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1InteractionParams interactionParams, @WebParam(mode = WebParam.Mode.OUT, name = "updateResponse", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<V1UpdateResponse> updateResponse, @WebParam(mode = WebParam.Mode.OUT, name = "redirectURL", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<String> redirectURL, @WebParam(mode = WebParam.Mode.OUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<V1Extension>> extensions) throws V1UnsupportedMimeType, V1UnsupportedMode, V1UnsupportedWindowState, V1InvalidCookie, V1InvalidSession, V1MissingParameters, V1UnsupportedLocale, V1InconsistentParameters, V1PortletStateChangeRequired, V1InvalidHandle, V1InvalidRegistration, V1InvalidUserCategory, V1AccessDenied, V1OperationFailed
   {
      getMarkupBehaviorFor(portletContext.getPortletHandle()).performBlockingInteraction(registrationContext, portletContext, runtimeContext, userContext, markupParams, interactionParams, updateResponse, redirectURL, extensions);
   }

   public List<V1Extension> releaseSessions(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1RegistrationContext registrationContext, @WebParam(name = "sessionIDs", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> sessionIDs) throws V1MissingParameters, V1InvalidRegistration, V1AccessDenied, V1OperationFailed
   {
      return null;
   }

   public void getMarkup(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1PortletContext portletContext, @WebParam(name = "runtimeContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1RuntimeContext runtimeContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1UserContext userContext, @WebParam(name = "markupParams", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1MarkupParams markupParams, @WebParam(mode = WebParam.Mode.OUT, name = "markupContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<V1MarkupContext> markupContext, @WebParam(mode = WebParam.Mode.OUT, name = "sessionContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<V1SessionContext> sessionContext, @WebParam(mode = WebParam.Mode.OUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<V1Extension>> extensions) throws V1UnsupportedMimeType, V1UnsupportedMode, V1UnsupportedWindowState, V1InvalidCookie, V1InvalidSession, V1MissingParameters, V1UnsupportedLocale, V1InconsistentParameters, V1InvalidHandle, V1InvalidRegistration, V1InvalidUserCategory, V1AccessDenied, V1OperationFailed
   {
      getMarkupBehaviorFor(portletContext.getPortletHandle()).getMarkup(registrationContext, portletContext, runtimeContext, userContext, markupParams, markupContext, sessionContext, extensions);
   }

   public List<V1Extension> initCookie(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1RegistrationContext registrationContext) throws V1InvalidRegistration, V1AccessDenied, V1OperationFailed
   {
      // should only be called if we required cookies to be initialized
      if (requiresInitCookie == null || V1CookieProtocol.NONE.equals(requiresInitCookie))
      {
         throw WSRP1ExceptionFactory.throwWSException(V1OperationFailed.class, "Shouldn't have called initCookie", null);
      }

      try
      {
         return getMarkupBehaviorFor(currentMarkupBehaviorHandle).initCookie(registrationContext);
      }
      catch (V1InvalidHandle invalidHandle)
      {
         throw WSRP1ExceptionFactory.throwWSException(V1OperationFailed.class, "Invalid handle", invalidHandle);
      }
   }

   // Registration implementation **************************************************************************************


   public void register(@WebParam(name = "consumerName", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") String consumerName, @WebParam(name = "consumerAgent", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") String consumerAgent, @WebParam(name = "methodGetSupported", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") boolean methodGetSupported, @WebParam(name = "consumerModes", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> consumerModes, @WebParam(name = "consumerWindowStates", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> consumerWindowStates, @WebParam(name = "consumerUserScopes", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> consumerUserScopes, @WebParam(name = "customUserProfileData", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> customUserProfileData, @WebParam(name = "registrationProperties", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<V1Property> registrationProperties, @WebParam(mode = WebParam.Mode.INOUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<V1Extension>> extensions, @WebParam(mode = WebParam.Mode.OUT, name = "registrationHandle", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<String> registrationHandle, @WebParam(mode = WebParam.Mode.OUT, name = "registrationState", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<byte[]> registrationState) throws V1MissingParameters, V1OperationFailed
   {
      registrationHandle.value = "registration";
   }

   public void modifyRegistration(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1RegistrationContext registrationContext, @WebParam(name = "registrationData", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1RegistrationData registrationData, @WebParam(mode = WebParam.Mode.OUT, name = "registrationState", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<byte[]> registrationState, @WebParam(mode = WebParam.Mode.OUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<V1Extension>> extensions) throws V1MissingParameters, V1InvalidRegistration, V1OperationFailed
   {
      // do nothing
   }

   public void deregister(@WebParam(name = "registrationHandle", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") String registrationHandle, @WebParam(name = "registrationState", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") byte[] registrationState, @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types", mode = WebParam.Mode.INOUT) Holder<List<V1Extension>> extensions) throws V1InvalidRegistration, V1OperationFailed
   {
      // do nothing
   }

   // PortletManagement implementation *********************************************************************************

   public void getPortletPropertyDescription(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1PortletContext portletContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1UserContext userContext, @WebParam(name = "desiredLocales", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> desiredLocales, @WebParam(mode = WebParam.Mode.OUT, name = "modelDescription", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<V1ModelDescription> modelDescription, @WebParam(mode = WebParam.Mode.OUT, name = "resourceList", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<V1ResourceList> resourceList, @WebParam(mode = WebParam.Mode.OUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<V1Extension>> extensions) throws V1MissingParameters, V1InconsistentParameters, V1InvalidHandle, V1InvalidRegistration, V1InvalidUserCategory, V1AccessDenied, V1OperationFailed
   {
      getPortletManagementBehavior().getPortletPropertyDescription(registrationContext, portletContext, userContext, desiredLocales, modelDescription, resourceList, extensions);
   }

   public void setPortletProperties(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1PortletContext portletContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1UserContext userContext, @WebParam(name = "propertyList", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1PropertyList propertyList, @WebParam(mode = WebParam.Mode.OUT, name = "portletHandle", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<String> portletHandle, @WebParam(mode = WebParam.Mode.OUT, name = "portletState", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<byte[]> portletState, @WebParam(mode = WebParam.Mode.OUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<V1Extension>> extensions) throws V1MissingParameters, V1InconsistentParameters, V1InvalidHandle, V1InvalidRegistration, V1InvalidUserCategory, V1AccessDenied, V1OperationFailed
   {
      getPortletManagementBehavior().setPortletProperties(registrationContext, portletContext, userContext, propertyList, portletHandle, portletState, extensions);
   }

   public void clonePortlet(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1PortletContext portletContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1UserContext userContext, @WebParam(mode = WebParam.Mode.OUT, name = "portletHandle", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<String> portletHandle, @WebParam(mode = WebParam.Mode.OUT, name = "portletState", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<byte[]> portletState, @WebParam(mode = WebParam.Mode.OUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<V1Extension>> extensions) throws V1MissingParameters, V1InconsistentParameters, V1InvalidHandle, V1InvalidRegistration, V1InvalidUserCategory, V1AccessDenied, V1OperationFailed
   {
      getPortletManagementBehavior().clonePortlet(registrationContext, portletContext, userContext, portletHandle, portletState, extensions);
   }

   public void getPortletDescription(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1PortletContext portletContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1UserContext userContext, @WebParam(name = "desiredLocales", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> desiredLocales, @WebParam(mode = WebParam.Mode.OUT, name = "portletDescription", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<V1PortletDescription> portletDescription, @WebParam(mode = WebParam.Mode.OUT, name = "resourceList", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<V1ResourceList> resourceList, @WebParam(mode = WebParam.Mode.OUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<V1Extension>> extensions) throws V1MissingParameters, V1InconsistentParameters, V1InvalidHandle, V1InvalidRegistration, V1InvalidUserCategory, V1AccessDenied, V1OperationFailed
   {
      getPortletManagementBehavior().getPortletDescription(registrationContext, portletContext, userContext, desiredLocales, portletDescription, resourceList, extensions);
   }

   public void getPortletProperties(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1PortletContext portletContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1UserContext userContext, @WebParam(name = "names", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> names, @WebParam(mode = WebParam.Mode.OUT, name = "properties", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<V1Property>> properties, @WebParam(mode = WebParam.Mode.OUT, name = "resetProperties", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<V1ResetProperty>> resetProperties, @WebParam(mode = WebParam.Mode.OUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<V1Extension>> extensions) throws V1MissingParameters, V1InconsistentParameters, V1InvalidHandle, V1InvalidRegistration, V1InvalidUserCategory, V1AccessDenied, V1OperationFailed
   {
      getPortletManagementBehavior().getPortletProperties(registrationContext, portletContext, userContext, names, properties, resetProperties, extensions);
   }

   public void destroyPortlets(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1RegistrationContext registrationContext, @WebParam(name = "portletHandles", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> portletHandles, @WebParam(mode = WebParam.Mode.OUT, name = "destroyFailed", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<V1DestroyFailed>> destroyFailed, @WebParam(mode = WebParam.Mode.OUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<V1Extension>> extensions) throws V1MissingParameters, V1InconsistentParameters, V1InvalidRegistration, V1OperationFailed
   {
      getPortletManagementBehavior().destroyPortlets(registrationContext, portletHandles, destroyFailed, extensions);
   }

   // Producer implementation ******************************************************************************************

   public V1CookieProtocol getRequiresInitCookie()
   {
      return requiresInitCookie;
   }

   public void setRequiresInitCookie(String cookieProtocolAsString)
   {
      V1CookieProtocol protocol = V1CookieProtocol.fromValue(cookieProtocolAsString);
      this.requiresInitCookie = protocol;
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
