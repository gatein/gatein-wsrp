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
 * <p>Java class for InteractionParams complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="InteractionParams">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="portletStateChange" type="{urn:oasis:names:tc:wsrp:v1:types}StateChange"/>
 *         &lt;element name="interactionState" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="formParameters" type="{urn:oasis:names:tc:wsrp:v1:types}NamedString" maxOccurs="unbounded"
 * minOccurs="0"/>
 *         &lt;element name="uploadContexts" type="{urn:oasis:names:tc:wsrp:v1:types}UploadContext"
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
@XmlType(name = "InteractionParams", propOrder = {
   "portletStateChange",
   "interactionState",
   "formParameters",
   "uploadContexts",
   "extensions"
})
public class InteractionParams
{

   @XmlElement(required = true)
   protected StateChange portletStateChange;
   protected String interactionState;
   protected List<NamedString> formParameters;
   protected List<UploadContext> uploadContexts;
   protected List<Extension> extensions;

   /**
    * Gets the value of the portletStateChange property.
    *
    * @return possible object is {@link StateChange }
    */
   public StateChange getPortletStateChange()
   {
      return portletStateChange;
   }

   /**
    * Sets the value of the portletStateChange property.
    *
    * @param value allowed object is {@link StateChange }
    */
   public void setPortletStateChange(StateChange value)
   {
      this.portletStateChange = value;
   }

   /**
    * Gets the value of the interactionState property.
    *
    * @return possible object is {@link String }
    */
   public String getInteractionState()
   {
      return interactionState;
   }

   /**
    * Sets the value of the interactionState property.
    *
    * @param value allowed object is {@link String }
    */
   public void setInteractionState(String value)
   {
      this.interactionState = value;
   }

   /**
    * Gets the value of the formParameters property.
    * <p/>
    * <p/>
    * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
    * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
    * the formParameters property.
    * <p/>
    * <p/>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getFormParameters().add(newItem);
    * </pre>
    * <p/>
    * <p/>
    * <p/>
    * Objects of the following type(s) are allowed in the list {@link NamedString }
    */
   public List<NamedString> getFormParameters()
   {
      if (formParameters == null)
      {
         formParameters = new ArrayList<NamedString>();
      }
      return this.formParameters;
   }

   /**
    * Gets the value of the uploadContexts property.
    * <p/>
    * <p/>
    * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
    * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
    * the uploadContexts property.
    * <p/>
    * <p/>
    * For example, to add a new item, do as follows:
    * <pre>
    *    getUploadContexts().add(newItem);
    * </pre>
    * <p/>
    * <p/>
    * <p/>
    * Objects of the following type(s) are allowed in the list {@link UploadContext }
    */
   public List<UploadContext> getUploadContexts()
   {
      if (uploadContexts == null)
      {
         uploadContexts = new ArrayList<UploadContext>();
      }
      return this.uploadContexts;
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
