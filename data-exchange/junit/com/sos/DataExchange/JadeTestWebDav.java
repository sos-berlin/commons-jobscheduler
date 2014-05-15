package com.sos.DataExchange;

import org.junit.Before;
import org.junit.Test;

import com.sos.JSHelper.Options.SOSOptionAuthenticationMethod.enuAuthenticationMethods;
import com.sos.JSHelper.Options.SOSOptionPortNumber;
import com.sos.JSHelper.Options.SOSOptionTransferType.enuTransferTypes;


public class JadeTestWebDav extends JadeTestBase {

	protected final String			WEB_URI					= "https://mediacenter.gmx.net";
	protected final String			WEB_USER				= "sos.apl@gmx.de";
	protected final String			WEB_PASS				= "sosapl10629";
//	protected final String			REMOTE_BASE_PATH		= "/home/kb/";
	protected final String			REMOTE_BASE_PATH		= "/home/test/";

	public JadeTestWebDav() {
		enuSourceTransferType = enuTransferTypes.local;
		enuTargetTransferType = enuTransferTypes.webdav;
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

		objTestOptions.SourceDir.Value(strTestPathName);

		objTestOptions.Source().protocol.Value(enuSourceTransferType);
		objTestOptions.Source().user.Value("test");
		objTestOptions.Source().password.Value("12345");
		objTestOptions.Source().host.Value("local");

		objTestOptions.Target().protocol.Value(enuTargetTransferType);

		objTestOptions.TargetDir.Value(REMOTE_BASE_PATH);
		objTestOptions.Target().host.Value(WEB_URI);
		objTestOptions.Target().port.value(SOSOptionPortNumber.conPortWebDav);
		objTestOptions.Target().user.Value(WEB_USER);
		objTestOptions.Target().password.Value(WEB_PASS);
		objTestOptions.Target().auth_method.Value(enuAuthenticationMethods.url);
	}

	@Override
	@Test
	public void testSend2file_spec() throws Exception {
		super.testSend2file_spec();
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

	@Override
	@Test
	public void testCopyWithFileList() throws Exception {
		super.testCopyWithFileList();
	}

	@Override
	@Test
	public void testBigCopy() throws Exception {
		objTestOptions.SourceDir.Value("R:/backup/sos/java/doxygen-docs/");
		objTestOptions.TargetDir.Value(conTargetOfDOXYGEN_DOCS);
		super.testBigCopy();
	}

	@Test
	public void testBigCopy2() throws Exception {
		objTestOptions.SourceDir.Value("R:/backup/sos/java/doxygen-docs/com.sos.VirtualFileSystem/");
		objTestOptions.TargetDir.Value(conTargetOfDOXYGEN_DOCS + "com.sos.VirtualFileSystem/");
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
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::testSend";
		super.testSend();
	}
	
	@Test
	public void testSendViaProxy() throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::testSend";
		objTestOptions.Target().proxy_host.Value("proxy.sos");
		objTestOptions.Target().proxy_port.Value("3128");
		super.testSend();
	}

	@Override
	@Test
	public void testSend2() throws Exception {
		final String conMethodName = conClassName + "::testSend2";

		objTestOptions.TargetDir.Value("/webdav/kb/");
		objTestOptions.Target().host.Value("http://homer.sos/webdav");
		objTestOptions.Target().port.value(8080);
		objTestOptions.Target().user.Value("test");
		objTestOptions.Target().password.Value("12345");
		super.testSend();
	}
	
	@Test (expected=com.sos.JSHelper.Exceptions.JobSchedulerException.class)
	public void testSend3() throws Exception {
		final String conMethodName = conClassName + "::testSend3";
		//targetDir doesn't have sufficient permisions to write files by apache (webdav) user
		objTestOptions.TargetDir.Value("/jade/403");
		objTestOptions.Target().host.Value("http://homer.sos/jade/");
		objTestOptions.Target().port.value(8080);
		objTestOptions.Target().user.Value("test");
		objTestOptions.Target().password.Value("12345");
		super.testSend();
	}
	
	@Test
	public void testSendViaMonkAsProxy() throws Exception {
		final String conMethodName = conClassName + "::testSendViaMonkAsProxy";

		objTestOptions.TargetDir.Value("/webdav2/kb/");
		objTestOptions.Target().host.Value("http://homer.sos/webdav2/");
		objTestOptions.Target().port.value(8080);
		objTestOptions.Target().user.Value("test");
		objTestOptions.Target().password.Value("12345");
		objTestOptions.Target().proxy_host.Value("proxy.sos");
		objTestOptions.Target().proxy_port.Value("3128");
		super.testSend();
	}
	
	@Test (expected=com.sos.JSHelper.Exceptions.JobSchedulerException.class)
	public void testSendViaUnknownProxy() throws Exception {
		final String conMethodName = conClassName + "::testSendViaUnknownProxy";

		objTestOptions.TargetDir.Value("/webdav2/kb/");
		objTestOptions.Target().host.Value("http://homer.sos/webdav2");
		objTestOptions.Target().port.value(8080);
		objTestOptions.Target().user.Value("test");
		objTestOptions.Target().password.Value("12345");
		objTestOptions.Target().proxy_host.Value("proxi.sos");
		objTestOptions.Target().proxy_port.Value("3128");
		super.testSend();
	}

	@Override
	@Test
	public void testSendFileSpec() throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::testSendFileSpec";
		objTestOptions.recursive.value(false);
		objTestOptions.file_spec.Value("^test.*\\.txt$");
		super.testSendFileSpec2();
	}
	
	@Test
	public void testSendRecursive() throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::testSendRecursive";
		objTestOptions.TargetDir.Value("/webdav/kb");
		objTestOptions.Target().host.Value("http://homer.sos/webdav");
		objTestOptions.Target().port.value(8080);
		objTestOptions.Target().user.Value("test");
		objTestOptions.Target().password.Value("12345");
		objTestOptions.file_spec.Value("^test.txt$");
		objTestOptions.recursive.value(true);
		super.testSendFileSpec2();
	}
	
	@Test
	public void testSendRecursive2() throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::testSendRecursive";
		objTestOptions.SourceDir.Value("R:/nobackup/junittests/testdata/JADE/recursive");
		objTestOptions.TargetDir.Value("/webdav/kb");
		objTestOptions.Target().host.Value("http://homer.sos/webdav");
		objTestOptions.Target().port.value(8080);
		objTestOptions.Target().user.Value("test");
		objTestOptions.Target().password.Value("12345");
		objTestOptions.file_spec.Value("\\.(txt|dot)$");
		objTestOptions.recursive.value(true);
		super.testSendFileSpec2();
	}

	@Override
	@Test
	public void testCopyAndRenameSourceAndTarget() throws Exception {
		objTestOptions.TargetDir.Value("/webdav/kb");
		objTestOptions.Target().host.Value("http://homer.sos/webdav/");
		objTestOptions.Target().port.value(8080);
		objTestOptions.Target().user.Value("test");
		objTestOptions.Target().password.Value("12345");
		super.testCopyAndRenameSourceAndTarget();
	}
	
	@Test
	public void testCopyAndRenameSourceAndTarget2() throws Exception {
		super.testCopyAndRenameSourceAndTarget();
	}
}
