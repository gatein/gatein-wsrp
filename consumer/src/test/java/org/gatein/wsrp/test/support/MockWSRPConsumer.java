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

package org.gatein.wsrp.test.support;

import org.gatein.common.NotYetImplemented;
import org.gatein.pc.api.Portlet;
import org.gatein.pc.api.PortletContext;
import org.gatein.pc.api.PortletInvokerException;
import org.gatein.pc.api.PortletStateType;
import org.gatein.pc.api.PortletStatus;
import org.gatein.pc.api.invocation.PortletInvocation;
import org.gatein.pc.api.invocation.response.PortletInvocationResponse;
import org.gatein.pc.api.state.DestroyCloneFailure;
import org.gatein.pc.api.state.PropertyChange;
import org.gatein.pc.api.state.PropertyMap;
import org.gatein.wsrp.api.session.SessionEvent;
import org.gatein.wsrp.consumer.ProducerInfo;
import org.gatein.wsrp.consumer.RefreshResult;
import org.gatein.wsrp.consumer.handlers.ProducerSessionInformation;
import org.gatein.wsrp.consumer.handlers.SessionHandler;
import org.gatein.wsrp.consumer.handlers.session.SessionRegistry;
import org.gatein.wsrp.consumer.migration.ExportInfo;
import org.gatein.wsrp.consumer.migration.ImportInfo;
import org.gatein.wsrp.consumer.migration.MigrationService;
import org.gatein.wsrp.consumer.portlet.info.WSRPPortletInfo;
import org.gatein.wsrp.consumer.spi.WSRPConsumerSPI;
import org.gatein.wsrp.services.MarkupService;
import org.oasis.wsrp.v2.RegistrationContext;
import org.oasis.wsrp.v2.RuntimeContext;
import org.oasis.wsrp.v2.UserContext;

import javax.servlet.http.HttpSession;
import java.util.List;
import java.util.Set;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 8784 $
 * @since 2.6
 */
public class MockWSRPConsumer implements WSRPConsumerSPI
{
   private ProducerInfo producerInfo;
   private boolean useWSRP2 = true;

   public MockWSRPConsumer(String id)
   {
      producerInfo = new ProducerInfo(null);
      producerInfo.setId(id);
      producerInfo.setEndpointConfigurationInfo(new MockEndpointConfigurationInfo());
   }

   public String getProducerId()
   {
      return producerInfo.getId();
   }

   public ProducerSessionInformation getProducerSessionInformationFrom(PortletInvocation invocation)
   {
      return null;
   }

   public ProducerSessionInformation getProducerSessionInformationFrom(HttpSession session)
   {
      return null;
   }

   public ProducerInfo getProducerInfo()
   {
      return producerInfo;
   }

   public MarkupService getMarkupService() throws PortletInvokerException
   {
      throw new NotYetImplemented();
   }

   public boolean supportsUserScope(String userScope)
   {
      throw new NotYetImplemented();
   }

   public WSRPPortletInfo getPortletInfo(PortletInvocation invocation) throws PortletInvokerException
   {
      throw new NotYetImplemented();
   }

   public SessionRegistry getSessionRegistry()
   {
      throw new NotYetImplemented();
   }

   @Override
   public void handleModifyRegistrationRequiredFault()
   {
      throw new NotYetImplemented();
   }

   public void setSessionRegistry(SessionRegistry sessionInfos)
   {
      throw new NotYetImplemented();
   }

   public RegistrationContext getRegistrationContext() throws PortletInvokerException
   {
      throw new NotYetImplemented();
   }

   public UserContext getUserContextFrom(WSRPPortletInfo info, PortletInvocation invocation, RuntimeContext runtimeContext) throws PortletInvokerException
   {
      throw new NotYetImplemented();
   }

   public SessionHandler getSessionHandler()
   {
      throw new NotYetImplemented();
   }

   public void setTemplatesIfNeeded(WSRPPortletInfo info, PortletInvocation invocation, RuntimeContext runtimeContext) throws PortletInvokerException
   {
      throw new NotYetImplemented();
   }

   public void refreshProducerInfo() throws PortletInvokerException
   {
      throw new NotYetImplemented();
   }

   public void handleInvalidRegistrationFault() throws PortletInvokerException
   {
      throw new NotYetImplemented();
   }

   public void releaseSessions()
   {
   }

   public void activate() throws Exception
   {
   }

   public void deactivate() throws Exception
   {
   }

   public boolean isActive()
   {
      return true;
   }

   public boolean isRefreshNeeded()
   {
      return false;
   }

   public RefreshResult refresh(boolean forceRefresh)
   {
      return null;
   }

   public Set<Portlet> getPortlets() throws PortletInvokerException
   {
      return null;
   }

   public Portlet getPortlet(PortletContext portletContext) throws IllegalArgumentException, PortletInvokerException
   {
      return null;
   }

   public PortletStatus getStatus(PortletContext portletContext) throws IllegalArgumentException, PortletInvokerException
   {
      return null;
   }

   public PortletInvocationResponse invoke(PortletInvocation invocation) throws IllegalArgumentException, PortletInvokerException
   {
      return null;
   }

   public PortletContext createClone(PortletStateType stateType, PortletContext portletContext) throws IllegalArgumentException, PortletInvokerException, UnsupportedOperationException
   {
      return null;
   }

   public List<DestroyCloneFailure> destroyClones(List<PortletContext> portletContexts) throws IllegalArgumentException, PortletInvokerException, UnsupportedOperationException
   {
      return null;
   }

   public PropertyMap getProperties(PortletContext portletContext, Set keys) throws IllegalArgumentException, PortletInvokerException, UnsupportedOperationException
   {
      return null;
   }

   public PropertyMap getProperties(PortletContext portletContext) throws IllegalArgumentException, PortletInvokerException, UnsupportedOperationException
   {
      return null;
   }

   public PortletContext setProperties(PortletContext portletContext, PropertyChange[] changes) throws IllegalArgumentException, PortletInvokerException, UnsupportedOperationException
   {
      return null;
   }

   public void create() throws Exception
   {
   }

   public void start() throws Exception
   {
   }

   public void stop()
   {
   }

   public ExportInfo exportPortlets(List<String> portletHandles) throws PortletInvokerException
   {
      throw new NotYetImplemented();
   }

   public void releaseExport(ExportInfo exportInfo)
   {
      throw new NotYetImplemented();
   }

   public MigrationService getMigrationService()
   {
      throw new NotYetImplemented();
   }

   public boolean isImportExportSupported()
   {
      return false;
   }

   public ImportInfo importPortlets(ExportInfo exportInfo, List<String> portlets) throws PortletInvokerException
   {
      throw new NotYetImplemented();
   }

   public boolean isUsingWSRP2()
   {
      return useWSRP2;
   }

   public void setUsingWSRP2(boolean useWSRP2)
   {
      this.useWSRP2 = useWSRP2;
   }

   public void destroy()
   {
   }

   public void onSessionEvent(SessionEvent event)
   {
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
}
