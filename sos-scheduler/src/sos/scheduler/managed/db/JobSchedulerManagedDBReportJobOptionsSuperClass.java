package sos.scheduler.managed.db;

import java.util.HashMap;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionInteger;
import com.sos.JSHelper.Options.SOSOptionJdbcUrl;
import com.sos.JSHelper.Options.SOSOptionOutFileName;
import com.sos.JSHelper.Options.SOSOptionPassword;
import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.JSHelper.Options.SOSOptionUserName;

/**
 * \class 		JobSchedulerManagedDBReportJobOptionsSuperClass - Launch Database Report
 *
 * \brief 
 * An Options-Super-Class with all Options. This Class will be extended by the "real" Options-class (\see JobSchedulerManagedDBReportJobOptions.
 * The "real" Option class will hold all the things, which are normaly overwritten at a new generation
 * of the super-class.
 *
 *

 *
 * see \see R:\backup\sos\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc\JobSchedulerManagedDBReportJob.xml for (more) details.
 * 
 * \verbatim ;
 * mechanicaly created by  from http://www.sos-berlin.com at 20120830214156 
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
	pobjHM.put ("		JobSchedulerManagedDBReportJobOptionsSuperClass.auth_file", "test");  // This parameter specifies the path and name of a user's pr
		return pobjHM;
  }  //  private void SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM)
 * \endverbatim
 */
@JSOptionClass(name = "JobSchedulerManagedDBReportJobOptionsSuperClass", description = "JobSchedulerManagedDBReportJobOptionsSuperClass")
public class JobSchedulerManagedDBReportJobOptionsSuperClass extends JSOptionsClass {
	private final String	conClassName		= "JobSchedulerManagedDBReportJobOptionsSuperClass";
	@SuppressWarnings("unused")
	private static Logger	logger				= Logger.getLogger(JobSchedulerManagedDBReportJobOptionsSuperClass.class);

	/**
	 * \var Adjust_column_names : Character conversion for column names
	 * 
	 *
	 */
	@JSOptionDefinition(name = "Adjust_column_names", description = "Character conversion for column names", key = "Adjust_column_names", type = "SOSOptionBoolean", mandatory = false)
	public SOSOptionBoolean	Adjust_column_names	= new SOSOptionBoolean(this, conClassName + ".Adjust_column_names", // HashMap-Key
														"Character conversion for column names", // Titel
														"true", // InitValue
														"true", // DefaultValue
														false // isMandatory
												);

	/**
	 * \brief getAdjust_column_names : Character conversion for column names
	 * 
	 * \details
	 * 
	 *
	 * \return Character conversion for column names
	 *
	 */
	public SOSOptionBoolean getAdjust_column_names() {
		return Adjust_column_names;
	}

	/**
	 * \brief setAdjust_column_names : Character conversion for column names
	 * 
	 * \details
	 * 
	 *
	 * @param Adjust_column_names : Character conversion for column names
	 */
	public void setAdjust_column_names(final SOSOptionBoolean p_Adjust_column_names) {
		Adjust_column_names = p_Adjust_column_names;
	}

	/**
	 * \var Column_names_case_sensitivity : Let Column names as is
	 * 
	 *
	 */
	@JSOptionDefinition(name = "Column_names_case_sensitivity", description = "Let Column names as is", key = "Column_names_case_sensitivity", type = "SOSOptionBoolean", mandatory = false)
	public SOSOptionBoolean	Column_names_case_sensitivity	= new SOSOptionBoolean(this, conClassName + ".Column_names_case_sensitivity", // HashMap-Key
																	"Let Column names as is", // Titel
																	"false", // InitValue
																	"false", // DefaultValue
																	false // isMandatory
															);

	/**
	 * \brief getColumn_names_case_sensitivity : Let Column names as is
	 * 
	 * \details
	 * 
	 *
	 * \return Let Column names as is
	 *
	 */
	public SOSOptionBoolean getColumn_names_case_sensitivity() {
		return Column_names_case_sensitivity;
	}

	/**
	 * \brief setColumn_names_case_sensitivity : Let Column names as is
	 * 
	 * \details
	 * 
	 *
	 * @param Column_names_case_sensitivity : Let Column names as is
	 */
	public void setColumn_names_case_sensitivity(final SOSOptionBoolean p_Column_names_case_sensitivity) {
		Column_names_case_sensitivity = p_Column_names_case_sensitivity;
	}

