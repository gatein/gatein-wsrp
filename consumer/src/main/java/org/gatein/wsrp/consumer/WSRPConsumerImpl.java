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

package org.gatein.wsrp.consumer;

import org.gatein.common.NotYetImplemented;
import org.gatein.common.util.ParameterValidation;
import org.gatein.common.util.Version;
import org.gatein.pc.api.InvalidPortletIdException;
import org.gatein.pc.api.InvokerUnavailableException;
import org.gatein.pc.api.NoSuchPortletException;
import org.gatein.pc.api.Portlet;
import org.gatein.pc.api.PortletContext;
import org.gatein.pc.api.PortletInvokerException;
import org.gatein.pc.api.PortletStateType;
import org.gatein.pc.api.invocation.PortletInvocation;
import org.gatein.pc.api.invocation.response.PortletInvocationResponse;
import org.gatein.pc.api.spi.UserContext;
import org.gatein.pc.api.state.DestroyCloneFailure;
import org.gatein.pc.api.state.PropertyChange;
import org.gatein.pc.api.state.PropertyMap;
import org.gatein.pc.federation.impl.FederatingPortletInvokerService;
import org.gatein.pc.portlet.impl.spi.AbstractPortletInvocationContext;
import org.gatein.pc.portlet.state.SimplePropertyMap;
import org.gatein.wsrp.UserContextConverter;
import org.gatein.wsrp.WSRPConstants;
import org.gatein.wsrp.WSRPConsumer;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.WSRPUtils;
import org.gatein.wsrp.api.session.SessionEvent;
import org.gatein.wsrp.consumer.handlers.InvocationDispatcher;
import org.gatein.wsrp.consumer.handlers.InvocationHandler;
import org.gatein.wsrp.consumer.handlers.ProducerSessionInformation;
import org.gatein.wsrp.consumer.handlers.SessionHandler;
import org.gatein.wsrp.consumer.migration.ExportInfo;
import org.gatein.wsrp.consumer.migration.ImportInfo;
import org.gatein.wsrp.consumer.migration.InMemoryMigrationService;
import org.gatein.wsrp.consumer.migration.MigrationService;
import org.gatein.wsrp.consumer.portlet.WSRPPortlet;
import org.gatein.wsrp.consumer.portlet.info.WSRPPortletInfo;
import org.gatein.wsrp.consumer.spi.WSRPConsumerSPI;
import org.gatein.wsrp.services.MarkupService;
import org.gatein.wsrp.services.PortletManagementService;
import org.gatein.wsrp.services.RegistrationService;
import org.gatein.wsrp.services.ServiceDescriptionService;
import org.gatein.wsrp.servlet.UserAccess;
import org.oasis.wsrp.v2.ExportedPortlet;
import org.oasis.wsrp.v2.Extension;
import org.oasis.wsrp.v2.FailedPortlets;
import org.oasis.wsrp.v2.ImportPortlet;
import org.oasis.wsrp.v2.ImportPortletsFailed;
import org.oasis.wsrp.v2.ImportedPortlet;
import org.oasis.wsrp.v2.InconsistentParameters;
import org.oasis.wsrp.v2.InvalidHandle;
import org.oasis.wsrp.v2.Lifetime;
import org.oasis.wsrp.v2.OperationNotSupported;
import org.oasis.wsrp.v2.Property;
import org.oasis.wsrp.v2.PropertyList;
import org.oasis.wsrp.v2.RegistrationContext;
import org.oasis.wsrp.v2.RegistrationData;
import org.oasis.wsrp.v2.ResetProperty;
import org.oasis.wsrp.v2.ResourceList;
import org.oasis.wsrp.v2.RuntimeContext;
import org.oasis.wsrp.v2.SessionParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.namespace.QName;
import javax.xml.ws.Holder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author <a href="mailto:boleslaw.dawidowicz@jboss.org">Boleslaw Dawidowicz</a>
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 11692 $
 * @since 2.4
 */
public class WSRPConsumerImpl implements WSRPConsumerSPI
{
   private final SessionHandler sessionHandler;

   private final InvocationDispatcher dispatcher;

   private ProducerInfo producerInfo;

