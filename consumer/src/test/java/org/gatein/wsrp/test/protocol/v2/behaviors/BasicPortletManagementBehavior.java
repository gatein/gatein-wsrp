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

package org.gatein.wsrp.test.protocol.v2.behaviors;

import org.gatein.common.NotYetImplemented;
import org.gatein.common.util.ParameterValidation;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.spec.v2.ErrorCodes;
import org.gatein.wsrp.spec.v2.WSRP2ExceptionFactory;
import org.gatein.wsrp.test.protocol.v2.BehaviorRegistry;
import org.gatein.wsrp.test.protocol.v2.MarkupBehavior;
import org.gatein.wsrp.test.protocol.v2.PortletManagementBehavior;
import org.oasis.wsrp.v2.AccessDenied;
import org.oasis.wsrp.v2.CopiedPortlet;
import org.oasis.wsrp.v2.ExportByValueNotSupported;
import org.oasis.wsrp.v2.ExportNoLongerValid;
import org.oasis.wsrp.v2.ExportedPortlet;
import org.oasis.wsrp.v2.Extension;
import org.oasis.wsrp.v2.FailedPortlets;
import org.oasis.wsrp.v2.ImportPortlet;
import org.oasis.wsrp.v2.ImportPortletsFailed;
import org.oasis.wsrp.v2.ImportedPortlet;
import org.oasis.wsrp.v2.InconsistentParameters;
import org.oasis.wsrp.v2.InvalidHandle;
import org.oasis.wsrp.v2.InvalidRegistration;
import org.oasis.wsrp.v2.InvalidUserCategory;
import org.oasis.wsrp.v2.Lifetime;
import org.oasis.wsrp.v2.MissingParameters;
import org.oasis.wsrp.v2.ModelDescription;
import org.oasis.wsrp.v2.ModifyRegistrationRequired;
import org.oasis.wsrp.v2.OperationFailed;
import org.oasis.wsrp.v2.OperationNotSupported;
import org.oasis.wsrp.v2.PortletContext;
import org.oasis.wsrp.v2.PortletDescription;
import org.oasis.wsrp.v2.PortletLifetime;
import org.oasis.wsrp.v2.Property;
import org.oasis.wsrp.v2.PropertyList;
import org.oasis.wsrp.v2.RegistrationContext;
import org.oasis.wsrp.v2.ResetProperty;
import org.oasis.wsrp.v2.ResourceList;
import org.oasis.wsrp.v2.ResourceSuspended;
import org.oasis.wsrp.v2.SetExportLifetime;
import org.oasis.wsrp.v2.UserContext;

import javax.jws.WebParam;
import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 8784 $
 * @since 2.6
 */
public class BasicPortletManagementBehavior extends PortletManagementBehavior
{
   private static final String CLONE_SUFFIX = "_clone";
   public static final String PROPERTY_NAME = "prop1";
   public static final String PROPERTY_VALUE = "value1";
   public static final String PROPERTY_NEW_VALUE = "value2";
   public static final String CLONED_HANDLE = BasicMarkupBehavior.PORTLET_HANDLE + CLONE_SUFFIX;
   public static final String CANNOT_BOTH_SET_AND_RESET_A_PROPERTY_AT_THE_SAME_TIME = "Cannot both set and reset a property at the same time!";
   private BehaviorRegistry registry;
   private String propValue = PROPERTY_VALUE;

   public BasicPortletManagementBehavior(BehaviorRegistry registry)
   {
      super();
      this.registry = registry;
   }

   @Override
   public void getPortletPropertyDescription(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") PortletContext portletContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext, @WebParam(name = "desiredLocales", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") List<String> desiredLocales, @WebParam(name = "modelDescription", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<ModelDescription> modelDescription, @WebParam(name = "resourceList", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<ResourceList> resourceList, @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions) throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      throw new NotYetImplemented();
   }

   @Override
   public void getPortletDescription(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") PortletContext portletContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext, @WebParam(name = "desiredLocales", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") List<String> desiredLocales, @WebParam(name = "portletDescription", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<PortletDescription> portletDescription, @WebParam(name = "resourceList", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<ResourceList> resourceList, @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions) throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      super.getPortletDescription(registrationContext, portletContext, userContext, desiredLocales, portletDescription, resourceList, extensions);

      String handle = getHandleFrom(portletContext, "portlet context");

      // need to fake that the clone exists... so remove suffix to get the description of the POP
      int index = handle.indexOf(CLONE_SUFFIX);
      if (index != -1)
      {
         handle = handle.substring(0, index);
      }

      // get the POP description...
      MarkupBehavior markupBehaviorFor = registry.getMarkupBehaviorFor(handle);
      PortletDescription description = markupBehaviorFor.getPortletDescriptionFor(handle);

      // if it was a clone, add the suffix back to the handle.
      if (index != -1)
      {
         description.setPortletHandle(handle + CLONE_SUFFIX);
      }

      portletDescription.value = description;
   }

