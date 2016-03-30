package sos.net;

import com.sos.JSHelper.Basics.VersionInfo;
import com.sos.JSHelper.Exceptions.JobSchedulerException;
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

/** @author andreas.pueschel@sos-berlin.com
 * @author mueruevet.oeksuez@sos-berlin.com **/
abstract public class SOSFTPCommand {

    protected static final String conParamFORCE_FILES = "force_files";
    protected static final String conNewLine = "\n";
    protected static final String conRegExpBackslash = "\\\\";
    protected static final String conRegExpAllChars = ".*";
    protected final static String DEFAULT_COMMAND_DELIMITER = "%%";
    protected static ArrayList listOfSuccessTransfer = null;
    protected static ArrayList listOfErrorTransfer = null;
    protected static boolean banner = true;
    protected static Object schedulerJob = null;
    protected static String SSHJOBNAME = "sos.scheduler.job.JobSchedulerSSHJob";
    protected static SOSString sosString = new SOSString();
    protected static String tempJumpRemoteDir = "";
    protected Vector<File> transferFileList = new Vector<File>();
    protected Vector<String> filelisttmp = null;
    protected SOSFTP sosftp = null;
    protected String strPreFtpCommands = "";
    protected boolean flgCheckServerFeatures = false;
    protected boolean flgUsePathAndFileName4Matching = false;
    protected String strControlEncoding = "";
    protected String strFileNameEncoding = "";
    protected boolean flgConvertUmlaute = false;
    protected String replacing = null;
    protected String replacement = null;
    protected String mailSMTP = "localhost";
    protected String mailPortNumber = "25";
    protected String mailFrom = "SOSFTP";
    protected String mailQueueDir = "";
    protected String[] commands = {};
    protected boolean ignoreError = false;
    protected boolean ignoreSignal = false;
    protected boolean ignoreStderr = false;
    protected long lasttime = 0;
    protected String commandDelimiter = DEFAULT_COMMAND_DELIMITER;
    protected Session sshSession = null;
    protected InputStream stdout = null;
    protected InputStream stderr = null;
    protected String commandScript = "";
    protected String currentLine = "";
    protected String host = "";
    protected int port = 21;                                                                                                                                                                                                                                                        // JS-649
    protected String user = "";
    protected String password = "";
    protected String proxyHost = "";
    protected int proxyPort = 0;
    protected String proxyUser = "";
    protected String proxyPassword = "";
    protected String authenticationFilename = "";
    protected String authenticationMethod = "publickey";
    protected Properties arguments = null;
    protected Properties savedArguments = null;
    protected boolean flgJumpTransferDefined = false;
    protected String postCommands = "";
    protected String account = "";
    protected SOSFileTransfer ftpClient = null;
    protected boolean isLoggedIn = false;
    protected String bannerHeader = "\n*************************************************************************"
            + "\n*                                                                       *"
            + "\n*                SOSFTP - Managed File Transfer Utility                 *"
            + "\n*                --------------------------------------                 *"
            + "\n*                                                                       *"
            + "\n*************************************************************************" + "\nversion              = %{version}"
            + "\ndate                 = %{date} %{time}" + "\noperation            = %{operation}" + "\nprotocol             = %{protocol}"
            + "\nport                 = %{port}" + "\nfile specification   = %{file_spec}" + "\nfile path            = %{file_path}"
            + "\nsource host          = %{localhost} (%{local_host_ip})" + "\nlocal directory      = %{local_dir}"
            + "\njump host            = %{jump_host}" + "\ntarget host          = %{host} (%{remote_host_ip})"
            + "\ntarget directory     = %{remote_dir}" + "\npid                  = %{current_pid}" + "\nppid                 = %{ppid}"
            + "\n*************************************************************************";
    protected String bannerFooter = "\n*************************************************************************"
            + "\n execution status     = %{status}" + "\n successful transfers = %{successful_transfers}"
            + "\n failed transfers     = %{failed_transfers}" + "\n last error           = %{last_error}"
            + "\n*************************************************************************";
    protected boolean utf8Supported = false;
    protected boolean restSupported = false;
    protected boolean mlsdSupported = false;
    protected boolean modezSupported = false;
    protected boolean dataChannelEncrypted = false;
    protected static ArrayList transactionalHistoryFile = null;
    protected ArrayList transactionalSchedulerRequestFile = null;
    protected static boolean transActional = false;
    private static final String conParamValueSUCCESS = "success";
    private static final String conParameterSTATUS = "status";
    private static final String conOperationDELETE = "delete";
    private static final Logger LOGGER = Logger.getLogger(SOSFTPCommand.class);
    private static final String conSettingsJUMP_COMMAND = "jump_command";
    private static final String conParamMAKE_DIRS = "make_dirs";
    private static final String conJobChainNameSCHEDULER_SOSFTP_HISTORY = "scheduler_sosftp_history";
    private static final String conParamSCHEDULER_JOB_CHAIN = "scheduler_job_chain";
    private static final String conParamSCHEDULER_PORT = "scheduler_port";
    private static final String conParamSCHEDULER_HOST = "scheduler_host";
    private static final String conParamSCHEDULER_MESSAGE = "scheduler_message";
    private static final String conOperationRECEIVE = "receive";
    private static final String conOperationSEND = "send";
    private static Integer exitStatus = null;
    private static SOSLogger logger = null;
    private static String conClassName = "SOSFTPCommand";
    private static Properties originalParam = null;
    private static File tempHistoryFile = null;
    private static File historyFile = null;
    private static BufferedWriter history = null;
    private final String defaultMandator = "SOS";
    private static ArrayList historyEntrys = new ArrayList();
    private static ArrayList noJumpParameter = new ArrayList();
    private static Properties environment = null;
    private static Properties schedulerParams = new Properties();
    private static SOSConfiguration sosConfiguration = null;
    private static SOSFTPCommand ftpCommand = null;
    private String historyFields =
            "guid;mandator;transfer_timestamp;pid;ppid;operation;localhost;localhost_ip;local_user;remote_host;remote_host_ip;"
                    + "remote_user;protocol;port;local_dir;remote_dir;local_filename;remote_filename;file_size;md5;status;last_error_message;log_filename";
    private String newHistoryFields = "jump_host;jump_host_ip;jump_port;jump_protocol;jump_user";
    private boolean writeBannerHeader = false;
    private boolean sendSchedulerSignale = false;
    private Connection sshConnection = null;
    private File lockHistoryFile = null;
    public static final String conParamPORT = "port";
    public static final String conParamPROTOCOL = "protocol";
    public static final String conParamSSH_AUTH_METHOD = "ssh_auth_method";
    public static final String conParamPASSWORD = "password";
    public static final String conParamUSER = "user";
    public static final String conParamHOST = "host";
    public static final String conParamJUMP_HOST = "jump_host";
    public static final String conParamSSH_AUTH_FILE = "ssh_auth_file";
    public static final String conParamJUMP_PROTOCOL = "jump_protocol";
    public static final String conSettingREMOVE_FILES = "remove_files";
    public static final String conParameterREMOVE_AFTER_JUMP_TRANSFER = "remove_after_jump_transfer";
    public static final String conParamREPLACEMENT = "replacement";
    public static final String conParamREPLACING = "replacing";
    public static final String FILE_MODIFICATION_DATE = "FILE_MODIFICATION_DATE";
    public static final String conOperationEXECUTE = "execute";
    public static final String conOperationREMOVE = "remove";
    public static final String conSettingOPERATION = "operation";
    public static final String conSettingLOCAL_DIR = "local_dir";
    public static final String conSettingFILE_PATH = "file_path";
    public static final String conSettingFILE_SPEC = "file_spec";
    public static final String USE_PATH_AND_FILE_NAME_4_MATCHING = "use_path_and_file_name_4_matching";
    public static final String CHECK_SERVER_FEATURES = "check_server_features";
    public static final String CONVERT_UMLAUTE = "convert_umlaute";
    public static final String FILENAME_ENCODING = "Filename_encoding";
    public static final String CONTROL_ENCODING = "control_encoding";
    public static final String PRE_FTP_COMMANDS = "pre_ftp_commands";
    public static final String conStatusERROR = "error";
    public static final String conStatusSUCCESS = conParamValueSUCCESS;
    public static final String conSettingSTATUS = conParameterSTATUS;
    public static final String conSettingREMOTE_DIR = "remote_dir";
    public static final String conSettingTRANSFER_MODE = "transfer_mode";
    public static final String conSettingFILE_SIZE = "file_size";
    public static final int ERROR_CODE = 300;
    public static Vector<String> filelist = null;
    public static boolean gflgUseSystemExit = true;

    public SOSFTPCommand(final SOSLogger logger, final Properties arguments_) throws Exception {
        this.setLogger(logger);
        arguments = arguments_;
        arguments.put("version", getVersion());
        try {
            String pid = getPids();
            if (!sosString.parseToString(pid).isEmpty()) {
                arguments.put("current_pid", pid);
            }
        } catch (Exception e) {
            //
        }
    }

    public SOSFTPCommand(final SOSConfiguration sosConfiguration_, final SOSLogger logger) throws Exception {
        this.setLogger(logger);
        sosConfiguration = sosConfiguration_;
        arguments = sosConfiguration.getParameterAsProperties();
        arguments.put("version", getVersion());
        try {
            String pid = getPids();
            if (!sosString.parseToString(pid).isEmpty()) {
                arguments.put("current_pid", pid);
            }
        } catch (Exception e) {
            //
        }
    }

    public void setSchedulerJob(final Object schedulerJob_) {
        schedulerJob = schedulerJob_;
    }

    protected boolean receive() throws Exception {
        return false;
    }

    protected boolean send() throws Exception {
        return false;
    }

