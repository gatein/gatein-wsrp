/*
 * JBoss, a division of Red Hat
 * Copyright 2011, Red Hat Middleware, LLC, and individual
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
import org.gatein.wsrp.WSRPConstants;
import org.gatein.wsrp.handler.CookieUtil;
import org.oasis.wsrp.v2.SessionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.net.HttpCookie;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Records session and cookie information for a producer.
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 12736 $
 * @since 2.4 (May 30, 2006)
 */
public class ProducerSessionInformation implements Serializable
{
   private static Logger log = LoggerFactory.getLogger(ProducerSessionInformation.class);

   private boolean initCookieDone = false;
   private boolean perGroupCookies = false;

   /** group id -> List<HttpCookie></HttpCookie> */
   private Map<String, List<HttpCookie>> groupCookies;

   /** portlet handle -> SessionInfo */
   private Map<String, SessionInfo> portletSessions;

   /** session id -> portlet handle */
   private Map<String, String> sessionId2PortletHandle;

   /** Cookies sent by the remote producer */
   private List<HttpCookie> userCookie;

   /** Parent SessionHandler so that session mappings can be updated */
   private transient SessionHandler parent;

   /** The identifier of the Session containing this ProducerSessionInformation */
   private String parentSessionId;

   String getParentSessionId()
   {
      return parentSessionId;
   }

   /**
    * @throws IllegalStateException if an attempt is made to set the parent session id to a different one when it has
    *                               already been set.
    * @since 2.6
    */
   void setParentSessionId(String parentSessionId)
   {
      boolean error = false;

      if (parentSessionId != null)
      {
         parentSessionId = SessionHandler.getRealId(parentSessionId);

         if (this.parentSessionId != null && !parentSessionId.equals(this.parentSessionId))
         {
            error = true;
         }
      }
      else
      {
         if (this.parentSessionId != null)
         {
            error = true;
         }
      }

      if (error)
      {
         throw new IllegalStateException("Cannot modify Parent Session id once it has been set!");
      }

      this.parentSessionId = parentSessionId;
   }

   public List<HttpCookie> getUserCookies()
   {
      userCookie = CookieUtil.purgeExpiredCookies(userCookie);
      if (userCookie.isEmpty())
      {
         setInitCookieDone(false);
      }
      return userCookie;
   }

   public void setUserCookies(List<HttpCookie> userCookie)
   {
      if (!ParameterValidation.existsAndIsNotEmpty(userCookie))
      {
         throw new IllegalArgumentException("Must provide a non-null, non-empty cookie list.");
      }

      this.userCookie = userCookie;
   }

   public boolean isInitCookieDone()
   {
      return initCookieDone;
   }

   public void setInitCookieDone(boolean initCookieDone)
   {
      this.initCookieDone = initCookieDone;
   }

   public boolean isPerGroupCookies()
   {
      return perGroupCookies;
   }

   public void setPerGroupCookies(boolean perGroupCookies)
   {
      this.perGroupCookies = perGroupCookies;
   }

   public void setGroupCookiesFor(String groupId, List<HttpCookie> cookies)
   {
      if (!isPerGroupCookies())
      {
         throw new IllegalStateException("Cannot add group cookie when cookie protocol is perUser.");
      }

      if (groupId == null)
      {
         throw new IllegalArgumentException("Cannot set cookie for a null portlet group id!");
      }

      if (!ParameterValidation.existsAndIsNotEmpty(cookies))
      {
         throw new IllegalArgumentException("Must provide a non-null, non-empty group cookie list.");
      }

      if (groupCookies == null)
      {
         groupCookies = new HashMap<String, List<HttpCookie>>();
      }

      if (groupCookies.containsKey(groupId))
      {
         log.debug("Trying to set a cookie for an existing group: " + groupId);
      }

      groupCookies.put(groupId, cookies);
   }

