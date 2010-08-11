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

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class ExportPortletData extends ExportData
{

   protected static final String ENCODING = "UTF-8";
   public static final String TYPE = "WSRP_EC";
   public static final double VERSION = 1.0;
   
   protected static final String PORTLETHANDLEKEY = "pID";
   protected static final String PORTLETSTATEKEY = "pState";
   
   protected String portletHandle;
   protected byte[] portletState;
   
   public ExportPortletData(String portletHandle, byte[] portletState)
   {
      this.portletHandle = portletHandle;
      this.portletState = portletState;
   }
   
   public String getPortletHandle()
   {
      return this.portletHandle;
   }
   
   public byte[] getPortletState()
   {
      return this.portletState;
   }
   
   public String getType()
   {
      return TYPE;
   }

   public double getVersion()
   {
      return VERSION;
   }

   public static ExportPortletData create(byte[] bytes) throws IOException
   {      
      ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
      ObjectInputStream ois = new ObjectInputStream(bais);
      
      String portletHandle;
      byte[] portletState;
      
      portletHandle = ois.readUTF();
      
      if (ois.available() > 0)
      {
         portletState = new byte[ois.available()];
         ois.readFully(portletState);
      }
      else
      {
         portletState = null;
      }
      
      ois.close();

      return new ExportPortletData(portletHandle, portletState);
   }
   
   protected byte[] internalEncodeAsBytes() throws IOException
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);
      
      oos.writeUTF(portletHandle);
      
      if (portletState != null)
      {
         oos.write(portletState);
      }
      
      oos.close();
      
      return baos.toByteArray();
   }

}

