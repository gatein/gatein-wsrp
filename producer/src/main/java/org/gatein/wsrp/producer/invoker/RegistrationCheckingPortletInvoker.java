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

package org.gatein.wsrp.producer.invoker;

import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.pc.api.NoSuchPortletException;
import org.gatein.pc.api.Portlet;
import org.gatein.pc.api.PortletContext;
import org.gatein.pc.api.PortletInvokerException;
import org.gatein.pc.api.PortletStateType;
import org.gatein.pc.api.PortletStatus;
import org.gatein.pc.api.invocation.PortletInvocation;
import org.gatein.pc.api.invocation.response.PortletInvocationResponse;
import org.gatein.pc.api.spi.InstanceContext;
import org.gatein.pc.api.state.DestroyCloneFailure;
import org.gatein.pc.api.state.PropertyChange;
import org.gatein.pc.api.state.PropertyMap;
import org.gatein.pc.portlet.PortletInvokerInterceptor;
import org.gatein.registration.Registration;
import org.gatein.registration.RegistrationDestructionListener;
import org.gatein.registration.RegistrationException;
import org.gatein.registration.RegistrationLocal;
import org.gatein.registration.RegistrationManager;
import org.gatein.registration.RegistrationPolicy;
import org.gatein.registration.spi.RegistrationSPI;
import org.gatein.wsrp.producer.handlers.processors.WSRPInstanceContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class RegistrationCheckingPortletInvoker extends PortletInvokerInterceptor implements RegistrationDestructionListener
{
   /** Registration Manager */
   private RegistrationManager registrationManager;
   private static final Logger log = LoggerFactory.getLogger(RegistrationCheckingPortletInvoker.class);

   public void setRegistrationManager(RegistrationManager registrationManager)
   {
      this.registrationManager = registrationManager;
      registrationManager.addRegistrationDestructionListener(this);
   }

   private RegistrationPolicy getPolicy()
   {
      return registrationManager.getPolicy();
   }

   public Portlet getPortlet(PortletContext portletContext) throws IllegalArgumentException, PortletInvokerException
   {
      Registration registration = RegistrationLocal.getRegistration();
      if (registration != null)
      {
         if (registration.knows(portletContext) || PortletStatus.OFFERED == super.getStatus(portletContext))
         {
            return super.getPortlet(portletContext);
         }
         else
         {
            String id = portletContext.getId();
            throw new NoSuchPortletException("Registration '" + registration.getRegistrationHandle()
               + "' does not know the '"
               + id + "' portlet", id);
         }
      }
      else
      {
         return super.getPortlet(portletContext);
      }
   }

   public Set<Portlet> getPortlets() throws PortletInvokerException
   {
      Set<Portlet> portlets = new HashSet<Portlet>(super.getPortlets());
      Registration registration = RegistrationLocal.getRegistration();

      if (registration != null)
      {
         Set<PortletContext> contexts = registration.getKnownPortletContexts();
         for (PortletContext context : contexts)
         {
            try
            {
               portlets.add(super.getPortlet(context));
            }
            catch (NoSuchPortletException e)
            {
               final RegistrationSPI registrationSPI = getRegistrationAsSPI();
               try
               {
                  registrationSPI.removePortletContext(context);
                  log.debug("Removed '" + context + "' from Registration '" + registration.getRegistrationHandle() + "' because it cannot be resolved anymore.");
               }
               catch (RegistrationException e1)
               {
                  throw new PortletInvokerException(e1);
               }
            }
         }
      }

      return portlets;
   }

   private RegistrationSPI getRegistrationAsSPI() throws PortletInvokerException
   {
      Registration registration = RegistrationLocal.getRegistration();

      if (registration == null)
      {
         return null;
      }

      if (registration instanceof RegistrationSPI)
      {
         return (RegistrationSPI)registration;
      }
      else
      {
         throw new PortletInvokerException("Cannot deal with non-RegistrationSPI Registrations.");
      }
   }


   public PortletInvocationResponse invoke(PortletInvocation invocation) throws IllegalArgumentException, PortletInvokerException
   {
      PortletContext portletContext = invocation.getTarget();

      RegistrationSPI registration = getRegistrationAsSPI();

      if (registration != null)
      {
         checkOperationIsAllowed(portletContext, registration, "invoke");

         PortletInvocationResponse response = super.invoke(invocation);

         InstanceContext instanceContext = invocation.getInstanceContext();
         if (instanceContext instanceof WSRPInstanceContext)
         {
            WSRPInstanceContext wsrpIC = (WSRPInstanceContext)instanceContext;
            PortletContext responseContext = wsrpIC.getPortletContext();
            if (wsrpIC.wasModified() && !responseContext.getId().equals(portletContext.getId()))
            {
               try
               {
                  registration.addPortletContext(responseContext);
               }
               catch (RegistrationException e)
               {
                  throw new PortletInvokerException("Couldn't add portlet context '" + responseContext + "' to registration '" + registration.getRegistrationHandle() + "'", e);
               }
            }
         }

         return response;
      }
      else
      {
         return super.invoke(invocation);
      }

   }

   private void checkOperationIsAllowed(PortletContext portletContext, Registration registration, String operation) throws NoSuchPortletException
   {
      if (!getPolicy().allowAccessTo(portletContext, registration, operation))
      {
         String id = portletContext.getId();
         throw new NoSuchPortletException("The PortletContext '" + id
            + "' does not exist or the application is lacking permission to access it for operation '"
            + operation + "'", id);
      }
   }

   public PortletContext createClone(PortletStateType stateType, PortletContext portletContext)
      throws IllegalArgumentException, PortletInvokerException, UnsupportedOperationException
   {
      RegistrationSPI registration = getRegistrationAsSPI();

      if (registration != null)
      {
         checkOperationIsAllowed(portletContext, registration, "createClone");

         PortletContext clonedPortletContext = super.createClone(stateType, portletContext);
         try
         {
            registration.addPortletContext(clonedPortletContext);
         }
         catch (RegistrationException e)
         {
            throw new PortletInvokerException("Couldn't add portlet context '" + clonedPortletContext + "' to registration '" + registration.getRegistrationHandle() + "'", e);
         }

         return clonedPortletContext;
      }
      else
      {
         return super.createClone(stateType, portletContext);
      }
   }

   public List<DestroyCloneFailure> destroyClones(List<PortletContext> portletContexts)
      throws IllegalArgumentException, PortletInvokerException, UnsupportedOperationException
   {
      RegistrationSPI registration = getRegistrationAsSPI();

      if (registration != null)
      {
         for (PortletContext portletContext : portletContexts)
         {
            checkOperationIsAllowed(portletContext, registration, "destroyClones");
         }
      }

      List<DestroyCloneFailure> cloneFailures = super.destroyClones(portletContexts);
      boolean noFailures = cloneFailures.isEmpty();

      if (registration != null)
      {
         for (PortletContext portletContext : portletContexts)
         {
            // only remove the portlet context if there are no failures or it's not part of the failed clones
            if (noFailures || !cloneFailures.contains(new DestroyCloneFailure(portletContext.getId())))
            {
               try
               {
                  registration.removePortletContext(portletContext);
               }
               catch (RegistrationException e)
               {
                  throw new PortletInvokerException("Couldn't remove portlet context '" + portletContext + "' to registration '" + registration.getRegistrationHandle() + "'", e);
               }
            }
         }
      }

      return cloneFailures;
   }

   public PropertyMap getProperties(PortletContext portletContext) throws IllegalArgumentException,
      PortletInvokerException, UnsupportedOperationException
   {
      checkOperationIsAllowed(portletContext, RegistrationLocal.getRegistration(), "getProperties");
      return super.getProperties(portletContext);
   }

   public PropertyMap getProperties(PortletContext portletContext, Set<String> keys) throws IllegalArgumentException,
      PortletInvokerException, UnsupportedOperationException
   {
      checkOperationIsAllowed(portletContext, RegistrationLocal.getRegistration(), "getProperties");
      return super.getProperties(portletContext, keys);
   }

   public PortletContext setProperties(PortletContext portletContext, PropertyChange[] changes)
      throws IllegalArgumentException, PortletInvokerException, UnsupportedOperationException
   {
      RegistrationSPI registration = getRegistrationAsSPI();

      if (registration != null)
      {
         checkOperationIsAllowed(portletContext, registration, "setProperties");
         PortletContext updatedPortletContext = super.setProperties(portletContext, changes);

         if (!portletContext.getId().equals(updatedPortletContext.getId()))
         {
            try
            {
               registration.addPortletContext(updatedPortletContext);
            }
            catch (RegistrationException e)
            {
               throw new PortletInvokerException("Couldn't add portlet context '" + updatedPortletContext + "' to registration '" + registration.getRegistrationHandle() + "'", e);
            }
         }

         return updatedPortletContext;
      }
      else
      {
         return super.setProperties(portletContext, changes);
      }
   }

   public PortletContext importPortlet(PortletStateType stateType, PortletContext originalPortletContext)
      throws PortletInvokerException, IllegalArgumentException
   {
      // The original portletcontext is the non cloned version and should be one the PC available from the getPortlets operation
      RegistrationSPI registration = getRegistrationAsSPI();

      if (registration != null)
      {
         checkOperationIsAllowed(originalPortletContext, registration, "importPortlet");

         PortletContext newPortletContext = super.importPortlet(stateType, originalPortletContext);

         if (!newPortletContext.getId().equals(originalPortletContext.getId()))
         {
            try
            {
               registration.addPortletContext(newPortletContext);
            }
            catch (RegistrationException e)
            {
               throw new PortletInvokerException("Couldn't add portlet context '" + newPortletContext + "' to registration '" + registration.getRegistrationHandle() + "'", e);
            }
         }

         return newPortletContext;
      }
      else
      {
         return super.importPortlet(stateType, originalPortletContext);
      }
   }

   public PortletContext exportPortlet(PortletStateType stateType, PortletContext portletContext)
      throws PortletInvokerException, IllegalArgumentException
   {
      checkOperationIsAllowed(portletContext, RegistrationLocal.getRegistration(), "exportPortlet");
      return super.exportPortlet(stateType, portletContext);
   }

   /**
    * Destroy the clones scoped by this Registration.
    *
    * @param registration the Registration about to be destroyed
    * @return
    */
   public Vote destructionScheduledFor(Registration registration)
   {
      if (registration != null)
      {
         List<PortletContext> portletContexts = new ArrayList<PortletContext>(registration.getKnownPortletContexts());
         List<DestroyCloneFailure> failures = Collections.emptyList();
         try
         {
            failures = super.destroyClones(portletContexts);
         }
         catch (Exception e)
         {
            if (log.isDebugEnabled())
            {
               log.debug("Couldn't destroy clones", e);
            }
            return Vote.negativeVote("Couldn't destroy clones: " + failures);
         }
      }

      return RegistrationDestructionListener.SUCCESS;
   }
}
