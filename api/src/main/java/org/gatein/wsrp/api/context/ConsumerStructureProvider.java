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

package org.gatein.wsrp.api.context;

import org.gatein.pc.api.PortletContext;

import java.util.List;

/**
 * Provides a way to interact with the structure of the portal in which the WSRP consumer is running. This is needed for the import/export functionality. The structure of a portal
 * is understood to be as follows: several uniquely identifiable pages containing windows, which are supposed to be uniquely identifiable within the context of the containing
 * page. Each window displays a portlet which can be re-assigned so that the window displays the content of the new portlet.
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public interface ConsumerStructureProvider
{
   /**
    * Retrieves the list of page identifiers.
    *
    * @return the list of page identifiers.
    */
   List<String> getPageIdentifiers();

   /**
    * Retrieves the list of identifiers for the windows that are contained within the page identified by the specified identifier.
    *
    * @param pageId the page identifier for which we want to retrieve windows
    * @return the list of identifiers of windows contained within the specified page
    */
   List<String> getWindowIdentifiersFor(String pageId);

   /**
    * Re-assigns the window identified by the specified window identifier in the specified page to display the portlet identified by the specified portlet context, potentially
    * using the specified title. Note that the title is only an indication for the portal to use to display on the window if it is implemented this way.
    *
    * @param portletContext the PortletContext identifying which portlet needs to be displayed
    * @param windowId       the window identifier of the window which will display the new portlet
    * @param pageId         the page identifier of the page which contains the window to re-assign
    * @param portletTitle   a portlet title to potentially use on the window decoration
    */
   void assignPortletToWindow(PortletContext portletContext, String windowId, String pageId, String portletTitle);
}
