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

import org.gatein.wsrp.WSRPConstants;

import javax.xml.namespace.QName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;

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
         public Serializable parseFromXML(String value)
         {
            return parseAnySimpleType(value);
         }

         @Override
         public String printToXML(Serializable value)
         {
            return printAnySimpleType((String)value);
         }

         @Override
         public Class getJavaType()
         {
            return Object.class;
         }

         @Override
         public QName getXSDType()
         {
            return WSRPConstants.XSD_ANY_SIMPLE_TYPE;
         }
      },
   BASE64_BINARY("base64Binary")
      {
         @Override
         public Serializable parseFromXML(String value)
         {
            return parseBase64Binary(value);
         }

         @Override
         public String printToXML(Serializable value)
         {
            return printBase64Binary((byte[])value);
         }

         @Override
         public Class getJavaType()
         {
            return byte[].class;
         }

         @Override
         public QName getXSDType()
         {
            return WSRPConstants.XSD_BASE_64_BINARY;
         }
      },
   BOOLEAN("boolean")
      {
         @Override
         public Serializable parseFromXML(String value)
         {
            return parseBoolean(value);
         }

         @Override
         public String printToXML(Serializable value)
         {
            return printBoolean((Boolean)value);
         }

         @Override
         public Class getJavaType()
         {
            return boolean.class;
         }

         @Override
         public QName getXSDType()
         {
            return WSRPConstants.XSD_BOOLEAN;
         }
      },
   BYTE("byte")
      {
         @Override
         public Serializable parseFromXML(String value)
         {
            return parseByte(value);
         }

         @Override
         public String printToXML(Serializable value)
         {
            return printByte((Byte)value);
         }

         @Override
         public Class getJavaType()
         {
            return byte.class;
         }

         @Override
         public QName getXSDType()
         {
            return WSRPConstants.XSD_BYTE;
         }
      },
   DATE("date")
      {
         @Override
         public Serializable parseFromXML(String value)
         {
            return parseDate(value);
         }

         @Override
         public String printToXML(Serializable value)
         {
            return printDate((Calendar)value);
         }

         @Override
         public Class getJavaType()
         {
//            return Calendar.class;
            return null;
         }

         @Override
         public QName getXSDType()
         {
            return WSRPConstants.XSD_DATE;
         }
      },
   DATE_TIME("dateTime")
      {
         @Override
         public Serializable parseFromXML(String value)
         {
            return parseDateTime(value);
         }

         @Override
         public String printToXML(Serializable value)
         {
            return printDateTime((Calendar)value);
         }

         @Override
         public Class getJavaType()
         {
            return Calendar.class;
         }

         @Override
         public QName getXSDType()
         {
            return WSRPConstants.XSD_DATE_TIME;
         }
      },
   DECIMAL("decimal")
      {
         @Override
         public Serializable parseFromXML(String value)
         {
            return parseDecimal(value);
         }

         @Override
         public String printToXML(Serializable value)
         {
            return printDecimal((BigDecimal)value);
         }

         @Override
         public Class getJavaType()
         {
            return BigDecimal.class;
         }

         @Override
         public QName getXSDType()
         {
            return WSRPConstants.XSD_DECIMAL;
         }
      },
   DOUBLE("double")
      {
         @Override
         public Serializable parseFromXML(String value)
         {
            return parseDouble(value);
         }

         @Override
         public String printToXML(Serializable value)
         {
            return printDouble((Double)value);
         }

         @Override
         public Class getJavaType()
         {
            return double.class;
         }

         @Override
         public QName getXSDType()
         {
            return WSRPConstants.XSD_DOUBLE;
         }
      },
   FLOAT("float")
      {
         @Override
         public Serializable parseFromXML(String value)
         {
            return parseFloat(value);
         }

         @Override
         public String printToXML(Serializable value)
         {
            return printFloat((Float)value);
         }

         @Override
         public Class getJavaType()
         {
            return float.class;
         }

         @Override
         public QName getXSDType()
         {
            return WSRPConstants.XSD_FLOAT;
         }
      },
   HEX_BINARY("hexBinary")
      {
         @Override
         public Serializable parseFromXML(String value)
         {
            return parseHexBinary(value);
         }

         @Override
         public String printToXML(Serializable value)
         {
            return printHexBinary((byte[])value);
         }

         @Override
         public Class getJavaType()
         {
//            return byte[].class;
            return null;
         }

         @Override
         public QName getXSDType()
         {
            return WSRPConstants.XSD_HEX_BINARY;
         }
      },
   INT("int")
      {
         @Override
         public Serializable parseFromXML(String value)
         {
            return parseInt(value);
         }

         @Override
         public String printToXML(Serializable value)
         {
            return printInt((Integer)value);
         }

         @Override
         public Class getJavaType()
         {
            return int.class;
         }

         @Override
         public QName getXSDType()
         {
            return WSRPConstants.XSD_INT;
         }
      },
   INTEGER("integer")
      {
         @Override
         public Serializable parseFromXML(String value)
         {
            return parseInteger(value);
         }

         @Override
         public String printToXML(Serializable value)
         {
            return printInteger((BigInteger)value);
         }

         @Override
         public Class getJavaType()
         {
            return BigInteger.class;
         }

         @Override
         public QName getXSDType()
         {
            return WSRPConstants.XSD_INTEGER;
         }
      },
   LONG("long")
      {
         @Override
         public Serializable parseFromXML(String value)
         {
            return parseLong(value);
         }

         @Override
         public String printToXML(Serializable value)
         {
            return printLong((Long)value);
         }

         @Override
         public Class getJavaType()
         {
            return long.class;
         }

         @Override
         public QName getXSDType()
         {
            return WSRPConstants.XSD_LONG;
         }
      },
   SHORT("short")
      {
         @Override
         public Serializable parseFromXML(String value)
         {
            return parseShort(value);
         }

         @Override
         public String printToXML(Serializable value)
         {
            return printShort((Short)value);
         }

         @Override
         public Class getJavaType()
         {
            return short.class;
         }

         @Override
         public QName getXSDType()
         {
            return WSRPConstants.XSD_SHORT;
         }
      },
   STRING("string")
      {
         @Override
         public Serializable parseFromXML(String value)
         {
            return parseString(value);
         }

         @Override
         public String printToXML(Serializable value)
         {
            return printString((String)value);
         }

         @Override
         public Class getJavaType()
         {
            return String.class;
         }

         @Override
         public QName getXSDType()
         {
            return WSRPConstants.XSD_STRING;
         }
      },
   TIME("time")
      {
         @Override
         public Serializable parseFromXML(String value)
         {
            return parseTime(value);
         }

         @Override
         public String printToXML(Serializable value)
         {
            return printTime((Calendar)value);
         }

         @Override
         public Class getJavaType()
         {
//            return Calendar.class;
            return null;
         }

         @Override
         public QName getXSDType()
         {
            return WSRPConstants.XSD_TIME;
         }
      },
   UNSIGNED_INT("unsignedInt")
      {
         @Override
         public Serializable parseFromXML(String value)
         {
            return parseUnsignedInt(value);
         }

         @Override
         public String printToXML(Serializable value)
         {
            return printUnsignedInt((Long)value);
         }

         @Override
         public Class getJavaType()
         {
//            return long.class;
            return null;
         }

         @Override
         public QName getXSDType()
         {
            return WSRPConstants.XSD_UNSIGNED_INT;
         }
      },
   UNSIGNED_SHORT("unsignedShort")
      {
         @Override
         public Serializable parseFromXML(String value)
         {
            return parseUnsignedShort(value);
         }

         @Override
         public String printToXML(Serializable value)
         {
            return printUnsignedShort((Integer)value);
         }

         @Override
         public Class getJavaType()
         {
//            return int.class;
            return null;
         }

         @Override
         public QName getXSDType()
         {
            return WSRPConstants.XSD_UNSIGNED_SHORT;
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

   public abstract Serializable parseFromXML(String value);

   public abstract String printToXML(Serializable value);

   public abstract Class getJavaType();

   public abstract QName getXSDType();
}
