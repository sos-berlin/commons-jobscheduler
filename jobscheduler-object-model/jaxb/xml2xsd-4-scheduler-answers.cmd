@echo 


if %~1x == x (
  echo ... first parameter must be an xml file
  exit /B 1
)

set XMLFILENAME=%~dpnx1
set XMLBASENAME=%~dpn1
set CUR_DIR=%CD%

cd /D "%~dp0"

java -jar "..\..\..\trang\trang.jar" "%XMLFILENAME%" "%XMLBASENAME%.xsd"

rem scheduler_answer.xsd muss manuell mit %XMLBASENAME%.xsd angepasst werden
rem ..\..\..\jaxb\jaxb-ri-20110115\bin\xjc  -mark-generated -p com.sos.scheduler.model.answers "scheduler_answer.xsd"

cd /D "%CUR_DIR%"
