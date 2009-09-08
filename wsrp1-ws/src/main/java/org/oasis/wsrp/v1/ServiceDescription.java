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
 * <p>Java class for ServiceDescription complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="ServiceDescription">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="requiresRegistration" type="{http://www.w3.org/2001/XMLSchema}boolean"/>
 *         &lt;element name="offeredPortlets" type="{urn:oasis:names:tc:wsrp:v1:types}PortletDescription"
 * maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="userCategoryDescriptions" type="{urn:oasis:names:tc:wsrp:v1:types}ItemDescription"
 * maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="customUserProfileItemDescriptions" type="{urn:oasis:names:tc:wsrp:v1:types}ItemDescription"
 * maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="customWindowStateDescriptions" type="{urn:oasis:names:tc:wsrp:v1:types}ItemDescription"
 * maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="customModeDescriptions" type="{urn:oasis:names:tc:wsrp:v1:types}ItemDescription"
 * maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="requiresInitCookie" type="{urn:oasis:names:tc:wsrp:v1:types}CookieProtocol"
 * minOccurs="0"/>
 *         &lt;element name="registrationPropertyDescription" type="{urn:oasis:names:tc:wsrp:v1:types}ModelDescription"
 * minOccurs="0"/>
 *         &lt;element name="locales" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"
 * minOccurs="0"/>
 *         &lt;element name="resourceList" type="{urn:oasis:names:tc:wsrp:v1:types}ResourceList" minOccurs="0"/>
 *         &lt;element name="extensions" type="{urn:oasis:names:tc:wsrp:v1:types}Extension" maxOccurs="unbounded"
 * minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ServiceDescription", propOrder = {
   "requiresRegistration",
   "offeredPortlets",
   "userCategoryDescriptions",
   "customUserProfileItemDescriptions",
   "customWindowStateDescriptions",
   "customModeDescriptions",
   "requiresInitCookie",
   "registrationPropertyDescription",
   "locales",
   "resourceList",
   "extensions"
})
public class ServiceDescription
{

   protected boolean requiresRegistration;
   protected List<PortletDescription> offeredPortlets;
   protected List<ItemDescription> userCategoryDescriptions;
   protected List<ItemDescription> customUserProfileItemDescriptions;
   protected List<ItemDescription> customWindowStateDescriptions;
   protected List<ItemDescription> customModeDescriptions;
   @XmlElement(defaultValue = "none")
   protected CookieProtocol requiresInitCookie;
   protected ModelDescription registrationPropertyDescription;
   protected List<String> locales;
   protected ResourceList resourceList;
   protected List<Extension> extensions;

   /** Gets the value of the requiresRegistration property. */
   public boolean isRequiresRegistration()
   {
      return requiresRegistration;
   }

   /** Sets the value of the requiresRegistration property. */
   public void setRequiresRegistration(boolean value)
   {
      this.requiresRegistration = value;
   }

   /**
    * Gets the value of the offeredPortlets property.
    * <p/>
    * <p/>
    * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
    * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
    * the offeredPortlets property.
    * <p/>
    * <p/>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getOfferedPortlets().add(newItem);
    * </pre>
    * <p/>
    * <p/>
    * <p/>
    * Objects of the following type(s) are allowed in the list {@link PortletDescription }
    */
   public List<PortletDescription> getOfferedPortlets()
   {
      if (offeredPortlets == null)
      {
         offeredPortlets = new ArrayList<PortletDescription>();
      }
      return this.offeredPortlets;
   }

   /**
    * Gets the value of the userCategoryDescriptions property.
    * <p/>
    * <p/>
    * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
    * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
    * the userCategoryDescriptions property.
    * <p/>
    * <p/>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getUserCategoryDescriptions().add(newItem);
    * </pre>
    * <p/>
    * <p/>
    * <p/>
    * Objects of the following type(s) are allowed in the list {@link ItemDescription }
    */
   public List<ItemDescription> getUserCategoryDescriptions()
   {
      if (userCategoryDescriptions == null)
      {
         userCategoryDescriptions = new ArrayList<ItemDescription>();
      }
      return this.userCategoryDescriptions;
   }

