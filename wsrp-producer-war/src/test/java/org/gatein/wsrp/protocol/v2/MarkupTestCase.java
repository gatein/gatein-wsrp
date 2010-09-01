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
package org.gatein.wsrp.protocol.v2;

import org.gatein.common.util.ParameterValidation;
import org.gatein.pc.api.StateString;
import org.gatein.wsrp.WSRPActionURL;
import org.gatein.wsrp.WSRPConstants;
import org.gatein.wsrp.WSRPPortletURL;
import org.gatein.wsrp.WSRPRenderURL;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.producer.WSRPProducerBaseTest;
import org.gatein.wsrp.servlet.ServletAccess;
import org.gatein.wsrp.test.ExtendedAssert;
import org.gatein.wsrp.test.support.MockHttpServletRequest;
import org.gatein.wsrp.test.support.MockHttpServletResponse;
import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.oasis.wsrp.v2.BlockingInteractionResponse;
import org.oasis.wsrp.v2.CacheControl;
import org.oasis.wsrp.v2.GetMarkup;
import org.oasis.wsrp.v2.InitCookie;
import org.oasis.wsrp.v2.InteractionParams;
import org.oasis.wsrp.v2.InvalidRegistration;
import org.oasis.wsrp.v2.MarkupContext;
import org.oasis.wsrp.v2.MarkupResponse;
import org.oasis.wsrp.v2.NamedString;
import org.oasis.wsrp.v2.NavigationalContext;
import org.oasis.wsrp.v2.OperationFailed;
import org.oasis.wsrp.v2.PerformBlockingInteraction;
import org.oasis.wsrp.v2.PortletContext;
import org.oasis.wsrp.v2.RuntimeContext;
import org.oasis.wsrp.v2.SessionContext;
import org.oasis.wsrp.v2.SessionParams;
import org.oasis.wsrp.v2.StateChange;
import org.oasis.wsrp.v2.UnsupportedMode;
import org.oasis.wsrp.v2.UpdateResponse;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Locale;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
@RunWith(Arquillian.class)
public class MarkupTestCase extends org.gatein.wsrp.protocol.v2.NeedPortletHandleTest
{
   private static final String DEFAULT_VIEW_MARKUP = "<p>symbol unset stock value: value unset</p>";
   private static final String DEFAULT_MARKUP_PORTLET_WAR = "test-markup-portlet.war";

   public MarkupTestCase()
      throws Exception
   {
      super("MarkupTestCase", DEFAULT_MARKUP_PORTLET_WAR);
   }

   @Deployment
   public static JavaArchive createDeployment()
   {
      JavaArchive jar = ShrinkWrap.create("test.jar", JavaArchive.class);
      jar.addClass(NeedPortletHandleTest.class);
      jar.addClass(V2ProducerBaseTest.class);
      jar.addClass(WSRPProducerBaseTest.class);
      return jar;
   }

   @Before
   public void setUp() throws Exception
   {
      if (System.getProperty("test.deployables.dir") != null)
      {
         super.setUp();
         //hack to get around having to have a httpservletrequest when accessing the producer services
         //I don't know why its really needed, seems to be a dependency where wsrp connects with the pc module
         ServletAccess.setRequestAndResponse(MockHttpServletRequest.createMockRequest(null), MockHttpServletResponse
            .createMockResponse());
      }
   }

   @After
   public void tearDown() throws Exception
   {
      if (System.getProperty("test.deployables.dir") != null)
      {
         super.tearDown();
      }
   }

   @Test
   public void testGetMarkupViewNoSession() throws Exception
   {
      GetMarkup getMarkup = createMarkupRequest();

      MarkupResponse response = producer.getMarkup(getMarkup);

      checkMarkupResponse(response, DEFAULT_VIEW_MARKUP);
   }

   @Test
   public void testInvalidGetMarkup() throws Exception
   {
      GetMarkup getMarkup = createMarkupRequest();
      getMarkup.getMarkupParams().setMode("invalid mode");

      try
      {
         producer.getMarkup(getMarkup);
         ExtendedAssert.fail();
      }
      catch (UnsupportedMode unsupportedMode)
      {
         // expected
      }
   }

