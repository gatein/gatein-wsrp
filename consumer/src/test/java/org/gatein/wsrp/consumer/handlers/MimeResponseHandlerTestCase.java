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

package org.gatein.wsrp.consumer.handlers;

import junit.framework.TestCase;
import org.gatein.pc.api.PortletContext;
import org.gatein.pc.api.PortletInvokerException;
import org.gatein.pc.api.URLFormat;
import org.gatein.pc.api.invocation.PortletInvocation;
import org.gatein.pc.api.invocation.response.ContentResponse;
import org.gatein.pc.api.invocation.response.PortletInvocationResponse;
import org.gatein.wsrp.WSRPResourceURL;
import org.gatein.wsrp.WSRPRewritingConstants;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.test.ExtendedAssert;
import org.gatein.wsrp.test.support.MockWSRPConsumer;
import org.gatein.wsrp.test.support.TestPortletInvocationContext;
import org.gatein.wsrp.test.support.TestRenderInvocation;
import org.gatein.wsrp.test.support.TestResourceInvocation;
import org.gatein.wsrp.test.support.TestWindowContext;
import org.oasis.wsrp.v2.MarkupContext;
import org.oasis.wsrp.v2.MimeResponse;
import org.oasis.wsrp.v2.ResourceContext;

import java.io.UnsupportedEncodingException;
import java.net.URL;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 10507 $
 * @since 2.6
 */
public class MimeResponseHandlerTestCase extends TestCase
{
   public static final String NAMESPACE = "NAMESPACE";
   public static final String PORTLETID = "PORTLETID";
   public static final MockWSRPConsumer CONSUMER = new MockWSRPConsumer("foo");
   public static final PortletContext PORTLET_CONTEXT = PortletContext.createPortletContext(PORTLETID, false);
   public static final TestPortletInvocationContext CONTEXT = new TestPortletInvocationContext();
   public static final URLFormat FORMAT = new URLFormat(false, false, true, true);

   @Override
   protected void setUp() throws Exception
   {
      CONSUMER.setUsingWSRP2(true);
   }

   public void testProcessMarkupV1()
   {
      // fake using WSRP 1
      CONSUMER.setUsingWSRP2(false);

      String markup;
      String expected;
      markup = "khlaksdhjflkjhsadljkwsrp_rewrite?wsrp-urlType=blockingAction&wsrp-interactionState=JBPNS_/wsrp_rewrite" +
         "fadsfadswsrp_rewrite?wsrp-urlType=render&wsrp-navigationalState=JBPNS_/wsrp_rewritefajdshfkjdshgfgrept";
      expected = "khlaksdhjflkjhsadljkAction is=JBPNS_ ns=null ws=null m=null" +
         "fadsfadsRender ns=JBPNS_ ws=null m=nullfajdshfkjdshgfgrept";
      processMarkupAndCheck(markup, expected);

      markup = "<form method='post' action='wsrp_rewrite?wsrp-urlType=blockingAction&wsrp" +
         "-interactionState=JBPNS_/wsrp_rewrite' id='wsrp_rewrite_portfolioManager'><table><tr><td>Stock symbol</t" +
         "d><td><input name='symbol'/></td></tr><tr><td><input type='submit' value='Submit'></td></tr></table></form>";
      expected = "<form method='post' action='Action is=JBPNS_ ns=null ws=null m=null' id='" + NAMESPACE
         + "portfolioManager'><table><tr><td>Stock symbol</t" +
         "d><td><input name='symbol'/></td></tr><tr><td><input type='submit' value='Submit'></td></tr></table></form>";
      processMarkupAndCheck(markup, expected);
   }

   public void testProcessMarkupV2()
   {
      String markup;
      String expected;
      markup = "khlaksdhjflkjhsadljkwsrp_rewrite?wsrp-urlType=blockingAction&wsrp-interactionState=JBPNS_/wsrp_rewrite" +
         "fadsfadswsrp_rewrite?wsrp-urlType=render&wsrp-navigationalState=JBPNS_/wsrp_rewritefajdshfkjdshgfgrept";
      expected = "khlaksdhjflkjhsadljkAction is=JBPNS_ ns=null ws=null m=null" +
         "fadsfadsRender ns=JBPNS_ ws=null m=nullfajdshfkjdshgfgrept";
      processMarkupAndCheck(markup, expected);

      markup = "<form method='post' action='wsrp_rewrite?wsrp-urlType=blockingAction&wsrp" +
         "-interactionState=JBPNS_/wsrp_rewrite' id='wsrp_rewrite_portfolioManager'><table><tr><td>Stock symbol</t" +
         "d><td><input name='symbol'/></td></tr><tr><td><input type='submit' value='Submit'></td></tr></table></form>";
      expected = "<form method='post' action='Action is=JBPNS_ ns=null ws=null m=null' id='" + NAMESPACE + "portfolioManager'><table><tr><td>Stock symbol</t" +
         "d><td><input name='symbol'/></td></tr><tr><td><input type='submit' value='Submit'></td></tr></table></form>";
      processMarkupAndCheck(markup, expected);
   }

