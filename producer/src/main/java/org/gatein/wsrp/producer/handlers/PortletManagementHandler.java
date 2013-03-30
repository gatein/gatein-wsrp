/*
 * JBoss, a division of Red Hat
 * Copyright 2011, Red Hat Middleware, LLC, and individual
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

package org.gatein.wsrp.producer.handlers;

import com.google.common.base.Function;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.gatein.common.i18n.LocalizedString;
import org.gatein.common.logging.Logger;
import org.gatein.common.logging.LoggerFactory;
import org.gatein.common.util.ParameterValidation;
import org.gatein.exports.data.ExportContext;
import org.gatein.exports.data.ExportPortletData;
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
import org.gatein.wsrp.producer.PortletManagementInterface;
import org.gatein.wsrp.producer.Utils;
import org.gatein.wsrp.producer.WSRPProducerImpl;
import org.gatein.wsrp.spec.v2.ErrorCodes;
import org.gatein.wsrp.spec.v2.WSRP2ExceptionFactory;
import org.oasis.wsrp.v2.AccessDenied;
import org.oasis.wsrp.v2.ClonePortlet;
import org.oasis.wsrp.v2.CopiedPortlet;
import org.oasis.wsrp.v2.CopyPortlets;
import org.oasis.wsrp.v2.CopyPortletsResponse;
import org.oasis.wsrp.v2.DestroyPortlets;
import org.oasis.wsrp.v2.DestroyPortletsResponse;
import org.oasis.wsrp.v2.ExportByValueNotSupported;
import org.oasis.wsrp.v2.ExportPortlets;
import org.oasis.wsrp.v2.ExportPortletsResponse;
import org.oasis.wsrp.v2.ExportedPortlet;
import org.oasis.wsrp.v2.Extension;
import org.oasis.wsrp.v2.FailedPortlets;
import org.oasis.wsrp.v2.GetPortletDescription;
import org.oasis.wsrp.v2.GetPortletProperties;
import org.oasis.wsrp.v2.GetPortletPropertyDescription;
import org.oasis.wsrp.v2.GetPortletsLifetime;
import org.oasis.wsrp.v2.GetPortletsLifetimeResponse;
import org.oasis.wsrp.v2.ImportPortlet;
import org.oasis.wsrp.v2.ImportPortlets;
import org.oasis.wsrp.v2.ImportPortletsFailed;
import org.oasis.wsrp.v2.ImportPortletsResponse;
import org.oasis.wsrp.v2.ImportedPortlet;
import org.oasis.wsrp.v2.InconsistentParameters;
import org.oasis.wsrp.v2.InvalidHandle;
import org.oasis.wsrp.v2.InvalidRegistration;
import org.oasis.wsrp.v2.InvalidUserCategory;
import org.oasis.wsrp.v2.Lifetime;
import org.oasis.wsrp.v2.MissingParameters;
import org.oasis.wsrp.v2.ModifyRegistrationRequired;
import org.oasis.wsrp.v2.OperationFailed;
import org.oasis.wsrp.v2.OperationNotSupported;
import org.oasis.wsrp.v2.PortletContext;
import org.oasis.wsrp.v2.PortletDescription;
import org.oasis.wsrp.v2.PortletDescriptionResponse;
import org.oasis.wsrp.v2.PortletPropertyDescriptionResponse;
import org.oasis.wsrp.v2.Property;
import org.oasis.wsrp.v2.PropertyDescription;
import org.oasis.wsrp.v2.PropertyList;
import org.oasis.wsrp.v2.RegistrationContext;
import org.oasis.wsrp.v2.ReleaseExport;
import org.oasis.wsrp.v2.ResetProperty;
import org.oasis.wsrp.v2.ResourceList;
import org.oasis.wsrp.v2.ResourceSuspended;
import org.oasis.wsrp.v2.SetExportLifetime;
import org.oasis.wsrp.v2.SetPortletProperties;
import org.oasis.wsrp.v2.SetPortletsLifetime;
import org.oasis.wsrp.v2.SetPortletsLifetimeResponse;
import org.oasis.wsrp.v2.UserContext;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 11147 $
 * @since 2.4
 */
public class PortletManagementHandler extends ServiceHandler implements PortletManagementInterface
{
   private static final String GET_PORTLET_PROPERTY_DESCRIPTION = "GetPortletPropertyDescription";
   private static final String GET_PORTLET_PROPERTIES = "GetPortletProperties";
   private static final String PORTLET_CONTEXT = "PortletContext";
   private static final String GET_PORTLET_DESCRIPTION = "GetPortletDescription";

