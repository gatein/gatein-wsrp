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

import org.gatein.common.util.ParameterValidation;
import org.gatein.pc.api.InvokerUnavailableException;
import org.gatein.pc.api.Portlet;
import org.gatein.pc.api.PortletInvokerException;
import org.gatein.pc.api.invocation.PortletInvocation;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.api.session.SessionEvent;
import org.gatein.wsrp.api.session.SessionEventListener;
import org.gatein.wsrp.consumer.WSRPConsumerImpl;
import org.gatein.wsrp.consumer.portlet.info.WSRPPortletInfo;
import org.gatein.wsrp.handler.RequestHeaderClientHandler;
import org.gatein.wsrp.servlet.UserAccess;
import org.oasis.wsrp.v2.CookieProtocol;
import org.oasis.wsrp.v2.InvalidRegistration;
import org.oasis.wsrp.v2.RuntimeContext;
import org.oasis.wsrp.v2.SessionContext;
import org.oasis.wsrp.v2.SessionParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages session informations on behalf of a consumer.
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 9360 $
 * @since 2.4 (May 31, 2006)
 */
public class SessionHandler implements SessionEventListener
{
   protected WSRPConsumerImpl consumer;
   private static Logger log = LoggerFactory.getLogger(SessionHandler.class);

   /** The prefix used to isolate WSRP-related session information in the actual session object. */
   private static final String SESSION_ID_PREFIX = "org.gatein.wsrp.session.";

   /** session id -> ProducerSessionInformation */
   private Map<String, ProducerSessionInformation> sessionInfos = new ConcurrentHashMap<String, ProducerSessionInformation>(); // todo: thread-safe?

   /**
    * Constructs a new SessionHandler.
    *
    * @param consumer the consumer this SessionHandler is associated with.
    */
   public SessionHandler(WSRPConsumerImpl consumer)
   {
      this.consumer = consumer;
   }

   public boolean isPerUserCookieInit()
   {
      return CookieProtocol.PER_USER.equals(getRequiresInitCookie());
   }

   public boolean requiresInitCookie()
   {
      return getRequiresInitCookie() != null && !CookieProtocol.NONE.equals(getRequiresInitCookie());
   }

   void initProducerSessionInformation(PortletInvocation invocation) throws PortletInvokerException
   {
      // if we need cookies, set the current group id
      String groupId;
      if (requiresGroupInitCookie())
      {
         WSRPPortletInfo info = consumer.getPortletInfo(invocation);
         groupId = info.getGroupId();
      }
      else
      {
         groupId = null;
      }

      RequestHeaderClientHandler.setCurrentInfo(groupId, getProducerSessionInformation(invocation, true));
   }

   private boolean requiresGroupInitCookie()
   {
      return requiresInitCookie() && CookieProtocol.PER_GROUP.equals(getRequiresInitCookie());
   }

   /** Resets the information held by RequestHeaderClientHandler for the current interaction. */
   public void resetCurrentlyHeldInformation()
   {
      RequestHeaderClientHandler.resetCurrentInfo();
   }

   public void initCookieIfNeeded(PortletInvocation invocation) throws PortletInvokerException
   {
      initCookieIfNeeded(invocation, true);
   }

   private void initCookieIfNeeded(PortletInvocation invocation, boolean retryIfFails) throws PortletInvokerException
   {
      // check if the cookie protocol requires cookie initialization
      if (!requiresInitCookie())
      {
         log.debug("Doesn't require initCookie");
         return;
      }

      // check if we have already initialized cookies for this user
      ProducerSessionInformation sessionInformation = getProducerSessionInformation(invocation);
      if (sessionInformation.isInitCookieDone())
      {
         return;
      }
      RequestHeaderClientHandler.setCurrentInfo(null, sessionInformation);

      if (isPerUserCookieInit())
      {
         log.debug("Cookie initialization per user requested.");
         sessionInformation.setPerGroupCookies(false);
         initCookie(invocation, retryIfFails);
      }
      else
      {
         log.debug("Cookie initialization per group requested.");
         sessionInformation.setPerGroupCookies(true);
         Map<String, Set<Portlet>> groups = consumer.getPortletGroupMap();

         try
         {
            for (String groupId : groups.keySet())
            {
               RequestHeaderClientHandler.setCurrentGroupId(groupId);
               log.debug("Initializing cookie for group '" + groupId + "'.");
               initCookie(invocation, retryIfFails);
            }
         }
         finally
         {
            resetCurrentlyHeldInformation();
         }
      }

      sessionInformation.setInitCookieDone(true);
   }

