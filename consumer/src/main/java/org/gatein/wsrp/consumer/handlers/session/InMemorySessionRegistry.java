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

package org.gatein.wsrp.consumer.handlers.session;

import org.gatein.wsrp.consumer.handlers.ProducerSessionInformation;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/** @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a> */
public class InMemorySessionRegistry implements SessionRegistry
{
   private Map<String, ProducerSessionInformation> sessionInfos = new ConcurrentHashMap<String, ProducerSessionInformation>(); // todo: thread-safe?

   public Set<ProducerSessionInformation> getAll()
   {
      return new HashSet<ProducerSessionInformation>(sessionInfos.values());
   }

   public ProducerSessionInformation get(String sessionId)
   {
      return sessionInfos.get(sessionId);
   }

   public ProducerSessionInformation remove(String sessionId)
   {
      return sessionInfos.remove(sessionId);
   }

   public void put(String sessionId, ProducerSessionInformation sessionInformation)
   {
      sessionInfos.put(sessionId, sessionInformation);
   }
}
