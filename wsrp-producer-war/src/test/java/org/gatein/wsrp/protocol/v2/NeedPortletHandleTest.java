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

import org.gatein.wsrp.WSRPConstants;
import org.gatein.wsrp.WSRPTypeFactory;
import org.oasis.wsrp.v2.GetMarkup;
import org.oasis.wsrp.v2.GetResource;
import org.oasis.wsrp.v2.GetServiceDescription;
import org.oasis.wsrp.v2.InteractionParams;
import org.oasis.wsrp.v2.InvalidRegistration;
import org.oasis.wsrp.v2.MarkupParams;
import org.oasis.wsrp.v2.MarkupResponse;
import org.oasis.wsrp.v2.OperationFailed;
import org.oasis.wsrp.v2.PerformBlockingInteraction;
import org.oasis.wsrp.v2.PortletContext;
import org.oasis.wsrp.v2.PortletDescription;
import org.oasis.wsrp.v2.ResourceParams;
import org.oasis.wsrp.v2.RuntimeContext;
import org.oasis.wsrp.v2.ServiceDescription;
import org.oasis.wsrp.v2.StateChange;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public abstract class NeedPortletHandleTest extends V2ProducerBaseTest
{
   private String mostUsedPortletWARFileName;


   public NeedPortletHandleTest(String portletWARFileName)
      throws Exception
   {
      this("NeedPortletHandleTest", portletWARFileName);
      this.mostUsedPortletWARFileName = portletWARFileName;
   }

   protected NeedPortletHandleTest(String name, String portletWARFileName)
      throws Exception
   {
      super(name);
      this.mostUsedPortletWARFileName = portletWARFileName;
   }

   protected String getDefaultHandle()
   {
      return getFirstHandleFor(mostUsedPortletWARFileName);
   }

   /**
    * @param archiveName
    * @return
    * @since 2.6.3
    */
   private String getFirstHandleFor(String archiveName)
   {
      return war2Handles.get(archiveName).get(0);
   }

   protected String getHandleForCurrentlyDeployedArchive()
   {
      return getFirstHandleFor(currentlyDeployedArchiveName);
   }

   /**
    * @return
    * @since 2.6.3
    */
   protected List<String> getHandlesForCurrentlyDeployedArchive()
   {
      return war2Handles.get(currentlyDeployedArchiveName);
   }

   @Override
   protected boolean removeCurrent(String archiveName)
   {
      return !mostUsedPortletWARFileName.equals(archiveName);
   }

   public void setUp() throws Exception
   {
      super.setUp();
      this.mostUsedPortletWARFileName = getMostUsedPortletWARFileName();
      deploy(mostUsedPortletWARFileName);

      // reset strict mode
      producer.usingStrictModeChangedTo(true);
   }

   /**
    * Sub-classes need to implement this method to return the local WAR file name of the portlet being deployed most of
    * the time in the context of the test case. This is required since we cannot rely on the name to be properly set at
    * all time via the constructor.
    *
    * @return
    */
   protected abstract String getMostUsedPortletWARFileName();

   public void tearDown() throws Exception
   {
      undeploy(mostUsedPortletWARFileName);
      super.tearDown();
   }

   /**
    * Creates a valid Markup request.
    *
    * @return a basic, valid GetMarkup object representing the markup request
    */
   protected GetMarkup createMarkupRequestForCurrentlyDeployedPortlet() throws Exception
   {
      return createMarkupRequest(getHandleForCurrentlyDeployedArchive());
   }

   /**
    * @param handle
    * @return
    * @throws RemoteException
    * @throws InvalidRegistration
    * @throws OperationFailed
    * @since 2.6.3
    */
   protected GetMarkup createMarkupRequest(String handle) throws RemoteException, InvalidRegistration, OperationFailed
   {
      GetMarkup getMarkup = createDefaultGetMarkup(handle);
      getMarkup.getMarkupParams().getMarkupCharacterSets().add(WSRPConstants.DEFAULT_CHARACTER_SET);

      return getMarkup;
   }

   protected GetMarkup createDefaultGetMarkup(String handle)
   {
      PortletContext portletContext = WSRPTypeFactory.createPortletContext(handle);
      RuntimeContext runtimeContext = createDefaultRuntimeContext();
      return WSRPTypeFactory.createGetMarkup(null, portletContext, runtimeContext, null, createDefaultMarkupParams());
   }

   protected PerformBlockingInteraction createDefaultPerformBlockingInteraction(String handle)
   {
      PortletContext portletContext = WSRPTypeFactory.createPortletContext(handle);
      return WSRPTypeFactory.createPerformBlockingInteraction(null, portletContext, createDefaultRuntimeContext(), null, createDefaultMarkupParams(),
         createDefaultInteractionParams());
   }
   
   protected GetResource createGetResource(String archiveName, String resourceID)
   {
      PortletContext portletContext = WSRPTypeFactory.createPortletContext(getFirstHandleFor(archiveName));
      RuntimeContext runtimeContext = createDefaultRuntimeContext();
      return WSRPTypeFactory.createGetResource(null, portletContext, runtimeContext, null, createResourceParams(resourceID));
   }
   
   protected ResourceParams createResourceParams(String resourceID)
   {
      return WSRPTypeFactory.createDefaultResourceParams(resourceID);
   }

   protected InteractionParams createDefaultInteractionParams()
   {
      return WSRPTypeFactory.createInteractionParams(StateChange.READ_ONLY);
   }

   protected RuntimeContext createDefaultRuntimeContext()
   {
      return WSRPTypeFactory.createRuntimeContext(WSRPConstants.NONE_USER_AUTHENTICATION, "foo", "ns");
   }

   protected MarkupParams createDefaultMarkupParams()
   {
      return WSRPTypeFactory.createMarkupParams(false, WSRPConstants.getDefaultLocales(), WSRPConstants.getDefaultMimeTypes(),
         WSRPConstants.VIEW_MODE, WSRPConstants.NORMAL_WINDOW_STATE);
   }

   protected String getPortletHandleFrom(String partialHandle)
   {
      List<String> handles = getHandlesForCurrentlyDeployedArchive();
      for (String handle : handles)
      {
         if (handle.contains(partialHandle))
         {
            return handle;
         }
      }

      throw new IllegalArgumentException("Couldn't find a portlet handle matching '" + partialHandle + "' in " + currentlyDeployedArchiveName);
   }

   protected GetMarkup createMarkupRequest() throws Exception
   {
      return createMarkupRequestForCurrentlyDeployedPortlet();
   }

   protected void checkSessionForCurrentlyDeployedPortlet(MarkupResponse response)
      throws RemoteException, InvalidRegistration, OperationFailed
   {
      // We don't send any portlet session information, just user cookies... The producer takes care of the details
      // What this means, though is that we don't have access to individual portlet session ids... so we can only
      // check that we get a cookie... Not very test-friendly...
      /*ProducerSessionInformation sessionInfo = RequestHeaderClientHandler.getCurrentProducerSessionInformation();
      ExtendedAssert.assertNotNull(sessionInfo);
      ExtendedAssert.assertTrue(sessionInfo.getUserCookie().lastIndexOf("JSESSIONID") != -1);

      // Check that we are not sending sessionID's
      SessionContext sessionContext = response.getSessionContext();
      ExtendedAssert.assertNull(sessionContext);*/
   }
}