   @Test
   public void testGetMarkupWithSessionID() throws Exception
   {
      // The consumer should never have access to or be able to set a sessionID. Sessions are handled by the Producer using cookies.
      GetMarkup getMarkup = createMarkupRequest();
      SessionParams sessionParams = new SessionParams();
      sessionParams.setSessionID("Hello World");
      getMarkup.getRuntimeContext().setSessionParams(sessionParams);

      try
      {
         producer.getMarkup(getMarkup);
         ExtendedAssert.fail("A sessionID should not be allowed to be passed in GetMarkup()");
      }
      catch (OperationFailed operationFailed)
      {
         // expected
      }
   }

   @Test
   public void testGetMarkupEditNoSession() throws Exception
   {
      GetMarkup getMarkup = createMarkupRequest();
      getMarkup.getMarkupParams().setMode(WSRPConstants.EDIT_MODE);

      MarkupResponse response = producer.getMarkup(getMarkup);

      checkMarkupResponse(response, "<form method='post' action='wsrp_rewrite?wsrp-urlType=blockingAction&wsrp" +
         "-interactionState=JBPNS_/wsrp_rewrite' id='wsrp_rewrite_portfolioManager'><table><tr><td>Stock symbol</t" +
         "d><td><input name='symbol'/></td></tr><tr><td><input type='submit' value='Submit'></td></tr></table></form>");
   }

   @Test
   public void testGetMarkupRenderParameters() throws Exception
   {
      undeploy(DEFAULT_MARKUP_PORTLET_WAR);
      String archiveName = "test-renderparam-portlet.war";
      deploy(archiveName);

      try
      {
         GetMarkup gm = createMarkupRequestForCurrentlyDeployedPortlet();
         MarkupResponse res = producer.getMarkup(gm);

         String markupString = res.getMarkupContext().getItemString();

         String julienLink = extractLink(markupString, 0);
         WSRPPortletURL julienURL = WSRPPortletURL.create(julienLink);

         ExtendedAssert.assertString1ContainsString2(markupString, "Hello, Anonymous!");
         ExtendedAssert.assertString1ContainsString2(markupString, "Counter: 0");

         ExtendedAssert.assertTrue(julienURL instanceof WSRPRenderURL);
         WSRPRenderURL julienRender = (WSRPRenderURL)julienURL;

         // We're now trying to get a hello for Julien ;)
         NavigationalContext navigationalContext = new NavigationalContext();
         navigationalContext.setOpaqueValue(julienRender.getNavigationalState().getStringValue());
         gm.getMarkupParams().setNavigationalContext(navigationalContext);
         res = producer.getMarkup(gm);
         markupString = res.getMarkupContext().getItemString();
         ExtendedAssert.assertString1ContainsString2(markupString, "Hello, Julien!");

         // julien.length() * 2 to bypass second link
         WSRPPortletURL incrementURL = WSRPPortletURL.create(extractLink(markupString, julienLink.length() * 2));
         ExtendedAssert.assertTrue(incrementURL instanceof WSRPActionURL);
         WSRPActionURL incrementAction = (WSRPActionURL)incrementURL;

         // let's see now if we can increment the counter
         PerformBlockingInteraction performBlockingInteraction =
            WSRPTypeFactory.createDefaultPerformBlockingInteraction(getHandleForCurrentlyDeployedArchive());
         InteractionParams interactionParams = performBlockingInteraction.getInteractionParams();
         interactionParams.setInteractionState(incrementAction.getInteractionState().getStringValue());
         producer.performBlockingInteraction(performBlockingInteraction);
         res = producer.getMarkup(gm);
         markupString = res.getMarkupContext().getItemString();
         ExtendedAssert.assertString1ContainsString2(markupString, "Counter: 1");
      }
      finally
      {
         undeploy(archiveName);
      }
   }

   private String extractLink(String markupString, int fromIndex)
   {
      int urlStartIndex = markupString.indexOf("='", fromIndex);
      int urlEndIndex = markupString.indexOf("'>", urlStartIndex);
      return markupString.substring(urlStartIndex + 2, urlEndIndex);
   }

