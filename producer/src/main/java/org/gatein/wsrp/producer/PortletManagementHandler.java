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

package org.gatein.wsrp.producer;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import org.gatein.common.i18n.LocalizedString;
import org.gatein.pc.api.InvalidPortletIdException;
import org.gatein.pc.api.NoSuchPortletException;
import org.gatein.pc.api.Portlet;
import org.gatein.pc.api.PortletInvokerException;
import org.gatein.pc.api.PortletStateType;
import org.gatein.pc.api.info.PortletInfo;
import org.gatein.pc.api.info.PreferenceInfo;
import org.gatein.pc.api.info.PreferencesInfo;
import org.gatein.pc.api.state.DestroyCloneFailure;
import org.gatein.pc.api.state.PropertyChange;
import org.gatein.pc.api.state.PropertyMap;
import org.gatein.registration.Registration;
import org.gatein.registration.RegistrationLocal;
import org.gatein.wsrp.WSRPConstants;
import org.gatein.wsrp.WSRPExceptionFactory;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.WSRPUtils;
import org.oasis.wsrp.v2.AccessDenied;
import org.oasis.wsrp.v2.ClonePortlet;
import org.oasis.wsrp.v2.DestroyPortlets;
import org.oasis.wsrp.v2.DestroyPortletsResponse;
import org.oasis.wsrp.v2.FailedPortlets;
import org.oasis.wsrp.v2.GetPortletDescription;
import org.oasis.wsrp.v2.GetPortletProperties;
import org.oasis.wsrp.v2.GetPortletPropertyDescription;
import org.oasis.wsrp.v2.InconsistentParameters;
import org.oasis.wsrp.v2.InconsistentParametersFault;
import org.oasis.wsrp.v2.InvalidHandle;
import org.oasis.wsrp.v2.InvalidHandleFault;
import org.oasis.wsrp.v2.InvalidRegistration;
import org.oasis.wsrp.v2.InvalidUserCategory;
import org.oasis.wsrp.v2.MissingParameters;
import org.oasis.wsrp.v2.OperationFailed;
import org.oasis.wsrp.v2.OperationFailedFault;
import org.oasis.wsrp.v2.PortletContext;
import org.oasis.wsrp.v2.PortletDescription;
import org.oasis.wsrp.v2.PortletDescriptionResponse;
import org.oasis.wsrp.v2.PortletPropertyDescriptionResponse;
import org.oasis.wsrp.v2.Property;
import org.oasis.wsrp.v2.PropertyDescription;
import org.oasis.wsrp.v2.PropertyList;
import org.oasis.wsrp.v2.ResetProperty;
import org.oasis.wsrp.v2.SetPortletProperties;
import org.oasis.wsrp.v2.UserContext;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 11147 $
 * @since 2.4
 */
class PortletManagementHandler extends ServiceHandler implements PortletManagementInterface
{
   private static final String GET_PORTLET_PROPERTY_DESCRIPTION = "GetPortletPropertyDescription";
   private static final String GET_PORTLET_PROPERTIES = "GetPortletProperties";
   private static final String PORTLET_CONTEXT = "PortletContext";
   private static final String GET_PORTLET_DESCRIPTION = "GetPortletDescription";

   PortletManagementHandler(WSRPProducerImpl producer)
   {
      super(producer);
   }

   public PortletDescriptionResponse getPortletDescription(GetPortletDescription getPortletDescription)
      throws AccessDenied, InvalidHandle, InvalidUserCategory, InconsistentParameters,
      MissingParameters, InvalidRegistration, OperationFailed
   {
      WSRPExceptionFactory.throwOperationFailedIfValueIsMissing(getPortletDescription, GET_PORTLET_DESCRIPTION);
      Registration registration = producer.getRegistrationOrFailIfInvalid(getPortletDescription.getRegistrationContext());

      PortletContext portletContext = getPortletDescription.getPortletContext();
      WSRPExceptionFactory.throwMissingParametersIfValueIsMissing(portletContext, PORTLET_CONTEXT, GET_PORTLET_DESCRIPTION);

      UserContext userContext = getPortletDescription.getUserContext();
      checkUserAuthorization(userContext);

      // RegistrationLocal.setRegistration is called further down the invocation in ServiceDescriptionHandler.getPortletDescription 
      PortletDescription description = producer.getPortletDescription(portletContext, getPortletDescription.getDesiredLocales(), registration);
      return WSRPTypeFactory.createPortletDescriptionResponse(description);
   }