	/**
	 * \var command : Database Commands for the Job
	 * Database Commands for the Job. It is possible to define more than one instruction in the COMMAND field. Such instructions are then carried out in the order in which they are written and must be separated by a semi colon and a subsequent new line. Parameters can be replaced in database commands. This is done by the addition of a variable in the form §{param} at any given point in a command. This variable is then given the value of the instruction parameter with the name [param] before execution.
	 *
	 */
	@JSOptionDefinition(name = "command", description = "Database Commands for the Job", key = "command", type = "SOSOptionString", mandatory = false)
	public SOSOptionString	command	= new SOSOptionString(this, conClassName + ".command", // HashMap-Key
											"Database Commands for the Job", // Titel
											" ", // InitValue
											" ", // DefaultValue
											false // isMandatory
									);

	/**
	 * \brief getcommand : Database Commands for the Job
	 * 
	 * \details
	 * Database Commands for the Job. It is possible to define more than one instruction in the COMMAND field. Such instructions are then carried out in the order in which they are written and must be separated by a semi colon and a subsequent new line. Parameters can be replaced in database commands. This is done by the addition of a variable in the form §{param} at any given point in a command. This variable is then given the value of the instruction parameter with the name [param] before execution.
	 *
	 * \return Database Commands for the Job
	 *
	 */
	public SOSOptionString getcommand() {
		return command;
	}

	/**
	 * \brief setcommand : Database Commands for the Job
	 * 
	 * \details
	 * Database Commands for the Job. It is possible to define more than one instruction in the COMMAND field. Such instructions are then carried out in the order in which they are written and must be separated by a semi colon and a subsequent new line. Parameters can be replaced in database commands. This is done by the addition of a variable in the form §{param} at any given point in a command. This variable is then given the value of the instruction parameter with the name [param] before execution.
	 *
	 * @param command : Database Commands for the Job
	 */
	public void setcommand(final SOSOptionString p_command) {
		command = p_command;
	}

	public SOSOptionString	SQL_Command			= (SOSOptionString) command.SetAlias(conClassName + ".SQL_Command");

	/**
	 * \var database_connection : database connection from table SCHEDULER_MANAGED_CONNECTIONS
	 * Name of the selected database connection in table SCHEDULER_MANAGED_CONNECTIONS.
	 *
	 */
	@JSOptionDefinition(name = "database_connection", description = "database connection from table SCHEDULER_MANAGED_CONNECTIONS", key = "database_connection", type = "SOSOptionString", mandatory = false)
	public SOSOptionString	database_connection	= new SOSOptionString(this, conClassName + ".database_connection", // HashMap-Key
														"database connection from table SCHEDULER_MANAGED_CONNECTIONS", // Titel
														" ", // InitValue
														" ", // DefaultValue
														false // isMandatory
												);

	/**
	 * \brief getdatabase_connection : database connection from table SCHEDULER_MANAGED_CONNECTIONS
	 * 
	 * \details
	 * Name of the selected database connection in table SCHEDULER_MANAGED_CONNECTIONS.
	 *
	 * \return database connection from table SCHEDULER_MANAGED_CONNECTIONS
	 *
	 */
	public SOSOptionString getdatabase_connection() {
		return database_connection;
	}

	/**
	 * \brief setdatabase_connection : database connection from table SCHEDULER_MANAGED_CONNECTIONS
	 * 
	 * \details
	 * Name of the selected database connection in table SCHEDULER_MANAGED_CONNECTIONS.
	 *
	 * @param database_connection : database connection from table SCHEDULER_MANAGED_CONNECTIONS
	 */
	public void setdatabase_connection(final SOSOptionString p_database_connection) {
		database_connection = p_database_connection;
	}

	/**
	 * \var db_class : SOS Connection class
	 * Name of the SOS Connection class for the database: SOSMSSQLConnection SOSMySQLConnection SOSOracleConnection SOSPgSQLConnection SOSFbSQLConnection SOSDB2Connection
	 *
	 */
	@JSOptionDefinition(name = "db_class", description = "SOS Connection class", key = "db_class", type = "SOSOptionString", mandatory = false)
	public SOSOptionString	db_class	= new SOSOptionString(this, conClassName + ".db_class", // HashMap-Key
												"SOS Connection class", // Titel
												" ", // InitValue
												" ", // DefaultValue
												false // isMandatory
										);

	/**
	 * \brief getdb_class : SOS Connection class
	 * 
	 * \details
	 * Name of the SOS Connection class for the database: SOSMSSQLConnection SOSMySQLConnection SOSOracleConnection SOSPgSQLConnection SOSFbSQLConnection SOSDB2Connection
	 *
	 * \return SOS Connection class
	 *
	 */
	public SOSOptionString getdb_class() {
		return db_class;
	}

	/**
	 * \brief setdb_class : SOS Connection class
	 * 
	 * \details
	 * Name of the SOS Connection class for the database: SOSMSSQLConnection SOSMySQLConnection SOSOracleConnection SOSPgSQLConnection SOSFbSQLConnection SOSDB2Connection
	 *
	 * @param db_class : SOS Connection class
	 */
	public void setdb_class(final SOSOptionString p_db_class) {
		db_class = p_db_class;
	}

