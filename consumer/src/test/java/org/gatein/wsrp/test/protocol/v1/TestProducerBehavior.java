/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2006, Red Hat Middleware, LLC, and individual                    *
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

package org.gatein.wsrp.test.protocol.v1;

import org.gatein.common.net.media.MediaType;
import org.gatein.wsrp.WSRPConstants;
import org.gatein.wsrp.WSRPTypeFactory;
import org.oasis.wsrp.v1.LocalizedString;
import org.oasis.wsrp.v1.MarkupType;
import org.oasis.wsrp.v1.PortletDescription;

/**
 * Provides a base class for Producer behavior used in Consumer testing.
 *
 * @author <a href="mailto:chris.laprun@jboss.com?subject=org.gatein.wsrp.test.TestProducerBehavior">Chris Laprun</a>
 * @version $Revision: 11317 $
 * @since 2.6
 */
public abstract class TestProducerBehavior
{
   protected int callCount;
   public static final String SAMPLE_DESCRIPTION = "SampleDescription";
   public static final String SAMPLE_SHORTTITLE = "SampleShortTitle";
   public static final String SAMPLE_TITLE = "SampleTitle";
   public static final String SAMPLE_DISPLAYNAME = "SampleDisplayName";
   public static final String SAMPLE_KEYWORD = "keyword";

   /**
    * Increment the number of times methods of this behavior have been called. Used when the behavior changes depending
    * on how many times methods have been called.
    */
   public void incrementCallCount()
   {
      callCount++;
   }

   public int getCallCount()
   {
      return callCount;
   }

   public PortletDescription createPortletDescription(String portletHandle, String suffix)
   {
      PortletDescription portletDesc = new PortletDescription();
      portletDesc.setPortletHandle(portletHandle);
      MarkupType markupType = new MarkupType();
      markupType.setMimeType(MediaType.TEXT_HTML.getValue());
      markupType.getModes().add(WSRPConstants.VIEW_MODE);
      markupType.getWindowStates().add(WSRPConstants.NORMAL_WINDOW_STATE);
      markupType.getLocales().addAll(WSRPConstants.getDefaultLocales());
      portletDesc.getMarkupTypes().add(markupType);

      String suffixString = suffix == null ? "" : suffix;
      portletDesc.setDescription(createLocalizedString(SAMPLE_DESCRIPTION + suffixString));
      portletDesc.setTitle(createLocalizedString(SAMPLE_TITLE + suffixString));
      portletDesc.setShortTitle(createLocalizedString(SAMPLE_SHORTTITLE + suffixString));
      portletDesc.setDisplayName(createLocalizedString(SAMPLE_DISPLAYNAME + suffixString));
      portletDesc.getKeywords().add(createLocalizedString(SAMPLE_KEYWORD + suffixString));
      return portletDesc;
   }

   /**
    * Create the dummiest form of LocalizedString
    *
    * @param value
    * @return
    */
   public static LocalizedString createLocalizedString(String value)
   {
      return WSRPTypeFactory.createLocalizedString(value);
   }

   /** Produces String from LocalizedString */
   public static String extractString(org.gatein.common.i18n.LocalizedString ls)
   {
      return ls.getPreferredOrBestLocalizedMappingFor(new String[]{"en"}).getString();
   }
}