   private static final Logger log = LoggerFactory.getLogger(PortletManagementHandler.class);

   public PortletManagementHandler(WSRPProducerImpl producer)
   {
      super(producer);
   }

   public PortletDescriptionResponse getPortletDescription(GetPortletDescription getPortletDescription)
      throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory,
      MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      WSRP2ExceptionFactory.throwOperationFailedIfValueIsMissing(getPortletDescription, GET_PORTLET_DESCRIPTION);
      Registration registration = producer.getRegistrationOrFailIfInvalid(getPortletDescription.getRegistrationContext());

      PortletContext portletContext = getPortletDescription.getPortletContext();
      WSRP2ExceptionFactory.throwMissingParametersIfValueIsMissing(portletContext, PORTLET_CONTEXT, GET_PORTLET_DESCRIPTION);

      UserContext userContext = getPortletDescription.getUserContext();
      checkUserAuthorization(userContext);

      // RegistrationLocal.setRegistration is called further down the invocation in ServiceDescriptionHandler.getPortletDescription 
      PortletDescription description = producer.getPortletDescription(portletContext, getPortletDescription.getDesiredLocales(), registration);
      return WSRPTypeFactory.createPortletDescriptionResponse(description);
   }

   public PortletPropertyDescriptionResponse getPortletPropertyDescription(GetPortletPropertyDescription getPortletPropertyDescription)
      throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory,
      MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      WSRP2ExceptionFactory.throwOperationFailedIfValueIsMissing(getPortletPropertyDescription, GET_PORTLET_PROPERTY_DESCRIPTION);

      PortletContext portletContext = getPortletPropertyDescription.getPortletContext();
      WSRP2ExceptionFactory.throwMissingParametersIfValueIsMissing(portletContext, PORTLET_CONTEXT, GET_PORTLET_PROPERTY_DESCRIPTION);

      try
      {
         Registration registration = producer.getRegistrationOrFailIfInvalid(getPortletPropertyDescription.getRegistrationContext());
         RegistrationLocal.setRegistration(registration);

         UserContext userContext = getPortletPropertyDescription.getUserContext();
         checkUserAuthorization(userContext);

         Portlet portlet = getPortletFrom(portletContext, registration);
         PortletInfo info = portlet.getInfo();
         PreferencesInfo prefsInfo = info.getPreferences();

         List<PropertyDescription> descs = Collections.emptyList();
         if (prefsInfo != null)
         {
            Set keySet = prefsInfo.getKeys();
            descs = new ArrayList<PropertyDescription>(keySet.size());
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
                  List<String> desiredLocales = getPortletPropertyDescription.getDesiredLocales();
                  PropertyDescription desc = WSRPTypeFactory.createPropertyDescription(prefInfo.getKey(), WSRPConstants.XSD_STRING);
                  desc.setLabel(Utils.convertToWSRPLocalizedString(prefInfo.getDisplayName(), desiredLocales));
                  desc.setHint(Utils.convertToWSRPLocalizedString(prefInfo.getDescription(), desiredLocales));
                  descs.add(desc);
               }
            }
         }

         return WSRPTypeFactory.createPortletPropertyDescriptionResponse(descs);
      }
      finally
      {
         RegistrationLocal.setRegistration(null);
      }
   }

   public PortletContext clonePortlet(ClonePortlet clonePortlet)
      throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory,
      MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      WSRP2ExceptionFactory.throwOperationFailedIfValueIsMissing(clonePortlet, "ClonePortlet");

      PortletContext portletContext = clonePortlet.getPortletContext();
      WSRP2ExceptionFactory.throwMissingParametersIfValueIsMissing(portletContext, "PortletContext", "ClonePortlet");

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
         throw WSRP2ExceptionFactory.throwWSException(InvalidHandle.class, "Failed to create clone for portlet '" + portletContext.getPortletHandle(), e);
      }
      catch (InvalidPortletIdException e)
      {
         throw WSRP2ExceptionFactory.throwWSException(InconsistentParameters.class, "Failed to create clone for portlet '" + portletContext.getPortletHandle(), e);
      }
      catch (PortletInvokerException e)
      {
         throw WSRP2ExceptionFactory.throwWSException(OperationFailed.class, "Failed to create clone for portlet '" + portletContext.getPortletHandle(), e);
      }
      finally
      {
         RegistrationLocal.setRegistration(null);
      }
   }

   public DestroyPortletsResponse destroyPortlets(DestroyPortlets destroyPortlets)
      throws InconsistentParameters, InvalidRegistration, MissingParameters, ModifyRegistrationRequired,
      OperationFailed, OperationNotSupported, ResourceSuspended
   {
      WSRP2ExceptionFactory.throwOperationFailedIfValueIsMissing(destroyPortlets, "DestroyPortlets");

      List<String> handles = destroyPortlets.getPortletHandles();
      WSRP2ExceptionFactory.throwMissingParametersIfValueIsMissing(handles, "portlet handles to be destroyed", "DestroyPortlets");

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
            Multimap<String, String> reasonToHandles = HashMultimap.create(failuresNumber, 1);
            for (DestroyCloneFailure failure : failuresList)
            {
               reasonToHandles.put(failure.getMessage(), failure.getPortletId());
            }

            // create a FailedPortlets object for each reason
            failedPortlets = new ArrayList<FailedPortlets>(reasonToHandles.size());
            for (String reason : reasonToHandles.keys())
            {
               failedPortlets.add(WSRPTypeFactory.createFailedPortlets(reasonToHandles.get(reason), ErrorCodes.Codes.OPERATIONFAILED, reason));
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
         throw WSRP2ExceptionFactory.throwWSException(OperationFailed.class, "Failed to destroy clones", e);
      }
      finally
      {
         RegistrationLocal.setRegistration(null);
      }
   }

   public GetPortletsLifetimeResponse getPortletsLifetime(GetPortletsLifetime getPortletsLifetime)
      throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, ModifyRegistrationRequired,
      OperationFailed, OperationNotSupported, ResourceSuspended
   {
      throw WSRP2ExceptionFactory.throwWSException(OperationNotSupported.class, "Lifetime operations are not currently supported.", null);
   }

   public SetPortletsLifetimeResponse setPortletsLifetime(SetPortletsLifetime setPortletsLifetime)
      throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, ModifyRegistrationRequired,
      OperationFailed, OperationNotSupported, ResourceSuspended
   {
      throw WSRP2ExceptionFactory.throwWSException(OperationNotSupported.class, "Lifetime operations are not currently supported.", null);
   }

   public CopyPortletsResponse copyPortlets(CopyPortlets copyPortlets)
      throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory,
      MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      WSRP2ExceptionFactory.throwOperationFailedIfValueIsMissing(copyPortlets, "copyPortlets");

      List<PortletContext> portletContexts = copyPortlets.getFromPortletContexts();

      if (!ParameterValidation.existsAndIsNotEmpty(portletContexts))
      {
         throw WSRP2ExceptionFactory.createWSException(MissingParameters.class, "Missing required portletContext in CopyPortlets.", null);
      }

      Registration fromRegistration = producer.getRegistrationOrFailIfInvalid(copyPortlets.getFromRegistrationContext());

      RegistrationContext toRegistationContext = copyPortlets.getToRegistrationContext();

      //if toRegistrationCotnext is null, then we use the fromRegistrationContext (from spec).
      //NOTE: this means we can't move between a PortletContext on a registered consumer to a non-registered consumer
      // between two non-registered consumers will still be ok.
      if (toRegistationContext == null)
      {
         toRegistationContext = copyPortlets.getFromRegistrationContext();
      }

      Registration toRegistration = producer.getRegistrationOrFailIfInvalid(toRegistationContext);

      UserContext fromUserContext = copyPortlets.getFromUserContext();
      checkUserAuthorization(fromUserContext);
      UserContext toUserContext = copyPortlets.getToUserContext();
      checkUserAuthorization(toUserContext);

      try
      {
         RegistrationLocal.setRegistration(fromRegistration);

         Map<String, FailedPortlets> failedPortletsMap = new HashMap<String, FailedPortlets>(portletContexts.size());

         List<CopiedPortlet> copiedPortlets = new ArrayList<CopiedPortlet>(portletContexts.size());

         for (PortletContext portletContext : portletContexts)
         {
            try
            {
               org.gatein.pc.api.PortletContext portalPC = WSRPUtils.convertToPortalPortletContext(portletContext);

               //NOTE: There are two ways we can do a copy. We can export using one registration and import using another. This seems the most straight forward way to do this, just seems a little overkill.
               // OR we can copy the portlet, then use the RegistrationManager and RegistrationPolicy to delete the PC from one registration and add it to another. But we don't actually 
               // create the copy under the toRegistration and we would need to add extra checks here to make sure the toRegistration has the proper permissions.
               // Note sure why there is even a copy portlet operation since it can be replicated by an export and then an import operation.

               org.gatein.pc.api.PortletContext exportedPortletContext = producer.getPortletInvoker().exportPortlet(PortletStateType.OPAQUE, portalPC);
               //Change the registration to the new registration and try and do an import. This should force the new import to be under the new registration context
               RegistrationLocal.setRegistration(toRegistration);
               org.gatein.pc.api.PortletContext copiedPortletContext = producer.getPortletInvoker().importPortlet(PortletStateType.OPAQUE, exportedPortletContext);

               PortletContext wsrpClonedPC = WSRPUtils.convertToWSRPPortletContext(copiedPortletContext);

               CopiedPortlet copiedPortlet = WSRPTypeFactory.createCopiedPortlet(wsrpClonedPC, portletContext.getPortletHandle());
               copiedPortlets.add(copiedPortlet);
            }
            catch (Exception e)
            {
               if (log.isWarnEnabled())
               {
                  log.warn("Error occured while trying to export a portlet.", e);
               }

               ErrorCodes.Codes errorCode;
               String reason;
               final String message = e.getLocalizedMessage();
               if (e instanceof NoSuchPortletException || e instanceof InvalidHandle
                  || (e instanceof IllegalArgumentException && message != null && message.contains(org.gatein.pc.api.PortletContext.INVALID_PORTLET_CONTEXT)))
               {
                  errorCode = ErrorCodes.Codes.INVALIDHANDLE;
                  reason = "The specified portlet handle is invalid";
               }
               else // default error message.
               {
                  errorCode = ErrorCodes.Codes.OPERATIONFAILED;
                  reason = "Error preparing portlet for export";
               }

               if (!failedPortletsMap.containsKey(errorCode.name()))
               {
                  List<String> portletHandles = new ArrayList<String>();
                  portletHandles.add(portletContext.getPortletHandle());

                  FailedPortlets failedPortlets = WSRPTypeFactory.createFailedPortlets(portletHandles, errorCode, reason);
                  failedPortletsMap.put(errorCode.name(), failedPortlets);
               }
               else
               {
                  FailedPortlets failedPortlets = failedPortletsMap.get(errorCode.name());
                  failedPortlets.getPortletHandles().add(portletContext.getPortletHandle());
               }
            }
         }

         List<FailedPortlets> failedPortlets = new ArrayList<FailedPortlets>(failedPortletsMap.values());
         //TODO: handle resources properly
         ResourceList resourceList = null;
         return WSRPTypeFactory.createCopyPortletsResponse(copiedPortlets, failedPortlets, resourceList);
      }
      catch (Exception e)
      {
         throw WSRP2ExceptionFactory.throwWSException(OperationFailed.class, "Operation Failed while trying to CopyPortlets.", e);
      }
      finally
      {
         RegistrationLocal.setRegistration(null);
      }
   }

   public PortletContext setPortletProperties(SetPortletProperties setPortletProperties)
      throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory,
      MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      WSRP2ExceptionFactory.throwOperationFailedIfValueIsMissing(setPortletProperties, "SetPortletProperties");

      PortletContext portletContext = setPortletProperties.getPortletContext();
      WSRP2ExceptionFactory.throwMissingParametersIfValueIsMissing(portletContext, "PortletContext", "SetPortletProperties");

      PropertyList propertyList = setPortletProperties.getPropertyList();
      WSRP2ExceptionFactory.throwMissingParametersIfValueIsMissing(propertyList, "PropertyList", "SetPortletProperties");

      Registration registration = producer.getRegistrationOrFailIfInvalid(setPortletProperties.getRegistrationContext());

      checkUserAuthorization(setPortletProperties.getUserContext());

      List<Property> properties = propertyList.getProperties();
      List<ResetProperty> resetProperties = propertyList.getResetProperties();
      int changesCount = 0;
      if (ParameterValidation.existsAndIsNotEmpty(properties))
      {
         changesCount += properties.size();

         // check that we don't set and reset the same property
         if (ParameterValidation.existsAndIsNotEmpty(resetProperties))
         {
            List<QName> names = new ArrayList<QName>(WSRPUtils.transform(properties, new Function<Property, QName>()
            {
               public QName apply(Property from)
               {
                  return from.getName();
               }
            }));
            names.retainAll(WSRPUtils.transform(resetProperties, new Function<ResetProperty, QName>()
            {
               public QName apply(ResetProperty from)
               {
                  return from.getName();
               }
            }));

            if (!names.isEmpty())
            {
               WSRP2ExceptionFactory.throwWSException(InconsistentParameters.class,
                  "Attempted to set and reset at the same time the following properties: " + names, null);
            }
         }
      }
      if (ParameterValidation.existsAndIsNotEmpty(resetProperties))
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
            throw WSRP2ExceptionFactory.throwWSException(InvalidHandle.class, "Failed to set properties for portlet '" + portletContext.getPortletHandle() + "'", e);
         }
         catch (InvalidPortletIdException e)
         {
            throw WSRP2ExceptionFactory.throwWSException(InconsistentParameters.class, "Failed to set properties for portlet '" + portletContext.getPortletHandle() + "'", e);
         }
         catch (PortletInvokerException e)
         {
            throw WSRP2ExceptionFactory.throwWSException(OperationFailed.class, "Failed to set properties for portlet '" + portletContext.getPortletHandle() + "'", e);
         }
         finally
         {
            RegistrationLocal.setRegistration(null);
         }
      }

      return portletContext;
   }

   public PropertyList getPortletProperties(GetPortletProperties getPortletProperties)
      throws AccessDenied, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory,
      MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      WSRP2ExceptionFactory.throwOperationFailedIfValueIsMissing(getPortletProperties, GET_PORTLET_PROPERTIES);

      PortletContext portletContext = getPortletProperties.getPortletContext();
      WSRP2ExceptionFactory.throwMissingParametersIfValueIsMissing(portletContext, PORTLET_CONTEXT, GET_PORTLET_PROPERTIES);

      Registration registration = producer.getRegistrationOrFailIfInvalid(getPortletProperties.getRegistrationContext());

      UserContext userContext = getPortletProperties.getUserContext();
      checkUserAuthorization(userContext);

      List<String> names = getPortletProperties.getNames();
      // workaround for GTNWSRP-290:
      if (names.size() == 1)
      {
         final String name = names.get(0);
         if (ParameterValidation.isNullOrEmpty(name))
         {
            names = Collections.emptyList();
         }
      }

      Set<String> keys = new HashSet<String>(names);

      try
      {
         PropertyMap properties;
         org.gatein.pc.api.PortletContext jbpContext = WSRPUtils.convertToPortalPortletContext(portletContext);

         RegistrationLocal.setRegistration(registration);
         if (!keys.isEmpty())
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
               String lang = WSRPConstants.DEFAULT_LOCALE;
               if (prefInfo != null)
               {
                  displayName = prefInfo.getDisplayName();
                  if (displayName != null)
                  {
                     lang = WSRPUtils.toString(displayName.getDefaultLocale());
                  }
               }

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
         throw WSRP2ExceptionFactory.throwWSException(InvalidHandle.class, "Could not retrieve properties for portlet '" + portletContext + "'", e);
      }
      finally
      {
         RegistrationLocal.setRegistration(null);
      }
   }

   public ExportPortletsResponse exportPortlets(ExportPortlets exportPortlets) throws AccessDenied,
      ExportByValueNotSupported, InconsistentParameters, InvalidHandle, InvalidRegistration, InvalidUserCategory,
      MissingParameters, ModifyRegistrationRequired, OperationFailed, OperationNotSupported, ResourceSuspended
   {
      WSRP2ExceptionFactory.throwMissingParametersIfValueIsMissing(exportPortlets, "ExportPortlets", "ExportPortlets");

      List<PortletContext> portletContexts = exportPortlets.getPortletContext();
      if (!ParameterValidation.existsAndIsNotEmpty(portletContexts))
      {
         throw WSRP2ExceptionFactory.createWSException(MissingParameters.class, "Missing required portletContext in ExportPortlets.", null);
      }

      Registration registration = producer.getRegistrationOrFailIfInvalid(exportPortlets.getRegistrationContext());

      UserContext userContext = exportPortlets.getUserContext();
      checkUserAuthorization(userContext);

      boolean exportByValueRequired;
      if (exportPortlets.isExportByValueRequired() != null)
      {
         exportByValueRequired = exportPortlets.isExportByValueRequired();
      }
      else
      {
         exportByValueRequired = false;
      }


      //check that the export manager can handle export by value
      if (exportByValueRequired && !producer.getExportManager().supportsExportByValue())
      {
         //TODO: instead of passing a string here, we should pass a resource so that its localized
         WSRP2ExceptionFactory.throwWSException(ExportByValueNotSupported.class, "The consumer is requesting portlets to be exported by value, but this consumer only supports export by reference.", null);
      }


      List<ExportedPortlet> exportedPortlets = new ArrayList<ExportedPortlet>(portletContexts.size());
      Map<String, FailedPortlets> failedPortletsMap = new HashMap<String, FailedPortlets>(portletContexts.size());

      try
      {
         RegistrationLocal.setRegistration(registration);

         ExportContext exportContext;
         if (exportPortlets.getLifetime() != null)
         {
            long currentTime = toLongDate(exportPortlets.getLifetime().getCurrentTime());
            long terminationTime = toLongDate(exportPortlets.getLifetime().getTerminationTime());
            long refreshDuration = exportPortlets.getLifetime().getRefreshDuration().getTimeInMillis(exportPortlets.getLifetime().getCurrentTime().toGregorianCalendar());
            exportContext = producer.getExportManager().createExportContext(exportByValueRequired, currentTime, terminationTime, refreshDuration);
         }
         else
         {
            exportContext = producer.getExportManager().createExportContext(exportByValueRequired, -1, -1, -1);
         }

         for (PortletContext portletContext : exportPortlets.getPortletContext())
         {
            try
            {
               byte[] exportData;

               String portletHandle = portletContext.getPortletHandle();
               byte[] portletState = portletContext.getPortletState();
               WSRP2ExceptionFactory.throwOperationFailedIfValueIsMissing(portletHandle, "Portlet handle");

               org.gatein.pc.api.PortletContext portalPC = WSRPUtils.convertToPortalPortletContext(portletContext);

               producer.getPortletInvoker().getPortlet(portalPC);

               org.gatein.pc.api.PortletContext exportedPortalPC = producer.getPortletInvoker().exportPortlet(PortletStateType.OPAQUE, portalPC);

               PortletContext exportedPortalContext = WSRPUtils.convertToWSRPPortletContext(exportedPortalPC);
               portletHandle = exportedPortalContext.getPortletHandle();
               portletState = exportedPortalContext.getPortletState();

               if (exportedPortalPC == null)
               {
                  WSRP2ExceptionFactory.throwWSException(InvalidHandle.class, "Could not find a portlet with handle " + portletHandle + " in the producer", null);
               }

               //get the exportPortletData
               ExportPortletData exportPortletData = producer.getExportManager().createExportPortletData(exportContext, portletHandle, portletState);

               //Create the exportedPortlet
               byte[] exportPortletBytes = producer.getExportManager().encodeExportPortletData(exportContext, exportPortletData);
               ExportedPortlet exportedPortlet = WSRPTypeFactory.createExportedPortlet(portletHandle, exportPortletData.encodeAsBytes());
               exportedPortlets.add(exportedPortlet);
            }

            catch (Exception e)
            {
               if (log.isWarnEnabled())
               {
                  log.warn("Error occured while trying to export a portlet.", e);
               }

               ErrorCodes.Codes errorCode;
               String reason;
               if (e instanceof NoSuchPortletException || e instanceof InvalidHandle)
               {
                  errorCode = ErrorCodes.Codes.INVALIDHANDLE;
                  reason = "The specified portlet handle is invalid";
               }
               else // default error message.
               {
                  errorCode = ErrorCodes.Codes.OPERATIONFAILED;
                  reason = "Error preparing portlet for export";
               }

               if (!failedPortletsMap.containsKey(errorCode.name()))
               {
                  List<String> portletHandles = new ArrayList<String>();
                  portletHandles.add(portletContext.getPortletHandle());

                  FailedPortlets failedPortlets = WSRPTypeFactory.createFailedPortlets(portletHandles, errorCode, reason);
                  failedPortletsMap.put(errorCode.name(), failedPortlets);
               }
               else
               {
                  FailedPortlets failedPortlets = failedPortletsMap.get(errorCode.name());
                  failedPortlets.getPortletHandles().add(portletContext.getPortletHandle());
               }
            }
         }

         //TODO: handle resourceLists better (should be using for things like errors)
         ResourceList resourceList = null;

         byte[] exportContextBytes = producer.getExportManager().encodeExportContextData(exportContext);

         Lifetime lifetime = null;

         if (exportContext.getCurrentTime() > 0)
         {
            lifetime = new Lifetime();
            lifetime.setCurrentTime(toXMLGregorianCalendar(exportContext.getCurrentTime()));
            lifetime.setTerminationTime(toXMLGregorianCalendar(exportContext.getTermintationTime()));
            lifetime.setRefreshDuration(toDuration(exportContext.getRefreshDuration()));
         }

         return WSRPTypeFactory.createExportPortletsResponse(exportContextBytes, exportedPortlets, new ArrayList<FailedPortlets>(failedPortletsMap.values()), lifetime, resourceList);
      }
      catch (Exception e)
      {
         throw WSRP2ExceptionFactory.throwWSException(OperationFailed.class, "Operation Failed while trying to ExportPortlets.", e);
      }
      finally
      {
         RegistrationLocal.setRegistration(null);
      }
   }

   public ImportPortletsResponse importPortlets(ImportPortlets importPortlets) throws OperationFailed, InvalidRegistration, MissingParameters, ModifyRegistrationRequired
   {
      WSRP2ExceptionFactory.throwOperationFailedIfValueIsMissing(importPortlets, "ImportPortlets");

      List<ImportPortlet> importPortletList = importPortlets.getImportPortlet();
      WSRP2ExceptionFactory.throwMissingParametersIfValueIsMissing(importPortletList, "ImportPortlet", "ImportPortlets");

      Registration registration = producer.getRegistrationOrFailIfInvalid(importPortlets.getRegistrationContext());

      // check if we have a valid userContext or not
      UserContext userContext = importPortlets.getUserContext();
      checkUserAuthorization(userContext);

      try
      {
         RegistrationLocal.setRegistration(registration);

         byte[] importContext = importPortlets.getImportContext();

         Lifetime lifeTime = importPortlets.getLifetime();

         List<ImportedPortlet> importedPortlets = new ArrayList<ImportedPortlet>();
         Map<String, ImportPortletsFailed> failedPortletsMap = new HashMap<String, ImportPortletsFailed>();

         ExportContext exportContext;
         try
         {
            exportContext = producer.getExportManager().createExportContext(importContext);
         }
         catch (Exception e)
         {
            throw WSRPExceptionFactory.createWSException(OperationFailed.class, "Invalid ImportContext.", e);
         }

         for (ImportPortlet importPortlet : importPortletList)
         {
            try
            {
               byte[] portletData = importPortlet.getExportData();

               ExportPortletData exportPortletData;
               if (lifeTime != null)
               {
                  long currentTime = toLongDate(lifeTime.getCurrentTime());
                  long terminationTime = toLongDate(lifeTime.getTerminationTime());
                  long refreshDuration = lifeTime.getRefreshDuration().getTimeInMillis(lifeTime.getCurrentTime().toGregorianCalendar());
                  exportPortletData = producer.getExportManager().createExportPortletData(exportContext, currentTime, terminationTime, refreshDuration, portletData);
               }
               else
               {
                  exportPortletData = producer.getExportManager().createExportPortletData(exportContext, -1, -1, -1, portletData);
               }

               String portletHandle = exportPortletData.getPortletHandle();
               byte[] portletState = exportPortletData.getPortletState();

               PortletContext pc = WSRPTypeFactory.createPortletContext(portletHandle, portletState);
               org.gatein.pc.api.PortletContext pcPortletContext = WSRPUtils.convertToPortalPortletContext(pc);

               org.gatein.pc.api.PortletContext cpc = producer.getPortletInvoker().importPortlet(PortletStateType.OPAQUE, pcPortletContext);
               PortletContext wpc = WSRPUtils.convertToWSRPPortletContext(cpc);

               ImportedPortlet importedPortlet = WSRPTypeFactory.createImportedPortlet(importPortlet.getImportID(), wpc);

               importedPortlets.add(importedPortlet);
            }
            catch (Exception e)
            {
               if (log.isWarnEnabled())
               {
                  log.warn("Error occured while trying to import a portlet.", e);
               }

               ErrorCodes.Codes errorCode;
               String reason;
               if (e instanceof NoSuchPortletException || e instanceof InvalidHandle)
               {
                  errorCode = ErrorCodes.Codes.INVALIDHANDLE;
                  reason = "The specified portlet handle is invalid";
               }
               else if (e instanceof OperationFailed)
               {
                  errorCode = ErrorCodes.Codes.OPERATIONFAILED;
                  reason = e.getMessage();
               }
               else if (e instanceof PortletInvokerException || e instanceof UnsupportedOperationException || e instanceof IllegalArgumentException)
               {
                  errorCode = ErrorCodes.Codes.OPERATIONFAILED;
                  reason = "Error trying to create imported portlet.";
               }
               else // default error message.
               {
                  errorCode = ErrorCodes.Codes.OPERATIONFAILED;
                  reason = "Error importing portlet.";
               }

               if (!failedPortletsMap.containsKey(errorCode.name()))
               {
                  List<String> portleIDs = new ArrayList<String>();
                  portleIDs.add(importPortlet.getImportID());

                  ImportPortletsFailed failedPortlets = WSRPTypeFactory.createImportPortletsFailed(portleIDs, errorCode, reason);
                  failedPortletsMap.put(errorCode.name(), failedPortlets);
               }
               else
               {
                  ImportPortletsFailed failedPortlets = failedPortletsMap.get(errorCode.name());
                  failedPortlets.getImportID().add(importPortlet.getImportID());
               }
            }
         }

         ResourceList resourceList = null; //TODO: figure out what exactly should be stored in the resource list here

         return WSRPTypeFactory.createImportPortletsResponse(importedPortlets, new ArrayList<ImportPortletsFailed>(failedPortletsMap.values()), resourceList);
      }
      finally
      {
         RegistrationLocal.setRegistration(null);
      }
   }

   public List<Extension> releaseExport(ReleaseExport releaseExport)
   {
      try
      {
         if (releaseExport != null && releaseExport.getExportContext() != null)
         {
            producer.getExportManager().releaseExport(releaseExport.getExportContext());
         }
      }
      catch (Exception e)
      {
         if (log.isWarnEnabled())
         {
            log.warn("Error occured while trying to perform a ReleaseExport", e);
         }
      }

      //this method shouldn't return anything
      return WSRPTypeFactory.createReturnAny().getExtensions();
   }

   public Lifetime setExportLifetime(SetExportLifetime setExportLifetime) throws OperationFailed, InvalidRegistration, OperationNotSupported, ModifyRegistrationRequired
   {
      //this method is only valid if the producer can handle exporting by reference.
      if (producer.getExportManager().getPersistenceManager() == null)
      {
         WSRP2ExceptionFactory.throwWSException(OperationNotSupported.class, "This producer does not support export by reference.", null);
      }

      WSRP2ExceptionFactory.throwOperationFailedIfValueIsMissing(setExportLifetime, "setExportLifetimePortlets");

      byte[] exportContextBytes = setExportLifetime.getExportContext();
      //NOTE: we can't throw a MissingParameterException since its not allowed as part of the spec
      if (exportContextBytes == null)
      {
         WSRPExceptionFactory.throwWSException(OperationFailed.class, "Cannot call setExportLifetime with an empty ExportContext.", null);
      }

      Registration registration = producer.getRegistrationOrFailIfInvalid(setExportLifetime.getRegistrationContext());

      // check if we have a valid userContext or not
      UserContext userContext = setExportLifetime.getUserContext();
      checkUserAuthorization(userContext);

      try
      {
         RegistrationLocal.setRegistration(registration);

         ExportContext exportContext;
         long currentTime = toLongDate(setExportLifetime.getLifetime().getCurrentTime());
         long terminationTime = toLongDate(setExportLifetime.getLifetime().getTerminationTime());
         long refreshDuration = setExportLifetime.getLifetime().getRefreshDuration().getTimeInMillis(setExportLifetime.getLifetime().getCurrentTime().toGregorianCalendar());
         exportContext = producer.getExportManager().setExportLifetime(exportContextBytes, currentTime, terminationTime, refreshDuration);

         return getLifetime(exportContext);
      }
      catch (Exception e)
      {
         throw WSRPExceptionFactory.createWSException(OperationFailed.class, "Operation Failed while trying to setExportLifetime.", e);
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
         throw WSRP2ExceptionFactory.throwWSException(InvalidHandle.class, "Could not retrieve portlet '" + portletContext.getPortletHandle() + "'", e);
      }
      finally
      {
         RegistrationLocal.setRegistration(null);
      }
   }

   //TODO: move these classes to the common module and write up proper lifetime utilities 
   private XMLGregorianCalendar toXMLGregorianCalendar(long time) throws DatatypeConfigurationException
   {
      Date date = new Date(time);
      GregorianCalendar gregorianCalendar = new GregorianCalendar();
      gregorianCalendar.setTime(date);
      return DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
   }

   private long toLongDate(XMLGregorianCalendar calendar)
   {
      return calendar.toGregorianCalendar().getTime().getTime();
   }

   private Duration toDuration(long duration) throws DatatypeConfigurationException
   {
      return DatatypeFactory.newInstance().newDuration(duration);
   }

   private Lifetime getLifetime(ExportContext exportContext) throws DatatypeConfigurationException
   {
      if (exportContext.getCurrentTime() >= 0)
      {
         Lifetime lifetime = new Lifetime();

         XMLGregorianCalendar currentTime = toXMLGregorianCalendar(exportContext.getCurrentTime());
         XMLGregorianCalendar terminationTime = toXMLGregorianCalendar(exportContext.getTermintationTime());

         Duration duration = toDuration(exportContext.getRefreshDuration());

         lifetime.setCurrentTime(currentTime);
         lifetime.setTerminationTime(terminationTime);
         lifetime.setRefreshDuration(duration);

         return lifetime;
      }
      else
      {
         return null;
      }
   }
}
