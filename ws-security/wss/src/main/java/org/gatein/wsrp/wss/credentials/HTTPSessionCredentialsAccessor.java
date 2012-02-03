/*
 * JBoss, a division of Red Hat
 * Copyright 2012, Red Hat Middleware, LLC, and individual
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

package org.gatein.wsrp.wss.credentials;

import org.gatein.wci.security.Credentials;
import org.gatein.wsrp.servlet.ServletAccess;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;

/**
 * Obtaining credentials from HTTP session.
 *
 * @author <a href="mailto:mposolda@redhat.com">Marek Posolda</a>
 */
public class HTTPSessionCredentialsAccessor implements CredentialsAccessor
{

   private static Logger log = LoggerFactory.getLogger(HTTPSessionCredentialsAccessor.class);

   /**
    * Return credentials from HTTP session. It assumes that Credentials are in HTTP session in attribute
    * {@link org.gatein.wci.security.Credentials#CREDENTIALS} and current HTTP request is bound to {@link
    * org.gatein.wsrp.servlet.ServletAccess} thread-local.
    *
    * @return credentials
    */
   @Override
   public Credentials getCredentials()
   {
      HttpServletRequest request = ServletAccess.getRequest();
      if (request != null && request.getSession() != null)
      {
         return (Credentials)request.getSession().getAttribute(Credentials.CREDENTIALS);
      }
      else
      {
         log.debug("Could not get current HttpServletRequest, cannot obtain credentials from HTTP session.");
         return null;
      }
   }
}
