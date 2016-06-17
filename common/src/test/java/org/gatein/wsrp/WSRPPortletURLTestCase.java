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

package org.gatein.wsrp;

import junit.framework.TestCase;
import org.gatein.pc.api.ContainerURL;
import org.gatein.pc.api.Mode;
import org.gatein.pc.api.ParametersStateString;
import org.gatein.pc.api.ResourceURL;
import org.gatein.pc.api.StateString;
import org.gatein.pc.api.WindowState;
import org.gatein.pc.api.cache.CacheLevel;
import org.gatein.wsrp.spec.v2.WSRP2RewritingConstants;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 13470 $
 * @since 2.4 (Apr 28, 2006)
 */
public class WSRPPortletURLTestCase extends TestCase
{
   @Override
   protected void setUp() throws Exception
   {
      super.setUp();
      WSRPPortletURL.setStrict(true);
   }

   public void testResourceURL()
   {
      String expected = "wsrp_rewrite?wsrp-urlType=resource&amp;wsrp-url=http%3A%2F%2Ftest.com%2Fimages%2Ftest.gif" +
         "&amp;wsrp-requiresRewrite=false/wsrp_rewrite";
      WSRPPortletURL url = WSRPPortletURL.create(expected);

      assertTrue(url instanceof WSRPResourceURL);
      WSRPResourceURL resource = (WSRPResourceURL)url;
      assertFalse(resource.requiresRewrite());
      assertEquals("http://test.com/images/test.gif", resource.getResourceURL().toExternalForm());

      //resource.getResourceId will not return the actual resource id, but an encoded resource map used to determine how to access the resource
      Map<String, String> resourceMap = WSRPResourceURL.decodeResource(resource.getResourceId());
      String resourceURL = resourceMap.get(WSRPRewritingConstants.RESOURCE_URL);
      assertEquals("http://test.com/images/test.gif", resourceURL);

      assertNull(resourceMap.get(WSRP2RewritingConstants.RESOURCE_ID));
      assertEquals("false", resourceMap.get(WSRP2RewritingConstants.RESOURCE_PREFER_OPERATION));
   }

   public void testResourceURLWithDoubleEncoding()
   {
      String expected = "wsrp_rewrite?wsrp-urlType=blockingAction&wsrp-mode=wsrp:view&wsrp-windowState=wsrp:normal&wsrp-interactionState=JBPNS_rO0ABX\\/wsrp_rewrite";
      WSRPPortletURL url = WSRPPortletURL.create(expected);

      assertTrue(url instanceof WSRPActionURL);
      WSRPActionURL resource = (WSRPActionURL)url;
      assertEquals("JBPNS_rO0ABX", resource.getInteractionState().getStringValue());
   }

   public void testResourceID()
   {
      String expected = "wsrp_rewrite?wsrp-urlType=resource&amp;wsrp-resourceID=resource_123" +
         "&amp;wsrp-requiresRewrite=false/wsrp_rewrite";
      WSRPPortletURL url = WSRPPortletURL.create(expected);

      assertTrue(url instanceof WSRPResourceURL);
      WSRPResourceURL resource = (WSRPResourceURL)url;
      assertFalse(resource.requiresRewrite());
      assertNull(resource.getResourceURL());

      //resource.getResourceId will not return the actual resource id, but an encoded resource map used to determine how to access the resource
      Map<String, String> resourceMap = WSRPResourceURL.decodeResource(resource.getResourceId());
      String resourceID = resourceMap.get(WSRP2RewritingConstants.RESOURCE_ID);
      assertEquals("resource_123", resourceID);

      assertNull(resourceMap.get(WSRPRewritingConstants.RESOURCE_URL));
      assertEquals("false", resourceMap.get(WSRP2RewritingConstants.RESOURCE_PREFER_OPERATION));
   }

