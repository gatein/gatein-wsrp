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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/** @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a> */
public class DOMUtils
{
   static Element createElement(String namespaceURI, String name)
   {
      DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
      builderFactory.setNamespaceAware(true);
      try
      {
         final DocumentBuilder builder = builderFactory.newDocumentBuilder();
         final Document document = builder.newDocument();
         return document.createElementNS(namespaceURI, name);
      }
      catch (ParserConfigurationException e)
      {
         throw new RuntimeException("Couldn't get a DocumentBuilder", e);
      }
   }

   static Node addChild(Node parent, String namespaceURI, String childName)
   {
      final Element child = parent.getOwnerDocument().createElementNS(namespaceURI, childName);
      return parent.appendChild(child);
   }
}
