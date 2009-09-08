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

package org.oasis.wsrp.v1;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each Java content interface and Java element interface generated in the
 * org.oasis.wsrp.v1 package. <p>An ObjectFactory allows you to programatically construct new instances of the Java
 * representation for XML content. The Java representation of XML content can consist of schema derived interfaces and
 * classes representing the binding of schema type definitions, element declarations and model groups.  Factory methods
 * for each of these are provided in this class.
 */
@XmlRegistry
public class ObjectFactory
{

   private final static QName _RuntimeContext_QNAME = new QName("urn:oasis:names:tc:wsrp:v1:types", "RuntimeContext");
   private final static QName _PortletContext_QNAME = new QName("urn:oasis:names:tc:wsrp:v1:types", "PortletContext");
   private final static QName _ServiceDescription_QNAME = new QName("urn:oasis:names:tc:wsrp:v1:types", "ServiceDescription");
   private final static QName _Deregister_QNAME = new QName("urn:oasis:names:tc:wsrp:v1:types", "deregister");
   private final static QName _UserContext_QNAME = new QName("urn:oasis:names:tc:wsrp:v1:types", "UserContext");
   private final static QName _UnsupportedMimeType_QNAME = new QName("urn:oasis:names:tc:wsrp:v1:types", "UnsupportedMimeType");
   private final static QName _GetMarkupResponse_QNAME = new QName("urn:oasis:names:tc:wsrp:v1:types", "getMarkupResponse");
   private final static QName _RegistrationData_QNAME = new QName("urn:oasis:names:tc:wsrp:v1:types", "RegistrationData");
   private final static QName _NamedStringArray_QNAME = new QName("urn:oasis:names:tc:wsrp:v1:types", "NamedStringArray");
   private final static QName _ModelDescription_QNAME = new QName("urn:oasis:names:tc:wsrp:v1:types", "ModelDescription");
   private final static QName _ClonePortletResponse_QNAME = new QName("urn:oasis:names:tc:wsrp:v1:types", "clonePortletResponse");
   private final static QName _MissingParameters_QNAME = new QName("urn:oasis:names:tc:wsrp:v1:types", "MissingParameters");
   private final static QName _DeregisterResponse_QNAME = new QName("urn:oasis:names:tc:wsrp:v1:types", "deregisterResponse");
   private final static QName _Key_QNAME = new QName("urn:oasis:names:tc:wsrp:v1:types", "Key");
   private final static QName _InteractionParams_QNAME = new QName("urn:oasis:names:tc:wsrp:v1:types", "InteractionParams");
   private final static QName _PerformBlockingInteractionResponse_QNAME = new QName("urn:oasis:names:tc:wsrp:v1:types", "performBlockingInteractionResponse");
   private final static QName _PropertyList_QNAME = new QName("urn:oasis:names:tc:wsrp:v1:types", "PropertyList");
   private final static QName _DestroyPortletsResponse_QNAME = new QName("urn:oasis:names:tc:wsrp:v1:types", "destroyPortletsResponse");
   private final static QName _RegisterResponse_QNAME = new QName("urn:oasis:names:tc:wsrp:v1:types", "registerResponse");
   private final static QName _ID_QNAME = new QName("urn:oasis:names:tc:wsrp:v1:types", "ID");
   private final static QName _RegistrationContext_QNAME = new QName("urn:oasis:names:tc:wsrp:v1:types", "RegistrationContext");
   private final static QName _PortletStateChangeRequired_QNAME = new QName("urn:oasis:names:tc:wsrp:v1:types", "PortletStateChangeRequired");
   private final static QName _InvalidRegistration_QNAME = new QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidRegistration");
   private final static QName _InconsistentParameters_QNAME = new QName("urn:oasis:names:tc:wsrp:v1:types", "InconsistentParameters");
   private final static QName _GetPortletDescriptionResponse_QNAME = new QName("urn:oasis:names:tc:wsrp:v1:types", "getPortletDescriptionResponse");
   private final static QName _InvalidSession_QNAME = new QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidSession");
   private final static QName _Templates_QNAME = new QName("urn:oasis:names:tc:wsrp:v1:types", "Templates");
   private final static QName _Contact_QNAME = new QName("urn:oasis:names:tc:wsrp:v1:types", "Contact");
   private final static QName _BlockingInteractionResponse_QNAME = new QName("urn:oasis:names:tc:wsrp:v1:types", "BlockingInteractionResponse");
   private final static QName _SetPortletPropertiesResponse_QNAME = new QName("urn:oasis:names:tc:wsrp:v1:types", "setPortletPropertiesResponse");
   private final static QName _RegistrationState_QNAME = new QName("urn:oasis:names:tc:wsrp:v1:types", "RegistrationState");
   private final static QName _UnsupportedLocale_QNAME = new QName("urn:oasis:names:tc:wsrp:v1:types", "UnsupportedLocale");
   private final static QName _AccessDenied_QNAME = new QName("urn:oasis:names:tc:wsrp:v1:types", "AccessDenied");
   private final static QName _GetServiceDescriptionResponse_QNAME = new QName("urn:oasis:names:tc:wsrp:v1:types", "getServiceDescriptionResponse");
   private final static QName _UnsupportedMode_QNAME = new QName("urn:oasis:names:tc:wsrp:v1:types", "UnsupportedMode");
   private final static QName _OperationFailed_QNAME = new QName("urn:oasis:names:tc:wsrp:v1:types", "OperationFailed");
   private final static QName _InitCookieResponse_QNAME = new QName("urn:oasis:names:tc:wsrp:v1:types", "initCookieResponse");
   private final static QName _Register_QNAME = new QName("urn:oasis:names:tc:wsrp:v1:types", "register");
   private final static QName _UnsupportedWindowState_QNAME = new QName("urn:oasis:names:tc:wsrp:v1:types", "UnsupportedWindowState");
   private final static QName _InvalidUserCategory_QNAME = new QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidUserCategory");
   private final static QName _StringArray_QNAME = new QName("urn:oasis:names:tc:wsrp:v1:types", "StringArray");
   private final static QName _SessionContext_QNAME = new QName("urn:oasis:names:tc:wsrp:v1:types", "SessionContext");
   private final static QName _MarkupParams_QNAME = new QName("urn:oasis:names:tc:wsrp:v1:types", "MarkupParams");
   private final static QName _ClientData_QNAME = new QName("urn:oasis:names:tc:wsrp:v1:types", "ClientData");
   private final static QName _ReleaseSessionsResponse_QNAME = new QName("urn:oasis:names:tc:wsrp:v1:types", "releaseSessionsResponse");
   private final static QName _InvalidCookie_QNAME = new QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidCookie");
   private final static QName _GetPortletPropertiesResponse_QNAME = new QName("urn:oasis:names:tc:wsrp:v1:types", "getPortletPropertiesResponse");
   private final static QName _GetPortletPropertyDescriptionResponse_QNAME = new QName("urn:oasis:names:tc:wsrp:v1:types", "getPortletPropertyDescriptionResponse");
   private final static QName _InvalidHandle_QNAME = new QName("urn:oasis:names:tc:wsrp:v1:types", "InvalidHandle");
   private final static QName _Handle_QNAME = new QName("urn:oasis:names:tc:wsrp:v1:types", "Handle");
   private final static QName _ModifyRegistrationResponse_QNAME = new QName("urn:oasis:names:tc:wsrp:v1:types", "modifyRegistrationResponse");

