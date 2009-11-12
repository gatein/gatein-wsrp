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
//      info = new EndpointConfigurationInfo(); // todo fix me
   }

   public void testSetURLs() throws InvokerUnavailableException
   {
      // default state is to use WSDL
      assertTrue(info.usesWSDL());


      info.setServiceDescriptionURL(url);
      assertEquals(url, info.getServiceDescriptionURL());

      // changing the URLs should switch to not using WSDL anymore...
      assertFalse(info.usesWSDL());
      try
      {
         info.getServiceFactory();
         fail("Missing markup URL: service factory should not be initialized");
      }
      catch (IllegalStateException expected)
      {
      }

      info.setMarkupURL(url);
      assertFalse(info.usesWSDL());
      assertNotNull(info.getServiceFactory());
      assertEquals(url, info.getServiceFactory().getServiceDescriptionURL());
      assertEquals(url, info.getServiceFactory().getMarkupURL());
      assertTrue(info.getServiceFactory().isAvailable());
   }

   public void testSetWSDLURL() throws InvokerUnavailableException
   {
      assertTrue(info.usesWSDL());

      // todo fix me

      /*info.setServiceDescriptionURL(url);
      info.setMarkupURL(url);
      assertTrue(info.getServiceFactory() instanceof PerEndpointSOAPInvokerServiceFactory);
      assertFalse(info.usesWSDL());

      String bea = "http://wsrp.bea.com:7001/producer/producer?WSDL";
      info.setWsdlDefinitionURL(bea);
      assertEquals(bea, info.getWsdlDefinitionURL());
      assertTrue(info.getServiceFactory() instanceof RemoteSOAPInvokerServiceFactory);
      assertEquals(bea, ((RemoteSOAPInvokerServiceFactory)info.getServiceFactory()).getWsdlDefinitionURL());
      assertTrue(info.usesWSDL());

      info.setMarkupURL(url);
      assertEquals(url, info.getMarkupURL());
      assertEquals(url, info.getServiceFactory().getMarkupURL());
      assertFalse(info.usesWSDL());*/
   }

   public void testSetInvalidWSDLURL()
   {
      info.setWsdlDefinitionURL(url);
      assertTrue(info.usesWSDL());
      assertEquals(url, info.getWsdlDefinitionURL());
   }

   public void testSetNullWSDLURL()
   {
      info.setServiceDescriptionURL(url);
      info.setMarkupURL(url);

      // it should be possible to set the WSDL to null for Hibernate
      info.setWsdlDefinitionURL(null);

      assertFalse(info.usesWSDL());
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

   public void testRefresh() throws Exception
   {
      assertTrue(info.isRefreshNeeded());
      assertFalse(info.isAvailable());

      // change the service factory to a fake one to be able to simulate access to endpoint
//      info.setServiceFactory(new BehaviorBackedServiceFactory()); //todo
      info.refresh();
      assertFalse(info.isRefreshNeeded());
      assertTrue(info.isAvailable());

      info.setServiceDescriptionURL(url);
      assertTrue(info.isRefreshNeeded());

      info.getRegistrationService();
      assertTrue(info.isRefreshNeeded());

      info.getServiceDescriptionService();
      assertFalse(info.isRefreshNeeded());
   }

   public void testGetServiceFactory() throws Exception
   {
      assertTrue(info.isRefreshNeeded());
      assertFalse(info.isAvailable());

      // change the service factory to a fake one to be able to simulate access to endpoint
//      info.setServiceFactory(new BehaviorBackedServiceFactory()); // todo
      ServiceFactory factory = info.getServiceFactory();
      assertNotNull(factory);
      assertFalse(info.isRefreshNeeded());
      assertTrue(info.isAvailable());
      assertTrue(factory.isAvailable());
   }
}
