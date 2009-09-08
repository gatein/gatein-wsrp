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

package org.gatein.wsrp.producer;

import org.gatein.common.net.URLTools;
import org.gatein.wsrp.WSRPRewritingConstants;

import javax.servlet.http.HttpServletRequest;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class AbsoluteURLReplacementGenerator extends URLTools.URLReplacementGenerator
{
   private String serverAddress;
   public static final String SCH_END = "://";
   public static final String PORT_END = ":";
   public static final String SLASH = "/";

   public AbsoluteURLReplacementGenerator(HttpServletRequest request)
   {
      String scheme = request.getScheme();
      String host = request.getServerName();
      int port = request.getServerPort();

      serverAddress = scheme + SCH_END + host + PORT_END + port;
   }

   public String getReplacementFor(int i, URLTools.URLMatch urlMatch)
   {
      return getAbsoluteURLFor(urlMatch.getURLAsString());
   }

   /**
    * todo: public only for tests
    *
    * @param url
    * @return
    */
   public String getAbsoluteURLFor(String url)
   {
      return getAbsoluteURLFor(url, true);
   }

   String getAbsoluteURLFor(String url, boolean checkWSRPToken)
   {
      // We don't encode URL through this API when it is a wsrp URL
      if (checkWSRPToken && url.startsWith(WSRPRewritingConstants.BEGIN_WSRP_REWRITE))
      {
         return url;
      }

      if (!URLTools.isNetworkURL(url) && url.startsWith(SLASH))
      {
         return serverAddress + url;
      }
      else
      {
         return url;
      }
   }
}
