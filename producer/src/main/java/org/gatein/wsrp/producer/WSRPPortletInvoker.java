/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2008, Red Hat Middleware, LLC, and individual                    *
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
package org.gatein.wsrp.producer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.gatein.pc.api.NoSuchPortletException;
import org.gatein.pc.api.Portlet;
import org.gatein.pc.api.PortletContext;
import org.gatein.pc.api.PortletInvokerException;
import org.gatein.pc.api.PortletStateType;
import org.gatein.pc.api.StatefulPortletContext;
import org.gatein.pc.api.invocation.PortletInvocation;
import org.gatein.pc.api.invocation.response.PortletInvocationResponse;
import org.gatein.pc.api.invocation.response.UpdateNavigationalStateResponse;
import org.gatein.pc.api.state.DestroyCloneFailure;
import org.gatein.pc.api.state.PropertyChange;
import org.gatein.pc.api.state.PropertyMap;
import org.gatein.pc.portlet.PortletInvokerInterceptor;
import org.gatein.registration.Registration;
import org.gatein.registration.RegistrationLocal;
import org.gatein.registration.RegistrationManager;
import org.gatein.wsrp.WSRPUtils;
import org.gatein.wsrp.producer.handlers.processors.WSRPInstanceContext;
import org.oasis.wsrp.v2.InvalidHandle;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class WSRPPortletInvoker extends PortletInvokerInterceptor
{
   /** Registration Manager */
   private RegistrationManager registrationManager; //todo: make sure it's multi-thread safe
   
   public RegistrationManager getRegistrationManager()
   {
      return registrationManager;
   }

   public void setRegistrationManager(RegistrationManager registrationManager)
   {
      this.registrationManager = registrationManager;
   }
   
   public Portlet getPortlet(PortletContext portletContext) throws IllegalArgumentException, PortletInvokerException
   {
      checkPortletContext(portletContext);
      Portlet portlet = super.getPortlet(portletContext);
      
      if (!portlet.getContext().getId().equals(portletContext.getId()))
      {
         addPortletContext(portlet.getContext());
      }
      
      return portlet;
   }
   
   public Set<Portlet> getPortlets() throws PortletInvokerException
   {
      /**
       * Note: due to the way the ProducerPortletInvoker work, when calling super.getPortlets() it will
       * return the portlets not for the ProducerPortletInvoker from its parent, which should only return
       * non-cloned Portlets.
       */
      
      Registration registration = RegistrationLocal.getRegistration();
      Set<Portlet> portlets = super.getPortlets();
      
      //We first need to let the RegistrationPolicy know that there are potentially new portlets available
      //from the PortletContainer (Note: this only included actual portlets, not clones).
      List<String> portletHandleList = new ArrayList<String>();
      for (Portlet portlet: portlets)
      {
         String portletHandle = WSRPUtils.convertToWSRPPortletContext(portlet.getContext()).getPortletHandle();
         portletHandleList.add(portletHandle);
      }
      this.registrationManager.getPolicy().updatePortletHandles(portletHandleList);
      
      //Now that the RegistrationPolicy knows about the new Portlets, we need to make 
      //sure that we only return portlets that the current registration has access to.
      Set<Portlet> acceptedPortlets = new LinkedHashSet<Portlet>();
      for (Portlet portlet : portlets)
      {
         String portletHandle = WSRPUtils.convertToWSRPPortletContext(portlet.getContext()).getPortletHandle();
         if (this.registrationManager.getPolicy().checkPortletHandle(registration, portletHandle))
         {
            acceptedPortlets.add(portlet);
         }
      }
      
      return acceptedPortlets;
   }
   

   public PortletInvocationResponse invoke(PortletInvocation invocation) throws IllegalArgumentException,
         PortletInvokerException
   {
      checkPortletContext(invocation.getTarget());
      
      PortletInvocationResponse response = super.invoke(invocation);
      
      if (invocation.getInstanceContext() instanceof WSRPInstanceContext)
      {
         WSRPInstanceContext wsrpIC = (WSRPInstanceContext)invocation.getInstanceContext();
         if (wsrpIC.wasModified() && !wsrpIC.getPortletContext().getId().equals(invocation.getTarget().getId()))
         {
            addPortletContext(wsrpIC.getPortletContext());
         }
      }

      return response;
   }
   
   public PortletContext createClone(PortletStateType stateType, PortletContext portletContext)
   throws IllegalArgumentException, PortletInvokerException, UnsupportedOperationException
   {
      checkPortletContext(portletContext);
      
      PortletContext clonedPortletContext = super.createClone(stateType, portletContext);
      
      if (!portletContext.getId().equals(clonedPortletContext.getId()))
         addPortletContext(clonedPortletContext);
      
      return clonedPortletContext;
   }

   public List<DestroyCloneFailure> destroyClones(List<PortletContext> portletContexts)
         throws IllegalArgumentException, PortletInvokerException, UnsupportedOperationException
   {
      //TODO: fix this, we shouldn't remove the pc from the registration policy unless its actually removed from the invoker cleanly
      for (PortletContext portletContext : portletContexts)
      {
         removePortletContext(portletContext);
      }
      
      return super.destroyClones(portletContexts);
   }
   
   public PropertyMap getProperties(PortletContext portletContext) throws IllegalArgumentException,
         PortletInvokerException, UnsupportedOperationException
   {
      checkPortletContext(portletContext);
      return super.getProperties(portletContext);
   }
   
   public PropertyMap getProperties(PortletContext portletContext, Set<String> keys) throws IllegalArgumentException,
         PortletInvokerException, UnsupportedOperationException
   {
      checkPortletContext(portletContext);
      return super.getProperties(portletContext, keys);
   }
   
   public PortletContext setProperties(PortletContext portletContext, PropertyChange[] changes)
         throws IllegalArgumentException, PortletInvokerException, UnsupportedOperationException
   {
      checkPortletContext(portletContext);
      PortletContext updatedPortletContext = super.setProperties(portletContext, changes);
      
      if (!portletContext.getId().equals(updatedPortletContext.getId()))
      {
         addPortletContext(updatedPortletContext);
      }
      
      return updatedPortletContext;
   }
   
   public PortletContext importPortlet(PortletStateType stateType, PortletContext originalPortletContext)
         throws PortletInvokerException, IllegalArgumentException
   {
      //The original portletcontext is the non cloned version and should be one the PC available from the getPortlets operation
      checkPortletContext(originalPortletContext);
      PortletContext newPortletContext = super.importPortlet(stateType, originalPortletContext);
      
      if (!newPortletContext.getId().equals(originalPortletContext.getId()))
         addPortletContext(newPortletContext);
      
      return newPortletContext;
   }
   
   public PortletContext exportPortlet(PortletStateType stateType, PortletContext portletContext)
         throws PortletInvokerException, IllegalArgumentException
   {
      checkPortletContext(portletContext);
      return super.exportPortlet(stateType, portletContext);
   }
   
   protected boolean checkPortletContext(PortletContext portletContext) throws PortletInvokerException
   {
      Registration registration = RegistrationLocal.getRegistration();
      String portletHandle = WSRPUtils.convertToWSRPPortletContext(portletContext).getPortletHandle();
      if (this.getRegistrationManager().getPolicy().checkPortletHandle(registration, portletHandle))
      {
         return true;
      }
      else
      {
         throw new NoSuchPortletException("The PortletContext " + portletContext.getId() + " does not exist or the application is lacking permission to access it.", portletContext.getId());
      }
   }
   
   protected void addPortletContext(PortletContext portletContext)
   {
      Registration registration = RegistrationLocal.getRegistration();
      String portletHandle = WSRPUtils.convertToWSRPPortletContext(portletContext).getPortletHandle();
      this.getRegistrationManager().getPolicy().addPortletHandle(registration, portletHandle);
      this.getRegistrationManager().getPersistenceManager().saveChangesTo(registration);
   }
   
   protected void removePortletContext(PortletContext portletContext)
   {
      Registration registration = RegistrationLocal.getRegistration();
      String portletHandle = WSRPUtils.convertToWSRPPortletContext(portletContext).getPortletHandle();
      this.getRegistrationManager().getPolicy().removePortletHandle(registration, portletHandle);
      this.getRegistrationManager().getPersistenceManager().saveChangesTo(registration);
   }
}

