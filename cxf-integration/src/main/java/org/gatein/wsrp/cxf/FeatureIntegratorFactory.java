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
package org.gatein.wsrp.cxf;

import org.apache.cxf.feature.AbstractFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * The role of this class is to provide a means to add a list of features to wsrp-service classes at
 * runtime since cxf lacks a means of externally configuring these classes outside of using spring.
 * <p/>
 * This class with look for a specific property file located in the configuration directory with a list
 * of feature classes. It will then use reflection to create these classes. The WSRPEndpointFeature will
 * call these classes, if any are configured, when ever its own feature methods are called.
 *
 * @author <a href="mailto:mwringe@redhat.com">Matt Wringe</a>
 * @version $Revision$
 */
public class FeatureIntegratorFactory
{
   private static Logger log = LoggerFactory.getLogger(FeatureIntegratorFactory.class);

   protected static final FeatureIntegratorFactory instance = new FeatureIntegratorFactory();

   protected List<AbstractFeature> features = new ArrayList<AbstractFeature>();

   public static final String DEFAULT_CXF_FEATURES_CONFIG_FILE_NAME = "producer.features";

   public FeatureIntegratorFactory()
   {
      try
      {
         File featuresPropertyFile = new File(CXFConfiguration.GATEIN_WSRP_CXF_CONF_DIR, DEFAULT_CXF_FEATURES_CONFIG_FILE_NAME);

         if (featuresPropertyFile.exists())
         {
            FileInputStream fileInStream = new FileInputStream(featuresPropertyFile);
            BufferedReader reader = new BufferedReader(new InputStreamReader(fileInStream));

            String featureClassName = reader.readLine();
            while (featureClassName != null)
            {
               //Don't use the class if its commented out using '#' or '!'
               //TODO: can we read this as normal property file but with null values?
               if (!featureClassName.trim().startsWith("#") && !featureClassName.trim().startsWith("!"))
               {
                  AbstractFeature feature = createFeature(featureClassName);
                  if (feature != null)
                  {
                     features.add(feature);
                  }
               }
               featureClassName = reader.readLine();
            }
            reader.close();
         }
         else
         {
            log.debug("The wsrp cxf features configuration file does not exist [" + featuresPropertyFile + "]. No features will be added to the wsrp producer service classes.");
         }
      }
      catch (Exception e)
      {
         log.error("Error processing GateIn CXF Feature configuration files", e);
      }
   }

   /**
    * Returns an instance of the FeatureIntegratorFactory
    *
    * @return The FeatureIntegratorFactory instance
    */
   public static FeatureIntegratorFactory getInstance()
   {
      return instance;
   }

   /**
    * Returns the features
    *
    * @return The features
    */
   public List<AbstractFeature> getFeatures()
   {
      return features;
   }

   /**
    * Constructs an AbstractFeature class based on the passed classname.
    * <p/>
    * Note: the AbstractFeature must have an empty constructor.
    *
    * @param className The class to create
    * @return The constructed AbstractFeature
    */
   protected AbstractFeature createFeature(String className)
   {
      try
      {
         Class clazz = Class.forName(className);
         Object object = clazz.getConstructor().newInstance();

         if (object instanceof AbstractFeature)
         {
            return (AbstractFeature)object;
         }
         else
         {
            log.error("Class " + className + " listed in feature file is not of type AbstractFeature. This entry is ignored.");
         }
      }
      catch (Exception e)
      {
         log.error("Error try to create class " + className + " listed in the the feature file. This entry will be ignored.", e);
      }

      return null;
   }

}
