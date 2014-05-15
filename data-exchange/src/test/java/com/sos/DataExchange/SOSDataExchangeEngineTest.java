package com.sos.DataExchange;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import junit.framework.Assert;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.DataExchange.Options.JADEOptions;
import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Logging.Log4JHelper;
import com.sos.JSHelper.Options.SOSOptionAuthenticationMethod;
import com.sos.JSHelper.Options.SOSOptionAuthenticationMethod.enuAuthenticationMethods;
import com.sos.JSHelper.Options.SOSOptionJSTransferMethod.enuJSTransferModes;
import com.sos.JSHelper.Options.SOSOptionJadeOperation;
import com.sos.JSHelper.Options.SOSOptionJadeOperation.enuJadeOperations;
import com.sos.JSHelper.Options.SOSOptionPortNumber;
import com.sos.JSHelper.Options.SOSOptionRegExp;
import com.sos.JSHelper.Options.SOSOptionTransferType;
import com.sos.JSHelper.Options.SOSOptionTransferType.enuTransferTypes;
import com.sos.JSHelper.io.Files.JSFile;
import com.sos.VirtualFileSystem.DataElements.SOSFileListEntry;
import com.sos.VirtualFileSystem.Factory.VFSFactory;
import com.sos.VirtualFileSystem.Interfaces.ISOSVFSHandler;
import com.sos.VirtualFileSystem.Interfaces.ISOSVfsFileTransfer;
import com.sos.VirtualFileSystem.Options.SOSConnection2Options;
import com.sos.VirtualFileSystem.Options.SOSConnection2OptionsAlternate;

/**
 * @author KB
 *
 */
public class SOSDataExchangeEngineTest extends JSToolBox {
	private static final String	conHostNameWILMA_SOS	= "wilma.sos";
	private static final String	conHostName8OF9_SOS		= "8of9.sos";
	private final String		conClassName			= "SOSFTPCommandSendTest";
	private final static Logger		logger					= Logger.getLogger(SOSDataExchangeEngineTest.class);
	private static Log4JHelper	objLogger				= null;
	private JADEOptions		objOptions				= null;
	private final String		strSettingsFileName		= "./scripts/sosdex_settings.ini";
//	protected String			strSettingsFile			= "R:/backup/sos/java/development/SOSDataExchange/examples/jade_settings.ini";
	protected String			strSettingsFile			= "./examples/jade_settings.ini";
	private ISOSVFSHandler		objVFS					= null;
	@SuppressWarnings("unused")
	private ISOSVfsFileTransfer	ftpClient				= null;
	private String				strTestFileName			= "text.txt";
	private final String		strTestPathName			= "R:/backup/sos/java/junittests/testdata/JADE/";
	private final String		strKBHome				= "/home/kb/";
	String						constrSettingsTestFile	= strTestPathName + "/jade-test.ini";
	@SuppressWarnings("unused")
	private final String		strAPrefix				= "~~";
	protected String			dynamicClassNameSource	= null;

	public SOSDataExchangeEngineTest() {
		//
	}

	/**
	 * \brief setUpBeforeClass
	 *
	 * \details
	 *
	 * \return void
	 *
	 * @throws java.lang.Exception
	 */
	@BeforeClass public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * \brief tearDownAfterClass
	 *
	 * \details
	 *
	 * \return void
	 *
	 * @throws java.lang.Exception
	 */
	@AfterClass public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * \brief setUp
	 *
	 * \details
	 *
	 * \return void
	 *
	 * @throws java.lang.Exception
	 */
	@Before public void setUp() throws Exception {
		String strLog4JFileName = "./log4j.properties";
		String strT = new File(strLog4JFileName).getAbsolutePath();
		objLogger = new Log4JHelper(strLog4JFileName);
		objLogger.setLevel(Level.DEBUG);
		// objLogger.setLevel(Level.INFO);
		logger.info("log4j properties filename = " + strT);
		//		objOptions = new JADEOptions();
		objOptions = new JADEOptions();
		objOptions.ApplicationName.Value("JADE");
		objOptions.ApplicationDocuUrl.Value("http://www.sos-berlin.com/doc/en/jade/JADE Parameter Reference.pdf");
		dynamicClassNameSource = "com.sos.VirtualFileSystem.SFTP.SOSVfsSFtpJCraft";
		objVFS = VFSFactory.getHandler(objOptions.protocol.Value());
		ftpClient = (ISOSVfsFileTransfer) objVFS;
	}

	/**
	 * \brief tearDown
	 *
	 * \details
	 *
	 * \return void
	 *
	 * @throws java.lang.Exception
	 */
	@After public void tearDown() throws Exception {
	}

	private void CreateTestFile() {
		CreateTestFile(strTestFileName);
	}

