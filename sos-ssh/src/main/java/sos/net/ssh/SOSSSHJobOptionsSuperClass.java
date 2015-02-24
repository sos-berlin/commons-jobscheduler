package sos.net.ssh;

import java.util.HashMap;

import com.sos.CredentialStore.SOSCredentialStoreImpl;
import com.sos.CredentialStore.Options.ISOSCredentialStoreOptionsBridge;
import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionAuthenticationMethod;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionCommandString;
import com.sos.JSHelper.Options.SOSOptionHostName;
import com.sos.JSHelper.Options.SOSOptionInFileName;
import com.sos.JSHelper.Options.SOSOptionInteger;
import com.sos.JSHelper.Options.SOSOptionIntegerArray;
import com.sos.JSHelper.Options.SOSOptionPassword;
import com.sos.JSHelper.Options.SOSOptionPortNumber;
import com.sos.JSHelper.Options.SOSOptionRegExp;
import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.JSHelper.Options.SOSOptionTransferType;
import com.sos.JSHelper.Options.SOSOptionUrl;
import com.sos.JSHelper.Options.SOSOptionUserName;
import com.sos.JSHelper.interfaces.ISOSConnectionOptions;
import com.sos.VirtualFileSystem.Interfaces.ISOSAuthenticationOptions;
import com.sos.VirtualFileSystem.Interfaces.ISOSShellOptions;
import com.sos.i18n.annotation.I18NResourceBundle;

