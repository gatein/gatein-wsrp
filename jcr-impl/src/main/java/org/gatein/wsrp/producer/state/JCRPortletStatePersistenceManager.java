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

package org.gatein.wsrp.producer.state;

import org.chromattic.api.ChromatticSession;
import org.gatein.common.util.ParameterValidation;
import org.gatein.pc.api.state.PropertyMap;
import org.gatein.pc.portlet.state.InvalidStateIdException;
import org.gatein.pc.portlet.state.NoSuchStateException;
import org.gatein.pc.portlet.state.producer.AbstractPortletStatePersistenceManager;
import org.gatein.pc.portlet.state.producer.PortletStateContext;
import org.gatein.wsrp.jcr.ChromatticPersister;
import org.gatein.wsrp.producer.state.mapping.PortletStateContextMapping;
import org.gatein.wsrp.producer.state.mapping.PortletStateContextsMapping;
import org.gatein.wsrp.producer.state.mapping.PortletStateMapping;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class JCRPortletStatePersistenceManager extends AbstractPortletStatePersistenceManager
{
   private ChromatticPersister persister;
   private static final String PATH = PortletStateContextsMapping.NODE_NAME + "/";

   public static final List<Class> mappingClasses = new ArrayList<Class>(3);

   static
   {
      Collections.addAll(mappingClasses, PortletStateContextsMapping.class, PortletStateContextMapping.class, PortletStateMapping.class);
   }

   public JCRPortletStatePersistenceManager(ChromatticPersister persister) throws Exception
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(persister, "ChromatticPersister");
      this.persister = persister;
   }

   private PortletStateContextsMapping getContexts(ChromatticSession session)
   {
      PortletStateContextsMapping portletStateContexts = session.findByPath(PortletStateContextsMapping.class, PortletStateContextsMapping.NODE_NAME);
      if (portletStateContexts == null)
      {
         portletStateContexts = session.insert(PortletStateContextsMapping.class, PortletStateContextsMapping.NODE_NAME);
      }
      return portletStateContexts;
   }

   @Override
   public void updateState(String stateId, PropertyMap propertyMap) throws NoSuchStateException, InvalidStateIdException
   {
      // more optimized version of updateState
      ParameterValidation.throwIllegalArgExceptionIfNull(propertyMap, "property map");

      try
      {
         ChromatticSession session = persister.getSession();

         PortletStateContextMapping pscm = getPortletStateContextMapping(session, stateId);
         PortletStateMapping psm = pscm.getState();
         psm.setProperties(propertyMap);

         persister.save();
      }
      finally
      {
         persister.closeSession(false);
      }
   }


   @Override
   protected PortletStateContext getStateContext(String stateId)
   {
      try
      {
         ChromatticSession session = persister.getSession();

         PortletStateContextMapping pscm = getPortletStateContextMapping(session, stateId);
         PortletStateContext context;
         if (pscm == null)
         {
            context = null;
         }
         else
         {
            context = pscm.toPortletStateContext();
         }

         return context;
      }
      finally
      {
         persister.closeSession(false);
      }
   }

   @Override
   protected String createStateContext(String portletId, PropertyMap propertyMap)
   {
      try
      {
         ChromatticSession session = persister.getSession();

         PortletStateContextsMapping portletStateContexts = getContexts(session);
         PortletStateContextMapping pscm = portletStateContexts.createPortletStateContext(UUID.randomUUID().toString());
         portletStateContexts.getPortletStateContexts().add(pscm);

         PortletStateMapping psm = pscm.getState();
         psm.setPortletID(portletId);
         psm.setProperties(propertyMap);

         // get the key
         final String key = pscm.getPersistentKey();

         // then save
         persister.save();

         return key;
      }
      finally
      {
         persister.closeSession(false);
      }
   }

   @Override
   protected PortletStateContext destroyStateContext(String stateId)
   {
      try
      {
         ChromatticSession session = persister.getSession();

         PortletStateContextMapping pscm = getPortletStateContextMapping(session, stateId);
         PortletStateContext result;
         if (pscm == null)
         {
            result = null;
         }
         else
         {
            getContexts(session).getPortletStateContexts().remove(pscm);
            result = pscm.toPortletStateContext();
         }

         persister.save();
         return result;
      }
      finally
      {
         persister.closeSession(false);
      }
   }

   @Override
   protected void updateStateContext(PortletStateContext stateContext)
   {
      throw new UnsupportedOperationException("Shouldn't be called as updateState method is overriden!");
   }

   private PortletStateContextMapping getPortletStateContextMapping(ChromatticSession session, String stateId)
   {
      return getContexts(session).findPortletStateContextById(stateId);
   }
}
