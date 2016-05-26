package com.sos.VirtualFileSystem.SFTP;

import java.util.Vector;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Options.SOSOptionJadeOperation.enuJadeOperations;
import com.sos.JSHelper.Options.SOSOptionPortNumber;
import com.sos.JSHelper.Options.SOSOptionProxyProtocol;
import com.sos.VirtualFileSystem.FTP.SOSVfsFtpTest;
import com.sos.VirtualFileSystem.Factory.VFSFactory;
import com.sos.VirtualFileSystem.Interfaces.ISOSVFSHandler;
import com.sos.VirtualFileSystem.Interfaces.ISOSVfsFileTransfer;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;
import com.sos.VirtualFileSystem.Options.SOSFTPOptions;

/** @author KB */
public class SOSVfsSFtpTest extends JSToolBox {

    protected static final Logger LOGGER = Logger.getLogger(SOSVfsFtpTest.class);
    protected static final String LOCAL_BASE_PATH = "R:\\nobackup\\junittests\\testdata\\SFTP\\";
    protected static final String REMOTE_BASE_PATH = "/home/kb/";
    protected SOSFTPOptions objOptions = null;
    protected ISOSVFSHandler objVFS = null;
    protected ISOSVfsFileTransfer ftpClient = null;
    protected String dynamicClassNameSource = null;
    protected String dynamicClassNameTarget = null;

    @Before
    public void setUp() throws Exception {
        objOptions = new SOSFTPOptions();
        objOptions.protocol.setValue("sftp");
        objVFS = VFSFactory.getHandler(objOptions.protocol.getValue());
        ftpClient = (ISOSVfsFileTransfer) objVFS;
        dynamicClassNameSource = "com.sos.VirtualFileSystem.SFTP.SOSVfsSFtp";
        dynamicClassNameTarget = "com.sos.VirtualFileSystem.SFTP.SOSVfsSFtp";
    }

    @Test
    public void testConnect() throws Exception {
        objOptions.host.setValue("wilma.sos");
        objOptions.port.value(SOSOptionPortNumber.getStandardSFTPPort());
        SOSConnection2OptionsAlternate objSource = objOptions.getConnectionOptions().getSource();
        setDynamicClassNameSource(objSource);
        objSource.host.setValue("wilma.sos");
        objSource.port.value(SOSOptionPortNumber.getStandardSFTPPort());
        objSource.user.setValue("kb");
        objSource.protocol.setValue("sftp");
        objSource.sshAuthMethod.isPassword(true);
        objOptions.operation.setValue("send");
        objVFS = VFSFactory.getHandler(objOptions.protocol.getValue());
        ftpClient = (ISOSVfsFileTransfer) objVFS;
        objVFS.connect(objOptions.getConnectionOptions().getSource());
    }

    @Test
    public void testHttpProxyConnect() throws Exception {
        objOptions.host.setValue("wilma.sos");
        objOptions.port.value(SOSOptionPortNumber.getStandardSFTPPort());
        SOSConnection2OptionsAlternate options = objOptions.getConnectionOptions().getSource();
        setDynamicClassNameSource(options);
        options.host.setValue("wilma.sos");
        options.port.value(SOSOptionPortNumber.getStandardSFTPPort());
        options.user.setValue("kb");
        options.password.setValue("kb");
        options.protocol.setValue("sftp");
        options.sshAuthMethod.isPassword(true);
        options.proxyProtocol.setValue(SOSOptionProxyProtocol.Protocol.http.name());
        options.proxyHost.setValue("homer.sos");
        options.proxyPort.value(3128);
        options.proxyUser.setValue("proxy_user");
        options.proxyPassword.setValue("12345");
        objOptions.operation.setValue("send");
        objVFS = VFSFactory.getHandler(objOptions.protocol.getValue());
        ftpClient = (ISOSVfsFileTransfer) objVFS;
        objVFS.connect(objOptions.getConnectionOptions().getSource());
        objVFS.authenticate(options);
    }

