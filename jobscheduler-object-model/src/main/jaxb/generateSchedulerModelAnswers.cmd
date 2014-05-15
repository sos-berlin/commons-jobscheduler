@echo off

set SCHEMAFILE=scheduler_answers.xsd
set CUR_DIR=%CD%
cd /D "%~dp0"

rem ..\..\..\jaxb\jaxb-ri-20110115\bin\xjc -mark-generated -d ..\..\..\jaxb %SCHEMAFILE% -b externalAnswersBind.xml
echo ..\..\..\jaxb\jaxb-ri-20110115\bin\xjc -mark-generated -d ..\..\..\jaxb -p com.sos.scheduler.model.answers %SCHEMAFILE% 
..\..\..\jaxb\jaxb-ri-20110115\bin\xjc -mark-generated -d ..\..\..\jaxb -p com.sos.scheduler.model.answers %SCHEMAFILE%

cd /D "%CUR_DIR%"
