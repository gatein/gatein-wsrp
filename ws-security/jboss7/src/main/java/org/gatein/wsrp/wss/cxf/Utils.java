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
package org.gatein.wsrp.wss.cxf;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class Utils
{

   private static Logger log = LoggerFactory.getLogger(Utils.class);
   
   public static final String WS_SECURITY_CONF_DIR_NAME = "ws-security";
   public static final String CONSUMER_CONF_DIR_NAME = "consumer";
   public static final String PRODUCER_CONF_DIR_NAME = "producer";
   
   public static final String WSS4J_ININTERCEPTOR_PROPERTY_FILE = "WSS4JInInterceptor.properties";
   public static final String WSS4J_OUTINTERCEPTOR_PROPERTY_FILE = "WSS4JOutInterceptor.properties";
   
   public static Map<String, Object> getCXFConfigProperties(String path)
   {
      String interceptorPropertyFileName = getCXFWSSecurityConfigDir() + File.separator + path;
      try
      {
         Map<String, Object> outProperties = new HashMap<String, Object>();

         File interceptorPropertyFile = new File (interceptorPropertyFileName);
         if (interceptorPropertyFile.exists())
         {
            Properties properties = new Properties();
            properties.load(new FileInputStream(interceptorPropertyFile));
            for (String propName : properties.stringPropertyNames())
            {
               outProperties.put(propName, properties.get(propName));
            }
         }
         else
         {
            log.debug("The interceptor property file (" + interceptorPropertyFileName + ") does not exist.");
            return null;
         }
         return outProperties;
      }
      catch (Exception e)
      {
         log.error("Exception occured trying to read the interceptor property file (" + interceptorPropertyFileName + ").", e);
      }
      return null;
   }
   
   protected static String getCXFWSSecurityConfigDir()
   {
      return org.gatein.wsrp.cxf.Utils.getWSRPCXFConfigDirectory() + File.separator + WS_SECURITY_CONF_DIR_NAME;
   }
}

