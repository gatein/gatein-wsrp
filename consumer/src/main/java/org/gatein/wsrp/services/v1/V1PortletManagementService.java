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

package org.gatein.wsrp.services.v1;

import org.gatein.common.NotYetImplemented;
import org.gatein.wsrp.WSRPExceptionFactory;
import org.gatein.wsrp.WSRPUtils;
import org.gatein.wsrp.services.PortletManagementService;
import org.gatein.wsrp.spec.v1.V1ToV2Converter;
import org.gatein.wsrp.spec.v1.V2ToV1Converter;
import org.oasis.wsrp.v1.V1AccessDenied;
import org.oasis.wsrp.v1.V1DestroyFailed;
import org.oasis.wsrp.v1.V1Extension;
import org.oasis.wsrp.v1.V1InconsistentParameters;
import org.oasis.wsrp.v1.V1InvalidHandle;
import org.oasis.wsrp.v1.V1InvalidRegistration;
import org.oasis.wsrp.v1.V1InvalidUserCategory;
import org.oasis.wsrp.v1.V1MissingParameters;
import org.oasis.wsrp.v1.V1ModelDescription;
import org.oasis.wsrp.v1.V1OperationFailed;
import org.oasis.wsrp.v1.V1PortletDescription;
import org.oasis.wsrp.v1.V1Property;
import org.oasis.wsrp.v1.V1ResetProperty;
import org.oasis.wsrp.v1.V1ResourceList;
import org.oasis.wsrp.v1.WSRPV1PortletManagementPortType;
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

