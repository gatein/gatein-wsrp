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
import org.gatein.wsrp.api.plugins.Plugins;
import org.gatein.wsrp.api.plugins.PluginsAccess;

/**
 * A delegate that can be used to intercept PortletInvocations and PortletInvocationReponses before they are processed
 * either by the Consumer or Producer. While this is particularly useful to be able to add and process WSRP Extensions
 * that are outside of the WSRP specification, delegates could certainly be used to do more. However, since they have
 * access to raw data from the portal's internal, one must be very careful about what's being done to the invocations
 * and responses as a wrong operation could possibly have disastrous results on the portal's behavior. It is therefore
 * <strong>extremely recommended</strong> (though not currently enforced) to treat the access to PortletInvocation and
 * PortletInvocationResponse objects as <strong>read-only</strong>.
 * <p/>
 * On the Consumer side, the consumer InvocationHandlerDelegate can intercept the incoming PortletInvocation from the
 * consumer portal before it is processed by the WSRP stack, i.e. before a WSRP request is sent to the remote producer.
 * On the flip side, the response from the producer can be processed right after the WSRP stack is done with it but
 * before it is processed by the consumer portal.
 * <p/>
 * On the Producer side, the producer InvocationHandlerDelegate can intercept the PortletInvocation that has been
 * created as a translation of the incoming WSRP request before it is sent to the producer portal's portlet container.
 * The response from the portlet container can then be processed before it is handled by the WSRP stack to be sent back
 * to the Consumer.
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 */
public abstract class InvocationHandlerDelegate
{
   private static InvocationHandlerDelegate consumerDelegate;
   private static InvocationHandlerDelegate producerDelegate;
   public static final String CONSUMER_DELEGATE_CLASSNAME = "org.gatein.wsrp.consumer.handlers.delegate";
   public static final String PRODUCER_DELEGATE_CLASSNAME = "org.gatein.wsrp.producer.handlers.delegate";

   static
   {
      final Plugins plugins = PluginsAccess.getPlugins();
      consumerDelegate = createInstance(CONSUMER_DELEGATE_CLASSNAME, plugins);
      producerDelegate = createInstance(PRODUCER_DELEGATE_CLASSNAME, plugins);
   }

   private static InvocationHandlerDelegate createInstance(String propertyName, Plugins plugins)
   {
      String delegateClassName = System.getProperty(propertyName);
      if (delegateClassName != null && !delegateClassName.isEmpty())
      {
         return plugins.createPluginInstance(delegateClassName, InvocationHandlerDelegate.class);
      }
      else
      {
         return null;
      }
   }

   /**
    * Only public for testing purposes
    *
    * @param delegate
    */
   public synchronized static void registerConsumerDelegate(InvocationHandlerDelegate delegate)
   {
      consumerDelegate = delegate;
   }

   /**
    * Only public for testing purposes
    *
    * @param delegate
    */
   public synchronized static void registerProducerDelegate(InvocationHandlerDelegate delegate)
   {
      producerDelegate = delegate;
   }

   /**
    * Retrieves the delegate on the Consumer side.
    *
    * @return
    */
   public static InvocationHandlerDelegate consumerDelegate()
   {
      return consumerDelegate;
   }

   /**
    * Retrieves the delegate on the Producer side.
    *
    * @return
    */
   public static InvocationHandlerDelegate producerDelegate()
   {
      return producerDelegate;
   }

   /**
    * Method to process the specified PortletInvocation before it is handled by the rest of the WSRP invocation chain.
    * See the class documentation for more details.
    *
    * @param invocation the PortletInvocation to process (recommended to consider as read-only, used to extract
    *                   information from, not modify)
    */
   public abstract void processInvocation(PortletInvocation invocation);

   /**
    * Method to process the specified PortletInvocationResponse before it is handled by the rest of the WSRP invocation
    * chain. See the class documentation for more details.
    *
    * @param response   the PortletInvocationResponse to process (recommended to consider as read-only, used to extract
    *                   information from, not modify)
    * @param invocation the PortletInvocation that caused the specified PortletInvocationResponse (recommended to
    *                   consider as read-only, used to extract
    *                   information from, not modify)
    */
   public abstract void processInvocationResponse(PortletInvocationResponse response, PortletInvocation invocation);
}
