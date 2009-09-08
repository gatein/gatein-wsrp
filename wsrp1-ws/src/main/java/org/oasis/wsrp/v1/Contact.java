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
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.List;


/**
 * <p>Java class for Contact complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="Contact">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="postal" type="{urn:oasis:names:tc:wsrp:v1:types}Postal" minOccurs="0"/>
 *         &lt;element name="telecom" type="{urn:oasis:names:tc:wsrp:v1:types}Telecom" minOccurs="0"/>
 *         &lt;element name="online" type="{urn:oasis:names:tc:wsrp:v1:types}Online" minOccurs="0"/>
 *         &lt;element name="extensions" type="{urn:oasis:names:tc:wsrp:v1:types}Extension" maxOccurs="unbounded"
 * minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Contact", propOrder = {
   "postal",
   "telecom",
   "online",
   "extensions"
})
public class Contact
{

   protected Postal postal;
   protected Telecom telecom;
   protected Online online;
   protected List<Extension> extensions;

   /**
    * Gets the value of the postal property.
    *
    * @return possible object is {@link Postal }
    */
   public Postal getPostal()
   {
      return postal;
   }

   /**
    * Sets the value of the postal property.
    *
    * @param value allowed object is {@link Postal }
    */
   public void setPostal(Postal value)
   {
      this.postal = value;
   }

   /**
    * Gets the value of the telecom property.
    *
    * @return possible object is {@link Telecom }
    */
   public Telecom getTelecom()
   {
      return telecom;
   }

   /**
    * Sets the value of the telecom property.
    *
    * @param value allowed object is {@link Telecom }
    */
   public void setTelecom(Telecom value)
   {
      this.telecom = value;
   }

   /**
    * Gets the value of the online property.
    *
    * @return possible object is {@link Online }
    */
   public Online getOnline()
   {
      return online;
   }

   /**
    * Sets the value of the online property.
    *
    * @param value allowed object is {@link Online }
    */
   public void setOnline(Online value)
   {
      this.online = value;
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
