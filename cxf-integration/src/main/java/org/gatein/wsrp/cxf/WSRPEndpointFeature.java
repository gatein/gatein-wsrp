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

import org.apache.cxf.Bus;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Server;
import org.apache.cxf.feature.AbstractFeature;
import org.apache.cxf.interceptor.InterceptorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A cxf feature whose role is to call the list of actual features from the FeatureIntegrationFactory.
 * 
 * Note: we can only pass a feature to the service methods using a cxf annotation, and not an external
 * configuration file (since that requires integrating spring with the application server running cxf).
 * Since the annotation requires a hardcoded feature class name, we need a class like this to allow for
 * runtime management of features to be added.
 * 
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class WSRPEndpointFeature extends AbstractFeature
{

   private static Logger log = LoggerFactory.getLogger(WSRPEndpointFeature.class);
   
   FeatureIntegratorFactory featureIntergrator;
   
   public WSRPEndpointFeature()
   {
      log.debug("Construct WSRPEndpointFeature");
      featureIntergrator = FeatureIntegratorFactory.getInstance();
   }

   @Override
   public void initialize(Bus bus)
   {
      for (AbstractFeature feature : featureIntergrator.getFeatures())
      {
         log.debug("Initializing Bus with " + feature.toString());
         feature.initialize(bus);
      }
   }
   
   @Override
   public void initialize(Client client, Bus bus)
   {
      for (AbstractFeature feature : featureIntergrator.getFeatures())
      {
         log.debug("Initializing Client with " + feature.toString());
         feature.initialize(client, bus);
      }
   }
   
   @Override
   public void initialize(InterceptorProvider interceptorProvider, Bus bus)
   {
      for (AbstractFeature feature : featureIntergrator.getFeatures())
      {
         log.debug("Initializing InterceptorProvider with " + feature.toString());
         feature.initialize(interceptorProvider, bus);
      }
   }
   
   @Override
   public void initialize(Server server, Bus bus)
   {
      for (AbstractFeature feature : featureIntergrator.getFeatures())
      {
         log.debug("Initializing Server with " + feature.toString());
         feature.initialize(server, bus);
      }
   }
}

