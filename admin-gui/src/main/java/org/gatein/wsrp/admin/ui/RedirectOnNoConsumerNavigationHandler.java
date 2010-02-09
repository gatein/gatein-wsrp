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

import javax.faces.application.NavigationHandler;
import javax.faces.context.FacesContext;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class RedirectOnNoConsumerNavigationHandler extends NavigationHandler
{
   private NavigationHandler base;
   private static final String CONFIGURE_CONSUMER = "configureConsumer";
   private static final String CONSUMERS = "consumers";

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

         // if not, check the session...
         if (currentConsumer == null)
         {
            currentConsumer = (String)JSFBeanContext.getSessionMap(facesContext).get(ConsumerManagerBean.SESSION_CONSUMER_ID);

            // if we still don't have consumer id, redirect to consumer list view
            if (currentConsumer == null)
            {
               outcome = CONSUMERS;
            }
         }
      }

      base.handleNavigation(facesContext, fromAction, outcome);
   }
}