import javax.xml.ws.Holder;
import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class V1PortletManagementService extends PortletManagementService<WSRPV1PortletManagementPortType>
{
   public V1PortletManagementService(WSRPV1PortletManagementPortType port)
   {
      super(port);
   }

   @Override
   public void getPortletDescription(RegistrationContext registrationContext, PortletContext portletContext, UserContext userContext, List<String> desiredLocales, Holder<PortletDescription> portletDescription, Holder<ResourceList> resourceList, Holder<List<Extension>> extensions) throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      try
      {
         Holder<V1PortletDescription> v1PortletDescriptionHolder = new Holder<V1PortletDescription>();
         Holder<V1ResourceList> v1ResourceListHolder = new Holder<V1ResourceList>();
         Holder<List<V1Extension>> v1Extensions = new Holder<List<V1Extension>>();
         service.getPortletDescription(
            V2ToV1Converter.toV1RegistrationContext(registrationContext),
            V2ToV1Converter.toV1PortletContext(portletContext),
            V2ToV1Converter.toV1UserContext(userContext),
            desiredLocales,
            v1PortletDescriptionHolder,
            v1ResourceListHolder,
            v1Extensions);

         portletDescription.value = V1ToV2Converter.toV2PortletDescription(v1PortletDescriptionHolder.value);
         resourceList.value = V1ToV2Converter.toV2ResourceList(v1ResourceListHolder.value);
         extensions.value = WSRPUtils.transform(v1Extensions.value, V1ToV2Converter.EXTENSION);
      }
      catch (V1AccessDenied v1AccessDenied)
      {
         throw V1ToV2Converter.toV2Exception(AccessDenied.class, v1AccessDenied);
      }
      catch (V1InconsistentParameters v1InconsistentParameters)
      {
         WSRPExceptionFactory.throwWSException(InconsistentParameters.class, v1InconsistentParameters.getMessage(), v1InconsistentParameters);
      }
      catch (V1InvalidHandle v1InvalidHandle)
      {
         WSRPExceptionFactory.throwWSException(InvalidHandle.class, v1InvalidHandle.getMessage(), v1InvalidHandle);
      }
      catch (V1InvalidRegistration v1InvalidRegistration)
      {
         WSRPExceptionFactory.throwWSException(InvalidRegistration.class, v1InvalidRegistration.getMessage(), v1InvalidRegistration);
      }
      catch (V1InvalidUserCategory v1InvalidUserCategory)
      {
         WSRPExceptionFactory.throwWSException(InvalidUserCategory.class, v1InvalidUserCategory.getMessage(), v1InvalidUserCategory);
      }
      catch (V1MissingParameters v1MissingParameters)
      {
         WSRPExceptionFactory.throwWSException(MissingParameters.class, v1MissingParameters.getMessage(), v1MissingParameters);
      }
      catch (V1OperationFailed v1OperationFailed)
      {
         WSRPExceptionFactory.throwWSException(OperationFailed.class, v1OperationFailed.getMessage(), v1OperationFailed);
      }
   }

   @Override
   public void clonePortlet(RegistrationContext registrationContext, PortletContext portletContext, UserContext userContext, Lifetime lifetime, Holder<String> portletHandle, Holder<byte[]> portletState, Holder<Lifetime> scheduledDestruction, Holder<List<Extension>> extensions) throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      try
      {
         Holder<List<V1Extension>> v1Extensions = new Holder<List<V1Extension>>();
         service.clonePortlet(
            V2ToV1Converter.toV1RegistrationContext(registrationContext),
            V2ToV1Converter.toV1PortletContext(portletContext),
            V2ToV1Converter.toV1UserContext(userContext),
            portletHandle,
            portletState,
            v1Extensions
         );
         extensions.value = WSRPUtils.transform(v1Extensions.value, V1ToV2Converter.EXTENSION);
      }
      catch (V1AccessDenied v1AccessDenied)
      {
         WSRPExceptionFactory.throwWSException(AccessDenied.class, v1AccessDenied.getMessage(), v1AccessDenied);
      }
      catch (V1InconsistentParameters v1InconsistentParameters)
      {
         WSRPExceptionFactory.throwWSException(InconsistentParameters.class, v1InconsistentParameters.getMessage(), v1InconsistentParameters);
      }
      catch (V1InvalidHandle v1InvalidHandle)
      {
         WSRPExceptionFactory.throwWSException(InvalidHandle.class, v1InvalidHandle.getMessage(), v1InvalidHandle);
      }
      catch (V1InvalidRegistration v1InvalidRegistration)
      {
         WSRPExceptionFactory.throwWSException(InvalidRegistration.class, v1InvalidRegistration.getMessage(), v1InvalidRegistration);
      }
      catch (V1InvalidUserCategory v1InvalidUserCategory)
      {
         WSRPExceptionFactory.throwWSException(InvalidUserCategory.class, v1InvalidUserCategory.getMessage(), v1InvalidUserCategory);
      }
      catch (V1MissingParameters v1MissingParameters)
      {
         WSRPExceptionFactory.throwWSException(MissingParameters.class, v1MissingParameters.getMessage(), v1MissingParameters);
      }
      catch (V1OperationFailed v1OperationFailed)
      {
         WSRPExceptionFactory.throwWSException(OperationFailed.class, v1OperationFailed.getMessage(), v1OperationFailed);
      }
   }

   @Override
   public void destroyPortlets(RegistrationContext registrationContext, List<String> portletHandles, UserContext userContext, Holder<List<FailedPortlets>> failedPortlets, Holder<List<Extension>> extensions) throws InconsistentParameters, InvalidRegistration, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      try
      {
         Holder<List<V1DestroyFailed>> destroyFailed = new Holder<List<V1DestroyFailed>>();
         Holder<List<V1Extension>> v1Extensions = new Holder<List<V1Extension>>();
         service.destroyPortlets(
            V2ToV1Converter.toV1RegistrationContext(registrationContext),
            portletHandles,
            destroyFailed,
            v1Extensions
         );

         failedPortlets.value = V1ToV2Converter.toV2FailedPortlets(destroyFailed.value);
         extensions.value = WSRPUtils.transform(v1Extensions.value, V1ToV2Converter.EXTENSION);
      }
      catch (V1InconsistentParameters v1InconsistentParameters)
      {
         WSRPExceptionFactory.throwWSException(InconsistentParameters.class, v1InconsistentParameters.getMessage(), v1InconsistentParameters);
      }
      catch (V1InvalidRegistration v1InvalidRegistration)
      {
         WSRPExceptionFactory.throwWSException(InvalidRegistration.class, v1InvalidRegistration.getMessage(), v1InvalidRegistration);
      }
      catch (V1MissingParameters v1MissingParameters)
      {
         WSRPExceptionFactory.throwWSException(MissingParameters.class, v1MissingParameters.getMessage(), v1MissingParameters);
      }
      catch (V1OperationFailed v1OperationFailed)
      {
         WSRPExceptionFactory.throwWSException(OperationFailed.class, v1OperationFailed.getMessage(), v1OperationFailed);
      }
   }

   @Override
   public void getPortletsLifetime(RegistrationContext registrationContext, List<PortletContext> portletContext, UserContext userContext, Holder<List<PortletLifetime>> portletLifetime, Holder<List<FailedPortlets>> failedPortlets, Holder<ResourceList> resourceList, Holder<List<Extension>> extensions) throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      WSRPExceptionFactory.throwWSException(OperationNotSupported.class, "getPortletsLifetime not supported in WSRP 1", null);
   }

   @Override
   public void setPortletsLifetime(RegistrationContext registrationContext, List<PortletContext> portletContext, UserContext userContext, Lifetime lifetime, Holder<List<PortletLifetime>> updatedPortlet, Holder<List<FailedPortlets>> failedPortlets, Holder<ResourceList> resourceList, Holder<List<Extension>> extensions) throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      WSRPExceptionFactory.throwWSException(OperationNotSupported.class, "setPortletsLifetime not supported in WSRP 1", null);
   }

   @Override
   public void copyPortlets(RegistrationContext toRegistrationContext, UserContext toUserContext, RegistrationContext fromRegistrationContext, UserContext fromUserContext, List<PortletContext> fromPortletContexts, Lifetime lifetime, Holder<List<CopiedPortlet>> copiedPortlets, Holder<List<FailedPortlets>> failedPortlets, Holder<ResourceList> resourceList, Holder<List<Extension>> extensions) throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      WSRPExceptionFactory.throwWSException(OperationNotSupported.class, "copyPortlets not supported in WSRP 1", null);
   }

   @Override
   public void exportPortlets(RegistrationContext registrationContext, List<PortletContext> portletContext, UserContext userContext, Holder<Lifetime> lifetime, Boolean exportByValueRequired, Holder<byte[]> exportContext, Holder<List<ExportedPortlet>> exportedPortlet, Holder<List<FailedPortlets>> failedPortlets, Holder<ResourceList> resourceList, Holder<List<Extension>> extensions) throws AccessDenied, ExportByValueNotSupported, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      WSRPExceptionFactory.throwWSException(OperationNotSupported.class, "exportPortlets not supported in WSRP 1", null);
   }

   @Override
   public void importPortlets(RegistrationContext registrationContext, byte[] importContext, List<ImportPortlet> importPortlet, UserContext userContext, Lifetime lifetime, Holder<List<ImportedPortlet>> importedPortlets, Holder<List<ImportPortletsFailed>> importFailed, Holder<ResourceList> resourceList, Holder<List<Extension>> extensions) throws AccessDenied, ExportNoLongerValid, InconsistentParameters, InvalidRegistration, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      WSRPExceptionFactory.throwWSException(OperationNotSupported.class, "importPortlets not supported in WSRP 1", null);
   }

   @Override
   public List<Extension> releaseExport(RegistrationContext registrationContext, byte[] exportContext, UserContext userContext)
   {
      throw new NotYetImplemented(); // WFT? this operation cannot fail? :)
   }

   @Override
   public Lifetime setExportLifetime(SetExportLifetime setExportLifetime) throws AccessDenied, InvalidHandle, InvalidRegistration, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      throw WSRPExceptionFactory.throwWSException(OperationNotSupported.class, "setExportLifetime not supported in WSRP 1", null);
   }

   @Override
   public void setPortletProperties(RegistrationContext registrationContext, PortletContext portletContext, UserContext userContext, PropertyList propertyList, Holder<String> portletHandle, Holder<byte[]> portletState, Holder<Lifetime> scheduledDestruction, Holder<List<Extension>> extensions) throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      try
      {
         Holder<List<V1Extension>> v1Extensions = new Holder<List<V1Extension>>();
         service.setPortletProperties(
            V2ToV1Converter.toV1RegistrationContext(registrationContext),
            V2ToV1Converter.toV1PortletContext(portletContext),
            V2ToV1Converter.toV1UserContext(userContext),
            V2ToV1Converter.toV1PropertyList(propertyList),
            portletHandle,
            portletState,
            v1Extensions
         );
         extensions.value = WSRPUtils.transform(v1Extensions.value, V1ToV2Converter.EXTENSION);
      }
      catch (V1AccessDenied v1AccessDenied)
      {
         WSRPExceptionFactory.throwWSException(AccessDenied.class, v1AccessDenied.getMessage(), v1AccessDenied);
      }
      catch (V1InconsistentParameters v1InconsistentParameters)
      {
         WSRPExceptionFactory.throwWSException(InconsistentParameters.class, v1InconsistentParameters.getMessage(), v1InconsistentParameters);
      }
      catch (V1InvalidHandle v1InvalidHandle)
      {
         WSRPExceptionFactory.throwWSException(InvalidHandle.class, v1InvalidHandle.getMessage(), v1InvalidHandle);
      }
      catch (V1InvalidRegistration v1InvalidRegistration)
      {
         WSRPExceptionFactory.throwWSException(InvalidRegistration.class, v1InvalidRegistration.getMessage(), v1InvalidRegistration);
      }
      catch (V1InvalidUserCategory v1InvalidUserCategory)
      {
         WSRPExceptionFactory.throwWSException(InvalidUserCategory.class, v1InvalidUserCategory.getMessage(), v1InvalidUserCategory);
      }
      catch (V1MissingParameters v1MissingParameters)
      {
         WSRPExceptionFactory.throwWSException(MissingParameters.class, v1MissingParameters.getMessage(), v1MissingParameters);
      }
      catch (V1OperationFailed v1OperationFailed)
      {
         WSRPExceptionFactory.throwWSException(OperationFailed.class, v1OperationFailed.getMessage(), v1OperationFailed);
      }
   }

   @Override
   public void getPortletProperties(RegistrationContext registrationContext, PortletContext portletContext, UserContext userContext, List<String> names, Holder<List<Property>> properties, Holder<List<ResetProperty>> resetProperties, Holder<List<Extension>> extensions) throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      try
      {
         Holder<List<V1Property>> v1PropertyList = new Holder<List<V1Property>>();
         Holder<List<V1ResetProperty>> v1ResetPropertyList = new Holder<List<V1ResetProperty>>();
         Holder<List<V1Extension>> v1Extensions = new Holder<List<V1Extension>>();
         service.getPortletProperties(
            V2ToV1Converter.toV1RegistrationContext(registrationContext),
            V2ToV1Converter.toV1PortletContext(portletContext),
            V2ToV1Converter.toV1UserContext(userContext),
            names,
            v1PropertyList,
            v1ResetPropertyList,
            v1Extensions
         );
         properties.value = WSRPUtils.transform(v1PropertyList.value, V1ToV2Converter.PROPERTY);
         resetProperties.value = WSRPUtils.transform(v1ResetPropertyList.value, V1ToV2Converter.RESETPROPERTY);
         extensions.value = WSRPUtils.transform(v1Extensions.value, V1ToV2Converter.EXTENSION);
      }
      catch (V1AccessDenied v1AccessDenied)
      {
         WSRPExceptionFactory.throwWSException(AccessDenied.class, v1AccessDenied.getMessage(), v1AccessDenied);
      }
      catch (V1InconsistentParameters v1InconsistentParameters)
      {
         WSRPExceptionFactory.throwWSException(InconsistentParameters.class, v1InconsistentParameters.getMessage(), v1InconsistentParameters);
      }
      catch (V1InvalidHandle v1InvalidHandle)
      {
         WSRPExceptionFactory.throwWSException(InvalidHandle.class, v1InvalidHandle.getMessage(), v1InvalidHandle);
      }
      catch (V1InvalidRegistration v1InvalidRegistration)
      {
         WSRPExceptionFactory.throwWSException(InvalidRegistration.class, v1InvalidRegistration.getMessage(), v1InvalidRegistration);
      }
      catch (V1InvalidUserCategory v1InvalidUserCategory)
      {
         WSRPExceptionFactory.throwWSException(InvalidUserCategory.class, v1InvalidUserCategory.getMessage(), v1InvalidUserCategory);
      }
      catch (V1MissingParameters v1MissingParameters)
      {
         WSRPExceptionFactory.throwWSException(MissingParameters.class, v1MissingParameters.getMessage(), v1MissingParameters);
      }
      catch (V1OperationFailed v1OperationFailed)
      {
         WSRPExceptionFactory.throwWSException(OperationFailed.class, v1OperationFailed.getMessage(), v1OperationFailed);
      }
   }

   @Override
   public void getPortletPropertyDescription(RegistrationContext registrationContext, PortletContext portletContext, UserContext userContext, List<String> desiredLocales, Holder<ModelDescription> modelDescription, Holder<ResourceList> resourceList, Holder<List<Extension>> extensions) throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory, MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      try
      {
         Holder<V1ResourceList> v1ResourceListHolder = new Holder<V1ResourceList>();
         Holder<List<V1Extension>> v1Extensions = new Holder<List<V1Extension>>();
         Holder<V1ModelDescription> v1ModelDescription = new Holder<V1ModelDescription>();

         service.getPortletPropertyDescription(
            V2ToV1Converter.toV1RegistrationContext(registrationContext),
            V2ToV1Converter.toV1PortletContext(portletContext),
            V2ToV1Converter.toV1UserContext(userContext),
            desiredLocales,
            v1ModelDescription,
            v1ResourceListHolder,
            v1Extensions);

         modelDescription.value = V1ToV2Converter.toV2ModelDescription(v1ModelDescription.value);
         resourceList.value = V1ToV2Converter.toV2ResourceList(v1ResourceListHolder.value);
         extensions.value = WSRPUtils.transform(v1Extensions.value, V1ToV2Converter.EXTENSION);
      }
      catch (V1AccessDenied v1AccessDenied)
      {
         WSRPExceptionFactory.throwWSException(AccessDenied.class, v1AccessDenied.getMessage(), v1AccessDenied);
      }
      catch (V1InconsistentParameters v1InconsistentParameters)
      {
         WSRPExceptionFactory.throwWSException(InconsistentParameters.class, v1InconsistentParameters.getMessage(), v1InconsistentParameters);
      }
      catch (V1InvalidHandle v1InvalidHandle)
      {
         WSRPExceptionFactory.throwWSException(InvalidHandle.class, v1InvalidHandle.getMessage(), v1InvalidHandle);
      }
      catch (V1InvalidRegistration v1InvalidRegistration)
      {
         WSRPExceptionFactory.throwWSException(InvalidRegistration.class, v1InvalidRegistration.getMessage(), v1InvalidRegistration);
      }
      catch (V1InvalidUserCategory v1InvalidUserCategory)
      {
         WSRPExceptionFactory.throwWSException(InvalidUserCategory.class, v1InvalidUserCategory.getMessage(), v1InvalidUserCategory);
      }
      catch (V1MissingParameters v1MissingParameters)
      {
         WSRPExceptionFactory.throwWSException(MissingParameters.class, v1MissingParameters.getMessage(), v1MissingParameters);
      }
      catch (V1OperationFailed v1OperationFailed)
      {
         WSRPExceptionFactory.throwWSException(OperationFailed.class, v1OperationFailed.getMessage(), v1OperationFailed);
      }
   }
}
