package sos.scheduler.CheckRunHistory;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSJobChainName;
import com.sos.JSHelper.Options.JSJobName;
import com.sos.JSHelper.Options.JSOptionMailOptions;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.JSOrderId;
import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.JSHelper.Options.SOSOptionStringValueList;
import com.sos.JSHelper.Options.SOSOptionTimeHorizon;
import com.sos.i18n.annotation.I18NMessage;
import com.sos.i18n.annotation.I18NMessages;
import com.sos.i18n.annotation.I18NResourceBundle;

/**
 * \class 		JobSchedulerCheckRunHistoryOptionsSuperClass - Check the last job run
 *
 * \brief 
 * An Options-Super-Class with all Options. This Class will be extended by the "real" Options-class (\see JobSchedulerCheckRunHistoryOptions.
 * The "real" Option class will hold all the things, which are normaly overwritten at a new generation
 * of the super-class.
 *
 *

 *
 * see \see J:\E\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc\JobSchedulerCheckRunHistory.xml for (more) details.
 * 
 * \verbatim ;
 * mechanicaly created by  from http://www.sos-berlin.com at 20110225184429 
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
	pobjHM.put ("		JobSchedulerCheckRunHistoryOptionsSuperClass.auth_file", "test");  // This parameter specifies the path and name of a user's pr
		return pobjHM;
  }  //  private void SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM)
 * \endverbatim
 */
@I18NResourceBundle(baseName = "com_sos_scheduler_messages", defaultLocale = "en")
@JSOptionClass(name = "JobSchedulerCheckRunHistoryOptionsSuperClass", description = "JobSchedulerCheckRunHistoryOptionsSuperClass")
public class JobSchedulerCheckRunHistoryOptionsSuperClass extends JSOptionsClass {
	/**
	 * 
	 */
	private static final long	serialVersionUID	= -8442592876516710875L;
	private final String	conClassName	= "JobSchedulerCheckRunHistoryOptionsSuperClass";
	@SuppressWarnings("unused")
	private static Logger	logger			= Logger.getLogger(JobSchedulerCheckRunHistoryOptionsSuperClass.class);
	/**
	 * \var JobChainName : The name of a job chain.
	 * The name of a job chain.
	 *
	 */ 
	@JSOptionDefinition(name = "JobChainName", description = "The name of a job chain.", key = "JobChainName", type = "JSJobChainName", mandatory = false)
	public JSJobChainName	JobChainName	= new JSJobChainName(this, conClassName + ".JobChainName", // HashMap-Key
													"The name of a job chain.", // Titel
													"", // InitValue
													"", // DefaultValue
													false // isMandatory
											);

	/**
	 * \brief getJobChainName : The name of a job chain.
	 * 
	 * \details
	 * The name of a job chain.
	 *
	 * \return The name of a job chain.
	 *
	 */
	public JSJobChainName getJobChainName() {
		return JobChainName;
	}

	/**
	 * \brief setJobChainName : The name of a job chain.
	 * 
	 * \details
	 * The name of a job chain.
	 *
	 * @param JobChainName : The name of a job chain.
	 */
	public void setJobChainName(JSJobChainName p_JobChainName) {
		this.JobChainName = p_JobChainName;
	}
	/**
	 * \var JobName : The name of a job.
	 * The name of a job.
	 *
	 */
	@JSOptionDefinition(name = "JobName", description = "The name of the job to check", key = "JobName", type = "JSJobName", mandatory = true)
	public JSJobName	JobName	= new JSJobName(this, conClassName + ".JobName", // HashMap-Key
										//getMsg(JSJ_CRH_0030), // Titel
										"The name of the job to check", // Titel
										"", // InitValue
					 					"", // DefaultValue
										true // isMandatory
								);

