package com.sos.DataExchange;


import org.junit.Before;
import org.junit.Test;

import com.sos.JSHelper.Options.SOSOptionAuthenticationMethod.enuAuthenticationMethods;
import com.sos.JSHelper.Options.SOSOptionPortNumber;
import com.sos.JSHelper.Options.SOSOptionTransferType.enuTransferTypes;


public class JadeTestWebDavAsSource extends JadeTestBase {

	protected final String			WEB_URI					= "https://mediacenter.gmx.net";
	protected final String			WEB_USER				= "sos.apl@gmx.de";
	protected final String			WEB_PASS				= "sosapl10629";
	protected final String			REMOTE_BASE_PATH		= "/home/kb/";

	public JadeTestWebDavAsSource() {
		enuSourceTransferType = enuTransferTypes.webdav;
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
		super.setUp();

		objTestOptions.TargetDir.Value(strTestPathName);

		objTestOptions.Target().protocol.Value(enuTargetTransferType);
		objTestOptions.Target().user.Value("test");
		objTestOptions.Target().password.Value("12345");
		objTestOptions.Target().host.Value("local");
		objTestOptions.Target().protocol.Value(enuTargetTransferType);

		objTestOptions.Source().protocol.Value(enuSourceTransferType);
		
		objTestOptions.SourceDir.Value(REMOTE_BASE_PATH);
		objTestOptions.Source().host.Value(WEB_URI);
		objTestOptions.Source().port.value(SOSOptionPortNumber.conPort4http);
		objTestOptions.Source().user.Value(WEB_USER);
		objTestOptions.Source().password.Value(WEB_PASS);
		objTestOptions.Source().auth_method.Value(enuAuthenticationMethods.url);
	}
	
	public void homerAsSource() throws Exception {
		objTestOptions.Source().host.Value("http://homer.sos/jade/");
		objTestOptions.Source().user.Value("test");
		objTestOptions.Source().password.Value("12345");
	}
	
	public void sourceBehindProxy() throws Exception {
		objTestOptions.Source().proxy_host.Value("proxy.sos");
		objTestOptions.Source().proxy_port.value(3128);
	}

	@Override
	@Test
	public void testSend2file_spec() throws Exception {
		super.testSend2file_spec();
	}
	
	@Test
	public void testSendfile_specSwissCom() throws Exception {
		objTestOptions.SourceDir.Value("Test");
		objTestOptions.Source().host.Value("https://filestation.creditmaster.ch");
		objTestOptions.Source().port.value(6006);
		objTestOptions.Source().user.Value("SwissTest");
		objTestOptions.Source().password.Value("PE8UKgKKFxDGOnbp9CD");
		objTestOptions.Source().auth_method.Value(enuAuthenticationMethods.url);
		sourceBehindProxy();
		objTestOptions.file_spec.Value("\\.txt$");
		objTestOptions.TargetDir.Value(strTestPathName+"SwissCom");
		super.testSendFileSpec2();
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
		final String conMethodName = conClassName + "::testSend";
		super.testSend();
	}
	
	@Override
	@Test
	public void testSend2() throws Exception {
		final String conMethodName = conClassName + "::testSend2";
		// /jade liegt in /tmp/test/jade
		objTestOptions.SourceDir.Value("/jade/out");
		homerAsSource();
		super.testSend();
	}

	@Override
	@Test
	public void testSendFileSpec() throws Exception {
		//in /jade/out sind Unterverzeichnisse
		final String conMethodName = conClassName + "::testSendFileSpec";
		objTestOptions.SourceDir.Value("/jade/out");
		homerAsSource();
		objTestOptions.recursive.value(false);
		objTestOptions.file_spec.Value(".*");
		super.testSendFileSpec2();
	}
	
	@Test
	public void testSendFileSpecWithProxy() throws Exception {
		//in /jade/out sind Unterverzeichnisse
		final String conMethodName = conClassName + "::testSendFileSpecWithProxy";
		objTestOptions.SourceDir.Value("/jade/out");
		homerAsSource();
		sourceBehindProxy();
		objTestOptions.recursive.value(false);
		objTestOptions.file_spec.Value(".*");
		super.testSendFileSpec2();
	}
	
	@Override
	@Test
	public void testSendFileSpec2() throws Exception {
		//in /jade/massive sind keine Unterverzeichnisse
		final String conMethodName = conClassName + "::testSendFileSpec";
		objTestOptions.SourceDir.Value("/jade/massive");
		homerAsSource();
		objTestOptions.recursive.value(false);
		objTestOptions.file_spec.Value(".*");
		super.testSendFileSpec2();
	}

	@Test
	public void testSendRecursive() throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::testSendRecursive";
		objTestOptions.SourceDir.Value("/jade/out");
		homerAsSource();
		objTestOptions.recursive.value(true);
		objTestOptions.file_spec.Value("\\.txt$");
		super.testSendFileSpec2();
	}
	
	@Test
	public void testSendRecursiveWithProxy() throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::testSendRecursiveWithProxy";
		objTestOptions.SourceDir.Value("/jade/out");
		homerAsSource();
		sourceBehindProxy();
		objTestOptions.recursive.value(true);
		objTestOptions.file_spec.Value("\\.txt$");
		super.testSendFileSpec2();
	}

	@Override
	@Test
	public void testCopyAndRenameSourceAndTarget() throws Exception {
		objTestOptions.SourceDir.Value("/jade/out");
		homerAsSource();
		super.testCopyAndRenameSourceAndTarget();
	}
}
