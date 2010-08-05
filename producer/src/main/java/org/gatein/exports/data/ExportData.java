/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2010, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/
package org.gatein.exports.data;

import java.io.UnsupportedEncodingException;

import org.gatein.common.NotYetImplemented;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public abstract class ExportData
{

   public abstract double getVersion();
   public abstract String getType();
   
   protected abstract byte[] internalEncodeAsBytes() throws UnsupportedEncodingException;
   
   
   //The encoding used to create the byte array
   protected static final String ENCODING = "UTF-8";
   
   protected static final String SEPARATOR = "_@_";
   
   public byte[] encodeAsBytes() throws UnsupportedEncodingException 
   {
      byte[] internalBytes = internalEncodeAsBytes();
      
      String token =  this.getType() + SEPARATOR + this.getVersion() + SEPARATOR;
      String dataString = new String(internalBytes, ENCODING);
      
      return (token + dataString).getBytes(ENCODING);
   }
   
   public static double getVersion(byte[] bytes) throws UnsupportedEncodingException
   {
      String dataString = new String(bytes, ENCODING);
      String[] split = dataString.split(SEPARATOR, 3);
      
      if (split.length >= 2)
      {
         double version = Double.parseDouble(split[1]);
         return version;
      }
   
      //if a version could not be found, return -1
      return -1;
   }
   
   public static String getType(byte[] bytes) throws UnsupportedEncodingException
   {
      String dataString = new String(bytes, ENCODING);
      String[] split = dataString.split(SEPARATOR, 2);
      
      if (split.length >= 2)
      {
         return split[0];
      }
      
      //if we could not find a type, then return null
      return null;
   }

   public static byte[] getInternalBytes(byte[] bytes) throws UnsupportedEncodingException
   {
      String dataString = new String(bytes, ENCODING);
      String[] split = dataString.split(SEPARATOR, 3);
      
      if (split.length >= 3)
      {
         String internalString = split[2];
         return internalString.getBytes(ENCODING);
      }
      else
      {
         //if we could not find the internal bytes, return null
         return null;
      }
   }
   
}

