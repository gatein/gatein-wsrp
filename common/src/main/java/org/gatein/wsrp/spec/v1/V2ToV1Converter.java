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
import org.gatein.common.util.ParameterValidation;
import org.gatein.wsrp.WSRPUtils;
import org.oasis.wsrp.v1.V1BlockingInteractionResponse;
import org.oasis.wsrp.v1.V1CacheControl;
import org.oasis.wsrp.v1.V1ClientData;
import org.oasis.wsrp.v1.V1Contact;
import org.oasis.wsrp.v1.V1CookieProtocol;
import org.oasis.wsrp.v1.V1DestroyFailed;
import org.oasis.wsrp.v1.V1DestroyPortletsResponse;
import org.oasis.wsrp.v1.V1EmployerInfo;
import org.oasis.wsrp.v1.V1Extension;
import org.oasis.wsrp.v1.V1InteractionParams;
import org.oasis.wsrp.v1.V1ItemDescription;
import org.oasis.wsrp.v1.V1LocalizedString;
import org.oasis.wsrp.v1.V1MarkupContext;
import org.oasis.wsrp.v1.V1MarkupParams;
import org.oasis.wsrp.v1.V1MarkupResponse;
import org.oasis.wsrp.v1.V1MarkupType;
import org.oasis.wsrp.v1.V1ModelDescription;
import org.oasis.wsrp.v1.V1ModelTypes;
import org.oasis.wsrp.v1.V1NamedString;
import org.oasis.wsrp.v1.V1Online;
import org.oasis.wsrp.v1.V1PersonName;
import org.oasis.wsrp.v1.V1PortletContext;
import org.oasis.wsrp.v1.V1PortletDescription;
import org.oasis.wsrp.v1.V1PortletDescriptionResponse;
import org.oasis.wsrp.v1.V1PortletPropertyDescriptionResponse;
import org.oasis.wsrp.v1.V1Postal;
import org.oasis.wsrp.v1.V1Property;
import org.oasis.wsrp.v1.V1PropertyDescription;
import org.oasis.wsrp.v1.V1PropertyList;
import org.oasis.wsrp.v1.V1RegistrationContext;
import org.oasis.wsrp.v1.V1RegistrationData;
import org.oasis.wsrp.v1.V1RegistrationState;
import org.oasis.wsrp.v1.V1ResetProperty;
import org.oasis.wsrp.v1.V1Resource;
import org.oasis.wsrp.v1.V1ResourceList;
import org.oasis.wsrp.v1.V1ResourceValue;
import org.oasis.wsrp.v1.V1ReturnAny;
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
import org.oasis.wsrp.v2.BlockingInteractionResponse;
import org.oasis.wsrp.v2.CacheControl;
import org.oasis.wsrp.v2.ClientData;
import org.oasis.wsrp.v2.Contact;
import org.oasis.wsrp.v2.CookieProtocol;
import org.oasis.wsrp.v2.DestroyPortletsResponse;
import org.oasis.wsrp.v2.EmployerInfo;
import org.oasis.wsrp.v2.Extension;
import org.oasis.wsrp.v2.FailedPortlets;
import org.oasis.wsrp.v2.InteractionParams;
import org.oasis.wsrp.v2.ItemDescription;
import org.oasis.wsrp.v2.LocalizedString;
import org.oasis.wsrp.v2.MarkupContext;
import org.oasis.wsrp.v2.MarkupParams;
import org.oasis.wsrp.v2.MarkupResponse;
import org.oasis.wsrp.v2.MarkupType;
import org.oasis.wsrp.v2.ModelDescription;
import org.oasis.wsrp.v2.ModelTypes;
import org.oasis.wsrp.v2.NamedString;
import org.oasis.wsrp.v2.Online;
import org.oasis.wsrp.v2.PersonName;
import org.oasis.wsrp.v2.PortletContext;
import org.oasis.wsrp.v2.PortletDescription;
import org.oasis.wsrp.v2.PortletDescriptionResponse;
import org.oasis.wsrp.v2.PortletPropertyDescriptionResponse;
import org.oasis.wsrp.v2.Postal;
import org.oasis.wsrp.v2.Property;
import org.oasis.wsrp.v2.PropertyDescription;
import org.oasis.wsrp.v2.PropertyList;
import org.oasis.wsrp.v2.RegistrationContext;
import org.oasis.wsrp.v2.RegistrationData;
import org.oasis.wsrp.v2.RegistrationState;
import org.oasis.wsrp.v2.ResetProperty;
import org.oasis.wsrp.v2.Resource;
import org.oasis.wsrp.v2.ResourceList;
import org.oasis.wsrp.v2.ResourceValue;
import org.oasis.wsrp.v2.ReturnAny;
import org.oasis.wsrp.v2.RuntimeContext;
import org.oasis.wsrp.v2.ServiceDescription;
import org.oasis.wsrp.v2.SessionContext;
import org.oasis.wsrp.v2.SessionParams;
import org.oasis.wsrp.v2.StateChange;
import org.oasis.wsrp.v2.Telecom;
import org.oasis.wsrp.v2.TelephoneNum;
import org.oasis.wsrp.v2.Templates;
import org.oasis.wsrp.v2.UpdateResponse;
import org.oasis.wsrp.v2.UploadContext;
import org.oasis.wsrp.v2.UserContext;
import org.oasis.wsrp.v2.UserProfile;

