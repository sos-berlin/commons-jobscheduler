@echo off
@rem 
@rem ------------------------------------------------------------
@rem Company: Software- und Organisations-Service GmbH
@rem Author : Andreas Püschel <andreas.pueschel@sos-berlin.com>
@rem Dated  : 2007-10-11
@rem Purpose: start FTP Processing
@rem ------------------------------------------------------------

SET CLASSPATH_BASE=.

java -classpath "%CLASSPATH_BASE%/commons-net-1.2.2.jar;%CLASSPATH_BASE%/sos.net.jar;%CLASSPATH_BASE%/sos.settings.jar;%CLASSPATH_BASE%/sos.util.jar;%CLASSPATH_BASE%/trilead-ssh2-build211.jar" sos.net.SOSFTPCommand %*
