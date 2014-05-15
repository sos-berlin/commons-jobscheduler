/**
 *
 */
package com.sos.DataExchange;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.sos.JSHelper.DataElements.JSDataElementDate;
import com.sos.JSHelper.Options.SOSOptionTransferType.enuTransferTypes;
import com.sos.JSHelper.io.Files.JSFile;

/**
 * @author KB
 *
 */
public class JadeTestLocal extends JadeTestBase {


	/**
	 *
	 */
	public JadeTestLocal() {
		// TODO Auto-generated constructor stub
	}

	@Override
	@Test
	public void testSendRegExpAsFileName() throws Exception {
		super.testSendRegExpAsFileName();
	}

	@Override
	@Test
	public void testTransferUsingFilePath() throws Exception {
		CreateTestFile("test.txt");
		gstrFilePath = "test.txt";
		super.testTransferUsingFilePath();
	}

	@Test (expected=com.sos.JSHelper.Exceptions.JobSchedulerException.class)
	public void testTransferUsingWrongFilePath() throws Exception {
		gstrFilePath = "test-test-test.txt";
		super.testTransferUsingFilePath();
	}

	@Override
	@Test (expected=com.sos.JSHelper.Exceptions.JobSchedulerException.class)
	public void testSendWrongFileSpec() throws Exception {
		super.testSendWrongFileSpec();
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
	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();
		enuSourceTransferType = enuTransferTypes.local;
		enuTargetTransferType = enuTransferTypes.local;

		objTestOptions.SourceDir.Value(strTestPathName );
		objTestOptions.TargetDir.Value(strTestPathName + "/SOSMDX/");

		objTestOptions.Source().protocol.Value(enuSourceTransferType);
		objTestOptions.Target().protocol.Value(enuTargetTransferType);
	}

	@Test
	public void testUseSubstitution() throws Exception {
		final String conMethodName = conClassName + "::testUseProfile";
		objOptions.settings.Value(strSettingsFile);
		objOptions.profile.Value("substitute_example");
		objOptions.ReadSettingsFile();

		logger.info(objOptions.dirtyString());
	}


	@Override
	@Test
	public void testUseProfile() throws Exception {
		final String conMethodName = conClassName + "::testUseProfile";
		objOptions.settings.Value(strSettingsFile);
		objOptions.profile.Value("getList_example");
		super.testUseProfile();
	}

	@Test
	public void testgetList_variable_filespec_example() throws Exception {
		final String conMethodName = conClassName + "::getList_variable_filespec_example";
		objOptions.settings.Value(strSettingsFile);
		objOptions.profile.Value("getList_variable_filespec_example");
		CreateTestFile(String.format("TestFile_%1$s.123", JSDataElementDate.getCurrentTimeAsString("yyyyMMdd")));
		super.testUseProfile2();
	}

	@Test
	public void testFtpReceive2Wilma() throws Exception {
		final String conMethodName = conClassName + "::testUseProfile";
		objOptions.settings.Value(strSettingsFile);
		objOptions.profile.Value("ftp_receive_2_wilma");
		super.testUseProfile();
	}

	@Test
	public void CopyAndRenameSourceAndTarget() throws Exception {
		final String conMethodName = conClassName + "::CopyAndRenameSourceAndTarget_Local2Local";
		objOptions.settings.Value(strSettingsFile);
		objOptions.profile.Value("CopyAndRenameSourceAndTarget_Local2Local");
		super.testUseProfile();
	}

	@Test
	public void CopyAndCreateVariableFolder() throws Exception {
		final String conMethodName = conClassName + "::CopyAndRenameSourceAndTarget";
		objOptions.settings.Value(strSettingsFile);
		objOptions.profile.Value("CopyAndCreateVariableFolder_Local2Local");
		super.testUseProfile();
	}

	@Test
	public void CopyAndMoveSource() throws Exception {
		final String conMethodName = conClassName + "::CopyAndMoveSource";
		objOptions.settings.Value(strSettingsFile);
		objOptions.profile.Value("CopyAndMoveSource_Local2Local");
		super.testUseProfile();
	}

	@Test
	public void CopyAndMoveSource2NewFolder() throws Exception {
		final String conMethodName = conClassName + "::CopyAndMoveSource2NewFolder";
		objOptions.settings.Value(strSettingsFile);
		objOptions.profile.Value("CopyAndMoveSource2NewFolder_Local2Local");
		super.testUseProfile();
		JSFile objFile = new JSFile(strTestPathName, "UNKNOWNFOLDER/Masstest00000.txt");
		assertTrue("File " + objFile.getAbsolutePath() + " must exist", objFile.exists());
	}

	@Test
	public void CopyAndRenameSource() throws Exception {
		final String conMethodName = conClassName + "::CopyAndRenameSource";
		objOptions.settings.Value(strSettingsFile);
		objOptions.profile.Value("CopyAndRenameSource_Local2Local");
		super.testUseProfile();
	}

	@Test
	public void CopyUsingUNCNames() throws Exception {
		final String conMethodName = conClassName + "::CopyUsingUNCNames";
		objOptions.settings.Value(strSettingsFile);
		objOptions.profile.Value("Copy_Local2Local_UNC");
		super.testUseProfileWOCreatingTestFiles();
	}

