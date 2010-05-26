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

package org.gatein.wsrp.test.protocol.v1.behaviors;

import org.gatein.wsrp.spec.v1.WSRP1ExceptionFactory;
import org.gatein.wsrp.spec.v1.WSRP1TypeFactory;
import org.gatein.wsrp.test.BehaviorRegistry;
import org.gatein.wsrp.test.protocol.v1.MarkupBehavior;
import org.gatein.wsrp.test.protocol.v1.PortletManagementBehavior;
import org.oasis.wsrp.v1.V1AccessDenied;
import org.oasis.wsrp.v1.V1DestroyFailed;
import org.oasis.wsrp.v1.V1Extension;
import org.oasis.wsrp.v1.V1InconsistentParameters;
import org.oasis.wsrp.v1.V1InvalidHandle;
import org.oasis.wsrp.v1.V1InvalidHandleFault;
import org.oasis.wsrp.v1.V1InvalidRegistration;
import org.oasis.wsrp.v1.V1InvalidUserCategory;
import org.oasis.wsrp.v1.V1MissingParameters;
import org.oasis.wsrp.v1.V1OperationFailed;
import org.oasis.wsrp.v1.V1OperationFailedFault;
import org.oasis.wsrp.v1.V1PortletContext;
import org.oasis.wsrp.v1.V1PortletDescription;
import org.oasis.wsrp.v1.V1Property;
import org.oasis.wsrp.v1.V1PropertyList;
import org.oasis.wsrp.v1.V1RegistrationContext;
import org.oasis.wsrp.v1.V1ResetProperty;
import org.oasis.wsrp.v1.V1ResourceList;
import org.oasis.wsrp.v1.V1UserContext;
import org.oasis.wsrp.v2.InvalidHandle;

import javax.jws.WebParam;
import javax.xml.ws.Holder;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 8784 $
 * @since 2.6
 */
public class BasicPortletManagementBehavior extends PortletManagementBehavior
{
   private static final String CLONE_SUFFIX = "_clone";
   public static final String PROPERTY_NAME = "prop1";
   public static final String PROPERTY_VALUE = "value1";
   public static final String PROPERTY_NEW_VALUE = "value2";
   public static final String CLONED_HANDLE = BasicMarkupBehavior.PORTLET_HANDLE + CLONE_SUFFIX;
   private BehaviorRegistry registry;

   public BasicPortletManagementBehavior(BehaviorRegistry registry)
   {
      super();
      this.registry = registry;
   }

