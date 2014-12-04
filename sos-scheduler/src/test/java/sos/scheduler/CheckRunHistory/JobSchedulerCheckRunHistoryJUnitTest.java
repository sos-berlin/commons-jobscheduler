package sos.scheduler.CheckRunHistory;
import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;

import org.apache.log4j.Logger;
import org.junit.*;

/**
 * \class 		JobSchedulerCheckRunHistoryJUnitTest - JUnit-Test for "Check the last job run"
 *
 * \brief MainClass to launch JobSchedulerCheckRunHistory as an executable command-line program
 *

 *
 * see \see J:\E\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc\JobSchedulerCheckRunHistory.xml for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by C:\Users\KB\eclipse\sos.scheduler.xsl\JSJobDoc2JSJUnitClass.xsl from http://www.sos-berlin.com at 20110224143615 
 * \endverbatim
 */
// sp 10.06.14 Test hängt im Jenkins build, lokal gibt es eine SocketTimeoutException! [SP]
@Ignore("Test set to Ignore for later examination")
public class JobSchedulerCheckRunHistoryJUnitTest extends JSToolBox {
	@SuppressWarnings("unused")
	private final static String						conClassName	= "JobSchedulerCheckRunHistoryJUnitTest";						//$NON-NLS-1$
	@SuppressWarnings("unused")
	private static Logger							logger			= Logger.getLogger(JobSchedulerCheckRunHistoryJUnitTest.class);
	protected JobSchedulerCheckRunHistoryOptions	objOptions		= null;
	private JobSchedulerCheckRunHistory				objE			= null;

	public JobSchedulerCheckRunHistoryJUnitTest() {
		//
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before 
	public void setUp() throws Exception {
		objE = new JobSchedulerCheckRunHistory();
		objE.registerMessageListener(this);
		objOptions = objE.Options();
		objOptions.registerMessageListener(this);
		JSListenerClass.bolLogDebugInformation = true;
		JSListenerClass.intMaxDebugLevel = 9;
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test (expected=com.sos.JSHelper.Exceptions.JobSchedulerException.class)
	public void testExecute() throws Exception {
		objOptions.message.Value("[JOB_NAME] is too late!");
		objOptions.start_time.Value("0:00:00:00");
		objOptions.JobName.Value("/schulung/exercise4");
		objOptions.SchedulerPort.value(4422);
		objOptions.SchedulerHostName.Value("homer.sos");
		objE.Execute();
		//		assertEquals ("auth_file", objO.auth_file.Value(),"test"); //$NON-NLS-1$
		//		assertEquals ("user", objO.user.Value(),"test"); //$NON-NLS-1$
	}
} // class JobSchedulerCheckRunHistoryJUnitTest