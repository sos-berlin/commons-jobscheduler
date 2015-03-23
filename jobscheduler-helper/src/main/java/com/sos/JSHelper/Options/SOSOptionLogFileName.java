package com.sos.JSHelper.Options;

import java.io.IOException;
import java.util.Enumeration;

import org.apache.log4j.Appender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.RollingFileAppender;

import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Logging.SOSHtmlLayout;
import com.sos.JSHelper.io.Files.JSTextFile;

/**
* \class SOSOptionLogFileName
*
* \brief SOSOptionLogFileName -
*
* \details
* This class is to implement an adapter to change the logging file name for a log4j configuration.
*
* \code
*   .... code goes here ...
* \endcode
*
* <p style="text-align:center">
* <br />---------------------------------------------------------------------------
* <br /> APL/Software GmbH - Berlin
* <br />##### generated by ClaviusXPress (http://www.sos-berlin.com) #########
* <br />---------------------------------------------------------------------------
* </p>
* \author eqbfd
* @version $Id$23.01.2009
* \see reference
*
* Created on 23.01.2009 17:00:04
 */

/**
 * @author eqbfd
 *
 */
public class SOSOptionLogFileName extends SOSOptionOutFileName {

	private static final long	serialVersionUID	= 144340120069043974L;
	private final String		conClassName		= "JSOptionOutFileName";
	private static Logger	logger				= Logger.getLogger(SOSOptionLogFileName.class);
	private FileAppender		objFileAppender		= null;
	@SuppressWarnings("hiding")
	public String ControlType = "file";
	/**
	 * \brief SOSOptionLogFileName
	 *
	 * \details
	 *
	 * @param pPobjParent
	 * @param pPstrKey
	 * @param pPstrDescription
	 * @param pPstrValue
	 * @param pPstrDefaultValue
	 * @param pPflgIsMandatory
	 * @throws Exception
	 */
	public SOSOptionLogFileName(final JSOptionsClass pPobjParent, final String pPstrKey, final String pPstrDescription, final String pPstrValue, final String pPstrDefaultValue,
			final boolean pPflgIsMandatory) {
		super(pPobjParent, pPstrKey, pPstrDescription, pPstrValue, pPstrDefaultValue, pPflgIsMandatory);
		// TODO Auto-generated constructor stub
	}

	private String	strHtmlLogFile	= "";

	public String getHtmlLogFileName() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getHtmlLogFileName";

		if (isEmpty(strHtmlLogFile) == false) {
			return strHtmlLogFile;
		}
		else {
			return "";
		}

		//	return String;
	} // private String getHtmlLogFileName

	public void resetHTMLEntities() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::resetHTMLEntities";

		if (isNotEmpty(strHtmlLogFile) == true && objFileAppender != null) {
			objFileAppender.close();
			JSTextFile objF = new JSTextFile(strHtmlLogFile);
			try {
				objF.replaceString("&lt;", "<");
				objF.replaceString("&gt;", ">");
			}
			catch (IOException e) {
			}
		}

		// return String;
	} // private String resetHTMLEntities

	public String getContent() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getContent";

		String strContent = "";

		return strContent;
	} // private String getContent

	public void setLogger(final Logger pobjLogger) {
		if (pobjLogger != null && this.isDirty()) {
			try {
				logger = pobjLogger;
				@SuppressWarnings("rawtypes")
				Enumeration appenders = pobjLogger.getAllAppenders();
				objFileAppender = null;
				while (appenders.hasMoreElements()) {
					Appender currAppender = (Appender) appenders.nextElement();
					if (currAppender != null) {
						if (currAppender instanceof FileAppender || currAppender instanceof RollingFileAppender) {
							objFileAppender = (FileAppender) currAppender;
							if (objFileAppender != null) {
								String strLogFileName = this.Value();
								if (objFileAppender.getLayout() instanceof SOSHtmlLayout) {
									if (isNotNull(objParentClass)) {
										/**
										 * This is a dirty trick:
										 * get the optionname by name will check, wether the option is present.
										 * if not, the title will not changed
										 * This coding below, with profile and settings, is for JADE
										 */
										String strProfile = objParentClass.OptionByName("profile");
										if (isNotEmpty(strProfile)) {
											String strSettings = objParentClass.OptionByName("settings");
											if (isNotEmpty(strSettings)) {
												strSettings += ":";
											}
											else {
												strSettings = "";
											}
											SOSHtmlLayout objLayout = (SOSHtmlLayout) objFileAppender.getLayout();
											String strTitle = objLayout.getTitle();
											objLayout.setTitle("[" + strSettings + strProfile + "] - " + strTitle);

										}
									}
									strLogFileName = strLogFileName + ".html";
									objFileAppender.setFile(strLogFileName);
									logger.debug(Messages.getMsg("%2$s: filename changed to '%1$s'", strLogFileName, "log4J.HTMLLayout"));
									strHtmlLogFile = strLogFileName;
								}
								else {
									objFileAppender.setFile(strLogFileName);
									logger.debug(Messages.getMsg("%2$s: filename changed to '%1$s'", strLogFileName, "log4J.FileAppender"));
								}
								objFileAppender.activateOptions();
							}
						}
					}
				}
				if (objFileAppender == null) {
					logger.info("No File Appender found");
				}
			}
			catch (Exception e) {
				logger.error(e.getLocalizedMessage());
				throw new JobSchedulerException("Problems with log4jappender", e);
			}
		}
		else {
			logger.trace("setLogger without instance of logger called.");
		}
	}

}
