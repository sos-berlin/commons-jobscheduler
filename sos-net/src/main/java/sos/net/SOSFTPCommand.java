package sos.net;

import com.sos.JSHelper.Basics.JSVersionInfo;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
import com.sos.JSHelper.Logging.Log4JHelper;
import com.sos.JSHelper.System.SOSCommandline;
import com.trilead.ssh2.*;
import org.apache.log4j.Logger;
import sos.configuration.SOSConfiguration;
import sos.configuration.SOSConfigurationItem;
import sos.util.*;

import java.io.*;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.*;
import java.util.Map.Entry;

/**
 *
 * Send/Receive files by FTP/FTPS/SFTP and execute commands by SSH
 *
 * This program is used as a standalone, synchroneous File Transfer solution.
 * For asynchroneous file transfer see the respective standard jobs with the Job Scheduler.
 *
 * File Transfer Features
 *
 *  - Send and receive files by FTP to/from some target host.
 *  - Send and receive files by SFTP to/from some target host.
 *  - Execute commands by SSH on some host.
 *  - Send files by FTP or SFTP to a "jump host" and forward them by FTP or SFTP to a target host.
 *    Different transfer protocols can be used between localhost and "jump_host" and between "jump_host" and target host.
 *  - Receive files from a remote host by FTP or SFTP to a "jump host" and forward them by FTP or SFTP to the local host.
 *    Different protocols can be used for transfer between the hosts.
 *  - Both password and publickey authentication are supported for SFTP.
 *  - The parameterization is effected by command line parameters and by configuration files.
 *  - All parameters are specified on the localhost exclusively, this applies in the same way when using
 *    a "jump host" as local parameters are dynamically forwarded to the "jump host".
 *  - Logging and error handling are provided, errors are detected on a per file basis.
 *
 *  - Security: no configuration files are used on the "jump host" (except for private key files that were used to access a target host);
 *              no passwords are stored on the "jump host"; no use is made of system proxy functionalities.
 *
 * @author andreas.pueschel@sos-berlin.com
 * @author mueruevet.oeksuez@sos-berlin.com
 * @version 2009-09-22
 * @see ./doc/sosftp.xml
 *
 */

abstract public class SOSFTPCommand {

	private static final String	conParamValueSUCCESS	= "success";
	private static final String	conParameterSTATUS	= "status";
	private static final String		conOperationDELETE						= "delete";
	public static final String		conParamPORT							= "port";
	public static final String		conParamPROTOCOL						= "protocol";
	private static Logger			objLogger								= Logger.getLogger(SOSFTPCommand.class);

	public static final String		conParamSSH_AUTH_METHOD					= "ssh_auth_method";
	public static final String		conParamPASSWORD						= "password";
	public static final String		conParamUSER							= "user";
	public static final String		conParamHOST							= "host";
	public static final String		conParamJUMP_HOST						= "jump_host";
	public static final String		conParamSSH_AUTH_FILE					= "ssh_auth_file";
	public static final String		conParamJUMP_PROTOCOL					= "jump_protocol";
	public static final String		conSettingREMOVE_FILES					= "remove_files";
	private static final String		conSettingsJUMP_COMMAND					= "jump_command";
	public static final String		conParameterREMOVE_AFTER_JUMP_TRANSFER	= "remove_after_jump_transfer";
	protected static final String	conParamFORCE_FILES						= "force_files";

	public static final String		conParamREPLACEMENT						= "replacement";
	public static final String		conParamREPLACING						= "replacing";
	private static final String		conParamMAKE_DIRS						= "make_dirs";
	private static final String		conJobChainNameSCHEDULER_SOSFTP_HISTORY	= "scheduler_sosftp_history";
	private static final String		conParamSCHEDULER_JOB_CHAIN				= "scheduler_job_chain";
	private static final String		conParamSCHEDULER_PORT					= "scheduler_port";
	private static final String		conParamSCHEDULER_HOST					= "scheduler_host";
	 public static final String FILE_MODIFICATION_DATE = "FILE_MODIFICATION_DATE";
	/**
	 * Params for the Background Service ....
	 */
	private static final String		conParamSCHEDULER_MESSAGE				= "scheduler_message";
	private static final String		conOperationRECEIVE						= "receive";
	private static final String		conOperationSEND						= "send";
	public static final String		conOperationEXECUTE						= "execute";
	public static final String		conOperationREMOVE						= "remove";
	public static final String		conSettingOPERATION						= "operation";
	public static final String		conSettingLOCAL_DIR						= "local_dir";
	public static final String		conSettingFILE_PATH						= "file_path";
	public static final String		conSettingFILE_SPEC						= "file_spec";
	public static final String		USE_PATH_AND_FILE_NAME_4_MATCHING		= "use_path_and_file_name_4_matching";
	public static final String		CHECK_SERVER_FEATURES					= "check_server_features";
	public static final String		CONVERT_UMLAUTE							= "convert_umlaute";
	public static final String		FILENAME_ENCODING						= "Filename_encoding";
	public static final String		CONTROL_ENCODING						= "control_encoding";
	public static final String		PRE_FTP_COMMANDS						= "pre_ftp_commands";
	public static final String		conStatusERROR							= "error";
	public static final String		conStatusSUCCESS						= conParamValueSUCCESS;
	public static final String		conSettingSTATUS						= conParameterSTATUS;
	public static final String		conSettingREMOTE_DIR					= "remote_dir";
	public static final String		conSettingTRANSFER_MODE					= "transfer_mode";
	public static final String		conSettingFILE_SIZE						= "file_size";

	private static String			conClassName							= "SOSFTPCommand";

	protected static final String	conNewLine								= "\n";
	protected static final String	conRegExpBackslash						= "\\\\";
	protected static final String	conRegExpAllChars						= ".*";

	protected SOSFTP				sosftp									= null;
	protected String				strPreFtpCommands						= "";
	protected boolean				flgCheckServerFeatures					= false;
	protected boolean				flgUsePathAndFileName4Matching			= false;

	protected String				strControlEncoding						= "";
	protected String				strFileNameEncoding						= "";
	protected boolean				flgConvertUmlaute						= false;

	/** Regular Expression for filename replacement with replacement . */
	protected String				replacing								= null;
	protected String				replacement								= null;

	/** The FTP server will always reply the ftp error codes,
	 * see http://www.the-eggman.com/seminars/ftp_error_codes.html */
	public static final int			ERROR_CODE								= 300;

	/** default command delimiter */
	protected final static String	DEFAULT_COMMAND_DELIMITER				= "%%";

	/** sos.util.SOSString Object */
	protected static SOSString		sosString								= new SOSString();

	/** sos.util.SOSLogger Object */
	private static SOSLogger		logger									= null;

	/** optional mail configuration */
	protected String				mailSMTP								= "localhost";
	protected String				mailPortNumber							= "25";
	protected String				mailFrom								= "SOSFTP";
	protected String				mailQueueDir							= "";

	/** array of commands that have been separated by the commandDelimiter */
	protected String[]				commands								= {};

	/** ignore errors reported by the exit status of commands */
	protected boolean				ignoreError								= false;

	/** ignore signals terminating remote execution */
	protected boolean				ignoreSignal							= false;

	/** ignore output to stderr */
	protected boolean				ignoreStderr							= false;

	/** timestamp of the last text from stdin or stderr **/
	protected long					lasttime								= 0;

	/** regular expression for delimiter of multiple commands specified as job or order parameter */
	protected String				commandDelimiter						= DEFAULT_COMMAND_DELIMITER;

	/** ssh connection object */
	private Connection				sshConnection							= null;

	/** ssh session object */
	protected Session				sshSession								= null;

	/** Inputstreams for stdout and stderr **/
	protected InputStream			stdout									= null;
	protected InputStream			stderr									= null;

	/** Script for a command which will be submitted and then executed **/
	protected String				commandScript							= "";

	/** Line currently being displayed on the shell **/
	protected String				currentLine								= "";

	/** remote host name or ip address */
	protected String				host									= "";

	/** remote ssh2 port */
	// protected int port = 0;
	protected int					port									= 21;																																																														// JS-649

	/** user name on remote host */
	protected String				user									= "";

	/** for publickey authentication this password secures the authentication file, for password authentication this is the password */
	protected String				password								= "";

	/** optional proxy configuration */
	protected String				proxyHost								= "";
	protected int					proxyPort								= 0;
	protected String				proxyUser								= "";
	protected String				proxyPassword							= "";

	/** key file: ~/.ssh/id_rsa or ~/.ssh/id_dsa */
	protected String				authenticationFilename					= "";

	/** authentication method: publickey, password */
	protected String				authenticationMethod					= "publickey";

	/** Program Arguments */
	protected Properties			arguments								= null;
	protected Properties			savedArguments							= null;
	/** Hilfsvariable. Hier werden alle ursprüngliche/unveränderte Parametern zum Historienschreiben geschrieben.
	 * Es geht mehr um die Jump Parameter*/
	private static Properties		originalParam							= null;

	/** hilfgsvariable, gibt an ob ein jump Host existiert */
	protected boolean				flgJumpTransferDefined					= false;

	/** temporary Directory of jump host:
	 * Security: no configuration files are used on the "jump host"
	 * (except for private key files that were used to access a target host);
	 * no passwords are stored on the "jump host"; no use is made of system proxy functionalities.
	 */
	protected static String			tempJumpRemoteDir						= "";

	/**
	 * The value of this parameter specifies an post command that is executed by the remote host.
	 */
	protected String				postCommands							= "";

	/**
	 * Anlegen einer temporären Historien Datei, die anschliessend in der globalen Historie Datei (siehe historyFile)
	 * angehängt wird
	 */
	private static File				tempHistoryFile							= null;

	/**
	 * Diese Parameter gibt die globale Historien Dateinamen an.
	 */
	private static File				historyFile								= null;

	/**
	 *  java.io.BufferedWriter Objekt for history
	 */
	private static BufferedWriter	history									= null;

	/**
	 * Angabe einer Default Mandatory, wenn keine Parameter mandator angegeben wurde.
	 * Der Mandator wird in die Historie geschrieben
	 */
	private final String			defaultMandator							= "SOS";

	/**
	 * Felder der Historiendatei
	 */
	private String					historyFields							= "guid;mandator;transfer_timestamp;pid;ppid;operation;localhost;localhost_ip;local_user;remote_host;remote_host_ip;remote_user;protocol;port;local_dir;remote_dir;local_filename;remote_filename;file_size;md5;status;last_error_message;log_filename";

	/**
	 * neue Felder der Historiendatei. Der Aufbau ist wie folgt: historyFields;<history_entry_>;newHistoryFields
	 */
	private String					newHistoryFields						= "jump_host;jump_host_ip;jump_port;jump_protocol;jump_user";

	/**
	 * Alle Parameter, die mit history_entry_ anfangen werden mit in historie geschrieben.
	 */
	@SuppressWarnings("rawtypes")
	private static ArrayList		historyEntrys							= new ArrayList();

	/**
	 * hilfsbariable, damit der banner header einmal geschrieben wird
	 */
	private boolean					writeBannerHeader						= false;

	/**Es wird eine scheduler Signal geschickt wenn ein Parameter scheduler_host, scheduler_port, scheduler_messages angegeben wurde */
	private boolean					sendSchedulerSignale					= false;

	/**Account for authorization at the FTP server */
	protected String				account									= "";

	/** sos.net.SOSFileTransfer */
	protected SOSFileTransfer		ftpClient								= null;

	/** check is ftp Connect*/
	protected boolean				isLoggedIn								= false;

	/** Default Bannerheader*/
	protected String				bannerHeader							=

																			"\n*************************************************************************"
																					+ "\n*                                                                       *"
																					+ "\n*                SOSFTP - Managed File Transfer Utility                 *"
																					+ "\n*                --------------------------------------                 *"
																					+ "\n*                                                                       *"
																					+ "\n*************************************************************************"
																					+ "\nversion              = %{version}"
																					+ "\ndate                 = %{date} %{time}"
																					+ "\noperation            = %{operation}"
																					+ "\nprotocol             = %{protocol}"
																					+ "\nport                 = %{port}"
																					+ "\nfile specification   = %{file_spec}"
																					+ "\nfile path            = %{file_path}"
																					+ "\nsource host          = %{localhost} (%{local_host_ip})"
																					+ "\nlocal directory      = %{local_dir}"
																					+ "\njump host            = %{jump_host}"
																					+ "\ntarget host          = %{host} (%{remote_host_ip})"
																					+ "\ntarget directory     = %{remote_dir}"
																					+ "\npid                  = %{current_pid}"
																					+ "\nppid                 = %{ppid}"
																					+ "\n*************************************************************************";

	/** Default Banner Footer*/
	protected String				bannerFooter							= "\n*************************************************************************"
																					+ "\n execution status     = %{status}"
																					+ "\n successful transfers = %{successful_transfers}"
																					+ "\n failed transfers     = %{failed_transfers}"
																					+ "\n last error           = %{last_error}"
																					+ "\n*************************************************************************";

	protected boolean				utf8Supported							= false;
	protected boolean				restSupported							= false;
	protected boolean				mlsdSupported							= false;
	protected boolean				modezSupported							= false;
	protected boolean				dataChannelEncrypted					= false;
	// /** 1. Stelle: ist die Major Version. wir erhöhen Sie nur nach Absprache */
	// private static int major = 1;
	// /** 2. Stelle: ist die Minor Version: wir erhöhen Sie nur nach Absprache */
	// private static int minor = 0;
	// /**3. Stelle: ist die Bug Fix Nummer: Sie erhöhen Sie automatisch bei jeder Auslieferung
	// * 1 = 23.01.2009
	// * a) Erstellen der Versionsnummer;
	// * b) Jeweils beim polling interval soll die Meldung ausgegeben werden.
	// * c) -skip_transfer: falsche Ausgabe im Protokoll
	// *
	// * 2 = 26.01.2009
	// * a) File Transfer Transactions -> transaktionsabhängige übertragung, d.h. löschen oder umbennen der transferierten dateien (atomic
	// suffx)
	// * erfolgt erst dann, wenn alle Dateien transferiert wurden.
	// *
	// * 3 = 11.02.2009
	// * a) Different file operations within one session.
	// *
	// * 4 = 12.02.2009
	// * a) Substitution von Environment-Variablen in Konfigurationsdateien
	// *
	// * 5 = 19.02.2009
	// * a)-file_spec2=.*\.xml$::parameter_set_2 sollen jetzt mit doppelpunkt
	// * b) Korrektur von Polling
	// *
	// * 6 = 20.02.2009
	// *
	// * a) schedulerMessages -> die mandantenspezifische Parameter werden an scheduler_meaages ohne den Präfix
	// * history_entry_ übergeben
	// *
	// * 7 = 24.02.2009
	// *
	// * a) Die Integration eines neuen Mechanismus, um Kennwörter via Shell Script dynamisch einzulesen.
	// *
	// * 8 = 09.03.2009
	// *
	// * a) Erweiterung für 3: unterschiedliche Parametrisierun mit unterschiedliche operation (z.B. einmal mit send und einmal mit receive)
	// *
	// * 9 = 12.03.2009
	// *
	// * a) neue Parameter: -transfer_success=parameter_set_1 und -transfer_error=parameter_set_1; Beim erfolgreichen transfer soll
	// transfer_success
	// * durchgeführt werden im Fehlerfall soll transfer_error durchgeführt werden
	// *
	// * 10 = 17.04.2009
	// *
	// * a) operation = install -> neues Unterordner "doc" für das Installieren der Unterverzeichnis;
	// * b) operation = install -> neue Dateien readme.txt und ThirdParty.txt
	// * c) Realisierung der Gross und Kleinschreiben der transferierten Dateinamen: Parameter -replacement=[filename:uppercase] bzw.
	// -replacement=[filename:lowercase]
	// *
	// * 11 = 28.07.2009
	// *
	// * a) operation = send -> Parameter poll_interval und poll_timeout werden jetzt auch für operation=send verwendet
	// * b) neue Parameter: testmode=yes|no
	// *
	// * 12 = 04.09.2009
	// * a) neue History Felder jump_host;jump_host_ip;jump_port;jump_protocol;jump_user
	// * b) file_spec_num -> Abarbeitung nach Reigenfolge
	// *
	// * 13 = 16.09.2009
	// * a) JIRA-6: jump host: remove_files/delete don't work
	// *
	// * 14 = 24.09.3009
	// * a) JIRA-5 history lock file
	// *
	// * 15 = 29.09.3009
	// * a) JIRA-5 history lock file funktioniert jetzt auch unter windows
	// *
	// * 16 = 27.10.3009
	// * a) JIRA-5 getParentId.exe wird jetzt verwendet
	// *
	// * 17 = 19.11.2009
	// * a) mergen von Scheduler Parameter
	// *
	// * 18 = 19.11.2009
	// * a) parameter simulate_shell ... löschen
	// *
	// * 18 -> 19 in sf 05.01.2010
	// * a) Parameter include funktioniert nicht
	// *
	// * 19 = 15.12.2009
	// * a) Redesign auf Configuration/Parametern
	// *
	// * 20 = 29.01.2010
	// * a) SOSFTPCommand so anpassen, das es auch im SchedulerBetrieb arbeitet
	// * sos.scheduler.ftp.SOSSchedulerFTPSend.java
	// * sos.scheduler.ftp.SOSSchedulerFTPReceive.java
	// * sos.scheduler.job.JobSchedulerSSHJob.java
	// * 21 = 10.02.2010
	// * a)Anpassung der operation=install
	// * -> wegen Redesign sind 4 neue Bibliotheken hinzugekommen: sos.xml.jar; xercesImpl.jar; xml-apis.jar; sos.connection.jar;
	// * -> diese Bibliotheken werden bei der operation=install mitgeliefert.
	// *
	// * 22 = 29.03.2009
	// * a) die Bibliotheken haben jetzt revisionsnummer und datum. operation=install muss angepasst werden
	// * b) neue Bibiliothek com.sos.configuration.jar
	// *
	// *
	// */
	// private static int bugFix = 22;

	@SuppressWarnings("rawtypes")
	protected static ArrayList		transactionalHistoryFile				= null;

	protected ArrayList				transactionalSchedulerRequestFile		= null;

	/**
	 * All files should appear in the target system as one atomic transaction.
	 * Approach: Transfer all files using the –atomic_transfer parameter that
	 * add a suffix to the file names. Then, having transferred all files
	 * successfully, they would be renamed. This is approx.
	 * the same as the current operation for atomic transfers, however,
	 * the renaming should be done for all files and not on a per file basis.
	 *
	 * This feature should consider the –remove_files parameter,
	 * i.e. files should be removed after successfule transfer of all files,
	 * not on a per file basis
	 */
	protected static boolean		transActional							= false;

	/**
	 * Parameter die nicht an den jump Host übergeben werden dürfen
	 */
	private static ArrayList		noJumpParameter							= new ArrayList();

	/**
	 *  Substitution von Environment-Variablen in Konfigurationsdateien
	 */
	private static Properties		environment								= null;

	/**
	 * -transfer_success=parameter_set_1 und -transfer_error=parameter_set_1; Beim erfolgreichen transfer soll transfer_success
	 * durchgeführt werden im Fehlerfall soll transfer_error durchgeführt werden.
	 * Die Argumente werden hier im listen gesammelt um im Fehlerfall bzw. Erfolgsplan alles auszuführen.
	 */
	protected static ArrayList		listOfSuccessTransfer					= null;
	protected static ArrayList		listOfErrorTransfer						= null;

	private static SOSFTPCommand	ftpCommand								= null;

	protected static boolean		banner									= true;
	/** */
	private File					lockHistoryFile							= null;

	/**
	 * Falls der SOSFTP im scheduler umgebung betrieben wird, werden alle Scheduler parameter aus der Umgebungsvariable
	 * SCHEDULER_PARAMS_<parameter> ausgelesen und den sosftp als Argument übergeben
	 */
	private static Properties		schedulerParams							= new Properties();

	private static SOSConfiguration	sosConfiguration						= null;

	// Liste der transferierten Dateien
	protected Vector<File>			transferFileList						= new Vector<File>();

	public static Vector<String>	filelist								= null;
	protected Vector<String>		filelisttmp								= null;
	protected static Object			schedulerJob							= null;

	protected static String			SSHJOBNAME								= "sos.scheduler.job.JobSchedulerSSHJob";

	// Does not work with includes. ur 20101117
	// public static String REQUIRED_DEFAULT_PARAMETERS_FILENAME = "sos/net/sosftp/Configuration.xml";
	// public static String REQUIRED_DEFAULT_PARAMETERS_FILENAME = "sos/net/sosftp/Configuration_norequire.xml";
	// private static String REQUIRED_DEFAULT_PARAMETERS_FILENAME_NOREQUIRE = "sos/net/sosftp/Configuration_norequire.xml";

	private static Integer			exitStatus								= null;

