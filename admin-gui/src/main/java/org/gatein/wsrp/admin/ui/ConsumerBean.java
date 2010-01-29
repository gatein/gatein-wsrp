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

import org.exoplatform.container.ExoContainerContext;
import org.gatein.wsrp.WSRPConsumer;
import org.gatein.wsrp.consumer.EndpointConfigurationInfo;
import org.gatein.wsrp.consumer.ProducerInfo;
import org.gatein.wsrp.consumer.RegistrationInfo;
import org.gatein.wsrp.consumer.RegistrationProperty;
import org.gatein.wsrp.consumer.registry.ConsumerRegistry;

import javax.faces.event.ValueChangeEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 12865 $
 * @since 2.6
 */
public class ConsumerBean extends ManagedBean
{
   private WSRPConsumer consumer;
   private ConsumerRegistry registry;
   private ConsumerManagerBean manager;
   private boolean modified;
   private boolean registrationLocallyModified;

   private String wsdl;

   private transient RegistrationInfo expectedRegistrationInfo;
   private static final String CANNOT_FIND_CONSUMER = "bean_consumer_cannot_find_consumer";
   private static final String CANNOT_UPDATE_CONSUMER = "bean_consumer_cannot_update_consumer";
   private static final String CANNOT_REFRESH_CONSUMER = "bean_consumer_cannot_refresh_consumer";
   private static final String MODIFY_REG_SUCCESS = "bean_consumer_modify_reg_success";
   private static final String INVALID_MODIFY = "bean_consumer_invalid_modify";
   private static final String CANNOT_MODIFY_REG = "bean_consumer_cannot_modify_reg";
   private static final String CANNOT_ERASE_REG = "bean_consumer_cannot_erase_reg";
   private static final String MALFORMED_URL = "bean_consumer_malformed_url";
   private static final String UPDATE_SUCCESS = "bean_consumer_update_success";
   private static final String CONSUMER_TYPE = "CONSUMER_TYPE";

   public void setRegistry(ConsumerRegistry registry)
   {
      this.registry = registry;
   }

   public void setManager(ConsumerManagerBean manager)
   {
      this.manager = manager;
   }

   public boolean isModified()
   {
      return modified || getProducerInfo().isModifyRegistrationRequired() || isRegistrationLocallyModified();
   }

   public boolean isRefreshNeeded()
   {
      return consumer.isRefreshNeeded();
   }

   public String getId()
   {
      return consumer.getProducerId();
   }

   public void setId(String id)
   {
      if (consumer != null)
      {
         // renaming scenario
         ProducerInfo info = getProducerInfo();
         String oldId = info.getId();

         // need to check that the new id is valid
         if (isOldAndNewDifferent(oldId, id))
         {
            id = checkNameValidity(id, "edit-cons-form:id");
            if (id != null)
            {
               info.setId(id);

               // properly update the registry after change of id
               getRegistry().updateProducerInfo(info);

               // we're not using modifyIfNeeded here to avoid double equality check, so we need to set modified manually
               modified = true;
            }
         }
      }
      else
      {
         // initialization scenario
         consumer = getRegistry().getConsumer(id);
         if (consumer != null)
         {
            EndpointConfigurationInfo endpoint = getProducerInfo().getEndpointConfigurationInfo();
            wsdl = endpoint.getWsdlDefinitionURL();
         }
         else
         {
            beanContext.createErrorMessage(CANNOT_FIND_CONSUMER, id);
         }
      }
   }

   public Integer getCache()
   {
      return getProducerInfo().getExpirationCacheSeconds();
   }

   public void setCache(Integer cache)
   {
      getProducerInfo().setExpirationCacheSeconds((Integer)modifyIfNeeded(getCache(), cache, "cache", false));
   }

   public String getWsdl()
   {
      return wsdl;
   }

   public void setWsdl(String wsdlURL)
   {
      wsdl = (String)modifyIfNeeded(wsdl, wsdlURL, "wsdl", true);
   }

   private void internalSetWsdl(String wsdlURL)
   {
      try
      {
         getProducerInfo().getEndpointConfigurationInfo().setWsdlDefinitionURL(wsdlURL);
      }
      catch (Exception e)
      {
         getRegistry().deactivateConsumerWith(getId());
         beanContext.createErrorMessageFrom("wsdl", e);
      }
   }