   @Override
   public void clonePortlet(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") PortletContext portletContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext, @WebParam(name = "lifetime", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") Lifetime lifetime, @WebParam(name = "portletHandle", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<String> portletHandle, @WebParam(name = "portletState", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<byte[]> portletState, @WebParam(name = "scheduledDestruction", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<Lifetime> scheduledDestruction, @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions) throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      String handle = getHandleFrom(portletContext, "portlet context");

      if (!BasicMarkupBehavior.PORTLET_HANDLE.equals(handle))
      {
         throw WSRP2ExceptionFactory.throwWSException(InvalidHandle.class, "Can only clone portlet with handle '" + BasicMarkupBehavior.PORTLET_HANDLE + "'", null);
      }

      portletHandle.value = CLONED_HANDLE;
   }

   @Override
   public void destroyPortlets(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext, @WebParam(name = "portletHandles", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") List<String> portletHandles, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext, @WebParam(name = "failedPortlets", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<FailedPortlets>> failedPortlets, @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions) throws InconsistentParameters, InvalidRegistration, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      super.destroyPortlets(registrationContext, portletHandles, userContext, failedPortlets, extensions);
      WSRP2ExceptionFactory.throwMissingParametersIfValueIsMissing(portletHandles, "portlet handles", "destroyPortlets");
      if (portletHandles.isEmpty())
      {
         WSRP2ExceptionFactory.throwMissingParametersIfValueIsMissing(portletHandles, "portlet handles", "DestroyPortlets");
      }

      List<FailedPortlets> failedPortletsList = failedPortlets.value;
      if (failedPortletsList == null)
      {
         failedPortletsList = new ArrayList<FailedPortlets>();
         failedPortlets.value = failedPortletsList;
      }

      for (String handle : portletHandles)
      {
         if (!CLONED_HANDLE.equals(handle))
         {
            ArrayList<String> failed = new ArrayList<String>();
            failed.add(handle);
            failedPortletsList.add(WSRPTypeFactory.createFailedPortlets(failed, ErrorCodes.Codes.OPERATIONFAILED, "Handle '" + handle + "' doesn't exist"));
         }
      }
   }

   @Override
   public void getPortletsLifetime(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") List<PortletContext> portletContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext, @WebParam(name = "portletLifetime", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<PortletLifetime>> portletLifetime, @WebParam(name = "failedPortlets", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<FailedPortlets>> failedPortlets, @WebParam(name = "resourceList", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<ResourceList> resourceList, @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions) throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      throw new NotYetImplemented();
   }

   @Override
   public void setPortletsLifetime(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") List<PortletContext> portletContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext, @WebParam(name = "lifetime", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") Lifetime lifetime, @WebParam(name = "updatedPortlet", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<PortletLifetime>> updatedPortlet, @WebParam(name = "failedPortlets", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<FailedPortlets>> failedPortlets, @WebParam(name = "resourceList", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<ResourceList> resourceList, @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions) throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      throw new NotYetImplemented();
   }

   @Override
   public void copyPortlets(@WebParam(name = "toRegistrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext toRegistrationContext, @WebParam(name = "toUserContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext toUserContext, @WebParam(name = "fromRegistrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext fromRegistrationContext, @WebParam(name = "fromUserContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext fromUserContext, @WebParam(name = "fromPortletContexts", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") List<PortletContext> fromPortletContexts, @WebParam(name = "lifetime", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") Lifetime lifetime, @WebParam(name = "copiedPortlets", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<CopiedPortlet>> copiedPortlets, @WebParam(name = "failedPortlets", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<FailedPortlets>> failedPortlets, @WebParam(name = "resourceList", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<ResourceList> resourceList, @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions) throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      throw new NotYetImplemented();
   }

   @Override
   public void exportPortlets(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") List<PortletContext> portletContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext, @WebParam(name = "lifetime", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.INOUT) Holder<Lifetime> lifetime, @WebParam(name = "exportByValueRequired", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") Boolean exportByValueRequired, @WebParam(name = "exportContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<byte[]> exportContext, @WebParam(name = "exportedPortlet", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<ExportedPortlet>> exportedPortlet, @WebParam(name = "failedPortlets", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<FailedPortlets>> failedPortlets, @WebParam(name = "resourceList", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<ResourceList> resourceList, @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions) throws AccessDenied, ExportByValueNotSupported, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      throw new NotYetImplemented();
   }

   @Override
   public void importPortlets(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext, @WebParam(name = "importContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") byte[] importContext, @WebParam(name = "importPortlet", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") List<ImportPortlet> importPortlet, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext, @WebParam(name = "lifetime", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") Lifetime lifetime, @WebParam(name = "importedPortlets", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<ImportedPortlet>> importedPortlets, @WebParam(name = "importFailed", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<ImportPortletsFailed>> importFailed, @WebParam(name = "resourceList", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<ResourceList> resourceList, @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions) throws AccessDenied, ExportNoLongerValid, InconsistentParameters, InvalidRegistration, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      throw new NotYetImplemented();
   }

   @Override
   public List<Extension> releaseExport(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext, @WebParam(name = "exportContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") byte[] exportContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext)
   {
      throw new NotYetImplemented();
   }

   @Override
   public Lifetime setExportLifetime(@WebParam(name = "setExportLifetime", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", partName = "setExportLifetime") SetExportLifetime setExportLifetime) throws AccessDenied, InvalidHandle, InvalidRegistration, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      throw new NotYetImplemented();
   }

   @Override
   public void setPortletProperties(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") PortletContext portletContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext, @WebParam(name = "propertyList", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") PropertyList propertyList, @WebParam(name = "portletHandle", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<String> portletHandle, @WebParam(name = "portletState", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<byte[]> portletState, @WebParam(name = "scheduledDestruction", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<Lifetime> scheduledDestruction, @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions) throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      String handle = getHandleFrom(portletContext, "portlet context");

      if (!(CLONED_HANDLE).equals(handle))
      {
         throw WSRP2ExceptionFactory.throwWSException(OperationFailed.class, "Cannot modify portlet '" + handle + "'", null);
      }

      List<Property> properties = propertyList.getProperties();
      List<ResetProperty> resetProperties = propertyList.getResetProperties();

      // check that we don't try to set and reset the same property
      if (ParameterValidation.existsAndIsNotEmpty(properties) && ParameterValidation.existsAndIsNotEmpty(resetProperties))
      {
         if (properties.size() != 1 && resetProperties.size() != 1)
         {
            WSRP2ExceptionFactory.throwWSException(InconsistentParameters.class, "Invalid number of properties!", null);
         }

         if (properties.get(0).getName().equals(resetProperties.get(0).getName()))
         {
            WSRP2ExceptionFactory.throwWSException(InconsistentParameters.class, CANNOT_BOTH_SET_AND_RESET_A_PROPERTY_AT_THE_SAME_TIME, null);
         }
      }

      // if we have the expected property name, update the value
      if (ParameterValidation.existsAndIsNotEmpty(properties))
      {
         if (properties.size() != 1)
         {
            WSRP2ExceptionFactory.throwWSException(InconsistentParameters.class, "Invalid number of properties!", null);
         }

         Property property = properties.get(0);
         QName name = property.getName();
         if (name.getLocalPart().equals(PROPERTY_NAME))
         {
            propValue = property.getStringValue();
         }
         else
         {
            WSRP2ExceptionFactory.throwWSException(InconsistentParameters.class, "Unknown property '" + name + "'", null);
         }
      }

      // if we have the proper reset property, reset it
      if (ParameterValidation.existsAndIsNotEmpty(resetProperties))
      {
         if (resetProperties.size() != 1)
         {
            WSRP2ExceptionFactory.throwWSException(InconsistentParameters.class, "Invalid number of reset properties!", null);
         }

         ResetProperty resetProperty = resetProperties.get(0);
         QName name = resetProperty.getName();
         if (name.getLocalPart().equals(PROPERTY_NAME))
         {
            propValue = PROPERTY_VALUE;
         }
         else
         {
            WSRP2ExceptionFactory.throwWSException(InconsistentParameters.class, "Unknown property '" + name + "'", null);
         }
      }


      portletHandle.value = handle;
   }

   @Override
   public void getPortletProperties(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") PortletContext portletContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") UserContext userContext, @WebParam(name = "names", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types") List<String> names, @WebParam(name = "properties", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Property>> properties, @WebParam(name = "resetProperties", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<ResetProperty>> resetProperties, @WebParam(name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v2:types", mode = WebParam.Mode.OUT) Holder<List<Extension>> extensions) throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      String handle = getHandleFrom(portletContext, "portlet context");

      List<Property> propertyList = new ArrayList<Property>(1);

      if (BasicMarkupBehavior.PORTLET_HANDLE.equals(handle))
      {
         propertyList.add(WSRPTypeFactory.createProperty(PROPERTY_NAME, "en", PROPERTY_VALUE));
      }
      else if (CLONED_HANDLE.equals(handle))
      {
         if (propValue != null)
         {
            propertyList.add(WSRPTypeFactory.createProperty(PROPERTY_NAME, "en", propValue));
         }
         if (callCount > 4)
         {
            throw new IllegalStateException("Shouldn't have been called more than four times!");
         }
      }
      else
      {
         WSRP2ExceptionFactory.throwWSException(InvalidHandle.class, "Unknown handle '" + handle + "'", null);
      }

      incrementCallCount();
      properties.value = propertyList;
   }

   private String getHandleFrom(PortletContext portletContext, String context) throws MissingParameters, InvalidHandle
   {
      WSRP2ExceptionFactory.throwMissingParametersIfValueIsMissing(portletContext, "portlet context", context);
      String handle = portletContext.getPortletHandle();
      WSRP2ExceptionFactory.throwMissingParametersIfValueIsMissing(handle, "portlet handle", "PortletContext");
      if (handle.length() == 0)
      {
         throw WSRP2ExceptionFactory.throwWSException(InvalidHandle.class, "Portlet handle is empty", null);
      }

      return handle;
   }
}
