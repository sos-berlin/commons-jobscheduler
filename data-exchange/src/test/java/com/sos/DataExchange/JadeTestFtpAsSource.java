package com.sos.DataExchange;

import org.junit.Before;
import org.junit.Test;

import com.sos.JSHelper.Options.SOSOptionPortNumber;
import com.sos.JSHelper.Options.SOSOptionTransferType.enuTransferTypes;

public class JadeTestFtpAsSource extends JadeTestBase {

	protected final String	WEB_URI				= "https://mediacenter.gmx.net";
	protected final String	WEB_USER			= "sos.apl@gmx.de";
	protected final String	WEB_PASS			= "sosapl10629";
	protected final String	REMOTE_BASE_PATH	= "/home/kb/";

	public JadeTestFtpAsSource() {
		enuSourceTransferType = enuTransferTypes.ftp;
		enuTargetTransferType = enuTransferTypes.local;
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
		enuSourceTransferType = enuTransferTypes.ftp;
		enuTargetTransferType = enuTransferTypes.local;
		super.setUp();

		objTestOptions.passive_mode.setTrue();

		objTestOptions.TargetDir.Value(strTestPathName);

		objTestOptions.Target().protocol.Value(enuTargetTransferType);
		objTestOptions.Target().user.Value(conUserIdTest);
		objTestOptions.Target().password.Value(conPasswordTest);
		objTestOptions.Target().host.Value("local");
		objTestOptions.Target().protocol.Value(enuTargetTransferType);
		objTestOptions.Target().port.value(SOSOptionPortNumber.conPort4FTP);
		objOptions.Target().port.value(SOSOptionPortNumber.conPort4FTP);

		objTestOptions.Source().protocol.Value(enuSourceTransferType);

		objTestOptions.SourceDir.Value("/home/test/jadetest/SOSDEX/");
		objTestOptions.Source().host.Value(conHostNameWILMA_SOS);
		objTestOptions.Source().port.value(SOSOptionPortNumber.conPort4FTP);
		objTestOptions.Source().user.Value(conUserIdTest);
		objTestOptions.Source().password.Value(conPasswordTest);
	}

	@Override
	@Test
	public void testReceiveMultipleFiles2 () throws Exception {
		super.testReceiveMultipleFiles2();
	}
	@Override
//	@Test
	public void testBigCopy() throws Exception {
		super.testBigCopy();
	}

//	@Test
	public void testBigCopy2() throws Exception {
		super.testBigCopy();
	}

	@Override
	@Test
	public void testBigCopyThreaded() throws Exception {
		this.testBigCopy();
	}

	@Override
	@Test
	public void testCopyAndRenameSource() throws Exception {
		super.testCopyAndRenameSource();
	}

	@Override
	@Test
	public void testCopyMultipleFiles() throws Exception {
//		objTestOptions.Target().host.Value("homer.sos");
		objTestOptions.transactional.value(true);
		super.testCopyMultipleFiles();
	}

	@Override
	@Test
	public void testCopyMultipleFilesThreaded() throws Exception {
		super.testCopyMultipleFilesThreaded();
	}

	@Override
	@Test
	public void testCopyMultipleResultList() throws Exception {
		super.testCopyMultipleResultList();
	}

	@Test
	public void testCopyRecursiveWithFolderInSourceDir() throws Exception {
		objTestOptions.recursive.setTrue();
		objTestOptions.makeDirs.setTrue();
		objTestOptions.SourceDir.Value("/home/test/jadetest/SOSDEX");
		super.testCopyWithFolderInSourceDir();
		objTestOptions.recursive.setFalse();
	}

	@Override
	@Test
	public void testCopyWithFileList() throws Exception {
		super.testCopyWithFileList();
	}

	@Override
	@Test
	public void testCopyWithFolderInSourceDir() throws Exception {
		objTestOptions.SourceDir.Value("/home/test/jadetest/SOSDEX");
		super.testCopyWithFolderInSourceDir();
	}

	@Override
	@Test
	public void testDeleteFiles() throws Exception {
		objTestOptions.FileNameRegExp.push();
		objTestOptions.FileNameRegExp.Value("^Masstest.*\\.txt$");
		super.testDeleteFiles();
		objTestOptions.FileNameRegExp.pop();
	}

	@Test (expected=com.sos.JSHelper.Exceptions.JobSchedulerException.class)
	public void testDeleteFilesWithError() throws Exception {
		objTestOptions.ErrorOnNoDataFound.push();
		objTestOptions.ErrorOnNoDataFound.setTrue();
		objTestOptions.FileNameRegExp.Value("^Masstest.\\.txt$");
		super.testDeleteFiles();
		objTestOptions.ErrorOnNoDataFound.pop();
	}

	@Test
	public void testDeleteFilesWithForce() throws Exception {
		objTestOptions.ErrorOnNoDataFound.push();
		objTestOptions.ErrorOnNoDataFound.setFalse();
		objTestOptions.FileNameRegExp.Value("^Masstest.\\.txt$");
		super.testDeleteFiles();
		objTestOptions.ErrorOnNoDataFound.pop();
	}

	@Override
	@Test
	public void testExecuteGetFileList() throws Exception {
		super.testExecuteGetFileList();
	}

	@Override
	@Test
	public void testReceiveFileWithRelativeSourceDir() throws Exception {

		super.testReceiveFileWithRelativeSourceDir();
	}

