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

package org.gatein.registration;

import org.gatein.common.util.ParameterValidation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 10388 $
 * @since 2.6
 */
public class RegistrationUtils
{
   private static boolean strict = true;
   private static final Logger log = LoggerFactory.getLogger(RegistrationUtils.class);

   public static void setStrict(boolean strict)
   {
      RegistrationUtils.strict = strict;
      log.debug("Using " + (strict ? "strict" : "lenient") + " Consumer Agent validation mode.");
   }

   /**
    * Per the specification (http://www.oasis-open.org/committees/download.php/18617/wsrp-2.0-spec-pr-01.html#_RegistrationData):
    * The consumerAgent value MUST start with "productName.majorVersion.minorVersion" where "productName" identifies the
    * product the Consumer installed for its deployment, and majorVersion and minorVersion are vendor-defined
    * indications of the version of its product. This string can then contain any additional characters/words the
    * product or Consumer wish to supply.
    *
    * @param consumerAgent
    * @throws IllegalArgumentException
    * @since 2.6
    */
   public static void validateConsumerAgent(final String consumerAgent) throws IllegalArgumentException
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(consumerAgent, "consumer agent", null);
      char periodChar = '.';

      String tmp = consumerAgent.trim();

      int period = tmp.indexOf(periodChar);
      if (period != -1)
      {
         tmp = tmp.substring(period + 1);
         period = tmp.indexOf(periodChar);

         if (period != -1)
         {
            tmp = tmp.substring(period + 1);

            if (tmp.length() > 0)
            {
               return;
            }
         }
      }

      String msg = "'" + consumerAgent + "' is not a valid Consumer Agent. Please notify your Consumer provider that it is not WSRP-compliant.";
      if (strict)
      {
         throw new IllegalArgumentException(msg);
      }
      else
      {
         log.debug(msg);
      }
   }
}
