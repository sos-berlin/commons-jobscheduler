package com.sos.scheduler.generics;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSJobUtilitiesClass;

/**
 * \class 		GenericAPIJob - Workerclass for "A generic internal API job"
 *
 * \brief AdapterClass of GenericAPIJob for the SOSJobScheduler
 *
 * This Class GenericAPIJob is the worker-class.
 *

 *
 * see \see C:\Users\KB\AppData\Local\Temp\scheduler_editor-2864692299059909179.html for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by C:\Users\KB\sos-berlin.com\jobscheduler\scheduler\config\JOETemplates\java\xsl\JSJobDoc2JSWorkerClass.xsl from http://www.sos-berlin.com at 20120611173607
 * \endverbatim
 */
public class GenericAPIJob extends JSJobUtilitiesClass <GenericAPIJobOptions> {
	private final String			conClassName		= "GenericAPIJob";							//$NON-NLS-1$
	private static Logger			logger				= Logger.getLogger(GenericAPIJob.class);

//	protected GenericAPIJobOptions	objOptions			= null;
//	private JSJobUtilities			objJSJobUtilities	= this;

	/**
	 *
	 * \brief GenericAPIJob
	 *
	 * \details
	 *
	 */
	public GenericAPIJob() {
		super(new GenericAPIJobOptions());
	}

	/**
	 *
	 * \brief Options - returns the GenericAPIJobOptionClass
	 *
	 * \details
	 * The GenericAPIJobOptionClass is used as a Container for all Options (Settings) which are
	 * needed.
	 *
	 * \return GenericAPIJobOptions
	 *
	 */
	@Override
	public GenericAPIJobOptions Options() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::Options"; //$NON-NLS-1$

		if (objOptions == null) {
			objOptions = new GenericAPIJobOptions();
		}
		return objOptions;
	}

	/**
	 *
	 * \brief Execute - Start the Execution of GenericAPIJob
	 *
	 * \details
	 *
	 * For more details see
	 *
	 * \see JobSchedulerAdapterClass
	 * \see GenericAPIJobMain
	 *
	 * \return GenericAPIJob
	 *
	 * @return
	 */
	public GenericAPIJob Execute() throws Exception {
		final String conMethodName = conClassName + "::Execute"; //$NON-NLS-1$

		logger.debug(String.format(Messages.getMsg("JSJ-I-110"), conMethodName));

		try {
			Options().CheckMandatory();
			logger.debug(Options().toString());
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

} // class GenericAPIJob