	private void CreateTestFile(final String pstrFileName) {
		JSFile objFile = new JSFile(strTestPathName, pstrFileName);
		// objFile.deleteOnExit();
		try {
			objFile.WriteLine("This is a simple Testfile. nothing else.");
			objFile.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void CreateBigTestFile(final String pstrFileName, final int fileSize) {
		JSFile objFile = new JSFile(strTestPathName, pstrFileName);
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(objFile);
			out.write(new byte[fileSize]);
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if (out != null) {
				try {
					out.flush();
					out.close();
				}
				catch (Exception x) {
				}
			}
		}
	}

	private void CreateTestFiles(final int intNumberOfFiles) {
		for (int i = 0; i < intNumberOfFiles; i++) {
			JSFile objFile = new JSFile(strTestPathName + "Masstest" + i + ".txt");
			// objFile.deleteOnExit();
			try {
				for (int j = 0; j < 10; j++) {
					objFile.WriteLine("This is a simple Testfile, created for the masstest. nothing else.");
				}
				objFile.close();
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	class WriteFiles4Polling implements Runnable {
		@Override public void run() {
			JSFile objFile = null;
			for (int i = 0; i < 15; i++) {
				logger.debug(i);
				objFile = new JSFile(strTestPathName + "/test-" + i + ".poll");
				try {
					Thread.sleep(5000);
					objFile.Write(i + ": This is a test");
					objFile.WriteLine(i + ": This is a test");
					objFile.WriteLine(i + ": This is a test");
					objFile.WriteLine(i + ": This is a test");
				}
				catch (IOException e) {
					e.printStackTrace();
				}
				catch (InterruptedException e) {
					e.printStackTrace();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
				try {
					objFile.close();
					objFile = null;
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
			logger.debug("finished");
		}
	}
	private boolean	flgUseFilePath	= false;

	private void sendWithPolling(final boolean flgForceFiles, final boolean flgCreateFiles) throws Exception {
		final String conMethodName = conClassName + "::sendWithPolling";
		objOptions = new JADEOptions();
		objOptions.host.Value(conHostNameWILMA_SOS);
		objOptions.protocol.Value(enuTransferTypes.sftp);
		assertEquals("sftp", "sftp", objOptions.protocol.Value());
		objOptions.port.value(SOSOptionPortNumber.conPort4SSH);
		objOptions.user.Value("test");
		objOptions.password.Value("12345");
		objOptions.auth_method.Value(enuAuthenticationMethods.password);
		if (flgUseFilePath) {
			objOptions.file_path.Value("R:/backup/sos/java/junittests/testdata/SOSDataExchange/test-0.poll");
		}
		else {
			objOptions.FileNamePatternRegExp.Value("^.*\\.poll$");
			objOptions.poll_minfiles.value(1);
		}
		objOptions.local_dir.Value(strTestPathName);
		objOptions.operation.Value("send");
		objOptions.log_filename.Value(objOptions.TempDir() + "test.log");
		objOptions.profile.Value(conMethodName);
		objOptions.CreateSecurityHash.value(false);
		objOptions.poll_interval.Value("0:30"); //
		objOptions.PollingDuration.Value("05:00"); // for 5 minutes
		// objOptions.force_files.value(flgForceFiles);
		objOptions.ErrorOnNoDataFound.value(flgForceFiles);
		objOptions.remove_files.value(true);
		logger.info(objOptions.dirtyString());
		if (flgCreateFiles == true) {
			Thread thread = new Thread(new WriteFiles4Polling()); // Create and start the thread
			thread.start();
		}
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		objJadeEngine.Execute();
		objJadeEngine.Logout();
	} // private void sendWithPolling

	@Test (expected=com.sos.JSHelper.Exceptions.JobSchedulerException.class) 
	public void testEmptyCommandLineParameter () throws Exception {
		try {
			objOptions.AllowEmptyParameterList.setFalse();
			objOptions.CommandLineArgs(new String[] {});
		}
		catch (Exception e) {
			throw e;
		}
	}
	
	@Test public void testEmptyCommandLineParameter2 () {
		try {
			objOptions.AllowEmptyParameterList.setTrue();
			objOptions.CommandLineArgs(new String[] {});
		}
		catch (Exception e) {
			throw e;
		}
	}
	
	@Test public void testSendWithPolling() throws Exception {
		final String conMethodName = conClassName + "::testSendWithPolling";
		logMethodName(conMethodName);
		sendWithPolling(true, true);
	}

	@Test(
			expected = com.sos.JSHelper.Exceptions.JobSchedulerException.class) public void testSendWithPolling0Files() throws Exception {
		final String conMethodName = conClassName + "::testSendWithPolling0Files";
		logMethodName(conMethodName);
		sendWithPolling(true, false);
	}

	@Test public void testSendWithPollingAndForce() throws Exception {
		final String conMethodName = conClassName + "::testSendWithPollingAndForce";
		logMethodName(conMethodName);
		sendWithPolling(false, false);
	}

	@Test public void testSendWithPollingUsingFilePath() throws Exception {
		final String conMethodName = conClassName + "::testSendWithPollingUsingFilePath";
		logMethodName(conMethodName);
		flgUseFilePath = true;
		sendWithPolling(true, true);
	}

	@Test(
			expected = com.sos.JSHelper.Exceptions.JobSchedulerException.class) public void testSendWithPolling0FilesUsingFilePath() throws Exception {
		final String conMethodName = conClassName + "::testSendWithPolling0FilesUsingFilePath";
		logMethodName(conMethodName);
		flgUseFilePath = true;
		sendWithPolling(true, false);
	}

	private void logMethodName(final String pstrName) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::logMethodName";
		// logMethodName(conMethodName);
	} // private void logMethodName

	/**
	 * Test method for {@link JadeEngine.net.sosftp.SOSFTPCommandSend#SOSFTPCommandSend(java.util.Properties)}.
	 */
	// @Test
	public void testSOSFTPCommandSendProperties() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link JadeEngine.net.sosftp.SOSFTPCommandSend#SOSFTPCommandSend(java.util.HashMap)}.
	 */
	// @Test
	public void testSOSFTPCommandSendHashMapOfStringString() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for {@link JadeEngine.net.sosftp.SOSFTPCommandSend#Options()}.
	 */
	// @Test
	public void testOptions() {
		fail("Not yet implemented");
	}

	@Test public void testSendServer2Server() throws Exception {
		final String conMethodName = conClassName + "::testSendServer2Server";
		CreateTestFile();
		logMethodName(conMethodName);
		SOSConnection2Options objConn = objOptions.getConnectionOptions();
		objConn.Source().HostName.Value(conHostNameWILMA_SOS);
		objConn.Source().port.value(21);
		objConn.Source().protocol.Value(SOSOptionTransferType.enuTransferTypes.ftp);
		objConn.Source().user.Value("kb");
		objConn.Source().password.Value("kb");
		objConn.Target().HostName.Value(conHostName8OF9_SOS);
		objConn.Target().port.value(21);
		objConn.Target().protocol.Value(SOSOptionTransferType.enuTransferTypes.ftp);
		objConn.Target().user.Value("kb");
		objConn.Target().password.Value("kb");
		// objOptions.user.Value("kb");
		// objOptions.password.Value("kb");
		objOptions.file_path.Value(strTestFileName);
		// objOptions.local_dir.Value("/home/kb/");
		objOptions.SourceDir.Value("/home/kb");
		// objOptions.remote_dir.Value("/kb/");
		objOptions.TargetDir.Value("/kb");
		objOptions.operation.Value("copy");
		objOptions.CheckMandatory();
		// logger.info(objOptions.toString());
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		objJadeEngine.Execute();
		boolean flgResult = objJadeEngine.objDataTargetClient.getFileHandle(objOptions.TargetDir.Value() + strTestFileName).FileExists();
		assertTrue("File must exist", flgResult);
		objJadeEngine.Logout();
	}

	@Test public void testSendServer2ServerWithJCraft() throws Exception {
		final String conMethodName = conClassName + "::testSendServer2ServerWithJCraft";
		CreateTestFile();
		logMethodName(conMethodName);
		SOSConnection2Options objConn = objOptions.getConnectionOptions();
		objConn.Source().HostName.Value(conHostNameWILMA_SOS);
		objConn.Source().port.value(SOSOptionPortNumber.getStandardSFTPPort());
		objConn.Source().protocol.Value(SOSOptionTransferType.enuTransferTypes.sftp);
		objConn.Source().user.Value("kb");
		objConn.Source().password.Value("kb");
		objConn.Source().ssh_auth_method.Value("password");
		objConn.Source().loadClassName.Value("com.sos.VirtualFileSystem.SFTP.SOSVfsSFtpJCraft");
		objConn.Target().HostName.Value(conHostNameWILMA_SOS);
		objConn.Target().port.value(SOSOptionPortNumber.getStandardSFTPPort());
		objConn.Target().protocol.Value(SOSOptionTransferType.enuTransferTypes.sftp);
		objConn.Target().user.Value("sos");
		objConn.Target().password.Value("sos");
		objConn.Target().ssh_auth_method.Value("password");
		objConn.Target().loadClassName.Value("com.sos.VirtualFileSystem.SFTP.SOSVfsSFtpJCraft");
		objOptions.file_path.Value(strTestFileName);
		objOptions.SourceDir.Value("/home/kb");
		objOptions.TargetDir.Value("/home/sos");
		objOptions.operation.Value("copy");
		objOptions.CheckMandatory();
		// logger.info(objOptions.toString());
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		objJadeEngine.Execute();
		boolean flgResult = objJadeEngine.objDataTargetClient.getFileHandle(objOptions.TargetDir.Value() + strTestFileName).FileExists();
		assertTrue("File must exist", flgResult);
		objJadeEngine.Logout();
	}

	@Test public void testSendServer2ServerMultiple() throws Exception {
		final String conMethodName = conClassName + "::testSendServer2Server";
		CreateTestFile();
		logMethodName(conMethodName);
		SOSConnection2Options objConn = objOptions.getConnectionOptions();
		objConn.Source().HostName.Value(conHostNameWILMA_SOS);
		objConn.Source().port.value(21);
		objConn.Source().protocol.Value(SOSOptionTransferType.enuTransferTypes.ftp);
		objConn.Source().user.Value("kb");
		objConn.Source().password.Value("kb");
		objConn.Target().HostName.Value(conHostName8OF9_SOS);
		objConn.Target().port.value(21);
		objConn.Target().protocol.Value(SOSOptionTransferType.enuTransferTypes.ftp);
		objConn.Target().user.Value("kb");
		objConn.Target().password.Value("kb");
		// objOptions.user.Value("kb");
		// objOptions.password.Value("kb");
		// objOptions.file_path.Value(strTestFileName);
		objOptions.file_spec.Value("^.*\\.txt$");
		// objOptions.local_dir.Value("/home/kb/");
		objOptions.SourceDir.Value("/home/kb");
		// objOptions.remote_dir.Value("/kb/");
		objOptions.TargetDir.Value("/kb");
		objOptions.operation.Value("copy");
		objOptions.CheckMandatory();
		// logger.info(objOptions.toString());
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		objJadeEngine.Execute();
		boolean flgResult = objJadeEngine.objDataTargetClient.getFileHandle(objOptions.TargetDir.Value() + strTestFileName).FileExists();
		assertTrue("File must exist", flgResult);
		objJadeEngine.Logout();
	}

	@Test public void testSendFtp2SFtp() throws Exception {
		final String conMethodName = conClassName + "::testSendFtp2SFtp";
		logMethodName(conMethodName);
		CreateTestFile();
		SOSConnection2Options objConn = objOptions.getConnectionOptions();
		SOSConnection2OptionsAlternate objS = objConn.Source();
		objS.HostName.Value(conHostName8OF9_SOS);
		objS.port.value(SOSOptionPortNumber.getStandardFTPPort());
		objS.protocol.Value("ftp");
		objS.user.Value("sos");
		objS.password.Value("sos");
		objOptions.local_dir.Value("/");
		SOSConnection2OptionsAlternate objT = objConn.Target();
		objT.HostName.Value(conHostNameWILMA_SOS);
		objT.port.value(SOSOptionPortNumber.getStandardSFTPPort());
		objT.ssh_auth_method.isPassword(true);
		objT.protocol.Value("sftp");
		objT.user.Value("test");
		objT.password.Value("12345");
		String strTestDir = "/home/test/";
		objOptions.remote_dir.Value(strTestDir);
		objOptions.TargetDir.Value(strTestDir);
		strTestFileName = "wilma.sh";
		objOptions.file_path.Value(strTestFileName);
		objOptions.operation.Value("copy");
		// logger.info(objOptions.toString());
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		objJadeEngine.Execute();
		boolean flgResult = objJadeEngine.objDataTargetClient.getFileHandle(strTestDir + strTestFileName).FileExists();
		assertTrue("File must exist " + strTestFileName, flgResult);
		objJadeEngine.Logout();
	}

	/**
	 * Test method for {@link JadeEngine.net.sosftp.SOSFTPCommandSend#send()}.
	 * @throws Exception
	 *
	 */
	@Test public void testSend() throws Exception {
		final String conMethodName = conClassName + "::testSend";
		logMethodName(conMethodName);
		CreateTestFile();
		objOptions = new JADEOptions();
		objOptions.host.Value(conHostNameWILMA_SOS);
		objOptions.protocol.Value(enuTransferTypes.sftp);
		assertEquals("sftp", "sftp", objOptions.protocol.Value());
		objOptions.port.value(SOSOptionPortNumber.conPort4SSH);
		objOptions.user.Value("test");
		objOptions.password.Value("12345");
		objOptions.auth_method.Value(enuAuthenticationMethods.password);
		objOptions.file_path.Value(strTestFileName);
		objOptions.local_dir.Value(strTestPathName);
		objOptions.operation.Value("send");
		objOptions.log_filename.Value("c:/temp/test.log");
		objOptions.profile.Value(conMethodName);
		// logger.info(objOptions.toString());
		setOptions4BackgroundService();
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		objJadeEngine.Execute();
		boolean flgResult = objJadeEngine.objDataTargetClient.getFileHandle("/home/kb/" + strTestFileName).FileExists();
		assertTrue("File must exist", flgResult);
		objJadeEngine.Logout();
	}

	@Test public void testSendWithPrePostCommands() throws Exception {
		final String conMethodName = conClassName + "::testSendWithPrePostCommands";
		logMethodName(conMethodName);
		CreateTestFile();
		objOptions = new JADEOptions();
		objOptions.host.Value(conHostNameWILMA_SOS);
		objOptions.protocol.Value(enuTransferTypes.sftp);
		assertEquals("sftp", "sftp", objOptions.protocol.Value());
		objOptions.port.value(SOSOptionPortNumber.conPort4SSH);
		objOptions.user.Value("kb");
		objOptions.password.Value("kb");
		objOptions.auth_method.Value(enuAuthenticationMethods.password);
		objOptions.file_spec.Value("^.*\\.txt$");
		// objOptions.file_path.Value(strTestFileName);
		objOptions.local_dir.Value(strTestPathName);
		objOptions.operation.Value("send");
		objOptions.log_filename.Value("c:/temp/test.log");
		objOptions.profile.Value(conMethodName);
		// logger.info(objOptions.toString());
		objOptions.PreFtpCommands.Value("rm -f t.1");
		objOptions.Target().Post_Command.Value("echo 'File: $TargetFileName' >> t.1;cat $TargetFileName >> t.1;rm -f $TargetFileName");
		objOptions.Target().Pre_Command.Value("touch $TargetFileName");
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		objJadeEngine.Execute();
		boolean flgResult = objJadeEngine.objDataTargetClient.getFileHandle("/home/kb/" + strTestFileName).FileExists();
		assertFalse("File must not exist", flgResult);
		objJadeEngine.Logout();
	}

	@Test public void testSendWithPrePostCommands2() throws Exception {
		final String conMethodName = conClassName + "::testSendWithPrePostCommands";
		logMethodName(conMethodName);
		CreateTestFile();
		objOptions = new JADEOptions();
		objOptions.host.Value("local");
		objOptions.protocol.Value(enuTransferTypes.local);
		objOptions.file_spec.Value("^.*\\.txt$");
		objOptions.local_dir.Value(strTestPathName);
		objOptions.remote_dir.Value("c:/temp/a");
		objOptions.operation.Value("send");
		objOptions.log_filename.Value("c:/temp/test.log");
		objOptions.profile.Value(conMethodName);
		// logger.info(objOptions.toString());
		//		objOptions.PreFtpCommands.Value("rm -f t.1");
		//		objOptions.Target().Post_Command.Value("echo 'File: $TargetFileName' >> t.1;cat $TargetFileName >> t.1;rm -f $TargetFileName");
		//		objOptions.Target().Pre_Command.Value("touch $TargetFileName");
		objOptions.PreFtpCommands.Value("del %{remote_dir}/t.1");
		objOptions.Target().Post_Command.Value("echo 'File: $TargetFileName' >> c:\\temp\\a\\t.1 & type $TargetFileName >> c:\\temp\\a\\t.1 & del $TargetFileName");
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		objJadeEngine.Execute();
		boolean flgResult = objJadeEngine.objDataTargetClient.getFileHandle("/home/kb/" + strTestFileName).FileExists();
		assertFalse("File must not exist", flgResult);
		objJadeEngine.Logout();
		logger.debug(objOptions.getOptionsAsCommandLine());
	}

	@Test public void testSendRegExpAsFileName() throws Exception {
		final String conMethodName = conClassName + "::testSendRegExpAsFileName";
		logMethodName(conMethodName);
		CreateTestFile();
		objOptions = new JADEOptions();
		objOptions.host.Value(conHostNameWILMA_SOS);
		objOptions.user.Value("kb");
		objOptions.password.Value("kb");
		strTestFileName = "test.txt";
		objOptions.file_spec.Value(strTestFileName);
		objOptions.local_dir.Value(strTestPathName);
		objOptions.operation.Value("send");
		objOptions.verbose.value(9);
		// logger.info(objOptions.toString());
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		objJadeEngine.Execute();
		boolean flgResult = objJadeEngine.objDataTargetClient.getFileHandle("/home/kb/" + strTestFileName).FileExists();
		assertTrue("File must exist", flgResult);
		objJadeEngine.Logout();
	}

	@Test public void testSendUsingReplacement() throws Exception {
		final String conMethodName = conClassName + "::testSendUsingReplacement";
		logMethodName(conMethodName);
		sendUsingReplacement("^t", "a");
	}

	@Test public void testSendUsingReplacement2() throws Exception {
		final String conMethodName = conClassName + "::testSendUsingReplacement2";
		logMethodName(conMethodName);
		sendUsingReplacement(".*", "renamed_[filename:]");
	}

	@Test public void testSendUsingReplacement3() throws Exception {
		final String conMethodName = conClassName + "::testSendUsingReplacement3";
		String strSaveTestfileName = strTestFileName;
		strTestFileName = "a" + strTestFileName;
		String strRenamedTestfileName = "renamed_" + strTestFileName;
		JSFile objFile = new JSFile(strTestPathName, strRenamedTestfileName);
		if (objFile.exists()) {
			objFile.delete();
		}
		objFile = new JSFile(strTestPathName, strTestFileName);
		if (objFile.exists()) {
			objFile.delete();
		}
		logMethodName(conMethodName);
		setFTPPrefixParams(".*", "renamed_[filename:]");
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		objJadeEngine.Execute();
		boolean flgResult = objJadeEngine.objDataTargetClient.getFileHandle(objOptions.remote_dir.Value() + "/" + strRenamedTestfileName).FileExists();
		boolean flgResult2 = objJadeEngine.objDataSourceClient.getFileHandle(objOptions.local_dir.Value() + "/" + strTestFileName).FileExists();
		boolean flgResult3 = objJadeEngine.objDataSourceClient.getFileHandle(objOptions.local_dir.Value() + "/" + strRenamedTestfileName).FileExists();
		assertTrue("Files must exist", flgResult && flgResult2 && !flgResult3);
		objJadeEngine.Logout();
		strTestFileName = strSaveTestfileName;
	}

	@Test public void testSendUsingEmptyReplacement() throws Exception {
		final String conMethodName = conClassName + "::testSendUsingEmptyReplacement";
		logger.info("********************************************** " + conMethodName + "******************");
		sendUsingReplacement("^t", "");
	}

	@Test public void testReceiveUsingEmptyReplacement() throws Exception {
		final String conMethodName = conClassName + "::testReceiveUsingEmptyReplacement";
		logger.info("********************************************** " + conMethodName + "******************");
		// setParams("renamed_", EMPTY_STRING);
		// objOptions.operation.Value(enuJadeOperations.receive);
		// JadeEngine objJadeEngine = new JadeEngine(objOptions);
		// objOptions.file_path.Value(EMPTY_STRING);
		// objOptions.file_spec.Value("^renamed_");
		HashMap<String, String> objHsh = new HashMap<String, String>();
		objHsh.put("ftp_host", conHostNameWILMA_SOS);
		objHsh.put("ftp_port", "21");
		objHsh.put("ftp_user", "test");
		objHsh.put("ftp_password", "12345");
		objHsh.put("ftp_transfer_mode", "binary");
		objHsh.put("ftp_passive_mode", "0");
		// Y: ist 8of9.sos c:
		objHsh.put("ftp_local_dir", "Y:/scheduler.test/testsuite_files/files/ftp_in/sosdex");
		objHsh.put("ftp_file_spec", "^renamed_");
		objHsh.put("ftp_remote_dir", "/home/test/temp/test/sosdex");
		objHsh.put("operation", "receive");
		objHsh.put("replacing", "^renamed_");
		// if replcing is not specified then "space" is assumed.
		// objHsh.put("replacement", "");
		objOptions = new JADEOptions();
		objOptions.setAllOptions(objOptions.DeletePrefix(objHsh, "ftp_"));
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		assertEquals("replacing", "^renamed_", objOptions.replacing.Value());
		assertEquals("replacement", "", objOptions.replacement.Value());
		objJadeEngine.Execute();
		objJadeEngine.Logout();
	}

	@Test public void testRenameOnSourceOnly4SFTP() throws Exception {
		final String conMethodName = conClassName + "::testRenameOnSourceOnly4SFTP";
		logger.info("********************************************** " + conMethodName + "******************");
		// setParams("renamed_", EMPTY_STRING);
		// objOptions.operation.Value(enuJadeOperations.receive);
		// JadeEngine objJadeEngine = new JadeEngine(objOptions);
		// objOptions.file_path.Value(EMPTY_STRING);
		// objOptions.file_spec.Value("^renamed_");
		HashMap<String, String> objHsh = new HashMap<String, String>();
		objHsh.put("source_host", conHostNameWILMA_SOS);
		objHsh.put("source_port", "22");
		objHsh.put("source_user", "test");
		objHsh.put("source_password", "12345");
		objHsh.put("source_protocol", "sftp");
		objHsh.put("source_ssh_auth_method", "password");
		objHsh.put("ftp_transfer_mode", "binary");
		objHsh.put("ftp_passive_mode", "false");
		// Y: ist 8of9.sos c:
		objHsh.put("source_dir", "/home/test/temp/test/sosdex");
		objHsh.put("file_spec", "^.*\\.txt$");
		objHsh.put("operation", "rename");
		objHsh.put("replacing", ".*");
		objHsh.put("replacement", "oh/[filename:]");
		objOptions = new JADEOptions();
		objOptions.setAllOptions(objOptions.DeletePrefix(objHsh, "ftp_"));
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		assertEquals("replacing", ".*", objOptions.replacing.Value());
		assertEquals("replacement", "oh/[filename:]", objOptions.replacement.Value());
		objJadeEngine.Execute();
		objJadeEngine.Logout();
	}

	@Test public void testRenameOnSourceOnly4FTP() throws Exception {
		final String conMethodName = conClassName + "::testRenameOnSourceOnly4FTP";
		logger.info("********************************************** " + conMethodName + "******************");
		// setParams("renamed_", EMPTY_STRING);
		// objOptions.operation.Value(enuJadeOperations.receive);
		// JadeEngine objJadeEngine = new JadeEngine(objOptions);
		// objOptions.file_path.Value(EMPTY_STRING);
		// objOptions.file_spec.Value("^renamed_");
		HashMap<String, String> objHsh = new HashMap<String, String>();
		objHsh.put("source_host", conHostNameWILMA_SOS);
		objHsh.put("source_port", "21");
		objHsh.put("source_user", "test");
		objHsh.put("source_password", "12345");
		objHsh.put("source_protocol", "ftp");
		objHsh.put("source_ssh_auth_method", "password");
		objHsh.put("ftp_transfer_mode", "binary");
		objHsh.put("ftp_passive_mode", "false");
		// Y: ist 8of9.sos c:
		objHsh.put("source_dir", "/home/test/temp/test/sosdex");
		objHsh.put("file_spec", "^.*\\.txt$");
		objHsh.put("operation", "rename");
		objHsh.put("replacing", ".*");
		objHsh.put("replacement", "oh/[filename:]");
		objOptions = new JADEOptions();
		objOptions.setAllOptions(objOptions.DeletePrefix(objHsh, "ftp_"));
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		assertEquals("replacing", ".*", objOptions.replacing.Value());
		assertEquals("replacement", "oh/[filename:]", objOptions.replacement.Value());
		objJadeEngine.Execute();
		objJadeEngine.Logout();
	}

	private void setParams(final String replacing, final String replacement) throws Exception {
		CreateTestFile();
		objOptions = new JADEOptions();
		objOptions.host.Value(conHostNameWILMA_SOS);
		objOptions.user.Value("kb");
		objOptions.password.Value("kb");
		objOptions.remote_dir.Value(strKBHome);
		objOptions.local_dir.Value(strTestPathName);
		objOptions.file_path.Value(strTestFileName);
		objOptions.operation.Value("send");
		objOptions.replacement.Value(replacement);
		objOptions.replacing.Value(replacing);
		objOptions.verbose.value(9);
	}

	private void setFTPPrefixParams(final String replacing, final String replacement) throws Exception {
		CreateTestFile();
		HashMap<String, String> objHsh = new HashMap<String, String>();
		objHsh.put("ftp_host", conHostNameWILMA_SOS);
		objHsh.put("ftp_protocol", "ftp");
		objHsh.put("ftp_port", "21");
		objHsh.put("ftp_user", "test");
		objHsh.put("ftp_password", "12345");
		objHsh.put("ftp_transfer_mode", "binary");
		objHsh.put("ftp_passive_mode", "0");
		objHsh.put("ftp_local_dir", strTestPathName);
		objHsh.put("ftp_file_spec", "^" + strTestFileName + "$");
		objHsh.put("ftp_remote_dir", "/home/test/temp/test");
		objHsh.put("operation", "send");
		objHsh.put("replacing", replacing);
		objHsh.put("replacement", replacement);
		objHsh.put("verbose", "9");
		objOptions = new JADEOptions();
		objOptions.setAllOptions(objOptions.DeletePrefix(objHsh, "ftp_"));
		// objOptions.CheckMandatory();
		// logger.info(objOptions.toString());
	}

	public void sendUsingReplacement(final String replacing, final String replacement) throws Exception {
		setParams(replacing, replacement);
		// logger.info(objOptions.toString());
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		objJadeEngine.Execute();
		SOSOptionRegExp objRE = new SOSOptionRegExp(null, "test", "TestOption", replacing, "", false);
		String expectedRemoteFile = strKBHome + objRE.doReplace(strTestFileName, replacement);
		boolean flgResult = objJadeEngine.objDataTargetClient.getFileHandle(expectedRemoteFile).FileExists();
		objJadeEngine.Logout();
		assertTrue(String.format("File '%1$s' does not exist", expectedRemoteFile), flgResult);
	}

	@Test public void testSendUsingRelativeLocalDir() throws Exception {
		final String conMethodName = conClassName + "::testSendUsingRelativeLocalDir";
		logMethodName(conMethodName);
		CreateTestFile();
		objOptions = new JADEOptions();
		objOptions.host.Value(conHostNameWILMA_SOS);
		objOptions.user.Value("kb");
		objOptions.password.Value("kb");
		objOptions.remote_dir.Value("./relative");
		objOptions.file_path.Value(strTestFileName);
		objOptions.local_dir.Value(strTestPathName);
		objOptions.operation.Value("send");
		// logger.info(objOptions.toString());
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		objJadeEngine.Execute();
		boolean flgResult = objJadeEngine.objDataTargetClient.getFileHandle("/home/kb/relative/" + strTestFileName).FileExists();
		assertTrue("File must exist", flgResult);
		// flgResult = objJadeEngine.objDataTargetClient.getFileHandle("./relative/" + strTestFileName).FileExists();
		// assertTrue("File must exist", flgResult);
		objJadeEngine.Logout();
	}

	@Test public void testSendUsingFilePathAndLocalDir() throws Exception {
		final String conMethodName = conClassName + "::testSendUsingRelativeLocalDir";
		// see http://www.sos-berlin.com/jira/browse/SOSFTP-106
		logMethodName(conMethodName);
		CreateTestFile();
		objOptions = new JADEOptions();
		objOptions.host.Value(conHostNameWILMA_SOS);
		objOptions.user.Value("kb");
		objOptions.password.Value("kb");
		objOptions.remote_dir.Value("./relative");
		objOptions.file_path.Value(strTestPathName + strTestFileName);
		objOptions.local_dir.Value(strTestPathName);
		objOptions.operation.Value("send");
		logger.info(objOptions.dirtyString());
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		objJadeEngine.Execute();
		boolean flgResult = objJadeEngine.objDataTargetClient.getFileHandle("/home/kb/relative/" + strTestFileName).FileExists();
		assertTrue("File must exist", flgResult);
		// flgResult = objJadeEngine.objDataTargetClient.getFileHandle("./relative/" + strTestFileName).FileExists();
		// assertTrue("File must exist", flgResult);
		objJadeEngine.Logout();
	}

	@Test public void testSendUsingFilePathAndLocalDir2() throws Exception {
		final String conMethodName = conClassName + "::testSendUsingFilePathAndLocalDir2";
		// see http://www.sos-berlin.com/jira/browse/SOSFTP-106
		logMethodName(conMethodName);
		CreateTestFile();
		objOptions = new JADEOptions();
		objOptions.host.Value(conHostNameWILMA_SOS);
		objOptions.user.Value("kb");
		objOptions.password.Value("kb");
		objOptions.remote_dir.Value("./relative");
		objOptions.file_path.Value(strTestPathName + strTestFileName);
		objOptions.local_dir.Value(strTestPathName + "Test/");
		objOptions.operation.Value("send");
		logger.info(objOptions.dirtyString());
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		objJadeEngine.Execute();
		boolean flgResult = objJadeEngine.objDataTargetClient.getFileHandle("/home/kb/relative/" + strTestFileName).FileExists();
		assertTrue("File must exist", flgResult);
		// flgResult = objJadeEngine.objDataTargetClient.getFileHandle("./relative/" + strTestFileName).FileExists();
		// assertTrue("File must exist", flgResult);
		objJadeEngine.Logout();
	}

	@Test public void testSendUsingFilePathAndLocalDir3() throws Exception {
		final String conMethodName = conClassName + "::testSendUsingFilePathAndLocalDir3";
		// see http://www.sos-berlin.com/jira/browse/SOSFTP-106
		logMethodName(conMethodName);
		CreateTestFile();
		objOptions = new JADEOptions();
		objOptions.host.Value(conHostNameWILMA_SOS);
		objOptions.user.Value("kb");
		objOptions.password.Value("kb");
		objOptions.remote_dir.Value("./relative");
		objOptions.file_path.Value(strTestFileName);
		objOptions.local_dir.Value(strTestPathName);
		runFilePathTest();
	}

	@Test public void testSendUsingFilePathAndLocalDir4() throws Exception {
		final String conMethodName = conClassName + "::testSendUsingFilePathAndLocalDir4";
		// see http://www.sos-berlin.com/jira/browse/SOSFTP-106
		logMethodName(conMethodName);
		CreateTestFile();
		objOptions = new JADEOptions();
		objOptions.host.Value(conHostNameWILMA_SOS);
		objOptions.user.Value("kb");
		objOptions.password.Value("kb");
		objOptions.remote_dir.Value("./relative");
		objOptions.file_path.Value(strTestPathName + strTestFileName);
		runFilePathTest();
	}

	@Test public void testSendUsingFilePathAndLocalDir5() throws Exception {
		final String conMethodName = conClassName + "::testSendUsingFilePathAndLocalDir5";
		// see http://www.sos-berlin.com/jira/browse/SOSFTP-106
		logMethodName(conMethodName);
		CreateTestFile();
		objOptions = new JADEOptions();
		objOptions.host.Value(conHostNameWILMA_SOS);
		objOptions.user.Value("kb");
		objOptions.password.Value("kb");
		objOptions.remote_dir.Value("./relative");
		objOptions.file_path.Value("SOSDataExchange/" + strTestFileName);
		objOptions.local_dir.Value("R:/backup/sos/java/junittests/testdata/");
		runFilePathTest();
	}

	private void runFilePathTest() throws Exception {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::runFilePathTest";
		objOptions.operation.Value("send");
		objOptions.CreateSecurityHash.setFalse();
		logger.info(objOptions.dirtyString());
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		objJadeEngine.Execute();
		boolean flgResult = objJadeEngine.objDataTargetClient.getFileHandle("/home/kb/relative/" + strTestFileName).FileExists();
		assertTrue("File must exist", flgResult);
		// flgResult = objJadeEngine.objDataTargetClient.getFileHandle("./relative/" + strTestFileName).FileExists();
		// assertTrue("File must exist", flgResult);
		objJadeEngine.Logout();
	} // private void runFilePathTest

	@Test public void testSend5() throws Exception {
		final String conMethodName = conClassName + "::testSend";
		logMethodName(conMethodName);
		CreateTestFile();
		objOptions = new JADEOptions();
		objOptions.host.Value(conHostNameWILMA_SOS);
		objOptions.user.Value("kb");
		objOptions.password.Value("kb");
		objOptions.file_path.Value(strTestFileName);
		objOptions.local_dir.Value(strTestPathName);
		objOptions.remote_dir.Value(strKBHome);
		objOptions.operation.Value("send");
		// logger.info(objOptions.toString());
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		objJadeEngine.Execute();
		boolean flgResult = objJadeEngine.objDataTargetClient.getFileHandle(strKBHome + strTestFileName).FileExists();
		assertTrue("File must exist", flgResult);
		objJadeEngine.Logout();
	}

	@Test public void testSendComand() throws Exception {
		final String conMethodName = conClassName + "::testSendComand";
		logMethodName(conMethodName);
		objOptions = new JADEOptions();
		objOptions.host.Value(conHostNameWILMA_SOS);
		objOptions.user.Value("kb");
		objOptions.password.Value("kb");
		objOptions.file_path.Value(strTestFileName);
		objOptions.local_dir.Value(strTestPathName);
		objOptions.remote_dir.Value(strKBHome);
		objOptions.operation.Value("send");
		setOptions4BackgroundService();
		objOptions.SendTransferHistory.value(false);
		objOptions.Target().Post_Command.Value("SITE CHMOD 777 $TargetFileName");
		objOptions.Source().Pre_Command.Value("dir $SourceFileName");
		// logger.info(objOptions.toString());
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		// objJadeEngine.DataTargetClient().getHandler().;
		// objJadeEngine.get;
		objJadeEngine.Execute();
		//
		// boolean flgResult = objJadeEngine.objDataTargetClient.getFileHandle(strKBHome + strTestFileName).FileExists();
		// assertTrue("File must exist", flgResult);
		objJadeEngine.Logout();
	}

	private void setOptions4BackgroundService() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setOptions4BackgroundService";
		objOptions.scheduler_host.Value(conHostName8OF9_SOS);
		objOptions.scheduler_port.Value("4210");
		// objOptions.Scheduler_Transfer_Method.Value(enuJSTransferModes.udp.description); //
		objOptions.Scheduler_Transfer_Method.Value(enuJSTransferModes.tcp.description); //
		objOptions.SendTransferHistory.value(true); //
	} // private void setOptions4BackgroundService

	// @Test
	public void testFerberSFtp() throws Exception {
		CreateTestFile();
		objOptions = new JADEOptions();
		objOptions.host.Value("85.214.92.170");
		objOptions.port.Value("22");
		objOptions.protocol.Value(enuTransferTypes.sftp);
		objOptions.alternative_host.Value("85.214.92.170");
		objOptions.alternative_port.Value("22");
		// objOptions.alternative_protocol.Value(enuTransferTypes.sftp);
		objOptions.user.Value("kb");
		objOptions.password.Value("kb");
		objOptions.file_path.Value(strTestPathName + strTestFileName);
		objOptions.local_dir.Value(strTestPathName);
		objOptions.operation.Value("send");
		// logger.info(objOptions.toString());
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		objJadeEngine.Execute();
	}

	@Test public void testSendToAlternateHost() throws Exception {
		final String conMethodName = conClassName + "::testSendToAlternateHost";
		logMethodName(conMethodName);
		CreateTestFile();
		objOptions = new JADEOptions();
		objOptions.host.Value("xwilma.sos");
		objOptions.alternative_host.Value(conHostNameWILMA_SOS);
		objOptions.alternative_port.Value("21");
		objOptions.user.Value("kb");
		objOptions.password.Value("kb");
		objOptions.file_path.Value(strTestPathName + strTestFileName);
		objOptions.local_dir.Value(strTestPathName);
		objOptions.operation.Value("send");
		// logger.info(objOptions.toString());
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		objJadeEngine.Execute();
		boolean flgResult = objJadeEngine.objDataTargetClient.getFileHandle("/home/kb/" + strTestFileName).FileExists();
		assertTrue("File must exist", flgResult);
		objJadeEngine.Logout();
	}

	@Test public void testSendToAlternateUser() throws Exception {
		final String conMethodName = conClassName + "::testSendToAlternateUser";
		logMethodName(conMethodName);
		CreateTestFile();
		objOptions = new JADEOptions();
		objOptions.host.Value("wilma.sos");
		objOptions.alternative_user.Value("test");
		objOptions.getConnectionOptions().Target().Alternatives().user.Value("test");
		objOptions.getConnectionOptions().Target().Alternatives().password.Value("12345");
		objOptions.alternative_port.Value("21");
		objOptions.getConnectionOptions().Alternatives().user.Value();
		objOptions.getConnectionOptions().Source().Alternatives().user.Value();
		objOptions.user.Value("kb");
		objOptions.password.Value("kbkbkb");
		objOptions.file_path.Value(strTestPathName + strTestFileName);
		objOptions.local_dir.Value(strTestPathName);
		objOptions.operation.Value("send");
		// logger.info(objOptions.toString());
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		objJadeEngine.Execute();
		boolean flgResult = objJadeEngine.objDataTargetClient.getFileHandle("/home/kb/" + strTestFileName).FileExists();
		assertTrue("File must exist", flgResult);
		objJadeEngine.Logout();
	}

	@Test public void testSend2() throws Exception {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::testSend2";
		CreateTestFile();
		objOptions.host.Value(conHostName8OF9_SOS);
		objOptions.user.Value("kb");
		objOptions.password.Value("kb");
		objOptions.file_path.Value(strTestPathName + strTestFileName);
		objOptions.local_dir.Value(strTestPathName);
		objOptions.remote_dir.Value("/kb/");
		objOptions.operation.Value("send");
		objOptions.passive_mode.value(true);
		// logger.info(objOptions.toString());
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		objJadeEngine.Execute();
		boolean flgResult = objJadeEngine.objDataTargetClient.getFileHandle("/kb/" + strTestFileName).FileExists();
		assertTrue("File must exist", flgResult);
		objJadeEngine.Logout();
	}

	@Test public void testSend2file_spec() throws Exception {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::testSend2";
		String strSaveTestfileName = strTestFileName;
		strTestFileName = "3519078034.pdf";
		CreateTestFile();
		objOptions.host.Value(conHostNameWILMA_SOS);
		objOptions.user.Value("kb");
		objOptions.password.Value("kb");
		objOptions.protocol.Value(enuTransferTypes.sftp);
		objOptions.ssh_auth_method.Value(enuAuthenticationMethods.password);
		objOptions.file_spec.Value("^[0-9]{10}\\.pdf$");
		objOptions.local_dir.Value(strTestPathName);
		objOptions.remote_dir.Value("/home/kb/");
		objOptions.transactional.setTrue();
		objOptions.atomic_suffix.Value(".tmp");
		objOptions.Post_Command.Value("chmod 777 $TargetTransferFileName");
		objOptions.operation.Value("send");
		objOptions.passive_mode.value(true);
		objOptions.log_filename.Value("c:/temp/test.log");
		setMailOptions();
		// logger.info(objOptions.toString());
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		objJadeEngine.Execute();
		boolean flgResult = objJadeEngine.objDataTargetClient.getFileHandle("/home/kb/" + strTestFileName).FileExists();
		assertTrue("File must exist", flgResult);
		objJadeEngine.Logout();
		strTestFileName = strSaveTestfileName;
	}

	@Test public void testSendWithRename() throws Exception {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::testSendWithRename";
		CreateTestFile();
		objOptions.host.Value(conHostNameWILMA_SOS);
		objOptions.user.Value("test");
		objOptions.password.Value("12345");
		objOptions.protocol.Value(enuTransferTypes.sftp);
		objOptions.ssh_auth_method.Value(enuAuthenticationMethods.password);
		objOptions.file_spec.Value("^" + strTestFileName + "$");
		objOptions.local_dir.Value(strTestPathName);
		objOptions.remote_dir.Value("/home/test/temp/test/");
		objOptions.replacement.Value(".*");
		objOptions.replacing.Value("renamed_[filename:]");
		objOptions.operation.Value("send");
		objOptions.log_filename.Value("c:/temp/test.log");
		//		setMailOptions();
		// logger.info(objOptions.toString());
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		objJadeEngine.Execute();
		boolean flgResult = objJadeEngine.objDataTargetClient.getFileHandle(objOptions.remote_dir.Value() + "/renamed_" + strTestFileName).FileExists();
		assertTrue("File must exist", flgResult);
		objJadeEngine.Logout();
	}

	private void setMailOptions() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setMailOptions";
		objOptions.getMailOptions().to.Value("kb@sos-berlin.com");
		objOptions.getMailOptions().SMTPHost.Value("smtp.sos");
	} // private void setMailOptions

	@Test public void testReceive() throws Exception {
		final String conMethodName = conClassName + "::testReceive";
		logMethodName(conMethodName);
		// CreateTestFile();
		objOptions.host.Value(conHostNameWILMA_SOS);
		objOptions.user.Value("kb");
		objOptions.password.Value("kb");
		objOptions.file_path.Value(strTestFileName);
		// TODO Alias "targetdir" erlauben. Da es nicht immer remote ist :-)
		/*
		 * bei receive,nach der bisherigen semantik, ist das local_dir praktisch
		 * das target_dir und das remote_dir ist das source_dir
		 *
		 * Also: bei receive muß target und source_dir anders gefüllt werden
		 * am besten so lösen, daß zwei neue Parameter eingeführt werden und die dann
		 * im code verwendet werden. Dadurch läßt sich das leicht drehen und ist eine
		 * Kompatibilitätsunterstützung.
		 */
		objOptions.remote_dir.Value("/home/kb/");
		objOptions.local_dir.Value(strTestPathName);
		objOptions.operation.Value("receive");
		objOptions.transfer_mode.Value("ascii");
		// logger.info(objOptions.toString());
		setOptions4BackgroundService();
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		objJadeEngine.Execute();
		objJadeEngine.Logout();
	}

	@Test public void testReceiveWithSymlinkInRemoteDir() throws Exception {
		final String conMethodName = conClassName + "::testReceiveWithSymlinkInRemoteDir";
		logMethodName(conMethodName);
		JSFile objFile = new JSFile(strTestPathName, strTestFileName);
		if (objFile.exists()) {
			objFile.delete();
		}
		objOptions.host.Value(conHostNameWILMA_SOS);
		objOptions.user.Value("test");
		objOptions.password.Value("12345");
		//		objOptions.file_path.Value(strTestFileName);
		objOptions.file_spec.Value("^.*\\.txt$");
		//		objOptions.file_spec.Value("^"+strTestFileName+"$");
		objOptions.remote_dir.Value("/tmp/test/symlink2home.test.temp/test");
		objOptions.local_dir.Value(strTestPathName);
		objOptions.operation.Value("receive");
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		objJadeEngine.Execute();
		boolean flgResult = objJadeEngine.objDataTargetClient.getFileHandle(objOptions.local_dir.Value() + "/" + strTestFileName).FileExists();
		assertTrue("File must exist", flgResult);
		objJadeEngine.Logout();
	}

	@Test public void testReceiveSFTP() throws Exception {
		final String conMethodName = conClassName + "::testReceiveSFTP";
		logMethodName(conMethodName);
		// CreateTestFile();
		objOptions.host.Value(conHostNameWILMA_SOS);
		objOptions.user.Value("kb");
		objOptions.protocol.Value(enuTransferTypes.sftp.Text());
		objOptions.port.Value("22");
		objOptions.password.Value("kb");
		// / objOptions.file_path.Value(strTestFileName);
		objOptions.file_path.Value("sosdex.txt");
		objOptions.remote_dir.Value("/home/sos/tmp");
		objOptions.ssh_auth_method.Value(enuAuthenticationMethods.password);
		objOptions.BufferSize.value(1024);
		/*
		 * bei receive,nach der bisherigen semantik, ist das local_dir praktisch
		 * das target_dir und das remote_dir ist das source_dir
		 *
		 * Also: bei receive muß target und source_dir anders gefüllt werden
		 * am besten so lösen, daß zwei neue Parameter eingeführt werden und die dann
		 * im code verwendet werden. Dadurch läßt sich das leicht drehen und ist eine
		 * Kompatibilitätsunterstützung.
		 */
		objOptions.local_dir.Value(strTestPathName);
		objOptions.operation.Value("receive");
		// logger.info(objOptions.toString());
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		objJadeEngine.Execute();
		objJadeEngine.Logout();
	}

	@Test public void testReceiveWithUmlaut() throws Exception {
		final String conMethodName = conClassName + "::testReceive";
		logMethodName(conMethodName);
		// CreateTestFile();
		objOptions.host.Value(conHostNameWILMA_SOS);
		objOptions.user.Value("kb");
		objOptions.password.Value("kb");
		objOptions.file_path.Value("Büttner.dat");
		// TODO Alias "targetdir" erlauben. Da es nicht immer remote ist :-)
		/*
		 * bei receive,nach der bisherigen semantik, ist das local_dir praktisch
		 * das target_dir und das remote_dir ist das source_dir
		 *
		 * Also: bei receive muß target und source_dir anders gefüllt werden
		 * am besten so lösen, daß zwei neue Parameter eingeführt werden und die dann
		 * im code verwendet werden. Dadurch läßt sich das leicht drehen und ist eine
		 * Kompatibilitätsunterstützung.
		 */
		objOptions.remote_dir.Value("/home/kb/");
		objOptions.local_dir.Value(strTestPathName);
		objOptions.operation.Value("receive");
		// logger.info(objOptions.toString());
		objOptions.ControlEncoding.Value("UTF-8");
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		objJadeEngine.Execute();
		objJadeEngine.Logout();
	}

	@Test public void testReceiveWithUmlautFromLocalhost() throws Exception {
		final String conMethodName = conClassName + "::testReceive";
		logMethodName(conMethodName);
		// CreateTestFile();
		objOptions.host.Value("localhost");
		objOptions.user.Value("kb");
		objOptions.password.Value("kb");
		objOptions.file_spec.Value(".*ttner\\..*");
		objOptions.remote_dir.Value("/");
		// objOptions.file_path.Value("Büttner.dat");
		// TODO Alias "targetdir" erlauben. Da es nicht immer remote ist :-)
		/*
		 * bei receive,nach der bisherigen semantik, ist das local_dir praktisch
		 * das target_dir und das remote_dir ist das source_dir
		 *
		 * Also: bei receive muß target und source_dir anders gefüllt werden
		 * am besten so lösen, daß zwei neue Parameter eingeführt werden und die dann
		 * im code verwendet werden. Dadurch läßt sich das leicht drehen und ist eine
		 * Kompatibilitätsunterstützung.
		 */
		objOptions.local_dir.Value(strTestPathName);
		objOptions.operation.Value("receive");
		// logger.info(objOptions.toString());
		objOptions.PreFtpCommands.Value("OPTS UTF8 ON");
		// objOptions.ControlEncoding.Value("UTF-8");
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		// objJadeEngine.DataTargetClient().getHandler().ExecuteCommand("OPTS UTF8 ON");
		objJadeEngine.Execute();
		objJadeEngine.Logout();
	}

	@Test public void testCopy() throws Exception {
		final String conMethodName = conClassName + "::testCopy";
		logMethodName(conMethodName);
		SOSConnection2OptionsAlternate objS = objOptions.getConnectionOptions().Source();
		objS.host.Value(conHostNameWILMA_SOS);
		objS.protocol.Value("ftp");
		objS.user.Value("kb");
		objS.password.Value("kb");
		objOptions.file_path.Value(strTestFileName);
		// TODO Alias "targetdir" erlauben. Da es nicht immer remote ist :-)
		/*
		 * bei receive,nach der bisherigen semantik, ist das local_dir praktisch
		 * das target_dir und das remote_dir ist das source_dir
		 *
		 * Also: bei receive muß target und source_dir anders gefüllt werden
		 * am besten so lösen, daß zwei neue Parameter eingeführt werden und die dann
		 * im code verwendet werden. Dadurch läßt sich das leicht drehen und ist eine
		 * Kompatibilitätsunterstützung.
		 */
		objOptions.SourceDir.Value("/home/kb/");
		SOSConnection2OptionsAlternate objT = objOptions.getConnectionOptions().Target();
		objT.protocol.Value("local");
		objOptions.TargetDir.Value(strTestPathName);
		objOptions.remote_dir.Value(strTestPathName);
		objOptions.operation.Value(SOSOptionJadeOperation.enuJadeOperations.copy);
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		objJadeEngine.Execute();
		objJadeEngine.Logout();
	}

	@Test public void testReceive2() throws Exception {
		final String conMethodName = conClassName + "::testReceive";
		logMethodName(conMethodName);
		// CreateTestFile();
		objOptions.host.Value(conHostName8OF9_SOS);
		objOptions.user.Value("kb");
		objOptions.password.Value("kb");
		objOptions.file_path.Value(strTestFileName);
		// TODO Alias "targetdir" erlauben. Da es nicht immer remote ist :-)
		/*
		 * bei receive,nach der bisherigen semantik, ist das local_dir praktisch
		 * das target_dir und das remote_dir ist das source_dir
		 *
		 * Also: bei receive muß target und source_dir anders gefüllt werden
		 * am besten so lösen, daß zwei neue Parameter eingeführt werden und die dann
		 * im code verwendet werden. Dadurch läßt sich das leicht drehen und ist eine
		 * Kompatibilitätsunterstützung.
		 */
		objOptions.remote_dir.Value("/kb/");
		objOptions.local_dir.Value(strTestPathName);
		objOptions.operation.Value("receive");
		// logger.info(objOptions.toString());
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		objJadeEngine.Execute();
		objJadeEngine.Logout();
	}

	@Test public void testSendMultipleFiles() throws Exception {
		final String conMethodName = conClassName + "::testSendMultipleFiles";
		logMethodName(conMethodName);
		objOptions.host.Value(conHostNameWILMA_SOS);
		objOptions.user.Value("kb");
		objOptions.password.Value("kb");
		objOptions.file_path.Value("");
		objOptions.file_spec.Value("^.*\\.txt$");
		objOptions.local_dir.Value(strTestPathName);
		objOptions.operation.Value("send");
		objOptions.verbose.value(9);
		setOptions4BackgroundService();
		// logger.info(objOptions.toString());
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		objJadeEngine.Execute();
		objJadeEngine.Logout();
	}

	@Test public void testReceiveMultipleFiles() throws Exception {
		objOptions.host.Value(conHostNameWILMA_SOS);
		objOptions.user.Value("kb");
		objOptions.password.Value("kb");
		objOptions.file_path.Value("");
		objOptions.file_spec.Value("^.*\\.txt$");
		objOptions.local_dir.Value(strTestPathName);
		objOptions.remote_dir.Value("/home/kb/");
		objOptions.append_files.value(false);
		objOptions.operation.Value("receive");
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		objJadeEngine.Execute();
		long intNoOfFilesTransferred = objJadeEngine.getFileList().SuccessfulTransfers();
		// Assert.assertEquals("Anzahl Dateien sind genau 21", 21L, intNoOfFilesTransferred);
		for (SOSFileListEntry objListItem : objJadeEngine.getFileList().List()) {
			String strF = MakeFullPathName(objOptions.TargetDir.Value(), objListItem.TargetFileName());
			boolean flgResult = objListItem.getDataTargetClient().getFileHandle(strF).FileExists();
			Assert.assertTrue("File " + strF + " exist, but should not", flgResult);
		}
		objJadeEngine.Logout();
	}

	@Test public void testResultSet() throws Exception {
		objOptions.host.Value(conHostNameWILMA_SOS);
		objOptions.user.Value("kb");
		objOptions.password.Value("kb");
		objOptions.file_path.Value("");
		objOptions.file_spec.Value("^.*\\.txt$");
		objOptions.local_dir.Value(strTestPathName);
		objOptions.remote_dir.Value("/home/kb/");
		objOptions.append_files.value(false);
		objOptions.operation.Value("receive");
		objOptions.CreateResultSet.value(true);
		String strResultSetFileName = objOptions.TempDir() + "/ResultSetFile.dat";
		objOptions.ResultSetFileName.Value(strResultSetFileName);
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		objJadeEngine.Execute();
		objJadeEngine.Logout();
	}

	@Test public void testSendAndDeleteMultipleFiles() throws Exception {
		// CreateTestFile();
		objOptions.host.Value(conHostNameWILMA_SOS);
		objOptions.user.Value("kb");
		objOptions.password.Value("kb");
		objOptions.file_path.Value("");
		objOptions.file_spec.Value("^.*\\.txt$");
		objOptions.local_dir.Value(strTestPathName);
		objOptions.operation.Value("send");
		objOptions.DeleteFilesAfterTransfer.value(true);
		objOptions.log_filename.Value("c:/temp/test.log");
		// objOptions.force_files.value(false);
		// logger.info(objOptions.toString());
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		objJadeEngine.Execute();
		for (SOSFileListEntry objListItem : objJadeEngine.getFileList().List()) {
			String strF = objListItem.SourceFileName();
			boolean flgResult = objListItem.getDataSourceClient().getFileHandle(strF).FileExists();
			Assert.assertFalse("File " + strF + " exist, but should not", flgResult);
		}
		objJadeEngine.Logout();
	}

	@Test public void testReceiveMultipleFiles2() throws Exception {
		// CreateTestFile();
		objOptions.host.Value(conHostNameWILMA_SOS);
		objOptions.user.Value("kb");
		objOptions.password.Value("kb");
		objOptions.file_path.Value("");
		objOptions.file_spec.Value("^.*\\.txt$");
		objOptions.local_dir.Value(strTestPathName);
		objOptions.remote_dir.Value("/home/kb/");
		objOptions.append_files.value(false);
		objOptions.operation.Value("receive");
		// objOptions.ControlEncoding.Value("UTF-8");
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		objJadeEngine.Execute();
		long intNoOfFilesTransferred = objJadeEngine.getFileList().SuccessfulTransfers();
		// Assert.assertEquals("Anzahl Dateien sind genau 21", 21L, intNoOfFilesTransferred);
		for (SOSFileListEntry objListItem : objJadeEngine.getFileList().List()) {
			String strF = MakeFullPathName(objOptions.TargetDir.Value(), objListItem.TargetFileName());
			boolean flgResult = objListItem.getDataTargetClient().getFileHandle(strF).FileExists();
			Assert.assertTrue("File " + strF + " exist, but should not", flgResult);
		}
		objJadeEngine.Logout();
	}

	public void renameLocalFiles(final String source_dir, final String file_spec) throws Exception {
		SOSConnection2Options objConn = objOptions.getConnectionOptions();
		SOSConnection2OptionsAlternate objS = objConn.Source();
		SOSConnection2OptionsAlternate objT = objConn.Target();
		objS.HostName.Value(conHostNameWILMA_SOS);
		objS.port.value(SOSOptionPortNumber.getStandardSFTPPort());
		objS.protocol.Value("sftp");
		objS.user.Value("test");
		objS.ssh_auth_method.isPassword(true);
		objS.password.Value("12345");
		// TODO target should be unnecessary when operation 'rename'
		objT.HostName.Value(conHostNameWILMA_SOS);
		objT.port.value(SOSOptionPortNumber.getStandardSFTPPort());
		objT.protocol.Value("sftp");
		objT.user.Value("test");
		objT.ssh_auth_method.isPassword(true);
		objT.password.Value("12345");
		objOptions.SourceDir.Value(source_dir);
		objOptions.TargetDir.Value(source_dir);
		objOptions.file_path.Value("");
		objOptions.operation.Value("rename");
		objOptions.file_spec.Value(file_spec);
		objOptions.replacing.Value(".*");
		objOptions.replacement.Value("moved/[filename:]");
		// logger.info(objOptions.toString());
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		objJadeEngine.Execute();
		// boolean flgResult = objJadeEngine.objDataSourceClient.getFileHandle(strTestDir + "/moved/scheduler.dll").FileExists();
		// assertTrue("File must exist " + strTestDir + "/moved/scheduler.dll", flgResult);
		objJadeEngine.Logout();
	}

	public void renameFiles(final String source_dir, final String file_spec) throws Exception {
		SOSConnection2Options objConn = objOptions.getConnectionOptions();
		SOSConnection2OptionsAlternate objS = objConn.Source();
		SOSConnection2OptionsAlternate objT = objConn.Target();
		objS.HostName.Value(conHostNameWILMA_SOS);
		objS.port.value(SOSOptionPortNumber.getStandardSFTPPort());
		objS.protocol.Value("sftp");
		objS.user.Value("test");
		objS.ssh_auth_method.isPassword(true);
		objS.password.Value("12345");
		// TODO target should be unnecessary when operation 'rename'
		objT.HostName.Value(conHostNameWILMA_SOS);
		objT.port.value(SOSOptionPortNumber.getStandardSFTPPort());
		objT.protocol.Value("sftp");
		objT.user.Value("test");
		objT.ssh_auth_method.isPassword(true);
		objT.password.Value("12345");
		objOptions.SourceDir.Value(source_dir);
		objOptions.TargetDir.Value(source_dir);
		objOptions.file_path.Value("");
		objOptions.operation.Value("rename");
		objOptions.file_spec.Value(file_spec);
		objOptions.replacing.Value(".*");
		objOptions.replacement.Value("moved/[filename:]");
		// logger.info(objOptions.toString());
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		objJadeEngine.Execute();
		// boolean flgResult = objJadeEngine.objDataSourceClient.getFileHandle(strTestDir + "/moved/scheduler.dll").FileExists();
		// assertTrue("File must exist " + strTestDir + "/moved/scheduler.dll", flgResult);
		objJadeEngine.Logout();
	}

	@Test public void testRenameFiles() throws Exception {
		final String conMethodName = conClassName + "::testRenameFiles";
		logMethodName(conMethodName);
		String strTestDir = "/home/test/temp/test/sosdex";
		renameFiles(strTestDir, "^\\d\\.txt$");
		// logger.info(objOptions.toString());
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		objJadeEngine.Execute();
		// boolean flgResult = objJadeEngine.objDataSourceClient.getFileHandle(strTestDir + "/moved/scheduler.dll").FileExists();
		// assertTrue("File must exist " + strTestDir + "/moved/scheduler.dll", flgResult);
		objJadeEngine.Logout();
	}

	@Test public void testRenameFiles2() throws Exception {
		final String conMethodName = conClassName + "::testRenameFiles2";
		logMethodName(conMethodName);
		String strTestDir = "/home/test/temp/test/sosdex";
		renameFiles(strTestDir, "^scheduler\\.dll$");
		// logger.info(objOptions.toString());
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		objJadeEngine.Execute();
		boolean flgResult = objJadeEngine.objDataSourceClient.getFileHandle(strTestDir + "/moved/scheduler.dll").FileExists();
		assertTrue("File must exist " + strTestDir + "/moved/scheduler.dll", flgResult);
		objJadeEngine.Logout();
	}

	@Test public void testSendMultipleFilesLocal2Local() throws Exception {
		CreateTestFiles(10);
		objOptions.Source().protocol.Value(enuTransferTypes.local);
		objOptions.Target().protocol.Value(enuTransferTypes.local);
		objOptions.SourceDir.Value(strTestPathName);
		objOptions.TargetDir.Value(strTestPathName + "/SOSMDX/");
		objOptions.file_path.Value("");
		objOptions.FileNamePatternRegExp.Value("^.*\\.txt$");
		objOptions.operation.Value("copy");
		objOptions.CreateSecurityHash.value(false);
		objOptions.Target().Post_Command.Value("echo $TargetFileName");
		objOptions.remove_files.value(true);
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		objJadeEngine.Execute();
		objJadeEngine.Logout();
	}

	@Test public void testSendMultipleFilesThreaded() throws Exception {
		objOptions.MaxConcurrentTransfers.value(10);
		objOptions.ConcurrentTransfer.value(true);
		testSendMultipleFilesLocal2Local();
	}

	@Test public void testBigCopyThreaded() throws Exception {
		objOptions.MaxConcurrentTransfers.value(30);
		objOptions.ConcurrentTransfer.value(true);
		testBigCopy();
	}

	@Test public void testBigCopy() throws Exception {
		objOptions.Source().protocol.Value(enuTransferTypes.local);
		objOptions.Target().protocol.Value(enuTransferTypes.local);
		objOptions.recursive.setTrue();
		objOptions.file_spec.Value("^.*$");
		objOptions.MaxFiles.value(15);
		objOptions.SourceDir.Value("R:/backup/sos/java/doxygen-docs");
		objOptions.TargetDir.Value("R:/backup/www.sos-berlin.com/doc/doxygen-docs");
		objOptions.operation.Value(enuJadeOperations.copy);
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		objJadeEngine.Execute();
		objJadeEngine.Logout();
	}

	@Test public void testDeleteZipFile() throws Exception {
		final String conMethodName = conClassName + "::testDeleteZipFile";
		logger.info("*********************************************** " + conMethodName + "******************");
		String[] strCmdLineParameters = new String[] { "-settings=" + strSettingsFileName, "-profile=zip_local_files" };
		objOptions.CommandLineArgs(strCmdLineParameters);
		File fleFile = new File(objOptions.remote_dir.Value());
		fleFile.delete();
	} // private void testDeleteZipFile

	@Test public void testParameterPriority() throws Exception {
		final String conMethodName = conClassName + "::testParameterPriority";
		logger.info("*********************************************** " + conMethodName + "******************");
		String[] strCmdLineParameters = new String[] { "-settings=" + strSettingsFileName, "-profile=zip_local_files_2", "-operation=receive" };
		objOptions.CommandLineArgs(strCmdLineParameters);
		String strOperation = objOptions.operation.Value();
		assertEquals("Operation not overwritten", "receive", strOperation);
		assertEquals("source protocol", "local", objOptions.getConnectionOptions().Source().protocol.Value());
		assertEquals("source dir", "J:\\E\\java\\junittests\\testdata\\SOSDataExchange/", objOptions.SourceDir.Value());
		assertEquals("Operation not overwritten", "receive", strOperation);
	} // private void testDeleteZipFile

	@Test public void testParameterPriority2() throws Exception {
		final String conMethodName = conClassName + "::testParameterPriority";
		logger.info("*********************************************** " + conMethodName + "******************");
		String[] strCmdLineParameters = new String[] { "-settings=" + strSettingsFileName, "-profile=zip_local_files", "-operation=getFileList" };
		objOptions.CommandLineArgs(strCmdLineParameters);
		String strOperation = objOptions.operation.Value();
		assertEquals("Precedence test failed", "getFileList", strOperation);
	} // private void testDeleteZipFile

	@Test public void testZipOperation() throws Exception {
		final String conMethodName = conClassName + "::testZipOperation";
		logger.info("*********************************************** " + conMethodName + "******************");
		String[] strCmdLineParameters = new String[] { "-settings=" + strSettingsFileName, "-profile=zip_local_files" };
		objOptions.CommandLineArgs(strCmdLineParameters);
		objOptions.SendTransferHistory.value(false);
		boolean flgOK = new JSFile(objOptions.TargetDir.Value()).delete();
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		objJadeEngine.Execute();
		// Assert.assertEquals("User ID", "kb", objOptions.user.Value());
		// Assert.assertEquals("password", "kb", objOptions.password.Value());
	}

	@Test public void testZipExtraction() throws Exception {
		final String conMethodName = conClassName + "::testZipExtraction";
		logger.info("*********************************************** " + conMethodName + "******************");
		String[] strCmdLineParameters = new String[] { "-settings=" + strSettingsFileName, "-profile=zip_extract_2_local_files" };
		objOptions.CommandLineArgs(strCmdLineParameters);
		objOptions.SendTransferHistory.value(false);
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		objJadeEngine.Execute();
		// Assert.assertEquals("User ID", "kb", objOptions.user.Value());
		// Assert.assertEquals("password", "kb", objOptions.password.Value());
	}

	@Test public void testSendMultipleZIPedFilesLocal2Local() throws Exception {
		objOptions.protocol.Value("local");
		objOptions.file_path.Value("");
		objOptions.file_spec.Value("^.*\\.txt$");
		objOptions.local_dir.Value(strTestPathName);
		objOptions.remote_dir.Value(strTestPathName + "/SOSMDX/");
		objOptions.operation.Value("send");
		objOptions.compress_files.value(true);
		objOptions.compressed_file_extension.Value(".zip");
		objOptions.ConcurrentTransfer.value(true);
		objOptions.MaxConcurrentTransfers.value(5);
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		objJadeEngine.Execute();
		objJadeEngine.Logout();
	}

	@Test public void testSendMultipleFilesLocal2LocalAtomic() throws Exception {
		objOptions.protocol.Value("local");
		objOptions.file_path.Value("");
		objOptions.file_spec.Value("^.*\\.txt$");
		objOptions.local_dir.Value(strTestPathName);
		objOptions.remote_dir.Value(strTestPathName + "/SOSMDX/");
		objOptions.operation.Value("send");
		objOptions.atomic_suffix.Value("~");
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		objJadeEngine.Execute();
		objJadeEngine.Logout();
	}

	@Test public void testSendMultipleFilesAtomicAndTransactional() throws Exception {
		objOptions.protocol.Value("local");
		objOptions.file_spec.Value("^.*\\.txt$");
		objOptions.local_dir.Value(strTestPathName);
		objOptions.remote_dir.Value(strTestPathName + "/SOSMDX/");
		objOptions.operation.Value("send");
		objOptions.atomic_suffix.Value(".xfer"); // War ein Problem bei der BKB
		objOptions.transactional.value(true);
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		objJadeEngine.Execute();
		objJadeEngine.Logout();
	}

	@Test public void testRenameMultipleFilesLocal() throws Exception {
		// TODO Wenn Source nicht angegeben wurde, dann klappt es nicht. es wird ftp verwendet (von der vorherigen Einstellung?)
		objOptions.protocol.Value("local");
		objOptions.getConnectionOptions().Source().protocol.Value("local");
		objOptions.getConnectionOptions().Target().protocol.Value("local");
		objOptions.file_spec.Value("^.*\\.txt$");
		objOptions.local_dir.Value(strTestPathName + "/SOSMDX/");
		objOptions.remote_dir.Value(strTestPathName + "/SOSMDX/");
		objOptions.operation.Value("rename");
		objOptions.replacing.Value("(.*)(.txt)");
		objOptions.replacement.Value("\\1_[date:yyyyMMddHHmm];\\2");
		// logger.info(objOptions.toString());
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		objJadeEngine.Execute();
		objJadeEngine.Logout();
	}

	@Test public void testDeleteMultipleFilesLocal() throws Exception {
		// TODO Wenn Source nicht angegeben wurde, dann klappt es nicht. es wird ftp verwendet (von der vorherigen Einstellung?)
		objOptions.protocol.Value("local");
		objOptions.getConnectionOptions().Source().protocol.Value("local");
		objOptions.getConnectionOptions().Target().protocol.Value("local");
		objOptions.file_path.Value("");
		objOptions.file_spec.Value("^.*\\.txt$");
		objOptions.local_dir.Value(strTestPathName + "/SOSMDX/");
		objOptions.remote_dir.Value(strTestPathName + "/SOSMDX/");
		objOptions.operation.Value("delete");
		// logger.info(objOptions.toString());
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		objJadeEngine.Execute();
		objJadeEngine.Logout();
	}

	@Test public void testSendHugeNumberOfFiles() throws Exception {
		final String conMethodName = conClassName + "::testSendHugeNumberOfFiles";
		logMethodName(conMethodName);
		CreateTestFiles(50);
		objOptions.host.Value(conHostNameWILMA_SOS);
		objOptions.user.Value("kb");
		objOptions.password.Value("kb");
		objOptions.file_path.Value("");
		objOptions.file_spec.Value("^.*\\.txt$");
		objOptions.local_dir.Value(strTestPathName);
		objOptions.operation.Value("send");
		objOptions.passive_mode.setTrue();
		objOptions.ConcurrentTransfer.value(true);
		objOptions.MaxConcurrentTransfers.value(4);
		// logger.info(objOptions.toString());
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		objJadeEngine.Execute();
		objJadeEngine.Logout();
	}

	@Test public void testIniFile1() throws Exception {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::CreateIniFile";
		logger.info("*********************************************** " + conMethodName + "******************");
		CreateIniFile();
		objOptions.settings.Value(constrSettingsTestFile);
		objOptions.profile.Value("globals");
		objOptions.ReadSettingsFile();
		assertEquals("User ID", "kb", objOptions.user.Value());
		assertEquals("password", "kb", objOptions.password.Value());
	}

	@Test public void testCopy_Local2SFTP_recursive() throws Exception {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::CreateIniFile";
		logger.info("*********************************************** " + conMethodName + "******************");
		//		CreateIniFile();
		objOptions.settings.Value("R:/backup/sos/java/development/JADEUserInterface/TestData/jade_settings.ini");
		objOptions.profile.Value("Copy_Local2SFTP_recursive");
		objOptions.ReadSettingsFile();
		Assert.assertEquals("User ID", "test", objOptions.Target().user.Value());
		Assert.assertEquals("password", "12345", objOptions.Target().password.Value());
	}

	@Test public void testJadeConfig() throws Exception {
		final String conMethodName = conClassName + "::testJadeConfig";
		logger.info("*********************************************** " + conMethodName + "******************");
		objOptions.settings.Value("ConfigurationExample.jadeconf");
		objOptions.profile.Value("copylocal2local1");
		objOptions.ReadSettingsFile();
		logger.debug(objOptions.DirtyString());
		Assert.assertEquals("operation ", "copy", objOptions.operation.Value());
	}

	@Test(
			expected = com.sos.JSHelper.Exceptions.JobSchedulerException.class) public void testIniFile2() throws Exception {
		final String conMethodName = conClassName + "::testIniFile2";
		logger.info("*********************************************** " + conMethodName + "******************");
		CreateIniFile();
		objOptions.settings.Value(constrSettingsTestFile);
		objOptions.profile.Value("include-TestTest");
		objOptions.ReadSettingsFile();
		Assert.assertEquals("User ID", "kb", objOptions.user.Value());
		Assert.assertEquals("password", "kb", objOptions.password.Value());
	}

	@Test public void testIniFile3() throws Exception {
		final String conMethodName = conClassName + "::testIniFile2";
		logger.info("*********************************************** " + conMethodName + "******************");
		CreateIniFile();
		objOptions.settings.Value(constrSettingsTestFile);
		objOptions.profile.Value("include-Test");
		objOptions.ReadSettingsFile();
		objOptions.local_dir.Value(".");
		Assert.assertEquals("User ID", "kb", objOptions.user.Value());
		Assert.assertEquals("password", "kb", objOptions.password.Value());
		Assert.assertEquals("Hostname", "localhost", objOptions.host.Value());
		Assert.assertEquals("port", 88, objOptions.port.value());
		Assert.assertEquals("protocol", "scp", objOptions.protocol.Value());
		//		objOptions.CheckMandatory();
	}

	@Test(
			expected = com.sos.JSHelper.Exceptions.JobSchedulerException.class) public void testIniFile4() throws Exception {
		final String conMethodName = conClassName + "::testIniFile2";
		logger.info("*********************************************** " + conMethodName + "******************");
		CreateIniFile();
		objOptions.settings.Value(constrSettingsTestFile);
		objOptions.profile.Value("include-TestWithNonexistenceInclude");
		objOptions.ReadSettingsFile();
		Assert.assertEquals("User ID", "kb", objOptions.user.Value());
		Assert.assertEquals("password", "kb", objOptions.password.Value());
	}

	@Test public void testIniFile5() throws Exception {
		final String conMethodName = conClassName + "::testIniFile5";
		logger.info("*********************************************** " + conMethodName + "******************");
		CreateIniFile();
		objOptions.settings.Value(constrSettingsTestFile);
		objOptions.profile.Value("substitute-Test");
		objOptions.ReadSettingsFile();
		System.out.println(objOptions.DirtyString());
		String strComputerName = System.getenv("computername");
		assertEquals("User ID", System.getenv("username"), objOptions.user.Value());
		assertEquals("Hostname", strComputerName, objOptions.host.Value());
		assertEquals("Hostnameon Target ", strComputerName + "-abc", objOptions.getConnectionOptions().Target().HostName.Value());
	}

	@Test public void testIniFileWithSourceAndTarget() throws Exception {
		final String conMethodName = conClassName + "::testIniFile5";
		logger.info("*********************************************** " + conMethodName + "******************");
		CreateIniFile();
		objOptions.settings.Value(strSettingsFile);
		objOptions.profile.Value("ftp_server_2_server");
		objOptions.ReadSettingsFile();
		SOSConnection2Options objConn = objOptions.getConnectionOptions();
		String strComputerName = System.getenv("computername");
		Assert.assertEquals("Source.Host", conHostNameWILMA_SOS, objConn.Source().host.Value());
		Assert.assertEquals("Target.Host", conHostName8OF9_SOS, objConn.Target().host.Value());
		Assert.assertEquals("file_path", "test.txt", objOptions.file_path.Value());
		objOptions.CheckMandatory();
		// logger.info(objOptions.toString());
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		objJadeEngine.Execute();
	}

	@Test public void BRANDUP_MOND_CRM_POC() throws Exception {
		final String conMethodName = conClassName + "::BRANDUP_MOND_CRM_POC";
		logger.info("*********************************************** " + conMethodName + "******************");
		objOptions.settings.Value(strSettingsFile);
		objOptions.profile.Value("BRANDUP_MOND_CRM_POC");
		objOptions.ReadSettingsFile();
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		objJadeEngine.Execute();
	}

	@Test public void testAliasFromIniFile () {

		JSFile objIni = new JSFile(constrSettingsTestFile);
		try {
			objIni.WriteLine("[testAlias]");
			objIni.WriteLine("auth_method=password");
			objIni.WriteLine("verbose=9");
			objIni.close();
			objOptions = new JADEOptions();
			objOptions.settings.Value(constrSettingsTestFile);
			objOptions.profile.Value("testAlias");
			objOptions.ReadSettingsFile();

			assertEquals("Alias: auth_method", "password", objOptions.auth_method.Value());

		}
		catch (IOException e) {
			e.printStackTrace();
		}

	}
	private void CreateIniFile() throws Exception {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::CreateIniFile";
		JSFile objIni = new JSFile(constrSettingsTestFile);
		objIni.WriteLine("[globals]");
		objIni.WriteLine("globaluser=kb");
		objIni.WriteLine("globalpassword=kb");
		objIni.WriteLine("[include1]");
		objIni.WriteLine("host=localhost");
		objIni.WriteLine("[include2]");
		objIni.WriteLine("port=88");
		objIni.WriteLine("[include3]");
		objIni.WriteLine("protocol=scp");
		objIni.WriteLine("[include1_and_2]");
		objIni.WriteLine("include=include1,include2");
		objIni.WriteLine("[include-Test]");
		objIni.WriteLine("include=include1_and_2,include3");
		objIni.WriteLine("[include-TestWithNonexistenceInclude]");
		objIni.WriteLine("include=include1,includeabcd2,include3");
		objIni.WriteLine("[substitute-Test]");
		objIni.WriteLine("user=${USERNAME}");
		objIni.WriteLine("host=${COMPUTERNAME}");
		objIni.WriteLine("cannotsubstitutet=${waltraut}");
		objIni.WriteLine("title=${globaluser} and ${globalpassword}");
		objIni.WriteLine("target_host=${host}-abc");
		objIni.WriteLine("alternate_target_host=${host}-abc");
	}

	@Test public void testGenericIniFile1() throws Exception {
		executeGenericIniFile("", "cumulate_test");
	}

	@Test public void testGenericIniFile2() throws Exception {
		CreateTestFiles(15);
		executeGenericIniFile("", "cumulate_using_cumulative_file");
	}

	@Test public void testCopyFilesWithMD5() throws Exception {
		CreateTestFiles(15);
		executeGenericIniFile("", "copy_files_with_md5");
	}

	@Test public void testCopyFilesCheckMD5() throws Exception {
		CreateTestFiles(15);
		executeGenericIniFile("", "copy_files_check_md5");
	}

	@Test public void testGenericIniFile3() throws Exception {
		CreateTestFiles(15);
		executeGenericIniFile("", "ftp_receive_2_wilma");
	}

	@Test public void testBackgroundService() throws Exception {
		executeGenericIniFile("", "ftp_background");
	}

	@Test public void testWithoutLoadClassName() throws Exception {
		executeGenericIniFile("", "ftp_without_loadClassName");
	}

	@Test public void receive_zbf_relaxed() throws Exception {
		executeGenericIniFile("", "receive_zbf_relaxed");
	}

	@Test(
			expected = com.sos.JSHelper.Exceptions.JobSchedulerException.class) public void receive_zbf_strict() throws Exception {
		executeGenericIniFile("", "receive_zbf_strict");
	}

	@Test public void receive_zbf_no() throws Exception {
		executeGenericIniFile("", "receive_zbf_no");
	}

	@Test public void receive_zbf_no_noFiles() throws Exception {
		executeGenericIniFile("", "receive_zbf_no_noFiles");
	}

	 /* CS Test  */
	@Test
	public void ReceiveUsingKeePass() throws Exception {
		executeGenericIniFile("", "ReceiveUsingKeePass");
	}

	@Test
	public void SendUsingKeePass() throws Exception {
		executeGenericIniFile("", "SendUsingKeePass");
	}

	@Test
	public void P2PCopyUsingKeePass() throws Exception {
		executeGenericIniFile("", "P2PCopyUsingKeePass");
	}

	@Test
	public void ReceiveUsingKeePassExpired() throws Exception {
		executeGenericIniFile("", "ReceiveUsingKeePassExpired");
	}

	@Test
	public void SendUsingKeePassExpired() throws Exception {
		executeGenericIniFile("", "SendUsingKeePassExpired");
	}

	@Test
	public void P2PCopyUsingKeePassExpired() throws Exception {
		executeGenericIniFile("", "P2PCopyUsingKeePassExpired");
	}

	@Test
	public void ReceiveUsingSSHKeyKeePass() throws Exception {
		executeGenericIniFile("", "ReceiveUsingSSHKeyKeePass");
	}


	@Test
	public void ReceiveUsingSFTPURLKeePass() throws Exception {
		executeGenericIniFile("", "ReceiveUsingSFTPURLKeePass");
	}

	@Test
	public void ReceiveUsingKeePassSecuredWithPpk() throws Exception {
		executeGenericIniFile("", "ReceiveUsingKeePassSecuredWithPpk");
	}


	@Test public void receive_zbf_yes() throws Exception {
		executeGenericIniFile("", "receive_zbf_yes");
	}


	@Test (expected = com.sos.JSHelper.Exceptions.JobSchedulerException.class) public void sftp_receive_local_wrong_host() throws Exception {
		executeGenericIniFile("", "sftp_receive_local_wrong_host");
	}


	@Test(
			expected = com.sos.JSHelper.Exceptions.JobSchedulerException.class) public void receive_zbf_no_onlyzbf() throws Exception {
		executeGenericIniFile("", "receive_zbf_no_onlyzbf");
	}

	@Test public void send_zbf_yes() throws Exception {
		executeGenericIniFile("", "send_zbf_yes");
	}

	private void executeGenericIniFile(final String pstrIniFileName, final String pstrProfileName) throws Exception {
		System.out.println(System.getProperty("user.dir"));
		objOptions.settings.Value(strSettingsFile);
		objOptions.profile.Value(pstrProfileName);
//		objOptions.ReadSettingsFile();
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		objJadeEngine.Execute();
	}

	@Test public void testAliasSettings() throws Exception {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::testAliasSettings";
		HashMap<String, String> objHsh = new HashMap<String, String>();
		String strPassword = SOSOptionAuthenticationMethod.enuAuthenticationMethods.password.Text();

		objHsh.put("ssh_auth_method", strPassword);
		objOptions = new JADEOptions();
		objOptions.setAllOptions(objHsh);
		assertEquals("Default: ssh_auth_method", SOSOptionAuthenticationMethod.enuAuthenticationMethods.publicKey.Text().toLowerCase(), objOptions.auth_method.DefaultValue());
		assertEquals("Alias: ssh_auth_method", strPassword, objOptions.auth_method.Value());

		objHsh.put("source_ssh_auth_method", strPassword);
		objOptions = new JADEOptions();
		objOptions.setAllOptions(objHsh);
		assertEquals("Alias: source_ssh_auth_method", strPassword, objOptions.Source().auth_method.Value());

		objHsh.put("target_ssh_auth_method", strPassword);
		objOptions = new JADEOptions();
		objOptions.setAllOptions(objHsh);
		assertEquals("Alias: target_ssh_auth_method", strPassword, objOptions.Target().auth_method.Value());

		objHsh.put("source_auth_method", strPassword);
		objOptions = new JADEOptions();
		objOptions.setAllOptions(objHsh);
		assertEquals("Alias: source_auth_method", strPassword, objOptions.Source().auth_method.Value());


	}


	@Test public void testAliasSettings2() throws Exception {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::testAliasSettings2";
		HashMap<String, String> objHsh = new HashMap<String, String>();
		String strPassword = SOSOptionAuthenticationMethod.enuAuthenticationMethods.password.Text();

		objHsh.put("auth_method", strPassword);
		objOptions = new JADEOptions();
		objOptions.setAllOptions(objHsh);
		assertEquals("Alias: auth_method", strPassword, objOptions.auth_method.Value());

		objHsh.put("source_auth_method", strPassword);
		objOptions = new JADEOptions();
		objOptions.setAllOptions(objHsh);
		assertEquals("Alias: source_auth_method", strPassword, objOptions.Source().auth_method.Value());

		objHsh.put("target_auth_method", strPassword);
		objOptions = new JADEOptions();
		objOptions.setAllOptions(objHsh);
		assertEquals("Alias: target_auth_method", strPassword, objOptions.Target().auth_method.Value());

		//	objHsh.put("ssh_auth_method", "password");
		//	objHsh.put("source_host", conHostNameWILMA_SOS);
		//	objHsh.put("alternative_source_user", "sos");
		//	objHsh.put("alternative_source_password", "sos");
		//	objHsh.put("source_user", "sos");
		//	objHsh.put("source_port", "22");
		//	objHsh.put("source_protocol", "sftp");
		//	objHsh.put("source_password", "sos");
		//	objHsh.put("source_dir", "/home/sos/setup.scheduler/releases");
		//	objHsh.put("source_ssh_auth_method", "password");
		//	objHsh.put("target_host", "tux.sos");
		//	objHsh.put("target_protocol", "sftp");
		//	objHsh.put("target_port", "22");
		//	objHsh.put("target_password", "sos");
		//	objHsh.put("target_user", "sos");
		//	objHsh.put("alternative_target_user", "abcdef");
		//	objHsh.put("target_dir", "/srv/www/htdocs/test");
		//	objHsh.put("target_ssh_auth_method", "password");
		//	objHsh.put("overwrite_files", "true");
		//	objHsh.put("check_size", "true");
		//	objHsh.put("file_spec", "^scheduler_(win32|linux)_joe\\.[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]{4}\\.(tar\\.gz|zip)$");
		//	objHsh.put("recursive", "false");
		//	// objHsh.put("remote_dir", "/srv/www/htdocs/test");
		//	// objHsh.put("local_dir", "/home/sos/setup.scheduler/releases");
		//	objHsh.put("verbose", "9");
		//	objHsh.put("buffer_size", "32000");
		//	objHsh.put("SendTransferHistory", "false");
		//	objHsh.put("log_filename", "c:/temp/test.log");
		//	objOptions = new JADEOptions();
		//	objOptions.setAllOptions(objOptions.DeletePrefix(objHsh, "ftp_"));
		//	assertEquals("", conHostNameWILMA_SOS, objOptions.getConnectionOptions().Source().host.Value());
		//	assertEquals("", "tux.sos", objOptions.getConnectionOptions().Target().host.Value());
		//	assertEquals("", "/srv/www/htdocs/test/", objOptions.TargetDir.Value());
		//	assertEquals("log filename not set", "c:/temp/test.log", objOptions.log_filename.Value());
		//	assertEquals("log filename not set", "c:/temp/test.log", objOptions.OptionByName("log_filename"));
		//	String strReplTest = "Hallo, welt %{log_filename} und \nverbose = %{verbose} ersetzt. Date %{date} wird nicht ersetzt";
		//	String strR = objOptions.replaceVars(strReplTest);
		//	System.out.println(strR);
		//	strReplTest = "Hallo, welt %{log_filename} und" + "\n" + "verbose = %{verbose} ersetzt. Date %{date} wird nicht ersetzt";
		//	strR = objOptions.replaceVars(strReplTest);
		//	System.out.println(strR);
		//	assertEquals("log filename not set", "sos", objOptions.getConnectionOptions().Source().Alternatives().user.Value());
		//	assertEquals("log filename not set", "abcdef", objOptions.getConnectionOptions().Target().Alternatives().user.Value());
	} // private void testHashMapSettings

	@Test public void testHashMapSettings() throws Exception {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::testHashMapSettings";
		HashMap<String, String> objHsh = new HashMap<String, String>();
		objHsh.put("operation", "copy");
		objHsh.put("source_host", conHostNameWILMA_SOS);
		objHsh.put("alternative_source_user", "sos");
		objHsh.put("alternative_source_password", "sos");
		objHsh.put("source_user", "sos");
		objHsh.put("source_port", "22");
		objHsh.put("source_protocol", "sftp");
		objHsh.put("source_password", "sos");
		objHsh.put("source_dir", "/home/sos/setup.scheduler/releases");
		objHsh.put("source_ssh_auth_method", "password");
		objHsh.put("target_host", "tux.sos");
		objHsh.put("target_protocol", "sftp");
		objHsh.put("target_port", "22");
		objHsh.put("target_password", "sos");
		objHsh.put("target_user", "sos");
		objHsh.put("alternative_target_user", "abcdef");
		objHsh.put("target_dir", "/srv/www/htdocs/test");
		objHsh.put("target_ssh_auth_method", "password");
		objHsh.put("overwrite_files", "true");
		objHsh.put("check_size", "true");
		objHsh.put("file_spec", "^scheduler_(win32|linux)_joe\\.[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]{4}\\.(tar\\.gz|zip)$");
		objHsh.put("recursive", "false");
		// objHsh.put("remote_dir", "/srv/www/htdocs/test");
		// objHsh.put("local_dir", "/home/sos/setup.scheduler/releases");
		objHsh.put("verbose", "9");
		objHsh.put("buffer_size", "32000");
		objHsh.put("SendTransferHistory", "false");
		objHsh.put("log_filename", "c:/temp/test.log");
		objOptions = new JADEOptions();
		objOptions.setAllOptions(objOptions.DeletePrefix(objHsh, "ftp_"));
		assertEquals("", conHostNameWILMA_SOS, objOptions.getConnectionOptions().Source().host.Value());
		assertEquals("", "tux.sos", objOptions.getConnectionOptions().Target().host.Value());
		assertEquals("", "/srv/www/htdocs/test/", objOptions.TargetDir.Value());
		assertEquals("log filename not set", "c:/temp/test.log", objOptions.log_filename.Value());
		assertEquals("log filename not set", "c:/temp/test.log", objOptions.OptionByName("log_filename"));
		String strReplTest = "Hallo, welt %{log_filename} und \nverbose = %{verbose} ersetzt. Date %{date} wird nicht ersetzt";
		String strR = objOptions.replaceVars(strReplTest);
		System.out.println(strR);
		strReplTest = "Hallo, welt %{log_filename} und" + "\n" + "verbose = %{verbose} ersetzt. Date %{date} wird nicht ersetzt";
		strR = objOptions.replaceVars(strReplTest);
		System.out.println(strR);
		assertEquals("log filename not set", "sos", objOptions.getConnectionOptions().Source().Alternatives().user.Value());
		assertEquals("log filename not set", "abcdef", objOptions.getConnectionOptions().Target().Alternatives().user.Value());
	} // private void testHashMapSettings

	@Test(
			expected = com.sos.JSHelper.Exceptions.JobSchedulerException.class) public void testSendWithHashMapSettings() throws Exception {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::testHashMapSettings";
		HashMap<String, String> objHsh = new HashMap<String, String>();
		objHsh.put("operation", "send");
		objHsh.put("local_dir", "c:/temp/");
		objHsh.put("host", "tux.sos");
		objHsh.put("protocol", "sftp");
		objHsh.put("port", "22");
		objHsh.put("password", "sos");
		objHsh.put("user", "sos");
		objHsh.put("remote_dir", "/srv/www/htdocs/test");
		objHsh.put("ssh_auth_method", SOSOptionAuthenticationMethod.enuAuthenticationMethods.password.text);
		// objHsh.put("ssh_auth_method", SOSOptionAuthenticationMethod.enuAuthenticationMethods.publicKey.text);
		objHsh.put("overwrite_files", "true");
		objHsh.put("check_size", "true");
		objHsh.put("file_spec", "^scheduler_(win32|linux)_joe\\.[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]{4}\\.(tar\\.gz|zip)$");
		objHsh.put("recursive", "false");
		objHsh.put("verbose", "9");
		objHsh.put("SendTransferHistory", "false");
		objOptions = new JADEOptions();
		objOptions.setAllOptions(objOptions.DeletePrefix(objHsh, "ftp_"));
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		objOptions.CheckMandatory();
		/**
		 * the sequence of the coding is essential. in checkmandatory the source- and target-class is
		 * populated with the needed values.
		 */
		assertEquals("", "localhost", objOptions.getConnectionOptions().Source().host.Value());
		assertEquals("", "tux.sos", objOptions.getConnectionOptions().Target().host.Value());
		assertEquals("", "sftp", objOptions.getConnectionOptions().Target().protocol.Value());
		objJadeEngine.Execute();
	} // private void testHashMapSettings

	@Test public void testSendWithHashMapSettings2() throws Exception {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::testSendWithHashMapSettings2";
		HashMap<String, String> objHsh = new HashMap<String, String>();
		objHsh.put("operation", "send");
		objHsh.put("local_dir", "c:/temp/");
		objHsh.put("host", "tux.sos");
		// objHsh.put("protocol", "sftp");
		objHsh.put("port", "22");
		objHsh.put("password", "sos");
		objHsh.put("user", "sos");
		objHsh.put("remote_dir", "/srv/www/htdocs/test");
		objHsh.put("ssh_auth_method", SOSOptionAuthenticationMethod.enuAuthenticationMethods.password.text);
		// objHsh.put("ssh_auth_method", SOSOptionAuthenticationMethod.enuAuthenticationMethods.publicKey.text);
		objHsh.put("overwrite_files", "true");
		objHsh.put("check_size", "true");
		objHsh.put("file_spec", "^scheduler_(win32|linux)_joe\\.[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]{4}\\.(tar\\.gz|zip)$");
		objHsh.put("recursive", "false");
		objHsh.put("verbose", "9");
		objHsh.put("SendTransferHistory", "false");
		objOptions = new JADEOptions();
		objOptions.protocol.Value(enuTransferTypes.ftp);
		objOptions.setAllOptions(objOptions.DeletePrefix(objHsh, "ftp_"));
		JadeEngine objJadeEngine = new JadeEngine(objOptions);
		objOptions.CheckMandatory();
		/**
		 * the sequence of the coding is essential. in checkmandatory the source- and target-class is
		 * populated with the needed values.
		 */
		assertEquals("", "localhost", objOptions.getConnectionOptions().Source().host.Value());
		assertEquals("", "tux.sos", objOptions.getConnectionOptions().Target().host.Value());
		assertEquals("", "ftp", objOptions.getConnectionOptions().Target().protocol.Value());
		// objJadeEngine.Execute();
	} // private void testHashMapSettings

	@Test public void testHashMapSettings3() throws Exception {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::testHashMapSettings3";
		HashMap<String, String> objHsh = new HashMap<String, String>();
		objHsh.put("operation", "copy");
		objHsh.put("source_host", conHostNameWILMA_SOS);
		objHsh.put("source_user", "sos");
		objHsh.put("source_port", "22");
		objHsh.put("source_protocol", "sftp");
		objHsh.put("source_password", "sos");
		objHsh.put("source_dir", "/home/sos/setup.scheduler/releases");
		objHsh.put("source_ssh_auth_method", "password");
		objHsh.put("target_host", "tux.sos");
		objHsh.put("target_protocol", "sftp");
		objHsh.put("target_port", "22");
		objHsh.put("target_password", "sos");
		objHsh.put("target_user", "sos");
		objHsh.put("target_dir", "/srv/www/htdocs/test");
		objHsh.put("target_ssh_auth_method", "password");
		objHsh.put("overwrite_files", "true");
		objHsh.put("check_size", "true");
		objHsh.put("file_spec", "^scheduler_(win32|linux)_joe\\.[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]{4}\\.(tar\\.gz|zip)$");
		objHsh.put("recursive", "false");
		// objHsh.put("remote_dir", "/srv/www/htdocs/test");
		// objHsh.put("local_dir", "/home/sos/setup.scheduler/releases");
		objHsh.put("verbose", "9");
		objHsh.put("buffer_size", "32000");
		objHsh.put("SendTransferHistory", "false");
		String strCmd = "SITE chmod 777 $SourceFileName";
		objHsh.put("source_pre_command", strCmd);
		objHsh.put("target_pre_command", strCmd);
		objOptions = new JADEOptions();
		objOptions.setAllOptions(objOptions.DeletePrefix(objHsh, "ftp_"));
		assertEquals("", conHostNameWILMA_SOS, objOptions.getConnectionOptions().Source().host.Value());
		assertEquals("", "tux.sos", objOptions.getConnectionOptions().Target().host.Value());
		assertEquals("", "/srv/www/htdocs/test/", objOptions.TargetDir.Value());
		assertEquals("source", strCmd, objOptions.Source().Pre_Command.Value());
		assertEquals("target", strCmd, objOptions.Target().Pre_Command.Value());
		String strT2 = strCmd.replace("$SourceFileName", "testfile");
		assertEquals("target", "SITE chmod 777 testfile", strT2);
	} // private void testHashMapSettings

	@Test public void testControlMChar() {
		char x0d = 0x0d;
		Pattern	SECTION_PATTERN	= Pattern.compile("^\\s*\\[([^\\]]*)\\].*$");

		String strT = "[profilename]\r";
//		String strT = "[profilename]";
		strT = strT.replaceAll("\\r", "");

		Matcher matcher = SECTION_PATTERN.matcher(strT);
		if (matcher.matches()) {
			String sectionName = matcher.group(1);
			System.out.println(sectionName);
		}
		else {
			System.out.println("no match in range");
		}

		if (strT.contains("\r")) {
			System.out.println("enthalten.");
		}
		if (strT.contains("\r") == false) {
			System.out.println("nicht enthalten.");
		}
		System.out.println(strT);
	}
}
