package sos.scheduler.file;

import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.sos.i18n.annotation.I18NResourceBundle;

/**
 * \class 		JSExistsFile - Workerclass for "check wether a file exist"
 *
 * \brief AdapterClass of JSExistFile for the SOSJobScheduler
 *
 * This Class JSExistsFile is the worker-class.
 *

 *
 * see \see C:\Users\KB\Documents\xmltest\JSExistFile.xml for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by C:\Users\KB\eclipse\xsl\JSJobDoc2JSWorkerClass.xsl from http://www.sos-berlin.com at 20110820120954 
 * \endverbatim
 */
@I18NResourceBundle(baseName = "com_sos_scheduler_messages", defaultLocale = "en")
public class JSExistsFile extends JSFileOperationBase {
	@SuppressWarnings("hiding")
	private final String	conClassName	= "JSExistsFile";														
	@SuppressWarnings("hiding")
	private static Logger	logger			= Logger.getLogger(JSExistsFile.class);
	private final String	conSVNVersion	= "$Id$";

	/**
	 * 
	 * \brief JSExistsFile
	 *
	 * \details
	 *
	 */
	public JSExistsFile() {
		super();
	}

	/**
	 * 
	 * \brief Execute - Start the Execution of JSExistsFile
	 * 
	 * \details
	 * 
	 * For more details see
	 * 
	 * \see JobSchedulerAdapterClass 
	 * \see JSExistsFileMain
	 * 
	 * \return JSExistsFile
	 *
	 * @return
	 */
	public boolean Execute() throws Exception {
		final String conMethodName = conClassName + "::Execute"; 

		logger.debug(String.format(Messages.getMsg("JSJ-I-110"), conMethodName));

		try {
			logger.info(conSVNVersion);
			initialize();
			Options().file.CheckMandatory();
			Options().file_spec.setRegExpFlags(Pattern.CASE_INSENSITIVE);

			flgOperationWasSuccessful = existsFile(Options().file, Options().file_spec, //
					Options().min_file_age, Options().max_file_age, Options().min_file_size, Options().max_file_size, //
					Options().skip_first_files, //
					Options().skip_last_files, -1, -1);

			flgOperationWasSuccessful = createResultListParam(flgOperationWasSuccessful);
			return flgOperationWasSuccessful;

//			if (flgOperationWasSuccessful == true) {
//				flgOperationWasSuccessful = checkSteadyStateOfFiles();
//			}
//			return setReturnResult(flgOperationWasSuccessful);

		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			logger.error(Messages.getMsg("JSJ-I-107", conMethodName), e);
		}
		finally {
			if(flgOperationWasSuccessful) {
				logger.debug(Messages.getMsg("JSJ-I-111", conMethodName));
			}
		}

		return flgOperationWasSuccessful;
	}

	public void init() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::init"; 
		doInitialize();
	}

	private void doInitialize() {
	} // doInitialize

} // class JSExistsFile