   /**
    * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package:
    * org.oasis.wsrp.v1
    */
   public ObjectFactory()
   {
   }

   /** Create an instance of {@link UserProfile } */
   public UserProfile createUserProfile()
   {
      return new UserProfile();
   }

   /** Create an instance of {@link Templates } */
   public Templates createTemplates()
   {
      return new Templates();
   }

   /** Create an instance of {@link PortletContext } */
   public PortletContext createPortletContext()
   {
      return new PortletContext();
   }

   /** Create an instance of {@link InvalidRegistrationFault } */
   public InvalidRegistrationFault createInvalidRegistrationFault()
   {
      return new InvalidRegistrationFault();
   }

   /** Create an instance of {@link InitCookie } */
   public InitCookie createInitCookie()
   {
      return new InitCookie();
   }

   /** Create an instance of {@link Telecom } */
   public Telecom createTelecom()
   {
      return new Telecom();
   }

   /** Create an instance of {@link PortletDescriptionResponse } */
   public PortletDescriptionResponse createPortletDescriptionResponse()
   {
      return new PortletDescriptionResponse();
   }

   /** Create an instance of {@link Resource } */
   public Resource createResource()
   {
      return new Resource();
   }

   /** Create an instance of {@link Postal } */
   public Postal createPostal()
   {
      return new Postal();
   }

   /** Create an instance of {@link SessionContext } */
   public SessionContext createSessionContext()
   {
      return new SessionContext();
   }

   /** Create an instance of {@link Fault } */
   public Fault createFault()
   {
      return new Fault();
   }

   /** Create an instance of {@link Online } */
   public Online createOnline()
   {
      return new Online();
   }

   /** Create an instance of {@link ReleaseSessions } */
   public ReleaseSessions createReleaseSessions()
   {
      return new ReleaseSessions();
   }

   /** Create an instance of {@link UploadContext } */
   public UploadContext createUploadContext()
   {
      return new UploadContext();
   }

   /** Create an instance of {@link Extension } */
   public Extension createExtension()
   {
      return new Extension();
   }

   /** Create an instance of {@link PortletDescription } */
   public PortletDescription createPortletDescription()
   {
      return new PortletDescription();
   }

   /** Create an instance of {@link PersonName } */
   public PersonName createPersonName()
   {
      return new PersonName();
   }

   /** Create an instance of {@link InvalidCookieFault } */
   public InvalidCookieFault createInvalidCookieFault()
   {
      return new InvalidCookieFault();
   }

   /** Create an instance of {@link RuntimeContext } */
   public RuntimeContext createRuntimeContext()
   {
      return new RuntimeContext();
   }

   /** Create an instance of {@link ClientData } */
   public ClientData createClientData()
   {
      return new ClientData();
   }

   /** Create an instance of {@link MarkupParams } */
   public MarkupParams createMarkupParams()
   {
      return new MarkupParams();
   }

   /** Create an instance of {@link ModifyRegistration } */
   public ModifyRegistration createModifyRegistration()
   {
      return new ModifyRegistration();
   }

