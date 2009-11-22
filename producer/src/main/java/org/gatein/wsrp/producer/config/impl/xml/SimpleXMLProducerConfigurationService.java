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

package org.gatein.wsrp.producer.config.impl.xml;

import org.gatein.wsrp.producer.config.ProducerConfiguration;
import org.gatein.wsrp.producer.config.impl.AbstractProducerConfigurationService;
import org.jboss.xb.binding.ObjectModelFactory;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;

import java.io.InputStream;

/**
 * A simple configuration service that only supports manually edits to an external XML configuration file. Reloading and
 * saving modifications are therefore not supported.
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 12276 $
 * @since 2.6
 */
public class SimpleXMLProducerConfigurationService extends AbstractProducerConfigurationService
{
   protected InputStream inputStream;

   public SimpleXMLProducerConfigurationService()
   {
   }

   public SimpleXMLProducerConfigurationService(InputStream inputStream)
   {
      this.inputStream = inputStream;
   }

   public void loadConfiguration() throws Exception
   {
      Unmarshaller unmarshaller = UnmarshallerFactory.newInstance().newUnmarshaller();
      ObjectModelFactory factory = new ProducerConfigurationFactory();
      configuration = (ProducerConfiguration)unmarshaller.unmarshal(inputStream, factory, null);
   }

   public void saveConfiguration() throws Exception
   {
      throw new UnsupportedOperationException("saveConfiguration is not supported!");
   }
}
