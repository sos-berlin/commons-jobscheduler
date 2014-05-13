

package sos.scheduler.CheckRunHistory;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.SOSOptionHostName;
import com.sos.JSHelper.Options.SOSOptionPortNumber;
import com.sos.i18n.annotation.I18NMessage;
import com.sos.i18n.annotation.I18NMessages;
import com.sos.i18n.annotation.I18NResourceBundle;

/**
 * \class 		JobSchedulerCheckRunHistoryOptions - Check the last job run
 *
 * \brief 
 * An Options as a container for the Options super class. 
 * The Option class will hold all the things, which would be otherwise overwritten at a re-creation
 * of the super-class.
 *
 *

 *
 * see \see J:\E\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc\JobSchedulerCheckRunHistory.xml for (more) details.
 * 
 * \verbatim ;
 * mechanicaly created by JobDocu2OptionsClass.xslt from http://www.sos-berlin.com at 20110224143553 
 * \endverbatim
 */
@I18NResourceBundle(baseName = "com_sos_scheduler_messages", defaultLocale = "en")
@JSOptionClass(name = "JobSchedulerCheckRunHistoryOptions", description = "Check the last job run")
public class JobSchedulerCheckRunHistoryOptions extends JobSchedulerCheckRunHistoryOptionsSuperClass {
	/**
	 * 
	 */
	private static final long	serialVersionUID	= -3625891732295134070L;
	private final String					conClassName						= "JobSchedulerCheckRunHistoryOptions";  //$NON-NLS-1$
	@SuppressWarnings("unused")
	private static Logger		logger			= Logger.getLogger(JobSchedulerCheckRunHistoryOptions.class);

    /**
    * constructors
    */
    
	public JobSchedulerCheckRunHistoryOptions() {
	} // public JobSchedulerCheckRunHistoryOptions

	public JobSchedulerCheckRunHistoryOptions(JSListener pobjListener) {
		this();
		this.registerMessageListener(pobjListener);
	} // public JobSchedulerCheckRunHistoryOptions

		//

	public JobSchedulerCheckRunHistoryOptions (HashMap <String, String> JSSettings) throws Exception {
		super(JSSettings);
	} // public JobSchedulerCheckRunHistoryOptions (HashMap JSSettings)
/**
 * \brief CheckMandatory - prüft alle Muss-Optionen auf Werte
 *
 * \details
 * @throws Exception
 *
 * @throws Exception
 * - wird ausgelöst, wenn eine mandatory-Option keinen Wert hat
 */
		@Override  // JobSchedulerCheckRunHistoryOptionsSuperClass
	public void CheckMandatory() {
		try {
			super.CheckMandatory();
//			if( this.JobName.IsEmpty() && (this.JobChainName.IsEmpty() || this.OrderId.IsEmpty()) ) {
//				this.SignalError(String.format("Either the option JobName or the options JobChainName and OrderId are mandatory, must be not null.%n"));
//			}
		}
		catch (Exception e) {
			throw new JSExceptionMandatoryOptionMissing(e.toString());
		}
	} // public void CheckMandatory ()

		@I18NMessages(value = { @I18NMessage("The Job Scheduler communication port"), //
				@I18NMessage(value = "The Job Scheduler communication port", locale = "en_UK", //
				explanation = "The Job Scheduler communication port" // 
				), //
				@I18NMessage(value = "JobScheduler TCP-Port Nummer", locale = "de", //
				explanation = "Mit diesem Port kommuniziert der JobScheduler über TCP" // 
				) //
		}, msgnum = "JSJ_CRH_0010", msgurl = "msgurl")
		/*!
		 * \var JS_CRH_0010
		 * \brief The Job Scheduler communication port
		 */
		public static final String JSJ_CRH_0010 = "JSJ_CRH_0010";

		/**
		 * \var PortNumber : The scheduler communication port.
		 * The scheduler communication port.
		 *
		 */ 
		@JSOptionDefinition(name = "scheduler_port", description = "The Job Scheduler communication port", key = "scheduler_port", type = "SOSOptionPortNumber", mandatory = false)
		public SOSOptionPortNumber	scheduler_port		= new SOSOptionPortNumber( // ...
															this, // ....
															conClassName + ".scheduler_port", // ...
															//getMsg(JSJ_CRH_0010), // ...
															"The Job Scheduler communication port", // ...
															"0", // ...
															"4444", // ...
															true);

		/**
		 * \brief getPortNumber : The Job Scheduler communication port.
		 * 
		 * \details
		 * The Job Scheduler communication port.
		 *
		 * \return The Job Scheduler communication port.
		 *
		 */
		public SOSOptionPortNumber getPortNumber() {
			return scheduler_port;
		}

		/**
		 * \brief setPortNumber : The Job Scheduler communication port.
		 * 
		 * \details
		 * The Job Scheduler communication port.
		 *
		 * @param SOSOptionPortNumber : The Job Scheduler communication port.
		 */
		public void setPortNumber(SOSOptionPortNumber p_PortNumber) {
			this.scheduler_port = p_PortNumber;
		}
		public SOSOptionPortNumber			SchedulerTcpPortNumber		= (SOSOptionPortNumber) scheduler_port.SetAlias(conClassName + ".SchedulerTcpPortNumber");
		public SOSOptionPortNumber			SchedulerPort		= (SOSOptionPortNumber) scheduler_port.SetAlias(conClassName + ".SchedulerPort");
		public SOSOptionPortNumber			PortNumber		= (SOSOptionPortNumber) scheduler_port.SetAlias(conClassName + ".PortNumber");
		
		/**
		 * \var HostName : The name of the Job Scheduler host.
		 * The name of the Job Scheduler host.
		 *
		 */ 
		@JSOptionDefinition(name = "SchedulerHostName", description = "The name of the Job Scheduler host", key = "SchedulerHostName", type = "SOSOptionHostName", mandatory = false)
		public SOSOptionHostName	SchedulerHostName	= new SOSOptionHostName( // ...
														  this, // ....
														  conClassName + ".SchedulerHostName", // ...
														  //getMsg(JSJ_CRH_0020), // ...
														  "The name of the Job Scheduler host", // ...
														  "", // .....
														  "localhost", // ...
														  true);
		
		@I18NMessages(value = { @I18NMessage("The name of the Job Scheduler host"), //
				@I18NMessage(value = "The name of the Job Scheduler host", locale = "en_UK", //
				explanation = "The name of the Job Scheduler host" // 
				), //
				@I18NMessage(value = "Der Name oder die IP des JobScheduler Servers", locale = "de", //
				explanation = "The name of the Job Scheduler host" // 
				) //
		}, msgnum = "JSJ_CRH_0020", msgurl = "msgurl")
		/*!
		 * \var MsgKey
		 * \brief The name of the Job Scheduler host
		 */
		
		public static final String JSJ_CRH_0020 = "JSJ_CRH_0020";
		/**
		 * \brief getHostName : The name of the Job Scheduler host.
		 * 
		 * \details
		 * The name of the Job Scheduler host.
		 *
		 * \return The name of the Job Scheduler host.
		 *
		 */
		public SOSOptionHostName getSchedulerHostName() {
			return SchedulerHostName;
		}

		/**
		 * \brief setHostName : The name of the Job Scheduler host.
		 * 
		 * \details
		 * The name of the Job Scheduler host.
		 *
		 * @param SOSOptionHostName : The name of the Job Scheduler host.
		 */
		public void setHostName(SOSOptionHostName p_SchedulerHostName) {
			this.SchedulerHostName = p_SchedulerHostName;
		}

}

