/*
* JBoss, a division of Red Hat
* Copyright 2008, Red Hat Middleware, LLC, and individual contributors as indicated
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

package org.gatein.wsrp.test.protocol.v1.behaviors.interop;

import org.gatein.wsrp.test.protocol.v1.ServiceDescriptionBehavior;
import org.oasis.wsrp.v1.LocalizedString;
import org.oasis.wsrp.v1.PortletDescription;

/**
 * Liferay behavior as exhibited at http://www.jboss.com/index.html?module=bb&op=viewtopic&p=4162201#4162201
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class LiferayServiceDescriptionBehavior extends ServiceDescriptionBehavior
{
   public LiferayServiceDescriptionBehavior()
   {
      /*
        <portletHandle>98</portletHandle>
<markupTypes>
 <mimeType>text/html</mimeType>
 <modes>wsrp:view</modes>
 <windowStates>wsrp:normal</windowStates>
 <windowStates>wsrp:minimized</windowStates>
 <windowStates>wsrp:maximized</windowStates>
 <locales>en_US</locales>
</markupTypes>
<groupID>98</groupID>
<shortTitle lang='English'>
 <value>javax.portlet.short-title.98</value>
</shortTitle>
<title lang='English'>
 <value>Software Catalog</value>
</title>
      */
      PortletDescription pd = createPortletDescription("98", null);
      pd.getMarkupTypes().get(0).getLocales().set(0, "en_US");
      LocalizedString locString = pd.getShortTitle();
      pd.setGroupID("98");
      String lang = "English";
      locString.setLang(lang);
      locString.setValue("javax.portlet.short-title.98");
      locString = pd.getTitle();
      locString.setLang(lang);
      locString.setValue("Software Catalog");

      // add another value that checks that we handle null lang properly
      PortletDescription pd2 = createPortletDescription("99", null);
      pd.getMarkupTypes().get(0).getLocales().set(0, "en_US");
      locString = pd.getShortTitle();
      locString.setLang(null);

      offeredPortlets.add(pd);
      offeredPortlets.add(pd2);
   }
}