   public void testResourceIDToken()
   {
      //Test what happens in the case where the url is created via token wsrp rewriting on the producer side
      //In this case the resource ID will not be an encoded resource map.

      Map<String, String> resourceMap = WSRPResourceURL.decodeResource("resource_123");
      String resourceID = resourceMap.get(WSRP2RewritingConstants.RESOURCE_ID);
      assertEquals("resource_123", resourceID);

      assertNull(resourceMap.get(WSRPRewritingConstants.RESOURCE_URL));
      assertNull(resourceMap.get(WSRP2RewritingConstants.RESOURCE_PREFER_OPERATION));
   }

   public void testResources()
   {
      String expected = "wsrp_rewrite?wsrp-urlType=resource&amp;wsrp-resourceID=resource_123" +
         "&amp;wsrp-url=http%3A%2F%2Ftest.com%2Fimages%2Ftest.gif&amp;wsrp-requiresRewrite=false/wsrp_rewrite";
      WSRPPortletURL url = WSRPPortletURL.create(expected);

      assertTrue(url instanceof WSRPResourceURL);
      WSRPResourceURL resource = (WSRPResourceURL)url;
      assertFalse(resource.requiresRewrite());
      assertEquals("http://test.com/images/test.gif", resource.getResourceURL().toExternalForm());

      //resource.getResourceId will not return the actual resource id, but an encoded resource map used to determine how to access the resource
      Map<String, String> resourceMap = WSRPResourceURL.decodeResource(resource.getResourceId());
      String resourceID = resourceMap.get(WSRP2RewritingConstants.RESOURCE_ID);
      assertEquals("resource_123", resourceID);

      String resourceURL = resourceMap.get(WSRPRewritingConstants.RESOURCE_URL);
      assertEquals("http://test.com/images/test.gif", resourceURL);
      assertEquals("false", resourceMap.get(WSRP2RewritingConstants.RESOURCE_PREFER_OPERATION));
   }

   public void testResourcesNoRequiresRewrite()
   {
      String expected = "wsrp_rewrite?wsrp-urlType=resource&amp;wsrp-resourceID=resource_123" +
         "&amp;wsrp-url=http%3A%2F%2Ftest.com%2Fimages%2Ftest.gif&amp;/wsrp_rewrite";
      WSRPPortletURL url = WSRPPortletURL.create(expected);

      assertTrue(url instanceof WSRPResourceURL);
      WSRPResourceURL resource = (WSRPResourceURL)url;
      assertFalse(resource.requiresRewrite());
      assertEquals("http://test.com/images/test.gif", resource.getResourceURL().toExternalForm());

      //resource.getResourceId will not return the actual resource id, but an encoded resource map used to determine how to access the resource
      Map<String, String> resourceMap = WSRPResourceURL.decodeResource(resource.getResourceId());
      String resourceID = resourceMap.get(WSRP2RewritingConstants.RESOURCE_ID);
      assertEquals("resource_123", resourceID);

      String resourceURL = resourceMap.get(WSRPRewritingConstants.RESOURCE_URL);
      assertEquals("http://test.com/images/test.gif", resourceURL);
      assertEquals("false", resourceMap.get(WSRP2RewritingConstants.RESOURCE_PREFER_OPERATION));
   }

   public void testResourcesNoResourceURL()
   {
      String expected = "wsrp_rewrite?wsrp-urlType=resource&amp;wsrp-resourceID=resource_123" +
         "&amp;wsrp-requiresRewrite=false/wsrp_rewrite";
      WSRPPortletURL url = WSRPPortletURL.create(expected);

      assertTrue(url instanceof WSRPResourceURL);
      WSRPResourceURL resource = (WSRPResourceURL)url;
      assertFalse(resource.requiresRewrite());

      //resource.getResourceId will not return the actual resource id, but an encoded resource map used to determine how to access the resource
      Map<String, String> resourceMap = WSRPResourceURL.decodeResource(resource.getResourceId());
      String resourceID = resourceMap.get(WSRP2RewritingConstants.RESOURCE_ID);
      assertEquals("resource_123", resourceID);

      assertEquals("false", resourceMap.get(WSRP2RewritingConstants.RESOURCE_PREFER_OPERATION));
   }

