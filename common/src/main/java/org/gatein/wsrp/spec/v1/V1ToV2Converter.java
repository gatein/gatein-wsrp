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
import org.gatein.pc.api.OpaqueStateString;
import org.gatein.wsrp.WSRPConstants;
import org.gatein.wsrp.WSRPExceptionFactory;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.WSRPUtils;
import org.oasis.wsrp.v1.V1CacheControl;
import org.oasis.wsrp.v1.V1ClientData;
import org.oasis.wsrp.v1.V1Contact;
import org.oasis.wsrp.v1.V1CookieProtocol;
import org.oasis.wsrp.v1.V1DestroyFailed;
import org.oasis.wsrp.v1.V1EmployerInfo;
import org.oasis.wsrp.v1.V1Extension;
import org.oasis.wsrp.v1.V1GetServiceDescription;
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
import org.oasis.wsrp.v1.V1Property;
import org.oasis.wsrp.v1.V1PropertyDescription;
import org.oasis.wsrp.v1.V1PropertyList;
import org.oasis.wsrp.v1.V1RegistrationContext;
import org.oasis.wsrp.v1.V1RegistrationData;
import org.oasis.wsrp.v1.V1ResetProperty;
import org.oasis.wsrp.v1.V1Resource;
import org.oasis.wsrp.v1.V1ResourceList;
import org.oasis.wsrp.v1.V1ResourceValue;
import org.oasis.wsrp.v1.V1RuntimeContext;
import org.oasis.wsrp.v1.V1SessionContext;
import org.oasis.wsrp.v1.V1StateChange;
import org.oasis.wsrp.v1.V1Telecom;
import org.oasis.wsrp.v1.V1TelephoneNum;
import org.oasis.wsrp.v1.V1Templates;
import org.oasis.wsrp.v1.V1UpdateResponse;
import org.oasis.wsrp.v1.V1UploadContext;
import org.oasis.wsrp.v1.V1UserContext;
import org.oasis.wsrp.v1.V1UserProfile;
import org.oasis.wsrp.v2.CacheControl;
import org.oasis.wsrp.v2.ClientData;
import org.oasis.wsrp.v2.Contact;
import org.oasis.wsrp.v2.CookieProtocol;
import org.oasis.wsrp.v2.EmployerInfo;
import org.oasis.wsrp.v2.Extension;
import org.oasis.wsrp.v2.FailedPortlets;
import org.oasis.wsrp.v2.GetServiceDescription;
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
import org.oasis.wsrp.v2.Property;
import org.oasis.wsrp.v2.PropertyDescription;
import org.oasis.wsrp.v2.PropertyList;
import org.oasis.wsrp.v2.RegistrationContext;
import org.oasis.wsrp.v2.RegistrationData;
import org.oasis.wsrp.v2.ResetProperty;
import org.oasis.wsrp.v2.Resource;
import org.oasis.wsrp.v2.ResourceList;
import org.oasis.wsrp.v2.ResourceValue;
import org.oasis.wsrp.v2.RuntimeContext;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class V1ToV2Converter
{
   public static final V1ToV2Extension EXTENSION = new V1ToV2Extension();
   public static final V1ToV2MarkupType MARKUPTYPE = new V1ToV2MarkupType();
   public static final V1ToV2PortletDescription PORTLETDESCRIPTION = new V1ToV2PortletDescription();
   public static final V1ToV2LocalizedString LOCALIZEDSTRING = new V1ToV2LocalizedString();
   public static final V1ToV2ItemDescription ITEMDESCRIPTION = new V1ToV2ItemDescription();
   public static final V1ToV2PropertyDescription PROPERTYDESCRIPTION = new V1ToV2PropertyDescription();
   public static final V1ToV2Resource RESOURCE = new V1ToV2Resource();
   public static final V1ToV2ResourceValue RESOURCEVALUE = new V1ToV2ResourceValue();
   public static final V1ToV2NamedString NAMEDSTRING = new V1ToV2NamedString();
   public static final V1ToV2UploadContext UPLOADCONTEXT = new V1ToV2UploadContext();
   public static final V1ToV2Property PROPERTY = new V1ToV2Property();
   public static final V1ToV2ResetProperty RESETPROPERTY = new V1ToV2ResetProperty();

   public static MarkupParams toV2MarkupParams(V1MarkupParams v1MarkupParams)
   {
      if (v1MarkupParams != null)
      {
         MarkupParams markupParams = WSRPTypeFactory.createMarkupParams(v1MarkupParams.isSecureClientCommunication(),
            v1MarkupParams.getLocales(), v1MarkupParams.getMimeTypes(), v1MarkupParams.getMode(),
            v1MarkupParams.getWindowState());
         markupParams.setClientData(toV2ClientData(v1MarkupParams.getClientData()));

         // we can't create an opaquestatestring if with a null string, so need to check
         if (v1MarkupParams.getNavigationalState() != null)
         {
            markupParams.setNavigationalContext(WSRPTypeFactory.createNavigationalContextOrNull(
               new OpaqueStateString(v1MarkupParams.getNavigationalState()), null));
         }
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

         List<Extension> extensions = WSRPUtils.transform(v1MarkupParams.getExtensions(), EXTENSION);
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

   public static ClientData toV2ClientData(V1ClientData v1ClientData)
   {
      if (v1ClientData != null)
      {
         ClientData clientData = WSRPTypeFactory.createClientData(v1ClientData.getUserAgent());

         List<V1Extension> extensions = v1ClientData.getExtensions();
         if (extensions != null)
         {
            clientData.getExtensions().addAll(Lists.transform(extensions, EXTENSION));
         }

         return clientData;
      }
      else
      {
         return null;
      }
   }

   public static PortletContext toV2PortletContext(V1PortletContext v1PortletContext)
   {
      if (v1PortletContext != null)
      {
         PortletContext portletContext = WSRPTypeFactory.createPortletContext(v1PortletContext.getPortletHandle(), v1PortletContext.getPortletState());

         List<V1Extension> extensions = v1PortletContext.getExtensions();
         if (extensions != null)
         {
            portletContext.getExtensions().addAll(WSRPUtils.transform(extensions, EXTENSION));
         }

         return portletContext;
      }
      else
      {
         return null;
      }
   }

   public static RegistrationContext toV2RegistrationContext(V1RegistrationContext registrationContext)
   {
      if (registrationContext != null)
      {
         RegistrationContext result = WSRPTypeFactory.createRegistrationContext(registrationContext.getRegistrationHandle());
         result.setRegistrationState(registrationContext.getRegistrationState());
         List<Extension> extensions = WSRPUtils.transform(registrationContext.getExtensions(), EXTENSION);
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

   public static RuntimeContext toV2RuntimeContext(V1RuntimeContext v1RuntimeContext)
   {
      if (v1RuntimeContext != null)
      {
         RuntimeContext runtimeContext = WSRPTypeFactory.createRuntimeContext(v1RuntimeContext.getUserAuthentication());
         runtimeContext.setNamespacePrefix(v1RuntimeContext.getNamespacePrefix());
         runtimeContext.setPortletInstanceKey(v1RuntimeContext.getPortletInstanceKey());

         //TODO: handle the SessionParams better
         SessionParams sessionParams = new SessionParams();
         sessionParams.setSessionID(v1RuntimeContext.getSessionID());

         runtimeContext.setSessionParams(sessionParams);
         runtimeContext.setTemplates(toV2Templates(v1RuntimeContext.getTemplates()));

         List<V1Extension> extensions = v1RuntimeContext.getExtensions();
         if (extensions != null)
         {
            runtimeContext.getExtensions().addAll(WSRPUtils.transform(extensions, EXTENSION));
         }

         return runtimeContext;
      }
      else
      {
         return null;
      }
   }

   public static Templates toV2Templates(V1Templates v1Templates)
   {
      if (v1Templates != null)
      {
         //TODO: should we be using the WSRP1TypeFactory,createTemplates(PortletInvocationContext) instead?
         Templates templates = new Templates();
         templates.setBlockingActionTemplate(v1Templates.getBlockingActionTemplate());
         templates.setDefaultTemplate(v1Templates.getDefaultTemplate());
         templates.setRenderTemplate(v1Templates.getRenderTemplate());
         templates.setResourceTemplate(v1Templates.getResourceTemplate());
         templates.setSecureBlockingActionTemplate(v1Templates.getSecureBlockingActionTemplate());
         templates.setSecureRenderTemplate(v1Templates.getSecureRenderTemplate());
         templates.setSecureResourceTemplate(v1Templates.getSecureResourceTemplate());

         List<V1Extension> extensions = v1Templates.getExtensions();
         if (extensions != null)
         {
            templates.getExtensions().addAll(WSRPUtils.transform(extensions, EXTENSION));
         }

         return templates;
      }
      else
      {
         return null;
      }
   }

   public static UserContext toV2UserContext(V1UserContext v1UserContext)
   {
      if (v1UserContext != null)
      {
         UserContext userContext = WSRPTypeFactory.createUserContext(v1UserContext.getUserContextKey());
         userContext.setProfile(toV2UserProfile(v1UserContext.getProfile()));

         List<V1Extension> extensions = v1UserContext.getExtensions();
         if (extensions != null)
         {
            userContext.getExtensions().addAll(WSRPUtils.transform(extensions, EXTENSION));
         }

         if (v1UserContext.getUserCategories() != null)
         {
            userContext.getUserCategories().addAll(v1UserContext.getUserCategories());
         }

         return userContext;
      }
      else
      {
         return null;
      }
   }

   public static UserProfile toV2UserProfile(V1UserProfile v1UserProfile)
   {
      if (v1UserProfile != null)
      {
         UserProfile userProfile = new UserProfile();
         userProfile.setBdate(v1UserProfile.getBdate());
         userProfile.setBusinessInfo(toV2Context(v1UserProfile.getBusinessInfo()));
         userProfile.setEmployerInfo(toV2EmployerInfo(v1UserProfile.getEmployerInfo()));
         userProfile.setGender(v1UserProfile.getGender());
         userProfile.setHomeInfo(toV2Context(v1UserProfile.getHomeInfo()));
         userProfile.setName(toV2PersonName(v1UserProfile.getName()));

         return userProfile;
      }
      else
      {
         return null;
      }
   }

   public static EmployerInfo toV2EmployerInfo(V1EmployerInfo v1EmployerInfo)
   {
      if (v1EmployerInfo != null)
      {
         EmployerInfo employerInfo = new EmployerInfo();
         employerInfo.setDepartment(v1EmployerInfo.getDepartment());
         employerInfo.setEmployer(v1EmployerInfo.getEmployer());
         employerInfo.setJobtitle(v1EmployerInfo.getJobtitle());

         List<V1Extension> extensions = v1EmployerInfo.getExtensions();
         if (extensions != null)
         {
            employerInfo.getExtensions().addAll(WSRPUtils.transform(extensions, EXTENSION));
         }

         return employerInfo;
      }
      else
      {
         return null;
      }
   }

   public static PersonName toV2PersonName(V1PersonName v1PersonName)
   {
      if (v1PersonName != null)
      {
         PersonName personName = new PersonName();
         personName.setFamily(v1PersonName.getFamily());
         personName.setGiven(v1PersonName.getGiven());
         personName.setMiddle(v1PersonName.getMiddle());
         personName.setNickname(v1PersonName.getNickname());
         personName.setPrefix(v1PersonName.getPrefix());
         personName.setSuffix(v1PersonName.getSuffix());

         List<V1Extension> extensions = v1PersonName.getExtensions();
         if (extensions != null)
         {
            personName.getExtensions().addAll(WSRPUtils.transform(extensions, EXTENSION));
         }

         return personName;
      }
      else
      {
         return null;
      }
   }

   public static Contact toV2Context(V1Contact v1Contact)
   {
      if (v1Contact != null)
      {
         Contact contact = new Contact();
         contact.setOnline(toV2Online(v1Contact.getOnline()));
         contact.setPostal(toV2Postal(v1Contact.getPostal()));
         contact.setTelecom(toV2Telecom(v1Contact.getTelecom()));

         List<V1Extension> extensions = v1Contact.getExtensions();
         if (extensions != null)
         {
            contact.getExtensions().addAll(WSRPUtils.transform(extensions, EXTENSION));
         }

         return contact;
      }
      else
      {
         return null;
      }
   }

   public static Online toV2Online(V1Online v1Online)
   {
      if (v1Online != null)
      {
         Online online = new Online();
         online.setEmail(v1Online.getEmail());
         online.setUri(v1Online.getUri());

         List<V1Extension> extensions = v1Online.getExtensions();
         if (extensions != null)
         {
            online.getExtensions().addAll(WSRPUtils.transform(extensions, EXTENSION));
         }

         return online;
      }
      else
      {
         return null;
      }
   }

   public static Postal toV2Postal(V1Postal v1Postal)
   {
      if (v1Postal != null)
      {
         Postal postal = new Postal();
         postal.setCity(v1Postal.getCity());
         postal.setCountry(v1Postal.getCountry());
         postal.setName(v1Postal.getName());
         postal.setOrganization(v1Postal.getOrganization());
         postal.setPostalcode(v1Postal.getPostalcode());
         postal.setStateprov(v1Postal.getStateprov());
         postal.setStreet(v1Postal.getStreet());

         List<V1Extension> extensions = v1Postal.getExtensions();
         if (extensions != null)
         {
            postal.getExtensions().addAll(WSRPUtils.transform(extensions, EXTENSION));
         }

         return postal;
      }
      else
      {
         return null;
      }
   }

   public static Telecom toV2Telecom(V1Telecom v1Telecom)
   {
      if (v1Telecom != null)
      {
         Telecom telecom = new Telecom();
         telecom.setFax(toV2TelephoneNum(v1Telecom.getFax()));
         telecom.setMobile(toV2TelephoneNum(v1Telecom.getMobile()));
         telecom.setPager(toV2TelephoneNum(v1Telecom.getPager()));
         telecom.setTelephone(toV2TelephoneNum(v1Telecom.getTelephone()));

         List<V1Extension> extensions = v1Telecom.getExtensions();
         if (extensions != null)
         {
            telecom.getExtensions().addAll(WSRPUtils.transform(extensions, EXTENSION));
         }

         return telecom;
      }
      else
      {
         return null;
      }
   }

   public static TelephoneNum toV2TelephoneNum(V1TelephoneNum v1TelephoneNum)
   {
      if (v1TelephoneNum != null)
      {
         TelephoneNum telephoneNum = new TelephoneNum();
         telephoneNum.setComment(v1TelephoneNum.getComment());
         telephoneNum.setExt(v1TelephoneNum.getExt());
         telephoneNum.setIntcode(v1TelephoneNum.getIntcode());
         telephoneNum.setLoccode(v1TelephoneNum.getLoccode());
         telephoneNum.setNumber(v1TelephoneNum.getNumber());

         List<V1Extension> extensions = v1TelephoneNum.getExtensions();
         if (extensions != null)
         {
            telephoneNum.getExtensions().addAll(WSRPUtils.transform(extensions, EXTENSION));
         }

         return telephoneNum;
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

   public static InteractionParams toV2InteractionParams(V1InteractionParams v1InteractionParams)
   {
      if (v1InteractionParams != null)
      {
         InteractionParams interactionParams = WSRPTypeFactory.createInteractionParams(toV2StateChange(v1InteractionParams.getPortletStateChange()));
         interactionParams.setInteractionState(v1InteractionParams.getInteractionState());
         interactionParams.getExtensions().addAll(Lists.transform(v1InteractionParams.getExtensions(), EXTENSION));
         interactionParams.getFormParameters().addAll(Lists.transform(v1InteractionParams.getFormParameters(), NAMEDSTRING));
         interactionParams.getUploadContexts().addAll(Lists.transform(v1InteractionParams.getUploadContexts(), UPLOADCONTEXT));

         return interactionParams;
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
         return StateChange.fromValue((v1StateChange.value()));
      }
      else
      {
         return null;
      }
   }

   public static LocalizedString toV2LocalizedString(V1LocalizedString localizedString)
   {
      return LOCALIZEDSTRING.apply(localizedString);
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
         ModelDescription result = WSRPTypeFactory.createModelDescription(WSRPUtils.transform(v1ModelDescription.getPropertyDescriptions(), PROPERTYDESCRIPTION));
         List<Extension> extensions = WSRPUtils.transform(v1ModelDescription.getExtensions(), EXTENSION);
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

   public static ModelTypes toV2ModelTypes(V1ModelTypes modelTypes)
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

         List<Extension> extensions = WSRPUtils.transform(v1ResourceList.getExtensions(), EXTENSION);
         if (extensions != null)
         {
            result.getExtensions().addAll(extensions);
         }
         List<Resource> resources = WSRPUtils.transform(v1ResourceList.getResources(), RESOURCE);
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

   public static PropertyList toV2PropertyList(V1PropertyList v1PropertyList)
   {
      if (v1PropertyList != null)
      {
         PropertyList result = WSRPTypeFactory.createPropertyList();

         List<Property> properties = WSRPUtils.transform(v1PropertyList.getProperties(), PROPERTY);
         if (properties != null)
         {
            result.getProperties().addAll(properties);
         }

         List<Extension> extensions = WSRPUtils.transform(v1PropertyList.getExtensions(), EXTENSION);
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

   public static PortletDescription toV2PortletDescription(V1PortletDescription v1PortletDescription)
   {
      return PORTLETDESCRIPTION.apply(v1PortletDescription);
   }

   public static List<FailedPortlets> toV2FailedPortlets(List<V1DestroyFailed> destroyFailed)
   {
      if (ParameterValidation.existsAndIsNotEmpty(destroyFailed))
      {
         // todo: might need improvements
         List<FailedPortlets> result = new ArrayList<FailedPortlets>(destroyFailed.size());
         for (V1DestroyFailed failed : destroyFailed)
         {
            result.add(WSRPTypeFactory.createFailedPortlets(Collections.singletonList(failed.getPortletHandle()), failed.getReason()));
         }

         return result;
      }
      else
      {
         return null;
      }
   }

   public static RegistrationData toV2RegistrationData(V1RegistrationData registrationData)
   {
      if (registrationData != null)
      {
         RegistrationData result = WSRPTypeFactory.createRegistrationData(registrationData.getConsumerName(), registrationData.isMethodGetSupported());
         result.setConsumerAgent(registrationData.getConsumerAgent());

         List<Property> properties = WSRPUtils.transform(registrationData.getRegistrationProperties(), PROPERTY);
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
         List<Extension> extensions = WSRPUtils.transform(registrationData.getExtensions(), EXTENSION);
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

   public static MarkupContext toV2MarkupContext(V1MarkupContext v1MarkupContext)
   {
      if (v1MarkupContext != null)
      {
         MarkupContext result;

         byte[] binary = v1MarkupContext.getMarkupBinary();
         String string = v1MarkupContext.getMarkupString();

         if (string != null)
         {
            result = WSRPTypeFactory.createMarkupContext(v1MarkupContext.getMimeType(), string);
         }
         else
         {
            result = WSRPTypeFactory.createMarkupContext(v1MarkupContext.getMimeType(), binary);
         }
         result.setCacheControl(toV2CacheControl(v1MarkupContext.getCacheControl()));
         result.setLocale(v1MarkupContext.getLocale());
         result.setMimeType(v1MarkupContext.getMimeType());
         result.setPreferredTitle(v1MarkupContext.getPreferredTitle());
         result.setRequiresRewriting(v1MarkupContext.isRequiresUrlRewriting());
         result.setUseCachedItem(v1MarkupContext.isUseCachedMarkup());

         List<V1Extension> extensions = v1MarkupContext.getExtensions();
         if (extensions != null)
         {
            result.getExtensions().addAll(WSRPUtils.transform(extensions, EXTENSION));
         }

         return result;
      }
      else
      {
         return null;
      }
   }

   private static CacheControl toV2CacheControl(V1CacheControl v1CacheControl)
   {
      if (v1CacheControl != null)
      {
         CacheControl result = WSRPTypeFactory.createCacheControl(v1CacheControl.getExpires(), v1CacheControl.getUserScope());
         result.setValidateTag(v1CacheControl.getValidateTag());

         List<V1Extension> extensions = v1CacheControl.getExtensions();
         if (extensions != null)
         {
            result.getExtensions().addAll(WSRPUtils.transform(extensions, EXTENSION));
         }

         return result;
      }
      else
      {
         return null;
      }
   }

   public static SessionContext toV2SessionContext(V1SessionContext sessionContext)
   {
      if (sessionContext != null)
      {
         SessionContext result = WSRPTypeFactory.createSessionContext(sessionContext.getSessionID(), sessionContext.getExpires());
         result.getExtensions().addAll(Lists.transform(sessionContext.getExtensions(), EXTENSION));

         return result;
      }
      else
      {
         return null;
      }
   }

   public static UpdateResponse toV2UpdateResponse(V1UpdateResponse updateResponse)
   {
      if (updateResponse != null)
      {
         UpdateResponse result = WSRPTypeFactory.createUpdateResponse();
         result.setMarkupContext(toV2MarkupContext(updateResponse.getMarkupContext()));
         String state = updateResponse.getNavigationalState();
         if (state != null)
         {
            result.setNavigationalContext(WSRPTypeFactory.createNavigationalContextOrNull(new OpaqueStateString(state), null));
         }
         result.setNewWindowState(updateResponse.getNewWindowState());
         result.setPortletContext(toV2PortletContext(updateResponse.getPortletContext()));
         result.setSessionContext(toV2SessionContext(updateResponse.getSessionContext()));
         result.setNewMode(updateResponse.getNewMode());
         return result;
      }
      else
      {
         return null;
      }
   }

   public static GetServiceDescription toV2GetServiceDescription(V1GetServiceDescription getServiceDescription)
   {
      if (getServiceDescription != null)
      {
         GetServiceDescription result = WSRPTypeFactory.createGetServiceDescription();
         result.setRegistrationContext(toV2RegistrationContext(getServiceDescription.getRegistrationContext()));
         List<String> locales = getServiceDescription.getDesiredLocales();
         if (ParameterValidation.existsAndIsNotEmpty(locales))
         {
            result.getDesiredLocales().addAll(locales);
         }

         return result;
      }
      else
      {
         return null;
      }
   }

   public static class V1ToV2Extension implements Function<V1Extension, Extension>
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

   public static class V1ToV2PortletDescription implements Function<V1PortletDescription, PortletDescription>
   {

      public PortletDescription apply(V1PortletDescription from)
      {
         if (from != null)
         {
            PortletDescription result = WSRPTypeFactory.createPortletDescription(from.getPortletHandle(),
               WSRPUtils.transform(from.getMarkupTypes(), MARKUPTYPE));
            result.setDescription(LOCALIZEDSTRING.apply(from.getDescription()));
            result.setDisplayName(LOCALIZEDSTRING.apply(from.getDisplayName()));
            List<Extension> extensions = WSRPUtils.transform(from.getExtensions(), EXTENSION);
            if (extensions != null)
            {
               result.getExtensions().addAll(extensions);
            }
            List<LocalizedString> keywords = WSRPUtils.transform(from.getKeywords(), LOCALIZEDSTRING);
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

   public static class V1ToV2ItemDescription implements Function<V1ItemDescription, ItemDescription>
   {

      public ItemDescription apply(V1ItemDescription from)
      {
         if (from != null)
         {
            ItemDescription result = new ItemDescription();
            result.setItemName(from.getItemName());
            result.setDescription(LOCALIZEDSTRING.apply(from.getDescription()));
            List<Extension> extensions = WSRPUtils.transform(from.getExtensions(), EXTENSION);
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

   public static class V1ToV2UploadContext implements Function<V1UploadContext, UploadContext>
   {

      public UploadContext apply(V1UploadContext v1UploadContext)
      {
         if (v1UploadContext != null)
         {
            UploadContext result = WSRPTypeFactory.createUploadContext(v1UploadContext.getMimeType(), v1UploadContext.getUploadData());
            result.getExtensions().addAll(Lists.transform(v1UploadContext.getExtensions(), EXTENSION));
            result.getMimeAttributes().addAll(Lists.transform(v1UploadContext.getMimeAttributes(), NAMEDSTRING));

            return result;
         }
         else
         {
            return null;
         }
      }

   }

   public static class V1ToV2MarkupType implements Function<V1MarkupType, MarkupType>
   {

      public MarkupType apply(V1MarkupType from)
      {
         if (from != null)
         {
            MarkupType result = WSRPTypeFactory.createMarkupType(from.getMimeType(), from.getModes(), from.getWindowStates(), from.getLocales());
            List<Extension> extensions = WSRPUtils.transform(from.getExtensions(), EXTENSION);
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

   public static class V1ToV2LocalizedString implements Function<V1LocalizedString, LocalizedString>
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

   public static class V1ToV2Resource implements Function<V1Resource, Resource>
   {
      public Resource apply(V1Resource from)
      {
         if (from != null)
         {
            Resource result = new Resource();
            result.setResourceName(from.getResourceName());
            List<ResourceValue> values = WSRPUtils.transform(from.getValues(), RESOURCEVALUE);
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

   public static class V1ToV2ResourceValue implements Function<V1ResourceValue, ResourceValue>
   {
      public ResourceValue apply(V1ResourceValue from)
      {
         if (from != null)
         {
            ResourceValue result = new ResourceValue();
            result.setLang(from.getLang());
            result.setValue(from.getValue());
            List<Extension> extensions = WSRPUtils.transform(from.getExtensions(), EXTENSION);
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

   public static class V1ToV2PropertyDescription implements Function<V1PropertyDescription, PropertyDescription>
   {
      public PropertyDescription apply(V1PropertyDescription from)
      {
         if (from != null)
         {
            PropertyDescription result = WSRPTypeFactory.createPropertyDescription(from.getName(), from.getType());
            result.setHint(toV2LocalizedString(from.getHint()));
            result.setLabel(toV2LocalizedString(from.getLabel()));
            List<Extension> extensions = WSRPUtils.transform(from.getExtensions(), EXTENSION);
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

   public static class V1ToV2Property implements Function<V1Property, Property>
   {
      public Property apply(V1Property from)
      {
         if (from != null)
         {
            Property result = WSRPTypeFactory.createProperty(from.getName(), from.getLang(), from.getStringValue());
            result.setType(WSRPConstants.XSD_STRING); // todo: not sure what to do here... :(

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

   private static class V1ToV2ResetProperty implements Function<V1ResetProperty, ResetProperty>
   {
      public ResetProperty apply(V1ResetProperty from)
      {
         if (from != null)
         {
            return WSRPTypeFactory.createResetProperty(from.getName());
         }
         else
         {
            return null;
         }
      }
   }
}
