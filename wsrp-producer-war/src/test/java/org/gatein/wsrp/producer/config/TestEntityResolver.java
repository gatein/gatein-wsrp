/*
* JBoss, a division of Red Hat
* Copyright 2006, Red Hat Middleware, LLC, and individual contributors as indicated
* by the @authors tag. See the copyright.txt in the distribution for a
* full listing of individual contributors.
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

package org.gatein.wsrp.producer.config;

import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 8784 $
 * @since 2.6
 */
public class TestEntityResolver implements EntityResolver
{
   private static final String CONSUMER = "-//JBoss Portal//DTD WSRP Remote Producer Configuration 2.6//EN";
   private static final String PRODUCER = "-//JBoss Portal//DTD WSRP Local Producer Configuration 2.6//EN";

   public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException
   {
      String dtd;
      if (PRODUCER.equals(publicId))
      {
         dtd = "jboss-wsrp-producer_2_6.dtd";
      }
      else if (CONSUMER.equals(publicId))
      {
         dtd = "jboss-wsrp-consumer_2_6.dtd";
      }
      else
      {
         return null;
      }

      InputStream dtdStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(dtd);
      if (dtdStream != null)
      {
         return new InputSource(dtdStream);
      }

      return null;
   }
}