   /** Create an instance of {@link MissingParametersFault } */
   public MissingParametersFault createMissingParametersFault()
   {
      return new MissingParametersFault();
   }

   /** Create an instance of {@link GetMarkup } */
   public GetMarkup createGetMarkup()
   {
      return new GetMarkup();
   }

   /** Create an instance of {@link RegistrationData } */
   public RegistrationData createRegistrationData()
   {
      return new RegistrationData();
   }

   /** Create an instance of {@link ServiceDescription } */
   public ServiceDescription createServiceDescription()
   {
      return new ServiceDescription();
   }

   /** Create an instance of {@link GetPortletProperties } */
   public GetPortletProperties createGetPortletProperties()
   {
      return new GetPortletProperties();
   }

   /** Create an instance of {@link BlockingInteractionResponse } */
   public BlockingInteractionResponse createBlockingInteractionResponse()
   {
      return new BlockingInteractionResponse();
   }

   /** Create an instance of {@link OperationFailedFault } */
   public OperationFailedFault createOperationFailedFault()
   {
      return new OperationFailedFault();
   }

   /** Create an instance of {@link InteractionParams } */
   public InteractionParams createInteractionParams()
   {
      return new InteractionParams();
   }

   /** Create an instance of {@link ItemDescription } */
   public ItemDescription createItemDescription()
   {
      return new ItemDescription();
   }

   /** Create an instance of {@link ResourceList } */
   public ResourceList createResourceList()
   {
      return new ResourceList();
   }

   /** Create an instance of {@link AccessDeniedFault } */
   public AccessDeniedFault createAccessDeniedFault()
   {
      return new AccessDeniedFault();
   }

   /** Create an instance of {@link SetPortletProperties } */
   public SetPortletProperties createSetPortletProperties()
   {
      return new SetPortletProperties();
   }

   /** Create an instance of {@link ResetProperty } */
   public ResetProperty createResetProperty()
   {
      return new ResetProperty();
   }

   /** Create an instance of {@link DestroyFailed } */
   public DestroyFailed createDestroyFailed()
   {
      return new DestroyFailed();
   }

   /** Create an instance of {@link GetPortletPropertyDescription } */
   public GetPortletPropertyDescription createGetPortletPropertyDescription()
   {
      return new GetPortletPropertyDescription();
   }

   /** Create an instance of {@link Contact } */
   public Contact createContact()
   {
      return new Contact();
   }

   /** Create an instance of {@link UnsupportedMimeTypeFault } */
   public UnsupportedMimeTypeFault createUnsupportedMimeTypeFault()
   {
      return new UnsupportedMimeTypeFault();
   }

   /** Create an instance of {@link MarkupResponse } */
   public MarkupResponse createMarkupResponse()
   {
      return new MarkupResponse();
   }

   /** Create an instance of {@link InvalidHandleFault } */
   public InvalidHandleFault createInvalidHandleFault()
   {
      return new InvalidHandleFault();
   }

   /** Create an instance of {@link InvalidUserCategoryFault } */
   public InvalidUserCategoryFault createInvalidUserCategoryFault()
   {
      return new InvalidUserCategoryFault();
   }

   /** Create an instance of {@link RegistrationContext } */
   public RegistrationContext createRegistrationContext()
   {
      return new RegistrationContext();
   }

   /** Create an instance of {@link ModelDescription } */
   public ModelDescription createModelDescription()
   {
      return new ModelDescription();
   }

   /** Create an instance of {@link PropertyList } */
   public PropertyList createPropertyList()
   {
      return new PropertyList();
   }

   /** Create an instance of {@link UserContext } */
   public UserContext createUserContext()
   {
      return new UserContext();
   }

   /** Create an instance of {@link GetServiceDescription } */
   public GetServiceDescription createGetServiceDescription()
   {
      return new GetServiceDescription();
   }

   /** Create an instance of {@link InconsistentParametersFault } */
   public InconsistentParametersFault createInconsistentParametersFault()
   {
      return new InconsistentParametersFault();
   }

   /** Create an instance of {@link LocalizedString } */
   public LocalizedString createLocalizedString()
   {
      return new LocalizedString();
   }

   /** Create an instance of {@link UpdateResponse } */
   public UpdateResponse createUpdateResponse()
   {
      return new UpdateResponse();
   }

   /** Create an instance of {@link MarkupType } */
   public MarkupType createMarkupType()
   {
      return new MarkupType();
   }

   /** Create an instance of {@link GetPortletDescription } */
   public GetPortletDescription createGetPortletDescription()
   {
      return new GetPortletDescription();
   }

   /** Create an instance of {@link ModelTypes } */
   public ModelTypes createModelTypes()
   {
      return new ModelTypes();
   }

   /** Create an instance of {@link MarkupContext } */
   public MarkupContext createMarkupContext()
   {
      return new MarkupContext();
   }

   /** Create an instance of {@link PortletPropertyDescriptionResponse } */
   public PortletPropertyDescriptionResponse createPortletPropertyDescriptionResponse()
   {
      return new PortletPropertyDescriptionResponse();
   }

   /** Create an instance of {@link EmployerInfo } */
   public EmployerInfo createEmployerInfo()
   {
      return new EmployerInfo();
   }

   /** Create an instance of {@link DestroyPortletsResponse } */
   public DestroyPortletsResponse createDestroyPortletsResponse()
   {
      return new DestroyPortletsResponse();
   }

