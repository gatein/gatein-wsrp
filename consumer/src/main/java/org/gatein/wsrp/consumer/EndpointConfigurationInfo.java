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

package org.gatein.wsrp.consumer;

import org.gatein.common.util.ParameterValidation;
import org.gatein.common.util.Version;
import org.gatein.pc.api.InvokerUnavailableException;
import org.gatein.wsrp.consumer.handlers.ProducerSessionInformation;
import org.gatein.wsrp.handler.RequestHeaderClientHandler;
import org.gatein.wsrp.services.MarkupService;
import org.gatein.wsrp.services.PortletManagementService;
import org.gatein.wsrp.services.RegistrationService;
import org.gatein.wsrp.services.SOAPServiceFactory;
import org.gatein.wsrp.services.ServiceDescriptionService;
import org.gatein.wsrp.services.ServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 */
public class EndpointConfigurationInfo
{
   private static final Logger log = LoggerFactory.getLogger(EndpointConfigurationInfo.class);
   private static final boolean DEBUG_ENABLED = log.isDebugEnabled();

   private static final int DEFAULT_COOLDOWN_PERIOD_SECONDS = 60;
   private static int cooldownSeconds = DEFAULT_COOLDOWN_PERIOD_SECONDS;

   static
   {
      // retrieve the system property that specifies how long the cooldown should be for producers before being re-added to the URL pool
      final String cooldown = System.getProperty("org.gatein.wsrp.consumer.producerCooldownSeconds");
      if (!ParameterValidation.isNullOrEmpty(cooldown))
      {
         try
         {
            cooldownSeconds = Integer.parseInt(cooldown);
            if (cooldownSeconds <= 0)
            {
               // if the specified value is not strictly positive, use the default value
               cooldownSeconds = DEFAULT_COOLDOWN_PERIOD_SECONDS;
            }
         }
         catch (NumberFormatException e)
         {
            // use default value if the property is not a numberWN_PERIOD_SECONDS;
         }
      }
   }


   // loadbalancer implementation support
   /** Parsed list of WSDL URLs */
   private transient List<String> allWSDLURLs = Collections.emptyList();
   /** The ordered (in the given order of URLs) map of ServiceFactories */
   private transient LinkedHashMap<String, ServiceFactory> urlToServiceFactory = new LinkedHashMap<String, ServiceFactory>();
   /** pointer to the current URL (modulo a modulo ^_^) for logged in users */
   private transient int currentLoggedInURL;
   /** pointer to the current URL (modulo a modulo ^_^) for requests not associated with a session */
   private transient int currentUnloggedURL;
   /** Separator that separate URLs in WSDL */
   private static final String SEPARATOR = " ";
   /** Computed composite WSDL URL */
   private transient String wsdlURL;
   /** Amount of milliseconds before a WS operation is considered as having timed out */
   private transient int msBeforeTimeOut;
   /** ServiceFactory prototype used to create ServiceFactories (mostly used so that we can use different types of factories, especially for tests) */
   private final ServiceFactory factoryPrototype;

   public EndpointConfigurationInfo(ServiceFactory serviceFactory)
   {
      factoryPrototype = serviceFactory;
      wsdlURL = serviceFactory.getWsdlDefinitionURL();
      msBeforeTimeOut = serviceFactory.getWSOperationTimeOut();
   }

   public EndpointConfigurationInfo()
   {
      this(new SOAPServiceFactory());
   }

   public synchronized String getWsdlDefinitionURL()
   {
      return wsdlURL;
   }

   String getEffectiveWSDLURL()
   {
      return getServiceFactory().getWsdlDefinitionURL();
   }

