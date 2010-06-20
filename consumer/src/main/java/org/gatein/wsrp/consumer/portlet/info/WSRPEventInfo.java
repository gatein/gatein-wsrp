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

package org.gatein.wsrp.consumer.portlet.info;

import org.gatein.common.i18n.LocalizedString;
import org.gatein.pc.api.info.EventInfo;
import org.gatein.pc.api.info.TypeInfo;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.Collections;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class WSRPEventInfo implements EventInfo
{
   private final QName name;
   private final LocalizedString displayName;
   private final LocalizedString description;
   private final TypeInfo type;
   private final Collection<QName> aliases;

   public WSRPEventInfo(QName name, LocalizedString displayName, LocalizedString description, TypeInfo type, Collection<QName> aliases)
   {
      this.name = name;
      this.displayName = displayName;
      this.description = description;
      this.type = type;
      this.aliases = Collections.unmodifiableCollection(aliases);
   }

   public QName getName()
   {
      return name;
   }

   public LocalizedString getDisplayName()
   {
      return displayName;
   }

   public LocalizedString getDescription()
   {
      return description;
   }

   public TypeInfo getType()
   {
      return type;
   }

   public Collection<QName> getAliases()
   {
      return aliases;
   }
}
