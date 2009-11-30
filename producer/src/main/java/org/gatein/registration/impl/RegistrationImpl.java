/*
 * JBoss, a division of Red Hat
 * Copyright 2009, Red Hat Middleware, LLC, and individual
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
import org.gatein.registration.Consumer;
import org.gatein.registration.Registration;
import org.gatein.registration.RegistrationStatus;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 8784 $
 * @since 2.6
 */
public class RegistrationImpl implements Registration
{

   private final String id;
   private ConsumerImpl consumer;
   private RegistrationStatus status;
   private Map<QName, Object> properties;
   private String registrationHandle;


   public RegistrationImpl(String id, ConsumerImpl consumer, RegistrationStatus status, Map properties)
   {
      this.id = id;
      this.consumer = consumer;
      this.status = status;
      this.properties = new HashMap<QName, Object>(properties);
   }

   public String getId()
   {
      return id;
   }

   public void setRegistrationHandle(String handle)
   {
      this.registrationHandle = handle;
   }

   public String getRegistrationHandle()
   {
      return registrationHandle;
   }

   public Consumer getConsumer()
   {
      return consumer;
   }

   public Map getProperties()
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

   public void clearAssociatedState()
   {
      //todo: implement
   }

   public void updateProperties(Map registrationProperties)
   {
      properties = new HashMap(registrationProperties);
   }

}