   private void initCookie(PortletInvocation invocation, boolean retryIfFails) throws PortletInvokerException
   {
      try
      {
         consumer.getMarkupService().initCookie(consumer.getRegistrationContext(), UserAccess.getUserContext());
      }
      catch (InvalidRegistration invalidRegistration)
      {
         consumer.handleInvalidRegistrationFault();
         if (retryIfFails)
         {
            initCookieIfNeeded(invocation, false);
         }
      }
      catch (Exception e)
      {
         if (consumer.getProducerInfo().canAttemptRecoveryFrom(e))
         {
            initCookie(invocation, retryIfFails);
         }
         else
         {
            throw new InvokerUnavailableException("Couldn't init cookies!", e);
         }
      }
   }

   void setSessionIdIfNeeded(PortletInvocation invocation, RuntimeContext runtimeContext, String portletHandle)
   {
      ProducerSessionInformation producerSessionInfo = getProducerSessionInformation(invocation, false);
      if (producerSessionInfo != null)
      {
         String sessionId = producerSessionInfo.getSessionIdForPortlet(portletHandle);
         SessionParams sessionParams = runtimeContext.getSessionParams();
         if (sessionParams == null)
         {
            sessionParams = WSRPTypeFactory.createSessionParams(sessionId);
            runtimeContext.setSessionParams(sessionParams);
         }
         else
         {
            sessionParams.setSessionID(sessionId);
         }
      }
   }

   public void updateSessionIfNeeded(SessionContext sessionContext, PortletInvocation invocation, String portletHandle)
   {
      if (sessionContext != null)
      {
         log.debug("Portlet '" + portletHandle + "' created session with id '" + sessionContext.getSessionID() + "'");
         ProducerSessionInformation sessionInfo = getProducerSessionInformation(invocation);
         sessionInfo.addSessionForPortlet(portletHandle, sessionContext);
      }
   }

   void updateCookiesIfNeeded(PortletInvocation invocation)
   {
      ProducerSessionInformation sessionInfo = getProducerSessionInformation(invocation, true);
      ProducerSessionInformation currentSessionInfo = RequestHeaderClientHandler.getCurrentProducerSessionInformation();
      if (sessionInfo != currentSessionInfo)
      {
         sessionInfo.replaceUserCookiesWith(currentSessionInfo);
      }
   }

   public ProducerSessionInformation getProducerSessionInformation(PortletInvocation invocation)
   {
      return getProducerSessionInformation(invocation, true);
   }

   private ProducerSessionInformation getProducerSessionInformation(PortletInvocation invocation, boolean create)
   {
      HttpSession session = WSRPConsumerImpl.getHttpSession(invocation);
      return getProducerSessionInformation(session, create);
   }

   public ProducerSessionInformation getProducerSessionInformation(HttpSession session)
   {
      return getProducerSessionInformation(session, false);
   }

   private ProducerSessionInformation getProducerSessionInformation(HttpSession session, boolean create)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(session, "Session");
      String producerSessionKey = getProducerSessionInformationKey();
      ProducerSessionInformation sessionInformation = (ProducerSessionInformation)session.getAttribute(producerSessionKey);

      if (sessionInformation != null)
      {
         sessionInformation.setParent(this);
         sessionInformation.setParentSessionId(session.getId());
      }
      else
      {
         if (create)
         {
            sessionInformation = new ProducerSessionInformation();
            sessionInformation.setParentSessionId(session.getId());
            session.setAttribute(producerSessionKey, sessionInformation);
            sessionInformation.setParent(this);
         }
      }

