

package sos.scheduler.misc;

import com.sos.JSHelper.Basics.JSToolBox;
import org.apache.log4j.Logger;


/**
 * \class 		CopyJob2OrderParameterMain - Main-Class for "CopyJob2OrderParameter"
 *
 * \brief MainClass to launch CopyJob2OrderParameter as an executable command-line program
 *
 * This Class CopyJob2OrderParameterMain is the worker-class.
 *

 *
 * see \see J:\E\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc\CopyJob2OrderParameter.xml for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by C:\Users\KB\eclipse\xsl\JSJobDoc2JSMainClass.xsl from http://www.sos-berlin.com at 20111104174329 
 * \endverbatim
 */
public class CopyJob2OrderParameterMain extends JSToolBox {
	private final static String					conClassName						= "CopyJob2OrderParameterMain"; //$NON-NLS-1$
	private static Logger		logger			= Logger.getLogger(CopyJob2OrderParameterMain.class);

	protected CopyJob2OrderParameterOptions	objOptions			= null;

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

		logger.info("CopyJob2OrderParameter - Main"); //$NON-NLS-1$

		try {
			CopyJob2OrderParameter objM = new CopyJob2OrderParameter();
			CopyJob2OrderParameterOptions objO = objM.Options();
			
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

}  // class CopyJob2OrderParameterMain