package com.sos.VirtualFileSystem.FTP;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import sos.net.mail.options.SOSSmtpMailOptions;

import com.sos.JSHelper.Logging.Log4JHelper;
import com.sos.VirtualFileSystem.Options.SOSConnection2Options;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;
import com.sos.VirtualFileSystem.Options.SOSFTPOptions;
import com.sos.VirtualFileSystem.Options.keepass4j.SOSCredentialStoreOptions;

public class SOSFTPOptionsTest {
	@SuppressWarnings("unused") private final String conClassName = this.getClass().getSimpleName();
	@SuppressWarnings("unused") private static final String conSVNVersion = "$Id$";
	@SuppressWarnings("unused") private static final Logger logger = Logger.getLogger(SOSFTPOptionsTest.class);
	
	private static Log4JHelper	objLogger				= null;

	@BeforeClass public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass public static void tearDownAfterClass() throws Exception {
	}

	@Before public void setUp() throws Exception {
		Log4JHelper.flgUseJobSchedulerLog4JAppender = false;
		objLogger = new Log4JHelper("./log4j.properties"); //$NON-NLS-1$
		objLogger.setLevel(Level.TRACE);

	}

	@After public void tearDown() throws Exception {
	}

	@Test public void testCheckMandatory() {
		// fail("Not yet implemented");
	}

	@Test public void testCommandLineArgsStringArray() {
		// fail("Not yet implemented");
	}

	@Test public void testSetAllOptionsHashMapOfStringString() {
		// fail("Not yet implemented");
	}

	@Test public void testGetPost_Command() {
		// fail("Not yet implemented");
	}

	@Test public void testSetPost_Command() {
		// fail("Not yet implemented");
	}

	@Test public void testGetPre_Command() {
		// fail("Not yet implemented");
	}

	@Test public void testSetPre_Command() {
		// fail("Not yet implemented");
	}

	@Test public void testGetCheckServerFeatures() {
		// fail("Not yet implemented");
	}

	@Test public void testSetCheckServerFeatures() {
		// fail("Not yet implemented");
	}

	@Test public void testGetPollKeepConnection() {
		// fail("Not yet implemented");
	}

	@Test public void testSetPollKeepConnection() {
		// fail("Not yet implemented");
	}

	@Test public void testGetFileNameEncoding() {
		// fail("Not yet implemented");
	}

	@Test public void testSetFileNameEncoding() {
		// fail("Not yet implemented");
	}

	@Test public void testGetControlEncoding() {
		// fail("Not yet implemented");
	}

	@Test public void testSetControlEncoding() {
		// fail("Not yet implemented");
	}

	@Test public void testGetHistoryEntries() {
		// fail("Not yet implemented");
	}

	@Test public void testSetHistoryEntries() {
		// fail("Not yet implemented");
	}

	@Test public void testGetSendTransferHistory() {
		// fail("Not yet implemented");
	}

	@Test public void testSetSendTransferHistory() {
		// fail("Not yet implemented");
	}

	@Test public void testGetScheduler_Transfer_Method() {
		// fail("Not yet implemented");
	}

	@Test public void testSetScheduler_Transfer_Method() {
		// fail("Not yet implemented");
	}

	@Test public void testGetPreFtpCommands() {
		// fail("Not yet implemented");
	}

	@Test public void testSetPreFtpCommands() {
		// fail("Not yet implemented");
	}

	@Test public void testSOSFTPOptions() {
		// fail("Not yet implemented");
	}

	@Test public void testProtocolCommandListener() throws Exception {
		HashMap<String, String> objH = new HashMap<String, String>();
		objH.put("source_Protocol_Command_Listener", "true");
		objH.put("Protocol_Command_Listener", "true");
		SOSFTPOptions objO = new SOSFTPOptions(objH);
		assertTrue("ProtocolCommandListeneris not true", objO.getConnectionOptions().Source().ProtocolCommandListener.value());
	}


