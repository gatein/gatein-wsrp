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
import org.gatein.wsrp.WSRPTypeFactory;
import org.oasis.wsrp.v2.Event;
import org.oasis.wsrp.v2.EventPayload;
import org.oasis.wsrp.v2.NamedStringArray;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.Serializable;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class PayloadUtils
{
   private final static Map<String, XSDTypeConverter> typeToConverters = new HashMap<String, XSDTypeConverter>(19);
   private final static Map<Class, XSDTypeConverter> classToConverters = new HashMap<Class, XSDTypeConverter>(19);
   private final static ThreadLocal<DocumentBuilder> documentBuilder = new ThreadLocal<DocumentBuilder>()
   {
      @Override
      protected DocumentBuilder initialValue()
      {
         DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
         builderFactory.setNamespaceAware(true);
         try
         {
            return builderFactory.newDocumentBuilder();
         }
         catch (ParserConfigurationException e)
         {
            throw new RuntimeException("Couldn't get a DocumentBuilder", e);
         }
      }
   };

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

   public static Serializable getPayloadAsSerializable(Event event)
   {
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

         if (type != null)
         {
            String typeName = type.getLocalPart();

            if (XMLConstants.W3C_XML_SCHEMA_NS_URI.equals(type.getNamespaceURI()))
            {
               // if we want a default simple datatype, convert it directly
               XSDTypeConverter converter = typeToConverters.get(typeName);
               if (converter == null)
               {
                  throw new IllegalArgumentException("Don't know how to deal with standard type: " + type);
               }

               return new SerializableSimplePayload(element, converter.parseFromXML(element.getTextContent()), converter);
            }
         }

         return new SerializablePayload(element);
      }
   }

   public static EventPayload getPayloadAsEventPayload(Event eventNeedingType, Serializable payload)
   {
      if (payload instanceof SerializableNamedStringArray)
      {
         SerializableNamedStringArray stringArray = (SerializableNamedStringArray)payload;
         return WSRPTypeFactory.createEventPayloadAsNamedString(stringArray.toNamedStringArray());
      }
      else if (payload instanceof SerializablePayload)
      {
         if (payload instanceof SerializableSimplePayload)
         {
            eventNeedingType.setType(((SerializableSimplePayload)payload).getConverter().getXSDType());
         }
         return WSRPTypeFactory.createEventPayloadAsAny(((SerializablePayload)payload).getElement());
      }
      else
      {
         Class payloadClass = payload.getClass();
         // try to get a converter from the payload class to assert a simple XSD type if possible
         XSDTypeConverter converter = classToConverters.get(payloadClass);
         if (converter != null)
         {
            eventNeedingType.setType(converter.getXSDType());
         }

         // Marshall payload to XML
         QName name = eventNeedingType.getName();
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
            throw new IllegalArgumentException("Couldn't marshall payload " + payload, e);
         }
      }
   }

   private static Document getDocument()
   {
      return documentBuilder.get().newDocument();
   }

   public static String outputToXML(Element node)
   {
      Document document = node.getOwnerDocument();
      DOMImplementationLS domImplLS = (DOMImplementationLS)document.getImplementation();
      LSSerializer serializer = domImplLS.createLSSerializer();
      return serializer.writeToString(node);
   }

   public static Element createElement(String namespaceURI, String name)
   {
      return getDocument().createElementNS(namespaceURI, name);
   }

   public static Element parseFromXMLString(String xml)
   {
      try
      {
         return documentBuilder.get().parse(new InputSource(new StringReader(xml))).getDocumentElement();
      }
      catch (Exception e)
      {
         throw new RuntimeException("Couldn't parse '" + xml + "' as a DOM Element.", e);
      }
   }
}
