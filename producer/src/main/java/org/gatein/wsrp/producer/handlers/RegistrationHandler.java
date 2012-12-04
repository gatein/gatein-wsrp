/*
 * JBoss, a division of Red Hat
 * Copyright 2011, Red Hat Middleware, LLC, and individual
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

package org.gatein.wsrp.producer.handlers;

import org.gatein.common.util.ParameterValidation;
import org.gatein.pc.api.Mode;
import org.gatein.pc.api.WindowState;
import org.gatein.registration.Consumer;
import org.gatein.registration.ConsumerCapabilities;
import org.gatein.registration.NoSuchRegistrationException;
import org.gatein.registration.Registration;
import org.gatein.registration.RegistrationException;
import org.gatein.registration.RegistrationStatus;
import org.gatein.registration.RegistrationUtils;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.WSRPUtils;
import org.gatein.wsrp.producer.RegistrationInterface;
import org.gatein.wsrp.producer.WSRPProducerImpl;
import org.gatein.wsrp.producer.config.ProducerRegistrationRequirements;
import org.gatein.wsrp.spec.v2.WSRP2ExceptionFactory;
import org.oasis.wsrp.v2.AccessDenied;
import org.oasis.wsrp.v2.Deregister;
import org.oasis.wsrp.v2.Extension;
import org.oasis.wsrp.v2.GetRegistrationLifetime;
import org.oasis.wsrp.v2.InvalidHandle;
import org.oasis.wsrp.v2.InvalidRegistration;
import org.oasis.wsrp.v2.Lifetime;
import org.oasis.wsrp.v2.MissingParameters;
import org.oasis.wsrp.v2.ModifyRegistration;
import org.oasis.wsrp.v2.ModifyRegistrationRequired;
import org.oasis.wsrp.v2.OperationFailed;
import org.oasis.wsrp.v2.OperationNotSupported;
import org.oasis.wsrp.v2.Property;
import org.oasis.wsrp.v2.Register;
import org.oasis.wsrp.v2.RegistrationContext;
import org.oasis.wsrp.v2.RegistrationData;
import org.oasis.wsrp.v2.RegistrationState;
import org.oasis.wsrp.v2.ResourceSuspended;
import org.oasis.wsrp.v2.SetRegistrationLifetime;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 13202 $
 * @since 2.4
 */
public class RegistrationHandler extends ServiceHandler implements RegistrationInterface
{
   public RegistrationHandler(WSRPProducerImpl producer)
   {
      super(producer);
   }

   public RegistrationContext register(Register register) throws MissingParameters, OperationFailed, OperationNotSupported
   {
      WSRP2ExceptionFactory.throwOperationFailedIfValueIsMissing(register, "Register");

      RegistrationData registrationData = register.getRegistrationData();
      WSRP2ExceptionFactory.throwMissingParametersIfValueIsMissing(registrationData, "RegistrationData", "Register");

      ProducerRegistrationRequirements registrationRequirements = producer.getProducerRegistrationRequirements();

      String consumerName = registrationData.getConsumerName();
      WSRP2ExceptionFactory.throwMissingParametersIfValueIsMissing(consumerName, "consumer name", "RegistrationData");

      String consumerAgent = registrationData.getConsumerAgent();
      WSRP2ExceptionFactory.throwMissingParametersIfValueIsMissing(consumerAgent, "consumer agent", "RegistrationData");

      Registration registration;
      try
      {
         log.debug("Attempting to register consumer named '" + consumerName + "', agent '" + consumerAgent + "'.");

         // check that the consumer agent is valid before trying to register
         RegistrationUtils.validateConsumerAgent(consumerAgent);

         registration = producer.getRegistrationManager().addRegistrationTo(consumerName, createRegistrationProperties(registrationData), registrationRequirements.getRegistrationProperties(), true);
         updateRegistrationInformation(registration, registrationData);
      }
      catch (Exception e)
      {
         String msg = "Could not register consumer named '" + consumerName + "'";
         log.debug(msg, e);
         throw WSRP2ExceptionFactory.throwWSException(OperationFailed.class, msg, e);
      }

      RegistrationContext registrationContext = WSRPTypeFactory.createRegistrationContext(registration.getRegistrationHandle());
      log.debug("Registration completed without error.");
      return registrationContext;
   }