	@I18NMessages(value = { @I18NMessage("The name of the job to check"), //
			@I18NMessage(value = "The name of the job to check", locale = "en_UK", //
			explanation = "The name of the job to check" // 
			), //
			@I18NMessage(value = "Der Name des zu prüfenden Jobs", locale = "de", //
			explanation = "The name of the job to check" // 
			) //
	}, msgnum = "JSJ_CRH_0030", msgurl = "msgurl")
	/*!
	 * \var JSJ_CRH_0030
	 * \brief The name of the job to check
	 */
	public static final String JSJ_CRH_0030 = "JSJ_CRH_0030";
	/**
	 * \brief getJobName : The name of a job.
	 * 
	 * \details
	 * The name of a job.
	 *
	 * \return The name of a job.
	 *
	 */
	public JSJobName getJobName() {
		return JobName;
	}

	/**
	 * \brief setJobName : The name of a job.
	 * 
	 * \details
	 * The name of a job.
	 *
	 * @param JobName : The name of a job.
	 */
	public void setJobName(JSJobName p_JobName) {
		this.JobName = p_JobName;
	}
	/**
	 * \var mail_bcc : Email blind carbon copy address of the recipient, see ./c
	 * Email blind carbon copy address of the recipient, see ./config/factory.ini, log_mail_bcc.
	 *
	 */
	@JSOptionDefinition(name = "mail_bcc", description = "Email blind carbon copy address of the recipient, see ./c", key = "mail_bcc", type = "JSOptionMailOptions", mandatory = false)
	public JSOptionMailOptions	mail_bcc	= new JSOptionMailOptions(this, conClassName + ".mail_bcc", // HashMap-Key
													//getMsg(JSJ_CRH_0040), // Titel
													"Email blind carbon copy address of the recipient", // Titel
													"", // InitValue
													"", // DefaultValue
													false // isMandatory
											);

	@I18NMessages(value = { @I18NMessage("Email blind carbon copy address of the recipient, see ./c"), //
			@I18NMessage(value = "Email blind carbon copy address of the recipient, see ./c", locale = "en_UK", //
			explanation = "Email blind carbon copy address of the recipient, see ./c" // 
			), //
			@I18NMessage(value = "Email blind carbon copy address of the recipient, see ./c", locale = "de", //
			explanation = "Email blind carbon copy address of the recipient, see ./c" // 
			) //
	}, msgnum = "JSJ_CRH_0040", msgurl = "msgurl")
	/*!
	 * \var JSJ_CRH_0040
	 * \brief Email blind carbon copy address of the recipient, see ./c
	 */
	public static final String JSJ_CRH_0040 = "JSJ_CRH_0040";
	/**
	 * \brief getmail_bcc : Email blind carbon copy address of the recipient, see ./c
	 * 
	 * \details
	 * Email blind carbon copy address of the recipient, see ./config/factory.ini, log_mail_bcc.
	 *
	 * \return Email blind carbon copy address of the recipient, see ./c
	 *
	 */
	public JSOptionMailOptions getmail_bcc() {
		return mail_bcc;
	}

	/**
	 * \brief setmail_bcc : Email blind carbon copy address of the recipient, see ./c
	 * 
	 * \details
	 * Email blind carbon copy address of the recipient, see ./config/factory.ini, log_mail_bcc.
	 *
	 * @param mail_bcc : Email blind carbon copy address of the recipient, see ./c
	 */
	public void setmail_bcc(JSOptionMailOptions p_mail_bcc) {
		this.mail_bcc = p_mail_bcc;
	}
	/**
	 * \var mail_cc : Email carbon copy address of the recipient, see ./config/
	 * Email carbon copy address of the recipient, see ./config/factory.ini, log_mail_cc.
	 *
	 */
	@JSOptionDefinition(name = "mail_cc", description = "Email carbon copy address of the recipient, see ./config/", key = "mail_cc", type = "JSOptionMailOptions", mandatory = false)
	public JSOptionMailOptions	mail_cc	= new JSOptionMailOptions(this, conClassName + ".mail_cc", // HashMap-Key
												"Email carbon copy address of the recipient, see ./config/", // Titel
												"", // InitValue
												"", // DefaultValue
												false // isMandatory
										);

