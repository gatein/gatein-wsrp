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

import java.security.Principal;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.auth.Subject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.interceptor.Fault;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSUsernameTokenPrincipal;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.jboss.wsf.stack.cxf.security.authentication.SubjectCreatingInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class GTNSubjectCreatingInterceptor extends SubjectCreatingInterceptor
{
   private static Logger log = LoggerFactory.getLogger(GTNSubjectCreatingInterceptor.class);

   private static final String USERNAME_TOKEN_IFAVAILABLE = "gtn.UsernameToken.ifAvailable";
   
   protected boolean gtnUsernameTokenIfAvailable = false;
   
   private WSUsernameTokenPrincipal wsUsernameTokenPrincipal = null;
   
   public GTNSubjectCreatingInterceptor()
   {
      this(new HashMap<String, Object>()); 
   }

   public GTNSubjectCreatingInterceptor(Map<String, Object> properties)
   {
      super(properties);
   }
   
   @Override
   public void handleMessage(SoapMessage msg) throws Fault
   {
      String actionProperty = (String)this.getProperties().get(WSHandlerConstants.ACTION);
      if (actionProperty.contains(USERNAME_TOKEN_IFAVAILABLE))
      {
         gtnUsernameTokenIfAvailable = true;
         this.setProperty(WSHandlerConstants.ACTION, actionProperty.replace(USERNAME_TOKEN_IFAVAILABLE, WSHandlerConstants.USERNAME_TOKEN));
      }

      try
      {
         //handle the message here which will create the SecurityContext containing the username and password
         super.handleMessage(msg);
      }
      finally
      {
         //Replace the action property with the original property after the parent has handled the message
         //Note: needed since on the next invocation, the user may have logged out but the action property will  have already been set as "UsernameToken" and the above checks will not be performed.
         if (gtnUsernameTokenIfAvailable)
         {
            this.setProperty(WSHandlerConstants.ACTION, actionProperty);
         }
      }
    
      if (wsUsernameTokenPrincipal != null)
      {
         HttpServletRequest request = (HttpServletRequest)msg.get("HTTP.REQUEST");
         
         String username = wsUsernameTokenPrincipal.getName();
         String password = wsUsernameTokenPrincipal.getPassword();
         
         wsUsernameTokenPrincipal = null;
         try
         {
            //only perform a login if the user is not already authenticated
            if (request.getRemoteUser() == null)
            {
               request.login(username, password);
            }
         }
         catch (ServletException e)
         {
            // FIXME
            e.printStackTrace();
         }
      }
   }
   
   /* NOTE: this method should be removed when JBWS-3541 has been fixed in the supported version of JBossAS
    * See https://issues.jboss.org/browse/JBWS-3541
    */
   @Override
   public Subject createSubject(String name, String password, boolean isDigest, String nonce, String created)
   {
      Subject originalSubject = super.createSubject(name, password, isDigest, nonce, created);
      
      Set<Principal> principals = originalSubject.getPrincipals();
      if (principals.iterator().next().getName() != name)
      {
         Principal namePrincipal = null;
         for (Principal principal: principals)
         {
            if (principal.getName().equals(name))
            {
               namePrincipal = principal;
               break;
            }
         }
         
         if (namePrincipal != null)
         {
            principals.remove(namePrincipal);
            Set<Principal> newPrincipals = new LinkedHashSet<Principal>();
            newPrincipals.add(namePrincipal);
            newPrincipals.addAll(principals);

            originalSubject.getPrincipals().clear();
            originalSubject.getPrincipals().addAll(newPrincipals);
         }           
      }
      
      
      return originalSubject;
   }
   

   @Override
   protected boolean checkReceiverResultsAnyOrder(List<WSSecurityEngineResult> wsResults, List<Integer> actions)
   {
      // if the action contains gtn.UsernameToken.ifAvailable then we need to override how this method works
      // so that we don't run into an error that the actions are mismatched. Otherwise the method will fail
      // if we have a username token in the soap message but didn't specify it, or the other way around.
      wsUsernameTokenPrincipal = null;
      if (gtnUsernameTokenIfAvailable)
      {
         boolean foundUsernameTokenResult = false;
         
         for (WSSecurityEngineResult wsResult: wsResults)
         {
            Integer actInt = (Integer) wsResult.get(WSSecurityEngineResult.TAG_ACTION);
            if (actInt == WSConstants.UT)
            {
               //usernametokenResult = wsResult;
               foundUsernameTokenResult = true;

               //since we already have the result and the result contains the username and
               //password, its easiest to just grab the data here and store it for later.
               Object principal = wsResult.get(WSSecurityEngineResult.TAG_PRINCIPAL);
               if (principal != null && principal instanceof WSUsernameTokenPrincipal)
               {
                  this.wsUsernameTokenPrincipal = (WSUsernameTokenPrincipal)principal;
               }

               break;
            }
         }

         if (foundUsernameTokenResult && !actions.contains(WSConstants.UT))
         {
            actions.add(WSConstants.UT);
         }
         else if (!foundUsernameTokenResult && actions.contains(WSConstants.UT))
         {
            actions.remove(actions.indexOf(WSConstants.UT)); //NOTE:careful here when using remove(Integer) since it removes the index not the Object!
         }

      }
      return super.checkReceiverResults(wsResults, actions);
   }
}

