/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2006, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/

package org.gatein.wsrp.protocol.v1;

import org.gatein.wsrp.WSRPConstants;
import org.gatein.wsrp.WSRPTypeFactory;
import org.oasis.wsrp.v1.GetMarkup;
import org.oasis.wsrp.v1.GetServiceDescription;
import org.oasis.wsrp.v1.InvalidRegistration;
import org.oasis.wsrp.v1.InvalidRegistrationFault;
import org.oasis.wsrp.v1.MarkupResponse;
import org.oasis.wsrp.v1.OperationFailed;
import org.oasis.wsrp.v1.OperationFailedFault;
import org.oasis.wsrp.v1.PortletDescription;
import org.oasis.wsrp.v1.ServiceDescription;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 11547 $
 * @since 2.4
 */
public abstract class NeedPortletHandleTest extends V1ProducerBaseTest
{
   private String mostUsedPortletWARFileName;
   private String currentlyDeployedArchiveName;
   private Map<String, List<String>> war2Handles = new HashMap<String, List<String>>(7);


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

   /**
    * Each time we deploy a new archive, check to see if the service description has changed and add any new portlet
    * handles found.
    *
    * @param archiveName
    * @throws Exception
    */
   public void deploy(String archiveName) throws Exception
   {
      super.deploy(archiveName);
      currentlyDeployedArchiveName = archiveName;

      if (!war2Handles.containsKey(archiveName))
      {
         GetServiceDescription getServiceDescription = WSRPTypeFactory.createGetServiceDescription();
         ServiceDescription serviceDescription = serviceDescriptionService.getServiceDescription(getServiceDescription);
         List<PortletDescription> offered = serviceDescription.getOfferedPortlets();
         if (offered != null)
         {
            for (PortletDescription portletDescription : offered)
            {
               String handle = portletDescription.getPortletHandle();
               String warName = handle.substring(1, handle.indexOf('.')) + ".war";
               if (warName.equals(archiveName))
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
            throw new IllegalArgumentException(archiveName + " didn't contain any portlets...");
         }
      }
   }

   public void undeploy(String archiveName) throws Exception
   {
      try
      {
         super.undeploy(archiveName);
      }
      catch (Exception e)
      {
         // if an exception occurred the portlet is still probably deployed, so just exit
         e.printStackTrace();
         return;
      }

      currentlyDeployedArchiveName = null;

      // only remove the mapping if we're not undeploying the most used portlet (optimization, as it avoids parsing the SD)
      if (!mostUsedPortletWARFileName.equals(archiveName))
      {
         war2Handles.remove(archiveName);
      }
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
    * @throws InvalidRegistrationFault
    * @throws OperationFailedFault
    * @since 2.6.3
    */
   protected GetMarkup createMarkupRequest(String handle) throws RemoteException, InvalidRegistration, OperationFailed
   {
      GetMarkup getMarkup = WSRPTypeFactory.createDefaultMarkupRequest(handle);
      getMarkup.getMarkupParams().getMarkupCharacterSets().add(WSRPConstants.DEFAULT_CHARACTER_SET);

      return getMarkup;
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