   public PortletPropertyDescriptionResponse getPortletPropertyDescription(GetPortletPropertyDescription getPortletPropertyDescription)
      throws MissingParameters, InconsistentParameters, InvalidUserCategory, InvalidRegistration, AccessDenied,
      InvalidHandle, OperationFailed
   {
      WSRPExceptionFactory.throwOperationFailedIfValueIsMissing(getPortletPropertyDescription, GET_PORTLET_PROPERTY_DESCRIPTION);

      PortletContext portletContext = getPortletPropertyDescription.getPortletContext();
      WSRPExceptionFactory.throwMissingParametersIfValueIsMissing(portletContext, PORTLET_CONTEXT, GET_PORTLET_PROPERTY_DESCRIPTION);

      Registration registration = producer.getRegistrationOrFailIfInvalid(getPortletPropertyDescription.getRegistrationContext());

      UserContext userContext = getPortletPropertyDescription.getUserContext();
      checkUserAuthorization(userContext);

      List<String> desiredLocales = getPortletPropertyDescription.getDesiredLocales();
      Portlet portlet = getPortletFrom(portletContext, registration);
      PortletInfo info = portlet.getInfo();
      PreferencesInfo prefsInfo = info.getPreferences();

      List<PropertyDescription> descs = Collections.emptyList();
      if (prefsInfo != null)
      {
         Set keySet = prefsInfo.getKeys();
         descs = new ArrayList<PropertyDescription>(keySet.size());
         int index = 0;
         for (Object key : keySet)
         {
            PreferenceInfo prefInfo = prefsInfo.getPreference((String)key);

            // WSRP Spec 8.7: return only the portion of the Portlet's persistent state the user is allowed to modify
            // if read only status is not determined, we consider it as being read-only to be safe
            Boolean readOnly = prefInfo.isReadOnly();
            if (readOnly != null && !readOnly)
            {
               //todo: check what we should use key
               //todo: right now we only support String properties
               PropertyDescription desc = WSRPTypeFactory.createPropertyDescription(prefInfo.getKey(), WSRPConstants.XSD_STRING);
               desc.setLabel(Utils.convertToWSRPLocalizedString(prefInfo.getDisplayName(), desiredLocales));
               desc.setHint(Utils.convertToWSRPLocalizedString(prefInfo.getDescription(), desiredLocales));
               descs.add(desc);
            }
         }
      }

      return WSRPTypeFactory.createPortletPropertyDescriptionResponse(descs);
   }

   public PortletContext clonePortlet(ClonePortlet clonePortlet) throws InvalidUserCategory, AccessDenied, OperationFailed,
      InvalidHandle, InvalidRegistration, InconsistentParameters, MissingParameters
   {
      WSRPExceptionFactory.throwOperationFailedIfValueIsMissing(clonePortlet, "ClonePortlet");

      PortletContext portletContext = clonePortlet.getPortletContext();
      WSRPExceptionFactory.throwMissingParametersIfValueIsMissing(portletContext, "PortletContext", "ClonePortlet");

      Registration registration = producer.getRegistrationOrFailIfInvalid(clonePortlet.getRegistrationContext());

      UserContext userContext = clonePortlet.getUserContext();
      checkUserAuthorization(userContext);

      org.gatein.pc.api.PortletContext portalPC = WSRPUtils.convertToPortalPortletContext(portletContext);
      try
      {
         RegistrationLocal.setRegistration(registration);
         org.gatein.pc.api.PortletContext response = producer.getPortletInvoker().createClone(PortletStateType.OPAQUE, portalPC);
         return WSRPUtils.convertToWSRPPortletContext(response);
      }
      catch (NoSuchPortletException e)
      {
         throw WSRPExceptionFactory.<InvalidHandle, InvalidHandleFault>throwWSException(WSRPExceptionFactory.INVALID_HANDLE,
            "Failed to create clone for portlet '" + portletContext.getPortletHandle(), e);
      }
      catch (InvalidPortletIdException e)
      {
         throw WSRPExceptionFactory.<InconsistentParameters, InconsistentParametersFault>throwWSException(WSRPExceptionFactory.INCONSISTENT_PARAMETERS,
            "Failed to create clone for portlet '" + portletContext.getPortletHandle(), e);
      }
      catch (PortletInvokerException e)
      {
         throw WSRPExceptionFactory.<OperationFailed, OperationFailedFault>throwWSException(WSRPExceptionFactory.OPERATION_FAILED,
            "Failed to create clone for portlet '" + portletContext.getPortletHandle(), e);
      }
      finally
      {
         RegistrationLocal.setRegistration(null);
      }
   }

