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

package org.gatein.wsrp.producer.config;

import org.chromattic.api.ChromatticBuilder;
import org.gatein.wsrp.jcr.BaseChromatticPersister;
import org.gatein.wsrp.jcr.ChromatticPersister;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.Session;
import java.net.URL;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class JCRProducerConfigurationServiceTestCase extends ProducerConfigurationTestCase
{
   @Override
   public void setUp() throws Exception
   {
      String workspaceName = "/wsrp-jcr-test" + Math.round(Math.abs(100000 * Math.random()));
      BaseChromatticPersister persister = new BaseChromatticPersister(workspaceName)
      {
         @Override
         protected void setBuilderOptions(ChromatticBuilder builder)
         {
            builder.setOptionValue(ChromatticBuilder.ROOT_NODE_PATH, workspaceName);
            builder.setOptionValue(ChromatticBuilder.ROOT_NODE_TYPE, "nt:unstructured");
            builder.setOptionValue(ChromatticBuilder.CREATE_ROOT_NODE, true);
         }
      };
      persister.initializeBuilderFor(JCRProducerConfigurationService.mappingClasses);
      service = new JCRProducerConfigurationService(persister);
   }

   @Override
   protected void tearDown() throws Exception
   {
      // remove node containing consumer informations so that we can start with a clean state
      final ChromatticPersister persister = getService().getPersister();
      final Session session = persister.getSession().getJCRSession();
      final Node rootNode = session.getRootNode();
      final NodeIterator nodes = rootNode.getNodes();
      while (nodes.hasNext())
      {
         nodes.nextNode().remove();
      }

      // then save
      persister.closeSession(true);
   }

   private JCRProducerConfigurationService getService()
   {
      return (JCRProducerConfigurationService)service;
   }

   @Override
   protected URL getConfigurationURL()
   {
      return null;
   }

   @Override
   protected ProducerConfiguration getProducerConfiguration(URL location) throws Exception
   {
      if (location != null)
      {
         getService().setConfigurationIS(location.openStream());
      }
      getService().loadConfiguration();
      return service.getConfiguration();
   }
}
