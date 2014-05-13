package sos.scheduler.LaunchAndObserve;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSJobName;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.JSOrderId;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionHostName;
import com.sos.JSHelper.Options.SOSOptionInteger;
import com.sos.JSHelper.Options.SOSOptionPortNumber;
import com.sos.JSHelper.Options.SOSOptionRegExp;
import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.JSHelper.Options.SOSOptionTime;

/**
 * \class 		JobSchedulerLaunchAndObserveOptionsSuperClass - Launch and observe any given job or job chain
 *
 * \brief 
 * An Options-Super-Class with all Options. This Class will be extended by the "real" Options-class (\see JobSchedulerLaunchAndObserveOptions.
 * The "real" Option class will hold all the things, which are normaly overwritten at a new generation
 * of the super-class.
 *
 *

 *
 * see \see J:\E\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc\JobSchedulerLaunchAndObserve.xml for (more) details.
 * 
 * \verbatim ;
 * mechanicaly created by  from http://www.sos-berlin.com at 20111124184608 
 * \endverbatim
 * \section OptionsTable Tabelle der vorhandenen Optionen
 * 
 * Tabelle mit allen Optionen
 * 
 * MethodName
 * Title
 * Setting
 * Description
 * IsMandatory
 * DataType
 * InitialValue
 * TestValue
 * 
 * 
 *
 * \section TestData Eine Hilfe zum Erzeugen einer HashMap mit Testdaten
 *
 * Die folgenden Methode kann verwendet werden, um für einen Test eine HashMap
 * mit sinnvollen Werten für die einzelnen Optionen zu erzeugen.
 *
 * \verbatim
 private HashMap <String, String> SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM) {
	pobjHM.put ("		JobSchedulerLaunchAndObserveOptionsSuperClass.auth_file", "test");  // This parameter specifies the path and name of a user's pr
		return pobjHM;
  }  //  private void SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM)
 * \endverbatim
 */
@JSOptionClass(name = "JobSchedulerLaunchAndObserveOptionsSuperClass", description = "JobSchedulerLaunchAndObserveOptionsSuperClass")
public class JobSchedulerLaunchAndObserveOptionsSuperClass extends JSOptionsClass  {
	/**
	 * 
	 */
	private static final long	serialVersionUID	= -4070202283420425512L;
	private final String	conClassName		= "JobSchedulerLaunchAndObserveOptionsSuperClass";
	@SuppressWarnings("unused")
	private static Logger	logger				= Logger.getLogger(JobSchedulerLaunchAndObserveOptionsSuperClass.class);
	@SuppressWarnings("unused")
	private final String							conSVNVersion	= "$Id: JobSchedulerJobAdapter.java 15749 2011-11-22 16:04:10Z kb $";

	/**
	 * \var check_for_regexp : Text pattern to search for in log file
	 * 
	 *
	 */
	@JSOptionDefinition(name = "check_for_regexp", description = "Text pattern to search for in log file", key = "check_for_regexp", type = "SOSOptionRegExp", mandatory = false)
	public SOSOptionRegExp	check_for_regexp	= new SOSOptionRegExp(this, conClassName + ".check_for_regexp", // HashMap-Key
														"Text pattern to search for in log file", // Titel
														"true", // InitValue
														"true", // DefaultValue
														false // isMandatory
												);

	/**
	 * \brief getcheck_for_regexp : Text pattern to search for in log file
	 * 
	 * \details
	 * 
	 *
	 * \return Text pattern to search for in log file
	 *
	 */
	public SOSOptionRegExp getcheck_for_regexp() {
		return check_for_regexp;
	}

	/**
	 * \brief setcheck_for_regexp : Text pattern to search for in log file
	 * 
	 * \details
	 * 
	 *
	 * @param check_for_regexp : Text pattern to search for in log file
	 */
	public void setcheck_for_regexp(SOSOptionRegExp p_check_for_regexp) {
		this.check_for_regexp = p_check_for_regexp;
	}

