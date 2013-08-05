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

package org.gatein.wsrp.consumer;

import junit.framework.TestCase;
import org.gatein.pc.api.InvokerUnavailableException;
import org.gatein.wsrp.handler.RequestHeaderClientHandler;
import org.gatein.wsrp.services.SOAPServiceFactory;
import org.gatein.wsrp.services.ServiceFactory;
import org.gatein.wsrp.test.protocol.v2.BehaviorBackedServiceFactory;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

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
      RequestHeaderClientHandler.resetCurrentInfo(); // make sure we don't have information that persists across tests
   }

   public void testSetWSDLURL() throws InvokerUnavailableException
   {
      String bea = "http://wsrp.bea.com:7001/producer/producer?WSDL";
      info.setWsdlDefinitionURL(bea);
      assertEquals(bea, info.getWsdlDefinitionURL());
   }

   public void testWSRPVersion() throws InvokerUnavailableException
   {
      // use a "real" service factory for this test
      info = new EndpointConfigurationInfo(new SOAPServiceFactory());

      info.setWsdlDefinitionURL(getWSDLURL("wsdl/simplev2.wsdl"));
      info.forceRefresh();
      assertEquals(ServiceFactory.WSRP2, info.getWSRPVersion());

      info.setWsdlDefinitionURL(getWSDLURL("wsdl/simplev1.wsdl"));
      info.forceRefresh();
      assertEquals(ServiceFactory.WSRP1, info.getWSRPVersion());
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
      assertTrue(info.refresh());
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
   }


   public void testAllWSDLURLs()
   {
      assertTrue(info.getAllWSDLURLs().isEmpty());

      final String wsrp2 = getWSDLURL("wsdl/simplev2.wsdl");
      final String wsrp1 = getWSDLURL("wsdl/simplev1.wsdl");

      info.setWsdlDefinitionURL(wsrp2);
      assertTrue(Arrays.equals(new String[]{wsrp2}, info.getAllWSDLURLs().toArray()));

      String wsdlDefinitionURL = wsrp2 + " " + wsrp1;
      info.setWsdlDefinitionURL(wsdlDefinitionURL);
      assertTrue(Arrays.equals(new String[]{wsrp2, wsrp1}, info.getAllWSDLURLs().toArray()));
      assertEquals(wsdlDefinitionURL, info.getWsdlDefinitionURL());
      assertEquals(wsrp2, info.getEffectiveWSDLURL());

      // we change the WSDL URLs but since the factory associated with wsrp2 is still valid, it's still the one that should be used
      wsdlDefinitionURL = wsrp1 + "    \t   \n " + wsrp2;
      info.setWsdlDefinitionURL(wsdlDefinitionURL);
      assertTrue(Arrays.equals(new String[]{wsrp1, wsrp2}, info.getAllWSDLURLs().toArray()));
      assertEquals(wsdlDefinitionURL, info.getWsdlDefinitionURL());
      assertEquals(wsrp2, info.getEffectiveWSDLURL());

      // now we remove wsrp2 from the URLs, wsrp1 should now be used
      final String missing = getWSDLURL("wsdl/missing-mandatory.wsdl");
      wsdlDefinitionURL = wsrp1 + " " + missing;
      info.setWsdlDefinitionURL(wsdlDefinitionURL);
      assertTrue(Arrays.equals(new String[]{wsrp1, missing}, info.getAllWSDLURLs().toArray()));
      assertEquals(wsdlDefinitionURL, info.getWsdlDefinitionURL());
      assertEquals(wsrp1, info.getEffectiveWSDLURL());
   }

   public void testSingleURLWithSpacesInString()
   {
      final String wsrp2 = getWSDLURL("wsdl/simplev2.wsdl");

      info.setWsdlDefinitionURL("\t    \n" + wsrp2 + "         \n ");
      assertEquals(wsrp2, info.getWsdlDefinitionURL());
      final List<String> allWSDLURLs = info.getAllWSDLURLs();
      assertEquals(1, allWSDLURLs.size());
      assertTrue(Arrays.equals(new String[]{wsrp2}, allWSDLURLs.toArray()));
   }

   public void testInitialState()
   {
      // use a "real" service factory for this test
      info = new EndpointConfigurationInfo(new SOAPServiceFactory());

      try
      {
         info.getServiceFactory();
         fail();
      }
      catch (RuntimeException e)
      {
         // expected
      }
   }

   public void testFailover() throws Exception
   {
      // use a "real" service factory for this test
      info = new EndpointConfigurationInfo(new SOAPServiceFactory());

      final String missing = getWSDLURL("wsdl/missing-mandatory.wsdl");
      final String wsrp2 = getWSDLURL("wsdl/simplev2.wsdl");

      final String wsdlDefinitionURL = missing + " " + wsrp2;
      info.setWsdlDefinitionURL(wsdlDefinitionURL);
      assertEquals(wsdlDefinitionURL, info.getWsdlDefinitionURL());
      assertNotNull(info.getServiceDescriptionService());
      assertEquals(wsrp2, info.getEffectiveWSDLURL());
   }

   public void testFailoverDoesNotLoopInfinitely() throws Exception
   {
      // use a "real" service factory for this test
      info = new EndpointConfigurationInfo(new SOAPServiceFactory());

      final String missing = getWSDLURL("wsdl/missing-mandatory.wsdl");

      // set timeout to keep test short :)
      info.setWSOperationTimeOut(1000);

      final String wsdlDefinitionURL = missing + " " + missing;
      info.setWsdlDefinitionURL(wsdlDefinitionURL);
      assertEquals(wsdlDefinitionURL, info.getWsdlDefinitionURL());
      try
      {
         info.getServiceDescriptionService();
         fail();
      }
      catch (Exception e)
      {
         // expected
      }

   }

   private String getWSDLURL(String fileName)
   {
      URL url = Thread.currentThread().getContextClassLoader().getResource(fileName);
      return url.toExternalForm();
   }
}
