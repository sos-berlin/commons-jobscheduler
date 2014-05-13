package sos.scheduler.LaunchAndObserve;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Logging.Log4JHelper;

/**
 * \class 		JobSchedulerLaunchAndObserveMain - Main-Class for "Launch and observe any given job or job chain"
 *
 * \brief MainClass to launch JobSchedulerLaunchAndObserve as an executable command-line program
 *
 * This Class JobSchedulerLaunchAndObserveMain is the worker-class.
 *

 *
 * see \see J:\E\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc\JobSchedulerLaunchAndObserve.xml for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by C:\Users\KB\eclipse\xsl\JSJobDoc2JSMainClass.xsl from http://www.sos-berlin.com at 20111124184942 
 * \endverbatim
 */
public class JobSchedulerLaunchAndObserveMain extends JSToolBox {
	private final static String						conClassName	= "JobSchedulerLaunchAndObserveMain";									//$NON-NLS-1$
	private static Logger							logger			= Logger.getLogger(JobSchedulerLaunchAndObserveMain.class);
	@SuppressWarnings("unused")
	private static Log4JHelper						objLogger		= null;

	@SuppressWarnings("unused")
	private final String							conSVNVersion	= "$Id: JobSchedulerJobAdapter.java 15749 2011-11-22 16:04:10Z kb $";

	protected JobSchedulerLaunchAndObserveOptions	objOptions		= null;

	/**
	 * 
	 * \brief main
	 * 
	 * \details
	 *
	 * \return void
	 *
	 * @param pstrArgs
	 * @throws Exception
	 */
	public final static void main(String[] pstrArgs) {

		final String conMethodName = conClassName + "::Main"; //$NON-NLS-1$

		objLogger = new Log4JHelper("./log4j.properties"); //$NON-NLS-1$

		logger = Logger.getRootLogger();
		logger.info("JobSchedulerLaunchAndObserve - Main"); //$NON-NLS-1$

		try {
			JobSchedulerLaunchAndObserve objM = new JobSchedulerLaunchAndObserve();
			JobSchedulerLaunchAndObserveOptions objO = objM.Options();

			objO.CommandLineArgs(pstrArgs);
			objM.Execute();
		}

		catch (Exception e) {
			System.err.println(conMethodName + ": " + "Error occured ..." + e.getMessage());
			e.printStackTrace(System.err);
			int intExitCode = 99;
			logger.error(String.format("JSJ-E-105: %1$s - terminated with exit-code %2$d", conMethodName, intExitCode), e);
			System.exit(intExitCode);
		}

		logger.info(String.format("JSJ-I-106: %1$s - ended without errors", conMethodName));
	}

} // class JobSchedulerLaunchAndObserveMain