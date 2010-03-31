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

import org.gatein.common.util.ParameterValidation;
import org.gatein.pc.api.InvokerUnavailableException;
import org.gatein.pc.api.NoSuchPortletException;
import org.gatein.pc.api.Portlet;
import org.gatein.pc.api.PortletContext;
import org.gatein.pc.api.PortletInvokerException;
import org.gatein.pc.api.PortletStateType;
import org.gatein.pc.api.invocation.ActionInvocation;
import org.gatein.pc.api.invocation.InvocationException;
import org.gatein.pc.api.invocation.PortletInvocation;
import org.gatein.pc.api.invocation.RenderInvocation;
import org.gatein.pc.api.invocation.ResourceInvocation;
import org.gatein.pc.api.invocation.response.PortletInvocationResponse;
import org.gatein.pc.api.spi.UserContext;
import org.gatein.pc.api.state.DestroyCloneFailure;
import org.gatein.pc.api.state.PropertyChange;
import org.gatein.pc.api.state.PropertyMap;
import org.gatein.pc.portlet.impl.spi.AbstractPortletInvocationContext;
import org.gatein.pc.portlet.state.SimplePropertyMap;
import org.gatein.wsrp.UserContextConverter;
import org.gatein.wsrp.WSRPConstants;
import org.gatein.wsrp.WSRPConsumer;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.WSRPUtils;
import org.gatein.wsrp.api.SessionEvent;
import org.gatein.wsrp.consumer.portlet.WSRPPortlet;
import org.gatein.wsrp.consumer.portlet.info.WSRPPortletInfo;
import org.gatein.wsrp.servlet.UserAccess;
import org.oasis.wsrp.v1.DestroyFailed;
import org.oasis.wsrp.v1.Extension;
import org.oasis.wsrp.v1.Property;
import org.oasis.wsrp.v1.PropertyList;
import org.oasis.wsrp.v1.RegistrationContext;
import org.oasis.wsrp.v1.RegistrationData;
import org.oasis.wsrp.v1.ResetProperty;
import org.oasis.wsrp.v1.RuntimeContext;
import org.oasis.wsrp.v1.WSRPV1MarkupPortType;
import org.oasis.wsrp.v1.WSRPV1PortletManagementPortType;
import org.oasis.wsrp.v1.WSRPV1RegistrationPortType;
import org.oasis.wsrp.v1.WSRPV1ServiceDescriptionPortType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.ws.Holder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:boleslaw.dawidowicz@jboss.org">Boleslaw Dawidowicz</a>
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 11692 $
 * @since 2.4
 */
public class WSRPConsumerImpl implements WSRPConsumer
{
   private ActionHandler actionHandler;
   private RenderHandler renderHandler;
   private ResourceHandler resourceHandler;
   private SessionHandler sessionHandler;

   private ProducerInfo producerInfo;

   /** A registration data element used to indicate when no registration was required by the producer */
   private final static RegistrationData REGISTRATION_NOT_NEEDED = WSRPTypeFactory.createDefaultRegistrationData();

