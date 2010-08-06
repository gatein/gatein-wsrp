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

package org.gatein.wsrp.protocol.v2;

import javax.xml.namespace.QName;

import org.gatein.registration.RegistrationException;
import org.gatein.registration.RegistrationManager;
import org.gatein.registration.policies.DefaultRegistrationPolicy;
import org.gatein.registration.policies.DefaultRegistrationPropertyValidator;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.producer.ProducerHolder;
import org.gatein.wsrp.producer.WSRPProducer;
import org.gatein.wsrp.producer.WSRPProducerBaseTest;
import org.gatein.wsrp.producer.config.ProducerRegistrationRequirements;
import org.gatein.wsrp.producer.v2.WSRP2Producer;
import org.gatein.wsrp.registration.RegistrationPropertyDescription;
import org.gatein.wsrp.test.ExtendedAssert;
import org.oasis.wsrp.v2.GetServiceDescription;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class V2ProducerBaseTest extends WSRPProducerBaseTest
{
   protected WSRP2Producer producer = ProducerHolder.getProducer(true);
   
   private static final String CONSUMER = "test-consumer";

   public V2ProducerBaseTest() throws Exception
   {
      this("V2ProducerBaseTest");
   }

   protected V2ProducerBaseTest(String name) throws Exception
   {
      super(name);
   }

   @Override
   protected WSRPProducer getProducer()
   {
      return producer;
   }

   protected GetServiceDescription getNoRegistrationServiceDescriptionRequest()
   {
      GetServiceDescription gs = WSRPTypeFactory.createGetServiceDescription();
      gs.getDesiredLocales().add("en-US");
      gs.getDesiredLocales().add("en");
      return gs;
   }
   
   protected RegistrationPropertyDescription configureRegistrationSettings(boolean requiresRegistration, boolean provideUnregisteredFullDescription)
   {
      // define expected registration infos
      ProducerRegistrationRequirements registrationRequirements = producer.getConfigurationService().getConfiguration().getRegistrationRequirements();
      registrationRequirements.setRegistrationRequired(requiresRegistration);
      registrationRequirements.setRegistrationRequiredForFullDescription(!provideUnregisteredFullDescription);

      if (requiresRegistration)
      {
         // fix-me: http://jira.jboss.com/jira/browse/JBPORTAL-821
         RegistrationPropertyDescription regProp = new RegistrationPropertyDescription("regProp",
            new QName("urn:oasis:names:tc:wsrp:v1:types", "LocalizedString", "ns1"));
         regProp.setDefaultLabel("Registration Property");
         registrationRequirements.addRegistrationProperty(regProp);

         // Use default registration policy: this wiring is normally handled at the producer start, should maybe use a
         // registration policy that is automatically configured when none is provided to avoid having a null one?
         DefaultRegistrationPolicy defaultRegistrationPolicy = new DefaultRegistrationPolicy();
         defaultRegistrationPolicy.setValidator(new DefaultRegistrationPropertyValidator());
         registrationRequirements.setPolicy(defaultRegistrationPolicy);

         RegistrationManager registrationManager = producer.getRegistrationManager();
         registrationManager.setPolicy(defaultRegistrationPolicy);
         registrationRequirements.addRegistrationPropertyChangeListener(registrationManager);

         // create consumer for policy to be able to make decisions properly
         try
         {
            registrationManager.createConsumer(CONSUMER);
         }
         catch (RegistrationException e)
         {
            ExtendedAssert.fail("Couldn't create consumer. Cause: " + e.getLocalizedMessage());
         }

         return regProp;
      }
      else
      {
         return null;
      }
   }
}
