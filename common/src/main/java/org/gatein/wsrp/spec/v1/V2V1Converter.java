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

package org.gatein.wsrp.spec.v1;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.gatein.common.NotYetImplemented;
import org.gatein.pc.api.OpaqueStateString;
import org.gatein.wsrp.WSRPExceptionFactory;
import org.gatein.wsrp.WSRPTypeFactory;
import org.oasis.wsrp.v1.V1ClientData;
import org.oasis.wsrp.v1.V1Contact;
import org.oasis.wsrp.v1.V1CookieProtocol;
import org.oasis.wsrp.v1.V1EmployerInfo;
import org.oasis.wsrp.v1.V1Extension;
import org.oasis.wsrp.v1.V1Fault;
import org.oasis.wsrp.v1.V1InteractionParams;
import org.oasis.wsrp.v1.V1ItemDescription;
import org.oasis.wsrp.v1.V1LocalizedString;
import org.oasis.wsrp.v1.V1MarkupContext;
import org.oasis.wsrp.v1.V1MarkupParams;
import org.oasis.wsrp.v1.V1MarkupType;
import org.oasis.wsrp.v1.V1ModelDescription;
import org.oasis.wsrp.v1.V1ModelTypes;
import org.oasis.wsrp.v1.V1NamedString;
import org.oasis.wsrp.v1.V1Online;
import org.oasis.wsrp.v1.V1PersonName;
import org.oasis.wsrp.v1.V1PortletContext;
import org.oasis.wsrp.v1.V1PortletDescription;
import org.oasis.wsrp.v1.V1Postal;
import org.oasis.wsrp.v1.V1PropertyDescription;
import org.oasis.wsrp.v1.V1RegistrationContext;
import org.oasis.wsrp.v1.V1Resource;
import org.oasis.wsrp.v1.V1ResourceList;
import org.oasis.wsrp.v1.V1ResourceValue;
import org.oasis.wsrp.v1.V1RuntimeContext;
import org.oasis.wsrp.v1.V1ServiceDescription;
import org.oasis.wsrp.v1.V1SessionContext;
import org.oasis.wsrp.v1.V1StateChange;
import org.oasis.wsrp.v1.V1Telecom;
import org.oasis.wsrp.v1.V1TelephoneNum;
import org.oasis.wsrp.v1.V1Templates;
import org.oasis.wsrp.v1.V1UpdateResponse;
import org.oasis.wsrp.v1.V1UploadContext;
import org.oasis.wsrp.v1.V1UserContext;
import org.oasis.wsrp.v1.V1UserProfile;
import org.oasis.wsrp.v2.ClientData;
import org.oasis.wsrp.v2.Contact;
import org.oasis.wsrp.v2.CookieProtocol;
import org.oasis.wsrp.v2.EmployerInfo;
import org.oasis.wsrp.v2.Extension;
import org.oasis.wsrp.v2.InteractionParams;
import org.oasis.wsrp.v2.ItemDescription;
import org.oasis.wsrp.v2.LocalizedString;
import org.oasis.wsrp.v2.MarkupContext;
import org.oasis.wsrp.v2.MarkupParams;
import org.oasis.wsrp.v2.MarkupType;
import org.oasis.wsrp.v2.ModelDescription;
import org.oasis.wsrp.v2.ModelTypes;
import org.oasis.wsrp.v2.NamedString;
import org.oasis.wsrp.v2.Online;
import org.oasis.wsrp.v2.PersonName;
import org.oasis.wsrp.v2.PortletContext;
import org.oasis.wsrp.v2.PortletDescription;
import org.oasis.wsrp.v2.Postal;
import org.oasis.wsrp.v2.PropertyDescription;
import org.oasis.wsrp.v2.RegistrationContext;
import org.oasis.wsrp.v2.Resource;
import org.oasis.wsrp.v2.ResourceList;
import org.oasis.wsrp.v2.ResourceValue;
import org.oasis.wsrp.v2.RuntimeContext;
import org.oasis.wsrp.v2.ServiceDescription;
import org.oasis.wsrp.v2.SessionContext;
import org.oasis.wsrp.v2.StateChange;
import org.oasis.wsrp.v2.Telecom;
import org.oasis.wsrp.v2.TelephoneNum;
import org.oasis.wsrp.v2.Templates;
import org.oasis.wsrp.v2.UpdateResponse;
import org.oasis.wsrp.v2.UploadContext;
import org.oasis.wsrp.v2.UserContext;
import org.oasis.wsrp.v2.UserProfile;

