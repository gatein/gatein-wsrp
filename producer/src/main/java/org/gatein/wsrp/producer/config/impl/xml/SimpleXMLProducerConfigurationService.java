/*
 * JBoss, a division of Red Hat
 * Copyright 2009, Red Hat Middleware, LLC, and individual
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

package org.gatein.wsrp.producer.config.impl.xml;

import org.gatein.wsrp.producer.config.ProducerConfiguration;
import org.gatein.wsrp.producer.config.impl.AbstractProducerConfigurationService;
import org.gatein.wsrp.producer.config.impl.ProducerConfigurationImpl;
import org.jboss.xb.binding.ObjectModelFactory;
import org.jboss.xb.binding.ObjectModelProvider;
import org.jboss.xb.binding.Unmarshaller;
import org.jboss.xb.binding.UnmarshallerFactory;
import org.jboss.xb.binding.XercesXsMarshaller;
import org.jboss.xb.binding.sunday.unmarshalling.DefaultSchemaResolver;
import org.xml.sax.SAXException;

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
import java.net.MalformedURLException;
import java.net.URL;

/**
 * A simple configuration service that only supports manually edits to an external XML configuration file. Reloading and
 * saving modifications are therefore not supported.
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 12276 $
 * @since 2.6
 */
public class SimpleXMLProducerConfigurationService extends AbstractProducerConfigurationService
{
   protected static final String PRODUCER_NAMESPACE = "http://www.gatein.org/xml/ns/gatein_wsrp_producer_1_0";
   protected static final String PRODUCER_XSD = "gatein_wsrp_producer_1_0.xsd";
   protected static DefaultSchemaResolver RESOLVER;
   protected InputStream inputStream;
   private static File tmp;

   static
   {
      RESOLVER = new DefaultSchemaResolver();
      RESOLVER.setCacheResolvedSchemas(true);
      RESOLVER.addSchemaLocation("http://www.w3.org/XML/1998/namespace", "xsd/xml.xsd");
      RESOLVER.addSchemaLocation(PRODUCER_NAMESPACE, "xsd/" + PRODUCER_XSD);

      try
      {
         tmp = File.createTempFile("producer-configuration", "xml");
         tmp.deleteOnExit();
      }
      catch (IOException e)
      {
         throw new RuntimeException(e);
      }
   }

   public SimpleXMLProducerConfigurationService()
   {
   }

   public SimpleXMLProducerConfigurationService(InputStream inputStream)
   {
      this.inputStream = inputStream;
   }

   public void setInputStream(InputStream inputStream)
   {
      this.inputStream = inputStream;
   }

   public void loadConfiguration() throws Exception
   {
      if (inputStream != null)
      {
         Unmarshaller unmarshaller = UnmarshallerFactory.newInstance().newUnmarshaller();
         ObjectModelFactory factory = new ProducerConfigurationFactory();
         configuration = (ProducerConfiguration)unmarshaller.unmarshal(inputStream, factory, null);
      }

      if (configuration == null)
      {
         configuration = new ProducerConfigurationImpl();
      }
   }

   public void saveConfiguration() throws Exception
   {
      saveConfigurationTo(tmp);
   }

   public URL getConfigurationURL()
   {
      try
      {
         return tmp.toURI().toURL();
      }
      catch (MalformedURLException e)
      {
         throw new RuntimeException(e);
      }
   }

   protected void saveConfigurationTo(File config) throws IOException, SAXException, ParserConfigurationException
   {
      // get the output writer to write the XML content
      StringWriter xmlOutput = new StringWriter();

      // get the XML Schema source
      InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("xsd/" + PRODUCER_XSD);

      Reader xsReader = new InputStreamReader(is);

      // create an instance of XML Schema marshaller
      //      MarshallerImpl marshaller = new MarshallerImpl(); // todo: replace XercesXsMarshaller by this
      XercesXsMarshaller marshaller = new XercesXsMarshaller();

      marshaller.setSchemaResolver(RESOLVER);

      // we need to specify what elements are top most (roots) providing namespace URI, prefix and local name
      marshaller.addRootElement(PRODUCER_NAMESPACE, "", "producer-configuration");

      // declare default namespace
      marshaller.declareNamespace("wpc", PRODUCER_NAMESPACE);

      // add schema location by declaring xsi namespace and adding xsi:schemaLocation attribute
      marshaller.declareNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
      marshaller.addAttribute("xsi", "schemaLocation", "string",
         PRODUCER_NAMESPACE + " http://www.jboss.org/portal/xsd/" + PRODUCER_XSD);

      // create an instance of Object Model Provider
      ObjectModelProvider provider = new ProducerConfigurationProvider();

      marshaller.setProperty("org.jboss.xml.binding.marshalling.indent", "true");
      marshaller.marshal(xsReader, provider, configuration, xmlOutput);

      // close XML Schema reader
      xsReader.close();

      config.createNewFile();
      Writer configFile = new BufferedWriter(new FileWriter(config));
      configFile.write(xmlOutput.toString());
      configFile.flush();
      configFile.close();
   }
}
