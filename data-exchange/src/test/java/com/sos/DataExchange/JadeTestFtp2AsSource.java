/**
 *
 */
package com.sos.DataExchange;

import org.junit.Before;
import org.junit.Test;

/**
 * @author KB
 *
 */
public class JadeTestFtp2AsSource extends JadeTestFtpAsSource {

	/**
	 *
	 */
	public JadeTestFtp2AsSource() {
	}

	@Override
	@Before
	public void setUp() throws Exception {
		super.setUp();

		objTestOptions.Source().loadClassName.Value("com.sos.VirtualFileSystem.FTP.SOSVfsFtp2");
	}

	@Override
	@Test
	public void testTransferUsingAbsolutFilePath () throws Exception {
		super.testTransferUsingAbsolutFilePath();
		super.testTransferUsingAbsolutFilePath();
	}

	@Override
	@Test
	public void testSendWithPolling0Files () throws Exception {
		objTestOptions.PollingServer.value(true);
		objTestOptions.VerbosityLevel.value(2);
		objTestOptions.poll_minfiles.value(1);
		objTestOptions.pollingServerDuration.Value("04:30");
		objTestOptions.force_files.setFalse();
		super.testSendWithPolling0Files();
	}

	@Override @Test
	public void jadeHomer2Local() throws Exception {
		super.jadeHomer2Local();
	}
	
	@Override
	@Test
	public void testUseProfileWithAsciiMode() throws Exception {
		final String conMethodName = conClassName + "::testUseProfileWithAsciiMode";
		objOptions.settings.Value(strSettingsFile);
		objOptions.profile.Value("copyWithAsciiMode");
		super.testUseProfileWithoutCreatingTestFiles();
	}
}
