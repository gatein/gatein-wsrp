/*
* JBoss, a division of Red Hat
* Copyright 2012, Red Hat Middleware, LLC, and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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

/** @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a> */
public class MIMEUtils
{
   public static final String UTF_8 = "UTF-8";
   public static final String CHARSET = "charset=";
   public static final int CHARSET_LENGTH = CHARSET.length();
   public static final String JAVASCRIPT = "javascript";
   public static final String TEXT = "text";
   public static final String ECMASCRIPT = "ecmascript";
   public static final String CSS = "css";
   public static final String HTML = "html";
   public static final String XML = "xml";
   public static final String APPLICATION_XML = "application/xml";

   /**
    * TODO: handle this better, we should probably have a class in the common module to determine if the MediaType
    * should be treated as a text file or as binary content. We also need to implement the algorithm to determine the
    * character encoding. See GTNCOMMON-14
    *
    * @param contentType
    * @return
    */
   public static boolean isInterpretableAsText(String contentType)
   {
      return contentType != null && (contentType.startsWith(TEXT) || isJavascript(contentType) || isApplicationXML(contentType));
   }

   public static boolean needsRewriting(String contentType)
   {
      return contentType != null && (
         // content is css, html or xml
         (contentType.startsWith(TEXT) && (contentType.contains(CSS) || contentType.contains(HTML) || contentType.contains(XML)))
            // or javascript
            || isJavascript(contentType)
            // or application/xml
            || isApplicationXML(contentType)
      );
   }

   private static boolean isApplicationXML(String contentType)
   {
      return contentType.startsWith(APPLICATION_XML);
   }

   private static boolean isJavascript(String contentType)
   {
      return contentType.contains(JAVASCRIPT) || contentType.contains(ECMASCRIPT);
   }

   public static String getCharsetFrom(String contentType)
   {
      String charset = UTF_8;
      if (contentType != null)
      {
         for (String part : contentType.split(";"))
         {
            if (part.startsWith(CHARSET))
            {
               charset = part.substring(CHARSET_LENGTH);
            }
         }
      }
      return charset;
   }
}