   public DestroyPortletsResponse destroyPortlets(DestroyPortlets destroyPortlets) throws InconsistentParameters,
      MissingParameters, InvalidRegistration, OperationFailed
   {
      WSRPExceptionFactory.throwOperationFailedIfValueIsMissing(destroyPortlets, "DestroyPortlets");

      List<String> handles = destroyPortlets.getPortletHandles();
      WSRPExceptionFactory.throwMissingParametersIfValueIsMissing(handles, "portlet handles to be destroyed", "DestroyPortlets");

      Registration registration = producer.getRegistrationOrFailIfInvalid(destroyPortlets.getRegistrationContext());

      List<org.gatein.pc.api.PortletContext> portletContexts = new ArrayList<org.gatein.pc.api.PortletContext>(handles.size());
      for (String handle : handles)
      {
         portletContexts.add(org.gatein.pc.api.PortletContext.createPortletContext(handle));
      }

      try
      {
         RegistrationLocal.setRegistration(registration);
         List<DestroyCloneFailure> failuresList = producer.getPortletInvoker().destroyClones(portletContexts);
         int failuresNumber = failuresList.size();
         List<FailedPortlets> failedPortlets;
         if (failuresNumber > 0)
         {
            // for each reason of failure, record the associated portlet handles, expecting one portlet handle per message
            Multimap<String,String> reasonToHandles = HashMultimap.create(failuresNumber, 1);
            for (DestroyCloneFailure failure : failuresList)
            {
               reasonToHandles.put(failure.getMessage(), failure.getPortletId());
            }

            // create a FailedPortlets object for each reason
            failedPortlets = new ArrayList<FailedPortlets>(reasonToHandles.size());
            for (String reason : reasonToHandles.keys())
            {
               failedPortlets.add(WSRPTypeFactory.createFailedPortlets(reasonToHandles.get(reason), reason));
            }
         }
         else
         {
            failedPortlets = null;
         }

         return WSRPTypeFactory.createDestroyPortletsResponse(failedPortlets);
      }
      catch (PortletInvokerException e)
      {
         throw WSRPExceptionFactory.<OperationFailed, OperationFailedFault>throwWSException(WSRPExceptionFactory.OPERATION_FAILED,
            "Failed to destroy clones", e);
      }
      finally
      {
         RegistrationLocal.setRegistration(null);
      }
   }

   public PortletContext setPortletProperties(SetPortletProperties setPortletProperties) throws OperationFailed,
      InvalidHandle, MissingParameters, InconsistentParameters, InvalidUserCategory, AccessDenied, InvalidRegistration
   {
      WSRPExceptionFactory.throwOperationFailedIfValueIsMissing(setPortletProperties, "SetPortletProperties");

      PortletContext portletContext = setPortletProperties.getPortletContext();
      WSRPExceptionFactory.throwMissingParametersIfValueIsMissing(portletContext, "PortletContext", "SetPortletProperties");

      PropertyList propertyList = setPortletProperties.getPropertyList();
      WSRPExceptionFactory.throwMissingParametersIfValueIsMissing(propertyList, "PropertyList", "SetPortletProperties");

      Registration registration = producer.getRegistrationOrFailIfInvalid(setPortletProperties.getRegistrationContext());

      checkUserAuthorization(setPortletProperties.getUserContext());

      List<Property> properties = propertyList.getProperties();
      List<ResetProperty> resetProperties = propertyList.getResetProperties();
      int changesCount = 0;
      if (properties != null)
      {
         changesCount += properties.size();
      }
      if (resetProperties != null)
      {
         changesCount += resetProperties.size();
      }

      if (changesCount > 0)
      {
         List<PropertyChange> changes = new ArrayList<PropertyChange>(changesCount);

         if (properties != null)
         {
            for (Property property : properties)
            {
               String value = property.getStringValue();

               // todo: deal with XML values...
               // List<Object> values = property.getAny();
               // todo: deal with language?
               // String lang = property.getLang(); 

               changes.add(PropertyChange.newUpdate(property.getName().toString(), value));
            }
         }

         if (resetProperties != null)
         {
            for (ResetProperty resetProperty : resetProperties)
            {
               changes.add(PropertyChange.newReset(resetProperty.getName().toString()));
            }
         }

         try
         {
            RegistrationLocal.setRegistration(registration);
            org.gatein.pc.api.PortletContext resultContext =
               producer.getPortletInvoker().setProperties(WSRPUtils.convertToPortalPortletContext(portletContext),
                  changes.toArray(new PropertyChange[changes.size()]));
            return WSRPUtils.convertToWSRPPortletContext(resultContext);
         }
         catch (NoSuchPortletException e)
         {
            throw WSRPExceptionFactory.<InvalidHandle, InvalidHandleFault>throwWSException(WSRPExceptionFactory.INVALID_HANDLE,
               "Failed to set properties for portlet '" + portletContext.getPortletHandle() + "'", e);
         }
         catch (InvalidPortletIdException e)
         {
            throw WSRPExceptionFactory.<InconsistentParameters, InconsistentParametersFault>throwWSException(WSRPExceptionFactory.INCONSISTENT_PARAMETERS,
               "Failed to set properties for portlet '" + portletContext.getPortletHandle() + "'", e);
         }
         catch (PortletInvokerException e)
         {
            throw WSRPExceptionFactory.<OperationFailed, OperationFailedFault>throwWSException(WSRPExceptionFactory.OPERATION_FAILED,
               "Failed to set properties for portlet '" + portletContext.getPortletHandle() + "'", e);
         }
         finally
         {
            RegistrationLocal.setRegistration(null);
         }
      }

      return portletContext;
   }

