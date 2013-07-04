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

package org.gatein.registration.policies;

import org.gatein.common.util.ParameterValidation;
import org.gatein.pc.api.PortletContext;
import org.gatein.registration.InvalidConsumerDataException;
import org.gatein.registration.Registration;
import org.gatein.registration.RegistrationException;
import org.gatein.registration.RegistrationManager;
import org.gatein.registration.RegistrationPolicy;
import org.gatein.wsrp.registration.PropertyDescription;

import javax.xml.namespace.QName;
import java.util.Map;

/**
 * Wraps user-provided RegistrationPolicy implementations so that we can decorate their functionality with default behavior such as consumer name sanitation.
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class RegistrationPolicyWrapper implements RegistrationPolicy
{
   private final RegistrationPolicy delegate;

   public static RegistrationPolicy wrap(RegistrationPolicy policy)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(policy, "RegistrationPolicy to wrap");

      if (!policy.isWrapped())
      {
         return new RegistrationPolicyWrapper(policy);
      }
      else
      {
         return policy;
      }
   }

   public static RegistrationPolicy unwrap(RegistrationPolicy policy)
   {
      ParameterValidation.throwIllegalArgExceptionIfNull(policy, "RegistrationPolicy to unwrap");

      if (policy.isWrapped())
      {
         return ((RegistrationPolicyWrapper)policy).getDelegate();
      }
      else
      {
         return policy;
      }
   }

   private RegistrationPolicyWrapper(RegistrationPolicy delegate)
   {
      this.delegate = delegate;
   }

   private RegistrationPolicy getDelegate()
   {
      return delegate;
   }

   public void validateRegistrationDataFor(Map<QName, Object> registrationProperties, String consumerIdentity, final Map<QName, ? extends PropertyDescription> expectations, final RegistrationManager manager)
      throws IllegalArgumentException, RegistrationException
   {
      delegate.validateRegistrationDataFor(registrationProperties, consumerIdentity, expectations, manager);
   }

   public String createRegistrationHandleFor(String registrationId)
      throws IllegalArgumentException
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(registrationId, "Registration id", null);
      return delegate.createRegistrationHandleFor(registrationId);
   }

   public String getAutomaticGroupNameFor(String consumerName)
      throws IllegalArgumentException
   {
      return delegate.getAutomaticGroupNameFor(sanitizeConsumerName(consumerName));
   }

   public String getConsumerIdFrom(String consumerName, Map<QName, Object> registrationProperties)
      throws IllegalArgumentException, InvalidConsumerDataException
   {
      return delegate.getConsumerIdFrom(sanitizeConsumerName(consumerName), registrationProperties);
   }

   public void validateConsumerName(String consumerName, final RegistrationManager manager)
      throws IllegalArgumentException, RegistrationException
   {
      delegate.validateConsumerName(sanitizeConsumerName(consumerName), manager);
   }

   public void validateConsumerGroupName(String groupName, RegistrationManager manager)
      throws IllegalArgumentException, RegistrationException
   {
      delegate.validateConsumerGroupName(groupName, manager);
   }

   public boolean allowAccessTo(PortletContext portletContext, Registration registration, String operation)
   {
      return delegate.allowAccessTo(portletContext, registration, operation);
   }

   public boolean isWrapped()
   {
      return true;
   }

   public String getClassName()
   {
      return delegate.getClassName();
   }

   public Class<? extends RegistrationPolicy> getRealClass()
   {
      return delegate.getClass();
   }

   static String sanitizeConsumerName(String consumerName)
   {
      ParameterValidation.throwIllegalArgExceptionIfNullOrEmpty(consumerName, "Consumer name", null);
      consumerName = consumerName.trim();
      consumerName = consumerName.replaceAll(",", "_");
      consumerName = consumerName.replaceAll(" ", "_");
      consumerName = consumerName.replaceAll("/", "_");

      return consumerName;
   }
}
