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

package org.gatein.wsrp.spec.v2;

import javax.xml.namespace.QName;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public final class ErrorCodes
{
   public static final String WSRP2_TYPES_NS = "urn:oasis:names:tc:wsrp:v2:types";
   public static final QName AccessDenied = new QName(WSRP2_TYPES_NS, "AccessDenied");
   public static final QName ExportNoLongerValid = new QName(WSRP2_TYPES_NS, "ExportNoLongerValid");
   public static final QName InconsistentParameters = new QName(WSRP2_TYPES_NS, "InconsistentParameters");
   public static final QName InvalidRegistration = new QName(WSRP2_TYPES_NS, "InvalidRegistration");
   public static final QName InvalidCookie = new QName(WSRP2_TYPES_NS, "InvalidCookie");
   public static final QName InvalidHandle = new QName(WSRP2_TYPES_NS, "InvalidHandle");
   public static final QName InvalidSession = new QName(WSRP2_TYPES_NS, "InvalidSession");
   public static final QName InvalidUserCategory = new QName(WSRP2_TYPES_NS, "InvalidUserCategory");
   public static final QName ModifyRegistrationRequired = new QName(WSRP2_TYPES_NS, "ModifyRegistrationRequired");
   public static final QName MissingParameters = new QName(WSRP2_TYPES_NS, "MissingParameters");
   public static final QName OperationFailed = new QName(WSRP2_TYPES_NS, "OperationFailed");
   public static final QName OperationNotSupported = new QName(WSRP2_TYPES_NS, "OperationNotSupported");
   public static final QName ResourceSuspended = new QName(WSRP2_TYPES_NS, "ResourceSuspended");
   public static final QName TooBusy = new QName(WSRP2_TYPES_NS, "TooBusy");
   public static final QName TooManyRequests = new QName(WSRP2_TYPES_NS, "TooManyRequests");
}
