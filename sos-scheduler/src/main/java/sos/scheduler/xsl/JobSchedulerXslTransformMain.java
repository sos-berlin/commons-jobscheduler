

package sos.scheduler.xsl;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Logging.Log4JHelper;


/**
 * \class 		JobSchedulerXslTransformationMain - Main-Class for "JobSchedulerXslTransform"
 *
 * \brief MainClass to launch JobSchedulerXslTransform as an executable command-line program
 *
 * This Class JobSchedulerXslTransformationMain is the worker-class.
 *

 *
 * see \see J:\E\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc\JobSchedulerXslTransform.xml for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by C:\Users\KB\eclipse\sos.scheduler.xsl\JSJobDoc2JSMainClass.xsl from http://www.sos-berlin.com at 20110815114233
 * \endverbatim
 */
public class JobSchedulerXslTransformMain extends JSToolBox {
	private final static String					conClassName						= "JobSchedulerXslTransformationMain"; //$NON-NLS-1$
	private static Logger		logger			= Logger.getLogger(JobSchedulerXslTransformMain.class);
	@SuppressWarnings("unused")
	private static Log4JHelper	objLogger		= null;

	protected JobSchedulerXslTransformOptions	objOptions			= null;

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

		objLogger = new Log4JHelper("./log4j.properties"); //$NON-NLS-1$

		logger = Logger.getRootLogger();
		logger.info("JobSchedulerXslTransform - Main"); //$NON-NLS-1$

		try {
			JobSchedulerXslTransform objM = new JobSchedulerXslTransform();
			JobSchedulerXslTransformOptions objO = objM.Options();

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

}  // class JobSchedulerXslTransformationMain