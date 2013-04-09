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
import org.gatein.wsrp.portlet.ApplicationScopeGetPortlet;
import org.gatein.wsrp.portlet.ApplicationScopeSetPortlet;
import org.gatein.wsrp.portlet.BasicPortlet;
import org.gatein.wsrp.portlet.DispatcherPortlet;
import org.gatein.wsrp.portlet.EncodeURLPortlet;
import org.gatein.wsrp.portlet.EventConsumerPortlet;
import org.gatein.wsrp.portlet.EventGeneratorPortlet;
import org.gatein.wsrp.portlet.GetLocalesPortlet;
import org.gatein.wsrp.portlet.ImplicitCloningPortlet;
import org.gatein.wsrp.portlet.MarkupPortlet;
import org.gatein.wsrp.portlet.MultiValuedPortlet;
import org.gatein.wsrp.portlet.RenderParamPortlet;
import org.gatein.wsrp.portlet.ResourceNoEncodeURLPortlet;
import org.gatein.wsrp.portlet.ResourcePortlet;
import org.gatein.wsrp.portlet.SessionPortlet;
import org.gatein.wsrp.portlet.UserContextPortlet;
import org.gatein.wsrp.producer.config.ProducerRegistrationRequirements;
import org.gatein.wsrp.producer.handlers.processors.ProducerHelper;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.OverProtocol;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.importer.ExplodedImporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.mockito.Mockito;