	/**
	 * \var db_driver : Name of the jd
	 * Name of the jdbc driver Class
	 *
	 */
	@JSOptionDefinition(name = "db_driver", description = "Name of the jd", key = "db_driver", type = "SOSOptionString", mandatory = false)
	public SOSOptionString	db_driver	= new SOSOptionString(this, conClassName + ".db_driver", // HashMap-Key
												"Name of the jd", // Titel
												" ", // InitValue
												" ", // DefaultValue
												false // isMandatory
										);

	/**
	 * \brief getdb_driver : Name of the jd
	 * 
	 * \details
	 * Name of the jdbc driver Class
	 *
	 * \return Name of the jd
	 *
	 */
	public SOSOptionString getdb_driver() {
		return db_driver;
	}

	/**
	 * \brief setdb_driver : Name of the jd
	 * 
	 * \details
	 * Name of the jdbc driver Class
	 *
	 * @param db_driver : Name of the jd
	 */
	public void setdb_driver(final SOSOptionString p_db_driver) {
		db_driver = p_db_driver;
	}

	/**
	 * \var db_password : database password
	 * database password
	 *
	 */
	@JSOptionDefinition(name = "db_password", description = "database password", key = "db_password", type = "SOSOptionPassword", mandatory = false)
	public SOSOptionPassword	db_password	= new SOSOptionPassword(this, conClassName + ".db_password", // HashMap-Key
													"database password", // Titel
													" ", // InitValue
													" ", // DefaultValue
													false // isMandatory
											);

	/**
	 * \brief getdb_password : database password
	 * 
	 * \details
	 * database password
	 *
	 * \return database password
	 *
	 */
	public SOSOptionPassword getdb_password() {
		return db_password;
	}

	/**
	 * \brief setdb_password : database password
	 * 
	 * \details
	 * database password
	 *
	 * @param db_password : database password
	 */
	public void setdb_password(final SOSOptionPassword p_db_password) {
		db_password = p_db_password;
	}

	/**
	 * \var db_url : jdbc url
	 * jdbc url
	 *
	 */
	@JSOptionDefinition(name = "db_url", description = "jdbc url", key = "db_url", type = "SOSOptionUrl", mandatory = false)
	public SOSOptionJdbcUrl	db_url	= new SOSOptionJdbcUrl(this, conClassName + ".db_url", // HashMap-Key
											"jdbc url", // Titel
											"", // InitValue
											"", // DefaultValue
											false // isMandatory
									);

	/**
	 * \brief getdb_url : jdbc url
	 * 
	 * \details
	 * jdbc url
	 *
	 * \return jdbc url
	 *
	 */
	public SOSOptionJdbcUrl getdb_url() {
		return db_url;
	}

	/**
	 * \brief setdb_url : jdbc url
	 * 
	 * \details
	 * jdbc url
	 *
	 * @param db_url : jdbc url
	 */
	public void setdb_url(final SOSOptionJdbcUrl p_db_url) {
		db_url = p_db_url;
	}

	/**
	 * \var db_user : database user
	 * database user
	 *
	 */
	@JSOptionDefinition(name = "db_user", description = "database user", key = "db_user", type = "SOSOptionUserName", mandatory = false)
	public SOSOptionUserName	db_user	= new SOSOptionUserName(this, conClassName + ".db_user", // HashMap-Key
												"database user", // Titel
												" ", // InitValue
												" ", // DefaultValue
												false // isMandatory
										);

	/**
	 * \brief getdb_user : database user
	 * 
	 * \details
	 * database user
	 *
	 * \return database user
	 *
	 */
	public SOSOptionUserName getdb_user() {
		return db_user;
	}

	/**
	 * \brief setdb_user : database user
	 * 
	 * \details
	 * database user
	 *
	 * @param db_user : database user
	 */
	public void setdb_user(final SOSOptionUserName p_db_user) {
		db_user = p_db_user;
	}

	/**
	 * \var exec_returns_resultset : If stored proc
	 * If stored procedures are called which return a result set, this needs to be set to true in order to run the stored procedure as a query. This does not work with the SQL Server 2000 and 2005 jdbc drivers.
	 *
	 */
	@JSOptionDefinition(name = "exec_returns_resultset", description = "If stored proc", key = "exec_returns_resultset", type = "SOSOptionBoolean", mandatory = false)
	public SOSOptionBoolean	exec_returns_resultset	= new SOSOptionBoolean(this, conClassName + ".exec_returns_resultset", // HashMap-Key
															"If stored proc", // Titel
															"false", // InitValue
															"false", // DefaultValue
															false // isMandatory
													);

