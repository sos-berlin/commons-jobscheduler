

package sos.scheduler.reports;

import org.apache.log4j.Logger;
import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Logging.Log4JHelper;


/**
 * \class 		JSReportAllParametersMain - Main-Class for "Report all Parameters"
 *
 * \brief MainClass to launch JSReportAllParameters as an executable command-line program
 *
 * This Class JSReportAllParametersMain is the worker-class.
 *

 *
 * see \see J:\E\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc\JSReportAllParameters.xml for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by C:\Users\KB\eclipse\sos.scheduler.xsl\JSJobDoc2JSMainClass.xsl from http://www.sos-berlin.com at 20110516150420 
 * \endverbatim
 */
public class JSReportAllParametersMain extends JSToolBox {
	private final static String					conClassName						= "JSReportAllParametersMain"; //$NON-NLS-1$
	private static Logger		logger			= Logger.getLogger(JSReportAllParametersMain.class);
	@SuppressWarnings("unused")	
	private static Log4JHelper	objLogger		= null;

	protected JSReportAllParametersOptions	objOptions			= null;

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
		logger.info("JSReportAllParameters - Main"); //$NON-NLS-1$

		try {
			JSReportAllParameters objM = new JSReportAllParameters();
			JSReportAllParametersOptions objO = objM.Options();
			
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

}  // class JSReportAllParametersMain