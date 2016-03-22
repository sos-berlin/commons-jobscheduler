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
    private SOSFTPOptions objOptions = null;
    private ISOSVFSHandler objVFS = null;
    private ISOSVfsFileTransfer ftpClient = null;
    private final String strTestFileName = "text.junittest";
    private final String strTestPathName = "c:/temp/";
    private final String strHostName4Test = "localhost";

    public SOSVfsFtpSTest() {
        //
    }

    @Before
    public void setUp() throws Exception {
        objOptions = new SOSFTPOptions();
        objOptions.protocol.Value(enuTransferTypes.ftps.Text());
        objVFS = VFSFactory.getHandler(objOptions.protocol.Value());
        ftpClient = (ISOSVfsFileTransfer) objVFS;
    }

    @Test
    public void testOptionOperation() throws Exception {
        HashMap<String, String> objHsh = new HashMap<String, String>();
        objHsh.put("operation", "rename");
        objOptions = new SOSFTPOptions(objHsh);
        assertEquals("", "rename", objOptions.operation.Value());
    }

    @Test(expected = java.lang.Exception.class)
    @Ignore("Test set to Ignore for later examination")
    public void testConnect() throws Exception {
        SOSConnection2OptionsAlternate objConOpts4Target = objOptions.getConnectionOptions().Target();
        objConOpts4Target.host.Value(strHostName4Test);
        objVFS.Connect(objConOpts4Target);
        String strR = ftpClient.getReplyString();
        objOptions.user.Value("kb");
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testExecuteCommand() throws Exception {
        SOSConnection2OptionsAlternate objConOpts4Target = objOptions.getConnectionOptions().Target();
        objConOpts4Target.host.Value(strHostName4Test);
        objVFS.Connect(objConOpts4Target);
        String strR = ftpClient.getReplyString();
        objOptions.user.Value("kb");
        objOptions.password.Value("kb");
        objVFS.Authenticate(objOptions);
        objVFS.ExecuteCommand("SYST");
        objVFS.ExecuteCommand("FEAT");
        objVFS.ExecuteCommand("OPTS");
        objVFS.ExecuteCommand("OPTS UTF8 NLST");
        objVFS.ExecuteCommand("OPTS UTF-8 NLST");
        objVFS.ExecuteCommand("OPTS UTF8 OFF");
        objVFS.ExecuteCommand("OPTS UTF8 ON");
        objVFS.ExecuteCommand("OPTS MLST Type;Size;Modify;UNIX.mode;UNIX.owner;UNIX.group;");
        objVFS.ExecuteCommand("MLST /Büttner.dat");
        objVFS.ExecuteCommand("OPTS UTF8 OFF");
        objVFS.ExecuteCommand("MLST /Büttner.dat");
        objVFS.ExecuteCommand("LIST");
        objVFS.ExecuteCommand("PORT 127,0,0,1,6,81");
        objVFS.ExecuteCommand("MLSD");
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testAuthenticate() throws Exception {
        /** Test expects the fileZilla Server on (localhost) */
        SOSConnection2OptionsAlternate objConOpts4Target = objOptions.getConnectionOptions().Target();
        objConOpts4Target.host.Value(strHostName4Test);
        objVFS.Connect(objConOpts4Target);
        String strR = ftpClient.getReplyString();
        if (strR.trim().length() <= 0) {
            strR = "xxxxx";
        }
        assertEquals("Connect message", "2", strR.substring(0, 1));
        objOptions.user.Value("kb");
        objOptions.password.Value("kb");
        objVFS.Authenticate(objOptions);
        strR = ftpClient.getReplyString();
        assertEquals("Login message", "230 Logged on", strR.substring(0, strR.length() - 2));
        objVFS.CloseSession();
        strR = ftpClient.getReplyString();
        assertEquals("Login message", "221 Goodbye", strR.substring(0, strR.length() - 2));
        objVFS.CloseConnection();
    }

    private void CreateTestFile() {
        JSFile objFile = new JSFile(strTestPathName + strTestFileName);
        try {
            objFile.WriteLine("Das ist eine Testdatei. Weiter nichts");
            objFile.close();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testPutFileString() throws Exception {
        CreateTestFile();
        /** Test expects the fileZilla Server on (localhost) */
        SOSConnection2OptionsAlternate objConOpts4Target = objOptions.getConnectionOptions().Target();
        objConOpts4Target.host.Value(strHostName4Test);
        objVFS.Connect(objConOpts4Target);
        String strR = ftpClient.getReplyString();
        if (strR.trim().length() <= 0) {
            strR = "xxxxx";
        }
        assertEquals("Connect message", "2", strR.substring(0, 1));
        objOptions.user.Value("kb");
        objOptions.password.Value("kb");
        objVFS.Authenticate(objOptions);
        strR = ftpClient.getReplyString();
        assertEquals("Login message", "2", strR.substring(0, 1));
        ftpClient.putFile(strTestPathName + strTestFileName, strTestFileName);
        objVFS.CloseSession();
        strR = ftpClient.getReplyString();
        assertEquals("Login message", "221 Goodbye", strR.substring(0, strR.length() - 2));
        objVFS.CloseConnection();
    }

}