	@Test public void testSOSCredentialStore1() throws Exception {
		HashMap<String, String> objH = new HashMap<String, String>();
		objH.put("source_dir", "source_dir");
		objH.put("alternative_source_dir", "alternate_source_dir");
		objH.put("alternative_source_user", "alternate_user");
		objH.put("source_use_credential_Store", "true");
		objH.put("source_CredentialStore_FileName", "./keepassX-test.kdb");
		objH.put("source_CredentialStore_KeyPath", "source_KeyPath");
//		objH.put("source_CredentialStore_KeyFileName", "./testing-key.key");
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
			logger.debug("objSource.UserName.Value() = " + objSource.user.Value());
			logger.debug("objSource.passwrod.Value() = " + objSource.password.Value());
			logger.debug("objSource.getAlternativeOptions().Directory.Value() = " + objSource.getAlternativeOptions().Directory.Value());
			logger.debug("objSource.dirtyString() = " + objSource.dirtyString());
			logger.debug("objSource.getAlternativeOptions().dirtyString() = " + objSource.getAlternativeOptions().dirtyString());
			logger.debug("testSOSCredentialStore1 " + objCS.dirtyString());
			assertEquals("source_CredentialStore_use_credential_Store", "./keepassX-test.kdb", objCS.CredentialStore_FileName.Value());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test public void testGetValuePairs() throws Exception {
		HashMap<String, String> objH = new HashMap<String, String>();
		objH.put("source_Protocol_Command_Listener", "true");
		objH.put("Protocol_Command_Listener", "true");
		SOSFTPOptions objO = new SOSFTPOptions(objH);
		assertTrue("ProtocolCommandListeneris not true", objO.getConnectionOptions().Source().ProtocolCommandListener.value());
		String strT = objO.getOptionsAsKeyValuePairs();
		System.out.println(strT);
	}

	@Test public void testGetMailOptions() throws Exception {
		HashMap<String, String> objH = new HashMap<String, String>();
		SOSSmtpMailOptions objM = new SOSSmtpMailOptions();
		objH.put(objM.SMTPHost.getShortKey(), "smtp.sos");
		SOSFTPOptions objO = new SOSFTPOptions(objH);
		assertEquals("host", "smtp.sos", objO.getMailOptions().SMTPHost.Value());
	}

	@Test// http://www.sos-berlin.com/jira/browse/SOSFTP-149
	public void testReplaceReplacing() throws Exception {
		HashMap<String, String> objH = new HashMap<String, String>();
		objH.put("replacing", ".*");
		SOSFTPOptions objO = new SOSFTPOptions(objH);
		SOSConnection2OptionsAlternate objSO = objO.Source();
		assertEquals("replacing", ".*", objO.replacing.Value());
		assertEquals("replacing", "", objSO.replacing.Value());
	}

	@Test
	public void testReplaceVars ()  {
		HashMap<String, String> objH = new HashMap<String, String>();
		objH.put("source_dir", "sourceDir");
		objH.put("source_protocol", "WebDav");
		objH.put("user", "Willi");
		objH.put("password", "password");
		objH.put("dirname", "myDearName");
		try {
			SOSFTPOptions objO = new SOSFTPOptions(objH);
			System.out.println(objO.DirtyString());
			String strReplaceIn = "--- %{source_protocol} --- %{dirname} --- %{TargetFileName} --- %{source_dir} ---  %{user} --- %{password} --- ${date} --- %{date} --- ${time} --- %{time} ---";
			strReplaceIn = objO.replaceVars(strReplaceIn);
			System.out.println(strReplaceIn);
//			Properties objPr = objO.getTextProperties();
//			for (Entry<Object, Object> objE : objPr.entrySet()) {
//				System.out.println(objE.getKey() + " = " + objE.getValue() + "\n");
//			} 
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	@Test public void testSOSFTPOptionsJSListener() {
		// fail("Not yet implemented");
	}

	@Test public void testIgnoreCertificateError() {
		SOSFTPOptions objO = new SOSFTPOptions();
		SOSConnection2OptionsAlternate objSO = objO.Source();
		objSO.IgnoreCertificateError.value(false);
		SOSConnection2OptionsAlternate objTarget = objO.Target();
		objTarget.IgnoreCertificateError.value(false);
		assertFalse("source_IgnoreCertificateError ", objSO.IgnoreCertificateError.value());
		assertFalse("target_IgnoreCertificateError", objTarget.IgnoreCertificateError.value());
	}

	@Test public void testIgnoreCertificateErrorWithProfile() throws Exception {
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

	
	@Test public final void testStoreOptionValues() {
		SOSFTPOptions objOC = new SOSFTPOptions();
		objOC.Locale.Value("en_EN");
		objOC.storeOptionValues();
		objOC = new SOSFTPOptions();
		objOC.initializeOptionValues();
		Assert.assertEquals("locale is wrong", "en_EN", objOC.Locale.Value());
	}

	@Test public void testSOSFTPOptionsHashMapOfStringString() {
		// fail("Not yet implemented");
	}

	@Test public void testIsAtomicTransfer() {
		// fail("Not yet implemented");
	}

	@Test public void testCommandLineArgsString() {
		// fail("Not yet implemented");
	}

	@Test public void testReadSettingsFile() {
		// fail("Not yet implemented");
	}

	@Test public void testSubstituteVariablesStringPropertiesStringString() {
		// fail("Not yet implemented");
	}

	@Test public void testCreateURI() {
		// fail("Not yet implemented");
	}

	@Test public void testOneOrMoreSingleFilesSpecified() {
		// fail("Not yet implemented");
	}

	@Test public void testGetDataTargetType() {
		// fail("Not yet implemented");
	}

	@Test public void testGetDataSourceType() {
		// fail("Not yet implemented");
	}

	@Test public void testDoNotOverwrite() {
		// fail("Not yet implemented");
	}

	@Test public void testTransferZeroByteFilesBoolean() {
		// fail("Not yet implemented");
	}

	@Test public void testTransferZeroByteFiles() {
		// fail("Not yet implemented");
	}

	@Test public void testSetZeroByteFilesStrict() {
		// fail("Not yet implemented");
	}

	@Test public void testIsZeroByteFilesStrict() {
		// fail("Not yet implemented");
	}

	@Test public void testSetZeroByteFilesRelaxed() {
		// fail("Not yet implemented");
	}

	@Test public void testIsZeroByteFilesRelaxed() {
		// fail("Not yet implemented");
	}

	@Test public void testGetConnectionOptions() {
		// fail("Not yet implemented");
	}

	@Test public void testSetConnectionOptions() {
		// fail("Not yet implemented");
	}

	@Test public void testIsReplaceReplacingInEffect() {
		// fail("Not yet implemented");
	}

	@Test public void testSource() {
		// fail("Not yet implemented");
	}

	@Test public void testTarget() {
		// fail("Not yet implemented");
	}

	@Test public void testNeedTargetClient() {
		// fail("Not yet implemented");
	}

	@Test public void testGetClone() {
		// fail("Not yet implemented");
	}

	@Test public void testClearJumpParameter() {
		// fail("Not yet implemented");
	}

	@Test public void testIsFilePollingEnabled() {
		// fail("Not yet implemented");
	}
}