	/**
	 * \var check_inactivity : Check Log File for Progress
	 * 
	 *
	 */
	@JSOptionDefinition(name = "check_inactivity", description = "Check job for inactivity", key = "check_inactivity", type = "SOSOptionBoolean", mandatory = false)
	public SOSOptionBoolean	check_inactivity	= new SOSOptionBoolean(this, conClassName + ".check_inactivity", // HashMap-Key
													"Check job for inactivity", // Titel
													"true", // InitValue
													"true", // DefaultValue
													false // isMandatory
											);

	/**
	 * \brief getcheck_log_file : Check Log File for Progress
	 * 
	 * \details
	 * 
	 *
	 * \return Check Log File for Progress
	 *
	 */
	public SOSOptionBoolean getcheck_inactivity() {
		return check_inactivity;
	}

	/**
	 * \brief setcheck_log_file : Check Log File for Progress
	 * 
	 * \details
	 * 
	 *
	 * @param check_inactivity : Check Log File for Progress
	 */
	public void setcheck_inactivity(SOSOptionBoolean p_check_log_file) {
		this.check_inactivity = p_check_log_file;
	}

	/**
	 * \var check_interval : This parameter specifies the interval in seconds
	 * This parameter specifies the interval in seconds between two file transfer trials, if repeated transfer of files has been configured using the check_retry parameter.
	 *
	 */
	@JSOptionDefinition(name = "check_interval", description = "This parameter specifies the interval in seconds", key = "check_interval", type = "SOSOptionInteger", mandatory = false)
	public SOSOptionInteger	check_interval	= new SOSOptionInteger(this, conClassName + ".check_interval", // HashMap-Key
													"This parameter specifies the interval in seconds", // Titel
													"60", // InitValue
													"60", // DefaultValue
													false // isMandatory
											);

	/**
	 * \brief getcheck_interval : This parameter specifies the interval in seconds
	 * 
	 * \details
	 * This parameter specifies the interval in seconds between two file transfer trials, if repeated transfer of files has been configured using the check_retry parameter.
	 *
	 * \return This parameter specifies the interval in seconds
	 *
	 */
	public SOSOptionInteger getcheck_interval() {
		return check_interval;
	}

	/**
	 * \brief setcheck_interval : This parameter specifies the interval in seconds
	 * 
	 * \details
	 * This parameter specifies the interval in seconds between two file transfer trials, if repeated transfer of files has been configured using the check_retry parameter.
	 *
	 * @param check_interval : This parameter specifies the interval in seconds
	 */
	public void setcheck_interval(SOSOptionInteger p_check_interval) {
		this.check_interval = p_check_interval;
	}

	/**
	 * \var job_name : The name of a job.
	 * The name of a job.
	 *
	 */
	@JSOptionDefinition(name = "job_name", description = "The name of a job.", key = "job_name", type = "JSJobName", mandatory = true)
	public JSJobName	job_name	= new JSJobName(this, conClassName + ".job_name", // HashMap-Key
											"The name of a job.", // Titel
											" ", // InitValue
											" ", // DefaultValue
											true // isMandatory
									);

	/**
	 * \brief getjob_name : The name of a job.
	 * 
	 * \details
	 * The name of a job.
	 *
	 * \return The name of a job.
	 *
	 */
	public JSJobName getjob_name() {
		return job_name;
	}

	/**
	 * \brief setjob_name : The name of a job.
	 * 
	 * \details
	 * The name of a job.
	 *
	 * @param job_name : The name of a job.
	 */
	public void setjob_name(JSJobName p_job_name) {
		this.job_name = p_job_name;
	}

	/**
	 * \var kill_job : kill job due to Inactivity
	 * 
	 *
	 */
	@JSOptionDefinition(name = "kill_job", description = "kill job due to Inactivity", key = "kill_job", type = "SOSOptionBoolean", mandatory = false)
	public SOSOptionBoolean	kill_job	= new SOSOptionBoolean(this, conClassName + ".kill_job", // HashMap-Key
												"kill job due to Inactivity", // Titel
												"true", // InitValue
												"true", // DefaultValue
												false // isMandatory
										);

