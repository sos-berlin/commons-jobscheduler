package sos.scheduler.db;
import java.util.HashMap;

import org.apache.log4j.Logger;

import com.sos.CredentialStore.SOSCredentialStoreSuperClass;
import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.SOSOptionCommandString;
import com.sos.JSHelper.Options.SOSOptionConnectionString;
import com.sos.JSHelper.Options.SOSOptionElement;
import com.sos.JSHelper.Options.SOSOptionHostName;
import com.sos.JSHelper.Options.SOSOptionPassword;
import com.sos.JSHelper.Options.SOSOptionPortNumber;
import com.sos.JSHelper.Options.SOSOptionRegExp;
import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.JSHelper.Options.SOSOptionStringValueList;
import com.sos.JSHelper.Options.SOSOptionUrl;
import com.sos.JSHelper.Options.SOSOptionUserName;
import com.sos.VirtualFileSystem.Interfaces.ISOSCmdShellOptions;

/**
 * \class 		SOSSQLPlusJobOptionsSuperClass - Start SQL*Plus client and execute sql*plus programs
 *
 * \brief
 * An Options-Super-Class with all Options. This Class will be extended by the "real" Options-class (\see SOSSQLPlusJobOptions.
 * The "real" Option class will hold all the things, which are normaly overwritten at a new generation
 * of the super-class.
 *
 *

 *
 * see \see R:\backup\sos\java\development\com.sos.scheduler\src\sos\scheduler\jobdoc\SOSSQLPlusJob.xml for (more) details.
 *
 * \verbatim ;
 * mechanicaly created by  from http://www.sos-berlin.com at 20120927163810
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
	pobjHM.put ("		SOSSQLPlusJobOptionsSuperClass.auth_file", "test");  // This parameter specifies the path and name of a user's pr
		return pobjHM;
  }  //  private void SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM)
 * \endverbatim
 */
@JSOptionClass(
				name = "SOSSQLPlusJobOptionsSuperClass",
				description = "SOSSQLPlusJobOptionsSuperClass")
public class SOSSQLPlusJobOptionsSuperClass extends SOSCredentialStoreSuperClass implements ISOSCmdShellOptions {
	/**
	 *
	 */
	private static final long	serialVersionUID	= 7532723066179760236L;
	private final String		conClassName		= "SOSSQLPlusJobOptionsSuperClass";
	@SuppressWarnings("unused")
	private static Logger		logger				= Logger.getLogger(SOSSQLPlusJobOptionsSuperClass.class);
	/**
	 * \option Start_Shell_command
	 * \type SOSOptionString
	 * \brief Start_Shell_command - Command to start a command shell
	 *
	 * \details
	 * Command to start a command shell
	 *
	 * \mandatory: false
	 *
	 * \created 30.11.2012 11:38:13 by KB
	 */
	@JSOptionDefinition(
						name = "Start_Shell_command",
						description = "Command to start a command shell",
						key = "Start_Shell_command",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionString		Start_Shell_command	= new SOSOptionString( // ...
															this, // ....
															conClassName + ".Start_Shell_command", // ...
															"Command to start a command shell", // ...
															"", // ...
															"", // ...
															false);

	/* (non-Javadoc)
	 * @see sos.scheduler.db.ISOSCmdShellOptions#getStart_Shell_command()
	 */
	@Override public SOSOptionString getStart_Shell_command() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getStart_Shell_command";
		return Start_Shell_command;
	} // public String getStart_Shell_command

	@Override public void setStart_Shell_command(final SOSOptionString pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setStart_Shell_command";
		Start_Shell_command = pstrValue;
	} // public SOSSQLPlusJobOptionsSuperClass setStart_Shell_command
	/**
	 * \option OS_Name
	 * \type SOSOptionString
	 * \brief OS_Name - Name of Operating-System
	 *
	 * \details
	 * Name of Operating-System
	 *
	 * \mandatory: false
	 *
	 * \created 30.11.2012 11:44:16 by KB
	 */
	@JSOptionDefinition(
						name = "OS_Name",
						description = "Name of Operating-System",
						key = "OS_Name",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionString	OS_Name	= new SOSOptionString( // ...
											this, // ....
											conClassName + ".OS_Name", // ...
											"Name of Operating-System", // ...
											"", // ...
											"", // ...
											false);

	/* (non-Javadoc)
	 * @see sos.scheduler.db.ISOSCmdShellOptions#getOS_Name()
	 */
	@Override public SOSOptionString getOS_Name() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getOS_Name";
		return OS_Name;
	} // public String getOS_Name

