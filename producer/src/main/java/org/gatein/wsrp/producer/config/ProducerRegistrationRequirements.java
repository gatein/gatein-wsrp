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

import org.gatein.registration.RegistrationPolicy;
import org.gatein.registration.RegistrationPolicyChangeListener;
import org.gatein.registration.RegistrationPropertyChangeListener;
import org.gatein.wsrp.registration.RegistrationPropertyDescription;
import org.gatein.wsrp.registration.ValueChangeListener;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision:5865 $
 * @since 2.6
 */
public interface ProducerRegistrationRequirements extends ValueChangeListener
{
   String DEFAULT_POLICY_CLASS_NAME = "org.gatein.registration.policies.DefaultRegistrationPolicy";
   String DEFAULT_VALIDATOR_CLASS_NAME = "org.gatein.registration.policies.DefaultRegistrationPropertyValidator";

   boolean isRegistrationRequired();

   void setRegistrationRequired(boolean requiresRegistration);

   boolean isRegistrationRequiredForFullDescription();

   void setRegistrationRequiredForFullDescription(boolean fullServiceDescriptionRequiresRegistration);

   Map<QName, RegistrationPropertyDescription> getRegistrationProperties();

   void addRegistrationProperty(RegistrationPropertyDescription propertyDescription);

   RegistrationPropertyDescription removeRegistrationProperty(String propertyName);

   RegistrationPropertyDescription removeRegistrationProperty(QName propertyName);

   void clearRegistrationProperties();

   boolean acceptValueFor(QName propertyName, Object value);

   boolean acceptValueFor(String propertyName, Object value);

   RegistrationPropertyDescription getRegistrationPropertyWith(String name);

   RegistrationPropertyDescription getRegistrationPropertyWith(QName name);

   void notifyRegistrationPropertyChangeListeners();

   void addRegistrationPropertyChangeListener(RegistrationPropertyChangeListener listener);

   void clearRegistrationPropertyChangeListeners();

   void removeRegistrationPropertyChangeListener(RegistrationPropertyChangeListener listener);

   void setPolicy(RegistrationPolicy policy);

   RegistrationPolicy getPolicy();

   void reloadPolicyFrom(String policyClassName, String validatorClassName);

   /**
    * @param propertyDescription
    * @param oldName
    * @since 2.6.3
    */
   void propertyHasBeenRenamed(RegistrationPropertyDescription propertyDescription, QName oldName);

   /**
    * @param name
    * @since 2.6.3
    */
   RegistrationPropertyDescription addEmptyRegistrationProperty(String name);

   /**
    * @param listener
    * @since 2.6.3
    */
   void addRegistrationPolicyChangeListener(RegistrationPolicyChangeListener listener);

   /**
    * @param listener
    * @since 2.6.3
    */
   void removeRegistrationPolicyChangeListener(RegistrationPolicyChangeListener listener);

   /** @since 2.6.3 */
   void clearRegistrationPolicyChangeListeners();

   Set<RegistrationPropertyChangeListener> getPropertyChangeListeners();

   Set<RegistrationPolicyChangeListener> getPolicyChangeListeners();

   long getLastModified();

   void setLastModified(long lastModified);

   void setRegistrationProperties(Collection<RegistrationPropertyDescription> registrationProperties);

   String getPolicyClassName();

   List<String> getAvailableRegistrationPolicies();

   List<String> getAvailableRegistrationPropertyValidators();
}
