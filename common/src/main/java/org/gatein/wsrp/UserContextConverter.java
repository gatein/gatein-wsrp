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

import org.gatein.common.NotYetImplemented;
import org.gatein.common.util.ParameterValidation;
import org.gatein.pc.api.spi.UserContext;
import org.oasis.wsrp.v2.Contact;
import org.oasis.wsrp.v2.EmployerInfo;
import org.oasis.wsrp.v2.Online;
import org.oasis.wsrp.v2.PersonName;
import org.oasis.wsrp.v2.Postal;
import org.oasis.wsrp.v2.Telecom;
import org.oasis.wsrp.v2.TelephoneNum;
import org.oasis.wsrp.v2.UserProfile;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.gatein.common.p3p.P3PConstants.*;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 11352 $
 * @since 2.4 (May 8, 2006)
 */
public class UserContextConverter
{
   private UserContextConverter()
   {
   }

   public static UserContext createPortalUserContextFrom(org.oasis.wsrp.v2.UserContext userContext,
                                                         List<String> desiredLocales, String preferredLocale)
   {
      return new WSRPMappedUserContext(userContext, desiredLocales, preferredLocale);
   }

   public static org.oasis.wsrp.v2.UserContext createWSRPUserContextFrom(UserContext userContext,
                                                                         String userContextKey,
                                                                         List<String> userCategories)
   {
      org.oasis.wsrp.v2.UserContext wsrpUserContext = WSRPTypeFactory.createUserContext(userContextKey);
      wsrpUserContext.setProfile(createUserProfileFrom(userContext));
      if (ParameterValidation.existsAndIsNotEmpty(userCategories))
      {
         wsrpUserContext.getUserCategories().addAll(userCategories);
      }
      return wsrpUserContext;
   }

   private static UserProfile createUserProfileFrom(UserContext userContext)
   {
      Map<String, String> userInfos = userContext.getInformations();

      if (!ParameterValidation.existsAndIsNotEmpty(userInfos))
      {
         return null;
      }

      PersonName name = createNameFrom(userInfos);

      XMLGregorianCalendar bdate = null;
      String bdateAsString = userInfos.get(INFO_USER_BDATE);
      if (bdateAsString != null)
      {
         DatatypeFactory datatypeFactory = null;
         try
         {
            datatypeFactory = DatatypeFactory.newInstance();
            bdate = datatypeFactory.newXMLGregorianCalendar(bdateAsString);
         }
         catch (DatatypeConfigurationException e)
         {
            // todo: do something better here
            e.printStackTrace();
         }
      }

      String employer = userInfos.get(INFO_USER_EMPLOYER);
      String department = userInfos.get(INFO_USER_DEPARTMENT);
      String jobTitle = userInfos.get(INFO_USER_JOB_TITLE);
      EmployerInfo employerInfo = WSRPTypeFactory.createEmployerInfo(employer, department, jobTitle);

      Contact homeInfo = createContactFrom(userInfos, false);
      Contact businessInfo = createContactFrom(userInfos, true);
      UserProfile userProfile = WSRPTypeFactory.createUserProfile(name, bdate, userInfos.get(INFO_USER_GENDER), employerInfo, homeInfo, businessInfo);

      return userProfile;
   }

   private static PersonName createNameFrom(Map<String, String> userInfos)
   {
      String prefix = userInfos.get(INFO_USER_NAME_PREFIX);
      String given = userInfos.get(INFO_USER_NAME_GIVEN);
      String family = userInfos.get(INFO_USER_NAME_FAMILY);
      String middle = userInfos.get(INFO_USER_NAME_MIDDLE);
      String suffix = userInfos.get(INFO_USER_NAME_SUFFIX);
      String nickName = userInfos.get(INFO_USER_NAME_NICKNAME);
      
      PersonName name = WSRPTypeFactory.createPersonName(prefix, given, family, middle, suffix, nickName);

      return name;
   }

