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
package org.gatein.wsrp.servlet;

import org.gatein.common.io.IOTools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Map;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 9360 $
 */
public class RequestDumperFilter implements Filter
{

   /** . */
   private static Logger log = LoggerFactory.getLogger(RequestDumperFilter.class);

   public void init(FilterConfig cfg) throws ServletException
   {
   }

   public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException
   {
      doFilter((HttpServletRequest)req, (HttpServletResponse)resp, chain);
   }

   public void doFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain chain) throws IOException, ServletException
   {
      boolean trace = log.isTraceEnabled();
      if (trace)
      {
         StringBuffer tmp = new StringBuffer();

         //
         tmp.append("characterEncoding=").append(req.getCharacterEncoding()).append('\n');
         tmp.append("contentLength=").append(req.getContentLength()).append('\n');
         tmp.append("contentType=").append(req.getContentType()).append('\n');
         tmp.append("method=").append(req.getMethod()).append('\n');
         tmp.append("pathInfo=").append(req.getPathInfo()).append('\n');
         tmp.append("queryString=").append(req.getQueryString()).append('\n');
         tmp.append("requestURI=").append(req.getRequestURI()).append('\n');
         tmp.append("servletPath=").append(req.getServletPath()).append('\n');

         //
         for (Object o : req.getParameterMap().entrySet())
         {
            Map.Entry entry = (Map.Entry)o;
            String name = (String)entry.getKey();
            String[] values = (String[])entry.getValue();
            tmp.append("param.").append(name).append('=');
            for (int j = 0; j < values.length; j++)
            {
               String value = values[j];
               tmp.append(j == 0 ? "" : ",").append(value);
            }
         }

         //
         Reader reader = req.getReader();
         if (reader != null)
         {
            StringWriter buffer = new StringWriter();
            IOTools.copy(reader, buffer);
            tmp.append("body=").append(buffer.toString());
         }

         log.trace(tmp.toString());
      }
      chain.doFilter(req, resp);
   }

   public void destroy()
   {
   }
}
