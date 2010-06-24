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
import org.gatein.pc.api.OpaqueStateString;
import org.gatein.pc.api.PortletInvokerException;
import org.gatein.pc.api.StateEvent;
import org.gatein.pc.api.StateString;
import org.gatein.pc.api.invocation.ActionInvocation;
import org.gatein.pc.api.invocation.PortletInvocation;
import org.gatein.pc.api.invocation.response.ErrorResponse;
import org.gatein.pc.api.invocation.response.HTTPRedirectionResponse;
import org.gatein.pc.api.invocation.response.PortletInvocationResponse;
import org.gatein.pc.api.invocation.response.UpdateNavigationalStateResponse;
import org.gatein.pc.api.spi.InstanceContext;
import org.gatein.pc.api.state.AccessMode;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.WSRPUtils;
import org.oasis.wsrp.v2.BlockingInteractionResponse;
import org.oasis.wsrp.v2.Extension;
import org.oasis.wsrp.v2.InteractionParams;
import org.oasis.wsrp.v2.NamedString;
import org.oasis.wsrp.v2.NavigationalContext;
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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 13121 $
 * @since 2.4 (May 31, 2006)
 */
public class ActionHandler extends InvocationHandler
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
      log.debug("Consumer about to attempt action on portlet '" + portletContext.getPortletHandle() + "'");

      // access mode
      InstanceContext instanceContext = invocation.getInstanceContext();
      ParameterValidation.throwIllegalArgExceptionIfNull(instanceContext, "instance context");
      AccessMode accessMode = instanceContext.getAccessMode();
      ParameterValidation.throwIllegalArgExceptionIfNull(accessMode, "access mode");
      log.debug("Portlet is requesting " + accessMode + " access mode");
      InteractionParams interactionParams =
         WSRPTypeFactory.createInteractionParams(WSRPUtils.getStateChangeFromAccessMode(accessMode));

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
                  log.debug("File field " + item.getFieldName() + " with file name " + item.getName() + " and content type "
                     + contentType + " detected.");
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

                  NamedString mimeAttribute = new NamedString();
                  mimeAttribute.setName(FileUpload.CONTENT_DISPOSITION);
                  mimeAttribute.setValue(FileUpload.FORM_DATA + ";"
                     + " name=\"" + item.getFieldName() + "\";"
                     + " filename=\"" + item.getName() + "\"");
                  mimeAttributes.add(mimeAttribute);

                  mimeAttribute = new NamedString();
                  mimeAttribute.setName(FileUpload.CONTENT_TYPE);
                  mimeAttribute.setValue(item.getContentType());
                  mimeAttributes.add(mimeAttribute);

                  uploadContext.getMimeAttributes().addAll(mimeAttributes);

                  uploadContexts.add(uploadContext);
               }
               else
               {
                  NamedString formParameter = new NamedString();
                  formParameter.setName(item.getFieldName());
                  formParameter.setValue(Streams.asString(stream));
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
                     formParameter = new NamedString();
                     formParameter.setName(name);
                     formParameter.setValue(value);
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

      log.debug(WSRPUtils.toString(interactionParams));

      // Create the blocking action request
      return WSRPTypeFactory.createPerformBlockingInteraction(portletContext, requestPrecursor.runtimeContext,
         requestPrecursor.markupParams, interactionParams);
   }

   protected PortletInvocationResponse processResponse(Object response, PortletInvocation invocation, RequestPrecursor requestPrecursor) throws PortletInvokerException
   {
      BlockingInteractionResponse blockingInteractionResponse = (BlockingInteractionResponse)response;
      log.debug("Starting processing response");

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

         UpdateNavigationalStateResponse result = new UpdateNavigationalStateResponse();
         // new mode
         String newMode = updateResponse.getNewMode();
         if (newMode != null)
         {
            result.setMode(WSRPUtils.getJSR168PortletModeFromWSRPName(newMode));
         }
         // new window state
         String newWindowState = updateResponse.getNewWindowState();
         if (newWindowState != null)
         {
            result.setWindowState(WSRPUtils.getJSR168WindowStateFromWSRPName(newWindowState));
         }
         // navigational state
         NavigationalContext navigationalContext = updateResponse.getNavigationalContext();
         if (navigationalContext != null)
         {
            String navigationalState = navigationalContext.getOpaqueValue();
            if (navigationalState != null) // todo: check meaning of empty private NS
            {
               result.setNavigationalState(new OpaqueStateString(navigationalState));
            }

            // todo: public NS GTNWSRP-38
         }

         // check if the portlet was cloned
         PortletContext portletContext = updateResponse.getPortletContext();
         if (portletContext != null)
         {
            PortletContext originalContext = requestPrecursor.getPortletContext();
            InstanceContext context = invocation.getInstanceContext();

            String handle = portletContext.getPortletHandle();
            if (!originalContext.getPortletHandle().equals(handle))
            {
               // todo: GTNWSRP-36 If the Producer returns a new portletHandle without returning a new sessionID, the Consumer MUST
               // associate the current sessionID with the new portletHandle rather than the previous portletHandle.
               log.debug("Portlet '" + requestPrecursor.getPortletHandle() + "' was implicitely cloned. New handle is '"
                  + handle + "'");
               StateEvent event = new StateEvent(WSRPUtils.convertToPortalPortletContext(portletContext), StateEvent.Type.PORTLET_CLONED_EVENT);
               context.onStateEvent(event);
            }
            else
            {
               // check if the state was modified
               byte[] originalState = originalContext.getPortletState();
               byte[] newState = portletContext.getPortletState();
               if (!Arrays.equals(originalState, newState))
               {
                  StateEvent event = new StateEvent(WSRPUtils.convertToPortalPortletContext(portletContext), StateEvent.Type.PORTLET_MODIFIED_EVENT);
                  context.onStateEvent(event);
               }
            }

            // update the session information associated with the portlet handle
            consumer.getSessionHandler().updateSessionInfoFor(originalContext.getPortletHandle(), handle, invocation);
         }
         else
         {
            portletContext = requestPrecursor.getPortletContext();
         }

         // update the session info, using either the original or cloned portlet context, as appropriate
         consumer.getSessionHandler().updateSessionIfNeeded(updateResponse.getSessionContext(), invocation,
            portletContext.getPortletHandle());

         log.debug("Response processed");
         return result;
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
      log.debug("performBlockingInteraction on '" + interaction.getPortletContext().getPortletHandle() + "'");
      consumer.getMarkupService().performBlockingInteraction(interaction.getRegistrationContext(),
         interaction.getPortletContext(), interaction.getRuntimeContext(), interaction.getUserContext(),
         interaction.getMarkupParams(), interaction.getInteractionParams(), updateResponseHolder, redirectURL,
         new Holder<List<Extension>>());

      // construct response
      BlockingInteractionResponse response = new BlockingInteractionResponse();
      response.setRedirectURL(redirectURL.value);
      response.setUpdateResponse(updateResponseHolder.value);
      return response;
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
