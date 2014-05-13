package sos.scheduler.LaunchAndObserve;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.apache.log4j.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import sos.scheduler.LaunchAndObserve.JobSchedulerLaunchAndObserve;
import sos.scheduler.LaunchAndObserve.JobSchedulerLaunchAndObserveOptions;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;
import com.sos.JSHelper.Logging.Log4JHelper;

/**
 * \class 		JobSchedulerLaunchAndObserveOptionsJUnitTest - Launch and observe any given job or job chain
 *
 * \brief 
 *
 *

 *
 * see \see J:\E\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc\JobSchedulerLaunchAndObserve.xml for (more) details.
 * 
 * \verbatim ;
 * mechanicaly created by C:\Users\KB\eclipse\xsl\JSJobDoc2JSJUnitOptionSuperClass.xsl from http://www.sos-berlin.com at 20111124185124 
 * \endverbatim
 *
 * \section TestData Eine Hilfe zum Erzeugen einer HashMap mit Testdaten
 *
 * Die folgenden Methode kann verwendet werden, um für einen Test eine HashMap
 * mit sinnvollen Werten für die einzelnen Optionen zu erzeugen.
 *
 * \verbatim
 private HashMap <String, String> SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM) {
	pobjHM.put ("		JobSchedulerLaunchAndObserveOptionsJUnitTest.auth_file", "test");  // This parameter specifies the path and name of a user's pr
		return pobjHM;
  }  //  private void SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM)
 * \endverbatim
 */
public class JobSchedulerLaunchAndObserveOptionsJUnitTest extends JSToolBox {
	private final String							conClassName	= "JobSchedulerLaunchAndObserveOptionsJUnitTest";						//$NON-NLS-1$
	@SuppressWarnings("unused")//$NON-NLS-1$
	private static Logger							logger			= Logger.getLogger(JobSchedulerLaunchAndObserveOptionsJUnitTest.class);
	@SuppressWarnings("unused")
	private static Log4JHelper						objLogger		= null;
	private JobSchedulerLaunchAndObserve			objE			= null;

	protected JobSchedulerLaunchAndObserveOptions	objOptions		= null;

	public JobSchedulerLaunchAndObserveOptionsJUnitTest() {
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
		objLogger = new Log4JHelper("./log4j.properties"); //$NON-NLS-1$
		objE = new JobSchedulerLaunchAndObserve();
		objE.registerMessageListener(this);
		objOptions = objE.Options();
		objOptions.registerMessageListener(this);

		JSListenerClass.bolLogDebugInformation = true;
		JSListenerClass.intMaxDebugLevel = 9;
	}

	@After
	public void tearDown() throws Exception {
	}

	/**
	 * \brief testcheck_for_regexp : Text pattern to search for in log file
	 * 
	 * \details
	 * 
	 *
	 */
	@Test
	public void testcheck_for_regexp() { // SOSOptionRegExp
		objOptions.check_for_regexp.Value("++true++");
		assertEquals("Text pattern to search for in log file", objOptions.check_for_regexp.Value(), "++true++");

	}

	/**
	 * \brief testcheck_log_file : Check Log File for Progress
	 * 
	 * \details
	 * 
	 *
	 */
	@Test
	public void testcheck_log_file() { // SOSOptionBoolean
		objOptions.check_inactivity.Value("true");
		assertTrue("Check Log File for Progress", objOptions.check_inactivity.value());
		objOptions.check_inactivity.Value("false");
		assertFalse("Check Log File for Progress", objOptions.check_inactivity.value());

	}

	/**
	 * \brief testcheck_interval : This parameter specifies the interval in seconds
	 * 
	 * \details
	 * This parameter specifies the interval in seconds between two file transfer trials, if repeated transfer of files has been configured using the check_retry parameter.
	 *
	 */
	@Test
	public void testcheck_interval() { // SOSOptionInteger
		objOptions.check_interval.Value("12345");
		assertEquals("This parameter specifies the interval in seconds", objOptions.check_interval.Value(), "12345");
		assertEquals("This parameter specifies the interval in seconds", objOptions.check_interval.value(), 12345);
		objOptions.check_interval.value(12345);
		assertEquals("This parameter specifies the interval in seconds", objOptions.check_interval.Value(), "12345");
		assertEquals("This parameter specifies the interval in seconds", objOptions.check_interval.value(), 12345);

	}

	/**
	 * \brief testjob_name : The name of a job.
	 * 
	 * \details
	 * The name of a job.
	 *
	 */
	@Test
	public void testjob_name() { // JSJobName
		objOptions.job_name.Value("++----++");
		assertEquals("The name of a job.", objOptions.job_name.Value(), "++----++");

	}

	/**
	 * \brief testkill_job : kill job due to Inactivity
	 * 
	 * \details
	 * 
	 *
	 */
	@Test
	public void testkill_job() { // SOSOptionBoolean
		objOptions.kill_job.Value("true");
		assertTrue("kill job due to Inactivity", objOptions.kill_job.value());
		objOptions.kill_job.Value("false");
		assertFalse("kill job due to Inactivity", objOptions.kill_job.value());
	}

