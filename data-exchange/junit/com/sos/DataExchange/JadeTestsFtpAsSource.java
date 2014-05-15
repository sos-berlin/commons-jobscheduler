package com.sos.DataExchange;


import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import com.sos.DataExchange.Options.JADEOptions;
import com.sos.JSHelper.Options.SOSOptionAuthenticationMethod.enuAuthenticationMethods;
import com.sos.JSHelper.Options.SOSOptionJadeOperation.enuJadeOperations;
import com.sos.JSHelper.Options.SOSOptionPortNumber;
import com.sos.JSHelper.Options.SOSOptionTransferType.enuTransferTypes;
import com.sos.JSHelper.io.Files.JSCsvFile;


public class JadeTestsFtpAsSource extends JadeTestBase {

	public JadeTestsFtpAsSource() {
		enuSourceTransferType = enuTransferTypes.sftp;
		enuTargetTransferType = enuTransferTypes.local;
	}

	/**
	 * \brief setUp
	 *
	 * \details
	 *
	 * \return void
	 *
	 * @throws java.lang.Exceptionf
	 */
	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();

		enuSourceTransferType = enuTransferTypes.sftp;
		enuTargetTransferType = enuTransferTypes.local;

		objTestOptions.TargetDir.Value(strTestPathName);

		objTestOptions.Target().protocol.Value(enuTargetTransferType);
		objTestOptions.Target().user.Value(conUserIdTest);
		objTestOptions.Target().password.Value(conPasswordTest);
		objTestOptions.Target().host.Value("local");
		objTestOptions.Target().protocol.Value(enuTargetTransferType);

		objTestOptions.Source().protocol.Value(enuSourceTransferType);
		objTestOptions.Target().protocol.Value(enuTargetTransferType);

		objTestOptions.SourceDir.Value("/home/test/jadetest/SOSDEX");
//		objTestOptions.SourceDir.Value("/home/test/jadetest");
		objTestOptions.Source().host.Value(conHostNameWILMA_SOS);
		objTestOptions.Source().port.value(SOSOptionPortNumber.conPort4SFTP);
		objTestOptions.Source().user.Value(conUserIdTest);
		objTestOptions.Source().password.Value(conPasswordTest);
		objTestOptions.Source().auth_method.Value(enuAuthenticationMethods.password);

