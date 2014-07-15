package com.sos.scheduler.messages;

import com.sos.localization.SOSMsg;
import org.apache.log4j.Logger;
import org.junit.*;
import sos.scheduler.CheckRunHistory.JobSchedulerCheckRunHistoryJUnitTest;

import static com.sos.scheduler.messages.JSMessages.JSJ_D_0032;
import static com.sos.scheduler.messages.JSMessages.JSJ_I_110;

public class JSMsgTest {

	@SuppressWarnings("unused")
	private final static String						conClassName	= "JobSchedulerCheckRunHistoryJUnitTest";						//$NON-NLS-1$
	@SuppressWarnings("unused")
	private static Logger							logger			= Logger.getLogger(JobSchedulerCheckRunHistoryJUnitTest.class);

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		SOSMsg.flgShowFullMessageText = true;
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test (expected=com.sos.JSHelper.Exceptions.JobSchedulerException.class)
	public void testJSMsgString() {
		JSMsg objMsg = new JSMsg("JSJ_F_107");
		objMsg.toLog(conClassName);
	}

	@Test
	public void testJSMsg_I_110() {
		JSJ_I_110.toLog(conClassName);
	}
	@Test
	public void testJSMsg_D_032() {
		JSMsg.VerbosityLevel = 5;
		JSJ_D_0032.toLog(conClassName);
		JSMsg.VerbosityLevel = 1;
		JSJ_D_0032.toLog(conClassName);
	}
	@Test
	public void testJSMsgStringInt() {
	}

}
