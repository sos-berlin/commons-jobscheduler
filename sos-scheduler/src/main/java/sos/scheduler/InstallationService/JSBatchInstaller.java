package sos.scheduler.InstallationService;
import java.util.Locale;

import org.apache.log4j.Logger;

import sos.scheduler.InstallationService.batchInstallationModel.JSBatchInstallerExecuter;

import com.sos.JSHelper.Basics.IJSCommands;
import com.sos.JSHelper.Basics.JSJobUtilities;
import com.sos.JSHelper.Basics.JSToolBox;
import com.sos.i18n.annotation.I18NResourceBundle;
import com.sos.localization.Messages;

/**
 * \class 		JSBatchInstaller - Workerclass for "Unattended Batch Installation on remote servers"
 *
 * \brief AdapterClass of JSBatchInstaller for the SOSJobScheduler
 *
 * This Class JSBatchInstaller is the worker-class.
 *

 *
 * see \see C:\Users\KB\Downloads\Preislisten\JSBatchInstaller.xml for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by C:\Users\KB\eclipse\sos.scheduler.xsl\JSJobDoc2JSWorkerClass.xsl from http://www.sos-berlin.com at 20110322142418 
 * \endverbatim
 */
@I18NResourceBundle(baseName = "com.sos.scheduler.messages", defaultLocale = "en")
public class JSBatchInstaller extends JSToolBox implements JSJobUtilities, IJSCommands {
	private final String				conClassName		= "JSBatchInstaller";									//$NON-NLS-1$
	private static Logger				logger				= null /* Logger.getLogger(JSBatchInstaller.class) */;
	protected JSBatchInstallerOptions	objOptions			= null;
	private JSJobUtilities				objJSJobUtilities	= this;
	private IJSCommands					objJSCommands		= this;

	/**
	 * 
	 * \brief JSBatchInstaller
	 *
	 * \details
	 *
	 */
	public JSBatchInstaller() {
		super();
		logger = Logger.getLogger(JSBatchInstaller.class);
		Messages = new Messages ("com_sos_scheduler_messages", Locale.getDefault());
	}

	public IJSCommands getJSCommands() {
		return objJSCommands;
	}

	/**
	 * 
	 * \brief Options - returns the JSBatchInstallerOptionClass
	 * 
	 * \details
	 * The JSBatchInstallerOptionClass is used as a Container for all Options (Settings) which are
	 * needed.
	 *  
	 * \return JSBatchInstallerOptions
	 *
	 */
	public JSBatchInstallerOptions Options() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::Options"; //$NON-NLS-1$
		if (objOptions == null) {
			objOptions = new JSBatchInstallerOptions();
		}
		return objOptions;
	}

	/**
	 * 
	 * \brief Options - set the JSBatchInstallerOptionClass
	 * 
	 * \details
	 * The JSBatchInstallerOptionClass is used as a Container for all Options (Settings) which are
	 * needed.
	 *  
	 * \return JSBatchInstallerOptions
	 *
	 */
	public JSBatchInstallerOptions Options(final JSBatchInstallerOptions pobjOptions) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::Options"; //$NON-NLS-1$
		objOptions = pobjOptions;
		return objOptions;
	}

	/**
	 * 
	 * \brief Execute - Start the Execution of JSBatchInstaller
	 * 
	 * \details
	 * 
	 * For more details see
	 * 
	 * \see JobSchedulerAdapterClass 
	 * \see JSBatchInstallerMain
	 * 
	 * \return JSBatchInstaller
	 *
	 * @return
	 */
	public JSBatchInstaller Execute() throws Exception {
		final String conMethodName = conClassName + "::Execute"; //$NON-NLS-1$
		logger.debug(String.format(Messages.getMsg("JSJ-I-110"), conMethodName));
		try {
			Options().CheckMandatory();
			logger.debug(Options().toString());
			JSBatchInstallerExecuter jsBatchInstaller = new JSBatchInstallerExecuter();
			jsBatchInstaller.performInstallation(this);
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			logger.error(String.format(Messages.getMsg("JSJ-I-107"), conMethodName), e);
		}
		finally {
			logger.debug(String.format(Messages.getMsg("JSJ-I-111"), conMethodName));
		}
		return this;
	}

	public void init() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::init"; //$NON-NLS-1$
		doInitialize();
	}

	private void doInitialize() {
	} // doInitialize

	@Override
	public String myReplaceAll(final String pstrSourceString, final String pstrReplaceWhat, final String pstrReplaceWith) {
		String newReplacement = pstrReplaceWith.replaceAll("\\$", "\\\\\\$");
		return pstrSourceString.replaceAll("(?m)" + pstrReplaceWhat, newReplacement);
	}

	/**
	 * 
	 * \brief replaceSchedulerVars
	 * 
	 * \details
	 * Dummy-Method to make sure, that there is always a valid Instance for the JSJobUtilities.
	 * \return 
	 *
	 * @param isWindows
	 * @param pstrString2Modify
	 * @return
	 */
	@Override
	public String replaceSchedulerVars(final boolean isWindows, final String pstrString2Modify) {
		logger.debug("replaceSchedulerVars as Dummy-call executed. No Instance of JobUtilites specified.");
		return pstrString2Modify;
	}

	/**
	 * 
	 * \brief setJSParam
	 * 
	 * \details
	 * Dummy-Method to make shure, that there is always a valid Instance for the JSJobUtilities.
	 * \return 
	 *
	 * @param pstrKey
	 * @param pstrValue
	 */
	@Override
	public void setJSParam(final String pstrKey, final String pstrValue) {
	}

	@Override
	public void setJSParam(final String pstrKey, final StringBuffer pstrValue) {
	}

	/**
	 * 
	 * \brief setJSJobUtilites
	 * 
	 * \details
	 * The JobUtilities are a set of methods used by the SSH-Job or can be used be other, similar, job-
	 * implementations.
	 * 
	 * \return void
	 *
	 * @param pobjJSJobUtilities
	 */
	@Override
	public void setJSJobUtilites(final JSJobUtilities pobjJSJobUtilities) {
		if (pobjJSJobUtilities == null) {
			objJSJobUtilities = this;
		}
		else {
			objJSJobUtilities = pobjJSJobUtilities;
		}
		logger.debug("objJSJobUtilities = " + objJSJobUtilities.getClass().getName());
	}

	@Override
	public String getCurrentNodeName() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * 
	 * \brief setJSCommands
	 * 
	 * \details
	 *
	 * \return void
	 *
	 * @param pobjJSCommands
	 */
	public void setJSCommands(final IJSCommands pobjJSCommands) {
		if (pobjJSCommands == null) {
			objJSCommands = this;
		}
		else {
			objJSCommands = pobjJSCommands;
		}
		logger.debug("pobjJSCommands = " + pobjJSCommands.getClass().getName());
	}

	@Override
	public Object getSpoolerObject() {
		return null;
	}

	@Override
	public String executeXML(final String pstrJSXmlCommand) {
		return "";
	}

	@Override
	public void setStateText(final String pstrStateText) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setCC(final int pintCC) {
		// TODO Auto-generated method stub
		
	}

	@Override public void setNextNodeState(final String pstrNodeName) {
		// TODO Auto-generated method stub
		
	}
} // class JSBatchInstaller