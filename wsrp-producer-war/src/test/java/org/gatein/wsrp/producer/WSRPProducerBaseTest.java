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
import org.gatein.pc.api.Portlet;
import org.gatein.pc.api.PortletContext;
import org.gatein.pc.portlet.container.managed.LifeCycleStatus;
import org.gatein.pc.portlet.container.managed.ManagedObjectLifeCycleEvent;
import org.gatein.pc.portlet.container.managed.ManagedPortletApplication;
import org.gatein.pc.portlet.container.managed.ManagedPortletContainer;
import org.gatein.registration.RegistrationException;
import org.gatein.wsrp.producer.config.ProducerRegistrationRequirements;
import org.gatein.wsrp.producer.handlers.processors.ProducerHelper;
import org.mockito.Mockito;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:boleslaw.dawidowicz@jboss.org">Boleslaw Dawidowicz</a>
 * @version $Revision: 8808 $
 */
public abstract class WSRPProducerBaseTest extends TestCase
{
   protected Map<String, List<String>> war2Handles = new HashMap<String, List<String>>(7);
   protected String currentlyDeployedArchiveName;

   protected WSRPProducerBaseTest(String name) throws Exception
   {
      super(name);
   }

   protected abstract WSRPProducer getProducer();

   protected abstract ProducerHelper getProducerHelper();

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

         WSRPProducer producer = getProducer();
         Set<Portlet> portlets = producer.getPortletInvoker().getPortlets();
         for (Portlet portlet : portlets)
         {
            // trigger management events so that the service description is properly updated
            org.gatein.pc.api.PortletContext context = portlet.getContext();
            if (!war2Handles.containsKey(getWarName(context.getId())))
            {
               ManagedPortletApplication portletApplication = Mockito.mock(ManagedPortletApplication.class);
               PortletContext.PortletContextComponents components = context.getComponents();
               Mockito.stub(portletApplication.getId()).toReturn(components.getApplicationName());

               ManagedPortletContainer portletContainer = Mockito.mock(ManagedPortletContainer.class);
               Mockito.stub(portletContainer.getManagedPortletApplication()).toReturn(portletApplication);
               Mockito.stub(portletContainer.getId()).toReturn(components.getPortletName());
               Mockito.stub(portletContainer.getInfo()).toReturn(portlet.getInfo());

               ManagedObjectLifeCycleEvent lifeCycleEvent = Mockito.mock(ManagedObjectLifeCycleEvent.class);
               Mockito.stub(lifeCycleEvent.getManagedObject()).toReturn(portletContainer);
               Mockito.stub(lifeCycleEvent.getStatus()).toReturn(LifeCycleStatus.STARTED);

               producer.onEvent(lifeCycleEvent);
            }
         }

         currentlyDeployedArchiveName = warFileName;

         if (!war2Handles.containsKey(warFileName))
         {
            Collection<String> portletHandles = getPortletHandles();
            if (portletHandles != null)
            {
               for (String handle : portletHandles)
               {
                  String warName = getWarName(handle);
                  if (warName.equals(warFileName))
                  {
                     List<String> handles = war2Handles.get(warName);
                     if (handles == null)
                     {
                        handles = new ArrayList<String>(3);
                        war2Handles.put(warName, handles);
                     }

                     handles.add(handle);
                  }
               }
            }
            else
            {
               throw new IllegalArgumentException(warFileName + " didn't contain any portlets...");
            }
         }
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
         currentlyDeployedArchiveName = null;

         List<String> handles = war2Handles.get(warFileName);
         WSRPProducer producer = getProducer();
         if (handles != null)
         {
            for (String handle : handles)
            {
               // trigger management events so that the service description is properly updated
               PortletContext context = PortletContext.createPortletContext(handle);

               try
               {
                  Portlet portlet = producer.getPortletInvoker().getPortlet(context);
                  ManagedPortletApplication portletApplication = Mockito.mock(ManagedPortletApplication.class);
                  PortletContext.PortletContextComponents components = context.getComponents();
                  Mockito.stub(portletApplication.getId()).toReturn(components.getApplicationName());

                  ManagedPortletContainer portletContainer = Mockito.mock(ManagedPortletContainer.class);
                  Mockito.stub(portletContainer.getManagedPortletApplication()).toReturn(portletApplication);
                  Mockito.stub(portletContainer.getId()).toReturn(components.getPortletName());
                  Mockito.stub(portletContainer.getInfo()).toReturn(portlet.getInfo());

                  ManagedObjectLifeCycleEvent lifeCycleEvent = Mockito.mock(ManagedObjectLifeCycleEvent.class);
                  Mockito.stub(lifeCycleEvent.getManagedObject()).toReturn(portletContainer);
                  Mockito.stub(lifeCycleEvent.getStatus()).toReturn(LifeCycleStatus.STOPPED);

                  producer.onEvent(lifeCycleEvent);
               }
               catch (Exception e)
               {
                  // do nothing the portlet is already undeployed
               }
            }
         }

         // only remove the mapping if we're not undeploying the most used portlet (optimization, as it avoids parsing the SD)
         if (removeCurrent(warFileName))
         {
            war2Handles.remove(warFileName);
         }

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
         throw new Exception("Could not find the jboss.undeploy.url.prefix system property.");
      }
   }

   protected abstract boolean removeCurrent(String archiveName);

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
      getProducerHelper().reset();
   }

   public void tearDown() throws Exception
   {
      resetRegistrationInfo();
      super.tearDown();
   }

   protected String getWarName(String handle)
   {
      org.gatein.pc.api.PortletContext context = org.gatein.pc.api.PortletContext.createPortletContext(handle);
      return context.getComponents().getApplicationName() + ".war";
   }

   protected abstract Collection<String> getPortletHandles() throws Exception;
}
