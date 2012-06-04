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

import org.oasis.wsrp.v2.Extension;

import java.util.List;

/**
 * Manages access to extensions on the producer-side so that API clients can set and retrieve extensions before
 * requests are processed or responses sent back to the consumer.
 * <p/>
 * This API is meant to be called from the producer-side {@link InvocationHandlerDelegate}, which methods are called by
 * the implementation before the producer calls the portlet targeted by the WSRP request and after the invocation on
 * the portlet is made but before the WSRP response is sent to the consumer.
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @see InvocationHandlerDelegate
 */
public interface ProducerExtensionAccessor
{
   /**
    * Adds the specified unmarshalled extension to the list of extensions associated with instances the specified WSRP
    * request class before the request is processed by the producer's portlet container.
    * <p/>
    * Note that this method is mostly targeted at the internal implementation.
    *
    * @param fromClass the class to which extensions should be associated
    * @param extension the unmarshalled extension to add to the specified request parameter
    */
   void addRequestExtension(Class fromClass, UnmarshalledExtension extension);

   /**
    * Retrieves the list of unmarshalled extensions currently associated with instances of the specified target
    * consumer
    * request class.
    * <p/>
    * Note that extensions can currently only be retrieved on {@link org.oasis.wsrp.v2.InteractionParams}, {@link
    * org.oasis.wsrp.v2.EventParams}, {@link org.oasis.wsrp.v2.MarkupParams} or {@link
    * org.oasis.wsrp.v2.ResourceParams}
    *
    * @param targetClass the class of request parameters for which extensions are to be retrieved
    * @return the list of unmarshalled extensions currently associated with instances of the specified target consumer
    *         request class
    */
   List<UnmarshalledExtension> getRequestExtensionsFor(Class targetClass);

   /**
    * Retrieves the extensions associated with the specified WSRP response class so that they can be set appropriately
    * on the response sent to the consumer.
    * <p/>
    * Note that this method is mostly targeted at the internal implementation.
    *
    * @param wsrpResponseClass the class on which extensions are to be set
    * @return the list of extensions associated with the specified WSRP response class so that they can be set
    *         appropriately on the response sent to the consumer.
    */
   List<Extension> getResponseExtensionsFor(Class wsrpResponseClass);

   /**
    * Add the specified extension (in the form a name / value pair) to be set to the targeted WSRP response class
    * before it is sent to the consumer.
    * <p/>
    * Note that currently, GateIn WSRP only processes extensions from {@link org.oasis.wsrp.v2.MarkupResponse},
    * {@link org.oasis.wsrp.v2.BlockingInteractionResponse}, {@link org.oasis.wsrp.v2.HandleEventsResponse} or {@link
    * org.oasis.wsrp.v2.ResourceResponse}. These classes are the ones that contain the specific information pertaining
    * to markup, interaction, resource or event requests.
    *
    * @param wsrpResponseClass
    * @param extension
    */
   void addResponseExtension(Class wsrpResponseClass, Object extension);
}
