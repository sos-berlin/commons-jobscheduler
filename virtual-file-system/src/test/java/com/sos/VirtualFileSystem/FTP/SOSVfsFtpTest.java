package com.sos.VirtualFileSystem.FTP;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.sos.JSHelper.Options.SOSOptionPortNumber;
import com.sos.JSHelper.Options.SOSOptionProxyProtocol;
import com.sos.JSHelper.io.Files.JSFile;
import com.sos.VirtualFileSystem.Factory.VFSFactory;
import com.sos.VirtualFileSystem.Interfaces.ISOSVFSHandler;
import com.sos.VirtualFileSystem.Interfaces.ISOSVfsFileTransfer;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;
import com.sos.VirtualFileSystem.Options.SOSFTPOptions;
import com.sos.VirtualFileSystem.common.SOSVfsMessageCodes;
import com.sos.i18n.annotation.I18NResourceBundle;

/** @author KB */
@I18NResourceBundle(baseName = "SOSVirtualFileSystem", defaultLocale = "en")
public class SOSVfsFtpTest {

    private static final Logger LOGGER = Logger.getLogger(SOSVfsFtpTest.class);
    private SOSFTPOptions objOptions = null;
    private ISOSVFSHandler objVFS = null;
    private ISOSVfsFileTransfer ftpClient = null;
    private final String strTestFileName = "text.txt";
    private final String strTestPathName = "R:/nobackup/junittests/testdata/JADE";
    private String constrSettingsTestFile = strTestPathName + "/SOSDEx-test.ini";

    @Before
    public void setUp() throws Exception {
        objOptions = new SOSFTPOptions();
        objVFS = VFSFactory.getHandler(objOptions.protocol.Value());
        ftpClient = (ISOSVfsFileTransfer) objVFS;
    }

    @Test
    public void testisIncludeDirective() {
        boolean flgR = objOptions.isIncludeDirective("include");
        Assert.assertTrue("include", flgR);
        flgR = objOptions.isIncludeDirective("source_include");
        Assert.assertTrue("source_include", flgR);
    }

    @Test
    public void testgetIncludePrefix() {
        String strR = objOptions.getIncludePrefix("include");
        Assert.assertEquals("include", "", strR);
        strR = objOptions.getIncludePrefix("source_include");
        Assert.assertEquals("source_include", "source_", strR);
    }

    @Test
    public void testOptionOperation() throws Exception {
        HashMap<String, String> objHsh = new HashMap<String, String>();
        objHsh.put("operation", "rename");
        objOptions = new SOSFTPOptions(objHsh);
        assertEquals("", "rename", objOptions.operation.Value());
    }

