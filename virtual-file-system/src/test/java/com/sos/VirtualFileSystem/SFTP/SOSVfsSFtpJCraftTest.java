package com.sos.VirtualFileSystem.SFTP;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;

public class SOSVfsSFtpJCraftTest extends SOSVfsSFtpTest {

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        dynamicClassNameSource = "com.sos.VirtualFileSystem.SFTP.SOSVfsSFtpJCraft";
    }

    @After
    public void tearDown() throws Exception {
        ftpClient.disconnect();
    }

    @Override
    @Test
    public void testConnect() throws Exception {
        super.testConnect();
    }

    @Override
    @Test
    public void testHttpProxyConnect() throws Exception {
        super.testHttpProxyConnect();
    }

    @Override
    @Test
    public void testSocks5ProxyConnect() throws Exception {
        super.testSocks5ProxyConnect();
    }

    @Override
    @Test
    public void testConnectOpenSSH() throws Exception {
        super.testConnectOpenSSH();
    }

    @Override
    @Test(expected = com.sos.JSHelper.Exceptions.JobSchedulerException.class)
    @Ignore("Test set to Ignore for later examination")
    public void testConnectWithWrongPortNumber() throws Exception {
        super.testConnectWithWrongPortNumber();
    }

    @Override
    @Test
    public void testAuthenticate() throws Exception {
        super.testAuthenticate();
    }

    @Test
    public void testAuthenticateWithZlib() throws Exception {
        SOSConnection2OptionsAlternate objSource = objOptions.getConnectionOptions().Source();
        objSource.useZlibCompression.value(true);
        objSource.zlibCompressionLevel.value(2);
        super.testAuthenticate();
    }

    @Override
    @Test
    public void testMkdir() throws Exception {
        super.testMkdir();
    }

    @Override
    @Test
    public void testMkdirMultiple() throws Exception {
        super.testMkdirMultiple();
    }

    @Override
    @Test
    public void testNListBoolean() throws Exception {
        super.testNListBoolean();
    }

    @Override
    @Test
    public void testCd() throws Exception {
        super.testCd();
    }

    @Override
    @Test
    public void testChangeWorkingDirectory() throws Exception {
        super.testChangeWorkingDirectory();
    }

    @Override
    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testDelete() throws Exception {
        super.testDelete();
    }

    @Override
    @Test
    public void testLogin() throws Exception {
        super.testLogin();
    }

    @Override
    @Test
    public void testDisconnect() throws Exception {
        super.testDisconnect();
    }

    @Override
    @Test
    public void testGetReplyString() throws Exception {
        super.testGetReplyString();
    }

    @Override
    @Test
    public void testIsConnected() throws Exception {
        super.testIsConnected();
    }

    @Override
    @Test
    public void testLogout() throws Exception {
        super.testLogout();
    }

    @Override
    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testRename() throws Exception {
        super.testRename();
    }

    @Override
    @Test
    public void testGetHandler() throws Exception {
        super.testGetHandler();
    }

    @Override
    @Test
    public void testExecuteCommand() throws Exception {
        super.testExecuteCommand();
    }

    @Override
    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testGetFilelist() throws Exception {
        super.testGetFilelist();
    }

}
