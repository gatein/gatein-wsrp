/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2009, Red Hat Middleware, LLC, and individual                    *
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

package org.gatein.wsrp.protocol.v1;

import org.gatein.wsrp.WSRPActionURL;
import org.gatein.wsrp.WSRPConstants;
import org.gatein.wsrp.WSRPPortletURL;
import org.gatein.wsrp.WSRPRenderURL;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.test.ExtendedAssert;
import org.oasis.wsrp.v1.BlockingInteractionResponse;
import org.oasis.wsrp.v1.CacheControl;
import org.oasis.wsrp.v1.GetMarkup;
import org.oasis.wsrp.v1.InitCookie;
import org.oasis.wsrp.v1.InteractionParams;
import org.oasis.wsrp.v1.InvalidRegistration;
import org.oasis.wsrp.v1.MarkupContext;
import org.oasis.wsrp.v1.MarkupResponse;
import org.oasis.wsrp.v1.NamedString;
import org.oasis.wsrp.v1.OperationFailed;
import org.oasis.wsrp.v1.PerformBlockingInteraction;
import org.oasis.wsrp.v1.PortletContext;
import org.oasis.wsrp.v1.RuntimeContext;
import org.oasis.wsrp.v1.SessionContext;
import org.oasis.wsrp.v1.StateChange;
import org.oasis.wsrp.v1.UnsupportedMode;
import org.oasis.wsrp.v1.UpdateResponse;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Locale;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 13149 $
 * @since 2.4
 */
public class MarkupTestCase extends NeedPortletHandleTest
{
   private static final String DEFAULT_VIEW_MARKUP = "<p>symbol unset stock value: value unset</p>";
   private static final String DEFAULT_MARKUP_PORTLET_WAR = "test-markup-portlet.war";

   public MarkupTestCase()
      throws Exception
   {
      super("MarkupTestCase", DEFAULT_MARKUP_PORTLET_WAR);
   }

   public void testGetMarkupViewNoSession() throws Exception
   {
      GetMarkup getMarkup = createMarkupRequest();

      MarkupResponse response = markupService.getMarkup(getMarkup);

      checkMarkupResponse(response, DEFAULT_VIEW_MARKUP);
   }

   public void testInvalidGetMarkup() throws Exception
   {
      GetMarkup getMarkup = createMarkupRequest();
      getMarkup.getMarkupParams().setMode("invalid mode");

      try
      {
         markupService.getMarkup(getMarkup);
         ExtendedAssert.fail();
      }
      catch (UnsupportedMode unsupportedMode)
      {
         // expected
      }
   }

   public void testGetMarkupWithSessionID() throws Exception
   {
      // The consumer should never have access to or be able to set a sessionID. Sessions are handled by the Producer using cookies.
      GetMarkup getMarkup = createMarkupRequest();
      getMarkup.getRuntimeContext().setSessionID("Hello World");

      try
      {
         markupService.getMarkup(getMarkup);
         ExtendedAssert.fail("A sessionID should not be allowed to be passed in GetMarkup()");
      }
      catch (OperationFailed operationFailed)
      {
         // expected
      }
   }

   public void testGetMarkupEditNoSession() throws Exception
   {
      GetMarkup getMarkup = createMarkupRequest();
      getMarkup.getMarkupParams().setMode(WSRPConstants.EDIT_MODE);

      MarkupResponse response = markupService.getMarkup(getMarkup);

      checkMarkupResponse(response, "<form method='post' action='wsrp_rewrite?wsrp-urlType=blockingAction&wsrp" +
         "-interactionState=JBPNS_/wsrp_rewrite' id='wsrp_rewrite_portfolioManager'><table><tr><td>Stock symbol</t" +
         "d><td><input name='symbol'/></td></tr><tr><td><input type='submit' value='Submit'></td></tr></table></form>");
   }

