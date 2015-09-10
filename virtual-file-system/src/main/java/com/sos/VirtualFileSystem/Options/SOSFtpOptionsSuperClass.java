package com.sos.VirtualFileSystem.Options;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.sos.JSHelper.Annotations.JSOptionClass;
import com.sos.JSHelper.Annotations.JSOptionDefinition;
import com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Listener.JSListener;
import com.sos.JSHelper.Options.JSJobChain;
import com.sos.JSHelper.Options.JSOptionsClass;
import com.sos.JSHelper.Options.SOSOptionArrayList;
import com.sos.JSHelper.Options.SOSOptionAuthenticationMethod;
import com.sos.JSHelper.Options.SOSOptionBackgroundServiceTransferMethod;
import com.sos.JSHelper.Options.SOSOptionBoolean;
import com.sos.JSHelper.Options.SOSOptionCommandScript;
import com.sos.JSHelper.Options.SOSOptionCommandScriptFile;
import com.sos.JSHelper.Options.SOSOptionCommandString;
import com.sos.JSHelper.Options.SOSOptionEncoding;
import com.sos.JSHelper.Options.SOSOptionFileName;
import com.sos.JSHelper.Options.SOSOptionFileSize;
import com.sos.JSHelper.Options.SOSOptionFolderName;
import com.sos.JSHelper.Options.SOSOptionHostName;
import com.sos.JSHelper.Options.SOSOptionInFileName;
import com.sos.JSHelper.Options.SOSOptionIniFileName;
import com.sos.JSHelper.Options.SOSOptionInteger;
import com.sos.JSHelper.Options.SOSOptionProxyProtocol;
import com.sos.JSHelper.Options.SOSOptionJSTransferMethod.enuJSTransferModes;
import com.sos.JSHelper.Options.SOSOptionJadeOperation;
import com.sos.JSHelper.Options.SOSOptionJobChainNode;
import com.sos.JSHelper.Options.SOSOptionOutFileName;
import com.sos.JSHelper.Options.SOSOptionPassword;
import com.sos.JSHelper.Options.SOSOptionPlatform;
import com.sos.JSHelper.Options.SOSOptionPortNumber;
import com.sos.JSHelper.Options.SOSOptionProcessID;
import com.sos.JSHelper.Options.SOSOptionRegExp;
import com.sos.JSHelper.Options.SOSOptionRelOp;
import com.sos.JSHelper.Options.SOSOptionString;
import com.sos.JSHelper.Options.SOSOptionStringValueList;
import com.sos.JSHelper.Options.SOSOptionTime;
import com.sos.JSHelper.Options.SOSOptionTransferMode;
import com.sos.JSHelper.Options.SOSOptionTransferType;
import com.sos.JSHelper.Options.SOSOptionUserName;
import com.sos.JSHelper.interfaces.ISOSConnectionOptions;
import com.sos.JSHelper.interfaces.ISOSFtpOptions;
import com.sos.VirtualFileSystem.Interfaces.ISOSAuthenticationOptions;
import com.sos.i18n.annotation.I18NResourceBundle;
import com.sos.localization.Messages;

/**
* \class 		SOSFtpOptionsSuperClass - Transfer files by FTP/SFTP
*
* \brief An Options-Class with all Options.
*
* \verbatim ;
* mechanicaly created by JobDocu2OptionsClass.xslt from http://www.sos-berlin.com at 2010-05-15-14-28-00
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
pobjHM.put ("		SOSFtpOptionsSuperClass.auth_file", "test");  // This parameter specifies the path and name of a user's pr
return pobjHM;
}  //  private void SetJobSchedulerSSHJobOptions (HashMap <String, String> pobjHM)
* \endverbatim
*/
@JSOptionClass(
				name = "SOSFtpOptionsSuperClass",
				description = "SOSFtpOptionsSuperClass")
@I18NResourceBundle(
					baseName = "SOSVirtualFileSystem",
					defaultLocale = "en")
public abstract class SOSFtpOptionsSuperClass extends JSOptionsClass implements ISOSConnectionOptions, ISOSAuthenticationOptions, ISOSFtpOptions {
	protected Messages			objMsg				= new Messages(this.getClass().getAnnotation(I18NResourceBundle.class).baseName());
	private static final long	serialVersionUID	= -4445655877481869778L;
	private final String		conClassName		= "SOSFtpOptionsSuperClass";
	private final static Logger		logger		= Logger.getLogger(SOSFtpOptionsSuperClass.class);
	// SOSFtp-191 Option TFN_Post_Command: commands executed after creating the final TargetFile
	/**
	 * \option TFN_Post_Command
	 * \type SOSOptionString
	 * \brief TFN_Post_Command - Post commands executed after creating the final TargetFile
	 *
	 * \details
	 * Post commands after creating the final TargetFileName
	 *
	 * \mandatory: false
	 *
	 * \created 08.04.2014 16:23:51 by KB
	 */
	@JSOptionDefinition(
						name = "TFN_Post_Command",
						description = "Post commands executed after creating the final TargetFile",
						key = "TFN_Post_Command",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionString		TFN_Post_Command	= new SOSOptionString( // ...
															this, // ....
															conClassName + ".TFN_Post_Command", // ...
															"Post commands executed after creating the final TargetFileName", // ...
															"", // ...
															"", // ...
															false);

	public SOSOptionString getTFN_Post_Command() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getTFN_Post_Command";
		return TFN_Post_Command;
	} // public String getTFN_Post_Command

	public SOSFtpOptionsSuperClass setTFN_Post_Command(final SOSOptionString pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setTFN_Post_Command";
		TFN_Post_Command = pstrValue;
		return this;
	} // public SOSConnection2OptionsSuperClass setTFN_Post_Command
	/**
	 * \option polling_wait_4_Source_Folder
	 * \type SOSOptionBoolean
	 * \brief polling_wait_4_Source_Folder - Wait for source folder if the folder does not exists
	 *
	 * \details
	 * During polling
	 *
	 * \mandatory: true
	 *
	 * \created 21.01.2014 13:09:41 by KB
	 */
	@JSOptionDefinition(
						name = "polling_wait_4_Source_Folder",
						description = "During polling",
						key = "polling_wait_4_Source_Folder",
						type = "SOSOptionBoolean",
						mandatory = true)
	public SOSOptionBoolean	pollingWait4SourceFolder	= new SOSOptionBoolean(
														// ...
																this, // ....
																conClassName + ".polling_wait_4_Source_Folder", // ...
																"During polling", // ...
																"false", // ...
																"false", // ...
																true);

