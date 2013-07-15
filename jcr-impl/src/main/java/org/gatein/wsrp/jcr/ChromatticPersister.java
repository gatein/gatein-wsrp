/*
 * JBoss, a division of Red Hat
 * Copyright 2011, Red Hat Middleware, LLC, and individual
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

package org.gatein.wsrp.jcr;

import org.chromattic.api.ChromatticSession;
import org.chromattic.api.format.FormatterContext;
import org.chromattic.api.format.ObjectFormatter;

/**
 * Encapsulates Chromattic to make it a little simpler to use.
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public interface ChromatticPersister
{
   /**
    * Retrieves the current ChromatticSession or open a new one if none already existed.
    *
    * @return the current ChromatticSession or a new one if none already existed
    */
   ChromatticSession getSession();

   /**
    * Closes the current ChromatticSession, saving the associated operations if so specified
    *
    * @param save whether or not to save the operations associated with the session
    */
   void closeSession(boolean save);

   /** Saves the modifications made in the context of the current session but keep the session open. */
   void save();

   /**
    * Deletes the specified object from the specified manager, the manager being, most of the time, the parent of the object to be deleted.
    *
    * @param toDelete the object to delete
    * @param manager the StoresByPathManager object that needs to be notified (to be updated) of the deletion
    * @param <T> the type of the object to delete
    * @return the object that was deleted or <code>null</code> if no such object existed in persistence
    */
   <T> boolean delete(T toDelete, StoresByPathManager<T> manager);

   /**
    * Checks whether the current session is closed.
    * @return <code>true</code> if no current session exists or if it's closed, <code>false</code> otherwise
    */
   boolean isSessionClosed();

   /**
    * Encodes and decodes QName to be properly persisted in JCR since ':', '{' and '}' are reserved chars.
    */
   class QNameFormatter implements ObjectFormatter
   {
      private static final String OPEN_BRACE_REPLACEMENT = "-__";
      private static final String CLOSE_BRACE_REPLACEMENT = "__-";
      private static final String COLON_REPLACEMENT = "_-_";
      private static final String CLOSE_BRACE = "}";
      private static final String OPEN_BRACE = "{";
      private static final String COLON = ":";

      public String decodeNodeName(FormatterContext formatterContext, String s)
      {
         return decode(s);
      }

      public String encodeNodeName(FormatterContext formatterContext, String s)
      {
         return encode(s);
      }

      public String decodePropertyName(FormatterContext formatterContext, String s)
      {
         return decode(s);
      }

      public String encodePropertyName(FormatterContext formatterContext, String s)
      {
         return encode(s);
      }

      public static String decode(String s)
      {
         return s.replace(CLOSE_BRACE_REPLACEMENT, CLOSE_BRACE).replace(OPEN_BRACE_REPLACEMENT, OPEN_BRACE).replace(COLON_REPLACEMENT, COLON);
      }

      public static String encode(String s)
      {
         return s.replace(OPEN_BRACE, OPEN_BRACE_REPLACEMENT).replace(CLOSE_BRACE, CLOSE_BRACE_REPLACEMENT).replace(COLON, COLON_REPLACEMENT);
      }
   }

   /**
    * Encodes and decodes portlet contexts to be properly persisted in JCR as '/' is a reserved char.
    */
   class PortletNameFormatter implements ObjectFormatter
   {
      public static final String SLASH_REPLACEMENT = "-_-";
      private static final String SLASH = "/";

      public String decodeNodeName(FormatterContext formatterContext, String s)
      {
         return decode(s);
      }

      public static String decode(String s)
      {
         return s.replace(SLASH_REPLACEMENT, SLASH);
      }

      public String encodeNodeName(FormatterContext formatterContext, String s) throws IllegalArgumentException, NullPointerException
      {
         return encode(s);
      }

      public static String encode(String s)
      {
         return s.replace(SLASH, SLASH_REPLACEMENT);
      }
   }
}
