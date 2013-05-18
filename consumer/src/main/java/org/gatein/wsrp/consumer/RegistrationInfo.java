/*
 * JBoss, a division of Red Hat
 * Copyright 2012, Red Hat Middleware, LLC, and individual
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

package org.gatein.wsrp.consumer;

import org.gatein.common.util.ParameterValidation;
import org.gatein.common.util.Version;
import org.gatein.pc.api.PortletInvokerException;
import org.gatein.wsrp.WSRPConstants;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.WSRPUtils;
import org.gatein.wsrp.registration.RegistrationPropertyDescription;
import org.oasis.wsrp.v2.ModelDescription;
import org.oasis.wsrp.v2.Property;
import org.oasis.wsrp.v2.PropertyDescription;
import org.oasis.wsrp.v2.RegistrationContext;
import org.oasis.wsrp.v2.RegistrationData;
import org.oasis.wsrp.v2.ServiceDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Records the registration status of the consumer owning this RegistrationInfo (via its ProducerInfo) with respect to the metadata provided by the producer, i.e. whether or not
 * registration is required, whether the consumer is currently registered if needed, whether the currently available registration properties are consistent with the producer's
 * expectations, whether the producer has changed its expectation since we last modified our registration properties and so if we need to modify our registration...
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 12686 $
 * @since 2.6
 */
public class RegistrationInfo implements RegistrationProperty.PropertyChangeListener, Serializable
{
   private static final Logger log = LoggerFactory.getLogger(RegistrationInfo.class);

   // persisted variables
   /**  The persistence identifier */
   private Long key;
   /** The name we provide to the producer when we register with it (persisted) */
   private String persistentConsumerName;
   /** The registration handle the producer provides us with once we have registered with it (persisted) */
   private String persistentRegistrationHandle;
   /** The registration state (if any) the producer provides us with once we have registered with it (persisted) */
   private byte[] persistentRegistrationState;
   /** The currently held registration properties that we sent / can send to the producer to register with it (persisted) */
   private Map<QName, RegistrationProperty> persistentRegistrationProperties;

   // transient variables
   /** whether the producer requires registration, null means we haven't yet checked with the producer */
   private transient Boolean requiresRegistration;
   /** whether the local data matches what the producer expects, null means we haven't yet checked with the producer */
   private transient Boolean consistentWithProducerExpectations;
   /** the registration data structured sent to the producer when we register with it, interpolated from the persisted data when needed */
   private transient RegistrationData registrationData;
   /** whether we need to regenerate the registration data */
   private transient boolean regenerateRegistrationData;
   /** whether modifications were made locally since we last check with the producer and refreshed our metadata */
   private transient boolean modifiedSinceLastRefresh;
   /** whether we need to modify our registration with the producer */
   private transient boolean modifyRegistrationNeeded;
   /** the owner of this RegistrationInfo object */
   private transient ProducerInfo parent;

   /**
    * Marker string to identify a RegistrationInfo created for a producer that might not require registration as a work
    * around https://jira.jboss.org/jira/browse/JBPORTAL-2284
    */
   private static final String UNDETERMINED_REGISTRATION = "__JBP__UNDETERMINED__REGISTRATION__";

   public RegistrationInfo(ProducerInfo producerInfo)
   {
      this();
      ParameterValidation.throwIllegalArgExceptionIfNull(producerInfo, "ProducerInfo");
      producerInfo.setRegistrationInfo(this);
      parent = producerInfo;
   }

   static RegistrationInfo createUndeterminedRegistration(ProducerInfo producerInfo)
   {
      return new RegistrationInfo(producerInfo);
   }

   public boolean isUndetermined()
   {
      return UNDETERMINED_REGISTRATION.equals(persistentConsumerName);
   }

   public RegistrationInfo(ProducerInfo producerInfo, boolean requiresRegistration)
   {
      this(producerInfo);
      this.requiresRegistration = requiresRegistration;
   }

   RegistrationInfo()
   {
      persistentConsumerName = UNDETERMINED_REGISTRATION;
   }

