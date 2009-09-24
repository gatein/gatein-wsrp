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

package org.gatein.wsrp.producer.config;

import org.gatein.registration.RegistrationPolicy;
import org.gatein.registration.policies.DefaultRegistrationPolicy;
import org.gatein.wsrp.WSRPUtils;
import org.gatein.wsrp.producer.config.impl.ProducerConfigurationImpl;
import org.gatein.wsrp.producer.config.impl.ProducerRegistrationRequirementsImpl;
import org.gatein.wsrp.registration.LocalizedString;
import org.gatein.wsrp.registration.RegistrationPropertyDescription;
import org.jboss.xb.binding.MarshallingContext;
import org.jboss.xb.binding.ObjectModelProvider;

/**
 * Used to marshall Producer configuration to XML via JBoss XB.
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 10408 $
 * @since 2.6.3
 */
public class ProducerConfigurationProvider implements ObjectModelProvider
{
   public Object getRoot(Object o, MarshallingContext marshallingContext, String s, String s1)
   {
      return o;
   }

   public Object getChildren(ProducerConfigurationImpl configuration, String namespaceUri, String localName)
   {
      if ("registration-configuration".equals(localName))
      {
         ProducerRegistrationRequirements registrationRequirements = configuration.getRegistrationRequirements();
         if (registrationRequirements != null && registrationRequirements.isRegistrationRequired())
         {
            return registrationRequirements;
         }
      }
      else if ("producer-configuration".equals(localName))
      {
         return configuration;
      }

      return null;
   }

   public Object getChildren(ProducerRegistrationRequirementsImpl regReq, String namespaceUri, String localName)
   {
      if ("registration-property-description".equals(localName))
      {
         return regReq.getRegistrationProperties().values();
      }

      return null;
   }

   public Object getAttributeValue(ProducerConfigurationImpl configuration, String namespaceUri, String localName)
   {
      if ("useStrictMode".equals(localName))
      {
         return configuration.isUsingStrictMode();
      }

      return null;
   }

   public Object getAttributeValue(ProducerRegistrationRequirementsImpl regReq, String namespaceUri, String localName)
   {
      if ("fullServiceDescriptionRequiresRegistration".equals(localName))
      {
         return regReq.isRegistrationRequiredForFullDescription();
      }

      return null;
   }

   public Object getElementValue(ProducerRegistrationRequirementsImpl regReq, String namespaceUri, String localName)
   {
      Object value = null;
      if ("registration-policy".equals(localName))
      {
         RegistrationPolicy policy = regReq.getPolicy();
         if (policy != null)
         {
            value = policy.getClass().getName();
         }
      }
      else if ("registration-property-validator".equals(localName))
      {
         RegistrationPolicy policy = regReq.getPolicy();
         if (policy instanceof DefaultRegistrationPolicy)
         {
            DefaultRegistrationPolicy defaultRegistrationPolicy = (DefaultRegistrationPolicy)policy;
            value = defaultRegistrationPolicy.getValidator().getClass().getName();
         }
      }
      else
      {
         value = null;
      }
      return value;
   }

   public Object getElementValue(RegistrationPropertyDescription propertyDescription, String namespaceUri, String localName)
   {
      Object value = null;
      if ("name".equals(localName))
      {
         value = propertyDescription.getName();
      }
      else if ("type".equals(localName))
      {
         value = propertyDescription.getType();
      }
      else if ("label".equals(localName))
      {
         value = getLocalizedStringOrNull(propertyDescription.getLabel());
      }
      else if ("hint".equals(localName))
      {
         value = getLocalizedStringOrNull(propertyDescription.getHint());
      }
      else if ("description".equals(localName))
      {
         value = getLocalizedStringOrNull(propertyDescription.getDescription());
      }
      return value;
   }

   private LocalizedString getLocalizedStringOrNull(LocalizedString string)
   {
      if (string != null)
      {
         String value = string.getValue();
         if (value == null || value.length() == 0)
         {
            return null;
         }
         else
         {
            return string;
         }
      }
      else
      {
         return null;
      }
   }

   public Object getAttributeValue(LocalizedString localizedString, String namespaceUri, String localName)
   {
      Object value = null;
      if ("lang".equals(localName))
      {
         value = WSRPUtils.toString(localizedString.getLocale());
      }
      else if ("resourceName".equals(localName))
      {
         value = localizedString.getResourceName();
      }
      return value;
   }

   public Object getElementValue(LocalizedString localizedString, String namespaceUri, String localName)
   {
      return localizedString.getValue();
   }
}
