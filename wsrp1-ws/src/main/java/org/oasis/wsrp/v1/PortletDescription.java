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
 * <p>Java class for PortletDescription complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="PortletDescription">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="portletHandle" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="markupTypes" type="{urn:oasis:names:tc:wsrp:v1:types}MarkupType" maxOccurs="unbounded"/>
 *         &lt;element name="groupID" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="description" type="{urn:oasis:names:tc:wsrp:v1:types}LocalizedString" minOccurs="0"/>
 *         &lt;element name="shortTitle" type="{urn:oasis:names:tc:wsrp:v1:types}LocalizedString" minOccurs="0"/>
 *         &lt;element name="title" type="{urn:oasis:names:tc:wsrp:v1:types}LocalizedString" minOccurs="0"/>
 *         &lt;element name="displayName" type="{urn:oasis:names:tc:wsrp:v1:types}LocalizedString" minOccurs="0"/>
 *         &lt;element name="keywords" type="{urn:oasis:names:tc:wsrp:v1:types}LocalizedString" maxOccurs="unbounded"
 * minOccurs="0"/>
 *         &lt;element name="userCategories" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"
 * minOccurs="0"/>
 *         &lt;element name="userProfileItems" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"
 * minOccurs="0"/>
 *         &lt;element name="usesMethodGet" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="defaultMarkupSecure" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="onlySecure" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="userContextStoredInSession" type="{http://www.w3.org/2001/XMLSchema}boolean"
 * minOccurs="0"/>
 *         &lt;element name="templatesStoredInSession" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="hasUserSpecificState" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="doesUrlTemplateProcessing" type="{http://www.w3.org/2001/XMLSchema}boolean"
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
@XmlType(name = "PortletDescription", propOrder = {
   "portletHandle",
   "markupTypes",
   "groupID",
   "description",
   "shortTitle",
   "title",
   "displayName",
   "keywords",
   "userCategories",
   "userProfileItems",
   "usesMethodGet",
   "defaultMarkupSecure",
   "onlySecure",
   "userContextStoredInSession",
   "templatesStoredInSession",
   "hasUserSpecificState",
   "doesUrlTemplateProcessing",
   "extensions"
})
public class PortletDescription
{

   @XmlElement(required = true)
   protected String portletHandle;
   @XmlElement(required = true)
   protected List<MarkupType> markupTypes;
   protected String groupID;
   protected LocalizedString description;
   protected LocalizedString shortTitle;
   protected LocalizedString title;
   protected LocalizedString displayName;
   protected List<LocalizedString> keywords;
   protected List<String> userCategories;
   protected List<String> userProfileItems;
   @XmlElement(defaultValue = "false")
   protected Boolean usesMethodGet;
   @XmlElement(defaultValue = "false")
   protected Boolean defaultMarkupSecure;
   @XmlElement(defaultValue = "false")
   protected Boolean onlySecure;
   @XmlElement(defaultValue = "false")
   protected Boolean userContextStoredInSession;
   @XmlElement(defaultValue = "false")
   protected Boolean templatesStoredInSession;
   @XmlElement(defaultValue = "false")
   protected Boolean hasUserSpecificState;
   @XmlElement(defaultValue = "false")
   protected Boolean doesUrlTemplateProcessing;
   protected List<Extension> extensions;

   /**
    * Gets the value of the portletHandle property.
    *
    * @return possible object is {@link String }
    */
   public String getPortletHandle()
   {
      return portletHandle;
   }

   /**
    * Sets the value of the portletHandle property.
    *
    * @param value allowed object is {@link String }
    */
   public void setPortletHandle(String value)
   {
      this.portletHandle = value;
   }

   /**
    * Gets the value of the markupTypes property.
    * <p/>
    * <p/>
    * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
    * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
    * the markupTypes property.
    * <p/>
    * <p/>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getMarkupTypes().add(newItem);
    * </pre>
    * <p/>
    * <p/>
    * <p/>
    * Objects of the following type(s) are allowed in the list {@link MarkupType }
    */
   public List<MarkupType> getMarkupTypes()
   {
      if (markupTypes == null)
      {
         markupTypes = new ArrayList<MarkupType>();
      }
      return this.markupTypes;
   }

   /**
    * Gets the value of the groupID property.
    *
    * @return possible object is {@link String }
    */
   public String getGroupID()
   {
      return groupID;
   }

   /**
    * Sets the value of the groupID property.
    *
    * @param value allowed object is {@link String }
    */
   public void setGroupID(String value)
   {
      this.groupID = value;
   }

   /**
    * Gets the value of the description property.
    *
    * @return possible object is {@link LocalizedString }
    */
   public LocalizedString getDescription()
   {
      return description;
   }

   /**
    * Sets the value of the description property.
    *
    * @param value allowed object is {@link LocalizedString }
    */
   public void setDescription(LocalizedString value)
   {
      this.description = value;
   }

   /**
    * Gets the value of the shortTitle property.
    *
    * @return possible object is {@link LocalizedString }
    */
   public LocalizedString getShortTitle()
   {
      return shortTitle;
   }

   /**
    * Sets the value of the shortTitle property.
    *
    * @param value allowed object is {@link LocalizedString }
    */
   public void setShortTitle(LocalizedString value)
   {
      this.shortTitle = value;
   }

   /**
    * Gets the value of the title property.
    *
    * @return possible object is {@link LocalizedString }
    */
   public LocalizedString getTitle()
   {
      return title;
   }

