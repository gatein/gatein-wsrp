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


/**
 * <p>Java class for DestroyFailed complex type.
 * <p/>
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p/>
 * <pre>
 * &lt;complexType name="DestroyFailed">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="portletHandle" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="reason" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DestroyFailed", propOrder = {
   "portletHandle",
   "reason"
})
public class DestroyFailed
{

   @XmlElement(required = true)
   protected String portletHandle;
   @XmlElement(required = true)
   protected String reason;

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
    * Gets the value of the reason property.
    *
    * @return possible object is {@link String }
    */
   public String getReason()
   {
      return reason;
   }

   /**
    * Sets the value of the reason property.
    *
    * @param value allowed object is {@link String }
    */
   public void setReason(String value)
   {
      this.reason = value;
   }

}
