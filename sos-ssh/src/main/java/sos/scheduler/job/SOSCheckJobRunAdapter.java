package sos.scheduler.job;

import sos.net.ssh.SOSSSHJob2;
import sos.net.ssh.SOSSSHJobOptions;

/**
 * \class SOSSSHJob2JSAdapter 
 * 
 * \brief 
 * AdapterClass of SOSSSHJob2JSAdapter for the SOSJobScheduler
 * 
 * Start a Script, a command or a script-file using SSH on a remote server.
 * 
 * This Class SOSSSHJob2JSAdapter works as an adapter-class between the SOS
 * JobScheduler and the worker-class SOSSSHJob2.
 * 
 * *** get by Name ***
 * 
 * \see
 * 
 * \code 
 * .... code goes here ... 
 * \endcode
 * 
 * \author Klaus Buettner - http://www.sos-berlin.com 
* @version $Id: SOSCheckJobRunAdapter.java 15153 2011-09-14 11:59:34Z kb $1.1.0.20100506
 * 
 * This Source-Code was created by JETTemplate
 * SOSJobSchedulerAdapterClass.javajet, Version 1.0 from 2009-12-26, written by
 * kb
 */

public class SOSCheckJobRunAdapter extends JobSchedulerJobAdapter {

	private final String	conClassName	= "SOSSSHJob2JSAdapter";

	public void init() throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::init";

		doInitialize();
	}

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
			logger.error(StackTrace2String(e));
			return false;
		}
		finally {
		} // finally
		
		return spooler_task.job().order_queue() != null;

	} // spooler_process

	@Override
	public void spooler_exit() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::spooler_exit";
//		System.out.println("--> super.exit()");

		super.spooler_exit();
	}

	private void doProcessing() throws Exception {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::doProcessing";

		SOSSSHJob2 objR = new SOSSSHJob2();
		SOSSSHJobOptions objO = objR.Options();
		objO.setAllOptions(getSchedulerParameterAsProperties(getJobOrOrderParameters()));
		objR.setJSJobUtilites(this);
		objO.CheckMandatory();
		objR.Execute();
	} // doProcessing

	private void doInitialize() throws Exception {
	} // doInitialize


} // SOSSSHJob2JSAdapter
