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

package org.gatein.wsrp.admin.ui;

import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.application.NavigationCase;
import javax.faces.application.NavigationHandler;
import javax.faces.context.FacesContext;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class RedirectOnNoConsumerNavigationHandler extends ConfigurableNavigationHandler
{
   private NavigationHandler base;
   private static final String CONFIGURE_CONSUMER = "configureConsumer";
   private static final String CONSUMERS = "consumers";

   /** must match ConsumerManagerBean in faces-config.xml */
   private static final String CONSUMERS_MGR = "consumersMgr";

   public RedirectOnNoConsumerNavigationHandler(NavigationHandler base)
   {
      this.base = base;
   }

   public void handleNavigation(FacesContext facesContext, String fromAction, String outcome)
   {
      // only check for need to redirect when we're asking for consumer details
      if (CONFIGURE_CONSUMER.equals(outcome))
      {
         // check if we have a currently selected consumer in the request...
         String currentConsumer = JSFBeanContext.getParameter(ConsumerManagerBean.REQUESTED_CONSUMER_ID, facesContext);

         // if we still don't have consumer id, redirect to consumer list view
         if (currentConsumer == null)
         {
            outcome = CONSUMERS;
         }
      }
      else if (CONSUMERS.equals(outcome))
      {
         // ensure that state is properly reset by calling ConsumerManagerBean.listConsumers()
         ConsumerManagerBean consumersMgr = (ConsumerManagerBean)JSFBeanContext.getSessionMap(facesContext).get(CONSUMERS_MGR);
         consumersMgr.listConsumers();
      }

      base.handleNavigation(facesContext, fromAction, outcome);
   }

   @Override
   public NavigationCase getNavigationCase(FacesContext context, String fromAction, String outcome)
   {
      return (base instanceof ConfigurableNavigationHandler) ? ((ConfigurableNavigationHandler)base).getNavigationCase(context, fromAction, outcome) : null;
   }

   @Override
   public Map<String, Set<NavigationCase>> getNavigationCases()
   {
      return (base instanceof ConfigurableNavigationHandler) ? ((ConfigurableNavigationHandler)base).getNavigationCases() : null;
   }
}