	/**
	 * \brief getexec_returns_resultset : If stored proc
	 * 
	 * \details
	 * If stored procedures are called which return a result set, this needs to be set to true in order to run the stored procedure as a query. This does not work with the SQL Server 2000 and 2005 jdbc drivers.
	 *
	 * \return If stored proc
	 *
	 */
	public SOSOptionBoolean getexec_returns_resultset() {
		return exec_returns_resultset;
	}

	/**
	 * \brief setexec_returns_resultset : If stored proc
	 * 
	 * \details
	 * If stored procedures are called which return a result set, this needs to be set to true in order to run the stored procedure as a query. This does not work with the SQL Server 2000 and 2005 jdbc drivers.
	 *
	 * @param exec_returns_resultset : If stored proc
	 */
	public void setexec_returns_resultset(final SOSOptionBoolean p_exec_returns_resultset) {
		exec_returns_resultset = p_exec_returns_resultset;
	}

	/**
	 * \var Max_No_Of_Records_To_Process : Max Number of lines in the result table to process
	 * "-1" means no restriction.
	 *
	 */
	@JSOptionDefinition(name = "Max_No_Of_Records_To_Process", description = "Max Number of lines in the result table to process", key = "Max_No_Of_Records_To_Process", type = "SOSOptionInteger", mandatory = false)
	public SOSOptionInteger	Max_No_Of_Records_To_Process	= new SOSOptionInteger(this, conClassName + ".Max_No_Of_Records_To_Process", // HashMap-Key
																	"Max Number of lines in the result table to process", // Titel
																	"-1", // InitValue
																	"-1", // DefaultValue
																	false // isMandatory
															);

	/**
	 * \brief getMax_No_Of_Records_To_Process : Max Number of lines in the result table to process
	 * 
	 * \details
	 * "-1" means no restriction.
	 *
	 * \return Max Number of lines in the result table to process
	 *
	 */
	public SOSOptionInteger getMax_No_Of_Records_To_Process() {
		return Max_No_Of_Records_To_Process;
	}

	/**
	 * \brief setMax_No_Of_Records_To_Process : Max Number of lines in the result table to process
	 * 
	 * \details
	 * "-1" means no restriction.
	 *
	 * @param Max_No_Of_Records_To_Process : Max Number of lines in the result table to process
	 */
	public void setMax_No_Of_Records_To_Process(final SOSOptionInteger p_Max_No_Of_Records_To_Process) {
		Max_No_Of_Records_To_Process = p_Max_No_Of_Records_To_Process;
	}

	/**
	 * \var scheduler_order_report_asbody : This setting d
	 * This setting determines whether the report should be sent as an attachment (default) or as the content of an e-mail.
	 *
	 */
	@JSOptionDefinition(name = "scheduler_order_report_asbody", description = "This setting d", key = "scheduler_order_report_asbody", type = "SOSOptionBoolean", mandatory = false)
	public SOSOptionBoolean	scheduler_order_report_asbody	= new SOSOptionBoolean(this, conClassName + ".scheduler_order_report_asbody", // HashMap-Key
																	"This setting d", // Titel
																	"false", // InitValue
																	"false", // DefaultValue
																	false // isMandatory
															);

	/**
	 * \brief getscheduler_order_report_asbody : This setting d
	 * 
	 * \details
	 * This setting determines whether the report should be sent as an attachment (default) or as the content of an e-mail.
	 *
	 * \return This setting d
	 *
	 */
	public SOSOptionBoolean getscheduler_order_report_asbody() {
		return scheduler_order_report_asbody;
	}

	/**
	 * \brief setscheduler_order_report_asbody : This setting d
	 * 
	 * \details
	 * This setting determines whether the report should be sent as an attachment (default) or as the content of an e-mail.
	 *
	 * @param scheduler_order_report_asbody : This setting d
	 */
	public void setscheduler_order_report_asbody(final SOSOptionBoolean p_scheduler_order_report_asbody) {
		scheduler_order_report_asbody = p_scheduler_order_report_asbody;
	}

	/**
	 * \var scheduler_order_report_body : 
	 * This parameter specifies the report layout in either HTML or plain text formats. The following placeholders can be used in the layout, each within square [ ] brackets, e.g. [date]: [date] - the current date [datetime] - the current date and time [orderid] - the order ID [jobname] - the job name [taskid] - the task ID [sql] - the SQL query used to create the report [xml] - untransformed XML generated from the query results Note that the content of this parameter will be ignored, should the scheduler_order_report_asbody parameter be set to false.
	 *
	 */
	@JSOptionDefinition(name = "scheduler_order_report_body", description = "", key = "scheduler_order_report_body", type = "SOSOptionString", mandatory = false)
	public SOSOptionString	scheduler_order_report_body	= new SOSOptionString(this, conClassName + ".scheduler_order_report_body", // HashMap-Key
																"", // Titel
																" ", // InitValue
																" ", // DefaultValue
																false // isMandatory
														);

