/*
 * JBoss, a division of Red Hat
 * Copyright 2009, Red Hat Middleware, LLC, and individual
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

package org.gatein.wsrp.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;

import javax.xml.namespace.QName;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPMessage;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.Set;

/**
 * JAX-WS Handler that strips the SOAP Message of any WSRP extensions.
 *
 * @author <a href="mailto:Anil.Saldhana@jboss.org">Anil Saldhana</a>
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 12277 $
 * @since 2.4 (Apr 26, 2006)
 */
public class WSRPExtensionHandler implements SOAPHandler<SOAPMessageContext>
{
   private static Logger log = LoggerFactory.getLogger(WSRPExtensionHandler.class);
   private boolean debug = false;
   private boolean removeExtensions = true;
   private static final String EXTENSIONS = "extensions";

   public boolean handleMessage(SOAPMessageContext messageContext)
   {
      removeExtensions(messageContext);
      return true;
   }

   public boolean handleFault(SOAPMessageContext messageContext)
   {
      return true;
   }

   public void close(MessageContext messageContext)
   {
      // nothing to do
   }

   public Set<QName> getHeaders()
   {
      return null;
   }

   private void removeExtensions(SOAPMessageContext msgContext)
   {
      SOAPMessage soapMessage = msgContext.getMessage();
      try
      {
         if (debug)
         {
            soapMessage.writeTo(System.out);
         }

         if (removeExtensions)
         {
            SOAPBody soapBody = soapMessage.getSOAPBody();
            traverseAndRemoveExtensions(soapBody);
         }
      }
      catch (Exception e)
      {
         log.error("Error in WSRPExtensionHandler.removeExtensions:", e);
      }
   }

   /**
    * Remove extensions nodes recursively, depth-first.
    *
    * @param node
    */
   private void traverseAndRemoveExtensions(org.w3c.dom.Node node)
   {
      NodeList children = node.getChildNodes();
      int childrenNb = children.getLength();
      for (int i = 0; i < childrenNb; i++)
      {
         org.w3c.dom.Node child = children.item(i);

         // only process elements
         if (org.w3c.dom.Node.ELEMENT_NODE == child.getNodeType())
         {
            String name = child.getLocalName();

            // if we found an extension, remove it or continue
            if (EXTENSIONS.equals(name))
            {
               if (debug)
               {
                  log.debug("Extensions removed on " + name);
               }
               node.removeChild(child);
               break;
            }
            else
            {
               traverseAndRemoveExtensions(child);
            }
         }
      }
   }
}
