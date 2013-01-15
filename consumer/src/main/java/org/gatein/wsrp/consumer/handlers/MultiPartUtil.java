/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2012, Red Hat Middleware, LLC, and individual                    *
 * contributors as indicated by the @authors tag. See the                     *
 * copyright.txt in the distribution for a full listing of                    *
 * individual contributors.                                                   *
 *                                                                            *
 * This is free software; you can redistribute it and/or modify it            *
 * under the terms of the GNU Lesser General Public License as                *
 * published by the Free Software Foundation; either version 2.1 of           *
 * the License, or (at your option) any later version.                        *
 *                                                                            *
 * This software is distributed in the hope that it will be useful,           *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of             *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU           *
 * Lesser General Public License for more details.                            *
 *                                                                            *
 * You should have received a copy of the GNU Lesser General Public           *
 * License along with this software; if not, write to the Free                *
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA         *
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.                   *
 ******************************************************************************/
package org.gatein.wsrp.consumer.handlers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUpload;
import org.apache.commons.fileupload.util.Streams;
import org.gatein.pc.api.spi.RequestContext;
import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.consumer.handlers.ActionHandler.RequestContextWrapper;
import org.oasis.wsrp.v2.NamedString;
import org.oasis.wsrp.v2.UploadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class MultiPartUtil
{
   protected static Logger log = LoggerFactory.getLogger(MultiPartUtil.class);

   public static MultiPartResult getMultiPartContent(RequestContext requestContext)
   {
      RequestContextWrapper requestContextWrapper = new RequestContextWrapper(requestContext);
      MultiPartResult result = null;

      try
      {
         if (FileUpload.isMultipartContent(requestContextWrapper))
         {
            result = new MultiPartResult();
            // content is multipart, we need to parse it (that includes form parameters)
            FileUpload upload = new FileUpload();
            FileItemIterator iter = upload.getItemIterator(requestContextWrapper);
            List<UploadContext> uploadContexts = new ArrayList<UploadContext>(7);
            List<NamedString> formParameters = new ArrayList<NamedString>(7);
            while (iter.hasNext())
            {
               FileItemStream item = iter.next();
               InputStream stream = item.openStream();
               if (!item.isFormField())
               {
                  String contentType = item.getContentType();
                  if (log.isDebugEnabled())
                  {
                     log.debug("File field " + item.getFieldName() + " with file name " + item.getName()
                        + " and content type " + contentType + " detected.");
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

                  final byte[] uploadData = baos.toByteArray();
                  if (uploadData.length != 0)
                  {
                     UploadContext uploadContext = WSRPTypeFactory.createUploadContext(contentType, uploadData);

                     List<NamedString> mimeAttributes = new ArrayList<NamedString>(2);

                     String value = FileUpload.FORM_DATA + ";" + " name=\"" + item.getFieldName() + "\";" + " filename=\"" + item.getName() + "\"";
                     NamedString mimeAttribute = WSRPTypeFactory.createNamedString(FileUpload.CONTENT_DISPOSITION, value);
                     mimeAttributes.add(mimeAttribute);

                     mimeAttribute = WSRPTypeFactory.createNamedString(FileUpload.CONTENT_TYPE, item.getContentType());
                     mimeAttributes.add(mimeAttribute);

                     uploadContext.getMimeAttributes().addAll(mimeAttributes);

                     uploadContexts.add(uploadContext);
                  }
                  else
                  {
                     log.debug("Ignoring empty file " + item.getName());
                  }
               }
               else
               {
                  NamedString formParameter = WSRPTypeFactory.createNamedString(item.getFieldName(), Streams.asString(stream));
                  formParameters.add(formParameter);
               }
            }

            result.getUploadContexts().addAll(uploadContexts);
            result.getFormParameters().addAll(formParameters);
         }
      }
      catch (Exception e)
      {
         log.debug("Couldn't create UploadContext", e);
      }
      return result;
   }

   static class MultiPartResult
   {
      protected List<NamedString> formParameters;
      protected List<UploadContext> uploadContexts;

      public List<NamedString> getFormParameters()
      {
         if (formParameters == null)
         {
            formParameters = new ArrayList<NamedString>();
         }
         return formParameters;
      }

      public List<UploadContext> getUploadContexts()
      {
         if (uploadContexts == null)
         {
            uploadContexts = new ArrayList<UploadContext>();
         }
         return uploadContexts;
      }
   }

}