	public String getPollingWait4SourceFolder() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getpolling_wait_4_Source_Folder";
		return pollingWait4SourceFolder.Value();
	} // public String getpolling_wait_4_Source_Folder

	public SOSFtpOptionsSuperClass setPollingWait4SourceFolder(final String pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setpolling_wait_4_Source_Folder";
		pollingWait4SourceFolder.Value(pstrValue);
		return this;
	} // public SOSFtpOptionsSuperClass setpolling_wait_4_Source_Folder
	/**
	 * \option include
	 * \type SOSOptionString
	 * \brief include - the include directive as an option
	 *
	 * \details
	 * the include directive as an option
	 *
	 * \mandatory: false
	 *
	 * \created 17.12.2013 19:51:59 by KB
	 */
	@JSOptionDefinition(
						name = "include",
						description = "the include directive as an option",
						key = "include",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionString	include	= new SOSOptionString(
									// ...
											this, // ....
											conClassName + ".include", // ...
											"the include directive as an option", // ...
											"", // ...
											"", // ...
											false);

	public String getinclude() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getinclude";
		return include.Value();
	} // public String getinclude

	public SOSFtpOptionsSuperClass setinclude(final String pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setinclude";
		include.Value(pstrValue);
		return this;
	} // public SOSFtpOptionsSuperClass setinclude
	/**
	 * \option use_filters
	 * \type SOSOptionBoolean
	 * \brief use_filters - Use filters for source and/or Targe
	 *
	 * \details
	 * Use filters for source and/or Targe
	 *
	 * \mandatory: false
	 *
	 * \created 14.05.2014 15:13:19 by KB
	 */
	@JSOptionDefinition(
						name = "use_filters",
						description = "Use filters for source and/or Targe",
						key = "use_filters",
						type = "SOSOptionBoolean",
						mandatory = false)
	public SOSOptionBoolean	use_filters	= new SOSOptionBoolean( // ...
												this, // ....
												conClassName + ".use_filters", // ...
												"Use filters for source and/or Targe", // ...
												"false", // ...
												"false", // ...
												false);

	public SOSOptionBoolean getuse_filters() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getuse_filters";
		return use_filters;
	} // public String getuse_filters

	public SOSFtpOptionsSuperClass setuse_filters(final SOSOptionBoolean pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setuse_filters";
		use_filters = pstrValue;
		return this;
	} // public SOSFtpOptionsSuperClass setuse_filters
	/**
	 * \option is_fragment
	 * \type SOSOptionBoolean
	 * \brief is_fragment - Mark a profile as an fragment (include snippet)
	 *
	 * \details
	 * Mark an profile as a fragment
	 *
	 * \mandatory: false
	 *
	 * \created 17.12.2013 19:48:18 by KB
	 */
	@JSOptionDefinition(
						name = "is_fragment",
						description = "Mark an profile as a fragment",
						key = "is_fragment",
						type = "SOSOptionBoolean",
						mandatory = false)
	public SOSOptionBoolean	isFragment	= new SOSOptionBoolean(
										// ...
												this, // ....
												conClassName + ".is_fragment", // ...
												"Mark an profile as a fragment", // ...
												"false", // ...
												"false", // ...
												false);

	public String getis_fragment() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getis_fragment";
		return isFragment.Value();
	} // public String getis_fragment

	public SOSFtpOptionsSuperClass setis_fragment(final String pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setis_fragment";
		isFragment.Value(pstrValue);
		return this;
	} // public SOSFtpOptionsSuperClass setis_fragment
	/**
	 * \option reuse_connection
	 * \type SOSOptionBoolean
	 * \brief reuse_connection - reuse the current connections for all transfers
	 *
	 * \details
	 * reuse the current connections for all transfers
	 *
	 * \mandatory: false
	 *
	 * \created 10.12.2013 12:06:44 by KB
	 */
	@JSOptionDefinition(
						name = "reuse_connection",
						description = "reuse the current connections for all transfers",
						key = "reuse_connection",
						type = "SOSOptionBoolean",
						mandatory = false)
	public SOSOptionBoolean	reuseConnection	= new SOSOptionBoolean(
											// ...
													this, // ....
													conClassName + ".reuse_connection", // ...
													"reuse the current connections for all transfers", // ...
													"false", // ...
													"false", // ...
													false);

	public String getreuse_connection() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getreuse_connection";
		return reuseConnection.Value();
	} // public String getreuse_connection

	public SOSFtpOptionsSuperClass setreuse_connection(final String pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setreuse_connection";
		reuseConnection.Value(pstrValue);
		return this;
	} // public SOSFtpOptionsSuperClass setreuse_connection
	/**
	 * \option polling_server
	 * \type SOSOptionBoolean
	 * \brief polling_server - act as a polling server
	 *
	 * \details
	 * act as a polling server
	 *
	 * \mandatory: false
	 *
	 * \created 14.11.2013 09:23:48 by KB
	 */
	@JSOptionDefinition(
						name = "polling_server",
						description = "act as a polling server",
						key = "polling_server",
						type = "SOSOptionBoolean",
						mandatory = false)
	public SOSOptionBoolean	PollingServer	= new SOSOptionBoolean(
											// ...
													this, // ....
													conClassName + ".polling_server", // ...
													"act as a polling server", // ...
													"false", // ...
													"false", // ...
													false);

	public String getpolling_server() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getpolling_server";
		return PollingServer.Value();
	} // public String getpolling_server

	public SOSFtpOptionsSuperClass setpolling_server(final String pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setpolling_server";
		PollingServer.Value(pstrValue);
		return this;
	} // public SOSFtpOptionsSuperClass setpolling_server
	/**
	 * \option polling_end_at
	 * \type SOSOptionTime
	 * \brief polling_end_at - PollingServer should stop at the specified date/time
	 *
	 * \details
	 * PollingServer should stop at the specified date/time
	 *
	 * \mandatory: false
	 *
	 * \created 10.12.2013 23:34:22 by KB
	 */
	@JSOptionDefinition(
						name = "polling_end_at",
						description = "PollingServer should stop at the specified date/time",
						key = "polling_end_at",
						type = "SOSOptionTime",
						mandatory = false)
	public SOSOptionTime	pollingEndAt	= new SOSOptionTime(
											// ...
													this, // ....
													conClassName + ".polling_end_at", // ...
													"Polling should stop at the specified date/time", // ...
													"0", // ...
													"0", // ...
													false);

	public String getpolling_end_at() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getpolling_end_at";
		return pollingEndAt.Value();
	} // public String getpolling_end_at

	public SOSFtpOptionsSuperClass setpolling_end_at(final String pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setpolling_end_at";
		pollingEndAt.Value(pstrValue);
		return this;
	} // public SOSFtpOptionsSuperClass setpolling_end_at
	/**
	 * \option polling_server_poll_forever
	 * \type SOSOptionBoolean
	 * \brief polling_server_poll_forever - Poll until forever - 1 day
	 *
	 * \details
	 * poll forever
	 *
	 * \mandatory: true
	 *
	 * \created 21.01.2014 13:40:08 by KB
	 */
	@JSOptionDefinition(
						name = "polling_server_poll_forever",
						description = "poll forever",
						key = "polling_server_poll_forever",
						type = "SOSOptionBoolean",
						mandatory = true)
	public SOSOptionBoolean	PollingServerPollForever	= new SOSOptionBoolean(
														// ...
																this, // ....
																conClassName + ".polling_server_poll_forever", // ...
																"poll forever", // ...
																"false", // ...
																"false", // ...
																true);

	public String getpolling_server_poll_forever() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getpolling_server_poll_forever";
		return PollingServerPollForever.Value();
	} // public String getpolling_server_poll_forever

	public SOSFtpOptionsSuperClass setpolling_server_poll_forever(final String pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setpolling_server_poll_forever";
		PollingServerPollForever.Value(pstrValue);
		return this;
	} // public SOSFtpOptionsSuperClass setpolling_server_poll_forever
	/**
	 * \option polling_duration
	 * \type SOSOptionTime
	 * \brief polling_duration - How long the PollingServer should run
	 *
	 * \details
	 * How long the PollingServer should run
	 *
	 * \mandatory: false
	 *
	 * \created 10.12.2013 23:30:57 by KB
	 */
	@JSOptionDefinition(
						name = "polling_server_duration",
						description = "How long the PollingServer should run",
						key = "polling_server_duration",
						type = "SOSOptionTime",
						mandatory = false)
	public SOSOptionTime	pollingServerDuration	= new SOSOptionTime(
													// ...
															this, // ....
															conClassName + ".polling_server_duration", // ...
															"How long the PollingServer should run", // ...
															"0", // ...
															"0", // ...
															false);

	public String getpolling_server_duration() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getpolling_duration";
		return pollingServerDuration.Value();
	} // public String getpolling_duration

	public SOSFtpOptionsSuperClass setpolling_server_duration(final String pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setpolling_duration";
		pollingServerDuration.Value(pstrValue);
		return this;
	} // public SOSFtpOptionsSuperClass setpolling_duration
	/**
	 * \option Lazy_Connection_Mode
	 * \type SOSOptionBoolean
	 * \brief Lazy_Connection_Mode - Connect to Target as late as possible
	 *
	 * \details
	 * Connect to Target as late as possible
	 *
	 * \mapollingServerDuration
	 *
	 * \created 14.11.2013 09:22:03 by KB
	 */
	@JSOptionDefinition(
						name = "Lazy_Connection_Mode",
						description = "Connect to Target as late as possible",
						key = "Lazy_Connection_Mode",
						type = "SOSOptionBoolean",
						mandatory = false)
	public SOSOptionBoolean	LazyConnectionMode	= new SOSOptionBoolean(
												// ...
														this, // ....
														conClassName + ".Lazy_Connection_Mode", // ...
														"Connect to Target as late as possible", // ...
														"false", // ...
														"false", // ...
														false);

	public String getLazy_Connection_Mode() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getLazy_Connection_Mode";
		return LazyConnectionMode.Value();
	} // public String getLazy_Connection_Mode

	public SOSFtpOptionsSuperClass setLazy_Connection_Mode(final String pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setLazy_Connection_Mode";
		LazyConnectionMode.Value(pstrValue);
		return this;
	} // public SOSFtpOptionsSuperClass setLazy_Connection_Mode
	/**
	 * \option platform
	 * \type SOSOptionString
	 * \brief platform - platform on which the app is running
	 *
	 * \details
	 * platform on which the app is running
	 *
	 * \mandatory: false
	 *
	 * \created 22.02.2013 18:21:22 by KB
	 */
	@JSOptionDefinition(
						name = "platform",
						description = "platform on which the app is running",
						key = "platform",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionPlatform	platform	= new SOSOptionPlatform(
											// ...
													this, // ....
													conClassName + ".platform", // ...
													"platform on which the app is running", // ...
													"", // ...
													"", // ...
													false);

	public String getplatform() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getplatform";
		return platform.Value();
	} // public String getplatform

	public SOSFtpOptionsSuperClass setplatform(final String pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setplatform";
		platform.Value(pstrValue);
		return this;
	} // public SOSFtpOptionsSuperClass setplatform
	/**
	 * \option mail_on_success
	 * \type SOSOptionBoolean
	 * \brief mail_on_success - Send a Mail in case of sucess
	 *
	 * \details
	 * Send a Mail in case of sucess
	 *
	 * \mandatory: false
	 *
	 * \created 07.01.2013 17:27:51 by KB
	 */
	@JSOptionDefinition(
						name = "mail_on_success",
						description = "Send a Mail in case of sucess",
						key = "mail_on_success",
						type = "SOSOptionBoolean",
						mandatory = false)
	public SOSOptionBoolean	mail_on_success	= new SOSOptionBoolean(
											// ...
													this, // ....
													conClassName + ".mail_on_success", // ...
													"Send a Mail in case of sucess", // ...
													"false", // ...
													"false", // ...
													false);

	public String getmail_on_success() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getmail_on_success";
		return mail_on_success.Value();
	} // public String getmail_on_success

	public SOSFtpOptionsSuperClass setmail_on_success(final String pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setmail_on_success";
		mail_on_success.Value(pstrValue);
		return this;
	} // public SOSFtpOptionsSuperClass setmail_on_success
	/**
	 * \option mail_on_error
	 * \type SOSOptionBoolean
	 * \brief mail_on_error - Send a Mail in case of sucess
	 *
	 * \details
	 * Send a Mail in case of sucess
	 *
	 * \mandatory: false
	 *
	 * \created 07.01.2013 17:27:51 by KB
	 */
	@JSOptionDefinition(
						name = "mail_on_error",
						description = "Send a Mail in case of error",
						key = "mail_on_error",
						type = "SOSOptionBoolean",
						mandatory = false)
	public SOSOptionBoolean	mail_on_error	= new SOSOptionBoolean(
											// ...
													this, // ....
													conClassName + ".mail_on_error", // ...
													"Send a Mail in case of sucess", // ...
													"false", // ...
													"false", // ...
													false);

	public String getmail_on_error() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getmail_on_error";
		return mail_on_error.Value();
	} // public String getmail_on_error

	public SOSFtpOptionsSuperClass setmail_on_error(final String pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setmail_on_error";
		mail_on_error.Value(pstrValue);
		return this;
	} // public SOSFtpOptionsSuperClass setmail_on_error
	/**
	 * \option mail_on_empty_files
	 * \type SOSOptionBoolean
	 * \brief mail_on_empty_files - Send a Mail in case of empty files
	 *
	 * \details
	 * Send a Mail in case of empty files
	 *
	 * \mandatory: false
	 *
	 * \created 07.01.2013 17:27:51 by KB
	 */
	@JSOptionDefinition(
						name = "mail_on_empty_files",
						description = "Send a Mail in case of empty files",
						key = "mail_on_empty_files",
						type = "SOSOptionBoolean",
						mandatory = false)
	public SOSOptionBoolean	mail_on_empty_files	= new SOSOptionBoolean(
												// ...
														this, // ....
														conClassName + ".mail_on_empty_files", // ...
														"Send a Mail in case of empty files", // ...
														"false", // ...
														"false", // ...
														false);

	public String getmail_on_empty_files() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getmail_on_empty_files";
		return mail_on_empty_files.Value();
	} // public String getmail_on_empty_files

	public SOSFtpOptionsSuperClass setmail_on_empty_files(final String pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setmail_on_empty_files";
		mail_on_empty_files.Value(pstrValue);
		return this;
	} // public SOSFtpOptionsSuperClass setmail_on_empty_files
	/**
	 * \option title
	 * \type SOSOptionString
	 * \brief title - The Title for a section /profile
	 *
	 * \details
	 * The Title for a section /profile
	 *
	 * \mandatory: false
	 *
	 * \created 13.12.2012 12:39:09 by KB
	 */
	@JSOptionDefinition(
						name = "title",
						description = "The Title for a section /profile",
						key = "title",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionString	title	= new SOSOptionString(
									// ...
											this, // ....
											conClassName + ".title", // ...
											"The Title for a section /profile", // ...
											"", // ...
											"", // ...
											false);

	public String gettitle() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::gettitle";
		return title.Value();
	} // public String gettitle

	public SOSFtpOptionsSuperClass settitle(final String pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::settitle";
		title.Value(pstrValue);
		return this;
	} // public SOSFtpOptionsSuperClass settitle
	/**
	 * \option keep_modification_date
	 * \type SOSOptionBoolean
	 * \brief keep_modification_date - Keep Modification Date of File
	 *
	 * \details
	 * Keep Modification Date of File
	 *
	 * \mandatory: false
	 *
	 * \created 26.11.2012 13:48:14 by KB
	 */
	@JSOptionDefinition(
						name = "keep_modification_date",
						description = "Keep Modification Date of File",
						key = "keep_modification_date",
						type = "SOSOptionBoolean",
						mandatory = false)
	public SOSOptionBoolean	keep_modification_date	= new SOSOptionBoolean(
													// ...
															this, // ....
															conClassName + ".keep_modification_date", // ...
															"Keep Modification Date of File", // ...
															"false", // ...
															"false", // ...
															false);
	public SOSOptionBoolean	KeepModificationDate	= (SOSOptionBoolean) keep_modification_date.SetAlias("KeepModificationate");

	public String getkeep_modification_date() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getkeep_modification_date";
		return keep_modification_date.Value();
	} // public String getkeep_modification_date

	public SOSFtpOptionsSuperClass setkeep_modification_date(final String pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setkeep_modification_date";
		keep_modification_date.Value(pstrValue);
		return this;
	} // public SOSFtpOptionsSuperClass setkeep_modification_date
	/**
	 * \option cumulate_files
	 * \type SOSOptionBoolean
	 * \brief cumulate_files - cumulate (all) files into one file by append
	 *
	 * \details
	 * cumulate (all) files into one file by append
	 *
	 * \mandatory: false
	 *
	 * \created 08.08.2012 10:47:12 by KB
	 */
	@JSOptionDefinition(
						name = "cumulate_files",
						description = "cumulate (all) files into one file by append",
						key = "cumulate_files",
						type = "SOSOptionBoolean",
						mandatory = false)
	public SOSOptionBoolean	CumulateFiles	= new SOSOptionBoolean(
											// ...
													this, // ....
													conClassName + ".cumulate_files", // ...
													"cumulate (all) files into one file by append", // ...
													"false", // ...
													"false", // ...
													false);

	public String getcumulate_files() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getcumulate_files";
		return CumulateFiles.Value();
	} // public String getcumulate_files

	public SOSFtpOptionsSuperClass setcumulate_files(final String pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setcumulate_files";
		CumulateFiles.Value(pstrValue);
		return this;
	} // public SOSFtpOptionsSuperClass setcumulate_files
	/**
	 * \option cumulative_filename
	 * \type SOSOptionFileName
	 * \brief cumulative_filename - Name of File into which all files hat to be cumulated
	 *
	 * \details
	 * Name of File into which all files hat to be cumulated
	 *
	 * \mandatory: false
	 *
	 * \created 08.08.2012 10:49:48 by KB
	 */
	@JSOptionDefinition(
						name = "cumulative_filename",
						description = "Name of File into which all files hat to be cumulated",
						key = "cumulative_filename",
						type = "SOSOptionFileName",
						mandatory = true)
	public SOSOptionFileName	CumulativeFileName	= new SOSOptionFileName(
													// ...
															this, // ....
															conClassName + ".cumulative_filename", // ...
															"Name of File into which all files hat to be cumulated", // ...
															"", // ...
															"", // ...
															false);

	public String getcumulative_filename() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getcumulative_filename";
		return CumulativeFileName.Value();
	} // public String getcumulative_filename

	public SOSFtpOptionsSuperClass setcumulative_filename(final String pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setcumulative_filename";
		CumulativeFileName.Value(pstrValue);
		return this;
	} // public SOSFtpOptionsSuperClass setcumulative_filename
	/**
	 * \option cumulative_file_separator
	 * \type SOSOptionString
	 * \brief cumulative_file_separator - Text which has to beplaced between cumulated files
	 *
	 * \details
	 * Text which has to beplaced between cumulated files.
	 * A text string or a file name, which contains the text of the file separator, is a possible value.
	 *
	 * \mandatory: false
	 *
	 * \created 08.08.2012 10:52:55 by KB
	 */
	@JSOptionDefinition(
						name = "cumulative_file_separator",
						description = "Text which has to beplaced between cumulated files",
						key = "cumulative_file_separator",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionString	CumulativeFileSeparator	= new SOSOptionString(
													// ...
															this, // ....
															conClassName + ".cumulative_file_separator", // ...
															"Text which has to beplaced between cumulated files", // ...
															"", // ...
															"", // ...
															false);

	public String getcumulative_file_separator() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getcumulative_file_separator";
		return CumulativeFileSeparator.Value();
	} // public String getcumulative_file_separator

	public SOSFtpOptionsSuperClass setcumulative_file_separator(final String pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setcumulative_file_separator";
		CumulativeFileSeparator.Value(pstrValue);
		return this;
	} // public SOSFtpOptionsSuperClass setcumulative_file_separator
	/**
	 * \option cumulative_file_delete
	 * \type SOSOptionBoolean
	 * \brief cumulative_file_delete - Delete cumulative file before starting transfer
	 *
	 * \details
	 * Delete cumulative file before starting transfer
	 *
	 * \mandatory: false
	 *
	 * \created 08.08.2012 13:52:58 by KB
	 */
	@JSOptionDefinition(
						name = "cumulative_file_delete",
						description = "Delete cumulative file before starting transfer",
						key = "cumulative_file_delete",
						type = "SOSOptionBoolean",
						mandatory = false)
	public SOSOptionBoolean	CumulativeFileDelete	= new SOSOptionBoolean(
													// ...
															this, // ....
															conClassName + ".cumulative_file_delete", // ...
															"Delete cumulative file before starting transfer", // ...
															"false", // ...
															"false", // ...
															false);

	public String getcumulative_file_delete() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getcumulative_file_delete";
		return CumulativeFileDelete.Value();
	} // public String getcumulative_file_delete

	public SOSFtpOptionsSuperClass setcumulative_file_delete(final String pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setcumulative_file_delete";
		CumulativeFileDelete.Value(pstrValue);
		return this;
	} // public SOSFtpOptionsSuperClass setcumulative_file_delete
	/**
	 * \option Post_Command
	 * \type SOSOptionString
	 * \brief Post_Command - FTP-Command to be executed after transfer
	 *
	 * \details
	 * FTP-Command to be executed after transfer for each file.
	 *
	 * \mandatory: false
	 *
	 * \created 12.10.2011 12:28:42 by KB
	 */
	@JSOptionDefinition(
						name = "Post_Command",
						description = "FTP-Command to be executed after transfer",
						key = "Post_Command",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionCommandString	Post_Command	= new SOSOptionCommandString(
													// ...
															this, // ....
															conClassName + ".Post_Command", // ...
															"FTP-Command to be executed after transfer", // ...
															"", // ...
															"", // ...
															false);

	public String getPost_Command() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getPost_Command";
		return Post_Command.Value();
	} // public String getPost_Command

	public SOSFtpOptionsSuperClass setPost_Command(final String pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setPost_Command";
		Post_Command.Value(pstrValue);
		return this;
	} // public SOSFtpOptionsSuperClass setPost_Command
	/**
	 * \option Pre_Command
	 * \type SOSOptionString
	 * \brief Pre_Command - A FTP-Command to be execute before transfer of each file is starting
	 *
	 * \details
	 *
	 *
	 * \mandatory: false
	 *
	 * \created 12.10.2011 12:25:27 by KB
	 */
	@JSOptionDefinition(
						name = "Pre_Command",
						description = "FTP-Command to be execute before transfer",
						key = "Pre_Command",
						type = "SOSOptionString  ",
						mandatory = false)
	public SOSOptionCommandString	Pre_Command	= new SOSOptionCommandString(
												// ...
														this, // ....
														conClassName + ".Pre_Command", // ...
														"", // ...
														"", // ...
														"", // ...
														false);

	public String getPre_Command() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getPre_Command";
		return Pre_Command.Value();
	} // public String getPre_Command

	public SOSFtpOptionsSuperClass setPre_Command(final String pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setPre_Command";
		Pre_Command.Value(pstrValue);
		return this;
	} // public SOSFtpOptionsSuperClass setPre_Command
	/**
	 * \option CheckServerFeatures
	 * \type SOSOptionBoolean
	 * \brief CheckServerFeatures - get the provided features of a ftp-server
	 *
	 * \details
	 * The available features of a ftp-server
	 *
	 * \mandatory: false
	 *
	 * \created 27.06.2011 12:02:51 by KB
	 */
	@JSOptionDefinition(
						name = "CheckServerFeatures",
						description = "The available features of a ftp-server",
						key = "Check_Server_Features",
						type = "SOSOptionBoolean",
						mandatory = false)
	public SOSOptionBoolean	CheckServerFeatures	= new SOSOptionBoolean(
												// ...
														this, // ....
														conClassName + ".Check_Server_Features", // ...
														"The available features of a ftp-server", // ...
														"false", // ...
														"false", // ...
														false);

	@Override public SOSOptionBoolean CheckServerFeatures() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getCheckServerFeatures";
		return CheckServerFeatures;
	} // public String getCheckServerFeatures

	public String getCheckServerFeatures() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getCheckServerFeatures";
		return CheckServerFeatures.Value();
	} // public String getCheckServerFeatures

	public SOSFtpOptionsSuperClass setCheckServerFeatures(final String pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setCheckServerFeatures";
		CheckServerFeatures.Value(pstrValue);
		return this;
	} // public SOSFtpOptionsSuperClass setCheckServerFeatures
	/**
	 * \option PollKeepConnection
	 * \type SOSOptionBoolean
	 * \brief PollKeepConnection - Keep connection during polling
	 *
	 * \details
	 * With this parameter it is possible to close the connection to the server
	 * after an unsucsesfull polling cycle.
	 * The connection will be established before the next cycle will start.
	 *
	 * \mandatory: false
	 *
	 * \created 20.06.2011 11:53:23 by KB
	 */
	@JSOptionDefinition(
						name = "PollKeepConnection",
						description = "Keep connection while polling",
						key = "PollKeepConnection",
						type = "SOSOptionBoolean",
						mandatory = true)
	public SOSOptionBoolean	PollKeepConnection	= new SOSOptionBoolean(
												// ...
														this, // ....
														conClassName + ".PollKeepConnection", // ...
														"Keep connection while polling", // ...
														"false", // ...
														"false", // ...
														true);

	public String getPollKeepConnection() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getPollKeepConnection";
		return PollKeepConnection.Value();
	} // public String getPollKeepConnection

	public SOSFtpOptionsSuperClass setPollKeepConnection(final String pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setPollKeepConnection";
		PollKeepConnection.Value(pstrValue);
		return this;
	} // public SOSFtpOptionsSuperClass setPollKeepConnection
	/**
	 * \option FileNameEncoding
	 * \type SOSOptionString
	 * \brief FileNameEncoding - Set the encoding-type of a file name
	 *
	 * \details
	 * Set the encoding-type of a file name
	 *
	 * \mandatory: false
	 *
	 * \created 27.06.2011 12:20:47 by KB
	 */
	@JSOptionDefinition(
						name = "FileNameEncoding",
						description = "Set the encoding-type of a file name",
						key = "FileNameEncoding",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionString	FileNameEncoding	= new SOSOptionString(
												// ...
														this, // ....
														conClassName + ".FileNameEncoding", // ...
														"Set the encoding-type of a file name", // ...
														"", // ...
														"ISO-8859-1", // ...
														false);

	public String getFileNameEncoding() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getFileNameEncoding";
		return FileNameEncoding.Value();
	} // public String getFileNameEncoding

	public SOSFtpOptionsSuperClass setFileNameEncoding(final String pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setFileNameEncoding";
		FileNameEncoding.Value(pstrValue);
		return this;
	} // public SOSFtpOptionsSuperClass setFileNameEncoding
	
	/**
	 * \option ControlEncoding
	 * \type SOSOptionString
	 * \brief ControlEncoding - Specify the encoding-type used by the server
	 *
	 * \details
	 * Specify the encoding-type, e.g. utf-8 or iso-8859-1, used by the server
	 *
	 * \mandatory: false
	 *
	 * \created 20.05.2011 12:05:40 by KB
	 */
	@JSOptionDefinition(
						name = "ControlEncoding",
						description = "Specify the encoding-type, e.g. utf-8, used by the server",
						key = "ControlEncoding",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionEncoding	ControlEncoding	= new SOSOptionEncoding(
												// ...
														this, // ....
														conClassName + ".ControlEncoding", // ...
														"Specify the encoding-type, e.g. utf-8, used by the server", // ...
														"", // ...
														"", // ...
														false);

	public String getControlEncoding() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getControlEncoding";
		return ControlEncoding.Value();
	} // public String getControl_Encoding

	public SOSFtpOptionsSuperClass setControlEncoding(final String pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setControlEncoding";
		ControlEncoding.Value(pstrValue);
		return this;
	} // public SOSFtpOptionsSuperClass setControl_Encoding
	/**
	 * \option History_File_Append_Mode
	 * \type SOSOptionBoolean
	 * \brief History_File_Append_Mode - Specifies wether the History File has to be written in append mode
	 *
	 * \details
	 * Specifies wether the History File has to be written in append mode
	 *
	 * \mandatory: false
	 *
	 * \created 04.04.2014 17:38:52 by KB
	 */
	@JSOptionDefinition(
						name = "History_File_Append_Mode",
						description = "Specifies wether the History File has to be written in append mode",
						key = "History_File_Append_Mode",
						type = "SOSOptionBoolean",
						mandatory = false)
	public SOSOptionBoolean	HistoryFileAppendMode	= new SOSOptionBoolean( // ...
															this, // ....
															conClassName + ".History_File_Append_Mode", // ...
															"Specifies wether the History File has to be written in append mode", // ...
															"false", // ...
															"false", // ...
															false);

	public SOSOptionBoolean getHistoryFileAppendMode() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getHistory_File_Append_Mode";
		return HistoryFileAppendMode;
	} // public String getHistory_File_Append_Mode

	public SOSFtpOptionsSuperClass setHistoryFileAppendMode(final SOSOptionBoolean pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setHistory_File_Append_Mode";
		HistoryFileAppendMode = pstrValue;
		return this;
	} // public SOSFtpOptionsSuperClass setHistory_File_Append_Mode
	/**
	 * \option HistoryEntries
	 * \type SOSOptionArrayList
	 * \brief HistoryEntries - List of additional entries for the transfer history
	 *
	 * \details
	 * List of additional entries for the transfer history.
	 *
	 * \mandatory: false
	 *
	 * \created 26.04.2011 21:21:45 by KB
	 */
	@JSOptionDefinition(
						name = "HistoryEntries",
						description = "List of additional entries for the transfer history record.",
						key = "HistoryEntries",
						type = "SOSOptionArrayList",
						mandatory = false)
	public SOSOptionArrayList	HistoryEntries	= new SOSOptionArrayList(
												// ...
														this, // ....
														conClassName + ".HistoryEntries", // ...
														"List of additional entries for the transfer history record.", // ...
														"", // ...
														"", // ...
														false);

	public String getHistoryEntries() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getHistoryEntries";
		return HistoryEntries.Value();
	} // public String getHistoryEntries

	public SOSFtpOptionsSuperClass setHistoryEntries(final String pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setHistoryEntries";
		HistoryEntries.Value(pstrValue);
		return this;
	} // public SOSFtpOptionsSuperClass setHistoryEntries
	/**
	 * \option SendTransferHistory
	 * \type SOSOptionBoolean
	 * \brief SendTransferHistory - Send transfer history to background service
	 *
	 * \details
	 * If this option is set to true, the the transfer history will be sent to the background service.
	 *
	 * \mandatory: false
	 *
	 * \created 26.04.2011 20:19:42 by KB
	 */
	@JSOptionDefinition(
						name = "SendTransferHistory",
						description = "If this option is set to true, the transfer history will be sent to the background service.",
						key = "SendTransferHistory",
						type = "SOSOptionBoolean",
						mandatory = true)
	public SOSOptionBoolean	SendTransferHistory	= new SOSOptionBoolean(
												// ...
														this, // ....
														conClassName + ".SendTransferHistory", // ...
														"If this option is set to true, the transfer history will be sent to the background service.", // ...
														"false", // ...
														"false", // ...
														false);

	public String getSendTransferHistory() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getSendTransferHistory";
		return SendTransferHistory.Value();
	} // public String getSendTransferHistory

	public SOSFtpOptionsSuperClass setSendTransferHistory(final String pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setSendTransferHistory";
		SendTransferHistory.Value(pstrValue);
		return this;
	} // public SchedulerObjectFactoryOptions setSendTransferHistory
	/**
	 * \option Scheduler_Transfer_Method
	 * \type SOSOptionJSTransferMethod
	 * \brief Scheduler_Transfer_Method - How to communicate with the JobScheduler
	 *
	 * \details
	 * The technical method of how to communicate with the JobScheduler
	 *
	 * \mandatory: true
	 *
	 * \created 26.04.2011 12:22:06 by KB
	 */
	@JSOptionDefinition(
						name = "Scheduler_Transfer_Method",
						description = "The technical method of how to communicate with the JobScheduler",
						key = "Scheduler_Transfer_Method",
						type = "SOSOptionJSTransferMethod",
						mandatory = true)
	public SOSOptionBackgroundServiceTransferMethod	Scheduler_Transfer_Method	= new SOSOptionBackgroundServiceTransferMethod(
																				// ...
																						this, // ....
																						conClassName + ".Scheduler_Transfer_Method", // ...
																						"The technical method of how to communicate with the JobScheduler", // ...
																						enuJSTransferModes.udp.description, // ...
																						enuJSTransferModes.udp.description, // ...
																						true);

	public String getScheduler_Transfer_Method() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getScheduler_Transfer_Method";
		return Scheduler_Transfer_Method.Value();
	} // public String getScheduler_Transfer_Method

	public SOSFtpOptionsSuperClass setScheduler_Transfer_Method(final String pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setScheduler_Transfer_Method";
		Scheduler_Transfer_Method.Value(pstrValue);
		return this;
	} // public SchedulerObjectFactoryOptions setScheduler_Transfer_Method
	/**
	 * \option PreFtpCommands
	 * \type SOSOptionString
	 * \brief PreFtpCommands - FTP commands, which has to be executed before the transfer started
	 *
	 * \details
	 * FTP commands, which has to be executed before the transfer started.
	 *
	 * see also: PostFtpCommands, PostCommand, PreCommand
	 *
	 * \mandatory: false
	 *
	 * \created 05.04.2011 15:45:52 by KB
	 */
	@JSOptionDefinition(
						name = "PreFtpCommands",
						description = "FTP commands, which has to be executed before the transfer started.",
						key = "PreFtpCommands",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionCommandString	PreFtpCommands		= new SOSOptionCommandString(
														// ...
																this, // ....
																conClassName + ".Pre_Ftp_Commands", // ...
																"FTP commands, which has to be executed before the transfer started.", // ...
																"", // ...
																"", // ...
																false);
	/**
	 * \see PreFtpCommands
	 */
	public SOSOptionCommandString			PreTransferCommands	= (SOSOptionCommandString) PreFtpCommands.SetAlias("pre_transfer_commands");

	public String getPreFtpCommands() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getPreFtpCommands";
		return PreFtpCommands.Value();
	} // public String getPreFtpCommands

	public SOSFtpOptionsSuperClass setPreFtpCommands(final String pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setPreFtpCommands";
		PreFtpCommands.Value(pstrValue);
		return this;
	} // public SOSFtpOptionsSuperClass setPreFtpCommands
	/**
	 * \option PostTransferCommands
	 * \type SOSOptionString
	 * \brief PostTransferCommands - FTP commands, which has to be executed after the transfer ended
	 *
	 * \details
	 * FTP commands, which has to be executed after the transfer ended.
	 *
	 * see also: PostFtpCommands, PostCommand, PreCommand
	 *
	 * \mandatory: false
	 *
	 * \created 05.04.2011 15:45:52 by KB
	 */
	@JSOptionDefinition(
						name = "PostTransferCommands",
						description = "FTP commands, which has to be executed after the transfer ended.",
						key = "PostTransferCommands",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionCommandString	PostTransferCommands	= new SOSOptionCommandString(
															// ...
																	this, // ....
																	conClassName + ".post_transfer_Commands", // ...
																	"FTP commands, which has to be executed after the transfer ended.", // ...
																	"", // ...
																	"", // ...
																	false);
	/**
	 * \see PostTransferCommands
	 */
	public SOSOptionString			PostFtpCommands			= (SOSOptionString) PostTransferCommands.SetAlias("post_Transfer_commands");

	public String getPostTransferCommands() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getPostTransferCommands";
		return PostTransferCommands.Value();
	} // public String getPostTransferCommands

	public SOSFtpOptionsSuperClass setPostTransferCommands(final String pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setPostTransferCommands";
		PostTransferCommands.Value(pstrValue);
		return this;
	} // public SOSFtpOptionsSuperClass setPostTransferCommands
		// see http://docs.oracle.com/javase/7/docs/technotes/guides/security/StandardNames.html#MessageDigest
	@JSOptionDefinition(
						name = "IntegrityHashType",
						description = "",
						key = "integrity_hash_type",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionString	IntegrityHashType		= new SOSOptionString(this, conClassName + ".integrity_hash_type", // HashMap-Key
															"The Type of the integrity hash, e.g. md5", // Titel
															"md5", // InitValue
															"md5", // DefaultValue
															false // isMandatory
													);
	public SOSOptionString	SecurityHashType		= (SOSOptionString) IntegrityHashType.SetAlias("security_hash_type");
	
	@JSOptionDefinition(
						name = "DecompressAfterTransfer",
						description = "",
						key = "Decompress_After_Transfer",
						type = "SOSOptionBoolean",
						mandatory = false)
	public SOSOptionBoolean	DecompressAfterTransfer	= new SOSOptionBoolean(this, conClassName + ".Decompress_After_Transfer", // HashMap-Key
															"Decompress zipped-files after transfer", // Titel
															"false", // InitValue
															"false", // DefaultValue
															false // isMandatory
													);
	@JSOptionDefinition(
						name = "ConcurrentTransfer",
						description = "",
						key = "Concurrent_Transfer",
						type = "SOSOptionBoolean",
						mandatory = false)
	public SOSOptionBoolean	ConcurrentTransfer		= new SOSOptionBoolean(this, conClassName + ".Concurrent_Transfer", // HashMap-Key
															"Process transfers simultaneously", // Titel
															"false", // InitValue
															"false", // DefaultValue
															false // isMandatory
													);
	@JSOptionDefinition(
						name = "CheckIntegrityHash",
						description = "",
						key = "check_integrity_hash",
						type = "SOSOptionBoolean",
						mandatory = false)
	public SOSOptionBoolean	CheckIntegrityHash		= new SOSOptionBoolean(this, conClassName + ".check_integrity_hash", // HashMap-Key
															"Calculates the integrity hash", // Titel
															"false", // InitValue
															"false", // DefaultValue
															false // isMandatory
													);
	public SOSOptionBoolean	CheckSecurityHash	= (SOSOptionBoolean) CheckIntegrityHash.SetAlias("check_security_hash");
	
	@JSOptionDefinition(
						name = "MaxConcurrentTransfers",
						description = "",
						key = "Max_Concurrent_Transfers",
						type = "SOSOptionInteger",
						mandatory = false)
	public SOSOptionInteger	MaxConcurrentTransfers	= new SOSOptionInteger(this, conClassName + ".Max_Concurrent_Transfers", // HashMap-Key
															"Maximum Numbers of parallel transfers", // Titel
															"5", // InitValue
															"1", // DefaultValue
															false // isMandatory
													);
	@JSOptionDefinition(
						name = "CreateIntegrityHashFile",
						description = "",
						key = "create_integrity_hash_file",
						type = "SOSOptionBoolean",
						mandatory = false)
	public SOSOptionBoolean	CreateIntegrityHashFile	= new SOSOptionBoolean(this, conClassName + ".create_integrity_hash_file", // HashMap-Key
															"Flag if an integrity hash file will be created on the target", // Titel
															"false", // InitValue
															"false", // DefaultValue
															false // isMandatory
													);
	
	public SOSOptionBoolean	CreateSecurityHashFile	= (SOSOptionBoolean) CreateIntegrityHashFile.SetAlias("create_security_hash_file");
	
	@JSOptionDefinition(
						name = "BufferSize",
						description = "",
						key = "buffer_Size",
						type = "SOSOptionInteger",
						mandatory = false)
	public SOSOptionInteger	BufferSize				= new SOSOptionInteger(this, conClassName + ".buffer_Size", // HashMap-Key
															"This parameter specifies the interval in seconds", // Titel
															"32000", // InitValue
															"4096", // DefaultValue
															false // isMandatory
													);
	/**
	 * \var create_order : Activate file-order creation With this parameter it is possible to specif
	 *
	 *
	 */
	@JSOptionDefinition(
						name = "create_order",
						description = "Activate file-order creation With this parameter it is possible to specif",
						key = "create_order",
						type = "SOSOptionBoolean",
						mandatory = false)
	public SOSOptionBoolean	create_order			= new SOSOptionBoolean(this, conClassName + ".create_order", // HashMap-Key
															"Activate file-order creation With this parameter it is possible to specif", // Titel
															"false", // InitValue
															"false", // DefaultValue
															false // isMandatory
													);

	/**
	 * \brief getcreate_order : Activate file-order creation With this parameter it is possible to specif
	 *
	 * \details
	 *
	 *
	 * \return Activate file-order creation With this parameter it is possible to specif
	 *
	 */
	public SOSOptionBoolean getcreate_order() {
		return create_order;
	}

	/**
	 * \brief setcreate_order : Activate file-order creation With this parameter it is possible to specif
	 *
	 * \details
	 *
	 *
	 * @param create_order : Activate file-order creation With this parameter it is possible to specif
	 */
	public void setcreate_order(final SOSOptionBoolean p_create_order) {
		create_order = p_create_order;
	}
	/**
	 * \var create_orders_for_all_files : Create a file-order for every file in the result-list
	 *
	 *
	 */
	@JSOptionDefinition(
						name = "create_orders_for_all_files",
						description = "Create a file-order for every file in the result-list",
						key = "create_orders_for_all_files",
						type = "SOSOptionBoolean",
						mandatory = false)
	public SOSOptionBoolean	create_orders_for_all_files	= new SOSOptionBoolean(this, conClassName + ".create_orders_for_all_files", // HashMap-Key
																"Create a file-order for every file in the result-list", // Titel
																"false", // InitValue
																"false", // DefaultValue
																false // isMandatory
														);

	/**
	 * \brief getcreate_orders_for_all_files : Create a file-order for every file in the result-list
	 *
	 * \details
	 *
	 *
	 * \return Create a file-order for every file in the result-list
	 *
	 */
	public SOSOptionBoolean getcreate_orders_for_all_files() {
		return create_orders_for_all_files;
	}

	/**
	 * \brief setcreate_orders_for_all_files : Create a file-order for every file in the result-list
	 *
	 * \details
	 *
	 *
	 * @param create_orders_for_all_files : Create a file-order for every file in the result-list
	 */
	public void setcreate_orders_for_all_files(final SOSOptionBoolean p_create_orders_for_all_files) {
		create_orders_for_all_files = p_create_orders_for_all_files;
	}
	/**
	 * \var expected_size_of_result_set : number of expected hits in result-list
	 *
	 *
	 */
	@JSOptionDefinition(
						name = "expected_size_of_result_set",
						description = "number of expected hits in result-list",
						key = "expected_size_of_result_set",
						type = "SOSOptionInteger",
						mandatory = false)
	public SOSOptionInteger	expected_size_of_result_set	= new SOSOptionInteger(this, conClassName + ".expected_size_of_result_set", // HashMap-Key
																"number of expected hits in result-list", // Titel
																"0", // InitValue
																"0", // DefaultValue
																false // isMandatory
														);

	/**
	 * \brief getexpected_size_of_result_set : number of expected hits in result-list
	 *
	 * \details
	 *
	 *
	 * \return number of expected hits in result-list
	 *
	 */
	public SOSOptionInteger getexpected_size_of_result_set() {
		return expected_size_of_result_set;
	}

	/**
	 * \brief setexpected_size_of_result_set : number of expected hits in result-list
	 *
	 * \details
	 *
	 *
	 * @param expected_size_of_result_set : number of expected hits in result-list
	 */
	public void setexpected_size_of_result_set(final SOSOptionInteger p_expected_size_of_result_set) {
		expected_size_of_result_set = p_expected_size_of_result_set;
	}
	/**
	 * \var file : File or Folder to watch for Checked file or directory Supports
	 *
	 *
	 */
	@JSOptionDefinition(
						name = "file",
						description = "File or Folder to watch for Checked file or directory Supports",
						key = "file",
						type = "SOSOptionString",
						mandatory = true)
	public SOSOptionFileName	file	= new SOSOptionFileName(this, conClassName + ".file", // HashMap-Key
												"File or Folder to watch for Checked file or directory Supports", // Titel
												".", // InitValue
												".", // DefaultValue
												true // isMandatory
										);
	@JSOptionDefinition(
						name = "target",
						description = "target or Folder to watch for Checked target or directory Supports",
						key = "target",
						type = "SOSOptionString",
						mandatory = true)
	public SOSOptionFileName	target	= new SOSOptionFileName(this, conClassName + ".target", // HashMap-Key
												"target or Folder to watch for Checked target or directory Supports", // Titel
												".", // InitValue
												".", // DefaultValue
												true // isMandatory
										);

	/**
	 * \brief getfile : File or Folder to watch for Checked file or directory Supports
	 *
	 * \details
	 *
	 *
	 * \return File or Folder to watch for Checked file or directory Supports
	 *
	 */
	public SOSOptionFileName getfile() {
		return file;
	}

	/**
	 * \brief setfile : File or Folder to watch for Checked file or directory Supports
	 *
	 * \details
	 *
	 *
	 * @param file : File or Folder to watch for Checked file or directory Supports
	 */
	public void setfile(final SOSOptionFileName p_file) {
		file = p_file;
	}
	public SOSOptionFileName	FileName		= (SOSOptionFileName) file.SetAlias(conClassName + ".FileName");
	// /**
	// * \var gracious : Specify error message tolerance Enables or disables error messages that
	// *
	// *
	// */
	// @JSOptionDefinition(name = "gracious", description = "Specify error message tolerance Enables or disables error messages that", key =
	// "gracious", type = "SOSOptionGracious", mandatory = false)
	// public SOSOptionGracious gracious = new SOSOptionGracious(this, conClassName + ".gracious", // HashMap-Key
	// "Specify error message tolerance Enables or disables error messages that", // Titel
	// "false", // InitValue
	// "false", // DefaultValue
	// false // isMandatory
	// );
	//
	// /**
	// * \brief getgracious : Specify error message tolerance Enables or disables error messages that
	// *
	// * \details
	// *
	// *
	// * \return Specify error message tolerance Enables or disables error messages that
	// *
	// */
	// public SOSOptionGracious getgracious() {
	// return gracious;
	// }
	//
	// /**
	// * \brief setgracious : Specify error message tolerance Enables or disables error messages that
	// *
	// * \details
	// *
	// *
	// * @param gracious : Specify error message tolerance Enables or disables error messages that
	// */
	// public void setgracious(SOSOptionGracious p_gracious) {
	// this.gracious = p_gracious;
	// }
	//
	// public SOSOptionGracious ErrorBehaviour = (SOSOptionGracious) gracious.SetAlias(conClassName + ".ErrorBehaviour");
	/**
	 * \var max_file_age : maximum age of a file Specifies the maximum age of a file. If a file
	 *
	 *
	 */
	@JSOptionDefinition(
						name = "max_file_age",
						description = "maximum age of a file Specifies the maximum age of a file. If a file",
						key = "max_file_age",
						type = "SOSOptionTime",
						mandatory = false)
	public SOSOptionTime		max_file_age	= new SOSOptionTime(this, conClassName + ".max_file_age", // HashMap-Key
														"maximum age of a file Specifies the maximum age of a file. If a file", // Titel
														"0", // InitValue
														"0", // DefaultValue
														false // isMandatory
												);

	/**
	 * \brief getmax_file_age : maximum age of a file Specifies the maximum age of a file. If a file
	 *
	 * \details
	 *
	 *
	 * \return maximum age of a file Specifies the maximum age of a file. If a file
	 *
	 */
	public SOSOptionTime getmax_file_age() {
		return max_file_age;
	}

	/**
	 * \brief setmax_file_age : maximum age of a file Specifies the maximum age of a file. If a file
	 *
	 * \details
	 *
	 *
	 * @param max_file_age : maximum age of a file Specifies the maximum age of a file. If a file
	 */
	public void setmax_file_age(final SOSOptionTime p_max_file_age) {
		max_file_age = p_max_file_age;
	}
	public SOSOptionTime		FileAgeMaximum	= (SOSOptionTime) max_file_age.SetAlias(conClassName + ".FileAgeMaximum");
	/**
	 * \var max_file_size : maximum size of a file Specifies the maximum size of a file in
	 * Specifies the maximum size of a file in bytes: should the size of one of the files exceed this value, then it is classified as non-existing.
	 *
	 */
	@JSOptionDefinition(
						name = "max_file_size",
						description = "maximum size of a file Specifies the maximum size of a file in",
						key = "max_file_size",
						type = "SOSOptionFileSize",
						mandatory = false)
	public SOSOptionFileSize	max_file_size	= new SOSOptionFileSize(this, conClassName + ".max_file_size", // HashMap-Key
														"maximum size of a file Specifies the maximum size of a file in", // Titel
														"-1", // InitValue
														"-1", // DefaultValue
														false // isMandatory
												);

	/**
	 * \brief getmax_file_size : maximum size of a file Specifies the maximum size of a file in
	 *
	 * \details
	 * Specifies the maximum size of a file in bytes: should the size of one of the files exceed this value, then it is classified as non-existing.
	 *
	 * \return maximum size of a file Specifies the maximum size of a file in
	 *
	 */
	public SOSOptionFileSize getmax_file_size() {
		return max_file_size;
	}

	/**
	 * \brief setmax_file_size : maximum size of a file Specifies the maximum size of a file in
	 *
	 * \details
	 * Specifies the maximum size of a file in bytes: should the size of one of the files exceed this value, then it is classified as non-existing.
	 *
	 * @param max_file_size : maximum size of a file Specifies the maximum size of a file in
	 */
	public void setmax_file_size(final SOSOptionFileSize p_max_file_size) {
		max_file_size = p_max_file_size;
	}
	public SOSOptionFileSize	FileSizeMaximum	= (SOSOptionFileSize) max_file_size.SetAlias(conClassName + ".FileSizeMaximum");
	/**
	 * \var min_file_age : minimum age of a file Specifies the minimum age of a files. If the fi
	 * Specifies the minimum age of a files. If the file(s) is newer then it is classified as non-existing, it will be not included in the result-list.
	 *
	 */
	@JSOptionDefinition(
						name = "min_file_age",
						description = "minimum age of a file Specifies the minimum age of a files. If the fi",
						key = "min_file_age",
						type = "SOSOptionTime",
						mandatory = false)
	public SOSOptionTime		min_file_age	= new SOSOptionTime(this, conClassName + ".min_file_age", // HashMap-Key
														"minimum age of a file Specifies the minimum age of a files. If the fi", // Titel
														"0", // InitValue
														"0", // DefaultValue
														false // isMandatory
												);

	/**
	 * \brief getmin_file_age : minimum age of a file Specifies the minimum age of a files. If the fi
	 *
	 * \details
	 * Specifies the minimum age of a files. If the file(s) is newer then it is classified as non-existing, it will be not included in the result-list.
	 *
	 * \return minimum age of a file Specifies the minimum age of a files. If the fi
	 *
	 */
	public SOSOptionTime getmin_file_age() {
		return min_file_age;
	}

	/**
	 * \brief setmin_file_age : minimum age of a file Specifies the minimum age of a files. If the fi
	 *
	 * \details
	 * Specifies the minimum age of a files. If the file(s) is newer then it is classified as non-existing, it will be not included in the result-list.
	 *
	 * @param min_file_age : minimum age of a file Specifies the minimum age of a files. If the fi
	 */
	public void setmin_file_age(final SOSOptionTime p_min_file_age) {
		min_file_age = p_min_file_age;
	}
	public SOSOptionTime		FileAgeMinimum	= (SOSOptionTime) min_file_age.SetAlias(conClassName + ".FileAgeMinimum");
	/**
	 * \var min_file_size : minimum size of one or multiple files Specifies the minimum size of one
	 *
	 *
	 */
	@JSOptionDefinition(
						name = "min_file_size",
						description = "minimum size of one or multiple files Specifies the minimum size of one",
						key = "min_file_size",
						type = "SOSOptionFileSize",
						mandatory = false)
	public SOSOptionFileSize	min_file_size	= new SOSOptionFileSize(this, conClassName + ".min_file_size", // HashMap-Key
														"minimum size of one or multiple files Specifies the minimum size of one", // Titel
														"-1", // InitValue
														"-1", // DefaultValue
														false // isMandatory
												);

	/**
	 * \brief getmin_file_size : minimum size of one or multiple files Specifies the minimum size of one
	 *
	 * \details
	 *
	 *
	 * \return minimum size of one or multiple files Specifies the minimum size of one
	 *
	 */
	public SOSOptionFileSize getmin_file_size() {
		return min_file_size;
	}

	/**
	 * \brief setmin_file_size : minimum size of one or multiple files Specifies the minimum size of one
	 *
	 * \details
	 *
	 *
	 * @param min_file_size : minimum size of one or multiple files Specifies the minimum size of one
	 */
	public void setmin_file_size(final SOSOptionFileSize p_min_file_size) {
		min_file_size = p_min_file_size;
	}
	public SOSOptionFileSize	FileSizeMinimum		= (SOSOptionFileSize) min_file_size.SetAlias(conClassName + ".FileSizeMinimum");
	/**
	 * \option MergeOrderParameter
	 * \type SOSOptionBoolean
	 * \brief MergeOrderParameter - Merge created order parameter with parameter of current order
	 *
	 * \details
	 * Merge created order parameter with parameter of current order
	 *
	 * \mandatory: false
	 *
	 * \created 03.08.2012 15:25:38 by KB
	 */
	@JSOptionDefinition(
						name = "MergeOrderParameter",
						description = "Merge created order parameter with parameter of current order",
						key = "MergeOrderParameter",
						type = "SOSOptionBoolean",
						mandatory = false)
	public SOSOptionBoolean		MergeOrderParameter	= new SOSOptionBoolean(
													// ...
															this, // ....
															conClassName + ".MergeOrderParameter", // ...
															"Merge created order parameter with parameter of current order", // ...
															"false", // ...
															"false", // ...
															false);

	public String getMergeOrderParameter() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getMergeOrderParameter";
		return MergeOrderParameter.Value();
	} // public String getMergeOrderParameter

	public SOSFtpOptionsSuperClass setMergeOrderParameter(final String pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setMergeOrderParameter";
		MergeOrderParameter.Value(pstrValue);
		return this;
	} // public SOSFtpOptionsSuperClass setMergeOrderParameter
	/**
	 * \var next_state : The first node to execute in a jobchain The name of the node of a jobchai
	 *
	 *
	 */
	@JSOptionDefinition(
						name = "next_state",
						description = "The first node to execute in a jobchain The name of the node of a jobchai",
						key = "next_state",
						type = "SOSOptionJobChainNode",
						mandatory = false)
	public SOSOptionJobChainNode	next_state	= new SOSOptionJobChainNode(this, conClassName + ".next_state", // HashMap-Key
														"The first node to execute in a jobchain The name of the node of a jobchai", // Titel
														"", // InitValue
														"", // DefaultValue
														false // isMandatory
												);

	/**
	 * \brief getnext_state : The first node to execute in a jobchain The name of the node of a jobchai
	 *
	 * \details
	 *
	 *
	 * \return The first node to execute in a jobchain The name of the node of a jobchai
	 *
	 */
	public SOSOptionJobChainNode getnext_state() {
		return next_state;
	}

	/**
	 * \brief setnext_state : The first node to execute in a jobchain The name of the node of a jobchai
	 *
	 * \details
	 *
	 *
	 * @param next_state : The first node to execute in a jobchain The name of the node of a jobchai
	 */
	public void setnext_state(final SOSOptionJobChainNode p_next_state) {
		next_state = p_next_state;
	}
	/**
	 * \var on_empty_result_set : Set next node on empty result set The next Node (Step, Job) to execute i
	 *
	 *
	 */
	@JSOptionDefinition(
						name = "on_empty_result_set",
						description = "Set next node on empty result set The next Node (Step, Job) to execute i",
						key = "on_empty_result_set",
						type = "SOSOptionJobChainNode",
						mandatory = false)
	public SOSOptionJobChainNode	on_empty_result_set	= new SOSOptionJobChainNode(this, conClassName + ".on_empty_result_set", // HashMap-Key
																"Set next node on empty result set The next Node (Step, Job) to execute i", // Titel
																"", // InitValue
																"", // DefaultValue
																false // isMandatory
														);

	/**
	 * \brief geton_empty_result_set : Set next node on empty result set The next Node (Step, Job) to execute i
	 *
	 * \details
	 *
	 *
	 * \return Set next node on empty result set The next Node (Step, Job) to execute i
	 *
	 */
	public SOSOptionJobChainNode geton_empty_result_set() {
		return on_empty_result_set;
	}

	/**
	 * \brief seton_empty_result_set : Set next node on empty result set The next Node (Step, Job) to execute i
	 *
	 * \details
	 *
	 *
	 * @param on_empty_result_set : Set next node on empty result set The next Node (Step, Job) to execute i
	 */
	public void seton_empty_result_set(final SOSOptionJobChainNode p_on_empty_result_set) {
		on_empty_result_set = p_on_empty_result_set;
	}
	
	/**
	 * Name of Jobscheduler Host where the order have to be started
	 */
	@JSOptionDefinition(name = "order_jobscheduler_host", description = "Name of Jobscheduler Host where the order have to be started", key = "order_jobscheduler_host", type = "SOSOptionHostName", mandatory = false)
	public SOSOptionHostName	order_jobscheduler_host	= new SOSOptionHostName( // ...
																		this, // ....
																		conClassName + ".order_jobscheduler_host", // ...
																		"Name of Jobscheduler Host where the order have to be started", // ...
																		"", // ...
																		"", // ...
																		false);

	/**
	 * 
	 * @return
	 */
	public SOSOptionHostName getorder_jobscheduler_host() {
		return order_jobscheduler_host;
	} 

	/**
	 * 
	 * @param hostName
	 */
	public void setorder_jobscheduler_host(final SOSOptionHostName hostName) {
		order_jobscheduler_host = hostName;
	}
	
	/**
	 * The port of the JobScheduler Node
	 */
	@JSOptionDefinition(name = "order_jobscheduler_port", description = "The port of the JobScheduler node", key = "order_jobscheduler_port", type = "SOSOptionPortNumber", mandatory = false)
	public SOSOptionPortNumber	order_jobscheduler_port	= new SOSOptionPortNumber( // ...
																		this, // ....
																		conClassName + ".order_jobscheduler_port", // ...
																		"The port of the JobScheduler node", // ...
																		"", // ...
																		"4444", // ...
																		false);

	/**
	 * 
	 * @return
	 */
	public SOSOptionPortNumber getorder_jobscheduler_port() {
		return order_jobscheduler_port;
	}
	
	/**
	 * 
	 * @param portNumber
	 */
	public void setorder_jobscheduler_port(final SOSOptionPortNumber portNumber) {
		order_jobscheduler_port = portNumber;
	}
	
	/**
	 * \var order_jobchain_name : The name of the jobchain which belongs to the order The name of the jobch
	 *
	 *
	 */
	@JSOptionDefinition(
						name = "order_jobchain_name",
						description = "The name of the jobchain which belongs to the order The name of the jobch",
						key = "order_jobchain_name",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionString	order_jobchain_name	= new SOSOptionString(this, conClassName + ".order_jobchain_name", // HashMap-Key
														"The name of the jobchain which belongs to the order The name of the jobch", // Titel
														"", // InitValue
														"", // DefaultValue
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
	public void setorder_jobchain_name(final SOSOptionString p_order_jobchain_name) {
		order_jobchain_name = p_order_jobchain_name;
	}
	/**
	 * \var raise_error_if_result_set_is : raise error on expected size of result-set With this parameter it is poss
	 *
	 *
	 */
	@JSOptionDefinition(
						name = "raise_error_if_result_set_is",
						description = "raise error on expected size of result-set With this parameter it is poss",
						key = "raise_error_if_result_set_is",
						type = "SOSOptionRelOp",
						mandatory = false)
	public SOSOptionRelOp	raise_error_if_result_set_is	= new SOSOptionRelOp(this, conClassName + ".raise_error_if_result_set_is", // HashMap-Key
																	"raise error on expected size of result-set With this parameter it is poss", // Titel
																	"", // InitValue
																	"", // DefaultValue
																	false // isMandatory
															);

	/**
	 * \brief getraise_error_if_result_set_is : raise error on expected size of result-set With this parameter it is poss
	 *
	 * \details
	 *
	 *
	 * \return raise error on expected size of result-set With this parameter it is poss
	 *
	 */
	public SOSOptionRelOp getraise_error_if_result_set_is() {
		return raise_error_if_result_set_is;
	}

	/**
	 * \brief setraise_error_if_result_set_is : raise error on expected size of result-set With this parameter it is poss
	 *
	 * \details
	 *
	 *
	 * @param raise_error_if_result_set_is : raise error on expected size of result-set With this parameter it is poss
	 */
	public void setraise_error_if_result_set_is(final SOSOptionRelOp p_raise_error_if_result_set_is) {
		raise_error_if_result_set_is = p_raise_error_if_result_set_is;
	}
	/**
	 * \var result_list_file : Name of the result-list file If the value of this parameter specifies a v
	 *
	 *
	 */
	@JSOptionDefinition(
						name = "result_list_file",
						description = "Name of the result-list file If the value of this parameter specifies a v",
						key = "result_list_file",
						type = "SOSOptionFileName",
						mandatory = false)
	public SOSOptionFileName	result_list_file	= new SOSOptionFileName(this, conClassName + ".result_list_file", // HashMap-Key
															"Name of the result-list file If the value of this parameter specifies a v", // Titel
															"", // InitValue
															"", // DefaultValue
															false // isMandatory
													);

	/**
	 * \brief getresult_list_file : Name of the result-list file If the value of this parameter specifies a v
	 *
	 * \details
	 *
	 *
	 * \return Name of the result-list file If the value of this parameter specifies a v
	 *
	 */
	public SOSOptionFileName getresult_list_file() {
		return result_list_file;
	}

	/**
	 * \brief setresult_list_file : Name of the result-list file If the value of this parameter specifies a v
	 *
	 * \details
	 *
	 *
	 * @param result_list_file : Name of the result-list file If the value of this parameter specifies a v
	 */
	public void setresult_list_file(final SOSOptionFileName p_result_list_file) {
		result_list_file = p_result_list_file;
	}
	/**
	 * \var scheduler_file_name : Name of the file to process for a file-order
	 *
	 *
	 */
	@JSOptionDefinition(
						name = "scheduler_file_name",
						description = "Name of the file to process for a file-order",
						key = "scheduler_file_name",
						type = "SOSOptionFileName",
						mandatory = false)
	public SOSOptionFileName	scheduler_file_name	= new SOSOptionFileName(this, conClassName + ".scheduler_file_name", // HashMap-Key
															"Name of the file to process for a file-order", // Titel
															"", // InitValue
															"", // DefaultValue
															false // isMandatory
													);

	/**
	 * \brief getscheduler_file_name : Name of the file to process for a file-order
	 *
	 * \details
	 *
	 *
	 * \return Name of the file to process for a file-order
	 *
	 */
	public SOSOptionFileName getscheduler_file_name() {
		return scheduler_file_name;
	}

	/**
	 * \brief setscheduler_file_name : Name of the file to process for a file-order
	 *
	 * \details
	 *
	 *
	 * @param scheduler_file_name : Name of the file to process for a file-order
	 */
	public void setscheduler_file_name(final SOSOptionFileName p_scheduler_file_name) {
		scheduler_file_name = p_scheduler_file_name;
	}
	/**
	 * \var scheduler_file_parent : pathanme of the file to process for a file-order
	 *
	 *
	 */
	@JSOptionDefinition(
						name = "scheduler_file_parent",
						description = "pathanme of the file to process for a file-order",
						key = "scheduler_file_parent",
						type = "SOSOptionFileName",
						mandatory = false)
	public SOSOptionFileName	scheduler_file_parent	= new SOSOptionFileName(this, conClassName + ".scheduler_file_parent", // HashMap-Key
																"pathanme of the file to process for a file-order", // Titel
																"", // InitValue
																"", // DefaultValue
																false // isMandatory
														);

	/**
	 * \brief getscheduler_file_parent : pathanme of the file to process for a file-order
	 *
	 * \details
	 *
	 *
	 * \return pathanme of the file to process for a file-order
	 *
	 */
	public SOSOptionFileName getscheduler_file_parent() {
		return scheduler_file_parent;
	}

	/**
	 * \brief setscheduler_file_parent : pathanme of the file to process for a file-order
	 *
	 * \details
	 *
	 *
	 * @param scheduler_file_parent : pathanme of the file to process for a file-order
	 */
	public void setscheduler_file_parent(final SOSOptionFileName p_scheduler_file_parent) {
		scheduler_file_parent = p_scheduler_file_parent;
	}
	/**
	 * \var scheduler_file_path : file to process for a file-order Using Directory Monitoring with
	 *
	 *
	 */
	@JSOptionDefinition(
						name = "scheduler_file_path",
						description = "file to process for a file-order Using Directory Monitoring with",
						key = "scheduler_file_path",
						type = "SOSOptionFileName",
						mandatory = false)
	public SOSOptionFileName	scheduler_file_path	= new SOSOptionFileName(this, conClassName + ".scheduler_file_path", // HashMap-Key
															"file to process for a file-order Using Directory Monitoring with", // Titel
															"", // InitValue
															"", // DefaultValue
															false // isMandatory
													);

	/**
	 * \brief getscheduler_file_path : file to process for a file-order Using Directory Monitoring with
	 *
	 * \details
	 *
	 *
	 * \return file to process for a file-order Using Directory Monitoring with
	 *
	 */
	public SOSOptionFileName getscheduler_file_path() {
		return scheduler_file_path;
	}

	/**
	 * \brief setscheduler_file_path : file to process for a file-order Using Directory Monitoring with
	 *
	 * \details
	 *
	 *
	 * @param scheduler_file_path : file to process for a file-order Using Directory Monitoring with
	 */
	public void setscheduler_file_path(final SOSOptionFileName p_scheduler_file_path) {
		scheduler_file_path = p_scheduler_file_path;
	}
	// /**
	// * \var scheduler_sosfileoperations_resultset : The result of the operation as a list of items
	// *
	// *
	// */
	// @JSOptionDefinition(name = "scheduler_sosfileoperations_resultset", description = "The result of the operation as a list of items",
	// key = "scheduler_sosfileoperations_resultset", type = "SOSOptionstring", mandatory = false)
	// public SOSOptionString scheduler_sosfileoperations_resultset = new SOSOptionString(this, conClassName +
	// ".scheduler_sosfileoperations_resultset", // HashMap-Key
	// "The result of the operation as a list of items", // Titel
	// "", // InitValue
	// "", // DefaultValue
	// false // isMandatory
	// );
	//
	// /**
	// * \brief getscheduler_sosfileoperations_resultset : The result of the operation as a list of items
	// *
	// * \details
	// *
	// *
	// * \return The result of the operation as a list of items
	// *
	// */
	// public SOSOptionString getscheduler_sosfileoperations_resultset() {
	// return scheduler_sosfileoperations_resultset;
	// }
	//
	// /**
	// * \brief setscheduler_sosfileoperations_resultset : The result of the operation as a list of items
	// *
	// * \details
	// *
	// *
	// * @param scheduler_sosfileoperations_resultset : The result of the operation as a list of items
	// */
	// public void setscheduler_sosfileoperations_resultset(SOSOptionString p_scheduler_sosfileoperations_resultset) {
	// this.scheduler_sosfileoperations_resultset = p_scheduler_sosfileoperations_resultset;
	// }
	//
	// public SOSOptionString ResultSet = (SOSOptionString) scheduler_sosfileoperations_resultset.SetAlias(conClassName
	// + ".ResultSet");
	//
	/**
	 * \var scheduler_sosfileoperations_resultsetsize : The amount of hits in the result set of the operation
	 *
	 *
	 */
	@JSOptionDefinition(
						name = "scheduler_sosfileoperations_resultsetsize",
						description = "The amount of hits in the result set of the operation",
						key = "scheduler_sosfileoperations_resultsetsize",
						type = "SOSOptionsInteger",
						mandatory = false)
	public SOSOptionInteger	scheduler_sosfileoperations_resultsetsize	= new SOSOptionInteger(this, conClassName
																				+ ".scheduler_sosfileoperations_resultsetsize", // HashMap-Key
																				"The amount of hits in the result set of the operation", // Titel
																				"", // InitValue
																				"", // DefaultValue
																				false // isMandatory
																		);

	/**
	 * \brief getscheduler_sosfileoperations_resultsetsize : The amount of hits in the result set of the operation
	 *
	 * \details
	 *
	 *
	 * \return The amount of hits in the result set of the operation
	 *
	 */
	public SOSOptionInteger getscheduler_sosfileoperations_resultsetsize() {
		return scheduler_sosfileoperations_resultsetsize;
	}

	/**
	 * \brief setscheduler_sosfileoperations_resultsetsize : The amount of hits in the result set of the operation
	 *
	 * \details
	 *
	 *
	 * @param scheduler_sosfileoperations_resultsetsize : The amount of hits in the result set of the operation
	 */
	public void setscheduler_sosfileoperations_resultsetsize(final SOSOptionInteger p_scheduler_sosfileoperations_resultsetsize) {
		scheduler_sosfileoperations_resultsetsize = p_scheduler_sosfileoperations_resultsetsize;
	}
	public SOSOptionInteger	ResultSetSize		= (SOSOptionInteger) scheduler_sosfileoperations_resultsetsize.SetAlias(conClassName + ".ResultSetSize");
	/**
	 * \var skip_first_files : number of files to remove from the top of the result-set The numbe
	 *
	 *
	 */
	@JSOptionDefinition(
						name = "skip_first_files",
						description = "number of files to remove from the top of the result-set The numbe",
						key = "skip_first_files",
						type = "SOSOptionInteger",
						mandatory = false)
	public SOSOptionInteger	skip_first_files	= new SOSOptionInteger(this, conClassName + ".skip_first_files", // HashMap-Key
														"number of files to remove from the top of the result-set The numbe", // Titel
														"0", // InitValue
														"0", // DefaultValue
														false // isMandatory
												);

	/**
	 * \brief getskip_first_files : number of files to remove from the top of the result-set The numbe
	 *
	 * \details
	 *
	 *
	 * \return number of files to remove from the top of the result-set The numbe
	 *
	 */
	public SOSOptionInteger getskip_first_files() {
		return skip_first_files;
	}

	/**
	 * \brief setskip_first_files : number of files to remove from the top of the result-set The numbe
	 *
	 * \details
	 *
	 *
	 * @param skip_first_files : number of files to remove from the top of the result-set The numbe
	 */
	public void setskip_first_files(final SOSOptionInteger p_skip_first_files) {
		skip_first_files = p_skip_first_files;
	}
	public SOSOptionInteger	NoOfFirstFiles2Skip	= (SOSOptionInteger) skip_first_files.SetAlias(conClassName + ".NoOfFirstFiles2Skip");
	/**
	 * \var skip_last_files : number of files to remove from the bottom of the result-set The numbe
	 *
	 *
	 */
	@JSOptionDefinition(
						name = "skip_last_files",
						description = "number of files to remove from the bottom of the result-set The numbe",
						key = "skip_last_files",
						type = "SOSOptionInteger",
						mandatory = false)
	public SOSOptionInteger	skip_last_files		= new SOSOptionInteger(this, conClassName + ".skip_last_files", // HashMap-Key
														"number of files to remove from the bottom of the result-set The numbe", // Titel
														"0", // InitValue
														"0", // DefaultValue
														false // isMandatory
												);

	/**
	 * \brief getskip_last_files : number of files to remove from the bottom of the result-set The numbe
	 *
	 * \details
	 *
	 *
	 * \return number of files to remove from the bottom of the result-set The numbe
	 *
	 */
	public SOSOptionInteger getskip_last_files() {
		return skip_last_files;
	}

	/**
	 * \brief setskip_last_files : number of files to remove from the bottom of the result-set The numbe
	 *
	 * \details
	 *
	 *
	 * @param skip_last_files : number of files to remove from the bottom of the result-set The numbe
	 */
	public void setskip_last_files(final SOSOptionInteger p_skip_last_files) {
		skip_last_files = p_skip_last_files;
	}
	public SOSOptionInteger	NoOfLastFiles2Skip	= (SOSOptionInteger) skip_last_files.SetAlias(conClassName + ".NoOfLastFiles2Skip");
	/**
	 * \option Max_Files
	 * \type SOSOptionInteger
	 * \brief Max_Files - Maximum number of files to process
	 *
	 * \details
	 * Maximum number of files to process
	 *
	 * \mandatory: false
	 *
	 * \created 30.07.2012 12:35:02 by KB
	 */
	@JSOptionDefinition(
						name = "Max_Files",
						description = "Maximum number of files to process",
						key = "Max_Files",
						type = "SOSOptionInteger",
						mandatory = false)
	public SOSOptionInteger	MaxFiles			= new SOSOptionInteger(
												// ...
														this, // ....
														conClassName + ".Max_Files", // ...
														"Maximum number of files to process", // ...
														"-1", // ...
														"-1", // ...
														false);

	public String getMax_Files() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getMax_Files";
		return MaxFiles.Value();
	} // public String getMax_Files

	public SOSFtpOptionsSuperClass setMax_Files(final String pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setMax_Files";
		MaxFiles.Value(pstrValue);
		return this;
	} // public SOSFtpOptionsSuperClass setMax_Files
	/**
	 * \option check_steady_count
	 * \type SOSOptionInteger
	 * \brief check_steady_count - Number of tries for Steady check
	 *
	 * \details
	 * Number of tries for Steady check
	 *
	 * \mandatory: false
	 *
	 * \created 22.02.2013 14:46:10 by KB
	 */
	@JSOptionDefinition(
						name = "check_steady_count",
						description = "Number of tries for Steady check",
						key = "check_steady_count",
						type = "SOSOptionInteger",
						mandatory = false)
	public SOSOptionInteger	CheckSteadyCount	= new SOSOptionInteger(
												// ...
														this, // ....
														conClassName + ".check_steady_count", // ...
														"Number of tries for Steady check", // ...
														"10", // ...
														"10", // ...
														false);

	public String getcheck_steady_count() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getcheck_steady_count";
		return CheckSteadyCount.Value();
	} // public String getcheck_steady_count

	public SOSFtpOptionsSuperClass setcheck_steady_count(final String pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setcheck_steady_count";
		CheckSteadyCount.Value(pstrValue);
		return this;
	} // public SOSFtpOptionsSuperClass setcheck_steady_count
	/**
	 * \option check_steady_state_interval
	 * \type SOSOptionFileTime
	 * \brief check_steady_state_interval - The intervall for steady state checking
	 *
	 * \details
	 * The intervall for steady state checking
	 *
	 * \mandatory: false
	 *
	 * \created 26.07.2012 15:13:16 by KB
	 */
	@JSOptionDefinition(
						name = "check_steady_state_interval",
						description = "The intervall for steady state checking",
						key = "check_steady_state_interval",
						type = "SOSOptionFileTime",
						mandatory = false)
	public SOSOptionTime	check_steady_state_interval	= new SOSOptionTime(
														// ...
																this, // ....
																conClassName + ".check_steady_state_interval", // ...
																"The intervall for steady state checking", // ...
																"1", // ...
																"1", // ...
																false);
	public SOSOptionTime	CheckSteadyStateInterval	= (SOSOptionTime) check_steady_state_interval.SetAlias("check_steady_state_interval");

	public String getcheck_steady_state_interval() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getcheck_steady_state_interval";
		return check_steady_state_interval.Value();
	} // public String getcheck_steady_state_interval

	public SOSFtpOptionsSuperClass setcheck_steady_state_interval(final String pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setcheck_steady_state_interval";
		check_steady_state_interval.Value(pstrValue);
		return this;
	} // public SOSFtpOptionsSuperClass setcheck_steady_state_interval
	/**
	 * \option CheckSteadyStateOfFiles
	 * \type SOSOptionBoolean
	 * \brief CheckSteadyStateOfFiles - Check wether a file is beeing modified
	 *
	 * \details
	 * Check wether a file is beeing modified
	 *
	 * \mandatory: false
	 *
	 * \created 26.07.2012 15:06:04 by KB
	 */
	@JSOptionDefinition(
						name = "Check_Steady_State_Of_Files",
						description = "Check wether a file is beeing modified",
						key = "Check_Steady_State_Of_Files",
						type = "SOSOptionBoolean",
						mandatory = false)
	public SOSOptionBoolean	CheckSteadyStateOfFiles	= new SOSOptionBoolean(
													// ...
															this, // ....
															conClassName + ".Check_Steady_State_Of_Files", // ...
															"Check wether a file is beeing modified", // ...
															"false", // ...
															"false", // ...
															false);

	public String getCheckSteadyStateOfFiles() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getCheckSteadyStateOfFiles";
		return CheckSteadyStateOfFiles.Value();
	} // public String getCheckSteadyStateOfFiles

	public SOSFtpOptionsSuperClass setCheckSteadyStateOfFiles(final String pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setCheckSteadyStateOfFiles";
		CheckSteadyStateOfFiles.Value(pstrValue);
		return this;
	} // public SOSFtpOptionsSuperClass setCheckSteadyStateOfFiles
	/**
	 * \option PollErrorState
	 * \type SOSOptionBoolean
	 * \brief PollErrorState - Next state in Chain if no files found
	 *
	 * \details
	 * Next state in Chain if no files found
	 *
	 * \mandatory: false
	 *
	 * \created 24.07.2012 18:04:38 by KB
	 */
	@JSOptionDefinition(
						name = "PollErrorState",
						description = "Next state in Chain if no files found",
						key = "Poll_Error_State",
						type = "SOSOptionBoolean",
						mandatory = false)
	public SOSOptionJobChainNode	PollErrorState	= new SOSOptionJobChainNode(
													// ...
															this, // ....
															conClassName + ".Poll_Error_State", // ...
															"Next state in Chain if no files found", // ...
															"", // ...
															"", // ...
															false);
	public SOSOptionJobChainNode	NoFilesState	= (SOSOptionJobChainNode) PollErrorState.SetAlias("No_files_state");

	public String getPollErrorState() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getPollErrorState";
		return PollErrorState.Value();
	} // public String getPollErrorState

	public SOSFtpOptionsSuperClass setPollErrorState(final String pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setPollErrorState";
		PollErrorState.Value(pstrValue);
		return this;
	} // public SOSFtpOptionsSuperClass setPollErrorState
	/**
	 * \option Steady_state_error_state
	 * \type SOSOptionString
	 * \brief Steady_state_error_state - Next state in JobChain if check steady state did not comes to an normal end
	 *
	 * \details
	 * Next state in JobChain if check steady state did not comes to an normal end
	 *
	 * \mandatory: false
	 *
	 * \created 25.02.2014 20:18:18 by KB
	 */
	@JSOptionDefinition(
						name = "Steady_state_error_state",
						description = "Next state in JobChain if check steady state did not comes to an normal end",
						key = "Steady_state_error_state",
						type = "SOSOptionJobChainNode",
						mandatory = false)
	public SOSOptionJobChainNode	Steady_state_error_state	= new SOSOptionJobChainNode( // ...
																		this, // ....
																		conClassName + ".Steady_state_error_state", // ...
																		"Next state in JobChain if check steady state did not comes to an normal end", // ...
																		"", // ...
																		"", // ...
																		false);
	public SOSOptionJobChainNode	SteadyStateErrorState		= (SOSOptionJobChainNode) Steady_state_error_state.SetAlias("SteadyErrorState");

	public String getSteady_state_error_state() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getSteady_state_error_state";
		return Steady_state_error_state.Value();
	} // public String getSteady_state_error_state

	public SOSFtpOptionsSuperClass setSteady_state_error_state(final String pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setSteady_state_error_state";
		Steady_state_error_state.Value(pstrValue);
		return this;
	} // public SOSFtpOptionsSuperClass setSteady_state_error_state
	/**
	 * \option make_Dirs
	 * \type SOSOptionBoolean
	 * \brief make_Dirs - Create missing Directory on Target
	 *
	 * \details
	 * Create missing Directory on Target
	 *
	 * \mandatory: false
	 *
	 * \created 20.07.2012 18:19:14 by KB
	 */
	@JSOptionDefinition(
						name = "make_Dirs",
						description = "Create missing Directory on Target",
						key = "make_Dirs",
						type = "SOSOptionBoolean",
						mandatory = false)
	public SOSOptionBoolean	makeDirs				= new SOSOptionBoolean(
													// ...
															this, // ....
															conClassName + ".make_Dirs", // ...
															"Create missing Directory on Target", // ...
															"true", // ...
															"true", // ...
															false);
	public SOSOptionBoolean	createFoldersOnTarget	= (SOSOptionBoolean) makeDirs.SetAlias("create_folders_on_target");

	public String getmake_Dirs() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getmake_Dirs";
		return makeDirs.Value();
	} // public String getmake_Dirs

	public SOSFtpOptionsSuperClass setmake_Dirs(final String pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setmake_Dirs";
		makeDirs.Value(pstrValue);
		return this;
	} // public SOSFtpOptionsSuperClass setmake_Dirs
	/**
	 * \option FileListName
	 * \type SOSOptionInFileName
	 * \brief FileListName - File with a list of file names
	 *
	 * \details
	 * File with a list of file names
	 *
	 * \mandatory: false
	 *
	 * \created 13.07.2012 10:55:08 by KB
	 */
	@JSOptionDefinition(
						name = "File_List_Name",
						description = "File with a list of file names",
						key = "File_List_Name",
						type = "SOSOptionInFileName",
						mandatory = false)
	public SOSOptionInFileName	FileListName	= new SOSOptionInFileName(
												// ...
														this, // ....
														conClassName + ".File_List_Name", // ...
														"File with a list of file names", // ...
														"", // ...
														"", // ...
														false);

	public String getFileListName() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getFileListName";
		return FileListName.Value();
	} // public String getFileListName

	public SOSFtpOptionsSuperClass setFileListName(final String pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setFileListName";
		FileListName.Value(pstrValue);
		return this;
	} // public SOSFtpOptionsSuperClass setFileListName
	/**
	 * \option CreateResultSet
	 * \type SOSOptionBoolean
	 * \brief CreateResultSet - Write the ResultSet on a File if True
	 *
	 * \details
	 * Write the ResultSet to a file
	 *
	 * \mandatory: false
	 *
	 * \created 13.07.2012 10:52:29 by KB
	 */
	@JSOptionDefinition(
						name = "Create_Result_Set",
						description = "Write the ResultSet to a file",
						key = "Create_Result_Set",
						type = "SOSOptionBoolean",
						mandatory = false)
	public SOSOptionBoolean	CreateResultSet		= new SOSOptionBoolean(
												// ...
														this, // ....
														conClassName + ".Create_Result_Set", // ...
														"Write the ResultSet to a file", // ...
														"false", // ...
														"false", // ...
														false);
	public SOSOptionBoolean	CreateResultList	= (SOSOptionBoolean) CreateResultSet.SetAlias("create_result_list");

	public String getCreateResultSet() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getCreateResultSet";
		return CreateResultSet.Value();
	} // public String getCreateResultSet

	public SOSFtpOptionsSuperClass setCreateResultSet(final String pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setCreateResultSet";
		CreateResultSet.Value(pstrValue);
		return this;
	} // public SOSFtpOptionsSuperClass setCreateResultSet
	/**
	 * \option ResultSetFileName
	 * \type SOSOptionFileName
	 * \brief ResultSetFileName - Name of a File with a filelist or a resultlist
	 *
	 * \details
	 * The value of this parameter ...
	 *
	 * \mandatory: false
	 *
	 * \created 13.07.2012 10:43:54 by KB
	 */
	@JSOptionDefinition(
						name = "ResultSetFileName",
						description = "Name of a File with a filelist or a resultlist",
						key = "Result_Set_FileName",
						type = "SOSOptionFileName",
						mandatory = false)
	public SOSOptionOutFileName	ResultSetFileName	= new SOSOptionOutFileName(
													// ...
															this, // ....
															conClassName + ".Result_Set_File_Name", // ...
															"Name of a File with a filelist or a resultlist", // ...
															"", // ...
															"", // ...
															false);

	public String getResultSetFileName() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getResultSetFileName";
		return ResultSetFileName.Value();
	} // public String getResultSetFileName

	public SOSFtpOptionsSuperClass setResultSetFileName(final String pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setResultSetFileName";
		ResultSetFileName.Value(pstrValue);
		return this;
	} // public SOSFtpOptionsSuperClass setResultSetFileName
	@JSOptionDefinition(
						name = "source_dir",
						description = "Optional account info for authentication with an",
						key = "account",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionFolderName	SourceDir	= new SOSOptionFolderName(this, conClassName + ".source_dir", // HashMap-Key
													"local_dir Local directory into which or from which", // Titel
													"", // InitValue
													"", // DefaultValue
													false // isMandatory
											);
	@JSOptionDefinition(
						name = "target_dir",
						description = "Optional account info for authentication with an",
						key = "account",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionFolderName	TargetDir	= new SOSOptionFolderName(this, conClassName + ".target_dir", // HashMap-Key
													"target_dir directory into which or from which", // Titel
													"", // InitValue
													"", // DefaultValue
													false // isMandatory
											);
	/**
	* \var account : Optional account info for authentication with an
	*
	Optional account info for authentication with an (FTP) server.
	*
	*/
	@JSOptionDefinition(
						name = "account",
						description = "Optional account info for authentication with an",
						key = "account",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionString		account		= new SOSOptionString(this, conClassName + ".account", // HashMap-Key
													"Optional account info for authentication with an", // Titel
													" ", // InitValue
													" ", // DefaultValue
													false // isMandatory
											);

	/**
	 * \brief getaccount
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionString getaccount() {
		return account;
	}

	/**
	 * \brief setaccount
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_account
	 */
	@Override public void setaccount(final SOSOptionString p_account) {
		account = p_account;
	}
	/**
	* \var alternative_account : Alternative parameter for the primary parameter
	*
	Alternative parameter for the primary parameter account. The alternative parameters are used solely should the connection to an FTP/SFTP server fail, e.g. if the server were not available or if invalid credentials were used.
	*
	*/
	@JSOptionDefinition(
						name = "alternative_account",
						description = "Alternative parameter for the primary parameter",
						key = "alternative_account",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionString	alternative_account	= new SOSOptionString(this, conClassName + ".alternative_account", // HashMap-Key
														"Alternative parameter for the primary parameter", // Titel
														" ", // InitValue
														" ", // DefaultValue
														false // isMandatory
												);

	/**
	 * \brief getalternative_account
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionString getalternative_account() {
		return alternative_account;
	}

	/**
	 * \brief setalternative_account
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_alternative_account
	 */
	@Override public void setalternative_account(final SOSOptionString p_alternative_account) {
		alternative_account = p_alternative_account;
	}
	/**
	* \var alternative_host : Alternative parameter for the primary parameter
	*
	Alternative parameter for the primary parameter host. The alternative parameters are used solely should the connection to an FTP/SFTP server fail, e.g. if the server were not available or if invalid credentials were used.
	*
	*/
	@JSOptionDefinition(
						name = "alternative_host",
						description = "Alternative parameter for the primary parameter",
						key = "alternative_host",
						type = "SOSOptionHostName",
						mandatory = false)
	public SOSOptionHostName	alternative_host	= new SOSOptionHostName(this, conClassName + ".alternative_host", // HashMap-Key
															"Alternative parameter for the primary parameter", // Titel
															" ", // InitValue
															" ", // DefaultValue
															false // isMandatory
													);

	/**
	 * \brief getalternative_host
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionHostName getalternative_host() {
		return alternative_host;
	}

	/**
	 * \brief setalternative_host
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_alternative_host
	 */
	@Override public void setalternative_host(final SOSOptionHostName p_alternative_host) {
		alternative_host = p_alternative_host;
	}
	/**
	* \var alternative_passive_mode : Alternative parameter for the primary parameter
	*
	Alternative parameter for the primary parameter passive_mode. The alternative parameters are used solely should the connection to an FTP/SFTP server fail, e.g. if the server were not available or if invalid credentials were used.
	*
	*/
	@JSOptionDefinition(
						name = "alternative_passive_mode",
						description = "Alternative parameter for the primary parameter",
						key = "alternative_passive_mode",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionString	alternative_passive_mode	= new SOSOptionString(this, conClassName + ".alternative_passive_mode", // HashMap-Key
																"Alternative parameter for the primary parameter", // Titel
																" ", // InitValue
																" ", // DefaultValue
																false // isMandatory
														);

	/**
	 * \brief getalternative_passive_mode
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionString getalternative_passive_mode() {
		return alternative_passive_mode;
	}

	/**
	 * \brief setalternative_passive_mode
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_alternative_passive_mode
	 */
	@Override public void setalternative_passive_mode(final SOSOptionString p_alternative_passive_mode) {
		alternative_passive_mode = p_alternative_passive_mode;
	}
	/**
	* \var alternative_password : Alternative parameter for the primary parameter
	*
	Alternative parameter for the primary parameter password. The alternative parameters are used solely should the connection to an FTP/SFTP server fail, e.g. if the server were not available or if invalid credentials were used.
	*
	*/
	@JSOptionDefinition(
						name = "alternative_password",
						description = "Alternative parameter for the primary parameter",
						key = "alternative_password",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionPassword	alternative_password	= new SOSOptionPassword(this, conClassName + ".alternative_password", // HashMap-Key
																"Alternative parameter for the primary parameter", // Titel
																" ", // InitValue
																" ", // DefaultValue
																false // isMandatory
														);

	/**
	 * \brief getalternative_password
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionPassword getalternative_password() {
		return alternative_password;
	}

	/**
	 * \brief setalternative_password
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_alternative_password
	 */
	@Override public void setalternative_password(final SOSOptionPassword p_alternative_password) {
		alternative_password = p_alternative_password;
	}
	/**
	* \var alternative_port : Alternative parameter for the primary parameter
	*
	Alternative parameter for the primary parameter port. The alternative parameters are used solely should the connection to an FTP/SFTP server fail, e.g. if the server were not available or if invalid credentials were used.
	*
	*/
	@JSOptionDefinition(
						name = "alternative_port",
						description = "Alternative parameter for the primary parameter",
						key = "alternative_port",
						type = "SOSOptionPortNumber",
						mandatory = false)
	public SOSOptionPortNumber	alternative_port	= new SOSOptionPortNumber(this, conClassName + ".alternative_port", // HashMap-Key
															"Alternative parameter for the primary parameter", // Titel
															"21", // InitValue
															"21", // DefaultValue
															false // isMandatory
													);

	/**
	 * \brief getalternative_port
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionPortNumber getalternative_port() {
		return alternative_port;
	}

	/**
	 * \brief setalternative_port
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_alternative_port
	 */
	@Override public void setalternative_port(final SOSOptionPortNumber p_alternative_port) {
		alternative_port = p_alternative_port;
	}
	public SOSOptionPortNumber	AlternativePortNumber	= (SOSOptionPortNumber) alternative_port.SetAlias(conClassName + ".AlternativePortNumber");
	/**
	* \var alternative_remote_dir : Alternative parameter for the primary parameter
	*
	Alternative parameter for the primary parameter remote_dir. The alternative parameters are used solely should the connection to an FTP/SFTP server fail, e.g. if the server were not available or if invalid credentials were used.
	*
	*/
	@JSOptionDefinition(
						name = "alternative_remote_dir",
						description = "Alternative parameter for the primary parameter",
						key = "alternative_remote_dir",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionString		alternative_remote_dir	= new SOSOptionString(this, conClassName + ".alternative_remote_dir", // HashMap-Key
																"Alternative parameter for the primary parameter", // Titel
																" ", // InitValue
																" ", // DefaultValue
																false // isMandatory
														);

	/**
	 * \brief getalternative_remote_dir
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionString getalternative_remote_dir() {
		return alternative_remote_dir;
	}

	/**
	 * \brief setalternative_remote_dir
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_alternative_remote_dir
	 */
	@Override public void setalternative_remote_dir(final SOSOptionString p_alternative_remote_dir) {
		alternative_remote_dir = p_alternative_remote_dir;
	}
	/**
	* \var alternative_transfer_mode : Alternative parameter for the primary parameter
	*
	Alternative parameter for the primary parameter transfer_mode. The alternative parameters are used solely should the connection to an FTP/SFTP server fail, e.g. if the server were not available or if invalid credentials were used.
	*
	*/
	@JSOptionDefinition(
						name = "alternative_transfer_mode",
						description = "Alternative parameter for the primary parameter",
						key = "alternative_transfer_mode",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionString	alternative_transfer_mode	= new SOSOptionString(this, conClassName + ".alternative_transfer_mode", // HashMap-Key
																"Alternative parameter for the primary parameter", // Titel
																" ", // InitValue
																" ", // DefaultValue
																false // isMandatory
														);

	/**
	 * \brief getalternative_transfer_mode
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionString getalternative_transfer_mode() {
		return alternative_transfer_mode;
	}

	/**
	 * \brief setalternative_transfer_mode
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_alternative_transfer_mode
	 */
	@Override public void setalternative_transfer_mode(final SOSOptionString p_alternative_transfer_mode) {
		alternative_transfer_mode = p_alternative_transfer_mode;
	}
	/**
	* \var alternative_user : Alternative parameter for the primary parameter
	*
	Alternative parameter for the primary parameter user. The alternative parameters are used solely should the connection to an FTP/SFTP server fail, e.g. if the server were not available or if invalid credentials were used.
	*
	*/
	@JSOptionDefinition(
						name = "alternative_user",
						description = "Alternative parameter for the primary parameter",
						key = "alternative_user",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionUserName	alternative_user	= new SOSOptionUserName(this, conClassName + ".alternative_user", // HashMap-Key
															"Alternative parameter for the primary parameter", // Titel
															"", // InitValue
															"", // DefaultValue
															false // isMandatory
													);

	/**
	 * \brief getalternative_user
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionUserName getalternative_user() {
		return alternative_user;
	}

	/**
	 * \brief setalternative_user
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_alternative_user
	 */
	@Override public void setalternative_user(final SOSOptionUserName p_alternative_user) {
		alternative_user = p_alternative_user;
	}
	/**
	* \var append_files : This parameter specifies whether the content of a
	*
	This parameter specifies whether the content of a source file should be appended to the target file should this file exist. The parameter overwrite_files will be ignored if this parameter is specified with the value true.
	*
	*/
	@JSOptionDefinition(
						name = "append_files",
						description = "This parameter specifies whether the content of a",
						key = "append_files",
						type = "SOSOptionBoolean",
						mandatory = false)
	public SOSOptionBoolean	append_files	= new SOSOptionBoolean(this, conClassName + ".append_files", // HashMap-Key
													"This parameter specifies whether the content of a", // Titel
													"false", // InitValue
													"false", // DefaultValue
													false // isMandatory
											);

	/**
	 * \brief getappend_files
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionBoolean getappend_files() {
		return append_files;
	}

	/**
	 * \brief setappend_files
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_append_files
	 */
	@Override public void setappend_files(final SOSOptionBoolean p_append_files) {
		append_files = p_append_files;
	}
	/**
	* \var atomic_prefix : This parameter specifies whether target files shou
	*
	This parameter specifies whether target files should be created with a prefix such as "~", and must be renamed to the target file name after the file transfer is completed without errors. This mechanism is useful if the target directory is monitored for incoming files by some application and if files are required to appear atomically instead of being subsequently written to. The temporary prefix is specified as the value of this parameter. This setting is recommended should target directories be monitored by an application or a JobScheduler.
	*
	*/
	@JSOptionDefinition(
						name = "atomic_prefix",
						description = "This parameter specifies whether target files shou",
						key = "atomic_prefix",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionString	atomic_prefix	= new SOSOptionString(this, conClassName + ".atomic_prefix", // HashMap-Key
													"This parameter specifies whether target files shou", // Titel
													"", // InitValue
													"", // DefaultValue
													false // isMandatory
											);

	/**
	 * \brief getatomic_prefix
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionString getatomic_prefix() {
		return atomic_prefix;
	}

	/**
	 * \brief setatomic_prefix
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_atomic_prefix
	 */
	@Override public void setatomic_prefix(final SOSOptionString p_atomic_prefix) {
		atomic_prefix = p_atomic_prefix;
	}
	/**
	* \var atomic_suffix : This parameter specifies whether target files shou
	*
	This parameter specifies whether target files should be created with a suffix such as "~", and should be renamed to the target file name after the file transfer is completed. This mechanism is useful if the target directory is monitored for incoming files by some application and if files are required to appear atomically instead of being subsequently written to. The temporary suffix is specified as the value of this parameter. This setting is recommended should target directories be monitored by an application or a Job Scheduler.
	*
	*/
	@JSOptionDefinition(
						name = "atomic_suffix",
						description = "This parameter specifies whether target files shou",
						key = "atomic_suffix",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionString	atomic_suffix	= new SOSOptionString(this, conClassName + ".atomic_suffix", // HashMap-Key
													"This parameter specifies whether target files shou", // Titel
													"", // InitValue
													"", // DefaultValue
													false // isMandatory
											);

	/**
	 * \brief getatomic_suffix
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionString getatomic_suffix() {
		return atomic_suffix;
	}

	/**
	 * \brief setatomic_suffix
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_atomic_suffix
	 */
	@Override public void setatomic_suffix(final SOSOptionString p_atomic_suffix) {
		atomic_suffix = p_atomic_suffix;
	}
	/**
	* \var banner_footer : Name der Template-Datei für Protokoll-Ende This p
	*
	This program logs output to stdout or to a file that has been specified by the parameter log_filename. A template can be used in order to organize the output that is created. The output is grouped into header, file list and footer. This parameter specifies a template file for footer output. Templates can use internal variables and parameters as placeholders in the form %{placeholder}. The standard footer template looks like this: ************************************************************************* execution status = %{status} successful transfers = %{successful_transfers} failed transfers = %{failed_transfers} last error = %{last_error} *************************************************************************
	*
	*/
	@JSOptionDefinition(
						name = "banner_footer",
						description = "Name der Template-Datei für Protokoll-Ende This p",
						key = "banner_footer",
						type = "SOSOptionInFileName",
						mandatory = false)
	public SOSOptionInFileName	banner_footer	= new SOSOptionInFileName(this, conClassName + ".banner_footer", // HashMap-Key
														"Name der Template-Datei für Protokoll-Ende This p", // Titel
														"", // InitValue
														"", // DefaultValue
														false // isMandatory
												);

	/**
	 * \brief getbanner_footer
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionInFileName getbanner_footer() {
		return banner_footer;
	}

	/**
	 * \brief setbanner_footer
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_banner_footer
	 */
	@Override public void setbanner_footer(final SOSOptionInFileName p_banner_footer) {
		banner_footer = p_banner_footer;
	}
	/**
	* \var banner_header : Name of Template-File for log-File
	*
	This program logs output to stdout or to a file that has been specified by the parameter log_filename. A template can be used in order to organize the output that is created. The output is grouped into header, file list and footer. This parameter specifies a template file for header output. Templates can use internal variables and parameters as placeholders in the form %{placeholder}. The standard header template looks like this: ************************************************************************* * * * SOSFTP - Managed File Transfer Utility * * -------------------------------------- * * * ************************************************************************* version = %{version} date = %{date} %{time} operation = %{operation} protocol = %{protocol} file specification = %{file_spec} file path = %{file_path} source host = %{localhost} (%{local_host_ip}) local directory = %{local_dir} jump host = %{jump_host} target host = %{host} (%{host_ip}) target directory = %{remote_dir} pid = %{current_pid} ppid = %{ppid} *************************************************************************
	*
	*/
	@JSOptionDefinition(
						name = "banner_header",
						description = "Name of Template-File for log-File",
						key = "banner_header",
						type = "SOSOptionInFileName",
						mandatory = false)
	public SOSOptionInFileName	banner_header	= new SOSOptionInFileName(this, conClassName + ".banner_header", // HashMap-Key
														"Name of Template-File for log-File", // Titel
														"", // InitValue
														"", // DefaultValue
														false // isMandatory
												);

	/**
	 * \brief getbanner_header
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionInFileName getbanner_header() {
		return banner_header;
	}

	/**
	 * \brief setbanner_header
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_banner_header
	 */
	@Override public void setbanner_header(final SOSOptionInFileName p_banner_header) {
		banner_header = p_banner_header;
	}
	/**
	* \var check_interval : This parameter specifies the interval in seconds
	*
	This parameter specifies the interval in seconds between two file transfer trials, if repeated transfer of files has been configured using the check_retry parameter.
	*
	*/
	@JSOptionDefinition(
						name = "check_interval",
						description = "This parameter specifies the interval in seconds",
						key = "check_interval",
						type = "SOSOptionInteger",
						mandatory = false)
	public SOSOptionInteger	check_interval	= new SOSOptionInteger(this, conClassName + ".check_interval", // HashMap-Key
													"This parameter specifies the interval in seconds", // Titel
													"60", // InitValue
													"60", // DefaultValue
													false // isMandatory
											);

	/**
	 * \brief getcheck_interval
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionInteger getcheck_interval() {
		return check_interval;
	}

	/**
	 * \brief setcheck_interval
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_check_interval
	 */
	@Override public void setcheck_interval(final SOSOptionInteger p_check_interval) {
		check_interval = p_check_interval;
	}
	/**
	* \var check_retry : This parameter specifies whether a file transfer
	*
	This parameter specifies whether a file transfer should be repeated in order to ensure that the file was complete when the transfer started. This is relevant for Unix systems that allow read and write access to a file at the same time. This parameter causes the size of the current file transfer and of the previous file transfer to be compared and repeats transferring one file up to the number of trials specified by this parameter. Should the file size of both transfers be the same, then it is assumed that the file was complete at the FTP/SFTP server. The interval between two trials to transfer a file is configured using the check_interval parameter.
	*
	*/
	@JSOptionDefinition(
						name = "check_retry",
						description = "This parameter specifies whether a file transfer",
						key = "check_retry",
						type = "SOSOptionInteger",
						mandatory = false)
	public SOSOptionInteger	check_retry	= new SOSOptionInteger(this, conClassName + ".check_retry", // HashMap-Key
												"This parameter specifies whether a file transfer", // Titel
												"0", // InitValue
												"0", // DefaultValue
												false // isMandatory
										);

	/**
	 * \brief getcheck_retry
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionInteger getcheck_retry() {
		return check_retry;
	}

	/**
	 * \brief setcheck_retry
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_check_retry
	 */
	@Override public void setcheck_retry(final SOSOptionInteger p_check_retry) {
		check_retry = p_check_retry;
	}
	/**
	* \var check_size : This parameter determines whether the original f
	*
	This parameter determines whether the original file size and the number of bytes transferred should be compared after a file transfer and whether an error should be raised if they would not match.
	*
	*/
	@JSOptionDefinition(
						name = "check_size",
						description = "This parameter determines whether the original f",
						key = "check_size",
						type = "SOSOptionBoolean",
						mandatory = false)
	public SOSOptionBoolean	check_size	= new SOSOptionBoolean(this, conClassName + ".check_size", // HashMap-Key
												"This parameter determines whether the original f", // Titel
												"true", // InitValue
												"true", // DefaultValue
												false // isMandatory
										);

	/**
	 * \brief getcheck_size
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionBoolean getcheck_size() {
		return check_size;
	}

	/**
	 * \brief setcheck_size
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_check_size
	 */
	@Override public void setcheck_size(final SOSOptionBoolean p_check_size) {
		check_size = p_check_size;
	}
	public SOSOptionBoolean		CheckFileSizeAfterTransfer	= (SOSOptionBoolean) check_size.SetAlias(conClassName + ".CheckFileSizeAfterTransfer");
	/**
	* \var classpath_base : The parameter is used during installation of this
	*
	The parameter is used during installation of this program on a remote server with the parameter operation=installcode>. This parameter specifies the path of the Java Runtime Environment (JRE) at the remote server and is used if on the remote server a JRE is not included in the system path. The path of the specified JRE is added to the start script at the remote server (sosftp.cmd respectively sosftp.sh).
	*
	*/
	@JSOptionDefinition(
						name = "classpath_base",
						description = "The parameter is used during installation of this",
						key = "classpath_base",
						type = "SOSOptionFolderName",
						mandatory = false)
	public SOSOptionFolderName	classpath_base				= new SOSOptionFolderName(this, conClassName + ".classpath_base", // HashMap-Key
																	"The parameter is used during installation of this", // Titel
																	"", // InitValue
																	"", // DefaultValue
																	false // isMandatory
															);

	/**
	 * \brief getclasspath_base
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionFolderName getclasspath_base() {
		return classpath_base;
	}

	/**
	 * \brief setclasspath_base
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_classpath_base
	 */
	@Override public void setclasspath_base(final SOSOptionFolderName p_classpath_base) {
		classpath_base = p_classpath_base;
	}
	/**
	* \var compress_files : This parameter specifies whether the content of the source files
	*
	This parameter specifies whether the content of the source files should be compressed by using a zip-algorithm or not. In case of sending files the files to be sent will be compressed.
	*
	*/
	@JSOptionDefinition(
						name = "compress_files",
						description = "This parameter specifies whether the content of the source files",
						key = "compress_files",
						type = "SOSOptionBoolean",
						mandatory = false)
	public SOSOptionBoolean	compress_files	= new SOSOptionBoolean(this, conClassName + ".compress_files", // HashMap-Key
													"This parameter specifies whether the content of the source files", // Titel
													"false", // InitValue
													"false", // DefaultValue
													false // isMandatory
											);

	/**
	 * \brief getcompress_files
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionBoolean getcompress_files() {
		return compress_files;
	}

	/**
	 * \brief setcompress_files
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_compress_files
	 */
	@Override public void setcompress_files(final SOSOptionBoolean p_compress_files) {
		compress_files = p_compress_files;
	}
	/**
	* \var compressed_file_extension : Additional file-name extension for compressed files This parameter spe
	*
	This parameter specifies the file extension should target file compression be specified using the compress_files parameter.
	*
	*/
	@JSOptionDefinition(
						name = "compressed_file_extension",
						description = "Additional file-name extension for compressed files This parameter spe",
						key = "compressed_file_extension",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionString	compressed_file_extension	= new SOSOptionString(this, conClassName + ".compressed_file_extension", // HashMap-Key
																"Additional file-name extension for compressed files This parameter spe", // Titel
																".gz", // InitValue
																".gz", // DefaultValue
																false // isMandatory
														);

	/**
	 * \brief getcompressed_file_extension
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionString getcompressed_file_extension() {
		return compressed_file_extension;
	}

	/**
	 * \brief setcompressed_file_extension
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_compressed_file_extension
	 */
	@Override public void setcompressed_file_extension(final SOSOptionString p_compressed_file_extension) {
		compressed_file_extension = p_compressed_file_extension;
	}
	/**
	* \var current_pid : This parameter is used for Unix systems and - as o
	*
	This parameter is used for Unix systems and - as opposed to other parameters - is usually specified in the start script sosftp.sh. The value of the environment variable $$ is assigned, that contains the current process id (PID). The process id is used when writing an entry to a history file for each transfer (see parameter history).
	*
	*/
	@JSOptionDefinition(
						name = "current_pid",
						description = "This parameter is used for Unix systems and - as o",
						key = "current_pid",
						type = "SOSOptionProcessID",
						mandatory = false)
	public SOSOptionProcessID	current_pid	= new SOSOptionProcessID(this, conClassName + ".current_pid", // HashMap-Key
													"This parameter is used for Unix systems and - as o", // Titel
													" ", // InitValue
													" ", // DefaultValue
													false // isMandatory
											);

	/**
	 * \brief getcurrent_pid
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionProcessID getcurrent_pid() {
		return current_pid;
	}

	/**
	 * \brief setcurrent_pid
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_current_pid
	 */
	@Override public void setcurrent_pid(final SOSOptionProcessID p_current_pid) {
		current_pid = p_current_pid;
	}
	/**
	* \var file_path : This parameter is used alternatively to the parame
	*
	This parameter is used alternatively to the parameter file_spec to specify a single file for transfer. When receiving files the following applies: This parameter accepts the absolute name and path of file at the FTP/SFTP server that should be transferred. The file name has to include both name and path of the file at the FTP/SFTP server. The file will be stored unter its name in the directory that is specified by the parameter local_dir. The following parameters are ignored should this parameter be used: file_spec and remote_dir. When sending files the following applies: This parameter accepts the absolute name and path of file that should be transferred. An absolute path has to be specified. The file will be stored under its name in the directory at the FTP/SFTP server that has been specified by the parameter remote_dir. The following parameters are ignored should this parameter be used: file_spec and local_dir.
	*
	*/
	@JSOptionDefinition(
						name = "file_path",
						description = "This parameter is used alternatively to the parame",
						key = "file_path",
						type = "SOSOptionFileName",
						mandatory = false)
	public SOSOptionFileName	file_path	= new SOSOptionFileName(this, conClassName + ".file_path", // HashMap-Key
													"This parameter is used alternatively to the parame", // Titel
													"", // InitValue
													"", // DefaultValue
													false // isMandatory
											);

	/**
	 * \brief getfile_path
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionFileName getfile_path() {
		return file_path;
	}

	/**
	 * \brief setfile_path
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_file_path
	 */
	@Override public void setfile_path(final SOSOptionFileName p_file_path) {
		file_path = p_file_path;
	}
	/**
	* \var file_spec : file_spec This parameter expects a regular expressi
	*
	This parameter expects a regular expression to select files from a local directory or from an FTP/SFTP server (depending on the operation parameter values send or receive) to be transferred. For the operations send and receive either this parameter has to be specified or the parameter file_path or a list of file names as additional parameters.
	*
	*/
	@JSOptionDefinition(
						name = "file_spec",
						description = "file_spec This parameter expects a regular expressi",
						key = "file_spec",
						type = "SOSOptionRegExp",
						mandatory = false)
	public SOSOptionRegExp	file_spec		= new SOSOptionRegExp(this, conClassName + ".file_spec", // HashMap-Key
													"file_spec This parameter expects a regular expressi", // Titel
													"^.*$", // InitValue
													"^.*$", // DefaultValue
													false // isMandatory
											);
	public SOSOptionRegExp	FileNameRegExp	= (SOSOptionRegExp) file_spec.SetAlias(conClassName + ".FileNameRegExp");

	/**
	 * \brief getfile_spec
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionRegExp getfile_spec() {
		return file_spec;
	}

	/**
	 * \brief setfile_spec
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_file_spec
	 */
	@Override public void setfile_spec(final SOSOptionRegExp p_file_spec) {
		file_spec = p_file_spec;
	}
	public SOSOptionRegExp	FileNamePatternRegExp	= (SOSOptionRegExp) file_spec.SetAlias(conClassName + ".FileNamePatternRegExp");
	// /**
	// * \var file_spec2 : In addition to what is stated for the parameter fi
	// *
	// In addition to what is stated for the parameter file_spec additional parameters can be specified for up to 9 file sets like this:
	// -file_spec=.*\.gif$ -local_dir=/tmp/1 -remote_dir=/tmp/1 -file_spec2=.*\.exe$::param_set_2
	// -param_set_2="transfer_mode=binary::remote_dir=/tmp/2::local_dir=/tmp/2" Within the file_spec2 parameter value the regular expression
	// is separated by :: from the name of a file set. This name can freely be chosen, it consists of the characters 0-9, a-z and _. The
	// name of the file set is used as a separate parameter in the command line. This parameter is assigend the list of parameters that
	// should be valid for the specific file set. Therefore the names and values of individual parameters are specified in the form
	// name=value::name2=value2 .... Such parameters are exclusively valid for the specific file set. The above sample causes all files with
	// the extension .gif to be transferred from the local directory /tmp/1 to a directory with the same name on the target host. For files
	// with the extension .exe a file set param_set_2 is specified that contains parameters that are specific for this file set, as binary
	// transfer and different source and target directories. Please, consider that parameter file sets cannot specify parameters that
	// control the connection to a target host, i.e. all files are transferred between the same local and remote hosts. However, the
	// transfer direction can be changed, e.g. by specifying a different operation parameter for a file set.
	// *
	// */
	// @JSOptionDefinition(name = "file_spec2", description = "In addition to what is stated for the parameter fi", key = "file_spec2", type
	// = "SOSOptionRegExp", mandatory = false)
	// public SOSOptionRegExp file_spec2 = new SOSOptionRegExp(this, conClassName + ".file_spec2", // HashMap-Key
	// "In addition to what is stated for the parameter fi", // Titel
	// " ", // InitValue
	// " ", // DefaultValue
	// false // isMandatory
	// );
	//
	// /**
	// * \brief getfile_spec2
	// *
	// * \details
	// *
	// * \return
	// *
	// * @return
	// */
	// public SOSOptionRegExp getfile_spec2() {
	// return file_spec2;
	// }
	//
	// /**
	// * \brief setfile_spec2
	// *
	// * \details
	// *
	// * \return
	// *
	// * @param p_file_spec2
	// */
	// public void setfile_spec2(SOSOptionRegExp p_file_spec2) {
	// this.file_spec2 = p_file_spec2;
	// }
	//
	/**
	* \var force_files : This parameter specifies whether an error should b
	*
	This parameter specifies whether an error should be raised if no files could be found for transfer. The number of files to be transferred is determined by the file_spec or file_path parameters and can be restricted by the overwrite_files parameter should this be specified with the value false.
	*
	*/
	@JSOptionDefinition(
						name = "force_files",
						description = "This parameter specifies whether an error should b",
						key = "force_files",
						type = "SOSOptionBoolean",
						mandatory = false)
	public SOSOptionBoolean	force_files				= new SOSOptionBoolean(this, conClassName + ".force_files", // HashMap-Key
															"This parameter specifies whether an error should b", // Titel
															"true", // InitValue
															"true", // DefaultValue
															false // isMandatory
													);
	public SOSOptionBoolean	ErrorOnNoDataFound		= (SOSOptionBoolean) force_files.SetAlias("error_on_no_data_found", "error_when_no_data_found");

	/**
	 * \brief getforce_files
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionBoolean getforce_files() {
		return force_files;
	}

	/**
	 * \brief setforce_files
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_force_files
	 */
	@Override public void setforce_files(final SOSOptionBoolean p_force_files) {
		force_files = p_force_files;
	}
	/**
	* \var history : This parameter causes a history file to be written
	*
	This parameter causes a history file to be written in CSV format. The path and name of the history file is specified as value for this parameter. A history record is created for each file that has been transferred. A history file contains the following columns: guid A unique identifier for the history entry. This identifier is used for checking of duplicate entries in combination with Job Scheduler for Managed File Transfer. mandator A character that denominates the mandator of a file transfer, see respective parameter. transfer_timestamp The point in time when the transfer took place. pid The process id of the current process that executes the file transfer, see parameter current_pid. ppid The process id of the parent of the process that executes the file transfer, see respective parameter. operation One of the operations send or receive, see respective parameter. localhost The name of the host on which this program is executed. localhost_ip The IP address of the host on which this program is executed. local_user The name of the user account for which this program is executed. remote_host The name of the host to/from which a transfer is executed, see parameter host. remote_host_ip The IP address of the host to/from which a transfer is executed, see parameter host. remote_user The name of the user account for the remote host, see parameter user. protocol The protocol can be either ftp, sftp or ftps, see respective parameter. port The port on the remote host, see respective parameter. local_dir The local directory to/from which a file has been transferred, see respective parameter. remote_dir The remote directory to/from which a file has been transferred, see respective parameter. local_filename For send operations this is the original file name on the local host. For receive operations this is the resulting file name on the local host optionally having applied replacement operations, see parameter replacing. remote_filename For send operations this is the resulting file name on the remote host optionally having applied replacement operations, see parameter replacing. For receive operations this is the original file name on the remote host. file_size The size of the transferred file in bytes. md5 The value of the MD5 hash that is created from the file that was transferred. status The status can be either success or error. last_error_message Should an error have occurred then the last error message will be given in this column. log_filename The name of the log file, see respective parameter.
	*
	*/
	@JSOptionDefinition(
						name = "history",
						description = "This parameter causes a history file to be written",
						key = "history",
						type = "SOSOptionOutFileName",
						mandatory = false)
	public SOSOptionOutFileName	history			= new SOSOptionOutFileName(this, conClassName + ".history", // HashMap-Key
														"This parameter causes a history file to be written", // Titel
														"", // InitValue
														"", // DefaultValue
														false // isMandatory
												);
	public SOSOptionOutFileName	HistoryFileName	= (SOSOptionOutFileName) history.SetAlias("history_file_name");

	/**
	 * \brief gethistory
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionOutFileName gethistory() {
		return history;
	}

	/**
	 * \brief sethistory
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_history
	 */
	@Override public void sethistory(final SOSOptionOutFileName p_history) {
		history = p_history;
	}
	public SOSOptionOutFileName	SOSFtpHistoryFileName	= (SOSOptionOutFileName) history.SetAlias(conClassName + ".SOSFtpHistoryFileName");
	/**
	* \var history_repeat : The parameter is used in order to synchronize para
	*
	The parameter is used in order to synchronize parallel write access to the history file by multiple instances of this program. 
	This parameter specifies the maximum number of repeat intervals when trying to write to the history file if the history file is 
	locked due to parallel instances of this program.
	*
	*/
	@JSOptionDefinition(
						name = "history_repeat",
						description = "The parameter is used in order to synchronize para",
						key = "history_repeat",
						type = "SOSOptionInteger",
						mandatory = false)
	public SOSOptionInteger		history_repeat			= new SOSOptionInteger(this, conClassName + ".history_repeat", // HashMap-Key
																"The parameter is used in order to synchronize para", // Titel
																"3", // InitValue
																"3", // DefaultValue
																false // isMandatory
														);

	/**
	 * \brief gethistory_repeat
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionInteger gethistory_repeat() {
		return history_repeat;
	}

	/**
	 * \brief sethistory_repeat
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_history_repeat
	 */
	@Override public void sethistory_repeat(final SOSOptionInteger p_history_repeat) {
		history_repeat = p_history_repeat;
	}
	/**
	* \var history_repeat_interval : The parameter is used in order to synchronize para
	*
	The parameter is used in order to synchronize parallel write access to the history file by multiple instances of this program. This parameter specifies the the interval in seconds of repeated trials to write to the history file if the history file is locked due to parallel instances of this program.
	*
	*/
	@JSOptionDefinition(
						name = "history_repeat_interval",
						description = "The parameter is used in order to synchronize para",
						key = "history_repeat_interval",
						type = "SOSOptionInteger",
						mandatory = false)
	public SOSOptionInteger	history_repeat_interval	= new SOSOptionInteger(this, conClassName + ".history_repeat_interval", // HashMap-Key
															"The parameter is used in order to synchronize para", // Titel
															"1", // InitValue
															"1", // DefaultValue
															false // isMandatory
													);

	/**
	 * \brief gethistory_repeat_interval
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionInteger gethistory_repeat_interval() {
		return history_repeat_interval;
	}

	/**
	 * \brief sethistory_repeat_interval
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_history_repeat_interval
	 */
	@Override public void sethistory_repeat_interval(final SOSOptionInteger p_history_repeat_interval) {
		history_repeat_interval = p_history_repeat_interval;
	}
	/**
	* \var host : Host-Name This parameter specifies th
	*
	This parameter specifies the hostname or IP address of the server to which a connection has to be made.
	*
	*/
	@JSOptionDefinition(
						name = "host",
						description = "Host-Name This parameter specifies th",
						key = "host",
						type = "SOSOptionHostName",
						mandatory = false)
	public SOSOptionHostName	host	= new SOSOptionHostName(this, conClassName + ".host", // HashMap-Key
												"Host-Name This parameter specifies th", // Titel
												" ", // InitValue
												" ", // DefaultValue
												false // isMandatory
										);

	/**
	 * \brief gethost
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionHostName gethost() {
		return host;
	}

	/**
	 * \brief sethost
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_host
	 */
	@Override public void sethost(final SOSOptionHostName p_host) {
		host = p_host;
	}
	public SOSOptionHostName	HostName		= (SOSOptionHostName) host.SetAlias(conClassName + ".HostName");
	/**
	* \var http_proxy_host : The value of this parameter is the host name or th
	*
	The value of this parameter is the host name or the IP address of a proxy used in order to establish a connection to the SSH server via SSL/TLS. The use of a proxy is optional and exclusively considered if the parameter protocol=ftps is used.
	*
	*/
	@JSOptionDefinition(
						name = "http_proxy_host",
						description = "The value of this parameter is the host name or th",
						key = "http_proxy_host",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionString		http_proxy_host	= new SOSOptionString(this, conClassName + ".http_proxy_host", // HashMap-Key
														"The value of this parameter is the host name or th", // Titel
														"", // InitValue
														"", // DefaultValue
														false // isMandatory
												);

	/**
	 * \brief gethttp_proxy_host
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionString gethttp_proxy_host() {
		return http_proxy_host;
	}

	/**
	 * \brief sethttp_proxy_host
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_http_proxy_host
	 */
	@Override public void sethttp_proxy_host(final SOSOptionString p_http_proxy_host) {
		http_proxy_host = p_http_proxy_host;
	}
	/**
	* \var http_proxy_port : This parameter specifies the port of a proxy that
	*
	This parameter specifies the port of a proxy that is used in order to establish a connection to the SSH server via SSL/TLS, see parameter http_proxy_host.
	*
	*/
	@JSOptionDefinition(
						name = "http_proxy_port",
						description = "This parameter specifies the port of a proxy that",
						key = "http_proxy_port",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionString	http_proxy_port	= new SOSOptionString(this, conClassName + ".http_proxy_port", // HashMap-Key
													"This parameter specifies the port of a proxy that", // Titel
													" ", // InitValue
													" ", // DefaultValue
													false // isMandatory
											);

	/**
	 * \brief gethttp_proxy_port
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionString gethttp_proxy_port() {
		return http_proxy_port;
	}

	/**
	 * \brief sethttp_proxy_port
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_http_proxy_port
	 */
	@Override public void sethttp_proxy_port(final SOSOptionString p_http_proxy_port) {
		http_proxy_port = p_http_proxy_port;
	}
	/**
	* \var jump_command : This parameter specifies a command that is to be e
	*
	This parameter specifies a command that is to be executed on the SSH server. Multiple commands can be separated by the command delimiter that is specified using the jump_command_delimiter parameter.
	*
	*/
	@JSOptionDefinition(
						name = "jump_command",
						description = "This parameter specifies a command that is to be e",
						key = "jump_command",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionString	jump_command	= new SOSOptionString(this, conClassName + ".jump_command", // HashMap-Key
															"This parameter specifies a command that is to be e", // Titel
															" ", // InitValue
															" ", // DefaultValue
															false // isMandatory
													);

	/**
	 * \brief getjump_command
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionString getjump_command() {
		return jump_command;
	}

	/**
	 * \brief setjump_command
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_jump_command
	 */
	@Override public void setjump_command(final SOSOptionString p_jump_command) {
		jump_command = p_jump_command;
	}
	/**
	* \var jump_command_delimiter : Command delimiter characters are specified using t
	*
	Command delimiter characters are specified using this parameter. These delimiters can then be used in the jump_command parameter to seperate multiple commands. These commands are then excecuted in separate SSH sessions.
	*
	*/
	@JSOptionDefinition(
						name = "jump_command_delimiter",
						description = "Command delimiter characters are specified using t",
						key = "jump_command_delimiter",
						type = "SOSOptionString",
						mandatory = true)
	public SOSOptionString	jump_command_delimiter	= new SOSOptionString(this, conClassName + ".jump_command_delimiter", // HashMap-Key
															"Command delimiter characters are specified using t", // Titel
															"%%", // InitValue
															"%%", // DefaultValue
															true // isMandatory
													);

	/**
	 * \brief getjump_command_delimiter
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionString getjump_command_delimiter() {
		return jump_command_delimiter;
	}

	/**
	 * \brief setjump_command_delimiter
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_jump_command_delimiter
	 */
	@Override public void setjump_command_delimiter(final SOSOptionString p_jump_command_delimiter) {
		jump_command_delimiter = p_jump_command_delimiter;
	}
	/**
	* \var jump_command_script : This parameter can be used as an alternative to ju
	*
	This parameter can be used as an alternative to jump_command, jump_command_delimiter and jump_command_script_file. It contains script code which will be transferred to the remote host as a file and will then be executed there.
	*
	*/
	@JSOptionDefinition(
						name = "jump_command_script",
						description = "This parameter can be used as an alternative to ju",
						key = "jump_command_script",
						type = "SOSOptionCommandScript",
						mandatory = false)
	public SOSOptionCommandScript	jump_command_script	= new SOSOptionCommandScript(this, conClassName + ".jump_command_script", // HashMap-Key
																"This parameter can be used as an alternative to ju", // Titel
																" ", // InitValue
																" ", // DefaultValue
																false // isMandatory
														);

	/**
	 * \brief getjump_command_script
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionCommandScript getjump_command_script() {
		return jump_command_script;
	}

	/**
	 * \brief setjump_command_script
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_jump_command_script
	 */
	@Override public void setjump_command_script(final SOSOptionCommandScript p_jump_command_script) {
		jump_command_script = p_jump_command_script;
	}
	/**
	* \var jump_command_script_file : This parameter can be used as an alternative to ju
	*
	This parameter can be used as an alternative to jump_command, jump_command_delimiter and jump_command_script. It contains the name of a script file, which will be transferred to the remote host and will then be executed there.
	*
	*/
	@JSOptionDefinition(
						name = "jump_command_script_file",
						description = "This parameter can be used as an alternative to ju",
						key = "jump_command_script_file",
						type = "SOSOptionCommandScriptFile",
						mandatory = false)
	public SOSOptionCommandScriptFile	jump_command_script_file	= new SOSOptionCommandScriptFile(this, conClassName + ".jump_command_script_file", // HashMap-Key
																			"This parameter can be used as an alternative to ju", // Titel
																			" ", // InitValue
																			" ", // DefaultValue
																			false // isMandatory
																	);

	/**
	 * \brief getjump_command_script_file
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionCommandScriptFile getjump_command_script_file() {
		return jump_command_script_file;
	}

	/**
	 * \brief setjump_command_script_file
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_jump_command_script_file
	 */
	@Override public void setjump_command_script_file(final SOSOptionCommandScriptFile p_jump_command_script_file) {
		jump_command_script_file = p_jump_command_script_file;
	}
	/**
	* \var jump_host : When using a jump_host then files are first transf
	*
	When using a jump_host then files are first transferred to this host and then to the target system. Different protocols (FTP/SFTP) can be used for these transfer operations. Host or IP address of the jump_host from which or to which files should be transferred in a first operation.
	*
	*/
	@JSOptionDefinition(
						name = "jump_host",
						description = "When using a jump_host then files are first transf",
						key = "jump_host",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionHostName	jump_host	= new SOSOptionHostName(this, conClassName + ".jump_host", // HashMap-Key
													"When using a jump_host then files are first transf", // Titel
													" ", // InitValue
													" ", // DefaultValue
													false // isMandatory
											);

	/**
	 * \brief getjump_host
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionHostName getjump_host() {
		return jump_host;
	}

	/**
	 * \brief setjump_host
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_jump_host
	 */
	@Override public void setjump_host(final SOSOptionHostName p_jump_host) {
		jump_host = p_jump_host;
	}
	/**
	* \var jump_ignore_error : Should the value true be specified, then execution
	*
	Should the value true be specified, then execution errors caused by commands on the SSH server are ignored. Otherwise such execution errors will be reported.
	*
	*/
	@JSOptionDefinition(
						name = "jump_ignore_error",
						description = "Should the value true be specified, then execution",
						key = "jump_ignore_error",
						type = "SOSOptionBoolean",
						mandatory = false)
	public SOSOptionBoolean	jump_ignore_error	= new SOSOptionBoolean(this, conClassName + ".jump_ignore_error", // HashMap-Key
														"Should the value true be specified, then execution", // Titel
														"false", // InitValue
														"false", // DefaultValue
														false // isMandatory
												);

	/**
	 * \brief getjump_ignore_error
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionBoolean getjump_ignore_error() {
		return jump_ignore_error;
	}

	/**
	 * \brief setjump_ignore_error
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_jump_ignore_error
	 */
	@Override public void setjump_ignore_error(final SOSOptionBoolean p_jump_ignore_error) {
		jump_ignore_error = p_jump_ignore_error;
	}
	/**
	* \var jump_ignore_signal : Should the value true be specified, t
	*
	Should the value true be specified, then on Unix systems all signals will be ignored that terminate the execution of a command on the SSH server - if for example a command is terminated using kill. Note that by default errors will be reported for commands that are terminated by signals.
	*
	*/
	@JSOptionDefinition(
						name = "jump_ignore_signal",
						description = "Should the value true be specified, t",
						key = "jump_ignore_signal",
						type = "SOSOptionBoolean",
						mandatory = false)
	public SOSOptionBoolean	jump_ignore_signal	= new SOSOptionBoolean(this, conClassName + ".jump_ignore_signal", // HashMap-Key
														"Should the value true be specified, t", // Titel
														"false", // InitValue
														"false", // DefaultValue
														false // isMandatory
												);

	/**
	 * \brief getjump_ignore_signal
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionBoolean getjump_ignore_signal() {
		return jump_ignore_signal;
	}

	/**
	 * \brief setjump_ignore_signal
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_jump_ignore_signal
	 */
	@Override public void setjump_ignore_signal(final SOSOptionBoolean p_jump_ignore_signal) {
		jump_ignore_signal = p_jump_ignore_signal;
	}
	/**
	* \var jump_ignore_stderr : This job checks if any output to stderr has been c
	*
	This job checks if any output to stderr has been created by a command that is being executed on the SSH server and reports this as an error. Should the value true be specified for this parameter, then output to stderr will not be reported as an error by the Job Scheduler.
	*
	*/
	@JSOptionDefinition(
						name = "jump_ignore_stderr",
						description = "This job checks if any output to stderr has been c",
						key = "jump_ignore_stderr",
						type = "SOSOptionBoolean",
						mandatory = false)
	public SOSOptionBoolean	jump_ignore_stderr	= new SOSOptionBoolean(this, conClassName + ".jump_ignore_stderr", // HashMap-Key
														"This job checks if any output to stderr has been c", // Titel
														"false", // InitValue
														"false", // DefaultValue
														false // isMandatory
												);

	/**
	 * \brief getjump_ignore_stderr
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionBoolean getjump_ignore_stderr() {
		return jump_ignore_stderr;
	}

	/**
	 * \brief setjump_ignore_stderr
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_jump_ignore_stderr
	 */
	@Override public void setjump_ignore_stderr(final SOSOptionBoolean p_jump_ignore_stderr) {
		jump_ignore_stderr = p_jump_ignore_stderr;
	}
	/**
	* \var jump_password : Password for authentication with the jump_host.
	*
	Password for authentication with the jump_host.
	*
	*/
	@JSOptionDefinition(
						name = "jump_password",
						description = "Password for authentication with the jump_host.",
						key = "jump_password",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionPassword	jump_password	= new SOSOptionPassword(this, conClassName + ".jump_password", // HashMap-Key
														"Password for authentication with the jump_host.", // Titel
														" ", // InitValue
														" ", // DefaultValue
														false // isMandatory
												);

	/**
	 * \brief getjump_password
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionPassword getjump_password() {
		return jump_password;
	}

	/**
	 * \brief setjump_password
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_jump_password
	 */
	@Override public void setjump_password(final SOSOptionPassword p_jump_password) {
		jump_password = p_jump_password;
	}
	/**
	* \var jump_port : Port on the jump_host by which files should be tra
	*
	Port on the jump_host by which files should be transferred. For FTP this is usually port 21, for SFTP this is usually port 22.
	*
	*/
	@JSOptionDefinition(
						name = "jump_port",
						description = "Port on the jump_host by which files should be tra",
						key = "jump_port",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionPortNumber	jump_port	= new SOSOptionPortNumber(this, conClassName + ".jump_port", // HashMap-Key
													"Port on the jump_host by which files should be tra", // Titel
													"22", // InitValue
													"22", // DefaultValue
													false // isMandatory
											);

	/**
	 * \brief getjump_port
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionPortNumber getjump_port() {
		return jump_port;
	}

	/**
	 * \brief setjump_port
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_jump_port
	 */
	@Override public void setjump_port(final SOSOptionPortNumber p_jump_port) {
		jump_port = p_jump_port;
	}
	/**
	* \var jump_protocol : When using a jump_host then files are first transf
	*
	When using a jump_host then files are first transferred to this host and then to the target system. Different protocols (FTP/SFTP) can be used for these transfer operations. This parameter expects ftp, sftp or ftps to be specified. If sftp is used, then the jump_ssh_* parameters will be considered.
	*
	*/
	@JSOptionDefinition(
						name = "jump_protocol",
						description = "When using a jump_host then files are first transf",
						key = "jump_protocol",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionString	jump_protocol	= new SOSOptionString(this, conClassName + ".jump_protocol", // HashMap-Key
													"When using a jump_host then files are first transf", // Titel
													"sftp", // InitValue
													"sftp", // DefaultValue
													false // isMandatory
											);

	/**
	 * \brief getjump_protocol
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionString getjump_protocol() {
		return jump_protocol;
	}

	/**
	 * \brief setjump_protocol
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_jump_protocol
	 */
	@Override public void setjump_protocol(final SOSOptionString p_jump_protocol) {
		jump_protocol = p_jump_protocol;
	}
	/**
	* \var jump_proxy_host : The value of this parameter is the host name or th
	*
	The value of this parameter is the host name or the IP address of a proxy used in order to establish a connection to the jump host. The use of a proxy is optional.
	*
	*/
	@JSOptionDefinition(
						name = "jump_proxy_host",
						description = "The value of this parameter is the host name or th",
						key = "jump_proxy_host",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionString	jump_proxy_host	= new SOSOptionString(this, conClassName + ".jump_proxy_host", // HashMap-Key
													"The value of this parameter is the host name or th", // Titel
													" ", // InitValue
													" ", // DefaultValue
													false // isMandatory
											);

	/**
	 * \brief getjump_proxy_host
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionString getjump_proxy_host() {
		return jump_proxy_host;
	}

	/**
	 * \brief setjump_proxy_host
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_jump_proxy_host
	 */
	@Override public void setjump_proxy_host(final SOSOptionString p_jump_proxy_host) {
		jump_proxy_host = p_jump_proxy_host;
	}
	/**
	* \var jump_proxy_password : This parameter specifies the password for the prox
	*
	This parameter specifies the password for the proxy server user account, should a proxy be used in order to connect to the jump host, see parameter jump_proxy_host.
	*
	*/
	@JSOptionDefinition(
						name = "jump_proxy_password",
						description = "This parameter specifies the password for the prox",
						key = "jump_proxy_password",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionString	jump_proxy_password	= new SOSOptionString(this, conClassName + ".jump_proxy_password", // HashMap-Key
														"This parameter specifies the password for the prox", // Titel
														" ", // InitValue
														" ", // DefaultValue
														false // isMandatory
												);

	/**
	 * \brief getjump_proxy_password
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionString getjump_proxy_password() {
		return jump_proxy_password;
	}

	/**
	 * \brief setjump_proxy_password
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_jump_proxy_password
	 */
	@Override public void setjump_proxy_password(final SOSOptionString p_jump_proxy_password) {
		jump_proxy_password = p_jump_proxy_password;
	}
	/**
	* \var jump_proxy_port : This parameter specifies the port of a proxy that
	*
	This parameter specifies the port of a proxy that is used in order to establish a connection to the jump host, see parameter jump_proxy_host.
	*
	*/
	@JSOptionDefinition(
						name = "jump_proxy_port",
						description = "This parameter specifies the port of a proxy that",
						key = "jump_proxy_port",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionString	jump_proxy_port	= new SOSOptionString(this, conClassName + ".jump_proxy_port", // HashMap-Key
													"This parameter specifies the port of a proxy that", // Titel
													" ", // InitValue
													" ", // DefaultValue
													false // isMandatory
											);

	/**
	 * \brief getjump_proxy_port
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionString getjump_proxy_port() {
		return jump_proxy_port;
	}

	/**
	 * \brief setjump_proxy_port
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_jump_proxy_port
	 */
	@Override public void setjump_proxy_port(final SOSOptionString p_jump_proxy_port) {
		jump_proxy_port = p_jump_proxy_port;
	}
	/**
	* \var jump_proxy_user : The value of this parameter specifies the user acc
	*
	The value of this parameter specifies the user account for authentication by the proxy server should a proxy be used in order to connect to the jump host, see parameter jump_proxy_host.
	*
	*/
	@JSOptionDefinition(
						name = "jump_proxy_user",
						description = "The value of this parameter specifies the user acc",
						key = "jump_proxy_user",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionUserName	jump_proxy_user	= new SOSOptionUserName(this, conClassName + ".jump_proxy_user", // HashMap-Key
														"The value of this parameter specifies the user acc", // Titel
														" ", // InitValue
														" ", // DefaultValue
														false // isMandatory
												);

	/**
	 * \brief getjump_proxy_user
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionUserName getjump_proxy_user() {
		return jump_proxy_user;
	}

	/**
	 * \brief setjump_proxy_user
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_jump_proxy_user
	 */
	@Override public void setjump_proxy_user(final SOSOptionUserName p_jump_proxy_user) {
		jump_proxy_user = p_jump_proxy_user;
	}
	
	
	/**
	* \var proxy_protocol : http, socks4 oder socks5
	*
	*
	*/
	@JSOptionDefinition(
						name = "jump_proxy_protocol",
						description = "Jump Proxy protocol",
						key = "jump_proxy_protocol",
						type = "SOSOptionProxyProtocol",
						mandatory = false)
	public SOSOptionProxyProtocol	jump_proxy_protocol	= new SOSOptionProxyProtocol(this, conClassName + ".jump_proxy_protocol", // HashMap-Key
													"Jump Proxy protocol", // Titel
													SOSOptionProxyProtocol.Protocol.http.name(), // InitValue
													SOSOptionProxyProtocol.Protocol.http.name(), // DefaultValue
													false // isMandatory
											);

	public SOSOptionProxyProtocol getjump_proxy_protocol() {
		return jump_proxy_protocol;
	}

	public void setjump_proxy_protocol(SOSOptionProxyProtocol val) {
		jump_proxy_protocol = val;
	}
	
	/**
	* \var jump_simulate_shell : Should the value true be specified for this parame
	*
	Should the value true be specified for this parameter, then a login to a shell is simulated to execute commands. Some scripts may cause problems if no shell is present.
	*
	*/
	@JSOptionDefinition(
						name = "jump_simulate_shell",
						description = "Should the value true be specified for this parame",
						key = "jump_simulate_shell",
						type = "SOSOptionBoolean",
						mandatory = false)
	public SOSOptionBoolean	jump_simulate_shell	= new SOSOptionBoolean(this, conClassName + ".jump_simulate_shell", // HashMap-Key
														"Should the value true be specified for this parame", // Titel
														"false", // InitValue
														"false", // DefaultValue
														false // isMandatory
												);

	/**
	 * \brief getjump_simulate_shell
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionBoolean getjump_simulate_shell() {
		return jump_simulate_shell;
	}

	/**
	 * \brief setjump_simulate_shell
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_jump_simulate_shell
	 */
	@Override public void setjump_simulate_shell(final SOSOptionBoolean p_jump_simulate_shell) {
		jump_simulate_shell = p_jump_simulate_shell;
	}
	/**
	* \var jump_simulate_shell_inactivity_timeout : If no new characters are written to stdout or stde
	*
	If no new characters are written to stdout or stderr after the given number of milliseconds, then it is assumed that the command has been carried out and that the shell is waiting for the next command.
	*
	*/
	@JSOptionDefinition(
						name = "jump_simulate_shell_inactivity_timeout",
						description = "If no new characters are written to stdout or stde",
						key = "jump_simulate_shell_inactivity_timeout",
						type = "SOSOptionInteger",
						mandatory = false)
	public SOSOptionInteger	jump_simulate_shell_inactivity_timeout	= new SOSOptionInteger(this, conClassName + ".jump_simulate_shell_inactivity_timeout", // HashMap-Key
																			"If no new characters are written to stdout or stde", // Titel
																			" ", // InitValue
																			" ", // DefaultValue
																			false // isMandatory
																	);

	/**
	 * \brief getjump_simulate_shell_inactivity_timeout
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionInteger getjump_simulate_shell_inactivity_timeout() {
		return jump_simulate_shell_inactivity_timeout;
	}

	/**
	 * \brief setjump_simulate_shell_inactivity_timeout
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_jump_simulate_shell_inactivity_timeout
	 */
	@Override public void setjump_simulate_shell_inactivity_timeout(final SOSOptionInteger p_jump_simulate_shell_inactivity_timeout) {
		jump_simulate_shell_inactivity_timeout = p_jump_simulate_shell_inactivity_timeout;
	}
	/**
	* \var jump_simulate_shell_login_timeout : If no new characters are written to stdout or stde
	*
	If no new characters are written to stdout or stderr after the given number of milliseconds, then it is assumed that the login has been carried out and that the shell is waiting for the next command.
	*
	*/
	@JSOptionDefinition(
						name = "jump_simulate_shell_login_timeout",
						description = "If no new characters are written to stdout or stde",
						key = "jump_simulate_shell_login_timeout",
						type = "SOSOptionInteger",
						mandatory = false)
	public SOSOptionInteger	jump_simulate_shell_login_timeout	= new SOSOptionInteger(this, conClassName + ".jump_simulate_shell_login_timeout", // HashMap-Key
																		"If no new characters are written to stdout or stde", // Titel
																		" ", // InitValue
																		" ", // DefaultValue
																		false // isMandatory
																);

	/**
	 * \brief getjump_simulate_shell_login_timeout
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionInteger getjump_simulate_shell_login_timeout() {
		return jump_simulate_shell_login_timeout;
	}

	/**
	 * \brief setjump_simulate_shell_login_timeout
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_jump_simulate_shell_login_timeout
	 */
	@Override public void setjump_simulate_shell_login_timeout(final SOSOptionInteger p_jump_simulate_shell_login_timeout) {
		jump_simulate_shell_login_timeout = p_jump_simulate_shell_login_timeout;
	}
	/**
	* \var jump_simulate_shell_prompt_trigger : The expected command line prompt. Using this promp
	*
	The expected command line prompt. Using this prompt the program tries to find out if commands may be entered or have been carried out. If no prompt can be configured, then timeout parameters have to be used in order to check if the shell is ready to accept commands.
	*
	*/
	@JSOptionDefinition(
						name = "jump_simulate_shell_prompt_trigger",
						description = "The expected command line prompt. Using this promp",
						key = "jump_simulate_shell_prompt_trigger",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionString	jump_simulate_shell_prompt_trigger	= new SOSOptionString(this, conClassName + ".jump_simulate_shell_prompt_trigger", // HashMap-Key
																		"The expected command line prompt. Using this promp", // Titel
																		" ", // InitValue
																		" ", // DefaultValue
																		false // isMandatory
																);

	/**
	 * \brief getjump_simulate_shell_prompt_trigger
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionString getjump_simulate_shell_prompt_trigger() {
		return jump_simulate_shell_prompt_trigger;
	}

	/**
	 * \brief setjump_simulate_shell_prompt_trigger
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_jump_simulate_shell_prompt_trigger
	 */
	@Override public void setjump_simulate_shell_prompt_trigger(final SOSOptionString p_jump_simulate_shell_prompt_trigger) {
		jump_simulate_shell_prompt_trigger = p_jump_simulate_shell_prompt_trigger;
	}
	/**
	* \var jump_ssh_auth_file : This parameter specifies the path and name of a us
	*
	This parameter specifies the path and name of a user's private key file used for login to the SSH server of the jump_host. This parameter must be specified if the publickey authentication method has been specified in the jump_ssh_auth_method parameter. Should the private key file be secured by a password, then this password has to be specified using the jump_password parameter.
	*
	*/
	@JSOptionDefinition(
						name = "jump_ssh_auth_file",
						description = "This parameter specifies the path and name of a us",
						key = "jump_ssh_auth_file",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionInFileName	jump_ssh_auth_file	= new SOSOptionInFileName(this, conClassName + ".jump_ssh_auth_file", // HashMap-Key
															"This parameter specifies the path and name of a us", // Titel
															" ", // InitValue
															" ", // DefaultValue
															false // isMandatory
													);

	/**
	 * \brief getjump_ssh_auth_file
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionInFileName getjump_ssh_auth_file() {
		return jump_ssh_auth_file;
	}

	/**
	 * \brief setjump_ssh_auth_file
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_jump_ssh_auth_file
	 */
	@Override public void setjump_ssh_auth_file(final SOSOptionInFileName p_jump_ssh_auth_file) {
		jump_ssh_auth_file = p_jump_ssh_auth_file;
	}
	/**
	* \var jump_ssh_auth_method : This parameter specifies the authentication method
	*
	This parameter specifies the authentication method for the SSH server - the publickey and password methods are supported. When the publickey authentication method is used, then the path name of the private key file must be set in the jump_ssh_auth_file parameter. Should the private key file be secured by a passphrase then this passphrase has to be specified by the jump_password parameter. For the password authentication method the password for the user account has to be specified using the jump_password parameter. The authentication methods that are enabled depend on the SSH server configuration. Not all SSH servers are configured for password authentication.
	*
	*/
	@JSOptionDefinition(
						name = "jump_ssh_auth_method",
						description = "This parameter specifies the authentication method",
						key = "jump_ssh_auth_method",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionAuthenticationMethod	jump_ssh_auth_method	= new SOSOptionAuthenticationMethod(this, conClassName + ".jump_ssh_auth_method", // HashMap-Key
																			"This parameter specifies the authentication method", // Titel
																			" ", // InitValue
																			" ", // DefaultValue
																			false // isMandatory
																	);

	/**
	 * \brief getjump_ssh_auth_method
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionAuthenticationMethod getjump_ssh_auth_method() {
		return jump_ssh_auth_method;
	}

	/**
	 * \brief setjump_ssh_auth_method
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_jump_ssh_auth_method
	 */
	@Override public void setjump_ssh_auth_method(final SOSOptionAuthenticationMethod p_jump_ssh_auth_method) {
		jump_ssh_auth_method = p_jump_ssh_auth_method;
	}
	/**
	* \var jump_user : User name for authentication with the jump_host.
	*
	User name for authentication with the jump_host.
	*
	*/
	@JSOptionDefinition(
						name = "jump_user",
						description = "User name for authentication with the jump_host.",
						key = "jump_user",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionUserName	jump_user	= new SOSOptionUserName(this, conClassName + ".jump_user", // HashMap-Key
													"User name for authentication with the jump_host.", // Titel
													"", // InitValue
													"", // DefaultValue
													false // isMandatory
											);

	/**
	 * \brief getjump_user
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionUserName getjump_user() {
		return jump_user;
	}

	/**
	 * \brief setjump_user
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_jump_user
	 */
	@Override public void setjump_user(final SOSOptionUserName p_jump_user) {
		jump_user = p_jump_user;
	}
	
	/**
	 * 
	 */
	@JSOptionDefinition(
			name = "jump_dir",
			description = "This parameter specifies the directory on the jump host",
			key = "jump_dir",
			type = "SOSOptionString",
			mandatory = false)
	public SOSOptionString	jump_dir	= new SOSOptionString(this, conClassName + ".jump_dir", // HashMap-Key
										"This parameter specifies the directory on the jump host", // Titel
										"/tmp", // InitValue
										"/tmp", // DefaultValue
										false // isMandatory
								);

	/**
	* \brief getjump_dir
	*
	* \details
	*
	* \return
	*
	* @return
	*/
	public SOSOptionString getjump_dir() {
	return jump_dir;
	}

	/**
	* \brief setjump_dir
	*
	* \details
	*
	* \return
	*
	* @param val
	*/
	public void setjump_dir(final SOSOptionString val) {
		jump_dir = val;
	}
	
	/**
	 * 
	 */
	@JSOptionDefinition(
			name = "jump_strict_hostKey_checking",
			description = "Check the hostkey against known hosts for SSH",
			key = "jump_strict_hostKey_checking",
			type = "SOSOptionBoolean",
			mandatory = false)
	public SOSOptionBoolean	jump_strict_hostkey_checking	= new SOSOptionBoolean(this, conClassName + ".jump_strict_hostkey_checking",
												"Check the hostkey against known hosts for SSH", "false", "false", false);

	public SOSOptionBoolean getjump_strict_hostKey_checking() {
		return jump_strict_hostkey_checking;
	}

	public void setjump_strict_hostKey_checking(final String value) {
		jump_strict_hostkey_checking.Value(value);
	}
	
	/**
	 * 
	 */
	@JSOptionDefinition(
			name = "jump_platform",
			description = "This parameter specifies the platform on the jump host",
			key = "jump_dir",
			type = "SOSOptionPlatform",
			mandatory = false)
	public SOSOptionPlatform jump_platform	= new SOSOptionPlatform(this, conClassName + ".jump_platform", // HashMap-Key
										"This parameter specifies the platform on the jump host", // Titel
										SOSOptionPlatform.enuValidPlatforms.unix.name(), // InitValue
										SOSOptionPlatform.enuValidPlatforms.unix.name(), // DefaultValue
										false // isMandatory
								);
	
	/**
	* \brief getjump_platform
	*
	* \details
	*
	* \return
	*
	* @return
	*/
	public SOSOptionPlatform getjump_platform() {
	return jump_platform;
	}

	/**
	* \brief setjump_platform
	*
	* \details
	*
	* \return
	*
	* @param val
	*/
	public void setjump_platform(final SOSOptionPlatform val) {
		jump_platform = val;
	}
	
	/**
	* \var local_dir : local_dir Local directory into which or from which
	*
	Local directory into which or from which files should be transferred. By default the current working directory is used. Besides paths in the local file system UNC path names are supported that could be used to address remote server systems: \\somehost\somedirectory can be used in the same way as //somehost/somedirectory to transfer files from an FTP/SFTP server to a different remote server system. Moreover, you could specify URIs for a file schema as in file:////somehost/somedirectory. Please, consider the required number of slashes. file URIs are subject to the following limitations due to constraints of the underlying Java JRE: File names and path names must not contain any spaces. Authentication by authority strings as in file:////user:password@somehost/somedirectory is not supported.
	*
	*/
	@JSOptionDefinition(
						name = "local_dir",
						description = "local_dir Local directory into which or from which",
						key = "local_dir",
						type = "SOSOptionFolderName",
						mandatory = true)
	public SOSOptionFolderName	local_dir	= new SOSOptionFolderName(this, conClassName + ".local_dir", // HashMap-Key
													"local_dir Local directory into which or from which", // Titel
													"", // InitValue
													"", // DefaultValue
													false // isMandatory
											);

	/**
	 * \brief getlocal_dir
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionFolderName getlocal_dir() {
		return local_dir;
	}

	/**
	 * \brief setlocal_dir
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_local_dir
	 */
	@Override public void setlocal_dir(final SOSOptionFolderName p_local_dir) {
		local_dir = p_local_dir;
	}
	/**
	* \var mandator : This parameter specifies the mandator for which a
	*
	This parameter specifies the mandator for which a file transfer is effected. The mandator is added to an optional history file, see parameter history and has no technical relevance for the transfer.
	*
	*/
	@JSOptionDefinition(
						name = "mandator",
						description = "This parameter specifies the mandator for which a",
						key = "mandator",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionString	mandator	= new SOSOptionString(this, conClassName + ".mandator", // HashMap-Key
												"This parameter specifies the mandator for which a", // Titel
												"SOS", // InitValue
												"SOS", // DefaultValue
												false // isMandatory
										);

	/**
	 * \brief getmandator
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionString getmandator() {
		return mandator;
	}

	/**
	 * \brief setmandator
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_mandator
	 */
	@Override public void setmandator(final SOSOptionString p_mandator) {
		mandator = p_mandator;
	}
	/**
	* \var operation : Operation to be executed send, receive, remove,
	*
	send, receive, remove, execute or install. send - transfer files by FTP/SFTP to a remote server receive - transfer files by FTP/SFTP from a remote server remove - remove files by FTP/SFTP on a remote server execute - execute a command by SSH on a remote server install - install SOSFTP on a remote server
	*
	*/
	@JSOptionDefinition(
						name = "operation",
						description = "Operation to be executed send, receive, remove,",
						key = "operation",
						type = "SOSOptionStringValueList",
						mandatory = true)
	public SOSOptionJadeOperation	operation	= new SOSOptionJadeOperation(this, conClassName + ".operation", // HashMap-Key
														"Operation to be executed send, receive, remove,", // Titel
														"send", // InitValue
														"send", // DefaultValue
														true // isMandatory
												);

	/**
	 * \brief getoperation
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionJadeOperation getoperation() {
		return operation;
	}

	/**
	 * \brief setoperation
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_operation
	 */
	@Override public void setoperation(final SOSOptionJadeOperation p_operation) {
		operation = p_operation;
	}
	/**
	* \var overwrite_files : This parameter specifies if existing files should
	*
	This parameter specifies if existing files should be overwritten. Should this parameter be used with force_files und should no files be transferred due to overwrite protection then an error will be raised stating that "no matching files" could be found.
	*
	*/
	@JSOptionDefinition(
						name = "overwrite_files",
						description = "This parameter specifies if existing files should",
						key = "overwrite_files",
						type = "SOSOptionBoolean",
						mandatory = false)
	public SOSOptionBoolean	overwrite_files	= new SOSOptionBoolean(this, conClassName + ".overwrite_files", // HashMap-Key
													"This parameter specifies if existing files should", // Titel
													"true", // InitValue
													"true", // DefaultValue
													false // isMandatory
											);

	/**
	 * \brief getoverwrite_files
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionBoolean getoverwrite_files() {
		return overwrite_files;
	}

	/**
	 * \brief setoverwrite_files
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_overwrite_files
	 */
	@Override public void setoverwrite_files(final SOSOptionBoolean p_overwrite_files) {
		overwrite_files = p_overwrite_files;
	}
	/**
	* \var passive_mode : passive_mode Passive mode for FTP is often used wit
	*
	Passive mode for FTP is often used with firewalls. Valid values are 0 or 1.
	*
	*/
	@JSOptionDefinition(
						name = "passive_mode",
						description = "passive_mode Passive mode for FTP is often used wit",
						key = "passive_mode",
						type = "SOSOptionBoolean",
						mandatory = false)
	public SOSOptionBoolean	passive_mode	= new SOSOptionBoolean(this, conClassName + ".passive_mode", // HashMap-Key
													"passive_mode Passive mode for FTP is often used wit", // Titel
													"false", // InitValue
													"false", // DefaultValue
													false // isMandatory
											);

	/**
	 * \brief getpassive_mode
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionBoolean getpassive_mode() {
		return passive_mode;
	}

	/**
	 * \brief setpassive_mode
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_passive_mode
	 */
	@Override public void setpassive_mode(final SOSOptionBoolean p_passive_mode) {
		passive_mode = p_passive_mode;
	}
	public SOSOptionBoolean		FTPTransferModeIsPassive	= (SOSOptionBoolean) passive_mode.SetAlias(conClassName + ".FTPTransferModeIsPassive");
	/**
	* \var password : Password for UserID Password for a
	*
	Password for authentication at the FTP/SFTP server. For SSH/SFTP connections that make use of public/private key authentication the password parameter is specified for the passphrase that optionally secures a private key.
	*
	*/
	@JSOptionDefinition(
						name = "password",
						description = "Password for UserID Password for a",
						key = "password",
						type = "SOSOptionPassword",
						mandatory = false)
	public SOSOptionPassword	password					= new SOSOptionPassword(this, conClassName + ".password", // HashMap-Key
																	"Password for UserID Password for a", // Titel
																	" ", // InitValue
																	" ", // DefaultValue
																	false // isMandatory
															);

	/**
	 * \brief getpassword
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionPassword getpassword() {
		return password;
	}

	/**
	 * \brief setpassword
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_password
	 */
	@Override public void setpassword(final SOSOptionPassword p_password) {
		password = p_password;
	}
	/**
	* \var poll_interval : This parameter specifies the interval in seconds
	*
	This parameter specifies the interval in seconds, how often a file is polled for within the duration that is specified by the parameter poll_timeout.
	*
	*/
	@JSOptionDefinition(
						name = "poll_interval",
						description = "This parameter specifies the interval in seconds",
						key = "poll_interval",
						type = "SOSOptionInteger",
						mandatory = false)
	public SOSOptionTime	poll_interval	= new SOSOptionTime(this, conClassName + ".poll_interval", // HashMap-Key
													"This parameter specifies the interval in seconds", // Titel
													"60", // InitValue
													"60", // DefaultValue
													false // isMandatory
											);

	/**
	 * \brief getpoll_interval
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionTime getpoll_interval() {
		return poll_interval;
	}

	/**
	 * \brief setpoll_interval
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_poll_interval
	 */
	@Override public void setpoll_interval(final SOSOptionTime p_poll_interval) {
		poll_interval = p_poll_interval;
	}
	/**
	 * \option Waiting_for_Late_comers
	 * \type SOSOptionBoolean
	 * \brief Waiting_for_Late_comers - Wait an addtional interval for late comers
	 *
	 * \details
	 * Wait an additional interval for late comers
	 *
	 * \mandatory: false
	 *
	 * \created 24.08.2012 17:54:34 by KB
	 */
	@JSOptionDefinition(
						name = "Waiting_for_Late_comers",
						description = "Wait an additional interval for late comers",
						key = "Waiting_for_Late_comers",
						type = "SOSOptionBoolean",
						mandatory = false)
	public SOSOptionBoolean	WaitingForLateComers	= new SOSOptionBoolean(
													// ...
															this, // ....
															conClassName + ".Waiting_for_Late_comers", // ...
															"Wait an additional interval for late comers", // ...
															"false", // ...
															"false", // ...
															false);

	public String getWaiting_for_Late_comers() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getWaiting_for_Late_comers";
		return WaitingForLateComers.Value();
	} // public String getWaiting_for_Late_comers

	public SOSFtpOptionsSuperClass setWaiting_for_Late_comers(final String pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setWaiting_for_Late_comers";
		WaitingForLateComers.Value(pstrValue);
		return this;
	} // public SOSFtpOptionsSuperClass setWaiting_for_Late_comers
	/**
	* \var poll_minfiles : This parameter specifies the number of files tha
	*
	This parameter specifies the number of files that have to be found during the polling period in order to cause the transfer to start. This parameter is used exclusively with the parameters poll_timeout.
	*
	*/
	@JSOptionDefinition(
						name = "poll_minfiles",
						description = "This parameter specifies the number of files tha",
						key = "poll_minfiles",
						type = "SOSOptionInteger",
						mandatory = false)
	public SOSOptionInteger	poll_minfiles	= new SOSOptionInteger(this, conClassName + ".poll_minfiles", // HashMap-Key
													"This parameter specifies the number of files tha", // Titel
													"0", // InitValue
													"0", // DefaultValue
													false // isMandatory
											);

	/**
	 * \brief getpoll_minfiles
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionInteger getpoll_minfiles() {
		return poll_minfiles;
	}

	/**
	 * \brief setpoll_minfiles
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_poll_minfiles
	 */
	@Override public void setpoll_minfiles(final SOSOptionInteger p_poll_minfiles) {
		poll_minfiles = p_poll_minfiles;
	}
	/**
	 * \option PollingDuration
	 * \type SOSOptionTime
	 * \brief PollingDuration - The duration of the polling period
	 *
	 * \details
	 * The duration of the polling period
	 *
	 * \mandatory: false
	 *
	 * \created 25.07.2012 13:23:44 by KB
	 */
	@JSOptionDefinition(
						name = "PollingDuration",
						description = "The duration of the polling period",
						key = "PollingDuration",
						type = "SOSOptionTime",
						mandatory = false)
	public SOSOptionTime	PollingDuration	= new SOSOptionTime(
											// ...
													this, // ....
													conClassName + ".PollingDuration", // ...
													"The duration of the polling period", // ...
													"0", // ...
													"0", // ...
													false);

	public String getPollingDuration() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getPollingDuration";
		return PollingDuration.Value();
	} // public String getPollingDuration

	public SOSFtpOptionsSuperClass setPollingDuration(final String pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setPollingDuration";
		PollingDuration.Value(pstrValue);
		return this;
	} // public SOSFtpOptionsSuperClass setPollingDuration
	/**
	* \var poll_timeout : This parameter specifies the time in minutes, how
	*
	This parameter specifies the time in minutes, how long a file is polled for. If a file becomes available within the time specified then it will be transferred, otherwise an error "no matching files" will be raised.
	*
	*/
	@JSOptionDefinition(
						name = "poll_timeout",
						description = "This parameter specifies the time in minutes, how",
						key = "poll_timeout",
						type = "SOSOptionInteger",
						mandatory = false)
	public SOSOptionInteger	poll_timeout	= new SOSOptionInteger(this, conClassName + ".poll_timeout", // HashMap-Key
													"This parameter specifies the time in minutes, how", // Titel
													"0", // InitValue
													"0", // DefaultValue
													false // isMandatory
											);

	/**
	 * \brief getpoll_timeout
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionInteger getpoll_timeout() {
		return poll_timeout;
	}

	/**
	 * \brief setpoll_timeout
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_poll_timeout
	 */
	@Override public void setpoll_timeout(final SOSOptionInteger p_poll_timeout) {
		poll_timeout = p_poll_timeout;
	}
	/**
	* \var port : Port-Number to be used for Data-Transfer
	*
	Port by which files should be transferred. For FTP this is usually port 21, for SFTP this is usually port 22.
	*
	*/
	@JSOptionDefinition(
						name = "port",
						description = "Port-Number to be used for Data-Transfer",
						key = "port",
						type = "SOSOptionPortNumber",
						mandatory = true)
	public SOSOptionPortNumber	port	= new SOSOptionPortNumber(this, conClassName + ".port", // HashMap-Key
												"Port-Number to be used for Data-Transfer", // Titel
												"21", // InitValue
												"21", // DefaultValue
												true // isMandatory
										);

	/**
	 * \brief getport
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionPortNumber getport() {
		return port;
	}

	/**
	 * \brief setport
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_port
	 */
	@Override public void setport(final SOSOptionPortNumber p_port) {
		port = p_port;
	}
	/**
	* \var ppid : This parameter is used for Unix systems and - as o
	*
	This parameter is used for Unix systems and - as opposed to other parameters - is usually specified in the start script sosftp.sh. The value of the environment variable $PPID is assigned, that contains the process id of the current parent process (PPID). 
	The parent process id is used when writing an entry to a history file for each transfer (see parameter history).
	*
	*/
	@JSOptionDefinition(
						name = "ppid",
						description = "This parameter is used for Unix systems and - as o",
						key = "ppid",
						type = "SOSOptionProcessID",
						mandatory = false)
	public SOSOptionProcessID	ppid	= new SOSOptionProcessID(this, conClassName + ".ppid", // HashMap-Key
												"This parameter is used for Unix systems and - as o", // Titel
												" ", // InitValue
												" ", // DefaultValue
												false // isMandatory
										);

	/**
	 * \brief getppid
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionProcessID getppid() {
		return ppid;
	}

	/**
	 * \brief setppid
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_ppid
	 */
	@Override public void setppid(final SOSOptionProcessID p_ppid) {
		ppid = p_ppid;
	}
	public SOSOptionProcessID	ParentProcessID	= (SOSOptionProcessID) ppid.SetAlias(conClassName + ".ParentProcessID");
	/**
	* \var profile : The Name of a Profile-Section to be executed
	*
	If a configuration file is being used (see parameter settings), then this parameter specifies a name of a section within the configuration file. Such sections, i.e. profiles, specify parameters as pairs of names and values that otherwise would be specified by command line parameters. At the command line the name of the configuration file and the profile are specified like this: sosftp.sh -operation=send -settings=settings.ini -profile=sample_transfer ...
	*
	*/
	@JSOptionDefinition(
						name = "profile",
						description = "The Name of a Profile-Section to be executed",
						key = "profile",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionString		profile			= new SOSOptionString(this, conClassName + ".profile", // HashMap-Key
														"The Name of a Profile-Section to be executed", // Titel
														" ", // InitValue
														" ", // DefaultValue
														false // isMandatory
												);

	/**
	 * \brief getprofile
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionString getprofile() {
		return profile;
	}

	/**
	 * \brief setprofile
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_profile
	 */
	@Override public void setprofile(final SOSOptionString p_profile) {
		profile = p_profile;
	}
	public SOSOptionString			SectionName	= (SOSOptionString) profile.SetAlias(conClassName + ".SectionName");
	/**
	* \var protocol : Type of requested Datatransfer The values ftp, sftp
	*
	The values ftp, sftp or ftps are valid for this parameter. If sftp is used, then the ssh_* parameters will be applied.
	*
	*/
	@JSOptionDefinition(
						name = "protocol",
						description = "Type of requested Datatransfer The values ftp, sftp",
						key = "protocol",
						type = "SOSOptionStringValueList",
						mandatory = true)
	public SOSOptionTransferType	protocol	= new SOSOptionTransferType(this, conClassName + ".protocol", // HashMap-Key
														"Type of requested Datatransfer The values ftp, sftp", // Titel
														"ftp", // InitValue
														"ftp", // DefaultValue
														true // isMandatory
												);

	/**
	 * \brief getprotocol
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionTransferType getprotocol() {
		return protocol;
	}

	/**
	 * \brief setprotocol
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_protocol
	 */
	@Override public void setprotocol(final SOSOptionTransferType p_protocol) {
		protocol = p_protocol;
	}
	public SOSOptionTransferType	TransferProtocol		= (SOSOptionTransferType) protocol.SetAlias(conClassName + ".TransferProtocol");
	/**
	* \var recursive : This parameter specifies if files from subdirector
	*
	This parameter specifies if files from subdirectories should be transferred recursively. Recursive processing is specified by one of the values yes or true. Regular expression matches apply to files from subdirectories as specified by the parameter file_spec.
	*
	*/
	@JSOptionDefinition(
						name = "recursive",
						description = "This parameter specifies if files from subdirector",
						key = "recursive",
						type = "SOSOptionBoolean",
						mandatory = false)
	public SOSOptionBoolean			recursive				= new SOSOptionBoolean(this, conClassName + ".recursive", // HashMap-Key
																	"This parameter specifies if files from subdirector", // Titel
																	"false", // InitValue
																	"false", // DefaultValue
																	false // isMandatory
															);
	public SOSOptionBoolean			IncludeSubdirectories	= (SOSOptionBoolean) recursive.SetAlias("include_sub_directories");

	/**
	 * \brief getrecursive
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionBoolean getrecursive() {
		return recursive;
	}

	/**
	 * \brief setrecursive
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_recursive
	 */
	@Override public void setrecursive(final SOSOptionBoolean p_recursive) {
		recursive = p_recursive;
	}
	public SOSOptionBoolean		RecurseSubFolders	= (SOSOptionBoolean) recursive.SetAlias(conClassName + ".RecurseSubFolders");
	/**
	* \var remote_dir : remote_dir Directory at the FTP/SFTP server from wh
	*
	Directory at the FTP/SFTP server from which or to which files should be transferred. By default the home directory of the user at the FTP/SFTP server is used.
	*
	*/
	@JSOptionDefinition(
						name = "remote_dir",
						description = "remote_dir Directory at the FTP/SFTP server from wh",
						key = "remote_dir",
						type = "SOSOptionFolderName",
						mandatory = true)
	public SOSOptionFolderName	remote_dir			= new SOSOptionFolderName(this, conClassName + ".remote_dir", // HashMap-Key
															"remote_dir Directory at the FTP/SFTP server from wh", // Titel
															".", // InitValue
															".", // DefaultValue
															false // isMandatory
													);

	/**
	 * \brief getremote_dir
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionFolderName getremote_dir() {
		return remote_dir;
	}

	/**
	 * \brief setremote_dir
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_remote_dir
	 */
	@Override public void setremote_dir(final SOSOptionFolderName p_remote_dir) {
		remote_dir = p_remote_dir;
	}
	/**
	* \var remove_files : This parameter specifies whether files on the FTP/
	*
	This parameter specifies whether files on the FTP/SFTP server should be removed after transfer.
	*
	*/
	@JSOptionDefinition(
						name = "remove_files",
						description = "This parameter specifies whether files on the FTP/",
						key = "remove_files",
						type = "SOSOptionBoolean",
						mandatory = false)
	public SOSOptionBoolean	remove_files				= new SOSOptionBoolean(this, conClassName + ".remove_files", // HashMap-Key
																"This parameter specifies whether files on the FTP/", // Titel
																"false", // InitValue
																"false", // DefaultValue
																false // isMandatory
														);
	public SOSOptionBoolean	DeleteFilesAfterTransfer	= (SOSOptionBoolean) remove_files.SetAlias(conClassName + ".DeleteFilesAfterTransfer");
	public SOSOptionBoolean	DeleteFilesOnSource			= (SOSOptionBoolean) remove_files.SetAlias(conClassName + ".DeleteFilesOnSource");

	/**
	 * \brief getremove_files
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionBoolean getremove_files() {
		return remove_files;
	}

	/**
	 * \brief setremove_files
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_remove_files
	 */
	@Override public void setremove_files(final SOSOptionBoolean p_remove_files) {
		remove_files = p_remove_files;
	}
	/**
	* \var replacement : String for replacement of matching character seque
	*
	String for replacement of matching character sequences within file names that are specified with the value of the parameter replacing. If multiple "capturing groups" shall be replaced then one replacement string per group has to be specified. These strings are separated by a semicolon ";": replacement: aa;[filename:];bb Supports masks for substitution in the file name with format strings that are enclosed with [ and ] . The following format strings are supported: [date: date format ] date format must be a valid Java data format string, e.g. yyyyMMddHHmmss , yyyy-MM-dd.HHmmss etc. [filename:] Will be substituted by the original file name including the file extension. [filename:lowercase] Will be substituted by the original file name including the file extension with all characters converted to lower case. [filename:uppercase] Will be substituted by the original file name including the file extension with all characters converted to upper case. Requires the parameter replacing to be specified.
	*
	*/
	@JSOptionDefinition(
						name = "replacement",
						description = "String for replacement of matching character seque",
						key = "replacement",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionString	replacement	= new SOSOptionString(this, conClassName + ".replacement", // HashMap-Key
												"String for replacement of matching character seque", // Titel
												null, // InitValue
												null, // DefaultValue
												false // isMandatory
										);

	/**
	 * \brief getreplacement
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionString getreplacement() {
		return replacement;
	}

	/**
	 * \brief setreplacement
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_replacement
	 */
	@Override public void setreplacement(final SOSOptionString p_replacement) {
		replacement = p_replacement;
	}
	public SOSOptionString	ReplaceWith	= (SOSOptionString) replacement.SetAlias(conClassName + ".ReplaceWith");
	/**
	* \var replacing : Regular expression for filename replacement with
	*
	Regular expression for filename replacement with the value of the parameter replacement. If the expression matches the filename then the groups found are replaced. a) For replacement "capturing groups" are used. Only the content of the capturing groups is replaced. Replacements are separated by a semicolon ";". Example: replacing : (1)abc(12)def(.*) replacement : A;BB;CCC Input file: 1abc12def123.txt Output file: AabcBBdefCCC b) If no "capturing groups" are specified then the entire match is replaced. Example: replacing : Hello replacement : 1234 Input file: Hello_World.txt Output file: 1234_World.txt Requires the parameter replacement to be specified.
	*
	*/
	@JSOptionDefinition(
						name = "replacing",
						description = "Regular expression for filename replacement with",
						key = "replacing",
						type = "SOSOptionRegExp",
						mandatory = false)
	public SOSOptionRegExp	replacing	= new SOSOptionRegExp(this, conClassName + ".replacing", // HashMap-Key
												"Regular expression for filename replacement with", // Titel
												" ", // InitValue
												" ", // DefaultValue
												false // isMandatory
										);

	/**
	 * \brief getreplacing
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionRegExp getreplacing() {
		return replacing;
	}

	/**
	 * \brief setreplacing
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_replacing
	 */
	@Override public void setreplacing(final SOSOptionRegExp p_replacing) {
		replacing = p_replacing;
	}
	public SOSOptionRegExp		ReplaceWhat	= (SOSOptionRegExp) replacing.SetAlias(conClassName + ".ReplaceWhat");
	/**
	* \var root : The parameter specifies the directory in which thi
	*
	The parameter specifies the directory in which this program is allowed to create temporary files. Temporary files are required if due to the parameter setting jump_host files have to be stored on an intermediary server and will be removed after completion of the transfer. Without this parameter the temporary directory is used as provided by the operating system.
	*
	*/
	@JSOptionDefinition(
						name = "root",
						description = "The parameter specifies the directory in which thi",
						key = "root",
						type = "SOSOptionFolderName",
						mandatory = false)
	public SOSOptionFolderName	root		= new SOSOptionFolderName(this, conClassName + ".root", // HashMap-Key
													"The parameter specifies the directory in which thi", // Titel
													"", // InitValue
													"", // DefaultValue
													false // isMandatory
											);

	/**
	 * \brief getroot
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionFolderName getroot() {
		return root;
	}

	/**
	 * \brief setroot
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_root
	 */
	@Override public void setroot(final SOSOptionFolderName p_root) {
		root = p_root;
	}
	public SOSOptionFolderName	TempFolderName			= (SOSOptionFolderName) root.SetAlias(conClassName + ".TempFolderName");
	/**
	* \var scheduler_host : This parameter specifies the host name or IP addre
	*
	This parameter specifies the host name or IP address of a server for which Job Scheduler is operated for Managed File Transfer. The contents of an optional history file (see parameter history), 
	is added to a central database by Job Scheduler. This parameter causes the transfer of the history entries for the current transfer by UDP to Job Scheduler. Should Job Scheduler not be 
	accessible then no errors are reported, instead, the contents of the history will automaticall be processed later on.
	*
	*/
	@JSOptionDefinition(
						name = "scheduler_host",
						description = "This parameter specifies the host name or IP addre",
						key = "scheduler_host",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionHostName	scheduler_host			= new SOSOptionHostName(this, conClassName + ".scheduler_host", // HashMap-Key
																"This parameter specifies the host name or IP addre", // Titel
																"", // InitValue
																"", // DefaultValue
																false // isMandatory
														);
	public SOSOptionHostName	BackgroundServiceHost	= (SOSOptionHostName) scheduler_host.SetAlias("Background_Service_Host");

	/**
	 * \brief getscheduler_host
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionHostName getscheduler_host() {
		return scheduler_host;
	}

	/**
	 * \brief setscheduler_host
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_scheduler_host
	 */
	@Override public void setscheduler_host(final SOSOptionHostName p_scheduler_host) {
		scheduler_host = p_scheduler_host;
	}
	/**
	* \var scheduler_job_chain : The name of a job chain for Managed File Transfer
	*
	The name of a job chain for Managed File Transfer with Job Scheduler, see parameter scheduler_host. The job chain accepts history entries and performs an import into a central database.
	*
	*/
	@JSOptionDefinition(
						name = "scheduler_job_chain",
						description = "The name of a job chain for Managed File Transfer",
						key = "scheduler_job_chain",
						type = "JSJobChain",
						mandatory = false)
	public JSJobChain	scheduler_job_chain				= new JSJobChain(this, conClassName + ".scheduler_job_chain", // HashMap-Key
																"The name of a job chain for Background Service", // Titel
																"/sos/jade/jade_history", // InitValue
																"/sos/jade/jade_history", // DefaultValue
																false // isMandatory
														);
	public JSJobChain	BackgroundServiceJobChainName	= (JSJobChain) scheduler_job_chain.SetAlias("BackgroundService_Job_Chain_Name");

	/**
	 * \brief getscheduler_job_chain
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public JSJobChain getscheduler_job_chain() {
		return scheduler_job_chain;
	}

	/**
	 * \brief setscheduler_job_chain
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_scheduler_job_chain
	 */
	@Override public void setscheduler_job_chain(final JSJobChain p_scheduler_job_chain) {
		scheduler_job_chain = p_scheduler_job_chain;
	}
	/**
	* \var scheduler_port : The port for which a Job Scheduler for Managed File Trans
	*
	The port for which a Job Scheduler for Managed File Transfer is operated, see parameter scheduler_host.
	*
	*/
	@JSOptionDefinition(
						name = "scheduler_port",
						description = "The port for which a Job Scheduler for Managed File Trans",
						key = "scheduler_port",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionPortNumber	scheduler_port			= new SOSOptionPortNumber(this, conClassName + ".scheduler_port", // HashMap-Key
																"The port for which a Job Scheduler for Managed File Trans", // Titel
																"0", // InitValue
																"4444", // DefaultValue
																false // isMandatory
														);
	public SOSOptionPortNumber	BackgroundServicePort	= (SOSOptionPortNumber) scheduler_port.SetAlias("Background_Service_Port",
																"Background_Service_PortNumber");

	/**
	 * \brief getscheduler_port
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionPortNumber getscheduler_port() {
		return scheduler_port;
	}

	/**
	 * \brief setscheduler_port
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_scheduler_port
	 */
	@Override public void setscheduler_port(final SOSOptionPortNumber p_scheduler_port) {
		scheduler_port = p_scheduler_port;
	}
	/**
	 * \option Restart
	 * \type SOSOptionBoolean
	 * \brief Restart - Set Restart/Resume Mode for Transfer
	 *
	 * \details
	 * Set Restart/Reesume Mode for Transfer
	 *
	 * \mandatory: false
	 *
	 * \created 29.11.2012 19:50:10 by KB
	 */
	@JSOptionDefinition(
						name = "Restart",
						description = "Set Restart/Resume Mode for Transfer",
						key = "Restart",
						type = "SOSOptionBoolean",
						mandatory = false)
	public SOSOptionBoolean	Restart			= new SOSOptionBoolean(
											// ...
													this, // ....
													conClassName + ".Restart", // ...
													"Set Restart/Resume Mode for Transfer", // ...
													"false", // ...
													"false", // ...
													false);
	public SOSOptionBoolean	ResumeTransfer	= (SOSOptionBoolean) Restart.SetAlias(conClassName + "Resume", conClassName + "Resume_Transfer");

	public String getRestart() {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::getRestart";
		return Restart.Value();
	} // public String getRestart

	public SOSFtpOptionsSuperClass setRestart(final String pstrValue) {
		@SuppressWarnings("unused") final String conMethodName = conClassName + "::setRestart";
		Restart.Value(pstrValue);
		return this;
	} // public SOSFtpOptionsSuperClass setRestart
	/**
	* \var settings : Name of INI-File which contains the profiles to execute
	*
	A configuration (INI-) file can be specified that contains profiles, i.e. sections, with parameters specified as pairs of names and values in a plain text format like this: [sample_transfer] protocol = ftp host = localhost port = 21 local_dir = /tmp ... At the command line the name of the configuration file and the profile are specified like this: sosftp.sh -operation=send -settings=settings.ini -profile=sample_transfer ... 
	A profile can reference the contents of other profiles like this: 
	[default] 
	history = /sosftp/transfer_history.csv 
	mandator = SOS scheduler_host = localhost scheduler_port = 4444 [sample_transfer] include = default protocol = ftp host = www.sos-berlin.com port = 21 local_dir = /tmp ... With this sample the profile sample_transfer includes the default profile via the include directive and thus applies the file transfer history settings.
	*
	*/
	@JSOptionDefinition(
						name = "settings",
						description = "Name of INI-File which contains the transfer profiles to execute",
						key = "settings",
						type = "SOSOptionIniFileName",
						mandatory = false)
	public SOSOptionIniFileName	settings			= new SOSOptionIniFileName(this, conClassName + ".settings", // HashMap-Key
															"Name of INI-File which contains the transfer profiles to execute", // Titel
															"", // InitValue
															"", // DefaultValue
															false // isMandatory
													);
	public SOSOptionIniFileName	ConfigurationFile	= (SOSOptionIniFileName) settings.SetAlias("JADE_Configuration_File", "JADE_Config_File", "Configuration",
															"JADE_Configuration", "JADE_INI_FILE");
	public SOSOptionIniFileName	SOSIniFileName		= (SOSOptionIniFileName) settings.SetAlias(conClassName + ".SOSIniFileName");

	/**
	 * \brief getsettings
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionIniFileName getsettings() {
		return settings;
	}

	/**
	 * \brief setsettings
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_settings
	 */
	@Override public void setsettings(final SOSOptionIniFileName p_settings) {
		settings = p_settings;
	}
	/**
	* \var skip_transfer : If this Parameter is set to true then
	*
	If this Parameter is set to true then all operations except for the transfer itself will be performed. This can be used to just trigger for files or to only delete files on the FTP/SFTP server.
	*
	*/
	@JSOptionDefinition(
						name = "skip_transfer",
						description = "If this Parameter is set to true then",
						key = "skip_transfer",
						type = "SOSOptionBoolean",
						mandatory = false)
	public SOSOptionBoolean	skip_transfer	= new SOSOptionBoolean(this, conClassName + ".skip_transfer", // HashMap-Key
													"If this Parameter is set to true then", // Titel
													"false", // InitValue
													"false", // DefaultValue
													false // isMandatory
											);

	/**
	 * \brief getskip_transfer
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionBoolean getskip_transfer() {
		return skip_transfer;
	}

	/**
	 * \brief setskip_transfer
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_skip_transfer
	 */
	@Override public void setskip_transfer(final SOSOptionBoolean p_skip_transfer) {
		skip_transfer = p_skip_transfer;
	}
	/**
	* \var ssh_auth_file : This parameter specifies the path and name of a us
	*
	This parameter specifies the path and name of a user's private key file that is used for authentication with an SSH server. This parameter has to be specified should the publickey authentication method have been specified in the ssh_auth_method parameter. Should the private key file be secured by a passphrase, then the passphrase has to be specified using the password parameter.
	*
	*/
	@JSOptionDefinition(
						name = "ssh_auth_file",
						description = "This parameter specifies the path and name of a us",
						key = "ssh_auth_file",
						type = "SOSOptionInFileName",
						mandatory = false)
	public SOSOptionInFileName	ssh_auth_file	= new SOSOptionInFileName(this, conClassName + ".ssh_auth_file", // HashMap-Key
														"This parameter specifies the path and name of a us", // Titel
														"", // InitValue
														"", // DefaultValue
														false // isMandatory
												);
	public SOSOptionInFileName	auth_file		= (SOSOptionInFileName) ssh_auth_file.SetAlias(conClassName + ".auth_file");

	/**
	 * \brief getssh_auth_file
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionInFileName getssh_auth_file() {
		return ssh_auth_file;
	}

	/**
	 * \brief setssh_auth_file
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_ssh_auth_file
	 */
	@Override public void setssh_auth_file(final SOSOptionInFileName p_ssh_auth_file) {
		ssh_auth_file = p_ssh_auth_file;
	}
	/**
	* \var ssh_auth_method : This parameter specifies the authentication method
	*
	This parameter specifies the authentication method for the SSH server - the publickey and password methods are supported. Should the publickey authentication method be used, then the path name of the private key file has to be specified with the ssh_auth_file parameter. Should the private key file be secured by a passphrase then the passphrase has to be specified with the password parameter. For the password authentication method the password for the user account has to be specified using the password parameter. The authentication methods that are enabled depend on the SSH server configuration. Not all SSH servers are configured for password authentication.
	*
	*/
	@JSOptionDefinition(
						name = "ssh_auth_method",
						description = "This parameter specifies the authentication method",
						key = "ssh_auth_method",
						type = "SOSOptionStringValueList",
						mandatory = false)
	public SOSOptionAuthenticationMethod	ssh_auth_method	= new SOSOptionAuthenticationMethod(this, conClassName + ".ssh_auth_method", // HashMap-Key
																	"This parameter specifies the authentication method", // Titel
																	"publickey", // InitValue
																	"publickey", // DefaultValue
																	false // isMandatory
															);
	public SOSOptionAuthenticationMethod	auth_method		= (SOSOptionAuthenticationMethod) ssh_auth_method.SetAlias(conClassName + ".auth_method");

	/**
	 * \brief getssh_auth_method
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionAuthenticationMethod getssh_auth_method() {
		return ssh_auth_method;
	}

	/**
	 * \brief setssh_auth_method
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_ssh_auth_method
	 */
	@Override public void setssh_auth_method(final SOSOptionAuthenticationMethod p_ssh_auth_method) {
		ssh_auth_method = p_ssh_auth_method;
	}
	/**
	* \var ssh_proxy_host : The value of this parameter is the host name or th
	*
	The value of this parameter is the host name or the IP address of a proxy that is used in order to establish a connection to the SSH server. The use of a proxy is optional.
	*
	*/
	@JSOptionDefinition(
						name = "ssh_proxy_host",
						description = "The value of this parameter is the host name or th",
						key = "ssh_proxy_host",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionString	ssh_proxy_host	= new SOSOptionString(this, conClassName + ".ssh_proxy_host", // HashMap-Key
													"The value of this parameter is the host name or th", // Titel
													" ", // InitValue
													" ", // DefaultValue
													false // isMandatory
											);

	/**
	 * \brief getssh_proxy_host
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionString getssh_proxy_host() {
		return ssh_proxy_host;
	}

	/**
	 * \brief setssh_proxy_host
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_ssh_proxy_host
	 */
	@Override public void setssh_proxy_host(final SOSOptionString p_ssh_proxy_host) {
		ssh_proxy_host = p_ssh_proxy_host;
	}
	/**
	* \var ssh_proxy_password : This parameter specifies the password for the prox
	*
	This parameter specifies the password for the proxy server user account, should a proxy be used in order to connect to the SSH server.
	*
	*/
	@JSOptionDefinition(
						name = "ssh_proxy_password",
						description = "This parameter specifies the password for the prox",
						key = "ssh_proxy_password",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionString	ssh_proxy_password	= new SOSOptionString(this, conClassName + ".ssh_proxy_password", // HashMap-Key
														"This parameter specifies the password for the prox", // Titel
														" ", // InitValue
														" ", // DefaultValue
														false // isMandatory
												);

	/**
	 * \brief getssh_proxy_password
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionString getssh_proxy_password() {
		return ssh_proxy_password;
	}

	/**
	 * \brief setssh_proxy_password
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_ssh_proxy_password
	 */
	@Override public void setssh_proxy_password(final SOSOptionString p_ssh_proxy_password) {
		ssh_proxy_password = p_ssh_proxy_password;
	}
	/**
	* \var ssh_proxy_port : This parameter specifies the port number of the pr
	*
	This parameter specifies the port number of the proxy, should a proxy be used in order to establish a connection to the SSH server.
	*
	*/
	@JSOptionDefinition(
						name = "ssh_proxy_port",
						description = "This parameter specifies the port number of the pr",
						key = "ssh_proxy_port",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionString	ssh_proxy_port	= new SOSOptionString(this, conClassName + ".ssh_proxy_port", // HashMap-Key
													"This parameter specifies the port number of the pr", // Titel
													" ", // InitValue
													" ", // DefaultValue
													false // isMandatory
											);

	/**
	 * \brief getssh_proxy_port
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionString getssh_proxy_port() {
		return ssh_proxy_port;
	}

	/**
	 * \brief setssh_proxy_port
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_ssh_proxy_port
	 */
	@Override public void setssh_proxy_port(final SOSOptionString p_ssh_proxy_port) {
		ssh_proxy_port = p_ssh_proxy_port;
	}
	/**
	* \var ssh_proxy_user : The value of this parameter specifies the user acc
	*
	The value of this parameter specifies the user account for authentication by the proxy server should a proxy be used in order to connect to the SSH server.
	*
	*/
	@JSOptionDefinition(
						name = "ssh_proxy_user",
						description = "The value of this parameter specifies the user acc",
						key = "ssh_proxy_user",
						type = "SOSOptionString",
						mandatory = false)
	public SOSOptionString	ssh_proxy_user	= new SOSOptionString(this, conClassName + ".ssh_proxy_user", // HashMap-Key
													"The value of this parameter specifies the user acc", // Titel
													" ", // InitValue
													" ", // DefaultValue
													false // isMandatory
											);

	/**
	 * \brief getssh_proxy_user
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionString getssh_proxy_user() {
		return ssh_proxy_user;
	}

	/**
	 * \brief setssh_proxy_user
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_ssh_proxy_user
	 */
	@Override public void setssh_proxy_user(final SOSOptionString p_ssh_proxy_user) {
		ssh_proxy_user = p_ssh_proxy_user;
	}
	/**
	* \var transactional : This parameter specifies if file transfers should
	*
	This parameter specifies if file transfers should be operated within a single transaction, i.e. either all files are successfully transferred or none. Should an error occur during a transfer operation then all transfers will be rolled back. When specifying the value true then the following applies: The parameter atomic_suffix has to be specified that causes target files to be created with a suffix such as "~" and that causes the respective files to be renamed to their target file name after the transfer of all files has been successfully completed. If at least one file out of a set of files cannot be transferred successfully then no files will be renamed, instead the temporarily created files are removed from the target system. The parameter remove_files that causes files to be removed after successful transfer will be effective only after all files have been successfully transferred. Otherwise no files will be removed.
	*
	*/
	@JSOptionDefinition(
						name = "transactional",
						description = "This parameter specifies if file transfers should",
						key = "transactional",
						type = "SOSOptionBoolean",
						mandatory = false)
	public SOSOptionBoolean	transactional	= new SOSOptionBoolean(this, conClassName + ".transactional", // HashMap-Key
													"This parameter specifies if file transfers should", // Titel
													"false", // InitValue
													"false", // DefaultValue
													false // isMandatory
											);

	/**
	 * \brief gettransactional
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionBoolean gettransactional() {
		return transactional;
	}

	/**
	 * \brief settransactional
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_transactional
	 */
	@Override public void settransactional(final SOSOptionBoolean p_transactional) {
		transactional = p_transactional;
	}
	public SOSOptionBoolean			TransactionMode	= (SOSOptionBoolean) transactional.SetAlias(conClassName + ".TransactionMode");
	/**
	* \var transfer_mode : Type of Character-Encoding Transfe
	*
	Transfer mode is used for FTP exclusively and can be either ascii or binary.
	*
	*/
	@JSOptionDefinition(
						name = "transfer_mode",
						description = "Type of Character-Encoding Transfe",
						key = "transfer_mode",
						type = "SOSOptionTransferMode",
						mandatory = false)
	public SOSOptionTransferMode	transfer_mode	= new SOSOptionTransferMode(this, conClassName + ".transfer_mode", // HashMap-Key
															"Type of Character-Encoding Transfe", // Titel
															"binary", // InitValue
															"binary", // DefaultValue
															false // isMandatory
													);

	/**
	 * \brief gettransfer_mode
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionTransferMode gettransfer_mode() {
		return transfer_mode;
	}

	/**
	 * \brief settransfer_mode
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_transfer_mode
	 */
	@Override public void settransfer_mode(final SOSOptionTransferMode p_transfer_mode) {
		transfer_mode = p_transfer_mode;
	}
	/**
	* \var user : UserID of user in charge User name
	*
	User name for authentication at the (FTP/SFTP) server.
	*
	*/
	@JSOptionDefinition(
						name = "user",
						description = "UserID of user in charge User name",
						key = "user",
						type = "SOSOptionUserName",
						mandatory = true)
	public SOSOptionUserName	user	= new SOSOptionUserName(this, conClassName + ".user", // HashMap-Key
												"UserID of user in charge User name", // Titel
												"", // InitValue
												"anonymous", // DefaultValue
												false // isMandatory
										);

	/**
	 * \brief getuser
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionUserName getuser() {
		return user;
	}

	/**
	 * \brief setuser
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_user
	 */
	@Override public void setuser(final SOSOptionUserName p_user) {
		user = p_user;
	}
	/**
	* \var verbose : The granuality of (Debug-)Messages The verbosit
	*
	The verbosity level specifies the intensity of log entries. A value between 1 and 9 can be specified. Higher values cause more detailed information to be logged. Log output is written to stdout or to a file that has been specified with the parameter log_filename.
	*
	*/
	@JSOptionDefinition(
						name = "verbose",
						description = "The granuality of (Debug-)Messages The verbosit",
						key = "verbose",
						type = "SOSOptionInteger",
						mandatory = false)
	public SOSOptionInteger	verbose	= new SOSOptionInteger(this, conClassName + ".verbose", // HashMap-Key
											"The granuality of (Debug-)Messages The verbosit", // Titel
											"1", // InitValue
											"10", // DefaultValue
											false // isMandatory
									);

	/**
	 * \brief getverbose
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionInteger getverbose() {
		return verbose;
	}

	/**
	 * \brief setverbose
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_verbose
	 */
	@Override public void setverbose(final SOSOptionInteger p_verbose) {
		verbose = p_verbose;
	}
	public SOSOptionInteger			VerbosityLevel			= (SOSOptionInteger) verbose.SetAlias(conClassName + ".VerbosityLevel");
	/**
	* \var zero_byte_transfer : This parameter specifies whether zero byte files
	*
	This parameter specifies whether zero byte files should be transferred and processed by subsequent commands. The following settings are available: yes : Files with zero byte size are transferred (default). no : Files with zero byte size are transferred, should at least one of the files have more than zero byte size. strict : Files with zero byte size are not transferred. An error will be raised if any zero byte file is found. relaxed : Files with zero byte size will not be transferred. However, no error will be raised if this results in no files being transferred. Use of this parameter can be refined using the force_files parameter: should force_files have the value false, then processing will be treated as successful in the event of no files having been transferred. Note that the remove_files parameter has unrestricted validity. Files with zero byte size will be removed regardless of whether or not they have been transferred.
	*
	*/
	@JSOptionDefinition(
						name = "zero_byte_transfer",
						description = "This parameter specifies whether zero byte files",
						key = "zero_byte_transfer",
						type = "SOSOptionStringValueList",
						mandatory = false)
	public SOSOptionStringValueList	zero_byte_transfer		= new SOSOptionStringValueList(this, conClassName + ".zero_byte_transfer", // HashMap-Key
																	"This parameter specifies whether zero byte files", // Titel
																	"yes;true;no;false;strict;relaxed", // InitValue
																	"yes", // DefaultValue
																	false // isMandatory
															);
	public SOSOptionStringValueList	TransferZeroByteFiles	= (SOSOptionStringValueList) zero_byte_transfer.SetAlias("transfer_zero_byte_files");

	/**
	 * \brief getzero_byte_transfer
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionStringValueList getzero_byte_transfer() {
		return zero_byte_transfer;
	}

	/**
	 * \brief setzero_byte_transfer
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param p_zero_byte_transfer
	 */
	@Override public void setzero_byte_transfer(final SOSOptionStringValueList p_zero_byte_transfer) {
		zero_byte_transfer = p_zero_byte_transfer;
	}

	public SOSFtpOptionsSuperClass() {
		// super("SOSVirtualFileSystem");
		objParentClass = this.getClass();
	} // public SOSFtpOptionsSuperClass

	public SOSFtpOptionsSuperClass(final JSListener pobjListener) {
		this();
		this.registerMessageListener(pobjListener);
	} // public SOSFtpOptionsSuperClass

	//
	public SOSFtpOptionsSuperClass(final HashMap<String, String> JSSettings) throws Exception {
		this();
		this.setAllOptions(JSSettings);
	} // public SOSFtpOptionsSuperClass (HashMap JSSettings)

	//
	/**
	* \brief getAllOptionsAsString - liefert die Werte und Beschreibung aller
	* Optionen als String
	*
	* \details
	*
	* \see toString \see toOut
	*/
	@SuppressWarnings("unused") private String getAllOptionsAsString() {
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

	public void setAllOptions(final Properties pobjProperties) {
		HashMap<String, String> map = new HashMap<String, String>((Map) pobjProperties);
		try {
			super.setAllOptions(map);
		}
		catch (Exception e) {
			logger.error(e.getLocalizedMessage());
		}
	}

	/**
	 * \brief setAllOptions
	 *
	 * \details
	 *
	 * \return
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

	//
	/**
	 * \brief CheckMandatory
	 *
	 * \details
	 *
	 * \return
	 *
	 * @throws JSExceptionMandatoryOptionMissing
	 */
	@Override public void CheckMandatory() throws com.sos.JSHelper.Exceptions.JSExceptionMandatoryOptionMissing //
	{
		try {
			super.CheckMandatory();
		}
		catch (Exception e) {
			throw new JSExceptionMandatoryOptionMissing(e.toString());
		}
	} // public void CheckMandatory ()

	//
	/**
	 * \brief CommandLineArgs
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param pstrArgs
	 * @throws Exception
	 */
	@Override public void CommandLineArgs(final String[] pstrArgs) {
		super.CommandLineArgs(pstrArgs);
		this.setAllOptions(super.objSettings);
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
	@Override public SOSOptionHostName getHost() {
		return host;
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
	@Override public SOSOptionPortNumber getPort() {
		return port;
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
	@Override public SOSOptionString getProxy_host() {
		return null;
		// return proxy_host;
	}

	/**
	 * \brief getProxy_password
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionPassword getProxy_password() {
		// TODO Auto-generated method stub
		return null;
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
	@Override public SOSOptionPortNumber getProxy_port() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * \brief getProxy_user
	 *
	 * \details
	 *
	 * \return
	 *
	 * @return
	 */
	@Override public SOSOptionUserName getProxy_user() {
		// TODO Auto-generated method stub
		return null;
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
	@Override public void setHost(final SOSOptionHostName host) {
		this.sethost(host);
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
	@Override public void setPort(final SOSOptionPortNumber port) {
		// TODO Auto-generated method stub
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
	@Override public void setProxy_host(final SOSOptionString proxyHost) {
		// TODO Auto-generated method stub
	}

	/**
	 * \brief setProxy_password
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param proxyPassword
	 */
	@Override public void setProxy_password(final SOSOptionPassword proxyPassword) {
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
	@Override public void setProxy_port(final SOSOptionPortNumber proxyPort) {
	}

	/**
	 * \brief setProxy_user
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param proxyUser
	 */
	@Override public void setProxy_user(final SOSOptionUserName proxyUser) {
		// TODO Auto-generated method stub
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
	@Override public SOSOptionInFileName getAuth_file() {
		return ssh_auth_file;
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
	@Override public SOSOptionAuthenticationMethod getAuth_method() {
		return ssh_auth_method;
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
	@Override public SOSOptionPassword getPassword() {
		return password;
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
	@Override public SOSOptionUserName getUser() {
		return user;
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
	@Override public void setAuth_file(final SOSOptionInFileName authFile) {
		ssh_auth_file = authFile;
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
	@Override public void setAuth_method(final SOSOptionAuthenticationMethod authMethod) {
		// TODO Auto-generated method stub
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
	@Override public void setPassword(final SOSOptionPassword password) {
		// TODO Auto-generated method stub
	}

	/**
	 * \brief setUser
	 *
	 * \details
	 *
	 * \return
	 *
	 * @param pobjUser
	 */
	@Override public void setUser(final SOSOptionUserName pobjUser) {
		// TODO Auto-generated method stub
		user.Value(pobjUser.Value());
	}

	@Override public SOSOptionRegExp getfile_spec2() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override public void setfile_spec2(final SOSOptionRegExp p_file_spec2) {
		// TODO Auto-generated method stub
	}

	@Override public SOSOptionFolderName SourceDir() {
		return SourceDir;
	}

	@Override public SOSOptionFolderName TargetDir() {
		return TargetDir;
	}

  @JSOptionDefinition(name = "raise_exception_on_error", description = "Raise an Exception if an error occured", key = "raise_exception_on_error", type = "SOSOptionBoolean", mandatory = true)
  public SOSOptionBoolean   raise_exception_on_error  = new SOSOptionBoolean( // ...
                                this, // ....
                                conClassName + ".raise_exception_on_error", // ...
                                "Raise an Exception if an error occured", // ...
                                "true", // ...
                                "true", // ...
                                true);
  

  public SOSOptionBoolean getraise_exception_on_error() {
    return raise_exception_on_error;
  } 

  public void setraise_exception_on_error(final SOSOptionBoolean raiseExceptionOnError) {
    this.raise_exception_on_error = raiseExceptionOnError;
  } 

} // public class SOSFtpOptionsSuperClass
//
