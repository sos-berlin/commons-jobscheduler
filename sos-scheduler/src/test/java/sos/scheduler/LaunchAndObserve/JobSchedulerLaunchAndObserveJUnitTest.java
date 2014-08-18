

package sos.scheduler.LaunchAndObserve;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Listener.JSListenerClass;
import org.apache.log4j.Logger;
import org.junit.*;
import sos.net.mail.options.SOSSmtpMailOptions;

import java.util.HashMap;

/**
 * \class 		JobSchedulerLaunchAndObserveJUnitTest - JUnit-Test for "Launch and observe any given job or job chain"
 *
 * \brief MainClass to launch JobSchedulerLaunchAndObserve as an executable command-line program
 *

 *
 * see \see J:\E\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc\JobSchedulerLaunchAndObserve.xml for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by C:\Users\KB\eclipse\xsl\JSJobDoc2JSJUnitClass.xsl from http://www.sos-berlin.com at 20111124185032 
 * \endverbatim
 */
public class JobSchedulerLaunchAndObserveJUnitTest extends JSToolBox {
	@SuppressWarnings("unused")	 //$NON-NLS-1$
	private final static String					conClassName						= "JobSchedulerLaunchAndObserveJUnitTest"; //$NON-NLS-1$
	@SuppressWarnings("unused")	 //$NON-NLS-1$
	private static Logger		logger			= Logger.getLogger(JobSchedulerLaunchAndObserveJUnitTest.class);

	@SuppressWarnings("unused")
	private final String				conSVNVersion		= "$Id: JobSchedulerJobAdapter.java 15749 2011-11-22 16:04:10Z kb $";

	protected JobSchedulerLaunchAndObserveOptions	objO			= null;
	private JobSchedulerLaunchAndObserve objE = null;
	
	
	public JobSchedulerLaunchAndObserveJUnitTest() {
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
		objE = new JobSchedulerLaunchAndObserve();
		objE.registerMessageListener(this);
		objO = objE.Options();
		objO.registerMessageListener(this);
		
		JSListenerClass.bolLogDebugInformation = true;
		JSListenerClass.intMaxDebugLevel = 9;
		
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testSplit () {
		
		String strA = "a,b,c,de,f";
		String strAR[] = strA.split(";|,");
		System.out.println(strAR[0]);
	}
	
	@Test
	public void testExecute() throws Exception {
		JobSchedulerLaunchAndObserveOptions objO = objE.Options();
		
		HashMap <String, String> objH = new HashMap <String, String> ();
//		objH.put(objO.getjob_name().getKey(), "GenericShellExecutor");
//		objH.put(objO.getOrderId().getKey(), "Execute1");
//		objH.put(objO.getorder_jobchain_name().getKey(), "ShellExecutor");
		objH.put(objO.getjob_name().getKey(), "/LaunchAndObserve/GenericOrderShellExecutor");
		objH.put(objO.getOrderId().getKey(), "Execute1");
		objH.put(objO.getorder_jobchain_name().getKey(), "/LaunchAndObserve/ShellExecutor");

//		objH.put(objO.getscheduler_host().getKey(), "localhost");
//		objH.put(objO.getscheduler_port().getKey(), "4444");
		objH.put(objO.getscheduler_host().getKey(), "8of9.sos");
		objH.put(objO.getscheduler_port().getKey(), "4210");

		objH.put(objO.getcheck_for_regexp().getKey(), "Antwort von");
		objH.put(objO.getcheck_inactivity().getKey(), "true");
		objH.put(objO.getmail_on_nonactivity().getKey(), "true");
		objH.put(objO.getmail_on_restart().getKey(), "true");

		objH.put(objO.getcheck_interval().getKey(), "11");

		objH.put(objO.getmail_on_nonactivity().getKey(), "true");
		objH.put(objO.getmail_on_restart().getKey() , "true");

		SOSSmtpMailOptions objM = (SOSSmtpMailOptions) objO.getMailOnRestartOptions();
		objH.put(objM.getbody().getShortKey()+"MailOnRestart_", "bodobodododo");
		objH.put(objM.getsubject().getShortKey()+"MailOnRestart_", "mail from LaunchAndObserve");
		objH.put(objM.getto().getShortKey()+"MailOnRestart_", "scheduler_test@sos-berlin.com,oh@sos-berlin.com");
		objH.put(objM.getfrom().getShortKey(),"8of9@sos-berlin.com");

		objH.put(objM.gethost().getShortKey(), "smtp.sos");

		objH.put(objM.getbody().getShortKey(), "bodobodododo");
		objH.put(objM.getsubject().getShortKey(), "mail from LaunchAndObserve");
		objH.put(objM.getto().getShortKey(), "scheduler_test@sos-berlin.com,oh@sos-berlin.com");

		objO.setAllOptions(objH);
		objO.CheckMandatory();
		logger.debug(objO.toString());
		
		logger.debug(objM.toString());
		
		objM.setAllOptions(objH);
		logger.debug(objM.toString());
		
		SOSSmtpMailOptions objK = (SOSSmtpMailOptions) objO.getMailOnKillOptions();
		objK.setAllOptions(objH);
		logger.debug(objK.toString());
		objE.Execute();
		
//		assertEquals ("auth_file", objO.auth_file.Value(),"test"); //$NON-NLS-1$
//		assertEquals ("user", objO.user.Value(),"test"); //$NON-NLS-1$


	}
}  // class JobSchedulerLaunchAndObserveJUnitTest