	/**
	 * \brief getmail_cc : Email carbon copy address of the recipient, see ./config/
	 * 
	 * \details
	 * Email carbon copy address of the recipient, see ./config/factory.ini, log_mail_cc.
	 *
	 * \return Email carbon copy address of the recipient, see ./config/
	 *
	 */
	public JSOptionMailOptions getmail_cc() {
		return mail_cc;
	}

	/**
	 * \brief setmail_cc : Email carbon copy address of the recipient, see ./config/
	 * 
	 * \details
	 * Email carbon copy address of the recipient, see ./config/factory.ini, log_mail_cc.
	 *
	 * @param mail_cc : Email carbon copy address of the recipient, see ./config/
	 */
	public void setmail_cc(JSOptionMailOptions p_mail_cc) {
		this.mail_cc = p_mail_cc;
	}
	/**
	 * \var mail_to : Email address of the recipient, see ./config/factory.ini,
	 * Email address of the recipient, see ./config/factory.ini, log_mail_to.
	 *
	 */
	@JSOptionDefinition(name = "mail_to", description = "Email address of the recipient, see ./config/factory.ini,", key = "mail_to", type = "JSOptionMailOptions", mandatory = false)
	public JSOptionMailOptions	mail_to	= new JSOptionMailOptions(this, conClassName + ".mail_to", // HashMap-Key
												"Email address of the recipient, see ./config/factory.ini,", // Titel
												"", // InitValue
												"", // DefaultValue
												false // isMandatory
										);

	/**
	 * \brief getmail_to : Email address of the recipient, see ./config/factory.ini,
	 * 
	 * \details
	 * Email address of the recipient, see ./config/factory.ini, log_mail_to.
	 *
	 * \return Email address of the recipient, see ./config/factory.ini,
	 *
	 */
	public JSOptionMailOptions getmail_to() {
		return mail_to;
	}

	/**
	 * \brief setmail_to : Email address of the recipient, see ./config/factory.ini,
	 * 
	 * \details
	 * Email address of the recipient, see ./config/factory.ini, log_mail_to.
	 *
	 * @param mail_to : Email address of the recipient, see ./config/factory.ini,
	 */
	public void setmail_to(JSOptionMailOptions p_mail_to) {
		this.mail_to = p_mail_to;
	}
	/**
	 * \var message : Text in the email subject and in the log.
	 * Text in the email subject and in the log. ${JOB_NAME} will be substituted with the value of the parameter jobname. ${NOW} will be substituted with the current time.
	 *
	 */
	@JSOptionDefinition(name = "message", description = "Text in the email subject and in the log.", key = "message", type = "SOSOptionString", mandatory = false)
	public SOSOptionString	message	= new SOSOptionString(this, conClassName + ".message", // HashMap-Key
											"Text in the email subject and in the log.", // Titel
											"", // InitValue
											"", // DefaultValue
											false // isMandatory
									);

	/**
	 * \brief getmessage : Text in the email subject and in the log.
	 * 
	 * \details
	 * Text in the email subject and in the log. ${JOB_NAME} will be substituted with the value of the parameter jobname. ${NOW} will be substituted with the current time.
	 *
	 * \return Text in the email subject and in the log.
	 *
	 */
	public SOSOptionString getmessage() {
		return message;
	}

	/**
	 * \brief setmessage : Text in the email subject and in the log.
	 * 
	 * \details
	 * Text in the email subject and in the log. ${JOB_NAME} will be substituted with the value of the parameter jobname. ${NOW} will be substituted with the current time.
	 *
	 * @param message : Text in the email subject and in the log.
	 */
	public void setmessage(SOSOptionString p_message) {
		this.message = p_message;
	}
	public SOSOptionString			Subject		= (SOSOptionString) message.SetAlias(conClassName + ".Subject");
	/**
	 * \var operation : Operation to be executed
	 * 
	 *
	 */
	@JSOptionDefinition(name = "operation", description = "Operation to be executed", key = "operation", type = "SOSOptionStringValueList", mandatory = true)
	public SOSOptionStringValueList	operation	= new SOSOptionStringValueList(this, conClassName + ".operation", // HashMap-Key
														"Operation to be executed", // Titel
														"late", // InitValue
														"late", // DefaultValue
														true // isMandatory
												);

