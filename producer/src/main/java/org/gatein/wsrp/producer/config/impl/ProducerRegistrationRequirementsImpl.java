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

import com.google.common.base.Function;
import org.gatein.common.util.ParameterValidation;
import org.gatein.registration.RegistrationPolicy;
import org.gatein.registration.RegistrationPolicyChangeListener;
import org.gatein.registration.RegistrationPropertyChangeListener;
import org.gatein.registration.policies.DefaultRegistrationPolicy;
import org.gatein.registration.policies.DefaultRegistrationPropertyValidator;
import org.gatein.registration.policies.RegistrationPolicyWrapper;
import org.gatein.registration.policies.RegistrationPropertyValidator;
import org.gatein.wsrp.SupportsLastModified;
import org.gatein.wsrp.WSRPConstants;
import org.gatein.wsrp.api.plugins.PluginsAccess;
import org.gatein.wsrp.producer.config.ProducerRegistrationRequirements;
import org.gatein.wsrp.registration.RegistrationPropertyDescription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 12017 $
 * @since 2.6
 */
public class ProducerRegistrationRequirementsImpl extends SupportsLastModified implements ProducerRegistrationRequirements
{
   private static final Logger log = LoggerFactory.getLogger(ProducerRegistrationRequirementsImpl.class);
   public static final Function<Class<? extends RegistrationPolicy>, String> CLASS_TO_NAME_FUNCTION = new Function<Class<? extends RegistrationPolicy>, String>()
   {
      @Override
      public String apply(Class<? extends RegistrationPolicy> aClass)
      {
         return aClass.getSimpleName();
      }
   };

   private boolean requiresRegistration;
   private boolean fullServiceDescriptionRequiresRegistration;
   private transient RegistrationPolicy policy;
   private String policyClassName;
   private String validatorClassName;

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

      // always use the default RegistrationPolicy by default
      setPolicy(new DefaultRegistrationPolicy());
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

      setLastModified(other.getLastModified());
   }

   public void setRegistrationProperties(Collection<RegistrationPropertyDescription> regProps)
   {
      Set<RegistrationPropertyDescription> original = new HashSet<RegistrationPropertyDescription>(registrationProperties.values());
      Set<RegistrationPropertyDescription> newProps = new HashSet<RegistrationPropertyDescription>(regProps);
      if (modifyNowIfNeeded(original, newProps))
      {
         registrationProperties.clear();

         for (RegistrationPropertyDescription propertyDescription : regProps)
         {
            addRegistrationProperty(new RegistrationPropertyDescription(propertyDescription));
         }

         notifyRegistrationPropertyChangeListeners();
      }
   }

   public boolean isRegistrationRequired()
   {
      return requiresRegistration;
   }

   public void setRegistrationRequired(boolean requiresRegistration)
   {
      if (modifyNowIfNeeded(this.requiresRegistration, requiresRegistration))
      {
         // if we switch from requiring registration to no registration, erase registration properties
         if (this.requiresRegistration && !requiresRegistration)
         {
            clearRegistrationProperties();
         }

         this.requiresRegistration = requiresRegistration;
      }
   }

   public boolean isRegistrationRequiredForFullDescription()
   {
      return fullServiceDescriptionRequiresRegistration;
   }

   public void setRegistrationRequiredForFullDescription(boolean fullServiceDescriptionRequiresRegistration)
   {
      if (modifyNowIfNeeded(this.fullServiceDescriptionRequiresRegistration, fullServiceDescriptionRequiresRegistration))
      {
         this.fullServiceDescriptionRequiresRegistration = fullServiceDescriptionRequiresRegistration;
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

      final RegistrationPropertyDescription old = registrationProperties.put(name, propertyDescription);
      if (modifyNowIfNeeded(old, propertyDescription))
      {
         propertyDescription.setValueChangeListener(this);
         notifyRegistrationPropertyChangeListeners();
      }
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
      final RegistrationPropertyDescription description = registrationProperties.get(propertyName);
      if (description != null)
      {
         return new RegistrationPropertyDescription(description);
      }
      else
      {
         throw new IllegalArgumentException("Unknown property name '" + propertyName + "'");
      }
   }

   public RegistrationPropertyDescription removeRegistrationProperty(QName propertyName)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(propertyName, "Property name");
      RegistrationPropertyDescription prop = registrationProperties.remove(propertyName);
      if (modifyNowIfNeeded(null, prop))
      {
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
      if (modifyNowIfNeeded(this.policy, policy))
      {
         // make sure we always have a RegistrationPolicy
         if (policy == null)
         {
            log.debug("Specified RegistrationPolicy was null, using the default one instead.");
            policy = new DefaultRegistrationPolicy();
         }

         this.policy = RegistrationPolicyWrapper.wrap(policy);
         policyClassName = policy.getClassName();

         if (DEFAULT_POLICY_CLASS_NAME.equals(policyClassName))
         {
            DefaultRegistrationPolicy registrationPolicy = (DefaultRegistrationPolicy)RegistrationPolicyWrapper.unwrap(policy);
            validatorClassName = registrationPolicy.getValidator().getClass().getName();
         }
         else
         {
            validatorClassName = null;
         }
         notifyRegistrationPolicyChangeListeners();
      }
   }

   public RegistrationPolicy getPolicy()
   {
      reloadPolicyFrom(policyClassName, validatorClassName);

      return policy;
   }

   public void reloadPolicyFrom(String policyClassName, String validatorClassName)
   {
      // only reload if we don't already have a policy or if the requested policy/validator classes are different
      // from the ones we already have 
      if (policy == null || (requiresRegistration && (!policy.getClassName().equals(policyClassName) || isCurrentValidatorClassDifferentFrom(validatorClassName))))
      {
         if (policyClassName != null && !DEFAULT_POLICY_CLASS_NAME.equals(policyClassName))
         {
            log.debug("Trying to use registration policy: " + policyClassName);
            setPolicy(PluginsAccess.getPlugins().createPluginInstance(policyClassName, RegistrationPolicy.class));
         }
         else
         {
            log.debug("Using default registration policy: " + DEFAULT_POLICY_CLASS_NAME);
            RegistrationPropertyValidator validator;
            if (validatorClassName != null && validatorClassName.length() > 0 && !DEFAULT_VALIDATOR_CLASS_NAME.equals(validatorClassName))
            {
               log.debug("Trying to use registration property validator: " + validatorClassName);
               validator = PluginsAccess.getPlugins().createPluginInstance(validatorClassName, RegistrationPropertyValidator.class);
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

   @Override
   public List<String> getAvailableRegistrationPolicies()
   {
      return PluginsAccess.getPlugins().getPluginImplementationNames(RegistrationPolicy.class, DEFAULT_POLICY_CLASS_NAME);
   }

   @Override
   public List<String> getAvailableRegistrationPropertyValidators()
   {
      return PluginsAccess.getPlugins().getPluginImplementationNames(RegistrationPropertyValidator.class, DEFAULT_VALIDATOR_CLASS_NAME);
   }

   private boolean isCurrentValidatorClassDifferentFrom(String validatorClassName)
   {
      return policy instanceof DefaultRegistrationPolicy && !((DefaultRegistrationPolicy)policy).getValidator().getClass().getCanonicalName().equals(validatorClassName);
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
      setValidatorClassName(null); // reset validator class name when the policy class name changes
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
