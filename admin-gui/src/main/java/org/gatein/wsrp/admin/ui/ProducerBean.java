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

package org.gatein.wsrp.admin.ui;

import org.gatein.registration.policies.DefaultRegistrationPolicy;
import org.gatein.registration.policies.RegistrationPolicyWrapper;
import org.gatein.wsrp.WSRPConstants;
import org.gatein.wsrp.producer.config.ProducerConfiguration;
import org.gatein.wsrp.producer.config.ProducerConfigurationService;
import org.gatein.wsrp.producer.config.ProducerRegistrationRequirements;
import org.gatein.wsrp.registration.LocalizedString;
import org.gatein.wsrp.registration.RegistrationPropertyDescription;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import javax.faces.validator.ValidatorException;
import javax.xml.namespace.QName;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 12854 $
 * @since 2.6.3
 */
public class ProducerBean extends WSRPManagedBean implements Serializable
{
   private static final String REGISTRATION_PROPERTY_TYPE = "REGISTRATION_PROPERTY_TYPE";
   private static final String SELECTED_PROP = "selectedProp";
   private static final String PROPERTY = "property";
   private static final String CURRENT_CONFIG = "currentConfig";
   private transient ProducerConfigurationService configurationService;
   private transient LocalProducerConfiguration localProducerConfiguration;

   @PostConstruct
   public void init()
   {
      if (localProducerConfiguration == null)
      {
         // try to get the local configuration from session to restore it after a property deletion for example
         localProducerConfiguration = beanContext.getFromSession(CURRENT_CONFIG, LocalProducerConfiguration.class);

         // if it's still null, load it from persistence
         if (localProducerConfiguration == null)
         {
            localProducerConfiguration = new LocalProducerConfiguration(this);
            localProducerConfiguration.initFrom(getConfiguration());
            return;
         }
         else
         {
            beanContext.removeFromSession(CURRENT_CONFIG);
         }
      }

      // update configuration if needed
      localProducerConfiguration.updateIfNeededFrom();
   }


   public ProducerConfigurationService getConfigurationService()
   {
      if (configurationService == null)
      {
         configurationService = beanContext.findBean("ProducerConfigurationService", ProducerConfigurationService.class);
      }
      return configurationService;
   }

   public void setConfigurationService(ProducerConfigurationService configurationService)
   {
      this.configurationService = configurationService;
   }

   public ProducerConfiguration getConfiguration()
   {
      return getConfigurationService().getConfiguration();
   }

   public boolean isRegistrationRequiredForFullDescription()
   {
      return getLocalConfiguration().isRegistrationRequiredForFullDescription();
   }

   public void setRegistrationRequiredForFullDescription(boolean requireRegForFullDescription)
   {
      getLocalConfiguration().setRegistrationRequiredForFullDescription(requireRegForFullDescription);
   }

   public boolean isRegistrationRequired()
   {
      return getLocalConfiguration().isRegistrationRequired();
   }

   public void setRegistrationRequired(boolean requireRegistration)
   {
      getLocalConfiguration().setRegistrationRequired(requireRegistration);
   }

   public boolean isStrictMode()
   {
      return getLocalConfiguration().isUsingStrictMode();
   }

   public void setStrictMode(boolean strictMode)
   {
      getLocalConfiguration().setUsingStrictMode(strictMode);
   }

   public List<RegistrationPropertyDescription> getRegistrationProperties()
   {
      return getLocalConfiguration().getRegistrationProperties();
   }

   public boolean isRegistrationPropertiesEmpty()
   {
      return getLocalConfiguration().getRegistrationProperties().isEmpty();
   }

   public List<SelectItem> getSupportedPropertyTypes()
   {
      return Collections.singletonList(new SelectItem("xsd:string"));
   }

   public String save()
   {
      try
      {
         // replicate local state to producer state
         ProducerConfiguration currentlyPersistedConfiguration = getConfiguration();
         LocalProducerConfiguration localConfiguration = getLocalConfiguration();

         ProducerRegistrationRequirements registrationRequirements = currentlyPersistedConfiguration.getRegistrationRequirements();

         registrationRequirements.setRegistrationRequiredForFullDescription(localConfiguration.isRegistrationRequiredForFullDescription());
         registrationRequirements.setRegistrationRequired(localConfiguration.isRegistrationRequired());

         registrationRequirements.reloadPolicyFrom(localConfiguration.getRegistrationPolicyClassName(), localConfiguration.getValidatorClassName());

         registrationRequirements.setRegistrationProperties(localConfiguration.getRegistrationProperties());

         currentlyPersistedConfiguration.setUsingStrictMode(localConfiguration.isUsingStrictMode());

         getConfigurationService().saveConfiguration();

         // force a reload local state
         localProducerConfiguration = null;

         beanContext.createInfoMessage("bean_producer_save_success");
      }
      catch (Exception e)
      {
         log.debug("Couldn't save producer", e);
         beanContext.createErrorMessage("bean_producer_cannot_save", e.getLocalizedMessage());
      }
      return null;
   }

