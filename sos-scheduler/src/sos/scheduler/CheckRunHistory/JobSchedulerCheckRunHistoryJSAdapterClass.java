package sos.scheduler.CheckRunHistory;
import org.apache.log4j.Logger;

import sos.scheduler.job.JobSchedulerJobAdapter;

/**
 * \class 		JobSchedulerCheckRunHistoryJSAdapterClass - JobScheduler Adapter for "Check the last job run"
 *
 * \brief AdapterClass of JobSchedulerCheckRunHistory for the SOSJobScheduler
 *
 * This Class JobSchedulerCheckRunHistoryJSAdapterClass works as an adapter-class between the SOS
 * JobScheduler and the worker-class JobSchedulerCheckRunHistory.
 *

 *
 * see \see J:\E\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc\JobSchedulerCheckRunHistory.xml for more details.
 *
 * \verbatim ;
 * mechanicaly created by C:\Users\KB\eclipse\sos.scheduler.xsl\JSJobDoc2JSAdapterClass.xsl from http://www.sos-berlin.com at 20110224143559 
 * \endverbatim
 */
public class JobSchedulerCheckRunHistoryJSAdapterClass extends JobSchedulerJobAdapter  {
	private final String	conClassName	= "JobSchedulerCheckRunHistoryJSAdapterClass";							//$NON-NLS-1$
	@SuppressWarnings("unused")
	private static Logger	logger			= Logger.getLogger(JobSchedulerCheckRunHistoryJSAdapterClass.class);

	public void init() {
		@SuppressWarnings("unused")//$NON-NLS-1$
		final String conMethodName = conClassName + "::init"; //$NON-NLS-1$
		doInitialize();
	}

	private void doInitialize() {
	} // doInitialize

	@Override
	public boolean spooler_init() {
		@SuppressWarnings("unused")//$NON-NLS-1$
		final String conMethodName = conClassName + "::spooler_init"; //$NON-NLS-1$
		return super.spooler_init();
	}

	@Override
	public boolean spooler_process() throws Exception {
		@SuppressWarnings("unused")//$NON-NLS-1$
		final String conMethodName = conClassName + "::spooler_process"; //$NON-NLS-1$
		try {
			super.spooler_process();
			doProcessing();
		}
		catch (Exception e) {
			return false;
		}
		finally {
		} // finally
			// return value for classic and order driven processing
			// TODO create method in base-class for this functionality
		return (spooler_task.job().order_queue() != null);
	} // spooler_process

	@Override
	public void spooler_exit() {
		@SuppressWarnings("unused")//$NON-NLS-1$
		final String conMethodName = conClassName + "::spooler_exit"; //$NON-NLS-1$
		super.spooler_exit();
	}

	private void doProcessing() throws Exception {
		@SuppressWarnings("unused")//$NON-NLS-1$
		final String conMethodName = conClassName + "::doProcessing"; //$NON-NLS-1$
		JobSchedulerCheckRunHistory objR = new JobSchedulerCheckRunHistory();
		JobSchedulerCheckRunHistoryOptions objO = objR.Options();
		objO.CurrentNodeName(getCurrentNodeName());

		objO.setAllOptions(getSchedulerParameterAsProperties(getParameters()));
//		objO.CheckMandatory();
		objR.setJSJobUtilites(this);
		objR.setJSCommands(this);
		objR.Execute();
	} // doProcessing
}
