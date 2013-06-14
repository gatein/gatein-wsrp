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
 * Accessor for consumer-side extensions. Use {@link #addRequestExtension(Class, Object)} to add extensions to
 * the request before it is sent to the producer. Use {@link #getResponseExtensionsFrom(Class)} to retrieve the
 * extensions that the producer might have sent back in its response.
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public interface ConsumerExtensionAccessor
{

   /**
    * Retrieves previously set extensions targeted at the specified WSRP 2 target class so that the consumer can add
    * them to the requests before sending them to the producer. For examples, to retrieve extensions currently targeted
    * to be added on MarkupParams, you would pass MarkupParams.class.
    * <p/>
    * Note that this method is essentially targeted at the internal implementation.
    *
    * @param targetClass the WSRP 2 class for which extensions are supposed to be retrieved, if any
    * @return a List containing the Extensions needed to be added to the target class or an empty List if no such
    *         extension exists.
    */
   List<Extension> getRequestExtensionsFor(Class targetClass);

   /**
    * Adds an extension before it is sent to the producer to elements of the specified target class.
    * <p/>
    * Note that extensions can currently only be set on {@link org.oasis.wsrp.v2.InteractionParams}, {@link
    * org.oasis.wsrp.v2.EventParams}, {@link org.oasis.wsrp.v2.MarkupParams} or {@link
    * org.oasis.wsrp.v2.ResourceParams}
    *
    * @param targetClass the class of elements on which extensions need to be added before being sent to the producer
    * @param extension   the extension to be added in the form of a {@link org.w3c.dom.Element} (<strong>strongly</strong> recommended) or a {@link java.io.Serializable} object
    */
   void addRequestExtension(Class targetClass, Object extension);

   /**
    * Retrieves extensions that were sent by the producer for instances of the specified response class.
    * <p/>
    * Note that currently, GateIn WSRP only processes extensions from {@link org.oasis.wsrp.v2.MarkupResponse},
    * {@link org.oasis.wsrp.v2.BlockingInteractionResponse}, {@link org.oasis.wsrp.v2.HandleEventsResponse} or {@link
    * org.oasis.wsrp.v2.ResourceResponse}. These classes are the ones that contain the specific information pertaining
    * to markup, interaction, resource or event responses.
    *
    * @param responseClass the WSRP 2 response class for which extensions are to be retrieved.
    * @return a List of UnmarshalledExtensions extracted from the specified response class
    */
   List<UnmarshalledExtension> getResponseExtensionsFrom(Class responseClass);

   /**
    * Adds the specified unmarshalled extension to be linked to the specified WSRP response class.
    * <p/>
    * Note that this method is essentially targeted at the internal implementation.
    *
    * @param responseClass the WSRP response class (among those currently supported) to which extracted and unmarshalled extensions need to be added
    * @param extension the extracted and unmarshalled extension to add
    */
   void addResponseExtension(Class responseClass, UnmarshalledExtension extension);

   /**
    * Clears the currently held extensions. This method is called once per request-response cycle by the internal
    * implementation.
    */
   void clear();

}