	/**
	 * Constructor
	 * @param logger
	 * @param arguments_
	 */
	public SOSFTPCommand(final SOSLogger logger, final Properties arguments_) throws Exception {
		this.setLogger(logger);
		arguments = arguments_;
		arguments.put("version", getVersion());
		try {
			String pid = getPids();
			if (sosString.parseToString(pid).length() > 0)
				arguments.put("current_pid", pid);

		}
		catch (Exception e) {
			try {
				// getLogger().info("could not get PID, cause: " + e.toString());
			}
			catch (Exception ex) {
			}
			// RaiseException("could not get PID, cause: " + e.toString());
		}
	}

	public SOSFTPCommand(final SOSConfiguration sosConfiguration_, final SOSLogger logger) throws Exception {
		this.setLogger(logger);
		sosConfiguration = sosConfiguration_;
		arguments = sosConfiguration.getParameterAsProperties();
		arguments.put("version", getVersion());
		try {
			String pid = getPids();
			if (sosString.parseToString(pid).length() > 0)
				arguments.put("current_pid", pid);

		}
		catch (Exception e) {
			try {
				// getLogger().info("could not get PID, cause: " + e.toString());

			}
			catch (Exception ex) {
			}
			// RaiseException("could not get PID, cause: " + e.toString());

		}
	}

	public void setSchedulerJob(final Object schedulerJob_) {
		schedulerJob = schedulerJob_;

	}

	/**
	 * Receive files by FTP/SFTP from a remote server
	 *
	 * @return boolean
	 * @throws Exception
	 */
	protected boolean receive() throws Exception {
		return false;
	}

	/**
	 * Sends files by FTP/SFTP to a remote server
	 *
	 * @return boolean
	 * @throws Exception
	 */
	protected boolean send() throws Exception {
		return false;
	}