   // fix-me: add more tests
   @Test
   public void testGetMarkupSession() throws Exception
   {
      undeploy(DEFAULT_MARKUP_PORTLET_WAR);
      // deploy session-manipulating portlet
      String sessionPortletArchive = "test-session-portlet.war";
      deploy(sessionPortletArchive);


      try
      {
         GetMarkup getMarkup = createMarkupRequestForCurrentlyDeployedPortlet();

         MarkupResponse response = producer.getMarkup(getMarkup);

         checkMarkupResponseWithSession(response, 0);

         response = producer.getMarkup(getMarkup);
         checkMarkupResponseWithSession(response, 1);

         // fix-me: try to reuse the old session id: what should happen?
         //         runtimeContext.setSessionID(sessionID);
         //         getMarkup.setRuntimeContext(runtimeContext);
         //         try
         //         {
         //            producer.getMarkup(getMarkup);
         //            fail("The session should be invalid...");
         //         }
         //         catch (InvalidSessionFault expected)
         //         {
         //         }
      }
      finally
      {
         // reset state
         undeploy(sessionPortletArchive);
      }
   }

   @Test
   public void testPerformBlockingInteractionNoRedirect() throws Exception
   {
      checkPBIAndGetNavigationalState("RHAT");
   }

   @Test
   public void testPerformBlockingInteractionRedirect() throws Exception
   {
      PerformBlockingInteraction performBlockingInteraction =
         WSRPTypeFactory.createDefaultPerformBlockingInteraction(getDefaultHandle());
      InteractionParams interactionParams = performBlockingInteraction.getInteractionParams();

      // crappy way but this is a test! ;)
      NamedString namedString = new NamedString();
      namedString.setName("symbol");
      namedString.setValue("HELP");
      interactionParams.getFormParameters().add(namedString);

      BlockingInteractionResponse response = producer.performBlockingInteraction(performBlockingInteraction);
      ExtendedAssert.assertNotNull(response);

      // this is a redirect...
      String redirectURL = response.getRedirectURL();
      ExtendedAssert.assertNotNull(redirectURL);
      ExtendedAssert.assertEquals("/WEB-INF/jsp/help.jsp", redirectURL); // fix-me: handle URL re-writing

      // no update response
      UpdateResponse updateResponse = response.getUpdateResponse();
      ExtendedAssert.assertNull(updateResponse);
   }

   @Test
   public void testGMAndPBIInteraction() throws Exception
   {
      testGetMarkupViewNoSession();
      String symbol = "AAPL";
      String navigationalState = checkPBIAndGetNavigationalState(symbol);

      GetMarkup getMarkup = createMarkupRequest();
      NavigationalContext navigationalContext = WSRPTypeFactory.createNavigationalContextOrNull(StateString.create(navigationalState), null);
      getMarkup.getMarkupParams().setNavigationalContext(navigationalContext);
      MarkupResponse response = producer.getMarkup(getMarkup);
      checkMarkupResponse(response, "<p>" + symbol + " stock value: 123.45</p>");
   }

   @Test
   public void testPBIWithSessionID() throws Exception
   {
      String portletHandle = getDefaultHandle();
      PerformBlockingInteraction performBlockingInteraction = WSRPTypeFactory.createDefaultPerformBlockingInteraction(portletHandle);

      RuntimeContext runtimeContext = performBlockingInteraction.getRuntimeContext();
      //the sessionID should never be set by the consumer. Sessions are handled by cookies instead
      SessionParams sessionParams = new SessionParams();
      sessionParams.setSessionID("Hello World");
      runtimeContext.setSessionParams(sessionParams);

      try
      {
         producer.performBlockingInteraction(performBlockingInteraction);
         ExtendedAssert.fail("Should not be able to pass a sessionID in a PerformBlockingInteraction()");
      }
      catch (OperationFailed expected)
      {
         // expected
      }
   }

   @Test
   public void testMarkupCaching() throws Exception
   {
      GetMarkup getMarkup = createMarkupRequest();

      MarkupResponse response = producer.getMarkup(getMarkup);

      CacheControl cacheControl = response.getMarkupContext().getCacheControl();
      ExtendedAssert.assertNotNull(cacheControl);
      ExtendedAssert.assertEquals(WSRPConstants.CACHE_PER_USER, cacheControl.getUserScope());
      ExtendedAssert.assertEquals(15, cacheControl.getExpires());

      undeploy(DEFAULT_MARKUP_PORTLET_WAR);
      String sessionPortletArchive = "test-session-portlet.war";
      deploy(sessionPortletArchive);

      response = producer.getMarkup(createMarkupRequestForCurrentlyDeployedPortlet());

      cacheControl = response.getMarkupContext().getCacheControl();
      ExtendedAssert.assertNull(cacheControl);

      undeploy(sessionPortletArchive);
   }