	@Override public void setOS_Name(final SOSOptionString pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setOS_Name";
		OS_Name = pstrValue;
	} // public SOSSQLPlusJobOptionsSuperClass setOS_Name
	/**
	 * \option Start_Shell_command_Parameter
	 * \type SOSOptionString
	 * \brief Start_Shell_command_Parameter - Additional Parameters for Start_Shell_command
	 *
	 * \details
	 * Additional Parameters for Shell command
	 *
	 * \mandatory: false
	 *
	 * \created 30.11.2012 11:40:11 by KB
	 */
	@JSOptionDefinition(
						name = "Start_Shell_command_Parameter",
						description = "Additional Parameters for Shell command",
						key = "Start_Shell_command_Parameter",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionString	Start_Shell_command_Parameter	= new SOSOptionString( // ...
																	this, // ....
																	conClassName + ".Start_Shell_command_Parameter", // ...
																	"Additional Parameters for Shell command", // ...
																	"", // ...
																	"", // ...
																	false);

	/* (non-Javadoc)
	 * @see sos.scheduler.db.ISOSCmdShellOptions#getStart_Shell_command_Parameter()
	 */
	@Override public SOSOptionString getStart_Shell_command_Parameter() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getStart_Shell_command_Parameter";
		return Start_Shell_command_Parameter;
	} // public String getStart_Shell_command_Parameter

	@Override public void setStart_Shell_command_Parameter(final SOSOptionString pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setStart_Shell_command_Parameter";
		Start_Shell_command_Parameter = pstrValue;
	} // public SOSSQLPlusJobOptionsSuperClass setStart_Shell_command_Parameter
	/**
	 * \option Shell_command_Parameter
	 * \type SOSOptionString
	 * \brief Shell_command_Parameter - Additional Parameters for Shell_command
	 *
	 * \details
	 * Additional Parameters for Shell command
	 *
	 * \mandatory: false
	 *
	 * \created 30.11.2012 11:40:11 by KB
	 */
	@JSOptionDefinition(
						name = "Shell_command_Parameter",
						description = "Additional Parameters for Shell command",
						key = "Shell_command_Parameter",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionString	Shell_command_Parameter	= new SOSOptionString( // ...
															this, // ....
															conClassName + ".Shell_command_Parameter", // ...
															"Additional Parameters for Shell command", // ...
															"", // ...
															"", // ...
															false);

	/* (non-Javadoc)
	 * @see sos.scheduler.db.ISOSCmdShellOptions#getShell_command_Parameter()
	 */
	@Override public SOSOptionString getShell_command_Parameter() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getShell_command_Parameter";
		return Shell_command_Parameter;
	} // public String getShell_command_Parameter

	@Override public void setShell_command_Parameter(final SOSOptionString pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setShell_command_Parameter";
		Shell_command_Parameter = pstrValue;
	} // public SOSSQLPlusJobOptionsSuperClass setShell_command_Parameter
	/**
	 * \option ignore_ora_messages
	 * \type SOSOptionStringValueList
	 * \brief ignore_ora_messages - Ignore ORA Messages
	 *
	 * \details
	 * Ignore ORA MEssages
	 *
	 * \mandatory: false
	 *
	 * \created 28.09.2012 16:53:50 by KB
	 */
	@JSOptionDefinition(
						name = "ignore_ora_messages",
						description = "Ignore ORA MEssages",
						key = "ignore_ora_messages",
						type = "SOSOptionStringValueList",
						mandatory = false)
	public SOSOptionStringValueList	ignore_ora_messages	= new SOSOptionStringValueList( // ...
																this, // ....
																conClassName + ".ignore_ora_messages", // ...
																"Ignore ORA MEssages", // ...
																"", // ...
																"", // ...
																false);

