package com.sos.DataExchange;

import org.apache.log4j.Logger;

import com.sos.DataExchange.Options.JADEOptions;
import com.sos.JSHelper.Basics.JSJobUtilities;
import com.sos.JSHelper.Basics.JSVersionInfo;
import com.sos.JSHelper.Logging.Log4JHelper;
import com.sos.i18n.I18NBase;
import com.sos.i18n.annotation.I18NMessage;
import com.sos.i18n.annotation.I18NMessages;
import com.sos.i18n.annotation.I18NResourceBundle;

/**
 * \class 		SOSDataExchangeEngineMain - Main-Class for "Transfer files by FTP/SFTP and execute commands by SSH"
 *
 * \brief MainClass to launch sosftp as an executable command-line program
 *
 * This Class SOSDataExchangeEngineMain is the worker-class.
 *

 *
 * see \see J:\E\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc\sosftp.xml for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by C:\Users\KB\eclipse\xsl\JSJobDoc2JSMainClass.xsl from http://www.sos-berlin.com at 20100930175655
 * \endverbatim
 */
@I18NResourceBundle(baseName = "SOSDataExchange", defaultLocale = "en")
public class SOSDataExchangeEngine4DMZMain extends I18NBase implements JSJobUtilities {
	private final static String	conClassName	= "SOSDataExchangeEngine4DMZMain";
	public static final String	conSVNVersion	= "$Id$";

	private static Logger		logger			= Logger.getLogger(SOSDataExchangeEngine4DMZMain.class);
	@SuppressWarnings("unused")
	private static Log4JHelper	objLoggerHelper	= null;
	protected JADEOptions		objOptions		= null;

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

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::Main"; //$NON-NLS-1$

		// will setup basic logging to the console, and the error messages will be gone.
		org.apache.log4j.BasicConfigurator.configure();

		// objLoggerHelper = new Log4JHelper("./log4j.properties");
		//
		// logger = Logger.getRootLogger();

	 	SOSDataExchangeEngine4DMZMain objEngine = new SOSDataExchangeEngine4DMZMain();
	 	objEngine.Execute(pstrArgs);
		