	/**
	 * \brief getscheduler_order_report_body : 
	 * 
	 * \details
	 * This parameter specifies the report layout in either HTML or plain text formats. The following placeholders can be used in the layout, each within square [ ] brackets, e.g. [date]: [date] - the current date [datetime] - the current date and time [orderid] - the order ID [jobname] - the job name [taskid] - the task ID [sql] - the SQL query used to create the report [xml] - untransformed XML generated from the query results Note that the content of this parameter will be ignored, should the scheduler_order_report_asbody parameter be set to false.
	 *
	 * \return 
	 *
	 */
	public SOSOptionString getscheduler_order_report_body() {
		return scheduler_order_report_body;
	}

	/**
	 * \brief setscheduler_order_report_body : 
	 * 
	 * \details
	 * This parameter specifies the report layout in either HTML or plain text formats. The following placeholders can be used in the layout, each within square [ ] brackets, e.g. [date]: [date] - the current date [datetime] - the current date and time [orderid] - the order ID [jobname] - the job name [taskid] - the task ID [sql] - the SQL query used to create the report [xml] - untransformed XML generated from the query results Note that the content of this parameter will be ignored, should the scheduler_order_report_asbody parameter be set to false.
	 *
	 * @param scheduler_order_report_body : 
	 */
	public void setscheduler_order_report_body(final SOSOptionString p_scheduler_order_report_body) {
		scheduler_order_report_body = p_scheduler_order_report_body;
	}

	/**
	 * \var scheduler_order_report_filename : 
	 * The name to be used for the attachment file when the report is to be sent as an e-mail. This name will also be used for the permanently saved copy of the report, should a path have been specified using the scheduler_order_report_path parameter. The following placeholders can be used, each placeholder being inserted in square [ ] brackets - e.g. [date]: [date] - the current date [orderid] - the order ID [jobname] - the job name [taskid] - the task ID
	 *
	 */
	@JSOptionDefinition(name = "scheduler_order_report_filename", description = "", key = "scheduler_order_report_filename", type = "SOSOptionOutFileName", mandatory = false)
	public SOSOptionOutFileName	scheduler_order_report_filename	= new SOSOptionOutFileName(this, conClassName + ".scheduler_order_report_filename", // HashMap-Key
																		"", // Titel
																		"report_[date]_[taskid].xml", // InitValue
																		"report_[date]_[taskid].xml", // DefaultValue
																		false // isMandatory
																);

	/**
	 * \brief getscheduler_order_report_filename : 
	 * 
	 * \details
	 * The name to be used for the attachment file when the report is to be sent as an e-mail. This name will also be used for the permanently saved copy of the report, should a path have been specified using the scheduler_order_report_path parameter. The following placeholders can be used, each placeholder being inserted in square [ ] brackets - e.g. [date]: [date] - the current date [orderid] - the order ID [jobname] - the job name [taskid] - the task ID
	 *
	 * \return 
	 *
	 */
	public SOSOptionOutFileName getscheduler_order_report_filename() {
		return scheduler_order_report_filename;
	}

	/**
	 * \brief setscheduler_order_report_filename : 
	 * 
	 * \details
	 * The name to be used for the attachment file when the report is to be sent as an e-mail. This name will also be used for the permanently saved copy of the report, should a path have been specified using the scheduler_order_report_path parameter. The following placeholders can be used, each placeholder being inserted in square [ ] brackets - e.g. [date]: [date] - the current date [orderid] - the order ID [jobname] - the job name [taskid] - the task ID
	 *
	 * @param scheduler_order_report_filename : 
	 */
	public void setscheduler_order_report_filename(final SOSOptionOutFileName p_scheduler_order_report_filename) {
		scheduler_order_report_filename = p_scheduler_order_report_filename;
	}

	/**
	 * \var scheduler_order_report_mailbcc : One or more e-
	 * One or more e-mail addresses of recipients of blind carbon copies of a report. Multiple addresses are to be separated by commas.
	 *
	 */
	@JSOptionDefinition(name = "scheduler_order_report_mailbcc", description = "One or more e-", key = "scheduler_order_report_mailbcc", type = "SOSOptionString", mandatory = false)
	public SOSOptionString	scheduler_order_report_mailbcc	= new SOSOptionString(this, conClassName + ".scheduler_order_report_mailbcc", // HashMap-Key
																	"One or more e-", // Titel
																	" ", // InitValue
																	" ", // DefaultValue
																	false // isMandatory
															);

	/**
	 * \brief getscheduler_order_report_mailbcc : One or more e-
	 * 
	 * \details
	 * One or more e-mail addresses of recipients of blind carbon copies of a report. Multiple addresses are to be separated by commas.
	 *
	 * \return One or more e-
	 *
	 */
	public SOSOptionString getscheduler_order_report_mailbcc() {
		return scheduler_order_report_mailbcc;
	}