	public String getignore_ora_messages() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getignore_ora_messages";
		return ignore_ora_messages.Value();
	} // public String getignore_ora_messages

	public void setignore_ora_messages(final String pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setignore_ora_messages";
		ignore_ora_messages.Value(pstrValue);
	} // public SOSSQLPlusJobOptionsSuperClass setignore_ora_messages
	/**
	 * \option ignore_sp2_messages
	 * \type SOSOptionValueList
	 * \brief ignore_sp2_messages - List of messages to ignore or *all
	 *
	 * \details
	 * List of messages to ignore or *all
	 *
	 * \mandatory: false
	 *
	 * \created 28.09.2012 16:34:40 by KB
	 */
	@JSOptionDefinition(
						name = "ignore_sp2_messages",
						description = "List of messages to ignore or *all",
						key = "ignore_sp2_messages",
						type = "SOSOptionValueList",
						mandatory = false)
	public SOSOptionStringValueList	ignore_sp2_messages	= new SOSOptionStringValueList( // ...
																this, // ....
																conClassName + ".ignore_sp2_messages", // ...
																"List of messages to ignore or *all", // ...
																"", // ...
																"", // ...
																false);

	public String getignore_sp2_messages() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getignore_sp2_messages";
		return ignore_sp2_messages.Value();
	} // public String getignore_sp2_messages

	public void setignore_sp2_messages(final String pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setignore_sp2_messages";
		ignore_sp2_messages.Value(pstrValue);
	} // public SOSSQLPlusJobOptionsSuperClass setignore_sp2_messages
	/**
	 * \option ConnectionString
	 * \type SOSOptionString
	 * \brief ConnectionString - The connection String which is used to connect to the database
	 *
	 * \details
	 * The connection String which is used to connect to the database
	 *
	 * \mandatory: false
	 *
	 * \created 11.07.2014 13:44:57 by KB
	 */
	@JSOptionDefinition(
						name = "ConnectionString",
						description = "The connection String which is used to connect to the database",
						key = "ConnectionString",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionConnectionString	ConnectionString	= new SOSOptionConnectionString( // ...
														this, // ....
														conClassName + ".ConnectionString", // ...
														"The connection String which is used to connect to the database", // ...
														"", // ...
														"", // ...
														false);

	public SOSOptionConnectionString getConnectionString() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getConnectionString";
		return ConnectionString;
	} // public String getConnectionString

	public SOSSQLPlusJobOptionsSuperClass setConnectionString(final SOSOptionConnectionString pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setConnectionString";
		ConnectionString = pstrValue;
		return this;
	} // public SOSSQLPlusJobOptionsSuperClass setConnectionString
	/**
	 * \var db_url : URL for connection to database jdbc url (e.g.
	 * jdbc url (e.g. jdbc:oracle:thin:@localhost:1521:XE)
	 *
	 */
	@JSOptionDefinition(
						name = "db_url",
						description = "URL for connection to database jdbc url (e.g.",
						key = "db_url",
						type = "SOSOptionString",
						mandatory = true)
	public SOSOptionUrl	db_url	= new SOSOptionUrl(this, conClassName + ".db_url", // HashMap-Key
										"URL for connection to database jdbc url (e.g.", // Titel
										"", // InitValue
										"", // DefaultValue
										true // isMandatory
								);

	/**
	 * \brief getdb_url : URL for connection to database jdbc url (e.g.
	 *
	 * \details
	 * jdbc url (e.g. jdbc:oracle:thin:@localhost:1521:XE)
	 *
	 * \return URL for connection to database jdbc url (e.g.
	 *
	 */
	public SOSOptionUrl getdb_url() {
		return db_url;
	}

	/**
	 * \brief setdb_url : URL for connection to database jdbc url (e.g.
	 *
	 * \details
	 * jdbc url (e.g. jdbc:oracle:thin:@localhost:1521:XE)
	 *
	 * @param db_url : URL for connection to database jdbc url (e.g.
	 */
	public void setdb_url(final SOSOptionUrl p_db_url) {
		db_url = p_db_url;
	}
	/**
	 * \var command_script_file : Script file name to Execute The va
	 * The value of this parameter contains the file-name (and path-name, if needed) of a local (script-)file, which will be transferred to the remote host and will then be executed there. The script can access job- and order-parameters by environment variables. The names of the environment variables are in upper case and have the string "SCHEDULER_PARAM_" as a prefix. Order parameters with the same name overwrite task parameters. This parameter can be used as an alternative to command , command_delimiter and command_script .
	 *
	 */
	@JSOptionDefinition(
						name = "command_script_file",
						description = "Script file name to Execute The va",
						key = "command_script_file",
						type = "SOSOptionInFileName",
						mandatory = false)
	public SOSOptionCommandString	command_script_file	= new SOSOptionCommandString(this, conClassName + ".command_script_file", // HashMap-Key
																"Script file name to Execute The va", // Titel
																"", // InitValue
																"", // DefaultValue
																true // isMandatory
														);

	/* (non-Javadoc)
	 * @see sos.scheduler.db.ISOSCmdShellOptions#getcommand_script_file()
	 */
	@Override public SOSOptionCommandString getcommand_script_file() {
		return command_script_file;
	}

	/* (non-Javadoc)
	 * @see sos.scheduler.db.ISOSCmdShellOptions#setcommand_script_file(com.sos.JSHelper.Options.SOSOptionCommandString)
	 */
	@Override public void setcommand_script_file(final SOSOptionCommandString p_command_script_file) {
		command_script_file = p_command_script_file;
	}
	public SOSOptionCommandString	sql_script_file				= (SOSOptionCommandString) command_script_file.SetAlias(conClassName + ".sql_script_file");
	/**
	 * \var variable_parser_reg_expr : variable_parser_reg_expr
	 *
	 *
	 */
	@JSOptionDefinition(
						name = "variable_parser_reg_expr",
						description = "variable_parser_reg_expr",
						key = "variable_parser_reg_expr",
						type = "SOSOptionRegExp",
						mandatory = false)
	public SOSOptionRegExp			variable_parser_reg_expr	= new SOSOptionRegExp(this, conClassName + ".variable_parser_reg_expr", // HashMap-Key
																		"variable_parser_reg_expr", // Titel
																		"^SET\\s+([^\\s]+)\\s*IS\\s+(.*)$", // InitValue
																		"^SET\\s+([^\\s]+)\\s*IS\\s+(.*)$", // DefaultValue
																		false // isMandatory
																);

	/**
	 * \brief getvariable_parser_reg_expr : variable_parser_reg_expr
	 *
	 * \details
	 *
	 *
	 * \return variable_parser_reg_expr
	 *
	 */
	public SOSOptionRegExp getvariable_parser_reg_expr() {
		return variable_parser_reg_expr;
	}

	/**
	 * \brief setvariable_parser_reg_expr : variable_parser_reg_expr
	 *
	 * \details
	 *
	 *
	 * @param variable_parser_reg_expr : variable_parser_reg_expr
	 */
	public void setvariable_parser_reg_expr(final SOSOptionRegExp p_variable_parser_reg_expr) {
		variable_parser_reg_expr = p_variable_parser_reg_expr;
	}
	public SOSOptionRegExp	VariableParserRegExpr	= (SOSOptionRegExp) variable_parser_reg_expr.SetAlias(conClassName + ".VariableParserRegExpr");
	/**
	 * \var Command_Line_options : Command_Line_options
	 *
	 * Options used by shell_command
	 *
	 */
	@JSOptionDefinition(
						name = "Command_Line_options",
						description = "Command_Line_options",
						key = "Command_Line_options",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionString	CommandLineOptions		= new SOSOptionString(this, conClassName + ".Command_Line_options", // HashMap-Key
															"Command_Line_options", // Titel
															"-S -L", // InitValue
															"-S -L", // DefaultValue
															false // isMandatory
													);

	/* (non-Javadoc)
	 * @see sos.scheduler.db.ISOSCmdShellOptions#getCommand_Line_options()
	 */
	@Override public SOSOptionString getCommand_Line_options() {
		return CommandLineOptions;
	}

	/* (non-Javadoc)
	 * @see sos.scheduler.db.ISOSCmdShellOptions#setCommand_Line_options(com.sos.JSHelper.Options.SOSOptionString)
	 */
	@Override public void setCommand_Line_options(final SOSOptionString p_Command_Line_options) {
		CommandLineOptions = p_Command_Line_options;
	}
	/**
	 * \var db_password : database password
	 * database password
	 *
	 */
	@JSOptionDefinition(
						name = "db_password",
						description = "database password",
						key = "db_password",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionPassword	db_password	= new SOSOptionPassword(this, conClassName + ".db_password", // HashMap-Key
													"database password", // Titel
													"", // InitValue
													"", // DefaultValue
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
	 * \var db_user : database user
	 * database user
	 *
	 */
	@JSOptionDefinition(
						name = "db_user",
						description = "database user",
						key = "db_user",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionUserName	db_user	= new SOSOptionUserName(this, conClassName + ".db_user", // HashMap-Key
												"database user", // Titel
												"", // InitValue
												"", // DefaultValue
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
	 * \var include_files : IncludeFiles
	 *
	 *
	 */
	@JSOptionDefinition(
						name = "include_files",
						description = "IncludeFiles",
						key = "include_files",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionString	include_files	= new SOSOptionString(this, conClassName + ".include_files", // HashMap-Key
													"IncludeFiles", // Titel
													"", // InitValue
													"", // DefaultValue
													false // isMandatory
											);

	/**
	 * \brief getinclude_files : IncludeFiles
	 *
	 * \details
	 *
	 *
	 * \return IncludeFiles
	 *
	 */
	public SOSOptionString getinclude_files() {
		return include_files;
	}

	/**
	 * \brief setinclude_files : IncludeFiles
	 *
	 * \details
	 *
	 *
	 * @param include_files : IncludeFiles
	 */
	public void setinclude_files(final SOSOptionString p_include_files) {
		include_files = p_include_files;
	}
	/**
	 * \var shell_command :
	 *
	 *
	 */
	@JSOptionDefinition(
						name = "shell_command",
						description = "",
						key = "shell_command",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionString	shell_command	= new SOSOptionString(this, conClassName + ".shell_command", // HashMap-Key
													"", // Titel
													"sqlplus", // InitValue
													"sqlplus", // DefaultValue
													false // isMandatory
											);

	/* (non-Javadoc)
	 * @see sos.scheduler.db.ISOSCmdShellOptions#getshell_command()
	 */
	@Override public SOSOptionString getshell_command() {
		return shell_command;
	}

	/* (non-Javadoc)
	 * @see sos.scheduler.db.ISOSCmdShellOptions#setshell_command(com.sos.JSHelper.Options.SOSOptionString)
	 */
	@Override public void setshell_command(final SOSOptionString p_shell_command) {
		shell_command = p_shell_command;
	}
	/**
	 * \var sql_error : sql_error
	 *
	 *
	 */
	@JSOptionDefinition(
						name = "sql_error",
						description = "sql_error",
						key = "sql_error",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionString	sql_error	= new SOSOptionString(this, conClassName + ".sql_error", // HashMap-Key
												"sql_error", // Titel
												"", // InitValue
												"", // DefaultValue
												false // isMandatory
										);

	/**
	 * \brief getsql_error : sql_error
	 *
	 * \details
	 *
	 *
	 * \return sql_error
	 *
	 */
	public SOSOptionString getsql_error() {
		return sql_error;
	}

	/**
	 * \brief setsql_error : sql_error
	 *
	 * \details
	 *
	 *
	 * @param sql_error : sql_error
	 */
	public void setsql_error(final SOSOptionString p_sql_error) {
		sql_error = p_sql_error;
	}

	public SOSSQLPlusJobOptionsSuperClass() {
		objParentClass = this.getClass();
	} // public SOSSQLPlusJobOptionsSuperClass

	public SOSSQLPlusJobOptionsSuperClass(final JSListener pobjListener) {
		this();
		this.registerMessageListener(pobjListener);
	} // public SOSSQLPlusJobOptionsSuperClass

	//
	public SOSSQLPlusJobOptionsSuperClass(final HashMap<String, String> JSSettings) throws Exception {
		this();
		this.setAllOptions(JSSettings);
	} // public SOSSQLPlusJobOptionsSuperClass (HashMap JSSettings)

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
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getAllOptionsAsString";
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
	@Override public void setAllOptions(final HashMap<String, String> pobjJSSettings) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setAllOptions";
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
	@Override public void CheckMandatory() throws JSExceptionMandatoryOptionMissing //
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
	@Override public void CommandLineArgs(final String[] pstrArgs) {
		super.CommandLineArgs(pstrArgs);
		this.setAllOptions(super.objSettings);
	}

//	@Override
//	public SOSOptionElement getUrl() {
//		return ConnectionString;
//	}
//
	@Override public void setUrl(final SOSOptionElement pstrValue) {
		ConnectionString.Set(pstrValue);
	}

	@Override public SOSOptionHostName getHost() {
		return null;
	}

	@Override public void setHost(final SOSOptionElement p_host) {
	}

	@Override public SOSOptionPortNumber getPort() {
		return null;
	}

	@Override public void setPort(final SOSOptionPortNumber p_port) {
	}

	@Override public SOSOptionUserName getUser() {
		return db_user;
	}

	@Override public SOSOptionPassword getPassword() {
		return db_password;
	}

	@Override public void setPassword(final SOSOptionPassword p_password) {
		db_password.Set(p_password);
	}

	@Override public void setUser(final SOSOptionUserName pobjUser) {
		db_user.Set(pobjUser);
	}
} // public class SOSSQLPlusJobOptionsSuperClass