   /** Create an instance of {@link ClonePortlet } */
   public ClonePortlet createClonePortlet()
   {
      return new ClonePortlet();
   }

   /** Create an instance of {@link UnsupportedModeFault } */
   public UnsupportedModeFault createUnsupportedModeFault()
   {
      return new UnsupportedModeFault();
   }

   /** Create an instance of {@link ResourceValue } */
   public ResourceValue createResourceValue()
   {
      return new ResourceValue();
   }

   /** Create an instance of {@link Property } */
   public Property createProperty()
   {
      return new Property();
   }

   /** Create an instance of {@link RegistrationState } */
   public RegistrationState createRegistrationState()
   {
      return new RegistrationState();
   }

   /** Create an instance of {@link TelephoneNum } */
   public TelephoneNum createTelephoneNum()
   {
      return new TelephoneNum();
   }

   /** Create an instance of {@link UnsupportedWindowStateFault } */
   public UnsupportedWindowStateFault createUnsupportedWindowStateFault()
   {
      return new UnsupportedWindowStateFault();
   }

   /** Create an instance of {@link UnsupportedLocaleFault } */
   public UnsupportedLocaleFault createUnsupportedLocaleFault()
   {
      return new UnsupportedLocaleFault();
   }

   /** Create an instance of {@link NamedStringArray } */
   public NamedStringArray createNamedStringArray()
   {
      return new NamedStringArray();
   }

   /** Create an instance of {@link CacheControl } */
   public CacheControl createCacheControl()
   {
      return new CacheControl();
   }

   /** Create an instance of {@link InvalidSessionFault } */
   public InvalidSessionFault createInvalidSessionFault()
   {
      return new InvalidSessionFault();
   }

   /** Create an instance of {@link PerformBlockingInteraction } */
   public PerformBlockingInteraction createPerformBlockingInteraction()
   {
      return new PerformBlockingInteraction();
   }

   /** Create an instance of {@link StringArray } */
   public StringArray createStringArray()
   {
      return new StringArray();
   }

   /** Create an instance of {@link PropertyDescription } */
   public PropertyDescription createPropertyDescription()
   {
      return new PropertyDescription();
   }

   /** Create an instance of {@link DestroyPortlets } */
   public DestroyPortlets createDestroyPortlets()
   {
      return new DestroyPortlets();
   }

   /** Create an instance of {@link NamedString } */
   public NamedString createNamedString()
   {
      return new NamedString();
   }

   /** Create an instance of {@link ReturnAny } */
   public ReturnAny createReturnAny()
   {
      return new ReturnAny();
   }

   /** Create an instance of {@link PortletStateChangeRequiredFault } */
   public PortletStateChangeRequiredFault createPortletStateChangeRequiredFault()
   {
      return new PortletStateChangeRequiredFault();
   }

   /** Create an instance of {@link JAXBElement }{@code <}{@link RuntimeContext }{@code >}} */
   @XmlElementDecl(namespace = "urn:oasis:names:tc:wsrp:v1:types", name = "RuntimeContext")
   public JAXBElement<RuntimeContext> createRuntimeContext(RuntimeContext value)
   {
      return new JAXBElement<RuntimeContext>(_RuntimeContext_QNAME, RuntimeContext.class, null, value);
   }

   /** Create an instance of {@link JAXBElement }{@code <}{@link PortletContext }{@code >}} */
   @XmlElementDecl(namespace = "urn:oasis:names:tc:wsrp:v1:types", name = "PortletContext")
   public JAXBElement<PortletContext> createPortletContext(PortletContext value)
   {
      return new JAXBElement<PortletContext>(_PortletContext_QNAME, PortletContext.class, null, value);
   }

   /** Create an instance of {@link JAXBElement }{@code <}{@link ServiceDescription }{@code >}} */
   @XmlElementDecl(namespace = "urn:oasis:names:tc:wsrp:v1:types", name = "ServiceDescription")
   public JAXBElement<ServiceDescription> createServiceDescription(ServiceDescription value)
   {
      return new JAXBElement<ServiceDescription>(_ServiceDescription_QNAME, ServiceDescription.class, null, value);
   }

   /** Create an instance of {@link JAXBElement }{@code <}{@link RegistrationContext }{@code >}} */
   @XmlElementDecl(namespace = "urn:oasis:names:tc:wsrp:v1:types", name = "deregister")
   public JAXBElement<RegistrationContext> createDeregister(RegistrationContext value)
   {
      return new JAXBElement<RegistrationContext>(_Deregister_QNAME, RegistrationContext.class, null, value);
   }

   /** Create an instance of {@link JAXBElement }{@code <}{@link UserContext }{@code >}} */
   @XmlElementDecl(namespace = "urn:oasis:names:tc:wsrp:v1:types", name = "UserContext")
   public JAXBElement<UserContext> createUserContext(UserContext value)
   {
      return new JAXBElement<UserContext>(_UserContext_QNAME, UserContext.class, null, value);
   }

