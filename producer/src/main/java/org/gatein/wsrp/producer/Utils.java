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

import org.gatein.common.util.ParameterValidation;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.WSRPUtils;
import org.gatein.wsrp.registration.LocalizedString;
import org.gatein.wsrp.registration.RegistrationPropertyDescription;
import org.oasis.wsrp.v1.ModelDescription;
import org.oasis.wsrp.v1.PropertyDescription;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class Utils
{
   /**
    * @param registrationInfo
    * @return
    * @since 2.6
    */
   public static ModelDescription convertRegistrationPropertiesToModelDescription(Map<QName, RegistrationPropertyDescription> registrationInfo)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(registrationInfo, "registration info");
      if (registrationInfo.isEmpty())
      {
         return WSRPTypeFactory.createModelDescription(null);
      }


      List<PropertyDescription> propertyDescriptions = new ArrayList<PropertyDescription>(registrationInfo.size());
      for (RegistrationPropertyDescription property : registrationInfo.values())
      {
         propertyDescriptions.add(convertToPropertyDescription(property));
      }

      return WSRPTypeFactory.createModelDescription(propertyDescriptions);
   }

   /**
    * @param propertyDescription
    * @return
    * @since 2.6
    */
   public static PropertyDescription convertToPropertyDescription(RegistrationPropertyDescription propertyDescription)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(propertyDescription, "RegistrationPropertyDescription");
      PropertyDescription propDesc = WSRPTypeFactory.createPropertyDescription(propertyDescription.getName().toString(),
         propertyDescription.getType());

      // todo: deal with languages properly!!
      LocalizedString hint = propertyDescription.getHint();
      if (hint != null)
      {
         propDesc.setHint(convertToWSRPLocalizedString(hint));
      }
      LocalizedString label = propertyDescription.getLabel();
      if (label != null)
      {
         propDesc.setLabel(convertToWSRPLocalizedString(label));
      }
      return propDesc;
   }

   /**
    * @param propertyDescription
    * @return
    * @since 2.6
    */
   public static RegistrationPropertyDescription convertToRegistrationPropertyDescription(PropertyDescription propertyDescription)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(propertyDescription, "PropertyDescription");
      RegistrationPropertyDescription desc = new RegistrationPropertyDescription(propertyDescription.getName(),
         propertyDescription.getType());
      desc.setLabel(getLocalizedStringOrNull(propertyDescription.getLabel()));
      desc.setHint(getLocalizedStringOrNull(propertyDescription.getHint()));

      return desc;
   }

   private static LocalizedString getLocalizedStringOrNull(org.oasis.wsrp.v1.LocalizedString wsrpLocalizedString)
   {
      if (wsrpLocalizedString == null)
      {
         return null;
      }
      else
      {
         return convertToLocalizedString(wsrpLocalizedString);
      }
   }

   /**
    * @param wsrpLocalizedString
    * @return
    * @since 2.6
    */
   public static LocalizedString convertToLocalizedString(org.oasis.wsrp.v1.LocalizedString wsrpLocalizedString)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(wsrpLocalizedString, "WSRP LocalizedString");
      String lang = wsrpLocalizedString.getLang();
      Locale locale;
      if (lang == null)
      {
         locale = Locale.getDefault();
      }
      else
      {
         locale = WSRPUtils.getLocale(lang);
      }

      LocalizedString localizedString = new LocalizedString(wsrpLocalizedString.getValue(), locale);
      localizedString.setResourceName(wsrpLocalizedString.getResourceName());
      return localizedString;
   }

   /**
    * @param regLocalizedString
    * @return
    * @since 2.6
    */
   public static org.oasis.wsrp.v1.LocalizedString convertToWSRPLocalizedString(LocalizedString regLocalizedString)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(regLocalizedString, "LocalizedString");
      return WSRPTypeFactory.createLocalizedString(WSRPUtils.toString(regLocalizedString.getLocale()),
         regLocalizedString.getResourceName(), regLocalizedString.getValue());
   }

   /**
    * Creates a WSRP LocalizedString based on the best (according to the specified desired locales) value of the given
    * LocalizedString.
    *
    * @param localizedString the LocalizedString from which a localized value is to be extracted
    * @param desiredLocales  the supported locales orderd by user preference, if no desiredLocales are given, the
    *                        default local will be used.
    * @return the best (according to the specified desired locales) value for the given LocalizedString or
    *         <code>null</code> if no such value can be found.
    * @since 2.6
    */
   public static org.oasis.wsrp.v1.LocalizedString convertToWSRPLocalizedString(org.gatein.common.i18n.LocalizedString localizedString,
                                                                                List<String> desiredLocales)
   {
      if (localizedString == null)
      {
         return null;
      }

      if (desiredLocales == null || desiredLocales.isEmpty())
      {
         desiredLocales = Collections.singletonList(WSRPUtils.toString(Locale.getDefault()));
      }

      // todo: rewrite getPreferredOrBestLocalizedMappingFor to take a List as argument
      org.gatein.common.i18n.LocalizedString.Value bestMapping =
         localizedString.getPreferredOrBestLocalizedMappingFor(desiredLocales.toArray(new String[desiredLocales.size()]));
      if (bestMapping != null)
      {
         Locale locale = bestMapping.getLocale();
         String value = bestMapping.getString();
         String language = WSRPUtils.toString(locale);
         return WSRPTypeFactory.createLocalizedString(language, null, value);
      }
      return null;
   }
}
