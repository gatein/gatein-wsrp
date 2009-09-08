/*
 * JBoss, a division of Red Hat
 * Copyright 2009, Red Hat Middleware, LLC, and individual
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

package org.gatein.wsrp.services;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A service factory that statically cache implementations. It is mainly used in the test environment to void the very
 * expensive creation of SOAP service proxies.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 11517 $
 */
public class CachingServiceFactory implements ManageableServiceFactory
{

   /** . */
   private static final Map<String, Object> cache = new ConcurrentHashMap<String, Object>();

   /** . */
   private ManageableServiceFactory delegate;

   public <T> T getService(Class<T> clazz) throws Exception
   {
      if (delegate == null)
      {
         throw new IllegalStateException("No delegate service factory");
      }
      if (clazz == null)
      {
         throw new IllegalArgumentException();
      }

      //
      Object service = cache.get(clazz.getName());
      if (service == null)
      {
         service = delegate.getService(clazz);
         if (service != null)
         {
            cache.put(clazz.getName(), service);
         }
      }
      return clazz.cast(service);
   }

   public ManageableServiceFactory getDelegate()
   {
      return delegate;
   }

   public void setDelegate(ManageableServiceFactory delegate)
   {
      this.delegate = delegate;
   }

   public boolean isAvailable()
   {
      return delegate != null && delegate.isAvailable();
   }

   public boolean isFailed()
   {
      return delegate == null || delegate.isFailed();
   }

   public void setFailed(boolean failed)
   {
      if (delegate != null)
      {
         delegate.setFailed(failed);
      }
   }

   public void setAvailable(boolean available)
   {
      if (delegate != null)
      {
         delegate.setAvailable(available);
      }
   }

   public String getServiceDescriptionURL()
   {
      if (delegate != null)
      {
         return delegate.getServiceDescriptionURL();
      }
      return null;
   }

   public String getMarkupURL()
   {
      if (delegate != null)
      {
         return delegate.getMarkupURL();
      }
      return null;
   }

   public String getRegistrationURL()
   {
      if (delegate != null)
      {
         return delegate.getRegistrationURL();
      }
      return null;
   }

   public String getPortletManagementURL()
   {
      if (delegate != null)
      {
         return delegate.getPortletManagementURL();
      }
      return null;
   }


   public void setServiceDescriptionURL(String serviceDescriptionURL)
   {
      if (delegate == null)
      {
         throw new IllegalStateException("No delegate service factory");
      }
      delegate.setServiceDescriptionURL(serviceDescriptionURL);
   }

   public void setMarkupURL(String markupURL)
   {
      if (delegate == null)
      {
         throw new IllegalStateException("No delegate service factory");
      }
      delegate.setMarkupURL(markupURL);
   }

   public void setRegistrationURL(String registrationURL)
   {
      if (delegate == null)
      {
         throw new IllegalStateException("No delegate service factory");
      }
      delegate.setRegistrationURL(registrationURL);
   }

   public void setPortletManagementURL(String portletManagementURL)
   {
      if (delegate == null)
      {
         throw new IllegalStateException("No delegate service factory");
      }
      delegate.setPortletManagementURL(portletManagementURL);
   }

   public void start()
   {
      if (delegate == null)
      {
         throw new IllegalStateException("No delegate service factory");
      }
      delegate.start();
   }

   public void stop()
   {
      if (delegate == null)
      {
         throw new IllegalStateException("No delegate service factory");
      }
      delegate.stop();
   }
}
