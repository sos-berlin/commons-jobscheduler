package com.sos.scheduler.converter.graphviz;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.JSHelper.Logging.Log4JHelper;

/**
 * \class 		JSObjects2GraphvizMain - Main-Class for "JSObjects2Graphviz"
 *
 * \brief MainClass to launch JSObjects2Graphviz as an executable command-line program
 *
 * This Class JSObjects2GraphvizMain is the worker-class.
 *

 *
 * see \see C:\Users\KB\AppData\Local\Temp\scheduler_editor-2781494595910967227.html for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by C:\Users\KB\sos-berlin.com\jobscheduler\scheduler\config\JOETemplates\java\xsl\JSJobDoc2JSMainClass.xsl from http://www.sos-berlin.com at 20121108150924
 * \endverbatim
 */
public class JSObjects2GraphvizMain extends JSToolBox {
	private final static String			conClassName	= "JSObjects2GraphvizMain";						//$NON-NLS-1$
	private static Logger				logger			= Logger.getLogger(JSObjects2GraphvizMain.class);
	@SuppressWarnings("unused")
	private static Log4JHelper			objLogger		= null;

	protected JSObjects2GraphvizOptions	objOptions		= null;

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
		logger.info("JSObjects2Graphviz - Main"); //$NON-NLS-1$

		try {
			JSObjects2Graphviz objM = new JSObjects2Graphviz();
			JSObjects2GraphvizOptions objO = objM.Options();

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

} // class JSObjects2GraphvizMain