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
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

import org.gatein.wci.security.Credentials;
import org.gatein.wsrp.servlet.ServletAccess;
import org.jboss.aspects.security.SecurityClientInterceptor;
import org.jboss.security.SecurityAssociation;
import org.jboss.security.SecurityContextAssociation;
import org.jboss.security.SecurityContextUtil;
import org.jboss.security.client.SecurityClient;
import org.jboss.security.client.SecurityClientFactory;
import org.jboss.web.tomcat.security.SecurityAssociationValve;
import org.jboss.ws.extensions.security.Util;
import org.jboss.ws.extensions.security.element.SecurityHeader;
import org.jboss.ws.extensions.security.element.UsernameToken;
import org.jboss.ws.extensions.security.element.X509Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class ConsumerWSSecurityHandler implements SOAPHandler<SOAPMessageContext>
{
   private static Logger log = LoggerFactory.getLogger(ConsumerWSSecurityHandler.class);

   public void close(MessageContext arg0)
   {
      //Nothing to do for now
   }

   public boolean handleFault(SOAPMessageContext soapMessageContext)
   {
      return true;
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

   private boolean handleRequest(SOAPMessageContext soapMessageContext)
   {
      try
      {

         log.debug("Attempting to convert security context to WS-Security header");

         Principal principal = SecurityAssociation.getPrincipal();
         Object credential = SecurityAssociation.getCredential();

         SOAPMessageContext smc = (SOAPMessageContext) soapMessageContext;
         SOAPMessage message = smc.getMessage();
         SecurityHeader header = new SecurityHeader(message.getSOAPHeader().getOwnerDocument());

         if (principal == null)
         {

            log.debug("No principal to put in WS-Security header");
            return true;

         }
         else if (credential instanceof X509Certificate[])
         {

            log.debug("Adding X509Token to WSRP WS-Security header");
            header.addToken(new X509Token(((X509Certificate[]) credential)[0], message.getSOAPHeader()
                  .getOwnerDocument()));
            Element soapHeader = Util.findOrCreateSoapHeader(message.getSOAPHeader().getOwnerDocument()
                  .getDocumentElement());

            Element wsse = header.getElement();
            wsse.setAttributeNS(soapHeader.getNamespaceURI(), soapHeader.getPrefix() + ":mustUnderstand", "1");
            soapHeader.insertBefore(wsse, soapHeader.getFirstChild());

         }
         else if (credential instanceof String)
         {

            log.debug("Adding UsernameToken to WSRP WS-Security header");

            boolean digest = false;
            String nonce = null;
            String created = null;

            header.addToken(new UsernameToken(principal.getName(), (String) credential, message.getSOAPHeader()
                  .getOwnerDocument(), digest, nonce, created));
            Element soapHeader = Util.findOrCreateSoapHeader(message.getSOAPHeader().getOwnerDocument()
                  .getDocumentElement());

            Element wsse = header.getElement();
            wsse.setAttributeNS(soapHeader.getNamespaceURI(), soapHeader.getPrefix() + ":mustUnderstand", "1");
            soapHeader.insertBefore(wsse, soapHeader.getFirstChild());

         }
         else
         {

            log.warn("Principal exists, but can not propogate in WS-Security header");

         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
         //throw new JAXRPCException(e);
      }

      return true;
   }

   private boolean handleResponse(SOAPMessageContext soapMessageContext)
   {
      return false;
   }

   public Set<QName> getHeaders()
   {
      return null;
   }

}

