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

package org.gatein.wsrp.services.v1;

import org.gatein.wsrp.WSRPExceptionFactory;
import org.gatein.wsrp.WSRPUtils;
import org.gatein.wsrp.services.RegistrationService;
import org.gatein.wsrp.spec.v1.V1ToV2Converter;
import org.gatein.wsrp.spec.v1.V2ToV1Converter;
import org.oasis.wsrp.v1.V1Extension;
import org.oasis.wsrp.v1.V1InvalidRegistration;
import org.oasis.wsrp.v1.V1MissingParameters;
import org.oasis.wsrp.v1.V1OperationFailed;
import org.oasis.wsrp.v1.WSRPV1RegistrationPortType;
import org.oasis.wsrp.v2.AccessDenied;
import org.oasis.wsrp.v2.Extension;
import org.oasis.wsrp.v2.GetRegistrationLifetime;
import org.oasis.wsrp.v2.InvalidHandle;
import org.oasis.wsrp.v2.InvalidRegistration;
import org.oasis.wsrp.v2.Lifetime;
import org.oasis.wsrp.v2.MissingParameters;
import org.oasis.wsrp.v2.ModifyRegistrationRequired;
import org.oasis.wsrp.v2.OperationFailed;
import org.oasis.wsrp.v2.OperationNotSupported;
import org.oasis.wsrp.v2.RegistrationContext;
import org.oasis.wsrp.v2.RegistrationData;
import org.oasis.wsrp.v2.ResourceSuspended;
import org.oasis.wsrp.v2.SetRegistrationLifetime;
import org.oasis.wsrp.v2.UserContext;

import javax.xml.ws.Holder;
import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class V1RegistrationService extends RegistrationService<WSRPV1RegistrationPortType>
{
   public V1RegistrationService(WSRPV1RegistrationPortType service)
   {
      super(service);
   }

   @Override
   public void register(RegistrationData registrationData, Lifetime lifetime, UserContext userContext, Holder<byte[]> registrationState, Holder<Lifetime> scheduledDestruction, Holder<List<Extension>> extensions, Holder<String> registrationHandle) throws MissingParameters, OperationFailed, OperationNotSupported
   {
      try
      {
         Holder<List<V1Extension>> v1Extensions = new Holder<List<V1Extension>>();

         service.register(
            registrationData.getConsumerName(),
            registrationData.getConsumerAgent(),
            registrationData.isMethodGetSupported(),
            registrationData.getConsumerModes(),
            registrationData.getConsumerWindowStates(),
            registrationData.getConsumerUserScopes(),
            null,
            WSRPUtils.transform(registrationData.getRegistrationProperties(), V2ToV1Converter.PROPERTY),
            new Holder<List<V1Extension>>(),
            registrationHandle,
            registrationState
         );

         extensions.value = WSRPUtils.transform(v1Extensions.value, V1ToV2Converter.EXTENSION);
      }
      catch (V1MissingParameters v1MissingParameters)
      {
         throw V1ToV2Converter.toV2Exception(MissingParameters.class, v1MissingParameters);
      }
      catch (V1OperationFailed v1OperationFailed)
      {
         throw V1ToV2Converter.toV2Exception(OperationFailed.class, v1OperationFailed);
      }
   }

   @Override
   public List<Extension> deregister(RegistrationContext registrationContext, UserContext userContext) throws InvalidRegistration, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      try
      {
         Holder<List<V1Extension>> v1Extensions = new Holder<List<V1Extension>>();

         service.deregister(registrationContext.getRegistrationHandle(),
            registrationContext.getRegistrationState(),
            v1Extensions);

         return WSRPUtils.transform(v1Extensions.value, V1ToV2Converter.EXTENSION);
      }
      catch (V1InvalidRegistration v1InvalidRegistration)
      {
         throw V1ToV2Converter.toV2Exception(InvalidRegistration.class, v1InvalidRegistration);
      }
      catch (V1OperationFailed v1OperationFailed)
      {
         throw V1ToV2Converter.toV2Exception(OperationFailed.class, v1OperationFailed);
      }
   }

   @Override
   public void modifyRegistration(RegistrationContext registrationContext, RegistrationData registrationData, UserContext userContext, Holder<byte[]> registrationState, Holder<Lifetime> scheduledDestruction, Holder<List<Extension>> extensions) throws InvalidRegistration, MissingParameters, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      try
      {
         Holder<List<V1Extension>> v1Extensions = new Holder<List<V1Extension>>();

         service.modifyRegistration(V2ToV1Converter.toV1RegistrationContext(registrationContext),
            V2ToV1Converter.toV1RegistrationData(registrationData),
            registrationState,
            v1Extensions);

         extensions.value = WSRPUtils.transform(v1Extensions.value, V1ToV2Converter.EXTENSION);
      }
      catch (V1InvalidRegistration v1InvalidRegistration)
      {
         throw V1ToV2Converter.toV2Exception(InvalidRegistration.class, v1InvalidRegistration);
      }
      catch (V1MissingParameters v1MissingParameters)
      {
         throw V1ToV2Converter.toV2Exception(MissingParameters.class, v1MissingParameters);
      }
      catch (V1OperationFailed v1OperationFailed)
      {
         throw V1ToV2Converter.toV2Exception(OperationFailed.class, v1OperationFailed);
      }
   }

   @Override
   public Lifetime getRegistrationLifetime(GetRegistrationLifetime getRegistrationLifetime) throws AccessDenied, InvalidHandle, InvalidRegistration, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      throw WSRPExceptionFactory.createWSException(OperationNotSupported.class, "getRegistrationLifetime is not supported in WSRP 1", null);
   }

   @Override
   public Lifetime setRegistrationLifetime(SetRegistrationLifetime setRegistrationLifetime) throws AccessDenied, InvalidHandle, InvalidRegistration, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      throw WSRPExceptionFactory.createWSException(OperationNotSupported.class, "setRegistrationLifetime is not supported in WSRP 1", null);
   }
}
