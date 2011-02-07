/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2011, Red Hat Middleware, LLC, and individual                    *
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
package org.wsrp.wss.jboss5.handlers;

import java.security.Principal;
import java.security.cert.X509Certificate;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.apache.catalina.connector.Request;
import org.gatein.wsrp.servlet.ServletAccess;
import org.jboss.web.tomcat.security.SecurityAssociationValve;
import org.jboss.ws.extensions.security.Constants;
import org.jboss.ws.extensions.security.Util;
import org.jboss.ws.extensions.security.element.BinarySecurityToken;
import org.jboss.ws.extensions.security.element.UsernameToken;
import org.jboss.ws.extensions.security.element.X509Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class ProducerWSSecurityHandler implements SOAPHandler<SOAPMessageContext>
{
   private static Logger log = LoggerFactory.getLogger(ProducerWSSecurityHandler.class);

   public Set<QName> getHeaders()
   {
      Set<QName> qNames = new HashSet<QName>();
      qNames.add(Constants.WSSE_HEADER_QNAME);
      return qNames;
   }

   public void close(MessageContext arg0)
   {
      //Nothing to do for now
   }

   public boolean handleFault(SOAPMessageContext arg0)
   {
      return false;
   }

   public boolean handleMessage(SOAPMessageContext soapMessageContext)
   {
      // outbound message means request
      if (Boolean.TRUE.equals(soapMessageContext.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY)))
      {
         return handleRequest(soapMessageContext);
      }
      else
      {
         return handleResponse(soapMessageContext);
      }
   }

   private boolean handleResponse(SOAPMessageContext soapMessageContext)
   {
      try
      {
         log.debug("Attempting to extract WS-Security header and create user context");
         //final Request r = (Request)ServletAccess.getRequest();
         final Request r = (Request) (SecurityAssociationValve.activeRequest.get());

         SOAPMessageContext smc = (SOAPMessageContext) soapMessageContext;
         SOAPMessage message = smc.getMessage();

         Element headerElement = Util.findElement(message.getSOAPHeader().getOwnerDocument().getDocumentElement(),
               "Security", Constants.WSSE_NS);

         if (headerElement != null)
         {
            Element child = Util.getFirstChildElement(headerElement);
            String tag = child.getLocalName();

            if (tag.equals("BinarySecurityToken"))
            {

               log.debug("Attempting to authenticate with a BinarySecurityToken");
               BinarySecurityToken token = BinarySecurityToken.createBinarySecurityToken(child);
               if (token instanceof X509Token)
               {
                  X509Certificate cert = ((X509Token) token).getCert();
                  Principal principal = r.getContext().getRealm().authenticate(new X509Certificate[]
                  {cert});
                  if (principal != null)
                  {
                     r.setAuthType(tag);
                     r.setUserPrincipal(principal);
                  }
                  else
                  {
                     throw new Exception("Could not authenticate principal from BinarySecurityToken");
                  }
               }
               else
               {
                  //throw new UnsupportedSecurityTokenException(tag + ": Only an X509 BinarySecurityToken is suppported.");
                  System.out.println(tag + ": Only an X509 BinarySecurityToken is suppported.");
               }

            }
            else if (tag.equals("UsernameToken"))
            {

               log.debug("Attempting to authenticate with a UsernameToken");
               UsernameToken token = new UsernameToken(child);
               Principal principal = r.getContext().getRealm().authenticate(token.getUsername(), token.getPassword());
               if (principal != null)
               {
                  r.setAuthType(tag);
                  r.setUserPrincipal(principal);
               }
               else
               {
                  throw new Exception("Could not authenticate from UsernameToken");
               }

            }
            else
            {

               //throw new UnsupportedSecurityTokenException(tag + ": Only BinarySecurityToken or UsernameToken is suppported.");
               System.out.println(tag + ": Only BinarySecurityToken or UsernameToken is suppported.");

            }

            if (Util.getNextSiblingElement(headerElement) != null)
            {
               //throw new UnsupportedSecurityTokenException(tag + ": Only a single X509 BinarySecurityToken or UsernameToken is suppported.");
               System.out.println(tag + ": Only a single X509 BinarySecurityToken or UsernameToken is suppported.");
            }

            //Remove the header since it's been processed
            headerElement.getParentNode().removeChild(headerElement);
         }
      }
      catch (Exception e)
      {
         //throw new JAXRPCException(e);
         e.printStackTrace();
      }

      return true;
   }

   private boolean handleRequest(SOAPMessageContext soapMessageContext)
   {
      //The Producer should only handle setting the header for now
      return false;
   }

}
