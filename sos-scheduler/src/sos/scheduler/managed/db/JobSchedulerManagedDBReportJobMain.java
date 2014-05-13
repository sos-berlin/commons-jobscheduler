

package sos.scheduler.managed.db;

import org.apache.log4j.Logger;
import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Logging.Log4JHelper;


/**
 * \class 		JobSchedulerManagedDBReportJobMain - Main-Class for "Launch Database Report"
 *
 * \brief MainClass to launch JobSchedulerManagedDBReportJob as an executable command-line program
 *
 * This Class JobSchedulerManagedDBReportJobMain is the worker-class.
 *

 *
 * see \see R:\backup\sos\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc\JobSchedulerManagedDBReportJob.xml for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by C:\Users\KB\eclipse\xsl\JSJobDoc2JSMainClass.xsl from http://www.sos-berlin.com at 20120830214436 
 * \endverbatim
 */
public class JobSchedulerManagedDBReportJobMain extends JSToolBox {
	private final static String					conClassName						= "JobSchedulerManagedDBReportJobMain"; //$NON-NLS-1$
	private static Logger		logger			= Logger.getLogger(JobSchedulerManagedDBReportJobMain.class);
	@SuppressWarnings("unused")	
	private static Log4JHelper	objLogger		= null;

	protected JobSchedulerManagedDBReportJobOptions	objOptions			= null;

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
		logger.info("JobSchedulerManagedDBReportJob - Main"); //$NON-NLS-1$

		try {
			JobSchedulerManagedDBReportJob objM = new JobSchedulerManagedDBReportJob();
			JobSchedulerManagedDBReportJobOptions objO = objM.Options();
			
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

}  // class JobSchedulerManagedDBReportJobMain