   private static Contact createContactFrom(Map<String, String> infos, boolean isBusiness)
   {
      String email = infos.get(getOnlineUserInfoKey(OnlineInfo.EMAIL, isBusiness));
      String uri = infos.get(getOnlineUserInfoKey(OnlineInfo.URI, isBusiness));
      Online online = WSRPTypeFactory.createOnline(email, uri);

      String name = infos.get(getPostalUserInfoKey(PostalInfo.NAME, isBusiness));
      String street = infos.get(getPostalUserInfoKey(PostalInfo.STREET, isBusiness));
      String city = infos.get(getPostalUserInfoKey(PostalInfo.CITY, isBusiness));
      String stateprov = infos.get(getPostalUserInfoKey(PostalInfo.STATEPROV, isBusiness));
      String postalCode = infos.get(getPostalUserInfoKey(PostalInfo.POSTALCODE, isBusiness));
      String country = infos.get(getPostalUserInfoKey(PostalInfo.COUNTRY, isBusiness));
      String organization = infos.get(getPostalUserInfoKey(PostalInfo.ORGANIZATION, isBusiness));
      Postal postal = WSRPTypeFactory.createPostal(name, street, city, stateprov, postalCode, country, organization);

      TelephoneNum telephone = createTelephoneNumFrom(infos, TelecomType.TELEPHONE, isBusiness);
      TelephoneNum fax = createTelephoneNumFrom(infos, TelecomType.FAX, isBusiness);
      TelephoneNum mobile = createTelephoneNumFrom(infos, TelecomType.MOBILE, isBusiness);
      TelephoneNum pager = createTelephoneNumFrom(infos, TelecomType.PAGER, isBusiness);
      Telecom telecom = WSRPTypeFactory.createTelecom(telephone, fax, mobile, pager);
      
      Contact contact = WSRPTypeFactory.createContact(postal, telecom, online);

      return contact;
   }

   private static TelephoneNum createTelephoneNumFrom(Map<String, String> infos, TelecomType type, boolean isBusiness)
   {
      String intCode = infos.get(getTelecomInfoKey(type, TelecomInfo.INTCODE, isBusiness));
      String loccode = infos.get(getTelecomInfoKey(type, TelecomInfo.LOCCODE, isBusiness));
      String number = infos.get(getTelecomInfoKey(type, TelecomInfo.NUMBER, isBusiness));
      String ext = infos.get(getTelecomInfoKey(type, TelecomInfo.EXT, isBusiness));
      String comment = infos.get(getTelecomInfoKey(type, TelecomInfo.COMMENT, isBusiness));
      TelephoneNum telephoneNum = WSRPTypeFactory.createTelephoneNum(intCode, loccode, number, ext, comment);

      return telephoneNum;
   }

   /**
    * Builds a Portal {@link org.gatein.pc.api.spi.UserContext} from a WSRP {@link org.oasis.wsrp.v2.UserContext}.
    *
    * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
    * @since 2.4 (May 8, 2006)
    */
   static class WSRPMappedUserContext implements UserContext
   {
      private Map<String, String> infos;
      private List<String> desiredLocales;
      private Locale locale;
      private String id;

      public WSRPMappedUserContext(org.oasis.wsrp.v2.UserContext userContext, List<String> desiredLocales, String preferredLocale)
      {
         this.desiredLocales = desiredLocales;
         this.locale = WSRPUtils.getLocale(preferredLocale);
         if (userContext != null)
         {
            UserProfile profile = userContext.getProfile();
            if (profile != null)
            {
               infos = new HashMap<String, String>();
               XMLGregorianCalendar bdate = profile.getBdate();
               if (bdate != null)
               {
                  infos.put(INFO_USER_BDATE, bdate.toString());
               }

               infos.put(INFO_USER_GENDER, profile.getGender());

               PersonName name = profile.getName();
               if (name != null)
               {
                  infos.put(INFO_USER_NAME_FAMILY, name.getFamily());
                  infos.put(INFO_USER_NAME_GIVEN, name.getGiven());
                  infos.put(INFO_USER_NAME_MIDDLE, name.getMiddle());
                  infos.put(INFO_USER_NAME_NICKNAME, name.getNickname());
                  infos.put(INFO_USER_NAME_PREFIX, name.getPrefix());
                  infos.put(INFO_USER_NAME_SUFFIX, name.getSuffix());
               }

               populateContactInfo(profile.getBusinessInfo(), true);

               populateContactInfo(profile.getHomeInfo(), false);

               EmployerInfo employerInfo = profile.getEmployerInfo();
               if (employerInfo != null)
               {
                  infos.put(INFO_USER_DEPARTMENT, employerInfo.getDepartment());
                  infos.put(INFO_USER_EMPLOYER, employerInfo.getEmployer());
                  infos.put(INFO_USER_JOB_TITLE, employerInfo.getJobtitle());
               }
            }

            String key = userContext.getUserContextKey();
            if (key == null)
            {
               throw new IllegalArgumentException("Missing required userContextKey in UserContext!");
            }
            id = key;
         }
         else
         {
            infos = Collections.emptyMap();
         }
      }

