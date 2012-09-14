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
package org.gatein.wsrp.wss.cxf.producer;

import java.io.File;
import java.util.Map;

import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.gatein.wsrp.wss.cxf.Utils;
import org.jboss.wsf.stack.cxf.security.authentication.SubjectCreatingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class WSRPWSSecurityFeature extends AbstractFeature
{
   private static Logger log = LoggerFactory.getLogger(WSRPWSSecurityFeature.class);
   
   protected static String GTN_SCI_INTERCEPTOR_CONFIG_FILE = "GTNSubjectCreatingInterceptor.properties";
   
   public WSRPWSSecurityFeature()
   {
      log.debug("WSRPWSSecurityFeature Constructed");
   }

   @Override
   public void initialize(Server server, Bus bus)
   {
      System.out.println("WSRPWSSecurityFeature initialize(Server server, Bus bus)");
      Map<String, Object> inPropertyMap = getWSS4JInInterceptorProperties();
      Map<String, Object> outPropertyMap = getWSS4JOutInterceptorProperties();
      Map<String, Object> sciPropertyMap = getGTNSubjectCreatingInterceptorProperties();

      if (sciPropertyMap != null)
      {
         SubjectCreatingInterceptor sci = new GTNSubjectCreatingInterceptor(sciPropertyMap);
         server.getEndpoint().getInInterceptors().add(sci);
      }
      
      if (inPropertyMap != null)
      {
         WSS4JInInterceptor inInterceptor = new WSS4JInInterceptor(inPropertyMap);
         server.getEndpoint().getInInterceptors().add(inInterceptor);
      }
      
      if (outPropertyMap != null)
      {
         WSS4JOutInterceptor outInterceptor = new WSS4JOutInterceptor(outPropertyMap);
         server.getEndpoint().getOutInterceptors().add(outInterceptor);
      }
   }
   
   protected Map<String, Object> getWSS4JInInterceptorProperties()
   {
      String wss4jInInterceptorConfigPath = Utils.PRODUCER_CONF_DIR_NAME + File.separator + Utils.WSS4J_ININTERCEPTOR_PROPERTY_FILE;
      
      Map<String, Object> inInterceptorProperties = Utils.getCXFConfigProperties(wss4jInInterceptorConfigPath);
      
      if (inInterceptorProperties == null)
      {
         log.debug("The WSS4JInInterceptor configuration file could not be found. No WSS4JInInterceptor will be added to the wsrp producer.");
      }
      
      return inInterceptorProperties;
   }
   
   protected Map<String, Object> getWSS4JOutInterceptorProperties()
   {
      String wss4jOutInterceptorConfigPath = Utils.PRODUCER_CONF_DIR_NAME + File.separator + Utils.WSS4J_OUTINTERCEPTOR_PROPERTY_FILE;
      
      Map<String, Object> outInterceptorProperties = Utils.getCXFConfigProperties(wss4jOutInterceptorConfigPath);
      
      if (outInterceptorProperties == null)
      {
         log.debug("The WSS4JOutInterceptor configuration file could not be found. No WSS4JOutInterceptor will be added to the wsrp producer.");
      }
      
      return outInterceptorProperties;
   }
   
   
   protected Map<String, Object> getGTNSubjectCreatingInterceptorProperties()
   {
      String gtnSCInterceptorConfigPath = Utils.PRODUCER_CONF_DIR_NAME + File.separator + GTN_SCI_INTERCEPTOR_CONFIG_FILE;
      
      Map<String, Object> interceptorProperties = Utils.getCXFConfigProperties(gtnSCInterceptorConfigPath);
      
      if (interceptorProperties == null)
      {
         log.debug("The GTNSubjectCreatingInterceptor configuration file could not be found. No GTNSubjectCreatingInterceptor will be added to the wsrp producer.");
      }
      
      return interceptorProperties;
   }
}

