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

package org.gatein.wsrp.test.protocol.v2.behaviors;

import org.gatein.pc.api.Mode;
import org.gatein.pc.api.WindowState;
import org.gatein.wsrp.spec.v2.WSRP2ExceptionFactory;
import org.gatein.wsrp.test.protocol.v2.BehaviorRegistry;
import org.oasis.wsrp.v2.AccessDenied;
import org.oasis.wsrp.v2.Extension;
import org.oasis.wsrp.v2.GetMarkup;
import org.oasis.wsrp.v2.InvalidCookie;
import org.oasis.wsrp.v2.InvalidRegistration;
import org.oasis.wsrp.v2.ModifyRegistrationRequired;
import org.oasis.wsrp.v2.OperationFailed;
import org.oasis.wsrp.v2.OperationNotSupported;
import org.oasis.wsrp.v2.RegistrationContext;
import org.oasis.wsrp.v2.ResourceSuspended;
import org.oasis.wsrp.v2.UserContext;

import javax.jws.WebParam;
import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class InitCookieNotRequiredMarkupBehavior extends InitCookieMarkupBehavior
{
   public static final String INIT_COOKIE_NOT_REQUIRED_HANDLE = "InitCookieNotRequired";

   public InitCookieNotRequiredMarkupBehavior(BehaviorRegistry registry)
   {
      super(registry);
   }

   protected void initPortletHandle()
   {
      portletHandle = INIT_COOKIE_NOT_REQUIRED_HANDLE;
   }

   @Override
   protected String getMarkupString(Mode mode, WindowState windowState, String navigationalState, GetMarkup getMarkup) throws OperationFailed, InvalidCookie
   {
      return portletHandle;
   }

   @Override
   public List<Extension> initCookie(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext) throws AccessDenied, InvalidRegistration, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      super.initCookie(registrationContext, userContext);
      throw WSRP2ExceptionFactory.throwWSException(OperationFailed.class, "Shouldn't be called", null);
   }
}