   public void testPreferOperation()
   {
      String expected = "wsrp_rewrite?wsrp-urlType=resource&amp;wsrp-resourceID=resource_123" +
         "&amp;wsrp-url=http%3A%2F%2Ftest.com%2Fimages%2Ftest.gif&amp;wsrp-preferOperation=true&amp;wsrp-requiresRewrite=false/wsrp_rewrite";
      WSRPPortletURL url = WSRPPortletURL.create(expected);

      assertTrue(url instanceof WSRPResourceURL);
      WSRPResourceURL resource = (WSRPResourceURL)url;
      assertFalse(resource.requiresRewrite());
      assertEquals("http://test.com/images/test.gif", resource.getResourceURL().toExternalForm());

      //resource.getResourceId will not return the actual resource id, but an encoded resource map used to determine how to access the resource
      Map<String, String> resourceMap = WSRPResourceURL.decodeResource(resource.getResourceId());
      String resourceID = resourceMap.get(WSRP2RewritingConstants.RESOURCE_ID);
      assertEquals("resource_123", resourceID);

      String resourceURL = resourceMap.get(WSRPRewritingConstants.RESOURCE_URL);
      assertEquals("http://test.com/images/test.gif", resourceURL);

      String preferOperation = resourceMap.get(WSRP2RewritingConstants.RESOURCE_PREFER_OPERATION);
      assertEquals("true", preferOperation);
   }

   public void testShouldSetPreferOperationToTrueWhenResourceIdIsPresent()
   {
      ResourceURL resourceURL = new TestResourceURL(TestResourceURL.RESID, TestResourceURL.DEFAULT_NS, CacheLevel.FULL);
      WSRPPortletURL url = WSRPPortletURL.create(resourceURL, false, WSRPPortletURL.URLContext.EMPTY);

      String actual = url.toString();
      assertTrue(actual.contains("wsrp-urlType=resource"));
      assertTrue(actual.contains("wsrp-resourceID=" + TestResourceURL.RESID));
      assertTrue(actual.contains("wsrp-preferOperation=true"));
   }

   public void testShouldProperlyTransmitResourceState()
   {
      // create URL from container
      ResourceURL resourceURL = new TestResourceURL("resparam", "resvalue");
      WSRPPortletURL url = WSRPPortletURL.create(resourceURL, false, WSRPPortletURL.URLContext.EMPTY);

      assertTrue(url instanceof WSRPResourceURL);
      WSRPResourceURL resource = (WSRPResourceURL)url;
      StateString resourceState = resource.getResourceState();
      assertNotNull(resourceState);

      // serialize URL to WSRP string format
      String resourceAsString = resource.toString();

      // and re-create it
      url = WSRPPortletURL.create(resourceAsString);

      // check that we have indeed the same state
      assertTrue(url instanceof WSRPResourceURL);
      resource = (WSRPResourceURL)url;
      resourceState = resource.getResourceState();
      assertNotNull(resourceState);
      Map<String, String[]> state = StateString.decodeOpaqueValue(resourceState.getStringValue());
      assertEquals("resvalue", state.get("resparam")[0]);
   }

   /** Declare a secure interaction back to the Portlet */
   public void testSecureInteraction()
   {
      String expected = "wsrp_rewrite?wsrp-urlType=blockingAction&amp;wsrp-secureURL=true" +
         "&amp;wsrp-navigationalState=a8h4K5JD9&amp;wsrp-interactionState=fg4h923mdk/wsrp_rewrite";
      WSRPPortletURL url = WSRPPortletURL.create(expected);

      assertTrue(url instanceof WSRPActionURL);
      WSRPActionURL actionURL = (WSRPActionURL)url;
      assertTrue(url.isSecure());
      assertEquals("a8h4K5JD9", actionURL.getNavigationalState().getStringValue());
      assertEquals("fg4h923mdk", actionURL.getInteractionState().getStringValue());
   }

