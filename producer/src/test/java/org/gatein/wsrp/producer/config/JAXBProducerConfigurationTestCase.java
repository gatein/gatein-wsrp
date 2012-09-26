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

import org.gatein.wsrp.api.plugins.PluginsAccess;
import org.gatein.wsrp.producer.config.impl.xml.ProducerConfigurationFactory;
import org.jboss.xb.binding.JBossXBException;
import org.jboss.xb.binding.ObjectModelFactory;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;

import java.io.IOException;
import java.net.URL;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class JAXBProducerConfigurationTestCase extends ProducerConfigurationTestCase
{
   private Unmarshaller unmarshaller;
   private ObjectModelFactory factory;

   static
   {
      if (PluginsAccess.getPlugins() == null)
      {
         PluginsAccess.register(new TestPlugins());
      }
   }

   protected void setUp() throws Exception
   {
      unmarshaller = UnmarshallerFactory.newInstance().newUnmarshaller();
      factory = new ProducerConfigurationFactory();
      unmarshaller.setEntityResolver(new TestEntityResolver());
   }

   protected ProducerConfiguration getProducerConfiguration(URL location) throws JBossXBException, IOException
   {
      Object o = unmarshaller.unmarshal(location.openStream(), factory, null);
      assertNotNull(o);
      assertTrue(o instanceof ProducerConfiguration);
      return (ProducerConfiguration)o;
   }
}
