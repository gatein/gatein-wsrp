/*
* JBoss, a division of Red Hat
* Copyright 2008, Red Hat Middleware, LLC, and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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

package org.gatein.wsrp.producer.config;

import org.gatein.wsrp.producer.config.impl.xml.SimpleXMLProducerConfigurationService;
import org.jboss.xb.binding.JBossXBException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class JAXBProducerConfigurationTestCase extends ProducerConfigurationTestCase
{

   protected void setUp() throws Exception
   {
      service = new SimpleXMLProducerConfigurationService();
   }

   protected ProducerConfiguration getProducerConfiguration(URL location) throws JBossXBException, IOException
   {
      if (location != null)
      {
         final InputStream inputStream = location.openStream();
         ((SimpleXMLProducerConfigurationService)service).setInputStream(inputStream);
      }
      return service.getConfiguration();
   }

   @Override
   protected URL getConfigurationURL()
   {
      return ((SimpleXMLProducerConfigurationService)service).getConfigurationURL();
   }
}