	/**
	 * \brief getoperation : Operation to be executed
	 * 
	 * \details
	 * 
	 *
	 * \return Operation to be executed
	 *
	 */
	public SOSOptionStringValueList getoperation() {
		return operation;
	}

	/**
	 * \brief setoperation : Operation to be executed
	 * 
	 * \details
	 * 
	 *
	 * @param operation : Operation to be executed
	 */
	public void setoperation(SOSOptionStringValueList p_operation) {
		this.operation = p_operation;
	}
	/**
	 * \var OrderId : The name or the identification of an order.
	 * The name or the identification of an order.
	 *
	 */
	@JSOptionDefinition(name = "OrderId", description = "The name or the identification of an order.", key = "OrderId", type = "JSOrderId", mandatory = false)
	public JSOrderId	OrderId	= new JSOrderId(this, conClassName + ".OrderId", // HashMap-Key
										"The name or the identification of an order.", // Titel
										"", // InitValue
										"", // DefaultValue
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
	 * \var start_time : The start time from which the parametrisized job is check
	 * The start time from which the parametrisized job is checked wether it has successfully run or not. The start time must be set in the form [number of elapsed days],Time(HH:MM:SS), so that the default value is last midnight.
	 *
	 */
	@JSOptionDefinition(name = "start_time", description = "The start time from which the parametrisized job is check", key = "start_time", type = "SOSOptionString", mandatory = false)
	public SOSOptionTimeHorizon	start_time	= new SOSOptionTimeHorizon(this, conClassName + ".start_time", // HashMap-Key
												"The start time from which the parametrisized job is check", // Titel
												"0:00:00:00", // InitValue
												"0:00:00:00", // DefaultValue
												false // isMandatory
										);

	/**
	 * \brief getstart_time : The start time from which the parametrisized job is check
	 * 
	 * \details
	 * The start time from which the parametrisized job is checked wether it has successfully run or not. The start time must be set in the form [number of elapsed days],Time(HH:MM:SS), so that the default value is last midnight.
	 *
	 * \return The start time from which the parametrisized job is check
	 *
	 */
	public SOSOptionTimeHorizon getstart_time() {
		return start_time;
	}

	/**
	 * \brief setstart_time : The start time from which the parametrisized job is check
	 * 
	 * \details
	 * The start time from which the parametrisized job is checked wether it has successfully run or not. The start time must be set in the form [number of elapsed days],Time(HH:MM:SS), so that the default value is last midnight.
	 *
	 * @param start_time : The start time from which the parametrisized job is check
	 */
	public void setstart_time(SOSOptionTimeHorizon p_start_time) {
		this.start_time = p_start_time;
	}

	public JobSchedulerCheckRunHistoryOptionsSuperClass() {
		objParentClass = this.getClass();
	} // public JobSchedulerCheckRunHistoryOptionsSuperClass

	public JobSchedulerCheckRunHistoryOptionsSuperClass(JSListener pobjListener) {
		this();
		this.registerMessageListener(pobjListener);
	} // public JobSchedulerCheckRunHistoryOptionsSuperClass

	//
	public JobSchedulerCheckRunHistoryOptionsSuperClass(HashMap<String, String> JSSettings) throws Exception {
		this();
		this.setAllOptions(JSSettings);
	} // public JobSchedulerCheckRunHistoryOptionsSuperClass (HashMap JSSettings)

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
	} // public void setAllOptions (HashMap <String, String> JSSettings)

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
} // public class JobSchedulerCheckRunHistoryOptionsSuperClass