   /**
    * Sets the WSDL URL to use to access the associated producer. Also checks whether the specified String actually defines a list of URLs separated by {@link #SEPARATOR} in which
    * case we switch to a loadbalancer behavior.
    *
    * @param wsdlDefinitionURL the URL of the WSDL for the associated producer
    */
   public synchronized void setWsdlDefinitionURL(String wsdlDefinitionURL)
   {
      if (wsdlDefinitionURL != null)
      {
         // first trim the String
         wsdlDefinitionURL = wsdlDefinitionURL.trim();

         if (wsdlDefinitionURL.contains(SEPARATOR))
         {
            // we have a URL with a separator to support passing several URLs for a simple failover mechanism, so we need to extract the individual URLs
            final String[] urls = wsdlDefinitionURL.split("\\s+");
            // make sure we don't have duplicates
            allWSDLURLs = new ArrayList<String>(urls.length);
            for (String url : urls)
            {
               if (!allWSDLURLs.contains(url))
               {
                  allWSDLURLs.add(url);
               }
            }

            // copy the existing ServiceFactories
            Map<String, ServiceFactory> existing = new HashMap<String, ServiceFactory>(urlToServiceFactory);
            // clear existing
            urlToServiceFactory.clear();
            // add ServiceFactories for each URL, keeping the ones we already have when a URL already existed
            // we re-add things to keep potentially new URL order
            for (String url : allWSDLURLs)
            {
               // retrieve the existing ServiceFactory
               ServiceFactory serviceFactory = existing.get(url);
               if (serviceFactory == null)
               {
                  // if we don't already have a ServiceFactory for this URL, create one
                  serviceFactory = createFromPrototype();
                  serviceFactory.setWsdlDefinitionURL(url);
                  serviceFactory.setWSOperationTimeOut(msBeforeTimeOut);
               }
               urlToServiceFactory.put(url, serviceFactory);

            }
         }
         else
         {
            // we only have one URL
            allWSDLURLs = Collections.singletonList(wsdlDefinitionURL);

            // remove all ServiceFactories
            urlToServiceFactory.clear();

            // add a new one with the proper WSDL URL
            ServiceFactory serviceFactory = createFromPrototype();
            serviceFactory.setWsdlDefinitionURL(wsdlDefinitionURL);
            serviceFactory.setWSOperationTimeOut(msBeforeTimeOut);
            urlToServiceFactory.put(wsdlDefinitionURL, serviceFactory);
         }
      }

      // reset current URL pointers
      currentLoggedInURL = 0;
      currentUnloggedURL = 0;
      wsdlURL = wsdlDefinitionURL;
   }

   private ServiceFactory createFromPrototype()
   {
      return factoryPrototype.clone();
   }

   public List<String> getAllWSDLURLs()
   {
      return Collections.unmodifiableList(allWSDLURLs);
   }

   public void start() throws Exception
   {
      // start the factory associated with each URL
      List<ServiceFactory> factories = new ArrayList<ServiceFactory>(urlToServiceFactory.values());
      for (ServiceFactory factory : factories)
      {
         try
         {
            factory.start();
         }
         catch (Exception e)
         {
            removeServiceFactory(factory);
         }
      }

      // all factories should use the same WSRP version and WSS availabilty
      final ServiceFactory factory = factories.get(0);
      initStateFrom(factory);
   }

   private synchronized void initStateFrom(ServiceFactory factory)
   {
      msBeforeTimeOut = factory.getWSOperationTimeOut();
   }

   private void removeServiceFactory(ServiceFactory factory)
   {
      final String url = factory.getWsdlDefinitionURL();
      removeServiceFactoryFor(url);
   }

   private synchronized void removeServiceFactoryFor(final String url)
   {
      if (DEBUG_ENABLED)
      {
         log.debug("ServiceFactory for URL '" + url + "' is not available. Removing it from round-robin.");
      }

      // remove the failed factory from the available ones but remember it
      final ServiceFactory removed = urlToServiceFactory.remove(url);
      final int index = allWSDLURLs.indexOf(url);

      if (index >= 0)
      {
         allWSDLURLs.remove(index);
      }

      // schedule it to be re-added after a cooldown period (if it existed in the first place)
      if (removed != null)
      {
         ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
         scheduledExecutorService.schedule(new Runnable()
         {
            public void run()
            {
               // re-add URL and factory
               urlToServiceFactory.put(url, removed);
               allWSDLURLs.add(url);
               if (DEBUG_ENABLED)
               {
                  log.debug("Re-added ServiceFactory for URL '" + url + "' after " + cooldownSeconds + " seconds cooldown period.");
               }
               // and recompute the compound WSDL
               recomputeWSDLURL();
            }
         }, cooldownSeconds, TimeUnit.SECONDS);
      }

      // if, after removing the URL, we don't have any anymore, throw an exception since we cannot do anything anymore
      if (allWSDLURLs.isEmpty())
      {
         throw new RuntimeException("Couldn't find an available ServiceFactory!"); // todo: improve error message / deal with this condition better
      }

      // compute next URL to use
      currentLoggedInURL = index + 1;
      currentUnloggedURL = index + 1;

      // re-compute the WSDL URL
      recomputeWSDLURL();
   }

