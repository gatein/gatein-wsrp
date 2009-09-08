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

package org.oasis.wsrp.v1;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for RegistrationData complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="RegistrationData">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="consumerName" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="consumerAgent" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="methodGetSupported" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="consumerModes" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"
 * minOccurs="0"/>
 *         &lt;element name="consumerWindowStates" type="{http://www.w3.org/2001/XMLSchema}string"
 * maxOccurs="unbounded"
 * minOccurs="0"/>
 *         &lt;element name="consumerUserScopes" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"
 * minOccurs="0"/>
 *         &lt;element name="customUserProfileData" type="{http://www.w3.org/2001/XMLSchema}string"
 * maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="registrationProperties" type="{urn:oasis:names:tc:wsrp:v1:types}Property"
 * maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="extensions" type="{urn:oasis:names:tc:wsrp:v1:types}Extension" maxOccurs="unbounded"
 * minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RegistrationData", propOrder = {
   "consumerName",
   "consumerAgent",
   "methodGetSupported",
   "consumerModes",
   "consumerWindowStates",
   "consumerUserScopes",
   "customUserProfileData",
   "registrationProperties",
   "extensions"
})
public class RegistrationData
{

   @XmlElement(required = true)
   protected String consumerName;
   @XmlElement(required = true)
   protected String consumerAgent;
   protected boolean methodGetSupported;
   protected List<String> consumerModes;
   protected List<String> consumerWindowStates;
   protected List<String> consumerUserScopes;
   protected List<String> customUserProfileData;
   protected List<Property> registrationProperties;
   protected List<Extension> extensions;

   /**
    * Gets the value of the consumerName property.
    *
    * @return
    *     possible object is
    *     {@link String }
    *
    */
   public String getConsumerName()
   {
      return consumerName;
   }

   /**
    * Sets the value of the consumerName property.
    *
    * @param value
    *     allowed object is
    *     {@link String }
    *
    */
   public void setConsumerName(String value)
   {
      this.consumerName = value;
   }

   /**
    * Gets the value of the consumerAgent property.
    *
    * @return
    *     possible object is
    *     {@link String }
    *
    */
   public String getConsumerAgent()
   {
      return consumerAgent;
   }

   /**
    * Sets the value of the consumerAgent property.
    *
    * @param value
    *     allowed object is
    *     {@link String }
    *
    */
   public void setConsumerAgent(String value)
   {
      this.consumerAgent = value;
   }

   /**
    * Gets the value of the methodGetSupported property.
    *
    */
   public boolean isMethodGetSupported()
   {
      return methodGetSupported;
   }

   /**
    * Sets the value of the methodGetSupported property.
    *
    */
   public void setMethodGetSupported(boolean value)
   {
      this.methodGetSupported = value;
   }

   /**
    * Gets the value of the consumerModes property.
    *
    * <p>
    * This accessor method returns a reference to the live list,
    * not a snapshot. Therefore any modification you make to the
    * returned list will be present inside the JAXB object.
    * This is why there is not a <CODE>set</CODE> method for the consumerModes property.
    *
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getConsumerModes().add(newItem);
    * </pre>
    *
    *
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link String }
    *
    *
    */
   public List<String> getConsumerModes()
   {
      if (consumerModes == null)
      {
         consumerModes = new ArrayList<String>();
      }
      return this.consumerModes;
   }

   /**
    * Gets the value of the consumerWindowStates property.
    *
    * <p>
    * This accessor method returns a reference to the live list,
    * not a snapshot. Therefore any modification you make to the
    * returned list will be present inside the JAXB object.
    * This is why there is not a <CODE>set</CODE> method for the consumerWindowStates property.
    *
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getConsumerWindowStates().add(newItem);
    * </pre>
    *
    *
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link String }
    *
    *
    */
   public List<String> getConsumerWindowStates()
   {
      if (consumerWindowStates == null)
      {
         consumerWindowStates = new ArrayList<String>();
      }
      return this.consumerWindowStates;
   }

   /**
    * Gets the value of the consumerUserScopes property.
    *
    * <p>
    * This accessor method returns a reference to the live list,
    * not a snapshot. Therefore any modification you make to the
    * returned list will be present inside the JAXB object.
    * This is why there is not a <CODE>set</CODE> method for the consumerUserScopes property.
    *
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getConsumerUserScopes().add(newItem);
    * </pre>
    *
    *
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link String }
    *
    *
    */
   public List<String> getConsumerUserScopes()
   {
      if (consumerUserScopes == null)
      {
         consumerUserScopes = new ArrayList<String>();
      }
      return this.consumerUserScopes;
   }

   /**
    * Gets the value of the customUserProfileData property.
    *
    * <p>
    * This accessor method returns a reference to the live list,
    * not a snapshot. Therefore any modification you make to the
    * returned list will be present inside the JAXB object.
    * This is why there is not a <CODE>set</CODE> method for the customUserProfileData property.
    *
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getCustomUserProfileData().add(newItem);
    * </pre>
    *
    *
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link String }
    *
    *
    */
   public List<String> getCustomUserProfileData()
   {
      if (customUserProfileData == null)
      {
         customUserProfileData = new ArrayList<String>();
      }
      return this.customUserProfileData;
   }

   /**
    * Gets the value of the registrationProperties property.
    *
    * <p>
    * This accessor method returns a reference to the live list,
    * not a snapshot. Therefore any modification you make to the
    * returned list will be present inside the JAXB object.
    * This is why there is not a <CODE>set</CODE> method for the registrationProperties property.
    *
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getRegistrationProperties().add(newItem);
    * </pre>
    *
    *
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link Property }
    *
    *
    */
   public List<Property> getRegistrationProperties()
   {
      if (registrationProperties == null)
      {
         registrationProperties = new ArrayList<Property>();
      }
      return this.registrationProperties;
   }

   /**
    * Gets the value of the extensions property.
    *
    * <p>
    * This accessor method returns a reference to the live list,
    * not a snapshot. Therefore any modification you make to the
    * returned list will be present inside the JAXB object.
    * This is why there is not a <CODE>set</CODE> method for the extensions property.
    *
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getExtensions().add(newItem);
    * </pre>
    *
    *
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link Extension }
    *
    *
    */
   public List<Extension> getExtensions()
   {
      if (extensions == null)
      {
         extensions = new ArrayList<Extension>();
      }
      return this.extensions;
   }

}
