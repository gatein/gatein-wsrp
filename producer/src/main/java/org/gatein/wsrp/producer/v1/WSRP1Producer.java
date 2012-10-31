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

package org.gatein.wsrp.producer.v1;

import org.gatein.exports.ExportManager;
import org.gatein.pc.api.Portlet;
import org.gatein.pc.api.PortletInvoker;
import org.gatein.pc.api.PortletInvokerException;
import org.gatein.pc.portlet.container.managed.ManagedObjectRegistryEvent;
import org.gatein.registration.Registration;
import org.gatein.registration.RegistrationManager;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.api.context.ProducerContext;
import org.gatein.wsrp.producer.ProducerHolder;
import org.gatein.wsrp.producer.WSRPProducer;
import org.gatein.wsrp.producer.config.ProducerConfigurationService;
import org.gatein.wsrp.producer.handlers.processors.ProducerHelper;
import org.gatein.wsrp.producer.v2.WSRP2Producer;
import org.gatein.wsrp.spec.v1.V1ToV2Converter;
import org.gatein.wsrp.spec.v1.V2ToV1Converter;
import org.gatein.wsrp.spec.v1.WSRP1ExceptionFactory;
import org.oasis.wsrp.v1.V1AccessDenied;
import org.oasis.wsrp.v1.V1BlockingInteractionResponse;
import org.oasis.wsrp.v1.V1ClonePortlet;
import org.oasis.wsrp.v1.V1DestroyPortlets;
import org.oasis.wsrp.v1.V1DestroyPortletsResponse;
import org.oasis.wsrp.v1.V1GetMarkup;
import org.oasis.wsrp.v1.V1GetPortletDescription;
import org.oasis.wsrp.v1.V1GetPortletProperties;
import org.oasis.wsrp.v1.V1GetPortletPropertyDescription;
import org.oasis.wsrp.v1.V1GetServiceDescription;
import org.oasis.wsrp.v1.V1InconsistentParameters;
import org.oasis.wsrp.v1.V1InitCookie;
import org.oasis.wsrp.v1.V1InvalidCookie;
import org.oasis.wsrp.v1.V1InvalidHandle;
import org.oasis.wsrp.v1.V1InvalidRegistration;
import org.oasis.wsrp.v1.V1InvalidSession;
import org.oasis.wsrp.v1.V1InvalidUserCategory;
import org.oasis.wsrp.v1.V1MarkupResponse;
import org.oasis.wsrp.v1.V1MissingParameters;
import org.oasis.wsrp.v1.V1ModifyRegistration;
import org.oasis.wsrp.v1.V1OperationFailed;
import org.oasis.wsrp.v1.V1PerformBlockingInteraction;
import org.oasis.wsrp.v1.V1PortletContext;
import org.oasis.wsrp.v1.V1PortletDescriptionResponse;
import org.oasis.wsrp.v1.V1PortletPropertyDescriptionResponse;
import org.oasis.wsrp.v1.V1PortletStateChangeRequired;
import org.oasis.wsrp.v1.V1PropertyList;
import org.oasis.wsrp.v1.V1RegistrationContext;
import org.oasis.wsrp.v1.V1RegistrationData;
import org.oasis.wsrp.v1.V1RegistrationState;
import org.oasis.wsrp.v1.V1ReleaseSessions;
import org.oasis.wsrp.v1.V1ReturnAny;
import org.oasis.wsrp.v1.V1ServiceDescription;
import org.oasis.wsrp.v1.V1SetPortletProperties;
import org.oasis.wsrp.v1.V1UnsupportedLocale;
import org.oasis.wsrp.v1.V1UnsupportedMimeType;
import org.oasis.wsrp.v1.V1UnsupportedMode;
import org.oasis.wsrp.v1.V1UnsupportedWindowState;
import org.oasis.wsrp.v2.AccessDenied;
import org.oasis.wsrp.v2.BlockingInteractionResponse;
import org.oasis.wsrp.v2.DestroyPortletsResponse;
import org.oasis.wsrp.v2.InconsistentParameters;
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
import org.oasis.wsrp.v2.PortletContext;
import org.oasis.wsrp.v2.PortletDescription;
import org.oasis.wsrp.v2.PortletDescriptionResponse;
import org.oasis.wsrp.v2.PortletPropertyDescriptionResponse;
import org.oasis.wsrp.v2.PortletStateChangeRequired;
import org.oasis.wsrp.v2.PropertyList;
import org.oasis.wsrp.v2.RegistrationContext;
import org.oasis.wsrp.v2.RegistrationData;
import org.oasis.wsrp.v2.RegistrationState;
import org.oasis.wsrp.v2.ResourceSuspended;
import org.oasis.wsrp.v2.ServiceDescription;
import org.oasis.wsrp.v2.UnsupportedLocale;
import org.oasis.wsrp.v2.UnsupportedMimeType;
import org.oasis.wsrp.v2.UnsupportedMode;
import org.oasis.wsrp.v2.UnsupportedWindowState;