   /** Request the Consumer render the Portlet in a different mode and window state */
   public void testDifferentModeAndWindowState()
   {
      String expected = "wsrp_rewrite?wsrp-urlType=render&amp;wsrp-mode=wsrp:help&amp;wsrp-windowState=wsrp:maximized/wsrp_rewrite";
      WSRPPortletURL url = WSRPPortletURL.create(expected);

      assertTrue(url instanceof WSRPRenderURL);
      assertEquals(Mode.HELP, url.getMode());
      assertEquals(WindowState.MAXIMIZED, url.getWindowState());
   }

   public void testMinimal()
   {
      String minimalURLType = "wsrp_rewrite?wsrp-urlType=render/wsrp_rewrite";
      WSRPPortletURL url = WSRPPortletURL.create(minimalURLType);

      assertTrue(url instanceof WSRPRenderURL);
   }

   public void testInvalidParameterName()
   {
      String message = "Should have detected invalid parameter: ";

      String invalid = "wsrp_rewrite?wsrp-urlType=render&amp;foo=bar/wsrp_rewrite";
      checkInvalidURL(invalid, message, "foo");
   }

//   Disable this test for now as we handle the doubly encoded ampersand.
//   public void testDoublyEncodedAmpersand()
//   {
//      String expected = "wsrp_rewrite?wsrp-urlType=render&amp;amp;wsrp-mode=wsrp:help&amp;amp;wsrp-windowState=wsrp:maximized/wsrp_rewrite";
//      try
//      {
//         WSRPPortletURL.create(expected);
//         fail("Should have thrown an exception on doubly encoded &!");
//      }
//      catch (Exception e)
//      {
//         // expected
//      }
//   }

   /** Relax validation and test that we now accept normally invalid URLs. */
   public void testExtraParametersRelaxedValidation()
   {
      WSRPPortletURL.setStrict(false);

      String validInRelaxedMode = "wsrp_rewrite?wsrp-urlType=render&amp;foo=bar/wsrp_rewrite";

      WSRPPortletURL url = WSRPPortletURL.create(validInRelaxedMode);
      assertTrue(url instanceof WSRPRenderURL);
      assertTrue(url.toString().contains("foo=bar"));

      validInRelaxedMode = "wsrp_rewrite?wsrp-urlType=render/wsrp_rewrite&amp;foo=bar";
      url = WSRPPortletURL.create(validInRelaxedMode);
      assertTrue(url instanceof WSRPRenderURL);
      assertTrue(url.toString().endsWith("foo=bar"));

      String stillInvalid = "wsrp_rewrite?wsrp-urlType=render&amp;foo=bar";
      checkInvalidURL(stillInvalid, "Should have detected missing end token", WSRPRewritingConstants.END_WSRP_REWRITE);
   }

   /*public void testExtraRelaxedValidation()
   {
      String valid = "wsrp_rewrite?wsrp-urlType=resource&wsrp-url=http%3A%2F%2Flocalhost%3A8080%2Fhelloworld&wsrp-requiresRewrite=true/wsrp_rewrite/helloworld.jar";
      WSRPPortletURL url = WSRPPortletURL.create(valid);
      assertEquals("http://localhost:8080/helloworld/helloworld.jar", url.toString());

      String invalid = "wsrp_rewrite?wsrp-urlType=resource&wsrp-url=http%3A%2F%2Flocalhost%3A8080%2Fhelloworld&wsrp-requiresRewrite=true/wsrp_rewrite&amp;foo=bar/helloworld.jar";
      checkInvalidURL(invalid, "Should have detected improper position of end token", WSRPRewritingConstants.END_WSRP_REWRITE);

      WSRPPortletURL.setStrict(false);
      String validInRelaxedMode = "wsrp_rewrite?wsrp-urlType=resource&wsrp-url=http%3A%2F%2Flocalhost%3A8080%2Fhelloworld&wsrp-requiresRewrite=true/wsrp_rewrite&amp;foo=bar/helloworld.jar";
      url = WSRPPortletURL.create(validInRelaxedMode);
      assertEquals("http://localhost:8080/helloworld&foo=bar/helloworld.jar", url.toString());
   }*/

