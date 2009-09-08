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
 * <p>Java class for UploadContext complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="UploadContext">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="mimeType" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="uploadData" type="{http://www.w3.org/2001/XMLSchema}base64Binary"/>
 *         &lt;element name="mimeAttributes" type="{urn:oasis:names:tc:wsrp:v1:types}NamedString" maxOccurs="unbounded"
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
@XmlType(name = "UploadContext", propOrder = {
   "mimeType",
   "uploadData",
   "mimeAttributes",
   "extensions"
})
public class UploadContext
{

   @XmlElement(required = true)
   protected String mimeType;
   @XmlElement(required = true)
   protected byte[] uploadData;
   protected List<NamedString> mimeAttributes;
   protected List<Extension> extensions;

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
    * Gets the value of the uploadData property.
    *
    * @return possible object is byte[]
    */
   public byte[] getUploadData()
   {
      return uploadData;
   }

   /**
    * Sets the value of the uploadData property.
    *
    * @param value allowed object is byte[]
    */
   public void setUploadData(byte[] value)
   {
      this.uploadData = ((byte[])value);
   }

   /**
    * Gets the value of the mimeAttributes property.
    * <p/>
    * <p/>
    * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
    * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
    * the mimeAttributes property.
    * <p/>
    * <p/>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getMimeAttributes().add(newItem);
    * </pre>
    * <p/>
    * <p/>
    * <p/>
    * Objects of the following type(s) are allowed in the list {@link NamedString }
    */
   public List<NamedString> getMimeAttributes()
   {
      if (mimeAttributes == null)
      {
         mimeAttributes = new ArrayList<NamedString>();
      }
      return this.mimeAttributes;
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
