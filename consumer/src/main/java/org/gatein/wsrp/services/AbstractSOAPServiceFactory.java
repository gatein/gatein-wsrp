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

import javax.xml.ws.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Perform common logic to soap based service factories. This one caches the service retrieved from the JNDI lookup.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 11484 $
 */
public abstract class AbstractSOAPServiceFactory extends AbstractJNDIServiceFactory
{

   /** Cache the services. */
   private Map<String, Service> services = new ConcurrentHashMap<String, Service>();

   /**
    * Retrieve the stub from the service. The stub is not thread safe and must be customized for each thead.
    *
    * @param serviceClass the requested service class
    * @param service      the service implementation obtained from the JNDI lookup
    * @return an implementation based on the provided service
    * @throws Exception
    */
   protected abstract <T> T getStubFromService(Class<T> serviceClass, Service service) throws Exception;

   public <T> T getService(Class<T> serviceClass) throws Exception
   {
      if (serviceClass == null)
      {
         throw new IllegalArgumentException("Null class not accepted to perform lookup");
      }

      //
      String key = serviceClass.getName();

      // Get the cached service, it's ok because they are thread safe
      Service service = services.get(key);
      if (service == null)
      {
         service = super.getServiceFor(serviceClass);

         //
         if (service != null)
         {
            services.put(key, service);
         }
      }

      // Get the stub from the service, remember that the stub itself is not threadsafe
      // and must be customized for every request to this method.
      if (service != null)
      {
         T result = ServiceWrapper.getServiceWrapper(serviceClass, getStubFromService(serviceClass, service), this);

         // if we managed to retrieve a service, we're probably available
         setFailed(false);
         setAvailable(true);

         return result;
      }
      else
      {
         return null;
      }
   }
}
