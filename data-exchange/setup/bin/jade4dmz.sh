#!/bin/sh
#
# ------------------------------------------------------------
# Company: Software- und Organisations-Service GmbH
# Author : Oliver Haufe <oliver.haufe@sos-berlin.com>
# $Id$
# ------------------------------------------------------------
#
CLASSPATH_BASE="%{INSTALL_PATH}/lib"
if [ ! -d "$CLASSPATH_BASE" ]
then
  echo "Classpath directory \"$CLASSPATH_BASE\" does not exist."
  exit 1
fi
test -z "$JAVA_HOME" && JAVA_HOME="%{JAVA_HOME}"
JAVA_BIN="$JAVA_HOME/bin/java" 
test -z "$TEMP" && TEMP="/tmp"
export TEMP

# set_classpath
CP=""
for lib in `ls $CLASSPATH_BASE/*.jar` 
do
  CP="$CP:$lib"
done
CP=`echo "$CP" | cut -c 2-`

"$JAVA_BIN" -classpath "$CP" com.sos.DataExchange.SOSDataExchangeEngine4DMZMain "$@" -current_pid=$$ -ppid=$PPID
exit $?
