package sos.scheduler.db;

import org.apache.log4j.Logger;

import sos.scheduler.job.JobSchedulerJobAdapter;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

// Super-Class for JobScheduler Java-API-Jobs
/**
 * \class 		SOSSQLPlusJobJSAdapterClass - JobScheduler Adapter for "Start SQL*Plus client and execute sql*plus programs"
 *
 * \brief AdapterClass of SOSSQLPlusJob for the SOSJobScheduler
 *
 * This Class SOSSQLPlusJobJSAdapterClass works as an adapter-class between the SOS
 * JobScheduler and the worker-class SOSSQLPlusJob.
 *

 *
 * see \see R:\backup\sos\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc\SOSSQLPlusJob.xml for more details.
 *
 * \verbatim ;
 * mechanicaly created by C:\Users\KB\eclipse - Kopie\xsl\JSJobDoc2JSAdapterClass.xsl from http://www.sos-berlin.com at 20120927164006
 * \endverbatim
 */
public class SOSSQLPlusJobJSAdapterClass extends JobSchedulerJobAdapter {
	private final String	conClassName	= "SOSSQLPlusJobJSAdapterClass";						//$NON-NLS-1$
	@SuppressWarnings({ "unused", "hiding" })
	private static Logger	logger			= Logger.getLogger(SOSSQLPlusJobJSAdapterClass.class);

	public void init() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::init"; //$NON-NLS-1$
		doInitialize();
	}

	private void doInitialize() {
	} // doInitialize

	@Override
	public boolean spooler_init() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::spooler_init"; //$NON-NLS-1$
		return super.spooler_init();
	}

	@Override
	public boolean spooler_process() throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::spooler_process"; //$NON-NLS-1$

		try {
			super.spooler_process();
			doProcessing();
		}
		catch (Exception e) {
			throw new JobSchedulerException("Fatal Error:" + e.getMessage(), e);
		}
		finally {
		} // finally
		return signalSuccess();

	} // spooler_process

	@Override
	public void spooler_exit() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::spooler_exit"; //$NON-NLS-1$
		super.spooler_exit();
	}

	private void doProcessing() throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::doProcessing"; //$NON-NLS-1$

		SOSSQLPlusJob objR = new SOSSQLPlusJob();
		SOSSQLPlusJobOptions objO = objR.Options();
		objO.CurrentNodeName(this.getCurrentNodeName());

		objO.setAllOptions(getSchedulerParameterAsProperties(getJobOrOrderParameters()));
		// TODO Use content of <script> tag of job as value of command_script_file parameter
		// http://www.sos-berlin.com/jira/browse/JITL-49
		setJobScript(objO.command_script_file);

		objO.CheckMandatory();
		objR.setJSJobUtilites(this);
		objR.Execute();
	} // doProcessing

}
