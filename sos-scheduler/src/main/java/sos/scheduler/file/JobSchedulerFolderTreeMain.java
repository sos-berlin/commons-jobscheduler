package sos.scheduler.file;

import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.i18n.annotation.I18NResourceBundle;
import com.sos.localization.Messages;
import org.apache.log4j.Logger;

import java.util.Locale;

/**
 * \class 		JobSchedulerFolderTreeMain - Main-Class for "check wether a file exist"
 *
 * \brief MainClass to launch JobSchedulerFolderTree as an executable command-line program
 *
 * This Class JobSchedulerFolderTreeMain is the worker-class.
 *

 *
 * see \see J:\E\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc\JobSchedulerFolderTree.xml for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by C:\Users\KB\eclipse\sos.scheduler.xsl\JSJobDoc2JSMainClass.xsl from http://www.sos-berlin.com at 20110805104825 
 * \endverbatim
 */
@I18NResourceBundle(baseName = "com.sos.scheduler.messages", defaultLocale = "en")
public class JobSchedulerFolderTreeMain extends JSToolBox {
	private final static String				conClassName	= "JobSchedulerFolderTreeMain";										//$NON-NLS-1$
	private static Logger					logger			= Logger.getLogger(JobSchedulerFolderTreeMain.class);
	private final String					conSVNVersion	= "$Id$";
	protected static Messages					Messages			= null;

	protected JobSchedulerFolderTreeOptions	objOptions		= null;

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
		Messages = new Messages("com_sos_scheduler_messages", Locale.getDefault());
		logger.info("JobSchedulerFolderTree - Main"); //$NON-NLS-1$

		try {
			JobSchedulerFolderTree objM = new JobSchedulerFolderTree();
			JobSchedulerFolderTreeOptions objO = objM.Options();

			objO.CommandLineArgs(pstrArgs);
			objM.Execute();
		}

		catch (Exception e) {
			System.err.println(conMethodName + ": " + "Error occured ..." + e.getMessage());
			e.printStackTrace(System.err);
			int intExitCode = 99;
			logger.error(Messages.getMsg("JSJ-E-105: %1$s - terminated with exit-code %2$d", conMethodName, intExitCode), e);
			System.exit(intExitCode);
		}

		logger.info(Messages.getMsg("JSJ-I-106: %1$s - ended without errors", conMethodName));
	}

} // class JobSchedulerFolderTreeMain