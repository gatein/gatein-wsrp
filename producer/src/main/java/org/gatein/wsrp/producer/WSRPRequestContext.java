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

package org.gatein.wsrp.producer;

import org.apache.commons.fileupload.FileUpload;
import org.gatein.common.util.ParameterMap;
import org.gatein.pc.api.spi.RequestContext;
import org.oasis.wsrp.v2.InteractionParams;
import org.oasis.wsrp.v2.NamedString;
import org.oasis.wsrp.v2.ResourceParams;
import org.oasis.wsrp.v2.UploadContext;

import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:julien@jboss.org">Julien Viet</a>
 * @version $Revision: 10337 $
 */
abstract class WSRPRequestContext implements RequestContext, org.apache.commons.fileupload.RequestContext
{
   protected String characterEncoding;

   protected WSRPRequestContext(String characterEncoding)
   {
      this.characterEncoding = characterEncoding;
   }

   public String getCharacterEncoding()
   {
      return characterEncoding;
   }

   public int getContentLength()
   {
      throw new UnsupportedOperationException("Not currently supported");
   }

   public BufferedReader getReader() throws IOException
   {
      throw new UnsupportedOperationException("Not currently supported");
   }

   public InputStream getInputStream() throws IOException
   {
      throw new UnsupportedOperationException("Not currently supported");
   }

   public abstract ParameterMap getForm();

   static class WSRPSimpleRequestContext extends WSRPRequestContext
   {
      private ParameterMap formParameters;
      private String contentType;

      protected WSRPSimpleRequestContext(String characterEncoding, String contentType, List<NamedString> formParams)
      {
         super(characterEncoding);
         this.contentType = contentType;

         if (formParams != null && !formParams.isEmpty())
         {
            Map<String, String[]> params = new HashMap<String, String[]>(formParams.size());
            for (NamedString formParam : formParams)
            {
               String paramName = formParam.getName();
               String paramValue = formParam.getValue();
               if (params.containsKey(paramName))
               {
                  // handle multi-valued parameters...
                  String[] oldValues = params.get(paramName);
                  int valuesLength = oldValues.length;
                  String[] newValues = new String[valuesLength + 1];
                  System.arraycopy(oldValues, 0, newValues, 0, valuesLength);
                  newValues[valuesLength] = paramValue;
                  params.put(paramName, newValues);
               }
               else
               {
                  params.put(paramName, new String[]{paramValue});
               }
               formParameters = new ParameterMap(params);
            }
         }
         else
         {
            formParameters = new ParameterMap();
         }

      }

      public ParameterMap getForm()
      {
         return formParameters;
      }

      public String getContentType()
      {
         return contentType;
      }
   }

   static class WSRPMultiRequestContext extends WSRPRequestContext
   {
      private byte[] content;
      private boolean usingStream;
      private boolean usingReader;
      private String contentType;

      protected WSRPMultiRequestContext(String characterEncoding, List<NamedString> formParams, List<UploadContext> uploadContexts) throws IOException, MessagingException
      {
         super(characterEncoding);

         MimeMultipart parts = new MimeMultipart();
         if (uploadContexts != null && !uploadContexts.isEmpty())
         {
            for (UploadContext uploadContext : uploadContexts)
            {
               InternetHeaders headers = new InternetHeaders();
               headers.addHeader(FileUpload.CONTENT_TYPE, uploadContext.getMimeType());

               List<NamedString> attributes = uploadContext.getMimeAttributes();
               if (attributes != null && !attributes.isEmpty())
               {
                  for (NamedString attribute : attributes)
                  {
                     headers.addHeader(attribute.getName(), attribute.getValue());
                  }
               }

               MimeBodyPart mimeBodyPart = new MimeBodyPart(headers, uploadContext.getUploadData());
               parts.addBodyPart(mimeBodyPart);
            }
         }

         final String paramContentDispositionHeader = FileUpload.FORM_DATA + "; name=\"";
         if (formParams != null)
         {
            for (NamedString formParam : formParams)
            {
               InternetHeaders headers = new InternetHeaders();

               StringBuffer paramContentDisposition = new StringBuffer(paramContentDispositionHeader);
               paramContentDisposition.append(formParam.getName()).append("\"");

               headers.addHeader(FileUpload.CONTENT_DISPOSITION, paramContentDisposition.toString());

               MimeBodyPart mimeBodyPart = new MimeBodyPart(headers, formParam.getValue().getBytes());
               parts.addBodyPart(mimeBodyPart);
            }
         }

         ByteArrayOutputStream baos = new ByteArrayOutputStream();
         parts.writeTo(baos);
         content = baos.toByteArray();
         contentType = parts.getContentType();
      }

      public ParameterMap getForm()
      {
         return new ParameterMap();
      }

      public String getContentType()
      {
         return contentType;
      }

      public int getContentLength()
      {
         return content.length;
      }

      public BufferedReader getReader() throws IOException
      {
         if (usingStream)
         {
            throw new IllegalStateException("getInputStream has already been called on this ActionContext!");
         }
         usingReader = true;
         return new BufferedReader(new InputStreamReader(getInputStreamFromContent()));
      }

      public InputStream getInputStream() throws IOException
      {
         if (usingReader)
         {
            throw new IllegalStateException("getReader has already been called on this ActionContext!");
         }
         usingStream = true;
         return getInputStreamFromContent();
      }


      private InputStream getInputStreamFromContent()
      {
         return new ByteArrayInputStream(content);
      }
   }

   public static WSRPRequestContext createRequestContext(MarkupRequest markupRequest, InteractionParams interactionParams)
   {
      return createRequestContext(markupRequest, interactionParams.getFormParameters(), interactionParams.getUploadContexts());
   }
   
   public static WSRPRequestContext createRequestContext(MarkupRequest markupRequest, ResourceParams resourceParams)
   {
      return createRequestContext(markupRequest, resourceParams.getFormParameters(), resourceParams.getUploadContexts());
   }
   
   public static WSRPRequestContext createRequestContext(MarkupRequest markupRequest, List<NamedString> formParams, List<UploadContext> uploadContexts)
   {
      if (uploadContexts != null && !uploadContexts.isEmpty())
      {
         try
         {
            return new WSRPMultiRequestContext(markupRequest.getCharacterSet(), formParams, uploadContexts);
         }
         catch (Exception e)
         {
            throw new IllegalArgumentException("Invalid upload contexts", e);
         }
      }
      else
      {
         return new WSRPSimpleRequestContext(markupRequest.getCharacterSet(), markupRequest.getMediaType(), formParams);

      }
   }
}
