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

package org.gatein.wsrp.consumer;

import org.gatein.common.util.ParameterValidation;
import org.gatein.pc.api.URLFormat;
import org.gatein.pc.api.spi.PortletInvocationContext;
import org.gatein.wsrp.WSRPPortletURL;
import org.gatein.wsrp.WSRPResourceURL;
import org.gatein.wsrp.WSRPRewritingConstants;

import java.util.Set;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class MarkupProcessor
{
   private final PortletInvocationContext context;
   private final URLFormat format;
   private final Set<String> supportedCustomModes;
   private final Set<String> supportedCustomWindowStates;
   private final String serverAddress;
   private final String portletApplicationName;
   private final String namespace;
   public static final int URL_DELIMITER_LENGTH = WSRPRewritingConstants.RESOURCE_URL_DELIMITER.length();

   protected MarkupProcessor(String namespace, PortletInvocationContext context, org.gatein.pc.api.PortletContext target, URLFormat format, ProducerInfo info)
   {
      this.namespace = namespace;
      this.context = context;
      this.format = format;
      supportedCustomModes = info.getSupportedCustomModes();
      supportedCustomWindowStates = info.getSupportedCustomWindowStates();
      serverAddress = info.getEndpointConfigurationInfo().getRemoteHostAddress();
      portletApplicationName = target.getApplicationName();
   }

   public String getReplacementFor(String match, String prefix, String suffix)
   {
      if (prefix.equals(match))
      {
         return namespace;
      }
      else if (match.startsWith(WSRPRewritingConstants.BEGIN_WSRP_REWRITE_END))
      {
         // remove end of rewrite token
         match = match.substring(WSRPRewritingConstants.BEGIN_WSRP_REWRITE_END.length());

         WSRPPortletURL portletURL = WSRPPortletURL.create(match, supportedCustomModes, supportedCustomWindowStates, true);
         return context.renderURL(portletURL, format);
      }
      else
      {
         // match is not something we know how to process
         return match;
      }
   }


   static String getResourceURL(String urlAsString, WSRPResourceURL resource)
   {
      String resourceURL = resource.getResourceURL().toExternalForm();
      if (InvocationHandler.log.isDebugEnabled())
      {
         InvocationHandler.log.debug("URL '" + urlAsString + "' refers to a resource which are not currently well supported. " +
            "Attempting to craft a URL that we might be able to work with: '" + resourceURL + "'");
      }

      // right now the resourceURL should be output as is, because it will be used directly but it really should be encoded
      return resourceURL;
   }

   /**
    * TODO: This is a copy of the TextTools.replaceBoundedString method found in common module, copied here to avoid
    * having to release a new version of the module TODO: REMOVE when a new version of common is released.
    *
    * @param initial
    * @param prefix
    * @param suffix
    * @param generator
    * @param replaceIfBoundedStringEmpty
    * @param keepBoundaries
    * @param suffixIsOptional
    * @return
    */
   public static String replaceBoundedString(final String initial, final String prefix, final String suffix, final MarkupProcessor generator,
                                             final boolean replaceIfBoundedStringEmpty, final boolean keepBoundaries, final boolean suffixIsOptional)
   {
      if (ParameterValidation.isNullOrEmpty(initial))
      {
         return initial;
      }

      ParameterValidation.throwIllegalArgExceptionIfNull(generator, "StringReplacementGenerator");

      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(prefix, "prefix", "Tools.replaceBoundedString");

      StringBuilder tmp = new StringBuilder(initial);
      int prefixIndex = tmp.indexOf(prefix);
      final int prefixLength = prefix.length();
      boolean suffixAbsent = suffix == null;

      // nothing to do if didn't ask for an optional suffix and we have one and it's not present in our string
      if (!suffixIsOptional && suffix != null && tmp.indexOf(suffix) == -1)
      {
         return initial;
      }

      // loop as long as we can find an instance of prefix in the String
      while (prefixIndex != -1)
      {
         int suffixIndex;
         if (suffixAbsent)
         {
            // replace prefix with replacement
            if (keepBoundaries)
            {
               // just insert replacement for prefix
               tmp.insert(prefixIndex + prefixLength, generator.getReplacementFor(prefix, prefix, suffix));
            }
            else
            {
               // delete prefix then insert remplacement instead
               tmp.delete(prefixIndex, prefixIndex + prefixLength);
               tmp.insert(prefixIndex, generator.getReplacementFor(prefix, prefix, suffix));
            }

            // new lookup starting position
            prefixIndex = tmp.indexOf(prefix, prefixIndex + prefixLength);
         }
         else
         {
            // look for suffix
            suffixIndex = tmp.indexOf(suffix, prefixIndex);

            if (suffixIndex == -1)
            {
               // we haven't found suffix in the rest of the String so don't look for it again
               suffixAbsent = true;
               continue;
            }
            else
            {
               if (suffixIsOptional)
               {
                  // if suffix is optional, look for potential next prefix instance that we'd need to replace
                  int nextPrefixIndex = tmp.indexOf(prefix, prefixIndex + prefixLength);

                  if (nextPrefixIndex != -1 && nextPrefixIndex <= suffixIndex)
                  {
                     // we've found an in-between prefix, use it as the suffix for the current match
                     // delete prefix then insert remplacement instead
                     tmp.delete(prefixIndex, prefixIndex + prefixLength);
                     String replacement = generator.getReplacementFor(prefix, prefix, suffix);
                     tmp.insert(prefixIndex, replacement);

                     prefixIndex = nextPrefixIndex - prefixLength + replacement.length();
                     continue;
                  }
               }

               // we don't care about empty bounded strings or prefix and suffix don't delimit an empty String => replace!
               if (replaceIfBoundedStringEmpty || suffixIndex != prefixIndex + prefixLength)
               {
                  String match = tmp.substring(prefixIndex + prefixLength, suffixIndex);
                  if (keepBoundaries)
                  {
                     if (suffix != null)
                     {
                        // delete only match
                        tmp.delete(prefixIndex + prefixLength, suffixIndex);
                     }
                     else
                     {
                        // delete nothing
                     }
                     tmp.insert(prefixIndex + prefixLength, generator.getReplacementFor(match, prefix, suffix));
                  }
                  else
                  {
                     int suffixLength = suffix != null ? suffix.length() : 0;

                     if (suffix != null)
                     {
                        // if we have a suffix, delete everything between start of prefix and end of suffix
                        tmp.delete(prefixIndex, suffixIndex + suffixLength);
                     }
                     else
                     {
                        // only delete prefix
                        tmp.delete(prefixIndex, prefixIndex + prefixLength);
                     }
                     tmp.insert(prefixIndex, generator.getReplacementFor(match, prefix, suffix));
                  }
               }
            }

            prefixIndex = tmp.indexOf(prefix, prefixIndex + prefixLength);

         }
      }

      return tmp.toString();
   }
}
