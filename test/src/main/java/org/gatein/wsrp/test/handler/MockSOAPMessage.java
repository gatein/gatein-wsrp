/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2006, Red Hat Middleware, LLC, and individual                    *
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

package org.gatein.wsrp.test.handler;

import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

/**
 * @author <a href="mailto:chris.laprun@jboss.com?subject=org.gatein.wsrp.wsrp.handler.MockSOAPMessage">Chris
 *         Laprun</a>
 * @version $Revision: 12277 $
 * @since 2.4
 */
public class MockSOAPMessage extends SOAPMessage
{
   MimeHeaders headers;
   String messageBody;

   public MockSOAPMessage()
   {
      headers = new MimeHeaders();
   }

   public MockSOAPMessage(MimeHeaders headers)
   {
      this.headers = headers;
   }

   public void setMessageBody(String messageBody)
   {
      this.messageBody = messageBody;
   }

   @Override
   public SOAPBody getSOAPBody() throws SOAPException
   {
      try
      {
         return MockSOAPBody.newInstance(messageBody);
      }
      catch (IOException e)
      {
         throw new SOAPException(e);
      }
   }

   public MimeHeaders getMimeHeaders()
   {
      return headers;
   }

   public void setMimeHeaders(MimeHeaders mimeHeaders)
   {
      this.headers = mimeHeaders;
   }

   public void addAttachmentPart(AttachmentPart attachmentPart)
   {
      throw new UnsupportedOperationException();
   }

   public AttachmentPart createAttachmentPart()
   {
      throw new UnsupportedOperationException();
   }

   public String getContentDescription()
   {
      throw new UnsupportedOperationException();
   }

   public void setContentDescription(String string)
   {
      throw new UnsupportedOperationException();
   }

   public SOAPPart getSOAPPart()
   {
      throw new UnsupportedOperationException();
   }

   public void removeAllAttachments()
   {
      throw new UnsupportedOperationException();
   }

   public int countAttachments()
   {
      throw new UnsupportedOperationException();
   }

   public Iterator getAttachments()
   {
      throw new UnsupportedOperationException();
   }

   /*
    * For JDK 1.6
    */
   public AttachmentPart getAttachment(SOAPElement element)
   {
      throw new UnsupportedOperationException();
   }

   public Iterator getAttachments(MimeHeaders mimeHeaders)
   {
      throw new UnsupportedOperationException();
   }

   public void saveChanges() throws SOAPException
   {
      throw new UnsupportedOperationException();
   }

   public boolean saveRequired()
   {
      throw new UnsupportedOperationException();
   }

   public void writeTo(OutputStream outputStream) throws SOAPException, IOException
   {
      throw new UnsupportedOperationException();
   }

   public void removeAttachments(MimeHeaders mimeHeaders)
   {
      throw new UnsupportedOperationException();
   }
}
