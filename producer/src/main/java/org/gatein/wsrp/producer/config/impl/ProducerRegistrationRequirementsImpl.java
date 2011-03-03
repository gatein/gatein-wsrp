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

package org.gatein.wsrp.producer.config.impl;

import org.gatein.common.util.ParameterValidation;
import org.gatein.registration.RegistrationPolicy;
import org.gatein.registration.RegistrationPolicyChangeListener;
import org.gatein.registration.RegistrationPropertyChangeListener;
import org.gatein.registration.policies.DefaultRegistrationPolicy;
import org.gatein.registration.policies.DefaultRegistrationPropertyValidator;
import org.gatein.registration.policies.RegistrationPolicyWrapper;
import org.gatein.registration.policies.RegistrationPropertyValidator;
import org.gatein.wsrp.WSRPConstants;
import org.gatein.wsrp.producer.config.ProducerRegistrationRequirements;
import org.gatein.wsrp.registration.RegistrationPropertyDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 12017 $
 * @since 2.6
 */
public class ProducerRegistrationRequirementsImpl implements ProducerRegistrationRequirements
{
   private static final Logger log = LoggerFactory.getLogger(ProducerRegistrationRequirementsImpl.class);

   private boolean requiresRegistration;
   private boolean fullServiceDescriptionRequiresRegistration;
   private RegistrationPolicy policy;
   private String policyClassName;
   private String validatorClassName;
   private long lastModified;

   private Map<QName, RegistrationPropertyDescription> registrationProperties;

   private Set<RegistrationPropertyChangeListener> propertyChangeListeners = new HashSet<RegistrationPropertyChangeListener>(3);
   private Set<RegistrationPolicyChangeListener> policyChangeListeners = new HashSet<RegistrationPolicyChangeListener>(3);

   public ProducerRegistrationRequirementsImpl(boolean requiresMarshalling, boolean requiresRegistration, boolean fullServiceDescriptionRequiresRegistration)
   {
      this();
      this.requiresRegistration = requiresRegistration;
      this.fullServiceDescriptionRequiresRegistration = fullServiceDescriptionRequiresRegistration;
      modifyNow();
   }

   public ProducerRegistrationRequirementsImpl()
   {
      registrationProperties = new HashMap<QName, RegistrationPropertyDescription>(7);
   }

   public ProducerRegistrationRequirementsImpl(ProducerRegistrationRequirements other)
   {
      this(false, other.isRegistrationRequired(), other.isRegistrationRequiredForFullDescription());
      setPolicy(other.getPolicy());

      Set<Map.Entry<QName, RegistrationPropertyDescription>> otherProps = other.getRegistrationProperties().entrySet();
      registrationProperties = new HashMap<QName, RegistrationPropertyDescription>(otherProps.size());
      for (Map.Entry<QName, RegistrationPropertyDescription> entry : otherProps)
      {
         registrationProperties.put(entry.getKey(), new RegistrationPropertyDescription(entry.getValue()));
      }

      modifyNow();
   }

   private void modifyNow()
   {
      lastModified = System.nanoTime();
   }

   public long getLastModified()
   {
      return lastModified;
   }

   public void setRegistrationProperties(Map<QName, RegistrationPropertyDescription> regProps)
   {
      if (!registrationProperties.equals(regProps))
      {
         registrationProperties.clear();

         for (RegistrationPropertyDescription propertyDescription : regProps.values())
         {
            addRegistrationProperty(new RegistrationPropertyDescription(propertyDescription));
         }

         modifyNow();

         notifyRegistrationPropertyChangeListeners();
      }
   }

   public boolean isRegistrationRequired()
   {
      return requiresRegistration;
   }

   public void setRegistrationRequired(boolean requiresRegistration)
   {
      if (this.requiresRegistration != requiresRegistration)
      {
         // if we switch from requiring registration to no registration, erase registration properties
         if (this.requiresRegistration && !requiresRegistration)
         {
            clearRegistrationProperties();
         }

         this.requiresRegistration = requiresRegistration;
         modifyNow();
      }
   }

   public boolean isRegistrationRequiredForFullDescription()
   {
      return fullServiceDescriptionRequiresRegistration;
   }

   public void setRegistrationRequiredForFullDescription(boolean fullServiceDescriptionRequiresRegistration)
   {
      if (this.fullServiceDescriptionRequiresRegistration != fullServiceDescriptionRequiresRegistration)
      {
         this.fullServiceDescriptionRequiresRegistration = fullServiceDescriptionRequiresRegistration;
         modifyNow();
      }
   }

   public Map<QName, RegistrationPropertyDescription> getRegistrationProperties()
   {
      return Collections.unmodifiableMap(registrationProperties);
   }

