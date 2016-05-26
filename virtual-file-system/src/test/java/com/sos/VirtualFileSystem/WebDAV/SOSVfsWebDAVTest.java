package com.sos.VirtualFileSystem.WebDAV;

import com.sos.JSHelper.Options.SOSOptionTransferType;
import com.sos.JSHelper.io.Files.JSTextFile;
import com.sos.VirtualFileSystem.Factory.VFSFactory;
import com.sos.VirtualFileSystem.Interfaces.ISOSVFSHandler;
import com.sos.VirtualFileSystem.Interfaces.ISOSVfsFileTransfer;
import com.sos.VirtualFileSystem.Interfaces.ISOSVirtualFile;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;
import com.sos.VirtualFileSystem.Options.SOSFTPOptions;

import org.apache.log4j.Logger;
import org.junit.*;

/** @author KB */
public class SOSVfsWebDAVTest {

    protected static final Logger LOGGER = Logger.getLogger(SOSVfsWebDAVTest.class);
    protected static final String LOCAL_BASE_PATH = "R:/backup/sos/java/junittests/testdata/JADE/";
    protected static final String REMOTE_BASE_PATH = "/webdav/";
    protected static final String WEB_URI = "http://homer.sos/webdav";
    protected static final String WEB_USER = "test";
    protected static final String WEB_PASS = "12345";
    protected SOSFTPOptions objOptions = null;
    protected ISOSVFSHandler objVFS = null;
    protected ISOSVfsFileTransfer objVfsClient = null;
    protected String dynamicClassNameSource = null;
    protected String dynamicClassNameTarget = null;

    @Before
    public void setUp() throws Exception {
        objOptions = new SOSFTPOptions(SOSOptionTransferType.enuTransferTypes.webdav);
        objOptions.protocol.setValue(SOSOptionTransferType.enuTransferTypes.webdav);
        objVFS = VFSFactory.getHandler(objOptions.protocol.getValue());
        objVfsClient = (ISOSVfsFileTransfer) objVFS;
    }

    private void connect() throws Exception {
        objOptions.host.setValue(WEB_URI);
        objOptions.port.value(8080);
        SOSConnection2OptionsAlternate objSource = objOptions.getConnectionOptions().getSource();
        objSource.host.setValue(WEB_URI);
        objSource.user.setValue(WEB_USER);
        objSource.port.value(8080);
        objSource.protocol.setValue("webdav");
        objOptions.operation.setValue("send");
        VFSFactory.setConnectionOptions(objSource);
        objVFS = VFSFactory.getHandler(objOptions.protocol.getValue());
        objVfsClient = (ISOSVfsFileTransfer) objVFS;
        objVFS.connect(objOptions.getConnectionOptions().getSource());
    }

    private void authenticate() throws Exception {
        SOSConnection2OptionsAlternate objSource = objOptions.getConnectionOptions().getSource();
        objSource.host.setValue(WEB_URI);
        objSource.user.setValue(WEB_USER);
        objSource.password.setValue(WEB_PASS);
        objSource.protocol.setValue("webdav");
        objSource.sshAuthMethod.isURL(true);
        objVFS.authenticate(objSource);
    }

    @Test
    public void testMkdir() throws Exception {
        connect();
        authenticate();
        objVfsClient.rmdir(REMOTE_BASE_PATH + "kb/test1");
        objVfsClient.mkdir(REMOTE_BASE_PATH + "kb/test1");
        ISOSVirtualFile objVF = objVfsClient.getFileHandle(REMOTE_BASE_PATH + "kb/test1");
        LOGGER.info(objVF.getFileSize());
        objVfsClient.disconnect();
    }