   private synchronized void recomputeWSDLURL()
   {
      final int urlNumber = getNumberOfWSDLURLs();
      StringBuilder sb = new StringBuilder(urlNumber * 128);
      int i = 0;
      for (String wsdl : allWSDLURLs)
      {
         sb.append(wsdl);
         if (i++ != urlNumber - 1)
         {
            sb.append(SEPARATOR);
         }
      }
      wsdlURL = sb.toString();
   }

   public void stop() throws Exception
   {
      for (ServiceFactory factory : urlToServiceFactory.values())
      {
         factory.stop();
      }
   }

   ServiceFactory getServiceFactory()
   {
      return getServiceFactory(true);
   }

   /**
    * Retrieves an available ServiceFactory, starting it if specified, or <code>null</code> if none could be found.
    *
    * @param start whether we should start the selected ServiceFactory
    * @return an available ServiceFactory (selected via a simple round-robin process, failing over to the next one if the currently selected one is down)
    * @throws RuntimeException if no available ServiceFactory can be found
    */
   ServiceFactory getServiceFactory(boolean start)
   {
      // figure out which ServiceFactory to use
      // first, check if there's already a ServiceFactory associated with the current session information to have sticky behavior
      final ProducerSessionInformation sessionInfo = RequestHeaderClientHandler.getProducerSessionInformation(true);
      ServiceFactory factory = sessionInfo.getServiceFactory();
      String parentSessionId = sessionInfo.getParentSessionId();

      synchronized (this)
      {
         // only use the factory from the session information if we have configured URLs and the factory is still part of the set of available factories
         if (factory != null && !allWSDLURLs.isEmpty() && !urlToServiceFactory.containsKey(factory.getWsdlDefinitionURL()))
         {
            // remove the factory from the session information
            sessionInfo.setServiceFactory(null);
            // set the factory to null so that another one can be picked
            factory = null;
         }
      }

      // if we haven't found a ServiceFactory, pick one from the available ones
      final boolean logged = parentSessionId != null;
      if (factory == null)
      {
         final int size = getNumberOfWSDLURLs();
         if (size == 0)
         {
            // if we don't have a list of URLs to work with yet, return a new instance based on prototype
            factory = createFromPrototype();
         }
         else
         {
            synchronized (this)
            {
               // very simple round-robin logic: infinitely increment a counter and modulo by the size of the available pool
               final int selectedFactory = logged ? currentLoggedInURL % size : currentUnloggedURL % size;
               if (DEBUG_ENABLED)
               {
                  log.debug("ServiceFactory selected: " + selectedFactory + " out of " + size + " available (currentLoggedInURL = " + currentLoggedInURL + ")");
               }
               // get the ServiceFactory associated with the currently selected URL
               factory = urlToServiceFactory.get(allWSDLURLs.get(selectedFactory));

               // increment pointer to current URL; we use 2 different pointers since a WSRP request will first retrieve the portlet info without associated session resulting
               // in potentially assigning logged users to the same factory if there are only 2 of them available
               if (logged)
               {
                  currentLoggedInURL++;
               }
               else
               {
                  currentUnloggedURL++;
               }
            }
         }
      }

      // make sure the factory is started if we intend to use it
      if (start && !factory.isAvailable())
      {
         try
         {
            factory.start();
            initStateFrom(factory);
         }
         catch (Exception e)
         {
            // factory didn't start properly, remove it from available ones and attempt to retrieve another one by recursively calling ourselves
            removeServiceFactory(factory);
            return getServiceFactory(true);
         }
      }

      // if everything went well, associate the factory we picked with the current session information
      sessionInfo.setServiceFactory(factory);
      if (logged && DEBUG_ENABLED)
      {
         log.debug("Consumer Session '" + parentSessionId + "' is associated with ServiceFactory " + factory + ", producer URL: '" + factory.getWsdlDefinitionURL());
      }

      return factory;
   }

