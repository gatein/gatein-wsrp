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

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.gatein.common.util.ParameterValidation;
import org.gatein.pc.api.PortletInvokerException;
import org.gatein.pc.api.StateString;
import org.gatein.pc.api.invocation.ActionInvocation;
import org.gatein.pc.api.invocation.PortletInvocation;
import org.gatein.pc.api.invocation.response.ErrorResponse;
import org.gatein.pc.api.invocation.response.HTTPRedirectionResponse;
import org.gatein.pc.api.invocation.response.PortletInvocationResponse;
import org.gatein.pc.api.spi.InstanceContext;
import org.gatein.pc.api.state.AccessMode;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.WSRPUtils;
import org.oasis.wsrp.v2.BlockingInteractionResponse;
import org.oasis.wsrp.v2.Extension;
import org.oasis.wsrp.v2.InteractionParams;
import org.oasis.wsrp.v2.NamedString;
import org.oasis.wsrp.v2.PerformBlockingInteraction;
import org.oasis.wsrp.v2.PortletContext;
import org.oasis.wsrp.v2.RuntimeContext;
import org.oasis.wsrp.v2.UpdateResponse;
import org.oasis.wsrp.v2.UploadContext;
import org.oasis.wsrp.v2.UserContext;

import javax.xml.ws.Holder;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 13121 $
 * @since 2.4 (May 31, 2006)
 */
public class ActionHandler extends NavigationalStateUpdatingHandler
{
   protected ActionHandler(WSRPConsumerImpl consumer)
   {
      super(consumer);
   }

   @SuppressWarnings({"CastToConcreteClass"})
   protected Object prepareRequest(RequestPrecursor requestPrecursor, PortletInvocation invocation)
   {
      if (!(invocation instanceof ActionInvocation))
      {
         throw new IllegalArgumentException("ActionHandler can only handle ActionInvocations!");
      }

      ActionInvocation actionInvocation = (ActionInvocation)invocation;

      PortletContext portletContext = requestPrecursor.getPortletContext();
      if (debug)
      {
         log.debug("Consumer about to attempt action on portlet '" + portletContext.getPortletHandle() + "'");
      }

      // access mode
      InstanceContext instanceContext = invocation.getInstanceContext();
      ParameterValidation.throwIllegalArgExceptionIfNull(instanceContext, "instance context");
      AccessMode accessMode = instanceContext.getAccessMode();
      ParameterValidation.throwIllegalArgExceptionIfNull(accessMode, "access mode");
      if (debug)
      {
         log.debug("Portlet is requesting " + accessMode + " access mode");
      }
      InteractionParams interactionParams = WSRPTypeFactory.createInteractionParams(WSRPUtils.getStateChangeFromAccessMode(accessMode));

      // interaction state
      StateString interactionState = actionInvocation.getInteractionState();
      if (interactionState != null)
      {
         String state = interactionState.getStringValue();
         if (!StateString.JBPNS_PREFIX.equals(state))  // fix-me: see JBPORTAL-900
         {
            interactionParams.setInteractionState(state);
         }
      }

      // check for multi-part
      RequestContextWrapper requestContext = new RequestContextWrapper(actionInvocation.getRequestContext());
      try
      {
         if (FileUpload.isMultipartContent(requestContext))
         {
            // content is multipart, we need to parse it (that includes form parameters)
            FileUpload upload = new FileUpload();
            FileItemIterator iter = upload.getItemIterator(requestContext);
            List<UploadContext> uploadContexts = new ArrayList<UploadContext>(7);
            List<NamedString> formParameters = new ArrayList<NamedString>(7);
            while (iter.hasNext())
            {
               FileItemStream item = iter.next();
               InputStream stream = item.openStream();
               if (!item.isFormField())
               {
                  String contentType = item.getContentType();
                  if (debug)
                  {
                     log.debug("File field " + item.getFieldName() + " with file name " + item.getName() + " and content type "
                        + contentType + " detected.");
                  }

                  BufferedInputStream bufIn = new BufferedInputStream(stream);

                  ByteArrayOutputStream baos = new ByteArrayOutputStream();
                  BufferedOutputStream bos = new BufferedOutputStream(baos);

                  int c = bufIn.read();
                  while (c != -1)
                  {
                     bos.write(c);
                     c = bufIn.read();
                  }

                  bos.flush();
                  baos.flush();
                  bufIn.close();
                  bos.close();

                  UploadContext uploadContext = WSRPTypeFactory.createUploadContext(contentType, baos.toByteArray());

                  List<NamedString> mimeAttributes = new ArrayList<NamedString>(2);

                  String value = FileUpload.FORM_DATA + ";"
                     + " name=\"" + item.getFieldName() + "\";"
                     + " filename=\"" + item.getName() + "\"";
                  NamedString mimeAttribute = WSRPTypeFactory.createNamedString(FileUpload.CONTENT_DISPOSITION, value);
                  mimeAttributes.add(mimeAttribute);

                  mimeAttribute = WSRPTypeFactory.createNamedString(FileUpload.CONTENT_TYPE, item.getContentType());
                  mimeAttributes.add(mimeAttribute);

                  uploadContext.getMimeAttributes().addAll(mimeAttributes);

                  uploadContexts.add(uploadContext);
               }
               else
               {
                  NamedString formParameter = WSRPTypeFactory.createNamedString(item.getFieldName(), Streams.asString(stream));
                  formParameters.add(formParameter);
               }
            }
            interactionParams.getUploadContexts().addAll(uploadContexts);
            interactionParams.getFormParameters().addAll(formParameters);
         }
         else
         {
            // if the content is not multipart, then check for form parameters
            Map<String, String[]> params = actionInvocation.getForm();
            if (params != null && !params.isEmpty())
            {
               int capacity = params.size();
               List<NamedString> formParameters = new ArrayList<NamedString>(capacity);
               for (Map.Entry param : params.entrySet())
               {
                  String name = (String)param.getKey();
                  String[] values = (String[])param.getValue();
                  NamedString formParameter;
                  for (String value : values)
                  {
                     formParameter = WSRPTypeFactory.createNamedString(name, value);
                     formParameters.add(formParameter);
                  }
               }
               interactionParams.getFormParameters().addAll(formParameters);
            }
         }
      }
      catch (Exception e)
      {
         log.debug("Couldn't create UploadContext", e);
      }

      // todo: need to deal with GET method in forms

      if (trace)
      {
         log.trace(WSRPUtils.toString(interactionParams));
      }

      // Create the blocking action request
      return WSRPTypeFactory.createPerformBlockingInteraction(portletContext, requestPrecursor.runtimeContext,
         requestPrecursor.markupParams, interactionParams);
   }

