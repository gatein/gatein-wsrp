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

package org.gatein.wsrp.producer.config.impl.xml;

import org.gatein.common.net.URLTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.Enumeration;

/**
 * A ProducerConfigurationService that can load an initial seed of an XML configuration file and then store
 * modifications made to it on the file system, in the <code>${java.io.tmpdir}/portal/wsrp-producer-config.xml</code>
 * file.
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 12276 $
 * @since 2.6
 */
public class FileSystemXMLProducerConfigurationService extends SimpleXMLProducerConfigurationService
{
   private static final Logger log = LoggerFactory.getLogger(FileSystemXMLProducerConfigurationService.class);

   private String configLocation;

   private File config;

   public String getConfigLocation()
   {
      return configLocation;
   }

   public void setConfigLocation(String configLocation)
   {
      this.configLocation = configLocation;
   }


   public void start() throws Exception
   {
      File dataDir = new File(System.getProperty("java.io.tmpdir"));

      // if "portal" directory doesn't exist already in data, create it (JBPORTAL-2229)
      File portalDir = new File(dataDir, "portal");
      if (!portalDir.exists())
      {
         if (!portalDir.mkdir())
         {
            throw new RuntimeException("Couldn't create 'portal' directory in " + dataDir.getAbsolutePath());
         }
      }
      else
      {
         if (!portalDir.isDirectory())
         {
            throw new RuntimeException("Was expecting a directory named 'portal' in " + dataDir.getAbsolutePath()
               + ", not a simple file! Cannot continue.");
         }
      }

      config = new File(portalDir, "wsrp-producer-config.xml");

      reloadConfiguration();
   }

   public void loadConfiguration() throws Exception
   {
      URL configURL = getConfigLocationURL();

      try
      {
         loadConfigurationAt(configURL);
      }
      catch (Exception e)
      {
         if (config.exists())
         {
            log.debug("Configuration saved at " + config.getCanonicalPath()
               + " is not loading properly. Falling back to default configuration.");
            config.delete(); // delete improper config so that we retrieve the default configuration
            loadConfigurationAt(getConfigLocationURL());
         }
      }
   }

   private void loadConfigurationAt(URL configURL) throws Exception
   {
      log.debug("About to parse producer configuration " + configURL);
      inputStream = configURL.openStream();

      super.loadConfiguration();
   }

   private URL getConfigLocationURL() throws Exception
   {
      if (!config.exists())
      {
         // Setup URLs
         if (configLocation == null)
         {
            throw new Exception("The config location is null");
         }

         // make sure we pick the furthest down the path in case there's several similarly named resources in the path
         Enumeration resources = Thread.currentThread().getContextClassLoader().getResources(configLocation);
         URL configURL = null;
         while (resources.hasMoreElements())
         {
            configURL = (URL)resources.nextElement();
         }

         if (configURL == null)
         {
            throw new Exception("The config " + configLocation + " does not exist");
         }
         if (!URLTools.exists(configURL))
         {
            throw new Exception("The config " + configURL + " does not exist");
         }
         return configURL;
      }
      else
      {
         return config.toURI().toURL();
      }
   }

   public void saveConfiguration() throws Exception
   {
      saveConfigurationTo(config);
   }
}
