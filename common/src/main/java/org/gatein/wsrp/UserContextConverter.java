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

package org.gatein.wsrp;

import org.gatein.common.NotYetImplemented;
import static org.gatein.common.p3p.P3PConstants.*;
import org.gatein.pc.api.spi.UserContext;
import org.oasis.wsrp.v1.Contact;
import org.oasis.wsrp.v1.EmployerInfo;
import org.oasis.wsrp.v1.Online;
import org.oasis.wsrp.v1.PersonName;
import org.oasis.wsrp.v1.Postal;
import org.oasis.wsrp.v1.Telecom;
import org.oasis.wsrp.v1.TelephoneNum;
import org.oasis.wsrp.v1.UserProfile;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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

   public static UserContext createPortalUserContextFrom(org.oasis.wsrp.v1.UserContext userContext,
                                                         List<String> desiredLocales, String preferredLocale)
   {
      return new WSRPMappedUserContext(userContext, desiredLocales, preferredLocale);
   }

   public static org.oasis.wsrp.v1.UserContext createWSRPUserContextFrom(UserContext userContext,
                                                                         String userContextKey,
                                                                         List<String> userCategories)
   {
      org.oasis.wsrp.v1.UserContext wsrpUserContext = WSRPTypeFactory.createUserContext(userContextKey);
      wsrpUserContext.setProfile(createUserProfileFrom(userContext));
      if (WSRPUtils.existsAndIsNotEmpty(userCategories))
      {
         wsrpUserContext.getUserCategories().addAll(userCategories);
      }
      return wsrpUserContext;
   }

   private static UserProfile createUserProfileFrom(UserContext userContext)
   {
      Map<String, String> userInfos = userContext.getInformations();

      if (!WSRPUtils.existsAndIsNotEmpty(userInfos))
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

      EmployerInfo employerInfo = new EmployerInfo();
      employerInfo.setEmployer(userInfos.get(INFO_USER_EMPLOYER));
      employerInfo.setDepartment(userInfos.get(INFO_USER_DEPARTMENT));
      employerInfo.setJobtitle(userInfos.get(INFO_USER_JOB_TITLE));

      UserProfile userProfile = new UserProfile();
      userProfile.setName(name);
      userProfile.setBdate(bdate);
      userProfile.setGender(userInfos.get(INFO_USER_GENDER));
      userProfile.setEmployerInfo(employerInfo);
      userProfile.setHomeInfo(createContactFrom(userInfos, false));
      userProfile.setBusinessInfo(createContactFrom(userInfos, true));
      return userProfile;
   }

   private static PersonName createNameFrom(Map<String, String> userInfos)
   {
      PersonName name = new PersonName();
      name.setPrefix(userInfos.get(INFO_USER_NAME_PREFIX));
      name.setFamily(userInfos.get(INFO_USER_NAME_FAMILY));
      name.setGiven(userInfos.get(INFO_USER_NAME_GIVEN));
      name.setMiddle(userInfos.get(INFO_USER_NAME_MIDDLE));
      name.setSuffix(userInfos.get(INFO_USER_NAME_SUFFIX));
      name.setNickname(userInfos.get(INFO_USER_NAME_NICKNAME));
      return name;
   }

   private static Contact createContactFrom(Map<String, String> infos, boolean isBusiness)
   {
      Online online = new Online();
      online.setEmail(infos.get(getOnlineUserInfoKey(OnlineInfo.EMAIL, isBusiness)));
      online.setUri(infos.get(getOnlineUserInfoKey(OnlineInfo.URI, isBusiness)));

      Postal postal = new Postal();
      postal.setName(infos.get(getPostalUserInfoKey(PostalInfo.NAME, isBusiness)));
      postal.setStreet(infos.get(getPostalUserInfoKey(PostalInfo.STREET, isBusiness)));
      postal.setCity(infos.get(getPostalUserInfoKey(PostalInfo.CITY, isBusiness)));
      postal.setStateprov(infos.get(getPostalUserInfoKey(PostalInfo.STATEPROV, isBusiness)));
      postal.setPostalcode(infos.get(getPostalUserInfoKey(PostalInfo.POSTALCODE, isBusiness)));
      postal.setCountry(infos.get(getPostalUserInfoKey(PostalInfo.COUNTRY, isBusiness)));
      postal.setOrganization(infos.get(getPostalUserInfoKey(PostalInfo.ORGANIZATION, isBusiness)));

      Telecom telecom = new Telecom();
      telecom.setTelephone(createTelephoneNumFrom(infos, TelecomType.TELEPHONE, isBusiness));
      telecom.setFax(createTelephoneNumFrom(infos, TelecomType.FAX, isBusiness));
      telecom.setMobile(createTelephoneNumFrom(infos, TelecomType.MOBILE, isBusiness));
      telecom.setPager(createTelephoneNumFrom(infos, TelecomType.PAGER, isBusiness));

      Contact contact = new Contact();
      contact.setPostal(postal);
      contact.setTelecom(telecom);
      contact.setOnline(online);

      return contact;
   }

   private static TelephoneNum createTelephoneNumFrom(Map<String, String> infos, TelecomType type, boolean isBusiness)
   {
      TelephoneNum num = new TelephoneNum();
      num.setIntcode(infos.get(getTelecomInfoKey(type, TelecomInfo.INTCODE, isBusiness)));
      num.setLoccode(infos.get(getTelecomInfoKey(type, TelecomInfo.LOCCODE, isBusiness)));
      num.setNumber(infos.get(getTelecomInfoKey(type, TelecomInfo.NUMBER, isBusiness)));
      num.setExt(infos.get(getTelecomInfoKey(type, TelecomInfo.EXT, isBusiness)));
      num.setComment(infos.get(getTelecomInfoKey(type, TelecomInfo.COMMENT, isBusiness)));

      return num;
   }

   /**
    * Builds a Portal {@link org.gatein.pc.api.spi.UserContext} from a WSRP {@link org.oasis.wsrp.v1.UserContext}.
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

      public WSRPMappedUserContext(org.oasis.wsrp.v1.UserContext userContext, List<String> desiredLocales, String preferredLocale)
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

         if (WSRPUtils.existsAndIsNotEmpty(desiredLocales))
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
