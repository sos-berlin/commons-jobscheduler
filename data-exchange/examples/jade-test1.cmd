@echo off

goto :start

jade.cmd -operation=send ^
         -user=test ^ 
         -password=12345 ^
         -file_path c:\temp\test.txt ^
         -host wilma.sos 

jade.cmd -settings=..\examples\jade_settings.ini -profile=ftp_send_1_wilma

jade.cmd -settings=..\examples\jade_settings.ini -profile=ftp_server_2_server

:start
jade.cmd -settings=..\examples\jade_settings.ini -profile=zip_local_files

