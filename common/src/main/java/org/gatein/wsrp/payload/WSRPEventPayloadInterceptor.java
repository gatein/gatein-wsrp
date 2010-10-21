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

package org.gatein.wsrp.payload;

import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.pc.api.PortletInvokerException;
import org.gatein.pc.api.info.EventInfo;
import org.gatein.pc.api.info.EventingInfo;
import org.gatein.pc.api.info.PortletInfo;
import org.gatein.pc.api.invocation.EventInvocation;
import org.gatein.pc.api.invocation.PortletInvocation;
import org.gatein.pc.api.invocation.response.PortletInvocationResponse;
import org.gatein.pc.portlet.PortletInvokerInterceptor;
import org.gatein.pc.portlet.container.ContainerPortletInvoker;
import org.gatein.pc.portlet.container.PortletApplication;
import org.gatein.pc.portlet.container.PortletApplicationContext;
import org.gatein.pc.portlet.container.PortletContainer;
import org.gatein.pc.portlet.impl.info.ContainerTypeInfo;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import java.io.Serializable;
import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class WSRPEventPayloadInterceptor extends PortletInvokerInterceptor
{
   /** . */
   private final static Logger log = LoggerFactory.getLogger(WSRPEventPayloadInterceptor.class);

   public PortletInvocationResponse invoke(PortletInvocation invocation) throws IllegalArgumentException, PortletInvokerException
   {
      if (invocation instanceof EventInvocation)
      {
         EventInvocation eventInvocation = (EventInvocation)invocation;

         Serializable srcPayload = eventInvocation.getPayload();

         Serializable dstPayload = srcPayload;


         if (srcPayload instanceof SerializablePayload)
         {
            PortletContainer container = (PortletContainer)invocation.getAttribute(ContainerPortletInvoker.PORTLET_CONTAINER);
            String containerId = container.getId();
            QName eventName = eventInvocation.getName();

            boolean trace = log.isTraceEnabled();

            // get the event metadata from the portlet metadata
            PortletInfo info = container.getInfo();
            EventingInfo eventingInfo = info.getEventing();
            Map<QName, ? extends EventInfo> consumedEventInfos = eventingInfo.getConsumedEvents();
            EventInfo eventInfo = consumedEventInfos.get(eventName);

            Class dstPayloadClass;
            if (eventInfo != null)
            {
               // get the type of the event
               ContainerTypeInfo typeInfo = (ContainerTypeInfo)eventInfo.getType();

               if (typeInfo != null)
               {
                  // if we managed to get the event type information, try to unmarshall the event from the XML payload
                  dstPayloadClass = typeInfo.getType();
                  if (trace)
                  {
                     log.trace("Obtained for event " + eventName + " its payload class " + dstPayloadClass.getName() + " declared by the portlet meta data "
                        + containerId);
                  }

                  // get the portlet application class loader so we can access the war classes
                  PortletApplication application = container.getPortletApplication();
                  PortletApplicationContext applicationContext = application.getContext();
                  ClassLoader loader = applicationContext.getClassLoader();

                  if (srcPayload instanceof SerializableSimplePayload)
                  {
                     dstPayload = ((SerializableSimplePayload)srcPayload).getPayload();
                  }
                  else
                  {
                     SerializablePayload scp = (SerializablePayload)srcPayload;

                     try
                     {
                        Class<? extends Serializable> clazz = loader.loadClass(dstPayloadClass.getName()).asSubclass(Serializable.class);
                        JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
                        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                        JAXBElement<? extends Serializable> result = unmarshaller.unmarshal(scp.getElement(), clazz);

                        dstPayload = result.getValue();
                     }
                     catch (Exception e)
                     {
                        throw new PortletInvokerException("Couldn't unmarshall event from payload!", e);
                     }
                  }
               }
               else
               {
                  if (trace)
                  {
                     log.trace("No type declared for event " + eventName + " declared by the portlet meta data " + containerId);
                  }
               }
            }

         }

         // Set payload
         eventInvocation.setPayload(dstPayload);

         //
         try
         {
            return super.invoke(invocation);
         }
         finally
         {
            eventInvocation.setPayload(srcPayload);
         }
      }
      else
      {
         return super.invoke(invocation);
      }
   }
}