   private transient MigrationService migrationService;

   /** A registration data element used to indicate when no registration was required by the producer */
   private final static RegistrationData REGISTRATION_NOT_NEEDED = WSRPTypeFactory.createDefaultRegistrationData();

   private final static Logger log = LoggerFactory.getLogger(WSRPConsumer.class);

   private final static String PORTLET_INFO_KEY = "wsrp_portlet_info";

   static
   {
      REGISTRATION_NOT_NEEDED.setConsumerAgent("INVALID AGENT");
      REGISTRATION_NOT_NEEDED.setConsumerName("INVALID NAME");
   }

   /** The default user scopes as per the specification (6.1.4) */
   private static final Set<String> WSRP_DEFAULT_USER_SCOPE = new HashSet<String>(2);

   static
   {
      WSRP_DEFAULT_USER_SCOPE.add(WSRPConstants.CACHE_FOR_ALL);
      WSRP_DEFAULT_USER_SCOPE.add(WSRPConstants.CACHE_PER_USER);
   }

   /** The set of supported user scopes */
   private Set supportedUserScopes = WSRP_DEFAULT_USER_SCOPE; // todo: make it possible to support different user scopes
   private transient boolean started;

   public WSRPConsumerImpl()
   {
      this(new ProducerInfo(), new InMemoryMigrationService());
   }

   public WSRPConsumerImpl(ProducerInfo info, MigrationService migrationService)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(info, "ProducerInfo");

      producerInfo = info;
      sessionHandler = new SessionHandler(this);
      dispatcher = new InvocationDispatcher(this);

