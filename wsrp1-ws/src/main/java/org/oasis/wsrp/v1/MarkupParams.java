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
 * <p>Java class for MarkupParams complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="MarkupParams">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="secureClientCommunication" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="locales" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/>
 *         &lt;element name="mimeTypes" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/>
 *         &lt;element name="mode" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="windowState" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="clientData" type="{urn:oasis:names:tc:wsrp:v1:types}ClientData" minOccurs="0"/>
 *         &lt;element name="navigationalState" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="markupCharacterSets" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"
 * minOccurs="0"/>
 *         &lt;element name="validateTag" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="validNewModes" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"
 * minOccurs="0"/>
 *         &lt;element name="validNewWindowStates" type="{http://www.w3.org/2001/XMLSchema}string"
 * maxOccurs="unbounded"
 * minOccurs="0"/>
 *         &lt;element name="extensions" type="{urn:oasis:names:tc:wsrp:v1:types}Extension" maxOccurs="unbounded"
 * minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MarkupParams", propOrder = {
   "secureClientCommunication",
   "locales",
   "mimeTypes",
   "mode",
   "windowState",
   "clientData",
   "navigationalState",
   "markupCharacterSets",
   "validateTag",
   "validNewModes",
   "validNewWindowStates",
   "extensions"
})
public class MarkupParams
{

   protected boolean secureClientCommunication;
   @XmlElement(required = true)
   protected List<String> locales;
   @XmlElement(required = true)
   protected List<String> mimeTypes;
   @XmlElement(required = true)
   protected String mode;
   @XmlElement(required = true)
   protected String windowState;
   protected ClientData clientData;
   protected String navigationalState;
   protected List<String> markupCharacterSets;
   protected String validateTag;
   protected List<String> validNewModes;
   protected List<String> validNewWindowStates;
   protected List<Extension> extensions;

   /**
    * Gets the value of the secureClientCommunication property.
    *
    */
   public boolean isSecureClientCommunication()
   {
      return secureClientCommunication;
   }

   /**
    * Sets the value of the secureClientCommunication property.
    *
    */
   public void setSecureClientCommunication(boolean value)
   {
      this.secureClientCommunication = value;
   }

   /**
    * Gets the value of the locales property.
    *
    * <p>
    * This accessor method returns a reference to the live list,
    * not a snapshot. Therefore any modification you make to the
    * returned list will be present inside the JAXB object.
    * This is why there is not a <CODE>set</CODE> method for the locales property.
    *
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getLocales().add(newItem);
    * </pre>
    *
    *
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link String }
    *
    *
    */
   public List<String> getLocales()
   {
      if (locales == null)
      {
         locales = new ArrayList<String>();
      }
      return this.locales;
   }

   /**
    * Gets the value of the mimeTypes property.
    *
    * <p>
    * This accessor method returns a reference to the live list,
    * not a snapshot. Therefore any modification you make to the
    * returned list will be present inside the JAXB object.
    * This is why there is not a <CODE>set</CODE> method for the mimeTypes property.
    *
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getMimeTypes().add(newItem);
    * </pre>
    *
    *
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link String }
    *
    *
    */
   public List<String> getMimeTypes()
   {
      if (mimeTypes == null)
      {
         mimeTypes = new ArrayList<String>();
      }
      return this.mimeTypes;
   }

   /**
    * Gets the value of the mode property.
    *
    * @return
    *     possible object is
    *     {@link String }
    *
    */
   public String getMode()
   {
      return mode;
   }

   /**
    * Sets the value of the mode property.
    *
    * @param value
    *     allowed object is
    *     {@link String }
    *
    */
   public void setMode(String value)
   {
      this.mode = value;
   }

   /**
    * Gets the value of the windowState property.
    *
    * @return
    *     possible object is
    *     {@link String }
    *
    */
   public String getWindowState()
   {
      return windowState;
   }

   /**
    * Sets the value of the windowState property.
    *
    * @param value
    *     allowed object is
    *     {@link String }
    *
    */
   public void setWindowState(String value)
   {
      this.windowState = value;
   }

   /**
    * Gets the value of the clientData property.
    *
    * @return
    *     possible object is
    *     {@link ClientData }
    *
    */
   public ClientData getClientData()
   {
      return clientData;
   }

   /**
    * Sets the value of the clientData property.
    *
    * @param value
    *     allowed object is
    *     {@link ClientData }
    *
    */
   public void setClientData(ClientData value)
   {
      this.clientData = value;
   }

   /**
    * Gets the value of the navigationalState property.
    *
    * @return
    *     possible object is
    *     {@link String }
    *
    */
   public String getNavigationalState()
   {
      return navigationalState;
   }

   /**
    * Sets the value of the navigationalState property.
    *
    * @param value
    *     allowed object is
    *     {@link String }
    *
    */
   public void setNavigationalState(String value)
   {
      this.navigationalState = value;
   }

   /**
    * Gets the value of the markupCharacterSets property.
    *
    * <p>
    * This accessor method returns a reference to the live list,
    * not a snapshot. Therefore any modification you make to the
    * returned list will be present inside the JAXB object.
    * This is why there is not a <CODE>set</CODE> method for the markupCharacterSets property.
    *
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getMarkupCharacterSets().add(newItem);
    * </pre>
    *
    *
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link String }
    *
    *
    */
   public List<String> getMarkupCharacterSets()
   {
      if (markupCharacterSets == null)
      {
         markupCharacterSets = new ArrayList<String>();
      }
      return this.markupCharacterSets;
   }

   /**
    * Gets the value of the validateTag property.
    *
    * @return
    *     possible object is
    *     {@link String }
    *
    */
   public String getValidateTag()
   {
      return validateTag;
   }

   /**
    * Sets the value of the validateTag property.
    *
    * @param value
    *     allowed object is
    *     {@link String }
    *
    */
   public void setValidateTag(String value)
   {
      this.validateTag = value;
   }

   /**
    * Gets the value of the validNewModes property.
    *
    * <p>
    * This accessor method returns a reference to the live list,
    * not a snapshot. Therefore any modification you make to the
    * returned list will be present inside the JAXB object.
    * This is why there is not a <CODE>set</CODE> method for the validNewModes property.
    *
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getValidNewModes().add(newItem);
    * </pre>
    *
    *
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link String }
    *
    *
    */
   public List<String> getValidNewModes()
   {
      if (validNewModes == null)
      {
         validNewModes = new ArrayList<String>();
      }
      return this.validNewModes;
   }

   /**
    * Gets the value of the validNewWindowStates property.
    *
    * <p>
    * This accessor method returns a reference to the live list,
    * not a snapshot. Therefore any modification you make to the
    * returned list will be present inside the JAXB object.
    * This is why there is not a <CODE>set</CODE> method for the validNewWindowStates property.
    *
    * <p>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getValidNewWindowStates().add(newItem);
    * </pre>
    *
    *
    * <p>
    * Objects of the following type(s) are allowed in the list
    * {@link String }
    *
    *
    */
   public List<String> getValidNewWindowStates()
   {
      if (validNewWindowStates == null)
      {
         validNewWindowStates = new ArrayList<String>();
      }
      return this.validNewWindowStates;
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
