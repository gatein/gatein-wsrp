/*
* JBoss, a division of Red Hat
* Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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

package org.gatein.wsrp.consumer;

import junit.framework.TestCase;
import org.gatein.pc.api.InvokerUnavailableException;
import org.gatein.wsrp.services.ServiceFactory;
import org.gatein.wsrp.test.support.BehaviorBackedServiceFactory;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 8893 $
 * @since 2.6
 */
public class EndpointConfigurationInfoTestCase extends TestCase
{
   private EndpointConfigurationInfo info;
   private String url = "http://www.example.com/";

   protected void setUp() throws Exception
   {
      info = new EndpointConfigurationInfo(new BehaviorBackedServiceFactory());
   }

   public void testSetWSDLURL() throws InvokerUnavailableException
   {
      String bea = "http://wsrp.bea.com:7001/producer/producer?WSDL";
      info.setWsdlDefinitionURL(bea);
      assertEquals(bea, info.getWsdlDefinitionURL());
   }

   /**
    * Setting the WSDL URL shouldn't trigger an attempt to retrieve the associated WSDL so it should be possible to
    * provide a URL that doesn't correspond to a valid WSDL location without triggering an error until a refresh
    */
   public void testSetWSDLURLDoesNotTriggerWSDLRetrieval()
   {
      info.setWsdlDefinitionURL(url);
      assertEquals(url, info.getWsdlDefinitionURL());
   }

   public void testRefreshWSDL() throws Exception
   {
      assertTrue(info.isRefreshNeeded());
      assertFalse(info.isAvailable());

      String bea = "http://wsrp.bea.com:7001/producer/producer?WSDL";
      info.setWsdlDefinitionURL(bea);
      info.refresh();
      assertFalse(info.isRefreshNeeded());
      assertTrue(info.isAvailable());
   }

   public void testGetRemoteHost()
   {
      String bea = "http://wsrp.bea.com:7001/producer/producer?WSDL";
      info.setWsdlDefinitionURL(bea);

      assertEquals("http://wsrp.bea.com:7001", info.getRemoteHostAddress());
   }

   public void testGetServiceFactory() throws Exception
   {
      assertTrue(info.isRefreshNeeded());
      assertFalse(info.isAvailable());

      ServiceFactory factory = info.getServiceFactory();
      assertNotNull(factory);
      assertFalse(info.isRefreshNeeded());
      assertTrue(info.isAvailable());
      assertTrue(factory.isAvailable());
   }
}
