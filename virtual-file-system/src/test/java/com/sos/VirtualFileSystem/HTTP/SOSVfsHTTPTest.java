package com.sos.VirtualFileSystem.HTTP;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.JSHelper.Options.SOSOptionTransferType;
import com.sos.VirtualFileSystem.Factory.VFSFactory;
import com.sos.VirtualFileSystem.Interfaces.ISOSVFSHandler;
import com.sos.VirtualFileSystem.Interfaces.ISOSVfsFileTransfer;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;
import com.sos.VirtualFileSystem.Options.SOSFTPOptions;

/** @author KB */
public class SOSVfsHTTPTest {

    protected static final Logger LOGGER = LoggerFactory.getLogger(SOSVfsHTTPTest.class);
    protected static final String LOCAL_BASE_PATH = "R:/backup/sos/java/junittests/testdata/JADE/";
    protected static final String HTTP_URI = "www.sos-berlin.com";
    protected static final int HTTP_PORT = 80;
    protected static final String HTTPS_URI = "https://kb.sos-berlin.com";
    protected static final int HTTPS_PORT = 443;
    protected SOSFTPOptions objOptions = null;
    protected ISOSVFSHandler objVFS = null;
    protected ISOSVfsFileTransfer objVfsClient = null;
    SOSConnection2OptionsAlternate objSource = null;
    protected String dynamicClassNameSource = null;
    protected String dynamicClassNameTarget = null;

    @Before
    public void setUp() throws Exception {
        objOptions = new SOSFTPOptions(SOSOptionTransferType.enuTransferTypes.http);
        objOptions.protocol.setValue(SOSOptionTransferType.enuTransferTypes.http);
        objOptions.authMethod.isURL(true);
        objSource = objOptions.getConnectionOptions().getSource();
        objVFS = VFSFactory.getHandler(objOptions.protocol.getValue());
        objVfsClient = (ISOSVfsFileTransfer) objVFS;
    }

    private void connect() throws RuntimeException, Exception {
        objSource.host.setValue(HTTP_URI);
        objSource.port.value(HTTP_PORT);
        objVFS.connect(objSource);
    }

    private void authenticate() throws Exception {
        objSource.user.setValue("xxx");
        objSource.password.setValue("xxx");
        objVFS.authenticate(objSource);
    }

    private void disconnect() throws Exception {
        objVfsClient.disconnect();
        objVFS.closeConnection();
    }

    @Test
    public void testConnect() throws Exception {
        connect();
        disconnect();
    }

    @Test
    public void testAuthenticate() throws Exception {
        connect();
        authenticate();
        disconnect();
    }

    @Test
    public void testGetFile() throws Exception {
        connect();
        authenticate();
        objVfsClient.getFile("timecard/timecard_dialog.php", "D:\\1.php");
        disconnect();
    }

    @Test
    public void testMkdir() throws Exception {
        connect();
        authenticate();
        objVfsClient.mkdir("test1");
        objVfsClient.rmdir("test1");
        disconnect();
    }

    @Test
    public void testMkdirMultiple() throws Exception {
        connect();
        authenticate();
        objVfsClient.mkdir("test1/test2/test3/");
        objVfsClient.rmdir("test1/");
        disconnect();
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testgetInputStream() throws Exception {
        connect();
        authenticate();
        objVfsClient.getInputStream("test1/test2/test3/");
        disconnect();
    }

    @Test
    public void testSize() throws Exception {
        connect();
        authenticate();
        disconnect();
    }

    @Test
    public void testPut() throws RuntimeException, Exception {
        connect();
        authenticate();
        disconnect();
    }

    @Test
    public void testPutFileStringOutputStream() throws Exception {
        connect();
        authenticate();
        disconnect();
    }

    @Test
    public void testCd() throws Exception {
        connect();
        authenticate();
        objVfsClient.changeWorkingDirectory("/xxx/xxx");
        disconnect();
    }

    @Test
    public void testDelete() throws Exception {
        connect();
        authenticate();
        disconnect();
    }

    @Test
    public void testIsConnected() throws Exception {
        connect();
        LOGGER.debug("IS CONNECTED = " + objVfsClient.isConnected());
        disconnect();
    }

    @Test
    public void testLogout() throws Exception {
        connect();
        disconnect();
    }

    @Test
    public void testRename() throws Exception {
        connect();
        authenticate();
        disconnect();
    }

    @Test
    public void testGetHandler() throws Exception {
        connect();
        authenticate();
        LOGGER.debug("HANDLER = " + objVfsClient.getHandler());
        disconnect();
    }

    @Test
    public void testExecuteCommand() throws Exception {
        connect();
        authenticate();
        String lineSeparator = "\n";
        objVFS.executeCommand("cd /home/test" + lineSeparator + "cd /home/kb");
        disconnect();
    }

}