	/**
	 * \brief testlifetime : Lifetime of the Job
	 * 
	 * \details
	 * 
	 *
	 */
	@Test
	public void testlifetime() { // SOSOptionTime
		objOptions.lifetime.Value("30");
		assertEquals("Lifetime of the Job", objOptions.lifetime.Value(), "30");
		assertEquals("Lifetime of the Job", objOptions.lifetime.getTimeAsSeconds(), 30);
		objOptions.lifetime.Value("1:30");
		assertEquals("Lifetime of the Job", objOptions.lifetime.Value(), "1:30");
		assertEquals("Lifetime of the Job", objOptions.lifetime.getTimeAsSeconds(), 90);
		objOptions.lifetime.Value("1:10:30");
		assertEquals("Lifetime of the Job", objOptions.lifetime.Value(), "1:10:30");
		assertEquals("Lifetime of the Job", objOptions.lifetime.getTimeAsSeconds(), 30 + 10 * 60 + 60 * 60);

	}

	/**
	 * \brief testmail_on_nonactivity : send eMail due to Inactivity
	 * 
	 * \details
	 * 
	 *
	 */
	@Test
	public void testmail_on_nonactivity() { // SOSOptionBoolean
		objOptions.mail_on_nonactivity.Value("true");
		assertTrue("send eMail due to Inactivity", objOptions.mail_on_nonactivity.value());
		objOptions.mail_on_nonactivity.Value("false");
		assertFalse("send eMail due to Inactivity", objOptions.mail_on_nonactivity.value());

	}

	/**
	 * \brief testmail_on_restart : send eMail with restart of job
	 * 
	 * \details
	 * 
	 *
	 */
	@Test
	public void testmail_on_restart() { // SOSOptionBoolean
		objOptions.mail_on_restart.Value("true");
		assertTrue("send eMail with restart of job", objOptions.mail_on_restart.value());
		objOptions.mail_on_restart.Value("false");
		assertFalse("send eMail with restart of job", objOptions.mail_on_restart.value());

	}

	/**
	 * \brief testorder_jobchain_name : The name of the jobchain which belongs to the order The name of the jobch
	 * 
	 * \details
	 * 
	 *
	 */
	@Test
	public void testorder_jobchain_name() { // SOSOptionString
		objOptions.order_jobchain_name.Value("++----++");
		assertEquals("The name of the jobchain which belongs to the order The name of the jobch", objOptions.order_jobchain_name.Value(), "++----++");

	}

	/**
	 * \brief testOrderId : The name or the identification of an order.
	 * 
	 * \details
	 * The name or the identification of an order.
	 *
	 */
	@Test
	public void testOrderId() { // JSOrderId
		objOptions.OrderId.Value("++----++");
		assertEquals("The name or the identification of an order.", objOptions.OrderId.Value(), "++----++");

	}

	/**
	 * \brief testrestart : Restart the observed Job This value o
	 * 
	 * \details
	 * This value of this parameter defines wether the job which has to be observed, should be restarted during the observation period, in case the job has ended.
	 *
	 */
	@Test
	public void testrestart() { // SOSOptionBoolean
		objOptions.restart.Value("true");
		assertTrue("Restart the observed Job This value o", objOptions.restart.value());
		objOptions.restart.Value("false");
		assertFalse("Restart the observed Job This value o", objOptions.restart.value());

	}

	/**
	 * \brief testscheduler_host : This parameter specifies the host name or IP addre
	 * 
	 * \details
	 * This parameter specifies the host name or IP address of a server for which Job Scheduler is operated for Managed File Transfer. The contents of an optional history file (see parameter history), is added to a central database by Job Scheduler. This parameter causes the transfer of the history entries for the current transfer by UDP to Job Scheduler. Should Job Scheduler not be accessible then no errors are reported, instead, the contents of the history will automaticall be processed later on.
	 *
	 */
	@Test
	public void testscheduler_host() { // SOSOptionHostName
		objOptions.scheduler_host.Value("++localhost++");
		assertEquals("This parameter specifies the host name or IP addre", objOptions.scheduler_host.Value(), "++localhost++");

	}

	/**
	 * \brief testscheduler_port : The TCP-port for which a JobScheduler, see parameter sche
	 * 
	 * \details
	 * The TCP-port for which a JobScheduler, see parameter scheduler_host.
	 *
	 */
	@Test
	public void testscheduler_port() { // SOSOptionPortNumber
		objOptions.scheduler_port.Value("4444");
		assertEquals("The TCP-port for which a JobScheduler, see parameter sche", objOptions.scheduler_port.Value(), "4444");

	}

	@Test
	public void testSetHashMap () throws Exception {
		
		objOptions.setAllOptions(SetJobSchedulerOptions());
		assertEquals(objOptions.check_for_regexp.Description(), objOptions.check_for_regexp.Value(), "test");
		assertEquals(objOptions.scheduler_host.Description(), objOptions.scheduler_host.Value(), "test");
		assertEquals(objOptions.scheduler_port.Description(), objOptions.scheduler_port.Value(), "1234");
		
		assertEquals("MailOnRestart_to", objOptions.getMailOnRestartOptions().getto().Value(), "MailOnRestart_to");
		assertEquals("MailOnKill_to", objOptions.getMailOnKillOptions().getto().Value(), "MailOnKill_to");
		
	}
	
	private HashMap<String, String> SetJobSchedulerOptions() {

		HashMap<String, String> pobjHM = new HashMap<String, String>();
		JobSchedulerLaunchAndObserveOptions objO= objOptions;
		pobjHM.put(objOptions.check_for_regexp.getKey(), "test"); 
		pobjHM.put(objOptions.scheduler_host.getKey(), "test"); 
		pobjHM.put(objOptions.scheduler_port.getKey(), "1234"); 
		
		pobjHM.put("MailOnRestart_to", "MailOnRestart_to");
		pobjHM.put("MailOnKill_to", "MailOnKill_to");
																							// user's pr
		return pobjHM;
	} // private void SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM)

} // public class JobSchedulerLaunchAndObserveOptionsJUnitTest