   public String reloadConfiguration()
   {
      try
      {
         getConfigurationService().reloadConfiguration();

         // force a reload local state
         localProducerConfiguration = null;

         beanContext.createInfoMessage("bean_producer_cancel_success");
      }
      catch (Exception e)
      {
         log.debug("Couldn't reload producer configuration", e);
         beanContext.createErrorMessage("bean_producer_cannot_reload", e.getLocalizedMessage());
      }
      return null;
   }

   public String addRegistrationProperty()
   {
      getLocalConfiguration().addEmptyRegistrationProperty(PROPERTY + System.currentTimeMillis());
      return null;
   }

   public String confirmPropDeletion(String selectedProp)
   {
      beanContext.replaceInSession(SELECTED_PROP, selectedProp);
      beanContext.replaceInSession(CURRENT_CONFIG, getLocalConfiguration());

      return "confirmPropDeletion";
   }

   public String deleteRegistrationProperty()
   {
      final String propertyName = beanContext.getFromSession(SELECTED_PROP, String.class);
      if (propertyName != null)
      {
         getLocalConfiguration().removeRegistrationProperty(propertyName);
         beanContext.replaceInSession(CURRENT_CONFIG, getLocalConfiguration());
      }
      beanContext.removeFromSession(SELECTED_PROP);
      return "producer";
   }

   public void requireRegistrationListener(ValueChangeEvent event)
   {
      setRegistrationRequired((Boolean)event.getNewValue());

      bypassAndRedisplay();
   }

   public void strictModeListener(ValueChangeEvent event)
   {
      setStrictMode((Boolean)event.getNewValue());

      bypassAndRedisplay();
   }

   public void requireRegistrationForFullDescListener(ValueChangeEvent event)
   {
      setRegistrationRequiredForFullDescription((Boolean)event.getNewValue());

      bypassAndRedisplay();
   }

   protected String getObjectTypeName()
   {
      return REGISTRATION_PROPERTY_TYPE;
   }

   public boolean isAlreadyExisting(String objectName)
   {
      // allow for edit of properties since they will be replaced anyway
      return false;
   }

   private LocalProducerConfiguration getLocalConfiguration()
   {
      init();
      return localProducerConfiguration;
   }

   public String getV1WSDL()
   {
      return beanContext.getServerAddress() + "/wsrp-producer/v1/MarkupService?wsdl";
   }

   public String getV2WSDL()
   {
      return beanContext.getServerAddress() + "/wsrp-producer/v2/MarkupService?wsdl";
   }

   public void validate(FacesContext facesContext, UIComponent uiComponent, Object o)
   {
      validate(facesContext, uiComponent, o, getValidator());
   }

   private void validate(FacesContext facesContext, UIComponent uiComponent, Object o, PropertyValidator validator)
   {
      String toValidate = null;
      if (o instanceof String)
      {
         toValidate = (String)o;
      }
      else if (o instanceof LocalizedString)
      {
         toValidate = LocalizedStringConverter.getAsString(o);
      }

      final String validated = this.checkNameValidity(toValidate, uiComponent.getClientId(facesContext), validator);
      if (validated == null)
      {
         throw new ValidatorException(new FacesMessage()); // need a non-null FacesMessage to avoid NPE
      }
   }

   public void validateLabelOrHint(FacesContext facesContext, UIComponent uiComponent, Object o)
   {
      validate(facesContext, uiComponent, o, new LabelOrHintValidator());
   }

   public List<SelectItem> getAvailableRegistrationPolicies()
   {
      return getSelectItemsFrom(getConfiguration().getRegistrationRequirements().getAvailableRegistrationPolicies());
   }

   public void policyChangeListener(ValueChangeEvent event)
   {
      getLocalConfiguration().setRegistrationPolicyClassName((String)event.getNewValue());

      bypassAndRedisplay();
   }

   public String getRegistrationPolicyClassName()
   {
      return getLocalConfiguration().getRegistrationPolicyClassName();
   }

   public void setRegistrationPolicyClassName(String policyClassName)
   {
      getLocalConfiguration().setRegistrationPolicyClassName(policyClassName);
   }

   public boolean isDefaultRegistrationPolicy()
   {
      return getLocalConfiguration().isDefaultRegistrationPolicy();
   }

   public String getValidatorClassName()
   {
      return getLocalConfiguration().getValidatorClassName();
   }

   public void setValidatorClassName(String validatorClassName)
   {
      getLocalConfiguration().setValidatorClassName(validatorClassName);
   }

   public List<SelectItem> getAvailableValidators()
   {
      return getSelectItemsFrom(getConfiguration().getRegistrationRequirements().getAvailableRegistrationPropertyValidators());
   }

