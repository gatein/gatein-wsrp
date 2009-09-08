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

import org.gatein.common.util.ParameterValidation;
import org.gatein.wsrp.WSRPConstants;
import org.gatein.wsrp.WSRPUtils;
import org.gatein.wsrp.producer.config.impl.ProducerConfigurationImpl;
import org.gatein.wsrp.producer.config.impl.ProducerRegistrationRequirementsImpl;
import org.gatein.wsrp.registration.LocalizedString;
import org.gatein.wsrp.registration.RegistrationPropertyDescription;
import org.jboss.util.StringPropertyReplacer;
import org.jboss.xb.binding.ObjectModelFactory;
import org.jboss.xb.binding.UnmarshallingContext;
import org.xml.sax.Attributes;

import javax.xml.namespace.QName;
import java.util.Locale;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 10408 $
 * @since 2.6
 */
public class ProducerConfigurationFactory implements ObjectModelFactory
{
   private static final String REGISTRATION_CONFIG = "registration-configuration";
   private static final boolean DEBUG = false;
   private boolean registrationDone = false;

   // kept to support old-style type
   private static final String LEGACY_XSD_STRING = "xsd:string";

   public Object newRoot(Object root, UnmarshallingContext nav, String nsURI, String localName, Attributes attrs)
   {
      if (DEBUG)
      {
         System.out.println("newRoot " + localName);
      }

      if ("producer-configuration".equals(localName))
      {
         ProducerConfiguration producerConfiguration = new ProducerConfigurationImpl();
         producerConfiguration.setUsingStrictMode(getBooleanAttributeValue(attrs, "useStrictMode", true));
         return producerConfiguration;
      }
      throw new IllegalArgumentException("The processed file doesn't seem to have the proper format, " +
         "was expecting producer-configuration, instead got " + localName);
   }

   public Object newChild(ProducerConfigurationImpl producerConfiguration, UnmarshallingContext nav, String nsURI, String localName, Attributes attrs)
   {
      if (DEBUG)
      {
         System.out.println("newChild prodConf " + localName);
      }

      if (REGISTRATION_CONFIG.equals(localName))
      {
         // check that we don't have several registration-configuration elements.
         if (registrationDone)
         {
            throw new IllegalArgumentException("Only one registration-configuration element can be defined!");
         }

         ProducerRegistrationRequirements registrationRequirements = new ProducerRegistrationRequirementsImpl();
         registrationRequirements.setRegistrationRequired(true);
         registrationRequirements.setRegistrationRequiredForFullDescription(getBooleanAttributeValue(attrs, "fullServiceDescriptionRequiresRegistration", false));
         registrationDone = true;
         return registrationRequirements;
      }
      return null;
   }

   public Object completeRoot(Object root, UnmarshallingContext nav, String nsURI, String localName)
   {
      return root;
   }

   public Object newChild(ProducerRegistrationRequirementsImpl regReq, UnmarshallingContext nav, String nsURI,
                          String localName, Attributes attrs)
   {
      if (DEBUG)
      {
         System.out.println("newChild regReq " + localName);
      }

      if ("registration-property-description".equals(localName))
      {
         return new RegistrationPropertyDescription();
      }
      else
      {
         return null;
      }
   }

   public void setValue(ProducerRegistrationRequirementsImpl regReq, UnmarshallingContext nav, String nsURI,
                        String localName, String value)
   {
      if ("registration-policy".equals(localName))
      {
         value = StringPropertyReplacer.replaceProperties(value);
         regReq.setPolicyClassName(value);
      }
      else if ("registration-property-validator".equals(localName))
      {
         value = StringPropertyReplacer.replaceProperties(value);
         regReq.setValidatorClassName(value);
      }
   }

