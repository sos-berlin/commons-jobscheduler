package com.sos.VirtualFileSystem.shell;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class cmdShellTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(cmdShellTest.class);
    private CmdShell objShell = null;

    @Before
    public void setUp() throws Exception {
        String osn = System.getProperty("os.name");
        String fcp = System.getProperty("file.encoding");
        String ccp = System.getProperty("console.encoding");
        LOGGER.info(osn + ", fcp =  " + fcp + ", ccp = " + ccp);
        objShell = new CmdShell();
    }

    public void testExecuteCommand() throws Exception {
        objShell = new CmdShell();
        int intCC = 0;
        intCC = objShell.executeCommand("dir");
        LOGGER.info("intCC = " + intCC);
    }

    @Test
    public void testExecuteSQLPlus() throws Exception {
        objShell = new CmdShell();
        int intCC = 0;
        intCC =
                objShell.executeCommand("echo 1 | \"C:/oraclexe/app/oracle/product/11.2.0/server/bin/sqlplus.exe\" -S -L "
                        + "sys/scheduler@localhost as sysdba @c:/temp/mycmd.sql");
        LOGGER.info("intCC = " + intCC);
    }

}