/*
* JBoss, a division of Red Hat
* Copyright 2012, Red Hat Middleware, LLC, and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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

package org.gatein.wsrp.wss.cxf.consumer;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.message.Message;
import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.ws.policy.AbstractPolicyInterceptorProvider;
import org.apache.cxf.ws.policy.AssertionInfo;
import org.apache.cxf.ws.policy.AssertionInfoMap;
import org.apache.cxf.ws.policy.PolicyInterceptorProviderRegistry;
import org.gatein.wsrp.services.PortCustomizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import java.util.Collection;

/** @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a> */
public class BEAPolicyIgnoringPortCustomizer implements PortCustomizer
{
   private static final Logger log = LoggerFactory.getLogger(BEAPolicyIgnoringPortCustomizer.class);

   public static final String IGNORE_BEA_POLICY_PROPERTY_NAME = "org.gatein.wsrp.consumer.ignoreDefaultBEAPolicy";
   private static final QName BEA_DEFAULT_POLICY = new QName("http://www.bea.com/wls90/security/policy", "Identity");
   private static final IgnorableAssertionsInterceptor IGNORABLE_ASSERTIONS_INTERCEPTOR = new IgnorableAssertionsInterceptor();
   private static final IgnorablePolicyInterceptorProvider IGNORABLE_POLICY_INTERCEPTOR_PROVIDER = new IgnorablePolicyInterceptorProvider();
   private static boolean ignore = true;

   public BEAPolicyIgnoringPortCustomizer()
   {
   }

   static
   {
      String ignoreProperty = System.getProperty(IGNORE_BEA_POLICY_PROPERTY_NAME);
      if (ignoreProperty != null && !ignoreProperty.isEmpty())
      {
         ignore = Boolean.parseBoolean(ignoreProperty);
      }
   }

   @Override
   public void customizePort(Object service)
   {
      if (ignore)
      {
         log.debug("Injecting interceptor to ignore BEA WebLogic 10's default WS-Policy.");

         Client client = ClientProxy.getClient(service);

         // CXF needs Policy interceptors to be provided by a provider otherwise it will be ignored
         final PolicyInterceptorProviderRegistry providerRegistry = client.getBus().getExtension(PolicyInterceptorProviderRegistry.class);
         providerRegistry.register(IGNORABLE_POLICY_INTERCEPTOR_PROVIDER);
      }
   }

   @Override
   public boolean isWSSFocused()
   {
      return false;
   }

   /** This policy interceptor provider can be used to implicitly handle unknown policy assertions. */
   private static class IgnorablePolicyInterceptorProvider extends AbstractPolicyInterceptorProvider
   {
      public IgnorablePolicyInterceptorProvider()
      {
         super(BEA_DEFAULT_POLICY);

         getInInterceptors().add(IGNORABLE_ASSERTIONS_INTERCEPTOR);
         getOutInterceptors().add(IGNORABLE_ASSERTIONS_INTERCEPTOR);
         getInFaultInterceptors().add(IGNORABLE_ASSERTIONS_INTERCEPTOR);
         getOutFaultInterceptors().add(IGNORABLE_ASSERTIONS_INTERCEPTOR);
      }
   }

   private static class IgnorableAssertionsInterceptor extends AbstractPhaseInterceptor<Message>
   {
      public IgnorableAssertionsInterceptor()
      {
         // somewhat irrelevant
         super(Phase.POST_LOGICAL);
      }

      public void handleMessage(Message message) throws Fault
      {
         AssertionInfoMap aim = message.get(AssertionInfoMap.class);
         Collection<AssertionInfo> ais = aim.getAssertionInfo(BEA_DEFAULT_POLICY);
         log.info("Ignoring " + BEA_DEFAULT_POLICY);
         if (null != ais)
         {
            for (AssertionInfo ai : ais)
            {
               ai.setAsserted(true);
            }
         }
      }
   }
}
