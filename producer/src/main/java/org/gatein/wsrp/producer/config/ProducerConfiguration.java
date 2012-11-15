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

package org.gatein.wsrp.producer.config;

import org.oasis.wsrp.v2.CookieProtocol;

import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 12017 $
 * @since 2.6
 */
public interface ProducerConfiguration
{
   ProducerRegistrationRequirements getRegistrationRequirements();

   /**
    * Is the associated producer using strict WSRP data validation? Strict validation means that even minor
    * non-compliance will cause a failure. Lenient validation relaxes the checks that are performed to improve
    * compatibility with some non-completely compliant consumers.
    *
    * @return <code>true</code> if strict validation is in effect, <code>false</code> otherwise.
    */
   boolean isUsingStrictMode();

   void setUsingStrictMode(boolean strict);

   void addChangeListener(ProducerConfigurationChangeListener listener);

   void removeChangeListener(ProducerConfigurationChangeListener listener);

   List<ProducerConfigurationChangeListener> getChangeListeners();

   /** The default session expiration time in milliseconds. */
   int DEFAULT_SESSION_EXPIRATION_TIME = 300000;

   /** The value used to specify that a session will never expire. */
   int INFINITE_SESSION_EXPIRATION_TIME = -1;

   /**
    * Indicates whether or not the Producer requires the Consumer to assist with cookie support of the HTTP protocol.
    * Supported values and semantics: <ul> <li>{@link org.oasis.wsrp.v2.CookieProtocol#NONE}: The Producer does not need
    * the Consumer to ever invoke {@link org.oasis.wsrp.v2.WSRPV2MarkupPortType#initCookie}.</li> <li>{@link
    * org.oasis.wsrp.v2.CookieProtocol#PER_USER}: The Consumer MUST invoke {@link org.oasis.wsrp.v2.WSRPV2MarkupPortType#initCookie}
    * once per user of the Consumer, and associate any returned cookies with subsequent invocations on behalf of that
    * user.</li> <li>{@link org.oasis.wsrp.v2.CookieProtocol#PER_GROUP}: The Consumer MUST invoke {@link
    * org.oasis.wsrp.v2.WSRPV2MarkupPortType#initCookie} once per unique groupID from the PortletDescriptions for the
    * Portlets it is aggregating on a page for each user of the Consumer, and associate any returned cookies with
    * subsequent invocations on behalf of that user targeting Portlets with identical groupIDs.</li> </ul>
    *
    * @return the level of cookie support required from the Consumer
    */
   CookieProtocol getRequiresInitCookie();

   /**
    * Sets the level of cookie support required by the Consumer.
    *
    * @param requiresInitCookie either {@link CookieProtocol#NONE}, {@link CookieProtocol#PER_USER} or {@link
    *                           CookieProtocol#PER_GROUP}
    */
   void setRequiresInitCookie(CookieProtocol requiresInitCookie);

   /**
    * Maximum number of seconds between invocations referencing a session ID before this Producer will schedule
    * releasing the related resources. {@link #INFINITE_SESSION_EXPIRATION_TIME} indicates that the sessionID will never
    * expire.
    *
    * @return the expiration time (in seconds) of session associated resources
    */
   int getSessionExpirationTime();

   /**
    * Sets the expiration time (in seconds) of session associated resources.
    *
    * @param sessionExpirationTime the maximum number of seconds between invocations referencing a session ID before
    *                              this Producer will schedule releasing the related resources. If {@link
    *                              #INFINITE_SESSION_EXPIRATION_TIME} is passed, then the session will never expire.
    */
   void setSessionExpirationTime(int sessionExpirationTime);

   void setRegistrationRequirements(ProducerRegistrationRequirements requirements);

   long getLastModified();

   void setLastModified(long lastModified);
}
