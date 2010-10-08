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

package org.gatein.wsrp.payload;

import org.gatein.common.util.ParameterValidation;
import org.gatein.pc.api.info.EventInfo;
import org.gatein.wsrp.WSRPTypeFactory;
import org.oasis.wsrp.v2.Event;
import org.oasis.wsrp.v2.EventPayload;
import org.oasis.wsrp.v2.NamedStringArray;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class PayloadUtils
{
   private final static Map<String, XSDTypeConverter> typeToConverters = new HashMap<String, XSDTypeConverter>(19);
   private final static Map<QName, XSDTypeConverter> nameToConverters = new HashMap<QName, XSDTypeConverter>(5);
   private final static Map<Class, XSDTypeConverter> classToConverters = new HashMap<Class, XSDTypeConverter>(19);

   static
   {
      XSDTypeConverter[] converterArray = XSDTypeConverter.values();
      for (XSDTypeConverter converter : converterArray)
      {
         typeToConverters.put(converter.typeName(), converter);

         // reverse mapping, unfortunately, there's not a one to one mapping from Java to XSD so converters with
         // null Java types are considered secondary... Semantic analysis could possibly be used...
         Class javaType = converter.getJavaType();
         if (javaType != null)
         {
            classToConverters.put(javaType, converter);
         }
      }
   }

   public static Serializable getPayloadAsSerializable(Event event, EventInfo eventInfo)
   {
      // GTNWSRP-49
      EventPayload payload = event.getPayload();
      if (payload == null)
      {
         return null;
      }

      ParameterValidation.throwIllegalArgExceptionIfNull(event, "Payload expected type");

      Object any = payload.getAny();
      if (any == null)
      {
         NamedStringArray namedStringArray = payload.getNamedStringArray();
         if (namedStringArray != null)
         {
            return new SerializableNamedStringArray(namedStringArray);
         }
         else
         {
            return null;
         }
      }
      else
      {
         Element element = (Element)any;
         QName type = event.getType();
         String typeName = type.getLocalPart();

         if (XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(type.getNamespaceURI()))
         {
            // if we want a default simple datatype, convert it directly
            XSDTypeConverter converter = typeToConverters.get(typeName);
            if (converter == null)
            {
               throw new IllegalArgumentException("Don't know how to deal with standard type: " + type);
            }

            // record which converter was used so that we can use it when it's time to marshall it back to XML
            nameToConverters.put(event.getName(), converter);

            return converter.parseFromXML(element.getTextContent());
         }
         else
         {
            // attempt to load the payload as a java class whose name is the type local part
            try
            {
               ClassLoader loader = Thread.currentThread().getContextClassLoader();
               Class<? extends Serializable> clazz = loader.loadClass(typeName).asSubclass(Serializable.class);
               JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
               Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
               JAXBElement result = unmarshaller.unmarshal(element, clazz);
               return (Serializable)result.getValue();
            }
            catch (Exception e)
            {
               throw new IllegalArgumentException("Couldn't unmarshall element " + element + " with expected type " + event, e);
            }
         }
      }
   }

   public static EventPayload getPayloadAsEventPayload(Event eventNeedingType, Serializable payload)
   {
      if (payload instanceof SerializableNamedStringArray)
      {
         SerializableNamedStringArray stringArray = (SerializableNamedStringArray)payload;
         return WSRPTypeFactory.createEventPayloadAsNamedString(stringArray.toNamedStringArray());
      }
      else
      {
         // todo: complete GTNWSRP-49
         QName name = eventNeedingType.getName();

         // we will use the payload class name as type for serialiation if we can't find something better...
         Class payloadClass = payload.getClass();
         QName type = new QName(payloadClass.getName());

         // first, try to get a converter from the event name and use the converter type
         XSDTypeConverter converter = nameToConverters.get(name);
         if (converter != null)
         {
            // remove from map to avoid memory leak
            nameToConverters.remove(name);

            type = converter.getXSDType();
         }
         else
         {
            // otherwise, try to get a converter from the payload class
            converter = classToConverters.get(payloadClass);

            if (converter != null)
            {
               type = converter.getXSDType();
            }
         }

         // else, use the class name as type for the serialization
         eventNeedingType.setType(type);
         try
         {
            JAXBContext context = JAXBContext.newInstance(payloadClass);
            Marshaller marshaller = context.createMarshaller();

            JAXBElement<Serializable> element = new JAXBElement<Serializable>(name, payloadClass, payload);
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(true);
            Document document = builderFactory.newDocumentBuilder().newDocument();
            marshaller.marshal(element, document);
            return WSRPTypeFactory.createEventPayloadAsAny(document.getDocumentElement());
         }
         catch (Exception e)
         {
            throw new IllegalArgumentException("Couldn't marshall payload " + payload + " with expected type " + type, e);
         }
      }
   }
}