   private void updateRegistrationInformation(Registration registration, RegistrationData registrationData) throws RegistrationException
   {
      registration.setStatus(RegistrationStatus.VALID);
      Consumer consumer = registration.getConsumer();
      consumer.setConsumerAgent(registrationData.getConsumerAgent());
      ConsumerCapabilities capabilities = consumer.getCapabilities();

      List<String> modeStrings = registrationData.getConsumerModes();
      modeStrings = WSRPUtils.replaceByEmptyListIfNeeded(modeStrings);
      int modesNb = modeStrings.size();
      if (modesNb > 0)
      {
         List<Mode> modes = new ArrayList<Mode>(modesNb);
         for (String modeString : modeStrings)
         {
            modes.add(WSRPUtils.getJSR168PortletModeFromWSRPName(modeString));
         }
         capabilities.setSupportedModes(modes);
      }

      List<String> wsStrings = registrationData.getConsumerWindowStates();
      wsStrings = WSRPUtils.replaceByEmptyListIfNeeded(wsStrings);
      int wsNb = wsStrings.size();
      if (wsNb > 0)
      {
         List<WindowState> windowStates = new ArrayList<WindowState>(wsNb);
         for (String wsString : wsStrings)
         {
            windowStates.add(WSRPUtils.getJSR168WindowStateFromWSRPName(wsString));
         }
         capabilities.setSupportedWindowStates(windowStates);
      }

      capabilities.setSupportedUserScopes(registrationData.getConsumerUserScopes());
      capabilities.setSupportsGetMethod(registrationData.isMethodGetSupported());

      producer.getRegistrationManager().getPersistenceManager().saveChangesTo(consumer);
   }

   public List<Extension> deregister(Deregister deregister)
      throws InvalidRegistration, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      WSRP2ExceptionFactory.throwOperationFailedIfValueIsMissing(deregister, "Deregister");

      if (producer.getProducerRegistrationRequirements().isRegistrationRequired())
      {

         final RegistrationContext registrationContext = deregister.getRegistrationContext();
         WSRP2ExceptionFactory.throwOperationFailedIfValueIsMissing(registrationContext, "RegistrationContext");

         String registrationHandle = registrationContext.getRegistrationHandle();
         if (ParameterValidation.isNullOrEmpty(registrationHandle))
         {
            throwInvalidRegistrationFault("Null or empty registration handle");
         }

         log.debug("Attempting to deregister registration with handle '" + registrationHandle + "'");

         String msg = "Could not deregister registration with handle '" + registrationHandle + "'";
         try
         {
            producer.getRegistrationManager().removeRegistration(registrationHandle);
         }
         catch (NoSuchRegistrationException e)
         {
            log.debug(msg, e);
            throwInvalidRegistrationFault(e.getLocalizedMessage());
         }
         catch (RegistrationException e)
         {
            log.debug(msg, e);
            throw WSRP2ExceptionFactory.throwWSException(OperationFailed.class, msg, e);
         }

         return Collections.emptyList();
      }