   @Test
   public void testGetMarkupWithDispatcherPortlet() throws Exception
   {

      undeploy(DEFAULT_MARKUP_PORTLET_WAR);
      String dispatcherPortletArchive = "test-dispatcher-portlet.war";
      deploy(dispatcherPortletArchive);

      try
      {
         GetMarkup getMarkup = createMarkupRequestForCurrentlyDeployedPortlet();

         MarkupResponse response = producer.getMarkup(getMarkup);
         checkMarkupResponse(response, "test");
      }
      finally
      {
         undeploy(dispatcherPortletArchive);
      }
   }

   @Test
   public void testGetMarkupWithNoContent() throws Exception
   {
      undeploy(DEFAULT_MARKUP_PORTLET_WAR);
      String basicPortletArchive = "test-basic-portlet.war";
      deploy(basicPortletArchive);

      try
      {
         GetMarkup getMarkup = createMarkupRequestForCurrentlyDeployedPortlet();

         MarkupResponse response = producer.getMarkup(getMarkup);
         checkMarkupResponse(response, "");
      }
      finally
      {
         undeploy(basicPortletArchive);
      }
   }

   @Test
   public void testGetMarkupWithNonStandardLocalesStrictMode() throws Exception
   {
      undeploy(DEFAULT_MARKUP_PORTLET_WAR);
      String getLocalesPortletArchive = "test-getlocales-portlet.war";
      deploy(getLocalesPortletArchive);

      GetMarkup getMarkup = createMarkupRequestForCurrentlyDeployedPortlet();
      // we need to clear the value first for this test since the getLocales will be populated
      // with default values for the system, and since en_US is not marked as a supported-locale in portlet.xml
      // the default values will be used.
      getMarkup.getMarkupParams().getLocales().clear();
      getMarkup.getMarkupParams().getLocales().add("en_US");

      try
      {
         producer.getMarkup(getMarkup);
         //fail("Should have thrown an UnsupportetLocaleFault"); // ideally cf http://jira.jboss.com/jira/browse/JBPORTAL-857
         ExtendedAssert.fail("Should have thrown an exception"); // right now
      }
      catch (Exception expected)
      {
         // expected
      }
      finally
      {
         // checkMarkupResponse(response, "GetLocalesPortlet"); // should we return try to generate markup regardless?
         undeploy(getLocalesPortletArchive);
      }
   }

   @Test
   public void testGetMarkupWithNonStandardLocalesLenientMode() throws Exception
   {
      undeploy(DEFAULT_MARKUP_PORTLET_WAR);
      String getLocalesPortletArchive = "test-getlocales-portlet.war";
      deploy(getLocalesPortletArchive);

      GetMarkup getMarkup = createMarkupRequestForCurrentlyDeployedPortlet();
      // we need to clear the value first for this test since the getLocales will be populated
      // with default values for the system, and since en_US is not marked as a supported-locale in portlet.xml
      // the default values will be used.
      getMarkup.getMarkupParams().getLocales().clear();
      getMarkup.getMarkupParams().getLocales().add("en_US");

      // Use the lenient mode
      producer.usingStrictModeChangedTo(false);

      // markup should be properly generated
      checkMarkupResponse(producer.getMarkup(getMarkup), "English (United States)");
      undeploy(getLocalesPortletArchive);
   }

   @Test
   public void testGetMarkupWithoutDeclaredLocale() throws Exception
   {
      undeploy(DEFAULT_MARKUP_PORTLET_WAR);
      String getLocalesPortletArchive = "test-getlocales-portlet.war";
      deploy(getLocalesPortletArchive);

      GetMarkup getMarkup = createMarkupRequest(getPortletHandleFrom("No Declared"));

      try
      {
         checkMarkupResponse(producer.getMarkup(getMarkup), Locale.getDefault().getDisplayName());
      }
      finally
      {
         undeploy(getLocalesPortletArchive);
      }
   }

   @Test
   public void testGetMarkupLocales() throws Exception
   {
      undeploy(DEFAULT_MARKUP_PORTLET_WAR);
      String getLocalesPortletArchive = "test-getlocales-portlet.war";
      deploy(getLocalesPortletArchive);

      GetMarkup getMarkup = createMarkupRequest(getPortletHandleFrom("Simple"));

      try
      {
         List<String> locales = getMarkup.getMarkupParams().getLocales();
         locales.add("en");
         locales.add("fr");
         MarkupResponse response = producer.getMarkup(getMarkup);
         checkMarkupResponse(response, Locale.ENGLISH.getDisplayName());

         locales.clear();
         locales.add("fr");
         locales.add("en");
         response = producer.getMarkup(getMarkup);
         checkMarkupResponse(response, Locale.FRENCH.getDisplayName());

         locales.clear();
         locales.add("de");
         locales.add("en");
         response = producer.getMarkup(getMarkup);
         checkMarkupResponse(response, Locale.ENGLISH.getDisplayName());
      }
      finally
      {
         undeploy(getLocalesPortletArchive);
      }
   }