		System.exit(0);
	}

	protected SOSDataExchangeEngine4DMZMain() {
		super("SOSDataExchange");
	}

	/**
	 *
	 * @param pstrArgs
	 */
	private void Execute(final String[] pstrArgs) {

		final String conMethodName = conClassName + "::Execute";

		try {
			Jade4DMZ objM = new Jade4DMZ();
			JADEOptions objO = objM.Options();

			String strLog4jPropertyFileName = objO.log4jPropertyFileName.Value();
			for (String strParam : pstrArgs) {
				if (strParam.toLowerCase().startsWith("-log4jPropertyFileName")) {
					String[] strS = strParam.split("=");
					strLog4jPropertyFileName = strS[1];
				}
			}

			objLoggerHelper = new Log4JHelper(strLog4jPropertyFileName);
			logger = Logger.getRootLogger();

			objM.setJSJobUtilites(this);
			objO.SendTransferHistory.value(true);
			objO.CommandLineArgs(pstrArgs);

			if (objO.log4jPropertyFileName.isDirty()) {
				objLoggerHelper = new Log4JHelper(objO.log4jPropertyFileName.Value());
				logger = Logger.getRootLogger();
			}

			logger.info(getMsg(SOSDX_Intro) + " -- " + JSVersionInfo.getVersionString());
			logger.debug(conSVNVersion);

			objO.CheckMandatory();
			objM.Execute();
		}

		catch (Exception e) {
			logger.error(String.format(getMsg(SOSDX_E_0001), conMethodName, e.getMessage()), e);
			int intExitCode = 99;
			 
			logger.error(String.format(getMsg(SOS_EXIT_CODE_RAISED), conMethodName, intExitCode), e);
			System.exit(intExitCode);
		}

		logger.info(String.format(getMsg(SOS_EXIT_WO_ERRORS), conMethodName));

	} // private void Execute

	@I18NMessages(value = { @I18NMessage("SOSDataExchangeEngine4DMZMain - Main routine started ..."), //
			@I18NMessage(value = "SOSDataExchange4DMZ - Main", locale = "en_UK", //
			explanation = "SOSDataExchange - Main" //
			), //
			@I18NMessage(value = "SOSDataExchange4DMZ - Kommandozeilenprogram startet ....", locale = "de", //
			explanation = "SOSDataExchange4DMZ - Main" //
			) //
	}, msgnum = "SOSDX-I-9999", msgurl = "")
	/*!
	 * \var SOSDX-Intro
	 * \brief SOSDataExchange - Main
	 */
	public static final String	SOSDX_Intro				= "SOSDataExchangeEngineMain.SOSDX-Intro";

	@I18NMessages(value = { @I18NMessage("%1$s: Error occurred ...: %2$s, exit-code 99 raised"), //
			@I18NMessage(value = "%1$s: Error occurred ...: %2$s", locale = "en_UK", //
			explanation = "%1$s: Error occurred ...: %2$s" //
			), //
			@I18NMessage(value = "%1$s: Fehler aufgetreten: %2$s, Programm wird mit Exit-Code 99 beendet.", locale = "de", //
			explanation = "%1$s: Error occurred ...: %2$s" //
			) //
	}, msgnum = "SOSDX-E-0001", msgurl = "")
	/*!
	 * \var SOSDX_E_0001
	 * \brief %1$s: Error occurred ...: %2$s
	 */
	public static final String	SOSDX_E_0001			= "SOSDataExchangeEngineMain.SOSDX_E_0001";

	@I18NMessages(value = { @I18NMessage("%1$s - ended without errorsended without errors"), //
			@I18NMessage(value = "%1$s - ended without errors", locale = "en_UK", //
			explanation = "%1$s - ended without errorsended without errors" //
			), //
			@I18NMessage(value = "%1$s - Programm wurde ohne Fehler beendet", locale = "de", //
			explanation = "%1$s - ended without errorsended without errors" //
			) //
	}, msgnum = "SOS-I-106", msgurl = "")
	/*!
	 * \var SOS_EXIT_WO_ERRORS
	 * \brief %1$s - ended without errorsended without errors
	 */
	public static final String	SOS_EXIT_WO_ERRORS		= "SOSDataExchangeEngineMain.SOS_EXIT_WO_ERRORS";

	@I18NMessages(value = { @I18NMessage("%1$s - terminated with exit-code %2$d"), //
			@I18NMessage(value = "%1$s - terminated with exit-code %2$d", locale = "en_UK", //
			explanation = "%1$s - terminated with exit-code %2$d" //
			), //
			@I18NMessage(value = "%1$s - Fehlercode %2$d wurde gesetzt", locale = "de", //
			explanation = "%1$s - terminated with exit-code %2$d" //
			) //
	}, msgnum = "SOSDX_E_0002", msgurl = "")
	/*!
	 * \var SOS_EXIT_CODE_RAISED
	 * \brief %1$s - terminated with exit-code %2$d
	 */
	public static final String	SOS_EXIT_CODE_RAISED	= "SOSDataExchangeEngineMain.SOS_EXIT_CODE_RAISED";

	@Override
	public String myReplaceAll(final String source, final String what, final String replacement) {
		return source;
	}

	@Override
	public String replaceSchedulerVars(final boolean isWindows, final String pstrString2Modify) {
		return pstrString2Modify;
	}

	@Override
	public void setJSParam(final String pstrKey, final String pstrValue) {
	}

	@Override
	public void setJSParam(final String pstrKey, final StringBuffer pstrValue) {
	}

	@Override
	public String getCurrentNodeName() {
		return "";
	}

	@Override
	public void setJSJobUtilites(final JSJobUtilities pobjJSJobUtilities) {

	}

	@Override
	public void setStateText(final String pstrStateText) {

	}

	@Override
	public void setCC(final int pintCC) {
 		
	}

	@Override public void setNextNodeState(final String pstrNodeName) {
		// TODO Auto-generated method stub
		
	}

} // class SOSDataExchangeEngine4DMZMain