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

import org.gatein.exports.ExportPersistenceManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public abstract class ExportData
{
   protected static final String NO_ID = "__NO_ID__";
   private String id = NO_ID;

   public static <T extends ExportData> T initExportData(Class<T> expected, byte[] encodedData, ExportPersistenceManager persistenceManager)
   {
      if (encodedData != null && encodedData.length > 0)
      {
         try
         {
            ByteArrayInputStream bais = new ByteArrayInputStream(encodedData);
            ObjectInputStream ois = new ObjectInputStream(bais);

            String type = ois.readUTF();

            ExportData exportData;
            if (ExportContext.TYPE.equals(type))
            {
               exportData = new ExportContext();
            }
            else if (ExportPortletData.TYPE.equals(type))
            {
               exportData = new ExportPortletData();
            }
            else
            {
               throw new IllegalArgumentException("Unknown ExportData type '" + type + "'");
            }

            T result = expected.cast(exportData);

            double version = ois.readDouble();
            if (!result.supports(version))
            {
               throw new IllegalArgumentException(expected.getSimpleName() + " doesn't know how to deal with version '" + version + "'");
            }

            String id = ois.readUTF();
            exportData.id = id;
            if (NO_ID.equals(id))
            {
               result.decodeExtraData(ois);
            }
            else
            {
               if(persistenceManager == null)
               {
                  throw new IllegalStateException("Encoded data points to persisted state, yet no ExportPersistenceManager has been provided to load state from persistence");
               }
               result = persistenceManager.loadExportData(id, expected);
            }

            return result;
         }
         catch (IOException e)
         {
            throw new IllegalArgumentException("Couldn't read from byte array", e);
         }
      }
      throw new IllegalArgumentException("Cannot create ExportData from null or empty byte array");
   }


   protected boolean supports(double version)
   {
      return Double.compare(getVersion(), version) == 0;
   }

   protected abstract void decodeExtraData(ObjectInputStream ois) throws IOException;
   protected abstract void encodeExtraData(ObjectOutputStream oos) throws IOException;


   public byte[] encodeAsBytes() throws IOException
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ObjectOutputStream oos = new ObjectOutputStream(baos);

      oos.writeUTF(getType());
      oos.writeDouble(getVersion());
      oos.writeUTF(id);

      encodeExtraData(oos);

      oos.close();

      return baos.toByteArray();
   }

   protected abstract double getVersion();

   protected abstract String getType();

   public String getId()
   {
      return id;
   }

   public void setId(String id)
   {
      this.id = id;
   }
}

