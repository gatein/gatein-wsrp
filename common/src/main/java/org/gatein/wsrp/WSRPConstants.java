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

package org.gatein.wsrp;

import org.gatein.common.RuntimeContext;
import org.gatein.pc.portlet.Version;

import javax.xml.namespace.QName;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Random;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 11317 $
 * @since 2.4
 */
public final class WSRPConstants
{
   public static final boolean RUNS_IN_EPP = RuntimeContext.getInstance().isRunningIn(RuntimeContext.RunningEnvironment.epp);

   /** The directory where WSRP services are deployed for automatic detection by {@link ResourceFinder}. */
   public static String SERVICES_DIRECTORY_URL;

   /**
    * The version of the WSRP service. This should match the maven version of the module. Right now, checked via the
    * UpdateWSRPForGatein.sh script.
    */
   public static final String WSRP_SERVICE_VERSION;

   static
   {
      Properties props = new Properties();
      try
      {
         props.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("wsrp.properties"));
      }
      catch (IOException e)
      {
         throw new RuntimeException("Could not load wsrp.properties.");
      }
      WSRP_SERVICE_VERSION = props.getProperty("wsrp.service.version");

      // we should really get the path from a system property instead of hardcoding it
      final String serverPath = System.getProperty("jboss.home.dir");
      if (serverPath != null)
      {
         SERVICES_DIRECTORY_URL = serverPath + "/gatein/deployments";
      }
      else
      {
         SERVICES_DIRECTORY_URL = null;
      }
   }


   // Window State Names ***********************************************************************************************

   /**
    * <p/> The <code>wsrp:normal</code> window state indicates the Portlet is likely sharing the aggregated page with
    * other Portlets. The <code>wsrp:normal</code> window state MAY also indicate that the target device has limited
    * display capabilities. Therefore, a Portlet SHOULD restrict the size of its rendered output in this window state.
    * </p> <p>Conformant Portlets MUST support the <code>wsrp:normal</code> window state.</p>
    */
   public static final String NORMAL_WINDOW_STATE = "wsrp:normal";
   /**
    * When the window state is <code>wsrp:minimized</code>, the Portlet SHOULD NOT render visible markup, but is free to
    * include non-visible data such as JavaScript or hidden forms. The <code>getMarkup</code> operation can be invoked
    * for the <code>wsrp:minimized</code> state just as for all other window states.
    */
   public static final String MINIMIZED_WINDOW_STATE = "wsrp:minimized";
   /**
    * The <code>wsrp:maximized</code> window state is an indication the Portlet is likely the only Portlet being
    * rendered in the aggregated page, or that the Portlet has more space compared to other Portlets in the aggregated
    * page. A Portlet SHOULD generate richer content when its window state is <code>wsrp:maximized</code>.
    */
   public static final String MAXIMIZED_WINDOW_STATE = "wsrp:maximized";
   /**
    * The <code>wsrp:solo</code> window state is an indication the Portlet is the only Portlet being rendered in the
    * aggregated page. A Portlet SHOULD generate richer content when its window state is <code>wsrp:solo</code>.
    */
   public static final String SOLO_WINDOW_STATE = "wsrp:solo";

   // Portlet mode names ***********************************************************************************************

   /**
    * <p>The expected functionality for a Portlet in <code>wsrp:view</code> mode is to render markup reflecting the
    * current state of the Portlet. The <code>wsrp:view</code> mode of a Portlet will include one or more screens that
    * the End-User can navigate and interact with or it may consist of static content devoid of user interactions.</p>
    * <p>The behavior and the generated content of a Portlet in the wsrp:view mode may depend on configuration,
    * personalization and all forms of state.</p> <p>Conformant Portlets MUST support the <code>wsrp:view</code>
    * mode.</p>
    */
   public static final String VIEW_MODE = "wsrp:view";
   /**
    * <p>Within the <code>wsrp:edit</code> mode, a Portlet should provide content and logic that let a user customize
    * the behavior of the Portlet , though such customizations are not limited to screens generated while in this mode.
    * The <code>wsrp:edit</code> mode can include one or more screens which users can navigate to enter their
    * customization data.</p> <p>Typically, Portlets in <code>wsrp:edit</code> mode will set or update Portlet enduring
    * state. How such changes impact Consumer management of Portlet usage by End-Users is discussed in [Section
    * 6.4.3].</p>
    */
   public static final String EDIT_MODE = "wsrp:edit";
   /**
    * <p>When in <code>wsrp:help</code> mode, a Portlet may provide help screens that explains the Portlet and its
    * expected usage. Some Portlets will provide context-sensitive help based on the markup the End-User was viewing
    * when entering this mode.</p>
    */
   public static final String HELP_MODE = "wsrp:help";
   /**
    * In <code>wsrp:preview</code> mode, a Portlet should provide a rendering of its standard <code>wsrp:view</code>
    * mode content, as a visual sample of how this Portlet will appear on the End-User's page with the current
    * configuration. This could be useful for a Consumer that offers an advanced layout capability.
    */
   public static final String PREVIEW_MODE = "wsrp:preview";

   // Standard user authentication values ******************************************************************************

   /** No authentication was done, user information is asserted for informational purposes only. */
   public static final String NONE_USER_AUTHENTICATION = "wsrp:none";
   /** The End-User identified themselves using the common userid/password scenario. */
   public static final String PASSWORD_USER_AUTHENTICATION = "wsrp:password";
   /** The End-User presented a security certificate to validate their identity. */
   public static final String CERTIFICATE_USER_AUTHENTICATION = "wsrp:certificate";

   // WSRP Defaults ****************************************************************************************************

   /** Default character set used to generate markup. */
   public static final String DEFAULT_CHARACTER_SET = "UTF-8";

   // Registration data related ****************************************************************************************

   /** Default consumer name if none has been provided in the WSRP descriptor (*-wsrp.xml) */
   public static final String DEFAULT_CONSUMER_NAME;
   /** The String identifying the portal consumer agent */
   public static final String CONSUMER_AGENT = "GateIn WSRP Consumer." + Version.MAJOR + "." + Version.MINOR;

   static
   {
      InetAddress localhost = null;
      try
      {
         localhost = InetAddress.getLocalHost();
      }
      catch (UnknownHostException e)
      {
         e.printStackTrace();
      }
      if (localhost != null)
      {
         DEFAULT_CONSUMER_NAME = localhost.getCanonicalHostName();
      }
      else
      {
         Random random = new Random(System.currentTimeMillis());
         DEFAULT_CONSUMER_NAME = CONSUMER_AGENT + " Unconfigured " + random.nextInt();
      }
   }

   // User scopes

   /**
    * The markup is specific to the userContext for which it was generated. Changes to the data of the UserContext MUST
    * invalidate the cached markup.
    */
   public static final String CACHE_PER_USER = "wsrp:perUser";

   /** The markup is not specific to the UserContext and therefore may be supplied to all users of the Consumer. */
   public static final String CACHE_FOR_ALL = "wsrp:forAll";

   /** The value of SessionContext.expires if the session will never expire. */
   public static final int SESSION_NEVER_EXPIRES = -1;

   /** A QName representing the string type defined as part of XML Schemas Part 2 */
   public static final QName XSD_STRING = new QName("http://www.w3.org/2001/XMLSchema", "string");

   public static final QName XSD_INTEGER = new QName("http://www.w3.org/2001/XMLSchema", "integer");
   public static final QName XSD_INT = new QName("http://www.w3.org/2001/XMLSchema", "int");
   public static final QName XSD_LONG = new QName("http://www.w3.org/2001/XMLSchema", "long");
   public static final QName XSD_SHORT = new QName("http://www.w3.org/2001/XMLSchema", "short");
   public static final QName XSD_DECIMAL = new QName("http://www.w3.org/2001/XMLSchema", "decimal");
   public static final QName XSD_FLOAT = new QName("http://www.w3.org/2001/XMLSchema", "float");
   public static final QName XSD_DOUBLE = new QName("http://www.w3.org/2001/XMLSchema", "double");
   public static final QName XSD_BOOLEAN = new QName("http://www.w3.org/2001/XMLSchema", "boolean");
   public static final QName XSD_BYTE = new QName("http://www.w3.org/2001/XMLSchema", "byte");
   public static final QName XSD_DATE_TIME = new QName("http://www.w3.org/2001/XMLSchema", "dateTime");
   public static final QName XSD_BASE_64_BINARY = new QName("http://www.w3.org/2001/XMLSchema", "base64Binary");
   public static final QName XSD_HEX_BINARY = new QName("http://www.w3.org/2001/XMLSchema", "hexBinary");
   public static final QName XSD_UNSIGNED_INT = new QName("http://www.w3.org/2001/XMLSchema", "unsignedInt");
   public static final QName XSD_UNSIGNED_SHORT = new QName("http://www.w3.org/2001/XMLSchema", "unsignedShort");
   public static final QName XSD_TIME = new QName("http://www.w3.org/2001/XMLSchema", "time");
   public static final QName XSD_DATE = new QName("http://www.w3.org/2001/XMLSchema", "date");
   public static final QName XSD_ANY_SIMPLE_TYPE = new QName("http://www.w3.org/2001/XMLSchema", "anySimpleType");
   public static final String FROM_WSRP_ATTRIBUTE_NAME = "org.gatein.invocation.fromWSRP";

   private WSRPConstants()
   {
   }

   // init default locale at loading time as it is slow
   public static String DEFAULT_LOCALE = WSRPUtils.toString(Locale.getDefault());

   public static List<String> getDefaultLocales()
   {
      // return copy to prevent modifications by client code
      ArrayList<String> locales = new ArrayList<String>(2);
      locales.add(DEFAULT_LOCALE);
      locales.add("en");
      return locales;
   }

   public static List<String> getDefaultMimeTypes()
   {
      return Collections.singletonList("text/html");
   }
}
