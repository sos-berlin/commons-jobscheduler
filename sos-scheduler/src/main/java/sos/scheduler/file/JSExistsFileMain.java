package sos.scheduler.file;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.i18n.annotation.I18NResourceBundle;
import org.apache.log4j.Logger;

/**
 * \class 		JSExistsFileMain - Main-Class for "check wether a file exist"
 *
 * \brief MainClass to launch JSExistFile as an executable command-line program
 *
 * This Class JSExistsFileMain is the worker-class.
 *

 *
 * see \see C:\Users\KB\Documents\xmltest\JSExistFile.xml for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by C:\Users\KB\eclipse\xsl\JSJobDoc2JSMainClass.xsl from http://www.sos-berlin.com at 20110820121009 
 * \endverbatim
 */
@I18NResourceBundle(baseName = "com_sos_scheduler_messages", defaultLocale = "en")
public class JSExistsFileMain extends JSToolBox {
	private final static String		conClassName	= "JSExistsFileMain";						//$NON-NLS-1$
	private static Logger			logger			= Logger.getLogger(JSExistsFileMain.class);

	protected JSExistsFileOptions	objOptions		= null;

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

		logger.info("JSExistFile - Main"); //$NON-NLS-1$

		try {
			JSExistsFile objM = new JSExistsFile();
			JSExistsFileOptions objO = objM.Options();

			objO.CommandLineArgs(pstrArgs);
			boolean flgResult = objM.Execute();
			if (flgResult) {
				System.exit(0);
			}
			else {
				System.exit(99);
			}
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

} // class JSExistsFileMain