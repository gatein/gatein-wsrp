/*
 * JBoss, a division of Red Hat
 * Copyright 2010, Red Hat Middleware, LLC, and individual
 * contributors as indicated by the @authors tag. See the
 * copyright.txt in the distribution for a full listing of
 * individual contributors.
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

package org.gatein.wsrp.protocol.v1;

import org.gatein.wsrp.spec.v1.WSRP1TypeFactory;
import org.gatein.wsrp.test.ExtendedAssert;
import org.jboss.logging.Logger;
import org.oasis.wsrp.v1.V1OperationFailed;
import org.oasis.wsrp.v1.V1RegistrationContext;
import org.oasis.wsrp.v1.V1ReleaseSessions;

import java.util.Arrays;
import java.util.List;

/**
 * Tests the behavior of the ReleaseSession method.
 *
 * @author Matt Wringe
 */
public class ReleaseSessionTestCase extends NeedPortletHandleTest
{

   // default portlet war used in this test
   private static final String DEFAULT_SESSION_PORTLET_WAR = "test-session-portlet.war";

   private static final Logger log = Logger.getLogger(ReleaseSessionTestCase.class);


   protected String getMostUsedPortletWARFileName()
   {
      return DEFAULT_SESSION_PORTLET_WAR;
   }

   public ReleaseSessionTestCase() throws Exception
   {
      super("SessionWar", DEFAULT_SESSION_PORTLET_WAR);
      log.debug("Instantiating ReleaseSessionTestCase");
   }


   public void testReleaseSession() throws Exception
   {
      // possible registration contexts are: actual RegistrationContext, null, and a made up value
      V1RegistrationContext fakeRegContext = WSRP1TypeFactory.createRegistrationContext("Fake Registration Handle");
      V1RegistrationContext[] regContexts = new V1RegistrationContext[]{null, null, fakeRegContext};

      // possible types of sessionIDs include null and a made up value.
      // Note: a valid session id cannot be used since the sessionID should never be sent to the consumer
      String nullSessionID = null;
      String fakeSessionID = "Fake Session ID";

      String[][] sessionIDs = new String[][]{{nullSessionID},
         {nullSessionID, nullSessionID},
         {fakeSessionID},
         {fakeSessionID, fakeSessionID},
         {fakeSessionID, nullSessionID},
         {nullSessionID, fakeSessionID}};

      for (int i = 0; i < regContexts.length; i++)
      {
         for (String[] sessionID : sessionIDs)
         {
            V1ReleaseSessions releaseSession = WSRP1TypeFactory.createReleaseSessions(regContexts[i], Arrays.asList(sessionID));
            releaseSessions(releaseSession, false, i);
            releaseSessions(releaseSession, true, i);
         }
      }
   }

   private void releaseSessions(V1ReleaseSessions releaseSessions, boolean useRegistration, int index) throws Exception
   {
      setUp();
      try
      {
         if (useRegistration)
         {
            configureRegistrationSettings(true, false);

            // faking correct registration context when we're supposed to have one... previous impl registered consumer
            // all the time but this cannot be done anymore since we prevent registering if no registration is required
            // so we need to wait for the proper case to init the registration context... Hackish! :(
            if (index == 0)
            {
               releaseSessions.setRegistrationContext(registerConsumer());
            }
         }
         log.info(getSetupString(releaseSessions));
         producer.releaseSessions(releaseSessions);
         ExtendedAssert.fail("ReleaseSessions did not thrown an OperationFailed Fault." + getSetupString(releaseSessions));
      }
      catch (V1OperationFailed operationFailedFault)
      {
         // expected fault.
      }
      finally
      {
         tearDown();
      }
   }

   private String getSetupString(V1ReleaseSessions releaseSessions)
   {
      StringBuffer message = new StringBuffer("ReleaseSessions Setup:\n");

      if (releaseSessions == null)
      {
         message.append(" ReleaseSessions : null");
      }
      else
      {
         V1RegistrationContext regContext = releaseSessions.getRegistrationContext();
         List<String> sessionIDs = releaseSessions.getSessionIDs();
         message.append(" RegistrationContext : ").append(regContext != null ? regContext.getRegistrationHandle() : null);
         message.append(" | SessionIDs : ");
         if (sessionIDs != null)
         {
            for (int i = 0; i < sessionIDs.size(); i++)
            {
               message.append(sessionIDs.get(i)).append(i == sessionIDs.size() - 1 ? "" : ", ");
            }
         }

      }

      if (producer.getConfigurationService().getConfiguration().getRegistrationRequirements().isRegistrationRequired())
      {
         message.append(" | with registration required.");
      }

      return message.toString();
   }

}