/**
 * \class SOSSSHJobOptions -
 *
 * \brief An Options-Class with all Options.
 *
 * \section csvdefs CSV-Definitionen f�r alle Optionen
 *
 * Mit den nachfolgenden CSV-Definitionen wurde diese Klasse mit dem Template \c
 * OptionsClass.tpl durch ClaviusXPress automatisch erzeugt. Zum Hinzuf�gen oder
 * �ndern von Optionen m�ssen die folgenden Zeilen in eine Datei extrahiert und
 * mit CxP diese Klasse erneut erzeugt werden.
 *
 * Alternativ kann auch die Datei \c C:/temp/JobSchedulerSSHJob.txt verwendet
 * werden.
 *
 * \verbatim ; mechanicaly created by JobDocu2OptionsClass.xslt from
 * http://www.sos-berlin.com at 2010-05-15-14-28-00 . SourceType=options .
 * PackageName=com.sos.net.ssh . ClassName=SOSSSHJobOptions .
 * Title=Launch remote commands or executable files by SSH . Description=Launch
 * remote commands or executable files by SSH .
 * ExtendsClassName=com.sos.DataSwitchHelper.SOSOptionsClass . keywords=Options
 * . Category=Options|JobScheduler-API|API-Job auth_file;auth_file;This
 * parameter specifies the path and name of a user's pr;This parameter specifies
 * the path and name of a user's pr;false;String; ; auth_method;auth_method;This
 * parameter specifies the authorization method for the;This parameter specifies
 * the authorization method for the;false;String;publickey;publickey
 * command;command;This parameter specifies a command that is to be
 * executed;This parameter specifies a command that is to be
 * executed;false;String; ; command_delimiter;command_delimiter;Command
 * delimiter characters are specified using this par;Command delimiter
 * characters are specified using this par;true;String;%%;%%
 * command_script;command_script;This parameter can be used as an alternative to
 * command,;This parameter can be used as an alternative to
 * command,;false;String; ; command_script_file;command_script_file;This
 * parameter can be used as an alternative to command,;This parameter can be
 * used as an alternative to command,;false;String; ;
 * command_script_param;command_script_param;This parameter contains a
 * parameterstring, which will be;This parameter contains a parameterstring,
 * which will be;false;String; ; host;host;This parameter specifies the hostname
 * or IP address of th;This parameter specifies the hostname or IP address of
 * th;true;String; ; ignore_error;ignore_error;Should the value true be
 * specified, then execution errors;Should the value true be specified, then
 * execution errors;false;String;false;false
 * ignore_exit_code;ignore_exit_code;This parameter configures one or more exit
 * codes which wi;This parameter configures one or more exit codes which
 * wi;false;String; ; ignore_signal;ignore_signal;Should the value true be
 * specified, then on;Should the value true be specified, then
 * on;false;String;false;false ignore_stderr;ignore_stderr;This job checks if
 * any output to stderr has been created;This job checks if any output to stderr
 * has been created;false;String;false;false password;password;This parameter
 * specifies the user account password for au;This parameter specifies the user
 * account password for au;false;String; ; port;port;This parameter specifies
 * the port number of the SSH serve;This parameter specifies the port number of
 * the SSH serve;true;String;22;22 proxy_host;proxy_host;The value of this
 * parameter is the host name or the IP ad;The value of this parameter is the
 * host name or the IP ad;false;String; ; proxy_password;proxy_password;This
 * parameter specifies the password for the proxy serve;This parameter specifies
 * the password for the proxy serve;false;String; ; proxy_port;proxy_port;This
 * parameter specifies the port number of the proxy,;This parameter specifies
 * the port number of the proxy,;false;String; ; proxy_user;proxy_user;The value
 * of this parameter specifies the user account fo;The value of this parameter
 * specifies the user account fo;false;String; ;
 * simulate_shell;simulate_shell;Should the value true be specified for this
 * parameter,;Should the value true be specified for this
 * parameter,;false;String;false;false
 * simulate_shell_inactivity_timeout;simulate_shell_inactivity_timeout;If no new
 * characters are written to stdout or stderr afte;If no new characters are
 * written to stdout or stderr afte;false;String; ;
 * simulate_shell_login_timeout;simulate_shell_login_timeout;If no new
 * characters are written to stdout or stderr afte;If no new characters are
 * written to stdout or stderr afte;false;String; ;
 * simulate_shell_prompt_trigger;simulate_shell_prompt_trigger;The expected
 * comman line prompt. Using this prompt the jo;The expected comman line prompt.
 * Using this prompt the jo;false;String; ; user;user;This parameter specifies
 * the user account to be used when;This parameter specifies the user account to
 * be used when;true;String; ; \endverbatim \section OptionsTable Tabelle der
 * vorhandenen Optionen
 * <TABLE border="1">
 * <CAPTION>Tabelle mit allen Optionen</CAPTION>
 * <TR style="bold">
 * <TD><b>MethodName</b></TD>
 * <TD><b>Title</b></TD>
 * <TD><b>Setting</b></TD>
 * <TD><b>Description</b></TD>
 * <TD><b>IsMandatory</b></TD>
 * <TD><b>DataType</b></TD>
 * <TD align="center"><b>InitialValue</b></TD>
 * <TD align="center"><b>TestValue</b></TD>
 * </TR>
 * <TR>
 * <TD>\see ::auth_file</TD>
 * <TD>This parameter specifies the path and name of a user's pr</TD>
 * <TD>Auth_file</TD>
 * <TD>This parameter specifies the path and name of a user's pr</TD>
 * <TD>false</TD>
 * <TD>String</TD>
 * <TD align="center">n.a.</TD>
 * <TD align="center">n.a.</TD>
 * </TR>
 * <TR>
 * <TD>\see ::auth_method</TD>
 * <TD>This parameter specifies the authorization method for the</TD>
 * <TD>Auth_method</TD>
 * <TD>This parameter specifies the authorization method for the</TD>
 * <TD>false</TD>
 * <TD>String</TD>
 * <TD align="center">publickey</TD>
 * <TD align="center">publickey</TD>
 * </TR>
 * <TR>
 * <TD>\see ::command</TD>
 * <TD>This parameter specifies a command that is to be executed</TD>
 * <TD>Command</TD>
 * <TD>This parameter specifies a command that is to be executed</TD>
 * <TD>false</TD>
 * <TD>String</TD>
 * <TD align="center">n.a.</TD>
 * <TD align="center">n.a.</TD>
 * </TR>
 * <TR>
 * <TD>\see ::command_delimiter</TD>
 * <TD>Command delimiter characters are specified using this par</TD>
 * <TD>Command_delimiter</TD>
 * <TD>Command delimiter characters are specified using this par</TD>
 * <TD>true</TD>
 * <TD>String</TD>
 * <TD align="center">%%</TD>
 * <TD align="center">%%</TD>
 * </TR>
 * <TR>
 * <TD>\see ::command_script</TD>
 * <TD>This parameter can be used as an alternative to command,</TD>
 * <TD>Command_script</TD>
 * <TD>This parameter can be used as an alternative to command,</TD>
 * <TD>false</TD>
 * <TD>String</TD>
 * <TD align="center">n.a.</TD>
 * <TD align="center">n.a.</TD>
 * </TR>
 * <TR>
 * <TD>\see ::command_script_file</TD>
 * <TD>This parameter can be used as an alternative to command,</TD>
 * <TD>Command_script_file</TD>
 * <TD>This parameter can be used as an alternative to command,</TD>
 * <TD>false</TD>
 * <TD>String</TD>
 * <TD align="center">n.a.</TD>
 * <TD align="center">n.a.</TD>
 * </TR>
 * <TR>
 * <TD>\see ::command_script_param</TD>
 * <TD>This parameter contains a parameterstring, which will be</TD>
 * <TD>Command_script_param</TD>
 * <TD>This parameter contains a parameterstring, which will be</TD>
 * <TD>false</TD>
 * <TD>String</TD>
 * <TD align="center">n.a.</TD>
 * <TD align="center">n.a.</TD>
 * </TR>
 * <TR>
 * <TD>\see ::host</TD>
 * <TD>This parameter specifies the hostname or IP address of th</TD>
 * <TD>Host</TD>
 * <TD>This parameter specifies the hostname or IP address of th</TD>
 * <TD>true</TD>
 * <TD>String</TD>
 * <TD align="center">n.a.</TD>
 * <TD align="center">n.a.</TD>
 * </TR>
 * <TR>
 * <TD>\see ::ignore_error</TD>
 * <TD>Should the value true be specified, then execution errors</TD>
 * <TD>Ignore_error</TD>
 * <TD>Should the value true be specified, then execution errors</TD>
 * <TD>false</TD>
 * <TD>String</TD>
 * <TD align="center">false</TD>
 * <TD align="center">false</TD>
 * </TR>
 * <TR>
 * <TD>\see ::ignore_exit_code</TD>
 * <TD>This parameter configures one or more exit codes which wi</TD>
 * <TD>Ignore_exit_code</TD>
 * <TD>This parameter configures one or more exit codes which wi</TD>
 * <TD>false</TD>
 * <TD>String</TD>
 * <TD align="center">n.a.</TD>
 * <TD align="center">n.a.</TD>
 * </TR>
 * <TR>
 * <TD>\see ::ignore_signal</TD>
 * <TD>Should the value true be specified, then on</TD>
 * <TD>Ignore_signal</TD>
 * <TD>Should the value true be specified, then on</TD>
 * <TD>false</TD>
 * <TD>String</TD>
 * <TD align="center">false</TD>
 * <TD align="center">false</TD>
 * </TR>
 * <TR>
 * <TD>\see ::ignore_stderr</TD>
 * <TD>This job checks if any output to stderr has been created</TD>
 * <TD>Ignore_stderr</TD>
 * <TD>This job checks if any output to stderr has been created</TD>
 * <TD>false</TD>
 * <TD>String</TD>
 * <TD align="center">false</TD>
 * <TD align="center">false</TD>
 * </TR>
 * <TR>
 * <TD>\see ::password</TD>
 * <TD>This parameter specifies the user account password for au</TD>
 * <TD>Password</TD>
 * <TD>This parameter specifies the user account password for au</TD>
 * <TD>false</TD>
 * <TD>String</TD>
 * <TD align="center">n.a.</TD>
 * <TD align="center">n.a.</TD>
 * </TR>
 * <TR>
 * <TD>\see ::port</TD>
 * <TD>This parameter specifies the port number of the SSH serve</TD>
 * <TD>Port</TD>
 * <TD>This parameter specifies the port number of the SSH serve</TD>
 * <TD>true</TD>
 * <TD>String</TD>
 * <TD align="center">22</TD>
 * <TD align="center">22</TD>
 * </TR>
 * <TR>
 * <TD>\see ::proxy_host</TD>
 * <TD>The value of this parameter is the host name or the IP ad</TD>
 * <TD>Proxy_host</TD>
 * <TD>The value of this parameter is the host name or the IP ad</TD>
 * <TD>false</TD>
 * <TD>String</TD>
 * <TD align="center">n.a.</TD>
 * <TD align="center">n.a.</TD>
 * </TR>
 * <TR>
 * <TD>\see ::proxy_password</TD>
 * <TD>This parameter specifies the password for the proxy serve</TD>
 * <TD>Proxy_password</TD>
 * <TD>This parameter specifies the password for the proxy serve</TD>
 * <TD>false</TD>
 * <TD>String</TD>
 * <TD align="center">n.a.</TD>
 * <TD align="center">n.a.</TD>
 * </TR>
 * <TR>
 * <TD>\see ::proxy_port</TD>
 * <TD>This parameter specifies the port number of the proxy,</TD>
 * <TD>Proxy_port</TD>
 * <TD>This parameter specifies the port number of the proxy,</TD>
 * <TD>false</TD>
 * <TD>String</TD>
 * <TD align="center">n.a.</TD>
 * <TD align="center">n.a.</TD>
 * </TR>
 * <TR>
 * <TD>\see ::proxy_user</TD>
 * <TD>The value of this parameter specifies the user account fo</TD>
 * <TD>Proxy_user</TD>
 * <TD>The value of this parameter specifies the user account fo</TD>
 * <TD>false</TD>
 * <TD>String</TD>
 * <TD align="center">n.a.</TD>
 * <TD align="center">n.a.</TD>
 * </TR>
 * <TR>
 * <TD>\see ::simulate_shell</TD>
 * <TD>Should the value true be specified for this parameter,</TD>
 * <TD>Simulate_shell</TD>
 * <TD>Should the value true be specified for this parameter,</TD>
 * <TD>false</TD>
 * <TD>String</TD>
 * <TD align="center">false</TD>
 * <TD align="center">false</TD>
 * </TR>
 * <TR>
 * <TD>\see ::simulate_shell_inactivity_timeout</TD>
 * <TD>If no new characters are written to stdout or stderr afte</TD>
 * <TD>Simulate_shell_inactivity_timeout</TD>
 * <TD>If no new characters are written to stdout or stderr afte</TD>
 * <TD>false</TD>
 * <TD>String</TD>
 * <TD align="center">n.a.</TD>
 * <TD align="center">n.a.</TD>
 * </TR>
 * <TR>
 * <TD>\see ::simulate_shell_login_timeout</TD>
 * <TD>If no new characters are written to stdout or stderr afte</TD>
 * <TD>Simulate_shell_login_timeout</TD>
 * <TD>If no new characters are written to stdout or stderr afte</TD>
 * <TD>false</TD>
 * <TD>String</TD>
 * <TD align="center">n.a.</TD>
 * <TD align="center">n.a.</TD>
 * </TR>
 * <TR>
 * <TD>\see ::simulate_shell_prompt_trigger</TD>
 * <TD>The expected comman line prompt. Using this prompt the jo</TD>
 * <TD>Simulate_shell_prompt_trigger</TD>
 * <TD>The expected comman line prompt. Using this prompt the jo</TD>
 * <TD>false</TD>
 * <TD>String</TD>
 * <TD align="center">n.a.</TD>
 * <TD align="center">n.a.</TD>
 * </TR>
 * <TR>
 * <TD>\see ::user</TD>
 * <TD>This parameter specifies the user account to be used when</TD>
 * <TD>User</TD>
 * <TD>This parameter specifies the user account to be used when</TD>
 * <TD>true</TD>
 * <TD>String</TD>
 * <TD align="center">n.a.</TD>
 * <TD align="center">n.a.</TD>
 * </TR>
 * </TABLE>
 *
 * \section TestData Eine Hilfe zum Erzeugen einer HashMap mit Testdaten
 *
 * Die folgenden Methode kann verwendet werden, um f�r einen Test eine HashMap
 * mit sinnvollen Werten f�r die einzelnen Optionen zu erzeugen.
 *
* \verbatim
 private HashMap <String, String> SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM) {
	pobjHM.put ("SOSSSHJobOptions.auth_file", "test");  // This parameter specifies the path and name of a user's pr
	pobjHM.put ("SOSSSHJobOptions.auth_method", "publickey");  // This parameter specifies the authorization method for the
	pobjHM.put ("SOSSSHJobOptions.command", "test");  // This parameter specifies a command that is to be executed
	pobjHM.put ("SOSSSHJobOptions.command_delimiter", "%%");  // Command delimiter characters are specified using this par
	pobjHM.put ("SOSSSHJobOptions.command_script", "test");  // This parameter can be used as an alternative to command,
	pobjHM.put ("SOSSSHJobOptions.command_script_file", "test");  // This parameter can be used as an alternative to command,
	pobjHM.put ("SOSSSHJobOptions.command_script_param", "test");  // This parameter contains a parameterstring, which will be
	pobjHM.put ("SOSSSHJobOptions.host", "test");  // This parameter specifies the hostname or IP address of th
	pobjHM.put ("SOSSSHJobOptions.ignore_error", "false");  // Should the value true be specified, then execution errors
	pobjHM.put ("SOSSSHJobOptions.ignore_exit_code", "test");  // This parameter configures one or more exit codes which wi
	pobjHM.put ("SOSSSHJobOptions.ignore_signal", "false");  // Should the value true be specified, then on
	pobjHM.put ("SOSSSHJobOptions.ignore_stderr", "false");  // This job checks if any output to stderr has been created
	pobjHM.put ("SOSSSHJobOptions.password", "test");  // This parameter specifies the user account password for au
	pobjHM.put ("SOSSSHJobOptions.port", "22");  // This parameter specifies the port number of the SSH serve
	pobjHM.put ("SOSSSHJobOptions.proxy_host", "test");  // The value of this parameter is the host name or the IP ad
	pobjHM.put ("SOSSSHJobOptions.proxy_password", "test");  // This parameter specifies the password for the proxy serve
	pobjHM.put ("SOSSSHJobOptions.proxy_port", "test");  // This parameter specifies the port number of the proxy,
	pobjHM.put ("SOSSSHJobOptions.proxy_user", "test");  // The value of this parameter specifies the user account fo
	pobjHM.put ("SOSSSHJobOptions.simulate_shell", "false");  // Should the value true be specified for this parameter,
	pobjHM.put ("SOSSSHJobOptions.simulate_shell_inactivity_timeout", "test");  // If no new characters are written to stdout or stderr afte
	pobjHM.put ("SOSSSHJobOptions.simulate_shell_login_timeout", "test");  // If no new characters are written to stdout or stderr afte
	pobjHM.put ("SOSSSHJobOptions.simulate_shell_prompt_trigger", "test");  // The expected comman line prompt. Using this prompt the jo
	pobjHM.put ("SOSSSHJobOptions.user", "test");  // This parameter specifies the user account to be used when
	return pobjHM;
  }  //  private void SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM)
 * \endverbatim
  */
