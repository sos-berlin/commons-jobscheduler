package com.sos.scheduler.messages;

import static com.sos.scheduler.messages.JSMessages.JSJ_D_0032;
import static com.sos.scheduler.messages.JSMessages.JSJ_I_110;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import sos.scheduler.CheckRunHistory.JobSchedulerCheckRunHistoryJUnitTest;

import com.sos.JSHelper.Logging.Log4JHelper;
import com.sos.localization.SOSMsg;

public class JSMsgTest {

	@SuppressWarnings("unused")
	private final static String						conClassName	= "JobSchedulerCheckRunHistoryJUnitTest";						//$NON-NLS-1$
	@SuppressWarnings("unused")
	private static Logger							logger			= Logger.getLogger(JobSchedulerCheckRunHistoryJUnitTest.class);
	@SuppressWarnings("unused")
	private static Log4JHelper						objLogger		= null;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		objLogger = new Log4JHelper("./log4j.properties"); //$NON-NLS-1$
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