    public boolean execute() throws Exception {
        StringBuilder stderrOutput = new StringBuilder();
        String commandScriptFileName = "";
        try {
            getLogger().debug1("calling " + sos.util.SOSClassUtil.getMethodName());
            try {
                if (this.getLogger() == null) {
                    this.setLogger(new SOSStandardLogger(0));
                }
                readSettings(false);
                this.getBaseParameters();
                if (!flgJumpTransferDefined) {
                    if (!getString("jump_command_delimiter").isEmpty()) {
                        this.setCommandDelimiter(getString("jump_command_delimiter"));
                        getLogger().debug1(".. parameter [jump_command_delimiter]: " + this.getCommandDelimiter());
                    } else {
                        this.setCommandDelimiter(DEFAULT_COMMAND_DELIMITER);
                    }
                    if (!getString("jump_command_script").isEmpty()) {
                        commandScript = getString("jump_command_script");
                        getLogger().debug1(".. parameter [jump_command_script]: " + commandScript);
                    }
                    if (!getString("jump_command_script_file").isEmpty()) {
                        commandScriptFileName = getString("jump_command_script_file");
                        getLogger().debug1(".. parameter [jump_command_script_file]: " + commandScriptFileName);
                    }
                    if (arguments.containsKey("xx_make_temp_directory_success_transfer_xx") && !getString("command").isEmpty()) {
                        this.setCommands(getString("command").split(this.getCommandDelimiter()));
                        getLogger().debug1(".. parameter [jump_command]: " + getString("command"));
                    } else if (!arguments.contains("xx_make_temp_directory_xx")) {
                        if (!getString(conSettingsJUMP_COMMAND).isEmpty()) {
                            this.setCommands(getString(conSettingsJUMP_COMMAND).split(this.getCommandDelimiter()));
                            getLogger().debug1(".. parameter [jump_command]: " + getString(conSettingsJUMP_COMMAND));
                        } else if (commandScript.isEmpty() && commandScriptFileName.isEmpty()) {
                            RaiseException("no command (or jump_command_script or jump_command_script_file) has been specified for parameter [jump_command]");
                        }
                    }
                }
                if (!getString("jump_ignore_error").isEmpty()) {
                    if (getBool("jump_ignore_error")) {
                        ignoreError = true;
                    } else {
                        ignoreError = false;
                    }
                    getLogger().debug1(".. parameter [jump_ignore_error]: " + ignoreError);
                } else {
                    ignoreError = false;
                }
                if (!getString("jump_ignore_signal").isEmpty()) {
                    if (sosString.parseToBoolean(getString("jump_ignore_signal"))) {
                        ignoreSignal = true;
                    } else {
                        ignoreSignal = false;
                    }
                    getLogger().debug1(".. parameter [jump_ignore_signal]: " + ignoreSignal);
                } else {
                    ignoreSignal = false;
                }
                if (!getString("jump_ignore_stderr").isEmpty()) {
                    if (sosString.parseToBoolean(getString("jump_ignore_stderr"))) {
                        ignoreStderr = true;
                    } else {
                        ignoreStderr = false;
                    }
                    getLogger().debug1(".. parameter [jump_ignore_stderr]: " + ignoreStderr);
                } else {
                    ignoreStderr = false;
                }
            } catch (Exception e) {
                RaiseException("error occurred processing parameters: " + e.getMessage(), e);
            }
            RemoteConsumer stdoutConsumer = null;
            RemoteConsumer stderrConsumer = null;
            try {
                this.getBaseAuthentication();
                boolean windows = remoteIsWindowsShell();
                String remoteCommandScriptFileName = "";
                if (!commandScript.isEmpty() || !commandScriptFileName.isEmpty()) {
                    File commandScriptFile = null;
                    if (!commandScript.isEmpty()) {
                        commandScriptFile = createCommandScript(windows);
                    } else {
                        commandScriptFile = createCommandScript(new File(commandScriptFileName), windows);
                    }
                    transferCommandScript(commandScriptFile, windows);
                    remoteCommandScriptFileName = commandScriptFile.getName();
                    commands = new String[1];
                    if (windows) {
                        commands[0] = commandScriptFile.getName();
                    } else {
                        commands[0] = "./" + commandScriptFile.getName();
                    }
                    if (!commandScript.isEmpty()) {
                        commandScriptFile.delete();
                    }
                }
                Class cl_ = null;
                for (int i = 0; i < this.getCommands().length; i++) {
                    try {
                        exitStatus = null;
                        String exitSignal = null;
                        if (sosString.parseToString(getCommands()[i]).trim().isEmpty()) {
                            continue;
                        }
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
                            if (line == null) {
                                break;
                            }
                            if (!windows && pid == 0 && schedulerJob != null && Class.forName(SSHJOBNAME) != null
                                    && schedulerJob.getClass().getName().equals(cl_.getName())) {
                                pid = Integer.parseInt(line);
                                if (cl_ != null) {
                                    Method method = cl_.getMethod("setSchedulerSSHKillPid", new Class[] { int.class });
                                    method.invoke(schedulerJob, new Object[] { new Integer(pid) });
                                }
                                getLogger().debug5("Parent pid: " + pid);
                                continue;
                            }
                            if (getString(conSettingOPERATION).equalsIgnoreCase(conOperationEXECUTE) && banner) {
                                getLogger().info(line);
                            } else if (conOperationRECEIVE.equalsIgnoreCase(getString(conSettingOPERATION))
                                    && sosString.parseToString(line).indexOf("files found.") > -1
                                    && sosString.parseToBoolean(getString("skip_transfer"))) {
                                getLogger().info(line.substring(line.indexOf("[info]") + "[info]".length()));
                            } else if (conOperationRECEIVE.equalsIgnoreCase(getString(conSettingOPERATION))
                                    && sosString.parseToString(line).indexOf("Processing file") > -1
                                    && sosString.parseToBoolean(getString("skip_transfer"))) {
                                String s = line.substring(line.indexOf("Processing file") + "Processing file".length() + 1);
                                if (filelisttmp == null) {
                                    filelisttmp = new Vector<String>();
                                }
                                filelisttmp.add(s);
                            } else if (line.indexOf("[warn]") > -1 || line.indexOf("[error]") > -1) {
                                getLogger().warn("remote execution reports error : " + line);
                            } else {
                                getLogger().debug1(line);
                            }
                            if (line.indexOf("*******ftp transfer directory is:") > -1) {
                                int pos1 = line.indexOf("*******ftp transfer directory is:") + "*******ftp transfer directory is:".length();
                                int pos2 = line.indexOf("******************");
                                tempJumpRemoteDir = line.substring(pos1, pos2);
                            }
                            if (line.indexOf("[info]") > -1 && line.indexOf("files found.") > -1 && getBool("testmode")
                                    && conOperationRECEIVE.equals(getString(conSettingOPERATION))) {
                                getLogger().info(line.substring(line.indexOf("[info]") + "[info]".length()));
                            }
                            if (line.indexOf("[info]") > -1
                                    && (line.indexOf("removing remote file:") > -1 || line.indexOf("files remove.") > -1)
                                    && (conOperationREMOVE.equals(getString(conSettingOPERATION)) || conOperationRECEIVE.equals(getString(conSettingOPERATION)))) {
                                getLogger().info(line.substring(line.indexOf("[info]") + "[info]".length()));
                            }
                        }
                        if (!windows && schedulerJob != null && cl_ != null && Class.forName(SSHJOBNAME) != null
                                && schedulerJob.getClass().getName().equals(cl_.getName())) {
                            Method method = cl_.getMethod("setSchedulerSSHKillPid", new Class[] { int.class });
                            method.invoke(schedulerJob, new Object[] { new Integer(0) });
                        }
                        getLogger().debug1("output to stderr for remote command: " + normalizedPassword(this.getCommands()[i]));
                        BufferedReader stderrReader = new BufferedReader(new InputStreamReader(stderr));
                        stderrOutput = new StringBuilder();
                        while (true) {
                            String line = stderrReader.readLine();
                            if (line == null) {
                                break;
                            }
                            getLogger().error(line);
                            stderrOutput.append(line + conNewLine);
                        }
                        if (stderrOutput != null && !stderrOutput.toString().isEmpty()) {
                            if (ignoreStderr) {
                                if (flgJumpTransferDefined) {
                                    getLogger().error("output to stderr is ignored: " + stderrOutput);
                                    return false;
                                } else {
                                    getLogger().info("output to stderr is ignored: " + stderrOutput);
                                }
                            } else {
                                RaiseException("remote execution reports error: " + stderrOutput);
                            }
                        }
                        try {
                            exitStatus = this.getSshSession().getExitStatus();
                        } catch (Exception e) {
                            getLogger().debug1("could not retrieve exit status, possibly not supported by remote ssh server");
                        }
                        if (exitStatus != null && !exitStatus.equals(new Integer(0))) {
                            if (ignoreError) {
                                getLogger().debug1("exit status is ignored: " + exitStatus);
                            } else {
                                RaiseException("remote command terminated with exit status: " + exitStatus
                                        + (logger.hasWarnings() ? " error: " + logger.getWarning() : ""));
                            }
                        }
                        try {
                            exitSignal = this.getSshSession().getExitSignal();
                        } catch (Exception e) {
                            getLogger().debug1("could not retrieve exit signal, possibly not supported by remote ssh server");
                        }
                        if (exitSignal != null && !exitSignal.isEmpty()) {
                            if (ignoreSignal) {
                                getLogger().debug1("exit signal is ignored: " + exitSignal);
                            } else {
                                RaiseException("remote command terminated with exit signal: " + exitSignal);
                            }
                        }
                    } catch (Exception e) {
                        throw e;
                    } finally {
                        if (remoteCommandScriptFileName != null && !remoteCommandScriptFileName.isEmpty()) {
                            deleteCommandScript(remoteCommandScriptFileName);
                        }
                        if (this.getSshSession() != null) {
                            try {
                                this.getSshSession().close();
                                this.setSshSession(null);
                            } catch (Exception ex) {
                                // gracefully ignore this error
                            }
                        }
                    }
                }
            } catch (Exception e) {
                RaiseException("error occurred processing ssh command: " + e.getMessage(), e);
            } finally {
                if (stderrConsumer != null) {
                    stderrConsumer.end();
                }
                if (stdoutConsumer != null) {
                    stdoutConsumer.end();
                }
                if (this.getSshConnection() != null) {
                    try {
                        this.getSshConnection().close();
                        this.setSshConnection(null);
                    } catch (Exception ex) {
                        // gracefully ignore this error
                    }
                }
            }
            return true;
        } catch (Exception e) {
            try {
                getLogger().warn(e.getMessage());
            } catch (Exception ex) {
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
            while (exists) {
                try {
                    SFTPv3FileAttributes attribs = sftpClient.stat(commandFile.getName());
                } catch (SFTPException e) {
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
            SFTPv3FileHandle fileHandle = sftpClient.createFileTruncate(commandFile.getName(), attr);
            FileInputStream fis = null;
            long offset = 0;
            try {
                fis = new FileInputStream(commandFile);
                byte[] buffer = new byte[1024];
                while (true) {
                    int len = fis.read(buffer, 0, buffer.length);
                    if (len <= 0) {
                        break;
                    }
                    sftpClient.write(fileHandle, offset, buffer, 0, len);
                    offset += len;
                }
                fis.close();
                fis = null;
            } catch (Exception e) {
                RaiseException("error occurred writing file [" + commandFile.getName() + "]: " + e.getMessage(), e);
            } finally {
                if (fis != null) {
                    try {
                        fis.close();
                        fis = null;
                    } catch (Exception ex) {
                        // gracefully ignore this error
                    }
                }
            }
            sftpClient.closeFile(fileHandle);
            fileHandle = null;
            sftpClient.close();
        } catch (Exception e) {
            RaiseException("Error transferring command script: " + e.getMessage(), e);
        }
        return commandFile;
    }

    public File createCommandScript(final boolean isWindows) throws Exception {
        File resultFile = null;
        try {
            if (!isWindows) {
                commandScript = commandScript.replaceAll("(?m)\r", "");
            }
            String suffix = isWindows ? ".cmd" : ".sh";
            resultFile = File.createTempFile("sos", suffix);
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(resultFile)));
            out.write(commandScript);
            out.close();
        } catch (Exception e) {
            RaiseException("Error creating command script: " + e.getMessage(), e);
        }
        return resultFile;
    }

    public File createCommandScript(final File scriptFile, final boolean isWindows) throws Exception {
        try {
            commandScript = sos.util.SOSFile.readFileUnicode(scriptFile);
            return createCommandScript(isWindows);
        } catch (Exception e) {
            RaiseException("Error creating command script: " + e.getMessage(), e);
        }
        return null;
    }

    private void deleteCommandScript(final String commandFile) throws Exception {
        try {
            SFTPv3Client sftpClient = new SFTPv3Client(this.getSshConnection());
            sftpClient.rm(commandFile);
            sftpClient.close();
        } catch (Exception e) {
            getLogger().warn("Failed to delete remote command script: " + e);
        }
    }

    private Session getSshSession() {
        return sshSession;
    }

    private void setSshSession(final Session sshSession1) {
        sshSession = sshSession1;
    }