	@Override
	@Test
	public void testReceiveWithSymlinkInRemoteDir() throws Exception {
		super.testReceiveWithSymlinkInRemoteDir();
	}

	@Override
	@Test
	public void testRenameFiles() throws Exception {
		super.testRenameFiles();
	}

	@Override
	@Test
	public void testRenameFiles2FolderWhichNotExist() throws Exception {
		super.testRenameFiles2FolderWhichNotExist();
	}

	@Override
	@Test
	public void testSend() throws Exception {
		final String conMethodName = conClassName + "::testSend";
		super.testSend();
	}

	@Override
	@Test
	public void testSendAndDeleteMultipleFiles() throws Exception {
		super.testSendAndDeleteMultipleFiles();
	}

	@Override
	@Test
	public void testSendFileSpec() throws Exception {
		final String conMethodName = conClassName + "::testSend";
		super.testSendFileSpec();
	}


	@Override
	@Test(expected = com.sos.JSHelper.Exceptions.JobSchedulerException.class)
	public void testSendWithPolling0Files() throws Exception {
		super.testSendWithPolling0Files();
	}

	@Test
	public void testTransferUsingAbsolutFilePath() throws Exception {
		/**
		 * Error and endles loop:
		 * main DEBUG 01:47:51,405   DEBUG (SOSVfsFtpBaseClass.java:636) ::LogReply 550 Failed to open file.
		 */
		gstrFilePath = "/home/test/tmp/myfile_20120801.csv";
		objTestOptions.SourceDir.Value("");
		
		super.testTransferUsingFilePath();
	}

	@Test
	public void testTransferUsingAbsolutFilePath2() throws Exception {
		/**
		 * Error and endles loop:
		 * main DEBUG 01:47:51,405   DEBUG (SOSVfsFtpBaseClass.java:636) ::LogReply 550 Failed to open file.
		 */
		gstrFilePath = "/home/test/tmp/myfile_20120801.csv;/home/test/tmp/test.kb";
		objTestOptions.SourceDir.Value("");
		super.testTransferUsingFilePath();
	}

	@Override
	@Test
	public void testTransferUsingFilePath() throws Exception {
		/**
		 * Error and endles loop:
		 * main DEBUG 01:47:51,405   DEBUG (SOSVfsFtpBaseClass.java:636) ::LogReply 550 Failed to open file.
		 */
		gstrFilePath = "myfile_20120801.csv";
		objTestOptions.SourceDir.Value("/home/test/tmp/");
		super.testTransferUsingFilePath();
	}

	@Test
	public void testTransferUsingFilePath2() throws Exception {
		/**
		 * Error and endles loop:
		 * main DEBUG 01:47:51,405   DEBUG (SOSVfsFtpBaseClass.java:636) ::LogReply 550 Failed to open file.
		 */
		gstrFilePath = "myfile_20120801.csv;test.kb";
		objTestOptions.SourceDir.Value("/home/test/tmp/");
		super.testTransferUsingFilePath();
	}

	@Test
	public void testTransferUsingRelativeFilePath() throws Exception {
		gstrFilePath = "./tmp/myfile_20120801.csv";
		super.testTransferUsingFilePath();
	}

	@Test
	public void testTransferUsingRelativeFilePath2() throws Exception {
		gstrFilePath = "tmp/myfile_20120801.csv";
		super.testTransferUsingFilePath();
	}

	@Test
	public void testUseProfileFilespec2() throws Exception {
		final String conMethodName = conClassName + "::testUseProfile";
		objOptions.settings.Value(strSettingsFile);
		objOptions.profile.Value("filespec2");
		super.testUseProfileWithoutCreatingTestFiles();
	}

	@Test
	public void test_send_local2ftp_file_spec_5() throws Exception {
		final String conMethodName = conClassName + "::test_send_local2ftp_file_spec_5";
		objOptions.settings.Value(strSettingsFile);
		objOptions.profile.Value("send_local2ftp_file_spec_5");
		super.testUseProfileWithoutCreatingTestFiles();
	}

	@Test
	public void testUseProfileWithAsciiMode() throws Exception {
		final String conMethodName = conClassName + "::testUseProfile";
		objOptions.settings.Value(strSettingsFile);
		objOptions.profile.Value("copyWithAsciiMode");
		super.testUseProfileWithoutCreatingTestFiles();
	}

	@Override
	@Test
	public void testUseProfileWithoutCreatingTestFiles() throws Exception {
		final String conMethodName = conClassName + "::testUseProfileWithoutCreatingTestFiles";
		objOptions.settings.Value(strSettingsFile);
		objOptions.profile.Value("getList_example_ftp");
		super.testUseProfileWithoutCreatingTestFiles();
	}

	@Test
	public void jadeHomer2Local() throws Exception {
		final String conMethodName = conClassName + "::jadeHomer2Local";
		objOptions.settings.Value(strSettingsFile);
		objOptions.profile.Value("jadeHomer2Local");
		objOptions.verbose.value(2);
		super.testUseProfileWithoutCreatingTestFiles();
	}
	
	@Test
	public void jadeAlternativeHomer2Local() throws Exception {
		final String conMethodName = conClassName + "::testUseProfile";
		objOptions.settings.Value(strSettingsFile);
		objOptions.profile.Value("jadeAlternativeHomer2Local");
		super.testUseProfileWithoutCreatingTestFiles();
	}
	
}