   /**
    * Sets the value of the title property.
    *
    * @param value allowed object is {@link LocalizedString }
    */
   public void setTitle(LocalizedString value)
   {
      this.title = value;
   }

   /**
    * Gets the value of the displayName property.
    *
    * @return possible object is {@link LocalizedString }
    */
   public LocalizedString getDisplayName()
   {
      return displayName;
   }

   /**
    * Sets the value of the displayName property.
    *
    * @param value allowed object is {@link LocalizedString }
    */
   public void setDisplayName(LocalizedString value)
   {
      this.displayName = value;
   }

   /**
    * Gets the value of the keywords property.
    * <p/>
    * <p/>
    * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
    * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
    * the keywords property.
    * <p/>
    * <p/>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getKeywords().add(newItem);
    * </pre>
    * <p/>
    * <p/>
    * <p/>
    * Objects of the following type(s) are allowed in the list {@link LocalizedString }
    */
   public List<LocalizedString> getKeywords()
   {
      if (keywords == null)
      {
         keywords = new ArrayList<LocalizedString>();
      }
      return this.keywords;
   }

   /**
    * Gets the value of the userCategories property.
    * <p/>
    * <p/>
    * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
    * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
    * the userCategories property.
    * <p/>
    * <p/>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getUserCategories().add(newItem);
    * </pre>
    * <p/>
    * <p/>
    * <p/>
    * Objects of the following type(s) are allowed in the list {@link String }
    */
   public List<String> getUserCategories()
   {
      if (userCategories == null)
      {
         userCategories = new ArrayList<String>();
      }
      return this.userCategories;
   }

   /**
    * Gets the value of the userProfileItems property.
    * <p/>
    * <p/>
    * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
    * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
    * the userProfileItems property.
    * <p/>
    * <p/>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getUserProfileItems().add(newItem);
    * </pre>
    * <p/>
    * <p/>
    * <p/>
    * Objects of the following type(s) are allowed in the list {@link String }
    */
   public List<String> getUserProfileItems()
   {
      if (userProfileItems == null)
      {
         userProfileItems = new ArrayList<String>();
      }
      return this.userProfileItems;
   }

   /**
    * Gets the value of the usesMethodGet property.
    *
    * @return possible object is {@link Boolean }
    */
   public Boolean isUsesMethodGet()
   {
      return usesMethodGet;
   }

   /**
    * Sets the value of the usesMethodGet property.
    *
    * @param value allowed object is {@link Boolean }
    */
   public void setUsesMethodGet(Boolean value)
   {
      this.usesMethodGet = value;
   }

   /**
    * Gets the value of the defaultMarkupSecure property.
    *
    * @return possible object is {@link Boolean }
    */
   public Boolean isDefaultMarkupSecure()
   {
      return defaultMarkupSecure;
   }

   /**
    * Sets the value of the defaultMarkupSecure property.
    *
    * @param value allowed object is {@link Boolean }
    */
   public void setDefaultMarkupSecure(Boolean value)
   {
      this.defaultMarkupSecure = value;
   }

   /**
    * Gets the value of the onlySecure property.
    *
    * @return possible object is {@link Boolean }
    */
   public Boolean isOnlySecure()
   {
      return onlySecure;
   }

   /**
    * Sets the value of the onlySecure property.
    *
    * @param value allowed object is {@link Boolean }
    */
   public void setOnlySecure(Boolean value)
   {
      this.onlySecure = value;
   }

   /**
    * Gets the value of the userContextStoredInSession property.
    *
    * @return possible object is {@link Boolean }
    */
   public Boolean isUserContextStoredInSession()
   {
      return userContextStoredInSession;
   }

   /**
    * Sets the value of the userContextStoredInSession property.
    *
    * @param value allowed object is {@link Boolean }
    */
   public void setUserContextStoredInSession(Boolean value)
   {
      this.userContextStoredInSession = value;
   }

   /**
    * Gets the value of the templatesStoredInSession property.
    *
    * @return possible object is {@link Boolean }
    */
   public Boolean isTemplatesStoredInSession()
   {
      return templatesStoredInSession;
   }

   /**
    * Sets the value of the templatesStoredInSession property.
    *
    * @param value allowed object is {@link Boolean }
    */
   public void setTemplatesStoredInSession(Boolean value)
   {
      this.templatesStoredInSession = value;
   }

   /**
    * Gets the value of the hasUserSpecificState property.
    *
    * @return possible object is {@link Boolean }
    */
   public Boolean isHasUserSpecificState()
   {
      return hasUserSpecificState;
   }

   /**
    * Sets the value of the hasUserSpecificState property.
    *
    * @param value allowed object is {@link Boolean }
    */
   public void setHasUserSpecificState(Boolean value)
   {
      this.hasUserSpecificState = value;
   }

   /**
    * Gets the value of the doesUrlTemplateProcessing property.
    *
    * @return possible object is {@link Boolean }
    */
   public Boolean isDoesUrlTemplateProcessing()
   {
      return doesUrlTemplateProcessing;
   }

   /**
    * Sets the value of the doesUrlTemplateProcessing property.
    *
    * @param value allowed object is {@link Boolean }
    */
   public void setDoesUrlTemplateProcessing(Boolean value)
   {
      this.doesUrlTemplateProcessing = value;
   }

   /**
    * Gets the value of the extensions property.
    * <p/>
    * <p/>
    * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
    * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
    * the extensions property.
    * <p/>
    * <p/>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getExtensions().add(newItem);
    * </pre>
    * <p/>
    * <p/>
    * <p/>
    * Objects of the following type(s) are allowed in the list {@link Extension }
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
