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
   /** System property to specify location of wsrp configuration files */
   private static final String GATEIN_WSRP_CONF_DIR_NAME_PROPERTY = "gatein.wsrp.conf.dir";

   /** System property to specify location of gatein configuration files */
   private static final String GATEIN_CONF_DIR_NAME_PROPERTY = "gatein.conf.dir";

   /** Default names for the cxf configuration directory */
   private static final String DEFAULT_WSRP_CONF_DIR_NAME = "wsrp";
   private static final String DEFAULT_WSRP_CXF_CONF_DIR_NAME = "cxf";

   public static final File GATEIN_WSRP_CXF_CONF_DIR;

   static
   {
      File gateinWSRPConfDir = null;

      final String gateinWSRPConfDirName = System.getProperty(GATEIN_WSRP_CONF_DIR_NAME_PROPERTY);
      if (gateinWSRPConfDirName == null)
      {
         String gateinConfDir = System.getProperty(GATEIN_CONF_DIR_NAME_PROPERTY);
         gateinWSRPConfDir = new File(gateinConfDir, DEFAULT_WSRP_CONF_DIR_NAME);
      }

      GATEIN_WSRP_CXF_CONF_DIR = new File(gateinWSRPConfDir, DEFAULT_WSRP_CXF_CONF_DIR_NAME);
   }

   public static File getWSRPCXFConfigDirectory()
   {
      return GATEIN_WSRP_CXF_CONF_DIR;
   }
}

