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

package org.gatein.wsrp.consumer.portlet.info;

import org.gatein.common.util.ParameterValidation;
import org.gatein.pc.api.info.NavigationInfo;
import org.gatein.pc.api.info.ParameterInfo;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class WSRPNavigationInfo implements NavigationInfo
{
   private final Map<String, ParameterInfo> byId;
   private final Map<QName, ParameterInfo> byName;

   public WSRPNavigationInfo(Collection<ParameterInfo> params)
   {
      if (ParameterValidation.existsAndIsNotEmpty(params))
      {
         byId = new HashMap<String, ParameterInfo>(params.size());
         byName = new HashMap<QName, ParameterInfo>(params.size());

         for (ParameterInfo param : params)
         {
            byId.put(param.getId(), param);
            byName.put(param.getName(), param);
         }
      }
      else
      {
         byId = Collections.emptyMap();
         byName = Collections.emptyMap();
      }
   }

   public ParameterInfo getPublicParameter(String id)
   {
      return byId.get(id);
   }

   public ParameterInfo getPublicParameter(QName name)
   {
      return byName.get(name);
   }

   public Collection<? extends ParameterInfo> getPublicParameters()
   {
      return Collections.unmodifiableCollection(byId.values());
   }
}
