

package sos.scheduler.db;

import com.sos.JSHelper.Basics.JSToolBox;
import org.apache.log4j.Logger;


/**
 * \class 		JobSchedulerPLSQLJobMain - Main-Class for "Launch Database Statement"
 *
 * \brief MainClass to launch JobSchedulerPLSQLJob as an executable command-line program
 *
 * This Class JobSchedulerPLSQLJobMain is the worker-class.
 *

 *
 * see \see R:\backup\sos\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc\JobSchedulerPLSQLJob.xml for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by C:\Users\KB\eclipse\xsl\JSJobDoc2JSMainClass.xsl from http://www.sos-berlin.com at 20120905153620
 * \endverbatim
 */
public class JobSchedulerPLSQLJobMain extends JSToolBox {
	
	private final static String					conClassName						= "JobSchedulerPLSQLJobMain"; //$NON-NLS-1$
	private static Logger		logger			= Logger.getLogger(JobSchedulerPLSQLJobMain.class);

	protected JobSchedulerPLSQLJobOptions	objOptions			= null;

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
	public final static void main(final String[] pstrArgs) {

		final String conMethodName = conClassName + "::Main"; //$NON-NLS-1$

		logger.info("JobSchedulerPLSQLJob - Main"); //$NON-NLS-1$

		try {
			JobSchedulerPLSQLJob objM = new JobSchedulerPLSQLJob();
			JobSchedulerPLSQLJobOptions objO = objM.Options();

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
		System.exit(0);
	}

}  // class JobSchedulerPLSQLJobMain