   public List<HttpCookie> getGroupCookiesFor(String groupId)
   {
      if (groupCookies == null)
      {
         return Collections.emptyList();
      }

      // purge expired cookies
      List<HttpCookie> cookies = groupCookies.get(groupId);
      if (cookies != null)
      {
         cookies = CookieUtil.purgeExpiredCookies(cookies);

         // if there are no non-expired cookies left, we will need to re-init them
         if (cookies.isEmpty())
         {
            setInitCookieDone(false);
         }

         // update cookies for the considered group id
         groupCookies.put(groupId, cookies);

         return cookies;
      }
      else
      {
         return Collections.emptyList();
      }
   }

   public void clearGroupCookies()
   {
      groupCookies = null;
   }

   public void addSessionForPortlet(String portletHandle, SessionContext sessionContext)
   {
      // sessionContext is validated in SessionInfo constructor
      SessionInfo info = new SessionInfo(sessionContext, portletHandle);

      if (portletSessions == null)
      {
         portletSessions = new HashMap<String, SessionInfo>();
         sessionId2PortletHandle = new HashMap<String, String>();
      }

      portletSessions.put(portletHandle, info);
      sessionId2PortletHandle.put(sessionContext.getSessionID(), portletHandle);

      if (parent != null)
      {
         parent.addSessionMapping(sessionContext.getSessionID(), this);
      }
   }

   /**
    * Retrieves the session id for the portlet with the specified handle. Note that this will "touch" the session,
    * hence resetting the time since the last use of the session.
    *
    * @param portletHandle the handle of the portlet for which the session id is to be retrieved
    * @return the session id for the specified portlet, <code>null</code> if there is no session associated with the
    *         portlet or if the session has expired.
    */
   public String getSessionIdForPortlet(String portletHandle)
   {
      ProducerSessionInformation.SessionIdResult idResult = internalGetSessionIdForPortlet(portletHandle);
      if (idResult.expired)
      {
         return null;
      }

      return idResult.id;
   }

   public int getNumberOfSessions()
   {
      if (portletSessions != null)
      {
         return portletSessions.size();
      }
      else
      {
         return 0;
      }
   }

   /**
    * @param sessionId
    * @return the id of the removed session or <code>null</code> if the session had already expired
    */
   public String removeSession(String sessionId)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(sessionId, "session id");

      String portletHandle = sessionId2PortletHandle.get(sessionId);
      if (portletHandle == null)
      {
         throw new IllegalArgumentException("No such session id: '" + sessionId + "'");
      }

