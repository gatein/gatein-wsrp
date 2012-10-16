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

import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.gatein.wsrp.wss.cxf.Utils;
import org.jboss.wsf.stack.cxf.security.authentication.SubjectCreatingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

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
      Map<String, Object> inPropertyMap = Utils.getWSS4JInterceptorConfiguration(false, true);
      Map<String, Object> outPropertyMap = Utils.getWSS4JInterceptorConfiguration(false, false);
      Map<String, Object> sciPropertyMap = Utils.getCXFConfiguration(false, GTN_SCI_INTERCEPTOR_CONFIG_FILE, "GTNSubjectCreatingInterceptor");

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
}

