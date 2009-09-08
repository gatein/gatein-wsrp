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
 * <p>Java class for MarkupContext complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="MarkupContext">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="useCachedMarkup" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="mimeType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="markupString" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="markupBinary" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *         &lt;element name="locale" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="requiresUrlRewriting" type="{http://www.w3.org/2001/XMLSchema}boolean" minOccurs="0"/>
 *         &lt;element name="cacheControl" type="{urn:oasis:names:tc:wsrp:v1:types}CacheControl" minOccurs="0"/>
 *         &lt;element name="preferredTitle" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="extensions" type="{urn:oasis:names:tc:wsrp:v1:types}Extension" maxOccurs="unbounded"
 * minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MarkupContext", propOrder = {
   "useCachedMarkup",
   "mimeType",
   "markupString",
   "markupBinary",
   "locale",
   "requiresUrlRewriting",
   "cacheControl",
   "preferredTitle",
   "extensions"
})
public class MarkupContext
{

   @XmlElement(defaultValue = "false")
   protected Boolean useCachedMarkup;
   protected String mimeType;
   protected String markupString;
   protected byte[] markupBinary;
   protected String locale;
   @XmlElement(defaultValue = "false")
   protected Boolean requiresUrlRewriting;
   protected CacheControl cacheControl;
   protected String preferredTitle;
   protected List<Extension> extensions;

   /**
    * Gets the value of the useCachedMarkup property.
    *
    * @return possible object is {@link Boolean }
    */
   public Boolean isUseCachedMarkup()
   {
      return useCachedMarkup;
   }

   /**
    * Sets the value of the useCachedMarkup property.
    *
    * @param value allowed object is {@link Boolean }
    */
   public void setUseCachedMarkup(Boolean value)
   {
      this.useCachedMarkup = value;
   }

   /**
    * Gets the value of the mimeType property.
    *
    * @return possible object is {@link String }
    */
   public String getMimeType()
   {
      return mimeType;
   }

   /**
    * Sets the value of the mimeType property.
    *
    * @param value allowed object is {@link String }
    */
   public void setMimeType(String value)
   {
      this.mimeType = value;
   }

   /**
    * Gets the value of the markupString property.
    *
    * @return possible object is {@link String }
    */
   public String getMarkupString()
   {
      return markupString;
   }

   /**
    * Sets the value of the markupString property.
    *
    * @param value allowed object is {@link String }
    */
   public void setMarkupString(String value)
   {
      this.markupString = value;
   }

   /**
    * Gets the value of the markupBinary property.
    *
    * @return possible object is byte[]
    */
   public byte[] getMarkupBinary()
   {
      return markupBinary;
   }

   /**
    * Sets the value of the markupBinary property.
    *
    * @param value allowed object is byte[]
    */
   public void setMarkupBinary(byte[] value)
   {
      this.markupBinary = ((byte[])value);
   }

   /**
    * Gets the value of the locale property.
    *
    * @return possible object is {@link String }
    */
   public String getLocale()
   {
      return locale;
   }

   /**
    * Sets the value of the locale property.
    *
    * @param value allowed object is {@link String }
    */
   public void setLocale(String value)
   {
      this.locale = value;
   }

   /**
    * Gets the value of the requiresUrlRewriting property.
    *
    * @return possible object is {@link Boolean }
    */
   public Boolean isRequiresUrlRewriting()
   {
      return requiresUrlRewriting;
   }

   /**
    * Sets the value of the requiresUrlRewriting property.
    *
    * @param value allowed object is {@link Boolean }
    */
   public void setRequiresUrlRewriting(Boolean value)
   {
      this.requiresUrlRewriting = value;
   }

   /**
    * Gets the value of the cacheControl property.
    *
    * @return possible object is {@link CacheControl }
    */
   public CacheControl getCacheControl()
   {
      return cacheControl;
   }

   /**
    * Sets the value of the cacheControl property.
    *
    * @param value allowed object is {@link CacheControl }
    */
   public void setCacheControl(CacheControl value)
   {
      this.cacheControl = value;
   }

   /**
    * Gets the value of the preferredTitle property.
    *
    * @return possible object is {@link String }
    */
   public String getPreferredTitle()
   {
      return preferredTitle;
   }

   /**
    * Sets the value of the preferredTitle property.
    *
    * @param value allowed object is {@link String }
    */
   public void setPreferredTitle(String value)
   {
      this.preferredTitle = value;
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