   @Override
   public void clonePortlet(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1PortletContext portletContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1UserContext userContext, @WebParam(mode = WebParam.Mode.OUT, name = "portletHandle", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<String> portletHandle, @WebParam(mode = WebParam.Mode.OUT, name = "portletState", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<byte[]> portletState, @WebParam(mode = WebParam.Mode.OUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<V1Extension>> extensions) throws V1MissingParameters, V1InconsistentParameters, V1InvalidHandle, V1InvalidRegistration, V1InvalidUserCategory, V1AccessDenied, V1OperationFailed
   {
      String handle = getHandleFrom(portletContext, "portlet context");

      if (!BasicMarkupBehavior.PORTLET_HANDLE.equals(handle))
      {
         throw WSRP1ExceptionFactory.<V1InvalidHandle, V1InvalidHandleFault>throwWSException(WSRP1ExceptionFactory.INVALID_HANDLE,
            "Can only clone portlet with handle '" + BasicMarkupBehavior.PORTLET_HANDLE + "'", null);
      }

      portletHandle.value = CLONED_HANDLE;
   }

   @Override
   public void getPortletDescription(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1PortletContext portletContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1UserContext userContext, @WebParam(name = "desiredLocales", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> desiredLocales, @WebParam(mode = WebParam.Mode.OUT, name = "portletDescription", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<V1PortletDescription> portletDescription, @WebParam(mode = WebParam.Mode.OUT, name = "resourceList", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<V1ResourceList> resourceList, @WebParam(mode = WebParam.Mode.OUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<V1Extension>> extensions) throws V1MissingParameters, V1InconsistentParameters, V1InvalidHandle, V1InvalidRegistration, V1InvalidUserCategory, V1AccessDenied, V1OperationFailed
   {
      super.getPortletDescription(registrationContext, portletContext, userContext, desiredLocales, portletDescription, resourceList, extensions);

      String handle = getHandleFrom(portletContext, "portlet context");

      // need to fake that the clone exists... so remove suffix to get the description of the POP
      int index = handle.indexOf(CLONE_SUFFIX);
      if (index != -1)
      {
         handle = handle.substring(0, index);
      }

      // get the POP description...
      MarkupBehavior markupBehaviorFor;
      try
      {
         markupBehaviorFor = registry.getMarkupBehaviorFor(handle);
      }
      catch (InvalidHandle invalidHandle)
      {
         throw WSRP1ExceptionFactory.<V1InvalidHandle, V1InvalidHandleFault>throwWSException(WSRP1ExceptionFactory.INVALID_HANDLE,
            "Invalid handle '" + handle + "'", invalidHandle);
      }

      V1PortletDescription description = markupBehaviorFor.getPortletDescriptionFor(handle);

      // if it was a clone, add the suffix back to the handle.
      if (index != -1)
      {
         description.setPortletHandle(handle + CLONE_SUFFIX);
      }

      portletDescription.value = description;
   }

   @Override
   public void getPortletProperties(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1PortletContext portletContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1UserContext userContext, @WebParam(name = "names", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> names, @WebParam(mode = WebParam.Mode.OUT, name = "properties", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<V1Property>> properties, @WebParam(mode = WebParam.Mode.OUT, name = "resetProperties", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<V1ResetProperty>> resetProperties, @WebParam(mode = WebParam.Mode.OUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<V1Extension>> extensions) throws V1MissingParameters, V1InconsistentParameters, V1InvalidHandle, V1InvalidRegistration, V1InvalidUserCategory, V1AccessDenied, V1OperationFailed
   {
      String handle = getHandleFrom(portletContext, "portlet context");

      List<V1Property> propertyList = new ArrayList<V1Property>(1);

      if (BasicMarkupBehavior.PORTLET_HANDLE.equals(handle))
      {
         propertyList.add(WSRP1TypeFactory.createProperty(PROPERTY_NAME, "en", PROPERTY_VALUE));
      }
      else if (CLONED_HANDLE.equals(handle))
      {
         if (callCount != 2)
         {
            propertyList.add(WSRP1TypeFactory.createProperty(PROPERTY_NAME, "en", PROPERTY_VALUE));
         }
         else
         {
            propertyList.add(WSRP1TypeFactory.createProperty(PROPERTY_NAME, "en", PROPERTY_NEW_VALUE));
         }
      }
      else
      {
         WSRP1ExceptionFactory.<V1InvalidHandle, V1InvalidHandleFault>throwWSException(WSRP1ExceptionFactory.INVALID_HANDLE,
            "Unknown handle '" + handle + "'", null);
      }

      incrementCallCount();
      properties.value = propertyList;
   }

   @Override
   public void setPortletProperties(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1RegistrationContext registrationContext, @WebParam(name = "portletContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1PortletContext portletContext, @WebParam(name = "userContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1UserContext userContext, @WebParam(name = "propertyList", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1PropertyList propertyList, @WebParam(mode = WebParam.Mode.OUT, name = "portletHandle", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<String> portletHandle, @WebParam(mode = WebParam.Mode.OUT, name = "portletState", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<byte[]> portletState, @WebParam(mode = WebParam.Mode.OUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<V1Extension>> extensions) throws V1MissingParameters, V1InconsistentParameters, V1InvalidHandle, V1InvalidRegistration, V1InvalidUserCategory, V1AccessDenied, V1OperationFailed
   {
      String handle = getHandleFrom(portletContext, "portlet context");

      if (!(CLONED_HANDLE).equals(handle))
      {
         throw WSRP1ExceptionFactory.<V1OperationFailed, V1OperationFailedFault>throwWSException(WSRP1ExceptionFactory.OPERATION_FAILED,
            "Cannot modify portlet '" + handle + "'", null);
      }

      portletHandle.value = handle;
   }

   private String getHandleFrom(V1PortletContext portletContext, String context) throws V1MissingParameters, V1InvalidHandle
   {
      WSRP1ExceptionFactory.throwMissingParametersIfValueIsMissing(portletContext, "portlet context", context);
      String handle = portletContext.getPortletHandle();
      WSRP1ExceptionFactory.throwMissingParametersIfValueIsMissing(handle, "portlet handle", "PortletContext");
      if (handle.length() == 0)
      {
         throw WSRP1ExceptionFactory.<V1InvalidHandle, V1InvalidHandleFault>throwWSException(WSRP1ExceptionFactory.INVALID_HANDLE,
            "Portlet handle is empty", null);
      }

      return handle;
   }

   @Override
   public void destroyPortlets(@WebParam(name = "registrationContext", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") V1RegistrationContext registrationContext, @WebParam(name = "portletHandles", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") List<String> portletHandles, @WebParam(mode = WebParam.Mode.OUT, name = "destroyFailed", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<V1DestroyFailed>> destroyFailed, @WebParam(mode = WebParam.Mode.OUT, name = "extensions", targetNamespace = "urn:oasis:names:tc:wsrp:v1:types") Holder<List<V1Extension>> extensions) throws V1MissingParameters, V1InconsistentParameters, V1InvalidRegistration, V1OperationFailed
   {
      super.destroyPortlets(registrationContext, portletHandles, destroyFailed, extensions);
      WSRP1ExceptionFactory.throwMissingParametersIfValueIsMissing(portletHandles, "portlet handles", "destroyPortlets");
      if (portletHandles.isEmpty())
      {
         WSRP1ExceptionFactory.throwMissingParametersIfValueIsMissing(portletHandles, "portlet handles", "DestroyPortlets");
      }

      for (String handle : portletHandles)
      {
         if (!CLONED_HANDLE.equals(handle))
         {
            destroyFailed.value.add(WSRP1TypeFactory.createDestroyFailed(handle, "Handle '" + handle + "' doesn't exist"));
         }
      }
   }
}
