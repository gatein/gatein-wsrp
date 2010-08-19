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

package org.gatein.wsrp.test.protocol.v1.behaviors;

import org.gatein.wsrp.handler.RequestHeaderClientHandler;
import org.gatein.wsrp.test.handler.MockSOAPMessage;
import org.gatein.wsrp.test.handler.MockSOAPMessageContext;
import org.gatein.wsrp.test.protocol.v1.BehaviorRegistry;
import org.oasis.wsrp.v1.V1AccessDenied;
import org.oasis.wsrp.v1.V1Extension;
import org.oasis.wsrp.v1.V1InvalidRegistration;
import org.oasis.wsrp.v1.V1OperationFailed;
import org.oasis.wsrp.v1.V1RegistrationContext;

import javax.xml.soap.MimeHeaders;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class PerUserInitCookieMarkupBehavior extends InitCookieMarkupBehavior
{
   public static final String PER_USER_INIT_COOKIE_HANDLE = "PerUserInitCookie";

   public PerUserInitCookieMarkupBehavior(BehaviorRegistry registry)
   {
      super(registry);
   }

   protected void initPortletHandle()
   {
      portletHandle = PER_USER_INIT_COOKIE_HANDLE;
   }

   @Override
   public List<V1Extension> initCookie(V1RegistrationContext registrationContext) throws V1InvalidRegistration,
      V1AccessDenied, V1OperationFailed
   {
      //Set the Cookie through the RequestHeaderClientHandler manually here since we use a test BehaviourBackedServiceFactory instead
      //of the real SOAPServiceFactory.
      MockSOAPMessage message = new MockSOAPMessage();
      SOAPMessageContext msgContext = MockSOAPMessageContext.createMessageContext(message, getClass().getClassLoader());
      MimeHeaders headers = new MimeHeaders();
      headers.setHeader("Set-Cookie", "name=value");
      message.setMimeHeaders(headers);

      RequestHeaderClientHandler rhch = new RequestHeaderClientHandler();
      rhch.handleResponse(msgContext);

      return super.initCookie(registrationContext);
   }
}