   private static class LocalProducerConfiguration implements Serializable
   {
      private List<RegistrationPropertyDescription> registrationProperties;
      private boolean strictMode;
      private String policyClassName;
      private String validatorClassName;
      private boolean registrationRequiredForFullDescription;
      private boolean registrationRequired;
      private long originalLastModified;
      private final ProducerBean producerBean;
      private long lastUpdateCheckTime;

      public LocalProducerConfiguration(ProducerBean producerBean)
      {
         this.producerBean = producerBean;
      }

      public void initFrom(ProducerConfiguration configuration)
      {
         ProducerRegistrationRequirements registrationRequirements = configuration.getRegistrationRequirements();
         Map<QName, RegistrationPropertyDescription> descriptions = registrationRequirements.getRegistrationProperties();
         registrationProperties = new LinkedList<RegistrationPropertyDescription>(descriptions.values());
         Collections.sort(registrationProperties);

         policyClassName = registrationRequirements.getPolicyClassName();
         if (isDefaultRegistrationPolicy())
         {
            DefaultRegistrationPolicy policy = (DefaultRegistrationPolicy)RegistrationPolicyWrapper.unwrap(registrationRequirements.getPolicy());
            validatorClassName = policy.getValidator().getClass().getName();
         }
         else
         {
            validatorClassName = null;
         }

         registrationRequiredForFullDescription = registrationRequirements.isRegistrationRequiredForFullDescription();
         registrationRequired = registrationRequirements.isRegistrationRequired();

         this.strictMode = configuration.isUsingStrictMode();

         originalLastModified = configuration.getLastModified();
      }

      public boolean isRegistrationRequiredForFullDescription()
      {
         return registrationRequiredForFullDescription;
      }

      public void setRegistrationRequiredForFullDescription(boolean registrationRequiredForFullDescription)
      {
         this.registrationRequiredForFullDescription = registrationRequiredForFullDescription;
      }

      public List<RegistrationPropertyDescription> getRegistrationProperties()
      {
         return registrationProperties;
      }

      public void addEmptyRegistrationProperty(String propertyName)
      {
         RegistrationPropertyDescription prop = new RegistrationPropertyDescription(propertyName, WSRPConstants.XSD_STRING);

         // Search for the non-existent item
         int index = Collections.binarySearch(registrationProperties, prop);

         // Add the non-existent item to the list
         if (index < 0)
         {
            registrationProperties.add(-index - 1, prop);
         }
      }

      public void removeRegistrationProperty(String propertyName)
      {
         int toRemove = -1;
         int index = 0;
         for (RegistrationPropertyDescription property : registrationProperties)
         {
            if (property.getName().equals(QName.valueOf(propertyName)))
            {
               toRemove = index;
               break;
            }
            index++;
         }

         if (toRemove != -1)
         {
            registrationProperties.remove(toRemove);
         }
      }

      public boolean isUsingStrictMode()
      {
         return strictMode;
      }

      public void setUsingStrictMode(boolean usingStrictMode)
      {
         this.strictMode = usingStrictMode;
      }

      public String getRegistrationPolicyClassName()
      {
         return policyClassName;
      }

      public void setRegistrationPolicyClassName(String className)
      {
         policyClassName = className;
      }

      public boolean isDefaultRegistrationPolicy()
      {
         return ProducerRegistrationRequirements.DEFAULT_POLICY_CLASS_NAME.equals(getRegistrationPolicyClassName());
      }

      public String getValidatorClassName()
      {
         return validatorClassName;
      }

      public void setValidatorClassName(String className)
      {
         validatorClassName = className;
      }

      public boolean isRegistrationRequired()
      {
         return registrationRequired;
      }

      public void setRegistrationRequired(boolean registrationRequired)
      {
         this.registrationRequired = registrationRequired;
      }

      public void updateIfNeededFrom()
      {
         // cool down period to avoid hammering the DB with constant checks
         if(System.currentTimeMillis() - lastUpdateCheckTime < 100)
         {
            return;
         }

         // if the configuration we got from the configuration service has been modified after the one we initialized from, we need to update ourselves
         if (producerBean.getConfigurationService().getPersistedLastModifiedForConfiguration() > originalLastModified)
         {
            initFrom(producerBean.getConfiguration());
         }

         lastUpdateCheckTime = System.currentTimeMillis();
      }
   }

   private class LabelOrHintValidator extends DefaultPropertyValidator
   {

      public static final String INVALID_HINT_OR_LABEL_ERROR = "INVALID_HINT_OR_LABEL_ERROR";

      @Override
      public boolean checkForDuplicates()
      {
         // no need to check for duplicates
         return false;
      }

      @Override
      public String doSimpleChecks(String value)
      {
         // allow . in value
         return (value.indexOf('/') != -1) ? null : value;
      }

      @Override
      public String getErrorKey()
      {
         return INVALID_HINT_OR_LABEL_ERROR;
      }
   }
}
