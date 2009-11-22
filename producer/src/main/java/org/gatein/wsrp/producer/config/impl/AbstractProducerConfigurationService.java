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

package org.gatein.wsrp.producer.config.impl;

import org.gatein.registration.RegistrationPolicyChangeListener;
import org.gatein.registration.RegistrationPropertyChangeListener;
import org.gatein.wsrp.producer.config.ProducerConfiguration;
import org.gatein.wsrp.producer.config.ProducerConfigurationChangeListener;
import org.gatein.wsrp.producer.config.ProducerConfigurationService;
import org.gatein.wsrp.producer.config.ProducerRegistrationRequirements;

import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public abstract class AbstractProducerConfigurationService implements ProducerConfigurationService
{
   protected ProducerConfiguration configuration;

   public ProducerConfiguration getConfiguration()
   {
      return configuration;
   }

   public void loadConfiguration() throws Exception
   {
      // save listeners if we already have a configuration
      List<ProducerConfigurationChangeListener> listeners = null;
      Set<RegistrationPolicyChangeListener> policyListeners = null;
      Set<RegistrationPropertyChangeListener> propertyListeners = null;
      ProducerRegistrationRequirements registrationRequirements;
      if (configuration != null)
      {
         listeners = configuration.getChangeListeners();
         registrationRequirements = configuration.getRegistrationRequirements();
         if (registrationRequirements != null)
         {
            policyListeners = registrationRequirements.getPolicyChangeListeners();
            propertyListeners = registrationRequirements.getPropertyChangeListeners();
         }
      }

      // reload
      reloadConfiguration();

      // restore listeners
      if (listeners != null)
      {
         for (ProducerConfigurationChangeListener listener : listeners)
         {
            configuration.addChangeListener(listener);
         }
      }
      registrationRequirements = configuration.getRegistrationRequirements();
      if (registrationRequirements != null)
      {
         if (propertyListeners != null)
         {
            for (RegistrationPropertyChangeListener listener : propertyListeners)
            {
               registrationRequirements.addRegistrationPropertyChangeListener(listener);
            }
         }
         if (policyListeners != null)
         {
            for (RegistrationPolicyChangeListener listener : policyListeners)
            {
               registrationRequirements.addRegistrationPolicyChangeListener(listener);
            }
         }
      }
   }
}
