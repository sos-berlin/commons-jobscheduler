#!/bin/sh
#
# ------------------------------------------------------------
# Company: Software- und Organisations-Service GmbH
# Author : Andreas Püschel <andreas.pueschel@sos-berlin.com>
# Dated  : 2007-10-11
# Purpose: start FTP Processing
# ------------------------------------------------------------

CLASSPATH_BASE=.
JAVA_BIN="java"

"$JAVA_BIN" -classpath "$CLASSPATH_BASE/commons-net-1.2.2.jar:$CLASSPATH_BASE/sos.net.jar:$CLASSPATH_BASE/sos.settings.jar:$CLASSPATH_BASE/sos.util.jar:$CLASSPATH_BASE/trilead-ssh2-build211.jar" sos.net.SOSFTPCommand ${1+"$@"}