	/**
	 * \brief setscheduler_order_report_mailbcc : One or more e-
	 * 
	 * \details
	 * One or more e-mail addresses of recipients of blind carbon copies of a report. Multiple addresses are to be separated by commas.
	 *
	 * @param scheduler_order_report_mailbcc : One or more e-
	 */
	public void setscheduler_order_report_mailbcc(final SOSOptionString p_scheduler_order_report_mailbcc) {
		scheduler_order_report_mailbcc = p_scheduler_order_report_mailbcc;
	}

	/**
	 * \var scheduler_order_report_mailcc : One or more e-
	 * One or more e-mail addresses of recipients of carbon copies of a report. Multiple addresses are to be separated by commas.
	 *
	 */
	@JSOptionDefinition(name = "scheduler_order_report_mailcc", description = "One or more e-", key = "scheduler_order_report_mailcc", type = "SOSOptionString", mandatory = false)
	public SOSOptionString	scheduler_order_report_mailcc	= new SOSOptionString(this, conClassName + ".scheduler_order_report_mailcc", // HashMap-Key
																	"One or more e-", // Titel
																	" ", // InitValue
																	" ", // DefaultValue
																	false // isMandatory
															);

	/**
	 * \brief getscheduler_order_report_mailcc : One or more e-
	 * 
	 * \details
	 * One or more e-mail addresses of recipients of carbon copies of a report. Multiple addresses are to be separated by commas.
	 *
	 * \return One or more e-
	 *
	 */
	public SOSOptionString getscheduler_order_report_mailcc() {
		return scheduler_order_report_mailcc;
	}

	/**
	 * \brief setscheduler_order_report_mailcc : One or more e-
	 * 
	 * \details
	 * One or more e-mail addresses of recipients of carbon copies of a report. Multiple addresses are to be separated by commas.
	 *
	 * @param scheduler_order_report_mailcc : One or more e-
	 */
	public void setscheduler_order_report_mailcc(final SOSOptionString p_scheduler_order_report_mailcc) {
		scheduler_order_report_mailcc = p_scheduler_order_report_mailcc;
	}

	/**
	 * \var scheduler_order_report_mailto : report_mailto: recipients of a report
	 * One or more e-mail addresses of recipients of a report. Multiple addresses are to be separated by commas. Should this setting be left empty, then no reports will be sent by e-mail. The reports will however be generated and saved.
	 *
	 */
	@JSOptionDefinition(name = "scheduler_order_report_mailto", description = "report_mailto: recipients of a report", key = "scheduler_order_report_mailto", type = "SOSOptionString", mandatory = false)
	public SOSOptionString	scheduler_order_report_mailto	= new SOSOptionString(this, conClassName + ".scheduler_order_report_mailto", // HashMap-Key
																	"report_mailto: recipients of a report", // Titel
																	" ", // InitValue
																	" ", // DefaultValue
																	false // isMandatory
															);

	/**
	 * \brief getscheduler_order_report_mailto : report_mailto: recipients of a report
	 * 
	 * \details
	 * One or more e-mail addresses of recipients of a report. Multiple addresses are to be separated by commas. Should this setting be left empty, then no reports will be sent by e-mail. The reports will however be generated and saved.
	 *
	 * \return report_mailto: recipients of a report
	 *
	 */
	public SOSOptionString getscheduler_order_report_mailto() {
		return scheduler_order_report_mailto;
	}

	/**
	 * \brief setscheduler_order_report_mailto : report_mailto: recipients of a report
	 * 
	 * \details
	 * One or more e-mail addresses of recipients of a report. Multiple addresses are to be separated by commas. Should this setting be left empty, then no reports will be sent by e-mail. The reports will however be generated and saved.
	 *
	 * @param scheduler_order_report_mailto : report_mailto: recipients of a report
	 */
	public void setscheduler_order_report_mailto(final SOSOptionString p_scheduler_order_report_mailto) {
		scheduler_order_report_mailto = p_scheduler_order_report_mailto;
	}

	/**
	 * \var scheduler_order_report_path : 
	 * The path to the directory in which a report is saved. The name of the report file is not to be specified here. The following placeholders can be used in the path, inserted in square [ ] brackets - e.g.  [date]: [date] - the current date [orderid] - the order ID [jobname] - the job name [taskid] - the task ID
	 *
	 */
	@JSOptionDefinition(name = "scheduler_order_report_path", description = "", key = "scheduler_order_report_path", type = "SOSOptionString", mandatory = false)
	public SOSOptionString	scheduler_order_report_path	= new SOSOptionString(this, conClassName + ".scheduler_order_report_path", // HashMap-Key
																"", // Titel
																" ", // InitValue
																" ", // DefaultValue
																false // isMandatory
														);

