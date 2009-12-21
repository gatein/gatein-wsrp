/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2006, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/
package org.gatein.wsrp.consumer.registry.xml;

import org.gatein.common.util.ParameterValidation;
import org.gatein.wsrp.WSRPConsumer;
import org.gatein.wsrp.consumer.EndpointConfigurationInfo;
import org.gatein.wsrp.consumer.ProducerInfo;
import org.gatein.wsrp.consumer.RegistrationInfo;
import org.gatein.wsrp.consumer.registry.ConsumerRegistry;
import org.jboss.util.StringPropertyReplacer;
import org.jboss.xb.binding.GenericObjectModelFactory;
import org.jboss.xb.binding.UnmarshallingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;

import java.util.SortedMap;
import java.util.TreeMap;

/**
 * Wire the mbeans to install
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 8784 $
 */
public class XMLWSRPConsumerFactory implements GenericObjectModelFactory
{
   private static final Logger log = LoggerFactory.getLogger(XMLWSRPConsumerFactory.class);

   private ConsumerRegistry consumerRegistry;

   private static final boolean DEBUG = false;

   /** . */
   private static final String WSRP_PRODUCER = "wsrp-producer";

   public static class Property
   {
      String name;
      String value;
      String lang;
   }

   public XMLWSRPConsumerFactory(ConsumerRegistry consumerRegistry)
   {
      this.consumerRegistry = consumerRegistry;
   }

   public Object newRoot(Object root, UnmarshallingContext nav, String nsURI, String localName, Attributes attrs)
   {
      return new TreeMap<String, WSRPConsumer>();
   }

   public Object completeRoot(Object root, UnmarshallingContext nav, String nsURI, String localName)
   {
      return root;
   }

   public Object newChild(Object parent, UnmarshallingContext unmarshallingContext, String nsURI, String localName, Attributes attributes)
   {
      if (parent instanceof SortedMap)
      {
         return newChild((SortedMap<String, WSRPConsumer>)parent, unmarshallingContext, nsURI, localName, attributes);
      }
      if (parent instanceof RegistrationInfo)
      {
         return newChild((RegistrationInfo)parent, unmarshallingContext, nsURI, localName, attributes);
      }
      if (parent instanceof WSRPConsumer)
      {
         return newChild((WSRPConsumer)parent, unmarshallingContext, nsURI, localName, attributes);
      }
      return null;
   }

   public void addChild(Object parent, Object child, UnmarshallingContext unmarshallingContext, String nsURI, String localName)
   {
      if (parent instanceof RegistrationInfo && child instanceof Property)
      {
         addChild((RegistrationInfo)parent, (Property)child, unmarshallingContext, nsURI, localName);
      }
      else if (parent instanceof SortedMap && child instanceof WSRPConsumer)
      {
         addChild((SortedMap<String, WSRPConsumer>)parent, (WSRPConsumer)child, unmarshallingContext, nsURI, localName);
      }
   }

   public void setValue(Object parent, UnmarshallingContext unmarshallingContext, String nsURI, String localName, String value)
   {
      if (parent instanceof EndpointConfigurationInfo)
      {
         setValue((EndpointConfigurationInfo)parent, unmarshallingContext, nsURI, localName, value);
      }
      else if (parent instanceof Property)
      {
         setValue((Property)parent, unmarshallingContext, nsURI, localName, value);
      }
      else if (parent instanceof RegistrationInfo)
      {
         setValue((RegistrationInfo)parent, unmarshallingContext, nsURI, localName, value);
      }
   }

   public Object newChild(SortedMap<String, WSRPConsumer> consumers, UnmarshallingContext nav, String nsURI, String localName,
                          Attributes attrs)
   {
      if (DEBUG)
      {
         System.out.println("newchild deployment " + localName);
      }

      if (WSRP_PRODUCER.equals(localName))
      {
         String id = attrs.getValue("id");
         ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(id, "producer identifier", "Configuring a producer");

         // check that the consumer doesn't exist in the database first
         WSRPConsumer consumer = consumerRegistry.getConsumer(id);
         if (consumer != null)
         {
            String message = "Added consumer for producer '" + id + "' with status: ";

            // if consumer is active, add it to the list of services 
            if (consumer.getProducerInfo().isActive())
            {
               consumers.put(id, consumer);
               message += "active";
            }
            else
            {
               message += "inactive";
            }

            log.info(message + " (loaded from database).");

            // consumer already exists, do not further process this producer and use the DB configuration instead
            return null;
         }

         String expirationCache = attrs.getValue("expiration-cache");
         Integer expirationCacheSeconds = null;
         if (expirationCache != null)
         {
            try
            {
               expirationCacheSeconds = new Integer(expirationCache);
            }
            catch (NumberFormatException e)
            {
               log.info("Ignoring bad expiration cache value " + expirationCache + " for producer '" + id + "'");
            }
         }

         // consumer didn't exist in the database, so create one and configure it
         consumer = consumerRegistry.createConsumer(id, expirationCacheSeconds, null);

         return consumer;
      }
      else
      {
         return null;
      }
   }