   public boolean isActive()
   {
      return consumer.isActive();
   }

   public boolean isRegistered()
   {
      return getProducerInfo().isRegistered();
   }

   public boolean isRegistrationRequired()
   {
      return getProducerInfo().isRegistrationRequired();
   }

   public boolean isRegistrationCheckNeeded()
   {
      ProducerInfo info = getProducerInfo();
      if (info.isRefreshNeeded(true))
      {
         RegistrationInfo regInfo = info.getRegistrationInfo();
         if (regInfo == null)
         {
            return true;
         }
         else
         {
            Boolean consistent = regInfo.isConsistentWithProducerExpectations();
            return consistent == null || !consistent.booleanValue();
         }
      }
      else
      {
         return false;
      }
   }

   public boolean isRegistrationModified()
   {
      return getProducerInfo().isModifyRegistrationRequired();
   }

   public boolean isRegistrationLocallyModified()
   {
      return isRegistered() && registrationLocallyModified;
   }

   public boolean isRegistrationChecked()
   {
      return getProducerInfo().isRegistrationChecked();
   }

   public boolean isRegistrationValid()
   {
      if (isRegistrationChecked())
      {
         return getProducerInfo().getRegistrationInfo().isRegistrationValid().booleanValue();
      }
      throw new IllegalStateException("Need to check the registration before determining if it's valid!");
   }

   public ProducerInfo getProducerInfo()
   {
      return consumer.getProducerInfo();
   }

   public boolean isLocalInfoPresent()
   {
      return getProducerInfo().hasLocalRegistrationInfo();
   }

   public boolean isRegistrationPropertiesEmpty()
   {
      RegistrationInfo regInfo = getProducerInfo().getRegistrationInfo();
      return regInfo == null || regInfo.isRegistrationPropertiesEmpty();
   }

   public boolean isExpectedRegistrationPropertiesEmpty()
   {
      RegistrationInfo info = getExpectedRegistrationInfo();
      if (info != null)
      {
         return info.isRegistrationPropertiesEmpty();
      }
      else
      {
         return true;
      }
   }

   private RegistrationInfo getExpectedRegistrationInfo()
   {
      if (expectedRegistrationInfo == null)
      {
         expectedRegistrationInfo = beanContext.getFromSession(ConsumerManagerBean.EXPECTED_REG_INFO_KEY, RegistrationInfo.class);
      }

      return expectedRegistrationInfo;
   }

   public List<RegistrationProperty> getRegistrationProperties()
   {
      return getSortedProperties(getProducerInfo().getRegistrationInfo());
   }

   public List<RegistrationProperty> getExpectedRegistrationProperties()
   {
      return getSortedProperties(getExpectedRegistrationInfo());
   }

   private List<RegistrationProperty> getSortedProperties(RegistrationInfo registrationInfo)
   {
      if (registrationInfo != null)
      {
         LinkedList<RegistrationProperty> list = new LinkedList<RegistrationProperty>(registrationInfo.getRegistrationProperties().values());
         Collections.sort(list);
         return list;
      }
      else
      {
         return Collections.emptyList();
      }
   }

   // Actions

   public String update()
   {
      return internalUpdate(true);
   }

   private String internalUpdate(boolean showMessage)
   {
      if (consumer != null)
      {
         if (isModified())
         {
            try
            {
               // update values
               ProducerInfo prodInfo = getProducerInfo();
               EndpointConfigurationInfo endpointInfo = prodInfo.getEndpointConfigurationInfo();
               internalSetWsdl(wsdl);

               saveToRegistry(prodInfo);
            }
            catch (Exception e)
            {
               beanContext.createErrorMessageFrom(e);
               return null;
            }
         }

         if (showMessage)
         {
            beanContext.createInfoMessage(UPDATE_SUCCESS);
         }
         return ConsumerManagerBean.CONFIGURE_CONSUMER;
      }

      beanContext.createErrorMessage(CANNOT_UPDATE_CONSUMER);
      return null;
   }

   private void saveToRegistry(ProducerInfo prodInfo)
   {
      getRegistry().updateProducerInfo(prodInfo);
      modified = false;
   }

