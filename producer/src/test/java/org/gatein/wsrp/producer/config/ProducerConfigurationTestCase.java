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
import org.gatein.registration.policies.DefaultRegistrationPolicy;
import org.gatein.registration.policies.DefaultRegistrationPropertyValidator;
import org.gatein.registration.policies.RegistrationPolicyWrapper;
import org.gatein.registration.policies.RegistrationPropertyValidator;
import org.gatein.wsrp.WSRPConstants;
import org.gatein.wsrp.producer.config.impl.ProducerConfigurationImpl;
import org.gatein.wsrp.producer.config.impl.xml.ProducerConfigurationProvider;
import org.gatein.wsrp.registration.LocalizedString;
import org.gatein.wsrp.registration.RegistrationPropertyDescription;
import org.jboss.xb.binding.ObjectModelProvider;
import org.jboss.xb.binding.XercesXsMarshaller;
import org.jboss.xb.binding.sunday.unmarshalling.DefaultSchemaResolver;
import org.xml.sax.SAXException;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.Locale;
import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 10408 $
 * @since 2.6
 */
public abstract class ProducerConfigurationTestCase extends TestCase
{

   private static DefaultSchemaResolver RESOLVER;

   static
   {
      RESOLVER = new DefaultSchemaResolver();
      RESOLVER.setCacheResolvedSchemas(true);
      RESOLVER.addSchemaLocation("http://www.w3.org/XML/1998/namespace", "xsd/xml.xsd");
      RESOLVER.addSchemaLocation("http://www.gatein.org/xml/ns/gatein_wsrp_producer_1_0", "xsd/gatein_wsrp_producer_1_0.xsd");
   }

   public void testCustomPolicyUnmarshalling() throws Exception
   {
      ProducerConfiguration producerConfiguration = getProducerConfiguration("custom-policy.xml");
      assertTrue(producerConfiguration.isUsingStrictMode());

      ProducerRegistrationRequirements requirements = producerConfiguration.getRegistrationRequirements();
      assertNotNull(requirements);
      RegistrationPolicy policy = requirements.getPolicy();

      // check that the policy is properly wrapped
      assertTrue(policy.isWrapped());

      // policy is wrapped so instanceof won't work!
      assertFalse(policy instanceof TestRegistrationPolicy);

      // and that the delegate is indeed the expected policy
      assertEquals(TestRegistrationPolicy.class, RegistrationPolicyWrapper.unwrap(policy).getClass());
   }

   public void testExtendedUnmarshalling() throws Exception
   {
      ProducerConfiguration producerConfiguration = getProducerConfiguration("extended.xml");
      assertTrue(producerConfiguration.isUsingStrictMode());

      ProducerRegistrationRequirements requirements = producerConfiguration.getRegistrationRequirements();
      assertNotNull(requirements);
      RegistrationPolicy policy = RegistrationPolicyWrapper.unwrap(requirements.getPolicy());
      assertTrue(policy instanceof DefaultRegistrationPolicy);
      RegistrationPropertyValidator propertyValidator = ((DefaultRegistrationPolicy)policy).getValidator();
      assertNotNull(propertyValidator);
      assertTrue(propertyValidator instanceof DefaultRegistrationPropertyValidator);
      assertTrue(requirements.isRegistrationRequired());
      assertTrue(requirements.isRegistrationRequiredForFullDescription());
      Map properties = requirements.getRegistrationProperties();
      assertNotNull(properties);
      assertEquals(2, properties.size());

      checkRegistrationProperty(requirements, 1);
      checkRegistrationProperty(requirements, 2);
   }

   public void testMinimalRegistrationUnmarshalling() throws Exception
   {
      ProducerConfiguration producerConfiguration = getProducerConfiguration("minimal-registration.xml");
      assertTrue(producerConfiguration.isUsingStrictMode());

      ProducerRegistrationRequirements requirements = producerConfiguration.getRegistrationRequirements();
      assertNotNull(requirements);
      assertTrue(requirements.isRegistrationRequired());
      assertTrue(!requirements.isRegistrationRequiredForFullDescription());
      Map properties = requirements.getRegistrationProperties();
      assertNotNull(properties);
      assertTrue(properties.isEmpty());
      assertNotNull(requirements.getPolicy());
   }