@I18NResourceBundle(baseName = "com_sos_net_messages", defaultLocale = "en")
@JSOptionClass(name = "SOSSSHJobOptionsSuperClass", description = "Option-Class for a SSH-Connection")
public class SOSSSHJobOptionsSuperClass extends JSOptionsClass implements ISOSConnectionOptions, ISOSAuthenticationOptions, ISOSShellOptions, ISOSCredentialStoreOptionsBridge {
	/**
	 *
	 */
	private static final long	serialVersionUID		= 526076781389979326L;
	private final String		conClassName			= "SOSSSHJobOptions";

	/**
	 * \option url
	 * \type SOSOptionURL
	 * \brief url - the url to which a connection have to be made
	 *
	 * \details
	 * the url for the connection
	 *
	 * \mandatory: false
	 *
	 * \created 30.04.2014 16:32:05 by KB
	 */
	@JSOptionDefinition(
						name = "url",
						description = "the url for the connection",
						key = "url",
						type = "SOSOptionURL",
						mandatory = false)
	public SOSOptionUrl			url					= new SOSOptionUrl( // ...
															this, // ....
															conClassName + ".url", // ...
															"the url for the connection", // ...
															"", // ...
															"", // ...
															false);

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#geturl()
	 */
	public SOSOptionUrl getUrl() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::geturl";
		return url;
	} // public String geturl

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#seturl(com.sos.JSHelper.Options.SOSOptionUrl)
	 */
	public void setUrl(final SOSOptionUrl pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::seturl";
		url = pstrValue;
	} // public SOSConnection2OptionsSuperClass seturl

	/**
	 * \option raise_exception_on_error
	 * \type SOSOptionBoolean
	 * \brief raise_exception_on_error - Raise an exception if an error occured
	 *
	 * \details
	 * Raise an Exception if an error occured
	 *
	 * \mandatory: true
	 *
	 * \created 06.03.2013 16:55:50 by KB
	 */
	@JSOptionDefinition(name = "raise_exception_on_error", description = "Raise an Exception if an error occured", key = "raise_exception_on_error", type = "SOSOptionBoolean", mandatory = true)
	public SOSOptionBoolean		RaiseExceptionOnError	= new SOSOptionBoolean( // ...
																this, // ....
																conClassName + ".raise_exception_on_error", // ...
																"Raise an Exception if an error occured", // ...
																"true", // ...
																"true", // ...
																true);

	public String getraise_exception_on_error() {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getraise_exception_on_error";

		return RaiseExceptionOnError.Value();
	} // public String getraise_exception_on_error

	public SOSSSHJobOptionsSuperClass setraise_exception_on_error(final String pstrValue) {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setraise_exception_on_error";
		RaiseExceptionOnError.Value(pstrValue);
		return this;
	} // public SOSSSHJobOptionsSuperClass setraise_exception_on_error

	/**
	* \var auth_file: This parameter specifies the path and name of a user's pr
	*/
	@JSOptionDefinition(name = "auth_file", description = "auth_file", key = "auth_file", type = "SOSOptionString", mandatory = false)
	public SOSOptionInFileName				auth_file							= new SOSOptionInFileName(this, conClassName + ".auth_file", // HashMap-Key
																						"auth_file", // Titel
																						null, // InitiValue
																						null, // DefaultValue
																						false // isMandatory
																				);	// This parameter specifies the path and name of a user's pr
	/**
	 * \var auth_method: This parameter specifies the authorization method for
	 * the
	 */
	@JSOptionDefinition(name = "auth_method", description = "This parameter specifies the authorization method for the", key = "auth_method", type = "SOSOptionString", mandatory = false)
	public SOSOptionAuthenticationMethod	auth_method							= new SOSOptionAuthenticationMethod(this, conClassName + ".auth_method", // HashMap-Key
																						"auth_method", // Titel
																						"publickey", // InitiValue
																						"publickey", // DefaultValue
																						false // isMandatory
																				);	// This parameter specifies the authorization method for the
	/**
	 * \var command: This parameter specifies a command that is to be executed
	 */
	@JSOptionDefinition(name = "command", description = "This parameter specifies a command that is to be executed", key = "command", type = "SOSOptionString", mandatory = false)
	public SOSOptionCommandString			command								= new SOSOptionCommandString(this, conClassName + ".command", // HashMap-Key
																						"This parameter specifies a command that is to be executed", // Titel
																						null, // InitiValue
																						null, // DefaultValue
																						false // isMandatory
																				);	// This parameter specifies a command that is to be executed
	/**
	 * \var command_delimiter: Command delimiter characters are specified using
	 * this par
	 */
	@JSOptionDefinition(name = "command_delimiter", description = "Command delimiter characters are specified using this par", key = "command_delimiter", type = "SOSOptionString", mandatory = true)
	public SOSOptionRegExp					command_delimiter					= new SOSOptionRegExp(this, conClassName + ".command_delimiter", // HashMap-Key
																						"Command delimiter characters are specified using this par", // Titel
																						"%%", // InitiValue
																						"%%", // DefaultValue
																						true // isMandatory
																				);	// Command delimiter characters are specified using this par
	/**
	 * \var command_script: This parameter can be used as an alternative to
	 * command,
	 */
	@JSOptionDefinition(name = "command_script", description = "This parameter can be used as an alternative to command,", key = "command_script", type = "SOSOptionString", mandatory = false)
	public SOSOptionCommandString			command_script						= new SOSOptionCommandString(this, conClassName + ".command_script", // HashMap-Key
																						"This parameter can be used as an alternative to command,", // Titel
																						null, // InitiValue
																						null, // DefaultValue
																						false // isMandatory
																				);	// This parameter can be used as an alternative to command,
	/**
	 * \var command_script_file: This parameter can be used as an alternative to
	 * command,
	 */
	@JSOptionDefinition(name = "command_script_file", description = "This parameter can be used as an alternative to command,", key = "command_script_file", type = "SOSOptionString", mandatory = false)
	public SOSOptionInFileName				command_script_file					= new SOSOptionInFileName(this, conClassName + ".command_script_file", // HashMap-Key
																						"This parameter can be used as an alternative to command,", // Titel
																						null, // InitiValue
																						null, // DefaultValue
																						false // isMandatory
																				);	// This parameter can be used as an alternative to command,
	/**
	 * \var command_script_param: This parameter contains a parameterstring,
	 * which will be
	 */
	@JSOptionDefinition(name = "command_script_param", description = "This parameter contains a parameterstring, which will be", key = "command_script_param", type = "SOSOptionString", mandatory = false)
	public SOSOptionString					command_script_param				= new SOSOptionString(this, conClassName + ".command_script_param", // HashMap-Key
																						"This parameter contains a parameterstring, which will be", // Titel
																						null, // InitiValue
																						null, // DefaultValue
																						false // isMandatory
																				);	// This parameter contains a parameterstring, which will be
	/**
	 * \var host: This parameter specifies the hostname or IP address of th
	 */
	@JSOptionDefinition(name = "host", description = "This parameter specifies the hostname or IP address of th", key = "host", type = "SOSOptionString", mandatory = true)
	public SOSOptionHostName				host								= new SOSOptionHostName(this, conClassName + ".host", // HashMap-Key
																						"This parameter specifies the hostname or IP address of th", // Titel
																						"localhost", // InitiValue
																						null, // DefaultValue
																						true // isMandatory
																				);	// This parameter specifies the hostname or IP address of th
	public SOSOptionHostName HostName = (SOSOptionHostName) host.SetAlias("host_name", "ssh_server_name");
	
	/**
	 * \var protocol : Type of requested Datatransfer The values ftp, sftp
	 * The values ftp, sftp or ftps are valid for this parameter. If sftp is used, then the ssh_* parameters will be applied.
	 *
	 */
	@JSOptionDefinition(
						name = "protocol",
						description = "Type of requested Datatransfer The values ftp, sftp",
						key = "protocol",
						type = "SOSOptionStringValueList",
						mandatory = true)
	public SOSOptionTransferType	protocol		= new SOSOptionTransferType(this, conClassName + ".protocol", // HashMap-Key
															"Type of requested Datatransfer The values ftp, sftp", // Titel
															"ssh", // InitValue
															"ssh", // DefaultValue
															true // isMandatory
													);
	public SOSOptionTransferType	ftp_protocol	= (SOSOptionTransferType) protocol.SetAlias("ftp_protocol");

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#getprotocol()
	 */
	public SOSOptionTransferType getProtocol() {
		return protocol;
	}

	/* (non-Javadoc)
	 * @see com.sos.VirtualFileSystem.Options.ISOSDataProviderOptions#setprotocol(com.sos.JSHelper.Options.SOSOptionTransferType)
	 */
	public void setProtocol(final SOSOptionTransferType p_protocol) {
		protocol = p_protocol;
	}
	public SOSOptionTransferType	TransferProtocol		= (SOSOptionTransferType) protocol.SetAlias(conClassName + ".TransferProtocol");

	/**
	 * \var ignore_error: Should the value true be specified, then execution
	 * errors
	 */
	@JSOptionDefinition(name = "ignore_error", description = "Should the value true be specified, then execution errors", key = "ignore_error", type = "SOSOptionString", mandatory = false)
	public SOSOptionBoolean					ignore_error						= new SOSOptionBoolean(this, conClassName + ".ignore_error", // HashMap-Key
																						"Should the value true be specified, then execution errors", // Titel
																						"false", // InitiValue
																						"false", // DefaultValue
																						false // isMandatory
																				);	// Should the value true be specified, then execution errors
	/**
	 * \var ignore_exit_code: This parameter configures one or more exit codes
	 * which wi
	 */
	@JSOptionDefinition(name = "ignore_exit_code", description = "This parameter configures one or more exit codes which wi", key = "ignore_exit_code", type = "SOSOptionString", mandatory = false)
	public SOSOptionIntegerArray			ignore_exit_code					= new SOSOptionIntegerArray(this, conClassName + ".ignore_exit_code", // HashMap-Key
																						"This parameter configures one or more exit codes which wi", // Titel
																						null, // InitiValue
																						null, // DefaultValue
																						false // isMandatory
																				);	// This parameter configures one or more exit codes which wi
	/**
	 * \var ignore_signal: Should the value true be specified, then on
	 */
	@JSOptionDefinition(name = "ignore_signal", description = "Should the value true be specified, then on", key = "ignore_signal", type = "SOSOptionString", mandatory = false)
	public SOSOptionBoolean					ignore_signal						= new SOSOptionBoolean(this, conClassName + ".ignore_signal", // HashMap-Key
																						"Should the value true be specified, then on", // Titel
																						"false", // InitiValue
																						"false", // DefaultValue
																						false // isMandatory
																				);	// Should the value true be specified, then on
	/**
	 * \var ignore_stderr: This job checks if any output to stderr has been
	 * created
	 */
	@JSOptionDefinition(name = "ignore_stderr", description = "This job checks if any output to stderr has been created", key = "ignore_stderr", type = "SOSOptionString", mandatory = false)
	public SOSOptionBoolean					ignore_stderr						= new SOSOptionBoolean(this, conClassName + ".ignore_stderr", // HashMap-Key
																						"This job checks if any output to stderr has been created", // Titel
																						"false", // InitiValue
																						"false", // DefaultValue
																						false // isMandatory
																				);	// This job checks if any output to stderr has been created
	/**
	 * \var password: This parameter specifies the user account password for au
	 */
	@JSOptionDefinition(name = "password", description = "This parameter specifies the user account password for au", key = "password", type = "SOSOptionString", mandatory = false)
	public SOSOptionPassword				password							= new SOSOptionPassword(this, conClassName + ".password", // HashMap-Key
																						"This parameter specifies the user account password for au", // Titel
																						null, // InitiValue
																						null, // DefaultValue
																						false // isMandatory
																				);	// This parameter specifies the user account password for au
	/**
	 * \var port: This parameter specifies the port number of the SSH serve
	 */
	@JSOptionDefinition(name = "port", description = "This parameter specifies the port number of the SSH serve", key = "port", type = "SOSOptionString", mandatory = true)
	public SOSOptionPortNumber				port								= new SOSOptionPortNumber(this, conClassName + ".port", // HashMap-Key
																						"This parameter specifies the port number of the SSH serve", // Titel
																						"22", // InitiValue
																						"22", // DefaultValue
																						true // isMandatory
																				);	// This parameter specifies the port number of the SSH serve
	/**
	 * \var proxy_host: The value of this parameter is the host name or the IP
	 * ad
	 */
	@JSOptionDefinition(name = "proxy_host", description = "The value of this parameter is the host name or the IP ad", key = "proxy_host", type = "SOSOptionString", mandatory = false)
	public SOSOptionString					proxy_host							= new SOSOptionString(this, conClassName + ".proxy_host", // HashMap-Key
																						"The value of this parameter is the host name or the IP ad", // Titel
																						null, // InitiValue
																						null, // DefaultValue
																						false // isMandatory
																				);	// The value of this parameter is the host name or the IP ad
	/**
	 * \var proxy_password: This parameter specifies the password for the proxy
	 * serve
	 */
	@JSOptionDefinition(name = "proxy_password", description = "This parameter specifies the password for the proxy serve", key = "proxy_password", type = "SOSOptionString", mandatory = false)
	public SOSOptionPassword				proxy_password						= new SOSOptionPassword(this, conClassName + ".proxy_password", // HashMap-Key
																						"This parameter specifies the password for the proxy serve", // Titel
																						null, // InitiValue
																						null, // DefaultValue
																						false // isMandatory
																				);	// This parameter specifies the password for the proxy serve
	/**
	 * \var proxy_port: This parameter specifies the port number of the proxy,
	 */
	@JSOptionDefinition(name = "proxy_port", description = "This parameter specifies the port number of the proxy,", key = "proxy_port", type = "SOSOptionString", mandatory = false)
	public SOSOptionPortNumber				proxy_port							= new SOSOptionPortNumber(this, conClassName + ".proxy_port", // HashMap-Key
																						"This parameter specifies the port number of the proxy,", // Titel
																						null, // InitiValue
																						null, // DefaultValue
																						false // isMandatory
																				);	// This parameter specifies the port number of the proxy,
	/**
	 * \var proxy_user: The value of this parameter specifies the user account
	 * fo
	 */
	@JSOptionDefinition(name = "proxy_user", description = "The value of this parameter specifies the user account fo", key = "proxy_user", type = "SOSOptionString", mandatory = false)
	public SOSOptionUserName				proxy_user							= new SOSOptionUserName(this, conClassName + ".proxy_user", // HashMap-Key
																						"The value of this parameter specifies the user account fo", // Titel
																						"user", // InitiValue
																						null, // DefaultValue
																						false // isMandatory
																				);	// The value of this parameter specifies the user account fo
	/**
	 * \var simulate_shell: Should the value true be specified for this
	 * parameter,
	 */
	@JSOptionDefinition(name = "simulate_shell", description = "Should the value true be specified for this parameter,", key = "simulate_shell", type = "SOSOptionString", mandatory = false)
	public SOSOptionBoolean					simulate_shell						= new SOSOptionBoolean(this, conClassName + ".simulate_shell", // HashMap-Key
																						"Should the value true be specified for this parameter,", // Titel
																						"false", // InitiValue
																						"false", // DefaultValue
																						false // isMandatory
																				);	// Should the value true be specified for this parameter,
	/**
	 * \var simulate_shell_inactivity_timeout: If no new characters are written
	 * to stdout or stderr afte
	 */
	@JSOptionDefinition(name = "simulate_shell_inactivity_timeout", description = "If no new characters are written to stdout or stderr afte", key = "simulate_shell_inactivity_timeout", type = "SOSOptionString", mandatory = false)
	public SOSOptionInteger					simulate_shell_inactivity_timeout	= new SOSOptionInteger(this, conClassName
																						+ ".simulate_shell_inactivity_timeout", // HashMap-Key
																						"If no new characters are written to stdout or stderr afte", // Titel
																						"0", // InitiValue
																						"0", // DefaultValue
																						false // isMandatory
																				);	// If no new characters are written to stdout or stderr afte
	/**
	 * \var simulate_shell_login_timeout: If no new characters are written to
	 * stdout or stderr afte
	 */
	@JSOptionDefinition(name = "simulate_shell_login_timeout", description = "If no new characters are written to stdout or stderr afte", key = "simulate_shell_login_timeout", type = "SOSOptionString", mandatory = false)
	public SOSOptionInteger					simulate_shell_login_timeout		= new SOSOptionInteger(this, conClassName + ".simulate_shell_login_timeout", // HashMap-Key
																						"If no new characters are written to stdout or stderr afte", // Titel
																						"0", // InitiValue
																						"0", // DefaultValue
																						false // isMandatory
																				);	// If no new characters are written to stdout or stderr afte
	/**
	 * \var simulate_shell_prompt_trigger: The expected comman line prompt.
	 * Using this prompt the jo
	 */
	@JSOptionDefinition(name = "simulate_shell_prompt_trigger", description = "The expected comman line prompt. Using this prompt the jo", key = "simulate_shell_prompt_trigger", type = "SOSOptionString", mandatory = false)
	public SOSOptionString					simulate_shell_prompt_trigger		= new SOSOptionString(this, conClassName + ".simulate_shell_prompt_trigger", // HashMap-Key
																						"The expected comman line prompt. Using this prompt the jo", // Titel
																						null, // InitiValue
																						null, // DefaultValue
																						false // isMandatory
																				);	// The expected comman line prompt. Using this prompt the jo
	/**
	 * \var user: This parameter specifies the user account to be used when
	 */
	@JSOptionDefinition(name = "user", description = "This parameter specifies the user account to be used when", key = "user", type = "SOSOptionString", mandatory = true)
	public SOSOptionUserName				user								= new SOSOptionUserName(this, conClassName + ".user", // HashMap-Key
																						"This parameter specifies the user account to be used when", // Titel
																						"user", // InitiValue
																						null, // DefaultValue
																						true // isMandatory
																				);	// This parameter specifies the user account to be used when

	@JSOptionDefinition(name = "ignore_hangup_signal", description = "Should the value true be specified, then execution errors", key = "ignore_hangup_signal", type = "SOSOptionBoolean", mandatory = false)
	public SOSOptionBoolean					ignore_hangup_signal				= new SOSOptionBoolean(this, conClassName + ".ignore_hangup_signal", // HashMap-Key
																						"Should the value true be specified, then execution errors", // Titel
																						"true", // InitiValue
																						"true", // DefaultValue
																						false // isMandatory
																				);	// Should the value true be specified, then execution errors

  /**
   * @author SP
   */
  @JSOptionDefinition(name = "preCommand", description = "the preCommand to set an environmental variable on the remote host", key = "preCommand", type = "SOSOptionString", mandatory = false)
  public SOSOptionString preCommand = new SOSOptionString(this, conClassName + ".preCommand", "the preCommand to set an environmental variable on the remote host", "export", "export", false);

  public SOSOptionString getPreCommand() {
    return preCommand;
  }

  public void setPreCommand(final SOSOptionString newPreCommand) {
    this.preCommand = newPreCommand;
  }

  /**
   * @author SP
   */
  @JSOptionDefinition(name = "postCommandRead", description = "the postCommand to read temporary file and write its content to stdout", key = "postCommandRead", type = "SOSOptionString", mandatory = false)
  public SOSOptionString postCommandRead = new SOSOptionString(this, conClassName + ".postCommandRead", "the postCommand to read temporary file and write its content to stdout", "cat", "cat", false);

  public SOSOptionString getPostCommandRead() {
    return postCommandRead;
  }

  public void setPostCommandRead(final SOSOptionString newPostCommandRead) {
    this.postCommandRead = newPostCommandRead;
  }

  /**
   * @author SP
   */
  @JSOptionDefinition(name = "postCommandDelete", description = "the postCommand to delete the temporary file", key = "postCommandDelete", type = "SOSOptionString", mandatory = false)
  public SOSOptionString postCommandDelete = new SOSOptionString(this, conClassName + ".postCommandDelete", "the postCommand to delete the temporary file", "rm", "rm", false);

  public SOSOptionString getPostCommandDelete() {
    return postCommandDelete;
  }

  public void setPostCommandDelete(final SOSOptionString newPostCommandDelete) {
    this.postCommandDelete = newPostCommandDelete;
  }

	
	public SOSSSHJobOptionsSuperClass() {
		objParentClass = this.getClass();
	} // public SOSSSHJobOptions

	public SOSSSHJobOptionsSuperClass(final JSListener pobjListener) {
		this();
		this.registerMessageListener(pobjListener);
	} // public SOSSSHJobOptions

	//

	/*
	 * \xmlonly
	 * ------------------------------------------------------------------
	 * --------- <constructor type="smcw" version="1.0">
	 * <name>SOSSSHJobOptions</name> <title>This parameter specifies
	 * the user account to be used when</title> <description> <para> Konstruktor
	 * SOSSSHJobOptions, als Parameter eine HashMap mit den Optionen
	 * (so wie es im DataSwitch Standard ist). Dieser Konstruktor mappt die
	 * Werte aus der HashMap auf die Properties der Klasse. </para>
	 * </description> <params> <param name="JSSettings" type="HashMap"
	 * ref="byvalue" > <para> Die Parameter, wie sie im Settings der
	 * JS-Datenbank definiert sind, sind in dieser HashMap enthalten und werden
	 * auf die Properties dieser Klasse gemappt. </para> </param> </params>
	 * <keywords> <keyword>IDoc</keyword> <keyword>Options</keyword>
	 * <keyword>Settings</keyword>
	 * <keyword>SOSSSHJobOptions:Class</keyword> </keywords>
	 * <categories> <category>IDoc</category> <category>OptionClass</category>
	 * </categories> </constructor>
	 * ----------------------------------------------
	 * ------------------------------ \endxmlonly
	 */
	public SOSSSHJobOptionsSuperClass(final HashMap<String, String> JSSettings) throws Exception {
		this();
		this.setAllOptions(JSSettings);
	} // public SOSSSHJobOptions (HashMap JSSettings)

	//

	/**
	 * \brief toOut - schreibt die Werte und Beschreibung aller Optionen nach
	 * System.out
	 *
	 * \details
	 *
	 * \see getAllOptionsAsString \see toString
	 */
	// @Override
	// public void toOut () {
	// @SuppressWarnings("unused")
	// final String conMethodName = conClassName + "::toOut";
	//
	// System.out.println(getAllOptionsAsString());
	// } // public void toOut ()
	//
	/**
	 * \brief toString - liefert die Werte und Beschreibung aller Optionen als
	 * String
	 *
	 * \details
	 *
	 * \see getAllOptionsAsString \see toOut
	 */
	// @Override
	// public String toString () {
	// @SuppressWarnings("unused")
	// final String conMethodName = conClassName + "::toString";
	//
	// return getAllOptionsAsString();
	// } // public String toString ()
	//
	/**
	 * \brief getAllOptionsAsString - liefert die Werte und Beschreibung aller
	 * Optionen als String
	 *
	 * \details
	 *
	 * \see toString \see toOut
	 */
	@SuppressWarnings("unused")
	private String getAllOptionsAsString() {
		final String conMethodName = conClassName + "::getAllOptionsAsString";
		String strT = conClassName + "\n";
		final StringBuffer strBuffer = new StringBuffer();
		// strT += IterateAllDataElementsByAnnotation(objParentClass, this,
		// SOSOptionsClass.IterationTypes.toString, strBuffer);
		// strT += IterateAllDataElementsByAnnotation(objParentClass, this, 13,
		// strBuffer);
		strT += this.toString(); // fix
		//
		return strT;
	} // private String getAllOptionsAsString ()

	//

	/**
	 * \brief setAllOptions - �bernimmt die OptionenWerte aus der HashMap
	 *
	 * \details In der als Parameter anzugebenden HashMap sind Schl�ssel (Name)
	 * und Wert der jeweiligen Option als Paar angegeben. Ein Beispiel f�r den
	 * Aufbau einer solchen HashMap findet sich in der Beschreibung dieser
	 * Klasse (\ref TestData "setJobSchedulerSSHJobOptions"). In dieser Routine
	 * werden die Schl�ssel analysiert und, falls gefunden, werden die
	 * dazugeh�rigen Werte den Properties dieser Klasse zugewiesen.
	 *
	 * Nicht bekannte Schl�ssel werden ignoriert.
	 *
	 * \see SOSOptionsClass::getItem
	 *
	 * @param pobjJSSettings
	 * @throws Exception
	 */
	@Override
	public void setAllOptions(final HashMap<String, String> pobjJSSettings)  {
		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setAllOptions";
		flgSetAllOptions = true;
		objSettings = pobjJSSettings;
		super.Settings(objSettings);
		super.setAllOptions(pobjJSSettings);
		flgSetAllOptions = false;
	} // public void setAllOptions (HashMap <String, String> JSSettings)

	//


	/**
	 * \brief CheckMandatory - pr�ft alle Muss-Optionen auf Werte
	 *
	 * \details
	 * @throws Exception
	 *
	 * @throws Exception
	 *             - wird ausgel�st, wenn eine mandatory-Option keinen Wert hat
	 */
	@Override
	public void CheckMandatory() throws JSExceptionMandatoryOptionMissing //
	{
		try {
			getCredentialStore().checkCredentialStoreOptions();

			super.CheckMandatory();
		}
		catch (Exception e) {
			throw new JSExceptionMandatoryOptionMissing(e.toString());
		}
	} // public void CheckMandatory ()

	//

	/**
	 *
	 * \brief CommandLineArgs - �bernehmen der Options/Settings aus der
	 * Kommandozeile
	 *
	 * \details Die in der Kommandozeile beim Starten der Applikation
	 * angegebenen Parameter werden hier in die HashMap �bertragen und danach
	 * den Optionen als Wert zugewiesen.
	 *
	 * \return void
	 *
	 * @param pstrArgs
	 * @throws Exception
	 */
	@Override
	public void CommandLineArgs(final String[] pstrArgs)  {
		super.CommandLineArgs(pstrArgs);
		this.setAllOptions(super.objSettings);
	}

	/**
	 * \brief getAuth_file
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override
	public SOSOptionInFileName getAuth_file() {
		return auth_file;
	}

	/**
	 * \brief setAuth_file
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param authFile
	 */
	@Override
	public void setAuth_file(final SOSOptionInFileName authFile) {
		auth_file = authFile;
	}

	/**
	 * \brief getAuth_method
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override
	public SOSOptionAuthenticationMethod getAuth_method() {
		return auth_method;
	}

	/**
	 * \brief setAuth_method
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param authMethod
	 */
	@Override
	public void setAuth_method(final SOSOptionAuthenticationMethod authMethod) {
		auth_method = authMethod;
	}

	/**
	 * \brief getCommand
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override
	public SOSOptionCommandString getCommand() {
		return command;
	}

	/**
	 * \brief setCommand
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param command
	 */
	@Override
	public void setCommand(final SOSOptionCommandString command) {
		this.command = command;
	}

	/**
	 * \brief getCommand_delimiter
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override
	public SOSOptionRegExp getCommand_delimiter() {
		return command_delimiter;
	}

	/**
	 * \brief setCommand_delimiter
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param commandDelimiter
	 */
	@Override
	public void setCommand_delimiter(final SOSOptionRegExp commandDelimiter) {
		command_delimiter = commandDelimiter;
	}

	/**
	 * \brief getCommand_script
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override
	public SOSOptionCommandString getCommand_script() {
		return command_script;
	}

	/**
	 * \brief setCommand_script
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param commandScript
	 */
	@Override
	public void setCommand_script(final SOSOptionCommandString commandScript) {
		command_script = commandScript;
	}

	/**
	 * \brief getCommand_script_file
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override
	public SOSOptionInFileName getCommand_script_file() {
		return command_script_file;
	}

	/**
	 * \brief setCommand_script_file
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param commandScriptFile
	 */
	@Override
	public void setCommand_script_file(final SOSOptionInFileName commandScriptFile) {
		command_script_file = commandScriptFile;
	}

	/**
	 * \brief getCommand_script_param
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override
	public SOSOptionString getCommand_script_param() {
		return command_script_param;
	}

	/**
	 * \brief setCommand_script_param
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param commandScriptParam
	 */
	@Override
	public void setCommand_script_param(final SOSOptionString commandScriptParam) {
		command_script_param = commandScriptParam;
	}

	/**
	 * \brief getHost
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override
	public SOSOptionHostName getHost() {
		return host;
	}

	/**
	 * \brief setHost
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param host
	 */
	@Override
	public void setHost(final SOSOptionHostName phost) {
		host = phost;
	}

	/**
	 * \brief getIgnore_error
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override
	public SOSOptionBoolean getIgnore_error() {
		return ignore_error;
	}

	/**
	 * \brief setIgnore_error
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param ignoreError
	 */
	@Override
	public void setIgnore_error(final SOSOptionBoolean ignoreError) {
		ignore_error = ignoreError;
	}

	/**
	 * \brief getIgnore_exit_code
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override
	public SOSOptionIntegerArray getIgnore_exit_code() {
		return ignore_exit_code;
	}

	/**
	 * \brief setIgnore_exit_code
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param ignoreExitCode
	 */
	@Override
	public void setIgnore_exit_code(final SOSOptionIntegerArray ignoreExitCode) {
		ignore_exit_code = ignoreExitCode;
	}

	/**
	 * \brief getIgnore_signal
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override
	public SOSOptionBoolean getIgnore_signal() {
		return ignore_signal;
	}

	/**
	 * \brief setIgnore_signal
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param ignoreSignal
	 */
	@Override
	public void setIgnore_signal(final SOSOptionBoolean ignoreSignal) {
		ignore_signal = ignoreSignal;
	}

	/**
	 * \brief getIgnore_stderr
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override
	public SOSOptionBoolean getIgnore_stderr() {
		return ignore_stderr;
	}

	/**
	 * \brief setIgnore_stderr
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param ignoreStderr
	 */
	@Override
	public void setIgnore_stderr(final SOSOptionBoolean ignoreStderr) {
		ignore_stderr = ignoreStderr;
	}

	/**
	 * \brief getPassword
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override
	public SOSOptionPassword getPassword() {
		return password;
	}

	/**
	 * \brief setPassword
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param password
	 */
	@Override
	public void setPassword(final SOSOptionPassword password) {
		this.password = password;
	}

	/**
	 * \brief getPort
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override
	public SOSOptionPortNumber getPort() {
		return port;
	}

	/**
	 * \brief setPort
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param port
	 */
	@Override
	public void setPort(final SOSOptionPortNumber port) {
		this.port = port;
	}

	/**
	 * \brief getProxy_host
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override
	public SOSOptionString getProxy_host() {
		return proxy_host;
	}

	/**
	 * \brief setProxy_host
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param proxyHost
	 */
	@Override
	public void setProxy_host(final SOSOptionString proxyHost) {
		proxy_host = proxyHost;
	}

	/**
	 * \brief getproxy_password
	 *
	 * \details
	 * getter
	 *
	 * @return the proxy_password
	 */
	@Override
	public SOSOptionPassword getProxy_password() {
		return proxy_password;
	}

	/**
	 * \brief setproxy_password -
	 *
	 * \details
	 * setter
	 *
	 * @param proxyPassword the value for proxy_password to set
	 */
	@Override
	public void setProxy_password(final SOSOptionPassword proxyPassword) {
		proxy_password = proxyPassword;
	}

	/**
	 * \brief getProxy_port
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override
	public SOSOptionPortNumber getProxy_port() {
		return proxy_port;
	}

	/**
	 * \brief setProxy_port
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param proxyPort
	 */
	@Override
	public void setProxy_port(final SOSOptionPortNumber proxyPort) {
		proxy_port = proxyPort;
	}

	/**
	 * \brief getproxy_user
	 *
	 * \details
	 * getter
	 *
	 * @return the proxy_user
	 */
	@Override
	public SOSOptionUserName getProxy_user() {
		return proxy_user;
	}

	/**
	 * \brief setproxy_user -
	 *
	 * \details
	 * setter
	 *
	 * @param proxyUser the value for proxy_user to set
	 */
	@Override
	public void setProxy_user(final SOSOptionUserName proxyUser) {
		proxy_user = proxyUser;
	}

	/**
	 * \brief getSimulate_shell
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override
	public SOSOptionBoolean getSimulate_shell() {
		return simulate_shell;
	}

	/**
	 * \brief setSimulate_shell
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param simulateShell
	 */
	@Override
	public void setSimulate_shell(final SOSOptionBoolean simulateShell) {
		simulate_shell = simulateShell;
	}

	/**
	 * \brief getSimulate_shell_inactivity_timeout
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override
	public SOSOptionInteger getSimulate_shell_inactivity_timeout() {
		return simulate_shell_inactivity_timeout;
	}

	/**
	 * \brief setSimulate_shell_inactivity_timeout
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param simulateShellInactivityTimeout
	 */
	@Override
	public void setSimulate_shell_inactivity_timeout(final SOSOptionInteger simulateShellInactivityTimeout) {
		simulate_shell_inactivity_timeout = simulateShellInactivityTimeout;
	}

	/**
	 * \brief getSimulate_shell_login_timeout
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override
	public SOSOptionInteger getSimulate_shell_login_timeout() {
		return simulate_shell_login_timeout;
	}

	/**
	 * \brief setSimulate_shell_login_timeout
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param simulateShellLoginTimeout
	 */
	@Override
	public void setSimulate_shell_login_timeout(final SOSOptionInteger simulateShellLoginTimeout) {
		simulate_shell_login_timeout = simulateShellLoginTimeout;
	}

	/**
	 * \brief getSimulate_shell_prompt_trigger
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override
	public SOSOptionString getSimulate_shell_prompt_trigger() {
		return simulate_shell_prompt_trigger;
	}

	/**
	 * \brief setSimulate_shell_prompt_trigger
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param simulateShellPromptTrigger
	 */
	@Override
	public void setSimulate_shell_prompt_trigger(final SOSOptionString simulateShellPromptTrigger) {
		simulate_shell_prompt_trigger = simulateShellPromptTrigger;
	}

	/**
	 * \brief getUser
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override
	public SOSOptionUserName getUser() {
		return user;
	}

	/**
	 * \brief setUser
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param user
	 */
	@Override
	public void setUser(final SOSOptionUserName user) {
		this.user = user;
	}

	/**
	 * \brief getIgnore_hangup_signal
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override
	public SOSOptionBoolean getIgnore_hangup_signal() {
		return ignore_hangup_signal;
	}

	/**
	 * \brief setIgnore_hangup_signal
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param ignoreHangupSignal
	 */
	@Override
	public void setIgnore_hangup_signal(final SOSOptionBoolean pIgnoreHangupSignal) {
		ignore_hangup_signal = pIgnoreHangupSignal;
	}

	@Override
	public SOSOptionString getalternative_account() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SOSOptionHostName getalternative_host() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SOSOptionString getalternative_passive_mode() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SOSOptionPassword getalternative_password() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SOSOptionPortNumber getalternative_port() {
		return null;
	}

	@Override
	public void setalternative_host(final SOSOptionHostName pAlternativeHost) {

	}

	@Override
	public void setalternative_password(final SOSOptionPassword pAlternativePassword) {

	}

	
	// Credential Store Methods and fields
	
	protected SOSCredentialStoreImpl objCredentialStore = null;
	public SOSCredentialStoreImpl getCredentialStore() {
		if (objCredentialStore == null) {
			objCredentialStore = new SOSCredentialStoreImpl(this);
		}
		return objCredentialStore;
	}

	public void setChildClasses(final HashMap<String, String> pobjJSSettings, final String pstrPrefix) throws Exception {
		getCredentialStore().setChildClasses(pobjJSSettings, pstrPrefix);
		objCredentialStore.checkCredentialStoreOptions();
	} // public SOSConnection2OptionsAlternate (HashMap JSSettings)


} // public class SOSSSHJobOptions
