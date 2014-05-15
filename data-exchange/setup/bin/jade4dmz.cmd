@echo off
@rem 
@rem ------------------------------------------------------------
@rem Company: Software- und Organisations-Service GmbH
@rem Author : Oliver Haufe <oliver.haufe@sos-berlin.com>
@rem $Id$
@rem ------------------------------------------------------------
SETLOCAL

set CLASSPATH_BASE=${INSTALL_PATH}\lib
if not exist "%CLASSPATH_BASE%" (
  echo Classpath directory "%CLASSPATH_BASE%" does not exist.
  exit /B 1
)
if not defined JAVA_HOME set JAVA_HOME=${JAVA_HOME}
set JAVA_BIN=%JAVA_HOME%\bin\java.exe
if not exist "%JAVA_BIN%" set JAVA_BIN=java.exe


@rem classpath
set CP=
for /F "usebackq" %%i in (`dir /B "%CLASSPATH_BASE%\*.jar"`) do call :set_classpath "%CLASSPATH_BASE%\%%i"
set CP=%CP:~1%
goto start

:set_classpath
set CP=%CP%;%~1
goto final

:start
"%JAVA_BIN%" -classpath "%CP:\=/%" com.sos.DataExchange.SOSDataExchangeEngine4MDZMain %*
exit /B %ERRORLEVEL%

ENDLOCAL
:final
