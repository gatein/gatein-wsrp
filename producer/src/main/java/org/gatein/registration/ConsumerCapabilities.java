/*
 * JBoss, a division of Red Hat
 * Copyright 2009, Red Hat Middleware, LLC, and individual
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

package org.gatein.registration;

import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 8784 $
 * @since 2.6
 */
public interface ConsumerCapabilities
{
   boolean supportsGetMethod();

   /**
    * Returns the list of supported {@link org.jboss.portal.Mode}s
    *
    * @return
    */
   List getSupportedModes();

   /**
    * Returns the list of supported {@link org.jboss.portal.WindowState}s
    *
    * @return
    */
   List getSupportedWindowStates();

   /**
    * Returns the list of user scopes (e.g. "wsrp:perUser") the associated Consumer is willing to process. See WSRP 1.0
    * 7.1.1 for more details.
    *
    * @return
    */
   List getSupportedUserScopes();

   /**
    * Returns the list of names of UserProfile extensions the associated Consumer supports. See WSRP 1.0 7.1.1 for more
    * details.
    *
    * @return
    */
   List getSupportedUserProfileData();

   void setSupportsGetMethod(boolean supportsGetMethod);

   void setSupportedModes(List supportedModes);

   void setSupportedWindowStates(List supportedWindowStates);

   void setSupportedUserScopes(List supportedUserScopes);

   void setSupportedUserProfileData(List supportedUserProfileData);
}
