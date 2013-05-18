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

package org.gatein.wsrp.consumer;

import org.oasis.wsrp.v2.ServiceDescription;

import static org.gatein.wsrp.consumer.RefreshResult.Status.*;

/**
 * A detailed status of what happened during a refresh of the producer metadata.
 *
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 11575 $
 * @since 2.6
 */
public class RefreshResult
{
   public enum Status
   {
      SUCCESS, FAILURE, UNAVAILABLE, BYPASSED, UNKNOWN, MODIFY_REGISTRATION_REQUIRED
   }

   private ServiceDescription serviceDescription;
   private Status status;
   private RefreshResult registrationResult;

   /** A RefreshResult is assumed successful unless proven otherwise. Same as RefreshResult(SUCCESS). */
   public RefreshResult()
   {
      this(SUCCESS);
   }

   public RefreshResult(Status status)
   {
      this.status = status;
   }

   public RefreshResult getRegistrationResult()
   {
      return registrationResult;
   }

   public void setRegistrationResult(RefreshResult registrationResult)
   {
      if (registrationResult != null)
      {
         this.registrationResult = registrationResult;

         // result of registration only impacts the result of the total refresh if it wasn't bypassed
         RefreshResult.Status regStatus = registrationResult.getStatus();
         if (!BYPASSED.equals(regStatus))
         {
            status = regStatus;
         }
      }
   }

   public boolean didRefreshHappen()
   {
      return !BYPASSED.equals(status) && !FAILURE.equals(status);
   }

   public boolean hasIssues()
   {
      return !(SUCCESS.equals(status) || BYPASSED.equals(status));
   }

   public Status getStatus()
   {
      return status;
   }

   public void setStatus(Status status)
   {
      this.status = status;
   }

   public void setServiceDescription(ServiceDescription serviceDescription)
   {
      this.serviceDescription = serviceDescription;
   }

   public ServiceDescription getServiceDescription()
   {
      return serviceDescription;
   }
}