   public RegistrationInfo(RegistrationInfo other)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(other, "RegistrationInfo to clone from");
      this.persistentConsumerName = other.persistentConsumerName;
      this.persistentRegistrationHandle = other.persistentRegistrationHandle;
      this.parent = other.parent;

      if (other.persistentRegistrationState != null)
      {
         this.persistentRegistrationState = new byte[other.persistentRegistrationState.length];
         System.arraycopy(other.persistentRegistrationState, 0, this.persistentRegistrationState, 0, other.persistentRegistrationState.length);
      }

      if (other.persistentRegistrationProperties != null)
      {
         this.persistentRegistrationProperties = new HashMap<QName, RegistrationProperty>(other.persistentRegistrationProperties.size());
         for (RegistrationProperty otherProp : other.persistentRegistrationProperties.values())
         {
            QName name = otherProp.getName();
            RegistrationProperty prop = new RegistrationProperty(name, otherProp.getValue(), otherProp.getLang(), this);
            prop.setStatus(otherProp.getStatus());
            this.persistentRegistrationProperties.put(name, prop);
         }
      }
   }

   public Long getKey()
   {
      return key;
   }

   public void setKey(Long key)
   {
      this.key = key;
   }

   public String getRegistrationHandle()
   {
      return persistentRegistrationHandle;
   }

   public void setRegistrationHandle(String registrationHandle)
   {
      this.persistentRegistrationHandle = registrationHandle;
   }

   public byte[] getRegistrationState()
   {
      return persistentRegistrationState;
   }

   public void setRegistrationState(byte[] registrationState)
   {
      this.persistentRegistrationState = registrationState;
   }

   public ProducerInfo getParent()
   {
      return parent;
   }

   public void setParent(ProducerInfo parent)
   {
      this.parent = parent;
   }

   /**
    * Determines whether this RegistrationInfo needs to be refreshed when its {@link #refresh(org.oasis.wsrp.v2.ServiceDescription, String, boolean, boolean, boolean)} method is
    * called.
    */
   public boolean isRefreshNeeded()
   {
      boolean result = requiresRegistration == null || isModifiedSinceLastRefresh();
      if (result)
      {
         log.debug("Refresh needed");
      }
      return result;
   }

   public Boolean isRegistrationValid()
   {
      if (consistentWithProducerExpectations == null || requiresRegistration == null)
      {
         return null;
      }
      return consistentWithProducerExpectations && hasRegisteredIfNeeded();
   }

   private boolean hasRegisteredIfNeeded()
   {
      return (persistentRegistrationHandle != null && isRegistrationDeterminedRequired()) || isRegistrationDeterminedNotRequired();
   }

   public Boolean isConsistentWithProducerExpectations()
   {
      return consistentWithProducerExpectations;
   }

   /**
    * Determines whether the associated Producer requires registration.
    *
    * @return <code>null</code> if this RegistrationInfo hasn't queried the Producer yet and thus, doesn't have a
    *         definitive answer on whether or not the associated Producer requires registration,
    *         <code>Boolean.TRUE</code> if the associated Producer requires registration, <code>Boolean.FALSE</code>
    *         otherwise.
    */
   public Boolean isRegistrationRequired()
   {
      return requiresRegistration;
   }

   /**
    * Determines whether it has been determined after querying the associated Producer that it requires registration.
    *
    * @return <code>true</code> if and only if the associated Producer has been queried and mandates registration,
    *         <code>false</code> otherwise.
    * @throws IllegalStateException if {@link #refresh} has not yet been called
    */
   public boolean isRegistrationDeterminedRequired()
   {
      if (requiresRegistration == null)
      {
         throw new IllegalStateException("Registration status not yet known: call refresh first!");
      }

      return requiresRegistration;
   }

   /**
    * Determines whether it has been determined after querying the associated Producer that it does
    * <strong>NOT</strong>
    * require registration.
    *
    * @return <code>true</code> if and only if the associated Producer has been queried and does NOT mandate
    *         registration, <code>false</code> otherwise.
    * @throws IllegalStateException if {@link #refresh} has not yet been called
    */
   public boolean isRegistrationDeterminedNotRequired()
   {
      if (requiresRegistration == null)
      {
         throw new IllegalStateException("Registration status not yet known: call refresh first!");
      }

      return !requiresRegistration;
   }

   public boolean hasLocalInfo()
   {
      return persistentRegistrationHandle != null || isRegistrationPropertiesExisting();
   }

   public boolean isRegistrationPropertiesExisting()
   {
      return persistentRegistrationProperties != null && !persistentRegistrationProperties.isEmpty();
   }

   /** Retrieves the RegistrationData that can be sent to the WSRP producer associated with the consumer owning this RegistrationInfo to perform registration. */
   public RegistrationData getRegistrationData()
   {
      if (registrationData == null || regenerateRegistrationData)
      {
         registrationData = WSRPTypeFactory.createDefaultRegistrationData();
         registrationData.setConsumerName(persistentConsumerName);
         List<Property> properties = new ArrayList<Property>();
         Map regProps = getRegistrationProperties(false);
         if (!regProps.isEmpty())
         {
            for (Object o : regProps.values())
            {
               RegistrationProperty prop = (RegistrationProperty)o;
               String value = prop.getValue();
               if (value != null && !prop.isDeterminedInvalid())
               {
                  properties.add(WSRPTypeFactory.createProperty(prop.getName(), prop.getLang(), prop.getValue()));
               }
            }

            registrationData.getRegistrationProperties().addAll(properties);
         }

         regenerateRegistrationData = false;
      }

      return registrationData;
   }

   public String getConsumerName()
   {
      return persistentConsumerName;
   }

   public void setConsumerName(String consumerName)
   {
      this.persistentConsumerName = consumerName;
   }

   public String getConsumerAgent()
   {
      return WSRPConstants.CONSUMER_AGENT;
   }

   public RegistrationProperty getRegistrationProperty(String name)
   {
      QName qName = QName.valueOf(name);

      return getRegistrationProperty(qName);
   }

   public RegistrationProperty getRegistrationProperty(QName name)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(name, "registration property name");
      return getRegistrationProperties(false).get(name);
   }

   public RegistrationProperty setRegistrationPropertyValue(String name, String value)
   {
      QName qName = QName.valueOf(name);

      return setRegistrationPropertyValue(qName, value);
   }

   public RegistrationProperty setRegistrationPropertyValue(QName name, String value)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(name, "registration property name");

      RegistrationProperty prop = getOrCreateRegistrationPropertiesMap(true).get(name);
      if (prop != null)
      {
         prop.setValue(value);
      }
      else
      {
         // todo: deal with language more appropriately
         prop = new RegistrationProperty(name, value, WSRPConstants.DEFAULT_LOCALE, this);
         getOrCreateRegistrationPropertiesMap(false).put(name, prop);
      }

      return prop;
   }

   public void removeRegistrationProperty(String name)
   {
      QName qName = QName.valueOf(name);

      removeRegistrationProperty(qName);
   }

   public void removeRegistrationProperty(QName name)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(name, "registration property name");
      Map<QName, RegistrationProperty> propertiesMap = getOrCreateRegistrationPropertiesMap(false);
      if (propertiesMap == null || propertiesMap.remove(name) == null)
      {
         throw new IllegalArgumentException("Cannot remove inexistent registration property '" + name + "'");
      }
      setModifiedSinceLastRefresh(true);
      setModifyRegistrationNeeded(true);
   }

   private Map<QName, RegistrationProperty> getOrCreateRegistrationPropertiesMap(boolean forceCreate)
   {
      if (forceCreate && persistentRegistrationProperties == null)
      {
         persistentRegistrationProperties = new HashMap<QName, RegistrationProperty>();
      }

      return persistentRegistrationProperties;
   }

   public Map<QName, RegistrationProperty> getRegistrationProperties()
   {
      return getRegistrationProperties(true);
   }

   private Map<QName, RegistrationProperty> getRegistrationProperties(boolean immutable)
   {
      Map<QName, RegistrationProperty> properties = getOrCreateRegistrationPropertiesMap(false);
      if (properties != null)
      {
         if (immutable)
         {
            return Collections.unmodifiableMap(properties);
         }
         else
         {
            return properties;
         }
      }
      else
      {
         return Collections.emptyMap();
      }
   }

   public void setRegistrationProperties(Map registrationProperties)
   {
      this.persistentRegistrationProperties = registrationProperties;
      regenerateRegistrationData = true;
   }

   Set<QName> getRegistrationPropertyNames()
   {
      return getRegistrationProperties().keySet();
   }

   /**
    * Refreshes the registration status and information required by the associated WSRP producer based on the information provided in the specified ServiceDescription.
    *
    * @param serviceDescription     the ServiceDescription to extract registration information from
    * @param producerId             the producer identifier associated with the ServiceDescription, used mainly for logging purposes
    * @param mergeWithLocalInfo     whether the information extracted from the ServiceDescription needs to be merged with any existing registration information already present in
    *                               this RegistrationInfo prior to the refresh operation. This is useful to provide a clean view of expected registration properties as opposed to
    *                               a
    *                               view mixing both existing and missing expected information.
    * @param forceRefresh           whether or not we should force the refresh regardless of the cache status
    * @param forceCheckOfExtraProps
    * @return
    */
   public RegistrationRefreshResult refresh(ServiceDescription serviceDescription, String producerId,
                                            boolean mergeWithLocalInfo, boolean forceRefresh, boolean forceCheckOfExtraProps)
   {
      log.debug("RegistrationInfo refresh requested");

      if (forceRefresh || isRefreshNeeded())
      {
         // if we were previously undetermined, become determined! :)
         if (isUndetermined())
         {
            Version version = parent.getEndpointConfigurationInfo().getWSRPVersion();
            String versionInfo = version != null ? " WSRP v" + version.getMajor() + " version" : " unknown WSRP version";
            setConsumerName(WSRPConstants.DEFAULT_CONSUMER_NAME + versionInfo);

            // todo: GTNWSRP-251, GTNWSRP-253: implemented but requires adding consumer identity to consumer name to work properly
            // setConsumerName(parent.getId() + " " + WSRPConstants.DEFAULT_CONSUMER_NAME + versionInfo);
         }

         // get a service description if we don't already have one
         String msg = "Couldn't get a service description to refresh from!";
         if (serviceDescription == null && parent != null)
         {
            try
            {
               serviceDescription = parent.getServiceDescription(true);
            }
            catch (PortletInvokerException e)
            {
               log.debug(msg, e);
               serviceDescription = null;
            }
         }

         // if we still don't have a service description, we have a problem!
         if (serviceDescription == null)
         {
            throw new IllegalArgumentException(msg);
         }

         persistentRegistrationProperties = getOrCreateRegistrationPropertiesMap(true);

         RegistrationRefreshResult result = new RegistrationRefreshResult();
         result.setServiceDescription(serviceDescription);

         // if we're not merging, we need to copy the current properties so that we can collect validation results.
         if (!mergeWithLocalInfo)
         {
            result.setRegistrationProperties(new HashMap<QName, RegistrationProperty>(persistentRegistrationProperties));
         }

         // reset modify registration needed flag, it will be reset during the refresh if needed
         setModifyRegistrationNeeded(false);

         if (serviceDescription.isRequiresRegistration())
         {
            requiresRegistration = Boolean.TRUE;
            log.debug("Producer '" + producerId + "' requires registration");

            // check if the configured registration properties match the producer expectations
            ModelDescription regPropDescs = serviceDescription.getRegistrationPropertyDescription();
            if (regPropDescs != null)
            {
               // assume success, there'll be time to fail if/when needed
               result.setStatus(RefreshResult.Status.SUCCESS);

               List<PropertyDescription> propertyDescriptions = regPropDescs.getPropertyDescriptions();
               if (propertyDescriptions != null && !propertyDescriptions.isEmpty())
               {

                  // extract expected properties from service description
                  Map<QName, RegistrationProperty> descriptionsMap = getRegistrationPropertyDescriptionsFromWSRP(propertyDescriptions);

                  // check that we don't have unexpected registration properties and if so, mark them as invalid or remove them
                  Set<QName> expectedNames = descriptionsMap.keySet();
                  checkForExtraProperties(persistentRegistrationProperties, expectedNames, result, !mergeWithLocalInfo);

                  // now that we've dealt with unexpected properties, check that expected properties map appropriately to existing ones
                  for (RegistrationProperty prop : descriptionsMap.values())
                  {
                     QName name = prop.getName();
                     RegistrationProperty existing = getRegistrationProperty(name);
                     if (existing != null)
                     {
                        // if the property exists, take the opportunity to add the property description... ^_^
                        existing.setDescription(prop.getDescription());
                        if (existing.isDeterminedInvalid())
                        {
                           // if it's not valid, result is failure
                           result.setStatus(RefreshResult.Status.FAILURE);
                        }
                     }
                     else
                     {
                        // if we don't have an existing property for this expected one...
                        if (mergeWithLocalInfo)
                        {
                           // add it to the existing ones to present a synthetic view to the user if we're merging
                           persistentRegistrationProperties.put(name, prop);
                        }
                        else
                        {
                           // else add it as missing to the set returned with the result
                           prop.setStatus(RegistrationProperty.Status.MISSING);
                           result.getRegistrationProperties().put(name, prop);
                        }

                        // since the producer is asking for a property we don't currently have, we need to check whether we need to modify the existing registration if we're registered, or fail the refresh if we're not.
                        log.debug("Missing value for property '" + name + "'");
                        setResultAsFailedOrModifyNeeded(result);
                     }
                  }
               }
               else
               {
                  // producer is not asking for any registration properties, decide what to do
                  handleNoRequiredRegistrationProperties(producerId, result, !mergeWithLocalInfo, forceCheckOfExtraProps);
               }
            }
            else
            {
               // producer is not asking for any registration properties, decide what to do
               handleNoRequiredRegistrationProperties(producerId, result, !mergeWithLocalInfo, forceCheckOfExtraProps);
            }
         }
         else
         {
            log.debug("Producer '" + producerId + "' doesn't require registration");
            requiresRegistration = Boolean.FALSE;
            result.setStatus(RefreshResult.Status.SUCCESS);
         }

         // if we're merging, the resulting properties are the saved properties
         if (mergeWithLocalInfo)
         {
            result.setRegistrationProperties(persistentRegistrationProperties);
         }

         // we just refreshed so we are not modified since last refresh
         setModifiedSinceLastRefresh(false);

         // if issues have been detected, mark the registration as invalid (but do not reset the data)
         // todo: check if the state is consistent with the producer expectations for example if we have registration
         // properties when the producer does not require registration?
         consistentWithProducerExpectations = !result.hasIssues();

         log.debug("Registration configuration is " + (consistentWithProducerExpectations ? "" : "NOT ") + "valid");
         return result;
      }
      else
      {
         // we didn't need to refresh
         RegistrationRefreshResult result = new RegistrationRefreshResult();
         result.setStatus(RefreshResult.Status.BYPASSED);
         result.setRegistrationProperties(persistentRegistrationProperties);
         result.setServiceDescription(serviceDescription);
         return result;
      }
   }

   /**
    * Decide what to do when the producer doesn't require any registration properties based on current available information.
    *
    * @param producerId             identifier of the producer we're refreshing from
    * @param result                 current result of the refresh to be modified for further downstream processing
    * @param keepExtraProperties    whether or not to keep unexpected properties if we find any
    * @param forceCheckOfExtraProps whether or not to force a check for extra, unexpected properties
    */
   private void handleNoRequiredRegistrationProperties(String producerId, RegistrationRefreshResult result, boolean keepExtraProperties, boolean forceCheckOfExtraProps)
   {
      log.debug("The producer didn't require any specific registration properties");
      Map<QName, RegistrationProperty> properties = getOrCreateRegistrationPropertiesMap(false);
      if (properties != null && !properties.isEmpty())
      {
         if (forceCheckOfExtraProps || !hasRegisteredIfNeeded())
         {
            log.debug("Registration data is available when none is expected by the producer");
            checkForExtraProperties(properties, Collections.<QName>emptySet(), result, keepExtraProperties);
         }
         else
         {
            log.debug("Consumer is registered: producer most likely did not resend property descriptions");
            result.setStatus(RefreshResult.Status.SUCCESS);
         }
      }
      else
      {
         log.debug("Using default registration data for producer '" + producerId + "'");
         registrationData = WSRPTypeFactory.createDefaultRegistrationData();
         registrationData.setConsumerName(getConsumerName());
         result.setStatus(RefreshResult.Status.SUCCESS);
      }
   }

   /**
    * Check if we have extra properties is the specified properties to check compared to the specified set of expected properties names, updating the current refresh result as
    * needed, keeping or not any extra properties in the given collection if we find any.
    *
    * @param propertiesToCheck   collection of properties to check for extra, unexpected properties
    * @param expectedNames       set of expected properties names
    * @param currentResult       current refresh result to be updated for further downstream processing
    * @param keepExtraProperties whether or not any found extra properties should remain in the collection of properties being looked at
    */
   private void checkForExtraProperties(Map<QName, RegistrationProperty> propertiesToCheck, Set<QName> expectedNames, RegistrationRefreshResult currentResult, boolean keepExtraProperties)
   {
      // copy all existing properties in a new set
      Set<QName> unexpected = new HashSet<QName>(propertiesToCheck.keySet());
      // and remove all the properties that are in the set of expected names
      unexpected.removeAll(expectedNames);

      // if we still have some, all we have left is unexpected properties
      if (!unexpected.isEmpty())
      {
         // we create a status message to explain all the operations we're doing
         StringBuffer message = new StringBuffer("Unexpected registration properties:\n");
         int size = unexpected.size();
         int index = 0;

         // for each unexpected property
         for (QName name : unexpected)
         {
            message.append("'").append(name).append("'");
            if (keepExtraProperties)
            {
               // if we keep the unexpected properties, mark the prop as invalid
               RegistrationProperty prop = propertiesToCheck.get(name);
               prop.setStatus(RegistrationProperty.Status.INVALID_VALUE);

               // do the same in the result
               prop = currentResult.getRegistrationProperties().get(name);
               prop.setStatus(RegistrationProperty.Status.INEXISTENT);
            }
            else
            {
               // if we don't keep unexpected properties, remove it
               message.append(" (was removed)");
               propertiesToCheck.remove(name);
            }

            if (index++ != size - 1)
            {
               message.append(";");
            }
         }
         log.debug(message.toString());
         setResultAsFailedOrModifyNeeded(currentResult); // decide whether the result of the operation is a refresh failure or a need to modify the existing registration
      }
   }

   /**
    * Determines whether the result of the current refresh operation is a failure or a need to modify the existing registration.
    *
    * @param result the current refresh result
    */
   private void setResultAsFailedOrModifyNeeded(RegistrationRefreshResult result)
   {
      if (persistentRegistrationHandle != null)
      {
         result.setStatus(RefreshResult.Status.MODIFY_REGISTRATION_REQUIRED);
         setModifyRegistrationNeeded(true);
      }
      else
      {
         result.setStatus(RefreshResult.Status.FAILURE);
      }
   }

   /**
    * @param descriptions
    * @return
    */
   private Map<QName, RegistrationProperty> getRegistrationPropertyDescriptionsFromWSRP(List<PropertyDescription> descriptions)
   {
      if (ParameterValidation.existsAndIsNotEmpty(descriptions))
      {
         Map<QName, RegistrationProperty> result = new HashMap<QName, RegistrationProperty>(descriptions.size());
         for (PropertyDescription description : descriptions)
         {
            QName name = description.getName();
            RegistrationPropertyDescription desc = WSRPUtils.convertToRegistrationPropertyDescription(description);
            RegistrationProperty prop = new RegistrationProperty(name, null, WSRPUtils.toString(desc.getLang()), this);
            prop.setDescription(desc);
            prop.setStatus(RegistrationProperty.Status.MISSING_VALUE);
            result.put(name, prop);
         }

         return result;
      }
      else
      {
         return Collections.emptyMap();
      }
   }

   void resetRegistration()
   {
      persistentRegistrationHandle = null;
      persistentRegistrationState = null;
   }

   public void setRegistrationContext(RegistrationContext registrationContext)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(registrationContext, "RegistrationContext");
      String handle = registrationContext.getRegistrationHandle();
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(handle, "registration handle", "RegistrationContext");
      persistentRegistrationHandle = handle;
      persistentRegistrationState = registrationContext.getRegistrationState();
      setRegistrationValidInternalState();
   }

   void setRegistrationValidInternalState()
   {
      // update RegistrationData if needed
      getRegistrationData();

      // mark the registration properties as valid
      if (persistentRegistrationProperties != null)
      {
         for (Object o : persistentRegistrationProperties.values())
         {
            RegistrationProperty prop = (RegistrationProperty)o;
            prop.setStatus(RegistrationProperty.Status.VALID);
         }
      }

      consistentWithProducerExpectations = Boolean.TRUE; // since we have a registration context, we're consistent with the Producer
      requiresRegistration = Boolean.TRUE; // we know we require registration
      setModifiedSinceLastRefresh(false); // our state is clean :)
      setModifyRegistrationNeeded(false);
   }

   public RegistrationContext getRegistrationContext()
   {
      if (persistentRegistrationHandle != null)
      {
         RegistrationContext registrationContext = WSRPTypeFactory.createRegistrationContext(persistentRegistrationHandle);
         registrationContext.setRegistrationState(persistentRegistrationState);
         return registrationContext;
      }
      else
      {
         return null;
      }
   }

   public boolean isModifyRegistrationNeeded()
   {
      return modifyRegistrationNeeded;
   }

   public boolean isModifiedSinceLastRefresh()
   {
      return modifiedSinceLastRefresh;
   }

   public void setModifiedSinceLastRefresh(boolean modifiedSinceLastRefresh)
   {
      this.modifiedSinceLastRefresh = modifiedSinceLastRefresh;
   }

   public void propertyValueChanged(RegistrationProperty property, RegistrationProperty.Status previousStatus, Object oldValue, Object newValue)
   {
      setModifiedSinceLastRefresh(true);

      if (previousStatus != null && !RegistrationProperty.Status.MISSING_VALUE.equals(previousStatus) && !RegistrationProperty.Status.UNCHECKED_VALUE.equals(previousStatus))
      {
         setModifyRegistrationNeeded(true);
      }

      regenerateRegistrationData = true;

      // make sure that the parent is marked as modified so that changes can be properly saved
      parent.modifyNow();
   }

   private void setModifyRegistrationNeeded(boolean modifyRegistrationNeeded)
   {
      this.modifyRegistrationNeeded = modifyRegistrationNeeded;
   }

   public boolean isRegistered()
   {
      Boolean valid = isRegistrationValid();
      if (valid == null)
      {
         return getRegistrationHandle() != null;
      }
      else
      {
         return valid;
      }
   }

   public class RegistrationRefreshResult extends RefreshResult
   {
      private Map<QName, RegistrationProperty> registrationProperties;

      public RegistrationRefreshResult()
      {
         super();
      }

      public Map<QName, RegistrationProperty> getRegistrationProperties()
      {
         return registrationProperties;
      }

      public void setRegistrationProperties(Map<QName, RegistrationProperty> registrationProperties)
      {
         this.registrationProperties = registrationProperties;
      }
   }
}