import sun.reflect.generics.visitor.Reifier;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class V2ToV1Converter
{

   public static final V2ToV1Extension EXTENSION = new V2ToV1Extension();
   public static final V2ToV1MarkupType MARKUPTYPE = new V2ToV1MarkupType();
   public static final V2ToV1PortletDescription PORTLETDESCRIPTION = new V2ToV1PortletDescription();
   public static final V2ToV1LocalizedString LOCALIZEDSTRING = new V2ToV1LocalizedString();
   public static final V2ToV1ItemDescription ITEMDESCRIPTION = new V2ToV1ItemDescription();
   public static final V2ToV1PropertyDescription PROPERTYDESCRIPTION = new V2ToV1PropertyDescription();
   public static final V2ToV1Resource RESOURCE = new V2ToV1Resource();
   public static final V2ToV1ResourceValue RESOURCEVALUE = new V2ToV1ResourceValue();
   public static final V2ToV1NamedString NAMEDSTRING = new V2ToV1NamedString();
   public static final V2ToV1UploadContext UPLOADCONTEXT = new V2ToV1UploadContext();
   public static final V2ToV1Property PROPERTY = new V2ToV1Property();
   public static final V2ToV1ResetProperty RESETPROPERTY = new V2ToV1ResetProperty();
   public static final V2ToV1FailedPortlets FAILEDPORTLET = new V2ToV1FailedPortlets();


   public static V1PortletContext toV1PortletContext(PortletContext portletContext)
   {
      if (portletContext != null)
      {
         V1PortletContext v1PortletContext = WSRP1TypeFactory.createPortletContext(portletContext.getPortletHandle(), portletContext.getPortletState());

         List<V1Extension> extensions = WSRPUtils.transform(portletContext.getExtensions(), EXTENSION);
         if (extensions != null)
         {
            v1PortletContext.getExtensions().addAll(extensions);
         }

         return v1PortletContext;
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

         List<V1Extension> extensions = WSRPUtils.transform(markupParams.getExtensions(), EXTENSION);
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

   private static V1ClientData toV1ClientData(ClientData clientData)
   {
      if (clientData != null)
      {
         V1ClientData v1ClientData = WSRP1TypeFactory.createClientData(clientData.getUserAgent());

         List<Extension> extensions = clientData.getExtensions();
         if (extensions != null)
         {
            v1ClientData.getExtensions().addAll(Lists.transform(extensions, EXTENSION));
         }

         return v1ClientData;
      }
      else
      {
         return null;
      }
   }

   public static V1RuntimeContext toV1RuntimeContext(RuntimeContext runtimeContext)
   {
      if (runtimeContext != null)
      {
         V1RuntimeContext v1RuntimeContext = WSRP1TypeFactory.createRuntimeContext(runtimeContext.getUserAuthentication());
         v1RuntimeContext.setNamespacePrefix(runtimeContext.getNamespacePrefix());
         v1RuntimeContext.setPortletInstanceKey(runtimeContext.getPortletInstanceKey());
         SessionParams sessionParams = runtimeContext.getSessionParams();
         if (sessionParams != null)
         {
            v1RuntimeContext.setSessionID(sessionParams.getSessionID());
         }
         v1RuntimeContext.setTemplates(V2ToV1Converter.toV1Templates(runtimeContext.getTemplates()));

         List<Extension> extensions = runtimeContext.getExtensions();
         if (extensions != null)
         {
            v1RuntimeContext.getExtensions().addAll(WSRPUtils.transform(extensions, EXTENSION));
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
            v1Templates.getExtensions().addAll(WSRPUtils.transform(extensions, EXTENSION));
         }

         return v1Templates;
      }
      else
      {
         return null;
      }
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
            v1UserContext.getExtensions().addAll(WSRPUtils.transform(extensions, EXTENSION));
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

   public static V1UserProfile toV1UserProfile(UserProfile userProfile)
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

   public static V1EmployerInfo toV1EmployerInfo(EmployerInfo employerInfo)
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
            v1EmployerInfo.getExtensions().addAll(WSRPUtils.transform(extensions, EXTENSION));
         }

         return v1EmployerInfo;
      }
      else
      {
         return null;
      }
   }

   public static V1PersonName toV1PersonName(PersonName personName)
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
            v1PersonName.getExtensions().addAll(WSRPUtils.transform(extensions, EXTENSION));
         }

         return v1PersonName;
      }
      else
      {
         return null;
      }
   }

   public static V1Contact toV1Context(Contact contact)
   {
      if (contact != null)
      {
         V1Contact v1Contact = new V1Contact();
         v1Contact.setOnline(toV1Online(contact.getOnline()));
         v1Contact.setPostal(toV1Postal(contact.getPostal()));
         v1Contact.setTelecom(toV1Telecom(contact.getTelecom()));

         List<Extension> extensions = contact.getExtensions();
         if (extensions != null)
         {
            v1Contact.getExtensions().addAll(WSRPUtils.transform(extensions, EXTENSION));
         }

         return v1Contact;
      }
      else
      {
         return null;
      }
   }

   public static V1Online toV1Online(Online online)
   {
      if (online != null)
      {
         V1Online v1Online = new V1Online();
         v1Online.setEmail(online.getEmail());
         v1Online.setUri(online.getUri());

         List<Extension> extensions = online.getExtensions();
         if (extensions != null)
         {
            v1Online.getExtensions().addAll(WSRPUtils.transform(extensions, EXTENSION));
         }

         return v1Online;
      }
      else
      {
         return null;
      }
   }

   public static V1Postal toV1Postal(Postal postal)
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
            v1Postal.getExtensions().addAll(WSRPUtils.transform(extensions, EXTENSION));
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
            v1Telecom.getExtensions().addAll(WSRPUtils.transform(extensions, EXTENSION));
         }

         return v1Telecom;
      }
      else
      {
         return null;
      }
   }

   public static V1TelephoneNum toV1TelephoneNum(TelephoneNum telephoneNum)
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
            v1TelephoneNum.getExtensions().addAll(WSRPUtils.transform(extensions, EXTENSION));
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
      if (markupContext != null)
      {
         byte[] binary = markupContext.getItemBinary();
         String string = markupContext.getItemString();
         V1MarkupContext v1MarkupContext;
         if (string != null)
         {
            v1MarkupContext = WSRP1TypeFactory.createMarkupContext(markupContext.getMimeType(), string);
         }
         else
         {
            v1MarkupContext = WSRP1TypeFactory.createMarkupContext(markupContext.getMimeType(), binary);
         }
         v1MarkupContext.setCacheControl(toV1CacheControl(markupContext.getCacheControl()));
         v1MarkupContext.setLocale(markupContext.getLocale());
         v1MarkupContext.setPreferredTitle(markupContext.getPreferredTitle());
         v1MarkupContext.setRequiresUrlRewriting(markupContext.isRequiresRewriting());
         v1MarkupContext.setUseCachedMarkup(markupContext.isUseCachedItem());

         List<Extension> extensions = markupContext.getExtensions();
         if (extensions != null)
         {
            v1MarkupContext.getExtensions().addAll(WSRPUtils.transform(extensions, EXTENSION));
         }

         return v1MarkupContext;
      }
      else
      {
         return null;
      }
   }

   public static V1CacheControl toV1CacheControl(CacheControl cacheControl)
   {
      if (cacheControl != null)
      {
         V1CacheControl v1CacheControl = WSRP1TypeFactory.createCacheControl(cacheControl.getExpires(), cacheControl.getUserScope());
         v1CacheControl.setValidateTag(cacheControl.getValidateTag());

         List<Extension> extensions = cacheControl.getExtensions();
         if (extensions != null)
         {
            v1CacheControl.getExtensions().addAll(WSRPUtils.transform(extensions, EXTENSION));
         }

         return v1CacheControl;
      }
      else
      {
         return null;
      }
   }

   public static V1RegistrationContext toV1RegistrationContext(RegistrationContext registrationContext)
   {
      if (registrationContext != null)
      {
         V1RegistrationContext result = WSRP1TypeFactory.createRegistrationContext(registrationContext.getRegistrationHandle());
         result.setRegistrationState(registrationContext.getRegistrationState());
         List<V1Extension> extensions = WSRPUtils.transform(registrationContext.getExtensions(), EXTENSION);
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

   public static <E extends Exception> E toV1Exception(Class<E> v1ExceptionClass, Exception v2Exception)
   {
      if (!"org.oasis.wsrp.v1".equals(v1ExceptionClass.getPackage().getName()))
      {
         throw new IllegalArgumentException("Specified exception class is not a WSRP 1 exception: " + v1ExceptionClass);
      }

      Class<? extends Exception> v2ExceptionClass = v2Exception.getClass();
      String v2Name = v2ExceptionClass.getSimpleName();
      if (!"org.oasis.wsrp.v2".equals(v2ExceptionClass.getPackage().getName()))
      {
         throw new IllegalArgumentException("Specified exception is not a WSRP 2 exception: " + v2Exception);
      }

      String v1Name = v1ExceptionClass.getSimpleName();
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
         V1ModelDescription result = WSRP1TypeFactory.createModelDescription(WSRPUtils.transform(modelDescription.getPropertyDescriptions(), PROPERTYDESCRIPTION));
         List<V1Extension> extensions = WSRPUtils.transform(modelDescription.getExtensions(), EXTENSION);
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
         List<V1Extension> extensions = WSRPUtils.transform(resourceList.getExtensions(), EXTENSION);
         if (extensions != null)
         {
            result.getExtensions().addAll(extensions);
         }
         List<V1Resource> v1Resources = WSRPUtils.transform(resourceList.getResources(), RESOURCE);
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

   public static V1InteractionParams toV1InteractionParams(InteractionParams interactionParams)
   {
      if (interactionParams != null)
      {
         V1InteractionParams v1InteractionParams = WSRP1TypeFactory.createInteractionParams(toV1StateChange(interactionParams.getPortletStateChange()));
         v1InteractionParams.setInteractionState(interactionParams.getInteractionState());

         List<Extension> extensions = interactionParams.getExtensions();
         if (extensions != null)
         {
            v1InteractionParams.getExtensions().addAll(Lists.transform(extensions, EXTENSION));
         }

         List<NamedString> formParameters = interactionParams.getFormParameters();
         if (formParameters != null)
         {
            v1InteractionParams.getFormParameters().addAll(Lists.transform(formParameters, NAMEDSTRING));
         }

         List<UploadContext> uploadContext = interactionParams.getUploadContexts();
         if (uploadContext != null)
         {
            v1InteractionParams.getUploadContexts().addAll(Lists.transform(uploadContext, UPLOADCONTEXT));
         }

         return v1InteractionParams;
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
         return V1StateChange.fromValue(stateChange.value());
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
      return LOCALIZEDSTRING.apply(localizedString);
   }

   public static V1SessionContext toV1SessionContext(SessionContext sessionContext)
   {
      if (sessionContext != null)
      {
         V1SessionContext v1SessionContext = WSRP1TypeFactory.createSessionContext(sessionContext.getSessionID(), sessionContext.getExpires());
         v1SessionContext.getExtensions().addAll(Lists.transform(sessionContext.getExtensions(), EXTENSION));

         return v1SessionContext;
      }
      else
      {
         return null;
      }
   }

   public static V1PortletDescription toV1PortletDescription(PortletDescription description)
   {
      return PORTLETDESCRIPTION.apply(description);
   }

   public static List<V1DestroyFailed> toV1DestroyFailed(List<FailedPortlets> failedPortletsList)
   {
      if (failedPortletsList != null)
      {
         List<V1DestroyFailed> result = new ArrayList<V1DestroyFailed>(failedPortletsList.size());

         for (FailedPortlets failedPortlets : failedPortletsList)
         {
            QName errorCode = failedPortlets.getErrorCode();
            V1LocalizedString reason = toV1LocalizedString(failedPortlets.getReason());
            String v1Reason = errorCode.toString() + ": " + reason.getValue();
            for (String handle : failedPortlets.getPortletHandles())
            {
               V1DestroyFailed destroyFailed = WSRP1TypeFactory.createDestroyFailed(handle, v1Reason);
               result.add(destroyFailed);
            }
         }

         return result;
      }
      else
      {
         return null;
      }
   }

   public static V1PropertyList toV1PropertyList(PropertyList propertyList)
   {
      if (propertyList != null)
      {
         V1PropertyList result = new V1PropertyList();

         List<V1Property> properties = WSRPUtils.transform(propertyList.getProperties(), PROPERTY);
         if (properties != null)
         {
            result.getProperties().addAll(properties);
         }

         List<V1Extension> extensions = WSRPUtils.transform(propertyList.getExtensions(), EXTENSION);
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

   public static V1RegistrationData toV1RegistrationData(RegistrationData registrationData)
   {
      if (registrationData != null)
      {
         V1RegistrationData result = WSRP1TypeFactory.createRegistrationData(registrationData.getConsumerName(), registrationData.isMethodGetSupported());
         result.setConsumerAgent(registrationData.getConsumerAgent());

         List<V1Property> properties = WSRPUtils.transform(registrationData.getRegistrationProperties(), PROPERTY);
         if (properties != null)
         {
            result.getRegistrationProperties().addAll(properties);
         }
         List<String> modes = registrationData.getConsumerModes();
         if (ParameterValidation.existsAndIsNotEmpty(modes))
         {
            result.getConsumerModes().addAll(modes);
         }
         List<String> consumerUserScopes = registrationData.getConsumerUserScopes();
         if (ParameterValidation.existsAndIsNotEmpty(consumerUserScopes))
         {
            result.getConsumerUserScopes().addAll(consumerUserScopes);
         }
         List<String> windowStates = registrationData.getConsumerWindowStates();
         if (ParameterValidation.existsAndIsNotEmpty(windowStates))
         {
            result.getConsumerWindowStates().addAll(windowStates);
         }
         List<V1Extension> extensions = WSRPUtils.transform(registrationData.getExtensions(), EXTENSION);
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

   public static V1ServiceDescription toV1ServiceDescription(ServiceDescription serviceDescription)
   {
      if (serviceDescription != null)
      {
         V1ServiceDescription result = new V1ServiceDescription();
         result.setRegistrationPropertyDescription(toV1ModelDescription(serviceDescription.getRegistrationPropertyDescription()));
         result.setRequiresInitCookie(toV1CookieProtocol(serviceDescription.getRequiresInitCookie()));
         result.setRequiresRegistration(serviceDescription.isRequiresRegistration());
         result.setResourceList(toV1ResourceList(serviceDescription.getResourceList()));

         List<V1ItemDescription> modes = WSRPUtils.transform(serviceDescription.getCustomModeDescriptions(), ITEMDESCRIPTION);
         if (modes != null)
         {
            result.getCustomModeDescriptions().addAll(modes);
         }

         List<V1ItemDescription> windowStates = WSRPUtils.transform(serviceDescription.getCustomWindowStateDescriptions(), ITEMDESCRIPTION);
         if (windowStates != null)
         {
            result.getCustomWindowStateDescriptions().addAll(windowStates);
         }

         List<V1Extension> extensions = WSRPUtils.transform(serviceDescription.getExtensions(), EXTENSION);
         if (extensions != null)
         {
            result.getExtensions().addAll(extensions);
         }

         List<String> locales = result.getLocales();
         if (ParameterValidation.existsAndIsNotEmpty(locales))
         {
            result.getLocales().addAll(locales);
         }

         List<V1ItemDescription> userCategories = WSRPUtils.transform(serviceDescription.getUserCategoryDescriptions(), ITEMDESCRIPTION);
         if (userCategories != null)
         {
            result.getUserCategoryDescriptions().addAll(userCategories);
         }

         List<V1PortletDescription> portletDescriptions = WSRPUtils.transform(serviceDescription.getOfferedPortlets(), PORTLETDESCRIPTION);
         if (portletDescriptions != null)
         {
            result.getOfferedPortlets().addAll(portletDescriptions);
         }

         return result;
      }
      else
      {
         return null;
      }
   }

   public static V1MarkupResponse toV1MarkupResponse(MarkupResponse markupResponse)
   {
      if (markupResponse != null)
      {
         V1MarkupResponse result = WSRP1TypeFactory.createMarkupResponse(toV1MarkupContext(markupResponse.getMarkupContext()));
         result.setSessionContext(toV1SessionContext(markupResponse.getSessionContext()));

         List<V1Extension> extensions = WSRPUtils.transform(markupResponse.getExtensions(), EXTENSION);
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

   public static V1ReturnAny toV1ReturnAny(ReturnAny returnAny)
   {
      if (returnAny != null)
      {
         V1ReturnAny result = new V1ReturnAny();

         List<V1Extension> extensions = WSRPUtils.transform(returnAny.getExtensions(), EXTENSION);
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

   public static V1RegistrationState toV1RegistrationState(RegistrationState registrationState)
   {
      if (registrationState != null)
      {
         V1RegistrationState result = new V1RegistrationState();
         result.setRegistrationState(registrationState.getRegistrationState());

         List<V1Extension> extensions = WSRPUtils.transform(registrationState.getExtensions(), EXTENSION);
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

   public static V1BlockingInteractionResponse toV1BlockingInteractionResponse(BlockingInteractionResponse blockingInteractionResponse)
   {
      if (blockingInteractionResponse != null)
      {
         V1BlockingInteractionResponse result;
         V1UpdateResponse updateResponse = toV1UpdateResponse(blockingInteractionResponse.getUpdateResponse());
         String redirectURL = blockingInteractionResponse.getRedirectURL();
         if (redirectURL != null)
         {
            result = WSRP1TypeFactory.createBlockingInteractionResponse(redirectURL);
         }
         else
         {
            result = WSRP1TypeFactory.createBlockingInteractionResponse(updateResponse);
         }
         
         List<V1Extension> extensions = WSRPUtils.transform(blockingInteractionResponse.getExtensions(), EXTENSION);
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

   public static V1DestroyPortletsResponse toV1DestroyPortlesResponse(DestroyPortletsResponse destroyPortletResponse)
   {
      if (destroyPortletResponse != null)
      {

         List<V1DestroyFailed> destroyedFailed = WSRPUtils.transform(destroyPortletResponse.getFailedPortlets(), FAILEDPORTLET);
         V1DestroyPortletsResponse result = WSRP1TypeFactory.createDestroyPortletsResponse(destroyedFailed);

         List<V1Extension> extensions = WSRPUtils.transform(destroyPortletResponse.getExtensions(), EXTENSION);
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

   public static V1PortletPropertyDescriptionResponse toV1PortletPropertyDescriptionResponse(PortletPropertyDescriptionResponse portletPropertyDescriptionResponse)
   {
      if (portletPropertyDescriptionResponse != null)
      {
         //todo use WSRP1TypeFactory instead
         V1PortletPropertyDescriptionResponse result = new V1PortletPropertyDescriptionResponse();
         result.setModelDescription(toV1ModelDescription(portletPropertyDescriptionResponse.getModelDescription()));
         result.setResourceList(toV1ResourceList(portletPropertyDescriptionResponse.getResourceList()));

         List<V1Extension> extensions = WSRPUtils.transform(portletPropertyDescriptionResponse.getExtensions(), EXTENSION);
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

   public static V1PortletDescriptionResponse toV1PortletDescriptionResponse(PortletDescriptionResponse portletDescriptionResponse)
   {
      if (portletDescriptionResponse != null)
      {
         V1PortletDescriptionResponse result = WSRP1TypeFactory.createPortletDescriptionResponse(toV1PortletDescription(portletDescriptionResponse.getPortletDescription()));
         result.setResourceList(toV1ResourceList(portletDescriptionResponse.getResourceList()));
         
         List<V1Extension> extensions = WSRPUtils.transform(portletDescriptionResponse.getExtensions(), EXTENSION);
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
   
   public static V1PropertyDescription toV1PropertyDescription(PropertyDescription propertyDescription)
   {
      if (propertyDescription != null)
      {
         V1PropertyDescription result = WSRP1TypeFactory.createPropertyDescription(propertyDescription.getName().toString(), propertyDescription.getType());
         result.setHint(toV1LocalizedString(propertyDescription.getHint()));
         result.setLabel(toV1LocalizedString(propertyDescription.getLabel()));
         List<V1Extension> extensions = WSRPUtils.transform(propertyDescription.getExtensions(), EXTENSION);
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
               WSRPUtils.transform(from.getMarkupTypes(), MARKUPTYPE));
            result.setDescription(LOCALIZEDSTRING.apply(from.getDescription()));
            result.setDisplayName(LOCALIZEDSTRING.apply(from.getDisplayName()));
            List<V1Extension> extensions = WSRPUtils.transform(from.getExtensions(), EXTENSION);
            if (extensions != null)
            {
               result.getExtensions().addAll(extensions);
            }
            List<V1LocalizedString> keywords = WSRPUtils.transform(from.getKeywords(), LOCALIZEDSTRING);
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
            result.setShortTitle(LOCALIZEDSTRING.apply(from.getShortTitle()));
            result.setTitle(LOCALIZEDSTRING.apply(from.getTitle()));

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
            result.setDescription(LOCALIZEDSTRING.apply(from.getDescription()));
            List<V1Extension> extensions = WSRPUtils.transform(from.getExtensions(), EXTENSION);
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
               result.getExtensions().addAll(Lists.transform(extensions, EXTENSION));
            }

            List<NamedString> mimeAttributes = uploadContext.getMimeAttributes();
            if (mimeAttributes != null)
            {
               result.getMimeAttributes().addAll(Lists.transform(mimeAttributes, NAMEDSTRING));
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
            List<V1Extension> extensions = WSRPUtils.transform(from.getExtensions(), EXTENSION);
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

   private static class V2ToV1PropertyDescription implements Function<PropertyDescription, V1PropertyDescription>
   {

      public V1PropertyDescription apply(PropertyDescription from)
      {
         return toV1PropertyDescription(from);
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
            List<V1ResourceValue> values = WSRPUtils.transform(from.getValues(), RESOURCEVALUE);
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
            List<V1Extension> extensions = WSRPUtils.transform(from.getExtensions(), EXTENSION);
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

   private static class V2ToV1Property implements Function<Property, V1Property>
   {
      public V1Property apply(Property from)
      {
         if (from != null)
         {
            V1Property result = WSRP1TypeFactory.createProperty(from.getName().toString(), from.getLang(), from.getStringValue());
            List<Object> any = from.getAny();
            if (ParameterValidation.existsAndIsNotEmpty(any))
            {
               result.getAny().addAll(any);
            }

            return result;
         }
         else
         {
            return null;
         }
      }
   }

   private static class V2ToV1ResetProperty implements Function<ResetProperty, V1ResetProperty>
   {
      public V1ResetProperty apply(ResetProperty from)
      {
         if (from != null)
         {
            return WSRP1TypeFactory.createResetProperty(from.getName().toString());
         }
         else
         {
            return null;
         }
      }
   }

   private static class V2ToV1FailedPortlets implements Function<FailedPortlets, V1DestroyFailed>
   {
      public V1ResetProperty apply(ResetProperty from)
      {
         if (from != null)
         {
            return WSRP1TypeFactory.createResetProperty(from.getName().toString());
         }
         else
         {
            return null;
         }
      }

      public V1DestroyFailed apply(FailedPortlets from)
      {
         if (from != null)
         {
            return WSRP1TypeFactory.createDestroyFailed(from.getPortletHandles().get(0), from.getReason().toString());
         }
         return null;
      }
   }

}