   public void testGetMarkupRenderParameters() throws Exception
   {
      undeploy(DEFAULT_MARKUP_PORTLET_WAR);
      String archiveName = "test-renderparam-portlet.war";
      deploy(archiveName);

      try
      {
         GetMarkup gm = createMarkupRequestForCurrentlyDeployedPortlet();
         MarkupResponse res = markupService.getMarkup(gm);

         String markupString = res.getMarkupContext().getMarkupString();

         String julienLink = extractLink(markupString, 0);
         WSRPPortletURL julienURL = WSRPPortletURL.create(julienLink);

         ExtendedAssert.assertString1ContainsString2(markupString, "Hello, Anonymous!");
         ExtendedAssert.assertString1ContainsString2(markupString, "Counter: 0");

         ExtendedAssert.assertTrue(julienURL instanceof WSRPRenderURL);
         WSRPRenderURL julienRender = (WSRPRenderURL)julienURL;

         // We're now trying to get a hello for Julien ;)
         gm.getMarkupParams().setNavigationalState(julienRender.getNavigationalState().getStringValue());
         res = markupService.getMarkup(gm);
         markupString = res.getMarkupContext().getMarkupString();
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
         markupService.performBlockingInteraction(performBlockingInteraction);
         res = markupService.getMarkup(gm);
         markupString = res.getMarkupContext().getMarkupString();
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
   public void testGetMarkupSession() throws Exception
   {
      undeploy(DEFAULT_MARKUP_PORTLET_WAR);
      // deploy session-manipulating portlet
      String sessionPortletArchive = "test-session-portlet.war";
      deploy(sessionPortletArchive);


      try
      {
         GetMarkup getMarkup = createMarkupRequestForCurrentlyDeployedPortlet();

         MarkupResponse response = markupService.getMarkup(getMarkup);

         checkMarkupResponseWithSession(response, 0);

         response = markupService.getMarkup(getMarkup);
         checkMarkupResponseWithSession(response, 1);

         // fix-me: try to reuse the old session id: what should happen?
//      runtimeContext.setSessionID(sessionID);
//      getMarkup.setRuntimeContext(runtimeContext);
//      try
//      {
//         markupService.getMarkup(getMarkup);
//         fail("The session should be invalid...");
//      }
//      catch (InvalidSessionFault expected)
//      {
//      }
      }
      finally
      {
         // reset state
         undeploy(sessionPortletArchive);
      }
   }

   public void testPerformBlockingInteractionNoRedirect() throws Exception
   {
      checkPBIAndGetNavigationalState("RHAT");
   }

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

      BlockingInteractionResponse response = markupService.performBlockingInteraction(performBlockingInteraction);
      ExtendedAssert.assertNotNull(response);

      // this is a redirect...
      String redirectURL = response.getRedirectURL();
      ExtendedAssert.assertNotNull(redirectURL);
      ExtendedAssert.assertEquals("/WEB-INF/jsp/help.jsp", redirectURL); // fix-me: handle URL re-writing

      // no update response
      UpdateResponse updateResponse = response.getUpdateResponse();
      ExtendedAssert.assertNull(updateResponse);
   }

   public void testGMAndPBIInteraction() throws Exception
   {
      testGetMarkupViewNoSession();
      String symbol = "AAPL";
      String navigationalState = checkPBIAndGetNavigationalState(symbol);

      GetMarkup getMarkup = createMarkupRequest();
      getMarkup.getMarkupParams().setNavigationalState(navigationalState);
      MarkupResponse response = markupService.getMarkup(getMarkup);
      checkMarkupResponse(response, "<p>" + symbol + " stock value: 123.45</p>");
   }


   public void testPBIWithSessionID() throws Exception
   {
      String portletHandle = getDefaultHandle();
      PerformBlockingInteraction performBlockingInteraction = WSRPTypeFactory.createDefaultPerformBlockingInteraction(portletHandle);

      RuntimeContext runtimeContext = performBlockingInteraction.getRuntimeContext();
      //the sessionID should never be set by the consumer. Sessions are handled by cookies instead 
      runtimeContext.setSessionID("Hello World");

      try
      {
         markupService.performBlockingInteraction(performBlockingInteraction);
         ExtendedAssert.fail("Should not be able to pass a sessionID in a PerformBlockingInteraction()");
      }
      catch (OperationFailed expected)
      {
         // expected
      }
   }

   public void testMarkupCaching() throws Exception
   {
      GetMarkup getMarkup = createMarkupRequest();

      MarkupResponse response = markupService.getMarkup(getMarkup);

      CacheControl cacheControl = response.getMarkupContext().getCacheControl();
      ExtendedAssert.assertNotNull(cacheControl);
      ExtendedAssert.assertEquals(WSRPConstants.CACHE_PER_USER, cacheControl.getUserScope());
      ExtendedAssert.assertEquals(15, cacheControl.getExpires());

      undeploy(DEFAULT_MARKUP_PORTLET_WAR);
      String sessionPortletArchive = "test-session-portlet.war";
      deploy(sessionPortletArchive);

      response = markupService.getMarkup(createMarkupRequestForCurrentlyDeployedPortlet());

      cacheControl = response.getMarkupContext().getCacheControl();
      ExtendedAssert.assertNull(cacheControl);

      undeploy(sessionPortletArchive);
   }

   public void testGetMarkupWithDispatcherPortlet() throws Exception
   {

      undeploy(DEFAULT_MARKUP_PORTLET_WAR);
      String dispatcherPortletArchive = "test-dispatcher-portlet.war";
      deploy(dispatcherPortletArchive);

      try
      {
         GetMarkup getMarkup = createMarkupRequestForCurrentlyDeployedPortlet();

         MarkupResponse response = markupService.getMarkup(getMarkup);
         checkMarkupResponse(response, "test");
      }
      finally
      {
         undeploy(dispatcherPortletArchive);
      }
   }

   public void testGetMarkupWithNoContent() throws Exception
   {
      undeploy(DEFAULT_MARKUP_PORTLET_WAR);
      String basicPortletArchive = "test-basic-portlet.war";
      deploy(basicPortletArchive);

      try
      {
         GetMarkup getMarkup = createMarkupRequestForCurrentlyDeployedPortlet();

         MarkupResponse response = markupService.getMarkup(getMarkup);
         checkMarkupResponse(response, "");
      }
      finally
      {
         undeploy(basicPortletArchive);
      }
   }

   public void testGetMarkupWithNonStandardLocalesStrictMode() throws Exception
   {
      undeploy(DEFAULT_MARKUP_PORTLET_WAR);
      String getLocalesPortletArchive = "test-getlocales-portlet.war";
      deploy(getLocalesPortletArchive);

      GetMarkup getMarkup = createMarkupRequestForCurrentlyDeployedPortlet();
      getMarkup.getMarkupParams().getLocales().add("en_US");

      try
      {
         markupService.getMarkup(getMarkup);
         //fail("Should have trown an UnsupportetLocaleFault"); // ideally cf http://jira.jboss.com/jira/browse/JBPORTAL-857
         ExtendedAssert.fail("Should have trown an exception"); // right now
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

   public void testGetMarkupWithNonStandardLocalesLenientMode() throws Exception
   {
      undeploy(DEFAULT_MARKUP_PORTLET_WAR);
      String getLocalesPortletArchive = "test-getlocales-portlet.war";
      deploy(getLocalesPortletArchive);

      GetMarkup getMarkup = createMarkupRequestForCurrentlyDeployedPortlet();
      getMarkup.getMarkupParams().getLocales().add("en_US");

      // Use the lenient mode
      producer.usingStrictModeChangedTo(false);

      // markup should be properly generated
      checkMarkupResponse(markupService.getMarkup(getMarkup), "English (United States)");
      undeploy(getLocalesPortletArchive);
   }

   public void testGetMarkupWithoutDeclaredLocale() throws Exception
   {
      undeploy(DEFAULT_MARKUP_PORTLET_WAR);
      String getLocalesPortletArchive = "test-getlocales-portlet.war";
      deploy(getLocalesPortletArchive);

      GetMarkup getMarkup = createMarkupRequest(getPortletHandleFrom("No Declared"));

      try
      {
         checkMarkupResponse(markupService.getMarkup(getMarkup), Locale.getDefault().getDisplayName());
      }
      finally
      {
         undeploy(getLocalesPortletArchive);
      }
   }

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
         MarkupResponse response = markupService.getMarkup(getMarkup);
         checkMarkupResponse(response, Locale.ENGLISH.getDisplayName());

         locales.clear();
         locales.add("fr");
         locales.add("en");
         response = markupService.getMarkup(getMarkup);
         checkMarkupResponse(response, Locale.FRENCH.getDisplayName());

         locales.clear();
         locales.add("de");
         locales.add("en");
         response = markupService.getMarkup(getMarkup);
         checkMarkupResponse(response, Locale.ENGLISH.getDisplayName());
      }
      finally
      {
         undeploy(getLocalesPortletArchive);
      }
   }

   public void testGetMarkupWithEncodedURLs() throws Exception
   {
      undeploy(DEFAULT_MARKUP_PORTLET_WAR);
      String encodeURLPortletArchive = "test-encodeurl-portlet.war";
      deploy(encodeURLPortletArchive);

      try
      {
         GetMarkup getMarkup = createMarkupRequestForCurrentlyDeployedPortlet();

         MarkupResponse response = markupService.getMarkup(getMarkup);
         checkMarkupResponse(response, "wsrp_rewrite?wsrp-urlType=blockingAction&wsrp-interactionState=JBPNS_/wsrp_rewrite\n" +
            "wsrp_rewrite?wsrp-urlType=render&wsrp-navigationalState=JBPNS_/wsrp_rewrite");
      }
      finally
      {
         undeploy(encodeURLPortletArchive);
      }
   }

   public void testGetMarkupWithUserContext() throws Exception
   {
      undeploy(DEFAULT_MARKUP_PORTLET_WAR);
      String userContextPortletArchive = "test-usercontext-portlet.war";
      deploy(userContextPortletArchive);

      try
      {
         GetMarkup getMarkup = createMarkupRequestForCurrentlyDeployedPortlet();
         getMarkup.setUserContext(WSRPTypeFactory.createUserContext("johndoe"));

         MarkupResponse response = markupService.getMarkup(getMarkup);
         checkMarkupResponse(response, "user: johndoe");
      }
      finally
      {
         undeploy(userContextPortletArchive);
      }
   }

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
         BlockingInteractionResponse actionResponse = markupService.performBlockingInteraction(action);
         GetMarkup markupRequest = createMarkupRequestForCurrentlyDeployedPortlet();
         markupRequest.getMarkupParams().setNavigationalState(actionResponse.getUpdateResponse().getNavigationalState());
         MarkupResponse response = markupService.getMarkup(markupRequest);
         checkMarkupResponse(response, "multi: value1");

         formParameters.clear();
         formParameters.add(namedString);
         formParameters.add(createNamedString("multi", "value2"));
         actionResponse = markupService.performBlockingInteraction(action);
         markupRequest = createMarkupRequestForCurrentlyDeployedPortlet();
         markupRequest.getMarkupParams().setNavigationalState(actionResponse.getUpdateResponse().getNavigationalState());
         response = markupService.getMarkup(markupRequest);
         checkMarkupResponse(response, "multi: value1, value2");

         formParameters.clear();
         formParameters.add(new NamedString());
         actionResponse = markupService.performBlockingInteraction(action);
         markupRequest = createMarkupRequestForCurrentlyDeployedPortlet();
         markupRequest.getMarkupParams().setNavigationalState(actionResponse.getUpdateResponse().getNavigationalState());
         response = markupService.getMarkup(markupRequest);
         checkMarkupResponse(response, "multi: ");
      }
      finally
      {
         undeploy(multiValuedPortletArchive);
      }
   }

   public void testImplicitCloning() throws Exception
   {
      undeploy(DEFAULT_MARKUP_PORTLET_WAR);
      String archiveName = "test-implicitcloning-portlet.war";
      deploy(archiveName);

      try
      {
         // check the initial value
         GetMarkup gm = createMarkupRequestForCurrentlyDeployedPortlet();
         MarkupResponse res = markupService.getMarkup(gm);
         String markupString = res.getMarkupContext().getMarkupString();
         ExtendedAssert.assertEquals("initial", markupString);

         // modify the preference value
         PerformBlockingInteraction pbi = WSRPTypeFactory.createDefaultPerformBlockingInteraction(getHandleForCurrentlyDeployedArchive());
         pbi.getInteractionParams().setPortletStateChange(StateChange.CLONE_BEFORE_WRITE); // request cloning if needed
         String value = "new value";
         pbi.getInteractionParams().getFormParameters().add(createNamedString("value", value));
         BlockingInteractionResponse response = markupService.performBlockingInteraction(pbi);
         ExtendedAssert.assertNotNull(response);

         // check that we got a new portlet context
         PortletContext pc = response.getUpdateResponse().getPortletContext();
         ExtendedAssert.assertNotNull(pc);

         // get the markup again and check that we still get the initial value with the initial portlet context
         res = markupService.getMarkup(gm);
         markupString = res.getMarkupContext().getMarkupString();
         ExtendedAssert.assertEquals("initial", markupString);

         // retrieving the markup with the new portlet context should return the new value
         gm.setPortletContext(pc);
         res = markupService.getMarkup(gm);
         markupString = res.getMarkupContext().getMarkupString();
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

   public void testGetMarkupWithResource() throws Exception
   {
      undeploy(DEFAULT_MARKUP_PORTLET_WAR);
      String archive = "test-resource-portlet.war";
      deploy(archive);

      try
      {
         GetMarkup gm = createMarkupRequestForCurrentlyDeployedPortlet();
         MarkupResponse res = markupService.getMarkup(gm);
         String markupString = res.getMarkupContext().getMarkupString();

         // accept either localhost or 127.0.0.1 for the host part of the generated markup
         String markupStart = "<img src='wsrp_rewrite?wsrp-urlType=resource&amp;wsrp-url=http%3A%2F%2F";
         String markupEnd = "%3A8080%2Ftest-resource-portlet%2Fgif%2Flogo.gif&amp;wsrp-requiresRewrite=true/wsrp_rewrite'/>";
         String localhostMarkup = markupStart + "localhost" + markupEnd;
         String homeIPMarkup = markupStart + "127.0.0.1" + markupEnd;
         boolean result = localhostMarkup.equals(markupString) || homeIPMarkup.equals(markupString);
         ExtendedAssert.assertTrue(result);
      }
      finally
      {
         undeploy(archive);
      }
   }

   public void testGetMarkupWithNonURLEncodedResource() throws Exception
   {
      undeploy(DEFAULT_MARKUP_PORTLET_WAR);
      String archive = "test-resourcenoencodeurl-portlet.war";
      deploy(archive);

      try
      {
         GetMarkup gm = createMarkupRequestForCurrentlyDeployedPortlet();
         MarkupResponse res = markupService.getMarkup(gm);
         String markupString = res.getMarkupContext().getMarkupString();

         // accept either localhost or 127.0.0.1 for the host part of the generated markup
         String markupStart = "<img src='http://";
         String markupEnd = ":8080/test-resourcenoencodeurl-portlet/gif/logo.gif'/>";
         String localhostMarkup = markupStart + "localhost" + markupEnd;
         String homeIPMarkup = markupStart + "127.0.0.1" + markupEnd;
         boolean result = localhostMarkup.equals(markupString) || homeIPMarkup.equals(markupString);
         ExtendedAssert.assertTrue(result);
      }
      finally
      {
         undeploy(archive);
      }
   }

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
         markupService.performBlockingInteraction(pbi);

         // the second portlet reads the appVar value and outputs it
         GetMarkup gm = createMarkupRequest(getPortletHandleFrom("Get"));
         MarkupResponse res = markupService.getMarkup(gm);
         checkMarkupResponse(res, "appVar=value");
      }
      finally
      {
         undeploy(archive);
      }
   }

   public void testGetMarkupNoRegistrationWhenRegistrationRequired() throws Exception
   {
      configureRegistrationSettings(true, false);

      GetMarkup gm = createMarkupRequest();
      try
      {
         markupService.getMarkup(gm);
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

   public void testPerformBlockingInteractionNoRegistrationWhenRegistrationRequired() throws Exception
   {
      configureRegistrationSettings(true, false);

      PerformBlockingInteraction pbi = WSRPTypeFactory.createDefaultPerformBlockingInteraction(getHandleForCurrentlyDeployedArchive());
      try
      {
         markupService.performBlockingInteraction(pbi);
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

   public void testInitCookieNoRegistrationWhenRegistrationRequired() throws Exception
   {
      configureRegistrationSettings(true, false);

      InitCookie initCookie = WSRPTypeFactory.createInitCookie(null);
      try
      {
         markupService.initCookie(initCookie);
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

      BlockingInteractionResponse response = markupService.performBlockingInteraction(performBlockingInteraction);
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

      String navigationalState = updateResponse.getNavigationalState();
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
      String markupString = markupContext.getMarkupString();
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
      ExtendedAssert.assertTrue(markupContext.isRequiresUrlRewriting());
      ExtendedAssert.assertEquals(markupString, markupContext.getMarkupString());

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
