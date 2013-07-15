/*
 * JBoss, a division of Red Hat
 * Copyright 2010, Red Hat Middleware, LLC, and individual
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

package org.gatein.wsrp.producer.handlers.processors;

import org.gatein.wsrp.spec.v2.WSRP2ExceptionFactory;
import org.oasis.wsrp.v2.GetMarkup;
import org.oasis.wsrp.v2.GetResource;
import org.oasis.wsrp.v2.HandleEvents;
import org.oasis.wsrp.v2.InvalidHandle;
import org.oasis.wsrp.v2.InvalidRegistration;
import org.oasis.wsrp.v2.MissingParameters;
import org.oasis.wsrp.v2.ModifyRegistrationRequired;
import org.oasis.wsrp.v2.OperationFailed;
import org.oasis.wsrp.v2.OperationNotSupported;
import org.oasis.wsrp.v2.PerformBlockingInteraction;
import org.oasis.wsrp.v2.UnsupportedLocale;
import org.oasis.wsrp.v2.UnsupportedMimeType;
import org.oasis.wsrp.v2.UnsupportedMode;
import org.oasis.wsrp.v2.UnsupportedWindowState;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class ProcessorFactory
{
   public static <Request, Response> RequestProcessor<Request, Response> getProcessorFor(ProducerHelper producer, Request request)
      throws OperationFailed, UnsupportedMode, InvalidHandle, MissingParameters, UnsupportedMimeType,
      UnsupportedWindowState, InvalidRegistration, ModifyRegistrationRequired, UnsupportedLocale, OperationNotSupported
   {
      RequestProcessor processor;
      if (request instanceof GetMarkup)
      {
         processor = new RenderRequestProcessor(producer, (GetMarkup)request);
      }
      else if (request instanceof PerformBlockingInteraction)
      {
         processor = new ActionRequestProcessor(producer, (PerformBlockingInteraction)request);
      }
      else if (request instanceof HandleEvents)
      {
         try
         {
            processor = new EventRequestProcessor(producer, (HandleEvents)request);
         }
         catch (OperationNotSupported operationNotSupported)
         {
            throw WSRP2ExceptionFactory.createWSException(OperationFailed.class,
               "Couldn't initiate EventRequestProcessor", operationNotSupported);
         }
      }
      else if (request instanceof GetResource)
      {
         processor = new ResourceRequestProcessor(producer, (GetResource)request);
      }
      else
      {
         throw new IllegalArgumentException("Unknown request type: " + request.getClass().getSimpleName());
      }

      return (RequestProcessor<Request, Response>)processor;
   }
}
