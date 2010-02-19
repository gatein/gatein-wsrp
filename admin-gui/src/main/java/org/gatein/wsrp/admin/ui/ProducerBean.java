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

package org.gatein.wsrp.admin.ui;

import org.gatein.registration.RegistrationPolicy;
import org.gatein.registration.policies.DefaultRegistrationPolicy;
import org.gatein.wsrp.producer.config.ProducerConfiguration;
import org.gatein.wsrp.producer.config.ProducerConfigurationService;
import org.gatein.wsrp.producer.config.ProducerRegistrationRequirements;
import org.gatein.wsrp.registration.RegistrationPropertyDescription;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 12854 $
 * @since 2.6.3
 */
public class ProducerBean extends ManagedBean
{
   private ProducerConfigurationService configurationService;
   private String policyClassName;
   private String validatorClassName;
   private static final String PROPERTY = "property";
   private static final String PRODUCER = "producer";
   private String selectedProp;

   public ProducerConfigurationService getConfigurationService()
   {
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
      return getRegRequirements().isRegistrationRequiredForFullDescription();
   }

   private ProducerRegistrationRequirements getRegRequirements()
   {
      return getConfiguration().getRegistrationRequirements();
   }

   public void setRegistrationRequiredForFullDescription(boolean requireRegForFullDescription)
   {
      getRegRequirements().setRegistrationRequiredForFullDescription(requireRegForFullDescription);
   }

   public boolean isRegistrationRequired()
   {
      return getRegRequirements().isRegistrationRequired();
   }

   public void setRegistrationRequired(boolean requireRegistration)
   {
      getRegRequirements().setRegistrationRequired(requireRegistration);
   }

   public String getRegistrationPolicyClassName()
   {
      RegistrationPolicy policy = getRegRequirements().getPolicy();
      if (policy != null)
      {
         return policy.getClass().getName();
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
         return ((DefaultRegistrationPolicy)getRegRequirements().getPolicy()).getValidator().getClass().getName();
      }
      throw new IllegalStateException("getValidatorClassName shouldn't be called if we're not using the default registration");
   }

   public void setValidatorClassName(String className)
   {
      validatorClassName = className;
   }

   public boolean isStrictMode()
   {
      return getConfiguration().isUsingStrictMode();
   }

   public void setStrictMode(boolean strictMode)
   {
      getConfiguration().setUsingStrictMode(strictMode);
   }

   public List<RegistrationPropertyDescription> getRegistrationProperties()
   {
      Map descriptions = getRegRequirements().getRegistrationProperties();
      Comparator<RegistrationPropertyDescription> descComparator = new Comparator<RegistrationPropertyDescription>()
      {
         public int compare(RegistrationPropertyDescription o1, RegistrationPropertyDescription o2)
         {
            return o1.getName().toString().compareTo(o2.getName().toString());
         }
      };

      List<RegistrationPropertyDescription> result = new ArrayList<RegistrationPropertyDescription>(descriptions.values());
      Collections.sort(result, descComparator);
      return result;
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
         if (!ProducerRegistrationRequirements.DEFAULT_POLICY_CLASS_NAME.equals(policyClassName))
         {
            getRegRequirements().reloadPolicyFrom(policyClassName, validatorClassName);
         }
         getConfigurationService().saveConfiguration();
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
      getRegRequirements().addEmptyRegistrationProperty(PROPERTY + System.currentTimeMillis());
      return PRODUCER;
   }

   public String deleteRegistrationProperty()
   {
      getRegRequirements().removeRegistrationProperty(selectedProp);
      return PRODUCER;
   }

   public void requireRegistrationListener(ValueChangeEvent event)
   {
      setRegistrationRequired((Boolean)event.getNewValue());

      // bypass the rest of the life cycle and re-display page
      FacesContext.getCurrentInstance().renderResponse();
   }

   public void selectProperty(ActionEvent event)
   {
      selectedProp = beanContext.getParameter("propName");
   }

   protected String getObjectTypeName()
   {
      return null; // default implementation as not used
   }

   public boolean isAlreadyExisting(String objectName)
   {
      return false; // default implementation as not used
   }
}