    @Test
    public void testSocks5ProxyConnect() throws Exception {
        objOptions.host.setValue("wilma.sos");
        objOptions.port.value(SOSOptionPortNumber.getStandardSFTPPort());
        SOSConnection2OptionsAlternate options = objOptions.getConnectionOptions().getSource();
        setDynamicClassNameSource(options);
        options.host.setValue("wilma.sos");
        options.port.value(SOSOptionPortNumber.getStandardSFTPPort());
        options.user.setValue("kb");
        options.password.setValue("kb");
        options.protocol.setValue("sftp");
        options.sshAuthMethod.isPassword(true);
        options.proxyProtocol.setValue(SOSOptionProxyProtocol.Protocol.socks5.name());
        options.proxyHost.setValue("homer.sos");
        options.proxyPort.value(1080);
        options.proxyUser.setValue("sos");
        options.proxyPassword.setValue("sos");
        objOptions.operation.setValue("send");
        objVFS = VFSFactory.getHandler(objOptions.protocol.getValue());
        ftpClient = (ISOSVfsFileTransfer) objVFS;
        objVFS.connect(objOptions.getConnectionOptions().getSource());
        objVFS.authenticate(options);
    }

    private void setDynamicClassNameSource(final SOSConnection2OptionsAlternate objSource) {
        if (isNotEmpty(dynamicClassNameSource)) {
            objSource.loadClassName.setValue(dynamicClassNameSource);
        }
    }

    private void setDynamicClassNameTarget(final SOSConnection2OptionsAlternate objTarget) {
        if (isNotEmpty(dynamicClassNameTarget)) {
            objTarget.loadClassName.setValue(dynamicClassNameTarget);
        }
    }

    @Test
    public void testConnectOpenSSH() throws Exception {
        objOptions.host.setValue("wilma.sos");
        objOptions.port.value(SOSOptionPortNumber.getStandardSFTPPort());
        SOSConnection2OptionsAlternate objSource = objOptions.getConnectionOptions().getSource();
        setDynamicClassNameSource(objSource);
        objSource.host.setValue("wilma.sos");
        objSource.port.value(SOSOptionPortNumber.getStandardSFTPPort());
        objSource.protocol.setValue("sftp");
        objSource.sshAuthMethod.isPassword(false);
        objOptions.operation.setValue("send");
        objVFS = VFSFactory.getHandler(objOptions.protocol.getValue());
        ftpClient = (ISOSVfsFileTransfer) objVFS;
        objVFS.connect(objOptions.getConnectionOptions().getSource());
    }

    private void connect() throws RuntimeException, Exception {
        objOptions.host.setValue("wilma.sos");
        objOptions.port.value(SOSOptionPortNumber.getStandardSFTPPort());
        SOSConnection2OptionsAlternate objSource = objOptions.getConnectionOptions().getSource();
        setDynamicClassNameSource(objSource);
        objSource.host.setValue("wilma.sos");
        objSource.port.value(SOSOptionPortNumber.getStandardSFTPPort());
        objSource.user.setValue("kb");
        objSource.protocol.setValue("sftp");
        objSource.sshAuthMethod.isPassword(true);
        objOptions.operation.setValue("send");
        objVFS = VFSFactory.getHandler(objOptions.protocol.getValue());
        ftpClient = (ISOSVfsFileTransfer) objVFS;
        objVFS.connect(objOptions.getConnectionOptions().getSource());
    }

    @Test(expected = com.sos.JSHelper.Exceptions.JobSchedulerException.class)
    @Ignore("Test set to Ignore for later examination")
    public void testConnectWithWrongPortNumber() throws Exception {
        objOptions.host.setValue("wilma.sos");
        objOptions.port.value(45678);
        SOSConnection2OptionsAlternate objSource = objOptions.getConnectionOptions().getSource();
        setDynamicClassNameSource(objSource);
        objSource.host.setValue("wilma.sos");
        objSource.port.value(45678);
        objSource.user.setValue("kb");
        objSource.protocol.setValue("sftp");
        objSource.sshAuthMethod.isPassword(true);
        objOptions.operation.setValue(enuJadeOperations.send);
        objVFS = VFSFactory.getHandler(objOptions.protocol.getValue());
        ftpClient = (ISOSVfsFileTransfer) objVFS;
        objVFS.connect(objOptions.getConnectionOptions().getSource());
    }

