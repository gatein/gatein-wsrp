/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2012, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/
package org.wsrp.wss.jboss5.handlers.consumer;

import org.gatein.wsrp.services.PortCustomizer;
import org.jboss.ws.core.StubExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class JBWSPortCustomizer implements PortCustomizer
{

   private static Logger log = LoggerFactory.getLogger(JBWSPortCustomizer.class);

   @Override
   public void customizePort(Object service)
   {
      if (service instanceof StubExt)
      {
         StubExt stub = (StubExt)service;
         stub.setConfigName("GateIn Consumer WSSecurity", "META-INF/gatein-consumer-config.xml");
      }
      else
      {
         log.warn("Service not an instance of StubExt, cannot customize the port for WS-Security.");
      }
   }

   @Override
   public boolean isWSSFocused()
   {
      return true;
   }

}

