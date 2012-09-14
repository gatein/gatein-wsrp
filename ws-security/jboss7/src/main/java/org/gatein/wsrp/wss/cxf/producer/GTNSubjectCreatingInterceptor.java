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
import javax.xml.namespace.QName;

import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.headers.Header;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.security.SecurityContext;
import org.apache.ws.security.WSUsernameTokenPrincipal;
import org.jboss.wsf.stack.cxf.security.authentication.SubjectCreatingInterceptor;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class GTNSubjectCreatingInterceptor extends SubjectCreatingInterceptor
{

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
      boolean modifiedActionProperty = false;
      String actionProperty = (String)this.getProperties().get("action");
      if (actionProperty.contains("gtn.UsernameToken.ifAvailable"))
      {
         QName wsseQName = new QName("http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-secext-1.0.xsd", "Security");
         Header wsseHeader = msg.getHeader(wsseQName);

         QName wsse11QName = new QName("http://docs.oasis-open.org/wss/oasis-wss-wssecurity-secext-1.1.xsd", "Security");
         Header wsse11Header = msg.getHeader(wsse11QName);
         
         //If we don't have the security header, don't do anything with the SubjectCreatingInterceptor
         if (wsseHeader == null && wsse11Header == null)
         {
            return;
         }
         else
         {
            modifiedActionProperty = true;
            this.setProperty("action", actionProperty.replace("gtn.UsernameToken.ifAvailable", "UsernameToken"));
         }

      }

      //handle the message here which will create the SecurityContext containing the username and password
      super.handleMessage(msg);
      
      //Replace the action property with the original property after the parent has handled the message
      //Note: needed since on the next invocation, the user may have logged out but the action property will  have already been set as "UsernameToken" and the above checks will not be performed.
      if (modifiedActionProperty)
      {
         this.setProperty("action", actionProperty);
      }
      
      SecurityContext context = msg.get(SecurityContext.class);

      Principal principal = context.getUserPrincipal();
      if (principal instanceof WSUsernameTokenPrincipal)
      {
         HttpServletRequest request = (HttpServletRequest)msg.get("HTTP.REQUEST");
         
         String username = ((WSUsernameTokenPrincipal)principal).getName();
         String password = ((WSUsernameTokenPrincipal)principal).getPassword();
         
         try
         {
            request.login(username, password);
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
   
}

