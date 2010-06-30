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

package org.gatein.wsrp.payload;

import java.io.Serializable;

import static javax.xml.bind.DatatypeConverter.*;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public enum XSDTypeConverter
{
   ANY_SIMPLE_TYPE("anySimpleType")
      {
         @Override
         public Serializable convert(String value)
         {
            return parseAnySimpleType(value);
         }
      },
   BASE64_BINARY("base64Binary")
      {
         @Override
         public Serializable convert(String value)
         {
            return parseBase64Binary(value);
         }
      },
   BOOLEAN("boolean")
      {
         @Override
         public Serializable convert(String value)
         {
            return parseBoolean(value);
         }
      },
   BYTE("byte")
      {
         @Override
         public Serializable convert(String value)
         {
            return parseByte(value);
         }
      },
   DATE("date")
      {
         @Override
         public Serializable convert(String value)
         {
            return parseDate(value);
         }
      },
   DATE_TIME("dateTime")
      {
         @Override
         public Serializable convert(String value)
         {
            return parseDateTime(value);
         }
      },
   DECIMAL("decimal")
      {
         @Override
         public Serializable convert(String value)
         {
            return parseDecimal(value);
         }
      },
   DOUBLE("double")
      {
         @Override
         public Serializable convert(String value)
         {
            return parseDouble(value);
         }
      },
   FLOAT("float")
      {
         @Override
         public Serializable convert(String value)
         {
            return parseFloat(value);
         }
      },
   HEX_BINARY("hexBinary")
      {
         @Override
         public Serializable convert(String value)
         {
            return parseHexBinary(value);
         }
      },
   INT("int")
      {
         @Override
         public Serializable convert(String value)
         {
            return parseInt(value);
         }
      },
   INTEGER("integer")
      {
         @Override
         public Serializable convert(String value)
         {
            return parseInteger(value);
         }
      },
   LONG("long")
      {
         @Override
         public Serializable convert(String value)
         {
            return parseLong(value);
         }
      },
   SHORT("short")
      {
         @Override
         public Serializable convert(String value)
         {
            return parseShort(value);
         }
      },
   STRING("string")
      {
         @Override
         public Serializable convert(String value)
         {
            return parseString(value);
         }
      },
   TIME("time")
      {
         @Override
         public Serializable convert(String value)
         {
            return parseTime(value);
         }
      },
   UNSIGNED_INT("unsignedInt")
      {
         @Override
         public Serializable convert(String value)
         {
            return parseUnsignedInt(value);
         }
      },
   UNSIGNED_SHORT("unsignedShort")
      {
         @Override
         public Serializable convert(String value)
         {
            return parseUnsignedShort(value);
         }
      };

   private XSDTypeConverter(String typeName)
   {
      this.typeName = typeName;
   }

   private String typeName;

   public String typeName()
   {
      return typeName;
   }

   public abstract Serializable convert(String value);
}