   public PropertyList getPortletProperties(GetPortletProperties getPortletProperties) throws InvalidHandle,
      MissingParameters, InvalidRegistration, AccessDenied, OperationFailed, InconsistentParameters, InvalidUserCategory
   {
      WSRPExceptionFactory.throwOperationFailedIfValueIsMissing(getPortletProperties, GET_PORTLET_PROPERTIES);

      PortletContext portletContext = getPortletProperties.getPortletContext();
      WSRPExceptionFactory.throwMissingParametersIfValueIsMissing(portletContext, PORTLET_CONTEXT, GET_PORTLET_PROPERTIES);

      Registration registration = producer.getRegistrationOrFailIfInvalid(getPortletProperties.getRegistrationContext());

      UserContext userContext = getPortletProperties.getUserContext();
      checkUserAuthorization(userContext);

      List<String> names = getPortletProperties.getNames();
      Set<String> keys = new HashSet<String>(names);

      try
      {
         PropertyMap properties;
         org.gatein.pc.api.PortletContext jbpContext = WSRPUtils.convertToPortalPortletContext(portletContext);

         RegistrationLocal.setRegistration(registration);
         if (keys != null)
         {
            properties = producer.getPortletInvoker().getProperties(jbpContext, keys);
         }
         else
         {
            properties = producer.getPortletInvoker().getProperties(jbpContext);
         }

         //todo: we need to check that the user can actually modify the properties
         Portlet portlet = getPortletFrom(portletContext, registration);
         PortletInfo info = portlet.getInfo();

         PropertyList result = WSRPTypeFactory.createPropertyList();
         int propertyNb = properties.size();

         if (propertyNb > 0)
         {
            PreferenceInfo prefInfo;
            String key;
            List<String> values;
            LocalizedString displayName;

            for (Map.Entry<String, List<String>> entry : properties.entrySet())
            {
               key = entry.getKey();
               values = entry.getValue();
               prefInfo = info.getPreferences().getPreference(key);
               displayName = prefInfo.getDisplayName();
               String lang = WSRPUtils.toString(displayName.getDefaultLocale());

               // todo: support multi-valued properties
               if (values.size() != 1)
               {
                  throw new UnsupportedOperationException("Currently doesn't support multi-valued properties!");
               }
               result.getProperties().add(WSRPTypeFactory.createProperty(key, lang, values.get(0))); //todo: check what we should use key
            }
         }

         return result;
      }
      catch (PortletInvokerException e)
      {
         throw WSRPExceptionFactory.<InvalidHandle, InvalidHandleFault>throwWSException(WSRPExceptionFactory.INVALID_HANDLE,
            "Could not retrieve properties for portlet '" + portletContext + "'", e);
      }
      finally
      {
         RegistrationLocal.setRegistration(null);
      }
   }

   private void checkUserAuthorization(UserContext userContext)
   {
      //todo: implement
      if (userContext != null)
      {

      }
   }

   private Portlet getPortletFrom(PortletContext portletContext, Registration registration) throws InvalidHandle
   {
      Portlet portlet;
      try
      {
         RegistrationLocal.setRegistration(registration);
         portlet = producer.getPortletInvoker().getPortlet(WSRPUtils.convertToPortalPortletContext(portletContext));
         return portlet;
      }
      catch (PortletInvokerException e)
      {
         throw WSRPExceptionFactory.<InvalidHandle, InvalidHandleFault>throwWSException(WSRPExceptionFactory.INVALID_HANDLE,
            "Could not retrieve portlet '" + portletContext.getPortletHandle() + "'", e);
      }
      finally
      {
         RegistrationLocal.setRegistration(null);
      }
   }
}