      this.migrationService = migrationService;
   }

   public ProducerInfo getProducerInfo()
   {
      return producerInfo;
   }

   // PortletInvoker implementation ************************************************************************************

   public Set<Portlet> getPortlets() throws InvokerUnavailableException
   {
      try
      {
         Map portletMap = producerInfo.getPortletMap();
         return new LinkedHashSet<Portlet>(portletMap.values());
      }
      catch (Exception e)
      {
         throw new InvokerUnavailableException(e.getMessage(), e.getCause());
      }
   }

   public Portlet getPortlet(PortletContext portletContext) throws IllegalArgumentException, PortletInvokerException
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(portletContext, "PortletContext");

      Portlet portlet = producerInfo.getPortlet(portletContext);

      if (portlet == null)
      {
         throw new NoSuchPortletException(portletContext.getId());
      }
      else
      {
         return portlet;
      }
   }

   public PortletInvocationResponse invoke(PortletInvocation invocation) throws PortletInvokerException
   {
      final PortletInvocationResponse response = dispatcher.dispatchAndHandle(invocation);
      if (response instanceof InvocationHandler.WSErrorResponse)
      {
         return invoke(invocation);
      }
      return response;
   }

   public PortletContext createClone(PortletStateType stateType, PortletContext portletContext) throws IllegalArgumentException, PortletInvokerException, UnsupportedOperationException
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(portletContext, "PortletContext");

      if (!PortletStateType.OPAQUE.equals(stateType))
      {
         throw new IllegalArgumentException("This PortletInvoker cannot deal with PortletStateTypes other than PortletStateType.OPAQUE. Given: " + stateType);
      }

      WSRPPortlet original = getWSRPPortlet(portletContext);
      if (original == null)
      {
         throw new PortletInvokerException("No portlet '" + portletContext.getId() + "' to clone!");
      }

      try
      {
         Holder<String> handle = new Holder<String>();
         Holder<byte[]> portletState = new Holder<byte[]>();
         getPortletManagementService().clonePortlet(getRegistrationContext(),
            WSRPUtils.convertToWSRPPortletContext(portletContext), UserAccess.getUserContext(), null, handle,
            portletState, new Holder<Lifetime>(), new Holder<List<Extension>>()
         );
         return WSRPUtils.convertToPortalPortletContext(handle.value, portletState.value);
      }
      catch (Exception e)
      {
         if (producerInfo.canAttemptRecoveryFrom(e))
         {
            return createClone(stateType, portletContext);
         }
         else
         {
            throw new PortletInvokerException("Couldn't clone portlet '" + portletContext.getId() + "'", e);
         }
      }
   }

   public List<DestroyCloneFailure> destroyClones(List<PortletContext> portletContexts) throws IllegalArgumentException, PortletInvokerException, UnsupportedOperationException
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(portletContexts, "Portlet identifiers");

      int numberOfClones = portletContexts.size();
      if (numberOfClones == 0)
      {
         return Collections.emptyList();
      }

      List<String> handles = new ArrayList<String>(numberOfClones);
      for (Object portletContext : portletContexts)
      {
         PortletContext context = (PortletContext)portletContext;
         String id = context.getId();
         handles.add(id);
      }
      if (log.isDebugEnabled())
      {
         log.debug("Attempting to destroy clones: " + handles);
      }

      try
      {
         Holder<List<FailedPortlets>> failedPortlets = new Holder<List<FailedPortlets>>();

         getPortletManagementService().destroyPortlets(getRegistrationContext(), handles, UserAccess.getUserContext(),
            failedPortlets, new Holder<List<Extension>>());

         List<FailedPortlets> failures = failedPortlets.value;
         List<DestroyCloneFailure> result = Collections.emptyList();
         if (failures != null)
         {
            result = new ArrayList<DestroyCloneFailure>(failures.size());
            // list all the failures and successes
            for (FailedPortlets failure : failures)
            {
               List<String> portletHandles = failure.getPortletHandles();
               String reason = failure.getReason().getValue();
               for (String portletHandle : portletHandles)
               {
                  result.add(new DestroyCloneFailure(portletHandle, reason));
                  if (log.isDebugEnabled())
                  {
                     log.debug("Couldn't destroy clone '" + portletHandles + "'");
                  }
               }
               handles.removeAll(portletHandles);
            }
         }

         // update ProducerInfo's caches by removing all the successfully destroyed clones
         if (!handles.isEmpty())
         {
            for (String handle : handles)
            {
               producerInfo.removeHandleFromCaches(handle);
            }
         }

         return result;
      }
      catch (Exception e)
      {
         if (producerInfo.canAttemptRecoveryFrom(e))
         {
            return destroyClones(portletContexts);
         }
         else
         {
            throw new PortletInvokerException("Couldn't destroy clones.", e);
         }
      }
   }

   public PropertyMap getProperties(PortletContext portletContext, Set<String> keys) throws IllegalArgumentException, PortletInvokerException, UnsupportedOperationException
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(keys, "Portlet ids");

      return getProperties(portletContext, new ArrayList<String>(keys));
   }

   private PropertyMap getProperties(PortletContext portletContext, List<String> keys) throws PortletInvokerException
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(portletContext, "PortletContext");

      try
      {
         Holder<List<Property>> properties = new Holder<List<Property>>();
         Holder<List<ResetProperty>> resetProperties = new Holder<List<ResetProperty>>();
         getPortletManagementService().getPortletProperties(
            getRegistrationContext(),
            WSRPUtils.convertToWSRPPortletContext(portletContext),
            UserAccess.getUserContext(),
            keys,
            properties,
            resetProperties,
            new Holder<List<Extension>>()
         );

         List<Property> props = properties.value;
         if (props != null)
         {
            PropertyMap result = new SimplePropertyMap(props.size());

            for (Property prop : props)
            {
               String name = prop.getName().toString();
               String value = prop.getStringValue();
               List<String> list = new ArrayList<String>();
               list.add(value);
               result.put(name, list); // todo: is that all?!?
            }

            return result;
         }
         else
         {
            return new SimplePropertyMap();
         }
      }
      catch (OperationNotSupported operationNotSupported)
      {
         throw new UnsupportedOperationException(operationNotSupported);
      }
      catch (InvalidHandle invalidHandle)
      {
         throw new InvalidPortletIdException(invalidHandle, portletContext.getId());
      }
      catch (InconsistentParameters inconsistentParameters)
      {
         throw new IllegalArgumentException(inconsistentParameters);
      }
      /*
      // GTNWSRP-62
      catch (InvalidRegistration invalidRegistration)
      {
      }
      catch (MissingParameters missingParameters)
      {
      }
      catch (ResourceSuspended resourceSuspended)
      {
      }
      catch (OperationFailed operationFailed)
      {
      }
      catch (AccessDenied accessDenied)
      {
      }
      catch (InvalidUserCategory invalidUserCategory)
      {
      }
      catch (ModifyRegistrationRequired modifyRegistrationRequired)
      {
      }*/
      catch (Exception e)
      {
         if (producerInfo.canAttemptRecoveryFrom(e))
         {
            return getProperties(portletContext, keys);
         }
         else
         {
            throw new PortletInvokerException(e);
         }
      }
   }

   public PropertyMap getProperties(PortletContext portletContext) throws IllegalArgumentException, PortletInvokerException, UnsupportedOperationException
   {
      return getProperties(portletContext, Collections.<String>emptyList());
   }

   public PortletContext setProperties(PortletContext portletContext, PropertyChange[] changes) throws IllegalArgumentException,
      PortletInvokerException, UnsupportedOperationException
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(portletContext, "PortletContext");
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(changes, "Property changes");

      WSRPPortlet portlet = getWSRPPortlet(portletContext);
      if (portlet == null)
      {
         throw new PortletInvokerException("Cannot set properties on portlet '" + portletContext.getId()
            + "' because there is no such portlet.");
      }

      PropertyList propertyList = WSRPTypeFactory.createPropertyList();
      int changesNumber = changes.length;
      List<Property> updates = new ArrayList<Property>(changesNumber);
      List<ResetProperty> resets = new ArrayList<ResetProperty>(changesNumber);
      for (int i = 0; i < changesNumber; i++)
      {
         PropertyChange change = changes[i];
         switch (change.getType())
         {
            case PropertyChange.PREF_RESET:
               resets.add(WSRPTypeFactory.createResetProperty(change.getKey()));
               break;

            case PropertyChange.PREF_UPDATE:
               // todo: deal with language more appropriately
               updates.add(WSRPTypeFactory.createProperty(change.getKey(),
                  WSRPUtils.toString(Locale.getDefault()), change.getValue().get(0)));
               break;

            default:
               throw new IllegalArgumentException("Unexpected property change type: " + change.getType());
         }
      }
      propertyList.getProperties().addAll(updates);
      propertyList.getResetProperties().addAll(resets);

      try
      {
         Holder<String> handle = new Holder<String>();
         Holder<byte[]> portletState = new Holder<byte[]>();
         getPortletManagementService().setPortletProperties(getRegistrationContext(),
            WSRPUtils.convertToWSRPPortletContext(portletContext),
            UserAccess.getUserContext(),
            propertyList,
            handle,
            portletState,
            new Holder<Lifetime>(),
            new Holder<List<Extension>>()
         );
         PortletContext newPortletContext = PortletContext.createPortletContext(handle.value, portletState.value);
         portlet.setPortletContext(newPortletContext);
         return newPortletContext;
      }
      catch (Exception e)
      {
         if (producerInfo.canAttemptRecoveryFrom(e))
         {
            return setProperties(portletContext, changes);
         }
         else
         {
            throw new PortletInvokerException("Unable to set properties for portlet '" + portletContext.getId() + "'", e);
         }
      }
   }

   public PortletContext exportPortlet(PortletStateType stateType, PortletContext originalPortletContext)
      throws PortletInvokerException
   {
      throw new NotYetImplemented();
   }

   public PortletContext importPortlet(PortletStateType stateType, PortletContext originalPortletContext)
      throws PortletInvokerException
   {
      throw new NotYetImplemented();
   }

   // Accessors ********************************************************************************************************

   public String getProducerId()
   {
      return producerInfo.getId();
   }

   public SessionHandler getSessionHandler()
   {
      return sessionHandler;
   }

   // Portlet-related methods ******************************************************************************************

   public Map<String, Set<Portlet>> getPortletGroupMap() throws PortletInvokerException
   {
      return producerInfo.getPortletGroupMap();
   }

   /**
    * @param invocation
    * @return
    * @since 2.6
    */
   public static PortletContext getPortletContext(PortletInvocation invocation)
   {
      return invocation.getTarget();
   }

   public WSRPPortletInfo getPortletInfo(PortletInvocation invocation) throws PortletInvokerException
   {
      // first try to get the info from the invocation
      Object info = invocation.getAttribute(PORTLET_INFO_KEY);

      // if the portlet info is not in the invocation, set it so that it can be used in further calls
      if (info == null)
      {
         info = getWSRPPortlet(getPortletContext(invocation)).getInfo();
         invocation.setAttribute(PORTLET_INFO_KEY, info);
      }

      return (WSRPPortletInfo)info;
   }

   WSRPPortlet getWSRPPortlet(PortletContext portletContext) throws PortletInvokerException
   {
      return (WSRPPortlet)getPortlet(portletContext);
   }

   public Set getSupportedUserScopes()
   {
      return Collections.unmodifiableSet(supportedUserScopes);
   }

   public boolean supportsUserScope(String userScope)
   {
      return supportedUserScopes.contains(userScope);
   }

   public boolean isSupportsExport()
   {
      return isUsingWSRP2(); // todo: fix-me, using WSRP 2 doesn't necessarily equals supporting export...
   }

   // Registration *****************************************************************************************************

   public void handleInvalidRegistrationFault() throws PortletInvokerException
   {
      // reset registration data and try again
      producerInfo.resetRegistration();
      refreshProducerInfo(true);
   }

   public RegistrationContext getRegistrationContext() throws PortletInvokerException
   {
      return producerInfo.getRegistrationContext();
   }

   // Session information access ***************************************************************************************

   public ProducerSessionInformation getProducerSessionInformationFrom(PortletInvocation invocation)
   {
      return sessionHandler.getProducerSessionInformation(invocation);
   }

   public ProducerSessionInformation getProducerSessionInformationFrom(HttpSession session)
   {
      return sessionHandler.getProducerSessionInformation(session);
   }


   public void activate() throws Exception
   {
      internalStart();
      producerInfo.setActiveAndSave(true);
      log.info("Consumer with id '" + getProducerId() + "' activated");
   }

   private void internalStart() throws Exception
   {
      if (!started)
      {
         try
         {
            start();
         }
         catch (Exception e)
         {
            // mark the consumer as inactive if it cannot be started
            producerInfo.setActiveAndSave(false);
            throw e;
         }
      }
   }

   public void deactivate() throws Exception
   {
      producerInfo.setActiveAndSave(false);
      if (started)
      {
         stop();
         log.info("Consumer with id '" + getProducerId() + "' deactivated");
      }
   }

   public boolean isActive()
   {
      return producerInfo.isActive() && started;
   }

   public boolean isRefreshNeeded()
   {
      return !started || producerInfo.isRefreshNeeded(false);
   }

   public RefreshResult refresh(boolean forceRefresh) throws PortletInvokerException
   {
      try
      {
         internalStart();
      }
      catch (Exception e)
      {
         throw new PortletInvokerException(e);
      }

      return refreshProducerInfo(forceRefresh);
   }

   // Service implementation *******************************************************************************************

   public void start() throws Exception
   {
      getEndpointConfigurationInfo().start();
      started = true;
      log.info("Consumer with id '" + getProducerId() + "' started");
   }

   public void stop() throws Exception
   {
      getEndpointConfigurationInfo().stop();
      started = false;
      log.info("Consumer with id '" + getProducerId() + "' stopped");
   }

   // Web services access **********************************************************************************************

   private EndpointConfigurationInfo getEndpointConfigurationInfo()
   {
      return producerInfo.getEndpointConfigurationInfo();
   }

   private ServiceDescriptionService getServiceDescriptionService() throws PortletInvokerException
   {
      refreshProducerInfo(false);
      return getEndpointConfigurationInfo().getServiceDescriptionService();
   }

   public MarkupService getMarkupService() throws PortletInvokerException
   {
      refreshProducerInfo(false);
      return getEndpointConfigurationInfo().getMarkupService();
   }

   private PortletManagementService getPortletManagementService() throws PortletInvokerException
   {
      refreshProducerInfo(false);
      return getEndpointConfigurationInfo().getPortletManagementService();
   }

   private RegistrationService getRegistrationService() throws PortletInvokerException
   {
      refreshProducerInfo(false);
      return getEndpointConfigurationInfo().getRegistrationService();
   }

   public void refreshProducerInfo() throws PortletInvokerException
   {
      refreshProducerInfo(true);
   }

   private RefreshResult refreshProducerInfo(boolean forceRefresh) throws PortletInvokerException
   {
      return producerInfo.detailedRefresh(forceRefresh);
   }

   public void releaseSessions() throws PortletInvokerException
   {
      sessionHandler.releaseSessions();
   }

