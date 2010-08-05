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
import java.util.Map;

import org.gatein.pc.api.ParametersStateString;
import org.gatein.pc.api.StateString;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class PersistedExportData extends ExportData
{
   protected final String type;
   protected final String refID;
   protected static final double VERSION = 1.0;
   
   protected static final String REFIDKEY = "rID";
   protected static final String TYPEKEY = "type";
   
   public PersistedExportData(String type, String refID)
   {
     this.type = type;
     this.refID = refID;
   }
   
   public String getType()
   {
      return type;
   }

   public double getVersion()
   {
      return VERSION;
   }

   protected byte[] internalEncodeAsBytes() throws UnsupportedEncodingException
   {
      ParametersStateString parameterStateString = ParametersStateString.create();

      parameterStateString.setValue(REFIDKEY, REFIDKEY);
      parameterStateString.setValue(TYPEKEY, type);
      
      String stateString = parameterStateString.getStringValue();
      return stateString.getBytes(ENCODING);
   }
   
   public static PersistedExportData create(byte[] bytes) throws UnsupportedEncodingException
   {
      Map<String, String[]> map = StateString.decodeOpaqueValue(new String(bytes, ENCODING));
      
      String refId = null;
      String type = null;
      
      if (map.containsKey(REFIDKEY) && map.get(REFIDKEY).length > 0)
      {
         refId = map.get(REFIDKEY)[0];
      }
      
      if (map.containsKey(TYPEKEY) && map.get(TYPEKEY).length > 0 )
      {
         type = map.get(TYPEKEY)[0];
      }
      
      return new PersistedExportData(type, refId);
   }
   

}