	/**
	 * Execute a command by SSH on a remote server
	 *
	 * @return boolean
	 * @throws Exception
	 */
	public boolean execute() throws Exception {
		StringBuffer stderrOutput = new StringBuffer();
		String commandScriptFileName = "";

		try {
			getLogger().debug1("calling " + sos.util.SOSClassUtil.getMethodName());

			try { // to fetch parameters, order parameters have precedence to job parameters
				if (this.getLogger() == null)
					this.setLogger(new SOSStandardLogger(0));

				readSettings(false);

				// get basic authentication parameters
				this.getBaseParameters();

				if (!flgJumpTransferDefined) {
					if (getString("jump_command_delimiter").length() > 0) {
						this.setCommandDelimiter(getString("jump_command_delimiter"));
						getLogger().debug1(".. parameter [jump_command_delimiter]: " + this.getCommandDelimiter());
					}
					else {
						this.setCommandDelimiter(DEFAULT_COMMAND_DELIMITER);
					}

					if (getString("jump_command_script").length() > 0) {
						commandScript = getString("jump_command_script");
						getLogger().debug1(".. parameter [jump_command_script]: " + commandScript);
					}

					if (getString("jump_command_script_file").length() > 0) {
						commandScriptFileName = getString("jump_command_script_file");
						getLogger().debug1(".. parameter [jump_command_script_file]: " + commandScriptFileName);
					}

					if (arguments.containsKey("xx_make_temp_directory_success_transfer_xx")) {
						if (getString("command").length() > 0) {
							this.setCommands(getString("command").split(this.getCommandDelimiter()));
							getLogger().debug1(".. parameter [jump_command]: " + getString("command"));
						}
					}
					else
						if (!arguments.contains("xx_make_temp_directory_xx")) {

							if (getString(conSettingsJUMP_COMMAND).length() > 0) {
								this.setCommands(getString(conSettingsJUMP_COMMAND).split(this.getCommandDelimiter()));
								getLogger().debug1(".. parameter [jump_command]: " + getString(conSettingsJUMP_COMMAND));
							}
							else
								if (commandScript.length() == 0 && commandScriptFileName.length() == 0) {
									RaiseException("no command (or jump_command_script or jump_command_script_file) has been specified for parameter [jump_command]");
								}
						}
				}

				if (getString("jump_ignore_error").length() > 0) {
					if (getBool("jump_ignore_error")) {
						ignoreError = true;
					}
					else {
						ignoreError = false;
					}
					getLogger().debug1(".. parameter [jump_ignore_error]: " + ignoreError);
				}
				else {
					ignoreError = false;
				}

				if (getString("jump_ignore_signal").length() > 0) {
					if (sosString.parseToBoolean(getString("jump_ignore_signal"))) {
						ignoreSignal = true;
					}
					else {
						ignoreSignal = false;
					}
					getLogger().debug1(".. parameter [jump_ignore_signal]: " + ignoreSignal);
				}
				else {
					ignoreSignal = false;
				}

				// TODO
				if (getString("jump_ignore_stderr").length() > 0) {
					if (sosString.parseToBoolean(getString("jump_ignore_stderr"))) {
						ignoreStderr = true;
					}
					else {
						ignoreStderr = false;
					}
					getLogger().debug1(".. parameter [jump_ignore_stderr]: " + ignoreStderr);
				}
				else {
					ignoreStderr = false;
				}

			}
			catch (Exception e) {
				RaiseException("error occurred processing parameters: " + e.getMessage());
			}
			RemoteConsumer stdoutConsumer = null;
			RemoteConsumer stderrConsumer = null;

			try { // to connect, authenticate and execute commands
				this.getBaseAuthentication();
				boolean windows = remoteIsWindowsShell();
				String remoteCommandScriptFileName = "";
				if (commandScript.length() > 0 || commandScriptFileName.length() > 0) {

					File commandScriptFile = null;
					if (commandScript.length() > 0) {
						commandScriptFile = createCommandScript(windows);
					}
					else {
						commandScriptFile = createCommandScript(new File(commandScriptFileName), windows);
					}
					transferCommandScript(commandScriptFile, windows);
					remoteCommandScriptFileName = commandScriptFile.getName();
					// change commands to execute transferred file

					commands = new String[1];
					if (windows) {
						commands[0] = commandScriptFile.getName();
					}
					else {
						commands[0] = "./" + commandScriptFile.getName();
					}
					// delete local file
					if (commandScript.length() > 0) {
						commandScriptFile.delete();
					}
				}

				Class cl_ = null;
				// execute commands
				for (int i = 0; i < this.getCommands().length; i++) {
					try {
						exitStatus = null;
						String exitSignal = null;

						if (sosString.parseToString(getCommands()[i]).trim().length() == 0)
							continue;

						getLogger().debug1("executing remote command: " + normalizedPassword(this.getCommands()[i]));

						this.setSshSession(this.getSshConnection().openSession());

						String currentCommand = this.getCommands()[i];

						if (!windows && schedulerJob != null && Class.forName(SSHJOBNAME) != null) {
							currentCommand = "echo $$ && " + currentCommand;
							cl_ = Class.forName(SSHJOBNAME);
							if (schedulerJob.getClass().getName().equals(cl_.getName())) {
								Method method = cl_.getMethod("initKillJob", new Class[] {});
								method.invoke(schedulerJob, new Class[] {});
							}
						}

						this.getSshSession().execCommand(currentCommand);

						getLogger().debug("output to stdout for remote command: " + normalizedPassword(this.getCommands()[i]));
						stdout = new StreamGobbler(this.getSshSession().getStdout());
						stderr = new StreamGobbler(this.getSshSession().getStderr());
						BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(stdout));
						int pid = 0;
						while (true) {
							String line = stdoutReader.readLine();
							if (line == null)
								break;
							if (!windows && pid == 0 && schedulerJob != null && Class.forName(SSHJOBNAME) != null) {
								if (schedulerJob.getClass().getName().equals(cl_.getName())) {
									pid = Integer.parseInt(line);
									if (cl_ != null) {

										Method method = cl_.getMethod("setSchedulerSSHKillPid", new Class[] { int.class });
										method.invoke(schedulerJob, new Object[] { new Integer(pid) });

									}
									getLogger().debug5("Parent pid: " + pid);
									continue;
								}
							}

							if (getString(conSettingOPERATION).equalsIgnoreCase(conOperationEXECUTE) && banner)
								getLogger().info(line);
							else
								if (getString(conSettingOPERATION).equalsIgnoreCase(conOperationRECEIVE)
										&& sosString.parseToString(line).indexOf("files found.") > -1 && sosString.parseToBoolean(getString("skip_transfer")))
									getLogger().info(line.substring(line.indexOf("[info]") + "[info]".length()));
								else
									if (getString(conSettingOPERATION).equalsIgnoreCase(conOperationRECEIVE)
											&& sosString.parseToString(line).indexOf("Processing file") > -1
											&& sosString.parseToBoolean(getString("skip_transfer"))) {
										String s = line.substring(line.indexOf("Processing file") + "Processing file".length() + 1);

										if (filelisttmp == null)
											filelisttmp = new Vector<String>();
										filelisttmp.add(s);
									}
									else
										if (line.indexOf("[warn]") > -1 || line.indexOf("[error]") > -1) {
											getLogger().warn("remote execution reports error : " + line);

										}
										else {
											getLogger().debug1(line);
										}
							// Die Schreibweise bitte nicht verändern, da hier String vergleiche gemacht werden
							if (line.indexOf("*******ftp transfer directory is:") > -1) {
								int pos1 = line.indexOf("*******ftp transfer directory is:") + "*******ftp transfer directory is:".length();
								int pos2 = line.indexOf("******************");
								tempJumpRemoteDir = line.substring(pos1, pos2);
							}

							if (line.indexOf("[info]") > -1 && line.indexOf("files found.") > -1 && getBool("testmode")
									&& getString(conSettingOPERATION).equals(conOperationRECEIVE)) {
								getLogger().info(line.substring(line.indexOf("[info]") + "[info]".length()));
							}

							if (line.indexOf("[info]") > -1
									&& (line.indexOf("removing remote file:") > -1 || line.indexOf("files remove.") > -1)
									&& (getString(conSettingOPERATION).equals(conOperationREMOVE) || getString(conSettingOPERATION).equals(conOperationRECEIVE))) {
								getLogger().info(line.substring(line.indexOf("[info]") + "[info]".length()));
							}
						}
						if (!windows && schedulerJob != null) {
							if (cl_ != null && Class.forName(SSHJOBNAME) != null) {
								if (schedulerJob.getClass().getName().equals(cl_.getName())) {
									Method method = cl_.getMethod("setSchedulerSSHKillPid", new Class[] { int.class });
									method.invoke(schedulerJob, new Object[] { new Integer(0) });
								}
							}
						}

						getLogger().debug1("output to stderr for remote command: " + normalizedPassword(this.getCommands()[i]));
						// Beide StreamGobbler müssen hintereinander instanziiert werden
						BufferedReader stderrReader = new BufferedReader(new InputStreamReader(stderr));
						stderrOutput = new StringBuffer();
						while (true) {
							String line = stderrReader.readLine();
							if (line == null)
								break;
							getLogger().error(line);
							stderrOutput.append(line + conNewLine);
						}

						if (stderrOutput != null && stderrOutput.length() > 0) {
							if (ignoreStderr) {
								if (flgJumpTransferDefined) {
									// testen
									getLogger().error("output to stderr is ignored: " + stderrOutput);
									return false;
								}
								else {
									getLogger().info("output to stderr is ignored: " + stderrOutput);
								}

							}
							else {
								RaiseException("remote execution reports error: " + stderrOutput);
							}
						}

						try {
							exitStatus = this.getSshSession().getExitStatus();
						}
						catch (Exception e) {
							getLogger().debug1("could not retrieve exit status, possibly not supported by remote ssh server");
						}

						if (exitStatus != null) {
							if (!exitStatus.equals(new Integer(0))) {
								if (ignoreError) {
									getLogger().debug1("exit status is ignored: " + exitStatus);
								}
								else {
									RaiseException("remote command terminated with exit status: " + exitStatus
											+ (logger.hasWarnings() ? " error: " + logger.getWarning() : ""));
								}
							}
						}

						try {
							exitSignal = this.getSshSession().getExitSignal();
						}
						catch (Exception e) {
							getLogger().debug1("could not retrieve exit signal, possibly not supported by remote ssh server");
						}

						if (exitSignal != null) {
							if (exitSignal.length() > 0) {
								if (ignoreSignal) {
									getLogger().debug1("exit signal is ignored: " + exitSignal);
								}
								else {
									RaiseException("remote command terminated with exit signal: " + exitSignal);
								}
							}
						}

					}
					catch (Exception e) {
						throw e;
					}
					finally {
						if (remoteCommandScriptFileName != null && remoteCommandScriptFileName.length() > 0) {
							deleteCommandScript(remoteCommandScriptFileName);
						}
						if (this.getSshSession() != null)
							try {
								this.getSshSession().close();
								this.setSshSession(null);
							}
							catch (Exception ex) {
							} // gracefully ignore this error
					}
				}

			}
			catch (Exception e) {

				RaiseException("error occurred processing ssh command: " + e.getMessage());
			}
			finally {
				if (stderrConsumer != null)
					stderrConsumer.end();
				if (stdoutConsumer != null)
					stdoutConsumer.end();
				if (this.getSshConnection() != null)
					try {
						this.getSshConnection().close();
						this.setSshConnection(null);
					}
					catch (Exception ex) {
					} // gracefully ignore this error
			}

			return true;

		}
		catch (Exception e) {
			try {
				getLogger().warn(e.getMessage());
			}
			catch (Exception ex) {
			}
			return false;
		}

	}

	public File transferCommandScript(File commandFile, final boolean isWindows) throws Exception {
		try {
			SFTPv3Client sftpClient = new SFTPv3Client(this.getSshConnection());
			SFTPv3FileAttributes attr = new SFTPv3FileAttributes();
			attr.permissions = new Integer(0700);

			boolean exists = true;
			// check if File already exists
			while (exists) {
				try {
					SFTPv3FileAttributes attribs = sftpClient.stat(commandFile.getName());
				}
				catch (SFTPException e) {
					getLogger().debug9("Error code when checking file existence: " + e.getServerErrorCode());
					exists = false;
				}
				if (exists) {
					getLogger().debug1("file with that name already exists, trying new name...");
					String suffix = isWindows ? ".cmd" : ".sh";
					File resultFile = File.createTempFile("sos", suffix);
					resultFile.delete();
					commandFile.renameTo(resultFile);
					commandFile = resultFile;
				}
			}

			// set execute permissions for owner
			SFTPv3FileHandle fileHandle = sftpClient.createFileTruncate(commandFile.getName(), attr);

			FileInputStream fis = null;
			long offset = 0;
			try {
				fis = new FileInputStream(commandFile);
				byte[] buffer = new byte[1024];
				while (true) {
					int len = fis.read(buffer, 0, buffer.length);
					if (len <= 0)
						break;
					sftpClient.write(fileHandle, offset, buffer, 0, len);
					offset += len;
				}
				fis.close();
				fis = null;

			}
			catch (Exception e) {
				RaiseException("error occurred writing file [" + commandFile.getName() + "]: " + e.getMessage());
			}
			finally {
				if (fis != null)
					try {
						fis.close();
						fis = null;
					}
					catch (Exception ex) {
					} // gracefully ignore this error
			}
			sftpClient.closeFile(fileHandle);

			fileHandle = null;
			sftpClient.close();
		}
		catch (Exception e) {
			RaiseException("Error transferring command script: " + e, e);
		}
		return commandFile;
	}

	public File createCommandScript(final boolean isWindows) throws Exception {
		File resultFile = null; // File.createTempFile("sos", suffix);
		try {

			if (!isWindows)
				commandScript = commandScript.replaceAll("(?m)\r", "");
			String suffix = isWindows ? ".cmd" : ".sh";
			resultFile = File.createTempFile("sos", suffix);
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(resultFile)));
			out.write(commandScript);
			out.close();
		}
		catch (Exception e) {
			RaiseException("Error creating command script: " + e, e);
		}
		return resultFile;
	}

	public File createCommandScript(final File scriptFile, final boolean isWindows) throws Exception {
		try {
			commandScript = sos.util.SOSFile.readFileUnicode(scriptFile);
			return createCommandScript(isWindows);
		}
		catch (Exception e) {
			RaiseException("Error creating command script: " + e, e);
		}
		return null;
	}

	private void deleteCommandScript(final String commandFile) throws Exception {
		try {
			SFTPv3Client sftpClient = new SFTPv3Client(this.getSshConnection());
			sftpClient.rm(commandFile);
			sftpClient.close();
		}
		catch (Exception e) {
			getLogger().warn("Failed to delete remote command script: " + e);
		}
	}

	/**
	 * @return Returns the sshSession.
	 */
	private Session getSshSession() {
		return sshSession;
	}

	/**
	 * @param sshSession1 The sshSession to set.
	 */
	private void setSshSession(final Session sshSession1) {
		sshSession = sshSession1;
	}

	/**
	 * Remove files by FTP/SFTP on a remote server.
	 *
	 * @return boolean
	 * @throws Exception
	 */
	public boolean remove() throws Exception {
		getLogger().debug1("calling " + sos.util.SOSClassUtil.getMethodName());
		// Das Löschen der Dateien per ftp ist identisch wie die Methode receive mit den Parameter
		// skip_trasfer=yes (-> unterdrücken der transferieren der Dateien) und remove_files=yes
		arguments.put("skip_transfer", "yes");
		arguments.put(conSettingREMOVE_FILES, "yes");

		return receive();
	}

	/**
	 * send mail
	 *
	 * @param recipient
	 * @param recipientCC carbon copy recipient
	 * @param recipientBCC blind carbon copy recipient
	 * @param subject
	 * @param body
	 * @throws Exception
	 */
	protected void sendMail(final String recipient, final String recipientCC, final String recipientBCC, final String subject, final String body)
			throws Exception {

		try {
			SOSMail sosMail = new SOSMail(mailSMTP);

			sosMail.setQueueDir(mailQueueDir);
			sosMail.setFrom(mailFrom);
			sosMail.setContentType("text/plain");
			sosMail.setEncoding("Base64");
			sosMail.setPort(mailPortNumber);

			String recipients[] = recipient.split(",");
			for (String recipient2 : recipients) {
				sosMail.addRecipient(recipient2.trim());
			}

			String recipientsCC[] = recipientCC.split(",");
			for (String element : recipientsCC) {
				sosMail.addCC(element.trim());
			}

			String recipientsBCC[] = recipientBCC.split(",");
			for (String element : recipientsBCC) {
				sosMail.addBCC(element.trim());
			}

			sosMail.setSubject(subject);
			sosMail.setBody(body);
			sosMail.setSOSLogger(this.getLogger());

			this.getLogger().debug1("sending mail: \n" + sosMail.dumpMessageAsString());

			if (!sosMail.send()) {
				this.getLogger().warn(
						"mail server is unavailable, mail for recipient [" + recipient + "] is queued in local directory [" + sosMail.getQueueDir() + "]:"
								+ sosMail.getLastError());
			}

			sosMail.clearRecipients();
		}
		catch (Exception e) {
			RaiseException("error occurred sending mai: " + e.getMessage());
		}
	}

	protected String getAlternative(final String param, final String alternativeParam) throws Exception {
		try {
			if (sosString.parseToString(alternativeParam).length() > 0) {
				return alternativeParam;
			}
			else {
				return param;
			}
		}
		catch (Exception e) {
			this.getLogger().warn("error in getAlternative(): " + e.getMessage());
			return param;
		}
	}

	protected int getAlternative(final int param, final int alternativeParam) throws Exception {
		try {
			if (alternativeParam > 0) {
				return alternativeParam;
			}
			else {
				return param;
			}
		}
		catch (Exception e) {
			this.getLogger().warn("error in getAlternative(): " + e.getMessage());
			return param;
		}
	}

	/**
	 * @return Returns the logger.
	 */
	public SOSLogger getLogger() {
		return logger;
	}

	/**
	 * @param logger_ The logger to set.
	 */
	public void setLogger(final SOSLogger logger_) {
		logger = logger_;
	}

	protected URI createURI(final String fileName) throws Exception {
		URI uri = null;
		try {
			uri = new URI(fileName);
		}
		catch (Exception e) {
			try {
				File f = new File(fileName);
				String path = f.getCanonicalPath();
				if (fileName.startsWith("/")) {
					path = fileName;
				}

				String fs = System.getProperty("file.separator");
				if (fs.length() == 1) {
					char sep = fs.charAt(0);
					if (sep != '/')
						path = path.replace(sep, '/');
					if (path.charAt(0) != '/')
						path = '/' + path;
				}
				if (!path.startsWith("file://")) {
					path = "file://" + path;
				}
				uri = new URI(path);
			}
			catch (Exception ex) {
				throw new Exception("error in createURI(): " + e.getMessage());
			}
		}
		return uri;
	}

	protected File createFile(final String fileName) throws Exception {
		try {
			if (fileName == null || fileName.length() == 0) {
				RaiseException("empty file name provided");
			}
			if (fileName.startsWith("file://")) {
				return new File(createURI(fileName));
			}
			else {
				return new File(fileName);
			}
		}
		catch (Exception e) {
			RaiseException("error in createFile() [" + fileName + "]: " + e.getMessage());
		}
		return null;
	}

	protected static String getErrorMessage(final Exception ex) throws Exception {
		String s = "";
		try {
			Throwable tr = ex.getCause();
			if (ex.toString() != null)
				s = ex.toString();
			while (tr != null) {
				if (s.indexOf(tr.toString()) == -1)
					s = (s.length() > 0 ? s + ", " : "") + tr.toString();
				tr = tr.getCause();
			}
		}
		catch (Exception e) {
			throw ex;
		}
		return s;
	}

	/**
	 * @return Returns the commands.
	 */
	protected String[] getCommands() {
		return commands;
	}

	/**
	 * @param commands The commands to set.
	 */
	protected void setCommands(final String[] commands) {
		this.commands = commands;
	}

	/**
	 * Mergen von Programm Argumente und Settings Eistellungen.
	 *
	 * @throws Exception
	 */
	protected void mergeSettings() throws Exception {
		Properties retVal = new Properties();
		try {

			// Umgebungsvariable, die alle mit SOSFTP anfangen
			Properties arg = getEnvVars(logger);
			retVal.putAll(arg);

			// zusätzliche Parametern generieren
			retVal.put("date", sos.util.SOSDate.getCurrentDateAsString());
			retVal.put("time", sos.util.SOSDate.getCurrentTimeAsString("hh:mm:ss"));
			retVal.put("transfer_timestamp", sos.util.SOSDate.getCurrentTimeAsString());
			retVal.put("local_user", sosString.parseToString(System.getProperty("user.name")));

			// zuweisen von Defaults bei leeren Parametern
			if (sosString.parseToString(arg, "current_pid").length() > 0)
				retVal.put("current_pid", sosString.parseToString(arg, "current_pid"));
			if (sosString.parseToString(arg, "ppid").length() > 0)
				retVal.put("ppid", sosString.parseToString(arg, "ppid"));

			try {
				java.net.InetAddress localMachine = java.net.InetAddress.getLocalHost();
				if (sosString.parseToString(localMachine.getHostName()).length() > 0)
					retVal.put("localhost", localMachine.getHostName());
				retVal.put("local_host_ip", localMachine.getHostAddress());
			}
			catch (java.net.UnknownHostException uhe) {
			} // tu nichts

			if (arguments != null)
				retVal.putAll(arguments);

			if (originalParam == null || originalParam.isEmpty())
				originalParam = (Properties) retVal.clone();

			if (savedArguments == null || savedArguments.isEmpty())
				savedArguments = (Properties) retVal.clone();

			logger.debug9(".. saved remote dir: " + savedArguments.getProperty(conSettingREMOTE_DIR));
			arguments = retVal;

			try {
				String host = sosString.parseToString(arguments, conParamHOST);
				if (host.length() > 0)
					arguments.put("remote_host_ip", java.net.InetAddress.getByName(host).getHostAddress());
			}
			catch (java.net.UnknownHostException uhe) {
			}

			if (!writeBannerHeader && !sosString.parseToString(arguments, conSettingOPERATION).startsWith("install")) {
				if (banner) {
					getLogger().info(getBanner(true));
				}
				else {
					getLogger().debug1(getBanner(true));
				}
				writeBannerHeader = true;// header nur einmal schreiben
			}

			// kb 2012-03-28: jump_host in effect only if a jump_host name is defined
			String strJumpHostName = getString(conParamJUMP_HOST);
			if (strJumpHostName.length() > 0) {
				int jumpParameterLength = strJumpHostName.length() + getString("jump_port").length() + getString("jump_user").length();
				if (jumpParameterLength > 0 && getString(conSettingOPERATION).equals(conOperationEXECUTE) == false) {
					flgJumpTransferDefined = true;
				}
			}

			createHistoryFile();
			checkSchedulerRequest();
		}
		catch (Exception e) {
			RaiseException("Failed to merge program arguments and settings: " + e);
		}
	}

	/**
	 *  Substitution von Environment-Variablen in Konfigurationsdateien
	 *
	 *  Für Unix und Windows soll bei allen Einstellungen in einer Konfigurationsdatei
	 *  Substitution von Environment-Variablen unterstützt werden.
	 *  Variablen müssen in der Form %VAR% bzw. ${VAR} geschrieben sein.
	 *
	 */
	private static Properties substituteEnvironment(final Properties prop) throws Exception {
		Properties newProp = new Properties();
		try {
			Iterator it = prop.keySet().iterator();
			String key = "";
			String value = "";
			while (it.hasNext()) {
				key = sosString.parseToString(it.next());
				if (!key.startsWith(";")) {
					value = sosString.parseToString(prop.get(key));
					// if(key.indexOf("password") == -1 || !sosConfiguration.getPasswordnames().contains(key))
					String msg = ".." + key + "=" + normalizedPassword(value) + " ";
					// logger.debug9(".." + key + "=" + normalizedPassword(value) + " wird ersetz mit ");
					value = normalizedFields(value, environment, "${", "}");
					value = normalizedFields(value, environment, "%", "%");
					// if(key.indexOf("password") == -1 || !sosConfiguration.getPasswordnames().contains(key))
					// logger.debug9(msg + " is normalized in " + key + "  --> " + normalizedPassword(value));
					newProp.put(key, value);
				}
			}
		}
		catch (Exception e) {
			logger.warn("Failed to substitute environment variables in configuration file, cause: " + e.toString());
			return prop;
		}
		return newProp;
	}

	/**
	 * Create History File if not exist
	 * @throws Exception
	 */
	private void createHistoryFile() throws Exception {
		boolean writeHeaderFields = false;
		getLogger().debug1("calling " + sos.util.SOSClassUtil.getMethodName());
		try {
			if (getBool("testmode"))
				return;

			if (tempHistoryFile != null && tempHistoryFile.exists())
				return;

			if (sosString.parseToString(arguments, "history").length() > 0) {
				if (sosString.parseToString(arguments, conSettingOPERATION).equals(conOperationSEND)
						|| sosString.parseToString(arguments, conSettingOPERATION).equals(conOperationRECEIVE)) {
					historyFile = new File(sosString.parseToString(arguments, "history"));
					if (!historyFile.exists()) {
						if (historyFile.createNewFile()) {
							getLogger().info("creating global history file " + historyFile.getAbsolutePath());
						}
						else {
							getLogger().warn("could not create history file : " + historyFile.getAbsolutePath());
						}
					}

					if (historyFile.length() == 0)
						writeHeaderFields = true;

					if (!historyFile.canWrite()) {
						getLogger().warn("history file is read only: " + historyFile.getAbsolutePath());
						return;
					}

					tempHistoryFile = File.createTempFile("sosftp_history", ".csv", new File(System.getProperty("java.io.tmpdir")));

					tempHistoryFile.deleteOnExit();

					// überprüfen ob die Historien Felder Reihenfolge anders ist als in der Historiendatei.
					checkHistoryField(historyFields);

					history = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(tempHistoryFile)));

					if (writeHeaderFields) {
						historyFields = historyFields + ";" + newHistoryFields;
						for (int i = 0; i < historyEntrys.size(); i++) {
							String key = sosString.parseToString(historyEntrys.get(i));
							historyFields = historyFields + ";" + sosString.parseToString(key).substring("history_entry_".length()); //
						}

						history.write(historyFields);
						history.newLine();
					}

				}
			}
			else {

				if (getHistory() != null) {
					getHistory().close();
					history = null;
				}
			}

		}
		catch (Exception e) {
			RaiseException("error in  " + sos.util.SOSClassUtil.getMethodName() + " could not create history file, cause: " + e);
		}
	}

	private boolean existHistoryLockFile() throws Exception {
		BufferedReader bra = null;
		BufferedReader procout = null;
		int countOfDelay = 3;
		String pid = "";
		int delay = 5;
		boolean existPID = false;
		try {
			if (getBool("testmode")) {
				return false;
			}
			// Überprüfen, ob eine lock Datei existiert
			lockHistoryFile = new File(System.getProperty("java.io.tmpdir"), historyFile.getName() + ".lock");

			if (lockHistoryFile.exists()) {
				getLogger().debug9("..history lock File exists.");
				getLogger().debug("timestamp of last modification of history file: " + lockHistoryFile.lastModified());
				// delay einbauen
				for (int i = 0; i < countOfDelay; i++) {
					getLogger().debug5("..history lock file exist. wait 5 Seconds.");
					Thread.sleep(delay + 1000);
					if (!lockHistoryFile.exists()) {
						break;
					}
				}
				// gucken ob die lockdatei weiterhin existiert
				if (lockHistoryFile.exists()) {
					getLogger().debug5("..history lock file exist.");
					// wenn ja, pid ermitteln
					bra = new BufferedReader(new FileReader(lockHistoryFile));
					// überprüfen, ob dieser pid existiert

					if ((pid = bra.readLine()) != null) {
						getLogger().debug("reading from history lock file: pid = " + pid);
						// Für Window
						Process proc = null;
						if (System.getProperty("os.name").toLowerCase().indexOf("windows") > -1)
							proc = Runtime.getRuntime().exec("tasklist /FI \"PID eq " + pid + "\"");
						else
							proc = Runtime.getRuntime().exec("ps -p " + pid);

						procout = new BufferedReader(new InputStreamReader(proc.getInputStream()));
						String s;

						while ((s = procout.readLine()) != null) {
							getLogger().debug5("command output =\t" + s);
							if (s.indexOf(pid) > -1) {// process ist aktuell
								existPID = true;
								break;
							}
						}

						if (bra != null)
							bra.close();
						if (procout != null)
							procout.close();
					}
				}
			}

			if (!existPID && sosString.parseToString(pid).trim().length() > 0) {
				getLogger().debug("History lock File is deleting cause Process ID not exist.");
				// Es existiert eine History Lock datei aber keine PID. Die Lock Datei kann gelöscht werden
				if (lockHistoryFile.delete()) {
					getLogger().info("History lock File successfully deleted, cause Process ID not exist.");
				}
				else {
					RaiseException("History lock File " + lockHistoryFile.getCanonicalPath() + " could not delete. There is no Process Id exist[pid=" + pid
							+ "]");
				}
				existPID = false;

			}

			if (existPID)// History Datei existiert und der entsprechende pid ist aktuell. Andere Instanzen dürfen nicht schreiben.
				RaiseException("Could not write in History File, cause there is existing History Lock File.");

			return existPID;
		}
		catch (Exception e) {
			RaiseException("error in  " + sos.util.SOSClassUtil.getMethodName() + " while looking-up existing history lock file, cause: " + e);
		}
		finally {
			if (bra != null)
				bra.close();
			if (procout != null)
				procout.close();
		}
		return false;
	}

	/**
	 * Überprüft, ob die Historienfelder in der Historien datei identisch sind.
	 */
	private void checkHistoryField(final String historyFields_) throws Exception {

		BufferedReader br = null;
		BufferedWriter bw = null;
		String thisLine = "";
		boolean historyFilesChanged = false;

		try {
			getLogger().debug1("calling " + sos.util.SOSClassUtil.getMethodName());

			br = new BufferedReader(new FileReader(historyFile));

			int line = 0;
			while ((thisLine = br.readLine()) != null) { // while loop begins here
				if (line > 0 && !historyFilesChanged) {
					return;// es hat sich nichts geändert, abbruchbedingung
				}

				if (line == 0) {// erste zeile, Historienfelder aus der Datei lesen und vergleichen, ob alle Felder vorhanden sind

					for (int i = 0; i < historyEntrys.size(); i++) {
						newHistoryFields = newHistoryFields + ";" + historyEntrys.get(i).toString().substring("history_entry_".length());
					}

					String[] splitnewHistoryFields = newHistoryFields.split(";");
					for (int i = 0; i < splitnewHistoryFields.length; i++) {

						// Überprüfe, ob die neuen Felder bereits in der HistorienDatei vorhanden sind
						if (thisLine.indexOf(";" + splitnewHistoryFields[i] + ";") == -1 && !thisLine.startsWith(splitnewHistoryFields[i] + ";")
								&& !thisLine.endsWith(splitnewHistoryFields[i])) {

							// neue Felder sind hinzugekommen
							thisLine = thisLine + ";" + splitnewHistoryFields[i];
							historyFilesChanged = true;
						}
					}

					historyFields = thisLine;

					if (historyFilesChanged) {
						if (!existHistoryLockFile()) {
							// lock Datei erzeugen, der verhindert, historien Einträge zu schreiben
							createHistoryLockFile();
							bw = new BufferedWriter(new FileWriter(historyFile));
							bw.write(thisLine);
							bw.newLine();
						}

					}
				}
				else {

					bw.write(thisLine);
					bw.newLine();

				}
				line++;
			}

		}
		catch (Exception e) {
			RaiseException("\n -> ..error in " + SOSClassUtil.getMethodName() + " " + e);
		}
		finally {

			if (bw != null)
				bw.close();
			if (br != null)
				br.close();

			if (historyFilesChanged) {
				removeHistoryLockFile();

			}
		}

	}

	/**
	 * Beim Schreiben in die Historien Datei wird vorher eine lock Datei generiert. Das soll parallele
	 * SOSFTP Instanzen verhindert gleichzeitig in die Historie Datei zu schreiben.
	 * @throws Exception
	 */
	private void createHistoryLockFile() throws Exception {
		BufferedWriter lockBW = null;
		try {

			lockBW = new BufferedWriter(new FileWriter(lockHistoryFile));

			lockBW.write(sosString.parseToString(arguments, "current_pid"));

		}
		catch (Exception e) {
			RaiseException("could not create a history lock file, cause: " + e.toString());
		}
		finally {
			if (lockBW != null)
				lockBW.close();
		}

	}

	/**
	 * Beim Schreiben in die Historien Datei wird vorher eine lock Datei generiert. Das soll parallele
	 * SOSFTP Instanzen verhindert gleichzeitig in die Historie Datei zu schreiben.
	 * Hier soll die Lock Datei gelöscht werden, damit parallele SOSFTP Instanzen in die Historie schreiben können.
	 * @throws Exception
	 */
	private void removeHistoryLockFile() throws Exception {
		try {
			getLogger().debug1("calling " + sos.util.SOSClassUtil.getMethodName());
			getLogger().debug9("lockHistoryFile=" + lockHistoryFile.getCanonicalPath());
			if (lockHistoryFile != null && lockHistoryFile.exists()) {

				if (!lockHistoryFile.delete()) {
					getLogger().debug3("history lock file could not be deleted: " + lockHistoryFile.getCanonicalPath());
					Thread.sleep(1000);
					if (!lockHistoryFile.delete()) {
						RaiseException("history lock file could not be deleted: " + lockHistoryFile.getCanonicalPath());
					}
				}
			}
		}
		catch (Exception e) {
			RaiseException("could not delete a history lock file, cause: " + e.toString());
		}
	}

	/**
	 * Überprüft ob ein Jump Host angegeben wurde.
	 * Hier wird auf dem jump host ein temporäres Verzeichnis erzeugt,
	 * indem die Verzeichnisse temporär transferiert werden. Dieses verzeichnis wird anschliessend
	 * gelöscht.
	 *
	 * Hier werden neue Kommandos generiert.
	 */
	protected void hasJumpArguments() throws Exception {
		String curCommands = "";
		try {

			if (flgJumpTransferDefined == false)
				return;

			// Parameter die nicht an die SSH Commands übergeben werden sollen
			noJumpParameter.add("transfer_timestamp");
			noJumpParameter.add("date");
			noJumpParameter.add("time");
			noJumpParameter.add("current_pid");
			noJumpParameter.add("ppid");
			noJumpParameter.add("pid");
			noJumpParameter.add("local_user");
			noJumpParameter.add(conParameterSTATUS);
			noJumpParameter.add("successful_transfers");
			noJumpParameter.add("failed_transfers");
			noJumpParameter.add("last_error");
			noJumpParameter.add("history");
			noJumpParameter.add("history");
			noJumpParameter.add("history_repeat");
			noJumpParameter.add("history_interval");
			noJumpParameter.add("localhost");
			noJumpParameter.add("include");
			noJumpParameter.add("remote_host_ip");
			noJumpParameter.add("local_host_ip");
			noJumpParameter.add("mandator");
			noJumpParameter.add(conParamSCHEDULER_HOST);
			noJumpParameter.add(conParamSCHEDULER_PORT);
			noJumpParameter.add(conParamSCHEDULER_MESSAGE);
			noJumpParameter.add(conParamSCHEDULER_JOB_CHAIN);
			noJumpParameter.add("file_size");
			noJumpParameter.add("log_filename");
			noJumpParameter.add("command");
			noJumpParameter.add(conParameterREMOVE_AFTER_JUMP_TRANSFER);
			noJumpParameter.add("root");
			noJumpParameter.add("version");
			noJumpParameter.add(conParamREPLACING);
			noJumpParameter.add(conParamREPLACEMENT);
			noJumpParameter.add("getpidsexe");
			noJumpParameter.add("verbose");

			if (getString(conParamSSH_AUTH_FILE).startsWith("local:")) {
				String filename = getString(conParamSSH_AUTH_FILE).substring("local:".length());
				String text = sos.util.SOSFile.readFile(new File(filename));
				arguments.put(conParamSSH_AUTH_FILE, "filecontent:" + text);
			}

			if (getString(conSettingOPERATION).startsWith("install")) {
				noJumpParameter.add(conSettingOPERATION);
			}

			if (!getString(conSettingOPERATION).equals(conOperationREMOVE) && !getString(conSettingOPERATION).equals(conOperationRECEIVE))
				noJumpParameter.add(conSettingFILE_SPEC);

			// jump_command nicht ausgelesen
			arguments.put("xx_make_temp_directory_xx", "ok");// hilfsvariable, wenn dieses key existiert, dann gilt im execute diese
																// jump_command Parameter
			// OH 2012-06-04: jump braucht keine temporaeres Verzeichnis, wenn operation=remove
			if (!getString(conSettingOPERATION).equalsIgnoreCase(conOperationREMOVE)) {
				curCommands = getString(conSettingsJUMP_COMMAND) + " -operation=make_temp_directory -root=" + sosString.parseToString(arguments, "root");

				this.setCommands(curCommands.split(getCommandDelimiter()));
				if (!execute()) {
					RaiseException("error occurred processing command: " + normalizedPassword(curCommands));
				}
			}
			arguments.remove("xx_make_temp_directory_xx");
			String arg = "";
			String arg4postCommands = "";
			java.util.Iterator keys = arguments.keySet().iterator();
			while (keys.hasNext()) {
				String key = sosString.parseToString(keys.next());
				if (!key.startsWith("jump") && !key.startsWith(";") && !key.startsWith("history_entry_") && !key.equals("profile") && !key.equals("settings")
						&& !key.equals(conSettingLOCAL_DIR) && !key.equals(conSettingFILE_PATH) && !key.equals(conSettingREMOVE_FILES)
						&& !noJumpParameter.contains(key)) {
					String val = sosString.parseToString(arguments, key);

					if (val.length() == 0)
						continue;

					if (key.equals(conParamPASSWORD)) {
						val = new SOSCommandline().getExternalPassword(val, logger);

					}

					arg = arg + " -" + key + "=" + "\"" + val + "\"";

					if (!key.equals(conSettingOPERATION) && !key.equals(conParamMAKE_DIRS) && !key.equals("root")
							&& !(key.equals("transactional") && getString(conSettingOPERATION).equals(conOperationRECEIVE))) {
						arg4postCommands = arg4postCommands + " -" + key + "=" + "\"" + val + "\"";
					}

				}

			}

			if (getString(conSettingOPERATION).equalsIgnoreCase(conOperationRECEIVE) && getBool("skip_transfer") && getBool(conSettingREMOVE_FILES)) {
				arg = arg + " -remove_files=\"yes\"";
			}

			if (getString(conSettingOPERATION).startsWith(conOperationRECEIVE) && getBool("skip_transfer")) {
				// wenn jump host überprüfen soll, welche Dateien es im target host liegen, dann wird dieser in execute
				// mit Hilfe String operationen ausgelesen. Damit sparen wird das Übertragen der Dateien auf jump Host um von Lokal Host
				// die Dateien zu überprüfen. Diese Änderung ist notwendig, wenn SOSFTP vom JobSchedulerReceive.java mit der Parameter
				// parallel = yes aufgerufen wird.
				arg = arg + " -verbose=\"9\"";

			}
			else {
				arg = arg + " -verbose=\"" + getLogger().getLogLevel() + "\"";
			}

			if (getString(conSettingOPERATION).startsWith("install"))
				arg += getKeywordValuePair(conSettingOPERATION, conOperationSEND); // " -operation=\"send\"";

			// OH 2012-06-04: jump braucht keine temporaeres Verzeichnis, wenn operation=remove
			// erstaunlicherweise reicht es, dass -local_dir gesetzt ist, damit ein tempDir angelegt wird.
			if (getString(conSettingOPERATION).equalsIgnoreCase(conOperationREMOVE)) {
				arg = arg + " -remove_files=yes";
			}
			else {
				arg = arg + " -local_dir=\"" + tempJumpRemoteDir + "\"";
			}
			if (getString(conSettingOPERATION).equalsIgnoreCase(conOperationRECEIVE) && sosString.parseToString(arguments, conSettingFILE_PATH).length() > 0) {

				arg = arg + " -file_path=\"" + sosString.parseToString(arguments, conSettingFILE_PATH) + "\"";
			}

			if (getString(conSettingOPERATION).equalsIgnoreCase(conOperationREMOVE) && sosString.parseToString(arguments, conSettingFILE_PATH).length() > 0) {
				/**
				 * OH 2012-07-09: If file list in file_path too long then ssh session throws error: Word too long
				 * So the command to jump host is splitted in tokens with max length 4096
				 * see http://stackoverflow.com/questions/5772156/linux-command-xargs-maximum-size-of-the-arguments-passed-by-it
				 */
				// arg = arg + " -file_path=\"" + sosString.parseToString(arguments, conSettingFILE_PATH) + "\"";
				// curCommands = getString(conSettingsJUMP_COMMAND) + " " + arg + commandDelimiter;
				curCommands = splitFilePathCommand(arg);
			}
			else
				if (getString(conSettingOPERATION).equalsIgnoreCase(conOperationSEND) || getString(conSettingOPERATION).startsWith("install")) {
					arg = arg + " -remove_files=yes";
					// - niemals bei 1. Transfer-Operation zum jump_host lokal löschen
					// - erst nach 2. Transfer-Operation löschen, wenn erfolgreich
					curCommands = getString(conSettingsJUMP_COMMAND) + " " + arg;
					if (!getString(conSettingOPERATION).equalsIgnoreCase(conOperationREMOVE)) {
						postCommands = curCommands;
					}
					curCommands += commandDelimiter;
				}
				else {
					curCommands = getString(conSettingsJUMP_COMMAND) + " " + arg + commandDelimiter;
					/**
					 * http://www.sos-berlin.com/jira/browse/SOSFTP-108
					 * Das Kommando zum löschen aller Dateien auf dem source_server, die file_spec entsprechen, wird hier aufgebaut.
					 * "remove_files" wirkt trotzdem, indem jede Datei direkt nach dem Transfer gelöscht wird :-((, wenn es nicht transaktional ist.
					 * Damit kann das Kommando mächtig Unsinn anstellen, wenn während der Übertragung weitere Dateien, die file_spec entsprechen, im Verzeichnis auftauchen.
					 * Die werden dann nämlich gelöscht, ohne daß diese übertragen worden sind.
					 * Es dürfen grundsätzlich eigentlich nur die Dateien gelöscht werden, die auch übertragen worden sind.
					 *
					 * kb 2012-05-25
					 */
					// if (getBool(conSettingREMOVE_FILES)) {
					// postCommands = getString(conSettingsJUMP_COMMAND) + " -operation=\"" + conOperationREMOVE + "\" " + arg4postCommands
					// +
					// " -file_path=\""
					// + sosString.parseToString(arguments, conSettingFILE_PATH) + "\"";
					// }
				}

			if (sosString.parseToString(postCommands).length() > 0) {
				/**
				 * Bei send/Receive darf das replacement nicht ausgeführt werden, weil dort der Client im Intranet
				 * der Master ist. Der client in der DMZ muß die Dateien einfach nur so rumschieben.
				 *
				 * Bei transactional und/oder atomic gibt es allerdings noch ein rename, wofür dann das replacement/replacing
				 * benötigt wird. Deshalb sind die parameter replacement und replacing hier für den DMZ-Client wichtig.
				 *
				 * kb 2012-06-06
				 */
				if (isTransferOperation() == false) {
					if (getString(conParamREPLACEMENT).length() > 0 || getString(conParamREPLACING).length() > 0) {
						postCommands += getKeywordValuePair(conParamREPLACEMENT) + getKeywordValuePair(conParamREPLACING);
					}
				}
				postCommands += commandDelimiter;
				getLogger().debug1("postCommands:  " + normalizedPassword(postCommands));
			}

			this.setCommands(curCommands.split(this.getCommandDelimiter()));

			getLogger().debug1("curCommands:  " + normalizedPassword(curCommands));

			// Anpassen der Parameter für einen jump transfer per ftp
			// Löschen der Dateien erst nach erfolgreichen übertragen

			/**
			 * Bug: damit werden alle Dateien evtl. gelöscht, auch die, die nicht übertragen wurden, weil sie während der Übertragung erst eingetroffen sind.
			 * http://www.sos-berlin.com/jira/browse/SOSFTP-108
			 *
			arguments.put(conParameterREMOVE_AFTER_JUMP_TRANSFER, getString("remove_files")));
			arguments.put("remove_files", "no");
			*/
			arguments.put(conParamMAKE_DIRS, "yes");// erlaubt das Generieren eines Temporären verzeichnis auf dem lokalen Rechner
			arguments.put("jump_remote_dir", tempJumpRemoteDir);// Name des temporären Verzeichnis als remote_dir angeben

			if (!arguments.containsKey(conParamJUMP_PROTOCOL)) {
				arguments.put(conParamJUMP_PROTOCOL, "sftp");
			}

			// jump Parameter jetzt in ftp Parameter umbennen um die Dateien temporär zu übertragen
			Properties prop = new Properties();
			java.util.Iterator keys2 = arguments.keySet().iterator();
			while (keys2.hasNext()) {
				String key = sosString.parseToString(keys2.next());
				if (key.startsWith("jump_")) {
					prop.put(key.substring(5), sosString.parseToString(arguments, key));
				}
			}
			arguments.putAll(prop);
		}
		catch (Exception e) {
			RaiseException("Failed to jump to: " + e);
		}

	}

	/**
	 * OH 2012-07-09: If file list in file_path option too long then ssh session throws error: Word too long
	 * So the command to jump host is splitted in tokens with max length 4096 (default)
	 * see http://stackoverflow.com/questions/5772156/linux-command-xargs-maximum-size-of-the-arguments-passed-by-it
	 * see http://support.microsoft.com/kb/830473
	 */
	protected String splitFilePathCommand(final String curCommandArguments) throws Exception {
		String curCmdArguments = getString(conSettingsJUMP_COMMAND) + " " + curCommandArguments + " -file_path=";
		int commandLineLength = curCmdArguments.length() + 2;
		int maxCommandLineLength = getIntArg(arguments, "max_command_length", 4096);
		getLogger().debug1("Parameter [max_command_length] : " + maxCommandLineLength);
		String curCommands = "";
		String[] filePaths = sosString.parseToString(arguments, conSettingFILE_PATH).split(";");
		String filePathPartBefore = "";
		String filePathPartAfter = "";

		for (String filePath : filePaths) {
			filePathPartBefore = filePathPartAfter;
			filePathPartAfter = filePathPartAfter + filePath + ";";
			if (filePathPartAfter.length() >= maxCommandLineLength - commandLineLength && filePathPartBefore.length() > 0) {
				curCommands = curCommands + curCmdArguments + "\"" + filePathPartBefore + "\"" + this.getCommandDelimiter();
				filePathPartAfter = filePath + ";";
			}
		}
		if (filePathPartAfter.length() > 0) {
			curCommands = curCommands + curCmdArguments + "\"" + filePathPartAfter + "\"" + this.getCommandDelimiter();
		}
		return curCommands;
	}

	protected boolean isTransferOperation() {
		boolean flgR = false;
		String strOp = getString(conSettingOPERATION);

		if (strOp.equalsIgnoreCase(conOperationSEND) || strOp.equalsIgnoreCase(conOperationRECEIVE)) {
			flgR = true;
		}
		return flgR;
	}

	/**
	 * Alle Umgebungsvariable die mit sosftp_ anfangen werden ausgelesen.
	 *
	 * Umgebungsvaribalen werden in einem globalen Parametern gemerkt, um
	 * Substitution von Environment-Variablen in Konfigurationsdateien vorzunehmen.
	 *
	 * @param logger_
	 * @return sos.util.Properties
	 * @throws Exception
	 */
	private static Properties getEnvVars(final SOSLogger logger_) throws Exception {
		Properties envVars = new Properties();
		try {
			if (logger != null)
				logger.debug5("reading environment");

			Process p = null;
			Runtime r = Runtime.getRuntime();
			String OS = System.getProperty("os.name").toLowerCase();

			environment = new Properties();

			if (OS.indexOf("windows 9") > -1) {
				p = r.exec("command.com /c set");
			}
			else
				if (OS.indexOf("nt") > -1 || OS.indexOf("windows") > -1) {
					p = r.exec("cmd.exe /c set");
				}
				else {
					p = r.exec("env");
				}

			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			while ((line = br.readLine()) != null) {
				int idx = line.indexOf('=');
				if (idx > -1) {
					String key = line.substring(0, idx);
					String value = "";
					if (key.toLowerCase().startsWith("sosftp_")) {
						value = line.substring(idx + 1);
						envVars.setProperty(key.substring(7).toLowerCase(), value);
					}
					else
						if (key.toLowerCase().startsWith("current_pid") || key.toLowerCase().startsWith("ppid")) {
							value = line.substring(idx + 1);
							envVars.setProperty(key.toLowerCase(), value);

						}
						else
							if (key.toLowerCase().indexOf("scheduler_param_") > -1) {
								value = line.substring(idx + 1);
								// envVars.setProperty( key.toLowerCase(), value );
								schedulerParams.setProperty(key.toLowerCase().substring("scheduler_param_".length()), value);
								if (logger != null)
									logger.debug5(".. environment [" + key + "]: " + value);
							}

					environment.put(key, line.substring(idx + 1));
				}
			}
			return envVars;
		}
		catch (Exception e) {
			if (logger != null)
				logger.warn("[ERROR] could not read environment, cause: " + e.getMessage());
			RaiseException("error occurred reading environment: " + e.toString());
		}
		return envVars;
	}

	/**
	 * Löscht das Temporäres Verzeichnis
	 * @return boolean
	 */
	private boolean removeTempDirectory() {

		try {
			getLogger().debug1("***********remove Temporary Directory*************");

			String inputFile = "";

			String[] split = sosString.parseToString(arguments, "input").split(";");
			for (String element : split) {
				inputFile = sosString.parseToString(element);
				getLogger().debug5("remove: " + inputFile);
				if (inputFile.length() == 0) {
					getLogger().debug1("no directory/file has been specified for removal: " + inputFile);
					return false;
				}

				getLogger().debug1("..parameter [input=" + inputFile + "]");

				File tmpDir = new File(inputFile);
				if (!tmpDir.exists()) {
					getLogger().debug1("directory/file to be removed does not exist: " + tmpDir.getCanonicalPath());
					return false;
				}

				// Verzeichnis kann nicht gelöscht werden, wenn eine Datei unterhalb dieses Verzeichnis besteht
				File[] listOfFiles = tmpDir.listFiles();
				for (int j = 0; j < listOfFiles.length; j++) {
					if (listOfFiles.length > 0) {
						if (!listOfFiles[j].delete())
							getLogger().warn(listOfFiles[j].getCanonicalPath() + " could not be deleted");
						else
							getLogger().debug1(listOfFiles[j].getCanonicalPath() + " was successfully deleted");
					}
				}
				if (tmpDir.delete())
					getLogger().debug1(tmpDir.getCanonicalPath() + " was successfully deleted");
				else {
					getLogger().warn(tmpDir.getCanonicalPath() + " could not be deleted");
				}
			}

		}
		catch (Exception e) {
			try {
				getLogger().warn("error deleting temporary directory, cause: " + e.toString());
			}
			catch (Exception ex) {

			}
		}
		return true;
	}

	/**
	 * Generiert ein Temporäres Verzeichnis für ftp transfer auf der Jump Host
	 * @return
	 */
	private boolean makeTempDirectory() throws Exception {

		String output = "";
		try {
			getLogger().debug1("***********make Temporary Directory*************");
			if (sosString.parseToString(arguments, "root").length() > 0) {
				output = File.createTempFile("sos_ftp", null, new File(normalized(sosString.parseToString(arguments, "root")))).getCanonicalPath();
			}
			else {
				output = File.createTempFile("sos_ftp", null, new File(System.getProperty("java.io.tmpdir"))).getCanonicalPath();
			}
			tempJumpRemoteDir = output;
			// diese Logausgabe darf nicht verändert werden. Wenn doch dann auch in execute-Methode anpassen. Der Log Level muss INFO sein
			getLogger().info("*******ftp transfer directory is:" + output + "******************");
			if (new File(output).getCanonicalFile().exists()) {
				// nur ein tempname wird gebraucht File.createTempFile erzeugt automatisch eine Datei, wir brauchen nur einen
				// eindeutigen Namen für einen Verzeichnis
				new File(output).getCanonicalFile().delete();
			}
			if (!new File(output).getCanonicalFile().mkdirs())
				RaiseException("could not create temporary directory");
		}
		catch (Exception e) {

			getLogger().warn("error creating temporary directory, cause: " + e.toString());
			RaiseException("error creating temporary directory, cause: " + e.toString());
		}
		return true;
	}

	protected String normalized(String str) {
		str = str.replaceAll(conRegExpBackslash, "/");
		return str.endsWith("/") || str.endsWith("\\") ? str : str + "/";

	}

	/**
	 * This program logs output to stdout or to a file that has been specified by the parameter log_filename.
	 * A template can be used in order to organize the output that is created. The output is grouped into header, file list and footer.
	 * This specifies a template file for header and footer output.
	 * Templates can use internal variables and parameters as placeholders in the form %{placeholder}.
	 *
	 * @param header
	 * @return String
	 * @throws Exception
	 */
	public String getBanner(final boolean header) throws Exception {
		String curBannerHeader = "";
		String curBannerFooter = "";
		String banner = "";
		try {

			if (header) {
				curBannerHeader = bannerHeader;

				if (sosString.parseToString(arguments, "banner_header").length() > 0) {
					String b = sosString.parseToString(arguments, "banner_header");
					File fBanner = new File(b);
					if (!fBanner.exists()) {
						getLogger().warn("[banner_header=" + b + "] does not exist. Using default banner");
					}
					else {
						curBannerHeader = sos.util.SOSFile.readFile(fBanner);
					}
				}

				Iterator it = arguments.keySet().iterator();
				while (it.hasNext()) {
					String key = sosString.parseToString(it.next());
					if (key.length() > 0) {
						if (key.equals(conSettingFILE_SPEC) && sosString.parseToString(arguments, conSettingFILE_PATH).length() > 0)
							continue;

						int pos1 = -1;
						int pos2 = -1;
						boolean loop = true;
						while (loop) {
							pos1 = curBannerHeader.indexOf("%{" + key + "}");
							pos2 = pos1 + ("%{" + key + "}").length();
							if (pos1 > -1 && pos2 > pos1) {
								curBannerHeader = curBannerHeader.substring(0, pos1) + sosString.parseToString(arguments, key)
										+ curBannerHeader.substring(pos2);
							}

							pos1 = curBannerHeader.indexOf("%{" + key + "}", pos2);
							if (pos1 == -1)
								loop = false;
						}

					}
				}
				curBannerHeader = clearBanner(curBannerHeader);
				banner = curBannerHeader;
			}
			else {

				curBannerFooter = bannerFooter;
				if (sosString.parseToString(arguments, "banner_footer").length() > 0) {
					String b = sosString.parseToString(arguments, "banner_footer");
					File fBanner = new File(b);
					if (!fBanner.exists()) {
						getLogger().warn("[banner_footer=" + b + "] does not exist. Using default banner");
					}
					else {
						curBannerFooter = sos.util.SOSFile.readFile(fBanner);
					}
				}

				@SuppressWarnings("rawtypes")
				Iterator it = arguments.keySet().iterator();
				while (it.hasNext()) {
					String key = sosString.parseToString(it.next());
					if (key.length() > 0) {
						if (key.equals(conSettingFILE_SPEC) && sosString.parseToString(arguments, conSettingFILE_PATH).length() > 0)
							continue;
						boolean loop = true;

						loop = true;
						int pos3 = -1;
						int pos4 = -1;
						while (loop) {
							pos3 = curBannerFooter.indexOf("%{" + key + "}");
							pos4 = pos3 + ("%{" + key + "}").length();

							if (pos3 > -1 && pos4 > pos3)
								curBannerFooter = curBannerFooter.substring(0, pos3) + sosString.parseToString(arguments, key)
										+ curBannerFooter.substring(pos4);

							pos3 = curBannerFooter.indexOf("%{" + key + "}", pos4);
							if (pos3 == -1)
								loop = false;
						}
					}
				}

				// alle Platzhalter, die keinen Wert haben löschen
				curBannerFooter = clearBanner(curBannerFooter);
				banner = curBannerFooter;
				// arguments.remove("for_banner");
			}
		}
		catch (Exception e) {
			RaiseException("error occurred getting banner: " + e.getMessage());
		}
		return banner;
	}

	/**
	 * Löschen von leeren Platzhalter
	 * @param str
	 * @return
	 */
	private String clearBanner(String str) {
		boolean loop = true;
		while (loop) {
			int pos1 = str.indexOf("%{");
			int pos2 = str.indexOf("}", pos1) + "}".length();

			if (pos1 > -1 && pos2 > pos1) {
				int iSpace = pos2 - pos1;
				String space = "   ";
				while (iSpace != 0) {
					space = space + " ";
					iSpace--;
				}
				str = str.substring(0, pos1) + space + str.substring(pos2);
			}
			pos1 = str.indexOf("%{", pos2);
			if (pos1 == -1)
				loop = false;
		}
		return str;
	}

	/**
	 * @return Returns the sshConnection.
	 */
	protected Connection getSshConnection() {
		return sshConnection;
	}

	/**
	 * @param sshConnection The sshConnection to set.
	 */
	protected void setSshConnection(final Connection sshConnection) {
		this.sshConnection = sshConnection;
	}

	/**
	 * Install SOSFTP on a remote server
	 *
	 * @return boolean
	 * @throws Exception
	 */
	protected boolean install() throws Exception {
		return false;
	}

	/**
	 * Initialisieren der sos.net.SOSFTP
	 * @throws Exception
	 */
	protected void initSOSFTP() throws Exception {
		try {

			try {
				sosftp = new SOSFTP(host, port);
			}
			catch (Exception e) {
				this.getLogger().debug("SOSFtp exception raised");
				// e.printStackTrace();
				throw e; // kb 2011-04-27 grrrrr
			}
			// sos.net.SOSFTP sosftp = new sos.net.SOSFTP(host, port);
			// sosftp.connect(host, port);
			ftpClient = sosftp;
			this.getLogger().debug("..ftp server reply [init] [host=" + host + "], [port=" + port + "]: " + ftpClient.getReplyString());

			if (account != null && account.length() > 0) {
				isLoggedIn = sosftp.login(user, new SOSCommandline().getExternalPassword(password, logger), account);
				this.getLogger().debug("..ftp server reply [login] [user=" + user + "], [account=" + account + "]: " + ftpClient.getReplyString());
			}
			else {
				isLoggedIn = sosftp.login(user, new SOSCommandline().getExternalPassword(password, logger));
				this.getLogger().debug("..ftp server reply [login] [user=" + user + "]: " + ftpClient.getReplyString());
			}

			if (isLoggedIn == false || sosftp.getReplyCode() > ERROR_CODE) {
				logger.info("..sftp server login failed [user=" + user + "], [host=" + host + "]: " + ftpClient.getReplyString());
				throw new Exception (ftpClient.getReplyString());
				// SOSFTP-113

			}

			postLoginOperations();
		}
		catch (Exception e) {
			throw e;
//			RaiseException("error in  " + sos.util.SOSClassUtil.getMethodName() + ", cause: " + e);
		}
	}

	/**
	 * Performs some post-login operations, such trying to detect server support
	 * for utf8.
	 *
	 */
	private void postLoginOperations() throws Exception {
		// synchronized (lock) {
		utf8Supported = false;
		restSupported = false;
		mlsdSupported = false;
		modezSupported = false;
		dataChannelEncrypted = false;

		if (flgCheckServerFeatures == true) { // JIRA SOSFTP-92
			String strCmd = "FEAT";
			sosftp.sendCommand(strCmd);
			this.getLogger().info("..ftp server reply [" + strCmd + "]: " + ftpClient.getReplyString());
		}
		// FTPReply r = sosftp.readFTPReply();
		// if (r.getCode() == 211) {
		// String[] lines = r.getMessages();
		// for (int i = 1; i < lines.length - 1; i++) {
		// String feat = lines[i].trim().toUpperCase();
		// // REST STREAM supported?
		// if ("REST STREAM".equalsIgnoreCase(feat)) {
		// restSupported = true;
		// continue;
		// }
		// // UTF8 supported?
		// if ("UTF8".equalsIgnoreCase(feat)) {
		// utf8Supported = true;
		// sosftp.changeCharset("UTF-8");
		// continue;
		// }
		// // MLSD supported?
		// if ("MLSD".equalsIgnoreCase(feat)) {
		// mlsdSupported = true;
		// continue;
		// }
		// // MODE Z supported?
		// if ("MODE Z".equalsIgnoreCase(feat) || feat.startsWith("MODE Z ")) {
		// modezSupported = true;
		// continue;
		// }
		// }
		// }
		// // Turn UTF 8 on (if supported).
		// if (utf8Supported) {
		// sosftp.sendFTPCommand("OPTS UTF8 ON");
		// sosftp.readFTPReply();
		// }
		// // Data channel security.
		// if (security == SECURITY_FTPS || security == SECURITY_FTPES) {
		// sosftp.sendFTPCommand("PBSZ 0");
		// sosftp.readFTPReply();
		// sosftp.sendFTPCommand("PROT P");
		// FTPReply reply = sosftp.readFTPReply();
		// if (reply.isSuccessCode()) {
		// dataChannelEncrypted = true;
		// }
		// }
		// }
	}

	/**
	 * Initialisieren der sos.net.SOSSFTP
	 * @throws Exception
	 */
	protected void initSOSSFTP() throws Exception {

		try {
			SOSSFTP sftpClient = new SOSSFTP(host, port);

			ftpClient = sftpClient;
			sftpClient.setAuthenticationFilename(authenticationFilename);
			sftpClient.setAuthenticationMethod(authenticationMethod);
			sftpClient.setPassword(new SOSCommandline().getExternalPassword(password, logger));
			sftpClient.setProxyHost(proxyHost);
			sftpClient.setProxyPort(proxyPort);
			sftpClient.setProxyPassword(new SOSCommandline().getExternalPassword(proxyPassword, logger));
			sftpClient.setProxyUser(proxyUser);
			sftpClient.setUser(user);
			sftpClient.connect();
			isLoggedIn = true;
			this.getLogger().debug("..sftp server logged in [user=" + user + "], [host=" + host + "]");
		}
		catch (Exception e) {
			isLoggedIn = false;
			logger.info("..sftp server login failed [user=" + user + "], [host=" + host + "]: " + e);
			throw e;
			// SOSFTP-113
		}
	}

	protected void initSOSFTPS() throws Exception {
		try {

			if (proxyHost != null && proxyPort != 0) {
				System.getProperties().setProperty("proxyHost", proxyHost);
				System.getProperties().setProperty("proxyPort", String.valueOf(proxyPort));
				System.getProperties().setProperty("proxySet", "true");
			}

			SOSFTPS sosftp = new SOSFTPS(host, port);
			ftpClient = sosftp;

			this.getLogger().debug("..ftps server reply [init] [host=" + host + "], [port=" + port + "]: " + ftpClient.getReplyString());

			isLoggedIn = sosftp.login(user, new SOSCommandline().getExternalPassword(password, logger));
			this.getLogger().debug("..ftps server reply [login] [user=" + user + "]: " + ftpClient.getReplyString());

			if (!isLoggedIn || sosftp.getReplyCode() > ERROR_CODE) {
				throw new Exception("..ftps server reply [login failed] [user=" + user + "], [account=" + account + "]: " + ftpClient.getReplyString());
			}

		}
		catch (Exception e) {
			logger.info("..sftp server login failed [user=" + user + "], [host=" + host + "]: " + e);
			throw e;
			// SOSFTP-113
		}
	}

	public Properties getArguments() {
		return arguments;
	}

	private BufferedWriter getHistory() {
		return history;
	}

	/**
	 * History Eintrag schreiben
	 * @param localFilename
	 * @param remoteFilename
	 * @throws Exception
	 */
	@SuppressWarnings("unchecked")
	protected void writeHistory(final String localFilename, final String remoteFilename) throws Exception {
		String EMPTY = "";
		String hist = "";
		// long guid = 0;
		String guid = EMPTY;
		String mandator = "";
		String transfer_timestamp = "";
		String pid = "";
		String ppid = "";
		String operation = "";
		String localhost = "";
		String localhost_ip = "";
		String local_user = "";
		String remote_host = "";
		String remote_host_ip = "";
		String remote_user = "";
		String protocol = "";
		String port = "";
		String local_dir = "";
		String remote_dir = "";
		String local_filename = "";
		String remote_filename = "";
		String fileSize = "";
		String md5 = "";
		String status = "";
		String last_error_message = "";
		String log_filename = "";

		// new HistoryFields 04.09.2009
		String jump_host = "";
		String jump_host_ip = "";
		String jump_port = "";
		String jump_protocol = "";
		String jump_user = "";

		try {
			if (!sosString.parseToString(arguments, conSettingOPERATION).equalsIgnoreCase(conOperationSEND)
					&& !sosString.parseToString(arguments, conSettingOPERATION).equalsIgnoreCase(conOperationRECEIVE)) {
				return;
			}

			getLogger().debug9("local filename=" + localFilename + ", remote filename=" + remoteFilename);

			if (sosString.parseToBoolean(sosString.parseToString(arguments, "skip_transfer"))) {
				return;
			}

			if (history == null && !sendSchedulerSignale) {
				return;
			}

			File localFile = null;

			if (sosString.parseToString(localFilename).length() > 0) {
				localFile = new File(localFilename);
			}

			// guid = sos.util.SOSUniqueID.get(); // 4- GUID
			UUID objUUID = UUID.randomUUID();
			guid = objUUID.toString();
			mandator = sosString.parseToString(arguments, "mandator").length() > 0 ? sosString.parseToString(arguments, "mandator") : defaultMandator; // 0-
																																						// mandator:
																																						// default
																																						// SOS
			transfer_timestamp = sos.util.SOSDate.getCurrentTimeAsString(); // 1- timestamp: Zeitstempel im ISO-Format
			pid = sosString.parseToString(arguments, "current_pid").length() > 0 ? sosString.parseToString(arguments, "current_pid") : "0"; // 2-
																																			// pid=
																																			// Environment
																																			// PID
																																			// |
																																			// 0
																																			// für
																																			// Windows
			ppid = sosString.parseToString(arguments, "ppid").length() > 0 ? sosString.parseToString(arguments, "ppid") : "0"; // 3- ppid=
																																// Environment
																																// PPID
																																// | 0
																																// für
																																// Windows
			operation = sosString.parseToString(arguments, conSettingOPERATION); // 4- operation: send|receive
			localhost = sosString.parseToString(arguments, "localhost"); // 5- local host
			localhost_ip = sosString.parseToString(arguments, "local_host_ip"); // 5-1- local host IP adresse
			local_user = sosString.parseToString(System.getProperty("user.name")); // 6- local user
			if (flgJumpTransferDefined) {
				remote_host = sosString.parseToString(originalParam, conParamHOST); // 7- remote host
				if (remote_host.length() > 0)
					remote_host_ip = java.net.InetAddress.getByName(remote_host).getHostAddress();
				remote_user = sosString.parseToString(originalParam, "user"); // 8- remote host user
				protocol = sosString.parseToString(originalParam, conParamPROTOCOL); // 9- protocol
			}
			else {
				// TODO check for use of alternate host 
				remote_host = sosString.parseToString(arguments, conParamHOST); // 7- remote host
				remote_host_ip = sosString.parseToString(arguments, "remote_host_ip"); // 7- remote host IP
				remote_user = sosString.parseToString(arguments, "user"); // 8- remote host user
				protocol = sosString.parseToString(arguments, conParamPROTOCOL); // 9- protocol
			}

			port = sosString.parseToString(arguments, conParamPORT); // 10- port

			// ab hier unterscheiden on der Fehler während dem Datei transfer oder vor dem datei transfer entstanden ist
			if (sosString.parseToString(arguments, conSettingOPERATION).equals(conOperationSEND)) {
				local_dir = localFile != null && localFile.getParent() != null ? clearCRLF(localFile.getParent()) : clearCRLF(sosString.parseToString(
						arguments, conSettingLOCAL_DIR)); // 11- local dir
			}
			else {
				local_dir = clearCRLF(sosString.parseToString(arguments, conSettingLOCAL_DIR)); // 11- local dir
			}
			if (sosString.parseToString(local_dir).length() == 0)
				local_dir = ".";
			if (flgJumpTransferDefined) {
				remote_dir = clearCRLF(sosString.parseToString(originalParam, "remote_dir")); // 12- remote dir
			}
			else {
				remote_dir = clearCRLF(sosString.parseToString(arguments, "remote_dir")); // 12- remote dir
			}
			local_filename = localFile != null ? clearCRLF(localFile.getName()) : EMPTY; // 13- file name
			remote_filename = sosString.parseToString(remoteFilename).length() > 0 ? clearCRLF(new File(remoteFilename).getName()) : EMPTY; // 14-
																																			// file
																																			// name
			fileSize = sosString.parseToString(arguments, "file_size").length() > 0 ? sosString.parseToString(arguments, "file_size") : EMPTY;

			if (sosString.parseToString(localFilename).length() == 0 || sosString.parseToString(remoteFilename).length() == 0) {
				md5 = EMPTY; // MD5
			}
			else {
				File f4md5 = localFile.getName().equals(new File(remoteFilename).getName()) ? new File(remoteFilename) : sosString.parseToString(arguments,
						conSettingOPERATION).equals(conOperationRECEIVE) ? new File(normalized(local_dir) + new File(remoteFilename).getName()) : new File(
						normalized(local_dir) + localFile.getName()); // falls ein replacing verwendet wurde

				if (f4md5.exists()) {
					getLogger().debug9("md5 for " + f4md5.getAbsolutePath());
					md5 = sos.util.SOSCrypt.MD5encrypt(f4md5); // MD5
				}
				else {
					md5 = EMPTY; // eventuell existiert die Datei nicht
				}
			}

			status = sosString.parseToString(arguments, conParameterSTATUS).length() == 0 ? conParamValueSUCCESS : sosString.parseToString(arguments, conParameterSTATUS);// -
																																			// status=success|error

			last_error_message = clearCRLF(getLogger().getError() != null && getLogger().getError().length() > 0 ? getLogger().getError()
					: getLogger().getWarning()); // 15- last_error=|warn message
			last_error_message = normalizedPassword(sosString.parseToString(last_error_message));
			log_filename = sosString.parseToString(arguments, "log_filename");

			jump_host = sosString.parseToString(arguments, conParamJUMP_HOST);
			if (jump_host.length() > 0)
				jump_host_ip = java.net.InetAddress.getByName(host).getHostAddress();
			jump_port = sosString.parseToString(arguments, "jump_port");
			jump_protocol = sosString.parseToString(arguments, conParamJUMP_PROTOCOL);
			jump_user = sosString.parseToString(arguments, "jump_user");

			// TODO mit den Mappings im SOSFTPHistoryJob synchronisieren. Hier muss eine *gemeinsame* Klasse verwendet werden
			// am besten mit der OptionKlasse koppeln, da gleiche datentypen etc.

			Properties objSchedulerOrderParameterSet = new Properties();
			// objSchedulerOrderParameterSet.put("guid", String.valueOf(guid)); // 1- GUID
			objSchedulerOrderParameterSet.put("guid", guid); // 1- GUID
			objSchedulerOrderParameterSet.put("mandator", mandator); // 2- mandator: default SOS
			objSchedulerOrderParameterSet.put("transfer_timestamp", transfer_timestamp); // 3- timestamp: Zeitstempel im ISO-Format
			objSchedulerOrderParameterSet.put("pid", pid); // 4- pid= Environment PID | 0 für Windows
			objSchedulerOrderParameterSet.put("ppid", ppid); // 5- ppid= Environment PPID | 0 für Windows
			objSchedulerOrderParameterSet.put(conSettingOPERATION, operation); // 6- operation: send|receive
			objSchedulerOrderParameterSet.put("localhost", localhost); // 7- local host
			objSchedulerOrderParameterSet.put("localhost_ip", localhost_ip); // 8- local host IP adresse
			objSchedulerOrderParameterSet.put("local_user", local_user); // 9- local user
			objSchedulerOrderParameterSet.put("remote_host", remote_host); // 10- remote host
			objSchedulerOrderParameterSet.put("remote_host_ip", remote_host_ip); // 11- remote host IP
			objSchedulerOrderParameterSet.put("remote_user", remote_user); // 12- remote host user
			objSchedulerOrderParameterSet.put(conParamPROTOCOL, protocol); // 13- protocol
			objSchedulerOrderParameterSet.put(conParamPORT, port); // 14- port
			objSchedulerOrderParameterSet.put(conSettingLOCAL_DIR, local_dir); // 15- local dir
			objSchedulerOrderParameterSet.put("remote_dir", remote_dir); // 16- remote dir
			objSchedulerOrderParameterSet.put("local_filename", local_filename); // 17- file name
			objSchedulerOrderParameterSet.put("remote_filename", remote_filename); // 18- file name
			objSchedulerOrderParameterSet.put("file_size", fileSize); // 19 - file name
			objSchedulerOrderParameterSet.put("md5", md5); // 20
			objSchedulerOrderParameterSet.put(conParameterSTATUS, status); // 21- status=success|error
			objSchedulerOrderParameterSet.put("last_error_message", last_error_message); // 22
			objSchedulerOrderParameterSet.put("log_filename", log_filename); // 23
			objSchedulerOrderParameterSet.put(conParamJUMP_HOST, jump_host); // 24
			objSchedulerOrderParameterSet.put("jump_host_ip", jump_host_ip); // 25
			objSchedulerOrderParameterSet.put("jump_port", jump_port); // 26
			objSchedulerOrderParameterSet.put(conParamJUMP_PROTOCOL, jump_protocol); // 27
			objSchedulerOrderParameterSet.put("jump_user", jump_user); // 28

			if (history != null) {
				String[] splitHistFields = historyFields.split(";");
				for (int i = 0; i < splitHistFields.length; i++) {
					String key = sosString.parseToString(splitHistFields[i]);
					String value = sosString.parseToString(objSchedulerOrderParameterSet, key).length() == 0 ? sosString.parseToString(arguments,
							"history_entry_".concat(key)) : sosString.parseToString(objSchedulerOrderParameterSet, key);
					hist = hist + (i == 0 ? "" : ";") + value; //
				}

				getLogger().debug9("history entry: " + hist);

				if (transActional) {
					if (transactionalHistoryFile == null)
						transactionalHistoryFile = new ArrayList();
					transactionalHistoryFile.add(hist);
				}
				else {
					history.write(hist);
					history.newLine();
				}
			}

			if (sendSchedulerSignale) {
				String schedulerMessages = "";
				if (sosString.parseToString(arguments, conParamSCHEDULER_MESSAGE).length() > 0) {
					schedulerMessages = sosString.parseToString(arguments, conParamSCHEDULER_MESSAGE);
					schedulerMessages = normalizedFields(schedulerMessages, objSchedulerOrderParameterSet, "%{", "}");
					schedulerMessages = normalizedFields(schedulerMessages, arguments, "%{", "}");
				}
				else {
					String strJobChainName = sosString.parseToString(arguments, conParamSCHEDULER_JOB_CHAIN);
					schedulerMessages = String.format("<add_order job_chain='%1$s'><params>", strJobChainName);

					for (final Entry element : objSchedulerOrderParameterSet.entrySet()) {
						final Map.Entry<String, String> mapItem = element;
						String key = mapItem.getKey().toString();
						schedulerMessages += String.format("<param name='%1$s' value='%2$s'/>", key, mapItem.getValue());
					}
					schedulerMessages = schedulerMessages + "</params></add_order>";
				}

				if (transActional) {
					if (transactionalSchedulerRequestFile == null)
						transactionalSchedulerRequestFile = new ArrayList();
					transactionalSchedulerRequestFile.add(schedulerMessages);
				}
				else {
					sendSchedulerRequest(schedulerMessages);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			getLogger().warn("error occurred writing history entry, cause: " + e.getMessage());
		}
	}

	private String clearCRLF(final String txt) {
		return txt.replaceAll("(\r\n|\r|\n|\n\r|;)", ",");
	}

	private void appendHistoryFile() throws Exception {

		int repeat = 3;
		int repeatIntervall = 1;
		try {

			if (sosString.parseToString(arguments, "history_repeat").length() > 0) {
				try {
					repeat = Integer.parseInt(sosString.parseToString(arguments, "history_repeat"));
				}
				catch (Exception e) {
					repeat = 3;
				}
			}
			if (sosString.parseToString(arguments, "history_repeat_interval").length() > 0) {
				try {
					repeatIntervall = Integer.parseInt(sosString.parseToString(arguments, "history_repeat_interval"));
				}
				catch (Exception e) {
					repeatIntervall = 1;
				}
			}

			boolean hasError = getLogger().hasErrors() || getLogger().hasWarnings();
			if (getHistory() != null) {
				for (int j = 0; transactionalHistoryFile != null && j < transactionalHistoryFile.size(); j++) {
					String hist = sosString.parseToString(transactionalHistoryFile.get(j));
					if (hasError) {
						hist = hist.replaceAll(";success;;", ";error;"
								+ clearCRLF(getLogger().getError() != null && getLogger().getError().length() > 0 ? getLogger().getError()
										: getLogger().getWarning()) + ";");
					}
					history.write(hist);
					history.newLine();
					getLogger().debug9("history entry: " + hist);
				}
			}

		}
		catch (Exception e) {
			getLogger().warn("error occurred writing history, cause: " + e.getMessage());
		}
		finally {

			for (int i = repeat; i > 0; i--) {
				try {
					if (getHistory() != null) {
						getHistory().close();
						// schreibe diese in die globalen history Datei
						if (!existHistoryLockFile()) {
							createHistoryLockFile();
							sos.util.SOSFile.copyFile(getTempHistoryFile(), historyFile, true);
							removeHistoryLockFile();
						}
						if (!tempHistoryFile.delete())
							getLogger().info(tempHistoryFile.getAbsolutePath() + " could not delete");
					}

					break;
				}
				catch (Exception e) {
					if (i == 1)
						getLogger().warn("could not write in History File, cause: " + e.toString());
					Thread.sleep(repeatIntervall * 1000);
				}
			}
			if (tempHistoryFile != null && tempHistoryFile.exists())
				if (!tempHistoryFile.delete())
					getLogger().info(tempHistoryFile.getAbsolutePath() + " could not delete");
		}
	}

	private void sendTransactionalSchedulerRequestFile() throws Exception {
		if (transActional) {
			if (transactionalSchedulerRequestFile != null) {
				for (int i = 0; i < transactionalSchedulerRequestFile.size(); i++) {
					String msg = sosString.parseToString(transactionalSchedulerRequestFile.get(i));
					boolean hasError = getLogger().hasErrors() || getLogger().hasWarnings();
					if (hasError) {
						msg = msg.replaceAll("<param name='status' value='success'/>", "<param name='status' value='error'/>");
						msg = msg.replaceAll("<param name='last_error_message' value=''/>", "<param name='last_error_message' value='"
								+ clearCRLF(getLogger().getError() != null && getLogger().getError().length() > 0 ? getLogger().getError()
										: getLogger().getWarning()) + "'/>");
					}
					sendSchedulerRequest(msg);
				}
			}
		}
	}

	/**
	 * sends a command to the scheduler
	 *
	 * @param msg XML String containing the command
	 * @throws java.lang.Exception
	 */
	public void sendSchedulerRequest(String msg) throws Exception {

		String _host = "";
		int _port = 0;
		DatagramSocket udpSocket = null;
		try {
			_host = sosString.parseToString(arguments, conParamSCHEDULER_HOST);
			if (sosString.parseToString(arguments, conParamSCHEDULER_PORT).length() > 0) {
				_port = Integer.parseInt(sosString.parseToString(arguments, conParamSCHEDULER_PORT));
			}

			if (_host == null || _host.length() == 0) {
				throw new Exception("JobScheduler host name missing.");
			}

			if (_port == 0) {
				throw new Exception("JobScheduler port missing.");
			}

			udpSocket = new DatagramSocket();
			udpSocket.connect(InetAddress.getByName(_host), _port);

			// protocol=udp
			if (msg.indexOf("<?xml") == -1) {
				msg = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>" + msg + "\r\n";
			}

			getLogger().debug9("sending Job Scheduler message: " + msg);

			byte[] commandBytes = msg.getBytes();
			udpSocket.send(new DatagramPacket(commandBytes, commandBytes.length, InetAddress.getByName(_host), _port));
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			getLogger().warn("could not send message to the Job Scheduler, cause " + e.getLocalizedMessage());
		}
		finally {
			if (udpSocket != null) {
				udpSocket.disconnect();
			}
		}
	}

	/**
	 * sends a command to the scheduler
	 *
	 * Überprüft ob alle Parameter correkt angegeben sind
	 * @throws java.lang.Exception
	 */
	private void checkSchedulerRequest() throws Exception {
		String _command = "";
		String _host = "";
		int _port = 0;

		try {

			_host = sosString.parseToString(arguments, conParamSCHEDULER_HOST);
			if (sosString.parseToString(arguments, conParamSCHEDULER_PORT).length() > 0)
				_port = Integer.parseInt(sosString.parseToString(arguments, conParamSCHEDULER_PORT));

			_command = sosString.parseToString(arguments, conParamSCHEDULER_MESSAGE);

			// abbruchbedingung
			if (sosString.parseToString(_host).concat(sosString.parseToString(arguments, conParamSCHEDULER_PORT)).concat(_command).length() == 0) {
				return;
			}
			if (_host == null || _host.length() == 0) {
				throw new RuntimeException("JobScheduler host name missing.");
			}
			if (_port == 0) {
				throw new RuntimeException("JobScheduler port missing.");
			}
			if (sosString.parseToString(arguments, conParamSCHEDULER_JOB_CHAIN).length() == 0) {
				arguments.put(conParamSCHEDULER_JOB_CHAIN, conJobChainNameSCHEDULER_SOSFTP_HISTORY);
			}
			sendSchedulerSignale = true;
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			throw new RuntimeException("error in checkSchedulerRequestParameter(): " + e.toString(), e);
		}
	}

	/**
	 * Alle Platzhalter in der Form %{parametername}  oder ${parametername}  oder %parametername% werden ersetzt mir dem Wert des Parameters,
	 * falls dieser Parametername vorhanden sind.
	 *
	 *
	 *
	 * @param txt
	 * @param prop
	 * @return
	 * @throws Exception
	 */
	private static String normalizedFields(String txt, final Properties prop, final String startPrefix, final String endPrefix) throws Exception {
		try {
			Iterator it = prop.keySet().iterator();
			while (it.hasNext()) {
				String key = sosString.parseToString(it.next());
				if (key.length() > 0) {
					int pos1 = -1;
					int pos2 = -1;
					boolean loop = true;
					while (loop) {

						pos1 = txt.indexOf(startPrefix + key + endPrefix);

						if (pos1 > -1 && txt.indexOf("\\" + startPrefix + key + endPrefix) > -1
								&& txt.indexOf("\\" + startPrefix + key + endPrefix) == pos1 - 1) // ist quottiert?
							pos1 = -1;

						pos2 = pos1 + (startPrefix + key + endPrefix).length();
						if (pos1 > -1 && pos2 > pos1)
							txt = txt.substring(0, pos1) + sosString.parseToString(prop, key) + txt.substring(pos2);

						pos1 = txt.indexOf(startPrefix + key + endPrefix, pos2);

						if (pos1 == -1)
							loop = false;

					}
				}
			}
		}
		catch (Exception e) {
			RaiseException("could not substitute parameters in: " + txt + ", cause: " + e.toString());
		}
		return txt;

	}

	private File getTempHistoryFile() {
		return tempHistoryFile;
	}

	/**
	 * @return Returns the commandDelimiter.
	 */
	protected String getCommandDelimiter() {
		return commandDelimiter;
	}

	private void readJumpSettings() throws Exception {

		this.getBaseParameters();
		String commandScript = "";
		String commandScriptFileName = "";

		try {
			if (getString("jump_command_delimiter").length() > 0) {
				this.setCommandDelimiter(getString("jump_command_delimiter"));
				getLogger().debug1(".. parameter [jump_command_delimiter]: " + this.getCommandDelimiter());
			}
			else {
				this.setCommandDelimiter(DEFAULT_COMMAND_DELIMITER);
			}

			if (getString("jump_command_script").length() > 0) {
				commandScript = getString("jump_command_script");
				getLogger().debug1(".. parameter [jump_command_script]: " + commandScript);
			}

			if (getString("jump_command_script_file").length() > 0) {
				commandScriptFileName = getString("jump_command_script_file");
				getLogger().debug1(".. parameter [jump_command_script_file]: " + commandScriptFileName);
			}

			if (getString(conSettingsJUMP_COMMAND).length() > 0) {
				this.setCommands(getString(conSettingsJUMP_COMMAND).split(this.getCommandDelimiter()));
				getLogger().debug1(".. parameter [jump_command]: " + getString(conSettingsJUMP_COMMAND));
			}

			if (getString("jump_ignore_error").length() > 0) {
				if (getBool("jump_ignore_error")) {
					ignoreError = true;
				}
				else {
					ignoreError = false;
				}
				getLogger().debug1(".. parameter [jump_ignore_error]: " + ignoreError);
			}
			else {
				ignoreError = false;
			}

			if (getString("jump_ignore_signal").length() > 0) {
				if (sosString.parseToBoolean(getString("jump_ignore_signal"))) {
					ignoreSignal = true;
				}
				else {
					ignoreSignal = false;
				}
				getLogger().debug1(".. parameter [jump_ignore_signal]: " + ignoreSignal);
			}
			else {
				ignoreSignal = false;
			}

			if (getString("jump_ignore_stderr").length() > 0) {
				if (getBool("jump_ignore_stderr")) {
					ignoreStderr = true;
				}
				else {
					ignoreStderr = false;
				}
				getLogger().debug1(".. parameter [jump_ignore_stderr]: " + ignoreStderr);
			}
			else {
				ignoreStderr = false;
			}
		}
		catch (Exception e) {
			RaiseException("error occurred processing parameters: " + e.getMessage());
		}
	}

	protected boolean getBool(final String pstrParameterName) {
		boolean flgRet = false;
		try {
			flgRet = sosString.parseToBoolean(arguments.get(pstrParameterName));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return flgRet;
	}

	protected boolean remoteIsWindowsShell(final boolean withConn) throws Exception {
		try {
			if (withConn) {
				if (getSshConnection() == null) {

					readJumpSettings();

					this.getBaseAuthentication();
				}
			}
			return remoteIsWindowsShell();

		}
		catch (Exception e) {
			RaiseException("Failed to check if remote system has windows shell: " + e);

		}
		finally {
			if (withConn) {
				if (this.getSshConnection() != null)
					try {
						this.getSshConnection().close();
						this.setSshConnection(null);
					}
					catch (Exception ex) {
					} // gracefully ignore this error
			}
		}
		return false;
	}

	/**
	 * Authentication-Processing
	 *
	 */
	protected Connection getBaseAuthentication() throws Exception {

		try { // to connect and authenticate
			boolean isAuthenticated = false;
			this.setSshConnection(new Connection(host, port));

			if (proxyHost != null && proxyHost.length() > 0) {
				if (proxyUser != null && proxyUser.length() > 0) {
					this.getSshConnection().setProxyData(new HTTPProxyData(proxyHost, proxyPort));
				}
				else {
					this.getSshConnection().setProxyData(
							new HTTPProxyData(proxyHost, proxyPort, proxyUser, new SOSCommandline().getExternalPassword(proxyPassword, logger)));
				}
			}

			this.getSshConnection().connect();

			if (authenticationMethod.equalsIgnoreCase("publickey")) {
				File authenticationFile = new File(authenticationFilename);
				if (!authenticationFile.exists())
					RaiseException("authentication file does not exist: " + authenticationFile.getCanonicalPath());
				if (!authenticationFile.canRead())
					RaiseException("authentication file not accessible: " + authenticationFile.getCanonicalPath());

				isAuthenticated = this.getSshConnection().authenticateWithPublicKey(user, authenticationFile,
						new SOSCommandline().getExternalPassword(password, logger));
			}
			else
				if (authenticationMethod.equalsIgnoreCase(conParamPASSWORD)) {
					isAuthenticated = this.getSshConnection().authenticateWithPassword(user, new SOSCommandline().getExternalPassword(password, logger));
				}

			if (!isAuthenticated)
				RaiseException("authentication failed [jump_host=" + host + ", jump_port=" + port + ", jump_user:" + user + ", jump_ssh_auth_method="
						+ authenticationMethod + ", jump_ssh_auth_file=" + authenticationFilename);

			return this.getSshConnection();

		}
		catch (Exception e) {
			if (this.getSshConnection() != null)
				try {
					this.getSshConnection().close();
					this.setSshConnection(null);
				}
				catch (Exception ex) {
				} // gracefully ignore this error
			RaiseException(e.getMessage());
		}
		return null;
	}

	/**
	 * Parameter-Processing
	 *
	 */
	protected void getBaseParameters() throws Exception {

		try {
			host = getParam(conParamJUMP_HOST, "");
			if (getString(conParamJUMP_HOST).length() <= 0) {
				RaiseException("no host name or ip address was specified as parameter [jump_host]");
			}
			if (getString("jump_port").length() <= 0) {
				arguments.put("jump_port", "22");
			}
			try {
				port = Integer.parseInt(getString("jump_port"));
				getLogger().debug1(".. parameter [jump_port]: " + port);
			}
			catch (Exception ex) {
				RaiseException("illegal non-numeric value for parameter [jump_port]: " + getString("jump_port"));
			}

			if (getString("jump_user").length() > 0) {
				user = getString("jump_user");
				getLogger().debug1(".. parameter [jump_user]: " + user);
			}
			else {
				RaiseException("no user name was specified as parameter [jump_user]");
			}

			if (getString("jump_password").length() > 0) {
				password = getString("jump_password");
				getLogger().debug1(".. parameter [jump_password]: ********");
			}
			else {
				password = "";
			}

			if (getString("jump_proxy_host").length() > 0) {
				proxyHost = getString("jump_proxy_host");
				getLogger().debug1(".. parameter [jump_proxy_host]: " + proxyHost);
			}
			else {
				proxyHost = "";
			}

			if (getString("jump_proxy_port").length() > 0) {
				try {
					proxyPort = Integer.parseInt(getString("jump_proxy_port"));
					getLogger().debug1(".. parameter [jump_proxy_port]: " + proxyPort);
				}
				catch (Exception ex) {
					RaiseException("illegal non-numeric value for parameter [jump_proxy_port]: " + getString("jump_proxy_port"));
				}
			}
			else {
				proxyPort = 3128;
			}

			if (getString("jump_proxy_user") != null && getString("jump_proxy_user").length() > 0) {
				proxyUser = getString("jump_proxy_user");
				getLogger().debug1(".. parameter [jump_proxy_user]: " + proxyUser);
			}
			else {
				proxyUser = "";
			}

			if (getString("jump_proxy_password").length() > 0) {
				proxyPassword = getString("jump_proxy_password");
				getLogger().debug1(".. parameter [jump_proxy_password]: ********");
			}
			else {
				proxyPassword = "";
			}

			String authMeth = getString("jump_ssh_auth_method");
			if (getString("jump_auth_method").length() > 0)
				authMeth = getString("jump_auth_method");
			else
				authMeth = getString("jump_ssh_auth_method");
			if (authMeth.length() > 0) {
				if (authMeth.equalsIgnoreCase("publickey") || authMeth.equalsIgnoreCase(conParamPASSWORD)) {
					authenticationMethod = authMeth;
					getLogger().debug1(".. parameter [jump_ssh_auth_method]: " + authenticationMethod);
				}
				else {
					RaiseException("invalid authentication method [publickey, password] specified: " + authMeth);
				}
			}
			else {
				authenticationMethod = "publickey";
			}

			String authFile = getString("jump_ssh_auth_file");
			if (authFile.length() > 0) {
				authenticationFilename = authFile;
				getLogger().debug1(".. parameter [jump_ssh_auth_file]: " + authenticationFilename);
			}
			else {
				if (authenticationMethod.equalsIgnoreCase("publickey"))
					RaiseException("no authentication filename was specified as parameter [jump_ssh_auth_file");
			}

		}
		catch (Exception e) {
			RaiseException("error occurred processing parameters: " + e.getMessage());
		}
	}

	protected boolean remoteIsWindowsShell() {
		Session session = null;
		try {
			String checkShellCommand = "echo %ComSpec%";
			getLogger().debug9("Opening ssh session...");
			session = this.getSshConnection().openSession();
			getLogger().debug9("Executing command " + checkShellCommand);
			session.execCommand(checkShellCommand);

			getLogger().debug9("output to stdout for remote command: " + checkShellCommand);
			stdout = new StreamGobbler(session.getStdout());
			stderr = new StreamGobbler(session.getStderr());
			BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(stdout));
			String stdOut = "";
			while (true) {
				String line = stdoutReader.readLine();
				if (line == null)
					break;
				getLogger().debug9(line);
				stdOut += line;
			}
			getLogger().debug9("output to stderr for remote command: " + checkShellCommand);
			// Beide StreamGobbler müssen hintereinander instanziiert werden
			// InputStream stderr = new StreamGobbler(this.getSshSession().getStderr());
			BufferedReader stderrReader = new BufferedReader(new InputStreamReader(stderr));
			while (true) {
				String line = stderrReader.readLine();
				if (line == null)
					break;
				getLogger().debug1(line);
			}
			if (stdOut.indexOf("cmd.exe") > -1) {
				getLogger().debug3("Remote shell is Windows shell.");
				return true;
			}
		}
		catch (Exception e) {
			try {
				getLogger().warn("Failed to check if remote system is windows shell: " + e);
			}
			catch (Exception es) {
				System.out.println(" Failed to check if remote system is windows shell: " + e);
			}
		}
		finally {
			if (session != null)
				try {
					session.close();
				}
				catch (Exception e) {
					try {
						getLogger().warn("Failed to close session: " + e);
					}
					catch (Exception ea) {
						System.out.println(" Failed to close session: " + e);
					}
				}
		}
		return false;
	}

	/**
	 * @param commandDelimiter The commandDelimiter to set.
	 */
	protected void setCommandDelimiter(final String commandDelimiter) {
		this.commandDelimiter = commandDelimiter;
	}

	/**
	 * This thread consumes output from the remote server puts it into
	 * fields of the main class
	 */

	class RemoteConsumer extends Thread {

		private final StringBuffer	sbuf;
		private boolean				writeCurrentline	= false;
		private final InputStream	stream;
		boolean						end					= false;

		private RemoteConsumer(final StringBuffer buffer, final boolean writeCurr, final InputStream str) {
			sbuf = buffer;
			writeCurrentline = true;
			stream = str;
		}

		private void addText(final byte[] data, final int len) {
			lasttime = System.currentTimeMillis();
			String outstring = new String(data).substring(0, len);
			sbuf.append(outstring);
			if (writeCurrentline) {
				int newlineIndex = outstring.indexOf(conNewLine);
				if (newlineIndex > -1) {
					String stringAfterNewline = outstring.substring(newlineIndex);
					currentLine = stringAfterNewline;
				}
				else
					currentLine += outstring;
			}
		}

		@Override
		public void run() {
			byte[] buff = new byte[64];
			try {
				while (!end) {
					buff = new byte[8];
					int len = stream.read(buff);
					if (len == -1)
						return;
					addText(buff, len);
				}
			}
			catch (Exception e) {
			}
		}

		public synchronized void end() {
			end = true;
		}
	}

	protected void readSettings(final boolean checkJumpArguments) throws Exception {
		try {

			// TODO wieso ist das auskommentiert ? kb 2010-05-18

			/*try {
				if(getSettingsFile() != null && getSettingsFile().length() > 0)
					this.setSettings(new SOSProfileSettings( this.getSettingsFile(), this.getSettingsSection(), this.getLogger()));
			} catch (Exception e) {
				RaiseException("error occurred retrieving settings: " + e.getMessage());
			}*/

			mergeSettings();

			if (checkJumpArguments)
				hasJumpArguments();

		}
		catch (Exception e) {
			RaiseException("error in  " + sos.util.SOSClassUtil.getMethodName() + " , cause: " + e);
		}
	}

	public static String getVersion() {
		String version = JSVersionInfo.conVersionNumber + " " + JSVersionInfo.conVersionDate + " / " + JSVersionInfo.conCopyrightText;
		// String version = major + "." + minor + "." + bugFix;
		return version;
	}

	/**
	 * Different file operations within one session
	 * For different file sets multiple parameter sets should be used:
	 *
	 * a)      Use case:
	 * -          3 files *.doc
	 * -          2 files *.csv
	 * -          For each file set a different replacing/replacement scheme should be applied
	 *
	 * Approach:
	 *
	 *   -          different file_spec paramters should be used and each file set should be assigned a set of different parameters
	 *   -          changes to the initial parameters by a parameter set have local scope, i.e. a second parameter set makes use oft he initial parameters,
	 *              and should not consider changes by a previous parameter set
	 *
	 *              sosftp.sh –file_spec=[regex1]:[regex2] –transfer_mode=ascii:binary
	 *              sosftp.sh –file_spec=[regex1] –file_spec2=[regex2]::[parameter_set2] –parameter_set2=“transfer_mode=ascii::remote_directory=/tmp“
	 *
	 *
	 * @param arg
	 * @return
	 * @throws Exception
	 */

	private static ArrayList getFileOperations(final Properties arg) throws Exception {
		ArrayList list = new ArrayList();
		String key = "";
		String value = "";
		Iterator it = null;
		listOfSuccessTransfer = new ArrayList();
		listOfErrorTransfer = new ArrayList();

		try {
			arg.put("index", "0");
			list.add(arg.clone());
			arg.remove("index");

			it = arg.keySet().iterator();
			while (it.hasNext()) {
				key = sosString.parseToString(it.next());
				value = sosString.parseToString(arg, key);

				// unterschiedlich Parametrisierung
				if (Check4MultipleFileSpecs(key)) { // ein multiples file_spec ist angegeben
					String[] split = value.split("::"); // –file_spec2=[regex2]::[parameter_set2]
					Properties newArg = (Properties) arg.clone();
					newArg.put(conSettingFILE_SPEC, split[0]);
					logger.info("found multiple file_spec parameter. Actual is : " + split[0]);
					noJumpParameter.add(key); // dürfen dem jump host nicht übergeben werden

					if (split.length > 1) {
						noJumpParameter.add(split[1]); // dürfen dem jump host nicht übergeben werden
						String newParameterSet = sosString.parseToString(arg, split[1]);
						String[] splitParams = newParameterSet.split("::");// Bsp.
																			// –parameter_set2=“transfer_mode=ascii:remote_directory=/tmp“
						for (String splitParam2 : splitParams) {
							String s = sosString.parseToString(splitParam2);
							if (s.length() > 0) {
								String[] splitParam = s.split("=");
								newArg.put(sosString.parseToString(splitParam[0]), splitParam.length == 1 ? "" : sosString.parseToString(splitParam[1]));
							}
						}
					}
					String index = key != null && key.length() > conSettingFILE_SPEC.length() ? key.substring(conSettingFILE_SPEC.length()) : "";
					newArg.put("index", index);// hilfsparameter, wird später wieder gelöscht
					list.add(newArg);
				}
				else {
					if (key.equalsIgnoreCase("transfer_success")) {// ein neues file_spec ist angegeben
						Properties newArg = (Properties) arg.clone();
						String newParameterSet = sosString.parseToString(arg.get(value));
						String[] splitParams = newParameterSet.split("::");// Bsp.
																			// –parameter_set2=“transfer_mode=ascii:remote_directory=/tmp“
						for (String splitParam2 : splitParams) {
							String s = sosString.parseToString(splitParam2);
							String[] splitParam = s.split("=");
							newArg.put(sosString.parseToString(splitParam[0]), splitParam.length == 1 ? "" : sosString.parseToString(splitParam[1]));
						}
						listOfSuccessTransfer.add(newArg);
					}
					else {
						if (key.equalsIgnoreCase("transfer_error")) {// ein neues file_spec ist angegeben

							Properties newArg = (Properties) arg.clone();
							String newParameterSet = sosString.parseToString(arg.get(value));
							String[] splitParams = newParameterSet.split("::");// Bsp.
																				// –parameter_set2=“transfer_mode=ascii:remote_directory=/tmp“
							for (String splitParam2 : splitParams) {
								String s = sosString.parseToString(splitParam2);
								String[] splitParam = s.split("=");
								newArg.put(sosString.parseToString(splitParam[0]), splitParam.length == 1 ? "" : sosString.parseToString(splitParam[1]));
							}
							listOfErrorTransfer.add(newArg);
						}
					}
				}
			}
			list = prepareInstall(list);
		}
		catch (Exception e) {
			RaiseException("error in  " + sos.util.SOSClassUtil.getMethodName() + " , cause: " + e);
		}

		list = sortListAtFileSpecNum(list);
		return list;
	}

	/**
	 * Different file operations within one session
	 * For different file sets multiple parameter sets should be used:
	 *
	 * a)      Use case:
	 * -          3 files *.doc
	 * -          2 files *.csv
	 * -          For each file set a different replacing/replacement scheme should be applied
	 *
	 * Approach:
	 *
	 *   -          different file_spec paramters should be used and each file set should be assigned a set of different parameters
	 *   -          changes to the initial parameters by a parameter set have local scope, i.e. a second parameter set makes use oft he initial parameters,
	 *              and should not consider changes by a previous parameter set
	 *
	 *              sosftp.sh –file_spec=[regex1]:[regex2] –transfer_mode=ascii:binary
	 *              sosftp.sh –file_spec=[regex1] –file_spec2=[regex2]::[parameter_set2] –parameter_set2=“transfer_mode=ascii::remote_directory=/tmp“
	 *
	 *
	 *              In addition to what is stated for the parameter file_spec additional parameters
	        can be specified for up to 9 file sets like this:

	        -file_spec=.*\.gif$ -local_dir=/tmp/1 -remote_dir=/tmp/1
	        -file_spec2=.*\.exe$::param_set_2 -param_set_2="transfer_mode=binary::remote_dir=/tmp/2::local_dir=/tmp/2"

	        Within the file_spec2 parameter value the regular expression is separated by ::
	        from the name of a file set. This name can freely be chosen, it consists of the
	        characters 0-9, a-z and _.

	        The name of the file set is used as a separate parameter in the command line.
	        This parameter is assigend the list of parameters that should be valid for the specific file set.
	        Therefore the names and values of individual parameters are specified in the form
	        name=value::name2=value2 .... Such parameters are exclusively valid for the specific file set.

	        The above sample causes all files with the extension .gif to be transferred from
	        the local directory /tmp/1 to a directory with the same name on the target host.
	        For files with the extension .exe a file set param_set_2 is specified
	        that contains parameters that are specific for this file set, as binary transfer and
	        different source and target directories.

	        Please, consider that parameter file sets cannot specify parameters that control the
	        connection to a target host, i.e. all files are transferred between the same local and remote hosts.
	        However, the transfer direction can be changed, e.g. by specifying a different operation
	        parameter for a file set.
	*/
	private static boolean Check4MultipleFileSpecs(final String pstrRegExp) {
		boolean flgMultipleFileSpecs = false;
		int intFileSpecSize = conSettingFILE_SPEC.length();
		if (pstrRegExp.startsWith(conSettingFILE_SPEC) && pstrRegExp.length() > intFileSpecSize) {
			String strFileSpecIndex = pstrRegExp.substring(intFileSpecSize); // format is file_spec_111
			if (strFileSpecIndex.startsWith("_")) {
				strFileSpecIndex = strFileSpecIndex.substring(1);
			}
			int intIndex = 0;
			try {
				intIndex = new Integer(strFileSpecIndex);
			}
			catch (NumberFormatException e) {
			}
			if (intIndex > 0) {
				flgMultipleFileSpecs = true;
			}
		}
		return flgMultipleFileSpecs;
	}

	/**
	 * Parameter list wird sortiert nach file_spec_[num]
	 * @param list
	 * @return list
	 */
	private static ArrayList sortListAtFileSpecNum(final ArrayList list) throws Exception {
		ArrayList sort = new ArrayList();
		try {
			// sortiert die Arrayliste nach index
			sort = sortArrayList(list, "index");

			// hilfsvariable index löschen
			for (int i = 0; i < sort.size(); i++) {
				Properties p = (Properties) list.get(i);
				if (sosString.parseToString(p.get("index")).length() > 0) {
					p.remove("index");
				}
			}

		}
		catch (Exception e) {
			RaiseException("error in  " + sos.util.SOSClassUtil.getMethodName() + " , cause: " + e);
		}
		return sort;
	}

	public static ArrayList sortArrayList(ArrayList list, final String key) throws Exception {
		try {
			Properties h = null;
			Object[] o = null;
			int pos = 0;
			ArrayList newList = new ArrayList();
			try {
				o = new Object[list.size()];
				for (int i = 0; i < list.size(); i++) {
					h = (Properties) list.get(i);
					o[i] = h.get(key) + "_@_" + String.valueOf(i);
				}

				Arrays.sort(o);

				for (Object element : o) {
					pos = Integer.parseInt(element.toString().substring(element.toString().indexOf("_@_") + 3));
					newList.add(list.get(pos));
				}

				list = newList;

			}
			catch (Exception e) {
				throw e;
			}
			return list;
		}
		catch (Exception e) {
			RaiseException("..error in " + SOSClassUtil.getMethodName() + " " + e);
		}
		return null;
	}

	/**
	 * Vorbereitung für operation=install. Es findet zwei installationen - entspricht zwei send Aufufe statt.
	 *
	 *
	 * @param list
	 * @return
	 * @throws Exception
	 */
	private static ArrayList prepareInstall(final ArrayList list) throws Exception {
		ArrayList newList = new ArrayList();
		for (int i = 0; i < list.size(); i++) {
			Properties p = (Properties) list.get(i);
			newList.add(p);
			if (p.containsKey(conSettingOPERATION) && sosString.parseToString(p.get(conSettingOPERATION)).equals("install")) {

				Properties newp = (Properties) p.clone();
				newp.put(conSettingOPERATION, "install_doc");
				newList.add(newp);

			}
		}
		return newList;

	}

	/**
	 * @return the writeBannerHeader
	 */
	public boolean isWriteBannerHeader() {
		return writeBannerHeader;
	}

	/**
	 * @param writeBannerHeader the writeBannerHeader to set
	 */
	public void setWriteBannerHeader(final boolean writeBannerHeader) {
		this.writeBannerHeader = writeBannerHeader;
	}

	private static String getUsage() {
		String usage = "Usage: sos.net.SOSFTPCommand -operation= -settings= -profile= -verbose=" + conNewLine
				+ "        -operation   =[send|receive|execute|remove|install]   FTP operation" + conNewLine
				+ "        -settings    =[file]                   Configuration file" + conNewLine
				+ "        -profile     =[profile]                 Section/Profile for FTP settings" + conNewLine
				+ "                                           in configuration file" + conNewLine
				+ "        -verbose     =[1..9]                    Verbosity level" + conNewLine
				+ "        -log_filename=[filename]               log file name    ";

		return usage;
	}

	private static boolean transfer(final ArrayList extractArguments, String operation, Properties arg, boolean pflgRC, final boolean continueOnError)
			throws Exception {
		try {
			for (int i = 0; i < extractArguments.size(); i++) {
				arg = (Properties) extractArguments.get(i);
				if (i > 0) {
					operation = arg.getProperty(conSettingOPERATION);
					sosConfiguration.setArguments(arg);

					// sosConfiguration.checkConfigurationItems(REQUIRED_DEFAULT_PARAMETERS_FILENAME);

					if (sosString.parseToString(arg.get("current_pid")).length() == 0 && sosString.parseToString(System.getProperty("pid")).length() > 0)
						arg.put("pid", sosString.parseToString(System.getProperty("pid")));

					if (banner)
						logger.info("SOSFTP-I-0440: Transfer for file set no. " + i + " is being completed");
					else
						logger.debug1("SOSFTP-I-0441: All files have been transferred successfully. Transaction for file set no. " + i + " is being completed");

				}

				if (sosString.parseToBoolean(arg.get("testmode"))) {
					logger.info("SOSFTP-I-0442: Test mode is active, no transfers are effected");
					arg.put("skip_transfer", "yes");
					arg.put(conSettingREMOVE_FILES, "no");
				}

				if (operation.equalsIgnoreCase(conOperationSEND)) {
					ftpCommand = new sos.net.sosftp.SOSFTPCommandSend(logger, arg);
					pflgRC = ftpCommand.send();
				}
				else
					if (operation.equalsIgnoreCase(conOperationRECEIVE)) {
						ftpCommand = new sos.net.sosftp.SOSFTPCommandReceive(logger, arg);
						pflgRC = ftpCommand.receive();
					}
					else
						if (operation.equalsIgnoreCase(conOperationEXECUTE)) {
							ftpCommand = new sos.net.sosftp.SOSFTPCommandSSH(logger, arg);
							pflgRC = ftpCommand.execute();
						}
						else
							if (operation.equalsIgnoreCase("make_temp_directory")) {
								ftpCommand = new sos.net.sosftp.SOSFTPCommandSend(logger, arg);
								pflgRC = ftpCommand.makeTempDirectory();
							}
							else
								if (operation.equalsIgnoreCase("remove_temp_directory")) {
									ftpCommand = new sos.net.sosftp.SOSFTPCommandSend(logger, arg);
									pflgRC = ftpCommand.removeTempDirectory();
								}
								else
									if (operation.equalsIgnoreCase(conOperationDELETE) || operation.equalsIgnoreCase(conOperationREMOVE)) {
										if (operation.equalsIgnoreCase(conOperationDELETE)) {
											arg.put(conSettingOPERATION, conOperationREMOVE);
										}
										ftpCommand = new sos.net.sosftp.SOSFTPCommandReceive(logger, arg);
										pflgRC = ftpCommand.remove();
									}
									else
										if (operation.equalsIgnoreCase("install")) {
											ftpCommand = new sos.net.sosftp.SOSFTPCommandSend(logger, arg);
											pflgRC = ftpCommand.install();
										}
										else
											if (operation.equalsIgnoreCase("install_doc")) {
												ftpCommand = new sos.net.sosftp.SOSFTPCommandSend(logger, arg);
												pflgRC = ftpCommand.install();
											}
											else
												if (operation.equalsIgnoreCase("delete_local_files")) {
													// wird intern verwendet
													ftpCommand = new sos.net.sosftp.SOSFTPCommandSend(logger, arg);
													pflgRC = ftpCommand.deleteLocalFiles();
												}
												else
													if (operation.equalsIgnoreCase("rename_local_files")) {
														// wird intern verwendet
														ftpCommand = new sos.net.sosftp.SOSFTPCommandReceive(logger, arg);
														pflgRC = ftpCommand.renameAtomicSuffixTransferFiles();
													}
													else {
														System.out.println(getUsage());
														RaiseException("[ERROR] no valid operation was specified, use send|receive|remove|execute|install: "
																+ operation);
													}
				if (logger.hasWarnings()) {
					ftpCommand.getArguments().put(conParameterSTATUS, "error");
				}
				else {
					ftpCommand.getArguments().put(conParameterSTATUS, conParamValueSUCCESS);
				}
				if (sosString.parseToString(logger.getWarning()).length() > 0 || sosString.parseToString(logger.getError()).length() > 0) {
					ftpCommand.getArguments().put("last_error", logger.getWarning() + " " + logger.getError());
				}
				else {
					ftpCommand.getArguments().put(conParameterSTATUS, conParamValueSUCCESS);
					ftpCommand.getArguments().put("last_error", "");
				}
				if (ftpCommand != null) {
					if (banner)
						logger.info(normalizedPassword(ftpCommand.getBanner(false)));
					else
						logger.debug(normalizedPassword(ftpCommand.getBanner(false)));
				}

				if ((logger.hasErrors() || logger.hasWarnings()) && !continueOnError) {
					// break;
					// RaiseException();
					return false;
				}
			}

			return pflgRC;
		}
		catch (Exception e) {
			e.printStackTrace();
			logger.warn("error in " + SOSClassUtil.getMethodName() + " cause: " + e.toString());
			return false;
		}
	}

	private static Properties extractArguments(final String[] args) throws Exception {
		Properties arg = new Properties();
		try {

			String filename = "";
			// Argumente
			for (String arg2 : args) {

				String[] split = arg2.split("=");

				String key = split[0].startsWith("-") ? split[0].substring(1) : split[0];
				if (split.length == 1 && !split[0].startsWith("-")) {
					if (key.trim().length() > 0)
						logger.debug1("file name specified as argument: " + key);
					filename = filename + key + ";";
				}
				else {
					arg.put(key.toLowerCase(), arg2.substring(arg2.indexOf("=") + 1));
				}
			}

			if (filename != null && filename.length() > 0) {
				filename = filename.replaceAll(conRegExpBackslash, "/");
				filename = filename.endsWith(";") ? filename.substring(0, filename.length() - 1) : filename;
				arg.put(conSettingFILE_PATH, filename);
			}

			return arg;
		}
		catch (Exception e) {
			logger.warn("[ERROR] could not process arguments, cause: " + e.getMessage());
			throw e;
		}
	}

	public static void createLoggerObject(final String logFile, final int logLevel) throws Exception {
		if (sosString.parseToString(logFile).length() > 0)
			logger = new SOSStandardLogger(logFile, logLevel);
		else
			logger = new SOSStandardLogger(logLevel);
	}

	private static boolean onlyVersion(final Properties arg) throws Exception {

		if (arg.containsKey("version")) {
			if (!arg.containsKey(conSettingOPERATION)) {
				logger.info("sosftp version \"" + getVersion() + "\"");
				return true;
			}
		}
		return false;
	}

	protected boolean deleteLocalFiles() throws Exception {

		ArrayList transActionalLocalFiles = new ArrayList();
		String error = "";
		if (arguments.containsKey("files"))
			transActionalLocalFiles = (ArrayList) arguments.get("files");

		for (int i = 0; i < transActionalLocalFiles.size(); i++) {
			File localRemFile = null;
			if (transActionalLocalFiles.get(i) instanceof File)
				localRemFile = (File) transActionalLocalFiles.get(i);
			else
				localRemFile = new File(sosString.parseToString(transActionalLocalFiles.get(i)));

			if (!localRemFile.delete()) {
				error = error + localRemFile.getAbsolutePath() + ";";
			}
			else {
				this.getLogger().debug1("removing file: " + localRemFile.getAbsolutePath());
			}
			if (error != null && error.length() > 0) {
				getLogger().warn("..error occurred, could not remove local file: " + error);
				return false;
			}

		}
		return true;
	}

	public boolean renameAtomicSuffixTransferFiles() throws Exception {

		try {
			ArrayList transActionalLocalFiles = new ArrayList();
			String atomicSuffix = getString("atomic_suffix");

			if (arguments.containsKey("files"))
				transActionalLocalFiles = (ArrayList) arguments.get("files");

			for (int i = 0; i < transActionalLocalFiles.size(); i++) {
				String filename = sosString.parseToString(transActionalLocalFiles.get(i));
				if (filename.trim().length() > 0) {
					File f = new File(filename);
					File nf = new File(filename.substring(0, filename.lastIndexOf(atomicSuffix)));

					if (nf.exists()) {
						getLogger().debug9(nf.getCanonicalPath() + " exists and will be replaced");
						nf.delete();
					}
					getLogger().debug("..rename " + f.getCanonicalPath() + " to " + nf.getName());
					if (!f.renameTo(nf))
						RaiseException("could not rename temporary file [" + f.getCanonicalPath() + "] to: " + nf.getAbsolutePath());
				}
			}

			return true;
		}
		catch (Exception e) {
			getLogger().warn("error in  " + sos.util.SOSClassUtil.getMethodName() + " , cause: " + e);
			return false;
		}
	}

	private static boolean doPostTransactionalOnSuccess() {

		try {
			boolean rc = true;
			banner = false;// es sollen keine banner mehr geschrieben werden
			if (listOfSuccessTransfer.size() > 0) {
				Properties p = (Properties) listOfSuccessTransfer.get(0);
				String operation = sosString.parseToString(p.get(conSettingOPERATION));
				rc = transfer(listOfSuccessTransfer, operation, p, true, false);
			}
			banner = true;
			if (!rc) {
				RaiseException("All files have been transferred successfully, however, the transaction could not be completed");
			}
			return true;
		}
		catch (Exception e) {
			try {
				logger.warn("error in doPostTransactionalOnSuccess, cause: " + e.toString());
			}
			catch (Exception ex) {
			}
			doPostTransactionalOnError();
			return false;
		}
	}

	private static boolean doPostTransactionalOnError() {
		try {
			boolean rc = false;
			banner = false;// es sollen keine banner mehr geschrieben werden
			if (listOfErrorTransfer.size() > 0) {
				Properties p = (Properties) listOfErrorTransfer.get(0);
				String operation = sosString.parseToString(p.get(conSettingOPERATION));
				transfer(listOfErrorTransfer, operation, p, rc, true);
			}
		}
		catch (Exception e) {
			try {
				logger.warn("error in doPostTransactionalOnError, cause: " + e.toString());
			}
			catch (Exception ex) {
			}
		}
		finally {
			banner = true;
		}
		return false;
	}

	protected static String normalizedPassword(String str) {
		try {

			if (str == null || str.trim().length() == 0)
				return "";

			// alle möglichen Passwortnamen, die gex't werden muss
			ArrayList namesOfPassword = sosConfiguration.getPasswordnames();
			if (!namesOfPassword.contains(conParamPASSWORD))
				namesOfPassword.add(conParamPASSWORD);

			for (int i = 0; i < namesOfPassword.size(); i++) {
				String pw = sosString.parseToString(namesOfPassword.get(i));
				if (pw.trim().length() == 0)
					continue;

				int pos1 = str.indexOf(pw + "=");
				if (pos1 > -1) {
					int pos2 = str.indexOf(" ", pos1);
					if (pos2 == -1 || pos1 > pos2)
						pos2 = str.length();
					str = str.substring(0, pos1) + pw + "=***** " + str.substring(pos2);
				}
				pos1 = str.indexOf("-ssh_auth_file=\"filecontent:");
				;
				if (pos1 > -1) {

					int pos2 = -1;

					if (pos1 == -1)
						return str;
					pos2 = str.indexOf("-----END DSA PRIVATE KEY-----", pos1) + "-----END DSA PRIVATE KEY-----".length() + 2;

					if (pos2 == -1 || pos1 > pos2)
						pos2 = str.length();
					str = str.substring(0, pos1) + "-ssh_auth_file=\"filecontent:*****" + str.substring(pos2);
				}
			}
			return str;
		}
		catch (Exception e) {

			return "";
		}
	}

	/**
	 * Ermittelt die PID.
	 * Der PID wird ermittelt, wenn historien Einträge erwünscht sind und
	 * nicht im Testmodus ist.
	 *
	 * @return
	 * @throws Exception
	 */
	public String getPids() throws Exception {
		String GETPIDSEXE = "";

		if (getBool("testmode"))
			return "";
		if (System.getProperty("os.name").toLowerCase().indexOf("wind") > -1) {
			if (GETPIDSEXE == null || GETPIDSEXE.length() == 0) {

				if (new File("getParentId.exe").exists()) {
					GETPIDSEXE = new File("getParentId.exe").getCanonicalPath();
				}
				else {
					getLogger().debug5("creating getParentId.exe ");

					// holt aus einer Bibliothek (jar File) die Datei und kopiert diese in das Verzeichnis
					java.net.URL url = ClassLoader.getSystemResource("getParentId.exe");
					getLogger().debug1("found url=" + url);
					InputStream in = getClass().getClassLoader().getSystemResourceAsStream("getParentId.exe");// aus Klassenpfad holen
					getLogger().debug9("get InputStream to create new getParentId.exe=" + in);
					if (in == null) {
						getLogger().debug9("try again to get InputStream to create new getParentId.exe=" + in);
						in = getClass().getClassLoader().getResourceAsStream("getParentId.exe");// aus der Bibliothel holen
						getLogger().debug9("InputStream is =" + in);
					}
					// /
					if (in == null) {
						// RaiseException ("missing executable File getParentId.exe in Library sos.net.jar");
					}
					else {
						OutputStream out = null;
						byte[] buffer = new byte[1024];
						try {

							out = new FileOutputStream("getParentId.exe", false);
							while (true) {
								synchronized (buffer) {
									int amountRead = in.read(buffer);
									if (amountRead == -1) {
										break;
									}
									out.write(buffer, 0, amountRead);
								}
							}
						}
						finally {
							if (in != null) {
								in.close();
							}
							if (out != null) {
								out.close();
							}
						}
						return getPids();
					}

				}
			}
		}
		String pid = getPID(GETPIDSEXE);

		if (pid != null && pid.length() > 0) {
			logger.debug1("current_pid is " + pid);
		}
		return pid;
	}

	/**
	 *
	 * Diese Methode liefert die aktuelle PID.
	 *
	 * Das Betriebssystem Windows brauch die Anwendung getpids.exe.
	 * getpids.exe liefert die aktuelle PID.
	 * Diese Anwendung kann von der Seite http://www.scheibli.com/projects/getpids/getpids-1.00.zip heruntergeladen werden.
	 *
	 * @param GETPIDEXE -> Anwendung
	 * @return String -> liefert die PID
	 * @throws IOException
	 * TODO: später vielleicht daraus einen statischen Method in SOS UTIL
	 */
	public String getPID(final String GETPIDEXE) throws IOException {
		String pid = "";

		String cmd[];
		if (System.getProperty("os.name").toLowerCase().indexOf("wind") == -1) {
			cmd = new String[] { "/bin/sh", "-c", "echo $$ $PPID" };
		}
		else {
			if (GETPIDEXE == null || GETPIDEXE.length() == 0)
				throw new IOException("executables Files getpids.exe or getParentId.exe not found. Check Installation.");

			cmd = new String[] { GETPIDEXE };
		}
		if (cmd != null) {
			Process p = Runtime.getRuntime().exec(cmd);

			InputStream inputstream = p.getInputStream();
			InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
			BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
			String line;
			if ((line = bufferedreader.readLine()) != null) {

				StringTokenizer stok = new StringTokenizer(line);
				if (System.getProperty("os.name").toLowerCase().indexOf("wind") == -1) {
					stok.nextToken(); // this is pid of the process we spanned
				}
				pid = stok.nextToken();
			}

			if (pid != null)
				System.setProperty("pid", pid); // NOI18N

		}

		return pid;
	}

	private static String[] mergeSchedulerParamsAndArgumenst(final String[] args) throws Exception {

		int index = -1;
		String filename = "";
		for (String arg2 : args) {
			String arg = arg2;
			int pos0 = 1;
			int pos1 = arg.indexOf("=");
			if (pos1 == -1)
				pos1 = arg.length();

			if (!sosString.parseToString(arg).startsWith("-") && sosString.parseToString(arg).indexOf("=") == -1) {
				filename = filename.length() > 0 ? filename + ";" + arg : arg;
			}
			else {
				String key = arg.substring(pos0, pos1);
				String value = arg.length() == pos1 ? "" : arg.substring(pos1 + 1);
				schedulerParams.put(key, value);
			}
		}

		if (sosString.parseToString(filename).trim().length() > 0)
			schedulerParams.put(conSettingFILE_PATH, filename);

		String[] retVal = new String[schedulerParams.size()];
		Iterator it = schedulerParams.keySet().iterator();

		while (it.hasNext()) {
			Object key = it.next();
			Object val = schedulerParams.get(key);
			retVal[++index] = "-" + sosString.parseToString(key) + "=" + sosString.parseToString(val);
		}

		return retVal;
	}

	/**
	 * Konvertiert ConfigurationsItem[] zu Properties.
	 *
	 * Berücksichtigen der include Parametern
	 *
	 *
	 * @return
	 * @throws Exception
	 */
	private static Properties convertParameters() throws Exception {
		try {
			Properties arg_s = sosConfiguration.getParameterAsProperties();
			if (sosString.parseToString(arg_s.get("settings")).length() > 0 && sosString.parseToString(arg_s.get("include")).length() > 0) {
				sos.configuration.SOSConfiguration config_ = new sos.configuration.SOSConfiguration(sosString.parseToString(arg_s.get("settings")),
						sosString.parseToString(arg_s.get("include")), logger);
				// sos.configuration.SOSConfigurationItem[] items = config_.checkConfigurationItems(REQUIRED_DEFAULT_PARAMETERS_FILENAME);
				SOSConfigurationItem[] items = config_.checkConfigurationItems(null);
				arg_s.putAll(config_.getParameterAsProperties());
			}

			// Löschen der Argumente, weil dieser in sos.util.configuration.Configuration ausgelesen wurden
			arg_s.remove("settings");
			arg_s.remove("profile");
			arg_s.remove("include");
			arg_s = substituteEnvironment(arg_s);
			return arg_s;
		}
		catch (Exception e) {
			RaiseException("error in " + sos.util.SOSClassUtil.getMethodName() + ": cause: " + e.toString());
		}
		return null;
	}

	private static String checkOperation() throws Exception {
		try {
			String operation = sosConfiguration.getConfigurationItemByName(conSettingOPERATION).getValue();

			if (sosString.parseToString(operation).length() == 0) {
				logger.warn(getUsage());
				RaiseException("missing command line parameter: operation");
			}
			return operation;
		}
		catch (Exception e) {
			RaiseException("error in " + sos.util.SOSClassUtil.getMethodName() + ": cause: " + e.toString());
		}
		return null;
	}

	/**
	 *
	 * @return boolean
	 */
	public boolean transfer() {

		boolean rc = false;
		String operation = "";

		listOfSuccessTransfer = new ArrayList();
		listOfErrorTransfer = new ArrayList();

		try {
			Properties env = getEnvVars(logger); // gleich am anfang, weil scheduler_params aus der Umgebungsvariable gelesen wird

			env.putAll(schedulerParams);

			operation = checkOperation();// operation value is given?
			Properties arg_s = new Properties();
			arg_s.putAll(env);
			arg_s.putAll(convertParameters()); // convert ConfigurationItem[] to Properties

			ArrayList extractArguments = getFileOperations(arg_s); // Different file operations within one session

			try {
				banner = true;
				rc = transfer(extractArguments, operation, arg_s, rc, false);

				if (rc) {
					rc = doPostTransactionalOnSuccess();
				}
				else {
					rc = doPostTransactionalOnError();
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				banner = true;
				throw e;
			}
		}
		catch (Exception e) {
			try {
				if (ftpCommand != null) {
					if (banner)
						logger.info(ftpCommand.getBanner(false));
					else
						logger.debug(ftpCommand.getBanner(false));
				}
				else {
					System.out.println(e.toString());
				}
			}
			catch (Exception x) {
				System.out.println(e.toString());
			}
			doExit(1);
		}
		finally {
			try {
				if (ftpCommand != null) {
					ftpCommand.appendHistoryFile();
					transactionalHistoryFile = null;
					ftpCommand.sendTransactionalSchedulerRequestFile();
				}
				logger.close();
			}
			catch (Exception e) {
				System.out.println(e.toString());

			}
		}
		return rc;
	}

	/**
	 * Send/Receive files by FTP/SFTP and execute commands by SSH
	 * see sosftp.xml
	 */

	private static int getIntArg(final Properties arg, final String strKey, final int pintDefault) {

		int intI = pintDefault;
		if (arg.containsKey(strKey)) {
			String strT = arg.getProperty(strKey);
			intI = Integer.valueOf(strT);
		}

		return intI;
	}

	private static String getStringArg(final Properties arg, final String strKey, final String pstrDefault) {

		String strT = pstrDefault;
		if (arg.containsKey(strKey)) {
			strT = arg.getProperty(strKey);
		}

		return strT;
	}

	public static void main(String[] args) {

		String conSVNVersion = "$Id: SOSFTPOptions.java 17481 2012-06-29 15:40:36Z kb $";

		boolean rc = false;
		int logLevel = 0;
		String logFile = "";
		String settingsFile = "";
		String settingsSection = "";
		String operation = "";

		listOfSuccessTransfer = new ArrayList();
		listOfErrorTransfer = new ArrayList();

		try {
			String version = "SOSFTP - " + JSVersionInfo.conVersionNumber + " " + JSVersionInfo.conVersionDate + " / " + JSVersionInfo.conCopyrightText;
			System.out.println(version);
			System.out.println(conSVNVersion);
			objLoggerHelper = new Log4JHelper("./log4j.properties");
			objLogger = Logger.getRootLogger();

			objLogger.info(version); // $NON-NLS-1$
			objLogger.info(conSVNVersion); // $NON-NLS-1$
			// objLogger.debug(version);

			Properties env = getEnvVars(logger); // gleich am anfang, weil scheduler_params aus der Umgebungsvariable gelesen wird
			args = mergeSchedulerParamsAndArgumenst(args);
			Properties arg = extractArguments(args);
			arg.putAll(schedulerParams);

			SOSArguments arguments = null;
			try {
				arguments = new SOSArguments(args, true);
				operation = arguments.as_string("-operation=", "");
				settingsFile = arguments.as_string("-settings=", "");
				settingsSection = arguments.as_string("-profile=", "");
				logLevel = arguments.as_int("-verbose=", SOSLogger.INFO);
				logFile = arguments.as_string("-log_filename=", "");
			}
			catch (Exception e) {
				e.printStackTrace(System.err);
				System.err.println("[ERROR] could not init SOSArguments, cause: " + e.getMessage());
				throw e;
			}

			createLoggerObject(logFile, logLevel);

			if (onlyVersion(arg)) {
				rc = true;
				doExit(255);
				return;
			}

			try {
				sosConfiguration = new sos.configuration.SOSConfiguration(arg, settingsFile, settingsSection, logger);
				// SOSConfigurationItem[] items = sosConfiguration.checkConfigurationItems(REQUIRED_DEFAULT_PARAMETERS_FILENAME_NOREQUIRE);
				SOSConfigurationItem[] items = sosConfiguration.checkConfigurationItems(null);
			}
			catch (Exception e) {
				System.out.println("[ERROR] could not init Configuration, cause: " + e.getMessage());
				throw e;
			}

			operation = checkOperation(); // operation value is given?
			Properties arg_s = convertParameters(); // convert ConfigurationItem[] to Properties
			logLevel = getIntArg(arg_s, "verbose", SOSLogger.INFO);
			logFile = getStringArg(arg_s, "log_filename", "");
			createLoggerObject(logFile, logLevel);

			ArrayList extractArguments = getFileOperations(arg_s); // multiple file operations within one session

			try {
				banner = true;
				rc = transfer(extractArguments, operation, arg_s, rc, false);
				boolean flgRC = false;
				if (rc) {
					rc = doPostTransactionalOnSuccess();
					//					flgRC = doPostTransactionalOnSuccess();
				}
				else {
					rc = doPostTransactionalOnError();
					//					flgRC = doPostTransactionalOnError();
				}
			}
			catch (Exception e) {
				banner = true;
				throw e;
			}
		}
		catch (Exception e) {
			try {
				if (ftpCommand != null) {
					if (banner)
						logger.info(ftpCommand.getBanner(false));
					else
						logger.debug(ftpCommand.getBanner(false));
				}
				else {
					System.err.println(e.toString());
				}
			}
			catch (Exception x) {
				System.err.println(e.toString());
			}
			doExit(1);
		}
		finally {
			try {
				if (ftpCommand != null) {
					ftpCommand.appendHistoryFile();
					ftpCommand.sendTransactionalSchedulerRequestFile();
				}
				logger.close();
			}
			catch (Exception e) {
				System.out.println(e.toString());
			}
			doExit(rc ? 0 : 1);
		}
	}

	public static boolean	gflgUseSystemExit	= true;

	protected static void doExit(final int pintExitcode) {
		if (gflgUseSystemExit == true) {
			if (pintExitcode != 0) {
				System.out.println("System-Exit code is " + pintExitcode);
			}
			System.exit(pintExitcode);
		}
		else {
			//
		}
	}

	/**
	 * List of transfer Files
	 *
	 * @return the filelist
	 */
	public Vector<String> getFilelist() throws Exception {
		return filelist;
	}

	/**
	 * List of transfered Files
	 *
	 * @return the filelist
	 */
	public Vector<File> getTransferredFilelist() throws Exception {
		if (transferFileList == null) {
			transferFileList = new Vector<File>();
		}
		return transferFileList;
	}

	/**
	 * @return the exitStatus
	 */
	public Integer getExitStatus() {
		if (exitStatus != null)
			return exitStatus;
		else
			return new Integer(0);
	}

	/**
	 *
	 * \brief getBooleanParam
	 *
	 * \details
	 *
	 * \return boolean
	 *
	 * @param pstrParamName
	 * @param pstrDefaultvalue
	 * @return
	 * @throws Exception
	 */
	protected boolean getBooleanParam(final String pstrParamName, final String pstrDefaultvalue) throws Exception {
		String strResult = getParam(pstrParamName, pstrDefaultvalue);

		if (strResult.toString().equalsIgnoreCase("true") || strResult.toString().equalsIgnoreCase("yes") || strResult.equals("1")) {
			return true;
		}
		return false;
	}

	protected String getParam(final String pstrParamName, final String pstrDefaultvalue) throws Exception {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::getParam";
		String strRet = "";

		String strVal = "";
		try {
			strVal = getString(pstrParamName.toLowerCase());
			if (strVal.length() <= 0) {
				String strNewParamName = pstrParamName.replaceAll("_", "");
				strVal = getString(strNewParamName.toLowerCase());
			}
			else {
				if (strVal.length() <= 0) {
					String strNewParamName = pstrParamName.replaceAll("-", "_");
					strVal = getString(strNewParamName.toLowerCase());
				}
			}
		}
		catch (Exception e) {
		}
		this.getLogger().debug(String.format(".. get Parameter '%1$s' with value '%2$s', default is '%3$s'", pstrParamName, strVal, pstrDefaultvalue));
		if (strVal.length() > 0) {
			strRet = strVal;
		}
		else {
			strRet = pstrDefaultvalue;
			if (arguments != null) {
				arguments.put(pstrParamName, pstrDefaultvalue);
			}
		}

		return strRet;
	} // private String getParam

	protected String getString(final String pstrParameterName) {
		String strR = "";
		try {
			strR = sosString.parseToString(arguments.get(pstrParameterName)).trim();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return strR;
	}

	protected String getQuotedString(final String pstrParameterName) {
		String strR = "";
		strR = "\"" + getString(pstrParameterName) + "\"";
		return strR;
	}

	protected String getKeywordValuePair(final String pstrParameterName) {
		String strR = " -" + pstrParameterName + "=";
		strR += getQuotedString(pstrParameterName);
		return strR;
	}

	protected String getKeywordValuePair(final String pstrParameterName, final String pstrParameterValue) {
		String strR = " -" + pstrParameterName + "=";
		strR += "\"" + pstrParameterValue + "\"";
		return strR;
	}

	protected String doEncoding(final String pstrStringToEncode, final String pstrEncoding) throws Exception {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::doEncoding";

		// Zeichen Unicode
		// ------------------------------
		// Ä, ä \u00c4, \u00e4
		// Ö, ö \u00d6, \u00f6
		// Ü, ü \u00dc, \u00fc
		// ß \u00df

		// see http://www.fileformat.info/info/unicode/char/search.htm

		final String conUTF8UmlU = "\u00fc"; // "Ã¼";
		final String conUTF8UmlBigA = "\u00c4"; // LATIN CAPITAL LETTER A WITH DIAERESIS "Ã\\?";
		final String conUTF8UmlBigO = "\u00d6"; // "Ã\\?";
		final String conUTF8UmlBigU = "\u00dc"; // "Ã\\?";
		final String conUTF8UmlA = "\u00e4"; // "Ã¤";
		final String conUTF8UmlO = "\u00f6"; // "Ã¶";
		final String conUTF8UmlS = "\u00DF";

		String strEncodedString = pstrStringToEncode;
		if (pstrEncoding.length() > 0) {
			byte[] iso88591Data = pstrStringToEncode.getBytes(Charset.forName(pstrEncoding));
			strEncodedString = new String(iso88591Data, Charset.forName(pstrEncoding));
			strEncodedString = strEncodedString.replaceAll(conUTF8UmlU, "ü");
			strEncodedString = strEncodedString.replaceAll(conUTF8UmlBigA, "Ä");
			strEncodedString = strEncodedString.replaceAll(conUTF8UmlBigU, "Ü");
			strEncodedString = strEncodedString.replaceAll(conUTF8UmlBigO, "Ö");
			strEncodedString = strEncodedString.replaceAll(conUTF8UmlA, "ä");
			strEncodedString = strEncodedString.replaceAll(conUTF8UmlO, "ö");
			strEncodedString = strEncodedString.replaceAll(conUTF8UmlS, "ß");
			getLogger().debug(String.format("Encode String '%1$s' to/in '%2$s' using '%3$s'", pstrStringToEncode, strEncodedString, pstrEncoding));
		}
		return strEncodedString;
	} // private String doEncoding

	protected String doEncodeUmlaute(final String pstrStringToEncode, final String pstrEncoding) throws Exception {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::doEncodeUmlaute";

		// Zeichen Unicode
		// ------------------------------
		// Ä, ä \u00c4, \u00e4
		// Ö, ö \u00d6, \u00f6
		// Ü, ü \u00dc, \u00fc
		// ß \u00df

		final String conUTF8UmlU = "ü";
		final String conUTF8UmlBigA = "\u00c4"; // "Ã\\?";
		final String conUTF8UmlBigO = "\u00d6"; // "Ã\\?";
		final String conUTF8UmlBigU = "\u00dc"; // "Ã\\?";
		final String conUTF8UmlA = "ä";
		final String conUTF8UmlO = "ö";
		final String conUTF8UmlS = "\u00df";

		String strEncodedString = pstrStringToEncode;
		if (pstrEncoding.length() > 0) {
			byte[] iso88591Data = pstrStringToEncode.getBytes(Charset.forName(pstrEncoding));
			strEncodedString = new String(iso88591Data, Charset.forName(pstrEncoding));
			strEncodedString = strEncodedString.replaceAll(conUTF8UmlU, "ue");
			strEncodedString = strEncodedString.replaceAll(conUTF8UmlBigA, "AE");
			strEncodedString = strEncodedString.replaceAll(conUTF8UmlBigU, "UE");
			strEncodedString = strEncodedString.replaceAll(conUTF8UmlBigO, "OE");
			strEncodedString = strEncodedString.replaceAll(conUTF8UmlA, "ae");
			strEncodedString = strEncodedString.replaceAll(conUTF8UmlO, "oe");
			strEncodedString = strEncodedString.replaceAll(conUTF8UmlS, "sz");
			getLogger().debug(String.format("Encode String '%1$s' to/in '%2$s' using '%3$s'", pstrStringToEncode, strEncodedString, pstrEncoding));
		}
		return strEncodedString;
	} // private String doEncoding

	protected void retrieveCommonParameters() throws Exception {

		flgUsePathAndFileName4Matching = getBooleanParam(SOSFTPCommand.USE_PATH_AND_FILE_NAME_4_MATCHING, "false");
		flgCheckServerFeatures = getBooleanParam(SOSFTPCommand.CHECK_SERVER_FEATURES, "");
		strPreFtpCommands = getParam(PRE_FTP_COMMANDS, "");
		strControlEncoding = getParam(SOSFTPCommand.CONTROL_ENCODING, "");
		strFileNameEncoding = getParam(SOSFTPCommand.FILENAME_ENCODING, "");
		flgConvertUmlaute = getBooleanParam(SOSFTPCommand.CONVERT_UMLAUTE, "false");

	}

	public void setParam(final String pstrParamName, final String pstrParamValue) throws Exception {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::setParam";

		arguments.put(pstrParamName, pstrParamValue);
		logger.debug(String.format("set param '%1$s' to value '%2$s'", pstrParamName, pstrParamValue));

	} // private void setParam

	protected String stripRemoteDirName(final String pstrRootPath, final String pstrPathName) throws Exception {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::stripRemoteDirName";

		String strResult = pstrPathName;
		String strR = new File(pstrRootPath).getAbsolutePath();
		String strP = new File(pstrPathName).getAbsolutePath();
		if (strP.startsWith(strR) == true) {
			strResult = strP.substring(strR.length());
			if (strResult.contains(File.separator)) {
				if (strResult.startsWith(File.separator)) {
					strResult = strResult.substring(1);
				}
				if (strResult.contains(File.separator)) {
					strResult = "." + File.separator + strResult;
				}
			}
			// strResult = new File(strResult).getCanonicalPath();
			strResult = adjustSeparator(strResult);
		}
		return strResult;
	} // private String stripRemoteDirName

	protected String adjustSeparator(final String pstrPathName) {

		@SuppressWarnings("unused")
		final String conMethodName = conClassName + "::";

		String strRet = pstrPathName;
		String[] strA = pstrPathName.split("[/\\\\]");
		if (strA.length > 0) {
			strRet = "";
			for (String string : strA) {
				if (string.length() > 0) {
					strRet = strRet + string + File.separator;
				}
			}
			strRet = strRet.substring(0, strRet.length() - 1);
		}

		return strRet;
	} // private String adjustSeparator

	protected static void RaiseException(final Exception e, final String pstrM) {
		try {
			logger.error(pstrM);
		}
		catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (e != null) {
			e.printStackTrace(System.err);
			throw new JobSchedulerException(pstrM, e);
		}
		else {
			throw new JobSchedulerException(pstrM);
		}
	}

	protected static void RaiseException(final String pstrM, final Exception e) throws Exception {
		RaiseException(e, pstrM);
	}

	protected static void RaiseException(final String pstrM) throws Exception {
		RaiseException(null, pstrM);
	}

	protected boolean isAPathName(final String pstrFileAndPathName) {
		boolean flgOK = false;
		String lstrPathName = pstrFileAndPathName.replaceAll("\\\\", "/");
		if (lstrPathName.startsWith(".") || lstrPathName.startsWith("..")) { // relative to localdir
			flgOK = false;
		}
		else {
			if (lstrPathName.contains(":/") || lstrPathName.startsWith("/")) {
				flgOK = true;
			}
			else {
				flgOK = lstrPathName.contains("/") == true;
			}
		}
		return flgOK;
	}

}