   /** Create an instance of {@link JAXBElement }{@code <}{@link UnsupportedMimeTypeFault }{@code >}} */
   @XmlElementDecl(namespace = "urn:oasis:names:tc:wsrp:v1:types", name = "UnsupportedMimeType")
   public JAXBElement<UnsupportedMimeTypeFault> createUnsupportedMimeType(UnsupportedMimeTypeFault value)
   {
      return new JAXBElement<UnsupportedMimeTypeFault>(_UnsupportedMimeType_QNAME, UnsupportedMimeTypeFault.class, null, value);
   }

   /** Create an instance of {@link JAXBElement }{@code <}{@link MarkupResponse }{@code >}} */
   @XmlElementDecl(namespace = "urn:oasis:names:tc:wsrp:v1:types", name = "getMarkupResponse")
   public JAXBElement<MarkupResponse> createGetMarkupResponse(MarkupResponse value)
   {
      return new JAXBElement<MarkupResponse>(_GetMarkupResponse_QNAME, MarkupResponse.class, null, value);
   }

   /** Create an instance of {@link JAXBElement }{@code <}{@link RegistrationData }{@code >}} */
   @XmlElementDecl(namespace = "urn:oasis:names:tc:wsrp:v1:types", name = "RegistrationData")
   public JAXBElement<RegistrationData> createRegistrationData(RegistrationData value)
   {
      return new JAXBElement<RegistrationData>(_RegistrationData_QNAME, RegistrationData.class, null, value);
   }

   /** Create an instance of {@link JAXBElement }{@code <}{@link NamedStringArray }{@code >}} */
   @XmlElementDecl(namespace = "urn:oasis:names:tc:wsrp:v1:types", name = "NamedStringArray")
   public JAXBElement<NamedStringArray> createNamedStringArray(NamedStringArray value)
   {
      return new JAXBElement<NamedStringArray>(_NamedStringArray_QNAME, NamedStringArray.class, null, value);
   }

   /** Create an instance of {@link JAXBElement }{@code <}{@link ModelDescription }{@code >}} */
   @XmlElementDecl(namespace = "urn:oasis:names:tc:wsrp:v1:types", name = "ModelDescription")
   public JAXBElement<ModelDescription> createModelDescription(ModelDescription value)
   {
      return new JAXBElement<ModelDescription>(_ModelDescription_QNAME, ModelDescription.class, null, value);
   }

   /** Create an instance of {@link JAXBElement }{@code <}{@link PortletContext }{@code >}} */
   @XmlElementDecl(namespace = "urn:oasis:names:tc:wsrp:v1:types", name = "clonePortletResponse")
   public JAXBElement<PortletContext> createClonePortletResponse(PortletContext value)
   {
      return new JAXBElement<PortletContext>(_ClonePortletResponse_QNAME, PortletContext.class, null, value);
   }

   /** Create an instance of {@link JAXBElement }{@code <}{@link MissingParametersFault }{@code >}} */
   @XmlElementDecl(namespace = "urn:oasis:names:tc:wsrp:v1:types", name = "MissingParameters")
   public JAXBElement<MissingParametersFault> createMissingParameters(MissingParametersFault value)
   {
      return new JAXBElement<MissingParametersFault>(_MissingParameters_QNAME, MissingParametersFault.class, null, value);
   }

   /** Create an instance of {@link JAXBElement }{@code <}{@link ReturnAny }{@code >}} */
   @XmlElementDecl(namespace = "urn:oasis:names:tc:wsrp:v1:types", name = "deregisterResponse")
   public JAXBElement<ReturnAny> createDeregisterResponse(ReturnAny value)
   {
      return new JAXBElement<ReturnAny>(_DeregisterResponse_QNAME, ReturnAny.class, null, value);
   }

   /** Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}} */
   @XmlElementDecl(namespace = "urn:oasis:names:tc:wsrp:v1:types", name = "Key")
   public JAXBElement<String> createKey(String value)
   {
      return new JAXBElement<String>(_Key_QNAME, String.class, null, value);
   }

   /** Create an instance of {@link JAXBElement }{@code <}{@link InteractionParams }{@code >}} */
   @XmlElementDecl(namespace = "urn:oasis:names:tc:wsrp:v1:types", name = "InteractionParams")
   public JAXBElement<InteractionParams> createInteractionParams(InteractionParams value)
   {
      return new JAXBElement<InteractionParams>(_InteractionParams_QNAME, InteractionParams.class, null, value);
   }

   /** Create an instance of {@link JAXBElement }{@code <}{@link BlockingInteractionResponse }{@code >}} */
   @XmlElementDecl(namespace = "urn:oasis:names:tc:wsrp:v1:types", name = "performBlockingInteractionResponse")
   public JAXBElement<BlockingInteractionResponse> createPerformBlockingInteractionResponse(BlockingInteractionResponse value)
   {
      return new JAXBElement<BlockingInteractionResponse>(_PerformBlockingInteractionResponse_QNAME, BlockingInteractionResponse.class, null, value);
   }

   /** Create an instance of {@link JAXBElement }{@code <}{@link PropertyList }{@code >}} */
   @XmlElementDecl(namespace = "urn:oasis:names:tc:wsrp:v1:types", name = "PropertyList")
   public JAXBElement<PropertyList> createPropertyList(PropertyList value)
   {
      return new JAXBElement<PropertyList>(_PropertyList_QNAME, PropertyList.class, null, value);
   }

