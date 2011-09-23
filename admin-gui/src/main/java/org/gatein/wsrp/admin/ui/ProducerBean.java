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

import org.gatein.registration.RegistrationPolicy;
import org.gatein.registration.policies.DefaultRegistrationPolicy;
import org.gatein.registration.policies.RegistrationPolicyWrapper;
import org.gatein.wsrp.producer.config.ProducerConfiguration;
import org.gatein.wsrp.producer.config.ProducerConfigurationService;
import org.gatein.wsrp.producer.config.ProducerRegistrationRequirements;
import org.gatein.wsrp.producer.config.impl.ProducerRegistrationRequirementsImpl;
import org.gatein.wsrp.registration.RegistrationPropertyDescription;

import javax.faces.component.UIComponent;
import javax.faces.component.html.HtmlDataTable;
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
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
public class ProducerBean extends ManagedBean implements Serializable
{
   private transient ProducerConfigurationService configurationService;
   private String policyClassName;
   private String validatorClassName;
   private static final String PROPERTY = "property";
   private static final String PRODUCER = "producer";
   private String selectedProp;
   private transient LocalProducerConfiguration localProducerConfiguration;

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

   public String getRegistrationPolicyClassName()
   {
      RegistrationPolicy policy = getLocalConfiguration().getPolicy();
      if (policy != null)
      {
         return policy.getClassName();
      }
      else
      {
         return beanContext.getMessageFromBundle("bean_producer_regpolicy_unset");
      }
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
      if (isDefaultRegistrationPolicy())
      {
         DefaultRegistrationPolicy policy = (DefaultRegistrationPolicy)RegistrationPolicyWrapper.unwrap(getLocalConfiguration().getPolicy());
         return policy.getValidator().getClass().getName();
      }
      throw new IllegalStateException("getValidatorClassName shouldn't be called if we're not using the default registration");
   }

   public void setValidatorClassName(String className)
   {
      validatorClassName = className;
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

   public List<SelectItem> getSupportedPropertyTypes()
   {
      return Collections.singletonList(new SelectItem("xsd:string"));
   }

   public String getSelectedPropertyName()
   {
      return selectedProp;
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

         registrationRequirements.reloadPolicyFrom(policyClassName, validatorClassName);

         registrationRequirements.setRegistrationProperties(localConfiguration.getRegistrationRequirements().getRegistrationProperties());

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
      return PRODUCER;
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
      return PRODUCER;
   }

   public String addRegistrationProperty()
   {
      getLocalConfiguration().addEmptyRegistrationProperty(PROPERTY + System.currentTimeMillis());
      return PRODUCER;
   }

   public String deleteRegistrationProperty()
   {
      getLocalConfiguration().removeRegistrationProperty(selectedProp);
      return PRODUCER;
   }

   public void requireRegistrationListener(ValueChangeEvent event)
   {
      setRegistrationRequired((Boolean)event.getNewValue());

      // bypass the rest of the life cycle and re-display page
      FacesContext.getCurrentInstance().renderResponse();
   }

   public void strictModeListener(ValueChangeEvent event)
   {
      setStrictMode((Boolean)event.getNewValue());

      // bypass the rest of the life cycle and re-display page
      FacesContext.getCurrentInstance().renderResponse();
   }

   public void requireRegistrationForFullDescListener(ValueChangeEvent event)
   {
      setRegistrationRequiredForFullDescription((Boolean)event.getNewValue());

      // bypass the rest of the life cycle and re-display page
      FacesContext.getCurrentInstance().renderResponse();
   }

   public void selectProperty(ActionEvent event)
   {
      // Retrieve parent table from event
      HtmlDataTable table = getParentDataTable((UIComponent)event.getSource());

      // Get selected prop
      RegistrationPropertyDescription prop = (RegistrationPropertyDescription)table.getRowData();

      selectedProp = prop.getNameAsString();
   }

   private HtmlDataTable getParentDataTable(UIComponent component)
   {
      if (component == null)
      {
         return null;
      }
      if (component instanceof HtmlDataTable)
      {
         return (HtmlDataTable)component;
      }
      return getParentDataTable(component.getParent());
   }

   protected String getObjectTypeName()
   {
      return null; // default implementation as not used
   }

   public boolean isAlreadyExisting(String objectName)
   {
      return false; // default implementation as not used
   }

   private LocalProducerConfiguration getLocalConfiguration()
   {
      if (localProducerConfiguration == null)
      {
         localProducerConfiguration = new LocalProducerConfiguration();
         ProducerConfiguration configuration = getConfiguration();
         localProducerConfiguration.initFrom(configuration.getRegistrationRequirements(), configuration.isUsingStrictMode());
      }

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

   private static class LocalProducerConfiguration
   {
      private List<RegistrationPropertyDescription> registrationProperties;
      private ProducerRegistrationRequirements registrationRequirements;
      private boolean strictMode;

      public void initFrom(ProducerRegistrationRequirements registrationRequirements, boolean usingStrictMode)
      {
         this.registrationRequirements = new ProducerRegistrationRequirementsImpl(registrationRequirements);

         Map<QName, RegistrationPropertyDescription> descriptions = registrationRequirements.getRegistrationProperties();
         registrationProperties = new LinkedList<RegistrationPropertyDescription>(descriptions.values());
         Collections.sort(registrationProperties);

         this.strictMode = usingStrictMode;
      }

      public boolean isRegistrationRequiredForFullDescription()
      {
         return registrationRequirements.isRegistrationRequiredForFullDescription();
      }

      public void setRegistrationRequiredForFullDescription(boolean requireRegForFullDescription)
      {
         registrationRequirements.setRegistrationRequiredForFullDescription(requireRegForFullDescription);
      }

      public boolean isRegistrationRequired()
      {
         return registrationRequirements.isRegistrationRequired();
      }

      public void setRegistrationRequired(boolean requireRegistration)
      {
         registrationRequirements.setRegistrationRequired(requireRegistration);
      }

      public RegistrationPolicy getPolicy()
      {
         return registrationRequirements.getPolicy();
      }

      public List<RegistrationPropertyDescription> getRegistrationProperties()
      {
         return registrationProperties;
      }

      public void addEmptyRegistrationProperty(String propertyName)
      {
         RegistrationPropertyDescription prop = registrationRequirements.addEmptyRegistrationProperty(propertyName);

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
         RegistrationPropertyDescription prop = registrationRequirements.removeRegistrationProperty(propertyName);

         registrationProperties.remove(prop);
      }

      public ProducerRegistrationRequirements getRegistrationRequirements()
      {
         return registrationRequirements;
      }

      public boolean isUsingStrictMode()
      {
         return strictMode;
      }

      public void setUsingStrictMode(boolean usingStrictMode)
      {
         this.strictMode = usingStrictMode;
      }
   }
}
