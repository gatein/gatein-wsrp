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

package org.gatein.wsrp;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public final class WSRP2RewritingConstants
{
   private WSRP2RewritingConstants()
   {
   }

   /**
    * 9.2.1.1.3.2 wsrp-resourceID This parameter provides the resourceID parameter which the Consumer MUST supply when
    * invoking the getResource operation. The presence of this parameter informs the Consumer that the getResource
    * operation is a viable means of fetching the resource requested by the Portlet's markup.
    */
   public static final String RESOURCE_ID = "wsrp-resourceID";

   /**
    * 9.2.1.1.3.3 wsrp-preferOperation When this optional parameter (default value is "false") has a value of "true",
    * the Portlet is indicating a preference for the Consumer to use the getResource operation to fetch the resource. If
    * the resource URL specifies both the wsrp-url and the wsrp-resourceID parameters, the Consumer can use either the
    * http proxy technique introduced in WSRP v1.0 or the getResource operation, but is encouraged to follow the
    * guidance provided by this url parameter.
    */
   public static final String RESOURCE_PREFER_OPERATION = "wsrp-preferOperation";

   /**
    * 9.2.1.1.3.4 wsrp-resourceState The value of this portlet URL parameter defines the state which the Consumer MUST
    * send in the resourceState field of the ResourceParams structure when the URL is activated. If this parameter is
    * missing, the Consumer MUST NOT supply a value in the resourceState field of the ResourceParams structure.
    */
   public static final String RESOURCE_STATE = "wsrp-resourceState";

   /** 9.2.1.1.3.6 wsrp-resourceCacheability */
   public static final String RESOURCE_CACHEABILITY = "wsrp-resourceCacheability";

   /** 9.2.1.3 wsrp-navigationalValues */
   public static final String NAVIGATIONAL_VALUES = "wsrp-navigationalValues";
}