// Support methods **************************************************************************************************

   private String getUserContextKeyFor(UserContext userContext)
   {
      // fix-me: probably need to have an Id attribute on userId context.
      String userId = userContext.getId();
      if (userId != null)
      {
         return userId;
      }
      else
      {
         return null;
      }
   }

   // fix-me!

   public org.oasis.wsrp.v2.UserContext getUserContextFrom(WSRPPortletInfo info, PortletInvocation invocation, RuntimeContext runtimeContext) throws PortletInvokerException
   {
      // first decide if we need to pass the user context...
      SessionParams sessionParams = runtimeContext.getSessionParams();
      if (info != null && info.isUserContextStoredInSession() && sessionParams != null && sessionParams.getSessionID() != null)
      {
         return null; // the user context is most likely in the session already
      }

      // todo: deal with user categories and user context key properly
      UserContext userContext = invocation.getUserContext();
      if (userContext != null)
      {
         String userContextKey = getUserContextKeyFor(userContext);
         if (userContextKey == null)
         {
            return null;
         }

         return UserContextConverter.createWSRPUserContextFrom(userContext, userContextKey, null);
      }
      return null;
   }

   public void setTemplatesIfNeeded(WSRPPortletInfo info, PortletInvocation invocation, RuntimeContext runtimeContext) throws PortletInvokerException
   {
      // todo: could store templates in producer session info to avoid to re-generate them all the time?
      SessionParams sessionParams = runtimeContext.getSessionParams();
      if (info != null && info.isDoesUrlTemplateProcessing()
         && (!info.isTemplatesStoredInSession() || sessionParams == null || sessionParams.getSessionID() == null))
      {
         // we need to supply the templates since the portlet does URL processing and either doesn't store
         // templates in the session or no session has been established yet
         runtimeContext.setTemplates(WSRPTypeFactory.createTemplates(invocation.getContext()));
      }
   }

   public static HttpServletRequest getHttpRequest(PortletInvocation invocation)
   {
      AbstractPortletInvocationContext invocationContext = (AbstractPortletInvocationContext)invocation.getContext();
      return invocationContext.getClientRequest();
   }

   public static HttpSession getHttpSession(PortletInvocation invocation)
   {
      return getHttpRequest(invocation).getSession();
   }

   public void onSessionEvent(SessionEvent event)
   {
      sessionHandler.onSessionEvent(event);
   }

   @Override
   public boolean equals(Object o)
   {
      if (this == o)
      {
         return true;
      }
      if (o == null || getClass() != o.getClass())
      {
         return false;
      }

      WSRPConsumerImpl that = (WSRPConsumerImpl)o;

      return producerInfo.equals(that.producerInfo);

   }

   @Override
   public int hashCode()
   {
      return producerInfo.hashCode();
   }

   public ExportInfo exportPortlets(List<String> portletHandles) throws PortletInvokerException
   {
      if (ParameterValidation.existsAndIsNotEmpty(portletHandles))
      {

         List<org.oasis.wsrp.v2.PortletContext> portletContexts = new ArrayList<org.oasis.wsrp.v2.PortletContext>(portletHandles.size());
         for (String handle : portletHandles)
         {
            portletContexts.add(WSRPTypeFactory.createPortletContext(handle));
         }

         try
         {
            Holder<byte[]> exportContextHolder = new Holder<byte[]>();
            Holder<List<ExportedPortlet>> exportedPortletsHolder = new Holder<List<ExportedPortlet>>();
            Holder<List<FailedPortlets>> failedPortletsHolder = new Holder<List<FailedPortlets>>();
            Holder<Lifetime> lifetimeHolder = new Holder<Lifetime>();
            getPortletManagementService().exportPortlets(getRegistrationContext(), portletContexts, UserAccess.getUserContext(),
               lifetimeHolder, true, exportContextHolder, exportedPortletsHolder, failedPortletsHolder,
               new Holder<ResourceList>(), new Holder<List<Extension>>());

            SortedMap<String, byte[]> handleToState = null;
            List<ExportedPortlet> exportedPortlets = exportedPortletsHolder.value;
            if (ParameterValidation.existsAndIsNotEmpty(exportedPortlets))
            {
               handleToState = new TreeMap<String, byte[]>();
               for (ExportedPortlet exportedPortlet : exportedPortlets)
               {
                  handleToState.put(exportedPortlet.getPortletHandle(), exportedPortlet.getExportData());
               }
            }

            SortedMap<QName, List<String>> errorCodeToHandle = null;
            List<FailedPortlets> failedPortlets = failedPortletsHolder.value;
            if (ParameterValidation.existsAndIsNotEmpty(failedPortlets))
            {
               errorCodeToHandle = new TreeMap<QName, List<String>>();
               for (FailedPortlets failedPortletsForReason : failedPortlets)
               {
                  errorCodeToHandle.put(failedPortletsForReason.getErrorCode(), failedPortletsForReason.getPortletHandles());
               }
            }

            // todo: deal with expiration time
            Lifetime lifetime = lifetimeHolder.value;
            if (lifetime != null)
            {
               XMLGregorianCalendar currentTime = lifetime.getCurrentTime();
               Duration refreshDuration = lifetime.getRefreshDuration();
               XMLGregorianCalendar terminationTime = lifetime.getTerminationTime();
            }

            ExportInfo exportInfo = new ExportInfo(System.currentTimeMillis(), errorCodeToHandle, handleToState, exportContextHolder.value);
            migrationService.add(exportInfo);
            return exportInfo;
         }
         catch (OperationNotSupported operationNotSupported)
         {
            throw new UnsupportedOperationException(operationNotSupported);
         }
         catch (InconsistentParameters inconsistentParameters)
         {
            throw new IllegalArgumentException(inconsistentParameters);
         }
         /*
         // GTNWSRP-62
         catch (AccessDenied accessDenied)
         {
            accessDenied.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         catch (ExportByValueNotSupported exportByValueNotSupported)
         {
            exportByValueNotSupported.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         catch (InvalidHandle invalidHandle)
         {
            invalidHandle.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         catch (InvalidRegistration invalidRegistration)
         {
            invalidRegistration.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         catch (InvalidUserCategory invalidUserCategory)
         {
            invalidUserCategory.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         catch (MissingParameters missingParameters)
         {
            missingParameters.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         catch (ModifyRegistrationRequired modifyRegistrationRequired)
         {
            modifyRegistrationRequired.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         catch (OperationFailed operationFailed)
         {
            operationFailed.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         catch (ResourceSuspended resourceSuspended)
         {
            resourceSuspended.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }*/
         catch (Exception e)
         {
            if (producerInfo.canAttemptRecoveryFrom(e))
            {
               return exportPortlets(portletHandles);
            }
            else
            {
               throw new PortletInvokerException(e.getLocalizedMessage(), e);
            }
         }
      }
      else
      {
         throw new IllegalArgumentException("Must provide a non-null, non-empty list of portlet handles.");
      }
   }

   public void releaseExport(ExportInfo exportInfo) throws PortletInvokerException
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(exportInfo, "ExportInfo to release");

      try
      {
         getPortletManagementService().releaseExport(getRegistrationContext(), exportInfo.getExportContext(), UserAccess.getUserContext());
      }
      catch (PortletInvokerException e)
      {
         if (producerInfo.canAttemptRecoveryFrom(e))
         {
            releaseExport(exportInfo);
         }
         else
         {
            throw e;
         }
      }
   }

   public ImportInfo importPortlets(ExportInfo exportInfo, List<String> portlets) throws PortletInvokerException
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(exportInfo, "ExportInfo to import from");

      if (ParameterValidation.existsAndIsNotEmpty(portlets))
      {
         try
         {
            List<ImportPortlet> importPortlets = new ArrayList<ImportPortlet>(portlets.size());
            for (String portlet : portlets)
            {
               // todo: check semantics
               importPortlets.add(WSRPTypeFactory.createImportPortlet(portlet, exportInfo.getPortletStateFor(portlet)));
            }

            Holder<List<ImportedPortlet>> importedPortletsHolder = new Holder<List<ImportedPortlet>>();
            Holder<List<ImportPortletsFailed>> failedPortletsHolder = new Holder<List<ImportPortletsFailed>>();
            Holder<ResourceList> resourceListHolder = new Holder<ResourceList>();
            getPortletManagementService().importPortlets(getRegistrationContext(), exportInfo.getExportContext(),
               importPortlets, UserAccess.getUserContext(), null, importedPortletsHolder, failedPortletsHolder,
               resourceListHolder, new Holder<List<Extension>>());

            List<ImportedPortlet> importedPortlets = importedPortletsHolder.value;
            SortedMap<String, PortletContext> importIdToPortletContext = new TreeMap<String, PortletContext>();
            if (ParameterValidation.existsAndIsNotEmpty(importedPortlets))
            {
               for (ImportedPortlet importedPortlet : importedPortlets)
               {
                  org.oasis.wsrp.v2.PortletContext portletContext = importedPortlet.getNewPortletContext();
                  PortletContext apiPC = PortletContext.createPortletContext(portletContext.getPortletHandle(), portletContext.getPortletState());
                  // we need to reference the resulting PortletContext so that it can then be used properly
                  importIdToPortletContext.put(importedPortlet.getImportID(), FederatingPortletInvokerService.reference(apiPC, getProducerId()));
               }
            }

            SortedMap<QName, List<String>> errorCodeToHandle = null;
            List<ImportPortletsFailed> failedPortlets = failedPortletsHolder.value;
            if (ParameterValidation.existsAndIsNotEmpty(failedPortlets))
            {
               errorCodeToHandle = new TreeMap<QName, List<String>>();
               for (ImportPortletsFailed failedPortletsForReason : failedPortlets)
               {
                  errorCodeToHandle.put(failedPortletsForReason.getErrorCode(), failedPortletsForReason.getImportID());
               }
            }

            return new ImportInfo(System.currentTimeMillis(), errorCodeToHandle, importIdToPortletContext);
         }
         catch (OperationNotSupported operationNotSupported)
         {
            throw new UnsupportedOperationException(operationNotSupported);
         }
         catch (InconsistentParameters inconsistentParameters)
         {
            throw new IllegalArgumentException(inconsistentParameters);
         }
         /*
         // GTNWSRP-62
         catch (AccessDenied accessDenied)
         {
            accessDenied.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         catch (ExportByValueNotSupported exportByValueNotSupported)
         {
            exportByValueNotSupported.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         catch (InvalidHandle invalidHandle)
         {
            invalidHandle.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         catch (InvalidRegistration invalidRegistration)
         {
            invalidRegistration.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         catch (InvalidUserCategory invalidUserCategory)
         {
            invalidUserCategory.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         catch (MissingParameters missingParameters)
         {
            missingParameters.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         catch (ModifyRegistrationRequired modifyRegistrationRequired)
         {
            modifyRegistrationRequired.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         catch (OperationFailed operationFailed)
         {
            operationFailed.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         catch (ResourceSuspended resourceSuspended)
         {
            resourceSuspended.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }*/
         catch (Exception e)
         {
            if (producerInfo.canAttemptRecoveryFrom(e))
            {
               return importPortlets(exportInfo, portlets);
            }
            else
            {
               throw new PortletInvokerException(e.getLocalizedMessage(), e);
            }
         }
      }
      else
      {
         throw new IllegalArgumentException("Must provide a non-null, non-empty list of portlet handles.");
      }
   }

   public boolean isUsingWSRP2()
   {
      Version wsrpVersion = getWSRPVersion();
      if (wsrpVersion != null)
      {
         return wsrpVersion.getMajor() >= 2;
      }
      else
      {
         return false;
      }
   }

   public MigrationService getMigrationService()
   {
      return migrationService;
   }

   public Version getWSRPVersion()
   {
      return producerInfo.getEndpointConfigurationInfo().getWSRPVersion();
   }
}
