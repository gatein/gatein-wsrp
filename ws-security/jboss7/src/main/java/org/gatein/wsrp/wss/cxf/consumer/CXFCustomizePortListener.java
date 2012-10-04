/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2012, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/
package org.gatein.wsrp.wss.cxf.consumer;

import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.ws.security.wss4j.WSS4JInInterceptor;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.ws.security.handler.WSHandlerConstants;
import org.gatein.wci.security.Credentials;
import org.gatein.wsrp.wss.CustomizePortListener;
import org.gatein.wsrp.wss.WebServiceSecurityFactory;
import org.gatein.wsrp.wss.credentials.CredentialsAccessor;
import org.gatein.wsrp.wss.cxf.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class CXFCustomizePortListener implements CustomizePortListener
{
   private static Logger log = LoggerFactory.getLogger(CXFCustomizePortListener.class);

   protected static String GTN_CURRENT_USER = "gtn.current.user";
   protected static String GTN_USERNAME_TOKEN_IF_AUTHENTICATED = "gtn.UsernameToken.ifCurrentUserAuthenticated";
   protected static String GTN_NO_USER = "gtn.no.user";

   @Override
   public void customizePort(Object service)
   {
      log.debug("Customizing the port for the wsrp cxf client.");

      Client client = ClientProxy.getClient(service);

      Map<String, Object> inPropertyMap = getWSS4JInInterceptorProperties();
      Map<String, Object> outPropertyMap = getWSS4JOutInterceptorProperties();

      if (inPropertyMap != null && handleSpecialProperties(inPropertyMap))
      {
         WSS4JInInterceptor inInterceptor = new WSS4JInInterceptor(inPropertyMap);
         client.getInInterceptors().add(inInterceptor);
      }

      if (outPropertyMap != null && handleSpecialProperties(outPropertyMap))
      {
         WSS4JOutInterceptor outInterceptor = new WSS4JOutInterceptor(outPropertyMap);
         client.getOutInterceptors().add(outInterceptor);
      }
   }

   protected Map<String, Object> getWSS4JInInterceptorProperties()
   {
      String wss4jInInterceptorConfigPath = Utils.CONSUMER_CONF_DIR_NAME + File.separator + Utils.WSS4J_ININTERCEPTOR_PROPERTY_FILE;
      Map<String, Object> inInterceptorProperties = Utils.getCXFConfigProperties(wss4jInInterceptorConfigPath);

      if (inInterceptorProperties == null)
      {
         log.debug("The WSS4JInInterceptor configuration file could not be found. No WSS4JInInterceptor will be added to the wsrp consumer.");
      }

      return inInterceptorProperties;
   }

   protected Map<String, Object> getWSS4JOutInterceptorProperties()
   {
      String wss4jOutInterceptorConfigPath = Utils.CONSUMER_CONF_DIR_NAME + File.separator + Utils.WSS4J_OUTINTERCEPTOR_PROPERTY_FILE;
      Map<String, Object> outInterceptorProperties = Utils.getCXFConfigProperties(wss4jOutInterceptorConfigPath);

      if (outInterceptorProperties == null)
      {
         log.debug("The WSS4JOutInterceptor configuration file could not be found. No WSS4JOutInterceptor will be added to the wsrp consumer.");
      }

      return outInterceptorProperties;
   }

   /**
    * Handles special properties which are specific to our wsrp configuration
    *
    * @param propertyMap The map of properties to consider
    * @return True only if the propertymap should be used
    */
   protected boolean handleSpecialProperties(Map<String, Object> propertyMap)
   {
      return handleUserAuthentication(propertyMap);
   }

   protected boolean handleUserAuthentication(Map<String, Object> propertyMap)
   {
      if (propertyMap.containsKey(WSHandlerConstants.USER) && propertyMap.get(WSHandlerConstants.USER).equals(GTN_CURRENT_USER))
      {
         CredentialsAccessor credentialsAccessor = WebServiceSecurityFactory.getInstance().getCredentialsAccessor();
         if (credentialsAccessor != null && credentialsAccessor.getCredentials() != null)
         {
            Credentials credentials = credentialsAccessor.getCredentials();
            propertyMap.put(WSHandlerConstants.USER, credentials.getUsername());

            String actionProperty = (String)propertyMap.get(WSHandlerConstants.ACTION);
            //Note: the action property can contain a space separated list of multiple actions
            if (actionProperty != null && actionProperty.contains(GTN_USERNAME_TOKEN_IF_AUTHENTICATED))
            {
               if (credentials.getPassword() != null)
               {
                  actionProperty = actionProperty.replace(GTN_USERNAME_TOKEN_IF_AUTHENTICATED, WSHandlerConstants.USERNAME_TOKEN);
               }
               else
               {
                  actionProperty = actionProperty.replace(GTN_USERNAME_TOKEN_IF_AUTHENTICATED, WSHandlerConstants.USERNAME_TOKEN_NO_PASSWORD);
               }
               //replace the old action property with the updated one
               propertyMap.put(WSHandlerConstants.ACTION, actionProperty);
            }
         }
         else // we don't have a logged in user
         {
            //NOTE: ideally we would be removing the 'user' attribute here, but WSS4J has a weird bug
            // where the user has to be specified even if it is not used (ie in the case of a signature or
            // encrypt action where 'signatureUser' or 'encryptionUser' would be used instead.
            propertyMap.put(WSHandlerConstants.USER, GTN_NO_USER);

            //remove the GTN_USERNAME_TOKEN_IF_AUTHENTICATED from the action property
            String actionProperty = (String)propertyMap.get(WSHandlerConstants.ACTION);
            if (actionProperty != null)
            {
               if (actionProperty.contains(GTN_USERNAME_TOKEN_IF_AUTHENTICATED))
               {
                  actionProperty = actionProperty.replace(GTN_USERNAME_TOKEN_IF_AUTHENTICATED, "");
               }
               propertyMap.put(WSHandlerConstants.ACTION, actionProperty);

               //if we don't have any other actions specified, then the only action specified was to use UsernameToken
               //only if we had an authenticated user. Since we don't have an authenticated user, we should not add the WSS4JInterceptor
               //NOTE: we could also set the action to NoSecurity, but its probably best to just not add it.
               if (actionProperty.trim().isEmpty())
               {
                  return false;
               }
            }
         }
      }
      return true;
   }
}