	/**
	 * \brief getkill_job : kill job due to Inactivity
	 * 
	 * \details
	 * 
	 *
	 * \return kill job due to Inactivity
	 *
	 */
	public SOSOptionBoolean getkill_job() {
		return kill_job;
	}

	/**
	 * \brief setkill_job : kill job due to Inactivity
	 * 
	 * \details
	 * 
	 *
	 * @param kill_job : kill job due to Inactivity
	 */
	public void setkill_job(SOSOptionBoolean p_kill_job) {
		this.kill_job = p_kill_job;
	}

	/**
	 * \var lifetime : Lifetime of the Job
	 * 
	 *
	 */
	@JSOptionDefinition(name = "lifetime", description = "Lifetime of the Job", key = "lifetime", type = "SOSOptionTime", mandatory = false)
	public SOSOptionTime	lifetime	= new SOSOptionTime(this, conClassName + ".lifetime", // HashMap-Key
												"Lifetime of the Job", // Titel
												"0", // InitValue
												"0", // DefaultValue
												false // isMandatory
										);

	/**
	 * \brief getlifetime : Lifetime of the Job
	 * 
	 * \details
	 * 
	 *
	 * \return Lifetime of the Job
	 *
	 */
	public SOSOptionTime getlifetime() {
		return lifetime;
	}

	/**
	 * \brief setlifetime : Lifetime of the Job
	 * 
	 * \details
	 * 
	 *
	 * @param lifetime : Lifetime of the Job
	 */
	public void setlifetime(SOSOptionTime p_lifetime) {
		this.lifetime = p_lifetime;
	}

	/**
	 * \var mail_on_nonactivity : send eMail due to Inactivity
	 * 
	 *
	 */
	@JSOptionDefinition(name = "mail_on_nonactivity", description = "send eMail due to Inactivity", key = "mail_on_nonactivity", type = "SOSOptionBoolean", mandatory = false)
	public SOSOptionBoolean	mail_on_nonactivity	= new SOSOptionBoolean(this, conClassName + ".mail_on_nonactivity", // HashMap-Key
														"send eMail due to Inactivity", // Titel
														"true", // InitValue
														"true", // DefaultValue
														false // isMandatory
												);

	/**
	 * \brief getmail_on_nonactivity : send eMail due to Inactivity
	 * 
	 * \details
	 * 
	 *
	 * \return send eMail due to Inactivity
	 *
	 */
	public SOSOptionBoolean getmail_on_nonactivity() {
		return mail_on_nonactivity;
	}

	/**
	 * \brief setmail_on_nonactivity : send eMail due to Inactivity
	 * 
	 * \details
	 * 
	 *
	 * @param mail_on_nonactivity : send eMail due to Inactivity
	 */
	public void setmail_on_nonactivity(SOSOptionBoolean p_mail_on_nonactivity) {
		this.mail_on_nonactivity = p_mail_on_nonactivity;
	}

	/**
	 * \var mail_on_restart : send eMail with restart of job
	 * 
	 *
	 */
	@JSOptionDefinition(name = "mail_on_restart", description = "send eMail with restart of job", key = "mail_on_restart", type = "SOSOptionBoolean", mandatory = false)
	public SOSOptionBoolean	mail_on_restart	= new SOSOptionBoolean(this, conClassName + ".mail_on_restart", // HashMap-Key
													"send eMail with restart of job", // Titel
													"true", // InitValue
													"true", // DefaultValue
													false // isMandatory
											);

	/**
	 * \brief getmail_on_restart : send eMail with restart of job
	 * 
	 * \details
	 * 
	 *
	 * \return send eMail with restart of job
	 *
	 */
	public SOSOptionBoolean getmail_on_restart() {
		return mail_on_restart;
	}