   public void addRegistrationProperty(RegistrationPropertyDescription propertyDescription)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(propertyDescription, "PropertyDescription");
      QName name = propertyDescription.getName();
      ParameterValidation.throwIllegalArgExceptionIfNull(name, "Property name");

      registrationProperties.put(name, propertyDescription);
      modifyNow();
      propertyDescription.setValueChangeListener(this);
      notifyRegistrationPropertyChangeListeners();
   }

   public RegistrationPropertyDescription addEmptyRegistrationProperty(String name)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(name, "Property name");
      RegistrationPropertyDescription reg = new RegistrationPropertyDescription(name, WSRPConstants.XSD_STRING);

      addRegistrationProperty(reg);

      return reg;
   }

   public boolean acceptValueFor(QName propertyName, Object value)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(propertyName, "Property name");

      QName type = getPropertyDescription(propertyName).getType();
      // todo: decide if type is actually compatible with value...
      return true;
   }

   public boolean acceptValueFor(String propertyName, Object value)
   {
      return acceptValueFor(QName.valueOf(propertyName), value);
   }

   public RegistrationPropertyDescription getRegistrationPropertyWith(String name)
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(name, "Property name", null);
      return getRegistrationPropertyWith(QName.valueOf(name));
   }


   public RegistrationPropertyDescription getRegistrationPropertyWith(QName name)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(name, "Property name");
      return registrationProperties.get(name);
   }

   private RegistrationPropertyDescription getPropertyDescription(QName propertyName)
   {
      // copy to ensure immutability
      return new RegistrationPropertyDescription(registrationProperties.get(propertyName));
   }

   public RegistrationPropertyDescription removeRegistrationProperty(QName propertyName)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(propertyName, "Property name");
      RegistrationPropertyDescription prop = registrationProperties.remove(propertyName);
      if (prop != null)
      {
         modifyNow();
         notifyRegistrationPropertyChangeListeners();
      }

      return prop;
   }

   public void clearRegistrationProperties()
   {
      registrationProperties.clear();
      modifyNow();
      notifyRegistrationPropertyChangeListeners();
   }

   public RegistrationPropertyDescription removeRegistrationProperty(String propertyName)
   {
      return removeRegistrationProperty(QName.valueOf(propertyName));
   }

   /*
    * == ValueChangeListener implementation
    */

   public void valueHasChanged(RegistrationPropertyDescription originatingProperty, Object oldValue, Object newValue, boolean isName)
   {
      modifyNow();
      notifyRegistrationPropertyChangeListeners();
      if (isName && oldValue instanceof QName)
      {
         propertyHasBeenRenamed(originatingProperty, (QName)oldValue);
      }
   }

   /*
   * == RegistrationPropertyChangeListeners handling ==
   */

   public void notifyRegistrationPropertyChangeListeners()
   {
      Map<QName, RegistrationPropertyDescription> newRegistrationProperties = Collections.unmodifiableMap(registrationProperties);
      for (RegistrationPropertyChangeListener listener : propertyChangeListeners)
      {
         listener.propertiesHaveChanged(newRegistrationProperties);
      }
   }

   public void clearRegistrationPropertyChangeListeners()
   {
      propertyChangeListeners.clear();
   }

   public void addRegistrationPropertyChangeListener(RegistrationPropertyChangeListener listener)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(listener, "RegistrationPropertyChangeListener");
      propertyChangeListeners.add(listener);
   }

   public void removeRegistrationPropertyChangeListener(RegistrationPropertyChangeListener listener)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(listener, "RegistrationPropertyChangeListener");
      propertyChangeListeners.remove(listener);
   }

   public Set<RegistrationPropertyChangeListener> getPropertyChangeListeners()
   {
      return propertyChangeListeners;
   }

   /*
   * == RegistrationPolicyChangeListeners handling
   */

   public void addRegistrationPolicyChangeListener(RegistrationPolicyChangeListener listener)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(listener, "RegistrationPolicyChangeListener");
      policyChangeListeners.add(listener);
   }

   public void removeRegistrationPolicyChangeListener(RegistrationPolicyChangeListener listener)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(listener, "RegistrationPolicyChangeListener");
      policyChangeListeners.remove(listener);
   }

   public void clearRegistrationPolicyChangeListeners()
   {
      policyChangeListeners.clear();
   }

   public void notifyRegistrationPolicyChangeListeners()
   {
      for (RegistrationPolicyChangeListener listener : policyChangeListeners)
      {
         listener.policyUpdatedTo(policy);
      }
   }

   public Set<RegistrationPolicyChangeListener> getPolicyChangeListeners()
   {
      return policyChangeListeners;
   }

   public void setPolicy(RegistrationPolicy policy)
   {
      if (ParameterValidation.isOldAndNewDifferent(this.policy, policy))
      {
         if (policy != null)
         {
            this.policy = RegistrationPolicyWrapper.wrap(policy);
            policyClassName = policy.getClassName();

            if (DEFAULT_POLICY_CLASS_NAME.equals(policyClassName))
            {
               DefaultRegistrationPolicy registrationPolicy = (DefaultRegistrationPolicy)policy;
               validatorClassName = registrationPolicy.getValidator().getClass().getName();
            }
            else
            {
               validatorClassName = null;
            }
         }
         else
         {
            this.policy = null;
         }

         modifyNow();
         notifyRegistrationPolicyChangeListeners();
      }
   }

   public RegistrationPolicy getPolicy()
   {
      if (policy == null && requiresRegistration)
      {
         reloadPolicyFrom(policyClassName, validatorClassName);
      }

      return policy;
   }

   public void reloadPolicyFrom(String policyClassName, String validatorClassName)
   {
      // only reload if we don't already have a policy or if the requested policy/validator classes are different
      // from the ones we already have 
      if (policy == null || ParameterValidation.isOldAndNewDifferent(this.policyClassName, policyClassName) ||
         (DEFAULT_POLICY_CLASS_NAME.equals(policyClassName) && ParameterValidation.isOldAndNewDifferent(this.validatorClassName, validatorClassName))
         )
      {
         if (policyClassName != null && !DEFAULT_POLICY_CLASS_NAME.equals(policyClassName))
         {
            log.debug("Using registration policy: " + policyClassName);
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            try
            {
               Class policyClass = loader.loadClass(policyClassName);
               if (!RegistrationPolicy.class.isAssignableFrom(policyClass))
               {
                  throw new IllegalArgumentException("Policy class does not implement RegistrationPolicy!");
               }
               RegistrationPolicy policy = (RegistrationPolicy)policyClass.newInstance();

               setPolicy(policy);
            }
            catch (ClassNotFoundException e)
            {
               throw new IllegalArgumentException("Couldn't find policy class " + policyClassName + ".", e);
            }
            catch (Exception e)
            {
               throw new IllegalArgumentException("Couldn't instantiate policy class.", e);
            }
         }
         else
         {
            log.debug("Using default registration policy: " + DEFAULT_POLICY_CLASS_NAME);
            RegistrationPropertyValidator validator;
            if (validatorClassName != null && validatorClassName.length() > 0 && !DEFAULT_VALIDATOR_CLASS_NAME.equals(validatorClassName))
            {
               log.debug("Using registration property validator: " + validatorClassName);
               ClassLoader loader = Thread.currentThread().getContextClassLoader();
               try
               {
                  Class validatorClass = loader.loadClass(validatorClassName);
                  if (!RegistrationPropertyValidator.class.isAssignableFrom(validatorClass))
                  {
                     throw new IllegalArgumentException("Validator class does not implement RegistrationPropertyValidator!");
                  }
                  validator = (RegistrationPropertyValidator)validatorClass.newInstance();
               }
               catch (ClassNotFoundException e)
               {
                  throw new IllegalArgumentException("Couldn't find validator class " + validatorClassName + ".", e);
               }
               catch (Exception e)
               {
                  throw new IllegalArgumentException("Couldn't instantiate validator class.", e);
               }
            }
            else
            {
               log.debug("Using default registration property validator: " + DEFAULT_VALIDATOR_CLASS_NAME);
               validator = new DefaultRegistrationPropertyValidator();
            }


            DefaultRegistrationPolicy delegate = new DefaultRegistrationPolicy();
            delegate.setValidator(validator);
            setPolicy(delegate);
         }
      }
   }

   public void propertyHasBeenRenamed(RegistrationPropertyDescription propertyDescription, QName oldName)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(propertyDescription, "RegistrationPropertyDescription");
      ParameterValidation.throwIllegalArgExceptionIfNull(oldName, "property old name");

      if (registrationProperties.containsKey(oldName))
      {
         synchronized (this)
         {
            registrationProperties.remove(oldName);
            registrationProperties.put(propertyDescription.getName(), propertyDescription);
            modifyNow();
         }
      }
   }


   public void setPolicyClassName(String policyClassName)
   {
      this.policyClassName = policyClassName;
   }

   public String getPolicyClassName()
   {
      if (policyClassName == null)
      {
         return DEFAULT_POLICY_CLASS_NAME;
      }

      return policyClassName;
   }

   public void setValidatorClassName(String validatorClassName)
   {
      this.validatorClassName = validatorClassName;
   }

   public String getValidatorClassName()
   {
      return validatorClassName;
   }
}
