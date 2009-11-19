/******************************************************************************
 * JBoss, a division of Red Hat                                               *
 * Copyright 2007, Red Hat Middleware, LLC, and individual                    *
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

package org.gatein.wsrp.registration;

import junit.framework.TestCase;
import org.gatein.wsrp.WSRPConstants;

import javax.xml.namespace.QName;
import java.util.Locale;

/**
 * @author <a href="mailto:chris.laprun@jboss.com">Chris Laprun</a>
 * @version $Revision: 12017 $
 * @since 2.6
 */
public class RegistrationPropertyDescriptionTestCase extends TestCase
{
   private RegistrationPropertyDescription desc;

   protected void setUp() throws Exception
   {
      desc = new RegistrationPropertyDescription("foo", WSRPConstants.XSD_STRING);
   }

   public void testNotify()
   {
      TestParent parent = new TestParent();
      desc.setValueChangeListener(parent);
      desc.setDefaultHint(null);
      assertFalse(parent.notifyCalled);
      parent.resetNotifyCalled();
      desc.setDefaultHint("hint");
      assertTrue(parent.notifyCalled);
   }

   public void testChangingNameUpdatesParent()
   {
      TestParent parent = new TestParent();
      desc.setValueChangeListener(parent);
      assertNotNull(parent.getRegistrationPropertyWith("foo"));

      desc.setName(QName.valueOf("bar"));
      assertEquals(desc, parent.getRegistrationPropertyWith("bar"));
      assertNull(parent.getRegistrationPropertyWith("foo"));
   }

   public void testModifyIfNeeded()
   {
      String oldValue = "old";
      String newValue = "new";
      assertEquals(oldValue, desc.modifyIfNeeded(oldValue, oldValue));
      assertEquals(newValue, desc.modifyIfNeeded(oldValue, newValue));
      assertEquals(null, desc.modifyIfNeeded(null, null));
      assertEquals(newValue, desc.modifyIfNeeded(null, newValue));
      assertEquals(null, desc.modifyIfNeeded(oldValue, null));
   }

   public void testGetLang()
   {
      assertEquals(Locale.getDefault(), desc.getLang());
      desc.setLabel(new LocalizedString("etiquette", Locale.FRENCH));
      assertEquals(Locale.FRENCH, desc.getLang());
   }

   class TestParent implements ValueChangeListener
   {
      private boolean notifyCalled;

      // prop name fakes the existence of a property as only one property exists for the tests
      private String propName = "foo";

      void resetNotifyCalled()
      {
         notifyCalled = false;
      }

      public RegistrationPropertyDescription getRegistrationPropertyWith(String name)
      {
         // return desc only if it matches the name we know about (used to fake property name updates)
         if (propName.equals(name))
         {
            return desc;
         }

         return null;
      }

      public void valueHasChanged(RegistrationPropertyDescription originating, Object oldValue, Object newValue, boolean isName)
      {
         if (isName)
         {
            // fake updating the property map if the old name was foo...
            if ("foo".equals(oldValue))
            {
               // then set the prop name to the new property name
               propName = originating.getName().getLocalPart();
            }

            notifyCalled = true;
         }
      }
   }
}