	/**
	 * \brief setmail_on_restart : send eMail with restart of job
	 * 
	 * \details
	 * 
	 *
	 * @param mail_on_restart : send eMail with restart of job
	 */
	public void setmail_on_restart(SOSOptionBoolean p_mail_on_restart) {
		this.mail_on_restart = p_mail_on_restart;
	}

	/**
	 * \var order_jobchain_name : The name of the jobchain which belongs to the order The name of the jobch
	 * 
	 *
	 */
	@JSOptionDefinition(name = "order_jobchain_name", description = "The name of the jobchain which belongs to the order The name of the jobch", key = "order_jobchain_name", type = "SOSOptionString", mandatory = false)
	public SOSOptionString	order_jobchain_name	= new SOSOptionString(this, conClassName + ".order_jobchain_name", // HashMap-Key
														"The name of the jobchain which belongs to the order The name of the jobch", // Titel
														" ", // InitValue
														" ", // DefaultValue
														false // isMandatory
												);

	/**
	 * \brief getorder_jobchain_name : The name of the jobchain which belongs to the order The name of the jobch
	 * 
	 * \details
	 * 
	 *
	 * \return The name of the jobchain which belongs to the order The name of the jobch
	 *
	 */
	public SOSOptionString getorder_jobchain_name() {
		return order_jobchain_name;
	}

	/**
	 * \brief setorder_jobchain_name : The name of the jobchain which belongs to the order The name of the jobch
	 * 
	 * \details
	 * 
	 *
	 * @param order_jobchain_name : The name of the jobchain which belongs to the order The name of the jobch
	 */
	public void setorder_jobchain_name(SOSOptionString p_order_jobchain_name) {
		this.order_jobchain_name = p_order_jobchain_name;
	}

	/**
	 * \var OrderId : The name or the identification of an order.
	 * The name or the identification of an order.
	 *
	 */
	@JSOptionDefinition(name = "OrderId", description = "The name or the identification of an order.", key = "OrderId", type = "JSOrderId", mandatory = false)
	public JSOrderId	OrderId	= new JSOrderId(this, conClassName + ".OrderId", // HashMap-Key
										"The name or the identification of an order.", // Titel
										" ", // InitValue
										" ", // DefaultValue
										false // isMandatory
								);

	/**
	 * \brief getOrderId : The name or the identification of an order.
	 * 
	 * \details
	 * The name or the identification of an order.
	 *
	 * \return The name or the identification of an order.
	 *
	 */
	public JSOrderId getOrderId() {
		return OrderId;
	}

	/**
	 * \brief setOrderId : The name or the identification of an order.
	 * 
	 * \details
	 * The name or the identification of an order.
	 *
	 * @param OrderId : The name or the identification of an order.
	 */
	public void setOrderId(JSOrderId p_OrderId) {
		this.OrderId = p_OrderId;
	}

	/**
	 * \var restart : Restart the observed Job This value o
	 * This value of this parameter defines wether the job which has to be observed, should be restarted during the observation period, in case the job has ended.
	 *
	 */
	@JSOptionDefinition(name = "restart", description = "Restart the observed Job This value o", key = "restart", type = "SOSOptionBoolean", mandatory = false)
	public SOSOptionBoolean	restart	= new SOSOptionBoolean(this, conClassName + ".restart", // HashMap-Key
											"Restart the observed Job This value o", // Titel
											"true", // InitValue
											"true", // DefaultValue
											false // isMandatory
									);

	/**
	 * \brief getrestart : Restart the observed Job This value o
	 * 
	 * \details
	 * This value of this parameter defines wether the job which has to be observed, should be restarted during the observation period, in case the job has ended.
	 *
	 * \return Restart the observed Job This value o
	 *
	 */
	public SOSOptionBoolean getrestart() {
		return restart;
	}

	/**
	 * \brief setrestart : Restart the observed Job This value o
	 * 
	 * \details
	 * This value of this parameter defines wether the job which has to be observed, should be restarted during the observation period, in case the job has ended.
	 *
	 * @param restart : Restart the observed Job This value o
	 */
	public void setrestart(SOSOptionBoolean p_restart) {
		this.restart = p_restart;
	}

