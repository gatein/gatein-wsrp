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

package org.gatein.wsrp.producer.config.impl;

import org.gatein.common.util.ParameterValidation;
import org.gatein.wsrp.producer.config.ProducerConfiguration;
import org.gatein.wsrp.producer.config.ProducerConfigurationChangeListener;
import org.gatein.wsrp.producer.config.ProducerRegistrationRequirements;
import org.oasis.wsrp.v2.CookieProtocol;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 12017 $
 * @since 2.6
 */
public class ProducerConfigurationImpl implements ProducerConfiguration
{
   private ProducerRegistrationRequirements requirements;

   // use strict mode by default
   private boolean strictMode = true;

   private List<ProducerConfigurationChangeListener> listeners = new ArrayList<ProducerConfigurationChangeListener>(7);

   private int sessionExpirationTime = DEFAULT_SESSION_EXPIRATION_TIME;

   private CookieProtocol requiresInitCookie = CookieProtocol.NONE;

   public ProducerRegistrationRequirements getRegistrationRequirements()
   {
      if (requirements == null)
      {
         requirements = new ProducerRegistrationRequirementsImpl(false, false, false);
      }

      return requirements;
   }

   public boolean isUsingStrictMode()
   {
      return strictMode;
   }

   public void setUsingStrictMode(boolean strict)
   {
      if (strictMode != strict)
      {
         strictMode = strict;
         for (ProducerConfigurationChangeListener listener : listeners)
         {
            listener.usingStrictModeChangedTo(strict);
         }
      }
   }

   public void addChangeListener(ProducerConfigurationChangeListener listener)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(listener, "ProducerConfigurationChangeListener");
      listeners.add(listener);
   }

   public void removeChangeListener(ProducerConfigurationChangeListener listener)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(listener, "ProducerConfigurationChangeListener");
      listeners.remove(listener);
   }

   public List<ProducerConfigurationChangeListener> getChangeListeners()
   {
      return listeners;
   }

   @Override
   public void setRegistrationRequirements(ProducerRegistrationRequirements requirements)
   {
      this.requirements = requirements;
   }

   public CookieProtocol getRequiresInitCookie()
   {
      return requiresInitCookie;
   }

   public void setRequiresInitCookie(CookieProtocol requiresInitCookie)
   {
      this.requiresInitCookie = requiresInitCookie;
   }

   public int getSessionExpirationTime()
   {
      return sessionExpirationTime;
   }

   public void setSessionExpirationTime(int sessionExpirationTime)
   {
      this.sessionExpirationTime = sessionExpirationTime;
   }
}
