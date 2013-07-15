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
import org.chromattic.api.annotations.DefaultValue;
import org.chromattic.api.annotations.Id;
import org.chromattic.api.annotations.MappedBy;
import org.chromattic.api.annotations.OneToOne;
import org.chromattic.api.annotations.Owner;
import org.chromattic.api.annotations.PrimaryType;
import org.chromattic.api.annotations.Property;
import org.gatein.wsrp.consumer.EndpointConfigurationInfo;
import org.gatein.wsrp.consumer.ProducerInfo;
import org.gatein.wsrp.consumer.RegistrationInfo;
import org.gatein.wsrp.consumer.spi.ConsumerRegistrySPI;
import org.gatein.wsrp.jcr.mapping.BaseMapping;
import org.gatein.wsrp.jcr.mapping.mixins.LastModifiedMixinHolder;
import org.gatein.wsrp.jcr.mapping.mixins.ModifyRegistrationRequired;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
@PrimaryType(name = ProducerInfoMapping.NODE_NAME)
public abstract class ProducerInfoMapping extends LastModifiedMixinHolder implements BaseMapping<ProducerInfo, ConsumerRegistrySPI>
{
   public static final String NODE_NAME = "wsrp:producerinfo";

   @OneToOne
   @Owner
   @MappedBy("endpoint")
   public abstract EndpointInfoMapping getEndpointInfo();

   @OneToOne
   @Owner
   @MappedBy("registration")
   public abstract RegistrationInfoMapping getRegistrationInfo();

   @Property(name = "producerid")
   public abstract String getId();

   public abstract void setId(String id);

   @Property(name = "cache")
   public abstract Integer getExpirationCacheSeconds();

   public abstract void setExpirationCacheSeconds(Integer expiration);

   @Property(name = "active")
   @DefaultValue("false")
   public abstract boolean getActive();

   public abstract void setActive(boolean active);

   @Id
   public abstract String getKey();

   @OneToOne(type = RelationshipType.EMBEDDED)
   @Owner
   public abstract ModifyRegistrationRequired getModifyRegistrationRequiredMixin();

   protected abstract void setModifyRegistrationRequiredMixin(ModifyRegistrationRequired mmr);

   @Create
   protected abstract ModifyRegistrationRequired createModifyRegistrationRequiredMixin();

   /* @Property(name = "available")
public abstract boolean getAvailable();

public abstract void setAvailable(boolean available);*/

   public void setModifyRegistrationRequired(boolean modifyRegistrationRequired)
   {
      getCreatedModifyRegistrationRequiredMixin().setModifyRegistrationRequired(modifyRegistrationRequired);
   }

   public boolean getModifyRegistrationRequired()
   {
      return getCreatedModifyRegistrationRequiredMixin().isModifyRegistrationRequired();
   }

   public void initFrom(ProducerInfo producerInfo)
   {
      setActive(producerInfo.isActive());
      setExpirationCacheSeconds(producerInfo.getExpirationCacheSeconds());
      setId(producerInfo.getId());
      setLastModified(producerInfo.getLastModified());

      EndpointInfoMapping eim = getEndpointInfo();
      eim.initFrom(producerInfo.getEndpointConfigurationInfo());

      RegistrationInfoMapping rim = getRegistrationInfo();
      RegistrationInfo regInfo = producerInfo.getRegistrationInfo();
      rim.initFrom(regInfo);
   }

   public ProducerInfo toModel(ProducerInfo initial, ConsumerRegistrySPI registry)
   {
      // todo: should probably use a ProducerInfo implementation backed by mapping at some point
      ProducerInfo info = initial;
      if (initial == null)
      {
         info = new ProducerInfo(registry);
      }

      // basic properties
      info.setKey(getKey());
      info.setId(getId());
      info.setActive(getActive());
      info.setExpirationCacheSeconds(getExpirationCacheSeconds());
      info.setLastModified(getLastModified());

      // endpoint
      EndpointConfigurationInfo endInfo = getEndpointInfo().toModel(info.getEndpointConfigurationInfo(), info);
      info.setEndpointConfigurationInfo(endInfo);

      // registration
      RegistrationInfo regInfo = getRegistrationInfo().toModel(info.getRegistrationInfo(), info);
      info.setRegistrationInfo(regInfo);

      return info;
   }

   public Class<ProducerInfo> getModelClass()
   {
      return ProducerInfo.class;
   }

   private ModifyRegistrationRequired getCreatedModifyRegistrationRequiredMixin()
   {
      ModifyRegistrationRequired mmr = getModifyRegistrationRequiredMixin();
      if (mmr == null)
      {
         mmr = createModifyRegistrationRequiredMixin();
         setModifyRegistrationRequiredMixin(mmr);
         mmr.initializeValue();
      }
      return mmr;
   }
}