	@Test
	public void CopyUsingUNCNamesWithNetUse() throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::CopyUsingUNCNamesWithNetUse";
		objOptions.settings.Value(strSettingsFile);
		objOptions.profile.Value("Copy_Local2Local_UNC_withNetUse");
		super.testUseProfileWOCreatingTestFiles();
	}

	@Test
	public void Copy_Local2Local_recursive() throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::Copy_Local2Local_recursive";
		objOptions.settings.Value(strSettingsFile);
		objOptions.profile.Value("Copy_Local2Local_recursive");
		super.testUseProfileWOCreatingTestFiles();
	}

	@Override
	@Test
	public void testExecuteGetFileList() throws Exception {
		super.testExecuteGetFileList();
	}

	@Override
	@Test
	public void testCopyAndCreateVariableFolder() throws Exception {
		super.testCopyAndCreateVariableFolder();
	}

	@Override
	@Test
	public void testCopyAndRenameSourceAndTarget() throws Exception {
		super.testCopyAndRenameSourceAndTarget();
	}

	@Override
	@Test
	public void testCopyAndRenameSource() throws Exception {
		super.testCopyAndRenameSource();
	}

	@Override
	@Test
	public void testCopyMultipleFiles() throws Exception {
		super.testCopyMultipleFiles();
	}

	@Override
	@Test
	public void testCopyMultipleFilesThreaded() throws Exception {
		super.testCopyMultipleFilesThreaded();
	}

	@Override
	@Test
	public void testBigCopyThreaded() throws Exception {
		this.testBigCopy();
	}

	 @Test (expected=com.sos.JSHelper.Exceptions.JobSchedulerException.class)
	public void testSendWithPollingWithoutWait4SourceDir() throws Exception  {
		final String conMethodName = conClassName + "::testSendWithPolling";
		logMethodName(conMethodName);
		objTestOptions.SourceDir.Value(strTestPathName + "/badname/");

		super.testSendWithPolling();
	}

	  @Override @Test
	public void testSendWithPolling() throws Exception {
		final String conMethodName = conClassName + "::testSendWithPolling";
		logMethodName(conMethodName);
		objTestOptions.SourceDir.Value(strTestPathName + "/badname/");
		objTestOptions.pollingWait4SourceFolder.setTrue();
		super.testSendWithPolling();
	}


	@Override
	@Test
	public void testCopyWithFileList() throws Exception {
		super.testCopyWithFileList();
	}

	@Override
	@Test
	public void testBigCopy() throws Exception {
		objTestOptions.SourceDir.Value(conSourceOfDOXYGEN_DOCS);
		objTestOptions.TargetDir.Value(conTargetOfDOXYGEN_DOCS);
		super.testBigCopy();
	}

	@Test
	public void testBigCopy2() throws Exception {
		objTestOptions.SourceDir.Value(conSourceOfDOXYGEN_DOCS + "SOSVirtualFileSystem/");
		objTestOptions.TargetDir.Value(conTargetOfDOXYGEN_DOCS+  "SOSVirtualFileSystem/");
		super.testBigCopy();
	}

	@Override
	@Test
	public void testCopyMultipleResultList() throws Exception {
		super.testCopyMultipleResultList();
	}

	@Override
	@Test
	public void testSendAndDeleteMultipleFiles() throws Exception {
		super.testSendAndDeleteMultipleFiles();
	}

	@Override
	@Test
	public void testRenameFiles() throws Exception {
		super.testRenameFiles();
	}

	@Override
	@Test
	public void testSend() throws Exception {
		final String conMethodName = conClassName + "::testSend";
		super.testSend();
	}

	@Test
	public void testSendAndCreateMd5Hash() throws Exception {
		final String conMethodName = conClassName + "::testSend";
		objTestOptions.CreateSecurityHash.setTrue();
		objTestOptions.CreateSecurityHashFile.setTrue();
		super.testSend();
	}

	@Test
	public void testSendAndCreatesha256Hash() throws Exception {
		final String conMethodName = conClassName + "::testSend";
		objTestOptions.CreateSecurityHash.setTrue();
		objTestOptions.CreateSecurityHashFile.setTrue();
		objTestOptions.SecurityHashType.Value("SHA-256");
		super.testSend();
	}

	@Test
	public void testSendWithMd5Check() throws Exception {
		final String conMethodName = conClassName + "::testSend";
		objTestOptions.CheckSecurityHash.setTrue();
		super.testSend();
	}

	@Override
	@Test
	public void testDeleteFiles2() throws Exception {
		super.testDeleteFiles2();
	}

	@Override
	@Test
	public void testCopyWithFolderInSourceDir() throws Exception {
		super.testCopyWithFolderInSourceDir();
	}

	@Test
	public void CopyAndCheckSteadyState_Local2Local() throws Exception {
		final String conMethodName = conClassName + "::CopyAndCheckSteadyState_Local2Local";
		objOptions.settings.Value(strSettingsFile);
		objOptions.profile.Value("CopyAndCheckSteadyState_Local2Local");
		super.testUseProfile();
	}

}
