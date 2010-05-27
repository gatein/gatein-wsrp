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

package org.gatein.wsrp.test.protocol.v1;

/**
 * Exposes WSPRProducer test implementation methods - we inject what it returns
 *
 * @author <a href="mailto:boleslaw.dawidowicz@jboss.org">Boleslaw Dawidowicz</a>
 * @version $Revision: 10610 $
 */
public interface TestWSRPProducer
{
   /** Resets any currently held state. */
   void reset();

   BehaviorRegistry getBehaviorRegistry();

   /**
    * Sets the currently used portlet handle identifying the MarkupBehavior we're using. This is needed to be able to
    * dispatch calls to initCookie to the proper behavior.
    *
    * @param handle
    */
   void setCurrentMarkupBehaviorHandle(String handle);

   void setRequiresInitCookie(String cookieProtocolAsString);

   void usingStrictModeChangedTo(boolean strictMode);
}
