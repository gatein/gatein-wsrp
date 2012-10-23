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

import junit.framework.TestCase;
import org.gatein.registration.RegistrationPolicy;
import org.gatein.registration.RegistrationPolicyChangeListener;
import org.gatein.registration.RegistrationPropertyChangeListener;
import org.gatein.registration.policies.DefaultRegistrationPolicy;
import org.gatein.registration.policies.DefaultRegistrationPropertyValidator;
import org.gatein.registration.policies.RegistrationPolicyWrapper;
import org.gatein.wsrp.WSRPConstants;
import org.gatein.wsrp.api.plugins.PluginsAccess;
import org.gatein.wsrp.producer.config.impl.ProducerRegistrationRequirementsImpl;
import org.gatein.wsrp.registration.PropertyDescription;
import org.gatein.wsrp.registration.RegistrationPropertyDescription;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class ProducerRegistrationRequirementsTestCase extends TestCase
{
   static
   {
      if (PluginsAccess.getPlugins() == null)
      {
         PluginsAccess.register(new TestPlugins());
      }
   }

   public void testSetRegistrationProperties()
   {
      ProducerRegistrationRequirements requirements = new ProducerRegistrationRequirementsImpl();
      requirements.addEmptyRegistrationProperty("foo");
      requirements.addEmptyRegistrationProperty("bar");

      final Collection<RegistrationPropertyDescription> expected = new ArrayList<RegistrationPropertyDescription>();
      expected.add(new RegistrationPropertyDescription("newFoo", WSRPConstants.XSD_STRING));

      long lastModified = requirements.getLastModified();

      requirements.addRegistrationPropertyChangeListener(new RegistrationPropertyChangeListener()
      {
         public void propertiesHaveChanged(Map<QName, ? extends PropertyDescription> newRegistrationProperties)
         {
            assertEquals(expected.size(), newRegistrationProperties.size());
            assertTrue(expected.containsAll(newRegistrationProperties.values()));
         }
      });

      requirements.setRegistrationProperties(expected);

      assertTrue(requirements.getLastModified() > lastModified);
   }

   public void testChangeRegistrationPolicy()
   {
      ProducerRegistrationRequirementsImpl requirements = new ProducerRegistrationRequirementsImpl();
      requirements.setRegistrationRequired(true); // so that we load the policy

      RegistrationPolicy policy = requirements.getPolicy();
      assertTrue(policy.isWrapped());
      assertFalse(policy instanceof DefaultRegistrationPolicy); // cannot use instanceof since policy is wrapped
      assertEquals(ProducerRegistrationRequirements.DEFAULT_POLICY_CLASS_NAME, policy.getClassName());

      requirements.reloadPolicyFrom("org.gatein.wsrp.producer.config.TestRegistrationPolicy", ProducerRegistrationRequirements.DEFAULT_VALIDATOR_CLASS_NAME);

      policy = requirements.getPolicy();
      assertTrue(policy.isWrapped());
      assertFalse(policy instanceof TestRegistrationPolicy); // cannot use instanceof since policy is wrapped
      assertEquals("org.gatein.wsrp.producer.config.TestRegistrationPolicy", requirements.getPolicyClassName());
      assertEquals("org.gatein.wsrp.producer.config.TestRegistrationPolicy", policy.getClassName());
      assertNull(requirements.getValidatorClassName());
   }

   public void testChangeToDefaultPolicyWithEmptyValidatorName()
   {
      ProducerRegistrationRequirementsImpl requirements = new ProducerRegistrationRequirementsImpl();
      requirements.setRegistrationRequired(true); // so that we load the policy

      requirements.reloadPolicyFrom(ProducerRegistrationRequirements.DEFAULT_POLICY_CLASS_NAME, "");

      RegistrationPolicy policy = requirements.getPolicy();
      assertEquals(ProducerRegistrationRequirements.DEFAULT_POLICY_CLASS_NAME, policy.getClassName());
      assertEquals(ProducerRegistrationRequirements.DEFAULT_VALIDATOR_CLASS_NAME, requirements.getValidatorClassName());

      DefaultRegistrationPolicy unwrap = (DefaultRegistrationPolicy)RegistrationPolicyWrapper.unwrap(policy);
      assertTrue(unwrap.getValidator() instanceof DefaultRegistrationPropertyValidator);
   }

   public void testSetUnchangedRegistrationProperties()
   {
      ProducerRegistrationRequirements requirements = new ProducerRegistrationRequirementsImpl();
      requirements.addEmptyRegistrationProperty("foo");

      final Collection<RegistrationPropertyDescription> expected = new ArrayList<RegistrationPropertyDescription>();
      expected.add(new RegistrationPropertyDescription("foo", WSRPConstants.XSD_STRING));

      long lastModified = requirements.getLastModified();

      requirements.addRegistrationPropertyChangeListener(new RegistrationPropertyChangeListener()
      {
         public void propertiesHaveChanged(Map<QName, ? extends PropertyDescription> newRegistrationProperties)
         {
            fail("Shouldn't have been called!");
         }
      });

      requirements.setRegistrationProperties(expected);

      assertEquals(lastModified, requirements.getLastModified());
   }

   public void testSetRegistrationPropertiesPropertyRemoval()
   {
      ProducerRegistrationRequirements requirements = new ProducerRegistrationRequirementsImpl();
      requirements.addEmptyRegistrationProperty("foo");

      final Collection<RegistrationPropertyDescription> expected = Collections.emptyList();

      long lastModified = requirements.getLastModified();

      requirements.addRegistrationPropertyChangeListener(new RegistrationPropertyChangeListener()
      {
         public void propertiesHaveChanged(Map<QName, ? extends PropertyDescription> newRegistrationProperties)
         {
            assertEquals(expected.size(), newRegistrationProperties.size());
            assertTrue(expected.containsAll(newRegistrationProperties.values()));
         }
      });

      requirements.setRegistrationProperties(expected);

      assertTrue(requirements.getLastModified() > lastModified);
   }

   public void testReloadSamePolicy()
   {
      ProducerRegistrationRequirements requirements = new ProducerRegistrationRequirementsImpl();

      // load policy the first time
      requirements.reloadPolicyFrom(ProducerRegistrationRequirements.DEFAULT_POLICY_CLASS_NAME, ProducerRegistrationRequirements.DEFAULT_VALIDATOR_CLASS_NAME);

      // add listener that shouldn't been called since policy hasn't changed
      requirements.addRegistrationPolicyChangeListener(new RegistrationPolicyChangeListener()
      {
         public void policyUpdatedTo(RegistrationPolicy policy)
         {
            fail("Shouldn't have been called!");
         }
      });

      // try reloading policy
      requirements.reloadPolicyFrom(ProducerRegistrationRequirements.DEFAULT_POLICY_CLASS_NAME, ProducerRegistrationRequirements.DEFAULT_VALIDATOR_CLASS_NAME);
   }

   public void testSetRegistrationRequired()
   {
      ProducerRegistrationRequirements requirements = new ProducerRegistrationRequirementsImpl();

      requirements.setRegistrationRequired(true);
      assertTrue(requirements.isRegistrationRequired());

      requirements.addRegistrationPropertyChangeListener(new RegistrationPropertyChangeListener()
      {
         public void propertiesHaveChanged(Map<QName, ? extends PropertyDescription> newRegistrationProperties)
         {
            fail("Shouldn't have been called!");
         }
      });

      requirements.setRegistrationRequired(true);
   }

   public void testRemoveProperty()
   {
      ProducerRegistrationRequirements requirements = new ProducerRegistrationRequirementsImpl();
      requirements.addEmptyRegistrationProperty("foo");

      long lastModified = requirements.getLastModified();

      requirements.removeRegistrationProperty("foo");

      requirements.addRegistrationPropertyChangeListener(new RegistrationPropertyChangeListener()
      {
         public void propertiesHaveChanged(Map<QName, ? extends PropertyDescription> newRegistrationProperties)
         {
            assertTrue(newRegistrationProperties.isEmpty());
         }
      });

      assertTrue(requirements.getLastModified() > lastModified);
   }

}