	/**
	 * \var scheduler_host : This parameter specifies the host name or IP addre
	 * This parameter specifies the host name or IP address of a server for which Job Scheduler is operated for Managed File Transfer. The contents of an optional history file (see parameter history), is added to a central database by Job Scheduler. This parameter causes the transfer of the history entries for the current transfer by UDP to Job Scheduler. Should Job Scheduler not be accessible then no errors are reported, instead, the contents of the history will automaticall be processed later on.
	 *
	 */
	@JSOptionDefinition(name = "scheduler_host", description = "This parameter specifies the host name or IP addre", key = "scheduler_host", type = "SOSOptionHostName", mandatory = false)
	public SOSOptionHostName	scheduler_host	= new SOSOptionHostName(this, conClassName + ".scheduler_host", // HashMap-Key
														"This parameter specifies the host name or IP addre", // Titel
														"", // InitValue
														"localhost", // DefaultValue
														true // isMandatory
												);

	/**
	 * \brief getscheduler_host : This parameter specifies the host name or IP addre
	 * 
	 * \details
	 * This parameter specifies the host name or IP address of a server for which Job Scheduler is operated for Managed File Transfer. The contents of an optional history file (see parameter history), is added to a central database by Job Scheduler. This parameter causes the transfer of the history entries for the current transfer by UDP to Job Scheduler. Should Job Scheduler not be accessible then no errors are reported, instead, the contents of the history will automaticall be processed later on.
	 *
	 * \return This parameter specifies the host name or IP addre
	 *
	 */
	public SOSOptionHostName getscheduler_host() {
		return scheduler_host;
	}

	/**
	 * \brief setscheduler_host : This parameter specifies the host name or IP addre
	 * 
	 * \details
	 * This parameter specifies the host name or IP address of a server for which Job Scheduler is operated for Managed File Transfer. The contents of an optional history file (see parameter history), is added to a central database by Job Scheduler. This parameter causes the transfer of the history entries for the current transfer by UDP to Job Scheduler. Should Job Scheduler not be accessible then no errors are reported, instead, the contents of the history will automaticall be processed later on.
	 *
	 * @param scheduler_host : This parameter specifies the host name or IP addre
	 */
	public void setscheduler_host(SOSOptionHostName p_scheduler_host) {
		this.scheduler_host = p_scheduler_host;
	}

	/**
	 * \var scheduler_port : The TCP-port for which a JobScheduler, see parameter sche
	 * The TCP-port for which a JobScheduler, see parameter scheduler_host.
	 *
	 */
	@JSOptionDefinition(name = "scheduler_port", description = "The TCP-port for which a JobScheduler, see parameter sche", key = "scheduler_port", type = "SOSOptionPortNumber", mandatory = false)
	public SOSOptionPortNumber	scheduler_port	= new SOSOptionPortNumber(this, conClassName + ".scheduler_port", // HashMap-Key
														"The TCP-port for which a JobScheduler, see parameter sche", // Titel
														"0", // InitValue
														"4444", // DefaultValue
														true // isMandatory
												);

	/**
	 * \brief getscheduler_port : The TCP-port for which a JobScheduler, see parameter sche
	 * 
	 * \details
	 * The TCP-port for which a JobScheduler, see parameter scheduler_host.
	 *
	 * \return The TCP-port for which a JobScheduler, see parameter sche
	 *
	 */
	public SOSOptionPortNumber getscheduler_port() {
		return scheduler_port;
	}

	/**
	 * \brief setscheduler_port : The TCP-port for which a JobScheduler, see parameter sche
	 * 
	 * \details
	 * The TCP-port for which a JobScheduler, see parameter scheduler_host.
	 *
	 * @param scheduler_port : The TCP-port for which a JobScheduler, see parameter sche
	 */
	public void setscheduler_port(SOSOptionPortNumber p_scheduler_port) {
		this.scheduler_port = p_scheduler_port;
	}