   protected PortletInvocationResponse processResponse(Object response, PortletInvocation invocation, RequestPrecursor requestPrecursor) throws PortletInvokerException
   {
      BlockingInteractionResponse blockingInteractionResponse = (BlockingInteractionResponse)response;

      String redirectURL = blockingInteractionResponse.getRedirectURL();
      UpdateResponse updateResponse = blockingInteractionResponse.getUpdateResponse();
      if (redirectURL != null && updateResponse != null)
      {
         return new ErrorResponse(new IllegalArgumentException("Producer error: response cannot both redirect and update state."));
      }

      if (redirectURL == null && updateResponse == null)
      {
         return new ErrorResponse(new IllegalArgumentException("Producer error: response must redirect or update state."));
      }

      if (redirectURL != null)
      {
         return new HTTPRedirectionResponse(redirectURL); // do we need to process URLs?
      }
      else
      {
         // updateResponse.getMarkupContext(); // ignore bundled markup for now.

         return processUpdateResponse(invocation, requestPrecursor, updateResponse);
      }
   }

   protected void updateUserContext(Object request, UserContext userContext)
   {
      getActionRequest(request).setUserContext(userContext);
   }

   protected void updateRegistrationContext(Object request) throws PortletInvokerException
   {
      getActionRequest(request).setRegistrationContext(consumer.getRegistrationContext());
   }

   protected RuntimeContext getRuntimeContextFrom(Object request)
   {
      return getActionRequest(request).getRuntimeContext();
   }

   protected Object performRequest(Object request) throws Exception
   {
      PerformBlockingInteraction interaction = getActionRequest(request);
      Holder<UpdateResponse> updateResponseHolder = new Holder<UpdateResponse>();
      Holder<String> redirectURL = new Holder<String>();

      // invocation
      if (debug)
      {
         log.debug("performBlockingInteraction on '" + interaction.getPortletContext().getPortletHandle() + "'");
      }
      consumer.getMarkupService().performBlockingInteraction(interaction.getRegistrationContext(),
         interaction.getPortletContext(), interaction.getRuntimeContext(), interaction.getUserContext(),
         interaction.getMarkupParams(), interaction.getInteractionParams(), updateResponseHolder, redirectURL,
         new Holder<List<Extension>>());

      // construct response
      if (redirectURL.value != null)
      {
         return WSRPTypeFactory.createBlockingInteractionResponse(redirectURL.value);
      }
      else
      {
         return WSRPTypeFactory.createBlockingInteractionResponse(updateResponseHolder.value);
      }
   }

   private PerformBlockingInteraction getActionRequest(Object request)
   {
      if (request instanceof PerformBlockingInteraction)
      {
         return (PerformBlockingInteraction)request;
      }

      throw new IllegalArgumentException("ActionHandler: request is not a PerformBlockingInteraction request!");
   }
}
