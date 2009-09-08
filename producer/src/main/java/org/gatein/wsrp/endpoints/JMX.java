/*
 * JBoss, a division of Red Hat
 * Copyright 2009, Red Hat Middleware, LLC, and individual
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

package org.gatein.wsrp.endpoints;

import org.jboss.mx.util.MBeanProxy;
import org.jboss.mx.util.MBeanServerLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class JMX
{
   private static final Logger log = LoggerFactory.getLogger(JMX.class);

   /**
    * Retrieves the MBeanProxy associated with the given class and name from the specified MBeanServer.
    *
    * @param expectedClass the expected class of the MBean's proxy
    * @param name          the MBean's ObjectName
    * @param server        the MBeanServer from which to retrieve the MBeanProxy
    * @return a MBeanProxy for the specified MBean if it exists
    * @throws RuntimeException if the MBean couldn't be retrieved
    */
   public static <T> T getMBeanProxy(Class<T> expectedClass, ObjectName name, MBeanServer server)
   {
      try
      {
         return expectedClass.cast(MBeanProxy.get(expectedClass, name, server));
      }
      catch (Exception e)
      {
         String message = "Couldn't retrieve '" + name.getCanonicalName() + "' MBean with class " + expectedClass.getName();
         log.error(message, e);
         throw new RuntimeException(message, e);
      }
   }

   /**
    * Retrieves the MBeanProxy associated with the given class and name from the JBoss microkernel as returned by
    * <code>MBeanServerLocator.locateJBoss()</code>.
    *
    * @param expectedClass the expected class of the MBean's proxy
    * @param name          a String representation of the MBean's ObjectName
    * @return a MBeanProxy for the specified MBean if it exists
    * @throws IllegalArgumentException if the given name is not a valid ObjectName
    * @throws RuntimeException         if the MBean couldn't be retrieved
    * @see #getMBeanProxy(Class, javax.management.ObjectName, javax.management.MBeanServer)
    * @since 2.4
    */
   public static <T> T getMBeanProxy(Class<T> expectedClass, String name)
   {
      ObjectName objecName = createObjectName(name);
      MBeanServer server = MBeanServerLocator.locateJBoss();
      return getMBeanProxy(expectedClass, objecName, server);
   }

   private static ObjectName createObjectName(String name)
   {
      ObjectName objecName;
      try
      {
         objecName = new ObjectName(name);
      }
      catch (MalformedObjectNameException e)
      {
         throw new IllegalArgumentException("'" + name + "' is not a valid ObjectName");
      }
      return objecName;
   }
}
