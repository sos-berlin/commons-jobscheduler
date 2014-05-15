#!/bin/sh

jade.sh -operation=send -user=test -password=12345 -file_path c:\temp\test.txt -host wilma.sos 

jade.sh -settings=../examples/jade_settings.ini -profile=ftp_send_1_wilma

jade.sh -settings=../examples/jade_settings.ini -profile=ftp_server_2_server

jade.sh -settings=../examples/jade_settings.ini -profile=zip_local_files

