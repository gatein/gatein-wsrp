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

import org.chromattic.api.annotations.Create;
import org.chromattic.api.annotations.MappedBy;
import org.chromattic.api.annotations.OneToOne;
import org.chromattic.api.annotations.Owner;
import org.chromattic.api.annotations.PrimaryType;
import org.chromattic.api.annotations.Property;
import org.gatein.wsrp.WSRPConstants;
import org.gatein.wsrp.consumer.RegistrationInfo;
import org.gatein.wsrp.consumer.RegistrationProperty;
import org.gatein.wsrp.jcr.mapping.BaseMapping;
import org.gatein.wsrp.registration.RegistrationPropertyDescription;
import org.gatein.wsrp.registration.mapping.RegistrationPropertyDescriptionMapping;

import javax.xml.namespace.QName;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
@PrimaryType(name = RegistrationPropertyMapping.NODE_NAME)
public abstract class RegistrationPropertyMapping implements BaseMapping<RegistrationProperty, RegistrationInfo>
{
   public static final String NODE_NAME = "wsrp:registrationproperty";

   @Property(name = "name")
   public abstract String getName();

   public abstract void setName(String name);

   @Property(name = "value")
   public abstract String getValue();

   public abstract void setValue(String value);

   @OneToOne
   @Owner
   @MappedBy("description")
   public abstract RegistrationPropertyDescriptionMapping getDescription();

   public abstract void setDescription(RegistrationPropertyDescriptionMapping rpdm);

   @Create
   public abstract RegistrationPropertyDescriptionMapping createDescription();

   @Property(name = "status")
   public abstract RegistrationProperty.Status getStatus();

   public abstract void setStatus(RegistrationProperty.Status status);

   public void initFrom(RegistrationProperty property)
   {
      // set properties
      setName(property.getName().toString());
      setStatus(property.getStatus());
      setValue(property.getValue());

      // description
      RegistrationPropertyDescription desc = property.getDescription();
      if (desc != null)
      {
         RegistrationPropertyDescriptionMapping rpdm = createDescription();
         setDescription(rpdm);
         rpdm.initFrom(desc);
      }
   }

   @Override
   public RegistrationProperty toModel(RegistrationProperty initial, RegistrationInfo registrationInfo)
   {
      if (initial == null)
      {
         initial = new RegistrationProperty();
      }

      initial.setName(QName.valueOf(getName()));
      initial.setStatus(getStatus());
      initial.setListener(registrationInfo); // we need to set the listener before we call setValue
      initial.setLang(WSRPConstants.DEFAULT_LOCALE);
      initial.setValue(getValue());

      final RegistrationPropertyDescriptionMapping descriptionMapping = getDescription();
      if (descriptionMapping != null)
      {
         final RegistrationPropertyDescription description = descriptionMapping.toModel(null, null);
         initial.setDescription(description);
      }

      return initial;
   }

   @Override
   public Class<RegistrationProperty> getModelClass()
   {
      return RegistrationProperty.class;
   }
}
