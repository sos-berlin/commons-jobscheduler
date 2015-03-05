package sos.scheduler.job;

import java.util.HashMap;

import org.apache.log4j.Logger;

import sos.net.ssh.SOSSSHJob2;
import sos.net.ssh.SOSSSHJobOptions;

import com.sos.JSHelper.Exceptions.JobSchedulerException;

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
 * \see
 *
 * \code
 * .... code goes here ...
 * \endcode
 *
 * \author Klaus Buettner - http://www.sos-berlin.com
* @version $Id: SOSSSHJob2JSAdapter.java 20725 2013-07-18 18:23:05Z kb $1.1.0.20100506
 *
 */

public class SOSSSHJob2JSBaseAdapter extends JobSchedulerJobAdapter {

	private final String conClassName = this.getClass().getSimpleName();
	@SuppressWarnings("hiding")
	protected final Logger logger = Logger.getLogger(this.getClass());

	@SuppressWarnings("unused")
	private final String	conSVNVersion	= "$Id: SOSSSHJob2JSAdapter.java 20725 2013-07-18 18:23:05Z kb $";

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
		}
		catch (Exception e) {
			logger.fatal(StackTrace2String(e));
			throw new JobSchedulerException(e);
		}
		finally {
		} // finally

		return signalSuccess();

	} // spooler_process

	@Override
	public void spooler_exit() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::spooler_exit";

		super.spooler_exit();
	}

//	private void doProcessing() throws Exception {
//		@SuppressWarnings("unused")
//		final String conMethodName = conClassName + "::doProcessing";
//
//		SOSSSHJob2 objR = new SOSSSHJob2();
//		SOSSSHJobOptions objO = objR.Options();
//		objO.CurrentNodeName(this.getCurrentNodeName());
//		//		objO.setAllOptions(getSchedulerParameterAsProperties(getJobOrOrderParameters()));
//		HashMap<String, String> hsmParameters1 = getSchedulerParameterAsProperties(getJobOrOrderParameters());
//		// see http://www.sos-berlin.com/otrs/index.pl?Action=AgentTicketZoom&TicketID=267&ArticleID=1275
//		objO.setAllOptions(objO.DeletePrefix(hsmParameters1, "ssh_"));
//
//		objR.setJSJobUtilites(this);
//		// Allow <script> tag of job as command parameter
//		// http://www.sos-berlin.com/jira/browse/JITL-48
//		if (objO.commandSpecified() == false) {
//			setJobScript(objO.command_script);
//		}
//
//		objO.CheckMandatory();
//		objR.Execute();
//	} // doProcessing

	private void doInitialize() throws Exception {
	} // doInitialize

} // SOSSSHJob2JSAdapter
