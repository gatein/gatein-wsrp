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

package org.gatein.wsrp.spec.v1;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.gatein.wsrp.WSRPTypeFactory;
import org.gatein.wsrp.spec.v2.ErrorCodes;
import org.gatein.wsrp.spec.v2.ErrorCodes.Codes;
import org.oasis.wsrp.v1.V1DestroyFailed;
import org.oasis.wsrp.v1.V1InvalidSession;
import org.oasis.wsrp.v1.V1ItemDescription;
import org.oasis.wsrp.v1.V1NamedString;
import org.oasis.wsrp.v1.V1OperationFailed;
import org.oasis.wsrp.v1.V1SessionContext;
import org.oasis.wsrp.v2.FailedPortlets;
import org.oasis.wsrp.v2.ItemDescription;
import org.oasis.wsrp.v2.LocalizedString;
import org.oasis.wsrp.v2.NamedString;
import org.oasis.wsrp.v2.OperationFailed;
import org.oasis.wsrp.v2.SessionContext;

import com.google.common.collect.Lists;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision$
 */
public class V2ToV1ConverterTestCase extends TestCase
{
   public void testException() throws Exception
   {
      Throwable throwable = new Throwable();
      OperationFailed operationFailed = new OperationFailed("foo", WSRPTypeFactory.createOperationFailedFault(), throwable);
      V1OperationFailed v1OperationFailed = V2ToV1Converter.toV1Exception(V1OperationFailed.class, operationFailed);
      assertNotNull(v1OperationFailed);
      assertEquals("foo", v1OperationFailed.getMessage());
      assertEquals(throwable, v1OperationFailed.getCause());
   }

   public void testExceptionMismatch()
   {
      Throwable throwable = new Throwable();
      OperationFailed operationFailed = new OperationFailed("foo", WSRPTypeFactory.createOperationFailedFault(), throwable);

      try
      {
         V2ToV1Converter.toV1Exception(V1InvalidSession.class, operationFailed);
         fail("Should have failed as requested v1 exception doesn't match specified v2");
      }
      catch (IllegalArgumentException e)
      {
         // expected
      }
   }

   public void testExceptionWrongRequestedException()
   {
      Throwable throwable = new Throwable();
      OperationFailed operationFailed = new OperationFailed("foo", WSRPTypeFactory.createOperationFailedFault(), throwable);

      try
      {
         V2ToV1Converter.toV1Exception(IllegalArgumentException.class, operationFailed);
         fail("Should have failed as requested exception is not a WSRP 1 exception class");
      }
      catch (IllegalArgumentException e)
      {
         // expected
      }
   }

   public void testExceptionWrongException()
   {
      try
      {
         V2ToV1Converter.toV1Exception(V1OperationFailed.class, new IllegalArgumentException());
         fail("Should have failed as specified exception is not a WSRP 1 exception");
      }
      catch (IllegalArgumentException e)
      {
         // expected
      }
   }
   
   public void testNamedString()
   {
      NamedString namedString = WSRPTypeFactory.createNamedString("name1", "value1");
      V1NamedString v1NamedString = convertNamedStringToV1NamedString(namedString);
      assertEquals("name1", v1NamedString.getName());
      assertEquals("value1", v1NamedString.getValue());
      
      //value is optional in v2, but not in v1, if a null value is passed the V1NamedString should use the name instead of the value
      namedString = WSRPTypeFactory.createNamedString("name2", null);
      v1NamedString = convertNamedStringToV1NamedString(namedString);
      assertEquals("name2", v1NamedString.getName());
      assertEquals("name2", v1NamedString.getValue());
      
      //TODO; should an empty value be valid?
      namedString = WSRPTypeFactory.createNamedString("name3", "");
      v1NamedString = convertNamedStringToV1NamedString(namedString);
      assertEquals("name3", v1NamedString.getName());
      assertEquals("", v1NamedString.getValue());
   }
   
   private V1NamedString convertNamedStringToV1NamedString(NamedString namedString)
   {
      List<NamedString> namedStringList = new ArrayList<NamedString>();
      namedStringList.add(namedString);
      
      List<V1NamedString> v1NamedStringList = Lists.transform(namedStringList, V2ToV1Converter.NAMEDSTRING);
      
      assertEquals(1, v1NamedStringList.size());
      
      return v1NamedStringList.iterator().next();
   }
   
