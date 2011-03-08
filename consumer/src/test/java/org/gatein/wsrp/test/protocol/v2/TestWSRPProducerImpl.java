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
import org.gatein.wsrp.spec.v2.WSRP2ExceptionFactory;
import org.oasis.wsrp.v2.AccessDenied;
import org.oasis.wsrp.v2.CookieProtocol;
import org.oasis.wsrp.v2.CopiedPortlet;
import org.oasis.wsrp.v2.EventDescription;
import org.oasis.wsrp.v2.EventParams;
import org.oasis.wsrp.v2.ExportByValueNotSupported;
import org.oasis.wsrp.v2.ExportDescription;
import org.oasis.wsrp.v2.ExportNoLongerValid;
import org.oasis.wsrp.v2.ExportedPortlet;
import org.oasis.wsrp.v2.Extension;
import org.oasis.wsrp.v2.ExtensionDescription;
import org.oasis.wsrp.v2.FailedPortlets;
import org.oasis.wsrp.v2.GetRegistrationLifetime;
import org.oasis.wsrp.v2.HandleEventsFailed;
import org.oasis.wsrp.v2.ImportPortlet;
import org.oasis.wsrp.v2.ImportPortletsFailed;
import org.oasis.wsrp.v2.ImportedPortlet;
import org.oasis.wsrp.v2.InconsistentParameters;
import org.oasis.wsrp.v2.InteractionParams;
import org.oasis.wsrp.v2.InvalidCookie;
import org.oasis.wsrp.v2.InvalidHandle;
import org.oasis.wsrp.v2.InvalidRegistration;
import org.oasis.wsrp.v2.InvalidSession;
import org.oasis.wsrp.v2.InvalidUserCategory;
import org.oasis.wsrp.v2.ItemDescription;
import org.oasis.wsrp.v2.Lifetime;
import org.oasis.wsrp.v2.MarkupContext;
import org.oasis.wsrp.v2.MarkupParams;
import org.oasis.wsrp.v2.MissingParameters;
import org.oasis.wsrp.v2.ModelDescription;
import org.oasis.wsrp.v2.ModelTypes;
import org.oasis.wsrp.v2.ModifyRegistrationRequired;
import org.oasis.wsrp.v2.OperationFailed;
import org.oasis.wsrp.v2.OperationNotSupported;
import org.oasis.wsrp.v2.PortletContext;
import org.oasis.wsrp.v2.PortletDescription;
import org.oasis.wsrp.v2.PortletLifetime;
import org.oasis.wsrp.v2.PortletStateChangeRequired;
import org.oasis.wsrp.v2.Property;
import org.oasis.wsrp.v2.PropertyList;
import org.oasis.wsrp.v2.RegistrationContext;
import org.oasis.wsrp.v2.RegistrationData;
import org.oasis.wsrp.v2.ResetProperty;
import org.oasis.wsrp.v2.ResourceContext;
import org.oasis.wsrp.v2.ResourceList;
import org.oasis.wsrp.v2.ResourceParams;
import org.oasis.wsrp.v2.ResourceSuspended;
import org.oasis.wsrp.v2.RuntimeContext;
import org.oasis.wsrp.v2.SessionContext;
import org.oasis.wsrp.v2.SetExportLifetime;
import org.oasis.wsrp.v2.SetRegistrationLifetime;
import org.oasis.wsrp.v2.UnsupportedLocale;
import org.oasis.wsrp.v2.UnsupportedMimeType;
import org.oasis.wsrp.v2.UnsupportedMode;
import org.oasis.wsrp.v2.UnsupportedWindowState;
import org.oasis.wsrp.v2.UpdateResponse;
import org.oasis.wsrp.v2.UserContext;
import org.oasis.wsrp.v2.WSRPV2MarkupPortType;
import org.oasis.wsrp.v2.WSRPV2PortletManagementPortType;
import org.oasis.wsrp.v2.WSRPV2RegistrationPortType;
import org.oasis.wsrp.v2.WSRPV2ServiceDescriptionPortType;

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
public class TestWSRPProducerImpl implements TestWSRPProducer, WSRPV2MarkupPortType, WSRPV2PortletManagementPortType, WSRPV2RegistrationPortType, WSRPV2ServiceDescriptionPortType
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


   public void getServiceDescription(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext, @WebParam(name = "desiredLocales", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") List<String> desiredLocales, @WebParam(name = "portletHandles", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") List<String> portletHandles, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext, @WebParam(name = "requiresRegistration", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<Boolean> requiresRegistration, @WebParam(name = "offeredPortlets", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<PortletDescription>> offeredPortlets, @WebParam(name = "userCategoryDescriptions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<ItemDescription>> userCategoryDescriptions, @WebParam(name = "extensionDescriptions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<ExtensionDescription>> extensionDescriptions, @WebParam(name = "customWindowStateDescriptions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<ItemDescription>> customWindowStateDescriptions, @WebParam(name = "customModeDescriptions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<ItemDescription>> customModeDescriptions, @WebParam(name = "requiresInitCookie", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<CookieProtocol> requiresInitCookie, @WebParam(name = "registrationPropertyDescription", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<ModelDescription> registrationPropertyDescription, @WebParam(name = "locales", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<String>> locales, @WebParam(name = "resourceList", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<ResourceList> resourceList, @WebParam(name = "eventDescriptions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<EventDescription>> eventDescriptions, @WebParam(name = "schemaType", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<ModelTypes> schemaType, @WebParam(name = "supportedOptions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<String>> supportedOptions, @WebParam(name = "exportDescription", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<ExportDescription> exportDescription, @WebParam(name = "mayReturnRegistrationState", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<Boolean> mayReturnRegistrationState, @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions) throws InvalidRegistration, ModifyRegistrationRequired, OperationFailed, ResourceSuspended
   {
      getServiceDescriptionBehavior().getServiceDescription(registrationContext, desiredLocales, portletHandles, userContext, requiresRegistration, offeredPortlets, userCategoryDescriptions, extensionDescriptions, customWindowStateDescriptions, customModeDescriptions, requiresInitCookie, registrationPropertyDescription, locales, resourceList, eventDescriptions, schemaType, supportedOptions, exportDescription, mayReturnRegistrationState, extensions);
   }

   // MarkupService implementation *************************************************************************************

   public void getMarkup(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") PortletContext portletContext, @WebParam(name = "runtimeContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RuntimeContext runtimeContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext, @WebParam(name = "markupParams", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") MarkupParams markupParams, @WebParam(name = "markupContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<MarkupContext> markupContext, @WebParam(name = "sessionContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<SessionContext> sessionContext, @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions) throws AccessDenied, InconsistentParameters, InvalidCookie, InvalidHandle, InvalidRegistration, InvalidSession, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, ResourceSuspended, UnsupportedLocale, UnsupportedMimeType, UnsupportedMode, UnsupportedWindowState
   {
      getMarkupBehaviorFor(portletContext.getPortletHandle()).getMarkup(registrationContext, portletContext, runtimeContext, userContext, markupParams, markupContext, sessionContext, extensions);
   }

   public void getResource(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.INOUT) Holder<PortletContext> portletContext, @WebParam(name = "runtimeContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RuntimeContext runtimeContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext, @WebParam(name = "resourceParams", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") ResourceParams resourceParams, @WebParam(name = "resourceContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<ResourceContext> resourceContext, @WebParam(name = "sessionContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<SessionContext> sessionContext, @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions) throws AccessDenied, InconsistentParameters, InvalidCookie, InvalidHandle, InvalidRegistration, InvalidSession, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended, UnsupportedLocale, UnsupportedMimeType, UnsupportedMode, UnsupportedWindowState
   {
      getMarkupBehaviorFor(portletContext.value.getPortletHandle()).getResource(registrationContext, portletContext, runtimeContext, userContext, resourceParams, resourceContext, sessionContext, extensions);
   }

   public void performBlockingInteraction(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") PortletContext portletContext, @WebParam(name = "runtimeContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RuntimeContext runtimeContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext, @WebParam(name = "markupParams", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") MarkupParams markupParams, @WebParam(name = "interactionParams", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") InteractionParams interactionParams, @WebParam(name = "updateResponse", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<UpdateResponse> updateResponse, @WebParam(name = "redirectURL", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<String> redirectURL, @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions) throws AccessDenied, InconsistentParameters, InvalidCookie, InvalidHandle, InvalidRegistration, InvalidSession, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, PortletStateChangeRequired, ResourceSuspended, UnsupportedLocale, UnsupportedMimeType, UnsupportedMode, UnsupportedWindowState
   {
      getMarkupBehaviorFor(portletContext.getPortletHandle()).performBlockingInteraction(registrationContext, portletContext, runtimeContext, userContext, markupParams, interactionParams, updateResponse, redirectURL, extensions);
   }

   public void handleEvents(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") PortletContext portletContext, @WebParam(name = "runtimeContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RuntimeContext runtimeContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext, @WebParam(name = "markupParams", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") MarkupParams markupParams, @WebParam(name = "eventParams", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") EventParams eventParams, @WebParam(name = "updateResponse", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<UpdateResponse> updateResponse, @WebParam(name = "failedEvents", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<HandleEventsFailed>> failedEvents, @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions) throws AccessDenied, InconsistentParameters, InvalidCookie, InvalidHandle, InvalidRegistration, InvalidSession, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, PortletStateChangeRequired, ResourceSuspended, UnsupportedLocale, UnsupportedMimeType, UnsupportedMode, UnsupportedWindowState
   {
      getMarkupBehaviorFor(portletContext.getPortletHandle()).handleEvents(registrationContext, portletContext, runtimeContext, userContext, markupParams, eventParams, updateResponse, failedEvents, extensions);
   }

   public List<Extension> releaseSessions(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext, @WebParam(name = "sessionIDs", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") List<String> sessionIDs, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext) throws AccessDenied, InvalidRegistration, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      throw new NotYetImplemented();
   }

   public List<Extension> initCookie(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext) throws AccessDenied, InvalidRegistration, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      // should only be called if we required cookies to be initialized
      if (requiresInitCookie == null || CookieProtocol.NONE.equals(requiresInitCookie))
      {
         throw WSRP2ExceptionFactory.throwWSException(OperationFailed.class, "Shouldn't have called initCookie", null);
      }

      try
      {
         return getMarkupBehaviorFor(currentMarkupBehaviorHandle).initCookie(registrationContext, userContext);
      }
      catch (InvalidHandle invalidHandle)
      {
         throw WSRP2ExceptionFactory.throwWSException(OperationFailed.class, "Invalid handle", invalidHandle);
      }
   }

   // Registration implementation **************************************************************************************

   public void register(@WebParam(name = "registrationData", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationData registrationData, @WebParam(name = "lifetime", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") Lifetime lifetime, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext, @WebParam(name = "registrationState", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<byte[]> registrationState, @WebParam(name = "scheduledDestruction", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<Lifetime> scheduledDestruction, @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions, @WebParam(name = "registrationHandle", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<String> registrationHandle) throws MissingParameters, OperationFailed, OperationNotSupported
   {
      registrationHandle.value = "registration";
   }

   public List<Extension> deregister(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext) throws InvalidRegistration, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      return null;
   }

   public void modifyRegistration(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext, @WebParam(name = "registrationData", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationData registrationData, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext, @WebParam(name = "registrationState", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<byte[]> registrationState, @WebParam(name = "scheduledDestruction", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<Lifetime> scheduledDestruction, @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions) throws InvalidRegistration, MissingParameters, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      // do nothing
   }

   public Lifetime getRegistrationLifetime(@WebParam(name = "getRegistrationLifetime", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", partName = "getRegistrationLifetime") GetRegistrationLifetime getRegistrationLifetime) throws AccessDenied, InvalidHandle, InvalidRegistration, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      throw new NotYetImplemented();
   }

   public Lifetime setRegistrationLifetime(@WebParam(name = "setRegistrationLifetime", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", partName = "setRegistrationLifetime") SetRegistrationLifetime setRegistrationLifetime) throws AccessDenied, InvalidHandle, InvalidRegistration, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      throw new NotYetImplemented();
   }

   // PortletManagement implementation *********************************************************************************

   public void getPortletDescription(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") PortletContext portletContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext, @WebParam(name = "desiredLocales", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") List<String> desiredLocales, @WebParam(name = "portletDescription", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<PortletDescription> portletDescription, @WebParam(name = "resourceList", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<ResourceList> resourceList, @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions) throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      getPortletManagementBehavior().getPortletDescription(registrationContext, portletContext, userContext, desiredLocales, portletDescription, resourceList, extensions);
   }

   public void clonePortlet(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") PortletContext portletContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext, @WebParam(name = "lifetime", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") Lifetime lifetime, @WebParam(name = "portletHandle", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<String> portletHandle, @WebParam(name = "portletState", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<byte[]> portletState, @WebParam(name = "scheduledDestruction", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<Lifetime> scheduledDestruction, @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions) throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      getPortletManagementBehavior().clonePortlet(registrationContext, portletContext, userContext, lifetime, portletHandle, portletState, scheduledDestruction, extensions);
   }

   public void destroyPortlets(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext, @WebParam(name = "portletHandles", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") List<String> portletHandles, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext, @WebParam(name = "failedPortlets", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<FailedPortlets>> failedPortlets, @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions) throws InconsistentParameters, InvalidRegistration, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      getPortletManagementBehavior().destroyPortlets(registrationContext, portletHandles, userContext, failedPortlets, extensions);
   }

   public void getPortletsLifetime(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") List<PortletContext> portletContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext, @WebParam(name = "portletLifetime", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<PortletLifetime>> portletLifetime, @WebParam(name = "failedPortlets", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<FailedPortlets>> failedPortlets, @WebParam(name = "resourceList", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<ResourceList> resourceList, @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions) throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      getPortletManagementBehavior().getPortletsLifetime(registrationContext, portletContext, userContext, portletLifetime, failedPortlets, resourceList, extensions);
   }

   public void setPortletsLifetime(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") List<PortletContext> portletContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext, @WebParam(name = "lifetime", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") Lifetime lifetime, @WebParam(name = "updatedPortlet", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<PortletLifetime>> updatedPortlet, @WebParam(name = "failedPortlets", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<FailedPortlets>> failedPortlets, @WebParam(name = "resourceList", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<ResourceList> resourceList, @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions) throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      getPortletManagementBehavior().setPortletsLifetime(registrationContext, portletContext, userContext, lifetime, updatedPortlet, failedPortlets, resourceList, extensions);
   }

   public void copyPortlets(@WebParam(name = "toRegistrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext toRegistrationContext, @WebParam(name = "toUserContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext toUserContext, @WebParam(name = "fromRegistrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext fromRegistrationContext, @WebParam(name = "fromUserContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext fromUserContext, @WebParam(name = "fromPortletContexts", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") List<PortletContext> fromPortletContexts, @WebParam(name = "lifetime", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") Lifetime lifetime, @WebParam(name = "copiedPortlets", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<CopiedPortlet>> copiedPortlets, @WebParam(name = "failedPortlets", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<FailedPortlets>> failedPortlets, @WebParam(name = "resourceList", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<ResourceList> resourceList, @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions) throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      getPortletManagementBehavior().copyPortlets(toRegistrationContext, toUserContext, fromRegistrationContext, fromUserContext, fromPortletContexts, lifetime, copiedPortlets, failedPortlets, resourceList, extensions);
   }

   public void exportPortlets(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") List<PortletContext> portletContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext, @WebParam(name = "lifetime", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.INOUT) Holder<Lifetime> lifetime, @WebParam(name = "exportByValueRequired", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") Boolean exportByValueRequired, @WebParam(name = "exportContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<byte[]> exportContext, @WebParam(name = "exportedPortlet", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<ExportedPortlet>> exportedPortlet, @WebParam(name = "failedPortlets", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<FailedPortlets>> failedPortlets, @WebParam(name = "resourceList", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<ResourceList> resourceList, @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions) throws AccessDenied, ExportByValueNotSupported, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      getPortletManagementBehavior().exportPortlets(registrationContext, portletContext, userContext, lifetime, exportByValueRequired, exportContext, exportedPortlet, failedPortlets, resourceList, extensions);
   }

   public void importPortlets(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext, @WebParam(name = "importContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") byte[] importContext, @WebParam(name = "importPortlet", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") List<ImportPortlet> importPortlet, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext, @WebParam(name = "lifetime", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") Lifetime lifetime, @WebParam(name = "importedPortlets", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<ImportedPortlet>> importedPortlets, @WebParam(name = "importFailed", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<ImportPortletsFailed>> importFailed, @WebParam(name = "resourceList", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<ResourceList> resourceList, @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions) throws AccessDenied, ExportNoLongerValid, InconsistentParameters, InvalidRegistration, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      getPortletManagementBehavior().importPortlets(registrationContext, importContext, importPortlet, userContext, lifetime, importedPortlets, importFailed, resourceList, extensions);
   }

   public List<Extension> releaseExport(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext, @WebParam(name = "exportContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") byte[] exportContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext)
   {
      return getPortletManagementBehavior().releaseExport(registrationContext, exportContext, userContext);
   }

   public Lifetime setExportLifetime(@WebParam(name = "setExportLifetime", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", partName = "setExportLifetime") SetExportLifetime setExportLifetime) throws AccessDenied, InvalidHandle, InvalidRegistration, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      return getPortletManagementBehavior().setExportLifetime(setExportLifetime);
   }

   public void setPortletProperties(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") PortletContext portletContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext, @WebParam(name = "propertyList", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") PropertyList propertyList, @WebParam(name = "portletHandle", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<String> portletHandle, @WebParam(name = "portletState", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<byte[]> portletState, @WebParam(name = "scheduledDestruction", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<Lifetime> scheduledDestruction, @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions) throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      getPortletManagementBehavior().setPortletProperties(registrationContext, portletContext, userContext, propertyList, portletHandle, portletState, scheduledDestruction, extensions);
   }

   public void getPortletProperties(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") PortletContext portletContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext, @WebParam(name = "names", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") List<String> names, @WebParam(name = "properties", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Property>> properties, @WebParam(name = "resetProperties", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<ResetProperty>> resetProperties, @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions) throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      getPortletManagementBehavior().getPortletProperties(registrationContext, portletContext, userContext, names, properties, resetProperties, extensions);
   }

   public void getPortletPropertyDescription(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") PortletContext portletContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext, @WebParam(name = "desiredLocales", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") List<String> desiredLocales, @WebParam(name = "modelDescription", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<ModelDescription> modelDescription, @WebParam(name = "resourceList", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<ResourceList> resourceList, @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions) throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      getPortletManagementBehavior().getPortletPropertyDescription(registrationContext, portletContext, userContext, desiredLocales, modelDescription, resourceList, extensions);
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