   public void testURLEscaping() throws Exception
   {
      String markup;
      String expected;

      String resourceID = WSRPResourceURL.encodeResource(null, new URL("http://localhost:8080/test-resource-portlet/gif/logo.gif"), false);

      //test with &amp;
      markup = "<img src='wsrp_rewrite?wsrp-urlType=resource&amp;wsrp-url=http%3A%2F%2Flocalhost%3A8080%2Ftest-resource-portlet%2Fgif%2Flogo.gif&amp;wsrp-requiresRewrite=true/wsrp_rewrite'/>";
      expected = "<img src='http://test/mock:type=resource?mock:ComponentID=foobar&amp;mock:resourceID=" + resourceID + "'/>";
      processMarkupAndCheck(markup, expected);

      //test with &
      markup = "<img src='wsrp_rewrite?wsrp-urlType=resource&wsrp-url=http%3A%2F%2Flocalhost%3A8080%2Ftest-resource-portlet%2Fgif%2Flogo.gif&wsrp-requiresRewrite=true/wsrp_rewrite'/>";
      expected = "<img src='http://test/mock:type=resource?mock:ComponentID=foobar&mock:resourceID=" + resourceID + "'/>";
      processMarkupAndCheck(markup, expected);

      //test with /x26
      markup = "<img src='wsrp_rewrite?wsrp-urlType=resource\\x26wsrp-url=http%3A%2F%2Flocalhost%3A8080%2Ftest-resource-portlet%2Fgif%2Flogo.gif\\x26wsrp-requiresRewrite=true/wsrp_rewrite'/>";
      expected = "<img src='http://test/mock:type=resource?mock:ComponentID=foobar\\x26mock:resourceID=" + resourceID + "'/>";
      processMarkupAndCheck(markup, expected);
   }

   /*public void testResourceURLs()
   {
      String markup;
      String expected;
      markup = "<img src='wsrp_rewrite?wsrp-urlType=resource&amp;wsrp-url=http%3A%2F%2Flocalhost%3A8080%2Ftest-resource-portlet%2Fgif%2Flogo.gif&amp;wsrp-requiresRewrite=true/wsrp_rewrite'/>";
      expected = "<img src='http://localhost:8080/test-resource-portlet/gif/logo.gif'/>";
      processMarkupAndCheck(markup, expected);

      markup = "<img src='http://localhost:8080/test-resourcenoencodeurl-portlet/gif/logo.gif'/>";
      processMarkupAndCheck(markup, markup);

      markup = "wsrp_rewrite?wsrp-urlType=resource&wsrp-url=http%3A%2F%2Flocalhost%3A8080%2Fhelloworld&wsrp-requiresRewrite=true/wsrp_rewrite/helloworld.jar";
      processMarkupAndCheck(markup, "http://localhost:8080/helloworld/helloworld.jar");

      markup = "wsrp_rewrite?wsrp-urlType=resource&wsrp-url=http%3A%2F%2Flocalhost%3A8080%2Fhelloworld&wsrp-requiresRewrite=true/wsrp_rewrite&foo=bar/helloworld.jar";
      processMarkupAndCheck(markup, "http://localhost:8080/helloworld&foo=bar/helloworld.jar");
   }*/

   public void testRegularURLIsNotAffected()
   {
      String markup;
      markup = "<a href=\"/portal/portal/default/Test/EXAMPLE/EXAMPLE?action=1d&windowstate=&mode=" +
         "&ns=_next%3D%2Fdk%2Fskat%2Fportal%2Ffront%2Fportlets%2Fexample%2Findex.jsp" +
         "&is=_action%3D%252Fdk%252Fskat%252Fportal%252Ffront%252Fportlets%252Fexample%252FprocessLink" +
         "%26jbpns_2fdefault_2fTest_2fEXAMPLE_2fEXAMPLEsnpbjname%3DChris\">Press to use default name.</a>";
      processMarkupAndCheck(markup, markup);
   }

