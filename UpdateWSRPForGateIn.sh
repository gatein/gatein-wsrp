#!/bin/sh
# JBoss, a division of Red Hat
# Copyright 2010, Red Hat Middleware, LLC, and individual
# contributors as indicated by the @authors tag. See the
# copyright.txt in the distribution for a full listing of
# individual contributors.
#
# This is free software; you can redistribute it and/or modify it
# under the terms of the GNU Lesser General Public License as
# published by the Free Software Foundation; either version 2.1 of
# the License, or (at your option) any later version.
#
# This software is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
# Lesser General Public License for more details.
#
# You should have received a copy of the GNU Lesser General Public
# License along with this software; if not, write to the Free
# Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
# 02110-1301 USA, or see the FSF site: http://www.fsf.org.

# ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
# This script allows for faster deployment of WSRP libraries to whatever version of GateIn is already deployed on an
# application server.
# @author Chris Laprun
# ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

if [ -z "$GATEIN_EAR_HOME" -o ! -d "$GATEIN_EAR_HOME" ]
then
   echo \=\=\> Please set GATEIN_EAR_HOME to point to the repository on your application server that contains gatein.ear
   exit
fi
echo Using GateIn home at: $GATEIN_EAR_HOME

if [ -z "$JBOSS_TEST_HOME" -o ! -d "$JBOSS_TEST_HOME" ]
then
   echo \=\=\> Please set JBOSS_TEST_HOME to point the root directory of a clean install of JBoss AS 5.1.x to be used for tests
   exit
fi
echo --------------------------------------------------------------------------
echo \| Using JBoss AS home at: $JBOSS_TEST_HOME

# Retrieve the current WSRP version as specified in the POM file
export CURRENT_WSRP=`grep -m 1 ".*<version>\(.*\)<\/version>.*" pom.xml | sed -n -e 's/.*<version>\(.*\)<\/.*/\1/p'`

# extract the current version of WSRP module from existing files
DEPLOYED_WSRP=`ls $GATEIN_EAR_HOME/lib/wsrp* | sed -n '1 s/.*\/.*-\([0-9]\.[0-9].[0-9].*\).jar/\1/p'`

echo \| Deployed WSRP version: \'$DEPLOYED_WSRP\'
echo \| Current WSRP version in project POM: \'$CURRENT_WSRP\'
echo \|

# If we have no argument, build and test.
# If we have one argument (irrelevant value), build but skip tests
# If we have more than one argument (irrelevant values), only deploy the files without build
if [ $# -eq 0 ]
then
   mvn clean install
else
   if [ $# -eq 1 ]
   then
      echo \| ===============
      echo \| Skipping tests!
      echo \| ===============
      echo --------------------------------------------------------------------------
      mvn clean install '-Dmaven.test.skip=true'
   fi
fi

# get the list of jar files we need to replace in lib
current=`ls $GATEIN_EAR_HOME/lib/wsrp* | sed -n 's/.*\/\(.*\)-'$DEPLOYED_WSRP'.jar/\1/p'`

# remove existing files so that we don't end up with duplicate (and conflicting) files if most recent and current
# version don't match
rm -f $GATEIN_EAR_HOME/lib/wsrp*

# extract which WSRP libs are currently needed by GateIn and replace them with a fresh version from local repository
echo Deploying JAR files:
for lib in $current
do
   echo Copying $lib-$CURRENT_WSRP.jar to $GATEIN_EAR_HOME/lib/
   cp $HOME/.m2/repository/org/gatein/wsrp/$lib/$CURRENT_WSRP/$lib-$CURRENT_WSRP.jar $GATEIN_EAR_HOME/lib/
done

echo

# get the list of war files we need to replace in $GATEIN_EAR_HOME
current=`ls $GATEIN_EAR_HOME/wsrp* | sed -n 's/.*\/\(.*\).war/\1/p'`

# remove existing files
rm -f $GATEIN_EAR_HOME/wsrp*

# deal with producer and admin GUI WARs separately as they are put elsewhere and without version name
echo Deploying WAR files:
for war in $current
do
   echo Copying $war-$CURRENT_WSRP.war to $GATEIN_EAR_HOME/$war.war
   cp $HOME/.m2/repository/org/gatein/wsrp/$war/$CURRENT_WSRP/$war-$CURRENT_WSRP.war $GATEIN_EAR_HOME/$war.war
done