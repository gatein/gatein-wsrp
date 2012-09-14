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
package org.gatein.wsrp.cxf;

import java.io.File;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class Utils
{
   /**
    * System property to specify location of wsrp configuration files
    */
   public static final String GATEIN_WSRP_CONF_DIR = "gatein.wsrp.conf.dir";

   /**
    * System property to specify location of gatein configuration files
    */
   public static final String GATEIN_CONF_DIR = "gatein.conf.dir";
   
   /**
    * Default names for the cxf configuration directory
    */
   public static final String DEFAULT_WSRP_CONF_DIR_NAME = "wsrp";
   public static final String DEFAULT_WSRP_CXF_CONF_DIR_NAME = "cxf";
   
   public static String getWSRPCXFConfigDirectory()
   {
      String gateinWSRPConfDir = System.getProperty(GATEIN_WSRP_CONF_DIR);
      
      if (gateinWSRPConfDir == null)
      {
         String gateinConfDir = System.getProperty(GATEIN_CONF_DIR);
         gateinWSRPConfDir = gateinConfDir + File.separator + DEFAULT_WSRP_CONF_DIR_NAME;
      }
      
      String wsrpCXFConfDir = gateinWSRPConfDir + File.separator + DEFAULT_WSRP_CXF_CONF_DIR_NAME;
      
      return wsrpCXFConfDir;
   }
}

