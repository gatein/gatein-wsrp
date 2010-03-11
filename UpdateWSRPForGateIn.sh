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

# This script allows for faster deployment of WSRP libraries to whatever version of GateIn is already deployed on an
# application server.
# @author Chris Laprun

if [ -z "$GATEIN_EAR_HOME" -o ! -d "$GATEIN_EAR_HOME" ]
then
   echo Please set GATEIN_EAR_HOME to point to the repository on your application that contains gatein.ear
   exit
fi
echo Using GateIn home at: $GATEIN_EAR_HOME

# Use this if you want to extract most recent version of WSRP module from the maven-metadata-local.xml <version> tag
# Looks at wsrp-common Maven metadat and only process 5 lines at most (to avoid retrieving several values)
# This allows to always use the most recent version regardless of what version might already be deployed
export MOST_RECENT_WSRP=`sed -n -e '5 s/.*<version>\(.*\)<\/version>.*/\1/p' $HOME/.m2/repository/org/gatein/wsrp/wsrp-common/maven-metadata-local.xml`

# extract the current version of WSRP module from existing files
CURRENT_WSRP=`ls $GATEIN_EAR_HOME/lib/wsrp* | sed -n '1 s/.*\/.*-\([0-9]\.[0-9].[0-9]-.*-.*\).jar/\1/p'`

echo Current WSRP version: \'$CURRENT_WSRP\'
echo Most recent WSRP version: \'$MOST_RECENT_WSRP\'
echo

# get the list of jar files we need to replace in lib
current=`ls $GATEIN_EAR_HOME/lib/wsrp* | sed -n 's/.*\/\(.*\)-'$CURRENT_WSRP'.jar/\1/p'`

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