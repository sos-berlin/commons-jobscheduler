package sos.scheduler.file;

import org.apache.log4j.Logger;

import sos.scheduler.job.JobSchedulerJobAdapter;

import com.sos.i18n.annotation.I18NResourceBundle;

/**
 * \class 		JobSchedulerFolderTreeJSAdapterClass - JobScheduler Adapter for "check wether a file exist"
 *
 * \brief AdapterClass of JobSchedulerFolderTree for the SOSJobScheduler
 *
 * This Class JobSchedulerFolderTreeJSAdapterClass works as an adapter-class between the SOS
 * JobScheduler and the worker-class JobSchedulerFolderTree.
 *

 *
 * see \see J:\E\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc\JobSchedulerFolderTree.xml for more details.
 *
 * \verbatim ;
 * mechanicaly created by C:\Users\KB\eclipse\sos.scheduler.xsl\JSJobDoc2JSAdapterClass.xsl from http://www.sos-berlin.com at 20110805104800 
 * \endverbatim
 */
@I18NResourceBundle(baseName = "com.sos.scheduler.messages", defaultLocale = "en")
public class JobSchedulerFolderTreeJSAdapterClass extends JobSchedulerJobAdapter {
	private final String	conClassName	= "JobSchedulerFolderTreeJSAdapterClass";
	@SuppressWarnings("unused")
	private static Logger	logger			= Logger.getLogger(JobSchedulerFolderTreeJSAdapterClass.class);
	@SuppressWarnings("unused")
	private final String	conSVNVersion	= "$Id$";

	public void init() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::init";
		doInitialize();
	}

	private void doInitialize() {
	} // doInitialize

	@Override
	public boolean spooler_init() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::spooler_init";
		return super.spooler_init();
	}

	@Override
	public boolean spooler_process() throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::spooler_process";

		try {
			super.spooler_process();
			doProcessing();
		}
		catch (Exception e) {
			throw e;
		}
		finally {
		} // finally
			// return value for classic and order driven processing
			// TODO create method in base-class for this functionality
			// return (spooler_task.job().order_queue() != null);
		return (signalSuccess());

	} // spooler_process

	@Override
	public void spooler_exit() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::spooler_exit";
		super.spooler_exit();
	}

	private void doProcessing() throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::doProcessing";

		JobSchedulerFolderTree objR = new JobSchedulerFolderTree();
		JobSchedulerFolderTreeOptions objO = objR.Options();
		objO.CurrentNodeName(this.getCurrentNodeName());

		objO.setAllOptions(getSchedulerParameterAsProperties(getParameters()));
		objO.CheckMandatory();
		objR.setJSJobUtilites(this);
		objR.Execute();
	} // doProcessing

}