import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class V2V1Converter
{
   public static final V1ToV2Extension V1_TO_V2_EXTENSION = new V1ToV2Extension();

   public static final V2ToV1Extension V2_TO_V1_EXTENSION = new V2ToV1Extension();
   public static final V2ToV1MarkupType V2_TO_V1_MARKUPTYPE = new V2ToV1MarkupType();
   public static final V1ToV2MarkupType V1_TO_V2_MARKUPTYPE = new V1ToV2MarkupType();
   public static final V2ToV1PortletDescription V2_TO_V1_PORTLETDESCRIPTION = new V2ToV1PortletDescription();
   public static final V1ToV2PortletDescription V1_TO_V2_PORTLETDESCRIPTION = new V1ToV2PortletDescription();
   public static final V2ToV1LocalizedString V2_TO_V1_LOCALIZEDSTRING = new V2ToV1LocalizedString();
   public static final V1ToV2LocalizedString V1_TO_V2_LOCALIZEDSTRING = new V1ToV2LocalizedString();
   public static final V2ToV1ItemDescription V2_TO_V1_ITEMDESCRIPTION = new V2ToV1ItemDescription();

   public static final V1ToV2ItemDescription V1_TO_V2_ITEMDESCRIPTION = new V1ToV2ItemDescription();
   public static final V2ToV1PropertyDescription V2_TO_V1_PROPERTYDESCRIPTION = new V2ToV1PropertyDescription();
   public static final V2ToV1Resource V2_TO_V1_RESOURCE = new V2ToV1Resource();
   public static final V2ToV1ResourceValue V2_TO_V1_RESOURCEVALUE = new V2ToV1ResourceValue();
   public static final V1ToV2PropertyDescription V1_TO_V2_PROPERTYDESCRIPTION = new V1ToV2PropertyDescription();
   public static final V1ToV2Resource V1_TO_V2_RESOURCE = new V1ToV2Resource();
   private static final V1ToV2ResourceValue V1_TO_V2_RESOURCEVALUE = new V1ToV2ResourceValue();


   public static final V1ToV2NamedString V1_TO_V2_NAMEDSTRING = new V1ToV2NamedString();
   public static final V2ToV1NamedString V2_TO_V1_NAMEDSTRING = new V2ToV1NamedString();
   public static final V1ToV2UploadContext V1_TO_V2_UPLOADCONTEXT = new V1ToV2UploadContext();
   public static final V2ToV1UploadContext V2_TO_V1_UPLOADCONTEXT = new V2ToV1UploadContext();

   public static <F, T> List<T> transform(List<F> fromList, Function<? super F, ? extends T> function)
   {
      if (fromList == null)
      {
         return null;
      }
      else
      {
         return Lists.transform(fromList, function);
      }
   }


   public static V1PortletDescription toV1PortletDescription(PortletDescription portletDescription)
   {
      throw new NotYetImplemented();
   }

   public static V1PortletContext toV1PortletContext(PortletContext portletContext)
   {
      throw new NotYetImplemented();
   }

   public static MarkupParams toV2MarkupParams(V1MarkupParams v1MarkupParams)
   {
      if (v1MarkupParams != null)
      {
         MarkupParams markupParams = WSRPTypeFactory.createMarkupParams(v1MarkupParams.isSecureClientCommunication(),
            v1MarkupParams.getLocales(), v1MarkupParams.getMimeTypes(), v1MarkupParams.getMode(),
            v1MarkupParams.getWindowState());
         markupParams.setClientData(toV2ClientData(v1MarkupParams.getClientData()));
         markupParams.setNavigationalContext(WSRPTypeFactory.createNavigationalContextOrNull(
            new OpaqueStateString(v1MarkupParams.getNavigationalState()), null));
         markupParams.setValidateTag(v1MarkupParams.getValidateTag());

         List<String> charSets = v1MarkupParams.getMarkupCharacterSets();
         if (charSets != null)
         {
            markupParams.getMarkupCharacterSets().addAll(charSets);
         }
         List<String> validNewModes = v1MarkupParams.getValidNewModes();
         if (validNewModes != null)
         {
            markupParams.getValidNewModes().addAll(validNewModes);
         }
         List<String> validNewWindowStates = v1MarkupParams.getValidNewWindowStates();
         if (validNewWindowStates != null)
         {
            markupParams.getValidNewWindowStates().addAll(validNewWindowStates);
         }

         List<Extension> extensions = V2V1Converter.transform(v1MarkupParams.getExtensions(), V1_TO_V2_EXTENSION);
         if (extensions != null)
         {
            markupParams.getExtensions().addAll(extensions);
         }
         return markupParams;
      }
      else
      {
         return null;
      }
   }

   public static V1MarkupParams toV1MarkupParams(MarkupParams markupParams)
   {
      if (markupParams != null)
      {
         V1MarkupParams v1MarkupParams = WSRP1TypeFactory.createMarkupParams(markupParams.isSecureClientCommunication(), markupParams.getLocales(), 
               markupParams.getMimeTypes(), markupParams.getMode(), markupParams.getWindowState());
       
         v1MarkupParams.setClientData(toV1ClientData(markupParams.getClientData()));
         v1MarkupParams.setNavigationalState(markupParams.getNavigationalContext().getOpaqueValue());
         v1MarkupParams.setValidateTag(markupParams.getValidateTag());
         
         List<String> charSets = markupParams.getMarkupCharacterSets();
         if (charSets != null)
         {
            v1MarkupParams.getMarkupCharacterSets().addAll(charSets);
         }
         
         List<String> validNewModes = markupParams.getValidNewModes();
         if (validNewModes != null)
         {
            v1MarkupParams.getValidNewModes().addAll(validNewModes);
         }
         
         List<String> validNewWindowStates = markupParams.getValidNewWindowStates();
         if (validNewWindowStates != null)
         {
            v1MarkupParams.getValidNewWindowStates().addAll(validNewWindowStates);
         }
         
         List<V1Extension> extensions = V2V1Converter.transform(markupParams.getExtensions(), V2_TO_V1_EXTENSION);
         if (extensions != null)
         {
            v1MarkupParams.getExtensions().addAll(extensions);
         }
         
         return v1MarkupParams;
      }
      else
      {
         return null;
      }
   }

   private static ClientData toV2ClientData(V1ClientData clientData)
   {
      throw new NotYetImplemented();
   }

   private static V1ClientData toV1ClientData(ClientData clientData)
   {
      if (clientData != null)
      {
         V1ClientData v1ClientData = WSRP1TypeFactory.createClientData(clientData.getUserAgent());
         
         List<Extension> extensions = clientData.getExtensions();
         if (extensions != null)
         {
            v1ClientData.getExtensions().addAll(Lists.transform(extensions, V2_TO_V1_EXTENSION));
         }

         return v1ClientData;
      }
      else
      {
         return null;
      }
   }
   
   public static PortletContext toV2PortletContext(V1PortletContext portletContext)
   {
      throw new NotYetImplemented();
   }

   public static RegistrationContext toV2RegistrationContext(V1RegistrationContext registrationContext)
   {
      if (registrationContext != null)
      {
         RegistrationContext result = WSRPTypeFactory.createRegistrationContext(registrationContext.getRegistrationHandle());
         result.setRegistrationState(registrationContext.getRegistrationState());
         List<Extension> extensions = V2V1Converter.transform(registrationContext.getExtensions(), V1_TO_V2_EXTENSION);
         if (extensions != null)
         {
            result.getExtensions().addAll(extensions);
         }

         return result;
      }
      else
      {
         return null;
      }
   }

   public static RuntimeContext toV2RuntimeContext(V1RuntimeContext runtimeContext)
   {
      throw new NotYetImplemented();
   }

   public static V1RuntimeContext toV1RuntimeContext(RuntimeContext runtimeContext)
   {
      if (runtimeContext != null)
      {
         V1RuntimeContext v1RuntimeContext = WSRP1TypeFactory.createRuntimeContext(runtimeContext.getUserAuthentication());
         v1RuntimeContext.setNamespacePrefix(runtimeContext.getNamespacePrefix());
         v1RuntimeContext.setPortletInstanceKey(runtimeContext.getPortletInstanceKey());
         v1RuntimeContext.setSessionID(runtimeContext.getSessionParams().getSessionID());
         v1RuntimeContext.setTemplates(V2V1Converter.toV1Templates(runtimeContext.getTemplates()));
         
         List<Extension> extensions = runtimeContext.getExtensions();
         if (extensions != null)
         {
            v1RuntimeContext.getExtensions().addAll(V2V1Converter.transform(extensions, V2_TO_V1_EXTENSION));
         }
         
         return v1RuntimeContext;
      }
      else
      {
         return null;
      }
   }
   
   public static V1Templates toV1Templates(Templates templates)
   {
      if (templates != null)
      {
         //TODO: should we be using the WSRP1TypeFactory,createTemplates(PortletInvocationContext) instead?
         V1Templates v1Templates = new V1Templates();
         v1Templates.setBlockingActionTemplate(templates.getBlockingActionTemplate());
         v1Templates.setDefaultTemplate(templates.getDefaultTemplate());
         v1Templates.setRenderTemplate(templates.getRenderTemplate());
         v1Templates.setResourceTemplate(templates.getResourceTemplate());
         v1Templates.setSecureBlockingActionTemplate(templates.getSecureBlockingActionTemplate());
         v1Templates.setSecureRenderTemplate(templates.getSecureRenderTemplate());
         v1Templates.setSecureResourceTemplate(templates.getSecureResourceTemplate());
         
         List<Extension> extensions = templates.getExtensions();
         if (extensions != null)
         {
            v1Templates.getExtensions().addAll(V2V1Converter.transform(extensions, V2_TO_V1_EXTENSION));
         }
         
         return v1Templates;
      }
      else
      {
         return null;
      }
   }
   
   public static UserContext toV2UserContext(V1UserContext userContext)
   {
      throw new NotYetImplemented();
   }

   public static V1UserContext toV1UserContext(UserContext userContext)
   {
      if (userContext != null)
      {
         V1UserContext v1UserContext = WSRP1TypeFactory.createUserContext(userContext.getUserContextKey());
         
         v1UserContext.setProfile(toV1UserProfile(userContext.getProfile()));
         
         List<Extension> extensions = userContext.getExtensions();
         if (extensions != null)
         {
            v1UserContext.getExtensions().addAll(V2V1Converter.transform(extensions, V2_TO_V1_EXTENSION));
         }
         
         if (userContext.getUserCategories() != null)
         {
            v1UserContext.getUserCategories().addAll(userContext.getUserCategories());
         }
         
         return v1UserContext;
      }
      else
      {
         return null;
      }
   }
   
   public static V1UserProfile toV1UserProfile (UserProfile userProfile)
   {
      if (userProfile != null)
      {
         V1UserProfile v1UserProfile = new V1UserProfile();
         v1UserProfile.setBdate(userProfile.getBdate());
         v1UserProfile.setBusinessInfo(toV1Context(userProfile.getBusinessInfo()));
         v1UserProfile.setEmployerInfo(toV1EmployerInfo(userProfile.getEmployerInfo()));
         v1UserProfile.setGender(userProfile.getGender());
         v1UserProfile.setHomeInfo(toV1Context(userProfile.getHomeInfo()));
         v1UserProfile.setName(toV1PersonName(userProfile.getName()));
         
         return v1UserProfile;
      }
      else
      {
         return null;
      }
   }
   
   public static V1EmployerInfo toV1EmployerInfo (EmployerInfo employerInfo)
   {
      if (employerInfo != null)
      {
         V1EmployerInfo v1EmployerInfo = new V1EmployerInfo();
         v1EmployerInfo.setDepartment(employerInfo.getDepartment());
         v1EmployerInfo.setEmployer(employerInfo.getEmployer());
         v1EmployerInfo.setJobtitle(employerInfo.getJobtitle());
         
         List<Extension> extensions = employerInfo.getExtensions();
         if (extensions != null)
         {
            v1EmployerInfo.getExtensions().addAll(V2V1Converter.transform(extensions, V2_TO_V1_EXTENSION));
         }

         return v1EmployerInfo;
      }
      else
      {
         return null;
      }
   }
   
   public static V1PersonName toV1PersonName (PersonName personName)
   {
      if (personName != null)
      {
         V1PersonName v1PersonName = new V1PersonName();
         v1PersonName.setFamily(personName.getFamily());
         v1PersonName.setGiven(personName.getGiven());
         v1PersonName.setMiddle(personName.getMiddle());
         v1PersonName.setNickname(personName.getNickname());
         v1PersonName.setPrefix(personName.getPrefix());
         v1PersonName.setSuffix(personName.getSuffix());
         
         List<Extension> extensions = personName.getExtensions();
         if (extensions != null)
         {
            v1PersonName.getExtensions().addAll(V2V1Converter.transform(extensions, V2_TO_V1_EXTENSION));
         }
       
         return v1PersonName;
      }
      else
      {
         return null;
      }
   }
   
   public static V1Contact toV1Context (Contact contact)
   {
      if (contact != null)
      {
         V1Contact v1Contact= new V1Contact();
         v1Contact.setOnline(toV1Online(contact.getOnline()));
         v1Contact.setPostal(toV1Postal(contact.getPostal()));
         v1Contact.setTelecom(toV1Telecom(contact.getTelecom()));
         
         List<Extension> extensions = contact.getExtensions();
         if (extensions != null)
         {
            v1Contact.getExtensions().addAll(V2V1Converter.transform(extensions, V2_TO_V1_EXTENSION));
         }
         
         return v1Contact;
      }
      else
      {
         return null;
      }
   }
   
   public static V1Online toV1Online (Online online)
   {
      if (online != null)
      {
         V1Online v1Online = new V1Online();
         v1Online.setEmail(online.getEmail());
         v1Online.setUri(online.getUri());
         
         List<Extension> extensions = online.getExtensions();
         if (extensions != null)
         {
            v1Online.getExtensions().addAll(V2V1Converter.transform(extensions, V2_TO_V1_EXTENSION));
         }
         
         return v1Online;
      }
      else
      {
         return null;
      }
   }
   
   public static V1Postal toV1Postal (Postal postal)
   {
      if (postal != null)
      {
         V1Postal v1Postal = new V1Postal();
         v1Postal.setCity(postal.getCity());
         v1Postal.setCountry(postal.getCountry());
         v1Postal.setName(postal.getName());
         v1Postal.setOrganization(postal.getOrganization());
         v1Postal.setPostalcode(postal.getPostalcode());
         v1Postal.setStateprov(postal.getStateprov());
         v1Postal.setStreet(postal.getStreet());
         
         List<Extension> extensions = postal.getExtensions();
         if (extensions != null)
         {
            v1Postal.getExtensions().addAll(V2V1Converter.transform(extensions, V2_TO_V1_EXTENSION));
         }
         
         return v1Postal;
      }
      else
      {
         return null;
      }
   }
   
   public static V1Telecom toV1Telecom(Telecom telecom)
   {
      if (telecom != null)
      {
         V1Telecom v1Telecom = new V1Telecom();
         v1Telecom.setFax(toV1TelephoneNum(telecom.getFax()));
         v1Telecom.setMobile(toV1TelephoneNum(telecom.getMobile()));
         v1Telecom.setPager(toV1TelephoneNum(telecom.getPager()));
         v1Telecom.setTelephone(toV1TelephoneNum(telecom.getTelephone()));
        
         List<Extension> extensions = telecom.getExtensions();
         if (extensions != null)
         {
            v1Telecom.getExtensions().addAll(V2V1Converter.transform(extensions, V2_TO_V1_EXTENSION));
         }
         
         return v1Telecom;
      }
      else
      {
         return null;
      }
   }
   
   public static V1TelephoneNum toV1TelephoneNum (TelephoneNum telephoneNum)
   {
      if (telephoneNum != null)
      {
         V1TelephoneNum v1TelephoneNum = new V1TelephoneNum();
         v1TelephoneNum.setComment(telephoneNum.getComment());
         v1TelephoneNum.setExt(telephoneNum.getExt());
         v1TelephoneNum.setIntcode(telephoneNum.getIntcode());
         v1TelephoneNum.setLoccode(telephoneNum.getLoccode());
         v1TelephoneNum.setNumber(telephoneNum.getNumber());
         
         List<Extension> extensions = telephoneNum.getExtensions();
         if (extensions != null)
         {
            v1TelephoneNum.getExtensions().addAll(V2V1Converter.transform(extensions, V2_TO_V1_EXTENSION));
         }
         
         return v1TelephoneNum;
      }
      else
      {
         return null;
      }
   }
   
   public static V1MarkupContext toV1MarkupContext(MarkupContext markupContext)
   {
      throw new NotYetImplemented();
   }

   public static ServiceDescription toV2ServiceDescription(V1ServiceDescription v1ServiceDescription)
   {
      throw new NotYetImplemented();
   }

   public static V1RegistrationContext toV1RegistrationContext(RegistrationContext registrationContext)
   {
      if (registrationContext != null)
      {
         V1RegistrationContext result = WSRP1TypeFactory.createRegistrationContext(registrationContext.getRegistrationHandle());
         result.setRegistrationState(registrationContext.getRegistrationState());
         List<V1Extension> extensions = V2V1Converter.transform(registrationContext.getExtensions(), V2_TO_V1_EXTENSION);
         if (extensions != null)
         {
            result.getExtensions().addAll(extensions);
         }
         return result;
      }
      else
      {
         return null;
      }
   }

   public static <E extends Exception> E toV2Exception(Class<E> v2ExceptionClass, Exception v1Exception)
   {
      if (!"org.oasis.wsrp.v2".equals(v2ExceptionClass.getPackage().getName()))
      {
         throw new IllegalArgumentException("Specified exception class is not a WSRP 2 exception: " + v2ExceptionClass);
      }

      Class<? extends Exception> v1ExceptionClass = v1Exception.getClass();
      String v1Name = v1ExceptionClass.getSimpleName();
      int v1Index = v1Name.indexOf("V1");
      if (v1Index != 0 && !"org.oasis.wsrp.v1".equals(v1ExceptionClass.getPackage().getName()))
      {
         throw new IllegalArgumentException("Specified exception is not a WSRP 1 exception: " + v1Exception);
      }

      String v2Name = v2ExceptionClass.getSimpleName();
      // V2 class name should match V1 class name minus "V1"
      if (!v2Name.equals(v1Name.substring(2)))
      {
         throw new IllegalArgumentException("Exception names do not match. Requested: " + v2Name
            + ", was given: " + v1Name);
      }

      return WSRPExceptionFactory.createWSException(v2ExceptionClass, v1Exception.getMessage(), v1Exception.getCause());
   }

   public static <E extends Exception> E toV1Exception(Class<E> v1ExceptionClass, Exception v2Exception)
   {
      if (!"org.oasis.wsrp.v1".equals(v1ExceptionClass.getPackage().getName()))
      {
         throw new IllegalArgumentException("Specified exception class is not a WSRP 1 exception: " + v1ExceptionClass);
      }

      Class<? extends Exception> v2ExceptionClass = v2Exception.getClass();
      String v1Name = v2ExceptionClass.getSimpleName();
      if (!"org.oasis.wsrp.v2".equals(v2ExceptionClass.getPackage().getName()))
      {
         throw new IllegalArgumentException("Specified exception is not a WSRP 2 exception: " + v2Exception);
      }

      String v2Name = v2ExceptionClass.getSimpleName();
      // V1 class name should match V2 class name plus "V1"
      if (!v2Name.equals(v1Name.substring(2)))
      {
         throw new IllegalArgumentException("Exception names do not match. Requested: " + v1Name
            + ", was given: " + v2Name);
      }

      return WSRP1ExceptionFactory.createWSException(v1ExceptionClass, v2Exception.getMessage(), v2Exception.getCause());
   }

   public static V1CookieProtocol toV1CookieProtocol(CookieProtocol requiresInitCookie)
   {
      if (requiresInitCookie != null)
      {
         return V1CookieProtocol.fromValue(requiresInitCookie.value());
      }
      else
      {
         return null;
      }
   }

   public static V1ModelDescription toV1ModelDescription(ModelDescription modelDescription)
   {
      if (modelDescription != null)
      {
         V1ModelDescription result = WSRP1TypeFactory.createModelDescription(V2V1Converter.transform(modelDescription.getPropertyDescriptions(), V2_TO_V1_PROPERTYDESCRIPTION));
         List<V1Extension> extensions = V2V1Converter.transform(modelDescription.getExtensions(), V2_TO_V1_EXTENSION);
         if (extensions != null)
         {
            result.getExtensions().addAll(extensions);
         }
         result.setModelTypes(toV1ModelTypes(modelDescription.getModelTypes()));

         return result;
      }
      else
      {
         return null;
      }
   }

   public static V1ModelTypes toV1ModelTypes(ModelTypes modelTypes)
   {
      if (modelTypes != null)
      {
         V1ModelTypes result = new V1ModelTypes();
         result.setAny(modelTypes.getAny());
         return result;
      }
      else
      {
         return null;
      }
   }

   public static V1ResourceList toV1ResourceList(ResourceList resourceList)
   {
      if (resourceList != null)
      {
         V1ResourceList result = new V1ResourceList();
         List<V1Extension> extensions = V2V1Converter.transform(resourceList.getExtensions(), V2_TO_V1_EXTENSION);
         if (extensions != null)
         {
            result.getExtensions().addAll(extensions);
         }
         List<V1Resource> v1Resources = V2V1Converter.transform(resourceList.getResources(), V2_TO_V1_RESOURCE);
         if (v1Resources != null)
         {
            result.getResources().addAll(v1Resources);
         }
         return result;
      }
      else
      {
         return null;
      }
   }

   public static InteractionParams toV2InteractionParams(V1InteractionParams v1InteractionParams)
   {
      if (v1InteractionParams != null)
      {
         InteractionParams interactionParams = WSRPTypeFactory.createInteractionParams(toV2StateChange(v1InteractionParams.getPortletStateChange()));
         interactionParams.setInteractionState(v1InteractionParams.getInteractionState());
         interactionParams.getExtensions().addAll(Lists.transform(v1InteractionParams.getExtensions(), V1_TO_V2_EXTENSION));
         interactionParams.getFormParameters().addAll(Lists.transform(v1InteractionParams.getFormParameters(), V1_TO_V2_NAMEDSTRING));
         interactionParams.getUploadContexts().addAll(Lists.transform(v1InteractionParams.getUploadContexts(), V1_TO_V2_UPLOADCONTEXT));

         return interactionParams;
      }
      else
      {
         return null;
      }
   }
   
   public static V1InteractionParams toV1InteractionParams(InteractionParams interactionParams)
   {
      if (interactionParams != null)
      {
         V1InteractionParams v1InteractionParams = WSRP1TypeFactory.createInteractionParams(toV1StateChange(interactionParams.getPortletStateChange()));
         v1InteractionParams.setInteractionState(interactionParams.getInteractionState());
         
         List<Extension> extensions = interactionParams.getExtensions();
         if (extensions != null)
         {
            v1InteractionParams.getExtensions().addAll(Lists.transform(extensions, V2_TO_V1_EXTENSION));
         }
         
         List<NamedString> formParameters = interactionParams.getFormParameters();
         if (formParameters != null)
         {
            v1InteractionParams.getFormParameters().addAll(Lists.transform(formParameters, V2_TO_V1_NAMEDSTRING));
         }
         
         List<UploadContext> uploadContext = interactionParams.getUploadContexts();
         if (uploadContext != null)
         {
            v1InteractionParams.getUploadContexts().addAll(Lists.transform(uploadContext, V2_TO_V1_UPLOADCONTEXT));
         }
         
         return v1InteractionParams;
      }
      else
      {
         return null;
      }
   }
   

   public static StateChange toV2StateChange(V1StateChange v1StateChange)
   {
      if (v1StateChange != null)
      {
         return StateChange.valueOf((v1StateChange.value()));
      }
      else
      {
         return null;
      }
   }
   
   public static V1StateChange toV1StateChange(StateChange stateChange)
   {
      if (stateChange != null)
      {
         return V1StateChange.valueOf(stateChange.value());
      }
      else
      {
         return null;
      }
   }

   public static V1UpdateResponse toV1UpdateResponse(UpdateResponse updateResponse)
   {
      if (updateResponse != null)
      {
         V1UpdateResponse v1UpdateResponse = WSRP1TypeFactory.createUpdateResponse();
         v1UpdateResponse.setMarkupContext(toV1MarkupContext(updateResponse.getMarkupContext()));
         v1UpdateResponse.setNavigationalState(updateResponse.getNavigationalContext().getOpaqueValue());
         v1UpdateResponse.setNewWindowState(updateResponse.getNewWindowState());
         v1UpdateResponse.setPortletContext(toV1PortletContext(updateResponse.getPortletContext()));
         v1UpdateResponse.setSessionContext(toV1SessionContext(updateResponse.getSessionContext()));
         v1UpdateResponse.setNewMode(updateResponse.getNewMode());
         return v1UpdateResponse;
      }
      else
      {
         return null;
      }
   }

   public static V1LocalizedString toV1LocalizedString(LocalizedString localizedString)
   {
      return V2_TO_V1_LOCALIZEDSTRING.apply(localizedString);
   }

   public static V1SessionContext toV1SessionContext(SessionContext sessionContext)
   {
      if (sessionContext != null)
      {
         V1SessionContext v1SessionContext = WSRP1TypeFactory.createSessionContext(sessionContext.getSessionID(), sessionContext.getExpires().intValue());
         v1SessionContext.getExtensions().addAll(Lists.transform(sessionContext.getExtensions(), V2_TO_V1_EXTENSION));

         return v1SessionContext;
      }
      else
      {
         return null;
      }
   }

   public static LocalizedString toV2LocalizedString(V1LocalizedString localizedString)
   {
      return V1_TO_V2_LOCALIZEDSTRING.apply(localizedString);
   }

   public static CookieProtocol toV2CookieProtocol(V1CookieProtocol v1CookieProtocol)
   {
      if (v1CookieProtocol != null)
      {
         return CookieProtocol.fromValue(v1CookieProtocol.value());
      }
      else
      {
         return null;
      }
   }

   public static ModelDescription toV2ModelDescription(V1ModelDescription v1ModelDescription)
   {
      if (v1ModelDescription != null)
      {
         ModelDescription result = WSRPTypeFactory.createModelDescription(V2V1Converter.transform(v1ModelDescription.getPropertyDescriptions(), V1_TO_V2_PROPERTYDESCRIPTION));
         List<Extension> extensions = V2V1Converter.transform(v1ModelDescription.getExtensions(), V1_TO_V2_EXTENSION);
         if (extensions != null)
         {
            result.getExtensions().addAll(extensions);
         }
         result.setModelTypes(toV2ModelTypes(v1ModelDescription.getModelTypes()));

         return result;
      }
      else
      {
         return null;
      }
   }

   private static ModelTypes toV2ModelTypes(V1ModelTypes modelTypes)
   {
      if (modelTypes != null)
      {
         ModelTypes result = new ModelTypes();
         result.setAny(modelTypes.getAny());
         return result;
      }
      else
      {
         return null;
      }
   }

   public static ResourceList toV2ResourceList(V1ResourceList v1ResourceList)
   {
      if (v1ResourceList != null)
      {
         ResourceList result = new ResourceList();

         List<Extension> extensions = V2V1Converter.transform(v1ResourceList.getExtensions(), V1_TO_V2_EXTENSION);
         if (extensions != null)
         {
            result.getExtensions().addAll(extensions);
         }
         List<Resource> resources = V2V1Converter.transform(v1ResourceList.getResources(), V1_TO_V2_RESOURCE);
         if (resources != null)
         {
            result.getResources().addAll(resources);
         }

         return result;
      }
      else
      {
         return null;
      }
   }

   private static class V1ToV2Extension implements Function<V1Extension, Extension>
   {
      public Extension apply(V1Extension from)
      {
         if (from != null)
         {
            Extension extension = new Extension();
            extension.setAny(from.getAny());
            return extension;
         }
         else
         {
            return null;
         }
      }
   }

   private static class V2ToV1Extension implements Function<Extension, V1Extension>
   {
      public V1Extension apply(Extension from)
      {
         if (from != null)
         {
            V1Extension extension = new V1Extension();
            extension.setAny(from.getAny());
            return extension;
         }
         else
         {
            return null;
         }
      }
   }

   private static class V2ToV1PortletDescription implements Function<PortletDescription, V1PortletDescription>
   {

      public V1PortletDescription apply(PortletDescription from)
      {
         if (from != null)
         {
            V1PortletDescription result = WSRP1TypeFactory.createPortletDescription(from.getPortletHandle(),
               V2V1Converter.transform(from.getMarkupTypes(), V2_TO_V1_MARKUPTYPE));
            result.setDescription(V2_TO_V1_LOCALIZEDSTRING.apply(from.getDescription()));
            result.setDisplayName(V2_TO_V1_LOCALIZEDSTRING.apply(from.getDisplayName()));
            List<V1Extension> extensions = V2V1Converter.transform(from.getExtensions(), V2_TO_V1_EXTENSION);
            if (extensions != null)
            {
               result.getExtensions().addAll(extensions);
            }
            List<V1LocalizedString> keywords = V2V1Converter.transform(from.getKeywords(), V2_TO_V1_LOCALIZEDSTRING);
            if (keywords != null)
            {
               result.getKeywords().addAll(keywords);
            }
            List<String> userCategories = from.getUserCategories();
            if (userCategories != null)
            {
               result.getUserCategories().addAll(userCategories);
            }
            List<String> userProfileItems = from.getUserProfileItems();
            if (userProfileItems != null)
            {
               result.getUserProfileItems().addAll(userProfileItems);
            }
            result.setDefaultMarkupSecure(from.isDefaultMarkupSecure());
            result.setDoesUrlTemplateProcessing(from.isDoesUrlTemplateProcessing());
            result.setTemplatesStoredInSession(from.isTemplatesStoredInSession());
            result.setHasUserSpecificState(from.isHasUserSpecificState());
            result.setOnlySecure(from.isOnlySecure());
            result.setUserContextStoredInSession(from.isUserContextStoredInSession());
            result.setUsesMethodGet(from.isUsesMethodGet());
            result.setShortTitle(V2_TO_V1_LOCALIZEDSTRING.apply(from.getShortTitle()));
            result.setTitle(V2_TO_V1_LOCALIZEDSTRING.apply(from.getTitle()));

            result.setGroupID(from.getGroupID());
            return result;
         }
         else
         {
            return null;
         }
      }
   }

   private static class V1ToV2PortletDescription implements Function<V1PortletDescription, PortletDescription>
   {

      public PortletDescription apply(V1PortletDescription from)
      {
         if (from != null)
         {
            PortletDescription result = WSRPTypeFactory.createPortletDescription(from.getPortletHandle(),
               V2V1Converter.transform(from.getMarkupTypes(), V1_TO_V2_MARKUPTYPE));
            result.setDescription(V1_TO_V2_LOCALIZEDSTRING.apply(from.getDescription()));
            result.setDisplayName(V1_TO_V2_LOCALIZEDSTRING.apply(from.getDisplayName()));
            List<Extension> extensions = V2V1Converter.transform(from.getExtensions(), V1_TO_V2_EXTENSION);
            if (extensions != null)
            {
               result.getExtensions().addAll(extensions);
            }
            List<LocalizedString> keywords = V2V1Converter.transform(from.getKeywords(), V1_TO_V2_LOCALIZEDSTRING);
            if (keywords != null)
            {
               result.getKeywords().addAll(keywords);
            }
            List<String> userCategories = from.getUserCategories();
            if (userCategories != null)
            {
               result.getUserCategories().addAll(userCategories);
            }
            List<String> userProfileItems = from.getUserProfileItems();
            if (userProfileItems != null)
            {
               result.getUserProfileItems().addAll(userProfileItems);
            }
            result.setDefaultMarkupSecure(from.isDefaultMarkupSecure());
            result.setDoesUrlTemplateProcessing(from.isDoesUrlTemplateProcessing());
            result.setTemplatesStoredInSession(from.isTemplatesStoredInSession());
            result.setHasUserSpecificState(from.isHasUserSpecificState());
            result.setOnlySecure(from.isOnlySecure());
            result.setUserContextStoredInSession(from.isUserContextStoredInSession());
            result.setUsesMethodGet(from.isUsesMethodGet());
            result.setShortTitle(V1_TO_V2_LOCALIZEDSTRING.apply(from.getShortTitle()));
            result.setTitle(V1_TO_V2_LOCALIZEDSTRING.apply(from.getTitle()));

            result.setGroupID(from.getGroupID());
            return result;
         }
         else
         {
            return null;
         }
      }
   }

   private static class V2ToV1ItemDescription implements Function<ItemDescription, V1ItemDescription>
   {

      public V1ItemDescription apply(ItemDescription from)
      {
         if (from != null)
         {
            V1ItemDescription result = new V1ItemDescription();
            result.setItemName(from.getItemName());
            result.setDescription(V2_TO_V1_LOCALIZEDSTRING.apply(from.getDescription()));
            List<V1Extension> extensions = V2V1Converter.transform(from.getExtensions(), V2_TO_V1_EXTENSION);
            if (extensions != null)
            {
               result.getExtensions().addAll(extensions);
            }
            return result;
         }
         else
         {
            return null;
         }
      }
   }

   private static class V1ToV2ItemDescription implements Function<V1ItemDescription, ItemDescription>
   {

      public ItemDescription apply(V1ItemDescription from)
      {
         if (from != null)
         {
            ItemDescription result = new ItemDescription();
            result.setItemName(from.getItemName());
            result.setDescription(V1_TO_V2_LOCALIZEDSTRING.apply(from.getDescription()));
            List<Extension> extensions = V2V1Converter.transform(from.getExtensions(), V1_TO_V2_EXTENSION);
            if (extensions != null)
            {
               result.getExtensions().addAll(extensions);
            }
            return result;
         }
         else
         {
            return null;
         }
      }
   }


   public static class V1ToV2NamedString implements Function<V1NamedString, NamedString>
   {
      public NamedString apply(V1NamedString v1NamedString)
      {
         if (v1NamedString != null)
         {
            NamedString result = new NamedString();
            result.setName(v1NamedString.getName());
            result.setValue(v1NamedString.getValue());
            return result;
         }
         else
         {
            return null;
         }
      }
   }

   public static class V2ToV1NamedString implements Function<NamedString, V1NamedString>
   {
      public V1NamedString apply(NamedString namedString)
      {
         if (namedString != null)
         {
            V1NamedString result = new V1NamedString();
            result.setName(namedString.getName());
            result.setValue(namedString.getValue());
            return result;
         }
         else
         {
            return null;
         }
      }
   }
   
   public static class V1ToV2UploadContext implements Function<V1UploadContext, UploadContext>
   {

      public UploadContext apply(V1UploadContext v1UploadContext)
      {
         if (v1UploadContext != null)
         {
            UploadContext result = WSRPTypeFactory.createUploadContext(v1UploadContext.getMimeType(), v1UploadContext.getUploadData());
            result.getExtensions().addAll(Lists.transform(v1UploadContext.getExtensions(), V1_TO_V2_EXTENSION));
            result.getMimeAttributes().addAll(Lists.transform(v1UploadContext.getMimeAttributes(), V1_TO_V2_NAMEDSTRING));

            return result;
         }
         else
         {
            return null;
         }
      }

   }
   
   public static class V2ToV1UploadContext implements Function<UploadContext, V1UploadContext>
   {

      public V1UploadContext apply(UploadContext uploadContext)
      {
         if (uploadContext != null)
         {
            V1UploadContext result = WSRP1TypeFactory.createUploadContext(uploadContext.getMimeType(), uploadContext.getUploadData());
            
            List<Extension> extensions = uploadContext.getExtensions();
            if (extensions != null)
            {
               result.getExtensions().addAll(Lists.transform(extensions, V2_TO_V1_EXTENSION));
            }
            
            List<NamedString> mimeAttributes = uploadContext.getMimeAttributes();
            if (mimeAttributes != null)
            {
               result.getMimeAttributes().addAll(Lists.transform(mimeAttributes, V2_TO_V1_NAMEDSTRING));
            }
            
            return result;
         }
         else
         {
            return null;
         }
      }
      
   }

   private static class V2ToV1MarkupType implements Function<MarkupType, V1MarkupType>
   {

      public V1MarkupType apply(MarkupType from)
      {
         if (from != null)
         {
            V1MarkupType result = WSRP1TypeFactory.createMarkupType(from.getMimeType(), from.getModes(), from.getWindowStates(), from.getLocales());
            List<V1Extension> extensions = V2V1Converter.transform(from.getExtensions(), V2_TO_V1_EXTENSION);
            if (extensions != null)
            {
               result.getExtensions().addAll(extensions);
            }

            return result;
         }
         else
         {
            return null;
         }
      }
   }

   private static class V1ToV2MarkupType implements Function<V1MarkupType, MarkupType>
   {

      public MarkupType apply(V1MarkupType from)
      {
         if (from != null)
         {
            MarkupType result = WSRPTypeFactory.createMarkupType(from.getMimeType(), from.getModes(), from.getWindowStates(), from.getLocales());
            List<Extension> extensions = V2V1Converter.transform(from.getExtensions(), V1_TO_V2_EXTENSION);
            if (extensions != null)
            {
               result.getExtensions().addAll(extensions);
            }

            return result;
         }
         else
         {
            return null;
         }
      }
   }

   private static class V2ToV1LocalizedString implements Function<LocalizedString, V1LocalizedString>
   {

      public V1LocalizedString apply(LocalizedString from)
      {
         if (from != null)
         {
            return WSRP1TypeFactory.createLocalizedString(from.getLang(), from.getResourceName(), from.getValue());
         }
         else
         {
            return null;
         }

      }
   }

   private static class V1ToV2LocalizedString implements Function<V1LocalizedString, LocalizedString>
   {

      public LocalizedString apply(V1LocalizedString from)
      {
         if (from != null)
         {
            return WSRPTypeFactory.createLocalizedString(from.getLang(), from.getResourceName(), from.getValue());
         }
         else
         {
            return null;
         }

      }
   }

   private static class V2ToV1PropertyDescription implements Function<PropertyDescription, V1PropertyDescription>
   {

      public V1PropertyDescription apply(PropertyDescription from)
      {
         if (from != null)
         {
            V1PropertyDescription result = WSRP1TypeFactory.createPropertyDescription(from.getName().toString(), from.getType());
            result.setHint(toV1LocalizedString(from.getHint()));
            result.setLabel(toV1LocalizedString(from.getLabel()));
            List<V1Extension> extensions = V2V1Converter.transform(from.getExtensions(), V2_TO_V1_EXTENSION);
            if (extensions != null)
            {
               result.getExtensions().addAll(extensions);
            }

            return result;
         }
         else
         {
            return null;
         }
      }
   }

   private static class V2ToV1Resource implements Function<Resource, V1Resource>
   {
      public V1Resource apply(Resource from)
      {
         if (from != null)
         {
            V1Resource result = new V1Resource();
            result.setResourceName(from.getResourceName());
            List<V1ResourceValue> values = V2V1Converter.transform(from.getValues(), V2_TO_V1_RESOURCEVALUE);
            if (values != null)
            {
               result.getValues().addAll(values);
            }

            return result;
         }
         else
         {
            return null;
         }
      }

   }

   private static class V2ToV1ResourceValue implements Function<ResourceValue, V1ResourceValue>
   {
      public V1ResourceValue apply(ResourceValue from)
      {
         if (from != null)
         {
            V1ResourceValue result = new V1ResourceValue();
            result.setLang(from.getLang());
            result.setValue(from.getValue());
            List<V1Extension> extensions = V2V1Converter.transform(from.getExtensions(), V2_TO_V1_EXTENSION);
            if (extensions != null)
            {
               result.getExtensions().addAll(extensions);
            }

            return result;
         }
         else
         {
            return null;
         }
      }
   }

   private static class V1ToV2Resource implements Function<V1Resource, Resource>
   {
      public Resource apply(V1Resource from)
      {
         if (from != null)
         {
            Resource result = new Resource();
            result.setResourceName(from.getResourceName());
            List<ResourceValue> values = V2V1Converter.transform(from.getValues(), V1_TO_V2_RESOURCEVALUE);
            if (values != null)
            {
               result.getValues().addAll(values);
            }

            return result;
         }
         else
         {
            return null;
         }
      }

   }

   private static class V1ToV2ResourceValue implements Function<V1ResourceValue, ResourceValue>
   {
      public ResourceValue apply(V1ResourceValue from)
      {
         if (from != null)
         {
            ResourceValue result = new ResourceValue();
            result.setLang(from.getLang());
            result.setValue(from.getValue());
            List<Extension> extensions = V2V1Converter.transform(from.getExtensions(), V1_TO_V2_EXTENSION);
            if (extensions != null)
            {
               result.getExtensions().addAll(extensions);
            }

            return result;
         }
         else
         {
            return null;
         }
      }
   }

   private static class V1ToV2PropertyDescription implements Function<V1PropertyDescription, PropertyDescription>
   {
      public PropertyDescription apply(V1PropertyDescription from)
      {
         if (from != null)
         {
            PropertyDescription result = WSRPTypeFactory.createPropertyDescription(from.getName(), from.getType());
            result.setHint(toV2LocalizedString(from.getHint()));
            result.setLabel(toV2LocalizedString(from.getLabel()));
            List<Extension> extensions = V2V1Converter.transform(from.getExtensions(), V1_TO_V2_EXTENSION);
            if (extensions != null)
            {
               result.getExtensions().addAll(extensions);
            }

            return result;
         }
         else
         {
            return null;
         }
      }
   }
}