      return sessionInformation;
   }

   private String getProducerSessionInformationKey()
   {
      return SESSION_ID_PREFIX + consumer.getProducerId();
   }

   void handleInvalidSessionFault(PortletInvocation invocation, RuntimeContext runtimeContext)
   {
      log.info("Resending information after InvalidSessionFault");
      // invalidate current session
      invalidateSession(invocation);

      // set the session id to null
      if (runtimeContext != null)
      {
         runtimeContext.setSessionParams(null);
      }
   }

   private void invalidateSession(PortletInvocation invocation)
   {
      HttpSession session = WSRPConsumerImpl.getHttpSession(invocation);

      // remove the associated info from the known producer session informations

      ProducerSessionInformation info = getProducerSessionInformation(session, false);
      if (info != null)
      {
         try
         {
            internalReleaseSessions(info.removeSessions());
         }
         catch (PortletInvokerException e)
         {
            // ignore since it's logged by internalReleaseSessions already
         }
      }


      session.removeAttribute(getProducerSessionInformationKey());
      RequestHeaderClientHandler.resetCurrentInfo();
   }

   /** @since 2.6 */
   public void releaseSessions() throws PortletInvokerException
   {
      List<String> idsToRelease = new ArrayList<String>();

      Set<ProducerSessionInformation> uniqueInfos = new HashSet<ProducerSessionInformation>(sessionInfos.values());

      for (ProducerSessionInformation info : uniqueInfos)
      {
         idsToRelease.addAll(info.removeSessions());
      }

      internalReleaseSessions(idsToRelease);
   }

   /**
    * @param sessionIds
    * @throws PortletInvokerException
    * @since 2.6
    */
   void releaseSessions(String[] sessionIds) throws PortletInvokerException
   {
      if (sessionIds != null)
      {
         List<String> idsToRelease = new ArrayList<String>();

         for (String sessionId : sessionIds)
         {
            ProducerSessionInformation info = sessionInfos.get(sessionId);
            sessionId = info.removeSession(sessionId);
            if (sessionId != null)
            {
               idsToRelease.add(sessionId);
            }
         }

         internalReleaseSessions(idsToRelease);
      }
   }

   private void internalReleaseSessions(List<String> idsToRelease) throws PortletInvokerException
   {
      if (idsToRelease != null && !idsToRelease.isEmpty())
      {
         try
         {
            consumer.getMarkupService().releaseSessions(consumer.getRegistrationContext(), idsToRelease, UserAccess.getUserContext());
         }
         catch (InvalidRegistration invalidRegistration)
         {
            log.debug("Invalid Registration");
            consumer.handleInvalidRegistrationFault();
         }
         catch (Exception e)
         {
            if (consumer.getProducerInfo().canAttemptRecoveryFrom(e))
            {
               internalReleaseSessions(idsToRelease);
            }
            else
            {
               String message = "Couldn't release sessions " + idsToRelease;
               log.debug(message, e);
               throw new PortletInvokerException(message, e);
            }
         }
      }
   }

   // ProducerSessionInformation callbacks

   /**
    * Update session mappings when a session has expired
    *
    * @param id
    * @since 2.6
    */
   void removeSessionId(String id)
   {
      sessionInfos.remove(id);
   }

   /**
    * Update session mappings when a new session id is added to the specified ProducerSessionInformation
    *
    * @param sessionID
    * @param producerSessionInformation
    * @since 2.6
    */
   void addSessionMapping(String sessionID, ProducerSessionInformation producerSessionInformation)
   {
      sessionInfos.put(sessionID, producerSessionInformation);
   }

   // End ProducerSessionInformation callbacks

   // SessionEventListener implementation

   public void onSessionEvent(SessionEvent event)
   {
      if (SessionEvent.SessionEventType.SESSION_DESTROYED.equals(event.getType()))
      {
         String id = event.getSession().getId();

         // check if the session being destroyed is the one associated with this thread
         ProducerSessionInformation info = RequestHeaderClientHandler.getCurrentProducerSessionInformation();
         if (info != null)
         {
            if (id != null && id.equals(info.getParentSessionId()))
            {
               try
               {
                  internalReleaseSessions(info.removeSessions());
               }
               catch (PortletInvokerException e)
               {
                  // already logged...
               }
               log.debug("Released session '" + id + "' after session was destroyed by Portal.");
            }
         }
      }
   }

   /**
    * @param originalHandle
    * @param newHandle
    * @param invocation
    * @since 2.6
    */
   public void updateSessionInfoFor(String originalHandle, String newHandle, PortletInvocation invocation)
   {
      ProducerSessionInformation info = getProducerSessionInformation(invocation, false);
      if (info != null)
      {
         info.updateHandleAssociatedInfo(originalHandle, newHandle);
      }
   }

   /** Cookie protocol required by the producer with the consumer */
   private CookieProtocol getRequiresInitCookie()
   {
      return consumer.getProducerInfo().getRequiresInitCookie();
   }
}
