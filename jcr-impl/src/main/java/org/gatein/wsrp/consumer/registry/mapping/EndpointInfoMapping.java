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

package org.gatein.wsrp.consumer.registry.mapping;

import org.chromattic.api.RelationshipType;
import org.chromattic.api.annotations.Create;
import org.chromattic.api.annotations.OneToOne;
import org.chromattic.api.annotations.Owner;
import org.chromattic.api.annotations.PrimaryType;
import org.chromattic.api.annotations.Property;
import org.gatein.wsrp.consumer.EndpointConfigurationInfo;
import org.gatein.wsrp.jcr.mapping.mixins.MixinHolder;
import org.gatein.wsrp.jcr.mapping.mixins.WSSEndpointEnabled;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
@PrimaryType(name = EndpointInfoMapping.NODE_NAME)
public abstract class EndpointInfoMapping extends MixinHolder<WSSEndpointEnabled>
{
   public static final String NODE_NAME = "wsrp:endpointinfo";

   @Property(name = "wsdlurl")
   public abstract String getWSDLURL();

   public abstract void setWSDLURL(String wsdlURL);

   @Property(name = "timeout")
   public abstract Integer getWSTimeoutMilliseconds();

   public abstract void setWSTimeoutMilliseconds(Integer expiration);

   @OneToOne(type = RelationshipType.EMBEDDED)
   @Owner
   public abstract WSSEndpointEnabled getWSSEndpointEnabledMixin();

   protected abstract void setWSSEndpointEnabledMixin(WSSEndpointEnabled mixin);

   @Create
   protected abstract WSSEndpointEnabled createWSSEndpointEnabledMixin();

   public void setWSSEnabled(boolean wssEnabled)
   {
      getCreatedMixin().setWSSEnabled(wssEnabled);
   }

   public boolean isWSSEnabled()
   {
      return getCreatedMixin().getWSSEnabled();
   }

   public void initFrom(EndpointConfigurationInfo info)
   {
      setWSDLURL(info.getWsdlDefinitionURL());
      setWSTimeoutMilliseconds(info.getWSOperationTimeOut());
      setWSSEnabled(info.getWSSEnabled());
   }

   EndpointConfigurationInfo toEndpointConfigurationInfo(EndpointConfigurationInfo initial)
   {
      initial.setWsdlDefinitionURL(getWSDLURL());
      initial.setWSOperationTimeOut(getWSTimeoutMilliseconds());
      initial.setWSSEnabled(isWSSEnabled());
      return initial;
   }

   @Override
   public WSSEndpointEnabled getMixin()
   {
      return getWSSEndpointEnabledMixin();
   }

   @Override
   protected void setMixin(WSSEndpointEnabled mixin)
   {
      setWSSEndpointEnabledMixin(mixin);
   }

   @Override
   protected WSSEndpointEnabled createMixin()
   {
      return createWSSEndpointEnabledMixin();
   }
}
