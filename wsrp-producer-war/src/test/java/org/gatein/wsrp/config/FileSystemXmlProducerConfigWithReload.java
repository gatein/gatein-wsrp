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
package org.gatein.wsrp.config;

import org.gatein.common.net.URLTools;
import org.gatein.wsrp.producer.config.impl.xml.SimpleXMLProducerConfigurationService;

import java.net.URL;
import java.util.Enumeration;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class FileSystemXmlProducerConfigWithReload extends SimpleXMLProducerConfigurationService
{

   // Hack to force the producer to reload when the configuration property is set.
   // If unused then we will have issues with the producer not being loaded when we try
   // and get an instance.

   public void setConfigFile(String configLocation) throws Exception
   {
      Enumeration<URL> resources = getClass().getClassLoader().getResources(configLocation);

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

      this.inputStream = configURL.openStream();
      this.reloadConfiguration();
   }

}

