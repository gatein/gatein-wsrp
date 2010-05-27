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

import org.gatein.pc.api.Mode;
import org.gatein.pc.api.WindowState;
import org.gatein.wsrp.test.protocol.v1.BehaviorRegistry;
import org.gatein.wsrp.test.protocol.v1.MarkupBehavior;
import org.oasis.wsrp.v1.V1AccessDenied;
import org.oasis.wsrp.v1.V1GetMarkup;
import org.oasis.wsrp.v1.V1InconsistentParameters;
import org.oasis.wsrp.v1.V1InvalidCookie;
import org.oasis.wsrp.v1.V1InvalidHandle;
import org.oasis.wsrp.v1.V1InvalidRegistration;
import org.oasis.wsrp.v1.V1InvalidSession;
import org.oasis.wsrp.v1.V1InvalidUserCategory;
import org.oasis.wsrp.v1.V1MissingParameters;
import org.oasis.wsrp.v1.V1OperationFailed;
import org.oasis.wsrp.v1.V1UnsupportedLocale;
import org.oasis.wsrp.v1.V1UnsupportedMimeType;
import org.oasis.wsrp.v1.V1UnsupportedMode;
import org.oasis.wsrp.v1.V1UnsupportedWindowState;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 8784 $
 * @since 2.6
 */
public class ResourceMarkupBehavior extends MarkupBehavior
{
   public static final String PORTLET_HANDLE = "resource";


   public ResourceMarkupBehavior(BehaviorRegistry registry)
   {
      super(registry);
      registerHandle(PORTLET_HANDLE);
   }

   @Override
   protected String getMarkupString(Mode mode, WindowState windowState, String navigationalState, V1GetMarkup getMarkup) throws V1UnsupportedWindowState, V1InvalidCookie, V1InvalidSession, V1AccessDenied, V1InconsistentParameters, V1InvalidHandle, V1UnsupportedLocale, V1UnsupportedMode, V1OperationFailed, V1MissingParameters, V1InvalidUserCategory, V1InvalidRegistration, V1UnsupportedMimeType
   {
      return "<img src='wsrp_rewrite?wsrp-urlType=resource&amp;wsrp-url=http%3A%2F%2Flocalhost%3A8080" +
         "%2Ftest-resource-portlet%2Fgif%2Flogo.gif&amp;wsrp-requiresRewrite=true/wsrp_rewrite'/>";
   }
}
