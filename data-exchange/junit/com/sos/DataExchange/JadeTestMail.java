package com.sos.DataExchange;

import org.junit.Before;
import org.junit.Test;

import com.sos.JSHelper.Options.SOSOptionTransferType.enuTransferTypes;

/**
* @author KB
*
*/
public class JadeTestMail extends JadeTestBase {

	private final String				strSettingsFile			= "R:/backup/sos/java/development/SOSDataExchange/examples/jade_mail_settings.ini";
	
	/**
	 *
	 */
	public JadeTestMail() {
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

		objTestOptions.SourceDir.Value(strTestPathName);
		objTestOptions.TargetDir.Value(strTestPathName + "/SOSMDX/");

		objTestOptions.Source().protocol.Value(enuSourceTransferType);
		objTestOptions.Target().protocol.Value(enuTargetTransferType);
	}
	
	@Test
	public void testMailWithNotification() throws Exception {
		//use file_notification_* params
		final String conMethodName = conClassName + "::testMailWithNotification";
		objOptions.settings.Value(strSettingsFile);
		objOptions.profile.Value("copy_files_with_notification");
		super.testUseProfile();
	}
	
	@Test
	public void testMailOnSuccess() throws Exception {
		//use mail_on_success_* params
		final String conMethodName = conClassName + "::testMailOnSuccess";
		objOptions.settings.Value(strSettingsFile);
		objOptions.profile.Value("copy_files_on_success");
		super.testUseProfile();
	}
	
	@Test
	public void testMailOnError() throws Exception {
		//use mail_on_error_* params
		final String conMethodName = conClassName + "::testMailOnError";
		objOptions.settings.Value(strSettingsFile);
		objOptions.profile.Value("copy_files_on_error");
		super.testUseProfile();
	}
	
	@Test
	public void testMailOnErrorButNoErrorOccurs() throws Exception {
		//use mail_on_error_* params
		final String conMethodName = conClassName + "::testMailOnErrorButNoErrorOccurs";
		objOptions.settings.Value(strSettingsFile);
		objOptions.profile.Value("copy_files_without_error_and_mail_on_error");
		super.testUseProfile();
	}
}