   /*public void testProcessMarkupResourceFromTemplate()
   {
      String url = "http%3a%2f%2fwsrp.netunitysoftware.com%2fWSRPTestService%2fWSRPTestService.asmx%3ftimeout%3d30000%2fgetResource%3fportletHandle%3d781F3EE5-22DF-4ef9-9664-F5FC759065DB%26Function%3dResource%26Name%3dNetUnity%26Type%3dGIF";
      String markup = "<table cellpadding=\"2\" cellspacing=\"0\" border=\"0\" width=\"100%\">\n" +
         "\t<tr class=\"portlet-table-header\">\n" +
         "\t\t<td>Symbol</td>\n" +
         "\t\t<td>Name</td>\n" +
         "\t\t<td align=\"right\">Price</td>\n" +
         "\t\t<td></td>\n" +
         "\t\t<td align=\"right\">Change</td>\n" +
         "\t\t<td align=\"right\">% Chg</td>\n" +
         "\t</tr>\n" +
         "</table>\n" +
         "<A HREF=\"http://www.netunitysoftware.com\" TITLE=\"NetUnity WSRP .NET Framework\" >" +
         "<img src=\"" + getResourceURL(url, false) + "\" border=\"0\" /></A>";

      String expected = "<table cellpadding=\"2\" cellspacing=\"0\" border=\"0\" width=\"100%\">\n" +
         "\t<tr class=\"portlet-table-header\">\n" +
         "\t\t<td>Symbol</td>\n" +
         "\t\t<td>Name</td>\n" +
         "\t\t<td align=\"right\">Price</td>\n" +
         "\t\t<td></td>\n" +
         "\t\t<td align=\"right\">Change</td>\n" +
         "\t\t<td align=\"right\">% Chg</td>\n" +
         "\t</tr>\n" +
         "</table>\n" +
         "<A HREF=\"http://www.netunitysoftware.com\" TITLE=\"NetUnity WSRP .NET Framework\" >" +
         "<img src=\"" + URLTools.decodeXWWWFormURL(url) + "\" border=\"0\" /></A>";
      processMarkupAndCheck(markup, expected);
   }*/

   public void testRewritingRequiredWithBinaryContent() throws UnsupportedEncodingException, PortletInvokerException
   {
      String expected = "/* Style Sheet */\n" +
         "." + TestWindowContext.NAMESPACE + "ExternalStyleClass\n" +
         "{\n" +
         "\tfont-weight: bold;\n" +
         "\tcolor: green;\n" +
         "\tfont-family: Arial;\n" +
         "\tborder:dashed 1px black; \n" +
         "\tpadding: 15px;\n" +
         "}";
      byte[] expectedBinary = expected.getBytes("UTF-8");
      String original = "/* Style Sheet */\n" +
         ".wsrp_rewrite_ExternalStyleClass\n" +
         "{\n" +
         "\tfont-weight: bold;\n" +
         "\tcolor: green;\n" +
         "\tfont-family: Arial;\n" +
         "\tborder:dashed 1px black; \n" +
         "\tpadding: 15px;\n" +
         "}";
      byte[] originalBinary = original.getBytes("UTF-8");

      processBinaryAndCheck(expectedBinary, new RenderHandler(CONSUMER), new TestRenderInvocation(CONTEXT), WSRPTypeFactory.createMimeResponse("text/css", null, originalBinary, MarkupContext.class));
      processBinaryAndCheck(expectedBinary, new ResourceHandler(CONSUMER), new TestResourceInvocation(CONTEXT), WSRPTypeFactory.createMimeResponse("text/css", null, originalBinary, ResourceContext.class));

      original = "//JScript file\n" +
         "function wsrp_rewrite_ExternalScriptFunction()\n" +
         "{\n" +
         "\talert('Script Function in Script File');\n" +
         "}";
      originalBinary = original.getBytes("UTF-8");
      expected = "//JScript file\n" +
         "function " + TestWindowContext.NAMESPACE + "ExternalScriptFunction()\n" +
         "{\n" +
         "\talert('Script Function in Script File');\n" +
         "}";
      expectedBinary = expected.getBytes("UTF-8");

      processBinaryAndCheck(expectedBinary, new RenderHandler(CONSUMER), new TestRenderInvocation(CONTEXT), WSRPTypeFactory.createMimeResponse("application/x-javascript", null, originalBinary, MarkupContext.class));
      processBinaryAndCheck(expectedBinary, new ResourceHandler(CONSUMER), new TestResourceInvocation(CONTEXT), WSRPTypeFactory.createMimeResponse("application/x-javascript", null, originalBinary, ResourceContext.class));
   }

   private void processBinaryAndCheck(byte[] expectedBinary, final MimeResponseHandler handler, final PortletInvocation invocation, final MimeResponse mimeResponse) throws PortletInvokerException
   {
      mimeResponse.setRequiresRewriting(true);
      PortletInvocationResponse response = handler.rewriteResponseIfNeeded(mimeResponse, invocation);
      assertTrue(response instanceof ContentResponse);
      ContentResponse contentResponse = (ContentResponse)response;
      ExtendedAssert.assertEquals(expectedBinary, contentResponse.getBytes());
   }

   private void processMarkupAndCheck(String markup, String expected)
   {
      String result = MimeResponseHandler.processMarkup(
         markup,
         NAMESPACE,
         CONTEXT,
         PORTLET_CONTEXT,
         FORMAT,
         CONSUMER
      );
      assertEquals(expected, result);
   }

   private String getResourceURL(String encodedURL, boolean requiresRewrite)
   {
      String result = WSRPRewritingConstants.FAKE_RESOURCE_URL.replace(WSRPRewritingConstants.WSRP_URL, encodedURL);
      result = result.replace(WSRPRewritingConstants.WSRP_REQUIRES_REWRITE, Boolean.toString(requiresRewrite));
      return result;
   }
}