    @Test
    public void testHashMapSettings() throws Exception {
        HashMap<String, String> objHsh = new HashMap<String, String>();
        objHsh.put("source_host", "wilma.sos");
        objHsh.put("target_host", "tux.sos");
        objOptions = new SOSFTPOptions();
        objOptions.setAllOptions(objHsh);
        assertEquals("", "wilma.sos", objOptions.getConnectionOptions().Source().host.Value());
        assertEquals("", "tux.sos", objOptions.getConnectionOptions().Target().host.Value());
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testIniFile1() throws Exception {
        LOGGER.info("*********************************************** SOSVfsFtpTest::CreateIniFile******************");
        CreateIniFile();
        objOptions.settings.Value(constrSettingsTestFile);
        objOptions.profile.Value("globals");
        objOptions.ReadSettingsFile();
        Assert.assertEquals("User ID", "kb", objOptions.user.Value());
        Assert.assertEquals("password", "kb", objOptions.password.Value());
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testIniFileUsingCmdLine() throws Exception {
        LOGGER.info("*********************************************** SOSVfsFtpTest::CreateIniFile******************");
        String[] strCmdLineParameters = new String[] { "-settings=" + constrSettingsTestFile, "-profile=globals" };
        CreateIniFile();
        objOptions.CommandLineArgs(strCmdLineParameters);
        Assert.assertEquals("User ID", "kb", objOptions.user.Value());
        Assert.assertEquals("password", "kb", objOptions.password.Value());
    }

    @Test(expected = com.sos.JSHelper.Exceptions.JobSchedulerException.class)
    @Ignore("Test set to Ignore for later examination")
    public void testIniFile2() throws Exception {
        LOGGER.info("*********************************************** SOSVfsFtpTest::testIniFile2******************");
        CreateIniFile();
        objOptions.settings.Value(constrSettingsTestFile);
        objOptions.profile.Value("include-TestTest");
        objOptions.ReadSettingsFile();
        Assert.assertEquals("User ID", "kb", objOptions.user.Value());
        Assert.assertEquals("password", "kb", objOptions.password.Value());
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testIniFile3() throws Exception {
        LOGGER.info("*********************************************** SOSVfsFtpTest::testIniFile3******************");
        CreateIniFile();
        objOptions.settings.Value(constrSettingsTestFile);
        objOptions.profile.Value("include-Test");
        objOptions.ReadSettingsFile();
        objOptions.local_dir.Value(".");
        Assert.assertEquals("User ID", "kb", objOptions.user.Value());
        Assert.assertEquals("password", "kb", objOptions.password.Value());
        Assert.assertEquals("Hostname", "hostFromInclude1", objOptions.host.Value());
        Assert.assertEquals("port", 88, objOptions.port.value());
        Assert.assertEquals("protocol", "scp", objOptions.protocol.Value());
        objOptions.CheckMandatory();
    }

    @Test(expected = com.sos.JSHelper.Exceptions.JobSchedulerException.class)
    @Ignore("Test set to Ignore for later examination")
    public void testIniFile4() throws Exception {
        LOGGER.info("*********************************************** SOSVfsFtpTest::testIniFile4******************");
        CreateIniFile();
        objOptions.settings.Value(constrSettingsTestFile);
        objOptions.profile.Value("include-TestWithNonexistenceInclude");
        objOptions.ReadSettingsFile();
        Assert.assertEquals("User ID", "kb", objOptions.user.Value());
        Assert.assertEquals("password", "kb", objOptions.password.Value());
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testIniFile5() throws Exception {
        LOGGER.info("*********************************************** SOSVfsFtpTest::testIniFile5******************");
        CreateIniFile();
        objOptions.settings.Value(constrSettingsTestFile);
        objOptions.profile.Value("substitute-Test");
        objOptions.ReadSettingsFile();
        String strComputerName = System.getenv("computername");
        Assert.assertEquals("User ID", System.getenv("username"), objOptions.user.Value());
        Assert.assertEquals("Hostname", strComputerName, objOptions.host.Value());
        Assert.assertEquals("Hostnameon Target ", strComputerName + "-abc", objOptions.getConnectionOptions().Target().HostName.Value());
    }

    private void CreateIniFile() throws Exception {
        JSFile objIni = new JSFile(constrSettingsTestFile);
        if (objIni.exists()) {
            return;
        }
        objIni.WriteLine("[globals]");
        objIni.WriteLine("user=kb");
        objIni.WriteLine("password=kb");
        objIni.WriteLine("[include1]");
        objIni.WriteLine("host=hostFromInclude1");
        objIni.WriteLine("[include2]");
        objIni.WriteLine("port=88");
        objIni.WriteLine("[include3]");
        objIni.WriteLine("protocol=scp");
        objIni.WriteLine("[include-Test]");
        objIni.WriteLine("include=include1,include2,include3");
        objIni.WriteLine("[include-TestWithNonexistenceInclude]");
        objIni.WriteLine("include=include1,includeabcd2,include3");
        objIni.WriteLine("[substitute-Test]");
        objIni.WriteLine("user=${USERNAME}");
        objIni.WriteLine("host=${COMPUTERNAME}");
        objIni.WriteLine("cannotsubstitutet=${waltraut}");
        objIni.WriteLine("target_host=${host}-abc");
        objIni.WriteLine("alternate_target_host=${host}-abc");
    }

    @Test(expected = java.lang.Exception.class)
    public void testConnect() throws Exception {
        System.setProperty("AddFTPProtocol", "true");
        objOptions.host.Value("wilma.sos");
        objOptions.user.Value("kbxsy");
        objOptions.passive_mode.setTrue();
        objVFS.Connect(objOptions);
    }

    @Test
    public void testHttpProxyConnect() throws Exception {
        SOSConnection2OptionsAlternate options = objOptions.getConnectionOptions().Source();
        options.host.Value("wilma.sos");
        options.port.value(SOSOptionPortNumber.getStandardFTPPort());
        options.user.Value("kb");
        options.password.Value("kb");
        options.protocol.Value("ftp");
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
        ftpClient.disconnect();
    }

    @Test
    public void testSocks5ProxyConnect() throws Exception {
        SOSConnection2OptionsAlternate options = objOptions.getConnectionOptions().Source();
        options.host.Value("wilma.sos");
        options.port.value(SOSOptionPortNumber.getStandardFTPPort());
        options.user.Value("kb");
        options.password.Value("kb");
        options.protocol.Value("ftp");
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
        ftpClient.disconnect();
    }

    @Test(expected = com.sos.JSHelper.Exceptions.JobSchedulerException.class)
    public void testConnectFailed() throws Exception {
        objOptions.host.Value("wilmaxxx.sos");
        objVFS.Options(objOptions);
        objVFS.Connect(objOptions);
    }

    @Test(expected = com.sos.JSHelper.Exceptions.JobSchedulerException.class)
    public void testConnectAlternateFailed() throws Exception {
        objOptions.host.Value("wilmaxxx.sos");
        objOptions.alternative_host.Value("kwkwkwk.sos");
        objVFS.Options(objOptions);
        objVFS.Connect(objOptions);
    }

    @Test
    public void testMkdir() throws Exception {
        String strPath = "/home/kb/test1/willi/";
        testConnect();
        authenticate();
        for (int i = 0; i <= 10; i++) {
            ftpClient.mkdir(strPath);
        }
        assertTrue("Directory must exist", ftpClient.isDirectory(strPath));
        ftpClient.rmdir("test1/willi");
        assertFalse("Directory should have been deleted ", ftpClient.isDirectory("test1/willi"));
        ftpClient.disconnect();
    }

    @Test
    public void testMkdir2() throws Exception {
        String strPath = "test1/willi/";
        testConnect();
        authenticate();
        for (int i = 0; i <= 10; i++) {
            ftpClient.mkdir(strPath);
        }
        assertTrue("Directory must exist", ftpClient.isDirectory(strPath));
        ftpClient.rmdir(strPath);
        assertFalse("Directory should have been deleted ", ftpClient.isDirectory(strPath));
        ftpClient.disconnect();
    }

    @Test
    public void testMkdirMultiple() throws Exception {
        String strPath = "test1/test2/test3/";
        testConnect();
        authenticate();
        try {
            ftpClient.rmdir(strPath);
        } catch (Exception e) {
            // ignore the error
        }
        assertFalse("Directory should have been deleted ", ftpClient.isDirectory(strPath));
        for (int i = 0; i < 10; i++) {
            ftpClient.mkdir(strPath);
            assertTrue("Directory must exist", ftpClient.isDirectory(strPath));
        }
        ftpClient.rmdir(strPath);
        assertFalse("Directory should have been deleted ", ftpClient.isDirectory(strPath));
        ftpClient.disconnect();
    }

    @Test
    public void testExecuteCommand() throws Exception {
        objOptions.host.Value("8of9.sos");
        objOptions.host.Value("wilma.sos");
        objOptions.user.Value("kb");
        objOptions.password.Value("kb");
        objVFS.Connect(objOptions);
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
        objVFS.ExecuteCommand("PORT 127,0,0,1,6,81");
        objVFS.ExecuteCommand("LIST");
        objVFS.ExecuteCommand("MLSD");
    }

    @Test
    public void testAuthenticate() throws Exception {
        objOptions.host.Value("wilma.sos");
        objVFS.Connect(objOptions);
        String strR = ftpClient.getReplyString();
        assertEquals("Connect message", "220 (vsFTPd 2.0.1)", strR.substring(0, strR.length() - 2));
        objOptions.user.Value("kb");
        objOptions.password.Value("kb");
        objVFS.Authenticate(objOptions);
        strR = ftpClient.getReplyString();
        objVFS.CloseSession();
        strR = ftpClient.getReplyString();
        assertEquals("Login message", "221 Goodbye.", strR.substring(0, strR.length() - 2));
        objVFS.CloseConnection();
    }

    private void authenticate() throws Exception {
        objOptions.host.Value("wilma.sos");
        objVFS.Connect(objOptions);
        String strR = ftpClient.getReplyString();
        if (strR != null) {
            assertEquals("Connect message", "220 (vsFTPd 2.0.1)", strR.substring(0, strR.length() - 2));
        }
        objOptions.user.Value("kb");
        objOptions.password.Value("kb");
        objVFS.Authenticate(objOptions);
        ftpClient.passive();
    }

    private void CreateTestFile() {
        JSFile objFile = new JSFile(strTestPathName + strTestFileName);
        objFile.deleteOnExit();
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
        objOptions.host.Value("wilma.sos");
        objVFS.Connect(objOptions);
        String strR = ftpClient.getReplyString();
        assertEquals("Connect message", "220 (vsFTPd 2.0.1)", strR.substring(0, strR.length() - 2));
        objOptions.user.Value("kb");
        objOptions.password.Value("kb");
        objVFS.Authenticate(objOptions);
        strR = ftpClient.getReplyString();
        ftpClient.putFile(strTestPathName + strTestFileName, strTestFileName);
        objVFS.CloseSession();
        strR = ftpClient.getReplyString();
        assertEquals("Logout message", "221 Goodbye.", strR.substring(0, strR.length() - 2));
        objVFS.CloseConnection();
    }

    @Test
    public void MessageTest() {
        String strM = SOSVfsMessageCodes.SOSVfs_E_0010.get();
        LOGGER.debug(strM);
        objOptions.Locale.setLocale(Locale.ENGLISH);
        strM = SOSVfsMessageCodes.SOSVfs_E_0010.get();
        LOGGER.debug(strM);
        objOptions.Locale.setLocale(Locale.GERMAN);
        strM = SOSVfsMessageCodes.SOSVfs_E_0010.get();
        LOGGER.debug(strM);
        objOptions.Locale.setLocale(Locale.FRENCH);
        strM = SOSVfsMessageCodes.SOSVfs_E_0010.get();
        LOGGER.debug(strM);
    }

}