
                                             Software- und Organisations-Service GmbH

                                             Giesebrechtstr. 15
                                             D-10629 Berlin
                                             tel  +49 30 86 47 90-0
                                             fax  +49 30 8 61 33 35
                                             mail info@sos-berlin.com
                                             web   www.sos-berlin.com


About SOSFTP Client
===================
SOSFTP Client is a FTP/SFTP-Client for Unix and Windows that 
supports secured file transfer between different TCP/IP networks.
The SOSFTP client supports the protocols FTP, FTPS (FTP over SSL) and SFTP.


Usage
=====
SOSFTP Client is implemented in Java therefore JRE 1.6 or higher is required.
The startscritps of SOSFTP Client are sosftp.sh (Unix) and sosftp.cmd (Windows).
Both startscripts set the environment variables CLASSPATH_BASE and JAVA_BIN.

This are the default values:

sosftp.sh
---------
CLASSPATH_BASE=.
JAVA_BIN="java"

sosftp.cmd
----------
set CLASSPATH_BASE=.
set JAVA_BIN="java"


These settings can be adjusted in respect to your individual environment.

The variable CLASSPATH_BASE is set as the directory were the required jar files are stored.
By default this is the installation directory of the SOSFTP Client.
Normally this has not to be changed.

The variable JAVA_BIN refers to the java binary.
If the java binary is not available in your working directory then adjust this variable
by inserting the path of your java binary.

