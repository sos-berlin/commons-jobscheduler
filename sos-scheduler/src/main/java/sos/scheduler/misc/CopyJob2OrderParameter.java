package sos.scheduler.misc;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSJobUtilities;
import com.sos.JSHelper.Basics.JSToolBox;

/**
 * \class 		CopyJob2OrderParameter - Workerclass for "CopyJob2OrderParameter"
 *
 * \brief AdapterClass of CopyJob2OrderParameter for the SOSJobScheduler
 *
 * This Class CopyJob2OrderParameter is the worker-class.
 *

 *
 * see \see J:\E\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc\CopyJob2OrderParameter.xml for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by C:\Users\KB\eclipse\xsl\JSJobDoc2JSWorkerClass.xsl from http://www.sos-berlin.com at 20111104174303 
 * \endverbatim
 */
public class CopyJob2OrderParameter extends JSToolBox implements JSJobUtilities {
	private final String					conClassName		= "CopyJob2OrderParameter";						//$NON-NLS-1$
	private static Logger					logger				= Logger.getLogger(CopyJob2OrderParameter.class);

	protected CopyJob2OrderParameterOptions	objOptions			= null;
	private JSJobUtilities					objJSJobUtilities	= this;

	/**
	 * 
	 * \brief CopyJob2OrderParameter
	 *
	 * \details
	 *
	 */
	public CopyJob2OrderParameter() {
		super();
	}

	/**
	 * 
	 * \brief Options - returns the CopyJob2OrderParameterOptionClass
	 * 
	 * \details
	 * The CopyJob2OrderParameterOptionClass is used as a Container for all Options (Settings) which are
	 * needed.
	 *  
	 * \return CopyJob2OrderParameterOptions
	 *
	 */
	public CopyJob2OrderParameterOptions Options() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::Options"; //$NON-NLS-1$

		if (objOptions == null) {
			objOptions = new CopyJob2OrderParameterOptions();
		}
		return objOptions;
	}

	/**
	 * 
	 * \brief Options - set the CopyJob2OrderParameterOptionClass
	 * 
	 * \details
	 * The CopyJob2OrderParameterOptionClass is used as a Container for all Options (Settings) which are
	 * needed.
	 *  
	 * \return CopyJob2OrderParameterOptions
	 *
	 */
	public CopyJob2OrderParameterOptions Options(final CopyJob2OrderParameterOptions pobjOptions) {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::Options"; //$NON-NLS-1$

		objOptions = pobjOptions;
		return objOptions;
	}

	/**
	 * 
	 * \brief Execute - Start the Execution of CopyJob2OrderParameter
	 * 
	 * \details
	 * 
	 * For more details see
	 * 
	 * \see JobSchedulerAdapterClass 
	 * \see CopyJob2OrderParameterMain
	 * 
	 * \return CopyJob2OrderParameter
	 *
	 * @return
	 */
	public CopyJob2OrderParameter Execute() throws Exception {
		final String conMethodName = conClassName + "::Execute"; //$NON-NLS-1$

		logger.debug(String.format(Messages.getMsg("JSJ-I-110"), conMethodName));

		try {
			Options().CheckMandatory();
			logger.debug(Options().toString());

			HashMap<String, String> objSettings = Options().Settings();
			for (final Object element : objSettings.entrySet()) {
				final Map.Entry mapItem = (Map.Entry) element;
				String strMapKey = mapItem.getKey().toString();
				String strTemp = mapItem.getValue().toString();
				objJSJobUtilities.setJSParam(strMapKey, strTemp);
			}
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

} // class CopyJob2OrderParameter