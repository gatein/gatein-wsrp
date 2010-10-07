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


import org.gatein.wsrp.WSRPTypeFactory;
import org.oasis.wsrp.v2.NamedString;
import org.oasis.wsrp.v2.NamedStringArray;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class SerializableNamedStringArray implements Serializable
{
   private SerializableNamedString[] strings;

   SerializableNamedStringArray(NamedStringArray array)
   {
      initFrom(array);
   }

   void initFrom(NamedStringArray array)
   {
      List<NamedString> namedString = array.getNamedString();
      strings = new SerializableNamedString[namedString.size()];
      int i = 0;
      for (NamedString string : namedString)
      {
         strings[i++] = new SerializableNamedString(string.getName(), string.getValue());
      }
   }

   NamedStringArray toNamedStringArray()
   {
      NamedStringArray array = WSRPTypeFactory.createNamedStringArray();

      List<NamedString> namedString = new ArrayList<NamedString>(strings.length);
      for (SerializableNamedString string : strings)
      {
         namedString.add(WSRPTypeFactory.createNamedString(string.name, string.value));
      }

      array.getNamedString().addAll(namedString);
      return array;
   }

   private static class SerializableNamedString implements Serializable
   {
      String name;
      String value;

      private SerializableNamedString(String name, String value)
      {
         this.name = name;
         this.value = value;
      }
   }
}
