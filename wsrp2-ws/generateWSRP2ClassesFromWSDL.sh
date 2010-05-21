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
# Generates the WSRP classes from the WSDL. Added mostly for documentation purposes. wsconsume.sh is the tool that comes
# bundled with JBoss WS in JBoss AS.
# @author Chris Laprun
# ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

if [ -z "$JBOSS_5_HOME" -o ! -d "$JBOSS_5_HOME" ]
then
   echo \=\=\> Please set JBOSS_5_HOME to point to the repository on your system where JBoss AS 5+ is installed
   exit
fi
echo Using JBoss AS home at: $JBOSS_5_HOME

# delete previous version
rm -rf ./src/main/java/org/oasis/wsrp/v2/*.java

$JBOSS_5_HOME/bin/wsconsume.sh -k -b ./src/main/resources/wsdl/jaxb-customization.xml -p org.oasis.wsrp.v2 \
   -s ./src/main/java/ -o ./tmp-wsconsume-output ./src/main/resources/wsdl/wsrp-2.0-services.wsdl

# remove useless classes
rm -f ./src/main/java/org/oasis/wsrp/v2/WSRPService.java
rm -f ./src/main/java/org/oasis/wsrp/v2/ObjectFactory.java

# remove the compiled version
rm -rf ./tmp-wsconsume-output