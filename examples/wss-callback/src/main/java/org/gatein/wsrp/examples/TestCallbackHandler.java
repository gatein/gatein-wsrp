/*
* JBoss, a division of Red Hat
* Copyright 2012, Red Hat Middleware, LLC, and individual contributors as indicated
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

package org.gatein.wsrp.examples;

import org.apache.ws.security.WSPasswordCallback;
import org.gatein.wsrp.wss.cxf.consumer.CurrentUserPasswordCallback;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;

public class TestCallbackHandler implements CallbackHandler
{

   @Override
   public void handle(Callback[] callbacks) throws IOException,
      UnsupportedCallbackException
   {

      //First check if we have any user name token call backs to add.
      //NOTE: only needed if using username tokens, and you want the currently authenticated users password added
      CurrentUserPasswordCallback currentUserPasswordCallback = new CurrentUserPasswordCallback();
      currentUserPasswordCallback.handle(callbacks);

      for (Callback callback : callbacks)
      {
         if (callback instanceof WSPasswordCallback)
         {
            WSPasswordCallback wsPWCallback = (WSPasswordCallback)callback;
            // since the CurrentUserPasswordCallback already handles the USERNAME_TOKEN case, we don't want to set it in this case
            if (wsPWCallback.getUsage() != WSPasswordCallback.USERNAME_TOKEN)
            {
               wsPWCallback.setPassword("wsrpAliasPassword");
            }
         }
      }
   }
}