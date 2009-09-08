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

import org.gatein.common.io.IOTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.InitialContext;
import javax.xml.ws.Service;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

/**
 * A service factory implementation that get the services using JNDI lookups.
 *
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 11484 $
 * @noinspection ALL
 * @since 2.4
 */
public abstract class AbstractJNDIServiceFactory implements ManageableServiceFactory
{

   /** The logger. */
   protected final Logger log = LoggerFactory.getLogger(getClass());

   /** The JNDI configuration. */
   private Properties env;

   /** Default mapping between WSRP port type class and associated JNDI name */
   private static Properties DEFAULT_FACTORY_MAPPING;

   /** Whether or not this ServiceFactory has an unrecoverable error condition */
   protected boolean failed = false;

   /** Whether or not this ServiceFactory is availble to provide services */
   protected boolean available = true;

   static
   {
      // fix-me: this is hardcoded from values from portal-wsrp-client.jar/META-INF/jboss-client.xml... NOT GOOD!
      DEFAULT_FACTORY_MAPPING = new Properties();
      DEFAULT_FACTORY_MAPPING.setProperty(
         "org.jboss.portal.wsrp.core.WSRP_v1_ServiceDescription_PortType",
         "wsrp-client/service/ServiceDescriptionService");
      DEFAULT_FACTORY_MAPPING.setProperty(
         "org.jboss.portal.wsrp.core.WSRP_v1_Markup_PortType",
         "wsrp-client/service/MarkupService");
      DEFAULT_FACTORY_MAPPING.setProperty(
         "org.jboss.portal.wsrp.core.WSRP_v1_Registration_PortType",
         "wsrp-client/service/RegistrationService");
      DEFAULT_FACTORY_MAPPING.setProperty(
         "org.jboss.portal.wsrp.core.WSRP_v1_PortletManagement_PortType",
         "wsrp-client/service/PortletManagementService");
   }

   /** A Map recording the mapping between WSRP port type class name and JDNI name of the implementing service. */
   protected Properties portJNDIMapping = DEFAULT_FACTORY_MAPPING;

   protected void createService() throws Exception
   {
      if (env != null)
      {
         for (Iterator i = env.entrySet().iterator(); i.hasNext();)
         {
            Map.Entry entry = (Map.Entry)i.next();
            String name = (String)entry.getKey();
            String value = (String)entry.getValue();
            log.debug("Use env property " + name + "=" + value);
         }
         return;
      }
      log.debug("createService: null env");
   }

   public Properties getEnv()
   {
      return env;
   }

   public void setEnv(Properties env)
   {
      this.env = env;
   }

   protected <T> Service getServiceFor(Class<T> serviceClass) throws Exception
   {
      if (serviceClass == null)
      {
         throw new IllegalArgumentException("Null class not accepted to perform lookup");
      }

      if (!isAvailable())
      {
         throw new IllegalStateException("This ServiceFactory is not ready to service clients!");
      }

      //
      String key = serviceClass.getName();
      if (!portJNDIMapping.containsKey(key))
      {
         setFailed(true);
         throw new IllegalArgumentException("Unknown service class: " + key);
      }

      String jndiName = (String)portJNDIMapping.get(key);
      log.debug("Looking up service for class " + key + " using JNDI name " + jndiName);
      if (jndiName == null)
      {
         setFailed(true);
         throw new IllegalArgumentException("No such service " + serviceClass);
      }

      //
      InitialContext ctx = null;
      try
      {
         if (env != null)
         {
            ctx = new InitialContext(env);
         }
         else
         {
            ctx = new InitialContext();
         }

         //
         Object service = ctx.lookup(jndiName);
         if (log.isTraceEnabled())
         {
            log.trace("JNDI lookup for " + jndiName + " returned " + service);
         }

         //
         return (Service)service;
      }
      finally
      {
         IOTools.safeClose(ctx);
      }
   }

   public boolean isFailed()
   {
      return failed;
   }

   public void start()
   {
      // todo: implement as needed 
   }

   public void stop()
   {
      // todo: implement as needed
   }

   public void setFailed(boolean failed)
   {
      this.failed = failed;
   }

   public boolean isAvailable()
   {
      return available && !failed;
   }

   public void setAvailable(boolean available)
   {
      this.available = available;
   }

   public Properties getPortJNDIMapping()
   {
      return portJNDIMapping;
   }

   public void setPortJNDIMapping(Properties portJNDIMapping)
   {
      this.portJNDIMapping = portJNDIMapping;
   }
}