   public void testMinimalUnmarshalling() throws Exception
   {
      ProducerConfiguration producerConfiguration = getProducerConfiguration("minimal.xml");
      assertTrue(producerConfiguration.isUsingStrictMode());

      ProducerRegistrationRequirements requirements = producerConfiguration.getRegistrationRequirements();
      assertNotNull(requirements);
      assertFalse(requirements.isRegistrationRequired());
      assertFalse(requirements.isRegistrationRequiredForFullDescription());
      assertTrue(requirements.getRegistrationProperties().isEmpty());
      assertNull(requirements.getPolicy());
   }

   public void testInvalidMultipleRegistrationConfiguration() throws Exception
   {
      try
      {
         getProducerConfiguration("invalid2.xml");
         fail("Only one registration-configuration element allowed!");
      }
      catch (Exception expected)
      {
      }
   }

   public void testInvalidTypeValue() throws Exception
   {
      try
      {
         getProducerConfiguration("invalid-type.xml");
         fail("Invalid type for property description should fail.");
      }
      catch (Exception expected)
      {
      }
   }

   public void testInvalidPropertyValidator()
   {
      try
      {
         getProducerConfiguration("invalid3.xml");
         fail("Doesn't make sense to define a property validator if a registration policy other than DefaultRegistrationPolicy");
      }
      catch (Exception expected)
      {
      }
   }

   public void testInvalidFullServiceDescriptionValue()
   {
      try
      {
         getProducerConfiguration("invalid1.xml");
         fail("Invalid value for fullServiceDescriptionRequiresRegistration should have caused failure");
      }
      catch (Exception expected)
      {
      }
   }

   public void testUseStrictMode() throws Exception
   {
      ProducerConfiguration producerConfiguration = getProducerConfiguration("strict-mode.xml");
      assertFalse(producerConfiguration.isUsingStrictMode());
   }

   public void testChangeListeners() throws Exception
   {
      ProducerConfiguration producerConfiguration = getProducerConfiguration("minimal.xml");
      assertTrue(producerConfiguration.isUsingStrictMode());

      TestProducerConfigurationChangeListener listener = new TestProducerConfigurationChangeListener();
      producerConfiguration.addChangeListener(listener);
      assertFalse(listener.called);

      // we're not changing the value, the listeners shouldn't be called
      producerConfiguration.setUsingStrictMode(true);
      assertFalse(listener.called);

      producerConfiguration.setUsingStrictMode(false);
      assertFalse(producerConfiguration.isUsingStrictMode());
      assertTrue(listener.called);
   }

   public void testSaveAndReload() throws Exception
   {
      ProducerConfiguration configuration = new ProducerConfigurationImpl();
      configuration.setUsingStrictMode(false);
      ProducerRegistrationRequirements registrationRequirements = configuration.getRegistrationRequirements();
      registrationRequirements.setRegistrationRequiredForFullDescription(true);
      registrationRequirements.setRegistrationRequired(true);
      String prop1 = "prop1";
      registrationRequirements.addEmptyRegistrationProperty(prop1);
      registrationRequirements.getRegistrationPropertyWith(prop1).setDefaultLabel("label1");
      String prop2 = "prop2";
      registrationRequirements.addEmptyRegistrationProperty(prop2);
      registrationRequirements.getRegistrationPropertyWith(prop2).setDefaultHint("hint2");
      String prop3 = "prop3";
      registrationRequirements.addEmptyRegistrationProperty(prop3);
      registrationRequirements.getRegistrationPropertyWith(prop3).setDefaultDescription("description3");
      String prop4 = "prop4";
      registrationRequirements.addEmptyRegistrationProperty(prop4);
      RegistrationPropertyDescription propDesc4 = registrationRequirements.getRegistrationPropertyWith(prop4);
      propDesc4.setDefaultLabel("label4");
      propDesc4.setDefaultHint("hint4");
      propDesc4.setDefaultDescription("description4");

      File tmp = File.createTempFile("producer-configuration-test-case", "xml");
      tmp.deleteOnExit();

      writeConfigToFile(configuration, tmp);

      configuration = getProducerConfiguration(tmp.toURI().toURL());

      assertFalse(configuration.isUsingStrictMode());

      registrationRequirements = configuration.getRegistrationRequirements();
      assertTrue(registrationRequirements.isRegistrationRequired());
      assertTrue(registrationRequirements.isRegistrationRequiredForFullDescription());

      assertEquals(4, registrationRequirements.getRegistrationProperties().size());
      assertEquals("label1", registrationRequirements.getRegistrationPropertyWith(prop1).getLabel().getValue());
      assertEquals("hint2", registrationRequirements.getRegistrationPropertyWith(prop2).getHint().getValue());
      assertEquals("description3", registrationRequirements.getRegistrationPropertyWith(prop3).getDescription().getValue());
      propDesc4 = registrationRequirements.getRegistrationPropertyWith(prop4);
      assertEquals("label4", propDesc4.getLabel().getValue());
      assertEquals("hint4", propDesc4.getHint().getValue());
      assertEquals("description4", propDesc4.getDescription().getValue());
   }