   @Test
   public void testGetMarkupWithEncodedURLs() throws Exception
   {
      undeploy(DEFAULT_MARKUP_PORTLET_WAR);
      String encodeURLPortletArchive = "test-encodeurl-portlet.war";
      deploy(encodeURLPortletArchive);

      try
      {
         GetMarkup getMarkup = createMarkupRequestForCurrentlyDeployedPortlet();

         MarkupResponse response = producer.getMarkup(getMarkup);
         checkMarkupResponse(response, "wsrp_rewrite?wsrp-urlType=blockingAction&wsrp-interactionState=JBPNS_/wsrp_rewrite\n" +
            "wsrp_rewrite?wsrp-urlType=render&wsrp-navigationalState=JBPNS_/wsrp_rewrite");
      }
      finally
      {
         undeploy(encodeURLPortletArchive);
      }
   }

   @Test
   public void testGetMarkupWithUserContext() throws Exception
   {
      undeploy(DEFAULT_MARKUP_PORTLET_WAR);
      String userContextPortletArchive = "test-usercontext-portlet.war";
      deploy(userContextPortletArchive);

      try
      {
         GetMarkup getMarkup = createMarkupRequestForCurrentlyDeployedPortlet();
         getMarkup.setUserContext(WSRPTypeFactory.createUserContext("johndoe"));

         MarkupResponse response = producer.getMarkup(getMarkup);
         checkMarkupResponse(response, "user: johndoe");
      }
      finally
      {
         undeploy(userContextPortletArchive);
      }
   }

   @Test
   public void testGetMarkupMultiValuedFormParams() throws Exception
   {
      undeploy(DEFAULT_MARKUP_PORTLET_WAR);
      String multiValuedPortletArchive = "test-multivalued-portlet.war";
      deploy(multiValuedPortletArchive);

      NamedString namedString = createNamedString("multi", "value1");
      try
      {
         PerformBlockingInteraction action =
            WSRPTypeFactory.createDefaultPerformBlockingInteraction(getHandleForCurrentlyDeployedArchive());
         List<NamedString> formParameters = action.getInteractionParams().getFormParameters();
         formParameters.add(namedString);
         BlockingInteractionResponse actionResponse = producer.performBlockingInteraction(action);
         GetMarkup markupRequest = createMarkupRequestForCurrentlyDeployedPortlet();
         markupRequest.getMarkupParams().setNavigationalContext(actionResponse.getUpdateResponse().getNavigationalContext());
         MarkupResponse response = producer.getMarkup(markupRequest);
         checkMarkupResponse(response, "multi: value1");

         formParameters.clear();
         formParameters.add(namedString);
         formParameters.add(createNamedString("multi", "value2"));
         actionResponse = producer.performBlockingInteraction(action);
         markupRequest = createMarkupRequestForCurrentlyDeployedPortlet();
         markupRequest.getMarkupParams().setNavigationalContext(actionResponse.getUpdateResponse().getNavigationalContext());
         response = producer.getMarkup(markupRequest);
         checkMarkupResponse(response, "multi: value1, value2");

         formParameters.clear();
         formParameters.add(new NamedString());
         actionResponse = producer.performBlockingInteraction(action);
         markupRequest = createMarkupRequestForCurrentlyDeployedPortlet();
         markupRequest.getMarkupParams().setNavigationalContext(actionResponse.getUpdateResponse().getNavigationalContext());
         response = producer.getMarkup(markupRequest);
         checkMarkupResponse(response, "multi: ");
      }
      finally
      {
         undeploy(multiValuedPortletArchive);
      }
   }