	/**
	 * \brief getscheduler_order_report_path : 
	 * 
	 * \details
	 * The path to the directory in which a report is saved. The name of the report file is not to be specified here. The following placeholders can be used in the path, inserted in square [ ] brackets - e.g.  [date]: [date] - the current date [orderid] - the order ID [jobname] - the job name [taskid] - the task ID
	 *
	 * \return 
	 *
	 */
	public SOSOptionString getscheduler_order_report_path() {
		return scheduler_order_report_path;
	}

	/**
	 * \brief setscheduler_order_report_path : 
	 * 
	 * \details
	 * The path to the directory in which a report is saved. The name of the report file is not to be specified here. The following placeholders can be used in the path, inserted in square [ ] brackets - e.g.  [date]: [date] - the current date [orderid] - the order ID [jobname] - the job name [taskid] - the task ID
	 *
	 * @param scheduler_order_report_path : 
	 */
	public void setscheduler_order_report_path(final SOSOptionString p_scheduler_order_report_path) {
		scheduler_order_report_path = p_scheduler_order_report_path;
	}

	/**
	 * \var scheduler_order_report_send_if_no_result : This setting s
	 * This setting specifies whether a report should be sent when the SQL query does not return a result.
	 *
	 */
	@JSOptionDefinition(name = "scheduler_order_report_send_if_no_result", description = "This setting s", key = "scheduler_order_report_send_if_no_result", type = "SOSOptionBoolean", mandatory = false)
	public SOSOptionBoolean	scheduler_order_report_send_if_no_result	= new SOSOptionBoolean(this,
																				conClassName + ".scheduler_order_report_send_if_no_result", // HashMap-Key
																				"This setting s", // Titel
																				"false", // InitValue
																				"false", // DefaultValue
																				false // isMandatory
																		);

	/**
	 * \brief getscheduler_order_report_send_if_no_result : This setting s
	 * 
	 * \details
	 * This setting specifies whether a report should be sent when the SQL query does not return a result.
	 *
	 * \return This setting s
	 *
	 */
	public SOSOptionBoolean getscheduler_order_report_send_if_no_result() {
		return scheduler_order_report_send_if_no_result;
	}

	/**
	 * \brief setscheduler_order_report_send_if_no_result : This setting s
	 * 
	 * \details
	 * This setting specifies whether a report should be sent when the SQL query does not return a result.
	 *
	 * @param scheduler_order_report_send_if_no_result : This setting s
	 */
	public void setscheduler_order_report_send_if_no_result(final SOSOptionBoolean p_scheduler_order_report_send_if_no_result) {
		scheduler_order_report_send_if_no_result = p_scheduler_order_report_send_if_no_result;
	}

	/**
	 * \var scheduler_order_report_send_if_result : This setting s
	 * This setting specifies whether a report should be sent when an SQL query returns a result.
	 *
	 */
	@JSOptionDefinition(name = "scheduler_order_report_send_if_result", description = "This setting s", key = "scheduler_order_report_send_if_result", type = "SOSOptionBoolean", mandatory = false)
	public SOSOptionBoolean	scheduler_order_report_send_if_result	= new SOSOptionBoolean(this, conClassName + ".scheduler_order_report_send_if_result", // HashMap-Key
																			"This setting s", // Titel
																			"true", // InitValue
																			"true", // DefaultValue
																			false // isMandatory
																	);

	/**
	 * \brief getscheduler_order_report_send_if_result : This setting s
	 * 
	 * \details
	 * This setting specifies whether a report should be sent when an SQL query returns a result.
	 *
	 * \return This setting s
	 *
	 */
	public SOSOptionBoolean getscheduler_order_report_send_if_result() {
		return scheduler_order_report_send_if_result;
	}

	/**
	 * \brief setscheduler_order_report_send_if_result : This setting s
	 * 
	 * \details
	 * This setting specifies whether a report should be sent when an SQL query returns a result.
	 *
	 * @param scheduler_order_report_send_if_result : This setting s
	 */
	public void setscheduler_order_report_send_if_result(final SOSOptionBoolean p_scheduler_order_report_send_if_result) {
		scheduler_order_report_send_if_result = p_scheduler_order_report_send_if_result;
	}

	/**
	 * \var scheduler_order_report_stylesheet : 
	 * The style sheet to be used to transform a report from XML format. Both the path and name of the style sheet are optional in this parameter. Should no individual style sheet be available, then the default style sheet can be used. The path can be specified relative to the JobScheduler installation directory.
	 *
	 */
	@JSOptionDefinition(name = "scheduler_order_report_stylesheet", description = "", key = "scheduler_order_report_stylesheet", type = "SOSOptionString", mandatory = false)
	public SOSOptionString	scheduler_order_report_stylesheet	= new SOSOptionString(this, conClassName + ".scheduler_order_report_stylesheet", // HashMap-Key
																		"", // Titel
																		"config/default.xslt", // InitValue
																		"config/default.xslt", // DefaultValue
																		false // isMandatory
																);