    public boolean remove() throws Exception {
        getLogger().debug1("calling " + sos.util.SOSClassUtil.getMethodName());
        arguments.put("skip_transfer", "yes");
        arguments.put(conSettingREMOVE_FILES, "yes");
        return receive();
    }

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
                        "mail server is unavailable, mail for recipient [" + recipient + "] is queued in local directory [" + sosMail.getQueueDir()
                                + "]:" + sosMail.getLastError());
            }
            sosMail.clearRecipients();
        } catch (Exception e) {
            RaiseException("error occurred sending mai: " + e.getMessage(), e);
        }
    }

    protected String getAlternative(final String param, final String alternativeParam) throws Exception {
        try {
            if (!sosString.parseToString(alternativeParam).isEmpty()) {
                return alternativeParam;
            } else {
                return param;
            }
        } catch (Exception e) {
            this.getLogger().warn("error in getAlternative(): " + e.getMessage());
            return param;
        }
    }

    protected int getAlternative(final int param, final int alternativeParam) throws Exception {
        try {
            if (alternativeParam > 0) {
                return alternativeParam;
            } else {
                return param;
            }
        } catch (Exception e) {
            this.getLogger().warn("error in getAlternative(): " + e.getMessage());
            return param;
        }
    }

    public SOSLogger getLogger() {
        return logger;
    }

    public void setLogger(final SOSLogger logger_) {
        logger = logger_;
    }

    protected URI createURI(final String fileName) throws Exception {
        URI uri = null;
        try {
            uri = new URI(fileName);
        } catch (Exception e) {
            try {
                File f = new File(fileName);
                String path = f.getCanonicalPath();
                if (fileName.startsWith("/")) {
                    path = fileName;
                }
                String fs = System.getProperty("file.separator");
                if (fs.length() == 1) {
                    char sep = fs.charAt(0);
                    if (sep != '/') {
                        path = path.replace(sep, '/');
                    }
                    if (path.charAt(0) != '/') {
                        path = '/' + path;
                    }
                }
                if (!path.startsWith("file://")) {
                    path = "file://" + path;
                }
                uri = new URI(path);
            } catch (Exception ex) {
                throw new Exception("error in createURI(): " + e.getMessage(), e);
            }
        }
        return uri;
    }

    protected File createFile(final String fileName) throws Exception {
        try {
            if (fileName == null || fileName.isEmpty()) {
                RaiseException("empty file name provided");
            }
            if (fileName.startsWith("file://")) {
                return new File(createURI(fileName));
            } else {
                return new File(fileName);
            }
        } catch (Exception e) {
            RaiseException("error in createFile() [" + fileName + "]: " + e.getMessage(), e);
        }
        return null;
    }

    protected static String getErrorMessage(final Exception ex) throws Exception {
        String s = "";
        try {
            Throwable tr = ex.getCause();
            if (ex.toString() != null) {
                s = ex.toString();
            }
            while (tr != null) {
                if (s.indexOf(tr.toString()) == -1) {
                    s = (s.length() > 0 ? s + ", " : "") + tr.toString();
                }
                tr = tr.getCause();
            }
        } catch (Exception e) {
            throw ex;
        }
        return s;
    }

    protected String[] getCommands() {
        return commands;
    }

    protected void setCommands(final String[] commands) {
        this.commands = commands;
    }

    protected void mergeSettings() throws Exception {
        Properties retVal = new Properties();
        try {
            Properties arg = getEnvVars(logger);
            retVal.putAll(arg);
            retVal.put("date", sos.util.SOSDate.getCurrentDateAsString());
            retVal.put("time", sos.util.SOSDate.getCurrentTimeAsString("hh:mm:ss"));
            retVal.put("transfer_timestamp", sos.util.SOSDate.getCurrentTimeAsString());
            retVal.put("local_user", sosString.parseToString(System.getProperty("user.name")));
            if (!sosString.parseToString(arg, "current_pid").isEmpty()) {
                retVal.put("current_pid", sosString.parseToString(arg, "current_pid"));
            }
            if (!sosString.parseToString(arg, "ppid").isEmpty()) {
                retVal.put("ppid", sosString.parseToString(arg, "ppid"));
            }
            try {
                java.net.InetAddress localMachine = java.net.InetAddress.getLocalHost();
                if (!sosString.parseToString(localMachine.getHostName()).isEmpty()) {
                    retVal.put("localhost", localMachine.getHostName());
                }
                retVal.put("local_host_ip", localMachine.getHostAddress());
            } catch (java.net.UnknownHostException uhe) {
                // do nothing
            }
            if (arguments != null) {
                retVal.putAll(arguments);
            }
            if (originalParam == null || originalParam.isEmpty()) {
                originalParam = (Properties) retVal.clone();
            }
            if (savedArguments == null || savedArguments.isEmpty()) {
                savedArguments = (Properties) retVal.clone();
            }
            logger.debug9(".. saved remote dir: " + savedArguments.getProperty(conSettingREMOTE_DIR));
            arguments = retVal;
            try {
                String host = sosString.parseToString(arguments, conParamHOST);
                if (!host.isEmpty()) {
                    arguments.put("remote_host_ip", java.net.InetAddress.getByName(host).getHostAddress());
                }
            } catch (java.net.UnknownHostException uhe) {
            }
            if (!writeBannerHeader && !sosString.parseToString(arguments, conSettingOPERATION).startsWith("install")) {
                if (banner) {
                    getLogger().info(getBanner(true));
                } else {
                    getLogger().debug1(getBanner(true));
                }
                writeBannerHeader = true;
            }
            String strJumpHostName = getString(conParamJUMP_HOST);
            if (!strJumpHostName.isEmpty()) {
                int jumpParameterLength = strJumpHostName.length() + getString("jump_port").length() + getString("jump_user").length();
                if (jumpParameterLength > 0 && !conOperationEXECUTE.equals(getString(conSettingOPERATION))) {
                    flgJumpTransferDefined = true;
                }
            }
            createHistoryFile();
            checkSchedulerRequest();
        } catch (Exception e) {
            RaiseException("Failed to merge program arguments and settings: " + e.getMessage(), e);
        }
    }

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
                    String msg = ".." + key + "=" + normalizedPassword(value) + " ";
                    value = normalizedFields(value, environment, "${", "}");
                    value = normalizedFields(value, environment, "%", "%");
                    newProp.put(key, value);
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to substitute environment variables in configuration file, cause: " + e.toString());
            return prop;
        }
        return newProp;
    }

    private void createHistoryFile() throws Exception {
        boolean writeHeaderFields = false;
        getLogger().debug1("calling " + sos.util.SOSClassUtil.getMethodName());
        try {
            if (getBool("testmode")) {
                return;
            }
            if (tempHistoryFile != null && tempHistoryFile.exists()) {
                return;
            }
            if (!sosString.parseToString(arguments, "history").isEmpty()) {
                if (conOperationSEND.equals(sosString.parseToString(arguments, conSettingOPERATION))
                        || conOperationRECEIVE.equals(sosString.parseToString(arguments, conSettingOPERATION))) {
                    historyFile = new File(sosString.parseToString(arguments, "history"));
                    if (!historyFile.exists()) {
                        if (historyFile.createNewFile()) {
                            getLogger().info("creating global history file " + historyFile.getAbsolutePath());
                        } else {
                            getLogger().warn("could not create history file : " + historyFile.getAbsolutePath());
                        }
                    }
                    if (historyFile.length() == 0) {
                        writeHeaderFields = true;
                    }
                    if (!historyFile.canWrite()) {
                        getLogger().warn("history file is read only: " + historyFile.getAbsolutePath());
                        return;
                    }
                    tempHistoryFile = File.createTempFile("sosftp_history", ".csv", new File(System.getProperty("java.io.tmpdir")));
                    tempHistoryFile.deleteOnExit();
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
            } else {
                if (getHistory() != null) {
                    getHistory().close();
                    history = null;
                }
            }
        } catch (Exception e) {
            RaiseException("error in  " + sos.util.SOSClassUtil.getMethodName() + " could not create history file, cause: " + e.getMessage(), e);
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
            lockHistoryFile = new File(System.getProperty("java.io.tmpdir"), historyFile.getName() + ".lock");
            if (lockHistoryFile.exists()) {
                getLogger().debug9("..history lock File exists.");
                getLogger().debug("timestamp of last modification of history file: " + lockHistoryFile.lastModified());
                for (int i = 0; i < countOfDelay; i++) {
                    getLogger().debug5("..history lock file exist. wait 5 Seconds.");
                    Thread.sleep(delay + 1000);
                    if (!lockHistoryFile.exists()) {
                        break;
                    }
                }
                if (lockHistoryFile.exists()) {
                    getLogger().debug5("..history lock file exist.");
                    bra = new BufferedReader(new FileReader(lockHistoryFile));
                    if ((pid = bra.readLine()) != null) {
                        getLogger().debug("reading from history lock file: pid = " + pid);
                        Process proc = null;
                        if (System.getProperty("os.name").toLowerCase().indexOf("windows") > -1) {
                            proc = Runtime.getRuntime().exec("tasklist /FI \"PID eq " + pid + "\"");
                        } else {
                            proc = Runtime.getRuntime().exec("ps -p " + pid);
                        }
                        procout = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                        String s;
                        while ((s = procout.readLine()) != null) {
                            getLogger().debug5("command output =\t" + s);
                            if (s.indexOf(pid) > -1) {
                                existPID = true;
                                break;
                            }
                        }
                        if (bra != null) {
                            bra.close();
                        }
                        if (procout != null) {
                            procout.close();
                        }
                    }
                }
            }
            if (!existPID && !sosString.parseToString(pid).trim().isEmpty()) {
                getLogger().debug("History lock File is deleting cause Process ID not exist.");
                if (lockHistoryFile.delete()) {
                    getLogger().info("History lock File successfully deleted, cause Process ID not exist.");
                } else {
                    RaiseException("History lock File " + lockHistoryFile.getCanonicalPath() + " could not delete. There is no Process Id exist[pid="
                            + pid + "]");
                }
                existPID = false;
            }

            if (existPID) {
                RaiseException("Could not write in History File, cause there is existing History Lock File.");
            }
            return existPID;
        } catch (Exception e) {
            RaiseException("error in  " + sos.util.SOSClassUtil.getMethodName() + " while looking-up existing history lock file, cause: "
                    + e.getMessage(), e);
        } finally {
            if (bra != null) {
                bra.close();
            }
            if (procout != null) {
                procout.close();
            }
        }
        return false;
    }

    private void checkHistoryField(final String historyFields_) throws Exception {
        BufferedReader br = null;
        BufferedWriter bw = null;
        String thisLine = "";
        boolean historyFilesChanged = false;
        try {
            getLogger().debug1("calling " + sos.util.SOSClassUtil.getMethodName());
            br = new BufferedReader(new FileReader(historyFile));
            int line = 0;
            while ((thisLine = br.readLine()) != null) {
                if (line > 0 && !historyFilesChanged) {
                    return;
                }
                if (line == 0) {
                    for (int i = 0; i < historyEntrys.size(); i++) {
                        newHistoryFields = newHistoryFields + ";" + historyEntrys.get(i).toString().substring("history_entry_".length());
                    }
                    String[] splitnewHistoryFields = newHistoryFields.split(";");
                    for (int i = 0; i < splitnewHistoryFields.length; i++) {
                        if (thisLine.indexOf(";" + splitnewHistoryFields[i] + ";") == -1 && !thisLine.startsWith(splitnewHistoryFields[i] + ";")
                                && !thisLine.endsWith(splitnewHistoryFields[i])) {
                            thisLine = thisLine + ";" + splitnewHistoryFields[i];
                            historyFilesChanged = true;
                        }
                    }
                    historyFields = thisLine;
                    if (historyFilesChanged && !existHistoryLockFile()) {
                        createHistoryLockFile();
                        bw = new BufferedWriter(new FileWriter(historyFile));
                        bw.write(thisLine);
                        bw.newLine();
                    }
                } else {
                    bw.write(thisLine);
                    bw.newLine();
                }
                line++;
            }
        } catch (Exception e) {
            RaiseException("\n -> ..error in " + SOSClassUtil.getMethodName() + " " + e.getMessage(), e);
        } finally {
            if (bw != null) {
                bw.close();
            }
            if (br != null) {
                br.close();
            }
            if (historyFilesChanged) {
                removeHistoryLockFile();
            }
        }
    }

    private void createHistoryLockFile() throws Exception {
        BufferedWriter lockBW = null;
        try {
            lockBW = new BufferedWriter(new FileWriter(lockHistoryFile));
            lockBW.write(sosString.parseToString(arguments, "current_pid"));
        } catch (Exception e) {
            RaiseException("could not create a history lock file, cause: " + e.getMessage(), e);
        } finally {
            if (lockBW != null) {
                lockBW.close();
            }
        }
    }

    private void removeHistoryLockFile() throws Exception {
        try {
            getLogger().debug1("calling " + sos.util.SOSClassUtil.getMethodName());
            getLogger().debug9("lockHistoryFile=" + lockHistoryFile.getCanonicalPath());
            if (lockHistoryFile != null && lockHistoryFile.exists() && !lockHistoryFile.delete()) {
                getLogger().debug3("history lock file could not be deleted: " + lockHistoryFile.getCanonicalPath());
                Thread.sleep(1000);
                if (!lockHistoryFile.delete()) {
                    RaiseException("history lock file could not be deleted: " + lockHistoryFile.getCanonicalPath());
                }
            }
        } catch (Exception e) {
            RaiseException("could not delete a history lock file, cause: " + e.getMessage(), e);
        }
    }

    protected void hasJumpArguments() throws Exception {
        String curCommands = "";
        try {
            if (!flgJumpTransferDefined) {
                return;
            }
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
            if (!conOperationREMOVE.equals(getString(conSettingOPERATION)) && !conOperationRECEIVE.equals(getString(conSettingOPERATION))) {
                noJumpParameter.add(conSettingFILE_SPEC);
            }
            arguments.put("xx_make_temp_directory_xx", "ok");
            if (!conOperationREMOVE.equalsIgnoreCase(getString(conSettingOPERATION))) {
                curCommands =
                        getString(conSettingsJUMP_COMMAND) + " -operation=make_temp_directory -root=" + sosString.parseToString(arguments, "root");
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
                if (!key.startsWith("jump") && !key.startsWith(";") && !key.startsWith("history_entry_") && !"profile".equals(key)
                        && !"settings".equals(key) && !conSettingLOCAL_DIR.equals(key) && !conSettingFILE_PATH.equals(key)
                        && !conSettingREMOVE_FILES.equals(key) && !noJumpParameter.contains(key)) {
                    String val = sosString.parseToString(arguments, key);
                    if (val.isEmpty()) {
                        continue;
                    }
                    if (conParamPASSWORD.equals(key)) {
                        val = new SOSCommandline().getExternalPassword(val, logger);
                    }
                    arg = arg + " -" + key + "=" + "\"" + val + "\"";
                    if (!conSettingOPERATION.equals(key) && !conParamMAKE_DIRS.equals(key) && !"root".equals(key) && !"transactional".equals(key)
                            && conOperationRECEIVE.equals(getString(conSettingOPERATION))) {
                        arg4postCommands = arg4postCommands + " -" + key + "=" + "\"" + val + "\"";
                    }
                }
            }
            if (conOperationRECEIVE.equalsIgnoreCase(getString(conSettingOPERATION)) && getBool("skip_transfer") && getBool(conSettingREMOVE_FILES)) {
                arg = arg + " -remove_files=\"yes\"";
            }
            if (getString(conSettingOPERATION).startsWith(conOperationRECEIVE) && getBool("skip_transfer")) {
                arg = arg + " -verbose=\"9\"";
            } else {
                arg = arg + " -verbose=\"" + getLogger().getLogLevel() + "\"";
            }
            if (getString(conSettingOPERATION).startsWith("install")) {
                arg += getKeywordValuePair(conSettingOPERATION, conOperationSEND);
            }
            if (conOperationREMOVE.equalsIgnoreCase(getString(conSettingOPERATION))) {
                arg = arg + " -remove_files=yes";
            } else {
                arg = arg + " -local_dir=\"" + tempJumpRemoteDir + "\"";
            }
            if (conOperationRECEIVE.equalsIgnoreCase(getString(conSettingOPERATION))
                    && !sosString.parseToString(arguments, conSettingFILE_PATH).isEmpty()) {
                arg = arg + " -file_path=\"" + sosString.parseToString(arguments, conSettingFILE_PATH) + "\"";
            }
            if (conOperationREMOVE.equalsIgnoreCase(getString(conSettingOPERATION))
                    && !sosString.parseToString(arguments, conSettingFILE_PATH).isEmpty()) {
                curCommands = splitFilePathCommand(arg);
            } else if (conOperationSEND.equalsIgnoreCase(getString(conSettingOPERATION)) || getString(conSettingOPERATION).startsWith("install")) {
                arg = arg + " -remove_files=yes";
                curCommands = getString(conSettingsJUMP_COMMAND) + " " + arg;
                if (!conOperationREMOVE.equalsIgnoreCase(getString(conSettingOPERATION))) {
                    postCommands = curCommands;
                }
                curCommands += commandDelimiter;
            } else {
                curCommands = getString(conSettingsJUMP_COMMAND) + " " + arg + commandDelimiter;
            }
            if (!sosString.parseToString(postCommands).isEmpty()) {
                if (!isTransferOperation() && (!getString(conParamREPLACEMENT).isEmpty() || !getString(conParamREPLACING).isEmpty())) {
                    postCommands += getKeywordValuePair(conParamREPLACEMENT) + getKeywordValuePair(conParamREPLACING);
                }
                postCommands += commandDelimiter;
                getLogger().debug1("postCommands:  " + normalizedPassword(postCommands));
            }
            this.setCommands(curCommands.split(this.getCommandDelimiter()));
            getLogger().debug1("curCommands:  " + normalizedPassword(curCommands));
            arguments.put(conParamMAKE_DIRS, "yes");
            arguments.put("jump_remote_dir", tempJumpRemoteDir);
            if (!arguments.containsKey(conParamJUMP_PROTOCOL)) {
                arguments.put(conParamJUMP_PROTOCOL, "sftp");
            }
            Properties prop = new Properties();
            java.util.Iterator keys2 = arguments.keySet().iterator();
            while (keys2.hasNext()) {
                String key = sosString.parseToString(keys2.next());
                if (key.startsWith("jump_")) {
                    prop.put(key.substring(5), sosString.parseToString(arguments, key));
                }
            }
            arguments.putAll(prop);
        } catch (Exception e) {
            RaiseException("Failed to jump to: " + e.getMessage(), e);
        }
    }

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
            if (filePathPartAfter.length() >= maxCommandLineLength - commandLineLength && !filePathPartBefore.isEmpty()) {
                curCommands = curCommands + curCmdArguments + "\"" + filePathPartBefore + "\"" + this.getCommandDelimiter();
                filePathPartAfter = filePath + ";";
            }
        }
        if (!filePathPartAfter.isEmpty()) {
            curCommands = curCommands + curCmdArguments + "\"" + filePathPartAfter + "\"" + this.getCommandDelimiter();
        }
        return curCommands;
    }

    protected boolean isTransferOperation() {
        boolean flgR = false;
        String strOp = getString(conSettingOPERATION);
        if (conOperationSEND.equalsIgnoreCase(strOp) || conOperationRECEIVE.equalsIgnoreCase(strOp)) {
            flgR = true;
        }
        return flgR;
    }

    private static Properties getEnvVars(final SOSLogger logger_) throws Exception {
        Properties envVars = new Properties();
        try {
            if (logger != null) {
                logger.debug5("reading environment");
            }
            Process p = null;
            Runtime r = Runtime.getRuntime();
            String OS = System.getProperty("os.name").toLowerCase();
            environment = new Properties();
            if (OS.indexOf("windows 9") > -1) {
                p = r.exec("command.com /c set");
            } else if (OS.indexOf("nt") > -1 || OS.indexOf("windows") > -1) {
                p = r.exec("cmd.exe /c set");
            } else {
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
                    } else if (key.toLowerCase().startsWith("current_pid") || key.toLowerCase().startsWith("ppid")) {
                        value = line.substring(idx + 1);
                        envVars.setProperty(key.toLowerCase(), value);
                    } else if (key.toLowerCase().indexOf("scheduler_param_") > -1) {
                        value = line.substring(idx + 1);
                        schedulerParams.setProperty(key.toLowerCase().substring("scheduler_param_".length()), value);
                        if (logger != null) {
                            logger.debug5(".. environment [" + key + "]: " + value);
                        }
                    }
                    environment.put(key, line.substring(idx + 1));
                }
            }
            return envVars;
        } catch (Exception e) {
            if (logger != null) {
                logger.warn("[ERROR] could not read environment, cause: " + e.getMessage());
            }
            RaiseException("error occurred reading environment: " + e.getMessage(), e);
        }
        return envVars;
    }

    private boolean removeTempDirectory() {
        try {
            getLogger().debug1("***********remove Temporary Directory*************");
            String inputFile = "";
            String[] split = sosString.parseToString(arguments, "input").split(";");
            for (String element : split) {
                inputFile = sosString.parseToString(element);
                getLogger().debug5("remove: " + inputFile);
                if (inputFile.isEmpty()) {
                    getLogger().debug1("no directory/file has been specified for removal: " + inputFile);
                    return false;
                }
                getLogger().debug1("..parameter [input=" + inputFile + "]");
                File tmpDir = new File(inputFile);
                if (!tmpDir.exists()) {
                    getLogger().debug1("directory/file to be removed does not exist: " + tmpDir.getCanonicalPath());
                    return false;
                }
                File[] listOfFiles = tmpDir.listFiles();
                for (int j = 0; j < listOfFiles.length; j++) {
                    if (listOfFiles.length > 0) {
                        if (!listOfFiles[j].delete()) {
                            getLogger().warn(listOfFiles[j].getCanonicalPath() + " could not be deleted");
                        } else {
                            getLogger().debug1(listOfFiles[j].getCanonicalPath() + " was successfully deleted");
                        }
                    }
                }
                if (tmpDir.delete()) {
                    getLogger().debug1(tmpDir.getCanonicalPath() + " was successfully deleted");
                } else {
                    getLogger().warn(tmpDir.getCanonicalPath() + " could not be deleted");
                }
            }
        } catch (Exception e) {
            try {
                getLogger().warn("error deleting temporary directory, cause: " + e.toString());
            } catch (Exception ex) {

            }
        }
        return true;
    }

    private boolean makeTempDirectory() throws Exception {
        String output = "";
        try {
            getLogger().debug1("***********make Temporary Directory*************");
            if (!sosString.parseToString(arguments, "root").isEmpty()) {
                output = File.createTempFile("sos_ftp", null, new File(normalized(sosString.parseToString(arguments, "root")))).getCanonicalPath();
            } else {
                output = File.createTempFile("sos_ftp", null, new File(System.getProperty("java.io.tmpdir"))).getCanonicalPath();
            }
            tempJumpRemoteDir = output;
            getLogger().info("*******ftp transfer directory is:" + output + "******************");
            if (new File(output).getCanonicalFile().exists()) {
                new File(output).getCanonicalFile().delete();
            }
            if (!new File(output).getCanonicalFile().mkdirs()) {
                RaiseException("could not create temporary directory");
            }
        } catch (Exception e) {
            getLogger().warn("error creating temporary directory, cause: " + e.toString());
            RaiseException("error creating temporary directory, cause: " + e.getMessage(), e);
        }
        return true;
    }

    protected String normalized(String str) {
        str = str.replaceAll(conRegExpBackslash, "/");
        return str.endsWith("/") || str.endsWith("\\") ? str : str + "/";
    }

    public String getBanner(final boolean header) throws Exception {
        String curBannerHeader = "";
        String curBannerFooter = "";
        String banner = "";
        try {
            if (header) {
                curBannerHeader = bannerHeader;
                if (!sosString.parseToString(arguments, "banner_header").isEmpty()) {
                    String b = sosString.parseToString(arguments, "banner_header");
                    File fBanner = new File(b);
                    if (!fBanner.exists()) {
                        getLogger().warn("[banner_header=" + b + "] does not exist. Using default banner");
                    } else {
                        curBannerHeader = sos.util.SOSFile.readFile(fBanner);
                    }
                }
                Iterator it = arguments.keySet().iterator();
                while (it.hasNext()) {
                    String key = sosString.parseToString(it.next());
                    if (!key.isEmpty()) {
                        if (conSettingFILE_SPEC.equals(key) && !sosString.parseToString(arguments, conSettingFILE_PATH).isEmpty()) {
                            continue;
                        }
                        int pos1 = -1;
                        int pos2 = -1;
                        boolean loop = true;
                        while (loop) {
                            pos1 = curBannerHeader.indexOf("%{" + key + "}");
                            pos2 = pos1 + ("%{" + key + "}").length();
                            if (pos1 > -1 && pos2 > pos1) {
                                curBannerHeader =
                                        curBannerHeader.substring(0, pos1) + sosString.parseToString(arguments, key)
                                                + curBannerHeader.substring(pos2);
                            }
                            pos1 = curBannerHeader.indexOf("%{" + key + "}", pos2);
                            if (pos1 == -1) {
                                loop = false;
                            }
                        }
                    }
                }
                curBannerHeader = clearBanner(curBannerHeader);
                banner = curBannerHeader;
            } else {
                curBannerFooter = bannerFooter;
                if (!sosString.parseToString(arguments, "banner_footer").isEmpty()) {
                    String b = sosString.parseToString(arguments, "banner_footer");
                    File fBanner = new File(b);
                    if (!fBanner.exists()) {
                        getLogger().warn("[banner_footer=" + b + "] does not exist. Using default banner");
                    } else {
                        curBannerFooter = sos.util.SOSFile.readFile(fBanner);
                    }
                }
                Iterator it = arguments.keySet().iterator();
                while (it.hasNext()) {
                    String key = sosString.parseToString(it.next());
                    if (!key.isEmpty()) {
                        if (conSettingFILE_SPEC.equals(key) && !sosString.parseToString(arguments, conSettingFILE_PATH).isEmpty()) {
                            continue;
                        }
                        boolean loop = true;
                        int pos3 = -1;
                        int pos4 = -1;
                        while (loop) {
                            pos3 = curBannerFooter.indexOf("%{" + key + "}");
                            pos4 = pos3 + ("%{" + key + "}").length();
                            if (pos3 > -1 && pos4 > pos3) {
                                curBannerFooter =
                                        curBannerFooter.substring(0, pos3) + sosString.parseToString(arguments, key)
                                                + curBannerFooter.substring(pos4);
                            }
                            pos3 = curBannerFooter.indexOf("%{" + key + "}", pos4);
                            if (pos3 == -1) {
                                loop = false;
                            }
                        }
                    }
                }
                curBannerFooter = clearBanner(curBannerFooter);
                banner = curBannerFooter;
            }
        } catch (Exception e) {
            RaiseException("error occurred getting banner: " + e.getMessage(), e);
        }
        return banner;
    }

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
            if (pos1 == -1) {
                loop = false;
            }
        }
        return str;
    }

    protected Connection getSshConnection() {
        return sshConnection;
    }

    protected void setSshConnection(final Connection sshConnection) {
        this.sshConnection = sshConnection;
    }

    protected boolean install() throws Exception {
        return false;
    }

    protected void initSOSFTP() throws Exception {
        try {
            try {
                sosftp = new SOSFTP(host, port);
            } catch (Exception e) {
                this.getLogger().debug("SOSFtp exception raised");
                throw e;
            }
            ftpClient = sosftp;
            this.getLogger().debug("..ftp server reply [init] [host=" + host + "], [port=" + port + "]: " + ftpClient.getReplyString());
            if (account != null && !account.isEmpty()) {
                isLoggedIn = sosftp.login(user, new SOSCommandline().getExternalPassword(password, logger), account);
                this.getLogger().debug("..ftp server reply [login] [user=" + user + "], [account=" + account + "]: " + ftpClient.getReplyString());
            } else {
                isLoggedIn = sosftp.login(user, new SOSCommandline().getExternalPassword(password, logger));
                this.getLogger().debug("..ftp server reply [login] [user=" + user + "]: " + ftpClient.getReplyString());
            }
            if (!isLoggedIn || sosftp.getReplyCode() > ERROR_CODE) {
                logger.info("..sftp server login failed [user=" + user + "], [host=" + host + "]: " + ftpClient.getReplyString());
                throw new Exception(ftpClient.getReplyString());
            }
            postLoginOperations();
        } catch (Exception e) {
            throw e;
        }
    }

    private void postLoginOperations() throws Exception {
        utf8Supported = false;
        restSupported = false;
        mlsdSupported = false;
        modezSupported = false;
        dataChannelEncrypted = false;
        if (flgCheckServerFeatures) {
            String strCmd = "FEAT";
            sosftp.sendCommand(strCmd);
            this.getLogger().info("..ftp server reply [" + strCmd + "]: " + ftpClient.getReplyString());
        }
    }

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
        } catch (Exception e) {
            isLoggedIn = false;
            logger.info("..sftp server login failed [user=" + user + "], [host=" + host + "]: " + e);
            throw e;
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
                throw new Exception("..ftps server reply [login failed] [user=" + user + "], [account=" + account + "]: "
                        + ftpClient.getReplyString());
            }
        } catch (Exception e) {
            logger.info("..sftp server login failed [user=" + user + "], [host=" + host + "]: " + e);
            throw e;
        }
    }

    public Properties getArguments() {
        return arguments;
    }

    private BufferedWriter getHistory() {
        return history;
    }

    protected void writeHistory(final String localFilename, final String remoteFilename) throws Exception {
        String EMPTY = "";
        String hist = "";
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
        String jump_host = "";
        String jump_host_ip = "";
        String jump_port = "";
        String jump_protocol = "";
        String jump_user = "";
        try {
            if (!conOperationSEND.equalsIgnoreCase(sosString.parseToString(arguments, conSettingOPERATION))
                    && !conOperationRECEIVE.equalsIgnoreCase(sosString.parseToString(arguments, conSettingOPERATION))) {
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
            if (!sosString.parseToString(localFilename).isEmpty()) {
                localFile = new File(localFilename);
            }
            UUID objUUID = UUID.randomUUID();
            guid = objUUID.toString();
            mandator = !sosString.parseToString(arguments, "mandator").isEmpty() ? sosString.parseToString(arguments, "mandator") : defaultMandator;
            transfer_timestamp = sos.util.SOSDate.getCurrentTimeAsString();
            pid = !sosString.parseToString(arguments, "current_pid").isEmpty() ? sosString.parseToString(arguments, "current_pid") : "0";
            ppid = !sosString.parseToString(arguments, "ppid").isEmpty() ? sosString.parseToString(arguments, "ppid") : "0";
            operation = sosString.parseToString(arguments, conSettingOPERATION);
            localhost = sosString.parseToString(arguments, "localhost");
            localhost_ip = sosString.parseToString(arguments, "local_host_ip");
            local_user = sosString.parseToString(System.getProperty("user.name"));
            if (flgJumpTransferDefined) {
                remote_host = sosString.parseToString(originalParam, conParamHOST);
                if (!remote_host.isEmpty()) {
                    remote_host_ip = java.net.InetAddress.getByName(remote_host).getHostAddress();
                }
                remote_user = sosString.parseToString(originalParam, "user");
                protocol = sosString.parseToString(originalParam, conParamPROTOCOL);
            } else {
                remote_host = sosString.parseToString(arguments, conParamHOST);
                remote_host_ip = sosString.parseToString(arguments, "remote_host_ip");
                remote_user = sosString.parseToString(arguments, "user");
                protocol = sosString.parseToString(arguments, conParamPROTOCOL);
            }
            port = sosString.parseToString(arguments, conParamPORT);
            if (conOperationSEND.equals(sosString.parseToString(arguments, conSettingOPERATION))) {
                local_dir =
                        localFile != null && localFile.getParent() != null ? clearCRLF(localFile.getParent()) : clearCRLF(sosString.parseToString(
                                arguments, conSettingLOCAL_DIR));
            } else {
                local_dir = clearCRLF(sosString.parseToString(arguments, conSettingLOCAL_DIR));
            }
            if (sosString.parseToString(local_dir).isEmpty()) {
                local_dir = ".";
            }
            if (flgJumpTransferDefined) {
                remote_dir = clearCRLF(sosString.parseToString(originalParam, "remote_dir"));
            } else {
                remote_dir = clearCRLF(sosString.parseToString(arguments, "remote_dir"));
            }
            local_filename = localFile != null ? clearCRLF(localFile.getName()) : EMPTY;
            remote_filename = !sosString.parseToString(remoteFilename).isEmpty() ? clearCRLF(new File(remoteFilename).getName()) : EMPTY;
            fileSize = !sosString.parseToString(arguments, "file_size").isEmpty() ? sosString.parseToString(arguments, "file_size") : EMPTY;
            if (sosString.parseToString(localFilename).isEmpty() || sosString.parseToString(remoteFilename).isEmpty()) {
                md5 = EMPTY;
            } else {
                File f4md5 =
                        localFile.getName().equals(new File(remoteFilename).getName()) ? new File(remoteFilename)
                                : conOperationRECEIVE.equals(sosString.parseToString(arguments, conSettingOPERATION)) ? new File(
                                        normalized(local_dir) + new File(remoteFilename).getName()) : new File(normalized(local_dir)
                                        + localFile.getName());
                if (f4md5.exists()) {
                    getLogger().debug9("md5 for " + f4md5.getAbsolutePath());
                    md5 = sos.util.SOSCrypt.MD5encrypt(f4md5);
                } else {
                    md5 = EMPTY;
                }
            }
            status =
                    sosString.parseToString(arguments, conParameterSTATUS).isEmpty() ? conParamValueSUCCESS : sosString.parseToString(arguments,
                            conParameterSTATUS);
            last_error_message =
                    clearCRLF(getLogger().getError() != null && !getLogger().getError().isEmpty() ? getLogger().getError() : getLogger().getWarning());
            last_error_message = normalizedPassword(sosString.parseToString(last_error_message));
            log_filename = sosString.parseToString(arguments, "log_filename");
            jump_host = sosString.parseToString(arguments, conParamJUMP_HOST);
            if (!jump_host.isEmpty()) {
                jump_host_ip = java.net.InetAddress.getByName(host).getHostAddress();
            }
            jump_port = sosString.parseToString(arguments, "jump_port");
            jump_protocol = sosString.parseToString(arguments, conParamJUMP_PROTOCOL);
            jump_user = sosString.parseToString(arguments, "jump_user");
            Properties objSchedulerOrderParameterSet = new Properties();
            objSchedulerOrderParameterSet.put("guid", guid);
            objSchedulerOrderParameterSet.put("mandator", mandator);
            objSchedulerOrderParameterSet.put("transfer_timestamp", transfer_timestamp);
            objSchedulerOrderParameterSet.put("pid", pid);
            objSchedulerOrderParameterSet.put("ppid", ppid);
            objSchedulerOrderParameterSet.put(conSettingOPERATION, operation);
            objSchedulerOrderParameterSet.put("localhost", localhost);
            objSchedulerOrderParameterSet.put("localhost_ip", localhost_ip);
            objSchedulerOrderParameterSet.put("local_user", local_user);
            objSchedulerOrderParameterSet.put("remote_host", remote_host);
            objSchedulerOrderParameterSet.put("remote_host_ip", remote_host_ip);
            objSchedulerOrderParameterSet.put("remote_user", remote_user);
            objSchedulerOrderParameterSet.put(conParamPROTOCOL, protocol);
            objSchedulerOrderParameterSet.put(conParamPORT, port);
            objSchedulerOrderParameterSet.put(conSettingLOCAL_DIR, local_dir);
            objSchedulerOrderParameterSet.put("remote_dir", remote_dir);
            objSchedulerOrderParameterSet.put("local_filename", local_filename);
            objSchedulerOrderParameterSet.put("remote_filename", remote_filename);
            objSchedulerOrderParameterSet.put("file_size", fileSize);
            objSchedulerOrderParameterSet.put("md5", md5);
            objSchedulerOrderParameterSet.put(conParameterSTATUS, status);
            objSchedulerOrderParameterSet.put("last_error_message", last_error_message);
            objSchedulerOrderParameterSet.put("log_filename", log_filename);
            objSchedulerOrderParameterSet.put(conParamJUMP_HOST, jump_host);
            objSchedulerOrderParameterSet.put("jump_host_ip", jump_host_ip);
            objSchedulerOrderParameterSet.put("jump_port", jump_port);
            objSchedulerOrderParameterSet.put(conParamJUMP_PROTOCOL, jump_protocol);
            objSchedulerOrderParameterSet.put("jump_user", jump_user);
            if (history != null) {
                String[] splitHistFields = historyFields.split(";");
                for (int i = 0; i < splitHistFields.length; i++) {
                    String key = sosString.parseToString(splitHistFields[i]);
                    String value =
                            sosString.parseToString(objSchedulerOrderParameterSet, key).length() == 0 ? sosString.parseToString(arguments,
                                    "history_entry_".concat(key)) : sosString.parseToString(objSchedulerOrderParameterSet, key);
                    hist = hist + (i == 0 ? "" : ";") + value; //
                }
                getLogger().debug9("history entry: " + hist);
                if (transActional) {
                    if (transactionalHistoryFile == null) {
                        transactionalHistoryFile = new ArrayList();
                    }
                    transactionalHistoryFile.add(hist);
                } else {
                    history.write(hist);
                    history.newLine();
                }
            }
            if (sendSchedulerSignale) {
                String schedulerMessages = "";
                if (!sosString.parseToString(arguments, conParamSCHEDULER_MESSAGE).isEmpty()) {
                    schedulerMessages = sosString.parseToString(arguments, conParamSCHEDULER_MESSAGE);
                    schedulerMessages = normalizedFields(schedulerMessages, objSchedulerOrderParameterSet, "%{", "}");
                    schedulerMessages = normalizedFields(schedulerMessages, arguments, "%{", "}");
                } else {
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
                    if (transactionalSchedulerRequestFile == null) {
                        transactionalSchedulerRequestFile = new ArrayList();
                    }
                    transactionalSchedulerRequestFile.add(schedulerMessages);
                } else {
                    sendSchedulerRequest(schedulerMessages);
                }
            }
        } catch (Exception e) {
            LOGGER.warn("error occurred writing history entry, cause: " + e.getMessage(), e);
        }
    }

    private String clearCRLF(final String txt) {
        return txt.replaceAll("(\r\n|\r|\n|\n\r|;)", ",");
    }

    private void appendHistoryFile() throws Exception {
        int repeat = 3;
        int repeatIntervall = 1;
        try {
            if (!sosString.parseToString(arguments, "history_repeat").isEmpty()) {
                try {
                    repeat = Integer.parseInt(sosString.parseToString(arguments, "history_repeat"));
                } catch (Exception e) {
                    repeat = 3;
                }
            }
            if (!sosString.parseToString(arguments, "history_repeat_interval").isEmpty()) {
                try {
                    repeatIntervall = Integer.parseInt(sosString.parseToString(arguments, "history_repeat_interval"));
                } catch (Exception e) {
                    repeatIntervall = 1;
                }
            }
            boolean hasError = getLogger().hasErrors() || getLogger().hasWarnings();
            if (getHistory() != null) {
                for (int j = 0; transactionalHistoryFile != null && j < transactionalHistoryFile.size(); j++) {
                    String hist = sosString.parseToString(transactionalHistoryFile.get(j));
                    if (hasError) {
                        hist =
                                hist.replaceAll(";success;;", ";error;"
                                        + clearCRLF(getLogger().getError() != null && !getLogger().getError().isEmpty() ? getLogger().getError()
                                                : getLogger().getWarning()) + ";");
                    }
                    history.write(hist);
                    history.newLine();
                    getLogger().debug9("history entry: " + hist);
                }
            }
        } catch (Exception e) {
            getLogger().warn("error occurred writing history, cause: " + e.getMessage());
        } finally {
            for (int i = repeat; i > 0; i--) {
                try {
                    if (getHistory() != null) {
                        getHistory().close();
                        if (!existHistoryLockFile()) {
                            createHistoryLockFile();
                            sos.util.SOSFile.copyFile(getTempHistoryFile(), historyFile, true);
                            removeHistoryLockFile();
                        }
                        if (!tempHistoryFile.delete()) {
                            getLogger().info(tempHistoryFile.getAbsolutePath() + " could not delete");
                        }
                    }
                    break;
                } catch (Exception e) {
                    if (i == 1) {
                        getLogger().warn("could not write in History File, cause: " + e.toString());
                    }
                    Thread.sleep(repeatIntervall * 1000);
                }
            }
            if (tempHistoryFile != null && tempHistoryFile.exists() && !tempHistoryFile.delete()) {
                getLogger().info(tempHistoryFile.getAbsolutePath() + " could not delete");
            }
        }
    }

    private void sendTransactionalSchedulerRequestFile() throws Exception {
        if (transActional && transactionalSchedulerRequestFile != null) {
            for (int i = 0; i < transactionalSchedulerRequestFile.size(); i++) {
                String msg = sosString.parseToString(transactionalSchedulerRequestFile.get(i));
                boolean hasError = getLogger().hasErrors() || getLogger().hasWarnings();
                if (hasError) {
                    msg = msg.replaceAll("<param name='status' value='success'/>", "<param name='status' value='error'/>");
                    msg =
                            msg.replaceAll("<param name='last_error_message' value=''/>", "<param name='last_error_message' value='"
                                    + clearCRLF(getLogger().getError() != null && getLogger().getError().length() > 0 ? getLogger().getError()
                                            : getLogger().getWarning()) + "'/>");
                }
                sendSchedulerRequest(msg);
            }
        }
    }

    public void sendSchedulerRequest(String msg) throws Exception {
        String _host = "";
        int _port = 0;
        DatagramSocket udpSocket = null;
        try {
            _host = sosString.parseToString(arguments, conParamSCHEDULER_HOST);
            if (!sosString.parseToString(arguments, conParamSCHEDULER_PORT).isEmpty()) {
                _port = Integer.parseInt(sosString.parseToString(arguments, conParamSCHEDULER_PORT));
            }
            if (_host == null || _host.isEmpty()) {
                throw new Exception("JobScheduler host name missing.");
            }
            if (_port == 0) {
                throw new Exception("JobScheduler port missing.");
            }
            udpSocket = new DatagramSocket();
            udpSocket.connect(InetAddress.getByName(_host), _port);
            if (msg.indexOf("<?xml") == -1) {
                msg = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>" + msg + "\r\n";
            }
            getLogger().debug9("sending Job Scheduler message: " + msg);
            byte[] commandBytes = msg.getBytes();
            udpSocket.send(new DatagramPacket(commandBytes, commandBytes.length, InetAddress.getByName(_host), _port));
        } catch (Exception e) {
            LOGGER.warn("could not send message to the Job Scheduler, cause " + e.getMessage(), e);
        } finally {
            if (udpSocket != null) {
                udpSocket.disconnect();
            }
        }
    }

    private void checkSchedulerRequest() throws Exception {
        String _command = "";
        String _host = "";
        int _port = 0;
        try {
            _host = sosString.parseToString(arguments, conParamSCHEDULER_HOST);
            if (!sosString.parseToString(arguments, conParamSCHEDULER_PORT).isEmpty()) {
                _port = Integer.parseInt(sosString.parseToString(arguments, conParamSCHEDULER_PORT));
            }
            _command = sosString.parseToString(arguments, conParamSCHEDULER_MESSAGE);
            if (sosString.parseToString(_host).concat(sosString.parseToString(arguments, conParamSCHEDULER_PORT)).concat(_command).isEmpty()) {
                return;
            }
            if (_host == null || _host.isEmpty()) {
                throw new RuntimeException("JobScheduler host name missing.");
            }
            if (_port == 0) {
                throw new RuntimeException("JobScheduler port missing.");
            }
            if (sosString.parseToString(arguments, conParamSCHEDULER_JOB_CHAIN).isEmpty()) {
                arguments.put(conParamSCHEDULER_JOB_CHAIN, conJobChainNameSCHEDULER_SOSFTP_HISTORY);
            }
            sendSchedulerSignale = true;
        } catch (Exception e) {
            throw new RuntimeException("error in checkSchedulerRequestParameter(): " + e.getMessage(), e);
        }
    }

    private static String normalizedFields(String txt, final Properties prop, final String startPrefix, final String endPrefix) throws Exception {
        try {
            Iterator it = prop.keySet().iterator();
            while (it.hasNext()) {
                String key = sosString.parseToString(it.next());
                if (!key.isEmpty()) {
                    int pos1 = -1;
                    int pos2 = -1;
                    boolean loop = true;
                    while (loop) {
                        pos1 = txt.indexOf(startPrefix + key + endPrefix);
                        if (pos1 > -1 && txt.indexOf("\\" + startPrefix + key + endPrefix) > -1
                                && txt.indexOf("\\" + startPrefix + key + endPrefix) == pos1 - 1) {
                            pos1 = -1;
                        }
                        pos2 = pos1 + (startPrefix + key + endPrefix).length();
                        if (pos1 > -1 && pos2 > pos1) {
                            txt = txt.substring(0, pos1) + sosString.parseToString(prop, key) + txt.substring(pos2);
                        }
                        pos1 = txt.indexOf(startPrefix + key + endPrefix, pos2);
                        if (pos1 == -1) {
                            loop = false;
                        }
                    }
                }
            }
        } catch (Exception e) {
            RaiseException("could not substitute parameters in: " + txt + ", cause: " + e.getMessage(), e);
        }
        return txt;
    }

    private File getTempHistoryFile() {
        return tempHistoryFile;
    }

    protected String getCommandDelimiter() {
        return commandDelimiter;
    }

    private void readJumpSettings() throws Exception {
        this.getBaseParameters();
        String commandScript = "";
        String commandScriptFileName = "";
        try {
            if (!getString("jump_command_delimiter").isEmpty()) {
                this.setCommandDelimiter(getString("jump_command_delimiter"));
                getLogger().debug1(".. parameter [jump_command_delimiter]: " + this.getCommandDelimiter());
            } else {
                this.setCommandDelimiter(DEFAULT_COMMAND_DELIMITER);
            }
            if (!getString("jump_command_script").isEmpty()) {
                commandScript = getString("jump_command_script");
                getLogger().debug1(".. parameter [jump_command_script]: " + commandScript);
            }
            if (!getString("jump_command_script_file").isEmpty()) {
                commandScriptFileName = getString("jump_command_script_file");
                getLogger().debug1(".. parameter [jump_command_script_file]: " + commandScriptFileName);
            }
            if (!getString(conSettingsJUMP_COMMAND).isEmpty()) {
                this.setCommands(getString(conSettingsJUMP_COMMAND).split(this.getCommandDelimiter()));
                getLogger().debug1(".. parameter [jump_command]: " + getString(conSettingsJUMP_COMMAND));
            }
            if (!getString("jump_ignore_error").isEmpty()) {
                if (getBool("jump_ignore_error")) {
                    ignoreError = true;
                } else {
                    ignoreError = false;
                }
                getLogger().debug1(".. parameter [jump_ignore_error]: " + ignoreError);
            } else {
                ignoreError = false;
            }
            if (!getString("jump_ignore_signal").isEmpty()) {
                if (sosString.parseToBoolean(getString("jump_ignore_signal"))) {
                    ignoreSignal = true;
                } else {
                    ignoreSignal = false;
                }
                getLogger().debug1(".. parameter [jump_ignore_signal]: " + ignoreSignal);
            } else {
                ignoreSignal = false;
            }
            if (!getString("jump_ignore_stderr").isEmpty()) {
                if (getBool("jump_ignore_stderr")) {
                    ignoreStderr = true;
                } else {
                    ignoreStderr = false;
                }
                getLogger().debug1(".. parameter [jump_ignore_stderr]: " + ignoreStderr);
            } else {
                ignoreStderr = false;
            }
        } catch (Exception e) {
            RaiseException("error occurred processing parameters: " + e.getMessage(), e);
        }
    }

    protected boolean getBool(final String pstrParameterName) {
        boolean flgRet = false;
        try {
            flgRet = sosString.parseToBoolean(arguments.get(pstrParameterName));
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return flgRet;
    }

    protected boolean remoteIsWindowsShell(final boolean withConn) throws Exception {
        try {
            if (withConn && getSshConnection() == null) {
                readJumpSettings();
                this.getBaseAuthentication();
            }
            return remoteIsWindowsShell();
        } catch (Exception e) {
            RaiseException("Failed to check if remote system has windows shell: " + e.getMessage(), e);
        } finally {
            if (withConn && this.getSshConnection() != null) {
                try {
                    this.getSshConnection().close();
                    this.setSshConnection(null);
                } catch (Exception ex) {
                    // gracefully ignore this error
                }
            }
        }
        return false;
    }

    protected Connection getBaseAuthentication() throws Exception {
        try {
            boolean isAuthenticated = false;
            this.setSshConnection(new Connection(host, port));
            if (proxyHost != null && !proxyHost.isEmpty()) {
                if (proxyUser != null && !proxyUser.isEmpty()) {
                    this.getSshConnection().setProxyData(new HTTPProxyData(proxyHost, proxyPort));
                } else {
                    this.getSshConnection().setProxyData(
                            new HTTPProxyData(proxyHost, proxyPort, proxyUser, new SOSCommandline().getExternalPassword(proxyPassword, logger)));
                }
            }
            this.getSshConnection().connect();
            if ("publickey".equalsIgnoreCase(authenticationMethod)) {
                File authenticationFile = new File(authenticationFilename);
                if (!authenticationFile.exists()) {
                    RaiseException("authentication file does not exist: " + authenticationFile.getCanonicalPath());
                }
                if (!authenticationFile.canRead()) {
                    RaiseException("authentication file not accessible: " + authenticationFile.getCanonicalPath());
                }
                isAuthenticated =
                        this.getSshConnection().authenticateWithPublicKey(user, authenticationFile,
                                new SOSCommandline().getExternalPassword(password, logger));
            } else if (authenticationMethod.equalsIgnoreCase(conParamPASSWORD)) {
                isAuthenticated = this.getSshConnection().authenticateWithPassword(user, new SOSCommandline().getExternalPassword(password, logger));
            }
            if (!isAuthenticated) {
                RaiseException("authentication failed [jump_host=" + host + ", jump_port=" + port + ", jump_user:" + user + ", jump_ssh_auth_method="
                        + authenticationMethod + ", jump_ssh_auth_file=" + authenticationFilename);
            }
            return this.getSshConnection();
        } catch (Exception e) {
            if (this.getSshConnection() != null) {
                try {
                    this.getSshConnection().close();
                    this.setSshConnection(null);
                } catch (Exception ex) {
                    // gracefully ignore this error
                }
            }
            RaiseException(e.getMessage());
        }
        return null;
    }

    protected void getBaseParameters() throws Exception {
        try {
            host = getParam(conParamJUMP_HOST, "");
            if (getString(conParamJUMP_HOST).isEmpty()) {
                RaiseException("no host name or ip address was specified as parameter [jump_host]");
            }
            if (getString("jump_port").isEmpty()) {
                arguments.put("jump_port", "22");
            }
            try {
                port = Integer.parseInt(getString("jump_port"));
                getLogger().debug1(".. parameter [jump_port]: " + port);
            } catch (Exception ex) {
                RaiseException("illegal non-numeric value for parameter [jump_port]: " + getString("jump_port"));
            }
            if (!getString("jump_user").isEmpty()) {
                user = getString("jump_user");
                getLogger().debug1(".. parameter [jump_user]: " + user);
            } else {
                RaiseException("no user name was specified as parameter [jump_user]");
            }
            if (!getString("jump_password").isEmpty()) {
                password = getString("jump_password");
                getLogger().debug1(".. parameter [jump_password]: ********");
            } else {
                password = "";
            }
            if (!getString("jump_proxy_host").isEmpty()) {
                proxyHost = getString("jump_proxy_host");
                getLogger().debug1(".. parameter [jump_proxy_host]: " + proxyHost);
            } else {
                proxyHost = "";
            }
            if (!getString("jump_proxy_port").isEmpty()) {
                try {
                    proxyPort = Integer.parseInt(getString("jump_proxy_port"));
                    getLogger().debug1(".. parameter [jump_proxy_port]: " + proxyPort);
                } catch (Exception ex) {
                    RaiseException("illegal non-numeric value for parameter [jump_proxy_port]: " + getString("jump_proxy_port"));
                }
            } else {
                proxyPort = 3128;
            }
            if (getString("jump_proxy_user") != null && !getString("jump_proxy_user").isEmpty()) {
                proxyUser = getString("jump_proxy_user");
                getLogger().debug1(".. parameter [jump_proxy_user]: " + proxyUser);
            } else {
                proxyUser = "";
            }
            if (!getString("jump_proxy_password").isEmpty()) {
                proxyPassword = getString("jump_proxy_password");
                getLogger().debug1(".. parameter [jump_proxy_password]: ********");
            } else {
                proxyPassword = "";
            }
            String authMeth = getString("jump_ssh_auth_method");
            if (!getString("jump_auth_method").isEmpty()) {
                authMeth = getString("jump_auth_method");
            } else {
                authMeth = getString("jump_ssh_auth_method");
            }
            if (!authMeth.isEmpty()) {
                if ("publickey".equalsIgnoreCase(authMeth) || conParamPASSWORD.equalsIgnoreCase(authMeth)) {
                    authenticationMethod = authMeth;
                    getLogger().debug1(".. parameter [jump_ssh_auth_method]: " + authenticationMethod);
                } else {
                    RaiseException("invalid authentication method [publickey, password] specified: " + authMeth);
                }
            } else {
                authenticationMethod = "publickey";
            }
            String authFile = getString("jump_ssh_auth_file");
            if (!authFile.isEmpty()) {
                authenticationFilename = authFile;
                getLogger().debug1(".. parameter [jump_ssh_auth_file]: " + authenticationFilename);
            } else {
                if ("publickey".equalsIgnoreCase(authenticationMethod)) {
                    RaiseException("no authentication filename was specified as parameter [jump_ssh_auth_file");
                }
            }
        } catch (Exception e) {
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
                if (line == null) {
                    break;
                }
                getLogger().debug9(line);
                stdOut += line;
            }
            getLogger().debug9("output to stderr for remote command: " + checkShellCommand);
            BufferedReader stderrReader = new BufferedReader(new InputStreamReader(stderr));
            while (true) {
                String line = stderrReader.readLine();
                if (line == null) {
                    break;
                }
                getLogger().debug1(line);
            }
            if (stdOut.indexOf("cmd.exe") > -1) {
                getLogger().debug3("Remote shell is Windows shell.");
                return true;
            }
        } catch (Exception e) {
            try {
                getLogger().warn("Failed to check if remote system is windows shell: " + e);
            } catch (Exception es) {
                LOGGER.warn(" Failed to check if remote system is windows shell: " + e.getMessage(), e);
            }
        } finally {
            if (session != null) {
                try {
                    session.close();
                } catch (Exception e) {
                    try {
                        getLogger().warn("Failed to close session: " + e);
                    } catch (Exception ea) {
                        LOGGER.warn(" Failed to close session: " + e.getMessage(), e);
                    }
                }
            }
        }
        return false;
    }

    protected void setCommandDelimiter(final String commandDelimiter) {
        this.commandDelimiter = commandDelimiter;
    }

    class RemoteConsumer extends Thread {

        private final StringBuffer sbuf;
        private boolean writeCurrentline = false;
        private final InputStream stream;
        boolean end = false;

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
                } else {
                    currentLine += outstring;
                }
            }
        }

        @Override
        public void run() {
            byte[] buff = new byte[64];
            try {
                while (!end) {
                    buff = new byte[8];
                    int len = stream.read(buff);
                    if (len == -1) {
                        return;
                    }
                    addText(buff, len);
                }
            } catch (Exception e) {
            }
        }

        public synchronized void end() {
            end = true;
        }
    }

    protected void readSettings(final boolean checkJumpArguments) throws Exception {
        try {
            mergeSettings();
            if (checkJumpArguments) {
                hasJumpArguments();
            }
        } catch (Exception e) {
            RaiseException("error in  " + sos.util.SOSClassUtil.getMethodName() + " , cause: " + e.getMessage(), e);
        }
    }

    public static String getVersion() {
        return VersionInfo.VERSION_STRING;
    }

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
                if (Check4MultipleFileSpecs(key)) {
                    String[] split = value.split("::");
                    Properties newArg = (Properties) arg.clone();
                    newArg.put(conSettingFILE_SPEC, split[0]);
                    logger.info("found multiple file_spec parameter. Actual is : " + split[0]);
                    noJumpParameter.add(key);
                    if (split.length > 1) {
                        noJumpParameter.add(split[1]);
                        String newParameterSet = sosString.parseToString(arg, split[1]);
                        String[] splitParams = newParameterSet.split("::");
                        for (String splitParam2 : splitParams) {
                            String s = sosString.parseToString(splitParam2);
                            if (!s.isEmpty()) {
                                String[] splitParam = s.split("=");
                                newArg.put(sosString.parseToString(splitParam[0]), splitParam.length == 1 ? ""
                                        : sosString.parseToString(splitParam[1]));
                            }
                        }
                    }
                    String index = key != null && key.length() > conSettingFILE_SPEC.length() ? key.substring(conSettingFILE_SPEC.length()) : "";
                    newArg.put("index", index);
                    list.add(newArg);
                } else {
                    if ("transfer_success".equalsIgnoreCase(key)) {
                        Properties newArg = (Properties) arg.clone();
                        String newParameterSet = sosString.parseToString(arg.get(value));
                        String[] splitParams = newParameterSet.split("::");
                        for (String splitParam2 : splitParams) {
                            String s = sosString.parseToString(splitParam2);
                            String[] splitParam = s.split("=");
                            newArg.put(sosString.parseToString(splitParam[0]), splitParam.length == 1 ? "" : sosString.parseToString(splitParam[1]));
                        }
                        listOfSuccessTransfer.add(newArg);
                    } else if ("transfer_error".equalsIgnoreCase(key)) {
                        Properties newArg = (Properties) arg.clone();
                        String newParameterSet = sosString.parseToString(arg.get(value));
                        String[] splitParams = newParameterSet.split("::");
                        for (String splitParam2 : splitParams) {
                            String s = sosString.parseToString(splitParam2);
                            String[] splitParam = s.split("=");
                            newArg.put(sosString.parseToString(splitParam[0]), splitParam.length == 1 ? "" : sosString.parseToString(splitParam[1]));
                        }
                        listOfErrorTransfer.add(newArg);
                    }
                }
            }
            list = prepareInstall(list);
        } catch (Exception e) {
            RaiseException("error in  " + sos.util.SOSClassUtil.getMethodName() + " , cause: " + e.getMessage(), e);
        }
        list = sortListAtFileSpecNum(list);
        return list;
    }

    private static boolean Check4MultipleFileSpecs(final String pstrRegExp) {
        boolean flgMultipleFileSpecs = false;
        int intFileSpecSize = conSettingFILE_SPEC.length();
        if (pstrRegExp.startsWith(conSettingFILE_SPEC) && pstrRegExp.length() > intFileSpecSize) {
            String strFileSpecIndex = pstrRegExp.substring(intFileSpecSize);
            if (strFileSpecIndex.startsWith("_")) {
                strFileSpecIndex = strFileSpecIndex.substring(1);
            }
            int intIndex = 0;
            try {
                intIndex = new Integer(strFileSpecIndex);
            } catch (NumberFormatException e) {
            }
            if (intIndex > 0) {
                flgMultipleFileSpecs = true;
            }
        }
        return flgMultipleFileSpecs;
    }

    private static ArrayList sortListAtFileSpecNum(final ArrayList list) throws Exception {
        ArrayList sort = new ArrayList();
        try {
            sort = sortArrayList(list, "index");
            for (int i = 0; i < sort.size(); i++) {
                Properties p = (Properties) list.get(i);
                if (!sosString.parseToString(p.get("index")).isEmpty()) {
                    p.remove("index");
                }
            }
        } catch (Exception e) {
            RaiseException("error in  " + sos.util.SOSClassUtil.getMethodName() + " , cause: " + e.getMessage(), e);
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
            } catch (Exception e) {
                throw e;
            }
            return list;
        } catch (Exception e) {
            RaiseException("..error in " + SOSClassUtil.getMethodName() + " " + e.getMessage(), e);
        }
        return null;
    }

    private static ArrayList prepareInstall(final ArrayList list) throws Exception {
        ArrayList newList = new ArrayList();
        for (int i = 0; i < list.size(); i++) {
            Properties p = (Properties) list.get(i);
            newList.add(p);
            if (p.containsKey(conSettingOPERATION) && "install".equals(sosString.parseToString(p.get(conSettingOPERATION)))) {
                Properties newp = (Properties) p.clone();
                newp.put(conSettingOPERATION, "install_doc");
                newList.add(newp);
            }
        }
        return newList;
    }

    public boolean isWriteBannerHeader() {
        return writeBannerHeader;
    }

    public void setWriteBannerHeader(final boolean writeBannerHeader) {
        this.writeBannerHeader = writeBannerHeader;
    }

    private static String getUsage() {
        String usage =
                "Usage: sos.net.SOSFTPCommand -operation= -settings= -profile= -verbose=" + conNewLine
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
                    if (sosString.parseToString(arg.get("current_pid")).isEmpty() && !sosString.parseToString(System.getProperty("pid")).isEmpty()) {
                        arg.put("pid", sosString.parseToString(System.getProperty("pid")));
                    }
                    if (banner) {
                        logger.info("SOSFTP-I-0440: Transfer for file set no. " + i + " is being completed");
                    } else {
                        logger.debug1("SOSFTP-I-0441: All files have been transferred successfully. Transaction for file set no. " + i
                                + " is being completed");
                    }
                }
                if (sosString.parseToBoolean(arg.get("testmode"))) {
                    logger.info("SOSFTP-I-0442: Test mode is active, no transfers are effected");
                    arg.put("skip_transfer", "yes");
                    arg.put(conSettingREMOVE_FILES, "no");
                }
                if (conOperationSEND.equalsIgnoreCase(operation)) {
                    ftpCommand = new sos.net.sosftp.SOSFTPCommandSend(logger, arg);
                    pflgRC = ftpCommand.send();
                } else if (conOperationRECEIVE.equalsIgnoreCase(operation)) {
                    ftpCommand = new sos.net.sosftp.SOSFTPCommandReceive(logger, arg);
                    pflgRC = ftpCommand.receive();
                } else if (conOperationEXECUTE.equalsIgnoreCase(operation)) {
                    ftpCommand = new sos.net.sosftp.SOSFTPCommandSSH(logger, arg);
                    pflgRC = ftpCommand.execute();
                } else if ("make_temp_directory".equalsIgnoreCase(operation)) {
                    ftpCommand = new sos.net.sosftp.SOSFTPCommandSend(logger, arg);
                    pflgRC = ftpCommand.makeTempDirectory();
                } else if ("remove_temp_directory".equalsIgnoreCase(operation)) {
                    ftpCommand = new sos.net.sosftp.SOSFTPCommandSend(logger, arg);
                    pflgRC = ftpCommand.removeTempDirectory();
                } else if (conOperationDELETE.equalsIgnoreCase(operation) || conOperationREMOVE.equalsIgnoreCase(operation)) {
                    if (conOperationDELETE.equalsIgnoreCase(operation)) {
                        arg.put(conSettingOPERATION, conOperationREMOVE);
                    }
                    ftpCommand = new sos.net.sosftp.SOSFTPCommandReceive(logger, arg);
                    pflgRC = ftpCommand.remove();
                } else if ("install".equalsIgnoreCase(operation)) {
                    ftpCommand = new sos.net.sosftp.SOSFTPCommandSend(logger, arg);
                    pflgRC = ftpCommand.install();
                } else if ("install_doc".equalsIgnoreCase(operation)) {
                    ftpCommand = new sos.net.sosftp.SOSFTPCommandSend(logger, arg);
                    pflgRC = ftpCommand.install();
                } else if ("delete_local_files".equalsIgnoreCase(operation)) {
                    ftpCommand = new sos.net.sosftp.SOSFTPCommandSend(logger, arg);
                    pflgRC = ftpCommand.deleteLocalFiles();
                } else if ("rename_local_files".equalsIgnoreCase(operation)) {
                    ftpCommand = new sos.net.sosftp.SOSFTPCommandReceive(logger, arg);
                    pflgRC = ftpCommand.renameAtomicSuffixTransferFiles();
                } else {
                    LOGGER.debug(getUsage());
                    RaiseException("[ERROR] no valid operation was specified, use send|receive|remove|execute|install: " + operation);
                }
                if (logger.hasWarnings()) {
                    ftpCommand.getArguments().put(conParameterSTATUS, "error");
                } else {
                    ftpCommand.getArguments().put(conParameterSTATUS, conParamValueSUCCESS);
                }
                if (!sosString.parseToString(logger.getWarning()).isEmpty() || !sosString.parseToString(logger.getError()).isEmpty()) {
                    ftpCommand.getArguments().put("last_error", logger.getWarning() + " " + logger.getError());
                } else {
                    ftpCommand.getArguments().put(conParameterSTATUS, conParamValueSUCCESS);
                    ftpCommand.getArguments().put("last_error", "");
                }
                if (ftpCommand != null) {
                    if (banner) {
                        logger.info(normalizedPassword(ftpCommand.getBanner(false)));
                    } else {
                        logger.debug(normalizedPassword(ftpCommand.getBanner(false)));
                    }
                }
                if ((logger.hasErrors() || logger.hasWarnings()) && !continueOnError) {
                    return false;
                }
            }
            return pflgRC;
        } catch (Exception e) {
            LOGGER.warn("error in " + SOSClassUtil.getMethodName() + " cause: " + e.getMessage(), e);
            return false;
        }
    }

    private static Properties extractArguments(final String[] args) throws Exception {
        Properties arg = new Properties();
        try {
            String filename = "";
            for (String arg2 : args) {
                String[] split = arg2.split("=");
                String key = split[0].startsWith("-") ? split[0].substring(1) : split[0];
                if (split.length == 1 && !split[0].startsWith("-")) {
                    if (!key.trim().isEmpty()) {
                        logger.debug1("file name specified as argument: " + key);
                    }
                    filename = filename + key + ";";
                } else {
                    arg.put(key.toLowerCase(), arg2.substring(arg2.indexOf("=") + 1));
                }
            }
            if (filename != null && !filename.isEmpty()) {
                filename = filename.replaceAll(conRegExpBackslash, "/");
                filename = filename.endsWith(";") ? filename.substring(0, filename.length() - 1) : filename;
                arg.put(conSettingFILE_PATH, filename);
            }
            return arg;
        } catch (Exception e) {
            logger.warn("[ERROR] could not process arguments, cause: " + e.getMessage());
            throw e;
        }
    }

    public static void createLoggerObject(final String logFile, final int logLevel) throws Exception {
        if (!sosString.parseToString(logFile).isEmpty()) {
            logger = new SOSStandardLogger(logFile, logLevel);
        } else {
            logger = new SOSStandardLogger(logLevel);
        }
    }

    private static boolean onlyVersion(final Properties arg) throws Exception {
        if (arg.containsKey("version") && !arg.containsKey(conSettingOPERATION)) {
            logger.info("sosftp version \"" + getVersion() + "\"");
            return true;
        }
        return false;
    }

    protected boolean deleteLocalFiles() throws Exception {
        ArrayList transActionalLocalFiles = new ArrayList();
        String error = "";
        if (arguments.containsKey("files")) {
            transActionalLocalFiles = (ArrayList) arguments.get("files");
        }
        for (int i = 0; i < transActionalLocalFiles.size(); i++) {
            File localRemFile = null;
            if (transActionalLocalFiles.get(i) instanceof File) {
                localRemFile = (File) transActionalLocalFiles.get(i);
            } else {
                localRemFile = new File(sosString.parseToString(transActionalLocalFiles.get(i)));
            }
            if (!localRemFile.delete()) {
                error = error + localRemFile.getAbsolutePath() + ";";
            } else {
                this.getLogger().debug1("removing file: " + localRemFile.getAbsolutePath());
            }
            if (error != null && !error.isEmpty()) {
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
            if (arguments.containsKey("files")) {
                transActionalLocalFiles = (ArrayList) arguments.get("files");
            }
            for (int i = 0; i < transActionalLocalFiles.size(); i++) {
                String filename = sosString.parseToString(transActionalLocalFiles.get(i));
                if (!filename.trim().isEmpty()) {
                    File f = new File(filename);
                    File nf = new File(filename.substring(0, filename.lastIndexOf(atomicSuffix)));
                    if (nf.exists()) {
                        getLogger().debug9(nf.getCanonicalPath() + " exists and will be replaced");
                        nf.delete();
                    }
                    getLogger().debug("..rename " + f.getCanonicalPath() + " to " + nf.getName());
                    if (!f.renameTo(nf)) {
                        RaiseException("could not rename temporary file [" + f.getCanonicalPath() + "] to: " + nf.getAbsolutePath());
                    }
                }
            }
            return true;
        } catch (Exception e) {
            getLogger().warn("error in  " + sos.util.SOSClassUtil.getMethodName() + " , cause: " + e);
            return false;
        }
    }

    private static boolean doPostTransactionalOnSuccess() {
        try {
            boolean rc = true;
            banner = false;
            if (!listOfSuccessTransfer.isEmpty()) {
                Properties p = (Properties) listOfSuccessTransfer.get(0);
                String operation = sosString.parseToString(p.get(conSettingOPERATION));
                rc = transfer(listOfSuccessTransfer, operation, p, true, false);
            }
            banner = true;
            if (!rc) {
                RaiseException("All files have been transferred successfully, however, the transaction could not be completed");
            }
            return true;
        } catch (Exception e) {
            try {
                logger.warn("error in doPostTransactionalOnSuccess, cause: " + e.toString());
            } catch (Exception ex) {
            }
            doPostTransactionalOnError();
            return false;
        }
    }

    private static boolean doPostTransactionalOnError() {
        try {
            boolean rc = false;
            banner = false;
            if (!listOfErrorTransfer.isEmpty()) {
                Properties p = (Properties) listOfErrorTransfer.get(0);
                String operation = sosString.parseToString(p.get(conSettingOPERATION));
                transfer(listOfErrorTransfer, operation, p, rc, true);
            }
        } catch (Exception e) {
            try {
                logger.warn("error in doPostTransactionalOnError, cause: " + e.toString());
            } catch (Exception ex) {
            }
        } finally {
            banner = true;
        }
        return false;
    }

    protected static String normalizedPassword(String str) {
        try {
            if (str == null || str.trim().isEmpty()) {
                return "";
            }
            ArrayList namesOfPassword = sosConfiguration.getPasswordnames();
            if (!namesOfPassword.contains(conParamPASSWORD)) {
                namesOfPassword.add(conParamPASSWORD);
            }
            for (int i = 0; i < namesOfPassword.size(); i++) {
                String pw = sosString.parseToString(namesOfPassword.get(i));
                if (pw.trim().isEmpty()) {
                    continue;
                }
                int pos1 = str.indexOf(pw + "=");
                if (pos1 > -1) {
                    int pos2 = str.indexOf(" ", pos1);
                    if (pos2 == -1 || pos1 > pos2) {
                        pos2 = str.length();
                    }
                    str = str.substring(0, pos1) + pw + "=***** " + str.substring(pos2);
                }
                pos1 = str.indexOf("-ssh_auth_file=\"filecontent:");
                if (pos1 > -1) {
                    int pos2 = -1;
                    if (pos1 == -1) {
                        return str;
                    }
                    pos2 = str.indexOf("-----END DSA PRIVATE KEY-----", pos1) + "-----END DSA PRIVATE KEY-----".length() + 2;
                    if (pos2 == -1 || pos1 > pos2) {
                        pos2 = str.length();
                    }
                    str = str.substring(0, pos1) + "-ssh_auth_file=\"filecontent:*****" + str.substring(pos2);
                }
            }
            return str;
        } catch (Exception e) {
            return "";
        }
    }

    public String getPids() throws Exception {
        String getPidsExe = "";
        if (getBool("testmode")) {
            return "";
        }
        if (System.getProperty("os.name").toLowerCase().indexOf("wind") > -1 && (getPidsExe == null || getPidsExe.isEmpty())) {
            if (new File("getParentId.exe").exists()) {
                getPidsExe = new File("getParentId.exe").getCanonicalPath();
            } else {
                getLogger().debug5("creating getParentId.exe ");
                java.net.URL url = ClassLoader.getSystemResource("getParentId.exe");
                getLogger().debug1("found url=" + url);
                InputStream in = getClass().getClassLoader().getSystemResourceAsStream("getParentId.exe");
                getLogger().debug9("get InputStream to create new getParentId.exe=" + in);
                if (in == null) {
                    getLogger().debug9("try again to get InputStream to create new getParentId.exe=" + in);
                    in = getClass().getClassLoader().getResourceAsStream("getParentId.exe");
                    getLogger().debug9("InputStream is =" + in);
                }
                if (in != null) {
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
                    } finally {
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
        String pid = getPID(getPidsExe);
        if (pid != null && !pid.isEmpty()) {
            logger.debug1("current_pid is " + pid);
        }
        return pid;
    }

    public String getPID(final String getPidExe) throws IOException {
        String pid = "";
        String cmd[];
        if (System.getProperty("os.name").toLowerCase().indexOf("wind") == -1) {
            cmd = new String[] { "/bin/sh", "-c", "echo $$ $PPID" };
        } else {
            if (getPidExe == null || getPidExe.isEmpty()) {
                throw new IOException("executables Files getpids.exe or getParentId.exe not found. Check Installation.");
            }
            cmd = new String[] { getPidExe };
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
                    stok.nextToken();
                }
                pid = stok.nextToken();
            }
            if (pid != null) {
                System.setProperty("pid", pid);
            }
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
            if (pos1 == -1) {
                pos1 = arg.length();
            }
            if (!sosString.parseToString(arg).startsWith("-") && sosString.parseToString(arg).indexOf("=") == -1) {
                filename = filename.length() > 0 ? filename + ";" + arg : arg;
            } else {
                String key = arg.substring(pos0, pos1);
                String value = arg.length() == pos1 ? "" : arg.substring(pos1 + 1);
                schedulerParams.put(key, value);
            }
        }
        if (!sosString.parseToString(filename).trim().isEmpty()) {
            schedulerParams.put(conSettingFILE_PATH, filename);
        }
        String[] retVal = new String[schedulerParams.size()];
        Iterator it = schedulerParams.keySet().iterator();
        while (it.hasNext()) {
            Object key = it.next();
            Object val = schedulerParams.get(key);
            retVal[++index] = "-" + sosString.parseToString(key) + "=" + sosString.parseToString(val);
        }
        return retVal;
    }

    private static Properties convertParameters() throws Exception {
        try {
            Properties arg_s = sosConfiguration.getParameterAsProperties();
            if (!sosString.parseToString(arg_s.get("settings")).isEmpty() && !sosString.parseToString(arg_s.get("include")).isEmpty()) {
                sos.configuration.SOSConfiguration config_ =
                        new sos.configuration.SOSConfiguration(sosString.parseToString(arg_s.get("settings")),
                                sosString.parseToString(arg_s.get("include")), logger);
                SOSConfigurationItem[] items = config_.checkConfigurationItems(null);
                arg_s.putAll(config_.getParameterAsProperties());
            }
            arg_s.remove("settings");
            arg_s.remove("profile");
            arg_s.remove("include");
            arg_s = substituteEnvironment(arg_s);
            return arg_s;
        } catch (Exception e) {
            RaiseException("error in " + sos.util.SOSClassUtil.getMethodName() + ": cause: " + e.getMessage(), e);
        }
        return null;
    }

    private static String checkOperation() throws Exception {
        try {
            String operation = sosConfiguration.getConfigurationItemByName(conSettingOPERATION).getValue();
            if (sosString.parseToString(operation).isEmpty()) {
                logger.warn(getUsage());
                RaiseException("missing command line parameter: operation");
            }
            return operation;
        } catch (Exception e) {
            RaiseException("error in " + sos.util.SOSClassUtil.getMethodName() + ": cause: " + e.getMessage(), e);
        }
        return null;
    }

    public boolean transfer() {
        boolean rc = false;
        String operation = "";
        listOfSuccessTransfer = new ArrayList();
        listOfErrorTransfer = new ArrayList();
        try {
            Properties env = getEnvVars(logger);
            env.putAll(schedulerParams);
            operation = checkOperation();
            Properties arg_s = new Properties();
            arg_s.putAll(env);
            arg_s.putAll(convertParameters());
            ArrayList extractArguments = getFileOperations(arg_s);
            try {
                banner = true;
                rc = transfer(extractArguments, operation, arg_s, rc, false);
                if (rc) {
                    rc = doPostTransactionalOnSuccess();
                } else {
                    rc = doPostTransactionalOnError();
                }
            } catch (Exception e) {
                banner = true;
                throw e;
            }
        } catch (Exception e) {
            try {
                if (ftpCommand != null) {
                    if (banner) {
                        logger.info(ftpCommand.getBanner(false));
                    } else {
                        logger.debug(ftpCommand.getBanner(false));
                    }
                } else {
                    LOGGER.debug(e.getMessage(), e);
                }
            } catch (Exception x) {
                LOGGER.debug(e.getMessage(), e);
            }
            doExit(1);
        } finally {
            try {
                if (ftpCommand != null) {
                    ftpCommand.appendHistoryFile();
                    transactionalHistoryFile = null;
                    ftpCommand.sendTransactionalSchedulerRequestFile();
                }
                logger.close();
            } catch (Exception e) {
                LOGGER.debug(e.getMessage(), e);
            }
        }
        return rc;
    }

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
            String version = "SOSFTP - " + VersionInfo.VERSION_STRING;
            LOGGER.info(version);
            LOGGER.info(conSVNVersion);
            Properties env = getEnvVars(logger);
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
            } catch (Exception e) {
                LOGGER.error("[ERROR] could not init SOSArguments, cause: " + e.getMessage(), e);
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
                SOSConfigurationItem[] items = sosConfiguration.checkConfigurationItems(null);
            } catch (Exception e) {
                LOGGER.debug("[ERROR] could not init Configuration, cause: " + e.getMessage(), e);
                throw e;
            }
            operation = checkOperation();
            Properties arg_s = convertParameters();
            logLevel = getIntArg(arg_s, "verbose", SOSLogger.INFO);
            logFile = getStringArg(arg_s, "log_filename", "");
            createLoggerObject(logFile, logLevel);
            ArrayList extractArguments = getFileOperations(arg_s);
            try {
                banner = true;
                rc = transfer(extractArguments, operation, arg_s, rc, false);
                boolean flgRC = false;
                if (rc) {
                    rc = doPostTransactionalOnSuccess();
                } else {
                    rc = doPostTransactionalOnError();
                }
            } catch (Exception e) {
                banner = true;
                throw e;
            }
        } catch (Exception e) {
            try {
                if (ftpCommand != null) {
                    if (banner) {
                        logger.info(ftpCommand.getBanner(false));
                    } else {
                        logger.debug(ftpCommand.getBanner(false));
                    }
                } else {
                    LOGGER.error(e.getMessage(), e);
                }
            } catch (Exception x) {
                LOGGER.error(e.getMessage(), e);
            }
            doExit(1);
        } finally {
            try {
                if (ftpCommand != null) {
                    ftpCommand.appendHistoryFile();
                    ftpCommand.sendTransactionalSchedulerRequestFile();
                }
                logger.close();
            } catch (Exception e) {
                LOGGER.debug(e.getMessage(), e);
            }
            doExit(rc ? 0 : 1);
        }
    }

    protected static void doExit(final int pintExitcode) {
        if (gflgUseSystemExit) {
            if (pintExitcode != 0) {
                LOGGER.debug("System-Exit code is " + pintExitcode);
            }
            System.exit(pintExitcode);
        }
    }

    public Vector<String> getFilelist() throws Exception {
        return filelist;
    }

    public Vector<File> getTransferredFilelist() throws Exception {
        if (transferFileList == null) {
            transferFileList = new Vector<File>();
        }
        return transferFileList;
    }

    public Integer getExitStatus() {
        if (exitStatus != null) {
            return exitStatus;
        } else {
            return new Integer(0);
        }
    }

    protected boolean getBooleanParam(final String pstrParamName, final String pstrDefaultvalue) throws Exception {
        String strResult = getParam(pstrParamName, pstrDefaultvalue);
        if ("true".equalsIgnoreCase(strResult.toString()) || "yes".equalsIgnoreCase(strResult.toString()) || "1".equals(strResult)) {
            return true;
        }
        return false;
    }

    protected String getParam(final String pstrParamName, final String pstrDefaultvalue) throws Exception {
        String strRet = "";
        String strVal = "";
        try {
            strVal = getString(pstrParamName.toLowerCase());
            if (strVal.isEmpty()) {
                String strNewParamName = pstrParamName.replaceAll("_", "");
                strVal = getString(strNewParamName.toLowerCase());
                if (strVal.isEmpty()) {
                    strNewParamName = pstrParamName.replaceAll("-", "_");
                    strVal = getString(strNewParamName.toLowerCase());
                }
            }
        } catch (Exception e) {
        }
        this.getLogger().debug(String.format(".. get Parameter '%1$s' with value '%2$s', default is '%3$s'", pstrParamName, strVal, pstrDefaultvalue));
        if (!strVal.isEmpty()) {
            strRet = strVal;
        } else {
            strRet = pstrDefaultvalue;
            if (arguments != null) {
                arguments.put(pstrParamName, pstrDefaultvalue);
            }
        }
        return strRet;
    }

    protected String getString(final String pstrParameterName) {
        String strR = "";
        try {
            strR = sosString.parseToString(arguments.get(pstrParameterName)).trim();
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        return strR;
    }

    protected String getQuotedString(final String pstrParameterName) {
        return "\"" + getString(pstrParameterName) + "\"";
    }

    protected String getKeywordValuePair(final String pstrParameterName) {
        return " -" + pstrParameterName + "=" + getQuotedString(pstrParameterName);
    }

    protected String getKeywordValuePair(final String pstrParameterName, final String pstrParameterValue) {
        return " -" + pstrParameterName + "=" + "\"" + pstrParameterValue + "\"";
    }

    protected String doEncoding(final String pstrStringToEncode, final String pstrEncoding) throws Exception {
        final String conUTF8UmlU = "\u00fc";
        final String conUTF8UmlBigA = "\u00c4";
        final String conUTF8UmlBigO = "\u00d6";
        final String conUTF8UmlBigU = "\u00dc";
        final String conUTF8UmlA = "\u00e4";
        final String conUTF8UmlO = "\u00f6";
        final String conUTF8UmlS = "\u00DF";
        String strEncodedString = pstrStringToEncode;
        if (!pstrEncoding.isEmpty()) {
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
    }

    protected String doEncodeUmlaute(final String pstrStringToEncode, final String pstrEncoding) throws Exception {
        final String conUTF8UmlU = "ü";
        final String conUTF8UmlBigA = "\u00c4";
        final String conUTF8UmlBigO = "\u00d6";
        final String conUTF8UmlBigU = "\u00dc";
        final String conUTF8UmlA = "ä";
        final String conUTF8UmlO = "ö";
        final String conUTF8UmlS = "\u00df";
        String strEncodedString = pstrStringToEncode;
        if (!pstrEncoding.isEmpty()) {
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
    }

    protected void retrieveCommonParameters() throws Exception {
        flgUsePathAndFileName4Matching = getBooleanParam(SOSFTPCommand.USE_PATH_AND_FILE_NAME_4_MATCHING, "false");
        flgCheckServerFeatures = getBooleanParam(SOSFTPCommand.CHECK_SERVER_FEATURES, "");
        strPreFtpCommands = getParam(PRE_FTP_COMMANDS, "");
        strControlEncoding = getParam(SOSFTPCommand.CONTROL_ENCODING, "");
        strFileNameEncoding = getParam(SOSFTPCommand.FILENAME_ENCODING, "");
        flgConvertUmlaute = getBooleanParam(SOSFTPCommand.CONVERT_UMLAUTE, "false");
    }

    public void setParam(final String pstrParamName, final String pstrParamValue) throws Exception {
        arguments.put(pstrParamName, pstrParamValue);
        logger.debug(String.format("set param '%1$s' to value '%2$s'", pstrParamName, pstrParamValue));
    }

    protected String stripRemoteDirName(final String pstrRootPath, final String pstrPathName) throws Exception {
        String strResult = pstrPathName;
        String strR = new File(pstrRootPath).getAbsolutePath();
        String strP = new File(pstrPathName).getAbsolutePath();
        if (strP.startsWith(strR)) {
            strResult = strP.substring(strR.length());
            if (strResult.contains(File.separator)) {
                if (strResult.startsWith(File.separator)) {
                    strResult = strResult.substring(1);
                }
                if (strResult.contains(File.separator)) {
                    strResult = "." + File.separator + strResult;
                }
            }
            strResult = adjustSeparator(strResult);
        }
        return strResult;
    }

    protected String adjustSeparator(final String pstrPathName) {
        String strRet = pstrPathName;
        String[] strA = pstrPathName.split("[/\\\\]");
        if (strA.length > 0) {
            strRet = "";
            for (String string : strA) {
                if (!string.isEmpty()) {
                    strRet = strRet + string + File.separator;
                }
            }
            strRet = strRet.substring(0, strRet.length() - 1);
        }
        return strRet;
    }

    protected static void RaiseException(final Exception e, final String pstrM) {
        try {
            logger.error(pstrM);
        } catch (Exception e1) {
            LOGGER.error(pstrM);
        }
        if (e != null) {
            throw new JobSchedulerException(pstrM, e);
        } else {
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
        if (lstrPathName.startsWith(".") || lstrPathName.startsWith("..")) {
            flgOK = false;
        } else {
            if (lstrPathName.contains(":/") || lstrPathName.startsWith("/")) {
                flgOK = true;
            } else {
                flgOK = lstrPathName.contains("/") == true;
            }
        }
        return flgOK;
    }

}