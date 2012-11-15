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

package org.gatein.wsrp.producer.config.mapping;

import org.chromattic.api.annotations.MappedBy;
import org.chromattic.api.annotations.OneToOne;
import org.chromattic.api.annotations.Owner;
import org.chromattic.api.annotations.PrimaryType;
import org.chromattic.api.annotations.Property;
import org.gatein.wsrp.jcr.mapping.BaseMapping;
import org.gatein.wsrp.jcr.mapping.mixins.LastModifiedMixinHolder;
import org.gatein.wsrp.producer.config.ProducerConfiguration;
import org.gatein.wsrp.producer.config.ProducerConfigurationService;
import org.gatein.wsrp.producer.config.ProducerRegistrationRequirements;
import org.gatein.wsrp.producer.config.impl.ProducerConfigurationImpl;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
@PrimaryType(name = ProducerConfigurationMapping.NODE_NAME)
public abstract class ProducerConfigurationMapping extends LastModifiedMixinHolder implements BaseMapping<ProducerConfiguration, ProducerConfigurationService>
{
   public static final String NODE_NAME = "wsrp:producerconfiguration";

   @Property(name = "strictmode")
   public abstract boolean getUsingStrictMode();

   public abstract void setUsingStrictMode(boolean strict);

   @OneToOne
   @Owner
   @MappedBy("registrationrequirements")
   public abstract RegistrationRequirementsMapping getRegistrationRequirements();

   @Override
   public void initFrom(ProducerConfiguration configuration)
   {
      setUsingStrictMode(configuration.isUsingStrictMode());

      setLastModified(configuration.getLastModified());

      RegistrationRequirementsMapping rrm = getRegistrationRequirements();
      rrm.initFrom(configuration.getRegistrationRequirements());
   }

   @Override
   public ProducerConfiguration toModel(ProducerConfiguration initial, ProducerConfigurationService registry)
   {
      ProducerConfiguration configuration = initial;
      if (initial == null)
      {
         configuration = new ProducerConfigurationImpl();
      }

      configuration.setUsingStrictMode(getUsingStrictMode());
      configuration.setLastModified(getLastModified());

      ProducerRegistrationRequirements req = getRegistrationRequirements().toModel(configuration.getRegistrationRequirements(), registry);
      configuration.setRegistrationRequirements(req);

      return configuration;
   }

   @Override
   public Class<ProducerConfiguration> getModelClass()
   {
      return ProducerConfiguration.class;
   }
}
