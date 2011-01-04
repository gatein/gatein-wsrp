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

package org.gatein.wsrp.admin.ui;

import javax.faces.application.NavigationHandler;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;
import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class RedirectToErrorIfWSRPUnavailablePhaseListener implements PhaseListener
{
   /** Same value as used in faces-config.xml to set the ConsumerRegistry */
   private static final String CONSUMER_REGISTRY = "ConsumerRegistry";
   /** Same value as used in faces-config.xml to set the ProducerConfigurationService */
   private static final String PRODUCER_CONFIGURATION_SERVICE = "ProducerConfigurationService";

   public void afterPhase(PhaseEvent event)
   {
      // nothing to do
   }

   public void beforePhase(PhaseEvent event)
   {
      FacesContext context = event.getFacesContext();

      // if we don't have a ConsumerRegistry or ProducerConfigurationService set in the application scope, then it means
      // that the WSRP is not properly setup and we need to redirect to the error page
      ExternalContext externalContext = context.getExternalContext();
      Map<String, Object> applicationMap = externalContext.getApplicationMap();
      if (!applicationMap.containsKey(CONSUMER_REGISTRY) || !applicationMap.containsKey(PRODUCER_CONFIGURATION_SERVICE))
      {
         NavigationHandler navigationHandler = context.getApplication().getNavigationHandler();
         navigationHandler.handleNavigation(context, null, "error");
      }
   }

   public PhaseId getPhaseId()
   {
      return PhaseId.RESTORE_VIEW;
   }
}