		objOptions.profile.Value(conClassName);
	}

	@Override
	@Test
	public void testReceiveWithSymlinkInRemoteDir() throws Exception {
		super.testReceiveWithSymlinkInRemoteDir();
	}


	@Override
	@Test
	public void testUseProfileWithoutCreatingTestFiles() throws Exception {
		final String conMethodName = conClassName + "::testUseProfile";
		objOptions.settings.Value(strSettingsFile);
		objOptions.profile.Value("getList_example_sftp");
		super.testUseProfileWithoutCreatingTestFiles();
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
	public void testTransferUsingAbsolutFilePath() throws Exception {
		/**
		 * Error and endles loop:
		 * main DEBUG 01:47:51,405   DEBUG (SOSVfsFtpBaseClass.java:636) ::LogReply 550 Failed to open file.
		 */
		gstrFilePath = "/home/test/tmp/myfile_20120801.csv";
		objTestOptions.SourceDir.Value("");
		//objTestOptions.remove_files.value(true);
		//objTestOptions.Source().loadClassName.Value("com.sos.VirtualFileSystem.SFTP.SOSVfsSFtpJCraft");
		
		super.testTransferUsingFilePath();
	}

	@Test
	public void testTransferUsingAbsolutFilePath2() throws Exception {
		/**
		 * Felsing-test
		 */
		gstrFilePath = "/home/test/tmp/myfile_20120801.csv";
		super.testTransferUsingFilePath();
	}

	@Test
	public void testTransferUsingRelativeFilePath() throws Exception {
		/*
		 * source_dir is empty: default is the directory of the user on the server (e.g. ftp pwd).
		 */
		gstrFilePath = "./tmp/myfile_20120801.csv";
		objTestOptions.SourceDir.Value("");
		objOptions.profile.Value("testTransferUsingRelativeFilePath");
		super.testTransferUsingFilePath();
	}

	@Test
	public void testTransferUsingRelativeFilePath2() throws Exception {
		objTestOptions.SourceDir.Value("");
		gstrFilePath = "tmp/myfile_20120801.csv";
		super.testTransferUsingFilePath();
	}

	@Override
	@Test
	public void testCopyMultipleFiles() throws Exception {
		super.testCopyMultipleFiles();
	}

	@Override
	@Test
	public void testDeleteFiles2() throws Exception {
		super.testDeleteFiles2();
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

	@Override
	@Test
	public void testCopyWithFileList() throws Exception {
		super.testCopyWithFileList();
	}

	@Override
	@Test
	public void testBigCopy() throws Exception {
		// not relevant; local 2 local
	}

	@Test
	public void testBigCopy2() throws Exception {
		// not relevant; local 2 local
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
		super.testSend();
	}
	
	@Test
	public void testSendWithHistoryAndBackgroundServiceAndEmptyHostUser() throws Exception {
		objTestOptions.Target().user.Value("");
		objTestOptions.Target().host.Value("");
		
		JSCsvFile csvFile = new JSCsvFile("R:/nobackup/junittests/testdata/JADE/history_files/historyWithEmptyHost.csv");
		if(csvFile.exists()) {
			csvFile.delete();
		}
		objOptions.HistoryFileName.Value(csvFile.getAbsolutePath());
		setOptions4BackgroundService();
		super.testSend();
		
		String[] strValues = null;
		csvFile.loadHeaders();
		String[] strHeader = csvFile.Headers();
		String remote_host = "";
		String remote_host_ip = "";
		String remote_user = "";
		strValues = csvFile.readCSVLine();
		for (int j = 0; j < strValues.length; j++) {
//			System.out.println(strHeader[j]+"="+strValues[j]);
			if (strHeader[j].equals("remote_host")) {
				remote_host = strValues[j];
			}
			if (strHeader[j].equals("remote_host_ip")) {
				remote_host_ip = strValues[j];
			}
			if (strHeader[j].equals("remote_user")) {
				remote_user = strValues[j];
			}
		}
		csvFile.close();
		assertTrue("remote_host is empty", remote_host.length() > 0);
		assertTrue("remote_user is empty", remote_user.length() > 0);
		assertTrue("remote_host_ip is empty", remote_host_ip.matches("\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"));
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

	@Override
	@Test
	public void testCopyWithFolderInSourceDir() throws Exception {
		super.testCopyWithFolderInSourceDir();
	}

	@Test
	public void testCopyRecursiveWithFolderInSourceDir() throws Exception {
		objTestOptions.recursive.value(true);
		super.testCopyWithFolderInSourceDir();
		objTestOptions.recursive.setFalse();
	}

	@Override
	@Test
	public void testDeleteFiles() throws Exception {
		objTestOptions.FileNameRegExp.Value("^Masstest.\\.txt$");
		super.testDeleteFiles();
	}

	@Test
	public void testDeleteFilesWithForce() throws Exception {
		objTestOptions.ErrorOnNoDataFound.push();
		objTestOptions.ErrorOnNoDataFound.setFalse();
		objTestOptions.FileNameRegExp.Value("^Masstest.\\.txt$");
		super.testDeleteFiles();
		objTestOptions.ErrorOnNoDataFound.pop();
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
	public void testSFTPReceive() throws Exception {
		JADEOptions	objTestOptions			= new JADEOptions();
		objTestOptions.operation.Value(enuJadeOperations.receive);
		objTestOptions.host.Value("homer.sos");
		objTestOptions.user.Value("test");
		objTestOptions.password.Value("12345");
		objTestOptions.port.value(SOSOptionPortNumber.conPort4SFTP);
		objTestOptions.protocol.Value(enuSourceTransferType);
		objTestOptions.auth_method.Value(enuAuthenticationMethods.password);
		objTestOptions.remote_dir.Value("/tmp/test/jade/out");
		objTestOptions.local_dir.Value("\\\\8of9\\c\\tmp\\sftpreceive");
		objTestOptions.file_spec.Value(".*");
		objTestOptions.recursive.value(true);
		objTestOptions.verbose.value(9);
		JadeEngine objJadeEngine = new JadeEngine(objTestOptions);
		objJadeEngine.Execute();
		objJadeEngine.Logout();
	}


}