   public void setValue(RegistrationPropertyDescription desc, UnmarshallingContext nav, String nsURI,
                        String localName, String value)
   {
      if (DEBUG)
      {
         System.out.println("setvalue desc " + localName);
      }

      if ("name".equals(localName))
      {
         value = StringPropertyReplacer.replaceProperties(value);
         desc.setName(new QName(value));
      }
      else if ("type".equals(localName))
      {
         value = StringPropertyReplacer.replaceProperties(value);

         // first check that we still support type as "xsd:string"...
         if (!LEGACY_XSD_STRING.equals(value))
         {
            QName type = nav.resolveQName(value);
            if (!WSRPConstants.XSD_STRING.equals(type))
            {
               throw new IllegalArgumentException("'" + value + "' is not a supported type. Currently, only 'xsd:string' is supported.");
            }
         }

         desc.setType(WSRPConstants.XSD_STRING);
      }
   }

   public Object newChild(RegistrationPropertyDescription desc, UnmarshallingContext nav, String nsURI, String localName,
                          Attributes attrs)
   {
      if (DEBUG)
      {
         System.out.println("newchild desc " + localName);
      }

      if ("hint".equals(localName) || "label".equals(localName) || "description".equals(localName))
      {
         String lang = attrs.getValue("xml:lang");
         ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(lang, "xml:lang", localName);
         Locale locale = WSRPUtils.getLocale(lang);

         String resourceName = attrs.getValue("resourceName");

         LocalizedString string = new LocalizedString();
         string.setLocale(locale);

         if (resourceName != null && resourceName.length() > 0)
         {
            string.setResourceName(resourceName);
         }

         return string;
      }

      return null;
   }

   public void setValue(LocalizedString string, UnmarshallingContext nav, String nsURI, String localName, String value)
   {
      if (DEBUG)
      {
         System.out.println("setvalue string " + localName);
      }

      value = StringPropertyReplacer.replaceProperties(value);
      string.setValue(value);
   }

   public void addChild(ProducerConfigurationImpl conf, ProducerRegistrationRequirementsImpl regReq,
                        UnmarshallingContext nav, String nsURI, String localName)
   {
      if (DEBUG)
      {
         System.out.println("addchild conf regReq " + localName);
      }

      if (!ProducerRegistrationRequirementsImpl.DEFAULT_POLICY_CLASS_NAME.equals(regReq.getPolicyClassName()) && regReq.getValidatorClassName() != null)
      {
         throw new IllegalStateException("Doesn't make sense to define a property validator without using DefaultRegistrationPolicy!");
      }

      conf.setRegistrationRequirements(regReq);
   }

   public void addChild(ProducerRegistrationRequirementsImpl regReq, RegistrationPropertyDescription desc,
                        UnmarshallingContext nav, String nsURI, String localName)
   {
      if (DEBUG)
      {
         System.out.println("addchild regreq desc " + localName);
      }

      regReq.addRegistrationProperty(desc);
   }

   public void addChild(RegistrationPropertyDescription desc, LocalizedString string, UnmarshallingContext nav,
                        String nsURI, String localName)
   {
      if (DEBUG)
      {
         System.out.println("addchild desc string " + localName);
      }

      if ("hint".equals(localName))
      {
         desc.setHint(string);
      }
      else if ("label".equals(localName))
      {
         desc.setLabel(string);
      }
      else if ("description".equals(localName))
      {
         desc.setDescription(string);
      }
   }

   private boolean getBooleanAttributeValue(Attributes attrs, String attributeName, boolean defaultValue)
   {
      String value = attrs.getValue(attributeName);

      // figure out which is the default value
      String defaultString = "false";
      String other = "true";
      if (defaultValue)
      {
         defaultString = "true";
         other = "false";
      }

      if (other.equals(value))
      {
         return !defaultValue;
      }
      else if (value == null || defaultString.equals(value))
      {
         return defaultValue;
      }
      else
      {
         throw new IllegalArgumentException("Invalid value for " + attributeName + " attribute. Acceptable values are: true, false.");
      }
   }
}
