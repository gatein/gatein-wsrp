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
 * <p>Java class for Postal complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="Postal">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="street" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="city" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="stateprov" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="postalcode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="country" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="organization" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="extensions" type="{urn:oasis:names:tc:wsrp:v1:types}Extension" maxOccurs="unbounded"
 * minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Postal", propOrder = {
   "name",
   "street",
   "city",
   "stateprov",
   "postalcode",
   "country",
   "organization",
   "extensions"
})
public class Postal
{

   protected String name;
   protected String street;
   protected String city;
   protected String stateprov;
   protected String postalcode;
   protected String country;
   protected String organization;
   protected List<Extension> extensions;

   /**
    * Gets the value of the name property.
    *
    * @return possible object is {@link String }
    */
   public String getName()
   {
      return name;
   }

   /**
    * Sets the value of the name property.
    *
    * @param value allowed object is {@link String }
    */
   public void setName(String value)
   {
      this.name = value;
   }

   /**
    * Gets the value of the street property.
    *
    * @return possible object is {@link String }
    */
   public String getStreet()
   {
      return street;
   }

   /**
    * Sets the value of the street property.
    *
    * @param value allowed object is {@link String }
    */
   public void setStreet(String value)
   {
      this.street = value;
   }

   /**
    * Gets the value of the city property.
    *
    * @return possible object is {@link String }
    */
   public String getCity()
   {
      return city;
   }

   /**
    * Sets the value of the city property.
    *
    * @param value allowed object is {@link String }
    */
   public void setCity(String value)
   {
      this.city = value;
   }

   /**
    * Gets the value of the stateprov property.
    *
    * @return possible object is {@link String }
    */
   public String getStateprov()
   {
      return stateprov;
   }

   /**
    * Sets the value of the stateprov property.
    *
    * @param value allowed object is {@link String }
    */
   public void setStateprov(String value)
   {
      this.stateprov = value;
   }

   /**
    * Gets the value of the postalcode property.
    *
    * @return possible object is {@link String }
    */
   public String getPostalcode()
   {
      return postalcode;
   }

   /**
    * Sets the value of the postalcode property.
    *
    * @param value allowed object is {@link String }
    */
   public void setPostalcode(String value)
   {
      this.postalcode = value;
   }

   /**
    * Gets the value of the country property.
    *
    * @return possible object is {@link String }
    */
   public String getCountry()
   {
      return country;
   }

   /**
    * Sets the value of the country property.
    *
    * @param value allowed object is {@link String }
    */
   public void setCountry(String value)
   {
      this.country = value;
   }

   /**
    * Gets the value of the organization property.
    *
    * @return possible object is {@link String }
    */
   public String getOrganization()
   {
      return organization;
   }

   /**
    * Sets the value of the organization property.
    *
    * @param value allowed object is {@link String }
    */
   public void setOrganization(String value)
   {
      this.organization = value;
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
