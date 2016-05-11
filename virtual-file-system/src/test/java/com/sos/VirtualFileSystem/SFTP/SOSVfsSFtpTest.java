package com.sos.VirtualFileSystem.SFTP;

import java.util.Vector;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
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

    protected static Logger logger = Logger.getLogger(SOSVfsFtpTest.class);
    protected final String LOCAL_BASE_PATH = "R:\\nobackup\\junittests\\testdata\\SFTP\\";
    protected final String REMOTE_BASE_PATH = "/home/kb/";
    protected SOSFTPOptions objOptions = null;
    protected ISOSVFSHandler objVFS = null;
    protected ISOSVfsFileTransfer ftpClient = null;
    protected String dynamicClassNameSource = null;
    protected String dynamicClassNameTarget = null;

    @Before
    public void setUp() throws Exception {
        objOptions = new SOSFTPOptions();
        objOptions.protocol.Value("sftp");
        objVFS = VFSFactory.getHandler(objOptions.protocol.Value());
        ftpClient = (ISOSVfsFileTransfer) objVFS;
        dynamicClassNameSource = "com.sos.VirtualFileSystem.SFTP.SOSVfsSFtp";
        dynamicClassNameTarget = "com.sos.VirtualFileSystem.SFTP.SOSVfsSFtp";
    }

    @Test
    public void testConnect() throws Exception {
        objOptions.host.Value("wilma.sos");
        objOptions.port.value(SOSOptionPortNumber.getStandardSFTPPort());
        SOSConnection2OptionsAlternate objSource = objOptions.getConnectionOptions().Source();
        setDynamicClassNameSource(objSource);
        objSource.host.Value("wilma.sos");
        objSource.port.value(SOSOptionPortNumber.getStandardSFTPPort());
        objSource.user.Value("kb");
        objSource.protocol.Value("sftp");
        objSource.ssh_auth_method.isPassword(true);
        objOptions.operation.Value("send");
        objVFS = VFSFactory.getHandler(objOptions.protocol.Value());
        ftpClient = (ISOSVfsFileTransfer) objVFS;
        objVFS.Connect(objOptions.getConnectionOptions().Source());
    }

    @Test
    public void testHttpProxyConnect() throws Exception {
        objOptions.host.Value("wilma.sos");
        objOptions.port.value(SOSOptionPortNumber.getStandardSFTPPort());
        SOSConnection2OptionsAlternate options = objOptions.getConnectionOptions().Source();
        setDynamicClassNameSource(options);
        options.host.Value("wilma.sos");
        options.port.value(SOSOptionPortNumber.getStandardSFTPPort());
        options.user.Value("kb");
        options.password.Value("kb");
        options.protocol.Value("sftp");
        options.ssh_auth_method.isPassword(true);
        options.proxy_protocol.Value(SOSOptionProxyProtocol.Protocol.http.name());
        options.proxy_host.Value("homer.sos");
        options.proxy_port.value(3128);
        options.proxy_user.Value("proxy_user");
        options.proxy_password.Value("12345");
        objOptions.operation.Value("send");
        objVFS = VFSFactory.getHandler(objOptions.protocol.Value());
        ftpClient = (ISOSVfsFileTransfer) objVFS;
        objVFS.Connect(objOptions.getConnectionOptions().Source());
        objVFS.Authenticate(options);
    }

    @Test
    public void testSocks5ProxyConnect() throws Exception {
        objOptions.host.Value("wilma.sos");
        objOptions.port.value(SOSOptionPortNumber.getStandardSFTPPort());
        SOSConnection2OptionsAlternate options = objOptions.getConnectionOptions().Source();
        setDynamicClassNameSource(options);
        options.host.Value("wilma.sos");
        options.port.value(SOSOptionPortNumber.getStandardSFTPPort());
        options.user.Value("kb");
        options.password.Value("kb");
        options.protocol.Value("sftp");
        options.ssh_auth_method.isPassword(true);
        options.proxy_protocol.Value(SOSOptionProxyProtocol.Protocol.socks5.name());
        options.proxy_host.Value("homer.sos");
        options.proxy_port.value(1080);
        options.proxy_user.Value("sos");
        options.proxy_password.Value("sos");
        objOptions.operation.Value("send");
        objVFS = VFSFactory.getHandler(objOptions.protocol.Value());
        ftpClient = (ISOSVfsFileTransfer) objVFS;
        objVFS.Connect(objOptions.getConnectionOptions().Source());
        objVFS.Authenticate(options);
    }

    private void setDynamicClassNameSource(final SOSConnection2OptionsAlternate objSource) {
        if (isNotEmpty(dynamicClassNameSource)) {
            objSource.loadClassName.Value(dynamicClassNameSource);
        }
    }

    @Test
    public void testConnectOpenSSH() throws Exception {
        objOptions.host.Value("wilma.sos");
        objOptions.port.value(SOSOptionPortNumber.getStandardSFTPPort());
        SOSConnection2OptionsAlternate objSource = objOptions.getConnectionOptions().Source();
        setDynamicClassNameSource(objSource);
        objSource.host.Value("wilma.sos");
        objSource.port.value(SOSOptionPortNumber.getStandardSFTPPort());
        objSource.protocol.Value("sftp");
        objSource.ssh_auth_method.isPassword(false);
        objOptions.operation.Value("send");
        objVFS = VFSFactory.getHandler(objOptions.protocol.Value());
        ftpClient = (ISOSVfsFileTransfer) objVFS;
        objVFS.Connect(objOptions.getConnectionOptions().Source());
    }

    private void connect() throws RuntimeException, Exception {
        objOptions.host.Value("wilma.sos");
        objOptions.port.value(SOSOptionPortNumber.getStandardSFTPPort());
        SOSConnection2OptionsAlternate objSource = objOptions.getConnectionOptions().Source();
        setDynamicClassNameSource(objSource);
        objSource.host.Value("wilma.sos");
        objSource.port.value(SOSOptionPortNumber.getStandardSFTPPort());
        objSource.user.Value("kb");
        objSource.protocol.Value("sftp");
        objSource.ssh_auth_method.isPassword(true);
        objOptions.operation.Value("send");
        objVFS = VFSFactory.getHandler(objOptions.protocol.Value());
        ftpClient = (ISOSVfsFileTransfer) objVFS;
        objVFS.Connect(objOptions.getConnectionOptions().Source());
    }

    @Test(expected = com.sos.JSHelper.Exceptions.JobSchedulerException.class)
    @Ignore("Test set to Ignore for later examination")
    public void testConnectWithWrongPortNumber() throws Exception {
        objOptions.host.Value("wilma.sos");
        objOptions.port.value(45678);
        SOSConnection2OptionsAlternate objSource = objOptions.getConnectionOptions().Source();
        setDynamicClassNameSource(objSource);
        objSource.host.Value("wilma.sos");
        objSource.port.value(45678);
        objSource.user.Value("kb");
        objSource.protocol.Value("sftp");
        objSource.ssh_auth_method.isPassword(true);
        objOptions.operation.Value(enuJadeOperations.send);
        objVFS = VFSFactory.getHandler(objOptions.protocol.Value());
        ftpClient = (ISOSVfsFileTransfer) objVFS;
        objVFS.Connect(objOptions.getConnectionOptions().Source());
    }

    @Test
    public void testAuthenticate() throws Exception {
        testConnect();
        authenticate();
        ftpClient.disconnect();
    }

    private void authenticate() throws Exception {
        SOSConnection2OptionsAlternate objSource = objOptions.getConnectionOptions().Source();
        objSource.host.Value("wilma.sos");
        objSource.port.value(SOSOptionPortNumber.getStandardSFTPPort());
        objSource.user.Value("kb");
        objSource.password.Value("kb");
        objSource.protocol.Value("sftp");
        objSource.ssh_auth_method.isPassword(true);
        objVFS.Authenticate(objSource);
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
            logger.info("item = " + item);
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
        logger.info("Replay = " + ftpClient.getReplyString());
        ftpClient.disconnect();
    }

    @Test
    public void testIsConnected() throws Exception {
        connect();
        logger.debug("IS CONNECTED = " + ftpClient.isConnected());
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
        logger.debug("HANDLER = " + ftpClient.getHandler());
        ftpClient.disconnect();
    }

    @Test
    public void testExecuteCommand() throws Exception {
        connect();
        authenticate();
        String lineSeparator = "\n";
        objVFS.ExecuteCommand("cd /home/test" + lineSeparator + "cd /home/kb");
        ftpClient.disconnect();
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testGetFilelist() throws Exception {
        connect();
        authenticate();
        String[] result = ftpClient.getFilelist(REMOTE_BASE_PATH, "", 0, false, null);
        for (String element : result) {
            logger.info(element);
        }
        ftpClient.disconnect();
    }

}