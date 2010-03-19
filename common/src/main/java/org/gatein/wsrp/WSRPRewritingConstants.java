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

/**
 * <p>This class provides constants used in the context of URL rewriting.</p>
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 8784 $
 * @since 2.4
 */
public final class WSRPRewritingConstants
{

   public static final String WSRP_REWRITE = "wsrp_rewrite";

   public static final String BEGIN_WSRP_REWRITE_END = "?";
   /**
    * 10.2.1 <p>All portlet URLs (i.e. those the Consumer needs to rewrite) are demarcated in the markup by a token
    * (wsrp_rewrite) both at the start (with a "?" appended to clearly delimit the start of the name/value pairs).</p>
    */
   public static final String BEGIN_WSRP_REWRITE = WSRP_REWRITE + BEGIN_WSRP_REWRITE_END;

   public static final int WSRP_REWRITE_PREFIX_LENGTH = 13;

   /**
    * 10.2.1 <p>All portlet URLs (i.e. those the Consumer needs to rewrite) are demarcated in the markup by a token
    * (wsrp_rewrite) at the end (proceeded by a "/" to form the end token) of the URL declaration.</p>
    */
   public static final String END_WSRP_REWRITE = "/" + WSRP_REWRITE;

   public static final int WSRP_REWRITE_SUFFIX_LENGTH = 13;

   /**
    * 10.2.1.1 wsrp-urlType <p>This parameter MUST be specified first when using the Consumer URL rewriting template and
    * the value selected from the following definitions. Well-known portlet URL parameter names that are valid for only
    * one wsrp-urlType are described relative to that wsrp-urlType while the remainder are described later.</p>
    */
   public static final String URL_TYPE_NAME = "wsrp-urlType";

   /**
    * 10.2.1.1.1 wsrp-urlType = blockingAction <p>Activation of the URL will result in an invocation of
    * performBlockingInteraction() on the Portlet that generated the markup. All form parameters, submitted as query
    * string parameters using the HTTP GET method, that are not used to encode parameters defined by this specification
    * MUST be passed to performBlockingInteraction() as formParameters.</p>
    */
   public static final String URL_TYPE_BLOCKING_ACTION = "blockingAction";

   /**
    * 10.2.1.1.2 wsrp-urlType = render <p>Activation of the URL will result in an invocation of getMarkup(). This
    * mechanism permits a Portlet"s markup to contain URLs, which do not involve changes to local state, to avoid the
    * overhead of two-step processing by directly invoking getMarkup(). The URL MAY specify a wsrp-navigationalState
    * portlet URL parameter, whose value the Consumer MUST supply in the navigationalState field of the MarkupParams
    * structure. If there is no such portlet URL parameter, the Consumer MUST NOT supply a value for this field.</p>
    */
   public static final String URL_TYPE_RENDER = "render";

   /**
    * 10.2.1.1.3 wsrp-urlType = resource <p>Activation of the URL will result in the Consumer acting as a gateway to the
    * underlying resource, possibly in a cached manner, and returning it to the user-agent. The URL for the resource
    * (including any query string parameters) is encoded as the value of the wsrp-url parameter. When a portlet URL
    * specifies "resource" for the wsrp-urlType portlet URL parameter, both the wsrp-url and wsrp-requiresRewrite
    * portlet URL parameters MUST also be specified. If the Portlet needs to share data with the referenced resource, it
    * can exploit the cookie support defined in section 10.4.</p>
    */
   public static final String URL_TYPE_RESOURCE = "resource";

   /**
    * 10.2.1.1.3.1 wsrp-url <p>This parameter provides the actual URL to the resource. Note that this needs to be an
    * absolute URL as the resource fetch will have no base for use in fetching a relative URL. Also note that since this
    * resource URL will appear as a parameter value, it has to be strictly encoded (i.e. "&", "=", "/", and "?" need to
    * be url-escaped) so that special URL characters do not invalidate the processing of the enclosing URL. Consumers
    * are encouraged to use the same communication style (e.g. HTTP Get or Post) for retrieving the resource as was used
    * in requesting the resource by the user-agent.</p>
    */
   public static final String RESOURCE_URL = "wsrp-url";

   /**
    * 10.2.1.1.3.2 wsrp-requiresRewrite <p>This boolean informs the Consumer that the resource needs to be parsed for
    * URL rewriting. Normally this means that there are names that will be cross-referenced between the markup and this
    * resource (e.g. JavaScript references). Note that this means the Consumer needs to deal with rewriting unique
    * "namespaced" names in a set of documents, rather than treating each document individually. Processing such
    * resources in a manner that allows caching of the resulting resource by the End-User"s user-agent can improve the
    * performance of the aggregated page for the End-User. In particular, Consumers can process namespace rewriting by
    * using a prefix that is unique to the user/Portlet pair provided any such prefix is held constant for the duration
    * of use within the user"s session with the Consumer of any one Portlet.</p>
    */
   public static final String RESOURCE_REQUIRES_REWRITE = "wsrp-requiresRewrite";

