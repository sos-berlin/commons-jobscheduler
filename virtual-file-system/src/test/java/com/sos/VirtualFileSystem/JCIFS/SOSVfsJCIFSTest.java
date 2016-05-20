package com.sos.VirtualFileSystem.JCIFS;

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

import java.io.OutputStream;

import static org.junit.Assert.assertEquals;

/** @author KB */
public class SOSVfsJCIFSTest {

    protected final String LOCAL_BASE_PATH = "R:/backup/sos/java/junittests/testdata/JADE/";
    protected final String REMOTE_BASE_PATH = "/test/unittests/testdata/JADE/";
    protected final String HOST = "wilma.sos";
    protected final String USER = "test";
    protected final String PASS = "12345";
    protected Logger logger = Logger.getLogger(this.getClass());
    protected SOSFTPOptions objOptions = null;
    protected ISOSVFSHandler objVFS = null;
    protected ISOSVfsFileTransfer objVfsClient = null;
    protected String dynamicClassNameSource = null;
    protected String dynamicClassNameTarget = null;

    @Before
    public void setUp() throws Exception {
        objOptions = new SOSFTPOptions(SOSOptionTransferType.enuTransferTypes.smb);
        objOptions.protocol.setValue(SOSOptionTransferType.enuTransferTypes.smb);
        objVFS = VFSFactory.getHandler(objOptions.protocol.getValue());
        objVfsClient = (ISOSVfsFileTransfer) objVFS;
    }

    private void connect() throws Exception {
        objOptions.host.setValue(HOST);
        objOptions.port.value(0);
        SOSConnection2OptionsAlternate objSource = objOptions.getConnectionOptions().getSource();
        objSource.host.setValue(HOST);
        objSource.user.setValue(USER);
        objSource.protocol.setValue(SOSOptionTransferType.enuTransferTypes.smb);
        objOptions.operation.setValue("send");
        VFSFactory.setConnectionOptions(objSource);
        objVFS = VFSFactory.getHandler(objOptions.protocol.getValue());
        objVfsClient = (ISOSVfsFileTransfer) objVFS;
        objVFS.connect(objOptions.getConnectionOptions().getSource());
    }

    private void authenticate() throws Exception {
        SOSConnection2OptionsAlternate objSource = objOptions.getConnectionOptions().getSource();
        objSource.domain.setValue("");
        objSource.host.setValue(HOST);
        objSource.user.setValue(USER);
        objSource.password.setValue(PASS);
        objSource.protocol.setValue(SOSOptionTransferType.enuTransferTypes.smb);
        objVFS.authenticate(objSource);
    }

    @Test
    public void testMkdir() throws Exception {
        connect();
        authenticate();
        objVfsClient.rmdir(REMOTE_BASE_PATH + "test1");
        objVfsClient.mkdir(REMOTE_BASE_PATH + "test1");
        ISOSVirtualFile objVF = objVfsClient.getFileHandle(REMOTE_BASE_PATH + "test1");
        logger.info(objVF.getFileSize());
        objVfsClient.disconnect();
    }

    @Test
    public void testMkdirMultiple() throws Exception {
        connect();
        authenticate();
        objVfsClient.rmdir(REMOTE_BASE_PATH + "test1");
        objVfsClient.mkdir(REMOTE_BASE_PATH + "test1/test2/test3/");
        objVfsClient.disconnect();
    }

    @Test
    public void testRmdirString() throws Exception {
        connect();
        authenticate();
        objVfsClient.mkdir(REMOTE_BASE_PATH + "test1/test2/test3/");
        objVfsClient.rmdir(REMOTE_BASE_PATH + "test1/test2/test3/");
        objVfsClient.disconnect();
    }

    @Test
    public void testSize() throws Exception {
        connect();
        authenticate();
        logger.info(objVfsClient.getFileSize(REMOTE_BASE_PATH + "BVG.pdf"));
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
        logger.info("Size of Targetfile = " + intFileSize);
        objVfsClient.delete(strTargetFileName);
        objVfsClient.disconnect();
    }

    @Test
    public void testPutFileStringOutputStream() throws Exception {
        connect();
        authenticate();
        String hallo = "hallo";
        String strTargetFileName = REMOTE_BASE_PATH + "out.test.txt";
        OutputStream out = objVfsClient.getOutputStream(strTargetFileName);
        out.write(hallo.getBytes());
        out.flush();
        out.close();
        ISOSVirtualFile objVF = objVfsClient.getFileHandle(strTargetFileName);
        long intFileSize = objVF.getFileSize();
        logger.info("Size of Targetfile = " + intFileSize);
        objVfsClient.disconnect();
        assertEquals(intFileSize, hallo.length());
    }

    @Test
    public void testCd() throws Exception {
        connect();
        authenticate();
        objVfsClient.changeWorkingDirectory("/test");
        objVfsClient.disconnect();
    }

    @Test
    public void testChangeWorkingDirectory() throws Exception {
        testCd();
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
        objVfsClient.login(USER, PASS);
        objVfsClient.disconnect();
    }

    @Test
    public void testDisconnect() throws Exception {
        connect();
        objVfsClient.login(USER, PASS);
        objVfsClient.disconnect();
    }

    @Test
    public void testGetReplyString() throws Exception {
        connect();
        objVfsClient.login(USER, PASS);
        logger.info("Replay = " + objVfsClient.getReplyString());
        objVfsClient.disconnect();
    }

    @Test
    public void testIsConnected() throws Exception {
        connect();
        logger.debug("IS CONNECTED = " + objVfsClient.isConnected());
        objVfsClient.disconnect();
    }

    @Test
    public void testLogout() throws Exception {
        connect();
        objVfsClient.login(USER, PASS);
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
    public void testGetHandler() throws Exception {
        connect();
        authenticate();
        logger.debug("HANDLER = " + objVfsClient.getHandler());
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
        logger.info(objVfsClient.getModificationTime(REMOTE_BASE_PATH + "sos-net-src.zip"));
        objVfsClient.disconnect();
    }

    @Test
    public void testGetFilelist() throws Exception {
        connect();
        authenticate();
        String[] result = objVfsClient.getFilelist(REMOTE_BASE_PATH, "\\.pdf$", 0, true, null);
        for (String element : result) {
            logger.info(element);
        }
        objVfsClient.disconnect();
    }

}