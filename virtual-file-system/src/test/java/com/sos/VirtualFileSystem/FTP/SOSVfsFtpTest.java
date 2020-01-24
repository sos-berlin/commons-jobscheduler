package com.sos.VirtualFileSystem.FTP;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSVfsFtpTest.class);
    private SOSFTPOptions objOptions = null;
    private ISOSVFSHandler objVFS = null;
    private ISOSVfsFileTransfer ftpClient = null;
    private final String strTestFileName = "text.txt";
    private final String strTestPathName = "R:/nobackup/junittests/testdata/JADE";
    private String constrSettingsTestFile = strTestPathName + "/SOSDEx-test.ini";

    @Before
    public void setUp() throws Exception {
        objOptions = new SOSFTPOptions();
        objVFS = VFSFactory.getHandler(objOptions.protocol.getValue());
        ftpClient = (ISOSVfsFileTransfer) objVFS;
    }

    @Test
    public void testIsIncludeDirective() {
        boolean flgR = objOptions.isIncludeDirective("include");
        Assert.assertTrue("include", flgR);
        flgR = objOptions.isIncludeDirective("source_include");
        Assert.assertTrue("source_include", flgR);
    }

    @Test
    public void testGetIncludePrefix() {
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
        assertEquals("", "rename", objOptions.operation.getValue());
    }

    @Test
    public void testHashMapSettings() throws Exception {
        HashMap<String, String> objHsh = new HashMap<String, String>();
        objHsh.put("source_host", "wilma.sos");
        objHsh.put("target_host", "tux.sos");
        objOptions = new SOSFTPOptions();
        objOptions.setAllOptions(objHsh);
        assertEquals("", "wilma.sos", objOptions.getConnectionOptions().getSource().host.getValue());
        assertEquals("", "tux.sos", objOptions.getConnectionOptions().getTarget().host.getValue());
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testIniFile1() throws Exception {
        LOGGER.info("*********************************************** SOSVfsFtpTest::CreateIniFile******************");
        createIniFile();
        objOptions.settings.setValue(constrSettingsTestFile);
        objOptions.profile.setValue("globals");
        objOptions.readSettingsFile();
        Assert.assertEquals("User ID", "kb", objOptions.user.getValue());
        Assert.assertEquals("password", "kb", objOptions.password.getValue());
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testIniFileUsingCmdLine() throws Exception {
        LOGGER.info("*********************************************** SOSVfsFtpTest::CreateIniFile******************");
        String[] strCmdLineParameters = new String[] { "-settings=" + constrSettingsTestFile, "-profile=globals" };
        createIniFile();
        objOptions.commandLineArgs(strCmdLineParameters);
        Assert.assertEquals("User ID", "kb", objOptions.user.getValue());
        Assert.assertEquals("password", "kb", objOptions.password.getValue());
    }

    @Test(expected = com.sos.JSHelper.Exceptions.JobSchedulerException.class)
    @Ignore("Test set to Ignore for later examination")
    public void testIniFile2() throws Exception {
        LOGGER.info("*********************************************** SOSVfsFtpTest::testIniFile2******************");
        createIniFile();
        objOptions.settings.setValue(constrSettingsTestFile);
        objOptions.profile.setValue("include-TestTest");
        objOptions.readSettingsFile();
        Assert.assertEquals("User ID", "kb", objOptions.user.getValue());
        Assert.assertEquals("password", "kb", objOptions.password.getValue());
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testIniFile3() throws Exception {
        LOGGER.info("*********************************************** SOSVfsFtpTest::testIniFile3******************");
        createIniFile();
        objOptions.settings.setValue(constrSettingsTestFile);
        objOptions.profile.setValue("include-Test");
        objOptions.readSettingsFile();
        objOptions.localDir.setValue(".");
        Assert.assertEquals("User ID", "kb", objOptions.user.getValue());
        Assert.assertEquals("password", "kb", objOptions.password.getValue());
        Assert.assertEquals("Hostname", "hostFromInclude1", objOptions.host.getValue());
        Assert.assertEquals("port", 88, objOptions.port.value());
        Assert.assertEquals("protocol", "scp", objOptions.protocol.getValue());
        objOptions.checkMandatory();
    }

    @Test(expected = com.sos.JSHelper.Exceptions.JobSchedulerException.class)
    @Ignore("Test set to Ignore for later examination")
    public void testIniFile4() throws Exception {
        LOGGER.info("*********************************************** SOSVfsFtpTest::testIniFile4******************");
        createIniFile();
        objOptions.settings.setValue(constrSettingsTestFile);
        objOptions.profile.setValue("include-TestWithNonexistenceInclude");
        objOptions.readSettingsFile();
        Assert.assertEquals("User ID", "kb", objOptions.user.getValue());
        Assert.assertEquals("password", "kb", objOptions.password.getValue());
    }

    @Test
    @Ignore("Test set to Ignore for later examination")
    public void testIniFile5() throws Exception {
        LOGGER.info("*********************************************** SOSVfsFtpTest::testIniFile5******************");
        createIniFile();
        objOptions.settings.setValue(constrSettingsTestFile);
        objOptions.profile.setValue("substitute-Test");
        objOptions.readSettingsFile();
        String strComputerName = System.getenv("computername");
        Assert.assertEquals("User ID", System.getenv("username"), objOptions.user.getValue());
        Assert.assertEquals("Hostname", strComputerName, objOptions.host.getValue());
        Assert.assertEquals("Hostnameon Target ", strComputerName + "-abc", objOptions.getConnectionOptions().getTarget().hostName.getValue());
    }

    private void createIniFile() throws Exception {
        JSFile objIni = new JSFile(constrSettingsTestFile);
        if (objIni.exists()) {
            return;
        }
        objIni.writeLine("[globals]");
        objIni.writeLine("user=kb");
        objIni.writeLine("password=kb");
        objIni.writeLine("[include1]");
        objIni.writeLine("host=hostFromInclude1");
        objIni.writeLine("[include2]");
        objIni.writeLine("port=88");
        objIni.writeLine("[include3]");
        objIni.writeLine("protocol=scp");
        objIni.writeLine("[include-Test]");
        objIni.writeLine("include=include1,include2,include3");
        objIni.writeLine("[include-TestWithNonexistenceInclude]");
        objIni.writeLine("include=include1,includeabcd2,include3");
        objIni.writeLine("[substitute-Test]");
        objIni.writeLine("user=${USERNAME}");
        objIni.writeLine("host=${COMPUTERNAME}");
        objIni.writeLine("cannotsubstitutet=${waltraut}");
        objIni.writeLine("target_host=${host}-abc");
        objIni.writeLine("alternate_target_host=${host}-abc");
    }

    @Test(expected = java.lang.Exception.class)
    public void testConnect() throws Exception {
        System.setProperty("AddFTPProtocol", "true");
        objOptions.host.setValue("wilma.sos");
        objOptions.user.setValue("kbxsy");
        objOptions.passiveMode.setTrue();
        objVFS.connect(objOptions);
    }

    @Test
    public void testHttpProxyConnect() throws Exception {
        SOSConnection2OptionsAlternate options = objOptions.getConnectionOptions().getSource();
        options.host.setValue("wilma.sos");
        options.port.value(SOSOptionPortNumber.getStandardFTPPort());
        options.user.setValue("kb");
        options.password.setValue("kb");
        options.protocol.setValue("ftp");
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
        ftpClient.disconnect();
    }

    @Test
    public void testSocks5ProxyConnect() throws Exception {
        SOSConnection2OptionsAlternate options = objOptions.getConnectionOptions().getSource();
        options.host.setValue("wilma.sos");
        options.port.value(SOSOptionPortNumber.getStandardFTPPort());
        options.user.setValue("kb");
        options.password.setValue("kb");
        options.protocol.setValue("ftp");
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
        ftpClient.disconnect();
    }

    @Test(expected = com.sos.JSHelper.Exceptions.JobSchedulerException.class)
    public void testConnectFailed() throws Exception {
        objOptions.host.setValue("wilmaxxx.sos");
        objVFS.getOptions(objOptions);
        objVFS.connect(objOptions);
    }

    @Test(expected = com.sos.JSHelper.Exceptions.JobSchedulerException.class)
    public void testConnectAlternateFailed() throws Exception {
        objOptions.host.setValue("wilmaxxx.sos");
        objOptions.alternativeHost.setValue("kwkwkwk.sos");
        objVFS.getOptions(objOptions);
        objVFS.connect(objOptions);
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
        objOptions.host.setValue("8of9.sos");
        objOptions.host.setValue("wilma.sos");
        objOptions.user.setValue("kb");
        objOptions.password.setValue("kb");
        objVFS.connect(objOptions);
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
        objVFS.executeCommand("PORT 127,0,0,1,6,81");
        objVFS.executeCommand("LIST");
        objVFS.executeCommand("MLSD");
    }

    @Test
    public void testAuthenticate() throws Exception {
        objOptions.host.setValue("wilma.sos");
        objVFS.connect(objOptions);
        String strR = ftpClient.getReplyString();
        assertEquals("Connect message", "220 (vsFTPd 2.0.1)", strR.substring(0, strR.length() - 2));
        objOptions.user.setValue("kb");
        objOptions.password.setValue("kb");
        objVFS.authenticate(objOptions);
        strR = ftpClient.getReplyString();
        objVFS.closeSession();
        strR = ftpClient.getReplyString();
        assertEquals("Login message", "221 Goodbye.", strR.substring(0, strR.length() - 2));
        objVFS.closeConnection();
    }

    private void authenticate() throws Exception {
        objOptions.host.setValue("wilma.sos");
        objVFS.connect(objOptions);
        String strR = ftpClient.getReplyString();
        if (strR != null) {
            assertEquals("Connect message", "220 (vsFTPd 2.0.1)", strR.substring(0, strR.length() - 2));
        }
        objOptions.user.setValue("kb");
        objOptions.password.setValue("kb");
        objVFS.authenticate(objOptions);
        ftpClient.passive();
    }

    private void createTestFile() {
        JSFile objFile = new JSFile(strTestPathName + strTestFileName);
        objFile.deleteOnExit();
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
        objOptions.host.setValue("wilma.sos");
        objVFS.connect(objOptions);
        String strR = ftpClient.getReplyString();
        assertEquals("Connect message", "220 (vsFTPd 2.0.1)", strR.substring(0, strR.length() - 2));
        objOptions.user.setValue("kb");
        objOptions.password.setValue("kb");
        objVFS.authenticate(objOptions);
        strR = ftpClient.getReplyString();
        ftpClient.putFile(strTestPathName + strTestFileName, strTestFileName);
        objVFS.closeSession();
        strR = ftpClient.getReplyString();
        assertEquals("Logout message", "221 Goodbye.", strR.substring(0, strR.length() - 2));
        objVFS.closeConnection();
    }

    @Test
    public void messageTest() {
        String strM = SOSVfsMessageCodes.SOSVfs_E_0010.get();
        LOGGER.debug(strM);
        objOptions.locale.setLocale(Locale.ENGLISH);
        strM = SOSVfsMessageCodes.SOSVfs_E_0010.get();
        LOGGER.debug(strM);
        objOptions.locale.setLocale(Locale.GERMAN);
        strM = SOSVfsMessageCodes.SOSVfs_E_0010.get();
        LOGGER.debug(strM);
        objOptions.locale.setLocale(Locale.FRENCH);
        strM = SOSVfsMessageCodes.SOSVfs_E_0010.get();
        LOGGER.debug(strM);
    }

}