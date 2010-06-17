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

package org.gatein.wsrp.producer;

import junit.framework.TestCase;
import org.gatein.registration.RegistrationException;
import org.gatein.wsrp.producer.config.ProducerRegistrationRequirements;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author <a href="mailto:boleslaw.dawidowicz@jboss.org">Boleslaw Dawidowicz</a>
 * @version $Revision: 8808 $
 */
public abstract class WSRPProducerBaseTest extends TestCase
{
   protected WSRPProducerBaseTest(String name) throws Exception
   {
      super(name);
   }

   protected abstract WSRPProducer getProducer();

   public void deploy(String warFileName) throws Exception
   {
      String deployURLPrefix = System.getProperty("jboss.deploy.url.prefix");
      if (deployURLPrefix != null)
      {
         File archiveDirectory = getDirectory("test.deployables.dir");
         File archiveFile = getArchive(warFileName, archiveDirectory);

         String deployURLString = deployURLPrefix + archiveFile.getAbsolutePath();

         URL deployURL = new URL(deployURLString);
         URLConnection connection = deployURL.openConnection();

         BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
         reader.readLine();
         reader.close();
      }
      else
      {
         throw new Exception("Could not find the jboss.deploy.url.prefix system property.");
      }
   }

   public void undeploy(String warFileName) throws Exception
   {
      String undeployURLPrefix = System.getProperty("jboss.undeploy.url.prefix");
      if (undeployURLPrefix != null)
      {
         File archiveDirectory = getDirectory("test.deployables.dir");
         File archiveFile = getArchive(warFileName, archiveDirectory);

         String undeployURLString = undeployURLPrefix + archiveFile.getAbsolutePath();

         URL undeployURL = new URL(undeployURLString);
         URLConnection connection = undeployURL.openConnection();

         BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
         reader.readLine();
         reader.close();
      }
      else
      {
         throw new Exception("Could not find the jboss.deploy.url.prefix system property.");
      }
   }

   protected void resetRegistrationInfo() throws RegistrationException
   {
      WSRPProducer producer = getProducer();
      ProducerRegistrationRequirements registrationRequirements = producer.getConfigurationService().getConfiguration().getRegistrationRequirements();
      registrationRequirements.setRegistrationRequired(false);
      registrationRequirements.clearRegistrationProperties();
      registrationRequirements.clearRegistrationPropertyChangeListeners();
      producer.getRegistrationManager().clear();
      registrationRequirements.removeRegistrationPropertyChangeListener(producer.getRegistrationManager());
   }

   private File getDirectory(String property) throws Exception
   {
      String deployableProperty = System.getProperty(property);
      if (deployableProperty != null)
      {
         File deployableDir = new File(deployableProperty);
         if (deployableDir.exists() && deployableDir.isDirectory())
         {
            return deployableDir;
         }
         else
         {
            throw new Error("Found a system property for \'" + property + "\' [" + deployableProperty + "] but value does not correspond to a directory.");
         }
      }
      else
      {
         throw new Error("Could not find the system property \'" + property + "\' cannot deploy test archives.");
      }
   }


   private File getArchive(String fileName, File deployDirectory) throws Exception
   {
      if (fileName != null && deployDirectory != null && deployDirectory.exists() && deployDirectory.isDirectory())
      {
         File archiveFile = new File(deployDirectory.getAbsoluteFile() + File.separator + fileName);
         if (archiveFile.exists())
         {
            return archiveFile;
         }
         else
         {
            throw new Exception("Archive " + fileName + " in directory " + deployDirectory + " does not exist. Cannot deploy this file");
         }
      }
      else
      {
         throw new Exception("Cannot find archive to deploy. Archive name [" + fileName + "] is null or the deploy directory + [" + deployDirectory + "] is not a directory");
      }
   }

   public void setUp() throws Exception
   {
      super.setUp();

      resetRegistrationInfo();
   }

   public void tearDown() throws Exception
   {
      resetRegistrationInfo();
      super.tearDown();
   }
}
