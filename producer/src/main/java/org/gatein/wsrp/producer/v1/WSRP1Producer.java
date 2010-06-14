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

import org.gatein.common.NotYetImplemented;
import org.gatein.pc.api.PortletInvoker;
import org.gatein.registration.RegistrationManager;
import org.gatein.wsrp.producer.ProducerHolder;
import org.gatein.wsrp.producer.WSRPProducer;
import org.gatein.wsrp.producer.config.ProducerConfigurationService;
import org.gatein.wsrp.producer.v2.WSRP2Producer;
import org.gatein.wsrp.spec.v1.V1ToV2Converter;
import org.gatein.wsrp.spec.v1.V2ToV1Converter;
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
import org.oasis.wsrp.v2.InvalidRegistration;
import org.oasis.wsrp.v2.OperationFailed;
import org.oasis.wsrp.v2.ServiceDescription;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class WSRP1Producer implements WSRPProducer, V1MarkupInterface, V1PortletManagementInterface,
   V1RegistrationInterface, V1ServiceDescriptionInterface
{

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

   private final WSRP2Producer producer = ProducerHolder.getProducer();

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

   }

   public V1RegistrationContext register(V1RegistrationData register) throws V1MissingParameters, V1OperationFailed
   {
      throw new NotYetImplemented();
   }

   public V1ReturnAny deregister(V1RegistrationContext deregister) throws V1OperationFailed, V1InvalidRegistration
   {
      throw new NotYetImplemented();
   }

   public V1RegistrationState modifyRegistration(V1ModifyRegistration modifyRegistration) throws V1MissingParameters, V1OperationFailed, V1InvalidRegistration
   {
      throw new NotYetImplemented();
   }

   public V1PortletDescriptionResponse getPortletDescription(V1GetPortletDescription getPortletDescription) throws V1AccessDenied, V1InvalidHandle, V1InvalidUserCategory, V1InconsistentParameters, V1MissingParameters, V1InvalidRegistration, V1OperationFailed
   {
      throw new NotYetImplemented();
   }

   public V1MarkupResponse getMarkup(V1GetMarkup getMarkup) throws V1UnsupportedWindowState, V1InvalidCookie, V1InvalidSession, V1AccessDenied, V1InconsistentParameters, V1InvalidHandle, V1UnsupportedLocale, V1UnsupportedMode, V1OperationFailed, V1MissingParameters, V1InvalidUserCategory, V1InvalidRegistration, V1UnsupportedMimeType
   {
      throw new NotYetImplemented();
   }

   public V1PortletContext clonePortlet(V1ClonePortlet clonePortlet) throws V1InvalidUserCategory, V1AccessDenied, V1OperationFailed, V1InvalidHandle, V1InvalidRegistration, V1InconsistentParameters, V1MissingParameters
   {
      throw new NotYetImplemented();
   }

   public V1BlockingInteractionResponse performBlockingInteraction(V1PerformBlockingInteraction performBlockingInteraction) throws V1InvalidSession, V1UnsupportedMode, V1UnsupportedMimeType, V1OperationFailed, V1UnsupportedWindowState, V1UnsupportedLocale, V1AccessDenied, V1PortletStateChangeRequired, V1InvalidRegistration, V1MissingParameters, V1InvalidUserCategory, V1InconsistentParameters, V1InvalidHandle, V1InvalidCookie
   {
      throw new NotYetImplemented();
   }

   public V1DestroyPortletsResponse destroyPortlets(V1DestroyPortlets destroyPortlets) throws V1InconsistentParameters, V1MissingParameters, V1InvalidRegistration, V1OperationFailed
   {
      throw new NotYetImplemented();
   }

   public V1PortletContext setPortletProperties(V1SetPortletProperties setPortletProperties) throws V1OperationFailed, V1InvalidHandle, V1MissingParameters, V1InconsistentParameters, V1InvalidUserCategory, V1AccessDenied, V1InvalidRegistration
   {
      throw new NotYetImplemented();
   }

   public V1ReturnAny releaseSessions(V1ReleaseSessions releaseSessions) throws V1InvalidRegistration, V1OperationFailed, V1MissingParameters, V1AccessDenied
   {
      throw new NotYetImplemented();
   }

   public V1PropertyList getPortletProperties(V1GetPortletProperties getPortletProperties) throws V1InvalidHandle, V1MissingParameters, V1InvalidRegistration, V1AccessDenied, V1OperationFailed, V1InconsistentParameters, V1InvalidUserCategory
   {
      throw new NotYetImplemented();
   }

   public V1ReturnAny initCookie(V1InitCookie initCookie) throws V1AccessDenied, V1OperationFailed, V1InvalidRegistration
   {
      throw new NotYetImplemented();
   }

   public V1PortletPropertyDescriptionResponse getPortletPropertyDescription(V1GetPortletPropertyDescription getPortletPropertyDescription) throws V1MissingParameters, V1InconsistentParameters, V1InvalidUserCategory, V1InvalidRegistration, V1AccessDenied, V1InvalidHandle, V1OperationFailed
   {
      throw new NotYetImplemented();
   }
}
