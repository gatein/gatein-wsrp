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

package org.wsrp.wss.jboss5.handlers;

import org.jboss.ws.extensions.security.jaxws.WSSecurityHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.ws.handler.MessageContext;
import java.io.File;
import java.net.MalformedURLException;

/** @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a> */
public abstract class AbstractJBWSSecurityHandlerWrapper extends WSSecurityHandler
{
   protected static final Logger log = LoggerFactory.getLogger(AbstractWSSecurityCredentialHandler.class);
   public static final String GATEIN_CONF_DIR = "gatein.conf.dir";

   @Override
   protected boolean handleInbound(MessageContext msgContext)
   {
      return handleInboundSecurity(msgContext);
   }

   @Override
   protected boolean handleOutbound(MessageContext msgContext)
   {
      return handleOutboundSecurity(msgContext);
   }

   @Override
   protected String getConfigResourceName()
   {
      File configFile;
      String configFileName = System.getProperty(getConfigPropertyName());
      if (configFileName == null)
      {
         String gateInConfDirectory = System.getProperty(GATEIN_CONF_DIR);
         configFile = new File(gateInConfDirectory, getDefaultConfigFileName());
      }
      else
      {
         configFile = new File(configFileName);
      }

      if (configFile.exists())
      {
         try
         {
            return configFile.toURI().toURL().toString();
         }
         catch (MalformedURLException e)
         {
            log.warn("Couldn't retrieve GateIn's WS Security " + getHandlerType() + " configuration file : " + configFileName, e);
         }
      }
      else
      {
         log.debug("No " + getDefaultConfigFileName() + " file found in the " + GATEIN_CONF_DIR +
            " directory. Using default empty WS Security configuration file instead.");
      }

      // if the file does not exist or if an exception occurs, return the default, empty internal configuration file.
      return getInternalConfigFileName();
   }

   protected abstract String getInternalConfigFileName();

   protected abstract String getDefaultConfigFileName();

   protected abstract String getConfigPropertyName();

   protected abstract String getHandlerType();
}