   @Test
   public void testImplicitCloning() throws Exception
   {
      undeploy(DEFAULT_MARKUP_PORTLET_WAR);
      String archiveName = "test-implicitcloning-portlet.war";
      deploy(archiveName);

      try
      {
         // check the initial value
         GetMarkup gm = createMarkupRequestForCurrentlyDeployedPortlet();
         MarkupResponse res = producer.getMarkup(gm);
         String markupString = res.getMarkupContext().getItemString();
         ExtendedAssert.assertEquals("initial", markupString);

         // modify the preference value
         PerformBlockingInteraction pbi = WSRPTypeFactory.createDefaultPerformBlockingInteraction(getHandleForCurrentlyDeployedArchive());
         pbi.getInteractionParams().setPortletStateChange(StateChange.CLONE_BEFORE_WRITE); // request cloning if needed
         String value = "new value";
         pbi.getInteractionParams().getFormParameters().add(createNamedString("value", value));
         BlockingInteractionResponse response = producer.performBlockingInteraction(pbi);
         ExtendedAssert.assertNotNull(response);

         // check that we got a new portlet context
         PortletContext pc = response.getUpdateResponse().getPortletContext();
         ExtendedAssert.assertNotNull(pc);

         // get the markup again and check that we still get the initial value with the initial portlet context
         res = producer.getMarkup(gm);
         markupString = res.getMarkupContext().getItemString();
         ExtendedAssert.assertEquals("initial", markupString);

         // retrieving the markup with the new portlet context should return the new value
         gm.setPortletContext(pc);
         res = producer.getMarkup(gm);
         markupString = res.getMarkupContext().getItemString();
         ExtendedAssert.assertEquals(value, markupString);
      }
      finally
      {
         undeploy(archiveName);
      }
   }

   private NamedString createNamedString(String name, String value)
   {
      NamedString namedString = new NamedString();
      namedString.setName(name);
      namedString.setValue(value);
      return namedString;
   }

   @Test
   public void testGetMarkupWithResource() throws Exception
   {
      undeploy(DEFAULT_MARKUP_PORTLET_WAR);
      String archive = "test-resource-portlet.war";
      deploy(archive);

      try
      {
         GetMarkup gm = createMarkupRequestForCurrentlyDeployedPortlet();
         MarkupResponse res = producer.getMarkup(gm);
         String markupString = res.getMarkupContext().getItemString();

         // accept either localhost or 127.0.0.1 for the host part of the generated markup
         // note that we are using a MockHttpServletRequest, so the host and port come from there.
         String markupStart = "<img src='wsrp_rewrite?wsrp-urlType=resource&amp;wsrp-url=http%3A%2F%2F";
         String markupEnd = "%3A8080%2Ftest-resource-portlet%2Fgif%2Flogo.gif&amp;wsrp-requiresRewrite=true/wsrp_rewrite'/>";
         String localhostMarkup = markupStart + "localhost" + markupEnd;
         String homeIPMarkup = markupStart + "127.0.0.1" + markupEnd;
         boolean result = localhostMarkup.equals(markupString) || homeIPMarkup.equals(markupString);
         ExtendedAssert.assertTrue("Expectd '" + localhostMarkup + "' or '" + homeIPMarkup + "' but received '" + markupString + "'.", result);
      }
      finally
      {
         undeploy(archive);
      }
   }

   @Test
   public void testGetMarkupWithNonURLEncodedResource() throws Exception
   {
      undeploy(DEFAULT_MARKUP_PORTLET_WAR);
      String archive = "test-resourcenoencodeurl-portlet.war";
      deploy(archive);

      try
      {
         GetMarkup gm = createMarkupRequestForCurrentlyDeployedPortlet();
         MarkupResponse res = producer.getMarkup(gm);
         String markupString = res.getMarkupContext().getItemString();

         // accept either localhost or 127.0.0.1 for the host part of the generated markup
         // note that we are using a MockHttpServletRequest, so the host and port come from there.
         String markupStart = "<img src='http://";
         String markupEnd = ":8080/test-resourcenoencodeurl-portlet/gif/logo.gif'/>";
         String localhostMarkup = markupStart + "localhost" + markupEnd;
         String homeIPMarkup = markupStart + "127.0.0.1" + markupEnd;
         boolean result = localhostMarkup.equals(markupString) || homeIPMarkup.equals(markupString);
         ExtendedAssert.assertTrue("Expectd '" + localhostMarkup + "' or '" + homeIPMarkup + "' but received '" + markupString + "'.", result);
      }
      finally
      {
         undeploy(archive);
      }
   }