	/**
	 * \brief getscheduler_order_report_stylesheet : 
	 * 
	 * \details
	 * The style sheet to be used to transform a report from XML format. Both the path and name of the style sheet are optional in this parameter. Should no individual style sheet be available, then the default style sheet can be used. The path can be specified relative to the JobScheduler installation directory.
	 *
	 * \return 
	 *
	 */
	public SOSOptionString getscheduler_order_report_stylesheet() {
		return scheduler_order_report_stylesheet;
	}

	/**
	 * \brief setscheduler_order_report_stylesheet : 
	 * 
	 * \details
	 * The style sheet to be used to transform a report from XML format. Both the path and name of the style sheet are optional in this parameter. Should no individual style sheet be available, then the default style sheet can be used. The path can be specified relative to the JobScheduler installation directory.
	 *
	 * @param scheduler_order_report_stylesheet : 
	 */
	public void setscheduler_order_report_stylesheet(final SOSOptionString p_scheduler_order_report_stylesheet) {
		scheduler_order_report_stylesheet = p_scheduler_order_report_stylesheet;
	}

	public SOSOptionString	report_stylesheet				= (SOSOptionString) scheduler_order_report_stylesheet.SetAlias(conClassName + ".report_stylesheet");

	/**
	 * \var scheduler_order_report_subject : report_subject
	 * The subject of e-mails to be used to send reports. This field is mandatory should reports be sent by e-mail. The following placeholders can be used, surrounded by square [ ] brackets - e.g. [date]: [date] - the current date [datetime] - the current date and time [orderid] - the order ID [jobname] - the job name [taskid] - the task ID
	 *
	 */
	@JSOptionDefinition(name = "scheduler_order_report_subject", description = "report_subject", key = "scheduler_order_report_subject", type = "SOSOptionString", mandatory = false)
	public SOSOptionString	scheduler_order_report_subject	= new SOSOptionString(this, conClassName + ".scheduler_order_report_subject", // HashMap-Key
																	"report_subject", // Titel
																	" ", // InitValue
																	" ", // DefaultValue
																	false // isMandatory
															);

	/**
	 * \brief getscheduler_order_report_subject : report_subject
	 * 
	 * \details
	 * The subject of e-mails to be used to send reports. This field is mandatory should reports be sent by e-mail. The following placeholders can be used, surrounded by square [ ] brackets - e.g. [date]: [date] - the current date [datetime] - the current date and time [orderid] - the order ID [jobname] - the job name [taskid] - the task ID
	 *
	 * \return report_subject
	 *
	 */
	public SOSOptionString getscheduler_order_report_subject() {
		return scheduler_order_report_subject;
	}

	/**
	 * \brief setscheduler_order_report_subject : report_subject
	 * 
	 * \details
	 * The subject of e-mails to be used to send reports. This field is mandatory should reports be sent by e-mail. The following placeholders can be used, surrounded by square [ ] brackets - e.g. [date]: [date] - the current date [datetime] - the current date and time [orderid] - the order ID [jobname] - the job name [taskid] - the task ID
	 *
	 * @param scheduler_order_report_subject : report_subject
	 */
	public void setscheduler_order_report_subject(final SOSOptionString p_scheduler_order_report_subject) {
		scheduler_order_report_subject = p_scheduler_order_report_subject;
	}

	public SOSOptionString	report_subject	= (SOSOptionString) scheduler_order_report_subject.SetAlias(conClassName + ".report_subject");

	public JobSchedulerManagedDBReportJobOptionsSuperClass() {
		objParentClass = this.getClass();
	} // public JobSchedulerManagedDBReportJobOptionsSuperClass

	public JobSchedulerManagedDBReportJobOptionsSuperClass(final JSListener pobjListener) {
		this();
		this.registerMessageListener(pobjListener);
	} // public JobSchedulerManagedDBReportJobOptionsSuperClass

	//

	public JobSchedulerManagedDBReportJobOptionsSuperClass(final HashMap<String, String> JSSettings) throws Exception {
		this();
		this.setAllOptions(JSSettings);
	} // public JobSchedulerManagedDBReportJobOptionsSuperClass (HashMap JSSettings)

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
	@Override
	public void setAllOptions(final HashMap<String, String> pobjJSSettings) {
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
	public void CommandLineArgs(final String[] pstrArgs) {
		super.CommandLineArgs(pstrArgs);
		this.setAllOptions(super.objSettings);
	}
} // public class JobSchedulerManagedDBReportJobOptionsSuperClass