    @Test
    public void testMkdirMultiple() throws Exception {
        connect();
        authenticate();
        objVfsClient.mkdir(REMOTE_BASE_PATH + "kb/test1/test2/test3/");
        objVfsClient.disconnect();
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testRmRdir() throws Exception {
        connect();
        authenticate();
        objVfsClient.rmdir(REMOTE_BASE_PATH + "kb/test1/test2/test3");
        objVfsClient.disconnect();
    }

    @Test
    public void testSize() throws Exception {
        connect();
        authenticate();
        LOGGER.info(objVfsClient.getFileSize(REMOTE_BASE_PATH + "text.txt"));
        objVfsClient.disconnect();
    }

    private String createTestFile() throws Exception {
        String strTestFileName = LOCAL_BASE_PATH + "webdav-test.dat";
        JSTextFile objF = new JSTextFile(strTestFileName);
        objF.writeLine("Die Basis ist das Fundament der Grundlage");
        objF.deleteOnExit();
        objF.close();
        return strTestFileName;
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testPut() throws Exception {
        connect();
        authenticate();
        String strTargetFileName = REMOTE_BASE_PATH + "sos-net-src.zip";
        objVfsClient.putFile(createTestFile(), strTargetFileName);
        ISOSVirtualFile objVF = objVfsClient.getFileHandle(strTargetFileName);
        long intFileSize = objVF.getFileSize();
        LOGGER.info("Size of Targetfile = " + intFileSize);
        objVfsClient.delete(strTargetFileName);
        objVfsClient.disconnect();
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testPutFileStringOutputStream() throws Exception {
        connect();
        authenticate();
        String strTargetFileName = REMOTE_BASE_PATH + "out.test.txt";
        objVfsClient.delete(strTargetFileName);
        ISOSVirtualFile objVF = objVfsClient.getFileHandle(strTargetFileName);
        objVF.write("hallo".getBytes());
        objVF.closeOutput();
        long intFileSize = objVF.getFileSize();
        LOGGER.info("Size of Targetfile = " + intFileSize);
        objVfsClient.delete(strTargetFileName);
        objVfsClient.disconnect();
    }

    @Test
    public void testCd() throws Exception {
        connect();
        authenticate();
        objVfsClient.changeWorkingDirectory("/webdav/kb");
        objVfsClient.disconnect();
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testDelete() throws Exception {
        connect();
        authenticate();
        objVfsClient.put(createTestFile(), REMOTE_BASE_PATH + "tmp123.zip");
        objVfsClient.delete(REMOTE_BASE_PATH + "tmp123.zip");
        objVfsClient.disconnect();
    }

    @Test
    public void testLogin() throws Exception {
        connect();
        objVfsClient.login(WEB_USER, WEB_PASS);
        objVfsClient.disconnect();
    }

    @Test
    public void testDisconnect() throws Exception {
        connect();
        objVfsClient.login(WEB_USER, WEB_PASS);
        objVfsClient.disconnect();
    }

    @Test
    public void testGetReplyString() throws Exception {
        connect();
        objVfsClient.login(WEB_USER, WEB_PASS);
        LOGGER.info("Replay = " + objVfsClient.getReplyString());
        objVfsClient.disconnect();
    }

    @Test
    public void testIsConnected() throws Exception {
        connect();
        LOGGER.debug("IS CONNECTED = " + objVfsClient.isConnected());
        objVfsClient.disconnect();
    }

    @Test
    public void testLogout() throws Exception {
        connect();
        objVfsClient.login(WEB_USER, WEB_PASS);
        objVfsClient.logout();
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testRename() throws Exception {
        connect();
        authenticate();
        String strTestFileName = createTestFile();
        objVfsClient.put(strTestFileName, REMOTE_BASE_PATH + "tmp123.zip");
        objVfsClient.rename(REMOTE_BASE_PATH + "tmp123.zip", REMOTE_BASE_PATH + "tmp1234.zip");
        objVfsClient.delete(REMOTE_BASE_PATH + "tmp1234.zip");
        objVfsClient.disconnect();
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testRename2() throws Exception {
        connect();
        authenticate();
        String strTestFileName = createTestFile();
        objVfsClient.put(strTestFileName, REMOTE_BASE_PATH + "tmp123.zip");
        objVfsClient.rename(REMOTE_BASE_PATH + "tmp123.zip", REMOTE_BASE_PATH + "tmp1234.zip");
        objVfsClient.delete(REMOTE_BASE_PATH + "tmp1234.zip");
        objVfsClient.disconnect();
    }

    @Test
    public void testGetHandler() throws Exception {
        connect();
        authenticate();
        LOGGER.debug("HANDLER = " + objVfsClient.getHandler());
        objVfsClient.disconnect();
    }

    @Test
    public void testExecuteCommand() throws Exception {
        connect();
        authenticate();
        String lineSeparator = "\n";
        objVFS.executeCommand("cd /home/test" + lineSeparator + "cd /home/kb");
        objVfsClient.disconnect();
    }

    @Test
    public void testGetFileHandle() throws Exception {
        connect();
        authenticate();
        LOGGER.info(objVfsClient.getModificationTime(REMOTE_BASE_PATH + "text.txt"));
        objVfsClient.disconnect();
    }

    @Test
    public void testGetFilelist() throws Exception {
        connect();
        authenticate();
        String[] result = objVfsClient.getFilelist(REMOTE_BASE_PATH + "kb", "", 0, true, null);
        for (String element : result) {
            LOGGER.info(element);
        }
        objVfsClient.disconnect();
    }

}