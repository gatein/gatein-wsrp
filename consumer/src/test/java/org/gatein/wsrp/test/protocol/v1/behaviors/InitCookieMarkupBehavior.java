/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2007, Red Hat Middleware, LLC, and individual                    *
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

package org.gatein.wsrp.test.protocol.v1.behaviors;

import org.gatein.pc.api.Mode;
import org.gatein.pc.api.WindowState;
import org.gatein.wsrp.test.BehaviorRegistry;
import org.gatein.wsrp.test.protocol.v1.MarkupBehavior;
import org.oasis.wsrp.v1.AccessDenied;
import org.oasis.wsrp.v1.Extension;
import org.oasis.wsrp.v1.GetMarkup;
import org.oasis.wsrp.v1.InvalidCookie;
import org.oasis.wsrp.v1.InvalidRegistration;
import org.oasis.wsrp.v1.OperationFailed;
import org.oasis.wsrp.v1.RegistrationContext;

import javax.jws.WebParam;
import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 10610 $
 * @since 2.6
 */
public abstract class InitCookieMarkupBehavior extends MarkupBehavior
{
   protected String portletHandle;
   protected int initCookieCallCount;

   public InitCookieMarkupBehavior(BehaviorRegistry registry)
   {
      super(registry);
      initPortletHandle();
      registerHandle(portletHandle);
   }

   protected abstract void initPortletHandle();


   protected String getMarkupString(Mode mode, WindowState windowState, String navigationalState, GetMarkup getMarkup) throws OperationFailed, InvalidCookie
   {
      String handle = getMarkup.getPortletContext().getPortletHandle();

      if (portletHandle.equals(handle))
      {
         return getMarkupString(handle);
      }

      // shouldn't happen
      throw new OperationFailed();
   }

   protected String getMarkupString(String handle) throws InvalidCookie, OperationFailed
   {
      switch (callCount++)
      {
         case 0:
            // simulate change of configuration between calls: upon receiving this, the consumer should invoke initCookie
            throw new InvalidCookie();

         case 1:
            return handle;

         default:
            // shouldn't be called more than twice
            throw new OperationFailed();
      }
   }

   @Override
   public List<Extension> initCookie(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") RegistrationContext registrationContext) throws InvalidRegistration, AccessDenied, OperationFailed
   {
      initCookieCallCount++;
      return null;
   }

   public int getInitCookieCallCount()
   {
      return initCookieCallCount;
   }
}
