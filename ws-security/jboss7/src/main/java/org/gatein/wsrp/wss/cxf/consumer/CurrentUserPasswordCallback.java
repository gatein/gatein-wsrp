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
package org.gatein.wsrp.wss.cxf.consumer;

import org.apache.ws.security.WSPasswordCallback;
import org.gatein.wci.security.Credentials;
import org.gatein.wsrp.wss.CredentialsAccess;
import org.gatein.wsrp.wss.credentials.CredentialsAccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class CurrentUserPasswordCallback implements CallbackHandler
{
   private static Logger log = LoggerFactory.getLogger(CurrentUserPasswordCallback.class);

   @Override
   public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException
   {
      for (Callback callback : callbacks)
      {
         if (callback instanceof WSPasswordCallback)
         {
            WSPasswordCallback wspasswordCallBack = (WSPasswordCallback)callback;

            //This callback is only for Username Tokens, not for authentication/signing of the soap message
            if (wspasswordCallBack.getUsage() == (WSPasswordCallback.USERNAME_TOKEN))
            {
               CredentialsAccessor credentialsAccessor = CredentialsAccess.getInstance().getCredentialsAccessor();

               if (credentialsAccessor != null && credentialsAccessor.getCredentials() != null)
               {
                  Credentials credentials = credentialsAccessor.getCredentials();
                  if (credentials.getUsername().equals(wspasswordCallBack.getIdentifier()))
                  {
                     wspasswordCallBack.setPassword(credentials.getPassword());
                  }
                  else
                  {
                     log.warn("The username in the callback does not match the currently authenticated user. Password not added to callback.");
                  }
               }
               else
               {
                  log.warn("Could not find credentials to put in WS-Security header");
               }
            }
         }
      }
   }

}