   /**
    * 10.2.1.2 wsrp-navigationalState <p>The value of this portlet URL parameter defines the navigational state the
    * Consumer MUST send to the Producer when the URL is activated. If this parameter is missing, the Consumer MUST NOT
    * supply the navigationalState field of the MarkupParams.</p>
    */
   public static final String NAVIGATIONAL_STATE = "wsrp-navigationalState";

   /**
    * 10.2.1.3 wsrp-interactionState <p>The value of this portlet URL parameter defines the interaction state the
    * Consumer MUST send to the Producer when the URL is activated. Tree.If this parameter is missing, the Consumer MUST
    * NOT supply the interactionState field of the InteractionParams structure.</p>
    */
   public static final String INTERACTION_STATE = "wsrp-interactionState";

   /**
    * 10.2.1.4 wsrp-mode <p>Activating this URL includes a request to change the mode parameter in MarkupParams into the
    * mode specified as the value for this portlet URL parameter. The value for wsrp-mode MUST be one of the modes
    * detailed in section 6.8 or a custom mode the Consumer specified as supported during registration. The wsrp-mode
    * portlet URL parameter MAY be used whenever the wsrp-urlType portlet URL parameter has a value of "blockingAction"
    * or "render".</p>
    */
   public static final String MODE = "wsrp-mode";

   /**
    * 10.2.1.5 wsrp-windowState <p>Activating this URL includes a request to change the windowState parameter in
    * MarkupParams into the window state specified as the value for this portlet URL parameter. The value for wsrp-
    * windowState MUST be one of the values detailed in section 6.9 or a custom window state the Consumer specified as
    * supported during registration. The wsrp-windowState portlet URL parameter MAY be used whenever the wsrp-urlType
    * portlet URL parameter has a value of "blockingAction"  or "render".</p>
    */
   public static final String WINDOW_STATE = "wsrp-windowState";

   /**
    * 10.2.1.6 wsrp-fragmentID <p>This portlet URL parameter specifies the portion of an URL that navigates to a place
    * within a document.</p>
    */
   public static final String FRAGMENT_ID = "wsrp-fragmentID";

   /**
    * 10.2.1.7 wsrp-secureURL <p>The value for the wsrp-secureURL is a boolean indicating whether the resulting URL MUST
    * involve secure communication between the client and Consumer, as well as between the Consumer and Producer. The
    * default value of this boolean is "false". Note that the Consumer"s aggregated page MUST be secure if any of the
    * Portlets whose content is being displayed on the page have indicated the need for secure communication for their
    * current markup.</p>
    */
   public static final String SECURE_URL = "wsrp-secureURL";

   public static final String WSRP_REWRITE_TOKEN_END = "_";
   /**
    * 10.3.1 Consumer Rewriting (Namespace encoding) <p>The Portlet can prefix the token with "wsrp_rewrite_". The
    * Consumer will locate such markers and MUST replace them with a prefix that is unique to this instance of this
    * portlet on the page. This prefix has been chosen such that the Consumer is able to do a single parse of the markup
    * to both locate such markers and the URL rewrite expressions described in section 10.2.1. In addition, this prefix
    * is legal for at least the JavaScript and VBScript scripting languages and CSS class names. This permits the
    * independent testing of most generated markup fragments.</p>
    */
   public static final String WSRP_REWRITE_TOKEN = WSRP_REWRITE + WSRP_REWRITE_TOKEN_END;

   /** Opening token for URL parameters. See 10.2.2. */
   public static final String REWRITE_PARAMETER_OPEN = "{";

   /** Closing token for URL parameters. See 10.2.2. */
   public static final String REWRITE_PARAMETER_CLOSE = "}";

   /** Encoded version of REWRITE_PARAMETER_OPEN */
   static final String ENC_OPEN = "%7B";

   /** Encoded version of REWRITE_PARAMETER_CLOSE */
   static final String ENC_CLOSE = "%7D";

   /* Constants for Resource URL processing todo: remove? */
   public static final String RESOURCE_URL_DELIMITER = "*";
   public static final String FAKE_RESOURCE_START = WSRP_REWRITE + RESOURCE_URL_DELIMITER;
   public static final String FAKE_RESOURCE_REQ_REW = RESOURCE_URL_DELIMITER;
   public static final String WSRP_URL = REWRITE_PARAMETER_OPEN + RESOURCE_URL + REWRITE_PARAMETER_CLOSE;
   public static final String WSRP_REQUIRES_REWRITE = REWRITE_PARAMETER_OPEN + RESOURCE_REQUIRES_REWRITE + REWRITE_PARAMETER_CLOSE;
   public static final String FAKE_RESOURCE_URL = FAKE_RESOURCE_START + WSRP_URL + RESOURCE_URL_DELIMITER +
      WSRP_REQUIRES_REWRITE + END_WSRP_REWRITE;
   public static final String GTNRESOURCE = "gtnresource";

   private WSRPRewritingConstants()
   {
   }
}