      return removeSessionForPortlet(portletHandle);
   }

   /**
    * @return a list containing the session ids that were still valid when they were removed and would need to be
    *         released
    */
   public List<String> removeSessions()
   {
      List<String> idsToRelease = new ArrayList<String>(getNumberOfSessions());

      // copy to avoid ConcurrentModificationException
      List<String> handlesCopy = new ArrayList<String>(portletSessions.keySet());

      for (String handle : handlesCopy)
      {
         SessionIdResult result = removeSessionIdForPortlet(handle);

         // only release sessions that are still valid
         if (!result.expired)
         {
            idsToRelease.add(result.id);
         }
      }

      return idsToRelease;
   }

   /**
    * @param portletHandle
    * @return the id of the removed session or <code>null</code> if the session had already expired
    */
   public String removeSessionForPortlet(String portletHandle)
   {
      SessionIdResult result = removeSessionIdForPortlet(portletHandle);

      return result.expired ? null : result.id;
   }

   private SessionIdResult removeSessionIdForPortlet(String portletHandle)
   {
      ProducerSessionInformation.SessionIdResult result = internalGetSessionIdForPortlet(portletHandle);
      final String id = result.id;

      if (id == null)
      {
         throw new IllegalArgumentException("There is no Session associated with portlet '" + portletHandle + "'");
      }

      // if the session is still valid, release it and remove the associated mappings
      if (!result.expired)
      {
         portletSessions.remove(portletHandle);
         sessionId2PortletHandle.remove(id);
         if (parent != null)
         {
            parent.removeSessionId(id);
         }
      }

      return result;
   }

   public void replaceUserCookiesWith(ProducerSessionInformation currentSessionInfo)
   {
      if (currentSessionInfo != null && currentSessionInfo.userCookie != null && !currentSessionInfo.userCookie.isEmpty())
      {
         this.userCookie = currentSessionInfo.userCookie;
      }
   }

   private SessionIdResult internalGetSessionIdForPortlet(String portletHandle)
   {
      SessionInfo session = getSessionInfoFor(portletHandle);
      if (session != null)
      {
         String id = session.getSessionId();
         if (!session.isStillValid())
         {
            portletSessions.remove(session.getPortletHandle());
            sessionId2PortletHandle.remove(session.getSessionId());
            if (parent != null)
            {
               parent.removeSessionId(session.getSessionId());
            }
            return new SessionIdResult(id, true);
         }
         else
         {
            return new SessionIdResult(id, false);
         }
      }
      return new SessionIdResult(null, false);
   }

   private SessionInfo getSessionInfoFor(String portletHandle)
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(portletHandle, "portlet handle", null);

      if (portletSessions == null)
      {
         return null;
      }

      return portletSessions.get(portletHandle);
   }

   /**
    * @return the known session id
    * @since 2.6
    */
   Collection<String> getSessionIds()
   {
      return sessionId2PortletHandle.keySet();
   }

   /**
    * @param sessionHandler
    * @since 2.6
    */
   void setParent(SessionHandler sessionHandler)
   {
      parent = sessionHandler;
   }

   /**
    * Update the mappings that were associated with the specified original portlet handle after it has been modified as
    * a result of a clone operation to the specified new handle.
    *
    * @param originalHandle
    * @param newHandle
    * @since 2.6
    */
   public void updateHandleAssociatedInfo(String originalHandle, String newHandle)
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(originalHandle, "original handle",
         "Updating information associated with a portlet handle");
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(newHandle, "new handle",
         "Updating information associated with a portlet handle");

      String sessionId = getSessionIdForPortlet(originalHandle);
      ProducerSessionInformation.SessionInfo info = getSessionInfoFor(originalHandle);
      if (sessionId != null && info != null)
      {
         portletSessions.put(newHandle, info);
         portletSessions.remove(originalHandle);
         sessionId2PortletHandle.put(sessionId, newHandle);
         log.debug("Updated mapping information for '" + originalHandle + "' to reference '" + newHandle + "' instead.");
      }
   }

   private class SessionInfo implements Serializable
   {
      private long lastInvocationTime;
      private final String portletHandle;
      private final String sessionID;
      private final int expires;

      public SessionInfo(SessionContext sessionContext, String portletHandle)
      {
         ParameterValidation.throwIllegalArgExceptionIfNull(sessionContext, "SessionContext");
         ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(sessionContext.getSessionID(), "session id", "SessionContext");
         ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(portletHandle, "portlet handle", "SessionInfo");

         this.sessionID = sessionContext.getSessionID();
         this.expires = sessionContext.getExpires();
         this.portletHandle = portletHandle;
         lastInvocationTime = now();
      }

      /**
       * Checks that the session associated with the session context hasn't expired and update the last invocation time
       *
       * @return
       */
      private boolean isStillValid()
      {
         if (expires == WSRPConstants.SESSION_NEVER_EXPIRES)
         {
            return true;
         }

         long now = now();
         long secondsSinceLastInvocation = (now - lastInvocationTime) / 1000;
         lastInvocationTime = now;

         long diff = expires - secondsSinceLastInvocation;
         log.debug("Session ID '" + sessionID + "' is " + ((diff > 0) ? "" : "not")
            + " valid (time since last invocation: " + diff + ")");
         return diff > 0;
      }

      private String getSessionId()
      {
         return sessionID;
      }

      private String getPortletHandle()
      {
         return portletHandle;
      }
   }

   private static long now()
   {
      return System.currentTimeMillis();
   }

   private class SessionIdResult
   {
      private String id;
      private boolean expired;

      public SessionIdResult(String id, boolean expired)
      {
         this.id = id;
         this.expired = expired;
      }
   }
}
