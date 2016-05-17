package com.sos.VirtualFileSystem.FTP;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import sos.net.mail.options.SOSSmtpMailOptions;

import com.sos.CredentialStore.Options.SOSCredentialStoreOptions;
import com.sos.VirtualFileSystem.Options.SOSConnection2Options;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;
import com.sos.VirtualFileSystem.Options.SOSFTPOptions;

public class SOSFTPOptionsTest {

    private static final Logger LOGGER = Logger.getLogger(SOSFTPOptionsTest.class);

    @Test
    public void testProtocolCommandListener() throws Exception {
        HashMap<String, String> objH = new HashMap<String, String>();
        objH.put("source_Protocol_Command_Listener", "true");
        objH.put("Protocol_Command_Listener", "true");
        SOSFTPOptions objO = new SOSFTPOptions(objH);
        assertTrue("ProtocolCommandListeneris not true", objO.getConnectionOptions().Source().protocolCommandListener.value());
    }

    @Test
    public void testSOSCredentialStore1() throws Exception {
        HashMap<String, String> objH = new HashMap<String, String>();
        objH.put("source_dir", "source_dir");
        objH.put("alternative_source_dir", "alternate_source_dir");
        objH.put("alternative_source_user", "alternate_user");
        objH.put("source_use_credential_Store", "true");
        objH.put("source_CredentialStore_FileName", "./keepassX-test.kdb");
        objH.put("source_CredentialStore_KeyPath", "source_KeyPath");
        objH.put("source_CredentialStore_password", "testing");
        objH.put("source_CredentialStore_Key_Path", "testserver/testserver2");
        objH.put("target_dir", "target_dir");
        objH.put("target_use_credential_Store", "true");
        objH.put("target_CredentialStore_FileName", "./keepassX-test.kdb");
        objH.put("target_CredentialStore_KeyPath", "target_KeyPath");
        objH.put("target_CredentialStore_password", "testing");
        objH.put("target_CredentialStore_Key_Path", "testserver/testserver2");
        try {
            SOSFTPOptions objO = new SOSFTPOptions(objH);
            SOSConnection2Options objCO = objO.getConnectionOptions();
            SOSConnection2OptionsAlternate objSource = objCO.Source();
            SOSCredentialStoreOptions objCS = objSource.getCredentialStore();
            LOGGER.debug("objSource.UserName.Value() = " + objSource.user.Value());
            LOGGER.debug("objSource.passwrod.Value() = " + objSource.password.Value());
            LOGGER.debug("objSource.getAlternativeOptions().Directory.Value() = " + objSource.getAlternativeOptions().directory.Value());
            LOGGER.debug("objSource.dirtyString() = " + objSource.dirtyString());
            LOGGER.debug("objSource.getAlternativeOptions().dirtyString() = " + objSource.getAlternativeOptions().dirtyString());
            LOGGER.debug("testSOSCredentialStore1 " + objCS.dirtyString());
            assertEquals("source_CredentialStore_use_credential_Store", "./keepassX-test.kdb", objCS.credentialStoreFileName.Value());
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Test
    public void testGetValuePairs() throws Exception {
        HashMap<String, String> objH = new HashMap<String, String>();
        objH.put("source_Protocol_Command_Listener", "true");
        objH.put("Protocol_Command_Listener", "true");
        SOSFTPOptions objO = new SOSFTPOptions(objH);
        assertTrue("ProtocolCommandListeneris not true", objO.getConnectionOptions().Source().protocolCommandListener.value());
        String strT = objO.getOptionsAsKeyValuePairs();
        LOGGER.info(strT);
    }

    @Test
    public void testGetMailOptions() throws Exception {
        HashMap<String, String> objH = new HashMap<String, String>();
        SOSSmtpMailOptions objM = new SOSSmtpMailOptions();
        objH.put(objM.SMTPHost.getShortKey(), "smtp.sos");
        SOSFTPOptions objO = new SOSFTPOptions(objH);
        assertEquals("host", "smtp.sos", objO.getMailOptions().SMTPHost.Value());
    }

    @Test
    public void testReplaceReplacing() throws Exception {
        HashMap<String, String> objH = new HashMap<String, String>();
        objH.put("replacing", ".*");
        SOSFTPOptions objO = new SOSFTPOptions(objH);
        SOSConnection2OptionsAlternate objSO = objO.Source();
        assertEquals("replacing", ".*", objO.replacing.Value());
        assertEquals("replacing", "", objSO.replacing.Value());
    }

    @Test
    public void testReplaceVars() {
        HashMap<String, String> objH = new HashMap<String, String>();
        objH.put("source_dir", "sourceDir");
        objH.put("source_protocol", "WebDav");
        objH.put("user", "Willi");
        objH.put("password", "password");
        objH.put("dirname", "myDearName");
        try {
            SOSFTPOptions objO = new SOSFTPOptions(objH);
            LOGGER.info(objO.DirtyString());
            String strReplaceIn = "--- %{source_protocol} --- %{dirname} --- %{TargetFileName} --- %{source_dir} ---  %{user} --- %{password} --- ${date} --- %{date} --- ${time} --- %{time} ---";
            strReplaceIn = objO.replaceVars(strReplaceIn);
            LOGGER.info(strReplaceIn);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Test
    public void testIgnoreCertificateError() {
        SOSFTPOptions objO = new SOSFTPOptions();
        SOSConnection2OptionsAlternate objSO = objO.Source();
        objSO.IgnoreCertificateError.value(false);
        SOSConnection2OptionsAlternate objTarget = objO.Target();
        objTarget.IgnoreCertificateError.value(false);
        assertFalse("source_IgnoreCertificateError ", objSO.IgnoreCertificateError.value());
        assertFalse("target_IgnoreCertificateError", objTarget.IgnoreCertificateError.value());
    }

    @Test
    public void testIgnoreCertificateErrorWithProfile() throws Exception {
        HashMap<String, String> objH = new HashMap<String, String>();
        objH.put("source_IgnoreCertificateError", "false");
        objH.put("target_IgnoreCertificateError", "false");
        SOSFTPOptions objO = new SOSFTPOptions(objH);
        SOSConnection2OptionsAlternate objSO = objO.Source();
        objSO.IgnoreCertificateError.value(false);
        SOSConnection2OptionsAlternate objTarget = objO.Target();
        objTarget.IgnoreCertificateError.value(false);
        assertFalse("source_IgnoreCertificateError ", objSO.IgnoreCertificateError.value());
        assertFalse("target_IgnoreCertificateError", objTarget.IgnoreCertificateError.value());
    }

    @Test
    public final void testStoreOptionValues() {
        SOSFTPOptions objOC = new SOSFTPOptions();
        objOC.Locale.Value("en_EN");
        objOC.storeOptionValues();
        objOC = new SOSFTPOptions();
        objOC.initializeOptionValues();
        Assert.assertEquals("locale is wrong", "en_EN", objOC.Locale.Value());
    }

}