   @Test
   public void testApplicationScopeVariableHandling() throws Exception
   {
      undeploy(DEFAULT_MARKUP_PORTLET_WAR);
      String archive = "test-applicationscope-portlet.war";
      deploy(archive);

      try
      {
         // set appVar to value in the application scope by the first portlet
         PerformBlockingInteraction pbi = WSRPTypeFactory.createDefaultPerformBlockingInteraction(getPortletHandleFrom("Set"));
         pbi.getInteractionParams().getFormParameters().add(createNamedString("appVar", "value"));
         producer.performBlockingInteraction(pbi);

         // the second portlet reads the appVar value and outputs it
         GetMarkup gm = createMarkupRequest(getPortletHandleFrom("Get"));
         MarkupResponse res = producer.getMarkup(gm);
         checkMarkupResponse(res, "appVar=value");
      }
      finally
      {
         undeploy(archive);
      }
   }

   /*@Test
   public void testGetMarkupWithEventsAndObjects() throws Exception
   {
      undeploy(DEFAULT_MARKUP_PORTLET_WAR);
      String archive = "test-eventswithobject-portlet.war";
      deploy(archive);

      NamedString namedString = createNamedString("username", "pjha");
      try
      {
         List<String> handles = getHandlesForCurrentlyDeployedArchive();
         String generatorHandle = null;
         String consumerHandle = null;
         for (String portletHandle : handles)
         {
            if (portletHandle.contains("Generator"))
            {
               generatorHandle = portletHandle;
            }
            else if (portletHandle.contains("Consumer"))
            {
               consumerHandle = portletHandle;
            }
         }
         PerformBlockingInteraction action = WSRPTypeFactory.createDefaultPerformBlockingInteraction(generatorHandle);
         List<NamedString> formParameters = action.getInteractionParams().getFormParameters();
         formParameters.add(namedString);
         BlockingInteractionResponse actionResponse = producer.performBlockingInteraction(action);
         GetMarkup markupRequestConsumer = createMarkupRequest(consumerHandle);
         markupRequestConsumer.getMarkupParams().setNavigationalContext(actionResponse.getUpdateResponse().getNavigationalContext());
         MarkupResponse response = producer.getMarkup(markupRequestConsumer);
         checkMarkupResponse(response, "Prabhat");
      }
      finally
      {
         undeploy(archive);
      }
   }*/

   @Test
   public void testGetMarkupWithSimpleEvent() throws Exception
   {
      undeploy(DEFAULT_MARKUP_PORTLET_WAR);
      String archive = "test-events-portlet.war";
      deploy(archive);

      NamedString namedString = createNamedString("parameter", "param-value");
      try
      {
         List<String> handles = getHandlesForCurrentlyDeployedArchive();
         String generatorHandle = null;
         String consumerHandle = null;
         for (String portletHandle : handles)
         {
            if (portletHandle.contains("Generator"))
            {
               generatorHandle = portletHandle;
            }
            else if (portletHandle.contains("Consumer"))
            {
               consumerHandle = portletHandle;
            }
         }
         PerformBlockingInteraction action = WSRPTypeFactory.createDefaultPerformBlockingInteraction(generatorHandle);
         List<NamedString> formParameters = action.getInteractionParams().getFormParameters();
         formParameters.add(namedString);
         BlockingInteractionResponse actionResponse = producer.performBlockingInteraction(action);
         GetMarkup markupRequestConsumer = createMarkupRequest(consumerHandle);
         markupRequestConsumer.getMarkupParams().setNavigationalContext(actionResponse.getUpdateResponse().getNavigationalContext());
         MarkupResponse response = producer.getMarkup(markupRequestConsumer);
         checkMarkupResponse(response, "param-value");
      }
      finally
      {
         undeploy(archive);
      }
   }

   @Test
   public void testGetMarkupNoRegistrationWhenRegistrationRequired() throws Exception
   {
      configureRegistrationSettings(true, false);

      GetMarkup gm = createMarkupRequest();
      try
      {
         producer.getMarkup(gm);
         ExtendedAssert.fail("Should have thrown InvalidRegistration!");
      }
      catch (InvalidRegistration invalidRegistration)
      {
         // expected
      }
      catch (Exception e)
      {
         ExtendedAssert.fail(e.getMessage());
      }
   }

