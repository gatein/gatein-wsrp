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

package org.gatein.wsrp.servlet;

import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.api.servlet.ServletAccess;
import org.oasis.wsrp.v2.UserContext;

import javax.servlet.http.HttpServletRequest;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 9360 $
 * @since 2.6
 */
public class UserAccess
{
   private UserAccess()
   {
   }

   public static String getUser()
   {
      HttpServletRequest req = ServletAccess.getRequest();
      return req != null ? req.getRemoteUser() : null;
   }

   public static UserContext getUserContext()
   {
      String userId = getUser();
      return userId != null ? WSRPTypeFactory.createUserContext(userId) : null;
   }
}