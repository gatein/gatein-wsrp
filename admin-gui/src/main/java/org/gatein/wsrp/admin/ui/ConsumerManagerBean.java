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

import org.gatein.common.util.ParameterValidation;
import org.gatein.wsrp.WSRPConsumer;
import org.gatein.wsrp.consumer.ProducerInfo;
import org.gatein.wsrp.consumer.RefreshResult;
import org.gatein.wsrp.consumer.RegistrationInfo;
import org.gatein.wsrp.consumer.registry.ConsumerRegistry;

import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 12865 $
 * @since 2.6
 */
public class ConsumerManagerBean extends ManagedBean implements Serializable
{
   private transient ConsumerRegistry registry;
   private String selectedId;

   private static final String NO_CONSUMER = "bean_consumermanager_no_consumer";
   private static final String INVALID_NEW_CONSUMER_NAME = "bean_consumermanager_invalid_new_consumer_name";
   private static final String REFRESH_BYPASSED = "bean_consumermanager_refresh_bypassed";
   private static final String REFRESH_SUCCESS = "bean_consumermanager_refresh_success";
   private static final String REFRESH_FAILURE = "bean_consumermanager_refresh_failure";
   private static final String REFRESH_FAILURE_WSDL = "bean_consumermanager_refresh_failure_wsdl";
   private static final String REFRESH_EXCEPTION = "bean_consumermanager_refresh_exception";
   static final String CONFIGURE_CONSUMER = "configureConsumer";
   static final String EXPORT = "export";
   static final String EXPORTS = "exports";
   static final String EXPORT_DETAIL = "exportDetail";
   static final String IMPORT = "import";

   static final String CONSUMERS = "consumers";
   static final String EXPECTED_REG_INFO_KEY = "expectedRegistrationInfo";
   static final String REFRESH_MODIFY = "bean_consumermanager_refresh_modify";
   static final String REQUESTED_CONSUMER_ID = "id";
   static final String SESSION_CONSUMER_ID = "consumerId";
   private static final String MESSAGE_TARGET = "add-consumer:createConsumer:consumerName";

   public ConsumerRegistry getRegistry()
   {
      // if the registry is not set, get it from the application scope
      if (registry == null)
      {
         registry = beanContext.findBean("ConsumerRegistry", ConsumerRegistry.class);
      }

      return registry;
   }

   public void setRegistry(ConsumerRegistry registry)
   {
      this.registry = registry;
   }

   public void setSelectedId(String consumerId)
   {
      this.selectedId = consumerId;
   }

   public String getSelectedId()
   {
      return selectedId;
   }

