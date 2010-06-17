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

import org.gatein.common.util.ParameterValidation;
import org.gatein.wsrp.servlet.ServletAccess;
import org.gatein.wsrp.spec.v2.WSRP2Constants;
import org.gatein.wsrp.test.ExtendedAssert;
import org.gatein.wsrp.test.support.MockHttpServletRequest;
import org.gatein.wsrp.test.support.MockHttpServletResponse;
import org.jboss.arquillian.api.Deployment;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Before;
import org.oasis.wsrp.v2.InvalidRegistration;
import org.oasis.wsrp.v2.OperationFailed;
import org.oasis.wsrp.v2.ServiceDescription;

import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class ServiceDescriptionTestCase extends V2ProducerBaseTest
{
   public ServiceDescriptionTestCase() throws Exception
   {
      super(ServiceDescriptionTestCase.class.getSimpleName());
   }

   @Deployment
   public static JavaArchive createDeployment()
   {
      return ShrinkWrap.create("test.jar", JavaArchive.class);
   }

   @Before
   public void setUp() throws Exception
   {
      if (System.getProperty("test.deployables.dir") != null)
      {
         super.setUp();
         //hack to get around having to have a httpservletrequest when accessing the producer services
         //I don't know why its really needed, seems to be a dependency where wsrp connects with the pc module
         ServletAccess.setRequestAndResponse(MockHttpServletRequest.createMockRequest(null), MockHttpServletResponse.createMockResponse());
      }
   }

   @After
   public void tearDown() throws Exception
   {
      if (System.getProperty("test.deployables.dir") != null)
      {
         super.tearDown();
      }
   }


   public void testSupportedOptions() throws OperationFailed, InvalidRegistration
   {
      ServiceDescription description = producer.getServiceDescription(getNoRegistrationServiceDescriptionRequest());

      ExtendedAssert.assertNotNull(description);
      List<String> options = description.getSupportedOptions();
      ExtendedAssert.assertTrue(ParameterValidation.existsAndIsNotEmpty(options));
      ExtendedAssert.assertTrue(options.contains(WSRP2Constants.OPTIONS_EVENTS));
      ExtendedAssert.assertTrue(options.contains(WSRP2Constants.OPTIONS_IMPORT));
      ExtendedAssert.assertTrue(options.contains(WSRP2Constants.OPTIONS_EXPORT));
   }
}