   public void testSessionContext()
   {
      SessionContext sessionContext = WSRPTypeFactory.createSessionContext("session1234", 0);
      V1SessionContext v1SessionContext = V2ToV1Converter.toV1SessionContext(sessionContext);
      assertEquals("session1234", v1SessionContext.getSessionID());
      assertEquals(0, v1SessionContext.getExpires());
      
      sessionContext = WSRPTypeFactory.createSessionContext(null, 0);
      v1SessionContext = V2ToV1Converter.toV1SessionContext(sessionContext);
      assertNull(v1SessionContext);
      
      sessionContext = WSRPTypeFactory.createSessionContext("", 0);
      v1SessionContext = V2ToV1Converter.toV1SessionContext(sessionContext);
      assertNull(v1SessionContext);
   }
   
   public void testItemDescription()
   {
      V1ItemDescription v1ItemDescription = convertItemDescriptionToV1ItemDescription("desc1", "dn1", "item1");
      assertEquals("desc1", v1ItemDescription.getDescription().getValue());
      assertEquals("item1", v1ItemDescription.getItemName());
      
      v1ItemDescription = convertItemDescriptionToV1ItemDescription(null, "dn1", "item1");
      assertEquals("dn1", v1ItemDescription.getDescription().getValue());
      assertEquals("item1", v1ItemDescription.getItemName());

      v1ItemDescription = convertItemDescriptionToV1ItemDescription("", "dn1", "item1");
      assertEquals("", v1ItemDescription.getDescription().getValue());
      assertEquals("item1", v1ItemDescription.getItemName());
      
      v1ItemDescription = convertItemDescriptionToV1ItemDescription(null, null, "item1");
      assertNotNull(v1ItemDescription.getDescription().getValue());
      assertEquals("item1", v1ItemDescription.getItemName());
   }
   
   private V1ItemDescription convertItemDescriptionToV1ItemDescription (String description, String displayName, String itemName)
   {  
      LocalizedString descriptionLS = null;
      if (description != null)
      {   
         descriptionLS = WSRPTypeFactory.createLocalizedString(description);
      }
      
      LocalizedString displayNameLS = null;
      if (displayName != null)
      {
         displayNameLS = WSRPTypeFactory.createLocalizedString(displayName);
      }
      
      ItemDescription itemDescription = WSRPTypeFactory.createItemDescription(descriptionLS, displayNameLS, itemName);
      
      List<ItemDescription> itemDescriptionList = new ArrayList<ItemDescription>();
      itemDescriptionList.add(itemDescription);
      
      List<V1ItemDescription> v1ItemDesciptionList = Lists.transform(itemDescriptionList, V2ToV1Converter.ITEMDESCRIPTION);
      
      assertEquals(1, v1ItemDesciptionList.size());
      
      return v1ItemDesciptionList.iterator().next();
   }
   
   public void testDestroyedFailed()
   {
      V1DestroyFailed destroyFailed = convertFailedPortletsToDestroyFailed("handle1", Codes.OPERATIONFAILED, "this failed for some reason");
      assertEquals("handle1", destroyFailed.getPortletHandle());
      assertTrue(destroyFailed.getReason().contains(ErrorCodes.getQname(Codes.OPERATIONFAILED).toString()));
      assertTrue(destroyFailed.getReason().contains("this failed for some reason"));
      
      destroyFailed = convertFailedPortletsToDestroyFailed("handle1", Codes.OPERATIONFAILED, null);
      assertEquals("handle1", destroyFailed.getPortletHandle());
      assertEquals(ErrorCodes.getQname(Codes.OPERATIONFAILED).toString(), destroyFailed.getReason());
   }
   
   private V1DestroyFailed convertFailedPortletsToDestroyFailed(String portletHandle, Codes errorCode, String reason)
   {
      List<String> portletHandles = new ArrayList<String>();
      portletHandles.add(portletHandle);
      
      FailedPortlets failedPortlets = WSRPTypeFactory.createFailedPortlets(portletHandles, errorCode, reason);
      List<FailedPortlets> failedPortletsList = new ArrayList<FailedPortlets>();
      failedPortletsList.add(failedPortlets);
      
      List<V1DestroyFailed> destroyFailedList = V2ToV1Converter.toV1DestroyFailed(failedPortletsList);
      
      assertEquals(1, destroyFailedList.size());
      
      return destroyFailedList.iterator().next();
   }

}
