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

package org.gatein.wsrp.handler;

import junit.framework.TestCase;
import org.gatein.wsrp.test.handler.MockSOAPMessage;
import org.gatein.wsrp.test.handler.MockSOAPMessageContext;

import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPException;
import javax.xml.ws.handler.soap.SOAPMessageContext;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class WSRPExtensionHandlerTestCase extends TestCase
{
   private WSRPExtensionHandler handler;

   @Override
   protected void setUp() throws Exception
   {
      handler = new WSRPExtensionHandler();
   }

   public void testRemoveExtensions() throws SOAPException
   {
      MockSOAPMessage message = new MockSOAPMessage();
      message.setMessageBody("<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'\n" +
         "\txmlns:ns0='urn:oasis:names:tc:wsrp:v1:types'\n" +
         "\txmlns:xsd='http://www.w3.org/2001/XMLSchema'\n" +
         "\txmlns:xsi='http://www.w3.org/2001/XMLSchema-instance'>\n" +
         "\t<env:Body>\n" +
         "\t\t<ns0:getMarkup xmlns:ns0='urn:oasis:names:tc:wsrp:v1:types'>\n" +
         "\t\t\t<ns0:registrationContext\n" +
         "\t\t\t\txmlns:ns0='urn:oasis:names:tc:wsrp:v1:types'>\n" +
         "\t\t\t\t<ns0:registrationHandle\n" +
         "\t\t\t\t\txmlns:ns0='urn:oasis:names:tc:wsrp:v1:types'>\n" +
         "\t\t\t\t\t17\n" +
         "\t\t\t\t</ns0:registrationHandle>\n" +
         "\t\t\t\t<ns0:registrationState\n" +
         "\t\t\t\t\txmlns:ns0='urn:oasis:names:tc:wsrp:v1:types' />\n" +
         "\t\t\t</ns0:registrationContext>\n" +
         "\t\t\t<ns0:portletContext\n" +
         "\t\t\t\txmlns:ns0='urn:oasis:names:tc:wsrp:v1:types'>\n" +
         "\t\t\t\t<ns0:portletHandle\n" +
         "\t\t\t\t\txmlns:ns0='urn:oasis:names:tc:wsrp:v1:types'>\n" +
         "\t\t\t\t\t_18\n" +
         "\t\t\t\t</ns0:portletHandle>\n" +
         "\t\t\t\t<ns0:portletState\n" +
         "\t\t\t\t\txmlns:ns0='urn:oasis:names:tc:wsrp:v1:types' />\n" +
         "\t\t\t</ns0:portletContext>\n" +
         "\t\t\t<ns0:runtimeContext\n" +
         "\t\t\t\txmlns:ns0='urn:oasis:names:tc:wsrp:v1:types'>\n" +
         "\t\t\t\t<ns0:userAuthentication xmlns:ns0='urn:oasis:names:tc:wsrp:v1:types'>\n" +
         "\t\t\t\t\twsrp:none\n" +
         "\t\t\t\t</ns0:userAuthentication>\n" +
         "\t\t\t\t<ns0:portletInstanceKey\n" +
         "\t\t\t\t\txmlns:ns0='urn:oasis:names:tc:wsrp:v1:types'>\n" +
         "\t\t\t\t\t949994222\n" +
         "\t\t\t\t</ns0:portletInstanceKey>\n" +
         "\t\t\t\t<ns0:namespacePrefix\n" +
         "\t\t\t\t\txmlns:ns0='urn:oasis:names:tc:wsrp:v1:types'>\n" +
         "\t\t\t\t\t__ns949994222_\n" +
         "\t\t\t\t</ns0:namespacePrefix>\n" +
         "\t\t\t</ns0:runtimeContext>\n" +
         "\t\t\t<ns0:userContext\n" +
         "\t\t\t\txmlns:ns0='urn:oasis:names:tc:wsrp:v1:types'>\n" +
         "\t\t\t\t<ns0:userContextKey\n" +
         "\t\t\t\t\txmlns:ns0='urn:oasis:names:tc:wsrp:v1:types'>\n" +
         "\t\t\t\t\twsrp:minimal\n" +
         "\t\t\t\t</ns0:userContextKey>\n" +
         "\t\t\t\t<ns0:profile\n" +
         "\t\t\t\t\txmlns:ns0='urn:oasis:names:tc:wsrp:v1:types' />\n" +
         "\t\t\t</ns0:userContext>\n" +
         "\t\t\t<ns0:markupParams\n" +
         "\t\t\t\txmlns:ns0='urn:oasis:names:tc:wsrp:v1:types'>\n" +
         "\t\t\t\t<ns0:secureClientCommunication xmlns:ns0='urn:oasis:names:tc:wsrp:v1:types'>\n" +
         "\t\t\t\t\tfalse\n" +
         "\t\t\t\t\t</ns0:secureClientCommunication>\n" +
         "\t\t\t\t\t<ns0:locales\n" +
         "\t\t\t\t\t\txmlns:ns0='urn:oasis:names:tc:wsrp:v1:types'>\n" +
         "\t\t\t\t\t\ten-US\n" +
         "\t\t\t\t\t</ns0:locales>\n" +
         "\t\t\t\t\t<ns0:mimeTypes\n" +
         "\t\t\t\t\t\txmlns:ns0='urn:oasis:names:tc:wsrp:v1:types'>\n" +
         "\t\t\t\t\t\ttext/html\n" +
         "\t\t\t\t\t</ns0:mimeTypes>\n" +
         "\t\t\t\t\t<ns0:mimeTypes\n" +
         "\t\t\t\t\t\txmlns:ns0='urn:oasis:names:tc:wsrp:v1:types'>\n" +
         "\t\t\t\t\t\ttext/xml\n" +
         "\t\t\t\t\t</ns0:mimeTypes>\n" +
         "\t\t\t\t\t<ns0:mimeTypes\n" +
         "\t\t\t\t\t\txmlns:ns0='urn:oasis:names:tc:wsrp:v1:types'>\n" +
         "\t\t\t\t\t\ttext/vnd.oracle.mobilexml\n" +
         "\t\t\t\t\t</ns0:mimeTypes>\n" +
         "\t\t\t\t\t<ns0:mimeTypes\n" +
         "\t\t\t\t\t\txmlns:ns0='urn:oasis:names:tc:wsrp:v1:types'>\n" +
         "\t\t\t\t\t\t*\n" +
         "\t\t\t\t\t</ns0:mimeTypes>\n" +
         "\t\t\t\t\t<ns0:mode\n" +
         "\t\t\t\t\t\txmlns:ns0='urn:oasis:names:tc:wsrp:v1:types'>\n" +
         "\t\t\t\t\t\twsrp:view\n" +
         "\t\t\t\t\t</ns0:mode>\n" +
         "\t\t\t\t\t<ns0:windowState\n" +
         "\t\t\t\t\t\txmlns:ns0='urn:oasis:names:tc:wsrp:v1:types'>\n" +
         "\t\t\t\t\t\twsrp:normal\n" +
         "\t\t\t\t\t</ns0:windowState>\n" +
         "\t\t\t\t\t<ns0:clientData\n" +
         "\t\t\t\t\t\txmlns:ns0='urn:oasis:names:tc:wsrp:v1:types'>\n" +
         "\t\t\t\t\t\t<ns0:userAgent\n" +
         "\t\t\t\t\t\t\txmlns:ns0='urn:oasis:names:tc:wsrp:v1:types'>\n" +
         "\t\t\t\t\t\t\tMozilla/5.0 (Windows; U; Windows NT 5.1;\n" +
         "\t\t\t\t\t\t\ten-US; rv:1.9.0.3) Gecko/2008092417\n" +
         "\t\t\t\t\t\t\tFirefox/3.0.3\n" +
         "\t\t\t\t\t\t</ns0:userAgent>\n" +
         "\t\t\t\t\t\t<ns0:extensions\n" +
         "\t\t\t\t\t\t\txmlns:ns0='urn:oasis:names:tc:wsrp:v1:types'>\n" +
         "\t\t\t\t\t\t\t<ns1:GenericExtension\n" +
         "\t\t\t\t\t\t\t\txmlns:ns1='http://xmlns.oracle.com/portal/wsrp/v1'>\n" +
         "\t\t\t\t\t\t\t\t<ns0:NamedString name='CONNECTION'\n" +
         "\t\t\t\t\t\t\t\t\txmlns:ns0='urn:oasis:names:tc:wsrp:v1:types'>\n" +
         "\t\t\t\t\t\t\t\t\t<ns0:value\n" +
         "\t\t\t\t\t\t\t\t\t\txmlns:ns0='urn:oasis:names:tc:wsrp:v1:types'>\n" +
         "\t\t\t\t\t\t\t\t\t\tkeep-alive\n" +
         "\t\t\t\t\t\t\t\t\t</ns0:value>\n" +
         "\t\t\t\t\t\t\t\t</ns0:NamedString>\n" +
         "\t\t\t\t\t\t\t\t<ns0:NamedString name='ACCEPT-ENCODING'\n" +
         "\t\t\t\t\t\t\t\t\txmlns:ns0='urn:oasis:names:tc:wsrp:v1:types'>\n" +
         "\t\t\t\t\t\t\t\t\t<ns0:value xmlns:ns0='urn:oasis:names:tc:wsrp:v1:types'>\n" +
         "\t\t\t\t\t\t\t\t\t\tgzip,deflate\n" +
         "\t\t\t\t\t\t\t\t\t\t</ns0:value>\n" +
         "\t\t\t\t\t\t\t\t</ns0:NamedString>\n" +
         "\t\t\t\t\t\t\t\t<ns0:NamedString name='KEEP-ALIVE'\n" +
         "\t\t\t\t\t\t\t\t\txmlns:ns0='urn:oasis:names:tc:wsrp:v1:types'>\n" +
         "\t\t\t\t\t\t\t\t\t<ns0:value xmlns:ns0='urn:oasis:names:tc:wsrp:v1:types'>\n" +
         "\t\t\t\t\t\t\t\t\t\t300\n" +
         "\t\t\t\t\t\t\t\t\t</ns0:value>\n" +
         "\t\t\t\t\t\t\t\t</ns0:NamedString>\n" +
         "\t\t\t\t\t\t\t\t<ns0:NamedString name='HOST'\n" +
         "\t\t\t\t\t\t\t\t\txmlns:ns0='urn:oasis:names:tc:wsrp:v1:types'>\n" +
         "\t\t\t\t\t\t\t\t\t<ns0:value xmlns:ns0='urn:oasis:names:tc:wsrp:v1:types'>\n" +
         "\t\t\t\t\t\t\t\t\t\t127.0.0.1:8988\n" +
         "\t\t\t\t\t\t\t\t\t</ns0:value>\n" +
         "\t\t\t\t\t\t\t\t</ns0:NamedString>\n" +
         "\t\t\t\t\t\t\t</ns1:GenericExtension>\n" +
         "\t\t\t\t\t\t</ns0:extensions>\n" +
         "\t\t\t\t\t</ns0:clientData>\n" +
         "\t\t\t\t\t<ns0:markupCharacterSets\n" +
         "\t\t\t\t\t\txmlns:ns0='urn:oasis:names:tc:wsrp:v1:types'>\n" +
         "\t\t\t\t\t\tUTF-8\n" +
         "\t\t\t\t\t</ns0:markupCharacterSets>\n" +
         "\t\t\t\t\t<ns0:validNewModes\n" +
         "\t\t\t\t\t\txmlns:ns0='urn:oasis:names:tc:wsrp:v1:types'>\n" +
         "\t\t\t\t\t\twsrp:view\n" +
         "\t\t\t\t\t</ns0:validNewModes>\n" +
         "\t\t\t\t\t<ns0:validNewModes\n" +
         "\t\t\t\t\t\txmlns:ns0='urn:oasis:names:tc:wsrp:v1:types'>\n" +
         "\t\t\t\t\t\turn:javax:portlet:mode:custo m:edit_defaults\n" +
         "\t\t\t\t\t</ns0:validNewModes>\n" +
         "\t\t\t\t\t<ns0:validNewModes\n" +
         "\t\t\t\t\t\txmlns:ns0='urn:oasis:names:tc:wsrp:v1:types'>\n" +
         "\t\t\t\t\t\twsrp:edit\n" +
         "\t\t\t\t\t</ns0:validNewModes>\n" +
         "\t\t\t\t\t<ns0:validNewModes\n" +
         "\t\t\t\t\t\txmlns:ns0='urn:oasis:names:tc:wsrp:v1:types'>\n" +
         "\t\t\t\t\t\twsrp:help\n" +
         "\t\t\t\t\t</ns0:validNewModes>\n" +
         "\t\t\t\t\t<ns0:validNewModes\n" +
         "\t\t\t\t\t\txmlns:ns0='urn:oasis:names:tc:wsrp:v1:types'>\n" +
         "\t\t\t\t\t\turn:javax:portlet:mode:custo m:about\n" +
         "\t\t\t\t\t</ns0:validNewModes>\n" +
         "\t\t\t\t\t<ns0:validNewModes\n" +
         "\t\t\t\t\t\txmlns:ns0='urn:oasis:names:tc:wsrp:v1:types'>\n" +
         "\t\t\t\t\t\turn:javax:portlet:mode:custo m:print\n" +
         "\t\t\t\t\t</ns0:validNewModes>\n" +
         "\t\t\t\t\t<ns0:validNewModes\n" +
         "\t\t\t\t\t\txmlns:ns0='urn:oasis:names:tc:wsrp:v1:types'>\n" +
         "\t\t\t\t\t\turn:javax:portlet:mode:custo m:config\n" +
         "\t\t\t\t\t</ns0:validNewModes>\n" +
         "\t\t\t\t\t<ns0:validNewWindowStates\n" +
         "\t\t\t\t\t\txmlns:ns0='urn:oasis:names:tc:wsrp:v1:types'>\n" +
         "\t\t\t\t\t\twsrp:normal\n" +
         "\t\t\t\t\t</ns0:validNewWindowStates>\n" +
         "\t\t\t\t\t<ns0:validNewWindowStates\n" +
         "\t\t\t\t\t\txmlns:ns0='urn:oasis:names:tc:wsrp:v1:types'>\n" +
         "\t\t\t\t\t\twsrp:maximized\n" +
         "\t\t\t\t\t</ns0:validNewWindowStates>\n" +
         "\t\t\t\t\t<ns0:validNewWindowStates\n" +
         "\t\t\t\t\t\txmlns:ns0='urn:oasis:names:tc:wsrp:v1:types'>\n" +
         "\t\t\t\t\t\twsrp:minimized\n" +
         "\t\t\t\t\t</ns0:validNewWindowStates>\n" +
         "\t\t\t</ns0:markupParams>\n" +
         "\t\t</ns0:getMarkup>\n" +
         "\t</env:Body>\n" +
         "</env:Envelope>");


      SOAPMessageContext msgContext = MockSOAPMessageContext.createMessageContext(message, getClass().getClassLoader());

      handler.handleMessage(msgContext);

      SOAPBody body = msgContext.getMessage().getSOAPBody();
      String asString = body.toString();
      assertFalse(asString.contains("ns0:extensions"));
      assertFalse(asString.contains("ns1:GenericExtensions"));
      assertFalse(asString.contains("ACCEPT-ENCODING"));
   }
}