    @Test
    public void testAuthenticate() throws Exception {
        testConnect();
        authenticate();
        ftpClient.disconnect();
    }

    private void authenticate() throws Exception {
        SOSConnection2OptionsAlternate objSource = objOptions.getConnectionOptions().getSource();
        objSource.host.setValue("wilma.sos");
        objSource.port.value(SOSOptionPortNumber.getStandardSFTPPort());
        objSource.user.setValue("kb");
        objSource.password.setValue("kb");
        objSource.protocol.setValue("sftp");
        objSource.sshAuthMethod.isPassword(true);
        objVFS.authenticate(objSource);
    }

    @Test
    public void testMkdir() throws Exception {
        connect();
        authenticate();
        ftpClient.mkdir("test1");
        ftpClient.rmdir("test1");
        ftpClient.disconnect();
    }

    @Test
    public void testMkdirMultiple() throws Exception {
        connect();
        authenticate();
        ftpClient.mkdir("test1/test2/test3/");
        ftpClient.rmdir("test1/test2/test3/");
        ftpClient.disconnect();
    }

    @Test
    public void testNListBoolean() throws Exception {
        connect();
        authenticate();
        ftpClient.changeWorkingDirectory("/home/re/Documents");
        Vector<String> v = ftpClient.nList(true);
        for (String item : v) {
            LOGGER.info("item = " + item);
        }
        ftpClient.disconnect();
    }

    @Test
    public void testCd() throws Exception {
        connect();
        authenticate();
        ftpClient.changeWorkingDirectory("/home/re");
        ftpClient.disconnect();
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
        ftpClient.put(LOCAL_BASE_PATH + "sos-net-src.zip", REMOTE_BASE_PATH + "tmp123.zip");
        ftpClient.delete(REMOTE_BASE_PATH + "tmp123.zip");
        ftpClient.disconnect();
    }

    @Test
    public void testLogin() throws Exception {
        connect();
        ftpClient.login("kb", "kb");
        ftpClient.disconnect();
    }

    @Test
    public void testDisconnect() throws Exception {
        connect();
        ftpClient.login("kb", "kb");
        ftpClient.disconnect();
    }

    @Test
    public void testGetReplyString() throws Exception {
        connect();
        ftpClient.login("kb", "kb");
        LOGGER.info("Replay = " + ftpClient.getReplyString());
        ftpClient.disconnect();
    }

    @Test
    public void testIsConnected() throws Exception {
        connect();
        LOGGER.debug("IS CONNECTED = " + ftpClient.isConnected());
        ftpClient.disconnect();
    }

    @Test
    public void testLogout() throws Exception {
        connect();
        ftpClient.login("kb", "kb");
        ftpClient.logout();
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testRename() throws Exception {
        connect();
        authenticate();
        ftpClient.put(LOCAL_BASE_PATH + "sos-net-src.zip", REMOTE_BASE_PATH + "tmp123.zip");
        ftpClient.rename(REMOTE_BASE_PATH + "tmp123.zip", REMOTE_BASE_PATH + "tmp1234.zip");
        ftpClient.delete(REMOTE_BASE_PATH + "tmp1234.zip");
        ftpClient.disconnect();
    }

    @Test
    public void testGetHandler() throws Exception {
        connect();
        authenticate();
        LOGGER.debug("HANDLER = " + ftpClient.getHandler());
        ftpClient.disconnect();
    }

    @Test
    public void testExecuteCommand() throws Exception {
        connect();
        authenticate();
        String lineSeparator = "\n";
        objVFS.executeCommand("cd /home/test" + lineSeparator + "cd /home/kb");
        ftpClient.disconnect();
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testGetFilelist() throws Exception {
        connect();
        authenticate();
        String[] result = ftpClient.getFilelist(REMOTE_BASE_PATH, "", 0, false, null);
        for (String element : result) {
            LOGGER.info(element);
        }
        ftpClient.disconnect();
    }

}