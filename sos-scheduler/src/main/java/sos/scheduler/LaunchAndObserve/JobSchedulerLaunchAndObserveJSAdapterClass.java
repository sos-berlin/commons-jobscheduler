package sos.scheduler.LaunchAndObserve;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.sos.JSHelper.interfaces.ISOSSmtpMailOptions;

import sos.net.mail.options.SOSSmtpMailOptions;
import sos.scheduler.job.JobSchedulerJobAdapter;
import sos.spooler.Mail;
import sos.spooler.Spooler;

/**
 * \class 		JobSchedulerLaunchAndObserveJSAdapterClass - JobScheduler Adapter for "Launch and observe any given job or job chain"
 *
 * \brief AdapterClass of JobSchedulerLaunchAndObserve for the SOSJobScheduler
 *
 * This Class JobSchedulerLaunchAndObserveJSAdapterClass works as an adapter-class between the SOS
 * JobScheduler and the worker-class JobSchedulerLaunchAndObserve.
 *

 *
 * see \see J:\E\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc\JobSchedulerLaunchAndObserve.xml for more details.
 *
 * \verbatim ;
 * mechanicaly created by C:\Users\KB\eclipse\xsl\JSJobDoc2JSAdapterClass.xsl from http://www.sos-berlin.com at 20111124184800 
 * \endverbatim
 */
public class JobSchedulerLaunchAndObserveJSAdapterClass extends JobSchedulerJobAdapter {
	private final String	conClassName	= "JobSchedulerLaunchAndObserveJSAdapterClass";						//$NON-NLS-1$
	private static Logger	logger			= Logger.getLogger(JobSchedulerLaunchAndObserveJSAdapterClass.class);

	@SuppressWarnings("unused")
	private final String				conSVNVersion		= "$Id: JobSchedulerLaunchAndObserveJSAdapterClass.java 15749 2011-11-22 16:04:10Z kb $";

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

		JobSchedulerLaunchAndObserve objR = new JobSchedulerLaunchAndObserve();
		JobSchedulerLaunchAndObserveOptions objO = objR.Options();
		objO.CurrentNodeName(this.getCurrentNodeName());
		HashMap <String, String> objH = getSchedulerParameterAsProperties(getParameters());
		objO.setAllOptions(objH);
		
		Object objSp = getSpoolerObject();
		Spooler objSpooler = (Spooler) objSp;
		Mail objMail = objSpooler.log().mail();
		ISOSSmtpMailOptions	objMailOnRestartOptions	= objO.getMailOnRestartOptions();
		ISOSSmtpMailOptions	objMailOnKillOptions = objO.getMailOnRestartOptions();
		
		if(objO.getscheduler_host().IsEmpty()) {
			objH.put(objO.getscheduler_host().getShortKey(), objSpooler.hostname());
		}
		if(objO.getscheduler_port().value() == 0) {
			objH.put(objO.getscheduler_port().getShortKey(), ""+objSpooler.tcp_port());
		}
		
		if(objMailOnRestartOptions.gethost().IsEmpty()) {
			objH.put(objMailOnRestartOptions.gethost().getShortKey(), objMail.smtp());
		}
		if(objMailOnRestartOptions.getfrom().IsEmpty()) {
			objH.put(objMailOnRestartOptions.getfrom().getShortKey(), objMail.from());
		}
		if(objMailOnRestartOptions.getqueue_directory().IsEmpty()) {
			objH.put(objMailOnRestartOptions.getqueue_directory().getShortKey(), objMail.queue_dir());
		}
		
		if(objMailOnKillOptions.gethost().IsEmpty()) {
			objH.put(objMailOnRestartOptions.gethost().getShortKey(), objMail.smtp());
		}
		if(objMailOnKillOptions.getfrom().IsEmpty()) {
			objH.put(objMailOnRestartOptions.getfrom().getShortKey(), objMail.from());
		}
		if(objMailOnKillOptions.getqueue_directory().IsEmpty()) {
			objH.put(objMailOnRestartOptions.getqueue_directory().getShortKey(), objMail.queue_dir());
		}
		
		objO.setAllOptions(objH);
		SOSSmtpMailOptions objM = (SOSSmtpMailOptions) objO.getMailOnRestartOptions();
		objM.setAllOptions(objH);
		// JITL-145: commented to prevent logging of passwords, toString-Method of JSOptionClass calls getAllOptionsAsString 
		// which itself aggregates a String with all Options without checking, to log that String can result in clear passwords being logged
//		logger.debug(objM.toString());
		
		SOSSmtpMailOptions objK = (SOSSmtpMailOptions) objO.getMailOnKillOptions();
		objK.setAllOptions(objH);
		// JITL-145: commented to prevent logging of passwords, toString-Method of JSOptionClass calls getAllOptionsAsString 
		// which itself aggregates a String with all Options without checking, to log that String can result in clear passwords being logged
//		logger.debug(objK.toString());

		
		objO.CheckMandatory();
		objR.setJSJobUtilites(this);
		objR.Execute();
	} // doProcessing

}
