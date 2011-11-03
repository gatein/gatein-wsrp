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

package org.gatein.wsrp.consumer.registry.hibernate;

import org.gatein.wsrp.consumer.ConsumerException;
import org.gatein.wsrp.consumer.ProducerInfo;
import org.gatein.wsrp.consumer.registry.AbstractConsumerRegistry;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import javax.naming.InitialContext;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class HibernateConsumerRegistry extends AbstractConsumerRegistry
{
   private SessionFactory sessionFactory;
   private String sessionFactoryJNDIName;

   public void save(ProducerInfo info, String messageOnError)
   {
      try
      {
         Session session = sessionFactory.getCurrentSession();
         session.persist(info);
      }
      catch (HibernateException e)
      {
         throw new ConsumerException(messageOnError, e);
      }
   }

   public void delete(ProducerInfo info)
   {
      Session session = sessionFactory.getCurrentSession();

      session.delete(info);
   }

   public String getSessionFactoryJNDIName()
   {
      return sessionFactoryJNDIName;
   }

   public void setSessionFactoryJNDIName(String sessionFactoryJNDIName)
   {
      this.sessionFactoryJNDIName = sessionFactoryJNDIName;
   }

   /**
    * Updates the given ProducerInfo
    *
    * @param producerInfo the ProducerInfo to update
    * @return the id that was previously assigned to the specified ProducerInfo or <code>null</code> if the id hasn't
    *         been modified
    */
   public String update(ProducerInfo producerInfo)
   {
      String oldId;
      Session session = sessionFactory.getCurrentSession();
      try
      {

         // Retrieve the previous id of the given ProducerInfo to update local consumers map if needed
         oldId = (String)session.createQuery("select pi.persistentId from ProducerInfo pi where pi.id = :key")
            .setParameter("key", producerInfo.getKey()).uniqueResult();
         if (producerInfo.getId().equals(oldId))
         {
            oldId = null; // reset oldId as the ProducerInfo's id hasn't been modified
         }

         // merge old producer info with new data
         session.update(producerInfo);

      }
      catch (HibernateException e)
      {
         throw new ConsumerException("Couldn't update ProducerInfo for Consumer '" + producerInfo.getId() + "'", e);
      }
      return oldId;
   }

   public Iterator<ProducerInfo> getProducerInfosFromStorage()
   {
      Session session = sessionFactory.getCurrentSession();

      return session.createQuery("from ProducerInfo pi order by pi.persistentId").iterate();
   }

   public ProducerInfo loadProducerInfo(String id)
   {
      Session session = sessionFactory.getCurrentSession();
      return (ProducerInfo)session.createQuery("from ProducerInfo pi where pi.persistentId = :id")
         .setParameter("id", id).uniqueResult();
   }

   @Override
   public void start() throws Exception
   {
      InitialContext initialContext = new InitialContext();
      sessionFactory = (SessionFactory)initialContext.lookup(sessionFactoryJNDIName);
      super.start();
   }

   @Override
   public void stop() throws Exception
   {
      sessionFactory = null;
      super.stop();
   }

   public Collection<String> getConfiguredConsumersIds()
   {
      throw new UnsupportedOperationException();
   }

   @Override
   protected void initConsumerCache()
   {
      setConsumerCache(new InMemoryConsumerCache(this));
   }
}