   public void testExtraParameters()
   {
      String validInRelaxedMode = "wsrp_rewrite?wsrp-urlType=render&amp;foo=bar/wsrp_rewrite";
      checkInvalidURL(validInRelaxedMode, "Should have detected invalid parameter: ", "foo");

      validInRelaxedMode = "wsrp_rewrite?wsrp-urlType=render/wsrp_rewrite&amp;foo=bar";
      checkInvalidURL(validInRelaxedMode, "Should have detected URL doesn't end with end token", WSRPRewritingConstants.END_WSRP_REWRITE);
   }

   public void testInvalidMode()
   {
      String message = "Should have detected invalid mode: ";

      String invalid = "wsrp_rewrite?wsrp-urlType=render&wsrp-mode=foo/wsrp_rewrite";
      checkInvalidURL(invalid, message, "foo");
   }

   public void testCustomModeAndWindowState()
   {
      Set<String> modes = new HashSet<String>();
      modes.add("urn:foo");

      Set<String> windowStates = new HashSet<String>();
      windowStates.add("urn:bar");

      String urlString = "wsrp_rewrite?wsrp-urlType=render&wsrp-mode=urn%3Afoo&wsrp-windowState=urn%3Abar/wsrp_rewrite";
      WSRPPortletURL url = WSRPPortletURL.create(urlString, modes, windowStates);
      assertEquals("urn:foo", url.getMode().toString());
      assertEquals("urn:bar", url.getWindowState().toString());
   }

   public void testEncodedMode()
   {
      String encoded = "wsrp_rewrite?wsrp-urlType=render&wsrp-mode=wsrp%3Aview/wsrp_rewrite";
      WSRPPortletURL url = WSRPPortletURL.create(encoded);
      assertEquals(Mode.VIEW, url.getMode());

      encoded = "wsrp_rewrite?wsrp-urlType=render&wsrp-mode=wsrp%3aedit/wsrp_rewrite";
      url = WSRPPortletURL.create(encoded);
      assertEquals(Mode.EDIT, url.getMode());
   }

   public void testEncodedWindowState()
   {
      String encoded = "wsrp_rewrite?wsrp-urlType=render&wsrp-windowState=wsrp%3Amaximized/wsrp_rewrite";
      WSRPPortletURL url = WSRPPortletURL.create(encoded);
      assertEquals(WindowState.MAXIMIZED, url.getWindowState());

      encoded = "wsrp_rewrite?wsrp-urlType=render&wsrp-windowState=wsrp%3aminimized/wsrp_rewrite";
      url = WSRPPortletURL.create(encoded);
      assertEquals(WindowState.MINIMIZED, url.getWindowState());
   }

   public void testInvalidResourceURLV1()
   {
      String message = "Should have detected missing parameter: ";

      /*
      // We now allow requiresRewrite to be missing and assume false instead... See GTNWSRP-103.
      String invalid = "wsrp_rewrite?wsrp-urlType=resource&amp;wsrp-url=http%3A%2F%2Flocalhost%2F/wsrp_rewrite";
      checkInvalidURL(invalid, message, WSRPRewritingConstants.RESOURCE_REQUIRES_REWRITE);
      */

      String invalid = "wsrp_rewrite?wsrp-urlType=resource&amp;wsrp-requiresRewrite=true/wsrp_rewrite";
      checkInvalidURL(invalid, message, WSRPRewritingConstants.RESOURCE_URL);

      invalid = "wsrp_rewrite?wsrp-urlType=resource&amp;wsrp-url=invalidURL&amp;wsrp-requiresRewrite=true/wsrp_rewrite";
      checkInvalidURL(invalid, "Should have detected invalid URL: ", "invalidURL");
   }