   /** Create an instance of {@link JAXBElement }{@code <}{@link DestroyPortletsResponse }{@code >}} */
   @XmlElementDecl(namespace = "urn:oasis:names:tc:wsrp:v1:types", name = "destroyPortletsResponse")
   public JAXBElement<DestroyPortletsResponse> createDestroyPortletsResponse(DestroyPortletsResponse value)
   {
      return new JAXBElement<DestroyPortletsResponse>(_DestroyPortletsResponse_QNAME, DestroyPortletsResponse.class, null, value);
   }

   /** Create an instance of {@link JAXBElement }{@code <}{@link RegistrationContext }{@code >}} */
   @XmlElementDecl(namespace = "urn:oasis:names:tc:wsrp:v1:types", name = "registerResponse")
   public JAXBElement<RegistrationContext> createRegisterResponse(RegistrationContext value)
   {
      return new JAXBElement<RegistrationContext>(_RegisterResponse_QNAME, RegistrationContext.class, null, value);
   }

   /** Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}} */
   @XmlElementDecl(namespace = "urn:oasis:names:tc:wsrp:v1:types", name = "ID")
   public JAXBElement<String> createID(String value)
   {
      return new JAXBElement<String>(_ID_QNAME, String.class, null, value);
   }

   /** Create an instance of {@link JAXBElement }{@code <}{@link RegistrationContext }{@code >}} */
   @XmlElementDecl(namespace = "urn:oasis:names:tc:wsrp:v1:types", name = "RegistrationContext")
   public JAXBElement<RegistrationContext> createRegistrationContext(RegistrationContext value)
   {
      return new JAXBElement<RegistrationContext>(_RegistrationContext_QNAME, RegistrationContext.class, null, value);
   }

   /** Create an instance of {@link JAXBElement }{@code <}{@link PortletStateChangeRequiredFault }{@code >}} */
   @XmlElementDecl(namespace = "urn:oasis:names:tc:wsrp:v1:types", name = "PortletStateChangeRequired")
   public JAXBElement<PortletStateChangeRequiredFault> createPortletStateChangeRequired(PortletStateChangeRequiredFault value)
   {
      return new JAXBElement<PortletStateChangeRequiredFault>(_PortletStateChangeRequired_QNAME, PortletStateChangeRequiredFault.class, null, value);
   }

   /** Create an instance of {@link JAXBElement }{@code <}{@link InvalidRegistrationFault }{@code >}} */
   @XmlElementDecl(namespace = "urn:oasis:names:tc:wsrp:v1:types", name = "InvalidRegistration")
   public JAXBElement<InvalidRegistrationFault> createInvalidRegistration(InvalidRegistrationFault value)
   {
      return new JAXBElement<InvalidRegistrationFault>(_InvalidRegistration_QNAME, InvalidRegistrationFault.class, null, value);
   }

   /** Create an instance of {@link JAXBElement }{@code <}{@link InconsistentParametersFault }{@code >}} */
   @XmlElementDecl(namespace = "urn:oasis:names:tc:wsrp:v1:types", name = "InconsistentParameters")
   public JAXBElement<InconsistentParametersFault> createInconsistentParameters(InconsistentParametersFault value)
   {
      return new JAXBElement<InconsistentParametersFault>(_InconsistentParameters_QNAME, InconsistentParametersFault.class, null, value);
   }

   /** Create an instance of {@link JAXBElement }{@code <}{@link PortletDescriptionResponse }{@code >}} */
   @XmlElementDecl(namespace = "urn:oasis:names:tc:wsrp:v1:types", name = "getPortletDescriptionResponse")
   public JAXBElement<PortletDescriptionResponse> createGetPortletDescriptionResponse(PortletDescriptionResponse value)
   {
      return new JAXBElement<PortletDescriptionResponse>(_GetPortletDescriptionResponse_QNAME, PortletDescriptionResponse.class, null, value);
   }

   /** Create an instance of {@link JAXBElement }{@code <}{@link InvalidSessionFault }{@code >}} */
   @XmlElementDecl(namespace = "urn:oasis:names:tc:wsrp:v1:types", name = "InvalidSession")
   public JAXBElement<InvalidSessionFault> createInvalidSession(InvalidSessionFault value)
   {
      return new JAXBElement<InvalidSessionFault>(_InvalidSession_QNAME, InvalidSessionFault.class, null, value);
   }

   /** Create an instance of {@link JAXBElement }{@code <}{@link Templates }{@code >}} */
   @XmlElementDecl(namespace = "urn:oasis:names:tc:wsrp:v1:types", name = "Templates")
   public JAXBElement<Templates> createTemplates(Templates value)
   {
      return new JAXBElement<Templates>(_Templates_QNAME, Templates.class, null, value);
   }

   /** Create an instance of {@link JAXBElement }{@code <}{@link Contact }{@code >}} */
   @XmlElementDecl(namespace = "urn:oasis:names:tc:wsrp:v1:types", name = "Contact")
   public JAXBElement<Contact> createContact(Contact value)
   {
      return new JAXBElement<Contact>(_Contact_QNAME, Contact.class, null, value);
   }

   /** Create an instance of {@link JAXBElement }{@code <}{@link BlockingInteractionResponse }{@code >}} */
   @XmlElementDecl(namespace = "urn:oasis:names:tc:wsrp:v1:types", name = "BlockingInteractionResponse")
   public JAXBElement<BlockingInteractionResponse> createBlockingInteractionResponse(BlockingInteractionResponse value)
   {
      return new JAXBElement<BlockingInteractionResponse>(_BlockingInteractionResponse_QNAME, BlockingInteractionResponse.class, null, value);
   }

