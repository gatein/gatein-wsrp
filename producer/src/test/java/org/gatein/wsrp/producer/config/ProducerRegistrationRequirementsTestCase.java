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

package org.gatein.wsrp.producer.config;

import junit.framework.TestCase;
import org.gatein.registration.RegistrationPolicy;
import org.gatein.registration.RegistrationPolicyChangeListener;
import org.gatein.registration.RegistrationPropertyChangeListener;
import org.gatein.wsrp.WSRPConstants;
import org.gatein.wsrp.producer.config.impl.ProducerRegistrationRequirementsImpl;
import org.gatein.wsrp.registration.PropertyDescription;
import org.gatein.wsrp.registration.RegistrationPropertyDescription;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class ProducerRegistrationRequirementsTestCase extends TestCase
{
   public void testSetRegistrationProperties()
   {
      ProducerRegistrationRequirements requirements = new ProducerRegistrationRequirementsImpl();
      requirements.addEmptyRegistrationProperty("foo");
      requirements.addEmptyRegistrationProperty("bar");

      final Map<QName, RegistrationPropertyDescription> expected = new HashMap<QName, RegistrationPropertyDescription>();
      RegistrationPropertyDescription newFoo = new RegistrationPropertyDescription("newFoo", WSRPConstants.XSD_STRING);
      expected.put(newFoo.getName(), newFoo);

      requirements.addRegistrationPropertyChangeListener(new RegistrationPropertyChangeListener()
      {
         public void propertiesHaveChanged(Map<QName, ? extends PropertyDescription> newRegistrationProperties)
         {
            assertEquals(expected, newRegistrationProperties);
         }
      });

      requirements.setRegistrationProperties(expected);
   }

   public void testSetUnchangedRegistrationProperties()
   {
      ProducerRegistrationRequirements requirements = new ProducerRegistrationRequirementsImpl();
      requirements.addEmptyRegistrationProperty("foo");

      final Map<QName, RegistrationPropertyDescription> expected = new HashMap<QName, RegistrationPropertyDescription>();
      RegistrationPropertyDescription newFoo = new RegistrationPropertyDescription("foo", WSRPConstants.XSD_STRING);
      expected.put(newFoo.getName(), newFoo);

      requirements.addRegistrationPropertyChangeListener(new RegistrationPropertyChangeListener()
      {
         public void propertiesHaveChanged(Map<QName, ? extends PropertyDescription> newRegistrationProperties)
         {
            fail("Shouldn't have been called!");
         }
      });

      requirements.setRegistrationProperties(expected);
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

}