   private final static Logger log = LoggerFactory.getLogger(WSRPConsumer.class);

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
      this(new ProducerInfo());
   }

   public WSRPConsumerImpl(ProducerInfo info)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(info, "ProducerInfo");

      producerInfo = info;
      actionHandler = new ActionHandler(this);
      renderHandler = new RenderHandler(this);
      sessionHandler = new SessionHandler(this);
      resourceHandler = new ResourceHandler(this);
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
      InvocationHandler handler;

      if (invocation instanceof RenderInvocation)
      {
         handler = renderHandler;
      }
      else if (invocation instanceof ActionInvocation)
      {
         handler = actionHandler;
      }
      else if (invocation instanceof ResourceInvocation)
      {
         handler = resourceHandler;
      }
      else
      {
         throw new InvocationException("Unknown invocation type: " + invocation);
      }

      return handler.handle(invocation);
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
            WSRPUtils.convertToWSRPPortletContext(portletContext), UserAccess.getUserContext(), handle, portletState,
            new Holder<List<Extension>>()
         );
         return WSRPUtils.convertToPortalPortletContext(handle.value, portletState.value);
      }
      catch (Exception e)
      {
         throw new PortletInvokerException("Couldn't clone portlet '" + portletContext.getId() + "'", e);
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
      log.debug("Attempting to destroy clones: " + handles);

      try
      {
         Holder<List<DestroyFailed>> destroyFailed = new Holder<List<DestroyFailed>>();

         getPortletManagementService().destroyPortlets(getRegistrationContext(), handles, destroyFailed, new Holder<List<Extension>>());

         List<DestroyFailed> failures = destroyFailed.value;
         List<DestroyCloneFailure> result = Collections.emptyList();
         if (failures != null)
         {
            result = new ArrayList<DestroyCloneFailure>(failures.size());
            // list all the failures and successes
            for (DestroyFailed failure : failures)
            {
               String handle = failure.getPortletHandle();
               result.add(new DestroyCloneFailure(handle, failure.getReason()));
               handles.remove(handle);
               log.debug("Couldn't destroy clone '" + handle + "'");
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
         throw new PortletInvokerException("Couldn't destroy clones.", e);
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
               String name = prop.getName();
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
      catch (Exception e)
      {
         // something went wrong but support for getPortletProperties is optional so return an empty PropertyMap
         log.debug("Couldn't get properties for portlet '" + portletContext.getId() + "'", e);
         return new SimplePropertyMap();
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
            new Holder<List<Extension>>()
         );
         PortletContext newPortletContext = PortletContext.createPortletContext(handle.value, portletState.value);
         portlet.setPortletContext(newPortletContext);
         return newPortletContext;
      }
      catch (Exception e)
      {
         throw new PortletInvokerException("Unable to set properties for portlet '" + portletContext.getId() + "'", e);
      }
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
   static PortletContext getPortletContext(PortletInvocation invocation)
   {
      return invocation.getTarget();
   }

   WSRPPortletInfo getPortletInfo(PortletInvocation invocation) throws PortletInvokerException
   {
      return (WSRPPortletInfo)getWSRPPortlet(getPortletContext(invocation)).getInfo();
   }

   WSRPPortlet getWSRPPortlet(PortletContext portletContext) throws PortletInvokerException
   {
      return (WSRPPortlet)getPortlet(portletContext);
   }

   public Set getSupportedUserScopes()
   {
      return Collections.unmodifiableSet(supportedUserScopes);
   }

   /**
    * Determines whether the specified user scope (for markup caching) is supported.
    *
    * @param userScope the user scope which support is to be determined
    * @return <code>true</code> if the given user scope is supported, <code>false</code> otherwise
    */
   public boolean supportsUserScope(String userScope)
   {
      return supportedUserScopes.contains(userScope);
   }

   // Registration *****************************************************************************************************

   void handleInvalidRegistrationFault() throws PortletInvokerException
   {
      // reset registration data and try again
      producerInfo.resetRegistration();
      refreshProducerInfo(true);
   }

   RegistrationContext getRegistrationContext() throws PortletInvokerException
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

   public WSRPV1ServiceDescriptionPortType getServiceDescriptionService() throws PortletInvokerException
   {
      refreshProducerInfo(false);
      return getEndpointConfigurationInfo().getServiceDescriptionService();
   }

   public WSRPV1MarkupPortType getMarkupService() throws PortletInvokerException
   {
      refreshProducerInfo(false);
      return getEndpointConfigurationInfo().getMarkupService();
   }

   public WSRPV1PortletManagementPortType getPortletManagementService() throws PortletInvokerException
   {
      refreshProducerInfo(false);
      return getEndpointConfigurationInfo().getPortletManagementService();
   }

   public WSRPV1RegistrationPortType getRegistrationService() throws PortletInvokerException
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
      RefreshResult refreshResult = producerInfo.detailedRefresh(forceRefresh);
      sessionHandler.setRequiresInitCookie(producerInfo.getRequiresInitCookie());
      return refreshResult;
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

   org.oasis.wsrp.v1.UserContext getUserContextFrom(PortletInvocation invocation, RuntimeContext runtimeContext) throws PortletInvokerException
   {
      // first decide if we need to pass the user context...
      WSRPPortletInfo info = getPortletInfo(invocation);

      if (info != null && info.isUserContextStoredInSession() && runtimeContext.getSessionID() != null)
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

   void setTemplatesIfNeeded(PortletInvocation invocation, RuntimeContext runtimeContext) throws PortletInvokerException
   {
      // todo: could store templates in producer session info to avoid to re-generate them all the time?
      WSRPPortletInfo info = getPortletInfo(invocation);

      if (info != null && info.isDoesUrlTemplateProcessing()
         && (!info.isTemplatesStoredInSession() || runtimeContext.getSessionID() == null))
      {
         // we need to supply the templates since the portlet does URL processing and either doesn't store
         // templates in the session or no session has been established yet
         runtimeContext.setTemplates(WSRPTypeFactory.createTemplates(invocation.getContext()));
      }
   }

   static HttpServletRequest getHttpRequest(PortletInvocation invocation)
   {
      AbstractPortletInvocationContext invocationContext = (AbstractPortletInvocationContext)invocation.getContext();
      return invocationContext.getClientRequest();
   }

   static HttpSession getHttpSession(PortletInvocation invocation)
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
}
