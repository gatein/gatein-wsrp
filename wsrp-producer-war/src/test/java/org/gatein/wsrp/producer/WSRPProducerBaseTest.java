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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;

import junit.framework.TestCase;
import org.gatein.common.NotYetImplemented;

/**
 * @author <a href="mailto:boleslaw.dawidowicz@jboss.org">Boleslaw Dawidowicz</a>
 * @version $Revision: 8808 $
 */
public abstract class WSRPProducerBaseTest extends TestCase
{
   protected WSRPProducerImpl producer = WSRPProducerImpl.getInstance();

   protected WSRPProducerBaseTest(String name) throws Exception
   {
      super(name);
   }

   public void deploy(String warFileName) throws Exception
   {
       File archiveDirectory = getDirectory("test.deployables.dir");
       File deployDirectory = getDirectory("jboss.server.home.dir", "deploy");
       File archiveFile = getArchive(warFileName, archiveDirectory, true);
       File deployArchive = getArchive(warFileName, deployDirectory, false);
       
       FileChannel inputChannel = new FileInputStream(archiveFile).getChannel();
       FileChannel outputChannel = new FileOutputStream(deployArchive).getChannel();
       
       inputChannel.transferTo(0, inputChannel.size(), outputChannel);
       
       Thread.currentThread().sleep(10000);
   }

   public void undeploy(String warFileName) throws Exception
   {
      File deployDirectory = getDirectory("jboss.server.home.dir", "deploy");
      File archive = getArchive(warFileName, deployDirectory, true);
      
      archive.delete();
      
      Thread.currentThread().sleep(10000);
   }
   
   private File getDirectory (String property) throws Exception
   {
       return getDirectory(property, null);
   }
    
   private File getDirectory (String property, String subDirectory) throws Exception
   {
       String deployableProperty = System.getProperty(property);
       if (deployableProperty != null)
       {
           if (subDirectory != null)
           {
               deployableProperty += File.separator + subDirectory;
           }
           
           File deployableDir = new File(deployableProperty);
           if (deployableDir.exists() && deployableDir.isDirectory())
           {
               return deployableDir;
           }
           else
           {
               throw new Error("Found a system property for \'" + property + "\' [" + deployableProperty + "] but value does not correspond to a directory.");
           }
       }
       else
       {
           throw new Error ("Could not find the system property \'" + property + "\' cannot deploy test archives.");
       }
   }
   
   
   private File getArchive(String fileName, File deployDirectory, boolean shouldExist) throws Exception
   {
       if (fileName != null && deployDirectory != null && deployDirectory.exists() && deployDirectory.isDirectory())
       {
           File archiveFile = new File(deployDirectory.getAbsoluteFile() + File.separator + fileName);
           return archiveFile;
//         if (archiveFile.exists() && shouldExist)
//         {
//             return archiveFile;
//         }
//         else if (!archiveFile.exists() && !shouldExist)
//         {
//             return archiveFile;
//         }
//         else
//         {
//             //since its not what we are expecting we need to throw the opposite error message
//             String existsString = shouldExist ? "does not exist" : "exists";
//             throw new Exception("Archive " + fileName + " in directory " + deployDirectory + " " + existsString + " which is not expected.");
//         }
       }
       else
       {
           throw new Exception("Cannot find archive to deploy. Archive name [" + fileName + "] is null or the deploy directory + [" + deployDirectory + "] is not a directory");
       }
   }
}