   public String refreshConsumer()
   {
      if (consumer != null)
      {
         if (isModified())
         {
            String updateResult = internalUpdate(false);
            if (updateResult == null)
            {
               return null;
            }
         }

         // if the registration is locally modified, bypass the refresh as it will not yield a proper result
         if (!isRegistrationLocallyModified())
         {
            manager.refresh(consumer);
         }
         else
         {
            beanContext.createInfoMessage(ConsumerManagerBean.REFRESH_MODIFY);
         }

         return ConsumerManagerBean.CONFIGURE_CONSUMER;
      }

      beanContext.createErrorMessage(CANNOT_REFRESH_CONSUMER);
      return null;
   }

   public String modifyRegistration()
   {
      if (consumer != null)
      {
         ProducerInfo info = getProducerInfo();
         if (isModified())
         {
            // get updated registration info
            RegistrationInfo newReg = getExpectedRegistrationInfo();

            // make sure we save any modified registration properties
            saveToRegistry(info);

            // save old info in case something goes wrong
            RegistrationInfo oldReg = getProducerInfo().getRegistrationInfo();

            // check that we have the proper state
            if (newReg == null)
            {
               // if we want to change an existing registration property (for example, to upgrade service) then there are
               // no expected information, we're just using the modified local version
               newReg = new RegistrationInfo(oldReg);

               if (!isRegistrationLocallyModified())
               {
                  IllegalStateException e =
                     new IllegalStateException("Registration not locally modified: there should be expected registration from producer!");
                  log.debug(e);
                  throw e;
               }
            }

            try
            {
               // todo: this should be done better cf regPropListener
               newReg.setModifiedSinceLastRefresh(true); // mark as modified to force refresh of RegistrationData
               // attempt to modify the registration using new registration info
               info.setRegistrationInfo(newReg);
               info.modifyRegistration();
               newReg.setModifiedSinceLastRefresh(false);

               registrationLocallyModified = false;

               beanContext.createInfoMessage(MODIFY_REG_SUCCESS);
            }
            catch (Exception e)
            {
               // restore old info
               info.setRegistrationInfo(oldReg);

               beanContext.createErrorMessageFrom(e);
               return null;
            }

            refreshConsumer();
            return null;
         }
         else
         {
            beanContext.createErrorMessage(INVALID_MODIFY);
         }
      }

      beanContext.createErrorMessage(CANNOT_MODIFY_REG);
      return null;
   }

   public String eraseLocalRegistration()
   {
      if (consumer != null)
      {
         getProducerInfo().eraseRegistrationInfo();
         return ConsumerManagerBean.CONFIGURE_CONSUMER;
      }

      beanContext.createErrorMessage(CANNOT_ERASE_REG);
      return null;
   }

   private Object modifyIfNeeded(Object oldValue, Object newValue, String target, boolean checkURL)
   {
      if (isOldAndNewDifferent(oldValue, newValue))
      {
         if (checkURL)
         {
            try
            {
               // check that the new value is a valid URL
               new URL(newValue.toString());
            }
            catch (MalformedURLException e)
            {
               beanContext.createTargetedErrorMessage(target, MALFORMED_URL, newValue, e.getLocalizedMessage());
            }
         }

         oldValue = newValue;
         modified = true;
      }

      return oldValue;
   }

   // Listeners

   // todo: valueChangeListener not needed anymore when events on RegistrationProperties work  

   public void regPropListener(ValueChangeEvent event)
   {
      if (!registrationLocallyModified)
      {
         // only mark as locally modified if we had a previous value
         Object oldValue = normalizeStringIfNeeded(event.getOldValue());
         if (oldValue != null)
         {
            registrationLocallyModified = isOldAndNewDifferent(oldValue, event.getNewValue());
         }
      }
   }

   protected String getObjectTypeName()
   {
      return CONSUMER_TYPE;
   }

   public boolean isAlreadyExisting(String objectName)
   {
      return getRegistry().getConsumer(objectName) != null;
   }

   public ConsumerRegistry getRegistry()
   {
      if (registry == null)
      {
         registry = (ConsumerRegistry)ExoContainerContext.getCurrentContainer().getComponentInstanceOfType(ConsumerRegistry.class);
      }
      return registry;
   }
}
