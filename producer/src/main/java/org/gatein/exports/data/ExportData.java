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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public abstract class ExportData
{

   public abstract double getVersion();
   public abstract String getType();
   
   protected abstract byte[] internalEncodeAsBytes() throws UnsupportedEncodingException, IOException;
   
   
   //The encoding used to create the byte array
   protected static final String ENCODING = "UTF-8";
   
   protected static final String SEPARATOR = "_@_";
   
   public byte[] encodeAsBytes() throws IOException 
   {
      byte[] internalBytes = internalEncodeAsBytes();
      
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      
      oos.writeUTF(this.getType());
      oos.writeDouble(this.getVersion());
      
      if (internalBytes != null)
      {
         oos.write(internalBytes);
      }
      
      oos.close();
      
      return baos.toByteArray();
   }
   
   public static double getVersion(byte[] bytes) throws IOException
   {
      if (bytes != null && bytes.length > 0)
      {
         ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
         ObjectInputStream ois = new ObjectInputStream(bais);

         String type = ois.readUTF();
         Double version = ois.readDouble();

         return version.doubleValue();
      }
      else
      {
         return -1;
      }
   }
   
   public static String getType(byte[] bytes) throws IOException
   {
      if (bytes != null && bytes.length > 0)
      {
         ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
         ObjectInputStream ois = new ObjectInputStream(bais);

         return ois.readUTF();
      }
      else
      {
         return null;
      }
   }

   public static byte[] getInternalBytes(byte[] bytes) throws IOException
   {
      if (bytes != null && bytes.length > 0)
      {
         ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
         ObjectInputStream ois = new ObjectInputStream(bais);

         String type = ois.readUTF();
         Double version = ois.readDouble();

         byte[] internalBytes = null;
         if (ois.available() > 0)
         {
            internalBytes = new byte[ois.available()];
            ois.readFully(internalBytes);
         }
         return internalBytes;
      }
      else
      {
         return null;
      }
   }
}

