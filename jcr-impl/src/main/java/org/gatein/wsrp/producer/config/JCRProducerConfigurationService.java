/*
 * JBoss, a division of Red Hat
 * Copyright 2011, Red Hat Middleware, LLC, and individual
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

package org.gatein.wsrp.producer.config;

import org.chromattic.api.ChromatticSession;
import org.gatein.common.util.ParameterValidation;
import org.gatein.wsrp.jcr.ChromatticPersister;
import org.gatein.wsrp.producer.config.impl.AbstractProducerConfigurationService;
import org.gatein.wsrp.producer.config.impl.xml.SimpleXMLProducerConfigurationService;
import org.gatein.wsrp.producer.config.mapping.ProducerConfigurationMapping;
import org.gatein.wsrp.producer.config.mapping.RegistrationRequirementsMapping;
import org.gatein.wsrp.registration.mapping.RegistrationPropertyDescriptionMapping;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class JCRProducerConfigurationService extends AbstractProducerConfigurationService
{
   private static String PRODUCER_CONFIGURATION_PATH = ProducerConfigurationMapping.NODE_NAME;

   private InputStream defaultConfigurationIS;
   private ChromatticPersister persister;

   public static final List<Class> mappingClasses = new ArrayList<Class>(3);

   static
   {
      Collections.addAll(mappingClasses, ProducerConfigurationMapping.class, RegistrationRequirementsMapping.class,
         RegistrationPropertyDescriptionMapping.class);
   }

   public JCRProducerConfigurationService(ChromatticPersister persister) throws Exception
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(persister, "ChromatticPersister");
      this.persister = persister;
   }

   /** @param is  */
   public void setConfigurationIS(InputStream is)
   {
      this.defaultConfigurationIS = is;
   }

   protected void loadConfiguration() throws Exception
   {
      // Try loading configuration from JCR first
      ChromatticSession session = persister.getSession();
      ProducerConfigurationMapping pcm = session.findByPath(ProducerConfigurationMapping.class, PRODUCER_CONFIGURATION_PATH);

      // if we don't have a configuration persisted in JCR already, force a reload from XML and save the resulting configuration
      if (pcm == null)
      {
         pcm = session.insert(ProducerConfigurationMapping.class, PRODUCER_CONFIGURATION_PATH);

         ProducerConfigurationService service = new SimpleXMLProducerConfigurationService(defaultConfigurationIS);

         service.reloadConfiguration();
         configuration = service.getConfiguration();
         pcm.initFrom(configuration);
      }
      else
      {
         configuration = pcm.toModel(null, this);
      }


      persister.closeSession(true);
   }

   public void saveConfiguration() throws Exception
   {
      ChromatticSession session = persister.getSession();

      ProducerConfigurationMapping pcm = session.findByPath(ProducerConfigurationMapping.class, PRODUCER_CONFIGURATION_PATH);
      if (pcm == null)
      {
         pcm = session.insert(ProducerConfigurationMapping.class, PRODUCER_CONFIGURATION_PATH);
      }
      pcm.initFrom(configuration);

      persister.closeSession(true);
   }
}