   ServiceDescriptionService getServiceDescriptionService() throws InvokerUnavailableException
   {
      try
      {
         return getServiceFactory().getServiceDescriptionService();
      }
      catch (Exception e)
      {
         throw new InvokerUnavailableException("Couldn't access ServiceDescription service. Cause: "
            + e.getLocalizedMessage(), e);
      }
   }

   MarkupService getMarkupService() throws InvokerUnavailableException
   {
      try
      {
         return getServiceFactory().getMarkupService();
      }
      catch (Exception e)
      {
         throw new InvokerUnavailableException("Couldn't access Markup service. Cause: "
            + e.getLocalizedMessage(), e);
      }
   }

   PortletManagementService getPortletManagementService() throws InvokerUnavailableException
   {
      try
      {
         return getServiceFactory().getPortletManagementService();
      }
      catch (Exception e)
      {
         throw new InvokerUnavailableException("Couldn't access PortletManagement service. Cause: "
            + e.getLocalizedMessage(), e);
      }
   }

   RegistrationService getRegistrationService() throws InvokerUnavailableException
   {
      try
      {
         return getServiceFactory().getRegistrationService();
      }
      catch (Exception e)
      {
         throw new InvokerUnavailableException("Couldn't access Registration service. Cause: "
            + e.getLocalizedMessage(), e);
      }
   }

   public boolean isAvailable()
   {
      return getServiceFactory(false).isAvailable();
   }

   public boolean isRefreshNeeded()
   {
      // we need a refresh if we're not available
      boolean result = !isAvailable();
      if (result && DEBUG_ENABLED)
      {
         log.debug("Refresh needed");
      }
      return result;
   }

   public boolean refresh() throws InvokerUnavailableException
   {
      return isRefreshNeeded() && forceRefresh();
   }

   boolean forceRefresh() throws InvokerUnavailableException
   {
      try
      {
         return getServiceFactory().refresh(true);
      }
      catch (Exception e)
      {
         throw new InvokerUnavailableException(e);
      }
   }

   public String getRemoteHostAddress()
   {
      // extract host URL
      String wsdl = getWsdlDefinitionURL();
      int hostBegin = wsdl.indexOf("://") + 3;
      return wsdl.substring(0, wsdl.indexOf('/', hostBegin));
   }

   /**
    * Number of milliseconds before a WS operation is considered as having timed out.
    *
    * @param msBeforeTimeOut number of milliseconds to wait for a WS operation to return before timing out. Will be set
    *                        to {@link ServiceFactory#DEFAULT_TIMEOUT_MS} if negative.
    */
   public synchronized void setWSOperationTimeOut(int msBeforeTimeOut)
   {
      this.msBeforeTimeOut = msBeforeTimeOut;
      // set the new value on all ServiceFactories
      for (ServiceFactory factory : urlToServiceFactory.values())
      {
         factory.setWSOperationTimeOut(msBeforeTimeOut);
      }
   }

   public synchronized int getWSOperationTimeOut()
   {
      return msBeforeTimeOut;
   }

   Version getWSRPVersion()
   {
      return getServiceFactory().getWSRPVersion();
   }

   public synchronized boolean isLoadbalancing()
   {
      return allWSDLURLs.size() > 1;
   }

   public synchronized int getNumberOfWSDLURLs()
   {
      return allWSDLURLs.size();
   }

   public boolean switchProducerIfPossible()
   {
      // assume that we have a failed ServiceFactory so remove it from the current session and from the set of available factories
      final ProducerSessionInformation sessionInformation = RequestHeaderClientHandler.getCurrentProducerSessionInformation();
      removeServiceFactory(sessionInformation.getServiceFactory());
      sessionInformation.setServiceFactory(null);

      // try to get a ServiceFactory, if we get one, we successfully switched producer if we assumed the current ServiceFactory had failed...
      return getServiceFactory() != null;
   }
}
