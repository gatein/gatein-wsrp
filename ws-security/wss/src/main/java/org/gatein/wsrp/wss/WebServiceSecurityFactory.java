/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2011, Red Hat Middleware, LLC, and individual                    *
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
package org.gatein.wsrp.wss;

import org.gatein.wsrp.wss.credentials.CredentialsAccessor;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class WebServiceSecurityFactory
{
   public static final WebServiceSecurityFactory instance = new WebServiceSecurityFactory();
   public List<CustomizePortListener> customizePortListeners;
   private CredentialsAccessor credentialsAccessor;

   private WebServiceSecurityFactory()
   {
   }

   public static WebServiceSecurityFactory getInstance()
   {
      return instance;
   }

   public void setCredentialsAccessor(CredentialsAccessor credentialsAccessor)
   {
      this.credentialsAccessor = credentialsAccessor;
   }

   public CredentialsAccessor getCredentialsAccessor()
   {
      return credentialsAccessor;
   }

   public void addCustomizePortListener(CustomizePortListener listener)
   {
      if (this.customizePortListeners == null)
      {
         customizePortListeners = new ArrayList<CustomizePortListener>();
      }
      customizePortListeners.add(listener);
   }

   public void removeCustomizePortListener(CustomizePortListener listener)
   {
      if (customizePortListeners != null)
      {
         customizePortListeners.remove(listener);
      }
   }

   public List<CustomizePortListener> getCustomizePortListeners()
   {
      return customizePortListeners;
   }


}

