package com.sos.VirtualFileSystem.FTPS;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.HashMap;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.sos.JSHelper.Options.SOSOptionTransferType.enuTransferTypes;
import com.sos.JSHelper.io.Files.JSFile;
import com.sos.VirtualFileSystem.Factory.VFSFactory;
import com.sos.VirtualFileSystem.Interfaces.ISOSVFSHandler;
import com.sos.VirtualFileSystem.Interfaces.ISOSVfsFileTransfer;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;
import com.sos.VirtualFileSystem.Options.SOSFTPOptions;

/** @author KB */
public class SOSVfsFtpSTest {

    private static final Logger LOGGER = Logger.getLogger(SOSVfsFtpSTest.class);
    private static final String TEST_FILE_NAME = "text.junittest";
    private static final String TEST_PATH_NAME = "c:/temp/";
    private static final String HOST_NAME_4_TEST = "localhost";
    private SOSFTPOptions objOptions = null;
    private ISOSVFSHandler objVFS = null;
    private ISOSVfsFileTransfer ftpClient = null;

    @Before
    public void setUp() throws Exception {
        objOptions = new SOSFTPOptions();
        objOptions.protocol.setValue(enuTransferTypes.ftps.getText());
        objVFS = VFSFactory.getHandler(objOptions.protocol.getValue());
        ftpClient = (ISOSVfsFileTransfer) objVFS;
    }

    @Test
    public void testOptionOperation() throws Exception {
        HashMap<String, String> objHsh = new HashMap<String, String>();
        objHsh.put("operation", "rename");
        objOptions = new SOSFTPOptions(objHsh);
        assertEquals("", "rename", objOptions.operation.getValue());
    }

    @Test(expected = java.lang.Exception.class)
    @Ignore("Test set to Ignore for later examination")
    public void testConnect() throws Exception {
        SOSConnection2OptionsAlternate objConOpts4Target = objOptions.getConnectionOptions().getTarget();
        objConOpts4Target.host.setValue(HOST_NAME_4_TEST);
        objVFS.connect(objConOpts4Target);
        String strR = ftpClient.getReplyString();
        objOptions.user.setValue("kb");
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testExecuteCommand() throws Exception {
        SOSConnection2OptionsAlternate objConOpts4Target = objOptions.getConnectionOptions().getTarget();
        objConOpts4Target.host.setValue(HOST_NAME_4_TEST);
        objVFS.connect(objConOpts4Target);
        String strR = ftpClient.getReplyString();
        objOptions.user.setValue("kb");
        objOptions.password.setValue("kb");
        objVFS.authenticate(objOptions);
        objVFS.executeCommand("SYST");
        objVFS.executeCommand("FEAT");
        objVFS.executeCommand("OPTS");
        objVFS.executeCommand("OPTS UTF8 NLST");
        objVFS.executeCommand("OPTS UTF-8 NLST");
        objVFS.executeCommand("OPTS UTF8 OFF");
        objVFS.executeCommand("OPTS UTF8 ON");
        objVFS.executeCommand("OPTS MLST Type;Size;Modify;UNIX.mode;UNIX.owner;UNIX.group;");
        objVFS.executeCommand("MLST /Büttner.dat");
        objVFS.executeCommand("OPTS UTF8 OFF");
        objVFS.executeCommand("MLST /Büttner.dat");
        objVFS.executeCommand("LIST");
        objVFS.executeCommand("PORT 127,0,0,1,6,81");
        objVFS.executeCommand("MLSD");
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testAuthenticate() throws Exception {
        /** Test expects the fileZilla Server on (localhost) */
        SOSConnection2OptionsAlternate objConOpts4Target = objOptions.getConnectionOptions().getTarget();
        objConOpts4Target.host.setValue(HOST_NAME_4_TEST);
        objVFS.connect(objConOpts4Target);
        String strR = ftpClient.getReplyString();
        if (strR.trim().length() <= 0) {
            strR = "xxxxx";
        }
        assertEquals("Connect message", "2", strR.substring(0, 1));
        objOptions.user.setValue("kb");
        objOptions.password.setValue("kb");
        objVFS.authenticate(objOptions);
        strR = ftpClient.getReplyString();
        assertEquals("Login message", "230 Logged on", strR.substring(0, strR.length() - 2));
        objVFS.closeSession();
        strR = ftpClient.getReplyString();
        assertEquals("Login message", "221 Goodbye", strR.substring(0, strR.length() - 2));
        objVFS.closeConnection();
    }

    private void createTestFile() {
        JSFile objFile = new JSFile(TEST_PATH_NAME + TEST_FILE_NAME);
        try {
            objFile.writeLine("Das ist eine Testdatei. Weiter nichts");
            objFile.close();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testPutFileString() throws Exception {
        createTestFile();
        /** Test expects the fileZilla Server on (localhost) */
        SOSConnection2OptionsAlternate objConOpts4Target = objOptions.getConnectionOptions().getTarget();
        objConOpts4Target.host.setValue(HOST_NAME_4_TEST);
        objVFS.connect(objConOpts4Target);
        String strR = ftpClient.getReplyString();
        if (strR.trim().length() <= 0) {
            strR = "xxxxx";
        }
        assertEquals("Connect message", "2", strR.substring(0, 1));
        objOptions.user.setValue("kb");
        objOptions.password.setValue("kb");
        objVFS.authenticate(objOptions);
        strR = ftpClient.getReplyString();
        assertEquals("Login message", "2", strR.substring(0, 1));
        ftpClient.putFile(TEST_PATH_NAME + TEST_FILE_NAME, TEST_FILE_NAME);
        objVFS.closeSession();
        strR = ftpClient.getReplyString();
        assertEquals("Login message", "221 Goodbye", strR.substring(0, strR.length() - 2));
        objVFS.closeConnection();
    }

}