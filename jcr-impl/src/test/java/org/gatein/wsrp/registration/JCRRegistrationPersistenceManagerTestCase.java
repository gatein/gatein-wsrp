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

package org.gatein.wsrp.registration;

import org.chromattic.api.ChromatticBuilder;
import org.gatein.registration.AbstractRegistrationPersistenceManagerTestCase;
import org.gatein.registration.RegistrationPersistenceManager;
import org.gatein.wsrp.jcr.BaseChromatticPersister;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class JCRRegistrationPersistenceManagerTestCase extends AbstractRegistrationPersistenceManagerTestCase
{
   JCRRegistrationPersistenceManager persistenceManager;

   @Override
   public void setUp() throws Exception
   {
      super.setUp();
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
      persister.initializeBuilderFor(JCRRegistrationPersistenceManager.mappingClasses);
      persistenceManager = new JCRRegistrationPersistenceManager(persister);
   }

   @Override
   public RegistrationPersistenceManager getManager() throws Exception
   {
      return persistenceManager;
   }
}
