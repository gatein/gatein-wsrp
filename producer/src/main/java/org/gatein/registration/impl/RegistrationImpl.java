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

package org.gatein.registration.impl;

import org.gatein.common.util.ParameterValidation;
import org.gatein.pc.api.PortletContext;
import org.gatein.registration.Registration;
import org.gatein.registration.RegistrationException;
import org.gatein.registration.RegistrationPersistenceManager;
import org.gatein.registration.RegistrationStatus;
import org.gatein.registration.spi.ConsumerSPI;
import org.gatein.registration.spi.RegistrationSPI;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 8784 $
 * @since 2.6
 */
public class RegistrationImpl implements RegistrationSPI
{

   private String key;
   private ConsumerSPI consumer;
   private RegistrationStatus status;
   private Map<QName, Object> properties;
   private String registrationHandle;
   private Set<PortletContext> portletContexts;
   private transient RegistrationPersistenceManager manager;


   RegistrationImpl(ConsumerSPI consumer, RegistrationStatus status, Map<QName, Object> properties, RegistrationPersistenceManager manager)
   {
      this.consumer = consumer;
      this.status = status;
      this.properties = new HashMap<QName, Object>(properties);
      portletContexts = new HashSet<PortletContext>();
      this.manager = manager;
   }

   public String getPersistentKey()
   {
      return key;
   }

   public void setPersistentKey(String key)
   {
      this.key = key;
   }

   public void setRegistrationHandle(String handle)
   {
      this.registrationHandle = handle;
   }

   public String getRegistrationHandle()
   {
      return registrationHandle;
   }

   public ConsumerSPI getConsumer()
   {
      return consumer;
   }

   public void addPortletContext(PortletContext portletContext) throws RegistrationException
   {
      addPortletContext(portletContext, true);
   }

   public void addPortletContext(PortletContext portletContext, boolean needsSaving) throws RegistrationException
   {
      portletContexts.add(portletContext);
      if (needsSaving)
      {
         manager.saveChangesTo(this);
      }
   }

   public void removePortletContext(PortletContext portletContext) throws RegistrationException
   {
      removePortletContext(portletContext, true);
   }

   public void removePortletContext(PortletContext portletContext, boolean needsSaving) throws RegistrationException
   {
      portletContexts.remove(portletContext);
      manager.saveChangesTo(this);
   }

   public Map<QName, Object> getProperties()
   {
      return Collections.unmodifiableMap(properties);
   }

   public void setPropertyValueFor(QName propertyName, Object value)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(propertyName, "Property name");
      ParameterValidation.throwIllegalArgExceptionIfNull(value, "Property value");

      // avoid modifying the properties if new value is the same as old one
      Object oldValue = properties.get(propertyName);
      if (!value.equals(oldValue))
      {
         properties.put(propertyName, value);
      }
   }

   public void setPropertyValueFor(String propertyName, Object value)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(propertyName, "Property name");
      setPropertyValueFor(new QName(propertyName), value);
   }

   public Object getPropertyValueFor(QName propertyName)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(propertyName, "Property name");
      return properties.get(propertyName);
   }

   public Object getPropertyValueFor(String propertyName)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(propertyName, "Property name");
      return getPropertyValueFor(new QName(propertyName));
   }


   public void removeProperty(QName propertyName)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(propertyName, "Property name");
      properties.remove(propertyName);
   }

   public void removeProperty(String propertyName)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(propertyName, "Property name");
      removeProperty(new QName(propertyName));
   }

   public boolean hasEqualProperties(Registration registration)
   {
      if (registration == null)
      {
         return false;
      }

      Map other = registration.getProperties();
      return hasEqualProperties(other);
   }

   public boolean hasEqualProperties(Map registrationProperties)
   {
      if (registrationProperties == null)
      {
         return false;
      }

      if (properties.size() != registrationProperties.size())
      {
         return false;
      }

      // check properties
      for (Map.Entry<QName, Object> entry : properties.entrySet())
      {
         // we should have a 1-1 match between name/value pair
         QName name = entry.getKey();
         if (!entry.getValue().equals(registrationProperties.get(name)))
         {
            return false;
         }
      }

      return true;
   }

   public void setRegistrationPropertyValueFor(String propertyName, Object value)
   {
      setPropertyValueFor(new QName(propertyName), value);
   }

   public RegistrationStatus getStatus()
   {
      return status;
   }

   public void setStatus(RegistrationStatus status)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(status, "RegistrationStatus");
      this.status = status;
   }

   public void updateProperties(Map registrationProperties)
   {
      properties = new HashMap(registrationProperties);
   }

   public boolean knows(PortletContext portletContext)
   {
      return portletContexts.contains(portletContext);
   }

   public boolean knows(String portletContextId)
   {
      return knows(PortletContext.createPortletContext(portletContextId));
   }

   public Set<PortletContext> getKnownPortletContexts()
   {
      return Collections.unmodifiableSet(portletContexts);
   }
}
