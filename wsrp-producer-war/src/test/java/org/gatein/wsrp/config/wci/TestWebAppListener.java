/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2012, Red Hat Middleware, LLC, and individual                    *
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
package org.gatein.wsrp.config.wci;

import org.gatein.pc.portlet.impl.deployment.DeploymentException;
import org.gatein.pc.portlet.impl.deployment.PortletApplicationDeployer;
import org.gatein.wci.ServletContainerFactory;
import org.gatein.wci.WebAppEvent;
import org.gatein.wci.WebAppLifeCycleEvent;
import org.gatein.wci.WebAppListener;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class TestWebAppListener implements WebAppListener
{
   
   protected PortletApplicationDeployer portletApplicationDeployer;
   
   public TestWebAppListener()
   {
      ServletContainerFactory.getServletContainer().addWebAppListener(this);
   }

   public void setPortletApplicationDeployer(PortletApplicationDeployer portletApplicationDeployer)
   {
      this.portletApplicationDeployer = portletApplicationDeployer;
   }


   @Override
   public void onEvent(WebAppEvent event)
   {
      if (portletApplicationDeployer != null)
      {
         if (event instanceof WebAppLifeCycleEvent)
         {
            WebAppLifeCycleEvent lifeCycleEvent = (WebAppLifeCycleEvent)event;
            int type = lifeCycleEvent.getType();
            if (type == WebAppLifeCycleEvent.ADDED)
            {
               try
               {
                  portletApplicationDeployer.add(lifeCycleEvent.getWebApp().getServletContext());
               }
               catch (DeploymentException e)
               {
                  // Portlet deployment failed
                  e.printStackTrace();
               }
            }
            else if (type == WebAppLifeCycleEvent.REMOVED)
            {
               portletApplicationDeployer.remove(lifeCycleEvent.getWebApp().getServletContext());
            }
         }
      }
   }

}

