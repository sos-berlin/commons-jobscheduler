

package sos.scheduler.db;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Logging.Log4JHelper;


/**
 * \class 		SOSSQLPlusJobMain - Main-Class for "Start SQL*Plus client and execute sql*plus programs"
 *
 * \brief MainClass to launch SOSSQLPlusJob as an executable command-line program
 *
 * This Class SOSSQLPlusJobMain is the worker-class.
 *

 *
 * see \see R:\backup\sos\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc\SOSSQLPlusJob.xml for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by C:\Users\KB\eclipse - Kopie\xsl\JSJobDoc2JSMainClass.xsl from http://www.sos-berlin.com at 20120927164116
 * \endverbatim
 */
public class SOSSQLPlusJobMain extends JSToolBox {
	private final static String					conClassName						= "SOSSQLPlusJobMain"; //$NON-NLS-1$
	private static Logger		logger			= Logger.getLogger(SOSSQLPlusJobMain.class);
	@SuppressWarnings("unused")
	private static Log4JHelper	objLogger		= null;

	protected SOSSQLPlusJobOptions	objOptions			= null;

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
		logger.info("SOSSQLPlusJob - Main"); //$NON-NLS-1$

		try {
			SOSSQLPlusJob objM = new SOSSQLPlusJob();
			SOSSQLPlusJobOptions objO = objM.Options();

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

}  // class SOSSQLPlusJobMain