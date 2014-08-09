package sos.scheduler.job;

import org.apache.log4j.Logger;

import com.sos.scheduler.converter.graphviz.JSObjects2Graphviz;
import com.sos.scheduler.converter.graphviz.JSObjects2GraphvizOptions;

// Super-Class for JobScheduler Java-API-Jobs
/**
 * \class 		JSObjects2GraphvizJSAdapterClass - JobScheduler Adapter for "JSObjects2Graphviz"
 *
 * \brief AdapterClass of JSObjects2Graphviz for the SOSJobScheduler
 *
 * This Class JSObjects2GraphvizJSAdapterClass works as an adapter-class between the SOS
 * JobScheduler and the worker-class JSObjects2Graphviz.
 *

 *
 * see \see C:\Users\KB\AppData\Local\Temp\scheduler_editor-2781494595910967227.html for more details.
 *
 * \verbatim ;
 * mechanicaly created by C:\Users\KB\sos-berlin.com\jobscheduler\scheduler\config\JOETemplates\java\xsl\JSJobDoc2JSAdapterClass.xsl from http://www.sos-berlin.com at 20121108150924
 * \endverbatim
 */
public class JSObjects2GraphvizJSAdapterClass extends JobSchedulerJobAdapter {
	private final String	conClassName	= "JSObjects2GraphvizJSAdapterClass";						//$NON-NLS-1$
	private final  Logger	logger			= Logger.getLogger(JSObjects2GraphvizJSAdapterClass.class);
	private final String				conSVNVersion		= "$Id: SOSSSHJob2JSAdapter.java 18220 2012-10-18 07:46:10Z kb $";

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
	public boolean spooler_process()  { 
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::spooler_process"; //$NON-NLS-1$

		try {
			super.spooler_process();
			doProcessing();
		}
		catch (Exception e) {
			return signalFailure();
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

		logger.info(conSVNVersion);
		
		JSObjects2Graphviz objR = new JSObjects2Graphviz();
		JSObjects2GraphvizOptions objO = objR.Options();
		objO.setAllOptions(getSchedulerParameterAsProperties(getJobOrOrderParameters()));
		objO.CurrentNodeName(this.getCurrentNodeName());
		objO.CheckMandatory();
		objR.setJSJobUtilites(this);
		objR.Execute();
	} // doProcessing

}
