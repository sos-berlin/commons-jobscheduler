/**
 *
 */
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
public class JadeTestsFtp extends JadeTestBase {

	/**
	 *
	 */
	public JadeTestsFtp() {
		// TODO Auto-generated constructor stub
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
		enuTargetTransferType = enuTransferTypes.sftp;

		objTestOptions.SourceDir.Value(strTestPathName);
		objTestOptions.TargetDir.Value("/home/test/jadetest" + "/SOSDEX/");

		objTestOptions.Source().protocol.Value(enuSourceTransferType);
		objTestOptions.Target().protocol.Value(enuTargetTransferType);

		objTestOptions.Target().host.Value(conHostNameWILMA_SOS);
		objTestOptions.Target().port.value(SOSOptionPortNumber.conPort4SFTP);
		objTestOptions.Target().user.Value("test");
		objTestOptions.Target().password.Value("12345");

		objTestOptions.Source().user.Value("test");
		objTestOptions.Source().password.Value("12345");

		objTestOptions.user.Value("test");
		objTestOptions.password.Value("12345");

		objTestOptions.Target().auth_method.Value(enuAuthenticationMethods.password);
		objTestOptions.Source().loadClassName.Value("com.sos.VirtualFileSystem.SFTP.SOSVfsSFtp");
		objTestOptions.Target().loadClassName.Value("com.sos.VirtualFileSystem.SFTP.SOSVfsSFtp");

	}

	@Override
	@Test
	public void testReceiveWithSymlinkInRemoteDir() throws Exception {
		super.testReceiveWithSymlinkInRemoteDir();
	}

	@Override
	@Test
	public void testSend2file_spec() throws Exception {
		super.testSend2file_spec();
	}

	@Override
	@Test
	public void testDeleteFiles2() throws Exception {
		super.testDeleteFiles2();
	}

	@Override
	@Test
	public void testCopyMultipleFiles() throws Exception {
		super.testCopyMultipleFiles();
	}

	@Override
	@Test
	public void testCopyMultipleFilesThreaded() throws Exception {
		CreateTestFiles(150);
		super.testCopyMultipleFilesThreaded();
	}

	@Override
//	@Test
	public void testBigCopyThreaded() throws Exception {
		this.testBigCopy();
	}

	@Override
	@Test
	public void testCopyWithFileList() throws Exception {
		super.testCopyWithFileList();
	}

	@Override
//	@Test
	public void testBigCopy() throws Exception {
		objTestOptions.SourceDir.Value("R:/backup/sos/java/doxygen-docs/");
		//		objTestOptions.TargetDir.Value(conTargetOfDOXYGEN_DOCS);
		super.testBigCopy();
	}

	@Test
	public void testBigCopy2() throws Exception {
		objTestOptions.SourceDir.Value("R:/backup/sos/java/doxygen-docs/com.sos.VirtualFileSystem/");
		//		objTestOptions.TargetDir.Value(conTargetOfDOXYGEN_DOCS + "com.sos.VirtualFileSystem/");
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
	public void testSendWithJCraft() throws Exception {
		final String conMethodName = conClassName + "::testSendWithJCraft";
		objTestOptions.Target().setloadClassName("com.sos.VirtualFileSystem.SFTP.SOSVfsSFtpJCraft");
		super.testSend();
	}
	

	@Override
	@Test
	public void testSendFileSpec() throws Exception {
		final String conMethodName = conClassName + "::testSend";
		super.testSendFileSpec();
	}

	@Override
	@Test
	public void testCopyAndCreateVariableFolder() throws Exception {
		super.testCopyAndCreateVariableFolder();
	}
	
	@Test
	public void Copy_Local2SFTP_recursive() throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::Copy_Local2SFTP_recursive";
		objOptions.settings.Value(strSettingsFile);
		objOptions.profile.Value("Copy_Local2SFTP_recursive");
		super.testUseProfileWOCreatingTestFiles();
	}
	
	@Test
	public void SOSFTP_186_withTransactional() throws Exception {
		objOptions.transactional.value(true);
		super.testSendCommandAfterReplacing();
	}
	
	@Test
	public void SOSFTP_186_withAtomicSuffix() throws Exception {
		objOptions.atomic_suffix.Value("~");
		super.testSendCommandAfterReplacing();
	}
	
	@Test
	public void SOSFTP_186_withOutAtomicSuffix() throws Exception {
		super.testSendCommandAfterReplacing();
	}
	
	
//	@Test
//	public void CopyWithCreateSecurityHashFile() throws Exception {
//		objTestOptions.Target().setloadClassName("com.sos.VirtualFileSystem.SFTP.SOSVfsSFtpJCraft");
//		objTestOptions.CreateSecurityHashFile.value(true);
//		super.testSend();
//	}
	
}