   public void testNullURL()
   {
      try
      {
         WSRPPortletURL.create(null);
         fail("Should have detected null URL");
      }
      catch (IllegalArgumentException e)
      {
         // expected
      }
   }

   public void testInvalidURLType()
   {
      String wrongURLType = "wsrp_rewrite?wsrp-urlType=pipo&amp;wsrp-mode=help/wsrp_rewrite";

      try
      {
         WSRPPortletURL.create(wrongURLType);
         fail("Should have detected wrong URL type");
      }
      catch (IllegalArgumentException e)
      {
         // expected
      }
   }

   public void testProperEndTokenInRelaxedMode()
   {
      WSRPPortletURL.setStrict(false);

      WSRPPortletURL url = new WSRPPortletURL()
      {
         @Override
         protected String getURLType()
         {
            return WSRPRewritingConstants.URL_TYPE_BLOCKING_ACTION;
         }

         @Override
         protected void appendEnd(StringBuffer sb)
         {
         }
      };
      assertTrue(url.toString().contains(WSRPRewritingConstants.END_WSRP_REWRITE));
   }

   private void checkInvalidURL(String invalid, String message, String mustBeInException)
   {
      try
      {
         WSRPPortletURL.create(invalid);
         fail(message + mustBeInException);
      }
      catch (IllegalArgumentException e)
      {
         assertTrue(e.getLocalizedMessage().contains(mustBeInException));
      }
   }

   private static class TestContainerURL implements ContainerURL
   {
      private Mode mode;
      private WindowState ws;
      private StateString ns;
      private Map<String, String> props;

      static final StateString DEFAULT_NS;
      static final Map<String, String[]> DEFAULT_PARAMS = new HashMap<String, String[]>(3);
      static final String PARAM = "param";
      static final String VALUE = "value";

      static
      {
         DEFAULT_PARAMS.put(PARAM, new String[]{VALUE});
         DEFAULT_NS = ParametersStateString.create(DEFAULT_PARAMS);
      }

      private TestContainerURL(Mode mode, WindowState ws, StateString ns, Map<String, String> props)
      {
         this.mode = mode;
         this.ws = ws;
         this.ns = ns;
         this.props = props;
      }

      public Mode getMode()
      {
         return mode;
      }

      public WindowState getWindowState()
      {
         return ws;
      }

      public StateString getNavigationalState()
      {
         return ns;
      }

      public Map<String, String> getProperties()
      {
         return props;
      }
   }

   private static class TestResourceURL extends TestContainerURL implements ResourceURL
   {
      private static final String RESID = "resid";
      private String id;
      private StateString state;
      private CacheLevel cache;

      private TestResourceURL(String id, StateString state, CacheLevel cache)
      {
         super(Mode.VIEW, WindowState.NORMAL, DEFAULT_NS, null);
         init(id, state, cache);
      }

      private void init(String id, StateString state, CacheLevel cache)
      {
         this.id = id;
         this.state = state;
         this.cache = cache;
      }

      private TestResourceURL(String param, String value)
      {
         super(Mode.VIEW, WindowState.NORMAL, DEFAULT_NS, null);

         HashMap<String, String[]> params = new HashMap<String, String[]>(3);
         params.put(param, new String[]{value});

         init(RESID, ParametersStateString.create(params), CacheLevel.FULL);
      }

      public String getResourceId()
      {
         return id;
      }

      public StateString getResourceState()
      {
         return state;
      }

      public CacheLevel getCacheability()
      {
         return cache;
      }
   }
}