/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2011, Red Hat Middleware, LLC, and individual                    *
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
package org.wsrp.wss.jboss5.handlers.producer;

import org.jboss.ws.extensions.security.jaxws.WSSecurityHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.ws.handler.MessageContext;
import java.io.File;
import java.net.MalformedURLException;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class JBWSSecurityHandlerWrapper extends WSSecurityHandler
{
   private static Logger log = LoggerFactory.getLogger(JBWSSecurityHandlerWrapper.class);

   protected boolean handleInbound(MessageContext msgContext)
   {
      return handleInboundSecurity(msgContext);
   }

   protected boolean handleOutbound(MessageContext msgContext)
   {
      return handleOutboundSecurity(msgContext);
   }

   @Override
   protected String getConfigResourceName()
   {
      String configFile = System.getProperty("gatein.wsrp.producer.wss.config");
      if (configFile == null)
      {
         String gateInConfDirectory = System.getProperty("gatein.conf.dir");
         configFile = gateInConfDirectory + File.separator + "gatein-wsse-producer.xml";
      }

      if (configFile != null)
      {
         File file = new File(configFile);
         if (file.exists())
         {
            try
            {
               return file.toURI().toURL().toString();
            }
            catch (MalformedURLException e)
            {
               log.warn("Exception when trying to get gatein wsse producer configuration file : " + configFile, e);
            }
         }
         else
         {
            log.debug("No gatein-wsse-producer.xml file found in the gatein.conf.dir. Using default empty wss configuration file.");
         }
      }
      // if the file does not exist or if an exception occurs, return the default, empty internal configuration file.
      return "conf/gatein-wsse-producer.xml";
   }

}

