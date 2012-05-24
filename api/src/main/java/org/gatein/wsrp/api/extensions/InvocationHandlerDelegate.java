/*
* JBoss, a division of Red Hat
* Copyright 2008, Red Hat Middleware, LLC, and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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

package org.gatein.wsrp.api.extensions;

import org.gatein.pc.api.invocation.PortletInvocation;
import org.gatein.pc.api.invocation.response.PortletInvocationResponse;

/** @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a> */
public abstract class InvocationHandlerDelegate
{
   private static final InvocationHandlerDelegate CONSUMER_DELEGATE;
   private static final InvocationHandlerDelegate PRODUCER_DELEGATE;
   public static final String CONSUMER_DELEGATE_CLASSNAME = "org.gatein.wsrp.consumer.handlers.delegate";
   public static final String PRODUCER_DELEGATE_CLASSNAME = "org.gatein.wsrp.producer.handlers.delegate";

   static
   {
      CONSUMER_DELEGATE = createDelegate(System.getProperty(CONSUMER_DELEGATE_CLASSNAME));
      PRODUCER_DELEGATE = createDelegate(System.getProperty(PRODUCER_DELEGATE_CLASSNAME));
   }

   private static InvocationHandlerDelegate createDelegate(String delegateClassName)
   {
      if (delegateClassName != null && !delegateClassName.isEmpty())
      {
         ClassLoader loader = Thread.currentThread().getContextClassLoader();
         try
         {
            Class delegateClass = loader.loadClass(delegateClassName);
            if (!InvocationHandlerDelegate.class.isAssignableFrom(delegateClass))
            {
               throw new IllegalArgumentException("Invocation handler delegate class " + delegateClassName + "does not extends " + InvocationHandlerDelegate.class.getName());
            }
            return (InvocationHandlerDelegate)delegateClass.newInstance();
         }
         catch (Exception e)
         {
            throw new RuntimeException(e);
         }
      }
      return null;
   }


   public static InvocationHandlerDelegate consumerDelegate()
   {
      return CONSUMER_DELEGATE;
   }

   public static InvocationHandlerDelegate producerDelegate()
   {
      return PRODUCER_DELEGATE;
   }

   public abstract void processInvocation(PortletInvocation invocation);

   public abstract void processInvocationResponse(PortletInvocationResponse response, PortletInvocation invocation);
}