import java.io.File;
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

   @ArquillianResource
   private Deployer deployer;

   protected void deployArchive(String deploymentName)
   {
      deployer.deploy(deploymentName);
   }

   protected void undeployArchive(String deploymentName)
   {
      deployer.undeploy(deploymentName);
   }

   public void deploy(String warFileName) throws Exception
   {
      deployArchive(warFileName);

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

            ManagedObjectLifeCycleEvent lifeCycleEvent = new ManagedObjectLifeCycleEvent(portletContainer, LifeCycleStatus.STARTED);

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

   public void undeploy(String warFileName) throws Exception
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

               // with changes for GTNPC-86, a portlet switches from STARTED to CREATED state when it gets undeployed or stopped
               ManagedObjectLifeCycleEvent lifeCycleEvent = new ManagedObjectLifeCycleEvent(portletContainer, LifeCycleStatus.CREATED);

               producer.onEvent(lifeCycleEvent);
            }
            catch (Exception e)
            {
               //TODO: there are a lot of errors being thrown here due to the setup of the tests with automated deployment/undeployment of a 'default' archive
               //      for each test suite. This does not currently reflect the current tests and this should be fixed.
               // do nothing the portlet is already undeployed
            }
         }
      }

      // only remove the mapping if we're not undeploying the most used portlet (optimization, as it avoids parsing the SD)
      if (removeCurrent(warFileName))
      {
         war2Handles.remove(warFileName);
      }

      undeployArchive(warFileName);
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

   //TODO: move these deployment definitions somewhere else?
   @Deployment(name = "test-basic-portlet.war", managed = false)
   @OverProtocol("Servlet 2.5")
   public static WebArchive createTestBasicPortletArchive()
   {
      WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "test-basic-portlet.war");
      webArchive.merge(ShrinkWrap.create(WebArchive.class).as(ExplodedImporter.class).importDirectory("src/test/test-portlets/test-basic-portlet-war").as(WebArchive.class));
      webArchive.addClass(BasicPortlet.class);
      return webArchive;
   }

   @Deployment(name = "test-markup-portlet.war", managed = false)
   @OverProtocol("Servlet 2.5")
   public static WebArchive createTestMarkupPortletArchive()
   {
      WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "test-markup-portlet.war");
      webArchive.merge(ShrinkWrap.create(WebArchive.class).as(ExplodedImporter.class).importDirectory("src/test/test-portlets/test-markup-portlet-war").as(WebArchive.class));
      webArchive.addClass(MarkupPortlet.class);
      return webArchive;
   }

   @Deployment(name = "test-resourcenoencodeurl-portlet.war", managed = false)
   @OverProtocol("Servlet 2.5")
   public static WebArchive createTestResourceNoEncodeURLPortletArchive()
   {
      WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "test-resourcenoencodeurl-portlet.war");
      webArchive.merge(ShrinkWrap.create(WebArchive.class).as(ExplodedImporter.class).importDirectory("src/test/test-portlets/test-resourcenoencodeurl-portlet-war").as(WebArchive.class));
      webArchive.addClass(ResourceNoEncodeURLPortlet.class);
      return webArchive;
   }

   @Deployment(name = "test-applicationscope-portlet.war", managed = false)
   @OverProtocol("Servlet 2.5")
   public static WebArchive createTestApplicationScopePortletArchive()
   {
      WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "test-applicationscope-portlet.war");
      webArchive.merge(ShrinkWrap.create(WebArchive.class).as(ExplodedImporter.class).importDirectory("src/test/test-portlets/test-applicationscope-portlet-war").as(WebArchive.class));
      webArchive.addClass(ApplicationScopeSetPortlet.class);
      webArchive.addClass(ApplicationScopeGetPortlet.class);
      return webArchive;
   }

   @Deployment(name = "test-session-portlet.war", managed = false)
   @OverProtocol("Servlet 2.5")
   public static WebArchive createTestSessionPortletArchive()
   {
      WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "test-session-portlet.war");
      webArchive.merge(ShrinkWrap.create(WebArchive.class).as(ExplodedImporter.class).importDirectory("src/test/test-portlets/test-session-portlet-war").as(WebArchive.class));
      webArchive.addClass(SessionPortlet.class);
      return webArchive;
   }

   @Deployment(name = "test-dispatcher-portlet.war", managed = false)
   @OverProtocol("Servlet 2.5")
   public static WebArchive createTestDispatcherPortletArchive()
   {
      WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "test-dispatcher-portlet.war");
      webArchive.merge(ShrinkWrap.create(WebArchive.class).as(ExplodedImporter.class).importDirectory("src/test/test-portlets/test-dispatcher-portlet-war").as(WebArchive.class));
      webArchive.addClass(DispatcherPortlet.class);
      return webArchive;
   }

   @Deployment(name = "test-getlocales-portlet.war", managed = false)
   @OverProtocol("Servlet 2.5")
   public static WebArchive createTestGetLocalesPortletArchive()
   {
      WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "test-getlocales-portlet.war");
      webArchive.merge(ShrinkWrap.create(WebArchive.class).as(ExplodedImporter.class).importDirectory("src/test/test-portlets/test-getlocales-portlet-war").as(WebArchive.class));
      webArchive.addClass(GetLocalesPortlet.class);
      return webArchive;
   }

   @Deployment(name = "test-encodeurl-portlet.war", managed = false)
   @OverProtocol("Servlet 2.5")
   public static WebArchive createTestEncodeURLPortletArchive()
   {
      WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "test-encodeurl-portlet.war");
      webArchive.merge(ShrinkWrap.create(WebArchive.class).as(ExplodedImporter.class).importDirectory("src/test/test-portlets/test-encodeurl-portlet-war").as(WebArchive.class));
      webArchive.addClass(EncodeURLPortlet.class);
      return webArchive;
   }

   @Deployment(name = "test-usercontext-portlet.war", managed = false)
   @OverProtocol("Servlet 2.5")
   public static WebArchive createTestUserContextPortletArchive()
   {
      WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "test-usercontext-portlet.war");
      webArchive.merge(ShrinkWrap.create(WebArchive.class).as(ExplodedImporter.class).importDirectory("src/test/test-portlets/test-usercontext-portlet-war").as(WebArchive.class));
      webArchive.addClass(UserContextPortlet.class);
      return webArchive;
   }

   @Deployment(name = "test-multivalued-portlet.war", managed = false)
   @OverProtocol("Servlet 2.5")
   public static WebArchive createTestMultiValuedPortletArchive()
   {
      WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "test-multivalued-portlet.war");
      webArchive.merge(ShrinkWrap.create(WebArchive.class).as(ExplodedImporter.class).importDirectory("src/test/test-portlets/test-multivalued-portlet-war").as(WebArchive.class));
      webArchive.addClass(MultiValuedPortlet.class);
      return webArchive;
   }

   @Deployment(name = "test-implicitcloning-portlet.war", managed = false)
   @OverProtocol("Servlet 2.5")
   public static WebArchive createTestImplicitCloningPortletArchive()
   {
      WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "test-implicitcloning-portlet.war");
      webArchive.merge(ShrinkWrap.create(WebArchive.class).as(ExplodedImporter.class).importDirectory("src/test/test-portlets/test-implicitcloning-portlet-war").as(WebArchive.class));
      webArchive.addClass(ImplicitCloningPortlet.class);
      return webArchive;
   }

   @Deployment(name = "test-resource-portlet.war", managed = false)
   @OverProtocol("Servlet 2.5")
   public static WebArchive createTestResourcePortletArchive()
   {
      WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "test-resource-portlet.war");
      webArchive.merge(ShrinkWrap.create(WebArchive.class).as(ExplodedImporter.class).importDirectory("src/test/test-portlets/test-resource-portlet-war").as(WebArchive.class));
      webArchive.addClass(ResourcePortlet.class);
      return webArchive;
   }

   @Deployment(name = "test-renderparam-portlet.war", managed = false)
   @OverProtocol("Servlet 2.5")
   public static WebArchive createTestRenderParamPortletArchive()
   {
      WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "test-renderparam-portlet.war");
      webArchive.merge(ShrinkWrap.create(WebArchive.class).as(ExplodedImporter.class).importDirectory("src/test/test-portlets/test-renderparam-portlet-war").as(WebArchive.class));
      webArchive.addClass(RenderParamPortlet.class);
      return webArchive;
   }

   @Deployment(name = "test-events-portlet.war", managed = false)
   @OverProtocol("Servlet 2.5")
   public static WebArchive createTestEventsPortletArchive()
   {
      WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "test-events-portlet.war");
      webArchive.merge(ShrinkWrap.create(WebArchive.class).as(ExplodedImporter.class).importDirectory("src/test/test-portlets/test-events-portlet-war").as(WebArchive.class));
      webArchive.addClass(EventGeneratorPortlet.class);
      webArchive.addClass(EventConsumerPortlet.class);
      return webArchive;
   }

   @Deployment(name = "test-prp-portlet.war", managed = false)
   @OverProtocol("Servlet 2.5")
   public static WebArchive createTestPRPPortletArchive()
   {
      WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "test-prp-portlet.war");
      webArchive.merge(ShrinkWrap.create(WebArchive.class).as(ExplodedImporter.class).importDirectory("src/test/test-portlets/test-prp-portlet-war").as(WebArchive.class));
      webArchive.addClass(RenderParamPortlet.class);
      return webArchive;
   }

   @Deployment(name = "test-portletmodes-portlet.war", managed = false)
   @OverProtocol("Servlet 2.5")
   public static WebArchive createTestPortletModesPortletArchive()
   {
      WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "test-portletmodes-portlet.war");
      webArchive.merge(ShrinkWrap.create(WebArchive.class).as(ExplodedImporter.class).importDirectory("src/test/test-portlets/test-portletmodes-portlet-war").as(WebArchive.class));
      webArchive.addClass(RenderParamPortlet.class);
      return webArchive;
   }

   @Deployment(name = "test-portletstate-portlet.war", managed = false)
   @OverProtocol("Servlet 2.5")
   public static WebArchive createTestPortletStatePortletArchive()
   {
      WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "test-portletstate-portlet.war");
      webArchive.merge(ShrinkWrap.create(WebArchive.class).as(ExplodedImporter.class).importDirectory("src/test/test-portlets/test-portletstate-portlet-war").as(WebArchive.class));
      webArchive.addClass(RenderParamPortlet.class);
      return webArchive;
   }

   @Deployment(name = "google-portlet.war", managed = false)
   @OverProtocol("Servlet 2.5")
   public static WebArchive createGooglePortletArchive()
   {
      //NOTE: the order in how this is configured matters. Since arquillian cannot handle overwriting files we need to specify the MANIFEST in a dummy war first, and
      //      then merge in the actual war
      WebArchive webArchive = ShrinkWrap.create(WebArchive.class, "google-portlet.war");
      webArchive.setManifest(new File("src/test/test-portlets/google-portlet-war/META-INF/MANIFEST.MF"));

      WebArchive googlePortletArchive = ShrinkWrap.createFromZipFile(WebArchive.class, new File("target/test-archives/google-portlet.war"));
      webArchive.merge(googlePortletArchive);

      return webArchive;

   }
}