   private void writeConfigToFile(ProducerConfiguration configuration, File file)
      throws IOException, ParserConfigurationException, SAXException
   {
      StringWriter xmlOutput = new StringWriter();

      // get the XML Schema source
      InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("xsd/gatein_wsrp_producer_1_0.xsd");

      Reader xsReader = new InputStreamReader(is);

      // create an instance of XML Schema marshaller
      XercesXsMarshaller marshaller = new XercesXsMarshaller();

      marshaller.setSchemaResolver(RESOLVER);

      // we need to specify what elements are top most (roots) providing namespace URI, prefix and local name
      marshaller.addRootElement("http://www.gatein.org/xml/ns/gatein_wsrp_producer_1_0", "", "producer-configuration");

      // declare default namespace
      marshaller.declareNamespace("wpc", "http://www.gatein.org/xml/ns/gatein_wsrp_producer_1_0");

      // add schema location by declaring xsi namespace and adding xsi:schemaLocation attribute
      marshaller.declareNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
      marshaller.addAttribute("xsi", "schemaLocation", "string",
         "http://www.gatein.org/xml/ns/gatein_wsrp_producer_1_0 http://www.jboss.org/portal/xsd/gatein_wsrp_producer_1_0.xsd");

      // create an instance of Object Model Provider
      ObjectModelProvider provider = new ProducerConfigurationProvider();

      marshaller.setProperty("org.jboss.xml.binding.marshalling.indent", "true");
      marshaller.marshal(xsReader, provider, configuration, xmlOutput);

      // close XML Schema reader
      xsReader.close();

      file.createNewFile();
      Writer configFile = new BufferedWriter(new FileWriter(file));
      configFile.write(xmlOutput.toString());
      configFile.flush();
      configFile.close();
   }

   protected ProducerConfiguration getProducerConfiguration(String fileName) throws Exception
   {
      URL location = Thread.currentThread().getContextClassLoader().getResource(fileName);
      assertNotNull(location);
      System.out.println("location = " + location);

      return getProducerConfiguration(location);
   }

   protected abstract ProducerConfiguration getProducerConfiguration(URL location) throws Exception;

   private void checkRegistrationProperty(ProducerRegistrationRequirements requirements, int index)
   {
      RegistrationPropertyDescription desc = requirements.getRegistrationPropertyWith("name" + index);
      assertNotNull(desc);
      assertEquals(new QName("name" + index), desc.getName());
      assertEquals(WSRPConstants.XSD_STRING, desc.getType());

      LocalizedString localizedString = new LocalizedString("hint" + index, Locale.ENGLISH);
      localizedString.setResourceName("resource.hint" + index);
      localizedString.setValue("hint" + index);
      assertEquals(localizedString, desc.getHint());

      localizedString = new LocalizedString("label" + index, Locale.ENGLISH);
      localizedString.setResourceName("resource.label" + index);
      localizedString.setValue("label" + index);
      assertEquals(localizedString, desc.getLabel());
   }

   private static class TestProducerConfigurationChangeListener implements ProducerConfigurationChangeListener
   {
      boolean called = false;

      public void usingStrictModeChangedTo(boolean strictMode)
      {
         called = true;
      }
   }
}
