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

package org.gatein.wsrp.jcr;

import org.chromattic.api.Chromattic;
import org.chromattic.api.ChromatticBuilder;
import org.chromattic.api.ChromatticSession;
import org.gatein.common.util.ParameterValidation;
import org.gatein.wsrp.jcr.mapping.BaseMapping;
import org.gatein.wsrp.jcr.mapping.mixins.LastModified;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public abstract class BaseChromatticPersister implements ChromatticPersister
{
   private Chromattic chrome;
   public static final String WSRP_WORKSPACE_NAME = "wsrp-system";
   public static final String PORTLET_STATES_WORKSPACE_NAME = "pc-system";
   protected static final String REPOSITORY_NAME = "repository";
   protected String workspaceName;
   private Map<Class, Class<? extends BaseMapping>> modelToMapping;

   private ThreadLocal<ChromatticSession> sessionHolder = new ThreadLocal<ChromatticSession>();

   public BaseChromatticPersister(String workspaceName)
   {
      this.workspaceName = workspaceName;
   }

   public void initializeBuilderFor(List<Class> mappingClasses) throws Exception
   {
      ChromatticBuilder builder = ChromatticBuilder.create();
      builder.setOptionValue(ChromatticBuilder.INSTRUMENTOR_CLASSNAME, "org.chromattic.apt.InstrumentorImpl");

      setBuilderOptions(builder);

      modelToMapping = new HashMap<Class, Class<? extends BaseMapping>>(mappingClasses.size());
      for (Class mappingClass : mappingClasses)
      {
         if (BaseMapping.class.isAssignableFrom(mappingClass))
         {
            Type[] interfaces = mappingClass.getGenericInterfaces();
            if (ParameterValidation.existsAndIsNotEmpty(interfaces))
            {
               Class type = (Class)((ParameterizedType)interfaces[0]).getActualTypeArguments()[0];
               modelToMapping.put(type, mappingClass);
            }
         }
         builder.add(mappingClass);
      }

      chrome = builder.build();
   }

   protected abstract void setBuilderOptions(ChromatticBuilder builder);

   public ChromatticSession getSession()
   {
      ChromatticSession chromatticSession = sessionHolder.get();
      if (chromatticSession == null)
      {
         ChromatticSession session = chrome.openSession();
         sessionHolder.set(session);
         return session;
      }
      else
      {
         return chromatticSession;
      }
   }

   public void closeSession(boolean save)
   {
      ChromatticSession session = getOpenedSessionOrFail();
      if (save)
      {
         synchronized (this)
         {
            session.save();
         }
      }
      session.close();
      sessionHolder.set(null);
   }

   private ChromatticSession getOpenedSessionOrFail()
   {
      ChromatticSession session = sessionHolder.get();
      if (session == null)
      {
         throw new IllegalStateException("Cannot close the session as it hasn't been opened first!");
      }
      return session;
   }

   public synchronized void save()
   {
      getOpenedSessionOrFail().save();
   }

   public <T> boolean delete(T toDelete, StoresByPathManager<T> manager)
   {
      Class<? extends Object> modelClass = toDelete.getClass();
      Class<? extends BaseMapping> baseMappingClass = modelToMapping.get(modelClass);
      if (baseMappingClass == null)
      {
         throw new IllegalArgumentException("Cannot find a mapping class for " + modelClass.getName());
      }

      ChromatticSession session = getSession();

      Object old = session.findByPath(baseMappingClass, manager.getChildPath(toDelete));

      if (old != null)
      {
         session.remove(old);

         // update last modified of element linked to toDelete if needed
         final LastModified lastModified = manager.lastModifiedToUpdateOnDelete(session);
         if (lastModified != null)
         {
            lastModified.setLastModified(System.currentTimeMillis());
         }
         closeSession(true);
         return true;
      }
      else
      {
         closeSession(false);
         return false;
      }
   }
}