import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class WSRP1Producer implements WSRPProducer, V1MarkupInterface, V1PortletManagementInterface,
   V1RegistrationInterface, V1ServiceDescriptionInterface, ProducerHelper
{
   public Portlet getPortletWith(org.gatein.pc.api.PortletContext portletContext, Registration registration) throws InvalidHandle, PortletInvokerException
   {
      return ((ProducerHelper)producer).getPortletWith(portletContext, registration);
   }

   public PortletDescription getPortletDescription(PortletContext portletContext, List<String> locales, Registration registration) throws InvalidHandle, OperationFailed
   {
      return ((ProducerHelper)producer).getPortletDescription(portletContext, locales, registration);
   }

   public Registration getRegistrationOrFailIfInvalid(RegistrationContext registrationContext) throws InvalidRegistration, OperationFailed, ModifyRegistrationRequired
   {
      return ((ProducerHelper)producer).getRegistrationOrFailIfInvalid(registrationContext);
   }

   public void reset()
   {
      ((ProducerHelper)producer).reset();
   }

   // On-demand class holder Singleton pattern (multi-thread safe)

   private static final class InstanceHolder
   {
      public static final WSRP1Producer producer = new WSRP1Producer();
   }

   public static WSRP1Producer getInstance()
   {
      return InstanceHolder.producer;
   }

   private WSRP1Producer()
   {
   }

   private final WSRP2Producer producer = ProducerHolder.getProducer(true);

   public RegistrationManager getRegistrationManager()
   {
      return producer.getRegistrationManager();
   }

   public void setRegistrationManager(RegistrationManager registrationManager)
   {
      producer.setRegistrationManager(registrationManager);
   }

   public ProducerConfigurationService getConfigurationService()
   {
      return producer.getConfigurationService();
   }

   public void setConfigurationService(ProducerConfigurationService configurationService)
   {
      producer.setConfigurationService(configurationService);
   }

   public PortletInvoker getPortletInvoker()
   {
      return producer.getPortletInvoker();
   }

   public void setPortletInvoker(PortletInvoker invoker)
   {
      producer.setPortletInvoker(invoker);
   }

   public void start()
   {
      producer.start();
   }

   public void stop()
   {
      producer.stop();
   }

   public void usingStrictModeChangedTo(boolean strictMode)
   {
      producer.usingStrictModeChangedTo(strictMode);
   }

   public ExportManager getExportManager()
   {
      return producer.getExportManager();
   }

   public void setExportManager(ExportManager exportManager)
   {
      producer.setExportManager(exportManager);
   }

   public ProducerContext getProducerContext()
   {
      return producer.getProducerContext();
   }

   public void setProducerContext(ProducerContext producerContext)
   {
      producer.setProducerContext(producerContext);
   }

   public V1ServiceDescription getServiceDescription(V1GetServiceDescription gs) throws V1InvalidRegistration, V1OperationFailed
   {
      try
      {
         ServiceDescription description = producer.getServiceDescription(V1ToV2Converter.toV2GetServiceDescription(gs));
         return V2ToV1Converter.toV1ServiceDescription(description);
      }
      catch (InvalidRegistration invalidRegistration)
      {
         throw V2ToV1Converter.toV1Exception(V1InvalidRegistration.class, invalidRegistration);
      }
      catch (OperationFailed operationFailed)
      {
         throw V2ToV1Converter.toV1Exception(V1OperationFailed.class, operationFailed);
      }
      catch (ModifyRegistrationRequired modifyRegistrationRequired)
      {
         throw WSRP1ExceptionFactory.createWSException(V1OperationFailed.class, "Need to call modifyRegistration", modifyRegistrationRequired);
      }
      catch (ResourceSuspended resourceSuspended)
      {
         throw WSRP1ExceptionFactory.createWSException(V1OperationFailed.class, "Resource suspended", resourceSuspended);
      }

   }

   public V1RegistrationContext register(V1RegistrationData register) throws V1MissingParameters, V1OperationFailed
   {
      try
      {
         final RegistrationData registrationData = V1ToV2Converter.toV2RegistrationData(register);
         RegistrationContext registrationContext = producer.register(WSRPTypeFactory.createRegister(registrationData, null, null));
         return V2ToV1Converter.toV1RegistrationContext(registrationContext);
      }
      catch (MissingParameters missingParameters)
      {
         throw V2ToV1Converter.toV1Exception(V1MissingParameters.class, missingParameters);
      }
      catch (OperationFailed operationFailed)
      {
         throw V2ToV1Converter.toV1Exception(V1OperationFailed.class, operationFailed);
      }
      catch (OperationNotSupported operationNotSupported)
      {
         throw WSRP1ExceptionFactory.createWSException(V1OperationFailed.class, "Not supported", operationNotSupported);
      }
   }

   public V1ReturnAny deregister(V1RegistrationContext deregister) throws V1OperationFailed, V1InvalidRegistration
   {
      try
      {
         final RegistrationContext registrationContext = V1ToV2Converter.toV2RegistrationContext(deregister);
         producer.deregister(WSRPTypeFactory.createDeregister(registrationContext, null));
         return null;
      }
      catch (InvalidRegistration invalidRegistration)
      {
         throw V2ToV1Converter.toV1Exception(V1InvalidRegistration.class, invalidRegistration);
      }
      catch (OperationFailed operationFailed)
      {
         throw V2ToV1Converter.toV1Exception(V1OperationFailed.class, operationFailed);
      }
      catch (ResourceSuspended resourceSuspended)
      {
         throw WSRP1ExceptionFactory.createWSException(V1OperationFailed.class, "Resource suspended", resourceSuspended);
      }
      catch (OperationNotSupported operationNotSupported)
      {
         throw WSRP1ExceptionFactory.createWSException(V1OperationFailed.class, "Not supported", operationNotSupported);
      }
      catch (Exception e)
      {
         throw WSRP1ExceptionFactory.createWSException(V1OperationFailed.class, "Operation failed", e);
      }
   }

   public V1RegistrationState modifyRegistration(V1ModifyRegistration modifyRegistration) throws V1MissingParameters, V1OperationFailed, V1InvalidRegistration
   {
      try
      {
         RegistrationState registrationState = producer.modifyRegistration(V1ToV2Converter.toV2ModifyRegistration(modifyRegistration));
         return V2ToV1Converter.toV1RegistrationState(registrationState);
      }
      catch (MissingParameters missingParameters)
      {
         throw V2ToV1Converter.toV1Exception(V1MissingParameters.class, missingParameters);
      }
      catch (OperationFailed operationFailed)
      {
         throw V2ToV1Converter.toV1Exception(V1OperationFailed.class, operationFailed);
      }
      catch (InvalidRegistration invalidRegistration)
      {
         throw V2ToV1Converter.toV1Exception(V1MissingParameters.class, invalidRegistration);
      }
      catch (ResourceSuspended resourceSuspended)
      {
         throw WSRP1ExceptionFactory.createWSException(V1OperationFailed.class, "Resource suspended", resourceSuspended);
      }
      catch (OperationNotSupported operationNotSupported)
      {
         throw WSRP1ExceptionFactory.createWSException(V1OperationFailed.class, "Not supported", operationNotSupported);
      }

   }

   public V1PortletDescriptionResponse getPortletDescription(V1GetPortletDescription getPortletDescription) throws V1AccessDenied, V1InvalidHandle, V1InvalidUserCategory, V1InconsistentParameters, V1MissingParameters, V1InvalidRegistration, V1OperationFailed
   {
      try
      {
         PortletDescriptionResponse portletDescriptionResponse = producer.getPortletDescription(V1ToV2Converter.toV2GetPortletDescription(getPortletDescription));
         return V2ToV1Converter.toV1PortletDescriptionResponse(portletDescriptionResponse);
      }
      catch (AccessDenied accessDenied)
      {
         throw V2ToV1Converter.toV1Exception(V1AccessDenied.class, accessDenied);
      }
      catch (InvalidHandle invalidHandle)
      {
         throw V2ToV1Converter.toV1Exception(V1InvalidHandle.class, invalidHandle);
      }
      catch (InvalidUserCategory invalidUserCategory)
      {
         throw V2ToV1Converter.toV1Exception(V1InvalidUserCategory.class, invalidUserCategory);
      }
      catch (InconsistentParameters inconsistentParameters)
      {
         throw V2ToV1Converter.toV1Exception(V1InconsistentParameters.class, inconsistentParameters);
      }
      catch (MissingParameters missingParameters)
      {
         throw V2ToV1Converter.toV1Exception(V1MissingParameters.class, missingParameters);
      }
      catch (InvalidRegistration invalidRegistration)
      {
         throw V2ToV1Converter.toV1Exception(V1InvalidRegistration.class, invalidRegistration);
      }
      catch (OperationFailed operationFailed)
      {
         throw V2ToV1Converter.toV1Exception(V1OperationFailed.class, operationFailed);
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

   }

   public V1MarkupResponse getMarkup(V1GetMarkup getMarkup) throws V1UnsupportedWindowState, V1InvalidCookie, V1InvalidSession, V1AccessDenied, V1InconsistentParameters, V1InvalidHandle, V1UnsupportedLocale, V1UnsupportedMode, V1OperationFailed, V1MissingParameters, V1InvalidUserCategory, V1InvalidRegistration, V1UnsupportedMimeType
   {
      try
      {
         MarkupResponse markupResponse = producer.getMarkup(V1ToV2Converter.toV2GetMarkup(getMarkup));
         return V2ToV1Converter.toV1MarkupResponse(markupResponse);
      }
      catch (UnsupportedMimeType unsupportedMimeType)
      {
         throw V2ToV1Converter.toV1Exception(V1UnsupportedMimeType.class, unsupportedMimeType);
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
      catch (ModifyRegistrationRequired modifyRegistrationRequired)
      {
         throw WSRP1ExceptionFactory.createWSException(V1OperationFailed.class, "Need to call modifyRegistration", modifyRegistrationRequired);
      }
      catch (ResourceSuspended resourceSuspended)
      {
         throw WSRP1ExceptionFactory.createWSException(V1OperationFailed.class, "Resource suspended", resourceSuspended);
      }
   }

   public V1PortletContext clonePortlet(V1ClonePortlet clonePortlet) throws V1InvalidUserCategory, V1AccessDenied, V1OperationFailed, V1InvalidHandle, V1InvalidRegistration, V1InconsistentParameters, V1MissingParameters
   {
      try
      {
         PortletContext portletContext = producer.clonePortlet(V1ToV2Converter.toV2ClonePortlet(clonePortlet));
         return V2ToV1Converter.toV1PortletContext(portletContext);
      }
      catch (AccessDenied accessDenied)
      {
         throw V2ToV1Converter.toV1Exception(V1AccessDenied.class, accessDenied);
      }
      catch (InvalidUserCategory invalidUserCategory)
      {
         throw V2ToV1Converter.toV1Exception(V1InvalidUserCategory.class, invalidUserCategory);
      }
      catch (OperationFailed operationFailed)
      {
         throw V2ToV1Converter.toV1Exception(V1OperationFailed.class, operationFailed);
      }
      catch (InvalidHandle invalidHandle)
      {
         throw V2ToV1Converter.toV1Exception(V1InvalidHandle.class, invalidHandle);
      }
      catch (InvalidRegistration invalidRegistration)
      {
         throw V2ToV1Converter.toV1Exception(V1InvalidRegistration.class, invalidRegistration);
      }
      catch (InconsistentParameters inconsistentParameters)
      {
         throw V2ToV1Converter.toV1Exception(V1InconsistentParameters.class, inconsistentParameters);
      }
      catch (MissingParameters missingParameters)
      {
         throw V2ToV1Converter.toV1Exception(V1MissingParameters.class, missingParameters);
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
   }

   public V1BlockingInteractionResponse performBlockingInteraction(V1PerformBlockingInteraction performBlockingInteraction) throws V1InvalidSession, V1UnsupportedMode, V1UnsupportedMimeType, V1OperationFailed, V1UnsupportedWindowState, V1UnsupportedLocale, V1AccessDenied, V1PortletStateChangeRequired, V1InvalidRegistration, V1MissingParameters, V1InvalidUserCategory, V1InconsistentParameters, V1InvalidHandle, V1InvalidCookie
   {
      try
      {
         BlockingInteractionResponse blockingInteractionResponse = producer.performBlockingInteraction(V1ToV2Converter.toV2PerformBlockingInteraction(performBlockingInteraction));
         return V2ToV1Converter.toV1BlockingInteractionResponse(blockingInteractionResponse);
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
      catch (InvalidHandle invalidHandle)
      {
         throw V2ToV1Converter.toV1Exception(V1InvalidHandle.class, invalidHandle);
      }
      catch (InvalidCookie invalidCookie)
      {
         throw V2ToV1Converter.toV1Exception(V1InvalidCookie.class, invalidCookie);
      }
      catch (ModifyRegistrationRequired modifyRegistrationRequired)
      {
         throw WSRP1ExceptionFactory.createWSException(V1OperationFailed.class, "Need to call modifyRegistration", modifyRegistrationRequired);
      }
      catch (ResourceSuspended resourceSuspended)
      {
         throw WSRP1ExceptionFactory.createWSException(V1OperationFailed.class, "Resource suspended", resourceSuspended);
      }
   }

   public V1DestroyPortletsResponse destroyPortlets(V1DestroyPortlets destroyPortlets) throws V1InconsistentParameters, V1MissingParameters, V1InvalidRegistration, V1OperationFailed
   {
      try
      {
         DestroyPortletsResponse destroyportletResponse = producer.destroyPortlets(V1ToV2Converter.toV2DestroyPortlets(destroyPortlets));
         return V2ToV1Converter.toV1DestroyPortlesResponse(destroyportletResponse);
      }
      catch (InconsistentParameters inconsistentParameters)
      {
         throw V2ToV1Converter.toV1Exception(V1InconsistentParameters.class, inconsistentParameters);
      }
      catch (MissingParameters missingParameters)
      {
         throw V2ToV1Converter.toV1Exception(V1MissingParameters.class, missingParameters);
      }
      catch (InvalidRegistration invalidRegistration)
      {
         throw V2ToV1Converter.toV1Exception(V1InvalidRegistration.class, invalidRegistration);
      }
      catch (OperationFailed operationFailed)
      {
         throw V2ToV1Converter.toV1Exception(V1OperationFailed.class, operationFailed);
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
   }

   public V1PortletContext setPortletProperties(V1SetPortletProperties setPortletProperties) throws V1OperationFailed, V1InvalidHandle, V1MissingParameters, V1InconsistentParameters, V1InvalidUserCategory, V1AccessDenied, V1InvalidRegistration
   {
      try
      {
         PortletContext portletContext = producer.setPortletProperties(V1ToV2Converter.toV2SetPortletProperties(setPortletProperties));
         return V2ToV1Converter.toV1PortletContext(portletContext);
      }
      catch (OperationFailed operationFailed)
      {
         throw V2ToV1Converter.toV1Exception(V1OperationFailed.class, operationFailed);
      }
      catch (InvalidHandle invalidHandle)
      {
         throw V2ToV1Converter.toV1Exception(V1InvalidHandle.class, invalidHandle);
      }
      catch (MissingParameters missingParameters)
      {
         throw V2ToV1Converter.toV1Exception(V1MissingParameters.class, missingParameters);
      }
      catch (InconsistentParameters inconsistentParameters)
      {
         throw V2ToV1Converter.toV1Exception(V1InconsistentParameters.class, inconsistentParameters);
      }
      catch (InvalidUserCategory invalidUserCategory)
      {
         throw V2ToV1Converter.toV1Exception(V1InvalidUserCategory.class, invalidUserCategory);
      }
      catch (AccessDenied accessDenied)
      {
         throw V2ToV1Converter.toV1Exception(V1AccessDenied.class, accessDenied);
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
   }

   public V1ReturnAny releaseSessions(V1ReleaseSessions releaseSessions) throws V1InvalidRegistration, V1OperationFailed, V1MissingParameters, V1AccessDenied
   {
      try
      {
         producer.releaseSessions(V1ToV2Converter.toV2ReleaseSessions(releaseSessions));
         return null;
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
   }

   public V1PropertyList getPortletProperties(V1GetPortletProperties getPortletProperties) throws V1InvalidHandle, V1MissingParameters, V1InvalidRegistration, V1AccessDenied, V1OperationFailed, V1InconsistentParameters, V1InvalidUserCategory
   {
      try
      {
         PropertyList propertyList = producer.getPortletProperties(V1ToV2Converter.toV2GetPortletProperties(getPortletProperties));
         return V2ToV1Converter.toV1PropertyList(propertyList);
      }
      catch (InvalidHandle invalidHandle)
      {
         throw V2ToV1Converter.toV1Exception(V1InvalidHandle.class, invalidHandle);
      }
      catch (MissingParameters missingParameters)
      {
         throw V2ToV1Converter.toV1Exception(V1MissingParameters.class, missingParameters);
      }
      catch (InvalidRegistration invalidRegistration)
      {
         throw V2ToV1Converter.toV1Exception(V1InvalidRegistration.class, invalidRegistration);
      }
      catch (AccessDenied accessDenied)
      {
         throw V2ToV1Converter.toV1Exception(V1AccessDenied.class, accessDenied);
      }
      catch (OperationFailed operationFailed)
      {
         throw V2ToV1Converter.toV1Exception(V1OperationFailed.class, operationFailed);
      }
      catch (InconsistentParameters inconsistentParameters)
      {
         throw V2ToV1Converter.toV1Exception(V1InconsistentParameters.class, inconsistentParameters);
      }
      catch (InvalidUserCategory invalidUserCategory)
      {
         throw V2ToV1Converter.toV1Exception(V1InvalidUserCategory.class, invalidUserCategory);
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
   }

   public V1ReturnAny initCookie(V1InitCookie initCookie) throws V1AccessDenied, V1OperationFailed, V1InvalidRegistration
   {
      try
      {
         producer.initCookie(V1ToV2Converter.toV2InitCookie(initCookie));
         return null;
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
   }

   public V1PortletPropertyDescriptionResponse getPortletPropertyDescription(V1GetPortletPropertyDescription getPortletPropertyDescription) throws V1MissingParameters, V1InconsistentParameters, V1InvalidUserCategory, V1InvalidRegistration, V1AccessDenied, V1InvalidHandle, V1OperationFailed
   {
      try
      {
         PortletPropertyDescriptionResponse portletPropertyDescriptionResponse = producer.getPortletPropertyDescription(V1ToV2Converter.toV2GetPortletPropertyDescription(getPortletPropertyDescription));
         return V2ToV1Converter.toV1PortletPropertyDescriptionResponse(portletPropertyDescriptionResponse);
      }
      catch (MissingParameters missingParameters)
      {
         throw V2ToV1Converter.toV1Exception(V1MissingParameters.class, missingParameters);
      }
      catch (InconsistentParameters inconsistentParameters)
      {
         throw V2ToV1Converter.toV1Exception(V1InconsistentParameters.class, inconsistentParameters);
      }
      catch (InvalidUserCategory invalidUserCategory)
      {
         throw V2ToV1Converter.toV1Exception(V1InvalidUserCategory.class, invalidUserCategory);
      }
      catch (InvalidRegistration invalidRegistration)
      {
         throw V2ToV1Converter.toV1Exception(V1InvalidRegistration.class, invalidRegistration);
      }
      catch (AccessDenied accessDenied)
      {
         throw V2ToV1Converter.toV1Exception(V1AccessDenied.class, accessDenied);
      }
      catch (InvalidHandle invalidHandle)
      {
         throw V2ToV1Converter.toV1Exception(V1InvalidHandle.class, invalidHandle);
      }
      catch (OperationFailed operationFailed)
      {
         throw V2ToV1Converter.toV1Exception(V1OperationFailed.class, operationFailed);
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
   }

   public void onEvent(ManagedObjectRegistryEvent event)
   {
      producer.onEvent(event);
   }
}
