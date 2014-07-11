package sos.scheduler.db;
import org.apache.log4j.Logger;

import sos.scheduler.job.JobSchedulerJobAdapter;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

/**
 * \class 		JobSchedulerPLSQLJobJSAdapterClass - JobScheduler Adapter for "Launch Database Statement"
 *
 * \brief AdapterClass of JobSchedulerPLSQLJob for the SOSJobScheduler
 *
 * This Class JobSchedulerPLSQLJobJSAdapterClass works as an adapter-class between the SOS
 * JobScheduler and the worker-class JobSchedulerPLSQLJob.
 *

 *
 * see \see R:\backup\sos\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc\JobSchedulerPLSQLJob.xml for more details.
 *
 * \verbatim ;
 * mechanicaly created by C:\Users\KB\eclipse\xsl\JSJobDoc2JSAdapterClass.xsl from http://www.sos-berlin.com at 20120905153510
 * \endverbatim
 */
public class JobSchedulerPLSQLJobJSAdapterClass extends JobSchedulerJobAdapter {
	private final String	conClassName	= "JobSchedulerPLSQLJobJSAdapterClass";						//$NON-NLS-1$
	@SuppressWarnings({ "unused", "hiding" })
	private final Logger	logger			= Logger.getLogger(JobSchedulerPLSQLJobJSAdapterClass.class);

	public void init() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::init"; //$NON-NLS-1$
		doInitialize();
	}

	private void doInitialize() {
	} // doInitialize

	@Override public boolean spooler_init() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::spooler_init"; //$NON-NLS-1$
		return super.spooler_init();
	}

	@Override public boolean spooler_process() throws Exception {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::spooler_process"; //$NON-NLS-1$
		try {
			super.spooler_process();
			doProcessing();
		}
		catch (Exception e) {
			throw new JobSchedulerException("Fatal Error", e);
		}
		finally {
		} // finally
		return signalSuccess();
	} // spooler_process

	@Override public void spooler_exit() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::spooler_exit"; //$NON-NLS-1$
		super.spooler_exit();
	}

	private void doProcessing() throws Exception {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::doProcessing"; //$NON-NLS-1$
		JobSchedulerPLSQLJob objR = new JobSchedulerPLSQLJob();
		JobSchedulerPLSQLJobOptions objO = objR.Options();
		objO.CurrentNodeName(this.getCurrentNodeName());
		objO.setAllOptions(getSchedulerParameterAsProperties(getJobOrOrderParameters()));
		// TODO Use content of <script> tag of job as value of command parameter
		// http://www.sos-berlin.com/jira/browse/JITL-50
		setJobScript(objO.command);
		objO.CheckMandatory();
		objR.setJSJobUtilites(this);
		objR.Execute();
	} // doProcessing
}
