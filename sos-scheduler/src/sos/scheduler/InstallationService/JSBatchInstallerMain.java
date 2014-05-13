

package sos.scheduler.InstallationService;

import org.apache.log4j.Logger;
import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Logging.Log4JHelper;


/**
 * \class 		JSBatchInstallerMain - Main-Class for "Unattended Batch Installation on remote servers"
 *
 * \brief MainClass to launch JSBatchInstaller as an executable command-line program
 *
 * This Class JSBatchInstallerMain is the worker-class.
 *

 *
 * see \see C:\Users\KB\Downloads\Preislisten\JSBatchInstaller.xml for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by C:\Users\KB\eclipse\sos.scheduler.xsl\JSJobDoc2JSMainClass.xsl from http://www.sos-berlin.com at 20110322142426 
 * \endverbatim
 */
public class JSBatchInstallerMain extends JSToolBox {
	private final static String					conClassName						= "JSBatchInstallerMain"; //$NON-NLS-1$
	private static Logger		logger			= Logger.getLogger(JSBatchInstallerMain.class);
	@SuppressWarnings("unused")	
	private static Log4JHelper	objLogger		= null;

	protected JSBatchInstallerOptions	objOptions			= null;

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
		logger.info("JSBatchInstaller - Main"); //$NON-NLS-1$

		try {
			JSBatchInstaller objM = new JSBatchInstaller();
			JSBatchInstallerOptions objO = objM.Options();
			
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

}  // class JSBatchInstallerMain