	public JobSchedulerLaunchAndObserveOptionsSuperClass() {
		objParentClass = this.getClass();
	} // public JobSchedulerLaunchAndObserveOptionsSuperClass

	public JobSchedulerLaunchAndObserveOptionsSuperClass(JSListener pobjListener) {
		this();
		this.registerMessageListener(pobjListener);
	} // public JobSchedulerLaunchAndObserveOptionsSuperClass

	//

	public JobSchedulerLaunchAndObserveOptionsSuperClass(HashMap<String, String> JSSettings) throws Exception {
		this();
		this.setAllOptions(JSSettings);
	} // public JobSchedulerLaunchAndObserveOptionsSuperClass (HashMap JSSettings)

	/**
	 * \brief getAllOptionsAsString - liefert die Werte und Beschreibung aller
	 * Optionen als String
	 *
	 * \details
	 * 
	 * \see toString 
	 * \see toOut
	 */
	private String getAllOptionsAsString() {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getAllOptionsAsString";
		String strT = conClassName + "\n";
		final StringBuffer strBuffer = new StringBuffer();
		// strT += IterateAllDataElementsByAnnotation(objParentClass, this,
		// JSOptionsClass.IterationTypes.toString, strBuffer);
		// strT += IterateAllDataElementsByAnnotation(objParentClass, this, 13,
		// strBuffer);
		strT += this.toString(); // fix
		//
		return strT;
	} // private String getAllOptionsAsString ()

	/**
	 * \brief setAllOptions - übernimmt die OptionenWerte aus der HashMap
	 *
	 * \details In der als Parameter anzugebenden HashMap sind Schlüssel (Name)
	 * und Wert der jeweiligen Option als Paar angegeben. Ein Beispiel für den
	 * Aufbau einer solchen HashMap findet sich in der Beschreibung dieser
	 * Klasse (\ref TestData "setJobSchedulerSSHJobOptions"). In dieser Routine
	 * werden die Schlüssel analysiert und, falls gefunden, werden die
	 * dazugehörigen Werte den Properties dieser Klasse zugewiesen.
	 *
	 * Nicht bekannte Schlüssel werden ignoriert.
	 *
	 * \see JSOptionsClass::getItem
	 *
	 * @param pobjJSSettings
	 * @throws Exception
	 */
	public void setAllOptions(HashMap<String, String> pobjJSSettings) {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setAllOptions";
		flgSetAllOptions = true;
		objSettings = pobjJSSettings;
		super.Settings(objSettings);
		super.setAllOptions(pobjJSSettings);
		flgSetAllOptions = false;
		
		try {
			setChildClasses(pobjJSSettings);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	} // public void setAllOptions (HashMap <String, String> JSSettings)

	public void setChildClasses(HashMap<String, String> pobjJSSettings) throws Exception {
	}


	/**
	 * \brief CheckMandatory - prüft alle Muss-Optionen auf Werte
	 *
	 * \details
	 * @throws Exception
	 *
	 * @throws Exception
	 * - wird ausgelöst, wenn eine mandatory-Option keinen Wert hat
	 */
	@Override
	public void CheckMandatory() throws JSExceptionMandatoryOptionMissing //
			, Exception {
		try {
			super.CheckMandatory();
		}
		catch (Exception e) {
			throw new JSExceptionMandatoryOptionMissing(e.toString());
		}
	} // public void CheckMandatory ()

	/**
	 *
	 * \brief CommandLineArgs - Übernehmen der Options/Settings aus der
	 * Kommandozeile
	 *
	 * \details Die in der Kommandozeile beim Starten der Applikation
	 * angegebenen Parameter werden hier in die HashMap übertragen und danach
	 * den Optionen als Wert zugewiesen.
	 *
	 * \return void
	 *
	 * @param pstrArgs
	 * @throws Exception
	 */
	@Override
	public void CommandLineArgs(String[] pstrArgs) {
		super.CommandLineArgs(pstrArgs);
		this.setAllOptions(super.objSettings);
	}
	

} // public class JobSchedulerLaunchAndObserveOptionsSuperClass