      public String getId()
      {
         return id;
      }

      public Map getInformations()
      {
         return infos;
      }

      private void populateContactInfo(Contact contact, boolean isBusiness)
      {
         if (contact != null)
         {
            Online online = contact.getOnline();
            if (online != null)
            {
               infos.put(getOnlineUserInfoKey(OnlineInfo.EMAIL, isBusiness), online.getEmail());
               infos.put(getOnlineUserInfoKey(OnlineInfo.URI, isBusiness), online.getUri());
            }

            Postal postal = contact.getPostal();
            if (postal != null)
            {
               infos.put(getPostalUserInfoKey(PostalInfo.NAME, isBusiness), postal.getName());
               infos.put(getPostalUserInfoKey(PostalInfo.STREET, isBusiness), postal.getStreet());
               infos.put(getPostalUserInfoKey(PostalInfo.CITY, isBusiness), postal.getCity());
               infos.put(getPostalUserInfoKey(PostalInfo.STATEPROV, isBusiness), postal.getStateprov());
               infos.put(getPostalUserInfoKey(PostalInfo.POSTALCODE, isBusiness), postal.getPostalcode());
               infos.put(getPostalUserInfoKey(PostalInfo.COUNTRY, isBusiness), postal.getCountry());
               infos.put(getPostalUserInfoKey(PostalInfo.ORGANIZATION, isBusiness), postal.getOrganization());
            }

            Telecom telecom = contact.getTelecom();
            if (telecom != null)
            {
               populateTelephoneInfo(telecom.getFax(), TelecomType.FAX, isBusiness);
               populateTelephoneInfo(telecom.getMobile(), TelecomType.MOBILE, isBusiness);
               populateTelephoneInfo(telecom.getPager(), TelecomType.PAGER, isBusiness);
               populateTelephoneInfo(telecom.getTelephone(), TelecomType.TELEPHONE, isBusiness);
            }
         }
      }

      private void populateTelephoneInfo(TelephoneNum telephoneNum, TelecomType type, boolean isBusiness)
      {
         if (telephoneNum != null)
         {
            infos.put(getTelecomInfoKey(type, TelecomInfo.INTCODE, isBusiness), telephoneNum.getIntcode());
            infos.put(getTelecomInfoKey(type, TelecomInfo.LOCCODE, isBusiness), telephoneNum.getLoccode());
            infos.put(getTelecomInfoKey(type, TelecomInfo.NUMBER, isBusiness), telephoneNum.getNumber());
            infos.put(getTelecomInfoKey(type, TelecomInfo.EXT, isBusiness), telephoneNum.getExt());
            infos.put(getTelecomInfoKey(type, TelecomInfo.COMMENT, isBusiness), telephoneNum.getComment());
         }
      }

      public Locale getLocale()
      {
         return locale;
      }

      public List<Locale> getLocales()
      {
         List<Locale> locales = Collections.emptyList();

         if (ParameterValidation.existsAndIsNotEmpty(desiredLocales))
         {
            locales = new ArrayList<Locale>(desiredLocales.size());
            for (String desiredLocale : desiredLocales)
            {
               Locale locale = WSRPUtils.getLocale(desiredLocale);
               locales.add(locale);
            }
         }

         return locales;
      }

      public Object getAttribute(String arg0)
      {
         throw new NotYetImplemented();
      }

      public void setAttribute(String arg0, Object arg1)
      {
         throw new NotYetImplemented();
      }
   }
}