   /**
    * Gets the value of the customUserProfileItemDescriptions property.
    * <p/>
    * <p/>
    * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
    * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
    * the customUserProfileItemDescriptions property.
    * <p/>
    * <p/>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getCustomUserProfileItemDescriptions().add(newItem);
    * </pre>
    * <p/>
    * <p/>
    * <p/>
    * Objects of the following type(s) are allowed in the list {@link ItemDescription }
    */
   public List<ItemDescription> getCustomUserProfileItemDescriptions()
   {
      if (customUserProfileItemDescriptions == null)
      {
         customUserProfileItemDescriptions = new ArrayList<ItemDescription>();
      }
      return this.customUserProfileItemDescriptions;
   }

   /**
    * Gets the value of the customWindowStateDescriptions property.
    * <p/>
    * <p/>
    * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
    * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
    * the customWindowStateDescriptions property.
    * <p/>
    * <p/>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getCustomWindowStateDescriptions().add(newItem);
    * </pre>
    * <p/>
    * <p/>
    * <p/>
    * Objects of the following type(s) are allowed in the list {@link ItemDescription }
    */
   public List<ItemDescription> getCustomWindowStateDescriptions()
   {
      if (customWindowStateDescriptions == null)
      {
         customWindowStateDescriptions = new ArrayList<ItemDescription>();
      }
      return this.customWindowStateDescriptions;
   }

   /**
    * Gets the value of the customModeDescriptions property.
    * <p/>
    * <p/>
    * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
    * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
    * the customModeDescriptions property.
    * <p/>
    * <p/>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getCustomModeDescriptions().add(newItem);
    * </pre>
    * <p/>
    * <p/>
    * <p/>
    * Objects of the following type(s) are allowed in the list {@link ItemDescription }
    */
   public List<ItemDescription> getCustomModeDescriptions()
   {
      if (customModeDescriptions == null)
      {
         customModeDescriptions = new ArrayList<ItemDescription>();
      }
      return this.customModeDescriptions;
   }

   /**
    * Gets the value of the requiresInitCookie property.
    *
    * @return possible object is {@link CookieProtocol }
    */
   public CookieProtocol getRequiresInitCookie()
   {
      return requiresInitCookie;
   }

   /**
    * Sets the value of the requiresInitCookie property.
    *
    * @param value allowed object is {@link CookieProtocol }
    */
   public void setRequiresInitCookie(CookieProtocol value)
   {
      this.requiresInitCookie = value;
   }

   /**
    * Gets the value of the registrationPropertyDescription property.
    *
    * @return possible object is {@link ModelDescription }
    */
   public ModelDescription getRegistrationPropertyDescription()
   {
      return registrationPropertyDescription;
   }

   /**
    * Sets the value of the registrationPropertyDescription property.
    *
    * @param value allowed object is {@link ModelDescription }
    */
   public void setRegistrationPropertyDescription(ModelDescription value)
   {
      this.registrationPropertyDescription = value;
   }

   /**
    * Gets the value of the locales property.
    * <p/>
    * <p/>
    * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
    * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
    * the locales property.
    * <p/>
    * <p/>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getLocales().add(newItem);
    * </pre>
    * <p/>
    * <p/>
    * <p/>
    * Objects of the following type(s) are allowed in the list {@link String }
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
    * Gets the value of the resourceList property.
    *
    * @return possible object is {@link ResourceList }
    */
   public ResourceList getResourceList()
   {
      return resourceList;
   }

   /**
    * Sets the value of the resourceList property.
    *
    * @param value allowed object is {@link ResourceList }
    */
   public void setResourceList(ResourceList value)
   {
      this.resourceList = value;
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
