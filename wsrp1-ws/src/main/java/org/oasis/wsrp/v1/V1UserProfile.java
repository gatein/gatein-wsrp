
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

package org.oasis.wsrp.v1;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * <p>Java class for UserProfile complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="UserProfile">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="name" type="{urn:oasis:names:tc:wsrp:v1:types}PersonName" minOccurs="0"/>
 *         &lt;element name="bdate" type="{http://www.w3.org/2001/XMLSchema}dateTime" minOccurs="0"/>
 *         &lt;element name="gender" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="employerInfo" type="{urn:oasis:names:tc:wsrp:v1:types}EmployerInfo" minOccurs="0"/>
 *         &lt;element name="homeInfo" type="{urn:oasis:names:tc:wsrp:v1:types}Contact" minOccurs="0"/>
 *         &lt;element name="businessInfo" type="{urn:oasis:names:tc:wsrp:v1:types}Contact" minOccurs="0"/>
 *         &lt;element name="extensions" type="{urn:oasis:names:tc:wsrp:v1:types}Extension" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "UserProfile", propOrder = {
    "name",
    "bdate",
    "gender",
    "employerInfo",
    "homeInfo",
    "businessInfo",
    "extensions"
})
public class V1UserProfile {

    protected V1PersonName name;
    @XmlSchemaType(name = "dateTime")
    protected XMLGregorianCalendar bdate;
    protected String gender;
    protected V1EmployerInfo employerInfo;
    protected V1Contact homeInfo;
    protected V1Contact businessInfo;
    protected List<V1Extension> extensions;

    /**
     * Gets the value of the name property.
     * 
     * @return
     *     possible object is
     *     {@link V1PersonName }
     *     
     */
    public V1PersonName getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link V1PersonName }
     *     
     */
    public void setName(V1PersonName value) {
        this.name = value;
    }

    /**
     * Gets the value of the bdate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getBdate() {
        return bdate;
    }

    /**
     * Sets the value of the bdate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setBdate(XMLGregorianCalendar value) {
        this.bdate = value;
    }

    /**
     * Gets the value of the gender property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGender() {
        return gender;
    }

    /**
     * Sets the value of the gender property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGender(String value) {
        this.gender = value;
    }

    /**
     * Gets the value of the employerInfo property.
     * 
     * @return
     *     possible object is
     *     {@link V1EmployerInfo }
     *     
     */
    public V1EmployerInfo getEmployerInfo() {
        return employerInfo;
    }

    /**
     * Sets the value of the employerInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link V1EmployerInfo }
     *     
     */
    public void setEmployerInfo(V1EmployerInfo value) {
        this.employerInfo = value;
    }

    /**
     * Gets the value of the homeInfo property.
     * 
     * @return
     *     possible object is
     *     {@link V1Contact }
     *     
     */
    public V1Contact getHomeInfo() {
        return homeInfo;
    }

    /**
     * Sets the value of the homeInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link V1Contact }
     *     
     */
    public void setHomeInfo(V1Contact value) {
        this.homeInfo = value;
    }

    /**
     * Gets the value of the businessInfo property.
     * 
     * @return
     *     possible object is
     *     {@link V1Contact }
     *     
     */
    public V1Contact getBusinessInfo() {
        return businessInfo;
    }

    /**
     * Sets the value of the businessInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link V1Contact }
     *     
     */
    public void setBusinessInfo(V1Contact value) {
        this.businessInfo = value;
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
     * {@link V1Extension }
     * 
     * 
     */
    public List<V1Extension> getExtensions() {
        if (extensions == null) {
            extensions = new ArrayList<V1Extension>();
        }
        return this.extensions;
    }

}