      throw WSRP2ExceptionFactory.throwWSException(OperationFailed.class, "Deregistration shouldn't be attempted if registration is not required", null);
   }

   public RegistrationState modifyRegistration(ModifyRegistration modifyRegistration)
      throws InvalidRegistration, MissingParameters, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      if (producer.getProducerRegistrationRequirements().isRegistrationRequired())
      {
         WSRP2ExceptionFactory.throwOperationFailedIfValueIsMissing(modifyRegistration, "ModifyRegistration");

         RegistrationContext registrationContext = modifyRegistration.getRegistrationContext();
         WSRP2ExceptionFactory.throwMissingParametersIfValueIsMissing(registrationContext, "RegistrationContext", "ModifyRegistration");
         String registrationHandle = registrationContext.getRegistrationHandle();
         if (ParameterValidation.isNullOrEmpty(registrationHandle))
         {
            throwInvalidRegistrationFault("Null or empty registration handle");
         }

         RegistrationData registrationData = modifyRegistration.getRegistrationData();
         WSRP2ExceptionFactory.throwMissingParametersIfValueIsMissing(registrationData, "RegistrationData", "ModifyRegistration");

         String consumerName = registrationData.getConsumerName();
         WSRP2ExceptionFactory.throwMissingParametersIfValueIsMissing(consumerName, "consumer name", "RegistrationData");

         String consumerAgent = registrationData.getConsumerAgent();
         WSRP2ExceptionFactory.throwMissingParametersIfValueIsMissing(consumerAgent, "consumer agent", "RegistrationData");

         log.debug("Attempting to modify registration with handle '" + registrationHandle + "'");
         String msg = "Could not modify registration with handle '" + registrationHandle + "'";
         try
         {
            Registration registration = producer.getRegistrationManager().getRegistration(registrationHandle);

            Map<QName, Object> properties = createRegistrationProperties(registrationData);

            // check that the given registration properties are acceptable according to expectations and policy
            ProducerRegistrationRequirements req = producer.getProducerRegistrationRequirements();
            req.getPolicy().validateRegistrationDataFor(properties, consumerName, req.getRegistrationProperties(), producer.getRegistrationManager());

            registration.updateProperties(properties);
            updateRegistrationInformation(registration, registrationData);
         }
         catch (NoSuchRegistrationException e)
         {
            log.debug(msg, e);
            throwInvalidRegistrationFault(e.getLocalizedMessage());
         }
         catch (RegistrationException e)
         {
            log.debug(msg, e);
            throw WSRP2ExceptionFactory.throwWSException(OperationFailed.class, msg, e);
         }


         log.debug("Modified registration with handle '" + registrationHandle + "'");
         return null;
      }

      throw WSRP2ExceptionFactory.throwWSException(OperationFailed.class, "Modifying a registration shouldn't be attempted if registration is not required", null);
   }

   public Lifetime getRegistrationLifetime(GetRegistrationLifetime getRegistrationLifetime)
      throws AccessDenied, InvalidHandle, InvalidRegistration, ModifyRegistrationRequired, OperationFailed,
      OperationNotSupported, ResourceSuspended
   {
      throw WSRP2ExceptionFactory.throwWSException(OperationNotSupported.class, "Lifetime operations are not currently supported.", null);
   }

   public Lifetime setRegistrationLifetime(SetRegistrationLifetime setRegistrationLifetime)
      throws AccessDenied, InvalidHandle, InvalidRegistration, ModifyRegistrationRequired, OperationFailed,
      OperationNotSupported, ResourceSuspended
   {
      throw WSRP2ExceptionFactory.throwWSException(OperationNotSupported.class, "Lifetime operations are not currently supported.", null);
   }

   /**
    * @param reg
    * @param throwExceptionIfInvalid
    * @return
    * @since 2.6.2
    */
   public boolean isRegistrationValid(Registration reg, boolean throwExceptionIfInvalid) throws InvalidRegistration, OperationFailed, ModifyRegistrationRequired
   {
      final boolean registrationRequired = producer.getProducerRegistrationRequirements().isRegistrationRequired();
      if (reg == null)
      {
         if (registrationRequired)
         {
            log.debug("Registration is required yet no RegistrationContext was provided: registration invalid!");
            if (throwExceptionIfInvalid)
            {
               throwInvalidRegistrationFault("registration is required yet no RegistrationContext was provided!");
            }
            return false;
         }

         log.debug("Registration not required, no registration: registration valid!");
         return true;
      }
      else
      {
         boolean isValid = RegistrationStatus.VALID.equals(reg.getStatus());
         if (registrationRequired)
         {
            boolean isPending = RegistrationStatus.PENDING.equals(reg.getStatus());
            log.debug("Registration required: registration is " + (isValid ? "valid!" : (isPending ? "pending!" : "invalid!")));

            if (throwExceptionIfInvalid)
            {
               if (isPending)
               {
                  WSRP2ExceptionFactory.throwWSException(ModifyRegistrationRequired.class, "Registration with handle '" + reg.getRegistrationHandle()
                     + "' is pending. Consumer needs to call modifyRegistration().", null);
               }
               else
               {
                  if (!isValid)
                  {
                     throwInvalidRegistrationFault("registration with handle '" + reg.getRegistrationHandle() + "' is not valid!");
                  }
               }
            }

            return isValid;
         }
         else
         {
            if (!isValid && throwExceptionIfInvalid)
            {
               throwInvalidRegistrationFault("registration information was provided but registration is not required!");
            }

            return isValid;
         }
      }
   }

   /**
    * @param registrationContext
    * @return
    * @since 2.6.2
    */
   public Registration getRegistrationFrom(RegistrationContext registrationContext) throws InvalidRegistration, OperationFailed
   {
      if (producer.getProducerRegistrationRequirements().isRegistrationRequired())
      {
         if (registrationContext == null)
         {
            throwInvalidRegistrationFault("registration context is missing but registration is required");
         }
      }

      if (registrationContext != null)
      {
         String regHandle = registrationContext.getRegistrationHandle();
         if (regHandle == null)
         {
            throwInvalidRegistrationFault("registration handle is missing but registration is required");
         }

         try
         {
            Registration registration = producer.getRegistrationManager().getRegistration(regHandle);
            if (registration == null)
            {
               throwInvalidRegistrationFault("provided registration handle '" + regHandle + "' is not registered with this producer");
            }
            return registration;
         }
         catch (RegistrationException e)
         {
            throwOperationFailedFault("Failed to retrieve registration information associated with handle " + regHandle, e);
            return null;
         }
      }
      else
      {
         try
         {
            Registration registration = producer.getRegistrationManager().getNonRegisteredRegistration();
            if (registration == null)
            {
               throwInvalidRegistrationFault("Could not acquire the nonregistered registration from the RegistrationManager");
            }
            return registration;
         }
         catch (RegistrationException e)
         {
            throwOperationFailedFault("Failed to retrieve registration information associated with the nonregistered consumer", e);
            return null;
         }
      }
   }

   private void throwOperationFailedFault(String message, RegistrationException e) throws OperationFailed
   {
      throw WSRP2ExceptionFactory.throwWSException(OperationFailed.class, message, e);
   }

   boolean throwInvalidRegistrationFault(String message) throws InvalidRegistration
   {
      throw WSRP2ExceptionFactory.throwWSException(InvalidRegistration.class, "Invalid registration: " + message, null);
   }

   private Map<QName, Object> createRegistrationProperties(RegistrationData registrationData)
   {
      List<Property> regProperties = registrationData.getRegistrationProperties();
      regProperties = WSRPUtils.replaceByEmptyListIfNeeded(regProperties);
      Map<QName, Object> properties;
      if (regProperties != null && !regProperties.isEmpty())
      {
         properties = new HashMap<QName, Object>(regProperties.size());
         for (Property property : regProperties)
         {
            // todo: should be more detailed here... use the language, allow other value types...
            QName propName = property.getName();
            String propValue = property.getStringValue();
            if (producer.getProducerRegistrationRequirements().acceptValueFor(propName, propValue))
            {
               properties.put(propName, propValue);
            }
            else
            {
               throw new IllegalArgumentException("Registration properties named '" + propName + "' with value '"
                  + propValue + "' was rejected by the WSRP producer.");
            }
         }
      }
      else
      {
         properties = Collections.emptyMap();
      }

      return properties;
   }

   private List getListFromArray(String[] array, boolean useEmptyForNull)
   {
      if (array == null)
      {
         return useEmptyForNull ? Collections.EMPTY_LIST : null;
      }
      return Arrays.asList(array);
   }
}