   /** Create an instance of {@link JAXBElement }{@code <}{@link PortletContext }{@code >}} */
   @XmlElementDecl(namespace = "urn:oasis:names:tc:wsrp:v1:types", name = "setPortletPropertiesResponse")
   public JAXBElement<PortletContext> createSetPortletPropertiesResponse(PortletContext value)
   {
      return new JAXBElement<PortletContext>(_SetPortletPropertiesResponse_QNAME, PortletContext.class, null, value);
   }

   /** Create an instance of {@link JAXBElement }{@code <}{@link RegistrationState }{@code >}} */
   @XmlElementDecl(namespace = "urn:oasis:names:tc:wsrp:v1:types", name = "RegistrationState")
   public JAXBElement<RegistrationState> createRegistrationState(RegistrationState value)
   {
      return new JAXBElement<RegistrationState>(_RegistrationState_QNAME, RegistrationState.class, null, value);
   }

   /** Create an instance of {@link JAXBElement }{@code <}{@link UnsupportedLocaleFault }{@code >}} */
   @XmlElementDecl(namespace = "urn:oasis:names:tc:wsrp:v1:types", name = "UnsupportedLocale")
   public JAXBElement<UnsupportedLocaleFault> createUnsupportedLocale(UnsupportedLocaleFault value)
   {
      return new JAXBElement<UnsupportedLocaleFault>(_UnsupportedLocale_QNAME, UnsupportedLocaleFault.class, null, value);
   }

   /** Create an instance of {@link JAXBElement }{@code <}{@link AccessDeniedFault }{@code >}} */
   @XmlElementDecl(namespace = "urn:oasis:names:tc:wsrp:v1:types", name = "AccessDenied")
   public JAXBElement<AccessDeniedFault> createAccessDenied(AccessDeniedFault value)
   {
      return new JAXBElement<AccessDeniedFault>(_AccessDenied_QNAME, AccessDeniedFault.class, null, value);
   }

   /** Create an instance of {@link JAXBElement }{@code <}{@link ServiceDescription }{@code >}} */
   @XmlElementDecl(namespace = "urn:oasis:names:tc:wsrp:v1:types", name = "getServiceDescriptionResponse")
   public JAXBElement<ServiceDescription> createGetServiceDescriptionResponse(ServiceDescription value)
   {
      return new JAXBElement<ServiceDescription>(_GetServiceDescriptionResponse_QNAME, ServiceDescription.class, null, value);
   }

   /** Create an instance of {@link JAXBElement }{@code <}{@link UnsupportedModeFault }{@code >}} */
   @XmlElementDecl(namespace = "urn:oasis:names:tc:wsrp:v1:types", name = "UnsupportedMode")
   public JAXBElement<UnsupportedModeFault> createUnsupportedMode(UnsupportedModeFault value)
   {
      return new JAXBElement<UnsupportedModeFault>(_UnsupportedMode_QNAME, UnsupportedModeFault.class, null, value);
   }

   /** Create an instance of {@link JAXBElement }{@code <}{@link OperationFailedFault }{@code >}} */
   @XmlElementDecl(namespace = "urn:oasis:names:tc:wsrp:v1:types", name = "OperationFailed")
   public JAXBElement<OperationFailedFault> createOperationFailed(OperationFailedFault value)
   {
      return new JAXBElement<OperationFailedFault>(_OperationFailed_QNAME, OperationFailedFault.class, null, value);
   }

   /** Create an instance of {@link JAXBElement }{@code <}{@link ReturnAny }{@code >}} */
   @XmlElementDecl(namespace = "urn:oasis:names:tc:wsrp:v1:types", name = "initCookieResponse")
   public JAXBElement<ReturnAny> createInitCookieResponse(ReturnAny value)
   {
      return new JAXBElement<ReturnAny>(_InitCookieResponse_QNAME, ReturnAny.class, null, value);
   }

   /** Create an instance of {@link JAXBElement }{@code <}{@link RegistrationData }{@code >}} */
   @XmlElementDecl(namespace = "urn:oasis:names:tc:wsrp:v1:types", name = "register")
   public JAXBElement<RegistrationData> createRegister(RegistrationData value)
   {
      return new JAXBElement<RegistrationData>(_Register_QNAME, RegistrationData.class, null, value);
   }

   /** Create an instance of {@link JAXBElement }{@code <}{@link UnsupportedWindowStateFault }{@code >}} */
   @XmlElementDecl(namespace = "urn:oasis:names:tc:wsrp:v1:types", name = "UnsupportedWindowState")
   public JAXBElement<UnsupportedWindowStateFault> createUnsupportedWindowState(UnsupportedWindowStateFault value)
   {
      return new JAXBElement<UnsupportedWindowStateFault>(_UnsupportedWindowState_QNAME, UnsupportedWindowStateFault.class, null, value);
   }

