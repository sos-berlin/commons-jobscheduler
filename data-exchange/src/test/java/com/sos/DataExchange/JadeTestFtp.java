package com.sos.DataExchange;


import org.junit.Before;
import org.junit.Test;

import com.sos.JSHelper.Options.SOSOptionAuthenticationMethod.enuAuthenticationMethods;
import com.sos.JSHelper.Options.SOSOptionPortNumber;
import com.sos.JSHelper.Options.SOSOptionTransferType.enuTransferTypes;

/**
 * @author KB
 *
 */
public class JadeTestFtp extends JadeTestBase {

	/**
	 *
	 */
	public JadeTestFtp() {
		enuSourceTransferType = enuTransferTypes.local; 
		enuTargetTransferType = enuTransferTypes.ftp;
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
		enuSourceTransferType = enuTransferTypes.local;
		enuTargetTransferType = enuTransferTypes.ftp;
		super.setUp();

//		objTestOptions.Target().loadClassName.Value("com.sos.VirtualFileSystem.FTP.SOSVfsFtp2");
		
		objTestOptions.SourceDir.Value(strTestPathName);
		objTestOptions.TargetDir.Value("/home/test/jadetest" + "/SOSDEX/");

		objTestOptions.Source().protocol.Value(enuSourceTransferType);
		objTestOptions.Target().protocol.Value(enuTargetTransferType);

		objTestOptions.Target().host.Value(conHostNameWILMA_SOS);
		objTestOptions.Target().port.value(SOSOptionPortNumber.conPort4FTP);
		objTestOptions.Target().user.Value("test");
		objTestOptions.Target().password.Value("12345");
//		objTestOptions.Target().user.Value("kb");
//		objTestOptions.Target().password.Value("kb");

		objTestOptions.Source().user.Value("test");
		objTestOptions.Source().password.Value("12345");

		objTestOptions.Target().auth_method.Value(enuAuthenticationMethods.password);
	}

	@Override
	@Test
	public void testCopyAndRenameSource() throws Exception {
		super.testCopyAndRenameSource();
	}

	@Override
	@Test
	public void testSend2file_spec() throws Exception {
		super.testSend2file_spec();
	}

	@Override
	@Test
	public void testCopyMultipleFiles() throws Exception {
		objTestOptions.Target().host.Value("homer.sos");
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
	public void testBigCopyThreaded() throws Exception {
		this.testBigCopy();
	}

//	@Test
//	public void testBigCopy2() throws Exception {
//		objTestOptions.SourceDir.Value(conSourceOfDOXYGEN_DOCS + "SOSVirtualFileSystem/");
//		objTestOptions.TargetDir.Value(conTargetOfDOXYGEN_DOCS+  "SOSVirtualFileSystem/");
//		super.testBigCopy();
//	}
//
	
	@Test
	public void testBigCopy2() throws Exception {
		objTestOptions.SourceDir.Value("R:/backup/sos/java/doxygen-docs/com.sos.VirtualFileSystem/");
		objTestOptions.TargetDir.Value("/home/test/doc/doxygen-docs/com.sos.VirtualFileSystem/");
		super.testBigCopy();
	}
	
	@Override
	@Test
	public void testCopyWithFileList() throws Exception {
		super.testCopyWithFileList();
	}

	@Override
	@Test
	public void testCopyMultipleResultList() throws Exception {
		super.testCopyMultipleResultList();
	}

	@Override
	@Test
	public void testGetFileList() throws Exception {
		super.testGetFileList();
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
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::testSend";
		super.testSend();
	}

	@Override
	@Test
	public void testSendFileSpec() throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::testSend";
		super.testSendFileSpec();
	}

	@Override
	@Test
	public void testCopyAndCreateVariableFolder() throws Exception {
		super.testCopyAndCreateVariableFolder();
	}

	@Override
	@Test
	public void testKeePass1() throws Exception {
		super.testKeePass1();
	}

	@Override
	@Test
	public void testSendWithPollingAndSteadyState() throws Exception {
		super.testSendWithPollingAndSteadyState();
	}
	
	@Test
	public void testSendWithPollingAndSteadyStateError() throws Exception {
		objOptions.Steady_state_error_state.Value("nextState");
		super.sendWithPollingAndSteadyStateError();
	}

	@Test
	public void Copy_Local2AlternativeFTP_withHistorie() throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::Copy_Local2AlternativeFTP_withHistorie";
		objOptions.settings.Value(strSettingsFile);
		objOptions.profile.Value("Copy_Local2AlternativeFTP_withHistorie");
		super.testUseProfileWOCreatingTestFiles();
	}

	@Test
	public void Copy_Local2FTP_recursive() throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::Copy_Local2FTP_recursive";
		objOptions.settings.Value(strSettingsFile);
		objOptions.profile.Value("Copy_Local2FTP_recursive");
		super.testUseProfileWOCreatingTestFiles();
	}

	@Test
	public void testPCL_FTP_REC() throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::testPCL_FTP_REC";
		objOptions.settings.Value(strSettingsFile);
		objOptions.profile.Value("PCL_FTP_REC");
		super.testUseProfileWOCreatingTestFiles();
	}

	@Test
	public void testsosftp_158() throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::testsosftp_158";
		objOptions.settings.Value(strSettingsFile);
		objOptions.profile.Value("sosftp_158");
		super.testUseProfileWOCreatingTestFiles();
	}

	@Test
	public void testurl_example_1() throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::url_example_1";
		objOptions.settings.Value(strSettingsFile);
		objOptions.profile.Value("url_example_1");
		super.testUseProfileWOCreatingTestFiles();
	}

}
