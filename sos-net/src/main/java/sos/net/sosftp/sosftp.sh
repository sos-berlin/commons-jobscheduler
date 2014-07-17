#!/bin/sh
#
# ------------------------------------------------------------
# Company: Software- und Organisations-Service GmbH
# Author : Andreas Püschel <andreas.pueschel@sos-berlin.com>
# Author : Oliver Haufe <oliver.haufe@sos-berlin.com>
# $Id:$
# Purpose: start FTP Processing
# ------------------------------------------------------------
#
CLASSPATH_BASE="%{INSTALL_PATH}"
CUR_DIR=`pwd`
if [ ! -d "$CLASSPATH_BASE" ]
then
  echo "Classpath directory \"$CLASSPATH_BASE\" does not exist."
  exit 1
fi
test -z "$JAVA_HOME" && JAVA_HOME="%{JAVA_HOME}"
JAVA_BIN="$JAVA_HOME/bin/java"

cd "$CLASSPATH_BASE"

"$JAVA_BIN" -classpath "*" sos.net.SOSFTPCommand "$@" -current_pid=$$ -ppid=$PPID
RC=$?

cd "$CUR_DIR"

exit $RC