   public WSRPConsumer getSelectedConsumer()
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(selectedId, "consumer id", null);
      return getRegistry().getConsumer(selectedId);
   }

   public boolean isConsumersEmpty()
   {
      return getRegistry().getConfiguredConsumerNumber() == 0;
   }

   public List<WSRPConsumer> getConsumers()
   {
      return getRegistry().getConfiguredConsumers();
   }

   public String reload()
   {
      getRegistry().reloadConsumers();
      return CONSUMERS;
   }

   public String activateConsumer()
   {
      if (refreshConsumerId() != null)
      {
         boolean activate = Boolean.valueOf(beanContext.getParameter("activate"));
         try
         {
            if (activate)
            {
               WSRPConsumer consumer = getSelectedConsumer();
               if (consumer.isRefreshNeeded())
               {
                  RefreshResult result = internalRefresh(consumer);
                  if (result != null && !result.hasIssues())
                  {
                     getRegistry().activateConsumerWith(selectedId);
                  }
               }
               else
               {
                  getRegistry().activateConsumerWith(selectedId);
               }
            }
            else
            {
               getRegistry().deactivateConsumerWith(selectedId);
            }
         }
         catch (Exception e)
         {
            beanContext.createErrorMessageFrom(e);
         }

         return listConsumers();
      }
      else
      {
         noSelectedConsumerError();
         return listConsumers();
      }
   }

   public String registerConsumer()
   {
      if (refreshConsumerId() != null)
      {
         boolean register = Boolean.valueOf(beanContext.getParameter("register"));

         try
         {
            getRegistry().registerOrDeregisterConsumerWith(selectedId, register);
            // show consumer configuration
            setConsumerIdInSession(false);
            return CONFIGURE_CONSUMER;
         }
         catch (Exception e)
         {
            beanContext.createErrorMessageFrom(e);
            return null;
         }
      }
      else
      {
         noSelectedConsumerError();
         return null;
      }
   }

   public String createConsumer()
   {
      selectedId = checkNameValidity(selectedId, MESSAGE_TARGET);
      if (selectedId != null)
      {
         try
         {
            getRegistry().createConsumer(selectedId, ProducerInfo.DEFAULT_CACHE_VALUE, null);
            setConsumerIdInSession(false);
            return CONFIGURE_CONSUMER;
         }
         catch (Exception e)
         {
            beanContext.createErrorMessageFrom(MESSAGE_TARGET, e);
            return null;
         }
      }

      return null;
   }

   public String destroyConsumer()
   {
      if (refreshConsumerId() != null)
      {
         try
         {
            getRegistry().destroyConsumer(selectedId);
            return listConsumers();
         }
         catch (Exception e)
         {
            beanContext.createErrorMessageFrom(e);
            return null;
         }
      }
      else
      {
         noSelectedConsumerError();
         return null;
      }
   }

   public String configureConsumer()
   {
      if (refreshConsumerId() != null)
      {
         setConsumerIdInSession(false);
         return CONFIGURE_CONSUMER;
      }
      else
      {
         noSelectedConsumerError();
         return null;
      }
   }

   public String refreshConsumer()
   {
      if (refreshConsumerId() != null)
      {
         internalRefresh(getSelectedConsumer());

         return configureConsumer();
      }
      else
      {
         noSelectedConsumerError();
         return null;
      }
   }

   public String importPortlets()
   {
      if (refreshConsumerId() != null)
      {
         setConsumerIdInSession(false);
         return EXPORTS;
      }
      else
      {
         noSelectedConsumerError();
         return null;
      }
   }

   public String exportPortlets()
   {
      if (refreshConsumerId() != null)
      {
         setConsumerIdInSession(false);
         return EXPORT;
      }
      else
      {
         noSelectedConsumerError();
         return null;
      }
   }

   private RefreshResult internalRefresh(WSRPConsumer consumer)
   {
      try
      {
         RefreshResult result = consumer.refresh(true);

         String statusMessage = getLocalizationKeyFrom(result);
         if (result.hasIssues())
         {
            // create the expected registration info and make it available
            RegistrationInfo expected = new RegistrationInfo(consumer.getProducerInfo().getRegistrationInfo());
            expected.refresh(result.getServiceDescription(), consumer.getProducerId(), true, true, true);
            setExpectedRegistrationInfo(expected);

            beanContext.createErrorMessage(statusMessage);

            // refresh had issues, we should deactivate this consumer
            getRegistry().deactivateConsumerWith(consumer.getProducerId());
         }
         else
         {
            // activate the consumer if it's supposed to be active
            if (consumer.isActive())
            {
               getRegistry().activateConsumerWith(consumer.getProducerId());
            }
            else
            {
               getRegistry().deactivateConsumerWith(consumer.getProducerId());
            }

            beanContext.createInfoMessage(statusMessage);
         }
         return result;
      }
      catch (Exception e)
      {
         beanContext.createErrorMessageFrom(e);
         return null;
      }
   }

   private String getLocalizationKeyFrom(RefreshResult result)
   {
      RefreshResult.Status status = result.getStatus();
      if (RefreshResult.Status.BYPASSED.equals(status))
      {
         return REFRESH_BYPASSED;
      }
      else if (RefreshResult.Status.SUCCESS.equals(status))
      {
         return REFRESH_SUCCESS;
      }
      else if (RefreshResult.Status.FAILURE.equals(status))
      {
         RefreshResult registrationResult = result.getRegistrationResult();
         if (registrationResult != null)
         {
            return REFRESH_FAILURE;
         }
         else
         {
            return REFRESH_FAILURE_WSDL;
         }
      }
      else if (RefreshResult.Status.MODIFY_REGISTRATION_REQUIRED.equals(status))
      {
         return REFRESH_MODIFY;
      }
      else
      {
         return REFRESH_EXCEPTION;
      }
   }

   RefreshResult refresh(WSRPConsumer consumer)
   {
      RefreshResult result = internalRefresh(consumer);

      selectedId = consumer.getProducerId();
      setConsumerIdInSession(false);
      return result;
   }

   private void setExpectedRegistrationInfo(RegistrationInfo expected)
   {
      Map<String, Object> sessionMap = FacesContext.getCurrentInstance().getExternalContext().getSessionMap();
      sessionMap.put(EXPECTED_REG_INFO_KEY, expected);
   }

   public String listConsumers()
   {
      setConsumerIdInSession(true);
      selectedId = null;
      return CONSUMERS;
   }

   public void selectConsumer(ActionEvent actionEvent)
   {
      refreshConsumerId();
      setConsumerIdInSession(false);
   }

   private String refreshConsumerId()
   {
      selectedId = beanContext.getParameter(REQUESTED_CONSUMER_ID);
      return selectedId;
   }

   private void setConsumerIdInSession(boolean remove)
   {
      Map<String, Object> sessionMap = beanContext.getSessionMap();
      String consumerBeanName = "consumer"; // must match ConsumerBean name in faces-config.xml

      // force recreation of ConsumerBean otherwise switching to the consumer view might not show the proper consumer 
      sessionMap.remove(consumerBeanName);

      if (!remove)
      {
         sessionMap.put(SESSION_CONSUMER_ID, selectedId);
      }
      else
      {
         sessionMap.remove(SESSION_CONSUMER_ID);
      }
   }

   private void noSelectedConsumerError()
   {
      beanContext.createErrorMessage(NO_CONSUMER);
   }

   protected String getObjectTypeName()
   {
      return "CONSUMER_TYPE";
   }

   public boolean isAlreadyExisting(String objectName)
   {
      return getRegistry().getConsumer(objectName) != null;
   }
}