   @Test
   public void testPerformBlockingInteractionNoRegistrationWhenRegistrationRequired() throws Exception
   {
      configureRegistrationSettings(true, false);

      PerformBlockingInteraction pbi = WSRPTypeFactory.createDefaultPerformBlockingInteraction(getHandleForCurrentlyDeployedArchive());
      try
      {
         producer.performBlockingInteraction(pbi);
         ExtendedAssert.fail("Should have thrown InvalidRegistration!");
      }
      catch (InvalidRegistration invalidRegistration)
      {
         // expected
      }
      catch (Exception e)
      {
         ExtendedAssert.fail(e.getMessage());
      }
   }

   @Test
   public void testInitCookieNoRegistrationWhenRegistrationRequired() throws Exception
   {
      configureRegistrationSettings(true, false);

      InitCookie initCookie = WSRPTypeFactory.createInitCookie(null);
      try
      {
         producer.initCookie(initCookie);
         ExtendedAssert.fail("Should have thrown InvalidRegistration!");
      }
      catch (InvalidRegistration invalidRegistration)
      {
         // expected
      }
      catch (Exception e)
      {
         ExtendedAssert.fail(e.getMessage());
      }
   }

   private String checkPBIAndGetNavigationalState(String symbol) throws Exception
   {
      PerformBlockingInteraction performBlockingInteraction =
         WSRPTypeFactory.createDefaultPerformBlockingInteraction(getDefaultHandle());
      InteractionParams interactionParams = performBlockingInteraction.getInteractionParams();
      interactionParams.getFormParameters().add(createNamedString("symbol", symbol));

      BlockingInteractionResponse response = producer.performBlockingInteraction(performBlockingInteraction);
      ExtendedAssert.assertNotNull(response);

      // this is not a redirect...
      ExtendedAssert.assertNull(response.getRedirectURL());

      // check update response
      UpdateResponse updateResponse = response.getUpdateResponse();
      ExtendedAssert.assertNotNull(updateResponse);
      // request was readOnly so no updated portlet context
      ExtendedAssert.assertNull(updateResponse.getPortletContext());
      // check that no sessionId is getting passed.
      ExtendedAssert.assertNull(updateResponse.getSessionContext());

      String navigationalState = updateResponse.getNavigationalContext().getOpaqueValue();
      ExtendedAssert.assertNotNull(navigationalState);
      ExtendedAssert.assertEquals(updateResponse.getNewMode(), WSRPConstants.VIEW_MODE);
      MarkupContext markupContext = updateResponse.getMarkupContext();
      ExtendedAssert.assertNull(markupContext); // we don't return markup for now

      return navigationalState;
   }

   private void checkMarkupResponseWithSession(MarkupResponse response, int count) throws RemoteException, InvalidRegistration, OperationFailed
   {
      ExtendedAssert.assertNotNull(response);

      // Markup context
      MarkupContext markupContext = response.getMarkupContext();
      ExtendedAssert.assertNotNull(markupContext);
      String markupString = markupContext.getItemString();
      ExtendedAssert.assertString1ContainsString2(markupString, "count = " + count);
      ExtendedAssert.assertString1ContainsString2(markupString, "<a href='wsrp_rewrite?wsrp-urlType=render&wsrp-navigationalState=JBPNS_/wsrp_rewrite'>render</a>");

      // checking session
      checkSessionForCurrentlyDeployedPortlet(response);
   }

   private MarkupContext checkMarkupResponse(MarkupResponse response, String markupString)
   {
      ExtendedAssert.assertNotNull(response);

      // Markup context
      MarkupContext markupContext = response.getMarkupContext();
      ExtendedAssert.assertNotNull(markupContext);
      ExtendedAssert.assertEquals("text/html", markupContext.getMimeType());
      ExtendedAssert.assertEquals("title", markupContext.getPreferredTitle());
      if (!ParameterValidation.isNullOrEmpty(markupString))
      {
         ExtendedAssert.assertTrue(markupContext.isRequiresRewriting());
      }
      else
      {
         ExtendedAssert.assertFalse(markupContext.isRequiresRewriting());
      }
      ExtendedAssert.assertEquals(markupString, markupContext.getItemString());

      // Session context
      SessionContext sessionContext = response.getSessionContext();
      // The session information is should never be sent to the consumer, Cookies are used instead.
      ExtendedAssert.assertNull(sessionContext);

      return markupContext;
   }

   protected String getMostUsedPortletWARFileName()
   {
      return DEFAULT_MARKUP_PORTLET_WAR;
   }
}


