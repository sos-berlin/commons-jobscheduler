

package sos.scheduler.managed.db;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Basics.JSJobUtilities;
import com.sos.JSHelper.Basics.JSToolBox;

/**
 * \class 		JobSchedulerManagedDBReportJob - Workerclass for "Launch Database Report"
 *
 * \brief AdapterClass of JobSchedulerManagedDBReportJob for the SOSJobScheduler
 *
 * This Class JobSchedulerManagedDBReportJob is the worker-class.
 *

 *
 * see \see R:\backup\sos\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc\JobSchedulerManagedDBReportJob.xml for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by C:\Users\KB\eclipse\xsl\JSJobDoc2JSWorkerClass.xsl from http://www.sos-berlin.com at 20120830214403 
 * \endverbatim
 */
public class JobSchedulerManagedDBReportJob extends JSToolBox implements JSJobUtilities {
	private final String					conClassName						= "JobSchedulerManagedDBReportJob";  //$NON-NLS-1$
	private static Logger		logger			= Logger.getLogger(JobSchedulerManagedDBReportJob.class);

	protected JobSchedulerManagedDBReportJobOptions	objOptions			= null;
    private JSJobUtilities      objJSJobUtilities   = this;


	/**
	 * 
	 * \brief JobSchedulerManagedDBReportJob
	 *
	 * \details
	 *
	 */
	public JobSchedulerManagedDBReportJob() {
		super();
	}

	/**
	 * 
	 * \brief Options - returns the JobSchedulerManagedDBReportJobOptionClass
	 * 
	 * \details
	 * The JobSchedulerManagedDBReportJobOptionClass is used as a Container for all Options (Settings) which are
	 * needed.
	 *  
	 * \return JobSchedulerManagedDBReportJobOptions
	 *
	 */
	public JobSchedulerManagedDBReportJobOptions Options() {

		@SuppressWarnings("unused")  
		final String conMethodName = conClassName + "::Options";  //$NON-NLS-1$

		if (objOptions == null) {
			objOptions = new JobSchedulerManagedDBReportJobOptions();
		}
		return objOptions;
	}

	/**
	 * 
	 * \brief Options - set the JobSchedulerManagedDBReportJobOptionClass
	 * 
	 * \details
	 * The JobSchedulerManagedDBReportJobOptionClass is used as a Container for all Options (Settings) which are
	 * needed.
	 *  
	 * \return JobSchedulerManagedDBReportJobOptions
	 *
	 */
	public JobSchedulerManagedDBReportJobOptions Options(final JobSchedulerManagedDBReportJobOptions pobjOptions) {

		@SuppressWarnings("unused")  
		final String conMethodName = conClassName + "::Options";  //$NON-NLS-1$

		objOptions = pobjOptions;
		return objOptions;
	}

	/**
	 * 
	 * \brief Execute - Start the Execution of JobSchedulerManagedDBReportJob
	 * 
	 * \details
	 * 
	 * For more details see
	 * 
	 * \see JobSchedulerAdapterClass 
	 * \see JobSchedulerManagedDBReportJobMain
	 * 
	 * \return JobSchedulerManagedDBReportJob
	 *
	 * @return
	 */
	public JobSchedulerManagedDBReportJob Execute() throws Exception {
		final String conMethodName = conClassName + "::Execute";  //$NON-NLS-1$

		logger.debug(String.format(Messages.getMsg("JSJ-I-110"), conMethodName ) );

		try { 
			Options().CheckMandatory();
			logger.debug(Options().toString());
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			logger.error(String.format(Messages.getMsg("JSJ-I-107"), conMethodName ), e);
		}
		finally {
			logger.debug(String.format(Messages.getMsg("JSJ-I-111"), conMethodName ) );
		}
		
		return this;
	}

	public void init() {
		@SuppressWarnings("unused")  
		final String conMethodName = conClassName + "::init";  //$NON-NLS-1$
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



}  // class JobSchedulerManagedDBReportJob