   /** Create an instance of {@link JAXBElement }{@code <}{@link InvalidUserCategoryFault }{@code >}} */
   @XmlElementDecl(namespace = "urn:oasis:names:tc:wsrp:v1:types", name = "InvalidUserCategory")
   public JAXBElement<InvalidUserCategoryFault> createInvalidUserCategory(InvalidUserCategoryFault value)
   {
      return new JAXBElement<InvalidUserCategoryFault>(_InvalidUserCategory_QNAME, InvalidUserCategoryFault.class, null, value);
   }

   /** Create an instance of {@link JAXBElement }{@code <}{@link StringArray }{@code >}} */
   @XmlElementDecl(namespace = "urn:oasis:names:tc:wsrp:v1:types", name = "StringArray")
   public JAXBElement<StringArray> createStringArray(StringArray value)
   {
      return new JAXBElement<StringArray>(_StringArray_QNAME, StringArray.class, null, value);
   }

   /** Create an instance of {@link JAXBElement }{@code <}{@link SessionContext }{@code >}} */
   @XmlElementDecl(namespace = "urn:oasis:names:tc:wsrp:v1:types", name = "SessionContext")
   public JAXBElement<SessionContext> createSessionContext(SessionContext value)
   {
      return new JAXBElement<SessionContext>(_SessionContext_QNAME, SessionContext.class, null, value);
   }

   /** Create an instance of {@link JAXBElement }{@code <}{@link MarkupParams }{@code >}} */
   @XmlElementDecl(namespace = "urn:oasis:names:tc:wsrp:v1:types", name = "MarkupParams")
   public JAXBElement<MarkupParams> createMarkupParams(MarkupParams value)
   {
      return new JAXBElement<MarkupParams>(_MarkupParams_QNAME, MarkupParams.class, null, value);
   }

   /** Create an instance of {@link JAXBElement }{@code <}{@link ClientData }{@code >}} */
   @XmlElementDecl(namespace = "urn:oasis:names:tc:wsrp:v1:types", name = "ClientData")
   public JAXBElement<ClientData> createClientData(ClientData value)
   {
      return new JAXBElement<ClientData>(_ClientData_QNAME, ClientData.class, null, value);
   }

   /** Create an instance of {@link JAXBElement }{@code <}{@link ReturnAny }{@code >}} */
   @XmlElementDecl(namespace = "urn:oasis:names:tc:wsrp:v1:types", name = "releaseSessionsResponse")
   public JAXBElement<ReturnAny> createReleaseSessionsResponse(ReturnAny value)
   {
      return new JAXBElement<ReturnAny>(_ReleaseSessionsResponse_QNAME, ReturnAny.class, null, value);
   }

   /** Create an instance of {@link JAXBElement }{@code <}{@link InvalidCookieFault }{@code >}} */
   @XmlElementDecl(namespace = "urn:oasis:names:tc:wsrp:v1:types", name = "InvalidCookie")
   public JAXBElement<InvalidCookieFault> createInvalidCookie(InvalidCookieFault value)
   {
      return new JAXBElement<InvalidCookieFault>(_InvalidCookie_QNAME, InvalidCookieFault.class, null, value);
   }

   /** Create an instance of {@link JAXBElement }{@code <}{@link PropertyList }{@code >}} */
   @XmlElementDecl(namespace = "urn:oasis:names:tc:wsrp:v1:types", name = "getPortletPropertiesResponse")
   public JAXBElement<PropertyList> createGetPortletPropertiesResponse(PropertyList value)
   {
      return new JAXBElement<PropertyList>(_GetPortletPropertiesResponse_QNAME, PropertyList.class, null, value);
   }

   /** Create an instance of {@link JAXBElement }{@code <}{@link PortletPropertyDescriptionResponse }{@code >}} */
   @XmlElementDecl(namespace = "urn:oasis:names:tc:wsrp:v1:types", name = "getPortletPropertyDescriptionResponse")
   public JAXBElement<PortletPropertyDescriptionResponse> createGetPortletPropertyDescriptionResponse(PortletPropertyDescriptionResponse value)
   {
      return new JAXBElement<PortletPropertyDescriptionResponse>(_GetPortletPropertyDescriptionResponse_QNAME, PortletPropertyDescriptionResponse.class, null, value);
   }

   /** Create an instance of {@link JAXBElement }{@code <}{@link InvalidHandleFault }{@code >}} */
   @XmlElementDecl(namespace = "urn:oasis:names:tc:wsrp:v1:types", name = "InvalidHandle")
   public JAXBElement<InvalidHandleFault> createInvalidHandle(InvalidHandleFault value)
   {
      return new JAXBElement<InvalidHandleFault>(_InvalidHandle_QNAME, InvalidHandleFault.class, null, value);
   }

   /** Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}} */
   @XmlElementDecl(namespace = "urn:oasis:names:tc:wsrp:v1:types", name = "Handle")
   public JAXBElement<String> createHandle(String value)
   {
      return new JAXBElement<String>(_Handle_QNAME, String.class, null, value);
   }

   /** Create an instance of {@link JAXBElement }{@code <}{@link RegistrationState }{@code >}} */
   @XmlElementDecl(namespace = "urn:oasis:names:tc:wsrp:v1:types", name = "modifyRegistrationResponse")
   public JAXBElement<RegistrationState> createModifyRegistrationResponse(RegistrationState value)
   {
      return new JAXBElement<RegistrationState>(_ModifyRegistrationResponse_QNAME, RegistrationState.class, null, value);
   }

}