   public Object newChild(WSRPConsumer consumer, UnmarshallingContext nav, String nsURI, String localName,
                          Attributes attrs)
   {
      if (DEBUG)
      {
         System.out.println("newchild service " + localName);
      }

      ProducerInfo prodInfo = consumer.getProducerInfo();

      if ("endpoint-config".equals(localName) || "endpoint-wsdl-url".equals(localName))
      {
         return prodInfo.getEndpointConfigurationInfo();
      }
      else if ("registration-data".equals(localName))
      {
         return new RegistrationInfo(prodInfo);
      }
      else
      {
         return null;
      }
   }

   public Object newChild(RegistrationInfo registrationInfo, UnmarshallingContext nav, String nsURI, String localName,
                          Attributes attrs)
   {
      if (DEBUG)
      {
         System.out.println("newChild registrationInfo " + localName);
      }

      if ("property".equals(localName))
      {
         return new Property();
      }
      return null;
   }

   public void setValue(EndpointConfigurationInfo endpointInfo, UnmarshallingContext nav, String nsURI,
                        String localName, String value)
   {
      if (DEBUG)
      {
         System.out.println("setvalue endpointInfo " + localName);
      }

      if ("endpoint-wsdl-url".equals(localName))
      {
         value = StringPropertyReplacer.replaceProperties(value);
         endpointInfo.setWsdlDefinitionURL(value);
      }
   }

   public void setValue(RegistrationInfo registrationInfo, UnmarshallingContext nav, String nsURI, String localName,
                        String value)
   {
      if (DEBUG)
      {
         System.out.println("setvalue registrationinfo " + localName);
      }

      if ("consumer-name".equals(localName))
      {
         value = StringPropertyReplacer.replaceProperties(value);
         registrationInfo.setConsumerName(value);
      }
   }

   public void setValue(Property property, UnmarshallingContext nav, String nsURI, String localName, String value)
   {
      if (DEBUG)
      {
         System.out.println("setvalue property " + localName);
      }

      if ("name".equals(localName))
      {
         value = StringPropertyReplacer.replaceProperties(value);
         property.name = value;
      }
      else if ("lang".equals(localName))
      {
         value = StringPropertyReplacer.replaceProperties(value);
         property.lang = value;
      }
      else if ("value".equals(localName))
      {
         value = StringPropertyReplacer.replaceProperties(value);
         property.value = value;
      }
   }

   public void addChild(RegistrationInfo registrationInfo, Property property,
                        UnmarshallingContext nav, String nsURI, String localName)
   {
      if (DEBUG)
      {
         System.out.println("addchild registrationinfo property " + localName);
      }

      registrationInfo.setRegistrationPropertyValue(property.name, property.value).setLang(property.lang);
   }

   public void addChild(SortedMap<String, WSRPConsumer> consumers, WSRPConsumer consumer, UnmarshallingContext nav, String nsURI,
                        String localName)
   {
      ProducerInfo info = consumer.getProducerInfo();

      if (DEBUG)
      {
         System.out.println("adding consumer " + info.getId() + " to deployment - localName: " + localName);
      }

      String id = consumer.getProducerId();
      consumers.put(id, consumer);
      log.info("Added consumer for producer '" + id + "' from xml configuration.");


      // update the producer info once the whole information is known
      try
      {
         consumerRegistry.updateProducerInfo(info);
      }
      catch (Exception e)
      {
         // if we couldn't update the info, remove it from the list of service to be activated
         consumers.remove(id);
         log.info("Couldn't update the ProducerInfo for Consumer '" + info.getId() + "'", e);
      }
   }
}
